package com.raishxn.gtna.mixin.gtceu;

import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.MultiblockState;
import com.gregtechceu.gtceu.api.pattern.util.PatternMatchContext;

import com.raishxn.gtna.api.machine.multiblock.ISubPatternMachine;
import org.spongepowered.asm.mixin.Mixin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Adds {@link ISubPatternMachine} support to GTCEu's multiblock pattern check.
 *
 * <p>
 * This mixin adds a {@code checkPattern()} method to {@link MultiblockControllerMachine}, overriding
 * the {@code IMultiController} default. It runs the controller's main pattern first (exactly like the
 * default) and, if that matches and the machine implements {@link ISubPatternMachine}, checks every
 * extension structure at the same controller and merges the parts it detects. That lets a module
 * attached to a machine unlock new abilities (e.g. Parallel or Accelerate hatches).
 *
 * <p>
 * Checking a sub-pattern resets the shared {@link MultiblockState}, so the main match context is
 * snapshotted and restored around the sub-pattern checks.
 */
@Mixin(MultiblockControllerMachine.class)
public abstract class MultiblockControllerMachineMixin {

    public boolean checkPattern() {
        MultiblockControllerMachine self = (MultiblockControllerMachine) (Object) this;
        BlockPattern pattern = self.getPattern();
        MultiblockState state = self.getMultiblockState();
        if (pattern == null || !pattern.checkPatternAt(state, false)) {
            return false;
        }
        if (!(self instanceof ISubPatternMachine host)) {
            return true;
        }
        List<BlockPattern> subPatterns = host.gtna$getSubPatterns();
        if (subPatterns == null || subPatterns.isEmpty()) {
            return true;
        }

        PatternMatchContext context = state.getMatchContext();
        // Snapshot the main pattern's match context; a sub-pattern check resets it.
        Map<String, Object> snapshot = new HashMap<>();
        for (Map.Entry<String, Object> entry : context.entrySet()) {
            snapshot.put(entry.getKey(), entry.getValue());
        }
        Set<IMultiPart> parts = new HashSet<>();
        Object mainParts = snapshot.get("parts");
        if (mainParts instanceof Set<?> set) {
            for (Object value : set) {
                if (value instanceof IMultiPart part) {
                    parts.add(part);
                }
            }
        }

        boolean matchedAny = false;
        for (BlockPattern sub : subPatterns) {
            if (sub == null) {
                continue;
            }
            if (sub.checkPatternAt(state, false)) {
                Object subParts = state.getMatchContext().get("parts");
                if (subParts instanceof Set<?> set) {
                    for (Object value : set) {
                        if (value instanceof IMultiPart part) {
                            parts.add(part);
                        }
                    }
                }
                matchedAny = true;
            }
        }

        // Restore the main context and, when a module matched, add its parts.
        context.reset();
        for (Map.Entry<String, Object> entry : snapshot.entrySet()) {
            context.set(entry.getKey(), entry.getValue());
        }
        if (matchedAny) {
            context.set("parts", parts);
        }
        return true;
    }
}
