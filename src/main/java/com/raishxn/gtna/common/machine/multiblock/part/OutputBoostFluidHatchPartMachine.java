package com.raishxn.gtna.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;

import com.raishxn.gtna.api.machine.feature.GTNAOutputBoostFluidPart;

public class OutputBoostFluidHatchPartMachine extends FluidHatchPartMachine implements GTNAOutputBoostFluidPart {

    public OutputBoostFluidHatchPartMachine(IMachineBlockEntity holder, int tier, Object... args) {
        super(holder, tier, IO.OUT, INITIAL_TANK_CAPACITY_1X, 1, args);
    }

    @Override
    public int gtna$getOutputMultiplier() {
        return OutputBoostHatchPartMachine.getMultiplierForTier(getTier());
    }
}
