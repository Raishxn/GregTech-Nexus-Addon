package com.raishxn.gtna.planner.neoforge.crafting;

import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.function.ToLongFunction;

/**
 * Least-recently-used snapshot cache bounded by entries <em>and</em> estimated bytes.
 *
 * <p>
 * A pure entry cap is not a memory cap: one snapshot of a large grid can weight far more than the
 * rest together. Eviction therefore always trims the least recently used entry until both the entry
 * count and the byte total fit, and the byte total is the conservative estimate the capture itself
 * accumulated while building the snapshot.
 */
final class CaptureSnapshotCache<K, V> {

    private final LinkedHashMap<K, V> entries = new LinkedHashMap<>(16, .75F, true);
    private final ToLongFunction<V> weigher;
    private int maxEntries;
    private long maxBytes;
    private long bytes;
    private long evictions;

    CaptureSnapshotCache(int maxEntries, long maxBytes, ToLongFunction<V> weigher) {
        this.weigher = Objects.requireNonNull(weigher, "weigher");
        require(maxEntries, maxBytes);
        this.maxEntries = maxEntries;
        this.maxBytes = maxBytes;
    }

    /** Applies the current policy limits, trimming immediately when they shrank. */
    void configure(int maxEntries, long maxBytes) {
        require(maxEntries, maxBytes);
        this.maxEntries = maxEntries;
        this.maxBytes = maxBytes;
        trim();
    }

    V get(K key) {
        return entries.get(key);
    }

    void put(K key, V value) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");
        V previous = entries.put(key, value);
        if (previous != null) bytes -= weight(previous);
        bytes += weight(value);
        trim();
    }

    void clear() {
        entries.clear();
        bytes = 0L;
    }

    int size() {
        return entries.size();
    }

    long bytes() {
        return bytes;
    }

    long evictions() {
        return evictions;
    }

    private void trim() {
        var iterator = entries.entrySet().iterator();
        while (iterator.hasNext() && (entries.size() > maxEntries || bytes > maxBytes)) {
            long weight = weight(iterator.next().getValue());
            iterator.remove();
            bytes -= weight;
            if (bytes < 0L) bytes = 0L;
            evictions++;
        }
    }

    private long weight(V value) {
        long weight = weigher.applyAsLong(value);
        return Math.max(0L, weight);
    }

    private static void require(int maxEntries, long maxBytes) {
        if (maxEntries < 1 || maxBytes < 1L) {
            throw new IllegalArgumentException("cache limits must be positive");
        }
    }
}
