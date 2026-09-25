package com.raishxn.gtna.common.item.terminal;

import com.gregtechceu.gtceu.api.pattern.MultiblockState;

import net.minecraft.core.BlockPos;

/** Defers change-triggered checks of the target multiblock while its terminal is placing blocks. */
public final class NexusBuildCheckGuard {

    private static final ThreadLocal<MultiblockState> BUILDING = new ThreadLocal<>();

    private NexusBuildCheckGuard() {}

    public static void run(MultiblockState state, Runnable action) {
        MultiblockState previous = BUILDING.get();
        BUILDING.set(state);
        try {
            action.run();
        } finally {
            if (previous == null) {
                BUILDING.remove();
            } else {
                BUILDING.set(previous);
            }
        }
    }

    public static boolean skips(MultiblockState state, BlockPos changedPos) {
        return BUILDING.get() == state && !changedPos.equals(state.controllerPos);
    }
}
