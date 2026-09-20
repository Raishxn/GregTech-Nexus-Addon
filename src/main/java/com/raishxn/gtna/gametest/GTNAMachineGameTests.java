package com.raishxn.gtna.gametest;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.pattern.MultiblockState;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.machine.multiblock.part.EnergyHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.common.data.GTNAMachines2;
import com.raishxn.gtna.common.machine.multiblock.electric.WorkableElectricMultipleRecipesMachine;
import com.raishxn.gtna.common.machine.multiblock.part.ae.GTNAMEPatternBufferPartMachine;
import com.raishxn.gtna.common.machine.trait.GTNAMultipleRecipesLogic;

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

    /** Smallest template that still gives room to place a machine and read it back. */
    private static final String TEMPLATE = "empty_5x5";

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

        // Geometry derived from the pattern factory the definition uses,
        // FactoryBlockPattern.start() = (charDir=LEFT, stringDir=UP, aisleDir=FRONT), with the
        // controller facing NORTH: the pattern char index maps to world -X, the string index to +Y
        // and the aisle index to -Z, and the controller's own cell is the origin. The pattern is
        // aisle("CCC","CCC","CCC") aisle("CCC","C#C","CCC") aisle("CCC","CSC","CCC")
        // so `S` sits at pattern (1,1,2) and the air hole `#` at (1,1,1): the air is one block at
        // +Z from the controller, and the shell spans -1..+1 in X and Y but 0..+2 in Z (the aisle
        // axis is reversed). Three shell cells are swapped for the mandatory energy hatch and the
        // input/output buses.
        BlockPos controllerPos = new BlockPos(2, 2, 2);
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

        MetaMachine placed = metaMachineAt(helper, controllerPos);
        if (!(placed instanceof WorkableElectricMultipleRecipesMachine controller)) {
            helper.fail("duration_tester block entity is not our machine class, got " + placed);
            return;
        }
        // Force the structure check rather than waiting for the ticker (the trick GTCEu's own
        // gametests use), otherwise the parts are not registered yet. Note that onStructureFormed()
        // sets isFormed() unconditionally, so the match result has to be asserted separately.
        MultiblockState state = controller.getMultiblockState();
        boolean matched = controller.getPattern().checkPatternAt(state, false);
        if (!matched) {
            helper.fail("duration_tester pattern did not match: " +
                    (state.error == null ? "unknown pattern error" : state.error.getErrorInfo().getString()));
            return;
        }
        controller.onStructureFormed();
        helper.assertTrue(controller.getParts().size() > 0,
                "the pattern matched but no parts were registered on the controller");

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

    /** Resolves the meta machine at {@code pos}, failing loudly when the block entity is not one. */
    private static MetaMachine metaMachineAt(GameTestHelper helper, BlockPos pos) {
        BlockEntity entity = helper.getBlockEntity(pos);
        if (!(entity instanceof MetaMachineBlockEntity machineBlockEntity)) {
            throw new IllegalStateException("expected a machine block entity at " + pos + ", got " + entity);
        }
        return machineBlockEntity.getMetaMachine();
    }
}
