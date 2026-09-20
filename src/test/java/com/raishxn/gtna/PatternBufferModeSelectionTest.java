package com.raishxn.gtna;

import com.raishxn.gtna.api.machine.feature.PatternBufferModeSelection;

/**
 * Locks the mode-selection rule used when mirroring a pattern buffer's mode onto the controller's
 * global {@code activeRecipeType}: the mode pinned on the slot wins, the recipe's own type is the
 * fallback, and nothing is mirrored when neither exists.
 *
 * <p>
 * Unlike {@code ModeIdMatcherTest}, this exercises the production class directly — the inputs are
 * plain strings, so no Minecraft bootstrap is needed and no mirrored copy of the algorithm has to
 * be kept in sync. GTLCore-style: {@code main()} + asserts, no JUnit.
 */
public final class PatternBufferModeSelectionTest {

    private PatternBufferModeSelectionTest() {}

    public static void main(String[] args) {
        pinnedModeWins();
        fallsBackToRecipeType();
        nothingToMirror();
        System.out.println("[PatternBufferModeSelectionTest] all cases passed");
    }

    /** A mode explicitly pinned on the slot must always be the one applied. */
    private static void pinnedModeWins() {
        check("gtceu:cutter", PatternBufferModeSelection.select("gtceu:cutter", "gtceu:assembler"),
                "pinned full id wins over a different recipe type");
        check("cutter", PatternBufferModeSelection.select("cutter", "gtceu:assembler"),
                "pinned short form is kept verbatim");
        check("gtna:legacy_mode", PatternBufferModeSelection.select("gtna:legacy_mode", "gtna:legacy_mode"),
                "legacy pinned mode is applied even when it equals the recipe type");
        check("gtceu:cutter", PatternBufferModeSelection.select("  gtceu:cutter  ", "gtceu:assembler"),
                "pinned mode is trimmed");
    }

    /** AUTO slots (blank preferred mode) fall back to the recipe's own type. */
    private static void fallsBackToRecipeType() {
        check("gtceu:assembler", PatternBufferModeSelection.select(null, "gtceu:assembler"),
                "null preferred falls back to recipe type");
        check("gtceu:assembler", PatternBufferModeSelection.select("", "gtceu:assembler"),
                "empty preferred falls back to recipe type");
        check("gtceu:assembler", PatternBufferModeSelection.select("   ", "gtceu:assembler"),
                "blank preferred falls back to recipe type");
        check("gtceu:assembler", PatternBufferModeSelection.select("", "  gtceu:assembler  "),
                "fallback recipe type is trimmed");
    }

    /** Nothing to mirror when the slot is on AUTO and the recipe has no usable type. */
    private static void nothingToMirror() {
        check(null, PatternBufferModeSelection.select(null, null), "both absent");
        check(null, PatternBufferModeSelection.select("", ""), "both blank");
        check(null, PatternBufferModeSelection.select("   ", null), "blank preferred and null type");
        check(null, PatternBufferModeSelection.select(null, "   "), "null preferred and blank type");
    }

    private static void check(String expected, String actual, String name) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(
                    name + ": expected " + (expected == null ? "<null>" : "'" + expected + "'") +
                            " but got " + (actual == null ? "<null>" : "'" + actual + "'"));
        }
    }
}
