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
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
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
import java.util.Objects;

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
 *
 * <p>
 * The page is two columns (see {@link PatternBufferLayout}): patterns on the left, the selected
 * slot's configuration docked on the right. The panel used to be swapped on top of the pattern grid
 * at the same size, which drew most of its widgets outside the page; the footer actions that made
 * it overflow (recipe-cache maintenance and embedded-circuit tooling) now live in the Buffer Tools
 * side tab, {@link PatternBufferToolsConfigurator}.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
final class PatternBufferUI {

    private final GTNAMEPatternBufferPartMachine machine;

    private WidgetGroup configContent;
    private LabelWidget configHint;
    private ButtonWidget modeSelectorButton;
    private final ItemStackTransfer circuitPreviewInventory = new ItemStackTransfer(1);

    PatternBufferUI(GTNAMEPatternBufferPartMachine machine) {
        this.machine = machine;
    }

    WidgetGroup createUIWidget() {
        WidgetGroup group = new WidgetGroup(0, 0, PatternBufferLayout.PAGE_WIDTH, PatternBufferLayout.PAGE_HEIGHT);
        group.setBackground(GuiTextures.BACKGROUND);

        addPatternColumn(group);
        addConfigPanel(group);

        applySelectionState(machine.getSelectedSlot() >= 0);
        return group;
    }

    // ------------------------------------------------------------------------------------------
    // Left column: pattern grid
    // ------------------------------------------------------------------------------------------

    private void addPatternColumn(WidgetGroup group) {
        WidgetGroup patternColumn = new WidgetGroup(PatternBufferLayout.PATTERN_COLUMN_X, 0,
                PatternBufferLayout.PATTERN_COLUMN_WIDTH, PatternBufferLayout.PAGE_HEIGHT);
        group.addWidget(patternColumn);

        patternColumn.addWidget(new LabelWidget(PatternBufferLayout.PATTERN_GRID_X,
                PatternBufferLayout.PATTERN_HEADER_Y,
                () -> machine.isOnlineForUi() ? "gtceu.gui.me_network.online" : "gtceu.gui.me_network.offline"));
        patternColumn.addWidget(new AETextInputButtonWidget(PatternBufferLayout.RENAME_FIELD_X,
                PatternBufferLayout.RENAME_FIELD_Y, PatternBufferLayout.RENAME_FIELD_WIDTH,
                PatternBufferLayout.RENAME_FIELD_HEIGHT)
                .setText(machine.getCustomName())
                .setOnConfirm(machine::setCustomName)
                .setButtonTooltips(Component.translatable("gui.gtceu.rename.desc")));

        int pageCount = getPageCount();
        machine.setCurrentPage(Math.max(0, Math.min(machine.getCurrentPage(), pageCount - 1)));
        int firstSlot = machine.getCurrentPage() * PatternBufferLayout.PATTERNS_PER_PAGE;
        int maxPatternCount = machine.getMaxPatternCount();
        int rows = getVisibleRows(firstSlot, maxPatternCount);
        int index = firstSlot;
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < PatternBufferLayout.PATTERN_COLUMNS && index < maxPatternCount &&
                    index < firstSlot + PatternBufferLayout.PATTERNS_PER_PAGE; x++) {
                int finalIndex = index;
                PatternSlotWidget slotWidget = new PatternSlotWidget(machine.getPatternInventory(), index++,
                        PatternBufferLayout.PATTERN_GRID_X + x * PatternBufferLayout.PATTERN_CELL,
                        PatternBufferLayout.PATTERN_GRID_Y + y * PatternBufferLayout.PATTERN_CELL, finalIndex);
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
                patternColumn.addWidget(slotWidget);
            }
        }

        // Footer: navigation stays anchored to the bottom so a partially filled buffer does not
        // leave the controls floating in the middle of the page.
        patternColumn.addWidget(new ButtonWidget(PatternBufferLayout.NAV_LEFT_X, PatternBufferLayout.NAV_ROW_Y,
                PatternBufferLayout.NAV_BUTTON_WIDTH, PatternBufferLayout.NAV_BUTTON_HEIGHT,
                new GuiTextureGroup(GuiTextures.BUTTON, new TextTexture("<<")), clickData -> {
                    if (!clickData.isRemote && machine.getCurrentPage() > 0) machine.setCurrentPage(
                            machine.getCurrentPage() - 1);
                }).setHoverTooltips(Component.translatable("gtna.machine.pattern_buffer.previous_page")));
        patternColumn.addWidget(new LabelWidget(67, PatternBufferLayout.NAV_ROW_Y + 2,
                () -> (machine.getCurrentPage() + 1) + " / " + getPageCount()));
        patternColumn.addWidget(new ButtonWidget(PatternBufferLayout.NAV_RIGHT_X, PatternBufferLayout.NAV_ROW_Y,
                PatternBufferLayout.NAV_BUTTON_WIDTH, PatternBufferLayout.NAV_BUTTON_HEIGHT,
                new GuiTextureGroup(GuiTextures.BUTTON, new TextTexture(">>")), clickData -> {
                    if (!clickData.isRemote && machine.getCurrentPage() + 1 < getPageCount()) machine.setCurrentPage(
                            machine.getCurrentPage() + 1);
                }).setHoverTooltips(Component.translatable("gtna.machine.pattern_buffer.next_page")));
        patternColumn.addWidget(new LabelWidget(PatternBufferLayout.PATTERN_GRID_X,
                PatternBufferLayout.PAGE_HINT_Y,
                () -> Component.translatable("gtna.machine.pattern_buffer.middle_click_hint").getString()));
    }

    private int getPageCount() {
        return Math.max(1, (int) Math.ceil(machine.getMaxPatternCount() /
                (double) PatternBufferLayout.PATTERNS_PER_PAGE));
    }

    private int getVisibleRows(int firstSlot, int maxPatternCount) {
        int remaining = Math.max(0, maxPatternCount - firstSlot);
        int rows = (int) Math.ceil(remaining / (double) PatternBufferLayout.PATTERN_COLUMNS);
        return Math.max(1, Math.min(PatternBufferLayout.PATTERN_ROWS, rows));
    }

    // ------------------------------------------------------------------------------------------
    // Right column: per-slot configuration, docked (never overlapping the grid)
    // ------------------------------------------------------------------------------------------

    private void addConfigPanel(WidgetGroup group) {
        WidgetGroup configPanel = new WidgetGroup(PatternBufferLayout.CONFIG_COLUMN_X, 0,
                PatternBufferLayout.CONFIG_COLUMN_WIDTH, PatternBufferLayout.PAGE_HEIGHT);
        configPanel.setBackground(GuiTextures.BACKGROUND_INVERSE);
        group.addWidget(configPanel);

        configHint = new LabelWidget(PatternBufferLayout.CONFIG_INNER_X, PatternBufferLayout.PAGE_HEIGHT / 2 - 4,
                () -> Component.translatable("gtna.machine.pattern_buffer.select_slot_hint").getString());
        configPanel.addWidget(configHint);

        configContent = new WidgetGroup(0, 0, PatternBufferLayout.CONFIG_COLUMN_WIDTH,
                PatternBufferLayout.PAGE_HEIGHT);
        configPanel.addWidget(configContent);

        int x = PatternBufferLayout.CONFIG_INNER_X;
        int wide = PatternBufferLayout.CONFIG_INNER_WIDTH;

        configContent.addWidget(new ButtonWidget(x, PatternBufferLayout.HEADER_Y, 18,
                PatternBufferLayout.HEADER_HEIGHT,
                new GuiTextureGroup(GuiTextures.BUTTON, new TextTexture("<")), clickData -> {
                    if (!clickData.isRemote) selectSlot(-1);
                }).setHoverTooltips(Component.translatable("gtna.machine.pattern_buffer.back")));
        configContent.addWidget(new LabelWidget(x + 24, PatternBufferLayout.HEADER_Y + 2,
                () -> machine.getSelectedSlot() >= 0 ?
                        Component.translatable("gtna.machine.pattern_buffer.selected_slot",
                                machine.getSelectedSlot() + 1).getString() :
                        Component.translatable("gtna.machine.pattern_buffer.no_slot_selected").getString()));
        // Two short diagnostic lines. Both values are pretty-printed and truncated to the panel's
        // text budget: the raw registry ids overflow a 164 px column. The constant is the rough
        // character width of the default font (6 px) minus room for the "Recipe:" / "Mode:" prefix.
        int textBudget = Math.max(8, wide / 6 - 9);
        configContent.addWidget(new LabelWidget(x, PatternBufferLayout.CACHED_LABEL_Y,
                () -> Component.translatable("gtna.machine.pattern_buffer.cached_recipe_short",
                        PatternBufferModeRegistry.compactDisplay(prettyMode(
                                machine.getSelectedConfig() == null ? "" :
                                        machine.getSelectedConfig().getCachedRecipeId()),
                                textBudget))
                        .getString()));
        configContent.addWidget(new LabelWidget(x, PatternBufferLayout.DERIVED_LABEL_Y,
                () -> Component.translatable("gtna.machine.pattern_buffer.derived_mode_short",
                        PatternBufferModeRegistry.compactDisplay(prettyMode(
                                machine.getSelectedConfig() == null ? "" :
                                        machine.getSelectedConfig().getDerivedModeId()),
                                textBudget))
                        .getString()));

        configContent.addWidget(new LabelWidget(x, PatternBufferLayout.ITEM_LABEL_Y,
                () -> Component.translatable("gtna.machine.pattern_buffer.item_field").getString()));
        addItemGhostRow(configContent, x, PatternBufferLayout.ITEM_ROW_Y);

        configContent.addWidget(new LabelWidget(x, PatternBufferLayout.FLUID_LABEL_Y,
                () -> Component.translatable("gtna.machine.pattern_buffer.fluid_field").getString()));
        addFluidGhostRow(configContent, x, PatternBufferLayout.FLUID_ROW_Y);

        configContent.addWidget(new LabelWidget(x, PatternBufferLayout.CATALYST_ITEM_LABEL_Y,
                () -> Component.translatable("gtna.machine.pattern_buffer.catalyst_item_field").getString()));
        addCatalystItemGhostRow(configContent, x, PatternBufferLayout.CATALYST_ITEM_ROW_Y);

        configContent.addWidget(new LabelWidget(x, PatternBufferLayout.CATALYST_FLUID_LABEL_Y,
                () -> Component.translatable("gtna.machine.pattern_buffer.catalyst_fluid_field").getString()));
        addCatalystFluidGhostRow(configContent, x, PatternBufferLayout.CATALYST_FLUID_ROW_Y);

        configContent.addWidget(new LabelWidget(x, PatternBufferLayout.CIRCUIT_LABEL_Y,
                () -> Component.translatable("gtna.machine.pattern_buffer.circuit_field").getString()));
        configContent.addWidget(new IntInputWidget(x, PatternBufferLayout.CIRCUIT_ROW_Y, 50, 14,
                () -> machine.getSelectedConfig() == null ? -1 : machine.getSelectedConfig().getCircuitConfig(),
                value -> {
                    GTNAPatternBufferSlotConfig config = machine.getSelectedConfig();
                    if (config != null) {
                        config.setCircuitConfig(value);
                    }
                }).setMin(-1).setMax(32));
        configContent.addWidget(new SlotWidget(circuitPreviewInventory, 0, x + 58,
                PatternBufferLayout.CIRCUIT_ROW_Y - 2, false, false)
                .setCanPutItems(false)
                .setCanTakeItems(false)
                .setBackgroundTexture(new GuiTextureGroup(GuiTextures.SLOT, GuiTextures.INT_CIRCUIT_OVERLAY))
                .setOnAddedTooltips((widget, tooltips) -> {
                    if (circuitPreviewInventory.getStackInSlot(0).isEmpty()) {
                        tooltips.add(Component.translatable("gtna.machine.pattern_buffer.no_circuit"));
                    }
                }));

        configContent.addWidget(new LabelWidget(x, PatternBufferLayout.MODE_LABEL_Y,
                () -> Component.translatable("gtna.machine.pattern_buffer.mode_field").getString()));
        modeSelectorButton = new ButtonWidget(x, PatternBufferLayout.MODE_BUTTON_Y, wide,
                PatternBufferLayout.BUTTON_HEIGHT,
                new GuiTextureGroup(
                        GuiTextures.BUTTON,
                        new TextTexture(this::getSelectedModeButtonText)
                                .setWidth(wide - 6)
                                .setType(TextTexture.TextType.ROLL)
                                .setDropShadow(false)),
                clickData -> {
                    if (!clickData.isRemote) {
                        cycleSelectedMode();
                    }
                });
        modeSelectorButton.setHoverTooltips(Component.translatable("gtna.machine.pattern_buffer.mode_button.tooltip"));
        configContent.addWidget(modeSelectorButton);

        // Per-slot recipe caching toggle (GTLCore cacheRecipe[] parity). The buffer-wide cache
        // maintenance and the embedded-circuit tooling moved to the Buffer Tools tab.
        configContent.addWidget(new ButtonWidget(x, PatternBufferLayout.CACHE_TOGGLE_Y, wide,
                PatternBufferLayout.BUTTON_HEIGHT,
                new GuiTextureGroup(
                        GuiTextures.BUTTON,
                        new TextTexture(this::getCacheToggleText)
                                .setWidth(wide - 6)
                                .setType(TextTexture.TextType.ROLL)
                                .setDropShadow(false)),
                clickData -> {
                    if (!clickData.isRemote) machine.toggleSelectedCacheRecipe();
                }).setHoverTooltips(Component.translatable("gtna.machine.pattern_buffer.cache_toggle.tooltip")));
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
            panel.addWidget(new PhantomSlotWidget(new SelectedConfigItemTransfer(), logicalSlot,
                    x + slot * PatternBufferLayout.GHOST_SLOT, y)
                    .setClearSlotOnRightClick(true)
                    .setChangeListener(this::onSelectedConfigWidgetChanged)
                    .setBackgroundTexture(new GuiTextureGroup(GuiTextures.SLOT, GuiTextures.FILTER_SLOT_OVERLAY)));
        }
    }

    private void addFluidGhostRow(WidgetGroup panel, int x, int y) {
        for (int slot = 0; slot < 9; slot++) {
            FluidStorageProxy storage = new FluidStorageProxy(slot);
            panel.addWidget(new PhantomTankWidget(storage, x + slot * PatternBufferLayout.GHOST_SLOT, y,
                    PatternBufferLayout.GHOST_SLOT, PatternBufferLayout.GHOST_SLOT)
                    .setAllowClickFilled(true)
                    .setAllowClickDrained(true)
                    .setBackground(GuiTextures.FLUID_SLOT)
                    .setChangeListener(this::onSelectedConfigWidgetChanged));
        }
    }

    private void addCatalystItemGhostRow(WidgetGroup panel, int x, int y) {
        for (int slot = 0; slot < 9; slot++) {
            int logicalSlot = slot;
            panel.addWidget(
                    new PhantomSlotWidget(new SelectedConfigCatalystItemTransfer(), logicalSlot,
                            x + slot * PatternBufferLayout.GHOST_SLOT, y)
                            .setClearSlotOnRightClick(true)
                            .setChangeListener(this::onSelectedConfigWidgetChanged)
                            .setBackgroundTexture(
                                    new GuiTextureGroup(GuiTextures.SLOT, GuiTextures.FILTER_SLOT_OVERLAY)));
        }
    }

    private void addCatalystFluidGhostRow(WidgetGroup panel, int x, int y) {
        for (int slot = 0; slot < 9; slot++) {
            CatalystFluidStorageProxy storage = new CatalystFluidStorageProxy(slot);
            panel.addWidget(new PhantomTankWidget(storage, x + slot * PatternBufferLayout.GHOST_SLOT, y,
                    PatternBufferLayout.GHOST_SLOT, PatternBufferLayout.GHOST_SLOT)
                    .setAllowClickFilled(true)
                    .setAllowClickDrained(true)
                    .setBackground(GuiTextures.FLUID_SLOT)
                    .setChangeListener(this::onSelectedConfigWidgetChanged));
        }
    }

    private void selectSlot(int slot) {
        int previous = machine.getSelectedSlot();
        if (slot >= 0 && slot < machine.getMaxPatternCount() && previous == slot) {
            machine.setSelectedSlot(-1);
        } else {
            machine.setSelectedSlot(slot >= 0 && slot < machine.getMaxPatternCount() ? slot : -1);
        }
        applySelectionState(machine.getSelectedSlot() >= 0);
        refreshSelectedConfigPreview();
    }

    /**
     * Shows either the configuration widgets or the "pick a slot" hint. Both are children of the
     * docked panel, so hiding one keeps the page size fixed instead of reflowing the frame.
     */
    private void applySelectionState(boolean selected) {
        if (configContent != null) {
            configContent.setVisible(selected);
            configContent.setActive(selected);
        }
        if (configHint != null) {
            configHint.setVisible(!selected);
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
            if (Objects.equals(options.get(i).id(), current)) {
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

    /** Pretty-prints a registry id for the compact diagnostic labels, keeping blanks blank. */
    private static String prettyMode(String modeId) {
        if (modeId == null || modeId.isBlank()) {
            return "";
        }
        return PatternBufferModeRegistry.formatModeLabel(modeId);
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
