package com.raishxn.gtna.client.personalspace.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import com.mojang.blaze3d.systems.RenderSystem;
import com.raishxn.gtna.GTNACORE;

/** Regions of the PersonalSpace widget sheet ({@code widgets.png}, LGPL-3.0, from the original mod). */
public enum Icons {

    // 9-patch rectangles
    GUI_BG(0, 0, 24, 24),
    BUTTON_NORMAL(24, 0, 15, 15),
    BUTTON_HIGHLIGHT(40, 0, 15, 15),
    BUTTON_OFF(56, 0, 15, 15),
    BUTTON_PRESSED_NORMAL(72, 0, 15, 15),
    BUTTON_PRESSED_HIGHLIGHT(88, 0, 15, 15),
    // large icons (16x16)
    SLOT(0, 24, 17, 17),
    CHECKMARK(32, 16, 16, 16),
    CROSS(48, 16, 16, 16),
    LOCK(64, 16, 16, 16),
    PLUS(80, 16, 16, 16),
    MINUS(96, 16, 16, 16),
    // small icons (8x8)
    SMALL_CROSS(112, 16, 8, 8),
    SMALL_UP(120, 16, 8, 8),
    SMALL_DOWN(128, 16, 8, 8),
    SMALL_X(136, 16, 8, 8),
    // more large icons (16x16)
    STAR(32, 32, 16, 16),
    STAR_BG(48, 32, 16, 16),
    PENCIL(64, 32, 16, 16),
    MOON(80, 32, 16, 16),
    SUN(96, 32, 16, 16),
    SUN_MOON(112, 32, 16, 16);

    public static final ResourceLocation TEXTURE = GTNACORE.id("textures/gui/personalspace/widgets.png");

    public final int x, y, w, h;

    Icons(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public void drawAt(GuiGraphics graphics, int xOff, int yOff) {
        RenderSystem.enableBlend();
        graphics.blit(TEXTURE, xOff, yOff, x, y, w, h);
    }

    /** Draws the region stretched as a 9-patch whose border is a third of the source size. */
    public void draw9Patch(GuiGraphics graphics, int xOff, int yOff, int width, int height) {
        RenderSystem.enableBlend();
        int sw = w / 3;
        int sh = h / 3;
        int cw = w - 2 * sw;
        int ch = h - 2 * sh;
        int innerW = Math.max(0, width - 2 * sw);
        int innerH = Math.max(0, height - 2 * sh);
        // corners
        part(graphics, xOff, yOff, sw, sh, x, y, sw, sh);
        part(graphics, xOff + width - sw, yOff, sw, sh, x + w - sw, y, sw, sh);
        part(graphics, xOff, yOff + height - sh, sw, sh, x, y + h - sh, sw, sh);
        part(graphics, xOff + width - sw, yOff + height - sh, sw, sh, x + w - sw, y + h - sh, sw, sh);
        // edges
        part(graphics, xOff + sw, yOff, innerW, sh, x + sw, y, cw, sh);
        part(graphics, xOff + sw, yOff + height - sh, innerW, sh, x + sw, y + h - sh, cw, sh);
        part(graphics, xOff, yOff + sh, sw, innerH, x, y + sh, sw, ch);
        part(graphics, xOff + width - sw, yOff + sh, sw, innerH, x + w - sw, y + sh, sw, ch);
        // center
        part(graphics, xOff + sw, yOff + sh, innerW, innerH, x + sw, y + sh, cw, ch);
    }

    private static void part(GuiGraphics graphics, int x, int y, int width, int height, int u, int v, int uw, int vh) {
        if (width <= 0 || height <= 0) return;
        graphics.blit(TEXTURE, x, y, width, height, u, v, uw, vh, 256, 256);
    }

    /** Item with an optional count text, offset by one pixel like the original. */
    public static void drawItem(GuiGraphics graphics, int xOff, int yOff, ItemStack stack, String text) {
        Font font = Minecraft.getInstance().font;
        graphics.pose().pushPose();
        graphics.pose().translate(1.0F, 1.0F, 0.0F);
        if (stack != null && !stack.isEmpty()) {
            graphics.renderItem(stack, xOff, yOff);
            graphics.renderItemDecorations(font, stack, xOff, yOff, text == null || text.isEmpty() ? "" : text);
        } else if (text != null && !text.isEmpty()) {
            graphics.pose().translate(0.0F, 0.0F, 200.0F);
            graphics.drawString(font, text, xOff + 19 - 2 - font.width(text), yOff + 6 + 3, 0xFFFFFF, true);
        }
        graphics.pose().popPose();
    }
}
