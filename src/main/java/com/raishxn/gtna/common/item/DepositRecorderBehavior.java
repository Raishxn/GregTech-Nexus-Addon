package com.raishxn.gtna.common.item;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.item.TooltipBehavior;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.FluidDrillMachine;
import com.gregtechceu.gtceu.common.machine.owner.MachineOwner;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import com.raishxn.gtna.api.machine.feature.IFluidExtractionReceipt;
import com.raishxn.gtna.common.data.GTNAItems;
import com.raishxn.gtna.common.data.fluid.FluidDiscoveryData;
import com.raishxn.gtna.config.VoidFluidDrillConfig;

import java.util.UUID;

/** Right-click a successful native rig to register; use in air with a card offhand to copy. */
public final class DepositRecorderBehavior extends TooltipBehavior implements IInteractionItem {

    public DepositRecorderBehavior() {
        super(lines -> lines.add(Component.translatable("gtna.fluid.recorder.tooltip")));
    }

    public static UUID owner(Player player) {
        var owner = MachineOwner.getOwner(player.getUUID());
        return scope(owner, player.getUUID());
    }

    public static UUID scope(MachineOwner owner, UUID playerId) {
        if (com.gregtechceu.gtceu.GTCEu.Mods.isFTBTeamsLoaded()) {
            var team = com.raishxn.gtna.integration.ftb.FTBFluidDiscoveryOwner.effectiveTeam(owner);
            if (team != null && !team.equals(MachineOwner.EMPTY)) return team;
        }
        if (owner == null || owner.getUUID().equals(MachineOwner.EMPTY)) return playerId;
        return owner.getUUID();
    }

    public static CompoundTag programTag(String dimension, String fluid) {
        CompoundTag tag = new CompoundTag();
        tag.putString("dimension", dimension);
        tag.putString("fluid", fluid);
        return tag;
    }

    public static boolean validCard(ServerLevel level, ItemStack card, UUID owner, String dimension, String fluid) {
        if (owner == null || !card.is(GTNAItems.DEPOSIT_DATA.get()) || card.getTag() == null) return false;
        var tag = card.getTag();
        return tag.hasUUID("certificate") && tag.hasUUID("owner") && tag.getUUID("owner").equals(owner) &&
                dimension.equals(tag.getString("dimension")) && fluid.equals(tag.getString("fluid")) &&
                FluidDiscoveryData.get(level).allows(tag.getUUID("certificate"), owner, dimension, fluid);
    }

    private static ItemStack card(ServerLevel level, UUID owner, String dimension, String fluid) {
        ItemStack card = GTNAItems.DEPOSIT_DATA.asStack();
        CompoundTag tag = programTag(dimension, fluid);
        tag.putUUID("owner", owner);
        tag.putUUID("certificate", FluidDiscoveryData.get(level).discover(owner, dimension, fluid));
        card.setTag(tag);
        return card;
    }

    private static boolean consumeStick(Player player) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.is(GTItems.TOOL_DATA_STICK.get())) {
                stack.shrink(1);
                player.getInventory().setChanged();
                return true;
            }
        }
        return false;
    }

    private static void give(Player player, ItemStack stack) {
        if (!player.getInventory().add(stack)) player.drop(stack, false);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || !(context.getLevel() instanceof ServerLevel level)) return InteractionResult.PASS;
        if (!(level.getBlockEntity(context.getClickedPos()) instanceof MetaMachineBlockEntity be) ||
                !(be.getMetaMachine() instanceof FluidDrillMachine rig))
            return InteractionResult.PASS;
        if (!VoidFluidDrillConfig.get().enabled) return InteractionResult.FAIL;
        var rigOwner = rig.getOwner();
        if (rigOwner != null && !scope(rigOwner, rig.getOwnerUUID()).equals(owner(player))) {
            player.displayClientMessage(Component.translatable("gtna.fluid.recorder.owner"), true);
            return InteractionResult.FAIL;
        }
        if (!(rig.getRecipeLogic() instanceof IFluidExtractionReceipt receipt) ||
                receipt.gtna$getExtractedFluid() == null) {
            player.displayClientMessage(Component.translatable("gtna.fluid.recorder.extract_first"), true);
            return InteractionResult.FAIL;
        }
        String dimension = level.dimension().location().toString();
        String fluid = BuiltInRegistries.FLUID.getKey(receipt.gtna$getExtractedFluid()).toString();
        if (VoidFluidDrillConfig.get().program(dimension, fluid) == null) {
            player.displayClientMessage(Component.translatable("gtna.fluid.recorder.unsupported"), true);
            return InteractionResult.FAIL;
        }
        if (!consumeStick(player)) {
            player.displayClientMessage(Component.translatable("gtna.fluid.recorder.stick"), true);
            return InteractionResult.FAIL;
        }
        give(player, card(level, owner(player), dimension, fluid));
        player.displayClientMessage(Component.translatable("gtna.fluid.recorder.recorded"), true);
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Item item, Level world, Player player, InteractionHand hand) {
        ItemStack tool = player.getItemInHand(hand);
        if (!(world instanceof ServerLevel level) || !VoidFluidDrillConfig.get().enabled) {
            return InteractionResultHolder.pass(tool);
        }
        ItemStack source = player.getItemInHand(
                hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
        if (!source.is(GTNAItems.DEPOSIT_DATA.get()) || source.getTag() == null)
            return InteractionResultHolder.pass(tool);
        var tag = source.getTag();
        if (!validCard(level, source, owner(player), tag.getString("dimension"), tag.getString("fluid"))) {
            player.displayClientMessage(Component.translatable("gtna.fluid.recorder.owner"), true);
            return InteractionResultHolder.fail(tool);
        }
        if (!consumeStick(player)) {
            player.displayClientMessage(Component.translatable("gtna.fluid.recorder.stick"), true);
            return InteractionResultHolder.fail(tool);
        }
        give(player, source.copyWithCount(1));
        player.displayClientMessage(Component.translatable("gtna.fluid.recorder.copied"), true);
        return InteractionResultHolder.success(tool);
    }
}
