package com.raishxn.gtna.api.machine.feature;

import org.jetbrains.annotations.Nullable;

/**
 * Decides which mode id a controller mirrors onto its global {@code activeRecipeType} when a
 * recipe starts from a pattern buffer.
 *
 * <p>
 * Policy chosen for the multi-mode pattern buffer: the mode pinned on the buffer slot that serves
 * the recipe is authoritative, because that is the mode the player explicitly asked for. When the
 * slot is on AUTO (blank preferred mode) the recipe's own type is used instead, which is the only
 * available signal left. When neither source provides an id there is nothing to mirror.
 *
 * <p>
 * The caller is responsible for actually applying the result through
 * {@link IPatternBufferModeHost#gtna$applyPatternBufferMode(String, com.gregtechceu.gtceu.api.recipe.GTRecipe)},
 * which resolves it against the machine's recipe types with
 * {@link ModeIdMatcher}. Keeping the selection rule pure is what makes it unit-testable without a
 * Minecraft bootstrap.
 */
public final class PatternBufferModeSelection {

    private PatternBufferModeSelection() {}

    /**
     * @param preferredModeId mode pinned on the matching pattern-buffer slot; may be {@code null}
     *                        or blank when the slot is on AUTO
     * @param recipeTypeId    {@code registryName} of the recipe's own type; may be {@code null}
     * @return the mode id to apply, or {@code null} when neither source provides one
     */
    @Nullable
    public static String select(@Nullable String preferredModeId, @Nullable String recipeTypeId) {
        if (preferredModeId != null && !preferredModeId.isBlank()) {
            return preferredModeId.trim();
        }
        if (recipeTypeId != null && !recipeTypeId.isBlank()) {
            return recipeTypeId.trim();
        }
        return null;
    }
}
