package com.raishxn.gtna;

import com.raishxn.gtna.utils.NumberUtils;

import java.math.BigInteger;
import java.util.Locale;

/**
 * Validates the pure-math helpers of {@code NumberUtils}: K/M/G formatting, the 0.95^N table,
 * the fake voltage tier mapping, the nearest-pow2 lookup, parallel-tier estimation and the
 * saturated add/multiply primitives. The {@code numberText} overloads are excluded — they need
 * a Minecraft bootstrap ({@code Component}). GTLCore-style: {@code main()} + asserts, no JUnit.
 *
 * <p>
 * Formatting is locale-sensitive ({@code DecimalFormat("#.##")}), so the test pins
 * {@code Locale.ROOT} in the forked test JVM to keep the decimal-point assertions stable on
 * any machine. The {@code getAdditionalTier} cases stay away from integer ratios, where a
 * 1-ulp difference would flip {@code ceil}.
 */
public final class NumberUtilsTest {

    private NumberUtilsTest() {}

    public static void main(String[] args) {
        Locale.setDefault(Locale.ROOT);
        pow95MatchesMathPow();
        formatting();
        longValueSaturation();
        fakeVoltageTiers();
        nearestPow2Lookup();
        additionalTier();
        saturatedAddCases();
        saturatedMultiplyCases();
        System.out.println("[NumberUtilsTest] all cases passed");
    }

    private static void pow95MatchesMathPow() {
        check(NumberUtils.pow95(0) == 1.0, "pow95(0) == 1");
        for (int n = 0; n <= 200; n++) {
            double expected = Math.pow(0.95, n);
            double actual = NumberUtils.pow95(n);
            check(relDiff(actual, expected) < 1e-9, "pow95(" + n + ") matches Math.pow");
        }
        // Table boundary (n=127 uses the precomputed table, n=128 the closed form): the two
        // paths must meet so callers cannot observe a discontinuity.
        double left = NumberUtils.pow95(127);
        double right = NumberUtils.pow95(128);
        check(relDiff(right, left * 0.95) < 1e-6, "pow95 table/closed-form continuity at 127->128");
    }

    private static void formatting() {
        checkFormatLong(0, "0");
        checkFormatLong(999, "999");
        checkFormatLong(1000, "1K");
        checkFormatLong(10_500, "10.5K");
        checkFormatLong(1_000_000, "1M");
        checkFormatLong(1_500_000, "1.5M");
        checkFormatLong(1_000_000_000_000L, "1T");
        checkFormatLong(Long.MAX_VALUE, "9.22E");
        checkFormatDouble(2_500_000.0, "2.5M");
        // 1e36 exhausts the unit table (index 11 = "D") and clamps instead of overflowing.
        checkFormatDouble(1e36, "1000D");
    }

    private static void longValueSaturation() {
        checkLong(NumberUtils.getLongValue(BigInteger.valueOf(42)), 42, "small BigInteger passes through");
        checkLong(NumberUtils.getLongValue(BigInteger.ZERO), 0, "zero passes through");
        checkLong(NumberUtils.getLongValue(BigInteger.valueOf(-7)), -7, "negative passes through");
        checkLong(NumberUtils.getLongValue(BigInteger.valueOf(Long.MIN_VALUE)), Long.MIN_VALUE,
                "MIN_VALUE passes through");
        checkLong(NumberUtils.getLongValue(BigInteger.valueOf(Long.MAX_VALUE)), Long.MAX_VALUE,
                "MAX_VALUE passes through");
        checkLong(NumberUtils.getLongValue(BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE)), Long.MAX_VALUE,
                "above MAX_VALUE saturates to MAX_VALUE");
    }

    private static void fakeVoltageTiers() {
        // ULV=0, LV=1, MV=2, HV=3, EV=4, IV=5, LuV=6, ZPM=7, UV=8, UHV=9, UEV=10 — the mapping
        // consumed by parallel/overclock UI math. Locked against named GTM voltages.
        checkTier(0, 0, "zero voltage");
        checkTier(31, 0, "below LV");
        checkTier(32, 1, "LV");
        checkTier(33, 1, "LV+1");
        checkTier(127, 1, "LV upper bound");
        checkTier(128, 2, "MV");
        checkTier(512, 3, "HV");
        checkTier(2048, 4, "EV");
        checkTier(8192, 5, "IV");
        checkTier(32768, 6, "LuV");
        checkTier(131072, 7, "ZPM");
        checkTier(524288, 8, "UV");
        checkTier(2097152, 9, "UHV");
        checkTier(8388608, 10, "UEV");
        checkLong(NumberUtils.getVoltageFromFakeTier(0), 8, "tier 0 voltage");
        checkLong(NumberUtils.getVoltageFromFakeTier(1), 32, "tier 1 voltage");
        checkLong(NumberUtils.getVoltageFromFakeTier(2), 128, "tier 2 voltage");
        checkLong(NumberUtils.getVoltageFromFakeTier(3), 512, "tier 3 voltage");
        // Round-trip across the representable range (tier 30 would overflow 4^(t+1)*2 to a
        // negative long — outside the intended domain, so not locked here).
        for (int tier = 0; tier <= 29; tier++) {
            long voltage = NumberUtils.getVoltageFromFakeTier(tier);
            check(NumberUtils.getFakeVoltageTier(voltage) == tier, "round trip tier " + tier);
        }
    }

    private static void nearestPow2Lookup() {
        // NEAREST is "closest power of two in {1, 2, 4, 8, 16}, ties round up": 5 sits between
        // 4 and 8 and goes DOWN (distance 1 vs 3), 6 is a tie and goes UP, same for 12.
        check(NumberUtils.nearestPow2Lookup(1) == 1, "1 -> 1");
        check(NumberUtils.nearestPow2Lookup(2) == 2, "2 -> 2");
        check(NumberUtils.nearestPow2Lookup(3) == 4, "3 -> 4");
        check(NumberUtils.nearestPow2Lookup(4) == 4, "4 -> 4");
        check(NumberUtils.nearestPow2Lookup(5) == 4, "5 -> 4 (closer to 4)");
        check(NumberUtils.nearestPow2Lookup(6) == 8, "6 -> 8 (tie rounds up)");
        check(NumberUtils.nearestPow2Lookup(10) == 8, "10 -> 8");
        check(NumberUtils.nearestPow2Lookup(11) == 8, "11 -> 8 (closer to 8)");
        check(NumberUtils.nearestPow2Lookup(12) == 16, "12 -> 16 (tie rounds up)");
        check(NumberUtils.nearestPow2Lookup(16) == 16, "16 -> 16");
        check(NumberUtils.nearestPow2Lookup(0) == 1, "below range clamps low");
        check(NumberUtils.nearestPow2Lookup(-3) == 1, "negative clamps low");
        check(NumberUtils.nearestPow2Lookup(17) == 16, "above range clamps high");
        check(NumberUtils.nearestPow2Lookup(999) == 16, "far above range clamps high");
    }

    private static void additionalTier() {
        check(NumberUtils.getAdditionalTier(0.95, 2) == 14, "0.95/2 -> 14");
        check(NumberUtils.getAdditionalTier(0.5, 3) == 2, "0.5/3 -> 2");
        check(NumberUtils.getAdditionalTier(0.5, 7) == 3, "0.5/7 -> 3");
        check(NumberUtils.getAdditionalTier(0.99, 10) == 230, "0.99/10 -> 230");
        check(NumberUtils.getAdditionalTier(0.9, 1) == 0, "parallel 1 -> 0 additional tiers");
    }

    private static void saturatedAddCases() {
        checkLong(NumberUtils.saturatedAdd(1, 2), 3, "plain add");
        checkLong(NumberUtils.saturatedAdd(0, 0), 0, "zero add");
        checkLong(NumberUtils.saturatedAdd(5, -5), 0, "cancelling add");
        checkLong(NumberUtils.saturatedAdd(-1, Long.MAX_VALUE), Long.MAX_VALUE - 1, "no overflow near max");
        checkLong(NumberUtils.saturatedAdd(Long.MAX_VALUE - 1, 1), Long.MAX_VALUE - 1 + 1, "exact max");
        checkLong(NumberUtils.saturatedAdd(Long.MAX_VALUE, 1), Long.MAX_VALUE, "MAX + 1 saturates high");
        checkLong(NumberUtils.saturatedAdd(Long.MAX_VALUE, Long.MAX_VALUE), Long.MAX_VALUE, "MAX + MAX saturates high");
        checkLong(NumberUtils.saturatedAdd(Long.MIN_VALUE, -1), Long.MIN_VALUE, "MIN - 1 saturates low");
        checkLong(NumberUtils.saturatedAdd(Long.MIN_VALUE, Long.MIN_VALUE), Long.MIN_VALUE,
                "MIN + MIN saturates low");
        checkLong(NumberUtils.saturatedAdd(Long.MAX_VALUE, Long.MIN_VALUE), -1, "MAX + MIN = -1");
    }

    private static void saturatedMultiplyCases() {
        checkLong(NumberUtils.saturatedMultiply(3, 5), 15, "plain multiply");
        checkLong(NumberUtils.saturatedMultiply(0, Long.MAX_VALUE), 0, "zero multiply");
        checkLong(NumberUtils.saturatedMultiply(-3, 4), -12, "negative multiply");
        checkLong(NumberUtils.saturatedMultiply(1L << 30, 1L << 30), 1L << 60, "exact 2^60");
        checkLong(NumberUtils.saturatedMultiply(1L << 31, 1L << 31), 1L << 62, "exact 2^62");
        checkLong(NumberUtils.saturatedMultiply(-(1L << 31), 1L << 31), -(1L << 62), "exact -2^62");
        checkLong(NumberUtils.saturatedMultiply(Long.MAX_VALUE, 2), Long.MAX_VALUE, "MAX * 2 saturates high");
        checkLong(NumberUtils.saturatedMultiply(Long.MIN_VALUE, 2), Long.MIN_VALUE, "MIN * 2 saturates low");
        checkLong(NumberUtils.saturatedMultiply(Long.MAX_VALUE, Long.MAX_VALUE), Long.MAX_VALUE,
                "MAX * MAX saturates high");
        checkLong(NumberUtils.saturatedMultiply(Long.MIN_VALUE, Long.MIN_VALUE), Long.MAX_VALUE,
                "MIN * MIN saturates high");
        checkLong(NumberUtils.saturatedMultiply(Long.MAX_VALUE, Long.MIN_VALUE), Long.MIN_VALUE,
                "MAX * MIN saturates low");
        checkLong(NumberUtils.saturatedMultiply(Long.MIN_VALUE, -1), Long.MAX_VALUE, "MIN * -1 saturates high");
        checkLong(NumberUtils.saturatedMultiply(Long.MIN_VALUE, 1), Long.MIN_VALUE, "MIN * 1 stays MIN");
        checkLong(NumberUtils.saturatedMultiply(1L << 40, 1L << 40), Long.MAX_VALUE, "2^80 saturates high");
        checkLong(NumberUtils.saturatedMultiply(1L << 32, 1L << 32), Long.MAX_VALUE, "2^64 saturates high");
    }

    // ---- helpers ----

    private static int caseNum = 0;

    private static void check(boolean condition, String name) {
        caseNum++;
        if (!condition) {
            throw new AssertionError("NumberUtilsTest case #" + caseNum + " '" + name + "' failed");
        }
    }

    private static void checkFormatLong(long input, String expected) {
        check(NumberUtils.formatLong(input).equals(expected),
                "formatLong(" + input + ") == \"" + expected + "\"");
    }

    private static void checkFormatDouble(double input, String expected) {
        check(NumberUtils.formatDouble(input).equals(expected),
                "formatDouble(" + input + ") == \"" + expected + "\"");
    }

    private static void checkLong(long actual, long expected, String name) {
        check(actual == expected, name + " (expected " + expected + ", got " + actual + ")");
    }

    private static void checkTier(long voltage, int expectedTier, String name) {
        check(NumberUtils.getFakeVoltageTier(voltage) == expectedTier,
                name + " -> tier " + expectedTier);
    }

    private static double relDiff(double a, double b) {
        double scale = Math.max(Math.abs(a), Math.abs(b));
        return scale == 0.0 ? Math.abs(a - b) : Math.abs(a - b) / scale;
    }
}
