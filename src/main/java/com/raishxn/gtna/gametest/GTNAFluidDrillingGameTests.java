package com.raishxn.gtna.gametest;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidVeinSavedData;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.FluidVeinWorldEntry;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.data.*;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.FluidDrillMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.*;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.crafting.PartialNBTIngredient;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.mojang.authlib.GameProfile;
import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.api.machine.feature.IFluidExtractionReceipt;
import com.raishxn.gtna.api.machine.multiblock.GTNAStructureRefresh;
import com.raishxn.gtna.common.data.GTNAItems;
import com.raishxn.gtna.common.data.GTNAMachines3;
import com.raishxn.gtna.common.data.GTNARecipeType;
import com.raishxn.gtna.common.data.fluid.FluidDiscoveryData;
import com.raishxn.gtna.common.item.DepositRecorderBehavior;
import com.raishxn.gtna.common.machine.multiblock.electric.VoidFluidDrillingRigMachine;
import com.raishxn.gtna.config.VoidFluidDrillConfig;

import java.util.UUID;

@GameTestHolder("gtna")
@PrefixGameTestTemplate(false)
public final class GTNAFluidDrillingGameTests {

    private static final BlockPos CONTROLLER = new BlockPos(5, 2, 4);
    private static final BlockPos ENERGY = CONTROLLER.offset(-1, 0, 2);
    private static final BlockPos INPUT = CONTROLLER.offset(0, 0, 2);
    private static final BlockPos DRILLING = CONTROLLER.offset(1, 0, 2);
    private static final BlockPos OUTPUT = CONTROLLER.offset(-1, 0, 1);
    private static final BlockPos MAINTENANCE = CONTROLLER.offset(1, 0, 1);
    private static final BlockPos PARALLEL = CONTROLLER.offset(0, 0, 1);
    private static final BlockPos NITROGEN = CONTROLLER.offset(-1, 0, 0);
    private static boolean remoteInjected;

    private static MetaMachine machine(GameTestHelper h, BlockPos p) {
        return ((MetaMachineBlockEntity) h.getBlockEntity(p)).getMetaMachine();
    }

    private static void shape(GameTestHelper h, boolean natural) {
        for (int x = -1; x <= 1; x++) for (int z = 0; z <= 2; z++) {
            h.setBlock(CONTROLLER.offset(x, 0, z),
                    natural ? GTBlocks.CASING_STEEL_SOLID.get() : GTBlocks.CASING_TITANIUM_STABLE.get());
        }
        for (int y = 1; y <= 3; y++) {
            h.setBlock(CONTROLLER.offset(0, y, 0), ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.Steel));
            h.setBlock(CONTROLLER.offset(-1, y, 1), ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.Steel));
            h.setBlock(CONTROLLER.offset(0, y, 1),
                    natural ? GTBlocks.CASING_STEEL_SOLID.get() : GTBlocks.CASING_TITANIUM_STABLE.get());
            h.setBlock(CONTROLLER.offset(1, y, 1), ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.Steel));
            h.setBlock(CONTROLLER.offset(0, y, 2), ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.Steel));
        }
        for (int y = 4; y <= 6; y++) {
            h.setBlock(CONTROLLER.offset(0, y, 1), ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.Steel));
        }
    }

    static VoidFluidDrillingRigMachine build(GameTestHelper h, int tier, UUID owner) {
        shape(h, false);
        h.setBlock(CONTROLLER, GTNAMachines3.VOID_FLUID_DRILLING_RIG.getBlock());
        h.setBlock(ENERGY, GTMachines.ENERGY_INPUT_HATCH[tier].getBlock());
        h.setBlock(INPUT, GTMachines.ITEM_IMPORT_BUS[GTValues.LV].getBlock());
        h.setBlock(DRILLING, GTMachines.FLUID_IMPORT_HATCH[GTValues.LV].getBlock());
        h.setBlock(NITROGEN, GTMachines.FLUID_IMPORT_HATCH[GTValues.LV].getBlock());
        h.setBlock(OUTPUT, GTMachines.FLUID_EXPORT_HATCH[GTValues.LV].getBlock());
        h.setBlock(MAINTENANCE, GTMachines.MAINTENANCE_HATCH.getBlock());
        if (tier >= GTValues.IV) h.setBlock(PARALLEL,
                com.gregtechceu.gtceu.common.data.machines.GCYMMachines.PARALLEL_HATCH[GTValues.IV].getBlock());
        var rig = (VoidFluidDrillingRigMachine) machine(h, CONTROLLER);
        rig.setOwnerUUID(owner);
        h.assertTrue(GTNAStructureRefresh.refresh(rig, true), "void fluid rig must form");
        ((MaintenanceHatchPartMachine) machine(h, MAINTENANCE)).fixAllMaintenanceProblems();
        rig.getRecipeLogic().updateTickSubscription();
        return rig;
    }

    static ItemStack card(GameTestHelper h, UUID owner, VoidFluidDrillConfig.Program p) {
        var scope = DepositRecorderBehavior
                .scope(com.gregtechceu.gtceu.common.machine.owner.MachineOwner.getOwner(owner), owner);
        var tag = DepositRecorderBehavior.programTag(p.dimension, p.fluid);
        tag.putUUID("owner", scope);
        tag.putUUID("certificate", FluidDiscoveryData.get(h.getLevel()).discover(scope, p.dimension, p.fluid));
        var card = GTNAItems.DEPOSIT_DATA.asStack();
        card.setTag(tag);
        return card;
    }

    static void insert(GameTestHelper h, ItemStack card, int drilling) {
        ((ItemBusPartMachine) machine(h, INPUT)).getInventory().setStackInSlot(0, card);
        ((FluidHatchPartMachine) machine(h, DRILLING)).tank.setFluidInTank(0,
                GTMaterials.DrillingFluid.getFluid(drilling));
    }

    static void ticks(GameTestHelper h, VoidFluidDrillingRigMachine rig, int count) {
        var energy = (EnergyHatchPartMachine) machine(h, ENERGY);
        for (int i = 0; i < count; i++) {
            energy.energyContainer.changeEnergy(1_000_000);
            rig.getRecipeLogic().serverTick();
        }
    }

    static int inputFluid(GameTestHelper h) {
        return ((FluidHatchPartMachine) machine(h, DRILLING)).tank.getFluidInTank(0).getAmount();
    }

    static int output(GameTestHelper h) {
        return ((FluidHatchPartMachine) machine(h, OUTPUT)).tank.getFluidInTank(0).getAmount();
    }

    private static GTRecipe oilRecipe(GameTestHelper h) {
        return h.getLevel().getRecipeManager().getRecipes().stream().filter(r -> r instanceof GTRecipe recipe &&
                recipe.recipeType == GTNARecipeType.VOID_FLUID_DRILLING_RECIPES &&
                recipe.data.getString("gtna_fluid_program").equals("oil")).map(GTRecipe.class::cast).findFirst()
                .orElseThrow();
    }

    @GameTest(template = "empty_12", timeoutTicks = 80)
    public static void nativeExtractionIsRequiredAndRecorded(GameTestHelper h) {
        shape(h, true);
        h.setBlock(CONTROLLER, GTMultiMachines.FLUID_DRILLING_RIG[GTValues.MV].getBlock());
        h.setBlock(ENERGY, GTMachines.ENERGY_INPUT_HATCH[GTValues.MV].getBlock());
        h.setBlock(OUTPUT, GTMachines.FLUID_EXPORT_HATCH[GTValues.LV].getBlock());
        var rig = (FluidDrillMachine) machine(h, CONTROLLER);
        var player = FakePlayerFactory.get(h.getLevel(), new GameProfile(UUID.randomUUID(), "FluidRecorder"));
        rig.setOwnerUUID(player.getUUID());
        var absolute = h.absolutePos(CONTROLLER);
        var pos = new ChunkPos(absolute);
        BedrockFluidVeinSavedData.getOrCreate(h.getLevel()).veinFluids.put(pos,
                new FluidVeinWorldEntry(GTBedrockFluids.OIL, 100, BedrockFluidVeinSavedData.MAXIMUM_VEIN_OPERATIONS));
        h.assertTrue(GTNAStructureRefresh.refresh(rig, true), "native rig must form");
        var receipt = (IFluidExtractionReceipt) rig.getRecipeLogic();
        var behavior = new DepositRecorderBehavior();
        player.setItemInHand(InteractionHand.MAIN_HAND, GTNAItems.DEPOSIT_RECORDER.asStack());
        player.getInventory().setItem(1, GTItems.TOOL_DATA_STICK.asStack());
        UseOnContext context = new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(absolute), Direction.NORTH, absolute, false));
        h.assertTrue(behavior.onItemUseFirst(player.getMainHandItem(), context) == InteractionResult.FAIL,
                "an unrun rig must not issue discovery data");
        h.assertTrue(player.getInventory().getItem(1).getCount() == 1, "failed discovery must not consume the stick");
        var out = (FluidHatchPartMachine) machine(h, OUTPUT);
        out.tank.setFluidInTank(0, GTMaterials.Water.getFluid(8000));
        var energy = (EnergyHatchPartMachine) machine(h, ENERGY);
        rig.getRecipeLogic().updateTickSubscription();
        for (int i = 0; i < 40; i++) {
            energy.energyContainer.changeEnergy(1_000_000);
            rig.getRecipeLogic().serverTick();
        }
        h.assertTrue(receipt.gtna$getExtractedFluid() == null, "full output must not count as successful extraction");
        out.tank.setFluidInTank(0, net.minecraftforge.fluids.FluidStack.EMPTY);
        for (int i = 0; i < 60; i++) {
            energy.energyContainer.changeEnergy(1_000_000);
            rig.getRecipeLogic().serverTick();
        }
        h.assertTrue(receipt.gtna$getExtractedFluid() == GTMaterials.Oil.getFluid(),
                "actual cycle must issue the extraction receipt");
        h.assertTrue(out.tank.getFluidInTank(0).getAmount() > 0, "native rig must actually deliver oil");
        h.assertTrue(behavior.onItemUseFirst(player.getMainHandItem(), context) == InteractionResult.SUCCESS,
                "recorder must generate data after successful extraction");
        ItemStack recorded = ItemStack.EMPTY;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i).is(GTNAItems.DEPOSIT_DATA.get()))
                recorded = player.getInventory().getItem(i);
        }
        h.assertTrue(DepositRecorderBehavior.validCard(h.getLevel(), recorded, DepositRecorderBehavior.owner(player),
                "minecraft:overworld", "gtceu:oil"), "recorded certificate must validate");
        player.setItemInHand(InteractionHand.OFF_HAND, recorded.copy());
        player.getInventory().setItem(2, GTItems.TOOL_DATA_STICK.asStack());
        h.assertTrue(
                behavior.use(GTNAItems.DEPOSIT_RECORDER.get(), h.getLevel(), player, InteractionHand.MAIN_HAND)
                        .getResult() == InteractionResult.SUCCESS,
                "owned reusable data must be copyable away from the deposit");
        h.succeed();
    }

    @GameTest(template = "empty_12", timeoutTicks = 80)
    public static void fluidProgramConsumesOnceAndPreservesData(GameTestHelper h) {
        UUID owner = UUID.randomUUID();
        var rig = build(h, GTValues.EV, owner);
        var p = VoidFluidDrillConfig.get().program("oil");
        ItemStack data = card(h, owner, p);
        insert(h, data, 100);
        ticks(h, rig, 450);
        h.assertTrue(output(h) == 2000,
                "one EV cycle must produce 2000 mB oil: " + output(h) + " " + rig.getRecipeLogic().getFailureReasons());
        h.assertTrue(inputFluid(h) == 0, "one cycle must consume 100 mB drilling fluid");
        h.assertTrue(
                ItemStack.isSameItemSameTags(data,
                        ((ItemBusPartMachine) machine(h, INPUT)).getInventory().getStackInSlot(0)),
                "discovery data must remain intact");
        h.succeed();
    }

    @GameTest(template = "empty_12", timeoutTicks = 80)
    public static void fullOutputPausesWithoutLosingPaidCycle(GameTestHelper h) {
        UUID owner = UUID.randomUUID();
        var rig = build(h, GTValues.EV, owner);
        insert(h, card(h, owner, VoidFluidDrillConfig.get().program("oil")), 100);
        var out = (FluidHatchPartMachine) machine(h, OUTPUT);
        out.tank.setFluidInTank(0, GTMaterials.Water.getFluid(8000));
        ticks(h, rig, 20);
        h.assertTrue(inputFluid(h) == 100, "full output at start must preserve reagents");
        out.tank.setFluidInTank(0, net.minecraftforge.fluids.FluidStack.EMPTY);
        rig.getRecipeLogic().markLastRecipeDirty();
        var raw = oilRecipe(h);
        var modified = rig.fullModifyRecipe(raw);
        h.assertTrue(modified != null, "open output must allow modification; gate=" + rig.isProgramAllowed(raw) +
                " out=" + out.tank.getFluidInTank(0));
        h.assertTrue(com.gregtechceu.gtceu.api.recipe.RecipeHelper.matchContents(rig, modified).isSuccess(),
                "open output must match modified recipe: " +
                        com.gregtechceu.gtceu.api.recipe.RecipeHelper.matchContents(rig, modified));
        rig.getRecipeLogic().findAndHandleRecipe();
        ticks(h, rig, 30);
        h.assertTrue(inputFluid(h) == 0, "a cycle must start after space opens");
        out.tank.setFluidInTank(0, GTMaterials.Water.getFluid(8000));
        ticks(h, rig, 450);
        h.assertTrue(out.tank.getFluidInTank(0).getFluid() == GTMaterials.Water.getFluid(),
                "blocked cycle must not overwrite the tank");
        out.tank.setFluidInTank(0, net.minecraftforge.fluids.FluidStack.EMPTY);
        ticks(h, rig, 5);
        h.assertTrue(output(h) == 2000, "the already-paid cycle must finish once space opens");
        ticks(h, rig, 450);
        h.assertTrue(output(h) == 2000, "the released output must not be duplicated");
        h.succeed();
    }

    @GameTest(template = "empty_12", timeoutTicks = 80)
    public static void certificatesCannotUnlockOtherOwnersOrUnknownFluids(GameTestHelper h) {
        UUID owner = UUID.randomUUID();
        var rig = build(h, GTValues.EV, owner);
        var p = VoidFluidDrillConfig.get().program("oil");
        ItemStack data = card(h, UUID.randomUUID(), p);
        insert(h, data, 100);
        h.assertFalse(rig.isProgramAllowed(oilRecipe(h)), "another team's discovery must not unlock this rig");
        data = card(h, owner, p);
        data.getTag().putUUID("certificate", UUID.randomUUID());
        insert(h, data, 100);
        ticks(h, rig, 40);
        h.assertTrue(inputFluid(h) == 100 && output(h) == 0, "invented card certificate must not consume or produce");
        var scope = DepositRecorderBehavior.scope(rig.getOwner(), owner);
        var store = new FluidDiscoveryData();
        UUID certificate = store.discover(scope, p.dimension, p.fluid);
        var loaded = FluidDiscoveryData.load(store.save(new CompoundTag()));
        h.assertTrue(loaded.allows(certificate, scope, p.dimension, p.fluid), "discovery must survive NBT reload");
        h.assertFalse(loaded.allows(certificate, scope, p.dimension, "gtceu:radon"),
                "discovery must remain fluid-specific");
        h.assertFalse(loaded.allows(certificate, scope, "ad_astra:mars", p.fluid),
                "discovery must remain origin-specific");
        h.succeed();
    }

    @GameTest(template = "empty_12", timeoutTicks = 80)
    public static void ivParallelScalesFluidAndOutput(GameTestHelper h) {
        UUID owner = UUID.randomUUID();
        var rig = build(h, GTValues.IV, owner);
        var hatch = (ParallelHatchPartMachine) machine(h, PARALLEL);
        hatch.setCurrentParallel(2);
        insert(h, card(h, owner, VoidFluidDrillConfig.get().program("oil")), 200);
        h.assertTrue(rig.parallelCap() == 2, "IV parallel cap must be 2");
        var raw = oilRecipe(h);
        var modified = rig.fullModifyRecipe(raw);
        h.assertTrue(modified != null,
                "parallel candidate must exist: gate=" + rig.isProgramAllowed(raw) + " item=" +
                        com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability.CAP.getMaxParallelByInput(rig,
                                raw, 2, false) +
                        " fluid=" + com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability.CAP
                                .getMaxParallelByInput(rig, raw, 2, false));
        rig.getRecipeLogic().markLastRecipeDirty();
        ticks(h, rig, 30);
        h.assertTrue(
                rig.getRecipeLogic().getLastRecipe() != null && rig.getRecipeLogic().getLastRecipe().parallels == 2,
                "IV must run two paid operations together: " + rig.getRecipeLogic().getFailureReasons() +
                        " last=" + rig.getRecipeLogic().getLastRecipe() + " tier=" + rig.operatingTier() + " hatch=" +
                        rig.getParallelHatch().map(x -> x.getCurrentParallel()) + " fluid=" + inputFluid(h));
        h.assertTrue(rig.getRecipeLogic().getLastRecipe().getInputEUt().getTotalEU() == 3840,
                "two parallels must reserve their 3840 EU/t before OC");
        ticks(h, rig, 450);
        h.assertTrue(inputFluid(h) == 0 && output(h) == 4000, "two parallels must consume 200 and produce 4000 mB");
        h.assertTrue(((ItemBusPartMachine) machine(h, INPUT)).getInventory().getStackInSlot(0).getCount() == 1,
                "parallel must not require or consume two discovery cards");
        h.succeed();
    }

    @GameTest(template = "empty_12", timeoutTicks = 80)
    public static void ftbPartySharesDiscoveryAndRejectsOutsiders(GameTestHelper h) throws ReflectiveOperationException {
        if (com.gregtechceu.gtceu.GTCEu.Mods.isFTBTeamsLoaded()) FTBFluidDiscoveryTests.run(h);
        else h.succeed();
    }

    private static synchronized void injectRemote(GameTestHelper h) {
        if (remoteInjected) return;
        remoteInjected = true;
        var p = new VoidFluidDrillConfig.Program("gametest_remote_oil", "oil", 2000);
        p.dimension = "minecraft:the_nether";
        VoidFluidDrillConfig.get().programs.add(p);
        var type = GTNARecipeType.VOID_FLUID_DRILLING_RECIPES;
        type.getAdditionHandler().beginStaging();
        // Completing staging replaces the DB; preserve all real programs in this test fixture.
        for (var recipe : h.getLevel().getRecipeManager().getRecipes()) {
            if (recipe instanceof GTRecipe gt && gt.recipeType == type) type.getAdditionHandler().addStaging(gt);
        }
        type.getAdditionHandler().addStaging(type.recipeBuilder(GTNACORE.id(p.id))
                .notConsumable(PartialNBTIngredient.of(GTNAItems.DEPOSIT_DATA.get(),
                        DepositRecorderBehavior.programTag(p.dimension, p.fluid)))
                .inputFluids(GTMaterials.DrillingFluid.getFluid(100)).outputFluids(GTMaterials.Oil.getFluid(2000))
                .duration(400).EUt(1920).addData("gtna_fluid_program", p.id).buildRawRecipe());
        type.getAdditionHandler().completeStaging();
    }

    @GameTest(template = "empty_12", timeoutTicks = 80)
    public static void remoteUpgradeNeedsTierAndExistingDiscovery(GameTestHelper h) {
        injectRemote(h);
        UUID owner = UUID.randomUUID();
        var rig = build(h, GTValues.IV, owner);
        var p = VoidFluidDrillConfig.get().program("gametest_remote_oil");
        insert(h, card(h, owner, p), 100);
        ticks(h, rig, 20);
        h.assertTrue(inputFluid(h) == 100 && output(h) == 0, "source origin must be required without an upgrade");
        ((ItemBusPartMachine) machine(h, INPUT)).getInventory().setStackInSlot(1,
                GTNAItems.FLUID_REMOTE_UPGRADES.get("terrestrial").asStack());
        ticks(h, rig, 250);
        h.assertTrue(output(h) == 2000, "an IV upgrade must allow remote production of the recorded origin");
        h.assertTrue(((ItemBusPartMachine) machine(h, INPUT)).getInventory().getStackInSlot(1).getCount() == 1,
                "remote upgrade must remain reusable");
        h.setBlock(ENERGY, GTMachines.ENERGY_INPUT_HATCH[GTValues.EV].getBlock());
        GTNAStructureRefresh.refresh(rig, true);
        insert(h, card(h, owner, p), 100);
        ticks(h, rig, 30);
        h.assertTrue(inputFluid(h) == 100, "EV must not run the IV remote upgrade");
        h.succeed();
    }

    @GameTest(template = "empty_12", timeoutTicks = 80)
    public static void gasRequiresItsExtraReagentAndInvalidConfigFailsClosed(GameTestHelper h) {
        UUID owner = UUID.randomUUID();
        var rig = build(h, GTValues.EV, owner);
        var p = VoidFluidDrillConfig.get().program("natural_gas");
        insert(h, card(h, owner, p), 100);
        ticks(h, rig, 20);
        h.assertTrue(inputFluid(h) == 100, "gas cannot run without nitrogen");
        ((FluidHatchPartMachine) machine(h, NITROGEN)).tank.setFluidInTank(0, GTMaterials.Nitrogen.getFluid(100));
        ticks(h, rig, 450);
        h.assertTrue(output(h) == 1500 && inputFluid(h) == 0, "gas must consume common and special reagents");
        h.assertTrue(((FluidHatchPartMachine) machine(h, NITROGEN)).tank.getFluidInTank(0).isEmpty(),
                "gas nitrogen cost must be exact");
        var config = VoidFluidDrillConfig.defaults();
        config.programs.get(0).drillingFluid = 0;
        boolean rejected = false;
        try {
            config.validate();
        } catch (IllegalArgumentException expected) {
            rejected = true;
        }
        h.assertTrue(rejected, "zero drilling fluid must be rejected instead of granting free production");
        h.succeed();
    }

    @GameTest(template = "empty_12", timeoutTicks = 80)
    public static void controllerRecipePreservesTerrestrialEvBootstrap(GameTestHelper h) {
        var recipe = h.getLevel().getRecipeManager().getRecipes().stream()
                .filter(r -> r instanceof GTRecipe gt && gt.recipeType == GTRecipeTypes.ASSEMBLER_RECIPES &&
                        gt.id.getPath().endsWith("/void_fluid_drilling_rig"))
                .map(GTRecipe.class::cast).findFirst().orElseThrow();
        var inputs = recipe.getInputContents(com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability.CAP);
        h.assertTrue(inputs.stream()
                .anyMatch(c -> com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability.CAP.of(c.content)
                        .test(GTMultiMachines.FLUID_DRILLING_RIG[GTValues.MV].asStack())),
                "controller must use an accessible MV drilling rig");
        h.assertTrue(inputs.stream()
                .anyMatch(c -> com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability.CAP.of(c.content)
                        .test(GTItems.SENSOR_HV.asStack())),
                "controller sensors must not require the first Quantum Eye");
        h.assertTrue(recipe.duration == 600 && recipe.getInputEUt().getTotalEU() == 1920,
                "controller must still require EV assembly");
        h.succeed();
    }

    private static ItemStack extractMarsData(GameTestHelper h, UUID owner) {
        var mars = h.getLevel().getServer().getLevel(net.minecraft.resources.ResourceKey.create(
                net.minecraft.core.registries.Registries.DIMENSION,
                net.minecraft.resources.ResourceLocation.parse("ad_astra:mars")));
        h.assertTrue(mars != null, "the GTIA profile requires an actual Mars ServerLevel");
        var anchor = new BlockPos(514, 240, 514);
        // An isolated fixture above the terrain, with normal GTCEu deposit generation.
        // Never insert a synthetic Radon vein or grant a certificate directly in this path.
        shape(h, true);
        h.setBlock(CONTROLLER, GTMultiMachines.FLUID_DRILLING_RIG[GTValues.MV].getBlock());
        h.setBlock(ENERGY, GTMachines.ENERGY_INPUT_HATCH[GTValues.MV].getBlock());
        h.setBlock(OUTPUT, GTMachines.FLUID_EXPORT_HATCH[GTValues.LV].getBlock());
        var positions = new java.util.ArrayList<BlockPos>();
        var veins = BedrockFluidVeinSavedData.getOrCreate(mars);
        var chunk = new ChunkPos(anchor);
        var oldVein = veins.veinFluids.remove(chunk);
        try {
            for (int x = -1; x <= 1; x++) for (int y = 0; y <= 6; y++) for (int z = 0; z <= 2; z++) {
                var target = anchor.offset(x, y, z);
                h.assertTrue(mars.getBlockState(target).isAir(), "Mars fixture must use empty space: " + target);
                positions.add(target);
                mars.setBlockAndUpdate(target,
                        h.getLevel().getBlockState(h.absolutePos(CONTROLLER.offset(x, y, z))));
            }
            h.assertTrue(veins.getFluidInChunk(chunk.x, chunk.z) == GTMaterials.Radon.getFluid(),
                    "normal GTCEu generation must select the configured Mars Radon deposit");
            var rig = (FluidDrillMachine) ((MetaMachineBlockEntity) mars.getBlockEntity(anchor)).getMetaMachine();
            var player = FakePlayerFactory.get(mars, new GameProfile(owner, "MarsFluidRecorder"));
            rig.setOwnerUUID(owner);
            h.assertTrue(GTNAStructureRefresh.refresh(rig, true), "native rig must form on Mars");
            var behavior = new DepositRecorderBehavior();
            player.setItemInHand(InteractionHand.MAIN_HAND, GTNAItems.DEPOSIT_RECORDER.asStack());
            player.getInventory().setItem(1, GTItems.TOOL_DATA_STICK.asStack());
            var context = new UseOnContext(player, InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(anchor), Direction.NORTH, anchor, false));
            h.assertTrue(behavior.onItemUseFirst(player.getMainHandItem(), context) == InteractionResult.FAIL,
                    "a generated Mars deposit alone must not grant discovery");
            h.assertTrue(player.getInventory().getItem(1).getCount() == 1,
                    "premature recording on Mars must preserve the Data Stick");
            var energy = (EnergyHatchPartMachine) ((MetaMachineBlockEntity) mars.getBlockEntity(
                    anchor.offset(-1, 0, 2))).getMetaMachine();
            var output = (FluidHatchPartMachine) ((MetaMachineBlockEntity) mars.getBlockEntity(
                    anchor.offset(-1, 0, 1))).getMetaMachine();
            rig.getRecipeLogic().updateTickSubscription();
            for (int tick = 0; tick < 60; tick++) {
                energy.energyContainer.changeEnergy(1_000_000);
                rig.getRecipeLogic().serverTick();
            }
            h.assertTrue(output.tank.getFluidInTank(0).getFluid() == GTMaterials.Radon.getFluid() &&
                    output.tank.getFluidInTank(0).getAmount() > 0,
                    "native Mars rig must actually output Radon before recording");
            h.assertTrue(behavior.onItemUseFirst(player.getMainHandItem(), context) == InteractionResult.SUCCESS,
                    "recorder must certify actual Mars extraction");
            for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
                var card = player.getInventory().getItem(slot);
                if (card.is(GTNAItems.DEPOSIT_DATA.get())) {
                    h.assertTrue(DepositRecorderBehavior.validCard(h.getLevel(), card,
                            DepositRecorderBehavior.owner(player), "ad_astra:mars", "gtceu:radon"),
                            "a certificate issued on Mars must validate in the Overworld");
                    return card.copy();
                }
            }
            throw new IllegalStateException("Mars recorder did not return discovery data");
        } finally {
            for (var position : positions) mars.setBlockAndUpdate(position,
                    net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            if (oldVein == null) veins.veinFluids.remove(chunk);
            else veins.veinFluids.put(chunk, oldVein);
        }
    }

    @GameTest(template = "empty_12", timeoutTicks = 80)
    public static void gtiaMarsRadonProfileNeedsDiscoveryUpgradeAndFilter(GameTestHelper h) {
        var p = VoidFluidDrillConfig.get().program("mars_radon");
        // The default GTNA profile intentionally has no planetary worldgen or production programs.
        if (p == null) {
            h.succeed();
            return;
        }
        var deposit = com.gregtechceu.gtceu.api.registry.GTRegistries.BEDROCK_FLUID_DEFINITIONS.get(
                net.minecraft.resources.ResourceLocation.parse("gtia:mars_radon"));
        h.assertTrue(deposit != null && deposit.getStoredFluid().get() == GTMaterials.Radon.getFluid(),
                "GTIA profile must load the Radon deposit");
        h.assertTrue(
                deposit.getDimensionFilter().size() == 1 &&
                        deposit.getDimensionFilter().iterator().next().location().toString().equals("ad_astra:mars"),
                "Radon deposit must remain specific to Mars");
        UUID owner = UUID.randomUUID();
        var recorded = extractMarsData(h, owner);
        var rig = build(h, GTValues.LuV, owner);
        ((ParallelHatchPartMachine) machine(h, PARALLEL)).setCurrentParallel(1);
        insert(h, recorded, 100);
        var bus = (ItemBusPartMachine) machine(h, INPUT);
        bus.getInventory().setStackInSlot(1, GTNAItems.FLUID_REMOTE_UPGRADES.get("terrestrial").asStack());
        bus.getInventory().setStackInSlot(2, GTNAItems.FLUID_SEPARATION_FILTER.asStack());
        ((FluidHatchPartMachine) machine(h, NITROGEN)).tank.setFluidInTank(0, GTMaterials.Nitrogen.getFluid(100));
        ticks(h, rig, 20);
        h.assertTrue(inputFluid(h) == 100, "a terrestrial upgrade must not allow remote Mars Radon");
        bus.getInventory().setStackInSlot(1, GTNAItems.FLUID_REMOTE_UPGRADES.get("t2").asStack());
        rig.getRecipeLogic().markLastRecipeDirty();
        rig.getRecipeLogic().findAndHandleRecipe();
        ticks(h, rig, 15);
        h.assertTrue(inputFluid(h) == 0 && bus.getInventory().getStackInSlot(2).isEmpty(),
                "Radon must consume common reagent and one filter at start");
        var data = bus.getInventory().getStackInSlot(0).copy();
        bus.getInventory().setStackInSlot(0, ItemStack.EMPTY);
        ticks(h, rig, 150);
        h.assertTrue(output(h) == 0, "removing data must pause the paid cycle");
        bus.getInventory().setStackInSlot(0, data);
        ticks(h, rig, 150);
        var out = (FluidHatchPartMachine) machine(h, OUTPUT);
        h.assertTrue(out.tank.getFluidInTank(0).getFluid() == GTMaterials.Radon.getFluid() && output(h) == 50,
                "restoring data must finish exactly 50 mB Radon, with no second payment");
        h.assertTrue(
                bus.getInventory().getStackInSlot(0).getCount() == 1 &&
                        bus.getInventory().getStackInSlot(1).getCount() == 1,
                "Mars data and T2 upgrade must remain reusable");
        GTNACORE.LOGGER.info("GTIA Mars native extraction -> recorded data -> remote Radon: PASS");
        h.succeed();
    }
}
