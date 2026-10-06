package com.raishxn.gtna.common.data.fluid;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** World-specific discovery certificates, scoped to GTCEu's player/team owner UUID. */
public final class FluidDiscoveryData extends SavedData {

    public record Discovery(UUID owner, String dimension, String fluid) {}

    private final Map<UUID, Discovery> discoveries = new LinkedHashMap<>();

    public static FluidDiscoveryData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(
                FluidDiscoveryData::load, FluidDiscoveryData::new, "gtna_fluid_discoveries");
    }

    public UUID discover(UUID owner, String dimension, String fluid) {
        Discovery entry = new Discovery(owner, dimension, fluid);
        for (var existing : discoveries.entrySet()) {
            if (existing.getValue().equals(entry)) return existing.getKey();
        }
        UUID certificate = UUID.randomUUID();
        discoveries.put(certificate, entry);
        setDirty();
        return certificate;
    }

    public boolean allows(UUID certificate, UUID owner, String dimension, String fluid) {
        return new Discovery(owner, dimension, fluid).equals(discoveries.get(certificate));
    }

    public static FluidDiscoveryData load(CompoundTag tag) {
        FluidDiscoveryData data = new FluidDiscoveryData();
        for (Tag value : tag.getList("discoveries", Tag.TAG_COMPOUND)) {
            CompoundTag entry = (CompoundTag) value;
            if (!entry.hasUUID("certificate") || !entry.hasUUID("owner")) continue;
            data.discoveries.put(entry.getUUID("certificate"), new Discovery(entry.getUUID("owner"),
                    entry.getString("dimension"), entry.getString("fluid")));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag entries = new ListTag();
        discoveries.forEach((certificate, discovery) -> {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("certificate", certificate);
            entry.putUUID("owner", discovery.owner());
            entry.putString("dimension", discovery.dimension());
            entry.putString("fluid", discovery.fluid());
            entries.add(entry);
        });
        tag.put("discoveries", entries);
        return tag;
    }
}
