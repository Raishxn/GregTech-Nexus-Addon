package com.raishxn.gtna.api.machine.feature;

/**
 * Pure math for the overclock hatch duration divisor.
 *
 * <p>
 * Matches GTOCore's {@code OverclockPartMachine}: the player configures an integer <b>divisor</b> of
 * the recipe duration (GTOCore labels it {@code gtocore.machine.overclock_hatch.divisor}, "Divisor of
 * duration"), and the hatch changes the overclock so that every 4x EU/t step reduces the duration to
 * {@code 1/divisor} instead of the standard {@code 0.5}. The tier sets the best (largest) divisor
 * {@code tier - 6}; the floor {@link #MIN_DIVISOR} is the standard overclock, i.e. no gain.
 *
 * <p>
 * Kept free of Minecraft types so it can be unit-tested without bootstrapping the game.
 */
public final class OverclockHatchMath {

    /** The standard overclock step ({@code 1/2}); a hatch set to this divisor has no effect. */
    public static final int MIN_DIVISOR = 2;
    /** The standard non-perfect overclock duration factor ({@code 0.5}). */
    public static final double STD_DURATION_FACTOR = 0.5;

    private OverclockHatchMath() {}

    /** The tier's best (largest) divisor: {@code tier - 6}, never below {@link #MIN_DIVISOR}. */
    public static int maxDivisor(int tier) {
        return Math.max(MIN_DIVISOR, tier - 6);
    }

    /** Clamps a configured divisor into {@code [MIN_DIVISOR, maxDivisor(tier)]}. */
    public static int clampDivisor(int divisor, int tier) {
        return Math.max(MIN_DIVISOR, Math.min(maxDivisor(tier), divisor));
    }

    /** The per-overclock-step duration factor, exactly {@code 1/divisor}. */
    public static double stepFactor(int divisor) {
        return 1.0 / Math.max(1, divisor);
    }

    /**
     * The additional factor a post-overclock duration must be multiplied by so that {@code ocLevel}
     * standard steps ({@code 0.5^ocLevel}) become {@code stepFactor^ocLevel}. A divisor at or below
     * {@link #MIN_DIVISOR} yields exactly {@code 1} (no change).
     */
    public static double additionalDurationMultiplier(int divisor, int ocLevel) {
        if (ocLevel <= 0 || divisor <= MIN_DIVISOR) {
            return 1.0;
        }
        return Math.pow(stepFactor(divisor) / STD_DURATION_FACTOR, ocLevel);
    }
}
