package com.raishxn.gtna.common.machine.multiblock.module.steamElevator;

/**
 * GTNA-native replacement for GTNL's {@code SteamElevatorModuleBase}.
 *
 * <p>
 * Every module is itself a {@code 1x5x2} multiblock
 * ({@link SteamElevatorModuleMachine}); the Steam Elevator host scans its twelve fixed module slots
 * and charges the <b>formed</b> module controllers it finds there. This keeps GTNL's modular
 * system (one multiblock per capability) while staying inside GTCEu's controller lifecycle: a stray
 * block or part in a slot is never connected, so it cannot inflate the installed-module count.
 */
public interface ISteamElevatorModule {

    /** GTNL {@code mTier}; drives the buffer size and the effect strength/range. */
    int getModuleTier();

    /** Radius (in blocks) the module affects, centred on the module itself. */
    int getEffectRange();

    /** EU consumed per active server tick. 0 means the module is a passive one. */
    long getEnergyUsage();

    /** Adds energy to this module's internal buffer, returns the amount actually accepted. */
    long receiveEnergy(long amount);

    /** Consumes EU from the internal buffer; returns {@code false} when it cannot pay. */
    boolean consumeEnergy(long amount);

    long getEnergyStored();

    long getEnergyCapacity();

    /**
     * Called by the owning {@link SteamElevator} every server tick while the elevator is formed and
     * working. Implementations should pay {@link #getEnergyUsage()} and apply their effect here; the
     * controller only invokes this after the per-module energy share has been delivered.
     */
    void onElevatorTick(SteamElevator elevator);

    /** Called when the elevator stops working or is dismantled, so modules can clean up effects. */
    default void onElevatorStop() {}
}
