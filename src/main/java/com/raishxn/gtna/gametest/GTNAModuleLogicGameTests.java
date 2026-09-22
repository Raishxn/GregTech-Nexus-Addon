package com.raishxn.gtna.gametest;

import com.gregtechceu.gtceu.api.GTValues;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.raishxn.gtna.common.machine.multiblock.module.steamElevator.SteamBeaconModule;
import com.raishxn.gtna.common.machine.multiblock.module.steamElevator.SteamEntityCrusherModule;
import com.raishxn.gtna.common.machine.multiblock.module.steamElevator.SteamFlightModule;
import com.raishxn.gtna.common.machine.multiblock.module.steamElevator.SteamGreenhouseModule;
import com.raishxn.gtna.common.machine.multiblock.module.steamElevator.SteamOilDrillModule;
import com.raishxn.gtna.common.machine.multiblock.module.steamElevator.SteamOreProcessorModule;
import com.raishxn.gtna.common.machine.multiblock.module.steamElevator.SteamWeatherModule;

/**
 * Server-side logic tests for the Steam Elevator modules.
 *
 * <p>
 * The full 1x5x2 module is fiddly to build reliably in the gametest template, and most of the
 * GTNL-parity behaviour is pure arithmetic (mode tables, upkeep formulas, effect caps, spawner-NBT
 * parsing). Those are exposed as static helpers by the modules and locked here, so a regression in
 * the numbers fails the dedicated server gate instead of only showing up in game.
 */
@PrefixGameTestTemplate(false)
@GameTestHolder("gtna")
public final class GTNAModuleLogicGameTests {

    private static final String TEMPLATE = "empty_12";

    private GTNAModuleLogicGameTests() {}

    /** Weather: the controller circuit selects the forced weather (1/2/3), anything else is off. */
    @GameTest(template = TEMPLATE, timeoutTicks = 20)
    public static void weatherCircuitSelectsMode(GameTestHelper helper) {
        helper.assertTrue(SteamWeatherModule.modeForCircuit(1) == SteamWeatherModule.MODE_CLEAR,
                "circuit 1 must select clear");
        helper.assertTrue(SteamWeatherModule.modeForCircuit(2) == SteamWeatherModule.MODE_RAIN,
                "circuit 2 must select rain");
        helper.assertTrue(SteamWeatherModule.modeForCircuit(3) == SteamWeatherModule.MODE_THUNDER,
                "circuit 3 must select thunder");
        helper.assertTrue(SteamWeatherModule.modeForCircuit(0) == SteamWeatherModule.MODE_OFF,
                "circuit 0 must be off");
        helper.assertTrue(SteamWeatherModule.modeForCircuit(9) == SteamWeatherModule.MODE_OFF,
                "an unassigned circuit must be off");
        helper.assertTrue(SteamWeatherModule.WEATHER_TIME == 72000,
                "the forced weather must last one in-game hour, was " + SteamWeatherModule.WEATHER_TIME);
        helper.succeed();
    }

    /** Ore Processor: GTNL's mode table, parallel and steam formulas. */
    @GameTest(template = TEMPLATE, timeoutTicks = 20)
    public static void oreProcessorModeTables(GameTestHelper helper) {
        int[] expectedTicks = { 600, 300, 200, 400, 340, 640, 20 };
        for (int mode = 0; mode < expectedTicks.length; mode++) {
            helper.assertTrue(SteamOreProcessorModule.cycleTicksFor(mode) == expectedTicks[mode],
                    "mode " + mode + " must take " + expectedTicks[mode] + " ticks, got " +
                            SteamOreProcessorModule.cycleTicksFor(mode));
        }
        helper.assertTrue(SteamOreProcessorModule.maxParallelFor(0) == 8,
                "mode 0 must allow 8 parallels");
        helper.assertTrue(SteamOreProcessorModule.maxParallelFor(1) == 16,
                "mode 1 must allow 16 parallels (the tooltip's 'up to 16 ores')");
        helper.assertTrue(SteamOreProcessorModule.maxParallelFor(2) == 32,
                "mode 2 must allow 32 parallels");
        helper.assertTrue(SteamOreProcessorModule.upkeepFor(0) == 128,
                "mode 0 must cost 128 mB/t");
        helper.assertTrue(SteamOreProcessorModule.upkeepFor(1) == 256,
                "the circuit must double the steam (mode 1 = 256 mB/t)");
        helper.assertTrue(SteamOreProcessorModule.upkeepFor(2) == 512,
                "the circuit must keep doubling (mode 2 = 512 mB/t)");
        // Out-of-range modes clamp instead of throwing.
        helper.assertTrue(SteamOreProcessorModule.cycleTicksFor(99) == 20, "a high mode must clamp to 6");
        helper.assertTrue(SteamOreProcessorModule.maxParallelFor(-5) == 8, "a low mode must clamp to 0");
        helper.succeed();
    }

    /** Oil Drill: GTNL uses V * 30 / 32 (VP), not the full V. */
    @GameTest(template = TEMPLATE, timeoutTicks = 20)
    public static void oilDrillUpkeepUsesVp(GameTestHelper helper) {
        helper.assertTrue(SteamOilDrillModule.upkeepForTier(2) == 120L,
                "tier I (GTNL tier 2) must cost 120 mB/t, got " + SteamOilDrillModule.upkeepForTier(2));
        helper.assertTrue(SteamOilDrillModule.upkeepForTier(3) == 480L,
                "tier II must cost 480 mB/t, got " + SteamOilDrillModule.upkeepForTier(3));
        helper.assertTrue(SteamOilDrillModule.upkeepForTier(4) == 1920L,
                "tier III must cost 1920 mB/t, got " + SteamOilDrillModule.upkeepForTier(4));
        helper.assertTrue(SteamOilDrillModule.upkeepForTier(4) == GTValues.V[4] * 30L / 32L,
                "the upkeep must be GTValues.VP (V * 30 / 32)");
        helper.succeed();
    }

    /** Entity Crusher: the spawner NBT is parsed and the GTNL doubling chance is capped at 34%. */
    @GameTest(template = TEMPLATE, timeoutTicks = 20)
    public static void entityCrusherSpawnerAndChance(GameTestHelper helper) {
        helper.assertTrue(SteamEntityCrusherModule.spawnerEntityType(new ItemStack(Items.SPAWNER)) == null,
                "a spawner without a stored mob must not be a valid catalyst");

        ItemStack zombieSpawner = new ItemStack(Items.SPAWNER);
        CompoundTag spawnData = new CompoundTag();
        CompoundTag entity = new CompoundTag();
        entity.putString("id", "minecraft:zombie");
        spawnData.put("entity", entity);
        CompoundTag blockEntity = new CompoundTag();
        blockEntity.put("SpawnData", spawnData);
        zombieSpawner.addTagElement("BlockEntityTag", blockEntity);
        helper.assertTrue(SteamEntityCrusherModule.spawnerEntityType(zombieSpawner) == EntityType.ZOMBIE,
                "the spawner's SpawnData.entity.id must resolve to the mob type");

        helper.assertTrue(SteamEntityCrusherModule.doublingChanceFor(0) == 2.0,
                "the base doubling chance must be 2%");
        helper.assertTrue(SteamEntityCrusherModule.doublingChanceFor(4) == 4.0,
                "each identical spawner adds 0.5%");
        helper.assertTrue(SteamEntityCrusherModule.doublingChanceFor(1_000) == 34.0,
                "the doubling chance must cap at 34%");
        helper.succeed();
    }

    /** Beacon: toggling an effect enforces the tier+2 cap and never mutates a full selection. */
    @GameTest(template = TEMPLATE, timeoutTicks = 20)
    public static void beaconEffectToggleCap(GameTestHelper helper) {
        int mask = SteamBeaconModule.toggleEffect(0, 0, true, 2);
        helper.assertTrue(mask == 0b1, "toggling effect 0 on must set bit 0, got " + mask);
        mask = SteamBeaconModule.toggleEffect(mask, 0, true, 2);
        helper.assertTrue(mask == 0b1, "toggling an already-on effect must not change the mask");
        mask = SteamBeaconModule.toggleEffect(mask, 1, true, 2);
        helper.assertTrue(mask == 0b11, "the second effect must fit under the cap of 2");
        mask = SteamBeaconModule.toggleEffect(mask, 2, true, 2);
        helper.assertTrue(mask == 0b11, "a third effect must be rejected by the cap of 2, got " + mask);
        mask = SteamBeaconModule.toggleEffect(mask, 0, false, 2);
        helper.assertTrue(mask == 0b10, "turning an effect off must clear its bit, got " + mask);
        helper.succeed();
    }

    /** Locks the two documented GTNL constants that the tooltips promise. */
    @GameTest(template = TEMPLATE, timeoutTicks = 20)
    public static void moduleChainConstants(GameTestHelper helper) {
        helper.assertTrue(SteamGreenhouseModule.WATER_PER_OPERATION == 16_000,
                "the greenhouse must use its documented 16,000 mB of water per crop");
        helper.assertTrue(SteamFlightModule.RANGE == 64, "the flight module's base range must be 64");
        helper.succeed();
    }
}
