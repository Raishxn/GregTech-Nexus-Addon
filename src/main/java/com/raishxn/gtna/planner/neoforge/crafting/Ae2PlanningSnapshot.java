package com.raishxn.gtna.planner.neoforge.crafting;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.stacks.AEKey;
import com.raishxn.gtna.planner.api.crafting.planner.ImmutableCraftingGraph;
import com.raishxn.gtna.planner.api.crafting.planner.PlanningCancellation;

import java.io.Serial;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.LongSupplier;

/**
 * Server-thread capture boundary. Only String keys and exact amounts enter the mathematical graph.
 * Native handles are used solely to build the final AE2 plan, never queried by the planning worker.
 *
 * <p>
 * A snapshot is published only after a complete capture, and it carries the conservative byte
 * weight the capture accumulated so the cache can be bounded by memory, not only by entry count.
 */
public record Ae2PlanningSnapshot(ImmutableCraftingGraph<String> graph, String target,
                                  Map<String, AEKey> keys, Map<AEKey, String> keyIds,
                                  Map<String, IPatternDetails> patterns, Map<String, Integer> amountsPerByte,
                                  boolean multiplePaths, long estimatedBytes) {

    public Ae2PlanningSnapshot {
        keys = Map.copyOf(keys);
        keyIds = Map.copyOf(keyIds);
        patterns = Map.copyOf(patterns);
        amountsPerByte = Map.copyOf(amountsPerByte);
        if (estimatedBytes < 0L) throw new IllegalArgumentException("estimatedBytes must be non-negative");
    }

    /** Throws Declined before publishing anything if any reachable pattern needs richer semantics. */
    public static Ae2PlanningSnapshot capture(Level level, ICraftingService service, AEKey target, long revision) {
        return capture(level, service, target, revision, CaptureLimits.DEFAULT);
    }

    /**
     * One-shot capture: drains the whole graph in a single unbounded slice. The bridge uses the
     * cooperative path instead, so this entry point only serves callers that accept a blocking capture.
     */
    public static Ae2PlanningSnapshot capture(Level level, ICraftingService service, AEKey target, long revision,
                                              CaptureLimits limits) {
        var source = new Ae2CaptureSource(level, service);
        var capture = new CooperativeGraphCapture<>(source, source.targetId(target), revision, limits,
                PlanningCancellation.NEVER);
        var slice = new CooperativeGraphCapture.Slice(Long.MAX_VALUE, Integer.MAX_VALUE);
        while (capture.advance(slice) == CooperativeGraphCapture.Status.YIELDED) {
            // An unbounded slice only yields if a total limit is about to be refused; advance reports it.
        }
        return of(capture.result(), source.keys());
    }

    /**
     * Publishes a completed cooperative capture. A graph key without a native handle would make the
     * final AE2 plan impossible to rebuild, so it is refused instead of being dropped silently.
     */
    public static Ae2PlanningSnapshot of(CooperativeGraphCapture.Captured<IPatternDetails> captured,
                                         Map<String, AEKey> nativeKeys) {
        Map<String, AEKey> keys = new LinkedHashMap<>();
        Map<AEKey, String> keyIds = new HashMap<>();
        captured.keys().forEach((id, details) -> {
            AEKey key = nativeKeys.get(id);
            if (key == null) throw new IllegalStateException("captured key without a native handle: " + id);
            keys.put(id, key);
            keyIds.put(key, id);
        });
        return new Ae2PlanningSnapshot(captured.graph(), captured.target(), keys, keyIds, captured.handles(),
                captured.amountsPerByte(), captured.multiplePaths(), captured.estimatedBytes());
    }

    /**
     * Limits of one capture. Total limits bound its cumulative active work across slices; slice limits
     * bound one slice. The edge allowance makes a slice reproducible, the time allowance keeps one
     * unusually fat key from monopolizing a tick, and the byte ceiling bounds the cache a snapshot may
     * occupy.
     */
    public record CaptureLimits(Duration timeout, int maxEdges, int maxKeys, long maxEstimatedBytes,
                                long sliceNanos, int sliceEdges) {

        /** Default per-grid slice: 2 ms and 512 edges, the roadmap target for one grid and tick. */
        public static final long DEFAULT_SLICE_NANOS = 2_000_000L;
        public static final int DEFAULT_SLICE_EDGES = 512;
        private static final CaptureLimits DEFAULT = new CaptureLimits(Duration.ofMillis(50), 100_000, 25_000,
                64L * 1024 * 1024);

        /** One-shot limits: a single unbounded slice under the same total ceilings. */
        public CaptureLimits(Duration timeout, int maxEdges, int maxKeys, long maxEstimatedBytes) {
            this(timeout, maxEdges, maxKeys, maxEstimatedBytes, Long.MAX_VALUE, Integer.MAX_VALUE);
        }

        public CaptureLimits {
            if (timeout == null || timeout.isZero() || timeout.isNegative() || maxEdges < 1 || maxKeys < 1 ||
                    maxEstimatedBytes < 1 || sliceNanos < 1L || sliceEdges < 1) {
                throw new IllegalArgumentException("snapshot limits must be positive");
            }
        }
    }

    /**
     * Deterministic accounting for one capture. Total limits are checked on every edge; slice limits
     * are checked between units only - one key lookup or one pattern - so the work a slice cannot
     * interrupt is one atomic tail, and that tail is reported instead of being smoothed away.
     */
    static final class CaptureBudget {

        private static final long EDGE_BYTES = 64;
        private static final long KEY_OVERHEAD_BYTES = 256;
        private static final long PATTERN_OVERHEAD_BYTES = 256;
        private final CaptureLimits limits;
        private final LongSupplier nanoTime;
        private final long timeoutNanos;
        private long activeNanos;
        private int edges;
        private int keys;
        private long estimatedBytes;
        private long sliceStarted;
        private long sliceNanos = Long.MAX_VALUE;
        private int sliceEdges = Integer.MAX_VALUE;
        private int sliceEdgeCount;
        private boolean sliceActive;

        CaptureBudget(CaptureLimits limits) {
            this(limits, System::nanoTime);
        }

        CaptureBudget(CaptureLimits limits, LongSupplier nanoTime) {
            this.limits = Objects.requireNonNull(limits, "limits");
            this.nanoTime = Objects.requireNonNull(nanoTime, "nanoTime");
            long nanos;
            try {
                nanos = limits.timeout().toNanos();
            } catch (ArithmeticException overflow) {
                nanos = Long.MAX_VALUE;
            }
            timeoutNanos = nanos;
        }

        /** Starts one slice with its own edge and time allowance. */
        void beginSlice(long nanos, int maxEdges) {
            if (sliceActive) throw new IllegalStateException("capture slice already active");
            sliceStarted = nanoTime.getAsLong();
            sliceNanos = nanos;
            sliceEdges = maxEdges;
            sliceEdgeCount = 0;
            sliceActive = true;
        }

        /** Charges only time spent inside a slice; idle server ticks are not capture work. */
        void endSlice() {
            if (!sliceActive) return;
            long elapsed = sliceElapsed();
            activeNanos = elapsed > Long.MAX_VALUE - activeNanos ? Long.MAX_VALUE : activeNanos + elapsed;
            sliceActive = false;
        }

        /**
         * Whether the current slice used its allowance. Checked between units, so a slice that already
         * started one always finishes it and progress never depends on wall-clock resolution.
         */
        boolean sliceExhausted() {
            return sliceEdgeCount >= sliceEdges || sliceElapsed() >= sliceNanos;
        }

        /** Edges the current slice really consumed, including the atomic tail it could not interrupt. */
        int sliceEdgesUsed() {
            return sliceEdgeCount;
        }

        void checkpoint() {
            long elapsed = sliceActive ? sliceElapsed() : 0L;
            if (activeNanos >= timeoutNanos || elapsed >= timeoutNanos - activeNanos) {
                throw new Declined("snapshot time limit");
            }
        }

        void edge() {
            if (++edges > limits.maxEdges()) throw new Declined("snapshot edge limit");
            sliceEdgeCount++;
            addBytes(EDGE_BYTES);
            checkpoint();
        }

        void key(String id) {
            if (++keys > limits.maxKeys()) throw new Declined("snapshot key limit");
            addBytes(KEY_OVERHEAD_BYTES + stringBytes(id));
        }

        void pattern(String id) {
            addBytes(PATTERN_OVERHEAD_BYTES + stringBytes(id));
        }

        long estimatedBytes() {
            return estimatedBytes;
        }

        private long sliceElapsed() {
            return Math.max(0L, nanoTime.getAsLong() - sliceStarted);
        }

        private static long stringBytes(String value) {
            try {
                return Math.multiplyExact((long) value.length(), Character.BYTES);
            } catch (ArithmeticException overflow) {
                throw new Declined("snapshot memory limit");
            }
        }

        private void addBytes(long amount) {
            if (amount > limits.maxEstimatedBytes() - estimatedBytes) {
                throw new Declined("snapshot memory limit");
            }
            estimatedBytes += amount;
        }
    }

    /** Sorted compound keys preserve components without depending on HashMap/NBT iteration order. */
    static String canonical(Tag tag) {
        if (tag instanceof CompoundTag compound) {
            List<String> names = new ArrayList<>(compound.getAllKeys());
            names.sort(Comparator.naturalOrder());
            StringBuilder result = new StringBuilder("{");
            for (String name : names) {
                result.append(StringTag.quoteAndEscape(name)).append(':')
                        .append(canonical(compound.get(name))).append(',');
            }
            return result.append('}').toString();
        }
        if (tag instanceof ListTag list) {
            StringBuilder result = new StringBuilder("[");
            for (Tag entry : list) result.append(canonical(entry)).append(',');
            return result.append(']').toString();
        }
        return tag.toString();
    }

    public static final class Declined extends RuntimeException {

        @Serial
        private static final long serialVersionUID = 1L;

        Declined(String reason) {
            super(reason, null, false, false);
        }
    }
}
