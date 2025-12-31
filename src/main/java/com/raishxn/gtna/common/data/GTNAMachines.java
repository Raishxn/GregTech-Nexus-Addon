package com.raishxn.gtna.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.raishxn.gtna.common.machine.multiblock.part.steam.WirelessSteamInputHatch;
import com.raishxn.gtna.common.machine.multiblock.part.steam.WirelessSteamOutputHatch;
import com.raishxn.gtna.common.machine.multiblock.steam.LargeSteamCrusher;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import static com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties.IS_FORMED;
import static com.gregtechceu.gtceu.api.pattern.Predicates.blocks;
import static com.raishxn.gtna.api.registry.GTNARegistry.REGISTRATE;
import static com.raishxn.gtna.common.machine.multiblock.steam.LargeSteamCrusher.GTNA_ADD;

public class GTNAMachines {

    private static final ResourceLocation OVERLAY_IN = new ResourceLocation("gtna", "block/overlay/machine/overlay_steam_wireless_in");
    private static final ResourceLocation OVERLAY_OUT = new ResourceLocation("gtna", "block/overlay/machine/overlay_steam_wireless_out");

    // --- INPUT HATCHES ---

    public static final MachineDefinition WIRELESS_STEAM_INPUT_HATCH = REGISTRATE
            .machine("wireless_steam_input_hatch", holder -> new WirelessSteamInputHatch(holder, false))
            .tier(0)
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.STEAM, PartAbility.IMPORT_FLUIDS)
            .colorOverlaySteamHullModel(OVERLAY_IN)
            .modelProperty(GTMachineModelProperties.IS_STEEL_MACHINE, false)
            .modelProperty(IS_FORMED, false) //para o bloco quando formado ficar na mesma textura que o multibloco
            .tooltips(
                    Component.translatable("gtna.machine.wireless_steam_hatch.tooltip"),
                    Component.translatable("gtceu.universal.tooltip.fluid_storage_capacity", 20000)
            )
            .tooltipBuilder(GTNA_ADD)
            .register();

    public static final MachineDefinition WIRELESS_STEAM_INPUT_HATCH_STEEL = REGISTRATE
            .machine("wireless_steam_input_hatch_steel", holder -> new WirelessSteamInputHatch(holder, true))
            .tier(1)
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.STEAM, PartAbility.IMPORT_FLUIDS)
            .colorOverlaySteamHullModel(OVERLAY_IN)
            .modelProperty(GTMachineModelProperties.IS_STEEL_MACHINE, true)
            .modelProperty(IS_FORMED, false)
            .tooltips(
                    Component.translatable("gtna.machine.wireless_steam_hatch.tooltip"),
                    Component.translatable("gtceu.universal.tooltip.fluid_storage_capacity", Integer.MAX_VALUE)
            )
            .tooltipBuilder(GTNA_ADD)
            .register();

    // --- OUTPUT HATCHES ---

    public static final MachineDefinition WIRELESS_STEAM_OUTPUT_HATCH = REGISTRATE
            .machine("wireless_steam_output_hatch", holder -> new WirelessSteamOutputHatch(holder, false))
            .tier(0)
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.STEAM, PartAbility.EXPORT_FLUIDS)
            .colorOverlaySteamHullModel(OVERLAY_OUT)
            .modelProperty(GTMachineModelProperties.IS_STEEL_MACHINE, false)
            .modelProperty(IS_FORMED, false)
            .tooltips(
                    Component.translatable("gtna.machine.wireless_steam_output_hatch.tooltip"),
                    Component.translatable("gtceu.universal.tooltip.fluid_storage_capacity", 20000)
            )
            .tooltipBuilder(GTNA_ADD)
            .register();

    public static final MachineDefinition WIRELESS_STEAM_OUTPUT_HATCH_STEEL = REGISTRATE
            .machine("wireless_steam_output_hatch_steel", holder -> new WirelessSteamOutputHatch(holder, true))
            .tier(1)
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.STEAM, PartAbility.EXPORT_FLUIDS)
            .colorOverlaySteamHullModel(OVERLAY_OUT)
            .modelProperty(GTMachineModelProperties.IS_STEEL_MACHINE, true)
            .modelProperty(IS_FORMED, false)
            .tooltips(
                    Component.translatable("gtna.machine.wireless_steam_output_hatch.tooltip"),
                    Component.translatable("gtceu.universal.tooltip.fluid_storage_capacity", Integer.MAX_VALUE)
            )
            .tooltipBuilder(GTNA_ADD)
            .register();

    // --- MULTIBLOCKS ---

    public static final MultiblockMachineDefinition LARGE_STEAM_CRUSHER = REGISTRATE
            .multiblock("large_steam_crusher", LargeSteamCrusher::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.MACERATOR_RECIPES)
            .appearanceBlock(GTBlocks.CASING_BRONZE_BRICKS)
            .recipeModifier(LargeSteamCrusher::recipeModifier)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("XXX", "XXX", "XXX")
                    .aisle("XXX", "X#X", "XXX")
                    .aisle("XXX", "XSX", "XXX")
                    .where('S', Predicates.controller(blocks(definition.get())))
                    .where('X', blocks(GTBlocks.CASING_BRONZE_BRICKS.get())
                            .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                            .or(Predicates.abilities(PartAbility.STEAM).setExactLimit(1)) // Permite hatch de vapor
                            .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS)) // Permite seus wireless hatches
                            .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS))
                    )
                    .where('#', Predicates.air()) // Ar no meio (estrutura oca)
                    .build())
            .workableCasingModel(
                    GTCEu.id("block/casings/solid/machine_casing_bronze_plated_bricks"),
                    GTCEu.id("block/multiblock/large_steam_crusher")) // Textura do overlay da face
            .tooltipBuilder(GTNA_ADD)
            .register();

    public static void init() {
    }
}