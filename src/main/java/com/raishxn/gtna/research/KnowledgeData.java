package com.raishxn.gtna.research;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

/**
 * Research progress per scope (team or player): unlocked nodes, research points per area, the nodes whose
 * item requirement was met, the nodes whose eureka was found, and the counters of the point sources. Ids that no longer
 * exist in the loaded
 * graph are kept, so removing a node from a pack update never erases a team's progress.
 * <p>
 * Version 1 saved only the unlocked nodes; it loads as version 2 with no points, requirements or eurekas.
 */
public final class KnowledgeData extends SavedData {

    public static final String NAME = "gtna_knowledge";
    public static final int DATA_VERSION = 2;

    /** Everything one scope has. */
    private static final class Progress {

        final Set<ResourceLocation> unlocked = new LinkedHashSet<>();
        final Map<ResourceLocation, Long> points = new LinkedHashMap<>();
        final Set<ResourceLocation> requirements = new LinkedHashSet<>();
        final Set<ResourceLocation> eurekas = new LinkedHashSet<>();
        final Map<ResourceLocation, Long> counters = new LinkedHashMap<>();

        boolean isEmpty() {
            return unlocked.isEmpty() && points.isEmpty() && requirements.isEmpty() && eurekas.isEmpty() &&
                    counters.isEmpty();
        }
    }

    private final Map<UUID, Progress> scopes = new LinkedHashMap<>();

    public static KnowledgeData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(KnowledgeData::load, KnowledgeData::new, NAME);
    }

    private Optional<Progress> find(UUID scope) {
        return Optional.ofNullable(scopes.get(scope));
    }

    private Progress of(UUID scope) {
        return scopes.computeIfAbsent(scope, key -> new Progress());
    }

    // ------------------------------------------------------------------ unlocked nodes

    public Set<ResourceLocation> unlocked(UUID scope) {
        return find(scope).map(p -> Collections.unmodifiableSet(p.unlocked)).orElse(Set.of());
    }

    public boolean isUnlocked(UUID scope, ResourceLocation node) {
        return find(scope).map(p -> p.unlocked.contains(node)).orElse(false);
    }

    /** @return true if the node was newly added. */
    public boolean add(UUID scope, ResourceLocation node) {
        boolean added = of(scope).unlocked.add(node);
        if (added) setDirty();
        return added;
    }

    // ------------------------------------------------------------------ research points

    /** Points per area for the scope; areas never earned are absent. */
    public Map<ResourceLocation, Long> points(UUID scope) {
        return find(scope).map(p -> Collections.unmodifiableMap(p.points)).orElse(Map.of());
    }

    public long points(UUID scope, ResourceLocation area) {
        return find(scope).map(p -> p.points.getOrDefault(area, 0L)).orElse(0L);
    }

    /**
     * Adds (or, with a negative amount, removes) points. The balance never goes below zero.
     *
     * @return the new balance of the area.
     */
    public long addPoints(UUID scope, ResourceLocation area, long amount) {
        Progress progress = of(scope);
        long value = Math.max(0L, progress.points.getOrDefault(area, 0L) + amount);
        if (value == 0L) progress.points.remove(area);
        else progress.points.put(area, value);
        setDirty();
        return value;
    }

    public boolean canAfford(UUID scope, Map<ResourceLocation, Integer> cost) {
        for (var entry : cost.entrySet()) {
            if (points(scope, entry.getKey()) < entry.getValue()) return false;
        }
        return true;
    }

    /** Takes the whole cost, or nothing when any area is short. */
    public boolean spend(UUID scope, Map<ResourceLocation, Integer> cost) {
        if (!canAfford(scope, cost)) return false;
        cost.forEach((area, amount) -> addPoints(scope, area, -amount));
        return true;
    }

    // ------------------------------------------------------------------ requirements and eurekas

    public boolean requirementMet(UUID scope, ResourceLocation node) {
        return find(scope).map(p -> p.requirements.contains(node)).orElse(false);
    }

    /** @return true if the requirement was newly met. */
    public boolean markRequirement(UUID scope, ResourceLocation node) {
        boolean added = of(scope).requirements.add(node);
        if (added) setDirty();
        return added;
    }

    public Set<ResourceLocation> requirements(UUID scope) {
        return find(scope).map(p -> Collections.unmodifiableSet(p.requirements)).orElse(Set.of());
    }

    public boolean eurekaMet(UUID scope, ResourceLocation node) {
        return find(scope).map(p -> p.eurekas.contains(node)).orElse(false);
    }

    /** @return true if the eureka was newly found. */
    public boolean markEureka(UUID scope, ResourceLocation node) {
        boolean added = of(scope).eurekas.add(node);
        if (added) setDirty();
        return added;
    }

    public Set<ResourceLocation> eurekas(UUID scope) {
        return find(scope).map(p -> Collections.unmodifiableSet(p.eurekas)).orElse(Set.of());
    }

    // ------------------------------------------------------------------ point source counters

    /** How many times the scope has done what a research source counts. */
    public long counter(UUID scope, ResourceLocation source) {
        return find(scope).map(p -> p.counters.getOrDefault(source, 0L)).orElse(0L);
    }

    public void setCounter(UUID scope, ResourceLocation source, long value) {
        of(scope).counters.put(source, Math.max(0L, value));
        setDirty();
    }

    // ------------------------------------------------------------------ reset and persistence

    /**
     * Removes everything the scope has: nodes, points, requirements, eurekas and source counters.
     *
     * @return how many unlocked nodes were removed.
     */
    public int reset(UUID scope) {
        Progress removed = scopes.remove(scope);
        if (removed == null) return 0;
        setDirty();
        return removed.unlocked.size();
    }

    public static KnowledgeData load(CompoundTag tag) {
        KnowledgeData data = new KnowledgeData();
        for (Tag value : tag.getList("scopes", Tag.TAG_COMPOUND)) {
            CompoundTag entry = (CompoundTag) value;
            if (!entry.hasUUID("scope")) continue;
            Progress progress = new Progress();
            readIds(entry.getList("nodes", Tag.TAG_STRING), progress.unlocked);
            readIds(entry.getList("requirements", Tag.TAG_STRING), progress.requirements);
            readIds(entry.getList("eurekas", Tag.TAG_STRING), progress.eurekas);
            CompoundTag points = entry.getCompound("points");
            for (String key : points.getAllKeys()) {
                ResourceLocation area = ResourceLocation.tryParse(key);
                long amount = points.getLong(key);
                if (area != null && amount > 0) progress.points.put(area, amount);
            }
            CompoundTag counters = entry.getCompound("counters");
            for (String key : counters.getAllKeys()) {
                ResourceLocation source = ResourceLocation.tryParse(key);
                if (source != null) progress.counters.put(source, counters.getLong(key));
            }
            if (!progress.isEmpty()) data.scopes.put(entry.getUUID("scope"), progress);
        }
        return data;
    }

    private static void readIds(ListTag list, Set<ResourceLocation> into) {
        for (Tag name : list) {
            ResourceLocation id = ResourceLocation.tryParse(name.getAsString());
            if (id != null) into.add(id);
        }
    }

    private static ListTag writeIds(Set<ResourceLocation> ids) {
        ListTag list = new ListTag();
        ids.forEach(id -> list.add(StringTag.valueOf(id.toString())));
        return list;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putInt("data_version", DATA_VERSION);
        ListTag list = new ListTag();
        scopes.forEach((scope, progress) -> {
            if (progress.isEmpty()) return;
            CompoundTag entry = new CompoundTag();
            entry.putUUID("scope", scope);
            entry.put("nodes", writeIds(progress.unlocked));
            entry.put("requirements", writeIds(progress.requirements));
            entry.put("eurekas", writeIds(progress.eurekas));
            CompoundTag points = new CompoundTag();
            progress.points.forEach((area, amount) -> points.putLong(area.toString(), amount));
            entry.put("points", points);
            CompoundTag counters = new CompoundTag();
            progress.counters.forEach((source, count) -> counters.putLong(source.toString(), count));
            entry.put("counters", counters);
            list.add(entry);
        });
        tag.put("scopes", list);
        return tag;
    }
}
