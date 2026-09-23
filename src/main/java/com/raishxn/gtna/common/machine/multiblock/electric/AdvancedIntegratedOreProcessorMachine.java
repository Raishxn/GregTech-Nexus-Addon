package com.raishxn.gtna.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;

/**
 * GTNA port of GTLCore's Advanced Integrated Ore Processor (originally TST; LGPLv3 attribution via
 * {@code GTNASources}).
 *
 * <p>
 * The endgame upgrade of {@link IntegratedOreProcessorMachine}: laser-powered and effectively
 * unlimited parallel (GTLCore/TST report {@code Max Parallel: 2147483647}), on the same
 * multiple-recipes base so it also accepts the Thread / Accelerate / Overclock / Output Boost
 * hatches.
 */
public class AdvancedIntegratedOreProcessorMachine extends WorkableElectricMultipleRecipesMachine {

    public AdvancedIntegratedOreProcessorMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    /** GTLCore parity: no Parallel Hatch required, effectively unlimited parallel. */
    @Override
    public int getMaxParallel() {
        return Integer.MAX_VALUE;
    }
}
