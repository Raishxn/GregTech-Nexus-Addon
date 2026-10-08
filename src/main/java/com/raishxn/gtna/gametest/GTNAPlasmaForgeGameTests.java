package com.raishxn.gtna.gametest;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.raishxn.gtna.common.data.GTNAPlasmaForge;
import com.raishxn.gtna.common.data.material.GodforgeChainMaterials;
import com.raishxn.gtna.common.machine.multiblock.electric.PlasmaForgeMachine;

@PrefixGameTestTemplate(false)
@GameTestHolder("gtna")
public final class GTNAPlasmaForgeGameTests {

    private GTNAPlasmaForgeGameTests() {}

    /** The GTNH structure forms from its preview and the catalyst discount follows GTNH's 8-hour curve. */
    @GameTest(template = "empty_48", timeoutTicks = 600)
    public static void plasmaForgeFormsAndDiscountsCatalysts(GameTestHelper helper) {
        var definition = GTNAPlasmaForge.PLASMA_FORGE;
        var shape = definition.getMatchingShapes().get(0).getBlocks();
        PlasmaForgeMachine forge = null;
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
                        forge = (PlasmaForgeMachine) ((IMachineBlockEntity) helper.getBlockEntity(pos))
                                .getMetaMachine();
                    }
                }
            }
        }
        helper.assertTrue(forge != null, "the preview has a controller");
        helper.assertTrue(forge.checkPatternWithLock(),
                "the GTNH DTPF structure must form: " + forge.getMultiblockState().error);

        helper.assertTrue(PlasmaForgeMachine.discount(0) == 1.0, "no run time, no discount");
        helper.assertTrue(PlasmaForgeMachine.discount(288_000) == 0.75, "4 h halves the way");
        helper.assertTrue(PlasmaForgeMachine.discount(10_000_000) == 0.5, "capped at 50%");

        GTRecipe lens = null;
        for (var recipe : helper.getLevel().getRecipeManager().getRecipes()) {
            if (recipe instanceof GTRecipe gt && gt.id.getPath().endsWith("godforge_chain/laser_lens_special"))
                lens = gt;
        }
        helper.assertTrue(lens != null, "the DTPF lens recipe is loaded");
        forge.setRunningTime(10_000_000);
        var modified = PlasmaForgeMachine.catalystDiscount(forge, lens).apply(lens);
        int catalyst = -1;
        for (var content : modified.getInputContents(FluidRecipeCapability.CAP)) {
            var ingredient = FluidRecipeCapability.CAP.of(content.content);
            if (ingredient.getStacks()[0].getFluid() == GodforgeChainMaterials.ExcitedDTRC.getFluid())
                catalyst = ingredient.getAmount();
        }
        helper.assertTrue(catalyst == 46, "92 mB of Excited DTRC at 50% discount must be 46, got " + catalyst);
        helper.succeed();
    }
}
