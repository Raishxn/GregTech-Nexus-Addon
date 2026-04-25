package com.raishxn.gtna.common.machine.multiblock.noenergy;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;

import javax.annotation.Nonnull;

public class DimensionallyTranscendentDirtForgeMachine extends WorkableMultiblockMachine {

    private static final int MAX_PARALLEL = 524288;

    public DimensionallyTranscendentDirtForgeMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    public static ModifierFunction recipeModifier(MetaMachine machine, @Nonnull GTRecipe recipe) {
        int parallels = ParallelLogic.getParallelAmount(machine, recipe, MAX_PARALLEL);
        double durationMultiplier = 1.0 / Math.max(1, recipe.duration);
        return ModifierFunction.builder()
                .parallels(parallels)
                .modifyAllContents(ContentModifier.multiplier(parallels))
                .durationMultiplier(durationMultiplier)
                .build();
    }
}
