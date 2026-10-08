package com.raishxn.gtna.common.machine.multiblock.noenergy;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockDisplayText;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

import com.raishxn.gtna.api.capability.WirelessEnergyManager;
import com.raishxn.gtna.api.machine.feature.eyeofharmony.EyeOfHarmonyMath;
import com.raishxn.gtna.common.data.GTNAMaterials;
import com.raishxn.gtna.common.data.multiblock.EyeOfHarmonyCatalog;
import com.raishxn.gtna.common.data.multiblock.EyeOfHarmonyPrograms;
import com.raishxn.gtna.utils.datastructure.Int128;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

public class EyeOfHarmonyMachine extends WorkableMultiblockMachine implements IDisplayUIMachine, IFancyUIMachine,
                                 com.gregtechceu.gtceu.api.machine.feature.IMachineLife {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            EyeOfHarmonyMachine.class, WorkableMultiblockMachine.MANAGED_FIELD_HOLDER);
    @DescSynced
    private int compressionTier = -1;
    @DescSynced
    private int accelerationTier = -1;
    @DescSynced
    private int stabilisationTier = -1;

    @Persisted
    @DescSynced
    private int overclockLevel = 0;
    @Persisted
    @DescSynced
    private long hydrogen = 0;
    @Persisted
    @DescSynced
    private long helium = 0;
    @Persisted
    @DescSynced
    private long cycleHydrogen = -1;
    @Persisted
    @DescSynced
    private long cycleHelium = -1;
    @Persisted
    @DescSynced
    private UUID networkOwner;

    @Persisted
    private final NotifiableItemStackHandler planetInventory = new NotifiableItemStackHandler(this, 1, IO.NONE, IO.NONE)
            .setFilter(stack -> EyeOfHarmonyPrograms.forStack(stack) != null);
    @Persisted
    @DescSynced
    private int cycleState;
    @Persisted
    @DescSynced
    private int cycleProgress;
    @Persisted
    @DescSynced
    private int cycleDuration;
    @Persisted
    @DescSynced
    private double cycleChance;
    @Persisted
    @DescSynced
    private double cycleYield;
    @Persisted
    @DescSynced
    private boolean cycleSuccess;
    @Persisted
    @DescSynced
    private String pendingEU = "0";
    @Persisted
    @DescSynced
    private String cycleDebit = "0";
    @Persisted
    @DescSynced
    private String quotedStartupEU = "0";
    @Persisted
    private UUID cycleOwner;
    @Persisted
    private double lastChance;
    @Persisted
    private double pity;
    @Persisted
    private CompoundTag pendingProducts = new CompoundTag();
    @Persisted
    private int deliveryCursor;
    @Persisted
    private int fluidCursor;
    @DescSynced
    private int previewDuration;
    @DescSynced
    private double previewChance;
    @DescSynced
    private double previewYield;
    @DescSynced
    private String previewReturn = "0";
    @DescSynced
    private String blockedReason = "planet";
    private EyeOfHarmonyCatalog.Catalog catalog;
    /** Planet item of the slot, for the client renderer and display. */
    @DescSynced
    private String planetId = "";

    public NotifiableItemStackHandler getPlanetInventory() {
        return planetInventory;
    }

    public int getCycleState() {
        return cycleState;
    }

    public int getCycleProgress() {
        return cycleProgress;
    }

    public int getCycleDuration() {
        return cycleDuration;
    }

    public CompoundTag getPendingProducts() {
        return pendingProducts.copy();
    }

    @Override
    protected RecipeLogic createRecipeLogic(Object... args) {
        return new RecipeLogic(this) {

            @Override
            public void findAndHandleRecipe() {
                // Already-paid legacy cycles may finish; no new Cosmos recipe may start.
            }

            @Override
            public void onRecipeFinish() {
                suspendAfterFinish = true;
                super.onRecipeFinish();
                suspendAfterFinish = false;
                lastRecipe = null;
                setStatus(Status.IDLE);
            }
        };
    }

    public EyeOfHarmonyMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        compressionTier = matchedTier(com.raishxn.gtna.common.block.EyeOfHarmonyFieldBlock.Family.COMPRESSION);
        accelerationTier = matchedTier(com.raishxn.gtna.common.block.EyeOfHarmonyFieldBlock.Family.ACCELERATION);
        stabilisationTier = matchedTier(com.raishxn.gtna.common.block.EyeOfHarmonyFieldBlock.Family.STABILISATION);
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        compressionTier = accelerationTier = stabilisationTier = -1;
    }

    private int matchedTier(com.raishxn.gtna.common.block.EyeOfHarmonyFieldBlock.Family family) {
        Integer tier = getMultiblockState().getMatchContext()
                .get(com.raishxn.gtna.common.data.multiblock.EyeOfHarmonyStructure.tierKey(family));
        return tier == null ? -1 : tier;
    }

    public int getCompressionTier() {
        return compressionTier;
    }

    public int getAccelerationTier() {
        return accelerationTier;
    }

    public int getStabilisationTier() {
        return stabilisationTier;
    }

    @Override
    public void onMachinePlaced(@Nullable LivingEntity player, ItemStack stack) {
        if (player != null && cycleState == 0) {
            this.networkOwner = player.getUUID();
            markDirty();
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            subscribeServerTick(this::tickPlanetary);
        }
    }

    public void tickPlanetary() {
        tickPlanetary(getOffsetTimer() % 20 == 0);
    }

    /** Controlled input-poll boundary also used by deterministic GameTests. */
    private void updatePlanetaryRenderState() {
        if (recipeLogic.isActive() || getLevel() == null || getLevel().isClientSide) return;
        var status = cycleState == 0 ? RecipeLogic.Status.IDLE :
                !isFormed() || !recipeLogic.isWorkingEnabled() ? RecipeLogic.Status.SUSPEND :
                        cycleState == 1 ? RecipeLogic.Status.WORKING : RecipeLogic.Status.WAITING;
        var renderState = getRenderState();
        var property = com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties.RECIPE_LOGIC_STATUS;
        if (renderState.hasProperty(property) && renderState.getValue(property) != status) {
            setRenderState(renderState.setValue(property, status));
        }
    }

    public void tickPlanetary(boolean pollInputs) {
        updatePlanetaryRenderState();
        if (!isFormed() || !recipeLogic.isWorkingEnabled() || recipeLogic.isActive() ||
                !(getLevel() instanceof ServerLevel level))
            return;
        if (networkOwner == null) networkOwner = getOwnerUUID();
        if (cycleState == 1) {
            cycleProgress++;
            if (cycleProgress >= cycleDuration) cycleState = 2;
            markDirty();
            return;
        }
        if (cycleState == 2) {
            deliver(level);
            return;
        }
        if (!pollInputs) return;
        absorbGases();
        overclockLevel = circuit();
        quotedStartupEU = getStartupEnergy().toBigInteger().toString();
        catalog(level);
        var previewCatalog = quoteCatalog(level);
        if (previewCatalog != null && compressionTier >= previewCatalog.program().requiredCompression() &&
                accelerationTier >= 0 && stabilisationTier >= 0) {
            var previewProgram = previewCatalog.program();
            var preview = EyeOfHarmonyMath.plan(previewProgram,
                    new EyeOfHarmonyMath.Fields(compressionTier, accelerationTier, stabilisationTier), overclockLevel,
                    Math.max(hydrogen, previewProgram.hydrogen()), Math.max(helium, previewProgram.helium()),
                    new EyeOfHarmonyMath.History(lastChance, pity));
            previewDuration = preview.durationTicks();
            previewChance = preview.chance();
            previewYield = preview.yield();
            previewReturn = preview.creditEU().toString();
        }
        if (EyeOfHarmonyPrograms.forStack(planetInventory.getStackInSlot(0)) == null) {
            blockedReason = "planet";
            return;
        }
        if (networkOwner == null) {
            blockedReason = "owner";
            return;
        }
        if (compressionTier < 0 || accelerationTier < 0 || stabilisationTier < 0) return;
        var outputs = catalog(level);
        if (outputs == null) {
            blockedReason = "catalog";
            return;
        }
        if (hydrogen < outputs.program().hydrogen() || helium < outputs.program().helium()) {
            blockedReason = "gas";
            return;
        }
        if (compressionTier < outputs.program().requiredCompression()) {
            blockedReason = "compression";
            return;
        }
        var plan = EyeOfHarmonyMath.plan(outputs.program(),
                new EyeOfHarmonyMath.Fields(compressionTier, accelerationTier, stabilisationTier),
                overclockLevel, hydrogen, helium, new EyeOfHarmonyMath.History(lastChance, pity));
        if (!WirelessEnergyManager.consumeDirectEnergy(level, networkOwner, Int128.fromBigInteger(plan.debitEU()),
                net.minecraft.core.GlobalPos.of(level.dimension(), getPos()),
                getBlockState().getBlock().getDescriptionId())) {
            blockedReason = "energy";
            return;
        }
        // Server-thread commit: after the single successful debit, freeze everything once.
        var result = EyeOfHarmonyMath.resolve(plan, level.random.nextInt(10_000));
        cycleOwner = networkOwner;
        cycleDebit = plan.debitEU().toString();
        pendingEU = result.creditEU().toString();
        cycleChance = plan.chance();
        cycleYield = plan.yield();
        cycleSuccess = result.success();
        lastChance = result.history().lastChance();
        pity = result.history().pity();
        cycleDuration = plan.durationTicks();
        cycleProgress = 0;
        deliveryCursor = fluidCursor = 0;
        pendingProducts = result.success() ? outputs.products().copy() : new CompoundTag();
        if (result.success()) {
            for (String type : List.of("items", "fluids")) {
                var list = pendingProducts.getList(type, 10);
                for (int i = 0; i < list.size(); i++) {
                    var entry = list.getCompound(i);
                    entry.putLong("remaining", plan.outputAmount(entry.getLong("remaining")));
                }
            }
        } else if (result.failedSpaceTime() > 0) {
            var fluids = new ListTag();
            fluids.add(EyeOfHarmonyCatalog.fluid(GTNAMaterials.SpaceTime.getFluid(), result.failedSpaceTime()));
            pendingProducts.put("fluids", fluids);
        }
        cycleHydrogen = hydrogen;
        cycleHelium = helium;
        hydrogen = helium = 0;
        cycleState = 1;
        blockedReason = "running";
        markDirty();
    }

    private EyeOfHarmonyCatalog.Catalog catalog(ServerLevel level) {
        var stack = planetInventory.getStackInSlot(0);
        var definition = EyeOfHarmonyPrograms.forStack(stack);
        String id = stack.isEmpty() ? "" : BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        if (!id.equals(planetId)) planetId = id;
        if (definition == null) return null;
        return cachedCatalog(level, definition);
    }

    /** Quotes with an empty slot use the Overworld program, as before planets existed. */
    private EyeOfHarmonyCatalog.Catalog quoteCatalog(ServerLevel level) {
        var definition = EyeOfHarmonyPrograms.forStack(planetInventory.getStackInSlot(0));
        return cachedCatalog(level, definition == null ? EyeOfHarmonyPrograms.overworld() : definition);
    }

    private EyeOfHarmonyCatalog.Catalog cachedCatalog(ServerLevel level, EyeOfHarmonyPrograms.Definition definition) {
        if (catalog == null || !catalog.definition().equals(definition)) {
            try {
                catalog = EyeOfHarmonyCatalog.build(level.getRecipeManager(), definition);
            } catch (IllegalStateException | ArithmeticException error) {
                catalog = null;
                return null;
            }
        }
        return catalog;
    }

    public String getPlanetId() {
        return planetId;
    }

    private void absorbGases() {
        for (var part : getParts()) {
            if (!(part instanceof FluidHatchPartMachine hatch) || hatch.tank.handlerIO != IO.IN) continue;
            for (int tank = 0; tank < hatch.tank.getTanks(); tank++) {
                var stack = hatch.tank.getFluidInTank(tank);
                boolean isHydrogen = stack.getFluid() == GTMaterials.Hydrogen.getFluid();
                boolean isHelium = stack.getFluid() == GTMaterials.Helium.getFluid();
                if (stack.isEmpty() || (!isHydrogen && !isHelium)) continue;
                long stored = isHydrogen ? hydrogen : helium;
                int amount = (int) Math.min(stack.getAmount(), Long.MAX_VALUE - stored);
                if (amount <= 0) continue;
                var drained = hatch.tank.getStorages()[tank].drain(amount, FluidAction.EXECUTE);
                if (isHydrogen) hydrogen += drained.getAmount();
                else helium += drained.getAmount();
                markDirty();
            }
        }
    }

    private int circuit() {
        for (var part : getParts()) {
            if (!(part instanceof ItemBusPartMachine bus) || bus.getInventory().handlerIO != IO.IN) continue;
            // The virtual GTCEu circuit slot is the modern counterpart of an input-bus circuit.
            var virtual = bus.getCircuitInventory().getStackInSlot(0);
            if (IntCircuitBehaviour.isIntegratedCircuit(virtual))
                return Math.min(24, IntCircuitBehaviour.getCircuitConfiguration(virtual));
            for (int slot = 0; slot < bus.getInventory().getSlots(); slot++) {
                var stack = bus.getInventory().getStackInSlot(slot);
                if (IntCircuitBehaviour.isIntegratedCircuit(stack))
                    return Math.min(24, IntCircuitBehaviour.getCircuitConfiguration(stack));
            }
        }
        return 0;
    }

    public Int128 getStartupEnergy() {
        if (cycleState != 0) return Int128.fromBigInteger(new BigInteger(cycleDebit));
        if (getLevel() instanceof ServerLevel level) {
            var outputs = quoteCatalog(level);
            if (outputs != null)
                return Int128.fromBigInteger(EyeOfHarmonyMath.startupDebit(outputs.program(), overclockLevel));
        }
        return Int128.fromBigInteger(new BigInteger(quotedStartupEU));
    }

    @Nullable
    public static ModifierFunction recipeModifier(MetaMachine machine, @NotNull GTRecipe recipe) {
        // Planetary operations own their transaction; searches and previews never spend resources.
        return ModifierFunction.NULL;
    }

    private void deliver(ServerLevel level) {
        BigInteger owed = new BigInteger(pendingEU);
        if (owed.signum() > 0) {
            var acceptedGross = WirelessEnergyManager.addEnergy(level, cycleOwner, Int128.fromBigInteger(owed));
            pendingEU = owed.subtract(acceptedGross.toBigInteger()).toString();
        }
        // Bounded work: at most 64 stack insertions and one fluid batch per tick.
        var items = pendingProducts.getList("items", 10);
        int budget = 64;
        for (var part : getParts()) {
            if (!(part instanceof ItemBusPartMachine bus) || bus.getInventory().handlerIO != IO.OUT) continue;
            for (int visited = 0; visited < items.size() && budget > 0; visited++) {
                deliveryCursor = Math.floorMod(deliveryCursor, items.size());
                var entry = items.getCompound(deliveryCursor);
                deliveryCursor = (deliveryCursor + 1) % items.size();
                long remaining = entry.getLong("remaining");
                for (int slot = 0; slot <
                        (bus instanceof com.gregtechceu.gtceu.integration.ae2.machine.MEOutputBusPartMachine ?
                                1 : bus.getInventory().getSlots()) &&
                        remaining > 0 && budget > 0; slot++) {
                    var stack = ItemStack.of(entry);
                    int batch = (int) Math.min(remaining,
                            bus instanceof com.gregtechceu.gtceu.integration.ae2.machine.MEOutputBusPartMachine ?
                                    Integer.MAX_VALUE : stack.getMaxStackSize());
                    stack.setCount(batch);
                    var leftover = bus.getInventory().insertItemInternal(slot, stack, false);
                    remaining -= batch - leftover.getCount();
                    budget--;
                }
                entry.putLong("remaining", remaining);
            }
        }
        var fluids = pendingProducts.getList("fluids", 10);
        for (int visited = 0; visited < fluids.size(); visited++) {
            fluidCursor = Math.floorMod(fluidCursor, fluids.size());
            var entry = fluids.getCompound(fluidCursor);
            fluidCursor = (fluidCursor + 1) % fluids.size();
            long remaining = entry.getLong("remaining");
            if (remaining <= 0) continue;
            for (var part : getParts()) {
                if (!(part instanceof FluidHatchPartMachine hatch) || hatch.tank.handlerIO != IO.OUT) continue;
                var fluid = FluidStack.loadFluidStackFromNBT(entry);
                fluid.setAmount((int) Math.min(remaining,
                        hatch instanceof com.gregtechceu.gtceu.integration.ae2.machine.MEOutputHatchPartMachine ?
                                Integer.MAX_VALUE : 1_000_000));
                remaining -= hatch.tank.fillInternal(fluid, FluidAction.EXECUTE);
            }
            entry.putLong("remaining", remaining);
            break;
        }
        boolean productsOwed = false;
        for (String type : List.of("items", "fluids")) {
            var list = pendingProducts.getList(type, 10);
            for (int i = 0; i < list.size(); i++) productsOwed |= list.getCompound(i).getLong("remaining") > 0;
        }
        if (new BigInteger(pendingEU).signum() == 0 && !productsOwed) {
            cycleState = 0;
            pendingProducts = new CompoundTag();
            cycleOwner = null;
            blockedReason = "ready";
        } else blockedReason = "outputs";
        markDirty();
    }

    @Override
    public void saveCustomPersistedData(@NotNull CompoundTag tag, boolean forDrop) {
        super.saveCustomPersistedData(tag, forDrop);
        var state = new CompoundTag();
        state.putInt("version", 1);
        state.putInt("cycleState", cycleState);
        state.putInt("cycleProgress", cycleProgress);
        state.putInt("cycleDuration", cycleDuration);
        state.putInt("overclockLevel", overclockLevel);
        state.putInt("deliveryCursor", deliveryCursor);
        state.putInt("fluidCursor", fluidCursor);
        state.putLong("hydrogen", hydrogen);
        state.putLong("helium", helium);
        state.putLong("cycleHydrogen", cycleHydrogen);
        state.putLong("cycleHelium", cycleHelium);
        state.putDouble("lastChance", lastChance);
        state.putDouble("pity", pity);
        state.putDouble("cycleChance", cycleChance);
        state.putDouble("cycleYield", cycleYield);
        state.putBoolean("cycleSuccess", cycleSuccess);
        state.putString("pendingEU", pendingEU);
        state.putString("cycleDebit", cycleDebit);
        if (networkOwner != null) state.putUUID("networkOwner", networkOwner);
        if (cycleOwner != null) state.putUUID("cycleOwner", cycleOwner);
        state.put("products", pendingProducts.copy());
        state.put("planet", planetInventory.getStackInSlot(0).save(new CompoundTag()));
        tag.put("EyeOfHarmonyOperation", state);
    }

    @Override
    public void loadCustomPersistedData(@NotNull CompoundTag tag) {
        super.loadCustomPersistedData(tag);
        if (!tag.contains("EyeOfHarmonyOperation", 10)) return;
        var state = tag.getCompound("EyeOfHarmonyOperation");
        cycleState = state.getInt("cycleState");
        cycleProgress = state.getInt("cycleProgress");
        cycleDuration = state.getInt("cycleDuration");
        overclockLevel = state.getInt("overclockLevel");
        deliveryCursor = state.getInt("deliveryCursor");
        fluidCursor = state.getInt("fluidCursor");
        hydrogen = state.getLong("hydrogen");
        helium = state.getLong("helium");
        cycleHydrogen = state.contains("cycleHydrogen") ? state.getLong("cycleHydrogen") : -1;
        cycleHelium = state.contains("cycleHelium") ? state.getLong("cycleHelium") : -1;
        lastChance = state.getDouble("lastChance");
        pity = state.getDouble("pity");
        cycleChance = state.getDouble("cycleChance");
        cycleYield = state.getDouble("cycleYield");
        cycleSuccess = state.getBoolean("cycleSuccess");
        pendingEU = state.getString("pendingEU");
        cycleDebit = state.getString("cycleDebit");
        networkOwner = state.hasUUID("networkOwner") ? state.getUUID("networkOwner") : null;
        cycleOwner = state.hasUUID("cycleOwner") ? state.getUUID("cycleOwner") : null;
        pendingProducts = state.getCompound("products").copy();
        planetInventory.setStackInSlot(0, ItemStack.of(state.getCompound("planet")));
        catalog = null;
    }

    @Override
    public boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        if (networkOwner == null) {
            networkOwner = player.getUUID();
            markDirty();
        }
        return true;
    }

    @Override
    public InteractionResult onUse(BlockState state, Level world, net.minecraft.core.BlockPos pos, Player player,
                                   InteractionHand hand, BlockHitResult hit) {
        if (player.getItemInHand(hand).is(GTItems.TOOL_DATA_STICK.asItem())) {
            if (cycleState != 0 || recipeLogic.isActive()) {
                if (!world.isClientSide)
                    player.sendSystemMessage(Component.translatable("gtna.eoh.operation.rebind_busy"));
                return InteractionResult.sidedSuccess(world.isClientSide);
            }
            this.networkOwner = player.getUUID();
            markDirty();
            if (!world.isClientSide) {
                player.sendSystemMessage(Component.translatable("gtna.machine.eye_of_harmony.rebound"));
            }
            return InteractionResult.sidedSuccess(world.isClientSide);
        }
        return super.onUse(state, world, pos, player, hand, hit);
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        return new ModularUI(310, 282, this, entityPlayer)
                .widget(new FancyMachineUIWidget(this, 310, 282));
    }

    @Override
    public Widget createUIWidget() {
        var group = new WidgetGroup(0, 0, 302, 199);
        var summary = new WidgetGroup(4, 4, 294, 54);
        summary.setBackground(GuiTextures.DISPLAY);
        summary.addWidget(new ComponentPanelWidget(5, 4, this::addOperationSummary).setMaxWidthLimit(284));
        group.addWidget(summary);
        var screen = new DraggableScrollableWidgetGroup(4, 62, 294, 107)
                .setBackground(GuiTextures.DISPLAY);
        screen.addWidget(new ComponentPanelWidget(5, 4, this::addDisplayText).setMaxWidthLimit(280));
        group.addWidget(screen);
        group.addWidget(new SlotWidget(planetInventory.storage, 0, 4, 176, true, true).setBackground(GuiTextures.SLOT));
        group.addWidget(new LabelWidget(28, 181, "gtna.eoh.operation.planet_slot"));
        group.setBackground(GuiTextures.BACKGROUND_INVERSE);
        return group;
    }

    @Override
    public void addDisplayText(@NotNull List<Component> textList) {
        if (isFormed()) {
            textList.add(Component.translatable("gtna.eoh.structure.tiers", compressionTier + 1,
                    accelerationTier + 1, stabilisationTier + 1));
            String ownerName = networkOwner == null ? "-" : resolvePlayerName(networkOwner);
            BigInteger stored = getLevel() instanceof ServerLevel serverLevel && networkOwner != null ?
                    com.raishxn.gtna.common.data.NexusEnergyNetwork.get(serverLevel).getExactEnergy(networkOwner) :
                    BigInteger.ZERO;
            textList.add(Component.translatable("gtna.machine.eye_of_harmony.owner", ownerName));
            textList.add(energyLine("gtna.machine.eye_of_harmony.network_eu", stored));
            textList.add(energyLine("gtna.machine.eye_of_harmony.startup_eu", getStartupEnergy().toBigInteger()));
            textList.add(Component.translatable("gtna.machine.eye_of_harmony.hydrogen",
                    formatBuckets(hydrogen)));
            textList.add(Component.translatable("gtna.machine.eye_of_harmony.helium",
                    formatBuckets(helium)));
            if (cycleState != 0) {
                textList.add(cycleHydrogen < 0 || cycleHelium < 0 ?
                        Component.translatable("gtna.eoh.operation.consumed_unknown") :
                        Component.translatable("gtna.eoh.operation.consumed_gases",
                                formatBuckets(cycleHydrogen),
                                formatBuckets(cycleHelium)));
            }
            if (cycleState == 0 || cycleHydrogen >= 0 && cycleHelium >= 0) {
                long effectiveHydrogen = cycleState == 0 ? hydrogen : cycleHydrogen;
                long effectiveHelium = cycleState == 0 ? helium : cycleHelium;
                textList.add(Component.translatable("gtna.eoh.operation.gas_excess",
                        Math.round(Math.max(0, (effectiveHydrogen / 1_000_000_000.0 - 1) * 10_000)) / 100.0,
                        Math.round(Math.max(0, (effectiveHelium / 1_000_000_000.0 - 1) * 10_000)) / 100.0));
            }
        }
        textList.add(Component.translatable("gtna.eoh.operation.planet", planetInventory.getStackInSlot(0).isEmpty() ?
                Component.translatable("gtna.eoh.operation.no_planet") :
                planetInventory.getStackInSlot(0).getHoverName()));
        textList.add(Component.translatable("gtna.eoh.operation.circuit", overclockLevel));
        textList.add(Component.translatable("gtna.eoh.operation.gas_required",
                FormattingUtil.formatNumbers(1_000_000_000L)));
        if (cycleState == 0 && previewDuration > 0) {
            textList.add(Component.translatable("gtna.eoh.operation.preview", previewDuration));
            textList.add(Component.translatable("gtna.eoh.operation.chance_yield",
                    (int) (previewChance * 10000) / 100.0, (int) (previewYield * 10000) / 100.0));
            textList.add(energyLine("gtna.eoh.operation.return", new BigInteger(previewReturn)));
            textList.add(energyLine("gtna.eoh.operation.net",
                    new BigInteger(previewReturn).subtract(getStartupEnergy().toBigInteger())));
        }
        if (cycleState != 0) {
            textList.add(energyLine("gtna.eoh.operation.pending_eu", new BigInteger(pendingEU)));
        }
        MultiblockDisplayText.builder(textList, isFormed())
                .setWorkingStatus(recipeLogic.isWorkingEnabled(), cycleState != 0 || recipeLogic.isActive())
                .addWorkingStatusLine()
                .addOutputLines(recipeLogic.getLastRecipe());
    }

    private void addOperationSummary(List<Component> lines) {
        lines.add(Component.translatable("gtna.eoh.operation.state." +
                (!isFormed() ? "structure" : !recipeLogic.isWorkingEnabled() ? "paused" :
                        cycleState == 0 ? blockedReason : cycleState == 1 ? "running" : "outputs"))
                .withStyle(net.minecraft.ChatFormatting.AQUA));
        int duration = cycleState == 0 ? previewDuration : cycleDuration;
        int progress = cycleState == 0 ? 0 : cycleProgress;
        lines.add(Component.translatable("gtna.eoh.operation.summary_progress",
                duration == 0 ? 0 : Math.min(100, progress * 100L / duration),
                timeRemaining(Math.max(0, duration - progress))));
        lines.add(Component.translatable("gtna.eoh.operation.chance_yield",
                Math.round((cycleState == 0 ? previewChance : cycleChance) * 10000) / 100.0,
                Math.round((cycleState == 0 ? previewYield : cycleYield) * 10000) / 100.0));
    }

    private static String timeRemaining(int ticks) {
        long seconds = (ticks + 19L) / 20L;
        return String.format(java.util.Locale.ROOT, "%02d:%02d:%02d", seconds / 3600, seconds / 60 % 60, seconds % 60);
    }

    private static String formatBuckets(long milliBuckets) {
        var format = java.text.NumberFormat.getNumberInstance(java.util.Locale.US);
        format.setMaximumFractionDigits(3);
        return format.format(java.math.BigDecimal.valueOf(milliBuckets, 3));
    }

    private static Component energyLine(String key, BigInteger amount) {
        String[] suffixes = { "", "k", "M", "G", "T", "P", "E", "Z", "Y", "R", "Q" };
        var value = new java.math.BigDecimal(amount);
        int tier = 0;
        while (value.abs().compareTo(java.math.BigDecimal.valueOf(1000)) >= 0 && tier < suffixes.length - 1) {
            value = value.movePointLeft(3);
            tier++;
        }
        String compact = tier == 0 ? amount.toString() :
                value.round(new java.math.MathContext(4)).stripTrailingZeros().toPlainString() + suffixes[tier];
        return Component.translatable(key, compact).withStyle(style -> style.withHoverEvent(
                new net.minecraft.network.chat.HoverEvent(net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT,
                        Component.literal(FormattingUtil.formatNumbers(amount) + " EU"))));
    }

    private String resolvePlayerName(UUID uuid) {
        if (getLevel() instanceof ServerLevel serverLevel) {
            Player player = serverLevel.getServer().getPlayerList().getPlayer(uuid);
            if (player != null) {
                return player.getName().getString();
            }
        }
        return uuid.toString().substring(0, 8) + "...";
    }
}
