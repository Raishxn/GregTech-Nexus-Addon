package com.raishxn.gtna.common.machine.multiblock.godforge;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.MultiblockState;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.lowdragmc.lowdraglib.utils.BlockInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

import com.raishxn.gtna.api.machine.feature.godforge.GodforgeData;
import com.raishxn.gtna.api.machine.feature.godforge.GodforgeMath;
import com.raishxn.gtna.api.machine.feature.godforge.GodforgeModuleStats;
import com.raishxn.gtna.api.machine.feature.godforge.GodforgeUpgrade;
import com.raishxn.gtna.api.machine.feature.godforge.GodforgeUpgradeStorage;
import com.raishxn.gtna.common.data.GTNAGodforgeContent;
import com.raishxn.gtna.common.data.GTNAMaterials;
import com.raishxn.gtna.common.data.multiblock.GTNAMultiBlockFileReader;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import static com.gregtechceu.gtceu.api.pattern.Predicates.*;

/**
 * Forge of Gods controller, ported from GTNH {@code MTEForgeOfGods} (GT5-Unofficial a3e1e112). It burns stellar fuel
 * into an internal battery, unlocks upgrades with graviton shards and drives connected modules every five seconds.
 */
public class ForgeOfGodsMachine extends WorkableMultiblockMachine
                                implements com.gregtechceu.gtceu.api.machine.feature.IMachineLife,
                                com.gregtechceu.gtceu.api.machine.feature.IUIMachine,
                                com.raishxn.gtna.api.machine.multiblock.ISubPatternMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(ForgeOfGodsMachine.class,
            WorkableMultiblockMachine.MANAGED_FIELD_HOLDER);

    public static final String MODULES_KEY = "gtna_godforge_modules";
    private static final int UPDATE_PERIOD = 100;

    private final GodforgeData data = new GodforgeData();
    private final List<BlockPos> modulePositions = new ArrayList<>();

    @Persisted
    @Nullable
    private UUID networkOwner;

    // Client-visible star state for the renderer.
    @DescSynced
    private boolean renderActive;
    @DescSynced
    private int syncedRingAmount = 1;
    @DescSynced
    private int syncedStarSize = GodforgeData.DEFAULT_STAR_SIZE;
    @DescSynced
    private int syncedRotationSpeed = GodforgeData.DEFAULT_ROTATION_SPEED;
    @DescSynced
    private String syncedStarColor = com.raishxn.gtna.api.machine.feature.godforge.GodforgeStarColor.DEFAULT
            .serialize();

    /** Full forge state for the GUI on the client (GTNH syncs the same data through its sync hypervisor). */
    @DescSynced
    private CompoundTag syncedData = new CompoundTag();
    @DescSynced
    private int connectedModules;
    /** {@code storedUpgradeWindowItems}: the manual insertion window of upgrade extra costs. */
    @Persisted
    private final com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler upgradeWindow = new com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler(
            16);
    @Nullable
    private GodforgeData clientView;
    @Nullable
    private CompoundTag clientViewTag;

    @Nullable
    private TickableSubscription tickSubscription;
    private long ticker;

    public ForgeOfGodsMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    public GodforgeData data() {
        return data;
    }

    /** The live data on the server, a copy rebuilt from the synced tag on the client. */
    public GodforgeData view() {
        if (!isRemote()) return data;
        if (clientView == null || clientViewTag != syncedData) {
            GodforgeData copy = new GodforgeData();
            GodforgeDataNbt.load(copy, syncedData);
            GodforgeMath.milestones(copy);
            GodforgeMath.inversionStatus(copy);
            clientView = copy;
            clientViewTag = syncedData;
        }
        return clientView;
    }

    public int connectedModules() {
        return connectedModules;
    }

    public com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler upgradeWindow() {
        return upgradeWindow;
    }

    public boolean isRenderActive() {
        return renderActive;
    }

    public int ringAmount() {
        return syncedRingAmount;
    }

    public int starSize() {
        return syncedStarSize;
    }

    public int rotationSpeed() {
        return syncedRotationSpeed;
    }

    public String starColor() {
        return syncedStarColor;
    }

    // ---- Pattern ----

    public static BlockPattern createPattern(MultiblockMachineDefinition definition) {
        return withBlocks(GTNAMultiBlockFileReader.start(definition, "god_forge"), definition).build();
    }

    private static Supplier<BlockPattern> ringPattern(String name) {
        return com.google.common.base.Suppliers.memoize(() -> withBlocks(GTNAMultiBlockFileReader.start(null, name),
                null).build());
    }

    private static final Supplier<BlockPattern> RING_2 = ringPattern("god_forge_ring_2");
    private static final Supplier<BlockPattern> RING_3 = ringPattern("god_forge_ring_3");
    private static final Supplier<BlockPattern> RING_2_AIR = ringPattern("god_forge_ring_2_air");
    private static final Supplier<BlockPattern> RING_3_AIR = ringPattern("god_forge_ring_3_air");
    @Nullable
    private BlockPattern activePattern;

    /** GTNH checks {@code STRUCTURE_PIECE_SHAFT} + {@code FIRST_RING_AIR} while the rings are in the controller. */
    @Override
    public BlockPattern getPattern() {
        if (!data.renderActive) return super.getPattern();
        if (activePattern == null) {
            activePattern = withBlocks(GTNAMultiBlockFileReader.start(getDefinition(), "god_forge_active"),
                    getDefinition()).build();
        }
        return activePattern;
    }

    private static com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern withBlocks(
                                                                                    com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern pattern,
                                                                                    @Nullable MultiblockMachineDefinition definition) {
        TraceabilityPredicate controller = definition == null ? controller(any()) :
                controller(blocks(definition.get()));
        return pattern.where('~', controller)
                .where('A', blocks(GTNAGodforgeContent.MAGNETIC_CONFINEMENT_CASING.get())
                        .or(standardHatch(PartAbility.IMPORT_ITEMS))
                        .or(standardHatch(PartAbility.IMPORT_FLUIDS))
                        .or(standardHatch(PartAbility.EXPORT_ITEMS)))
                .where('B', blocks(GTNAGodforgeContent.SINGULARITY_SHIELDING_CASING.get()))
                .where('C', blocks(GTNAGodforgeContent.GUIDANCE_CASING.get()))
                .where('D', blocks(GTNAGodforgeContent.BOUNDLESS_STRUCTURE_CASING.get()))
                .where('E', blocks(GTNAGodforgeContent.MAGNETIC_CONFINEMENT_CASING.get()))
                .where('F', blocks(GTNAGodforgeContent.STELLAR_ENERGY_SIPHON_CASING.get()))
                .where('G', blocks(GTNAGodforgeContent.REMOTE_GRAVITON_FLOW_MODULATOR.get()))
                .where('H', blocks(GTNAGodforgeContent.GRAVITATIONAL_LENS.get()))
                .where('I', blocks(GTNAGodforgeContent.CENTRAL_GRAVITON_FLOW_MODULATOR.get()))
                .where('J', moduleSlot())
                .where('K', blocks(GTNAGodforgeContent.MEDIAL_GRAVITON_FLOW_MODULATOR.get()))
                .where('L', air())
                .where(' ', any());
    }

    /**
     * GTNH {@code checkOneInputBus/Hatch/OutputBus}: exactly one of each. The preview and the Nexus Terminal only
     * offer GTCEu's regular buses and hatches (other parts sharing the ability, such as tesseracts, still match).
     */
    private static TraceabilityPredicate standardHatch(PartAbility ability) {
        TraceabilityPredicate predicate = abilities(ability).setExactLimit(1);
        for (var simple : predicate.limited) {
            var all = simple.candidates;
            if (all == null) continue;
            simple.candidates = () -> java.util.Arrays.stream(all.get())
                    .filter(info -> net.minecraft.core.registries.BuiltInRegistries.BLOCK
                            .getKey(info.getBlockState().getBlock()).getNamespace().equals("gtceu"))
                    .toArray(BlockInfo[]::new);
        }
        return predicate;
    }

    /** A module controller or the shielding casing; module positions are collected for the controller. */
    private static TraceabilityPredicate moduleSlot() {
        return new TraceabilityPredicate(state -> {
            if (state.getBlockState().is(GTNAGodforgeContent.SINGULARITY_SHIELDING_CASING.get())) return true;
            if (MetaMachine.getMachine(state.getWorld(), state.getPos()) instanceof IGodforgeModule) {
                List<BlockPos> modules = state.getMatchContext().getOrCreate(MODULES_KEY, ArrayList::new);
                modules.add(state.getPos().immutable());
                return true;
            }
            return false;
        }, () -> new BlockInfo[] { BlockInfo.fromBlock(GTNAGodforgeContent.SINGULARITY_SHIELDING_CASING.get()) });
    }

    private boolean checkRing(Supplier<BlockPattern> pattern) {
        if (!(getLevel() instanceof ServerLevel level)) return false;
        return pattern.get().checkPatternAt(new MultiblockState(level, getPos()), getPos(), getFrontFacing(),
                getUpwardsFacing(), isFlipped(), false);
    }

    // ---- Lifecycle ----

    @Override
    public void onMachinePlaced(@Nullable LivingEntity player, ItemStack stack) {
        if (player != null) networkOwner = player.getUUID();
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        modulePositions.clear();
        List<BlockPos> found = getMultiblockState().getMatchContext().get(MODULES_KEY);
        if (found != null) modulePositions.addAll(found);
        updateRings();
        tickSubscription = subscribeServerTick(tickSubscription, this::godforgeTick);
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        forEachModule(IGodforgeModule::disconnect);
        modulePositions.clear();
        if (tickSubscription != null) {
            tickSubscription.unsubscribe();
            tickSubscription = null;
        }
    }

    /**
     * GTNH {@code survivalConstruct}: the second ring is buildable once CD is unlocked, the third with END. Exposed
     * as sub-patterns so the Nexus Terminal's module build places them.
     */
    @Override
    public List<BlockPattern> gtna$getSubPatterns() {
        GodforgeData view = view();
        if (!view.isUpgradeActive(GodforgeUpgrade.CD)) return List.of();
        if (!view.isUpgradeActive(GodforgeUpgrade.END)) return List.of(RING_2.get());
        return List.of(RING_2.get(), RING_3.get());
    }

    private int formedSubPatterns;

    @Override
    public int gtna$formedModuleCount() {
        return formedSubPatterns;
    }

    @Override
    public void gtna$setFormedModuleCount(int count) {
        formedSubPatterns = count;
    }

    /** Ring section of {@code checkMachine}: rings 2 and 3 count with CD and END and join an active star. */
    private void updateRings() {
        if (!(getLevel() instanceof ServerLevel level)) return;
        if (data.isUpgradeActive(GodforgeUpgrade.CD)) {
            if (checkRing(RING_2)) {
                data.ringAmount = 2;
                if (!data.rendererDisabled && data.renderActive) GodforgeRings.destroy(level, ring(2));
            }
            if (data.renderActive && data.ringAmount >= 2 && !checkRing(RING_2_AIR)) destroyRenderer();
        } else {
            if (data.ringAmount == 3) buildRing(3);
            if (data.ringAmount >= 2) {
                data.ringAmount = 1;
                buildRing(2);
            }
        }
        if (data.isUpgradeActive(GodforgeUpgrade.END)) {
            if (checkRing(RING_3)) {
                data.ringAmount = 3;
                if (!data.rendererDisabled && data.renderActive) GodforgeRings.destroy(level, ring(3));
            }
            if (data.renderActive && data.ringAmount == 3 && !checkRing(RING_3_AIR)) destroyRenderer();
        } else if (data.ringAmount == 3) {
            data.ringAmount = 2;
            buildRing(3);
        }
        syncVisuals();
    }

    private java.util.Map<BlockPos, Character> ring(int ring) {
        return GodforgeRings.positions(getLevel(), getPos(), getFrontFacing(), getUpwardsFacing(), isFlipped(), ring);
    }

    /** Rebuilds a ring that was taken into the controller (only while the star holds it). */
    private void buildRing(int ring) {
        if (data.renderActive) GodforgeRings.build(getLevel(), ring(ring));
    }

    /** {@code createRenderer}: the star appears and the rings are taken into the controller. */
    private void createRenderer() {
        if (!(getLevel() instanceof ServerLevel level) || data.renderActive) return;
        data.renderActive = true;
        for (int ring = 1; ring <= data.ringAmount; ring++) GodforgeRings.destroy(level, ring(ring));
        syncVisuals();
        markDirty();
    }

    /** {@code destroyRenderer}: the star goes out and the rings are placed back. */
    private void destroyRenderer() {
        if (!(getLevel() instanceof ServerLevel level) || !data.renderActive) return;
        data.renderActive = false;
        for (int ring = 1; ring <= data.ringAmount; ring++) GodforgeRings.build(level, ring(ring));
        syncVisuals();
        markDirty();
    }

    @Override
    public void onMachineRemoved() {
        destroyRenderer();
        clearInventory(upgradeWindow);
    }

    /** Rotation is locked while the rings are inside the controller, as in {@code isRotationChangeAllowed}. */
    @Override
    public boolean isFacingValid(net.minecraft.core.Direction facing) {
        if (data.renderActive && facing != getFrontFacing()) return false;
        return super.isFacingValid(facing);
    }

    @Override
    protected net.minecraft.world.InteractionResult onScrewdriverClick(net.minecraft.world.entity.player.Player player,
                                                                       net.minecraft.world.InteractionHand hand,
                                                                       net.minecraft.core.Direction gridSide,
                                                                       net.minecraft.world.phys.BlockHitResult hit) {
        if (isRemote()) return net.minecraft.world.InteractionResult.SUCCESS;
        toggleRenderer();
        player.sendSystemMessage(net.minecraft.network.chat.Component.translatable(data.rendererDisabled ?
                "gtna.godforge.animations.disabled" : "gtna.godforge.animations.enabled"));
        return net.minecraft.world.InteractionResult.CONSUME;
    }

    private void godforgeTick() {
        if (!isFormed() || !(getLevel() instanceof ServerLevel)) return;
        if (++ticker % UPDATE_PERIOD != 0) return;
        if (networkOwner == null) networkOwner = getOwnerUUID();

        updateRings();
        absorbStartupFuelAndShards();
        if (data.internalBattery != 0 && !data.renderActive && !data.rendererDisabled) createRenderer();
        if (data.internalBattery != 0) drainFuel();

        List<GodforgeModuleStats> moduleStats = new ArrayList<>();
        forEachModule(module -> moduleStats.add(module.godforgeStats()));
        GodforgeMath.compositionLevel(data, moduleStats);
        GodforgeMath.milestones(data);
        GodforgeMath.inversionStatus(data);
        GodforgeMath.gravitonShardAmount(data);
        if (data.isUpgradeActive(GodforgeUpgrade.END) && data.gravitonShardEjection) ejectGravitonShards();

        int maxModules = GodforgeMath.maxModuleCount(data);
        boolean canRun = data.internalBattery > 0 && modulePositions.size() <= maxModules;
        forEachModule(module -> {
            GodforgeModuleStats stats = module.godforgeStats();
            if (canRun && GodforgeMath.allowConnection(stats, data)) {
                module.setNetworkOwner(networkOwner);
                module.connect();
                GodforgeMath.updateModule(stats, data);
                if (GodforgeMath.factorChangeDuringRecipe(stats)) module.disconnect();
            } else {
                module.disconnect();
            }
        });
        syncVisuals();
        markDirty();
    }

    private void forEachModule(java.util.function.Consumer<IGodforgeModule> action) {
        if (getLevel() == null) return;
        for (BlockPos pos : modulePositions) {
            if (MetaMachine.getMachine(getLevel(), pos) instanceof IGodforgeModule module) action.accept(module);
        }
    }

    /** Stellar Fuel before the star is lit; graviton shards afterwards once END is unlocked. */
    private void absorbStartupFuelAndShards() {
        boolean lit = data.internalBattery != 0;
        if (lit && !data.isUpgradeActive(GodforgeUpgrade.END)) return;
        ItemStack wanted = lit ? ChemicalHelper.get(TagPrefix.gem, GTNAMaterials.GravitonShard) : stellarFuel();
        for (var storage : itemHandlers(IO.IN)) {
            for (int slot = 0; slot < storage.getSlots(); slot++) {
                ItemStack stack = storage.getStackInSlot(slot);
                if (stack.isEmpty() || !ItemStack.isSameItemSameTags(stack, wanted)) continue;
                int amount = Math.min(stack.getCount(), Integer.MAX_VALUE - data.stellarFuelAmount);
                storage.extractItem(slot, amount, false);
                if (!lit) {
                    data.stellarFuelAmount += amount;
                } else {
                    data.gravitonShardsAvailable += amount;
                    data.gravitonShardsSpent -= amount;
                }
            }
        }
        if (!lit) {
            data.neededStartupFuel = GodforgeMath.startupFuelConsumption(data);
            if (data.stellarFuelAmount >= data.neededStartupFuel) {
                data.stellarFuelAmount -= data.neededStartupFuel;
                data.increaseBattery(data.neededStartupFuel);
                if (!data.rendererDisabled) createRenderer();
            }
        }
    }

    /**
     * GTNH uses Avaritia's Stellar Fuel and falls back to a Neutronium block without Avaritia; GTNA uses the
     * fallback.
     */
    public static ItemStack stellarFuel() {
        return ChemicalHelper.get(TagPrefix.block, GTMaterials.Neutronium);
    }

    private Fluid selectedFuel() {
        return switch (data.selectedFuelType) {
            case 0 -> GTNAMaterials.DimensionallyTranscendentResidue.getFluid();
            case 1 -> GTNAMaterials.RawStarMatter.getFluid();
            default -> GTNAMaterials.MagnetohydrodynamicallyConstrainedStarMatter.getFluid();
        };
    }

    private void drainFuel() {
        GodforgeMath.clampFuelFactor(data);
        int factor = data.fuelConsumptionFactor;
        data.fuelConsumption = (long) Math.max(GodforgeMath.fuelConsumption(data) * 5 *
                (data.batteryCharging ? 2 : 1), 1);
        if (data.fuelConsumption >= Integer.MAX_VALUE) {
            reduceBattery(factor);
            return;
        }
        // Through the recipe handler path, so creative/infinite hatches (GTMThings) behave as in recipes.
        Fluid fuel = selectedFuel();
        var tanks = fluidHandlers(IO.IN);
        List<com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient> want = List.of(
                com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient.of(fuel, (int) data.fuelConsumption));
        List<com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient> left = want;
        for (var tank : tanks) {
            left = tank.handleRecipeInner(IO.IN, null, copyIngredients(left), true);
            if (left == null || left.isEmpty()) break;
        }
        if (left == null || left.isEmpty()) {
            left = want;
            for (var tank : tanks) {
                left = tank.handleRecipeInner(IO.IN, null, copyIngredients(left), false);
                if (left == null || left.isEmpty()) break;
            }
            data.totalFuelConsumed += factor;
            if (data.batteryCharging) data.increaseBattery(factor);
            return;
        }
        reduceBattery(factor);
    }

    private static List<com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient> copyIngredients(
                                                                                                     List<com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient> list) {
        List<com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient> copy = new ArrayList<>();
        for (var ingredient : list) copy.add(ingredient.copy());
        return copy;
    }

    /** Item storages of every part handler with the given direction (any bus type, including addon ones). */
    private List<com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler> itemHandlers(IO io) {
        List<com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler> result = new ArrayList<>();
        for (var part : getParts()) {
            for (var list : part.getRecipeHandlers()) {
                for (var handler : list.getHandlersFlat()) {
                    if (handler instanceof com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler items &&
                            items.handlerIO == io)
                        result.add(items.storage);
                }
            }
        }
        return result;
    }

    private List<com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank> fluidHandlers(IO io) {
        List<com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank> result = new ArrayList<>();
        for (var part : getParts()) {
            for (var list : part.getRecipeHandlers()) {
                for (var handler : list.getHandlersFlat()) {
                    if (handler instanceof com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank tank &&
                            tank.handlerIO == io)
                        result.add(tank);
                }
            }
        }
        return result;
    }

    private void reduceBattery(long amount) {
        if (data.reduceBattery(amount)) {
            forEachModule(IGodforgeModule::disconnect);
            destroyRenderer();
        }
    }

    private void ejectGravitonShards() {
        for (var storage : itemHandlers(IO.OUT)) {
            int available = data.gravitonShardsAvailable;
            ItemStack shard = ChemicalHelper.get(TagPrefix.gem, GTNAMaterials.GravitonShard);
            int ejected = 0;
            for (int slot = 0; slot < storage.getSlots() && ejected < available; slot++) {
                ItemStack batch = shard.copyWithCount(Math.min(shard.getMaxStackSize(), available - ejected));
                ItemStack left = storage.insertItem(slot, batch, false);
                ejected += batch.getCount() - left.getCount();
            }
            data.gravitonShardsAvailable -= ejected;
            data.gravitonShardsSpent += ejected;
            return;
        }
    }

    // ---- Upgrades (called by the GUI) ----

    public boolean unlockUpgrade(GodforgeUpgrade upgrade) {
        boolean changed = data.unlockUpgrade(upgrade);
        if (changed && (upgrade == GodforgeUpgrade.CD || upgrade == GodforgeUpgrade.END)) updateRings();
        if (changed) changed();
        return changed;
    }

    public boolean respecUpgrade(GodforgeUpgrade upgrade) {
        boolean changed = data.respecUpgrade(upgrade);
        if (changed && (upgrade == GodforgeUpgrade.CD || upgrade == GodforgeUpgrade.END)) updateRings();
        if (changed) changed();
        return changed;
    }

    /** Screwdriver toggle of the star animation, as in the original. */
    public void toggleRenderer() {
        if (data.rendererDisabled) {
            data.rendererDisabled = false;
        } else {
            data.rendererDisabled = true;
            destroyRenderer();
        }
        syncVisuals();
        markDirty();
    }

    // ---- GUI ----

    @Override
    public com.lowdragmc.lowdraglib.gui.modular.ModularUI createUI(net.minecraft.world.entity.player.Player player) {
        return com.raishxn.gtna.common.machine.multiblock.godforge.gui.ForgeOfGodsUI.createUI(this, player);
    }

    // ---- GUI actions (server side) ----

    public void payUpgradeCost(GodforgeUpgrade upgrade) {
        List<GodforgeUpgradeStorage.Payment> payments = new ArrayList<>();
        for (int slot = 0; slot < upgradeWindow.getSlots(); slot++) {
            final int index = slot;
            ItemStack stack = upgradeWindow.getStackInSlot(slot);
            if (stack.isEmpty()) continue;
            String id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
            payments.add(new GodforgeUpgradeStorage.Payment() {

                @Override
                public String item() {
                    return id;
                }

                @Override
                public int amount() {
                    return upgradeWindow.getStackInSlot(index).getCount();
                }

                @Override
                public void shrink(int by) {
                    upgradeWindow.extractItem(index, by, false);
                }
            });
        }
        data.upgrades.payCost(upgrade, payments);
        changed();
    }

    public void setFuelType(int type) {
        data.selectedFuelType = Math.max(0, Math.min(2, type));
        GodforgeMath.clampFuelFactor(data);
        changed();
    }

    public void setFuelFactor(int factor) {
        data.fuelConsumptionFactor = Math.max(1, Math.min(factor, GodforgeMath.maxFuelFactor(data)));
        changed();
    }

    public void toggleBatteryCharging() {
        data.batteryCharging = !data.batteryCharging;
        changed();
    }

    /** {@code MAX_BATTERY_CHARGE}: configurable once REC is unlocked. */
    public void setMaxBatteryCharge(long charge) {
        if (!data.isUpgradeActive(GodforgeUpgrade.REC)) return;
        data.maxBatteryCharge = Math.max(1, charge);
        if (data.internalBattery > data.maxBatteryCharge) data.internalBattery = data.maxBatteryCharge;
        changed();
    }

    public void toggleShardEjection() {
        if (!data.isUpgradeActive(GodforgeUpgrade.END)) return;
        data.gravitonShardEjection = !data.gravitonShardEjection;
        changed();
    }

    public void toggleSecretUpgrade() {
        data.secretUpgrade = !data.secretUpgrade;
        changed();
    }

    /** "Refresh module connection status": re-runs the structure check. */
    public void refreshModules() {
        if (getLevel() instanceof ServerLevel && checkPatternWithLock()) onStructureFormed();
        changed();
    }

    public void setRotationSpeed(int speed) {
        data.rotationSpeed = Math.max(0, Math.min(100, speed));
        changed();
    }

    public void setStarSize(int size) {
        data.starSize = Math.max(0, Math.min(40, size));
        changed();
    }

    public void selectStarColor(String name) {
        if (data.starColors.byName(name) == null) return;
        data.selectedStarColor = name;
        changed();
    }

    /** Stores a new custom color or replaces the custom color at {@code index} (from the color editor). */
    public void saveStarColor(String serialized, int index) {
        var color = com.raishxn.gtna.api.machine.feature.godforge.GodforgeStarColor.deserializeString(serialized);
        if (color == null || color.numColors() == 0) return;
        if (index >= 0 && index < data.starColors.size() && !data.starColors.byIndex(index).isPreset()) {
            String old = data.starColors.byIndex(index).name();
            data.starColors.insert(color, index);
            if (old.equals(data.selectedStarColor)) data.selectedStarColor = color.name();
        } else {
            data.starColors.store(color);
        }
        changed();
    }

    public void deleteStarColor(String name) {
        var color = data.starColors.byName(name);
        if (color == null || color.isPreset()) return;
        data.starColors.drop(color);
        if (name.equals(data.selectedStarColor)) {
            data.selectedStarColor = com.raishxn.gtna.api.machine.feature.godforge.GodforgeStarColor.DEFAULT.name();
        }
        changed();
    }

    /** Debug/testing entry for {@code /gtna godforge shards}. */
    public int addGravitonShards(int amount) {
        // Available shards are recomputed every tick as milestone total minus spent, so a grant lowers "spent".
        int granted = Math.max(-data.gravitonShardsAvailable, amount);
        data.gravitonShardsSpent -= granted;
        data.gravitonShardsAvailable += granted;
        changed();
        return data.gravitonShardsAvailable;
    }

    private void changed() {
        syncVisuals();
        markDirty();
    }

    private void syncVisuals() {
        renderActive = data.renderActive && !data.rendererDisabled;
        syncedRingAmount = data.ringAmount;
        syncedData = GodforgeDataNbt.save(data);
        int connected = 0;
        if (getLevel() != null) {
            for (BlockPos pos : modulePositions) {
                if (MetaMachine.getMachine(getLevel(), pos) instanceof IGodforgeModule module &&
                        module.godforgeStats().connected)
                    connected++;
            }
        }
        connectedModules = connected;
        syncedStarSize = data.starSize;
        syncedRotationSpeed = data.rotationSpeed;
        var color = data.starColors.byName(data.selectedStarColor);
        syncedStarColor = (color == null ? com.raishxn.gtna.api.machine.feature.godforge.GodforgeStarColor.DEFAULT :
                color).serialize();
    }

    // ---- Persistence ----

    @Override
    public void saveCustomPersistedData(CompoundTag tag, boolean forDrop) {
        super.saveCustomPersistedData(tag, forDrop);
        tag.put("godforge", GodforgeDataNbt.save(data));
    }

    @Override
    public void loadCustomPersistedData(CompoundTag tag) {
        super.loadCustomPersistedData(tag);
        GodforgeDataNbt.load(data, tag.getCompound("godforge"));
        syncVisuals();
    }
}
