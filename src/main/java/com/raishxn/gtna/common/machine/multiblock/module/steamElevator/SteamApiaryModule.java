package com.raishxn.gtna.common.machine.multiblock.module.steamElevator;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;

import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;

/**
 * GTNL {@code SteamApiaryModule} port (LGPLv3, original by ScienceNotLeisure).
 *
 * <p>
 * <b>Documented deviation:</b> GTNL drives the Forestry/Binnie bee API (queen genomes, per-species
 * product chance tables, royal-jelly boosted simulations). Forestry is not targeted for 1.20.1, so
 * this port keeps the apiary contract — a housed colony turns brood into comb and honey at a steam
 * upkeep — with vanilla bee products: honeycomb (the brood/"bee") from the module structure's
 * <b>input bus</b> plus water from its <b>input hatch</b> are consumed every cycle to produce more
 * honeycomb and honey bottles in the <b>output bus</b>.
 *
 * <p>
 * GTNL numbers mapped: tier 6 (the item's tier), upkeep {@code V[4] * 8} (GTNL
 * {@code V[4] * mMaxSlots} at the base colony size of 8) and GTNL's {@code mMaxProgresstime = 6000}
 * (the tooltip's "Fixed operating time: 300 seconds"). The per-cycle products are the vanilla
 * analogue of the bee drop table.
 */
public class SteamApiaryModule extends SteamElevatorModuleMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            SteamApiaryModule.class, SteamElevatorModuleMachine.MANAGED_FIELD_HOLDER);

    /** GTNL apiary water feed; the greenhouse also consumes water from a hatch. */
    public static final int WATER_PER_CYCLE = 1000;
    /** GTNL {@code mMaxProgresstime}: 300 seconds. */
    private static final int CYCLE_TICKS = 6000;
    private static final int COMBS_CONSUMED = 1;
    private static final int COMBS_PRODUCED = 2;
    private static final int HONEY_PRODUCED = 1;

    @Persisted
    @DescSynced
    private int progress;

    public SteamApiaryModule(IMachineBlockEntity holder, int tier) {
        super(holder, tier);
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
    public int getModuleProgress() {
        return progress;
    }

    @Override
    public int getModuleMaxProgress() {
        return CYCLE_TICKS;
    }

    private static FluidStack water() {
        return new FluidStack(Fluids.WATER, WATER_PER_CYCLE);
    }

    @Override
    public void onElevatorTick(SteamElevator elevator) {
        if (!consumeSteam(getSteamUpkeep())) return;
        if (++progress < CYCLE_TICKS) return;
        progress = 0;

        if (countItem(Items.HONEYCOMB) < COMBS_CONSUMED) return;
        if (countFluid(water()) < WATER_PER_CYCLE) return;

        ItemStack comb = new ItemStack(Items.HONEYCOMB, COMBS_PRODUCED);
        ItemStack honey = new ItemStack(Items.HONEY_BOTTLE, HONEY_PRODUCED);
        if (!canInsertItems(comb, honey)) return;

        drainFluid(water(), WATER_PER_CYCLE);
        consumeItem(Items.HONEYCOMB, COMBS_CONSUMED);
        insertItems(comb, honey);
        markDirty();
    }

    @Override
    protected Widget createModuleUIWidget() {
        WidgetGroup group = screenGroup(150, 64);
        group.addWidget(new LabelWidget(5, 4, () -> "Apiary tier §b" + getModuleTier()));
        group.addWidget(new LabelWidget(5, 16,
                () -> "Water in hatch: §b" + countFluid(water()) + " mB"));
        group.addWidget(new LabelWidget(5, 28,
                () -> "Progress: §b" + (progress * 100 / CYCLE_TICKS) + "%%"));
        group.addWidget(new LabelWidget(5, 40, () -> "§7Comb: input bus"));
        group.addWidget(new LabelWidget(5, 52, () -> "§7Water: input hatch"));
        return group;
    }
}
