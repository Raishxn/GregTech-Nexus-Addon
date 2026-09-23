package com.raishxn.gtna.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;

/**
 * GTNA port of GTLCore's Integrated Ore Processor (LGPLv3; attribution via {@code GTNASources}).
 *
 * <p>
 * Runs the {@code gtna:ore_processing} chain on the multiple-recipes base, so it accepts the Thread /
 * Accelerate / Overclock / Output Boost hatches and the Parallel Hatch, exactly like the other GTNA
 * processors. The structure, casings and tooltips follow GTLCore's
 * {@code integrated_ore_processor} definition.
 */
public class IntegratedOreProcessorMachine extends WorkableElectricMultipleRecipesMachine {

    public IntegratedOreProcessorMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }
}
