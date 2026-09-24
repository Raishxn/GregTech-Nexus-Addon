package com.raishxn.gtna.planner.neoforge.crafting;

import appeng.api.networking.crafting.ICraftingPlan;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Future handed to AE2 for a request whose graph capture did not finish inside one tick.
 *
 * <p>
 * AE2 expects an asynchronous result here: its own planner returns a future that is completed by a
 * worker which sleeps between ticks. This wrapper keeps that contract while the capture is being
 * completed cooperatively. It is handed the real planning future as soon as the capture finishes, and
 * every {@code Future} operation is then delegated, so callers that poll {@code isDone()} or block in
 * {@code get()} behave exactly as they do for a same-tick plan.
 *
 * <p>
 * Cancellation is forwarded both ways: a caller cancelling this future marks the request abandoned,
 * so the bridge stops feeding that capture and cancels the planning future when there already is one.
 */
final class DeferredPlanFuture implements Future<ICraftingPlan>, OriginAwareFuture {

    private final CompletableFuture<Future<ICraftingPlan>> handoff = new CompletableFuture<>();
    private final AtomicReference<Future<ICraftingPlan>> delegate = new AtomicReference<>();
    private final AtomicBoolean cancelled = new AtomicBoolean();
    private final AtomicReference<PlanningOrigin> origin = new AtomicReference<>(PlanningOrigin.NONE);

    /** Publishes the future that will produce the plan. Idempotent. */
    void complete(Future<ICraftingPlan> planning) {
        complete(planning, PlanningOrigin.RAISHX);
    }

    /** Publishes the future and the engine that actually owns its calculation. */
    void complete(Future<ICraftingPlan> planning, PlanningOrigin planningOrigin) {
        if (planning != null && delegate.compareAndSet(null, planning)) {
            origin.set(planningOrigin);
            handoff.complete(planning);
        }
    }

    /** Completes with cancellation for an invalidation that is not a caller cancellation. */
    void abandon() {
        cancelled.set(true);
        Future<ICraftingPlan> planning = delegate.get();
        if (planning != null) planning.cancel(false);
        handoff.cancel(false);
    }

    boolean abandoned() {
        return cancelled.get();
    }

    @Override
    public PlanningOrigin planningOrigin() {
        return origin.get();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        Future<ICraftingPlan> planning = delegate.get();
        cancelled.set(true);
        if (planning != null) planning.cancel(mayInterruptIfRunning);
        handoff.cancel(false);
        return true;
    }

    @Override
    public boolean isCancelled() {
        return cancelled.get() || handoff.isCancelled();
    }

    @Override
    public boolean isDone() {
        if (handoff.isCompletedExceptionally()) return true;
        Future<ICraftingPlan> planning = handoff.getNow(null);
        return planning != null && planning.isDone();
    }

    @Override
    public ICraftingPlan get() throws InterruptedException, ExecutionException {
        return handoff.get().get();
    }

    @Override
    public ICraftingPlan get(long timeout, TimeUnit unit)
                                                          throws InterruptedException, ExecutionException,
                                                          TimeoutException {
        long timeoutNanos = unit.toNanos(timeout);
        long started = System.nanoTime();
        Future<ICraftingPlan> planning = handoff.get(timeout, unit);
        long elapsed = Math.max(0L, System.nanoTime() - started);
        long remaining = Math.max(0L, timeoutNanos - elapsed);
        return planning.get(remaining, TimeUnit.NANOSECONDS);
    }
}
