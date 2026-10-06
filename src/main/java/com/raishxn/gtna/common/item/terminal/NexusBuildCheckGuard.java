package com.raishxn.gtna.common.item.terminal;

import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.pattern.MultiblockState;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.api.machine.multiblock.GTNAStructureRefresh;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

/** Coalesces placement and Forge's later snapshot notifications into one check at tick end. */
@Mod.EventBusSubscriber(modid = GTNACORE.MOD_ID)
public final class NexusBuildCheckGuard {

    private static final ThreadLocal<MultiblockState> BUILDING = new ThreadLocal<>();
    private static final ThreadLocal<Set<MultiblockState>> PENDING = ThreadLocal.withInitial(
            () -> Collections.newSetFromMap(new IdentityHashMap<>()));

    private NexusBuildCheckGuard() {}

    public static void run(MultiblockState state, Runnable action) {
        MultiblockState previous = BUILDING.get();
        BUILDING.set(state);
        try {
            action.run();
        } finally {
            restore(previous);
            if (state.getWorld() instanceof ServerLevel) PENDING.get().add(state);
        }
    }

    private static void restore(MultiblockState previous) {
        if (previous == null) BUILDING.remove();
        else BUILDING.set(previous);
    }

    public static boolean skips(MultiblockState state, BlockPos changedPos) {
        // Controller removal must still invalidate immediately, including during Forge rollback.
        return !changedPos.equals(state.controllerPos) &&
                (BUILDING.get() == state || PENDING.get().contains(state));
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) flushPending();
    }

    /** Server-thread only. State identity scopes suppression to the specific build target. */
    public static void flushPending() {
        var states = Set.copyOf(PENDING.get());
        PENDING.get().clear();
        for (var state : states) {
            if (!(state.getWorld() instanceof ServerLevel level) || !level.isLoaded(state.controllerPos)) continue;
            if (!(state.getController() instanceof MultiblockControllerMachine controller)) continue;
            MultiblockState previous = BUILDING.get();
            BUILDING.set(state);
            try {
                GTNAStructureRefresh.refresh(controller, true);
            } finally {
                restore(previous);
            }
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        PENDING.remove();
        BUILDING.remove();
    }
}
