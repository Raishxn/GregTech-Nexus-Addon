package com.raishxn.gtna.common.item;

import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.WorkableTieredMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import com.raishxn.gtna.utils.GTNATooltips;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CoordinateCardBehavior implements IInteractionItem, IAddInformation {

    public static final CoordinateCardBehavior INSTANCE = new CoordinateCardBehavior();

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }

        BlockPos blockPos = context.getClickedPos();
        BlockState blockState = level.getBlockState(blockPos);
        if (!blockState.hasBlockEntity()) {
            return InteractionResult.PASS;
        }

        ItemStack card = player.getItemInHand(context.getHand());
        CompoundTag tag = card.getOrCreateTag();
        tag.putInt("x", blockPos.getX());
        tag.putInt("y", blockPos.getY());
        tag.putInt("z", blockPos.getZ());

        MetaMachine machine = MetaMachine.getMachine(level, blockPos);
        if (machine instanceof WorkableTieredMachine || machine instanceof IMultiController) {
            tag.putBoolean("machine", true);
        } else {
            tag.remove("machine");
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Item item, Level level, Player player, InteractionHand usedHand) {
        if (!level.isClientSide) {
            player.getItemInHand(usedHand).setTag(null);
        }
        return IInteractionItem.super.use(item, level, player, usedHand);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
        list.add(GTNATooltips.desc("item.gtna.coordinate_card.tooltip.1"));
        CompoundTag tag = itemStack.getTag();
        if (tag != null && tag.contains("x") && tag.contains("y") && tag.contains("z")) {
            list.add(GTNATooltips.info("item.gtna.coordinate_card.tooltip.2",
                    tag.getInt("x"), tag.getInt("y"), tag.getInt("z")));
        }
    }

    @Nullable
    public static BlockPos getStoredCoordinates(ItemStack itemStack) {
        CompoundTag tag = itemStack.getTag();
        if (tag != null && tag.contains("x") && tag.contains("y") && tag.contains("z")) {
            return new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"));
        }
        return null;
    }
}
