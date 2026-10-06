package com.raishxn.gtna.client.personalspace.gui;

import com.raishxn.gtna.common.world.personalspace.PersonalSpaceSettings.DaylightCycle;

import java.awt.Rectangle;
import java.util.List;

public class WCycleButton extends WButton {

    public record ButtonState(DaylightCycle cycle, Icons icon) {}

    private final List<ButtonState> states;
    private int currentIndex;

    public WCycleButton(Rectangle position, String text, boolean dropShadow, int color, List<ButtonState> states,
                        int initialIndex, Runnable onClick) {
        super(position, text, dropShadow, color, Icons.CHECKMARK, onClick);
        this.states = states;
        this.currentIndex = initialIndex;
        this.buttonIcon = states.get(currentIndex).icon();
    }

    public DaylightCycle getState() {
        return states.get(currentIndex).cycle();
    }

    private void next() {
        if (++currentIndex >= states.size()) currentIndex = 0;
        buttonIcon = states.get(currentIndex).icon();
    }

    @Override
    protected boolean mouseClickedImpl(int x, int y, int button) {
        next();
        return super.mouseClickedImpl(x, y, button);
    }
}
