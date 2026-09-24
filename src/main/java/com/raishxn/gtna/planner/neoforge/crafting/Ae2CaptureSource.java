package com.raishxn.gtna.planner.neoforge.crafting;

import net.minecraft.world.level.Level;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.stacks.AEKey;
import appeng.me.service.CraftingService;
import com.raishxn.gtna.planner.api.amount.UfoAmount;
import com.raishxn.gtna.planner.neoforge.crafting.CooperativeGraphCapture.KeyDetails;
import com.raishxn.gtna.planner.neoforge.crafting.CooperativeGraphCapture.PatternDetails;
import com.raishxn.gtna.planner.neoforge.crafting.CooperativeGraphCapture.Slot;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Server-thread adapter that answers the neutral capture machine in terms of canonical key ids.
 *
 * <p>
 * Every AE2 call and every semantic refusal lives here, so {@link CooperativeGraphCapture} stays
 * free of grid types and can be driven across ticks and unit tested with plain strings. Native
 * handles are only kept to rebuild the AE2 plan after the worker is done; no worker ever sees them.
 *
 * <p>
 * Refusals are deliberately loud: an unstable definition or an invalid input raises
 * {@link Ae2PlanningSnapshot.Declined} instead of producing a graph that would misrepresent the grid.
 * A substitution slot is pinned deterministically to the encoded first option; that option is still
 * accepted by the native pattern, so the resulting plan is executable without multiplying every tag
 * alternative into the graph. Container remainders are captured as coproducts; a remainder equal to
 * its input is captured as the reusable seed semantics the planner already supports. The answers are
 * cached per id, so a capture that resumes on
 * a later tick never repeats a grid query, and patterns are validated one index at a time, so a key
 * with many patterns is captured across several slices instead of in one call.
 */
final class Ae2CaptureSource implements CooperativeGraphCapture.Source<IPatternDetails> {

    private final Level level;
    private final ICraftingService service;
    private final Map<AEKey, String> ids = new HashMap<>();
    private final Map<String, AEKey> keys = new LinkedHashMap<>();
    private final Map<String, Integer> amountsPerByte = new LinkedHashMap<>();
    private final Map<String, List<IPatternDetails>> available = new HashMap<>();
    private final Map<String, KeyDetails> described = new HashMap<>();
    private final Set<String> capturedPatterns = new HashSet<>();

    Ae2CaptureSource(Level level, ICraftingService service) {
        this.level = level;
        this.service = service;
    }

    /** Canonical id of the requested key; also registers it with its byte conversion. */
    String targetId(AEKey target) {
        return id(target);
    }

    /** Native handle of every key the capture registered, by canonical id. */
    Map<String, AEKey> keys() {
        return keys;
    }

    @Override
    public KeyDetails describe(String id) {
        KeyDetails known = described.get(id);
        if (known != null) return known;
        AEKey key = nativeKey(id);
        var details = new KeyDetails(id, amountPerByte(id), service.canEmitFor(key),
                availableFor(id).size());
        described.put(id, details);
        return details;
    }

    @Override
    public int patternCount(String id) {
        return availableFor(id).size();
    }

    /**
     * Validates exactly one pattern of one key, so the capture can end its slice in the middle of a
     * fat key. A pattern another key already contributed is reported as {@code null} instead of being
     * dropped silently, which is what the machine expects from a shared pattern.
     */
    @Override
    @Nullable
    public PatternDetails<IPatternDetails> patternAt(String id, int index) {
        List<IPatternDetails> patterns = availableFor(id);
        if (index < 0 || index >= patterns.size()) {
            throw new Ae2PlanningSnapshot.Declined("pattern index out of range for " + id);
        }
        return capture(patterns.get(index));
    }

    /** Canonical id for a native key. Two keys with the same id make the capture incoherent. */
    String id(AEKey key) {
        String known = ids.get(key);
        if (known != null) return known;
        String id = Ae2PlanningSnapshot.canonical(key.toTagGeneric());
        AEKey previous = keys.putIfAbsent(id, key);
        if (previous != null && !previous.equals(key)) {
            throw new Ae2PlanningSnapshot.Declined("ambiguous key serialization");
        }
        int amount = key.getAmountPerByte();
        if (amount <= 0) throw new Ae2PlanningSnapshot.Declined("invalid byte conversion");
        ids.put(key, id);
        amountsPerByte.put(id, amount);
        return id;
    }

    @Nullable
    private PatternDetails<IPatternDetails> capture(IPatternDetails pattern) {
        var definition = pattern.getDefinition();
        if (definition == null) throw new Ae2PlanningSnapshot.Declined("pattern without stable definition");
        String patternId = Ae2PlanningSnapshot.canonical(definition.toTagGeneric());
        if (capturedPatterns.contains(patternId)) return null;
        Map<String, Slot> inputs = new LinkedHashMap<>();
        Map<String, Slot> reusableInputs = new LinkedHashMap<>();
        Map<String, Slot> remainderOutputs = new LinkedHashMap<>();
        var patternInputs = pattern.getInputs();
        for (int inputIndex = 0; inputIndex < patternInputs.length; inputIndex++) {
            var input = patternInputs[inputIndex];
            var options = input.getPossibleInputs();
            if (options.length == 0) throw invalidInput(patternId, inputIndex, "no possible inputs");
            if (options[0] == null || options[0].what() == null || options[0].amount() <= 0) {
                throw invalidInput(patternId, inputIndex, "invalid primary input");
            }
            if (input.getMultiplier() <= 0) {
                throw invalidInput(patternId, inputIndex, "non-positive multiplier " + input.getMultiplier());
            }
            AEKey inputKey = options[0].what();
            if (!input.isValid(inputKey, level)) {
                throw invalidInput(patternId, inputIndex, "primary input rejected: " + inputKey);
            }
            String inputId = id(inputKey);
            UfoAmount amount = UfoAmount.of(options[0].amount()).multiply(input.getMultiplier());
            AEKey remainderKey = input.getRemainingKey(inputKey);
            if (remainderKey == null) {
                inputs.merge(inputId, new Slot(amount, amountPerByte(inputId)), Ae2CaptureSource::merge);
                continue;
            }
            String remainderId = id(remainderKey);
            UfoAmount returned = UfoAmount.of(input.getMultiplier());
            if (remainderId.equals(inputId)) {
                if (amount.compareTo(returned) < 0) {
                    throw invalidInput(patternId, inputIndex,
                            "remainder exceeds primary input: " + inputKey);
                }
                UfoAmount consumed = amount.subtract(returned);
                if (!consumed.isZero()) {
                    inputs.merge(inputId, new Slot(consumed, amountPerByte(inputId)), Ae2CaptureSource::merge);
                }
                reusableInputs.merge(inputId,
                        new Slot(returned, amountPerByte(inputId)), Ae2CaptureSource::merge);
            } else {
                inputs.merge(inputId, new Slot(amount, amountPerByte(inputId)), Ae2CaptureSource::merge);
                remainderOutputs.merge(remainderId,
                        new Slot(returned, amountPerByte(remainderId)), Ae2CaptureSource::merge);
            }
        }
        Map<String, Slot> outputs = new LinkedHashMap<>();
        for (var result : pattern.getOutputs()) {
            if (result.amount() <= 0) throw new Ae2PlanningSnapshot.Declined("invalid output");
            String outputId = id(result.what());
            outputs.merge(outputId, new Slot(UfoAmount.of(result.amount()), amountPerByte(outputId)),
                    Ae2CaptureSource::merge);
        }
        remainderOutputs.forEach((id, slot) -> outputs.merge(id, slot, Ae2CaptureSource::merge));
        if (outputs.isEmpty()) throw new Ae2PlanningSnapshot.Declined("pattern without outputs");
        String primary = id(pattern.getPrimaryOutput().what());
        if (!outputs.containsKey(primary)) {
            throw new Ae2PlanningSnapshot.Declined("primary output is not a declared output");
        }
        capturedPatterns.add(patternId);
        return new PatternDetails<>(pattern, patternId, priority(pattern), inputs, reusableInputs, outputs,
                Set.of(primary));
    }

    private int priority(IPatternDetails pattern) {
        int highest = Integer.MIN_VALUE;
        if (service instanceof CraftingService nativeService) {
            for (var provider : nativeService.getProviders(pattern)) {
                highest = Math.max(highest, provider.getPatternPriority());
            }
        }
        return highest == Integer.MIN_VALUE ? 0 : highest;
    }

    private List<IPatternDetails> availableFor(String id) {
        return available.computeIfAbsent(id, known -> List.copyOf(service.getCraftingFor(nativeKey(known))));
    }

    private int amountPerByte(String id) {
        Integer amount = amountsPerByte.get(id);
        if (amount == null) throw new Ae2PlanningSnapshot.Declined("unknown key id: " + id);
        return amount;
    }

    private AEKey nativeKey(String id) {
        AEKey key = keys.get(id);
        if (key == null) throw new Ae2PlanningSnapshot.Declined("unknown key id: " + id);
        return key;
    }

    private static Slot merge(Slot left, Slot right) {
        return new Slot(left.amount().add(right.amount()), left.amountPerByte());
    }

    private static Ae2PlanningSnapshot.Declined invalidInput(String patternId, int inputIndex, String reason) {
        return new Ae2PlanningSnapshot.Declined(
                "pattern " + patternId + " input " + inputIndex + ": " + reason);
    }
}
