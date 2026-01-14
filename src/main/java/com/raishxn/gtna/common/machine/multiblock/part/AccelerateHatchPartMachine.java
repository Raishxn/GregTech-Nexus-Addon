package com.raishxn.gtna.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AccelerateHatchPartMachine extends MultiblockPartMachine implements ITieredMachine {
    private final int tier;
    public AccelerateHatchPartMachine(IMachineBlockEntity holder, int tier, Object... args) {
        super(holder);
        this.tier = tier;
    }
    @Override
    public int getTier() {
        return this.tier;
    }
    public int getMinDurationPercentage() {
        return Math.max(1, 50 - (2 * (this.getTier() - 1)));
    }
    public int calcDurationPercentage(int machineTier) {
        int basePercentage = getMinDurationPercentage();
        int tierDiff = machineTier - this.getTier();
        if (tierDiff > 0) {
            basePercentage += (tierDiff * 20);
        }
        return Math.min(100, Math.max(1, basePercentage));
    }
}