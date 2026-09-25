package com.raishxn.gtna.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import com.raishxn.gtna.common.data.multiblock.GTOCompressedPatternReader;
import com.raishxn.gtna.common.machine.multiblock.electric.FishingGroundMachine;
import com.raishxn.gtna.common.machine.multiblock.electric.GreenhouseMachine;
import com.raishxn.gtna.common.machine.multiblock.energy.GeneratorArrayMachine;

import static com.gregtechceu.gtceu.api.pattern.Predicates.abilities;
import static com.gregtechceu.gtceu.api.pattern.Predicates.air;
import static com.gregtechceu.gtceu.api.pattern.Predicates.autoAbilities;
import static com.gregtechceu.gtceu.api.pattern.Predicates.blocks;
import static com.gregtechceu.gtceu.api.pattern.Predicates.controller;
import static com.gregtechceu.gtceu.api.pattern.Predicates.fluids;
import static com.gregtechceu.gtceu.api.pattern.Predicates.frames;
import static com.gregtechceu.gtceu.api.pattern.util.RelativeDirection.BACK;
import static com.gregtechceu.gtceu.api.pattern.util.RelativeDirection.RIGHT;
import static com.gregtechceu.gtceu.api.pattern.util.RelativeDirection.UP;
import static com.raishxn.gtna.api.registry.GTNARegistry.REGISTRATE;

/** GTOCore LV–HV multiblock ports kept separate from the older machine registry. */
public final class GTNAMachines3 {

    public static final MultiblockMachineDefinition GENERATOR_ARRAY = REGISTRATE
            .multiblock("generator_array", GeneratorArrayMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.STEAM_TURBINE_FUELS)
            .recipeType(GTRecipeTypes.GAS_TURBINE_FUELS)
            .recipeType(GTRecipeTypes.COMBUSTION_GENERATOR_FUELS)
            .generator(true)
            .recipeModifier(GeneratorArrayMachine::recipeModifier)
            .appearanceBlock(GTBlocks.CASING_STEEL_SOLID)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("XXX", "CCC", "XXX")
                    .aisle("XXX", "C#C", "XXX")
                    .aisle("XSX", "CCC", "XXX")
                    .where('S', controller(blocks(definition.get())))
                    .where('X', blocks(GTBlocks.CASING_STEEL_SOLID.get())
                            .or(abilities(PartAbility.IMPORT_ITEMS).setExactLimit(1))
                            .or(abilities(PartAbility.IMPORT_FLUIDS).setMaxGlobalLimited(4))
                            .or(abilities(PartAbility.EXPORT_FLUIDS).setMaxGlobalLimited(1))
                            .or(abilities(PartAbility.OUTPUT_ENERGY).setExactLimit(1))
                            .or(abilities(PartAbility.MAINTENANCE).setExactLimit(1)))
                    .where('C', blocks(GTBlocks.CASING_TEMPERED_GLASS.get()))
                    .where('#', air())
                    .build())
            .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_solid_steel"),
                    GTCEu.id("block/multiblock/processing_array"))
            .tooltips(Component.translatable("gtna.machine.generator_array.tooltip.0"),
                    Component.translatable("gtna.machine.generator_array.tooltip.1"),
                    Component.translatable("gtna.machine.generator_array.tooltip.2"),
                    Component.translatable("gtna.machine.generator_array.tooltip.3"),
                    Component.translatable("gtna.machine.generator_array.tooltip.4"))
            .register();

    public static final MultiblockMachineDefinition FISHING_GROUND = REGISTRATE
            .multiblock("fishing_ground", FishingGroundMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTNARecipeType.FISHING_GROUND_RECIPES)
            .recipeModifiers(FishingGroundMachine::parallelModifier, GTRecipeModifiers.OC_NON_PERFECT)
            .appearanceBlock(GTNABlocks.ALUMINIUM_BRONZE_CASING)
            .pattern(definition -> GTOCompressedPatternReader.start("fishing_ground")
                    .where('A', blocks(GTNABlocks.ALUMINIUM_BRONZE_CASING.get())
                            .or(autoAbilities(definition.getRecipeTypes()))
                            .or(abilities(PartAbility.PARALLEL_HATCH).setMaxGlobalLimited(1))
                            .or(abilities(PartAbility.MAINTENANCE).setExactLimit(1)))
                    .where('B', blocks(GTNABlocks.ALUMINIUM_BRONZE_CASING.get()))
                    .where('C', fluids(net.minecraft.world.level.material.Fluids.WATER))
                    .where('D', blocks(GTBlocks.CASING_STEEL_PIPE.get()))
                    .where('E', frames(GTMaterials.StainlessSteel))
                    .where('F', controller(blocks(definition.get())))
                    .where(' ', com.gregtechceu.gtceu.api.pattern.Predicates.any())
                    .build())
            .workableCasingModel(com.raishxn.gtna.GTNACORE.id("block/casings/aluminium_bronze_casing"),
                    GTCEu.id("block/multiblock/gcym/large_assembler"))
            .tooltips(Component.translatable("gtna.machine.fishing_ground.tooltip.0"),
                    Component.translatable("gtna.machine.fishing_ground.tooltip.1"),
                    Component.translatable("gtna.machine.fishing_ground.tooltip.2"),
                    Component.translatable("gtna.machine.fishing_ground.tooltip.3"),
                    Component.translatable("gtna.machine.fishing_ground.tooltip.4"),
                    Component.translatable("gtna.machine.fishing_ground.tooltip.5"),
                    Component.translatable("gtna.machine.fishing_ground.tooltip.6"),
                    Component.translatable("gtna.machine.fishing_ground.tooltip.7"),
                    Component.translatable("gtna.machine.fishing_ground.tooltip.8"))
            .register();

    public static final MultiblockMachineDefinition EVAPORATION_PLANT = REGISTRATE
            .multiblock("evaporation_plant", WorkableElectricMultiblockMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTNARecipeType.EVAPORATION_RECIPES)
            .recipeModifiers(GTRecipeModifiers::hatchParallel, GTRecipeModifiers.OC_NON_PERFECT)
            .appearanceBlock(GTNABlocks.STAINLESS_EVAPORATION_CASING)
            .pattern(definition -> FactoryBlockPattern.start(RIGHT, BACK, UP)
                    .aisle("FYF", "YYY", "FYF")
                    .aisle("YSY", "Y#Y", "YYY")
                    .aisle("XXX", "X#X", "XXX").setRepeatable(5)
                    .aisle(" Z ", "ZZZ", " Z ")
                    .where('S', controller(blocks(definition.get())))
                    .where('Y', blocks(GTNABlocks.STAINLESS_EVAPORATION_CASING.get())
                            .or(abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(2))
                            .or(nonSteamFluidInputHatches().setExactLimit(1)))
                    .where('X', blocks(GTNABlocks.STAINLESS_EVAPORATION_CASING.get())
                            .or(abilities(PartAbility.EXPORT_FLUIDS).setMaxLayerLimited(1)))
                    .where('Z', blocks(GTNABlocks.STAINLESS_EVAPORATION_CASING.get()))
                    .where('F', frames(GTMaterials.Aluminium))
                    .where('#', air())
                    .where(' ', com.gregtechceu.gtceu.api.pattern.Predicates.any())
                    .build())
            .workableCasingModel(com.raishxn.gtna.GTNACORE.id("block/casings/stainless_evaporation_casing"),
                    GTCEu.id("block/multiblock/evaporation_plant"))
            .tooltips(Component.translatable("gtna.machine.evaporation_plant.tooltip.0"),
                    Component.translatable("gtna.machine.evaporation_plant.tooltip.1"),
                    Component.translatable("gtna.machine.evaporation_plant.tooltip.2"))
            .register();

    public static final MultiblockMachineDefinition GREENHOUSE = REGISTRATE
            .multiblock("greenhouse", GreenhouseMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTNARecipeType.GREENHOUSE_RECIPES)
            .recipeModifier(GTRecipeModifiers.OC_NON_PERFECT)
            .appearanceBlock(GTBlocks.MACHINE_CASING_ULV)
            .pattern(definition -> GTOCompressedPatternReader.start("greenhouse")
                    .where('E', controller(blocks(definition.get())))
                    .where('G', blocks(GTBlocks.CASING_TEMPERED_GLASS.get()))
                    .where('B', blocks(GTBlocks.MACHINE_CASING_ULV.get())
                            .setMinGlobalLimited(40)
                            .or(autoAbilities(definition.getRecipeTypes()))
                            .or(abilities(PartAbility.MAINTENANCE).setExactLimit(1)))
                    .where('d', blocks(Blocks.MUD)
                            .or(blocks(BuiltInRegistries.BLOCK
                                    .getOptional(new ResourceLocation("farmersdelight", "rich_soil"))
                                    .orElse(Blocks.MUD))))
                    .where('#', air())
                    .where('0', com.gregtechceu.gtceu.api.pattern.Predicates.any())
                    .build())
            .workableCasingModel(GTCEu.id("block/casings/voltage/ulv/side"),
                    GTCEu.id("block/multiblock/fusion_reactor"))
            .tooltips(Component.translatable("gtna.machine.greenhouse.tooltip.0"),
                    Component.translatable("gtna.machine.greenhouse.tooltip.1"),
                    Component.translatable("gtna.machine.greenhouse.tooltip.2"))
            .register();

    private GTNAMachines3() {}

    public static void init() {}

    /** Keep steam-only hatches out of this electric machine's fluid input positions. */
    static TraceabilityPredicate nonSteamFluidInputHatches() {
        return blocks(PartAbility.IMPORT_FLUIDS.getAllBlocks().stream()
                .filter(block -> !PartAbility.STEAM.isApplicable(block))
                .toArray(Block[]::new));
    }
}
