package com.raishxn.gtna;

import com.raishxn.gtna.data.recipe.IntegratedOreMath;

/**
 * Locks the GTLCore-parity numbers of the integrated ore processing recipes (G-0054). The generator
 * itself needs GTCEu classes and can only run in game; this covers the arithmetic that defines the
 * balance: crushed-ore amounts, wash-fluid amounts and per-circuit durations.
 */
public final class IntegratedOreMathTest {

    private IntegratedOreMathTest() {}

    public static void main(String[] args) {
        crushedAmounts();
        washFluids();
        durations();
        clamp();
        System.out.println("[IntegratedOreMathTest] all cases passed");
    }

    private static void crushedAmounts() {
        // GTLCore: raw = (oreMultiplier * mult / 2) * 2; stone = oreMultiplier * 2 * mult.
        assertEq(4, IntegratedOreMath.rawCrushedAmount(1, 4), "raw crushed amount for a 1x ore at mult 4");
        assertEq(8, IntegratedOreMath.rawCrushedAmount(2, 4), "raw crushed amount for a 2x ore at mult 4");
        // GTLCore's integer division means a 1x ore at mult 1 rounds the raw chain down to nothing
        // (the stone chain still produces 2); the generator skips a zero crushed amount.
        assertEq(0, IntegratedOreMath.rawCrushedAmount(1, 1), "raw crushed amount for a 1x ore at mult 1");
        assertEq(8, IntegratedOreMath.stoneCrushedAmount(1, 4), "stone crushed amount for a 1x ore at mult 4");
        assertEq(16, IntegratedOreMath.stoneCrushedAmount(2, 4), "stone crushed amount for a 2x ore at mult 4");
        assertEq(2, IntegratedOreMath.stoneCrushedAmount(1, 1), "stone crushed amount for a 1x ore at mult 1");
        // The stone chain always yields twice the raw chain for the same ore multiplier.
        for (int mult : new int[] { 1, 2, 3, 4, 8 }) {
            assertEq(IntegratedOreMath.rawCrushedAmount(2, mult) * 2,
                    IntegratedOreMath.stoneCrushedAmount(2, mult),
                    "stone chain must be 2x the raw chain at mult " + mult);
        }
    }

    private static void washFluids() {
        assertEq(400, IntegratedOreMath.washFluidAmount(4), "wash fluid for 4 crushed ore");
        assertEq(800, IntegratedOreMath.washFluidAmount(8), "wash fluid for 8 crushed ore");
    }

    private static void durations() {
        // Circuit 1 includes the material mass (macerate + centrifuge): 26 + (26 + mass * 4) * amount.
        assertEq(1834, IntegratedOreMath.duration(1, 50, 8), "circuit 1 duration");
        // Circuits 2 and 4 share the 200+200+26 stage cost; 3 and 6 the 200+26+16; 5 shares 2, 7 shares 4.
        assertEq(3434, IntegratedOreMath.duration(2, 50, 8), "circuit 2 duration");
        assertEq(1962, IntegratedOreMath.duration(3, 50, 8), "circuit 3 duration");
        assertEq(3434, IntegratedOreMath.duration(4, 50, 8), "circuit 4 duration");
        assertEq(3434, IntegratedOreMath.duration(5, 50, 8), "circuit 5 duration");
        assertEq(1962, IntegratedOreMath.duration(6, 50, 8), "circuit 6 duration");
        assertEq(3434, IntegratedOreMath.duration(7, 50, 8), "circuit 7 duration");
    }

    private static void clamp() {
        // 26 + (26 + 1e8 * 4) * 1000 = 400,000,000,026 ticks: far beyond an int, must clamp.
        assertEq(Integer.MAX_VALUE, IntegratedOreMath.duration(1, 100_000_000L, 1000),
                "an overflowing duration must clamp instead of wrapping negative");
        assertEq(Integer.MAX_VALUE, IntegratedOreMath.washFluidAmount(Integer.MAX_VALUE),
                "an overflowing wash amount must clamp");
    }

    private static void assertEq(int expected, int actual, String what) {
        if (expected != actual) {
            throw new AssertionError(what + ": expected " + expected + " but got " + actual);
        }
    }
}
