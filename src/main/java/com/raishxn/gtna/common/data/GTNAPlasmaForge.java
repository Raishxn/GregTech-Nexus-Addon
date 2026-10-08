package com.raishxn.gtna.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;

import net.minecraft.network.chat.Component;

import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.common.data.multiblock.GTNAMultiBlockFileReader;
import com.raishxn.gtna.common.machine.multiblock.electric.PlasmaForgeMachine;

import static com.gregtechceu.gtceu.api.pattern.Predicates.abilities;
import static com.gregtechceu.gtceu.api.pattern.Predicates.any;
import static com.gregtechceu.gtceu.api.pattern.Predicates.blocks;
import static com.gregtechceu.gtceu.api.pattern.Predicates.controller;
import static com.gregtechceu.gtceu.api.pattern.Predicates.heatingCoils;
import static com.raishxn.gtna.api.registry.GTNARegistry.REGISTRATE;

/**
 * GTNH Dimensionally Transcendent Plasma Forge (G-0192): structure converted from {@code MTEPlasmaForge}
 * ({@code tools/convert_dtpf_structure.py}): 2121 Transcendent casings, 1273 Injection casings (at least 1250, the
 * rest hatches), 2112 coils and 120 Bridge casings. Up to two energy hatches or one laser, as in GTNH.
 */
public final class GTNAPlasmaForge {

    public static final MultiblockMachineDefinition PLASMA_FORGE = REGISTRATE
            .multiblock("dimensionally_transcendent_plasma_forge", PlasmaForgeMachine::new)
            .langValue("Dimensionally Transcendent Plasma Forge")
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTNARecipeType.PLASMA_FORGE_RECIPES)
            .recipeModifiers(PlasmaForgeMachine::catalystDiscount, GTRecipeModifiers::ebfOverclock)
            .appearanceBlock(GTNABlocks.PLASMA_FORGE_BRIDGE_CASING)
            .pattern(definition -> GTNAMultiBlockFileReader.start(definition, "dtpf_gtnh")
                    .where('A', blocks(GTNABlocks.DIMENSIONALLY_TRANSCENDENT_CASING.get()))
                    .where('B', blocks(GTNABlocks.DIMENSION_INJECTION_CASING.get()).setMinGlobalLimited(1250)
                            .or(abilities(PartAbility.IMPORT_ITEMS).setPreviewCount(1))
                            .or(abilities(PartAbility.EXPORT_ITEMS).setPreviewCount(1))
                            .or(abilities(PartAbility.IMPORT_FLUIDS).setPreviewCount(1))
                            .or(abilities(PartAbility.EXPORT_FLUIDS).setPreviewCount(1))
                            .or(abilities(PartAbility.INPUT_ENERGY).setMaxGlobalLimited(2).setPreviewCount(1))
                            .or(abilities(PartAbility.INPUT_LASER).setMaxGlobalLimited(1).setPreviewCount(0)))
                    .where('C', heatingCoils())
                    .where('D', blocks(GTNABlocks.PLASMA_FORGE_BRIDGE_CASING.get()))
                    .where('~', controller(blocks(definition.getBlock())))
                    .where(' ', any())
                    .build())
            .workableCasingModel(GTNACORE.id("block/casings/plasma_forge_bridge_casing"),
                    GTCEu.id("block/multiblock/fusion_reactor"))
            .tooltips(Component.translatable("gtna.machine.dimensionally_transcendent_plasma_forge.tooltip.0"),
                    Component.translatable("gtna.machine.dimensionally_transcendent_plasma_forge.tooltip.1"),
                    Component.translatable("gtna.machine.dimensionally_transcendent_plasma_forge.tooltip.2"),
                    Component.translatable("gtna.machine.dimensionally_transcendent_plasma_forge.tooltip.3"),
                    Component.translatable("gtna.machine.dimensionally_transcendent_plasma_forge.tooltip.4"),
                    Component.translatable("gtna.machine.dimensionally_transcendent_plasma_forge.tooltip.5"),
                    Component.translatable("gtna.machine.dimensionally_transcendent_plasma_forge.tooltip.6"),
                    Component.translatable("gtna.machine.dimensionally_transcendent_plasma_forge.tooltip.7"),
                    Component.translatable("gtna.machine.dimensionally_transcendent_plasma_forge.tooltip.8"),
                    Component.translatable("gtna.machine.dimensionally_transcendent_plasma_forge.tooltip.9"),
                    Component.translatable("gtna.machine.dimensionally_transcendent_plasma_forge.tooltip.10"),
                    Component.translatable("gtna.machine.dimensionally_transcendent_plasma_forge.tooltip.11"),
                    Component.translatable("gtna.machine.dimensionally_transcendent_plasma_forge.tooltip.12"))
            .register();

    private GTNAPlasmaForge() {}

    public static void init() {}
}
