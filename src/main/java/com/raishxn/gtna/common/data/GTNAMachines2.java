package com.raishxn.gtna.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
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
import com.raishxn.gtna.common.data.multiblock.GTNAMultiBlockFileReader;
import com.raishxn.gtna.common.machine.multiblock.electric.WorkableElectricMultipleRecipesMachine;
import com.raishxn.gtna.common.machine.multiblock.module.steamElevator.SteamApiaryModule;
import com.raishxn.gtna.common.machine.multiblock.module.steamElevator.SteamBeaconModule;
import com.raishxn.gtna.common.machine.multiblock.module.steamElevator.SteamBeeBreedingModule;
import com.raishxn.gtna.common.machine.multiblock.module.steamElevator.SteamEntityCrusherModule;
import com.raishxn.gtna.common.machine.multiblock.module.steamElevator.SteamFlightModule;
import com.raishxn.gtna.common.machine.multiblock.module.steamElevator.SteamGreenhouseModule;
import com.raishxn.gtna.common.machine.multiblock.module.steamElevator.SteamMonsterRepellentModule;
import com.raishxn.gtna.common.machine.multiblock.module.steamElevator.SteamOilDrillModule;
import com.raishxn.gtna.common.machine.multiblock.module.steamElevator.SteamOreProcessorModule;
import com.raishxn.gtna.common.machine.multiblock.module.steamElevator.SteamWeatherModule;
import com.raishxn.gtna.common.machine.multiblock.part.AccelerateHatchPartMachine;
import com.raishxn.gtna.common.machine.multiblock.part.AdvancedParallelHatchPartMachine;
import com.raishxn.gtna.common.machine.multiblock.part.InfiniteInputBusPartMachine;
import com.raishxn.gtna.common.machine.multiblock.part.InfiniteInputHatchPartMachine;
import com.raishxn.gtna.common.machine.multiblock.part.OutputBoostFluidHatchPartMachine;
import com.raishxn.gtna.common.machine.multiblock.part.OutputBoostHatchPartMachine;
import com.raishxn.gtna.common.machine.multiblock.part.OutputBoostItemBusPartMachine;
import com.raishxn.gtna.common.machine.multiblock.part.OverclockHatchPartMachine;
import com.raishxn.gtna.common.machine.multiblock.part.ThreadPartMachine;
import com.raishxn.gtna.common.machine.multiblock.part.ae.GTNACraftPatternPartMachine;
import com.raishxn.gtna.common.machine.multiblock.part.ae.GTNACraftingCPUInterfacePartMachine;
import com.raishxn.gtna.common.machine.multiblock.part.ae.GTNAMEPatternBufferPartMachine;
import com.raishxn.gtna.common.machine.multiblock.part.ae.GTNAMEPatternBufferProxyPartMachine;
import com.raishxn.gtna.common.machine.multiblock.part.ae.GTNAMEStorageAccessPartMachine;
import com.raishxn.gtna.common.machine.tesseract.DirectedTesseractMachine;
import com.raishxn.gtna.config.ConfigHolder;

import java.util.Locale;
import java.util.function.Function;
import java.util.function.Supplier;

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
    public static MachineDefinition ME_PATTERN_BUFFER_PROXY;
    public static MachineDefinition CRAFTING_CPU_INTERFACE;
    public static MachineDefinition ME_STORAGE_ACCESS_HATCH;
    public static MachineDefinition ME_BIG_STORAGE_ACCESS_HATCH;
    public static MachineDefinition ME_IO_PORT_HATCH;
    public static MachineDefinition DIRECTED_TESSERACT_GENERATOR;

    // Steam Elevator modules (GTNL port, LGPLv3)
    public static MachineDefinition STEAM_ELEVATOR_FLIGHT_MODULE;
    public static MachineDefinition STEAM_ELEVATOR_WEATHER_MODULE;
    public static MachineDefinition STEAM_ELEVATOR_GREENHOUSE_MODULE;
    public static MachineDefinition STEAM_ELEVATOR_OIL_DRILL_MODULE_I;
    public static MachineDefinition STEAM_ELEVATOR_OIL_DRILL_MODULE_II;
    public static MachineDefinition STEAM_ELEVATOR_OIL_DRILL_MODULE_III;
    public static MachineDefinition STEAM_ELEVATOR_ENTITY_CRUSHER_MODULE;
    public static MachineDefinition STEAM_ELEVATOR_ORE_PROCESSOR_MODULE;
    public static MachineDefinition STEAM_ELEVATOR_MONSTER_REPELLENT_MODULE_I;
    public static MachineDefinition STEAM_ELEVATOR_MONSTER_REPELLENT_MODULE_II;
    public static MachineDefinition STEAM_ELEVATOR_MONSTER_REPELLENT_MODULE_III;
    public static MachineDefinition STEAM_ELEVATOR_BEACON_MODULE_I;
    public static MachineDefinition STEAM_ELEVATOR_BEACON_MODULE_II;
    public static MachineDefinition STEAM_ELEVATOR_BEACON_MODULE_III;
    public static MachineDefinition STEAM_ELEVATOR_APIARY_MODULE;
    public static MachineDefinition STEAM_ELEVATOR_BEE_BREEDING_MODULE;

    public static void init() {
        registerPatternBuffers();
        registerCraftingCpuInterface();
        registerMEStorageAccessHatches();
        registerDirectedTesseract();
        registerParallelHatch(GTValues.UHV, 1024);
        registerParallelHatch(GTValues.UEV, 4096);
        registerParallelHatch(GTValues.UIV, 16384);
        registerParallelHatch(GTValues.UXV, 65536);
        registerParallelHatch(GTValues.OpV, 262144);
        registerOverclockHatches();
        registerThreadHatches();
        registerSteamElevatorModules();
        for (int i = GTValues.LV; i <= GTValues.MAX; i++) {
            registerAccelerateHatch(i);
            registerOutputBoostHatch(i);
            registerInfiniteInputBus(i);
            registerInfiniteInputHatch(i);
            registerOutputBoostItemBus(i);
            registerOutputBoostFluidHatch(i);
        }
    }

    public static final MultiblockMachineDefinition DURATION_TESTER = registerMachine("durationTester", () -> REGISTRATE
            .multiblock("duration_tester", WorkableElectricMultipleRecipesMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            // Two recipe types so the pattern buffer's automatic mode mirroring is exercisable:
            // a pattern of either type must flip the controller's machine-mode tab on start.
            .recipeType(GTRecipeTypes.ASSEMBLER_RECIPES)
            .recipeType(GTRecipeTypes.CIRCUIT_ASSEMBLER_RECIPES)
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
            .register());

    private static void registerPatternBuffers() {
        ME_MINI_PATTERN_BUFFER = ConfigHolder.isHatchEnabled("meMiniPatternBuffer") ?
                registerPatternBuffer("me_mini_pattern_buffer", GTValues.LuV, 9) : null;
        ME_PATTERN_BUFFER = ConfigHolder.isHatchEnabled("mePatternBuffer") ?
                registerPatternBuffer("me_pattern_buffer", GTValues.ZPM, 21) : null;
        ME_ADVANCED_PATTERN_BUFFER = ConfigHolder.isHatchEnabled("meAdvancedPatternBuffer") ?
                registerPatternBuffer("me_advanced_pattern_buffer", GTValues.UV, 32) : null;
        ME_ULTIMATE_PATTERN_BUFFER = ConfigHolder.isHatchEnabled("meUltimatePatternBuffer") ?
                registerPatternBuffer("me_ultimate_pattern_buffer", GTValues.UHV, 72) : null;
        ME_CRAFT_PATTERN_HATCH = ConfigHolder.isHatchEnabled("meCraftPatternHatch") ? registerCraftPatternHatch() :
                null;
        ME_PATTERN_BUFFER_PROXY = ConfigHolder.isHatchEnabled("mePatternBufferProxy") ?
                registerPatternBufferProxy() : null;
    }

    /**
     * A part that borrows the slot handlers of a distant ME Pattern Buffer. Bind with a data
     * stick (shift-right-click the buffer to store its position, right-click this part to apply).
     */
    private static MachineDefinition registerPatternBufferProxy() {
        return REGISTRATE.machine("me_pattern_buffer_proxy", GTNAMEPatternBufferProxyPartMachine::new)
                .tier(GTValues.UV)
                .rotationState(RotationState.ALL)
                .abilities(
                        PartAbility.IMPORT_ITEMS,
                        PartAbility.IMPORT_FLUIDS,
                        PartAbility.EXPORT_FLUIDS,
                        PartAbility.EXPORT_ITEMS)
                .colorOverlayTieredHullModel(GTCEu.id("block/overlay/appeng/me_buffer_hatch"))
                .tooltips(
                        Component.translatable("gtna.machine.pattern_buffer.proxy.tooltip"),
                        Component.translatable("gtna.machine.pattern_buffer.proxy.binding"),
                        Component.translatable("gtna.machine.pattern_buffer.proxy.range_note"))
                .register();
    }

    private static void registerDirectedTesseract() {
        DIRECTED_TESSERACT_GENERATOR = REGISTRATE
                .machine("directed_tesseract_generator", DirectedTesseractMachine::new)
                .tier(GTValues.IV)
                .rotationState(RotationState.ALL)
                .abilities(
                        PartAbility.IMPORT_ITEMS,
                        PartAbility.IMPORT_FLUIDS,
                        PartAbility.EXPORT_ITEMS,
                        PartAbility.EXPORT_FLUIDS)
                .modelProperty(IS_FORMED, false)
                .model((ctx, prov, builder) -> {
                    var model = prov.models()
                            .withExistingParent(ctx.getName(), GTCEu.id("block/machine/template/part/hatch_machine"))
                            .texture("overlay", GTNACORE.id("block/machines/tesseract_generator/side"))
                            .texture("side", GTNACORE.id("block/machines/tesseract_generator/side"))
                            .texture("top", GTNACORE.id("block/machines/tesseract_generator/top"))
                            .texture("bottom", GTNACORE.id("block/machines/tesseract_generator/top"))
                            .texture("particle", GTNACORE.id("block/machines/tesseract_generator/side"));
                    builder.partialState().setModel(model);
                })
                .tooltips(
                        Component.translatable("gtna.machine.directed_tesseract.tooltip"),
                        Component.translatable("gtna.machine.directed_tesseract.tooltip.assembly"),
                        Component.translatable("gtna.machine.directed_tesseract.tooltip.marker"))
                .register();
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

    private static void registerCraftingCpuInterface() {
        CRAFTING_CPU_INTERFACE = REGISTRATE
                .machine("crafting_cpu_interface", GTNACraftingCPUInterfacePartMachine::new)
                .langValue("Crafting CPU Interface")
                .tier(GTValues.HV)
                .rotationState(RotationState.ALL)
                .colorOverlayTieredHullModel(GTCEu.id("block/overlay/appeng/me_buffer_hatch"))
                .tooltips(
                        Component.translatable("gtna.machine.crafting_cpu_interface.tooltip"),
                        Component.translatable("gtna.machine.crafting_cpu_interface.network"),
                        Component.translatable("gtceu.part_sharing.disabled"))
                .register();
    }

    private static void registerMEStorageAccessHatches() {
        ME_STORAGE_ACCESS_HATCH = ConfigHolder.isHatchEnabled("meStorageAccessHatch") ?
                registerMEStorageAccessHatch(
                        "me_storage_access_hatch",
                        GTValues.EV,
                        GTNAMEStorageAccessPartMachine.Mode.STORAGE,
                        "gtna.machine.me_storage_access_hatch.tooltip") :
                null;
        ME_BIG_STORAGE_ACCESS_HATCH = ConfigHolder.isHatchEnabled("meBigStorageAccessHatch") ?
                registerMEStorageAccessHatch(
                        "me_big_storage_access_hatch",
                        GTValues.IV,
                        GTNAMEStorageAccessPartMachine.Mode.BIG_STORAGE,
                        "gtna.machine.me_big_storage_access_hatch.tooltip") :
                null;
        ME_IO_PORT_HATCH = ConfigHolder.isHatchEnabled("meIOPortHatch") ?
                registerMEStorageAccessHatch(
                        "me_io_port_hatch",
                        GTValues.EV,
                        GTNAMEStorageAccessPartMachine.Mode.IO_PORT,
                        "gtna.machine.me_io_port_hatch.tooltip") :
                null;
    }

    private static MachineDefinition registerMEStorageAccessHatch(
                                                                  String id, int tier,
                                                                  GTNAMEStorageAccessPartMachine.Mode mode,
                                                                  String tooltipKey) {
        return REGISTRATE.machine(id, holder -> new GTNAMEStorageAccessPartMachine(holder, mode))
                .tier(tier)
                .rotationState(RotationState.ALL)
                .abilities(GTNAPartAbility.ME_STORAGE_ACCESS)
                .colorOverlayTieredHullModel(GTCEu.id("block/overlay/appeng/me_buffer_hatch"))
                .tooltips(
                        Component.translatable(tooltipKey),
                        Component.translatable("gtna.machine.me_storage_access_hatch.network"),
                        Component.translatable("gtceu.part_sharing.disabled"))
                .register();
    }

    private static void registerParallelHatch(int tier, int parallelAmount) {
        if (!ConfigHolder.isHatchEnabled("advancedParallelHatches")) return;
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
        if (!ConfigHolder.isHatchEnabled("accelerateHatches")) return;
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
                        Component.translatable("gtna.machine.accelerate_hatch.compat"),
                        Component.translatable("gtceu.part_sharing.disabled"))
                .register();
    }

    private static void registerThreadHatches() {
        if (!ConfigHolder.isHatchEnabled("threadHatches")) return;
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
                            Component.translatable("gtna.machine.thread_hatch.range", threads),
                            Component.translatable("gtna.machine.thread_hatch.requires"),
                            Component.translatable("gtceu.part_sharing.disabled"))
                    .register();
        }
    }

    /**
     * The Steam Elevator modules (GTNL port, LGPLv3). Every module is its own {@code 1x5x2}
     * multiblock (structure {@code pattern/steam_elevator_module.mbs}) rather than a part machine:
     * the elevator scans its fixed module slots and connects only formed module controllers, so a
     * stray block or part in a slot can no longer count as an installed module. Tiers I/II/III
     * variants follow GTNL's registration (Beacon 1/2/3, Repellent 1/2/3, Oil Drill 2/3/4);
     * Flight/Weather are tier 1, Greenhouse tier 5, the Ore Processor and Bee Breeding tier 8 and
     * the Apiary tier 6.
     */
    private static void registerSteamElevatorModules() {
        if (!ConfigHolder.isMachineEnabled("steamElevatorModules")) return;
        // The tooltips are the exact GTNL module tooltip lines (see GTNALangProvider): GTCEu inserts
        // gtna.machine.<id>.tooltip automatically as the first description line, and the remaining
        // GTNL lines are passed here in order. Nothing is emitted twice.
        STEAM_ELEVATOR_FLIGHT_MODULE = registerElevatorModule("steam_elevator_flight_module", "Steam Flight Module", 1,
                holder -> new SteamFlightModule(holder, 1),
                moduleLines("steam_elevator_flight_module", 1, 4));
        STEAM_ELEVATOR_WEATHER_MODULE = registerElevatorModule("steam_elevator_weather_module", "Steam Weather Module",
                1, holder -> new SteamWeatherModule(holder, 1),
                moduleLines("steam_elevator_weather_module", 1, 2));
        STEAM_ELEVATOR_GREENHOUSE_MODULE = registerElevatorModule("steam_elevator_greenhouse_module",
                "Steam Greenhouse Planting Module", 5, holder -> new SteamGreenhouseModule(holder, 5),
                moduleLines("steam_elevator_greenhouse_module", 1, 5));
        STEAM_ELEVATOR_OIL_DRILL_MODULE_I = registerElevatorModule("steam_elevator_oil_drill_module_i",
                "Steam Oil Drill Module I", 2, holder -> new SteamOilDrillModule(holder, 2),
                moduleLines("steam_elevator_oil_drill_module_i", 0, 4));
        STEAM_ELEVATOR_OIL_DRILL_MODULE_II = registerElevatorModule("steam_elevator_oil_drill_module_ii",
                "Steam Oil Drill Module II", 3, holder -> new SteamOilDrillModule(holder, 3),
                moduleLines("steam_elevator_oil_drill_module_ii", 0, 4));
        STEAM_ELEVATOR_OIL_DRILL_MODULE_III = registerElevatorModule("steam_elevator_oil_drill_module_iii",
                "Steam Oil Drill Module III", 4, holder -> new SteamOilDrillModule(holder, 4),
                moduleLines("steam_elevator_oil_drill_module_iii", 0, 4));
        STEAM_ELEVATOR_ENTITY_CRUSHER_MODULE = registerElevatorModule("steam_elevator_entity_crusher_module",
                "Steam Entity Crusher Module", 1, holder -> new SteamEntityCrusherModule(holder, 1),
                moduleLines("steam_elevator_entity_crusher_module", 1, 6));
        STEAM_ELEVATOR_ORE_PROCESSOR_MODULE = registerElevatorModule("steam_elevator_ore_processor_module",
                "Steam Ore Processing Module", 8, holder -> new SteamOreProcessorModule(holder, 8),
                moduleLines("steam_elevator_ore_processor_module", 1, 8));
        STEAM_ELEVATOR_MONSTER_REPELLENT_MODULE_I = registerElevatorModule("steam_elevator_monster_repellent_module_i",
                "Steam Monster Repellator Module I", 1, holder -> new SteamMonsterRepellentModule(holder, 1),
                moduleLines("steam_elevator_monster_repellent_module_i", 0, 3));
        STEAM_ELEVATOR_MONSTER_REPELLENT_MODULE_II = registerElevatorModule(
                "steam_elevator_monster_repellent_module_ii", "Steam Monster Repellator Module II", 2,
                holder -> new SteamMonsterRepellentModule(holder, 2),
                moduleLines("steam_elevator_monster_repellent_module_ii", 0, 3));
        STEAM_ELEVATOR_MONSTER_REPELLENT_MODULE_III = registerElevatorModule(
                "steam_elevator_monster_repellent_module_iii", "Steam Monster Repellator Module III", 3,
                holder -> new SteamMonsterRepellentModule(holder, 3),
                moduleLines("steam_elevator_monster_repellent_module_iii", 0, 3));
        STEAM_ELEVATOR_BEACON_MODULE_I = registerElevatorModule("steam_elevator_beacon_module_i",
                "Steam Elevator Beacon Module I", 1, holder -> new SteamBeaconModule(holder, 1),
                moduleLines("steam_elevator_beacon_module_i", 0, 4));
        STEAM_ELEVATOR_BEACON_MODULE_II = registerElevatorModule("steam_elevator_beacon_module_ii",
                "Steam Elevator Beacon Module II", 2, holder -> new SteamBeaconModule(holder, 2),
                moduleLines("steam_elevator_beacon_module_ii", 0, 4));
        STEAM_ELEVATOR_BEACON_MODULE_III = registerElevatorModule("steam_elevator_beacon_module_iii",
                "Steam Elevator Beacon Module III", 3, holder -> new SteamBeaconModule(holder, 3),
                moduleLines("steam_elevator_beacon_module_iii", 0, 4));
        if (ConfigHolder.isMachineEnabled("steamApiaryModule")) {
            STEAM_ELEVATOR_APIARY_MODULE = registerElevatorModule("steam_elevator_apiary_module",
                    "Steam-Powered Apiary Module", 6, holder -> new SteamApiaryModule(holder, 6),
                    moduleLines("steam_elevator_apiary_module", 1, 5));
        }
        if (ConfigHolder.isMachineEnabled("steamBeeBreedingModule")) {
            STEAM_ELEVATOR_BEE_BREEDING_MODULE = registerElevatorModule("steam_elevator_bee_breeding_module",
                    "Steam Bee Breeding Module", 8, holder -> new SteamBeeBreedingModule(holder, 8),
                    moduleLines("steam_elevator_bee_breeding_module", 1, 5));
        }
    }

    /** The consecutive {@code gtna.machine.<id>.tooltip.<from..to>} lines, in order. */
    private static Component[] moduleLines(String id, int from, int to) {
        Component[] lines = new Component[to - from + 1];
        for (int i = from; i <= to; i++) {
            lines[i - from] = Component.translatable("gtna.machine." + id + ".tooltip." + i);
        }
        return lines;
    }

    private static MachineDefinition registerElevatorModule(String id, String name, int tier,
                                                            Function<IMachineBlockEntity, ? extends MultiblockControllerMachine> factory,
                                                            Component... extraTooltips) {
        return REGISTRATE.multiblock(id, factory)
                .tier(Math.min(tier, GTValues.MAX))
                .rotationState(RotationState.NON_Y_AXIS)
                // Keep the module's local frame aligned with the host's pattern frame.
                .allowExtendedFacing(false)
                .allowFlip(false)
                // No real recipes: the module effects are driven by the host, but a dummy recipe
                // type keeps the definition's recipe-type array non-empty.
                .recipeType(GTRecipeTypes.DUMMY_RECIPES)
                .appearanceBlock(GTBlocks.CASING_STEEL_SOLID)
                .pattern(definition -> GTNAMultiBlockFileReader.start(definition, "steam_elevator_module")
                        .where('~', controller(blocks(definition.get())))
                        // GTNL SteamElevatorModuleBase#getStructureDefinition: the module shell accepts
                        // the steam input hatches (and the usual item/fluid/maintenance parts) chained
                        // with solid steel machine casing, so a steam hatch placed in the module's own
                        // structure is connected and can feed the module's upkeep.
                        .where('A', blocks(GTBlocks.CASING_STEEL_SOLID.get())
                                .or(abilities(PartAbility.STEAM).setMaxGlobalLimited(1))
                                .or(abilities(PartAbility.STEAM_IMPORT_ITEMS).setMaxGlobalLimited(1))
                                .or(abilities(PartAbility.STEAM_EXPORT_ITEMS).setMaxGlobalLimited(1))
                                .or(abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(1))
                                .or(abilities(PartAbility.EXPORT_ITEMS).setMaxGlobalLimited(1))
                                .or(abilities(PartAbility.IMPORT_FLUIDS).setMaxGlobalLimited(1))
                                .or(abilities(PartAbility.EXPORT_FLUIDS).setMaxGlobalLimited(1))
                                .or(abilities(PartAbility.MAINTENANCE).setMaxGlobalLimited(1)))
                        .build())
                .workableCasingModel(
                        GTCEu.id("block/casings/solid/machine_casing_solid_steel"),
                        GTCEu.id("block/multiblock/steam_grinder"))
                // GTCEu's MetaMachineBlock#appendHoverText automatically inserts the
                // gtna.machine.<id>.tooltip line at index 1, so it must NOT be listed here or the
                // description would be printed twice. Only the stats and the part-sharing notice
                // are passed explicitly.
                .tooltips(extraTooltips)
                .tooltips(Component.translatable("gtceu.part_sharing.disabled"))
                .register();
    }

    private static void registerOutputBoostHatch(int tier) {
        if (!ConfigHolder.isHatchEnabled("outputBoostHatches")) return;
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
                .abilities(GTNAPartAbility.OUTPUT_BOOST_HATCH, PartAbility.EXPORT_ITEMS, PartAbility.EXPORT_FLUIDS)
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
        if (!ConfigHolder.isHatchEnabled("infiniteInputBuses")) return;
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
        if (!ConfigHolder.isHatchEnabled("infiniteInputHatches")) return;
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
        if (!ConfigHolder.isHatchEnabled("outputBoostItemBuses")) return;
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
        if (!ConfigHolder.isHatchEnabled("outputBoostFluidHatches")) return;
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
        if (!ConfigHolder.isHatchEnabled("overclockHatches")) return;
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
                case GTValues.UV -> 50.0;
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
                            Component.translatable("gtna.machine.overclock_hatch.note"),
                            Component.translatable("gtceu.part_sharing.disabled"))
                    .register();
        }
    }

    private static <T extends MachineDefinition> T registerMachine(String machineId, Supplier<T> supplier) {
        return ConfigHolder.isMachineEnabled(machineId) ? supplier.get() : null;
    }
}
