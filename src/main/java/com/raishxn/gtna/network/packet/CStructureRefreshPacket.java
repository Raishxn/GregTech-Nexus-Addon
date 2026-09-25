package com.raishxn.gtna.network.packet;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.MultiblockState;
import com.gregtechceu.gtceu.api.pattern.error.PatternError;
import com.gregtechceu.gtceu.api.pattern.error.PatternStringError;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import com.raishxn.gtna.api.machine.multiblock.GTNAPatternDiagnostics;
import com.raishxn.gtna.api.machine.multiblock.GTNAStructureRefresh;
import com.raishxn.gtna.api.machine.multiblock.GTNASubPatterns;
import com.raishxn.gtna.api.machine.multiblock.IGTNAModuleHost;
import com.raishxn.gtna.network.GTNANetworkHandler;

import java.util.List;
import java.util.function.Supplier;

/** Requests a structure recheck and reports the first incorrect block to the player. */
public record CStructureRefreshPacket(BlockPos pos, boolean force) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeBoolean(force);
    }

    public static CStructureRefreshPacket decode(FriendlyByteBuf buf) {
        return new CStructureRefreshPacket(buf.readBlockPos(), buf.readBoolean());
    }

    public static void handle(CStructureRefreshPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || player.distanceToSqr(msg.pos.getX() + 0.5, msg.pos.getY() + 0.5,
                    msg.pos.getZ() + 0.5) > 64 * 64) {
                return;
            }
            if (!(player.serverLevel().getBlockEntity(msg.pos) instanceof MetaMachineBlockEntity holder) ||
                    !(holder.getMetaMachine() instanceof MultiblockControllerMachine controller)) {
                return;
            }
            if (controller.isFormed() && !msg.force) {
                return;
            }

            GTNAStructureRefresh.refresh(controller, msg.force);
            if (!controller.isFormed()) {
                GTNAPatternDiagnostics.Mismatch mismatch = GTNAPatternDiagnostics.firstMismatch(controller,
                        controller.getPattern());
                if (mismatch != null) reportMismatch(player, mismatch);
                else reportError(player, controller.getMultiblockState().error);
                return;
            }

            int total = GTNASubPatterns.get(controller.getDefinition()).size();
            int formed = ((IGTNAModuleHost) controller).gtna$formedModuleCount();
            if (formed < total) {
                for (BlockPattern module : GTNASubPatterns.get(controller.getDefinition())) {
                    MultiblockState state = new MultiblockState(player.serverLevel(), msg.pos);
                    if (!module.checkPatternAt(state, false)) {
                        GTNAPatternDiagnostics.Mismatch mismatch = GTNAPatternDiagnostics.firstMismatch(controller,
                                module);
                        if (mismatch != null) reportMismatch(player, mismatch);
                        else reportError(player, state.error);
                        return;
                    }
                }
                player.displayClientMessage(Component.translatable("gtna.machine.structure_check.rejected"), false);
                return;
            }
            // A successful check is reflected in the button state; repeated clicks should not spam chat.
        });
        ctx.get().setPacketHandled(true);
    }

    private static void reportError(ServerPlayer player, PatternError error) {
        if (error == null || error == MultiblockState.UNINIT_ERROR || error == MultiblockState.UNLOAD_ERROR) {
            player.displayClientMessage(Component.translatable("gtna.machine.structure_check.generic", "unknown"),
                    false);
            return;
        }
        BlockPos errorPos = error.getPos();
        if (errorPos == null) {
            player.displayClientMessage(Component.translatable("gtna.machine.structure_check.generic",
                    error.getErrorInfo()), false);
            return;
        }
        if (error instanceof PatternStringError) {
            player.displayClientMessage(Component.translatable("gtna.machine.structure_check.generic",
                    error.getErrorInfo()), false);
            highlight(player, errorPos);
            return;
        }
        ItemStack expected = error.getCandidates().stream().flatMap(List::stream)
                .filter(stack -> !stack.isEmpty()).findFirst().orElse(ItemStack.EMPTY);
        Component expectedName = expected.isEmpty() ? error.getErrorInfo() : expected.getHoverName();
        Component actualName = player.serverLevel().getBlockState(errorPos).getBlock().getName();
        player.displayClientMessage(Component.translatable("gtna.machine.structure_check.missing",
                errorPos.toShortString(), actualName, expectedName), false);
        highlight(player, errorPos);
    }

    private static void reportMismatch(ServerPlayer player, GTNAPatternDiagnostics.Mismatch mismatch) {
        BlockPos pos = mismatch.pos();
        Component expected = mismatch.expected().isEmpty() ?
                Component.translatable("gtna.machine.structure_check.expected") : mismatch.expected().getHoverName();
        Component actual = player.serverLevel().getBlockState(pos).getBlock().getName();
        player.displayClientMessage(Component.translatable("gtna.machine.structure_check.missing",
                pos.toShortString(), actual, expected), false);
        highlight(player, pos);
    }

    private static void highlight(ServerPlayer player, BlockPos errorPos) {
        GTNANetworkHandler.sendToPlayer(new SStructureDetectHighlight(errorPos, player.level().dimension(),
                System.currentTimeMillis() + 15_000L), player);
    }
}
