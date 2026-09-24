package com.raishxn.gtna.planner;

import com.raishxn.gtna.config.ConfigHolder;
import com.raishxn.gtna.planner.api.crafting.planner.MissingWeightPolicy;

/** Runtime limits for the RaishxCore planner port on AE2 1.20.1. */
public final class CoreConfig {

    private static final PlannerPolicy DEFAULT_POLICY = new PlannerPolicy(
            2, 32, 2_000, 8, 10_000_000L, 100_000, 128,
            50, 100_000, 25_000, 64L * 1024 * 1024, 16, 128L * 1024 * 1024, 2, 512, 4, 8,
            4, 3, 10_000);

    private CoreConfig() {}

    public static boolean isPlannerEnabled() {
        return ConfigHolder.INSTANCE == null || ConfigHolder.INSTANCE.machines.nexusPlannerEnabled;
    }

    public static PlannerPolicy plannerPolicy() {
        return DEFAULT_POLICY;
    }

    public static MissingWeightPolicy missingWeightPolicy() {
        return MissingWeightPolicy.NONE;
    }

    public record PlannerPolicy(int workers, int queueCapacity, int timeoutMillis, int exactSearchChoiceLimit,
                                long maxOperations,
                                int maxDepth, int checkpointInterval, int snapshotTimeoutMillis,
                                int snapshotMaxEdges, int snapshotMaxKeys, long snapshotMaxEstimatedBytes,
                                int snapshotCacheEntries, long snapshotCacheBytes, int snapshotSliceMillis,
                                int snapshotSliceEdges, int snapshotTickBudgetMillis, int maxPendingCaptures,
                                int maxInFlightPerGrid, int circuitFailureThreshold, int circuitCooldownMillis) {

        public PlannerPolicy {
            if (workers < 1 || queueCapacity < 1 || timeoutMillis < 1 || exactSearchChoiceLimit < 1 ||
                    maxOperations < 1 || maxDepth < 1 || checkpointInterval < 1 || snapshotTimeoutMillis < 1 ||
                    snapshotMaxEdges < 1 || snapshotMaxKeys < 1 || snapshotMaxEstimatedBytes < 1 ||
                    snapshotCacheEntries < 1 || snapshotCacheBytes < 1 || snapshotSliceMillis < 1 ||
                    snapshotSliceEdges < 1 || snapshotTickBudgetMillis < 1 || maxPendingCaptures < 1 ||
                    maxInFlightPerGrid < 1 || circuitFailureThreshold < 1 || circuitCooldownMillis < 1) {
                throw new IllegalArgumentException("planner policy limits must be positive");
            }
        }
    }
}
