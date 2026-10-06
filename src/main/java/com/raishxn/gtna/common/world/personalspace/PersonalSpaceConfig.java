package com.raishxn.gtna.common.world.personalspace;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fml.loading.FMLPaths;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.raishxn.gtna.GTNACORE;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Server configuration of PersonalSpace ({@code config/gtna/personal_space.json}), mirroring the original
 * {@code Config}. Block rules accept modern IDs, {@code #tags}, or the original {@code modid:block:meta-range} syntax
 * for legacy vanilla blocks.
 */
public final class PersonalSpaceConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "personal_space.json";

    private static Values values = Values.defaults();

    private PersonalSpaceConfig() {}

    public static final class Values {

        public List<String> defaultPresets;
        public List<String> allowedBlocks;
        public List<String> allowedBoundaryBlocks;
        public List<String> allowedGapBlocks;
        public List<String> allowedCenterBlocks;
        public List<String> allowedBiomes;
        public Integer firstDimensionId;
        public Boolean debugLogging;
        public Boolean useBlockEventChecks;
        public Integer dropdownMaxVisibleRows;
        public Integer dropdownMaxVisibleColumns;

        public static Values defaults() {
            Values values = new Values();
            values.defaultPresets = List.of(PersonalSpaceSettings.PRESET_UW_VOID,
                    PersonalSpaceSettings.PRESET_UW_GARDEN, PersonalSpaceSettings.PRESET_UW_MINING,
                    PersonalSpaceSettings.PRESET_GTNA_ROADS);
            values.allowedBlocks = buildingBlocks();
            values.allowedBoundaryBlocks = buildingBlocks();
            values.allowedGapBlocks = buildingBlocks();
            values.allowedCenterBlocks = buildingBlocks();
            values.allowedBiomes = List.of("minecraft:plains", "minecraft:ocean", "minecraft:desert",
                    "minecraft:windswept_hills", "minecraft:forest", "minecraft:taiga", "minecraft:swamp",
                    "minecraft:river", "minecraft:mushroom_fields", "minecraft:jungle", "minecraft:savanna",
                    "minecraft:badlands");
            values.firstDimensionId = 180;
            values.debugLogging = false;
            values.useBlockEventChecks = true;
            values.dropdownMaxVisibleRows = 6;
            values.dropdownMaxVisibleColumns = 12;
            return values;
        }

        /**
         * GTNA default: vanilla building blocks plus Xtones and Factory Blocks when installed. The original only allows
         * a few layer blocks and wool for decorations; servers can narrow this list again.
         */
        static List<String> buildingBlocks() {
            List<String> list = new ArrayList<>(List.of("minecraft:air", "minecraft:bedrock", "minecraft:stone",
                    "minecraft:cobblestone", "minecraft:mossy_cobblestone", "minecraft:smooth_stone",
                    "minecraft:stone_bricks", "minecraft:mossy_stone_bricks", "minecraft:cracked_stone_bricks",
                    "minecraft:chiseled_stone_bricks", "minecraft:granite", "minecraft:polished_granite",
                    "minecraft:diorite", "minecraft:polished_diorite", "minecraft:andesite",
                    "minecraft:polished_andesite", "minecraft:deepslate", "minecraft:cobbled_deepslate",
                    "minecraft:polished_deepslate", "minecraft:deepslate_bricks", "minecraft:cracked_deepslate_bricks",
                    "minecraft:deepslate_tiles", "minecraft:cracked_deepslate_tiles", "minecraft:chiseled_deepslate",
                    "minecraft:tuff", "minecraft:calcite", "minecraft:dripstone_block", "minecraft:dirt",
                    "minecraft:coarse_dirt", "minecraft:rooted_dirt", "minecraft:podzol", "minecraft:mycelium",
                    "minecraft:grass_block", "minecraft:moss_block", "minecraft:mud", "minecraft:packed_mud",
                    "minecraft:mud_bricks", "minecraft:clay", "minecraft:gravel", "#minecraft:sand",
                    "minecraft:sandstone", "minecraft:chiseled_sandstone", "minecraft:cut_sandstone",
                    "minecraft:smooth_sandstone", "minecraft:red_sandstone", "minecraft:chiseled_red_sandstone",
                    "minecraft:cut_red_sandstone", "minecraft:smooth_red_sandstone", "minecraft:netherrack",
                    "minecraft:basalt", "minecraft:polished_basalt", "minecraft:smooth_basalt", "minecraft:blackstone",
                    "minecraft:polished_blackstone", "minecraft:polished_blackstone_bricks",
                    "minecraft:cracked_polished_blackstone_bricks", "minecraft:chiseled_polished_blackstone",
                    "minecraft:gilded_blackstone", "minecraft:nether_bricks", "minecraft:red_nether_bricks",
                    "minecraft:soul_sand", "minecraft:soul_soil", "minecraft:magma_block", "minecraft:end_stone",
                    "minecraft:end_stone_bricks", "minecraft:purpur_block", "minecraft:purpur_pillar",
                    "minecraft:obsidian", "minecraft:bricks", "minecraft:prismarine", "minecraft:prismarine_bricks",
                    "minecraft:dark_prismarine", "minecraft:sea_lantern", "minecraft:quartz_block",
                    "minecraft:smooth_quartz", "minecraft:quartz_bricks", "minecraft:quartz_pillar",
                    "minecraft:chiseled_quartz_block", "minecraft:snow_block", "minecraft:ice", "minecraft:packed_ice",
                    "minecraft:blue_ice", "minecraft:glowstone", "minecraft:shroomlight",
                    "minecraft:ochre_froglight", "minecraft:verdant_froglight", "minecraft:pearlescent_froglight",
                    "minecraft:amethyst_block", "minecraft:terracotta", "#minecraft:terracotta", "#minecraft:wool",
                    "#minecraft:planks", "#minecraft:logs", "minecraft:glass"));
            for (String color : new String[] { "white", "orange", "magenta", "light_blue", "yellow", "lime", "pink",
                    "gray", "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black" }) {
                list.add("minecraft:" + color + "_concrete");
                list.add("minecraft:" + color + "_concrete_powder");
                list.add("minecraft:" + color + "_glazed_terracotta");
                list.add("minecraft:" + color + "_stained_glass");
            }
            list.add("xtonesreworked:*");
            list.add("xtones:*");
            list.add("factory_blocks:*");
            return List.copyOf(list);
        }

        void applyDefaults(Values defaults) {
            if (defaultPresets == null) defaultPresets = defaults.defaultPresets;
            if (allowedBlocks == null) allowedBlocks = defaults.allowedBlocks;
            if (allowedBoundaryBlocks == null) allowedBoundaryBlocks = defaults.allowedBoundaryBlocks;
            if (allowedGapBlocks == null) allowedGapBlocks = defaults.allowedGapBlocks;
            if (allowedCenterBlocks == null) allowedCenterBlocks = defaults.allowedCenterBlocks;
            if (allowedBiomes == null) allowedBiomes = defaults.allowedBiomes;
            if (firstDimensionId == null || firstDimensionId < 1) firstDimensionId = defaults.firstDimensionId;
            if (debugLogging == null) debugLogging = defaults.debugLogging;
            if (useBlockEventChecks == null) useBlockEventChecks = defaults.useBlockEventChecks;
            if (dropdownMaxVisibleRows == null) dropdownMaxVisibleRows = defaults.dropdownMaxVisibleRows;
            if (dropdownMaxVisibleColumns == null) dropdownMaxVisibleColumns = defaults.dropdownMaxVisibleColumns;
            dropdownMaxVisibleRows = Math.max(1, Math.min(20, dropdownMaxVisibleRows));
            dropdownMaxVisibleColumns = Math.max(1, Math.min(24, dropdownMaxVisibleColumns));
            List<String> biomes = new ArrayList<>();
            for (String biome : allowedBiomes) biomes.add(PersonalSpaceSettings.normalizeBiome(biome));
            allowedBiomes = List.copyOf(biomes);
        }
    }

    public static Path path() {
        return FMLPaths.CONFIGDIR.get().resolve("gtna").resolve(FILE_NAME);
    }

    /** @return whether the file was read (or created) successfully. */
    public static boolean load() {
        Path path = path();
        Values defaults = Values.defaults();
        try {
            Files.createDirectories(path.getParent());
            if (Files.notExists(path)) {
                try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                    GSON.toJson(defaults, writer);
                }
                values = defaults;
                return true;
            }
            try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                Values loaded = GSON.fromJson(reader, Values.class);
                if (loaded == null) loaded = defaults;
                loaded.applyDefaults(defaults);
                values = loaded;
                return true;
            }
        } catch (IOException | JsonParseException exception) {
            GTNACORE.LOGGER.warn("Failed to load {}; using PersonalSpace defaults", path, exception);
            values = defaults;
            return false;
        }
    }

    public static Values values() {
        return values;
    }

    public static List<String> defaultPresets() {
        return values.defaultPresets;
    }

    public static int firstDimensionId() {
        return values.firstDimensionId;
    }

    public static boolean debugLogging() {
        return values.debugLogging;
    }

    public static boolean useBlockEventChecks() {
        return values.useBlockEventChecks;
    }

    public static List<String> allowedBiomes() {
        return values.allowedBiomes;
    }

    public static Set<String> allowedBlocks() {
        return expand(values.allowedBlocks);
    }

    public static Set<String> allowedBoundaryBlocks() {
        return expand(values.allowedBoundaryBlocks);
    }

    public static Set<String> allowedGapBlocks() {
        return expand(values.allowedGapBlocks);
    }

    public static Set<String> allowedCenterBlocks() {
        return expand(values.allowedCenterBlocks);
    }

    /** Expands configured rules to existing modern block IDs, preserving order and dropping invalid entries. */
    public static Set<String> expand(List<String> rules) {
        Set<String> result = new LinkedHashSet<>();
        for (String raw : rules) {
            if (raw == null || raw.isBlank()) continue;
            String rule = raw.trim();
            if (rule.startsWith("#")) {
                ResourceLocation tagId = ResourceLocation.tryParse(rule.substring(1));
                if (tagId == null) continue;
                TagKey<Block> tag = TagKey.create(Registries.BLOCK, tagId);
                BuiltInRegistries.BLOCK.getTagOrEmpty(tag).forEach(holder -> addHolder(result, holder));
                continue;
            }
            if (rule.endsWith(":*")) {
                String namespace = rule.substring(0, rule.length() - 2);
                BuiltInRegistries.BLOCK.keySet().stream()
                        .filter(id -> id.getNamespace().equals(namespace))
                        .sorted()
                        .forEach(id -> addIfExists(result, id.toString()));
                continue;
            }
            int first = rule.indexOf(':');
            int last = rule.lastIndexOf(':');
            if (first > 0 && last > first) {
                String base = rule.substring(0, last);
                for (int meta : PersonalSpaceBlocks.metaValues(rule.substring(last + 1))) {
                    addIfExists(result, PersonalSpaceBlocks.normalize(base + ":" + meta));
                }
            } else {
                addIfExists(result, PersonalSpaceBlocks.normalize(rule));
            }
        }
        return result;
    }

    private static void addHolder(Set<String> result, Holder<Block> holder) {
        holder.unwrapKey().ifPresent(key -> result.add(key.location().toString()));
    }

    private static void addIfExists(Set<String> result, String id) {
        if (id != null && !id.isEmpty() && PersonalSpaceBlocks.block(id) != null) result.add(id);
    }
}
