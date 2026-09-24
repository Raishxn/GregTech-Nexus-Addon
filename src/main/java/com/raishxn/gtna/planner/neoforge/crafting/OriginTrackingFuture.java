package com.raishxn.gtna.planner.neoforge.crafting;

import appeng.api.networking.crafting.ICraftingPlan;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/** Immutable origin tag around an ordinary AE2 calculation future. */
final class OriginTrackingFuture implements Future<ICraftingPlan>, OriginAwareFuture {

    private final Future<ICraftingPlan> delegate;
    private final PlanningOrigin origin;

    OriginTrackingFuture(Future<ICraftingPlan> delegate, PlanningOrigin origin) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        this.origin = Objects.requireNonNull(origin, "origin");
    }

    @Override
    public PlanningOrigin planningOrigin() {
        return origin;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return delegate.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return delegate.isCancelled();
    }

    @Override
    public boolean isDone() {
        return delegate.isDone();
    }

    @Override
    public ICraftingPlan get() throws InterruptedException, ExecutionException {
        return delegate.get();
    }

    @Override
    public ICraftingPlan get(long timeout, TimeUnit unit)
                                                          throws InterruptedException, ExecutionException,
                                                          TimeoutException {
        return delegate.get(timeout, unit);
    }
}
