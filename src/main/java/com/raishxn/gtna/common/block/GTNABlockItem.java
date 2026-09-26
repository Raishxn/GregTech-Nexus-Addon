package com.raishxn.gtna.common.block;

import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.List;

import javax.annotation.Nullable;

/** Shows the physical properties of GTNA casings alongside the normal source tooltip. */
public class GTNABlockItem extends BlockItem {

    public GTNABlockItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip,
                                TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        Block block = getBlock();
        tooltip.add(Component.translatable("gtna.tooltip.block_properties",
                FormattingUtil.formatNumbers(block.defaultDestroyTime()),
                FormattingUtil.formatNumbers(block.getExplosionResistance())).withStyle(ChatFormatting.GRAY));
    }
}
