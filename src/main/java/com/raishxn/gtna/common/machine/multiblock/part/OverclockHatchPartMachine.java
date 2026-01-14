package com.raishxn.gtna.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class OverclockHatchPartMachine extends MultiblockPartMachine implements ITieredMachine {
    private final int tier;

    public OverclockHatchPartMachine(IMachineBlockEntity holder, int tier, Object... args) {
        super(holder);
        this.tier = tier;
    }

    @Override
    public int getTier() {
        return this.tier;
    }

    public double getOverclockMultiplier() {
        return switch (this.tier) {
            case GTValues.UV -> 0.55;       // 55%
            case GTValues.UHV -> 0.3333;    // 33.33%
            case GTValues.UEV -> 0.25;      // 25%
            case GTValues.UIV -> 0.20;      // 20%
            case GTValues.UXV -> 0.1667;    // 16.67%
            case GTValues.OpV -> 0.1429;    // 14.29%
            case GTValues.MAX -> 0.125;     // 12.5%
            default -> 1.0;
        };
    }
}