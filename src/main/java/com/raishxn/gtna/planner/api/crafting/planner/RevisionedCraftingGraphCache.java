package com.raishxn.gtna.planner.api.crafting.planner;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.LongFunction;

/** Thread-safe single-snapshot cache. A changed grid revision can never reuse the prior graph. */
public final class RevisionedCraftingGraphCache<K> {

    private final AtomicReference<Entry<K>> current = new AtomicReference<>();
    private final AtomicLong hits = new AtomicLong();
    private final AtomicLong misses = new AtomicLong();

    public ImmutableCraftingGraph<K> getOrBuild(long revision,
                                                LongFunction<ImmutableCraftingGraph<K>> factory) {
        if (revision < 0L) throw new IllegalArgumentException("revision must be non-negative");
        Objects.requireNonNull(factory, "factory");
        Entry<K> observed = current.get();
        if (observed != null && observed.revision == revision) {
            hits.incrementAndGet();
            return observed.graph;
        }
        synchronized (this) {
            observed = current.get();
            if (observed != null && observed.revision == revision) {
                hits.incrementAndGet();
                return observed.graph;
            }
            ImmutableCraftingGraph<K> graph = Objects.requireNonNull(factory.apply(revision), "graph");
            if (graph.revision() != revision) {
                throw new IllegalArgumentException("factory returned graph for revision " + graph.revision());
            }
            // A delayed request for an older revision must not evict the latest grid snapshot.
            if (observed == null || observed.revision < revision) current.set(new Entry<>(revision, graph));
            misses.incrementAndGet();
            return graph;
        }
    }

    public synchronized void invalidate() {
        current.set(null);
    }

    public CacheStats stats() {
        return new CacheStats(hits.get(), misses.get());
    }

    public record CacheStats(long hits, long misses) {}

    private record Entry<K>(long revision, ImmutableCraftingGraph<K> graph) {}
}
