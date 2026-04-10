package com.raishxn.gtna.api.machine.feature;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;

import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public interface IPatternBufferModeHost {

    default @Nullable String gtna$resolvePatternBufferMode(GTRecipe recipe) {
        return null;
    }

    default boolean gtna$applyPatternBufferMode(String modeId, GTRecipe recipe) {
        return false;
    }

    default boolean gtna$matchesModeId(String requestedModeId, GTRecipeType recipeType) {
        if (requestedModeId == null || requestedModeId.isBlank() || recipeType == null || recipeType.registryName == null) {
            return false;
        }
        String requested = requestedModeId.trim().toLowerCase(Locale.ROOT);
        String fullId = recipeType.registryName.toString().toLowerCase(Locale.ROOT);
        String path = recipeType.registryName.getPath().toLowerCase(Locale.ROOT);
        if (requested.equals(fullId) || requested.equals(path)) {
            return true;
        }
        return path.endsWith("_" + requested) || path.endsWith("/" + requested);
    }
}
