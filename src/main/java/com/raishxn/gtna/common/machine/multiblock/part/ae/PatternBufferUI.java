package com.raishxn.gtna.common.machine.multiblock.part.ae;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.IntInputWidget;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.integration.ae2.gui.widget.AETextInputButtonWidget;
import com.gregtechceu.gtceu.integration.ae2.gui.widget.slot.AEPatternViewSlotWidget;

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

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import appeng.crafting.pattern.EncodedPatternItem;
import appeng.crafting.pattern.ProcessingPatternItem;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * UI half of the Pattern Buffer part machine (Fase 3 extraction).
 *
 * <p>
 * Owns the page of pattern slots and the per-slot configuration panel: widget construction, the
 * ghost rows for special/catalyst items and fluids, the mode selector's presentation, and the
 * client-side selection/preview state that backs them. The machine keeps the persistent/synced
 * state ({@code selectedSlot}, {@code currentPage}, the inventories and slot configs) and the
 * domain actions the buttons trigger; this class is deliberately behaviour-free apart from
 * selection bookkeeping, so it can be reconstructed whenever a UI is opened.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
final class PatternBufferUI {

    private static final int PANEL_WIDTH = 176;
    private static final int PANEL_HEIGHT = 220;
    private static final int PATTERNS_PER_PAGE = 54;

    private final GTNAMEPatternBufferPartMachine machine;

    private WidgetGroup patternPagePanel;
    private WidgetGroup configPanel;
    private ButtonWidget modeSelectorButton;
    private final ItemStackTransfer circuitPreviewInventory = new ItemStackTransfer(1);

    PatternBufferUI(GTNAMEPatternBufferPartMachine machine) {
        this.machine = machine;
    }

    Widget createUIWidget() {
        WidgetGroup group = new WidgetGroup(0, 0, PANEL_WIDTH, PANEL_HEIGHT);
        group.setBackground(GuiTextures.BACKGROUND);
        patternPagePanel = new WidgetGroup(0, 0, PANEL_WIDTH, PANEL_HEIGHT);
        group.addWidget(patternPagePanel);

        patternPagePanel.addWidget(new LabelWidget(5, 4,
                () -> machine.isOnlineForUi() ? "gtceu.gui.me_network.online" : "gtceu.gui.me_network.offline"));
        patternPagePanel.addWidget(new AETextInputButtonWidget(96, 4, 74, 10)
                .setText(machine.getCustomName())
                .setOnConfirm(machine::setCustomName)
                .setButtonTooltips(Component.translatable("gui.gtceu.rename.desc")));

        int pageCount = getPageCount();
        machine.setCurrentPage(Math.max(0, Math.min(machine.getCurrentPage(), pageCount - 1)));
        int firstSlot = machine.getCurrentPage() * PATTERNS_PER_PAGE;
        int maxPatternCount = machine.getMaxPatternCount();
        int rows = Math.min(6, Math.max(1, (int) Math.ceil((maxPatternCount - firstSlot) / 9.0)));
        int index = firstSlot;
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < 9 && index < maxPatternCount && index < firstSlot + PATTERNS_PER_PAGE; x++) {
                int finalIndex = index;
                PatternSlotWidget slotWidget = new PatternSlotWidget(machine.getPatternInventory(), index++,
                        8 + x * 18, 22 + y * 18, finalIndex);
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
                slotWidget.setChangeListener(() -> machine.onPatternChange(finalIndex));
                slotWidget.setBackground(GuiTextures.SLOT, GuiTextures.PATTERN_OVERLAY);
                slotWidget.setOnAddedTooltips((widget, tooltips) -> {
                    tooltips.add(Component.translatable("gtna.machine.pattern_buffer.middle_click_hint"));
                    // GTLCore parity: flag the slots whose resolved recipe is being cached.
                    GTNAPatternBufferSlotConfig[] slotConfigs = machine.getSlotConfigs();
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
                    if (!clickData.isRemote && machine.getCurrentPage() > 0) machine.setCurrentPage(
                            machine.getCurrentPage() - 1);
                }).setHoverTooltips(Component.translatable("gtna.machine.pattern_buffer.previous_page")));
        patternPagePanel.addWidget(new LabelWidget(67, navigationY + 2,
                () -> (machine.getCurrentPage() + 1) + " / " + getPageCount()));
        patternPagePanel.addWidget(new ButtonWidget(143, navigationY, 28, 13,
                new GuiTextureGroup(GuiTextures.BUTTON, new TextTexture(">>")), clickData -> {
                    if (!clickData.isRemote && machine.getCurrentPage() + 1 < getPageCount()) machine.setCurrentPage(
                            machine.getCurrentPage() + 1);
                }).setHoverTooltips(Component.translatable("gtna.machine.pattern_buffer.next_page")));
        patternPagePanel.addWidget(new LabelWidget(5, navigationY + 18,
                () -> Component.translatable("gtna.machine.pattern_buffer.middle_click_hint").getString()));
        addConfigPanel(group);
        return group;
    }

    private int getPageCount() {
        return Math.max(1, (int) Math.ceil(machine.getMaxPatternCount() / (double) PATTERNS_PER_PAGE));
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
                () -> machine.getSelectedSlot() >= 0 ?
                        Component.translatable("gtna.machine.pattern_buffer.selected_slot",
                                machine.getSelectedSlot() + 1).getString() :
                        Component.translatable("gtna.machine.pattern_buffer.no_slot_selected").getString()));
        y += 17;
        configPanel.addWidget(new LabelWidget(innerX, y,
                () -> Component.translatable("gtna.machine.pattern_buffer.cached_recipe_short",
                        PatternBufferModeRegistry.compactDisplay(
                                machine.getSelectedConfig() == null ? "" :
                                        machine.getSelectedConfig().getCachedRecipeId(),
                                27))
                        .getString()));
        y += 14;
        configPanel.addWidget(new LabelWidget(innerX, y,
                () -> Component.translatable("gtna.machine.pattern_buffer.derived_mode_short",
                        PatternBufferModeRegistry.compactDisplay(
                                machine.getSelectedConfig() == null ? "" :
                                        machine.getSelectedConfig().getDerivedModeId(),
                                27))
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
                () -> machine.getSelectedConfig() == null ? -1 : machine.getSelectedConfig().getCircuitConfig(),
                value -> {
                    GTNAPatternBufferSlotConfig config = machine.getSelectedConfig();
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
                    if (!clickData.isRemote) machine.toggleSelectedCacheRecipe();
                }).setHoverTooltips(Component.translatable("gtna.machine.pattern_buffer.cache_toggle.tooltip")));

        int buttonY = y + 19;
        configPanel.addWidget(makeTextButton(innerX, buttonY, 76,
                "gtna.machine.pattern_buffer.clear_machine_recipe_cache",
                clickData -> {
                    if (!clickData.isRemote) machine.clearMachineRecipeCaches();
                }));
        configPanel.addWidget(makeTextButton(innerX + 84, buttonY, 84,
                "gtna.machine.pattern_buffer.clear_pattern_recipe_cache",
                clickData -> {
                    if (!clickData.isRemote) machine.clearSelectedRecipeCache();
                }));

        // Embedded-circuit block (GTLCore PatternCircuitConfigurator parity): config input,
        // skip-existing toggle, and the two bulk actions.
        int circuitY = buttonY + 19;
        configPanel.addWidget(new LabelWidget(innerX, circuitY,
                () -> Component.translatable("gtna.machine.pattern_buffer.embedded_circuit").getString()));
        configPanel.addWidget(new IntInputWidget(innerX, circuitY + 11, 50, 14,
                machine::getEmbeddedCircuitConfig,
                value -> {
                    machine.setEmbeddedCircuitConfig(value);
                    machine.markDirty();
                }).setMin(1).setMax(32));
        configPanel.addWidget(new ButtonWidget(innerX + 54, circuitY + 11, 52, 14,
                new GuiTextureGroup(GuiTextures.BUTTON,
                        new TextTexture(this::getSkipExistingText).setWidth(48)
                                .setType(TextTexture.TextType.ROLL).setDropShadow(false)),
                clickData -> {
                    if (!clickData.isRemote) {
                        machine.setSkipExistingCircuitPatterns(!machine.isSkipExistingCircuitPatterns());
                        machine.markDirty();
                    }
                }).setHoverTooltips(Component.translatable("gtna.machine.pattern_buffer.skip_existing.tooltip")));
        int actionY = circuitY + 27;
        configPanel.addWidget(makeTextButton(innerX, actionY, 84,
                "gtna.machine.pattern_buffer.embed_circuit",
                clickData -> {
                    if (!clickData.isRemote) machine.embedCircuitInAllPatterns();
                }));
        configPanel.addWidget(makeTextButton(innerX + 88, actionY, 80,
                "gtna.machine.pattern_buffer.remove_circuits",
                clickData -> {
                    if (!clickData.isRemote) machine.removeAllPatternCircuits();
                }));
    }

    private String getSkipExistingText() {
        return Component.translatable(machine.isSkipExistingCircuitPatterns() ?
                "gtna.machine.pattern_buffer.skip_existing.on" : "gtna.machine.pattern_buffer.skip_existing.off")
                .getString();
    }

    private String getCacheToggleText() {
        GTNAPatternBufferSlotConfig config = machine.getSelectedConfig();
        boolean enabled = config == null || config.isCacheRecipe();
        return Component.translatable(enabled ? "gtna.machine.pattern_buffer.cache_toggle.on" :
                "gtna.machine.pattern_buffer.cache_toggle.off").getString();
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
        if (slot >= 0 && slot < machine.getMaxPatternCount() && machine.getSelectedSlot() == slot) {
            machine.setSelectedSlot(-1);
        } else {
            machine.setSelectedSlot(slot >= 0 && slot < machine.getMaxPatternCount() ? slot : -1);
        }
        if (configPanel != null) {
            configPanel.setVisible(machine.getSelectedSlot() >= 0);
            configPanel.setActive(machine.getSelectedSlot() >= 0);
        }
        refreshSelectedConfigPreview();
        refreshModeSelector();
    }

    private void clearSelectedSpecialization() {
        GTNAPatternBufferSlotConfig config = machine.getSelectedConfig();
        if (config != null) {
            config.clearSpecialization();
        }
    }

    /** Pushes the selected slot's configured circuit into the read-only preview slot. */
    void refreshSelectedConfigPreview() {
        ItemStack stack = ItemStack.EMPTY;
        GTNAPatternBufferSlotConfig config = machine.getSelectedConfig();
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

    private void cycleSelectedMode() {
        GTNAPatternBufferSlotConfig config = machine.getSelectedConfig();
        if (config == null) {
            return;
        }
        List<PatternBufferModeRegistry.ModeOption> options = machine.getModeRegistry().getAvailableModeOptions();
        int currentIndex = getCurrentModeOptionIndex(options, config.getPreferredModeId());
        PatternBufferModeRegistry.ModeOption next = options.get((currentIndex + 1) % options.size());
        config.setPreferredModeId(next.id());
        refreshModeSelector();
    }

    private int getCurrentModeOptionIndex(List<PatternBufferModeRegistry.ModeOption> options,
                                          String preferredModeId) {
        String current = preferredModeId == null ? "" : preferredModeId.trim();
        for (int i = 0; i < options.size(); i++) {
            if (java.util.Objects.equals(options.get(i).id(), current)) {
                return i;
            }
        }
        return 0;
    }

    private String getSelectedModeButtonText() {
        GTNAPatternBufferSlotConfig config = machine.getSelectedConfig();
        if (config == null) {
            return Component.translatable("gtna.machine.pattern_buffer.mode.none").getString();
        }
        List<PatternBufferModeRegistry.ModeOption> options = machine.getModeRegistry().getAvailableModeOptions();
        return options.get(getCurrentModeOptionIndex(options, config.getPreferredModeId())).label();
    }

    private void refreshModeSelector() {
        if (modeSelectorButton == null) {
            return;
        }
        GTNAPatternBufferSlotConfig config = machine.getSelectedConfig();
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
                Component.translatable("gtna.machine.pattern_buffer.mode.none").getString() :
                config.getDerivedModeId();
        modeSelectorButton.setHoverTooltips(
                Component.translatable("gtna.machine.pattern_buffer.mode_button.tooltip"),
                Component.translatable("gtna.machine.pattern_buffer.mode_button.current", preferredMode),
                Component.translatable("gtna.machine.pattern_buffer.mode_button.derived", derivedMode));
    }

    private final class SelectedConfigItemTransfer extends ItemStackTransfer {

        private SelectedConfigItemTransfer() {
            super(9);
        }

        @Override
        public int getSlots() {
            GTNAPatternBufferSlotConfig config = machine.getSelectedConfig();
            return config == null ? 9 : config.getSpecialItems().getSlots();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            GTNAPatternBufferSlotConfig config = machine.getSelectedConfig();
            return config == null ? ItemStack.EMPTY : config.getSpecialItems().getStackInSlot(slot);
        }

        @Override
        public void setStackInSlot(int slot, ItemStack stack) {
            GTNAPatternBufferSlotConfig config = machine.getSelectedConfig();
            if (config != null) {
                config.getSpecialItems().setStackInSlot(slot, stack);
            }
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate, boolean notifyChanges) {
            GTNAPatternBufferSlotConfig config = machine.getSelectedConfig();
            if (config == null) {
                return stack;
            }
            return config.getSpecialItems().insertItem(slot, stack, simulate, notifyChanges);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate, boolean notifyChanges) {
            GTNAPatternBufferSlotConfig config = machine.getSelectedConfig();
            if (config == null) {
                return ItemStack.EMPTY;
            }
            return config.getSpecialItems().extractItem(slot, amount, simulate, notifyChanges);
        }

        @Override
        public int getSlotLimit(int slot) {
            GTNAPatternBufferSlotConfig config = machine.getSelectedConfig();
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
            GTNAPatternBufferSlotConfig config = machine.getSelectedConfig();
            return config == null ? 9 : config.getCatalystItems().getSlots();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            GTNAPatternBufferSlotConfig config = machine.getSelectedConfig();
            return config == null ? ItemStack.EMPTY : config.getCatalystItems().getStackInSlot(slot);
        }

        @Override
        public void setStackInSlot(int slot, ItemStack stack) {
            GTNAPatternBufferSlotConfig config = machine.getSelectedConfig();
            if (config != null) {
                config.getCatalystItems().setStackInSlot(slot, stack);
            }
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate, boolean notifyChanges) {
            GTNAPatternBufferSlotConfig config = machine.getSelectedConfig();
            if (config == null) {
                return stack;
            }
            return config.getCatalystItems().insertItem(slot, stack, simulate, notifyChanges);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate, boolean notifyChanges) {
            GTNAPatternBufferSlotConfig config = machine.getSelectedConfig();
            if (config == null) {
                return ItemStack.EMPTY;
            }
            return config.getCatalystItems().extractItem(slot, amount, simulate, notifyChanges);
        }

        @Override
        public int getSlotLimit(int slot) {
            GTNAPatternBufferSlotConfig config = machine.getSelectedConfig();
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
            GTNAPatternBufferSlotConfig config = machine.getSelectedConfig();
            return config == null ? com.lowdragmc.lowdraglib.side.fluid.FluidStack.empty() :
                    config.getCatalystFluids()[slot].getFluid();
        }

        @Override
        public void setFluid(com.lowdragmc.lowdraglib.side.fluid.FluidStack fluid) {
            GTNAPatternBufferSlotConfig config = machine.getSelectedConfig();
            if (config != null) {
                config.getCatalystFluids()[slot].setFluid(fluid);
            }
        }

        @Override
        public long getCapacity() {
            GTNAPatternBufferSlotConfig config = machine.getSelectedConfig();
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
            GTNAPatternBufferSlotConfig config = machine.getSelectedConfig();
            return config == null ? com.lowdragmc.lowdraglib.side.fluid.FluidStack.empty() :
                    config.getSpecialFluids()[slot].getFluid();
        }

        @Override
        public void setFluid(com.lowdragmc.lowdraglib.side.fluid.FluidStack fluid) {
            GTNAPatternBufferSlotConfig config = machine.getSelectedConfig();
            if (config != null) {
                config.getSpecialFluids()[slot].setFluid(fluid);
            }
        }

        @Override
        public long getCapacity() {
            GTNAPatternBufferSlotConfig config = machine.getSelectedConfig();
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
}
