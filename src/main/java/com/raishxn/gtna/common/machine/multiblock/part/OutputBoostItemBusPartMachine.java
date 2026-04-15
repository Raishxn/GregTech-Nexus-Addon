package com.raishxn.gtna.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;

import com.raishxn.gtna.api.machine.feature.GTNAOutputBoostItemPart;

public class OutputBoostItemBusPartMachine extends ItemBusPartMachine implements GTNAOutputBoostItemPart {

    public OutputBoostItemBusPartMachine(IMachineBlockEntity holder, int tier, Object... args) {
        super(holder, tier, IO.OUT, args);
    }

    @Override
    public int gtna$getOutputMultiplier() {
        return OutputBoostHatchPartMachine.getMultiplierForTier(getTier());
    }
}
