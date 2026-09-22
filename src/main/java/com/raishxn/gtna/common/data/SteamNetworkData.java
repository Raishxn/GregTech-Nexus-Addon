package com.raishxn.gtna.common.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SteamNetworkData extends SavedData {

    private static final String DATA_NAME = "gtna_steam_network";
    private final Map<UUID, Long> steamStorage = new HashMap<>();

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
}
