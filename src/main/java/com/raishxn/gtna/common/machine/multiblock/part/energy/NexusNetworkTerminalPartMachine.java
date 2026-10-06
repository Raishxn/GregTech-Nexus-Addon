// SPDX-License-Identifier: GPL-3.0-only
// Adapted behavior: GTLAdditions 8caff5e93a5e65914d10dd176d48d66e7ec8c329; see THIRD_PARTY_NOTICES.md.
package com.raishxn.gtna.common.machine.multiblock.part.energy;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IInteractedMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.gregtechceu.gtceu.common.data.GTItems;

import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import com.raishxn.gtna.api.capability.WirelessEnergyManager;
import com.raishxn.gtna.utils.datastructure.Int128;

import java.util.UUID;

/** GTLAdditions network-terminal behavior adapted to GTNA's bounded, loss-aware Nexus account. */
public class NexusNetworkTerminalPartMachine extends TieredIOPartMachine implements IMachineLife, IInteractedMachine {

    private static final ManagedFieldHolder FIELDS = new ManagedFieldHolder(NexusNetworkTerminalPartMachine.class,
            TieredIOPartMachine.MANAGED_FIELD_HOLDER);
    @Persisted
    private UUID networkOwner;
    public final NexusNetworkEnergyContainer energyContainer;
    public final IO networkIO;

    public NexusNetworkTerminalPartMachine(IMachineBlockEntity holder, IO io) {
        super(holder, GTValues.MAX, io);
        networkIO = io;
        energyContainer = new NexusNetworkEnergyContainer(this);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return FIELDS;
    }

    public UUID getNetworkOwner() {
        return networkOwner;
    }

    public void setNetworkOwner(UUID owner) {
        networkOwner = owner;
        onChanged();
        energyContainer.notifyListeners();
    }

    @Override
    public void onMachinePlaced(LivingEntity player, ItemStack stack) {
        if (player != null) setNetworkOwner(player.getUUID());
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!getLevel().isClientSide) subscribeServerTick(() -> report(Int128.ZERO()));
    }

    public void report(Int128 transferred) {
        if (getLevel() instanceof ServerLevel level && networkOwner != null) {
            WirelessEnergyManager.reportConnection(level, networkOwner,
                    GlobalPos.of(level.dimension(), getPos()), networkIO == IO.OUT, GTValues.MAX, 1,
                    networkIO == IO.OUT ? "Nexus Network Output Terminal" : "Nexus Network Input Terminal",
                    transferred);
        }
    }

    @Override
    public boolean canShared() {
        return false;
    }

    @Override
    public boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        return false;
    }

    @Override
    public InteractionResult onUse(BlockState state, Level world, BlockPos pos, Player player,
                                   InteractionHand hand, BlockHitResult hit) {
        if (!player.getItemInHand(hand).is(GTItems.TOOL_DATA_STICK.asItem())) return InteractionResult.PASS;
        if (!world.isClientSide) {
            setNetworkOwner(player.getUUID());
            player.displayClientMessage(Component.translatable("gtna.network_terminal.bound", player.getName()), true);
        }
        return InteractionResult.sidedSuccess(world.isClientSide);
    }

    @Override
    public boolean onLeftClick(Player player, Level world, InteractionHand hand, BlockPos pos, Direction direction) {
        if (!player.getItemInHand(hand).is(GTItems.TOOL_DATA_STICK.asItem())) return false;
        if (!world.isClientSide) {
            setNetworkOwner(null);
            player.displayClientMessage(Component.translatable("gtna.network_terminal.unbound"), true);
        }
        return true;
    }
}
