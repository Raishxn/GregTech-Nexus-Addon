package com.raishxn.gtna.integration.ae2.pattern;

import net.minecraft.world.level.Level;

import appeng.api.crafting.IPatternDetails;
import org.jetbrains.annotations.Nullable;

public interface IParallelPatternDetails extends IPatternDetails {

    IPatternDetails getDelegate();

    IParallelPatternDetails copy(long parallelCount, Level level);

    IParallelPatternDetails getCopy();

    void parallel(long parallelCount);

    long getParallel();

    static @Nullable IParallelPatternDetails of(@Nullable IPatternDetails details, @Nullable Level level,
                                                long parallelCount) {
        if (details == null || level == null) {
            return null;
        }
        if (details instanceof IParallelPatternDetails parallelDetails) {
            parallelDetails.parallel(parallelCount);
            return parallelDetails;
        }
        return new ParallelPatternDetails(details, level, parallelCount);
    }
}
