package com.raishxn.gtna.api.machine.feature;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import org.jetbrains.annotations.Nullable;

public interface IPatternBufferModeProvider {

    @Nullable
    String gtna$getPreferredModeForRecipe(GTRecipe recipe);

    void gtna$onRecipeStarted(GTRecipe recipe);
}
