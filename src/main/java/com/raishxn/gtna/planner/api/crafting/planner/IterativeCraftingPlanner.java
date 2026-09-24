package com.raishxn.gtna.planner.api.crafting.planner;

import com.raishxn.gtna.planner.api.amount.UfoAmount;
import com.raishxn.gtna.planner.api.crafting.planner.ImmutableCraftingGraph.CompiledPattern;
import com.raishxn.gtna.planner.api.crafting.planner.ImmutableCraftingGraph.PatternEntry;

import java.io.Serial;
import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.LongSupplier;

/**
 * Exact, stack-safe planner over immutable snapshots.
 * Single-route DAGs aggregate demands in topological order. Other graphs use explicit
 * continuations and a reversible journal, including choices that conflict with later siblings.
 * Selection is deterministic. Between routes that are otherwise equally viable it prefers the one
 * with the smaller total leaf demand, computed bottom-up once per plan, so a correct plan is not
 * several times more expensive than it has to be. That is a preference, not a proof: the first
 * feasible plan is still returned, and general multi-route optimality under shared stock is not
 * claimed. Quantities are batched. A recipe that yields a key as a secondary output is a route to that
 * key like any other, so it competes with the recipes that declare it: the extra firings and the
 * primary that comes out alongside are priced by the same comparison, and the cheapest route wins
 * whichever kind it is. Naming a secondary as missing without considering the recipe that makes it
 * would name a key nobody can supply. Stateful feedback/catalyst optimization
 * belongs to a separate adapter, and is one: see {@link FeedbackCyclePlanner}, which states a
 * recycling loop as a decaying catalyst this planner can balance and rewrites the plan back to the
 * real patterns. That adapter composes with this one rather than being built into it, so a caller
 * that does not want it pays nothing for it.
 */
public final class IterativeCraftingPlanner<K> {

    private final LongSupplier nanoTime;

    public IterativeCraftingPlanner() {
        this(System::nanoTime);
    }

    IterativeCraftingPlanner(LongSupplier nanoTime) {
        this.nanoTime = Objects.requireNonNull(nanoTime);
    }

    /**
     * How many comparison links can decide a route choice, in the order the comparator consults them:
     * pattern priority, whether the route is short of an input, whether its leaf demand is known, its
     * leaf demand, its reachability rank, its shortage deficit, its input cost, its execution count,
     * its yield and finally its identifier. The histogram is exposed by diagnostics so an operator can
     * say <em>why</em> a route won, which the chosen plan alone cannot.
     */
    public static final int CHOICE_LINK_COUNT = 10;

    /**
     * One comparison step without materialising the comparator twice: whether link {@code index}
     * separates the winner from the runner-up. Only the two best candidates are compared, once, and
     * only when a choice actually exists, which is what keeps the diagnostic off the common path.
     */
    private static <K> boolean decides(int index, K key, Candidate<K> left, Candidate<K> right) {
        return switch (index) {
            case 0 -> left.pattern.pattern().priority() != right.pattern.pattern().priority();
            case 1 -> (left.deficit.signum() != 0) != (right.deficit.signum() != 0);
            case 2 -> (left.leafCost == null) != (right.leafCost == null);
            case 3 -> left.leafCost != null && right.leafCost != null && left.leafCost.compareTo(right.leafCost) != 0;
            case 4 -> left.rank != right.rank;
            case 5 -> left.deficit.compareTo(right.deficit) != 0;
            case 6 -> left.cost.compareTo(right.cost) != 0;
            case 7 -> left.runs.compareTo(right.runs) != 0;
            case 8 -> left.pattern.outputAmount(key).compareTo(right.pattern.outputAmount(key)) != 0;
            default -> !left.pattern.pattern().id().equals(right.pattern.pattern().id());
        };
    }

    public PlanningResult<K> plan(ImmutableCraftingGraph<K> graph, PlanningRequest<K> request) {
        return plan(graph, request, true);
    }

    /**
     * Plans without enumerating the Cartesian product of producer choices.
     *
     * <p>
     * The same stock-aware route ordering, batching, byproduct accounting and cycle guards are
     * used as by the exact planner, but a shortage is returned from the best deterministic route
     * selection instead of trying every combination merely to prove that shortage. This keeps large
     * modpack graphs linear in the selected plan and, importantly, remains a RaishxCore calculation:
     * no external AE2 planning provider is involved.
     */
    public PlanningResult<K> planFast(ImmutableCraftingGraph<K> graph, PlanningRequest<K> request) {
        return plan(graph, request, false);
    }

    private PlanningResult<K> plan(ImmutableCraftingGraph<K> graph, PlanningRequest<K> request,
                                   boolean exhaustiveChoices) {
        Objects.requireNonNull(graph, "graph");
        Objects.requireNonNull(request, "request");
        long started = nanoTime.getAsLong();
        Budget budget = new Budget(request, started);
        State state = new State(graph, request);
        PlanningResult.Status halt = null;
        try {
            budget.check();
            if (!graph.simpleDemandOrder().isEmpty()) {
                planDag(graph, request, state, budget);
            } else {
                Map<K, Integer> ranks = reachability(graph, request, budget);
                // The leaf demand pass only informs a choice between routes. A graph where every key
                // has a single route has nothing to choose, so it must not pay for the computation.
                Map<K, BigInteger> leafCosts = hasRouteChoice(graph) ? leafCosts(graph, budget) : Map.of();
                if (!exhaustiveChoices) {
                    // Dense graphs normally spend nearly all their time proving that every other
                    // combination has the same shortage. Select the best routes once and preserve
                    // that useful plan instead of discarding it for an exponential proof.
                    search(graph, request, state, ranks, leafCosts, budget, true);
                } else if (!search(graph, request, state, ranks, leafCosts, budget, false)) {
                    // Shortage reporting starts from a fresh snapshot, never speculative leftovers.
                    state = new State(graph, request);
                    // The discarded search's choices are not the returned plan's, so report the ones
                    // the simulation actually made rather than counting every route twice.
                    budget.choiceLinks = null;
                    search(graph, request, state, ranks, leafCosts, budget, true);
                }
            }
            budget.check();
        } catch (PlanningHalt stop) {
            halt = stop.status;
            // A partial search is not an executable plan, even if its missing map was still empty.
            state = new State(graph, request);
            state.missing.put(request.target(), request.amount());
        }
        CraftingPlan<K> plan = state.freeze(request.target(), request.amount());
        var status = halt != null ? halt :
                plan.complete() ? PlanningResult.Status.COMPLETE : PlanningResult.Status.MISSING_INGREDIENTS;
        return new PlanningResult<>(status, plan, new PlanningResult.Diagnostics(graph.revision(),
                budget.operations, budget.maximumDepth, Math.max(0, nanoTime.getAsLong() - started),
                PlanningResult.ShortageSummary.of(plan.shortage()), budget.cycleCuts,
                budget.choiceLinks));
    }

    private void planDag(ImmutableCraftingGraph<K> graph, PlanningRequest<K> request, State state, Budget budget) {
        Map<K, UfoAmount> demand = new HashMap<>();
        Map<K, Integer> depths = new HashMap<>();
        demand.put(request.target(), request.amount());
        depths.put(request.target(), 1);
        if (graph.compiledPatternsFor(request.target()).isEmpty()) {
            UfoAmount shortfall = state.consume(request.target(), request.amount());
            state.add(state.missing, request.target(), shortfall);
            return;
        }
        for (K key : graph.simpleDemandOrder()) {
            budget.operation(0);
            UfoAmount needed = demand.get(key);
            if (needed == null) continue;
            int depth = depths.get(key);
            budget.operation(depth);
            UfoAmount required = state.consume(key, needed);
            if (required.isZero()) continue;
            List<CompiledPattern<K>> options = graph.compiledPatternsFor(key);
            if (options.isEmpty()) {
                state.add(state.missing, key, required);
                continue;
            }
            CompiledPattern<K> pattern = options.get(0);
            UfoAmount runs = ceil(required, pattern.outputAmount(key));
            state.add(state.executions, pattern.pattern(), runs);
            state.schedule.add(new CraftingPlan.Execution<>(pattern.pattern(), runs));
            state.add(state.crafted, key, multiply(pattern.outputAmount(key), runs).subtract(required));
            for (PatternEntry<K> input : pattern.inputs()) {
                budget.operation(depth);
                demand.merge(input.key(), multiply(input.amount(), runs), UfoAmount::add);
                depths.merge(input.key(), depth + 1, Math::max);
            }
        }
        Collections.reverse(state.schedule);
    }

    /** Linear hypergraph reachability. Each input edge is visited once, even in a reverse-sorted deep chain. */
    private Map<K, Integer> reachability(ImmutableCraftingGraph<K> graph, PlanningRequest<K> request,
                                         Budget budget) {
        Map<K, Integer> ranks = new HashMap<>();
        ArrayDeque<K> queue = new ArrayDeque<>();
        request.inventory().forEach((key, amount) -> {
            ranks.put(key, 0);
            queue.add(key);
        });
        Map<CompiledPattern<K>, Integer> pending = new IdentityHashMap<>();
        Map<CompiledPattern<K>, Integer> maxima = new IdentityHashMap<>();
        for (CompiledPattern<K> pattern : graph.compiledPatterns()) {
            budget.operation(0);
            pending.put(pattern, pattern.inputs().size());
            if (pattern.inputs().isEmpty()) {
                for (PatternEntry<K> output : pattern.outputs()) {
                    if (ranks.putIfAbsent(output.key(), 0) == null) queue.add(output.key());
                }
            }
        }
        while (!queue.isEmpty()) {
            K key = queue.removeFirst();
            for (CompiledPattern<K> pattern : graph.consumersOf(key)) {
                budget.operation(0);
                maxima.merge(pattern, ranks.get(key), Math::max);
                if (pending.merge(pattern, -1, Integer::sum) == 0) {
                    int rank = maxima.get(pattern) + 1;
                    for (PatternEntry<K> output : pattern.outputs()) {
                        if (ranks.putIfAbsent(output.key(), rank) == null) queue.addLast(output.key());
                    }
                }
            }
        }
        return ranks;
    }

    /** True when some key is produced by more than one pattern, so route choice actually exists. */
    private static <K> boolean hasRouteChoice(ImmutableCraftingGraph<K> graph) {
        for (CompiledPattern<K> pattern : graph.compiledPatterns()) {
            for (PatternEntry<K> output : pattern.outputs()) {
                if (graph.compiledPatternsFor(output.key()).size() > 1) return true;
            }
        }
        return false;
    }

    /**
     * Minimum total leaf demand needed to obtain one unit of every key, computed bottom-up over the
     * acyclic part of the graph.
     *
     * <p>
     * Choosing between two viable routes otherwise looks one level deep, so a narrow and a wide
     * route tie on input count and an identifier breaks the tie. A plan can then be correct and
     * still ask the player for several times the material it needs: on the multi-route Fibonacci
     * corpus case the reported shortage was 6.857 times the known minimum. Leaf demand is the
     * quantity shortage quality is measured with, so it has to drive the choice.
     *
     * <p>
     * A key nothing produces is one unit of shortage. A pattern's cost is the sum of its inputs'
     * costs divided by how much it yields, rounded up, because a fractional unit cannot be supplied.
     * Keys and patterns on a cycle never resolve and stay absent, so they keep the previous ordering.
     */
    private Map<K, BigInteger> leafCosts(ImmutableCraftingGraph<K> graph, Budget budget) {
        Map<K, BigInteger> costs = new HashMap<>();
        Map<CompiledPattern<K>, Integer> unresolvedInputs = new IdentityHashMap<>();
        Map<CompiledPattern<K>, BigInteger> inputTotals = new IdentityHashMap<>();
        Map<K, Integer> unresolvedProducers = new HashMap<>();
        Set<K> keys = new HashSet<>();

        for (CompiledPattern<K> pattern : graph.compiledPatterns()) {
            budget.operation(0);
            unresolvedInputs.put(pattern, pattern.inputs().size());
            inputTotals.put(pattern, BigInteger.ZERO);
            for (PatternEntry<K> input : pattern.inputs()) {
                keys.add(input.key());
            }
            for (PatternEntry<K> output : pattern.outputs()) {
                keys.add(output.key());
                unresolvedProducers.merge(output.key(), 1, Integer::sum);
            }
        }

        ArrayDeque<K> finalized = new ArrayDeque<>();
        // A leaf costs one unit of itself, or what the request says that unit is worth. The rest of
        // the pass multiplies amounts through, so a root ends up holding weighted leaf value, and the
        // route comparison is then made in the same currency as the shortage it is trying to avoid.
        // Leaves were counted in bare units before, which meant a route needing ten cheap units beat
        // one needing a single valuable unit and the declared weights were never consulted at all.
        // One is the default and stays the same object, so an unweighted request is untouched.
        Map<K, Long> weights = budget.request.missingWeights();
        for (K key : keys) {
            if (unresolvedProducers.getOrDefault(key, 0) == 0) {
                costs.put(key, weights.isEmpty() ? BigInteger.ONE : BigInteger.valueOf(weights.getOrDefault(key, 1L)));
                finalized.add(key);
            }
        }

        while (!finalized.isEmpty()) {
            K key = finalized.removeFirst();
            BigInteger cost = costs.get(key);
            for (CompiledPattern<K> pattern : graph.consumersOf(key)) {
                budget.operation(0);
                BigInteger units = BigInteger.ZERO;
                int matches = 0;
                for (PatternEntry<K> input : pattern.inputs()) {
                    if (input.key().equals(key)) {
                        units = units.add(input.amount().asBigInteger());
                        matches++;
                    }
                }
                if (matches == 0) continue;
                inputTotals.merge(pattern, cost.multiply(units), BigInteger::add);
                if (unresolvedInputs.merge(pattern, -matches, Integer::sum) != 0) continue;
                resolveLeafCost(pattern, costs, unresolvedProducers, inputTotals, finalized);
            }
        }
        return costs;
    }

    private void resolveLeafCost(CompiledPattern<K> pattern, Map<K, BigInteger> costs,
                                 Map<K, Integer> unresolvedProducers,
                                 Map<CompiledPattern<K>, BigInteger> inputTotals,
                                 ArrayDeque<K> finalized) {
        BigInteger total = inputTotals.get(pattern);
        for (PatternEntry<K> output : pattern.outputs()) {
            K key = output.key();
            BigInteger perUnit = ceilDiv(total, output.amount().asBigInteger());
            BigInteger existing = costs.get(key);
            if (existing == null || perUnit.compareTo(existing) < 0) {
                costs.put(key, perUnit);
            }
            if (unresolvedProducers.merge(key, -1, Integer::sum) == 0) {
                costs.putIfAbsent(key, BigInteger.ONE);
                finalized.add(key);
            }
        }
    }

    private static BigInteger ceilDiv(BigInteger numerator, BigInteger denominator) {
        BigInteger[] quotient = numerator.divideAndRemainder(denominator);
        return quotient[1].signum() == 0 ? quotient[0] : quotient[0].add(BigInteger.ONE);
    }

    private boolean search(ImmutableCraftingGraph<K> graph, PlanningRequest<K> request, State state,
                           Map<K, Integer> ranks, Map<K, BigInteger> leafCosts, Budget budget,
                           boolean simulate) {
        Task<K> pending = new Task<>(request.target(), request.amount(), 1, null, null, null);
        ArrayDeque<Choice<K>> choices = new ArrayDeque<>();
        while (pending != null) {
            Task<K> task = pending;
            pending = task.next;
            budget.operation(task.depth);
            if (task.pattern != null) {
                state.add(state.executions, task.pattern.pattern(), task.runs);
                state.schedule.add(new CraftingPlan.Execution<>(task.pattern.pattern(), task.runs));
                for (PatternEntry<K> output : task.pattern.outputs()) {
                    budget.operation(task.depth);
                    state.add(state.crafted, output.key(), multiply(output.amount(), task.runs));
                }
                UfoAmount remainder = state.consume(task.key, task.amount);
                if (!remainder.isZero()) throw new IllegalStateException("selected pattern did not cover demand");
                state.activate(task.key, false);
                continue;
            }
            UfoAmount required = state.consume(task.key, task.amount);
            if (required.isZero()) continue;
            List<Candidate<K>> options = state.active.contains(task.key) ? List.of() :
                    candidates(graph, task.key, required, state, ranks, leafCosts, budget);
            if (options.isEmpty()) {
                if (simulate) {
                    state.add(state.missing, task.key, required);
                    continue;
                }
                boolean recovered = false;
                while (!choices.isEmpty()) {
                    budget.operation(0);
                    Choice<K> choice = choices.peek();
                    state.rollback(choice.mark, choice.scheduleSize);
                    if (choice.next < choice.options.size()) {
                        pending = expand(choice.key, choice.required, choice.depth,
                                choice.options.get(choice.next++), choice.continuation, state, budget);
                        recovered = true;
                        break;
                    }
                    choices.pop();
                }
                if (!recovered) return false;
                continue;
            }
            if (!simulate && options.size() > 1) {
                state.journaling = true;
                choices.push(new Choice<>(task.key, required, task.depth, options, pending,
                        state.journal.size(), state.schedule.size()));
            }
            pending = expand(task.key, required, task.depth, options.get(0), pending, state, budget);
        }
        return true;
    }

    private Task<K> expand(K key, UfoAmount required, int depth, Candidate<K> option, Task<K> next,
                           State state, Budget budget) {
        UfoAmount covered = multiply(option.pattern.outputAmount(key), option.runs).min(required);
        if (covered.compareTo(required) < 0) {
            next = new Task<>(key, required.subtract(covered), depth, null, null, next);
        }
        state.activate(key, true);
        // A self-feeding input is produced by the pattern it feeds, so it has to be claimed after that
        // pattern has run. Every other input is wrapped into the head of the chain, where it would be
        // drawn before the pattern could create it and the cycle guard would refuse the whole plan.
        List<PatternEntry<K>> outside = new ArrayList<>();
        List<UfoAmount> outsideDraws = new ArrayList<>();
        List<UfoAmount> feedingDraws = new ArrayList<>();
        UfoAmount selfConsumedPerRun = UfoAmount.ZERO;
        // A decaying catalyst is declared twice, as a catalyst and as a consumed input. The presence
        // check below has to demand both at once, because it runs before the draws and would otherwise
        // see the whole stock and wave through a batch that eats into the catalyst it just approved.
        Map<K, UfoAmount> consumedPerKey = null;
        boolean hasCatalyst = false;
        for (PatternEntry<K> input : option.pattern.inputs()) {
            if (input.reusable()) {
                hasCatalyst = true;
                break;
            }
        }
        if (hasCatalyst) {
            // Only a pattern that carries a catalyst needs this at all. Building it unconditionally
            // charged every pattern in the graph for a map that stays empty, which showed up as bytes
            // per operation on a case that has no catalyst in it whatsoever.
            consumedPerKey = new HashMap<>();
            for (PatternEntry<K> input : option.pattern.inputs()) {
                if (input.reusable() || input.key().equals(key)) continue;
                consumedPerKey.merge(input.key(),
                        input.durable() ? multiply(input.amount(), ceil(option.runs, UfoAmount.of(input.uses()))) :
                                multiply(input.amount(), option.runs),
                        UfoAmount::add);
            }
        }
        for (PatternEntry<K> input : option.pattern.inputs()) {
            budget.operation(depth);
            if (input.reusable()) {
                // A catalyst must be on hand but is handed back, so it is never consumed and no
                // demand is propagated for it. It is checked once, when the pattern is expanded, so a
                // catalyst that this same plan would craft only later still reads as missing.
                UfoAmount alsoConsumed = consumedPerKey == null ? UfoAmount.ZERO :
                        consumedPerKey.getOrDefault(input.key(), UfoAmount.ZERO);
                // Only the working stock is charged once for the whole plan; the decay is charged every
                // time, because it really is consumed every time.
                // Allocated on first use: a graph with no catalyst at all must not pay for a map that
                // would stay empty, and most graphs are that.
                UfoAmount working = input.amount().subtractClamped(state.presence == null ? UfoAmount.ZERO :
                        state.presence.getOrDefault(input.key(), UfoAmount.ZERO));
                if (!working.isZero()) {
                    if (state.presence == null) {
                        state.presence = new HashMap<>();
                    }
                    state.put(state.presence, input.key(), input.amount());
                }
                // The decay is drawn by its own consumed entry a few lines below, so charging the whole
                // of working plus decay here double-counts it whenever the stock cannot cover the
                // draw: with nothing in stock, a working stock of two and a decay of two demanded four
                // here and two more there. What is charged here is the part the normal draw cannot
                // explain, which is the shortfall of the total beyond the shortfall of the decay.
                UfoAmount present = input.variants().isEmpty() ? state.available(input.key()) :
                        state.availableAcross(input.variants());
                UfoAmount totalShort = working.add(alsoConsumed).subtractClamped(present);
                UfoAmount decayShort = alsoConsumed.subtractClamped(present);
                state.add(state.missing, input.key(), totalShort.subtractClamped(decayShort));
                continue;
            }
            UfoAmount drawn = input.durable() ? durableDraw(state, option.pattern, input, option.runs, true) :
                    multiply(input.amount(), option.runs);
            if (input.key().equals(key)) {
                selfConsumedPerRun = selfConsumedPerRun.add(input.amount());
                feedingDraws.add(drawn);
            } else {
                outside.add(input);
                outsideDraws.add(drawn);
            }
        }
        if (!feedingDraws.isEmpty()) {
            // The loop needs a seed to start, and the caller already took whatever stock there was.
            // One seed is enough to reach any amount, so a shortage is the seed rather than the
            // request. It is also modelled as available: the balance contract assumes a reported
            // shortage is supplied, and unlike a consumed input a seed is not used up, so the residue
            // it leaves has to appear in the plan.
            UfoAmount seed = state.extracted.getOrDefault(key, UfoAmount.ZERO);
            UfoAmount shortfall = selfConsumedPerRun.subtractClamped(seed);
            if (!shortfall.isZero()) {
                state.add(state.missing, key, shortfall);
                state.add(state.crafted, key, shortfall);
            }
        }
        Task<K> chained = next;
        for (int i = feedingDraws.size() - 1; i >= 0; i--) {
            chained = new Task<>(key, feedingDraws.get(i), depth + 1, null, null, chained);
        }
        Task<K> head = new Task<>(key, covered, depth, option.pattern, option.runs, chained);
        for (int i = outside.size() - 1; i >= 0; i--) {
            head = new Task<>(outside.get(i).key(), outsideDraws.get(i), depth + 1, null, null, head);
        }
        return head;
    }

    /** One durable carrier group: a pattern and the input key whose carriers it consumes. */
    private record DurableGroup<K>(CompiledPattern<K> pattern, K key) {}

    /**
     * How many carriers a batch of {@code runs} more firings needs beyond what earlier batches of the
     * same group already claimed. The increments telescope to the ceiling of the total firings, which
     * is what the schedule really consumes; a ceiling per batch does not, and two ceilings of one and
     * three firings across a carrier that lasts two add up to three carriers where two suffice.
     */
    private UfoAmount durableDraw(State state, CompiledPattern<K> pattern, PatternEntry<K> input,
                                  UfoAmount runs, boolean commit) {
        DurableGroup<K> group = new DurableGroup<>(pattern, input.key());
        UfoAmount before = state.firings == null ? UfoAmount.ZERO : state.firings.getOrDefault(group, UfoAmount.ZERO);
        UfoAmount after = before.add(runs);
        if (commit) {
            if (state.firings == null) {
                state.firings = new HashMap<>();
            }
            state.put(state.firings, group, after);
        }
        UfoAmount uses = UfoAmount.of(input.uses());
        return multiply(ceil(after, uses).subtract(ceil(before, uses)), input.amount());
    }

    /** Firings of a durable group already claimed, so a capacity can be stated in what is left. */
    private UfoAmount firingsClaimed(State state, CompiledPattern<K> pattern, PatternEntry<K> input) {
        return state.firings == null ? UfoAmount.ZERO :
                state.firings.getOrDefault(new DurableGroup<>(pattern, input.key()), UfoAmount.ZERO);
    }

    private List<Candidate<K>> candidates(ImmutableCraftingGraph<K> graph, K key, UfoAmount required,
                                          State state, Map<K, Integer> ranks,
                                          Map<K, BigInteger> leafCosts, Budget budget) {
        ArrayList<Candidate<K>> options = new ArrayList<>();
        List<CompiledPattern<K>> routes = graph.compiledPatternsFor(key);
        // A secondary output is a way to obtain the key, not a lesser kind of way, so it competes. It
        // used to be consulted only when nothing declared the key at all, which meant a recipe yielding
        // nine of it per firing lost to any recipe that named it, however expensive. The comparison
        // below already prices the extra firings and the primary that comes out alongside, so the
        // recipe that is actually cheapest wins. The list is built only when there is something to add.
        List<CompiledPattern<K>> secondary = graph.secondaryRoutesFor(key);
        if (!secondary.isEmpty()) {
            ArrayList<CompiledPattern<K>> both = new ArrayList<>(routes.size() + secondary.size());
            both.addAll(routes);
            both.addAll(secondary);
            routes = both;
        }
        for (CompiledPattern<K> pattern : routes) {
            budget.operation(0);
            // A pattern that feeds itself only closes when it produces more than it consumes: that is
            // a growth step, and the material it needs is the seed it also makes. Refusing it, as the
            // cycle guard did, discarded the family outright.
            UfoAmount selfConsumed = UfoAmount.ZERO;
            UfoAmount selfProduced = UfoAmount.ZERO;
            for (PatternEntry<K> entry : pattern.inputs()) {
                if (entry.key().equals(key) && !entry.reusable()) {
                    selfConsumed = selfConsumed.add(entry.amount());
                }
            }
            for (PatternEntry<K> entry : pattern.outputs()) {
                if (entry.key().equals(key)) selfProduced = selfProduced.add(entry.amount());
            }
            BigInteger selfNet = selfProduced.subtract(selfConsumed).asBigInteger();
            boolean growth = !selfConsumed.isZero() && selfNet.signum() > 0;
            // The caller already took the seed from stock, so it is not subtracted again: each run
            // adds the net, and the runs needed are the ceiling of what is still missing over it.
            UfoAmount runs = growth ?
                    UfoAmount.of(required.asBigInteger().add(selfNet).subtract(BigInteger.ONE).divide(selfNet)) :
                    ceil(required, pattern.outputAmount(key));
            UfoAmount capacity = runs;
            BigInteger deficit = BigInteger.ZERO;
            // Only built when a weight was actually declared, so the ordinary request never pays for it.
            Map<K, Long> weights = budget.request.missingWeights();
            BigInteger weighted = weights.isEmpty() ? null : BigInteger.ZERO;
            BigInteger inputCost = BigInteger.ZERO;
            BigInteger leafCost = BigInteger.ZERO;
            boolean leafKnown = true;
            int rank = 0;
            boolean cycle = false;
            for (PatternEntry<K> input : pattern.inputs()) {
                budget.operation(0);
                // A fuzzy slot is satisfied by any variant it accepts, so presence is their total.
                UfoAmount available = input.reusable() && !input.variants().isEmpty() ?
                        state.availableAcross(input.variants()) : state.available(input.key());
                // A catalyst is required once and handed back; a durable carrier is consumed but one
                // unit survives several firings, so neither scales with the run count the way an
                // ordinary input does.
                UfoAmount needed;
                if (input.reusable()) {
                    needed = input.amount();
                } else if (input.durable()) {
                    needed = durableDraw(state, pattern, input, runs, false);
                } else {
                    needed = multiply(input.amount(), runs);
                }
                if (state.active.contains(input.key()) && available.compareTo(needed) < 0) {
                    cycle = true;
                    budget.cycleCuts++;
                }
                // A self-feeding pattern is admitted only when it gains material; a self-loop that
                // consumes at least as much as it makes cannot close.
                if (input.key().equals(key) && !growth) {
                    cycle = true;
                    budget.cycleCuts++;
                }
                if (!input.reusable()) {
                    UfoAmount carriers = UfoAmount.of(available.asBigInteger().divide(input.amount().asBigInteger()));
                    capacity = capacity.min(input.durable() ? multiply(carriers, UfoAmount.of(input.uses()))
                            .subtractClamped(firingsClaimed(state, pattern, input)) : carriers);
                }
                BigInteger gap = needed.subtractClamped(available).asBigInteger();
                deficit = deficit.add(gap);
                if (weighted != null && gap.signum() != 0) {
                    weighted = weighted.add(gap.multiply(BigInteger.valueOf(weights.getOrDefault(
                            input.key(), 1L))));
                }
                inputCost = inputCost.add(needed.asBigInteger());
                rank = Math.max(rank, ranks.getOrDefault(input.key(), Integer.MAX_VALUE));
                BigInteger inputLeaf = leafCosts.get(input.key());
                if (inputLeaf == null) {
                    leafKnown = false;
                } else {
                    // Per execution, so routes with different yields stay comparable.
                    leafCost = leafCost.add(inputLeaf.multiply(input.amount().asBigInteger()));
                }
            }
            if (cycle) continue;
            BigInteger routeLeafCost = leafKnown ? leafCost : null;
            // The same slot carries whichever shortage measure the request asked for: units when
            // nothing was weighted, weighted units when something was. It is one field either way, so
            // a request without weights allocates exactly what it allocated before.
            BigInteger ranking = weighted == null ? deficit : weighted;
            options.add(new Candidate<>(pattern, runs, ranking, inputCost, rank, routeLeafCost));
            if (!capacity.isZero() && capacity.compareTo(runs) < 0) {
                options.add(new Candidate<>(pattern, capacity, BigInteger.ZERO, inputCost, rank,
                        routeLeafCost));
            }
        }
        for (int i = 0; i < options.size(); i++) budget.operation(0);
        Comparator<Candidate<K>> order = Comparator
                .<Candidate<K>>comparingInt(option -> option.pattern.pattern().priority())
                .reversed()
                .thenComparing(option -> option.deficit.signum() != 0)
                // Real leaf demand decides between routes that are otherwise equally viable. Without
                // it an identifier chose, and a correct plan could still ask for several times the
                // material the case needs.
                .thenComparing(option -> option.leafCost == null)
                .thenComparing(option -> option.leafCost, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparingInt(option -> option.rank)
                .thenComparing(option -> option.deficit)
                .thenComparing(option -> option.cost)
                // Two routes can cost the same and fire the same number of times while one leaves
                // material behind. The roadmap puts overproduction after the execution count and
                // before the identifier, so without this the identifier decided it and a plan could
                // carry avoidable surplus because its recipe happened to sort first. Overshoot is
                // runs times yield less the demand, and the demand is the same for every candidate
                // here, so ordering by runs and then by yield orders by overshoot exactly, without
                // computing it: the two keys are already on hand and both are needed anyway.
                .thenComparing(option -> option.runs)
                .thenComparing(option -> option.pattern.outputAmount(key))
                .thenComparing(option -> option.pattern.pattern().id());
        options.sort((left, right) -> {
            budget.operation(0);
            return order.compare(left, right);
        });
        // Allocated only when a choice exists, so a graph with a single route per key never pays for
        // the histogram. The winner is options.get(0); whatever separates it from options.get(1) is why.
        if (options.size() > 1) {
            if (budget.choiceLinks == null) {
                budget.choiceLinks = new long[CHOICE_LINK_COUNT];
            }
            for (int index = 0; index < CHOICE_LINK_COUNT; index++) {
                if (decides(index, key, options.get(0), options.get(1))) {
                    budget.choiceLinks[index]++;
                    break;
                }
            }
        }
        return options;
    }

    private static UfoAmount ceil(UfoAmount numerator, UfoAmount denominator) {
        BigInteger[] quotient = numerator.asBigInteger().divideAndRemainder(denominator.asBigInteger());
        return UfoAmount.of(quotient[1].signum() == 0 ? quotient[0] : quotient[0].add(BigInteger.ONE));
    }

    private static UfoAmount multiply(UfoAmount left, UfoAmount right) {
        return UfoAmount.of(left.asBigInteger().multiply(right.asBigInteger()));
    }

    private record Candidate<K>(CompiledPattern<K> pattern, UfoAmount runs, BigInteger deficit,
                                BigInteger cost, int rank, BigInteger leafCost) {}

    private record Task<K>(K key, UfoAmount amount, int depth, CompiledPattern<K> pattern,
                           UfoAmount runs, Task<K> next) {}

    private static final class Choice<K> {

        final K key;
        final UfoAmount required;
        final int depth;
        final List<Candidate<K>> options;
        final Task<K> continuation;
        final int mark;
        final int scheduleSize;
        int next = 1;

        Choice(K key, UfoAmount required, int depth, List<Candidate<K>> options, Task<K> continuation,
               int mark, int scheduleSize) {
            this.key = key;
            this.required = required;
            this.depth = depth;
            this.options = options;
            this.continuation = continuation;
            this.mark = mark;
            this.scheduleSize = scheduleSize;
        }
    }

    private final class Budget {

        final PlanningRequest<K> request;
        final long started;
        final long timeout;
        long operations;
        int maximumDepth;
        /** Routes refused because they would have to reach into a key the plan is already expanding. */
        long cycleCuts;
        /**
         * One count per comparison link, or {@code null} when the plan never had a route to choose.
         * Nothing allocates it on the common single-route path.
         */
        long[] choiceLinks;

        Budget(PlanningRequest<K> request, long started) {
            this.request = request;
            this.started = started;
            long nanos;
            try {
                nanos = request.limits().timeout().toNanos();
            } catch (ArithmeticException overflow) {
                nanos = Long.MAX_VALUE;
            }
            timeout = nanos;
        }

        void operation(int depth) {
            maximumDepth = Math.max(maximumDepth, depth);
            if (depth > request.limits().maxDepth()) throw new PlanningHalt(PlanningResult.Status.DEPTH_LIMIT);
            if (operations >= request.limits().maxOperations()) {
                throw new PlanningHalt(PlanningResult.Status.OPERATION_LIMIT);
            }
            if (++operations % request.limits().checkpointInterval() == 0) check();
        }

        void check() {
            if (request.cancellation().isCancelled() || Thread.currentThread().isInterrupted()) {
                throw new PlanningHalt(PlanningResult.Status.CANCELLED);
            }
            if (nanoTime.getAsLong() - started >= timeout) throw new PlanningHalt(PlanningResult.Status.TIMED_OUT);
        }
    }

    private static final class PlanningHalt extends RuntimeException {

        @Serial
        private static final long serialVersionUID = 1L;
        final PlanningResult.Status status;

        PlanningHalt(PlanningResult.Status status) {
            super(null, null, false, false);
            this.status = status;
        }
    }

    private final class State {

        final Comparator<? super K> keys;
        final Map<K, UfoAmount> stock;
        final Map<K, UfoAmount> crafted;
        final Map<K, UfoAmount> extracted;
        final Map<K, UfoAmount> missing;
        /**
         * Working stock already demanded of each catalyst. A catalyst is handed back, so one unit
         * covers every firing of its recipe; a plan that expands the same recipe in two steps, as it
         * does when a secondary output is chased separately from the primary, must not be charged for
         * the same catalyst twice.
         */
        Map<K, UfoAmount> presence;
        final ImmutableCraftingGraph<K> graph;
        /**
         * Firings already served by each durable carrier group. A carrier survives several firings, so
         * a plan that expands the same recipe twice - once for its primary and again to chase a
         * secondary - has to draw the carrier budget across the whole plan rather than at each ceiling
         * separately, or it charges for a tool that the schedule never consumes.
         */
        Map<DurableGroup<K>, UfoAmount> firings;
        final Map<CraftingPattern<K>, UfoAmount> executions = new TreeMap<>(Comparator.comparing(CraftingPattern::id));
        final Set<K> active = new HashSet<>();
        final ArrayList<CraftingPlan.Execution<K>> schedule = new ArrayList<>();
        final ArrayList<Runnable> journal = new ArrayList<>();
        boolean journaling;

        State(ImmutableCraftingGraph<K> graph, PlanningRequest<K> request) {
            this.graph = graph;
            keys = graph.keyComparator();
            stock = new TreeMap<>(keys);
            stock.putAll(request.inventory());
            crafted = new TreeMap<>(keys);
            extracted = new TreeMap<>(keys);
            missing = new TreeMap<>(keys);
        }

        UfoAmount available(K key) {
            return stock.getOrDefault(key, UfoAmount.ZERO).add(crafted.getOrDefault(key, UfoAmount.ZERO));
        }

        /** How much of {@code key} is missing for it to be present at all. Consumes nothing. */
        UfoAmount requirePresent(K key, UfoAmount amount) {
            return requirePresent(key, amount, Set.of());
        }

        /** Availability summed across every key a fuzzy slot accepts. */
        UfoAmount availableAcross(Set<K> variants) {
            UfoAmount total = UfoAmount.ZERO;
            for (K variant : variants) {
                total = total.add(available(variant));
            }
            return total;
        }

        /**
         * The same, for a slot any of {@code variants} may satisfy: a logical tool accepts a damaged
         * one, so presence is the total across everything the slot accepts.
         */
        UfoAmount requirePresent(K key, UfoAmount amount, Set<K> variants) {
            UfoAmount present = variants.isEmpty() ? available(key) : availableAcross(variants);
            return amount.subtractClamped(present);
        }

        UfoAmount consume(K key, UfoAmount amount) {
            UfoAmount surplus = crafted.getOrDefault(key, UfoAmount.ZERO);
            UfoAmount taken = surplus.min(amount);
            if (!taken.isZero()) put(crafted, key, surplus.subtract(taken));
            UfoAmount rest = amount.subtract(taken);
            UfoAmount stored = stock.getOrDefault(key, UfoAmount.ZERO);
            taken = stored.min(rest);
            if (!taken.isZero()) {
                put(stock, key, stored.subtract(taken));
                add(extracted, key, taken);
            }
            return rest.subtract(taken);
        }

        <T> void add(Map<T, UfoAmount> map, T key, UfoAmount amount) {
            if (!amount.isZero()) put(map, key, map.getOrDefault(key, UfoAmount.ZERO).add(amount));
        }

        <T> void put(Map<T, UfoAmount> map, T key, UfoAmount amount) {
            UfoAmount previous = map.get(key);
            if (journaling) journal.add(() -> {
                if (previous == null) map.remove(key);
                else map.put(key, previous);
            });
            if (amount.isZero()) map.remove(key);
            else map.put(key, amount);
        }

        void activate(K key, boolean value) {
            boolean previous = active.contains(key);
            if (journaling) journal.add(() -> {
                if (previous) active.add(key);
                else active.remove(key);
            });
            if (value) active.add(key);
            else active.remove(key);
        }

        void rollback(int mark, int scheduleSize) {
            for (int i = journal.size() - 1; i >= mark; i--) journal.remove(journal.size() - 1).run();
            schedule.subList(scheduleSize, schedule.size()).clear();
        }

        CraftingPlan<K> freeze(K target, UfoAmount requested) {
            TreeMap<K, UfoAmount> remaining = new TreeMap<>(keys);
            remaining.putAll(stock);
            crafted.forEach((key, amount) -> remaining.merge(key, amount, UfoAmount::add));
            UfoAmount runs = executions.values().stream().reduce(UfoAmount.ZERO, UfoAmount::add);
            UfoAmount shortfall = missing.values().stream().reduce(UfoAmount.ZERO, UfoAmount::add);
            UfoAmount surplus = crafted.values().stream().reduce(UfoAmount.ZERO, UfoAmount::add);
            return new CraftingPlan<>(target, requested, executions, extracted, missing, remaining, schedule,
                    new CraftingPlan.PlanQuality(missing.isEmpty(), executions.size(), runs, shortfall, surplus),
                    splitShortage(missing));
        }

        /**
         * Charges a missing key to what was consumed first, then to what wore out, and calls whatever
         * cannot be explained that way a seed. A key can be short for more than one reason at once, and
         * the consumed part is the one worth naming.
         */
        /** Shared by every plan that is short of nothing, so an empty split costs nothing. */
        private static final CraftingPlan.Shortage<Object> NO_SHORTAGE = new CraftingPlan.Shortage<>(Map.of(), Map.of(),
                Map.of());

        /** True when the pattern produces the key, which makes an input of it a seed rather than food. */
        private boolean produces(CompiledPattern<K> pattern, K key) {
            for (PatternEntry<K> output : pattern.outputs()) {
                if (output.key().equals(key)) {
                    return true;
                }
            }
            return false;
        }

        @SuppressWarnings("unchecked")
        private CraftingPlan.Shortage<K> splitShortage(Map<K, UfoAmount> missing) {
            if (missing.isEmpty()) {
                // Three maps for a plan with no shortage is three maps too many, and most plans have
                // none: the allocation gate charged seven and a half percent for it on a case whose
                // plan is complete.
                return (CraftingPlan.Shortage<K>) NO_SHORTAGE;
            }
            // Derived from the executions the plan settled on rather than accumulated while it was
            // built. Accumulating per draw charged every plan for a diagnostic only a short one needs,
            // which is the same reason the split is computed here rather than in the search.
            // One pass over the plan, for the keys that are actually missing: asking per key walked the
            // whole plan once per key. The graph is walked rather than indexed, so nothing is built to
            // look up what is already reachable.
            Map<K, UfoAmount[]> askedByKey = new HashMap<>();
            for (CompiledPattern<K> pattern : graph.compiledPatterns()) {
                UfoAmount runs = executions.get(pattern.pattern());
                if (runs == null) {
                    continue;
                }
                for (PatternEntry<K> input : pattern.inputs()) {
                    if (!missing.containsKey(input.key())) {
                        continue;
                    }
                    UfoAmount[] asked = askedByKey.computeIfAbsent(input.key(),
                            ignored -> new UfoAmount[] { UfoAmount.ZERO, UfoAmount.ZERO, UfoAmount.ZERO });
                    if (input.reusable()) {
                        asked[2] = asked[2].add(input.amount());
                    } else if (input.durable()) {
                        asked[1] = asked[1].add(multiply(input.amount(),
                                ceil(runs, UfoAmount.of(input.uses()))));
                    } else if (produces(pattern, input.key())) {
                        // A self-feeding input is the material it makes, so it is handed back.
                        asked[2] = asked[2].add(multiply(input.amount(), runs));
                    } else {
                        asked[0] = asked[0].add(multiply(input.amount(), runs));
                    }
                }
            }
            Map<K, UfoAmount> consumable = new LinkedHashMap<>();
            Map<K, UfoAmount> carrier = new LinkedHashMap<>();
            Map<K, UfoAmount> seed = new LinkedHashMap<>();
            for (K key : missing.keySet()) {
                UfoAmount[] asked = askedByKey.getOrDefault(key,
                        new UfoAmount[] { UfoAmount.ZERO, UfoAmount.ZERO, UfoAmount.ZERO });
                UfoAmount left = missing.get(key);
                UfoAmount byConsumable = left.min(asked[0]);
                if (!byConsumable.isZero()) {
                    consumable.put(key, byConsumable);
                    left = left.subtract(byConsumable);
                }
                UfoAmount byCarrier = left.min(asked[1]);
                if (!byCarrier.isZero()) {
                    carrier.put(key, byCarrier);
                    left = left.subtract(byCarrier);
                }
                if (!left.isZero()) {
                    // Whatever the plan's own demand cannot explain was asked for as a seed, or the key
                    // is the target of a request nothing routes at all, which is material to be found.
                    seed.put(key, left);
                }
            }
            return new CraftingPlan.Shortage<>(consumable, seed, carrier);
        }
    }
}
