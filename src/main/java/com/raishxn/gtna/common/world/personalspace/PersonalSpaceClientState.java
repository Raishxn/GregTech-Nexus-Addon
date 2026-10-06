package com.raishxn.gtna.common.world.personalspace;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import com.raishxn.gtna.network.packet.SPersonalSpaceWorldList;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** What the client knows from the last world-list packet; plain data, safe to load on a server. */
public final class PersonalSpaceClientState {

    private static volatile List<String> allowedBiomes = List.of(PersonalSpaceSettings.DEFAULT_BIOME);
    private static volatile Set<String> allowedBlocks = Set.of();
    private static volatile List<String> allowedBoundaryBlocks = List.of();
    private static volatile List<String> allowedGapBlocks = List.of();
    private static volatile List<String> allowedCenterBlocks = List.of();
    private static volatile List<String> defaultPresets = List.of();
    private static volatile int dropdownRows = 6;
    private static volatile int dropdownColumns = 12;
    private static final Map<Integer, PersonalSpaceSettings> DIMENSIONS = new ConcurrentHashMap<>();

    private PersonalSpaceClientState() {}

    public static void update(SPersonalSpaceWorldList packet) {
        allowedBiomes = packet.biomes();
        allowedBlocks = new LinkedHashSet<>(packet.blocks());
        allowedBoundaryBlocks = packet.boundaryBlocks();
        allowedGapBlocks = packet.gapBlocks();
        allowedCenterBlocks = packet.centerBlocks();
        defaultPresets = packet.presets();
        dropdownRows = packet.dropdownRows();
        dropdownColumns = packet.dropdownColumns();
        DIMENSIONS.clear();
        DIMENSIONS.putAll(packet.dimensions());
    }

    public static void clear() {
        DIMENSIONS.clear();
    }

    public static PersonalSpaceSettings settings(int id) {
        return DIMENSIONS.get(id);
    }

    public static PersonalSpaceSettings settings(ResourceKey<Level> dimension) {
        int id = PersonalSpaceWorlds.id(dimension);
        return id > 0 ? DIMENSIONS.get(id) : null;
    }

    public static List<String> allowedBiomes() {
        return allowedBiomes;
    }

    public static Set<String> allowedBlocks() {
        return allowedBlocks;
    }

    public static List<String> allowedBoundaryBlocks() {
        return allowedBoundaryBlocks;
    }

    public static List<String> allowedGapBlocks() {
        return allowedGapBlocks;
    }

    public static List<String> allowedCenterBlocks() {
        return allowedCenterBlocks;
    }

    public static List<String> defaultPresets() {
        return defaultPresets;
    }

    public static int dropdownRows() {
        return dropdownRows;
    }

    public static int dropdownColumns() {
        return dropdownColumns;
    }
}
