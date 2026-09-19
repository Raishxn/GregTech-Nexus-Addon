package com.raishxn.gtna;

import java.util.Locale;

/**
 * Validates the pattern-buffer mode matching rules (strict exact + path-suffix, no fuzzy
 * contains() and no hardcoded saw/cutter equivalence). The production class
 * {@code ModeIdMatcher} operates on {@code GTRecipeType}; since that type requires a
 * Minecraft bootstrap, this test exercises the identical matching algorithm against raw
 * (fullId, path) pairs. GTLCore-style: {@code main()} + asserts, no JUnit.
 *
 * <p>
 * Keep the {@link #matches} body byte-for-byte in sync with
 * {@code ModeIdMatcher.matches(...)} — the point of this test is to lock the rules so the
 * saw/cutter regression cannot silently return.
 */
public final class ModeIdMatcherTest {

    private ModeIdMatcherTest() {}

    public static void main(String[] args) {
        exactAndSuffix();
        antiHardcodeGuarantees();
        degenerateInputs();
        System.out.println("[ModeIdMatcherTest] all cases passed");
    }

    private static void exactAndSuffix() {
        check(true, "gtceu:cutter", "gtceu:cutter", "cutter", "exact full id");
        check(true, "cutter", "gtceu:cutter", "cutter", "exact path");
        check(true, "CUTTER", "gtceu:cutter", "cutter", "case-insensitive");
        check(true, "  cutter  ", "gtceu:cutter", "cutter", "trim");
        check(true, "cutter", "gtna:big_cutter", "big_cutter", "suffix underscore");
        check(true, "cutter", "gtna:big/cutter", "big/cutter", "suffix slash");
        check(true, "big/cutter", "gtna:big_cutter", "big_cutter", "normalized separator");
    }

    private static void antiHardcodeGuarantees() {
        check(false, "saw", "gtceu:cutter", "cutter", "NO saw->cutter fuzzy");
        check(false, "cutting_saw", "gtceu:cutter", "cutter", "NO cutting_saw->cutter");
        check(false, "saw", "gtna:tablesaw", "tablesaw", "NO contains('saw') partial");
        check(false, "cut", "gtceu:cutter", "cutter", "NO prefix match");
    }

    private static void degenerateInputs() {
        check(false, null, "gtceu:cutter", "cutter", "null request");
        check(false, "", "gtceu:cutter", "cutter", "empty request");
        check(false, "   ", "gtceu:cutter", "cutter", "blank request");
        check(false, "cutter", null, "cutter", "null full id");
        check(false, "othermod:cutter", "gtceu:cutter", "cutter", "different namespace");
    }

    // ---- mirror of ModeIdMatcher.matches (keep in sync!) ----

    private static boolean matches(String requestedModeId, String fullIdIn, String pathIn) {
        if (requestedModeId == null || requestedModeId.isBlank() || fullIdIn == null) return false;
        String requested = requestedModeId.trim().toLowerCase(Locale.ROOT);
        String fullId = fullIdIn.toLowerCase(Locale.ROOT);
        String path = pathIn.toLowerCase(Locale.ROOT);
        if (requested.equals(fullId) || requested.equals(path)) return true;
        String requestedNormalized = requested.replace('_', '/');
        String pathNormalized = path.replace('_', '/');
        if (requestedNormalized.equals(fullId) || requestedNormalized.equals(pathNormalized)) return true;
        return path.endsWith("_" + requested) || path.endsWith("/" + requested) ||
                pathNormalized.endsWith("/" + requestedNormalized);
    }

    private static int caseNum = 0;

    private static void check(boolean expected, String req, String full, String path, String name) {
        caseNum++;
        boolean actual = matches(req, full, path);
        if (actual != expected) {
            throw new AssertionError("ModeIdMatcherTest case #" + caseNum + " '" + name + "': expected=" +
                    expected + " got=" + actual);
        }
    }
}
