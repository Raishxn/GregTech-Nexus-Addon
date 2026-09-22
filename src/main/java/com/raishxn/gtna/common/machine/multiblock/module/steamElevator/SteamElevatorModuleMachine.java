package com.raishxn.gtna.common.machine.multiblock.module.steamElevator;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.UITemplate;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.common.data.GTMaterials;

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
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared behaviour of the Steam Elevator modules.
 *
 * <p>
 * Mirrors GTNL {@code SteamElevatorModuleBase}: every module is itself a {@code 1x5x2} multiblock
 * (structure {@code pattern/steam_elevator_module.mbs}, decoded like the host). The Steam Elevator
 * host scans its twelve fixed module slots, connects the <b>formed</b> module controllers it finds
 * there and lets each module pay its steam upkeep from the formed structure's steam input hatches;
 * the module then applies its effect in {@link #onElevatorTick(SteamElevator)}.
 *
 * <p>
 * There is <b>no EU buffer</b> here. A module draws its upkeep from the steam input hatches placed in
 * its <b>own</b> structure first (GTNL's module shell accepts steam hatches) and from the host
 * structure's steam hatches for the remainder, draining exactly what it pays so no steam is voided.
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

    /** Steam input hatches placed in this module's own structure. */
    private final List<NotifiableFluidTank> steamTanks = new ArrayList<>();

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
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    protected RecipeLogic createRecipeLogic(Object... args) {
        // Module effects are driven by the host, not by recipes; keep the default logic inert so it
        // never indexes the empty recipe-type array.
        return new SteamElevator.InertRecipeLogic(this);
    }

    @Override
    public int getModuleTier() {
        return tier;
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        steamTanks.clear();
        for (var part : getParts()) {
            if (!PartAbility.STEAM.isApplicable(part.self().getDefinition().getBlock())) continue;
            for (var handlerList : part.getRecipeHandlers()) {
                if (!handlerList.isValid(IO.IN)) continue;
                for (var fluidHandler : handlerList.getCapability(FluidRecipeCapability.CAP)) {
                    if (fluidHandler instanceof NotifiableFluidTank tank &&
                            tank.isFluidValid(0, GTMaterials.Steam.getFluid(1))) {
                        steamTanks.add(tank);
                    }
                }
            }
        }
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        steamTanks.clear();
        disconnectFromHost();
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
    public void onUnload() {
        disconnectFromHost();
        super.onUnload();
    }

    @Override
    public int getEffectRange() {
        return 0;
    }

    // ------------------------------------------------------------------
    // Steam upkeep: exact, void-free accounting.
    // ------------------------------------------------------------------

    /** Total steam currently held by this module's own input hatches. */
    public long getStoredSteam() {
        long total = 0;
        for (NotifiableFluidTank tank : steamTanks) {
            total += tank.getFluidInTank(0).getAmount();
        }
        return total;
    }

    /** Drains up to {@code amount} from this module's own hatches; returns the amount drained. */
    private long drainOwnSteam(long amount) {
        long remaining = amount;
        for (NotifiableFluidTank tank : steamTanks) {
            if (remaining <= 0) break;
            FluidStack drained = tank.drainInternal((int) Math.min(remaining, Integer.MAX_VALUE),
                    IFluidHandler.FluidAction.EXECUTE);
            remaining -= drained.getAmount();
        }
        return amount - remaining;
    }

    /**
     * Pays {@code amount} of steam, drawing from this module's own hatches first and the host's
     * steam hatches for the rest. The availability of both pools is checked <b>before</b> anything
     * is drained, so a module can never pay a partial upkeep and void the difference.
     */
    protected boolean consumeSteam(long amount) {
        if (amount <= 0) return true;
        long own = getStoredSteam();
        long hostAvailable = host != null ? host.getAvailableSteam() : 0L;
        if (own + hostAvailable < amount) return false;

        long fromOwn = Math.min(own, amount);
        drainOwnSteam(fromOwn);
        long fromHost = amount - fromOwn;
        if (fromHost > 0 && host != null) {
            host.drainSteam(fromHost);
        }
        return true;
    }

    @Override
    public void onElevatorTick(SteamElevator elevator) {
        // Default: pay the upkeep; subclasses add their effect.
        consumeSteam(getSteamUpkeep());
    }

    // ------------------------------------------------------------------
    // GUI: the IDisplayUIMachine panel plus the module-specific widget.
    // ------------------------------------------------------------------

    @Override
    public void addDisplayText(java.util.List<Component> textList) {
        IDisplayUIMachine.super.addDisplayText(textList);
        textList.add(Component.translatable("gtna.machine.steam_elevator_module.tier", tier));
        textList.add(Component.translatable("gtna.machine.steam_elevator_module.upkeep", getSteamUpkeep(),
                getStoredSteam()));
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
