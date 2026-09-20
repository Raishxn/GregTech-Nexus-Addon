package com.raishxn.gtna.integration.jade.provider;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.integration.jade.GTElementHelper;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.common.machine.multiblock.part.ae.GTNAMEPatternBufferPartMachine;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.fluid.JadeFluidObject;
import snownee.jade.api.ui.IElementHelper;

/**
 * Jade overlay for the ME Pattern Buffer (GTM official {@code MEPatternBufferProvider} parity):
 * shows the buffer's merged slot contents (items + fluids) plus the number of bound proxies.
 */
public class GTNAPatternBufferProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    public static final GTNAPatternBufferProvider INSTANCE = new GTNAPatternBufferProvider();
    private static final ResourceLocation UID = GTNACORE.id("me_pattern_buffer");

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        if (!data.getBoolean("formed")) {
            return;
        }
        tooltip.add(Component.translatable("gtna.jade.pattern_buffer.proxies", data.getInt("proxies")));
        readBufferTag(tooltip, data);
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (accessor.getBlockEntity() instanceof MetaMachineBlockEntity machineBlock &&
                machineBlock.getMetaMachine() instanceof GTNAMEPatternBufferPartMachine buffer) {
            data.putBoolean("formed", buffer.isFormed());
            if (!buffer.isFormed()) {
                return;
            }
            data.putInt("proxies", buffer.getProxyCount());
            writeBufferTag(data, buffer);
        }
    }

    public static void writeBufferTag(CompoundTag data, GTNAMEPatternBufferPartMachine buffer) {
        var merged = buffer.mergeInternalSlots();
        ListTag itemsTag = new ListTag();
        for (var entry : merged.items().object2LongEntrySet()) {
            CompoundTag entryTag = entry.getKey().save(new CompoundTag());
            entryTag.putLong("real", entry.getLongValue());
            itemsTag.add(entryTag);
        }
        if (!itemsTag.isEmpty()) {
            data.put("items", itemsTag);
        }
        ListTag fluidsTag = new ListTag();
        for (var entry : merged.fluids().object2LongEntrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entry.getKey().writeToNBT(entryTag);
            entryTag.putLong("real", entry.getLongValue());
            fluidsTag.add(entryTag);
        }
        if (!fluidsTag.isEmpty()) {
            data.put("fluids", fluidsTag);
        }
    }

    public static void readBufferTag(ITooltip tooltip, CompoundTag data) {
        IElementHelper helper = tooltip.getElementHelper();
        for (Tag tag : data.getList("items", Tag.TAG_COMPOUND)) {
            if (!(tag instanceof CompoundTag entry)) {
                continue;
            }
            ItemStack stack = ItemStack.of(entry);
            long count = entry.getLong("real");
            if (!stack.isEmpty() && count > 0) {
                tooltip.add(helper.smallItem(stack));
                tooltip.append(Component.literal(" ")
                        .append(Component.literal(FormattingUtil.formatNumbers(count))
                                .withStyle(ChatFormatting.DARK_PURPLE))
                        .append(Component.literal("× ").withStyle(ChatFormatting.WHITE))
                        .append(stack.getHoverName().copy().withStyle(ChatFormatting.GOLD)));
            }
        }
        for (Tag tag : data.getList("fluids", Tag.TAG_COMPOUND)) {
            if (!(tag instanceof CompoundTag entry)) {
                continue;
            }
            FluidStack stack = FluidStack.loadFluidStackFromNBT(entry);
            long amount = entry.getLong("real");
            if (!stack.isEmpty() && amount > 0) {
                tooltip.add(GTElementHelper.smallFluid(JadeFluidObject.of(stack.getFluid())));
                tooltip.append(Component.literal(" ")
                        .append(Component.literal(FormattingUtil.formatBuckets(amount))
                                .withStyle(ChatFormatting.DARK_PURPLE))
                        .append(Component.literal(" ").withStyle(ChatFormatting.WHITE))
                        .append(stack.getDisplayName().copy().withStyle(ChatFormatting.DARK_AQUA)));
            }
        }
    }
}
