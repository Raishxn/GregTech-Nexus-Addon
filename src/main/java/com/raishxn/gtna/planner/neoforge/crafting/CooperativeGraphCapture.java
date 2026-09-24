package com.raishxn.gtna.planner.neoforge.crafting;

import com.raishxn.gtna.planner.api.amount.UfoAmount;
import com.raishxn.gtna.planner.api.crafting.planner.CraftingPattern;
import com.raishxn.gtna.planner.api.crafting.planner.ImmutableCraftingGraph;
import com.raishxn.gtna.planner.api.crafting.planner.PlanningCancellation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.LongSupplier;

/**
 * Resumable capture of every pattern reachable from one target key, driven in bounded slices.
 *
 * <p>
 * The machine never touches AE2 or Minecraft: a {@link Source} answers everything by canonical key
 * id, so the traversal runs on the server thread in per-tick slices and stays testable without a
 * world. Identity is the canonical id, never a native handle, and every quantity stays exact.
 *
 * <p>
 * Progress is deterministic and as fine as one pattern. The queue is a breadth-first walk over
 * ids in registration order, a slice only ends between two patterns, and one slice always performs
 * at least one unit of work, so a budget smaller than the first unit still finishes the capture
 * instead of spinning. A key with an unusual number of patterns no longer decides how long a slice
 * may take: the only work a slice cannot interrupt is one key lookup and one pattern, which is the
 * atomic tail the accounting reports. Wall-clock limits are a safety net for that tail, never the
 * reason a slice stops progressing.
 *
 * <p>
 * No partial state escapes. A cancelled, declined or discarded machine keeps no keys, patterns or
 * graph, and {@link #result()} only answers after {@link Status#COMPLETED}.
 */
public final class CooperativeGraphCapture<P> {

    /** Outcome of one {@link #advance(Slice)} call. */
    public enum Status {
        /** Every reachable pattern was captured and {@link #result()} is available. */
        COMPLETED,
        /** The slice budget ended with work left; call {@link #advance(Slice)} again later. */
        YIELDED,
        /** Cancelled or discarded: the capture must not be resumed and holds no partial state. */
        CANCELLED
    }

    /**
     * Time and edge allowance for one slice. The edge allowance makes a slice reproducible in tests;
     * the time allowance keeps one atomic unit - a key lookup plus one pattern - bounded.
     */
    public record Slice(long nanos, int maxEdges) {

        public Slice {
            if (nanos < 1L || maxEdges < 1) {
                throw new IllegalArgumentException("slice budget must be positive");
            }
        }
    }

    /**
     * Everything the machine needs to know about the grid, answered by canonical key id.
     *
     * <p>
     * Patterns are asked for one index at a time, so the adapter can be interrupted in the middle
     * of a fat key instead of validating every pattern of that key in one call.
     */
    public interface Source<P> {

        /** Facts about one key that reaches the traversal. Throws {@code Declined} to refuse it. */
        KeyDetails describe(String id);

        /** How many raw patterns this key reports, before this capture deduplicates them. */
        int patternCount(String id);

        /**
         * The pattern at one index, already validated and normalized by the adapter, or {@code null}
         * when the adapter has already captured that pattern for another key.
         */
        @Nullable
        PatternDetails<P> patternAt(String id, int index);
    }

    /** Key facts that decide whether the traversal can continue. */
    public record KeyDetails(String id, int amountPerByte, boolean emitter, int routes) {

        public KeyDetails {
            Objects.requireNonNull(id, "id");
            if (id.isEmpty() || amountPerByte < 1 || routes < 0) {
                throw new IllegalArgumentException("invalid key details for " + id);
            }
        }
    }

    /** One normalized input or output of a pattern: amount and byte conversion for the key id. */
    public record Slot(UfoAmount amount, int amountPerByte) {

        public Slot {
            Objects.requireNonNull(amount, "amount");
            if (amount.isZero() || amountPerByte < 1) {
                throw new IllegalArgumentException("invalid pattern slot");
            }
        }
    }

    /**
     * One pattern already validated by the adapter. {@code craftable} holds the ids that AE2 exposes
     * as selectable routes, which is a strict subset of the outputs for a coproduct pattern.
     */
    public record PatternDetails<P>(P handle, String id, int priority, Map<String, Slot> inputs,
                                    Map<String, Slot> reusableInputs, Map<String, Slot> outputs,
                                    Set<String> craftable) {

        /** Compatibility constructor for sources whose patterns have no reusable inputs. */
        public PatternDetails(P handle, String id, int priority, Map<String, Slot> inputs,
                              Map<String, Slot> outputs, Set<String> craftable) {
            this(handle, id, priority, inputs, Map.of(), outputs, craftable);
        }

        public PatternDetails {
            Objects.requireNonNull(handle, "handle");
            Objects.requireNonNull(id, "id");
            // Insertion-ordered copies: the traversal order must not depend on hash order, otherwise
            // which limit trips first could differ between two runs of the same grid.
            inputs = ordered(inputs);
            reusableInputs = ordered(reusableInputs);
            outputs = ordered(outputs);
            craftable = Set.copyOf(craftable);
            if (id.isEmpty() || outputs.isEmpty() || !outputs.keySet().containsAll(craftable)) {
                throw new IllegalArgumentException("invalid pattern details for " + id);
            }
        }
    }

    /** Complete capture. Only produced after the traversal finished inside its budgets. */
    public record Captured<P>(ImmutableCraftingGraph<String> graph, String target, Map<String, KeyDetails> keys,
                              Map<String, P> handles, boolean multiplePaths, long estimatedBytes) {

        public Captured {
            Objects.requireNonNull(graph, "graph");
            Objects.requireNonNull(target, "target");
            keys = ordered(keys);
            handles = ordered(handles);
            if (estimatedBytes < 0L) throw new IllegalArgumentException("estimatedBytes must be non-negative");
        }

        /** Byte conversion per key id, needed to size the AE2 plan. */
        public Map<String, Integer> amountsPerByte() {
            Map<String, Integer> amounts = new LinkedHashMap<>();
            keys.forEach((id, details) -> amounts.put(id, details.amountPerByte()));
            return Map.copyOf(amounts);
        }
    }

    private static <T> Map<String, T> ordered(Map<String, T> source) {
        return Collections.unmodifiableMap(new LinkedHashMap<>(source));
    }

    private final Source<P> source;
    private final String target;
    private final long revision;
    private final Ae2PlanningSnapshot.CaptureLimits limits;
    private final PlanningCancellation cancellation;
    private final Ae2PlanningSnapshot.CaptureBudget budget;
    private final ArrayDeque<String> pending = new ArrayDeque<>();
    private final Set<String> seen = new LinkedHashSet<>();
    private final Map<String, KeyDetails> keys = new LinkedHashMap<>();
    private final Map<String, CraftingPattern<String>> patterns = new LinkedHashMap<>();
    private final Map<String, P> handles = new LinkedHashMap<>();
    private boolean multiplePaths;
    private int patternsCaptured;
    @Nullable
    private String keyInProgress;
    private int keyPatterns;
    private int cursor;
    private long lastSliceNanos;
    private int lastSliceEdges;
    private long lastKeyNanos;
    private long lastPatternNanos;
    private long lastPublishNanos;
    private boolean discarded;
    private Status status = Status.YIELDED;
    private Captured<P> result;

    public CooperativeGraphCapture(Source<P> source, String target, long revision,
                                   Ae2PlanningSnapshot.CaptureLimits limits, PlanningCancellation cancellation) {
        this(source, target, revision, limits, cancellation, System::nanoTime);
    }

    CooperativeGraphCapture(Source<P> source, String target, long revision,
                            Ae2PlanningSnapshot.CaptureLimits limits, PlanningCancellation cancellation,
                            LongSupplier nanoTime) {
        this.source = Objects.requireNonNull(source, "source");
        this.target = Objects.requireNonNull(target, "target");
        if (revision < 0L) throw new IllegalArgumentException("revision must be non-negative");
        if (target.isEmpty()) throw new IllegalArgumentException("target id must not be empty");
        this.revision = revision;
        this.limits = Objects.requireNonNull(limits, "limits");
        this.cancellation = Objects.requireNonNull(cancellation, "cancellation");
        this.budget = new Ae2PlanningSnapshot.CaptureBudget(limits, nanoTime);
        pending.addLast(target);
    }

    /** Advances the capture by at most one slice. Idempotent once completed or cancelled. */
    public Status advance(Slice slice) {
        Objects.requireNonNull(slice, "slice");
        if (discarded) return Status.CANCELLED;
        if (status != Status.YIELDED) return status;
        budget.beginSlice(slice.nanos(), slice.maxEdges());
        lastKeyNanos = 0L;
        lastPatternNanos = 0L;
        lastPublishNanos = 0L;
        long started = System.nanoTime();
        try {
            budget.checkpoint();
            if (cancellation.isCancelled()) return discard();
            boolean worked = false;
            while (!finished()) {
                // At least one unit per slice: otherwise a slice allowance smaller than the clock
                // resolution would yield forever without progress and the capture would never finish.
                if (worked && budget.sliceExhausted()) return Status.YIELDED;
                if (cancellation.isCancelled()) return discard();
                budget.checkpoint();
                step();
                worked = true;
            }
            if (cancellation.isCancelled()) return discard();
            budget.checkpoint();
            long publishing = System.nanoTime();
            publish();
            lastPublishNanos = Math.max(0L, System.nanoTime() - publishing);
            return Status.COMPLETED;
        } catch (Ae2PlanningSnapshot.Declined declined) {
            discard();
            throw declined;
        } finally {
            lastSliceNanos = Math.max(0L, System.nanoTime() - started);
            lastSliceEdges = budget.sliceEdgesUsed();
            budget.endSlice();
        }
    }

    /** The captured graph. Only valid after {@link Status#COMPLETED}. */
    public Captured<P> result() {
        if (result == null) throw new IllegalStateException("capture did not complete");
        return result;
    }

    /** Wall-clock time consumed by the last slice, charged back to the shared tick budget. */
    public long lastSliceNanos() {
        return lastSliceNanos;
    }

    /**
     * Edges the last slice really consumed. Deterministic, so a gate can assert that a slice never
     * grew with the size of a key instead of relying on wall-clock readings.
     */
    public int lastSliceEdges() {
        return lastSliceEdges;
    }

    /** Time the last slice spent looking up keys, including their pattern counts. */
    public long lastKeyNanos() {
        return lastKeyNanos;
    }

    /** Time the last slice spent accepting patterns. */
    public long lastPatternNanos() {
        return lastPatternNanos;
    }

    /** Time the last slice spent building and publishing the graph, zero until the capture ends. */
    public long lastPublishNanos() {
        return lastPublishNanos;
    }

    public Source<P> source() {
        return source;
    }

    public boolean cancelled() {
        return discarded || status == Status.CANCELLED;
    }

    /** Diagnostics: patterns accepted so far by this capture. */
    public int patternsCaptured() {
        return patternsCaptured;
    }

    /** Whether every reachable key was consumed and only the result is missing. */
    private boolean finished() {
        return keyInProgress == null && pending.isEmpty();
    }

    /**
     * Performs one unit of work: either the lookup of one key, or one of the patterns of the key being
     * walked. The unit is the smallest interruption the machine allows, so a slice can end inside a
     * key that has many patterns.
     */
    private void step() {
        if (keyInProgress != null) {
            acceptPattern();
            return;
        }
        visitKey();
    }

    private void visitKey() {
        String id = pending.removeFirst();
        if (!seen.add(id)) return;
        long started = System.nanoTime();
        KeyDetails details = source.describe(id);
        if (!details.id().equals(id)) {
            throw new Ae2PlanningSnapshot.Declined("key id changed during capture");
        }
        KeyDetails known = keys.putIfAbsent(id, details);
        if (known == null) budget.key(id);
        if (details.emitter()) throw new Ae2PlanningSnapshot.Declined("crafting emitter");
        if (details.routes() > 1) multiplePaths = true;
        keyPatterns = source.patternCount(id);
        if (keyPatterns < 0) throw new Ae2PlanningSnapshot.Declined("negative pattern count for " + id);
        cursor = 0;
        keyInProgress = keyPatterns == 0 ? null : id;
        lastKeyNanos += Math.max(0L, System.nanoTime() - started);
    }

    private void acceptPattern() {
        String id = keyInProgress;
        int index = cursor;
        if (++cursor >= keyPatterns) keyInProgress = null;
        long started = System.nanoTime();
        PatternDetails<P> recipe = source.patternAt(id, index);
        if (recipe != null) accept(recipe);
        lastPatternNanos += Math.max(0L, System.nanoTime() - started);
    }

    private void accept(PatternDetails<P> recipe) {
        budget.edge();
        if (patterns.containsKey(recipe.id())) return;
        budget.pattern(recipe.id());
        verifySlots(recipe.inputs());
        verifySlots(recipe.reusableInputs());
        verifySlots(recipe.outputs());
        Map<String, UfoAmount> inputs = new LinkedHashMap<>();
        recipe.inputs().forEach((inputId, slot) -> inputs.put(inputId, slot.amount()));
        Map<String, UfoAmount> reusableInputs = new LinkedHashMap<>();
        recipe.reusableInputs().forEach((inputId, slot) -> reusableInputs.put(inputId, slot.amount()));
        Map<String, UfoAmount> outputs = new LinkedHashMap<>();
        recipe.outputs().forEach((outputId, slot) -> outputs.put(outputId, slot.amount()));
        patterns.put(recipe.id(), new CraftingPattern<>(recipe.id(), recipe.priority(), inputs,
                reusableInputs, outputs, recipe.craftable()));
        handles.put(recipe.id(), recipe.handle());
        recipe.inputs().keySet().forEach(input -> pending.addLast(input));
        recipe.reusableInputs().keySet().forEach(input -> pending.addLast(input));
        patternsCaptured++;
    }

    /** Counts one edge per pattern slot and refuses a slot the adapter left without an id. */
    private void verifySlots(Map<String, Slot> slots) {
        for (String id : slots.keySet()) {
            if (id.isEmpty()) throw new Ae2PlanningSnapshot.Declined("incomplete pattern slot");
            budget.edge();
        }
    }

    private void publish() {
        var graph = ImmutableCraftingGraph.create(revision, Comparator.<String>naturalOrder(), patterns.values());
        result = new Captured<>(graph, target, keys, handles, multiplePaths, budget.estimatedBytes());
        status = Status.COMPLETED;
    }

    /** Drops every partial structure so a cancelled capture can never publish half a graph. */
    private Status discard() {
        discarded = true;
        pending.clear();
        seen.clear();
        keys.clear();
        patterns.clear();
        handles.clear();
        keyInProgress = null;
        keyPatterns = 0;
        cursor = 0;
        result = null;
        return Status.CANCELLED;
    }
}
