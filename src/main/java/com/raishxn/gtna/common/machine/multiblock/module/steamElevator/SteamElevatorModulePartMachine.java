package com.raishxn.gtna.common.machine.multiblock.module.steamElevator;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;

import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.network.chat.Component;

/**
 * Shared behaviour of the eight Steam Elevator modules.
 *
 * <p>
 * Mirrors GTNL {@code SteamElevatorModuleBase}: an internal steam/EU buffer sized
 * {@code 640000 * (1 << tier)} that the elevator charges from the steam it burns, plus a
 * connect/disconnect lifecycle. The GTNL modules are multiblocks; here each module is a part
 * machine, so {@code addedToController}/{@code removedFromController} replace
 * {@code connect()}/{@code disconnect()}.
 *
 * <p>
 * Deviation (documented): GTNL charges the module EU buffer from the elevator and the module then
 * burns it through its own {@code onRunningTick}. GTNA keeps the same buffer and charging order but
 * the elevator calls {@link #onElevatorTick(SteamElevator)} after delivering the share, because a
 * part machine has no recipe logic of its own.
 */
public abstract class SteamElevatorModulePartMachine extends MultiblockPartMachine
                                                     implements ISteamElevatorModule, IFancyUIMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            SteamElevatorModulePartMachine.class, MultiblockPartMachine.MANAGED_FIELD_HOLDER);

    private final int tier;

    @Persisted
    @DescSynced
    protected long storedEnergy;

    @Persisted
    @DescSynced
    protected boolean elevatorConnected;

    public SteamElevatorModulePartMachine(IMachineBlockEntity holder, int tier) {
        super(holder);
        this.tier = tier;
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public int getModuleTier() {
        return tier;
    }

    @Override
    public long getEnergyCapacity() {
        // GTNL SteamElevatorModuleBase#steamBufferSize.
        return 640000L * (1L << Math.min(30, tier));
    }

    @Override
    public long getEnergyStored() {
        return storedEnergy;
    }

    @Override
    public long receiveEnergy(long amount) {
        if (amount <= 0) return 0;
        long accepted = Math.min(amount, getEnergyCapacity() - storedEnergy);
        if (accepted > 0) {
            storedEnergy += accepted;
            markDirty();
        }
        return accepted;
    }

    @Override
    public boolean consumeEnergy(long amount) {
        if (amount <= 0) return true;
        if (storedEnergy < amount) return false;
        storedEnergy -= amount;
        markDirty();
        return true;
    }

    public boolean isElevatorConnected() {
        return elevatorConnected;
    }

    @Override
    public void addedToController(IMultiController controller) {
        super.addedToController(controller);
        if (controller instanceof SteamElevator) {
            elevatorConnected = true;
            markDirty();
        }
    }

    @Override
    public void removedFromController(IMultiController controller) {
        super.removedFromController(controller);
        if (controller instanceof SteamElevator) {
            elevatorConnected = false;
            markDirty();
        }
    }

    @Override
    public int getEffectRange() {
        return 0;
    }

    @Override
    public void onElevatorTick(SteamElevator elevator) {
        // Default: pay the upkeep; subclasses add their effect.
        consumeEnergy(getEnergyUsage());
    }

    @Override
    public Widget createUIWidget() {
        var group = new WidgetGroup(0, 0, 150, 40);
        group.addWidget(new LabelWidget(5, 5, () -> Component
                .translatable(getDefinition().getDescriptionId()).getString()));
        group.addWidget(new LabelWidget(5, 17, () -> "Tier: §b" + tier + " §r| Range: §b" + getEffectRange()));
        group.addWidget(new LabelWidget(5, 29, () -> "EU: §b" + storedEnergy + " / " + getEnergyCapacity()));
        return group;
    }

    /** Convenience for subclasses that show a custom screen background. */
    protected static WidgetGroup screenGroup(int width, int height) {
        WidgetGroup group = new WidgetGroup(0, 0, width, height);
        group.setBackground(GuiTextures.DISPLAY);
        return group;
    }
}
