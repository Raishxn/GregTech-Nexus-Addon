package com.raishxn.gtna.common.data;

import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

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
        public Int128 inputPerTick = Int128.ZERO();
        public Int128 rawInputPerTick = Int128.ZERO();
        public Int128 lossPerTick = Int128.ZERO();
        public Int128 outputPerTick = Int128.ZERO();
        public Int128 lastInputPerTick = Int128.ZERO();
        public Int128 lastRawInputPerTick = Int128.ZERO();
        public Int128 lastLossPerTick = Int128.ZERO();
        public Int128 lastOutputPerTick = Int128.ZERO();
        public long lastTickTime = 0;

        public Map<GlobalPos, ConnectionInfo> connections = new ConcurrentHashMap<>();

        public long totalCapacitors = 0;
        public int averageTier = 0;
        public double efficiency = 0.0;
        public Int128 transferLimit = Int128.ZERO();
        public boolean matrixFormed = false;
        public String matrixDimension = "";
        public int lossRemainder;
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
            state.totalCapacitors = entry.getLong("TotalCapacitors");
            state.averageTier = entry.getInt("AvgTier");
            state.efficiency = entry.getDouble("Efficiency");
            state.transferLimit = Int128.fromString(entry.getString("TransferLimit"), Int128.ZERO());
            state.matrixFormed = entry.getBoolean("MatrixFormed");
            state.matrixDimension = entry.getString("MatrixDimension");
            state.lossRemainder = Math.max(0, Math.min(9_999, entry.getInt("LossRemainder")));
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
            entry.putLong("TotalCapacitors", state.totalCapacitors);
            entry.putInt("AvgTier", state.averageTier);
            entry.putDouble("Efficiency", state.efficiency);
            entry.putString("TransferLimit", state.transferLimit.toString());
            entry.putBoolean("MatrixFormed", state.matrixFormed);
            entry.putString("MatrixDimension", state.matrixDimension);
            entry.putInt("LossRemainder", state.lossRemainder);
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
            state.lastRawInputPerTick.set(state.rawInputPerTick);
            state.lastLossPerTick.set(state.lossPerTick);
            state.lastOutputPerTick.set(state.outputPerTick);
        } else {
            state.lastInputPerTick.set(0L, 0L);
            state.lastRawInputPerTick.set(0L, 0L);
            state.lastLossPerTick.set(0L, 0L);
            state.lastOutputPerTick.set(0L, 0L);
        }

        state.inputPerTick.set(0L, 0L);
        state.rawInputPerTick.set(0L, 0L);
        state.lossPerTick.set(0L, 0L);
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

    public Int128 getLastRawInputPerTick(UUID owner) {
        return getState(owner).lastRawInputPerTick.copy();
    }

    public Int128 getCurrentRawInputPerTick(UUID owner) {
        return getState(owner).rawInputPerTick.copy();
    }

    public Int128 getLastLossPerTick(UUID owner) {
        return getState(owner).lastLossPerTick.copy();
    }

    public Int128 getCurrentLossPerTick(UUID owner) {
        return getState(owner).lossPerTick.copy();
    }

    public Int128 getLastOutputPerTick(UUID owner) {
        return getState(owner).lastOutputPerTick.copy();
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

    public void setMatrixDimension(UUID owner, ResourceKey<Level> dimension) {
        getState(owner).matrixDimension = dimension == null ? "" : dimension.location().toString();
        setDirty();
    }

    public boolean canTransfer(UUID owner, ResourceKey<Level> dimension) {
        NetworkState state = getState(owner);
        if (state.matrixDimension.isBlank()) {
            return !"MATRIX_INPUT_ONCE".equals(GTNABalance.getNexusLossApplication());
        }
        return state.matrixDimension.equals(dimension.location().toString()) ||
                GTNABalance.isNexusCrossDimensionEnabled(state.averageTier);
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
        if (!canTransfer(owner, level.dimension())) return Int128.ZERO();

        NetworkState state = getState(owner);
        handleTick(state, level.getGameTime());

        String policy = GTNABalance.getNexusLossApplication();
        if ("MATRIX_INPUT_ONCE".equals(policy) && !state.matrixFormed) return Int128.ZERO();
        BigInteger maxEnergy = state.maxCapacity.isZero() ? Int128.MAX_VALUE.toBigInteger() :
                state.maxCapacity.toBigInteger();
        BigInteger space = maxEnergy.subtract(state.energy.toBigInteger());
        if (space.signum() <= 0) return Int128.ZERO();
        int basisPoints = "MATRIX_INPUT_ONCE".equals(policy) ?
                GTNABalance.getNexusLossBasisPoints(state.averageTier) : 0;
        NexusWirelessLoss.Transfer transfer = NexusWirelessLoss.accept(amount.toBigInteger(), space,
                basisPoints, state.lossRemainder);
        if (transfer.gross().signum() <= 0) return Int128.ZERO();
        Int128 gross = Int128.fromBigInteger(transfer.gross());
        Int128 credited = Int128.fromBigInteger(transfer.credited());
        state.energy.add(credited);
        state.inputPerTick.add(credited);
        state.rawInputPerTick.add(gross);
        state.lossPerTick.add(Int128.fromBigInteger(transfer.lost()));
        state.lossRemainder = transfer.remainder();

        setDirty();
        // Callers must drain the gross amount from their source; the network stores credited EU.
        return gross;
    }

    /** Adds a source-side loss already applied by the legacy Generator Array path to the UI totals. */
    public void recordLegacySourceLoss(UUID owner, Int128 lost, ServerLevel level) {
        if (lost.isZero() || lost.isNegative()) return;
        NetworkState state = getState(owner);
        handleTick(state, level.getGameTime());
        state.rawInputPerTick.add(lost);
        state.lossPerTick.add(lost);
        setDirty();
    }

    public void setEnergy(UUID owner, Int128 amount) {
        NetworkState state = getState(owner);
        state.energy = amount.copy();
        setDirty();
    }

    public boolean consumeEnergy(UUID owner, Int128 amount, ServerLevel level) {
        if (amount.isZero() || amount.isNegative()) return false;
        if (!canTransfer(owner, level.dimension())) return false;

        NetworkState state = getState(owner);
        if ("MATRIX_INPUT_ONCE".equals(GTNABalance.getNexusLossApplication()) && !state.matrixFormed) return false;
        handleTick(state, level.getGameTime());

        if (state.energy.compareTo(amount) < 0) return false;

        state.energy.subtract(amount);
        state.outputPerTick.add(amount);
        setDirty();
        return true;
    }
}
