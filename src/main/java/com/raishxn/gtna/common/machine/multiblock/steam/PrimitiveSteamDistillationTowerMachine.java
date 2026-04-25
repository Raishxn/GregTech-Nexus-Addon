package com.raishxn.gtna.common.machine.multiblock.steam;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class PrimitiveSteamDistillationTowerMachine extends AdjustableSteamParallelMachine {

    public PrimitiveSteamDistillationTowerMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, GTRecipeTypes.DISTILLATION_RECIPES, 1, 1, 1.0, false, args);
    }

    @Override
    protected double getConversionRate() {
        return 0.75;
    }

    @Nullable
    @Override
    public GTRecipe createThreadedRecipe(@Nonnull GTRecipe recipe) {
        long eut = Math.abs(RecipeHelper.getRealEUt(recipe).getTotalEU());
        if (eut > GTValues.VA[GTValues.MV]) {
            return null;
        }
        return super.createThreadedRecipe(recipe);
    }
}
