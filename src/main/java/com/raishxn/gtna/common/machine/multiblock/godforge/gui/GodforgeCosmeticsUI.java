package com.raishxn.gtna.common.machine.multiblock.godforge.gui;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceBorderTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import com.raishxn.gtna.api.machine.feature.godforge.GodforgeStarColor;
import com.raishxn.gtna.common.machine.multiblock.godforge.ForgeOfGodsMachine;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.raishxn.gtna.common.machine.multiblock.godforge.gui.GodforgeGui.*;

/**
 * Star cosmetics of the Forge of Gods: GTNH {@code StarCosmeticsPanel}, {@code CustomStarColorPanel},
 * {@code CustomStarColorSelector} and {@code StarColorImportPanel} (GT5-Unofficial a3e1e112). The colour editor lives
 * on the client; Save and Delete send the result to the controller.
 */
final class GodforgeCosmeticsUI {

    private static final String COS = "gtna.godforge.cosmetics.";
    static final IGuiTexture OUTLINE = new ResourceBorderTexture("gtna:textures/gui/godforge/button_outline.png", 16,
            16, 1, 1);
    static final IGuiTexture OUTLINE_PRESSED = new ResourceBorderTexture(
            "gtna:textures/gui/godforge/button_outline_pressed.png", 16, 16, 1, 1);
    private static final IGuiTexture POWER_ON = tex("overlay_button_power_switch_on");
    private static final IGuiTexture POWER_OFF = tex("overlay_button_power_switch_off");
    private static final IGuiTexture UNSELECTED = tex("picture_unselected_option");
    private static final int MAX_LISTED = 32;

    private GodforgeCosmeticsUI() {}

    /** Colour editor state (GTNH {@code ColorData} plus the editing list). Client side only. */
    static final class Editor {

        int r = GodforgeStarColor.DEFAULT_RED, g = GodforgeStarColor.DEFAULT_GREEN, b = GodforgeStarColor.DEFAULT_BLUE;
        float gamma = GodforgeStarColor.DEFAULT_GAMMA;
        boolean hsv;
        final List<GodforgeStarColor.Setting> list = new ArrayList<>();
        int selectedSlot = -1;
        int cycleSpeed = GodforgeStarColor.DEFAULT_CYCLE_SPEED;
        String name = "New Star Color";
        int editIndex = -1;
        String importText = "";

        void startNew() {
            resetColor();
            list.clear();
            selectedSlot = -1;
            cycleSpeed = GodforgeStarColor.DEFAULT_CYCLE_SPEED;
            name = "New Star Color";
            editIndex = -1;
        }

        void load(GodforgeStarColor color, int index) {
            list.clear();
            list.addAll(color.settings());
            selectedSlot = -1;
            cycleSpeed = color.cycleSpeed();
            name = color.name();
            editIndex = color.isPreset() ? -1 : index;
            if (!list.isEmpty()) setColor(list.get(0));
        }

        void resetColor() {
            r = GodforgeStarColor.DEFAULT_RED;
            g = GodforgeStarColor.DEFAULT_GREEN;
            b = GodforgeStarColor.DEFAULT_BLUE;
            gamma = GodforgeStarColor.DEFAULT_GAMMA;
        }

        void setColor(GodforgeStarColor.Setting setting) {
            r = setting.r();
            g = setting.g();
            b = setting.b();
            gamma = setting.gamma();
        }

        int rgb() {
            return r << 16 | g << 8 | b;
        }

        float[] hsb() {
            return java.awt.Color.RGBtoHSB(r, g, b, null);
        }

        void setHsb(float h, float s, float v) {
            int rgb = java.awt.Color.HSBtoRGB(h, s, v);
            r = (rgb >> 16) & 255;
            g = (rgb >> 8) & 255;
            b = rgb & 255;
        }

        GodforgeStarColor.Setting current() {
            return new GodforgeStarColor.Setting(r, g, b, gamma);
        }

        GodforgeStarColor build() {
            GodforgeStarColor color = new GodforgeStarColor(name.isBlank() ? "New Star Color" : name)
                    .cycleSpeed(cycleSpeed);
            List<GodforgeStarColor.Setting> settings = list.isEmpty() ? List.of(current()) : list;
            for (var setting : settings) color.addColor(setting);
            return color;
        }
    }

    // ---------------------------------------------------------------- Star Cosmetics (200 x 200)

    static Widget cosmetics(ForgeOfGodsMachine machine, ForgeOfGodsUI.Navigator nav, Editor editor) {
        WidgetGroup page = new WidgetGroup(0, 0, 200, 200);
        page.setBackground(tex("background_white_glow"));
        page.addWidget(new WrappedText(0, 8, 200, 10, () -> Component.translatable(COS + "header")
                .withStyle(ChatFormatting.GOLD), true, 1f));
        // Colour column.
        page.addWidget(new WrappedText(9, 28, 90, 10, () -> Component.translatable(COS + "color")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.UNDERLINE), false, 1f));
        var list = new DraggableScrollableWidgetGroup(4, 44, 104, 147);
        list.setYScrollBarWidth(2).setYBarStyle(IGuiTexture.EMPTY, IGuiTexture.EMPTY);
        for (int i = 0; i <= MAX_LISTED; i++) {
            final int index = i;
            WidgetGroup row = new WidgetGroup(0, i * 20, 100, 16) {

                @Override
                public boolean isVisible() {
                    return index <= machine.view().starColors.size();
                }

                @Override
                public com.lowdragmc.lowdraglib.utils.Position getSelfPosition() {
                    return super.getSelfPosition();
                }
            };
            // Rows past the stored colours become the "+ Custom..." entry.
            row.addWidget(new ImageWidget(5, 0, 16, 16, () -> {
                var colors = machine.view().starColors;
                if (index == colors.size()) return OUTLINE;
                if (index > colors.size()) return IGuiTexture.EMPTY;
                return colors.byIndex(index).name().equals(machine.view().selectedStarColor) ?
                        tex("button_standard_pressed") : tex("button_standard");
            }));
            row.addWidget(new StarColorSwatch(6, 1, 14, () -> {
                var colors = machine.view().starColors;
                return index < colors.size() ? colors.byIndex(index) : null;
            }));
            row.addWidget(new ImageWidget(5, 0, 16, 16, () -> index == machine.view().starColors.size() ?
                    new TextTexture("+", 0xFFFFFF) : IGuiTexture.EMPTY));
            row.addWidget(new WrappedText(25, 4, 75, 10, () -> {
                var colors = machine.view().starColors;
                if (index == colors.size()) {
                    return Component.translatable(COS + "customstarcolor").withStyle(ChatFormatting.GOLD);
                }
                if (index > colors.size()) return Component.empty();
                var color = colors.byIndex(index);
                return (color.nameKey().isEmpty() ? Component.literal(color.name()) :
                        Component.translatable(color.nameKey())).withStyle(ChatFormatting.GOLD);
            }, false, 1f));
            row.addWidget(new ButtonWidget(5, 0, 16, 16, IGuiTexture.EMPTY, click -> {
                var colors = machine.view().starColors;
                if (index == colors.size()) {
                    editor.startNew();
                    nav.open = "customcolor";
                    return;
                }
                if (index > colors.size()) return;
                var color = colors.byIndex(index);
                if (click.isShiftClick && !color.isPreset()) {
                    editor.load(color, index);
                    nav.open = "customcolor";
                } else if (!click.isRemote) {
                    machine.selectStarColor(color.name());
                }
            }));
            row.addWidget(new TooltipArea(5, 0, 16, 16, () -> {
                if (index == machine.view().starColors.size()) {
                    return List.of(Component.translatable(COS + "starcolor"));
                }
                return List.of(Component.translatable(COS + "selectcolor.tooltip.1"),
                        Component.translatable(COS + "selectcolor.tooltip.2"));
            }));
            list.addWidget(row);
        }
        page.addWidget(list);
        // Miscellaneous column (right aligned).
        int mx = 200 - 9 - 69;
        page.addWidget(new WrappedText(mx, 28, 80, 10, () -> Component.translatable(COS + "misc")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.UNDERLINE), false, 1f));
        page.addWidget(new WrappedText(mx, 48, 34, 10, () -> Component.translatable(COS + "spin")
                .withStyle(ChatFormatting.GOLD), false, 1f));
        page.addWidget(new com.raishxn.gtna.common.machine.multiblock.godforge.gui.GodforgeGui.ServerField(mx + 34, 44,
                35, 18,
                () -> String.valueOf(machine.view().rotationSpeed), value -> machine.setRotationSpeed(parse(value)))
                .setNumbersOnly(0, 100).setHoverTooltips(COS + "onlyintegers"));
        page.addWidget(new WrappedText(mx, 68, 34, 10, () -> Component.translatable(COS + "size")
                .withStyle(ChatFormatting.GOLD), false, 1f));
        page.addWidget(
                new com.raishxn.gtna.common.machine.multiblock.godforge.gui.GodforgeGui.ServerField(mx + 34, 64, 35, 18,
                        () -> String.valueOf(machine.view().starSize), value -> machine.setStarSize(parse(value)))
                        .setNumbersOnly(0, 40).setHoverTooltips(COS + "onlyintegers"));
        page.addWidget(new WrappedText(mx, 88, 53, 10, () -> Component.translatable(COS + "animations")
                .withStyle(ChatFormatting.GOLD), false, 1f));
        page.addWidget(celestialButton(mx + 53, 84, () -> machine.view().rendererDisabled ? POWER_OFF : POWER_ON,
                click -> {
                    if (!click.isRemote) machine.toggleRenderer();
                }, () -> List.of(Component.translatable(COS + "animations.tooltip"))));
        return page;
    }

    // ---------------------------------------------------------------- Custom Star Color (200 x 200)

    private static final int[] CHANNEL_COLORS = { 0xFF5555, 0x55FF55, 0x5555FF };

    static Widget customColor(ForgeOfGodsMachine machine, ForgeOfGodsUI.Navigator nav, Editor editor) {
        WidgetGroup page = new WidgetGroup(0, 0, 200, 200);
        page.setBackground(tex("background_white_glow"));
        page.addWidget(new WrappedText(0, 8, 200, 10, () -> Component.translatable(COS + "starcolor")
                .withStyle(ChatFormatting.GOLD), true, 1f));
        // RGB / HSV rows.
        for (int c = 0; c < 3; c++) {
            final int channel = c;
            int y = 22 + c * 18;
            page.addWidget(new WrappedText(8, y + 4, 31, 10, () -> editor.hsv ?
                    Component.literal(new String[] { "H", "S", "V" }[channel]) :
                    Component.translatable(COS + "color." + new String[] { "red", "green", "blue" }[channel])
                            .withStyle(style -> style.withColor(CHANNEL_COLORS[channel])),
                    false, 1f));
            page.addWidget(new ColorSlider(40, y + 4, 118, 8, 1, () -> gradient(editor, channel),
                    () -> normalized(editor, channel), value -> setNormalized(editor, channel, value)));
            page.addWidget(new EditorField(162, y, 32, 16, () -> channelText(editor, channel),
                    value -> setChannelText(editor, channel, value)));
        }
        // Gamma.
        page.addWidget(new WrappedText(8, 80, 31, 10, () -> Component.translatable(COS + "color.gamma")
                .withStyle(ChatFormatting.GRAY), false, 1f));
        page.addWidget(new ColorSlider(40, 80, 118, 8, 100, () -> new int[] { 0xFF808080, 0xFF808080 },
                () -> (double) editor.gamma, value -> editor.gamma = Math.round(value * 10) / 10f));
        page.addWidget(new EditorField(162, 76, 32, 16, () -> String.format(Locale.ROOT, "%.1f", editor.gamma),
                value -> editor.gamma = (float) clamp(parseDouble(value, editor.gamma), 0, 100)));
        // RGB / HSV switch, hex code, preview.
        page.addWidget(new ImageWidget(8, 96, 24, 15, () -> editor.hsv ? OUTLINE : OUTLINE_PRESSED));
        page.addWidget(new ImageWidget(8, 96, 24, 15, () -> new TextTexture(editor.hsv ? "RGB" :
                "§cR§aG§9B", 0xFFFFFF)));
        page.addWidget(new ButtonWidget(8, 96, 24, 15, IGuiTexture.EMPTY, click -> editor.hsv = false)
                .setClientSideWidget());
        page.addWidget(new ImageWidget(34, 96, 24, 15, () -> editor.hsv ? OUTLINE_PRESSED : OUTLINE));
        page.addWidget(new ImageWidget(34, 96, 24, 15, () -> new TextTexture(editor.hsv ? "§dH§6S§bV" : "HSV",
                0xFFFFFF)));
        page.addWidget(new ButtonWidget(34, 96, 24, 15, IGuiTexture.EMPTY, click -> editor.hsv = true)
                .setClientSideWidget());
        page.addWidget(new WrappedText(80, 100, 24, 10, () -> Component.translatable(COS + "color.hex")
                .withStyle(ChatFormatting.GOLD), false, 1f));
        page.addWidget(new EditorField(106, 96, 52, 15, () -> String.format("#%06X", editor.rgb()), value -> {
            try {
                int rgb = Integer.parseInt(value.replace("#", "").trim(), 16);
                editor.r = (rgb >> 16) & 255;
                editor.g = (rgb >> 8) & 255;
                editor.b = rgb & 255;
            } catch (NumberFormatException ignored) {}
        }));
        page.addWidget(new ImageWidget(162, 96, 32, 15, () -> new com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture(
                0xFF000000 | editor.rgb())));
        // Add / Apply and Reset.
        textButton(page, 62, 115, 37, 15, () -> editor.selectedSlot >= 0 ? COS + "applycolor" : COS + "addcolor",
                () -> {
                    if (editor.selectedSlot >= 0 && editor.selectedSlot < editor.list.size()) {
                        editor.list.set(editor.selectedSlot, editor.current());
                    } else if (editor.list.size() < GodforgeStarColor.MAX_COLORS) {
                        editor.list.add(editor.current());
                    }
                }, () -> editor.selectedSlot >= 0 ? COS + "applycolor.tooltip" : COS + "addcolor.tooltip");
        textButton(page, 101, 115, 37, 15, () -> COS + "resetcolor", editor::resetColor,
                () -> COS + "resetcolor.tooltip");
        // Colour list (nine slots) and cycle speed.
        for (int i = 0; i < GodforgeStarColor.MAX_COLORS; i++) {
            final int slot = i;
            int x = 8 + i * 18;
            page.addWidget(new ImageWidget(x, 134, 18, 18, () -> slot < editor.list.size() ? IGuiTexture.EMPTY :
                    UNSELECTED));
            page.addWidget(new ImageWidget(x + 1, 135, 16, 16, () -> {
                if (slot >= editor.list.size()) return IGuiTexture.EMPTY;
                var s = editor.list.get(slot);
                return new com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture(0xFF000000 | s.r() << 16 |
                        s.g() << 8 | s.b());
            }));
            page.addWidget(new ImageWidget(x, 134, 18, 18, () -> editor.selectedSlot == slot ? SELECTOR :
                    IGuiTexture.EMPTY));
            page.addWidget(new ButtonWidget(x, 134, 18, 18, IGuiTexture.EMPTY, click -> {
                if (slot >= editor.list.size()) return;
                if (click.button == 1) {
                    editor.list.remove(slot);
                    editor.selectedSlot = -1;
                } else if (editor.selectedSlot == slot) {
                    editor.selectedSlot = -1;
                } else {
                    editor.selectedSlot = slot;
                    editor.setColor(editor.list.get(slot));
                }
            }).setClientSideWidget());
        }
        page.addWidget(new EditorField(172, 135, 21, 16, () -> String.valueOf(editor.cycleSpeed),
                value -> editor.cycleSpeed = (int) clamp(parse(value), 1, 100)));
        page.addWidget(new TooltipArea(172, 135, 21, 16,
                () -> List.of(Component.translatable(COS + "cyclespeed"))));
        // Name.
        page.addWidget(new WrappedText(8, 160, 92, 10, () -> Component.translatable(COS + "starcolorname")
                .withStyle(ChatFormatting.GOLD), false, 1f));
        page.addWidget(new EditorField(100, 156, 92, 16, () -> editor.name,
                value -> editor.name = value.length() > 15 ? value.substring(0, 15) : value)
                .setMaxStringLength(15));
        page.addWidget(new TooltipArea(100, 156, 92, 16, () -> List.of(
                Component.translatable(COS + "starcolorname.tooltip.1"),
                Component.translatable(COS + "starcolorname.tooltip.2"))));
        // Export / Import / Delete / Save.
        textButton(page, 23, 178, 37, 15, () -> COS + "exportcolors", () -> {
            String text = editor.build().serializeToString();
            var mc = net.minecraft.client.Minecraft.getInstance();
            mc.keyboardHandler.setClipboard(text);
            if (mc.player != null) mc.player.sendSystemMessage(Component.translatable(COS + "exportcolors.message"));
        }, () -> COS + "exportcolors.tooltip");
        textButton(page, 62, 178, 37, 15, () -> COS + "importcolors", () -> {
            editor.importText = "";
            nav.open = "importcolor";
        }, () -> COS + "importcolors.tooltip");
        page.addWidget(new ImageWidget(101, 178, 37, 15, OUTLINE));
        page.addWidget(new ImageWidget(101, 178, 37, 15, new TextTexture(COS + "deletecolors", 0xFFFFFF)));
        page.addWidget(new PayloadButton(101, 178, 37, 15, () -> {
            var colors = machine.view().starColors;
            return editor.editIndex >= 0 && editor.editIndex < colors.size() ?
                    colors.byIndex(editor.editIndex).name() : null;
        }, machine::deleteStarColor, () -> {
            editor.startNew();
            nav.open = "cosmetics";
        }));
        page.addWidget(new TooltipArea(101, 178, 37, 15,
                () -> List.of(Component.translatable(COS + "deletecolors.tooltip"))));
        page.addWidget(new ImageWidget(140, 178, 37, 15, OUTLINE));
        page.addWidget(new ImageWidget(140, 178, 37, 15, new TextTexture(COS + "savecolors", 0xFFFFFF)));
        page.addWidget(new PayloadButton(140, 178, 37, 15,
                () -> editor.editIndex + ";" + editor.build().serializeToString(), payload -> {
                    int split = payload.indexOf(';');
                    if (split < 0) return;
                    int index;
                    try {
                        index = Integer.parseInt(payload.substring(0, split));
                    } catch (NumberFormatException e) {
                        return;
                    }
                    machine.saveStarColor(payload.substring(split + 1), index);
                }, () -> nav.open = "cosmetics"));
        page.addWidget(new TooltipArea(140, 178, 37, 15,
                () -> List.of(Component.translatable(COS + "savecolors.tooltip"))));
        return page;
    }

    // ---------------------------------------------------------------- Star Color import (200 x 70)

    static Widget importColor(ForgeOfGodsUI.Navigator nav, Editor editor) {
        WidgetGroup page = new WidgetGroup(0, 0, 200, 70);
        page.setBackground(tex("background_white_glow"));
        page.addWidget(new WrappedText(0, 8, 200, 10, () -> Component.translatable(COS + "importer.import")
                .withStyle(ChatFormatting.GOLD), true, 1f));
        page.addWidget(new EditorField(8, 22, 184, 14, () -> editor.importText, value -> editor.importText = value)
                .setMaxStringLength(Short.MAX_VALUE));
        page.addWidget(new WrappedText(8, 39, 184, 10, () -> editor.importText.isEmpty() ? Component.empty() :
                GodforgeStarColor.deserializeString(editor.importText) == null ?
                        Component.translatable(COS + "importer.error").withStyle(ChatFormatting.RED) :
                        Component.translatable(COS + "importer.valid").withStyle(ChatFormatting.GREEN),
                true, 1f));
        textButton(page, 62, 51, 37, 15, () -> COS + "importer.apply", () -> {
            var color = GodforgeStarColor.deserializeString(editor.importText);
            if (color == null) return;
            int editIndex = editor.editIndex;
            editor.load(color, -1);
            editor.editIndex = editIndex;
            nav.open = "customcolor";
        }, () -> COS + "importer.apply.tooltip");
        textButton(page, 101, 51, 37, 15, () -> COS + "importer.reset", () -> editor.importText = "",
                () -> COS + "importer.reset.tooltip");
        return page;
    }

    // ---------------------------------------------------------------- helpers

    private static void textButton(WidgetGroup page, int x, int y, int w, int h,
                                   java.util.function.Supplier<String> key, Runnable action,
                                   java.util.function.Supplier<String> tooltip) {
        page.addWidget(new ImageWidget(x, y, w, h, OUTLINE));
        page.addWidget(new ImageWidget(x, y, w, h, () -> new TextTexture(key.get(), 0xFFFFFF)));
        page.addWidget(new ButtonWidget(x, y, w, h, IGuiTexture.EMPTY, click -> action.run()).setClientSideWidget());
        page.addWidget(new TooltipArea(x, y, w, h, () -> List.of(Component.translatable(tooltip.get()))));
    }

    private static int[] gradient(Editor editor, int channel) {
        if (editor.hsv) {
            float[] hsb = editor.hsb();
            if (channel == 0) {
                int[] stops = new int[7];
                for (int i = 0; i < 7; i++) stops[i] = java.awt.Color.HSBtoRGB(i / 6f, 1f, 1f);
                return stops;
            }
            return channel == 1 ?
                    new int[] { java.awt.Color.HSBtoRGB(hsb[0], 0f, hsb[2]),
                            java.awt.Color.HSBtoRGB(hsb[0], 1f, hsb[2]) } :
                    new int[] { java.awt.Color.HSBtoRGB(hsb[0], hsb[1], 0f),
                            java.awt.Color.HSBtoRGB(hsb[0], hsb[1], 1f) };
        }
        int rgb = editor.rgb();
        int shift = 16 - channel * 8;
        int start = 0xFF000000 | (rgb & ~(0xFF << shift));
        int end = 0xFF000000 | rgb | (0xFF << shift);
        return new int[] { start, end };
    }

    /** Slider position 0-1 of a channel (RGB / 255, or the HSB component). */
    private static double normalized(Editor editor, int channel) {
        if (editor.hsv) return editor.hsb()[channel];
        return (channel == 0 ? editor.r : channel == 1 ? editor.g : editor.b) / 255.0;
    }

    private static void setNormalized(Editor editor, int channel, double value) {
        if (editor.hsv) {
            float[] hsb = editor.hsb();
            hsb[channel] = (float) value;
            editor.setHsb(hsb[0], hsb[1], hsb[2]);
        } else {
            setChannel(editor, channel, value * 255);
        }
    }

    /** Channel value as shown in its field: 0-255 for RGB, hue 0-360 and 0-1 for S and V. */
    private static double channelValue(Editor editor, int channel) {
        if (editor.hsv) return editor.hsb()[channel] * (channel == 0 ? 360.0 : 1.0);
        return channel == 0 ? editor.r : channel == 1 ? editor.g : editor.b;
    }

    private static void setChannel(Editor editor, int channel, double sliderValue) {
        int value = (int) Math.round(sliderValue);
        if (channel == 0) editor.r = value;
        else if (channel == 1) editor.g = value;
        else editor.b = value;
    }

    private static String channelText(Editor editor, int channel) {
        if (!editor.hsv) return String.valueOf((int) channelValue(editor, channel));
        double value = channelValue(editor, channel);
        return channel == 0 ? String.valueOf(Math.round(value)) : String.format(Locale.ROOT, "%.2f", value);
    }

    private static void setChannelText(Editor editor, int channel, String text) {
        if (editor.hsv) {
            double value = parseDouble(text, channelValue(editor, channel));
            setNormalized(editor, channel, channel == 0 ? clamp(value, 0, 360) / 360.0 : clamp(value, 0, 1));
        } else {
            setChannel(editor, channel, clamp(parse(text), 0, 255));
        }
    }

    private static int parse(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static double parseDouble(String value, double fallback) {
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
