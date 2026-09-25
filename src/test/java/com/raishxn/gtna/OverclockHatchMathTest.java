package com.raishxn.gtna;

import com.raishxn.gtna.api.machine.feature.OverclockHatchMath;

/**
 * Locks the GTOCore overclock-hatch semantics: the value is an integer duration divisor, the
 * per-step factor is exactly {@code 1/divisor}, and the tier caps the divisor at {@code tier - 6}
 * but never below the standard overclock ({@code 2}).
 */
public final class OverclockHatchMathTest {

    private OverclockHatchMathTest() {}

    public static void main(String[] args) {
        // 1. Tier caps: UV and below are the standard overclock (no gain); MAX reaches 8.
        assertEquals(2, OverclockHatchMath.maxDivisor(1), "LV must cap at the standard divisor");
        assertEquals(2, OverclockHatchMath.maxDivisor(8), "UV must cap at the standard divisor");
        assertEquals(3, OverclockHatchMath.maxDivisor(9), "UHV must allow divisor 3");
        assertEquals(7, OverclockHatchMath.maxDivisor(13), "OpV must allow divisor 7");
        assertEquals(8, OverclockHatchMath.maxDivisor(14), "MAX must allow divisor 8");

        // 2. The configured divisor is clamped into [2, tier cap].
        assertEquals(2, OverclockHatchMath.clampDivisor(0, 14), "the floor is 2");
        assertEquals(2, OverclockHatchMath.clampDivisor(1, 14), "the floor is 2");
        assertEquals(8, OverclockHatchMath.clampDivisor(99, 14), "the ceiling is the tier cap");
        assertEquals(3, OverclockHatchMath.clampDivisor(3, 9), "in-range values pass through");

        // 3. Exact factors: the point of a divisor is that 1/3 and 1/8 do not round to 33% / 13%.
        assertClose(0.5, OverclockHatchMath.stepFactor(2), "divisor 2 is the standard factor");
        assertClose(1.0 / 3.0, OverclockHatchMath.stepFactor(3), "divisor 3 is exactly one third");
        assertClose(1.0 / 7.0, OverclockHatchMath.stepFactor(7), "divisor 7 is exactly one seventh");
        assertClose(1.0 / 8.0, OverclockHatchMath.stepFactor(8), "divisor 8 is exactly one eighth");

        // 4. Post-overclock conversion: the standard 0.5^oc becomes (1/divisor)^oc.
        assertClose(1.0, OverclockHatchMath.additionalDurationMultiplier(2, 3),
                "divisor 2 changes nothing");
        assertClose(Math.pow(2.0 / 3.0, 2), OverclockHatchMath.additionalDurationMultiplier(3, 2),
                "UHV divisor 3 with two overclocks");
        assertClose(Math.pow(1.0 / 4.0, 3), OverclockHatchMath.additionalDurationMultiplier(8, 3),
                "MAX divisor 8 with three overclocks");
        assertClose(1.0, OverclockHatchMath.additionalDurationMultiplier(8, 0),
                "no overclock means no change");

        System.out.println("[OverclockHatchMathTest] all cases passed");
    }

    private static void assertEquals(int expected, int actual, String message) {
        if (expected != actual) {
            throw new AssertionError(message + " (expected " + expected + ", got " + actual + ")");
        }
    }

    private static void assertClose(double expected, double actual, String message) {
        if (Math.abs(expected - actual) > 1e-9) {
            throw new AssertionError(message + " (expected " + expected + ", got " + actual + ")");
        }
    }
}
