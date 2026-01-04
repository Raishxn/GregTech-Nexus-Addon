package com.raishxn.gtna.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.MultiblockShapeInfo;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.common.machine.multiblock.energy.IndustrialSlaughterhouse;
import com.raishxn.gtna.common.machine.multiblock.noenergy.HyperPressureReactor;
import com.raishxn.gtna.common.machine.multiblock.noenergy.InfernalCokeOven;
import com.raishxn.gtna.common.machine.multiblock.noenergy.LeapForwardBlastFurnace;
import com.raishxn.gtna.common.machine.multiblock.part.steam.HugeSteamInputBus;
import com.raishxn.gtna.common.machine.multiblock.part.steam.HugeSteamOutputBus;
import com.raishxn.gtna.common.machine.multiblock.part.steam.WirelessSteamInputHatch;
import com.raishxn.gtna.common.machine.multiblock.part.steam.WirelessSteamOutputHatch;
import com.raishxn.gtna.common.machine.multiblock.steam.*;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

import java.util.List;
import java.util.function.BiConsumer;

import static com.gregtechceu.gtceu.api.machine.multiblock.PartAbility.*;
import static com.gregtechceu.gtceu.api.machine.multiblock.PartAbility.EXPORT_ITEMS;
import static com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties.IS_FORMED;
import static com.gregtechceu.gtceu.api.pattern.Predicates.*;
import static com.gregtechceu.gtceu.api.pattern.util.RelativeDirection.*;
import static com.raishxn.gtna.api.registry.GTNARegistry.REGISTRATE;
import static com.raishxn.gtna.common.data.GTNARecipeType.HIGH_PRESSURE_REACTOR_RECIPES;
import static com.raishxn.gtna.common.data.GTNARecipeType.SUPERHEATER_RECIPES;

public class GTNAMachines {

    private static final ResourceLocation OVERLAY_IN = new ResourceLocation("gtna", "block/overlay/machine/overlay_steam_wireless_in");
    private static final ResourceLocation OVERLAY_OUT = new ResourceLocation("gtna", "block/overlay/machine/overlay_steam_wireless_out");
    private static final ResourceLocation OVERLAY_STEAM_IN = new ResourceLocation("gtceu", "block/overlay/machine/overlay_item_hatch_input");
    private static final ResourceLocation OVERLAY_STEAM_OUT = new ResourceLocation("gtceu", "block/overlay/machine/overlay_item_hatch_output");
    public static final BiConsumer<ItemStack, List<Component>> GTNA_ADD = (stack, components) -> components
            .add(Component.translatable("gtna.registry.add")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));

    // --- INPUT HATCHES (Recebe Vapor) ---

    public static final MachineDefinition WIRELESS_STEAM_INPUT_HATCH = REGISTRATE
            .machine("wireless_steam_input_hatch", holder -> new WirelessSteamInputHatch(holder, false))
            .tier(0)
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.STEAM, IMPORT_FLUIDS)
            .colorOverlaySteamHullModel(OVERLAY_IN)
            .modelProperty(GTMachineModelProperties.IS_STEEL_MACHINE, false)
            .modelProperty(IS_FORMED, false)
            .tooltips(
                    Component.translatable("gtna.machine.wireless_steam_input.tooltip_desc")
                            .withStyle(ChatFormatting.GRAY),
                    Component.translatable("gtceu.universal.tooltip.fluid_storage_capacity", 20000)
            )
            .tooltipBuilder(GTNA_ADD)
            .register();

    public static final MachineDefinition WIRELESS_STEAM_INPUT_HATCH_STEEL = REGISTRATE
            .machine("wireless_steam_input_hatch_steel", holder -> new WirelessSteamInputHatch(holder, true))
            .tier(1)
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.STEAM, IMPORT_FLUIDS)
            .colorOverlaySteamHullModel(OVERLAY_IN)
            .modelProperty(GTMachineModelProperties.IS_STEEL_MACHINE, true)
            .modelProperty(IS_FORMED, false)
            .tooltips(
                    Component.translatable("gtna.machine.wireless_steam_input.tooltip_desc")
                            .withStyle(ChatFormatting.GRAY),
                    Component.translatable("gtceu.universal.tooltip.fluid_storage_capacity", Integer.MAX_VALUE)
            )
            .tooltipBuilder(GTNA_ADD)
            .register();

    // --- OUTPUT HATCHES (Envia Vapor) ---

    public static final MachineDefinition WIRELESS_STEAM_OUTPUT_HATCH = REGISTRATE
            .machine("wireless_steam_output_hatch", holder -> new WirelessSteamOutputHatch(holder, false))
            .tier(0)
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.STEAM, EXPORT_FLUIDS)
            .colorOverlaySteamHullModel(OVERLAY_OUT)
            .modelProperty(GTMachineModelProperties.IS_STEEL_MACHINE, false)
            .modelProperty(IS_FORMED, false)
            .tooltips(
                    Component.translatable("gtna.machine.wireless_steam_output.tooltip_desc")
                            .withStyle(ChatFormatting.GRAY),
                    Component.translatable("gtna.machine.wireless_steam_output.tooltip_usage")
                            .withStyle(ChatFormatting.GOLD),
                    Component.translatable("gtceu.universal.tooltip.fluid_storage_capacity", 20000)
            )
            .tooltipBuilder(GTNA_ADD)
            .register();

    public static final MachineDefinition WIRELESS_STEAM_OUTPUT_HATCH_STEEL = REGISTRATE
            .machine("wireless_steam_output_hatch_steel", holder -> new WirelessSteamOutputHatch(holder, true))
            .tier(1)
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.STEAM, EXPORT_FLUIDS)
            .colorOverlaySteamHullModel(OVERLAY_OUT)
            .modelProperty(GTMachineModelProperties.IS_STEEL_MACHINE, true)
            .modelProperty(IS_FORMED, false)
            .tooltips(
                    Component.translatable("gtna.machine.wireless_steam_output.tooltip_desc")
                            .withStyle(ChatFormatting.GRAY),
                    Component.translatable("gtna.machine.wireless_steam_output.tooltip_usage")
                            .withStyle(ChatFormatting.GOLD),
                    Component.translatable("gtceu.universal.tooltip.fluid_storage_capacity", Integer.MAX_VALUE)
            )
            .tooltipBuilder(GTNA_ADD)
            .register();

    public static final MachineDefinition HUGE_STEAM_INPUT_BUS = REGISTRATE
            .machine("huge_steam_input_bus", HugeSteamInputBus::new)
            .rotationState(RotationState.ALL)
            .tier(GTValues.ULV)
            .abilities(PartAbility.STEAM_IMPORT_ITEMS)
            .modelProperty(IS_FORMED, false)
            .modelProperty(GTMachineModelProperties.IS_STEEL_MACHINE, false)
            .colorOverlaySteamHullModel(OVERLAY_STEAM_IN)
            .tooltipBuilder(GTNA_ADD)
            .tooltips(Component.translatable("gtna.tooltip.huge_steam_bus").withStyle(ChatFormatting.GREEN))
            .register();

    public static final MachineDefinition HUGE_STEAM_OUTPUT_BUS = REGISTRATE
            .machine("huge_steam_output_bus", HugeSteamOutputBus::new)
            .rotationState(RotationState.ALL)
            .tier(GTValues.ULV)
            .abilities(PartAbility.STEAM_EXPORT_ITEMS)
            .modelProperty(IS_FORMED, false)
            .modelProperty(GTMachineModelProperties.IS_STEEL_MACHINE, false)
            .colorOverlaySteamHullModel(OVERLAY_STEAM_OUT)
            .tooltips(Component.translatable("gtna.tooltip.huge_steam_bus").withStyle(ChatFormatting.GREEN))
            .tooltipBuilder(GTNA_ADD)
            .register();








    // --- MULTIBLOCKS ---

    public static final MultiblockMachineDefinition LARGE_STEAM_CRUSHER = REGISTRATE
            .multiblock("large_steam_crusher", LargeSteamCrusher::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.MACERATOR_RECIPES)
            .appearanceBlock(GTBlocks.CASING_BRONZE_BRICKS)
            .recipeModifier(LargeSteamCrusher::recipeModifier)
            .workableCasingModel(
                    GTCEu.id("block/casings/solid/machine_casing_bronze_plated_bricks"),
                    GTCEu.id("block/multiblock/steam_grinder"))
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle(
                            "EEEEEEE",
                            "AAAAAAA",
                            "AAAAAAA",
                            "AAAAAAA",
                            "DDDDDDD",
                            "       ",
                            "       ",
                            "       ")
                    .aisle(
                            "EEEEEEE",
                            "A     A",
                            "ACCCCCA",
                            "A     A",
                            "ABBBBBA",
                            "DCCCCCD",
                            "DD   DD",
                            "       ")
                    .aisle(
                            "EEEEEEE",
                            "DBBBBBD",
                            "A     A",
                            "A     A",
                            "A     A",
                            "DD   DD",
                            "D     D",
                            "       ")
                    .aisle(
                            "EEEEEEE",
                            "ACCCCCA",
                            "DBBBBBD",
                            "ACCCCCA",
                            "AAAAAAA",
                            "ADDDDDA",
                            "A     A",
                            "ADDDDDA")
                    .aisle(
                            "EEEEEEE",
                            "DBBBBBD",
                            "A     A",
                            "A     A",
                            "AAAAAAA",
                            "AAAAAAA",
                            "AAAAAAA",
                            "AAAAAAA")
                    .aisle(
                            "EEEEEEE",
                            "ACCCCCA",
                            "AAAAAAA",
                            "AAAAAAA",
                            " AAAAA ",
                            "AAAAAAA",
                            "AAAAAAA",
                            "CAAAAAC")
                    .aisle(
                            "EEEEEEE",
                            "A     A",
                            "AAAAAAA",
                            " AAAAA ",
                            "       ",
                            " AAAAA ",
                            " AAAAA ",
                            "C     C")
                    .aisle(
                            "EEEEEEE",
                            "AAAAAAA",
                            "AADDDAA",
                            " ADDDA ",
                            "  DDD  ",
                            "  DDD  ",
                            "       ",
                            "C     C")
                    .aisle(
                            "EEEEEEE",
                            "AAAAAAA",
                            "  D D  ",
                            "  D D  ",
                            "  DDD  ",
                            "  DDD  ",
                            "       ",
                            "C     C")
                    .aisle(
                            "EEEEEEE",
                            "AAAAAAA",
                            "  DDD  ",
                            "  DDD  ",
                            "  DDD  ",
                            "  DDD  ",
                            "       ",
                            "C     C")
                    .aisle(
                            "EEEEEEE",
                            "ACCCCCA",
                            "AAA~AAA",
                            "AAAAAAA",
                            "C     C",
                            "C     C",
                            "C     C",
                            "CCCCCCC")
                    .where('~', controller(blocks(definition.get())))
                    .where('A', blocks(GTBlocks.CASING_BRONZE_BRICKS.get())
                            .or(abilities(PartAbility.STEAM_IMPORT_ITEMS).setPreviewCount(1))
                            .or(abilities(PartAbility.STEAM_EXPORT_ITEMS).setPreviewCount(1))
                            .or(abilities(PartAbility.STEAM).setExactLimit(1)))
                    .where('B', blocks(GTBlocks.CASING_STEEL_SOLID.get()))
                    .where('C', blocks(GTBlocks.CASING_BRONZE_BRICKS.get()))
                    .where('D', blocks(GTBlocks.CASING_STEEL_SOLID.get()))
                    .where('E', blocks(GTBlocks.CASING_BRONZE_BRICKS.get()))
                    .where(' ', Predicates.any())
                    .build())
            .tooltipBuilder(GTNA_ADD)
            .tooltips(Component.translatable("gtna.tooltip.large_steam_crusher.speed").withStyle(ChatFormatting.GOLD),
                    Component.translatable("gtna.tooltip.large_steam_crusher.parallel").withStyle(ChatFormatting.BLUE))
            .register();

    public static final MultiblockMachineDefinition MEGA_PRESSURE_SOLAR_BOILER = REGISTRATE.multiblock("mega_pressure_solar_boiler", MegaSolarBoilerMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.DUMMY_RECIPES)
            .appearanceBlock(GTNABlocks.HYPER_PRESSURE_BREEL_CASING)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("AAA", "ABA", "A~A")
                    .where('~', controller(blocks(definition.get())))
                    .where('A', blocks(GTNABlocks.HYPER_PRESSURE_BREEL_CASING.get())
                            .or(abilities(IMPORT_FLUIDS))
                            .or(abilities(EXPORT_FLUIDS)))
                    .where('B', blocks(GTNABlocks.SOLAR_BOILING_CELL.get()))
                    .build())
            .shapeInfos(definition -> {
                var minShape = MultiblockShapeInfo.builder()
                        .aisle("AAA", "BBB", "BBB", "BBB", "A~A")
                        .where('~', definition, Direction.NORTH)
                        .where('A', GTNABlocks.HYPER_PRESSURE_BREEL_CASING.get())
                        .where('B', GTNABlocks.SOLAR_BOILING_CELL.get())
                        .build();
                return List.of(minShape);
            })
            .workableCasingModel(GTNACORE.id("block/casings/mega_pressure_solar_boiler_casing"), GTNACORE.id("block/overlay/machine/solarboiler"))
            .tooltips(
                    Component.translatable("gtna.tooltip.mega_solar.desc", "A massive solar thermal power plant.")
                            .withStyle(ChatFormatting.GRAY),
                    Component.translatable("gtna.tooltip.mega_solar.expansion", "Structure is expandable! Add Solar Pipes behind and to the sides.")
                            .withStyle(ChatFormatting.GOLD),
                    Component.translatable("gtna.tooltip.mega_solar.sunlight", "REQUIREMENT: Every Solar Pipe casing must have direct access to the sky.")
                            .withStyle(ChatFormatting.RED),
                    Component.translatable("gtna.tooltip.mega_solar.production", "Production: 10,000 L/s of Steam per active Pipe Block.")
                            .withStyle(ChatFormatting.BLUE),
                    Component.translatable("gtna.tooltip.mega_solar.max_size", "Max Size: 33 Wide x 32 Deep.")
                            .withStyle(ChatFormatting.DARK_GRAY),
                    Component.literal("Warning: May produce more steam than unplayed games in your library")
                            .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD)
            )
            .register();

    public static final MultiblockMachineDefinition LARGE_STEAM_FURNACE = REGISTRATE
            .multiblock("large_steam_furnace", LargeSteamFurnace::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.FURNACE_RECIPES)
            .recipeType(GTRecipeTypes.BLAST_RECIPES)
            .recipeModifier(LargeSteamFurnace::recipeModifier)
            .appearanceBlock(GTBlocks.CASING_BRONZE_BRICKS)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle(
                            "F_______F",
                            "D_______D",
                            "D_______D",
                            "D_______D",
                            "D_______D",
                            "D_______D",
                            "D_______D",
                            "F_______F"
                    )
                    .aisle(
                            "FGGGGGGGF",
                            "DAAAAAAAD",
                            "DAAAAAAAD",
                            "DAAAAAAAD",
                            "DAAAAAAAD",
                            "DAAAAAAAD",
                            "DAAAAAAAD",
                            "FGGGGGGGF"
                    )
                    .aisle(
                            "FGGGGGGGF",
                            "DABBBBBAD",
                            "DAHHHHHAD",
                            "DAHHHHHAD",
                            "DAHHHHHAD",
                            "DAHHHHHAD",
                            "DABBBBBAD",
                            "FGGGGGGGF"
                    )
                    .aisle(
                            "FGGGGGGGF",
                            "DABBBBBAD",
                            "DAHDDDHAD",
                            "DAH   HAD",
                            "DAH   HAD",
                            "DAHDDDHAD",
                            "DABBBBBAD",
                            "FGGGGGGGF"
                    )
                    .aisle(
                            "FGGGGGGGF",
                            "DABBBBBAD",
                            "DAHDDDHAD",
                            "DAH   HAD",
                            "DAH   HAD",
                            "DAHDDDHAD",
                            "DABBBBBAD",
                            "FGGGGGGGF"
                    )
                    .aisle(
                            "FGGGGGGGF",
                            "DABBBBBAD",
                            "DAHDDDHAD",
                            "DAH   HAD",
                            "DAH   HAD",
                            "DAHDDDHAD",
                            "DABBBBBAD",
                            "FGGGGGGGF"
                    )
                    .aisle(
                            "FGGGGGGGF",
                            "DAAAAAAAD",
                            "DAHHHHHAD",
                            "DAHHHHHAD",
                            "DAHHHHHAD",
                            "DAHHHHHAD",
                            "DAAAAAAAD",
                            "FGGGGGGGF"
                    )
                    .aisle(
                            "FFFFFFFFF",
                            "DAAAAAAAD",
                            "DAAAAAAAD",
                            "DAAASAAAD",
                            "DAAAAAAAD",
                            "DAAAAAAAD",
                            "DAAAAAAAD",
                            "FFFFFFFFF"
                    )
                    .where('S', Predicates.controller(Predicates.blocks(definition.get())))
                    .where('A', Predicates.blocks(GTBlocks.CASING_BRONZE_BRICKS.get())
                            .or(abilities(PartAbility.STEAM_IMPORT_ITEMS).setPreviewCount(1))
                            .or(abilities(PartAbility.STEAM_EXPORT_ITEMS).setPreviewCount(1))
                            .or(abilities(PartAbility.STEAM).setExactLimit(1)))
                    .where('B', Predicates.blocks(GTBlocks.CASING_BRONZE_PIPE.get()))
                    .where('C', Predicates.blocks(GTBlocks.CASING_BRONZE_GEARBOX.get()))
                    .where('D', Predicates.blocks(ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.Bronze)))
                    .where('E', Predicates.blocks(GTBlocks.CASING_STEEL_SOLID.get()))
                    .where('F', Predicates.blocks(GTBlocks.CASING_BRONZE_BRICKS.get()))
                    .where('G', Predicates.blocks(Blocks.STONE_BRICKS))
                    .where('H', Predicates.blocks(GTBlocks.CASING_STEEL_SOLID.get()))
                    .where(' ', Predicates.air())
                    .where('_', Predicates.any())
                    .build())
            .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_bronze_plated_bricks"), GTNACORE.id("block/overlay/machine/largesteamfurnace"))
            .tooltips(
                    Component.translatable("gtna.tooltip.large_steam_furnace.desc", "An industrial-grade steam smelting facility.")
                            .withStyle(ChatFormatting.GRAY),

                    Component.translatable("gtna.tooltip.large_steam_furnace.speed", "Speed: 900% faster than a standard Steam Furnace.")
                            .withStyle(ChatFormatting.GOLD),

                    Component.translatable("gtna.tooltip.large_steam_furnace.efficiency", "Efficiency: Consumes only 50% of the required Steam.")
                            .withStyle(ChatFormatting.GREEN),

                    Component.translatable("gtna.tooltip.large_steam_furnace.parallel", "Parallelism: Processes up to 128 items simultaneously.")
                            .withStyle(ChatFormatting.BLUE),

                    Component.translatable("gtna.tooltip.large_steam_furnace.structure", "Structure: 9x5x9 (Hollow Center). check JEI for details.")
                            .withStyle(ChatFormatting.DARK_GRAY),

                    Component.literal("Warning: Do not attempt to bake cookies inside. They will vaporize instantly.")
                            .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD)
            )
            .register();

    public static final MultiblockMachineDefinition LARGE_STEAM_ALLOY_SMELTER = REGISTRATE
            .multiblock("large_steam_alloy_smelter", LargeSteamAlloySmelter::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.ALLOY_SMELTER_RECIPES)
            .recipeModifier(LargeSteamAlloySmelter::recipeModifier)
            .appearanceBlock(GTBlocks.CASING_BRONZE_BRICKS)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle(
                            "BBB",
                            "AAA",
                            "AAA",
                            " A "
                    )
                    .aisle(
                            "BBB",
                            "A A",
                            "A A",
                            "AAA"
                    )
                    .aisle(
                            "BBB",
                            "A~A",
                            "AAA",
                            " A "
                    )
                    .where('~', controller(blocks(definition.get())))
                    .where('A', blocks(GTBlocks.CASING_BRONZE_BRICKS.get())
                            .or(abilities(PartAbility.STEAM_IMPORT_ITEMS).setPreviewCount(1))
                            .or(abilities(PartAbility.STEAM_EXPORT_ITEMS).setPreviewCount(1))
                            .or(abilities(PartAbility.STEAM).setExactLimit(1)))
                    .where('B', blocks(GTBlocks.FIREBOX_BRONZE.get()))
                    .where(' ', any())
                    .build())
            .workableCasingModel(
                    GTCEu.id("block/casings/solid/machine_casing_bronze_plated_bricks"),
                    GTNACORE.id("block/overlay/machine/largesteamalloysmelter"))
            .tooltips(
                    Component.translatable("gtna.tooltip.large_steam_alloy.desc", "High-pressure steam alloying.")
                            .withStyle(ChatFormatting.GRAY),
                    Component.translatable("gtna.tooltip.large_steam_alloy.speed", "Speed: 43% faster than Singleblock.")
                            .withStyle(ChatFormatting.GOLD),
                    Component.translatable("gtna.tooltip.large_steam_alloy.parallel", "Parallel: Processes up to 64 items.")
                            .withStyle(ChatFormatting.BLUE),
                    Component.translatable("gtna.tooltip.large_steam_alloy.structure", "Structure: 3x4x3 (WxHxD). Fireboxes at bottom.")
                            .withStyle(ChatFormatting.DARK_GRAY)
            )
            .register();

    public static final MultiblockMachineDefinition STEAM_COBBLER = REGISTRATE
            .multiblock("steam_cobbler", SteamCobbler::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.ROCK_BREAKER_RECIPES)
            .recipeModifier(SteamCobbler::recipeModifier)
            .appearanceBlock(GTBlocks.CASING_BRONZE_BRICKS)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle(
                            "AAAAAAAAAAA",
                            " AAAA AAAA ",
                            "   A   A   ",
                            "           ",
                            "           ",
                            "           "
                    )
                    .aisle(
                            "ABBBBABBBBA",
                            "AGGGGAGGGGA",
                            "           ",
                            "   A   A   ",
                            "           ",
                            "           "
                    )
                    .aisle(
                            "ABAAAAAAABA",
                            "AGGGFEFGGGA",
                            "           ",
                            "   A   A   ",
                            "           ",
                            "           "
                    )
                    .aisle(
                            "ABACCCCCABA",
                            "AGGFEEEFGGA",
                            "A  D   D  A",
                            " AAD   DAA ",
                            "           ",
                            "           "
                    )
                    .aisle(
                            "ABACCCCCABA",
                            "AGFEEEEEFGA",
                            "     E     ",
                            "     E     ",
                            "     E     ",
                            "     E     "
                    )
                    .aisle(
                            "AAACCCCCAAA",
                            " AEEEEEEEA ",
                            "    E E    ",
                            "    E E    ",
                            "    E E    ",
                            "    EEE    "
                    )
                    .aisle(
                            "ABACCCCCABA",
                            "AGFEEEEEFGA",
                            "     E     ",
                            "     E     ",
                            "     E     ",
                            "     E     "
                    )
                    .aisle(
                            "ABACCCCCABA",
                            "AGGFEEEFGGA",
                            "A  D   D  A",
                            " AAD   DAA ",
                            "           ",
                            "           "
                    )
                    .aisle(
                            "ABAAAAAAABA",
                            "AGGGFEFGGGA",
                            "           ",
                            "   A   A   ",
                            "           ",
                            "           "
                    )
                    .aisle(
                            "ABBBBABBBBA",
                            "AGGGGAGGGGA",
                            "           ",
                            "   A   A   ",
                            "           ",
                            "           "
                    )
                    .aisle(
                            "AAAAAAAAAAA",
                            " AAAA~AAAA ",
                            "   A   A   ",
                            "           ",
                            "           ",
                            "           "
                    )
                    .where(' ', any())
                    .where('A', blocks(GTBlocks.CASING_BRONZE_BRICKS.get())
                            .or(abilities(PartAbility.IMPORT_ITEMS).setPreviewCount(1))
                            .or(abilities(PartAbility.STEAM_EXPORT_ITEMS).setPreviewCount(1))
                            .or(abilities(PartAbility.STEAM).setExactLimit(1)))
                    .where('B', blocks(GTBlocks.CASING_BRONZE_BRICKS.get()))
                    .where('C', blocks(GTBlocks.CASING_BRONZE_PIPE.get()))
                    .where('D', blocks(Blocks.IRON_BLOCK))
                    .where('E', blocks(Blocks.MAGMA_BLOCK))
                    .where('F', blocks(Blocks.COBBLESTONE))
                    .where('G', blocks(Blocks.WATER))
                    .where('~', controller(blocks(definition.get())))
                    .build())
            .workableCasingModel(
                    GTCEu.id("block/casings/solid/machine_casing_bronze_plated_bricks"),
                    GTNACORE.id("block/overlay/machine/steamcobbler"))
            .tooltips(
                    Component.translatable("gtna.tooltip.steam_cobbler.desc", "Advanced Steam Rock Generator.")
                            .withStyle(ChatFormatting.GRAY),
                    Component.translatable("gtna.tooltip.steam_cobbler.modes", "Generates various stones based on Programmed Circuits.")
                            .withStyle(ChatFormatting.GOLD),
                    Component.translatable("gtna.tooltip.steam_cobbler.consumption", "Steam Consumption: 1200 L/s (60 L/t)")
                            .withStyle(ChatFormatting.RED),
                    Component.translatable("gtna.tooltip.steam_cobbler.parallel", "Max Parallel: 16 operations.")
                            .withStyle(ChatFormatting.BLUE),
                    Component.translatable("gtna.tooltip.steam_cobbler.structure", "Structure: 3x3x3 Cube with Bronze Pipe center.")
                            .withStyle(ChatFormatting.DARK_GRAY)
            )
            .register();

    // Em GTNAMachines.java

    public static final MultiblockMachineDefinition STONE_SUPERHEATER = REGISTRATE
            .multiblock("stone_superheater", StoneSuperHeater::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(SUPERHEATER_RECIPES)
            .appearanceBlock(GTNABlocks.STRONZE_WRAPPED_CASING)
            .recipeModifier(StoneSuperHeater::recipeModifier)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle(
                            "AAA",
                            "ABA",
                            "ABA",
                            "ABA",
                            "AAA"
                    )
                    .aisle(
                            "AAA",
                            "BCB",
                            "BCB",
                            "BCB",
                            "AAA"
                    )
                    .aisle(
                            "A~A",
                            "ABA",
                            "ABA",
                            "ABA",
                            "AAA"
                    )
                    .where('~', controller(blocks(definition.get())))
                    .where('A', blocks(GTNABlocks.STRONZE_WRAPPED_CASING.get())
                            .or(abilities(PartAbility.IMPORT_ITEMS).setPreviewCount(1))
                            .or(abilities(PartAbility.EXPORT_FLUIDS).setPreviewCount(1))
                            .or(abilities(PartAbility.STEAM).setExactLimit(1)))
                    .where('B', blocks(GTNABlocks.BOROSILICATE_GLASS_BLOCK.get()))
                    .where('C', blocks(Blocks.MAGMA_BLOCK))
                    .build())
            .workableCasingModel(
                    GTNACORE.id("block/casings/stronze_wrapped_casing"),
                    GTNACORE.id("block/overlay/machine/stonesuperheater"))
            .tooltips(
                    Component.translatable("gtna.tooltip.stone_superheater.desc", "Extreme heat stone melting.").withStyle(ChatFormatting.GRAY),
                    Component.translatable("gtna.tooltip.stone_superheater.parallel", "Max Parallel: 32").withStyle(ChatFormatting.BLUE),
                    Component.translatable("gtna.tooltip.stone_superheater.steam", "Steam Cost: 640 L/s per active recipe.").withStyle(ChatFormatting.RED)
            )
            .register();
    public static final MultiblockMachineDefinition STEAM_MANUFACTURER = REGISTRATE
            .multiblock("steam_manufacturer", SteamManufacturer::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTNARecipeType.HYDRAULIC_MANUFACTURING)
            .appearanceBlock(GTNABlocks.BREEL_PLATED_CASING)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle(
                            " CCCCC   ",
                            " DDDDD   ",
                            " CCCCC   ",
                            "         ",
                            "         ",
                            "         ",
                            "         "
                    )
                    .aisle(
                            "CCCCCCC  ",
                            "D     D  ",
                            "C     C  ",
                            "         ",
                            "         ",
                            "         ",
                            "         "
                    )
                    .aisle(
                            "CCCCCCCCC",
                            "D EEE EA ",
                            "C     CA ",
                            "       A ",
                            "       A ",
                            "     AABA",
                            "       A "
                    )
                    .aisle(
                            "CCCCCCCCC",
                            "D E EEEEC",
                            "C     CEC",
                            "   B   EC",
                            "   B   EC",
                            "   BBBBBC",
                            "    AAACA"
                    )
                    .aisle(
                            "CCCCCCCCC",
                            "D EEE EA ",
                            "C     CA ",
                            "       A ",
                            "       A ",
                            "     AABA",
                            "       A "
                    )
                    .aisle(
                            "CCCCCCC  ",
                            "D     D  ",
                            "C     C  ",
                            "         ",
                            "         ",
                            "         ",
                            "         "
                    )
                    .aisle(
                            " CCCCC   ",
                            " DD~DD   ",
                            " CCCCC   ",
                            "         ",
                            "         ",
                            "         ",
                            "         "
                    )
                    .where('~', controller(blocks(definition.get())))
                    .where('A', blocks(GTNABlocks.BREEL_PIPE_CASING.get()))
                    .where('B', blocks(GTNABlocks.HYDRAULIC_ASSEMBLER_CASING.get()))
                    .where('C', blocks(GTNABlocks.BREEL_PLATED_CASING.get())
                            .or(abilities(PartAbility.IMPORT_ITEMS).setPreviewCount(1))
                            .or(abilities(EXPORT_ITEMS).setPreviewCount(1))
                            .or(abilities(PartAbility.STEAM).setExactLimit(1)))
                    .where('D', blocks(GTBlocks.CASING_STEEL_GEARBOX.get()))
                    .where('E', blocks(ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.Steel)))
                    .where(' ', any())
                    .build())
            .workableCasingModel(
                    GTNACORE.id("block/casings/breel_plated_casing"),
                    GTNACORE.id("block/overlay/machine/steammanufacturer"))
            .tooltips(
                    Component.translatable("gtna.tooltip.steam_manufacturer.desc", "Advanced Hydraulic Assembly Line.").withStyle(ChatFormatting.GRAY),
                    Component.translatable("gtna.tooltip.steam_manufacturer.parallel", "Max Parallel: 16").withStyle(ChatFormatting.BLUE),
                    Component.translatable("gtna.tooltip.steam_manufacturer.type", "Recipe Type: Hydraulic Manufacturing").withStyle(ChatFormatting.GOLD)
            )
            .register();
    public static final MultiblockMachineDefinition STEAM_WOODCUTTER = REGISTRATE
            .multiblock("steam_woodcutter", SteamWoodcutter::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTNARecipeType.WOODCUTTER_RECIPES)
            .recipeModifier(SteamWoodcutter::recipeModifier)
            .appearanceBlock(GTNABlocks.BRONZE_REINFORCED_WOOD)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle(
                            "  BBB  ",
                            "       ",
                            "       ",
                            "       ",
                            "       ",
                            "       ",
                            "  BBB  "
                    )
                    .aisle(
                            " BBABB ",
                            "  DDD  ",
                            "  DDD  ",
                            "  DDD  ",
                            "  DDD  ",
                            "  DDD  ",
                            " BBABB "
                    )
                    .aisle(
                            "BBEEEBB",
                            " D   D ",
                            " D   D ",
                            " D   D ",
                            " D   D ",
                            " D   D ",
                            "BBACABB"
                    )
                    .aisle(
                            "BAEEEAB",
                            " D   D ",
                            " D   D ",
                            " D   D ",
                            " D   D ",
                            " D   D ",
                            "BACCCAB"
                    )
                    .aisle(
                            "BBEEEBB",
                            " D   D ",
                            " D   D ",
                            " D   D ",
                            " D   D ",
                            " D   D ",
                            "BBACABB"
                    )
                    .aisle(
                            " BBABB ",
                            "  DDD  ",
                            "  DDD  ",
                            "  DDD  ",
                            "  DDD  ",
                            "  DDD  ",
                            " BBABB "
                    )
                    .aisle(
                            "  B~B  ",
                            "       ",
                            "       ",
                            "       ",
                            "       ",
                            "       ",
                            "  BBB  "
                    )
                    .where('~', controller(blocks(definition.get())))
                    .where('A', blocks(GTNABlocks.IRON_REINFORCED_WOOD.get()))
                    .where('B', blocks(GTNABlocks.BRONZE_REINFORCED_WOOD.get())
                            .or(abilities(PartAbility.STEAM_IMPORT_ITEMS).setPreviewCount(1))
                            .or(abilities(PartAbility.STEAM_EXPORT_ITEMS).setPreviewCount(1))
                            .or(abilities(PartAbility.STEAM).setExactLimit(1)))
                    .where('C', blocks(GTNABlocks.STEEL_REINFORCED_WOOD.get()))
                    .where('D', blocks(Blocks.GLASS))
                    .where('E', blocks(Blocks.DIRT))
                    .where(' ', any())
                    .build())
            .workableCasingModel(
                    GTNACORE.id("block/casings/bronze_reinforced_wood"),
                    GTNACORE.id("block/overlay/machine/steamwoodcutter"))
            .tooltips(
                    Component.translatable("gtna.tooltip.steam_woodcutter.desc", "Industrial Tree Processor.").withStyle(ChatFormatting.GRAY),
                    Component.translatable("gtna.tooltip.steam_woodcutter.parallel", "Max Parallel: 64").withStyle(ChatFormatting.BLUE),
                    Component.translatable("gtna.tooltip.steam_woodcutter.steam", "Base Steam: 1200 L/s").withStyle(ChatFormatting.RED),
                    Component.translatable("gtna.tooltip.steam_woodcutter.info", "Processes saplings into huge amounts of resources without consuming them.").withStyle(ChatFormatting.GOLD)
            )
            .register();

    public static final MultiblockMachineDefinition LEAP_FORWARD_ONE_BLAST_FURNACE = REGISTRATE
            .multiblock("leap_forward_one_blast_furnace", LeapForwardBlastFurnace::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.PRIMITIVE_BLAST_FURNACE_RECIPES)
            .recipeModifier(LeapForwardBlastFurnace::recipeModifier)
            .appearanceBlock(GTBlocks.CASING_PRIMITIVE_BRICKS)
            .pattern(definition -> FactoryBlockPattern.start( BACK, RIGHT, UP)
                    .aisle("     AAAAA     ", "  DDDDDDDDDDD  ", " DDDDDDDDDDDDD ", " DDDDDDDDDDDDD ", " DDDDDDDDDDDDD ", "ADDDDDDDDDDDDDA", "ADDDDDDDDDDDDDA", "ADDDDDDDDDDDDDA", "ADDDDDDDDDDDDDA", "ADDDDDDDDDDDDDA", " DDDDDDDDDDDDD ", " DDDDDDDDDDDDD ", " DDDDDDDDDDDDD ", "  DDDDDDDDDDD  ", "     AAAAA     ")
                    .aisle("     AAAAA     ", "    DEEEEED    ", "   DE     ED   ", "  DE       ED  ", " DE         ED ", "AE           EA", "AE           EA", "GE           EA", "AE           EA", "AE           EA", " DE         ED ", "  DE       ED  ", "   DE     ED   ", "    DEEEEED    ", "     AAAAA     ")
                    .aisle("     BCCCB     ", "    D     D    ", "   D       D   ", "  D         D  ", " D           D ", "B             B", "C             C", "C             C", "C             C", "B             B", " D           D ", "  D         D  ", "   D       D   ", "    D     D    ", "     BCCCB     ")
                    .aisle("     BCCCB     ", "    D     D    ", "   D       D   ", "  D         D  ", " D           D ", "B             B", "C             C", "C             C", "C             C", "B             B", " D           D ", "  D         D  ", "   D       D   ", "    D     D    ", "     BCCCB     ")
                    .aisle("     DDDDD     ", "    DEEEEED    ", "   DE     ED   ", "  DE       ED  ", " DE         ED ", "DE           ED", "DE           ED", "DE           ED", "DE           ED", "DE           ED", " DE         ED ", "  DE       ED  ", "   DE     ED   ", "    DEEEEED    ", "     DDDDD     ")
                    .aisle("               ", "     DDDDD     ", "    DDEEEDD    ", "   DEDFFFDED   ", "  DEEDFFFDEED  ", " DDDDDDDDDDDDDD", " DEFFDE EDFFEDF", " DEFFD   DFFEDF", " DEFFDE EDFFEDF", " DDDDDDDDDDDDDD", "  DEEDFFFDEED  ", "   DEDFFFDED   ", "    DDEEEDD    ", "     DDDDD     ", "               ")
                    .aisle("               ", "       D       ", "      EDE      ", "    EE   EE    ", "   EE     EE   ", "   E       E   ", "  E    E    E F", " DD   E E   DD ", "  E    E    E F", "   E       E   ", "   EE     EE   ", "    EE   EE    ", "      EDE      ", "       D       ", "               ")
                    .aisle("               ", "               ", "      EDE      ", "     E   E     ", "    E     E    ", "   E       E   ", "  E    E    E F", "  D   E E   D  ", "  E    E    E H", "   E       E   ", "    E     E    ", "     E   E     ", "      EDE      ", "               ", "               ").setRepeatable(2, 16)
                    .aisle("               ", "               ", "      DDD      ", "     DEEED     ", "    D     D    ", "   D       D   ", "  DE   E   ED F", "  DE  E E  ED  ", "  DE   E   ED F", "   D       D   ", "    D     D    ", "     DEEED     ", "      DDD      ", "               ", "               ")
                    .aisle("               ", "               ", "     FFFFF     ", "    FDEDEDF    ", "   FDEE EEDF   ", "  FDEE   EEDF  ", "  FEE  E  EEFFF", "  FD  E E  DF F", "  FEE  E  EEFFF", "  FDEE   EEDF  ", "   FDEE EEDF   ", "    FDEDEDF    ", "     FFFFF     ", "               ", "               ")
                    .aisle("               ", "               ", "               ", "      EDE      ", "     EEEEE     ", "    EEEEEEE    ", "   EEEEEEEEE   ", "   DEEE EEED   ", "   EEEEEEEEE   ", "    EEEEEEE    ", "     EEEEE     ", "      EDE      ", "               ", "               ", "               ")
                    .aisle("               ", "               ", "               ", "      EEE      ", "     E   E     ", "    E     E    ", "   E       E   ", "   E       E   ", "   E       E   ", "    E     E    ", "     E   E     ", "      EEE      ", "               ", "               ", "               ")
                    .where('A', blocks(GTBlocks.CASING_PRIMITIVE_BRICKS.get())
                            .or(abilities(IMPORT_ITEMS).setMaxGlobalLimited(4, 1))
                            .or(abilities(EXPORT_ITEMS).setMaxGlobalLimited(2, 1)))
                    .where('B', blocks(GTBlocks.STEEL_HULL.get()))
                    .where('C', blocks(GTBlocks.FIREBOX_STEEL.get()))
                    .where('D', blocks(Blocks.STONE_BRICKS))
                    .where('E', blocks(GTBlocks.CASING_PRIMITIVE_BRICKS.get()))
                    .where('F', blocks(ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.Steel)))
                    .where('G', controller(blocks(definition.get())))
                    .where('H', blocks(ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.Steel)))
                    .where(' ', any())
                    .build())
            .workableCasingModel(
                    GTCEu.id("block/casings/solid/machine_primitive_bricks"),
                    GTCEu.id("block/multiblock/primitive_blast_furnace"))
            .tooltips(
                    Component.translatable("gtna.tooltip.leap_pbf.desc", "A Leap Forward in Primitive Technology."),
                    Component.translatable("gtna.tooltip.leap_pbf.speed", "Duration: Starts at 20s (+20s per layer).").withStyle(ChatFormatting.RED),
                    Component.translatable("gtna.tooltip.leap_pbf.parallel", "Parallel: Doubles every layer (Starts at 8x).").withStyle(ChatFormatting.GOLD),
                    Component.translatable("gtna.tooltip.leap_pbf.max", "Max Parallel: 32,000.").withStyle(ChatFormatting.BLUE),
                    Component.translatable("gtna.tooltip.leap_pbf.note", "Trade-off: Taller structure = More items but slower cycle.").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)
            )
            .register();

    public static final MultiblockMachineDefinition INFERNAL_COKE_OVEN = REGISTRATE
            .multiblock("infernal_coke_oven", InfernalCokeOven::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTNARecipeType.INFERNAL_COKE_RECIPES)
            .recipeModifier(InfernalCokeOven::recipeModifier)
            .appearanceBlock(GTBlocks.CASING_BRONZE_BRICKS)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle(
                            "CCCCC",
                            "AAAAA",
                            "BBBBB",
                            "AAAAA",
                            "CCCCC"
                    )
                    .aisle(
                            "CCCCC",
                            "A   A",
                            "B   B",
                            "A   A",
                            "CCCCC"
                    )
                    .aisle(
                            "CCCCC",
                            "A   A",
                            "B   B",
                            "A   A",
                            "CCCCC"
                    )
                    .aisle(
                            "CCCCC",
                            "A   A",
                            "B   B",
                            "A   A",
                            "CCCCC"
                    )
                    .aisle(
                            "CCCCC",
                            "AA~AA",
                            "BBBBB",
                            "AAAAA",
                            "CCCCC"
                    )
                    .where('~', controller(blocks(definition.get())))
                    .where('A', blocks(Blocks.NETHER_BRICKS)
                            .or(abilities(PartAbility.IMPORT_ITEMS).setPreviewCount(1))
                            .or(abilities(PartAbility.EXPORT_ITEMS).setPreviewCount(1))
                            .or(abilities(PartAbility.IMPORT_FLUIDS).setPreviewCount(1))
                            .or(abilities(PartAbility.STEAM).setPreviewCount(1))
                            .or(abilities(PartAbility.EXPORT_FLUIDS).setPreviewCount(1)))
                    .where('B', blocks(GTNABlocks.BREEL_PLATED_CASING.get()))
                    .where('C', blocks(GTNABlocks.STRONZE_WRAPPED_CASING.get()))
                    .where(' ', any())
                    .build())
            .workableCasingModel(
                    new ResourceLocation("minecraft", "block/nether_bricks"),
                    GTNACORE.id("block/overlay/machine/steaminfernalcokeoven")
            )
            .tooltips(
                    Component.translatable("gtna.tooltip.infernal_coke.desc").withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
                    Component.translatable("gtna.tooltip.infernal_coke.speed_bonus").withStyle(ChatFormatting.GOLD),
                    Component.translatable("gtna.tooltip.infernal_coke.max_speed").withStyle(ChatFormatting.RED),
                    Component.translatable("gtna.tooltip.infernal_coke.parallel").withStyle(ChatFormatting.BLUE),
                    Component.translatable("gtna.tooltip.infernal_coke.steam").withStyle(ChatFormatting.GRAY),
                    Component.translatable("gtna.tooltip.infernal_coke.structure").withStyle(ChatFormatting.DARK_GRAY)
            )
            .register();

    public static final MultiblockMachineDefinition HYPER_PRESSURE_REACTOR = REGISTRATE
            .multiblock("hyper_pressure_reactor", HyperPressureReactor::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(HIGH_PRESSURE_REACTOR_RECIPES)
            .recipeModifier(HyperPressureReactor::recipeModifier)
            .appearanceBlock(GTNABlocks.HYPER_PRESSURE_BREEL_CASING)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle(
                            "               ",
                            "      CCC      ",
                            "               "
                    )
                    .aisle(
                            "      BBB      ",
                            "    CCAAACC    ",
                            "      BBB      "
                    )
                    .aisle(
                            "    BB   BB    ",
                            "   DAAB~BAAD   ",
                            "    BB   BB    "
                    )
                    .aisle(
                            "   B       B   ",
                            "  DADC   CDAD  ",
                            "   B       B   "
                    )
                    .aisle(
                            "  B         B  ",
                            " CAD       DAC ",
                            "  B         B  "
                    )
                    .aisle(
                            "  B         B  ",
                            " CAC       CAC ",
                            "  B         B  "
                    )
                    .aisle(
                            " B           B ",
                            "CAC         CAC",
                            " B           B "
                    )
                    .aisle(
                            " B           B ",
                            "CAC         CAC",
                            " B           B "
                    )
                    .aisle(
                            " B           B ",
                            "CAC         CAC",
                            " B           B "
                    )
                    .aisle(
                            "  B         B  ",
                            " CAC       CAC ",
                            "  B         B  "
                    )
                    .aisle(
                            "  B         B  ",
                            " CAD       DAC ",
                            "  B         B  "
                    )
                    .aisle(
                            "   B       B   ",
                            "  DADC   CDAD  ",
                            "   B       B   "
                    )
                    .aisle(
                            "    BB   BB    ",
                            "   DAACCCAAD   ",
                            "    BB   BB    "
                    )
                    .aisle(
                            "      BBB      ",
                            "    CCAAACC    ",
                            "      BBB      "
                    )
                    .aisle(
                            "               ",
                            "      CCC      ",
                            "               "
                    )
                    .where('~', controller(blocks(definition.get())))
                    .where('A', blocks(GTNABlocks.BREEL_PIPE_CASING.get()))
                    .where('B', blocks(GTNABlocks.HYPER_PRESSURE_BREEL_CASING.get())
                            .or(abilities(PartAbility.IMPORT_FLUIDS).setPreviewCount(1))
                            .or(abilities(PartAbility.EXPORT_FLUIDS).setPreviewCount(1)))
                    .where('C', blocks(GTNABlocks.BOROSILICATE_GLASS_BLOCK.get()))
                    .where('D', blocks(GTNABlocks.HYPER_PRESSURE_BREEL_CASING.get())
                            .or(abilities(PartAbility.IMPORT_FLUIDS).setPreviewCount(1))
                            .or(abilities(PartAbility.EXPORT_FLUIDS).setPreviewCount(1)))
                    .build())
            .workableCasingModel(
                    GTNACORE.id("block/casings/hyper_pressure_breel_casing"),
                    GTCEu.id("block/multiblock/steam_grinder"))
            .tooltips(
                    Component.translatable("gtna.tooltip.hyper_pressure.desc", "Pressure-based fluid reaction chamber.")
                            .withStyle(ChatFormatting.GRAY),
                    Component.translatable("gtna.tooltip.hyper_pressure.no_energy", "Requires NO Energy or Steam to operate.")
                            .withStyle(ChatFormatting.GREEN),
                    Component.translatable("gtna.tooltip.hyper_pressure.parallel", "Max Parallel: 1")
                            .withStyle(ChatFormatting.BLUE)
            )
            .register();

    public static final MultiblockMachineDefinition COMPACT_HYPER_PRESSURE_REACTOR = REGISTRATE
            .multiblock("compact_hyper_pressure_reactor", HyperPressureReactor::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(HIGH_PRESSURE_REACTOR_RECIPES)
            .recipeModifier(HyperPressureReactor::recipeModifier)
            .appearanceBlock(GTNABlocks.VIBRATION_SAFE_CASING)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle(
                            "                                               ",
                            "                                               ",
                            "                    DBBBBBD                    ",
                            "                    DBCCCBD                    ",
                            "                    DBBBBBD                    ",
                            "                                               ",
                            "                                               "
                    )
                    .aisle(
                            "                                               ",
                            "                    DBCCCBD                    ",
                            "                   BB     BB                   ",
                            "                   BB     BB                   ",
                            "                   BB     BB                   ",
                            "                    DBCCCBD                    ",
                            "                                               "
                    )
                    .aisle(
                            "                    DBBBBBD                    ",
                            "                   BB     BB                   ",
                            "                BBBBB     BBBBB                ",
                            "                BBBAAAAAAAAABBB                ",
                            "                BBBBB     BBBBB                ",
                            "                   BB     BB                   ",
                            "                    DBBBBBD                    "
                    )
                    .aisle(
                            "                    DBCCCBD                    ",
                            "                BBBBB     BBBBB                ",
                            "              BBBBBAAAAAAAAABBBBB              ",
                            "              BBAAAAAAAAAAAAAAABB              ",
                            "              BBBBBAAAAAAAAABBBBB              ",
                            "                BBBBB     BBBBB                ",
                            "                    DBCCCBD                    "
                    )
                    .aisle(
                            "                    DBBBBBD                    ",
                            "              BBBBBBB     BBBBBBB              ",
                            "            BBBBAAABB     BBAAABBBB            ",
                            "            BBAAAAAAAAAAAAAAAAAAABB            ",
                            "            BBBBAAABB     BBAAABBBB            ",
                            "              BBBBBBB     BBBBBBB              ",
                            "                    DBBBBBD                    "
                    )
                    .aisle(
                            "                                               ",
                            "            BBBBBBB DBCCCBD BBBBBBB            ",
                            "           BBBAABBBBB     BBBBBAABBB           ",
                            "           BAAAAAAABB     BBAAAAAAAB           ",
                            "           BBBAABBBBB     BBBBBAABBB           ",
                            "            BBBBBBB DBCCCBD BBBBBBB            ",
                            "                                               "
                    )
                    .aisle(
                            "                                               ",
                            "           BBBBB               BBBBB           ",
                            "          BBAABBBBB DBBBBBD BBBBBAABB          ",
                            "          BAAAAABBB DBE~EBD BBBAAAAAB          ",
                            "          BBAABBBBB DBBBBBD BBBBBAABB          ",
                            "           BBBBB               BBBBB           ",
                            "                                               "
                    )
                    .aisle(
                            "                                               ",
                            "          BBBB                   BBBB          ",
                            "         BBABBBB               BBBBABB         ",
                            "         BAAAABB               BBAAAAB         ",
                            "         BBABBBB               BBBBABB         ",
                            "          BBBB                   BBBB          ",
                            "                                               "
                    )
                    .aisle(
                            "                                               ",
                            "         BBB                       BBB         ",
                            "        BBABBB                   BBBABB        ",
                            "        BAAABB                   BBAAAB        ",
                            "        BBABBB                   BBBABB        ",
                            "         BBB                       BBB         ",
                            "                                               "
                    )
                    .aisle(
                            "                                               ",
                            "        BBB                         BBB        ",
                            "       BBABB                       BBABB       ",
                            "       BAAAB                       BAAAB       ",
                            "       BBABB                       BBABB       ",
                            "        BBB                         BBB        ",
                            "                                               "
                    )
                    .aisle(
                            "                                               ",
                            "       BBB                           BBB       ",
                            "      BBABB                         BBABB      ",
                            "      BAAAB                         BAAAB      ",
                            "      BBABB                         BBABB      ",
                            "       BBB                           BBB       ",
                            "                                               "
                    )
                    .aisle(
                            "                                               ",
                            "      BBB                             BBB      ",
                            "     BBABB                           BBABB     ",
                            "     BAAAB                           BAAAB     ",
                            "     BBABB                           BBABB     ",
                            "      BBB                             BBB      ",
                            "                                               "
                    )
                    .aisle(
                            "                                               ",
                            "     BBB                               BBB     ",
                            "    BBABB                             BBABB    ",
                            "    BAAAB                             BAAAB    ",
                            "    BBABB                             BBABB    ",
                            "     BBB                               BBB     ",
                            "                                               "
                    )
                    .aisle(
                            "                                               ",
                            "     BBB                               BBB     ",
                            "    BBABB                             BBABB    ",
                            "    BAAAB                             BAAAB    ",
                            "    BBABB                             BBABB    ",
                            "     BBB                               BBB     ",
                            "                                               "
                    )
                    .aisle(
                            "                                               ",
                            "    BBB                                 BBB    ",
                            "   BBABB                               BBABB   ",
                            "   BAAAB                               BAAAB   ",
                            "   BBABB                               BBABB   ",
                            "    BBB                                 BBB    ",
                            "                                               "
                    )
                    .aisle(
                            "                                               ",
                            "    BBB                                 BBB    ",
                            "   BBABB                               BBABB   ",
                            "   BAAAB                               BAAAB   ",
                            "   BBABB                               BBABB   ",
                            "    BBB                                 BBB    ",
                            "                                               "
                    )
                    .aisle(
                            "                                               ",
                            "   BBB                                   BBB   ",
                            "  BBABB                                 BBABB  ",
                            "  BAAAB                                 BAAAB  ",
                            "  BBABB                                 BBABB  ",
                            "   BBB                                   BBB   ",
                            "                                               "
                    )
                    .aisle(
                            "                                               ",
                            "   BBB                                   BBB   ",
                            "  BBABB                                 BBABB  ",
                            "  BAAAB                                 BAAAB  ",
                            "  BBABB                                 BBABB  ",
                            "   BBB                                   BBB   ",
                            "                                               "
                    )
                    .aisle(
                            "                                               ",
                            "   BBB                                   BBB   ",
                            "  BBABB                                 BBABB  ",
                            "  BAAAB                                 BAAAB  ",
                            "  BBABB                                 BBABB  ",
                            "   BBB                                   BBB   ",
                            "                                               "
                    )
                    .aisle(
                            "                                               ",
                            "  BBB                                     BBB  ",
                            " BBABB                                   BBABB ",
                            " BAAAB                                   BAAAB ",
                            " BBABB                                   BBABB ",
                            "  BBB                                     BBB  ",
                            "                                               "
                    )
                    .aisle(
                            "  DDD                                     DDD  ",
                            " DBBBD                                   DBBBD ",
                            "DBBABBD                                 DBBABBD",
                            "DBAAABD                                 DBAAABD",
                            "DBBABBD                                 DBBABBD",
                            " DBBBD                                   DBBBD ",
                            "  DDD                                     DDD  "
                    )
                    .aisle(
                            "  BBB                                     BBB  ",
                            " B   B                                   B   B ",
                            "B  A  B                                 B  A  B",
                            "B AAA B                                 B AAA B",
                            "B  A  B                                 B  A  B",
                            " B   B                                   B   B ",
                            "  BBB                                     BBB  "
                    )
                    .aisle(
                            "  BCB                                     BCB  ",
                            " C   C                                   C   C ",
                            "B  A  B                                 B  A  B",
                            "C AAA E                                 E AAA C",
                            "B  A  B                                 B  A  B",
                            " C   C                                   C   C ",
                            "  BCB                                     BCB  "
                    )
                    .aisle(
                            "  BCB                                     BCB  ",
                            " C   C                                   C   C ",
                            "B  A  B                                 B  A  B",
                            "C AAA C                                 C AAA C",
                            "B  A  B                                 B  A  B",
                            " C   C                                   C   C ",
                            "  BCB                                     BCB  "
                    )
                    .aisle(
                            "  BCB                                     BCB  ",
                            " C   C                                   C   C ",
                            "B  A  B                                 B  A  B",
                            "C AAA E                                 E AAA C",
                            "B  A  B                                 B  A  B",
                            " C   C                                   C   C ",
                            "  BCB                                     BCB  "
                    )
                    .aisle(
                            "  BBB                                     BBB  ",
                            " B   B                                   B   B ",
                            "B  A  B                                 B  A  B",
                            "B AAA B                                 B AAA B",
                            "B  A  B                                 B  A  B",
                            " B   B                                   B   B ",
                            "  BBB                                     BBB  "
                    )
                    .aisle(
                            "  DDD                                     DDD  ",
                            " DBBBD                                   DBBBD ",
                            "DBBABBD                                 DBBABBD",
                            "DBAAABD                                 DBAAABD",
                            "DBBABBD                                 DBBABBD",
                            " DBBBD                                   DBBBD ",
                            "  DDD                                     DDD  "
                    )
                    .aisle(
                            "                                               ",
                            "  BBB                                     BBB  ",
                            " BBABB                                   BBABB ",
                            " BAAAB                                   BAAAB ",
                            " BBABB                                   BBABB ",
                            "  BBB                                     BBB  ",
                            "                                               "
                    )
                    .aisle(
                            "                                               ",
                            "   BBB                                   BBB   ",
                            "  BBABB                                 BBABB  ",
                            "  BAAAB                                 BAAAB  ",
                            "  BBABB                                 BBABB  ",
                            "   BBB                                   BBB   ",
                            "                                               "
                    )
                    .aisle(
                            "                                               ",
                            "   BBB                                   BBB   ",
                            "  BBABB                                 BBABB  ",
                            "  BAAAB                                 BAAAB  ",
                            "  BBABB                                 BBABB  ",
                            "   BBB                                   BBB   ",
                            "                                               "
                    )
                    .aisle(
                            "                                               ",
                            "   BBB                                   BBB   ",
                            "  BBABB                                 BBABB  ",
                            "  BAAAB                                 BAAAB  ",
                            "  BBABB                                 BBABB  ",
                            "   BBB                                   BBB   ",
                            "                                               "
                    )
                    .aisle(
                            "                                               ",
                            "    BBB                                 BBB    ",
                            "   BBABB                               BBABB   ",
                            "   BAAAB                               BAAAB   ",
                            "   BBABB                               BBABB   ",
                            "    BBB                                 BBB    ",
                            "                                               "
                    )
                    .aisle(
                            "                                               ",
                            "    BBB                                 BBB    ",
                            "   BBABB                               BBABB   ",
                            "   BAAAB                               BAAAB   ",
                            "   BBABB                               BBABB   ",
                            "    BBB                                 BBB    ",
                            "                                               "
                    )
                    .aisle(
                            "                                               ",
                            "     BBB                               BBB     ",
                            "    BBABB                             BBABB    ",
                            "    BAAAB                             BAAAB    ",
                            "    BBABB                             BBABB    ",
                            "     BBB                               BBB     ",
                            "                                               "
                    )
                    .aisle(
                            "                                               ",
                            "     BBB                               BBB     ",
                            "    BBABB                             BBABB    ",
                            "    BAAAB                             BAAAB    ",
                            "    BBABB                             BBABB    ",
                            "     BBB                               BBB     ",
                            "                                               "
                    )
                    .aisle(
                            "                                               ",
                            "      BBB                             BBB      ",
                            "     BBABB                           BBABB     ",
                            "     BAAAB                           BAAAB     ",
                            "     BBABB                           BBABB     ",
                            "      BBB                             BBB      ",
                            "                                               "
                    )
                    .aisle(
                            "                                               ",
                            "       BBB                           BBB       ",
                            "      BBABB                         BBABB      ",
                            "      BAAAB                         BAAAB      ",
                            "      BBABB                         BBABB      ",
                            "       BBB                           BBB       ",
                            "                                               "
                    )
                    .aisle(
                            "                                               ",
                            "        BBB                         BBB        ",
                            "       BBABB                       BBABB       ",
                            "       BAAAB                       BAAAB       ",
                            "       BBABB                       BBABB       ",
                            "        BBB                         BBB        ",
                            "                                               "
                    )
                    .aisle(
                            "                                               ",
                            "         BBB                       BBB         ",
                            "        BBABBB                   BBBABB        ",
                            "        BAAABB                   BBAAAB        ",
                            "        BBABBB                   BBBABB        ",
                            "         BBB                       BBB         ",
                            "                                               "
                    )
                    .aisle(
                            "                                               ",
                            "          BBBB                   BBBB          ",
                            "         BBABBBB               BBBBABB         ",
                            "         BAAAABB               BBAAAAB         ",
                            "         BBABBBB               BBBBABB         ",
                            "          BBBB                   BBBB          ",
                            "                                               "
                    )
                    .aisle(
                            "                                               ",
                            "           BBBBB               BBBBB           ",
                            "          BBAABBBBB DBBBBBD BBBBBAABB          ",
                            "          BAAAAABBB DBECEBD BBBAAAAAB          ",
                            "          BBAABBBBB DBBBBBD BBBBBAABB          ",
                            "           BBBBB               BBBBB           ",
                            "                                               "
                    )
                    .aisle(
                            "                                               ",
                            "            BBBBBBB DBCCCBD BBBBBBB            ",
                            "           BBBAABBBBB     BBBBBAABBB           ",
                            "           BAAAAAAABB     BBAAAAAAAB           ",
                            "           BBBAABBBBB     BBBBBAABBB           ",
                            "            BBBBBBB DBCCCBD BBBBBBB            ",
                            "                                               "
                    )
                    .aisle(
                            "                    DBBBBBD                    ",
                            "              BBBBBBB     BBBBBBB              ",
                            "            BBBBAAABB     BBAAABBBB            ",
                            "            BBAAAAAAAAAAAAAAAAAAABB            ",
                            "            BBBBAAABB     BBAAABBBB            ",
                            "              BBBBBBB     BBBBBBB              ",
                            "                    DBBBBBD                    "
                    )
                    .aisle(
                            "                    DBCCCBD                    ",
                            "                BBBBB     BBBBB                ",
                            "              BBBBBAAAAAAAAABBBBB              ",
                            "              BBAAAAAAAAAAAAAAABB              ",
                            "              BBBBBAAAAAAAAABBBBB              ",
                            "                BBBBB     BBBBB                ",
                            "                    DBCCCBD                    "
                    )
                    .aisle(
                            "                    DBBBBBD                    ",
                            "                   BB     BB                   ",
                            "                BBBBB     BBBBB                ",
                            "                BBBAAAAAAAAABBB                ",
                            "                BBBBB     BBBBB                ",
                            "                   BB     BB                   ",
                            "                    DBBBBBD                    "
                    )
                    .aisle(
                            "                                               ",
                            "                    DBCCCBD                    ",
                            "                   BB     BB                   ",
                            "                   BB     BB                   ",
                            "                   BB     BB                   ",
                            "                    DBCCCBD                    ",
                            "                                               "
                    )
                    .aisle(
                            "                                               ",
                            "                                               ",
                            "                    DBBBBBD                    ",
                            "                    DBCCCBD                    ",
                            "                    DBBBBBD                    ",
                            "                                               ",
                            "                                               "
                    )
                    .where('~', controller(blocks(definition.get())))
                    .where('A', blocks(GTNABlocks.STEAM_COMPACT_PIPE_CASING.get()))
                    .where('B', blocks(GTNABlocks.VIBRATION_SAFE_CASING.get())
                            .or(abilities(PartAbility.IMPORT_FLUIDS).setPreviewCount(1))
                            .or(abilities(PartAbility.EXPORT_FLUIDS).setPreviewCount(1))
                    )
                    .where('C', blocks(GTNABlocks.BOROSILICATE_GLASS_BLOCK.get()))
                    .where('D', blocks(ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.Steel)))
                    .where('E',blocks(GTNABlocks.VIBRATION_SAFE_CASING.get())
                            .or(abilities(PartAbility.IMPORT_FLUIDS).setPreviewCount(1))
                            .or(abilities(PartAbility.EXPORT_FLUIDS).setPreviewCount(1))
                    )
                    .build())
            .workableCasingModel(
                    GTNACORE.id("block/casings/vibration_safe_casing"),
                    GTCEu.id("block/multiblock/steam_grinder"))
            .tooltips(
                    Component.translatable("gtna.tooltip.compact_hyper_pressure.desc", "Extreme density fluid processor.")
                            .withStyle(ChatFormatting.DARK_PURPLE),
                    Component.translatable("gtna.tooltip.hyper_pressure.no_energy", "Requires NO Energy or Steam to operate.")
                            .withStyle(ChatFormatting.GREEN),
                    Component.translatable("gtna.tooltip.compact_hyper_pressure.special", "Can process Dense Supercritical Steam from basic resources.")
                            .withStyle(ChatFormatting.GOLD),
                    Component.translatable("gtna.tooltip.compact_hyper_pressure.parallel", "Max Parallel: 512")
                            .withStyle(ChatFormatting.RED, ChatFormatting.BOLD)
            )
            .register();

    public static final MultiblockMachineDefinition VOID_MINER_STEAM_GATE_AGED = REGISTRATE
            .multiblock("void_miner_steam_gate_aged", VoidMinerSteamGateAged::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.DUMMY_RECIPES)
            .appearanceBlock(GTBlocks.CASING_BRONZE_BRICKS)
            .recipeModifier(VoidMinerSteamGateAged::recipeModifier)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("BBBBBBBBB", "BBBBBBBBB", "B       B", "B       B", "B       B", "BBBBBBBBB", "BCCCCCCCB", "BCCCCCCCB", "B       B", "B       B")
                    .aisle("B       B", "B       B", "         ", "         ", "         ", "B   D   B", "C  DDD  C", "C  DDD  C", "   DDD   ", "         ")
                    .aisle("B       B", "B       B", "         ", "    D    ", "   DDD   ", "B  DDD  B", "C DD DD C", "C D   D C", "  D   D  ", "         ")
                    .aisle("B   D   B", "B   D   B", "   DDD   ", "   D D   ", "  DD DD  ", "B D   D B", "C D   D C", "C     D C", " D     D ", "         ")
                    .aisle("B   D   B", "B   D   B", "   D D   ", "  D   D  ", "  D   D  ", "B D   D B", "C     D C", "C     D C", " D     D ", "         ")
                    .aisle("B   D   B", "B   D   B", "   DDD   ", "   D D   ", "  DD DD  ", "B D   D B", "C D   D C", "C     D C", " D     D ", "         ")
                    .aisle("B       B", "B       B", "         ", "    D    ", "   DDD   ", "B  DDD  B", "C DD DD C", "C D   D C", "  D   D  ", "         ")
                    .aisle("B       B", "B       B", "         ", "         ", "         ", "B   D   B", "C  DDD  C", "C  DDD  C", "   DDD   ", "         ")
                    .aisle("BBBBBBBBB", "BBBBEBBBB", "B       B", "B       B", "B       B", "BBBBBBBBB", "BCCCCCCCB", "BCCCCCCCB", "B       B", "B       B")
                    .where(' ', any())
                    .where('B', blocks(GTBlocks.CASING_BRONZE_BRICKS.get())
                            .or(abilities(PartAbility.EXPORT_ITEMS).setPreviewCount(1))
                            .or(abilities(PartAbility.IMPORT_FLUIDS).setPreviewCount(1))
                            .or(abilities(PartAbility.EXPORT_FLUIDS).setPreviewCount(1))
                            .or(abilities(PartAbility.STEAM).setPreviewCount(1)))
                    .where('C', blocks(ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.Steel)))
                    .where('D', blocks(GTNABlocks.BREEL_PLATED_CASING.get()))
                    .where('E', controller(blocks(definition.get())))
                    .build())
            .workableCasingModel(
                    GTCEu.id("block/casings/solid/machine_casing_bronze_plated_bricks"),
                    GTNACORE.id("block/overlay/machine/voidminersteamgateaged"))
            .tooltips(
                    Component.translatable("gtna.tooltip.void_miner.desc")
                            .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC),
                    Component.translatable("gtna.tooltip.void_miner.fluid_req")
                            .withStyle(ChatFormatting.GRAY),
                    Component.empty(),
                    Component.translatable("gtna.tooltip.void_miner.catalyst_info")
                            .withStyle(ChatFormatting.YELLOW),
                    Component.literal("- ")
                            .append(Component.translatable("gtna.tooltip.void_miner.tier_dense"))
                            .withStyle(ChatFormatting.GRAY),
                    Component.literal("- ")
                            .append(Component.translatable("gtna.tooltip.void_miner.tier_super"))
                            .withStyle(ChatFormatting.GRAY),
                    Component.literal("- ")
                            .append(Component.translatable("gtna.tooltip.void_miner.tier_insane"))
                            .withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD),
                    Component.empty(),
                    Component.translatable("gtna.tooltip.void_miner.outputs")
                            .withStyle(ChatFormatting.BLUE)
            )
            .register();

    // ... imports

    public static final MultiblockMachineDefinition INDUSTRIAL_SLAUGHTERHOUSE = REGISTRATE
            .multiblock("industrial_slaughterhouse", IndustrialSlaughterhouse::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTNARecipeType.SLAUGHTERHOUSE_RECIPES) // *Importante: Crie este RecipeType em GTNARecipeType*
            .recipeModifier(IndustrialSlaughterhouse::recipeModifier)
            .appearanceBlock(GTBlocks.CASING_STEEL_SOLID)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("AAAAAAA", "AAAAAAA", "ABBBBBA", "ABBBBBA", "ABBBBBA", "ABBBBBA", "ABBBBBA", "ABBBBBA", "ABBBBBA", "AAAAAAA")
                    .aisle("AAAAAAA", "ACCCCCA", "BDDDDDB", "BDDDDDB", "BDDDDDB", "BDDDDDB", "BDDDDDB", "BDDDDDB", "BEEEEEB", "AAAAAAA")
                    .aisle("AAAAAAA", "ACCCCCA", "BD   DB", "BD   DB", "BD   DB", "BD   DB", "BD   DB", "BD   DB", "BEEEEEB", "AAAAAAA")
                    .aisle("AAAAAAA", "ACCCCCA", "BD   DB", "BD   DB", "BD   DB", "BD   DB", "BD   DB", "BD   DB", "BEEEEEB", "AAAAAAA")
                    .aisle("AAAAAAA", "ACCCCCA", "BD   DB", "BD   DB", "BD   DB", "BD   DB", "BD   DB", "BD   DB", "BEEEEEB", "AAAAAAA")
                    .aisle("AAAAAAA", "ACCCCCA", "BDDDDDB", "BDDDDDB", "BDDDDDB", "BDDDDDB", "BDDDDDB", "BDDDDDB", "BEEEEEB", "AAAAAAA")
                    .aisle("AAAAAAA", "AAA~AAA", "ABBBBBA", "ABBBBBA", "ABBBBBA", "ABBBBBA", "ABBBBBA", "ABBBBBA", "ABBBBBA", "AAAAAAA")
                    .where("~", Predicates.controller(Predicates.blocks(definition.get())))
                    .where("A", Predicates.blocks(GTBlocks.CASING_STEEL_SOLID.get())
                            .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setMaxGlobalLimited(2))
                            .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setMaxGlobalLimited(4))
                            .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(1))
                            .or(Predicates.abilities(PARALLEL_HATCH).setMaxGlobalLimited(1))
                            .or(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1)))
                    .where("B", Predicates.blocks(GTBlocks.CASING_TEMPERED_GLASS.get()))
                    .where("C", Predicates.blocks(GTBlocks.CASING_STEEL_GEARBOX.get()))
                    .where("D", Predicates.blocks(Blocks.IRON_BARS))
                    .where("E", Predicates.blocks(GTBlocks.FIREBOX_STEEL.get()))
                    .where(" ", Predicates.air())
                    .build())
            .workableCasingModel(
                    GTCEu.id("block/casings/solid/machine_casing_solid_steel"),
                    GTCEu.id("block/multiblock/implosion_compressor"))
            .tooltips(
                    Component.translatable("gtna.machine.slaughterhouse.desc"),
                    Component.translatable("gtna.machine.slaughterhouse.mechanics").withStyle(ChatFormatting.GOLD),
                    Component.translatable("gtna.machine.slaughterhouse.circuit1").withStyle(ChatFormatting.GREEN),
                    Component.translatable("gtna.machine.slaughterhouse.circuit2").withStyle(ChatFormatting.RED),
                    Component.translatable("gtna.machine.slaughterhouse.circuit3").withStyle(ChatFormatting.DARK_PURPLE),
                    Component.translatable("gtna.machine.slaughterhouse.circuit4").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD)
            )
            .register();

    public static void init() {
    }
}