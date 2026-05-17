package com.raishxn.gtna.common.item;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import com.raishxn.gtna.common.cover.WirelessEUCoverBase;
import com.raishxn.gtna.common.cover.WirelessEUReceiverCover;
import com.raishxn.gtna.common.cover.WirelessEUTransmitterCover;
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
        ItemStack stack = context.getItemInHand();

        // ── 1. Nexus Flux Matrix: copy network UUID into linker ───────────────
        if (be instanceof IMachineBlockEntity mbe) {
            MetaMachine machine = mbe.getMetaMachine();

            if (machine instanceof NexusFluxMatrixMachine controller) {
                if (player.isShiftKeyDown()) {
                    UUID owner = controller.getOwnerUUID();
                    if (owner == null) owner = player.getUUID();
                    CompoundTag tag = stack.getOrCreateTag();
                    tag.putUUID("NetworkID", owner);
                    stack.setTag(tag);
                    player.displayClientMessage(
                            Component.translatable("gtna.message.linker.copied")
                                    .withStyle(ChatFormatting.GREEN), true);
                    return InteractionResult.SUCCESS;
                }

            // ── 2. Wireless Energy Hatch ──────────────────────────────────────
            } else if (machine instanceof WirelessEnergyHatchPartMachine h) {
                return handleHatchBinding(stack, player,
                        netId -> h.setNetworkOwner(netId), "Energy Hatch");

            // ── 3. Wireless Dynamo Hatch ──────────────────────────────────────
            } else if (machine instanceof WirelessDynamoHatchPartMachine h) {
                return handleHatchBinding(stack, player,
                        netId -> h.setNetworkOwner(netId), "Dynamo Hatch");
            }
        }

        // ── 4. Wireless EU Cover (receiver or transmitter) ────────────────────
        // ICoverable.getCoverAtSide() is defined directly on the ICoverable interface.
        if (be instanceof ICoverable coverable) {
            Direction face = context.getClickedFace();
            CoverBehavior cover = coverable.getCoverAtSide(face);

            if (cover instanceof WirelessEUCoverBase wirelessCover) {
                String coverName = (wirelessCover instanceof WirelessEUReceiverCover)
                        ? "EU Receiver Cover" : "EU Transmitter Cover";
                return handleCoverBinding(stack, player, wirelessCover, coverName);
            }
        }

        return InteractionResult.PASS;
    }

    private InteractionResult handleHatchBinding(ItemStack stack, Player player,
                                                  java.util.function.Consumer<UUID> setter,
                                                  String machineName) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.hasUUID("NetworkID")) {
            setter.accept(tag.getUUID("NetworkID"));
            player.displayClientMessage(
                    Component.translatable("gtna.message.linker.linked", machineName)
                            .withStyle(ChatFormatting.AQUA), true);
            return InteractionResult.SUCCESS;
        } else if (player.isShiftKeyDown()) {
            setter.accept(null);
            player.displayClientMessage(
                    Component.translatable("gtna.message.linker.unbound", machineName)
                            .withStyle(ChatFormatting.YELLOW), true);
            return InteractionResult.SUCCESS;
        } else {
            player.displayClientMessage(
                    Component.translatable("gtna.message.linker.no_network_id"), true);
            return InteractionResult.SUCCESS;
        }
    }

    private InteractionResult handleCoverBinding(ItemStack stack, Player player,
                                                  WirelessEUCoverBase cover, String coverName) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.hasUUID("NetworkID")) {
            cover.setNetworkOwner(tag.getUUID("NetworkID"));
            player.displayClientMessage(
                    Component.translatable("gtna.message.linker.linked", coverName)
                            .withStyle(ChatFormatting.AQUA), true);
            return InteractionResult.SUCCESS;
        } else if (player.isShiftKeyDown()) {
            cover.setNetworkOwner(null);
            player.displayClientMessage(
                    Component.translatable("gtna.message.linker.unbound", coverName)
                            .withStyle(ChatFormatting.YELLOW), true);
            return InteractionResult.SUCCESS;
        } else {
            player.displayClientMessage(
                    Component.translatable("gtna.message.linker.no_network_id"), true);
            return InteractionResult.SUCCESS;
        }
    }
}
