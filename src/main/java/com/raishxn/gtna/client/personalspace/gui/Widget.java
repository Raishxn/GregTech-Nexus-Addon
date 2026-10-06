package com.raishxn.gtna.client.personalspace.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

/** Port of the original PersonalSpace widget tree; coordinates are relative to the parent widget. */
public class Widget {

    public Rectangle position = new Rectangle(0, 0, 1, 1);
    public boolean visible = true, enabled = true;
    public final List<Widget> children = new ArrayList<>();
    protected boolean dragged = false;

    public final void addChild(Widget widget) {
        if (widget != null) children.add(widget);
    }

    public final void update() {
        updateImpl();
        for (Widget child : children) child.update();
    }

    protected void updateImpl() {}

    public final boolean testPoint(int x, int y) {
        return visible && x >= position.x && y >= position.y && x < position.getMaxX() && y < position.getMaxY();
    }

    public final void draw(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        graphics.pose().pushPose();
        graphics.pose().translate(position.x, position.y, 0);
        if (visible) {
            drawImpl(graphics, mouseX, mouseY, partialTicks);
            for (Widget child : children) child.draw(graphics, mouseX - position.x, mouseY - position.y, partialTicks);
        }
        graphics.pose().popPose();
    }

    protected void drawImpl(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {}

    public final void drawForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        graphics.pose().pushPose();
        graphics.pose().translate(position.x, position.y, 0);
        if (visible) {
            drawForegroundImpl(graphics, mouseX, mouseY, partialTicks);
            for (Widget child : children) {
                child.drawForeground(graphics, mouseX - position.x, mouseY - position.y, partialTicks);
            }
        }
        graphics.pose().popPose();
    }

    protected void drawForegroundImpl(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {}

    public final boolean keyPressed(int key, int scanCode, int modifiers) {
        if (!visible) return false;
        if (keyPressedImpl(key, scanCode, modifiers)) return true;
        for (Widget child : children) if (child.keyPressed(key, scanCode, modifiers)) return true;
        return false;
    }

    protected boolean keyPressedImpl(int key, int scanCode, int modifiers) {
        return false;
    }

    public final boolean charTyped(char character, int modifiers) {
        if (!visible) return false;
        if (charTypedImpl(character, modifiers)) return true;
        for (Widget child : children) if (child.charTyped(character, modifiers)) return true;
        return false;
    }

    protected boolean charTypedImpl(char character, int modifiers) {
        return false;
    }

    public final boolean mouseClicked(int x, int y, int button) {
        if (!visible) return false;
        if (enabled && testPoint(x, y) && mouseClickedImpl(x, y, button)) {
            dragged = true;
            return true;
        } else if (enabled && !testPoint(x, y)) {
            mouseClickedOutsideImpl(x, y, button);
        }
        for (Widget child : children) {
            if (child.mouseClicked(x - position.x, y - position.y, button)) return true;
        }
        return false;
    }

    protected boolean mouseClickedImpl(int x, int y, int button) {
        return false;
    }

    protected void mouseClickedOutsideImpl(int x, int y, int button) {}

    public final boolean mouseReleased(int x, int y, int button) {
        if (!visible) return false;
        dragged = false;
        if (enabled && testPoint(x, y) && mouseReleasedImpl(x, y, button)) return true;
        for (Widget child : children) {
            if (child.mouseReleased(x - position.x, y - position.y, button)) return true;
        }
        return false;
    }

    protected boolean mouseReleasedImpl(int x, int y, int button) {
        return false;
    }

    public final boolean mouseDragged(int x, int y, int button) {
        if (!visible) return false;
        if ((enabled && testPoint(x, y) || dragged) && mouseDraggedImpl(x, y, button)) return true;
        for (Widget child : children) {
            if (child.mouseDragged(x - position.x, y - position.y, button)) return true;
        }
        return false;
    }

    protected boolean mouseDraggedImpl(int x, int y, int button) {
        return false;
    }

    public final void clickSound() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    /** Vanilla-style tooltip, one line per {@code \n}; the first line is white, the rest gray, like the original. */
    public static void drawTooltip(GuiGraphics graphics, int x, int y, String message) {
        String[] lines = message.split("\n");
        List<Component> components = new ArrayList<>();
        for (int i = 0; i < lines.length; i++) {
            components.add(Component.literal((i == 0 ? "§f" : "§7") + lines[i]));
        }
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 300);
        graphics.renderComponentTooltip(Minecraft.getInstance().font, components, x, y);
        graphics.pose().popPose();
    }
}
