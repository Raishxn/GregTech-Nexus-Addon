package com.raishxn.gtna.client.personalspace.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

import java.awt.Rectangle;
import java.util.Locale;
import java.util.function.DoubleConsumer;

public class WSlider extends Widget {

    private String text = "";
    public boolean dropShadow;
    public int color;
    public Icons buttonIcon;
    public double minValue, maxValue, rawValue, step;
    public DoubleConsumer onChange;
    private boolean sliderActive = false;

    public WSlider(Rectangle position, String text, double min, double max, double value, double step,
                   boolean dropShadow, int color, Icons buttonIcon, DoubleConsumer onChange) {
        this.position = position;
        this.dropShadow = dropShadow;
        this.color = color;
        this.onChange = onChange;
        this.buttonIcon = buttonIcon;
        this.minValue = min;
        this.maxValue = max;
        this.rawValue = value;
        this.step = step;
        this.text = text == null ? "" : text;
    }

    public double getValue() {
        return step > 1.0e-6 ? Math.round(rawValue / step) * step : rawValue;
    }

    public int getValueInt() {
        return (int) getValue();
    }

    public double value01() {
        return Mth.clamp((rawValue - minValue) / Math.max(maxValue - minValue, 1.0e-6), 0, 1);
    }

    @Override
    protected void drawImpl(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        Icons icon = enabled ? Icons.BUTTON_NORMAL : Icons.BUTTON_OFF;
        if (enabled && testPoint(mouseX, mouseY)) icon = Icons.BUTTON_HIGHLIGHT;
        Icons.BUTTON_OFF.draw9Patch(graphics, 0, 0, position.width, position.height);
        icon.draw9Patch(graphics, (int) (value01() * (position.width - 15)), 0, 15, position.height);
        int textSpace = position.width - 6;
        if (buttonIcon != null) {
            textSpace -= buttonIcon.w;
            buttonIcon.drawAt(graphics, 4, position.height / 2 - buttonIcon.h / 2 - 1);
        }
        String shown = text;
        if (!shown.isEmpty()) {
            if (shown.contains("%")) shown = String.format(Locale.ROOT, shown, rawValue);
            Font font = Minecraft.getInstance().font;
            String drawText = font.plainSubstrByWidth(shown, Math.max(0, textSpace - 6));
            int textW = font.width(drawText);
            graphics.drawString(font, drawText, position.width - textSpace / 2 - textW / 2,
                    position.height / 2 - font.lineHeight / 2, color, dropShadow);
        }
    }

    private void updateValueFromMouse(int x) {
        double newVal01 = Mth.clamp((double) (x - position.x - 3) / (double) (position.width - 6), 0, 1);
        double newVal = minValue + newVal01 * (maxValue - minValue);
        double oldVal = rawValue;
        rawValue = newVal;
        rawValue = getValue();
        if (Math.abs(newVal - oldVal) > 1.0e-6 && onChange != null) onChange.accept(rawValue);
    }

    @Override
    protected boolean mouseClickedImpl(int x, int y, int button) {
        sliderActive = true;
        clickSound();
        updateValueFromMouse(x);
        return true;
    }

    @Override
    protected boolean mouseReleasedImpl(int x, int y, int button) {
        if (!sliderActive) return false;
        sliderActive = false;
        updateValueFromMouse(x);
        return true;
    }

    @Override
    protected boolean mouseDraggedImpl(int x, int y, int button) {
        if (!sliderActive) return false;
        updateValueFromMouse(x);
        return true;
    }
}
