package com.raishxn.gtna.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.MultiblockShapeInfo;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.common.machine.multiblock.part.steam.HugeSteamInputBus;
import com.raishxn.gtna.common.machine.multiblock.part.steam.HugeSteamOutputBus;
import com.raishxn.gtna.common.machine.multiblock.part.steam.WirelessSteamInputHatch;
import com.raishxn.gtna.common.machine.multiblock.part.steam.WirelessSteamOutputHatch;
import com.raishxn.gtna.common.machine.multiblock.steam.LargeSteamCrusher;
import com.raishxn.gtna.common.machine.multiblock.steam.MegaSolarBoilerMachine;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.BiConsumer;

import static com.gregtechceu.gtceu.api.machine.multiblock.PartAbility.EXPORT_FLUIDS;
import static com.gregtechceu.gtceu.api.machine.multiblock.PartAbility.IMPORT_FLUIDS;
import static com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties.IS_FORMED;
import static com.gregtechceu.gtceu.api.pattern.Predicates.*;
import static com.raishxn.gtna.api.registry.GTNARegistry.REGISTRATE;

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
                            "CCCCCCC") // Topo (Y=7)
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
    public static void init() {
    }
}