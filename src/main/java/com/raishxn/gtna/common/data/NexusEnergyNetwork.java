package com.raishxn.gtna.common.data;

import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;

import com.raishxn.gtna.config.ConfigHolder;
import com.raishxn.gtna.config.GTNABalance;
import com.raishxn.gtna.utils.datastructure.Int128;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class NexusEnergyNetwork extends SavedData {

    private static final String DATA_NAME = "gtna_nexus_energy_network";
    private final Map<UUID, NetworkState> energyStorage = new HashMap<>();

    public static class ConnectionInfo {

        public GlobalPos pos;
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
        public long lastAlertTime = 0;

        public Int128 inputPerTick = Int128.ZERO();
        public Int128 outputPerTick = Int128.ZERO();
        public Int128 lastInputPerTick = Int128.ZERO();
        public Int128 lastOutputPerTick = Int128.ZERO();
        public long lastTickTime = 0;

        public Map<GlobalPos, ConnectionInfo> connections = new ConcurrentHashMap<>();

        public long totalCapacitors = 0;
        public int averageTier = 0;
        public double efficiency = 0.0;
        public Int128 transferLimit = Int128.ZERO();
        public boolean matrixFormed = false;
    }

    public NexusEnergyNetwork() {}

    public NexusEnergyNetwork(CompoundTag tag) {
        if (!tag.contains("EnergyNetworks", Tag.TAG_LIST)) return;

        ListTag list = tag.getList("EnergyNetworks", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            if (!entry.hasUUID("Owner")) continue;

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
        return energyStorage.computeIfAbsent(owner, ignored -> new NetworkState());
    }

    private void handleTick(NetworkState state, long currentTime) {
        if (currentTime <= state.lastTickTime) return;

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
        state.connections.values().removeIf(connection -> currentTime - connection.lastUpdateTick > 100);
    }

    public void reportConnection(UUID owner, GlobalPos pos, boolean isInput, int tier, int amperage,
                                 String machineType, Int128 amountTransferred, ServerLevel level) {
        if (owner == null || pos == null) return;

        NetworkState state = getState(owner);
        long currentTime = level.getGameTime();
        handleTick(state, currentTime);

        ConnectionInfo info = state.connections.computeIfAbsent(pos, ignored -> new ConnectionInfo());
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

    public Map<GlobalPos, ConnectionInfo> getConnections(UUID owner) {
        return getState(owner).connections;
    }

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

        Int128 accepted;
        if (!state.maxCapacity.isZero()) {
            Int128 space = state.maxCapacity.copy();
            space.subtract(state.energy);
            if (space.isZero() || space.isNegative()) return Int128.ZERO();
            accepted = amount.compareTo(space) > 0 ? space : amount.copy();
        } else {
            accepted = amount.copy();
        }

        state.energy.add(accepted);
        state.inputPerTick.add(accepted);

        checkSafeMode(owner, state, level);
        setDirty();
        return accepted;
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

        if (state.safeMode) return false;
        if (state.energy.compareTo(amount) < 0) return false;

        state.energy.subtract(amount);
        state.outputPerTick.add(amount);
        checkSafeMode(owner, state, level);
        setDirty();
        return true;
    }

    private void checkSafeMode(UUID owner, NetworkState state, ServerLevel level) {
        if (state.maxCapacity.isZero()) return;

        var cfg = ConfigHolder.INSTANCE.machines.nexusFluxMatrix;
        if (!GTNABalance.getNexusFluxMatrix().safeMode.enabled) {
            if (state.safeMode) {
                state.safeMode = false;
                setDirty();
            }
            return;
        }

        double currentRatio;
        if (state.maxCapacity.compareTo(Int128.fromBigInteger(BigInteger.valueOf(1_000_000_000L))) < 0) {
            currentRatio = (double) state.energy.toLong() / (double) state.maxCapacity.toLong();
        } else {
            currentRatio = state.energy.toBigInteger().doubleValue() / state.maxCapacity.toBigInteger().doubleValue();
        }

        double percentage = currentRatio * 100.0;
        long currentGameTime = level.getGameTime();

        if (!state.safeMode && percentage <= cfg.safeModeThreshold) {
            state.safeMode = true;
            maybeAlertOwner(owner, state, level, currentGameTime,
                    "CRITICAL: Energy < " + cfg.safeModeThreshold + "%. Entering Safe Mode.",
                    net.minecraft.ChatFormatting.DARK_RED,
                    net.minecraft.ChatFormatting.BOLD);
        } else if (state.safeMode && percentage >= cfg.safeModeRecovery) {
            state.safeMode = false;
            maybeAlertOwner(owner, state, level, currentGameTime,
                    "Power restored. Safe Mode deactivated.",
                    net.minecraft.ChatFormatting.GREEN);
        }
    }

    private void maybeAlertOwner(UUID owner, NetworkState state, ServerLevel level, long currentGameTime,
                                 String message, net.minecraft.ChatFormatting... formatting) {
        int cooldown = Math.max(0, ConfigHolder.INSTANCE.machines.nexusFluxMatrix.alertCooldownTicks);
        if (currentGameTime - state.lastAlertTime < cooldown) return;

        state.lastAlertTime = currentGameTime;
        ServerPlayer alertPlayer = level.getServer().getPlayerList().getPlayer(owner);
        if (alertPlayer != null) {
            alertPlayer.displayClientMessage(net.minecraft.network.chat.Component.literal(message)
                    .withStyle(formatting), false);
        }
    }
}
