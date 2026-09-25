package com.raishxn.gtna.api.machine.multiblock;

import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.MultiblockState;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

import com.raishxn.gtna.mixin.gtceu.BlockPatternAccessor;

import java.util.List;

/** Finds a real mismatched cell at the controller anchor instead of the matcher's last search probe. */
public final class GTNAPatternDiagnostics {

    private GTNAPatternDiagnostics() {}

    public record Mismatch(BlockPos pos, ItemStack expected) {}

    public static Mismatch firstMismatch(MultiblockControllerMachine controller, BlockPattern pattern) {
        // Repeatable aisles need the matcher's search/repetition logic, so retain its own error there.
        for (int[] repetition : pattern.aisleRepetitions) {
            if (repetition[0] != 1 || repetition[1] != 1) return null;
        }
        MismatchResult normal = scan(controller, pattern, false);
        if (!controller.self().allowFlip()) return normal.first();
        MismatchResult flipped = scan(controller, pattern, true);
        return flipped.count() < normal.count() ? flipped.first() : normal.first();
    }

    private static MismatchResult scan(MultiblockControllerMachine controller, BlockPattern pattern, boolean flipped) {
        BlockPatternAccessor accessor = (BlockPatternAccessor) pattern;
        TraceabilityPredicate[][][] cells = accessor.gtna$getBlockMatches();
        int[] center = accessor.gtna$getCenterOffset();
        Direction facing = controller.self().getFrontFacing();
        Direction up = controller.self().getUpwardsFacing();
        BlockPos origin = controller.self().getPos();
        MultiblockState state = new MultiblockState(controller.getLevel(), origin);
        state.clean();
        Mismatch first = null;
        int count = 0;
        for (int aisle = 0; aisle < cells.length; aisle++) {
            for (int row = 0; row < cells[aisle].length; row++) {
                for (int column = 0; column < cells[aisle][row].length; column++) {
                    TraceabilityPredicate predicate = cells[aisle][row][column];
                    BlockPos pos = origin.offset(accessor.gtna$actualRelativeOffset(column - center[0],
                            row - center[1], aisle - center[2], facing, up, flipped));
                    if (!state.update(pos, predicate)) continue;
                    if (!predicate.test(state)) {
                        count++;
                        if (first == null) {
                            ItemStack expected = state.error == null ? ItemStack.EMPTY :
                                    state.error.getCandidates().stream().flatMap(List::stream)
                                            .filter(stack -> !stack.isEmpty()).findFirst().orElse(ItemStack.EMPTY);
                            first = new Mismatch(pos, expected);
                        }
                    }
                }
            }
        }
        return new MismatchResult(first, count);
    }

    private record MismatchResult(Mismatch first, int count) {}
}
