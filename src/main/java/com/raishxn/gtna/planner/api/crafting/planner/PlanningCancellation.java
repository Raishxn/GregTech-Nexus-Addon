package com.raishxn.gtna.planner.api.crafting.planner;

import java.util.concurrent.atomic.AtomicBoolean;

/** Cooperative cancellation checked at bounded intervals by the iterative planner. */
@FunctionalInterface
public interface PlanningCancellation {

    PlanningCancellation NEVER = () -> false;

    boolean isCancelled();

    static Source source() {
        return new Source();
    }

    final class Source implements PlanningCancellation {

        private final AtomicBoolean cancelled = new AtomicBoolean();

        public void cancel() {
            cancelled.set(true);
        }

        @Override
        public boolean isCancelled() {
            return cancelled.get();
        }
    }
}
