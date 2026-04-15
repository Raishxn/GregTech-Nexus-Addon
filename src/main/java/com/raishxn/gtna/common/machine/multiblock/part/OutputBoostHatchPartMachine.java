package com.raishxn.gtna.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;

import com.raishxn.gtna.api.machine.feature.GTNAOutputBoostFluidPart;
import com.raishxn.gtna.api.machine.feature.GTNAOutputBoostItemPart;
import com.raishxn.gtna.config.GTNABalance;

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
        return GTNABalance.getOutputBoostMultiplier(tier);
    }

    public int getOutputMultiplier() {
        return getMultiplierForTier(this.tier);
    }

    @Override
    public int gtna$getOutputMultiplier() {
        return getOutputMultiplier();
    }
}
