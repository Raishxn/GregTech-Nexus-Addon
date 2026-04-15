package com.raishxn.gtna.common.machine.multiblock.part.steam;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.common.machine.multiblock.part.SteamItemBusPartMachine;

import com.raishxn.gtna.api.machine.feature.GTNANoConsumeItemPart;

public class InfiniteSteamInputBus extends SteamItemBusPartMachine implements GTNANoConsumeItemPart {

    public InfiniteSteamInputBus(IMachineBlockEntity holder, Object... args) {
        super(holder, IO.IN, args);
    }
}
