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
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.MultiblockShapeInfo;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.gregtechceu.gtceu.common.data.GCYMBlocks;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;

import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.api.machine.multiblock.GTNAPartAbility;
import com.raishxn.gtna.client.renderer.machine.AnnihilateGeneratorRenderer;
import com.raishxn.gtna.client.renderer.machine.EyeOfHarmonyRenderer;
import com.raishxn.gtna.client.renderer.machine.EyeOfWoodRenderer;
import com.raishxn.gtna.common.data.multiblock.DimensionallyTranscendentPatterns;
import com.raishxn.gtna.common.data.multiblock.EyeOfHarmonyAisles;
import com.raishxn.gtna.common.data.multiblock.EyeOfWoodAisles;
import com.raishxn.gtna.common.data.multiblock.GTNAMultiBlockFileReader;
import com.raishxn.gtna.common.machine.multiblock.energy.ArtificialStarMachine;
import com.raishxn.gtna.common.machine.multiblock.energy.IndustrialSlaughterhouse;
import com.raishxn.gtna.common.machine.multiblock.energy.MEStorageMachine;
import com.raishxn.gtna.common.machine.multiblock.energy.NexusMEHyperCoreMachine;
import com.raishxn.gtna.common.machine.multiblock.energy.NexusMolecularForgeMachine;
import com.raishxn.gtna.common.machine.multiblock.noenergy.DimensionallyTranscendentDirtForgeMachine;
import com.raishxn.gtna.common.machine.multiblock.noenergy.EyeOfHarmonyMachine;
import com.raishxn.gtna.common.machine.multiblock.noenergy.EyeOfWoodMachine;
import com.raishxn.gtna.common.machine.multiblock.noenergy.HyperPressureReactor;
import com.raishxn.gtna.common.machine.multiblock.noenergy.InfernalCokeOven;
import com.raishxn.gtna.common.machine.multiblock.noenergy.LeapForwardBlastFurnace;
import com.raishxn.gtna.common.machine.multiblock.part.OutputBoostHatchPartMachine;
import com.raishxn.gtna.common.machine.noenergy.platformdeployment.PlatformDeploymentMachine;
import com.raishxn.gtna.common.machine.multiblock.part.steam.HugeSteamInputBus;
import com.raishxn.gtna.common.machine.multiblock.part.steam.HugeSteamOutputBus;
import com.raishxn.gtna.common.machine.multiblock.part.steam.InfiniteSteamInputBus;
import com.raishxn.gtna.common.machine.multiblock.part.steam.OutputBoostSteamOutputBus;
import com.raishxn.gtna.common.machine.multiblock.part.steam.WirelessSteamInputHatch;
import com.raishxn.gtna.common.machine.multiblock.part.steam.WirelessSteamOutputHatch;
import com.raishxn.gtna.common.machine.multiblock.steam.*;
import com.raishxn.gtna.config.ConfigHolder;
import com.raishxn.gtna.utils.Registries;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static com.gregtechceu.gtceu.api.machine.multiblock.PartAbility.*;
import static com.gregtechceu.gtceu.api.machine.multiblock.PartAbility.EXPORT_ITEMS;
import static com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties.IS_FORMED;
import static com.gregtechceu.gtceu.api.pattern.Predicates.*;
import static com.gregtechceu.gtceu.api.pattern.util.RelativeDirection.*;
import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.createWorkableCasingMachineModel;
import static com.raishxn.gtna.api.registry.GTNARegistry.REGISTRATE;
import static com.raishxn.gtna.common.data.GTNARecipeType.HIGH_PRESSURE_REACTOR_RECIPES;
import static com.raishxn.gtna.common.data.GTNARecipeType.SUPERHEATER_RECIPES;

public class GTNAMachines {

    private static final ResourceLocation OVERLAY_IN = new ResourceLocation("gtna",
            "block/overlay/machine/overlay_steam_wireless_in");
    private static final ResourceLocation OVERLAY_OUT = new ResourceLocation("gtna",
            "block/overlay/machine/overlay_steam_wireless_out");
    private static final ResourceLocation OVERLAY_STEAM_IN = new ResourceLocation("gtceu",
            "block/overlay/machine/overlay_item_hatch_input");
    private static final ResourceLocation OVERLAY_STEAM_OUT = new ResourceLocation("gtceu",
            "block/overlay/machine/overlay_item_hatch_output");
    public static final BiConsumer<ItemStack, List<Component>> GTNA_ADD = (stack, components) -> components
            .add(Component.translatable("gtna.registry.add")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));

    // --- INPUT HATCHES (Recebe Vapor) ---

    public static final MachineDefinition WIRELESS_STEAM_INPUT_HATCH = registerHatch("wirelessSteamInputBronze",
            () -> REGISTRATE
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
                            Component.translatable("gtceu.universal.tooltip.fluid_storage_capacity", 20000))
                    .tooltipBuilder(GTNA_ADD)
                    .register());

    public static final MachineDefinition WIRELESS_STEAM_INPUT_HATCH_STEEL = registerHatch("wirelessSteamInputSteel",
            () -> REGISTRATE
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
                            Component.translatable("gtceu.universal.tooltip.fluid_storage_capacity", Integer.MAX_VALUE))
                    .tooltipBuilder(GTNA_ADD)
                    .register());

    // --- OUTPUT HATCHES (Envia Vapor) ---

    public static final MachineDefinition WIRELESS_STEAM_OUTPUT_HATCH = registerHatch("wirelessSteamOutputBronze",
            () -> REGISTRATE
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
                            Component.translatable("gtceu.universal.tooltip.fluid_storage_capacity", 20000))
                    .tooltipBuilder(GTNA_ADD)
                    .register());

    public static final MachineDefinition WIRELESS_STEAM_OUTPUT_HATCH_STEEL = registerHatch("wirelessSteamOutputSteel",
            () -> REGISTRATE
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
                            Component.translatable("gtceu.universal.tooltip.fluid_storage_capacity", Integer.MAX_VALUE))
                    .tooltipBuilder(GTNA_ADD)
                    .register());

    public static final MachineDefinition HUGE_STEAM_INPUT_BUS = registerHatch("hugeSteamInputBus", () -> REGISTRATE
            .machine("huge_steam_input_bus", HugeSteamInputBus::new)
            .rotationState(RotationState.ALL)
            .tier(GTValues.ULV)
            .abilities(PartAbility.STEAM_IMPORT_ITEMS)
            .modelProperty(IS_FORMED, false)
            .modelProperty(GTMachineModelProperties.IS_STEEL_MACHINE, false)
            .colorOverlaySteamHullModel(OVERLAY_STEAM_IN)
            .tooltipBuilder(GTNA_ADD)
            .tooltips(Component.translatable("gtna.tooltip.huge_steam_bus").withStyle(ChatFormatting.GREEN))
            .register());

    public static final MachineDefinition HUGE_STEAM_OUTPUT_BUS = registerHatch("hugeSteamOutputBus", () -> REGISTRATE
            .machine("huge_steam_output_bus", HugeSteamOutputBus::new)
            .rotationState(RotationState.ALL)
            .tier(GTValues.ULV)
            .abilities(PartAbility.STEAM_EXPORT_ITEMS)
            .modelProperty(IS_FORMED, false)
            .modelProperty(GTMachineModelProperties.IS_STEEL_MACHINE, false)
            .colorOverlaySteamHullModel(OVERLAY_STEAM_OUT)
            .tooltips(Component.translatable("gtna.tooltip.huge_steam_bus").withStyle(ChatFormatting.GREEN))
            .tooltipBuilder(GTNA_ADD)
            .register());

    public static final MachineDefinition INFINITE_STEAM_INPUT_BUS = registerHatch("infiniteSteamInputBus",
            () -> REGISTRATE
                    .machine("infinite_steam_input_bus", InfiniteSteamInputBus::new)
                    .rotationState(RotationState.ALL)
                    .tier(GTValues.ULV)
                    .abilities(PartAbility.STEAM_IMPORT_ITEMS)
                    .modelProperty(IS_FORMED, false)
                    .modelProperty(GTMachineModelProperties.IS_STEEL_MACHINE, false)
                    .colorOverlaySteamHullModel(OVERLAY_STEAM_IN)
                    .tooltips(Component.translatable("gtna.machine.infinite_steam_input_bus.tooltip"))
                    .tooltipBuilder(GTNA_ADD)
                    .register());

    public static final MachineDefinition OUTPUT_BOOST_STEAM_OUTPUT_BUS = registerHatch("outputBoostSteamOutputBus",
            () -> REGISTRATE
                    .machine("output_boost_steam_output_bus", OutputBoostSteamOutputBus::new)
                    .rotationState(RotationState.ALL)
                    .tier(GTValues.ULV)
                    .abilities(PartAbility.STEAM_EXPORT_ITEMS)
                    .modelProperty(IS_FORMED, false)
                    .modelProperty(GTMachineModelProperties.IS_STEEL_MACHINE, false)
                    .colorOverlaySteamHullModel(OVERLAY_STEAM_OUT)
                    .tooltips(Component.translatable("gtna.machine.output_boost_steam_output_bus.tooltip",
                            OutputBoostHatchPartMachine.getMultiplierForTier(GTValues.ULV)))
                    .tooltipBuilder(GTNA_ADD)
                    .register());

    public static final MachineDefinition INDUSTRIAL_PLATFORM_DEPLOYMENT_TOOLS = registerMachine(
            "industrialPlatformDeploymentTools",
            () -> REGISTRATE
                    .machine("industrial_platform_deployment_tools", PlatformDeploymentMachine::new)
                    .rotationState(RotationState.NON_Y_AXIS)
                    .tier(GTValues.HV)
                    .workableCasingModel(
                            GTNACORE.id("block/casings/dyson_deployment_casing"),
                            GTCEu.id("block/multiblock/fusion_reactor"))
                    .tooltips(
                            Component.translatable("gtna.machine.industrial_platform_deployment_tools.tooltip.0"),
                            Component.translatable("gtna.machine.industrial_platform_deployment_tools.tooltip.1"))
                    .tooltipBuilder(GTNA_ADD)
                    .register());

    // --- MULTIBLOCKS ---

    public static final MultiblockMachineDefinition LARGE_STEAM_CRUSHER = registerMachine("largeSteamCrusher",
            () -> REGISTRATE
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
                                    .or(blocks(GTMachines.STEAM_HATCH.getBlock()).setExactLimit(1)))
                            .where('B', blocks(GTBlocks.CASING_STEEL_SOLID.get()))
                            .where('C', blocks(GTBlocks.CASING_BRONZE_BRICKS.get()))
                            .where('D', blocks(GTBlocks.CASING_STEEL_SOLID.get()))
                            .where('E', blocks(GTBlocks.CASING_BRONZE_BRICKS.get()))
                            .where(' ', Predicates.any())
                            .build())
                    .tooltipBuilder(GTNA_ADD)
                    .tooltips(
                            Component.translatable("gtna.tooltip.large_steam_crusher.speed")
                                    .withStyle(ChatFormatting.GOLD),
                            Component.translatable("gtna.tooltip.large_steam_crusher.parallel")
                                    .withStyle(ChatFormatting.BLUE))
                    .register());

    public static final MultiblockMachineDefinition MEGA_PRESSURE_SOLAR_BOILER = registerMachine(
            "megaPressureSolarBoiler", () -> REGISTRATE
                    .multiblock("mega_pressure_solar_boiler", MegaSolarBoilerMachine::new)
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
                                .aisle("A~A")
                                .aisle("ABA")
                                .aisle("AAA")
                                .where('~', definition, Direction.NORTH)
                                .where('A', GTNABlocks.HYPER_PRESSURE_BREEL_CASING.get())
                                .where('B', GTNABlocks.SOLAR_BOILING_CELL.get())
                                .build();
                        return List.of(minShape);
                    })
                    .workableCasingModel(GTNACORE.id("block/casings/mega_pressure_solar_boiler_casing"),
                            GTNACORE.id("block/overlay/machine/solarboiler"))
                    .tooltips(
                            Component
                                    .translatable("gtna.tooltip.mega_solar.desc",
                                            "A massive solar thermal power plant.")
                                    .withStyle(ChatFormatting.GRAY),
                            Component
                                    .translatable("gtna.tooltip.mega_solar.expansion",
                                            "Structure is expandable! Add Solar Pipes behind and to the sides.")
                                    .withStyle(ChatFormatting.GOLD),
                            Component
                                    .translatable("gtna.tooltip.mega_solar.sunlight",
                                            "REQUIREMENT: Every Solar Pipe casing must have direct access to the sky.")
                                    .withStyle(ChatFormatting.RED),
                            Component
                                    .translatable("gtna.tooltip.mega_solar.production",
                                            "Production: 10,000 L/s of Steam per active Pipe Block.")
                                    .withStyle(ChatFormatting.BLUE),
                            Component.translatable("gtna.tooltip.mega_solar.max_size", "Max Size: 33 Wide x 32 Deep.")
                                    .withStyle(ChatFormatting.DARK_GRAY),
                            Component.literal("Warning: May produce more steam than unplayed games in your library")
                                    .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD))
                    .register());

    public static final MultiblockMachineDefinition LARGE_STEAM_FURNACE = registerMachine("largeSteamFurnace",
            () -> REGISTRATE
                    .multiblock("large_steam_furnace", LargeSteamFurnace::new)
                    .rotationState(RotationState.NON_Y_AXIS)
                    .recipeType(GTRecipeTypes.FURNACE_RECIPES)
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
                                    "F_______F")
                            .aisle(
                                    "FGGGGGGGF",
                                    "DAAAAAAAD",
                                    "DAAAAAAAD",
                                    "DAAAAAAAD",
                                    "DAAAAAAAD",
                                    "DAAAAAAAD",
                                    "DAAAAAAAD",
                                    "FGGGGGGGF")
                            .aisle(
                                    "FGGGGGGGF",
                                    "DABBBBBAD",
                                    "DAHHHHHAD",
                                    "DAHHHHHAD",
                                    "DAHHHHHAD",
                                    "DAHHHHHAD",
                                    "DABBBBBAD",
                                    "FGGGGGGGF")
                            .aisle(
                                    "FGGGGGGGF",
                                    "DABBBBBAD",
                                    "DAHDDDHAD",
                                    "DAH   HAD",
                                    "DAH   HAD",
                                    "DAHDDDHAD",
                                    "DABBBBBAD",
                                    "FGGGGGGGF")
                            .aisle(
                                    "FGGGGGGGF",
                                    "DABBBBBAD",
                                    "DAHDDDHAD",
                                    "DAH   HAD",
                                    "DAH   HAD",
                                    "DAHDDDHAD",
                                    "DABBBBBAD",
                                    "FGGGGGGGF")
                            .aisle(
                                    "FGGGGGGGF",
                                    "DABBBBBAD",
                                    "DAHDDDHAD",
                                    "DAH   HAD",
                                    "DAH   HAD",
                                    "DAHDDDHAD",
                                    "DABBBBBAD",
                                    "FGGGGGGGF")
                            .aisle(
                                    "FGGGGGGGF",
                                    "DAAAAAAAD",
                                    "DAHHHHHAD",
                                    "DAHHHHHAD",
                                    "DAHHHHHAD",
                                    "DAHHHHHAD",
                                    "DAAAAAAAD",
                                    "FGGGGGGGF")
                            .aisle(
                                    "FFFFFFFFF",
                                    "DAAAAAAAD",
                                    "DAAAAAAAD",
                                    "DAAASAAAD",
                                    "DAAAAAAAD",
                                    "DAAAAAAAD",
                                    "DAAAAAAAD",
                                    "FFFFFFFFF")
                            .where('S', Predicates.controller(Predicates.blocks(definition.get())))
                            .where('A', Predicates.blocks(GTBlocks.CASING_BRONZE_BRICKS.get())
                                    .or(abilities(PartAbility.STEAM_IMPORT_ITEMS).setPreviewCount(1))
                                    .or(abilities(PartAbility.STEAM_EXPORT_ITEMS).setPreviewCount(1))
                                    .or(blocks(GTMachines.STEAM_HATCH.getBlock()).setExactLimit(1)))
                            .where('B', Predicates.blocks(GTBlocks.CASING_BRONZE_PIPE.get()))
                            .where('C', Predicates.blocks(GTBlocks.CASING_BRONZE_GEARBOX.get()))
                            .where('D',
                                    Predicates.blocks(ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.Bronze)))
                            .where('E', Predicates.blocks(GTBlocks.CASING_STEEL_SOLID.get()))
                            .where('F', Predicates.blocks(GTBlocks.CASING_BRONZE_BRICKS.get()))
                            .where('G', Predicates.blocks(Blocks.STONE_BRICKS))
                            .where('H', Predicates.blocks(GTBlocks.CASING_STEEL_SOLID.get()))
                            .where(' ', Predicates.air())
                            .where('_', Predicates.any())
                            .build())
                    .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_bronze_plated_bricks"),
                            GTCEu.id("block/multiblock/steam_oven"))
                    .tooltips(
                            Component
                                    .translatable("gtna.tooltip.large_steam_furnace.desc",
                                            "An industrial-grade steam smelting facility.")
                                    .withStyle(ChatFormatting.GRAY),

                            Component
                                    .translatable("gtna.tooltip.large_steam_furnace.speed",
                                            "Speed: 900% faster than a standard Steam Furnace.")
                                    .withStyle(ChatFormatting.GOLD),

                            Component
                                    .translatable("gtna.tooltip.large_steam_furnace.efficiency",
                                            "Efficiency: Consumes only 50% of the required Steam.")
                                    .withStyle(ChatFormatting.GREEN),

                            Component
                                    .translatable("gtna.tooltip.large_steam_furnace.parallel",
                                            "Parallelism: Processes up to 128 items simultaneously.")
                                    .withStyle(ChatFormatting.BLUE),

                            Component
                                    .translatable("gtna.tooltip.large_steam_furnace.structure",
                                            "Structure: GTOCore large steam furnace shell. Check JEI for details.")
                                    .withStyle(ChatFormatting.DARK_GRAY),

                            Component.literal(
                                    "Warning: Do not attempt to bake cookies inside. They will vaporize instantly.")
                                    .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD))
                    .register());

    public static final MultiblockMachineDefinition LARGE_STEAM_ALLOY_SMELTER = registerMachine(
            "largeSteamAlloySmelter", () -> REGISTRATE
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
                                    " A ")
                            .aisle(
                                    "BBB",
                                    "A A",
                                    "A A",
                                    "AAA")
                            .aisle(
                                    "BBB",
                                    "A~A",
                                    "AAA",
                                    " A ")
                            .where('~', controller(blocks(definition.get())))
                            .where('A', blocks(GTBlocks.CASING_BRONZE_BRICKS.get())
                                    .or(abilities(PartAbility.STEAM_IMPORT_ITEMS).setPreviewCount(1))
                                    .or(abilities(PartAbility.STEAM_EXPORT_ITEMS).setPreviewCount(1))
                                    .or(blocks(GTMachines.STEAM_HATCH.getBlock()).setExactLimit(1)))
                            .where('B', blocks(GTBlocks.FIREBOX_BRONZE.get()))
                            .where(' ', any())
                            .build())
                    .workableCasingModel(
                            GTCEu.id("block/casings/solid/machine_casing_bronze_plated_bricks"),
                            GTNACORE.id("block/overlay/machine/largesteamalloysmelter"))
                    .tooltips(
                            Component
                                    .translatable("gtna.tooltip.large_steam_alloy.desc",
                                            "High-pressure steam alloying.")
                                    .withStyle(ChatFormatting.GRAY),
                            Component
                                    .translatable("gtna.tooltip.large_steam_alloy.speed",
                                            "Speed: 43% faster than Singleblock.")
                                    .withStyle(ChatFormatting.GOLD),
                            Component
                                    .translatable("gtna.tooltip.large_steam_alloy.parallel",
                                            "Parallel: Processes up to 64 items.")
                                    .withStyle(ChatFormatting.BLUE),
                            Component
                                    .translatable("gtna.tooltip.large_steam_alloy.structure",
                                            "Structure: 3x4x3 (WxHxD). Fireboxes at bottom.")
                                    .withStyle(ChatFormatting.DARK_GRAY))
                    .register());

    public static final MultiblockMachineDefinition LARGE_STEAM_HAMMER = registerMachine("largeSteamHammer",
            () -> REGISTRATE
                    .multiblock("large_steam_hammer", LargeSteamHammer::new)
                    .rotationState(RotationState.NON_Y_AXIS)
                    .recipeType(GTRecipeTypes.FORGE_HAMMER_RECIPES)
                    .recipeModifier(LargeSteamHammer::recipeModifier)
                    .appearanceBlock(GTBlocks.CASING_BRONZE_BRICKS)
                    .pattern(definition -> FactoryBlockPattern.start()
                            .aisle("  AAA  ", "  AAA  ", "   A   ", "       ", "       ", "       ", "       ",
                                    "       ",
                                    "       ", "       ", "   A   ", "  AAA  ", "  AAA  ")
                            .aisle(" AAAAA ", " AADAA ", " CAAAC ", " CACAC ", " C   C ", " C   C ", " C   C ",
                                    " C   C ",
                                    " C   C ", " CACAC ", " CAAAC ", " AADAA ", " AAAAA ")
                            .aisle("AAAAAAA", "AADDDAA", " ADDDA ", " ABEBA ", "  BEB  ", "  BEB  ", "  BEB  ",
                                    "  BEB  ",
                                    "  BEB  ", " ABEBA ", " ADDDA ", "AADDDAA", "AAAAAAA")
                            .aisle("AAAAAAA", "ADDDDDA", "AADDDAA", " CE EC ", "  E E  ", "  E E  ", "  E E  ",
                                    "  E E  ",
                                    "  EDE  ", " CEDEC ", "AADDDAA", "ADDDDDA", "AAAAAAA")
                            .aisle("AAAAAAA", "AADDDAA", " ADDDA ", " ABEBA ", "  BEB  ", "  BEB  ", "  BEB  ",
                                    "  BEB  ",
                                    "  BEB  ", " ABEBA ", " ADDDA ", "AADDDAA", "AAAAAAA")
                            .aisle(" AAAAA ", " AADAA ", " CAAAC ", " CACAC ", " C   C ", " C   C ", " C   C ",
                                    " C   C ",
                                    " C   C ", " CACAC ", " CAAAC ", " AADAA ", " AAAAA ")
                            .aisle("  AAA  ", "  A~A  ", "   A   ", "       ", "       ", "       ", "       ",
                                    "       ",
                                    "       ", "       ", "   A   ", "  AAA  ", "  AAA  ")
                            .where('~', controller(blocks(definition.get())))
                            .where('A', blocks(GTBlocks.CASING_BRONZE_BRICKS.get())
                                    .or(abilities(PartAbility.STEAM_IMPORT_ITEMS).setPreviewCount(1))
                                    .or(abilities(PartAbility.STEAM_EXPORT_ITEMS).setPreviewCount(1))
                                    .or(blocks(GTMachines.STEAM_HATCH.getBlock()).setExactLimit(1)))
                            .where('B', blocks(GTBlocks.CASING_BRONZE_GEARBOX.get()))
                            .where('C', blocks(ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.Bronze)))
                            .where('D', blocks(Blocks.IRON_BLOCK))
                            .where('E', blocks(Blocks.GLASS))
                            .where(' ', Predicates.air())
                            .build())
                    .workableCasingModel(
                            GTCEu.id("block/casings/solid/machine_casing_bronze_plated_bricks"),
                            GTCEu.id("block/multiblock/gcym/large_material_press"))
                    .tooltips(
                            Component.translatable("gtna.tooltip.large_steam_hammer.desc",
                                    "Heavy steam forge hammer based on the GTNH addon layout.")
                                    .withStyle(ChatFormatting.GRAY),
                            Component
                                    .translatable("gtna.tooltip.large_steam_hammer.speed",
                                            "Speed: 100% faster than singleblock.")
                                    .withStyle(ChatFormatting.GOLD),
                            Component.translatable("gtna.tooltip.large_steam_hammer.parallel",
                                    "Parallel: Processes up to 64 items.")
                                    .withStyle(ChatFormatting.BLUE),
                            Component.translatable("gtna.tooltip.large_steam_hammer.structure",
                                    "Structure: 7x13x7 with iron core, bronze frames, and glass columns.")
                                    .withStyle(ChatFormatting.DARK_GRAY))
                    .register());

    public static final MultiblockMachineDefinition LARGE_STEAM_COMPRESSOR = registerMachine("largeSteamCompressor",
            () -> REGISTRATE
                    .multiblock("large_steam_compressor", LargeSteamCompressor::new)
                    .rotationState(RotationState.NON_Y_AXIS)
                    .recipeType(GTRecipeTypes.COMPRESSOR_RECIPES)
                    .recipeModifier(LargeSteamCompressor::recipeModifier)
                    .appearanceBlock(GTBlocks.CASING_BRONZE_BRICKS)
                    .pattern(definition -> FactoryBlockPattern.start()
                            .aisle("  AAA  ", "  AAA  ", "       ", "       ", "       ", "  AAA  ", "  AAA  ")
                            .aisle(" AAAAA ", " ABBBA ", " CEEEC ", " CEEEC ", " CEEEC ", " ABBBA ", " AAAAA ")
                            .aisle("AAAAAAA", "ABDDDBA", " E   E ", " E   E ", " E   E ", "ABDDDBA", "AAAAAAA")
                            .aisle("AAAAAAA", "ABDDDBA", " E   E ", " E   E ", " E   E ", "ABDDDBA", "AAAAAAA")
                            .aisle("AAAAAAA", "ABDDDBA", " E   E ", " E   E ", " E   E ", "ABDDDBA", "AAAAAAA")
                            .aisle(" AAAAA ", " ABBBA ", " CEEEC ", " CEEEC ", " CEEEC ", " ABBBA ", " AAAAA ")
                            .aisle("  AAA  ", "  A~A  ", "       ", "       ", "       ", "  AAA  ", "  AAA  ")
                            .where('~', controller(blocks(definition.get())))
                            .where('A', blocks(GTBlocks.CASING_BRONZE_BRICKS.get())
                                    .or(abilities(PartAbility.STEAM_IMPORT_ITEMS).setPreviewCount(1))
                                    .or(abilities(PartAbility.STEAM_EXPORT_ITEMS).setPreviewCount(1))
                                    .or(blocks(GTMachines.STEAM_HATCH.getBlock()).setExactLimit(1)))
                            .where('B', blocks(GTBlocks.CASING_BRONZE_GEARBOX.get()))
                            .where('C', blocks(ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.Bronze)))
                            .where('D', blocks(Blocks.IRON_BLOCK))
                            .where('E', blocks(Blocks.GLASS))
                            .where(' ', Predicates.air())
                            .build())
                    .workableCasingModel(
                            GTCEu.id("block/casings/solid/machine_casing_bronze_plated_bricks"),
                            GTCEu.id("block/multiblock/implosion_compressor"))
                    .tooltips(
                            Component.translatable("gtna.tooltip.large_steam_compressor.desc",
                                    "High-throughput steam compressor using the GTNH reference shell.")
                                    .withStyle(ChatFormatting.GRAY),
                            Component.translatable("gtna.tooltip.large_steam_compressor.speed",
                                    "Speed: 150% faster than singleblock.")
                                    .withStyle(ChatFormatting.GOLD),
                            Component.translatable("gtna.tooltip.large_steam_compressor.parallel",
                                    "Parallel: Processes up to 48 items.")
                                    .withStyle(ChatFormatting.BLUE),
                            Component.translatable("gtna.tooltip.large_steam_compressor.structure",
                                    "Structure: 7x7x7 with framed compression chamber and glass sides.")
                                    .withStyle(ChatFormatting.DARK_GRAY))
                    .register());

    public static final MultiblockMachineDefinition LARGE_STEAM_EXTRACTOR = registerMachine("largeSteamExtractor",
            () -> REGISTRATE
                    .multiblock("large_steam_extractor", LargeSteamExtractor::new)
                    .rotationState(RotationState.NON_Y_AXIS)
                    .recipeType(GTRecipeTypes.EXTRACTOR_RECIPES)
                    .recipeModifier(LargeSteamExtractor::recipeModifier)
                    .appearanceBlock(GTBlocks.CASING_BRONZE_BRICKS)
                    .pattern(definition -> FactoryBlockPattern.start()
                            .aisle("C   C", "DDDDD", "DAAAD", "DDDDD", " DDD ")
                            .aisle("C   C", "DDCDD", "DAAAD", "DB BD", "DD DD")
                            .aisle("C   C", "DDCDD", "DAAAD", "DB BD", "DD DD")
                            .aisle("C   C", "DDCDD", "DAAAD", "DB BD", "DD DD")
                            .aisle("C   C", "DD~DD", "DAAAD", "DDDDD", " DDD ")
                            .where('~', controller(blocks(definition.get())))
                            .where('A', blocks(GTBlocks.CASING_BRONZE_GEARBOX.get()))
                            .where('B', blocks(GTBlocks.CASING_BRONZE_PIPE.get()))
                            .where('C', blocks(Blocks.GLASS))
                            .where('D', blocks(GTBlocks.CASING_BRONZE_BRICKS.get())
                                    .or(abilities(PartAbility.STEAM_IMPORT_ITEMS).setPreviewCount(1))
                                    .or(abilities(PartAbility.STEAM_EXPORT_ITEMS).setPreviewCount(1))
                                    .or(blocks(GTMachines.STEAM_HATCH.getBlock()).setExactLimit(1)))
                            .where(' ', Predicates.air())
                            .build())
                    .workableCasingModel(
                            GTCEu.id("block/casings/solid/machine_casing_bronze_plated_bricks"),
                            GTCEu.id("block/multiblock/gcym/large_extractor"))
                    .tooltips(
                            Component.translatable("gtna.tooltip.large_steam_extractor.desc",
                                    "Steam extractor with the same compact frame from the GTNH reference addon.")
                                    .withStyle(ChatFormatting.GRAY),
                            Component.translatable("gtna.tooltip.large_steam_extractor.speed",
                                    "Speed: 75% faster than singleblock.")
                                    .withStyle(ChatFormatting.GOLD),
                            Component.translatable("gtna.tooltip.large_steam_extractor.parallel",
                                    "Parallel: Processes up to 48 items.")
                                    .withStyle(ChatFormatting.BLUE),
                            Component.translatable("gtna.tooltip.large_steam_extractor.structure",
                                    "Structure: 5x5x5 pressure cage with bronze pipes and glass vents.")
                                    .withStyle(ChatFormatting.DARK_GRAY))
                    .register());

    public static final MultiblockMachineDefinition LARGE_STEAM_ORE_WASHER = registerMachine("largeSteamOreWasher",
            () -> REGISTRATE
                    .multiblock("large_steam_ore_washer", LargeSteamOreWasher::new)
                    .rotationState(RotationState.NON_Y_AXIS)
                    .recipeType(GTRecipeTypes.ORE_WASHER_RECIPES)
                    .recipeModifier(LargeSteamOreWasher::recipeModifier)
                    .appearanceBlock(GTBlocks.CASING_BRONZE_BRICKS)
                    .pattern(definition -> FactoryBlockPattern.start()
                            .aisle("AAAAAAAAA", "AAAAAAAAA", "AAAAAAAAA", "AAAAAAAAA", "AAAAAAAAA")
                            .aisle("AAAAAAAAA", "A   B   A", "A       A", "A       A", "ACCCCCCCA")
                            .aisle("AAAAAAAAA", "A   B   A", "A       A", "A       A", "ACCCCCCCA")
                            .aisle("AAAAAAAAA", "A   B   A", "A   B   A", "A       A", "ACCCCCCCA")
                            .aisle("AAAAAAAAA", "ABBBBBBBA", "A  BBB  A", "A       A", "ACCCCCCCA")
                            .aisle("AAAAAAAAA", "A   B   A", "A   B   A", "A       A", "ACCCCCCCA")
                            .aisle("AAAAAAAAA", "A   B   A", "A       A", "A       A", "ACCCCCCCA")
                            .aisle("AAAAAAAAA", "A   B   A", "A       A", "A       A", "ACCCCCCCA")
                            .aisle("AAAA~AAAA", "AAAAAAAAA", "AAAAAAAAA", "AAAAAAAAA", "AAAAAAAAA")
                            .where('~', controller(blocks(definition.get())))
                            .where('A', blocks(GTBlocks.CASING_BRONZE_BRICKS.get())
                                    .or(abilities(PartAbility.STEAM_IMPORT_ITEMS).setPreviewCount(1))
                                    .or(abilities(PartAbility.STEAM_EXPORT_ITEMS).setPreviewCount(1))
                                    .or(blocks(GTMachines.STEAM_HATCH.getBlock()).setExactLimit(1))
                                    .or(abilities(IMPORT_FLUIDS).setPreviewCount(1)))
                            .where('B', blocks(GTBlocks.CASING_BRONZE_PIPE.get()))
                            .where('C', blocks(Blocks.GLASS))
                            .where(' ', Predicates.air())
                            .build())
                    .workableCasingModel(
                            GTCEu.id("block/casings/solid/machine_casing_bronze_plated_bricks"),
                            GTCEu.id("block/machines/ore_washer"))
                    .tooltips(
                            Component.translatable("gtna.tooltip.large_steam_ore_washer.desc",
                                    "Large steam ore washer using the reference washing basin layout.")
                                    .withStyle(ChatFormatting.GRAY),
                            Component.translatable("gtna.tooltip.large_steam_ore_washer.speed",
                                    "Speed: 400% faster than singleblock.")
                                    .withStyle(ChatFormatting.GOLD),
                            Component.translatable("gtna.tooltip.large_steam_ore_washer.parallel",
                                    "Parallel: Processes up to 96 items.")
                                    .withStyle(ChatFormatting.BLUE),
                            Component.translatable("gtna.tooltip.large_steam_ore_washer.structure",
                                    "Structure: 9x5x9 basin with glass walls and bronze pipe agitators.")
                                    .withStyle(ChatFormatting.DARK_GRAY))
                    .register());

    public static final MultiblockMachineDefinition LARGE_STEAM_CIRCUIT_ASSEMBLER = registerMachine(
            "largeSteamCircuitAssembler", () -> REGISTRATE
                    .multiblock("large_steam_circuit_assembler", LargeSteamCircuitAssembler::new)
                    .rotationState(RotationState.NON_Y_AXIS)
                    .recipeType(GTRecipeTypes.CIRCUIT_ASSEMBLER_RECIPES)
                    .appearanceBlock(GTBlocks.CASING_BRONZE_BRICKS)
                    .pattern(definition -> FactoryBlockPattern.start()
                            .aisle("AAA", "AAA", "DDD", " D ")
                            .aisle("AAA", "ABA", "DCD", " D ")
                            .aisle("AAA", "ABA", "DCD", " D ")
                            .aisle("AAA", "ABA", "DCD", " D ")
                            .aisle("AAA", "ABA", "DCD", " D ")
                            .aisle("AAA", "ABA", "DCD", " D ")
                            .aisle("AAA", "ABA", "DCD", " D ")
                            .aisle("AAA", "ABA", "DCD", " D ")
                            .aisle("AAA", "ABA", "DCD", " D ")
                            .aisle("AAA", "ASA", "DDD", " D ")
                            .where('S', controller(blocks(definition.get())))
                            .where('A', blocks(GTBlocks.CASING_BRONZE_BRICKS.get())
                                    .or(blocks(GTMachines.STEAM_HATCH.getBlock()).setExactLimit(1)))
                            .where('B', blocks(GTBlocks.CASING_BRONZE_PIPE.get()))
                            .where('C', blocks(GTNABlocks.STEAM_ASSEMBLY_BLOCK.get()))
                            .where('D', blocks(GTBlocks.CASING_BRONZE_BRICKS.get())
                                    .or(abilities(PartAbility.STEAM_IMPORT_ITEMS).setMaxGlobalLimited(1).setPreviewCount(1))
                                    .or(abilities(PartAbility.STEAM_EXPORT_ITEMS).setMaxGlobalLimited(1).setPreviewCount(1))
                                    .or(abilities(IMPORT_FLUIDS).setMaxGlobalLimited(2))
                                    .or(abilities(IMPORT_ITEMS).setMaxGlobalLimited(2))
                                    .or(abilities(EXPORT_ITEMS).setMaxGlobalLimited(1)))
                            .where(' ', any())
                            .build())
                    .workableCasingModel(
                            GTCEu.id("block/casings/solid/machine_casing_bronze_plated_bricks"),
                            GTNACORE.id("block/multiblock/steam_circuit_assembler"))
                    .tooltips(
                            Component.translatable("gtna.tooltip.large_steam_circuit_assembler.desc")
                                    .withStyle(ChatFormatting.GRAY),
                            Component.translatable("gtna.tooltip.large_steam_circuit_assembler.mode")
                                    .withStyle(ChatFormatting.GOLD),
                            Component.translatable("gtna.tooltip.large_steam_circuit_assembler.parallel")
                                    .withStyle(ChatFormatting.BLUE),
                            Component.translatable("gtna.tooltip.large_steam_circuit_assembler.structure")
                                    .withStyle(ChatFormatting.DARK_GRAY))
                    .register());

    public static final MultiblockMachineDefinition LARGE_STEAM_MIXER = registerMachine("largeSteamMixer",
            () -> REGISTRATE
                    .multiblock("large_steam_mixer",
                            holder -> new AdjustableSteamParallelMachine(holder, GTRecipeTypes.MIXER_RECIPES, 64, 64,
                                    0.5, true))
                    .rotationState(RotationState.NON_Y_AXIS)
                    .recipeType(GTRecipeTypes.MIXER_RECIPES)
                    .appearanceBlock(GTBlocks.CASING_BRONZE_BRICKS)
                    .pattern(definition -> FactoryBlockPattern.start()
                            .aisle(" AAAAAAA ", " AAAAAAA ", " AAAAAAA ", " AAAAAAA ", " AAAAAAA ", " AAAAAAA ", " AAAAAAA ")
                            .aisle("AAAAAAAAA", "AA     AA", "AA     AA", "AA     AA", "AA     AA", "AA     AA", "AA  B  AA")
                            .aisle("AAAAAAAAA", "A       A", "A   C   A", "A       A", "A   C   A", "A       A", "A   B   A")
                            .aisle("AAAAAAAAA", "A       A", "A   C   A", "A       A", "A   C   A", "A       A", "A   B   A")
                            .aisle("AAAAAAAAA", "A   D   A", "A CCCCC A", "A   D   A", "A CCCCC A", "A   D   A", "ABBBBBBBA")
                            .aisle("AAAAAAAAA", "A       A", "A   C   A", "A       A", "A   C   A", "A       A", "A   B   A")
                            .aisle("AAAAAAAAA", "A       A", "A   C   A", "A       A", "A   C   A", "A       A", "A   B   A")
                            .aisle("AAAAAAAAA", "AA     AA", "AA     AA", "AA     AA", "AA     AA", "AA     AA", "AA  B  AA")
                            .aisle(" AAAAAAA ", " AAASAAA ", " AAAAAAA ", " AAAAAAA ", " AAAAAAA ", " AAAAAAA ", " AAAAAAA ")
                            .where('S', controller(blocks(definition.get())))
                            .where('A', blocks(GTBlocks.CASING_BRONZE_BRICKS.get())
                                    .or(blocks(GTMachines.STEAM_HATCH.getBlock()).setExactLimit(1))
                                    .or(abilities(PartAbility.STEAM_IMPORT_ITEMS).setMaxGlobalLimited(1))
                                    .or(abilities(PartAbility.STEAM_EXPORT_ITEMS).setMaxGlobalLimited(1))
                                    .or(abilities(IMPORT_FLUIDS).setMaxGlobalLimited(2))
                                    .or(abilities(EXPORT_FLUIDS).setMaxGlobalLimited(1))
                                    .or(abilities(IMPORT_ITEMS).setMaxGlobalLimited(4))
                                    .or(abilities(EXPORT_ITEMS).setMaxGlobalLimited(1)))
                            .where('B', blocks(ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.Bronze)))
                            .where('C', blocks(GTBlocks.CASING_BRONZE_PIPE.get()))
                            .where('D', blocks(GTBlocks.CASING_BRONZE_GEARBOX.get()))
                            .where(' ', any())
                            .build())
                    .workableCasingModel(
                            GTCEu.id("block/casings/solid/machine_casing_bronze_plated_bricks"),
                            GTNACORE.id("block/multiblock/steam_mixer"))
                    .tooltips(
                            Component.translatable("gtna.tooltip.large_steam_mixer.desc")
                                    .withStyle(ChatFormatting.GRAY),
                            Component.translatable("gtna.tooltip.large_steam_mixer.speed")
                                    .withStyle(ChatFormatting.GREEN),
                            Component.translatable("gtna.tooltip.large_steam_mixer.parallel")
                                    .withStyle(ChatFormatting.BLUE),
                            Component.translatable("gtna.tooltip.large_steam_mixer.structure")
                                    .withStyle(ChatFormatting.DARK_GRAY))
                    .register());

    public static final MultiblockMachineDefinition LARGE_STEAM_CENTRIFUGE = registerMachine(
            "largeSteamCentrifuge", () -> REGISTRATE
                    .multiblock("large_steam_centrifuge",
                            holder -> new AdjustableSteamParallelMachine(holder, GTRecipeTypes.CENTRIFUGE_RECIPES, 64,
                                    64, 0.5, true))
                    .rotationState(RotationState.NON_Y_AXIS)
                    .recipeType(GTRecipeTypes.CENTRIFUGE_RECIPES)
                    .appearanceBlock(GTBlocks.CASING_BRONZE_BRICKS)
                    .pattern(definition -> FactoryBlockPattern.start()
                            .aisle("           ", "   AAAAA   ", "  AAAAAAA  ", "   AAAAA   ", "           ")
                            .aisle("  AAAAAAA  ", "  A     A  ", " AB     BA ", "  A     A  ", "  AAAAAAA  ")
                            .aisle(" AAAAAAAAA ", " A       A ", "AB   C   BA", " A       A ", " AAAAAAAAA ")
                            .aisle(" AAAAAAAAA ", "A         A", "A    C    A", "A         A", " AAAAAAAAA ")
                            .aisle(" AAAAAAAAA ", "A    E    A", "A    C    A", "A         A", " AAAAAAAAA ")
                            .aisle(" AAAAAAAAA ", "A   ECE   A", "A CCCCCCC A", "A    C    A", " AAAAFAAAA ")
                            .aisle(" AAAAAAAAA ", "A    E    A", "A    C    A", "A         A", " AAAAAAAAA ")
                            .aisle(" AAAAAAAAA ", "A         A", "A    C    A", "A         A", " AAAAAAAAA ")
                            .aisle(" AAAAAAAAA ", " A       A ", "AB   C   BA", " A       A ", " AAAAAAAAA ")
                            .aisle("  AAAAAAA  ", "  A     A  ", " AB     BA ", "  A     A  ", "  AAAAAAA  ")
                            .aisle("           ", "   AAAAA   ", "  AAASAAA  ", "   AAAAA   ", "           ")
                            .where('S', controller(blocks(definition.get())))
                            .where('A', blocks(GTBlocks.CASING_BRONZE_BRICKS.get())
                                    .or(blocks(GTMachines.STEAM_HATCH.getBlock()).setExactLimit(1))
                                    .or(abilities(PartAbility.STEAM_IMPORT_ITEMS).setMaxGlobalLimited(1))
                                    .or(abilities(PartAbility.STEAM_EXPORT_ITEMS).setMaxGlobalLimited(1))
                                    .or(abilities(IMPORT_ITEMS).setMaxGlobalLimited(1))
                                    .or(abilities(EXPORT_ITEMS).setMaxGlobalLimited(4))
                                    .or(abilities(IMPORT_FLUIDS).setMaxGlobalLimited(1))
                                    .or(abilities(EXPORT_FLUIDS).setMaxGlobalLimited(4)))
                            .where('B', blocks(ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.Bronze)))
                            .where('C', blocks(GTBlocks.CASING_BRONZE_PIPE.get()))
                            .where('E', blocks(GTBlocks.CASING_BRONZE_GEARBOX.get()))
                            .where('F', abilities(MUFFLER).setExactLimit(1))
                            .where(' ', any())
                            .build())
                    .workableCasingModel(
                            GTCEu.id("block/casings/solid/machine_casing_bronze_plated_bricks"),
                            GTNACORE.id("block/multiblock/steam_centrifuge"))
                    .tooltips(
                            Component.translatable("gtna.tooltip.large_steam_centrifuge.desc")
                                    .withStyle(ChatFormatting.GRAY),
                            Component.translatable("gtna.tooltip.large_steam_centrifuge.speed")
                                    .withStyle(ChatFormatting.GREEN),
                            Component.translatable("gtna.tooltip.large_steam_centrifuge.parallel")
                                    .withStyle(ChatFormatting.BLUE),
                            Component.translatable("gtna.tooltip.large_steam_centrifuge.structure")
                                    .withStyle(ChatFormatting.DARK_GRAY))
                    .register());

    public static final MultiblockMachineDefinition LARGE_STEAM_THERMAL_CENTRIFUGE = registerMachine(
            "largeSteamThermalCentrifuge", () -> REGISTRATE
                    .multiblock("large_steam_thermal_centrifuge",
                            holder -> new AdjustableSteamParallelMachine(holder,
                                    GTRecipeTypes.THERMAL_CENTRIFUGE_RECIPES, 64, 64, 0.5, true))
                    .rotationState(RotationState.NON_Y_AXIS)
                    .recipeType(GTRecipeTypes.THERMAL_CENTRIFUGE_RECIPES)
                    .appearanceBlock(GTBlocks.CASING_BRONZE_BRICKS)
                    .pattern(definition -> FactoryBlockPattern.start()
                            .aisle(" AAAAA ", " BBBBB ", " BBBBB ", " BBBBB ", "       ")
                            .aisle("ABBABBA", "BC   CB", "BC   CB", "BC   CB", " BBBBB ")
                            .aisle("ABAAABA", "B     B", "B     B", "B     B", " BBBBB ")
                            .aisle("AAAAAAA", "B  D  B", "B  D  B", "B  D  B", " BBEBB ")
                            .aisle("ABAAABA", "B     B", "B     B", "B     B", " BBBBB ")
                            .aisle("ABBABBA", "BC   CB", "BC   CB", "BC   CB", " BBBBB ")
                            .aisle(" AAAAA ", " BBBBB ", " BBSBB ", " BBBBB ", "       ")
                            .where('S', controller(blocks(definition.get())))
                            .where('A', blocks(GTBlocks.FIREBOX_BRONZE.get()))
                            .where('B', blocks(GTBlocks.CASING_BRONZE_BRICKS.get())
                                    .or(blocks(GTMachines.STEAM_HATCH.getBlock()).setExactLimit(1))
                                    .or(abilities(PartAbility.STEAM_IMPORT_ITEMS).setMaxGlobalLimited(1))
                                    .or(abilities(PartAbility.STEAM_EXPORT_ITEMS).setMaxGlobalLimited(1))
                                    .or(abilities(IMPORT_ITEMS).setMaxGlobalLimited(1))
                                    .or(abilities(EXPORT_ITEMS).setMaxGlobalLimited(3)))
                            .where('C', blocks(ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.Bronze)))
                            .where('D', blocks(GTBlocks.CASING_BRONZE_PIPE.get()))
                            .where('E', abilities(MUFFLER).setExactLimit(1))
                            .where(' ', any())
                            .build())
                    .workableCasingModel(
                            GTCEu.id("block/casings/solid/machine_casing_bronze_plated_bricks"),
                            GTNACORE.id("block/multiblock/steam_thermal_centrifuge"))
                    .tooltips(
                            Component.translatable("gtna.tooltip.large_steam_thermal_centrifuge.desc")
                                    .withStyle(ChatFormatting.GRAY),
                            Component.translatable("gtna.tooltip.large_steam_thermal_centrifuge.speed")
                                    .withStyle(ChatFormatting.GREEN),
                            Component.translatable("gtna.tooltip.large_steam_thermal_centrifuge.parallel")
                                    .withStyle(ChatFormatting.BLUE),
                            Component.translatable("gtna.tooltip.large_steam_thermal_centrifuge.structure")
                                    .withStyle(ChatFormatting.DARK_GRAY))
                    .register());

    public static final MultiblockMachineDefinition LARGE_STEAM_BATH = registerMachine("largeSteamBath",
            () -> REGISTRATE
                    .multiblock("large_steam_bath",
                            holder -> new AdjustableSteamParallelMachine(holder, GTRecipeTypes.CHEMICAL_BATH_RECIPES,
                                    64, 64, 0.5, true))
                    .rotationState(RotationState.NON_Y_AXIS)
                    .recipeType(GTRecipeTypes.CHEMICAL_BATH_RECIPES)
                    .appearanceBlock(GTBlocks.CASING_BRONZE_BRICKS)
                    .pattern(definition -> FactoryBlockPattern.start()
                            .aisle("AAAAAAAAA", "AAAAAAAAA", "AAAAAAAAA", "AAAAAAAAA", "AAAAAAAAA")
                            .aisle("AAAAAAAAA", "ABBBBBBBA", "ABBBDBBBA", "ABBBBBBBA", "AAAAAAAAA")
                            .aisle("AAAAAAAAA", "AB     BA", "AB  D  BA", "AB     BA", "AACCCCCAA")
                            .aisle("AAAAAAAAA", "AB     BA", "A   D   A", "AB     BA", "AACCCCCAA")
                            .aisle("AAAAAAAAA", "AB     BA", "A   D   A", "AB     BA", "AACCCCCAA")
                            .aisle("AAAAAAAAA", "AB     BA", "A   D   A", "AB     BA", "AACCCCCAA")
                            .aisle("AAAAAAAAA", "AB     BA", "A   D   A", "AB     BA", "AACCCCCAA")
                            .aisle("AAAAAAAAA", "AB     BA", "AB  D  BA", "AB     BA", "AACCCCCAA")
                            .aisle("AAAAAAAAA", "ABBBBBBBA", "ABBBDBBBA", "ABBBBBBBA", "AAAAAAAAA")
                            .aisle("AAAAAAAAA", "AAAAAAAAA", "AAAASAAAA", "AAAAAAAAA", "AAAAAAAAA")
                            .where('S', controller(blocks(definition.get())))
                            .where('A', blocks(GTBlocks.CASING_BRONZE_BRICKS.get())
                                    .or(blocks(GTMachines.STEAM_HATCH.getBlock()).setExactLimit(1))
                                    .or(abilities(PartAbility.STEAM_IMPORT_ITEMS).setMaxGlobalLimited(1))
                                    .or(abilities(PartAbility.STEAM_EXPORT_ITEMS).setMaxGlobalLimited(1))
                                    .or(abilities(IMPORT_ITEMS).setMaxGlobalLimited(1))
                                    .or(abilities(EXPORT_ITEMS).setMaxGlobalLimited(3))
                                    .or(abilities(IMPORT_FLUIDS).setMaxGlobalLimited(1)))
                            .where('B', blocks(ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.Bronze)))
                            .where('C', blocks(Blocks.GLASS))
                            .where('D', blocks(ChemicalHelper.getBlock(TagPrefix.block, GTMaterials.Potin)))
                            .where(' ', air())
                            .build())
                    .workableCasingModel(
                            GTCEu.id("block/casings/solid/machine_casing_bronze_plated_bricks"),
                            GTCEu.id("block/machines/chemical_bath"))
                    .tooltips(
                            Component.translatable("gtna.tooltip.large_steam_bath.desc")
                                    .withStyle(ChatFormatting.GRAY),
                            Component.translatable("gtna.tooltip.large_steam_bath.speed")
                                    .withStyle(ChatFormatting.GREEN),
                            Component.translatable("gtna.tooltip.large_steam_bath.parallel")
                                    .withStyle(ChatFormatting.BLUE),
                            Component.translatable("gtna.tooltip.large_steam_bath.structure")
                                    .withStyle(ChatFormatting.DARK_GRAY))
                    .register());

    public static final MultiblockMachineDefinition PRIMITIVE_DISTILLATION_TOWER = registerMachine(
            "primitiveDistillationTower", () -> REGISTRATE
                    .multiblock("primitive_distillation_tower", PrimitiveSteamDistillationTowerMachine::new)
                    .rotationState(RotationState.NON_Y_AXIS)
                    .recipeType(GTRecipeTypes.DISTILLATION_RECIPES)
                    .appearanceBlock(GTBlocks.STEEL_HULL)
                    .pattern(definition -> FactoryBlockPattern.start(RIGHT, BACK, UP)
                            .aisle("A~A", "ASA", "AAA")
                            .aisle("BBB", "B B", "BBB").setRepeatable(5, 5)
                            .aisle("BBB", "BBB", "BBB")
                            .where('~', controller(blocks(definition.get())))
                            .where('A', blocks(GTBlocks.FIREBOX_STEEL.get())
                                    .or(blocks(GTMachines.ITEM_IMPORT_BUS[GTValues.LV].getBlock())
                                            .setMaxGlobalLimited(1))
                                    .or(blocks(GTMachines.ITEM_IMPORT_BUS[GTValues.MV].getBlock())
                                            .setMaxGlobalLimited(1))
                                    .or(blocks(GTMachines.ITEM_EXPORT_BUS[GTValues.LV].getBlock())
                                            .setMaxGlobalLimited(1))
                                    .or(blocks(GTMachines.ITEM_EXPORT_BUS[GTValues.MV].getBlock())
                                            .setMaxGlobalLimited(1))
                                    .or(blocks(GTMachines.FLUID_IMPORT_HATCH[GTValues.LV].getBlock())
                                            .setMaxGlobalLimited(2))
                                    .or(blocks(GTMachines.FLUID_IMPORT_HATCH[GTValues.MV].getBlock())
                                            .setMaxGlobalLimited(2)))
                            .where('S', blocks(GTMachines.STEAM_HATCH.getBlock())
                                    .or(blocks(WIRELESS_STEAM_INPUT_HATCH.getBlock()))
                                    .or(blocks(WIRELESS_STEAM_INPUT_HATCH_STEEL.getBlock())))
                            .where('B', blocks(GTBlocks.STEEL_HULL.get())
                                    .or(blocks(GTMachines.FLUID_EXPORT_HATCH[GTValues.LV].getBlock())
                                            .setMaxGlobalLimited(6))
                                    .or(blocks(GTMachines.FLUID_EXPORT_HATCH[GTValues.MV].getBlock())
                                            .setMaxGlobalLimited(6)))
                            .where(' ', air())
                            .build())
                    .shapeInfos(definition -> {
                        var minShape = MultiblockShapeInfo.builder()
                                .aisle("A~A", "BBB", "BBB", "BBB", "BBB", "BBB", "BBB")
                                .aisle("ASA", "B B", "B B", "B B", "B B", "B B", "BDB")
                                .aisle("AAA", "BDB", "BDB", "BDB", "BDB", "BDB", "BBB")
                                .where('~', definition, Direction.NORTH)
                                .where('A', GTBlocks.FIREBOX_STEEL.get())
                                .where('S', GTMachines.STEAM_HATCH, Direction.NORTH)
                                .where('B', GTBlocks.STEEL_HULL.get())
                                .where('D', GTMachines.FLUID_EXPORT_HATCH[GTValues.LV], Direction.NORTH)
                                .build();
                        return List.of(minShape);
                    })
                    .workableCasingModel(
                            GTCEu.id("block/casings/steam/steel/side"),
                            GTCEu.id("block/multiblock/distillation_tower"))
                    .tooltips(
                            Component.translatable("gtna.tooltip.primitive_distillation_tower.desc")
                                    .withStyle(ChatFormatting.GRAY),
                            Component.translatable("gtna.tooltip.primitive_distillation_tower.parallel")
                                    .withStyle(ChatFormatting.BLUE),
                            Component.translatable("gtna.tooltip.primitive_distillation_tower.structure")
                                    .withStyle(ChatFormatting.DARK_GRAY))
                    .register());

    public static final MultiblockMachineDefinition LARGE_STEAM_LATHE = registerMachine("largeSteamLathe",
            () -> REGISTRATE
                    .multiblock("large_steam_lathe",
                            holder -> new AdjustableSteamParallelMachine(holder, GTRecipeTypes.LATHE_RECIPES, 16, 16,
                                    0.5, true))
                    .rotationState(RotationState.NON_Y_AXIS)
                    .recipeType(GTRecipeTypes.LATHE_RECIPES)
                    .appearanceBlock(GTBlocks.CASING_BRONZE_BRICKS)
                    .pattern(definition -> FactoryBlockPattern.start()
                            .aisle(" BBBBB ", "  BBB  ", "       ", "       ")
                            .aisle("BAAAAAB", "BADDDAB", " AHHHA ", " EFFFE ")
                            .aisle("BAAAAAB", "BA   AB", "BCGGGCB", "BAFFFAB")
                            .aisle("BAAAAAB", "BADDDAB", " AHHHA ", " EFFFE ")
                            .aisle(" BBBBB ", "  B~B  ", "       ", "       ")
                            .where('~', controller(blocks(definition.get())))
                            .where('A', blocks(GTBlocks.CASING_BRONZE_BRICKS.get()))
                            .where('B', blocks(GTBlocks.CASING_BRONZE_BRICKS.get())
                                    .or(blocks(GTMachines.STEAM_HATCH.getBlock()).setExactLimit(1))
                                    .or(abilities(PartAbility.STEAM_IMPORT_ITEMS).setMaxGlobalLimited(1))
                                    .or(abilities(PartAbility.STEAM_EXPORT_ITEMS).setMaxGlobalLimited(1))
                                    .or(abilities(IMPORT_ITEMS).setMaxGlobalLimited(1))
                                    .or(abilities(EXPORT_ITEMS).setMaxGlobalLimited(2)))
                            .where('C', blocks(GTBlocks.CASING_BRONZE_GEARBOX.get()))
                            .where('D', blocks(GTBlocks.CASING_BRONZE_PIPE.get()))
                            .where('E', blocks(ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.Bronze)))
                            .where('F', blocks(GTBlocks.CASING_BRONZE_BRICKS.get()))
                            .where('G', blocks(Blocks.IRON_BLOCK))
                            .where('H', blocks(Blocks.GLASS))
                            .where(' ', any())
                            .build())
                    .workableCasingModel(
                            GTCEu.id("block/casings/solid/machine_casing_bronze_plated_bricks"),
                            GTCEu.id("block/machines/lathe"))
                    .tooltips(
                            Component.translatable("gtna.tooltip.large_steam_lathe.desc")
                                    .withStyle(ChatFormatting.GRAY),
                            Component.translatable("gtna.tooltip.large_steam_lathe.speed")
                                    .withStyle(ChatFormatting.GREEN),
                            Component.translatable("gtna.tooltip.large_steam_lathe.efficiency")
                                    .withStyle(ChatFormatting.GOLD),
                            Component.translatable("gtna.tooltip.large_steam_lathe.parallel")
                                    .withStyle(ChatFormatting.BLUE))
                    .register());

    public static final MultiblockMachineDefinition LARGE_STEAM_CUTTING = registerMachine("largeSteamCutting",
            () -> REGISTRATE
                    .multiblock("large_steam_cutting",
                            holder -> new AdjustableSteamParallelMachine(holder, GTRecipeTypes.CUTTER_RECIPES, 16, 16,
                                    0.5, true))
                    .rotationState(RotationState.NON_Y_AXIS)
                    .recipeType(GTRecipeTypes.CUTTER_RECIPES)
                    .appearanceBlock(GTBlocks.CASING_BRONZE_BRICKS)
                    .pattern(definition -> FactoryBlockPattern.start()
                            .aisle(" GBBBBBG ", " GBBBBBG ", "  GEEEG  ", "   BBB   ")
                            .aisle("AAFFFFFAA", "AAG   GAA", " AE C EA ", "  BGGGB  ")
                            .aisle("AAFFFFFAA", "ADDDDDDDA", " AEHHHEA ", "  BGGGB  ")
                            .aisle("AAFFFFFAA", "AAG   GAA", " AE C EA ", "  BGGGB  ")
                            .aisle(" GBBBBBG ", " GBB~BBG ", "  GEEEG  ", "   BBB   ")
                            .where('~', controller(blocks(definition.get())))
                            .where('A', blocks(GTBlocks.CASING_BRONZE_BRICKS.get()))
                            .where('B', blocks(GTBlocks.CASING_BRONZE_BRICKS.get())
                                    .or(blocks(GTMachines.STEAM_HATCH.getBlock()).setExactLimit(1))
                                    .or(abilities(PartAbility.STEAM_IMPORT_ITEMS).setMaxGlobalLimited(1))
                                    .or(abilities(PartAbility.STEAM_EXPORT_ITEMS).setMaxGlobalLimited(1))
                                    .or(abilities(IMPORT_ITEMS).setMaxGlobalLimited(1))
                                    .or(abilities(EXPORT_ITEMS).setMaxGlobalLimited(2))
                                    .or(abilities(IMPORT_FLUIDS).setMaxGlobalLimited(1)))
                            .where('C', blocks(GTBlocks.CASING_BRONZE_GEARBOX.get()))
                            .where('D', blocks(GTBlocks.CASING_BRONZE_PIPE.get()))
                            .where('E', blocks(ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.Bronze)))
                            .where('F', blocks(Blocks.BRICKS))
                            .where('G', blocks(GTBlocks.CASING_BRONZE_BRICKS.get()))
                            .where('H', blocks(Blocks.DIAMOND_BLOCK))
                            .where(' ', any())
                            .build())
                    .workableCasingModel(
                            GTCEu.id("block/casings/solid/machine_casing_bronze_plated_bricks"),
                            GTCEu.id("block/multiblock/gcym/large_cutter"))
                    .tooltips(
                            Component.translatable("gtna.tooltip.large_steam_cutting.desc")
                                    .withStyle(ChatFormatting.GRAY),
                            Component.translatable("gtna.tooltip.large_steam_cutting.speed")
                                    .withStyle(ChatFormatting.GREEN),
                            Component.translatable("gtna.tooltip.large_steam_cutting.efficiency")
                                    .withStyle(ChatFormatting.GOLD),
                            Component.translatable("gtna.tooltip.large_steam_cutting.parallel")
                                    .withStyle(ChatFormatting.BLUE))
                    .register());

    public static final MultiblockMachineDefinition LARGE_STEAM_FORMING_PRESS = registerMachine(
            "largeSteamFormingPress", () -> REGISTRATE
                    .multiblock("large_steam_forming_press",
                            holder -> new AdjustableSteamParallelMachine(holder, GTRecipeTypes.FORMING_PRESS_RECIPES,
                                    32, 32, 0.4, true))
                    .rotationState(RotationState.NON_Y_AXIS)
                    .recipeType(GTRecipeTypes.FORMING_PRESS_RECIPES)
                    .appearanceBlock(GTBlocks.CASING_BRONZE_BRICKS)
                    .pattern(definition -> FactoryBlockPattern.start()
                            .aisle(" AAA ", " A A ", " AAA ")
                            .aisle("AAAAA", "ABCBA", "AAAAA")
                            .aisle("AAAAA", " C C ", "AAAAA")
                            .aisle("AAAAA", "ABCBA", "AAAAA")
                            .aisle(" AAA ", " A A ", " A~A ")
                            .where('~', controller(blocks(definition.get())))
                            .where('A', blocks(GTBlocks.CASING_BRONZE_BRICKS.get())
                                    .or(blocks(GTMachines.STEAM_HATCH.getBlock()).setExactLimit(1))
                                    .or(abilities(PartAbility.STEAM_IMPORT_ITEMS).setMaxGlobalLimited(1))
                                    .or(abilities(PartAbility.STEAM_EXPORT_ITEMS).setMaxGlobalLimited(1))
                                    .or(abilities(IMPORT_ITEMS).setMaxGlobalLimited(1))
                                    .or(abilities(EXPORT_ITEMS).setMaxGlobalLimited(1)))
                            .where('B', blocks(GTBlocks.CASING_BRONZE_GEARBOX.get()))
                            .where('C', blocks(GTBlocks.CASING_BRONZE_PIPE.get()))
                            .where(' ', any())
                            .build())
                    .workableCasingModel(
                            GTCEu.id("block/casings/solid/machine_casing_bronze_plated_bricks"),
                            GTCEu.id("block/machines/forming_press"))
                    .tooltips(
                            Component.translatable("gtna.tooltip.large_steam_forming_press.desc")
                                    .withStyle(ChatFormatting.GRAY),
                            Component.translatable("gtna.tooltip.large_steam_forming_press.speed")
                                    .withStyle(ChatFormatting.GREEN),
                            Component.translatable("gtna.tooltip.large_steam_forming_press.efficiency")
                                    .withStyle(ChatFormatting.GOLD),
                            Component.translatable("gtna.tooltip.large_steam_forming_press.parallel")
                                    .withStyle(ChatFormatting.BLUE))
                    .register());

    public static final MultiblockMachineDefinition LARGE_STEAM_STORAGE_TANK = registerMachine(
            "largeSteamStorageTank", () -> REGISTRATE
                    .multiblock("large_steam_storage_tank", LargeSteamStorageTank::new)
                    .rotationState(RotationState.NON_Y_AXIS)
                    .recipeType(GTRecipeTypes.DUMMY_RECIPES)
                    .appearanceBlock(GCYMBlocks.CASING_INDUSTRIAL_STEAM)
                    .pattern(definition -> FactoryBlockPattern.start()
                            .aisle("ABBBA", "ABCBA", "ABCBA", "ABABA", "ABABA", "ABABA", "A   A")
                            .aisle("BBBBB", "BDEDB", "BDDDB", "BDEDB", "BDEDB", "BDEDB", " ADA ")
                            .aisle("BBBBB", "FCCCF", "FDCDF", "AEAEA", "AEAEA", "AEAEA", " DFD ")
                            .aisle("BBBBB", "BDEDB", "BDDDB", "BDEDB", "BDEDB", "BDEDB", " ADA ")
                            .aisle("ABBBA", "ABGBA", "ABFBA", "ABABA", "ABABA", "ABABA", "A   A")
                            .where('G', controller(blocks(definition.get())))
                            .where('A', blocks(GTNABlocks.BRASS_REINFORCED_WOODEN_CASING.get()))
                            .where('B', blocks(GCYMBlocks.CASING_INDUSTRIAL_STEAM.get())
                                    .or(blocks(GTMultiMachines.STEEL_TANK_VALVE.get()).setMaxGlobalLimited(2)))
                            .where('C', blocks(ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.Bronze)))
                            .where('D', blocks(GCYMBlocks.CASING_INDUSTRIAL_STEAM.get()))
                            .where('E', blocks(GTBlocks.CASING_BRONZE_PIPE.get()))
                            .where('F', blocks(Blocks.GLASS))
                            .where(' ', any())
                            .build())
                    .workableCasingModel(GTCEu.id("block/casings/gcym/industrial_steam_casing"),
                            GTCEu.id("block/multiblock/multiblock_tank"))
                    .tooltips(
                            Component.translatable("gtna.tooltip.large_steam_storage_tank.desc")
                                    .withStyle(ChatFormatting.GRAY),
                            Component.translatable("gtna.tooltip.large_steam_storage_tank.capacity")
                                    .withStyle(ChatFormatting.BLUE),
                            Component.translatable("gtna.tooltip.large_steam_storage_tank.structure")
                                    .withStyle(ChatFormatting.DARK_GRAY))
                    .register());

    public static final MultiblockMachineDefinition LARGE_STEAM_SOLAR_BOILER = registerMachine(
            "largeSteamSolarBoiler", () -> REGISTRATE
                    .multiblock("large_steam_solar_boiler", LargeSteamSolarBoilerMachine::new)
                    .rotationState(RotationState.NON_Y_AXIS)
                    .recipeType(GTRecipeTypes.DUMMY_RECIPES)
                    .appearanceBlock(GTBlocks.STEEL_HULL)
                    .pattern(definition -> FactoryBlockPattern.start()
                            .aisle("AAAAA")
                            .aisle("ABBBA")
                            .aisle("ABBBA")
                            .aisle("ABBBA")
                            .aisle("AB~BA")
                            .where('A', blocks(GTBlocks.STEEL_HULL.get())
                                    .or(abilities(IMPORT_FLUIDS).setPreviewCount(1))
                                    .or(abilities(EXPORT_FLUIDS).setPreviewCount(1)))
                            .where('B', blocks(GTNABlocks.SOLAR_BOILING_CELL.get()))
                            .where('~', controller(blocks(definition.get())))
                            .build())
                    .shapeInfos(definition -> {
                        var minShape = MultiblockShapeInfo.builder()
                                .aisle("AC~DA")
                                .aisle("ABBBA")
                                .aisle("ABBBA")
                                .aisle("ABBBA")
                                .aisle("AAAAA")
                                .where('~', definition, Direction.NORTH)
                                .where('A', GTBlocks.STEEL_HULL.get())
                                .where('B', GTNABlocks.SOLAR_BOILING_CELL.get())
                                .where('C', GTMachines.FLUID_IMPORT_HATCH[GTValues.LV], Direction.NORTH)
                                .where('D', GTMachines.FLUID_EXPORT_HATCH[GTValues.LV], Direction.NORTH)
                                .build();

                        final int maxL = 63;
                        final int maxR = 63;
                        final int maxB = 125;
                        final int width = maxL + maxR + 1;
                        String controllerRow = "A".repeat(maxL) + "~" + "A".repeat(maxR);
                        String middleRow = "A" + "B".repeat(width - 2) + "A";
                        String boundaryRow = "A".repeat(width);

                        var maxBuilder = MultiblockShapeInfo.builder().aisle(controllerRow);
                        for (int i = 0; i < maxB; i++) {
                            maxBuilder.aisle(middleRow);
                        }
                        var maxShape = maxBuilder.aisle(boundaryRow)
                                .where('~', definition, Direction.NORTH)
                                .where('A', GTBlocks.STEEL_HULL.get())
                                .where('B', GTNABlocks.SOLAR_BOILING_CELL.get())
                                .build();

                        return List.of(minShape, maxShape);
                    })
                    .workableCasingModel(GTCEu.id("block/casings/steam/steel/side"),
                            GTCEu.id("block/multiblock/multiblock_tank"))
                    .tooltips(
                            Component.translatable("gtna.tooltip.large_steam_solar_boiler.desc")
                                    .withStyle(ChatFormatting.GRAY),
                            Component.translatable("gtna.tooltip.large_steam_solar_boiler.expandable")
                                    .withStyle(ChatFormatting.GOLD),
                            Component.translatable("gtna.tooltip.large_steam_solar_boiler.production")
                                    .withStyle(ChatFormatting.BLUE),
                            Component.translatable("gtna.tooltip.large_steam_solar_boiler.structure")
                                    .withStyle(ChatFormatting.DARK_GRAY))
                    .register());

    public static final MultiblockMachineDefinition DIMENSIONALLY_TRANSCENDENT_DIRT_FORGE = registerMachine(
            "dimensionallyTranscendentDirtForge", () -> REGISTRATE
                    .multiblock("dimensionally_transcendent_dirt_forge",
                            DimensionallyTranscendentDirtForgeMachine::new)
                    .rotationState(RotationState.ALL)
                    .recipeType(GTRecipeTypes.PRIMITIVE_BLAST_FURNACE_RECIPES)
                    .appearanceBlock(GTBlocks.CASING_PRIMITIVE_BRICKS)
                    .recipeModifier(DimensionallyTranscendentDirtForgeMachine::recipeModifier)
                    .pattern(definition -> DimensionallyTranscendentPatterns.DTPF
                            .where('a', controller(blocks(definition.get())))
                            .where('e', blocks(GTBlocks.CASING_PRIMITIVE_BRICKS.get())
                                    .or(abilities(EXPORT_ITEMS).setMaxGlobalLimited(2))
                                    .or(abilities(IMPORT_ITEMS).setMaxGlobalLimited(2)))
                            .where('b', blocks(Blocks.BRICKS))
                            .where('C', blocks(Blocks.DIRT))
                            .where('d', blocks(Blocks.STONE_BRICKS))
                            .where('s', blocks(GTBlocks.CASING_PRIMITIVE_BRICKS.get()))
                            .where(' ', any())
                            .build())
                    .additionalDisplay((controller, components) -> {
                        if (controller.isFormed()) {
                            components.add(Component.translatable("gtceu.multiblock.parallel",
                                    Component.literal("524288").withStyle(ChatFormatting.DARK_PURPLE))
                                    .withStyle(ChatFormatting.GRAY));
                        }
                    })
                    .workableCasingModel(GTCEu.id("block/casings/solid/machine_primitive_bricks"),
                            GTCEu.id("block/multiblock/primitive_blast_furnace"))
                    .tooltips(
                            Component.translatable("gtna.tooltip.dimensionally_transcendent_dirt_forge.desc")
                                    .withStyle(ChatFormatting.GRAY),
                            Component.translatable("gtna.tooltip.dimensionally_transcendent_dirt_forge.parallel")
                                    .withStyle(ChatFormatting.BLUE),
                            Component.translatable("gtna.tooltip.dimensionally_transcendent_dirt_forge.structure")
                                    .withStyle(ChatFormatting.DARK_GRAY))
                    .register());

    public static final MultiblockMachineDefinition DIMENSIONALLY_TRANSCENDENT_STEAM_BOILER = registerMachine(
            "dimensionallyTranscendentSteamBoiler", () -> REGISTRATE
                    .multiblock("dimensionally_transcendent_steam_boiler",
                            holder -> new com.gregtechceu.gtceu.common.machine.multiblock.steam.LargeBoilerMachine(
                                    holder, 4_096_000, 32))
                    .rotationState(RotationState.NON_Y_AXIS)
                    .recipeType(GTRecipeTypes.LARGE_BOILER_RECIPES)
                    .recipeModifier(
                            com.gregtechceu.gtceu.common.machine.multiblock.steam.LargeBoilerMachine::recipeModifier)
                    .appearanceBlock(GTBlocks.CASING_TUNGSTENSTEEL_ROBUST)
                    .pattern(definition -> DimensionallyTranscendentPatterns.DTPF
                            .where('a', controller(blocks(definition.get())))
                            .where('e', blocks(GTBlocks.CASING_TUNGSTENSTEEL_ROBUST.get())
                                    .or(abilities(EXPORT_FLUIDS).setMaxGlobalLimited(16))
                                    .or(abilities(IMPORT_FLUIDS).setMaxGlobalLimited(2))
                                    .or(abilities(IMPORT_ITEMS).setMaxGlobalLimited(1)))
                            .where('b', blocks(GTBlocks.CASING_INVAR_HEATPROOF.get()))
                            .where('C', blocks(GCYMBlocks.MOLYBDENUM_DISILICIDE_COIL_BLOCK.get()))
                            .where('d', blocks(GTBlocks.CASING_TUNGSTENSTEEL_ROBUST.get()))
                            .where('s', blocks(GTBlocks.CASING_TUNGSTENSTEEL_PIPE.get()))
                            .where(' ', any())
                            .build())
                    .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_robust_tungstensteel"),
                            GTCEu.id("block/multiblock/generator/large_tungstensteel_boiler"))
                    .tooltips(
                            Component.translatable("gtna.tooltip.dimensionally_transcendent_steam_boiler.desc")
                                    .withStyle(ChatFormatting.GRAY),
                            Component.translatable("gtna.tooltip.dimensionally_transcendent_steam_boiler.output")
                                    .withStyle(ChatFormatting.BLUE),
                            Component.translatable("gtna.tooltip.dimensionally_transcendent_steam_boiler.structure")
                                    .withStyle(ChatFormatting.DARK_GRAY))
                    .register());

    public static final MultiblockMachineDefinition DIMENSIONALLY_TRANSCENDENT_STEAM_OVEN = registerMachine(
            "dimensionallyTranscendentSteamOven", () -> REGISTRATE
                    .multiblock("dimensionally_transcendent_steam_oven", DimensionallyTranscendentSteamOvenMachine::new)
                    .rotationState(RotationState.NON_Y_AXIS)
                    .recipeType(GTRecipeTypes.FURNACE_RECIPES)
                    .appearanceBlock(GTBlocks.CASING_COKE_BRICKS)
                    .pattern(definition -> DimensionallyTranscendentPatterns.DTPF
                            .where('a', controller(blocks(definition.get())))
                            .where('e', blocks(GTBlocks.CASING_BRONZE_BRICKS.get())
                                    .or(blocks(GTMachines.STEAM_HATCH.getBlock()).setExactLimit(1))
                                    .or(abilities(PartAbility.STEAM_IMPORT_ITEMS).setMaxGlobalLimited(1))
                                    .or(abilities(PartAbility.STEAM_EXPORT_ITEMS).setMaxGlobalLimited(1))
                                    .or(abilities(EXPORT_ITEMS).setMaxGlobalLimited(4))
                                    .or(abilities(IMPORT_ITEMS).setMaxGlobalLimited(4)))
                            .where('b', blocks(Blocks.BRICKS))
                            .where('C', blocks(Blocks.DEEPSLATE))
                            .where('d', blocks(Blocks.STONE_BRICKS))
                            .where('s', blocks(GTBlocks.CASING_BRONZE_BRICKS.get()))
                            .where(' ', any())
                            .build())
                    .additionalDisplay((controller, components) -> {
                        if (controller.isFormed()) {
                            components.add(Component.translatable("gtceu.multiblock.parallel",
                                    Component.literal("524288").withStyle(ChatFormatting.DARK_PURPLE))
                                    .withStyle(ChatFormatting.GRAY));
                        }
                    })
                    .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_bronze_plated_bricks"),
                            GTCEu.id("block/multiblock/steam_oven"))
                    .tooltips(
                            Component.translatable("gtna.tooltip.dimensionally_transcendent_steam_oven.desc")
                                    .withStyle(ChatFormatting.GRAY),
                            Component.translatable("gtna.tooltip.dimensionally_transcendent_steam_oven.speed")
                                    .withStyle(ChatFormatting.GREEN),
                            Component.translatable("gtna.tooltip.dimensionally_transcendent_steam_oven.threads")
                                    .withStyle(ChatFormatting.AQUA),
                            Component.translatable("gtna.tooltip.dimensionally_transcendent_steam_oven.parallel")
                                    .withStyle(ChatFormatting.BLUE),
                            Component.translatable("gtna.tooltip.dimensionally_transcendent_steam_oven.structure")
                                    .withStyle(ChatFormatting.DARK_GRAY))
                    .register());

    public static final MultiblockMachineDefinition STEAM_COBBLER = registerMachine("steamCobbler", () -> REGISTRATE
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
                            "           ")
                    .aisle(
                            "ABBBBABBBBA",
                            "AGGGGAGGGGA",
                            "           ",
                            "   A   A   ",
                            "           ",
                            "           ")
                    .aisle(
                            "ABAAAAAAABA",
                            "AGGGFEFGGGA",
                            "           ",
                            "   A   A   ",
                            "           ",
                            "           ")
                    .aisle(
                            "ABACCCCCABA",
                            "AGGFEEEFGGA",
                            "A  D   D  A",
                            " AAD   DAA ",
                            "           ",
                            "           ")
                    .aisle(
                            "ABACCCCCABA",
                            "AGFEEEEEFGA",
                            "     E     ",
                            "     E     ",
                            "     E     ",
                            "     E     ")
                    .aisle(
                            "AAACCCCCAAA",
                            " AEEEEEEEA ",
                            "    E E    ",
                            "    E E    ",
                            "    E E    ",
                            "    EEE    ")
                    .aisle(
                            "ABACCCCCABA",
                            "AGFEEEEEFGA",
                            "     E     ",
                            "     E     ",
                            "     E     ",
                            "     E     ")
                    .aisle(
                            "ABACCCCCABA",
                            "AGGFEEEFGGA",
                            "A  D   D  A",
                            " AAD   DAA ",
                            "           ",
                            "           ")
                    .aisle(
                            "ABAAAAAAABA",
                            "AGGGFEFGGGA",
                            "           ",
                            "   A   A   ",
                            "           ",
                            "           ")
                    .aisle(
                            "ABBBBABBBBA",
                            "AGGGGAGGGGA",
                            "           ",
                            "   A   A   ",
                            "           ",
                            "           ")
                    .aisle(
                            "AAAAAAAAAAA",
                            " AAAA~AAAA ",
                            "   A   A   ",
                            "           ",
                            "           ",
                            "           ")
                    .where(' ', any())
                    .where('A', blocks(GTBlocks.CASING_BRONZE_BRICKS.get())
                            .or(abilities(PartAbility.IMPORT_ITEMS).setPreviewCount(1))
                            .or(abilities(PartAbility.STEAM_EXPORT_ITEMS).setPreviewCount(1))
                            .or(blocks(GTMachines.STEAM_HATCH.getBlock()).setExactLimit(1)))
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
                    Component
                            .translatable("gtna.tooltip.steam_cobbler.modes",
                                    "Generates various stones based on Programmed Circuits.")
                            .withStyle(ChatFormatting.GOLD),
                    Component
                            .translatable("gtna.tooltip.steam_cobbler.consumption",
                                    "Steam Consumption: 1200 L/s (60 L/t)")
                            .withStyle(ChatFormatting.RED),
                    Component.translatable("gtna.tooltip.steam_cobbler.parallel", "Max Parallel: 16 operations.")
                            .withStyle(ChatFormatting.BLUE),
                    Component
                            .translatable("gtna.tooltip.steam_cobbler.structure",
                                    "Structure: 3x3x3 Cube with Bronze Pipe center.")
                            .withStyle(ChatFormatting.DARK_GRAY))
            .register());

    // Em GTNAMachines.java

    public static final MultiblockMachineDefinition STONE_SUPERHEATER = registerMachine("stoneSuperheater",
            () -> REGISTRATE
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
                                    "AAA")
                            .aisle(
                                    "AAA",
                                    "BCB",
                                    "BCB",
                                    "BCB",
                                    "AAA")
                            .aisle(
                                    "A~A",
                                    "ABA",
                                    "ABA",
                                    "ABA",
                                    "AAA")
                            .where('~', controller(blocks(definition.get())))
                            .where('A', blocks(GTNABlocks.STRONZE_WRAPPED_CASING.get())
                                    .or(abilities(PartAbility.IMPORT_ITEMS).setPreviewCount(1))
                                    .or(abilities(PartAbility.EXPORT_FLUIDS).setPreviewCount(1))
                                    .or(blocks(GTMachines.STEAM_HATCH.getBlock()).setExactLimit(1)))
                            .where('B', blocks(GTNABlocks.BOROSILICATE_GLASS_BLOCK.get()))
                            .where('C', blocks(Blocks.MAGMA_BLOCK))
                            .build())
                    .workableCasingModel(
                            GTNACORE.id("block/casings/stronze_wrapped_casing"),
                            GTNACORE.id("block/overlay/machine/stonesuperheater"))
                    .tooltips(
                            Component.translatable("gtna.tooltip.stone_superheater.desc", "Extreme heat stone melting.")
                                    .withStyle(ChatFormatting.GRAY),
                            Component.translatable("gtna.tooltip.stone_superheater.parallel", "Max Parallel: 32")
                                    .withStyle(ChatFormatting.BLUE),
                            Component.translatable("gtna.tooltip.stone_superheater.steam",
                                    "Steam Cost: 640 L/s per active recipe.").withStyle(ChatFormatting.RED))
                    .register());
    public static final MultiblockMachineDefinition STEAM_MANUFACTURER = registerMachine("steamManufacturer",
            () -> REGISTRATE
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
                                    "         ")
                            .aisle(
                                    "CCCCCCC  ",
                                    "D     D  ",
                                    "C     C  ",
                                    "         ",
                                    "         ",
                                    "         ",
                                    "         ")
                            .aisle(
                                    "CCCCCCCCC",
                                    "D EEE EA ",
                                    "C     CA ",
                                    "       A ",
                                    "       A ",
                                    "     AABA",
                                    "       A ")
                            .aisle(
                                    "CCCCCCCCC",
                                    "D E EEEEC",
                                    "C     CEC",
                                    "   B   EC",
                                    "   B   EC",
                                    "   BBBBBC",
                                    "    AAACA")
                            .aisle(
                                    "CCCCCCCCC",
                                    "D EEE EA ",
                                    "C     CA ",
                                    "       A ",
                                    "       A ",
                                    "     AABA",
                                    "       A ")
                            .aisle(
                                    "CCCCCCC  ",
                                    "D     D  ",
                                    "C     C  ",
                                    "         ",
                                    "         ",
                                    "         ",
                                    "         ")
                            .aisle(
                                    " CCCCC   ",
                                    " DD~DD   ",
                                    " CCCCC   ",
                                    "         ",
                                    "         ",
                                    "         ",
                                    "         ")
                            .where('~', controller(blocks(definition.get())))
                            .where('A', blocks(GTNABlocks.BREEL_PIPE_CASING.get()))
                            .where('B', blocks(GTNABlocks.HYDRAULIC_ASSEMBLER_CASING.get()))
                            .where('C', blocks(GTNABlocks.BREEL_PLATED_CASING.get())
                                    .or(abilities(PartAbility.IMPORT_ITEMS).setPreviewCount(1))
                                    .or(abilities(EXPORT_ITEMS).setPreviewCount(1))
                                    .or(blocks(GTMachines.STEAM_HATCH.getBlock()).setExactLimit(1)))
                            .where('D', blocks(GTBlocks.CASING_STEEL_GEARBOX.get()))
                            .where('E', blocks(ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.Steel)))
                            .where(' ', any())
                            .build())
                    .workableCasingModel(
                            GTNACORE.id("block/casings/breel_plated_casing"),
                            GTNACORE.id("block/overlay/machine/steammanufacturer"))
                    .tooltips(
                            Component
                                    .translatable("gtna.tooltip.steam_manufacturer.desc",
                                            "Advanced Hydraulic Assembly Line.")
                                    .withStyle(ChatFormatting.GRAY),
                            Component.translatable("gtna.tooltip.steam_manufacturer.parallel", "Max Parallel: 16")
                                    .withStyle(ChatFormatting.BLUE),
                            Component.translatable("gtna.tooltip.steam_manufacturer.type",
                                    "Recipe Type: Hydraulic Manufacturing").withStyle(ChatFormatting.GOLD))
                    .register());
    public static final MultiblockMachineDefinition STEAM_WOODCUTTER = registerMachine("steamWoodcutter",
            () -> REGISTRATE
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
                                    "  BBB  ")
                            .aisle(
                                    " BBABB ",
                                    "  DDD  ",
                                    "  DDD  ",
                                    "  DDD  ",
                                    "  DDD  ",
                                    "  DDD  ",
                                    " BBABB ")
                            .aisle(
                                    "BBEEEBB",
                                    " D   D ",
                                    " D   D ",
                                    " D   D ",
                                    " D   D ",
                                    " D   D ",
                                    "BBACABB")
                            .aisle(
                                    "BAEEEAB",
                                    " D   D ",
                                    " D   D ",
                                    " D   D ",
                                    " D   D ",
                                    " D   D ",
                                    "BACCCAB")
                            .aisle(
                                    "BBEEEBB",
                                    " D   D ",
                                    " D   D ",
                                    " D   D ",
                                    " D   D ",
                                    " D   D ",
                                    "BBACABB")
                            .aisle(
                                    " BBABB ",
                                    "  DDD  ",
                                    "  DDD  ",
                                    "  DDD  ",
                                    "  DDD  ",
                                    "  DDD  ",
                                    " BBABB ")
                            .aisle(
                                    "  B~B  ",
                                    "       ",
                                    "       ",
                                    "       ",
                                    "       ",
                                    "       ",
                                    "  BBB  ")
                            .where('~', controller(blocks(definition.get())))
                            .where('A', blocks(GTNABlocks.IRON_REINFORCED_WOOD.get()))
                            .where('B', blocks(GTNABlocks.BRONZE_REINFORCED_WOOD.get())
                                    .or(abilities(PartAbility.STEAM_IMPORT_ITEMS).setPreviewCount(1))
                                    .or(abilities(PartAbility.STEAM_EXPORT_ITEMS).setPreviewCount(1))
                                    .or(blocks(GTMachines.STEAM_HATCH.getBlock()).setExactLimit(1)))
                            .where('C', blocks(GTNABlocks.STEEL_REINFORCED_WOOD.get()))
                            .where('D', blocks(Blocks.GLASS))
                            .where('E', blocks(Blocks.DIRT))
                            .where(' ', any())
                            .build())
                    .workableCasingModel(
                            GTNACORE.id("block/casings/bronze_reinforced_wood"),
                            GTNACORE.id("block/overlay/machine/steamwoodcutter"))
                    .tooltips(
                            Component.translatable("gtna.tooltip.steam_woodcutter.desc", "Industrial Tree Processor.")
                                    .withStyle(ChatFormatting.GRAY),
                            Component.translatable("gtna.tooltip.steam_woodcutter.parallel", "Max Parallel: 64")
                                    .withStyle(ChatFormatting.BLUE),
                            Component.translatable("gtna.tooltip.steam_woodcutter.steam", "Base Steam: 1200 L/s")
                                    .withStyle(ChatFormatting.RED),
                            Component
                                    .translatable("gtna.tooltip.steam_woodcutter.info",
                                            "Processes saplings into huge amounts of resources without consuming them.")
                                    .withStyle(ChatFormatting.GOLD))
                    .register());

    public static final MultiblockMachineDefinition LEAP_FORWARD_ONE_BLAST_FURNACE = registerMachine(
            "leapForwardOneBlastFurnace", () -> REGISTRATE
                    .multiblock("leap_forward_one_blast_furnace", LeapForwardBlastFurnace::new)
                    .rotationState(RotationState.NON_Y_AXIS)
                    .recipeType(GTRecipeTypes.PRIMITIVE_BLAST_FURNACE_RECIPES)
                    .recipeModifier(LeapForwardBlastFurnace::recipeModifier)
                    .appearanceBlock(GTBlocks.CASING_PRIMITIVE_BRICKS)
                    .pattern(definition -> FactoryBlockPattern.start(BACK, RIGHT, UP)
                            .aisle("     AAAAA     ", "  DDDDDDDDDDD  ", " DDDDDDDDDDDDD ", " DDDDDDDDDDDDD ",
                                    " DDDDDDDDDDDDD ", "ADDDDDDDDDDDDDA", "ADDDDDDDDDDDDDA", "ADDDDDDDDDDDDDA",
                                    "ADDDDDDDDDDDDDA", "ADDDDDDDDDDDDDA", " DDDDDDDDDDDDD ", " DDDDDDDDDDDDD ",
                                    " DDDDDDDDDDDDD ", "  DDDDDDDDDDD  ", "     AAAAA     ")
                            .aisle("     AAAAA     ", "    DEEEEED    ", "   DE     ED   ", "  DE       ED  ",
                                    " DE         ED ", "AE           EA", "AE           EA", "GE           EA",
                                    "AE           EA", "AE           EA", " DE         ED ", "  DE       ED  ",
                                    "   DE     ED   ", "    DEEEEED    ", "     AAAAA     ")
                            .aisle("     BCCCB     ", "    D     D    ", "   D       D   ", "  D         D  ",
                                    " D           D ", "B             B", "C             C", "C             C",
                                    "C             C", "B             B", " D           D ", "  D         D  ",
                                    "   D       D   ", "    D     D    ", "     BCCCB     ")
                            .aisle("     BCCCB     ", "    D     D    ", "   D       D   ", "  D         D  ",
                                    " D           D ", "B             B", "C             C", "C             C",
                                    "C             C", "B             B", " D           D ", "  D         D  ",
                                    "   D       D   ", "    D     D    ", "     BCCCB     ")
                            .aisle("     DDDDD     ", "    DEEEEED    ", "   DE     ED   ", "  DE       ED  ",
                                    " DE         ED ", "DE           ED", "DE           ED", "DE           ED",
                                    "DE           ED", "DE           ED", " DE         ED ", "  DE       ED  ",
                                    "   DE     ED   ", "    DEEEEED    ", "     DDDDD     ")
                            .aisle("               ", "     DDDDD     ", "    DDEEEDD    ", "   DEDFFFDED   ",
                                    "  DEEDFFFDEED  ", " DDDDDDDDDDDDDD", " DEFFDE EDFFEDF", " DEFFD   DFFEDF",
                                    " DEFFDE EDFFEDF", " DDDDDDDDDDDDDD", "  DEEDFFFDEED  ", "   DEDFFFDED   ",
                                    "    DDEEEDD    ", "     DDDDD     ", "               ")
                            .aisle("               ", "       D       ", "      EDE      ", "    EE   EE    ",
                                    "   EE     EE   ", "   E       E   ", "  E    E    E F", " DD   E E   DD ",
                                    "  E    E    E F", "   E       E   ", "   EE     EE   ", "    EE   EE    ",
                                    "      EDE      ", "       D       ", "               ")
                            .aisle("               ", "               ", "      EDE      ", "     E   E     ",
                                    "    E     E    ", "   E       E   ", "  E    E    E F", "  D   E E   D  ",
                                    "  E    E    E H", "   E       E   ", "    E     E    ", "     E   E     ",
                                    "      EDE      ", "               ", "               ")
                            .setRepeatable(2, 16)
                            .aisle("               ", "               ", "      DDD      ", "     DEEED     ",
                                    "    D     D    ", "   D       D   ", "  DE   E   ED F", "  DE  E E  ED  ",
                                    "  DE   E   ED F", "   D       D   ", "    D     D    ", "     DEEED     ",
                                    "      DDD      ", "               ", "               ")
                            .aisle("               ", "               ", "     FFFFF     ", "    FDEDEDF    ",
                                    "   FDEE EEDF   ", "  FDEE   EEDF  ", "  FEE  E  EEFFF", "  FD  E E  DF F",
                                    "  FEE  E  EEFFF", "  FDEE   EEDF  ", "   FDEE EEDF   ", "    FDEDEDF    ",
                                    "     FFFFF     ", "               ", "               ")
                            .aisle("               ", "               ", "               ", "      EDE      ",
                                    "     EEEEE     ", "    EEEEEEE    ", "   EEEEEEEEE   ", "   DEEE EEED   ",
                                    "   EEEEEEEEE   ", "    EEEEEEE    ", "     EEEEE     ", "      EDE      ",
                                    "               ", "               ", "               ")
                            .aisle("               ", "               ", "               ", "      EEE      ",
                                    "     E   E     ", "    E     E    ", "   E       E   ", "   E       E   ",
                                    "   E       E   ", "    E     E    ", "     E   E     ", "      EEE      ",
                                    "               ", "               ", "               ")
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
                            Component.translatable("gtna.tooltip.leap_pbf.desc",
                                    "A Leap Forward in Primitive Technology."),
                            Component
                                    .translatable("gtna.tooltip.leap_pbf.speed",
                                            "Duration: Starts at 20s (+20s per layer).")
                                    .withStyle(ChatFormatting.RED),
                            Component.translatable("gtna.tooltip.leap_pbf.parallel",
                                    "Parallel: Doubles every layer (Starts at 8x).").withStyle(ChatFormatting.GOLD),
                            Component.translatable("gtna.tooltip.leap_pbf.max", "Max Parallel: 32,000.")
                                    .withStyle(ChatFormatting.BLUE),
                            Component
                                    .translatable("gtna.tooltip.leap_pbf.note",
                                            "Trade-off: Taller structure = More items but slower cycle.")
                                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))
                    .register());

    public static final MultiblockMachineDefinition INFERNAL_COKE_OVEN = registerMachine("infernalCokeOven",
            () -> REGISTRATE
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
                                    "CCCCC")
                            .aisle(
                                    "CCCCC",
                                    "A   A",
                                    "B   B",
                                    "A   A",
                                    "CCCCC")
                            .aisle(
                                    "CCCCC",
                                    "A   A",
                                    "B   B",
                                    "A   A",
                                    "CCCCC")
                            .aisle(
                                    "CCCCC",
                                    "A   A",
                                    "B   B",
                                    "A   A",
                                    "CCCCC")
                            .aisle(
                                    "CCCCC",
                                    "AA~AA",
                                    "BBBBB",
                                    "AAAAA",
                                    "CCCCC")
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
                            GTNACORE.id("block/overlay/machine/steaminfernalcokeoven"))
                    .tooltips(
                            Component.translatable("gtna.tooltip.infernal_coke.desc").withStyle(ChatFormatting.DARK_RED,
                                    ChatFormatting.ITALIC),
                            Component.translatable("gtna.tooltip.infernal_coke.speed_bonus")
                                    .withStyle(ChatFormatting.GOLD),
                            Component.translatable("gtna.tooltip.infernal_coke.max_speed")
                                    .withStyle(ChatFormatting.RED),
                            Component.translatable("gtna.tooltip.infernal_coke.parallel")
                                    .withStyle(ChatFormatting.BLUE),
                            Component.translatable("gtna.tooltip.infernal_coke.steam").withStyle(ChatFormatting.GRAY),
                            Component.translatable("gtna.tooltip.infernal_coke.structure")
                                    .withStyle(ChatFormatting.DARK_GRAY))
                    .register());

    public static final MultiblockMachineDefinition HYPER_PRESSURE_REACTOR = registerMachine("hyperPressureReactor",
            () -> REGISTRATE
                    .multiblock("hyper_pressure_reactor", HyperPressureReactor::new)
                    .rotationState(RotationState.NON_Y_AXIS)
                    .recipeType(HIGH_PRESSURE_REACTOR_RECIPES)
                    .recipeModifier(HyperPressureReactor::recipeModifier)
                    .appearanceBlock(GTNABlocks.HYPER_PRESSURE_BREEL_CASING)
                    .pattern(definition -> FactoryBlockPattern.start()
                            .aisle(
                                    "               ",
                                    "      CCC      ",
                                    "               ")
                            .aisle(
                                    "      BBB      ",
                                    "    CCAAACC    ",
                                    "      BBB      ")
                            .aisle(
                                    "    BB   BB    ",
                                    "   DAAB~BAAD   ",
                                    "    BB   BB    ")
                            .aisle(
                                    "   B       B   ",
                                    "  DADC   CDAD  ",
                                    "   B       B   ")
                            .aisle(
                                    "  B         B  ",
                                    " CAD       DAC ",
                                    "  B         B  ")
                            .aisle(
                                    "  B         B  ",
                                    " CAC       CAC ",
                                    "  B         B  ")
                            .aisle(
                                    " B           B ",
                                    "CAC         CAC",
                                    " B           B ")
                            .aisle(
                                    " B           B ",
                                    "CAC         CAC",
                                    " B           B ")
                            .aisle(
                                    " B           B ",
                                    "CAC         CAC",
                                    " B           B ")
                            .aisle(
                                    "  B         B  ",
                                    " CAC       CAC ",
                                    "  B         B  ")
                            .aisle(
                                    "  B         B  ",
                                    " CAD       DAC ",
                                    "  B         B  ")
                            .aisle(
                                    "   B       B   ",
                                    "  DADC   CDAD  ",
                                    "   B       B   ")
                            .aisle(
                                    "    BB   BB    ",
                                    "   DAACCCAAD   ",
                                    "    BB   BB    ")
                            .aisle(
                                    "      BBB      ",
                                    "    CCAAACC    ",
                                    "      BBB      ")
                            .aisle(
                                    "               ",
                                    "      CCC      ",
                                    "               ")
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
                            Component
                                    .translatable("gtna.tooltip.hyper_pressure.desc",
                                            "Pressure-based fluid reaction chamber.")
                                    .withStyle(ChatFormatting.GRAY),
                            Component
                                    .translatable("gtna.tooltip.hyper_pressure.no_energy",
                                            "Requires NO Energy or Steam to operate.")
                                    .withStyle(ChatFormatting.GREEN),
                            Component.translatable("gtna.tooltip.hyper_pressure.parallel", "Max Parallel: 1")
                                    .withStyle(ChatFormatting.BLUE))
                    .register());

    public static final MultiblockMachineDefinition COMPACT_HYPER_PRESSURE_REACTOR = registerMachine(
            "compactHyperPressureReactor", () -> REGISTRATE
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
                                    "                                               ")
                            .aisle(
                                    "                                               ",
                                    "                    DBCCCBD                    ",
                                    "                   BB     BB                   ",
                                    "                   BB     BB                   ",
                                    "                   BB     BB                   ",
                                    "                    DBCCCBD                    ",
                                    "                                               ")
                            .aisle(
                                    "                    DBBBBBD                    ",
                                    "                   BB     BB                   ",
                                    "                BBBBB     BBBBB                ",
                                    "                BBBAAAAAAAAABBB                ",
                                    "                BBBBB     BBBBB                ",
                                    "                   BB     BB                   ",
                                    "                    DBBBBBD                    ")
                            .aisle(
                                    "                    DBCCCBD                    ",
                                    "                BBBBB     BBBBB                ",
                                    "              BBBBBAAAAAAAAABBBBB              ",
                                    "              BBAAAAAAAAAAAAAAABB              ",
                                    "              BBBBBAAAAAAAAABBBBB              ",
                                    "                BBBBB     BBBBB                ",
                                    "                    DBCCCBD                    ")
                            .aisle(
                                    "                    DBBBBBD                    ",
                                    "              BBBBBBB     BBBBBBB              ",
                                    "            BBBBAAABB     BBAAABBBB            ",
                                    "            BBAAAAAAAAAAAAAAAAAAABB            ",
                                    "            BBBBAAABB     BBAAABBBB            ",
                                    "              BBBBBBB     BBBBBBB              ",
                                    "                    DBBBBBD                    ")
                            .aisle(
                                    "                                               ",
                                    "            BBBBBBB DBCCCBD BBBBBBB            ",
                                    "           BBBAABBBBB     BBBBBAABBB           ",
                                    "           BAAAAAAABB     BBAAAAAAAB           ",
                                    "           BBBAABBBBB     BBBBBAABBB           ",
                                    "            BBBBBBB DBCCCBD BBBBBBB            ",
                                    "                                               ")
                            .aisle(
                                    "                                               ",
                                    "           BBBBB               BBBBB           ",
                                    "          BBAABBBBB DBBBBBD BBBBBAABB          ",
                                    "          BAAAAABBB DBE~EBD BBBAAAAAB          ",
                                    "          BBAABBBBB DBBBBBD BBBBBAABB          ",
                                    "           BBBBB               BBBBB           ",
                                    "                                               ")
                            .aisle(
                                    "                                               ",
                                    "          BBBB                   BBBB          ",
                                    "         BBABBBB               BBBBABB         ",
                                    "         BAAAABB               BBAAAAB         ",
                                    "         BBABBBB               BBBBABB         ",
                                    "          BBBB                   BBBB          ",
                                    "                                               ")
                            .aisle(
                                    "                                               ",
                                    "         BBB                       BBB         ",
                                    "        BBABBB                   BBBABB        ",
                                    "        BAAABB                   BBAAAB        ",
                                    "        BBABBB                   BBBABB        ",
                                    "         BBB                       BBB         ",
                                    "                                               ")
                            .aisle(
                                    "                                               ",
                                    "        BBB                         BBB        ",
                                    "       BBABB                       BBABB       ",
                                    "       BAAAB                       BAAAB       ",
                                    "       BBABB                       BBABB       ",
                                    "        BBB                         BBB        ",
                                    "                                               ")
                            .aisle(
                                    "                                               ",
                                    "       BBB                           BBB       ",
                                    "      BBABB                         BBABB      ",
                                    "      BAAAB                         BAAAB      ",
                                    "      BBABB                         BBABB      ",
                                    "       BBB                           BBB       ",
                                    "                                               ")
                            .aisle(
                                    "                                               ",
                                    "      BBB                             BBB      ",
                                    "     BBABB                           BBABB     ",
                                    "     BAAAB                           BAAAB     ",
                                    "     BBABB                           BBABB     ",
                                    "      BBB                             BBB      ",
                                    "                                               ")
                            .aisle(
                                    "                                               ",
                                    "     BBB                               BBB     ",
                                    "    BBABB                             BBABB    ",
                                    "    BAAAB                             BAAAB    ",
                                    "    BBABB                             BBABB    ",
                                    "     BBB                               BBB     ",
                                    "                                               ")
                            .aisle(
                                    "                                               ",
                                    "     BBB                               BBB     ",
                                    "    BBABB                             BBABB    ",
                                    "    BAAAB                             BAAAB    ",
                                    "    BBABB                             BBABB    ",
                                    "     BBB                               BBB     ",
                                    "                                               ")
                            .aisle(
                                    "                                               ",
                                    "    BBB                                 BBB    ",
                                    "   BBABB                               BBABB   ",
                                    "   BAAAB                               BAAAB   ",
                                    "   BBABB                               BBABB   ",
                                    "    BBB                                 BBB    ",
                                    "                                               ")
                            .aisle(
                                    "                                               ",
                                    "    BBB                                 BBB    ",
                                    "   BBABB                               BBABB   ",
                                    "   BAAAB                               BAAAB   ",
                                    "   BBABB                               BBABB   ",
                                    "    BBB                                 BBB    ",
                                    "                                               ")
                            .aisle(
                                    "                                               ",
                                    "   BBB                                   BBB   ",
                                    "  BBABB                                 BBABB  ",
                                    "  BAAAB                                 BAAAB  ",
                                    "  BBABB                                 BBABB  ",
                                    "   BBB                                   BBB   ",
                                    "                                               ")
                            .aisle(
                                    "                                               ",
                                    "   BBB                                   BBB   ",
                                    "  BBABB                                 BBABB  ",
                                    "  BAAAB                                 BAAAB  ",
                                    "  BBABB                                 BBABB  ",
                                    "   BBB                                   BBB   ",
                                    "                                               ")
                            .aisle(
                                    "                                               ",
                                    "   BBB                                   BBB   ",
                                    "  BBABB                                 BBABB  ",
                                    "  BAAAB                                 BAAAB  ",
                                    "  BBABB                                 BBABB  ",
                                    "   BBB                                   BBB   ",
                                    "                                               ")
                            .aisle(
                                    "                                               ",
                                    "  BBB                                     BBB  ",
                                    " BBABB                                   BBABB ",
                                    " BAAAB                                   BAAAB ",
                                    " BBABB                                   BBABB ",
                                    "  BBB                                     BBB  ",
                                    "                                               ")
                            .aisle(
                                    "  DDD                                     DDD  ",
                                    " DBBBD                                   DBBBD ",
                                    "DBBABBD                                 DBBABBD",
                                    "DBAAABD                                 DBAAABD",
                                    "DBBABBD                                 DBBABBD",
                                    " DBBBD                                   DBBBD ",
                                    "  DDD                                     DDD  ")
                            .aisle(
                                    "  BBB                                     BBB  ",
                                    " B   B                                   B   B ",
                                    "B  A  B                                 B  A  B",
                                    "B AAA B                                 B AAA B",
                                    "B  A  B                                 B  A  B",
                                    " B   B                                   B   B ",
                                    "  BBB                                     BBB  ")
                            .aisle(
                                    "  BCB                                     BCB  ",
                                    " C   C                                   C   C ",
                                    "B  A  B                                 B  A  B",
                                    "C AAA E                                 E AAA C",
                                    "B  A  B                                 B  A  B",
                                    " C   C                                   C   C ",
                                    "  BCB                                     BCB  ")
                            .aisle(
                                    "  BCB                                     BCB  ",
                                    " C   C                                   C   C ",
                                    "B  A  B                                 B  A  B",
                                    "C AAA C                                 C AAA C",
                                    "B  A  B                                 B  A  B",
                                    " C   C                                   C   C ",
                                    "  BCB                                     BCB  ")
                            .aisle(
                                    "  BCB                                     BCB  ",
                                    " C   C                                   C   C ",
                                    "B  A  B                                 B  A  B",
                                    "C AAA E                                 E AAA C",
                                    "B  A  B                                 B  A  B",
                                    " C   C                                   C   C ",
                                    "  BCB                                     BCB  ")
                            .aisle(
                                    "  BBB                                     BBB  ",
                                    " B   B                                   B   B ",
                                    "B  A  B                                 B  A  B",
                                    "B AAA B                                 B AAA B",
                                    "B  A  B                                 B  A  B",
                                    " B   B                                   B   B ",
                                    "  BBB                                     BBB  ")
                            .aisle(
                                    "  DDD                                     DDD  ",
                                    " DBBBD                                   DBBBD ",
                                    "DBBABBD                                 DBBABBD",
                                    "DBAAABD                                 DBAAABD",
                                    "DBBABBD                                 DBBABBD",
                                    " DBBBD                                   DBBBD ",
                                    "  DDD                                     DDD  ")
                            .aisle(
                                    "                                               ",
                                    "  BBB                                     BBB  ",
                                    " BBABB                                   BBABB ",
                                    " BAAAB                                   BAAAB ",
                                    " BBABB                                   BBABB ",
                                    "  BBB                                     BBB  ",
                                    "                                               ")
                            .aisle(
                                    "                                               ",
                                    "   BBB                                   BBB   ",
                                    "  BBABB                                 BBABB  ",
                                    "  BAAAB                                 BAAAB  ",
                                    "  BBABB                                 BBABB  ",
                                    "   BBB                                   BBB   ",
                                    "                                               ")
                            .aisle(
                                    "                                               ",
                                    "   BBB                                   BBB   ",
                                    "  BBABB                                 BBABB  ",
                                    "  BAAAB                                 BAAAB  ",
                                    "  BBABB                                 BBABB  ",
                                    "   BBB                                   BBB   ",
                                    "                                               ")
                            .aisle(
                                    "                                               ",
                                    "   BBB                                   BBB   ",
                                    "  BBABB                                 BBABB  ",
                                    "  BAAAB                                 BAAAB  ",
                                    "  BBABB                                 BBABB  ",
                                    "   BBB                                   BBB   ",
                                    "                                               ")
                            .aisle(
                                    "                                               ",
                                    "    BBB                                 BBB    ",
                                    "   BBABB                               BBABB   ",
                                    "   BAAAB                               BAAAB   ",
                                    "   BBABB                               BBABB   ",
                                    "    BBB                                 BBB    ",
                                    "                                               ")
                            .aisle(
                                    "                                               ",
                                    "    BBB                                 BBB    ",
                                    "   BBABB                               BBABB   ",
                                    "   BAAAB                               BAAAB   ",
                                    "   BBABB                               BBABB   ",
                                    "    BBB                                 BBB    ",
                                    "                                               ")
                            .aisle(
                                    "                                               ",
                                    "     BBB                               BBB     ",
                                    "    BBABB                             BBABB    ",
                                    "    BAAAB                             BAAAB    ",
                                    "    BBABB                             BBABB    ",
                                    "     BBB                               BBB     ",
                                    "                                               ")
                            .aisle(
                                    "                                               ",
                                    "     BBB                               BBB     ",
                                    "    BBABB                             BBABB    ",
                                    "    BAAAB                             BAAAB    ",
                                    "    BBABB                             BBABB    ",
                                    "     BBB                               BBB     ",
                                    "                                               ")
                            .aisle(
                                    "                                               ",
                                    "      BBB                             BBB      ",
                                    "     BBABB                           BBABB     ",
                                    "     BAAAB                           BAAAB     ",
                                    "     BBABB                           BBABB     ",
                                    "      BBB                             BBB      ",
                                    "                                               ")
                            .aisle(
                                    "                                               ",
                                    "       BBB                           BBB       ",
                                    "      BBABB                         BBABB      ",
                                    "      BAAAB                         BAAAB      ",
                                    "      BBABB                         BBABB      ",
                                    "       BBB                           BBB       ",
                                    "                                               ")
                            .aisle(
                                    "                                               ",
                                    "        BBB                         BBB        ",
                                    "       BBABB                       BBABB       ",
                                    "       BAAAB                       BAAAB       ",
                                    "       BBABB                       BBABB       ",
                                    "        BBB                         BBB        ",
                                    "                                               ")
                            .aisle(
                                    "                                               ",
                                    "         BBB                       BBB         ",
                                    "        BBABBB                   BBBABB        ",
                                    "        BAAABB                   BBAAAB        ",
                                    "        BBABBB                   BBBABB        ",
                                    "         BBB                       BBB         ",
                                    "                                               ")
                            .aisle(
                                    "                                               ",
                                    "          BBBB                   BBBB          ",
                                    "         BBABBBB               BBBBABB         ",
                                    "         BAAAABB               BBAAAAB         ",
                                    "         BBABBBB               BBBBABB         ",
                                    "          BBBB                   BBBB          ",
                                    "                                               ")
                            .aisle(
                                    "                                               ",
                                    "           BBBBB               BBBBB           ",
                                    "          BBAABBBBB DBBBBBD BBBBBAABB          ",
                                    "          BAAAAABBB DBECEBD BBBAAAAAB          ",
                                    "          BBAABBBBB DBBBBBD BBBBBAABB          ",
                                    "           BBBBB               BBBBB           ",
                                    "                                               ")
                            .aisle(
                                    "                                               ",
                                    "            BBBBBBB DBCCCBD BBBBBBB            ",
                                    "           BBBAABBBBB     BBBBBAABBB           ",
                                    "           BAAAAAAABB     BBAAAAAAAB           ",
                                    "           BBBAABBBBB     BBBBBAABBB           ",
                                    "            BBBBBBB DBCCCBD BBBBBBB            ",
                                    "                                               ")
                            .aisle(
                                    "                    DBBBBBD                    ",
                                    "              BBBBBBB     BBBBBBB              ",
                                    "            BBBBAAABB     BBAAABBBB            ",
                                    "            BBAAAAAAAAAAAAAAAAAAABB            ",
                                    "            BBBBAAABB     BBAAABBBB            ",
                                    "              BBBBBBB     BBBBBBB              ",
                                    "                    DBBBBBD                    ")
                            .aisle(
                                    "                    DBCCCBD                    ",
                                    "                BBBBB     BBBBB                ",
                                    "              BBBBBAAAAAAAAABBBBB              ",
                                    "              BBAAAAAAAAAAAAAAABB              ",
                                    "              BBBBBAAAAAAAAABBBBB              ",
                                    "                BBBBB     BBBBB                ",
                                    "                    DBCCCBD                    ")
                            .aisle(
                                    "                    DBBBBBD                    ",
                                    "                   BB     BB                   ",
                                    "                BBBBB     BBBBB                ",
                                    "                BBBAAAAAAAAABBB                ",
                                    "                BBBBB     BBBBB                ",
                                    "                   BB     BB                   ",
                                    "                    DBBBBBD                    ")
                            .aisle(
                                    "                                               ",
                                    "                    DBCCCBD                    ",
                                    "                   BB     BB                   ",
                                    "                   BB     BB                   ",
                                    "                   BB     BB                   ",
                                    "                    DBCCCBD                    ",
                                    "                                               ")
                            .aisle(
                                    "                                               ",
                                    "                                               ",
                                    "                    DBBBBBD                    ",
                                    "                    DBCCCBD                    ",
                                    "                    DBBBBBD                    ",
                                    "                                               ",
                                    "                                               ")
                            .where('~', controller(blocks(definition.get())))
                            .where('A', blocks(GTNABlocks.STEAM_COMPACT_PIPE_CASING.get()))
                            .where('B', blocks(GTNABlocks.VIBRATION_SAFE_CASING.get())
                                    .or(abilities(PartAbility.IMPORT_FLUIDS).setPreviewCount(1))
                                    .or(abilities(PartAbility.EXPORT_FLUIDS).setPreviewCount(1)))
                            .where('C', blocks(GTNABlocks.BOROSILICATE_GLASS_BLOCK.get()))
                            .where('D', blocks(ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.Steel)))
                            .where('E', blocks(GTNABlocks.VIBRATION_SAFE_CASING.get())
                                    .or(abilities(PartAbility.IMPORT_FLUIDS).setPreviewCount(1))
                                    .or(abilities(PartAbility.EXPORT_FLUIDS).setPreviewCount(1)))
                            .build())
                    .workableCasingModel(
                            GTNACORE.id("block/casings/vibration_safe_casing"),
                            GTCEu.id("block/multiblock/steam_grinder"))
                    .tooltips(
                            Component
                                    .translatable("gtna.tooltip.compact_hyper_pressure.desc",
                                            "Extreme density fluid processor.")
                                    .withStyle(ChatFormatting.DARK_PURPLE),
                            Component
                                    .translatable("gtna.tooltip.hyper_pressure.no_energy",
                                            "Requires NO Energy or Steam to operate.")
                                    .withStyle(ChatFormatting.GREEN),
                            Component
                                    .translatable("gtna.tooltip.compact_hyper_pressure.special",
                                            "Can process Dense Supercritical Steam from basic resources.")
                                    .withStyle(ChatFormatting.GOLD),
                            Component.translatable("gtna.tooltip.compact_hyper_pressure.parallel", "Max Parallel: 512")
                                    .withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                    .register());

    public static final MultiblockMachineDefinition VOID_MINER_STEAM_GATE_AGED = registerMachine(
            "voidMinerSteamGateAged", () -> REGISTRATE
                    .multiblock("void_miner_steam_gate_aged", VoidMinerSteamGateAged::new)
                    .rotationState(RotationState.NON_Y_AXIS)
                    .recipeType(GTRecipeTypes.DUMMY_RECIPES)
                    .appearanceBlock(GTBlocks.CASING_BRONZE_BRICKS)
                    .recipeModifier(VoidMinerSteamGateAged::recipeModifier)
                    .pattern(definition -> FactoryBlockPattern.start()
                            .aisle("BBBBBBBBB", "BBBBBBBBB", "B       B", "B       B", "B       B", "BBBBBBBBB",
                                    "BCCCCCCCB",
                                    "BCCCCCCCB", "B       B", "B       B")
                            .aisle("B       B", "B       B", "         ", "         ", "         ", "B   D   B",
                                    "C  DDD  C",
                                    "C  DDD  C", "   DDD   ", "         ")
                            .aisle("B       B", "B       B", "         ", "    D    ", "   DDD   ", "B  DDD  B",
                                    "C DD DD C",
                                    "C D   D C", "  D   D  ", "         ")
                            .aisle("B   D   B", "B   D   B", "   DDD   ", "   D D   ", "  DD DD  ", "B D   D B",
                                    "C D   D C",
                                    "C     D C", " D     D ", "         ")
                            .aisle("B   D   B", "B   D   B", "   D D   ", "  D   D  ", "  D   D  ", "B D   D B",
                                    "C     D C",
                                    "C     D C", " D     D ", "         ")
                            .aisle("B   D   B", "B   D   B", "   DDD   ", "   D D   ", "  DD DD  ", "B D   D B",
                                    "C D   D C",
                                    "C     D C", " D     D ", "         ")
                            .aisle("B       B", "B       B", "         ", "    D    ", "   DDD   ", "B  DDD  B",
                                    "C DD DD C",
                                    "C D   D C", "  D   D  ", "         ")
                            .aisle("B       B", "B       B", "         ", "         ", "         ", "B   D   B",
                                    "C  DDD  C",
                                    "C  DDD  C", "   DDD   ", "         ")
                            .aisle("BBBBBBBBB", "BBBBEBBBB", "B       B", "B       B", "B       B", "BBBBBBBBB",
                                    "BCCCCCCCB",
                                    "BCCCCCCCB", "B       B", "B       B")
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
                                    .withStyle(ChatFormatting.BLUE))
                    .register());

    // ... imports

    public static final MultiblockMachineDefinition INDUSTRIAL_SLAUGHTERHOUSE = registerMachine(
            "industrialSlaughterhouse", () -> REGISTRATE
                    .multiblock("industrial_slaughterhouse", IndustrialSlaughterhouse::new)
                    .rotationState(RotationState.NON_Y_AXIS)
                    .recipeType(GTNARecipeType.SLAUGHTERHOUSE_RECIPES) // *Importante: Crie este RecipeType em
                                                                       // GTNARecipeType*
                    .recipeModifier(IndustrialSlaughterhouse::recipeModifier)
                    .appearanceBlock(GTBlocks.CASING_STEEL_SOLID)
                    .pattern(definition -> FactoryBlockPattern.start()
                            .aisle("AAAAAAA", "AAAAAAA", "ABBBBBA", "ABBBBBA", "ABBBBBA", "ABBBBBA", "ABBBBBA",
                                    "ABBBBBA",
                                    "ABBBBBA", "AAAAAAA")
                            .aisle("AAAAAAA", "ACCCCCA", "BDDDDDB", "BDDDDDB", "BDDDDDB", "BDDDDDB", "BDDDDDB",
                                    "BDDDDDB",
                                    "BEEEEEB", "AAAAAAA")
                            .aisle("AAAAAAA", "ACCCCCA", "BD   DB", "BD   DB", "BD   DB", "BD   DB", "BD   DB",
                                    "BD   DB",
                                    "BEEEEEB", "AAAAAAA")
                            .aisle("AAAAAAA", "ACCCCCA", "BD   DB", "BD   DB", "BD   DB", "BD   DB", "BD   DB",
                                    "BD   DB",
                                    "BEEEEEB", "AAAAAAA")
                            .aisle("AAAAAAA", "ACCCCCA", "BD   DB", "BD   DB", "BD   DB", "BD   DB", "BD   DB",
                                    "BD   DB",
                                    "BEEEEEB", "AAAAAAA")
                            .aisle("AAAAAAA", "ACCCCCA", "BDDDDDB", "BDDDDDB", "BDDDDDB", "BDDDDDB", "BDDDDDB",
                                    "BDDDDDB",
                                    "BEEEEEB", "AAAAAAA")
                            .aisle("AAAAAAA", "AAA~AAA", "ABBBBBA", "ABBBBBA", "ABBBBBA", "ABBBBBA", "ABBBBBA",
                                    "ABBBBBA",
                                    "ABBBBBA", "AAAAAAA")
                            .where("~", Predicates.controller(Predicates.blocks(definition.get())))
                            .where("A", Predicates.blocks(GTBlocks.CASING_STEEL_SOLID.get())
                                    .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setMaxGlobalLimited(2))
                                    .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setMaxGlobalLimited(4))
                                    .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(1))
                                    .or(Predicates.abilities(PARALLEL_HATCH).setMaxGlobalLimited(1))
                                    .or(Predicates.abilities(GTNAPartAbility.OVERCLOCK_HATCH)
                                            .setMaxGlobalLimited(1))
                                    .or(Predicates.abilities(GTNAPartAbility.ACCELERATE_HATCH)
                                            .setMaxGlobalLimited(1))
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
                            Component.translatable("gtna.machine.slaughterhouse.mechanics")
                                    .withStyle(ChatFormatting.GOLD),
                            Component.translatable("gtna.machine.slaughterhouse.circuit1")
                                    .withStyle(ChatFormatting.GREEN),
                            Component.translatable("gtna.machine.slaughterhouse.circuit2")
                                    .withStyle(ChatFormatting.RED),
                            Component.translatable("gtna.machine.slaughterhouse.circuit3")
                                    .withStyle(ChatFormatting.DARK_PURPLE),
                            Component.translatable("gtna.machine.slaughterhouse.circuit4").withStyle(
                                    ChatFormatting.DARK_RED,
                                    ChatFormatting.BOLD))
                    .register());

    public static final MultiblockMachineDefinition ARTIFICIAL_STAR = registerMachine("artificialStar", () -> REGISTRATE
            .multiblock("annihilate_generator", ArtificialStarMachine::new)
            .langValue("Artificial Star")
            .rotationState(RotationState.ALL)
            .recipeType(GTNARecipeType.ARTIFICIAL_STAR_RECIPES)
            .tooltips(
                    Component.translatable("gtceu.machine.perfect_oc"),
                    Component.translatable("gtna.machine.artificial_star.output"),
                    Component.translatable("gtceu.machine.available_recipe_map_1.tooltip",
                            Component.translatable("gtceu.annihilate_generator")),
                    Component.literal("Artificial Star"))
            .tooltipBuilder(GTNA_ADD)
            .generator(true)
            .recipeModifier(ArtificialStarMachine::recipeModifier)
            .appearanceBlock(GTBlocks.HIGH_POWER_CASING)
            .pattern(GTNAMachines::createArtificialStarPattern)
            .model(createWorkableCasingMachineModel(
                    GTCEu.id("block/casings/hpca/high_power_casing"),
                    GTCEu.id("block/multiblock/fusion_reactor"))
                    .andThen(builder -> builder.addDynamicRenderer(AnnihilateGeneratorRenderer::new)))
            .register());

    public static final MultiblockMachineDefinition EYE_OF_HARMONY = registerMachine("eyeOfHarmony", () -> REGISTRATE
            .multiblock("eye_of_harmony", EyeOfHarmonyMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .allowExtendedFacing(false)
            .recipeType(GTNARecipeType.COSMOS_SIMULATION_RECIPES)
            .tooltips(
                    Component.translatable("gtna.machine.eye_of_harmony.tooltip.0"),
                    Component.translatable("gtna.machine.eye_of_harmony.tooltip.1"),
                    Component.translatable("gtna.machine.eye_of_harmony.tooltip.2"),
                    Component.translatable("gtna.machine.eye_of_harmony.tooltip.3"),
                    Component.translatable("gtna.machine.eye_of_harmony.tooltip.4"),
                    Component.translatable("gtna.machine.eye_of_harmony.tooltip.5"),
                    Component.translatable("gtna.machine.eye_of_harmony.tooltip.6"),
                    Component.translatable("gtna.machine.eye_of_harmony.tooltip.7"),
                    Component.translatable("gtceu.machine.available_recipe_map_1.tooltip",
                            Component.translatable("gtna.cosmos_simulation")))
            .tooltipBuilder(GTNA_ADD)
            .recipeModifier(EyeOfHarmonyMachine::recipeModifier)
            .appearanceBlock(GTBlocks.HIGH_POWER_CASING)
            .pattern(GTNAMachines::createEyeOfHarmonyPattern)
            .model(createWorkableCasingMachineModel(
                    GTNACORE.id("block/casings/dimensionally_transcendent_casing"),
                    GTCEu.id("block/multiblock/fluid_drilling_rig"))
                    .andThen(builder -> builder.addDynamicRenderer(EyeOfHarmonyRenderer::new)))
            .register());

    public static final MultiblockMachineDefinition EYE_OF_WOOD = registerMachine("eyeOfWood", () -> REGISTRATE
            .multiblock("eye_of_wood", EyeOfWoodMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .allowExtendedFacing(false)
            .recipeType(GTRecipeTypes.DUMMY_RECIPES)
            .appearanceBlock(GTBlocks.CASING_BRONZE_BRICKS)
            .pattern(GTNAMachines::createEyeOfWoodPattern)
            .modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE)
            .model(createWorkableCasingMachineModel(
                    GTCEu.id("block/casings/solid/machine_casing_bronze_plated_bricks"),
                    GTCEu.id("block/multiblock/fluid_drilling_rig"))
                    .andThen(builder -> builder.addDynamicRenderer(EyeOfWoodRenderer::new)))
            .tooltips(
                    Component.translatable("gtna.machine.eye_of_wood.tooltip.0").withStyle(ChatFormatting.GRAY),
                    Component.translatable("gtna.machine.eye_of_wood.tooltip.1").withStyle(ChatFormatting.GOLD),
                    Component.translatable("gtna.machine.eye_of_wood.tooltip.2").withStyle(ChatFormatting.GREEN),
                    Component.translatable("gtna.machine.eye_of_wood.tooltip.3").withStyle(ChatFormatting.AQUA),
                    Component.translatable("gtna.machine.eye_of_wood.tooltip.4").withStyle(ChatFormatting.AQUA),
                    Component.translatable("gtna.machine.eye_of_wood.tooltip.5").withStyle(ChatFormatting.BLUE),
                    Component.translatable("gtna.machine.eye_of_wood.tooltip.6").withStyle(ChatFormatting.RED),
                    Component.translatable("gtna.machine.eye_of_wood.tooltip.7").withStyle(ChatFormatting.DARK_GRAY))
            .tooltipBuilder(GTNA_ADD)
            .register());

    public static final MultiblockMachineDefinition NEXUS_MOLECULAR_FORGE = registerMachine("nexusMolecularForge",
            () -> REGISTRATE
                    .multiblock("nexus_molecular_forge", NexusMolecularForgeMachine::new)
                    .langValue("Nexus Assembly Forge")
                    .rotationState(RotationState.NON_Y_AXIS)
                    .allowExtendedFacing(false)
                    .recipeType(GTRecipeTypes.DUMMY_RECIPES)
                    .appearanceBlock(GTNABlocks.OXIDATION_RESISTANT_HASTELLOY_N_MECHANICAL_CASING)
                    .pattern(GTNAMachines::createNexusMolecularForgePattern)
                    .workableCasingModel(
                            GTNACORE.id("block/casings/oxidation_resistant_hastelloy_n_mechanical_casing"),
                            GTCEu.id("block/multiblock/fusion_reactor"))
                    .tooltips(
                            Component.translatable("gtna.machine.nexus_molecular_forge.tooltip.0")
                                    .withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD),
                            Component.translatable("gtna.machine.nexus_molecular_forge.tooltip.1")
                                    .withStyle(ChatFormatting.AQUA),
                            Component.translatable("gtna.machine.nexus_molecular_forge.tooltip.2")
                                    .withStyle(ChatFormatting.LIGHT_PURPLE),
                            Component.translatable("gtna.machine.nexus_molecular_forge.tooltip.3")
                                    .withStyle(ChatFormatting.RED),
                            Component.translatable("gtna.machine.nexus_molecular_forge.tooltip.4")
                                    .withStyle(ChatFormatting.GOLD),
                            Component.translatable("gtna.machine.nexus_molecular_forge.tooltip.5")
                                    .withStyle(ChatFormatting.GRAY))
                    .tooltipBuilder(GTNA_ADD)
                    .register());

    public static final MultiblockMachineDefinition NEXUS_ME_HYPERCORE = registerMachine("nexusMeHypercore",
            () -> REGISTRATE
                    .multiblock("nexus_me_hypercore", NexusMEHyperCoreMachine::new)
                    .langValue("Nexus ME Hypercore")
                    .rotationState(RotationState.NON_Y_AXIS)
                    .allowExtendedFacing(false)
                    .recipeType(GTRecipeTypes.DUMMY_RECIPES)
                    .appearanceBlock(GCYMBlocks.CASING_NONCONDUCTING)
                    .pattern(GTNAMachines::createNexusMEHyperCorePattern)
                    .workableCasingModel(
                            GTCEu.id("block/casings/gcym/nonconducting_casing"),
                            GTCEu.id("block/multiblock/assembly_line"))
                    .tooltips(
                            Component.literal("Original ME Super Computer Core structure, renamed as the Nexus ME Hypercore.")
                                    .withStyle(ChatFormatting.AQUA),
                            Component.literal(
                                    "Uses the GTOCore ME CPU frame with Matrix Crafting Modules inside.")
                                    .withStyle(ChatFormatting.GRAY),
                            Component.literal(
                                    "Accepts the AE2 Crafting Unit fallback where the original structure allows it.")
                                    .withStyle(ChatFormatting.GRAY))
                    .tooltipBuilder(GTNA_ADD)
                    .register());

    public static final MultiblockMachineDefinition ME_STORAGE = registerMachine("meStorage",
            () -> REGISTRATE
                    .multiblock("me_storage", MEStorageMachine::new)
                    .langValue("ME Storage")
                    .rotationState(RotationState.NON_Y_AXIS)
                    .allowExtendedFacing(false)
                    .recipeType(GTRecipeTypes.DUMMY_RECIPES)
                    .appearanceBlock(GTBlocks.COMPUTER_CASING)
                    .pattern(GTNAMachines::createMEStoragePattern)
                    .workableCasingModel(
                            GTCEu.id("block/casings/hpca/computer_casing/back"),
                            GTCEu.id("block/multiblock/fusion_reactor"))
                    .tooltips(
                            Component.literal("Expandable GTO-style ME storage multiblock.")
                                    .withStyle(ChatFormatting.AQUA),
                            Component.literal("Repeat the core slice to install up to 128 storage sections.")
                                    .withStyle(ChatFormatting.GRAY),
                            Component.literal("Requires exactly one ME Storage Access, Big Storage Access, or IO Port Hatch.")
                                    .withStyle(ChatFormatting.GRAY))
                    .tooltipBuilder(GTNA_ADD)
                    .register());

    private static BlockPattern createArtificialStarPattern(MultiblockMachineDefinition definition) {
        var pattern = FactoryBlockPattern.start();
        for (int index = 1; index <= 109; index++) {
            pattern.aisle(getArtificialStarAisle(index));
        }
        return pattern.where('~', controller(blocks(definition.get())))
                .where('A', blocks(GTNABlocks.GRAVITON_FIELD_CONSTRAINT_CASING.get()))
                .where('B', blocks(GTNABlocks.ANNIHILATE_CORE.get()))
                .where('C', blocks(GTNABlocks.HYPER_MECHANICAL_CASING.get()))
                .where('D', blocks(GTNABlocks.HOLLOW_CASING.get()))
                .where('E', blocks(GTNABlocks.NAQUADAH_ALLOY_CASING.get()))
                .where('F', blocks(GTBlocks.FUSION_GLASS.get()))
                .where('G', blocks(GTNABlocks.DYSON_CONTROL_TOROID.get()))
                .where('H', blocks(GTNABlocks.RHENIUM_REINFORCED_ENERGY_GLASS.get()))
                .where('P', blocks(GTNABlocks.DYSON_CONTROL_CASING.get()))
                .where('S', blocks(GTBlocks.HIGH_POWER_CASING.get())
                        .or(abilities(OUTPUT_ENERGY).setMaxGlobalLimited(1))
                        .or(abilities(OUTPUT_LASER))
                        .or(abilities(IMPORT_ITEMS))
                        .or(abilities(EXPORT_ITEMS)))
                .where('T', blocks(GTNABlocks.DEGENERATE_RHENIUM_CONSTRAINED_CASING.get()))
                .where('R', blocks(GTNABlocks.DYSON_RECEIVER_CASING.get()))
                .where(' ', any())
                .build();
    }

    private static BlockPattern createEyeOfHarmonyPattern(MultiblockMachineDefinition definition) {
        var pattern = FactoryBlockPattern.start();
        for (String[] aisle : EyeOfHarmonyAisles.AISLES) {
            pattern.aisle(aisle);
        }
        return pattern.where('~', controller(blocks(definition.get())))
                .where('A', blocks(GTNABlocks.DIMENSIONALLY_TRANSCENDENT_CASING.get()))
                .where('B', blocks(GTBlocks.HIGH_POWER_CASING.get())
                        .or(abilities(EXPORT_ITEMS).setPreviewCount(1))
                        .or(abilities(IMPORT_ITEMS).setPreviewCount(1))
                        .or(abilities(EXPORT_FLUIDS).setPreviewCount(1))
                        .or(abilities(IMPORT_FLUIDS).setPreviewCount(1)))
                .where('D', blocks(GTNABlocks.DIMENSION_INJECTION_CASING.get()))
                .where('E', blocks(GTNABlocks.DIMENSIONAL_BRIDGE_CASING.get()))
                .where('F', blocks(GTNABlocks.SPACETIME_COMPRESSION_FIELD_GENERATOR.get()))
                .where('G', blocks(GTNABlocks.DIMENSIONAL_STABILITY_CASING.get()))
                .where(' ', any())
                .build();
    }

    private static BlockPattern createEyeOfWoodPattern(MultiblockMachineDefinition definition) {
        var pattern = FactoryBlockPattern.start(LEFT, UP, BACK);
        for (String[] aisle : EyeOfWoodAisles.aisles()) {
            pattern.aisle(aisle);
        }
        return pattern.where('~', controller(blocks(definition.get())))
                .where('A', blocks(GTBlocks.CASING_BRONZE_BRICKS.get())
                        .or(abilities(IMPORT_ITEMS).setMaxGlobalLimited(2))
                        .or(abilities(EXPORT_ITEMS).setMaxGlobalLimited(2))
                        .or(abilities(IMPORT_FLUIDS).setMaxGlobalLimited(2))
                        .or(abilities(EXPORT_FLUIDS).setMaxGlobalLimited(1)))
                .where('B', blocks(Blocks.LAPIS_BLOCK))
                .where('C', blocks(Blocks.BOOKSHELF))
                .where('D', blocks(Blocks.BRICKS))
                .where('E', blocks(Blocks.CRACKED_STONE_BRICKS))
                .where('F', blocks(
                        Blocks.OAK_PLANKS, Blocks.SPRUCE_PLANKS, Blocks.BIRCH_PLANKS, Blocks.JUNGLE_PLANKS,
                        Blocks.ACACIA_PLANKS, Blocks.DARK_OAK_PLANKS, Blocks.MANGROVE_PLANKS, Blocks.CHERRY_PLANKS,
                        Blocks.BAMBOO_PLANKS, Blocks.WARPED_PLANKS, Blocks.CRIMSON_PLANKS))
                .where(' ', air())
                .build();
    }

    private static BlockPattern createNexusMolecularForgePattern(MultiblockMachineDefinition definition) {
        var dPredicate = blocks(GTNABlocks.ZIRCONIA_CERAMIC_HIGH_STRENGTH_BENDING_RESISTANCE_MECHANICAL_BLOCK.get())
                .setMinGlobalLimited(20)
                .or(abilities(INPUT_ENERGY).setMaxGlobalLimited(2))
                .or(abilities(EXPORT_ITEMS));
        if (GTNAMachines2.ME_CRAFT_PATTERN_HATCH != null) {
            dPredicate = dPredicate.or(blocks(GTNAMachines2.ME_CRAFT_PATTERN_HATCH.getBlock()));
        }

        return FactoryBlockPattern.start()
                .aisle("AAAAAAAAA", "AAAABAAAA", "AAAABAAAA", "AAAABAAAA", "AAAACAAAA", "AACCCCCAA", "AAAACAAAA",
                        "AAAABAAAA", "AAAABAAAA", "AAAABAAAA", "AAAAAAAAA")
                .aisle("AAAABAAAA", "AAADCDAAA", "AADDCDDAA", "AAEDCDEAA", "AAEDFDEAA", "ACEDFDECA", "AAEDFDEAA",
                        "AAEDCDEAA", "AADDCDDAA", "AAADCDAAA", "AAAABAAAA")
                .aisle("AAAABAAAA", "AADGHGDAA", "ADIIIIIDA", "AEAAFAAEA", "AEAAAAAEA", "CEAAAAAEC", "AEAAAAAEA",
                        "AEAAFAAEA", "ADIIIIIDA", "AADGHGDAA", "AAAABAAAA")
                .aisle("AAAABAAAA", "ADGGHGGDA", "ADIJJJIDA", "ADAAKAADA", "ADAAAAADA", "CDAAAAADC", "ADAAAAADA",
                        "ADAAKAADA", "ADIJJJIDA", "ADGGHGGDA", "AAAABAAAA")
                .aisle("ABBBBBBBA", "BCHHHHHCB", "BCIJJJICB", "BCFKLKFCB", "CFAALAAFC", "CFAALAAFC", "CFAALAAFC",
                        "BCFKLKFCB", "BCIJJJICB", "BCHHHHHCB", "ABBBBBBBA")
                .aisle("AAAABAAAA", "ADGGHGGDA", "ADIJJJIDA", "ADAAKAADA", "ADAAAAADA", "CDAAAAADC", "ADAAAAADA",
                        "ADAAKAADA", "ADIJJJIDA", "ADGGHGGDA", "AAAABAAAA")
                .aisle("AAAABAAAA", "AADGHGDAA", "ADIIIIIDA", "AEAAFAAEA", "AEAAAAAEA", "CEAAAAAEC", "AEAAAAAEA",
                        "AEAAFAAEA", "ADIIIIIDA", "AADGHGDAA", "AAAABAAAA")
                .aisle("AAAABAAAA", "AAADCDAAA", "AADDCDDAA", "AAEDCDEAA", "AAEDFDEAA", "ACEDFDECA", "AAEDFDEAA",
                        "AAEDCDEAA", "AADDCDDAA", "AAADCDAAA", "AAAABAAAA")
                .aisle("AAAAAAAAA", "AAAABAAAA", "AAAABAAAA", "AAAABAAAA", "AAAACAAAA", "AACCMCCAA", "AAAACAAAA",
                        "AAAABAAAA", "AAAABAAAA", "AAAABAAAA", "AAAAAAAAA")
                .where('A', Predicates.air())
                .where('B', blocks(ChemicalHelper.getBlock(TagPrefix.frameGt, GTNAMaterials.HastelloyN)))
                .where('C', blocks(GTNABlocks.OXIDATION_RESISTANT_HASTELLOY_N_MECHANICAL_CASING.get()))
                .where('D', dPredicate)
                .where('E', blocks(GTNABlocks.NAQUADAH_BOROSILICATE_GLASS.get()))
                .where('F', blocks(GTNABlocks.MAGTECH_CASING.get()))
                .where('G', blocks(GTNABlocks.PROCESS_MACHINE_CASING.get()))
                .where('H', blocks(GTBlocks.CASING_ASSEMBLY_LINE.get()))
                .where('I', blocks(GTBlocks.HIGH_POWER_CASING.get()))
                .where('J', blocks(GTNABlocks.COMPRESSOR_CONTROLLER_CASING.get()))
                .where('K', blocks(ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.Europium)))
                .where('L', blocks(GTNABlocks.EXTREME_DENSITY_CASING.get()))
                .where('M', controller(blocks(definition.get())))
                .build();
    }

    private static BlockPattern createNexusMEHyperCorePattern(MultiblockMachineDefinition definition) {
        var bPredicate = blocks(GCYMBlocks.CASING_NONCONDUCTING.get())
                .or(abilities(PARALLEL_HATCH).setMaxGlobalLimited(1))
                .or(blocks(GTNAMachines2.CRAFTING_CPU_INTERFACE.getBlock()).setExactLimit(1));

        return GTNAMultiBlockFileReader.start(definition, "me_cpu")
                .where('A', blocks(GTNABlocks.HIGH_STRENGTH_CONCRETE.get()))
                .where('B', bPredicate)
                .where('C', blocks(GCYMBlocks.MOLYBDENUM_DISILICIDE_COIL_BLOCK.get()))
                .where('D', blocks(GCYMBlocks.CASING_NONCONDUCTING.get()))
                .where('E', blocks(ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.BlackSteel)))
                .where('F',
                        blocks(GTNABlocks.COBALT_OXIDE_CERAMIC_STRONG_THERMALLY_CONDUCTIVE_MECHANICAL_BLOCK.get()))
                .where('G', blocks(GCYMBlocks.ELECTROLYTIC_CELL.get()))
                .where('H', blocks(GTBlocks.CASING_PALLADIUM_SUBSTATION.get()))
                .where('I', blocks(GCYMBlocks.CASING_LASER_SAFE_ENGRAVING.get()))
                .where('J', blocks(GTNABlocks.OXIDATION_RESISTANT_HASTELLOY_N_MECHANICAL_CASING.get()))
                .where('K', blocks(ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.StainlessSteel)))
                .where('L', blocks(GTBlocks.CASING_EXTREME_ENGINE_INTAKE.get()))
                .where('M', blocks(GTBlocks.HIGH_POWER_CASING.get()))
                .where('N', blockTag(Tags.Blocks.GLASS))
                .where('O', craftingStorageCorePredicate()
                        .or(blocks(Registries.getBlock("ae2:crafting_unit")).setMaxGlobalLimited(480)))
                .where('P', blocks(GTBlocks.FILTER_CASING.get()))
                .where('Q', controller(blocks(definition.get())))
                .where(' ', any())
                .build();
    }

    private static TraceabilityPredicate craftingStorageCorePredicate() {
        return blocks(GTNABlocks.T1_CRAFTING_STORAGE_CORE.get())
                .or(blocks(GTNABlocks.T2_CRAFTING_STORAGE_CORE.get()))
                .or(blocks(GTNABlocks.T3_CRAFTING_STORAGE_CORE.get()))
                .or(blocks(GTNABlocks.T4_CRAFTING_STORAGE_CORE.get()))
                .or(blocks(GTNABlocks.T5_CRAFTING_STORAGE_CORE.get()));
    }

    private static BlockPattern createMEStoragePattern(MultiblockMachineDefinition definition) {
        var dPredicate = blocks(GTBlocks.COMPUTER_CASING.get());
        var accessPredicate = abilities(GTNAPartAbility.ME_STORAGE_ACCESS).setExactLimit(1);

        var corePredicate = blocks(GTNABlocks.T1_ME_STORAGE_CORE.get())
                .or(blocks(GTNABlocks.T2_ME_STORAGE_CORE.get()))
                .or(blocks(GTNABlocks.T3_ME_STORAGE_CORE.get()))
                .or(blocks(GTNABlocks.T4_ME_STORAGE_CORE.get()))
                .or(blocks(GTNABlocks.T5_ME_STORAGE_CORE.get()));

        return FactoryBlockPattern.start(BACK, UP, RIGHT)
                .aisle("AAA", "DDD", "DDD", "DDD", "AAA")
                .aisle("AAA", "DBD", "EBD", "DBD", "AAA")
                .aisle("BBB", "BGB", "BGB", "BGB", "BBB")
                .aisle("CBC", "cHc", "cHc", "cHc", "CBC").setRepeatable(1, 128)
                .aisle("BBB", "BBB", "BBB", "BBB", "BBB")
                .where('A', blocks(GTBlocks.COMPUTER_HEAT_VENT.get()))
                .where('B', blocks(GTBlocks.COMPUTER_CASING.get()))
                .where('C', absCasingPredicate())
                .where('D', dPredicate.or(accessPredicate))
                .where('E', controller(blocks(definition.get())))
                .where('G', blocks(GTBlocks.HIGH_POWER_CASING.get()))
                .where('H', blocks(GTNABlocks.LITHIUM_OXIDE_CERAMIC_HEAT_RESISTANT_SHOCK_RESISTANT_MECHANICAL_CUBE.get()))
                .where('c', corePredicate)
                .build();
    }

    private static TraceabilityPredicate absCasingPredicate() {
        return blocks(GTNABlocks.ABS_BLACK_CASING.get())
                .or(blocks(GTNABlocks.ABS_BLUE_CASING.get()))
                .or(blocks(GTNABlocks.ABS_BROWN_CASING.get()))
                .or(blocks(GTNABlocks.ABS_GREEN_CASING.get()))
                .or(blocks(GTNABlocks.ABS_GREY_CASING.get()))
                .or(blocks(GTNABlocks.ABS_LIME_CASING.get()))
                .or(blocks(GTNABlocks.ABS_ORANGE_CASING.get()))
                .or(blocks(GTNABlocks.ABS_RED_CASING.get()))
                .or(blocks(GTNABlocks.ABS_WHITE_CASING.get()))
                .or(blocks(GTNABlocks.ABS_YELLOW_CASING.get()))
                .or(blocks(GTNABlocks.ABS_CYAN_CASING.get()))
                .or(blocks(GTNABlocks.ABS_MAGENTA_CASING.get()))
                .or(blocks(GTNABlocks.ABS_PINK_CASING.get()))
                .or(blocks(GTNABlocks.ABS_PURPLE_CASING.get()))
                .or(blocks(GTNABlocks.ABS_LIGHT_BULL_CASING.get()))
                .or(blocks(GTNABlocks.ABS_LIGHT_GREY_CASING.get()));
    }

    private static String[] getArtificialStarAisle(int index) {
        try {
            Class<?> holder = index <= 53 ? AnnihilateGeneratorB.class : AnnihilateGeneratorA.class;
            Field field = holder.getField("A_" + index);
            return (String[]) field.get(null);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to load Artificial Star aisle " + index, exception);
        }
    }

    private static <T extends MachineDefinition> T registerHatch(String hatchId, Supplier<T> supplier) {
        return ConfigHolder.isHatchEnabled(hatchId) ? supplier.get() : null;
    }

    private static <T extends MachineDefinition> T registerMachine(String machineId, Supplier<T> supplier) {
        return ConfigHolder.isMachineEnabled(machineId) ? supplier.get() : null;
    }

    public static void init() {}
}
