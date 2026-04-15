package com.raishxn.gtna.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;

import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.raishxn.gtna.api.machine.IThreadModifierMachine;
import com.raishxn.gtna.config.GTNABalance;

public class ThreadPartMachine extends TieredIOPartMachine {

    private final int threadCount;

    public ThreadPartMachine(IMachineBlockEntity holder, int tier, Object... args) {
        super(holder, tier, IO.NONE);
        // Lógica Exponencial: 2^(Tier - 6) - 1
        // ZPM (Tier 7): 2^(1) - 1 = 1
        // UV (Tier 8): 2^(2) - 1 = 3
        // ...
        // MAX (Tier 14): 2^(8) - 1 = 255
        this.threadCount = GTNABalance.getThreadCount(tier);
    }

    public int getThreadCount() {
        return this.threadCount;
    }

    @Override
    public void addedToController(IMultiController controller) {
        super.addedToController(controller);
        if (controller instanceof IThreadModifierMachine threadMachine) {
            threadMachine.setThreadPartMachine(this);
        }
    }

    @Override
    public void removedFromController(IMultiController controller) {
        super.removedFromController(controller);
        if (controller instanceof IThreadModifierMachine threadMachine) {
            if (threadMachine.getThreadPartMachine() == this) {
                threadMachine.setThreadPartMachine(null);
            }
        }
    }

    @Override
    public Widget createUIWidget() {
        var group = new WidgetGroup(0, 0, 100, 20);
        group.addWidget(new LabelWidget(5, 5, () -> "Threads: §b+" + this.getThreadCount()));
        return group;
    }
}
