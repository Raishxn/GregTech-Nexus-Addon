package com.raishxn.gtna.gametest;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.common.data.GTNANanoForge;
import com.raishxn.gtna.common.machine.multiblock.electric.NanoForgeMachine;

@PrefixGameTestTemplate(false)
@GameTestHolder("gtna")
public final class GTNANanoForgeGameTests {

    private GTNANanoForgeGameTests() {}

    /** Every GTNH nanite recipe of tiers 1-3 and the controller are registered. */
    @GameTest(template = "empty_12", timeoutTicks = 40)
    public static void naniteRecipesAreRegistered(GameTestHelper helper) {
        var wanted = new java.util.HashSet<>(java.util.List.of("nano_carbon", "nano_silver", "nano_neutronium",
                "nano_glowstone", "nano_gold", "nano_transcendent_metal", "nano_six_phased_copper",
                "nano_white_dwarf_matter", "nano_black_dwarf_matter", "nano_universium", "nano_eternity",
                "nano_forge", "carbon_nanites", "stellar_alloy", "tesseract", "transcendent_metal_dust",
                "transcendent_metal", "stable_boson_containment_unit", "superconductor_composite",
                "electromagnet_tengam",
                "neutronium_heat_capacitor", "space_coolant_cell", "thermal_superconductor",
                "relativistic_heat_capacitor",
                "force_field_glass", "cosmic_fabric_manipulator", "graviton_anomaly", "field_restriction_coil_t3",
                "really_ultimate_battery",
                "extremely_ultimate_battery", "insanely_ultimate_battery", "mega_ultimate_battery",
                "dimensionally_shifted_superfluid_t2", "dimensionally_shifted_superfluid_t5"));
        for (var recipe : helper.getLevel().getRecipeManager().getRecipes()) {
            if (recipe instanceof GTRecipe gt && GTNACORE.MOD_ID.equals(gt.id.getNamespace()))
                wanted.removeIf(id -> gt.id.getPath().endsWith("godforge_chain/" + id));
        }
        helper.assertTrue(wanted.isEmpty(), "missing Nano Forge recipes: " + wanted);
        helper.succeed();
    }

    /** The structure forms from its preview. */
    @GameTest(template = "empty_48", timeoutTicks = 400)
    public static void nanoForgeBaseFormsAtTierOne(GameTestHelper helper) {
        var definition = GTNANanoForge.NANO_FORGE;
        var shape = definition.getMatchingShapes().get(0).getBlocks();
        NanoForgeMachine machine = null;
        for (int x = 0; x < shape.length; x++) {
            for (int y = 0; y < shape[x].length; y++) {
                for (int z = 0; z < shape[x][y].length; z++) {
                    var info = shape[x][y][z];
                    var pos = new BlockPos(x + 1, y + 1, z + 1);
                    if (info == null || info.getBlockState().isAir()) {
                        helper.setBlock(pos, Blocks.AIR);
                        continue;
                    }
                    helper.setBlock(pos, info.getBlockState());
                    if (info.getBlockState().is(definition.getBlock())) {
                        machine = (NanoForgeMachine) ((IMachineBlockEntity) helper.getBlockEntity(pos))
                                .getMetaMachine();
                    }
                }
            }
        }
        helper.assertTrue(machine != null, "the preview has a controller");
        helper.assertTrue(machine.checkPatternWithLock(),
                "the structure must form: " + machine.getMultiblockState().error);
        machine.onStructureFormed();
        helper.assertTrue(machine.getStructureTier() == 1 && machine.getNanoTier() == 0,
                "base only and no nanite: structure tier 1, machine tier 0, got " + machine.getStructureTier() + "/" +
                        machine.getNanoTier());
        helper.succeed();
    }
}
