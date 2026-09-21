package com.raishxn.gtna.api.machine.gui;

import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.MachineModeFancyConfigurator;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;

import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.Widget;

/**
 * Machine-mode tab that shows a fixed number of rows with a scrollbar instead of every recipe type.
 *
 * <p>
 * GTCEu's stock {@link MachineModeFancyConfigurator} renders one button per recipe type with no
 * height limit; the Universal Factory has 32 of them, which overflows the screen and is unusable.
 */
public class ScrollableMachineModeFancyConfigurator extends MachineModeFancyConfigurator {

    private static final int VISIBLE_ROWS = 5;
    private static final int ROW_HEIGHT = 20;
    private static final int WIDTH = 140;

    public ScrollableMachineModeFancyConfigurator(IRecipeLogicMachine machine) {
        super(machine);
    }

    @Override
    public Widget createMainPage(FancyMachineUIWidget widget) {
        Widget full = super.createMainPage(widget);
        int rows = Math.min(machine.getRecipeTypes().length, VISIBLE_ROWS);
        var scroll = new DraggableScrollableWidgetGroup(0, 0, WIDTH, rows * ROW_HEIGHT + 4);
        scroll.addWidget(full);
        return scroll;
    }
}
