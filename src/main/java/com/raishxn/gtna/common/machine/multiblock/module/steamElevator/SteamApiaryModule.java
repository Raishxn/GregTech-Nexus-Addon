package com.raishxn.gtna.common.machine.multiblock.module.steamElevator;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.gui.widget.TankWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
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
import net.minecraftforge.fluids.capability.IFluidHandler;

/**
 * GTNL {@code SteamApiaryModule} port (LGPLv3, original by ScienceNotLeisure).
 *
 * <p>
 * <b>Documented deviation:</b> GTNL drives the Forestry/Binnie bee API (queen genomes, per-species
 * product chance tables, royal-jelly boosted simulations). Forestry is not targeted for 1.20.1 and
 * Productive Bees is <b>not</b> on the GTNA dev classpath (no dependency in {@code build.gradle}), so
 * this port keeps the apiary contract — a housed colony turns brood into comb and honey at a steam
 * upkeep — with vanilla bee products: honeycomb (the brood/"bee") and water are consumed every cycle
 * to produce more honeycomb plus honey bottles. No external bee mod is required or referenced.
 *
 * <p>
 * GTNL numbers mapped: tier 6 (the item's tier), upkeep {@code V[4] * 8} (GTNL
 * {@code V[4] * mMaxSlots} at the base colony size) and a 200-tick production cycle (GTNL's
 * {@code mMaxProgresstime = 6000} with drop acceleration, reduced to a per-batch cycle).
 */
public class SteamApiaryModule extends SteamElevatorModuleMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            SteamApiaryModule.class, SteamElevatorModuleMachine.MANAGED_FIELD_HOLDER);

    /** GTNL apiary water feed; the greenhouse also consumes water from a tank. */
    public static final int WATER_PER_CYCLE = 1000;
    private static final int CYCLE_TICKS = 200;
    private static final int COMBS_CONSUMED = 1;
    private static final int COMBS_PRODUCED = 2;
    private static final int HONEY_PRODUCED = 1;

    public final NotifiableItemStackHandler inputInventory;
    public final NotifiableItemStackHandler outputInventory;
    public final NotifiableFluidTank waterTank;

    @Persisted
    @DescSynced
    private int progress;

    public SteamApiaryModule(IMachineBlockEntity holder, int tier) {
        super(holder, tier);
        this.inputInventory = new NotifiableItemStackHandler(this, 9, IO.IN);
        this.outputInventory = new NotifiableItemStackHandler(this, 9, IO.OUT);
        this.waterTank = new NotifiableFluidTank(this, 1, 16_000, IO.IN);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public long getSteamUpkeep() {
        // GTNL: GTValues.V[4] * mMaxSlots at the base 8-bee colony.
        return GTValues.V[4] * 8L;
    }

    @Override
    public void onElevatorTick(SteamElevator elevator) {
        if (!consumeSteam(getSteamUpkeep())) return;
        if (++progress < CYCLE_TICKS) return;
        progress = 0;

        if (count(Items.HONEYCOMB) < COMBS_CONSUMED) return;
        if (waterTank.getFluidInTank(0).getAmount() < WATER_PER_CYCLE) return;

        ItemStack comb = new ItemStack(Items.HONEYCOMB, COMBS_PRODUCED);
        ItemStack honey = new ItemStack(Items.HONEY_BOTTLE, HONEY_PRODUCED);
        if (!hasOutputRoom(comb, honey)) return;

        waterTank.drainInternal(WATER_PER_CYCLE, IFluidHandler.FluidAction.EXECUTE);
        consume(Items.HONEYCOMB, COMBS_CONSUMED);
        insertOutputs(comb, honey);
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
    protected Widget createModuleUIWidget() {
        WidgetGroup group = screenGroup(150, 80);
        group.addWidget(new LabelWidget(5, 4, () -> "Apiary tier §b" + getModuleTier()));
        for (int i = 0; i < 9; i++) {
            group.addWidget(new SlotWidget(inputInventory, i, 5 + (i % 3) * 18, 18 + (i / 3) * 18)
                    .setBackgroundTexture(GuiTextures.SLOT));
        }
        group.addWidget(new TankWidget(waterTank.getStorages()[0], 63, 18, 18, 18, true, true));
        for (int i = 0; i < 9; i++) {
            group.addWidget(new SlotWidget(outputInventory, i, 89 + (i % 3) * 18, 18 + (i / 3) * 18)
                    .setBackgroundTexture(GuiTextures.SLOT));
        }
        group.addWidget(new LabelWidget(5, 66,
                () -> "Water: §b" + waterTank.getFluidInTank(0).getAmount() + " mB"));
        return group;
    }
}
