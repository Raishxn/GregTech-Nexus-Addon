package com.raishxn.gtna.common.machine.multiblock.module.steamElevator;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.UITemplate;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import org.jetbrains.annotations.Nullable;

/**
 * Shared behaviour of the Steam Elevator modules.
 *
 * <p>
 * Mirrors GTNL {@code SteamElevatorModuleBase}: every module is itself a {@code 1x5x2} multiblock
 * (structure {@code pattern/steam_elevator_module.mbs}, decoded like the host) with an internal
 * steam/EU buffer sized {@code 640000 * (1 << tier)}. The Steam Elevator host scans its twelve
 * fixed module slots, connects the <b>formed</b> module controllers it finds there and charges their
 * buffers from the steam it burns; the module then pays its upkeep and applies its effect in
 * {@link #onElevatorTick(SteamElevator)}.
 *
 * <p>
 * GTNL's modules are multiblocks that double as hatches inside the elevator. GTNA keeps the
 * multiblock shape and the fixed host-side slots, but the connection is a GTNA-native host/module
 * link ({@link #connectToHost}/{@link #disconnectFromHost}) instead of GTNL's structure-library
 * hatch element: only a fully formed module counts, so a stray block or part in a module slot is
 * ignored.
 */
public abstract class SteamElevatorModuleMachine extends WorkableMultiblockMachine
                                                 implements ISteamElevatorModule, IDisplayUIMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            SteamElevatorModuleMachine.class, WorkableMultiblockMachine.MANAGED_FIELD_HOLDER);

    private final int tier;

    @Persisted
    @DescSynced
    protected long storedEnergy;

    @Persisted
    @DescSynced
    protected boolean elevatorConnected;

    @Nullable
    private SteamElevator host;

    public SteamElevatorModuleMachine(IMachineBlockEntity holder, int tier) {
        super(holder);
        this.tier = tier;
    }

    @Override
    protected RecipeLogic createRecipeLogic(Object... args) {
        // Module effects are driven by the host, not by recipes; keep the default logic inert so it
        // never indexes the empty recipe-type array.
        return new SteamElevator.InertRecipeLogic(this);
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

    /** Binds this module to the elevator that owns its slot; called by the host's slot scan. */
    public void connectToHost(SteamElevator elevator) {
        if (this.host != null && this.host != elevator) {
            this.host.removeModule(this);
        }
        this.host = elevator;
        this.elevatorConnected = true;
        markDirty();
    }

    /** Unbinds the module and cleans up its world effect; safe to call repeatedly. */
    public void disconnectFromHost() {
        SteamElevator previous = this.host;
        this.host = null;
        this.elevatorConnected = false;
        if (previous != null) {
            previous.removeModule(this);
        }
        onElevatorStop();
        markDirty();
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        disconnectFromHost();
    }

    @Override
    public void onUnload() {
        disconnectFromHost();
        super.onUnload();
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

    // ------------------------------------------------------------------
    // GUI: the IDisplayUIMachine panel plus the module-specific widget.
    // ------------------------------------------------------------------

    @Override
    public void addDisplayText(java.util.List<Component> textList) {
        IDisplayUIMachine.super.addDisplayText(textList);
        textList.add(Component.translatable("gtna.machine.steam_elevator_module.tier", tier));
        textList.add(Component.translatable("gtna.machine.steam_elevator_module.energy", storedEnergy,
                getEnergyCapacity()));
        textList.add(Component.translatable(elevatorConnected ?
                "gtna.machine.steam_elevator_module.connected" :
                "gtna.machine.steam_elevator_module.disconnected"));
    }

    @Override
    public ModularUI createUI(Player player) {
        var screen = new DraggableScrollableWidgetGroup(7, 4, 162, 121).setBackground(getScreenTexture());
        screen.addWidget(new LabelWidget(4, 5, self().getBlockState().getBlock().getDescriptionId()));
        screen.addWidget(new ComponentPanelWidget(4, 17, this::addDisplayText)
                .textSupplier(self().getLevel().isClientSide ? null : this::addDisplayText)
                .setMaxWidthLimit(150)
                .clickHandler(this::handleDisplayClick));
        Widget moduleWidget = createModuleUIWidget();
        if (moduleWidget != null) {
            moduleWidget.setSelfPosition(4, 58);
            screen.addWidget(moduleWidget);
        }
        return new ModularUI(176, 216, this, player)
                .background(GuiTextures.BACKGROUND)
                .widget(screen)
                .widget(UITemplate.bindPlayerInventory(player.getInventory(), GuiTextures.SLOT, 7, 134, true));
    }

    /** Subclass-specific controls (tanks, slots, weather button); may be {@code null}. */
    @Nullable
    protected Widget createModuleUIWidget() {
        return null;
    }

    /** Convenience for subclasses that show a custom screen background. */
    protected static com.lowdragmc.lowdraglib.gui.widget.WidgetGroup screenGroup(int width, int height) {
        var group = new com.lowdragmc.lowdraglib.gui.widget.WidgetGroup(0, 0, width, height);
        group.setBackground(GuiTextures.DISPLAY);
        return group;
    }
}
