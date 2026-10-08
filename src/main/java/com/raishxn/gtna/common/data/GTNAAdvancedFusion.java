package com.raishxn.gtna.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.gregtechceu.gtceu.common.data.GCYMBlocks;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;

import com.raishxn.gtna.api.machine.multiblock.GTNAPartAbility;
import com.raishxn.gtna.api.machine.multiblock.GTNASubPatterns;
import com.raishxn.gtna.common.data.material.GodforgeChainMaterials;
import com.raishxn.gtna.common.data.multiblock.GTOCompressedPatternReader;
import com.raishxn.gtna.common.machine.multiblock.electric.AdvancedFusionReactorMachine;

import static com.gregtechceu.gtceu.api.pattern.Predicates.abilities;
import static com.gregtechceu.gtceu.api.pattern.Predicates.any;
import static com.gregtechceu.gtceu.api.pattern.Predicates.blocks;
import static com.gregtechceu.gtceu.api.pattern.Predicates.controller;
import static com.gregtechceu.gtceu.api.pattern.Predicates.frames;
import static com.raishxn.gtna.api.registry.GTNARegistry.REGISTRATE;

/**
 * GTOCore {@code luv_kuangbiao_one_giant_nuclear_fusion_reactor} (MultiBlockD, structure files {@code kuangbiao1..5}
 * and {@code kuangbiao_crossrecipe}) with every GTO block mapped to its GTNA port (G-0191). GTO's Control Hatch is
 * not ported; GTNA also accepts normal/wireless energy hatches next to the lasers.
 */
public final class GTNAAdvancedFusion {

    public static final MultiblockMachineDefinition ADVANCED_FUSION_REACTOR = REGISTRATE
            .multiblock("advanced_fusion_reactor", AdvancedFusionReactorMachine::new)
            .langValue("Advanced Fusion Reactor MK-I")
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.FUSION_RECIPES)
            .appearanceBlock(GTBlocks.FUSION_CASING)
            .pattern(definition -> GTOCompressedPatternReader.start("kuangbiao1")
                    .where('A', blocks(GCYMBlocks.CASING_NONCONDUCTING.get()))
                    .where('B', blocks(GTNABlocks.HIGH_STRENGTH_CONCRETE.get()))
                    .where('C', frames(GTMaterials.Tungsten))
                    .where('D', blocks(GTBlocks.FUSION_CASING.get())
                            .or(abilities(PartAbility.IMPORT_FLUIDS).setPreviewCount(16))
                            .or(abilities(PartAbility.EXPORT_FLUIDS).setPreviewCount(16))
                            .or(abilities(PartAbility.PARALLEL_HATCH).setMaxGlobalLimited(1))
                            .or(abilities(PartAbility.INPUT_LASER).setMaxGlobalLimited(16, 16))
                            .or(abilities(PartAbility.INPUT_ENERGY).setMaxGlobalLimited(16).setPreviewCount(0))
                            .or(abilities(GTNAPartAbility.ACCELERATE_HATCH).setMaxGlobalLimited(1)))
                    .where('E', blocks(GTBlocks.FUSION_CASING.get()))
                    .where('F', frames(GTMaterials.NaquadahAlloy))
                    .where('G', blocks(GTNABlocks.STRENGTHEN_THE_BASE_BLOCK.get()))
                    .where('H', blocks(GTNABlocks.IMPROVED_SUPERCONDUCTOR_COIL.get()))
                    .where('I', blocks(GTNABlocks.PBI_RADIATION_RESISTANT_MECHANICAL_ENCLOSURE.get()))
                    .where('J', blocks(GCYMBlocks.ELECTROLYTIC_CELL.get()))
                    .where('K', blocks(GTBlocks.FUSION_GLASS.get()))
                    .where('L', blocks(GTNABlocks.FISSION_REACTOR_CASING.get()))
                    .where('M', controller(blocks(definition.getBlock())))
                    .where(' ', any())
                    .build())
            .workableCasingModel(GTCEu.id("block/casings/fusion/fusion_casing"),
                    GTCEu.id("block/multiblock/fusion_reactor"))
            .register();

    private GTNAAdvancedFusion() {}

    private static TraceabilityPredicate b(com.tterrag.registrate.util.entry.BlockEntry<? extends Block> entry) {
        return blocks(entry.get());
    }

    public static void init() {
        var id = ADVANCED_FUSION_REACTOR.getId();
        // Tier extensions first: AdvancedFusionReactorMachine counts consecutive formed modules from index 0.
        GTNASubPatterns.register(id, def -> GTOCompressedPatternReader.start("kuangbiao2")
                .where('A', controller(blocks(def.getBlock())))
                .where('B', frames(GTMaterials.Ultimet))
                .where('C', b(GTNABlocks.COBALT_OXIDE_CERAMIC_STRONG_THERMALLY_CONDUCTIVE_MECHANICAL_BLOCK))
                .where('D', b(GTNABlocks.HIGH_PRESSURE_PIPE_CASING))
                .where('E', b(GCYMBlocks.CASING_NONCONDUCTING))
                .where('F', b(GTNABlocks.HYPER_MECHANICAL_CASING))
                .where('G', b(GTNABlocks.ANTIFREEZE_HEATPROOF_MACHINE_CASING))
                .where('H', b(GCYMBlocks.HEAT_VENT))
                .where('I', b(GTBlocks.CASING_PALLADIUM_SUBSTATION))
                .where('J', b(GTNABlocks.BORON_CARBIDE_CERAMIC_RADIATION_RESISTANT_MECHANICAL_CUBE))
                .where('K', b(GTBlocks.CASING_TUNGSTENSTEEL_PIPE))
                .where('L', frames(GTMaterials.Duranium))
                .where('M', b(GTNABlocks.RADIATION_ABSORBENT_CASING))
                .where('N', b(GTBlocks.HIGH_POWER_CASING))
                .where('O', b(GTBlocks.FUSION_CASING_MK2))
                .where('P', b(GTNABlocks.COMPRESSED_FUSION_COIL))
                .where('Q', b(GTNABlocks.IRIDIUM_CASING))
                .where('R', b(GTNABlocks.LASER_CASING))
                .where('S', b(GTBlocks.FUSION_GLASS))
                .where('T', b(GTNABlocks.MAGTECH_CASING))
                .where('U', b(GCYMBlocks.ELECTROLYTIC_CELL))
                .where(' ', any())
                .build(), Component.translatable("gtna.machine.advanced_fusion_reactor.module", 1, "ZPM"));
        GTNASubPatterns.register(id, def -> GTOCompressedPatternReader.start("kuangbiao3")
                .where('A', b(GTBlocks.FUSION_CASING_MK3))
                .where('B', b(GTNABlocks.HYPER_MECHANICAL_CASING))
                .where('C', frames(GTMaterials.Tritanium))
                .where('D', b(GTNABlocks.IRIDIUM_CASING))
                .where('E', b(GTBlocks.HIGH_POWER_CASING))
                .where('F', b(GTNABlocks.ADVANCED_COMPRESSED_FUSION_COIL))
                .where('G', b(GTNABlocks.OPTICAL_RESONANCE_CHAMBER))
                .where('H', b(GTNABlocks.LASER_CASING))
                .where('I', b(GTBlocks.CASING_EXTREME_ENGINE_INTAKE))
                .where('J', frames(GTMaterials.Naquadria))
                .where('K', b(GCYMBlocks.ELECTROLYTIC_CELL))
                .where('L', b(GTBlocks.FUSION_GLASS))
                .where('M', b(GCYMBlocks.CASING_LASER_SAFE_ENGRAVING))
                .where('N', controller(blocks(def.getBlock())))
                .where('O', b(GTNABlocks.NAQUADAH_ALLOY_CASING))
                .where('P', b(GTBlocks.CASING_PALLADIUM_SUBSTATION))
                .where('Q', b(GTNABlocks.TITANIUM_NITRIDE_CERAMIC_IMPACT_RESISTANT_MECHANICAL_BLOCK))
                .where('R', b(GTNABlocks.CHEMICAL_CORROSION_RESISTANT_PIPE_CASING))
                .where('S', b(GTBlocks.CASING_ASSEMBLY_LINE))
                .where('T', b(GTBlocks.CASING_POLYTETRAFLUOROETHYLENE_PIPE))
                .where('U', b(GCYMBlocks.CASING_NONCONDUCTING))
                .where('V', b(GTNABlocks.PRESSURE_CONTAINMENT_CASING))
                .where(' ', any())
                .build(), Component.translatable("gtna.machine.advanced_fusion_reactor.module", 2, "UV"));
        GTNASubPatterns.register(id, def -> GTOCompressedPatternReader.start("kuangbiao4")
                .where('A', frames(GTMaterials.Trinium))
                .where('B', b(GTNABlocks.NAQUADAH_ALLOY_CASING))
                .where('C', frames(GTMaterials.Naquadah))
                .where('D', b(GTNABlocks.NEUTRONIUM_STABLE_CASING))
                .where('E', b(GTNABlocks.HYPER_MECHANICAL_CASING))
                .where('F', b(GTNABlocks.FUSION_CASING_MK4))
                .where('G', b(GTNABlocks.PRESSURE_CONTAINMENT_CASING))
                .where('H', b(GTNABlocks.STRONTIUM_CARBONATE_CERAMIC_RAY_ABSORBING_MECHANICAL_CUBE))
                .where('I', b(GTNABlocks.ELECTRON_PERMEABLE_AMPROSIUM_COATED_GLASS))
                .where('J', b(GTBlocks.COMPUTER_CASING))
                .where('K', b(GTBlocks.FUSION_GLASS))
                .where('L', b(GTNABlocks.IRIDIUM_CASING))
                .where('M', b(GTBlocks.HIGH_POWER_CASING))
                .where('N', b(GCYMBlocks.ELECTROLYTIC_CELL))
                // GTO's Amprosium is an alias of Neutronium.
                .where('O', frames(GTMaterials.Neutronium))
                .where('P', b(GTNABlocks.ACCELERATED_PIPELINE))
                .where('Q', b(GTNABlocks.BORON_CARBIDE_CERAMIC_RADIATION_RESISTANT_MECHANICAL_CUBE))
                .where('R', b(GTBlocks.CASING_PALLADIUM_SUBSTATION))
                .where('S', b(GTBlocks.BATTERY_EMPTY_TIER_II))
                .where('T', b(GTNABlocks.CHEMICAL_CORROSION_RESISTANT_PIPE_CASING))
                .where('U', b(GTNABlocks.RADIATION_ABSORBENT_CASING))
                .where('V', b(GTNABlocks.HIGH_STRENGTH_SUPPORT_MECHANICAL_CASING))
                .where('W', b(GTNABlocks.MOLECULAR_CASING))
                .where('X', b(GTNABlocks.CONTAINMENT_FIELD_GENERATOR))
                .where('Y', b(GTNABlocks.HOLLOW_CASING))
                .where('Z', b(GTNABlocks.COMPRESSED_FUSION_COIL_MK2_PROTOTYPE))
                .where('[', b(GTNABlocks.STRENGTHEN_THE_BASE_BLOCK))
                .where('r', b(GTNABlocks.NAQUADAH_REINFORCED_PLANT_CASING))
                .where(']', b(GTNABlocks.AMPROSIUM_PIPE_CASING))
                .where('^', b(GTNABlocks.MAGNESIUM_OXIDE_CERAMIC_HIGH_TEMPERATURE_INSULATION_MECHANICAL_BLOCK))
                .where('_', controller(blocks(def.getBlock())))
                .where(' ', any())
                .build(), Component.translatable("gtna.machine.advanced_fusion_reactor.module", 3, "UHV"));
        GTNASubPatterns.register(id, def -> GTOCompressedPatternReader.start("kuangbiao5")
                .where('A', frames(GodforgeChainMaterials.OdysseyNanoSuperalloy))
                .where('B', b(GTNABlocks.COMPRESSED_FUSION_COIL_MK2))
                .where('C', frames(GodforgeChainMaterials.PlatinumManganeseAntimonyHeuslerAlloy))
                .where('D', b(GTNABlocks.VACUUM_CHAMBER_BEAM_BLOCK))
                .where('E', b(GTNABlocks.HIGH_PRESSURE_GAS_STORAGE_TANKS_CASING))
                .where('F', b(GTNABlocks.HERMETIC_CASING_UEV))
                .where('G', b(GTNABlocks.HIGH_PRESSURE_PIPE_CASING))
                .where('H', frames(GTMaterials.Trinium))
                .where('I', b(GTNABlocks.FUSION_CASING_MK5))
                .where('J', b(GTNABlocks.PLASMA_FIELD_GLASS))
                .where('K', b(GTNABlocks.ACCELERATOR_PROTECTION_CASING))
                .where('L', b(GTNABlocks.RADIATION_ABSORBENT_CASING))
                .where('M', b(GTNABlocks.ACCELERATOR_OBSERVATION_GLASS))
                .where('N', b(GTNABlocks.FISSION_REACTOR_CASING))
                .where('O', b(GTNABlocks.NAQUADAH_ALLOY_CASING))
                .where('P', b(GTBlocks.FUSION_GLASS))
                .where('Q', b(GTNABlocks.MACHINING_CONTROL_CASING_MK3))
                .where('R', b(GCYMBlocks.ELECTROLYTIC_CELL))
                .where('S', b(GTNABlocks.CONTAINMENT_FIELD_GENERATOR))
                .where('T', b(GTNABlocks.IMPROVED_SUPERCONDUCTOR_COIL))
                .where('U', b(GTBlocks.HIGH_POWER_CASING))
                .where('V', b(GTNABlocks.COOLANT_PIPE_CASING))
                .where('W', b(GTNABlocks.OPTICAL_RESONANCE_CHAMBER))
                .where('X', b(GTNABlocks.MOLECULAR_COIL))
                .where('Y', b(GTNABlocks.FUSION_CASING_MK4))
                .where('Z', b(GTBlocks.CASING_PALLADIUM_SUBSTATION))
                .where('[', b(GTNABlocks.ACCELERATOR_MAGNETIC_CONSTRAINED_RAIL_CASING))
                .where('\\', b(GTNABlocks.RESTRAINT_DEVICE))
                .where(']', controller(blocks(def.getBlock())))
                .where(' ', any())
                .build(), Component.translatable("gtna.machine.advanced_fusion_reactor.module", 4, "UEV"));
        GTNASubPatterns.register(id, def -> GTOCompressedPatternReader.start("kuangbiao_crossrecipe")
                .where('A', b(GTBlocks.FUSION_CASING))
                .where('B', b(GTBlocks.FUSION_CASING)
                        .or(abilities(GTNAPartAbility.THREAD_HATCH).setMaxGlobalLimited(1))
                        .or(abilities(GTNAPartAbility.OVERCLOCK_HATCH).setMaxGlobalLimited(1)))
                .where('C', b(GCYMBlocks.CASING_NONCONDUCTING))
                .where('c', blocks(GCYMBlocks.CASING_NONCONDUCTING.get(), GTNABlocks.FUSION_CASING_MK4.get()))
                .where('d', blocks(GCYMBlocks.CASING_NONCONDUCTING.get(), GCYMBlocks.ELECTROLYTIC_CELL.get()))
                .where('D', b(GTBlocks.COMPUTER_CASING))
                .where('E', b(GTNABlocks.MACHINING_CONTROL_CASING_MK2))
                .where('F', b(GTBlocks.COMPUTER_HEAT_VENT))
                .where('G', b(GTBlocks.HIGH_POWER_CASING))
                .where('H', b(GTBlocks.CASING_PALLADIUM_SUBSTATION))
                .where('I', frames(GTMaterials.Tritanium))
                .where('J', controller(blocks(def.getBlock())))
                .where(' ', any())
                .build(),
                Component.translatable("gtna.machine.advanced_fusion_reactor.cross_recipe"));
    }
}
