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

    public static boolean consumeEnergy(ServerLevel level, UUID userUuid, Int128 amount) {
        if (level == null || userUuid == null || amount == null || amount.isZero() || amount.isNegative()) return false;

        NexusEnergyNetwork data = NexusEnergyNetwork.get(level);
        return data.consumeEnergy(userUuid, amount, level);
    }

    public static void addMaxCapacity(ServerLevel level, UUID userUuid, Int128 capacity) {
        if (level == null || userUuid == null || capacity == null || capacity.isZero()) return;
        NexusEnergyNetwork data = NexusEnergyNetwork.get(level);
        Int128 current = data.getMaxCapacity(userUuid);
        current.add(capacity);
        data.setMaxCapacity(userUuid, current);
    }

    public static void removeMaxCapacity(ServerLevel level, UUID userUuid, Int128 capacity) {
        if (level == null || userUuid == null || capacity == null || capacity.isZero()) return;
        NexusEnergyNetwork data = NexusEnergyNetwork.get(level);
        Int128 current = data.getMaxCapacity(userUuid);
        current.subtract(capacity);
        if (current.isNegative()) current = Int128.ZERO();
        data.setMaxCapacity(userUuid, current);
    }

    public static Int128 getMaxCapacity(ServerLevel level, UUID userUuid) {
        if (level == null || userUuid == null) return Int128.ZERO();
        return NexusEnergyNetwork.get(level).getMaxCapacity(userUuid);
    }

    public static void reportConnection(ServerLevel level, UUID userUuid, net.minecraft.core.GlobalPos pos,
                                        boolean isInput, int tier, int amperage, String machineType,
                                        Int128 amountTransferred) {
        if (level == null || userUuid == null || pos == null) return;
        NexusEnergyNetwork data = NexusEnergyNetwork.get(level);
        data.reportConnection(userUuid, pos, isInput, tier, amperage, machineType, amountTransferred, level);
    }
}
