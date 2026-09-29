package com.raishxn.gtna.common.data;

import java.math.BigInteger;

/** Exact, fractional-carry accounting for loss applied at the wireless network input. */
public final class NexusWirelessLoss {

    private static final BigInteger BASIS = BigInteger.valueOf(10_000);

    private NexusWirelessLoss() {}

    public record Transfer(BigInteger gross, BigInteger credited, BigInteger lost, int remainder) {

        public static Transfer empty(int remainder) {
            return new Transfer(BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO, remainder);
        }
    }

    public static Transfer accept(BigInteger offered, BigInteger space, int lossBasisPoints, int remainder) {
        if (offered.signum() <= 0 || space.signum() <= 0) {
            return Transfer.empty(remainder);
        }
        if (lossBasisPoints < 0 || lossBasisPoints > 10_000 || remainder < 0 || remainder >= 10_000) {
            throw new IllegalArgumentException("Invalid wireless loss rate or remainder");
        }
        BigInteger gross = offered;
        if (credit(gross, lossBasisPoints, remainder).compareTo(space) > 0) {
            BigInteger low = BigInteger.ZERO;
            BigInteger high = offered;
            while (low.compareTo(high) < 0) {
                BigInteger mid = low.add(high).add(BigInteger.ONE).shiftRight(1);
                if (credit(mid, lossBasisPoints, remainder).compareTo(space) <= 0) low = mid;
                else high = mid.subtract(BigInteger.ONE);
            }
            gross = low;
        }
        BigInteger[] lossAndRemainder = gross.multiply(BigInteger.valueOf(lossBasisPoints))
                .add(BigInteger.valueOf(remainder)).divideAndRemainder(BASIS);
        BigInteger credited = gross.subtract(lossAndRemainder[0]);
        return new Transfer(gross, credited, lossAndRemainder[0], lossAndRemainder[1].intValue());
    }

    private static BigInteger credit(BigInteger gross, int lossBasisPoints, int remainder) {
        return gross.subtract(gross.multiply(BigInteger.valueOf(lossBasisPoints))
                .add(BigInteger.valueOf(remainder)).divide(BASIS));
    }
}
