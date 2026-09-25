package com.raishxn.gtna.gametest;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.MultiblockState;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntCircuitIngredient;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.machines.GCYMMachines;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;
import com.gregtechceu.gtceu.common.machine.multiblock.part.EnergyHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.CalculationStrategy;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.crafting.ICraftingSimulationRequester;
import appeng.api.networking.crafting.ICraftingSubmitResult;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.StorageHelper;
import appeng.blockentity.crafting.CraftingBlockEntity;
import appeng.blockentity.storage.DriveBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.crafting.execution.CraftingCpuLogic;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.me.helpers.BaseActionSource;
import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.api.capability.SteamWirelessNetworkManager;
import com.raishxn.gtna.api.machine.multiblock.GTNAPartAbility;
import com.raishxn.gtna.api.machine.multiblock.GTNAPatternDiagnostics;
import com.raishxn.gtna.api.machine.multiblock.GTNASubPatterns;
import com.raishxn.gtna.common.WirelessSteamHudSync;
import com.raishxn.gtna.common.data.GTNABlocks;
import com.raishxn.gtna.common.data.GTNAMachines;
import com.raishxn.gtna.common.data.GTNAMachines2;
import com.raishxn.gtna.common.data.GTNAMachines3;
import com.raishxn.gtna.common.data.GTNARecipeType;
import com.raishxn.gtna.common.data.NexusEnergyNetwork;
import com.raishxn.gtna.common.data.multiblock.GTOCompressedPatternReader;
import com.raishxn.gtna.common.machine.multiMachineBase.SteamMultiMachineBase;
import com.raishxn.gtna.common.machine.multiblock.electric.GreenhouseMachine;
import com.raishxn.gtna.common.machine.multiblock.electric.LiquefactionFurnaceMachine;
import com.raishxn.gtna.common.machine.multiblock.electric.UniversalFactoryMachine;
import com.raishxn.gtna.common.machine.multiblock.electric.WorkableElectricMultipleRecipesMachine;
import com.raishxn.gtna.common.machine.multiblock.module.steamElevator.SteamOreProcessorModule;
import com.raishxn.gtna.common.machine.multiblock.noenergy.BrickKilnMachine;
import com.raishxn.gtna.common.machine.multiblock.noenergy.PrimitiveStoneFurnaceMachine;
import com.raishxn.gtna.common.machine.multiblock.noenergy.ThermalPowerPumpMachine;
import com.raishxn.gtna.common.machine.multiblock.part.OutputBoostHatchPartMachine;
import com.raishxn.gtna.common.machine.multiblock.part.OverclockHatchPartMachine;
import com.raishxn.gtna.common.machine.multiblock.part.ae.GTNACraftingCPUInterfacePartMachine;
import com.raishxn.gtna.common.machine.multiblock.part.ae.GTNAMEPatternBufferPartMachine;
import com.raishxn.gtna.common.machine.multiblock.part.steam.WirelessSteamInputHatch;
import com.raishxn.gtna.common.machine.multiblock.part.steam.WirelessSteamOutputHatch;
import com.raishxn.gtna.common.machine.multiblock.steam.AdjustableSteamParallelMachine;
import com.raishxn.gtna.common.machine.multiblock.steam.SteamItemVaultMachine;
import com.raishxn.gtna.common.machine.multiblock.steam.SteamLavaMakerMachine;
import com.raishxn.gtna.common.machine.trait.GTNAMultipleRecipesLogic;
import com.raishxn.gtna.network.packet.SWirelessSteamStats;
import com.raishxn.gtna.utils.datastructure.Int128;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * In-game tests (Forge GameTest) for the parts the plain unit tests cannot reach: machine
 * registration, block-entity wiring and the pattern buffer's mode handling.
 *
 * <p>
 * Run headlessly with {@code ./gradlew runGameTestServer}; structures come from
 * {@code src/main/resources/data/gtna/structures/*.nbt}. The build only enables this namespace via
 * {@code forge.enabledGameTestNamespaces}, so the classes are inert in normal play.
 *
 * <p>
 * What is covered here and why:
 * <ul>
 * <li>{@code durationTesterExposesTwoRecipeTypes} locks the precondition of the automatic machine
 * mode switch: with a single recipe type {@code gtna$resolvePatternBufferMode} returns {@code null}
 * and the whole feature silently becomes a no-op (that is exactly how it stayed invisible).</li>
 * <li>{@code bufferModeFilterGatesSlotAcceptance} exercises the buffer-level mode filter
 * (GTOCore {@code MultiMachineModeFancyConfigurator} parity) end to end, on a real recipe of a real
 * recipe type: pinned to its own type the buffer serves the recipe, pinned to another type it must
 * refuse, and clearing the filter must restore acceptance.</li>
 * <li>{@code durationTesterControllerCanBePlaced} is the harness smoke test: the definition can be
 * placed and the block entity is our machine class.</li>
 * </ul>
 */
@PrefixGameTestTemplate(false)
@GameTestHolder("gtna")
public final class GTNAMachineGameTests {

    /**
     * Empty 12x12x12 area. Deliberately larger than any single structure: the two structure tests
     * build in <b>disjoint quadrants</b> (origin 2,2,2 and 8,2,8) so that even if the framework runs
     * them against the same area or leaves blocks behind between runs, neither can contaminate the
     * other's pattern match.
     */
    private static final String TEMPLATE = "empty_12";
    /** Half-extent of the box wiped around a structure before building it. */
    private static final int WIPE_RADIUS = 3;

    private GTNAMachineGameTests() {}

    @GameTest(template = "empty_16", timeoutTicks = 20)
    public static void largeCutterStructureDiagnosticUsesControllerAnchor(GameTestHelper helper) {
        assertAnchoredDiagnostic(helper, GCYMMachines.LARGE_CUTTER, 3);
    }

    @GameTest(template = "empty_16", timeoutTicks = 20)
    public static void largeMaterialPressStructureDiagnosticUsesControllerAnchor(GameTestHelper helper) {
        assertAnchoredDiagnostic(helper, GCYMMachines.LARGE_MATERIAL_PRESS, 2);
    }

    private static void assertAnchoredDiagnostic(GameTestHelper helper, MultiblockMachineDefinition definition,
                                                 int aisleOffset) {
        BlockPos origin = new BlockPos(8, 5, 8);
        helper.setBlock(origin, definition.getBlock());
        if (!(metaMachineAt(helper, origin) instanceof WorkableElectricMultiblockMachine machine)) {
            helper.fail("controller did not instantiate: " + definition.getId());
            return;
        }
        machine.setFrontFacing(Direction.NORTH);
        GTNAPatternDiagnostics.Mismatch north = GTNAPatternDiagnostics.firstMismatch(machine, machine.getPattern());
        helper.assertTrue(north != null && north.pos().equals(helper.absolutePos(origin.offset(1, -1, aisleOffset))),
                "north diagnostic must point at first missing casing, not a search probe: " + north);
        machine.setFrontFacing(Direction.EAST);
        GTNAPatternDiagnostics.Mismatch east = GTNAPatternDiagnostics.firstMismatch(machine, machine.getPattern());
        helper.assertTrue(east != null && east.pos().equals(helper.absolutePos(origin.offset(-aisleOffset, -1, 1))),
                "east diagnostic must rotate the missing casing with the controller: " + east);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 20)
    public static void durationTesterExposesTwoRecipeTypes(GameTestHelper helper) {
        MultiblockMachineDefinition definition = GTNAMachines2.DURATION_TESTER;
        if (definition == null) {
            helper.fail("duration_tester is disabled by config; the mode-switch tests cannot run");
            return;
        }
        GTRecipeType[] types = definition.getRecipeTypes();
        helper.assertTrue(types.length == 2,
                "duration_tester must expose exactly 2 recipe types for the mode switch to be " +
                        "observable, but has " + types.length);
        helper.assertTrue(types[0] == GTRecipeTypes.ASSEMBLER_RECIPES,
                "first recipe type must be assembler, got " + types[0]);
        helper.assertTrue(types[1] == GTRecipeTypes.CIRCUIT_ASSEMBLER_RECIPES,
                "second recipe type must be circuit assembler, got " + types[1]);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 20)
    public static void durationTesterControllerCanBePlaced(GameTestHelper helper) {
        MultiblockMachineDefinition definition = GTNAMachines2.DURATION_TESTER;
        if (definition == null) {
            helper.fail("duration_tester is disabled by config");
            return;
        }
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, definition.get().self());
        BlockEntity placed = helper.getBlockEntity(pos);
        helper.assertTrue(placed instanceof MetaMachineBlockEntity holder &&
                holder.getMetaMachine() instanceof WorkableElectricMultipleRecipesMachine,
                "placing duration_tester must create a WorkableElectricMultipleRecipesMachine, got " + placed);
        helper.succeed();
    }

    /**
     * Locks the GTOCore overclock-hatch semantics end to end: the hatch stores an integer duration
     * divisor (not a rounded percentage), the MAX hatch defaults to the tier's best divisor (8), the
     * machine's overclock logic uses exactly that divisor, and dialing it back to 2 restores the
     * standard 0.5 overclock.
     */
    @GameTest(template = TEMPLATE, timeoutTicks = 40)
    public static void overclockHatchUsesIntegerDivisor(GameTestHelper helper) {
        if (GTNAMachines2.DURATION_TESTER == null ||
                GTNAMachines2.OVERCLOCK_HATCHES[GTValues.MAX] == null) {
            helper.fail("duration_tester and the overclock hatches must be enabled");
            return;
        }
        BlockPos controllerPos = new BlockPos(8, 2, 8);
        clearArea(helper, controllerPos);
        buildDurationTester(helper, controllerPos);
        BlockPos hatchPos = controllerPos.offset(0, 1, 2);
        helper.setBlock(hatchPos, GTNAMachines2.OVERCLOCK_HATCHES[GTValues.MAX].getBlock());
        if (!(metaMachineAt(helper, controllerPos) instanceof WorkableElectricMultipleRecipesMachine controller)) {
            helper.fail("duration_tester must be a WorkableElectricMultipleRecipesMachine");
            return;
        }
        MultiblockState state = controller.getMultiblockState();
        helper.assertTrue(controller.getPattern().checkPatternAt(state, false),
                "duration_tester pattern did not match: " + patternError(helper, state, controllerPos));
        controller.onStructureFormed();
        if (!(metaMachineAt(helper, hatchPos) instanceof OverclockHatchPartMachine hatch)) {
            helper.fail("the overclock hatch did not instantiate");
            return;
        }
        // MAX tier caps the divisor at 8 and defaults to it.
        helper.assertTrue(hatch.getOverclockDivisor() == 8,
                "the MAX overclock hatch must default to divisor 8, got " + hatch.getOverclockDivisor());
        helper.assertTrue(Math.abs(hatch.getOverclockMultiplier() - 1.0 / 8.0) < 1e-9,
                "divisor 8 must be exactly one eighth, got " + hatch.getOverclockMultiplier());
        helper.assertTrue(Math.abs(controller.getOverclockDurationFactor() - 1.0 / 8.0) < 1e-9,
                "the machine must use the hatch divisor, got " + controller.getOverclockDurationFactor());
        // Dialing the divisor back to 2 restores the standard overclock, and 1 clamps up to 2.
        hatch.setCurrentAmount(2);
        helper.assertTrue(hatch.getOverclockDivisor() == 2,
                "divisor 2 must be accepted, got " + hatch.getOverclockDivisor());
        helper.assertTrue(Math.abs(controller.getOverclockDurationFactor() - 0.5) < 1e-9,
                "divisor 2 must restore the standard 0.5 factor, got " +
                        controller.getOverclockDurationFactor());
        hatch.setCurrentAmount(1);
        helper.assertTrue(hatch.getOverclockDivisor() == 2,
                "divisor 1 must clamp up to 2, got " + hatch.getOverclockDivisor());
        helper.succeed();
    }

    /**
     * Locks the GTLCore-parity of the integrated ore processing recipes (G-0054): one recipe per
     * circuit 1..7 for both the raw-ore and the stone-ore input, with the real per-stage byproducts
     * and the material's own washing fluid. The recipe type is a real registered type, so this runs
     * against the loaded recipe manager.
     */
    @GameTest(template = TEMPLATE, timeoutTicks = 40)
    public static void integratedOreProcessingIsFaithful(GameTestHelper helper) {
        var recipes = helper.getLevel().getRecipeManager().getAllRecipesFor(GTNARecipeType.ORE_PROCESSING_RECIPES);
        helper.assertTrue(recipes.size() >= 700,
                "expected the integrated ore processing recipes to be loaded, got " + recipes.size());

        boolean raw = false;
        boolean stone = false;
        boolean distilledWater = false;
        boolean mercury = false;
        boolean circuitOneHasFluid = false;
        boolean outputTooSmall = false;
        boolean[] circuitSeen = new boolean[8];
        for (GTRecipe recipe : recipes) {
            if (recipe.id.getPath().contains("_raw_")) raw = true;
            else stone = true;

            int circuit = circuitOf(recipe);
            if (circuit >= 1 && circuit <= 7) circuitSeen[circuit] = true;

            List<Content> fluidInputs = recipe.getInputContents(FluidRecipeCapability.CAP);
            if (circuit == 1 && !fluidInputs.isEmpty()) circuitOneHasFluid = true;
            for (Content content : fluidInputs) {
                for (Fluid fluid : fluidsOf(content)) {
                    if (fluid == GTMaterials.DistilledWater.getFluid()) distilledWater = true;
                    if (fluid == GTMaterials.Mercury.getFluid()) mercury = true;
                }
            }

            // The base dust plus at least one byproduct; a lone output means the chain was simplified.
            if (recipe.getOutputContents(ItemRecipeCapability.CAP).size() < 2) outputTooSmall = true;
        }

        helper.assertTrue(raw, "the raw-ore variants must be generated");
        helper.assertTrue(stone, "the stone-ore variants must be generated");
        for (int circuit = 1; circuit <= 7; circuit++) {
            helper.assertTrue(circuitSeen[circuit], "circuit " + circuit + " recipes are missing");
        }
        helper.assertTrue(distilledWater, "circuits 2/3/4 must wash in distilled water");
        helper.assertTrue(mercury, "materials washed in mercury (e.g. cooperite) must use mercury");
        helper.assertFalse(circuitOneHasFluid, "circuit 1 (grind-grind-centrifuge) must not take a fluid");
        helper.assertFalse(outputTooSmall, "every integrated recipe must output the product plus byproducts");
        helper.succeed();
    }

    private static int circuitOf(GTRecipe recipe) {
        for (Content content : recipe.getInputContents(ItemRecipeCapability.CAP)) {
            if (content.content instanceof IntCircuitIngredient circuit) {
                ItemStack[] items = circuit.getItems();
                if (items.length > 0) {
                    return IntCircuitBehaviour.getCircuitConfiguration(items[0]);
                }
            }
        }
        return -1;
    }

    private static List<Fluid> fluidsOf(Content content) {
        if (content.content instanceof FluidIngredient ingredient) {
            List<Fluid> fluids = new ArrayList<>();
            for (FluidIngredient.Value value : ingredient.values) {
                fluids.addAll(value.getFluids());
            }
            return fluids;
        }
        return List.of();
    }

    /**
     * Regression for the ore processor module returning the raw ore unchanged: the module's chain
     * lookup must find GTCEu's recipes for a raw ore. The bug was passing {@code Ingredient.of(stack)}
     * to {@code db().find} — ore recipes are tag-based and only the {@code ItemStack} lookup expands
     * the item's tags into the {@code ItemTagMapIngredient} the recipe DB indexes on.
     */
    @GameTest(template = TEMPLATE, timeoutTicks = 20)
    public static void oreProcessorModuleRefinesRawOre(GameTestHelper helper) {
        ItemStack rawGold = ChemicalHelper.get(TagPrefix.rawOre, GTMaterials.Gold);
        helper.assertFalse(rawGold.isEmpty(), "raw gold must exist for the refine test");
        List<ItemStack> outputs = SteamOreProcessorModule.refine(rawGold, 2);
        helper.assertFalse(outputs.isEmpty(), "the ore processor chain must produce output for raw gold");
        helper.assertFalse(outputs.size() == 1 && ItemStack.isSameItem(outputs.get(0), rawGold),
                "the module must not return the raw ore unchanged: " + outputs);
        boolean goldDust = outputs.stream()
                .anyMatch(s -> ItemStack.isSameItem(s, ChemicalHelper.get(TagPrefix.dust, GTMaterials.Gold)));
        helper.assertTrue(goldDust, "circuit 2 of raw gold must end in gold dust, got " + outputs);

        FluidStack circuit1 = SteamOreProcessorModule.requiredFluidFor(rawGold, 1);
        helper.assertTrue(circuit1.isEmpty(), "circuit 1 must not require a fluid, got " + circuit1);
        FluidStack circuit2 = SteamOreProcessorModule.requiredFluidFor(rawGold, 2);
        helper.assertFalse(circuit2.isEmpty(), "circuit 2 of raw gold must require distilled water (entry=" +
                ChemicalHelper.getMaterialEntry(rawGold.getItem()) + ")");
        helper.assertTrue(circuit2.getFluid() == GTMaterials.DistilledWater.getFluid(),
                "circuit 2 of raw gold must require distilled water, got " + circuit2);
        helper.succeed();
    }

    /** The Generator Array must form with all four required hatches in the GTOCore 3x3x3 shell. */
    @GameTest(template = "empty_16", timeoutTicks = 40)
    public static void generatorArrayForms(GameTestHelper helper) {
        BlockPos controllerPos = new BlockPos(4, 2, 4);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = 0; dy <= 2; dy++) {
                for (int dz = 0; dz <= 2; dz++) {
                    helper.setBlock(controllerPos.offset(dx, dy, dz), Blocks.AIR);
                }
            }
        }
        helper.setBlock(controllerPos, GTNAMachines3.GENERATOR_ARRAY.getBlock());
        String[][] aisles = {
                { "XXX", "CCC", "XXX" },
                { "XXX", "C#C", "XXX" },
                { "XSX", "CCC", "XXX" }
        };
        for (int aisle = 0; aisle < aisles.length; aisle++) {
            for (int y = 0; y < 3; y++) {
                for (int x = 0; x < 3; x++) {
                    BlockPos pos = controllerPos.offset(1 - x, y, 2 - aisle);
                    switch (aisles[aisle][y].charAt(x)) {
                        case 'X' -> helper.setBlock(pos, GTBlocks.CASING_STEEL_SOLID.get());
                        case 'C' -> helper.setBlock(pos, GTBlocks.CASING_TEMPERED_GLASS.get());
                        case 'S' -> {}
                        default -> {}
                    }
                }
            }
        }
        helper.setBlock(controllerPos.offset(1, 0, 0), GTMachines.ITEM_IMPORT_BUS[GTValues.LV].getBlock());
        helper.setBlock(controllerPos.offset(-1, 0, 0), GTMachines.FLUID_IMPORT_HATCH[GTValues.LV].getBlock());
        helper.setBlock(controllerPos.offset(1, 2, 0), GTMachines.ENERGY_OUTPUT_HATCH[GTValues.MV].getBlock());
        helper.setBlock(controllerPos.offset(-1, 2, 0), GTMachines.MAINTENANCE_HATCH.getBlock());

        MetaMachine machine = metaMachineAt(helper, controllerPos);
        if (!(machine instanceof com.raishxn.gtna.common.machine.multiblock.energy.GeneratorArrayMachine array)) {
            helper.fail("generator_array controller is missing: " + machine);
            return;
        }
        MultiblockState state = array.getMultiblockState();
        if (!array.getPattern().checkPatternAt(state, false)) {
            helper.fail("generator_array pattern did not match: " + patternError(helper, state, controllerPos));
            return;
        }
        array.onStructureFormed();
        helper.assertTrue(array.isFormed(), "generator_array must form");
        ItemStack fourTurbines = GTMachines.STEAM_TURBINE[GTValues.LV].asStack().copyWithCount(5);
        ItemStack excess = array.getGeneratorStorage().insertItem(0, fourTurbines, false);
        helper.assertTrue(excess.getCount() == 1 && array.getInstalledGeneratorCount() == 4,
                "controller slot must accept at most four matching generators");
        helper.assertTrue(array.getGeneratorStorage().insertItem(0, GTMachines.MACERATOR[GTValues.LV].asStack(),
                false).getCount() == 1, "controller slot must reject non-generators");
        array.getGeneratorStorage().storage.extractItem(0, 4, false);
        ItemBusPartMachine inputBus = (ItemBusPartMachine) metaMachineAt(helper, controllerPos.offset(1, 0, 0));
        inputBus.getInventory().insertItem(0, GTMachines.STEAM_TURBINE[GTValues.LV].asStack(), false);
        helper.assertTrue(array.getInstalledGeneratorCount() == 1,
                "one installed steam turbine must be detected as the generator catalyst");
        FluidHatchPartMachine fluidHatch = (FluidHatchPartMachine) metaMachineAt(helper,
                controllerPos.offset(-1, 0, 0));
        fluidHatch.tank.setFluidInTank(0, GTMaterials.Steam.getFluid(6400));
        EnergyHatchPartMachine outputHatch = (EnergyHatchPartMachine) metaMachineAt(helper,
                controllerPos.offset(1, 2, 0));
        var maintenance = (com.gregtechceu.gtceu.common.machine.multiblock.part.MaintenanceHatchPartMachine) metaMachineAt(
                helper, controllerPos.offset(-1, 2, 0));
        maintenance.fixAllMaintenanceProblems();
        array.refreshGeneratorMode();
        var fuel = array.getRecipeLogic().searchRecipe();
        helper.assertTrue(fuel.hasNext(), "steam fuel recipe must be indexed");
        var fuelRecipe = fuel.next();
        var modifier = com.raishxn.gtna.common.machine.multiblock.energy.GeneratorArrayMachine.recipeModifier(array,
                fuelRecipe);
        helper.assertTrue(modifier.apply(fuelRecipe) != null,
                "array must accept indexed steam fuel recipe: " + fuelRecipe);
        helper.assertTrue(array.getDefinition().getRecipeModifier().applyModifier(array, fuelRecipe) != null,
                "registered array modifier must accept steam fuel");
        helper.assertTrue(array.fullModifyRecipe(fuelRecipe) != null,
                "array must retain fuel after full recipe modification");
        helper.assertTrue(array.getRecipeLogic().checkMatchedRecipeAvailable(fuelRecipe),
                "array must start an available steam fuel recipe (failures=" +
                        array.getRecipeLogic().getFailureReasons() + ")");
        array.getRecipeLogic().updateTickSubscription();
        for (int tick = 0; tick < 30; tick++) {
            array.getRecipeLogic().serverTick();
        }
        helper.assertTrue(outputHatch.energyContainer.getEnergyStored() > 0,
                "steam turbine fuel must generate energy in the dynamo hatch (status=" +
                        array.getRecipeLogic().getStatus() + ", lastRecipe=" +
                        array.getRecipeLogic().getLastRecipe() + ", steam=" +
                        fluidHatch.tank.getFluidInTank(0).getAmount() + ", candidates=" +
                        array.getRecipeLogic().searchRecipe().hasNext() + ", failures=" +
                        array.getRecipeLogic().getFailureReasons() + ")");
        helper.succeed();
    }

    /** GTOCore's compressed 13x4x13 fishing ground must retain its water and frame geometry. */
    @GameTest(template = "empty_16", timeoutTicks = 60)
    public static void fishingGroundForms(GameTestHelper helper) {
        BlockPos controllerPos = new BlockPos(7, 2, 1);
        var source = GTOCompressedPatternReader.read("fishing_ground");
        helper.assertTrue(source.slices().length == 13 && source.slices()[0].length == 4,
                "fishing ground must retain GTOCore's 13x4x13 shape");
        helper.setBlock(controllerPos, GTNAMachines3.FISHING_GROUND.getBlock());
        for (int aisle = 0; aisle < source.slices().length; aisle++) {
            for (int row = 0; row < source.slices()[aisle].length; row++) {
                String line = source.slices()[aisle][row];
                for (int column = 0; column < line.length(); column++) {
                    BlockPos pos = controllerPos.offset(column - 6, row - 1, aisle);
                    switch (line.charAt(column)) {
                        case 'A', 'B' -> helper.setBlock(pos, GTNABlocks.ALUMINIUM_BRONZE_CASING.get());
                        case 'C' -> helper.setBlock(pos, Blocks.WATER);
                        case 'D' -> helper.setBlock(pos, GTBlocks.CASING_STEEL_PIPE.get());
                        case 'E' -> helper.setBlock(pos,
                                ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.StainlessSteel));
                        case 'F' -> {}
                        default -> helper.setBlock(pos, Blocks.AIR);
                    }
                }
            }
        }
        helper.setBlock(controllerPos.offset(-2, -1, 0), GTMachines.ITEM_IMPORT_BUS[GTValues.HV].getBlock());
        helper.setBlock(controllerPos.offset(-1, -1, 0), GTMachines.ITEM_EXPORT_BUS[GTValues.HV].getBlock());
        helper.setBlock(controllerPos.offset(1, -1, 0), GTMachines.ENERGY_INPUT_HATCH[GTValues.HV].getBlock());
        helper.setBlock(controllerPos.offset(2, -1, 0), GTMachines.MAINTENANCE_HATCH.getBlock());
        MetaMachine machine = metaMachineAt(helper, controllerPos);
        if (!(machine instanceof WorkableElectricMultiblockMachine fishingGround)) {
            helper.fail("fishing ground controller is missing: " + machine);
            return;
        }
        MultiblockState state = fishingGround.getMultiblockState();
        if (!fishingGround.getPattern().checkPatternAt(state, false)) {
            helper.fail("fishing ground pattern did not match: " + patternError(helper, state, controllerPos));
            return;
        }
        fishingGround.onStructureFormed();
        helper.assertTrue(fishingGround.isFormed(), "fishing ground must form");
        ItemBusPartMachine inputBus = (ItemBusPartMachine) metaMachineAt(helper,
                controllerPos.offset(-2, -1, 0));
        ItemBusPartMachine outputBus = (ItemBusPartMachine) metaMachineAt(helper,
                controllerPos.offset(-1, -1, 0));
        EnergyHatchPartMachine energyHatch = (EnergyHatchPartMachine) metaMachineAt(helper,
                controllerPos.offset(1, -1, 0));
        var maintenance = (com.gregtechceu.gtceu.common.machine.multiblock.part.MaintenanceHatchPartMachine) metaMachineAt(
                helper, controllerPos.offset(2, -1, 0));
        maintenance.fixAllMaintenanceProblems();
        energyHatch.energyContainer.changeEnergy(1_000_000);
        inputBus.getInventory().insertItem(0, new ItemStack(Items.COD, 64), false);
        inputBus.getInventory().insertItem(1,
                ChemicalHelper.get(TagPrefix.dustTiny, GTMaterials.Meat, 64), false);
        fishingGround.getRecipeLogic().updateTickSubscription();
        for (int tick = 0; tick < 2_100; tick++) fishingGround.getRecipeLogic().serverTick();
        int caught = 0;
        for (int slot = 0; slot < outputBus.getInventory().getSlots(); slot++) {
            ItemStack stack = outputBus.getInventory().getStackInSlot(slot);
            if (stack.is(Items.COD)) caught += stack.getCount();
        }
        helper.assertTrue(caught >= 32,
                "bait recipe must produce at least 32 cod (caught=" + caught + ", status=" +
                        fishingGround.getRecipeLogic().getStatus() + ")");
        inputBus.getInventory().setStackInSlot(0, IntCircuitBehaviour.stack(2));
        inputBus.getInventory().setStackInSlot(1, ItemStack.EMPTY);
        var fishingController = (com.raishxn.gtna.common.machine.multiblock.electric.FishingGroundMachine) fishingGround;
        helper.assertTrue(fishingController.getCircuitMode() == 2,
                "input bus must expose fishing circuit 2 (slot=" +
                        inputBus.getInventory().getStackInSlot(0) + ", formed=" + fishingGround.isFormed() + ")");
        int beforeLoot = 0;
        for (int slot = 0; slot < outputBus.getInventory().getSlots(); slot++) {
            beforeLoot += outputBus.getInventory().getStackInSlot(slot).getCount();
        }
        var lootCandidates = fishingGround.getRecipeLogic().searchRecipe();
        helper.assertTrue(lootCandidates.hasNext(), "circuit 2 must select the vanilla fishing fish loot table");
        var selectedLoot = lootCandidates.next();
        helper.assertTrue(selectedLoot.id.getPath().endsWith("/fishing_loot_2"),
                "circuit 2 must choose the fishing fish loot recipe: " + selectedLoot.id);
        fishingGround.getRecipeLogic().resetRecipeLogic();
        for (int tick = 0; tick < 30; tick++) {
            energyHatch.energyContainer.changeEnergy(10_000);
            fishingGround.getRecipeLogic().serverTick();
        }
        int afterLoot = 0;
        for (int slot = 0; slot < outputBus.getInventory().getSlots(); slot++) {
            afterLoot += outputBus.getInventory().getStackInSlot(slot).getCount();
        }
        helper.assertTrue(afterLoot > beforeLoot,
                "circuit 2 must produce vanilla fish loot (before=" + beforeLoot + ", after=" + afterLoot +
                        ", status=" + fishingGround.getRecipeLogic().getStatus() + ", reason=" +
                        fishingGround.getRecipeLogic().getFancyTooltip() + ", progress=" +
                        fishingGround.getRecipeLogic().getProgress() + ", recipe=" +
                        fishingGround.getRecipeLogic().getLastRecipe() + ", energy=" +
                        energyHatch.energyContainer.getEnergyStored() + ")");
        helper.succeed();
    }

    /** GTOCore's eight-layer column must form and turn water into salt water. */
    @GameTest(template = "empty_16", timeoutTicks = 40)
    public static void evaporationPlantForms(GameTestHelper helper) {
        BlockPos controllerPos = new BlockPos(4, 2, 4);
        String[][] bottom = { { "FYF", "YYY", "FYF" }, { "YSY", "Y#Y", "YYY" } };
        helper.setBlock(controllerPos, GTNAMachines3.EVAPORATION_PLANT.getBlock());
        for (int aisle = 0; aisle < 8; aisle++) {
            String[] rows = aisle < 2 ? bottom[aisle] :
                    aisle == 7 ? new String[] { " Z ", "ZZZ", " Z " } :
                            new String[] { "XXX", "X#X", "XXX" };
            for (int row = 0; row < 3; row++) {
                for (int column = 0; column < 3; column++) {
                    BlockPos pos = controllerPos.offset(column - 1, aisle - 1, row);
                    switch (rows[row].charAt(column)) {
                        case 'Y', 'X', 'Z' -> helper.setBlock(pos,
                                GTNABlocks.STAINLESS_EVAPORATION_CASING.get());
                        case 'F' -> helper.setBlock(pos,
                                ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.Aluminium));
                        case '#', ' ' -> helper.setBlock(pos, Blocks.AIR);
                        case 'S' -> {}
                        default -> helper.fail("unexpected evaporation symbol");
                    }
                }
            }
        }
        helper.setBlock(controllerPos.offset(0, -1, 1), GTMachines.FLUID_IMPORT_HATCH[GTValues.HV].getBlock());
        helper.setBlock(controllerPos.offset(0, -1, 0), GTMachines.ENERGY_INPUT_HATCH[GTValues.HV].getBlock());
        helper.setBlock(controllerPos.offset(0, 1, 0), GTMachines.FLUID_EXPORT_HATCH[GTValues.HV].getBlock());
        MetaMachine machine = metaMachineAt(helper, controllerPos);
        if (!(machine instanceof WorkableElectricMultiblockMachine plant)) {
            helper.fail("evaporation plant controller is missing: " + machine);
            return;
        }
        MultiblockState state = plant.getMultiblockState();
        if (!plant.getPattern().checkPatternAt(state, false)) {
            helper.fail("evaporation plant pattern did not match: " + patternError(helper, state, controllerPos));
            return;
        }
        BlockPos fluidInputPos = controllerPos.offset(0, -1, 1);
        helper.setBlock(fluidInputPos, GTMachines.STEAM_HATCH.getBlock());
        helper.assertTrue(!plant.getPattern().checkPatternAt(state, false),
                "evaporation plant must reject the standard steam input hatch");
        helper.assertTrue(GTNAMachines.WIRELESS_STEAM_INPUT_HATCH != null,
                "wireless steam input hatch must be registered for the evaporation regression test");
        for (var steamInput : new com.gregtechceu.gtceu.api.machine.MachineDefinition[] {
                GTNAMachines.WIRELESS_STEAM_INPUT_HATCH, GTNAMachines.WIRELESS_STEAM_INPUT_HATCH_STEEL }) {
            helper.assertTrue(PartAbility.STEAM.isApplicable(steamInput.getBlock()),
                    "wireless steam input must retain its steam ability");
            helper.assertTrue(!PartAbility.IMPORT_FLUIDS.isApplicable(steamInput.getBlock()),
                    "wireless steam input must not be a universal fluid hatch");
        }
        for (var steamOutput : new com.gregtechceu.gtceu.api.machine.MachineDefinition[] {
                GTNAMachines.WIRELESS_STEAM_OUTPUT_HATCH, GTNAMachines.WIRELESS_STEAM_OUTPUT_HATCH_STEEL }) {
            helper.assertTrue(GTNAPartAbility.STEAM_EXPORT_FLUIDS.isApplicable(steamOutput.getBlock()),
                    "wireless steam output must retain its dedicated steam output ability");
            helper.assertTrue(!PartAbility.EXPORT_FLUIDS.isApplicable(steamOutput.getBlock()),
                    "wireless steam output must not be a universal fluid hatch");
        }
        helper.setBlock(fluidInputPos, GTNAMachines.WIRELESS_STEAM_INPUT_HATCH.getBlock());
        helper.assertTrue(!plant.getPattern().checkPatternAt(state, false),
                "evaporation plant must reject a steam-only input hatch");
        helper.setBlock(fluidInputPos, GTMachines.FLUID_IMPORT_HATCH[GTValues.HV].getBlock());
        helper.assertTrue(plant.getPattern().checkPatternAt(state, false),
                "evaporation plant must still accept a normal fluid input hatch");
        plant.onStructureFormed();
        helper.assertTrue(plant.isFormed(), "evaporation plant must form");
        helper.assertTrue(!GTNASubPatterns.get(GTNAMachines3.EVAPORATION_PLANT).isEmpty(),
                "GTOCore titanium auxiliary tower must be registered");
        FluidHatchPartMachine input = (FluidHatchPartMachine) metaMachineAt(helper,
                controllerPos.offset(0, -1, 1));
        FluidHatchPartMachine output = (FluidHatchPartMachine) metaMachineAt(helper,
                controllerPos.offset(0, 1, 0));
        EnergyHatchPartMachine energy = (EnergyHatchPartMachine) metaMachineAt(helper,
                controllerPos.offset(0, -1, 0));
        input.tank.setFluidInTank(0, GTMaterials.Water.getFluid(50_000));
        plant.getRecipeLogic().updateTickSubscription();
        for (int tick = 0; tick < 250; tick++) {
            energy.energyContainer.changeEnergy(10_000);
            plant.getRecipeLogic().serverTick();
        }
        helper.assertTrue(output.tank.getFluidInTank(0).getFluid() == GTMaterials.SaltWater.getFluid() &&
                output.tank.getFluidInTank(0).getAmount() >= 1_000,
                "water evaporation must output 1000 mB salt water (output=" +
                        output.tank.getFluidInTank(0) + ", status=" + plant.getRecipeLogic().getStatus() + ")");
        input.tank.setFluidInTank(0, GTMaterials.SaltWater.getFluid(20_000));
        output.tank.setFluidInTank(0, FluidStack.EMPTY);
        plant.getRecipeLogic().resetRecipeLogic();
        for (int tick = 0; tick < 1_100; tick++) {
            energy.energyContainer.changeEnergy(10_000);
            plant.getRecipeLogic().serverTick();
        }
        helper.assertTrue(output.tank.getFluidInTank(0).getFluid() ==
                com.raishxn.gtna.common.data.GTNAMaterials.RawBrine.getFluid() &&
                output.tank.getFluidInTank(0).getAmount() >= 1_000,
                "brine evaporation must output 1000 mB raw brine (output=" +
                        output.tank.getFluidInTank(0) + ", status=" + plant.getRecipeLogic().getStatus() + ")");
        BlockPos otherBasePos = controllerPos.offset(1, -1, 1);
        helper.setBlock(fluidInputPos, GTNABlocks.STAINLESS_EVAPORATION_CASING.get());
        helper.setBlock(otherBasePos, GTMachines.FLUID_IMPORT_HATCH[GTValues.HV].getBlock());
        helper.assertTrue(plant.getPattern().checkPatternAt(state, false),
                "evaporation plant must allow a fluid input at another base casing");
        helper.setBlock(otherBasePos, GTNABlocks.STAINLESS_EVAPORATION_CASING.get());
        helper.setBlock(fluidInputPos, GTMachines.FLUID_IMPORT_HATCH[GTValues.HV].getBlock());
        BlockPos otherOutputPos = controllerPos.offset(1, 1, 0);
        helper.setBlock(controllerPos.offset(0, 1, 0), GTNABlocks.STAINLESS_EVAPORATION_CASING.get());
        helper.setBlock(otherOutputPos, GTMachines.FLUID_EXPORT_HATCH[GTValues.HV].getBlock());
        helper.assertTrue(plant.getPattern().checkPatternAt(state, false),
                "evaporation plant must allow a fluid output at another tower casing");
        helper.setBlock(otherOutputPos, GTMachines.FLUID_EXPORT_HATCH[GTValues.MAX].getBlock());
        helper.assertTrue(plant.getPattern().checkPatternAt(state, false),
                "evaporation plant must accept a MAX fluid output hatch in a tower stage");
        helper.setBlock(otherOutputPos, GTNABlocks.STAINLESS_EVAPORATION_CASING.get());
        helper.setBlock(controllerPos.offset(-1, 0, 0), GTMachines.FLUID_EXPORT_HATCH[GTValues.MAX].getBlock());
        helper.assertTrue(!plant.getPattern().checkPatternAt(state, false),
                "evaporation plant must reject a MAX fluid output hatch in a base input position");
        helper.succeed();
    }

    /** Low charge and legacy SafeMode NBT must never block a valid withdrawal. */
    @GameTest(template = TEMPLATE, timeoutTicks = 20)
    public static void nexusFluxMatrixCanDrainBelowFormerSafeModeThreshold(GameTestHelper helper) {
        UUID owner = UUID.randomUUID();
        NexusEnergyNetwork network = new NexusEnergyNetwork();
        network.setMaxCapacity(owner, new Int128(1_000));
        helper.assertTrue(network.addEnergy(owner, new Int128(50), helper.getLevel()).longValue() == 50,
                "network must accept energy below the former safe-mode threshold");
        helper.assertTrue(network.consumeEnergy(owner, new Int128(50), helper.getLevel()),
                "network must allow withdrawal down to zero");

        CompoundTag oldSave = network.save(new CompoundTag());
        oldSave.getList("EnergyNetworks", net.minecraft.nbt.Tag.TAG_COMPOUND)
                .getCompound(0).putBoolean("SafeMode", true);
        NexusEnergyNetwork restored = new NexusEnergyNetwork(oldSave);
        restored.addEnergy(owner, new Int128(10), helper.getLevel());
        helper.assertTrue(restored.consumeEnergy(owner, new Int128(10), helper.getLevel()),
                "legacy SafeMode flag must not block withdrawal after loading");
        helper.assertTrue(!restored.save(new CompoundTag())
                .getList("EnergyNetworks", net.minecraft.nbt.Tag.TAG_COMPOUND)
                .getCompound(0).contains("SafeMode"),
                "removed SafeMode flag must not be written back to world data");
        helper.succeed();
    }

    /** GTOCore Greenhouse must form from its 5x5x5 MBS and grow the original cactus recipe. */
    @GameTest(template = "empty_16", timeoutTicks = 80)
    public static void greenhouseFormsAndGrows(GameTestHelper helper) {
        // GameTest templates sit underground; move the crop chamber into open sky.
        BlockPos controllerPos = new BlockPos(7, 200, 7);
        var source = GTOCompressedPatternReader.read("greenhouse");
        helper.assertTrue(source.slices().length == 5 && source.slices()[0].length == 5,
                "greenhouse must retain GTOCore's 5x5x5 shape");
        helper.setBlock(controllerPos, GTNAMachines3.GREENHOUSE.getBlock());
        for (int aisle = 0; aisle < 5; aisle++) {
            for (int row = 0; row < 5; row++) {
                String line = source.slices()[aisle][row];
                for (int column = 0; column < line.length(); column++) {
                    BlockPos pos = controllerPos.offset(2 - column, row - 1, 4 - aisle);
                    switch (line.charAt(column)) {
                        case 'B' -> helper.setBlock(pos, GTBlocks.MACHINE_CASING_ULV.get());
                        case 'G' -> helper.setBlock(pos, GTBlocks.CASING_TEMPERED_GLASS.get());
                        case 'd' -> helper.setBlock(pos, Blocks.MUD);
                        case '#', '0' -> helper.setBlock(pos, Blocks.AIR);
                        case 'E' -> {}
                        default -> helper.fail("unexpected greenhouse symbol");
                    }
                }
            }
        }
        BlockPos itemInputPos = controllerPos.offset(-1, -1, 0);
        BlockPos itemOutputPos = controllerPos.offset(0, -1, 0);
        BlockPos fluidInputPos = controllerPos.offset(1, -1, 0);
        BlockPos energyPos = controllerPos.offset(-1, 0, 0);
        BlockPos maintenancePos = controllerPos.offset(1, 0, 0);
        helper.setBlock(itemInputPos, GTMachines.ITEM_IMPORT_BUS[GTValues.MV].getBlock());
        helper.setBlock(itemOutputPos, GTMachines.ITEM_EXPORT_BUS[GTValues.MV].getBlock());
        helper.setBlock(fluidInputPos, GTMachines.FLUID_IMPORT_HATCH[GTValues.MV].getBlock());
        helper.setBlock(energyPos, GTMachines.ENERGY_INPUT_HATCH[GTValues.MV].getBlock());
        helper.setBlock(maintenancePos, GTMachines.MAINTENANCE_HATCH.getBlock());
        MetaMachine machine = metaMachineAt(helper, controllerPos);
        if (!(machine instanceof GreenhouseMachine greenhouse)) {
            helper.fail("greenhouse controller is missing: " + machine);
            return;
        }
        MultiblockState state = greenhouse.getMultiblockState();
        if (!greenhouse.getPattern().checkPatternAt(state, false)) {
            helper.fail("greenhouse pattern did not match: " + patternError(helper, state, controllerPos));
            return;
        }
        greenhouse.onStructureFormed();
        helper.assertTrue(greenhouse.isFormed(), "greenhouse must form");
        ItemBusPartMachine itemInput = (ItemBusPartMachine) metaMachineAt(helper, itemInputPos);
        ItemBusPartMachine itemOutput = (ItemBusPartMachine) metaMachineAt(helper, itemOutputPos);
        FluidHatchPartMachine fluidInput = (FluidHatchPartMachine) metaMachineAt(helper, fluidInputPos);
        EnergyHatchPartMachine energy = (EnergyHatchPartMachine) metaMachineAt(helper, energyPos);
        var maintenance = (com.gregtechceu.gtceu.common.machine.multiblock.part.MaintenanceHatchPartMachine) metaMachineAt(
                helper, maintenancePos);
        maintenance.fixAllMaintenanceProblems();
        helper.getLevel().setDayTime(1000);
        for (int x = -2; x <= 2; x++) {
            for (int z = 0; z <= 4; z++) {
                helper.setBlock(controllerPos.offset(x, 4, z), Blocks.AIR);
            }
        }
        helper.runAfterDelay(20, () -> {
            helper.assertTrue(greenhouse.getCurrentIllumination() > 0,
                    "greenhouse must receive sunlight before its roof is covered (light=" +
                            greenhouse.getCurrentIllumination() + ", facing=" + greenhouse.getFrontFacing() +
                            ", center=" + greenhouse.getPos().relative(greenhouse.getFrontFacing().getOpposite(), 2)
                                    .above(4) +
                            ", roof=" + helper.getLevel().getBlockState(
                                    greenhouse.getPos().relative(greenhouse.getFrontFacing().getOpposite(), 2)
                                            .above(3)) +
                            ", sample=" + helper.getLevel().getBlockState(
                                    greenhouse.getPos().relative(greenhouse.getFrontFacing().getOpposite(), 2)
                                            .above(4)) +
                            ", top=" +
                            helper.getLevel().getHeight(
                                    net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE,
                                    greenhouse.getPos().getX(),
                                    greenhouse.getPos().relative(greenhouse.getFrontFacing().getOpposite(), 2).getZ()) +
                            ", dark=" + helper.getLevel().getSkyDarken() +
                            ")");
            for (int x = -2; x <= 2; x++) {
                for (int z = 0; z <= 4; z++) {
                    helper.setBlock(controllerPos.offset(x, 4, z), Blocks.STONE);
                }
            }
            helper.runAfterDelay(20, () -> {
                helper.assertTrue(greenhouse.getCurrentIllumination() == 0,
                        "opaque cover above the glass roof must block daylight inside the greenhouse");
                for (int x = -2; x <= 2; x++) {
                    for (int z = 0; z <= 4; z++) {
                        helper.setBlock(controllerPos.offset(x, 4, z), Blocks.AIR);
                    }
                }
                helper.runAfterDelay(20, () -> {
                    helper.assertTrue(greenhouse.getCurrentIllumination() > 0,
                            "greenhouse must recover sunlight after the cover is removed (light=" +
                                    greenhouse.getCurrentIllumination() + ")");
                    itemInput.getInventory().insertItem(0, new ItemStack(Blocks.CACTUS), false);
                    itemInput.getInventory().insertItem(1, IntCircuitBehaviour.stack(1), false);
                    fluidInput.tank.setFluidInTank(0, GTMaterials.Water.getFluid(1000));
                    greenhouse.getRecipeLogic().updateTickSubscription();
                    for (int tick = 0; tick < 1300; tick++) {
                        energy.energyContainer.changeEnergy(1000);
                        greenhouse.getRecipeLogic().serverTick();
                    }
                    helper.assertTrue(itemOutput.getInventory().getStackInSlot(0).is(Blocks.CACTUS.asItem()) &&
                            itemOutput.getInventory().getStackInSlot(0).getCount() >= 12,
                            "greenhouse must grow twelve cactus (output=" +
                                    itemOutput.getInventory().getStackInSlot(0) +
                                    ", status=" + greenhouse.getRecipeLogic().getStatus() +
                                    ", reason=" + greenhouse.getRecipeLogic().getFancyTooltip() +
                                    ", input=" + itemInput.getInventory().getStackInSlot(0) + "/" +
                                    itemInput.getInventory().getStackInSlot(1) +
                                    ", fluid=" + fluidInput.tank.getFluidInTank(0) +
                                    ", energy=" + energy.energyContainer.getEnergyStored() +
                                    ", skylight=" + greenhouse.getCurrentIllumination() +
                                    ", skyVisible=" + helper.getLevel().canSeeSky(
                                            helper.absolutePos(controllerPos).relative(greenhouse.getFrontFacing())
                                                    .above(4)) +
                                    ")");
                    helper.succeed();
                });
            });
        });
    }

    /**
     * Formation test for the GTOCore Thermal Power Pump port (G-0062): the decoded 3x3x8 structure
     * (from GTOCore's {@code pattern/thermal_power_pump.mbs}) must match and form, with its one
     * fluid import hatch, one fluid export hatch and one maintenance hatch.
     */
    @GameTest(template = "empty_16", timeoutTicks = 40)
    public static void thermalPowerPumpForms(GameTestHelper helper) {
        if (GTNAMachines.THERMAL_POWER_PUMP == null) {
            helper.fail("thermal_power_pump is disabled by config; the formation test cannot run");
            return;
        }
        BlockPos controllerPos = new BlockPos(4, 2, 2);
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -1; dz <= 8; dz++) {
                    helper.setBlock(controllerPos.offset(dx, dy, dz), Blocks.AIR);
                }
            }
        }
        helper.setBlock(controllerPos, GTNAMachines.THERMAL_POWER_PUMP.getBlock());

        String[][] pattern = {
                { "FFF", "G G", "FFF" },
                { "FHF", "HHH", "FFF" },
                { "FFF", "GEG", "FFF" },
                { "DDD", "DED", "DDD" },
                { "CDC", "AEA", "CAC" },
                { "CDC", "AEA", "CAC" },
                { "CDC", "AEA", "CAC" },
                { "AAA", "A~A", "AAA" },
        };
        // The A cells that carry the three exact-limit parts.
        BlockPos importPos = controllerPos.offset(1, -1, 0);
        BlockPos exportPos = controllerPos.offset(-1, -1, 0);
        BlockPos maintenancePos = controllerPos.offset(0, 1, 0);
        for (int aisle = 0; aisle < pattern.length; aisle++) {
            for (int y = 0; y < 3; y++) {
                for (int x = 0; x < 3; x++) {
                    char c = pattern[aisle][y].charAt(x);
                    if (c == '~' || c == ' ') continue;
                    BlockPos pos = controllerPos.offset(1 - x, y - 1, 7 - aisle);
                    if (pos.equals(importPos) || pos.equals(exportPos) || pos.equals(maintenancePos)) {
                        continue;
                    }
                    switch (c) {
                        case 'A', 'D' -> helper.setBlock(pos, GTNABlocks.BRASS_REINFORCED_WOODEN_CASING.get());
                        case 'C' -> helper.setBlock(pos, GTBlocks.CASING_BRONZE_BRICKS.get());
                        case 'E' -> helper.setBlock(pos, GTBlocks.CASING_BRONZE_PIPE.get());
                        case 'F' -> helper.setBlock(pos, GTNABlocks.BRONZE_REINFORCED_WOOD.get());
                        case 'G' -> helper.setBlock(pos,
                                ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.TreatedWood));
                        case 'H' -> helper.setBlock(pos, GTBlocks.CASING_BRONZE_GEARBOX.get());
                        default -> {}
                    }
                }
            }
        }
        helper.setBlock(importPos, GTMachines.FLUID_IMPORT_HATCH[GTValues.LV].getBlock());
        helper.setBlock(exportPos, GTMachines.FLUID_EXPORT_HATCH[GTValues.LV].getBlock());
        helper.setBlock(maintenancePos, GTMachines.MAINTENANCE_HATCH.getBlock());

        MetaMachine placed = metaMachineAt(helper, controllerPos);
        if (!(placed instanceof ThermalPowerPumpMachine controller)) {
            helper.fail("thermal_power_pump block entity is not a ThermalPowerPumpMachine, got " + placed);
            return;
        }
        MultiblockState state = controller.getMultiblockState();
        if (!controller.getPattern().checkPatternAt(state, false)) {
            helper.fail("thermal_power_pump pattern did not match: " +
                    patternError(helper, state, controller.self().getPos()));
            return;
        }
        controller.onStructureFormed();
        helper.assertTrue(controller.isFormed(), "thermal_power_pump must form from the decoded structure");
        helper.succeed();
    }

    /**
     * Formation test for the GTOCore Liquefaction Furnace port (G-0063): the 5x3x5 coil pattern must
     * match and form.
     */
    @GameTest(template = "empty_16", timeoutTicks = 40)
    public static void liquefactionFurnaceForms(GameTestHelper helper) {
        if (GTNAMachines.LIQUEFACTION_FURNACE == null) {
            helper.fail("liquefaction_furnace is disabled by config; the formation test cannot run");
            return;
        }
        BlockPos controllerPos = new BlockPos(4, 2, 4);
        for (int dx = -2; dx <= 6; dx++) {
            for (int dy = -2; dy <= 4; dy++) {
                for (int dz = -4; dz <= 4; dz++) {
                    helper.setBlock(controllerPos.offset(dx, dy, dz), Blocks.AIR);
                }
            }
        }
        helper.setBlock(controllerPos, GTNAMachines.LIQUEFACTION_FURNACE.getBlock());

        String[][] pattern = {
                { "AAAAA", " BBB ", " AAA " },
                { "AAAAA", "B B B", "ACCCA" },
                { "AAAA~", "BBEBB", "ACFCA" },
                { "AAAAA", "B B B", "ACCCA" },
                { "AAAAA", " BBB ", " AAA " },
        };
        // The pattern is built with FRONT/UP/RIGHT directions, so for a controller facing NORTH:
        // char -> -Z, row -> +Y, aisle -> +X. The controller sits at (char 4, row 0, aisle 2).
        BlockPos maintenancePos = controllerPos.offset(-2, 0, 1);
        BlockPos energyPos = controllerPos.offset(-2, 0, 4);
        BlockPos inputBusPos = controllerPos.offset(-2, 0, 3);
        BlockPos outputHatchPos = controllerPos.offset(-2, 0, 2);
        for (int aisle = 0; aisle < pattern.length; aisle++) {
            for (int y = 0; y < 3; y++) {
                for (int x = 0; x < 5; x++) {
                    char c = pattern[aisle][y].charAt(x);
                    if (c == '~' || c == ' ') continue;
                    BlockPos pos = controllerPos.offset(aisle - 2, y, 4 - x);
                    if (pos.equals(maintenancePos) || pos.equals(energyPos) || pos.equals(inputBusPos) ||
                            pos.equals(outputHatchPos)) {
                        continue;
                    }
                    switch (c) {
                        case 'A' -> helper.setBlock(pos, GTBlocks.CASING_INVAR_HEATPROOF.get());
                        case 'B' -> helper.setBlock(pos, GTBlocks.COIL_CUPRONICKEL.get());
                        case 'C' -> helper.setBlock(pos, GTBlocks.CASING_STEEL_SOLID.get());
                        case 'E' -> helper.setBlock(pos, GTBlocks.CASING_STEEL_PIPE.get());
                        case 'F' -> helper.setBlock(pos, GTMachines.MUFFLER_HATCH[GTValues.LV].getBlock());
                        default -> {}
                    }
                }
            }
        }
        helper.setBlock(maintenancePos, GTMachines.MAINTENANCE_HATCH.getBlock());
        helper.setBlock(energyPos, GTMachines.ENERGY_INPUT_HATCH[GTValues.LV].getBlock());
        helper.setBlock(inputBusPos, GTMachines.ITEM_IMPORT_BUS[GTValues.LV].getBlock());
        helper.setBlock(outputHatchPos, GTMachines.FLUID_EXPORT_HATCH[GTValues.LV].getBlock());

        MetaMachine placed = metaMachineAt(helper, controllerPos);
        if (!(placed instanceof LiquefactionFurnaceMachine controller)) {
            helper.fail("liquefaction_furnace block entity is not a LiquefactionFurnaceMachine, got " + placed);
            return;
        }
        MultiblockState state = controller.getMultiblockState();
        if (!controller.getPattern().checkPatternAt(state, false)) {
            helper.fail("liquefaction_furnace pattern did not match: " +
                    patternError(helper, state, controller.self().getPos()));
            return;
        }
        helper.assertTrue(controller.checkPattern(), "liquefaction_furnace combined pattern must match");
        helper.assertTrue(state.isPosInCache(helper.absolutePos(energyPos)),
                "checking the module must preserve the main structure's block change cache");
        controller.onStructureFormed();
        helper.assertTrue(controller.isFormed(), "liquefaction_furnace must form");
        helper.succeed();
    }

    /**
     * The GTNA module registered for GTCEu's Electric Blast Furnace (G-0064) must be present in the
     * sub-pattern registry; the geometry is validated in game.
     */
    @GameTest(template = TEMPLATE, timeoutTicks = 20)
    public static void ebfModuleIsRegistered(GameTestHelper helper) {
        MultiblockMachineDefinition ebf = GTMultiMachines.ELECTRIC_BLAST_FURNACE;
        helper.assertTrue(!GTNASubPatterns.get(ebf).isEmpty(),
                "the electric_blast_furnace module must be registered in GTNASubPatterns");
        helper.succeed();
    }

    /**
     * The Liquefaction Furnace (GTOCore port, G-0063) must expose its stainless tower module in the
     * sub-pattern registry so it shows up in the JEI preview and unlocks Parallel / Accelerate
     * hatches.
     */
    @GameTest(template = TEMPLATE, timeoutTicks = 20)
    public static void liquefactionModuleIsRegistered(GameTestHelper helper) {
        helper.assertTrue(!GTNASubPatterns.get(GTNAMachines.LIQUEFACTION_FURNACE).isEmpty(),
                "the liquefaction_furnace module must be registered in GTNASubPatterns");
        helper.succeed();
    }

    /**
     * A machine with modules must advertise them on its item tooltip (G-0067), so players learn what
     * an attachable module unlocks — GTOCore does this with {@code moduleTooltips}.
     */
    @GameTest(template = TEMPLATE, timeoutTicks = 20)
    public static void moduleTooltipsAreRegistered(GameTestHelper helper) {
        helper.assertTrue(!GTNASubPatterns.getTooltips(GTMultiMachines.ELECTRIC_BLAST_FURNACE).isEmpty(),
                "the electric_blast_furnace module must carry a tooltip");
        helper.assertTrue(!GTNASubPatterns.getTooltips(GTNAMachines.LIQUEFACTION_FURNACE).isEmpty(),
                "the liquefaction_furnace module must carry a tooltip");
        helper.succeed();
    }

    /**
     * Formation test for the GTOCore Electric Blast Furnace module (G-0068): builds the stock 3x4x3
     * EBF plus the invar shell module at the controller and checks the module matches (so the extra
     * Energy / Accelerate hatches are merged into the controller).
     */
    @GameTest(template = "empty_16", timeoutTicks = 40)
    public static void ebfModuleForms(GameTestHelper helper) {
        BlockPos controllerPos = new BlockPos(6, 2, 6);
        for (int dx = -6; dx <= 6; dx++) {
            for (int dy = -2; dy <= 8; dy++) {
                for (int dz = -6; dz <= 8; dz++) {
                    helper.setBlock(controllerPos.offset(dx, dy, dz), Blocks.AIR);
                }
            }
        }
        helper.setBlock(controllerPos, GTMultiMachines.ELECTRIC_BLAST_FURNACE.getBlock());

        // Stock EBF: 3 aisles (z), 4 rows (y), 3 chars (x); controller 'S' at (char 1, row 0, aisle 2).
        // Default directions + facing NORTH: world = (1 - char, row, 2 - aisle).
        String[][] main = {
                { "XXX", "CCC", "CCC", "XXX" },
                { "XXX", "C#C", "C#C", "XMX" },
                { "XSX", "CCC", "CCC", "XXX" },
        };
        BlockPos energyPos = controllerPos.offset(1, 0, 0);
        BlockPos inputBusPos = controllerPos.offset(-1, 0, 0);
        BlockPos outputBusPos = controllerPos.offset(1, 0, 2);
        BlockPos maintPos = controllerPos.offset(-1, 0, 2);
        for (int aisle = 0; aisle < main.length; aisle++) {
            for (int row = 0; row < 4; row++) {
                for (int ch = 0; ch < 3; ch++) {
                    char c = main[aisle][row].charAt(ch);
                    if (c == 'S' || c == '#') continue;
                    BlockPos pos = controllerPos.offset(1 - ch, row, 2 - aisle);
                    if (pos.equals(energyPos) || pos.equals(inputBusPos) || pos.equals(outputBusPos) ||
                            pos.equals(maintPos)) {
                        continue;
                    }
                    switch (c) {
                        case 'X' -> helper.setBlock(pos, GTBlocks.CASING_INVAR_HEATPROOF.get());
                        case 'C' -> helper.setBlock(pos, GTBlocks.COIL_CUPRONICKEL.get());
                        case 'M' -> helper.setBlock(pos, GTMachines.MUFFLER_HATCH[GTValues.LV].getBlock());
                        default -> {}
                    }
                }
            }
        }
        helper.setBlock(energyPos, GTMachines.ENERGY_INPUT_HATCH[GTValues.LV].getBlock());
        helper.setBlock(inputBusPos, GTMachines.ITEM_IMPORT_BUS[GTValues.LV].getBlock());
        helper.setBlock(outputBusPos, GTMachines.ITEM_EXPORT_BUS[GTValues.LV].getBlock());
        helper.setBlock(maintPos, GTMachines.MAINTENANCE_HATCH.getBlock());

        // GTOCore module: 5 aisles, 4 rows, 5 chars; controller 'E' at (char 2, row 0, aisle 4).
        // Default directions + facing NORTH: world = (2 - char, row, 4 - aisle).
        String[][] module = {
                { "AAAAA", " DBD ", " DBD ", " CCC " },
                { "ACCCA", "BD DB", "BD DB", "CCCCC" },
                { "A   A", "     ", "     ", "C   C" },
                { "A   A", "B   B", "B   B", "C   C" },
                { "A E A", "     ", "     ", "     " },
        };
        for (int aisle = 0; aisle < module.length; aisle++) {
            for (int row = 0; row < 4; row++) {
                for (int ch = 0; ch < 5; ch++) {
                    char c = module[aisle][row].charAt(ch);
                    if (c == 'E' || c == ' ') continue;
                    BlockPos pos = controllerPos.offset(2 - ch, row, 4 - aisle);
                    switch (c) {
                        case 'A' -> helper.setBlock(pos, GTBlocks.CASING_INVAR_HEATPROOF.get());
                        case 'B' -> helper.setBlock(pos, ChemicalHelper
                                .getBlock(TagPrefix.frameGt, GTMaterials.StainlessSteel));
                        case 'C' -> helper.setBlock(pos, GTBlocks.CASING_INVAR_HEATPROOF.get());
                        case 'D' -> helper.setBlock(pos, GTBlocks.CASING_STEEL_PIPE.get());
                        default -> {}
                    }
                }
            }
        }
        // Put the module's extra Energy Hatch and Accelerate Hatch on two of its 'A' cells.
        helper.setBlock(controllerPos.offset(2, 0, 4), GTMachines.ENERGY_INPUT_HATCH[GTValues.LV].getBlock());
        helper.setBlock(controllerPos.offset(1, 0, 4), GTNAMachines2.ACCELERATE_HATCHES[GTValues.LV].getBlock());

        MetaMachine placed = metaMachineAt(helper, controllerPos);
        if (!(placed instanceof com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine controller)) {
            helper.fail("electric_blast_furnace block entity is not a MultiblockControllerMachine, got " + placed);
            return;
        }
        var state = controller.getMultiblockState();
        if (!controller.getPattern().checkPatternAt(state, false)) {
            helper.fail("electric_blast_furnace main pattern did not match: " +
                    patternError(helper, state, controllerPos));
            return;
        }
        var sub = GTNASubPatterns.get(GTMultiMachines.ELECTRIC_BLAST_FURNACE).get(0);
        if (!sub.checkPatternAt(state, false)) {
            helper.fail("electric_blast_furnace module pattern did not match");
            return;
        }
        if (!controller.checkPattern()) {
            helper.fail("electric_blast_furnace + module combined checkPattern() did not match");
            return;
        }
        int formed = ((com.raishxn.gtna.api.machine.multiblock.IGTNAModuleHost) controller).gtna$formedModuleCount();
        helper.assertTrue(formed == 1, "expected 1 formed module, got " + formed);
        helper.assertTrue(com.raishxn.gtna.api.machine.multiblock.GTNAStructureRefresh.refresh(controller, true),
                "forced structure refresh must form the EBF and its module");
        helper.assertTrue(controller.isFormed(), "EBF must remain formed after the forced refresh");

        // The base allows two Energy Hatches; the auxiliary shell contributes exactly one more.
        BlockPos secondBaseEnergy = controllerPos.offset(1, 0, 1);
        helper.setBlock(secondBaseEnergy, GTMachines.ENERGY_INPUT_HATCH[GTValues.LV].getBlock());
        helper.assertTrue(com.raishxn.gtna.api.machine.multiblock.GTNAStructureRefresh.refresh(controller, true),
                "EBF must form with two base Energy Hatches and one auxiliary Energy Hatch");
        helper.assertTrue(((com.raishxn.gtna.api.machine.multiblock.IGTNAModuleHost) controller)
                .gtna$formedModuleCount() == 1, "third Energy Hatch must be supplied by the module");

        BlockPos secondModuleEnergy = controllerPos.offset(-2, 0, 4);
        helper.setBlock(secondModuleEnergy, GTMachines.ENERGY_INPUT_HATCH[GTValues.LV].getBlock());
        helper.assertTrue(com.raishxn.gtna.api.machine.multiblock.GTNAStructureRefresh.refresh(controller, true),
                "EBF base must remain formed with a second auxiliary Energy Hatch");
        helper.assertTrue(((com.raishxn.gtna.api.machine.multiblock.IGTNAModuleHost) controller)
                .gtna$formedModuleCount() == 0, "module must reject its second Energy Hatch");
        helper.setBlock(secondModuleEnergy, GTBlocks.CASING_INVAR_HEATPROOF.get());

        helper.setBlock(secondModuleEnergy,
                com.raishxn.gtna.common.data.GTNAEnergyHatches.WIRELESS_ENERGY_HATCHES[GTValues.LV][0].getBlock());
        helper.assertTrue(com.raishxn.gtna.api.machine.multiblock.GTNAStructureRefresh.refresh(controller, true),
                "EBF base must remain formed with a wireless hatch in the module shell");
        helper.assertTrue(((com.raishxn.gtna.api.machine.multiblock.IGTNAModuleHost) controller)
                .gtna$formedModuleCount() == 0, "module must reject wireless Energy Hatches");
        helper.setBlock(secondModuleEnergy, GTBlocks.CASING_INVAR_HEATPROOF.get());

        // A second Accelerate Hatch in the base must not be counted with the one in the module.
        BlockPos baseCasing = controllerPos.offset(-1, 0, 1);
        helper.setBlock(baseCasing, GTNAMachines2.ACCELERATE_HATCHES[GTValues.LV].getBlock());
        helper.assertTrue(com.raishxn.gtna.api.machine.multiblock.GTNAStructureRefresh.refresh(controller, true),
                "EBF base must still form with one Accelerate Hatch");
        helper.assertTrue(((com.raishxn.gtna.api.machine.multiblock.IGTNAModuleHost) controller)
                .gtna$formedModuleCount() == 0, "module must reject a second Accelerate Hatch");
        helper.setBlock(baseCasing, GTBlocks.CASING_INVAR_HEATPROOF.get());
        helper.assertTrue(com.raishxn.gtna.api.machine.multiblock.GTNAStructureRefresh.refresh(controller, true),
                "EBF module must form again after removing the duplicate hatch");
        helper.assertTrue(((com.raishxn.gtna.api.machine.multiblock.IGTNAModuleHost) controller)
                .gtna$formedModuleCount() == 1, "module must be restored with one Accelerate Hatch");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 20)
    public static void nativeCraftingCpuKeepsAe2Executor(GameTestHelper helper) {
        CraftingCPUCluster cluster = new CraftingCPUCluster(BlockPos.ZERO, BlockPos.ZERO);
        helper.assertTrue(cluster.craftingLogic.getClass() == CraftingCpuLogic.class,
                "a native AE2 CPU must keep CraftingCpuLogic, got " + cluster.craftingLogic.getClass().getName());
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 20)
    public static void hypercoreInterfaceHasSharedCpuAndNoInventory(GameTestHelper helper) {
        if (GTNAMachines2.CRAFTING_CPU_INTERFACE == null) {
            helper.fail("Crafting CPU Interface is disabled by config");
            return;
        }
        BlockPos pos = new BlockPos(2, 2, 2);
        helper.setBlock(pos, GTNAMachines2.CRAFTING_CPU_INTERFACE.getBlock());
        if (!(helper.getBlockEntity(pos) instanceof MetaMachineBlockEntity holder) ||
                !(holder.getMetaMachine() instanceof GTNACraftingCPUInterfacePartMachine machine)) {
            helper.fail("Crafting CPU Interface did not create its machine");
            return;
        }
        helper.assertTrue(machine.getInventory().getSlots() == 0,
                "the network interface must not expose item slots");
        machine.configurePool(1024 + 2048 + 4096, 2 + 4 + 8, false);
        helper.assertTrue(machine.getCpuPool().getAvailableStorage() == 7168,
                "storage modules must contribute to one shared CPU capacity");
        helper.assertTrue(machine.getCpuPool().getCoProcessors() == 14,
                "co-processors must contribute to one shared lane budget");
        helper.assertTrue(machine.getCpuPool().getActiveJobCount() == 0,
                "an idle pool must not create CPUs before a crafting request");
        net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
        machine.saveCustomPersistedData(tag, false);
        helper.assertTrue(tag.contains("NexusSharedCraftingPool", net.minecraft.nbt.Tag.TAG_COMPOUND),
                "shared pool state must be persisted");
        helper.succeed();
    }

    /**
     * Formation test for the GTOCore Brick Kiln port (G-0060): the decoded 5x4x7 primitive structure
     * (from GTOCore's {@code pattern/brick_kiln.mbs}) must match and form. Uses the larger
     * {@code empty_16} template because the 7-deep shape does not fit a disjoint quadrant of
     * {@code empty_12}.
     */
    @GameTest(template = "empty_16", timeoutTicks = 40)
    public static void brickKilnForms(GameTestHelper helper) {
        if (GTNAMachines.BRICK_KILN == null) {
            helper.fail("brick_kiln is disabled by config; the formation test cannot run");
            return;
        }
        BlockPos controllerPos = new BlockPos(4, 2, 2);
        // Wipe the 5x4x7 volume plus a margin (clearArea's fixed radius is too small for this shape).
        for (int dx = -3; dx <= 3; dx++) {
            for (int dy = -1; dy <= 4; dy++) {
                for (int dz = -1; dz <= 7; dz++) {
                    helper.setBlock(controllerPos.offset(dx, dy, dz), Blocks.AIR);
                }
            }
        }
        helper.setBlock(controllerPos, GTNAMachines.BRICK_KILN.getBlock());

        String[][] pattern = {
                { " AAA ", " BBB ", " BBB ", "  B  " },
                { "ACDCA", "BB BB", "BB BB", " BBB " },
                { "ADDDA", "B   B", "B   B", " BBB " },
                { "ADDDA", "B   B", "B   B", " BBB " },
                { "ADDDA", "B   B", "B   B", " BBB " },
                { "ACDCA", "BB BB", "BB BB", " BBB " },
                { " A~A ", " BBB ", " BBB ", "  B  " },
        };
        for (int aisle = 0; aisle < pattern.length; aisle++) {
            for (int y = 0; y < 4; y++) {
                for (int x = 0; x < 5; x++) {
                    char c = pattern[aisle][y].charAt(x);
                    if (c == '~' || c == ' ') continue;
                    BlockPos pos = controllerPos.offset(2 - x, y, 6 - aisle);
                    switch (c) {
                        case 'A', 'C' -> helper.setBlock(pos, GTBlocks.CASING_PRIMITIVE_BRICKS.get());
                        case 'B' -> helper.setBlock(pos, Blocks.BRICKS);
                        case 'D' -> helper.setBlock(pos, Blocks.STONE_BRICKS);
                        default -> {}
                    }
                }
            }
        }

        // The controller can run an async pattern check while the structure is being placed.
        // Check after the next tick so its cached state reflects the complete kiln.
        helper.runAfterDelay(2, () -> {
            MetaMachine placed = metaMachineAt(helper, controllerPos);
            if (!(placed instanceof BrickKilnMachine controller)) {
                helper.fail("brick_kiln block entity is not a BrickKilnMachine, got " + placed);
                return;
            }
            MultiblockState state = controller.getMultiblockState();
            if (!controller.getPattern().checkPatternAt(state, false)) {
                helper.fail("brick_kiln pattern did not match: " +
                        patternError(helper, state, controller.self().getPos()));
                return;
            }
            controller.onStructureFormed();
            helper.assertTrue(controller.isFormed(), "brick_kiln must form from the decoded structure");
            helper.succeed();
        });
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 40)
    public static void bufferModeFilterGatesSlotAcceptance(GameTestHelper helper) {
        if (GTNAMachines2.ME_PATTERN_BUFFER == null) {
            helper.fail("me_pattern_buffer is disabled by config; the mode filter test cannot run");
            return;
        }
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, GTNAMachines2.ME_PATTERN_BUFFER.get().self());
        BlockEntity placed = helper.getBlockEntity(pos);
        if (!(placed instanceof MetaMachineBlockEntity holder) ||
                !(holder.getMetaMachine() instanceof GTNAMEPatternBufferPartMachine buffer)) {
            helper.fail("placing me_pattern_buffer must create a GTNAMEPatternBufferPartMachine, got " + placed);
            return;
        }

        // `var` on purpose: the recipe-holder type name differs across mappings, and we only need
        // the value.
        var assemblerRecipes = helper.getLevel().getRecipeManager()
                .getAllRecipesFor(GTRecipeTypes.ASSEMBLER_RECIPES);
        if (assemblerRecipes.isEmpty()) {
            helper.fail("no assembler recipes are loaded, so the buffer filter cannot be exercised");
            return;
        }
        GTRecipe assemblerRecipe = assemblerRecipes.get(0);

        // Unfiltered buffer (the default) accepts the recipe.
        helper.assertTrue(buffer.gtna$slotAcceptsRecipe(0, assemblerRecipe),
                "an unpinned buffer must accept a recipe of any of its controller's types");

        // Pinned to the recipe's own mode: still accepted.
        buffer.setSelectedModeId("gtceu:assembler");
        helper.assertTrue(buffer.gtna$slotAcceptsRecipe(0, assemblerRecipe),
                "a buffer pinned to the recipe's own mode must still accept it");

        // Pinned to a different mode: refused, whichever slot is asked.
        buffer.setSelectedModeId("gtceu:circuit_assembler");
        helper.assertFalse(buffer.gtna$slotAcceptsRecipe(0, assemblerRecipe),
                "a buffer pinned to another mode must refuse the recipe");
        helper.assertFalse(buffer.gtna$slotAcceptsRecipe(buffer.getMaxPatternCount() - 1, assemblerRecipe),
                "the buffer-level filter must apply to every slot, not just the first");

        // Clearing the filter (the "All Modes" option) restores acceptance.
        buffer.setSelectedModeId("");
        helper.assertTrue(buffer.gtna$slotAcceptsRecipe(0, assemblerRecipe),
                "clearing the mode filter must restore acceptance");

        helper.succeed();
    }

    /**
     * End-to-end test of the automatic machine-mode mirror: a formed duration_tester that runs a
     * circuit-assembler recipe must flip its active recipe type to circuit assembler (index 1).
     *
     * <p>
     * This is the assertion the whole feature lives or dies on and the one the unit tests cannot
     * make: it needs a real structure, a real recipe of the second type, energy and items. The
     * recipe is injected into the circuit assembler type at runtime so the test does not depend on
     * whatever the datapack happens to contain.
     */
    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void runningSecondRecipeTypeMirrorsControllerMode(GameTestHelper helper) {
        if (GTNAMachines2.DURATION_TESTER == null) {
            helper.fail("duration_tester is disabled by config; the mode mirror cannot be tested");
            return;
        }
        injectCircuitAssemblerRecipe();

        // Build and match, retrying on the intermittent spurious limit error. GTCEu's matcher
        // occasionally reports "Maximum: 1" for this definition even when the area provably holds a
        // single maintenance hatch (the failure dump prints the whole area). A rebuild does not hide
        // feature regressions: those fail the recipe assertions below, not the structure check.
        BlockPos controllerPos = new BlockPos(2, 2, 2);
        WorkableElectricMultipleRecipesMachine controller = null;
        boolean matched = false;
        for (int attempt = 1; attempt <= 3 && !matched; attempt++) {
            clearArea(helper, controllerPos);
            buildDurationTester(helper, controllerPos);
            MetaMachine placed = metaMachineAt(helper, controllerPos);
            if (!(placed instanceof WorkableElectricMultipleRecipesMachine machine)) {
                helper.fail("duration_tester block entity is not our machine class, got " + placed);
                return;
            }
            controller = machine;
            // Force the structure check rather than waiting for the ticker (the trick GTCEu's own
            // gametests use). onStructureFormed() sets isFormed() unconditionally, so the match
            // result itself has to be asserted.
            MultiblockState state = controller.getMultiblockState();
            matched = controller.getPattern().checkPatternAt(state, false);
            if (!matched && attempt == 3) {
                helper.fail("duration_tester pattern did not match after 3 attempts: " +
                        patternError(helper, state, controller.self().getPos()));
                return;
            }
        }
        controller.onStructureFormed();
        // Same offsets the builder used; kept here so the assertions below can address the parts.
        BlockPos energyPos = controllerPos.offset(-1, -1, 2);
        BlockPos inputBusPos = controllerPos.offset(0, -1, 2);

        helper.assertTrue(controller.getActiveRecipeType() == 0,
                "the machine must start on its first recipe type (assembler), was index " +
                        controller.getActiveRecipeType());

        // Energy is needed even for the match pass: NotifiableEnergyContainer.handleRecipeInner
        // consults getEnergyStored() when simulating an IO.IN. EnergyContainerList (what the
        // controller exposes) does not implement addEnergy — the interface default is a no-op — so
        // fill the hatch's own container and then check the controller sees it.
        EnergyHatchPartMachine energyHatch = (EnergyHatchPartMachine) metaMachineAt(helper, energyPos);
        IEnergyContainer energy = energyHatch.energyContainer;
        long target = Math.min(energy.getEnergyCapacity(), 1_000_000L);
        for (int attempt = 0; attempt < 8 && energy.getEnergyStored() < target; attempt++) {
            energy.addEnergy(target - energy.getEnergyStored());
        }
        helper.assertTrue(energy.getEnergyStored() > 0,
                "the energy hatch must hold energy, otherwise the recipe can never match");
        // GTCEu also checks patterns on its own async thread (MultiblockWorldSavedData.searchingTask,
        // every 250 ms) and that thread rewrites the very MultiblockState match context this test
        // writes. In roughly one run out of seven the interleaving was observed to leave the
        // controller with a partial part set, so getEnergyContainer() came back empty
        // (capacity=0, euInHandlers=0) although the hatch itself held 65 kEU. Re-running
        // onStructureFormed() rebuilds parts and capabilities from the match context, which steps out
        // of that race without masking anything: the pattern match is asserted above and the recipe
        // assertions below still have to pass.
        for (int attempt = 0; attempt < 5 && controller.getEnergyContainer().getEnergyStored() <= 0; attempt++) {
            controller.onStructureFormed();
        }
        helper.assertTrue(controller.getParts().size() > 0,
                "the pattern matched but no parts were registered on the controller");
        helper.assertTrue(controller.getEnergyContainer().getEnergyStored() > 0,
                "the controller must see the energy stored in its energy hatch (parts=" +
                        controller.getParts().size() + ", hatchStored=" + energy.getEnergyStored() +
                        ", capacity=" + controller.getEnergyContainer().getEnergyCapacity() +
                        ", euInHandlers=" +
                        controller.getCapabilitiesFlat(IO.IN, EURecipeCapability.CAP).size() + ")");
        ItemBusPartMachine inputBus = (ItemBusPartMachine) metaMachineAt(helper, inputBusPos);
        inputBus.getInventory().insertItem(0, new ItemStack(Items.COBBLESTONE, 16), false);

        // Drive the logic directly: one serverTick collects the candidates and starts the recipe,
        // and the mode mirror is applied inside that same path.
        GTNAMultipleRecipesLogic logic = controller.getRecipeLogic();
        for (int attempt = 0; attempt < 10 && controller.getActiveRecipeType() == 0; attempt++) {
            logic.serverTick();
        }

        helper.assertTrue(controller.getActiveRecipeType() == 1,
                "running a circuit-assembler recipe must mirror mode index 1 onto the controller, " +
                        "but activeRecipeType is " + controller.getActiveRecipeType());
        helper.succeed();
    }

    /**
     * The pattern grid of GTCEu's {@code multi_smelter} (GTMultiMachines), copied so the structure can
     * be rebuilt by code: aisle 0 = pattern z+2 relative to the controller, aisle 2 = the controller's
     * own layer. {@code X} casing (also accepts the buses/hatches), {@code C} heating coil,
     * {@code M} muffler, {@code #} air, {@code S} controller.
     */
    private static final String[][] MULTI_SMELTER_PATTERN = {
            { "XXX", "CCC", "XXX" },
            { "XXX", "C#C", "XMX" },
            { "XSX", "CCC", "XXX" },
    };

    /**
     * Pattern grid of {@code large_steam_alloy_smelter} (GTNA), copied so the 3x4x3 structure can be
     * rebuilt by code: {@code A} bronze casing (also accepts the steam machine buses/hatches, and now
     * any steam source by ability), {@code B} firebox, {@code ~} controller, space = anything.
     */
    private static final String[][] LARGE_STEAM_ALLOY_SMELTER_PATTERN = {
            { "BBB", "AAA", "AAA", " A " },
            { "BBB", "A A", "AAA", "AAA" },
            { "BBB", "A~A", "AAA", " A " },
    };

    /** Pattern grid of the Universal Factory: casing {@code A}, steel frame {@code B}, controller {@code ~}. */
    private static final String[][] UNIVERSAL_FACTORY_PATTERN = {
            { "AAA", "AAA", "AAA" },
            { "AAA", "ABA", "AAA" },
            { "AAA", "A~A", "AAA" },
    };

    /** Pattern grid of the Primitive Stone Furnace: stone {@code A}, controller {@code ~}, air {@code ' '}. */
    private static final String[][] PRIMITIVE_STONE_FURNACE_PATTERN = {
            { "AAA", "AAA", "AAA" },
            { "AAA", "A A", "AAA" },
            { "AAA", "A~A", "AAA" },
    };

    /**
     * End-to-end test of the buffer-driven machine mode on a <b>base GTCEu</b> multiblock: the
     * {@code multi_smelter} uses the stock {@code RecipeLogic}, which only searches its active recipe
     * type, so the mixin at HEAD of {@code searchRecipe} is what lets a pattern buffer put the
     * machine in the right mode before the search.
     *
     * <p>
     * Pinning the buffer to the machine's second type must flip {@code activeRecipeType} 0 -> 1 once
     * the machine searches; clearing the pin must leave the mode alone.
     */
    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void patternBufferDrivesBaseMachineMode(GameTestHelper helper) {
        if (GTNAMachines2.ME_PATTERN_BUFFER == null) {
            helper.fail("me_pattern_buffer is disabled by config; the auto-switch cannot be tested");
            return;
        }
        // Second quadrant: far enough from the duration_tester test (origin 2,2,2) that the two
        // structures can never share a cell.
        BlockPos controllerPos = new BlockPos(8, 2, 8);
        clearArea(helper, controllerPos);
        helper.setBlock(controllerPos, GTMultiMachines.MULTI_SMELTER.getBlock());

        // World offsets from the pattern axes: char index -> world -X, string index -> +Y and aisle
        // index -> -Z, with the controller cell as the origin. The multi_smelter's controller sits at
        // char 1, string 0, aisle 2, so the offsets below are relative to it.
        for (int aisle = 0; aisle < 3; aisle++) {
            for (int string = 0; string < 3; string++) {
                for (int charX = 0; charX < 3; charX++) {
                    BlockPos pos = controllerPos.offset(1 - charX, string, 2 - aisle);
                    switch (MULTI_SMELTER_PATTERN[aisle][string].charAt(charX)) {
                        case 'C' -> helper.setBlock(pos, GTBlocks.COIL_CUPRONICKEL.get());
                        case 'M' -> helper.setBlock(pos, GTMachines.MUFFLER_HATCH[GTValues.LV].getBlock());
                        case 'X' -> helper.setBlock(pos, GTBlocks.CASING_INVAR_HEATPROOF.get());
                        default -> {
                            // 'S' (controller, already placed) and '#' (must stay air)
                        }
                    }
                }
            }
        }

        // The GTNA pattern buffer registers IMPORT_ITEMS, so the machine's autoAbilities predicates
        // accept it in a casing slot exactly like an input bus. Energy and maintenance are mandatory
        // here (autoAbilities requires an energy hatch, and maintenance is enabled by default).
        BlockPos bufferPos = controllerPos.offset(0, 0, 1);
        helper.setBlock(bufferPos, GTNAMachines2.ME_PATTERN_BUFFER.getBlock());
        helper.setBlock(controllerPos.offset(1, 0, 2), GTMachines.ENERGY_INPUT_HATCH[GTValues.LV].getBlock());
        helper.setBlock(controllerPos.offset(0, 0, 2), GTMachines.ITEM_IMPORT_BUS[GTValues.LV].getBlock());
        helper.setBlock(controllerPos.offset(-1, 0, 2), GTMachines.ITEM_EXPORT_BUS[GTValues.LV].getBlock());
        helper.setBlock(controllerPos.offset(1, 0, 1), GTMachines.MAINTENANCE_HATCH.getBlock());

        MetaMachine placed = metaMachineAt(helper, controllerPos);
        if (!(placed instanceof WorkableElectricMultiblockMachine controller)) {
            helper.fail("multi_smelter block entity is not a WorkableElectricMultiblockMachine, got " + placed);
            return;
        }
        MultiblockState state = controller.getMultiblockState();
        if (!controller.getPattern().checkPatternAt(state, false)) {
            helper.fail(
                    "multi_smelter pattern did not match: " + patternError(helper, state, controller.self().getPos()));
            return;
        }
        controller.onStructureFormed();
        helper.assertTrue(controller.getParts().size() > 0,
                "the pattern matched but no parts were registered on the controller");
        helper.assertTrue(controller.getActiveRecipeType() == 0,
                "multi_smelter must start on furnace, was index " + controller.getActiveRecipeType());

        if (!(metaMachineAt(helper, bufferPos) instanceof GTNAMEPatternBufferPartMachine buffer)) {
            helper.fail("no GTNA pattern buffer in the structure");
            return;
        }

        // Pin the buffer to the machine's second recipe type. The buffer's own filter is the explicit
        // manual control, and it is what the auto-switch reads as the pending request.
        buffer.setSelectedModeId("gtceu:alloy_smelter");
        controller.getRecipeLogic().findAndHandleRecipe();
        helper.assertTrue(controller.getActiveRecipeType() == 1,
                "an idle machine must follow the buffer's pinned mode (expected alloy_smelter, got index " +
                        controller.getActiveRecipeType() + ")");

        // Clearing the request must not move the machine back: with nothing pending the buffer has
        // no opinion, which is exactly the idle-only policy.
        buffer.setSelectedModeId("");
        controller.getRecipeLogic().findAndHandleRecipe();
        helper.assertTrue(controller.getActiveRecipeType() == 1,
                "a buffer with no request must leave the mode alone, but it became index " +
                        controller.getActiveRecipeType());
        helper.succeed();
    }

    /**
     * Regression for the "wireless steam input hatch is not accepted" report: a GTNA steam multiblock
     * must accept the wireless hatch in its steam-source slot and wire its tank as the machine's
     * steam energy handler.
     *
     * <p>
     * The bug was a predicate mismatch, invisible to both unit tests and the other gametests: the
     * patterns pinned the steam slot to the exact stock block, {@code blocks(GTMachines.STEAM_HATCH)},
     * instead of the ability ({@code abilities(PartAbility.STEAM)}), so a part that legitimately
     * declares the STEAM ability was rejected by {@code checkPatternAt}. GTCEu's own steam
     * multiblocks (steam grinder/oven) use the ability form; the contract is also locked by
     * {@code SteamWiringContractTest}.
     */
    @GameTest(template = TEMPLATE, timeoutTicks = 40)
    public static void wirelessSteamHatchIsAcceptedAsSteamSource(GameTestHelper helper) {
        MultiblockMachineDefinition definition = GTNAMachines.LARGE_STEAM_ALLOY_SMELTER;
        if (definition == null) {
            helper.fail("large_steam_alloy_smelter is disabled by config; the wireless hatch test cannot run");
            return;
        }
        if (GTNAMachines.WIRELESS_STEAM_INPUT_HATCH == null) {
            helper.fail("wireless_steam_input_hatch is disabled by config; the acceptance test cannot run");
            return;
        }
        // Third area, high in Y: disjoint from the other structure tests (origins 2,2,2 and 8,2,8)
        // in every axis.
        BlockPos controllerPos = new BlockPos(2, 8, 2);
        clearArea(helper, controllerPos);
        helper.setBlock(controllerPos, definition.getBlock());

        // Same axis mapping as the other structure tests: offset = (1 - char, string - s0, 2 - aisle),
        // with the controller sitting at pattern (char 1, string 1, aisle 2) in this definition.
        // Skip the one 'A' cell that the wireless hatch takes over.
        BlockPos wirelessPos = controllerPos.offset(1, 0, 0);
        for (int aisle = 0; aisle < 3; aisle++) {
            for (int string = 0; string < 4; string++) {
                for (int charX = 0; charX < 3; charX++) {
                    BlockPos pos = controllerPos.offset(1 - charX, string - 1, 2 - aisle);
                    if (pos.equals(wirelessPos)) {
                        continue;
                    }
                    switch (LARGE_STEAM_ALLOY_SMELTER_PATTERN[aisle][string].charAt(charX)) {
                        case 'B' -> helper.setBlock(pos, GTBlocks.FIREBOX_BRONZE.get());
                        case 'A' -> helper.setBlock(pos, GTBlocks.CASING_BRONZE_BRICKS.get());
                        default -> {
                            // '~' (controller, already placed) and space (any).
                        }
                    }
                }
            }
        }
        helper.setBlock(wirelessPos, GTNAMachines.WIRELESS_STEAM_INPUT_HATCH.getBlock());

        MetaMachine placed = metaMachineAt(helper, controllerPos);
        if (!(placed instanceof SteamMultiMachineBase controller)) {
            helper.fail("large_steam_alloy_smelter block entity is not a SteamMultiMachineBase, got " + placed);
            return;
        }
        MultiblockState state = controller.getMultiblockState();
        if (!controller.getPattern().checkPatternAt(state, false)) {
            helper.fail("a steam multiblock must accept the wireless steam input hatch in its steam slot " +
                    "(the pattern still pins the exact stock block?): " +
                    patternError(helper, state, controller.self().getPos()));
            return;
        }
        controller.onStructureFormed();
        helper.assertTrue(controller.isFormed(),
                "the wireless steam input hatch must be wired as the machine's steam energy source " +
                        "(a steam-capable part was not found, so the structure invalidated itself)");
        helper.assertTrue(!controller.isHighPressure(),
                "a bronze-cased structure must not be in high pressure mode");
        helper.assertTrue(controller.getEffectiveConversionRate() == 1.0,
                "a bronze-cased structure must use the normal steam conversion rate");
        helper.assertTrue(!controller.getCapabilitiesFlat(IO.IN, EURecipeCapability.CAP).isEmpty(),
                "the wireless hatch's steam tank must be exposed to the controller as an EU IN handler");

        // Steam accounting: the global network must never lose or duplicate steam (GTNL
        // add/consume balance). Exercised at runtime because it is the reported "voiding" path.
        UUID owner = UUID.randomUUID();
        long start = SteamWirelessNetworkManager.getUserSteam(helper.getLevel(), owner);
        helper.assertTrue(start == 0L, "a fresh network owner must start at 0 mB, was " + start);
        helper.assertTrue(SteamWirelessNetworkManager.addSteamToGlobalSteamMap(helper.getLevel(), owner, 1000L),
                "adding steam to the network must succeed");
        helper.assertTrue(SteamWirelessNetworkManager.getUserSteam(helper.getLevel(), owner) == 1000L,
                "the network must hold exactly the steam that was added");
        helper.assertTrue(SteamWirelessNetworkManager.consumeSteamFromGlobalMap(helper.getLevel(), owner, 400L),
                "consuming available steam must succeed");
        helper.assertTrue(SteamWirelessNetworkManager.getUserSteam(helper.getLevel(), owner) == 600L,
                "the network must hold exactly the steam that remains (1000 - 400)");
        helper.assertTrue(!SteamWirelessNetworkManager.consumeSteamFromGlobalMap(helper.getLevel(), owner, 601L),
                "an overdraft must be rejected instead of going negative");
        helper.assertTrue(SteamWirelessNetworkManager.getUserSteam(helper.getLevel(), owner) == 600L,
                "a rejected overdraft must leave the balance untouched");
        helper.assertTrue(SteamWirelessNetworkManager.addSteamToGlobalSteamMap(helper.getLevel(), owner, -600L),
                "an atomic subtract down to zero must succeed");
        helper.assertTrue(SteamWirelessNetworkManager.getUserSteam(helper.getLevel(), owner) == 0L,
                "the network must reach exactly zero after subtracting its whole balance");
        helper.assertTrue(!SteamWirelessNetworkManager.addSteamToGlobalSteamMap(helper.getLevel(), owner, -1L),
                "subtracting below zero must be rejected atomically");
        helper.assertTrue(SteamWirelessNetworkManager.getUserSteam(helper.getLevel(), owner) == 0L,
                "a rejected subtract must leave the balance untouched");

        // Round trip through the real hatches: an output hatch pushes its tank into the network and
        // an input hatch pulls it back, with no loss. Both must share the placer's owner UUID (the
        // network key). This is the "input hatch reports no steam" path, exercised end to end.
        BlockPos outputPos = new BlockPos(2, 8, 8);
        helper.setBlock(outputPos, GTNAMachines.WIRELESS_STEAM_OUTPUT_HATCH.getBlock());
        if (!(metaMachineAt(helper, outputPos) instanceof WirelessSteamOutputHatch outputHatch)) {
            helper.fail("the wireless steam output hatch block entity is not a WirelessSteamOutputHatch");
            return;
        }
        if (!(metaMachineAt(helper, wirelessPos) instanceof WirelessSteamInputHatch inputHatch)) {
            helper.fail("the wireless steam input hatch block entity is not a WirelessSteamInputHatch");
            return;
        }
        outputHatch.setOwnerUUID(owner);
        inputHatch.setOwnerUUID(owner);

        // Regression for "a boiler's steam does not enter the network": a boiler dumps a whole
        // recipe cycle into the output hatch at once, so the hatch must move the ENTIRE tank in a
        // single tick. The old brick was a hardcoded bronze cap of 10,000 mB/t (plus a 20,000 mB
        // buffer), which stranded more than 90% of the cycle behind a trickle. The bronze INPUT
        // hatch now has a deliberately small 100,000 mB buffer, so a 312,000 mB push arrives in the
        // network whole but has to be pulled over several rounds; nothing may be lost.
        int cycleSteam = 312_000;
        outputHatch.tank.setFluidInTank(0, GTMaterials.Steam.getFluid(cycleSteam));
        outputHatch.serverTick();
        long afterPush = SteamWirelessNetworkManager.getUserSteam(helper.getLevel(), owner);
        helper.assertTrue(afterPush == cycleSteam,
                "the output hatch must move its whole tank into the network in one tick (got " + afterPush +
                        " of " + cycleSteam + "); a per-tick cap below the buffer strands a boiler cycle");
        helper.assertTrue(outputHatch.tank.getFluidInTank(0).isEmpty(),
                "the output hatch tank must be fully drained into the network");

        // The input pulls only what its buffer holds; free it like the receiving machine consuming
        // the steam and keep pulling until the network is empty.
        long inputCapacity = inputHatch.tank.getTankCapacity(0);
        long totalPulled = 0;
        for (int round = 0; round < 8 &&
                SteamWirelessNetworkManager.getUserSteam(helper.getLevel(), owner) > 0; round++) {
            inputHatch.tank.setFluidInTank(0, FluidStack.EMPTY);
            inputHatch.serverTick();
            long got = inputHatch.tank.getFluidInTank(0).getAmount();
            helper.assertTrue(got <= inputCapacity,
                    "an input hatch can never hold more than its buffer (" + inputCapacity + "), got " + got);
            totalPulled += got;
        }
        helper.assertTrue(totalPulled == cycleSteam,
                "every drop of the boiler cycle must reach the input over the rounds, got " + totalPulled);
        helper.assertTrue(SteamWirelessNetworkManager.getUserSteam(helper.getLevel(), owner) == 0L,
                "the network must be empty once the input pulled the whole cycle");

        // The same network must survive back-to-back cycles without losing or duplicating a drop.
        int smallCycle = 96_000;
        for (int cycle = 0; cycle < 4; cycle++) {
            outputHatch.tank.setFluidInTank(0, GTMaterials.Steam.getFluid(smallCycle));
            outputHatch.serverTick();
            helper.assertTrue(SteamWirelessNetworkManager.getUserSteam(helper.getLevel(), owner) == smallCycle,
                    "cycle " + cycle + ": the output hatch must add the whole tank to the network");
            inputHatch.tank.setFluidInTank(0, FluidStack.EMPTY);
            inputHatch.serverTick();
            helper.assertTrue(inputHatch.tank.getFluidInTank(0).getAmount() == smallCycle,
                    "cycle " + cycle + ": the input hatch must pull the whole network balance back");
            helper.assertTrue(SteamWirelessNetworkManager.getUserSteam(helper.getLevel(), owner) == 0L,
                    "cycle " + cycle + ": the network must be empty after the pull");
        }
        helper.succeed();
    }

    /**
     * The core of the "network stuck at 0 mB, 24 inputs never fill" report: with several input
     * hatches on one network, no single hatch may drain the whole pool in a tick. The first hatch
     * in tick order used to take everything (it requested the whole balance), so the pool always
     * read 0 and every other machine starved. The input hatch must split the balance over the
     * inputs that still have space, and every drop must be conserved across the round trip.
     */
    @GameTest(template = TEMPLATE, timeoutTicks = 40)
    public static void wirelessSteamDistributesAcrossManyInputs(GameTestHelper helper) {
        if (GTNAMachines.WIRELESS_STEAM_INPUT_HATCH == null || GTNAMachines.WIRELESS_STEAM_OUTPUT_HATCH == null) {
            helper.fail("the wireless steam hatches are disabled by config; the distribution test cannot run");
            return;
        }
        UUID owner = UUID.randomUUID();
        int pushed = 312_000;
        int inputCount = 5;

        BlockPos outputPos = new BlockPos(1, 1, 1);
        helper.setBlock(outputPos, GTNAMachines.WIRELESS_STEAM_OUTPUT_HATCH.getBlock());
        if (!(metaMachineAt(helper, outputPos) instanceof WirelessSteamOutputHatch outputHatch)) {
            helper.fail("the wireless steam output hatch block entity is not a WirelessSteamOutputHatch");
            return;
        }
        outputHatch.setOwnerUUID(owner);

        WirelessSteamInputHatch[] inputs = new WirelessSteamInputHatch[inputCount];
        for (int i = 0; i < inputCount; i++) {
            BlockPos pos = new BlockPos(3 + i, 1, 1);
            helper.setBlock(pos, GTNAMachines.WIRELESS_STEAM_INPUT_HATCH.getBlock());
            if (!(metaMachineAt(helper, pos) instanceof WirelessSteamInputHatch inputHatch)) {
                helper.fail("the wireless steam input hatch block entity is not a WirelessSteamInputHatch");
                return;
            }
            inputHatch.setOwnerUUID(owner);
            inputs[i] = inputHatch;
        }

        // Warm the runtime registry the way a running server does: every hatch reports once before
        // the pool is funded. Without this the very first tick has only one registered hatch, so the
        // fair-share denominator cannot see its peers yet (the load-time blip, harmless in game
        // because every later tick has the full registry).
        for (WirelessSteamInputHatch input : inputs) {
            input.serverTick();
        }

        outputHatch.tank.setFluidInTank(0, GTMaterials.Steam.getFluid(pushed));
        outputHatch.serverTick();
        helper.assertTrue(SteamWirelessNetworkManager.getUserSteam(helper.getLevel(), owner) == pushed,
                "the output hatch must push its whole tank into the network first");

        // One round: every input is ticked exactly once, in a fixed order. The old winner-takes-all
        // pull made inputs[0] swallow the whole 312,000 and leave the rest at 0.
        long singlePassLimit = (pushed + inputCount - 1L) / inputCount;
        long pulledFirstPass = 0;
        for (WirelessSteamInputHatch input : inputs) {
            long before = input.tank.getFluidInTank(0).getAmount();
            input.serverTick();
            long got = input.tank.getFluidInTank(0).getAmount() - before;
            helper.assertTrue(got <= singlePassLimit,
                    "an input hatch pulled " + got + " mB in one pass, more than its fair share of " +
                            singlePassLimit + " mB; one hatch is monopolising the network again");
            pulledFirstPass += got;
        }
        helper.assertTrue(pulledFirstPass > 0, "no input hatch pulled anything from a funded network");
        helper.assertTrue(pulledFirstPass < pushed,
                "a single pass drained the whole pool (" + pulledFirstPass + " of " + pushed +
                        "), which is the winner-takes-all behaviour");
        helper.assertTrue(
                SteamWirelessNetworkManager.getUserSteam(helper.getLevel(), owner) == pushed - pulledFirstPass,
                "the network balance must drop by exactly what the inputs pulled");

        // Subsequent passes must converge to an empty network with every drop accounted for.
        for (int pass = 0; pass < 12; pass++) {
            for (WirelessSteamInputHatch input : inputs) {
                input.serverTick();
            }
        }
        long inHatches = 0;
        for (WirelessSteamInputHatch input : inputs) {
            inHatches += input.tank.getFluidInTank(0).getAmount();
        }
        helper.assertTrue(inHatches == pushed,
                "every drop must end up in an input tank: expected " + pushed + ", got " + inHatches);
        helper.assertTrue(SteamWirelessNetworkManager.getUserSteam(helper.getLevel(), owner) == 0L,
                "the network must be empty once every input has pulled its share");

        // The runtime registry behind /gtna steam must list every hatch with its live tank level.
        var connections = SteamWirelessNetworkManager.getConnections(helper.getLevel(), owner);
        helper.assertTrue(connections.size() == inputCount + 1,
                "the inspection registry must list every connected hatch, got " + connections.size());
        long reportedInputs = connections.stream().filter(c -> c.isInput).count();
        helper.assertTrue(reportedInputs == inputCount,
                "the inspection registry must report all " + inputCount + " inputs, got " + reportedInputs);
        helper.assertTrue(connections.stream().anyMatch(c -> !c.isInput && c.tankAmount == 0),
                "the inspection registry must report the output hatch with its (empty) tank");
        helper.succeed();
    }

    /**
     * A full input hatch must not dilute the fair share of the ones that still have space, and it
     * must never void steam: it pulls nothing and the pool keeps every drop for the other inputs.
     */
    @GameTest(template = TEMPLATE, timeoutTicks = 40)
    public static void wirelessSteamFullInputDoesNotDiluteOrVoid(GameTestHelper helper) {
        if (GTNAMachines.WIRELESS_STEAM_INPUT_HATCH == null || GTNAMachines.WIRELESS_STEAM_OUTPUT_HATCH == null) {
            helper.fail("the wireless steam hatches are disabled by config; the no-void test cannot run");
            return;
        }
        UUID owner = UUID.randomUUID();
        long pushed = 100_000L;

        BlockPos outputPos = new BlockPos(1, 3, 1);
        helper.setBlock(outputPos, GTNAMachines.WIRELESS_STEAM_OUTPUT_HATCH.getBlock());
        if (!(metaMachineAt(helper, outputPos) instanceof WirelessSteamOutputHatch outputHatch)) {
            helper.fail("the wireless steam output hatch block entity is not a WirelessSteamOutputHatch");
            return;
        }
        outputHatch.setOwnerUUID(owner);

        BlockPos fullPos = new BlockPos(3, 3, 1);
        helper.setBlock(fullPos, GTNAMachines.WIRELESS_STEAM_INPUT_HATCH.getBlock());
        if (!(metaMachineAt(helper, fullPos) instanceof WirelessSteamInputHatch fullInput)) {
            helper.fail("the wireless steam input hatch block entity is not a WirelessSteamInputHatch");
            return;
        }
        fullInput.setOwnerUUID(owner);
        long fullCapacity = fullInput.tank.getTankCapacity(0);
        fullInput.tank.setFluidInTank(0, GTMaterials.Steam.getFluid((int) fullCapacity));

        BlockPos emptyPos = new BlockPos(5, 3, 1);
        helper.setBlock(emptyPos, GTNAMachines.WIRELESS_STEAM_INPUT_HATCH.getBlock());
        if (!(metaMachineAt(helper, emptyPos) instanceof WirelessSteamInputHatch emptyInput)) {
            helper.fail("the wireless steam input hatch block entity is not a WirelessSteamInputHatch");
            return;
        }
        emptyInput.setOwnerUUID(owner);

        outputHatch.tank.setFluidInTank(0, GTMaterials.Steam.getFluid((int) pushed));
        outputHatch.serverTick();
        helper.assertTrue(SteamWirelessNetworkManager.getUserSteam(helper.getLevel(), owner) == pushed,
                "the output hatch must push its whole tank into the network");

        // A full hatch has no space: it must pull nothing and must not swallow a share either.
        fullInput.serverTick();
        helper.assertTrue(fullInput.tank.getFluidInTank(0).getAmount() == fullCapacity,
                "a full input hatch must keep its tank untouched (no void, no duplication)");
        helper.assertTrue(SteamWirelessNetworkManager.getUserSteam(helper.getLevel(), owner) == pushed,
                "a full input hatch must not consume from the network; the balance must stay " + pushed);

        // The only input with space therefore gets the whole pool.
        emptyInput.serverTick();
        helper.assertTrue(emptyInput.tank.getFluidInTank(0).getAmount() == pushed,
                "the only input with space must receive the whole balance, got " +
                        emptyInput.tank.getFluidInTank(0).getAmount());
        helper.assertTrue(SteamWirelessNetworkManager.getUserSteam(helper.getLevel(), owner) == 0L,
                "the network must be empty after the only available input pulled it");
        helper.succeed();
    }

    /**
     * Wiring regression for the natural server tick: the hatches must feed each other through
     * {@code onLoad}'s tick subscription, not only through a manual {@code serverTick()} call in a
     * test. The pool may read 0 between ticks (that is the pass-through design) but the steam must
     * land in the input tank within a few ticks.
     */
    @GameTest(template = TEMPLATE, timeoutTicks = 60)
    public static void wirelessSteamFeedsOnNaturalServerTick(GameTestHelper helper) {
        if (GTNAMachines.WIRELESS_STEAM_INPUT_HATCH == null || GTNAMachines.WIRELESS_STEAM_OUTPUT_HATCH == null) {
            helper.fail("the wireless steam hatches are disabled by config; the natural tick test cannot run");
            return;
        }
        UUID owner = UUID.randomUUID();
        int pushed = 96_000;

        BlockPos outputPos = new BlockPos(1, 5, 1);
        helper.setBlock(outputPos, GTNAMachines.WIRELESS_STEAM_OUTPUT_HATCH.getBlock());
        if (!(metaMachineAt(helper, outputPos) instanceof WirelessSteamOutputHatch outputHatch)) {
            helper.fail("the wireless steam output hatch block entity is not a WirelessSteamOutputHatch");
            return;
        }
        outputHatch.setOwnerUUID(owner);

        BlockPos inputPos = new BlockPos(3, 5, 1);
        helper.setBlock(inputPos, GTNAMachines.WIRELESS_STEAM_INPUT_HATCH.getBlock());
        if (!(metaMachineAt(helper, inputPos) instanceof WirelessSteamInputHatch inputHatch)) {
            helper.fail("the wireless steam input hatch block entity is not a WirelessSteamInputHatch");
            return;
        }
        inputHatch.setOwnerUUID(owner);
        outputHatch.tank.setFluidInTank(0, GTMaterials.Steam.getFluid(pushed));

        helper.runAfterDelay(10, () -> {
            long inTank = inputHatch.tank.getFluidInTank(0).getAmount();
            helper.assertTrue(inTank == pushed,
                    "after 10 natural server ticks the input hatch must hold the pushed steam, got " + inTank);
            helper.assertTrue(outputHatch.tank.getFluidInTank(0).isEmpty(),
                    "the output hatch must have emptied into the network");
            helper.assertTrue(SteamWirelessNetworkManager.getUserSteam(helper.getLevel(), owner) == 0L,
                    "the network must be empty once the input pulled the steam");
            helper.succeed();
        });
    }

    /**
     * Runtime coverage for the server half of the wireless steam HUD (GTOCore {@code
     * WirelessEnergyHUD} parity): the snapshot sent once per second must report the pool balance,
     * the connected hatch counts and the flow since the previous sample, with the lifetime
     * counters converted to per-second deltas.
     */
    @GameTest(template = TEMPLATE, timeoutTicks = 40)
    public static void wirelessSteamHudSnapshotReportsNetworkState(GameTestHelper helper) {
        if (GTNAMachines.WIRELESS_STEAM_INPUT_HATCH == null || GTNAMachines.WIRELESS_STEAM_OUTPUT_HATCH == null) {
            helper.fail("the wireless steam hatches are disabled by config; the HUD snapshot test cannot run");
            return;
        }
        UUID owner = UUID.randomUUID();
        int pushed = 96_000;

        BlockPos outputPos = new BlockPos(1, 7, 1);
        helper.setBlock(outputPos, GTNAMachines.WIRELESS_STEAM_OUTPUT_HATCH.getBlock());
        if (!(metaMachineAt(helper, outputPos) instanceof WirelessSteamOutputHatch outputHatch)) {
            helper.fail("the wireless steam output hatch block entity is not a WirelessSteamOutputHatch");
            return;
        }
        outputHatch.setOwnerUUID(owner);

        BlockPos inputPos = new BlockPos(3, 7, 1);
        helper.setBlock(inputPos, GTNAMachines.WIRELESS_STEAM_INPUT_HATCH.getBlock());
        if (!(metaMachineAt(helper, inputPos) instanceof WirelessSteamInputHatch inputHatch)) {
            helper.fail("the wireless steam input hatch block entity is not a WirelessSteamInputHatch");
            return;
        }
        inputHatch.setOwnerUUID(owner);

        // Register both hatches (owner was set after placement, so onLoad could not report yet) and
        // take the baseline sample the delta conversion needs.
        outputHatch.serverTick();
        inputHatch.serverTick();
        SWirelessSteamStats baseline = WirelessSteamHudSync.snapshot(helper.getLevel(), owner);
        helper.assertTrue(baseline.getBalance() == 0L && baseline.getAddedPerSecond() == 0L &&
                baseline.getConsumedPerSecond() == 0L, "a fresh network must snapshot as all zeros");
        helper.assertTrue(baseline.getInputHatches() == 1 && baseline.getOutputHatches() == 1,
                "the snapshot must count the connected input and output hatches, got " +
                        baseline.getInputHatches() + " in / " + baseline.getOutputHatches() + " out");

        outputHatch.tank.setFluidInTank(0, GTMaterials.Steam.getFluid(pushed));
        outputHatch.serverTick();
        SWirelessSteamStats afterPush = WirelessSteamHudSync.snapshot(helper.getLevel(), owner);
        helper.assertTrue(afterPush.getBalance() == pushed,
                "the HUD snapshot must report the pool balance, got " + afterPush.getBalance());
        helper.assertTrue(afterPush.getAddedPerSecond() == pushed,
                "the HUD snapshot must report what the output pushed since the last sample, got " +
                        afterPush.getAddedPerSecond());
        helper.assertTrue(afterPush.getConsumedPerSecond() == 0L,
                "nothing was consumed yet, got " + afterPush.getConsumedPerSecond());

        inputHatch.serverTick();
        SWirelessSteamStats afterPull = WirelessSteamHudSync.snapshot(helper.getLevel(), owner);
        helper.assertTrue(afterPull.getBalance() == 0L,
                "the HUD snapshot must report the emptied pool, got " + afterPull.getBalance());
        helper.assertTrue(afterPull.getConsumedPerSecond() == pushed,
                "the HUD snapshot must report what the input pulled since the last sample, got " +
                        afterPull.getConsumedPerSecond());
        helper.assertTrue(afterPull.getAddedPerSecond() == 0L,
                "the push already happened in the previous interval, got " + afterPull.getAddedPerSecond());
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 20)
    public static void steamCasingTiers(GameTestHelper helper) {
        helper.assertTrue(SteamMultiMachineBase.casingTier(GTBlocks.CASING_BRONZE_BRICKS.get().defaultBlockState()) ==
                SteamMultiMachineBase.BRONZE_TIER, "bronze plated bricks must be tier 1");
        helper.assertTrue(SteamMultiMachineBase.casingTier(GTBlocks.CASING_STEEL_SOLID.get().defaultBlockState()) ==
                SteamMultiMachineBase.STEEL_TIER, "solid steel casing must be tier 2");
        helper.assertTrue(
                SteamMultiMachineBase.casingTier(GTNABlocks.INDUSTRIAL_STEAM_CASING.get().defaultBlockState()) ==
                        SteamMultiMachineBase.BRONZE_TIER,
                "the ported industrial steam casing must be tier 1");
        helper.assertTrue(
                SteamMultiMachineBase.casingTier(GTNABlocks.ADVANCED_INDUSTRIAL_STEAM_CASING.get()
                        .defaultBlockState()) == SteamMultiMachineBase.STEEL_TIER,
                "the ported advanced industrial steam casing must be tier 2");
        helper.assertTrue(SteamMultiMachineBase.casingTier(Blocks.STONE.defaultBlockState()) == -1,
                "a non-casing block must not have a casing tier");
        helper.succeed();
    }

    /**
     * High pressure mode (GTNL {@code SteamMultiMachineBase#isHighPressure}, {@code tierMachine == 2}):
     * the same structure built with <b>advanced industrial steam casings</b> instead of bronze must
     * form, report high pressure and double the steam consumption. This is the runtime half of the
     * tier-aware casing predicate ({@code SteamMultiMachineBase.casing()}).
     */
    @GameTest(template = TEMPLATE, timeoutTicks = 40)
    public static void steelCasingEnablesHighPressure(GameTestHelper helper) {
        MultiblockMachineDefinition definition = GTNAMachines.LARGE_STEAM_ALLOY_SMELTER;
        if (definition == null) {
            helper.fail("large_steam_alloy_smelter is disabled by config; the high pressure test cannot run");
            return;
        }
        // Fresh quadrant, wiped before building.
        BlockPos controllerPos = new BlockPos(8, 8, 8);
        clearArea(helper, controllerPos);
        helper.setBlock(controllerPos, definition.getBlock());

        // Same axis mapping as the wireless test; the steam hatch takes over one 'A' cell.
        BlockPos steamPos = controllerPos.offset(1, 0, 0);
        for (int aisle = 0; aisle < 3; aisle++) {
            for (int string = 0; string < 4; string++) {
                for (int charX = 0; charX < 3; charX++) {
                    BlockPos pos = controllerPos.offset(1 - charX, string - 1, 2 - aisle);
                    if (pos.equals(steamPos)) {
                        continue;
                    }
                    switch (LARGE_STEAM_ALLOY_SMELTER_PATTERN[aisle][string].charAt(charX)) {
                        case 'B' -> helper.setBlock(pos, GTBlocks.FIREBOX_STEEL.get());
                        case 'A' -> helper.setBlock(pos, GTBlocks.CASING_STEEL_SOLID.get());
                        default -> {
                            // '~' (controller, already placed) and space (any).
                        }
                    }
                }
            }
        }
        helper.setBlock(steamPos, GTMachines.STEAM_HATCH.getBlock());

        MetaMachine placed = metaMachineAt(helper, controllerPos);
        if (!(placed instanceof SteamMultiMachineBase controller)) {
            helper.fail("large_steam_alloy_smelter block entity is not a SteamMultiMachineBase, got " + placed);
            return;
        }
        MultiblockState state = controller.getMultiblockState();
        if (!controller.getPattern().checkPatternAt(state, false)) {
            helper.fail("a steam multiblock must accept steel casings in its bronze casing slots: " +
                    patternError(helper, state, controller.self().getPos()));
            return;
        }
        controller.onStructureFormed();
        helper.assertTrue(controller.isFormed(),
                "the steel-cased structure must form and find the steam hatch as its steam source");
        helper.assertTrue(controller.isHighPressure(),
                "a fully steel-cased structure must put the machine in high pressure mode");
        helper.assertTrue(controller.getEffectiveConversionRate() == 2.0,
                "high pressure mode must double the steam consumption (conversion rate 2.0)");
        helper.succeed();
    }

    /**
     * Locks the Thread Hatch foundation: a multiblock on the {@code WorkableElectricMultipleRecipesMachine}
     * base must accept the hatch by ability and wire its thread count into the recipe logic.
     *
     * <p>
     * The port manifest (rule 8) wants every GTNA controller migrated onto this base; today the
     * duration_tester is the only registered one, so the feature is foundation-only outside KubeJS.
     * This test keeps the foundation from regressing while the migration (manifest delivery step 2)
     * is pending: it proves the ability is accepted, {@code addedToController} hands the part to the
     * machine, and {@code GTNAMultipleRecipesLogic.getMaxThreads()} follows the hatch tier.
     */
    @GameTest(template = TEMPLATE, timeoutTicks = 40)
    public static void threadHatchWiresIntoMultipleRecipesMachine(GameTestHelper helper) {
        if (GTNAMachines2.DURATION_TESTER == null) {
            helper.fail("duration_tester is disabled by config; the thread hatch foundation cannot be tested");
            return;
        }
        if (GTNAMachines2.THREAD_HATCHES[GTValues.ZPM] == null) {
            helper.fail("thread_hatch_zpm is disabled by config; the thread hatch test cannot run");
            return;
        }
        // Fourth area: disjoint from the other structure tests in every axis.
        BlockPos controllerPos = new BlockPos(8, 8, 2);
        clearArea(helper, controllerPos);
        buildDurationTester(helper, controllerPos);
        // Swap one shell casing for the thread hatch (pattern cell char 0, string 2, aisle 2).
        helper.setBlock(controllerPos.offset(1, 1, 0), GTNAMachines2.THREAD_HATCHES[GTValues.ZPM].getBlock());

        MetaMachine placed = metaMachineAt(helper, controllerPos);
        if (!(placed instanceof WorkableElectricMultipleRecipesMachine controller)) {
            helper.fail("duration_tester block entity is not a WorkableElectricMultipleRecipesMachine, got " + placed);
            return;
        }
        MultiblockState state = controller.getMultiblockState();
        if (!controller.getPattern().checkPatternAt(state, false)) {
            helper.fail("a multiple-recipes machine must accept the thread hatch: " +
                    patternError(helper, state, controller.self().getPos()));
            return;
        }
        controller.onStructureFormed();
        var threadPart = controller.getThreadPartMachine();
        helper.assertTrue(threadPart != null,
                "onStructureFormed must hand the thread hatch to the machine via addedToController");
        int expected = 1 + threadPart.getThreadCount();
        helper.assertTrue(controller.getRecipeLogic().getMaxThreads() >= expected,
                "the thread hatch (tier ZPM, +" + threadPart.getThreadCount() +
                        ") must raise max threads to at least " + expected +
                        ", got " + controller.getRecipeLogic().getMaxThreads());
        helper.succeed();
    }

    /**
     * Regression for the output-boost double application (M -> M^2) on the multiple-recipes base:
     * completes a real recipe and asserts the output bus holds exactly one multiplier's worth of
     * items - not its square. The {@code RecipeHelperMixin} (match + execution) is the single source
     * of truth; {@code GTNAMultipleRecipesLogic} must not apply it again.
     */
    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void outputBoostAppliesOnceOnMultipleRecipesMachine(GameTestHelper helper) {
        if (GTNAMachines2.DURATION_TESTER == null) {
            helper.fail("duration_tester is disabled by config; the output boost test cannot run");
            return;
        }
        if (GTNAMachines2.OUTPUT_BOOST_HATCHES[GTValues.LV] == null) {
            helper.fail("output_boost_hatch_lv is disabled by config; the output boost test cannot run");
            return;
        }
        injectAssemblerRecipe();

        // Another disjoint area: the only free quadrant is high in Z here.
        BlockPos controllerPos = new BlockPos(2, 2, 8);
        clearArea(helper, controllerPos);
        buildDurationTester(helper, controllerPos);
        BlockPos boostPos = controllerPos.offset(1, 1, 0);
        helper.setBlock(boostPos, GTNAMachines2.OUTPUT_BOOST_HATCHES[GTValues.LV].getBlock());

        MetaMachine placed = metaMachineAt(helper, controllerPos);
        if (!(placed instanceof WorkableElectricMultipleRecipesMachine controller)) {
            helper.fail("duration_tester block entity is not a WorkableElectricMultipleRecipesMachine, got " + placed);
            return;
        }
        MultiblockState state = controller.getMultiblockState();
        if (!controller.getPattern().checkPatternAt(state, false)) {
            helper.fail("duration_tester pattern did not match with the output boost hatch: " +
                    patternError(helper, state, controller.self().getPos()));
            return;
        }
        controller.onStructureFormed();

        if (!(metaMachineAt(helper, boostPos) instanceof OutputBoostHatchPartMachine boostHatch)) {
            helper.fail("no OutputBoostHatchPartMachine in the structure");
            return;
        }
        int multiplier = boostHatch.getOutputMultiplier();
        helper.assertTrue(multiplier > 1, "the LV output boost hatch must have a multiplier above 1");

        EnergyHatchPartMachine energyHatch = (EnergyHatchPartMachine) metaMachineAt(helper,
                controllerPos.offset(-1, -1, 2));
        IEnergyContainer energy = energyHatch.energyContainer;
        long target = Math.min(energy.getEnergyCapacity(), 1_000_000L);
        for (int attempt = 0; attempt < 8 && energy.getEnergyStored() < target; attempt++) {
            energy.addEnergy(target - energy.getEnergyStored());
        }

        BlockPos outputPos = controllerPos.offset(1, -1, 2);
        ItemBusPartMachine inputBus = (ItemBusPartMachine) metaMachineAt(helper, controllerPos.offset(0, -1, 2));
        inputBus.getInventory().insertItem(0, new ItemStack(Items.NETHER_STAR, 1), false);

        GTNAMultipleRecipesLogic logic = controller.getRecipeLogic();
        boolean everActive = false;
        for (int tick = 0; tick < 60; tick++) {
            logic.serverTick();
            if (logic.getActiveRecipeCount() > 0) {
                everActive = true;
            } else if (everActive) {
                break;
            }
        }
        helper.assertTrue(everActive, "the injected assembler recipe never started");
        helper.assertTrue(logic.getActiveRecipeCount() == 0, "the injected assembler recipe never finished");

        ItemBusPartMachine outputBus = (ItemBusPartMachine) metaMachineAt(helper, outputPos);
        int stone = 0;
        for (int slot = 0; slot < outputBus.getInventory().getSlots(); slot++) {
            ItemStack stack = outputBus.getInventory().getStackInSlot(slot);
            if (stack.is(Items.STONE)) {
                stone += stack.getCount();
            }
        }
        helper.assertTrue(stone == multiplier,
                "output boost must apply exactly once: expected " + multiplier + " stone, got " + stone +
                        (stone == multiplier * multiplier ?
                                " (that is the multiplier SQUARED - the double application is back)" : ""));
        helper.succeed();
    }

    /**
     * Smoke test for the GTLsupb Universal Factory port: it must register with 32 recipe types and
     * its 3x3x3 casing structure (with the mandatory maintenance hatch) must match.
     */
    @GameTest(template = TEMPLATE, timeoutTicks = 40)
    public static void universalFactoryFormsAndExposesRecipeTypes(GameTestHelper helper) {
        MultiblockMachineDefinition definition = GTNAMachines.UNIVERSAL_FACTORY;
        if (definition == null) {
            helper.fail("universal_factory is disabled by config");
            return;
        }
        helper.assertTrue(definition.getRecipeTypes().length == 32,
                "universal_factory must expose 32 recipe types, got " + definition.getRecipeTypes().length);

        BlockPos controllerPos = new BlockPos(8, 2, 2);
        clearArea(helper, controllerPos);
        helper.setBlock(controllerPos, definition.getBlock());
        for (int aisle = 0; aisle < 3; aisle++) {
            for (int string = 0; string < 3; string++) {
                for (int charX = 0; charX < 3; charX++) {
                    BlockPos pos = controllerPos.offset(1 - charX, string - 1, 2 - aisle);
                    switch (UNIVERSAL_FACTORY_PATTERN[aisle][string].charAt(charX)) {
                        case 'A' -> helper.setBlock(pos, GTNABlocks.UNIVERSAL_FACTORY_CASING.get());
                        case 'B' -> helper.setBlock(pos,
                                ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.Steel));
                        default -> {
                            // '~' controller, already placed
                        }
                    }
                }
            }
        }
        // Mandatory maintenance hatch (min AND max 1) plus an energy hatch and item buses.
        helper.setBlock(controllerPos.offset(1, 0, 2), GTMachines.MAINTENANCE_HATCH.getBlock());
        helper.setBlock(controllerPos.offset(-1, 0, 2), GTMachines.ENERGY_INPUT_HATCH[GTValues.LuV].getBlock());
        helper.setBlock(controllerPos.offset(1, -1, 2), GTMachines.ITEM_IMPORT_BUS[GTValues.LV].getBlock());
        helper.setBlock(controllerPos.offset(0, -1, 2), GTMachines.ITEM_EXPORT_BUS[GTValues.LV].getBlock());

        MetaMachine placed = metaMachineAt(helper, controllerPos);
        if (!(placed instanceof UniversalFactoryMachine controller)) {
            helper.fail("universal_factory block entity is not a UniversalFactoryMachine, got " + placed);
            return;
        }
        MultiblockState state = controller.getMultiblockState();
        if (!controller.getPattern().checkPatternAt(state, false)) {
            helper.fail("universal_factory pattern did not match: " +
                    patternError(helper, state, controller.self().getPos()));
            return;
        }
        controller.onStructureFormed();
        helper.assertTrue(controller.isFormed(), "universal_factory must remain formed");
        helper.assertTrue(controller.getDynamicThreads() > 1,
                "the universal factory must scale threads with the voltage tier, got " +
                        controller.getDynamicThreads());
        helper.succeed();
    }

    /** Builds the Universal Factory 3x3x3 (energy + buses, optionally the maintenance hatch). */
    private static UniversalFactoryMachine buildUniversalFactory(GameTestHelper helper, BlockPos controllerPos,
                                                                 boolean withMaintenance) {
        MultiblockMachineDefinition definition = GTNAMachines.UNIVERSAL_FACTORY;
        helper.setBlock(controllerPos, definition.getBlock());
        for (int aisle = 0; aisle < 3; aisle++) {
            for (int string = 0; string < 3; string++) {
                for (int charX = 0; charX < 3; charX++) {
                    BlockPos pos = controllerPos.offset(1 - charX, string - 1, 2 - aisle);
                    switch (UNIVERSAL_FACTORY_PATTERN[aisle][string].charAt(charX)) {
                        case 'A' -> helper.setBlock(pos, GTNABlocks.UNIVERSAL_FACTORY_CASING.get());
                        case 'B' -> helper.setBlock(pos,
                                ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.Steel));
                        default -> {
                            // '~' controller, already placed
                        }
                    }
                }
            }
        }
        helper.setBlock(controllerPos.offset(-1, 0, 2), GTMachines.ENERGY_INPUT_HATCH[GTValues.LuV].getBlock());
        helper.setBlock(controllerPos.offset(1, -1, 2), GTMachines.ITEM_IMPORT_BUS[GTValues.LV].getBlock());
        helper.setBlock(controllerPos.offset(0, -1, 2), GTMachines.ITEM_EXPORT_BUS[GTValues.LV].getBlock());
        if (withMaintenance) {
            helper.setBlock(controllerPos.offset(1, 0, 2), GTMachines.MAINTENANCE_HATCH.getBlock());
        }
        return metaMachineAt(helper, controllerPos) instanceof UniversalFactoryMachine machine ? machine : null;
    }

    /** Repeated requests and concurrent recipe types must consume only their encoded patterns. */
    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void universalFactoryProcessesRepeatedPatternFromAnotherMode(GameTestHelper helper) {
        if (GTNAMachines.UNIVERSAL_FACTORY == null || GTNAMachines2.ME_PATTERN_BUFFER == null) {
            helper.fail("universal_factory and me_pattern_buffer must be enabled");
            return;
        }
        injectCircuitAssemblerRecipe();
        BlockPos controllerPos = new BlockPos(8, 8, 8);
        clearArea(helper, controllerPos);
        UniversalFactoryMachine controller = buildUniversalFactory(helper, controllerPos, true);
        BlockPos bufferPos = controllerPos.offset(1, -1, 2);
        helper.setBlock(bufferPos, GTNAMachines2.ME_PATTERN_BUFFER.getBlock());
        helper.assertTrue(controller != null, "universal_factory controller must exist");
        MultiblockState state = controller.getMultiblockState();
        helper.assertTrue(controller.getPattern().checkPatternAt(state, false),
                "universal_factory pattern did not match: " + patternError(helper, state, controllerPos));
        controller.onStructureFormed();
        helper.assertTrue(controller.isFormed(), "universal_factory must form with ME Pattern Buffer");
        if (!(metaMachineAt(helper, bufferPos) instanceof GTNAMEPatternBufferPartMachine buffer)) {
            helper.fail("ME Pattern Buffer is missing from the formed structure");
            return;
        }
        ItemStack pattern = PatternDetailsHelper.encodeProcessingPattern(
                new GenericStack[] { GenericStack.fromItemStack(new ItemStack(Items.COBBLESTONE)) },
                new GenericStack[] { GenericStack.fromItemStack(new ItemStack(Items.STONE)) });
        buffer.getPatternInventory().setStackInSlot(0, pattern);
        var details = PatternDetailsHelper.decodePattern(pattern, helper.getLevel());
        helper.assertTrue(details != null, "processing pattern must decode");

        EnergyHatchPartMachine energyHatch = (EnergyHatchPartMachine) metaMachineAt(helper,
                controllerPos.offset(-1, 0, 2));
        IEnergyContainer energy = energyHatch.energyContainer;
        energy.addEnergy(Math.min(energy.getEnergyCapacity(), 1_000_000L));
        helper.assertTrue(controller.getEnergyContainer().getEnergyStored() > 0,
                "universal_factory must see the energy hatch");
        ItemBusPartMachine outputBus = (ItemBusPartMachine) metaMachineAt(helper,
                controllerPos.offset(0, -1, 2));

        for (int request = 1; request <= 2; request++) {
            KeyCounter inputs = new KeyCounter();
            inputs.add(AEItemKey.of(Items.COBBLESTONE), 1);
            buffer.getInternalInventory()[0].pushPattern(details, new KeyCounter[] { inputs });
            for (int tick = 0; tick < 20; tick++) {
                controller.getRecipeLogic().serverTick();
            }
            ItemStack output = outputBus.getInventory().getStackInSlot(0);
            helper.assertTrue(output.is(Items.STONE) && output.getCount() == request,
                    "request " + request + " must produce one stone; output=" + output +
                            ", staged=" + buffer.getInternalInventory()[0].getItems());
            helper.assertTrue(controller.getActiveRecipeType() == 13,
                    "request " + request + " must switch from bender to circuit assembler; mode=" +
                            controller.getActiveRecipeType());
        }

        injectFactoryBenderRecipe();
        ItemStack secondPattern = PatternDetailsHelper.encodeProcessingPattern(
                new GenericStack[] { GenericStack.fromItemStack(new ItemStack(Items.NETHER_STAR)) },
                new GenericStack[] { GenericStack.fromItemStack(new ItemStack(Items.EMERALD)) });
        buffer.getPatternInventory().setStackInSlot(1, secondPattern);
        var secondDetails = PatternDetailsHelper.decodePattern(secondPattern, helper.getLevel());
        helper.assertTrue(secondDetails != null, "second processing pattern must decode");
        KeyCounter cobble = new KeyCounter();
        cobble.add(AEItemKey.of(Items.COBBLESTONE), 1);
        KeyCounter star = new KeyCounter();
        star.add(AEItemKey.of(Items.NETHER_STAR), 1);
        buffer.getInternalInventory()[0].pushPattern(details, new KeyCounter[] { cobble });
        buffer.getInternalInventory()[1].pushPattern(secondDetails, new KeyCounter[] { star });
        controller.getRecipeLogic().serverTick();
        helper.assertTrue(controller.getRecipeLogic().getActiveRecipeCount() == 2,
                "both distinct recipe types must start concurrently in separate threads");
        for (int tick = 0; tick < 20; tick++) {
            controller.getRecipeLogic().serverTick();
        }
        int stone = 0;
        int emerald = 0;
        for (int slot = 0; slot < outputBus.getInventory().getSlots(); slot++) {
            ItemStack output = outputBus.getInventory().getStackInSlot(slot);
            if (output.is(Items.STONE)) stone += output.getCount();
            if (output.is(Items.EMERALD)) emerald += output.getCount();
        }
        helper.assertTrue(stone == 3 && emerald == 1,
                "concurrent crafts must produce exactly 3 stone and 1 emerald; got " + stone + "/" + emerald);
        helper.succeed();
    }

    /** Three queued AE2 crafts must remain valid when the machine scales one recipe in parallel. */
    @GameTest(template = TEMPLATE, timeoutTicks = 300)
    public static void universalFactoryProcessesAccumulatedLaminatedGlass(GameTestHelper helper) {
        if (GTNAMachines.UNIVERSAL_FACTORY == null || GTNAMachines2.ME_ADVANCED_PATTERN_BUFFER == null) {
            helper.fail("universal_factory and advanced ME Pattern Buffer must be enabled");
            return;
        }
        BlockPos controllerPos = new BlockPos(8, 8, 8);
        clearArea(helper, controllerPos);
        UniversalFactoryMachine controller = buildUniversalFactory(helper, controllerPos, true);
        BlockPos bufferPos = controllerPos.offset(1, -1, 2);
        helper.setBlock(bufferPos, GTNAMachines2.ME_ADVANCED_PATTERN_BUFFER.getBlock());
        helper.assertTrue(controller != null, "universal_factory controller must exist");
        MultiblockState state = controller.getMultiblockState();
        helper.assertTrue(controller.getPattern().checkPatternAt(state, false),
                "universal_factory pattern did not match: " + patternError(helper, state, controllerPos));
        controller.onStructureFormed();
        if (!(metaMachineAt(helper, bufferPos) instanceof GTNAMEPatternBufferPartMachine buffer)) {
            helper.fail("advanced ME Pattern Buffer is missing from the structure");
            return;
        }
        ItemStack glass = GTBlocks.CASING_TEMPERED_GLASS.asStack(2);
        ItemStack pvb = ChemicalHelper.get(TagPrefix.plate, GTMaterials.PolyvinylButyral);
        ItemStack laminated = GTBlocks.CASING_LAMINATED_GLASS.asStack();
        ItemStack pattern = PatternDetailsHelper.encodeProcessingPattern(
                new GenericStack[] { GenericStack.fromItemStack(glass), GenericStack.fromItemStack(pvb) },
                new GenericStack[] { GenericStack.fromItemStack(laminated) });
        buffer.getPatternInventory().setStackInSlot(0, pattern);
        var details = PatternDetailsHelper.decodePattern(pattern, helper.getLevel());
        helper.assertTrue(details != null, "laminated glass pattern must decode");
        buffer.getSlotConfigs()[0].setCachedRecipeId("gtceu:forming_press/laminated_glass");

        EnergyHatchPartMachine energyHatch = (EnergyHatchPartMachine) metaMachineAt(helper,
                controllerPos.offset(-1, 0, 2));
        energyHatch.energyContainer.addEnergy(energyHatch.energyContainer.getEnergyCapacity());
        helper.assertTrue(controller.getEnergyContainer().getEnergyStored() > 0,
                "universal_factory must see the energy hatch");
        ItemBusPartMachine outputBus = (ItemBusPartMachine) metaMachineAt(helper,
                controllerPos.offset(0, -1, 2));

        for (int request = 0; request < 3; request++) {
            KeyCounter inputs = new KeyCounter();
            inputs.add(AEItemKey.of(glass), 2);
            inputs.add(AEItemKey.of(pvb), 1);
            buffer.getInternalInventory()[0].pushPattern(details, new KeyCounter[] { inputs });
        }
        helper.assertTrue(buffer.getInternalInventory()[0].getItems().stream()
                .mapToInt(ItemStack::getCount).sum() == 9,
                "three crafts must stage 6 tempered glass and 3 PVB plates");
        for (int tick = 0; tick < 220; tick++) {
            // The real setup uses a creative energy hatch. Keep this finite test hatch charged so
            // the assertion measures pattern routing and parallel execution, not its capacity.
            energyHatch.energyContainer.addEnergy(energyHatch.energyContainer.getEnergyCapacity() -
                    energyHatch.energyContainer.getEnergyStored());
            controller.getRecipeLogic().serverTick();
        }
        int produced = 0;
        for (int slot = 0; slot < outputBus.getInventory().getSlots(); slot++) {
            ItemStack output = outputBus.getInventory().getStackInSlot(slot);
            if (ItemStack.isSameItemSameTags(output, laminated)) produced += output.getCount();
        }
        helper.assertTrue(produced == 3,
                "three accumulated crafts must produce 3 laminated glass, got " + produced +
                        "; staged=" + buffer.getInternalInventory()[0].getItems() +
                        "; mode=" + controller.getActiveRecipeType() +
                        "; active=" + controller.getRecipeLogic().getActiveRecipeCount() +
                        "; energy=" + controller.getEnergyContainer().getEnergyStored());
        helper.succeed();
    }

    /** Four separate six-step crafts exercise three branches, two joins and repeated type changes. */
    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void universalFactoryProcessesLayeredPatternCrafts(GameTestHelper helper) {
        if (GTNAMachines.UNIVERSAL_FACTORY == null || GTNAMachines2.ME_ADVANCED_PATTERN_BUFFER == null) {
            helper.fail("universal_factory and advanced ME Pattern Buffer must be enabled");
            return;
        }
        GTRecipeType[] types = { GTRecipeTypes.BENDER_RECIPES, GTRecipeTypes.COMPRESSOR_RECIPES,
                GTRecipeTypes.FORGE_HAMMER_RECIPES, GTRecipeTypes.LATHE_RECIPES,
                GTRecipeTypes.CUTTER_RECIPES, GTRecipeTypes.FORMING_PRESS_RECIPES };
        ItemStack[][] inputs = {
                { new ItemStack(Items.DRAGON_BREATH) },
                { new ItemStack(Items.HEART_OF_THE_SEA) },
                { new ItemStack(Items.ECHO_SHARD) },
                { new ItemStack(Items.BLAZE_ROD), new ItemStack(Items.ENDER_PEARL) },
                { new ItemStack(Items.AMETHYST_SHARD) },
                { new ItemStack(Items.DIAMOND), new ItemStack(Items.PRISMARINE_SHARD) }
        };
        ItemStack[] outputs = { new ItemStack(Items.BLAZE_ROD), new ItemStack(Items.ENDER_PEARL),
                new ItemStack(Items.AMETHYST_SHARD), new ItemStack(Items.DIAMOND),
                new ItemStack(Items.PRISMARINE_SHARD), new ItemStack(Items.NETHERITE_INGOT) };
        injectLayeredFactoryRecipes(types, inputs, outputs);

        BlockPos controllerPos = new BlockPos(8, 8, 8);
        clearArea(helper, controllerPos);
        UniversalFactoryMachine controller = buildUniversalFactory(helper, controllerPos, true);
        BlockPos bufferPos = controllerPos.offset(1, -1, 2);
        helper.setBlock(bufferPos, GTNAMachines2.ME_ADVANCED_PATTERN_BUFFER.getBlock());
        helper.assertTrue(controller != null, "universal_factory controller must exist");
        MultiblockState state = controller.getMultiblockState();
        helper.assertTrue(controller.getPattern().checkPatternAt(state, false),
                "universal_factory pattern did not match: " + patternError(helper, state, controllerPos));
        controller.onStructureFormed();
        helper.assertTrue(controller.isFormed(), "universal_factory must form with advanced buffer");
        if (!(metaMachineAt(helper, bufferPos) instanceof GTNAMEPatternBufferPartMachine buffer)) {
            helper.fail("advanced ME Pattern Buffer is missing from the structure");
            return;
        }
        EnergyHatchPartMachine energyHatch = (EnergyHatchPartMachine) metaMachineAt(helper,
                controllerPos.offset(-1, 0, 2));
        ItemBusPartMachine outputBus = (ItemBusPartMachine) metaMachineAt(helper,
                controllerPos.offset(0, -1, 2));
        for (int stage = 0; stage < types.length; stage++) {
            GenericStack[] patternInputs = new GenericStack[inputs[stage].length];
            for (int input = 0; input < patternInputs.length; input++) {
                patternInputs[input] = GenericStack.fromItemStack(inputs[stage][input]);
            }
            ItemStack pattern = PatternDetailsHelper.encodeProcessingPattern(patternInputs,
                    new GenericStack[] { GenericStack.fromItemStack(outputs[stage]) });
            buffer.getPatternInventory().setStackInSlot(stage, pattern);
        }

        // Each request walks the dependency graph in order. Outputs are extracted from the real
        // machine bus before they become the inputs of the next layer's AE2 pattern delivery.
        for (int request = 1; request <= 4; request++) {
            ItemStack[] produced = new ItemStack[types.length];
            for (int stage = 0; stage < types.length; stage++) {
                if (stage == 3) {
                    helper.assertTrue(ItemStack.isSameItemSameTags(produced[0], inputs[stage][0]) &&
                            ItemStack.isSameItemSameTags(produced[1], inputs[stage][1]),
                            "request " + request + " join layer must consume both branch outputs");
                } else if (stage == 4) {
                    helper.assertTrue(ItemStack.isSameItemSameTags(produced[2], inputs[stage][0]),
                            "request " + request + " cutter layer must consume hammer output");
                } else if (stage == 5) {
                    helper.assertTrue(ItemStack.isSameItemSameTags(produced[3], inputs[stage][0]) &&
                            ItemStack.isSameItemSameTags(produced[4], inputs[stage][1]),
                            "request " + request + " final layer must consume both subassemblies");
                }
                var details = PatternDetailsHelper.decodePattern(buffer.getPatternInventory().getStackInSlot(stage),
                        helper.getLevel());
                helper.assertTrue(details != null, "pattern must decode at stage " + stage);
                ItemStack[] stepInputs = switch (stage) {
                    case 3 -> new ItemStack[] { produced[0], produced[1] };
                    case 4 -> new ItemStack[] { produced[2] };
                    case 5 -> new ItemStack[] { produced[3], produced[4] };
                    default -> inputs[stage];
                };
                KeyCounter delivered = new KeyCounter();
                for (ItemStack input : stepInputs) {
                    delivered.add(AEItemKey.of(input), input.getCount());
                }
                buffer.getInternalInventory()[stage].pushPattern(details, new KeyCounter[] { delivered });
                for (int tick = 0; tick < 20; tick++) {
                    energyHatch.energyContainer.addEnergy(energyHatch.energyContainer.getEnergyCapacity() -
                            energyHatch.energyContainer.getEnergyStored());
                    controller.getRecipeLogic().serverTick();
                }
                int expectedMode = -1;
                for (int mode = 0; mode < controller.getDefinition().getRecipeTypes().length; mode++) {
                    if (controller.getDefinition().getRecipeTypes()[mode] == types[stage]) expectedMode = mode;
                }
                helper.assertTrue(controller.getActiveRecipeType() == expectedMode,
                        "request " + request + " stage " + stage + " must switch to " + types[stage] +
                                "; mode=" + controller.getActiveRecipeType());
                for (int slot = 0; slot < outputBus.getInventory().getSlots(); slot++) {
                    ItemStack stack = outputBus.getInventory().getStackInSlot(slot);
                    if (ItemStack.isSameItemSameTags(stack, outputs[stage])) {
                        produced[stage] = outputBus.getInventory().extractItem(slot, 1, false);
                        break;
                    }
                }
                helper.assertTrue(produced[stage] != null && produced[stage].getCount() == 1,
                        "request " + request + " stage " + stage + " did not yield " + outputs[stage] +
                                "; staged=" + buffer.getInternalInventory()[stage].getItems());
                helper.assertTrue(buffer.getInternalInventory()[stage].getItems().stream().allMatch(ItemStack::isEmpty),
                        "request " + request + " stage " + stage + " left inputs stuck in the pattern buffer: " +
                                buffer.getInternalInventory()[stage].getItems());
            }
            helper.assertTrue(produced[5].is(Items.NETHERITE_INGOT),
                    "request " + request + " must finish the full six-step craft");
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------
    // AE2 autocrafting QA (G-0086): a real ME network, a real crafting CPU, the advanced ME Pattern
    // Buffer and the Universal Factory. The final order walks a three-layer tree over six recipe
    // types; the CPU plans and dispatches every pattern and the machine returns each output.
    // ------------------------------------------------------------------

    /** Recipe types of the autocraft tree. */
    private static final GTRecipeType[] AUTOCRAFT_TYPES = {
            GTRecipeTypes.BENDER_RECIPES,
            GTRecipeTypes.COMPRESSOR_RECIPES,
            GTRecipeTypes.FORGE_HAMMER_RECIPES,
            GTRecipeTypes.LATHE_RECIPES,
            GTRecipeTypes.CUTTER_RECIPES,
            GTRecipeTypes.FORMING_PRESS_RECIPES
    };

    /**
     * Inputs of each autocraft stage. Stage 3 joins stages 0 and 1, stage 4 consumes stage 2 and
     * stage 5 joins stages 3 and 4, so a correct plan has three layers and two joins.
     */
    private static final ItemStack[][] AUTOCRAFT_INPUTS = {
            { new ItemStack(Items.MUSIC_DISC_13) },
            { new ItemStack(Items.MUSIC_DISC_BLOCKS) },
            { new ItemStack(Items.MUSIC_DISC_FAR) },
            { new ItemStack(Items.MUSIC_DISC_CAT), new ItemStack(Items.MUSIC_DISC_CHIRP) },
            { new ItemStack(Items.MUSIC_DISC_MALL) },
            { new ItemStack(Items.MUSIC_DISC_MELLOHI), new ItemStack(Items.MUSIC_DISC_STAL) }
    };

    private static final ItemStack[] AUTOCRAFT_OUTPUTS = {
            new ItemStack(Items.MUSIC_DISC_CAT),
            new ItemStack(Items.MUSIC_DISC_CHIRP),
            new ItemStack(Items.MUSIC_DISC_MALL),
            new ItemStack(Items.MUSIC_DISC_MELLOHI),
            new ItemStack(Items.MUSIC_DISC_STAL),
            new ItemStack(Items.MUSIC_DISC_STRAD)
    };

    /** Raw resources the network must hold before the order can be planned. */
    private static final ItemStack[] AUTOCRAFT_BASE_INPUTS = {
            new ItemStack(Items.MUSIC_DISC_13),
            new ItemStack(Items.MUSIC_DISC_BLOCKS),
            new ItemStack(Items.MUSIC_DISC_FAR)
    };

    private static final int AUTOCRAFT_REQUESTS = 2;
    private static final int AUTOCRAFT_RECIPE_DURATION = 20;

    private static boolean autocraftFactoryRecipesInjected;

    /**
     * Injects the autocraft tree under dedicated ids so it never collides with the layered test's
     * recipes. Music discs are used because GTCEu has no recipes for them, so the planner can only
     * satisfy the order through the six patterns in the buffer.
     */
    private static void injectAutocraftFactoryRecipes() {
        if (autocraftFactoryRecipesInjected) return;
        autocraftFactoryRecipesInjected = true;
        for (int stage = 0; stage < AUTOCRAFT_TYPES.length; stage++) {
            GTRecipeType type = AUTOCRAFT_TYPES[stage];
            type.getAdditionHandler().beginStaging();
            type.getAdditionHandler().addStaging(type.recipeBuilder(GTNACORE.id("gametest_autocraft_factory_" + stage))
                    .inputItems(AUTOCRAFT_INPUTS[stage])
                    .outputItems(AUTOCRAFT_OUTPUTS[stage])
                    // LuV-tier EU so the LuV test hatch cannot overclock the recipe to one tick: the
                    // GameTest must observe every recipe-type switch, not a blur.
                    .EUt(GTValues.VA[GTValues.LuV])
                    .duration(AUTOCRAFT_RECIPE_DURATION)
                    .buildRawRecipe());
            type.getAdditionHandler().completeStaging();
        }
    }

    /**
     * End-to-end AE2 autocrafting QA. Builds a real ME network (creative energy cell, drive with a
     * 1K cell, native crafting CPU) beside the Universal Factory's advanced pattern buffer, then
     * asks the CPU for the final item twice, one order at a time. Every pattern must be planned,
     * dispatched, consumed and returned to the network, with the controller switching through all
     * six recipe types on each order.
     */
    @GameTest(template = "empty_16", timeoutTicks = 2400)
    public static void universalFactoryAutocraftsThroughAe2Network(GameTestHelper helper) {
        if (GTNAMachines.UNIVERSAL_FACTORY == null || GTNAMachines2.ME_ADVANCED_PATTERN_BUFFER == null) {
            helper.fail("universal_factory and advanced ME Pattern Buffer must be enabled");
            return;
        }
        injectAutocraftFactoryRecipes();

        BlockPos controllerPos = new BlockPos(4, 4, 4);
        wipeBox(helper, new BlockPos(0, 0, 0), new BlockPos(15, 10, 10));
        UniversalFactoryMachine controller = buildUniversalFactory(helper, controllerPos, true);
        BlockPos bufferPos = controllerPos.offset(1, -1, 2);
        BlockState bufferState = GTNAMachines2.ME_ADVANCED_PATTERN_BUFFER.getBlock().defaultBlockState()
                .setValue(BlockStateProperties.FACING, Direction.EAST);
        helper.setBlock(bufferPos, bufferState);
        helper.assertTrue(controller != null, "universal_factory controller must exist");
        MultiblockState state = controller.getMultiblockState();
        helper.assertTrue(controller.getPattern().checkPatternAt(state, false),
                "universal_factory pattern did not match: " + patternError(helper, state, controllerPos));
        controller.onStructureFormed();
        helper.assertTrue(controller.isFormed(), "universal_factory must form with the advanced buffer");
        if (!(metaMachineAt(helper, bufferPos) instanceof GTNAMEPatternBufferPartMachine buffer)) {
            helper.fail("advanced ME Pattern Buffer is missing from the structure");
            return;
        }
        for (int stage = 0; stage < AUTOCRAFT_TYPES.length; stage++) {
            GenericStack[] patternInputs = new GenericStack[AUTOCRAFT_INPUTS[stage].length];
            for (int input = 0; input < patternInputs.length; input++) {
                patternInputs[input] = GenericStack.fromItemStack(AUTOCRAFT_INPUTS[stage][input]);
            }
            ItemStack pattern = PatternDetailsHelper.encodeProcessingPattern(patternInputs,
                    new GenericStack[] { GenericStack.fromItemStack(AUTOCRAFT_OUTPUTS[stage]) });
            buffer.getTerminalPatternInventory().setItemDirect(stage, pattern);
        }

        // Real AE2 network: the buffer exposes only its front (EAST) face, so the network starts there.
        BlockPos energyPos = bufferPos.east();
        BlockPos drivePos = energyPos.east();
        BlockPos cpuUnitPos = drivePos.east();
        helper.setBlock(energyPos, AEBlocks.CREATIVE_ENERGY_CELL.block());
        helper.setBlock(drivePos, AEBlocks.DRIVE.block());
        helper.setBlock(cpuUnitPos, AEBlocks.CRAFTING_UNIT.block());
        helper.setBlock(cpuUnitPos.east(), AEBlocks.CRAFTING_STORAGE_1K.block());
        if (helper.getBlockEntity(drivePos) instanceof DriveBlockEntity drive) {
            drive.getInternalInventory().setItemDirect(0, AEItems.ITEM_CELL_1K.stack());
        }

        EnergyHatchPartMachine energyHatch = (EnergyHatchPartMachine) metaMachineAt(helper,
                controllerPos.offset(-1, 0, 2));
        new Ae2AutocraftRun(helper, controller, buffer, energyHatch).start();
    }

    /** Wipes an inclusive block box, so the test does not depend on a pristine template. */
    private static void wipeBox(GameTestHelper helper, BlockPos min, BlockPos max) {
        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int y = min.getY(); y <= max.getY(); y++) {
                for (int z = min.getZ(); z <= max.getZ(); z++) {
                    helper.setBlock(new BlockPos(x, y, z), Blocks.AIR);
                }
            }
        }
    }

    private static CraftingCPUCluster findCraftingCluster(IGrid grid) {
        for (var cpu : grid.getCraftingService().getCpus()) {
            if (cpu instanceof CraftingCPUCluster cluster) {
                return cluster;
            }
        }
        return null;
    }

    private static long countStored(IGrid grid, AEItemKey key) {
        KeyCounter counter = new KeyCounter();
        grid.getStorageService().getInventory().getAvailableStacks(counter);
        return counter.get(key);
    }

    /**
     * Tick-driven state machine for the AE2 autocrafting test. The GameTest method must return
     * quickly, so the wait for the grid, the plan future and the crafting jobs all advance here.
     */
    private static final class Ae2AutocraftRun {

        private final GameTestHelper helper;
        private final UniversalFactoryMachine controller;
        private final GTNAMEPatternBufferPartMachine buffer;
        private final EnergyHatchPartMachine energyHatch;
        private final AEItemKey finalKey = AEItemKey.of(AUTOCRAFT_OUTPUTS[AUTOCRAFT_OUTPUTS.length - 1]);

        private IGrid grid;
        private CraftingCPUCluster cluster;
        private Future<ICraftingPlan> planFuture;
        private boolean ready;
        private int request;
        private boolean submitted;
        private boolean finished;
        private long requestStartTick;
        private final int[] pushedAtStart = new int[AUTOCRAFT_TYPES.length];

        Ae2AutocraftRun(GameTestHelper helper, UniversalFactoryMachine controller,
                        GTNAMEPatternBufferPartMachine buffer, EnergyHatchPartMachine energyHatch) {
            this.helper = helper;
            this.controller = controller;
            this.buffer = buffer;
            this.energyHatch = energyHatch;
        }

        void start() {
            helper.onEachTick(this::tick);
        }

        private void tick() {
            if (finished) return;
            try {
                doTick();
            } catch (Throwable failure) {
                finished = true;
                helper.fail("autocraft state machine threw: " + failure);
            }
        }

        private void doTick() {
            // The finite test energy hatch must not be the bottleneck; keep it charged like the
            // creative hatch the author uses in game.
            if (energyHatch != null) {
                var container = energyHatch.energyContainer;
                container.addEnergy(container.getEnergyCapacity() - container.getEnergyStored());
            }
            if (!ready) {
                if (grid == null) {
                    grid = buffer.getGrid();
                    if (grid == null) {
                        if (helper.getTick() > 120) {
                            finished = true;
                            helper.fail("AE2 grid never formed: bufferNode=" + buffer.getMainNode().getNode() +
                                    " bufferOnline=" + buffer.getMainNode().isOnline() +
                                    " bufferActive=" + buffer.getMainNode().isActive());
                        }
                        return;
                    }
                }
                if (cluster == null) {
                    cluster = findCraftingCluster(grid);
                    if (cluster == null) {
                        if (helper.getTick() > 240) {
                            finished = true;
                            helper.fail("AE2 grid formed but no crafting CPU was found: craftingBlocks=" +
                                    grid.getMachines(CraftingBlockEntity.class).size() + " cpus=" +
                                    grid.getCraftingService().getCpus().size());
                        }
                        return;
                    }
                }
                if (!grid.getCraftingService().isCraftable(finalKey)) {
                    if (helper.getTick() > 360) {
                        finished = true;
                        helper.fail("AE2 CPU is present but the buffer patterns are not craftable: patterns=" +
                                buffer.getAvailablePatterns().size() + " cpus=" +
                                grid.getCraftingService().getCpus().size());
                    }
                    return;
                }
                ready = true;
                beginRequest();
                return;
            }
            if (planFuture != null) {
                if (!planFuture.isDone()) {
                    if (helper.getTick() - requestStartTick > 200) {
                        finished = true;
                        helper.fail("request " + request + " planning never completed (future=" +
                                planFuture + ")");
                    }
                    return;
                }
                submitPlan();
                return;
            }
            if (!submitted) return;
            if (cluster.craftingLogic.hasJob()) {
                if (helper.getTick() - requestStartTick > 800) {
                    finished = true;
                    helper.fail("request " + request + " stalled after " +
                            (helper.getTick() - requestStartTick) + " ticks: buffer=" + dumpBuffer() +
                            " mode=" + controller.getActiveRecipeType() + " pushes=" + pushedSummary());
                }
                return;
            }
            finishRequest();
        }

        private void beginRequest() {
            request++;
            requestStartTick = helper.getTick();
            submitted = false;
            planFuture = null;
            buffer.gtna$clearStartedRecipeTypes();
            for (int slot = 0; slot < pushedAtStart.length; slot++) {
                pushedAtStart[slot] = buffer.gtna$getPushedPatternCount(slot);
            }
            IActionSource source = new BaseActionSource();
            var storage = grid.getStorageService().getInventory();
            var energy = grid.getEnergyService();
            for (ItemStack input : AUTOCRAFT_BASE_INPUTS) {
                AEItemKey key = AEItemKey.of(input);
                long inserted = StorageHelper.poweredInsert(energy, storage, key, input.getCount(), source);
                helper.assertTrue(inserted == input.getCount(),
                        "request " + request + " base input " + input + " must enter the network, inserted " +
                                inserted);
            }
            ICraftingSimulationRequester requester = new ICraftingSimulationRequester() {

                @Override
                public IActionSource getActionSource() {
                    return source;
                }

                @Override
                public IGridNode getGridNode() {
                    return buffer.getMainNode().getNode();
                }
            };
            planFuture = grid.getCraftingService().beginCraftingCalculation(
                    helper.getLevel(), requester, finalKey, 1, CalculationStrategy.REPORT_MISSING_ITEMS);
        }

        private void submitPlan() {
            ICraftingPlan plan;
            try {
                plan = planFuture.get(0, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                finished = true;
                helper.fail("request " + request + " planning failed: " + e);
                return;
            }
            planFuture = null;
            helper.assertTrue(plan.patternTimes().size() == AUTOCRAFT_TYPES.length,
                    "request " + request + " plan must use all " + AUTOCRAFT_TYPES.length + " patterns, got " +
                            plan.patternTimes().size() + " simulation=" + plan.simulation() + " final=" +
                            plan.finalOutput() + " missing=" + dumpCounter(plan.missingItems()) + " used=" +
                            dumpCounter(plan.usedItems()) + " craftingForFinal=" +
                            grid.getCraftingService().getCraftingFor(finalKey).size() + " bufferPatterns=" +
                            buffer.getAvailablePatterns().size());
            IActionSource source = new BaseActionSource();
            ICraftingSubmitResult result = grid.getCraftingService().submitJob(plan, null, cluster, false, source);
            helper.assertTrue(result.successful(),
                    "request " + request + " job must submit, error=" + result.errorCode());
            submitted = true;
        }

        private void finishRequest() {
            long returned = countStored(grid, finalKey);
            helper.assertTrue(returned >= 1,
                    "request " + request + " must return " + finalKey + " to the network, got " + returned +
                            "; buffer=" + dumpBuffer());
            for (int stage = 0; stage < AUTOCRAFT_TYPES.length; stage++) {
                helper.assertTrue(buffer.gtna$getPushedPatternCount(stage) > pushedAtStart[stage],
                        "request " + request + " pattern slot " + stage + " was never dispatched by AE2");
                helper.assertTrue(buffer.getInternalInventory()[stage].getItems().stream().allMatch(ItemStack::isEmpty),
                        "request " + request + " left inputs stuck in slot " + stage + ": " +
                                buffer.getInternalInventory()[stage].getItems());
            }
            Set<GTRecipeType> started = buffer.gtna$getStartedRecipeTypes();
            for (GTRecipeType type : AUTOCRAFT_TYPES) {
                helper.assertTrue(started.contains(type),
                        "request " + request + " must run a " + type + " recipe, saw " + started +
                                " (elapsed " + (helper.getTick() - requestStartTick) + " ticks)");
            }
            if (request >= AUTOCRAFT_REQUESTS) {
                finished = true;
                helper.succeed();
                return;
            }
            // Remove the crafted result so the next order must be planned and crafted again.
            IActionSource source = new BaseActionSource();
            long extracted = StorageHelper.poweredExtraction(grid.getEnergyService(),
                    grid.getStorageService().getInventory(), finalKey, 64, source);
            helper.assertTrue(extracted >= 1,
                    "the crafted " + finalKey + " must be extractable before the next order, got " + extracted);
            beginRequest();
        }

        private String dumpBuffer() {
            StringBuilder out = new StringBuilder();
            for (int slot = 0; slot < AUTOCRAFT_TYPES.length; slot++) {
                out.append(slot).append('=').append(buffer.getInternalInventory()[slot].getItems()).append(' ');
            }
            return out.toString();
        }

        private String pushedSummary() {
            StringBuilder out = new StringBuilder("[");
            for (int slot = 0; slot < AUTOCRAFT_TYPES.length; slot++) {
                if (slot > 0) out.append(',');
                out.append(buffer.gtna$getPushedPatternCount(slot) - pushedAtStart[slot]);
            }
            return out.append(']').toString();
        }

        private static String dumpCounter(KeyCounter counter) {
            StringBuilder out = new StringBuilder("{");
            boolean first = true;
            for (var entry : counter) {
                if (!first) out.append(',');
                first = false;
                out.append(entry.getKey()).append('=').append(entry.getLongValue());
            }
            return out.append('}').toString();
        }
    }

    /**
     * Negative test (Horizon-QA style): the Universal Factory must <b>not</b> match without the
     * mandatory maintenance hatch.
     */
    @GameTest(template = TEMPLATE, timeoutTicks = 45)
    public static void universalFactoryDoesNotFormWithoutMaintenance(GameTestHelper helper) {
        if (GTNAMachines.UNIVERSAL_FACTORY == null) {
            helper.fail("universal_factory is disabled by config");
            return;
        }
        BlockPos controllerPos = new BlockPos(8, 8, 8);
        clearArea(helper, controllerPos);
        UniversalFactoryMachine controller = buildUniversalFactory(helper, controllerPos, false);
        if (controller == null) {
            helper.fail("universal_factory block entity is not a UniversalFactoryMachine");
            return;
        }
        helper.assertFalse(controller.getPattern().checkPatternAt(controller.getMultiblockState(), false),
                "universal_factory must not match without its mandatory maintenance hatch");
        GTNAGameTestUtils.assertNeverForms(helper, controller, 40,
                "universal_factory must remain unformed without maintenance");
    }

    /**
     * Invariant-by-tick test (Horizon-QA style): once formed, the Universal Factory must stay formed
     * with at least one thread for the whole window. A transient unforming would fail on its tick.
     */
    @GameTest(template = TEMPLATE, timeoutTicks = 60)
    public static void universalFactoryStaysFormedWithThreads(GameTestHelper helper) {
        if (GTNAMachines.UNIVERSAL_FACTORY == null) {
            helper.fail("universal_factory is disabled by config");
            return;
        }
        BlockPos controllerPos = new BlockPos(2, 8, 8);
        clearArea(helper, controllerPos);
        UniversalFactoryMachine controller = buildUniversalFactory(helper, controllerPos, true);
        if (controller == null) {
            helper.fail("universal_factory block entity is not a UniversalFactoryMachine");
            return;
        }
        MultiblockState state = controller.getMultiblockState();
        if (!controller.getPattern().checkPatternAt(state, false)) {
            helper.fail("universal_factory pattern did not match: " +
                    patternError(helper, state, controller.self().getPos()));
            return;
        }
        controller.onStructureFormed();
        GTNAGameTestUtils.assertEveryTickUntilTimeout(helper, 60,
                "universal_factory must stay formed with at least one thread",
                () -> {
                    helper.assertTrue(controller.isFormed(), "the universal factory unformed");
                    helper.assertTrue(controller.getDynamicThreads() >= 1,
                            "dynamic threads dropped below 1");
                });
    }

    /**
     * Manifest phase 2 regression: the Industrial Slaughterhouse must be migrated onto the GTNA
     * multiple-recipes base (otherwise the Thread Hatch and the cross-recipe logic cannot work on it).
     */
    @GameTest(template = TEMPLATE, timeoutTicks = 20)
    public static void industrialSlaughterhouseUsesMultipleRecipesBase(GameTestHelper helper) {
        MultiblockMachineDefinition definition = GTNAMachines.INDUSTRIAL_SLAUGHTERHOUSE;
        if (definition == null) {
            helper.fail("industrial_slaughterhouse is disabled by config");
            return;
        }
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, definition.getBlock());
        MetaMachine placed = metaMachineAt(helper, pos);
        helper.assertTrue(placed instanceof WorkableElectricMultipleRecipesMachine,
                "industrial_slaughterhouse must extend WorkableElectricMultipleRecipesMachine, got " +
                        placed.getClass().getName());
        helper.succeed();
    }

    /**
     * The staged-content half of the auto-switch: the existing mode test exercises the buffer pin
     * ({@code selectedModeId}); this one exercises {@code gtna$getPendingModeId} when the mode is
     * asked for by a slot that actually holds staged inputs. A slot with no staged content must not
     * pull the machine into a mode.
     */
    @GameTest(template = TEMPLATE, timeoutTicks = 40)
    public static void stagedContentDrivesBufferModeRequest(GameTestHelper helper) {
        if (GTNAMachines2.ME_PATTERN_BUFFER == null) {
            helper.fail("me_pattern_buffer is disabled by config; the staged-content test cannot run");
            return;
        }
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, GTNAMachines2.ME_PATTERN_BUFFER.get().self());
        BlockEntity placed = helper.getBlockEntity(pos);
        if (!(placed instanceof MetaMachineBlockEntity holder) ||
                !(holder.getMetaMachine() instanceof GTNAMEPatternBufferPartMachine buffer)) {
            helper.fail("placing me_pattern_buffer must create a GTNAMEPatternBufferPartMachine, got " + placed);
            return;
        }

        // A configured slot with no staged content must stay silent.
        buffer.getSlotConfig(0).setPreferredModeId("gtceu:alloy_smelter");
        helper.assertTrue(buffer.gtna$getPendingModeId() == null,
                "an empty slot must not request a mode, got " + buffer.gtna$getPendingModeId());

        // Staged content asks for the slot's preferred mode.
        buffer.gtna$stageSlotItem(0, new ItemStack(Items.IRON_INGOT));
        helper.assertTrue("gtceu:alloy_smelter".equals(buffer.gtna$getPendingModeId()),
                "a staged slot must request its preferred mode, got " + buffer.gtna$getPendingModeId());

        // An explicit buffer pin wins over the staged content.
        buffer.setSelectedModeId("gtceu:furnace");
        helper.assertTrue("gtceu:furnace".equals(buffer.gtna$getPendingModeId()),
                "a pinned buffer must win over staged content, got " + buffer.gtna$getPendingModeId());

        helper.succeed();
    }

    /**
     * Runtime coverage for the deferred ME output path (the one big behaviour that had no runtime
     * test): a saturated network must keep the shortfall queued, a partial insert must keep the
     * remainder, and a successful insert must clear it. The drain insert is injected, so no live AE2
     * grid is needed.
     */
    @GameTest(template = TEMPLATE, timeoutTicks = 40)
    public static void pendingNetworkOutputRetriesUntilItFits(GameTestHelper helper) {
        if (GTNAMachines2.ME_PATTERN_BUFFER == null) {
            helper.fail("me_pattern_buffer is disabled by config; the deferred output test cannot run");
            return;
        }
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, GTNAMachines2.ME_PATTERN_BUFFER.get().self());
        BlockEntity placed = helper.getBlockEntity(pos);
        if (!(placed instanceof MetaMachineBlockEntity holder) ||
                !(holder.getMetaMachine() instanceof GTNAMEPatternBufferPartMachine buffer)) {
            helper.fail("placing me_pattern_buffer must create a GTNAMEPatternBufferPartMachine, got " + placed);
            return;
        }

        AEItemKey key = AEItemKey.of(new ItemStack(Items.DIAMOND));
        buffer.gtna$bufferPendingOutput(key, 64L);
        helper.assertTrue(!buffer.gtna$pendingOutputIsEmpty(),
                "a refused output must stay queued, not be voided");

        // Saturated network: nothing moves and nothing is lost.
        helper.assertTrue(!buffer.gtna$drainPendingOutput((ignoredKey, amount) -> 0L),
                "a saturated network must not report drain work");
        helper.assertTrue(buffer.gtna$pendingOutputAmount(key) == 64L,
                "the shortfall must stay queued when the network is full, got " +
                        buffer.gtna$pendingOutputAmount(key));

        // Partial insert: only the accepted part leaves the queue.
        buffer.gtna$drainPendingOutput((ignoredKey, amount) -> 16L);
        helper.assertTrue(buffer.gtna$pendingOutputAmount(key) == 48L,
                "a partial insert must leave the remainder queued, got " + buffer.gtna$pendingOutputAmount(key));

        // The network finally takes everything: the queue clears.
        helper.assertTrue(buffer.gtna$drainPendingOutput((ignoredKey, amount) -> amount),
                "a successful insert must report drain work");
        helper.assertTrue(buffer.gtna$pendingOutputIsEmpty(),
                "a fully accepted output must leave the queue empty");

        helper.succeed();
    }

    /**
     * The four "large steam forming" siblings (bending/extruder/wiremill/sifter) complete the
     * large_steam_* family: each must expose its own recipe type on the adjustable steam parallel
     * base.
     */
    @GameTest(template = TEMPLATE, timeoutTicks = 20)
    public static void largeSteamFormingFamilyIsSteamBase(GameTestHelper helper) {
        java.util.Map<MultiblockMachineDefinition, GTRecipeType> expected = new java.util.LinkedHashMap<>();
        expected.put(GTNAMachines.LARGE_STEAM_BENDING, GTRecipeTypes.BENDER_RECIPES);
        expected.put(GTNAMachines.LARGE_STEAM_EXTRUDER, GTRecipeTypes.EXTRUDER_RECIPES);
        expected.put(GTNAMachines.LARGE_STEAM_WIREMILL, GTRecipeTypes.WIREMILL_RECIPES);
        expected.put(GTNAMachines.LARGE_STEAM_SIFTER, GTRecipeTypes.SIFTER_RECIPES);
        for (var entry : expected.entrySet()) {
            MultiblockMachineDefinition definition = entry.getKey();
            if (definition == null) {
                helper.fail("a large steam forming machine is disabled by config");
                return;
            }
            GTRecipeType[] types = definition.getRecipeTypes();
            helper.assertTrue(types.length == 1 && types[0] == entry.getValue(),
                    definition.getId() + " must expose exactly " + entry.getValue() + ", got " +
                            java.util.Arrays.toString(types));
        }

        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, GTNAMachines.LARGE_STEAM_SIFTER.getBlock());
        MetaMachine placed = metaMachineAt(helper, pos);
        helper.assertTrue(placed instanceof AdjustableSteamParallelMachine,
                "large_steam_sifter must be an AdjustableSteamParallelMachine, got " + placed);
        helper.succeed();
    }

    /**
     * Steam Lava Maker: a custom recipe type with a stone -> lava recipe, on the steam base.
     */
    @GameTest(template = TEMPLATE, timeoutTicks = 40)
    public static void steamLavaMakerHasStoneToLavaRecipe(GameTestHelper helper) {
        MultiblockMachineDefinition definition = GTNAMachines.STEAM_LAVA_MAKER;
        if (definition == null) {
            helper.fail("steam_lava_maker is disabled by config");
            return;
        }
        GTRecipeType[] types = definition.getRecipeTypes();
        helper.assertTrue(types.length == 1 && types[0] == GTNARecipeType.LAVA_MAKER_RECIPES,
                "steam_lava_maker must expose the lava_maker recipe type, got " + java.util.Arrays.toString(types));

        var recipes = helper.getLevel().getRecipeManager().getAllRecipesFor(GTNARecipeType.LAVA_MAKER_RECIPES);
        helper.assertTrue(!recipes.isEmpty(), "no lava_maker recipes are loaded, so the lava maker cannot run");

        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, definition.getBlock());
        MetaMachine placed = metaMachineAt(helper, pos);
        helper.assertTrue(placed instanceof SteamLavaMakerMachine,
                "steam_lava_maker must be a SteamLavaMakerMachine, got " + placed);
        helper.succeed();
    }

    /**
     * Steam Item Vault: a large item storage on the steam base. The storage must accept a stack far
     * larger than a normal slot (64,000 per slot).
     */
    @GameTest(template = TEMPLATE, timeoutTicks = 40)
    public static void steamItemVaultHoldsLargeStacks(GameTestHelper helper) {
        MultiblockMachineDefinition definition = GTNAMachines.STEAM_ITEM_VAULT;
        if (definition == null) {
            helper.fail("steam_item_vault is disabled by config");
            return;
        }
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, definition.getBlock());
        MetaMachine placed = metaMachineAt(helper, pos);
        if (!(placed instanceof SteamItemVaultMachine vault)) {
            helper.fail("steam_item_vault must be a SteamItemVaultMachine, got " + placed);
            return;
        }
        var handler = vault.getVaultStorage();
        helper.assertTrue(handler.getSlots() >= 256,
                "the vault must expose at least 256 slots, got " + handler.getSlots());
        helper.assertTrue(handler.getSlotLimit(0) >= 64_000,
                "the vault slots must hold large stacks, got limit " + handler.getSlotLimit(0));

        ItemStack remainder = handler.insertItem(0, new ItemStack(Items.DIAMOND, 64), false);
        helper.assertTrue(remainder.isEmpty(), "a 64-item stack must fit in one vault slot");
        helper.assertTrue(handler.getStackInSlot(0).getCount() == 64,
                "the vault slot must hold the inserted stack");
        helper.succeed();
    }

    /**
     * The new steam machines must not share a structure. An earlier port mistakenly reused a single
     * pattern for all of them; this locks their dimensions apart so it cannot regress.
     */
    @GameTest(template = TEMPLATE, timeoutTicks = 20)
    public static void newSteamMachinesHaveDistinctStructures(GameTestHelper helper) {
        java.util.Map<String, String> sizes = new java.util.LinkedHashMap<>();
        java.util.List<MultiblockMachineDefinition> definitions = java.util.List.of(
                GTNAMachines.LARGE_STEAM_BENDING, GTNAMachines.LARGE_STEAM_EXTRUDER,
                GTNAMachines.LARGE_STEAM_WIREMILL, GTNAMachines.LARGE_STEAM_SIFTER,
                GTNAMachines.STEAM_LAVA_MAKER, GTNAMachines.STEAM_ITEM_VAULT);
        for (MultiblockMachineDefinition definition : definitions) {
            if (definition == null) {
                helper.fail("a new steam machine is disabled by config");
                return;
            }
            String size = java.util.Arrays.toString(definition.getPatternFactory().get().getDimensions());
            String previous = sizes.put(size, definition.getId().toString());
            helper.assertTrue(previous == null,
                    "structure size " + size + " is shared by " + previous + " and " + definition.getId());
        }
        helper.succeed();
    }

    /** The author's Hypercore layout must decode and build at its full 44×22×44 size. */
    @GameTest(template = TEMPLATE, timeoutTicks = 20)
    public static void nexusHypercorePatternLoads(GameTestHelper helper) {
        int[] dimensions = GTNAMachines.NEXUS_ME_HYPERCORE.getPatternFactory().get().getDimensions();
        helper.assertTrue(java.util.Arrays.equals(dimensions, new int[] { 44, 22, 44 }),
                "Nexus ME Hypercore pattern dimensions: " + java.util.Arrays.toString(dimensions));
        helper.succeed();
    }

    /**
     * QA smoke: every GTNA machine's item-tooltip builder must run without throwing. Catches the
     * "tooltip crash" class of bug (tier index out of bounds, null display, missing key) for all
     * machines at once instead of one machine at a time.
     */
    @GameTest(template = TEMPLATE, timeoutTicks = 20)
    public static void everyGtnaMachineTooltipBuilds(GameTestHelper helper) {
        int checked = 0;
        for (com.gregtechceu.gtceu.api.machine.MachineDefinition definition : com.gregtechceu.gtceu.api.registry.GTRegistries.MACHINES) {
            if (!GTNACORE.MOD_ID.equals(definition.getId().getNamespace())) {
                continue;
            }
            java.util.function.BiConsumer<ItemStack, java.util.List<Component>> builder = definition
                    .getTooltipBuilder();
            if (builder == null) {
                continue;
            }
            java.util.List<Component> tooltip = new java.util.ArrayList<>();
            try {
                builder.accept(ItemStack.EMPTY, tooltip);
            } catch (RuntimeException exception) {
                helper.fail("tooltip builder for " + definition.getId() + " threw: " + exception);
                return;
            }
            checked++;
        }
        helper.assertTrue(checked > 0, "no GTNA machine tooltip builders were exercised");
        helper.succeed();
    }

    /**
     * GTO asked GTNA to credit the original addon in the tooltips of ported content (the same
     * convention GTNH and GTO use between themselves). This locks the central wiring in
     * {@code GTNASources} for a machine of known origin.
     */
    @GameTest(template = TEMPLATE, timeoutTicks = 20)
    public static void portedMachinesCreditTheirSource(GameTestHelper helper) {
        MultiblockMachineDefinition definition = GTNAMachines.ARTIFICIAL_STAR;
        if (definition == null) {
            helper.fail("artificial_star is disabled by config");
            return;
        }
        java.util.List<Component> tooltip = new java.util.ArrayList<>();
        definition.getTooltipBuilder().accept(ItemStack.EMPTY, tooltip);
        // On a dedicated server the lang keys are not resolved, so assert on the translation key.
        boolean credited = tooltip.stream()
                .anyMatch(component -> component
                        .getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents contents &&
                        "gtna.tooltip.source".equals(contents.getKey()));
        helper.assertTrue(credited,
                "annihilate_generator tooltip must carry the ported-content attribution line; got: " + tooltip);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 20)
    public static void newGtoControllersDescribeTheirFunction(GameTestHelper helper) {
        for (MultiblockMachineDefinition definition : new MultiblockMachineDefinition[] {
                GTNAMachines3.GENERATOR_ARRAY, GTNAMachines3.FISHING_GROUND,
                GTNAMachines3.EVAPORATION_PLANT, GTNAMachines3.GREENHOUSE }) {
            java.util.List<Component> tooltip = new java.util.ArrayList<>();
            definition.getTooltipBuilder().accept(ItemStack.EMPTY, tooltip);
            String prefix = "gtna.machine." + definition.getId().getPath() + ".tooltip";
            long functionalLines = tooltip.stream().filter(component -> component
                    .getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents contents &&
                    contents.getKey().startsWith(prefix + ".")).count();
            int expectedLines = definition == GTNAMachines3.FISHING_GROUND ? 9 :
                    definition == GTNAMachines3.GENERATOR_ARRAY ? 5 : 3;
            helper.assertTrue(functionalLines == expectedLines,
                    definition.getId() + " has incomplete functional tooltip: " + tooltip);
            helper.assertTrue(tooltip.stream().noneMatch(component -> component
                    .getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents contents &&
                    prefix.equals(contents.getKey())),
                    definition.getId() + " explicitly repeats the automatic description: " + tooltip);
            long sourceLines = tooltip.stream().filter(component -> component
                    .getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents contents &&
                    "gtna.tooltip.source".equals(contents.getKey())).count();
            helper.assertTrue(sourceLines == 1,
                    definition.getId() + " must have exactly one source attribution: " + tooltip);
        }
        helper.succeed();
    }

    /**
     * Manifest phase 2 regression: the Dimensionally Transcendent Dirt Forge must also be on the GTNA
     * multiple-recipes base (migrated as a zero-energy machine).
     */
    @GameTest(template = TEMPLATE, timeoutTicks = 20)
    public static void dirtForgeUsesMultipleRecipesBase(GameTestHelper helper) {
        MultiblockMachineDefinition definition = GTNAMachines.DIMENSIONALLY_TRANSCENDENT_DIRT_FORGE;
        if (definition == null) {
            helper.fail("dimensionally_transcendent_dirt_forge is disabled by config");
            return;
        }
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, definition.getBlock());
        MetaMachine placed = metaMachineAt(helper, pos);
        helper.assertTrue(placed instanceof WorkableElectricMultipleRecipesMachine,
                "dimensionally_transcendent_dirt_forge must extend WorkableElectricMultipleRecipesMachine, got " +
                        placed.getClass().getName());
        helper.succeed();
    }

    /**
     * Smoke + behaviour test for the GTLsupb Primitive Stone Furnace port: it must form and smelt a
     * recipe with no energy hatch present (GTLsupb {@code consumeEnergy = false}).
     */
    @GameTest(template = TEMPLATE, timeoutTicks = 120)
    public static void primitiveStoneFurnaceSmeltsWithoutEnergy(GameTestHelper helper) {
        MultiblockMachineDefinition definition = GTNAMachines.PRIMITIVE_STONE_FURNACE;
        if (definition == null) {
            helper.fail("primitive_stone_furnace is disabled by config");
            return;
        }
        injectFurnaceRecipe();

        BlockPos controllerPos = new BlockPos(2, 8, 8);
        clearArea(helper, controllerPos);
        helper.setBlock(controllerPos, definition.getBlock());
        for (int aisle = 0; aisle < 3; aisle++) {
            for (int string = 0; string < 3; string++) {
                for (int charX = 0; charX < 3; charX++) {
                    if (PRIMITIVE_STONE_FURNACE_PATTERN[aisle][string].charAt(charX) != 'A') {
                        continue;
                    }
                    helper.setBlock(controllerPos.offset(1 - charX, string - 1, 2 - aisle), Blocks.STONE);
                }
            }
        }
        BlockPos inputPos = controllerPos.offset(1, 0, 2);
        BlockPos outputPos = controllerPos.offset(-1, 0, 2);
        helper.setBlock(inputPos, GTMachines.ITEM_IMPORT_BUS[GTValues.LV].getBlock());
        helper.setBlock(outputPos, GTMachines.ITEM_EXPORT_BUS[GTValues.LV].getBlock());

        MetaMachine placed = metaMachineAt(helper, controllerPos);
        if (!(placed instanceof PrimitiveStoneFurnaceMachine controller)) {
            helper.fail("primitive_stone_furnace block entity is not a PrimitiveStoneFurnaceMachine, got " + placed);
            return;
        }
        MultiblockState state = controller.getMultiblockState();
        if (!controller.getPattern().checkPatternAt(state, false)) {
            helper.fail("primitive_stone_furnace pattern did not match: " +
                    patternError(helper, state, controller.self().getPos()));
            return;
        }
        controller.onStructureFormed();

        ItemBusPartMachine inputBus = (ItemBusPartMachine) metaMachineAt(helper, inputPos);
        inputBus.getInventory().insertItem(0, new ItemStack(Items.NETHER_STAR, 1), false);

        for (int tick = 0; tick < 40; tick++) {
            controller.getRecipeLogic().serverTick();
        }

        ItemBusPartMachine outputBus = (ItemBusPartMachine) metaMachineAt(helper, outputPos);
        int stone = 0;
        for (int slot = 0; slot < outputBus.getInventory().getSlots(); slot++) {
            ItemStack stack = outputBus.getInventory().getStackInSlot(slot);
            if (stack.is(Items.STONE)) {
                stone += stack.getCount();
            }
        }
        helper.assertTrue(stone >= 1,
                "the primitive stone furnace must smelt with no energy hatch, but the output bus held no stone");
        helper.succeed();
    }

    private static boolean furnaceRecipeInjected;

    /** Adds a trivial nether-star -> stone recipe to the furnace type, for the stone furnace test. */
    private static void injectFurnaceRecipe() {
        if (furnaceRecipeInjected) {
            return;
        }
        furnaceRecipeInjected = true;
        GTRecipeType type = GTRecipeTypes.FURNACE_RECIPES;
        type.getAdditionHandler().beginStaging();
        type.getAdditionHandler().addStaging(type.recipeBuilder(GTNACORE.id("gametest_stone_furnace_recipe"))
                .inputItems(new ItemStack(Items.NETHER_STAR))
                .outputItems(new ItemStack(Items.STONE))
                .EUt(GTValues.VA[GTValues.LV])
                .duration(200)
                .buildRawRecipe());
        type.getAdditionHandler().completeStaging();
    }

    private static boolean assemblerRecipeInjected;

    /** Adds a trivial nether-star -> stone recipe to the assembler type, for the output boost test. */
    private static void injectAssemblerRecipe() {
        if (assemblerRecipeInjected) {
            return;
        }
        assemblerRecipeInjected = true;
        GTRecipeType type = GTRecipeTypes.ASSEMBLER_RECIPES;
        type.getAdditionHandler().beginStaging();
        type.getAdditionHandler().addStaging(type.recipeBuilder(GTNACORE.id("gametest_output_boost_recipe"))
                .inputItems(new ItemStack(Items.NETHER_STAR))
                .outputItems(new ItemStack(Items.STONE))
                .EUt(GTValues.VA[GTValues.LV])
                .duration(1)
                .buildRawRecipe());
        type.getAdditionHandler().completeStaging();
    }

    private static boolean circuitAssemblerRecipeInjected;
    private static boolean factoryBenderRecipeInjected;
    private static boolean layeredFactoryRecipesInjected;

    private static void injectLayeredFactoryRecipes(GTRecipeType[] types, ItemStack[][] inputs,
                                                    ItemStack[] outputs) {
        if (layeredFactoryRecipesInjected) return;
        layeredFactoryRecipesInjected = true;
        for (int stage = 0; stage < types.length; stage++) {
            GTRecipeType type = types[stage];
            type.getAdditionHandler().beginStaging();
            type.getAdditionHandler().addStaging(type.recipeBuilder(GTNACORE.id("gametest_layered_factory_" + stage))
                    .inputItems(inputs[stage])
                    .outputItems(outputs[stage])
                    .EUt(GTValues.VA[GTValues.LV])
                    .duration(3)
                    .buildRawRecipe());
            type.getAdditionHandler().completeStaging();
        }
    }

    private static void injectFactoryBenderRecipe() {
        if (factoryBenderRecipeInjected) return;
        factoryBenderRecipeInjected = true;
        GTRecipeType type = GTRecipeTypes.BENDER_RECIPES;
        type.getAdditionHandler().beginStaging();
        type.getAdditionHandler().addStaging(type.recipeBuilder(GTNACORE.id("gametest_factory_bender_recipe"))
                .inputItems(new ItemStack(Items.NETHER_STAR))
                .outputItems(new ItemStack(Items.EMERALD))
                .EUt(GTValues.VA[GTValues.LV])
                .duration(3)
                .buildRawRecipe());
        type.getAdditionHandler().completeStaging();
    }

    /**
     * Adds a trivial cobblestone to stone recipe to the circuit assembler type, so the mirror test
     * exercises the second real type instead of something the datapack happens to provide.
     */
    private static void injectCircuitAssemblerRecipe() {
        if (circuitAssemblerRecipeInjected) {
            return;
        }
        circuitAssemblerRecipeInjected = true;
        GTRecipeType type = GTRecipeTypes.CIRCUIT_ASSEMBLER_RECIPES;
        type.getAdditionHandler().beginStaging();
        type.getAdditionHandler().addStaging(type.recipeBuilder(GTNACORE.id("gametest_mode_mirror_recipe"))
                .inputItems(new ItemStack(Items.COBBLESTONE))
                .outputItems(new ItemStack(Blocks.STONE))
                .EUt(GTValues.VA[GTValues.LV])
                .duration(1)
                .buildRawRecipe());
        type.getAdditionHandler().completeStaging();
    }

    /** Human-readable pattern error, including the failing cell and the area contents. */
    private static String patternError(GameTestHelper helper, MultiblockState state, BlockPos controllerPos) {
        if (state.error == null) {
            return "unknown pattern error";
        }
        BlockPos failed = state.error.getPos();
        String relative = failed == null ? "?" :
                failed.subtract(helper.absolutePos(controllerPos)).toShortString();
        return state.error.getErrorInfo().getString() + " | failed world=" + failed + " relative=" + relative +
                " | area=" + areaDump(helper);
    }

    /** Every non-air block in the template area, to expose leftovers from another test or run. */
    private static String areaDump(GameTestHelper helper) {
        StringBuilder out = new StringBuilder("[");
        for (int y = 0; y < 12; y++) {
            for (int x = 0; x < 12; x++) {
                for (int z = 0; z < 12; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState blockState = helper.getBlockState(pos);
                    if (!blockState.isAir()) {
                        out.append(pos.toShortString()).append('=')
                                .append(ForgeRegistries.BLOCKS.getKey(blockState.getBlock())).append("; ");
                    }
                }
            }
        }
        return out.append(']').toString();
    }

    /**
     * Clears the whole template volume before building.
     *
     * <p>
     * Our template is intentionally all air, and the gametest framework "places" a template by
     * writing its blocks — so it does not erase anything when the template has no blocks. A reused
     * structure area therefore keeps whatever the previous test left behind, which made the structure
     * tests flaky (a machine casing from another test sitting on a cell this test expects to be
     * empty). Wiping first makes every structure test independent of execution order.
     */
    private static void clearArea(GameTestHelper helper, BlockPos center) {
        for (int dx = -WIPE_RADIUS; dx <= WIPE_RADIUS; dx++) {
            for (int dy = -WIPE_RADIUS; dy <= WIPE_RADIUS; dy++) {
                for (int dz = -WIPE_RADIUS; dz <= WIPE_RADIUS; dz++) {
                    helper.setBlock(center.offset(dx, dy, dz), Blocks.AIR);
                }
            }
        }
    }

    /**
     * Builds the duration_tester 3x3x3 by code.
     *
     * <p>
     * Geometry derived from the pattern factory the definition uses,
     * {@code FactoryBlockPattern.start() = (charDir=LEFT, stringDir=UP, aisleDir=FRONT)} with the
     * controller facing NORTH: the pattern char index maps to world -X, the string index to +Y and
     * the aisle index to -Z, and the controller's own cell is the origin. The pattern is
     * {@code aisle("CCC","CCC","CCC") aisle("CCC","C#C","CCC") aisle("CCC","CSC","CCC")}, so {@code S}
     * sits at pattern (1,1,2) and the air hole {@code #} at (1,1,1): the air is one block at +Z from
     * the controller, and the shell spans -1..+1 in X and Y but 0..+2 in Z (the aisle axis is
     * reversed). Three shell cells are swapped for the mandatory energy hatch and the buses.
     */
    private static void buildDurationTester(GameTestHelper helper, BlockPos controllerPos) {
        BlockPos airPos = controllerPos.offset(0, 0, 1);
        BlockPos energyPos = controllerPos.offset(-1, -1, 2);
        BlockPos inputBusPos = controllerPos.offset(0, -1, 2);
        BlockPos outputBusPos = controllerPos.offset(1, -1, 2);
        // The definition pins maintenance with setExactLimit(1), which is min AND max, so a
        // maintenance hatch is mandatory here (unlike the muffler/parallel/thread hats, which are
        // declared with max limits only).
        BlockPos maintenancePos = controllerPos.offset(-1, 0, 2);

        helper.setBlock(controllerPos, GTNAMachines2.DURATION_TESTER.getBlock());
        helper.setBlock(energyPos, GTMachines.ENERGY_INPUT_HATCH[GTValues.EV].getBlock());
        helper.setBlock(inputBusPos, GTMachines.ITEM_IMPORT_BUS[GTValues.LV].getBlock());
        helper.setBlock(outputBusPos, GTMachines.ITEM_EXPORT_BUS[GTValues.LV].getBlock());
        helper.setBlock(maintenancePos, GTMachines.MAINTENANCE_HATCH.getBlock());
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = 0; dz <= 2; dz++) {
                    BlockPos pos = controllerPos.offset(dx, dy, dz);
                    if (pos.equals(controllerPos) || pos.equals(airPos) || pos.equals(energyPos) ||
                            pos.equals(inputBusPos) || pos.equals(outputBusPos) || pos.equals(maintenancePos)) {
                        continue;
                    }
                    helper.setBlock(pos, GTBlocks.CASING_STEEL_SOLID.get());
                }
            }
        }
    }

    /** Resolves the meta machine at {@code pos}, failing loudly when the block entity is not one. */
    private static MetaMachine metaMachineAt(GameTestHelper helper, BlockPos pos) {
        BlockEntity entity = helper.getBlockEntity(pos);
        if (!(entity instanceof MetaMachineBlockEntity machineBlockEntity)) {
            throw new IllegalStateException("expected a machine block entity at " + pos + ", got " + entity);
        }
        return machineBlockEntity.getMetaMachine();
    }
}
