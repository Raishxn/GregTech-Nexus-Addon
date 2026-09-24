package com.raishxn.gtna.planner.neoforge.crafting;

import java.time.Duration;
import java.util.Objects;
import java.util.function.LongSupplier;

/** Per-grid consecutive-failure circuit with one recovery probe after each cooldown. */
final class PlannerCircuitBreaker {

    private final LongSupplier nanoTime;
    private State state = State.CLOSED;
    private int consecutiveFailures;
    private long retryAtNanos;

    PlannerCircuitBreaker() {
        this(System::nanoTime);
    }

    PlannerCircuitBreaker(LongSupplier nanoTime) {
        this.nanoTime = Objects.requireNonNull(nanoTime, "nanoTime");
    }

    synchronized boolean tryAcquire() {
        if (state == State.CLOSED) return true;
        if (state == State.HALF_OPEN || nanoTime.getAsLong() - retryAtNanos < 0L) return false;
        state = State.HALF_OPEN;
        return true;
    }

    synchronized void recordSuccess() {
        state = State.CLOSED;
        consecutiveFailures = 0;
        retryAtNanos = 0L;
    }

    synchronized void recordFailure(int threshold, Duration cooldown) {
        if (threshold < 1) throw new IllegalArgumentException("threshold must be positive");
        long cooldownNanos = positiveNanos(cooldown);
        if (state == State.HALF_OPEN || ++consecutiveFailures >= threshold) {
            state = State.OPEN;
            retryAtNanos = saturatedAdd(nanoTime.getAsLong(), cooldownNanos);
        }
    }

    synchronized void abortProbe(Duration cooldown) {
        if (state != State.HALF_OPEN) return;
        state = State.OPEN;
        retryAtNanos = saturatedAdd(nanoTime.getAsLong(), positiveNanos(cooldown));
    }

    synchronized void reset() {
        state = State.CLOSED;
        consecutiveFailures = 0;
        retryAtNanos = 0L;
    }

    synchronized Snapshot snapshot() {
        return new Snapshot(state, consecutiveFailures);
    }

    private static long positiveNanos(Duration duration) {
        Objects.requireNonNull(duration, "duration");
        if (duration.isZero() || duration.isNegative()) {
            throw new IllegalArgumentException("cooldown must be positive");
        }
        try {
            return duration.toNanos();
        } catch (ArithmeticException overflow) {
            return Long.MAX_VALUE;
        }
    }

    private static long saturatedAdd(long left, long right) {
        try {
            return Math.addExact(left, right);
        } catch (ArithmeticException overflow) {
            return Long.MAX_VALUE;
        }
    }

    enum State {
        CLOSED,
        OPEN,
        HALF_OPEN
    }

    record Snapshot(State state, int consecutiveFailures) {}
}
