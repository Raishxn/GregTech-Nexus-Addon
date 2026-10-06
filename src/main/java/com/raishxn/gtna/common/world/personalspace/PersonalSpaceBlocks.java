package com.raishxn.gtna.common.world.personalspace;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Block names used by PersonalSpace presets. The 1.7.10 original stores {@code modid:name[:meta]}; GTNA stores
 * modern registry IDs and converts the legacy vanilla names and metadata that GTNH presets commonly use.
 */
public final class PersonalSpaceBlocks {

    private static final String[] COLORS = { "white", "orange", "magenta", "light_blue", "yellow", "lime", "pink",
            "gray", "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black" };
    private static final String[] WOODS = { "oak", "spruce", "birch", "jungle", "acacia", "dark_oak" };
    private static final Map<String, String> LEGACY = new HashMap<>();

    static {
        for (int meta = 0; meta < 16; meta++) {
            LEGACY.put("minecraft:wool:" + meta, "minecraft:" + COLORS[meta] + "_wool");
            LEGACY.put("minecraft:stained_hardened_clay:" + meta, "minecraft:" + COLORS[meta] + "_terracotta");
            LEGACY.put("minecraft:stained_glass:" + meta, "minecraft:" + COLORS[meta] + "_stained_glass");
            LEGACY.put("minecraft:carpet:" + meta, "minecraft:" + COLORS[meta] + "_carpet");
        }
        for (int meta = 0; meta < WOODS.length; meta++) {
            LEGACY.put("minecraft:planks:" + meta, "minecraft:" + WOODS[meta] + "_planks");
        }
        LEGACY.put("minecraft:grass:0", "minecraft:grass_block");
        LEGACY.put("minecraft:double_stone_slab:0", "minecraft:smooth_stone");
        LEGACY.put("minecraft:hardened_clay:0", "minecraft:terracotta");
        LEGACY.put("minecraft:snow:0", "minecraft:snow_block");
        LEGACY.put("minecraft:brick_block:0", "minecraft:bricks");
        LEGACY.put("minecraft:stonebrick:0", "minecraft:stone_bricks");
        LEGACY.put("minecraft:stonebrick:1", "minecraft:mossy_stone_bricks");
        LEGACY.put("minecraft:stonebrick:2", "minecraft:cracked_stone_bricks");
        LEGACY.put("minecraft:stonebrick:3", "minecraft:chiseled_stone_bricks");
        LEGACY.put("minecraft:sand:1", "minecraft:red_sand");
        LEGACY.put("minecraft:dirt:1", "minecraft:coarse_dirt");
        LEGACY.put("minecraft:dirt:2", "minecraft:podzol");
        LEGACY.put("minecraft:sandstone:1", "minecraft:chiseled_sandstone");
        LEGACY.put("minecraft:sandstone:2", "minecraft:smooth_sandstone");
        LEGACY.put("minecraft:quartz_block:1", "minecraft:chiseled_quartz_block");
        LEGACY.put("minecraft:quartz_block:2", "minecraft:quartz_pillar");
        LEGACY.put("minecraft:lit_pumpkin:0", "minecraft:jack_o_lantern");
        LEGACY.put("minecraft:web:0", "minecraft:cobweb");
        LEGACY.put("minecraft:stone_slab:0", "minecraft:smooth_stone_slab");
        LEGACY.put("minecraft:mycelium:0", "minecraft:mycelium");
        LEGACY.put("minecraft:end_stone:0", "minecraft:end_stone");
        LEGACY.put("minecraft:nether_brick:0", "minecraft:nether_bricks");
        LEGACY.put("minecraft:quartz_ore:0", "minecraft:nether_quartz_ore");
    }

    private PersonalSpaceBlocks() {}

    /**
     * Converts {@code modid:name}, {@code modid:name:meta} (legacy) or an empty string to a modern block ID. Unknown
     * legacy metadata yields {@code null}; an unknown modern ID is returned unchanged so callers can report it.
     */
    public static String normalize(String name) {
        if (name == null) return "";
        String trimmed = name.trim();
        if (trimmed.isEmpty()) return "";
        int first = trimmed.indexOf(':');
        int last = trimmed.lastIndexOf(':');
        if (first > 0 && last > first) {
            String meta = trimmed.substring(last + 1);
            if (meta.chars().allMatch(Character::isDigit) && !meta.isEmpty()) {
                String base = trimmed.substring(0, last);
                String mapped = LEGACY.get(base + ":" + Integer.parseInt(meta));
                if (mapped != null) return mapped;
                return Integer.parseInt(meta) == 0 ? normalize(base) : null;
            }
        }
        String mapped = LEGACY.get(trimmed + ":0");
        return mapped != null ? mapped : trimmed;
    }

    /** @return the block for a modern or legacy name, or {@code null} when it does not exist. */
    public static Block block(String name) {
        String id = normalize(name);
        if (id == null || id.isEmpty()) return null;
        ResourceLocation location = ResourceLocation.tryParse(id);
        if (location == null) return null;
        return BuiltInRegistries.BLOCK.getOptional(location).orElse(null);
    }

    /** @return the default state, or {@code null} for empty, air or unknown blocks. */
    public static BlockState solidState(String name) {
        Block block = block(name);
        if (block == null || block == Blocks.AIR) return null;
        return block.defaultBlockState();
    }

    public static boolean isAir(String name) {
        Block block = block(name);
        return block == Blocks.AIR || block == Blocks.CAVE_AIR || block == Blocks.VOID_AIR;
    }

    public static String id(Block block) {
        return BuiltInRegistries.BLOCK.getKey(block).toString();
    }

    /** Parses the original meta syntax: {@code 0}, {@code 0-12}, {@code !5}, {@code 0-15,!3}. */
    public static List<Integer> metaValues(String spec) {
        List<int[]> positive = new ArrayList<>();
        List<int[]> negative = new ArrayList<>();
        for (String partRaw : spec.split(",")) {
            String part = partRaw.trim();
            boolean negated = part.startsWith("!");
            if (negated) part = part.substring(1).trim();
            if (part.isEmpty()) continue;
            int[] range;
            try {
                int dash = part.indexOf('-');
                if (dash >= 0) {
                    int a = Math.max(0, Integer.parseInt(part.substring(0, dash).trim()));
                    int b = Math.max(0, Integer.parseInt(part.substring(dash + 1).trim()));
                    range = new int[] { Math.min(a, b), Math.max(a, b) };
                } else {
                    int m = Math.max(0, Integer.parseInt(part));
                    range = new int[] { m, m };
                }
            } catch (NumberFormatException exception) {
                return List.of();
            }
            (negated ? negative : positive).add(range);
        }
        if (positive.isEmpty() && negative.isEmpty()) positive.add(new int[] { 0, 0 });
        int max = 15;
        for (int[] range : positive) max = Math.max(max, Math.min(range[1], 255));
        List<Integer> result = new ArrayList<>();
        for (int meta = 0; meta <= max; meta++) {
            boolean allowed = positive.isEmpty();
            for (int[] range : positive) allowed |= meta >= range[0] && meta <= range[1];
            for (int[] range : negative) allowed &= meta < range[0] || meta > range[1];
            if (allowed) result.add(meta);
        }
        return result;
    }
}
