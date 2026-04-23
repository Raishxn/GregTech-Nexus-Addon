package com.raishxn.gtna.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import com.raishxn.gtna.client.renderer.BlockHighlightHandler;

import java.util.function.Supplier;

public class SRegionHighlightPacket {

    private final BlockPos start;
    private final BlockPos end;
    private final ResourceKey<Level> dim;
    private final int color;
    private final long expiryTime;
    private final boolean clear;

    public SRegionHighlightPacket(BlockPos start, BlockPos end, ResourceKey<Level> dim, int color, long expiryTime, boolean clear) {
        this.start = start;
        this.end = end;
        this.dim = dim;
        this.color = color;
        this.expiryTime = expiryTime;
        this.clear = clear;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarLong(start.asLong());
        buf.writeVarLong(end.asLong());
        buf.writeResourceKey(dim);
        buf.writeInt(color);
        buf.writeVarLong(expiryTime);
        buf.writeBoolean(clear);
    }

    public static SRegionHighlightPacket decode(FriendlyByteBuf buf) {
        return new SRegionHighlightPacket(
                BlockPos.of(buf.readVarLong()),
                BlockPos.of(buf.readVarLong()),
                buf.readResourceKey(Registries.DIMENSION),
                buf.readInt(),
                buf.readVarLong(),
                buf.readBoolean());
    }

    public static void handle(SRegionHighlightPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (msg.clear) {
                BlockHighlightHandler.stopRegionHighlight(msg.start, msg.end, msg.dim);
            } else {
                BlockHighlightHandler.highlightRegion(msg.start, msg.end, msg.dim, msg.color, msg.expiryTime);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
