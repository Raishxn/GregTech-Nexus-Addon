package com.raishxn.gtna.api.machine.feature.godforge;

/**
 * GT5-Unofficial {@code OverclockCalculator} (a3e1e112) as the Forge of Gods modules configure it: unlimited tier
 * skips, no amperage overclocks, optional heat overclocks/discount, configurable time reduction per overclock. Laser
 * overclocks are not used by the modules and are left out.
 */
public final class GodforgeOverclock {

    private static final int HEAT_DISCOUNT_THRESHOLD = 900;
    private static final int HEAT_OVERCLOCK_THRESHOLD = 1800;
    private static final double DURATION_DECREASE_PER_HEAT_OC = 4;

    public long recipeEUt;
    public long machineVoltage;
    public long machineAmperage = 1;
    public int duration;
    public int parallel = 1;
    public double eutModifier = 1;
    public double durationModifier = 1;
    public double eutIncreasePerOC = 4;
    public double durationDecreasePerOC = 2;
    public boolean amperageOC;
    public int maxOverclocks = Integer.MAX_VALUE;
    public int recipeHeat;
    public int machineHeat;
    public boolean heatOC;
    public boolean heatDiscount;
    public double heatDiscountExponent = 0.95;

    private int overclocks;
    private long calculatedConsumption;
    private int calculatedDuration;

    public long consumption() {
        return calculatedConsumption;
    }

    public int calculatedDuration() {
        return calculatedDuration;
    }

    public int overclocks() {
        return overclocks;
    }

    public double heatDiscountMultiplier() {
        int heatDiscounts = heatDiscount ? (machineHeat - recipeHeat) / HEAT_DISCOUNT_THRESHOLD : 0;
        return GodforgeMath.powInt(heatDiscountExponent, heatDiscounts);
    }

    private double machinePower() {
        return machineVoltage * (amperageOC ? machineAmperage : Math.min(machineAmperage, parallel));
    }

    public GodforgeOverclock calculate() {
        double duration = this.duration * durationModifier;
        double recipePower = recipeEUt * parallel * eutModifier * heatDiscountMultiplier();
        double machinePower = machinePower();
        int tiersAbove = tiersAbove(machinePower, recipePower);
        overclocks = Math.min(maxOverclocks, tiersAbove);
        if (!amperageOC) {
            int voltageTierMachine = (int) Math.max(log4ceil(machineVoltage / 8), 1);
            int voltageTierRecipe = (int) Math.max(log4ceil(recipeEUt / 8), 1);
            overclocks = Math.min(overclocks, voltageTierMachine - voltageTierRecipe);
        }
        overclocks = Math.max(overclocks, 0);
        int heatOverclocks = Math.min(heatOC ? (machineHeat - recipeHeat) / HEAT_OVERCLOCK_THRESHOLD : 0, overclocks);
        int regularOverclocks = overclocks - heatOverclocks;
        calculatedConsumption = (long) Math.ceil(recipePower * GodforgeMath.powInt(eutIncreasePerOC, overclocks));
        duration /= GodforgeMath.powInt(DURATION_DECREASE_PER_HEAT_OC, heatOverclocks);
        duration /= GodforgeMath.powInt(durationDecreasePerOC, regularOverclocks);
        calculatedDuration = (int) Math.max(duration, 1);
        return this;
    }

    /** Extra parallel factor once overclocks would go below one tick ({@code calculateMultiplierUnderOneTick}). */
    public double multiplierUnderOneTick() {
        double duration = this.duration * durationModifier;
        double recipePower = recipeEUt * parallel * eutModifier * heatDiscountMultiplier();
        double machinePower = machinePower();
        int voltageTierRecipe = (int) Math.max(log4ceil(recipeEUt / 8), 1);
        int voltageTierMachine = (int) Math.max(log4ceil(machineVoltage / 8), 1);
        int powerTiersAbove = tiersAbove(machinePower, recipePower);
        int voltageTiersAbove = voltageTierMachine - voltageTierRecipe;
        int overclocks = Math.max(0, Math.min(maxOverclocks, amperageOC ? powerTiersAbove : voltageTiersAbove));
        int heatOverclocks = Math.min(heatOC ? (machineHeat - recipeHeat) / HEAT_OVERCLOCK_THRESHOLD : 0, overclocks);
        int regularOverclocks = overclocks - heatOverclocks;
        double durationAfterHeatOC = duration / GodforgeMath.powInt(DURATION_DECREASE_PER_HEAT_OC, heatOverclocks);
        int neededHeatOverclocks = (int) Math.ceil(Math.log(duration) / Math.log(DURATION_DECREASE_PER_HEAT_OC));
        int neededRegularOverclocks = (int) Math.ceil(Math.log(durationAfterHeatOC) / Math.log(durationDecreasePerOC));
        int extraHeatOverclocks = Math.max(heatOverclocks - neededHeatOverclocks, 0);
        int extraRegularOverclocks = Math.max(regularOverclocks - neededRegularOverclocks, 0);
        double heatMultiplier = GodforgeMath.powInt(DURATION_DECREASE_PER_HEAT_OC, extraHeatOverclocks);
        double regularMultiplier = GodforgeMath.powInt(durationDecreasePerOC, extraRegularOverclocks);
        double correctionMultiplier;
        if (heatOverclocks >= neededHeatOverclocks) {
            correctionMultiplier = GodforgeMath.powInt(DURATION_DECREASE_PER_HEAT_OC, neededHeatOverclocks) / duration;
        } else if (regularOverclocks >= neededRegularOverclocks) {
            correctionMultiplier = GodforgeMath.powInt(durationDecreasePerOC, neededRegularOverclocks) /
                    durationAfterHeatOC;
        } else {
            return 1.0;
        }
        return Math.ceil(heatMultiplier * regularMultiplier * correctionMultiplier);
    }

    public static int tiersAbove(double power, double compareBase) {
        if (power < compareBase) return -1;
        long scale = 100L;
        if (power < Long.MAX_VALUE / scale) {
            long scaledPower = Math.round(power * scale);
            long scaledCompareBase = Math.round(compareBase * scale);
            return (int) log4(scaledPower / Math.max(scaledCompareBase, 32L * scale));
        }
        return (int) log4((long) (power / Math.max(compareBase, 32.0)));
    }

    public static long log4(long a) {
        if (a <= 1) return 0;
        return 63 - Long.numberOfLeadingZeros(a) >> 1;
    }

    public static long log4ceil(long a) {
        if (a <= 1) return 0;
        return 65 - Long.numberOfLeadingZeros(a - 1) >> 1;
    }
}
