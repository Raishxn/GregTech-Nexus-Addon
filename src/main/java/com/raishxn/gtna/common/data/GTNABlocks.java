package com.raishxn.gtna.common.data;

import com.gregtechceu.gtceu.api.item.tool.GTToolType;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GlassBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.common.block.NexusCapacitorBlock;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import java.util.function.Supplier;

import static com.raishxn.gtna.api.registry.GTNARegistry.REGISTRATE;

public class GTNABlocks {

    static {
        REGISTRATE.creativeModeTab(() -> GTNACreativeModeTabs.BLOCKS);
    }

    public static final BlockEntry<Block> BREEL_PIPE_CASING = createCasingBlock("breel_pipe_casing");
    public static final BlockEntry<Block> HYPER_PRESSURE_BREEL_CASING = createCasingBlock(
            "hyper_pressure_breel_casing");
    public static final BlockEntry<Block> STEAM_COMPACT_PIPE_CASING = createCasingBlock("steam_compact_pipe_casing");
    public static final BlockEntry<Block> VIBRATION_SAFE_CASING = createCasingBlock("vibration_safe_casing");
    public static final BlockEntry<Block> BRONZE_REINFORCED_WOOD = createCasingBlock("bronze_reinforced_wood");
    public static final BlockEntry<Block> STEEL_REINFORCED_WOOD = createCasingBlock("steel_reinforced_wood");
    public static final BlockEntry<Block> IRON_REINFORCED_WOOD = createCasingBlock("iron_reinforced_wood");
    public static final BlockEntry<Block> SOLAR_BOILING_CELL = createSolarCasingBlock("solar_boiling_cell");
    public static final BlockEntry<Block> STRONZE_WRAPPED_CASING = createCasingBlock("stronze_wrapped_casing"); // Usando
                                                                                                                // Bronze
                                                                                                                // como
                                                                                                                // base
                                                                                                                // material
    public static final BlockEntry<Block> HYDRAULIC_ASSEMBLER_CASING = createCasingBlock("hydraulic_assembler_casing");
    public static final BlockEntry<Block> BREEL_PLATED_CASING = createCasingBlock("breel_plated_casing");
    public static final BlockEntry<Block> BOROSILICATE_GLASS_BLOCK = createGlassCasingBlock(
            "borosilicate_glass", GTNACORE.id("block/casings/borosilicate_glass"), () -> RenderType::cutoutMipped);
    public static final BlockEntry<Block> OXIDATION_RESISTANT_HASTELLOY_N_MECHANICAL_CASING = createCasingBlock(
            "oxidation_resistant_hastelloy_n_mechanical_casing",
            GTNACORE.id("block/casings/oxidation_resistant_hastelloy_n_mechanical_casing"));
    public static final BlockEntry<Block> ZIRCONIA_CERAMIC_HIGH_STRENGTH_BENDING_RESISTANCE_MECHANICAL_BLOCK =
            createCasingBlock(
                    "zirconia_ceramic_high_strength_bending_resistance_mechanical_block",
                    GTNACORE.id("block/zirconia_ceramic_high_strength_bending_resistance_mechanical_block"));
    public static final BlockEntry<Block> NAQUADAH_BOROSILICATE_GLASS = createGlassCasingBlock(
            "naquadah_borosilicate_glass",
            GTNACORE.id("block/casings/naquadah_borosilicate_glass"),
            () -> RenderType::cutoutMipped);
    public static final BlockEntry<Block> MAGTECH_CASING = createCasingBlock(
            "magtech_casing",
            GTNACORE.id("block/casings/magtech_casing"));
    public static final BlockEntry<Block> PROCESS_MACHINE_CASING = createCasingBlock(
            "process_machine_casing",
            GTNACORE.id("block/casings/process_machine_casing"));
    public static final BlockEntry<Block> COMPRESSOR_CONTROLLER_CASING = createCasingBlock(
            "compressor_controller_casing",
            GTNACORE.id("block/casings/compressor_controller_casing"));
    public static final BlockEntry<Block> EXTREME_DENSITY_CASING = createCasingBlock(
            "extreme_density_casing",
            GTNACORE.id("block/extreme_density_casing"));
    public static final BlockEntry<Block> GRAVITON_FIELD_CONSTRAINT_CASING = createCasingBlock(
            "graviton_field_constraint_casing");
    public static final BlockEntry<Block> ANNIHILATE_CORE = createCasingBlock("annihilate_core",
            GTNACORE.id("block/annihilate_core"));
    public static final BlockEntry<Block> HYPER_MECHANICAL_CASING = createCasingBlock("hyper_mechanical_casing");
    public static final BlockEntry<Block> HOLLOW_CASING = createCasingBlock("hollow_casing",
            GTNACORE.id("block/hollow_casing"));
    public static final BlockEntry<Block> NAQUADAH_ALLOY_CASING = createCasingBlock("naquadah_alloy_casing",
            GTNACORE.id("block/casings/naquadah_reinforced_plant_casing"));
    public static final BlockEntry<Block> DYSON_CONTROL_TOROID = createCasingBlock("dyson_control_toroid");
    public static final BlockEntry<Block> DYSON_CONTROL_CASING = createCasingBlock("dyson_control_casing");
    public static final BlockEntry<Block> DEGENERATE_RHENIUM_CONSTRAINED_CASING = createCasingBlock(
            "degenerate_rhenium_constrained_casing");
    public static final BlockEntry<Block> DYSON_RECEIVER_CASING = createCasingBlock("dyson_receiver_casing",
            GTNACORE.id("block/casings/cosmic_detection_receiver_material_ray_absorbing_array"));
    public static final BlockEntry<Block> RHENIUM_REINFORCED_ENERGY_GLASS = createGlassCasingBlock(
            "rhenium_reinforced_energy_glass",
            GTNACORE.id("block/casings/rhenium_reinforced_energy_glass"),
            () -> RenderType::cutoutMipped);
    public static final BlockEntry<Block> ANTIMATTER_CONTAINMENT_CASING = createGlassCasingBlock(
            "antimatter_containment_casing",
            GTNACORE.id("block/casings/antimatter_containment_casing"),
            () -> RenderType::cutoutMipped);
    public static final BlockEntry<Block> DIMENSIONALLY_TRANSCENDENT_CASING = createCasingBlock(
            "dimensionally_transcendent_casing");
    public static final BlockEntry<Block> DIMENSION_INJECTION_CASING = createCasingBlock(
            "dimension_injection_casing");
    public static final BlockEntry<Block> DIMENSIONAL_BRIDGE_CASING = createCasingBlock(
            "dimensional_bridge_casing",
            GTNACORE.id("block/dimensional_bridge_casing"));
    public static final BlockEntry<Block> DIMENSIONAL_STABILITY_CASING = createCasingBlock(
            "dimensional_stability_casing",
            GTNACORE.id("block/dimensional_stability_casing"));
    public static final BlockEntry<Block> SPACETIME_COMPRESSION_FIELD_GENERATOR = createCasingBlock(
            "spacetime_compression_field_generator",
            GTNACORE.id("block/spacetime_compression_field_generator"));

    public static final BlockEntry<NexusCapacitorBlock> NEXUS_CAPACITOR_LV = createCapacitorBlock("nexus_capacitor_lv",
            1, 160_000L);
    public static final BlockEntry<NexusCapacitorBlock> NEXUS_CAPACITOR_MV = createCapacitorBlock("nexus_capacitor_mv",
            2, 1_500_000L);
    public static final BlockEntry<NexusCapacitorBlock> NEXUS_CAPACITOR_HV = createCapacitorBlock("nexus_capacitor_hv",
            3, 10_000_000L);
    public static final BlockEntry<NexusCapacitorBlock> NEXUS_CAPACITOR_EV = createCapacitorBlock("nexus_capacitor_ev",
            4, 50_000_000L);
    public static final BlockEntry<NexusCapacitorBlock> NEXUS_CAPACITOR_IV = createCapacitorBlock("nexus_capacitor_iv",
            5, 250_000_000L);
    public static final BlockEntry<NexusCapacitorBlock> NEXUS_CAPACITOR_LUV = createCapacitorBlock(
            "nexus_capacitor_luv", 6, 1_500_000_000L);
    public static final BlockEntry<NexusCapacitorBlock> NEXUS_CAPACITOR_ZPM = createCapacitorBlock(
            "nexus_capacitor_zpm", 7, 15_000_000_000L);
    public static final BlockEntry<NexusCapacitorBlock> NEXUS_CAPACITOR_UV = createCapacitorBlock("nexus_capacitor_uv",
            8, 150_000_000_000L);
    public static final BlockEntry<NexusCapacitorBlock> NEXUS_CAPACITOR_UHV = createCapacitorBlock(
            "nexus_capacitor_uhv", 9, 3_000_000_000_000L);
    public static final BlockEntry<NexusCapacitorBlock> NEXUS_CAPACITOR_UEV = createCapacitorBlock(
            "nexus_capacitor_uev", 10, 50_000_000_000_000L);
    public static final BlockEntry<NexusCapacitorBlock> NEXUS_CAPACITOR_UIV = createCapacitorBlock(
            "nexus_capacitor_uiv", 11, 900_000_000_000_000L);
    public static final BlockEntry<NexusCapacitorBlock> NEXUS_CAPACITOR_UXV = createCapacitorBlock(
            "nexus_capacitor_uxv", 12, 15_000_000_000_000_000L);
    public static final BlockEntry<NexusCapacitorBlock> NEXUS_CAPACITOR_OPV = createCapacitorBlock(
            "nexus_capacitor_opv", 13, 250_000_000_000_000_000L);
    public static final BlockEntry<NexusCapacitorBlock> NEXUS_CAPACITOR_MAX = createCapacitorBlock(
            "nexus_capacitor_max", 14, 5_000_000_000_000_000_000L);

    public static BlockEntry<Block> createCasingBlock(String name) {
        return createCasingBlock(name, Block::new, GTNACORE.id("block/casings/" + name), () -> Blocks.IRON_BLOCK,
                () -> RenderType::solid);
    }

    public static BlockEntry<Block> createCasingBlock(String name, ResourceLocation texture) {
        return createCasingBlock(name, Block::new, texture, () -> Blocks.IRON_BLOCK, () -> RenderType::solid);
    }

    @SuppressWarnings("all")
    public static BlockEntry<Block> createCasingBlock(String name,
                                                      NonNullFunction<BlockBehaviour.Properties, Block> blockSupplier,
                                                      ResourceLocation texture,
                                                      NonNullSupplier<? extends Block> properties,
                                                      Supplier<Supplier<RenderType>> type) {
        return REGISTRATE.block(name, blockSupplier)
                .initialProperties(properties)
                .properties(p -> p
                        .mapColor(MapColor.METAL)
                        .strength(5.0f, 6.0f)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .isValidSpawn((state, level, pos, ent) -> false))
                .addLayer(type)
                .blockstate((ctx, prov) -> prov.simpleBlock(ctx.get(), prov.models().cubeAll(ctx.getName(), texture)))
                .tag(GTToolType.WRENCH.harvestTags.get(0), BlockTags.MINEABLE_WITH_PICKAXE)
                .item(BlockItem::new)
                .build()
                .register();
    }

    private static BlockEntry<Block> createSolarCasingBlock(String name) {
        return REGISTRATE.block(name, Block::new)
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .properties(p -> p.mapColor(MapColor.METAL).strength(5.0f, 6.0f).sound(SoundType.METAL)
                        .requiresCorrectToolForDrops())
                .addLayer(() -> RenderType::solid)
                .blockstate((ctx, prov) -> {
                    prov.simpleBlock(ctx.get(), prov.models().cube(ctx.getName(),
                            prov.modLoc("block/casings/" + name), // down
                            prov.modLoc("block/overlay/block/solar_boiling_cell_top_overlay"), // up
                            prov.modLoc("block/casings/" + name), // north
                            prov.modLoc("block/casings/" + name), // south
                            prov.modLoc("block/casings/" + name), // east
                            prov.modLoc("block/casings/" + name)  // west
                    ));
                })
                .tag(GTToolType.WRENCH.harvestTags.get(0), BlockTags.MINEABLE_WITH_PICKAXE)
                .item(BlockItem::new)

                .build()
                .register();
    }

    private static BlockEntry<Block> createGlassCasingBlock(String name, ResourceLocation texture,
                                                            Supplier<Supplier<RenderType>> type) {
        return createCasingBlock(name, GlassBlock::new, texture, () -> Blocks.GLASS, type);
    }

    public static BlockEntry<NexusCapacitorBlock> createCapacitorBlock(String name, int tier, long capacity) {
        return REGISTRATE.block(name, properties -> new NexusCapacitorBlock(properties, tier, capacity))
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .properties(p -> p
                        .mapColor(MapColor.METAL)
                        .strength(5.0f, 6.0f)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .isValidSpawn((state, level, pos, ent) -> false))
                .addLayer(() -> RenderType::solid)
                .blockstate((ctx, prov) -> prov.simpleBlock(ctx.get(),
                        prov.models().cubeAll(ctx.getName(),
                                GTNACORE.id("block/wireless_energy_unit/" + name.replace("nexus_capacitor_", "")))))
                .tag(GTToolType.WRENCH.harvestTags.get(0), BlockTags.MINEABLE_WITH_PICKAXE)
                .item(BlockItem::new)
                .build()
                .register();
    }
}
