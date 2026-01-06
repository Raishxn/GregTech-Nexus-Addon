package com.raishxn.gtna.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ParallelHatchPartMachine;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AdvancedParallelHatchPartMachine extends ParallelHatchPartMachine {

    private final int customParallel;

    public AdvancedParallelHatchPartMachine(IMachineBlockEntity holder, int tier, int customParallel, Object... args) {
        super(holder, tier);
        this.customParallel = customParallel;
    }

    @Override
    public int getCurrentParallel() {
        return customParallel;
    }
}