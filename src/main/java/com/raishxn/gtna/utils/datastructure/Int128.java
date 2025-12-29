package com.raishxn.gtna.utils.datastructure;

import com.google.common.primitives.Ints;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;

public final class Int128 extends Number implements Comparable<Int128> {
    private long high;
    private long low;
    public static final Int128 MAX_VALUE = new Int128(Long.MAX_VALUE, -1L);
    public static final Int128 MIN_VALUE = new Int128(Long.MIN_VALUE, 0L);

    public static Int128 ZERO() {
        return new Int128(0L, 0L);
    }

    public static Int128 ONE() {
        return new Int128(0L, 1L);
    }

    public static Int128 NEGATIVE_ONE() {
        return new Int128(-1L, -1L);
    }

    public Int128() {
        this.high = 0L;
        this.low = 0L;
    }

    public int intValue() {
        return Ints.saturatedCast(this.longValue());
    }

    public long longValue() {
        if ((this.high != 0L || this.low < 0L) && (this.high != -1L || this.low >= 0L)) {
            return this.isNegative() ? Long.MIN_VALUE : Long.MAX_VALUE;
        } else {
            return this.low;
        }
    }

    public float floatValue() {
        return (float)this.toDouble();
    }

    public double doubleValue() {
        return this.toDouble();
    }

    public Int128(long high, long low) {
        this.high = high;
        this.low = low;
    }

    public Int128(long value) {
        this.high = value < 0L ? -1L : 0L;
        this.low = value;
    }

    public Int128 set(long high, long low) {
        this.high = high;
        this.low = low;
        return this;
    }

    public Int128 set(Int128 other) {
        this.high = other.high;
        this.low = other.low;
        return this;
    }

    public Int128 add(long other) {
        long newLow = this.low + other;
        long newHigh = this.high;
        if (Long.compareUnsigned(newLow, this.low) < 0) {
            ++newHigh;
        }

        if (other < 0L) {
            --newHigh;
        }

        this.low = newLow;
        this.high = newHigh;
        return this;
    }

    public Int128 add(Int128 other) {
        long newLow = this.low + other.low;
        long newHigh = this.high + other.high;
        if (Long.compareUnsigned(newLow, this.low) < 0) {
            ++newHigh;
        }

        this.low = newLow;
        this.high = newHigh;
        return this;
    }

    public static Int128 add(Int128 a, Int128 b, Int128 result) {
        long newLow = a.low + b.low;
        long newHigh = a.high + b.high;
        if (Long.compareUnsigned(newLow, a.low) < 0) {
            ++newHigh;
        }

        result.low = newLow;
        result.high = newHigh;
        return result;
    }

    public Int128 subtract(Int128 other) {
        long newLow = this.low - other.low;
        long newHigh = this.high - other.high;
        if (Long.compareUnsigned(this.low, other.low) < 0) {
            --newHigh;
        }

        this.low = newLow;
        this.high = newHigh;
        return this;
    }

    public static Int128 subtract(Int128 a, Int128 b, Int128 result) {
        long newLow = a.low - b.low;
        long newHigh = a.high - b.high;
        if (Long.compareUnsigned(a.low, b.low) < 0) {
            --newHigh;
        }

        result.low = newLow;
        result.high = newHigh;
        return result;
    }

    public Int128 multiply(Int128 other) {
        long a0 = this.low & 4294967295L;
        long a1 = this.low >>> 32;
        long a2 = this.high & 4294967295L;
        long a3 = this.high >>> 32;
        long b0 = other.low & 4294967295L;
        long b1 = other.low >>> 32;
        long b2 = other.high & 4294967295L;
        long b3 = other.high >>> 32;
        long p0 = a0 * b0;
        long p1 = a0 * b1 + a1 * b0;
        long p2 = a0 * b2 + a1 * b1 + a2 * b0;
        long p3 = a0 * b3 + a1 * b2 + a2 * b1 + a3 * b0;
        p1 += p0 >>> 32;
        p2 += p1 >>> 32;
        p3 += p2 >>> 32;
        this.low = p1 << 32 | p0 & 4294967295L;
        this.high = p3 << 32 | p2 & 4294967295L;
        return this;
    }

    public static Int128 multiply(Int128 a, Int128 b, Int128 result) {
        long a0 = a.low & 4294967295L;
        long a1 = a.low >>> 32;
        long a2 = a.high & 4294967295L;
        long a3 = a.high >>> 32;
        long b0 = b.low & 4294967295L;
        long b1 = b.low >>> 32;
        long b2 = b.high & 4294967295L;
        long b3 = b.high >>> 32;
        long p0 = a0 * b0;
        long p1 = a0 * b1 + a1 * b0;
        long p2 = a0 * b2 + a1 * b1 + a2 * b0;
        long p3 = a0 * b3 + a1 * b2 + a2 * b1 + a3 * b0;
        p1 += p0 >>> 32;
        p2 += p1 >>> 32;
        p3 += p2 >>> 32;
        result.low = p1 << 32 | p0 & 4294967295L;
        result.high = p3 << 32 | p2 & 4294967295L;
        return result;
    }

    public Int128 multiply(long multiplier) {
        long a0 = this.low & 4294967295L;
        long a1 = this.low >>> 32;
        long a2 = this.high & 4294967295L;
        long a3 = this.high >>> 32;
        long m0 = multiplier & 4294967295L;
        long m1 = multiplier >>> 32;
        long p0 = a0 * m0;
        long p1 = a0 * m1 + a1 * m0;
        long p2 = a1 * m1 + a2 * m0;
        long p3 = a2 * m1 + a3 * m0;
        p1 += p0 >>> 32;
        p2 += p1 >>> 32;
        p3 += p2 >>> 32;
        this.low = p1 << 32 | p0 & 4294967295L;
        this.high = p3 << 32 | p2 & 4294967295L;
        return this;
    }

    public static Int128 multiply(Int128 a, long multiplier, Int128 result) {
        result.set(a.high, a.low);
        return result.multiply(multiplier);
    }

    public Int128 divide(Int128 divisor, Int128 remainder) {
        if (divisor.isZero()) {
            throw new ArithmeticException("Division by zero");
        } else if (this.isZero()) {
            remainder.set(0L, 0L);
            return this.set(0L, 0L);
        } else {
            boolean negativeResult = this.isNegative() != divisor.isNegative();
            Int128 dividend = new Int128(this.high, this.low);
            Int128 div = new Int128(divisor.high, divisor.low);
            if (dividend.isNegative()) {
                dividend.negate();
            }

            if (div.isNegative()) {
                div.negate();
            }

            Int128 quotient = new Int128();
            Int128 temp = new Int128();

            for(int i = 127; i >= 0; --i) {
                temp.shiftLeft(1);
                if (dividend.getBit(i)) {
                    temp.low |= 1L;
                }

                if (temp.compareTo(div) >= 0) {
                    temp.subtract(div);
                    quotient.setBit(i, true);
                }
            }

            remainder.set(temp);
            this.set(quotient);
            if (negativeResult) {
                this.negate();
            }

            return this;
        }
    }

    public Int128 divide(long divisor) {
        if (divisor == 0L) {
            throw new ArithmeticException("Division by zero");
        } else {
            boolean neg = this.isNegative() != divisor < 0L;
            if (this.isNegative()) {
                this.negate();
            }

            if (divisor < 0L) {
                divisor = -divisor;
            }

            long rem = 0L;
            long resultHigh = 0L;
            long resultLow = 0L;
            if (this.high != 0L) {
                resultHigh = Long.divideUnsigned(this.high, divisor);
                rem = Long.remainderUnsigned(this.high, divisor);
            }

            if (rem != 0L) {
                long combined = rem << 32 | this.low >>> 32;
                long q1 = Long.divideUnsigned(combined, divisor);
                rem = Long.remainderUnsigned(combined, divisor);
                combined = rem << 32 | this.low & 4294967295L;
                long q0 = Long.divideUnsigned(combined, divisor);
                resultLow = q1 << 32 | q0;
            } else {
                resultLow = Long.divideUnsigned(this.low, divisor);
            }

            this.high = resultHigh;
            this.low = resultLow;
            if (neg) {
                this.negate();
            }

            return this;
        }
    }

    public Int128 divideNew(long divisor) {
        if (divisor == 0L) {
            throw new ArithmeticException("Division by zero");
        } else {
            Int128 result = new Int128(this.high, this.low);
            boolean neg = result.isNegative() != divisor < 0L;
            if (result.isNegative()) {
                result.negate();
            }

            long absDivisor = divisor < 0L ? -divisor : divisor;
            long rem = 0L;
            long resultHigh = 0L;
            long resultLow = 0L;
            if (result.high != 0L) {
                resultHigh = Long.divideUnsigned(result.high, absDivisor);
                rem = Long.remainderUnsigned(result.high, absDivisor);
            }

            if (rem != 0L) {
                long combined = rem << 32 | result.low >>> 32;
                long q1 = Long.divideUnsigned(combined, absDivisor);
                rem = Long.remainderUnsigned(combined, absDivisor);
                combined = rem << 32 | result.low & 4294967295L;
                long q0 = Long.divideUnsigned(combined, absDivisor);
                resultLow = q1 << 32 | q0;
            } else {
                resultLow = Long.divideUnsigned(result.low, absDivisor);
            }

            result.high = resultHigh;
            result.low = resultLow;
            if (neg) {
                result.negate();
            }

            return result;
        }
    }

    public Int128 shiftLeft(int n) {
        n &= 127;
        if (n >= 64) {
            this.high = this.low << n - 64;
            this.low = 0L;
        } else if (n > 0) {
            this.high = this.high << n | this.low >>> 64 - n;
            this.low <<= n;
        }

        return this;
    }

    public Int128 shiftRight(int n) {
        n &= 127;
        if (n >= 64) {
            this.low = this.high >> n - 64;
            this.high >>= 63;
        } else if (n > 0) {
            this.low = this.low >>> n | this.high << 64 - n;
            this.high >>= n;
        }

        return this;
    }

    public Int128 shiftRightUnsigned(int n) {
        n &= 127;
        if (n >= 64) {
            this.low = this.high >>> n - 64;
            this.high = 0L;
        } else if (n > 0) {
            this.low = this.low >>> n | this.high << 64 - n;
            this.high >>>= n;
        }

        return this;
    }

    public Int128 negate() {
        this.low = ~this.low;
        this.high = ~this.high;
        ++this.low;
        if (this.low == 0L) {
            ++this.high;
        }

        return this;
    }

    public int compareTo(Int128 other) {
        boolean thisNeg = this.isNegative();
        boolean otherNeg = other.isNegative();
        if (thisNeg != otherNeg) {
            return thisNeg ? -1 : 1;
        } else {
            return this.high != other.high ? Long.compare(this.high, other.high) : Long.compareUnsigned(this.low, other.low);
        }
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof Int128)) {
            return false;
        } else {
            Int128 other = (Int128)obj;
            return this.high == other.high && this.low == other.low;
        }
    }

    public boolean isZero() {
        return this.high == 0L && this.low == 0L;
    }

    public boolean isNegative() {
        return this.high < 0L;
    }

    public boolean isPositive() {
        return !this.isNegative() && !this.isZero();
    }

    public boolean getBit(int index) {
        if (index < 64) {
            return (this.low & 1L << index) != 0L;
        } else {
            return (this.high & 1L << index - 64) != 0L;
        }
    }

    public void setBit(int index, boolean value) {
        if (index < 64) {
            if (value) {
                this.low |= 1L << index;
            } else {
                this.low &= ~(1L << index);
            }
        } else if (value) {
            this.high |= 1L << index - 64;
        } else {
            this.high &= ~(1L << index - 64);
        }

    }

    public long toLong() {
        return this.low;
    }

    public double toDouble() {
        return (double)this.high * Math.pow((double)2.0F, (double)64.0F) + (double)(this.low & Long.MAX_VALUE) + (this.low < 0L ? Math.pow((double)2.0F, (double)63.0F) : (double)0.0F);
    }

    public BigInteger toBigInteger() {
        if (this.isZero()) {
            return BigInteger.ZERO;
        } else {
            byte[] bytes = new byte[16];

            for(int i = 0; i < 8; ++i) {
                bytes[i] = (byte)((int)(this.high >>> 56 - i * 8));
            }

            for(int i = 0; i < 8; ++i) {
                bytes[i + 8] = (byte)((int)(this.low >>> 56 - i * 8));
            }

            return new BigInteger(bytes);
        }
    }

    public static Int128 fromBigInteger(BigInteger value) {
        if (value == null) {
            return ZERO();
        } else if (value.bitLength() > 127) {
            throw new ArithmeticException("BigInteger too large for Int128: " + String.valueOf(value));
        } else if (value.equals(BigInteger.ZERO)) {
            return new Int128(0L, 0L);
        } else if (value.equals(BigInteger.ONE)) {
            return new Int128(0L, 1L);
        } else {
            byte[] bytes = value.toByteArray();
            long high = 0L;
            long low = 0L;
            int len = bytes.length;

            for(int i = 0; i < Math.min(8, len); ++i) {
                int byteIndex = len - 1 - i;
                if (byteIndex >= 0) {
                    low |= (long)(bytes[byteIndex] & 255) << i * 8;
                }
            }

            for(int i = 8; i < Math.min(16, len); ++i) {
                int byteIndex = len - 1 - i;
                if (byteIndex >= 0) {
                    high |= (long)(bytes[byteIndex] & 255) << (i - 8) * 8;
                }
            }

            if (value.signum() < 0 && len < 16) {
                if (len <= 8) {
                    if (len < 8) {
                        low |= -1L << len * 8;
                    }

                    high = -1L;
                } else {
                    high |= -1L << (len - 8) * 8;
                }
            }

            return new Int128(high, low);
        }
    }

    public String toString() {
        if (this.isZero()) {
            return "0";
        } else {
            return this.high != 0L && (this.high != -1L || this.low >= 0L) ? this.toStringFast() : Long.toString(this.low);
        }
    }

    private String toStringFast() {
        boolean negative = this.isNegative();
        long workHigh = negative ? ~this.high : this.high;
        long workLow = negative ? ~this.low + 1L : this.low;
        if (negative && workLow == 0L) {
            ++workHigh;
        }

        if (workHigh == 0L) {
            return negative ? "-" + workLow : Long.toString(workLow);
        } else {
            char[] digits = new char[40];
            int pos = digits.length;

            long quotientLow = 0;
            for(long BILLION = 1000000000L; workHigh != 0L || workLow != 0L; workLow = quotientLow) {
                long quotientHigh;
                long remainder;
                if (workHigh == 0L) {
                    quotientHigh = 0L;
                    quotientLow = workLow / 1000000000L;
                    remainder = workLow % 1000000000L;
                } else {
                    long temp = workHigh % 1000000000L;
                    quotientHigh = workHigh / 1000000000L;
                    long combined = temp << 32 | workLow >>> 32;
                    long q1 = combined / 1000000000L;
                    temp = combined % 1000000000L;
                    combined = temp << 32 | workLow & 4294967295L;
                    long q0 = combined / 1000000000L;
                    remainder = combined % 1000000000L;
                    quotientLow = q1 << 32 | q0;
                }

                for(int i = 0; i < 9 && (remainder != 0L || workHigh != 0L || workLow != quotientLow * 1000000000L + remainder); ++i) {
                    --pos;
                    digits[pos] = (char)((int)(48L + remainder % 10L));
                    remainder /= 10L;
                }

                workHigh = quotientHigh;
            }

            if (negative) {
                --pos;
                digits[pos] = '-';
            }

            return new String(digits, pos, digits.length - pos);
        }
    }

    public static Int128 fromString(@NotNull String str) {
        str = str.trim();
        if (str.isEmpty()) {
            throw new NumberFormatException("empty string");
        } else {
            return fromDecimalString(str);
        }
    }

    public static Int128 fromString(@NotNull String str, Int128 defaultValue) {
        try {
            return fromString(str);
        } catch (Exception var3) {
            return defaultValue;
        }
    }

    private static Int128 fromDecimalString(@NotNull String str) {
        boolean negative = false;
        int start = 0;
        if (str.charAt(0) == '-') {
            negative = true;
            start = 1;
        } else if (str.charAt(0) == '+') {
            start = 1;
        }

        if (start >= str.length()) {
            throw new NumberFormatException("no digits");
        } else {
            if (str.length() - start <= 18) {
                try {
                    long value = Long.parseLong(str.substring(start));
                    return new Int128(negative ? -value : value);
                } catch (NumberFormatException var7) {
                }
            }

            Int128 result = new Int128();
            Int128 ten = new Int128(10L);

            for(int i = start; i < str.length(); ++i) {
                char c = str.charAt(i);
                if (c < '0' || c > '9') {
                    throw new NumberFormatException("invalid digit: " + c);
                }

                result.multiply(ten);
                result.add(new Int128((long)(c - 48)));
            }

            if (negative) {
                result.negate();
            }

            return result;
        }
    }

    public static Int128 sum(Int128 a, Int128 b) {
        return a.add(b);
    }

    public String toHexString() {
        return String.format("%016X%016X", this.high, this.low);
    }

    public String toFormattedString(String separator) {
        if (separator == null) {
            separator = ",";
        }

        String baseStr = this.toString();
        if (baseStr.length() <= 3) {
            return baseStr;
        } else {
            boolean negative = baseStr.startsWith("-");
            String digits = negative ? baseStr.substring(1) : baseStr;
            StringBuilder formatted = new StringBuilder();
            int len = digits.length();

            for(int i = 0; i < len; ++i) {
                if (i > 0 && (len - i) % 3 == 0) {
                    formatted.append(separator);
                }

                formatted.append(digits.charAt(i));
            }

            if (negative) {
                formatted.insert(0, "-");
            }

            return formatted.toString();
        }
    }

    public String toFormattedString() {
        return this.toFormattedString(",");
    }

    public String toCompactString() {
        if (this.isZero()) {
            return "0";
        } else {
            String str = this.toString();
            boolean negative = str.startsWith("-");
            String digits = negative ? str.substring(1) : str;
            if (digits.length() <= 6) {
                return str;
            } else {
                char firstDigit = digits.charAt(0);
                String var10000 = String.valueOf(firstDigit);
                String mantissa = var10000 + "." + digits.substring(1, 4);
                int exponent = digits.length() - 1;
                String result = mantissa + "E+" + exponent;
                return negative ? "-" + result : result;
            }
        }
    }

    public String toHumanReadableString() {
        if (this.isZero()) {
            return "0";
        } else {
            String[] units = new String[]{"", "K", "M", "B", "T", "P", "E", "Z", "Y"};
            String str = this.toString();
            boolean negative = str.startsWith("-");
            String digits = negative ? str.substring(1) : str;
            if (digits.length() <= 3) {
                return str;
            } else {
                int unitIndex = (digits.length() - 1) / 3;
                if (unitIndex >= units.length) {
                    return this.toCompactString();
                } else {
                    int significantDigits = digits.length() - unitIndex * 3;
                    String integerPart = digits.substring(0, significantDigits);
                    StringBuilder result = new StringBuilder();
                    if (negative) {
                        result.append("-");
                    }

                    result.append(integerPart);
                    int remainingDigits = digits.length() - significantDigits;
                    if (remainingDigits > 0 && integerPart.length() < 3) {
                        result.append(".");
                        int decimalPlaces = Math.min(2, Math.min(remainingDigits, 3 - integerPart.length()));
                        result.append(digits, significantDigits, significantDigits + decimalPlaces);
                    }

                    result.append(units[unitIndex]);
                    return result.toString();
                }
            }
        }
    }

    public int hashCode() {
        return Long.hashCode(this.high) * 31 + Long.hashCode(this.low);
    }

    public Int128 copy() {
        return new Int128(this.high, this.low);
    }

    public long getHigh() {
        return this.high;
    }

    public long getLow() {
        return this.low;
    }
}
