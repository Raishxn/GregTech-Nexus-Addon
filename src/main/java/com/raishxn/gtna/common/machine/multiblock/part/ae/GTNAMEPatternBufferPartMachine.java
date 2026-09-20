package com.raishxn.gtna.common.machine.multiblock.part.ae;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfiguratorButton;
import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.ButtonConfigurator;
import com.gregtechceu.gtceu.api.machine.feature.IDataStickInteractable;
import com.gregtechceu.gtceu.api.machine.feature.IDropSaveMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerList;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;
import com.gregtechceu.gtceu.integration.ae2.machine.MEBusPartMachine;
import com.gregtechceu.gtceu.utils.GTMath;
import com.gregtechceu.gtceu.utils.ItemStackHashStrategy;

import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.syncdata.IContentChangeAware;
import com.lowdragmc.lowdraglib.syncdata.ITagSerializable;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;

import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.implementations.blockentities.PatternContainerGroup;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageHelper;
import appeng.crafting.pattern.ProcessingPatternItem;
import appeng.helpers.patternprovider.PatternContainer;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.raishxn.gtna.api.machine.feature.IPatternBufferModeProvider;
import com.raishxn.gtna.api.machine.feature.ModeIdMatcher;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GTNAMEPatternBufferPartMachine extends MEBusPartMachine
                                            implements ICraftingProvider, PatternContainer, IDataStickInteractable,
                                            IDropSaveMachine, IPatternBufferModeProvider {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            GTNAMEPatternBufferPartMachine.class, MEBusPartMachine.MANAGED_FIELD_HOLDER);
    private static final String SLOT_CONFIGS_TAG = "gtnaPatternConfigs";
    private static final String INTERNAL_SLOTS_TAG = "gtnaPatternInternalSlots";
    private static final String PENDING_OUTPUT_TAG = "gtnaPendingNetworkOutput";

    /**
     * Bounds of the AE2 drain pump, mirroring GTLCore's {@code MEPatternOutputMin/Max} defaults
     * (5 and 80 ticks).
     */
    private static final int MIN_DRAIN_TICKS = 5;
    private static final int MAX_DRAIN_TICKS = 80;

    /** Caps the work a single tick can do when a large backlog accumulated. */
    private static final int DRAIN_OPS_PER_TICK = 64;
    private static final int MAX_DRAIN_FAILURES = 5;

    /** Key (and amount, under {@code real}) format used by GTLCore's {@code AEUtils.createListTag}. */
    private static final String PENDING_OUTPUT_AMOUNT_TAG = "real";
    @Getter
    private final int maxPatternCount;

    private final InternalInventory internalPatternInventory = new InternalInventory() {

        @Override
        public int size() {
            return maxPatternCount;
        }

        @Override
        public ItemStack getStackInSlot(int slotIndex) {
            return patternInventory.getStackInSlot(slotIndex);
        }

        @Override
        public void setItemDirect(int slotIndex, ItemStack stack) {
            patternInventory.setStackInSlot(slotIndex, stack);
            patternInventory.onContentsChanged(slotIndex);
            onPatternChange(slotIndex);
        }
    };

    @Getter
    @Persisted
    @DescSynced
    private final CustomItemStackHandler patternInventory;

    @Getter
    @Persisted
    protected final NotifiableItemStackHandler shareInventory;

    @Getter
    @Persisted
    protected final NotifiableFluidTank shareTank;

    @Getter
    protected final InternalSlot[] internalInventory;

    @Getter
    protected final GTNAPatternBufferSlotConfig[] slotConfigs;

    private final BiMap<IPatternDetails, InternalSlot> detailsSlotMap;

    @Getter
    protected final GTNAPatternBufferRecipeHandler internalRecipeHandler;

    /** Fase 3 extraction: recipe search + matching core; the persistent state stays here. */
    private final PatternSlotResolver slotResolver = new PatternSlotResolver(this);

    /**
     * Outputs (and refunded slot contents) that the ME network could not accept at the moment they
     * were produced. The drain ticker retries them until they fit, so a momentarily full network or
     * a missing AE energy buffer never voids items.
     *
     * <p>
     * GTLCore parity: {@code MEExtendedOutputPartMachineBase.buffer}, persisted under the same
     * key/amount NBT shape.
     */
    private final Object2LongOpenHashMap<AEKey> pendingNetworkOutput = new Object2LongOpenHashMap<>();

    /** Fase 3 extraction: recipe-type mode discovery + labels; the synced cache stays here. */
    @Getter
    private final PatternBufferModeRegistry modeRegistry = new PatternBufferModeRegistry(this);

    @Getter
    @DescSynced
    @Persisted
    @Setter
    private String customName = "";

    @DescSynced
    @Persisted
    @Setter
    private boolean hiddenInTerminal = false;

    /**
     * GTLCore {@code keepByProduct} parity (default {@code false}): when disabled, only the
     * primary output of each pattern is considered when matching a recipe, so secondary
     * byproducts do not have to line up. Enabled keeps every output in the comparison.
     */
    @Getter
    @Persisted
    @Setter
    private boolean keepByProduct = false;

    /**
     * GTLCore {@code embeddedCircuitConfig} / {@code skipExistingCircuitPatterns} parity: the
     * circuit written into every pattern by the "embed circuit" action, and whether patterns that
     * already carry one are left alone.
     */
    @DescSynced
    @Persisted
    @Setter
    @Getter
    private int embeddedCircuitConfig = 1;

    @DescSynced
    @Persisted
    @Setter
    @Getter
    private boolean skipExistingCircuitPatterns = true;

    @Override
    public boolean isVisibleInTerminal() {
        return !hiddenInTerminal;
    }

    /** The {@code isOnline} field is protected in {@code MEBusPartMachine}; the UI needs to read it. */
    boolean isOnlineForUi() {
        return isOnline;
    }

    private boolean needPatternSync;
    @Getter
    @Setter
    private int selectedSlot = -1;
    @Getter
    @Setter
    @Persisted
    @DescSynced
    private int currentPage;
    @Getter
    @Setter
    @DescSynced
    private String availableModeIds = "";

    /**
     * Buffer-level mode filter (GTOCore {@code MultiMachineModeFancyConfigurator} parity): when set,
     * this buffer only serves recipes of that type; blank means "every mode the controller offers".
     *
     * <p>
     * Stored as a full recipe-type registry id — that is what the selector offers — persisted so a
     * pinned buffer keeps its filter across break/place, and synced so the UI reads it directly.
     * Unlike the per-slot {@code preferredModeId}, which decides <em>which slot</em> serves a
     * recipe, this decides <em>whether this buffer is in that mode at all</em>.
     */
    @Getter
    @DescSynced
    @Persisted
    private String selectedModeId = "";

    /** Fase 3 extraction: the currently open UI, if any (client-side only, never persisted). */
    @Nullable
    private PatternBufferUI patternBufferUI;

    @Nullable
    protected TickableSubscription updateSubs;

    public GTNAMEPatternBufferPartMachine(IMachineBlockEntity holder, int maxPatternCount, Object... args) {
        super(holder, IO.IN, args);
        this.maxPatternCount = Math.max(1, maxPatternCount);
        this.patternInventory = new CustomItemStackHandler(this.maxPatternCount);
        this.patternInventory.setFilter(stack -> stack.getItem() instanceof ProcessingPatternItem);
        this.internalInventory = new InternalSlot[this.maxPatternCount];
        this.slotConfigs = new GTNAPatternBufferSlotConfig[this.maxPatternCount];
        this.detailsSlotMap = HashBiMap.create(this.maxPatternCount);
        for (int i = 0; i < this.maxPatternCount; i++) {
            this.internalInventory[i] = new InternalSlot();
            this.slotConfigs[i] = new GTNAPatternBufferSlotConfig();
            int slotIndex = i;
            this.slotConfigs[i].setOnContentsChanged(() -> onSlotConfigurationChanged(slotIndex));
        }
        getMainNode().addService(ICraftingProvider.class, this);
        // Hybrid output path: leftovers land in pendingNetworkOutput, so the machine needs a
        // drain pump (GTLCore registers its Ticker the same way).
        getMainNode().addService(IGridTickable.class, new NetworkOutputTicker());
        this.shareInventory = new NotifiableItemStackHandler(this, 9, IO.IN, IO.NONE);
        this.shareTank = new NotifiableFluidTank(this, 9, 8 * FluidType.BUCKET_VOLUME, IO.IN, IO.NONE);
        this.internalRecipeHandler = new GTNAPatternBufferRecipeHandler(this, this.internalInventory, this.slotConfigs);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        modeRegistry.refreshAvailableModesCache();
        verifySelectedMode();
        if (getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().tell(new TickTask(1, this::rebuildPatternMap));
        }
    }

    @Override
    public void addedToController(IMultiController controller) {
        super.addedToController(controller);
        modeRegistry.refreshAvailableModesCache();
        verifySelectedMode();
    }

    @Override
    public void removedFromController(IMultiController controller) {
        super.removedFromController(controller);
        modeRegistry.refreshAvailableModesCache();
        verifySelectedMode();
    }

    /**
     * Pins (or, with a blank id, clears) the recipe type this buffer serves.
     *
     * <p>
     * Mirrors GTOCore's {@code setRecipeType}: the controller(s) are asked to re-search so the new
     * filter takes effect immediately instead of on their next recipe change.
     */
    public void setSelectedModeId(@Nullable String modeId) {
        String normalized = modeId == null ? "" : modeId.trim();
        if (normalized.equals(selectedModeId)) {
            return;
        }
        selectedModeId = normalized;
        markDirty();
        for (IMultiController controller : getControllers()) {
            if (controller instanceof IRecipeLogicMachine recipeMachine) {
                recipeMachine.getRecipeLogic().markLastRecipeDirty();
            }
        }
    }

    /**
     * GTOCore {@code MultiMachineModeFancyConfigurator.verify}: attaching to or detaching from a
     * controller can retire the selected mode, so drop the filter rather than silently serving
     * nothing. Comparison is exact because the selector only ever offers full registry ids.
     */
    void verifySelectedMode() {
        // Server-side concern only: on the client the synced availableModeIds may not have arrived
        // yet, and clearing there would just flicker the UI until the next sync.
        if (isRemote() || selectedModeId.isBlank() ||
                modeRegistry.getCachedAvailableModeIds().contains(selectedModeId)) {
            return;
        }
        selectedModeId = "";
        markDirty();
    }

    @Override
    public List<RecipeHandlerList> getRecipeHandlers() {
        return internalRecipeHandler.getSlotHandlers();
    }

    // ------------------------------------------------------------------
    // Proxy support (GTLCore MEPatternBufferProxyPartMachine parity).
    // A proxy is a part placed inside another multiblock that borrows this
    // buffer's slot handlers, so a distant structure can use the patterns here.
    // ------------------------------------------------------------------

    private final Set<GTNAMEPatternBufferProxyPartMachine> proxies = new LinkedHashSet<>();

    public void addProxy(GTNAMEPatternBufferProxyPartMachine proxy) {
        proxies.add(proxy);
    }

    public void removeProxy(GTNAMEPatternBufferProxyPartMachine proxy) {
        proxies.remove(proxy);
    }

    /** Re-notifies every attached proxy that this buffer's slot handlers may have changed. */
    public void notifyProxySlotRemoved(int slot) {
        for (GTNAMEPatternBufferProxyPartMachine proxy : List.copyOf(proxies)) {
            proxy.onBufferSlotInvalidated(slot);
        }
    }

    public int getProxyCount() {
        return proxies.size();
    }

    /**
     * Lets a Pattern Buffer act as an AE2 output hatch as well as an input bus.
     * Outputs are inserted directly into the connected grid, so a separate ME
     * Output Bus is not required for recipes started from this buffer.
     */
    public List<Ingredient> gtna$handleNetworkItemOutput(GTRecipe recipe, List<Ingredient> left, boolean simulate) {
        if (left == null || left.isEmpty() || getMainNode().getGrid() == null) {
            return left;
        }
        MEStorage storage = getMainNode().getGrid().getStorageService().getInventory();
        for (var iterator = left.listIterator(); iterator.hasNext();) {
            Ingredient ingredient = iterator.next();
            if (ingredient == null || ingredient.isEmpty()) {
                iterator.remove();
                continue;
            }
            ItemStack[] candidates = ingredient.getItems();
            if (candidates.length == 0 || candidates[0].isEmpty()) {
                iterator.remove();
                continue;
            }
            int amount = ingredient instanceof SizedIngredient sized ? sized.getAmount() : candidates[0].getCount();
            AEItemKey key = AEItemKey.of(candidates[0]);
            if (key == null || amount <= 0) {
                continue;
            }
            long inserted = simulate ? storage.insert(key, amount, Actionable.SIMULATE, actionSource) :
                    StorageHelper.poweredInsert(getMainNode().getGrid().getEnergyService(), storage, key, amount,
                            actionSource);
            int remaining = amount - GTMath.saturatedCast(inserted);
            if (remaining <= 0) {
                iterator.remove();
            } else if (simulate) {
                // Matching pass: keep reporting the shortfall so the recipe is not started while
                // the grid cannot accept its output.
                if (ingredient instanceof SizedIngredient sized) {
                    sized.setAmount(remaining);
                } else {
                    candidates[0].setCount(remaining);
                }
            } else {
                // Hybrid path: hand the shortfall to the drain pump instead of returning it to the
                // recipe logic, where onRecipeFinish() discards the failed IO.OUT result.
                bufferPendingNetworkOutput(key, remaining);
                iterator.remove();
            }
        }
        return left.isEmpty() ? null : left;
    }

    public List<FluidIngredient> gtna$handleNetworkFluidOutput(GTRecipe recipe, List<FluidIngredient> left,
                                                               boolean simulate) {
        if (left == null || left.isEmpty() || getMainNode().getGrid() == null) {
            return left;
        }
        MEStorage storage = getMainNode().getGrid().getStorageService().getInventory();
        for (var iterator = left.iterator(); iterator.hasNext();) {
            FluidIngredient ingredient = iterator.next();
            if (ingredient == null || ingredient.isEmpty()) {
                iterator.remove();
                continue;
            }
            FluidStack[] candidates = ingredient.getStacks();
            if (candidates.length == 0 || candidates[0].isEmpty()) {
                iterator.remove();
                continue;
            }
            int amount = candidates[0].getAmount();
            AEFluidKey key = AEFluidKey.of(candidates[0]);
            if (key == null || amount <= 0) {
                continue;
            }
            long inserted = simulate ? storage.insert(key, amount, Actionable.SIMULATE, actionSource) :
                    StorageHelper.poweredInsert(getMainNode().getGrid().getEnergyService(), storage, key, amount,
                            actionSource);
            int remaining = amount - GTMath.saturatedCast(inserted);
            if (remaining <= 0) {
                iterator.remove();
            } else if (simulate) {
                // Matching pass: keep reporting the shortfall so the recipe is not started while
                // the grid cannot accept its output.
                ingredient.setAmount(remaining);
            } else {
                // Hybrid path: hand the shortfall to the drain pump instead of returning it to the
                // recipe logic, where onRecipeFinish() discards the failed IO.OUT result.
                bufferPendingNetworkOutput(key, remaining);
                iterator.remove();
            }
        }
        return left.isEmpty() ? null : left;
    }

    // ------------------------------------------------------------------
    // Deferred network output (GTLCore MEPatternBufferPartMachine.Ticker parity, adapted to the
    // hybrid path: the inline insert is still attempted first, and only the shortfall lands here).
    // ------------------------------------------------------------------

    /** Queues an output the grid could not take and wakes the drain pump if it was idle. */
    private void bufferPendingNetworkOutput(AEKey key, long amount) {
        if (key == null || amount <= 0) {
            return;
        }
        boolean wasEmpty = pendingNetworkOutput.isEmpty();
        pendingNetworkOutput.addTo(key, amount);
        if (wasEmpty) {
            alertPendingOutputDrain();
        }
    }

    /**
     * Asks AE2's tick manager to tick this node again. Without it a dynamic ticker that returned
     * {@link TickRateModulation#SLEEP} would only be revived by an unrelated grid change, so the
     * pending output could sit there indefinitely.
     */
    private void alertPendingOutputDrain() {
        IGrid network = getMainNode().getGrid();
        if (network != null) {
            network.getTickManager().alertDevice(getMainNode().getNode());
        }
    }

    /**
     * Pushes at most {@link #DRAIN_OPS_PER_TICK} buffered keys into the grid and gives up after
     * {@link #MAX_DRAIN_FAILURES} consecutive rejections, so a saturated network cannot burn the
     * whole tick. Mirrors GTLCore's {@code AEUtils.reFunds}.
     *
     * @return {@code true} when at least one key moved
     */
    private boolean drainPendingNetworkOutput() {
        IGrid network = getMainNode().getGrid();
        if (network == null || pendingNetworkOutput.isEmpty()) {
            return false;
        }
        MEStorage storage = network.getStorageService().getInventory();
        var energy = network.getEnergyService();
        boolean didWork = false;
        int operations = 0;
        int consecutiveFailures = 0;
        for (var it = pendingNetworkOutput.object2LongEntrySet()
                .iterator(); it.hasNext() && operations < DRAIN_OPS_PER_TICK;) {
            var entry = it.next();
            long amount = entry.getLongValue();
            if (amount <= 0) {
                it.remove();
                continue;
            }
            long inserted = StorageHelper.poweredInsert(energy, storage, entry.getKey(), amount, actionSource);
            operations++;
            if (inserted > 0) {
                didWork = true;
                consecutiveFailures = 0;
                long remaining = amount - inserted;
                if (remaining <= 0) {
                    it.remove();
                } else {
                    entry.setValue(remaining);
                }
            } else if (++consecutiveFailures >= MAX_DRAIN_FAILURES) {
                break;
            }
        }
        return didWork;
    }

    /**
     * AE2 ticking service that empties {@link #pendingNetworkOutput} with an adaptive rate: it
     * sleeps once nothing is pending (bounded by {@link #MAX_DRAIN_TICKS}), reports
     * {@link TickRateModulation#URGENT} when it made progress and slows down otherwise.
     */
    protected class NetworkOutputTicker implements IGridTickable {

        @Override
        public TickingRequest getTickingRequest(IGridNode node) {
            return new TickingRequest(MIN_DRAIN_TICKS, MAX_DRAIN_TICKS, false, true);
        }

        @Override
        public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
            if (!getMainNode().isActive()) {
                return TickRateModulation.SLEEP;
            }
            if (pendingNetworkOutput.isEmpty()) {
                return ticksSinceLastCall >= MAX_DRAIN_TICKS ?
                        TickRateModulation.SLEEP : TickRateModulation.SLOWER;
            }
            return drainPendingNetworkOutput() ? TickRateModulation.URGENT : TickRateModulation.SLOWER;
        }
    }

    @Override
    public boolean isWorkingEnabled() {
        return true;
    }

    @Override
    public void setWorkingEnabled(boolean ignored) {}

    @Override
    public boolean isDistinct() {
        return true;
    }

    @Override
    public void setDistinct(boolean ignored) {}

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        super.onMainNodeStateChanged(reason);
        updateSubscription();
    }

    protected void updateSubscription() {
        if (getMainNode().isOnline()) {
            updateSubs = subscribeServerTick(updateSubs, this::update);
        } else if (updateSubs != null) {
            updateSubs.unsubscribe();
            updateSubs = null;
        }
    }

    protected void update() {
        if (needPatternSync) {
            ICraftingProvider.requestUpdate(getMainNode());
            needPatternSync = false;
        }
    }

    @Override
    public void saveCustomPersistedData(@NotNull CompoundTag tag, boolean forDrop) {
        super.saveCustomPersistedData(tag, forDrop);
        ListTag slotConfigTag = new ListTag();
        ListTag internalSlotTag = new ListTag();
        for (int i = 0; i < maxPatternCount; i++) {
            CompoundTag configTag = slotConfigs[i].serializeNBT();
            configTag.putInt("slot", i);
            slotConfigTag.add(configTag);

            CompoundTag internalTag = internalInventory[i].serializeNBT();
            internalTag.putInt("slot", i);
            internalSlotTag.add(internalTag);
        }
        tag.put(SLOT_CONFIGS_TAG, slotConfigTag);
        tag.put(INTERNAL_SLOTS_TAG, internalSlotTag);
        if (!pendingNetworkOutput.isEmpty()) {
            ListTag pendingTag = new ListTag();
            for (var entry : pendingNetworkOutput.object2LongEntrySet()) {
                if (entry.getLongValue() <= 0) {
                    continue;
                }
                CompoundTag entryTag = entry.getKey().toTagGeneric();
                entryTag.putLong(PENDING_OUTPUT_AMOUNT_TAG, entry.getLongValue());
                pendingTag.add(entryTag);
            }
            if (!pendingTag.isEmpty()) {
                tag.put(PENDING_OUTPUT_TAG, pendingTag);
            }
        }
    }

    @Override
    public void loadCustomPersistedData(@NotNull CompoundTag tag) {
        super.loadCustomPersistedData(tag);
        for (GTNAPatternBufferSlotConfig slotConfig : slotConfigs) {
            slotConfig.deserializeNBT(new CompoundTag());
        }
        for (InternalSlot internalSlot : internalInventory) {
            internalSlot.deserializeNBT(new CompoundTag());
        }
        ListTag slotConfigTag = tag.getList(SLOT_CONFIGS_TAG, Tag.TAG_COMPOUND);
        for (Tag entry : slotConfigTag) {
            if (entry instanceof CompoundTag ct) {
                int slot = ct.getInt("slot");
                if (slot >= 0 && slot < slotConfigs.length) {
                    slotConfigs[slot].deserializeNBT(ct);
                }
            }
        }
        ListTag internalSlotTag = tag.getList(INTERNAL_SLOTS_TAG, Tag.TAG_COMPOUND);
        for (Tag entry : internalSlotTag) {
            if (entry instanceof CompoundTag ct) {
                int slot = ct.getInt("slot");
                if (slot >= 0 && slot < internalInventory.length) {
                    internalInventory[slot].deserializeNBT(ct);
                }
            }
        }
        rebuildPatternMap();
        pendingNetworkOutput.clear();
        ListTag pendingTag = tag.getList(PENDING_OUTPUT_TAG, Tag.TAG_COMPOUND);
        for (Tag entry : pendingTag) {
            if (entry instanceof CompoundTag ct) {
                AEKey key = AEKey.fromTagGeneric(ct);
                long amount = ct.getLong(PENDING_OUTPUT_AMOUNT_TAG);
                if (key != null && amount > 0) {
                    pendingNetworkOutput.addTo(key, amount);
                }
            }
        }
        if (!pendingNetworkOutput.isEmpty()) {
            alertPendingOutputDrain();
        }
    }

    @Override
    public void saveToItem(CompoundTag tag) {
        IDropSaveMachine.super.saveToItem(tag);
        saveCustomPersistedData(tag, true);
    }

    @Override
    public void loadFromItem(CompoundTag tag) {
        IDropSaveMachine.super.loadFromItem(tag);
        loadCustomPersistedData(tag);
    }

    public GTNAPatternBufferSlotConfig getSlotConfig(int slot) {
        return slotConfigs[slot];
    }

    public void invalidateSlotCache(int slot) {
        if (slot >= 0 && slot < slotConfigs.length) {
            slotConfigs[slot].clearRecipeCacheSilently();
            slotResolver.clearPatternRecipeMetadata(slot);
            notifyProxySlotRemoved(slot);
        }
    }

    /**
     * Keeps a pattern-buffer slot bound to the recipe type selected for that slot.
     * The controller's active recipe type is global, so it cannot be used as the
     * routing state when several GTNA recipe threads run at once.
     */
    public boolean gtna$slotAcceptsRecipe(int slot, GTRecipe recipe) {
        if (slot < 0 || slot >= slotConfigs.length || recipe == null) {
            return false;
        }
        // Buffer-level filter first (GTOCore MultiMachineModeFancyConfigurator parity): a buffer
        // pinned to one mode serves only that type, no matter what its slots are configured for.
        if (!selectedModeId.isBlank() && !ModeIdMatcher.matches(selectedModeId, recipe.getType())) {
            return false;
        }
        GTNAPatternBufferSlotConfig config = slotConfigs[slot];
        if (!config.getPreferredModeId().isBlank()) {
            return PatternSlotResolver.matchesPreferredMode(config, recipe);
        }
        // Auto is deliberately not constrained by the previous recipe. Processing
        // patterns already identify their machine recipe type, so retaining the
        // derived type here made a slot permanently reject a different valid mode
        // after its first craft.
        return true;
    }

    @Override
    public @Nullable String gtna$getPreferredModeForRecipe(GTRecipe recipe) {
        PatternSlotResolver.SlotMatch match = slotResolver.findMatchingSlot(recipe);
        if (match == null) {
            return null;
        }
        GTNAPatternBufferSlotConfig config = slotConfigs[match.slot()];
        if (!config.getPreferredModeId().isBlank()) {
            return config.getPreferredModeId();
        }
        return slotResolver.resolveDerivedMode(recipe);
    }

    @Override
    public void gtna$onRecipeStarted(GTRecipe recipe) {
        PatternSlotResolver.SlotMatch match = slotResolver.findMatchingSlot(recipe);
        if (match == null) {
            return;
        }
        slotResolver.cacheResolvedRecipe(match.slot(), recipe);
        if (match.slot() == selectedSlot) {
            refreshUiPreview();
        }
        markDirty();
    }

    private void rebuildPatternMap() {
        detailsSlotMap.clear();
        for (int i = 0; i < patternInventory.getSlots(); i++) {
            ItemStack pattern = patternInventory.getStackInSlot(i);
            IPatternDetails details = PatternDetailsHelper.decodePattern(pattern, getLevel());
            if (details != null) {
                detailsSlotMap.forcePut(details, internalInventory[i]);
            }
            slotResolver.loadPatternRecipeMetadata(i, pattern);
        }
        needPatternSync = true;
    }

    private void onSlotConfigurationChanged(int slot) {
        invalidateSlotCache(slot);
        slotResolver.resolveAndCacheSlotRecipe(slot);
        needPatternSync = true;
        if (slot == selectedSlot) {
            refreshUiPreview();
        }
        markDirty();
    }

    void onPatternChange(int index) {
        if (isRemote()) return;
        InternalSlot internalSlot = internalInventory[index];
        ItemStack newPattern = patternInventory.getStackInSlot(index);
        IPatternDetails newPatternDetails = PatternDetailsHelper.decodePattern(newPattern, getLevel());
        IPatternDetails oldPatternDetails = detailsSlotMap.inverse().get(internalSlot);
        if (oldPatternDetails != null && !oldPatternDetails.equals(newPatternDetails)) {
            internalSlot.refund();
        }
        if (newPatternDetails == null) {
            detailsSlotMap.inverse().remove(internalSlot);
        } else {
            detailsSlotMap.forcePut(newPatternDetails, internalSlot);
        }
        invalidateSlotCache(index);
        slotResolver.loadPatternRecipeMetadata(index, newPattern);
        slotResolver.resolveAndCacheSlotRecipe(index);
        needPatternSync = true;
    }

    private void refundAll(com.lowdragmc.lowdraglib.gui.util.ClickData clickData) {
        if (!clickData.isRemote) {
            for (InternalSlot internalSlot : internalInventory) {
                internalSlot.refund();
            }
        }
    }

    @Override
    public void attachSideTabs(TabsWidget sideTabs) {
        super.attachSideTabs(sideTabs);
        // Buffer-level mode selector (GTOCore MultiMachineModeFancyConfigurator parity). The
        // controller's own "Machine Mode" tab mirrors what is running; this one filters which recipe
        // types this buffer is allowed to serve in the first place.
        sideTabs.attachSubTab(new PatternBufferModeConfigurator(this));
    }

    @Override
    public void attachConfigurators(ConfiguratorPanel configuratorPanel) {
        super.attachConfigurators(configuratorPanel);
        configuratorPanel.attachConfigurators(new ButtonConfigurator(
                new GuiTextureGroup(GuiTextures.BUTTON, GuiTextures.REFUND_OVERLAY), this::refundAll)
                .setTooltips(List.of(Component.translatable("gui.gtceu.refund_all.desc"))));
        // GTLCore parity: hide/show this buffer in the ME Pattern Access Terminal.
        // No custom icons needed — labels come from the lang keys.
        configuratorPanel.attachConfigurators(new IFancyConfiguratorButton.Toggle(
                GuiTextures.BUTTON, GuiTextures.BUTTON,
                () -> hiddenInTerminal,
                (clickData, pressed) -> setHiddenInTerminal(pressed))
                .setTooltipsSupplier(pressed -> List.of(
                        Component.translatable("gtna.machine.pattern_buffer.terminal_visibility")
                                .append(Component.translatable(pressed ? "gtna.machine.pattern_buffer.terminal_hidden" :
                                        "gtna.machine.pattern_buffer.terminal_visible")))));
        // GTLCore keepByProduct toggle: when OFF only the primary output is matched.
        configuratorPanel.attachConfigurators(new IFancyConfiguratorButton.Toggle(
                GuiTextures.BUTTON, GuiTextures.BUTTON,
                () -> keepByProduct,
                (clickData, pressed) -> {
                    setKeepByProduct(pressed);
                    markDirty();
                })
                .setTooltipsSupplier(pressed -> List.of(
                        Component.translatable("gtna.machine.pattern_buffer.keep_byproduct")
                                .append(Component.translatable(pressed ? "gtna.machine.pattern_buffer.toggle_yes" :
                                        "gtna.machine.pattern_buffer.toggle_no")))));
    }

    @Override
    public Widget createUIWidget() {
        patternBufferUI = new PatternBufferUI(this);
        return patternBufferUI.createUIWidget();
    }

    /** Refreshes the open UI preview, if any. */
    void refreshUiPreview() {
        if (patternBufferUI != null) {
            patternBufferUI.refreshSelectedConfigPreview();
        }
    }

    // ------------------------------------------------------------------
    // Embedded circuit actions (GTLCore embedCircuitToPatterns /
    // removeAllPatternCircuits parity). Both only touch encoded patterns;
    // slot configs are left as they are.
    // ------------------------------------------------------------------

    /** Writes {@link #embeddedCircuitConfig} into every pattern that does not already have one. */
    void embedCircuitInAllPatterns() {
        int circuit = Math.max(1, Math.min(IntCircuitBehaviour.CIRCUIT_MAX, embeddedCircuitConfig));
        int changed = 0;
        for (int i = 0; i < patternInventory.getSlots(); i++) {
            ItemStack stack = patternInventory.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }
            // GTLCore's skipExistingCircuitPatterns is the inverse of replaceExisting.
            ItemStack updated = GTNAPatternCircuitHelper.withCircuit(stack, circuit,
                    !skipExistingCircuitPatterns, getLevel());
            if (!updated.isEmpty() && !ItemStack.matches(stack, updated)) {
                internalPatternInventory.setItemDirect(i, updated);
                changed++;
            }
        }
        if (changed > 0) {
            rebuildPatternMap();
        }
    }

    /** Strips the embedded circuit from every pattern. */
    void removeAllPatternCircuits() {
        int changed = 0;
        for (int i = 0; i < patternInventory.getSlots(); i++) {
            ItemStack stack = patternInventory.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }
            ItemStack updated = GTNAPatternCircuitHelper.withoutCircuit(stack, getLevel());
            if (!updated.isEmpty() && !ItemStack.matches(stack, updated)) {
                internalPatternInventory.setItemDirect(i, updated);
                changed++;
            }
        }
        if (changed > 0) {
            rebuildPatternMap();
        }
    }

    void toggleSelectedCacheRecipe() {
        GTNAPatternBufferSlotConfig config = getSelectedConfig();
        if (config == null) {
            return;
        }
        config.setCacheRecipe(!config.isCacheRecipe());
        if (selectedSlot >= 0) {
            invalidateSlotCache(selectedSlot);
            slotResolver.resolveAndCacheSlotRecipe(selectedSlot);
            refreshUiPreview();
            markDirty();
        }
    }

    @Nullable
    GTNAPatternBufferSlotConfig getSelectedConfig() {
        return selectedSlot >= 0 && selectedSlot < slotConfigs.length ? slotConfigs[selectedSlot] : null;
    }

    void clearSelectedRecipeCache() {
        GTNAPatternBufferSlotConfig config = getSelectedConfig();
        if (config != null) {
            config.clearRecipeCache();
            slotResolver.clearPatternRecipeMetadata(selectedSlot);
            if (selectedSlot >= 0) {
                needPatternSync = true;
                refreshUiPreview();
                markDirty();
            }
        }
    }

    /** Clears runtime lookup caches without changing any encoded pattern data. */
    void clearMachineRecipeCaches() {
        for (GTNAPatternBufferSlotConfig config : slotConfigs) {
            config.clearRecipeCacheSilently();
        }
        needPatternSync = true;
        refreshUiPreview();
        markDirty();
    }

    List<Ingredient> consumeCircuitInventory(List<Ingredient> left) {
        if (left == null || left.isEmpty() || !isHasCircuitSlot()) {
            return left;
        }
        ItemStack circuitStack = circuitInventory.storage.getStackInSlot(0);
        if (circuitStack.isEmpty()) {
            return left;
        }
        return PatternSlotResolver.consumeVirtualItemList(List.of(circuitStack), left);
    }

    /**
     * Same indirection as {@link SelectedConfigItemTransfer} but bound to the per-slot catalyst
     * item inventory (GTLCore's catalyst UI edits {@code catalystItems} directly; here we route
     * through the selected slot config so one row serves whichever slot is open).
     */
    @Override
    public List<IPatternDetails> getAvailablePatterns() {
        return detailsSlotMap.keySet().stream().filter(Objects::nonNull).toList();
    }

    @Override
    public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder) {
        if (!isFormed() || !getMainNode().isActive() || !detailsSlotMap.containsKey(patternDetails) ||
                !checkInput(inputHolder)) {
            return false;
        }
        InternalSlot slot = detailsSlotMap.get(patternDetails);
        if (slot != null) {
            slot.pushPattern(patternDetails, inputHolder);
            int logicalSlot = getInternalSlotIndex(slot);
            if (logicalSlot >= 0) {
                slotResolver.resolveAndCacheSlotRecipe(logicalSlot);
            }
            return true;
        }
        return false;
    }

    private int getInternalSlotIndex(InternalSlot target) {
        for (int i = 0; i < internalInventory.length; i++) {
            if (internalInventory[i] == target) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean isBusy() {
        return false;
    }

    private boolean checkInput(KeyCounter[] inputHolder) {
        for (KeyCounter input : inputHolder) {
            boolean illegal = input.keySet().stream()
                    .map(AEKey::getType)
                    .map(AEKeyType::getId)
                    .anyMatch(id -> !id.equals(AEKeyType.items().getId()) && !id.equals(AEKeyType.fluids().getId()));
            if (illegal) return false;
        }
        return true;
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public @Nullable IGrid getGrid() {
        return getMainNode().getGrid();
    }

    @Override
    public InternalInventory getTerminalPatternInventory() {
        return internalPatternInventory;
    }

    @Override
    public PatternContainerGroup getTerminalGroup() {
        if (isFormed()) {
            IMultiController controller = getControllers().first();
            MultiblockMachineDefinition controllerDefinition = controller.self().getDefinition();
            if (!customName.isEmpty()) {
                return new PatternContainerGroup(
                        AEItemKey.of(controllerDefinition.asStack()),
                        Component.literal(customName),
                        Collections.emptyList());
            }
            ItemStack circuitStack = isHasCircuitSlot() ? circuitInventory.storage.getStackInSlot(0) : ItemStack.EMPTY;
            int circuitConfiguration = circuitStack.isEmpty() ? -1 :
                    IntCircuitBehaviour.getCircuitConfiguration(circuitStack);
            Component groupName = circuitConfiguration != -1 ?
                    Component.translatable(controllerDefinition.getDescriptionId())
                            .append(" - " + circuitConfiguration) :
                    Component.translatable(controllerDefinition.getDescriptionId());
            return new PatternContainerGroup(
                    AEItemKey.of(controllerDefinition.asStack()),
                    groupName,
                    Collections.emptyList());
        }
        if (!customName.isEmpty()) {
            return new PatternContainerGroup(
                    AEItemKey.of(getDefinition().asStack()),
                    Component.literal(customName),
                    Collections.emptyList());
        }
        return new PatternContainerGroup(
                AEItemKey.of(getDefinition().asStack()),
                getDefinition().getItem().getDescription(),
                Collections.emptyList());
    }

    @Override
    public void onMachineRemoved() {
        clearInventory(patternInventory);
        clearInventory(shareInventory);
    }

    @Override
    public InteractionResult onDataStickShiftUse(Player player, ItemStack dataStick) {
        dataStick.getOrCreateTag().putIntArray("pos", new int[] { getPos().getX(), getPos().getY(), getPos().getZ() });
        return InteractionResult.SUCCESS;
    }

    // ------------------------------------------------------------------
    // Pattern-buffer copy/paste API (GTLCore parity, machine side).
    // Serializes ONLY the portable config: patterns themselves, the per-slot
    // configs (specialization, circuit, preferred mode) and the custom name.
    // Runtime caches, internal slot fluids/items and controller bindings are
    // rebuilt from the patterns on the target machine, never copied.
    // Format version is stored so future changes can migrate instead of failing.
    // ------------------------------------------------------------------

    /**
     * Strips every encoded pattern from this buffer (GTLCore "cut" semantics). Slot configs are
     * cleared as well so the snapshot/apply pair is symmetric.
     */
    public void cutPatternsFromBuffer() {
        for (int i = 0; i < patternInventory.getSlots(); i++) {
            if (!patternInventory.getStackInSlot(i).isEmpty()) {
                patternInventory.setStackInSlot(i, ItemStack.EMPTY);
                onPatternChange(i);
            }
        }
        for (GTNAPatternBufferSlotConfig config : slotConfigs) {
            config.clearSpecialization();
        }
        rebuildPatternMap();
        needPatternSync = true;
        markDirty();
    }

    private static final String COPY_TAG_ROOT = "gtnaBufferCopy";
    private static final String COPY_TAG_VERSION = "version";
    private static final int COPY_VERSION = 1;

    /**
     * Serializes a portable snapshot of this buffer into {@code out} without touching
     * the machine's live state. The pattern items are copied; nothing is moved.
     */
    public void copyBufferToTag(CompoundTag out) {
        CompoundTag root = new CompoundTag();
        root.putInt(COPY_TAG_VERSION, COPY_VERSION);
        root.putString("name", customName);
        root.putInt("count", maxPatternCount);
        ListTag patterns = new ListTag();
        for (int i = 0; i < maxPatternCount; i++) {
            ItemStack pattern = patternInventory.getStackInSlot(i);
            if (pattern.isEmpty()) continue;
            CompoundTag entry = new CompoundTag();
            entry.putInt("slot", i);
            entry.put("pattern", pattern.serializeNBT());
            entry.put("config", slotConfigs[i].serializeNBT());
            patterns.add(entry);
        }
        root.put("patterns", patterns);
        out.put(COPY_TAG_ROOT, root);
    }

    /**
     * Applies a snapshot produced by {@link #copyBufferToTag} to this buffer. Patterns are
     * written only into empty slots; occupied slots are skipped so no encoded pattern is ever
     * silently overwritten.
     *
     * @return number of patterns actually pasted.
     */
    public int pasteBufferFromTag(CompoundTag in) {
        if (!in.contains(COPY_TAG_ROOT, Tag.TAG_COMPOUND)) return 0;
        CompoundTag root = in.getCompound(COPY_TAG_ROOT);
        if (root.getInt(COPY_TAG_VERSION) != COPY_VERSION) return 0;
        int pasted = 0;
        ListTag patterns = root.getList("patterns", Tag.TAG_COMPOUND);
        for (Tag tag : patterns) {
            if (!(tag instanceof CompoundTag entry)) continue;
            int slot = entry.getInt("slot");
            if (slot < 0 || slot >= maxPatternCount) continue;
            if (!patternInventory.getStackInSlot(slot).isEmpty()) continue;
            int target = slot;
            if (!internalPatternInventory.getStackInSlot(target).isEmpty()) {
                target = -1;
                for (int i = 0; i < maxPatternCount; i++) {
                    if (patternInventory.getStackInSlot(i).isEmpty()) {
                        target = i;
                        break;
                    }
                }
                if (target < 0) break;
            }
            ItemStack pattern = ItemStack.of(entry.getCompound("pattern"));
            if (pattern.isEmpty()) continue;
            internalPatternInventory.setItemDirect(target, pattern);
            slotConfigs[target].deserializeNBT(entry.getCompound("config"));
            pasted++;
        }
        return pasted;
    }

    public record BufferData(Object2LongMap<ItemStack> items, Object2LongMap<FluidStack> fluids) {}

    public BufferData mergeInternalSlots() {
        var items = new Object2LongOpenCustomHashMap<>(ItemStackHashStrategy.comparingAllButCount());
        var fluids = new Object2LongOpenHashMap<FluidStack>();
        for (InternalSlot slot : internalInventory) {
            slot.itemInventory.object2LongEntrySet().fastForEach(e -> items.addTo(e.getKey(), e.getLongValue()));
            slot.fluidInventory.object2LongEntrySet().fastForEach(e -> fluids.addTo(e.getKey(), e.getLongValue()));
        }
        return new BufferData(items, fluids);
    }

    public class InternalSlot implements ITagSerializable<CompoundTag>, IContentChangeAware {

        @Getter
        @Setter
        private Runnable onContentsChanged = () -> {};

        private final Object2LongOpenCustomHashMap<ItemStack> itemInventory = new Object2LongOpenCustomHashMap<>(
                ItemStackHashStrategy.comparingAllButCount());
        private final Object2LongOpenHashMap<FluidStack> fluidInventory = new Object2LongOpenHashMap<>();
        private List<ItemStack> itemStacks;
        private List<FluidStack> fluidStacks;

        public boolean isItemEmpty() {
            return itemInventory.isEmpty();
        }

        public boolean isFluidEmpty() {
            return fluidInventory.isEmpty();
        }

        public void onContentsChanged() {
            itemStacks = null;
            fluidStacks = null;
            onContentsChanged.run();
        }

        private void add(AEKey what, long amount) {
            if (amount <= 0L) return;
            if (what instanceof AEItemKey itemKey) {
                itemInventory.addTo(itemKey.toStack(), amount);
            } else if (what instanceof AEFluidKey fluidKey) {
                fluidInventory.addTo(fluidKey.toStack(1), amount);
            }
        }

        public List<ItemStack> getItems() {
            if (itemStacks == null) {
                itemStacks = new ArrayList<>();
                itemInventory.object2LongEntrySet().stream()
                        .map(e -> GTMath.splitStacks(e.getKey(), e.getLongValue()))
                        .forEach(itemStacks::addAll);
            }
            return itemStacks;
        }

        public List<FluidStack> getFluids() {
            if (fluidStacks == null) {
                fluidStacks = new ArrayList<>();
                fluidInventory.object2LongEntrySet().stream()
                        .map(e -> GTMath.splitFluidStacks(e.getKey(), e.getLongValue()))
                        .forEach(fluidStacks::addAll);
            }
            return fluidStacks;
        }

        public void refund() {
            IGrid network = getMainNode().getGrid();
            if (network == null) return;
            MEStorage networkInv = network.getStorageService().getInventory();
            var energy = network.getEnergyService();
            for (var it = itemInventory.object2LongEntrySet().iterator(); it.hasNext();) {
                var entry = it.next();
                ItemStack stack = entry.getKey();
                long count = entry.getLongValue();
                if (stack.isEmpty() || count == 0) {
                    it.remove();
                    continue;
                }
                var key = AEItemKey.of(stack);
                if (key == null) continue;
                long inserted = StorageHelper.poweredInsert(energy, networkInv, key, count, actionSource);
                count -= inserted;
                if (count > 0) {
                    // Whatever the grid could not take goes to the drain pump, leaving the slot
                    // clean; keeping it here would strand it until the next pattern change.
                    bufferPendingNetworkOutput(key, count);
                }
                it.remove();
            }
            for (var it = fluidInventory.object2LongEntrySet().iterator(); it.hasNext();) {
                var entry = it.next();
                FluidStack stack = entry.getKey();
                long amount = entry.getLongValue();
                if (stack.isEmpty() || amount == 0) {
                    it.remove();
                    continue;
                }
                var key = AEFluidKey.of(stack);
                if (key == null) continue;
                long inserted = StorageHelper.poweredInsert(energy, networkInv, key, amount, actionSource);
                amount -= inserted;
                if (amount > 0) {
                    // See the item loop above.
                    bufferPendingNetworkOutput(key, amount);
                }
                it.remove();
            }
            onContentsChanged();
        }

        public void pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder) {
            patternDetails.pushInputsToExternalInventory(inputHolder, this::add);
            onContentsChanged();
        }

        public @Nullable List<Ingredient> handleItemInternal(List<Ingredient> left, boolean simulate) {
            boolean changed = false;
            for (var it = left.listIterator(); it.hasNext();) {
                Ingredient ingredient = it.next();
                if (ingredient.isEmpty()) {
                    it.remove();
                    continue;
                }
                ItemStack[] items = ingredient.getItems();
                if (items.length == 0 || items[0].isEmpty()) {
                    it.remove();
                    continue;
                }
                int amount = items[0].getCount();
                for (var it2 = itemInventory.object2LongEntrySet().iterator(); it2.hasNext();) {
                    var entry = it2.next();
                    ItemStack stack = entry.getKey();
                    long count = entry.getLongValue();
                    if (stack.isEmpty() || count == 0) {
                        it2.remove();
                        continue;
                    }
                    if (!ingredient.test(stack)) continue;
                    int extracted = Math.min(GTMath.saturatedCast(count), amount);
                    if (!simulate && extracted > 0) {
                        changed = true;
                        count -= extracted;
                        if (count == 0) {
                            it2.remove();
                        } else {
                            entry.setValue(count);
                        }
                    }
                    amount -= extracted;
                    if (amount <= 0) {
                        it.remove();
                        break;
                    }
                }
                if (amount > 0) {
                    if (ingredient instanceof SizedIngredient sizedIngredient) {
                        sizedIngredient.setAmount(amount);
                    } else {
                        items[0].setCount(amount);
                    }
                }
            }
            if (changed) onContentsChanged();
            return left.isEmpty() ? null : left;
        }

        public @Nullable List<FluidIngredient> handleFluidInternal(List<FluidIngredient> left, boolean simulate) {
            boolean changed = false;
            for (var it = left.listIterator(); it.hasNext();) {
                FluidIngredient ingredient = it.next();
                if (ingredient.isEmpty()) {
                    it.remove();
                    continue;
                }
                FluidStack[] fluids = ingredient.getStacks();
                if (fluids.length == 0 || fluids[0].isEmpty()) {
                    it.remove();
                    continue;
                }
                int amount = fluids[0].getAmount();
                for (var it2 = fluidInventory.object2LongEntrySet().iterator(); it2.hasNext();) {
                    var entry = it2.next();
                    FluidStack stack = entry.getKey();
                    long count = entry.getLongValue();
                    if (stack.isEmpty() || count == 0) {
                        it2.remove();
                        continue;
                    }
                    if (!ingredient.test(stack)) continue;
                    int extracted = Math.min(GTMath.saturatedCast(count), amount);
                    if (!simulate && extracted > 0) {
                        changed = true;
                        count -= extracted;
                        if (count == 0) {
                            it2.remove();
                        } else {
                            entry.setValue(count);
                        }
                    }
                    amount -= extracted;
                    if (amount <= 0) {
                        it.remove();
                        break;
                    }
                }
                if (amount > 0) {
                    ingredient.setAmount(amount);
                }
            }
            if (changed) onContentsChanged();
            return left.isEmpty() ? null : left;
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            ListTag itemsTag = new ListTag();
            for (var entry : itemInventory.object2LongEntrySet()) {
                CompoundTag ct = entry.getKey().serializeNBT();
                ct.putLong("real", entry.getLongValue());
                itemsTag.add(ct);
            }
            if (!itemsTag.isEmpty()) {
                tag.put("inventory", itemsTag);
            }
            ListTag fluidsTag = new ListTag();
            for (var entry : fluidInventory.object2LongEntrySet()) {
                CompoundTag ct = entry.getKey().writeToNBT(new CompoundTag());
                ct.putLong("real", entry.getLongValue());
                fluidsTag.add(ct);
            }
            if (!fluidsTag.isEmpty()) {
                tag.put("fluidInventory", fluidsTag);
            }
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            itemInventory.clear();
            fluidInventory.clear();
            ListTag items = tag.getList("inventory", Tag.TAG_COMPOUND);
            for (Tag t : items) {
                if (!(t instanceof CompoundTag ct)) continue;
                ItemStack stack = ItemStack.of(ct);
                long count = ct.getLong("real");
                if (!stack.isEmpty() && count > 0) {
                    itemInventory.put(stack, count);
                }
            }
            ListTag fluids = tag.getList("fluidInventory", Tag.TAG_COMPOUND);
            for (Tag t : fluids) {
                if (!(t instanceof CompoundTag ct)) continue;
                FluidStack stack = FluidStack.loadFluidStackFromNBT(ct);
                long amount = ct.getLong("real");
                if (!stack.isEmpty() && amount > 0) {
                    fluidInventory.put(stack, amount);
                }
            }
            onContentsChanged();
        }
    }
}
