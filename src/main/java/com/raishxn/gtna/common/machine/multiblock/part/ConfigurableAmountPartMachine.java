package com.raishxn.gtna.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.gui.widget.IntInputWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;

import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Base for GTNA hatches whose effect is a player-adjustable amount, mirroring GTOCore's
 * {@code WorkableAmountConfigurationPartMachine}: the value is persisted, synced and edited through
 * an {@link IntInputWidget}. The default is the tier's best value (the minimum); players can dial it
 * back towards {@code maxAmount} (usually 100, i.e. "no effect").
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ConfigurableAmountPartMachine extends MultiblockPartMachine implements ITieredMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            ConfigurableAmountPartMachine.class, MultiblockPartMachine.MANAGED_FIELD_HOLDER);

    private final int tier;
    private final int minAmount;
    private final int maxAmount;

    @Persisted
    @DescSynced
    private int currentAmount;

    protected ConfigurableAmountPartMachine(IMachineBlockEntity holder, int tier, int minAmount, int maxAmount) {
        super(holder);
        this.tier = tier;
        this.minAmount = Math.min(minAmount, maxAmount);
        this.maxAmount = Math.max(minAmount, maxAmount);
        this.currentAmount = this.minAmount;
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public int getTier() {
        return tier;
    }

    public int getMinAmount() {
        return minAmount;
    }

    public int getMaxAmount() {
        return maxAmount;
    }

    public int getCurrentAmount() {
        return Math.max(minAmount, Math.min(maxAmount, currentAmount));
    }

    public void setCurrentAmount(int value) {
        int clamped = Math.max(minAmount, Math.min(maxAmount, value));
        if (clamped != currentAmount) {
            currentAmount = clamped;
            markDirty();
            onAmountChanged();
        }
    }

    /** Hook for subclasses that cache something derived from the amount. */
    protected void onAmountChanged() {}

    @Override
    public Widget createUIWidget() {
        var group = new WidgetGroup(0, 0, 120, 42);
        group.addWidget(new LabelWidget(4, 4, () -> getBlockState().getBlock().getDescriptionId()));
        group.addWidget(new IntInputWidget(4, 20, 60, 14, this::getCurrentAmount, this::setCurrentAmount)
                .setMin(minAmount)
                .setMax(maxAmount));
        return group;
    }
}
