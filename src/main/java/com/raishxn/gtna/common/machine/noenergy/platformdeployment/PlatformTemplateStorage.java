package com.raishxn.gtna.common.machine.noenergy.platformdeployment;

import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

import static com.raishxn.gtna.common.machine.noenergy.platformdeployment.PlatformBlockType.PlatformBlockStructure.structure;
import static com.raishxn.gtna.common.machine.noenergy.platformdeployment.PlatformBlockType.PlatformPreset.preset;

public final class PlatformTemplateStorage {

    private static final List<PlatformBlockType.PlatformPreset> PRESETS = new ArrayList<>();

    private static final String PLATFORM = "Platform";
    private static final String PLATFORM_3_3 = "Platform (3x3)";
    private static final String PLATFORM_LARGE = "Platform (Large)";
    private static final String ROAD = "Road";
    private static final String FACTORY = "Factory";

    private PlatformTemplateStorage() {}

    public static List<PlatformBlockType.PlatformPreset> initializePresets() {
        return PRESETS;
    }

    private static ResourceLocation platform(String name) {
        return PlatformSupport.id("platforms/" + name);
    }

    private static String pretty(String name) {
        String[] parts = name.split("_");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) continue;
            if (builder.length() > 0) builder.append(' ');
            builder.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return builder.toString();
    }

    private static PlatformBlockType.PlatformBlockStructure eppFactory(String name) {
        return structure(name)
                .type(FACTORY)
                .displayName(pretty(name))
                .resource(platform("epp/sy_1/" + name))
                .symbolMap(platform("epp/sy_1/" + name + ".json"))
                .materials(0, 800)
                .materials(1, 800)
                .build();
    }

    static {
        PRESETS.add(
                preset("platform_standard_library")
                        .displayName("Platform Standard Library")
                        .description("Starter platform and flooring presets")
                        .addStructure(structure("high_saturation_chessboard_1_blue_pink")
                                .type(PLATFORM)
                                .displayName("High Saturation Chessboard")
                                .description("1x1 blue and pink")
                                .resource(platform("high_saturation_chessboard_1"))
                                .symbolMap(platform("high_saturation_chessboard_blue_pink.json"))
                                .materials(0, 144)
                                .build())
                        .addStructure(structure("high_saturation_chessboard_1_orange_white")
                                .type(PLATFORM)
                                .displayName("High Saturation Chessboard")
                                .description("1x1 orange and white")
                                .resource(platform("high_saturation_chessboard_1"))
                                .symbolMap(platform("high_saturation_chessboard_orange_white.json"))
                                .materials(0, 144)
                                .build())
                        .addStructure(structure("high_saturation_chessboard_3_blue_pink")
                                .type(PLATFORM_3_3)
                                .displayName("High Saturation Chessboard")
                                .description("3x3 blue and pink")
                                .resource(platform("high_saturation_chessboard_3"))
                                .symbolMap(platform("high_saturation_chessboard_blue_pink.json"))
                                .materials(0, 1296)
                                .build())
                        .addStructure(structure("high_saturation_chessboard_3_orange_white")
                                .type(PLATFORM_3_3)
                                .displayName("High Saturation Chessboard")
                                .description("3x3 orange and white")
                                .resource(platform("high_saturation_chessboard_3"))
                                .symbolMap(platform("high_saturation_chessboard_orange_white.json"))
                                .materials(0, 1296)
                                .build())
                        .addStructure(structure("high_saturation_chessboard_3_yellow_lime")
                                .type(PLATFORM_3_3)
                                .displayName("High Saturation Chessboard")
                                .description("3x3 yellow and lime")
                                .resource(platform("high_saturation_chessboard_3"))
                                .symbolMap(platform("high_saturation_chessboard_yellow_lime.json"))
                                .materials(0, 1296)
                                .build())
                        .addStructure(structure("high_saturation_panel_1_white_pink")
                                .type(PLATFORM)
                                .displayName("High Saturation Panel")
                                .description("1x1 white with pink inlay")
                                .resource(platform("high_saturation_panel_1"))
                                .symbolMap(platform("high_saturation_panel_white_pink.json"))
                                .materials(0, 144)
                                .build())
                        .addStructure(structure("high_saturation_panel_1_black_blue")
                                .type(PLATFORM)
                                .displayName("High Saturation Panel")
                                .description("1x1 black with blue inlay")
                                .resource(platform("high_saturation_panel_1"))
                                .symbolMap(platform("high_saturation_panel_black_blue.json"))
                                .materials(0, 144)
                                .build())
                        .addStructure(structure("high_saturation_panel_3_white_pink")
                                .type(PLATFORM_3_3)
                                .displayName("High Saturation Panel")
                                .description("3x3 white with pink inlay")
                                .resource(platform("high_saturation_panel_3"))
                                .symbolMap(platform("high_saturation_panel_white_pink.json"))
                                .materials(0, 1296)
                                .build())
                        .addStructure(structure("high_saturation_panel_3_black_blue")
                                .type(PLATFORM_3_3)
                                .displayName("High Saturation Panel")
                                .description("3x3 black with blue inlay")
                                .resource(platform("high_saturation_panel_3"))
                                .symbolMap(platform("high_saturation_panel_black_blue.json"))
                                .materials(0, 1296)
                                .build())
                        .addStructure(structure("white_floor_with_greenery_and_orange_and_yellow_edges")
                                .type(PLATFORM_LARGE)
                                .displayName("Greenery Floor")
                                .description("2x2 white floor with greenery and accent edges")
                                .resource(platform("white_floor_with_greenery_and_orange_and_yellow_edges"))
                                .symbolMap(platform("white_floor_with_greenery_and_orange_and_yellow_edges.json"))
                                .materials(0, 576)
                                .build())
                        .build());

        PRESETS.add(
                preset("platform_extension_library")
                        .displayName("Platform Extension Library")
                        .description("Road and illuminated floor presets")
                        .addStructure(structure("light_colored_road_floor_1")
                                .type(PLATFORM)
                                .displayName("Light Colored Road Floor")
                                .resource(platform("light_colored_road_floor_1"))
                                .symbolMap(platform("light_colored_road_floor_1.json"))
                                .materials(0, 100)
                                .build())
                        .addStructure(structure("light_colored_road_floor_2")
                                .type(ROAD)
                                .displayName("Light Colored Road Floor")
                                .resource(platform("light_colored_road_floor_2"))
                                .symbolMap(platform("light_colored_road_floor_2.json"))
                                .materials(0, 20)
                                .build())
                        .addStructure(structure("light_colored_road_floor_3")
                                .type(ROAD)
                                .displayName("Light Colored Road Floor")
                                .resource(platform("light_colored_road_floor_3"))
                                .symbolMap(platform("light_colored_road_floor_3.json"))
                                .materials(0, 40)
                                .build())
                        .addStructure(structure("light_colored_road_floor_4")
                                .type(ROAD)
                                .displayName("Light Colored Road Floor")
                                .resource(platform("light_colored_road_floor_4"))
                                .symbolMap(platform("light_colored_road_floor_4.json"))
                                .materials(0, 80)
                                .build())
                        .addStructure(structure("gray_floor_with_lights_1")
                                .type(PLATFORM)
                                .displayName("Gray Floor With Lights")
                                .resource(platform("gray_floor_with_lights_1"))
                                .symbolMap(platform("gray_floor_with_lights_1.json"))
                                .materials(2, 100)
                                .build())
                        .addStructure(structure("gray_floor_with_lights_2")
                                .type(PLATFORM)
                                .displayName("Gray Floor With Lights")
                                .resource(platform("gray_floor_with_lights_2"))
                                .symbolMap(platform("gray_floor_with_lights_2.json"))
                                .materials(2, 196)
                                .build())
                        .addStructure(structure("gray_floor_with_lights_3")
                                .type(PLATFORM)
                                .displayName("Gray Floor With Lights")
                                .resource(platform("gray_floor_with_lights_3"))
                                .symbolMap(platform("gray_floor_with_lights_3.json"))
                                .materials(2, 400)
                                .build())
                        .addStructure(structure("gray_floor_with_lights_4")
                                .type(PLATFORM_LARGE)
                                .displayName("Gray Floor With Lights")
                                .resource(platform("gray_floor_with_lights_4"))
                                .symbolMap(platform("gray_floor_with_lights_4.json"))
                                .materials(2, 676)
                                .build())
                        .build());

        PRESETS.add(
                preset("factory_standard_library")
                        .displayName("Factory Standard Library")
                        .description("Core GTO factory shells")
                        .addStructure(structure("standard_factory_building")
                                .type(FACTORY)
                                .displayName("Standard Factory Building")
                                .resource(platform("standard_factory_building"))
                                .symbolMap(platform("standard_factory_building.json"))
                                .materials(0, 400)
                                .materials(1, 100)
                                .build())
                        .addStructure(structure("long_corridor_factory_building")
                                .type(FACTORY)
                                .displayName("Long Corridor Factory Building")
                                .resource(platform("long_corridor_factory_building"))
                                .symbolMap(platform("long_corridor_factory_building.json"))
                                .materials(0, 800)
                                .materials(1, 800)
                                .build())
                        .build());

        PRESETS.add(
                preset("sy_1_batch_construction_template")
                        .displayName("SY-1 Batch Construction Template")
                        .description("Extended factory shells ported from GTOEPP")
                        .source("GregTech Odyssey Extended Platform Presets")
                        .addStructure(eppFactory("rubiks_cube_factory"))
                        .addStructure(eppFactory("starry_sky_theme_suite_earth_style"))
                        .addStructure(eppFactory("starry_sky_theme_suite_solar_system_style"))
                        .addStructure(eppFactory("starry_sky_theme_suite_barnard_style"))
                        .addStructure(eppFactory("starry_sky_theme_suite_proxima_style"))
                        .addStructure(eppFactory("starry_sky_theme_suite_ross_128b_style"))
                        .addStructure(eppFactory("extra_large_factory"))
                        .addStructure(eppFactory("assembly_plant"))
                        .addStructure(eppFactory("trans_space_assembly_plant"))
                        .addStructure(eppFactory("cell_culture_center"))
                        .addStructure(eppFactory("bacteria_factory"))
                        .addStructure(eppFactory("fluid_refinery"))
                        .addStructure(eppFactory("silica_rock_power_plant"))
                        .addStructure(eppFactory("nuclear_power_plant"))
                        .addStructure(eppFactory("mineral_processing_center"))
                        .addStructure(eppFactory("deep_compositing_center"))
                        .addStructure(eppFactory("institute_of_microphysics"))
                        .addStructure(eppFactory("supercomputing_center_tai_chi_computer_room"))
                        .addStructure(eppFactory("supercomputing_center_simple_computer_room"))
                        .addStructure(eppFactory("space_elevator"))
                        .build());
    }
}
