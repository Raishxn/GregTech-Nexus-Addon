package com.raishxn.gtna.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;

import com.raishxn.gtna.api.machine.feature.GTNANoConsumeItemPart;

public class InfiniteInputBusPartMachine extends ItemBusPartMachine implements GTNANoConsumeItemPart {

    public InfiniteInputBusPartMachine(IMachineBlockEntity holder, int tier, Object... args) {
        super(holder, tier, IO.IN, args);
    }
}
