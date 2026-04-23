package com.raishxn.gtna.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.network.packet.CLocateConnectionPacket;
import com.raishxn.gtna.network.packet.SRegionHighlightPacket;
import com.raishxn.gtna.network.packet.SStructureDetectHighlight;
import com.raishxn.gtna.network.packet.SStructureGhostPreviewPacket;

public class GTNANetworkHandler {

    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(GTNACORE.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    /**
     * Convenience alias so call sites can use {@code GTNANetworkHandler.INSTANCE.sendToServer(...)}.
     */
    public static final GTNANetworkHandler INSTANCE = new GTNANetworkHandler();

    private static int packetId = 0;

    public static void init() {
        // S2C – Server highlights a block on the client
        CHANNEL.messageBuilder(SStructureDetectHighlight.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SStructureDetectHighlight::encode)
                .decoder(SStructureDetectHighlight::decode)
                .consumerMainThread(SStructureDetectHighlight::handle)
                .add();

        CHANNEL.messageBuilder(SRegionHighlightPacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SRegionHighlightPacket::encode)
                .decoder(SRegionHighlightPacket::decode)
                .consumerMainThread(SRegionHighlightPacket::handle)
                .add();

        CHANNEL.messageBuilder(SStructureGhostPreviewPacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SStructureGhostPreviewPacket::encode)
                .decoder(SStructureGhostPreviewPacket::decode)
                .consumerMainThread(SStructureGhostPreviewPacket::handle)
                .add();

        // C2S – Client requests a locate highlight
        CHANNEL.messageBuilder(CLocateConnectionPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(CLocateConnectionPacket::encode)
                .decoder(CLocateConnectionPacket::decode)
                .consumerMainThread(CLocateConnectionPacket::handle)
                .add();
    }

    // ── Instance methods (used via INSTANCE) ──

    /**
     * Send a packet from the client to the server (C2S).
     */
    public void sendToServer(Object msg) {
        CHANNEL.sendToServer(msg);
    }

    /**
     * Send a packet from the server to a specific player (S2C).
     * Instance-method variant so callers can use {@code INSTANCE.sendTo(msg, player)}.
     */
    public void sendTo(Object msg, ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), msg);
    }

    // ── Static convenience methods ──

    /**
     * Send a packet from the server to a specific player (S2C).
     */
    public static void sendToPlayer(Object msg, ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), msg);
    }

    public static void sendToAll(Object msg) {
        CHANNEL.send(PacketDistributor.ALL.noArg(), msg);
    }
}
