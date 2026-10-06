package com.raishxn.gtna.client.personalspace.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class WLabel extends Widget {

    private String text = "";
    public boolean dropShadow;
    public int color = 0x222222;

    public WLabel(int x, int y, String text, boolean dropShadow) {
        this.dropShadow = dropShadow;
        position.x = x;
        position.y = y;
        setText(text);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text == null ? "" : text;
        position.width = Minecraft.getInstance().font.width(this.text);
        position.height = Minecraft.getInstance().font.lineHeight;
    }

    @Override
    protected void drawImpl(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (!text.isEmpty()) graphics.drawString(Minecraft.getInstance().font, text, 0, 0, color, dropShadow);
    }
}
