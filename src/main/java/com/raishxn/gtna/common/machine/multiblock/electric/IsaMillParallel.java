package com.raishxn.gtna.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;

/** Content parallel for the ISA Mill, bounded by available inputs and output space. */
public final class IsaMillParallel {

    private IsaMillParallel() {}

    public static ModifierFunction apply(MetaMachine machine, GTRecipe recipe) {
        int parallel = ParallelLogic.getParallelAmount(machine, recipe, 2);
        if (parallel == 0) return ModifierFunction.NULL;
        if (parallel == 1) return ModifierFunction.IDENTITY;
        return ModifierFunction.builder()
                .modifyAllContents(ContentModifier.multiplier(parallel))
                .eutMultiplier(parallel)
                .parallels(parallel)
                .build();
    }
}
