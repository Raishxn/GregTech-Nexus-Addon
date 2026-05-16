package com.raishxn.gtna.api.capability;

import net.minecraft.server.level.ServerLevel;

import com.raishxn.gtna.common.data.NexusEnergyNetwork;
import com.raishxn.gtna.utils.datastructure.Int128;

import java.util.UUID;

public class WirelessEnergyManager {

    private WirelessEnergyManager() {}

    public static Int128 addEnergy(ServerLevel level, UUID userUuid, Int128 amount) {
        if (level == null || userUuid == null || amount == null || amount.isZero() || amount.isNegative())
            return Int128.ZERO();

        NexusEnergyNetwork data = NexusEnergyNetwork.get(level);
        return data.addEnergy(userUuid, amount, level);
    }

    public static Int128 getEnergy(ServerLevel level, UUID userUuid) {
        if (level == null || userUuid == null) return Int128.ZERO();
        return NexusEnergyNetwork.get(level).getEnergy(userUuid);
    }

    public static void setEnergy(ServerLevel level, UUID userUuid, Int128 amount) {
        if (level == null || userUuid == null || amount == null) return;
        NexusEnergyNetwork.get(level).setEnergy(userUuid, amount);
    }

    /**
     * Consume energy from the network, capped to the transfer limit.
     * Returns the amount actually consumed (ZERO means failure / nothing consumed).
     * Callers must credit their local buffer with the returned value, not the
     * requested amount, to prevent EU creation.
     */
    public static Int128 consumeEnergy(ServerLevel level, UUID userUuid, Int128 amount) {
        if (level == null || userUuid == null || amount == null || amount.isZero() || amount.isNegative())
            return Int128.ZERO();

        NexusEnergyNetwork data = NexusEnergyNetwork.get(level);
        return data.consumeEnergy(userUuid, amount, level);
    }

    /**
     * Consume energy without the transfer-limit cap.
     * Use only for machines with intentionally large one-time energy costs.
     */
    public static boolean consumeEnergyUnlimited(ServerLevel level, UUID userUuid, Int128 amount) {
        if (level == null || userUuid == null || amount == null || amount.isZero() || amount.isNegative())
            return false;

        NexusEnergyNetwork data = NexusEnergyNetwork.get(level);
        return data.consumeEnergyUnlimited(userUuid, amount, level);
    }

    public static Int128 getMaxCapacity(ServerLevel level, UUID userUuid) {
        if (level == null || userUuid == null) return Int128.ZERO();
        return NexusEnergyNetwork.get(level).getMaxCapacity(userUuid);
    }

    public static Int128 getTransferLimit(ServerLevel level, UUID userUuid) {
        if (level == null || userUuid == null) return Int128.ZERO();
        return NexusEnergyNetwork.get(level).getTransferLimit(userUuid);
    }

    public static void reportConnection(ServerLevel level, UUID userUuid, net.minecraft.core.GlobalPos pos,
                                        boolean isInput, int tier, int amperage, String machineType,
                                        Int128 amountTransferred) {
        if (level == null || userUuid == null || pos == null) return;
        NexusEnergyNetwork data = NexusEnergyNetwork.get(level);
        data.reportConnection(userUuid, pos, isInput, tier, amperage, machineType, amountTransferred, level);
    }
}
