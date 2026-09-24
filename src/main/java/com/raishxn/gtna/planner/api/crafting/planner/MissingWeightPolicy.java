package com.raishxn.gtna.planner.api.crafting.planner;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The operator's side of the weight contract: a default multiplier plus per-key multipliers, read from
 * {@code core.toml} so a pack can disagree with an addon without recompiling either.
 *
 * <p>
 * The consumer declares base weights through {@link MissingWeights}; this policy scales them. A key
 * listed in {@code overrides} uses that multiplier instead of the default, and a key that has an
 * override but no declared base weight is weighted even if no consumer registered it. A multiplier of
 * one with no overrides returns the registered map unchanged, which is what keeps an unconfigured
 * installation byte for byte on the path a request without weights took before.
 */
public record MissingWeightPolicy(long defaultMultiplier, Map<String, Long> overrides) {

    /** The unconfigured policy: every multiplier is one and nothing is overridden. */
    public static final MissingWeightPolicy NONE = new MissingWeightPolicy(1L, Map.of());

    public MissingWeightPolicy {
        if (defaultMultiplier < 1L) {
            throw new IllegalArgumentException("the missing-weight multiplier is at least one");
        }
        Objects.requireNonNull(overrides, "overrides");
        LinkedHashMap<String, Long> copy = new LinkedHashMap<>();
        overrides.forEach((key, multiplier) -> {
            Objects.requireNonNull(key, "override key");
            if (key.isBlank()) {
                throw new IllegalArgumentException("a missing-weight override needs a serialized key");
            }
            if (multiplier == null || multiplier < 1L) {
                throw new IllegalArgumentException("a missing-weight override multiplier is at least one: " + key);
            }
            if (multiplier != 1L) {
                copy.put(key, multiplier);
            }
        });
        overrides = Collections.unmodifiableMap(copy);
    }

    /**
     * Effective weights for a registered base map. When nothing is configured the registered map is
     * returned unchanged, and an entry that ends up weighing one is dropped so the planner's own
     * normalization sees no work to do.
     */
    public Map<String, Long> effective(Map<String, Long> registered) {
        Objects.requireNonNull(registered, "registered");
        if (registered.isEmpty() && overrides.isEmpty()) {
            return Map.of();
        }
        if (defaultMultiplier == 1L && overrides.isEmpty()) {
            return registered;
        }
        LinkedHashMap<String, Long> effective = new LinkedHashMap<>();
        registered.forEach((key, weight) -> {
            long multiplier = overrides.getOrDefault(key, defaultMultiplier);
            putWeighted(effective, key, saturatedMultiply(weight, multiplier));
        });
        overrides.forEach((key, multiplier) -> {
            if (!registered.containsKey(key)) {
                putWeighted(effective, key, multiplier);
            }
        });
        return Collections.unmodifiableMap(effective);
    }

    /** Effective weights for the current {@link MissingWeights#registered()} declarations. */
    public Map<String, Long> effective() {
        return effective(MissingWeights.registered());
    }

    /** Reads the {@code "key=multiplier"} entries the config file stores, or the empty map. */
    public static Map<String, Long> parseOverrides(List<? extends String> entries) {
        Objects.requireNonNull(entries, "entries");
        if (entries.isEmpty()) {
            return Map.of();
        }
        LinkedHashMap<String, Long> parsed = new LinkedHashMap<>();
        for (Object raw : entries) {
            String text = raw == null ? "" : raw.toString().trim();
            if (text.isEmpty()) {
                continue;
            }
            int separator = text.indexOf('=');
            if (separator <= 0 || separator == text.length() - 1) {
                throw new IllegalArgumentException("a missing-weight override is \"key=multiplier\": " + text);
            }
            String key = text.substring(0, separator).trim();
            long multiplier;
            try {
                multiplier = Long.parseLong(text.substring(separator + 1).trim());
            } catch (NumberFormatException notANumber) {
                throw new IllegalArgumentException("a missing-weight override is \"key=multiplier\": " + text);
            }
            if (key.isEmpty() || multiplier < 1L) {
                throw new IllegalArgumentException("a missing-weight override is \"key=multiplier\": " + text);
            }
            parsed.put(key, multiplier);
        }
        return parsed;
    }

    /** True when an entry is the shape the config file documents, used to validate it at load time. */
    public static boolean isValidOverride(Object value) {
        if (!(value instanceof String text)) {
            return false;
        }
        try {
            parseOverrides(List.of(text));
            return !text.isBlank();
        } catch (IllegalArgumentException invalid) {
            return false;
        }
    }

    private static void putWeighted(Map<String, Long> target, String key, long weight) {
        if (weight > 1L) {
            target.put(key, weight);
        }
    }

    private static long saturatedMultiply(long weight, long multiplier) {
        if (weight > Long.MAX_VALUE / multiplier) {
            return Long.MAX_VALUE;
        }
        return weight * multiplier;
    }
}
