package com.raishxn.gtna.common.data;

import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.MissingMappingsEvent;

/** Retired aliases disappear from registries; old saves retain physical blocks and items. */
public final class EyeOfHarmonyLegacyMappings {

    private EyeOfHarmonyLegacyMappings() {}

    public static Block replacement(String path) {
        return switch (path) {
            case "dimensional_bridge_casing" -> GTNAEyeOfHarmonyContent.SPATIAL_CASING.get();
            case "dimensional_stability_casing" -> GTNAEyeOfHarmonyContent.STABILISATION_FIELDS[0].get();
            case "spacetime_compression_field_generator" -> GTNAEyeOfHarmonyContent.COMPRESSION_FIELDS[0].get();
            default -> null;
        };
    }

    public static void remap(MissingMappingsEvent event) {
        for (var mapping : event.getMappings(ForgeRegistries.Keys.BLOCKS, "gtna")) {
            var block = replacement(mapping.getKey().getPath());
            if (block != null) mapping.remap(block);
        }
        for (var mapping : event.getMappings(ForgeRegistries.Keys.ITEMS, "gtna")) {
            var block = replacement(mapping.getKey().getPath());
            if (block != null) mapping.remap(block.asItem());
        }
    }
}
