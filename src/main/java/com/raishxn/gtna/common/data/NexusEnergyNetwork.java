package com.raishxn.gtna.common.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import com.raishxn.gtna.utils.datastructure.Int128;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NexusEnergyNetwork extends SavedData {

    private static final String DATA_NAME = "gtna_nexus_energy_network";
    private final Map<UUID, NetworkState> energyStorage = new HashMap<>();

    public static class ConnectionInfo {

        public net.minecraft.core.GlobalPos pos;
        public boolean isInput;
        public int tier;
        public int amperage;
        public String machineType;
        public Int128 euTransferred = Int128.ZERO();
        public Int128 lastTickEuTransferred = Int128.ZERO();
        public long lastUpdateTick;
        public long currentTick;
    }

    public static class NetworkState {

        public Int128 energy = Int128.ZERO();
        public Int128 maxCapacity = Int128.ZERO();
        public boolean safeMode = false;
        public long lastAlertTime = 0; // Para cooldown de chat

        public Int128 inputPerTick = Int128.ZERO();
        public Int128 outputPerTick = Int128.ZERO();
        public Int128 lastInputPerTick = Int128.ZERO();
        public Int128 lastOutputPerTick = Int128.ZERO();
        public long lastTickTime = 0;

        public Map<net.minecraft.core.GlobalPos, ConnectionInfo> connections = new java.util.concurrent.ConcurrentHashMap<>();

        // Matrix structural stats (pushed by controller)
        public long totalCapacitors = 0;
        public int averageTier = 0;
        public double efficiency = 0.0;
        public Int128 transferLimit = Int128.ZERO();
        public boolean matrixFormed = false;
    }

    public NexusEnergyNetwork() {}

    public NexusEnergyNetwork(CompoundTag tag) {
        if (tag.contains("EnergyNetworks", Tag.TAG_LIST)) {
            ListTag list = tag.getList("EnergyNetworks", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag entry = list.getCompound(i);
                if (entry.hasUUID("Owner")) {
                    NetworkState state = new NetworkState();
                    state.energy = Int128.fromString(entry.getString("Amount"), Int128.ZERO());
                    state.maxCapacity = Int128.fromString(entry.getString("MaxCapacity"), Int128.ZERO());
                    state.safeMode = entry.getBoolean("SafeMode");
                    state.totalCapacitors = entry.getLong("TotalCapacitors");
                    state.averageTier = entry.getInt("AvgTier");
                    state.efficiency = entry.getDouble("Efficiency");
                    state.transferLimit = Int128.fromString(entry.getString("TransferLimit"), Int128.ZERO());
                    state.matrixFormed = entry.getBoolean("MatrixFormed");
                    energyStorage.put(entry.getUUID("Owner"), state);
                }
            }
        }
    }

    public static NexusEnergyNetwork get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage()
                .computeIfAbsent(NexusEnergyNetwork::new, NexusEnergyNetwork::new, DATA_NAME);
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        ListTag list = new ListTag();
        energyStorage.forEach((uuid, state) -> {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("Owner", uuid);
            entry.putString("Amount", state.energy.toString());
            entry.putString("MaxCapacity", state.maxCapacity.toString());
            entry.putBoolean("SafeMode", state.safeMode);
            entry.putLong("TotalCapacitors", state.totalCapacitors);
            entry.putInt("AvgTier", state.averageTier);
            entry.putDouble("Efficiency", state.efficiency);
            entry.putString("TransferLimit", state.transferLimit.toString());
            entry.putBoolean("MatrixFormed", state.matrixFormed);
            list.add(entry);
        });
        tag.put("EnergyNetworks", list);
        return tag;
    }

    private NetworkState getState(UUID owner) {
        return energyStorage.computeIfAbsent(owner, k -> new NetworkState());
    }

    private void handleTick(NetworkState state, long currentTime) {
        if (currentTime > state.lastTickTime) {
            if (currentTime == state.lastTickTime + 1) {
                state.lastInputPerTick.set(state.inputPerTick);
                state.lastOutputPerTick.set(state.outputPerTick);
            } else {
                state.lastInputPerTick.set(0L, 0L);
                state.lastOutputPerTick.set(0L, 0L);
            }
            state.inputPerTick.set(0L, 0L);
            state.outputPerTick.set(0L, 0L);
            state.lastTickTime = currentTime;

            // Cleanup stale connections (older than 100 ticks)
            state.connections.values().removeIf(c -> currentTime - c.lastUpdateTick > 100);
        }
    }

    public void reportConnection(UUID owner, net.minecraft.core.GlobalPos pos, boolean isInput, int tier, int amperage,
                                 String machineType, Int128 amountTransferred, ServerLevel level) {
        if (owner == null || pos == null) return;
        NetworkState state = getState(owner);
        long currentTime = level.getGameTime();

        handleTick(state, currentTime);

        ConnectionInfo info = state.connections.computeIfAbsent(pos, k -> new ConnectionInfo());
        info.pos = pos;
        info.isInput = isInput;
        info.tier = tier;
        info.amperage = amperage;
        info.machineType = machineType;
        info.lastUpdateTick = currentTime;

        if (currentTime > info.currentTick) {
            info.lastTickEuTransferred.set(info.euTransferred);
            info.euTransferred.set(0L, 0L);
            info.currentTick = currentTime;
        }

        if (amountTransferred != null && !amountTransferred.isZero()) {
            info.euTransferred.add(amountTransferred);
        }
        // Dirty flag not strictly necessary just for ephemeral stats, but let's be safe
        setDirty();
    }

    public Int128 getEnergy(UUID owner) {
        return getState(owner).energy.copy();
    }

    public Int128 getLastInputPerTick(UUID owner) {
        return getState(owner).lastInputPerTick.copy();
    }

    public Int128 getLastOutputPerTick(UUID owner) {
        return getState(owner).lastOutputPerTick.copy();
    }

    public boolean getSafeMode(UUID owner) {
        return getState(owner).safeMode;
    }

    public Map<net.minecraft.core.GlobalPos, ConnectionInfo> getConnections(UUID owner) {
        return getState(owner).connections;
    }

    // Matrix structural stats
    public void setMatrixStats(UUID owner, long totalCapacitors, int averageTier, double efficiency,
                               Int128 transferLimit, boolean matrixFormed) {
        NetworkState state = getState(owner);
        state.totalCapacitors = totalCapacitors;
        state.averageTier = averageTier;
        state.efficiency = efficiency;
        state.transferLimit = transferLimit.copy();
        state.matrixFormed = matrixFormed;
        setDirty();
    }

    public long getTotalCapacitors(UUID owner) {
        return getState(owner).totalCapacitors;
    }

    public int getAverageTier(UUID owner) {
        return getState(owner).averageTier;
    }

    public double getEfficiency(UUID owner) {
        return getState(owner).efficiency;
    }

    public Int128 getTransferLimit(UUID owner) {
        return getState(owner).transferLimit.copy();
    }

    public boolean isMatrixFormed(UUID owner) {
        return getState(owner).matrixFormed;
    }

    public void setMaxCapacity(UUID owner, Int128 maxCapacity) {
        NetworkState state = getState(owner);
        state.maxCapacity = maxCapacity.copy();
        setDirty();
    }

    public Int128 getMaxCapacity(UUID owner) {
        return getState(owner).maxCapacity.copy();
    }

    public Int128 addEnergy(UUID owner, Int128 amount, ServerLevel level) {
        if (amount.isZero() || amount.isNegative()) return Int128.ZERO();

        NetworkState state = getState(owner);
        handleTick(state, level.getGameTime());

        // Calculate how much space is left
        Int128 actualAccepted;
        if (!state.maxCapacity.isZero()) {
            Int128 space = state.maxCapacity.copy();
            space.subtract(state.energy);
            if (space.isZero() || space.isNegative()) {
                // Network is full — don't accept any energy
                return Int128.ZERO();
            }
            // Accept only as much as fits
            if (amount.compareTo(space) > 0) {
                actualAccepted = space;
            } else {
                actualAccepted = amount.copy();
            }
        } else {
            // No capacity limit set — accept everything
            actualAccepted = amount.copy();
        }

        state.energy.add(actualAccepted);
        state.inputPerTick.add(actualAccepted);

        checkSafeMode(owner, state, level);
        setDirty();
        return actualAccepted;
    }

    public void setEnergy(UUID owner, Int128 amount) {
        NetworkState state = getState(owner);
        state.energy = amount.copy();
        setDirty();
    }

    public boolean consumeEnergy(UUID owner, Int128 amount, ServerLevel level) {
        if (amount.isZero() || amount.isNegative()) return false;

        NetworkState state = getState(owner);
        handleTick(state, level.getGameTime());

        if (state.safeMode) {
            return false; // Safe mode blocks outputs
        }

        if (state.energy.compareTo(amount) >= 0) {
            state.energy.subtract(amount);
            state.outputPerTick.add(amount);
            checkSafeMode(owner, state, level);
            setDirty();
            return true;
        }
        return false;
    }

    private void checkSafeMode(UUID owner, NetworkState state, ServerLevel level) {
        if (state.maxCapacity.isZero()) return;

        double currentRatio;
        if (state.maxCapacity.compareTo(Int128.fromBigInteger(java.math.BigInteger.valueOf(1000000000L))) < 0) {
            currentRatio = (double) state.energy.toLong() / (double) state.maxCapacity.toLong();
        } else {
            currentRatio = state.energy.toBigInteger().doubleValue() / state.maxCapacity.toBigInteger().doubleValue();
        }

        double percentage = currentRatio * 100.0;

        if (!state.safeMode && percentage <= 10.0) {
            state.safeMode = true;
            net.minecraft.server.level.ServerPlayer alertPlayer = level.getServer().getPlayerList().getPlayer(owner);
            if (alertPlayer != null) {
                alertPlayer.displayClientMessage(
                        net.minecraft.network.chat.Component.literal("⛔ CRITICAL: Energy < 10%. Entering Safe Mode.")
                                .withStyle(net.minecraft.ChatFormatting.DARK_RED, net.minecraft.ChatFormatting.BOLD),
                        false);
            }
        } else if (state.safeMode && percentage >= 25.0) {
            state.safeMode = false;
            net.minecraft.server.level.ServerPlayer alertPlayer = level.getServer().getPlayerList().getPlayer(owner);
            if (alertPlayer != null) {
                alertPlayer.displayClientMessage(
                        net.minecraft.network.chat.Component.literal("🔋 Power restored. Safe Mode deactivated.")
                                .withStyle(net.minecraft.ChatFormatting.GREEN),
                        false);
            }
        }
    }
}
