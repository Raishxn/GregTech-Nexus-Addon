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

import cy.jdkdigital.productivebees.common.item.SpawnEgg;
import cy.jdkdigital.productivebees.init.ModItems;

/**
 * GTNL {@code SteamBeeBreedingModule} port (LGPLv3, original by ScienceNotLeisure), ported onto
 * <b>Productive Bees</b>.
 *
 * <p>
 * GTNL reads a Forestry queen from the controller slot, spends 128 royal jelly and returns an ignoble
 * copy of the queen (a princess). Forestry is not targeted for 1.20.1, so the target is Productive Bees
 * instead. Productive Bees has no Forestry-style queen/ignoble-princess pair: a bee is carried as a
 * {@link SpawnEgg} item, so the GTNL "queen" is any Productive Bees bee spawn egg placed in the input
 * inventory (a catalyst, exactly like the GTNL controller slot) and the "ignoble princess" is a new
 * copy of that same bee. "Royal jelly" is Productive Bees' honey treat.
 *
 * <p>
 * The class references Productive Bees types directly, so it is only loaded when the mod is present:
 * {@code GTNAMachines2} registers this module behind a {@code ModList.get().isLoaded("productivebees")}
 * guard, and the module item/recipe simply do not exist otherwise.
 *
 * <p>
 * GTNL numbers kept: tier 8, upkeep {@code V[6]}, 128 feed per operation and the 12000-tick cycle
 * ({@code mMaxProgresstime}).
 */
public class SteamBeeBreedingModule extends SteamElevatorModuleMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            SteamBeeBreedingModule.class, SteamElevatorModuleMachine.MANAGED_FIELD_HOLDER);

    /** GTNL {@code mMaxProgresstime}. */
    private static final int CYCLE_TICKS = 12000;
    /** GTNL consumes 2 x 64 royal jelly; the Productive Bees equivalent is honey treats. */
    private static final int FEED_CONSUMED = 128;

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
    public long getSteamUpkeep() {
        // GTNL: GTValues.V[6].
        return GTValues.V[6];
    }

    @Override
    public void onElevatorTick(SteamElevator elevator) {
        if (!consumeSteam(getSteamUpkeep())) return;

        ItemStack queen = findQueen();
        if (queen.isEmpty() || count(ModItems.HONEY_TREAT.get()) < FEED_CONSUMED) {
            // GTNL reports NO_RECIPE with no queen / no royal jelly, so the cycle idles.
            if (progress != 0) {
                progress = 0;
                markDirty();
            }
            return;
        }

        if (++progress < CYCLE_TICKS) return;
        progress = 0;

        // The queen is a catalyst (GTNL reads, never depletes, the controller slot); the offspring is
        // a fresh copy of the same bee, NBT included so the bee type survives.
        ItemStack bred = queen.copyWithCount(1);
        if (!hasOutputRoom(bred)) return;

        consume(ModItems.HONEY_TREAT.get(), FEED_CONSUMED);
        insertOutputs(bred);
        markDirty();
    }

    /**
     * The first Productive Bees bee spawn egg in the input inventory (the GTNL controller queen). Every
     * Productive Bees bee item is a {@link SpawnEgg}.
     */
    private ItemStack findQueen() {
        for (int slot = 0; slot < inputInventory.getSlots(); slot++) {
            ItemStack stack = inputInventory.getStackInSlot(slot);
            if (!stack.isEmpty() && stack.getItem() instanceof SpawnEgg) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
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
    protected Widget createModuleUIWidget() {
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
