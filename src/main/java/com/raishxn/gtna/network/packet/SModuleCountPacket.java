package com.raishxn.gtna.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import com.raishxn.gtna.client.ModuleCountClientHandler;

import java.util.function.Supplier;

/**
 * Server → Client packet: the number of auxiliary modules (sub-patterns) that matched a multiblock
 * controller. The count lives in a mixin field that the server updates on every structure check;
 * without this sync the client would always read zero, so the machine-mode switcher (which filters
 * the recipe types by module availability, e.g. the Cold Ice Freezer atomization mode) would never
 * show the unlocked type.
 *
 * <p>
 * The client work lives in {@link ModuleCountClientHandler} (an {@code @OnlyIn(CLIENT)} class) so
 * this packet class never references client-only Minecraft classes on the dedicated server.
 */
public record SModuleCountPacket(BlockPos pos, int count) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeVarInt(this.count);
    }

    public static SModuleCountPacket decode(FriendlyByteBuf buf) {
        return new SModuleCountPacket(buf.readBlockPos(), buf.readVarInt());
    }

    public static void handle(SModuleCountPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ModuleCountClientHandler.apply(msg.pos, msg.count));
        ctx.get().setPacketHandled(true);
    }
}
