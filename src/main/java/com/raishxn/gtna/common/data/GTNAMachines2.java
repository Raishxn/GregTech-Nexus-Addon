package com.raishxn.gtna.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.api.machine.multiblock.GTNAPartAbility;
import com.raishxn.gtna.common.machine.multiblock.electric.WorkableElectricMultipleRecipesMachine;
import com.raishxn.gtna.common.machine.multiblock.part.AccelerateHatchPartMachine;
import com.raishxn.gtna.common.machine.multiblock.part.AdvancedParallelHatchPartMachine;
import com.raishxn.gtna.common.machine.multiblock.part.InfiniteInputBusPartMachine;
import com.raishxn.gtna.common.machine.multiblock.part.InfiniteInputHatchPartMachine;
import com.raishxn.gtna.common.machine.multiblock.part.OverclockHatchPartMachine;
import com.raishxn.gtna.common.machine.multiblock.part.OutputBoostFluidHatchPartMachine;
import com.raishxn.gtna.common.machine.multiblock.part.OutputBoostHatchPartMachine;
import com.raishxn.gtna.common.machine.multiblock.part.OutputBoostItemBusPartMachine;
import com.raishxn.gtna.common.machine.multiblock.part.ThreadPartMachine;
import com.raishxn.gtna.common.machine.multiblock.part.ae.GTNACraftPatternPartMachine;
import com.raishxn.gtna.common.machine.multiblock.part.ae.GTNAMEPatternBufferPartMachine;

import java.util.Locale;

import static com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties.IS_FORMED;
import static com.gregtechceu.gtceu.api.pattern.Predicates.*;
import static com.raishxn.gtna.api.registry.GTNARegistry.REGISTRATE;

public class GTNAMachines2 {

    public static final MachineDefinition[] ADVANCED_PARALLEL_HATCH = new MachineDefinition[GTValues.MAX + 1];
    public static final MachineDefinition[] ACCELERATE_HATCHES = new MachineDefinition[GTValues.MAX + 1];
    public static final MachineDefinition[] THREAD_HATCHES = new MachineDefinition[GTValues.MAX + 1];
    public static final MachineDefinition[] OVERCLOCK_HATCHES = new MachineDefinition[GTValues.MAX + 1];
    public static final MachineDefinition[] OUTPUT_BOOST_HATCHES = new MachineDefinition[GTValues.MAX + 1];
    public static final MachineDefinition[] INFINITE_INPUT_BUSES = new MachineDefinition[GTValues.MAX + 1];
    public static final MachineDefinition[] INFINITE_INPUT_HATCHES = new MachineDefinition[GTValues.MAX + 1];
    public static final MachineDefinition[] OUTPUT_BOOST_ITEM_BUSES = new MachineDefinition[GTValues.MAX + 1];
    public static final MachineDefinition[] OUTPUT_BOOST_FLUID_HATCHES = new MachineDefinition[GTValues.MAX + 1];
    public static MachineDefinition ME_MINI_PATTERN_BUFFER;
    public static MachineDefinition ME_PATTERN_BUFFER;
    public static MachineDefinition ME_ADVANCED_PATTERN_BUFFER;
    public static MachineDefinition ME_ULTIMATE_PATTERN_BUFFER;
    public static MachineDefinition ME_CRAFT_PATTERN_HATCH;

    public static void init() {
        registerPatternBuffers();
        registerParallelHatch(GTValues.UHV, 1024);
        registerParallelHatch(GTValues.UEV, 4096);
        registerParallelHatch(GTValues.UIV, 16384);
        registerParallelHatch(GTValues.UXV, 65536);
        registerParallelHatch(GTValues.OpV, 262144);
        registerOverclockHatches();
        registerThreadHatches();
        for (int i = GTValues.LV; i <= GTValues.MAX; i++) {
            registerAccelerateHatch(i);
            registerOutputBoostHatch(i);
            registerInfiniteInputBus(i);
            registerInfiniteInputHatch(i);
            registerOutputBoostItemBus(i);
            registerOutputBoostFluidHatch(i);
        }
    }

    public static final MultiblockMachineDefinition DURATION_TESTER = REGISTRATE
            .multiblock("duration_tester", WorkableElectricMultipleRecipesMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.ASSEMBLER_RECIPES)
            .appearanceBlock(GTBlocks.CASING_STEEL_SOLID)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("CCC", "CCC", "CCC")
                    .aisle("CCC", "C#C", "CCC")
                    .aisle("CCC", "CSC", "CCC")
                    .where('S', controller(blocks(definition.get())))
                    .where('C', blocks(GTBlocks.CASING_STEEL_SOLID.get())
                            .or(autoAbilities(definition.getRecipeTypes()))
                            .or(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1))
                            .or(Predicates.abilities(PartAbility.PARALLEL_HATCH).setMaxGlobalLimited(1))
                            .or(Predicates.abilities(GTNAPartAbility.OUTPUT_BOOST_HATCH).setMaxGlobalLimited(1))
                            .or(Predicates.abilities(GTNAPartAbility.THREAD_HATCH).setMaxGlobalLimited(1))
                            .or(Predicates.abilities(GTNAPartAbility.OVERCLOCK_HATCH).setMaxGlobalLimited(1))
                            .or(Predicates.abilities(GTNAPartAbility.ACCELERATE_HATCH).setMaxGlobalLimited(1))
                            .or(Predicates.abilities(PartAbility.MUFFLER).setMaxGlobalLimited(1)))
                    .where('#', Predicates.air())
                    .build())
            .workableCasingModel(
                    GTCEu.id("block/casings/solid/machine_casing_solid_steel"),
                    GTCEu.id("block/multiblock/implosion_compressor"))
            .tooltips(Component.literal("§6Machine for testing Duration & Accelerate Hatches"))
            .register();

    private static void registerPatternBuffers() {
        ME_MINI_PATTERN_BUFFER = registerPatternBuffer("me_mini_pattern_buffer", GTValues.LuV, 9);
        ME_PATTERN_BUFFER = registerPatternBuffer("me_pattern_buffer", GTValues.ZPM, 21);
        ME_ADVANCED_PATTERN_BUFFER = registerPatternBuffer("me_advanced_pattern_buffer", GTValues.UV, 32);
        ME_ULTIMATE_PATTERN_BUFFER = registerPatternBuffer("me_ultimate_pattern_buffer", GTValues.UHV, 72);
        ME_CRAFT_PATTERN_HATCH = registerCraftPatternHatch();
    }

    private static MachineDefinition registerPatternBuffer(String id, int tier, int slotCount) {
        return REGISTRATE.machine(id, holder -> new GTNAMEPatternBufferPartMachine(holder, slotCount))
                .tier(tier)
                .rotationState(RotationState.ALL)
                .abilities(
                        PartAbility.IMPORT_ITEMS,
                        PartAbility.IMPORT_FLUIDS,
                        PartAbility.EXPORT_FLUIDS,
                        PartAbility.EXPORT_ITEMS)
                .colorOverlayTieredHullModel(GTCEu.id("block/overlay/appeng/me_buffer_hatch"))
                .tooltips(
                        Component.translatable("gtna.machine.pattern_buffer.tooltip"),
                        Component.translatable("gtna.machine.pattern_buffer.slots", slotCount),
                        Component.translatable("gtna.machine.pattern_buffer.break_persist"),
                        Component.translatable("gtna.machine.pattern_buffer.specialization_pending"))
                .register();
    }

    private static MachineDefinition registerCraftPatternHatch() {
        return REGISTRATE.machine("me_craft_pattern_hatch", holder -> new GTNACraftPatternPartMachine(holder, 72))
                .tier(GTValues.ZPM)
                .rotationState(RotationState.ALL)
                .abilities(
                        PartAbility.IMPORT_ITEMS,
                        PartAbility.IMPORT_FLUIDS,
                        PartAbility.EXPORT_FLUIDS,
                        PartAbility.EXPORT_ITEMS)
                .colorOverlayTieredHullModel(GTCEu.id("block/overlay/appeng/me_buffer_hatch"))
                .tooltips(
                        Component.translatable("gtna.machine.craft_pattern_hatch.tooltip"),
                        Component.translatable("gtna.machine.craft_pattern_hatch.slots", 72),
                        Component.translatable("gtna.machine.craft_pattern_hatch.patterns"),
                        Component.translatable("gtna.machine.craft_pattern_hatch.cheat"))
                .register();
    }

    private static void registerParallelHatch(int tier, int parallelAmount) {
        GTNACORE.LOGGER.info("TENTANDO REGISTRAR PARALLEL HATCH: " + tier);
        String tierName = GTValues.VN[tier].toLowerCase(Locale.ROOT);
        int mkLevel = tier - 8;
        var texturePath = GTNACORE.id("block/machines/parallel_hatch/parallel_hatch_mk" + mkLevel + "/overlay_front");

        // Define as texturas padrão do GTCEu para este tier
        ResourceLocation hullSide = GTCEu.id("block/casings/voltage/" + tierName + "/side");
        ResourceLocation hullTop = GTCEu.id("block/casings/voltage/" + tierName + "/top");
        ResourceLocation hullBottom = GTCEu.id("block/casings/voltage/" + tierName + "/bottom");

        ADVANCED_PARALLEL_HATCH[tier] = REGISTRATE
                .machine("parallel_hatch_" + tierName,
                        holder -> new AdvancedParallelHatchPartMachine(holder, tier, parallelAmount))
                .tier(tier)
                .rotationState(RotationState.ALL)
                .abilities(PartAbility.PARALLEL_HATCH)
                .modelProperty(IS_FORMED, false)
                .modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE)
                .model((ctx, prov, builder) -> {
                    String modelName = "block/machines/parallel_hatch/parallel_hatch_" + tierName;
                    var model = prov.models()
                            .withExistingParent(modelName, GTCEu.id("block/machine/template/part/hatch_machine"))
                            .texture("overlay", texturePath)
                            // CORREÇÃO: Definir todas as faces e particle
                            .texture("side", hullSide)
                            .texture("top", hullTop)
                            .texture("bottom", hullBottom)
                            .texture("particle", hullSide);
                    builder.partialState().setModel(model);
                })
                .tooltips(
                        Component.translatable("gtna.machine.parallel_hatch.tooltip"),
                        Component.translatable("gtna.machine.parallel_hatch.tier",
                                parallelAmount == Integer.MAX_VALUE ? "Infinite" : parallelAmount),
                        Component.translatable("gtceu.part_sharing.disabled"))
                .register();
    }

    private static void registerAccelerateHatch(int tier) {
        String tierName = GTValues.VN[tier].toLowerCase(Locale.ROOT);
        String regName = "accelerate_hatch_" + tierName;
        int mkLevel = tier;
        var texturePath = GTNACORE
                .id("block/machines/accelerate_hatch/accelerate_hatch_mk" + mkLevel + "/overlay_front");

        ResourceLocation hullSide = GTCEu.id("block/casings/voltage/" + tierName + "/side");
        ResourceLocation hullTop = GTCEu.id("block/casings/voltage/" + tierName + "/top");
        ResourceLocation hullBottom = GTCEu.id("block/casings/voltage/" + tierName + "/bottom");

        int minPercentage = Math.max(1, 50 - (2 * (tier - 1)));
        ACCELERATE_HATCHES[tier] = REGISTRATE
                .machine(regName, holder -> new AccelerateHatchPartMachine(holder, tier))
                .tier(tier)
                .rotationState(RotationState.ALL)
                .abilities(GTNAPartAbility.ACCELERATE_HATCH)
                .modelProperty(IS_FORMED, false)
                .modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE)
                .model((ctx, prov, builder) -> {
                    String modelName = "block/machines/accelerate_hatch/accelerate_hatch_" + tierName;
                    var model = prov.models()
                            .withExistingParent(modelName, GTCEu.id("block/machine/template/part/hatch_machine"))
                            .texture("overlay", texturePath)
                            // CORREÇÃO
                            .texture("side", hullSide)
                            .texture("top", hullTop)
                            .texture("bottom", hullBottom)
                            .texture("particle", hullSide);
                    builder.partialState().setModel(model);
                })
                .tooltips(
                        Component.translatable("gtna.machine.accelerate_hatch.main_function"),
                        Component.translatable("gtna.machine.accelerate_hatch.range", minPercentage + "%"),
                        Component.translatable("gtna.machine.accelerate_hatch.weakness"),
                        Component.translatable("gtceu.part_sharing.disabled"))
                .register();
    }

    private static void registerThreadHatches() {
        int[] tiers = {
                GTValues.ZPM,
                GTValues.UV, GTValues.UHV, GTValues.UEV,
                GTValues.UIV, GTValues.UXV, GTValues.OpV, GTValues.MAX
        };
        for (int i = 0; i < tiers.length; i++) {
            int tier = tiers[i];
            int mkLevel = i + 1;
            String tierName = GTValues.VN[tier].toLowerCase(Locale.ROOT);
            String regName = "thread_hatch_" + tierName;
            var texturePath = GTNACORE.id("block/machines/thread_hatch/thread_hatch_mk" + mkLevel + "/overlay_front");

            ResourceLocation hullSide = GTCEu.id("block/casings/voltage/" + tierName + "/side");
            ResourceLocation hullTop = GTCEu.id("block/casings/voltage/" + tierName + "/top");
            ResourceLocation hullBottom = GTCEu.id("block/casings/voltage/" + tierName + "/bottom");

            int threads = (1 << (tier - 6)) - 1;

            THREAD_HATCHES[tier] = REGISTRATE
                    .machine(regName, holder -> new ThreadPartMachine(holder, tier))
                    .tier(tier)
                    .rotationState(RotationState.ALL)
                    .abilities(GTNAPartAbility.THREAD_HATCH)
                    .modelProperty(IS_FORMED, false)
                    .modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE)
                    .model((ctx, prov, builder) -> {
                        String modelName = "block/machines/thread_hatch/thread_hatch_" + tierName;
                        var model = prov.models()
                                .withExistingParent(modelName, GTCEu.id("block/machine/template/part/hatch_machine"))
                                .texture("overlay", texturePath)
                                // CORREÇÃO
                                .texture("side", hullSide)
                                .texture("top", hullTop)
                                .texture("bottom", hullBottom)
                                .texture("particle", hullSide);
                        builder.partialState().setModel(model);
                    })
                    .tooltips(
                            Component.translatable("gtna.machine.thread_hatch.tooltip", threads),
                            Component.translatable("gtceu.part_sharing.disabled"))
                    .register();
        }
    }

    private static void registerOutputBoostHatch(int tier) {
        String tierName = GTValues.VN[tier].toLowerCase(Locale.ROOT);
        String regName = "output_boost_hatch_" + tierName;
        var texturePath = GTCEu.id("block/overlay/machine/overlay_hatch");
        int multiplier = OutputBoostHatchPartMachine.getMultiplierForTier(tier);

        ResourceLocation hullSide = GTCEu.id("block/casings/voltage/" + tierName + "/side");
        ResourceLocation hullTop = GTCEu.id("block/casings/voltage/" + tierName + "/top");
        ResourceLocation hullBottom = GTCEu.id("block/casings/voltage/" + tierName + "/bottom");

        OUTPUT_BOOST_HATCHES[tier] = REGISTRATE
                .machine(regName, holder -> new OutputBoostHatchPartMachine(holder, tier))
                .tier(tier)
                .rotationState(RotationState.ALL)
                .abilities(GTNAPartAbility.OUTPUT_BOOST_HATCH)
                .modelProperty(IS_FORMED, false)
                .modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE)
                .model((ctx, prov, builder) -> {
                    String modelName = "block/machines/output_boost_hatch/output_boost_hatch_" + tierName;
                    var model = prov.models()
                            .withExistingParent(modelName, GTCEu.id("block/machine/template/part/hatch_machine"))
                            .texture("overlay", texturePath)
                            .texture("side", hullSide)
                            .texture("top", hullTop)
                            .texture("bottom", hullBottom)
                            .texture("particle", hullSide);
                    builder.partialState().setModel(model);
                })
                .tooltips(
                        Component.translatable("gtna.machine.output_boost_hatch.main_function"),
                        Component.translatable("gtna.machine.output_boost_hatch.multiplier", multiplier),
                        Component.translatable("gtceu.part_sharing.disabled"))
                .register();
    }

    private static void registerInfiniteInputBus(int tier) {
        String tierName = GTValues.VN[tier].toLowerCase(Locale.ROOT);
        INFINITE_INPUT_BUSES[tier] = REGISTRATE
                .machine("infinite_input_bus_" + tierName, holder -> new InfiniteInputBusPartMachine(holder, tier))
                .tier(tier)
                .rotationState(RotationState.ALL)
                .abilities(PartAbility.IMPORT_ITEMS)
                .modelProperty(IS_FORMED, false)
                .model((ctx, prov, builder) -> {
                    var model = prov.models()
                            .withExistingParent(ctx.getName(), GTCEu.id("block/machine/template/part/hatch_machine"))
                            .texture("overlay", GTCEu.id("block/overlay/machine/overlay_item_hatch_input"))
                            .texture("side", GTCEu.id("block/casings/voltage/" + tierName + "/side"))
                            .texture("top", GTCEu.id("block/casings/voltage/" + tierName + "/top"))
                            .texture("bottom", GTCEu.id("block/casings/voltage/" + tierName + "/bottom"))
                            .texture("particle", GTCEu.id("block/casings/voltage/" + tierName + "/side"));
                    builder.partialState().setModel(model);
                })
                .tooltips(
                        Component.translatable("gtna.machine.infinite_input_bus.tooltip"),
                        Component.translatable("gtceu.part_sharing.disabled"))
                .register();
    }

    private static void registerInfiniteInputHatch(int tier) {
        String tierName = GTValues.VN[tier].toLowerCase(Locale.ROOT);
        INFINITE_INPUT_HATCHES[tier] = REGISTRATE
                .machine("infinite_input_hatch_" + tierName, holder -> new InfiniteInputHatchPartMachine(holder, tier))
                .tier(tier)
                .rotationState(RotationState.ALL)
                .abilities(PartAbility.IMPORT_FLUIDS)
                .modelProperty(IS_FORMED, false)
                .model((ctx, prov, builder) -> {
                    var model = prov.models()
                            .withExistingParent(ctx.getName(), GTCEu.id("block/machine/template/part/hatch_machine"))
                            .texture("overlay", GTCEu.id("block/overlay/machine/overlay_fluid_hatch_input"))
                            .texture("side", GTCEu.id("block/casings/voltage/" + tierName + "/side"))
                            .texture("top", GTCEu.id("block/casings/voltage/" + tierName + "/top"))
                            .texture("bottom", GTCEu.id("block/casings/voltage/" + tierName + "/bottom"))
                            .texture("particle", GTCEu.id("block/casings/voltage/" + tierName + "/side"));
                    builder.partialState().setModel(model);
                })
                .tooltips(
                        Component.translatable("gtna.machine.infinite_input_hatch.tooltip"),
                        Component.translatable("gtceu.part_sharing.disabled"))
                .register();
    }

    private static void registerOutputBoostItemBus(int tier) {
        String tierName = GTValues.VN[tier].toLowerCase(Locale.ROOT);
        int multiplier = OutputBoostHatchPartMachine.getMultiplierForTier(tier);
        OUTPUT_BOOST_ITEM_BUSES[tier] = REGISTRATE
                .machine("output_boost_item_bus_" + tierName, holder -> new OutputBoostItemBusPartMachine(holder, tier))
                .tier(tier)
                .rotationState(RotationState.ALL)
                .abilities(PartAbility.EXPORT_ITEMS)
                .modelProperty(IS_FORMED, false)
                .model((ctx, prov, builder) -> {
                    var model = prov.models()
                            .withExistingParent(ctx.getName(), GTCEu.id("block/machine/template/part/hatch_machine"))
                            .texture("overlay", GTCEu.id("block/overlay/machine/overlay_item_hatch_output"))
                            .texture("side", GTCEu.id("block/casings/voltage/" + tierName + "/side"))
                            .texture("top", GTCEu.id("block/casings/voltage/" + tierName + "/top"))
                            .texture("bottom", GTCEu.id("block/casings/voltage/" + tierName + "/bottom"))
                            .texture("particle", GTCEu.id("block/casings/voltage/" + tierName + "/side"));
                    builder.partialState().setModel(model);
                })
                .tooltips(
                        Component.translatable("gtna.machine.output_boost_bus.tooltip", multiplier),
                        Component.translatable("gtceu.part_sharing.disabled"))
                .register();
    }

    private static void registerOutputBoostFluidHatch(int tier) {
        String tierName = GTValues.VN[tier].toLowerCase(Locale.ROOT);
        int multiplier = OutputBoostHatchPartMachine.getMultiplierForTier(tier);
        OUTPUT_BOOST_FLUID_HATCHES[tier] = REGISTRATE
                .machine("output_boost_fluid_hatch_" + tierName,
                        holder -> new OutputBoostFluidHatchPartMachine(holder, tier))
                .tier(tier)
                .rotationState(RotationState.ALL)
                .abilities(PartAbility.EXPORT_FLUIDS)
                .modelProperty(IS_FORMED, false)
                .model((ctx, prov, builder) -> {
                    var model = prov.models()
                            .withExistingParent(ctx.getName(), GTCEu.id("block/machine/template/part/hatch_machine"))
                            .texture("overlay", GTCEu.id("block/overlay/machine/overlay_fluid_hatch_output"))
                            .texture("side", GTCEu.id("block/casings/voltage/" + tierName + "/side"))
                            .texture("top", GTCEu.id("block/casings/voltage/" + tierName + "/top"))
                            .texture("bottom", GTCEu.id("block/casings/voltage/" + tierName + "/bottom"))
                            .texture("particle", GTCEu.id("block/casings/voltage/" + tierName + "/side"));
                    builder.partialState().setModel(model);
                })
                .tooltips(
                        Component.translatable("gtna.machine.output_boost_hatch.multiplier", multiplier),
                        Component.translatable("gtceu.part_sharing.disabled"))
                .register();
    }

    private static void registerOverclockHatches() {
        int[] tiers = {
                GTValues.UV, GTValues.UHV, GTValues.UEV,
                GTValues.UIV, GTValues.UXV, GTValues.OpV, GTValues.MAX
        };
        for (int i = 0; i < tiers.length; i++) {
            int tier = tiers[i];
            int mkLevel = i + 1;
            String tierName = GTValues.VN[tier].toLowerCase(Locale.ROOT);
            String regName = "overclock_hatch_" + tierName;
            var texturePath = GTNACORE
                    .id("block/machines/overclock_hatch/overclock_hatch_mk" + mkLevel + "/overlay_front");

            ResourceLocation hullSide = GTCEu.id("block/casings/voltage/" + tierName + "/side");
            ResourceLocation hullTop = GTCEu.id("block/casings/voltage/" + tierName + "/top");
            ResourceLocation hullBottom = GTCEu.id("block/casings/voltage/" + tierName + "/bottom");

            double mult = switch (tier) {
                case GTValues.UV -> 55.0;
                case GTValues.UHV -> 33.33;
                case GTValues.UEV -> 25.0;
                case GTValues.UIV -> 20.0;
                case GTValues.UXV -> 16.67;
                case GTValues.OpV -> 14.29;
                case GTValues.MAX -> 12.5;
                default -> 100.0;
            };
            OVERCLOCK_HATCHES[tier] = REGISTRATE
                    .machine(regName, holder -> new OverclockHatchPartMachine(holder, tier))
                    .tier(tier)
                    .rotationState(RotationState.ALL)
                    .abilities(GTNAPartAbility.OVERCLOCK_HATCH)
                    .modelProperty(IS_FORMED, false)
                    .modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE)
                    .model((ctx, prov, builder) -> {
                        String modelName = "block/machines/overclock_hatch/overclock_hatch_" + tierName;
                        var model = prov.models()
                                .withExistingParent(modelName, GTCEu.id("block/machine/template/part/hatch_machine"))
                                .texture("overlay", texturePath)
                                // CORREÇÃO
                                .texture("side", hullSide)
                                .texture("top", hullTop)
                                .texture("bottom", hullBottom)
                                .texture("particle", hullSide);
                        builder.partialState().setModel(model);
                    })
                    .tooltips(
                            Component.translatable("gtna.machine.overclock_hatch.main_function"),
                            Component.translatable("gtna.machine.overclock_hatch.not_installed"),
                            Component.translatable("gtna.machine.overclock_hatch.installed", mult + "%"),
                            Component.translatable("gtna.machine.overclock_hatch.desc"),
                            Component.translatable("gtceu.part_sharing.disabled"))
                    .register();
        }
    }
}
