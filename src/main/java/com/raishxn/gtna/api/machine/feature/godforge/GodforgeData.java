package com.raishxn.gtna.api.machine.feature.godforge;

import java.math.BigInteger;

/** Persistent state of a Forge of Gods, ported from GTNH {@code ForgeOfGodsData} without Minecraft types. */
public final class GodforgeData {

    public static final int DEFAULT_FUEL_CONSUMPTION_FACTOR = 1;
    public static final long DEFAULT_MAX_BATTERY_CHARGE = 100;
    public static final int DEFAULT_RING_AMOUNT = 1;
    public static final int DEFAULT_ROTATION_SPEED = 5;
    public static final int DEFAULT_STAR_SIZE = 20;

    public static final long POWER_MILESTONE_CONSTANT = 1_000_000_000_000_000L;
    public static final long RECIPE_MILESTONE_CONSTANT = 10_000_000L;
    public static final long FUEL_MILESTONE_CONSTANT = 10_000L;
    public static final long RECIPE_MILESTONE_T7_CONSTANT = RECIPE_MILESTONE_CONSTANT * 4096L;
    public static final long FUEL_MILESTONE_T7_CONSTANT = FUEL_MILESTONE_CONSTANT * 729L;
    public static final BigInteger POWER_MILESTONE_T7_CONSTANT = BigInteger.valueOf(POWER_MILESTONE_CONSTANT)
            .multiply(BigInteger.valueOf(9).pow(6));
    public static final double POWER_LOG_CONSTANT = Math.log(9);
    public static final double RECIPE_LOG_CONSTANT = Math.log(4);
    public static final double FUEL_LOG_CONSTANT = Math.log(3);

    public static final int MAX_RESIDUE_FACTOR = 70;
    public static final int MAX_RESIDUE_FACTOR_DISCOUNTED = 72;
    public static final int MAX_STELLAR_PLASMA_FACTOR = 181;
    public static final int MAX_STELLAR_PLASMA_FACTOR_DISCOUNTED = 184;

    /** Fuel types in GUI order: Dimensionally Transcendent Residue, Raw Star Matter, Magnetohydrodynamic CSM. */
    public enum Fuel {
        RESIDUE,
        STELLAR_PLASMA,
        MAGMATTER
    }

    public int fuelConsumptionFactor = DEFAULT_FUEL_CONSUMPTION_FACTOR;
    public int selectedFuelType;
    public long internalBattery;
    public long maxBatteryCharge = DEFAULT_MAX_BATTERY_CHARGE;
    public int gravitonShardsAvailable;
    public int gravitonShardsSpent;
    public int ringAmount = DEFAULT_RING_AMOUNT;
    public int stellarFuelAmount;
    public int neededStartupFuel;
    public long fuelConsumption;
    public long totalRecipesProcessed;
    public long totalFuelConsumed;
    public float totalExtensionsBuilt;
    public float powerMilestonePercentage, recipeMilestonePercentage, fuelMilestonePercentage,
            structureMilestonePercentage;
    public float invertedPowerMilestonePercentage, invertedRecipeMilestonePercentage,
            invertedFuelMilestonePercentage, invertedStructureMilestonePercentage;
    public final int[] milestoneProgress = new int[4];
    public BigInteger totalPowerConsumed = BigInteger.ZERO;
    public boolean batteryCharging;
    public boolean inversion;
    public boolean gravitonShardEjection;
    public boolean renderActive;
    public boolean rendererDisabled;
    public boolean secretUpgrade;
    public final GodforgeUpgradeStorage upgrades = new GodforgeUpgradeStorage();
    public final GodforgeStarColor.Storage starColors = new GodforgeStarColor.Storage();
    public String selectedStarColor = GodforgeStarColor.DEFAULT.name();
    public int rotationSpeed = DEFAULT_ROTATION_SPEED;
    public int starSize = DEFAULT_STAR_SIZE;

    public boolean isUpgradeActive(GodforgeUpgrade upgrade) {
        return upgrades.isUpgradeActive(upgrade);
    }

    /** Buys an upgrade if prerequisites, split limit (one per ring) and costs allow it. */
    public boolean unlockUpgrade(GodforgeUpgrade upgrade) {
        if (isUpgradeActive(upgrade)) return false;
        if (!upgrades.checkPrerequisites(upgrade)) return false;
        if (!upgrades.checkSplit(upgrade, ringAmount)) return false;
        if (!upgrades.checkCost(upgrade, gravitonShardsAvailable)) return false;
        upgrades.unlock(upgrade);
        gravitonShardsAvailable -= upgrade.shardCost();
        gravitonShardsSpent += upgrade.shardCost();
        return true;
    }

    /** Refunds an upgrade unless an active dependent still needs it. */
    public boolean respecUpgrade(GodforgeUpgrade upgrade) {
        if (!isUpgradeActive(upgrade)) return false;
        if (!upgrades.checkDependents(upgrade)) return false;
        upgrades.respec(upgrade);
        gravitonShardsAvailable += upgrade.shardCost();
        gravitonShardsSpent -= upgrade.shardCost();
        if (upgrade == GodforgeUpgrade.END) gravitonShardEjection = false;
        return true;
    }

    public void setMilestoneProgress(int index, int value) {
        milestoneProgress[index] = value;
    }

    /** Adds charge up to the configured maximum; reaching it stops charging. */
    public void increaseBattery(long amount) {
        long newCharge = Long.MAX_VALUE - internalBattery < amount ? Long.MAX_VALUE : internalBattery + amount;
        if (newCharge <= maxBatteryCharge) {
            internalBattery = newCharge;
        } else {
            internalBattery = maxBatteryCharge;
            batteryCharging = false;
        }
    }

    /** @return true if the battery emptied (modules disconnect and the star goes out). */
    public boolean reduceBattery(long amount) {
        if (internalBattery - amount <= 0) {
            internalBattery = 0;
            return true;
        }
        internalBattery -= amount;
        totalFuelConsumed += amount;
        return false;
    }
}
