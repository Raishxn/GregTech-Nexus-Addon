package com.raishxn.gtna.gametest;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.MultiblockState;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.gregtechceu.gtceu.common.machine.multiblock.part.EnergyHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

import appeng.api.stacks.AEItemKey;
import appeng.crafting.execution.CraftingCpuLogic;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.api.capability.SteamWirelessNetworkManager;
import com.raishxn.gtna.common.WirelessSteamHudSync;
import com.raishxn.gtna.common.data.GTNABlocks;
import com.raishxn.gtna.common.data.GTNAMachines;
import com.raishxn.gtna.common.data.GTNAMachines2;
import com.raishxn.gtna.common.data.GTNARecipeType;
import com.raishxn.gtna.common.machine.multiMachineBase.SteamMultiMachineBase;
import com.raishxn.gtna.common.machine.multiblock.electric.UniversalFactoryMachine;
import com.raishxn.gtna.common.machine.multiblock.electric.WorkableElectricMultipleRecipesMachine;
import com.raishxn.gtna.common.machine.multiblock.noenergy.PrimitiveStoneFurnaceMachine;
import com.raishxn.gtna.common.machine.multiblock.part.OutputBoostHatchPartMachine;
import com.raishxn.gtna.common.machine.multiblock.part.ae.GTNAMEPatternBufferPartMachine;
import com.raishxn.gtna.common.machine.multiblock.part.steam.WirelessSteamInputHatch;
import com.raishxn.gtna.common.machine.multiblock.part.steam.WirelessSteamOutputHatch;
import com.raishxn.gtna.common.machine.multiblock.steam.AdjustableSteamParallelMachine;
import com.raishxn.gtna.common.machine.multiblock.steam.SteamItemVaultMachine;
import com.raishxn.gtna.common.machine.multiblock.steam.SteamLavaMakerMachine;
import com.raishxn.gtna.common.machine.trait.GTNAMultipleRecipesLogic;
import com.raishxn.gtna.network.packet.SWirelessSteamStats;

import java.util.UUID;

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

    @GameTest(template = TEMPLATE, timeoutTicks = 20)
    public static void nativeCraftingCpuKeepsAe2Executor(GameTestHelper helper) {
        CraftingCPUCluster cluster = new CraftingCPUCluster(BlockPos.ZERO, BlockPos.ZERO);
        helper.assertTrue(cluster.craftingLogic.getClass() == CraftingCpuLogic.class,
                "a native AE2 CPU must keep CraftingCpuLogic, got " + cluster.craftingLogic.getClass().getName());
        helper.succeed();
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

    /**
     * Negative test (Horizon-QA style): the Universal Factory must <b>not</b> match without the
     * mandatory maintenance hatch.
     */
    @GameTest(template = TEMPLATE, timeoutTicks = 20)
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
        helper.succeed();
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
                failed.offset(-controllerPos.getX(), -controllerPos.getY(), -controllerPos.getZ()).toShortString();
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
