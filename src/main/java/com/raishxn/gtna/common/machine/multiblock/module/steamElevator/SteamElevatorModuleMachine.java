package com.raishxn.gtna.common.machine.multiblock.module.steamElevator;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.UITemplate;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockDisplayText;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerList;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;

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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import com.raishxn.gtna.client.renderer.GTNATextures;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Shared behaviour of the Steam Elevator modules.
 *
 * <p>
 * Mirrors GTNL {@code SteamElevatorModuleBase}: every module is itself a {@code 1x5x2} multiblock
 * (structure {@code pattern/steam_elevator_module.mbs}, decoded like the host). The Steam Elevator
 * host scans its twelve fixed module slots and connects the <b>formed</b> module controllers it
 * finds there; each bound module then drives its own effect every server tick and pays its steam
 * upkeep from the formed structure's steam input hatches.
 *
 * <p>
 * <b>All item and fluid IO goes through the module structure's own hatches</b> — the input bus, the
 * output bus, the input hatch and the output hatch — exactly like a normal GT multiblock. The module
 * controller keeps no private inventory: {@link #countItem}, {@link #consumeItem},
 * {@link #canInsertItems}, {@link #insertItems}, {@link #countFluid}, {@link #drainFluid},
 * {@link #canInsertFluid} and {@link #insertFluid} read and write the parts placed in the 1x5x2.
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
    /** Item input buses (steam or LV) placed in this module's own structure. */
    private final List<NotifiableItemStackHandler> itemInputs = new ArrayList<>();
    /** Item output buses placed in this module's own structure. */
    private final List<NotifiableItemStackHandler> itemOutputs = new ArrayList<>();
    /** Fluid input hatches placed in this module's own structure (steam excluded). */
    private final List<NotifiableFluidTank> fluidInputs = new ArrayList<>();
    /** Fluid output hatches placed in this module's own structure. */
    private final List<NotifiableFluidTank> fluidOutputs = new ArrayList<>();

    @Persisted
    @DescSynced
    protected boolean elevatorConnected;

    @Nullable
    private SteamElevator host;
    @Nullable
    private TickableSubscription tickSubscription;

    public SteamElevatorModuleMachine(IMachineBlockEntity holder, int tier) {
        super(holder);
        this.tier = tier;
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            // Each module drives its own effect while it is formed and bound, instead of relying on
            // the host's tick: a module that is connected in game must work even if the host tower
            // is not being ticked for any reason, and the upkeep can only be charged once.
            tickSubscription = subscribeServerTick(this::moduleTick);
        }
    }

    @Override
    public void onUnload() {
        if (tickSubscription != null) {
            tickSubscription.unsubscribe();
            tickSubscription = null;
        }
        disconnectFromHost();
        super.onUnload();
    }

    /** Applies this module's effect (and pays its upkeep) once per server tick while bound. */
    private void moduleTick() {
        if (isRemote() || !isFormed() || !elevatorConnected) return;
        SteamElevator currentHost = host;
        if (currentHost == null) return;
        onElevatorTick(currentHost);
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
        itemInputs.clear();
        itemOutputs.clear();
        fluidInputs.clear();
        fluidOutputs.clear();
        for (IMultiPart part : getParts()) {
            Block block = part.self().getDefinition().getBlock();
            boolean steamPart = PartAbility.STEAM.isApplicable(block);
            boolean itemInput = PartAbility.IMPORT_ITEMS.isApplicable(block) ||
                    PartAbility.STEAM_IMPORT_ITEMS.isApplicable(block);
            boolean itemOutput = PartAbility.EXPORT_ITEMS.isApplicable(block) ||
                    PartAbility.STEAM_EXPORT_ITEMS.isApplicable(block);
            boolean fluidInput = PartAbility.IMPORT_FLUIDS.isApplicable(block);
            boolean fluidOutput = PartAbility.EXPORT_FLUIDS.isApplicable(block);
            for (RecipeHandlerList handlerList : part.getRecipeHandlers()) {
                if (!handlerList.isValid(IO.IN) && !handlerList.isValid(IO.OUT)) continue;
                for (IRecipeHandler<?> handler : handlerList.getCapability(ItemRecipeCapability.CAP)) {
                    if (!(handler instanceof NotifiableItemStackHandler items)) continue;
                    if (itemInput && handlerList.isValid(IO.IN)) itemInputs.add(items);
                    if (itemOutput && handlerList.isValid(IO.OUT)) itemOutputs.add(items);
                }
                for (IRecipeHandler<?> handler : handlerList.getCapability(FluidRecipeCapability.CAP)) {
                    if (!(handler instanceof NotifiableFluidTank tank)) continue;
                    if (steamPart) {
                        if (handlerList.isValid(IO.IN)) steamTanks.add(tank);
                    } else {
                        if (fluidInput && handlerList.isValid(IO.IN)) fluidInputs.add(tank);
                        if (fluidOutput && handlerList.isValid(IO.OUT)) fluidOutputs.add(tank);
                    }
                }
            }
        }
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        steamTanks.clear();
        itemInputs.clear();
        itemOutputs.clear();
        fluidInputs.clear();
        fluidOutputs.clear();
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

    // ------------------------------------------------------------------
    // Item / fluid IO through the module structure's own hatches.
    // ------------------------------------------------------------------

    /** The module's own input buses (steam or LV), read-only view for per-slot iteration. */
    protected List<NotifiableItemStackHandler> inputItemHandlers() {
        return itemInputs;
    }

    /** The module's own output buses, read-only view for per-slot iteration. */
    protected List<NotifiableItemStackHandler> outputItemHandlers() {
        return itemOutputs;
    }

    /** The module's own fluid input hatches (steam excluded). */
    protected List<NotifiableFluidTank> fluidInputTanks() {
        return fluidInputs;
    }

    /** The module's own fluid output hatches. */
    protected List<NotifiableFluidTank> fluidOutputTanks() {
        return fluidOutputs;
    }

    /** Total input items matching {@code filter} across the module's input buses. */
    protected int countItem(Predicate<ItemStack> filter) {
        int total = 0;
        for (NotifiableItemStackHandler handler : itemInputs) {
            for (int slot = 0; slot < handler.getSlots(); slot++) {
                ItemStack stack = handler.getStackInSlot(slot);
                if (!stack.isEmpty() && filter.test(stack)) total += stack.getCount();
            }
        }
        return total;
    }

    /** Convenience for the common "N of this exact item" case. */
    protected int countItem(Item item) {
        return countItem(stack -> stack.is(item));
    }

    /** Removes up to {@code amount} of matching items from the module's input buses. */
    protected void consumeItem(Predicate<ItemStack> filter, int amount) {
        int remaining = amount;
        for (NotifiableItemStackHandler handler : itemInputs) {
            for (int slot = 0; slot < handler.getSlots() && remaining > 0; slot++) {
                ItemStack stack = handler.getStackInSlot(slot);
                if (stack.isEmpty() || !filter.test(stack)) continue;
                int taken = Math.min(remaining, stack.getCount());
                handler.extractItemInternal(slot, taken, false);
                remaining -= taken;
            }
            if (remaining <= 0) break;
        }
    }

    protected void consumeItem(Item item, int amount) {
        consumeItem(stack -> stack.is(item), amount);
    }

    /** The first input-bus stack matching {@code filter}, or empty. */
    protected ItemStack findItem(Predicate<ItemStack> filter) {
        for (NotifiableItemStackHandler handler : itemInputs) {
            for (int slot = 0; slot < handler.getSlots(); slot++) {
                ItemStack stack = handler.getStackInSlot(slot);
                if (!stack.isEmpty() && filter.test(stack)) return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    /** The controller circuit configuration found in the module's input buses, or 0. */
    protected int findCircuit() {
        for (NotifiableItemStackHandler handler : itemInputs) {
            for (int slot = 0; slot < handler.getSlots(); slot++) {
                ItemStack stack = handler.getStackInSlot(slot);
                if (IntCircuitBehaviour.isIntegratedCircuit(stack)) {
                    return IntCircuitBehaviour.getCircuitConfiguration(stack);
                }
            }
        }
        return 0;
    }

    /** Dry-run of {@link #insertItems} over the output buses; false means something would be lost. */
    protected boolean canInsertItems(List<ItemStack> stacks) {
        List<ItemStack[]> snapshot = new ArrayList<>();
        for (NotifiableItemStackHandler handler : itemOutputs) {
            ItemStack[] slots = new ItemStack[handler.getSlots()];
            for (int slot = 0; slot < slots.length; slot++) {
                slots[slot] = handler.getStackInSlot(slot).copy();
            }
            snapshot.add(slots);
        }
        for (ItemStack stack : stacks) {
            int remaining = stack.getCount();
            for (ItemStack[] slots : snapshot) {
                for (int slot = 0; slot < slots.length && remaining > 0; slot++) {
                    ItemStack current = slots[slot];
                    if (current.isEmpty()) {
                        int added = Math.min(remaining, stack.getMaxStackSize());
                        slots[slot] = stack.copyWithCount(added);
                        remaining -= added;
                    } else if (ItemStack.isSameItemSameTags(current, stack)) {
                        int added = Math.min(remaining, current.getMaxStackSize() - current.getCount());
                        current.grow(added);
                        remaining -= added;
                    }
                }
                if (remaining <= 0) break;
            }
            if (remaining > 0) return false;
        }
        return true;
    }

    protected boolean canInsertItems(ItemStack... stacks) {
        return canInsertItems(List.of(stacks));
    }

    /** Inserts into the module's output buses; callers should have checked {@link #canInsertItems}. */
    protected void insertItems(List<ItemStack> stacks) {
        for (ItemStack stack : stacks) {
            int remaining = stack.getCount();
            for (NotifiableItemStackHandler handler : itemOutputs) {
                for (int slot = 0; slot < handler.getSlots() && remaining > 0; slot++) {
                    ItemStack rest = handler.insertItemInternal(slot, stack.copyWithCount(remaining), false);
                    remaining = rest.getCount();
                }
                if (remaining <= 0) break;
            }
        }
    }

    protected void insertItems(ItemStack... stacks) {
        insertItems(List.of(stacks));
    }

    /** Total input fluid matching {@code fluid}'s type across the module's fluid input hatches. */
    protected int countFluid(FluidStack fluid) {
        int total = 0;
        for (NotifiableFluidTank tank : fluidInputs) {
            for (int i = 0; i < tank.getTanks(); i++) {
                FluidStack stored = tank.getFluidInTank(i);
                if (!stored.isEmpty() && stored.isFluidEqual(fluid)) total += stored.getAmount();
            }
        }
        return total;
    }

    /** Total fluid of any type across the module's fluid input hatches (UI diagnostics). */
    protected int totalInputFluid() {
        int total = 0;
        for (NotifiableFluidTank tank : fluidInputs) {
            for (int i = 0; i < tank.getTanks(); i++) {
                total += tank.getFluidInTank(i).getAmount();
            }
        }
        return total;
    }

    /** Drains exactly {@code amount} of {@code fluid}; returns false (and drains nothing) if short. */
    protected boolean drainFluid(FluidStack fluid, int amount) {
        if (countFluid(fluid) < amount) return false;
        int remaining = amount;
        for (NotifiableFluidTank tank : fluidInputs) {
            if (remaining <= 0) break;
            FluidStack drained = tank.drainInternal(new FluidStack(fluid, remaining),
                    IFluidHandler.FluidAction.EXECUTE);
            remaining -= drained.getAmount();
        }
        return true;
    }

    /** True when the module's fluid output hatches can take all of {@code fluid}. */
    protected boolean canInsertFluid(FluidStack fluid) {
        int accepted = 0;
        for (NotifiableFluidTank tank : fluidOutputs) {
            accepted += tank.fillInternal(fluid.copy(), IFluidHandler.FluidAction.SIMULATE);
            if (accepted >= fluid.getAmount()) return true;
        }
        return accepted >= fluid.getAmount();
    }

    /** Inserts into the module's fluid output hatches; should be preceded by {@link #canInsertFluid}. */
    protected void insertFluid(FluidStack fluid) {
        int remaining = fluid.getAmount();
        for (NotifiableFluidTank tank : fluidOutputs) {
            if (remaining <= 0) break;
            int filled = tank.fillInternal(new FluidStack(fluid, remaining), IFluidHandler.FluidAction.EXECUTE);
            remaining -= filled;
        }
    }

    @Override
    public void onElevatorTick(SteamElevator elevator) {
        // Default: pay the upkeep; subclasses add their effect.
        consumeSteam(getSteamUpkeep());
    }

    /** True when this module can pay its steam upkeep this tick. */
    protected boolean hasUpkeepSteam() {
        long upkeep = getSteamUpkeep();
        if (upkeep <= 0) return true;
        long available = getStoredSteam();
        if (host != null) available += host.getAvailableSteam();
        return available >= upkeep;
    }

    /**
     * A module is "active" once it is formed, bound to the host and has the steam to pay its upkeep,
     * so the standard multiblock "Running"/"Idling" display (and the active machine texture) match
     * the other machines instead of always reading as working.
     */
    @Override
    public boolean isActive() {
        return isFormed() && elevatorConnected && hasUpkeepSteam();
    }

    // ------------------------------------------------------------------
    // GUI: the IDisplayUIMachine panel plus the module-specific widget.
    // ------------------------------------------------------------------

    @Override
    public void addDisplayText(java.util.List<Component> textList) {
        IDisplayUIMachine.super.addDisplayText(textList);
        MultiblockDisplayText.builder(textList, isFormed())
                .setWorkingStatus(true, isActive())
                .addWorkingStatusLine();
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
                // The addon logo in the bottom-right corner (GTNL convention). Added to the ModularUI,
                // not the scrollable screen group, whose scissor would clip its right/bottom edge.
                .widget(GTNATextures.logo(151, 107))
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
