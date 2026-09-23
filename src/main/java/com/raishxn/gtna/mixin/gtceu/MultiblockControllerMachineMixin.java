package com.raishxn.gtna.mixin.gtceu;

import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.MultiblockState;
import com.gregtechceu.gtceu.api.pattern.util.PatternMatchContext;

import com.raishxn.gtna.api.machine.multiblock.GTNASubPatterns;
import com.raishxn.gtna.api.machine.multiblock.IGTNAModuleHost;
import com.raishxn.gtna.api.machine.multiblock.ISubPatternMachine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
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
 * default) and, if that matches, checks every extension structure at the same controller and merges
 * the parts it detects. That lets a module attached to a machine unlock new abilities (e.g. Parallel
 * or Accelerate hatches).
 *
 * <p>
 * It also makes every multiblock an {@link IGTNAModuleHost}, exposing how many modules matched, which
 * the multiblock UI renders as "Formed modules: n / total".
 *
 * <p>
 * Checking a sub-pattern resets the shared {@link MultiblockState}, so the main match context is
 * snapshotted and restored around the sub-pattern checks.
 */
@Mixin(MultiblockControllerMachine.class)
public abstract class MultiblockControllerMachineMixin implements IGTNAModuleHost {

    @Unique
    private int gtna$formedModuleCount = 0;

    @Override
    public int gtna$formedModuleCount() {
        return gtna$formedModuleCount;
    }

    @Override
    public void gtna$setFormedModuleCount(int count) {
        gtna$formedModuleCount = count;
    }

    public boolean checkPattern() {
        MultiblockControllerMachine self = (MultiblockControllerMachine) (Object) this;
        BlockPattern pattern = self.getPattern();
        MultiblockState state = self.getMultiblockState();
        if (pattern == null || !pattern.checkPatternAt(state, false)) {
            gtna$formedModuleCount = 0;
            return false;
        }
        List<BlockPattern> subPatterns = new ArrayList<>();
        if (self instanceof ISubPatternMachine host) {
            List<BlockPattern> fromMachine = host.gtna$getSubPatterns();
            if (fromMachine != null) {
                subPatterns.addAll(fromMachine);
            }
        }
        subPatterns.addAll(GTNASubPatterns.get(self.getDefinition()));
        if (subPatterns.isEmpty()) {
            gtna$formedModuleCount = 0;
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

        int matched = 0;
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
                matched++;
            }
        }
        gtna$formedModuleCount = matched;

        // Restore the main context and, when a module matched, add its parts.
        context.reset();
        for (Map.Entry<String, Object> entry : snapshot.entrySet()) {
            context.set(entry.getKey(), entry.getValue());
        }
        if (matched > 0) {
            context.set("parts", parts);
        }
        return true;
    }
}
