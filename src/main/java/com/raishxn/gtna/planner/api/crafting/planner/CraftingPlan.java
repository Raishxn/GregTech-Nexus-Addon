package com.raishxn.gtna.planner.api.crafting.planner;

import com.raishxn.gtna.planner.api.amount.UfoAmount;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Immutable exact plan, including shortages and deterministic quality metrics. */
public record CraftingPlan<K>(K target, UfoAmount requested,
                              Map<CraftingPattern<K>, UfoAmount> patternExecutions,
                              Map<K, UfoAmount> extractedFromInventory,
                              Map<K, UfoAmount> missing,
                              Map<K, UfoAmount> remaining,
                              List<Execution<K>> schedule,
                              PlanQuality quality,
                              Shortage<K> shortage) {

    public CraftingPlan {
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(requested, "requested");
        patternExecutions = freeze(patternExecutions);
        extractedFromInventory = freeze(extractedFromInventory);
        missing = freeze(missing);
        remaining = freeze(remaining);
        schedule = List.copyOf(schedule);
        Objects.requireNonNull(quality, "quality");
        Objects.requireNonNull(shortage, "shortage");
    }

    /**
     * The shortage split by what the missing material is for, because the three are not the same request
     * to the player. A consumable is gone once used; a seed is handed back, so one unit covers the whole
     * batch; a carrier wears out over a fixed number of firings. A flat shortage tells the player to
     * gather five of something without saying that one of them comes back.
     *
     * <p>
     * The keys of the three maps are disjoint and together cover {@code missing}. A key missing for
     * more than one reason at once is charged in the order consumed, worn, handed back: what is really
     * consumed is the most actionable thing to report, and only what cannot be explained that way is
     * called a seed.
     */
    public record Shortage<K>(Map<K, UfoAmount> consumable, Map<K, UfoAmount> seed,
                              Map<K, UfoAmount> carrier) {

        public Shortage {
            consumable = freeze(consumable);
            seed = freeze(seed);
            carrier = freeze(carrier);
        }

        public boolean isEmpty() {
            return consumable.isEmpty() && seed.isEmpty() && carrier.isEmpty();
        }
    }

    public boolean complete() {
        return missing.isEmpty();
    }

    private static <T> Map<T, UfoAmount> freeze(Map<T, UfoAmount> map) {
        return Collections.unmodifiableMap(new LinkedHashMap<>(map));
    }

    public record Execution<K>(CraftingPattern<K> pattern, UfoAmount runs) {}

    public record PlanQuality(boolean complete, int distinctPatterns, UfoAmount totalExecutions,
                              UfoAmount missingUnits, UfoAmount overproducedUnits) {

        public PlanQuality {
            Objects.requireNonNull(totalExecutions, "totalExecutions");
            Objects.requireNonNull(missingUnits, "missingUnits");
            Objects.requireNonNull(overproducedUnits, "overproducedUnits");
        }
    }
}
