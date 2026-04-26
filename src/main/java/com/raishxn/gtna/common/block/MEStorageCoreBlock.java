package com.raishxn.gtna.common.block;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

import javax.annotation.Nullable;

public class MEStorageCoreBlock extends Block {

    private static final long BASE_CAPACITY = 67_108_864L;

    private final int tier;
    private final long capacity;
    private final boolean craftingCore;

    public MEStorageCoreBlock(Properties properties, int tier, boolean craftingCore) {
        super(properties);
        this.tier = tier;
        this.craftingCore = craftingCore;
        this.capacity = BASE_CAPACITY * (1L << (tier << 1));
    }

    public int getTier() {
        return tier;
    }

    public long getCapacity() {
        return capacity;
    }

    public boolean isCraftingCore() {
        return craftingCore;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable BlockGetter level,
                                @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.literal("Tier " + tier + " Matrix " + (craftingCore ? "Crafting" : "Storage") + " Module")
                .withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.literal("Capacity: " + formatBytes(capacity)).withStyle(ChatFormatting.GRAY));
    }

    private static String formatBytes(long bytes) {
        String[] units = { "B", "KiB", "MiB", "GiB", "TiB", "PiB" };
        double value = bytes;
        int unit = 0;
        while (value >= 1024.0D && unit < units.length - 1) {
            value /= 1024.0D;
            unit++;
        }
        return String.format(Locale.US, value == Math.rint(value) ? "%.0f %s" : "%.2f %s", value, units[unit]);
    }
}
