package com.raishxn.gtna.integration.jade.provider;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import com.raishxn.gtna.GTNACORE;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

/** Shows the physical block values in Jade, as the GTO block tooltip does. */
public final class GTNABlockStatsProvider implements IBlockComponentProvider {

    public static final GTNABlockStatsProvider INSTANCE = new GTNABlockStatsProvider();

    private GTNABlockStatsProvider() {}

    @Override
    public ResourceLocation getUid() {
        return GTNACORE.id("block_stats");
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (!GTNACORE.MOD_ID.equals(accessor.getBlockState().getBlock().builtInRegistryHolder().key().location()
                .getNamespace()))
            return;
        float hardness = accessor.getBlockState().getDestroySpeed(accessor.getLevel(), accessor.getPosition());
        float blastResistance = accessor.getBlockState().getBlock().getExplosionResistance();
        tooltip.add(Math.min(1, tooltip.size()), Component.translatable("gtna.jade.hardness",
                Component.literal(format(hardness)).withStyle(ChatFormatting.BLUE))
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(" "))
                .append(Component.translatable("gtna.jade.blast_resistance",
                        Component.literal(format(blastResistance)).withStyle(ChatFormatting.BLUE))
                        .withStyle(ChatFormatting.GRAY)));
    }

    private static String format(float value) {
        return java.math.BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }
}
