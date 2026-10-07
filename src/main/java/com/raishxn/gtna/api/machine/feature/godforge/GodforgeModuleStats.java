package com.raishxn.gtna.api.machine.feature.godforge;

import java.math.BigInteger;

/**
 * Values the Forge of Gods pushes to each connected module every five seconds, and the tallies a module reports
 * back. Mirrors the fields of GTNH {@code MTEBaseModule}.
 */
public final class GodforgeModuleStats {

    public enum Type {
        SMELTING,
        MOLTEN,
        PLASMA,
        EXOTIC
    }

    public final Type type;
    public boolean magmatterMode;
    public boolean connected;
    public int heat;
    public int heatForOC;
    public int calculatedMaxParallel;
    public double speedBonus = 1;
    public double energyDiscount = 1;
    public long processingVoltage = 2_000_000_000L;
    public int plasmaTier;
    /**
     * GTNA: highest Thread Hatch tier that adds threads (GPCI/CD/END unlock more); higher tiers form but do nothing.
     */
    public int maxThreadHatchTier = com.gregtechceu.gtceu.api.GTValues.UHV;
    public double overclockTimeFactor = 2;
    public boolean upgrade83;
    public boolean multiStepPlasma;
    public boolean magmatterCapable;
    public boolean voltageConfig;
    public boolean inversion;
    public long currentRecipeHeat;
    public BigInteger powerTally = BigInteger.ZERO;
    public long recipeTally;

    public GodforgeModuleStats(Type type) {
        this.type = type;
    }

    /** Heat energy discount per overclock: 0.92 with IMKG, otherwise 0.95. */
    public double heatEnergyDiscount() {
        return upgrade83 ? 0.92 : 0.95;
    }
}
