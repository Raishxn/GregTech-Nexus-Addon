package com.raishxn.gtna.api.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SteamNetworkData extends SavedData {
    public static final String DATA_NAME = "gtna_steam_network";
    private final Map<UUID, Long> steamStorage = new HashMap<>();

    // Método factory para carregar ou criar os dados
    public static SteamNetworkData get(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            return serverLevel.getServer().overworld().getDataStorage().computeIfAbsent(
                    SteamNetworkData::load,
                    SteamNetworkData::new,
                    DATA_NAME
            );
        }
        return null;
    }

    public long getSteam(UUID playerUUID) {
        return steamStorage.getOrDefault(playerUUID, 0L);
    }

    public void addSteam(UUID playerUUID, long amount) {
        long current = getSteam(playerUUID);
        long max = Long.MAX_VALUE;
        // Previne overflow
        if (current + amount < 0) {
            steamStorage.put(playerUUID, max);
        } else {
            steamStorage.put(playerUUID, Math.min(max, current + amount));
        }
        setDirty();
    }

    public boolean consumeSteam(UUID playerUUID, long amount) {
        long current = getSteam(playerUUID);
        if (current >= amount) {
            steamStorage.put(playerUUID, current - amount);
            setDirty();
            return true;
        }
        return false;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag compound) {
        ListTag list = new ListTag();
        steamStorage.forEach((uuid, amount) -> {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("UUID", uuid);
            entry.putLong("Amount", amount);
            list.add(entry);
        });
        compound.put("SteamData", list);
        return compound;
    }

    public static SteamNetworkData load(CompoundTag compound) {
        SteamNetworkData data = new SteamNetworkData();
        ListTag list = compound.getList("SteamData", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            data.steamStorage.put(entry.getUUID("UUID"), entry.getLong("Amount"));
        }
        return data;
    }
}