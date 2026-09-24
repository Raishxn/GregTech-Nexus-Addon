package com.raishxn.gtna.planner.api.amount;

import java.math.BigInteger;
import java.util.Objects;

/** Exact, non-negative amount for values that may exceed {@code long}. */
public final class UfoAmount implements Comparable<UfoAmount> {

    public static final UfoAmount ZERO = new UfoAmount(BigInteger.ZERO);
    public static final UfoAmount ONE = new UfoAmount(BigInteger.ONE);
    public static final int DEFAULT_MAX_BITS = 4096;

    private final BigInteger value;

    private UfoAmount(BigInteger value) {
        this.value = value;
    }

    public static UfoAmount of(long value) {
        if (value < 0L) throw new IllegalArgumentException("amount must be non-negative");
        return value == 0L ? ZERO : value == 1L ? ONE : new UfoAmount(BigInteger.valueOf(value));
    }

    public static UfoAmount of(BigInteger value) {
        Objects.requireNonNull(value, "value");
        if (value.signum() < 0) throw new IllegalArgumentException("amount must be non-negative");
        return value.signum() == 0 ? ZERO : value.equals(BigInteger.ONE) ? ONE : new UfoAmount(value);
    }

    public static UfoAmount parse(String value) {
        return parse(value, DEFAULT_MAX_BITS);
    }

    public static UfoAmount parse(String value, int maxBits) {
        Objects.requireNonNull(value, "value");
        if (maxBits < 1) throw new IllegalArgumentException("maxBits must be positive");
        if (value.isEmpty() || value.charAt(0) == '+' || value.chars().anyMatch(c -> c < '0' || c > '9')) {
            throw new IllegalArgumentException("amount must be an unsigned decimal integer");
        }
        BigInteger parsed = new BigInteger(value);
        if (parsed.bitLength() > maxBits) throw new IllegalArgumentException("amount exceeds " + maxBits + " bits");
        return of(parsed);
    }

    public BigInteger asBigInteger() {
        return this.value;
    }

    public boolean isZero() {
        return this.value.signum() == 0;
    }

    public UfoAmount add(UfoAmount other) {
        return of(this.value.add(require(other).value));
    }

    public UfoAmount subtract(UfoAmount other) {
        BigInteger result = this.value.subtract(require(other).value);
        if (result.signum() < 0) throw new ArithmeticException("amount underflow");
        return of(result);
    }

    public UfoAmount subtractClamped(UfoAmount other) {
        BigInteger result = this.value.subtract(require(other).value);
        return result.signum() <= 0 ? ZERO : of(result);
    }

    public UfoAmount multiply(long multiplier) {
        if (multiplier < 0L) throw new IllegalArgumentException("multiplier must be non-negative");
        return multiplier == 0L || isZero() ? ZERO : of(this.value.multiply(BigInteger.valueOf(multiplier)));
    }

    public UfoAmount multiply(BigInteger multiplier) {
        Objects.requireNonNull(multiplier, "multiplier");
        if (multiplier.signum() < 0) throw new IllegalArgumentException("multiplier must be non-negative");
        return multiplier.signum() == 0 || isZero() ? ZERO : of(this.value.multiply(multiplier));
    }

    public UfoAmount min(UfoAmount other) {
        return compareTo(require(other)) <= 0 ? this : other;
    }

    public UfoAmount max(UfoAmount other) {
        return compareTo(require(other)) >= 0 ? this : other;
    }

    public long longValueExact() {
        return this.value.longValueExact();
    }

    public long longValueSaturated() {
        return this.value.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) >= 0 ? Long.MAX_VALUE : this.value.longValue();
    }

    public int bitLength() {
        return this.value.bitLength();
    }

    @Override
    public int compareTo(UfoAmount other) {
        return this.value.compareTo(require(other).value);
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof UfoAmount other && this.value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return this.value.hashCode();
    }

    @Override
    public String toString() {
        return this.value.toString();
    }

    private static UfoAmount require(UfoAmount amount) {
        return Objects.requireNonNull(amount, "amount");
    }
}
