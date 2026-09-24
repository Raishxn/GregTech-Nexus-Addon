package com.raishxn.gtna.planner.neoforge.crafting;

import java.util.concurrent.Future;

/**
 * The engine that produced one particular AE2 crafting calculation.
 *
 * <p>
 * This is deliberately carried by the request future rather than inferred from a bridge's last
 * diagnostic. A grid may have several calculations in flight, and a capture can fall back to AE2
 * after another request already completed in RaishxCore.
 * </p>
 */
public enum PlanningOrigin {

    /** No RaishxCore-owned future was returned for this request. */
    NONE,
    /** The immutable snapshot was planned by RaishxCore. */
    RAISHX,
    /** A deferred RaishxCore capture was discarded and AE2 planned the request. */
    AE2;

    /** Finds the origin associated with a calculation future, if it has one. */
    public static PlanningOrigin of(Future<?> future) {
        return future instanceof OriginAwareFuture tracked ? tracked.planningOrigin() : NONE;
    }
}
