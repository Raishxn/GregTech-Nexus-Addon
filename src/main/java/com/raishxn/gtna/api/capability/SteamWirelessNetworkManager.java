package com.raishxn.gtna.api.capability;

import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import com.raishxn.gtna.common.data.SteamNetworkData;
import com.raishxn.gtna.config.ConfigHolder;

import java.util.List;
import java.util.UUID;

public class SteamWirelessNetworkManager {

    /** How long (in ticks) a wireless hatch stays "connected" after its last report. */
    public static final long CONNECTION_TTL_TICKS = 40L;

    private SteamWirelessNetworkManager() {}

    /**
     * Adds steam to the owner's global network. A negative amount subtracts atomically: if the
     * balance would go below zero the operation is rejected and the balance is left untouched
     * (GTNL {@code addSteamToGlobalSteamMap} semantics), so a caller can never overdraft or void.
     */
    public static boolean addSteamToGlobalSteamMap(ServerLevel level, UUID userUuid, long steamAmount) {
        if (level == null || userUuid == null || steamAmount == 0 || !ConfigHolder.INSTANCE.wirelessSteam.enabled) {
            return false;
        }

        SteamNetworkData data = SteamNetworkData.get(level);
        return data.addSteam(userUuid, steamAmount);
    }

    public static long getUserSteam(ServerLevel level, UUID userUuid) {
        if (level == null || userUuid == null) return 0L;
        return SteamNetworkData.get(level).getSteam(userUuid);
    }

    public static int getUserSteamInt(ServerLevel level, UUID userUuid) {
        long steam = getUserSteam(level, userUuid);
        if (steam > Integer.MAX_VALUE) return Integer.MAX_VALUE;
        return (int) steam;
    }

    public static void setUserSteam(ServerLevel level, UUID userUuid, long steamAmount) {
        if (level == null || userUuid == null) return;
        SteamNetworkData.get(level).setSteam(userUuid, steamAmount);
    }

    /**
     * Consumes exactly {@code amount} from the owner's network, or nothing. The caller caps
     * {@code amount} (the wireless hatches cap it at their per-tick transfer rate); the network
     * itself never silently discards a remainder.
     */
    public static boolean consumeSteamFromGlobalMap(ServerLevel level, UUID userUuid, long amount) {
        if (level == null || userUuid == null || amount <= 0) return false;
        if (!ConfigHolder.INSTANCE.wirelessSteam.enabled) {
            return false;
        }

        SteamNetworkData data = SteamNetworkData.get(level);
        return data.consumeSteam(userUuid, amount);
    }

    public static boolean extractSteam(Level level, UUID userUuid, long amount, boolean simulate) {
        if (!(level instanceof ServerLevel serverLevel) || userUuid == null || amount <= 0) return false;
        if (!ConfigHolder.INSTANCE.wirelessSteam.enabled) {
            return false;
        }
        SteamNetworkData data = SteamNetworkData.get(serverLevel);
        long current = data.getSteam(userUuid);
        if (current >= amount) {
            if (!simulate) {
                data.setSteam(userUuid, current - amount);
            }
            return true;
        }
        return false;
    }

    // ------------------------------------------------------------------
    // Inspection support (runtime only).
    // ------------------------------------------------------------------

    /** Records a live wireless steam hatch so the inspection command can list it. */
    public static void reportConnection(ServerLevel level, UUID userUuid, GlobalPos pos, boolean isInput,
                                        boolean isSteel) {
        if (level == null || userUuid == null || pos == null) return;
        SteamNetworkData.get(level).reportConnection(userUuid, pos, isInput, isSteel, level.getGameTime());
    }

    /** The wireless steam hatches that reported for {@code userUuid} within the last TTL ticks. */
    public static List<SteamNetworkData.ConnectionInfo> getConnections(ServerLevel level, UUID userUuid) {
        if (level == null || userUuid == null) return List.of();
        return SteamNetworkData.get(level).getActiveConnections(userUuid, level.getGameTime(), CONNECTION_TTL_TICKS);
    }
}
