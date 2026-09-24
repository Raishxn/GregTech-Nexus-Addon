package com.raishxn.gtna.planner.api.crafting.planner;

import com.raishxn.gtna.planner.api.amount.UfoAmount;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable input snapshot for one plan.
 *
 * @param missingWeights how much worse one missing unit of a key is than another, so a route that
 *                       would leave a cheap material short is preferred over one that leaves an
 *                       expensive one short. Weights are exact integers rather than fractions so the
 *                       comparison stays exact and the plan stays reproducible. A key that is absent
 *                       weighs one, and a weight of one is not stored, which is what lets a request
 *                       with no weights at all take exactly the path it took before weights existed.
 */
public record PlanningRequest<K>(K target, UfoAmount amount, Map<K, UfoAmount> inventory,
                                 PlanningLimits limits, PlanningCancellation cancellation,
                                 Map<K, Long> missingWeights) {

    public PlanningRequest {
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(amount, "amount");
        if (amount.isZero()) throw new IllegalArgumentException("requested amount must be positive");
        Objects.requireNonNull(inventory, "inventory");
        LinkedHashMap<K, UfoAmount> copy = new LinkedHashMap<>();
        inventory.forEach((key, value) -> {
            Objects.requireNonNull(key, "inventory key");
            Objects.requireNonNull(value, "inventory amount");
            if (!value.isZero()) copy.put(key, value);
        });
        inventory = Collections.unmodifiableMap(copy);
        limits = Objects.requireNonNull(limits, "limits");
        cancellation = Objects.requireNonNull(cancellation, "cancellation");
        LinkedHashMap<K, Long> weights = new LinkedHashMap<>();
        Objects.requireNonNull(missingWeights, "missingWeights").forEach((key, weight) -> {
            Objects.requireNonNull(key, "missing weight key");
            if (weight == null || weight < 1L) {
                throw new IllegalArgumentException("a missing weight is at least one: " + key);
            }
            if (weight != 1L) weights.put(key, weight);
        });
        missingWeights = Collections.unmodifiableMap(weights);
    }

    public PlanningRequest(K target, UfoAmount amount, Map<K, UfoAmount> inventory,
                           PlanningLimits limits, PlanningCancellation cancellation) {
        this(target, amount, inventory, limits, cancellation, Map.of());
    }

    public PlanningRequest(K target, UfoAmount amount, Map<K, UfoAmount> inventory) {
        this(target, amount, inventory, PlanningLimits.DEFAULT, PlanningCancellation.NEVER, Map.of());
    }

    /** One for a key nobody weighted, which is every key in a request that declares no weights. */
    public long weightOf(K key) {
        return missingWeights.getOrDefault(key, 1L);
    }
}
