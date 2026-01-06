package com.raishxn.gtna.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.common.machine.multiblock.part.AccelerateHatchPartMachine;
import com.raishxn.gtna.common.machine.multiblock.part.AdvancedParallelHatchPartMachine;
import net.minecraft.network.chat.Component;

import java.util.Locale;

import static com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties.IS_FORMED;
import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.createWorkableTieredHullMachineModel;
import static com.raishxn.gtna.api.registry.GTNARegistry.REGISTRATE;

public class GTNAMachines2 {

    public static final MachineDefinition[] ADVANCED_PARALLEL_HATCH = new MachineDefinition[GTValues.MAX + 1];
    public static final MachineDefinition[] ACCELERATE_HATCHES = new MachineDefinition[GTValues.MAX + 1];
    public static void init() {
        registerParallelHatch(GTValues.UHV, 1024);
        registerParallelHatch(GTValues.UEV, 4096);
        registerParallelHatch(GTValues.UIV, 16384);
        registerParallelHatch(GTValues.UXV, 65536);
        registerParallelHatch(GTValues.OpV, 262144);
        registerParallelHatch(GTValues.MAX, Integer.MAX_VALUE);
        for (int tier = GTValues.LV; tier <= GTValues.MAX; tier++) {
            registerAccelerateHatch(tier);
        }
    }
    public static final MultiblockMachineDefinition DURATION_TESTER = REGISTRATE
            .multiblock("duration_tester", WorkableElectricMultiblockMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.ASSEMBLER_RECIPES)
            .appearanceBlock(GTBlocks.CASING_STEEL_SOLID)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("CCC", "CCC", "CCC")
                    .aisle("CCC", "C#C", "CCC")
                    .aisle("CCC", "CSC", "CCC")
                    .where('S', Predicates.controller(Predicates.blocks(definition.get())))
                    .where('C', Predicates.blocks(GTBlocks.CASING_STEEL_SOLID.get())
                            .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                            .or(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1))
                            .or(Predicates.abilities(PartAbility.PARALLEL_HATCH).setMaxGlobalLimited(1))
                            .or(Predicates.abilities(PartAbility.MUFFLER).setMaxGlobalLimited(1))
                    )
                    .where('#', Predicates.air())
                    .build())
            .workableCasingModel(
                    GTCEu.id("block/casings/solid/machine_casing_solid_steel"),
                    GTCEu.id("block/multiblock/assembler"))
            .tooltips(Component.literal("§6Machine for testing Duration & Accelerate Hatches"))
            .register();
    private static void registerParallelHatch(int tier, int parallelAmount) {
        String tierName = GTValues.VN[tier].toLowerCase(Locale.ROOT);
        int mkLevel = tier - 8;
        var texturePath = GTNACORE.id("block/machines/parallel_hatch/parallel_hatch_mk" + mkLevel + "/overlay_front");
        ADVANCED_PARALLEL_HATCH[tier] = REGISTRATE
                .machine("parallel_hatch_" + tierName, holder -> new AdvancedParallelHatchPartMachine(holder, tier, parallelAmount))
                .tier(tier)
                .rotationState(RotationState.ALL)
                .abilities(PartAbility.PARALLEL_HATCH)
                .modelProperty(IS_FORMED, false)
                .modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE)
                .model(createWorkableTieredHullMachineModel(texturePath)
                        .andThen((ctx, prov, model) -> {
                            model.addReplaceableTextures("bottom", "top", "side");
                        }))
                .tooltips(
                        Component.translatable("gtna.machine.parallel_hatch.tooltip"),
                        Component.translatable("gtna.machine.parallel_hatch.tier", parallelAmount == Integer.MAX_VALUE ? "Infinite" : parallelAmount),
                        Component.translatable("gtceu.part_sharing.disabled")).register();
    }
    private static void registerAccelerateHatch(int tier) {
        String tierName = GTValues.VN[tier].toLowerCase(Locale.ROOT);
        String regName = "accelerate_hatch_" + tierName;
        int mkLevel = tier;
        var texturePath = GTNACORE.id("block/machines/accelerate_hatch/accelerate_hatch_mk" + mkLevel + "/overlay_front");
        int percentage = 52 - (2 * tier);
        if (percentage < 1) percentage = 1;
        ACCELERATE_HATCHES[tier] = REGISTRATE
                .machine(regName, holder -> new AccelerateHatchPartMachine(holder, tier))
                .tier(tier)
                .rotationState(RotationState.ALL)
                .abilities(PartAbility.IMPORT_ITEMS)
                .modelProperty(IS_FORMED, false)
                .modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE)
                .model(createWorkableTieredHullMachineModel(texturePath)
                        .andThen((ctx, prov, model) -> {
                            model.addReplaceableTextures("bottom", "top", "side");
                        }))
                .tooltips(
                        Component.translatable("gtna.machine.accelerate_hatch.tooltip"),
                        Component.translatable("gtna.machine.accelerate_hatch.desc"),
                        Component.translatable("gtna.machine.accelerate_hatch.amount", percentage + "%"),
                        Component.translatable("gtceu.part_sharing.disabled")
                )
                .register();
    }
}