package com.raishxn.gtna.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import com.raishxn.gtna.client.renderer.BlockHighlightHandler;

import java.util.function.Supplier;

/**
 * Server → Client packet: highlights a block in the world for 15 seconds.
 * Used by the locate feature in controller UI and quantum terminal.
 */
public class SStructureDetectHighlight {

    private final BlockPos pos;
    private final ResourceKey<Level> dim;
    private final long time;

    public SStructureDetectHighlight(BlockPos pos, ResourceKey<Level> dim, long time) {
        this.pos = pos;
        this.dim = dim;
        this.time = time;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarLong(this.pos.asLong());
        buf.writeResourceKey(this.dim);
        buf.writeVarLong(this.time);
    }

    public static SStructureDetectHighlight decode(FriendlyByteBuf buf) {
        BlockPos pos = BlockPos.of(buf.readVarLong());
        ResourceKey<Level> dim = buf.readResourceKey(Registries.DIMENSION);
        long time = buf.readVarLong();
        return new SStructureDetectHighlight(pos, dim, time);
    }

    public static void handle(SStructureDetectHighlight msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            BlockHighlightHandler.highlight(msg.pos, msg.dim, msg.time);
        });
        ctx.get().setPacketHandled(true);
    }
}
