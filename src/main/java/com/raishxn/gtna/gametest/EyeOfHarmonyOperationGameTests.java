package com.raishxn.gtna.gametest;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;

import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.raishxn.gtna.api.capability.WirelessEnergyManager;
import com.raishxn.gtna.api.machine.feature.eyeofharmony.EyeOfHarmonyMath;
import com.raishxn.gtna.common.data.GTNAEyeOfHarmonyContent;
import com.raishxn.gtna.common.data.NexusEnergyNetwork;
import com.raishxn.gtna.common.data.multiblock.EyeOfHarmonyCatalog;
import com.raishxn.gtna.common.machine.multiblock.noenergy.EyeOfHarmonyMachine;
import com.raishxn.gtna.utils.datastructure.Int128;

import java.math.BigInteger;
import java.util.UUID;

@GameTestHolder("gtna")
@PrefixGameTestTemplate(false)
public final class EyeOfHarmonyOperationGameTests {

    @GameTest(template = "empty_16", timeoutTicks = 40)
    public static void eyeOfHarmonyOverworldCatalogUsesRealDustsPlasmasAndFuelCost(GameTestHelper helper) {
        var catalog = EyeOfHarmonyCatalog.build(helper.getLevel());
        helper.assertTrue(catalog.program().startupEU() > 36_000_188_743_680_000L,
                "real eligible plasmas must add a real fuel-derived cost");
        var products = catalog.products();
        var items = products.getList("items", 10);
        var fluids = products.getList("fluids", 10);
        helper.assertTrue(items.size() > 10 && fluids.size() >= 3, "Overworld catalog must contain a real ore pool");
        for (int i = 0; i < items.size(); i++) {
            helper.assertTrue(
                    !ItemStack.of(items.getCompound(i)).isEmpty() && items.getCompound(i).getLong("remaining") > 0,
                    "catalog must never manufacture air or a zero quantity");
        }
        for (int i = 0; i < fluids.size() - 2; i++) helper.assertTrue(
                fluids.getCompound(i).getLong("remaining") == 8_000_000_000L,
                "each plasma output must be eight million buckets, stored as eight billion mB");
        helper.assertTrue(fluids.getCompound(fluids.size() - 2).getLong("remaining") == 100_000 &&
                fluids.getCompound(fluids.size() - 1).getLong("remaining") == 1152,
                "Overworld raw stellar matter and white dwarf quantities must match GTNH");
        long dust = 0;
        for (int i = 0; i < items.size() - 1; i++) dust += items.getCompound(i).getLong("remaining");
        helper.assertTrue(items.getCompound(items.size() - 1).getLong("remaining") == 3 * dust,
                "stone dust must be three times the processed resource sum");
        helper.succeed();
    }

    @GameTest(template = "empty_16", timeoutTicks = 40)
    public static void eyeOfHarmonyDisplayKeepsEveryProductAndLongAmount(GameTestHelper helper) {
        var catalog = EyeOfHarmonyCatalog.build(helper.getLevel().getRecipeManager());
        var pages = com.raishxn.gtna.common.data.multiblock.EyeOfHarmonyDisplay.pages(catalog);
        var expected = new java.util.ArrayList<CompoundTag>();
        for (String kind : java.util.List.of("items", "fluids")) {
            for (var tag : catalog.products().getList(kind, 10)) expected.add((CompoundTag) tag);
        }
        var actual = pages.stream().flatMap(page -> page.products().stream()).toList();
        helper.assertTrue(actual.size() == expected.size(), "all outputs must be displayed exactly once");
        for (int i = 0; i < actual.size(); i++) {
            helper.assertTrue(actual.get(i).tag().equals(expected.get(i)),
                    "display identity and amount must match the executing catalog");
        }
        for (int capacity : new int[] { 36, 54, 99 }) {
            var sized = com.raishxn.gtna.common.data.multiblock.EyeOfHarmonyDisplay.pages(catalog, capacity);
            var flattened = sized.stream().flatMap(page -> page.products().stream()).toList();
            helper.assertTrue(
                    flattened.size() == expected.size() && sized.stream()
                            .allMatch(page -> page.rows() == capacity / 9 && page.products().size() <= capacity),
                    "adaptive viewer must paginate without discarding outputs at capacity " + capacity);
            for (int index = 0; index < expected.size(); index++) helper.assertTrue(
                    flattened.get(index).tag().equals(expected.get(index)),
                    "adaptive output identity/amount must match");
        }
        for (int capacity : new int[] { 9, 18, 36, 99 }) {
            var reflowed = pages.stream().flatMap(
                    page -> com.raishxn.gtna.common.data.multiblock.EyeOfHarmonyDisplay.reflow(page, capacity).stream())
                    .toList();
            var flattened = reflowed.stream().flatMap(page -> page.products().stream()).toList();
            helper.assertTrue(flattened.size() == expected.size(), "EMI small panels must retain every output");
            helper.assertTrue(reflowed.stream().allMatch(page -> page.products().size() <= capacity),
                    "EMI output pages must fit allocated rows");
            for (int index = 0; index < expected.size(); index++) helper.assertTrue(
                    flattened.get(index).tag().equals(expected.get(index)),
                    "EMI reflow must retain product order, identity and long amount");
        }
        for (int itemRows : new int[] { 1, 4, 9 }) {
            for (int fluidRows : new int[] { 1, 2 }) {
                var separated = com.raishxn.gtna.common.data.multiblock.EyeOfHarmonyDisplay
                        .separatedPages(catalog, itemRows, fluidRows);
                for (boolean fluid : new boolean[] { false, true }) {
                    var family = separated.stream().flatMap(part -> part.products().stream())
                            .filter(product -> product.fluid() == fluid).toList();
                    var expectedFamily = catalog.products().getList(fluid ? "fluids" : "items", 10);
                    helper.assertTrue(family.size() == expectedFamily.size(),
                            "separate grids must index every product exactly once");
                    for (int i = 0; i < family.size(); i++) helper.assertTrue(
                            family.get(i).tag().equals(expectedFamily.getCompound(i)),
                            "separate grids preserve family order, identity and exact long quantities");
                    for (var part : separated) helper.assertTrue(
                            part.products().stream().filter(product -> product.fluid() == fluid).count() <=
                                    (fluid ? fluidRows : itemRows) * 9,
                            "each family must fit its own region");
                }
                var reflowed = separated.stream()
                        .flatMap(part -> com.raishxn.gtna.common.data.multiblock.EyeOfHarmonyDisplay
                                .separated(part, 1, 1).stream())
                        .flatMap(part -> part.products().stream()).toList();
                helper.assertTrue(reflowed.size() == expected.size(),
                        "reflow of already-separated pages must neither repeat nor lose products");
            }
        }
        var single = com.raishxn.gtna.common.data.multiblock.EyeOfHarmonyDisplay.singlePage(catalog);
        helper.assertTrue(single.number() == 1 && single.total() == 1 && single.products().size() == expected.size(),
                "one EMI recipe must still index the complete output catalog");
        for (int itemRows : new int[] { 1, 9 }) {
            var preview = com.raishxn.gtna.common.data.multiblock.EyeOfHarmonyDisplay.singlePreview(single, itemRows,
                    2);
            helper.assertTrue(preview.number() == 1 && preview.total() == 1, "preview never creates a second page");
            for (boolean fluid : new boolean[] { false, true }) {
                var family = preview.products().stream().filter(product -> product.fluid() == fluid).toList();
                var original = catalog.products().getList(fluid ? "fluids" : "items", 10);
                helper.assertTrue(family.size() == Math.min(original.size(), (fluid ? 2 : itemRows) * 9),
                        "single-page preview fills each independent region");
                for (int i = 0; i < family.size(); i++) helper.assertTrue(
                        family.get(i).tag().equals(original.getCompound(i)),
                        "preview preserves exact displayed products");
            }
            helper.assertTrue(single.products().size() == expected.size(), "preview cannot trim indexed outputs");
        }
        var copy = catalog.products().copy();
        copy.getList("items", 10).getCompound(0).putLong("remaining", 5_000_000_001L);
        var large = com.raishxn.gtna.common.data.multiblock.EyeOfHarmonyDisplay.pages(
                new EyeOfHarmonyCatalog.Catalog(catalog.program(), copy));
        copy.getList("items", 10).getCompound(0).putLong("remaining", 1);
        helper.assertTrue(large.get(0).products().get(0).amount() == 5_000_000_001L,
                "display must preserve amounts above int and detach product tags from runtime mutations");
        helper.succeed();
    }

    @GameTest(template = "empty_48", timeoutTicks = 600)
    public static void eyeOfHarmonyQueriesMissingResourcesAndUnsupportedPlanetNeverDebit(GameTestHelper helper) throws Exception {
        var fixture = fixture(helper);
        var machine = fixture.machine;
        set(machine, "hydrogen", 1_000_000_000L);
        set(machine, "helium", 1_000_000_000L);
        machine.getPlanetInventory().setStackInSlot(0, GTNAEyeOfHarmonyContent.NETHER_PLANET.asStack());
        machine.tickPlanetary(true);
        helper.assertTrue(machine.getCycleState() == 0 && balance(helper, fixture.owner) == 10_000,
                "unsupported planet must never debit");
        machine.getPlanetInventory().setStackInSlot(0, GTNAEyeOfHarmonyContent.OVERWORLD_PLANET.asStack());
        set(machine, "helium", 999_999_999L);
        machine.tickPlanetary(true);
        helper.assertTrue(machine.getCycleState() == 0 && balance(helper, fixture.owner) == 10_000,
                "one missing mB must prevent startup");
        set(machine, "helium", 1_000_000_000L);
        WirelessEnergyManager.setEnergy(helper.getLevel(), fixture.owner, new Int128(999));
        machine.tickPlanetary(true);
        var recipe = com.raishxn.gtna.common.data.GTNARecipeType.COSMOS_SIMULATION_RECIPES
                .recipeBuilder("eoh_preview_fixture").duration(10).buildRawRecipe();
        for (int i = 0; i < 5; i++) EyeOfHarmonyMachine.recipeModifier(machine, recipe);
        helper.assertTrue(machine.getCycleState() == 0 && balance(helper, fixture.owner) == 999 &&
                gas(machine, "hydrogen") == 1_000_000_000L && gas(machine, "helium") == 1_000_000_000L,
                "failed debit and repeated modifiers must leave gas and network untouched");
        helper.succeed();
    }

    @GameTest(template = "empty_48", timeoutTicks = 600)
    public static void eyeOfHarmonyGasAbsorptionCircuitRangeAndDisableAreRespected(GameTestHelper helper) throws Exception {
        var fixture = fixture(helper);
        var machine = fixture.machine;
        for (var part : machine.getParts()) {
            if (part instanceof FluidHatchPartMachine hatch &&
                    hatch.tank.handlerIO == com.gregtechceu.gtceu.api.capability.recipe.IO.IN) {
                ((com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEFluidList) hatch.tank)
                        .getInventory()[0].addStack(new appeng.api.stacks.GenericStack(
                                appeng.api.stacks.AEFluidKey.of(GTMaterials.Hydrogen.getFluid()), 1234));
            }
            if (part instanceof ItemBusPartMachine bus &&
                    bus.getInventory().handlerIO == com.gregtechceu.gtceu.api.capability.recipe.IO.IN) {
                bus.getCircuitInventory().setStackInSlot(0, IntCircuitBehaviour.stack(32));
            }
        }
        machine.getRecipeLogic().setWorkingEnabled(false);
        machine.tickPlanetary(true);
        helper.assertTrue(gas(machine, "hydrogen") == 0, "disabled machine must not absorb gas");
        machine.getRecipeLogic().setWorkingEnabled(true);
        machine.tickPlanetary(true);
        helper.assertTrue(gas(machine, "hydrogen") == 2468,
                "each hatch must drain its entire available gas, not a fixed batch");
        helper.assertTrue(machine.getStartupEnergy().toBigInteger().equals(BigInteger.valueOf(1000L).shiftLeft(48)),
                "modern virtual circuit slot must clamp 32 to 24");
        helper.assertTrue(machine.getCycleState() == 0 && balance(helper, fixture.owner) == 10_000,
                "gas absorption is not an operation debit");
        helper.succeed();
    }

    @GameTest(template = "empty_48", timeoutTicks = 600)
    public static void eyeOfHarmonyPaidCycleSurvivesReloadPausesAndBlocksOwnerSwap(GameTestHelper helper) throws Exception {
        var fixture = fixture(helper);
        var machine = fixture.machine;
        start(machine);
        helper.assertTrue(machine.getCycleState() == 1 && balance(helper, fixture.owner) == 9000,
                "startup must debit exactly once");
        helper.assertTrue(gas(machine, "hydrogen") == 0 && gas(machine, "helium") == 0,
                "startup empties both complete internal buffers");
        helper.assertTrue((long) get(machine, "cycleHydrogen") == 1_000_000_000L &&
                (long) get(machine, "cycleHelium") == 1_000_000_000L,
                "consumed gas snapshot must remain visible while next-cycle buffers are empty");
        var debit = NexusEnergyNetwork.get(helper.getLevel()).getLastDirectDebit(fixture.owner);
        helper.assertTrue(debit != null && debit.source().pos().equals(machine.getPos()) &&
                debit.amount().toLong() == 1000,
                "the EOH startup must register its direct withdrawal source and exact debit");
        var restoredNetwork = new NexusEnergyNetwork(NexusEnergyNetwork.get(helper.getLevel()).save(new CompoundTag()));
        helper.assertTrue(restoredNetwork.getLastDirectDebit(fixture.owner).amount().equals(debit.amount()) &&
                restoredNetwork.getLastDirectDebit(fixture.owner).source().equals(debit.source()),
                "direct withdrawal remains visible after network serialization");
        var debitLines = new java.util.ArrayList<net.minecraft.network.chat.Component>();
        com.raishxn.gtna.common.item.NexusDirectDebitDisplay.append(debitLines, restoredNetwork, fixture.owner);
        helper.assertTrue(debitLines.size() == 1 && debitLines.get(0).getStyle().getHoverEvent() != null,
                "shared terminal/matrix direct-debit display must include the exact tooltip");

        machine.tickPlanetary(false);
        CompoundTag saved = machine.getHolder().getSelf().saveWithFullMetadata();
        var frozen = machine.getPendingProducts();
        machine.getPlanetInventory().setStackInSlot(0, GTNAEyeOfHarmonyContent.END_PLANET.asStack());
        machine.tickPlanetary(false);
        machine.getHolder().getSelf().load(saved);
        helper.assertTrue((long) get(machine, "cycleHydrogen") == 1_000_000_000L &&
                (long) get(machine, "cycleHelium") == 1_000_000_000L,
                "paid gas snapshot must survive block-entity reload");
        helper.assertTrue(machine.getCycleProgress() == 1 && machine.getPendingProducts().equals(frozen),
                "reload restores progress, selected planet and resolved products without a new roll");
        helper.assertTrue(balance(helper, fixture.owner) == 9000, "reload must not debit");
        machine.getRecipeLogic().setWorkingEnabled(false);
        machine.tickPlanetary(false);
        helper.assertTrue(machine.getCycleProgress() == 1, "disabled paid cycle pauses");
        machine.getRecipeLogic().setWorkingEnabled(true);
        machine.onStructureInvalid();
        machine.tickPlanetary(false);
        helper.assertTrue(machine.getCycleProgress() == 1, "unformed paid cycle pauses");
        helper.assertTrue(machine.checkPatternWithLock(), "unchanged structure can reform");
        machine.onStructureFormed();
        var player = net.minecraftforge.common.util.FakePlayerFactory.get(helper.getLevel(),
                new com.mojang.authlib.GameProfile(UUID.randomUUID(), "EOHRebindQA"));
        player.setItemInHand(InteractionHand.MAIN_HAND, GTItems.TOOL_DATA_STICK.asStack());
        machine.onUse(machine.getBlockState(), helper.getLevel(), machine.getPos(), player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(machine.getPos()), Direction.NORTH, machine.getPos(), false));
        helper.assertTrue(get(machine, "networkOwner").equals(fixture.owner),
                "cannot redirect a paid cycle to another owner");
        machine.tickPlanetary(false);
        machine.tickPlanetary(false);
        helper.assertTrue(machine.getCycleState() == 2 && balance(helper, fixture.owner) == 9000,
                "finish does not re-debit and delivers on next tick");
        CompoundTag dropped = new CompoundTag();
        machine.saveCustomPersistedData(dropped, true);
        helper.assertTrue(dropped.getCompound("EyeOfHarmonyOperation").getInt("cycleState") == 2,
                "controller item drop must carry paid operation recovery state");
        helper.succeed();
    }

    @GameTest(template = "empty_48", timeoutTicks = 600)
    public static void eyeOfHarmonyMEOutputsAndPartialNexusCreditRemainPendingAcrossReload(GameTestHelper helper) throws Exception {
        var fixture = fixture(helper);
        var machine = fixture.machine;
        var itemOutput = machine.getParts().stream()
                .filter(part -> part instanceof com.gregtechceu.gtceu.integration.ae2.machine.MEOutputBusPartMachine)
                .findFirst().orElseThrow();
        var fluidOutput = machine.getParts().stream()
                .filter(part -> part instanceof com.gregtechceu.gtceu.integration.ae2.machine.MEOutputHatchPartMachine)
                .findFirst().orElseThrow();
        start(machine);
        for (int i = 0; i < 3; i++) machine.tickPlanetary(false);
        var network = NexusEnergyNetwork.get(helper.getLevel());
        network.setEnergy(fixture.owner, Int128.ZERO());
        network.setMaxCapacity(fixture.owner, new Int128(100));
        machine.tickPlanetary(false);
        long pending = Long.parseLong((String) get(machine, "pendingEU"));
        helper.assertTrue(machine.getCycleState() == 2 && pending > 0,
                "full Nexus capacity must retain the unpaid energy even after ME accepts products");
        var iron = ChemicalHelper.get(TagPrefix.dust, GTMaterials.Iron);
        helper.assertTrue(meAmount(itemOutput, appeng.api.stacks.AEItemKey.of(iron)) == 70 &&
                meAmount(fluidOutput, appeng.api.stacks.AEFluidKey.of(GTMaterials.Hydrogen.getFluid())) == 500,
                "offline ME queues must accept exact item/fluid totals");
        CompoundTag saved = new CompoundTag();
        machine.saveCustomPersistedData(saved, false);
        machine.loadCustomPersistedData(saved);
        machine.tickPlanetary(false);
        helper.assertTrue(Long.parseLong((String) get(machine, "pendingEU")) == pending &&
                meAmount(itemOutput, appeng.api.stacks.AEItemKey.of(iron)) == 70,
                "reload must neither credit twice nor repeat delivered ME products");
        network.setMaxCapacity(fixture.owner, new Int128(1000));
        machine.tickPlanetary(false);
        helper.assertTrue(machine.getCycleState() == 0 && get(machine, "pendingEU").equals("0"),
                "cycle returns to idle only after products and gross EU are accepted");
        var queueSaved = meBuffer(itemOutput).serializeNBT();
        var restoredQueue = new com.gregtechceu.gtceu.integration.ae2.utils.KeyStorage();
        restoredQueue.deserializeNBT(queueSaved);
        helper.assertTrue(restoredQueue.storage.getLong(appeng.api.stacks.AEItemKey.of(iron)) == 70,
                "ME queue serializes exact outstanding network delivery");
        var balance = WirelessEnergyManager.getEnergy(helper.getLevel(), fixture.owner).toBigInteger();
        for (int i = 0; i < 5; i++) machine.tickPlanetary(false);
        helper.assertTrue(
                WirelessEnergyManager.getEnergy(helper.getLevel(), fixture.owner).toBigInteger().equals(balance),
                "completed operation cannot credit again");
        helper.succeed();
    }

    @GameTest(template = "empty_48", timeoutTicks = 600)
    public static void eyeOfHarmonyFailedCycleConsumesExcessButStillReturnsEnergy(GameTestHelper helper) throws Exception {
        var fixture = fixture(helper);
        set(fixture.machine, "hydrogen", 2_000_000_000L);
        set(fixture.machine, "helium", 2_000_000_000L);
        fixture.machine.tickPlanetary(true);
        helper.assertTrue(!(boolean) get(fixture.machine, "cycleSuccess") &&
                fixture.machine.getPendingProducts().getList("items", 10).isEmpty(),
                "zero chance failure produces no success resources");
        helper.assertTrue(gas(fixture.machine, "hydrogen") == 0 && gas(fixture.machine, "helium") == 0,
                "all excess gas is consumed at startup");
        for (int i = 0; i < 4; i++) fixture.machine.tickPlanetary(false);
        helper.assertTrue(fixture.machine.getCycleState() == 0 && balance(helper, fixture.owner) > 9000,
                "failed operation still returns its energy once");
        helper.succeed();
    }

    @GameTest(template = "empty_48", timeoutTicks = 600)
    public static void eyeOfHarmonyLegacyPaidRecipeFinishesWithoutRepeating(GameTestHelper helper) throws Exception {
        var fixture = fixture(helper);
        var machine = fixture.machine;
        var recipe = com.raishxn.gtna.common.data.GTNARecipeType.COSMOS_SIMULATION_RECIPES
                .recipeBuilder("eoh_legacy_fixture").outputItems(Items.COAL).duration(1).buildRawRecipe();
        var logic = machine.getRecipeLogic();
        logic.setupRecipe(recipe);
        helper.assertTrue(logic.isActive(), "simulate an already-paid legacy active recipe");
        logic.onRecipeFinish();
        for (int i = 0; i < 5; i++) logic.serverTick();
        helper.assertTrue(!logic.isActive() && logic.getLastRecipe() == null && logic.isWorkingEnabled(),
                "legacy finish must clear the recipe and leave the planetary operation enabled");
        var output = machine.getParts().stream()
                .filter(part -> part instanceof com.gregtechceu.gtceu.integration.ae2.machine.MEOutputBusPartMachine)
                .findFirst().orElseThrow();
        helper.assertTrue(meAmount(output, appeng.api.stacks.AEItemKey.of(new ItemStack(Items.COAL))) == 1 &&
                balance(helper, fixture.owner) == 10_000,
                "legacy ME output occurs once without charging another startup");
        helper.succeed();
    }

    @GameTest(template = "empty_48", timeoutTicks = 600)
    public static void eyeOfHarmonyMEBulkDeliveryBudgetPersistsCursor(GameTestHelper helper) throws Exception {
        var fixture = fixture(helper);
        var machine = fixture.machine;
        var output = machine.getParts().stream()
                .filter(part -> part instanceof com.gregtechceu.gtceu.integration.ae2.machine.MEOutputBusPartMachine)
                .findFirst().orElseThrow();
        var items = new ListTag();
        long perEntry = 3_000_000_000L;
        for (int i = 0; i < 100; i++) {
            var tag = ChemicalHelper.get(TagPrefix.dust, GTMaterials.Iron).save(new CompoundTag());
            tag.putLong("remaining", perEntry);
            items.add(tag);
        }
        var products = new CompoundTag();
        products.put("items", items);
        products.put("fluids", new ListTag());
        set(machine, "pendingProducts", products);
        set(machine, "cycleState", 2);
        set(machine, "cycleOwner", fixture.owner);
        machine.tickPlanetary(false);
        helper.assertTrue(machine.getCycleState() == 2 &&
                meAmount(output,
                        appeng.api.stacks.AEItemKey.of(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Iron))) ==
                        64L * Integer.MAX_VALUE,
                "one tick must perform exactly 64 ME batches, preserving values above int");
        var saved = new CompoundTag();
        machine.saveCustomPersistedData(saved, false);
        machine.loadCustomPersistedData(saved);
        for (int i = 0; i < 6; i++) machine.tickPlanetary(false);
        helper.assertTrue(machine.getCycleState() == 0 &&
                meAmount(output,
                        appeng.api.stacks.AEItemKey.of(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Iron))) ==
                        100L * perEntry,
                "cursor/reload must eventually deliver every long quantity once");
        helper.succeed();
    }

    private static com.gregtechceu.gtceu.integration.ae2.utils.KeyStorage meBuffer(Object part) throws Exception {
        var field = part.getClass().getDeclaredField("internalBuffer");
        field.setAccessible(true);
        return (com.gregtechceu.gtceu.integration.ae2.utils.KeyStorage) field.get(part);
    }

    private static long meAmount(Object part, appeng.api.stacks.AEKey key) throws Exception {
        return meBuffer(part).storage.getLong(key);
    }

    private static Fixture fixture(GameTestHelper helper) throws Exception {
        var assembly = EyeOfHarmonyStructureGameTests.build(helper, Direction.NORTH, 0, 0, 0);
        var machine = assembly.machine();
        helper.assertTrue(machine.checkPatternWithLock(), "operation fixture uses the real 33-cube structure");
        machine.onStructureFormed();
        var owner = UUID.randomUUID();
        set(machine, "networkOwner", owner);
        var network = NexusEnergyNetwork.get(helper.getLevel());
        network.setMatrixStats(owner, 1, GTValues.LV, 0.95, new Int128(10_000), true);
        network.setMatrixDimension(owner, helper.getLevel().dimension());
        network.setEnergy(owner, new Int128(10_000));
        machine.getPlanetInventory().setStackInSlot(0, GTNAEyeOfHarmonyContent.OVERWORLD_PLANET.asStack());
        // Small deterministic catalog tests production lifecycle without simulating five real hours.
        var items = new ListTag();
        var item = ChemicalHelper.get(TagPrefix.dust, GTMaterials.Iron).save(new CompoundTag());
        item.putLong("remaining", 70);
        items.add(item);
        var fluids = new ListTag();
        fluids.add(EyeOfHarmonyCatalog.fluid(GTMaterials.Hydrogen.getFluid(), 500));
        var products = new CompoundTag();
        products.put("items", items);
        products.put("fluids", fluids);
        set(machine, "catalog", new EyeOfHarmonyCatalog.Catalog(new EyeOfHarmonyMath.Program(0, 0, 3,
                1_000_000_000L, 1_000_000_000L, 1000, 600, 1), products));
        return new Fixture(machine, owner);
    }

    @GameTest(template = "empty_48", timeoutTicks = 600)
    public static void eyeOfHarmonyNormalSpeedCircuitPreviewAndPaidPlanStayConsistent(GameTestHelper helper)
                                                                                                             throws Exception {
        var fixture = fixture(helper);
        var machine = fixture.machine;
        var original = (EyeOfHarmonyCatalog.Catalog) get(machine, "catalog");
        set(machine, "catalog", new EyeOfHarmonyCatalog.Catalog(
                new EyeOfHarmonyMath.Program(0, 0, 360_000, 1_000_000_000L, 1_000_000_000L, 1000, 600, 1),
                original.products()));
        var input = (ItemBusPartMachine) machine.getParts().stream()
                .filter(part -> part instanceof ItemBusPartMachine bus && bus.getInventory().handlerIO ==
                        com.gregtechceu.gtceu.api.capability.recipe.IO.IN)
                .findFirst().orElseThrow();
        var network = NexusEnergyNetwork.get(helper.getLevel());
        network.setEnergy(fixture.owner, Int128.ZERO());
        set(machine, "hydrogen", 1_000_000_000L);
        set(machine, "helium", 1_000_000_000L);
        for (int circuit = 0; circuit <= 24; circuit++) {
            input.getCircuitInventory().setStackInSlot(0, IntCircuitBehaviour.stack(circuit));
            machine.tickPlanetary(true);
            helper.assertTrue(machine.getStartupEnergy().toBigInteger().equals(
                    BigInteger.valueOf(1000L).shiftLeft(2 * circuit)),
                    "controller quote must match the 4^k execution cost: " + circuit);
            helper.assertTrue((int) get(machine, "previewDuration") == Math.max(360_000 >> circuit, 1),
                    "real virtual ME circuit slot must halve duration in the controller preview: " + circuit);
        }
        input.getCircuitInventory().setStackInSlot(0, IntCircuitBehaviour.stack(4));
        network.setMaxCapacity(fixture.owner, new Int128(1_000_000));
        network.setEnergy(fixture.owner, new Int128(1_000_000));
        machine.tickPlanetary(true);
        helper.assertTrue(machine.getCycleState() == 1 && (int) get(machine, "cycleDuration") == 22_500,
                "paid operation must freeze the same 22,500-tick duration as circuit 4 preview");
        input.getCircuitInventory().setStackInSlot(0, IntCircuitBehaviour.stack(8));
        machine.tickPlanetary(true);
        helper.assertTrue(machine.getCycleProgress() == 1 && (int) get(machine, "cycleDuration") == 22_500,
                "normal speed advances one tick and an ongoing paid plan ignores later circuit changes");
        helper.succeed();
    }

    @GameTest(template = "empty_16", timeoutTicks = 40)
    public static void artificialStarFuelCanFundCircuit24AfterRebalance(GameTestHelper helper) {
        var recipes = helper.getLevel().getRecipeManager()
                .getAllRecipesFor(com.raishxn.gtna.common.data.GTNARecipeType.ARTIFICIAL_STAR_RECIPES);
        String[] fuels = { "neutronium", "draconium", "cosmic_neutronium", "infinity" };
        long[] expectedVoltage = { 8_796_093_022_208L, 140_737_488_355_328L,
                2_251_799_813_685_248L, 36_028_797_018_963_968L };
        BigInteger infinityEnergy = BigInteger.ZERO;
        for (int i = 0; i < fuels.length; i++) {
            String id = fuels[i] + "_antimatter_fuel_rod";
            var recipe = recipes.stream().filter(candidate -> candidate.id.getPath().endsWith("/" + id))
                    .findFirst().orElseThrow();
            helper.assertTrue(recipe.duration == 200 && recipe.getOutputEUt().voltage() == expectedVoltage[i],
                    "all four fuels receive 16x generation while retaining the 200-tick base duration: " + id);
            if (i == 3) infinityEnergy = BigInteger.valueOf(recipe.getOutputEUt().voltage())
                    .multiply(BigInteger.valueOf(recipe.duration));
        }
        var containers = new java.util.ArrayList<com.gregtechceu.gtceu.api.capability.IEnergyContainer>();
        for (int i = 0; i < 16; i++) {
            var pos = new net.minecraft.core.BlockPos(i, 1, 1);
            helper.setBlock(pos,
                    com.raishxn.gtna.common.data.GTNAEnergyHatches.WIRELESS_DYNAMO_HATCHES[GTValues.MAX][10]
                            .getBlock());
            var holder = (com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity) helper.getBlockEntity(pos);
            var hatch = (com.raishxn.gtna.common.machine.multiblock.part.energy.WirelessDynamoHatchPartMachine) holder
                    .getMetaMachine();
            helper.assertTrue(hatch.energyContainer.getEnergyCapacity() > 0, "dynamo buffer must not overflow");
            containers.add(hatch.energyContainer);
        }
        var combined = new com.gregtechceu.gtceu.api.misc.EnergyContainerList(containers);
        helper.assertTrue(combined.getOutputVoltage() * combined.getOutputAmperage() >= expectedVoltage[3],
                "16 existing MAX dynamos must actually transport the buffed Infinity EU/t");
        var catalog = EyeOfHarmonyCatalog.build(helper.getLevel().getRecipeManager());
        var plan = EyeOfHarmonyMath.plan(catalog.program(), new EyeOfHarmonyMath.Fields(0, 0, 0), 24,
                1_000_000_000L, 1_000_000_000L, EyeOfHarmonyMath.History.fresh());
        helper.assertTrue(plan.durationTicks() == 1 && plan.debitEU().equals(
                BigInteger.valueOf(catalog.program().startupEU()).shiftLeft(48)),
                "circuit 24 must use a one-tick cycle and the GTNH 4^24 startup cost");
        helper.succeed();
    }

    private static void start(EyeOfHarmonyMachine machine) throws Exception {
        set(machine, "hydrogen", 1_000_000_000L);
        set(machine, "helium", 1_000_000_000L);
        machine.tickPlanetary(true);
    }

    private static Object get(EyeOfHarmonyMachine machine, String name) throws Exception {
        var field = EyeOfHarmonyMachine.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(machine);
    }

    private static void set(EyeOfHarmonyMachine machine, String name, Object value) throws Exception {
        var field = EyeOfHarmonyMachine.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(machine, value);
    }

    private static long gas(EyeOfHarmonyMachine machine, String name) throws Exception {
        return (long) get(machine, name);
    }

    private static long balance(GameTestHelper helper, UUID owner) {
        return WirelessEnergyManager.getEnergy(helper.getLevel(), owner).toBigInteger().longValueExact();
    }

    private record Fixture(EyeOfHarmonyMachine machine, UUID owner) {}
}
