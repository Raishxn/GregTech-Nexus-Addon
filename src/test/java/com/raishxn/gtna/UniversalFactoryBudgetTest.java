package com.raishxn.gtna;

import com.raishxn.gtna.common.machine.multiblock.electric.UniversalFactoryBudget;

public final class UniversalFactoryBudgetTest {

    private UniversalFactoryBudgetTest() {}

    public static void main(String[] args) {
        for (int capacity : new int[] { 1, 2, 4, 8, 16, 128, 1048576 }) {
            for (int threads = 1; threads <= Math.min(256, capacity); threads++) {
                int parallel = UniversalFactoryBudget.parallelPerThread(capacity, threads);
                if (parallel < 1 || (long) parallel * threads > capacity) {
                    throw new AssertionError("budget exceeded for capacity=" + capacity + ", threads=" + threads);
                }
            }
        }
        if (UniversalFactoryBudget.parallelPerThread(2, 2) != 1 ||
                UniversalFactoryBudget.parallelPerThread(8, 3) != 2 ||
                UniversalFactoryBudget.remaining(8, 6) != 2 ||
                UniversalFactoryBudget.remaining(8, 9) != 0 ||
                UniversalFactoryBudget.remaining(8, Long.MAX_VALUE) != 0) {
            throw new AssertionError("shared budget or active-recipe reservation is incorrect");
        }
    }
}
