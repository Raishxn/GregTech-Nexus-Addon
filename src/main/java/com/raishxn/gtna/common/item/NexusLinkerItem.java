package com.raishxn.gtna.common.item;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import com.raishxn.gtna.common.machine.multiblock.energy.NexusFluxMatrixMachine;
import com.raishxn.gtna.common.machine.multiblock.part.energy.WirelessDynamoHatchPartMachine;
import com.raishxn.gtna.common.machine.multiblock.part.energy.WirelessEnergyHatchPartMachine;

import java.util.UUID;

public class NexusLinkerItem extends Item {

    public NexusLinkerItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) return InteractionResult.SUCCESS;

        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        BlockEntity be = level.getBlockEntity(context.getClickedPos());
        if (be instanceof IMachineBlockEntity mbe) {
            MetaMachine machine = mbe.getMetaMachine();
            ItemStack stack = context.getItemInHand();

            if (machine instanceof NexusFluxMatrixMachine controller) {
                if (player.isShiftKeyDown()) {
                    UUID owner = controller.getOwnerUUID();
                    if (owner == null) owner = player.getUUID(); // fallback if unowned

                    CompoundTag tag = stack.getOrCreateTag();
                    tag.putUUID("NetworkID", owner);
                    stack.setTag(tag);

                    player.displayClientMessage(
                            Component.translatable("gtna.message.linker.copied").withStyle(ChatFormatting.GREEN), true);
                    return InteractionResult.SUCCESS;
                }
            } else if (machine instanceof WirelessEnergyHatchPartMachine energyHatch) {
                if (player.isShiftKeyDown()) {
                    energyHatch.setNetworkOwner(null);
                    player.displayClientMessage(Component.translatable("gtna.message.linker.unbound", "Energy Hatch")
                            .withStyle(ChatFormatting.YELLOW), true);
                    return InteractionResult.SUCCESS;
                }
                CompoundTag tag = stack.getTag();
                if (tag != null && tag.hasUUID("NetworkID")) {
                    UUID netId = tag.getUUID("NetworkID");
                    energyHatch.setNetworkOwner(netId);
                    player.displayClientMessage(Component.translatable("gtna.message.linker.linked", "Energy Hatch")
                            .withStyle(ChatFormatting.AQUA), true);
                    return InteractionResult.SUCCESS;
                }
            } else if (machine instanceof WirelessDynamoHatchPartMachine dynamoHatch) {
                if (player.isShiftKeyDown()) {
                    dynamoHatch.setNetworkOwner(null);
                    player.displayClientMessage(Component.translatable("gtna.message.linker.unbound", "Dynamo Hatch")
                            .withStyle(ChatFormatting.YELLOW), true);
                    return InteractionResult.SUCCESS;
                }
                CompoundTag tag = stack.getTag();
                if (tag != null && tag.hasUUID("NetworkID")) {
                    UUID netId = tag.getUUID("NetworkID");
                    dynamoHatch.setNetworkOwner(netId);
                    player.displayClientMessage(Component.translatable("gtna.message.linker.linked", "Dynamo Hatch")
                            .withStyle(ChatFormatting.AQUA), true);
                    return InteractionResult.SUCCESS;
                }
            }
        }

        return InteractionResult.PASS;
    }
}
