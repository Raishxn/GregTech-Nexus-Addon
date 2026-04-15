package com.raishxn.gtna.integration.ae2.pattern;

import net.minecraft.world.level.Level;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;

import java.util.Arrays;

final class ParallelPatternDetails implements IParallelPatternDetails {

    private final IPatternDetails delegate;
    private final Level level;
    private long parallel;
    private IPatternDetails.IInput[] inputs;
    private GenericStack[] outputs;

    ParallelPatternDetails(IPatternDetails delegate, Level level, long parallel) {
        this.delegate = delegate;
        this.level = level;
        this.parallel = Math.max(1L, parallel);
        rebuild();
    }

    private void rebuild() {
        this.inputs = Arrays.stream(delegate.getInputs())
                .map(input -> new ParallelInput(input, parallel))
                .toArray(IPatternDetails.IInput[]::new);
        this.outputs = scaleStacks(delegate.getOutputs(), parallel);
    }

    @Override
    public IParallelPatternDetails copy(long parallelCount, Level level) {
        return new ParallelPatternDetails(delegate, level, parallelCount);
    }

    @Override
    public IParallelPatternDetails getCopy() {
        return copy(parallel, level);
    }

    @Override
    public void parallel(long parallelCount) {
        this.parallel = Math.max(1L, parallelCount);
        rebuild();
    }

    @Override
    public long getParallel() {
        return parallel;
    }

    @Override
    public AEItemKey getDefinition() {
        return delegate.getDefinition();
    }

    @Override
    public IPatternDetails.IInput[] getInputs() {
        return inputs;
    }

    @Override
    public GenericStack[] getOutputs() {
        return outputs;
    }

    @Override
    public boolean supportsPushInputsToExternalInventory() {
        return delegate.supportsPushInputsToExternalInventory();
    }

    @Override
    public void pushInputsToExternalInventory(KeyCounter[] inputHolder, PatternInputSink sink) {
        delegate.pushInputsToExternalInventory(inputHolder, sink);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof IPatternDetails other)) return false;
        return delegate.getDefinition().equals(other.getDefinition());
    }

    @Override
    public int hashCode() {
        return delegate.getDefinition().hashCode();
    }

    private static GenericStack[] scaleStacks(GenericStack[] source, long multiplier) {
        GenericStack[] scaled = new GenericStack[source.length];
        for (int i = 0; i < source.length; i++) {
            GenericStack stack = source[i];
            scaled[i] = stack == null ? null : new GenericStack(stack.what(), safeMultiply(stack.amount(), multiplier));
        }
        return scaled;
    }

    private static long safeMultiply(long left, long right) {
        if (left <= 0L || right <= 0L) {
            return 0L;
        }
        if (left > Long.MAX_VALUE / right) {
            return Long.MAX_VALUE;
        }
        return left * right;
    }

    private static final class ParallelInput implements IPatternDetails.IInput {

        private final IPatternDetails.IInput delegate;
        private final long parallel;
        private final GenericStack[] possibleInputs;

        private ParallelInput(IPatternDetails.IInput delegate, long parallel) {
            this.delegate = delegate;
            this.parallel = Math.max(1L, parallel);
            this.possibleInputs = delegate.getPossibleInputs();
        }

        @Override
        public GenericStack[] getPossibleInputs() {
            return possibleInputs;
        }

        @Override
        public long getMultiplier() {
            return safeMultiply(delegate.getMultiplier(), parallel);
        }

        @Override
        public boolean isValid(AEKey input, net.minecraft.world.level.Level level) {
            return delegate.isValid(input, level);
        }

        @Override
        public AEKey getRemainingKey(AEKey templateInput) {
            return delegate.getRemainingKey(templateInput);
        }
    }
}
