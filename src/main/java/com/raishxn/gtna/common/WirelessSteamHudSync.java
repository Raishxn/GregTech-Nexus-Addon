package com.raishxn.gtna.common;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.api.capability.SteamWirelessNetworkManager;
import com.raishxn.gtna.common.data.SteamNetworkData;
import com.raishxn.gtna.network.GTNANetworkHandler;
import com.raishxn.gtna.network.packet.SWirelessSteamStats;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Server side of the wireless steam HUD (GTOCore {@code WirelessEnergyHUD} parity: the client
 * overlay needs the owner's pool numbers, and the pool lives in the overworld SavedData).
 *
 * <p>
 * Once per second every online player receives a {@link SWirelessSteamStats} snapshot of their own
 * network. The lifetime flow counters in {@link SteamNetworkData} are converted to per-second
 * deltas here, so the client never has to know when the counters were reset (a server restart).
 */
@Mod.EventBusSubscriber(modid = GTNACORE.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class WirelessSteamHudSync {

    /** One snapshot per second, like GTOCore's HUD data sync. */
    public static final int SYNC_INTERVAL_TICKS = 20;

    /** Previous lifetime counters per owner, to derive the per-second deltas. Runtime only. */
    private static final Map<UUID, Sample> LAST_SAMPLE = new HashMap<>();

    private WirelessSteamHudSync() {}

    private record Sample(long added, long consumed) {}

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        MinecraftServer server = event.getServer();
        if (server == null || server.getTickCount() % SYNC_INTERVAL_TICKS != 0) return;

        ServerLevel level = server.overworld();
        Set<UUID> online = new HashSet<>();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            online.add(player.getUUID());
            GTNANetworkHandler.sendToPlayer(snapshot(level, player.getUUID()), player);
        }
        // Drop the samples of players that logged off so a later login starts at delta 0 instead
        // of reporting everything produced while they were away.
        LAST_SAMPLE.keySet().retainAll(online);
    }

    /**
     * Builds the HUD snapshot for {@code owner} and advances the per-owner sample cursor, turning
     * the lifetime counters into the flow of the last {@link #SYNC_INTERVAL_TICKS} ticks.
     */
    public static SWirelessSteamStats snapshot(ServerLevel level, UUID owner) {
        SteamNetworkData data = SteamNetworkData.get(level);
        SteamNetworkData.FlowStats flow = data.getFlowStats(owner);
        Sample previous = LAST_SAMPLE.put(owner, new Sample(flow.added, flow.consumed));
        long added = previous == null ? 0L : Math.max(0L, flow.added - previous.added());
        long consumed = previous == null ? 0L : Math.max(0L, flow.consumed - previous.consumed());

        int inputs = 0;
        int outputs = 0;
        for (SteamNetworkData.ConnectionInfo connection : data.getActiveConnections(owner, level.getGameTime(),
                SteamWirelessNetworkManager.CONNECTION_TTL_TICKS)) {
            if (connection.isInput) {
                inputs++;
            } else {
                outputs++;
            }
        }
        return new SWirelessSteamStats(data.getSteam(owner), added, consumed, inputs, outputs);
    }
}
