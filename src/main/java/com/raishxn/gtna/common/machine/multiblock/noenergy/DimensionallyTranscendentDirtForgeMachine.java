package com.raishxn.gtna.common.machine.multiblock.noenergy;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;

import com.raishxn.gtna.api.machine.IZeroEnergyMachine;
import com.raishxn.gtna.common.machine.multiblock.electric.WorkableElectricMultipleRecipesMachine;

import javax.annotation.Nonnull;

/**
 * Manifest phase 2: on the GTNA multiple-recipes base, but zero-energy (no energy hatch). The
 * zero-energy branch of {@code GTNAMultipleRecipesLogic} replicates what the old static modifier did
 * (parallel up to {@link #MAX_PARALLEL} and a 1-tick duration).
 */
public class DimensionallyTranscendentDirtForgeMachine extends WorkableElectricMultipleRecipesMachine
                                                       implements IZeroEnergyMachine {

    private static final int MAX_PARALLEL = 524288;

    public DimensionallyTranscendentDirtForgeMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    public int getMaxParallel() {
        return MAX_PARALLEL;
    }

    @Override
    public boolean onWorking() {
        // Zero-energy: skip the electric/part working checks.
        return true;
    }

    /** Kept for the registration preview/EMI; execution uses the zero-energy branch of the logic. */
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
