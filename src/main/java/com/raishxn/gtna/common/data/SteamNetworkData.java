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

    public void addSteam(UUID owner, long amount) {
        long current = getSteam(owner);
        long next = current + amount;
        if (next < 0) next = Long.MAX_VALUE;

        steamStorage.put(owner, next);
        setDirty();
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