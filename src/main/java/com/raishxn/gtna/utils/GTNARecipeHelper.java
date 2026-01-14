package com.raishxn.gtna.utils;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;

import java.util.List;

public class GTNARecipeHelper {

    public static void runFairAllocation(MetaMachine machine, List<GTRecipe> recipes, long totalMaxParallel, List<GTNARecipeUtils.ActiveRecipe> activeRecipesList, IRecipeLogicMachine logicMachine) {
        if (recipes.isEmpty() || totalMaxParallel <= 0) return;
        long fairShareParallel = totalMaxParallel / recipes.size();
        if (fairShareParallel < 1) fairShareParallel = 1;
        for (GTRecipe recipe : recipes) {
            if (totalMaxParallel <= 0) break;
            int maxRecipeParallel = ParallelLogic.getParallelAmount(machine, recipe, Integer.MAX_VALUE);
            long actualParallel = Math.min(maxRecipeParallel, fairShareParallel);
            actualParallel = Math.min(actualParallel, totalMaxParallel);
            if (actualParallel > 0) {
                ModifierFunction modifier = ModifierFunction.builder()
                        .modifyAllContents(ContentModifier.multiplier(actualParallel))
                        .eutMultiplier(actualParallel)
                        .parallels((int) actualParallel)
                        .build();
                GTRecipe modifiedRecipe = modifier.apply(recipe);
                if (modifiedRecipe != null && RecipeHelper.matchContents((IRecipeCapabilityHolder) machine, modifiedRecipe).isSuccess()) {
                    RecipeHelper.handleRecipeIO((IRecipeCapabilityHolder) machine, modifiedRecipe, IO.IN, null);
                    activeRecipesList.add(new GTNARecipeUtils.ActiveRecipe(modifiedRecipe, modifiedRecipe.duration, null));
                    totalMaxParallel -= actualParallel;
                }
            }
        }
    }
}