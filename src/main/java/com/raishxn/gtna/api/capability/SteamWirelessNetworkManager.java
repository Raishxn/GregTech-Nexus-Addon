package com.raishxn.gtna.api.capability;

import com.raishxn.gtna.common.data.SteamNetworkData;
import net.minecraft.server.level.ServerLevel;
import com.raishxn.gtna.common.data.SteamNetworkData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class SteamWirelessNetworkManager {

    private SteamWirelessNetworkManager() {}

    public static boolean addSteamToGlobalSteamMap(ServerLevel level, UUID userUuid, long steamAmount) {
        if (level == null || userUuid == null || steamAmount <= 0) return false;

        SteamNetworkData data = SteamNetworkData.get(level);
        data.addSteam(userUuid, steamAmount);
        return true;
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

    public static boolean consumeSteamFromGlobalMap(ServerLevel level, UUID userUuid, long amount) {
        if (level == null || userUuid == null || amount <= 0) return false;
        SteamNetworkData data = SteamNetworkData.get(level);
        return data.consumeSteam(userUuid, amount);
    }
    public static boolean extractSteam(Level level, UUID userUuid, long amount, boolean simulate) {
        if (!(level instanceof ServerLevel serverLevel) || userUuid == null || amount <= 0) return false;
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
}