package com.raishxn.gtna;

import com.raishxn.gtna.api.machine.feature.AccelerateHatchMath;

/**
 * Locks the GTOCore accelerate-hatch semantics: the tier penalty follows the <b>recipe</b> tier, not
 * the machine tier, and the result is clamped to the configured bounds.
 */
public final class AccelerateHatchMathTest {

    private static final int PENALTY = 20;
    private static final int MIN = 1;
    private static final int MAX = 100;

    private AccelerateHatchMathTest() {}

    public static void main(String[] args) {
        // 1. Recipe at the hatch tier: no penalty.
        assertEquals(38, AccelerateHatchMath.compute(38, 7, 7, PENALTY, MIN, MAX),
                "same tier must not be penalized");

        // 2. Recipe BELOW the hatch tier: still no penalty (the GTOCore rule that never punishes).
        assertEquals(36, AccelerateHatchMath.compute(36, 8, 1, PENALTY, MIN, MAX),
                "a low-tier recipe in a high-tier hatch must not be penalized");

        // 3. Recipe above the hatch tier: +20 per missing tier.
        assertEquals(58, AccelerateHatchMath.compute(38, 7, 8, PENALTY, MIN, MAX),
                "one tier above adds the per-tier penalty");
        assertEquals(98, AccelerateHatchMath.compute(38, 7, 10, PENALTY, MIN, MAX),
                "three tiers above add three penalties");

        // 4. Penalty can reach 100% (no effect) and must be capped there.
        assertEquals(100, AccelerateHatchMath.compute(90, 7, 9, PENALTY, MIN, MAX),
                "the ceiling caps a heavily penalized hatch at no effect");

        // 5. The floor is applied too.
        assertEquals(MIN, AccelerateHatchMath.compute(0, 7, 7, PENALTY, MIN, MAX),
                "the floor must be respected");

        System.out.println("[AccelerateHatchMathTest] all cases passed");
    }

    private static void assertEquals(int expected, int actual, String message) {
        if (expected != actual) {
            throw new AssertionError(message + " (expected " + expected + ", got " + actual + ")");
        }
    }
}
