package com.raishxn.gtna.common.machine.multiblock.electric;

/** Integer-only operation budget shared by the Universal Factory's simultaneous recipes. */
public final class UniversalFactoryBudget {

    private UniversalFactoryBudget() {}

    public static int parallelPerThread(int capacity, int selectedThreads) {
        int safeCapacity = Math.max(1, capacity);
        int threads = Math.max(1, Math.min(selectedThreads, safeCapacity));
        return safeCapacity / threads;
    }

    public static int remaining(int capacity, long occupied) {
        return (int) Math.max(0, Math.min(Integer.MAX_VALUE, (long) Math.max(1, capacity) -
                Math.max(0, occupied)));
    }
}
