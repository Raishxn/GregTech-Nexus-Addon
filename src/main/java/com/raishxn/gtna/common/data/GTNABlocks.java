package com.raishxn.gtna.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GlassBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.common.block.GTNABlockItem;
import com.raishxn.gtna.common.block.MEStorageCoreBlock;
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
    public static final BlockEntry<Block> STEAM_ASSEMBLY_BLOCK = createCasingBlock(
            "steam_assembly_block",
            GTNACORE.id("block/steam_assembly_block"));
    public static final BlockEntry<Block> VIBRATION_SAFE_CASING = createCasingBlock("vibration_safe_casing");
    /**
     * GTNL {@code GTNLCasings.IndustrialSteamCasing} (metaCasing02:1). Shell of the bronze-tier
     * large steam multiblocks; texture from the Modernity-GTNH pack (same author as GTNL).
     */
    public static final BlockEntry<Block> INDUSTRIAL_STEAM_CASING = createCasingBlock("industrial_steam_casing");
    /**
     * GTNL {@code GTNLCasings.AdvancedIndustrialSteamCasing} (metaCasing02:2). Shell of the steel-tier
     * (high pressure) large steam multiblocks; texture from the Modernity-GTNH pack.
     */
    public static final BlockEntry<Block> ADVANCED_INDUSTRIAL_STEAM_CASING = createCasingBlock(
            "advanced_industrial_steam_casing");
    public static final BlockEntry<Block> BRONZE_REINFORCED_WOOD = createCasingBlock("bronze_reinforced_wood");
    public static final BlockEntry<Block> BRASS_REINFORCED_WOODEN_CASING = createCasingBlock(
            "brass_reinforced_wooden_casing",
            GTNACORE.id("block/casings/brass_reinforced_wooden_casing"));
    public static final BlockEntry<Block> ALUMINIUM_BRONZE_CASING = createCasingBlock(
            "aluminium_bronze_casing",
            GTNACORE.id("block/casings/aluminium_bronze_casing"));
    public static final BlockEntry<Block> STAINLESS_EVAPORATION_CASING = createCasingBlock(
            "stainless_evaporation_casing",
            GTNACORE.id("block/casings/stainless_evaporation_casing"));
    public static final BlockEntry<Block> MULTI_FUNCTIONAL_CASING = createCasingBlock("multi_functional_casing");
    /**
     * GTOCore Inconel-625 family (ISA Mill shell, gearbox and pipe). The textures keep GTOCore's
     * relative paths, so the pipe/gearbox live under {@code block/} and the casing under
     * {@code block/casings/}.
     */
    public static final BlockEntry<Block> INCONEL_625_CASING = createCasingBlock("inconel_625_casing",
            GTNACORE.id("block/casings/inconel_625_casing"));
    public static final BlockEntry<Block> INCONEL_625_GEARBOX = createCasingBlock("inconel_625_gearbox",
            GTNACORE.id("block/inconel_625_gearbox"));
    public static final BlockEntry<Block> INCONEL_625_PIPE = createCasingBlock("inconel_625_pipe",
            GTNACORE.id("block/inconel_625_pipe"));
    public static final BlockEntry<Block> BLAZE_CASING = createTwoLayerCasingBlock("blaze_casing");
    public static final BlockEntry<Block> COLD_ICE_CASING = createTwoLayerCasingBlock("cold_ice_casing");
    public static final BlockEntry<Block> COMPONENT_ASSEMBLY_CASING_LV = createCasingBlock(
            "component_assembly_casing_lv");
    public static final BlockEntry<Block> COMPONENT_ASSEMBLY_CASING_MV = createCasingBlock(
            "component_assembly_casing_mv");
    public static final BlockEntry<Block> COMPONENT_ASSEMBLY_CASING_HV = createCasingBlock(
            "component_assembly_casing_hv");
    public static final BlockEntry<Block> COMPONENT_ASSEMBLY_CASING_EV = createCasingBlock(
            "component_assembly_casing_ev");
    public static final BlockEntry<Block> COMPONENT_ASSEMBLY_CASING_IV = createCasingBlock(
            "component_assembly_casing_iv");
    /**
     * GTOCore {@code component_assembly_line_casing_luv} (CC BY-NC-SA 4.0): the LuV member of the
     * Component Assembler casing family, reached only with the large extension formed. The texture
     * keeps GTOCore's {@code component_assembly_line} sheet with the GTNA family name.
     */
    public static final BlockEntry<Block> COMPONENT_ASSEMBLY_CASING_LUV = createCasingBlock(
            "component_assembly_casing_luv");
    /**
     * GTOCore {@code component_assembly_line_casing_zpm} (CC BY-NC-SA 4.0): the ZPM member of the
     * Component Assembler casing family, reached only with the large extension formed.
     */
    public static final BlockEntry<Block> COMPONENT_ASSEMBLY_CASING_ZPM = createCasingBlock(
            "component_assembly_casing_zpm");
    /**
     * GTOCore {@code component_assembly_line_casing_uv} (CC BY-NC-SA 4.0): the UV member of the
     * Component Assembler casing family, the top of the GTNA extension port.
     */
    public static final BlockEntry<Block> COMPONENT_ASSEMBLY_CASING_UV = createCasingBlock(
            "component_assembly_casing_uv");
    public static final BlockEntry<Block> STEEL_REINFORCED_WOOD = createCasingBlock("steel_reinforced_wood");
    public static final BlockEntry<Block> IRON_REINFORCED_WOOD = createCasingBlock("iron_reinforced_wood");
    public static final BlockEntry<Block> SOLAR_BOILING_CELL = createSolarCasingBlock("solar_boiling_cell");
    public static final BlockEntry<Block> SOLAR_HEAT_COLLECTOR_PIPE_CASING = createSolarCasingBlock(
            "solar_heat_collector_pipe_casing");
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
    public static final BlockEntry<Block> ZIRCONIA_CERAMIC_HIGH_STRENGTH_BENDING_RESISTANCE_MECHANICAL_BLOCK = createCasingBlock(
            "zirconia_ceramic_high_strength_bending_resistance_mechanical_block",
            GTNACORE.id("block/zirconia_ceramic_high_strength_bending_resistance_mechanical_block"));
    public static final BlockEntry<Block> HIGH_STRENGTH_CONCRETE = createCasingBlock(
            "high_strength_concrete",
            GTNACORE.id("block/casings/high_strength_concrete"));
    public static final BlockEntry<Block> COBALT_OXIDE_CERAMIC_STRONG_THERMALLY_CONDUCTIVE_MECHANICAL_BLOCK = createCasingBlock(
            "cobalt_oxide_ceramic_strong_thermally_conductive_mechanical_block",
            GTNACORE.id("block/casings/cobalt_oxide_ceramic_strong_thermally_conductive_mechanical_block"));
    public static final BlockEntry<Block> LITHIUM_OXIDE_CERAMIC_HEAT_RESISTANT_SHOCK_RESISTANT_MECHANICAL_CUBE = createCasingBlock(
            "lithium_oxide_ceramic_heat_resistant_shock_resistant_mechanical_cube",
            GTNACORE.id("block/casings/lithium_oxide_ceramic_heat_resistant_shock_resistant_mechanical_cube"));
    public static final BlockEntry<Block> NAQUADAH_BOROSILICATE_GLASS = createGlassCasingBlock(
            "naquadah_borosilicate_glass",
            GTNACORE.id("block/casings/naquadah_borosilicate_glass"),
            () -> RenderType::cutoutMipped);
    public static final BlockEntry<Block> MAGTECH_CASING = createCasingBlock(
            "magtech_casing",
            GTNACORE.id("block/casings/magtech_casing"));
    public static final BlockEntry<Block> UNIVERSAL_FACTORY_CASING = createCasingBlock(
            "universal_factory_casing",
            GTNACORE.id("block/casings/universal_factory_casing"));
    public static final BlockEntry<Block> ABS_BLACK_CASING = createCasingBlock(
            "abs_black_casing",
            GTNACORE.id("block/casings/abs_black_casing"));
    public static final BlockEntry<Block> ABS_BLUE_CASING = createCasingBlock(
            "abs_blue_casing",
            GTNACORE.id("block/casings/abs_blue_casing"));
    public static final BlockEntry<Block> ABS_BROWN_CASING = createCasingBlock(
            "abs_brown_casing",
            GTNACORE.id("block/casings/abs_brown_casing"));
    public static final BlockEntry<Block> ABS_GREEN_CASING = createCasingBlock(
            "abs_green_casing",
            GTNACORE.id("block/casings/abs_green_casing"));
    public static final BlockEntry<Block> ABS_GREY_CASING = createCasingBlock(
            "abs_grey_casing",
            GTNACORE.id("block/casings/abs_grey_casing"));
    public static final BlockEntry<Block> ABS_LIME_CASING = createCasingBlock(
            "abs_lime_casing",
            GTNACORE.id("block/casings/abs_lime_casing"));
    public static final BlockEntry<Block> ABS_ORANGE_CASING = createCasingBlock(
            "abs_orange_casing",
            GTNACORE.id("block/casings/abs_orange_casing"));
    public static final BlockEntry<Block> ABS_RED_CASING = createCasingBlock(
            "abs_red_casing",
            GTNACORE.id("block/casings/abs_red_casing"));
    public static final BlockEntry<Block> ABS_WHITE_CASING = createCasingBlock(
            "abs_white_casing",
            GTNACORE.id("block/casings/abs_white_casing"));
    public static final BlockEntry<Block> ABS_YELLOW_CASING = createCasingBlock(
            "abs_yellow_casing",
            GTNACORE.id("block/casings/abs_yellow_casing"));
    public static final BlockEntry<Block> ABS_CYAN_CASING = createCasingBlock(
            "abs_cyan_casing",
            GTNACORE.id("block/casings/abs_cyan_casing"));
    public static final BlockEntry<Block> ABS_MAGENTA_CASING = createCasingBlock(
            "abs_magenta_casing",
            GTNACORE.id("block/casings/abs_magenta_casing"));
    public static final BlockEntry<Block> ABS_PINK_CASING = createCasingBlock(
            "abs_pink_casing",
            GTNACORE.id("block/casings/abs_pink_casing"));
    public static final BlockEntry<Block> ABS_PURPLE_CASING = createCasingBlock(
            "abs_purple_casing",
            GTNACORE.id("block/casings/abs_purple_casing"));
    public static final BlockEntry<Block> ABS_LIGHT_BULL_CASING = createCasingBlock(
            "abs_light_bull_casing",
            GTNACORE.id("block/casings/abs_light_bull_casing"));
    public static final BlockEntry<Block> ABS_LIGHT_GREY_CASING = createCasingBlock(
            "abs_light_grey_casing",
            GTNACORE.id("block/casings/abs_light_grey_casing"));
    public static final BlockEntry<Block> RESTRAINT_DEVICE = createCasingBlock(
            "restraint_device",
            GTNACORE.id("block/restraint_device"));
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
    /**
     * GTOCore {@code naquadah_alloy_casing} (CC BY-NC-SA 4.0). GTO reuses the
     * {@code hyper_mechanical_casing} sheet for this block (the earlier GTNA copy pointed at the
     * water-purification {@code naquadah_reinforced_plant_casing} sheet by mistake).
     */
    public static final BlockEntry<Block> NAQUADAH_ALLOY_CASING = createCasingBlock("naquadah_alloy_casing",
            GTNACORE.id("block/casings/hyper_mechanical_casing"));
    /** GTOCore {@code iridium_casing} (CC BY-NC-SA 4.0): the Component Assembly Line shell. */
    public static final BlockEntry<Block> IRIDIUM_CASING = createCasingBlock("iridium_casing");
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
    public static final BlockEntry<Block> NEXUS_HYPERCORE_CASING = createCasingBlock(
            "nexus_hypercore_casing",
            GTCEu.id("block/casings/gcym/nonconducting_casing"));
    public static final BlockEntry<Block> MATRIX_MODULE_I = createCasingBlock(
            "matrix_module_i",
            GTNACORE.id("block/wireless_energy_unit/ev"));
    public static final BlockEntry<Block> MATRIX_MODULE_II = createCasingBlock(
            "matrix_module_ii",
            GTNACORE.id("block/wireless_energy_unit/luv"));
    public static final BlockEntry<Block> MATRIX_MODULE_III = createCasingBlock(
            "matrix_module_iii",
            GTNACORE.id("block/wireless_energy_unit/uv"));
    public static final BlockEntry<Block> MATRIX_MODULE_IV = createCasingBlock(
            "matrix_module_iv",
            GTNACORE.id("block/wireless_energy_unit/uev"));

    /**
     * GTOCore {@code supercritical_turbine_casing} (CC BY-NC-SA 4.0): the wall of the non-mega
     * Supercritical Steam Turbine and of its SUPERCRITICAL module. The texture, its connected
     * texture sheet and the {@code .png.mcmeta} are copied from {@code gtocore} with the namespace
     * rewritten to {@code gtna}.
     */
    public static final BlockEntry<Block> SUPERCRITICAL_TURBINE_CASING = createCasingBlock(
            "supercritical_turbine_casing");

    /**
     * GTOCore {@code hastelloy_n_75_casing} (CC BY-NC-SA 4.0): the shell of the Industrial Flotation
     * Cell. The texture, its connected texture sheet and the {@code .png.mcmeta} are copied from
     * {@code gtocore} with the namespace rewritten to {@code gtna}.
     */
    public static final BlockEntry<Block> HASTELLOY_N_75_CASING = createCasingBlock(
            "hastelloy_n_75_casing");
    /**
     * GTOCore {@code hastelloy_n_75_gearbox} (CC BY-NC-SA 4.0): the flotation cell's central
     * gearbox. GTOCore keeps this texture under {@code block/}, not {@code block/casings/}.
     */
    public static final BlockEntry<Block> HASTELLOY_N_75_GEARBOX = createCasingBlock(
            "hastelloy_n_75_gearbox", GTNACORE.id("block/hastelloy_n_75_gearbox"));
    /** GTOCore {@code hastelloy_n_75_pipe} (CC BY-NC-SA 4.0): the cell's aeration pipe. */
    public static final BlockEntry<Block> HASTELLOY_N_75_PIPE = createCasingBlock(
            "hastelloy_n_75_pipe", GTNACORE.id("block/hastelloy_n_75_pipe"));
    /** GTOCore {@code flotation_cell} (CC BY-NC-SA 4.0): the flotation tank walls. */
    public static final BlockEntry<Block> FLOTATION_CELL = createCasingBlock(
            "flotation_cell", GTNACORE.id("block/flotation_cell"));
    /**
     * GTOCore {@code red_steel_casing} (CC BY-NC-SA 4.0): the Vacuum Drying Furnace shell. The
     * texture, its connected texture sheet and the {@code .png.mcmeta} are copied from
     * {@code gtocore} with the namespace rewritten to {@code gtna}.
     */
    public static final BlockEntry<Block> RED_STEEL_CASING = createCasingBlock(
            "red_steel_casing");

    /**
     * GTOCore {@code three_proof_computer_casing} (CC BY-NC-SA 4.0): the computer wall of the
     * Component Assembler's large extension. Texture, connected sheet and {@code .png.mcmeta} copied
     * from {@code gtocore} with the namespace rewritten to {@code gtna}.
     */
    public static final BlockEntry<Block> THREE_PROOF_COMPUTER_CASING = createCasingBlock(
            "three_proof_computer_casing");
    /**
     * GTOCore {@code machining_control_casing_mk2} (CC BY-NC-SA 4.0): the sided machining-control
     * wall of the Component Assembler's large extension. Sided base sheet, connected sheet,
     * {@code .png.mcmeta} and the emissive {@code side_bloom} overlay copied from {@code gtocore}
     * with the namespace rewritten to {@code gtna}; rendered with GTCEu's
     * {@code cube_2_layer/bottom_top} model.
     */
    public static final BlockEntry<Block> MACHINING_CONTROL_CASING_MK2 = createSidedCasingBlock(
            "machining_control_casing_mk2");
    /**
     * GTOCore {@code energy_control_casing_mk2} (CC BY-NC-SA 4.0): the sided energy-control wall of
     * the Component Assembler's large extension. Same two-layer treatment as the machining casing.
     */
    public static final BlockEntry<Block> ENERGY_CONTROL_CASING_MK2 = createSidedCasingBlock(
            "energy_control_casing_mk2");
    /**
     * GTOCore {@code electric_power_transmission_casing} (CC BY-NC-SA 4.0): the power line wall of
     * the Component Assembler's large extension.
     */
    public static final BlockEntry<Block> ELECTRIC_POWER_TRANSMISSION_CASING = createCasingBlock(
            "electric_power_transmission_casing");
    /**
     * GTOCore {@code titanium_nitride_ceramic_impact_resistant_mechanical_block} (CC BY-NC-SA 4.0):
     * the impact-resistant ceramic wall of the Component Assembler extension. GTOCore keeps this
     * texture under {@code block/}, not {@code block/casings/}; the GTNA copy lives with the casings.
     */
    public static final BlockEntry<Block> TITANIUM_NITRIDE_CERAMIC_IMPACT_RESISTANT_MECHANICAL_BLOCK = createCasingBlock(
            "titanium_nitride_ceramic_impact_resistant_mechanical_block");
    /** GTOCore Component Assembly Line casings (CC BY-NC-SA 4.0). */
    public static final BlockEntry<Block> MOLECULAR_CASING = createTwoLayerCasingBlock("molecular_casing");
    public static final BlockEntry<Block> BORON_CARBIDE_CERAMIC_RADIATION_RESISTANT_MECHANICAL_CUBE = createCasingBlock(
            "boron_carbide_ceramic_radiation_resistant_mechanical_cube");
    public static final BlockEntry<Block> PRECISION_PROCESSING_MECHANICAL_CASING = createCasingBlock(
            "precision_processing_mechanical_casing");
    public static final BlockEntry<Block> ADVANCED_ASSEMBLY_LINE_UNIT = createCasingBlock(
            "advanced_assembly_line_unit");
    public static final BlockEntry<Block> CHEMICAL_CORROSION_RESISTANT_PIPE_CASING = createCasingBlock(
            "chemical_corrosion_resistant_pipe_casing");
    public static final BlockEntry<Block> MACHINE_CASING_CIRCUIT_ASSEMBLY_LINE = createCasingBlock(
            "machine_casing_circuit_assembly_line");
    public static final BlockEntry<Block> SPACETIME_ASSEMBLY_LINE_UNIT = createCasingBlock(
            "spacetime_assembly_line_unit");
    public static final BlockEntry<Block> PRESSURE_CONTAINMENT_CASING = createCasingBlock(
            "pressure_containment_casing");
    /**
     * GTOCore {@code component_assembly_line_casing_lv..uv} (CC BY-NC-SA 4.0): the tier casing
     * family of the {@code component_assembly_line}. GTNA ports LV..UV; the UHV..MAX tiers stay
     * documented as out of scope. Textures keep GTOCore's
     * {@code block/casings/component_assembly_line/} path.
     */
    public static final BlockEntry<Block> COMPONENT_ASSEMBLY_LINE_CASING_LV = createCasingBlock(
            "component_assembly_line_casing_lv",
            GTNACORE.id("block/casings/component_assembly_line/component_assembly_line_casing_lv"));
    public static final BlockEntry<Block> COMPONENT_ASSEMBLY_LINE_CASING_MV = createCasingBlock(
            "component_assembly_line_casing_mv",
            GTNACORE.id("block/casings/component_assembly_line/component_assembly_line_casing_mv"));
    public static final BlockEntry<Block> COMPONENT_ASSEMBLY_LINE_CASING_HV = createCasingBlock(
            "component_assembly_line_casing_hv",
            GTNACORE.id("block/casings/component_assembly_line/component_assembly_line_casing_hv"));
    public static final BlockEntry<Block> COMPONENT_ASSEMBLY_LINE_CASING_EV = createCasingBlock(
            "component_assembly_line_casing_ev",
            GTNACORE.id("block/casings/component_assembly_line/component_assembly_line_casing_ev"));
    public static final BlockEntry<Block> COMPONENT_ASSEMBLY_LINE_CASING_IV = createCasingBlock(
            "component_assembly_line_casing_iv",
            GTNACORE.id("block/casings/component_assembly_line/component_assembly_line_casing_iv"));
    public static final BlockEntry<Block> COMPONENT_ASSEMBLY_LINE_CASING_LUV = createCasingBlock(
            "component_assembly_line_casing_luv",
            GTNACORE.id("block/casings/component_assembly_line/component_assembly_line_casing_luv"));
    public static final BlockEntry<Block> COMPONENT_ASSEMBLY_LINE_CASING_ZPM = createCasingBlock(
            "component_assembly_line_casing_zpm",
            GTNACORE.id("block/casings/component_assembly_line/component_assembly_line_casing_zpm"));
    public static final BlockEntry<Block> COMPONENT_ASSEMBLY_LINE_CASING_UV = createCasingBlock(
            "component_assembly_line_casing_uv",
            GTNACORE.id("block/casings/component_assembly_line/component_assembly_line_casing_uv"));

    public static final BlockEntry<MEStorageCoreBlock> T1_ME_STORAGE_CORE = createMEStorageCore(1, false);
    public static final BlockEntry<MEStorageCoreBlock> T2_ME_STORAGE_CORE = createMEStorageCore(2, false);
    public static final BlockEntry<MEStorageCoreBlock> T3_ME_STORAGE_CORE = createMEStorageCore(3, false);
    public static final BlockEntry<MEStorageCoreBlock> T4_ME_STORAGE_CORE = createMEStorageCore(4, false);
    public static final BlockEntry<MEStorageCoreBlock> T5_ME_STORAGE_CORE = createMEStorageCore(5, false);
    public static final BlockEntry<MEStorageCoreBlock> T1_CRAFTING_STORAGE_CORE = createMEStorageCore(1, true);
    public static final BlockEntry<MEStorageCoreBlock> T2_CRAFTING_STORAGE_CORE = createMEStorageCore(2, true);
    public static final BlockEntry<MEStorageCoreBlock> T3_CRAFTING_STORAGE_CORE = createMEStorageCore(3, true);
    public static final BlockEntry<MEStorageCoreBlock> T4_CRAFTING_STORAGE_CORE = createMEStorageCore(4, true);
    public static final BlockEntry<MEStorageCoreBlock> T5_CRAFTING_STORAGE_CORE = createMEStorageCore(5, true);

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

    /**
     * A GTOCore-style sided control casing: one texture for the top face and one for the sides, under
     * {@code block/casings/sided/<name>/}. GTNA's {@link #createCasingBlock} only emits cube-all
     * models, so this helper reproduces the sided blockstate with the same tags and item-model
     * conventions.
     */
    /**
     * GTOCore two-layer (bloom) casing: the base texture plus the emissive {@code _bloom} overlay,
     * both connected through their {@code .png.mcmeta} (CC BY-NC-SA 4.0 from GTOCore). GTCEu's
     * {@code cube_2_layer/all} parent renders the pair, exactly like the original.
     */
    public static BlockEntry<Block> createTwoLayerCasingBlock(String name) {
        ResourceLocation base = GTNACORE.id("block/casings/" + name);
        ResourceLocation bloom = GTNACORE.id("block/casings/" + name + "_bloom");
        return REGISTRATE.block(name, Block::new)
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .properties(p -> p
                        .mapColor(MapColor.METAL)
                        .strength(5.0f, 6.0f)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .isValidSpawn((state, level, pos, ent) -> false))
                .addLayer(() -> RenderType::cutoutMipped)
                .blockstate((ctx, prov) -> prov.simpleBlock(ctx.get(),
                        prov.models().withExistingParent(ctx.getName(), GTCEu.id("block/cube_2_layer/all"))
                                .texture("bot_all", base)
                                .texture("top_all", bloom)))
                .tag(GTToolType.WRENCH.harvestTags.get(0), BlockTags.MINEABLE_WITH_PICKAXE)
                .item(GTNABlockItem::new)
                .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), prov.modLoc("block/" + ctx.getName())))
                .build()
                .register();
    }

    public static BlockEntry<Block> createSidedCasingBlock(String name) {
        ResourceLocation side = GTNACORE.id("block/casings/sided/" + name + "/side");
        ResourceLocation top = GTNACORE.id("block/casings/sided/" + name + "/top");
        ResourceLocation bloom = GTNACORE.id("block/casings/sided/" + name + "/side_bloom");
        return REGISTRATE.block(name, Block::new)
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .properties(p -> p
                        .mapColor(MapColor.METAL)
                        .strength(5.0f, 6.0f)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .isValidSpawn((state, level, pos, ent) -> false))
                .addLayer(() -> RenderType::cutoutMipped)
                .blockstate((ctx, prov) -> prov.simpleBlock(ctx.get(),
                        prov.models().withExistingParent(ctx.getName(), GTCEu.id("block/cube_2_layer/bottom_top"))
                                .texture("bot_side", side)
                                .texture("bot_top", top)
                                .texture("bot_bottom", top)
                                .texture("top_side", bloom)
                                .texture("top_top", top)
                                .texture("top_bottom", top)))
                .tag(GTToolType.WRENCH.harvestTags.get(0), BlockTags.MINEABLE_WITH_PICKAXE)
                .item(GTNABlockItem::new)
                .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), prov.modLoc("block/" + ctx.getName())))
                .build()
                .register();
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
                .item(GTNABlockItem::new)
                .model((ctx, prov) -> {
                    if ("borosilicate_glass".equals(ctx.getName())) {
                        prov.withExistingParent(ctx.getName(), new ResourceLocation("minecraft", "block/cube_all"))
                                .texture("all", prov.modLoc("block/casings/borosilicate_glass_inventory"));
                    } else {
                        prov.withExistingParent(ctx.getName(), prov.modLoc("block/" + ctx.getName()));
                    }
                })
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
                    ).texture("particle", prov.modLoc("block/casings/" + name)));
                })
                .tag(GTToolType.WRENCH.harvestTags.get(0), BlockTags.MINEABLE_WITH_PICKAXE)
                .item(GTNABlockItem::new)

                .build()
                .register();
    }

    private static BlockEntry<Block> createGlassCasingBlock(String name, ResourceLocation texture,
                                                            Supplier<Supplier<RenderType>> type) {
        return createCasingBlock(name, GlassBlock::new, texture, () -> Blocks.GLASS, type);
    }

    private static BlockEntry<MEStorageCoreBlock> createMEStorageCore(int tier, boolean craftingCore) {
        String name = "t" + tier + (craftingCore ? "_crafting_storage_core" : "_me_storage_core");
        String texture = "block/casings/" + (craftingCore ? "crafting_storage_core" : "me_storage_core") + "/t" + tier;
        return REGISTRATE.block(name, p -> new MEStorageCoreBlock(p, tier, craftingCore))
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .properties(p -> p
                        .mapColor(MapColor.METAL)
                        .strength(5.0f, 6.0f)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .isValidSpawn((state, level, pos, ent) -> false))
                .addLayer(() -> RenderType::solid)
                .blockstate((ctx, prov) -> prov.simpleBlock(ctx.get(), prov.models().cubeAll(ctx.getName(),
                        GTNACORE.id(texture))))
                .tag(GTToolType.WRENCH.harvestTags.get(0), BlockTags.MINEABLE_WITH_PICKAXE)
                .item(GTNABlockItem::new)
                .build()
                .register();
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
                .item(GTNABlockItem::new)
                .build()
                .register();
    }
}
