package com.raishxn.gtna.api.machine.gui;

import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.ui.GTRecipeTypeUI;

import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

/** Keeps large output pools inside the recipe viewer while preserving every output slot. */
public class ScrollableRecipeTypeUI extends GTRecipeTypeUI {

    private static final int OUTPUT_HEIGHT = 6 * 18 + 8;

    public ScrollableRecipeTypeUI(GTRecipeType recipeType) {
        super(recipeType);
    }

    @Override
    protected WidgetGroup addInventorySlotGroup(boolean isOutputs, boolean isSteam, boolean isHighPressure) {
        WidgetGroup slots = super.addInventorySlotGroup(isOutputs, isSteam, isHighPressure);
        if (!isOutputs || slots.getSize().height <= OUTPUT_HEIGHT) {
            return slots;
        }
        var scroll = new DraggableScrollableWidgetGroup(0, 0, slots.getSize().width + 6, OUTPUT_HEIGHT);
        scroll.setYScrollBarWidth(6).setYBarStyle(new ColorRectTexture(0xFF404040),
                new ColorRectTexture(0xFFAAAAAA));
        for (var slot : java.util.List.copyOf(slots.widgets)) {
            slots.removeWidget(slot);
            scroll.addWidget(slot);
        }
        return scroll;
    }
}
