package com.raishxn.gtna.gametest;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.recipe.ingredient.EnergyStack;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.raishxn.gtna.common.data.*;
import com.raishxn.gtna.common.machine.multiblock.part.energy.NexusNetworkTerminalPartMachine;
import com.raishxn.gtna.utils.datastructure.Int128;

import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

@GameTestHolder("gtna")
@PrefixGameTestTemplate(false)
public final class NexusNetworkTerminalGameTests {

    private static NexusNetworkTerminalPartMachine terminal(GameTestHelper helper, BlockPos pos, boolean output,
                                                            UUID owner) {
        helper.setBlock(pos,
                (output ? GTNAEnergyHatches.NETWORK_OUTPUT_TERMINAL : GTNAEnergyHatches.NETWORK_INPUT_TERMINAL)
                        .getBlock());
        var machine = (NexusNetworkTerminalPartMachine) ((MetaMachineBlockEntity) helper.getBlockEntity(pos))
                .getMetaMachine();
        machine.setNetworkOwner(owner);
        return machine;
    }

    private static NexusEnergyNetwork network(GameTestHelper helper, UUID owner, BigInteger capacity) {
        var network = NexusEnergyNetwork.get(helper.getLevel());
        network.setMatrixStats(owner, 1, GTValues.LV, 0.95, Int128.fromBigInteger(capacity), true);
        network.setMatrixDimension(owner, helper.getLevel().dimension());
        network.setMaxCapacity(owner, Int128.fromBigInteger(capacity));
        return network;
    }

    @GameTest(template = "empty_16", timeoutTicks = 40)
    public static void nexusNetworkTerminalsSimulateWithoutDebitAndPreserveLargeBalances(GameTestHelper helper) {
        var owner = UUID.randomUUID();
        var network = network(helper, owner, BigInteger.ONE.shiftLeft(100));
        var output = terminal(helper, new BlockPos(1, 1, 1), true, owner);
        var input = terminal(helper, new BlockPos(2, 1, 1), false, owner);
        var recipe = GTRecipeTypes.DUMMY_RECIPES.recipeBuilder("network_test").duration(1).buildRawRecipe();
        var energy = List.of(new EnergyStack(36_028_797_018_963_968L));
        var before = network.save(new net.minecraft.nbt.CompoundTag());
        helper.assertTrue(output.energyContainer.handleRecipeInner(IO.OUT, recipe, energy, true) == null,
                "Infinity generation must fit one direct terminal");
        helper.assertTrue(before.equals(network.save(new net.minecraft.nbt.CompoundTag())),
                "simulation must not mutate network NBT");
        helper.assertTrue(output.energyContainer.handleRecipeInner(IO.OUT, recipe, energy, false) == null,
                "generation commits");
        BigInteger credited = network.getEnergy(owner).toBigInteger();
        helper.assertTrue(credited.signum() > 0 && credited.compareTo(BigInteger.valueOf(energy.get(0).voltage())) <= 0,
                "network applies loss exactly once on insertion");
        var state = network.getConnections(owner)
                .get(net.minecraft.core.GlobalPos.of(helper.getLevel().dimension(), output.getPos()));
        helper.assertTrue(
                state != null && state.euTransferred.toBigInteger().equals(BigInteger.valueOf(energy.get(0).voltage())),
                "terminal monitor records gross accepted generation");
        network.setEnergy(owner, Int128.fromBigInteger(BigInteger.ONE.shiftLeft(80)));
        helper.assertTrue(input.energyContainer.getEnergyStored() == Long.MAX_VALUE,
                "large balance saturates the long view");
        BigInteger start = network.getEnergy(owner).toBigInteger();
        var withdrawal = List.of(new EnergyStack(1234));
        helper.assertTrue(input.energyContainer.handleRecipeInner(IO.IN, recipe, withdrawal, true) == null &&
                network.getEnergy(owner).toBigInteger().equals(start), "input simulation cannot debit");
        helper.assertTrue(input.energyContainer.handleRecipeInner(IO.IN, recipe, withdrawal, false) == null &&
                network.getEnergy(owner).toBigInteger().equals(start.subtract(BigInteger.valueOf(1234))),
                "input debits only recipe amount");
        helper.assertTrue(output.energyContainer.handleRecipeInner(IO.OUT, recipe,
                List.of(new EnergyStack(Long.MAX_VALUE), new EnergyStack(1)), false) != null,
                "sum above long must be rejected without overflow");
        helper.assertTrue(network.getEnergy(owner).toBigInteger().equals(start.subtract(BigInteger.valueOf(1234))),
                "overflow rejection is nonmutating");
        helper.succeed();
    }

    @GameTest(template = "empty_16", timeoutTicks = 40)
    public static void nexusNetworkTerminalRejectsFullOfflineAndWrongDirection(GameTestHelper helper) {
        var owner = UUID.randomUUID();
        var network = network(helper, owner, BigInteger.valueOf(1000));
        var output = terminal(helper, new BlockPos(1, 1, 1), true, owner);
        var input = terminal(helper, new BlockPos(2, 1, 1), false, owner);
        var recipe = GTRecipeTypes.DUMMY_RECIPES.recipeBuilder("network_full_test").duration(1).buildRawRecipe();
        var energy = List.of(new EnergyStack(100));
        network.setEnergy(owner, new Int128(1000));
        helper.assertTrue(output.energyContainer.handleRecipeInner(IO.OUT, recipe, energy, true) != null &&
                output.energyContainer.handleRecipeInner(IO.OUT, recipe, energy, false) != null,
                "full network blocks both matching and commit without discarding energy");
        network.setEnergy(owner, new Int128(995));
        long quote = output.energyContainer.quote(100);
        helper.assertTrue(quote > 0 && quote < 100, "capacity quote accounts for fractional loss");
        helper.assertTrue(
                output.energyContainer.changeEnergy(100) == quote && network.getEnergy(owner).toLong() <= 1000,
                "partial IEnergyContainer insertion accepts only quoted gross amount");
        helper.assertTrue(
                input.energyContainer.changeEnergy(100) == 0 && output.energyContainer.changeEnergy(-100) == 0,
                "direction cannot bypass roles");
        output.setNetworkOwner(null);
        helper.assertTrue(output.energyContainer.changeEnergy(100) == 0, "unbound output cannot generate");
        input.setWorkingEnabled(false);
        helper.assertTrue(input.energyContainer.changeEnergy(-100) == 0, "disabled input cannot debit");
        network.setMatrixDimension(owner, net.minecraft.world.level.Level.NETHER);
        output.setNetworkOwner(owner);
        helper.assertTrue(output.energyContainer.changeEnergy(100) == 0, "dimension policy is preserved");
        helper.succeed();
    }

    @GameTest(template = "empty_16", timeoutTicks = 40)
    public static void nexusNetworkTerminalBindingSurvivesReload(GameTestHelper helper) {
        var owner = UUID.randomUUID();
        var pos = new BlockPos(1, 1, 1);
        var terminal = terminal(helper, pos, true, owner);
        var holder = (MetaMachineBlockEntity) helper.getBlockEntity(pos);
        var tag = holder.saveWithFullMetadata();
        terminal.setNetworkOwner(null);
        holder.load(tag);
        helper.assertTrue(owner.equals(terminal.getNetworkOwner()), "network UUID persists on reload");
        helper.assertTrue(
                terminal.energyContainer.getOutputAmperage() == 1 &&
                        terminal.energyContainer.getOutputVoltage() == Long.MAX_VALUE,
                "nominal terminal has one amp, not the upstream voltage-as-amperage bug");
        helper.assertTrue(!terminal.canShared() && !terminal.energyContainer.inputsEnergy(null) &&
                !terminal.energyContainer.outputsEnergy(null), "part is not shared and does not connect to cables");
        helper.succeed();
    }

    @GameTest(template = "empty_16", timeoutTicks = 40)
    public static void artificialStarRunsInfinityThroughOneNetworkTerminal(GameTestHelper helper) throws Exception {
        var owner = UUID.randomUUID();
        var network = network(helper, owner, BigInteger.ONE.shiftLeft(100));
        var output = terminal(helper, new BlockPos(1, 1, 1), true, owner);
        helper.setBlock(new BlockPos(3, 1, 1), GTNAMachines.ARTIFICIAL_STAR.getBlock());
        var star = (com.raishxn.gtna.common.machine.multiblock.energy.ArtificialStarMachine) ((MetaMachineBlockEntity) helper
                .getBlockEntity(new BlockPos(3, 1, 1))).getMetaMachine();
        // Isolate real generator modifier/capability plumbing; physical 109-slice formation remains manual.
        var field = com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine.class
                .getDeclaredField("energyContainer");
        field.setAccessible(true);
        field.set(star, new com.gregtechceu.gtceu.api.misc.EnergyContainerList(List.of(output.energyContainer)));
        var recipe = helper.getLevel().getRecipeManager().getAllRecipesFor(GTNARecipeType.ARTIFICIAL_STAR_RECIPES)
                .stream().filter(r -> r.id.getPath().endsWith("/infinity_antimatter_fuel_rod")).findFirst()
                .orElseThrow();
        var modified = GTNAMachines.ARTIFICIAL_STAR.getRecipeModifier().applyModifier(star, recipe);
        helper.assertTrue(modified != null && modified.duration > 0 && modified.getOutputEUt().getTotalEU() > 0,
                "real Star modifier produces valid overclocked Infinity");
        var energy = List.of(modified.getOutputEUt());
        helper.assertTrue(output.energyContainer.handleRecipeInner(IO.OUT, modified, energy, true) == null &&
                output.energyContainer.handleRecipeInner(IO.OUT, modified, energy, false) == null,
                "one terminal accepts real overclocked Infinity output");
        helper.assertTrue(network.getEnergy(owner).toBigInteger().signum() > 0,
                "generated energy reaches Nexus account");
        helper.succeed();
    }

    @GameTest(template = "empty_16", timeoutTicks = 40)
    public static void nexusMultipleNetworkTerminalsDoNotOverflowAggregatedCapabilities(GameTestHelper helper) throws Exception {
        var owner = UUID.randomUUID();
        var one = terminal(helper, new BlockPos(1, 1, 1), true, owner);
        var two = terminal(helper, new BlockPos(2, 1, 1), true, owner);
        var ordinaryPos = new BlockPos(4, 1, 1);
        helper.setBlock(ordinaryPos, GTNAEnergyHatches.WIRELESS_DYNAMO_HATCHES[GTValues.MAX][10].getBlock());
        var ordinary = (com.raishxn.gtna.common.machine.multiblock.part.energy.WirelessDynamoHatchPartMachine) ((MetaMachineBlockEntity) helper
                .getBlockEntity(ordinaryPos)).getMetaMachine();
        var starPos = new BlockPos(3, 1, 1);
        helper.setBlock(starPos, GTNAMachines.ARTIFICIAL_STAR.getBlock());
        var star = (com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine) ((MetaMachineBlockEntity) helper
                .getBlockEntity(starPos)).getMetaMachine();
        var positions = com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine.class
                .getDeclaredField("partPositions");
        positions.setAccessible(true);
        positions.set(star, new BlockPos[] { one.getPos(), two.getPos(), ordinary.getPos() });
        star.getParts();
        one.addedToController(star);
        two.addedToController(star);
        var combined = new com.gregtechceu.gtceu.api.misc.EnergyContainerList(List.of(
                one.energyContainer, two.energyContainer, ordinary.energyContainer));
        helper.assertTrue(combined.getOutputVoltage() > 0 && combined.getEnergyCapacity() > 0,
                "multiple direct terminals and ordinary dynamo cannot overflow voltage or buffer sums");
        helper.assertTrue(BigInteger.valueOf(one.energyContainer.getOutputVoltage())
                .add(BigInteger.valueOf(two.energyContainer.getOutputVoltage()))
                .add(BigInteger.valueOf(ordinary.energyContainer.getOutputVoltage())
                        .multiply(BigInteger.valueOf(ordinary.amperage)))
                .compareTo(BigInteger.valueOf(Long.MAX_VALUE)) <= 0, "nominal aggregate remains within long");
        var inputOne = terminal(helper, new BlockPos(5, 1, 1), false, owner);
        var inputTwo = terminal(helper, new BlockPos(6, 1, 1), false, owner);
        positions.set(star, new BlockPos[] { inputOne.getPos(), inputTwo.getPos() });
        star.getParts();
        inputOne.addedToController(star);
        inputTwo.addedToController(star);
        var network = network(helper, owner, BigInteger.ONE.shiftLeft(100));
        network.setEnergy(owner, Int128.fromBigInteger(BigInteger.ONE.shiftLeft(80)));
        var inputs = new com.gregtechceu.gtceu.api.misc.EnergyContainerList(
                List.of(inputOne.energyContainer, inputTwo.energyContainer));
        helper.assertTrue(inputs.getEnergyStored() > 0 && inputs.getEnergyCapacity() >= inputs.getEnergyStored(),
                "multiple input long views cannot overflow stored/capacity aggregation");
        var assemblyRecipes = helper.getLevel().getRecipeManager()
                .getAllRecipesFor(GTRecipeTypes.ASSEMBLY_LINE_RECIPES);
        for (String name : new String[] { "input", "output" }) helper.assertTrue(assemblyRecipes.stream()
                .anyMatch(recipe -> recipe.id.getPath().endsWith("/nexus_network_" + name + "_terminal")),
                "both adapted terminal recipes must be loaded");
        helper.succeed();
    }
}
