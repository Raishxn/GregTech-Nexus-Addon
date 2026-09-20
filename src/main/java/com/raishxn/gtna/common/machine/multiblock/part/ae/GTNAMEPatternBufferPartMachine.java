package com.raishxn.gtna.common.machine.multiblock.part.ae;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfiguratorButton;
import com.gregtechceu.gtceu.api.gui.widget.IntInputWidget;
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
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;
import com.gregtechceu.gtceu.integration.ae2.gui.widget.AETextInputButtonWidget;
import com.gregtechceu.gtceu.integration.ae2.gui.widget.slot.AEPatternViewSlotWidget;
import com.gregtechceu.gtceu.integration.ae2.machine.MEBusPartMachine;
import com.gregtechceu.gtceu.utils.GTMath;
import com.gregtechceu.gtceu.utils.ItemStackHashStrategy;

import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.PhantomSlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.PhantomTankWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.misc.FluidStorage;
import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;
import com.lowdragmc.lowdraglib.syncdata.IContentChangeAware;
import com.lowdragmc.lowdraglib.syncdata.ITagSerializable;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
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
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageHelper;
import appeng.crafting.pattern.EncodedPatternItem;
import appeng.crafting.pattern.ProcessingPatternItem;
import appeng.helpers.patternprovider.PatternContainer;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.raishxn.gtna.api.machine.feature.IPatternBufferModeProvider;
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
    private static final int PANEL_WIDTH = 176;
    private static final int PANEL_HEIGHT = 220;
    private static final int PATTERNS_PER_PAGE = 54;

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

    private boolean needPatternSync;
    private int selectedSlot = -1;
    @Persisted
    @DescSynced
    private int currentPage;
    private WidgetGroup patternPagePanel;
    private WidgetGroup configPanel;
    private ButtonWidget modeSelectorButton;
    @DescSynced
    private String availableModeIds = "";
    private final ItemStackTransfer circuitPreviewInventory = new ItemStackTransfer(1);

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
        this.shareInventory = new NotifiableItemStackHandler(this, 9, IO.IN, IO.NONE);
        this.shareTank = new NotifiableFluidTank(this, 9, 8 * FluidType.BUCKET_VOLUME, IO.IN, IO.NONE);
        this.internalRecipeHandler = new GTNAPatternBufferRecipeHandler(this, this.internalInventory, this.slotConfigs);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        refreshAvailableModesCache();
        if (getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().tell(new TickTask(1, this::rebuildPatternMap));
        }
    }

    @Override
    public void addedToController(IMultiController controller) {
        super.addedToController(controller);
        refreshAvailableModesCache();
    }

    @Override
    public void removedFromController(IMultiController controller) {
        super.removedFromController(controller);
        refreshAvailableModesCache();
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
            } else if (ingredient instanceof SizedIngredient sized) {
                sized.setAmount(remaining);
            } else {
                candidates[0].setCount(remaining);
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
            } else {
                ingredient.setAmount(remaining);
            }
        }
        return left.isEmpty() ? null : left;
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
            refreshSelectedConfigPreview();
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
            refreshSelectedConfigPreview();
        }
        markDirty();
    }

    private void onPatternChange(int index) {
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
        WidgetGroup group = new WidgetGroup(0, 0, PANEL_WIDTH, PANEL_HEIGHT);
        group.setBackground(GuiTextures.BACKGROUND);
        patternPagePanel = new WidgetGroup(0, 0, PANEL_WIDTH, PANEL_HEIGHT);
        group.addWidget(patternPagePanel);

        patternPagePanel.addWidget(new LabelWidget(5, 4,
                () -> this.isOnline ? "gtceu.gui.me_network.online" : "gtceu.gui.me_network.offline"));
        patternPagePanel.addWidget(new AETextInputButtonWidget(96, 4, 74, 10)
                .setText(customName)
                .setOnConfirm(this::setCustomName)
                .setButtonTooltips(Component.translatable("gui.gtceu.rename.desc")));

        int pageCount = getPageCount();
        currentPage = Math.max(0, Math.min(currentPage, pageCount - 1));
        int firstSlot = currentPage * PATTERNS_PER_PAGE;
        int rows = Math.min(6, Math.max(1, (int) Math.ceil((maxPatternCount - firstSlot) / 9.0)));
        int index = firstSlot;
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < 9 && index < maxPatternCount && index < firstSlot + PATTERNS_PER_PAGE; x++) {
                int finalIndex = index;
                PatternSlotWidget slotWidget = new PatternSlotWidget(patternInventory, index++, 8 + x * 18,
                        22 + y * 18, finalIndex);
                slotWidget.setOccupiedTexture(GuiTextures.SLOT);
                slotWidget.setItemHook(stack -> {
                    if (!stack.isEmpty() && stack.getItem() instanceof EncodedPatternItem encodedPatternItem) {
                        ItemStack output = encodedPatternItem.getOutput(stack);
                        if (!output.isEmpty()) {
                            return output;
                        }
                    }
                    return stack;
                });
                slotWidget.setChangeListener(() -> onPatternChange(finalIndex));
                slotWidget.setBackground(GuiTextures.SLOT, GuiTextures.PATTERN_OVERLAY);
                slotWidget.setOnAddedTooltips((widget, tooltips) -> {
                    tooltips.add(Component.translatable("gtna.machine.pattern_buffer.middle_click_hint"));
                    // GTLCore parity: flag the slots whose resolved recipe is being cached.
                    if (finalIndex >= 0 && finalIndex < slotConfigs.length && slotConfigs[finalIndex].isCacheRecipe()) {
                        tooltips.add(Component.translatable("gtna.machine.pattern_buffer.recipe_cached"));
                    }
                });
                patternPagePanel.addWidget(slotWidget);
            }
        }
        int navigationY = 22 + Math.min(6, Math.max(1, (int) Math.ceil((maxPatternCount - firstSlot) / 9.0))) * 18 + 4;
        patternPagePanel.addWidget(new ButtonWidget(5, navigationY, 28, 13,
                new GuiTextureGroup(GuiTextures.BUTTON, new TextTexture("<<")), clickData -> {
                    if (!clickData.isRemote && currentPage > 0) currentPage--;
                }).setHoverTooltips(Component.translatable("gtna.machine.pattern_buffer.previous_page")));
        patternPagePanel.addWidget(new LabelWidget(67, navigationY + 2,
                () -> (currentPage + 1) + " / " + getPageCount()));
        patternPagePanel.addWidget(new ButtonWidget(143, navigationY, 28, 13,
                new GuiTextureGroup(GuiTextures.BUTTON, new TextTexture(">>")), clickData -> {
                    if (!clickData.isRemote && currentPage + 1 < getPageCount()) currentPage++;
                }).setHoverTooltips(Component.translatable("gtna.machine.pattern_buffer.next_page")));
        patternPagePanel.addWidget(new LabelWidget(5, navigationY + 18,
                () -> Component.translatable("gtna.machine.pattern_buffer.middle_click_hint").getString()));
        addConfigPanel(group);
        return group;
    }

    private int getPageCount() {
        return Math.max(1, (int) Math.ceil(maxPatternCount / (double) PATTERNS_PER_PAGE));
    }

    private void addConfigPanel(WidgetGroup group) {
        int innerX = 8;
        int y = 6;

        configPanel = new WidgetGroup(0, 0, PANEL_WIDTH, PANEL_HEIGHT);
        configPanel.setBackground(GuiTextures.BACKGROUND);
        configPanel.setVisible(false);
        configPanel.setActive(false);
        group.addWidget(configPanel);

        configPanel.addWidget(new ButtonWidget(innerX, y, 18, 13,
                new GuiTextureGroup(GuiTextures.BUTTON, new TextTexture("<")), clickData -> {
                    if (!clickData.isRemote) selectSlot(-1);
                }).setHoverTooltips(Component.translatable("gtna.machine.pattern_buffer.back")));
        configPanel.addWidget(new LabelWidget(innerX + 24, y + 2,
                () -> selectedSlot >= 0 ?
                        Component.translatable("gtna.machine.pattern_buffer.selected_slot", selectedSlot + 1)
                                .getString() :
                        Component.translatable("gtna.machine.pattern_buffer.no_slot_selected").getString()));
        y += 17;
        configPanel.addWidget(new LabelWidget(innerX, y,
                () -> Component.translatable("gtna.machine.pattern_buffer.cached_recipe_short",
                        compactDisplay(getSelectedConfig() == null ? "" : getSelectedConfig().getCachedRecipeId(), 27))
                        .getString()));
        y += 14;
        configPanel.addWidget(new LabelWidget(innerX, y,
                () -> Component.translatable("gtna.machine.pattern_buffer.derived_mode_short",
                        compactDisplay(getSelectedConfig() == null ? "" : getSelectedConfig().getDerivedModeId(), 27))
                        .getString()));

        y += 16;
        configPanel.addWidget(new LabelWidget(innerX, y,
                () -> Component.translatable("gtna.machine.pattern_buffer.item_field").getString()));
        y += 10;
        addItemGhostRow(configPanel, innerX, y);
        y += 24;
        configPanel.addWidget(new LabelWidget(innerX, y,
                () -> Component.translatable("gtna.machine.pattern_buffer.fluid_field").getString()));
        y += 10;
        addFluidGhostRow(configPanel, innerX, y);
        y += 24;
        configPanel.addWidget(new LabelWidget(innerX, y,
                () -> Component.translatable("gtna.machine.pattern_buffer.catalyst_item_field").getString()));
        y += 10;
        addCatalystItemGhostRow(configPanel, innerX, y);
        y += 24;
        configPanel.addWidget(new LabelWidget(innerX, y,
                () -> Component.translatable("gtna.machine.pattern_buffer.catalyst_fluid_field").getString()));
        y += 10;
        addCatalystFluidGhostRow(configPanel, innerX, y);
        y += 24;
        configPanel.addWidget(new LabelWidget(innerX, y,
                () -> Component.translatable("gtna.machine.pattern_buffer.circuit_field").getString()));
        y += 11;
        configPanel.addWidget(new IntInputWidget(innerX, y, 50, 14,
                () -> getSelectedConfig() == null ? -1 : getSelectedConfig().getCircuitConfig(),
                value -> {
                    GTNAPatternBufferSlotConfig config = getSelectedConfig();
                    if (config != null) {
                        config.setCircuitConfig(value);
                    }
                }).setMin(-1).setMax(32));
        configPanel.addWidget(new com.lowdragmc.lowdraglib.gui.widget.SlotWidget(circuitPreviewInventory, 0,
                innerX + 58, y - 2, false, false)
                .setCanPutItems(false)
                .setCanTakeItems(false)
                .setBackgroundTexture(new GuiTextureGroup(GuiTextures.SLOT, GuiTextures.INT_CIRCUIT_OVERLAY))
                .setOnAddedTooltips((widget, tooltips) -> {
                    if (circuitPreviewInventory.getStackInSlot(0).isEmpty()) {
                        tooltips.add(Component.translatable("gtna.machine.pattern_buffer.no_circuit"));
                    }
                }));
        y += 19;
        configPanel.addWidget(new LabelWidget(innerX, y,
                () -> Component.translatable("gtna.machine.pattern_buffer.mode_field").getString()));
        y += 10;
        modeSelectorButton = new ButtonWidget(innerX, y, PANEL_WIDTH - 16, 14,
                new GuiTextureGroup(
                        GuiTextures.BUTTON,
                        new TextTexture(this::getSelectedModeButtonText)
                                .setWidth(PANEL_WIDTH - 22)
                                .setType(TextTexture.TextType.ROLL)
                                .setDropShadow(false)),
                clickData -> {
                    if (!clickData.isRemote) {
                        cycleSelectedMode();
                    }
                });
        modeSelectorButton.setHoverTooltips(Component.translatable("gtna.machine.pattern_buffer.mode_button.tooltip"));
        configPanel.addWidget(modeSelectorButton);

        y += 19;
        // Per-slot recipe caching toggle (GTLCore cacheRecipe[] parity).
        configPanel.addWidget(new ButtonWidget(innerX, y, PANEL_WIDTH - 16, 14,
                new GuiTextureGroup(
                        GuiTextures.BUTTON,
                        new TextTexture(this::getCacheToggleText)
                                .setWidth(PANEL_WIDTH - 22)
                                .setType(TextTexture.TextType.ROLL)
                                .setDropShadow(false)),
                clickData -> {
                    if (!clickData.isRemote) toggleSelectedCacheRecipe();
                }).setHoverTooltips(Component.translatable("gtna.machine.pattern_buffer.cache_toggle.tooltip")));

        int buttonY = y + 19;
        configPanel.addWidget(makeTextButton(innerX, buttonY, 76,
                "gtna.machine.pattern_buffer.clear_machine_recipe_cache",
                clickData -> {
                    if (!clickData.isRemote) clearMachineRecipeCaches();
                }));
        configPanel.addWidget(makeTextButton(innerX + 84, buttonY, 84,
                "gtna.machine.pattern_buffer.clear_pattern_recipe_cache",
                clickData -> {
                    if (!clickData.isRemote) clearSelectedRecipeCache();
                }));

        // Embedded-circuit block (GTLCore PatternCircuitConfigurator parity): config input,
        // skip-existing toggle, and the two bulk actions.
        int circuitY = buttonY + 19;
        configPanel.addWidget(new LabelWidget(innerX, circuitY,
                () -> Component.translatable("gtna.machine.pattern_buffer.embedded_circuit").getString()));
        configPanel.addWidget(new IntInputWidget(innerX, circuitY + 11, 50, 14,
                this::getEmbeddedCircuitConfig,
                value -> {
                    setEmbeddedCircuitConfig(value);
                    markDirty();
                }).setMin(1).setMax(32));
        configPanel.addWidget(new ButtonWidget(innerX + 54, circuitY + 11, 52, 14,
                new GuiTextureGroup(GuiTextures.BUTTON,
                        new TextTexture(this::getSkipExistingText).setWidth(48)
                                .setType(TextTexture.TextType.ROLL).setDropShadow(false)),
                clickData -> {
                    if (!clickData.isRemote) {
                        setSkipExistingCircuitPatterns(!skipExistingCircuitPatterns);
                        markDirty();
                    }
                }).setHoverTooltips(Component.translatable("gtna.machine.pattern_buffer.skip_existing.tooltip")));
        int actionY = circuitY + 27;
        configPanel.addWidget(makeTextButton(innerX, actionY, 84,
                "gtna.machine.pattern_buffer.embed_circuit",
                clickData -> {
                    if (!clickData.isRemote) embedCircuitInAllPatterns();
                }));
        configPanel.addWidget(makeTextButton(innerX + 88, actionY, 80,
                "gtna.machine.pattern_buffer.remove_circuits",
                clickData -> {
                    if (!clickData.isRemote) removeAllPatternCircuits();
                }));
    }

    private String getSkipExistingText() {
        return Component.translatable(skipExistingCircuitPatterns ?
                "gtna.machine.pattern_buffer.skip_existing.on" : "gtna.machine.pattern_buffer.skip_existing.off")
                .getString();
    }

    private String getCacheToggleText() {
        GTNAPatternBufferSlotConfig config = getSelectedConfig();
        boolean enabled = config == null || config.isCacheRecipe();
        return Component.translatable(enabled ? "gtna.machine.pattern_buffer.cache_toggle.on" :
                "gtna.machine.pattern_buffer.cache_toggle.off").getString();
    }

    // ------------------------------------------------------------------
    // Embedded circuit actions (GTLCore embedCircuitToPatterns /
    // removeAllPatternCircuits parity). Both only touch encoded patterns;
    // slot configs are left as they are.
    // ------------------------------------------------------------------

    /** Writes {@link #embeddedCircuitConfig} into every pattern that does not already have one. */
    private void embedCircuitInAllPatterns() {
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
    private void removeAllPatternCircuits() {
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

    private void toggleSelectedCacheRecipe() {
        GTNAPatternBufferSlotConfig config = getSelectedConfig();
        if (config == null) {
            return;
        }
        config.setCacheRecipe(!config.isCacheRecipe());
        if (selectedSlot >= 0) {
            invalidateSlotCache(selectedSlot);
            slotResolver.resolveAndCacheSlotRecipe(selectedSlot);
            refreshSelectedConfigPreview();
            markDirty();
        }
    }

    private void addItemGhostRow(WidgetGroup panel, int x, int y) {
        for (int slot = 0; slot < 9; slot++) {
            int logicalSlot = slot;
            panel.addWidget(new PhantomSlotWidget(new SelectedConfigItemTransfer(), logicalSlot, x + slot * 18, y)
                    .setClearSlotOnRightClick(true)
                    .setChangeListener(this::onSelectedConfigWidgetChanged)
                    .setBackgroundTexture(new GuiTextureGroup(GuiTextures.SLOT, GuiTextures.FILTER_SLOT_OVERLAY)));
        }
    }

    private void addFluidGhostRow(WidgetGroup panel, int x, int y) {
        for (int slot = 0; slot < 9; slot++) {
            FluidStorageProxy storage = new FluidStorageProxy(slot);
            panel.addWidget(new PhantomTankWidget(storage, x + slot * 18, y, 18, 18)
                    .setAllowClickFilled(true)
                    .setAllowClickDrained(true)
                    .setBackground(GuiTextures.FLUID_SLOT)
                    .setChangeListener(this::onSelectedConfigWidgetChanged));
        }
    }

    private void addItemGhostGrid(WidgetGroup panel, int x, int y) {
        WidgetGroup container = new WidgetGroup(x, y, 62, 62);
        container.setBackground(GuiTextures.BACKGROUND_INVERSE);
        for (int slot = 0; slot < 9; slot++) {
            int drawX = 4 + (slot % 3) * 18;
            int drawY = 4 + (slot / 3) * 18;
            int logicalSlot = slot;
            container.addWidget(new PhantomSlotWidget(new SelectedConfigItemTransfer(), logicalSlot, drawX, drawY)
                    .setClearSlotOnRightClick(true)
                    .setChangeListener(this::onSelectedConfigWidgetChanged)
                    .setBackgroundTexture(new GuiTextureGroup(GuiTextures.SLOT, GuiTextures.FILTER_SLOT_OVERLAY)));
        }
        panel.addWidget(container);
    }

    private void addCatalystItemGhostRow(WidgetGroup panel, int x, int y) {
        for (int slot = 0; slot < 9; slot++) {
            int logicalSlot = slot;
            panel.addWidget(
                    new PhantomSlotWidget(new SelectedConfigCatalystItemTransfer(), logicalSlot, x + slot * 18, y)
                            .setClearSlotOnRightClick(true)
                            .setChangeListener(this::onSelectedConfigWidgetChanged)
                            .setBackgroundTexture(
                                    new GuiTextureGroup(GuiTextures.SLOT, GuiTextures.FILTER_SLOT_OVERLAY)));
        }
    }

    private void addCatalystFluidGhostRow(WidgetGroup panel, int x, int y) {
        for (int slot = 0; slot < 9; slot++) {
            CatalystFluidStorageProxy storage = new CatalystFluidStorageProxy(slot);
            panel.addWidget(new PhantomTankWidget(storage, x + slot * 18, y, 18, 18)
                    .setAllowClickFilled(true)
                    .setAllowClickDrained(true)
                    .setBackground(GuiTextures.FLUID_SLOT)
                    .setChangeListener(this::onSelectedConfigWidgetChanged));
        }
    }

    private void addFluidGhostGrid(WidgetGroup panel, int x, int y) {
        WidgetGroup container = new WidgetGroup(x, y, 62, 62);
        container.setBackground(GuiTextures.BACKGROUND_INVERSE);
        for (int slot = 0; slot < 9; slot++) {
            int drawX = 4 + (slot % 3) * 18;
            int drawY = 4 + (slot / 3) * 18;
            FluidStorageProxy storage = new FluidStorageProxy(slot);
            container.addWidget(new PhantomTankWidget(storage, drawX, drawY, 18, 18)
                    .setAllowClickFilled(true)
                    .setAllowClickDrained(true)
                    .setBackground(GuiTextures.FLUID_SLOT)
                    .setChangeListener(this::onSelectedConfigWidgetChanged)
                    .setOnAddedTooltips((widget, tooltips) -> tooltips
                            .add(Component.translatable("gtna.machine.pattern_buffer.fluid_amount_hint"))));
        }
        panel.addWidget(container);
    }

    private ButtonWidget makeIconButton(int x, int y, com.lowdragmc.lowdraglib.gui.texture.IGuiTexture icon, String key,
                                        java.util.function.Consumer<com.lowdragmc.lowdraglib.gui.util.ClickData> onPress) {
        ButtonWidget button = new ButtonWidget(x, y, 18, 18,
                new GuiTextureGroup(GuiTextures.BUTTON, icon), onPress);
        button.setHoverTexture(new GuiTextureGroup(GuiTextures.BUTTON, icon));
        button.setHoverTooltips(Component.translatable(key));
        return button;
    }

    private ButtonWidget makeTextButton(int x, int y, int width, String key,
                                        java.util.function.Consumer<com.lowdragmc.lowdraglib.gui.util.ClickData> onPress) {
        ButtonWidget button = new ButtonWidget(x, y, width, 13,
                new GuiTextureGroup(GuiTextures.BUTTON,
                        new TextTexture(() -> Component.translatable(key).getString())
                                .setWidth(width - 4)
                                .setType(TextTexture.TextType.ROLL)
                                .setDropShadow(false)),
                onPress);
        button.setHoverTooltips(Component.translatable(key + ".tooltip"));
        return button;
    }

    private void selectSlot(int slot) {
        if (slot >= 0 && slot < maxPatternCount && this.selectedSlot == slot) {
            this.selectedSlot = -1;
        } else {
            this.selectedSlot = slot >= 0 && slot < maxPatternCount ? slot : -1;
        }
        if (configPanel != null) {
            configPanel.setVisible(this.selectedSlot >= 0);
            configPanel.setActive(this.selectedSlot >= 0);
        }
        refreshSelectedConfigPreview();
        refreshModeSelector();
    }

    private @Nullable GTNAPatternBufferSlotConfig getSelectedConfig() {
        return selectedSlot >= 0 && selectedSlot < slotConfigs.length ? slotConfigs[selectedSlot] : null;
    }

    private void clearSelectedSpecialization() {
        GTNAPatternBufferSlotConfig config = getSelectedConfig();
        if (config != null) {
            config.clearSpecialization();
        }
    }

    private void clearSelectedRecipeCache() {
        GTNAPatternBufferSlotConfig config = getSelectedConfig();
        if (config != null) {
            config.clearRecipeCache();
            slotResolver.clearPatternRecipeMetadata(selectedSlot);
            if (selectedSlot >= 0) {
                needPatternSync = true;
                refreshSelectedConfigPreview();
                markDirty();
            }
        }
    }

    /** Clears runtime lookup caches without changing any encoded pattern data. */
    private void clearMachineRecipeCaches() {
        for (GTNAPatternBufferSlotConfig config : slotConfigs) {
            config.clearRecipeCacheSilently();
        }
        needPatternSync = true;
        refreshSelectedConfigPreview();
        markDirty();
    }

    private void refreshSelectedConfigPreview() {
        ItemStack stack = ItemStack.EMPTY;
        GTNAPatternBufferSlotConfig config = getSelectedConfig();
        if (config != null) {
            ItemStack configuredCircuit = config.getCircuitStack();
            if (configuredCircuit != null) {
                stack = configuredCircuit;
            }
        }
        circuitPreviewInventory.setStackInSlot(0, stack);
        refreshModeSelector();
    }

    private void onSelectedConfigWidgetChanged() {
        refreshSelectedConfigPreview();
    }

    private List<ModeOption> getAvailableModeOptions() {
        List<ModeOption> options = new ArrayList<>();
        options.add(new ModeOption("", Component.translatable("gtna.machine.pattern_buffer.mode.auto").getString()));

        Set<String> seen = new LinkedHashSet<>();
        for (String modeId : getCachedAvailableModeIds()) {
            if (seen.add(modeId)) {
                options.add(new ModeOption(modeId, formatModeLabel(modeId)));
            }
        }
        if (isFormed() && !getControllers().isEmpty()) {
            IMultiController controller = getControllers().first();
            if (controller instanceof IRecipeLogicMachine recipeMachine) {
                GTRecipeType[] recipeTypes = recipeMachine.getRecipeTypes();
                if (recipeTypes == null || recipeTypes.length == 0) {
                    recipeTypes = new GTRecipeType[] { recipeMachine.getRecipeType() };
                }
                for (GTRecipeType recipeType : recipeTypes) {
                    if (recipeType == null || recipeType.registryName == null) {
                        continue;
                    }
                    String id = recipeType.registryName.toString();
                    if (seen.add(id)) {
                        options.add(new ModeOption(id, formatModeLabel(recipeType)));
                    }
                }
            }
        }

        GTNAPatternBufferSlotConfig config = getSelectedConfig();
        if (config != null && !config.getPreferredModeId().isBlank() && seen.add(config.getPreferredModeId())) {
            options.add(new ModeOption(config.getPreferredModeId(),
                    Component.translatable("gtna.machine.pattern_buffer.mode.legacy",
                            compactDisplay(config.getPreferredModeId(), 18)).getString()));
        }
        if (config != null && config.getPreferredModeId().isBlank() && !config.getDerivedModeId().isBlank() &&
                seen.add(config.getDerivedModeId())) {
            options.add(new ModeOption(config.getDerivedModeId(), formatModeLabel(config.getDerivedModeId())));
        }
        return options;
    }

    private void cycleSelectedMode() {
        GTNAPatternBufferSlotConfig config = getSelectedConfig();
        if (config == null) {
            return;
        }
        List<ModeOption> options = getAvailableModeOptions();
        int currentIndex = getCurrentModeOptionIndex(options, config.getPreferredModeId());
        ModeOption next = options.get((currentIndex + 1) % options.size());
        config.setPreferredModeId(next.id());
        refreshModeSelector();
    }

    private int getCurrentModeOptionIndex(List<ModeOption> options, String preferredModeId) {
        String current = preferredModeId == null ? "" : preferredModeId.trim();
        for (int i = 0; i < options.size(); i++) {
            if (Objects.equals(options.get(i).id(), current)) {
                return i;
            }
        }
        return 0;
    }

    private String getSelectedModeButtonText() {
        GTNAPatternBufferSlotConfig config = getSelectedConfig();
        if (config == null) {
            return Component.translatable("gtna.machine.pattern_buffer.mode.none").getString();
        }
        List<ModeOption> options = getAvailableModeOptions();
        return options.get(getCurrentModeOptionIndex(options, config.getPreferredModeId())).label();
    }

    private void refreshModeSelector() {
        if (modeSelectorButton == null) {
            return;
        }
        GTNAPatternBufferSlotConfig config = getSelectedConfig();
        if (config == null) {
            modeSelectorButton.setActive(false);
            modeSelectorButton.setHoverTooltips(Component.translatable("gtna.machine.pattern_buffer.mode.none"));
            return;
        }
        modeSelectorButton.setActive(true);
        String preferredMode = config.getPreferredModeId().isBlank() ?
                Component.translatable("gtna.machine.pattern_buffer.mode.auto").getString() :
                config.getPreferredModeId();
        String derivedMode = config.getDerivedModeId().isBlank() ?
                Component.translatable("gtna.machine.pattern_buffer.mode.none").getString() : config.getDerivedModeId();
        modeSelectorButton.setHoverTooltips(
                Component.translatable("gtna.machine.pattern_buffer.mode_button.tooltip"),
                Component.translatable("gtna.machine.pattern_buffer.mode_button.current", preferredMode),
                Component.translatable("gtna.machine.pattern_buffer.mode_button.derived", derivedMode));
    }

    private static String compactDisplay(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        return value.length() <= maxLength ? value : value.substring(0, Math.max(0, maxLength - 3)) + "...";
    }

    private static String formatModeLabel(GTRecipeType recipeType) {
        if (recipeType == null || recipeType.registryName == null) {
            return "-";
        }
        return formatModeLabel(recipeType.registryName.toString());
    }

    private static String formatModeLabel(String modeId) {
        if (modeId == null || modeId.isBlank()) {
            return "-";
        }
        String path = modeId;
        int namespaceSeparator = path.indexOf(':');
        if (namespaceSeparator >= 0 && namespaceSeparator + 1 < path.length()) {
            path = path.substring(namespaceSeparator + 1);
        }
        String[] parts = path.split("[/_]");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isBlank()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                builder.append(part.substring(1));
            }
        }
        return builder.length() == 0 ? path : builder.toString();
    }

    private List<String> getCachedAvailableModeIds() {
        if (availableModeIds == null || availableModeIds.isBlank()) {
            return List.of();
        }
        List<String> ids = new ArrayList<>();
        for (String token : availableModeIds.split("\\|")) {
            String trimmed = token == null ? "" : token.trim();
            if (!trimmed.isBlank()) {
                ids.add(trimmed);
            }
        }
        return ids;
    }

    private void refreshAvailableModesCache() {
        Set<String> ids = new LinkedHashSet<>();
        if (isFormed() && !getControllers().isEmpty()) {
            for (IMultiController controller : getControllers()) {
                if (!(controller instanceof IRecipeLogicMachine recipeMachine)) {
                    continue;
                }
                GTRecipeType[] recipeTypes = recipeMachine.getRecipeTypes();
                if (recipeTypes == null || recipeTypes.length == 0) {
                    recipeTypes = new GTRecipeType[] { recipeMachine.getRecipeType() };
                }
                for (GTRecipeType recipeType : recipeTypes) {
                    if (recipeType != null && recipeType.registryName != null) {
                        ids.add(recipeType.registryName.toString());
                    }
                }
            }
        }
        availableModeIds = String.join("|", ids);
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

    private record ModeOption(String id, String label) {}

    private final class SelectedConfigItemTransfer extends ItemStackTransfer {

        private SelectedConfigItemTransfer() {
            super(9);
        }

        @Override
        public int getSlots() {
            GTNAPatternBufferSlotConfig config = getSelectedConfig();
            return config == null ? 9 : config.getSpecialItems().getSlots();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            GTNAPatternBufferSlotConfig config = getSelectedConfig();
            return config == null ? ItemStack.EMPTY : config.getSpecialItems().getStackInSlot(slot);
        }

        @Override
        public void setStackInSlot(int slot, ItemStack stack) {
            GTNAPatternBufferSlotConfig config = getSelectedConfig();
            if (config != null) {
                config.getSpecialItems().setStackInSlot(slot, stack);
            }
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate, boolean notifyChanges) {
            GTNAPatternBufferSlotConfig config = getSelectedConfig();
            if (config == null) {
                return stack;
            }
            return config.getSpecialItems().insertItem(slot, stack, simulate, notifyChanges);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate, boolean notifyChanges) {
            GTNAPatternBufferSlotConfig config = getSelectedConfig();
            if (config == null) {
                return ItemStack.EMPTY;
            }
            return config.getSpecialItems().extractItem(slot, amount, simulate, notifyChanges);
        }

        @Override
        public int getSlotLimit(int slot) {
            GTNAPatternBufferSlotConfig config = getSelectedConfig();
            return config == null ? 64 : config.getSpecialItems().getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return !(stack.getItem() instanceof ProcessingPatternItem);
        }
    }

    /**
     * Same indirection as {@link SelectedConfigItemTransfer} but bound to the per-slot catalyst
     * item inventory (GTLCore's catalyst UI edits {@code catalystItems} directly; here we route
     * through the selected slot config so one row serves whichever slot is open).
     */
    private final class SelectedConfigCatalystItemTransfer extends ItemStackTransfer {

        private SelectedConfigCatalystItemTransfer() {
            super(9);
        }

        @Override
        public int getSlots() {
            GTNAPatternBufferSlotConfig config = getSelectedConfig();
            return config == null ? 9 : config.getCatalystItems().getSlots();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            GTNAPatternBufferSlotConfig config = getSelectedConfig();
            return config == null ? ItemStack.EMPTY : config.getCatalystItems().getStackInSlot(slot);
        }

        @Override
        public void setStackInSlot(int slot, ItemStack stack) {
            GTNAPatternBufferSlotConfig config = getSelectedConfig();
            if (config != null) {
                config.getCatalystItems().setStackInSlot(slot, stack);
            }
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate, boolean notifyChanges) {
            GTNAPatternBufferSlotConfig config = getSelectedConfig();
            if (config == null) {
                return stack;
            }
            return config.getCatalystItems().insertItem(slot, stack, simulate, notifyChanges);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate, boolean notifyChanges) {
            GTNAPatternBufferSlotConfig config = getSelectedConfig();
            if (config == null) {
                return ItemStack.EMPTY;
            }
            return config.getCatalystItems().extractItem(slot, amount, simulate, notifyChanges);
        }

        @Override
        public int getSlotLimit(int slot) {
            GTNAPatternBufferSlotConfig config = getSelectedConfig();
            return config == null ? 64 : config.getCatalystItems().getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return !(stack.getItem() instanceof ProcessingPatternItem);
        }
    }

    private final class CatalystFluidStorageProxy extends FluidStorage {

        private final int slot;

        private CatalystFluidStorageProxy(int slot) {
            super(Integer.MAX_VALUE);
            this.slot = slot;
        }

        @Override
        public com.lowdragmc.lowdraglib.side.fluid.FluidStack getFluid() {
            GTNAPatternBufferSlotConfig config = getSelectedConfig();
            return config == null ? com.lowdragmc.lowdraglib.side.fluid.FluidStack.empty() :
                    config.getCatalystFluids()[slot].getFluid();
        }

        @Override
        public void setFluid(com.lowdragmc.lowdraglib.side.fluid.FluidStack fluid) {
            GTNAPatternBufferSlotConfig config = getSelectedConfig();
            if (config != null) {
                config.getCatalystFluids()[slot].setFluid(fluid);
            }
        }

        @Override
        public long getCapacity() {
            GTNAPatternBufferSlotConfig config = getSelectedConfig();
            return config == null ? Integer.MAX_VALUE : config.getCatalystFluids()[slot].getCapacity();
        }
    }

    private final class FluidStorageProxy extends FluidStorage {

        private final int slot;

        private FluidStorageProxy(int slot) {
            super(Integer.MAX_VALUE);
            this.slot = slot;
        }

        @Override
        public com.lowdragmc.lowdraglib.side.fluid.FluidStack getFluid() {
            GTNAPatternBufferSlotConfig config = getSelectedConfig();
            return config == null ? com.lowdragmc.lowdraglib.side.fluid.FluidStack.empty() :
                    config.getSpecialFluids()[slot].getFluid();
        }

        @Override
        public void setFluid(com.lowdragmc.lowdraglib.side.fluid.FluidStack fluid) {
            GTNAPatternBufferSlotConfig config = getSelectedConfig();
            if (config != null) {
                config.getSpecialFluids()[slot].setFluid(fluid);
            }
        }

        @Override
        public long getCapacity() {
            GTNAPatternBufferSlotConfig config = getSelectedConfig();
            return config == null ? Integer.MAX_VALUE : config.getSpecialFluids()[slot].getCapacity();
        }
    }

    private final class PatternSlotWidget extends AEPatternViewSlotWidget {

        private final int logicalSlot;

        private PatternSlotWidget(CustomItemStackHandler itemHandler, int slotIndex, int xPosition, int yPosition,
                                  int logicalSlot) {
            super(itemHandler, slotIndex, xPosition, yPosition);
            this.logicalSlot = logicalSlot;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (isMouseOverElement(mouseX, mouseY) && button == 2) {
                selectSlot(logicalSlot);
                writeClientAction(200, buffer -> buffer.writeVarInt(logicalSlot));
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public void handleClientAction(int id, FriendlyByteBuf buffer) {
            super.handleClientAction(id, buffer);
            if (id == 200) {
                selectSlot(buffer.readVarInt());
            }
        }
    }

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
                if (inserted > 0) {
                    count -= inserted;
                    if (count == 0) {
                        it.remove();
                    } else {
                        entry.setValue(count);
                    }
                }
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
                if (inserted > 0) {
                    amount -= inserted;
                    if (amount == 0) {
                        it.remove();
                    } else {
                        entry.setValue(amount);
                    }
                }
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
