package com.raishxn.gtna.api.machine.feature;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import org.jetbrains.annotations.Nullable;

public interface IPatternBufferModeHost {

    default @Nullable String gtna$resolvePatternBufferMode(GTRecipe recipe) {
        return null;
    }

    default boolean gtna$applyPatternBufferMode(String modeId, GTRecipe recipe) {
        return false;
    }
}
