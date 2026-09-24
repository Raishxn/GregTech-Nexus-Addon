package com.raishxn.gtna.planner.api.crafting.planner;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Registration point where a consumer declares how much worse one missing unit of a serialized key is
 * than another.
 *
 * <p>
 * The Core never derives material value: nothing in the network says what an item is worth, and the
 * planner must not guess about content. A consumer that does know declares exact integer weights here,
 * keyed by the same serialized identity the bridge and diagnostics already use. A key nobody registers
 * weighs one, and a weight of one is never stored, so a request that declares no weights keeps the
 * exact path it took before weights existed.
 *
 * <p>
 * Weights are read on the planner worker and may be registered from any thread. An attempt sees the
 * map as it was when that attempt started, so changing a weight while a plan is in flight cannot
 * rewrite the result: a plan is always reproducible from the weights it began with.
 *
 * <p>
 * This is a registration point, not content. No weight is filled in by the Core, and an installation
 * that registers nothing is byte for byte an installation from before the weights existed.
 */
public final class MissingWeights {

    private static final AtomicReference<Map<String, Long>> REGISTERED = new AtomicReference<>(Map.of());

    private MissingWeights() {}

    /**
     * Declares the weight of one serialized key, replacing any previous declaration. A weight of one
     * removes the declaration instead of storing it, because one is the default.
     */
    public static void register(String serializedKey, long weight) {
        Objects.requireNonNull(serializedKey, "serializedKey");
        if (serializedKey.isBlank()) {
            throw new IllegalArgumentException("a missing weight needs a serialized key");
        }
        if (weight < 1L) {
            throw new IllegalArgumentException("a missing weight is at least one: " + serializedKey);
        }
        synchronized (MissingWeights.class) {
            LinkedHashMap<String, Long> copy = new LinkedHashMap<>(REGISTERED.get());
            if (weight == 1L) {
                copy.remove(serializedKey);
            } else {
                copy.put(serializedKey, weight);
            }
            REGISTERED.set(Collections.unmodifiableMap(copy));
        }
    }

    /** Removes one declaration; the key falls back to the default weight of one. */
    public static void unregister(String serializedKey) {
        Objects.requireNonNull(serializedKey, "serializedKey");
        synchronized (MissingWeights.class) {
            Map<String, Long> current = REGISTERED.get();
            if (!current.containsKey(serializedKey)) {
                return;
            }
            LinkedHashMap<String, Long> copy = new LinkedHashMap<>(current);
            copy.remove(serializedKey);
            REGISTERED.set(Collections.unmodifiableMap(copy));
        }
    }

    /**
     * Drops every declaration, bringing the fast unweighted path back. Used by a consumer that reloads
     * its own content and by tests that must not leak a declaration into another test.
     */
    public static void clear() {
        REGISTERED.set(Map.of());
    }

    /**
     * The current declarations, immutable. While nothing is registered this is the constant empty map,
     * so reading it on the planning path allocates nothing.
     */
    public static Map<String, Long> registered() {
        return REGISTERED.get();
    }
}
