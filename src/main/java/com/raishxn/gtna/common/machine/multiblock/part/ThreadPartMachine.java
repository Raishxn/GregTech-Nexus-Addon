package com.raishxn.gtna.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.widget.IntInputWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;

import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.MethodsReturnNonnullByDefault;

import com.raishxn.gtna.api.machine.IThreadModifierMachine;
import com.raishxn.gtna.config.GTNABalance;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Adds parallel, different recipes to a machine on the GTNA multiple-recipes base. The tier sets the
 * maximum thread count ({@code 2^(tier-6)-1} extra) and the player can dial the active count down
 * (GTOCore {@code WorkableAmountConfigurationPartMachine} parity).
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ThreadPartMachine extends TieredIOPartMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            ThreadPartMachine.class, TieredIOPartMachine.MANAGED_FIELD_HOLDER);

    private final int maxThreads;

    @Persisted
    @DescSynced
    private int currentThreads;

    public ThreadPartMachine(IMachineBlockEntity holder, int tier, Object... args) {
        super(holder, tier, IO.NONE);
        // Exponential: 2^(tier - 6) - 1 (ZPM: +1, UV: +3, ..., MAX: +255).
        this.maxThreads = GTNABalance.getThreadCount(tier);
        this.currentThreads = this.maxThreads;
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    public int getMaxThreads() {
        return this.maxThreads;
    }

    public int getThreadCount() {
        return Math.max(0, Math.min(maxThreads, this.currentThreads));
    }

    public void setThreadCount(int value) {
        int clamped = Math.max(0, Math.min(maxThreads, value));
        if (clamped != this.currentThreads) {
            this.currentThreads = clamped;
            markDirty();
        }
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
        var group = new WidgetGroup(0, 0, 150, 42);
        group.addWidget(new LabelWidget(5, 4, () -> "Threads: §b+" + getThreadCount()));
        group.addWidget(new IntInputWidget(5, 20, 60, 14, this::getThreadCount, this::setThreadCount)
                .setMin(0)
                .setMax(maxThreads));
        return group;
    }
}
