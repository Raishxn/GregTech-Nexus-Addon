package com.raishxn.gtna.common.machine.multiblock.noenergy;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;

import net.minecraft.MethodsReturnNonnullByDefault;

import com.raishxn.gtna.api.machine.IZeroEnergyMachine;
import com.raishxn.gtna.common.machine.multiblock.electric.WorkableElectricMultipleRecipesMachine;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * GTNA-native port of GTLsupb's Primitive Stone Furnace (LGPLv3): a no-energy multiblock that runs
 * FURNACE_RECIPES with GTNA's multiple-recipes logic, so it gets cross-recipe threads and parallel
 * processing exactly like the GTLsupb original ({@code consumeEnergy = false}, unlimited parallel).
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PrimitiveStoneFurnaceMachine extends WorkableElectricMultipleRecipesMachine
                                          implements IZeroEnergyMachine {

    /** GTLsupb {@code MachineConfig.recipeDuration} default. */
    private static final int RECIPE_DURATION = 1;

    public PrimitiveStoneFurnaceMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    public int gtna$recipeDuration() {
        return RECIPE_DURATION;
    }

    @Override
    public boolean onWorking() {
        // Zero-energy: skip the electric/part working checks (no energy hatch, no muffler, ...).
        return true;
    }

    @Override
    public int getMaxParallel() {
        // GTLsupb's machine config uses an effectively unlimited parallel budget (Long.MAX_VALUE);
        // the real count is bounded by the available inputs.
        return Integer.MAX_VALUE;
    }

    @Override
    public int getAdditionalThread() {
        // GTLsupb's zero-energy multi-type logic runs every matching recipe concurrently; expose an
        // effectively unlimited thread budget so the furnace behaves like the original.
        return Integer.MAX_VALUE - 1;
    }
}
