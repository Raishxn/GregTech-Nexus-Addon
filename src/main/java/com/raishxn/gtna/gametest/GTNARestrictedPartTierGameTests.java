package com.raishxn.gtna.gametest;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.common.data.GTNAMachines2;
import com.raishxn.gtna.utils.GTNASpecialPartUtil;

@PrefixGameTestTemplate(false)
@GameTestHolder("gtna")
public final class GTNARestrictedPartTierGameTests {

    private GTNARestrictedPartTierGameTests() {}

    /** Infinite input / output boost parts only act on recipes of their own tier; ULV recipes count as LV. */
    @GameTest(template = "empty_12", timeoutTicks = 20)
    public static void restrictedPartsOnlyActOnTheirTier(GameTestHelper helper) {
        var hv = part(helper, new BlockPos(2, 2, 2), GTValues.HV);
        var lv = part(helper, new BlockPos(4, 2, 2), GTValues.LV);
        var hvRecipe = recipe("tier_lock_hv", GTValues.VA[GTValues.HV]);
        var lvRecipe = recipe("tier_lock_lv", GTValues.VA[GTValues.LV]);
        var ulvRecipe = recipe("tier_lock_ulv", 7);
        helper.assertTrue(GTNASpecialPartUtil.matchesTier(hv, hvRecipe), "HV part must act on HV recipes");
        helper.assertFalse(GTNASpecialPartUtil.matchesTier(hv, lvRecipe), "HV part must not act on LV recipes");
        helper.assertTrue(GTNASpecialPartUtil.matchesTier(lv, ulvRecipe), "ULV recipes count as LV");
        helper.assertFalse(GTNASpecialPartUtil.matchesTier(lv, hvRecipe), "LV part must not act on HV recipes");
        helper.succeed();
    }

    private static IMultiPart part(GameTestHelper helper, BlockPos pos, int tier) {
        helper.setBlock(pos, GTNAMachines2.INFINITE_INPUT_BUSES[tier].getBlock());
        return (IMultiPart) ((MetaMachineBlockEntity) helper.getBlockEntity(pos)).getMetaMachine();
    }

    private static com.gregtechceu.gtceu.api.recipe.GTRecipe recipe(String id, long eut) {
        return GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GTNACORE.id(id)).EUt(eut).duration(20)
                .buildRawRecipe();
    }
}
