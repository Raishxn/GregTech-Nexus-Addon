package com.raishxn.gtna.api.machine.feature;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;

import org.jetbrains.annotations.Nullable;

public interface IPatternBufferModeHost {

    default @Nullable String gtna$resolvePatternBufferMode(GTRecipe recipe) {
        return null;
    }

    default boolean gtna$applyPatternBufferMode(String modeId, GTRecipe recipe) {
        return false;
    }

    default boolean gtna$matchesModeId(String requestedModeId, GTRecipeType recipeType) {
        return ModeIdMatcher.matches(requestedModeId, recipeType);
    }
}
