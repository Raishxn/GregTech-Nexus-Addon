package com.raishxn.gtna.common.data;

import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SteamNetworkData extends SavedData {

    private static final String DATA_NAME = "gtna_steam_network";
    private final Map<UUID, Long> steamStorage = new HashMap<>();

    /**
     * Runtime-only (never persisted) view of the wireless steam hatches that reported in recently,
     * keyed by network owner. Used by the inspection command; entries expire after a short TTL so an
     * unloaded hatch disappears on its own without an explicit unregister.
     */
    private final Map<UUID, Map<GlobalPos, ConnectionInfo>> connections = new HashMap<>();

    /** A wireless steam hatch that touched the network recently. */
    public static final class ConnectionInfo {

        public final GlobalPos pos;
        public final boolean isInput;
        public final boolean isSteel;
        public long lastSeenTick;

        public ConnectionInfo(GlobalPos pos, boolean isInput, boolean isSteel, long lastSeenTick) {
            this.pos = pos;
            this.isInput = isInput;
            this.isSteel = isSteel;
            this.lastSeenTick = lastSeenTick;
        }
    }

    public SteamNetworkData() {}

    public SteamNetworkData(CompoundTag tag) {
        if (tag.contains("SteamNetworks", Tag.TAG_LIST)) {
            ListTag list = tag.getList("SteamNetworks", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag entry = list.getCompound(i);
                if (entry.hasUUID("Owner")) {
                    steamStorage.put(entry.getUUID("Owner"), entry.getLong("Amount"));
                }
            }
        }
    }

    public static SteamNetworkData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage()
                .computeIfAbsent(SteamNetworkData::new, SteamNetworkData::new, DATA_NAME);
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        ListTag list = new ListTag();
        steamStorage.forEach((uuid, amount) -> {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("Owner", uuid);
            entry.putLong("Amount", amount);
            list.add(entry);
        });
        tag.put("SteamNetworks", list);
        return tag;
    }

    public long getSteam(UUID owner) {
        return steamStorage.getOrDefault(owner, 0L);
    }

    /**
     * Atomically adds {@code amount} to the owner's balance. A negative {@code amount} subtracts;
     * if the balance would go below zero the operation is rejected and the balance is left
     * untouched (GTNL {@code addSteamToGlobalSteamMap} semantics), so a caller can never overdraft.
     *
     * @return {@code true} when the balance changed, {@code false} on an overdraft.
     */
    public boolean addSteam(UUID owner, long amount) {
        if (amount == 0) return false;
        long current = getSteam(owner);
        long next = current + amount;
        // A negative result means either an overdraft (negative amount) or a positive overflow;
        // both are rejected atomically rather than silently wrapping or voiding steam.
        if (next < 0) return false;

        steamStorage.put(owner, next);
        setDirty();
        return true;
    }

    public void setSteam(UUID owner, long amount) {
        steamStorage.put(owner, amount);
        setDirty();
    }

    public boolean consumeSteam(UUID owner, long amount) {
        long current = getSteam(owner);
        if (current >= amount) {
            steamStorage.put(owner, current - amount);
            setDirty();
            return true;
        }
        return false;
    }

    // ------------------------------------------------------------------
    // Runtime hatch bookkeeping (for the inspection command). Not persisted.
    // ------------------------------------------------------------------

    /** Records that a wireless steam hatch owned by {@code owner} is alive at {@code pos}. */
    public void reportConnection(UUID owner, GlobalPos pos, boolean isInput, boolean isSteel, long tick) {
        connections.computeIfAbsent(owner, ignored -> new HashMap<>())
                .put(pos, new ConnectionInfo(pos, isInput, isSteel, tick));
    }

    /**
     * The connections of {@code owner} seen within the last {@code ttl} ticks; stale entries are
     * pruned as a side effect.
     */
    public List<ConnectionInfo> getActiveConnections(UUID owner, long nowTick, long ttl) {
        Map<GlobalPos, ConnectionInfo> map = connections.get(owner);
        if (map == null) return List.of();
        List<ConnectionInfo> active = new ArrayList<>(map.size());
        var iterator = map.values().iterator();
        while (iterator.hasNext()) {
            ConnectionInfo info = iterator.next();
            if (nowTick - info.lastSeenTick > ttl) {
                iterator.remove();
            } else {
                active.add(info);
            }
        }
        return active;
    }
}
