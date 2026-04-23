package com.raishxn.gtna.common.machine.multiblock.steam;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

public class DimensionallyTranscendentSteamOvenMachine extends AdjustableSteamParallelMachine {

    public DimensionallyTranscendentSteamOvenMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, GTRecipeTypes.FURNACE_RECIPES, 524288, 524288, 0.01, false, args);
    }
}
