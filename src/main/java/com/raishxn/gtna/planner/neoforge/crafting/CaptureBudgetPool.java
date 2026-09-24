package com.raishxn.gtna.planner.neoforge.crafting;

import java.time.Duration;
import java.util.Objects;

/**
 * Server-wide capture budget for one server tick, shared by every grid.
 *
 * <p>
 * The per-grid slice budget alone would let {@code N} grids each spend a full slice in the same
 * tick. This pool hands out reservations from one tick allowance instead, so the total main-thread
 * cost stays bounded no matter how many captures are in flight. Work that cannot be reserved waits
 * for the next tick; it is never served by a fallback just because the pool ran out.
 *
 * <p>
 * Accounting is exact arithmetic, never wall-clock reading, so the pool itself is deterministic
 * and unit testable: a caller reserves, runs its slice and settles the difference.
 */
public final class CaptureBudgetPool {

    private final long nanosPerTick;
    private long remainingNanos;
    private long ticks;
    private long reservations;
    private long grantedNanos;
    private long spentNanos;
    private long spentAtTickStart;
    private long exhaustedTicks;

    public CaptureBudgetPool(Duration tickBudget) {
        Objects.requireNonNull(tickBudget, "tickBudget");
        long nanos;
        try {
            nanos = tickBudget.toNanos();
        } catch (ArithmeticException overflow) {
            nanos = Long.MAX_VALUE;
        }
        if (nanos < 1L) throw new IllegalArgumentException("tick budget must be positive");
        this.nanosPerTick = nanos;
        this.remainingNanos = nanos;
    }

    /** Starts a new tick with the full allowance. Called once per server tick, before any request. */
    public void beginTick() {
        remainingNanos = nanosPerTick;
        ticks++;
        spentAtTickStart = spentNanos;
    }

    /**
     * Time every grid together really spent on capture since the current tick began, never what was
     * reserved for it. This is the per-tick sample the slice histograms report.
     */
    public long spentThisTick() {
        return spentNanos - spentAtTickStart;
    }

    /** Reserves up to {@code wantedNanos} from this tick, returning what was actually available. */
    public long reserve(long wantedNanos) {
        if (wantedNanos < 0L) throw new IllegalArgumentException("reservation must be non-negative");
        long granted = Math.min(wantedNanos, remainingNanos);
        if (granted <= 0L) {
            exhaustedTicks++;
            return 0L;
        }
        remainingNanos -= granted;
        reservations++;
        grantedNanos += granted;
        return granted;
    }

    /**
     * Settles a reservation with the time the slice really spent: the unused part returns to the tick,
     * and an overrun is charged in full. A slice only stops between two keys, so one unusually heavy key
     * can exceed its allowance; hiding that would let the next grid spend a budget that is already gone.
     * The allowance may therefore go negative, which only makes every later reservation impossible until
     * the next tick.
     */
    public void settle(long reserved, long spent) {
        if (reserved < 0L || spent < 0L) {
            throw new IllegalArgumentException("invalid settlement");
        }
        remainingNanos += reserved - spent;
        spentNanos += spent;
    }

    public long nanosPerTick() {
        return nanosPerTick;
    }

    public long remainingNanos() {
        return remainingNanos;
    }

    public boolean exhausted() {
        return remainingNanos <= 0L;
    }

    public Stats stats() {
        return new Stats(ticks, reservations, grantedNanos, spentNanos, exhaustedTicks);
    }

    /** Counters kept for diagnostics only; they never influence scheduling. */
    public record Stats(long ticks, long reservations, long grantedNanos, long spentNanos, long exhaustedTicks) {}
}
