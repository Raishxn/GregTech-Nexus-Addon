package com.raishxn.gtna.mixin.gtceu;

import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Exposes the matcher geometry for an anchored structure diagnostic. */
@Mixin(BlockPattern.class)
public interface BlockPatternAccessor {

    @Accessor(value = "blockMatches", remap = false)
    TraceabilityPredicate[][][] gtna$getBlockMatches();

    @Accessor(value = "centerOffset", remap = false)
    int[] gtna$getCenterOffset();

    @Invoker(value = "setActualRelativeOffset", remap = false)
    BlockPos gtna$actualRelativeOffset(int x, int y, int z, Direction facing, Direction upwardsFacing,
                                       boolean flipped);
}
