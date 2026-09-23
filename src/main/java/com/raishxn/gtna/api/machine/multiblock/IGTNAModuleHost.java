package com.raishxn.gtna.api.machine.multiblock;

/**
 * Implemented at runtime on every {@code MultiblockControllerMachine} by GTNA's
 * {@code MultiblockControllerMachineMixin}. It exposes how many module (sub-pattern) structures
 * matched the controller on the last structure check, so a machine can display it in its UI.
 *
 * <p>
 * This is separate from {@link ISubPatternMachine}: the latter is an opt-in interface a machine
 * implements when it ships its own sub-patterns, while every multiblock has a formed-module count.
 */
public interface IGTNAModuleHost {

    /** How many modules (sub-patterns) matched on the last structure check. */
    int gtna$formedModuleCount();

    void gtna$setFormedModuleCount(int count);
}
