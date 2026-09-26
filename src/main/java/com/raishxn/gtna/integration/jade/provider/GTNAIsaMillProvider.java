package com.raishxn.gtna.integration.jade.provider;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.common.machine.multiblock.electric.IsaMillMachine;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import java.util.Locale;

/** ISA Mill status beyond the common GTCEu progress, recipe output and structure providers. */
public final class GTNAIsaMillProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    public static final GTNAIsaMillProvider INSTANCE = new GTNAIsaMillProvider();
    private static final ResourceLocation UID = GTNACORE.id("isa_mill_status");

    private GTNAIsaMillProvider() {}

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (accessor.getBlockEntity() instanceof MetaMachineBlockEntity entity &&
                entity.getMetaMachine() instanceof IsaMillMachine mill) {
            data.putBoolean("GTNAIsaBallMissing", mill.isGrindBallMissing());
            var logic = mill.getRecipeLogic();
            var active = logic.getLastRecipe();
            var original = logic.getLastOriginRecipe();
            if (active != null && original != null && original.duration > 0) {
                long originalEUt = RecipeHelper.getRealEUt(original).getTotalEU();
                long activeEUt = RecipeHelper.getRealEUt(active).getTotalEU();
                if (originalEUt > 0) {
                    data.putDouble("GTNAIsaEnergyPercent", 100.0 * activeEUt / originalEUt);
                }
                data.putDouble("GTNAIsaTimePercent", 100.0 * active.duration / original.duration);
            }
        }
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (accessor.getServerData().getBoolean("GTNAIsaBallMissing")) {
            tooltip.add(Component.translatable("gtna.jade.need_grind_ball").withStyle(ChatFormatting.RED));
        }
        CompoundTag data = accessor.getServerData();
        if (data.contains("GTNAIsaEnergyPercent")) {
            tooltip.add(Component.translatable("gtna.jade.energy_multiplier",
                    String.format(Locale.ROOT, "%.2f%%", data.getDouble("GTNAIsaEnergyPercent")))
                    .withStyle(ChatFormatting.GRAY));
        }
        if (data.contains("GTNAIsaTimePercent")) {
            tooltip.add(Component.translatable("gtna.jade.time_multiplier",
                    String.format(Locale.ROOT, "%.2f%%", data.getDouble("GTNAIsaTimePercent")))
                    .withStyle(ChatFormatting.GRAY));
        }
    }
}
