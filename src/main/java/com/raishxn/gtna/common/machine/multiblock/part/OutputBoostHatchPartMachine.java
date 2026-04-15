package com.raishxn.gtna.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;

import com.raishxn.gtna.api.machine.feature.GTNAOutputBoostFluidPart;
import com.raishxn.gtna.api.machine.feature.GTNAOutputBoostItemPart;

import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class OutputBoostHatchPartMachine extends MultiblockPartMachine
                                         implements ITieredMachine, GTNAOutputBoostItemPart,
                                         GTNAOutputBoostFluidPart {

    private final int tier;

    public OutputBoostHatchPartMachine(IMachineBlockEntity holder, int tier, Object... args) {
        super(holder);
        this.tier = tier;
    }

    @Override
    public int getTier() {
        return this.tier;
    }

    public static int getMultiplierForTier(int tier) {
        return switch (tier) {
            case 0, 1 -> 25;
            case 2 -> 50;
            case 3 -> 100;
            case 4 -> 250;
            case 5 -> 500;
            case 6 -> 1000;
            case 7 -> 2500;
            case 8 -> 5000;
            case 9 -> 10000;
            case 10 -> 25000;
            case 11 -> 50000;
            case 12 -> 100000;
            case 13 -> 250000;
            case 14 -> 1000000;
            default -> 25;
        };
    }

    public int getOutputMultiplier() {
        return getMultiplierForTier(this.tier);
    }

    @Override
    public int gtna$getOutputMultiplier() {
        return getOutputMultiplier();
    }
}
