package com.raishxn.gtna.common.machine.multiblock.part.ae;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import com.gregtechceu.gtceu.api.transfer.fluid.CustomFluidTank;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.common.machine.multiblock.part.DualHatchPartMachine;
import com.gregtechceu.gtceu.integration.ae2.gui.widget.list.AEListGridWidget;
import com.gregtechceu.gtceu.integration.ae2.machine.feature.IGridConnectedMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.trait.GridNodeHolder;
import com.gregtechceu.gtceu.integration.ae2.utils.KeyStorage;

import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.config.Actionable;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * ME Export Buffer: one part that is both the item bus and the fluid hatch of a multiblock and sends every
 * product to the ME network. Outputs never block the recipe: they go into long-counted key buffers (up to
 * {@link Long#MAX_VALUE} per item or fluid type) that are flushed to the network on the AE2 update interval.
 *
 * <p>
 * Same role as the GTMThings {@code me_export_buffer}; written for GTNA on top of the GTCEu (LGPL) ME output
 * bus and hatch, because GTMThings is not part of GT:IA. Unlike the GTCEu ME output hatch, fluid amounts are
 * not capped at {@code Integer.MAX_VALUE} per fluid.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GTNAMEExportBufferPartMachine extends DualHatchPartMachine
                                           implements IGridConnectedMachine, IMachineLife {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            GTNAMEExportBufferPartMachine.class, DualHatchPartMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    protected final GridNodeHolder nodeHolder;
    @DescSynced
    protected boolean isOnline;
    protected final IActionSource actionSource;

    // Assigned from the super constructor through createInventory/createTank: no field initializers here.
    @Persisted
    private KeyStorage itemBuffer;
    @Persisted
    private KeyStorage fluidBuffer;

    public GTNAMEExportBufferPartMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, GTValues.LuV, IO.OUT, args);
        this.nodeHolder = new GridNodeHolder(this);
        this.actionSource = IActionSource.ofMachine(nodeHolder.getMainNode()::getNode);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    /////////////////////////////////
    // ***** Machine LifeCycle ****//
    /////////////////////////////////

    @Override
    protected NotifiableItemStackHandler createInventory(Object... args) {
        this.itemBuffer = new KeyStorage();
        return new BufferItemHandler(this);
    }

    @Override
    protected NotifiableFluidTank createTank(int initialCapacity, int slots, Object... args) {
        this.fluidBuffer = new KeyStorage();
        return new BufferFluidTank(this);
    }

    @Override
    public void onMachineRemoved() {
        var grid = getMainNode().getGrid();
        if (grid == null) return;
        var inventory = grid.getStorageService().getInventory();
        for (var buffer : List.of(itemBuffer, fluidBuffer)) {
            for (var entry : buffer) {
                inventory.insert(entry.getKey(), entry.getLongValue(), Actionable.MODULATE, actionSource);
            }
        }
    }

    public long getBufferedAmount(AEKey key) {
        return (key instanceof AEFluidKey ? fluidBuffer : itemBuffer).storage.getLong(key);
    }

    /////////////////////////////////
    // ********** Sync ME *********//
    /////////////////////////////////

    @Override
    public IManagedGridNode getMainNode() {
        return nodeHolder.getMainNode();
    }

    @Override
    public boolean isOnline() {
        return isOnline;
    }

    @Override
    public void setOnline(boolean online) {
        this.isOnline = online;
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        IGridConnectedMachine.super.onMainNodeStateChanged(reason);
        updateInventorySubscription();
    }

    @Override
    public void onRotated(Direction oldFacing, Direction newFacing) {
        super.onRotated(oldFacing, newFacing);
        getMainNode().setExposedOnSides(EnumSet.of(newFacing));
    }

    @Override
    protected void updateInventorySubscription() {
        if (isWorkingEnabled() && isOnline() && (!itemBuffer.storage.isEmpty() || !fluidBuffer.storage.isEmpty())) {
            autoIOSubs = subscribeServerTick(autoIOSubs, this::autoIO);
        } else if (autoIOSubs != null) {
            autoIOSubs.unsubscribe();
            autoIOSubs = null;
        }
    }

    @Override
    protected void autoIO() {
        if (!shouldSyncME()) return;
        if (updateMEStatus()) {
            var grid = getMainNode().getGrid();
            if (grid != null) {
                var inventory = grid.getStorageService().getInventory();
                if (!itemBuffer.isEmpty()) itemBuffer.insertInventory(inventory, actionSource);
                if (!fluidBuffer.isEmpty()) fluidBuffer.insertInventory(inventory, actionSource);
            }
        }
        updateInventorySubscription();
    }

    // The part only exports to ME; a screwdriver must not swap it into a dual input hatch.
    @Override
    public boolean swapIO() {
        return false;
    }

    ///////////////////////////////
    // ********** GUI ***********//
    ///////////////////////////////

    @Override
    public Widget createUIWidget() {
        WidgetGroup group = new WidgetGroup(0, 0, 170, 140);
        group.addWidget(new LabelWidget(5, 0, () -> isOnline ?
                "gtceu.gui.me_network.online" :
                "gtceu.gui.me_network.offline"));
        group.addWidget(new LabelWidget(5, 10, "gtceu.gui.waiting_list"));
        group.addWidget(new AEListGridWidget.Item(5, 20, 3, itemBuffer));
        group.addWidget(new AEListGridWidget.Fluid(5, 80, 3, fluidBuffer));
        return group;
    }

    private static long add(KeyStorage buffer, AEKey key, long amount) {
        long old = buffer.storage.getLong(key);
        long added = Math.min(Long.MAX_VALUE - old, amount);
        if (added > 0) buffer.storage.put(key, old + added);
        return added;
    }

    ///////////////////////////////
    // ******** Handlers ********//
    ///////////////////////////////

    /** Recipe-only item output: every stack goes straight into {@link #itemBuffer}. */
    private class BufferItemHandler extends NotifiableItemStackHandler {

        BufferItemHandler(MetaMachine holder) {
            super(holder, 1, IO.OUT, IO.NONE, ignored -> new ItemDelegate());
            itemBuffer.setOnContentsChanged(this::onContentsChanged);
        }

        @Override
        public @Nullable List<Ingredient> handleRecipeInner(IO io, GTRecipe recipe, List<Ingredient> left,
                                                            boolean simulate) {
            if (io != IO.OUT) return left;
            boolean changed = false;
            for (var it = left.iterator(); it.hasNext();) {
                var ingredient = it.next();
                it.remove();
                if (ingredient.isEmpty()) continue;
                ItemStack output;
                int amount;
                if (ingredient instanceof IntProviderIngredient provider) {
                    if (simulate) continue;
                    provider.setItemStacks(null);
                    provider.setSampledCount(-1);
                    var items = provider.getItems();
                    if (items.length == 0) continue;
                    output = items[0];
                    amount = output.getCount();
                } else {
                    var items = ingredient.getItems();
                    if (items.length == 0) continue;
                    output = items[0];
                    amount = ingredient instanceof SizedIngredient sized ? sized.getAmount() : output.getCount();
                }
                if (simulate || output.isEmpty() || amount <= 0) continue;
                changed |= add(itemBuffer, AEItemKey.of(output), amount) > 0;
            }
            if (changed) itemBuffer.onChanged();
            return null;
        }

        @Override
        public @NotNull List<Object> getContents() {
            return Collections.emptyList();
        }

        @Override
        public double getTotalContentAmount() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }
    }

    /** Lets pipes and automation insert too; nothing can be extracted, the ME network takes it all. */
    private class ItemDelegate extends CustomItemStackHandler {

        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        public int getSlotLimit(int slot) {
            return Integer.MAX_VALUE;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return ItemStack.EMPTY;
        }

        @Override
        public void setStackInSlot(int slot, ItemStack stack) {}

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (stack.isEmpty()) return ItemStack.EMPTY;
            var key = AEItemKey.of(stack);
            long room = Long.MAX_VALUE - itemBuffer.storage.getLong(key);
            int accepted = (int) Math.min(room, stack.getCount());
            if (!simulate && accepted > 0) {
                add(itemBuffer, key, accepted);
                itemBuffer.onChanged();
            }
            return stack.copyWithCount(stack.getCount() - accepted);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }
    }

    /** Recipe-only fluid output with long amounts per fluid in {@link #fluidBuffer}. */
    private class BufferFluidTank extends NotifiableFluidTank {

        BufferFluidTank(MetaMachine holder) {
            super(holder, List.of(new FluidDelegate()), IO.OUT, IO.NONE);
            fluidBuffer.setOnContentsChanged(this::onContentsChanged);
            allowSameFluids = true;
        }

        @Override
        public @Nullable List<FluidIngredient> handleRecipeInner(IO io, GTRecipe recipe, List<FluidIngredient> left,
                                                                 boolean simulate) {
            if (io != IO.OUT) return left;
            boolean changed = false;
            for (var it = left.iterator(); it.hasNext();) {
                var ingredient = it.next();
                it.remove();
                if (ingredient.isEmpty() || simulate) continue;
                var fluids = ingredient.getStacks();
                if (fluids.length == 0 || fluids[0].isEmpty()) continue;
                var output = fluids[0];
                changed |= add(fluidBuffer, AEFluidKey.of(output.getFluid(), output.getTag()),
                        ingredient.getAmount()) > 0;
            }
            if (changed) fluidBuffer.onChanged();
            return null;
        }

        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public @NotNull List<Object> getContents() {
            return Collections.emptyList();
        }

        @Override
        public double getTotalContentAmount() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public @NotNull FluidStack getFluidInTank(int tank) {
            return FluidStack.EMPTY;
        }

        @Override
        public void setFluidInTank(int tank, @NotNull FluidStack fluidStack) {}

        @Override
        public int getTankCapacity(int tank) {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            return true;
        }
    }

    private class FluidDelegate extends CustomFluidTank {

        FluidDelegate() {
            super(0);
        }

        @Override
        public int getCapacity() {
            return Integer.MAX_VALUE;
        }

        @Override
        public void setFluid(FluidStack fluid) {}

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (resource.isEmpty()) return 0;
            var key = AEFluidKey.of(resource.getFluid(), resource.getTag());
            long room = Long.MAX_VALUE - fluidBuffer.storage.getLong(key);
            int accepted = (int) Math.min(room, resource.getAmount());
            if (action.execute() && accepted > 0) {
                add(fluidBuffer, key, accepted);
                fluidBuffer.onChanged();
            }
            return accepted;
        }

        @Override
        public boolean supportsFill(int tank) {
            return true;
        }

        @Override
        public boolean supportsDrain(int tank) {
            return false;
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return FluidStack.EMPTY;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return FluidStack.EMPTY;
        }
    }
}
