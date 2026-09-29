package com.raishxn.gtna;

import com.raishxn.gtna.common.data.NexusWirelessLoss;

import java.math.BigInteger;

public final class NexusWirelessLossTest {

    private NexusWirelessLossTest() {}

    public static void main(String[] args) {
        check(1000, 1000, 500, 1000, 950, 50);
        check(1000, 100, 500, 105, 100, 5);
        check(1000, 0, 500, 0, 0, 0);
        check(1000, 1000, 0, 1000, 1000, 0);
        int remainder = 0;
        long credited = 0;
        long lost = 0;
        for (int i = 0; i < 20; i++) {
            var transfer = NexusWirelessLoss.accept(BigInteger.ONE, BigInteger.valueOf(100), 500, remainder);
            credited += transfer.credited().longValueExact();
            lost += transfer.lost().longValueExact();
            remainder = transfer.remainder();
        }
        if (credited != 19 || lost != 1 || remainder != 0) {
            throw new AssertionError("twenty one-EU inputs must lose exactly one EU");
        }
        BigInteger huge = BigInteger.ONE.shiftLeft(100);
        var large = NexusWirelessLoss.accept(huge, huge, 125, 0);
        if (!large.gross().equals(huge) || !large.credited().add(large.lost()).equals(huge)) {
            throw new AssertionError("large transfers must conserve energy without overflow");
        }
    }

    private static void check(long offered, long space, int basisPoints, long gross, long credit, long loss) {
        var result = NexusWirelessLoss.accept(BigInteger.valueOf(offered), BigInteger.valueOf(space),
                basisPoints, 0);
        if (result.gross().longValueExact() != gross || result.credited().longValueExact() != credit ||
                result.lost().longValueExact() != loss) {
            throw new AssertionError("loss calculation mismatch for offered=" + offered + ", space=" + space +
                    ": " + result);
        }
    }
}
