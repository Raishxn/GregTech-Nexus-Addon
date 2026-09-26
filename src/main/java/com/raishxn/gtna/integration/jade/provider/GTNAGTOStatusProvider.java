package com.raishxn.gtna.integration.jade.provider;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;

import com.raishxn.gtna.GTNACORE;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import java.util.Locale;
import java.util.Set;

/** Extra GTO-style diagnostics computed from the actual GTNA recipe, without replacing GTCEu data. */
public final class GTNAGTOStatusProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    public static final GTNAGTOStatusProvider INSTANCE = new GTNAGTOStatusProvider();
    private static final Set<String> PORTS = Set.of(
            "fishing_ground", "evaporation_plant", "greenhouse", "component_assembler",
            "component_assembly_line", "large_greenhouse", "blaze_blast_furnace", "cold_ice_freezer",
            "chemical_plant", "mega_alloy_blast_smelter", "isa_mill", "industrial_flotation_cell",
            "vacuum_drying_furnace");

    private GTNAGTOStatusProvider() {}

    @Override
    public ResourceLocation getUid() {
        return GTNACORE.id("gto_status");
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        BlockEntity blockEntity = accessor.getBlockEntity();
        if (!(blockEntity instanceof MetaMachineBlockEntity entity) ||
                !(entity.getMetaMachine() instanceof WorkableElectricMultiblockMachine machine))
            return;
        var id = machine.getDefinition().getId();
        if (!GTNACORE.MOD_ID.equals(id.getNamespace()) || !PORTS.contains(id.getPath())) return;
        data.putBoolean("GTNAGTOPort", true);
        if (accessor.getLevel() instanceof ServerLevel serverLevel) {
            data.putBoolean("GTNAChunkNotForced", !serverLevel.getForcedChunks()
                    .contains(new ChunkPos(accessor.getPosition()).toLong()));
        }
        if ("isa_mill".equals(id.getPath())) return; // Its dedicated provider already shows multipliers.
        var logic = machine.getRecipeLogic();
        var active = logic.getLastRecipe();
        var origin = logic.getLastOriginRecipe();
        if (active == null || origin == null || origin.duration <= 0) return;
        long activeEUt = RecipeHelper.getRealEUt(active).getTotalEU();
        long originEUt = RecipeHelper.getRealEUt(origin).getTotalEU();
        if (originEUt > 0) data.putDouble("GTNAEnergyPercent", 100.0 * activeEUt / originEUt);
        data.putDouble("GTNATimePercent", 100.0 * active.duration / origin.duration);
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        if (!data.getBoolean("GTNAGTOPort")) return;
        int index = Math.min(3, tooltip.size());
        if (data.getBoolean("GTNAChunkNotForced")) {
            tooltip.add(index++, Component.translatable("gtna.jade.chunk_not_forced")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
        }
        if (data.contains("GTNAEnergyPercent")) {
            tooltip.add(index++, Component.translatable("gtna.jade.energy_multiplier",
                    Component.literal(percent(data.getDouble("GTNAEnergyPercent")))
                            .withStyle(ChatFormatting.GOLD))
                    .withStyle(ChatFormatting.GRAY));
        }
        if (data.contains("GTNATimePercent")) {
            tooltip.add(index, Component.translatable("gtna.jade.time_multiplier",
                    Component.literal(percent(data.getDouble("GTNATimePercent")))
                            .withStyle(ChatFormatting.GOLD))
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    private static String percent(double value) {
        String formatted = String.format(Locale.ROOT, "%.2f", value);
        return formatted.replaceFirst("\\.?0+$", "") + "%";
    }
}
