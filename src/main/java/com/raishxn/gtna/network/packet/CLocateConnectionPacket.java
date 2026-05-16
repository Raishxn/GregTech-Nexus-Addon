package com.raishxn.gtna.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import com.raishxn.gtna.network.GTNANetworkHandler;

import java.util.function.Supplier;

/**
 * Client → Server packet: requests that the server highlights a specific
 * block position in a specific dimension for the requesting player.
 *
 * On the server side, the handler creates an S2C {@link SStructureDetectHighlight}
 * packet and sends it back to the same player, triggering a 15-second flashing
 * overlay on the target block.
 */
public class CLocateConnectionPacket {

    private final int x;
    private final int y;
    private final int z;
    private final String dimension;

    public CLocateConnectionPacket(int x, int y, int z, String dimension) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.dimension = dimension;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(x);
        buf.writeVarInt(y);
        buf.writeVarInt(z);
        buf.writeUtf(dimension);
    }

    public static CLocateConnectionPacket decode(FriendlyByteBuf buf) {
        int x = buf.readVarInt();
        int y = buf.readVarInt();
        int z = buf.readVarInt();
        String dim = buf.readUtf(256);
        return new CLocateConnectionPacket(x, y, z, dim);
    }

    public static void handle(CLocateConnectionPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            BlockPos pos = new BlockPos(msg.x, msg.y, msg.z);
            ResourceLocation dimLoc = ResourceLocation.tryParse(msg.dimension);
            if (dimLoc == null) return;
            ResourceKey<Level> dimKey = ResourceKey.create(Registries.DIMENSION, dimLoc);

            // Highlight for 15 seconds
            long expiryTime = System.currentTimeMillis() + 15_000L;
            SStructureDetectHighlight highlight = new SStructureDetectHighlight(pos, dimKey, expiryTime);
            GTNANetworkHandler.sendToPlayer(highlight, player);
        });
        ctx.get().setPacketHandled(true);
    }
}
