package com.raishxn.gtna;

import com.raishxn.gtna.api.machine.feature.godforge.GodforgeData;
import com.raishxn.gtna.api.machine.feature.godforge.GodforgeMath;
import com.raishxn.gtna.api.machine.feature.godforge.GodforgeModuleStats;
import com.raishxn.gtna.api.machine.feature.godforge.GodforgeModuleStats.Type;
import com.raishxn.gtna.api.machine.feature.godforge.GodforgeUpgrade;

import java.math.BigInteger;
import java.util.List;

import static com.raishxn.gtna.api.machine.feature.godforge.GodforgeUpgrade.*;

/** Forge of Gods rules against values derived from the GTNH formulas (GT5-Unofficial a3e1e112). */
public final class GodforgeMathTest {

    private GodforgeMathTest() {}

    public static void main(String[] args) {
        fuel();
        modules();
        upgradeTree();
        milestones();
        overclock();
    }

    private static void overclock() {
        var lvToMv = new com.raishxn.gtna.api.machine.feature.godforge.GodforgeOverclock();
        lvToMv.recipeEUt = 32;
        lvToMv.duration = 100;
        lvToMv.machineVoltage = 128;
        lvToMv.calculate();
        check(lvToMv.consumption() == 128 && lvToMv.calculatedDuration() == 50, "one regular overclock");
        var heat = new com.raishxn.gtna.api.machine.feature.godforge.GodforgeOverclock();
        heat.recipeEUt = 120;
        heat.duration = 1000;
        heat.machineVoltage = 480;
        heat.recipeHeat = 1800;
        heat.machineHeat = 3600;
        heat.heatOC = true;
        heat.heatDiscount = true;
        heat.calculate();
        check(heat.consumption() == 434 && heat.calculatedDuration() == 250,
                "heat discount 0.95^2 and one perfect heat overclock: " + heat.consumption() + "/" +
                        heat.calculatedDuration());
    }

    private static void fuel() {
        GodforgeData data = new GodforgeData();
        close(345, GodforgeMath.fuelConsumption(data), "residue f=1");
        close(30, GodforgeMath.startupFuelConsumption(data), "startup fuel f=1");
        data.fuelConsumptionFactor = 10;
        close(10 * 300 * Math.pow(1.15, 10), GodforgeMath.fuelConsumption(data), "residue f=10");
        data.selectedFuelType = 1;
        close(10 * 2 * Math.pow(1.08, 10), GodforgeMath.fuelConsumption(data), "stellar plasma f=10");
        data.selectedFuelType = 2;
        close(0.4, GodforgeMath.fuelConsumption(data), "magmatter f=10");
        data.selectedFuelType = 0;
        check(GodforgeMath.maxFuelFactor(data) == 5, "base fuel cap 5");
        unlock(data, START, IGCC, CFCE);
        check(GodforgeMath.maxFuelFactor(data) == 6, "CFCE multiplies the cap by 1.2");
        check(GodforgeMath.effectiveFuelFactor(43) == 43 && GodforgeMath.effectiveFuelFactor(100) == 48,
                "effective fuel factor softens after 43");
        check(GodforgeMath.powInt(4, 3) == 64 && GodforgeMath.powInt(2, -1) == 0.5, "powInt");
    }

    private static void modules() {
        GodforgeData data = new GodforgeData();
        GodforgeModuleStats smelting = new GodforgeModuleStats(Type.SMELTING);
        data.fuelConsumptionFactor = 10;
        GodforgeMath.updateModule(smelting, data);
        check(smelting.heat == 12601 + (int) (Math.log(10) / Math.log(1.5) * 1000), "heat formula");
        check(smelting.heatForOC == 15000, "OC heat capped at 15000 without CNTI");
        check(smelting.calculatedMaxParallel == 1024, "smelting base parallel");
        check(smelting.processingVoltage == 2_000_000_000L, "base processing voltage");
        close(1, smelting.speedBonus, "no speed bonus without IGCC");

        unlock(data, START, IGCC, CFCE, SA);
        GodforgeMath.updateModule(smelting, data);
        check(smelting.calculatedMaxParallel == (int) (1024 * (1 + 10 / 15f)), "SA fuel factor multiplier");
        close(Math.pow(smelting.heat, -0.01), smelting.speedBonus, "IGCC speed bonus");

        GodforgeModuleStats exotic = new GodforgeModuleStats(Type.EXOTIC);
        GodforgeMath.updateModule(exotic, data);
        check(exotic.calculatedMaxParallel == 64 && exotic.speedBonus == 1, "exotic ignores bonuses without PA");

        GodforgeModuleStats plasma = new GodforgeModuleStats(Type.PLASMA);
        check(!GodforgeMath.allowConnection(plasma, data), "plasma needs GPCI");
        check(GodforgeMath.allowConnection(smelting, data), "smelting always connects");
        check(GodforgeMath.maxModuleCount(data) == 8, "eight modules on one ring");
    }

    private static void upgradeTree() {
        GodforgeData data = new GodforgeData();
        data.gravitonShardsAvailable = 1000;
        check(!data.unlockUpgrade(IGCC), "IGCC needs START");
        check(data.unlockUpgrade(START), "START is free when it has no item cost");
        check(data.unlockUpgrade(IGCC) && data.gravitonShardsSpent == 1, "shards are spent");
        unlock(data, STEM, GISS, FDIM, GPCI);
        check(data.unlockUpgrade(REC), "REC with GISS and GPCI");
        check(!data.unlockUpgrade(CTCDD), "CTCDD requires both GPCI and SA");
        unlock(data, CFCE, SA, CTCDD, QGPIU);
        check(data.unlockUpgrade(SEFCP), "first split upgrade on one ring");
        check(!data.unlockUpgrade(TCT), "only one split upgrade per ring");
        data.ringAmount = 2;
        check(data.unlockUpgrade(TCT), "second ring allows a second split");
        check(!data.respecUpgrade(QGPIU), "cannot respec while dependents need it");
        check(data.respecUpgrade(TCT), "leaf upgrade can be refunded");
        GodforgeUpgrade.START.addExtraCost(new GodforgeUpgrade.ExtraCost("minecraft:cobblestone", 4));
        GodforgeData paid = new GodforgeData();
        check(!paid.unlockUpgrade(START), "extra item cost must be paid first");
        GodforgeUpgrade.START.clearExtraCost();
        check(GodforgeUpgrade.END.shardCost() == 12 && GodforgeUpgrade.VALUES.length == 31, "tree size");
    }

    private static void milestones() {
        GodforgeData data = new GodforgeData();
        data.totalPowerConsumed = BigInteger.valueOf(GodforgeData.POWER_MILESTONE_CONSTANT).multiply(
                BigInteger.valueOf(81));
        data.totalRecipesProcessed = GodforgeData.RECIPE_MILESTONE_CONSTANT * 16;
        data.totalFuelConsumed = GodforgeData.FUEL_MILESTONE_CONSTANT * 9;
        data.ringAmount = 3;
        GodforgeMath.compositionLevel(data, List.of(new GodforgeModuleStats(Type.SMELTING),
                new GodforgeModuleStats(Type.PLASMA)));
        GodforgeMath.milestones(data);
        check(data.milestoneProgress[0] == 3, "charge milestone 9^2 -> level 3: " + data.milestoneProgress[0]);
        check(data.milestoneProgress[1] == 3, "conversion milestone 4^2 -> level 3: " + data.milestoneProgress[1]);
        check(data.milestoneProgress[2] == 3, "catalyst milestone 3^2 -> level 3: " + data.milestoneProgress[2]);
        check(data.milestoneProgress[3] == 4, "composition: two module kinds + two extra rings");
        GodforgeMath.gravitonShardAmount(data);
        check(data.gravitonShardsAvailable == 6 + 6 + 6 + 10, "shards n(n+1)/2 per milestone");
        for (int i = 0; i < 4; i++) data.milestoneProgress[i] = 7;
        GodforgeMath.inversionStatus(data);
        check(data.inversion, "inversion once every milestone reaches 7");
    }

    private static void unlock(GodforgeData data, GodforgeUpgrade... upgrades) {
        data.gravitonShardsAvailable += 1000;
        for (GodforgeUpgrade upgrade : upgrades) {
            if (!data.isUpgradeActive(upgrade)) check(data.unlockUpgrade(upgrade), "unlock " + upgrade);
        }
    }

    private static void close(double expected, double actual, String message) {
        if (Math.abs(expected - actual) > 1e-6 * Math.max(1, Math.abs(expected))) {
            throw new AssertionError(message + ": expected " + expected + ", got " + actual);
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
