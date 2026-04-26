package com.raishxn.gtna.common.item.terminal.ui;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.common.block.CoilBlock;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMachines;

import com.lowdragmc.lowdraglib.gui.texture.ColorBorderTexture;
import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import com.raishxn.gtna.common.data.GTNABlocks;
import com.raishxn.gtna.common.data.GTNAMachines2;

import java.util.*;

/**
 * Creates the "Block Configuration" tab for the Nexus Structure Terminal.
 * Displays categories (Coils, Machine Casings, Muffler, Rotor Holder,
 * Wireless Capacitor) as a scrollable grid of item icons with clear
 * selection indicators using purple accent borders.
 */
public class BlockSelectionConfigWidget {

    // ─── Theme Colors (matching NexusTerminalUIFactory) ─────────────────────
    private static final int COLOR_BG_DARK = 0xFF1A1A1A;
    private static final int COLOR_BG_MEDIUM = 0xFF2B2B2B;
    private static final int COLOR_BG_LIGHT = 0xFF3A3A3A;
    private static final int COLOR_BORDER_DARK = 0xFF0A0A0A;
    private static final int COLOR_BORDER_LIGHT = 0xFF7B4FBF;
    private static final int COLOR_ACCENT = 0xFF9B6FDF;
    private static final int COLOR_SELECTED = 0xFFAA66FF;  // bright purple for selected
    private static final int COLOR_HOVER = 0x40FFFFFF;
    private static final int COLOR_TEXT_WHITE = 0xFFFFFFFF;
    private static final int COLOR_TEXT_GRAY = 0xFFAAAAAA;
    private static final int COLOR_CATEGORY_BG = 0xFF222222;
    private static final int COLOR_SEPARATOR = 0xFF3A3A3A;
    private static final int COLOR_SLOT_BG = 0xFF1E1E1E;
    private static final int COLOR_SLOT_BORDER = 0xFF333333;

    // ─── Category definitions ───────────────────────────────────────────────
    public enum BlockCategory {

        COILS("gtna.terminal.config.coils", "SelectedCoil"),
        MACHINE_CASING("gtna.terminal.config.machine_casing", "SelectedCasing"),
        MUFFLER("gtna.terminal.config.muffler", "SelectedMuffler"),
        ROTOR_HOLDER("gtna.terminal.config.rotor_holder", "SelectedRotor"),
        WIRELESS_CAPACITOR("gtna.terminal.config.wireless_capacitor", "SelectedCapacitor"),
        MATRIX_STORAGE_MODULE("gtna.terminal.config.matrix_storage_module", "SelectedMatrixStorageModule"),
        MATRIX_CRAFTING_MODULE("gtna.terminal.config.matrix_crafting_module", "SelectedMatrixCraftingModule"),
        ME_STORAGE_ACCESS("gtna.terminal.config.me_storage_access", "SelectedMEStorageAccess");

        public final String translationKey;
        public final String nbtKey;

        BlockCategory(String translationKey, String nbtKey) {
            this.translationKey = translationKey;
            this.nbtKey = nbtKey;
        }
    }

    private final ItemStack terminalStack;
    private String blueprintPrefix = "";

    public BlockSelectionConfigWidget(ItemStack terminalStack) {
        this.terminalStack = terminalStack;
    }

    public void setBlueprintName(String name) {
        this.blueprintPrefix = name != null && !name.isEmpty() ? name + "_" : "";
    }

    /**
     * Build the full configuration panel positioned at the given coords.
     * Called from NexusTerminalUIFactory as a second tab.
     */
    public WidgetGroup createConfigPanel(int x, int y, int width, int height) {
        WidgetGroup panel = new WidgetGroup(x, y, width, height);
        panel.setBackground(new GuiTextureGroup(
                new ColorRectTexture(COLOR_BG_MEDIUM),
                new ColorBorderTexture(1, COLOR_BORDER_DARK)));

        DraggableScrollableWidgetGroup scroll = new DraggableScrollableWidgetGroup(
                2, 2, width - 4, height - 4);
        scroll.setYScrollBarWidth(6);
        scroll.setYBarStyle(
                new ColorRectTexture(COLOR_BORDER_DARK),
                new ColorRectTexture(COLOR_BORDER_LIGHT));

        int scrollY = 4;

        // ── Category: Coils ───────────────────────────────────────────────────
        scrollY = addCategory(scroll, scrollY, width - 14,
                BlockCategory.COILS, getCoilEntries());

        // ── Category: Machine Casings ─────────────────────────────────────────
        scrollY = addCategory(scroll, scrollY, width - 14,
                BlockCategory.MACHINE_CASING, getMachineCasingEntries());

        // ── Category: Mufflers ────────────────────────────────────────────────
        scrollY = addCategory(scroll, scrollY, width - 14,
                BlockCategory.MUFFLER, getMufflerEntries());

        // ── Category: Rotor Holders ───────────────────────────────────────────
        scrollY = addCategory(scroll, scrollY, width - 14,
                BlockCategory.ROTOR_HOLDER, getRotorHolderEntries());

        // ── Category: Wireless Capacitors ─────────────────────────────────────
        scrollY = addCategory(scroll, scrollY, width - 14,
                BlockCategory.WIRELESS_CAPACITOR, getWirelessCapacitorEntries());

        scrollY = addCategory(scroll, scrollY, width - 14,
                BlockCategory.MATRIX_STORAGE_MODULE, getMatrixStorageModuleEntries());

        scrollY = addCategory(scroll, scrollY, width - 14,
                BlockCategory.MATRIX_CRAFTING_MODULE, getMatrixCraftingModuleEntries());

        scrollY = addCategory(scroll, scrollY, width - 14,
                BlockCategory.ME_STORAGE_ACCESS, getMEStorageAccessEntries());

        panel.addWidget(scroll);
        return panel;
    }

    /**
     * Legacy method for backward compat — creates panel at default position.
     */
    public WidgetGroup createConfigPanel() {
        return createConfigPanel(0, 0, 160, 200);
    }

    /**
     * Add a category section: accent header bar + grid of item icons.
     *
     * @return the new y offset after this category
     */
    private int addCategory(DraggableScrollableWidgetGroup scroll, int startY,
                            int availableWidth, BlockCategory category, List<ItemStack> entries) {
        int y = startY;

        // Category header bar with accent underline
        WidgetGroup headerGroup = new WidgetGroup(0, y, availableWidth, 16);
        headerGroup.setBackground(new ColorRectTexture(COLOR_CATEGORY_BG));
        scroll.addWidget(headerGroup);

        LabelWidget catLabel = new LabelWidget(6, 4,
                Component.translatable(category.translationKey).getString());
        catLabel.setTextColor(COLOR_ACCENT);
        headerGroup.addWidget(catLabel);

        // Selected indicator in header
        LabelWidget selIndicator = new LabelWidget(availableWidth - 60, 4,
                () -> {
                    int sel = getSelectedIndex(category);
                    return sel >= 0 ? "§d✓ " + (sel + 1) : "§8—";
                });
        selIndicator.setTextColor(COLOR_TEXT_GRAY);
        headerGroup.addWidget(selIndicator);

        y += 18;

        // Accent underline
        scroll.addWidget(new ImageWidget(0, y, availableWidth, 1,
                new ColorRectTexture(COLOR_BORDER_LIGHT)));
        y += 3;

        if (entries.isEmpty()) {
            LabelWidget empty = new LabelWidget(6, y, "§7No items available");
            empty.setTextColor(COLOR_TEXT_GRAY);
            scroll.addWidget(empty);
            y += 16;
            return y + 6;
        }

        // Grid of block icons with selection
        int cols = 7;
        int cellSize = 22;
        int padding = 2;
        int gridOffsetX = 4;

        for (int i = 0; i < entries.size(); i++) {
            int col = i % cols;
            int row = i / cols;
            int cellX = gridOffsetX + col * (cellSize + padding);
            int cellY = y + row * (cellSize + padding);

            ItemStack stack = entries.get(i);
            final int index = i;

            boolean isSelected = getSelectedIndex(category) == index;

            // Slot background — different for selected vs unselected
            if (isSelected) {
                // Selected: bright purple border
                scroll.addWidget(new ImageWidget(cellX, cellY, cellSize, cellSize,
                        new GuiTextureGroup(
                                new ColorRectTexture(COLOR_SLOT_BG),
                                new ColorBorderTexture(2, COLOR_SELECTED))));
            } else {
                // Normal: subtle dark border
                scroll.addWidget(new ImageWidget(cellX, cellY, cellSize, cellSize,
                        new GuiTextureGroup(
                                new ColorRectTexture(COLOR_SLOT_BG),
                                new ColorBorderTexture(1, COLOR_SLOT_BORDER))));
            }

            // Item icon (centered in cell)
            scroll.addWidget(new ImageWidget(cellX + 3, cellY + 3, cellSize - 6, cellSize - 6,
                    new ItemStackTexture(stack)));

            // Clickable overlay — handles selection toggle
            ButtonWidget clickTarget = new ButtonWidget(cellX, cellY, cellSize, cellSize,
                    new ColorRectTexture(0x00000000),
                    cd -> setSelectedIndex(category, index));
            clickTarget.setHoverTexture(new ColorRectTexture(COLOR_HOVER));
            clickTarget.setHoverTooltips(Component.translatable(stack.getDescriptionId()));
            scroll.addWidget(clickTarget);
        }

        int rowCount = (entries.size() + cols - 1) / cols;
        y += rowCount * (cellSize + padding) + 6;

        // Category separator
        scroll.addWidget(new ImageWidget(6, y, availableWidth - 12, 1,
                new ColorRectTexture(COLOR_SEPARATOR)));
        y += 6;

        return y;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // CATEGORY → ITEM LIST BUILDERS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Get all registered heating coils, sorted by tier ascending.
     */
    public static List<ItemStack> getCoilEntries() {
        List<ItemStack> stacks = new ArrayList<>();
        GTCEuAPI.HEATING_COILS.entrySet().stream()
                .sorted(Comparator.comparingInt(entry -> entry.getKey().getTier()))
                .forEach(entry -> {
                    CoilBlock block = entry.getValue().get();
                    stacks.add(new ItemStack(block));
                });
        return stacks;
    }

    /**
     * Get machine casings ULV → MAX (15 tiers).
     */
    @SuppressWarnings("deprecation")
    public static List<ItemStack> getMachineCasingEntries() {
        List<ItemStack> stacks = new ArrayList<>();
        try {
            var fields = new Object[] {
                    GTBlocks.MACHINE_CASING_ULV, GTBlocks.MACHINE_CASING_LV,
                    GTBlocks.MACHINE_CASING_MV, GTBlocks.MACHINE_CASING_HV,
                    GTBlocks.MACHINE_CASING_EV, GTBlocks.MACHINE_CASING_IV,
                    GTBlocks.MACHINE_CASING_LuV, GTBlocks.MACHINE_CASING_ZPM,
                    GTBlocks.MACHINE_CASING_UV, GTBlocks.MACHINE_CASING_UHV,
                    GTBlocks.MACHINE_CASING_UEV, GTBlocks.MACHINE_CASING_UIV,
                    GTBlocks.MACHINE_CASING_UXV, GTBlocks.MACHINE_CASING_OpV,
                    GTBlocks.MACHINE_CASING_MAX
            };
            for (Object entry : fields) {
                if (entry instanceof com.tterrag.registrate.util.entry.BlockEntry<?> blockEntry) {
                    stacks.add(new ItemStack(blockEntry.get()));
                }
            }
        } catch (Exception ignored) {}
        return stacks;
    }

    /**
     * Get muffler hatches from GTMachines (tiered array).
     */
    public static List<ItemStack> getMufflerEntries() {
        List<ItemStack> stacks = new ArrayList<>();
        try {
            if (GTMachines.MUFFLER_HATCH != null) {
                for (var def : GTMachines.MUFFLER_HATCH) {
                    if (def != null) {
                        stacks.add(def.asStack());
                    }
                }
            }
        } catch (Exception ignored) {}
        return stacks;
    }

    /**
     * Get rotor holders from GTMachines (tiered array).
     */
    public static List<ItemStack> getRotorHolderEntries() {
        List<ItemStack> stacks = new ArrayList<>();
        try {
            if (GTMachines.ROTOR_HOLDER != null) {
                for (var def : GTMachines.ROTOR_HOLDER) {
                    if (def != null) {
                        stacks.add(def.asStack());
                    }
                }
            }
        } catch (Exception ignored) {}
        return stacks;
    }

    /**
     * Get all GTNA Nexus Capacitor blocks (LV through MAX).
     */
    public static List<ItemStack> getWirelessCapacitorEntries() {
        List<ItemStack> stacks = new ArrayList<>();
        try {
            var capacitors = new Object[] {
                    GTNABlocks.NEXUS_CAPACITOR_LV, GTNABlocks.NEXUS_CAPACITOR_MV,
                    GTNABlocks.NEXUS_CAPACITOR_HV, GTNABlocks.NEXUS_CAPACITOR_EV,
                    GTNABlocks.NEXUS_CAPACITOR_IV, GTNABlocks.NEXUS_CAPACITOR_LUV,
                    GTNABlocks.NEXUS_CAPACITOR_ZPM, GTNABlocks.NEXUS_CAPACITOR_UV,
                    GTNABlocks.NEXUS_CAPACITOR_UHV, GTNABlocks.NEXUS_CAPACITOR_UEV,
                    GTNABlocks.NEXUS_CAPACITOR_UIV, GTNABlocks.NEXUS_CAPACITOR_UXV,
                    GTNABlocks.NEXUS_CAPACITOR_OPV, GTNABlocks.NEXUS_CAPACITOR_MAX
            };
            for (Object cap : capacitors) {
                if (cap instanceof com.tterrag.registrate.util.entry.BlockEntry<?> blockEntry) {
                    stacks.add(new ItemStack(blockEntry.get()));
                }
            }
        } catch (Exception ignored) {}
        return stacks;
    }

    public static List<ItemStack> getMatrixStorageModuleEntries() {
        return blockEntries(
                GTNABlocks.T1_ME_STORAGE_CORE,
                GTNABlocks.T2_ME_STORAGE_CORE,
                GTNABlocks.T3_ME_STORAGE_CORE,
                GTNABlocks.T4_ME_STORAGE_CORE,
                GTNABlocks.T5_ME_STORAGE_CORE);
    }

    public static List<ItemStack> getMatrixCraftingModuleEntries() {
        return blockEntries(
                GTNABlocks.T1_CRAFTING_STORAGE_CORE,
                GTNABlocks.T2_CRAFTING_STORAGE_CORE,
                GTNABlocks.T3_CRAFTING_STORAGE_CORE,
                GTNABlocks.T4_CRAFTING_STORAGE_CORE,
                GTNABlocks.T5_CRAFTING_STORAGE_CORE);
    }

    public static List<ItemStack> getMEStorageAccessEntries() {
        List<ItemStack> stacks = new ArrayList<>();
        if (GTNAMachines2.ME_STORAGE_ACCESS_HATCH != null) {
            stacks.add(GTNAMachines2.ME_STORAGE_ACCESS_HATCH.asStack());
        }
        if (GTNAMachines2.ME_BIG_STORAGE_ACCESS_HATCH != null) {
            stacks.add(GTNAMachines2.ME_BIG_STORAGE_ACCESS_HATCH.asStack());
        }
        if (GTNAMachines2.ME_IO_PORT_HATCH != null) {
            stacks.add(GTNAMachines2.ME_IO_PORT_HATCH.asStack());
        }
        return stacks;
    }

    private static List<ItemStack> blockEntries(com.tterrag.registrate.util.entry.BlockEntry<?>... entries) {
        List<ItemStack> stacks = new ArrayList<>();
        for (var entry : entries) {
            if (entry != null) {
                stacks.add(new ItemStack(entry.get()));
            }
        }
        return stacks;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // NBT HELPERS
    // ═══════════════════════════════════════════════════════════════════════════

    private int getSelectedIndex(BlockCategory category) {
        CompoundTag tag = terminalStack.getTag();
        String key = blueprintPrefix + category.nbtKey;
        if (tag != null && tag.contains(key)) {
            return tag.getInt(key);
        }
        return -1; // -1 = no selection (use default)
    }

    private void setSelectedIndex(BlockCategory category, int index) {
        CompoundTag tag = terminalStack.getOrCreateTag();
        String key = blueprintPrefix + category.nbtKey;
        // If already selected, deselect (toggle behavior)
        if (tag.getInt(key) == index && tag.contains(key)) {
            tag.putInt(key, -1);
        } else {
            tag.putInt(key, index);
        }
    }

    /**
     * Get the selected ItemStack for a category, or null if none selected.
     * Uses no blueprint prefix (for GTCEu Terminal).
     */
    public static ItemStack getSelectedBlock(ItemStack terminalStack, BlockCategory category) {
        return getSelectedBlock(terminalStack, category, "");
    }

    /**
     * Get the selected ItemStack for a category, or null if none selected, using a blueprint prefix.
     */
    public static ItemStack getSelectedBlock(ItemStack terminalStack, BlockCategory category, String blueprintName) {
        CompoundTag tag = terminalStack.getTag();
        String prefix = blueprintName != null && !blueprintName.isEmpty() ? blueprintName + "_" : "";
        if (tag == null || !tag.contains(prefix + category.nbtKey)) return null;
        int idx = tag.getInt(prefix + category.nbtKey);
        if (idx < 0) return null;

        List<ItemStack> entries = switch (category) {
            case COILS -> getCoilEntries();
            case MACHINE_CASING -> getMachineCasingEntries();
            case MUFFLER -> getMufflerEntries();
            case ROTOR_HOLDER -> getRotorHolderEntries();
            case WIRELESS_CAPACITOR -> getWirelessCapacitorEntries();
            case MATRIX_STORAGE_MODULE -> getMatrixStorageModuleEntries();
            case MATRIX_CRAFTING_MODULE -> getMatrixCraftingModuleEntries();
            case ME_STORAGE_ACCESS -> getMEStorageAccessEntries();
        };

        if (idx < entries.size()) {
            return entries.get(idx);
        }
        return null;
    }
}
