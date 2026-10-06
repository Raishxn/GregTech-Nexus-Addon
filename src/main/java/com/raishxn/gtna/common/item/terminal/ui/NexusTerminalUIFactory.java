package com.raishxn.gtna.common.item.terminal.ui;

import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.*;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.utils.Size;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import com.raishxn.gtna.common.item.terminal.ui.BlockSelectionConfigWidget.BlockCategory;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

/**
 * Compact settings and tier selectors inspired by the GTO Advanced Terminal workflow.
 * Uses native GTCEu 7.5.3 widgets and Nexus NBT, not GTO's incompatible uipro classes.
 */
public class NexusTerminalUIFactory {

    private static final int BG = 0xff17131d;
    private static final int PANEL = 0xff28222f;
    private static final int ACCENT = 0xff9861d0;
    private static final int TEXT = 0xffddd3ea;
    private final HeldItemUIFactory.HeldItemHolder holder;
    private final Player player;
    private final ItemStack stack;
    private final Map<BlockCategory, WidgetGroup> grids = new EnumMap<>(BlockCategory.class);
    private BlockCategory active;

    public NexusTerminalUIFactory(HeldItemUIFactory.HeldItemHolder holder, Player player) {
        this.holder = holder;
        this.player = player;
        this.stack = holder.getHeld();
    }

    public ModularUI createModularUI() {
        WidgetGroup root = new WidgetGroup(0, 0, 500, 248);
        WidgetGroup main = new WidgetGroup(162, 0, 176, 248);
        main.setBackground(new GuiTextureGroup(new ColorRectTexture(PANEL), new ColorBorderTexture(1, ACCENT)));
        main.addWidget(new LabelWidget(8, 8, Component.translatable("gtna.terminal.nexus.title")).setTextColor(ACCENT));
        WidgetGroup controls = new WidgetGroup(0, 24, 176, 224);
        numeric(controls, 8, "Repetitions", "gtna.terminal.nexus.repetitions", 1000);
        numeric(controls, 44, "ModuleBuild", "gtna.terminal.nexus.module_build", 100);
        toggle(controls, 81, "ReplaceMode", "gtna.terminal.nexus.replace_mode");
        toggle(controls, 99, "DemolitionMode", "gtna.terminal.nexus.demolition_mode");
        toggle(controls, 117, "UseAE", "gtna.terminal.nexus.use_ae");
        toggle(controls, 135, "MirrorBuild", "gtna.terminal.nexus.mirror_build");
        toggle(controls, 153, "NoHatchMode", "gtna.terminal.nexus.no_hatch");
        main.addWidget(controls);
        root.addWidget(main);

        WidgetGroup chooser = new WidgetGroup(188, 0, 134, 248);
        chooser.setBackground(new GuiTextureGroup(new ColorRectTexture(PANEL), new ColorBorderTexture(1, ACCENT)));
        chooser.addWidget(
                new LabelWidget(6, 8, Component.translatable("gtna.terminal.nexus.tiered_block")).setTextColor(TEXT));
        chooser.addWidget(new ButtonWidget(110, 4, 18, 18, button("×"), cd -> {
            chooser.setVisible(false);
            chooser.setActive(false);
            main.setSelfPosition(new com.lowdragmc.lowdraglib.utils.Position(162, 0));
            show(null);
        }));
        var categories = scroll(2, 26, 130, 218);
        int y = 3;
        for (var category : BlockCategory.values()) {
            // Glass and decorative lights are intentionally outside the tier selector.
            if (category == BlockCategory.GLASS || category == BlockCategory.LIGHT) continue;
            var entries = BlockSelectionConfigWidget.getEntries(category);
            if (entries.isEmpty()) continue;
            int row = y;
            categories.addWidget(new ImageWidget(3, row, 20, 20, () -> {
                var selected = BlockSelectionConfigWidget.getSelectedBlock(stack, category);
                return new GuiTextureGroup(new ColorRectTexture(BG), new ColorBorderTexture(1, 0xff51435e),
                        new ItemStackTexture(selected == null ? ItemStack.EMPTY : selected));
            }));
            var select = new ButtonWidget(25, row, 94, 20, button(category.translationKey), cd -> show(category));
            select.setHoverTooltips(Component.translatable(category.translationKey));
            categories.addWidget(select);
            y += 23;
            WidgetGroup grid = createGrid(category, entries);
            grids.put(category, grid);
            root.addWidget(grid);
        }
        chooser.addWidget(categories);
        root.addWidget(chooser);
        chooser.setVisible(false);
        chooser.setActive(false);
        show(null);
        controls.addWidget(
                new LabelWidget(6, 186, Component.translatable("gtna.terminal.nexus.tiered_block")).setTextColor(TEXT));
        controls.addWidget(new ButtonWidget(76, 181, 65, 20, button("gtna.terminal.nexus.select"), cd -> {
            main.setSelfPosition(new com.lowdragmc.lowdraglib.utils.Position(6, 0));
            chooser.setVisible(true);
            chooser.setActive(true);
        }));
        var clear = new ButtonWidget(145, 181, 23, 20, button("×"), cd -> BlockSelectionConfigWidget.clearAll(stack));
        clear.setHoverTooltips(Component.translatable("gtna.terminal.nexus.clear_all"));
        controls.addWidget(clear);
        return new ModularUI(new Size(500, 248), holder, player).widget(root).background(new ColorRectTexture(0));
    }

    private void show(BlockCategory selected) {
        active = selected;
        grids.forEach((category, group) -> {
            group.setVisible(category == selected);
            group.setActive(category == selected);
        });
    }

    private WidgetGroup createGrid(BlockCategory category, List<ItemStack> entries) {
        WidgetGroup group = new WidgetGroup(328, 0, 166, 248);
        group.setBackground(new GuiTextureGroup(new ColorRectTexture(PANEL), new ColorBorderTexture(1, ACCENT)));
        group.addWidget(new ImageWidget(4, 4, 134, 18,
                new TextTexture(category.translationKey).setWidth(134).setType(TextTexture.TextType.ROLL)));
        group.addWidget(new ButtonWidget(144, 4, 18, 18, button("×"), cd -> show(null)));
        var scroll = scroll(4, 26, 158, 186);
        for (int i = 0; i < entries.size(); i++) {
            ItemStack item = entries.get(i);
            int x = (i % 6) * 24 + 2;
            int y = (i / 6) * 24 + 2;
            scroll.addWidget(new ImageWidget(x, y, 22, 22, () -> {
                var selected = BlockSelectionConfigWidget.getSelectedBlock(stack, category);
                return new GuiTextureGroup(new ColorRectTexture(BG), new ColorBorderTexture(1,
                        selected != null && ItemStack.isSameItem(selected, item) ? ACCENT : 0xff51435e));
            }));
            scroll.addWidget(new ImageWidget(x + 3, y + 3, 16, 16, new ItemStackTexture(item)));
            var button = new ButtonWidget(x, y, 22, 22, new ColorRectTexture(0),
                    cd -> BlockSelectionConfigWidget.select(stack, category, item));
            button.setHoverTexture(new ColorBorderTexture(1, 0xffdfbbff));
            button.setHoverTooltips(item.getHoverName(), Component.translatable("gtna.terminal.nexus.select_hint"));
            scroll.addWidget(button);
        }
        group.addWidget(scroll);
        group.addWidget(new ButtonWidget(4, 225, 158, 18,
                button("gtna.terminal.nexus.clear_choice"), cd -> BlockSelectionConfigWidget.clear(stack, category)));
        return group;
    }

    private DraggableScrollableWidgetGroup scroll(int x, int y, int width, int height) {
        var group = new DraggableScrollableWidgetGroup(x, y, width, height);
        group.setYScrollBarWidth(4);
        group.setYBarStyle(new ColorRectTexture(BG), new ColorRectTexture(ACCENT));
        group.setBackground(new ColorRectTexture(PANEL));
        return group;
    }

    private IGuiTexture button(String text) {
        return new GuiTextureGroup(new ColorRectTexture(BG), new ColorBorderTexture(1, 0xff51435e),
                new TextTexture(text).setWidth(96).setType(TextTexture.TextType.ROLL));
    }

    private void toggle(WidgetGroup group, int y, String key, String text) {
        group.addWidget(new LabelWidget(6, y + 4, Component.translatable(text)).setTextColor(TEXT));
        group.addWidget(new ImageWidget(140, y + 1, 28, 14,
                () -> new ColorRectTexture(stack.getOrCreateTag().getBoolean(key) ? 0xff673a91 : BG)));
        group.addWidget(new LabelWidget(146, y + 4, () -> stack.getOrCreateTag().getBoolean(key) ? "§aI" : "§70"));
        var button = new ButtonWidget(140, y + 1, 28, 14, new ColorBorderTexture(1, ACCENT),
                cd -> stack.getOrCreateTag().putBoolean(key, !stack.getOrCreateTag().getBoolean(key)));
        button.setHoverTooltips(Component.translatable(text + ".tooltip"));
        group.addWidget(button);
    }

    private void numeric(WidgetGroup group, int y, String key, String text, int max) {
        group.addWidget(new LabelWidget(6, y, Component.translatable(text)).setTextColor(TEXT));
        group.addWidget(new ButtonWidget(6, y + 14, 22, 16, button("-1"),
                cd -> setNumber(key, stack.getOrCreateTag().getInt(key) - 1, max)));
        var input = new TextFieldWidget(32, y + 14, 108, 16,
                () -> Integer.toString(Math.max(0, Math.min(max, stack.getOrCreateTag().getInt(key)))), value -> {
                    try {
                        setNumber(key, Integer.parseInt(value), max);
                    } catch (NumberFormatException ignored) {}
                });
        input.setNumbersOnly(0, max);
        input.setWheelDur(1);
        input.setTextColor(TEXT);
        input.setBackground(new ColorRectTexture(BG));
        input.setHoverTooltips(Component.translatable(text + ".tooltip"));
        group.addWidget(input);
        group.addWidget(new ButtonWidget(144, y + 14, 22, 16, button("+1"),
                cd -> setNumber(key, stack.getOrCreateTag().getInt(key) + 1, max)));
    }

    private void setNumber(String key, int value, int max) {
        stack.getOrCreateTag().putInt(key, Math.max(0, Math.min(max, value)));
    }

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
                setting.repetitions = Math.max(0, Math.min(1000, tag.getInt("Repetitions")));
                setting.moduleBuild = Math.max(0, Math.min(100, tag.getInt("ModuleBuild")));
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
