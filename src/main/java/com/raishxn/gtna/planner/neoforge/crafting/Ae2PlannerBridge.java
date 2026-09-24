package com.raishxn.gtna.planner.neoforge.crafting;

import net.minecraft.world.level.Level;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.CalculationStrategy;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.crafting.ICraftingSimulationRequester;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.crafting.CraftingPlan;
import com.mojang.logging.LogUtils;
import com.raishxn.gtna.planner.CoreConfig;
import com.raishxn.gtna.planner.api.amount.UfoAmount;
import com.raishxn.gtna.planner.api.crafting.planner.FeedbackCyclePlanner;
import com.raishxn.gtna.planner.api.crafting.planner.IterativeCraftingPlanner;
import com.raishxn.gtna.planner.api.crafting.planner.PlanningCancellation;
import com.raishxn.gtna.planner.api.crafting.planner.PlanningLimits;
import com.raishxn.gtna.planner.api.crafting.planner.PlanningRequest;
import com.raishxn.gtna.planner.api.crafting.planner.PlanningResult;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.math.BigInteger;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;

/**
 * Per-grid revision cache, cooperative capture queue and bounded worker queue.
 *
 * <p>
 * Graph capture no longer runs to completion in one call. A request gets one slice of the graph
 * inside the shared per-tick budget; if the slice ends with work left, AE2 receives a deferred future
 * and {@link #tick()} keeps feeding that capture on later ticks. A capture that finishes inside the
 * request's own slice behaves exactly like before, so the common case keeps its same-tick latency.
 *
 * <p>
 * A slice may end inside a key, between two of its patterns, so the time one grid may hold the
 * server thread no longer grows with how many patterns a single key has. Every slice is recorded in
 * {@link CaptureSliceMetrics}, which is what makes the measured p95 available to diagnostics.
 *
 * <p>
 * A request handed to a worker never touches the world: the snapshot, the inventory copy and the
 * patterns are immutable, and native handles are only read on the server thread to rebuild the plan.
 */
public final class Ae2PlannerBridge {

    private static final Logger LOG = LogUtils.getLogger();
    private static final Set<Ae2PlannerBridge> ACTIVE = ConcurrentHashMap.newKeySet();
    private static final AtomicLong NEXT_BRIDGE_ID = new AtomicLong();
    private static long bridgeRotation;
    private final long bridgeId = NEXT_BRIDGE_ID.getAndIncrement();
    private final ThreadPoolExecutor workers;
    private final InFlightRequestCoordinator<RequestKey, ICraftingPlan> requests;
    private final PlannerCircuitBreaker circuitBreaker = new PlannerCircuitBreaker();
    private final CaptureSnapshotCache<AEKey, Ae2PlanningSnapshot> snapshots;
    private final LinkedHashMap<AEKey, PendingCapture> captures = new LinkedHashMap<>();
    private volatile long revision = -1;
    private volatile long statusGeneration;
    private long hits;
    private long misses;
    private long backpressureRejections;
    private long circuitRejections;
    private long deferredRequests;
    private long captureCancellations;
    private long captureSlices;
    private int capturedPatterns;
    private int rotation;
    private volatile PlanningResult.Diagnostics lastDiagnostics;
    private volatile int lastGraphKeys;
    private volatile int lastGraphPatterns;
    private volatile int lastGraphEdges;
    private volatile String lastStatus = "idle";

    public Ae2PlannerBridge() {
        workers = WorkerPool.INSTANCE;
        requests = new InFlightRequestCoordinator<>(workers);
        var policy = CoreConfig.plannerPolicy();
        snapshots = new CaptureSnapshotCache<>(policy.snapshotCacheEntries(), policy.snapshotCacheBytes(),
                Ae2PlanningSnapshot::estimatedBytes);
        ACTIVE.add(this);
    }

    /**
     * One crafting request as AE2 hands it over: the revision it was captured at, a way to notice that
     * the revision moved while a capture is still running, and AE2's own planner as the fallback that
     * is only allowed after the RaishxCore attempt was discarded in full.
     */
    public record BeginRequest(Level level, IGrid grid, ICraftingSimulationRequester requester, AEKey target,
                               long amount, CalculationStrategy strategy, long revision,
                               LongSupplier revisionCheck, @Nullable VanillaFallback fallback) {}

    /** AE2's built-in planner, used for requests whose cooperative attempt no longer exists. */
    @FunctionalInterface
    public interface VanillaFallback {

        @Nullable
        Future<ICraftingPlan> plan();
    }

    @Nullable
    public Future<ICraftingPlan> begin(BeginRequest request) {
        Objects.requireNonNull(request, "request");
        Level level = request.level();
        IGrid grid = request.grid();
        ICraftingSimulationRequester requester = request.requester();
        AEKey target = request.target();
        long amount = request.amount();
        if (level == null || level.isClientSide || level.getServer() == null || !level.getServer().isSameThread() ||
                requester == null || requester.getActionSource() == null || amount <= 0)
            return null;
        var node = requester.getGridNode();
        // CraftConfirmMenu's requester only supplies an action source. Its default getGridNode()
        // therefore returns null even though this bridge belongs to the authoritative grid's
        // CraftingService. A non-null node remains useful for rejecting a genuinely foreign grid.
        if (node != null && node.getGrid() != grid) return null;
        CoreConfig.PlannerPolicy policy = CoreConfig.plannerPolicy();
        Object owner = requester.getActionSource().player()
                .<Object>map(player -> player.getUUID())
                .orElse(requester);
        try {
            if (revision != request.revision()) {
                revision = request.revision();
                statusGeneration++;
                snapshots.clear();
                requests.cancelAll();
                circuitBreaker.reset();
                cancelCaptures("grid revision changed");
            }
            snapshots.configure(policy.snapshotCacheEntries(), policy.snapshotCacheBytes());
            Ae2PlanningSnapshot cached = snapshots.get(target);
            if (cached != null) {
                hits++;
                var waiter = new Waiter(owner, statusGeneration, amount, request.strategy(), policy,
                        inventoryOf(grid, requester), request.fallback());
                settle(cached, waiter);
                return finish(waiter);
            }
            PendingCapture pending = captures.get(target);
            if (pending == null) {
                if (captures.size() >= policy.maxPendingCaptures()) {
                    backpressureRejections++;
                    lastStatus = "ae2: too many pending captures";
                    return null;
                }
                var source = new Ae2CaptureSource(level, grid.getCraftingService());
                pending = new PendingCapture(source, target, source.targetId(target), request.revision(),
                        captureLimits(policy), request.revisionCheck());
                captures.put(target, pending);
                misses++;
            }
            var waiter = new Waiter(owner, statusGeneration, amount, request.strategy(), policy,
                    inventoryOf(grid, requester), request.fallback());
            pending.waiters().add(waiter);
            advance(target, pending);
            if (waiter.outcome() == Outcome.PENDING) {
                deferredRequests++;
                lastStatus = "capture deferred";
            }
            return finish(waiter);
        } catch (Ae2PlanningSnapshot.Declined declined) {
            lastStatus = "ae2: " + declined.getMessage();
            LOG.debug("RaishxCore delegated {} to AE2 during the initial capture: {}", target,
                    declined.getMessage());
            return null;
        }
    }

    /** Returns the value AE2 receives: a plan future, a deferred future, or {@code null} for fallback. */
    @Nullable
    private Future<ICraftingPlan> finish(Waiter waiter) {
        waiter.markReturned();
        return switch (waiter.outcome()) {
            case PLANNING -> waiter.plan();
            case REFUSED -> null;
            case PENDING -> waiter.future();
        };
    }

    /** One capture slice, from the shared budget, with cancellation checked at every transition. */
    private void advance(AEKey target, PendingCapture pending) {
        if (pending.revisionCheck().getAsLong() != pending.revision()) {
            rejectCapture(target, pending, "grid revision changed during capture");
            return;
        }
        long reservation = tickBudget().reserve(pending.limits().sliceNanos());
        if (reservation <= 0L) return;
        CooperativeGraphCapture.Status status;
        try {
            status = pending.machine().advance(new CooperativeGraphCapture.Slice(
                    reservation, pending.limits().sliceEdges()));
        } catch (Ae2PlanningSnapshot.Declined declined) {
            settleSlice(pending, reservation);
            rejectCapture(target, pending, declined.getMessage());
            return;
        } catch (RuntimeException unexpected) {
            settleSlice(pending, reservation);
            LOG.warn("RaishxCore graph capture failed unexpectedly.", unexpected);
            rejectCapture(target, pending, "capture failed: " + unexpected);
            return;
        }
        settleSlice(pending, reservation);
        try {
            switch (status) {
                case COMPLETED -> publish(target, pending);
                case CANCELLED -> cancelCapture(target, pending, "capture cancelled");
                case YIELDED -> {}
            }
        } catch (RuntimeException unexpected) {
            // A failure here must never escape into the server tick loop, and it must never leave a
            // caller waiting on a capture that no longer exists.
            LOG.warn("RaishxCore capture completion failed unexpectedly.", unexpected);
            rejectCapture(target, pending, "capture failed: " + unexpected);
        }
    }

    private void settleSlice(PendingCapture pending, long reservation) {
        CooperativeGraphCapture<IPatternDetails> machine = pending.machine();
        long spent = machine.lastSliceNanos();
        tickBudget().settle(reservation, spent);
        captureSlices++;
        metrics().recordSlice(spent, machine.lastSliceEdges(), machine.lastKeyNanos(),
                machine.lastPatternNanos(), machine.lastPublishNanos());
    }

    /** Publishes a complete capture. A partial graph is never cached and never planned. */
    private void publish(AEKey target, PendingCapture pending) {
        // Built before anything is cleared, so a failure here leaves the waiters recoverable.
        var snapshot = Ae2PlanningSnapshot.of(pending.machine().result(), pending.source().keys());
        snapshots.put(target, snapshot);
        captures.remove(target);
        capturedPatterns = pending.machine().patternsCaptured();
        List<Waiter> waiters = List.copyOf(pending.waiters());
        pending.waiters().clear();
        for (Waiter waiter : waiters) {
            if (waiter.future().abandoned()) continue;
            try {
                settle(snapshot, waiter);
            } catch (RuntimeException unexpected) {
                LOG.warn("RaishxCore could not submit a plan for a captured graph.", unexpected);
                waiter.refuse("plan submission failed: " + unexpected);
            }
        }
    }

    /**
     * Discards a capture that hit a limit. Waiters that already hold a deferred future are served by
     * AE2's own planner, because their request is still perfectly valid.
     */
    private void rejectCapture(AEKey target, PendingCapture pending, String reason) {
        captures.remove(target);
        lastStatus = "ae2: " + reason;
        LOG.debug("RaishxCore delegated {} to AE2 after a deferred capture: {}", target, reason);
        List<Waiter> waiters = List.copyOf(pending.waiters());
        pending.waiters().clear();
        for (Waiter waiter : waiters) waiter.refuse(reason);
    }

    /** Hard cancellation: the grid, the player or the server is gone, so no plan is worth starting. */
    private void cancelCapture(AEKey target, PendingCapture pending, String reason) {
        captures.remove(target);
        pending.cancellation().cancel();
        List<Waiter> waiters = List.copyOf(pending.waiters());
        pending.waiters().clear();
        for (Waiter waiter : waiters) {
            if (!waiter.returned()) {
                waiter.refuse(reason);
                continue;
            }
            captureCancellations++;
            waiter.future().abandon();
        }
        lastStatus = "ae2: " + reason;
    }

    /**
     * Advances every pending capture for one tick, in rotating order so no grid starves another, and
     * reports how many slices it ran so an idle tick is never recorded as a capture sample.
     */
    int advanceCaptures() {
        if (captures.isEmpty()) return 0;
        int slices = 0;
        List<Map.Entry<AEKey, PendingCapture>> order = new ArrayList<>(captures.entrySet());
        int start = Math.floorMod(rotation++, order.size());
        for (int index = 0; index < order.size(); index++) {
            if (tickBudget().exhausted()) break;
            var entry = order.get((start + index) % order.size());
            PendingCapture pending = captures.get(entry.getKey());
            if (pending == null) continue;
            pending.waiters().removeIf(waiter -> waiter.future().abandoned());
            if (pending.waiters().isEmpty()) {
                cancelCapture(entry.getKey(), pending, "capture abandoned by its callers");
                continue;
            }
            advance(entry.getKey(), pending);
            slices++;
        }
        return slices;
    }

    /** Opens the shared budget and rotates the first grid served so a busy grid cannot starve another. */
    public static void tick() {
        tickBudget().beginTick();
        int slices = 0;
        List<Ae2PlannerBridge> active = new ArrayList<>(ACTIVE);
        active.removeIf(bridge -> bridge.captures.isEmpty());
        active.sort(Comparator.comparingLong(bridge -> bridge.bridgeId));
        if (!active.isEmpty()) {
            int start = Math.floorMod(bridgeRotation++, active.size());
            for (int index = 0; index < active.size() && !tickBudget().exhausted(); index++) {
                slices += active.get((start + index) % active.size()).advanceCaptures();
            }
        }
        // One sample per tick that really captured something, so idle ticks cannot flatter the p95.
        if (slices > 0) metrics().recordTick(tickBudget().spentThisTick());
    }

    private static CaptureBudgetPool tickBudget() {
        return TickBudget.INSTANCE;
    }

    private static CaptureSliceMetrics metrics() {
        return CaptureMetrics.INSTANCE;
    }

    private void settle(Ae2PlanningSnapshot snapshot, Waiter waiter) {
        if (waiter.future().abandoned()) {
            waiter.refuse("request abandoned before planning");
            return;
        }
        Map<String, UfoAmount> stock = stockFor(snapshot, waiter.inventory());
        var requestKey = new RequestKey(revision, snapshot.target(), waiter.amount(), waiter.strategy(), stock,
                waiter.policy());
        if (!circuitBreaker.tryAcquire()) {
            circuitRejections++;
            lastStatus = "ae2: planner circuit open";
            waiter.refuse("planner circuit open");
            return;
        }
        try {
            Future<ICraftingPlan> plan = requests.submit(requestKey, waiter.owner(),
                    PlanningTask.classified(
                            () -> calculateGuarded(snapshot, stock, waiter.amount(), waiter.strategy(),
                                    waiter.generation(), waiter.policy()),
                            status -> recordStatus(waiter.generation(), status),
                            unexpected -> LOG.warn("RaishxCore planning failed unexpectedly.", unexpected)),
                    waiter.policy().maxInFlightPerGrid());
            waiter.deliver(plan);
        } catch (InFlightRequestCoordinator.PerGridLimitExceededException busy) {
            circuitBreaker.abortProbe(Duration.ofMillis(waiter.policy().circuitCooldownMillis()));
            backpressureRejections++;
            lastStatus = "ae2: per-grid planner limit";
            waiter.refuse("per-grid planner limit");
        } catch (RejectedExecutionException busy) {
            circuitBreaker.abortProbe(Duration.ofMillis(waiter.policy().circuitCooldownMillis()));
            backpressureRejections++;
            lastStatus = "ae2: planner queue full";
            waiter.refuse("planner queue full");
        }
    }

    private static Map<String, UfoAmount> stockFor(Ae2PlanningSnapshot snapshot, Map<AEKey, Long> inventory) {
        Map<String, UfoAmount> stock = new HashMap<>();
        inventory.forEach((key, amount) -> {
            String id = snapshot.keyIds().get(key);
            if (id != null && !id.equals(snapshot.target())) stock.put(id, UfoAmount.of(amount));
        });
        return Map.copyOf(stock);
    }

    /**
     * Inventory exactly as the request saw it. AE2's own planner simulates from a snapshot taken when
     * the calculation starts, so a capture that spans ticks must not drift to a later inventory.
     */
    private static Map<AEKey, Long> inventoryOf(IGrid grid, ICraftingSimulationRequester requester) {
        KeyCounter inventory = requester.getActionSource().player().isPresent() ?
                grid.getStorageService().getInventory().getAvailableStacks() :
                grid.getStorageService().getCachedInventory();
        Map<AEKey, Long> copy = new LinkedHashMap<>();
        for (var entry : inventory) {
            if (entry.getLongValue() > 0L) copy.put(entry.getKey(), entry.getLongValue());
        }
        return Map.copyOf(copy);
    }

    private ICraftingPlan calculateGuarded(Ae2PlanningSnapshot snapshot, Map<String, UfoAmount> stock,
                                           long amount, CalculationStrategy strategy, long generation,
                                           CoreConfig.PlannerPolicy policy) throws TimeoutException {
        try {
            ICraftingPlan plan = calculate(snapshot, stock, amount, strategy, generation, policy);
            circuitBreaker.recordSuccess();
            return plan;
        } catch (java.util.concurrent.CancellationException cancelled) {
            circuitBreaker.abortProbe(Duration.ofMillis(policy.circuitCooldownMillis()));
            throw cancelled;
        } catch (TimeoutException | RuntimeException failure) {
            circuitBreaker.recordFailure(policy.circuitFailureThreshold(),
                    Duration.ofMillis(policy.circuitCooldownMillis()));
            throw failure;
        }
    }

    private ICraftingPlan calculate(Ae2PlanningSnapshot snapshot, Map<String, UfoAmount> stock,
                                    long amount, CalculationStrategy strategy,
                                    long generation, CoreConfig.PlannerPolicy policy) throws TimeoutException {
        long started = System.nanoTime();
        // Analysed once per request rather than once per probe: the binary search below runs this many
        // times over the same graph, and recognising a feedback loop is a pass over every pattern.
        var loops = FeedbackCyclePlanner.analyse(snapshot.graph(), snapshot.target(),
                UfoAmount.of(amount), stock);
        // Read once per request: the operator policy scales what consumers registered, and with nothing
        // registered or configured this is the constant empty map, so the unweighted path is untouched.
        Map<String, Long> missingWeights = CoreConfig.missingWeightPolicy().effective();
        var planningGraph = loops.augmentedGraph();
        LOG.debug(
                "RaishxCore planning started target={} amount={} strategy={} graphKeys={} graphPatterns={} graphEdges={} routeAlternatives={} mode={} feedbackMacro={}",
                snapshot.target(), amount, strategy, planningGraph.keyCount(), planningGraph.patternCount(),
                planningGraph.edgeCount(), planningGraph.routeChoiceAlternatives(),
                usesFastPlanner(planningGraph, policy.exactSearchChoiceLimit()) ? "fast" : "exact",
                loops.applies());
        var full = attempt(snapshot, stock, amount, started, generation, policy, loops, missingWeights);
        if (full.status() == PlanningResult.Status.COMPLETE) return adapt(snapshot, full);
        if (strategy == CalculationStrategy.CRAFT_LESS) {
            long successful = 0;
            PlanningResult<String> best = null;
            for (long increment = Long.highestOneBit(amount); increment > 0; increment /= 2) {
                if (increment >= amount - successful) continue;
                long test = successful + increment;
                var candidate = attempt(snapshot, stock, test, started, generation, policy, loops,
                        missingWeights);
                if (candidate.status() == PlanningResult.Status.COMPLETE) {
                    successful = test;
                    best = candidate;
                }
            }
            if (best != null) return adapt(snapshot, best);
        }
        return adapt(snapshot, full);
    }

    private PlanningResult<String> attempt(Ae2PlanningSnapshot snapshot, Map<String, UfoAmount> stock,
                                           long amount, long started, long generation,
                                           CoreConfig.PlannerPolicy policy,
                                           FeedbackCyclePlanner<String> loops,
                                           Map<String, Long> missingWeights) throws TimeoutException {
        long timeoutNanos = Duration.ofMillis(policy.timeoutMillis()).toNanos();
        long remaining = timeoutNanos - (System.nanoTime() - started);
        if (remaining <= 0) throw new TimeoutException("RaishxCore planning deadline");
        var limits = new PlanningLimits(policy.maxOperations(), policy.maxDepth(),
                Duration.ofNanos(remaining), policy.checkpointInterval());
        var planner = new IterativeCraftingPlanner<String>();
        var request = new PlanningRequest<>(snapshot.target(), UfoAmount.of(amount), stock, limits,
                PlanningCancellation.NEVER, missingWeights);
        var graph = loops.augmentedGraph();
        var result = usesFastPlanner(graph, policy.exactSearchChoiceLimit()) ? planner.planFast(graph, request) :
                planner.plan(graph, request);
        LOG.debug(
                "RaishxCore planning attempt target={} amount={} status={} operations={} maximumDepth={} elapsedNanos={} cycleCuts={}",
                snapshot.target(), amount, result.status(), result.diagnostics().operations(),
                result.diagnostics().maximumDepth(), result.diagnostics().elapsedNanos(),
                result.diagnostics().cycleCuts());
        if (statusGeneration == generation) {
            lastDiagnostics = result.diagnostics();
            lastStatus = result.status().name();
            // Read off the graph rather than counted here: the numbers were computed once when the
            // snapshot was compiled, so asking how large it is costs nothing per plan.
            lastGraphKeys = graph.keyCount();
            lastGraphPatterns = graph.patternCount();
            lastGraphEdges = graph.edgeCount();
        }
        switch (result.status()) {
            case COMPLETE, MISSING_INGREDIENTS -> {
                return withLoopsExpanded(loops, result);
            }
            case CANCELLED -> throw new java.util.concurrent.CancellationException("RaishxCore planning cancelled");
            default -> throw new TimeoutException("RaishxCore planning stopped: " + result.status());
        }
    }

    /**
     * Rewrites a plan's feedback macro back into the patterns it stands for. Everything downstream of
     * here — the adapted plan an AE2 commit walks, the reported leftovers — has to see the real
     * patterns, because that is what the world runs.
     */
    private static PlanningResult<String> withLoopsExpanded(FeedbackCyclePlanner<String> loops,
                                                            PlanningResult<String> result) {
        if (!loops.applies()) {
            return result;
        }
        com.raishxn.gtna.planner.api.crafting.planner.CraftingPlan<String> expanded = loops.expand(result.plan());
        return expanded == result.plan() ? result :
                new PlanningResult<>(result.status(), expanded, result.diagnostics());
    }

    static CraftingPlan adapt(Ae2PlanningSnapshot snapshot, PlanningResult<String> result) {
        if (result.status() != PlanningResult.Status.COMPLETE &&
                result.status() != PlanningResult.Status.MISSING_INGREDIENTS) {
            throw new IllegalArgumentException("cannot adapt interrupted planning");
        }
        var plan = result.plan();
        KeyCounter used = new KeyCounter();
        plan.extractedFromInventory()
                .forEach((id, amount) -> used.add(snapshot.keys().get(id), amount.longValueExact()));
        // The neutral planner leaves a reusable seed in its remaining inventory because the recipe
        // hands it back. AE2's CPU still has to reserve that seed before it can execute the first
        // pattern. Reserve the largest amount any selected pattern needs, once per key, matching the
        // maximum inventory dip its native simulator would report rather than multiplying by runs.
        Map<String, UfoAmount> reusable = new HashMap<>();
        plan.patternExecutions().keySet().forEach(pattern -> pattern.reusableInputs().forEach(
                (id, amount) -> reusable.merge(id, amount, UfoAmount::max)));
        reusable.forEach((id, required) -> {
            UfoAmount availableSeed = plan.remaining().getOrDefault(id, UfoAmount.ZERO).min(required);
            if (!availableSeed.isZero()) {
                used.add(snapshot.keys().get(id), availableSeed.longValueExact());
            }
        });
        KeyCounter missing = new KeyCounter();
        plan.missing().forEach((id, amount) -> {
            long displayAmount = toAe2MissingDisplayAmount(amount);
            if (amount.asBigInteger().compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0) {
                LOG.warn(
                        "AE2 cannot display the exact missing amount for {} ({}); capping only the simulation UI counter at Long.MAX_VALUE.",
                        id, amount);
            }
            missing.add(snapshot.keys().get(id), displayAmount);
        });
        Map<IPatternDetails, Long> times = new HashMap<>();
        Map<String, UfoAmount> demand = new HashMap<>();
        Map<String, UfoAmount> produced = new HashMap<>();
        demand.put(plan.target(), plan.requested());
        BigInteger bytes = BigInteger.valueOf(8);
        for (var entry : plan.patternExecutions().entrySet()) {
            long patternTimes = toAe2PatternDisplayAmount(entry.getValue(), plan.complete());
            if (!plan.complete() && entry.getValue().asBigInteger().compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0) {
                LOG.warn(
                        "AE2 cannot display the exact simulated execution count for pattern {} ({}); capping only the simulation UI counter at Long.MAX_VALUE.",
                        entry.getKey().id(), entry.getValue());
            }
            times.merge(snapshot.patterns().get(entry.getKey().id()), patternTimes,
                    plan.complete() ? Math::addExact : Ae2PlannerBridge::saturatedAdd);
            bytes = bytes.add(entry.getValue().asBigInteger())
                    .add(BigInteger.valueOf(8L * (1 + entry.getKey().inputs().size())));
            entry.getKey().inputs().forEach(
                    (key, value) -> demand.merge(key, value.multiply(entry.getValue().asBigInteger()), UfoAmount::add));
            entry.getKey().outputs().forEach((key, value) -> produced.merge(key,
                    value.multiply(entry.getValue().asBigInteger()), UfoAmount::add));
        }
        reusable.forEach((key, value) -> demand.merge(key, value, UfoAmount::add));
        // AE2's public plan uses long counters. Pattern executions and committed inventory use stay
        // exact, while a missing-ingredient preview may cap only its display counter: it is a
        // simulation and therefore can never be submitted to a CPU. Demand remains BigInteger for
        // byte accounting, so the planner's result itself is not truncated.
        if (plan.complete()) {
            for (var amount : produced.values()) amount.longValueExact();
        }
        for (var entry : demand.entrySet()) {
            if (plan.complete()) entry.getValue().longValueExact();
            BigInteger divisor = BigInteger.valueOf(snapshot.amountsPerByte().get(entry.getKey()));
            BigInteger numerator = entry.getValue().asBigInteger().multiply(BigInteger.valueOf(8));
            bytes = bytes.add(numerator.add(divisor).subtract(BigInteger.ONE).divide(divisor));
        }
        return new CraftingPlan(new GenericStack(snapshot.keys().get(plan.target()), plan.requested().longValueExact()),
                bytes.longValueExact(), !plan.complete(), snapshot.multiplePaths(), used, new KeyCounter(), missing,
                Map.copyOf(times));
    }

    static long toAe2MissingDisplayAmount(UfoAmount amount) {
        return amount.asBigInteger().min(BigInteger.valueOf(Long.MAX_VALUE)).longValueExact();
    }

    static boolean usesFastPlanner(com.raishxn.gtna.planner.api.crafting.planner.ImmutableCraftingGraph<?> graph,
                                   int exactSearchChoiceLimit) {
        return graph.routeChoiceAlternatives() >= exactSearchChoiceLimit;
    }

    static long toAe2PatternDisplayAmount(UfoAmount amount, boolean complete) {
        return complete ? amount.longValueExact() : amount.longValueSaturated();
    }

    private static long saturatedAdd(long left, long right) {
        return left >= Long.MAX_VALUE - right ? Long.MAX_VALUE : left + right;
    }

    /** Records that this request was declined by the config kill-switch. */
    public void recordDisabled() {
        invalidate("disabled");
    }

    public void invalidate(String status) {
        statusGeneration++;
        requests.cancelAll();
        snapshots.clear();
        circuitBreaker.reset();
        cancelCaptures(status);
        lastStatus = status;
        lastDiagnostics = null;
    }

    /** Drops every pending capture and cancels the attempts that already reached a caller. */
    private void cancelCaptures(String reason) {
        if (captures.isEmpty()) return;
        List<PendingCapture> pending = List.copyOf(captures.values());
        captures.clear();
        for (PendingCapture capture : pending) {
            capture.cancellation().cancel();
            for (Waiter waiter : capture.waiters()) {
                if (!waiter.returned()) continue;
                captureCancellations++;
                waiter.future().abandon();
            }
            capture.waiters().clear();
        }
        lastStatus = reason;
    }

    public void close() {
        invalidate("grid closed");
        ACTIVE.remove(this);
    }

    /**
     * Diagnostics of every live planner. Only the payloads leave, never the bridges: the report is a
     * bag of numbers, and a caller that held a bridge could hold a grid through it.
     */
    public static List<Diagnostics> activeDiagnostics() {
        List<Diagnostics> all = new ArrayList<>(ACTIVE.size());
        for (Ae2PlannerBridge bridge : ACTIVE) {
            all.add(bridge.diagnostics());
        }
        return all;
    }

    public static void cancelForPlayer(UUID playerId) {
        for (Ae2PlannerBridge bridge : ACTIVE) {
            bridge.requests.cancelOwner(playerId);
            bridge.cancelCapturesOwnedBy(playerId);
        }
    }

    private void cancelCapturesOwnedBy(Object owner) {
        if (captures.isEmpty()) return;
        List<AEKey> empty = new ArrayList<>();
        for (var entry : captures.entrySet()) {
            entry.getValue().waiters().removeIf(waiter -> {
                if (!waiter.owner().equals(owner)) return false;
                captureCancellations++;
                waiter.future().abandon();
                return true;
            });
            if (entry.getValue().waiters().isEmpty()) empty.add(entry.getKey());
        }
        for (AEKey target : empty) {
            PendingCapture pending = captures.remove(target);
            if (pending != null) pending.cancellation().cancel();
        }
    }

    public static void cancelForServerStop() {
        for (Ae2PlannerBridge bridge : ACTIVE) bridge.invalidate("server stopping");
        ACTIVE.clear();
        bridgeRotation = 0L;
    }

    private void recordStatus(long generation, String status) {
        if (statusGeneration == generation) lastStatus = status;
    }

    public Diagnostics diagnostics() {
        var requestStats = requests.stats();
        var circuit = circuitBreaker.snapshot();
        var budget = tickBudget();
        return new Diagnostics(revision, hits, misses, lastStatus, lastDiagnostics,
                lastGraphKeys, lastGraphPatterns, lastGraphEdges,
                requestStats.inFlight(), requestStats.submitted(), requestStats.deduplicated(),
                requestStats.cancelled(), workers.getActiveCount(), workers.getQueue().size(),
                backpressureRejections, circuitRejections, circuit.state().name(), circuit.consecutiveFailures(),
                captures.size(), deferredRequests, captureSlices, captureCancellations, capturedPatterns,
                snapshots.size(), snapshots.bytes(), snapshots.evictions(), budget.remainingNanos(),
                metrics().snapshot());
    }

    public record Diagnostics(long revision, long cacheHits, long cacheMisses, String status,
                              @Nullable PlanningResult.Diagnostics lastPlan,
                              int graphKeys, int graphPatterns, int graphEdges, int inFlightRequests,
                              long submittedRequests, long deduplicatedRequests, long cancelledRequests,
                              int activeWorkers, int queuedRequests, long backpressureRejections,
                              long circuitRejections, String circuitState, int consecutiveFailures,
                              int pendingCaptures, long deferredRequests, long captureSlices,
                              long captureCancellations, int capturedPatterns, int cachedSnapshots,
                              long cacheBytes, long cacheEvictions, long tickBudgetRemainingNanos,
                              CaptureSliceMetrics.Snapshot captureMetrics) {}

    private record RequestKey(long revision, String target, long amount, CalculationStrategy strategy,
                              Map<String, UfoAmount> inventory, CoreConfig.PlannerPolicy policy) {}

    /** What a caller of {@link #begin(BeginRequest)} is waiting for. */
    private enum Outcome {
        PENDING,
        PLANNING,
        REFUSED
    }

    /**
     * One request attached to a capture, plus the inventory it saw and the fallback it may use. A
     * waiter only becomes visible to a caller once {@code returned} is set, which is what decides
     * whether a refusal can still be answered with {@code null} (AE2 plans it this tick) or has to be
     * answered through the deferred future.
     */
    private final class Waiter {

        private final Object owner;
        private final long generation;
        private final long amount;
        private final CalculationStrategy strategy;
        private final CoreConfig.PlannerPolicy policy;
        private final Map<AEKey, Long> inventory;
        @Nullable
        private final VanillaFallback fallback;
        private final DeferredPlanFuture future = new DeferredPlanFuture();
        @Nullable
        private Future<ICraftingPlan> plan;
        private Outcome outcome = Outcome.PENDING;
        private boolean returned;

        Waiter(Object owner, long generation, long amount, CalculationStrategy strategy,
               CoreConfig.PlannerPolicy policy, Map<AEKey, Long> inventory,
               @Nullable VanillaFallback fallback) {
            this.owner = owner;
            this.generation = generation;
            this.amount = amount;
            this.strategy = strategy;
            this.policy = policy;
            this.inventory = inventory;
            this.fallback = fallback;
        }

        Object owner() {
            return owner;
        }

        long generation() {
            return generation;
        }

        long amount() {
            return amount;
        }

        CalculationStrategy strategy() {
            return strategy;
        }

        CoreConfig.PlannerPolicy policy() {
            return policy;
        }

        Map<AEKey, Long> inventory() {
            return inventory;
        }

        DeferredPlanFuture future() {
            return future;
        }

        Outcome outcome() {
            return outcome;
        }

        boolean returned() {
            return returned;
        }

        @Nullable
        Future<ICraftingPlan> plan() {
            return plan;
        }

        void markReturned() {
            returned = true;
        }

        void deliver(Future<ICraftingPlan> planning) {
            plan = new OriginTrackingFuture(planning, PlanningOrigin.RAISHX);
            outcome = Outcome.PLANNING;
            future.complete(plan, PlanningOrigin.RAISHX);
        }

        /** No own plan for this attempt: answer with a fallback when the caller cannot take null. */
        void refuse(String reason) {
            outcome = Outcome.REFUSED;
            if (!returned || future.abandoned()) return;
            Future<ICraftingPlan> vanilla = null;
            if (fallback != null) {
                try {
                    vanilla = fallback.plan();
                } catch (RuntimeException failure) {
                    LOG.warn("AE2 fallback planner failed after a discarded capture.", failure);
                }
            }
            if (vanilla == null) {
                lastStatus = "ae2: " + reason;
                future.abandon();
                return;
            }
            lastStatus = "ae2 fallback: " + reason;
            future.complete(vanilla, PlanningOrigin.AE2);
        }
    }

    /** One capture in progress for one grid, shared by every waiter that asks for the same target. */
    private final class PendingCapture {

        private final Ae2CaptureSource source;
        private final CooperativeGraphCapture<IPatternDetails> machine;
        private final PlanningCancellation.Source cancellation = PlanningCancellation.source();
        private final long revision;
        private final Ae2PlanningSnapshot.CaptureLimits limits;
        private final LongSupplier revisionCheck;
        private final List<Waiter> waiters = new ArrayList<>();

        PendingCapture(Ae2CaptureSource source, AEKey target, String targetId, long revision,
                       Ae2PlanningSnapshot.CaptureLimits limits, LongSupplier revisionCheck) {
            this.source = source;
            this.revision = revision;
            this.limits = limits;
            this.revisionCheck = revisionCheck;
            this.machine = new CooperativeGraphCapture<>(source, targetId, revision, limits, cancellation);
        }

        Ae2CaptureSource source() {
            return source;
        }

        CooperativeGraphCapture<IPatternDetails> machine() {
            return machine;
        }

        PlanningCancellation.Source cancellation() {
            return cancellation;
        }

        long revision() {
            return revision;
        }

        Ae2PlanningSnapshot.CaptureLimits limits() {
            return limits;
        }

        LongSupplier revisionCheck() {
            return revisionCheck;
        }

        List<Waiter> waiters() {
            return waiters;
        }
    }

    private static Ae2PlanningSnapshot.CaptureLimits captureLimits(CoreConfig.PlannerPolicy policy) {
        return new Ae2PlanningSnapshot.CaptureLimits(Duration.ofMillis(policy.snapshotTimeoutMillis()),
                policy.snapshotMaxEdges(), policy.snapshotMaxKeys(), policy.snapshotMaxEstimatedBytes(),
                Duration.ofMillis(policy.snapshotSliceMillis()).toNanos(), policy.snapshotSliceEdges());
    }

    private static final class TickBudget {

        private static final CaptureBudgetPool INSTANCE = new CaptureBudgetPool(
                Duration.ofMillis(CoreConfig.plannerPolicy().snapshotTickBudgetMillis()));
    }

    /** Server-wide capture metrics: the budget they describe is server-wide too. */
    private static final class CaptureMetrics {

        private static final CaptureSliceMetrics INSTANCE = new CaptureSliceMetrics();
    }

    private static final class WorkerPool {

        private static final ThreadPoolExecutor INSTANCE = create();

        private static ThreadPoolExecutor create() {
            CoreConfig.PlannerPolicy policy = CoreConfig.plannerPolicy();
            var executor = new ThreadPoolExecutor(policy.workers(), policy.workers(), 30, TimeUnit.SECONDS,
                    new ArrayBlockingQueue<>(policy.queueCapacity()), runnable -> {
                        Thread worker = new Thread(runnable, "RaishxCore-planner");
                        worker.setDaemon(true);
                        return worker;
                    });
            executor.allowCoreThreadTimeOut(true);
            return executor;
        }
    }
}
