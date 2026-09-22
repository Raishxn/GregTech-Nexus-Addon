package com.raishxn.gtna.client.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * A client overlay that can be moved around in {@link HudEditorScreen} (GTOCore
 * {@code IMoveableHUD} parity, trimmed to what GTNA needs).
 *
 * <p>
 * The HUD itself decides where it lives (GTNA keeps the position in the client config, so it
 * survives restarts); the editor only forwards mouse events and asks for the bounds.
 */
@OnlyIn(Dist.CLIENT)
public interface IMoveableHud {

    Component getDisplayName();

    /** Draws the HUD; the caller has already decided that it should be visible. */
    void render(GuiGraphics graphics, int screenWidth, int screenHeight);

    /** The current (config) position and size in GUI pixels. */
    Rect2i getBounds(int screenWidth, int screenHeight);

    /** The position to outline while dragging: the pending offset applied on top of the bounds. */
    default Rect2i getAnchorBounds(int screenWidth, int screenHeight) {
        return getBounds(screenWidth, screenHeight);
    }

    boolean isEnabled();

    void setEnabled(boolean enabled);

    boolean mouseClicked(double mouseX, double mouseY, int button);

    boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY);

    boolean mouseReleased(double mouseX, double mouseY, int button);

    boolean isPositionDragging();

    /** Moves the HUD to {@code (x, y)} in GUI pixels, clamped to the screen. */
    void setTopLeftPosition(int x, int y, int screenWidth, int screenHeight);

    default boolean isMouseOver(double mouseX, double mouseY) {
        Minecraft mc = Minecraft.getInstance();
        return getBounds(mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight())
                .contains((int) mouseX, (int) mouseY);
    }

    static void drawOutline(GuiGraphics graphics, Rect2i bounds, int color) {
        if (bounds == null || bounds.getWidth() <= 0 || bounds.getHeight() <= 0) {
            return;
        }
        int left = bounds.getX();
        int top = bounds.getY();
        int right = left + bounds.getWidth() - 1;
        int bottom = top + bounds.getHeight() - 1;
        graphics.hLine(left, right, top, color);
        graphics.hLine(left, right, bottom, color);
        graphics.vLine(left, top, bottom, color);
        graphics.vLine(right, top, bottom, color);
    }
}
