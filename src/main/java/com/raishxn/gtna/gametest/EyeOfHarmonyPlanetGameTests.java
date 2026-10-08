package com.raishxn.gtna.gametest;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.api.machine.feature.eyeofharmony.EyeOfHarmonyMath;
import com.raishxn.gtna.common.data.GTNAEyeOfHarmonyContent;
import com.raishxn.gtna.common.data.multiblock.EyeOfHarmonyCatalog;
import com.raishxn.gtna.common.data.multiblock.EyeOfHarmonyPrograms;

@PrefixGameTestTemplate(false)
@GameTestHolder("gtna")
public final class EyeOfHarmonyPlanetGameTests {

    private EyeOfHarmonyPlanetGameTests() {}

    /** Every built-in program builds from its dimension's veins with the GTNH tier parameters. */
    @GameTest(template = "empty_12", timeoutTicks = 100)
    public static void planetaryProgramsFollowGtnhTiers(GameTestHelper helper) {
        var recipes = helper.getLevel().getRecipeManager();
        for (var definition : EyeOfHarmonyPrograms.all()) {
            var catalog = EyeOfHarmonyCatalog.build(recipes, definition);
            int tier = definition.rocketTier();
            var program = catalog.program();
            helper.assertTrue(program.baseTicks() == EyeOfHarmonyMath.Program.miningSeconds(tier) * 20,
                    definition.planet() + " duration");
            helper.assertTrue(program.hydrogen() == 1_000_000_000L * (tier + 1), definition.planet() + " gas");
            helper.assertTrue(program.requiredCompression() == Math.max(tier, 1) - 1,
                    definition.planet() + " compression");
            helper.assertTrue(Math.abs(program.baseChance() - (1 - 0.05 * tier)) < 1e-12,
                    definition.planet() + " chance");
            helper.assertTrue(!catalog.definition().planetStack().isEmpty(), definition.planet() + " planet item");
        }
        helper.assertTrue(EyeOfHarmonyMath.Program.miningSeconds(5) == 96_808L, "18000 * 1.4^5 truncated");
        var moon = program("moon");
        helper.assertTrue(has(EyeOfHarmonyCatalog.build(recipes, moon), GTMaterials.Ilmenite),
                "the Moon catalog must contain Ilmenite dust");
        helper.assertTrue(has(EyeOfHarmonyCatalog.build(recipes, program("mars")), GTMaterials.Tungstate),
                "the Mars catalog must contain Tungstate dust");
        helper.assertTrue(EyeOfHarmonyPrograms.forStack(GTNAEyeOfHarmonyContent.GLACIO_PLANET.asStack()) != null,
                "the Glacio Planet Block must select a program");
        helper.succeed();
    }

    /** The KubeJS path: any item can select any dimension, and removal clears it. */
    @GameTest(template = "empty_12", timeoutTicks = 40)
    public static void customProgramsCanBeRegisteredAndRemoved(GameTestHelper helper) {
        var diamond = new ResourceLocation("minecraft", "diamond");
        EyeOfHarmonyPrograms.register(diamond, new ResourceLocation("minecraft", "overworld"), 3, null);
        try {
            var definition = EyeOfHarmonyPrograms.forStack(new ItemStack(net.minecraft.world.item.Items.DIAMOND));
            helper.assertTrue(definition != null && definition.rocketTier() == 3, "custom program registered");
            var catalog = EyeOfHarmonyCatalog.build(helper.getLevel().getRecipeManager(), definition);
            helper.assertTrue(catalog.program().requiredCompression() == 2, "tier 3 needs compression 2");
        } finally {
            EyeOfHarmonyPrograms.remove(diamond);
        }
        helper.assertTrue(EyeOfHarmonyPrograms.forStack(new ItemStack(net.minecraft.world.item.Items.DIAMOND)) == null,
                "custom program removed");
        helper.succeed();
    }

    private static EyeOfHarmonyPrograms.Definition program(String planet) {
        return EyeOfHarmonyPrograms.get(GTNACORE.id("eye_of_harmony_planet_" + planet));
    }

    private static boolean has(EyeOfHarmonyCatalog.Catalog catalog,
                               com.gregtechceu.gtceu.api.data.chemical.material.Material material) {
        var dust = ChemicalHelper.get(TagPrefix.dust, material);
        var items = catalog.products().getList("items", 10);
        for (int i = 0; i < items.size(); i++) {
            CompoundTag tag = items.getCompound(i);
            if (ItemStack.isSameItem(ItemStack.of(tag), dust) && tag.getLong("remaining") > 0) return true;
        }
        return false;
    }
}
