package com.raishxn.gtna.common.world.personalspace;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import com.raishxn.gtna.GTNACORE;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Stable numeric IDs and the {@code DimensionConfig} of every personal dimension, saved in the overworld independently
 * of which dimensions are loaded.
 */
public final class PersonalSpaceDirectory extends SavedData {

    private static final String FILE_NAME = "gtna_personal_space";
    private final Map<Integer, PersonalSpaceSettings> dimensions = new LinkedHashMap<>();
    private int nextId = PersonalSpaceConfig.firstDimensionId();

    public static PersonalSpaceDirectory get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(PersonalSpaceDirectory::load,
                PersonalSpaceDirectory::new, FILE_NAME);
    }

    public Map<Integer, PersonalSpaceSettings> dimensions() {
        return Collections.unmodifiableMap(dimensions);
    }

    /** The live settings object; call {@link #setDirty()} after changing it. */
    public PersonalSpaceSettings settings(int id) {
        return dimensions.get(id);
    }

    public int nextId() {
        return Math.max(nextId, PersonalSpaceConfig.firstDimensionId());
    }

    public void add(int id, PersonalSpaceSettings settings) {
        if (id < 1 || id != nextId() || dimensions.containsKey(id)) {
            throw new IllegalArgumentException("PersonalSpace ID is already used or out of order: " + id);
        }
        dimensions.put(id, settings);
        nextId = id + 1;
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putInt("NextId", nextId);
        ListTag list = new ListTag();
        dimensions.forEach((id, settings) -> {
            CompoundTag entry = new CompoundTag();
            entry.putInt("Id", id);
            entry.put("Settings", settings.save());
            list.add(entry);
        });
        tag.put("Dimensions", list);
        return tag;
    }

    public static PersonalSpaceDirectory load(CompoundTag tag) {
        PersonalSpaceDirectory result = new PersonalSpaceDirectory();
        ListTag list = tag.getList("Dimensions", Tag.TAG_COMPOUND);
        for (int index = 0; index < list.size(); index++) {
            CompoundTag entry = list.getCompound(index);
            int id = entry.getInt("Id");
            if (id < 1 || result.dimensions.containsKey(id)) continue;
            try {
                result.dimensions.put(id, PersonalSpaceSettings.load(entry.getCompound("Settings")));
                result.nextId = Math.max(result.nextId, id + 1);
            } catch (RuntimeException exception) {
                GTNACORE.LOGGER.error("Skipping invalid PersonalSpace settings for dimension {}", id, exception);
            }
        }
        result.nextId = Math.max(result.nextId, tag.getInt("NextId"));
        return result;
    }
}
