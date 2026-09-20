package com.raishxn.gtna.api.machine.feature;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import org.jetbrains.annotations.Nullable;

public interface IPatternBufferModeProvider {

    @Nullable
    String gtna$getPreferredModeForRecipe(GTRecipe recipe);

    void gtna$onRecipeStarted(GTRecipe recipe);

    /**
     * Machine mode this buffer's staged content asks the controller to be in, or {@code null} when
     * nothing is pending or no mode can be derived.
     *
     * <p>
     * This exists because GTCEu's stock {@code RecipeLogic} only searches the <b>active</b> recipe
     * type: for a multiblock like the Large Cutter (cutter + lathe) to follow the buffer, the
     * controller has to be put in the right mode <em>before</em> the search runs. This is the hint
     * the auto-switch uses.
     *
     * <p>
     * Default is "no request", so implementations that only mirror the mode of a running recipe do
     * not have to care.
     */
    default @Nullable String gtna$getPendingModeId() {
        return null;
    }
}
