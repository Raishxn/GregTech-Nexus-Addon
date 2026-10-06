package com.raishxn.gtna.client.personalspace.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

import java.awt.Rectangle;

public class WButton extends Widget {

    public static final int DEFAULT_COLOR = 0xFFFFFF;

    public String text = "";
    public boolean dropShadow = true;
    public int color = DEFAULT_COLOR;
    public Icons buttonIcon;
    public ItemStack itemStack;
    public String itemStackText = "";
    public Runnable onClick;
    public int lastButton = 0;
    public String tooltip;

    public WButton(Rectangle position, String text, boolean dropShadow, int color, Icons buttonIcon, Runnable onClick) {
        this.position = position;
        this.dropShadow = dropShadow;
        this.color = color;
        this.onClick = onClick;
        this.buttonIcon = buttonIcon;
        setText(text);
    }

    public void setText(String text) {
        this.text = text == null ? "" : text;
    }

    @Override
    protected void drawImpl(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        Icons icon = enabled ? Icons.BUTTON_NORMAL : Icons.BUTTON_OFF;
        if (enabled && testPoint(mouseX, mouseY)) icon = Icons.BUTTON_HIGHLIGHT;
        icon.draw9Patch(graphics, 0, 0, position.width, position.height);
        int textSpace = position.width;
        if (buttonIcon != null) {
            textSpace -= buttonIcon.w + 6;
            buttonIcon.drawAt(graphics, 1, position.height / 2 - buttonIcon.h / 2);
        }
        if (itemStack != null || itemStackText != null && !itemStackText.isEmpty()) {
            textSpace -= 18;
            Icons.drawItem(graphics, 1, position.height / 2 - 9, itemStack, itemStackText);
        }
        if (!text.isEmpty()) {
            Font font = Minecraft.getInstance().font;
            String drawText = font.plainSubstrByWidth(text, Math.max(0, textSpace - 6));
            int textW = font.width(drawText);
            graphics.drawString(font, drawText, position.width - textSpace / 2 - textW / 2,
                    position.height / 2 - font.lineHeight / 2, color, dropShadow);
        }
    }

    @Override
    protected void drawForegroundImpl(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (tooltip != null && !tooltip.isEmpty() && testPoint(mouseX, mouseY) && !WBlockDropdown.isAnyDropdownOpen()) {
            drawTooltip(graphics, mouseX - position.x, mouseY - position.y, tooltip);
        }
    }

    @Override
    protected boolean mouseClickedImpl(int x, int y, int button) {
        lastButton = button;
        if (onClick != null) onClick.run();
        clickSound();
        return true;
    }
}
