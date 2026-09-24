package com.raishxn.gtna.planner.neoforge.crafting;

/** Implemented on AE2's provider registry; increments on every mutation, including within one tick. */
public interface PlannerRevisionSource {

    long raishxcore$getPatternRevision();
}
