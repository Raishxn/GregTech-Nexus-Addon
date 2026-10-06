package com.raishxn.gtna.client.personalspace.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import java.awt.Rectangle;

/** Text field backed by a vanilla {@link EditBox} placed at the widget origin. */
public class WTextField extends Widget {

    public final EditBox textField;
    public String tooltip;

    public WTextField(Rectangle position, String text) {
        this.position = position;
        textField = new EditBox(Minecraft.getInstance().font, 0, 0, position.width, position.height,
                Component.empty());
        textField.setMaxLength(4096);
        textField.setValue(text);
        textField.moveCursorToStart();
    }

    public String getText() {
        return textField.getValue();
    }

    public void setText(String text) {
        textField.setValue(text);
    }

    @Override
    protected void updateImpl() {
        textField.tick();
        textField.setWidth(position.width);
        textField.setEditable(enabled);
        if (!enabled) textField.setFocused(false);
    }

    @Override
    protected void drawImpl(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        textField.setX(0);
        textField.setY(0);
        textField.render(graphics, mouseX - position.x, mouseY - position.y, partialTicks);
    }

    @Override
    protected void drawForegroundImpl(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (tooltip != null && !tooltip.isEmpty() && testPoint(mouseX, mouseY) && !WBlockDropdown.isAnyDropdownOpen()) {
            drawTooltip(graphics, mouseX - position.x, mouseY - position.y, tooltip);
        }
    }

    @Override
    protected boolean keyPressedImpl(int key, int scanCode, int modifiers) {
        return textField.isFocused() && textField.keyPressed(key, scanCode, modifiers);
    }

    @Override
    protected boolean charTypedImpl(char character, int modifiers) {
        return textField.isFocused() && textField.charTyped(character, modifiers);
    }

    /** Right click clears the field, as in the original. */
    @Override
    protected boolean mouseClickedImpl(int x, int y, int button) {
        textField.setFocused(true);
        if (button == 1) {
            textField.setValue("");
            textField.moveCursorToStart();
            return true;
        }
        textField.mouseClicked(x - position.x, y - position.y, button);
        return true;
    }

    @Override
    protected void mouseClickedOutsideImpl(int x, int y, int button) {
        textField.setFocused(false);
    }

    public boolean isFocused() {
        return textField.isFocused();
    }
}
