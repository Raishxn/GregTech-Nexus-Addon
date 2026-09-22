package com.raishxn.gtna.common.machine.multiblock.module.steamElevator;

/**
 * GTNA-native replacement for GTNL's {@code SteamElevatorModuleBase}.
 *
 * <p>
 * Every module is itself a {@code 1x5x2} multiblock
 * ({@link SteamElevatorModuleMachine}); the Steam Elevator host scans its twelve fixed module slots
 * and connects the <b>formed</b> module controllers it finds there. This keeps GTNL's modular
 * system (one multiblock per capability) while staying inside GTCEu's controller lifecycle: a stray
 * block or part in a slot is never connected, so it cannot inflate the installed-module count.
 *
 * <p>
 * There is <b>no EU buffer</b> on either the elevator or the modules (author's model): the elevator
 * itself always runs when formed, and each module pays a steam upkeep drawn from the formed
 * structure's steam input hatches, which the host distributes to its connected modules.
 */
public interface ISteamElevatorModule {

    /** GTNL {@code mTier}; drives the upkeep and the effect strength/range. */
    int getModuleTier();

    /** Radius (in blocks) the module affects, centred on the module itself. */
    int getEffectRange();

    /** Steam (mB) consumed per active server tick. {@code 0} means the module is a passive one. */
    long getSteamUpkeep();

    /**
     * Called by the owning {@link SteamElevator} every server tick while the elevator is formed.
     * Implementations should pay {@link #getSteamUpkeep()} and apply their effect here; the host
     * only invokes this once the module is connected to it.
     */
    void onElevatorTick(SteamElevator elevator);

    /** Called when the elevator stops working or is dismantled, so modules can clean up effects. */
    default void onElevatorStop() {}
}
