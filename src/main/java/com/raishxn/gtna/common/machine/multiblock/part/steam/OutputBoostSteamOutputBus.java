package com.raishxn.gtna.common.machine.multiblock.part.steam;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.common.machine.multiblock.part.SteamItemBusPartMachine;

import com.raishxn.gtna.api.machine.feature.GTNAOutputBoostItemPart;
import com.raishxn.gtna.common.machine.multiblock.part.OutputBoostHatchPartMachine;

public class OutputBoostSteamOutputBus extends SteamItemBusPartMachine implements GTNAOutputBoostItemPart {

    public OutputBoostSteamOutputBus(IMachineBlockEntity holder, Object... args) {
        super(holder, IO.OUT, args);
    }

    @Override
    public int gtna$getOutputMultiplier() {
        return OutputBoostHatchPartMachine.getMultiplierForTier(getTier());
    }
}
