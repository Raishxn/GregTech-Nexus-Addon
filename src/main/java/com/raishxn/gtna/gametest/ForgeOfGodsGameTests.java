package com.raishxn.gtna.gametest;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.raishxn.gtna.api.machine.feature.godforge.GodforgeMath;
import com.raishxn.gtna.common.data.GTNAMachines3;
import com.raishxn.gtna.common.data.multiblock.GTNAMultiBlockFileReader;
import com.raishxn.gtna.common.machine.multiblock.godforge.ForgeOfGodsMachine;

@GameTestHolder("gtna")
@PrefixGameTestTemplate(false)
public final class ForgeOfGodsGameTests {

    @GameTest(template = "empty_16", timeoutTicks = 1200)
    public static void forgeOfGodsPreviewFormsAndLightsTheStar(GameTestHelper helper) throws Exception {
        var definition = GTNAMachines3.FORGE_OF_GODS;
        var shapes = definition.getMatchingShapes();
        helper.assertTrue(!shapes.isEmpty(), "the structure must have a preview");
        var shape = shapes.get(0).getBlocks();
        ForgeOfGodsMachine machine = null;
        int placed = 0;
        for (int x = 0; x < shape.length; x++) {
            for (int y = 0; y < shape[x].length; y++) {
                for (int z = 0; z < shape[x][y].length; z++) {
                    var info = shape[x][y][z];
                    if (info == null || info.getBlockState().isAir()) continue;
                    var pos = new BlockPos(x, y + 140, z);
                    // The GameTest world is reused: clear first so an old controller cannot keep its state.
                    helper.setBlock(pos, net.minecraft.world.level.block.Blocks.AIR);
                    helper.setBlock(pos, info.getBlockState());
                    placed++;
                    if (info.getBlockState().is(definition.getBlock())) {
                        machine = (ForgeOfGodsMachine) ((IMachineBlockEntity) helper.getBlockEntity(pos))
                                .getMetaMachine();
                    }
                }
            }
        }
        helper.assertTrue(placed == 7561, "beam shaft and first ring have 7561 blocks: " + placed);
        helper.assertTrue(machine != null, "preview must contain the controller");
        // The structure reaches far beyond the test plot: load every chunk it may touch.
        var center = machine.getPos();
        for (int cx = (center.getX() - 200) >> 4; cx <= (center.getX() + 200) >> 4; cx++) {
            for (int cz = (center.getZ() - 200) >> 4; cz <= (center.getZ() + 200) >> 4; cz++) {
                helper.getLevel().getChunk(cx, cz);
            }
        }
        helper.assertTrue(machine.checkPatternWithLock(),
                "the GTNH main structure must form: " + (machine.getMultiblockState().error == null ? null :
                        machine.getMultiblockState().error.getErrorInfo().getString() + " at " +
                                machine.getMultiblockState().error.getPos() + " ctrl " + machine.getPos() + " active=" +
                                machine.data().renderActive));
        machine.onStructureFormed();
        helper.assertTrue(machine.data().ringAmount == 1, "one ring without the CD upgrade");

        var handlers = ForgeOfGodsMachine.class.getDeclaredMethod("itemHandlers", IO.class);
        handlers.setAccessible(true);
        var inputs = (java.util.List<?>) handlers.invoke(machine, IO.IN);
        helper.assertTrue(!inputs.isEmpty(), "the structure has its input bus; parts=" + machine.getParts());
        int needed = GodforgeMath.startupFuelConsumption(machine.data());
        ((com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler) inputs.get(0)).setStackInSlot(0,
                ForgeOfGodsMachine.stellarFuel().copyWithCount(needed));
        var ticker = ForgeOfGodsMachine.class.getDeclaredField("ticker");
        ticker.setAccessible(true);
        ticker.setLong(machine, 0);
        var tick = ForgeOfGodsMachine.class.getDeclaredMethod("godforgeTick");
        tick.setAccessible(true);
        for (int i = 0; i < 100; i++) tick.invoke(machine);
        // Same tick, no fuel fluid in the hatch: the first drain removes one charge, as in GTNH.
        helper.assertTrue(machine.data().internalBattery == needed - 1 && machine.isRenderActive(),
                "stellar fuel lights the star and charges the battery: " + machine.data().internalBattery +
                        " fuel=" + machine.data().stellarFuelAmount + " consumed=" + machine.data().totalFuelConsumed +
                        " needed=" + needed + " ticker=" + ticker.getLong(machine) + " factor=" +
                        machine.data().fuelConsumptionFactor + " drain=" + machine.data().fuelConsumption);
        var ring = com.raishxn.gtna.common.machine.multiblock.godforge.GodforgeRings.positions(helper.getLevel(),
                machine.getPos(), machine.getFrontFacing(), machine.getUpwardsFacing(), machine.isFlipped(), 1);
        helper.assertTrue(ring.size() == 6044, "the first ring has 6044 blocks: " + ring.size());
        helper.assertTrue(ring.keySet().stream().allMatch(pos -> helper.getLevel().getBlockState(pos).isAir()),
                "the lit star takes the first ring into the controller");
        helper.assertTrue(machine.checkPatternWithLock(),
                "the beam shaft with an empty ring still forms");
        for (int i = 0; i < 100 * (needed - 1); i++) tick.invoke(machine);
        helper.assertTrue(machine.data().internalBattery == 0 && !machine.isRenderActive(),
                "without fuel the battery drains and the star goes out");
        helper.assertTrue(ring.keySet().stream().noneMatch(pos -> helper.getLevel().getBlockState(pos).isAir()),
                "the ring is placed back when the star goes out");
        helper.assertTrue(machine.checkPatternWithLock(),
                "the full structure forms again: " + machine.getMultiblockState().error);
        helper.succeed();
    }

    @GameTest(template = "empty_16", timeoutTicks = 100)
    public static void forgeOfGodsRingPatternsShareTheController(GameTestHelper helper) {
        for (String name : new String[] { "god_forge", "god_forge_ring_2", "god_forge_ring_3" }) {
            var aisles = GTNAMultiBlockFileReader.loadAisles(name);
            int controllers = 0;
            for (String[] aisle : aisles) for (String row : aisle) for (char c : row.toCharArray()) if (c == '~')
                controllers++;
            helper.assertTrue(aisles.length == 187 && controllers == 1, name + " has one controller in 187 aisles");
        }
        helper.succeed();
    }

    @GameTest(template = "empty_16", timeoutTicks = 200)
    public static void godforgeSmeltingModuleRunsBlastRecipesOnWirelessEnergy(GameTestHelper helper) {
        var definition = GTNAMachines3.GODFORGE_SMELTING_MODULE;
        var shape = definition.getMatchingShapes().get(0).getBlocks();
        com.raishxn.gtna.common.machine.multiblock.godforge.GodforgeModuleMachine module = null;
        for (int x = 0; x < shape.length; x++) {
            for (int y = 0; y < shape[x].length; y++) {
                for (int z = 0; z < shape[x][y].length; z++) {
                    var info = shape[x][y][z];
                    var pos = new BlockPos(x + 1, y + 30, z + 1);
                    helper.setBlock(pos, net.minecraft.world.level.block.Blocks.AIR);
                    if (info == null || info.getBlockState().isAir()) continue;
                    helper.setBlock(pos, info.getBlockState());
                    if (info.getBlockState().is(definition.getBlock())) {
                        module = (com.raishxn.gtna.common.machine.multiblock.godforge.GodforgeModuleMachine) ((IMachineBlockEntity) helper
                                .getBlockEntity(pos)).getMetaMachine();
                    }
                }
            }
        }
        helper.assertTrue(module != null, "module preview has a controller");
        // Hatches are optional in the module shell; put a bus on each side of the controller.
        var facing = module.getFrontFacing();
        helper.getLevel().setBlockAndUpdate(module.getPos().relative(facing.getClockWise()),
                com.gregtechceu.gtceu.common.data.GTMachines.ITEM_IMPORT_BUS[com.gregtechceu.gtceu.api.GTValues.LV]
                        .getBlock().defaultBlockState());
        helper.getLevel().setBlockAndUpdate(module.getPos().relative(facing.getCounterClockWise()),
                com.gregtechceu.gtceu.common.data.GTMachines.ITEM_EXPORT_BUS[com.gregtechceu.gtceu.api.GTValues.LV]
                        .getBlock().defaultBlockState());
        helper.assertTrue(module.checkPatternWithLock(),
                "the GTNH 7x7x13 module structure must form: " + module.getMultiblockState().error);
        module.onStructureFormed();
        var stats = module.godforgeStats();
        stats.heat = 20000;
        stats.heatForOC = 15000;
        stats.calculatedMaxParallel = 1024;
        java.util.UUID owner = java.util.UUID.randomUUID();
        module.setNetworkOwner(owner);
        module.connect();
        var network = com.raishxn.gtna.common.data.NexusEnergyNetwork.get(helper.getLevel());
        network.setMatrixStats(owner, 1, com.gregtechceu.gtceu.api.GTValues.MAX, 0.95,
                new com.raishxn.gtna.utils.datastructure.Int128(Long.MAX_VALUE), true);
        network.setMatrixDimension(owner, helper.getLevel().dimension());
        var budget = new com.raishxn.gtna.utils.datastructure.Int128(Long.MAX_VALUE);
        com.raishxn.gtna.api.capability.WirelessEnergyManager.setEnergy(helper.getLevel(), owner, budget);

        com.gregtechceu.gtceu.api.recipe.GTRecipe blast = null;
        for (var recipe : helper.getLevel().getRecipeManager().getAllRecipesFor(
                com.gregtechceu.gtceu.common.data.GTRecipeTypes.BLAST_RECIPES)) {
            if (recipe.getInputContents(com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability.CAP)
                    .isEmpty() && recipe.data.getInt("ebf_temp") > 1000) {
                blast = recipe;
                break;
            }
        }
        helper.assertTrue(blast != null, "a GTCEu blast recipe without fluids exists");
        var handlers = module.getParts().stream().flatMap(part -> part.getRecipeHandlers().stream())
                .flatMap(list -> list.getHandlersFlat().stream())
                .filter(h -> h instanceof com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler items &&
                        items.handlerIO == IO.IN)
                .map(h -> (com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler) h).toList();
        helper.assertTrue(!handlers.isEmpty(), "module has an input bus");
        int slot = 0;
        for (var content : blast
                .getInputContents(com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability.CAP)) {
            var ingredient = com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability.CAP.of(content.content);
            var stack = ingredient.getItems()[0].copyWithCount(64);
            handlers.get(0).storage.setStackInSlot(slot++, stack);
        }
        var direct = com.raishxn.gtna.common.machine.multiblock.godforge.GodforgeModuleMachine.recipeModifier(module,
                blast).apply(blast);
        int parallel = com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic.getParallelAmountWithoutEU(module,
                blast, 1024);
        helper.assertTrue(direct != null, "module modifier must accept " + blast.id + " parallel=" + parallel +
                " eut=" + com.gregtechceu.gtceu.api.recipe.RecipeHelper.getRealEUtWithIO(blast) + " heat=" +
                blast.data.getInt("ebf_temp") + " full=" + module.fullModifyRecipe(blast));
        boolean debit = com.raishxn.gtna.api.capability.WirelessEnergyManager.consumeDirectEnergy(helper.getLevel(),
                owner, new com.raishxn.gtna.utils.datastructure.Int128(1),
                net.minecraft.core.GlobalPos.of(helper.getLevel().dimension(), module.getPos()), "test");
        helper.assertTrue(debit, "wireless debit works; energy=" +
                com.raishxn.gtna.api.capability.WirelessEnergyManager.getEnergy(helper.getLevel(), owner) +
                " connected=" + module.isConnected());
        var modified = module.fullModifyRecipe(blast);
        var check = modified == null ? null : com.gregtechceu.gtceu.api.recipe.RecipeHelper.matchContents(module,
                modified);
        helper.assertTrue(modified != null && check.isSuccess(), "modified recipe must match: modified=" + modified +
                " check=" + (check == null ? null : check.reason()));
        boolean started = module.getRecipeLogic().checkMatchedRecipeAvailable(blast);
        var left = com.raishxn.gtna.api.capability.WirelessEnergyManager.getEnergy(helper.getLevel(), owner);
        helper.assertTrue(started && left.compareTo(budget) < 0 && stats.recipeTally > 1,
                "the module must start the recipe with parallels and pay EU from the wireless network (failures=" +
                        module.getRecipeLogic().getFailureReasons() + ", tally=" + stats.recipeTally + ")");
        helper.succeed();
    }

    @GameTest(template = "empty_16", timeoutTicks = 100)
    public static void godforgePlasmaRecipesFollowTheGtnhTable(GameTestHelper helper) {
        var recipes = helper.getLevel().getRecipeManager()
                .getAllRecipesFor(com.raishxn.gtna.common.data.GTNARecipeType.GODFORGE_PLASMA_RECIPES);
        helper.assertTrue(recipes.size() > 100, "plasma recipes generated for the GTNH table: " + recipes.size());
        var molten = helper.getLevel().getRecipeManager()
                .getAllRecipesFor(com.raishxn.gtna.common.data.GTNARecipeType.GODFORGE_MOLTEN_RECIPES);
        var blast = helper.getLevel().getRecipeManager()
                .getAllRecipesFor(com.gregtechceu.gtceu.common.data.GTRecipeTypes.BLAST_RECIPES);
        helper.assertTrue(molten.size() > 50 && molten.size() <= blast.size(),
                "every blast furnace recipe gets a molten copy: " + molten.size() + "/" + blast.size());
        helper.assertTrue(molten.stream().anyMatch(r -> !r.getOutputContents(
                com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability.CAP).isEmpty()),
                "molten copies output fluids");
        var iron = recipes.stream().filter(r -> r.id.getPath().endsWith("godforge_plasma_iron_dust")).findFirst();
        helper.assertTrue(iron.isPresent() && iron.get().duration == 20 &&
                iron.get().data.getInt(com.raishxn.gtna.data.recipe.GTNAGodforgeRecipes.PLASMA_TIER) == 0,
                "iron dust becomes plasma in one second at tier 0");
        var neutronium = recipes.stream().filter(r -> r.id.getPath().endsWith("godforge_plasma_neutronium_dust"))
                .findFirst();
        helper.assertTrue(neutronium.isPresent() && neutronium.get().data.getBoolean(
                com.raishxn.gtna.data.recipe.GTNAGodforgeRecipes.PLASMA_MULTISTEP) &&
                neutronium.get().data.getInt(
                        com.raishxn.gtna.data.recipe.GTNAGodforgeRecipes.PLASMA_TIER) == 2,
                "neutronium is an exotic multi-step plasma");
        helper.succeed();
    }

    @GameTest(template = "empty_16", timeoutTicks = 800)
    public static void godforgeExoticModuleTurnsExactPlasmasIntoQuarkGluonPlasma(GameTestHelper helper) {
        var definition = GTNAMachines3.GODFORGE_EXOTIC_MODULE;
        var shape = definition.getMatchingShapes().get(0).getBlocks();
        com.raishxn.gtna.common.machine.multiblock.godforge.GodforgeExoticModuleMachine module = null;
        for (int x = 0; x < shape.length; x++) {
            for (int y = 0; y < shape[x].length; y++) {
                for (int z = 0; z < shape[x][y].length; z++) {
                    var info = shape[x][y][z];
                    var pos = new BlockPos(x + 1, y + 60, z + 1);
                    helper.setBlock(pos, net.minecraft.world.level.block.Blocks.AIR);
                    if (info == null || info.getBlockState().isAir()) continue;
                    helper.setBlock(pos, info.getBlockState());
                    if (info.getBlockState().is(definition.getBlock())) {
                        module = (com.raishxn.gtna.common.machine.multiblock.godforge.GodforgeExoticModuleMachine) ((IMachineBlockEntity) helper
                                .getBlockEntity(pos)).getMetaMachine();
                    }
                }
            }
        }
        helper.assertTrue(module != null, "exotic module preview has a controller");
        var facing = module.getFrontFacing();
        var inputPos = module.getPos().relative(facing.getClockWise());
        var outputPos = module.getPos().relative(facing.getCounterClockWise());
        helper.getLevel().setBlockAndUpdate(inputPos,
                com.gregtechceu.gtceu.common.data.GTMachines.FLUID_IMPORT_HATCH_9X[com.gregtechceu.gtceu.api.GTValues.UHV]
                        .getBlock().defaultBlockState());
        helper.getLevel().setBlockAndUpdate(outputPos,
                com.gregtechceu.gtceu.common.data.GTMachines.FLUID_EXPORT_HATCH_9X[com.gregtechceu.gtceu.api.GTValues.UHV]
                        .getBlock().defaultBlockState());
        helper.assertTrue(module.checkPatternWithLock(),
                "the exotic module structure must form: " + module.getMultiblockState().error);
        module.onStructureFormed();
        var stats = module.godforgeStats();
        stats.calculatedMaxParallel = 4;
        stats.processingVoltage = com.gregtechceu.gtceu.api.GTValues.VA[com.gregtechceu.gtceu.api.GTValues.MAX];
        stats.speedBonus = 0.1;
        java.util.UUID owner = java.util.UUID.randomUUID();
        module.setNetworkOwner(owner);
        module.connect();
        var network = com.raishxn.gtna.common.data.NexusEnergyNetwork.get(helper.getLevel());
        network.setMatrixStats(owner, 1, com.gregtechceu.gtceu.api.GTValues.MAX, 0.95,
                new com.raishxn.gtna.utils.datastructure.Int128(Long.MAX_VALUE), true);
        network.setMatrixDimension(owner, helper.getLevel().dimension());
        com.raishxn.gtna.api.capability.WirelessEnergyManager.setEnergy(helper.getLevel(), owner,
                new com.raishxn.gtna.utils.datastructure.Int128(Long.MAX_VALUE));
        final var exotic = module;
        helper.runAfterDelay(45, () -> {
            var requirements = exotic.requirements();
            helper.assertTrue(!requirements.isEmpty() && requirements.size() <= 7,
                    "the module draws up to seven plasma inputs: " + requirements);
            var hatch = com.gregtechceu.gtceu.api.machine.MetaMachine.getMachine(helper.getLevel(), inputPos);
            var tank = ((com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine) hatch).tank;
            int index = 0;
            for (var requirement : requirements) {
                var fluid = net.minecraft.core.registries.BuiltInRegistries.FLUID.get(requirement.fluid());
                tank.getStorages()[index++].setFluid(new net.minecraftforge.fluids.FluidStack(fluid,
                        (int) requirement.amount()));
            }
        });
        helper.succeedWhen(() -> {
            var hatch = com.gregtechceu.gtceu.api.machine.MetaMachine.getMachine(helper.getLevel(), outputPos);
            var tank = ((com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine) hatch).tank;
            long qgp = 0;
            for (var storage : tank.getStorages()) {
                if (storage.getFluid().getFluid() ==
                        com.raishxn.gtna.common.data.GTNAMaterials.QuarkGluonPlasma.getFluid())
                    qgp += storage.getFluidAmount();
            }
            helper.assertTrue(qgp == 4000, "four parallels give 4000 mB of quark-gluon plasma: " + qgp +
                    " requirements=" + exotic.requirements());
        });
    }

    @GameTest(template = "empty_16", timeoutTicks = 100)
    public static void godforgeProgressionRecipesAndExtraCostsExist(GameTestHelper helper) {
        var lines = helper.getLevel().getRecipeManager()
                .getAllRecipesFor(com.gregtechceu.gtceu.common.data.GTRecipeTypes.ASSEMBLY_LINE_RECIPES);
        for (String id : new String[] { "forge_of_gods", "magnetic_confinement_casing", "stellar_energy_siphon_casing",
                "godforge_smelting_module", "godforge_exotic_module" }) {
            helper.assertTrue(lines.stream().anyMatch(r -> r.id.getPath().endsWith("godforge/" + id)),
                    "assembly line recipe for " + id);
        }
        for (var upgrade : new com.raishxn.gtna.api.machine.feature.godforge.GodforgeUpgrade[] {
                com.raishxn.gtna.api.machine.feature.godforge.GodforgeUpgrade.START,
                com.raishxn.gtna.api.machine.feature.godforge.GodforgeUpgrade.CD,
                com.raishxn.gtna.api.machine.feature.godforge.GodforgeUpgrade.END }) {
            helper.assertTrue(upgrade.hasExtraCost(), upgrade + " has the GTNH extra material cost");
        }
        helper.succeed();
    }
}
