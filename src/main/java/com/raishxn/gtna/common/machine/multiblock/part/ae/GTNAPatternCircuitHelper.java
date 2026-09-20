package com.raishxn.gtna.common.machine.multiblock.part.ae;

import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.crafting.pattern.AEProcessingPattern;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Embedded-circuit handling for encoded processing patterns (GTLCore
 * {@code MEBufferPatternHelper} parity, limited to the operations the buffer UI needs).
 *
 * <p>
 * Operations:
 * <ul>
 * <li>{@link #extractCircuit} — reads the circuit configuration carried inside a pattern.</li>
 * <li>{@link #withCircuit} — rewrites a pattern with a circuit, optionally replacing an
 * existing one; used by the "embed circuit in all patterns" action.</li>
 * <li>{@link #withoutCircuit} — strips the circuit, used by "remove circuits from all".</li>
 * </ul>
 */
public final class GTNAPatternCircuitHelper {

    private GTNAPatternCircuitHelper() {}

    /** @return the circuit configuration inside the pattern, or -1 when it carries none. */
    public static int extractCircuit(ItemStack patternStack, Level level) {
        if (patternStack == null || patternStack.isEmpty() ||
                !(PatternDetailsHelper.decodePattern(patternStack, level) instanceof AEProcessingPattern pattern)) {
            return -1;
        }
        return extractCircuitFrom(pattern);
    }

    /**
     * @param replaceExisting when {@code false} a pattern that already embeds a circuit is returned
     *                        unchanged (matches GTLCore's {@code skipExistingCircuitPatterns}).
     * @return the rewritten pattern, or {@code ItemStack.EMPTY} when the stack is not a processing
     *         pattern.
     */
    public static ItemStack withCircuit(ItemStack patternStack, int circuitConfig, boolean replaceExisting,
                                        Level level) {
        if (circuitConfig < 1 || circuitConfig > IntCircuitBehaviour.CIRCUIT_MAX) {
            return patternStack;
        }
        if (patternStack == null || patternStack.isEmpty() ||
                !(PatternDetailsHelper.decodePattern(patternStack, level) instanceof AEProcessingPattern pattern)) {
            return ItemStack.EMPTY;
        }

        List<GenericStack> inputs = new ArrayList<>(inputsOf(pattern));
        boolean hasCircuit = inputs.removeIf(GTNAPatternCircuitHelper::isCircuitStack);
        if (hasCircuit && !replaceExisting) {
            return patternStack;
        }
        inputs.add(0, GenericStack.fromItemStack(IntCircuitBehaviour.stack(circuitConfig)));
        return PatternDetailsHelper.encodeProcessingPattern(
                inputs.toArray(new GenericStack[0]), pattern.getSparseOutputs());
    }

    /** @return a copy of the pattern with any embedded circuit removed; unchanged when it has none. */
    public static ItemStack withoutCircuit(ItemStack patternStack, Level level) {
        if (patternStack == null || patternStack.isEmpty() ||
                !(PatternDetailsHelper.decodePattern(patternStack, level) instanceof AEProcessingPattern pattern)) {
            return ItemStack.EMPTY;
        }
        List<GenericStack> inputs = new ArrayList<>(inputsOf(pattern));
        if (!inputs.removeIf(GTNAPatternCircuitHelper::isCircuitStack)) {
            return patternStack;
        }
        return PatternDetailsHelper.encodeProcessingPattern(
                inputs.toArray(new GenericStack[0]), pattern.getSparseOutputs());
    }

    private static int extractCircuitFrom(AEProcessingPattern pattern) {
        for (GenericStack input : inputsOf(pattern)) {
            if (input.what() instanceof AEItemKey key) {
                ItemStack stack = key.toStack();
                if (IntCircuitBehaviour.isIntegratedCircuit(stack)) {
                    return IntCircuitBehaviour.getCircuitConfiguration(stack);
                }
            }
        }
        return -1;
    }

    private static boolean isCircuitStack(GenericStack stack) {
        if (stack == null || !(stack.what() instanceof AEItemKey key)) {
            return false;
        }
        // GTM 7.5.3 has no single INTEGRATED_CIRCUIT item: each tier has its own, so detect via
        // the behaviour helper instead of an item identity check (GTLCore used a fork field).
        return IntCircuitBehaviour.isIntegratedCircuit(key.toStack());
    }

    private static List<GenericStack> inputsOf(AEProcessingPattern pattern) {
        return Arrays.stream(pattern.getSparseInputs()).filter(Objects::nonNull).toList();
    }
}
