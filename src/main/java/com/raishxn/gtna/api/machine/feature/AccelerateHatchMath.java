package com.raishxn.gtna.api.machine.feature;

/**
 * Pure math for the accelerate hatch duration percentage.
 *
 * <p>
 * Matches GTOCore's {@code AccelerateHatchPartMachine#modifyRecipe}: the hatch reduces a recipe to
 * {@code current}% of its duration, and the tier penalty only applies when the <b>recipe</b> tier is
 * above the hatch tier ({@code recipeTier - hatchTier > 0}), never based on the machine tier. The
 * result is clamped to the configured floor/ceiling.
 *
 * <p>
 * Kept free of Minecraft types so it can be unit-tested without bootstrapping the game.
 */
public final class AccelerateHatchMath {

    private AccelerateHatchMath() {}

    /**
     * @param basePercent    the hatch's configured percentage (default: best value for its tier)
     * @param hatchTier      the hatch's own tier
     * @param recipeTier     the recipe's <b>pre-overclock</b> voltage tier
     * @param penaltyPerTier percentage added per recipe tier above the hatch
     * @param minPercent     absolute floor (config)
     * @param maxPercent     absolute ceiling (config; 100 = no effect)
     * @return the final duration percentage, clamped
     */
    public static int compute(int basePercent, int hatchTier, int recipeTier, int penaltyPerTier,
                              int minPercent, int maxPercent) {
        int percent = basePercent;
        int tierDiff = recipeTier - hatchTier;
        if (tierDiff > 0) {
            percent += tierDiff * penaltyPerTier;
        }
        return Math.max(minPercent, Math.min(maxPercent, percent));
    }
}
