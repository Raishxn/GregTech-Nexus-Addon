package com.raishxn.gtna.common.item.terminal.ui;

import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.ColorBorderTexture;
import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.Size;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import lombok.Getter;
import lombok.Setter;

/**
 * Builds the ModularUI for the Nexus Structure Terminal settings screen.
 * Design inspired by GTCEu-Terminals ManagerSettingsUI with a dark theme
 * and purple accent. Two tabs: Settings and Block Configuration.
 */
public class NexusTerminalUIFactory {

    // ─── Dimensions ────────────────────────────────────────────────────────────
    private static final int GUI_WIDTH = 220;
    private static final int GUI_HEIGHT = 230;

    // ─── Dark Theme — Purple Accent ────────────────────────────────────────────
    private static final int COLOR_BG_DARK = 0xFF1A1A1A;
    private static final int COLOR_BG_MEDIUM = 0xFF2B2B2B;
    private static final int COLOR_BG_LIGHT = 0xFF3A3A3A;
    private static final int COLOR_BORDER_LIGHT = 0xFF7B4FBF;  // purple accent
    private static final int COLOR_BORDER_DARK = 0xFF0A0A0A;
    private static final int COLOR_ACCENT = 0xFF9B6FDF;  // lighter purple
    private static final int COLOR_ACCENT_DIM = 0xAA7B4FBF;  // semi-transparent
    private static final int COLOR_TEXT_WHITE = 0xFFFFFFFF;
    private static final int COLOR_TEXT_GRAY = 0xFFAAAAAA;
    private static final int COLOR_HINT = 0xFF666666;
    private static final int COLOR_HOVER = 0x40FFFFFF;
    private static final int COLOR_TAB_ACTIVE = 0xFF7B4FBF;
    private static final int COLOR_TAB_INACTIVE = 0xFF2B2B2B;
    private static final int COLOR_TOGGLE_ON = 0xFF2E7D32;  // green for "Yes"
    private static final int COLOR_TOGGLE_OFF = 0xFF5A2020;  // red for "No"

    private final HeldItemUIFactory.HeldItemHolder holder;
    private final Player player;
    private final ItemStack itemStack;

    // Tab state
    private boolean showBlockConfig = false;
    private WidgetGroup settingsContent;
    private WidgetGroup blockConfigContent;
    private ButtonWidget tabSettingsBtn;
    private ButtonWidget tabBlocksBtn;

    public NexusTerminalUIFactory(HeldItemUIFactory.HeldItemHolder holder, Player player) {
        this.holder = holder;
        this.player = player;
        this.itemStack = holder.getHeld();
    }

    public ModularUI createModularUI() {
        WidgetGroup mainGroup = new WidgetGroup(0, 0, GUI_WIDTH, GUI_HEIGHT);

        // ─── 3D Border decoration (light top/left, dark bottom/right) ──────
        mainGroup.addWidget(new ImageWidget(0, 0, GUI_WIDTH, 2,
                new ColorRectTexture(COLOR_BORDER_LIGHT)));
        mainGroup.addWidget(new ImageWidget(0, 0, 2, GUI_HEIGHT,
                new ColorRectTexture(COLOR_BORDER_LIGHT)));
        mainGroup.addWidget(new ImageWidget(GUI_WIDTH - 2, 0, 2, GUI_HEIGHT,
                new ColorRectTexture(COLOR_BORDER_DARK)));
        mainGroup.addWidget(new ImageWidget(0, GUI_HEIGHT - 2, GUI_WIDTH, 2,
                new ColorRectTexture(COLOR_BORDER_DARK)));

        // ─── Header ───────────────────────────────────────────────────────────
        mainGroup.addWidget(createHeader());

        // ─── Tabs ─────────────────────────────────────────────────────────────
        mainGroup.addWidget(createTabBar());

        // ─── Content panels (swapped by tab clicks) ───────────────────────────
        settingsContent = createSettingsPanel();
        blockConfigContent = createBlockConfigPanel();

        // Initial state: show settings
        blockConfigContent.setVisible(false);
        blockConfigContent.setActive(false);

        mainGroup.addWidget(settingsContent);
        mainGroup.addWidget(blockConfigContent);

        ModularUI gui = new ModularUI(new Size(GUI_WIDTH, GUI_HEIGHT), holder, player);
        gui.widget(mainGroup);
        gui.background(new ColorRectTexture(COLOR_BG_DARK));
        return gui;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // HEADER
    // ═══════════════════════════════════════════════════════════════════════════

    private WidgetGroup createHeader() {
        WidgetGroup header = new WidgetGroup(2, 2, GUI_WIDTH - 4, 22);
        header.setBackground(new ColorRectTexture(COLOR_BG_MEDIUM));

        LabelWidget title = new LabelWidget(GUI_WIDTH / 2 - 60, 7,
                Component.translatable("gtna.terminal.nexus.title").getString());
        title.setTextColor(COLOR_ACCENT);
        header.addWidget(title);

        return header;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TAB BAR
    // ═══════════════════════════════════════════════════════════════════════════

    private WidgetGroup createTabBar() {
        WidgetGroup tabBar = new WidgetGroup(2, 26, GUI_WIDTH - 4, 18);

        int halfW = (GUI_WIDTH - 4) / 2;

        // Tab: Settings
        tabSettingsBtn = new ButtonWidget(0, 0, halfW, 18,
                new ColorRectTexture(COLOR_TAB_ACTIVE),
                cd -> switchToTab(false));
        tabSettingsBtn.setHoverTexture(new ColorRectTexture(COLOR_HOVER));
        tabBar.addWidget(tabSettingsBtn);
        tabBar.addWidget(new ImageWidget(4, 4, halfW - 8, 12,
                new TextTexture("§f⚙ Settings").setWidth(halfW - 8)
                        .setType(TextTexture.TextType.NORMAL)));

        // Tab: Block Config
        tabBlocksBtn = new ButtonWidget(halfW, 0, halfW, 18,
                new ColorRectTexture(COLOR_TAB_INACTIVE),
                cd -> switchToTab(true));
        tabBlocksBtn.setHoverTexture(new ColorRectTexture(COLOR_HOVER));
        tabBar.addWidget(tabBlocksBtn);
        tabBar.addWidget(new ImageWidget(halfW + 4, 4, halfW - 8, 12,
                new TextTexture("§7⬛ Blocks").setWidth(halfW - 8)
                        .setType(TextTexture.TextType.NORMAL)));

        return tabBar;
    }

    private void switchToTab(boolean blocksTab) {
        showBlockConfig = blocksTab;

        settingsContent.setVisible(!blocksTab);
        settingsContent.setActive(!blocksTab);
        blockConfigContent.setVisible(blocksTab);
        blockConfigContent.setActive(blocksTab);

        // Update tab button colors
        tabSettingsBtn.setButtonTexture(
                new ColorRectTexture(blocksTab ? COLOR_TAB_INACTIVE : COLOR_TAB_ACTIVE));
        tabBlocksBtn.setButtonTexture(
                new ColorRectTexture(blocksTab ? COLOR_TAB_ACTIVE : COLOR_TAB_INACTIVE));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SETTINGS PANEL
    // ═══════════════════════════════════════════════════════════════════════════

    private WidgetGroup createSettingsPanel() {
        int contentY = 46;
        int contentH = GUI_HEIGHT - contentY - 4;
        WidgetGroup panel = new WidgetGroup(4, contentY, GUI_WIDTH - 8, contentH);
        panel.setBackground(new GuiTextureGroup(
                new ColorRectTexture(COLOR_BG_MEDIUM),
                new ColorBorderTexture(1, COLOR_BORDER_DARK)));

        DraggableScrollableWidgetGroup scroll = new DraggableScrollableWidgetGroup(
                2, 2, GUI_WIDTH - 12, contentH - 4);
        scroll.setYScrollBarWidth(6);
        scroll.setYBarStyle(
                new ColorRectTexture(COLOR_BORDER_DARK),
                new ColorRectTexture(COLOR_BORDER_LIGHT));

        int yPos = 6;
        int labelX = 8;
        int controlX = GUI_WIDTH - 76;
        int controlW = 50;
        int rowStep = 30;

        // ── 1. No Hatch Mode ──────────────────────────────────────────────────
        yPos = addToggleSetting(scroll, yPos, labelX, controlX, controlW,
                "gtna.terminal.nexus.no_hatch",
                "gtna.terminal.nexus.no_hatch.tooltip",
                "gtna.terminal.nexus.no_hatch.hint",
                "NoHatchMode");

        // ── 2. Replace Mode ───────────────────────────────────────────────────
        yPos = addToggleSetting(scroll, yPos, labelX, controlX, controlW,
                "gtna.terminal.nexus.replace_mode",
                "gtna.terminal.nexus.replace_mode.tooltip",
                "gtna.terminal.nexus.replace_mode.hint",
                "ReplaceMode");

        // ── 3. Demolition Mode ────────────────────────────────────────────────
        yPos = addToggleSetting(scroll, yPos, labelX, controlX, controlW,
                "gtna.terminal.nexus.demolition_mode",
                "gtna.terminal.nexus.demolition_mode.tooltip",
                "gtna.terminal.nexus.demolition_mode.hint",
                "DemolitionMode");

        // ── 4. Use AE2 ───────────────────────────────────────────────────────
        yPos = addToggleSetting(scroll, yPos, labelX, controlX, controlW,
                "gtna.terminal.nexus.use_ae",
                "gtna.terminal.nexus.use_ae.tooltip",
                "gtna.terminal.nexus.use_ae.hint",
                "UseAE");

        // ── 5. Mirror Build ───────────────────────────────────────────────────
        yPos = addToggleSetting(scroll, yPos, labelX, controlX, controlW,
                "gtna.terminal.nexus.mirror_build",
                "gtna.terminal.nexus.mirror_build.tooltip",
                "gtna.terminal.nexus.mirror_build.hint",
                "MirrorBuild");

        // ── Separator line ────────────────────────────────────────────────────
        scroll.addWidget(new ImageWidget(labelX, yPos, GUI_WIDTH - 30, 1,
                new ColorRectTexture(COLOR_BG_LIGHT)));
        yPos += 8;

        // ── 6. Repetitions (numeric) ──────────────────────────────────────────
        yPos = addNumericSetting(scroll, yPos, labelX, controlX, controlW,
                "gtna.terminal.nexus.repetitions",
                "gtna.terminal.nexus.repetitions.tooltip",
                "gtna.terminal.nexus.repetitions.hint",
                "Repetitions", 0, 1000);

        // ── 7. Module Build (numeric) ─────────────────────────────────────────
        yPos = addNumericSetting(scroll, yPos, labelX, controlX, controlW,
                "gtna.terminal.nexus.module_build",
                "gtna.terminal.nexus.module_build.tooltip",
                "gtna.terminal.nexus.module_build.hint",
                "ModuleBuild", 0, 100);

        panel.addWidget(scroll);
        return panel;
    }

    /**
     * Add a toggle setting row: label + button + hint.
     * Returns new yPos after this setting.
     */
    private int addToggleSetting(DraggableScrollableWidgetGroup scroll,
                                 int y, int labelX, int controlX, int controlW,
                                 String labelKey, String tooltipKey, String hintKey,
                                 String nbtKey) {
        final String yesStr = "§a● Yes";
        final String noStr = "§c● No";

        // Label
        LabelWidget label = new LabelWidget(labelX, y + 2,
                Component.translatable(labelKey).getString());
        label.setTextColor(COLOR_TEXT_GRAY);
        label.setHoverTooltips(Component.translatable(tooltipKey));
        scroll.addWidget(label);

        // Toggle button
        ButtonWidget toggle = new ButtonWidget(controlX, y, controlW, 14,
                new ColorRectTexture(COLOR_BG_DARK),
                cd -> setBoolTag(nbtKey, !getBoolTag(nbtKey)));
        toggle.setHoverTexture(new ColorRectTexture(COLOR_BG_LIGHT));
        scroll.addWidget(toggle);

        // Dynamic label in button (Yes/No)
        LabelWidget toggleLabel = new LabelWidget(controlX + 6, y + 3,
                () -> getBoolTag(nbtKey) ? yesStr : noStr);
        toggleLabel.setTextColor(COLOR_TEXT_WHITE);
        scroll.addWidget(toggleLabel);

        // Hint text
        LabelWidget hint = new LabelWidget(labelX, y + 16,
                Component.translatable(hintKey).getString());
        hint.setTextColor(COLOR_HINT);
        scroll.addWidget(hint);

        return y + 30;
    }

    /**
     * Add a numeric input setting: label + text field + hint.
     * Returns new yPos after this setting.
     */
    private int addNumericSetting(DraggableScrollableWidgetGroup scroll,
                                  int y, int labelX, int controlX, int controlW,
                                  String labelKey, String tooltipKey, String hintKey,
                                  String nbtKey, int min, int max) {
        // Label
        LabelWidget label = new LabelWidget(labelX, y + 2,
                Component.translatable(labelKey).getString());
        label.setTextColor(COLOR_TEXT_GRAY);
        label.setHoverTooltips(Component.translatable(tooltipKey));
        scroll.addWidget(label);

        // Numeric text field
        TextFieldWidget numField = new TextFieldWidget(controlX, y, controlW, 14,
                () -> String.valueOf(getIntTag(nbtKey)),
                str -> {
                    try {
                        int val = Integer.parseInt(str);
                        val = Math.max(min, Math.min(max, val));
                        setIntTag(nbtKey, val);
                    } catch (NumberFormatException ignored) {}
                });
        numField.setNumbersOnly(min, max);
        numField.setTextColor(COLOR_TEXT_WHITE);
        numField.setBackground(new ColorRectTexture(COLOR_BG_DARK));
        numField.setHoverTexture(new ColorRectTexture(COLOR_BG_LIGHT));
        numField.setWheelDur(1);
        scroll.addWidget(numField);

        // Hint text
        LabelWidget hint = new LabelWidget(labelX, y + 16,
                Component.translatable(hintKey).getString());
        hint.setTextColor(COLOR_HINT);
        scroll.addWidget(hint);

        return y + 30;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // BLOCK CONFIG PANEL (delegates to BlockSelectionConfigWidget)
    // ═══════════════════════════════════════════════════════════════════════════

    private WidgetGroup createBlockConfigPanel() {
        int contentY = 46;
        int contentH = GUI_HEIGHT - contentY - 4;

        BlockSelectionConfigWidget configBuilder = new BlockSelectionConfigWidget(itemStack);
        return configBuilder.createConfigPanel(4, contentY, GUI_WIDTH - 8, contentH);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // NBT HELPERS
    // ═══════════════════════════════════════════════════════════════════════════

    private int getIntTag(String key) {
        var tag = itemStack.getTag();
        if (tag != null && tag.contains(key)) {
            return tag.getInt(key);
        }
        return 0;
    }

    private void setIntTag(String key, int value) {
        var tag = itemStack.getOrCreateTag();
        tag.putInt(key, value);
    }

    private boolean getBoolTag(String key) {
        var tag = itemStack.getTag();
        if (tag != null && tag.contains(key)) {
            return tag.getBoolean(key);
        }
        return false;
    }

    private void setBoolTag(String key, boolean value) {
        var tag = itemStack.getOrCreateTag();
        tag.putBoolean(key, value);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SETTINGS DATA CLASS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Encapsulates all terminal settings read from NBT.
     * Used by the AutoBuilder to configure block placement behavior.
     */
    @Getter
    @Setter
    public static class AutoBuildSetting {

        private int repetitions = 0;
        private int moduleBuild = 0;
        private boolean replaceMode = false;
        private boolean demolitionMode = false;
        private boolean useAE = false;
        private boolean mirrorBuild = false;
        private boolean noHatchMode = false;

        // Block selection indices (-1 = default/no selection)
        private int selectedCoilIndex = -1;
        private int selectedCasingIndex = -1;
        private int selectedMufflerIndex = -1;
        private int selectedRotorIndex = -1;
        private int selectedCapacitorIndex = -1;

        /**
         * Read settings from an ItemStack's NBT tag.
         */
        public static AutoBuildSetting getSetting(ItemStack stack) {
            AutoBuildSetting setting = new AutoBuildSetting();
            CompoundTag tag = stack.getTag();
            if (tag != null) {
                setting.repetitions = tag.getInt("Repetitions");
                setting.moduleBuild = tag.getInt("ModuleBuild");
                setting.replaceMode = tag.getBoolean("ReplaceMode");
                setting.demolitionMode = tag.getBoolean("DemolitionMode");
                setting.useAE = tag.getBoolean("UseAE");
                setting.mirrorBuild = tag.getBoolean("MirrorBuild");
                setting.noHatchMode = tag.getBoolean("NoHatchMode");

                // Block selections
                setting.selectedCoilIndex = tag.contains("SelectedCoil") ? tag.getInt("SelectedCoil") : -1;
                setting.selectedCasingIndex = tag.contains("SelectedCasing") ? tag.getInt("SelectedCasing") : -1;
                setting.selectedMufflerIndex = tag.contains("SelectedMuffler") ? tag.getInt("SelectedMuffler") : -1;
                setting.selectedRotorIndex = tag.contains("SelectedRotor") ? tag.getInt("SelectedRotor") : -1;
                setting.selectedCapacitorIndex = tag.contains("SelectedCapacitor") ? tag.getInt("SelectedCapacitor") :
                        -1;
            }
            return setting;
        }

        /**
         * Write settings to an ItemStack's NBT tag.
         */
        public void save(ItemStack stack) {
            CompoundTag tag = stack.getOrCreateTag();
            tag.putInt("Repetitions", repetitions);
            tag.putInt("ModuleBuild", moduleBuild);
            tag.putBoolean("ReplaceMode", replaceMode);
            tag.putBoolean("DemolitionMode", demolitionMode);
            tag.putBoolean("UseAE", useAE);
            tag.putBoolean("MirrorBuild", mirrorBuild);
            tag.putBoolean("NoHatchMode", noHatchMode);
            tag.putInt("SelectedCoil", selectedCoilIndex);
            tag.putInt("SelectedCasing", selectedCasingIndex);
            tag.putInt("SelectedMuffler", selectedMufflerIndex);
            tag.putInt("SelectedRotor", selectedRotorIndex);
            tag.putInt("SelectedCapacitor", selectedCapacitorIndex);
        }

        /**
         * Get the selected ItemStack for the coil category.
         */
        public ItemStack getSelectedCoil() {
            return BlockSelectionConfigWidget.getCoilEntries().size() > selectedCoilIndex && selectedCoilIndex >= 0 ?
                    BlockSelectionConfigWidget.getCoilEntries().get(selectedCoilIndex) : null;
        }
    }
}
