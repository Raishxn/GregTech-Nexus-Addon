package com.raishxn.gtna.integration.jade.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.common.data.GTNARecipeType;
import com.raishxn.gtna.common.machine.multiblock.electric.ElectricVoidMinerMachine;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

/** The random pool is a set of independent chances, never a list of guaranteed products. */
public final class GTNAVoidMinerProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    public static final GTNAVoidMinerProvider INSTANCE = new GTNAVoidMinerProvider();

    @Override
    public ResourceLocation getUid() {
        return GTNACORE.id("void_miner_random");
    }

    @Override
    public int getDefaultPriority() {
        return 1000;
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (!(accessor.getBlockEntity() instanceof MetaMachineBlockEntity entity) ||
                !(entity.getMetaMachine() instanceof ElectricVoidMinerMachine miner))
            return;
        var recipe = miner.getRecipeLogic().getLastRecipe();
        if (!miner.getRecipeLogic().isWorking() || recipe == null ||
                recipe.recipeType != GTNARecipeType.RANDOM_VOID_MINING_RECIPES)
            return;
        var outputs = recipe.getOutputContents(ItemRecipeCapability.CAP);
        data.putInt("GTNAVoidPool", outputs.size());
        int tier = RecipeHelper.getPreOCRecipeEuTier(recipe);
        double min = 100.0;
        double max = 0.0;
        for (var output : outputs) {
            double chance = 100.0 * recipe.getType().getChanceFunction()
                    .getBoostedChance(output, tier, tier + recipe.ocLevel) / output.maxChance;
            min = Math.min(min, chance);
            max = Math.max(max, chance);
        }
        data.putString("GTNAVoidChance", format(min) + (min == max ? "" : "–" + format(max)));
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        var data = accessor.getServerData();
        if (!data.contains("GTNAVoidPool")) return;
        tooltip.remove(GTCEu.id("recipe_output_info"));
        tooltip.add(Component.translatable("gtna.jade.void_random_pool", data.getInt("GTNAVoidPool")));
        tooltip.add(Component.translatable("gtna.jade.void_random_chance", data.getString("GTNAVoidChance")));
    }

    private static String format(double value) {
        return java.math.BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }
}
