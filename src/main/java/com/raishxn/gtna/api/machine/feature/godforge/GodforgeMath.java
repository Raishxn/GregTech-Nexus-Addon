package com.raishxn.gtna.api.machine.feature.godforge;

import com.raishxn.gtna.api.machine.feature.godforge.GodforgeModuleStats.Type;

import java.math.BigInteger;
import java.util.List;
import java.util.Random;

import static com.raishxn.gtna.api.machine.feature.godforge.GodforgeUpgrade.*;

/**
 * Port of GTNH {@code GodforgeMath} (GT5-Unofficial a3e1e112). Every formula keeps the original operation order so
 * module stats match the 1.7.10 values.
 */
public final class GodforgeMath {

    private GodforgeMath() {}

    /** Same as {@code GTUtility.powInt}: exponentiation by squaring, exact for bases 2 and 4. */
    public static double powInt(double base, int exp) {
        if (exp > 0) return powBySquaring(base, exp);
        if (exp < 0) return 1.0 / powBySquaring(base, -exp);
        return 1.0;
    }

    private static double powBySquaring(double base, int exp) {
        if (base == 2) return exp > 1023 ? Double.POSITIVE_INFINITY : Double.longBitsToDouble(exp + 1023L << 52);
        if (base == 4) return exp > 511 ? Double.POSITIVE_INFINITY : Double.longBitsToDouble(exp * 2L + 1023L << 52);
        double result = 1.0;
        while (exp > 0) {
            if ((exp & 1) == 1) result *= base;
            base *= base;
            exp >>= 1;
        }
        return result;
    }

    public static int randomIntInRange(Random random, int min, int max) {
        return (int) (random.nextDouble() * (max - min)) + min;
    }

    /** Fuel units drained per second (the controller drains ×5 every five seconds, ×2 while charging). */
    public static double fuelConsumption(GodforgeData data) {
        double upgradeFactor = data.isUpgradeActive(STEM) ? 0.8 : 1;
        int fuelFactor = data.fuelConsumptionFactor;
        if (data.selectedFuelType == 0) return fuelFactor * 300 * powInt(1.15, fuelFactor) * upgradeFactor;
        if (data.selectedFuelType == 1) return fuelFactor * 2 * powInt(1.08, fuelFactor) * upgradeFactor;
        return fuelFactor / 25f * upgradeFactor;
    }

    /** Stellar Fuel items needed to light the star. */
    public static int startupFuelConsumption(GodforgeData data) {
        int fuelFactor = data.fuelConsumptionFactor;
        double value = Math.max(fuelFactor * 25 * powInt(1.2, fuelFactor), 1);
        return (int) Math.min(value, Integer.MAX_VALUE);
    }

    public static int maxFuelFactor(GodforgeData data) {
        int fuelCap = 5;
        int fuelType = data.selectedFuelType;
        if (data.isUpgradeActive(TSE)) {
            if (fuelType == 0) {
                fuelCap = data.isUpgradeActive(STEM) ? GodforgeData.MAX_RESIDUE_FACTOR_DISCOUNTED :
                        GodforgeData.MAX_RESIDUE_FACTOR;
            } else if (fuelType == 1) {
                fuelCap = data.isUpgradeActive(STEM) ? GodforgeData.MAX_STELLAR_PLASMA_FACTOR_DISCOUNTED :
                        GodforgeData.MAX_STELLAR_PLASMA_FACTOR;
            } else {
                fuelCap = Integer.MAX_VALUE;
            }
        } else {
            if (data.isUpgradeActive(GEM)) fuelCap += data.upgrades.totalActiveUpgrades();
            if (data.isUpgradeActive(CFCE)) fuelCap *= 1.2;
        }
        return Math.max(fuelCap, 1);
    }

    /** Clamps the configured fuel factor to the cap the fuel type allows, as {@code drainFuel} does. */
    public static void clampFuelFactor(GodforgeData data) {
        boolean stem = data.isUpgradeActive(STEM);
        if (data.selectedFuelType == 0) {
            int max = stem ? GodforgeData.MAX_RESIDUE_FACTOR_DISCOUNTED : GodforgeData.MAX_RESIDUE_FACTOR;
            if (data.fuelConsumptionFactor > max) data.fuelConsumptionFactor = max;
        } else if (data.selectedFuelType == 1) {
            int max = stem ? GodforgeData.MAX_STELLAR_PLASMA_FACTOR_DISCOUNTED :
                    GodforgeData.MAX_STELLAR_PLASMA_FACTOR;
            if (data.fuelConsumptionFactor > max) data.fuelConsumptionFactor = max;
        }
    }

    public static int effectiveFuelFactor(int fuelFactor) {
        if (fuelFactor <= 43) return fuelFactor;
        return 43 + (int) Math.floor(Math.pow(fuelFactor - 43, 0.4));
    }

    public static void maxHeat(GodforgeModuleStats module, GodforgeData data) {
        maxHeat(module, data, data.fuelConsumptionFactor);
    }

    public static void maxHeat(GodforgeModuleStats module, GodforgeData data, int fuelFactor) {
        double logBase = 1.5;
        int baseHeat = 12601;
        if (data.isUpgradeActive(SEFCP)) logBase = module.type == Type.SMELTING ? 1.12 : 1.18;
        int recipeHeat = baseHeat + (int) (Math.log(fuelFactor) / Math.log(logBase) * 1000);
        module.heatForOC = overclockHeat(module, data, recipeHeat);
        module.heat = recipeHeat;
    }

    public static int overclockHeat(GodforgeModuleStats module, GodforgeData data, int recipeHeat) {
        if (data.isUpgradeActive(NDPE)) {
            double exponent = module.type == Type.SMELTING ? 0.85 : 0.8;
            return recipeHeat > 30000 ? (int) Math.floor(30000 + Math.pow(recipeHeat - 30000, exponent)) : recipeHeat;
        }
        if (data.isUpgradeActive(CNTI)) return Math.min(recipeHeat, 30000);
        return Math.min(recipeHeat, 15000);
    }

    public static void speedBonus(GodforgeModuleStats module, GodforgeData data) {
        double speedBonus = 1;
        if (data.isUpgradeActive(IGCC)) speedBonus = Math.pow(module.heat, -0.01);
        if (data.isUpgradeActive(DOR)) {
            speedBonus /= Math.pow(module.calculatedMaxParallel, module.type == Type.PLASMA ? 0.02 : 0.012);
        }
        if (module.type == Type.EXOTIC) speedBonus = data.isUpgradeActive(PA) ? Math.sqrt(speedBonus) : 1;
        module.speedBonus = speedBonus;
    }

    public static void maxParallel(GodforgeModuleStats module, GodforgeData data) {
        maxParallel(module, data, data.fuelConsumptionFactor);
    }

    public static void maxParallel(GodforgeModuleStats module, GodforgeData data, int fuelFactor) {
        int baseParallel = switch (module.type) {
            case SMELTING -> 1024;
            case MOLTEN, PLASMA -> 512;
            case EXOTIC -> 64;
        };
        float fuelFactorMultiplier = 1;
        float heatMultiplier = 1;
        float upgradeAmountMultiplier = 1;
        int node53 = data.isUpgradeActive(CTCDD) ? 2 : 1;
        boolean moltenOrBoostedSmelting = module.type == Type.MOLTEN ||
                module.type == Type.SMELTING && data.isUpgradeActive(DOP);
        if (data.isUpgradeActive(SA)) {
            fuelFactorMultiplier = 1 + effectiveFuelFactor(fuelFactor) / 15f;
            if (data.isUpgradeActive(TCT)) fuelFactorMultiplier *= moltenOrBoostedSmelting ? 3 : 2;
        }
        if (data.isUpgradeActive(EPEC)) {
            heatMultiplier = moltenOrBoostedSmelting ? 1 + module.heat / 15000f : 1 + module.heat / 25000f;
        }
        if (data.isUpgradeActive(POS)) {
            int upgrades = data.upgrades.totalActiveUpgrades();
            upgradeAmountMultiplier = moltenOrBoostedSmelting ? 1 + upgrades / 5f : 1 + upgrades / 8f;
        }
        float totalBonuses = node53 * fuelFactorMultiplier * heatMultiplier * upgradeAmountMultiplier;
        if (module.type == Type.EXOTIC) totalBonuses = data.isUpgradeActive(PA) ? (float) Math.sqrt(totalBonuses) : 1;
        module.calculatedMaxParallel = (int) (baseParallel * totalBonuses);
    }

    public static void energyDiscount(GodforgeModuleStats module, GodforgeData data) {
        double fillRatioDiscount = 1;
        double maxBatteryDiscount = 1;
        if (data.isUpgradeActive(REC)) {
            maxBatteryDiscount = 1 - (1 - Math.pow(1.05, -0.05 * data.maxBatteryCharge)) / 20;
        }
        if (data.isUpgradeActive(IMKG)) {
            double fill = (double) data.internalBattery / data.maxBatteryCharge - 0.5;
            double base = fill * fill * (-0.6) + 0.15;
            fillRatioDiscount = module.type == Type.PLASMA ? 1 - base : 1 - base * 2 / 3;
        }
        if (module.type == Type.EXOTIC) {
            if (data.isUpgradeActive(PA)) {
                fillRatioDiscount = Math.sqrt(fillRatioDiscount);
                maxBatteryDiscount = Math.sqrt(maxBatteryDiscount);
            } else {
                fillRatioDiscount = 1;
                maxBatteryDiscount = 1;
            }
        }
        module.energyDiscount = (float) (fillRatioDiscount * maxBatteryDiscount);
    }

    public static void processingVoltage(GodforgeModuleStats module, GodforgeData data) {
        processingVoltage(module, data, data.fuelConsumptionFactor);
    }

    public static void processingVoltage(GodforgeModuleStats module, GodforgeData data, int fuelFactor) {
        long voltage = 2_000_000_000L;
        if (data.isUpgradeActive(GISS)) voltage += effectiveFuelFactor(fuelFactor) * 100_000_000L;
        if (data.isUpgradeActive(NGMS)) voltage *= (long) powInt(4, data.ringAmount);
        module.processingVoltage = voltage;
    }

    public static void miscParameters(GodforgeModuleStats module, GodforgeData data) {
        int plasmaTier = data.isUpgradeActive(EE) ? 2 : data.isUpgradeActive(SEDS) ? 1 : 0;
        double overclockTimeFactor = 2;
        if (data.isUpgradeActive(GGEBE)) {
            overclockTimeFactor = module.type == Type.PLASMA ? 4 - (double) 25000 / module.heat : 2.15;
            if (module.type == Type.EXOTIC) {
                overclockTimeFactor = data.isUpgradeActive(PA) ?
                        2 + (overclockTimeFactor - 2) * (overclockTimeFactor - 2) : 2;
            }
        }
        module.upgrade83 = data.isUpgradeActive(IMKG);
        module.multiStepPlasma = data.isUpgradeActive(TPTP);
        module.plasmaTier = plasmaTier;
        module.magmatterCapable = data.isUpgradeActive(EE);
        module.voltageConfig = data.isUpgradeActive(TBF);
        module.maxThreadHatchTier = data.isUpgradeActive(END) ? com.gregtechceu.gtceu.api.GTValues.MAX :
                data.isUpgradeActive(CD) ? com.gregtechceu.gtceu.api.GTValues.OpV :
                        data.isUpgradeActive(GPCI) ? com.gregtechceu.gtceu.api.GTValues.UIV :
                                com.gregtechceu.gtceu.api.GTValues.UHV;
        module.overclockTimeFactor = overclockTimeFactor;
    }

    public static boolean allowConnection(GodforgeModuleStats module, GodforgeData data) {
        return switch (module.type) {
            case SMELTING -> true;
            case MOLTEN -> data.isUpgradeActive(FDIM);
            case PLASMA -> data.isUpgradeActive(GPCI);
            case EXOTIC -> module.magmatterMode ? data.isUpgradeActive(EE) : data.isUpgradeActive(QGPIU);
        };
    }

    /** A smelting or molten recipe that needs more heat than the module now has must stop. */
    public static boolean factorChangeDuringRecipe(GodforgeModuleStats module) {
        return (module.type == Type.SMELTING || module.type == Type.MOLTEN) && module.currentRecipeHeat > module.heat;
    }

    public static void queryMilestoneStats(GodforgeModuleStats module, GodforgeData data) {
        data.totalPowerConsumed = data.totalPowerConsumed.add(module.powerTally);
        module.powerTally = BigInteger.ZERO;
        data.totalRecipesProcessed += module.recipeTally;
        module.recipeTally = 0;
        module.inversion = data.inversion;
    }

    /** All module calculations, in the order the controller performs them every five seconds. */
    public static void updateModule(GodforgeModuleStats module, GodforgeData data) {
        maxHeat(module, data);
        maxParallel(module, data);
        speedBonus(module, data);
        energyDiscount(module, data);
        miscParameters(module, data);
        queryMilestoneStats(module, data);
        if (!data.isUpgradeActive(TBF)) processingVoltage(module, data);
    }

    public static void chargeMilestone(GodforgeData data) {
        if (!data.inversion) {
            float charge = (float) Math.max(Math.log(data.totalPowerConsumed
                    .divide(BigInteger.valueOf(GodforgeData.POWER_MILESTONE_CONSTANT)).longValue()) /
                    GodforgeData.POWER_LOG_CONSTANT + 1, 0) / 7;
            data.powerMilestonePercentage = charge;
            data.setMilestoneProgress(0, (int) Math.floor(data.powerMilestonePercentage * 7));
            return;
        }
        float rawProgress = (data.totalPowerConsumed.divide(GodforgeData.POWER_MILESTONE_T7_CONSTANT).floatValue() -
                1) / 7;
        int closestRelevantSeven = (int) Math.floor(rawProgress);
        float actualProgress = rawProgress - closestRelevantSeven;
        data.setMilestoneProgress(0, 7 + (int) Math.floor(rawProgress * 7));
        if (closestRelevantSeven % 2 == 0) {
            data.invertedPowerMilestonePercentage = actualProgress;
            data.powerMilestonePercentage = 1 - actualProgress;
        } else {
            data.powerMilestonePercentage = actualProgress;
            data.invertedPowerMilestonePercentage = 1 - actualProgress;
        }
    }

    public static void conversionMilestone(GodforgeData data) {
        if (!data.inversion) {
            double raw = Math.log(data.totalRecipesProcessed * 1f / GodforgeData.RECIPE_MILESTONE_CONSTANT) /
                    GodforgeData.RECIPE_LOG_CONSTANT + 1;
            data.recipeMilestonePercentage = (float) Math.max(raw, 0) / 7;
            data.setMilestoneProgress(1, (int) Math.floor(data.recipeMilestonePercentage * 7));
            return;
        }
        float rawProgress = ((float) data.totalRecipesProcessed / GodforgeData.RECIPE_MILESTONE_T7_CONSTANT - 1) / 7;
        int closestRelevantSeven = (int) Math.floor(rawProgress);
        float actualProgress = rawProgress - closestRelevantSeven;
        data.setMilestoneProgress(1, 7 + (int) Math.floor(rawProgress * 7));
        if (closestRelevantSeven % 2 == 0) {
            data.invertedRecipeMilestonePercentage = actualProgress;
            data.recipeMilestonePercentage = 1 - actualProgress;
        } else {
            data.recipeMilestonePercentage = actualProgress;
            data.invertedRecipeMilestonePercentage = 1 - actualProgress;
        }
    }

    public static void catalystMilestone(GodforgeData data) {
        if (!data.inversion) {
            double raw = Math.log(data.totalFuelConsumed * 1f / GodforgeData.FUEL_MILESTONE_CONSTANT) /
                    GodforgeData.FUEL_LOG_CONSTANT + 1;
            data.fuelMilestonePercentage = (float) Math.max(raw, 0) / 7;
            data.setMilestoneProgress(2, (int) Math.floor(data.fuelMilestonePercentage * 7));
            return;
        }
        float rawProgress = ((float) data.totalFuelConsumed / GodforgeData.FUEL_MILESTONE_T7_CONSTANT - 1) / 7;
        int closestRelevantSeven = (int) Math.floor(rawProgress);
        float actualProgress = rawProgress - closestRelevantSeven;
        data.setMilestoneProgress(2, 7 + (int) Math.floor(rawProgress * 7));
        if (closestRelevantSeven % 2 == 0) {
            data.invertedFuelMilestonePercentage = actualProgress;
            data.fuelMilestonePercentage = 1 - actualProgress;
        } else {
            data.fuelMilestonePercentage = actualProgress;
            data.invertedFuelMilestonePercentage = 1 - actualProgress;
        }
    }

    public static void compositionMilestone(GodforgeData data) {
        if (!data.inversion) {
            data.structureMilestonePercentage = data.totalExtensionsBuilt / 7.0f;
            return;
        }
        float rawProgress = (data.totalExtensionsBuilt - 7) / 7f;
        int closestRelevantSeven = (int) Math.floor(rawProgress);
        float actualProgress = rawProgress - closestRelevantSeven;
        if (closestRelevantSeven % 2 == 0) {
            data.invertedStructureMilestonePercentage = actualProgress;
            data.structureMilestonePercentage = 1 - actualProgress;
        } else {
            data.structureMilestonePercentage = actualProgress;
            data.invertedStructureMilestonePercentage = 1 - actualProgress;
        }
    }

    /** Composition milestone input: distinct module kinds plus extra rings, with the inversion bonus. */
    public static void compositionLevel(GodforgeData data, List<GodforgeModuleStats> modules) {
        int[] unique = new int[5];
        int smelting = 0, molten = 0, plasma = 0, exotic = 0, exoticMagmatter = 0;
        for (GodforgeModuleStats module : modules) {
            switch (module.type) {
                case SMELTING -> {
                    unique[0] = 1;
                    smelting++;
                }
                case MOLTEN -> {
                    unique[1] = 1;
                    molten++;
                }
                case PLASMA -> {
                    unique[2] = 1;
                    plasma++;
                }
                case EXOTIC -> {
                    if (!module.magmatterMode) {
                        unique[3] = 1;
                        exotic++;
                    } else {
                        unique[4] = 1;
                        exoticMagmatter++;
                    }
                }
            }
        }
        int sum = 0;
        for (int value : unique) sum += value;
        data.totalExtensionsBuilt = sum + data.ringAmount - 1;
        if (data.inversion) {
            float toAdd = (smelting - 1 + (molten - 1) * 2 + (plasma - 1) * 3 + (exotic - 1) * 4 +
                    (exoticMagmatter - 1) * 5) / 5f;
            data.totalExtensionsBuilt += toAdd;
        }
        data.setMilestoneProgress(3, (int) Math.floor(data.totalExtensionsBuilt));
    }

    public static void milestones(GodforgeData data) {
        chargeMilestone(data);
        conversionMilestone(data);
        catalystMilestone(data);
        compositionMilestone(data);
    }

    /** Inversion starts once all four milestones reach level 7. */
    public static void inversionStatus(GodforgeData data) {
        int reached = 0;
        for (int progress : data.milestoneProgress) {
            if (progress < 7) break;
            reached++;
        }
        data.inversion = reached == 4;
    }

    /** Shards earned: n(n+1)/2 per milestone level (capped at 7 before inversion), minus those spent. */
    public static void gravitonShardAmount(GodforgeData data) {
        int sum = 0;
        for (int progress : data.milestoneProgress) {
            if (!data.inversion) progress = Math.min(progress, 7);
            sum += progress * (progress + 1) / 2;
        }
        data.gravitonShardsAvailable = sum - data.gravitonShardsSpent;
    }

    /** Maximum number of modules: 8, +4 with CD, +4 with END. */
    public static int maxModuleCount(GodforgeData data) {
        int max = 8;
        if (data.isUpgradeActive(CD)) max += 4;
        if (data.isUpgradeActive(END)) max += 4;
        return max;
    }
}
