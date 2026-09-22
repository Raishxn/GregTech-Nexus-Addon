package com.raishxn.gtna.common.machine.multiblock.module.steamElevator;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;

import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * GTNL {@code SteamBeeBreedingModule} port (LGPLv3, original by ScienceNotLeisure).
 *
 * <p>
 * <b>Documented deviation:</b> GTNL reads a Forestry queen from the controller slot, consumes two
 * stacks of 64 royal jelly and returns an ignoble copy of the queen (princess). Forestry is not
 * targeted for 1.20.1 and Productive Bees is <b>not</b> on the GTNA dev classpath, so this port keeps
 * the breeding contract — two parent bees plus feed become one new bee — with vanilla items: two
 * honeycomb (the parents) and eight honey bottles (the royal-jelly substitute) are consumed over a
 * long cycle to produce a bee spawn egg (the bred bee). No external bee mod is required or
 * referenced.
 *
 * <p>
 * GTNL numbers kept: tier 8, upkeep {@code V[6]} and the 12000-tick breeding cycle
 * ({@code mMaxProgresstime}).
 */
public class SteamBeeBreedingModule extends SteamElevatorModulePartMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            SteamBeeBreedingModule.class, SteamElevatorModulePartMachine.MANAGED_FIELD_HOLDER);

    /** GTNL {@code mMaxProgresstime}. */
    private static final int CYCLE_TICKS = 12000;
    private static final int PARENTS_CONSUMED = 2;
    private static final int FEED_CONSUMED = 8;

    public final NotifiableItemStackHandler inputInventory;
    public final NotifiableItemStackHandler outputInventory;

    @Persisted
    @DescSynced
    private int progress;

    public SteamBeeBreedingModule(IMachineBlockEntity holder, int tier) {
        super(holder, tier);
        this.inputInventory = new NotifiableItemStackHandler(this, 9, IO.IN);
        this.outputInventory = new NotifiableItemStackHandler(this, 9, IO.OUT);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public long getEnergyUsage() {
        // GTNL: GTValues.V[6].
        return GTValues.V[6];
    }

    @Override
    public void onElevatorTick(SteamElevator elevator) {
        if (!consumeEnergy(getEnergyUsage())) return;
        if (++progress < CYCLE_TICKS) return;
        progress = 0;

        if (count(Items.HONEYCOMB) < PARENTS_CONSUMED) return;
        if (count(Items.HONEY_BOTTLE) < FEED_CONSUMED) return;

        ItemStack bred = new ItemStack(Items.BEE_SPAWN_EGG);
        if (!hasOutputRoom(bred)) return;

        consume(Items.HONEYCOMB, PARENTS_CONSUMED);
        consume(Items.HONEY_BOTTLE, FEED_CONSUMED);
        insertOutputs(bred);
        markDirty();
    }

    /** Total number of {@code item} across the input inventory. */
    private int count(Item item) {
        int total = 0;
        for (int slot = 0; slot < inputInventory.getSlots(); slot++) {
            ItemStack stack = inputInventory.getStackInSlot(slot);
            if (stack.is(item)) total += stack.getCount();
        }
        return total;
    }

    /** Removes exactly {@code amount} of {@code item}; callers must have checked {@link #count}. */
    private void consume(Item item, int amount) {
        int remaining = amount;
        for (int slot = 0; slot < inputInventory.getSlots() && remaining > 0; slot++) {
            ItemStack stack = inputInventory.getStackInSlot(slot);
            if (!stack.is(item)) continue;
            int taken = Math.min(remaining, stack.getCount());
            inputInventory.extractItem(slot, taken, false);
            remaining -= taken;
        }
    }

    /** Dry-run insertion against a copy of the output slots so a partial insert never dupes. */
    private boolean hasOutputRoom(ItemStack... outputs) {
        ItemStack[] sim = new ItemStack[outputInventory.getSlots()];
        for (int i = 0; i < sim.length; i++) {
            sim[i] = outputInventory.getStackInSlot(i).copy();
        }
        for (ItemStack output : outputs) {
            int remaining = output.getCount();
            for (int slot = 0; slot < sim.length && remaining > 0; slot++) {
                ItemStack current = sim[slot];
                if (current.isEmpty()) {
                    int added = Math.min(remaining, output.getMaxStackSize());
                    sim[slot] = new ItemStack(output.getItem(), added);
                    remaining -= added;
                } else if (ItemStack.isSameItemSameTags(current, output)) {
                    int added = Math.min(remaining, current.getMaxStackSize() - current.getCount());
                    current.setCount(current.getCount() + added);
                    remaining -= added;
                }
            }
            if (remaining > 0) return false;
        }
        return true;
    }

    private void insertOutputs(ItemStack... outputs) {
        for (ItemStack output : outputs) {
            int remaining = output.getCount();
            for (int slot = 0; slot < outputInventory.getSlots() && remaining > 0; slot++) {
                ItemStack rest = outputInventory.insertItem(slot, new ItemStack(output.getItem(), remaining), false);
                remaining = rest.getCount();
            }
        }
    }

    @Override
    public Widget createUIWidget() {
        WidgetGroup group = screenGroup(150, 80);
        group.addWidget(new LabelWidget(5, 4, () -> "Bee Breeding tier §b" + getModuleTier()));
        for (int i = 0; i < 9; i++) {
            group.addWidget(new SlotWidget(inputInventory, i, 5 + (i % 3) * 18, 18 + (i / 3) * 18)
                    .setBackgroundTexture(GuiTextures.SLOT));
        }
        for (int i = 0; i < 9; i++) {
            group.addWidget(new SlotWidget(outputInventory, i, 89 + (i % 3) * 18, 18 + (i / 3) * 18)
                    .setBackgroundTexture(GuiTextures.SLOT));
        }
        group.addWidget(new LabelWidget(5, 66, () -> "Progress: §b" + (progress * 100 / CYCLE_TICKS) + "%"));
        return group;
    }
}
