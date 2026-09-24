package com.raishxn.gtna.planner.neoforge.crafting;

/**
 * Bounded, allocation-free histograms for graph capture on the server thread.
 *
 * <p>
 * Gate O asks for a measured slice p95 per grid and per tick. One sample is recorded per slice -
 * the time one grid spent inside {@code advance} - and one per tick - the time every grid together
 * charged to the shared budget in that tick. A tick with no capture work records nothing, so idle
 * ticks cannot flatter the percentiles.
 *
 * <p>
 * Percentiles come from fixed buckets, so a reported p95 is the upper edge of the bucket the
 * sample landed in: an over-estimate, never a guess below the truth. Samples are only ever the
 * wall-clock readings the caller supplies, so the arithmetic is testable without a world.
 *
 * <p>
 * These samples describe the machine itself, including the atomic tail - one key lookup plus one
 * pattern - that a slice cannot interrupt. Measuring the live 2 ms target of a real AE2 grid is what
 * the in-game diagnostics are for; this class only makes the number observable.
 */
public final class CaptureSliceMetrics {

    /** Upper edges, in nanoseconds, of the fixed buckets; the last bucket catches any overflow. */
    static final long[] BOUNDS_NANOS = {
            25_000L, 50_000L, 100_000L, 200_000L, 400_000L, 800_000L,
            1_000_000L, 2_000_000L, 4_000_000L, 8_000_000L, 16_000_000L, 33_000_000L,
            66_000_000L, 133_000_000L, 266_000_000L, 533_000_000L,
    };

    private final Histogram slices = new Histogram();
    private final Histogram ticks = new Histogram();
    private long edges;
    private long keyNanos;
    private long patternNanos;
    private long publishNanos;

    /**
     * Records one slice: the wall-clock time it spent, the deterministic edges it consumed and how
     * that time split between the key lookup, the patterns and the publish step.
     */
    public void recordSlice(long nanos, int edges, long keyNanos, long patternNanos, long publishNanos) {
        if (edges < 0 || keyNanos < 0L || patternNanos < 0L || publishNanos < 0L) {
            throw new IllegalArgumentException("capture slice accounting must be non-negative");
        }
        slices.record(nanos);
        this.edges += edges;
        this.keyNanos += keyNanos;
        this.patternNanos += patternNanos;
        this.publishNanos += publishNanos;
    }

    /** Records one tick with capture activity: the time all grids together spent inside a slice. */
    public void recordTick(long nanos) {
        ticks.record(nanos);
    }

    /** One consistent reading of every histogram and accumulator. */
    public Snapshot snapshot() {
        return new Snapshot(slices.total(), slices.percentile(50), slices.percentile(95), slices.percentile(99),
                slices.maximum(), slices.sum(), edges, ticks.total(), ticks.percentile(50), ticks.percentile(95),
                ticks.percentile(99), ticks.maximum(), ticks.sum(), keyNanos, patternNanos, publishNanos,
                slices.overflow());
    }

    /**
     * One reading of every histogram. The percentiles are bucket upper edges, the totals are exact, and
     * the phase accumulators are subsets of {@code sliceTotalNanos} - what is left over is the slice
     * loop itself.
     */
    public record Snapshot(long slices, long sliceP50Nanos, long sliceP95Nanos, long sliceP99Nanos,
                           long sliceMaxNanos, long sliceTotalNanos, long edges,
                           long ticks, long tickP50Nanos, long tickP95Nanos, long tickP99Nanos,
                           long tickMaxNanos, long tickTotalNanos,
                           long keyNanos, long patternNanos, long publishNanos, long overflowSamples) {

        /** Mean wall-clock time of one slice, in nanoseconds, or zero before the first sample. */
        public long meanSliceNanos() {
            return slices == 0L ? 0L : sliceTotalNanos / slices;
        }

        /** Mean edges a slice consumed, or zero before the first sample. */
        public long meanSliceEdges() {
            return slices == 0L ? 0L : edges / slices;
        }
    }

    /** Fixed-bucket histogram: counts only, no per-sample allocation and no unbounded growth. */
    static final class Histogram {

        private final long[] counts = new long[BOUNDS_NANOS.length + 1];
        private long total;
        private long maximum;
        private long sum;
        private long overflow;

        void record(long nanos) {
            if (nanos < 0L) throw new IllegalArgumentException("sample must be non-negative");
            total++;
            sum += nanos;
            if (nanos > maximum) maximum = nanos;
            for (int bucket = 0; bucket < BOUNDS_NANOS.length; bucket++) {
                if (nanos <= BOUNDS_NANOS[bucket]) {
                    counts[bucket]++;
                    return;
                }
            }
            counts[BOUNDS_NANOS.length]++;
            overflow++;
        }

        /** Upper edge of the bucket the percentile fell in, or the observed maximum when it overflowed. */
        long percentile(int percentile) {
            if (percentile < 1 || percentile > 100) {
                throw new IllegalArgumentException("percentile must be within 1..100");
            }
            if (total == 0L) return 0L;
            long target = (total * percentile + 99L) / 100L;
            long cumulative = 0L;
            for (int bucket = 0; bucket < BOUNDS_NANOS.length; bucket++) {
                cumulative += counts[bucket];
                if (cumulative >= target) return BOUNDS_NANOS[bucket];
            }
            return maximum;
        }

        long total() {
            return total;
        }

        long maximum() {
            return maximum;
        }

        long sum() {
            return sum;
        }

        long overflow() {
            return overflow;
        }
    }
}
