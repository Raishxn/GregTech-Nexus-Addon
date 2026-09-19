package com.raishxn.gtna;

import com.raishxn.gtna.utils.datastructure.Int128;

import java.math.BigInteger;
import java.util.Random;

/**
 * Validates the hand-rolled 128-bit arithmetic in {@link Int128} against {@link BigInteger}
 * as the oracle. GTLCore-style: plain {@code main()} + assert helper, no JUnit (the GTLCore
 * reference project runs its tests the same way, with no test framework on the classpath).
 *
 * <p>
 * Run with the {@code runUnitTests} Gradle task.
 */
public final class Int128Test {

    private Int128Test() {}

    public static void main(String[] args) {
        addMatchesBigInteger();
        subtractMatchesBigInteger();
        multiplyMatchesBigInteger();
        divideMatchesBigInteger();
        shiftLeftMatchesBigInteger();
        negationAndSign();
        edgeValues();
        System.out.println("[Int128Test] all cases passed");
    }

    /**
     * Regression guard for the previously-broken multiply(): the old 32-bit-limb schoolbook
     * implementation dropped carries (~7% wrong). Now routed through the verified
     * toBigInteger/fromBigInteger path and held to 0 mismatches against BigInteger.
     */
    private static void multiplyMatchesBigInteger() {
        Random rng = new Random(0xC0FFEE);
        for (int i = 0; i < 5000; i++) {
            Int128 a = random(rng), b = random(rng);
            BigInteger expected = wrap128(bi(a).multiply(bi(b)));
            Int128 actual = a.copy().multiply(b);
            require(expected.equals(bi(actual)), "multiply " + bi(a) + " * " + bi(b) + " = " + bi(actual));
        }
        require(new Int128(0, 3).multiply(new Int128(0, 3)).longValue() == 9, "3*3 == 9");
        require(bi(Int128.MAX_VALUE.copy().multiply(Int128.MAX_VALUE)).equals(BigInteger.ONE),
                "MAX_VALUE^2 wraps to 1");
    }

    /**
     * Regression guard for the previously-broken divideNew()/divide(long): the old limb path was
     * wrong for large or negative dividends. Now routed through the verified bit-by-bit divide.
     */
    private static void divideMatchesBigInteger() {
        Random rng = new Random(0xD1E);
        for (int i = 0; i < 5000; i++) {
            Int128 a = random(rng);
            long d = rng.nextLong();
            if (d == 0) d = 1;
            BigInteger expected = bi(a).divide(BigInteger.valueOf(d));
            Int128 actual = a.copy().divideNew(d);
            require(expected.equals(bi(actual)), "divideNew " + bi(a) + " / " + d + " = " + bi(actual));
        }
        // Note: a negative value is new Int128(-10) (high=-1), NOT new Int128(0,-10) — the
        // latter is the large *positive* 2^64-10 because low is an unsigned limb.
        require(new Int128(-10).divideNew(2).longValue() == -5, "-10 / 2 == -5");
        require(new Int128(0, 100).divideNew(7).longValue() == 14, "100 / 7 == 14");
        require(new Int128(0, 10).divideNew(-2).longValue() == -5, "10 / -2 == -5");
    }

    private static void addMatchesBigInteger() {
        Random rng = new Random(0xA11CE);
        for (int i = 0; i < 2000; i++) {
            Int128 a = random(rng), b = random(rng);
            BigInteger expected = wrap128(bi(a).add(bi(b)));
            Int128 actual = a.copy().add(b);
            require(expected.equals(bi(actual)), "add " + bi(a) + " + " + bi(b) + " = " + bi(actual));
        }
    }

    private static void subtractMatchesBigInteger() {
        Random rng = new Random(0xBEEF);
        for (int i = 0; i < 2000; i++) {
            Int128 a = random(rng), b = random(rng);
            BigInteger expected = wrap128(bi(a).subtract(bi(b)));
            Int128 actual = a.copy().subtract(b);
            require(expected.equals(bi(actual)), "subtract " + bi(a) + " - " + bi(b) + " = " + bi(actual));
        }
    }

    private static void shiftLeftMatchesBigInteger() {
        Random rng = new Random(0x5EED);
        for (int i = 0; i < 500; i++) {
            Int128 a = random(rng);
            int n = rng.nextInt(128);
            BigInteger expected = wrap128(bi(a).shiftLeft(n));
            Int128 actual = a.copy().shiftLeft(n);
            require(expected.equals(bi(actual)), "shiftLeft " + bi(a) + " << " + n + " = " + bi(actual));
        }
    }

    private static void negationAndSign() {
        require(new Int128(0).longValue() == 0, "zero longValue");
        require(Int128.MAX_VALUE.copy().add(Int128.ONE()).equals(Int128.MIN_VALUE), "overflow wraps to MIN_VALUE");
        Int128 neg = new Int128(-5);
        require(neg.copy().negate().longValue() == 5, "negate(-5) == 5");
        require(new Int128(7).copy().subtract(new Int128(12)).longValue() == -5, "7 - 12 == -5");
    }

    private static void edgeValues() {
        // Long.MAX_VALUE + 1 must set the high word (carry into bit 64).
        Int128 carry = new Int128(Long.MAX_VALUE).add(1L);
        require(carry.getHigh() == 0 && carry.getLow() == Long.MIN_VALUE, "2^63 carry");
        require(bi(carry).equals(BigInteger.ONE.shiftLeft(63)), "2^63 value");
        // (2^64) via shift.
        Int128 twoTo64 = new Int128(1).shiftLeft(64);
        require(bi(twoTo64).equals(BigInteger.ONE.shiftLeft(64)), "1 << 64");
    }

    // ---- helpers ----

    private static Int128 random(Random rng) {
        return new Int128(rng.nextLong(), rng.nextLong());
    }

    private static final BigInteger TWO_POW_128 = BigInteger.ONE.shiftLeft(128);

    private static BigInteger bi(Int128 v) {
        // Reconstruct the signed 128-bit value from two's-complement high/low words.
        // `low` is always an unsigned limb (bits 0..63); `high` carries the sign (bits 64..127).
        BigInteger high = BigInteger.valueOf(v.getHigh()).shiftLeft(64);
        BigInteger low = BigInteger.valueOf(v.getLow());
        if (low.signum() < 0) low = low.add(BigInteger.ONE.shiftLeft(64)); // low limb is unsigned
        return wrap128(high.add(low));
    }

    /** Reduce a BigInteger to the signed 128-bit range (mod 2^128, two's complement) like Int128 does. */
    private static BigInteger wrap128(BigInteger v) {
        v = v.mod(TWO_POW_128);
        if (v.compareTo(BigInteger.ONE.shiftLeft(127)) >= 0) v = v.subtract(TWO_POW_128);
        return v;
    }

    private static void require(boolean condition, String what) {
        if (!condition) throw new AssertionError("Int128Test failed: " + what);
    }
}
