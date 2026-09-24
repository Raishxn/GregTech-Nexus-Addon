package com.raishxn.gtna.planner.neoforge.crafting;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

/** Worker failure boundary. Async failures propagate through the Future, with no AE2 retry. */
final class PlanningTask {

    private PlanningTask() {}

    static <T> Callable<T> classified(Callable<T> calculation, Consumer<String> status,
                                      Consumer<RuntimeException> reportUnexpected) {
        return () -> {
            try {
                return calculation.call();
            } catch (CancellationException cancelled) {
                status.accept("cancelled");
                throw cancelled;
            } catch (TimeoutException deadline) {
                status.accept("timeout");
                throw new IllegalStateException("RaishxCore planning deadline exceeded", deadline);
            } catch (RuntimeException unexpected) {
                status.accept("failed: " + unexpected);
                reportUnexpected.accept(unexpected);
                throw unexpected;
            }
        };
    }
}
