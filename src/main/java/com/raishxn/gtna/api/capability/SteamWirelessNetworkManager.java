package com.raishxn.gtna.api.capability;

import com.raishxn.gtna.common.data.SteamNetworkData;
import net.minecraft.server.level.ServerLevel;

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

    // --- MÉTODO ADICIONADO AQUI ---
    /**
     * Tenta consumir uma quantidade de vapor da rede global do jogador.
     * @return true se o consumo foi bem-sucedido (havia vapor suficiente), false caso contrário.
     */
    public static boolean consumeSteamFromGlobalMap(ServerLevel level, UUID userUuid, long amount) {
        if (level == null || userUuid == null || amount <= 0) return false;

        // Obtém os dados salvos do mundo
        SteamNetworkData data = SteamNetworkData.get(level);

        // Chama o método de consumo na classe de dados (SteamNetworkData)
        return data.consumeSteam(userUuid, amount);
    }
}