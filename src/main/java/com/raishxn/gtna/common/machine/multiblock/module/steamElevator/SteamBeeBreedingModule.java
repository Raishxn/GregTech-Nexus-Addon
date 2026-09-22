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
 * {@link SpawnEgg} item, so the GTNL "queen" is any Productive Bees bee spawn egg placed in the module
 * structure's <b>input bus</b> (a catalyst, exactly like the GTNL controller slot) and the "ignoble
 * princess" is a new copy of that same bee in the <b>output bus</b>. "Royal jelly" is Productive Bees'
 * honey treat.
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

    @Persisted
    @DescSynced
    private int progress;

    public SteamBeeBreedingModule(IMachineBlockEntity holder, int tier) {
        super(holder, tier);
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
    public int getModuleProgress() {
        return progress;
    }

    @Override
    public int getModuleMaxProgress() {
        return CYCLE_TICKS;
    }

    @Override
    public void onElevatorTick(SteamElevator elevator) {
        if (!consumeSteam(getSteamUpkeep())) return;

        ItemStack queen = findQueen();
        if (queen.isEmpty() || countItem(ModItems.HONEY_TREAT.get()) < FEED_CONSUMED) {
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
        if (!canInsertItems(bred)) return;

        consumeItem(ModItems.HONEY_TREAT.get(), FEED_CONSUMED);
        insertItems(bred);
        markDirty();
    }

    /**
     * The first Productive Bees bee spawn egg in the module's input bus (the GTNL controller queen).
     * Every Productive Bees bee item is a {@link SpawnEgg}.
     */
    private ItemStack findQueen() {
        return findItem(stack -> stack.getItem() instanceof SpawnEgg);
    }

    @Override
    protected Widget createModuleUIWidget() {
        WidgetGroup group = screenGroup(150, 64);
        group.addWidget(new LabelWidget(5, 4, () -> "Bee Breeding tier §b" + getModuleTier()));
        group.addWidget(new LabelWidget(5, 16,
                () -> "Progress: §b" + (progress * 100 / CYCLE_TICKS) + "%%"));
        group.addWidget(new LabelWidget(5, 28,
                () -> "Feed: §b" + countItem(ModItems.HONEY_TREAT.get()) + " §r/ " + FEED_CONSUMED));
        group.addWidget(new LabelWidget(5, 40, () -> "§7Bee + treats: input bus"));
        group.addWidget(new LabelWidget(5, 52, () -> "§7Bred bee: output bus"));
        return group;
    }
}
