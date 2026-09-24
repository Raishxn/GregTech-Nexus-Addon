package com.raishxn.gtna.planner.api.crafting.planner;

import com.raishxn.gtna.planner.api.amount.UfoAmount;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Immutable crafting graph compiled once for one grid revision.
 * Pattern and ingredient iteration order is canonical and never depends on hash order.
 *
 * <p>
 * Within one pattern, an input that has a selectable route is resolved before an input that has
 * none. A deterministic coproduct is captured as a secondary output, so it is never a route: it can
 * only be collected after the sibling branch that produces it has run. Resolving it in raw key order
 * made a composite whose coproduct key sorts before its routable keys report an impossible shortage.
 */
public final class ImmutableCraftingGraph<K> {

    private final long revision;
    private final int keyCount;
    private final int edgeCount;
    private final int routeChoiceAlternatives;
    private final Comparator<? super K> keyComparator;
    private final List<CraftingPattern<K>> patterns;
    private final NavigableMap<K, List<CompiledPattern<K>>> byOutput;
    private final Map<K, List<CompiledPattern<K>>> bySecondaryOutput;
    private final Map<K, List<CompiledPattern<K>>> byInput;
    private final List<CompiledPattern<K>> compiled;
    private final List<K> simpleDemandOrder;

    private ImmutableCraftingGraph(long revision, Comparator<? super K> keyComparator,
                                   Collection<CraftingPattern<K>> source) {
        if (revision < 0L) throw new IllegalArgumentException("revision must be non-negative");
        this.revision = revision;
        this.keyComparator = Objects.requireNonNull(keyComparator, "keyComparator");
        ArrayList<CraftingPattern<K>> ordered = new ArrayList<>(Objects.requireNonNull(source, "patterns"));
        ordered.sort(Comparator.comparing(CraftingPattern::id));
        HashSet<String> ids = new HashSet<>();
        TreeMap<K, List<CompiledPattern<K>>> index = new TreeMap<>(keyComparator);
        // Built only when something is actually a secondary output. Every graph without one would
        // otherwise pay for a whole extra map on every compilation, and compilation is not rare.
        TreeMap<K, List<CompiledPattern<K>>> secondary = null;
        TreeMap<K, List<CompiledPattern<K>>> consumers = new TreeMap<>(keyComparator);
        ArrayList<CompiledPattern<K>> compiledPatterns = new ArrayList<>();
        TreeMap<K, K> uniqueKeys = new TreeMap<>(keyComparator);
        TreeSet<K> routable = new TreeSet<>(keyComparator);
        for (CraftingPattern<K> pattern : ordered) {
            Objects.requireNonNull(pattern, "pattern");
            if (!ids.add(pattern.id())) throw new IllegalArgumentException("duplicate pattern id: " + pattern.id());
            routable.addAll(pattern.craftableOutputs());
        }
        for (CraftingPattern<K> pattern : ordered) {
            CompiledPattern<K> compiled = compile(pattern, keyComparator, routable);
            compiledPatterns.add(compiled);
            for (K key : pattern.inputs().keySet()) checkKey(uniqueKeys, key);
            for (K key : pattern.reusableInputs().keySet()) checkKey(uniqueKeys, key);
            for (K key : pattern.outputs().keySet()) checkKey(uniqueKeys, key);
            for (PatternEntry<K> output : compiled.outputs()) {
                if (pattern.craftableOutputs().contains(output.key())) {
                    index.computeIfAbsent(output.key(), ignored -> new ArrayList<>()).add(compiled);
                } else {
                    // A secondary output is not a selectable route, but firing the pattern for its
                    // primary is a way to obtain it, and the only way when the primary is not wanted
                    // for its own sake. Indexed separately so nothing that asks for a selectable route
                    // starts seeing one that is not.
                    if (secondary == null) {
                        secondary = new TreeMap<>(keyComparator);
                    }
                    secondary.computeIfAbsent(output.key(), ignored -> new ArrayList<>()).add(compiled);
                }
            }
            for (PatternEntry<K> input : compiled.inputs()) {
                consumers.computeIfAbsent(input.key(), ignored -> new ArrayList<>()).add(compiled);
            }
        }
        index.replaceAll((key, value) -> List.copyOf(value));
        if (secondary != null) {
            secondary.replaceAll((key, value) -> List.copyOf(value));
        }
        this.patterns = List.copyOf(ordered);
        this.byOutput = Collections.unmodifiableNavigableMap(index);
        this.bySecondaryOutput = secondary == null ? Map.of() : Collections.unmodifiableMap(secondary);
        consumers.replaceAll((key, value) -> List.copyOf(value));
        this.byInput = Collections.unmodifiableMap(consumers);
        this.compiled = List.copyOf(compiledPatterns);
        this.simpleDemandOrder = compileSimpleDemandOrder(uniqueKeys.keySet());
        // Counted here rather than on demand: the compilation already visits every entry, and an
        // operator asking how large the graph is must not make every plan pay to be able to answer.
        this.keyCount = uniqueKeys.size();
        int edges = 0;
        for (CompiledPattern<K> pattern : compiledPatterns) {
            edges += pattern.inputs().size() + pattern.outputs().size();
        }
        this.edgeCount = edges;
        int alternatives = 0;
        for (K key : uniqueKeys.keySet()) {
            int routes = this.byOutput.getOrDefault(key, List.of()).size() +
                    this.bySecondaryOutput.getOrDefault(key, List.of()).size();
            alternatives += Math.max(0, routes - 1);
        }
        this.routeChoiceAlternatives = alternatives;
    }

    public static <K> ImmutableCraftingGraph<K> create(long revision,
                                                       Comparator<? super K> keyComparator,
                                                       Collection<CraftingPattern<K>> patterns) {
        return new ImmutableCraftingGraph<>(revision, keyComparator, patterns);
    }

    public long revision() {
        return revision;
    }

    /** Distinct resource keys in the graph. */
    public int keyCount() {
        return keyCount;
    }

    /** Compiled patterns, which is the number of recipes the planner may fire. */
    public int patternCount() {
        return compiled.size();
    }

    /** Input entries plus output entries across every compiled pattern. */
    public int edgeCount() {
        return edgeCount;
    }

    /**
     * Number of producer alternatives beyond the first, summed across keys.
     * This is a cheap predictor for the planner's reversible-search width: a
     * large recipe graph with one producer per key stays linear, while many
     * interchangeable producers can create a combinatorial proof search.
     */
    public int routeChoiceAlternatives() {
        return routeChoiceAlternatives;
    }

    public Comparator<? super K> keyComparator() {
        return keyComparator;
    }

    public List<CraftingPattern<K>> patterns() {
        return patterns;
    }

    public List<CraftingPattern<K>> patternsFor(K output) {
        List<CompiledPattern<K>> matches = byOutput.get(output);
        return matches == null ? List.of() : matches.stream().map(CompiledPattern::pattern).toList();
    }

    List<CompiledPattern<K>> compiledPatternsFor(K output) {
        return byOutput.getOrDefault(output, List.of());
    }

    /**
     * Patterns that yield {@code output} only as a secondary product. Empty for anything a recipe
     * declares as a selectable route, so a caller can fall back to these without ever preferring a
     * secondary over a route.
     */
    List<CompiledPattern<K>> secondaryRoutesFor(K output) {
        return bySecondaryOutput.getOrDefault(output, List.of());
    }

    List<CompiledPattern<K>> consumersOf(K input) {
        return byInput.getOrDefault(input, List.of());
    }

    List<CompiledPattern<K>> compiledPatterns() {
        return compiled;
    }

    /** Nonempty only for a single-route, single-output DAG. Ordered from products to ingredients. */
    List<K> simpleDemandOrder() {
        return simpleDemandOrder;
    }

    private void checkKey(TreeMap<K, K> keys, K key) {
        K previous = keys.putIfAbsent(key, key);
        if (previous != null && !previous.equals(key)) {
            throw new IllegalArgumentException("key comparator equates different resource keys");
        }
    }

    private List<K> compileSimpleDemandOrder(Set<K> keys) {
        // A catalyst is not a dependency the demand pass may propagate, and a durable carrier is not
        // consumed once per firing, so anything carrying either leaves the fast path and goes through
        // the planner that understands them.
        if (byOutput.values().stream().anyMatch(value -> value.size() != 1) ||
                compiled.stream().anyMatch(value -> value.outputs().size() != 1) ||
                compiled.stream().anyMatch(value -> value.inputs().stream()
                        .anyMatch(input -> input.reusable() || input.durable())))
            return List.of();
        Map<K, Integer> incoming = new HashMap<>();
        for (K key : keys) incoming.put(key, 0);
        for (CompiledPattern<K> pattern : compiled) {
            for (PatternEntry<K> input : pattern.inputs()) incoming.merge(input.key(), 1, Integer::sum);
        }
        ArrayDeque<K> ready = new ArrayDeque<>();
        for (K key : keys) if (incoming.get(key) == 0) ready.add(key);
        ArrayList<K> order = new ArrayList<>(keys.size());
        while (!ready.isEmpty()) {
            K key = ready.removeFirst();
            order.add(key);
            for (CompiledPattern<K> pattern : compiledPatternsFor(key)) {
                for (PatternEntry<K> input : pattern.inputs()) {
                    if (incoming.merge(input.key(), -1, Integer::sum) == 0) ready.addLast(input.key());
                }
            }
        }
        return order.size() == keys.size() ? List.copyOf(order) : List.of();
    }

    private static <K> CompiledPattern<K> compile(CraftingPattern<K> pattern,
                                                  Comparator<? super K> comparator, Set<K> routable) {
        return new CompiledPattern<>(pattern,
                entries(pattern.inputs(), pattern.reusableInputs(), pattern.durableUses(),
                        pattern.fuzzyVariants(), comparator, routable),
                entries(pattern.outputs(), Map.of(), Map.of(), Map.of(), comparator, null));
    }

    /**
     * Canonical entry order. When {@code routableFirst} is given, inputs with a selectable route come
     * before inputs that can only be collected as a deterministic coproduct of a sibling branch.
     */
    private static <K> List<PatternEntry<K>> entries(Map<K, UfoAmount> amounts,
                                                     Map<K, UfoAmount> reusable,
                                                     Map<K, Integer> durable,
                                                     Map<K, Set<K>> fuzzy,
                                                     Comparator<? super K> comparator,
                                                     Set<K> routableFirst) {
        // A catalyst is an input the pattern needs even though it is handed back, so the compiled
        // entry list carries both, with the flag telling the planner which is which. The extra list is
        // built only when there is a catalyst to merge: an extra list per pattern would otherwise be
        // charged to every graph, including the ones that have no reusable input at all.
        //
        // A decaying catalyst appears in both maps and must keep both entries. Collapsing them, as a
        // plain map merge does, silently drops the decay and reports a plan that runs out of catalyst.
        ArrayList<PatternEntry<K>> entries = new ArrayList<>(amounts.size() + reusable.size());
        if (reusable.isEmpty()) {
            amounts.forEach((key, amount) -> entries.add(new PatternEntry<>(key, amount,
                    durable.containsKey(key) ? EntryKind.DURABLE : EntryKind.EXACT,
                    durable.getOrDefault(key, 0), fuzzy.getOrDefault(key, Set.of()))));
        } else {
            reusable.forEach((key, amount) -> entries.add(new PatternEntry<>(key, amount,
                    EntryKind.REUSABLE, durable.getOrDefault(key, 0), fuzzy.getOrDefault(key, Set.of()))));
            amounts.forEach((key, amount) -> entries.add(new PatternEntry<>(key, amount,
                    reusable.containsKey(key) || !durable.containsKey(key) ? EntryKind.EXACT : EntryKind.DURABLE,
                    durable.getOrDefault(key, 0), fuzzy.getOrDefault(key, Set.of()))));
        }
        entries.sort((left, right) -> {
            if (routableFirst != null) {
                boolean leftRoutable = routableFirst.contains(left.key());
                boolean rightRoutable = routableFirst.contains(right.key());
                if (leftRoutable != rightRoutable) return leftRoutable ? -1 : 1;
            }
            return comparator.compare(left.key(), right.key());
        });
        return List.copyOf(entries);
    }

    /** How a compiled input is drawn from the plan. */
    enum EntryKind {
        /** Consumed outright, one amount per firing. */
        EXACT,
        /** Required to be present and handed back, so it is never consumed. */
        REUSABLE,
        /** Consumed, but one carrier survives several firings. */
        DURABLE
    }

    record PatternEntry<K>(K key, UfoAmount amount, EntryKind kind, int uses, Set<K> variants) {

        PatternEntry(K key, UfoAmount amount) {
            this(key, amount, EntryKind.EXACT, 0, Set.of());
        }

        boolean reusable() {
            return kind == EntryKind.REUSABLE;
        }

        boolean durable() {
            return kind == EntryKind.DURABLE;
        }
    }

    record CompiledPattern<K>(CraftingPattern<K> pattern, List<PatternEntry<K>> inputs,
                              List<PatternEntry<K>> outputs) {

        UfoAmount outputAmount(K key) {
            for (PatternEntry<K> output : outputs) if (output.key().equals(key)) return output.amount();
            throw new IllegalArgumentException("pattern does not produce requested key: " + key);
        }
    }
}
