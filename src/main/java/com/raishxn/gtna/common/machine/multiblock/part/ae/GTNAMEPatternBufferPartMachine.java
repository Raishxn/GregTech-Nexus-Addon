package com.raishxn.gtna.common.machine.multiblock.part.ae;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.gui.widget.IntInputWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IDataStickInteractable;
import com.gregtechceu.gtceu.api.machine.feature.IDropSaveMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerList;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.ButtonConfigurator;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;
import com.gregtechceu.gtceu.integration.ae2.gui.widget.AETextInputButtonWidget;
import com.gregtechceu.gtceu.integration.ae2.gui.widget.slot.AEPatternViewSlotWidget;
import com.gregtechceu.gtceu.integration.ae2.machine.MEBusPartMachine;
import com.gregtechceu.gtceu.utils.GTMath;
import com.gregtechceu.gtceu.utils.ItemStackHashStrategy;

import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.PhantomSlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.PhantomTankWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
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
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.raishxn.gtna.api.machine.feature.IPatternBufferModeHost;
import com.raishxn.gtna.api.machine.feature.IPatternBufferModeProvider;
import com.lowdragmc.lowdraglib.misc.FluidStorage;
import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

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
    private static final String PATTERN_RECIPE_ID_TAG = "gtnaPatternRecipeId";
    private static final String PATTERN_MODE_ID_TAG = "gtnaPatternModeId";
    private static final int PANEL_WIDTH = 108;
    private static final int PANEL_HEIGHT = 250;

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

    @DescSynced
    @Persisted
    @Setter
    private String customName = "";

    private boolean needPatternSync;
    private int selectedSlot = -1;
    private WidgetGroup configPanel;
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
        if (getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().tell(new TickTask(1, this::rebuildPatternMap));
        }
    }

    @Override
    public List<RecipeHandlerList> getRecipeHandlers() {
        return internalRecipeHandler.getSlotHandlers();
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
            clearPatternRecipeMetadata(slot);
        }
    }

    @Override
    public @Nullable String gtna$getPreferredModeForRecipe(GTRecipe recipe) {
        SlotMatch match = findMatchingSlot(recipe);
        if (match == null) {
            return null;
        }
        GTNAPatternBufferSlotConfig config = slotConfigs[match.slot()];
        if (!config.getPreferredModeId().isBlank()) {
            return config.getPreferredModeId();
        }
        return config.getDerivedModeId().isBlank() ? null : config.getDerivedModeId();
    }

    @Override
    public void gtna$onRecipeStarted(GTRecipe recipe) {
        SlotMatch match = findMatchingSlot(recipe);
        if (match == null) {
            return;
        }
        cacheResolvedRecipe(match.slot(), recipe);
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
            loadPatternRecipeMetadata(i, pattern);
        }
        needPatternSync = true;
    }

    private void onSlotConfigurationChanged(int slot) {
        invalidateSlotCache(slot);
        resolveAndCacheSlotRecipe(slot);
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
        loadPatternRecipeMetadata(index, newPattern);
        resolveAndCacheSlotRecipe(index);
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
    }

    @Override
    public Widget createUIWidget() {
        int rows = Math.max(1, (int) Math.ceil(maxPatternCount / 9.0));
        int columns = Math.min(9, maxPatternCount);
        int gridWidth = 18 * columns + 16;
        int gridHeight = 18 * rows + 16;
        WidgetGroup group = new WidgetGroup(0, 0, gridWidth, gridHeight + 18);
        int index = 0;
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < 9 && index < maxPatternCount; x++) {
                int finalIndex = index;
                PatternSlotWidget slotWidget = new PatternSlotWidget(patternInventory, index++, 8 + x * 18,
                        14 + y * 18, finalIndex);
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
                slotWidget.setOnAddedTooltips((widget, tooltips) ->
                        tooltips.add(Component.translatable("gtna.machine.pattern_buffer.middle_click_hint")));
                group.addWidget(slotWidget);
            }
        }
        group.addWidget(new LabelWidget(
                8,
                2,
                () -> this.isOnline ? "gtceu.gui.me_network.online" : "gtceu.gui.me_network.offline"));
        group.addWidget(new AETextInputButtonWidget(Math.max(8, gridWidth - 70), 2, 70, 10)
                .setText(customName)
                .setOnConfirm(this::setCustomName)
                .setButtonTooltips(Component.translatable("gui.gtceu.rename.desc")));
        addConfigPanel(group, gridWidth);
        return group;
    }

    private void addConfigPanel(WidgetGroup group, int gridWidth) {
        int panelX = gridWidth + 4;
        int innerX = 8;
        int y = 8;

        configPanel = new WidgetGroup(panelX, 4, PANEL_WIDTH, PANEL_HEIGHT);
        configPanel.setBackground(GuiTextures.BACKGROUND);
        configPanel.setVisible(false);
        configPanel.setActive(false);
        group.addWidget(configPanel);

        configPanel.addWidget(new LabelWidget(innerX, y,
                () -> selectedSlot >= 0 ?
                        Component.translatable("gtna.machine.pattern_buffer.selected_slot", selectedSlot + 1)
                                .getString() :
                        Component.translatable("gtna.machine.pattern_buffer.no_slot_selected").getString()));
        y += 14;
        configPanel.addWidget(new LabelWidget(innerX, y,
                () -> Component.translatable("gtna.machine.pattern_buffer.cached_recipe_short",
                        compactDisplay(getSelectedConfig() == null ? "" : getSelectedConfig().getCachedRecipeId(), 10))
                        .getString()));
        y += 12;
        configPanel.addWidget(new LabelWidget(innerX, y,
                () -> Component.translatable("gtna.machine.pattern_buffer.derived_mode_short",
                        compactDisplay(getSelectedConfig() == null ? "" : getSelectedConfig().getDerivedModeId(), 12))
                        .getString()));

        y += 14;
        configPanel.addWidget(new LabelWidget(innerX, y,
                () -> Component.translatable("gtna.machine.pattern_buffer.circuit_field").getString()));
        y += 10;
        configPanel.addWidget(new IntInputWidget(innerX, y, 50, 14,
                () -> getSelectedConfig() == null ? -1 : getSelectedConfig().getCircuitConfig(),
                value -> {
                    GTNAPatternBufferSlotConfig config = getSelectedConfig();
                    if (config != null) {
                        config.setCircuitConfig(value);
                    }
                }).setMin(-1).setMax(32));
        configPanel.addWidget(new com.lowdragmc.lowdraglib.gui.widget.SlotWidget(circuitPreviewInventory, 0, innerX + 58, y - 2, false, false)
                .setCanPutItems(false)
                .setCanTakeItems(false)
                .setBackgroundTexture(new GuiTextureGroup(GuiTextures.SLOT, GuiTextures.INT_CIRCUIT_OVERLAY))
                .setOnAddedTooltips((widget, tooltips) -> {
                    if (circuitPreviewInventory.getStackInSlot(0).isEmpty()) {
                        tooltips.add(Component.translatable("gtna.machine.pattern_buffer.no_circuit"));
                    }
                }));
        y += 20;
        configPanel.addWidget(new LabelWidget(innerX, y,
                () -> Component.translatable("gtna.machine.pattern_buffer.item_field").getString()));
        y += 10;
        addItemGhostGrid(configPanel, innerX, y);
        y += 68;
        configPanel.addWidget(new LabelWidget(innerX, y,
                () -> Component.translatable("gtna.machine.pattern_buffer.fluid_field").getString()));
        y += 10;
        addFluidGhostGrid(configPanel, innerX, y);
        y += 68;
        configPanel.addWidget(new LabelWidget(innerX, y,
                () -> Component.translatable("gtna.machine.pattern_buffer.mode_field").getString()));
        y += 10;
        configPanel.addWidget(new TextFieldWidget(innerX, y, PANEL_WIDTH - 16, 14,
                () -> getSelectedConfig() == null ? "" : getSelectedConfig().getPreferredModeId(),
                value -> {
                    GTNAPatternBufferSlotConfig config = getSelectedConfig();
                    if (config != null) config.setPreferredModeId(value);
                })
                .setResourceLocationOnly());

        int buttonY = y + 24;
        configPanel.addWidget(makeIconButton(innerX, buttonY, GuiTextures.BUTTON_CLEAR_GRID,
                "gtna.machine.pattern_buffer.clear_specialization",
                clickData -> {
                    if (!clickData.isRemote) clearSelectedSpecialization();
                }));
        configPanel.addWidget(makeIconButton(innerX + 22, buttonY, GuiTextures.BUTTON_LIST,
                "gtna.machine.pattern_buffer.clear_cache",
                clickData -> {
                    if (!clickData.isRemote) clearSelectedRecipeCache();
                }));
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
                    .setOnAddedTooltips((widget, tooltips) ->
                            tooltips.add(Component.translatable("gtna.machine.pattern_buffer.fluid_amount_hint"))));
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
            clearPatternRecipeMetadata(selectedSlot);
            if (selectedSlot >= 0) {
                needPatternSync = true;
                refreshSelectedConfigPreview();
                markDirty();
            }
        }
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
    }

    private void loadPatternRecipeMetadata(int slot, ItemStack pattern) {
        if (slot < 0 || slot >= slotConfigs.length || pattern.isEmpty() || !pattern.hasTag()) {
            return;
        }
        CompoundTag tag = pattern.getTag();
        if (tag == null) {
            return;
        }
        GTNAPatternBufferSlotConfig config = slotConfigs[slot];
        if (tag.contains(PATTERN_RECIPE_ID_TAG, Tag.TAG_STRING)) {
            config.setCachedRecipeId(tag.getString(PATTERN_RECIPE_ID_TAG));
        }
        if (config.getPreferredModeId().isBlank() && tag.contains(PATTERN_MODE_ID_TAG, Tag.TAG_STRING)) {
            config.setDerivedModeId(tag.getString(PATTERN_MODE_ID_TAG));
        }
    }

    private void clearPatternRecipeMetadata(int slot) {
        if (slot < 0 || slot >= patternInventory.getSlots()) {
            return;
        }
        ItemStack pattern = patternInventory.getStackInSlot(slot);
        if (pattern.isEmpty() || !pattern.hasTag()) {
            return;
        }
        CompoundTag tag = pattern.getOrCreateTag();
        tag.remove(PATTERN_RECIPE_ID_TAG);
        tag.remove(PATTERN_MODE_ID_TAG);
    }

    private void cacheResolvedRecipe(int slot, GTRecipe recipe) {
        if (slot < 0 || slot >= slotConfigs.length || recipe.id == null) {
            return;
        }
        GTNAPatternBufferSlotConfig config = slotConfigs[slot];
        config.setCachedRecipeId(recipe.id.toString());
        String resolvedMode = config.getPreferredModeId().isBlank() ? resolveDerivedMode(recipe) : config.getPreferredModeId();
        if (config.getPreferredModeId().isBlank()) {
            config.setDerivedModeId(resolvedMode == null ? "" : resolvedMode);
        }
        persistPatternRecipeMetadata(slot, recipe, resolvedMode);
    }

    private void persistPatternRecipeMetadata(int slot, GTRecipe recipe, @Nullable String modeId) {
        if (slot < 0 || slot >= patternInventory.getSlots() || recipe.id == null) {
            return;
        }
        ItemStack pattern = patternInventory.getStackInSlot(slot);
        if (pattern.isEmpty()) {
            return;
        }
        CompoundTag tag = pattern.getOrCreateTag();
        tag.putString(PATTERN_RECIPE_ID_TAG, recipe.id.toString());
        if (modeId == null || modeId.isBlank()) {
            tag.remove(PATTERN_MODE_ID_TAG);
        } else {
            tag.putString(PATTERN_MODE_ID_TAG, modeId);
        }
    }

    private void resolveAndCacheSlotRecipe(int slot) {
        if (slot < 0 || slot >= maxPatternCount || isRemote()) {
            return;
        }
        ItemStack pattern = patternInventory.getStackInSlot(slot);
        if (pattern.isEmpty() || !isFormed() || getControllers().isEmpty()) {
            return;
        }
        IMultiController controller = getControllers().first();
        if (!(controller instanceof IRecipeLogicMachine recipeMachine) || !(controller instanceof com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder holder)) {
            return;
        }
        GTRecipeType[] recipeTypes = recipeMachine.getRecipeTypes();
        if (recipeTypes == null || recipeTypes.length == 0) {
            recipeTypes = new GTRecipeType[] { recipeMachine.getRecipeType() };
        }
        GTRecipe resolved = findResolvedRecipeForSlot(slot, holder, recipeTypes);
        if (resolved != null) {
            cacheResolvedRecipe(slot, resolved);
            notifyControllerModeChange(slot, resolved);
        }
    }

    /**
     * Notifica o controller do multibloco para trocar o activeRecipeType
     * imediatamente quando um pattern é inserido e a receita é resolvida.
     * Isso garante que o multibloco já esteja no modo correto ANTES da receita executar.
     */
    private void notifyControllerModeChange(int slot, GTRecipe recipe) {
        if (!isFormed() || getControllers().isEmpty()) return;
        IMultiController controller = getControllers().first();

        GTNAPatternBufferSlotConfig config = slotConfigs[slot];
        String modeId = !config.getPreferredModeId().isBlank()
                ? config.getPreferredModeId()
                : config.getDerivedModeId();

        if (modeId == null || modeId.isBlank()) return;

        if (controller instanceof IPatternBufferModeHost host) {
            host.gtna$applyPatternBufferMode(modeId, recipe);
        } else if (controller instanceof IRecipeLogicMachine recipeMachine) {
            var recipeTypes = recipeMachine.getRecipeTypes();
            if (recipeTypes != null && recipeTypes.length > 1) {
                for (int i = 0; i < recipeTypes.length; i++) {
                    if (recipeTypes[i] != null && recipeTypes[i].registryName != null) {
                        String requested = modeId.trim().toLowerCase(Locale.ROOT);
                        String fullId = recipeTypes[i].registryName.toString().toLowerCase(Locale.ROOT);
                        String path = recipeTypes[i].registryName.getPath().toLowerCase(Locale.ROOT);
                        if (requested.equals(fullId) || requested.equals(path)) {
                            if (recipeMachine.getActiveRecipeType() != i) {
                                recipeMachine.setActiveRecipeType(i);
                            }
                            return;
                        }
                    }
                }
            }
        }
    }

    private @Nullable GTRecipe findResolvedRecipeForSlot(int slot, com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder holder,
                                                         GTRecipeType[] recipeTypes) {
        String cachedRecipeId = slotConfigs[slot].getCachedRecipeId();
        GTRecipe fallback = null;
        for (GTRecipeType recipeType : recipeTypes) {
            if (recipeType == null) {
                continue;
            }
            var iterator = recipeType.searchRecipe(holder, recipe -> true);
            int searchLimit = 256;
            while (iterator.hasNext() && searchLimit-- > 0) {
                GTRecipe recipe = iterator.next();
                if (recipe == null || !matchesSlot(slot, recipe)) {
                    continue;
                }
                if (recipe.id != null && recipe.id.toString().equals(cachedRecipeId)) {
                    return recipe;
                }
                if (fallback == null) {
                    fallback = recipe;
                }
            }
        }
        return fallback;
    }

    private void onSelectedConfigWidgetChanged() {
        refreshSelectedConfigPreview();
    }

    private static String compactDisplay(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        return value.length() <= maxLength ? value : value.substring(0, Math.max(0, maxLength - 3)) + "...";
    }

    private @Nullable String resolveDerivedMode(GTRecipe recipe) {
        if (!isFormed() || getControllers().isEmpty()) {
            return null;
        }
        IMultiController controller = getControllers().first();
        if (controller instanceof IPatternBufferModeHost host) {
            return host.gtna$resolvePatternBufferMode(recipe);
        }
        if (controller instanceof IRecipeLogicMachine recipeMachine &&
                recipeMachine.getRecipeTypes() != null &&
                recipeMachine.getRecipeTypes().length > 1 &&
                recipe.getType() != null &&
                recipe.getType().registryName != null) {
            return recipe.getType().registryName.toString();
        }
        return null;
    }

    private static boolean matchesPreferredMode(GTNAPatternBufferSlotConfig config, GTRecipe recipe) {
        return matchesModeId(config.getPreferredModeId(), recipe);
    }

    private static boolean matchesDerivedMode(GTNAPatternBufferSlotConfig config, GTRecipe recipe) {
        return matchesModeId(config.getDerivedModeId(), recipe);
    }

    private static boolean matchesModeId(String modeId, GTRecipe recipe) {
        if (modeId == null || modeId.isBlank() || recipe.getType() == null || recipe.getType().registryName == null) {
            return false;
        }
        String requested = modeId.trim().toLowerCase(Locale.ROOT);
        String fullId = recipe.getType().registryName.toString().toLowerCase(Locale.ROOT);
        String path = recipe.getType().registryName.getPath().toLowerCase(Locale.ROOT);
        if (requested.equals(fullId) || requested.equals(path) ||
                path.endsWith("_" + requested) || path.endsWith("/" + requested)) {
            return true;
        }
        return ("saw".equals(requested) || "cutting_saw".equals(requested)) &&
                (path.contains("cutter") || path.contains("saw"));
    }

    private @Nullable SlotMatch findMatchingSlot(GTRecipe recipe) {
        String recipeId = recipe.id == null ? "" : recipe.id.toString();
        for (int i = 0; i < maxPatternCount; i++) {
            GTNAPatternBufferSlotConfig config = slotConfigs[i];
            if (!recipeId.isBlank() && recipeId.equals(config.getCachedRecipeId())) {
                return new SlotMatch(i);
            }
        }
        for (int i = 0; i < maxPatternCount; i++) {
            GTNAPatternBufferSlotConfig config = slotConfigs[i];
            if (!config.getPreferredModeId().isBlank() && !matchesPreferredMode(config, recipe)) {
                continue;
            }
            if (config.getPreferredModeId().isBlank() && !config.getDerivedModeId().isBlank() &&
                    !matchesDerivedMode(config, recipe)) {
                continue;
            }
            if (matchesSlot(i, recipe)) {
                return new SlotMatch(i);
            }
        }
        return null;
    }

    private boolean matchesSlot(int slotIndex, GTRecipe recipe) {
        List<Ingredient> itemInputs = copyItemInputs(recipe);
        List<FluidIngredient> fluidInputs = copyFluidInputs(recipe);
        itemInputs = consumeCircuitInventory(itemInputs);
        itemInputs = consumeVirtualItems(slotConfigs[slotIndex], itemInputs);
        fluidInputs = consumeVirtualFluids(slotConfigs[slotIndex], fluidInputs);
        itemInputs = internalInventory[slotIndex].handleItemInternal(itemInputs, true);
        fluidInputs = internalInventory[slotIndex].handleFluidInternal(fluidInputs, true);
        boolean itemsMatched = itemInputs == null || itemInputs.isEmpty();
        boolean fluidsMatched = fluidInputs == null || fluidInputs.isEmpty();
        return itemsMatched && fluidsMatched;
    }

    private List<Ingredient> copyItemInputs(GTRecipe recipe) {
        List<Ingredient> copied = new ArrayList<>();
        for (Content content : recipe.getInputContents(ItemRecipeCapability.CAP)) {
            Object inner = content.getContent();
            if (inner instanceof Ingredient ingredient) {
                copied.add(SizedIngredient.copy(ingredient));
            } else if (inner instanceof ItemStack stack && !stack.isEmpty()) {
                copied.add(SizedIngredient.create(stack.copy()));
            }
        }
        return copied;
    }

    private List<FluidIngredient> copyFluidInputs(GTRecipe recipe) {
        List<FluidIngredient> copied = new ArrayList<>();
        for (Content content : recipe.getInputContents(FluidRecipeCapability.CAP)) {
            Object inner = content.getContent();
            if (inner instanceof FluidIngredient ingredient) {
                copied.add(ingredient.copy());
            } else if (inner instanceof FluidStack stack && !stack.isEmpty()) {
                copied.add(FluidIngredient.of(stack.copy()));
            }
        }
        return copied;
    }

    private List<Ingredient> consumeCircuitInventory(List<Ingredient> left) {
        if (left == null || left.isEmpty() || !isHasCircuitSlot()) {
            return left;
        }
        ItemStack circuitStack = circuitInventory.storage.getStackInSlot(0);
        if (circuitStack.isEmpty()) {
            return left;
        }
        return consumeVirtualItemList(List.of(circuitStack), left);
    }

    private List<Ingredient> consumeVirtualItems(GTNAPatternBufferSlotConfig config, List<Ingredient> left) {
        if (left == null || left.isEmpty()) {
            return left;
        }
        return consumeVirtualItemList(config.getVirtualItemStacks(), left);
    }

    private List<Ingredient> consumeVirtualItemList(List<ItemStack> virtualStacks, List<Ingredient> left) {
        if (virtualStacks.isEmpty()) {
            return left;
        }
        for (var it = left.listIterator(); it.hasNext();) {
            Ingredient ingredient = it.next();
            if (ingredient == null || ingredient.isEmpty()) {
                it.remove();
                continue;
            }
            int amountLeft = extractAmount(ingredient);
            if (amountLeft <= 0) {
                it.remove();
                continue;
            }
            for (ItemStack stack : virtualStacks) {
                if (stack.isEmpty() || !ingredient.test(stack)) {
                    continue;
                }
                amountLeft -= stack.getCount();
                if (amountLeft <= 0) {
                    it.remove();
                    break;
                }
            }
            if (amountLeft > 0) {
                applyAmount(ingredient, amountLeft);
            }
        }
        return left.isEmpty() ? null : left;
    }

    private List<FluidIngredient> consumeVirtualFluids(GTNAPatternBufferSlotConfig config, List<FluidIngredient> left) {
        if (left == null || left.isEmpty()) {
            return left;
        }
        List<FluidStack> configuredFluids = config.getVirtualFluidStacks();
        if (configuredFluids.isEmpty()) {
            return left;
        }
        for (var it = left.listIterator(); it.hasNext();) {
            FluidIngredient ingredient = it.next();
            if (ingredient == null || ingredient.isEmpty()) {
                it.remove();
                continue;
            }
            int amountLeft = ingredient.getAmount();
            for (FluidStack configuredFluid : configuredFluids) {
                if (configuredFluid.isEmpty() || !ingredient.test(configuredFluid)) {
                    continue;
                }
                amountLeft -= configuredFluid.getAmount();
                if (amountLeft <= 0) {
                    break;
                }
            }
            if (amountLeft <= 0) {
                it.remove();
            } else {
                ingredient.setAmount(amountLeft);
            }
        }
        return left.isEmpty() ? null : left;
    }

    private static int extractAmount(Ingredient ingredient) {
        if (ingredient instanceof SizedIngredient sizedIngredient) {
            return sizedIngredient.getAmount();
        }
        ItemStack[] items = ingredient.getItems();
        return items.length > 0 ? items[0].getCount() : 0;
    }

    private static void applyAmount(Ingredient ingredient, int amount) {
        if (ingredient instanceof SizedIngredient sizedIngredient) {
            sizedIngredient.setAmount(amount);
            return;
        }
        ItemStack[] items = ingredient.getItems();
        if (items.length > 0) {
            items[0].setCount(amount);
        }
    }

    private record SlotMatch(int slot) {}

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
            return true;
        }
        return false;
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
                    Component.translatable(controllerDefinition.getDescriptionId()).append(" - " + circuitConfiguration) :
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
