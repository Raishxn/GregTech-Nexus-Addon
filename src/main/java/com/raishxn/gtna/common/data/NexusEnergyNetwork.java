package com.raishxn.gtna.common.data;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
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

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.ArrayList;
import java.util.List;

public class NexusEnergyNetwork extends SavedData {

    private static final String DATA_NAME = "gtna_nexus_energy_network";
    private static final Logger LOGGER = LogManager.getLogger(NexusEnergyNetwork.class);
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

    /**
     * Immutable (after construction) structural snapshot of one NexusFluxMatrix controller.
     * Multiple MatrixRecords may exist per network owner when several matrices share the
     * same Network ID. Aggregate stats (capacity, tier, efficiency, TL) are computed
     * from the full set by {@code recomputeAggregates}.
     */
    public static class MatrixRecord {

        public long totalCapacitors;
        public int averageTier;
        public double efficiency;
        public Int128 transferLimit;
        public Int128 maxCapacity;

        public MatrixRecord() {
            this.transferLimit = Int128.ZERO();
            this.maxCapacity   = Int128.ZERO();
        }

        public MatrixRecord(long totalCapacitors, int averageTier, double efficiency,
                            Int128 transferLimit, Int128 maxCapacity) {
            this.totalCapacitors = totalCapacitors;
            this.averageTier     = averageTier;
            this.efficiency      = efficiency;
            this.transferLimit   = transferLimit.copy();
            this.maxCapacity     = maxCapacity.copy();
        }
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
        // Per-controller structural registry — key is GlobalPos of the NFM controller.
        public Map<GlobalPos, MatrixRecord> matrices = new HashMap<>();

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

            // Load per-matrix registry (new format)
            if (entry.contains("Matrices", Tag.TAG_LIST)) {
                ListTag mList = entry.getList("Matrices", Tag.TAG_COMPOUND);
                for (int j = 0; j < mList.size(); j++) {
                    CompoundTag mTag = mList.getCompound(j);
                    try {
                        ResourceLocation dimRl = ResourceLocation.tryParse(mTag.getString("Dimension"));
                        if (dimRl == null) continue; // skip malformed dimension entries
                        ResourceKey<Level> dimKey = ResourceKey.create(Registries.DIMENSION, dimRl);
                        BlockPos bpos = BlockPos.of(mTag.getLong("BlockPos"));
                        GlobalPos gpos = GlobalPos.of(dimKey, bpos);
                        MatrixRecord rec = new MatrixRecord();
                        rec.totalCapacitors = mTag.getLong("Capacitors");
                        rec.averageTier     = mTag.getInt("Tier");
                        rec.efficiency      = mTag.getDouble("Efficiency");
                        rec.transferLimit   = Int128.fromString(mTag.getString("TransferLimit"), Int128.ZERO());
                        rec.maxCapacity     = Int128.fromString(mTag.getString("MaxCapacity"), Int128.ZERO());
                        state.matrices.put(gpos, rec);
                    } catch (Exception ignored) {}
                }
                // Only recompute when records were actually loaded.  An empty "Matrices"
                // list (written during the window between world-load and controller
                // re-registration) must NOT trigger recomputeAggregates because that
                // would zero out flat fields that are still valid from the outer load.
                if (!state.matrices.isEmpty()) {
                    recomputeAggregates(state);
                }
            }
            // Legacy saves (no "Matrices" key): flat fields (totalCapacitors, averageTier,
            // etc.) were already loaded above and remain valid for the session.  matrices
            // is left empty — real controllers call registerMatrix on onStructureFormed
            // (within the same tick as world load) which re-populates the registry and
            // triggers recomputeAggregates.  No fake placeholder is inserted because
            // it could never be removed by unregisterMatrix(realControllerPos).

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

            // Serialise per-matrix registry
            ListTag matrixList = new ListTag();
            state.matrices.forEach((gpos, rec) -> {
                CompoundTag mTag = new CompoundTag();
                mTag.putString("Dimension", gpos.dimension().location().toString());
                mTag.putLong("BlockPos", gpos.pos().asLong());
                mTag.putLong("Capacitors", rec.totalCapacitors);
                mTag.putInt("Tier", rec.averageTier);
                mTag.putDouble("Efficiency", rec.efficiency);
                mTag.putString("TransferLimit", rec.transferLimit.toString());
                mTag.putString("MaxCapacity", rec.maxCapacity.toString());
                matrixList.add(mTag);
            });
            entry.put("Matrices", matrixList);

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

    /**
     * Register (or update) a single NexusFluxMatrix controller's structural stats
     * in the per-owner matrix registry.  The aggregate network stats (capacity,
     * tier, efficiency, transfer limit) are recomputed atomically from the full
     * registry after every call.
     *
     * @param owner         Network ID (player UUID)
     * @param controllerPos GlobalPos of the controller block (dim + BlockPos)
     * @param record        Structural snapshot from the newly formed multiblock
     */
    public void registerMatrix(UUID owner, GlobalPos controllerPos, MatrixRecord record) {
        if (owner == null || controllerPos == null || record == null) return;
        NetworkState state = getState(owner);
        // Dedup: remove any stale entry for the same physical position.
        // ResourceKey.create() does not guarantee reference equality with
        // registry-interned keys, so we compare by location string instead.
        final net.minecraft.core.BlockPos physPos = controllerPos.pos();
        final String physDim = controllerPos.dimension().location().toString();
        state.matrices.entrySet().removeIf(e ->
                e.getKey().pos().equals(physPos)
                        && e.getKey().dimension().location().toString().equals(physDim));
        state.matrices.put(controllerPos, record);
        recomputeAggregates(state);
        setDirty();
    }

    /**
     * Unregister a controller when its multiblock is invalidated.  If this was
     * the last matrix on the network the aggregates are zeroed and
     * {@code isMatrixFormed} returns false.
     */
    public void unregisterMatrix(UUID owner, GlobalPos controllerPos) {
        if (owner == null || controllerPos == null) return;
        NetworkState state = getState(owner);
        final net.minecraft.core.BlockPos physPos = controllerPos.pos();
        final String physDim = controllerPos.dimension().location().toString();
        state.matrices.entrySet().removeIf(e ->
                e.getKey().pos().equals(physPos)
                        && e.getKey().dimension().location().toString().equals(physDim));
        recomputeAggregates(state);
        setDirty();
    }

    /**
     * Startup sweep: verify every registered matrix entry is still a formed multiblock.
     * Called once from ServerStartedEvent so the QNT reflects the true network state
     * immediately after a world load, even when the controller was removed while the
     * server was offline (crash, manual file edit, etc.).
     *
     * Chunks containing registered matrix positions are force-loaded momentarily so
     * that the GTCEu machine lifecycle (onLoad / structure check) runs before we
     * query isFormed().  The one-time startup cost is negligible for the small
     * number of matrices typically registered.  Stale entries are pruned and
     * aggregates recomputed so the QNT shows accurate data immediately.
     */
    public void sweepStaleMatrices(MinecraftServer server) {
        boolean dirty = false;

        for (Map.Entry<UUID, NetworkState> ownerEntry : energyStorage.entrySet()) {
            NetworkState state = ownerEntry.getValue();
            if (state.matrices.isEmpty()) continue;

            List<GlobalPos> stale = new ArrayList<>();

            for (GlobalPos gpos : state.matrices.keySet()) {
                ResourceKey<Level> dimKey = gpos.dimension();
                ServerLevel level = server.getLevel(dimKey);

                if (level == null) {
                    // Dimension no longer registered (mod removed, etc.) -- always stale.
                    stale.add(gpos);
                    LOGGER.warn("[GTNA] Startup sweep: dimension '{}' not found -- pruning stale matrix at {}",
                            dimKey.location(), gpos.pos());
                    continue;
                }

                BlockPos bpos = gpos.pos();
                // Force-load the chunk so the GTCEu machine's onLoad() and structure-check
                // lifecycle fires before we call isFormed().  One-time startup cost only.
                level.getChunk(bpos.getX() >> 4, bpos.getZ() >> 4);

                // A valid, formed NFM controller must be a GTCEu WorkableMultiblockMachine
                // whose structure has successfully validated after loading.
                BlockEntity be = level.getBlockEntity(bpos);
                boolean formed = (be instanceof IMachineBlockEntity machBe)
                        && (machBe.getMetaMachine() instanceof WorkableMultiblockMachine ctrl)
                        && ctrl.isFormed();

                if (!formed) {
                    stale.add(gpos);
                    LOGGER.warn("[GTNA] Startup sweep: matrix at {} in '{}' is missing or unformed -- pruning",
                            bpos, dimKey.location());
                }
            }

            if (!stale.isEmpty()) {
                for (GlobalPos sp : stale) {
                    final BlockPos physPos = sp.pos();
                    final String physDim  = sp.dimension().location().toString();
                    state.matrices.entrySet().removeIf(e ->
                            e.getKey().pos().equals(physPos)
                                    && e.getKey().dimension().location().toString().equals(physDim));
                }
                recomputeAggregates(state);
                dirty = true;
            }
        }

        if (dirty) {
            setDirty();
            LOGGER.info("[GTNA] Startup sweep complete -- pruned stale matrix entries and updated network state.");
        } else {
            LOGGER.info("[GTNA] Startup sweep complete -- all registered matrices verified OK.");
        }
    }

    /** How many controllers are currently registered on this network. */
    public int getMatrixCount(UUID owner) {
        return getState(owner).matrices.size();
    }

    /** Read-only view of the per-controller registry, keyed by controller GlobalPos. */
    public java.util.Map<GlobalPos, MatrixRecord> getMatrices(UUID owner) {
        return java.util.Collections.unmodifiableMap(getState(owner).matrices);
    }

    /**
     * Recompute all aggregate stats from the per-controller registry.
     * Called after every register/unregister and after NBT deserialisation.
     */
    private void recomputeAggregates(NetworkState state) {
        if (state.matrices.isEmpty()) {
            state.totalCapacitors = 0;
            state.averageTier     = 0;
            state.efficiency      = 0.0;
            state.transferLimit   = Int128.ZERO();
            state.maxCapacity     = Int128.ZERO();
            state.matrixFormed    = false;
            return;
        }

        long caps           = 0;
        long weightedTier   = 0;
        double weightedEff  = 0.0;
        Int128 totalTL      = Int128.ZERO();
        Int128 totalCap     = Int128.ZERO();

        for (MatrixRecord rec : state.matrices.values()) {
            caps += rec.totalCapacitors;
            if (rec.totalCapacitors > 0) {
                weightedTier += (long) rec.averageTier * rec.totalCapacitors;
                weightedEff  += rec.efficiency * rec.totalCapacitors;
            }
            totalTL.add(rec.transferLimit);
            totalCap.add(rec.maxCapacity);
        }

        state.totalCapacitors = caps;
        state.averageTier     = (caps > 0) ? (int) (weightedTier / caps) : 1;
        if (state.averageTier < 1) state.averageTier = 1;
        state.efficiency      = (caps > 0) ? (weightedEff / caps) : 0.85;
        state.transferLimit   = totalTL;
        state.maxCapacity     = totalCap;
        state.matrixFormed    = true;

        // Clamp stored energy to the new combined capacity.
        if (!state.maxCapacity.isZero() && state.energy.compareTo(state.maxCapacity) > 0) {
            state.energy = state.maxCapacity.copy();
        }
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

    public Int128 getMaxCapacity(UUID owner) {
        return getState(owner).maxCapacity.copy();
    }

    public Int128 addEnergy(UUID owner, Int128 amount, ServerLevel level) {
        if (amount.isZero() || amount.isNegative()) return Int128.ZERO();

        NetworkState state = getState(owner);
        handleTick(state, level.getGameTime());

        // Reject energy into networks with no registered matrix (maxCapacity == 0).
        // Accepting unlimited energy into an unformed network causes overfill later.
        if (state.maxCapacity.isZero()) return Int128.ZERO();

        // Fix 1: clamp the incoming amount to the network transfer limit so
        // dynamos cannot push more than the displayed cap in a single tick.
        Int128 capped = (!state.transferLimit.isZero() && amount.compareTo(state.transferLimit) > 0)
                ? state.transferLimit.copy()
                : amount.copy();

        Int128 space = state.maxCapacity.copy();
        space.subtract(state.energy);
        if (space.isZero() || space.isNegative()) return Int128.ZERO();
        // accepted = how much the dynamo will drain from its own container
        Int128 accepted = capped.compareTo(space) > 0 ? space : capped;

        // Fix 2: apply efficiency — only a fraction of the accepted EU is actually
        // stored; the rest is lost as heat. The dynamo still drains the full
        // accepted amount so the energy "leaves" the source correctly.
        Int128 stored = accepted.copy();
        if (state.efficiency > 0.0 && state.efficiency < 1.0) {
            long storedLong = (long) (accepted.toLong() * state.efficiency);
            stored = new Int128(Math.max(0L, storedLong));
        }

        state.energy.add(stored);
        state.inputPerTick.add(stored); // record the actually-stored EU, not the raw push

        checkSafeMode(owner, state, level);
        setDirty();
        return accepted; // caller (dynamo) drains this much from its container
    }

    public void setEnergy(UUID owner, Int128 amount) {
        NetworkState state = getState(owner);
        Int128 clamped = amount.copy();
        if (!state.maxCapacity.isZero() && clamped.compareTo(state.maxCapacity) > 0) {
            clamped = state.maxCapacity.copy();
        }
        state.energy = clamped;
        setDirty();
    }

    /**
     * Consume energy from the network, enforcing the per-tick transfer limit.
     * Returns the amount actually consumed (Int128.ZERO on failure).
     * Callers must use the returned value — not the requested amount — when
     * crediting energy to a local buffer, so no EU is created from nothing.
     * Special machines with intentionally large one-time costs should use
     * {@link #consumeEnergyUnlimited} instead.
     */
    public Int128 consumeEnergy(UUID owner, Int128 amount, ServerLevel level) {
        if (amount.isZero() || amount.isNegative()) return Int128.ZERO();

        NetworkState state = getState(owner);
        handleTick(state, level.getGameTime());

        // Enforce transfer limit at the network layer (invariant for all hatch callers).
        Int128 capped = (!state.transferLimit.isZero() && amount.compareTo(state.transferLimit) > 0)
                ? state.transferLimit.copy()
                : amount.copy();

        if (state.safeMode) return Int128.ZERO();
        // Partial withdrawal: if stored energy is less than the capped request,
        // drain whatever remains so residual energy is never stranded.
        Int128 actual = state.energy.compareTo(capped) < 0 ? state.energy.copy() : capped;
        if (actual.isZero()) return Int128.ZERO();

        state.energy.subtract(actual);
        state.outputPerTick.add(actual);
        checkSafeMode(owner, state, level);
        setDirty();
        return actual;
    }

    /**
     * Consume energy without applying the transfer limit.
     * Use only for machines that require large one-time energy costs
     * (e.g. EyeOfHarmonyMachine startup) that must not be throttled.
     */
    public boolean consumeEnergyUnlimited(UUID owner, Int128 amount, ServerLevel level) {
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
