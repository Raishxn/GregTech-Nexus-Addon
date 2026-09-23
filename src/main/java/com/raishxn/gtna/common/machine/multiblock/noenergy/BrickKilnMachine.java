package com.raishxn.gtna.common.machine.multiblock.noenergy;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;

import net.minecraft.MethodsReturnNonnullByDefault;

import com.raishxn.gtna.api.machine.IZeroEnergyMachine;
import com.raishxn.gtna.common.machine.multiblock.electric.WorkableElectricMultipleRecipesMachine;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * GTOCore {@code brick_kiln} port (LGPLv3, attribution via {@code GTNASources}): a primitive
 * no-energy multiblock that fires bricks and ceramics from compressed clay + coal.
 *
 * <p>
 * GTOCore uses its {@code NoEnergyMultiblockMachine} with {@code accurateParallel(4)}; this port runs
 * on the GTNA multiple-recipes base in its zero-energy branch (no energy hatch) with parallel 4 and
 * the recipe's own duration (GTOCore's brick recipes use 150/450 ticks).
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BrickKilnMachine extends WorkableElectricMultipleRecipesMachine implements IZeroEnergyMachine {

    /** GTOCore {@code accurateParallel(4)}. */
    private static final int MAX_PARALLEL = 4;

    public BrickKilnMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    public int getMaxParallel() {
        return MAX_PARALLEL;
    }

    @Override
    public boolean onWorking() {
        // Zero-energy: skip the electric/part working checks (no energy hatch, no muffler, ...).
        return true;
    }

    @Override
    public int gtna$recipeDuration() {
        // Keep the recipe's own duration.
        return 0;
    }
}
