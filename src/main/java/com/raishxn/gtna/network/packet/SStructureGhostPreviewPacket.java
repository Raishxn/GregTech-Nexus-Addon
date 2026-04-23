package com.raishxn.gtna.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import com.raishxn.gtna.client.renderer.BlockHighlightHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SStructureGhostPreviewPacket {

    private final ResourceKey<Level> dim;
    private final List<BlockPos> positions;
    private final int color;
    private final long expiryTime;
    private final boolean clear;

    public SStructureGhostPreviewPacket(ResourceKey<Level> dim, List<BlockPos> positions, int color, long expiryTime, boolean clear) {
        this.dim = dim;
        this.positions = positions;
        this.color = color;
        this.expiryTime = expiryTime;
        this.clear = clear;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceKey(dim);
        buf.writeVarInt(positions.size());
        for (BlockPos pos : positions) {
            buf.writeVarLong(pos.asLong());
        }
        buf.writeInt(color);
        buf.writeVarLong(expiryTime);
        buf.writeBoolean(clear);
    }

    public static SStructureGhostPreviewPacket decode(FriendlyByteBuf buf) {
        ResourceKey<Level> dim = buf.readResourceKey(Registries.DIMENSION);
        int size = buf.readVarInt();
        List<BlockPos> positions = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            positions.add(BlockPos.of(buf.readVarLong()));
        }
        return new SStructureGhostPreviewPacket(
                dim,
                positions,
                buf.readInt(),
                buf.readVarLong(),
                buf.readBoolean());
    }

    public static void handle(SStructureGhostPreviewPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (msg.clear) {
                BlockHighlightHandler.clearStructureGhost(msg.dim);
            } else {
                BlockHighlightHandler.highlightStructureGhost(msg.dim, msg.positions, msg.color, msg.expiryTime);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
