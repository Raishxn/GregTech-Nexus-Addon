package com.raishxn.gtna.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.SimpleGeneratorMachine;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IRotorHolderMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.MultiblockState;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.gregtechceu.gtceu.api.pattern.error.PatternStringError;
import com.gregtechceu.gtceu.api.pattern.predicates.SimplePredicate;
import com.gregtechceu.gtceu.common.data.GCYMBlocks;
import com.gregtechceu.gtceu.common.data.GCYMRecipeTypes;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;
import com.gregtechceu.gtceu.common.machine.multiblock.part.RotorHolderPartMachine;

import com.lowdragmc.lowdraglib.utils.BlockInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.api.machine.multiblock.GTNAPartAbility;
import com.raishxn.gtna.common.data.multiblock.GTOCompressedPatternReader;
import com.raishxn.gtna.common.machine.multiblock.electric.BlazeBlastFurnaceMachine;
import com.raishxn.gtna.common.machine.multiblock.electric.ChemicalPlantMachine;
import com.raishxn.gtna.common.machine.multiblock.electric.ColdIceFreezerMachine;
import com.raishxn.gtna.common.machine.multiblock.electric.ComponentAssemblerMachine;
import com.raishxn.gtna.common.machine.multiblock.electric.ComponentAssemblyLineMachine;
import com.raishxn.gtna.common.machine.multiblock.electric.FishingGroundMachine;
import com.raishxn.gtna.common.machine.multiblock.electric.GreenhouseMachine;
import com.raishxn.gtna.common.machine.multiblock.electric.IndustrialFlotationCellMachine;
import com.raishxn.gtna.common.machine.multiblock.electric.IsaMillMachine;
import com.raishxn.gtna.common.machine.multiblock.electric.IsaMillParallel;
import com.raishxn.gtna.common.machine.multiblock.electric.MegaAlloyBlastSmelterMachine;
import com.raishxn.gtna.common.machine.multiblock.electric.RocketLargeTurbineMachine;
import com.raishxn.gtna.common.machine.multiblock.electric.SupercriticalSteamTurbineMachine;
import com.raishxn.gtna.common.machine.multiblock.electric.VacuumDryingFurnaceMachine;
import com.raishxn.gtna.common.machine.multiblock.energy.GeneratorArrayMachine;

import static com.gregtechceu.gtceu.api.pattern.Predicates.abilities;
import static com.gregtechceu.gtceu.api.pattern.Predicates.air;
import static com.gregtechceu.gtceu.api.pattern.Predicates.any;
import static com.gregtechceu.gtceu.api.pattern.Predicates.autoAbilities;
import static com.gregtechceu.gtceu.api.pattern.Predicates.blocks;
import static com.gregtechceu.gtceu.api.pattern.Predicates.controller;
import static com.gregtechceu.gtceu.api.pattern.Predicates.fluids;
import static com.gregtechceu.gtceu.api.pattern.Predicates.frames;
import static com.gregtechceu.gtceu.api.pattern.Predicates.heatingCoils;
import static com.gregtechceu.gtceu.api.pattern.util.RelativeDirection.BACK;
import static com.gregtechceu.gtceu.api.pattern.util.RelativeDirection.RIGHT;
import static com.gregtechceu.gtceu.api.pattern.util.RelativeDirection.UP;
import static com.raishxn.gtna.api.registry.GTNARegistry.REGISTRATE;

/** GTOCore LV–HV multiblock ports kept separate from the older machine registry. */
public final class GTNAMachines3 {

    /** GTO's electric Dehydrator family (LV through UV). */
    public static final MachineDefinition[] DEHYDRATOR = GTMachineUtils.registerTieredMachines(
            REGISTRATE, "dehydrator",
            (holder, tier) -> new SimpleTieredMachine(holder, tier, GTMachineUtils.defaultTankSizeFunction),
            (tier, builder) -> builder
                    .langValue("%s Dehydrator %s".formatted(GTValues.VLVH[tier], GTValues.VLVT[tier]))
                    .editableUI(SimpleTieredMachine.EDITABLE_UI_CREATOR.apply(GTNACORE.id("dehydrator"),
                            GTNARecipeType.DEHYDRATOR_RECIPES))
                    .rotationState(RotationState.NON_Y_AXIS)
                    .recipeType(GTNARecipeType.DEHYDRATOR_RECIPES)
                    .recipeModifier(GTRecipeModifiers.OC_NON_PERFECT)
                    .workableTieredHullModel(GTNACORE.id("block/machines/dehydrator"))
                    .tooltips(GTMachineUtils.workableTiered(tier, GTValues.V[tier], GTValues.V[tier] * 64,
                            GTNARecipeType.DEHYDRATOR_RECIPES,
                            GTMachineUtils.defaultTankSizeFunction.applyAsInt(tier), true))
                    .register(),
            GTMachineUtils.ELECTRIC_TIERS);

    /** GTO's EV, IV and LuV Rocket Engines; the EV engine is the turbine controller ingredient. */
    public static final MachineDefinition[] ROCKET_ENGINE_GENERATOR = GTMachineUtils.registerTieredMachines(
            REGISTRATE, "rocket_engine",
            (holder, tier) -> new SimpleGeneratorMachine(holder, tier, 0.1F * tier,
                    GTMachineUtils.genericGeneratorTankSizeFunction),
            (tier, builder) -> builder
                    .langValue("%s Rocket Engine %s".formatted(GTValues.VLVH[tier], GTValues.VLVT[tier]))
                    .editableUI(SimpleGeneratorMachine.EDITABLE_UI_CREATOR.apply(GTCEu.id("rocket_engine"),
                            GTNARecipeType.ROCKET_ENGINE_FUELS))
                    .rotationState(RotationState.ALL)
                    .recipeType(GTNARecipeType.ROCKET_ENGINE_FUELS)
                    .recipeModifier(SimpleGeneratorMachine::recipeModifier, true)
                    .addOutputLimit(ItemRecipeCapability.CAP, 0)
                    .addOutputLimit(FluidRecipeCapability.CAP, 0)
                    .simpleGeneratorModel(GTNACORE.id("block/generators/rocket_engine"))
                    .tooltips(GTMachineUtils.workableTiered(tier, GTValues.V[tier], GTValues.V[tier] * 64,
                            GTNARecipeType.ROCKET_ENGINE_FUELS,
                            GTMachineUtils.genericGeneratorTankSizeFunction.applyAsInt(tier), false))
                    .register(),
            GTValues.EV, GTValues.IV, GTValues.LuV);

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

    /**
     * GTOCore 7 × 5 × 5 Component Assembler base with its uniform LV–UV casing rule. The large
     * extension (the two {@code addSubPattern} layers in GTOCore) is registered as GTNA modules in
     * {@code GTNAModules}; with a module formed the machine accepts UV casings and recipes, without
     * one it stays capped at IV like GTOCore's base.
     */
    public static final MultiblockMachineDefinition COMPONENT_ASSEMBLER = REGISTRATE
            .multiblock("component_assembler", ComponentAssemblerMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTNARecipeType.COMPONENT_ASSEMBLY_RECIPES)
            .recipeModifiers(GTRecipeModifiers::hatchParallel, GTRecipeModifiers.OC_NON_PERFECT)
            .appearanceBlock(GTBlocks.CASING_STEEL_SOLID)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("AaaaaaA", "ACDDDCA", "ACDDDCA", "ACDDDCA", "AAAAAAA")
                    .aisle("aAEEEAa", "FG   GF", "FG   GF", "FG   GF", "AACACAA")
                    .aisle("aAEEEAa", "FHI IHF", "FJI IJF", "FG   GF", "AACACAA")
                    .aisle("aAEEEAa", "FG   GF", "FG   GF", "FG   GF", "AACACAA")
                    .aisle("AaaBaaA", "ACDDDCA", "ACDDDCA", "ACDDDCA", "AAAAAAA")
                    .where('A', blocks(GTBlocks.CASING_STEEL_SOLID.get()))
                    .where('a', blocks(GTBlocks.CASING_STEEL_SOLID.get())
                            .or(autoAbilities(definition.getRecipeTypes()))
                            .or(abilities(PartAbility.MAINTENANCE).setExactLimit(1)))
                    .where('B', controller(blocks(definition.get())))
                    .where('C', blocks(GTBlocks.CASING_GRATE.get()))
                    .where('D', blocks(GTBlocks.CASING_TEMPERED_GLASS.get()))
                    .where('E', blocks(GTBlocks.STEEL_HULL.get()))
                    .where('F', componentAssemblyTierCasings())
                    .where('G', blocks(GTNABlocks.MULTI_FUNCTIONAL_CASING.get()))
                    .where('H', frames(GTMaterials.Steel))
                    .where('I', blocks(Blocks.IRON_BARS))
                    .where('J', blocks(GTBlocks.CASING_STEEL_GEARBOX.get()))
                    .where(' ', any())
                    .build())
            .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_solid_steel"),
                    GTCEu.id("block/multiblock/gcym/large_assembler"))
            .tooltips(Component.translatable("gtna.machine.component_assembler.tooltip.0"),
                    Component.translatable("gtna.machine.component_assembler.tooltip.1"),
                    Component.translatable("gtna.machine.component_assembler.tooltip.2"))
            .register();

    static TraceabilityPredicate componentAssemblyTierCasings() {
        Block[] tierBlocks = {
                GTNABlocks.COMPONENT_ASSEMBLY_CASING_LV.get(), GTNABlocks.COMPONENT_ASSEMBLY_CASING_MV.get(),
                GTNABlocks.COMPONENT_ASSEMBLY_CASING_HV.get(), GTNABlocks.COMPONENT_ASSEMBLY_CASING_EV.get(),
                GTNABlocks.COMPONENT_ASSEMBLY_CASING_IV.get(), GTNABlocks.COMPONENT_ASSEMBLY_CASING_LUV.get(),
                GTNABlocks.COMPONENT_ASSEMBLY_CASING_ZPM.get(), GTNABlocks.COMPONENT_ASSEMBLY_CASING_UV.get()
        };
        return new TraceabilityPredicate(state -> {
            for (int i = 0; i < tierBlocks.length; i++) {
                if (!state.getBlockState().is(tierBlocks[i])) continue;
                int tier = i + 1;
                Object first = state.getMatchContext().getOrPut("ComponentAssemblyCasingTier", tier);
                if (first.equals(tier)) return true;
                state.setError(new PatternStringError("gtna.multiblock.pattern.error.component_casing_tier"));
                return false;
            }
            return false;
        }, () -> java.util.Arrays.stream(tierBlocks)
                .map(block -> BlockInfo.fromBlockState(block.defaultBlockState()))
                .toArray(BlockInfo[]::new));
    }

    /** GTOCore Large Greenhouse, including its separate Tree Growth Simulator recipe mode. */
    public static final MultiblockMachineDefinition LARGE_GREENHOUSE = REGISTRATE
            .multiblock("large_greenhouse", WorkableElectricMultiblockMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTNARecipeType.GREENHOUSE_RECIPES)
            .recipeType(GTNARecipeType.TREE_GROWTH_RECIPES)
            .recipeModifiers(GTRecipeModifiers::hatchParallel, GTRecipeModifiers.OC_NON_PERFECT)
            .appearanceBlock(GTBlocks.CASING_STAINLESS_CLEAN)
            .pattern(definition -> GTOCompressedPatternReader.start("large_greenhouse")
                    .where('~', controller(blocks(definition.get())))
                    .where('b', blocks(GTBlocks.CASING_TEMPERED_GLASS.get()))
                    .where('c', frames(GTMaterials.StainlessSteel))
                    .where('d', blocks(Blocks.MUD)
                            .or(blocks(BuiltInRegistries.BLOCK
                                    .getOptional(new ResourceLocation("farmersdelight", "rich_soil"))
                                    .orElse(Blocks.MUD))))
                    .where('e', blocks(GTBlocks.CASING_POLYTETRAFLUOROETHYLENE_PIPE.get()))
                    .where('f', blocks(GTBlocks.CASING_GRATE.get()))
                    .where('a', blocks(GTBlocks.CASING_STAINLESS_CLEAN.get())
                            .setMinGlobalLimited(180)
                            .or(autoAbilities(definition.getRecipeTypes()))
                            .or(abilities(PartAbility.PARALLEL_HATCH).setMaxGlobalLimited(1))
                            .or(abilities(PartAbility.MAINTENANCE).setExactLimit(1)))
                    .where(' ', air())
                    .build())
            .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_clean_stainless_steel"),
                    GTCEu.id("block/multiblock/fusion_reactor"))
            .tooltips(Component.translatable("gtna.machine.large_greenhouse.tooltip.0"),
                    Component.translatable("gtna.machine.large_greenhouse.tooltip.1"),
                    Component.translatable("gtna.machine.large_greenhouse.tooltip.2"))
            .register();

    /** GTOCore blaze blast furnace: coiled EBF shell and continuously consumed molten Blaze. */
    public static final MultiblockMachineDefinition BLAZE_BLAST_FURNACE = REGISTRATE
            .multiblock("blaze_blast_furnace", BlazeBlastFurnaceMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.BLAST_RECIPES)
            .recipeModifiers(BlazeBlastFurnaceMachine::recipeModifier, GTRecipeModifiers.OC_NON_PERFECT)
            .appearanceBlock(GTNABlocks.BLAZE_CASING)
            .pattern(definition -> GTOCompressedPatternReader.start("blaze_blast_furnace")
                    .where('A', blocks(GCYMBlocks.CASING_HIGH_TEMPERATURE_SMELTING.get()))
                    .where('B', blocks(GCYMBlocks.HEAT_VENT.get()))
                    .where('C', blocks(GTBlocks.CASING_TUNGSTENSTEEL_PIPE.get()))
                    .where('D', blocks(GTNABlocks.BLAZE_CASING.get()))
                    .where('E', blocks(GTNABlocks.BLAZE_CASING.get())
                            .or(autoAbilities(definition.getRecipeTypes()))
                            .or(abilities(PartAbility.MAINTENANCE).setExactLimit(1)))
                    .where('F', heatingCoils())
                    .where('G', controller(blocks(definition.get())))
                    .where('H', abilities(PartAbility.MUFFLER))
                    .where(' ', any())
                    .build())
            .workableCasingModel(com.raishxn.gtna.GTNACORE.id("block/casings/blaze_casing"),
                    GTCEu.id("block/multiblock/blast_furnace"))
            .tooltips(Component.translatable("gtna.machine.blaze_blast_furnace.tooltip.0"),
                    Component.translatable("gtna.machine.blaze_blast_furnace.tooltip.1"),
                    Component.translatable("gtna.machine.blaze_blast_furnace.tooltip.2"))
            .register();

    /** GTOCore 9 × 5 × 5 Cold Ice Freezer base structure. */
    public static final MultiblockMachineDefinition COLD_ICE_FREEZER = REGISTRATE
            .multiblock("cold_ice_freezer", ColdIceFreezerMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.VACUUM_RECIPES)
            .recipeType(GTNARecipeType.ATOMIZATION_CONDENSATION_RECIPES)
            .recipeModifiers(ColdIceFreezerMachine::recipeModifier, GTRecipeModifiers.OC_NON_PERFECT)
            .appearanceBlock(GTNABlocks.COLD_ICE_CASING)
            .pattern(definition -> FactoryBlockPattern.start(RIGHT, UP, BACK)
                    .aisle("AAAAA", " BBB ", " BGB ", " BBB ", "AAAAA")
                    .aisle("AAAAA", "BE EB", "BE EB", "BE EB", "AAAAA")
                    .aisle("AAAAA", " F F ", " F F ", " F F ", "ACHCA")
                    .aisle("AAAAA", "BE EB", "BE EB", "BE EB", "ACCCA")
                    .aisle("AAAAA", "D   D", "D   D", "D   D", "ACCCA")
                    .aisle("AAAAA", "BE EB", "BE EB", "BE EB", "ACCCA")
                    .aisle("AAAAA", " F F ", " F F ", " F F ", "ACHCA")
                    .aisle("AAAAA", "BE EB", "BE EB", "BE EB", "AAAAA")
                    .aisle("AAAAA", " BBB ", " BBB ", " BBB ", "AAAAA")
                    .where('A', blocks(GTBlocks.CASING_ALUMINIUM_FROSTPROOF.get()))
                    .where('B', blocks(GTNABlocks.COLD_ICE_CASING.get())
                            .or(autoAbilities(definition.getRecipeTypes()))
                            .or(abilities(PartAbility.MAINTENANCE).setExactLimit(1)))
                    .where('C', blocks(GTNABlocks.COLD_ICE_CASING.get()))
                    .where('D', blocks(GTBlocks.CASING_TEMPERED_GLASS.get()))
                    .where('E', frames(GTMaterials.Aluminium))
                    .where('F', blocks(GTBlocks.CASING_TUNGSTENSTEEL_PIPE.get()))
                    .where('G', controller(blocks(definition.get())))
                    .where('H', abilities(PartAbility.MUFFLER))
                    .where(' ', any())
                    .build())
            .workableCasingModel(com.raishxn.gtna.GTNACORE.id("block/casings/cold_ice_casing"),
                    GTCEu.id("block/multiblock/vacuum_freezer"))
            .tooltips(Component.translatable("gtna.machine.cold_ice_freezer.tooltip.0"),
                    Component.translatable("gtna.machine.cold_ice_freezer.tooltip.1"),
                    Component.translatable("gtna.machine.cold_ice_freezer.tooltip.2"))
            .register();

    /**
     * GTOCore Chemical Plant: coiled LCR shell, Parallel Hatch and a perfect overclock. GTO's
     * Catalyst Hatch and Machine Access Link are GTO-only parts and are not ported; the original
     * casing, coil and PTFE pipe layout is preserved. The overlay reuses GTCEu's Large Chemical
     * Reactor front, which shares the same inert-PTFE casing.
     */
    public static final MultiblockMachineDefinition CHEMICAL_PLANT = REGISTRATE
            .multiblock("chemical_plant", ChemicalPlantMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.LARGE_CHEMICAL_RECIPES)
            .recipeModifier(ChemicalPlantMachine::recipeModifier)
            .appearanceBlock(GTBlocks.CASING_PTFE_INERT)
            .pattern(definition -> GTOCompressedPatternReader.start("chemical_plant")
                    .where('a', controller(blocks(definition.get())))
                    .where('b', blocks(GTBlocks.CASING_PTFE_INERT.get())
                            .setMinGlobalLimited(60)
                            .or(abilities(PartAbility.PARALLEL_HATCH).setMaxGlobalLimited(1))
                            .or(autoAbilities(definition.getRecipeTypes()))
                            .or(abilities(PartAbility.MAINTENANCE).setExactLimit(1)))
                    .where('c', heatingCoils())
                    .where('d', blocks(GTBlocks.CASING_POLYTETRAFLUOROETHYLENE_PIPE.get()))
                    .where(' ', any())
                    .build())
            .additionalDisplay(ChemicalPlantMachine::addCoilDisplay)
            .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_inert_ptfe"),
                    GTCEu.id("block/multiblock/large_chemical_reactor"))
            .tooltips(Component.translatable("gtna.machine.chemical_plant.tooltip.0"),
                    Component.translatable("gtna.machine.chemical_plant.tooltip.1"),
                    Component.translatable("gtna.machine.chemical_plant.tooltip.2"))
            .register();

    /**
     * GTOCore Mega Alloy Blast Smelter pattern aisles (11 aisles × 18 rows × 11 chars), kept as data
     * so the QA GameTest builds exactly what this definition registers. The controller {@code ~}
     * sits at char 5, row 2, aisle 10; with the controller facing NORTH that maps to
     * {@code (5 - char, row - 2, 10 - aisle)} in world space.
     */
    public static final String[][] MEGA_ALLOY_BLAST_SMELTER_PATTERN = {
            { "   eeeee   ", "   cbbbc   ", "   cbbbc   ", "   cbbbc   ", "   eeeee   ", "   bbbbb   ",
                    "           ", "           ", "           ", "           ", "           ", "           ",
                    "           ", "           ", "           ", "           ", "           ", "           " },
            { "  ebbbbbe  ", "  c     c  ", "  c     c  ", "  c     c  ", "  efffffe  ", "  bbbbbbb  ",
                    "   bbbbb   ", "   ccccc   ", "   ccccc   ", "   ccccc   ", "   ccccc   ", "   ccccc   ",
                    "   ccccc   ", "   ccccc   ", "   ccccc   ", "   ccccc   ", "   bbbbb   ", "           " },
            { " ebbbbbbbe ", " cbeeeeebc ", " cbeeeeebc ", " cbeeeeebc ", " ebeeeeebe ", " bbbbbbbbb ",
                    "  baaaaab  ", "  caaaaac  ", "  caaaaac  ", "  caaaaac  ", "  caaaaac  ", "  caaaaac  ",
                    "  caaaaac  ", "  caaaaac  ", "  caaaaac  ", "  caaaaac  ", "  bbbbbbb  ", "   bbbbb   " },
            { "ebbbbbbbbbe", "c ehhhhhe c", "c eiiiiie c", "c ejjjjje c", "efeeeeeeefe", "bbbb   bbbb",
                    " baa   aab ", " caa   aac ", " caa   aac ", " caa   aac ", " caa   aac ", " caa   aac ",
                    " caa   aac ", " caa   aac ", " caa   aac ", " caa   aac ", " bbb   bbb ", "  bbbbbbb  " },
            { "ebbbbbbbbbe", "b ehhhhhe b", "b eiiiiie b", "b ejjjjje b", "efeeeeeeefe", "bbb     bbb",
                    " ba     ab ", " ca     ac ", " ca     ac ", " ca     ac ", " ca     ac ", " ca     ac ",
                    " ca     ac ", " ca     ac ", " ca     ac ", " ca     ac ", " bb     bb ", "  bbbbbbb  " },
            { "ebbbbbbbbbe", "b ehhkhhe b", "b eiikiie b", "b ejjkjje b", "efeeeAeeefe", "bbb  k  bbb",
                    " ba  k  ab ", " ca  k  ac ", " ca  k  ac ", " ca  k  ac ", " ca  k  ac ", " ca  k  ac ",
                    " ca  k  ac ", " ca  k  ac ", " ca  k  ac ", " ca  k  ac ", " bb  k  bb ", "  bbbgbbb  " },
            { "ebbbbbbbbbe", "b ehhhhhe b", "b eiiiiie b", "b ejjjjje b", "efeeeeeeefe", "bbb     bbb",
                    " ba     ab ", " ca     ac ", " ca     ac ", " ca     ac ", " ca     ac ", " ca     ac ",
                    " ca     ac ", " ca     ac ", " ca     ac ", " ca     ac ", " bb     bb ", "  bbbbbbb  " },
            { "ebbbbbbbbbe", "c ehhhhhe c", "c eiiiiie c", "c ejjjjje c", "efeeeeeeefe", "bbbb   bbbb",
                    " baa   aab ", " caa   aac ", " caa   aac ", " caa   aac ", " caa   aac ", " caa   aac ",
                    " caa   aac ", " caa   aac ", " caa   aac ", " caa   aac ", " bbb   bbb ", "  bbbbbbb  " },
            { " ebbbbbbbe ", " cbeeeeebc ", " cbeeeeebc ", " cbeeeeebc ", " ebeeeeebe ", " bbbbbbbbb ",
                    "  baaaaab  ", "  caaaaac  ", "  caaaaac  ", "  caaaaac  ", "  caaaaac  ", "  caaaaac  ",
                    "  caaaaac  ", "  caaaaac  ", "  caaaaac  ", "  caaaaac  ", "  bbbbbbb  ", "   bbbbb   " },
            { "  ebbbbbe  ", "  c     c  ", "  c     c  ", "  c     c  ", "  efffffe  ", "  bbbbbbb  ",
                    "   bbbbb   ", "   ccccc   ", "   ccccc   ", "   ccccc   ", "   ccccc   ", "   ccccc   ",
                    "   ccccc   ", "   ccccc   ", "   ccccc   ", "   ccccc   ", "   bbbbb   ", "           " },
            { "   eeeee   ", "   cbbbc   ", "   cb~bc   ", "   cbbbc   ", "   eeeee   ", "   bbbbb   ",
                    "           ", "           ", "           ", "           ", "           ", "           ",
                    "           ", "           ", "           ", "           ", "           ", "           " },
    };

    /**
     * GTOCore Mega Alloy Blast Smelter: the 11×18×11 coiled GCYM shell running Alloy Blast recipes
     * with a Parallel Hatch and GTO's 0.8× EU / 0.6× duration bonus. GTCEu already ships the normal
     * Alloy Blast Smelter, so only GTOCore's mega id is added. GTO's tiered "integral framework"
     * cell is substituted by a TungstenSteel frame because GTNA does not port that tier-block
     * system; GTO's GCYM abilities map to the standard auto abilities.
     */
    public static final MultiblockMachineDefinition MEGA_ALLOY_BLAST_SMELTER = REGISTRATE
            .multiblock("mega_alloy_blast_smelter", MegaAlloyBlastSmelterMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GCYMRecipeTypes.ALLOY_BLAST_RECIPES)
            .recipeModifier(MegaAlloyBlastSmelterMachine::recipeModifier)
            .appearanceBlock(GCYMBlocks.CASING_HIGH_TEMPERATURE_SMELTING)
            .pattern(definition -> {
                FactoryBlockPattern pattern = FactoryBlockPattern.start();
                for (String[] aisle : MEGA_ALLOY_BLAST_SMELTER_PATTERN) {
                    pattern.aisle(aisle);
                }
                return pattern
                        .where('~', controller(blocks(definition.get())))
                        .where('b', blocks(GCYMBlocks.CASING_HIGH_TEMPERATURE_SMELTING.get()).setMinGlobalLimited(280)
                                .or(autoAbilities(definition.getRecipeTypes()))
                                .or(abilities(PartAbility.MAINTENANCE).setExactLimit(1))
                                .or(abilities(PartAbility.PARALLEL_HATCH).setMaxGlobalLimited(1)))
                        .where('a', heatingCoils())
                        .where('g', abilities(PartAbility.MUFFLER))
                        .where('e', blocks(GCYMBlocks.HEAT_VENT.get()))
                        .where('c', blocks(GTBlocks.CASING_TEMPERED_GLASS.get()))
                        .where('f', blocks(GTBlocks.CASING_EXTREME_ENGINE_INTAKE.get()))
                        .where('h', blocks(GTBlocks.FIREBOX_STEEL.get()))
                        .where('i', blocks(GTBlocks.FIREBOX_TITANIUM.get()))
                        .where('j', blocks(GTBlocks.FIREBOX_TUNGSTENSTEEL.get()))
                        .where('k', blocks(GTBlocks.CASING_TUNGSTENSTEEL_PIPE.get()))
                        .where(' ', any())
                        .where('A', frames(GTMaterials.TungstenSteel))
                        .build();
            })
            .workableCasingModel(GTCEu.id("block/casings/gcym/high_temperature_smelting_casing"),
                    GTCEu.id("block/multiblock/gcym/blast_alloy_smelter"))
            .tooltips(Component.translatable("gtna.machine.mega_alloy_blast_smelter.tooltip.0"),
                    Component.translatable("gtna.machine.mega_alloy_blast_smelter.tooltip.1"),
                    Component.translatable("gtna.machine.mega_alloy_blast_smelter.tooltip.2"))
            .register();

    /**
     * GTOCore ISA Mill: 3×3×7 Inconel-625 shell with a gearbox, a pipe end cap and a Ball Hatch
     * slot. Runs {@code ISA_MILL_RECIPES} with a perfect overclock behind the grinding-ball gate
     * (recipe data {@code grindball} must match the ball in the hatch).
     *
     * <p>
     * GTO's Control Hatch is a GTO-only part and is not ported; the original JEI recovery stack
     * (the first item output of the previewed recipe) is dynamic and GTCEu's
     * {@code recoveryStacks(Supplier)} API cannot express it, so it is not ported either.
     */
    public static final MultiblockMachineDefinition ISA_MILL = REGISTRATE
            .multiblock("isa_mill", IsaMillMachine::new)
            .rotationState(RotationState.ALL)
            .recipeType(GTNARecipeType.ISA_MILL_RECIPES)
            .recipeModifiers(IsaMillParallel::apply, GTRecipeModifiers.OC_PERFECT)
            .appearanceBlock(GTNABlocks.INCONEL_625_CASING)
            .pattern(definition -> GTOCompressedPatternReader.start("isa_mill")
                    .where('~', controller(blocks(definition.get())))
                    .where('B', blocks(GTNABlocks.INCONEL_625_CASING.get())
                            .or(abilities(PartAbility.INPUT_ENERGY).setMaxGlobalLimited(2).setPreviewCount(1))
                            .or(abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(4).setPreviewCount(1))
                            .or(abilities(PartAbility.IMPORT_FLUIDS).setMaxGlobalLimited(1).setPreviewCount(1))
                            .or(abilities(PartAbility.EXPORT_ITEMS).setMaxGlobalLimited(1).setPreviewCount(1))
                            .or(abilities(PartAbility.MAINTENANCE).setExactLimit(1))
                            .or(abilities(PartAbility.MUFFLER).setExactLimit(1)))
                    .where('C', blocks(GTNABlocks.INCONEL_625_GEARBOX.get()))
                    .where('A', blocks(GTNABlocks.INCONEL_625_PIPE.get()))
                    .where('D', abilities(GTNAPartAbility.GRIND_BALL_HATCH).setExactLimit(1))
                    .build())
            .workableCasingModel(GTNACORE.id("block/casings/inconel_625_casing"),
                    GTCEu.id("block/multiblock/gcym/large_maceration_tower"))
            .tooltips(Component.translatable("gtna.machine.isa_mill.tooltip.0"),
                    Component.translatable("gtna.machine.isa_mill.tooltip.1"),
                    Component.translatable("gtna.machine.isa_mill.tooltip.2"))
            .register();

    private GTNAMachines3() {}

    public static void init() {}

    /**
     * GTOCore Rocket Large Turbine: the 3×3×3 large-turbine base with the
     * {@code rocket_large_turbine} sub-pattern module. EV and {@code special = true}, so the base
     * output is {@code V[EV] * 2.5 = 5120 EU/t}; the rocket engine module adds GTO's 2× output,
     * +20% efficiency and 2× high-speed damage multiplier. Runs the ported
     * {@code rocket_engine} recipe family.
     *
     * <p>
     * GTO's controller recipe is a shaped recipe whose center ingredient is
     * {@code GTOMachines.ROCKET_ENGINE_GENERATOR[EV]}, a GTO-only single-block machine, so the
     * recipe is omitted and recorded in {@code ControllerRecipePolicyTest} (same policy as the
     * Chemical Plant).
     */
    public static final MultiblockMachineDefinition ROCKET_LARGE_TURBINE = REGISTRATE
            .multiblock("rocket_large_turbine", holder -> new RocketLargeTurbineMachine(holder, GTValues.EV, true))
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTNARecipeType.ROCKET_ENGINE_FUELS)
            .generator(true)
            .appearanceBlock(GTBlocks.CASING_TITANIUM_TURBINE)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("CCCC", "CHHC", "CCCC")
                    .aisle("CHHC", "RGGR", "CHHC")
                    .aisle("CCCC", "CSHC", "CCCC")
                    .where('S', controller(blocks(definition.get())))
                    .where('G', blocks(GTBlocks.CASING_TITANIUM_GEARBOX.get()))
                    .where('C', blocks(GTBlocks.CASING_TITANIUM_TURBINE.get()))
                    .where('R', rotorBlockFacingOutwards(GTValues.EV).setExactLimit(1)
                            .or(abilities(PartAbility.OUTPUT_ENERGY)).setExactLimit(1))
                    .where('H', blocks(GTBlocks.CASING_TITANIUM_TURBINE.get())
                            .or(autoAbilities(definition.getRecipeTypes(), false, false, true, true, true, true))
                            .or(autoAbilities(true, true, false)))
                    .build())
            .workableCasingModel(GTCEu.id("block/casings/mechanic/machine_casing_turbine_titanium"),
                    GTCEu.id("block/multiblock/generator/large_gas_turbine"))
            .tooltips(Component.translatable("gtna.machine.rocket_large_turbine.tooltip.0"),
                    Component.translatable("gtna.machine.rocket_large_turbine.tooltip.1"),
                    Component.translatable("gtna.machine.rocket_large_turbine.tooltip.2"))
            .register();

    /**
     * GTOCore Supercritical Steam Turbine: the 3×3×3 large-turbine base with the
     * {@code supercritical_steam_turbine} sub-pattern module. IV and {@code special = false}, so
     * the base output is {@code V[IV] * 2 = 16384 EU/t}; the module adds GTO's 2× output, +20%
     * efficiency and 2× high-speed damage multiplier. Runs the ported
     * {@code supercritical_steam_turbine} recipe family, which reuses GTNA's
     * {@code DenseSupercriticalSteam} in place of GTO's {@code SupercriticalSteam}.
     *
     * <p>
     * Unlike the rocket turbine, GTO's controller recipe only uses GTCEu/GTNA resources
     * ({@code GTMachines.HULL[LuV]}, LuV circuits/motor, GTNA's Mar-M200 steel and GTCEu's
     * TungstenCarbide pipes), so it is ported as an Assembler recipe.
     */
    public static final MultiblockMachineDefinition SUPERCRITICAL_STEAM_TURBINE = REGISTRATE
            .multiblock("supercritical_steam_turbine",
                    holder -> new SupercriticalSteamTurbineMachine(holder, GTValues.IV, false))
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTNARecipeType.SUPERCRITICAL_STEAM_TURBINE_FUELS)
            .generator(true)
            .appearanceBlock(GTNABlocks.SUPERCRITICAL_TURBINE_CASING)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("CCCC", "CHHC", "CCCC")
                    .aisle("CHHC", "RGGR", "CHHC")
                    .aisle("CCCC", "CSHC", "CCCC")
                    .where('S', controller(blocks(definition.get())))
                    .where('G', blocks(GTBlocks.CASING_TUNGSTENSTEEL_GEARBOX.get()))
                    .where('C', blocks(GTNABlocks.SUPERCRITICAL_TURBINE_CASING.get()))
                    .where('R', rotorBlockFacingOutwards(GTValues.IV).setExactLimit(1)
                            .or(abilities(PartAbility.OUTPUT_ENERGY)).setExactLimit(1))
                    .where('H', blocks(GTNABlocks.SUPERCRITICAL_TURBINE_CASING.get())
                            .or(autoAbilities(definition.getRecipeTypes(), false, false, true, true, true, true))
                            .or(autoAbilities(true, true, false)))
                    .build())
            .workableCasingModel(GTNACORE.id("block/casings/supercritical_turbine_casing"),
                    GTCEu.id("block/multiblock/generator/large_plasma_turbine"))
            .tooltips(Component.translatable("gtna.machine.supercritical_steam_turbine.tooltip.0"),
                    Component.translatable("gtna.machine.supercritical_steam_turbine.tooltip.1"),
                    Component.translatable("gtna.machine.supercritical_steam_turbine.tooltip.2"))
            .register();

    /**
     * GTOCore Industrial Flotation Cell: the 9-aisle Hastelloy-N75 froth-flotation tank with its
     * central aeration pipe and gearbox. Runs the ported {@code flotating_beneficiation} family with
     * a Parallel Hatch and GTO's perfect overclock; the controller recipe is the ported Assembly
     * Line {@code flotation_cell_regulator}. GTOCore's controller has no muffler slot, so none is
     * added here.
     */
    public static final MultiblockMachineDefinition INDUSTRIAL_FLOTATION_CELL = REGISTRATE
            .multiblock("industrial_flotation_cell", IndustrialFlotationCellMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTNARecipeType.FLOTATING_BENEFICIATION_RECIPES)
            .appearanceBlock(GTNABlocks.HASTELLOY_N_75_CASING)
            .pattern(definition -> GTOCompressedPatternReader.start("industrial_flotation_cell")
                    .where('~', controller(blocks(definition.get())))
                    .where('A', blocks(GTNABlocks.HASTELLOY_N_75_CASING.get())
                            .or(abilities(PartAbility.INPUT_ENERGY).setMaxGlobalLimited(2).setPreviewCount(1))
                            .or(abilities(PartAbility.IMPORT_FLUIDS).setMaxGlobalLimited(1).setPreviewCount(1))
                            .or(abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(2).setPreviewCount(1))
                            .or(abilities(PartAbility.EXPORT_FLUIDS).setMaxGlobalLimited(1).setPreviewCount(1))
                            .or(abilities(PartAbility.PARALLEL_HATCH).setMaxGlobalLimited(1))
                            .or(abilities(PartAbility.MAINTENANCE).setExactLimit(1)))
                    .where('B', blocks(GTNABlocks.FLOTATION_CELL.get()))
                    .where('C', blocks(GTNABlocks.HASTELLOY_N_75_PIPE.get()))
                    .where('D', blocks(GTNABlocks.HASTELLOY_N_75_GEARBOX.get()))
                    .where('E', blocks(GTNABlocks.HASTELLOY_N_75_CASING.get()))
                    .where('#', air())
                    .where(' ', any())
                    .build())
            .workableCasingModel(
                    com.raishxn.gtna.GTNACORE.id("block/casings/hastelloy_n_75_casing"),
                    GTCEu.id("block/multiblock/gcym/large_chemical_bath"))
            .tooltips(Component.translatable("gtna.machine.industrial_flotation_cell.tooltip.0"),
                    Component.translatable("gtna.machine.industrial_flotation_cell.tooltip.1"),
                    Component.translatable("gtna.machine.industrial_flotation_cell.tooltip.2"))
            .register();

    /**
     * GTOCore Vacuum Drying Furnace: the red-steel, coil-heated drying chamber. Runs two families:
     * the ported {@code vacuum_drying} ore-foam recipes (EBF-style overclock, serial) and the GTO
     * {@code dehydrator} family (coil-scaled parallel). GTO's JEI recovery stacks (a dynamic
     * tinydust from the dust output) are not portable through GTCEu's static
     * {@code recoveryItems} API and are omitted, like the ISA Mill.
     *
     * <p>
     * GTO's controller recipe uses four IV Dehydrators; GTNA ports that prerequisite above and
     * retains the original Assembler recipe.
     */
    public static final MultiblockMachineDefinition VACUUM_DRYING_FURNACE = REGISTRATE
            .multiblock("vacuum_drying_furnace", VacuumDryingFurnaceMachine::new)
            .rotationState(RotationState.ALL)
            .recipeType(GTNARecipeType.VACUUM_DRYING_RECIPES)
            .recipeType(GTNARecipeType.DEHYDRATOR_RECIPES)
            .appearanceBlock(GTNABlocks.RED_STEEL_CASING)
            .pattern(definition -> GTOCompressedPatternReader.start("vacuum_drying_furnace")
                    .where('~', controller(blocks(definition.get())))
                    .where('C', abilities(PartAbility.MUFFLER).setExactLimit(1))
                    .where('A', blocks(GTNABlocks.RED_STEEL_CASING.get())
                            .or(abilities(PartAbility.INPUT_ENERGY).setMaxGlobalLimited(2).setPreviewCount(1))
                            .or(abilities(PartAbility.MAINTENANCE).setExactLimit(1))
                            .or(abilities(PartAbility.IMPORT_FLUIDS).setMaxGlobalLimited(2).setPreviewCount(1))
                            .or(abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(2).setPreviewCount(1))
                            .or(abilities(PartAbility.EXPORT_FLUIDS).setMaxGlobalLimited(2).setPreviewCount(1))
                            .or(abilities(PartAbility.EXPORT_ITEMS).setMaxGlobalLimited(1).setPreviewCount(1)))
                    .where('B', heatingCoils())
                    .where(' ', air())
                    .build())
            .workableCasingModel(
                    com.raishxn.gtna.GTNACORE.id("block/casings/red_steel_casing"),
                    GTCEu.id("block/multiblock/fusion_reactor"))
            .tooltips(Component.translatable("gtna.machine.vacuum_drying_furnace.tooltip.0"),
                    Component.translatable("gtna.machine.vacuum_drying_furnace.tooltip.1"),
                    Component.translatable("gtna.machine.vacuum_drying_furnace.tooltip.2"))
            .register();

    /**
     * GTOCore {@code component_assembly_line}: the 31×15×47 tier-cased line running the
     * {@code component_assembly} family with a Parallel Hatch. GTO's block list is replaced by
     * documented GTNA/GTCEu equivalents because the original depends on nine GTO-only casings and
     * materials (see the ledger G-0110 table); the tier casing family
     * {@code component_assembly_line_casing_lv..uv} is ported with GTOCore's textures and the
     * LV–UV tiers (UHV..MAX stay out of scope). GTOCore's controller recipe is an Assembly Line
     * recipe using GTO-only machines/blocks (Advanced Assembly Line, Advanced Assembly Line Unit,
     * Mithril), so it is omitted and recorded in {@code ControllerRecipePolicyTest}.
     *
     * <p>
     * The gtolib base {@code TierCasingCrossRecipeMultiblockMachine} is substituted by a
     * {@link ComponentAssemblyLineMachine} on {@code WorkableElectricMultiblockMachine} with the same
     * uniform-tier rule and hatch parallel; the cross-recipe execution is not reproduced.
     */
    public static final MultiblockMachineDefinition COMPONENT_ASSEMBLY_LINE = REGISTRATE
            .multiblock("component_assembly_line", ComponentAssemblyLineMachine::new)
            .rotationState(RotationState.ALL)
            .recipeType(GTNARecipeType.COMPONENT_ASSEMBLY_RECIPES)
            .recipeModifiers(GTRecipeModifiers::hatchParallel, GTRecipeModifiers.OC_NON_PERFECT)
            .appearanceBlock(GTNABlocks.IRIDIUM_CASING)
            .pattern(definition -> GTOCompressedPatternReader.start("component_assembly_line")
                    .where('A', blocks(GTNABlocks.IRIDIUM_CASING.get()))
                    .where('B', blocks(GTBlocks.CASING_POLYTETRAFLUOROETHYLENE_PIPE.get()))
                    .where('C', blocks(GTNABlocks.NAQUADAH_ALLOY_CASING.get()))
                    .where('D', blocks(GCYMBlocks.CASING_NONCONDUCTING.get()))
                    .where('E', frames(GTNAMaterials.HastelloyN))
                    .where('F', blocks(GTNABlocks.MOLECULAR_CASING.get()))
                    .where('G', blocks(GTNABlocks.TITANIUM_NITRIDE_CERAMIC_IMPACT_RESISTANT_MECHANICAL_BLOCK.get()))
                    .where('H', blocks(GTNABlocks.BORON_CARBIDE_CERAMIC_RADIATION_RESISTANT_MECHANICAL_CUBE.get()))
                    .where('I', blocks(GTNABlocks.PRECISION_PROCESSING_MECHANICAL_CASING.get()))
                    .where('J', frames(GTMaterials.HSLASteel))
                    .where('K', blocks(GTNABlocks.IRIDIUM_CASING.get())
                            .or(abilities(PartAbility.PARALLEL_HATCH).setMaxGlobalLimited(1))
                            .or(abilities(PartAbility.MAINTENANCE).setExactLimit(1))
                            .or(abilities(PartAbility.INPUT_ENERGY).setMaxGlobalLimited(2).setPreviewCount(1))
                            .or(abilities(PartAbility.INPUT_LASER).setMaxGlobalLimited(2))
                            .or(autoAbilities(definition.getRecipeTypes(), false, false, true, true, true, true)))
                    .where('L', frames(GTMaterials.Naquadria))
                    .where('M', blocks(GTBlocks.CASING_EXTREME_ENGINE_INTAKE.get()))
                    .where('N', blocks(GTNABlocks.OXIDATION_RESISTANT_HASTELLOY_N_MECHANICAL_CASING.get()))
                    .where('O', blocks(GTBlocks.HERMETIC_CASING_LuV.get()))
                    .where('P', blocks(GTBlocks.FILTER_CASING.get()))
                    .where('Q', blocks(GTBlocks.CLEANROOM_GLASS.get()))
                    .where('R', blocks(GTNABlocks.ADVANCED_ASSEMBLY_LINE_UNIT.get()))
                    .where('S', blocks(GTNABlocks.CHEMICAL_CORROSION_RESISTANT_PIPE_CASING.get()))
                    .where('T', blocks(GTNABlocks.ZIRCONIA_CERAMIC_HIGH_STRENGTH_BENDING_RESISTANCE_MECHANICAL_BLOCK
                            .get()))
                    .where('U', componentAssemblyLineLights())
                    .where('V', blocks(GCYMBlocks.ELECTROLYTIC_CELL.get()))
                    .where('W', blocks(GTNABlocks.MACHINE_CASING_CIRCUIT_ASSEMBLY_LINE.get()))
                    .where('X', blocks(GTNABlocks.SPACETIME_ASSEMBLY_LINE_UNIT.get()))
                    .where('Y', blocks(GTBlocks.CASING_ASSEMBLY_LINE.get()))
                    .where('Z', blocks(GTNABlocks.PRESSURE_CONTAINMENT_CASING.get()))
                    .where('[', componentAssemblyLineTierCasings())
                    .where('\\', controller(blocks(definition.getBlock())))
                    .where(' ', any())
                    .build())
            .workableCasingModel(GTNACORE.id("block/casings/iridium_casing"),
                    GTCEu.id("block/multiblock/assembly_line"))
            .tooltips(Component.translatable("gtna.machine.component_assembly_line.tooltip.0"),
                    Component.translatable("gtna.machine.component_assembly_line.tooltip.1"),
                    Component.translatable("gtna.machine.component_assembly_line.tooltip.2"))
            .register();

    /**
     * GTOCore's {@code CALMAP} tier-casing rule for the component assembly line: the LV–UV
     * {@code component_assembly_line_casing_*} blocks must all share one tier and record it for the
     * machine's recipe gate.
     */
    private static TraceabilityPredicate componentAssemblyLineTierCasings() {
        Block[] tierBlocks = {
                GTNABlocks.COMPONENT_ASSEMBLY_LINE_CASING_LV.get(),
                GTNABlocks.COMPONENT_ASSEMBLY_LINE_CASING_MV.get(),
                GTNABlocks.COMPONENT_ASSEMBLY_LINE_CASING_HV.get(),
                GTNABlocks.COMPONENT_ASSEMBLY_LINE_CASING_EV.get(),
                GTNABlocks.COMPONENT_ASSEMBLY_LINE_CASING_IV.get(),
                GTNABlocks.COMPONENT_ASSEMBLY_LINE_CASING_LUV.get(),
                GTNABlocks.COMPONENT_ASSEMBLY_LINE_CASING_ZPM.get(),
                GTNABlocks.COMPONENT_ASSEMBLY_LINE_CASING_UV.get()
        };
        return new TraceabilityPredicate(state -> {
            for (int i = 0; i < tierBlocks.length; i++) {
                if (!state.getBlockState().is(tierBlocks[i])) continue;
                int tier = i + 1;
                Object first = state.getMatchContext().getOrPut("ComponentAssemblyLineCasingTier", tier);
                if (first.equals(tier)) return true;
                state.setError(new PatternStringError("gtna.multiblock.pattern.error.component_casing_tier"));
                return false;
            }
            return false;
        }, () -> java.util.Arrays.stream(tierBlocks)
                .map(block -> BlockInfo.fromBlockState(block.defaultBlockState()))
                .toArray(BlockInfo[]::new));
    }

    /** GTOCore's {@code GTOPredicates.light()}: the generic GTCEu lamps, no GTO light casing. */
    private static TraceabilityPredicate componentAssemblyLineLights() {
        Block[] lamps = GTBlocks.LAMPS.values().stream()
                .map(com.tterrag.registrate.util.entry.BlockEntry::get)
                .toArray(Block[]::new);
        return blocks(lamps);
    }

    /** Keep steam-only hatches out of this electric machine's fluid input positions. */
    static TraceabilityPredicate nonSteamFluidInputHatches() {
        return blocks(PartAbility.IMPORT_FLUIDS.getAllBlocks().stream()
                .filter(block -> !PartAbility.STEAM.isApplicable(block))
                .toArray(Block[]::new));
    }

    /**
     * GTOCore {@code GTOPredicates.RotorBlockFacingOutwards}: a rotor holder of at least
     * {@code tier} whose front faces away from the machine's centerline, with GTO's clearance
     * rules (the 3×3 in front must stay clear and no other rotor holder may sit in the surrounding
     * 5×5). GTCEu's own large turbine only checks the front face; the port keeps GTO's tier check
     * and clearance scan.
     */
    private static TraceabilityPredicate rotorBlockFacingOutwards(int tier) {
        return new TraceabilityPredicate(new SimplePredicate(state -> {
            MetaMachine machine = MetaMachine.getMachine(state.getWorld(), state.getPos());
            if (!(machine instanceof IRotorHolderMachine) || machine.getDefinition().getTier() < tier) {
                return false;
            }
            return rotorClearance(state, machine.getFrontFacing());
        }, () -> PartAbility.ROTOR_HOLDER.getAllBlocks().stream()
                .filter(block -> block instanceof MetaMachineBlock metaBlock &&
                        metaBlock.getDefinition().getTier() >= tier)
                .map(BlockInfo::fromBlock)
                .toArray(BlockInfo[]::new)))
                .addTooltips(Component.translatable("gtceu.multiblock.pattern.clear_amount_3"))
                .addTooltips(Component.translatable("gtceu.multiblock.pattern.error.limited.1",
                        GTValues.VN[tier]));
    }

    /** GTOCore {@code GTOPredicates.checkRotorClearance}, using GTCEu's rotor holder part. */
    private static boolean rotorClearance(MultiblockState state, Direction machineFacing) {
        boolean permuteXZ = machineFacing.getAxis() == Direction.Axis.Z;
        for (int x = -2; x < 3; x++) {
            for (int y = -2; y < 3; y++) {
                if (x == 0 && y == 0) continue;
                BlockPos offset = state.getPos().offset(permuteXZ ? x : 0, y, permuteXZ ? 0 : x);
                if (state.getWorld().getBlockState(offset).hasBlockEntity() &&
                        MetaMachine.getMachine(state.getWorld(), offset) instanceof RotorHolderPartMachine) {
                    state.setError(new PatternStringError("gtna.multiblock.pattern.rotor_clearance"));
                    return false;
                }
                if (x == -2 || x == 2 || y == -2 || y == 2) continue;
                if (!state.getWorld().getBlockState(offset.relative(machineFacing)).isAir()) {
                    state.setError(new PatternStringError("gtceu.multiblock.pattern.clear_amount_3"));
                    return false;
                }
            }
        }
        return true;
    }
}
