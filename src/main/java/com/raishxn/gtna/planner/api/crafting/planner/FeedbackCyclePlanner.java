package com.raishxn.gtna.planner.api.crafting.planner;

import com.raishxn.gtna.planner.api.amount.UfoAmount;
import com.raishxn.gtna.planner.api.crafting.planner.ImmutableCraftingGraph.CompiledPattern;
import com.raishxn.gtna.planner.api.crafting.planner.ImmutableCraftingGraph.PatternEntry;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Recognises a feedback loop that recycles one of its own inputs, states it as a single macro pattern
 * the balance machinery can already handle, and rewrites the resulting plan back to the real patterns
 * it stands for.
 *
 * <p>
 * The planner refuses a route into a key that is currently being expanded. That is what keeps it
 * safe on a cycle, and it is also why it cannot see a catalyst loop: it never notices that the key
 * comes back. A loop that recycles its catalyst is therefore reported as needing the whole catalyst
 * per firing, which for eight firings is eight times what the loop actually consumes.
 *
 * <p>
 * One turn of a component is ordered, its cost and yield are read off that order, and the loop
 * becomes a pattern whose working stock must be present and whose decay is consumed per firing. The
 * working stock is not the deepest point of the turn, it is that less one turn of decay, because the
 * last turn is never replenished: a turn may spend its working stock and finish. Writing {@code d}
 * for the depth of the turn, {@code c} for its net consumption and {@code n} for the turns a request
 * needs, the loop demands {@code (n-1)·c + d}, which is {@code (d-c) + n·c} and so is exactly the
 * decaying catalyst the graph already knows how to compile.
 *
 * <p>
 * The plan is then rewritten turn by turn rather than grouped. Grouping would let a replay — or a
 * machine — ask for the whole batch of the first pattern before the pattern that returns the catalyst
 * has run, which is the very thing the loop makes impossible. A request whose expansion would be
 * enormous is declined instead, because a plan that cannot be written down cannot be executed.
 *
 * <p>
 * Anything ambiguous is declined rather than guessed: a component key with more than one route, a
 * pattern that consumes two component keys, a loop that gains material, a turn shallower than its own
 * decay. A decline is reported by the base planner as the shortage it already reported.
 *
 * @param <K> resource key type
 */
public final class FeedbackCyclePlanner<K> {

    /** Ahead of any real recipe, so the loop is considered before the batch-at-a-time route. */
    private static final int MACRO_PRIORITY = 10_000;
    /** One step per pattern per turn: enough for any hand-built loop, small enough to reject a runaway. */
    private static final BigInteger MAX_TURNS = BigInteger.valueOf(100_000L);

    private final CraftingPattern<K> macro;
    private final List<CompiledPattern<K>> turn;
    private final ImmutableCraftingGraph<K> augmented;
    private final Rotation<K> chosen;
    private final Map<K, UfoAmount> inventory;

    private FeedbackCyclePlanner(ImmutableCraftingGraph<K> graph, Analysis<K> analysis,
                                 ImmutableCraftingGraph<K> augmented, Map<K, UfoAmount> inventory) {
        this.macro = analysis == null ? null : analysis.macro();
        this.turn = analysis == null ? List.of() : analysis.turn();
        this.chosen = analysis == null ? null : analysis.rotation();
        this.augmented = augmented;
        this.inventory = Map.copyOf(inventory);
    }

    /**
     * Analyses the graph for a loop that recycles part of itself while producing {@code target}.
     *
     * @param requested how much of the target is wanted, used to price the loop's decay
     */
    public static <K> FeedbackCyclePlanner<K> analyse(ImmutableCraftingGraph<K> graph, K target,
                                                      UfoAmount requested,
                                                      Map<K, UfoAmount> inventory) {
        Objects.requireNonNull(graph, "graph");
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(requested, "requested");
        Objects.requireNonNull(inventory, "inventory");
        Analysis<K> analysis = analyseComponent(graph, target, requested, inventory);
        if (analysis == null) {
            return new FeedbackCyclePlanner<>(graph, null, graph, inventory);
        }
        ArrayList<CraftingPattern<K>> patterns = new ArrayList<>(graph.patterns());
        patterns.add(analysis.macro());
        ImmutableCraftingGraph<K> augmented = ImmutableCraftingGraph.create(graph.revision(), graph.keyComparator(),
                patterns);
        return new FeedbackCyclePlanner<>(graph, analysis, augmented, inventory);
    }

    /** True when a loop was recognised and {@link #augmentedGraph()} carries its macro pattern. */
    public boolean applies() {
        return macro != null;
    }

    /** The graph the base planner should be given: the original patterns plus the loop's macro. */
    public ImmutableCraftingGraph<K> augmentedGraph() {
        return augmented;
    }

    /**
     * Rewrites the macro out of a plan and back into the real patterns, one turn at a time. A plan
     * that never used the macro is returned untouched.
     */
    public CraftingPlan<K> expand(CraftingPlan<K> plan) {
        Objects.requireNonNull(plan, "plan");
        if (macro == null) {
            return plan;
        }
        ArrayList<CraftingPlan.Execution<K>> expanded = new ArrayList<>(plan.schedule().size());
        LinkedHashMap<CraftingPattern<K>, UfoAmount> executions = new LinkedHashMap<>();
        for (CraftingPlan.Execution<K> step : plan.schedule()) {
            if (!step.pattern().equals(macro)) {
                expanded.add(step);
                executions.merge(step.pattern(), step.runs(), UfoAmount::add);
                continue;
            }
            BigInteger turns = step.runs().asBigInteger();
            for (BigInteger i = BigInteger.ZERO; i.compareTo(turns) < 0; i = i.add(BigInteger.ONE)) {
                for (CompiledPattern<K> member : turn) {
                    expanded.add(new CraftingPlan.Execution<>(member.pattern(), UfoAmount.ONE));
                    executions.merge(member.pattern(), UfoAmount.ONE, UfoAmount::add);
                }
            }
        }
        UfoAmount runs = executions.values().stream().reduce(UfoAmount.ZERO, UfoAmount::add);
        CraftingPlan.PlanQuality quality = new CraftingPlan.PlanQuality(plan.complete(), executions.size(),
                runs, plan.quality().missingUnits(), plan.quality().overproducedUnits());
        LinkedHashMap<K, UfoAmount> remaining = new LinkedHashMap<>(plan.remaining());
        LinkedHashMap<K, UfoAmount> extracted = new LinkedHashMap<>(plan.extractedFromInventory());
        BigInteger turns = loopTurns(plan);
        // The macro speaks in working stock and decay; the real schedule speaks in what each turn
        // draws and leaves. The two agree in total, but not line by line, and the difference is
        // exactly the material the loop recycles: the first turn takes the whole working stock out of
        // the inventory and every later turn replaces one turn of decay. The declared numbers have to
        // be restated in the schedule's terms or a reader checking the totals finds material missing.
        for (K key : chosen.working().keySet()) {
            UfoAmount decay = chosen.decay().getOrDefault(key, UfoAmount.ZERO);
            UfoAmount initial = inventory.getOrDefault(key, UfoAmount.ZERO)
                    .add(plan.missing().getOrDefault(key, UfoAmount.ZERO));
            UfoAmount left = initial.subtractClamped(decay.multiply(turns));
            UfoAmount drawn = chosen.working().get(key).add(decay.multiply(turns.subtract(BigInteger.ONE)));
            if (left.isZero()) {
                remaining.remove(key);
            } else {
                remaining.put(key, left);
            }
            // Only what the inventory actually holds can be declared as taken; the rest is what the
            // reported shortage is for. Declaring the whole requirement would claim a withdrawal the
            // stock cannot cover, and the plan would read as extracting more than it was given.
            UfoAmount taken = drawn.min(inventory.getOrDefault(key, UfoAmount.ZERO));
            if (taken.isZero()) {
                extracted.remove(key);
            } else {
                extracted.put(key, taken);
            }
        }
        return new CraftingPlan<>(plan.target(), plan.requested(), executions, extracted,
                plan.missing(), remaining, expanded, quality, plan.shortage());
    }

    private BigInteger loopTurns(CraftingPlan<K> plan) {
        BigInteger turns = BigInteger.ZERO;
        for (CraftingPlan.Execution<K> step : plan.schedule()) {
            if (step.pattern().equals(macro)) {
                turns = turns.add(step.runs().asBigInteger());
            }
        }
        return turns;
    }

    private record Analysis<K>(CraftingPattern<K> macro, List<CompiledPattern<K>> turn,
                               Rotation<K> rotation) {}

    private static <K> Analysis<K> analyseComponent(ImmutableCraftingGraph<K> graph, K target,
                                                    UfoAmount requested,
                                                    Map<K, UfoAmount> inventory) {
        List<CompiledPattern<K>> targetRoutes = graph.compiledPatternsFor(target);
        if (targetRoutes.size() != 1) {
            return null;
        }
        Map<K, List<CompiledPattern<K>>> producers = new HashMap<>();
        for (CompiledPattern<K> pattern : graph.compiledPatterns()) {
            for (PatternEntry<K> output : pattern.outputs()) {
                producers.computeIfAbsent(output.key(), ignored -> new ArrayList<>()).add(pattern);
            }
        }
        List<CompiledPattern<K>> cycle = findCycle(targetRoutes.get(0), producers);
        // A single-pattern loop is self-feeding growth, which the planner already funds a seed at a
        // time. Handling it here as well would leave two mechanisms disagreeing over the same plan.
        if (cycle == null || cycle.size() < 2) {
            return null;
        }
        return buildMacro(cycle, target, requested, inventory);
    }

    /**
     * Walks from the target's recipe along the single route into each of its inputs until a pattern
     * repeats. Anything with a choice or a fork on the way is not a loop this can price.
     */
    private static <K> List<CompiledPattern<K>> findCycle(CompiledPattern<K> start,
                                                          Map<K, List<CompiledPattern<K>>> producers) {
        ArrayList<CompiledPattern<K>> chain = new ArrayList<>();
        IdentityHashMap<CompiledPattern<K>, Integer> seen = new IdentityHashMap<>();
        CompiledPattern<K> current = start;
        while (true) {
            Integer at = seen.get(current);
            if (at != null) {
                // The target's own recipe has to be inside the loop, or the turn would not make it.
                return at == 0 ? reverse(chain) : null;
            }
            seen.put(current, chain.size());
            chain.add(current);
            CompiledPattern<K> next = null;
            for (PatternEntry<K> input : current.inputs()) {
                if (input.reusable()) {
                    continue;
                }
                List<CompiledPattern<K>> routes = producers.get(input.key());
                if (routes == null || routes.isEmpty()) {
                    continue;
                }
                if (routes.size() != 1) {
                    return null;
                }
                if (next != null && !next.equals(routes.get(0))) {
                    return null;
                }
                next = routes.get(0);
            }
            if (next == null) {
                return null;
            }
            current = next;
        }
    }

    private static <K> List<CompiledPattern<K>> reverse(List<CompiledPattern<K>> chain) {
        ArrayList<CompiledPattern<K>> out = new ArrayList<>(chain.size());
        for (int i = chain.size() - 1; i >= 0; i--) {
            out.add(chain.get(i));
        }
        return out;
    }

    /**
     * Prices every rotation of the loop and keeps the one needing the fewest kinds of seed, because a
     * loop primed by a single key is the one a plan can actually be written for.
     */
    private static <K> Analysis<K> buildMacro(List<CompiledPattern<K>> cycle, K target,
                                              UfoAmount requested,
                                              Map<K, UfoAmount> inventory) {
        Map<K, UfoAmount> perTurnOutput = new LinkedHashMap<>();
        Map<K, UfoAmount> perTurnExternal = new LinkedHashMap<>();
        ArrayList<K> keys = new ArrayList<>();
        for (CompiledPattern<K> pattern : cycle) {
            for (PatternEntry<K> output : pattern.outputs()) {
                perTurnOutput.merge(output.key(), output.amount(), UfoAmount::add);
                if (!keys.contains(output.key())) {
                    keys.add(output.key());
                }
            }
        }
        Set<K> members = Set.copyOf(keys);
        for (CompiledPattern<K> pattern : cycle) {
            for (PatternEntry<K> input : pattern.inputs()) {
                if (!input.reusable() && !members.contains(input.key())) {
                    perTurnExternal.merge(input.key(), input.amount(), UfoAmount::add);
                }
            }
        }
        UfoAmount perTurn = perTurnOutput.getOrDefault(target, UfoAmount.ZERO);
        if (perTurn.isZero()) {
            return null;
        }
        BigInteger turns = ceilDiv(requested.asBigInteger(), perTurn.asBigInteger());
        if (turns.multiply(BigInteger.valueOf(cycle.size())).compareTo(MAX_TURNS) > 0) {
            return null;
        }

        Rotation<K> best = null;
        for (int offset = 0; offset < cycle.size(); offset++) {
            Rotation<K> candidate = price(cycle, offset, keys, turns, inventory);
            if (candidate != null && (best == null || candidate.betterThan(best))) {
                best = candidate;
            }
        }
        if (best == null) {
            return null;
        }

        LinkedHashMap<K, UfoAmount> inputs = new LinkedHashMap<>(perTurnExternal);
        LinkedHashMap<K, UfoAmount> reusable = new LinkedHashMap<>();
        for (Map.Entry<K, UfoAmount> working : best.working.entrySet()) {
            K key = working.getKey();
            // The stated working stock is the depth of the turn less one turn of decay, so that the
            // batch's own decay plus it come to the depth of every turn but the last. Stating the whole
            // requirement as working stock instead would be checked once and never charged.
            UfoAmount decay = best.decay.getOrDefault(key, UfoAmount.ZERO);
            reusable.put(key, working.getValue().subtract(decay));
            if (!decay.isZero()) {
                inputs.merge(key, decay, UfoAmount::add);
            }
        }
        CraftingPattern<K> macro = new CraftingPattern<>("feedback:" + target, MACRO_PRIORITY, inputs,
                reusable, Map.of(target, perTurn), Set.of(target));
        return new Analysis<>(macro, best.turn, best);
    }

    private record Rotation<K>(List<CompiledPattern<K>> turn, Map<K, UfoAmount> working,
                               Map<K, UfoAmount> decay, int seedCount, BigInteger total,
                               int shortfallKinds, BigInteger shortfall) {

        /**
         * A loop has as many rotations as it has patterns and they are not equally usable: priming the
         * wrong key asks for material the request never had, which reads as a shortage on a scenario
         * that is perfectly feasible. The rotation whose seed the inventory already covers wins, then
         * the smallest shortage, then the fewest kinds of seed.
         */
        boolean betterThan(Rotation<K> other) {
            if (shortfallKinds != other.shortfallKinds) return shortfallKinds < other.shortfallKinds;
            if (!shortfall.equals(other.shortfall)) return shortfall.compareTo(other.shortfall) < 0;
            return seedCount != other.seedCount ? seedCount < other.seedCount : total.compareTo(other.total) < 0;
        }
    }

    /**
     * Prices one rotation. Walking the turn in this order gives, for each loop key, how deep it goes
     * and how much it nets; the seed is the depth plus what every turn but the last has to replace,
     * because the last turn may spend its seed and finish.
     */
    private static <K> Rotation<K> price(List<CompiledPattern<K>> cycle, int offset, List<K> keys,
                                         BigInteger turns, Map<K, UfoAmount> inventory) {
        ArrayList<CompiledPattern<K>> turn = new ArrayList<>(cycle.size());
        for (int i = 0; i < cycle.size(); i++) {
            turn.add(cycle.get((offset + i) % cycle.size()));
        }
        LinkedHashMap<K, UfoAmount> working = new LinkedHashMap<>();
        LinkedHashMap<K, UfoAmount> decay = new LinkedHashMap<>();
        BigInteger total = BigInteger.ZERO;
        BigInteger shortfall = BigInteger.ZERO;
        int shortfallKinds = 0;
        for (K key : keys) {
            UfoAmount consumed = UfoAmount.ZERO;
            UfoAmount produced = UfoAmount.ZERO;
            UfoAmount consumedSoFar = UfoAmount.ZERO;
            UfoAmount producedBefore = UfoAmount.ZERO;
            UfoAmount deepest = UfoAmount.ZERO;
            for (CompiledPattern<K> pattern : turn) {
                UfoAmount stepConsumed = UfoAmount.ZERO;
                for (PatternEntry<K> input : pattern.inputs()) {
                    if (!input.reusable() && input.key().equals(key)) {
                        stepConsumed = stepConsumed.add(input.amount());
                    }
                }
                // A pattern draws its inputs before it produces anything, so the deepest point of the
                // turn is what it has to have in hand at that moment, not what it holds afterwards.
                consumedSoFar = consumedSoFar.add(stepConsumed);
                deepest = deepest.max(consumedSoFar.subtractClamped(producedBefore));
                consumed = consumed.add(stepConsumed);
                for (PatternEntry<K> output : pattern.outputs()) {
                    if (output.key().equals(key)) {
                        producedBefore = producedBefore.add(output.amount());
                    }
                }
                produced = producedBefore;
            }
            UfoAmount net = consumed.subtractClamped(produced);
            if (net.isZero()) {
                if (!deepest.isZero()) {
                    working.put(key, deepest);
                    total = total.add(deepest.asBigInteger());
                    BigInteger gap = deepest.subtractClamped(
                            inventory.getOrDefault(key, UfoAmount.ZERO)).asBigInteger();
                    if (gap.signum() > 0) {
                        shortfall = shortfall.add(gap);
                        shortfallKinds++;
                    }
                }
                continue;
            }
            // A loop that gains material of its own accord is a different capability; a turn shallower
            // than its own decay cannot be stated as a working stock at all.
            if (consumed.compareTo(produced) < 0 || deepest.compareTo(net) < 0) {
                return null;
            }
            UfoAmount requirement = net.multiply(turns.subtract(BigInteger.ONE)).add(deepest);
            working.put(key, deepest);
            decay.put(key, net);
            total = total.add(requirement.asBigInteger());
            BigInteger gap = requirement.subtractClamped(
                    inventory.getOrDefault(key, UfoAmount.ZERO)).asBigInteger();
            if (gap.signum() > 0) {
                shortfall = shortfall.add(gap);
                shortfallKinds++;
            }
        }
        return new Rotation<>(List.copyOf(turn), working, decay, working.size(), total,
                shortfallKinds, shortfall);
    }

    private static BigInteger ceilDiv(BigInteger numerator, BigInteger denominator) {
        BigInteger[] quotient = numerator.divideAndRemainder(denominator);
        return quotient[1].signum() == 0 ? quotient[0] : quotient[0].add(BigInteger.ONE);
    }
}
