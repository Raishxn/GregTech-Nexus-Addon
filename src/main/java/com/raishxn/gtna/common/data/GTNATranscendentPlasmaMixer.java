package com.raishxn.gtna.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;

import net.minecraft.network.chat.Component;

import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.common.machine.multiblock.electric.TranscendentPlasmaMixerMachine;

import static com.gregtechceu.gtceu.api.pattern.Predicates.abilities;
import static com.gregtechceu.gtceu.api.pattern.Predicates.any;
import static com.gregtechceu.gtceu.api.pattern.Predicates.blocks;
import static com.gregtechceu.gtceu.api.pattern.Predicates.controller;
import static com.raishxn.gtna.api.registry.GTNARegistry.REGISTRATE;

/**
 * GTNH Transcendent Plasma Mixer (G-0193). Structure from {@code MTETranscendentPlasmaMixer} (slices and rows
 * reversed for GTCEu): A Dimensionally Transcendent Casing, B Dimensional Injection Casing or hatches, C Dimensional
 * Bridge Casing. Hatches as in GTNH (input hatches, output hatch, input bus) plus one Parallel Hatch; no energy hatch.
 */
public final class GTNATranscendentPlasmaMixer {

    public static final MultiblockMachineDefinition TRANSCENDENT_PLASMA_MIXER = REGISTRATE
            .multiblock("transcendent_plasma_mixer", TranscendentPlasmaMixerMachine::new)
            .langValue("Transcendent Plasma Mixer")
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTNARecipeType.TRANSCENDENT_PLASMA_MIXER_RECIPES)
            .recipeModifiers(TranscendentPlasmaMixerMachine::wirelessParallel)
            .appearanceBlock(GTNABlocks.DIMENSION_INJECTION_CASING)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle(" CAC ", " ABA ", " ABA ", " ABA ", " ABA ", " ABA ", " CAC ")
                    .aisle("CBBBC", "A   A", "A   A", "A   A", "A   A", "A   A", "CBBBC")
                    .aisle("ABBBA", "B   B", "B   B", "B   B", "B   B", "B   B", "ABBBA")
                    .aisle("CBBBC", "A   A", "A   A", "A   A", "A   A", "A   A", "CBBBC")
                    .aisle(" CAC ", " ABA ", " ABA ", " A~A ", " ABA ", " ABA ", " CAC ")
                    .where('A', blocks(GTNABlocks.DIMENSIONALLY_TRANSCENDENT_CASING.get()))
                    .where('B', blocks(GTNABlocks.DIMENSION_INJECTION_CASING.get())
                            .or(abilities(PartAbility.IMPORT_FLUIDS).setPreviewCount(2))
                            .or(abilities(PartAbility.EXPORT_FLUIDS).setPreviewCount(1))
                            .or(abilities(PartAbility.IMPORT_ITEMS).setPreviewCount(1))
                            .or(abilities(PartAbility.PARALLEL_HATCH).setMaxGlobalLimited(1)))
                    .where('C', blocks(GTNABlocks.PLASMA_FORGE_BRIDGE_CASING.get()))
                    .where('~', controller(blocks(definition.getBlock())))
                    .where(' ', any())
                    .build())
            .workableCasingModel(GTNACORE.id("block/casings/dimension_injection_casing"),
                    GTCEu.id("block/multiblock/fusion_reactor"))
            .tooltips(Component.translatable("gtna.machine.transcendent_plasma_mixer.tooltip.0"),
                    Component.translatable("gtna.machine.transcendent_plasma_mixer.tooltip.1"),
                    Component.translatable("gtna.machine.transcendent_plasma_mixer.tooltip.2"),
                    Component.translatable("gtna.machine.transcendent_plasma_mixer.tooltip.3"),
                    Component.translatable("gtna.machine.transcendent_plasma_mixer.tooltip.4"))
            .register();

    private GTNATranscendentPlasmaMixer() {}

    public static void init() {}
}
