package com.raishxn.gtna.planner.api.crafting.planner;

import java.time.Duration;
import java.util.Objects;

/** Hard safety bounds for one planning attempt. */
public record PlanningLimits(long maxOperations, int maxDepth, Duration timeout, int checkpointInterval) {

    public static final PlanningLimits DEFAULT = new PlanningLimits(10_000_000L, 100_000,
            Duration.ofSeconds(2), 256);

    public PlanningLimits {
        if (maxOperations < 1L) throw new IllegalArgumentException("maxOperations must be positive");
        if (maxDepth < 1) throw new IllegalArgumentException("maxDepth must be positive");
        Objects.requireNonNull(timeout, "timeout");
        if (timeout.isNegative() || timeout.isZero()) throw new IllegalArgumentException("timeout must be positive");
        if (checkpointInterval < 1) throw new IllegalArgumentException("checkpointInterval must be positive");
    }
}
