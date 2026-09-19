package com.raishxn.gtna.api.machine.feature;

import com.gregtechceu.gtceu.api.recipe.GTRecipeType;

import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * Single source of truth for "does this mode id refer to this recipe type?".
 *
 * <p>
 * Matching is deliberately strict — exact id or exact path-suffix only.
 * The previous implementation used fuzzy {@code contains("saw")} matching with a
 * hardcoded saw/cutter equivalence, which produced false positives that masked
 * routing bugs (a cutter pattern could match a "saw" mode and vice-versa).
 *
 * <p>
 * Accepted forms (all case-insensitive, {@code '_'} and {@code '/'} normalized):
 * <ul>
 * <li>full id: {@code "gtceu:cutter"}</li>
 * <li>exact path: {@code "cutter"}</li>
 * <li>path suffix: {@code "cutter"} matches {@code "gtna:big_cutter"}</li>
 * </ul>
 *
 * <p>
 * When two recipe types share the same path suffix, callers must use the full
 * id — suffix matching is only a convenience for unambiguous short names.
 */
public final class ModeIdMatcher {

    private ModeIdMatcher() {}

    /**
     * @return {@code true} when {@code requestedModeId} unambiguously refers to {@code recipeType}.
     */
    public static boolean matches(@Nullable String requestedModeId, @Nullable GTRecipeType recipeType) {
        if (requestedModeId == null || requestedModeId.isBlank() || recipeType == null ||
                recipeType.registryName == null) {
            return false;
        }
        String requested = requestedModeId.trim().toLowerCase(Locale.ROOT);
        ResourceLocation id = recipeType.registryName;
        String fullId = id.toString().toLowerCase(Locale.ROOT);
        String path = id.getPath().toLowerCase(Locale.ROOT);

        // Exact matches: full "namespace:path" or bare path.
        if (requested.equals(fullId) || requested.equals(path)) {
            return true;
        }

        // Normalized variants: treat '_' and '/' as equivalent separators so
        // "big_cutter" and "big/cutter" both match path "big_cutter".
        String requestedNormalized = requested.replace('_', '/');
        String pathNormalized = path.replace('_', '/');
        if (requestedNormalized.equals(fullId) || requestedNormalized.equals(pathNormalized)) {
            return true;
        }

        // Path-suffix: "cutter" matches "big_cutter" / "big/cutter".
        return path.endsWith("_" + requested) ||
                path.endsWith("/" + requested) ||
                pathNormalized.endsWith("/" + requestedNormalized);
    }

    /**
     * Convenience overload matching against the recipe's own type.
     *
     * @return {@code true} when {@code modeId} refers to {@code recipe}'s type.
     */
    public static boolean matchesRecipe(@Nullable String modeId,
                                        @Nullable com.gregtechceu.gtceu.api.recipe.GTRecipe recipe) {
        if (recipe == null || recipe.getType() == null) {
            return false;
        }
        return matches(modeId, recipe.getType());
    }
}
