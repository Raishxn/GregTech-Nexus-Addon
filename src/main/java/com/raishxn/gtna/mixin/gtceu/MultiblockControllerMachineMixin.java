package com.raishxn.gtna.mixin.gtceu;

import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.MultiblockState;
import com.gregtechceu.gtceu.api.pattern.error.PatternError;
import com.gregtechceu.gtceu.api.pattern.util.PatternMatchContext;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.network.PacketDistributor;

import com.raishxn.gtna.api.machine.multiblock.GTNAPartAbility;
import com.raishxn.gtna.api.machine.multiblock.GTNASubPatterns;
import com.raishxn.gtna.api.machine.multiblock.IGTNAModuleHost;
import com.raishxn.gtna.api.machine.multiblock.IGTNAModulePerformanceHost;
import com.raishxn.gtna.api.machine.multiblock.ISubPatternMachine;
import com.raishxn.gtna.network.GTNANetworkHandler;
import com.raishxn.gtna.network.packet.SModuleCountPacket;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
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
public abstract class MultiblockControllerMachineMixin implements IGTNAModuleHost, IGTNAModulePerformanceHost {

    @Unique
    private static final List<PartAbility> GTNA$SINGLE_PER_CONTROLLER = List.of(
            PartAbility.PARALLEL_HATCH,
            GTNAPartAbility.ACCELERATE_HATCH,
            GTNAPartAbility.THREAD_HATCH,
            GTNAPartAbility.OVERCLOCK_HATCH,
            GTNAPartAbility.OUTPUT_BOOST_HATCH);

    @Unique
    private volatile int gtna$formedModuleCount = 0;
    @Unique
    private double gtna$moduleSpeedBonus = 1.0;
    @Unique
    private boolean gtna$modulePerfectOverclock;

    @Override
    public double gtna$getModuleSpeedBonus() {
        return gtna$moduleSpeedBonus;
    }

    @Override
    public boolean gtna$hasModulePerfectOverclock() {
        return gtna$modulePerfectOverclock;
    }

    @Override
    public void gtna$setModulePerformance(double speedBonus, boolean perfectOverclock) {
        gtna$moduleSpeedBonus = speedBonus;
        gtna$modulePerfectOverclock = perfectOverclock;
    }

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
            gtna$setModuleCount(self, 0);
            gtna$setModulePerformance(1.0, false);
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
            gtna$setModuleCount(self, 0);
            gtna$setModulePerformance(1.0, false);
            return true;
        }

        PatternMatchContext context = state.getMatchContext();
        // Snapshot the main pattern's match context; a sub-pattern check resets it.
        Map<String, Object> snapshot = new HashMap<>();
        for (Map.Entry<String, Object> entry : context.entrySet()) {
            snapshot.put(entry.getKey(), entry.getValue());
        }
        // Each sub-pattern check calls MultiblockState.clean(), replacing the position cache. Keep
        // the main positions and every module position reached, including the first missing block.
        // GTCEu uses this cache to dispatch block changes to formed controllers.
        LongOpenHashSet positionCache = new LongOpenHashSet(state.cache);
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
        double speedBonus = 1.0;
        boolean perfectOverclock = false;
        for (BlockPattern sub : subPatterns) {
            if (sub == null) {
                continue;
            }
            // A failed match only caches cells up to its first mismatch. Watch the complete
            // extension volume so repairing or breaking any other module block retriggers it.
            gtna$cacheModuleVolume(self, state, sub, positionCache);
            if (sub.checkPatternAt(state, false)) {
                Object subParts = state.getMatchContext().get("parts");
                Set<IMultiPart> candidateParts = new HashSet<>(parts);
                if (subParts instanceof Set<?> set) {
                    for (Object value : set) {
                        if (value instanceof IMultiPart part) {
                            candidateParts.add(part);
                        }
                    }
                }
                if (!gtna$hasDuplicatePerformanceHatch(candidateParts)) {
                    parts = candidateParts;
                    matched++;
                    var performance = GTNASubPatterns.performance(sub);
                    if (performance != null) {
                        speedBonus *= performance.speedBonus();
                        perfectOverclock |= performance.perfectOverclock();
                    }
                }
            } else {
                // The sub-pattern stopped at its first mismatching cell. That cell is NOT added to
                // MultiblockState.cache by BlockPattern (the update fails before addPosCache), so the
                // controller would never be notified when the player places the missing block back.
                // Register the failing position explicitly, so the machine re-detects the module
                // without a manual structure refresh.
                PatternError error = state.error;
                BlockPos failed = error == null ? null : error.getPos();
                if (failed != null) {
                    positionCache.add(failed.asLong());
                }
            }
            positionCache.addAll(state.cache);
        }
        gtna$setModuleCount(self, matched);
        gtna$setModulePerformance(Math.min(speedBonus, 1024.0), perfectOverclock);

        // Restore the main context and, when a module matched, add its parts.
        context.reset();
        for (Map.Entry<String, Object> entry : snapshot.entrySet()) {
            context.set(entry.getKey(), entry.getValue());
        }
        state.cache = positionCache;
        if (matched > 0) {
            context.set("parts", parts);
        }
        return true;
    }

    @Unique
    private static void gtna$cacheModuleVolume(MultiblockControllerMachine self, MultiblockState state,
                                               BlockPattern pattern, LongOpenHashSet cache) {
        BlockPatternAccessor geometry = (BlockPatternAccessor) pattern;
        int[] center = geometry.gtna$getCenterOffset();
        int[] dimensions = pattern.getDimensions();
        // The z bounds in centerOffset delimit where a repeatable first aisle may start;
        // they do not describe the full pattern depth. Walk every aisle from its anchor.
        for (int z = -center[4]; z < dimensions[0] - center[4]; z++) {
            for (int y = -center[1]; y < dimensions[1] - center[1]; y++) {
                for (int x = -center[0]; x < dimensions[2] - center[0]; x++) {
                    BlockPos relative = geometry.gtna$actualRelativeOffset(x, y, z,
                            self.getFrontFacing(), self.getUpwardsFacing(), state.isNeededFlip());
                    cache.add(self.getPos().offset(relative).asLong());
                }
            }
        }
    }

    @Unique
    private void gtna$setModuleCount(MultiblockControllerMachine self, int count) {
        int previous = gtna$formedModuleCount;
        gtna$formedModuleCount = count;
        if (previous == count || !(self.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }
        // GTCEu may check a multiblock from its async pattern thread. Looking up a chunk or
        // sending a tracking packet there can block on the server thread while the pattern lock
        // is still held, deadlocking the next structure update. Dispatch only the visual sync.
        serverLevel.getServer().execute(() -> {
            if (gtna$formedModuleCount != count) {
                return;
            }
            LevelChunk chunk = serverLevel.getChunkAt(self.getPos());
            GTNANetworkHandler.CHANNEL.send(PacketDistributor.TRACKING_CHUNK.with(() -> chunk),
                    new SModuleCountPacket(self.getPos(), count));
        });
    }

    @Unique
    private static boolean gtna$hasDuplicatePerformanceHatch(Set<IMultiPart> parts) {
        for (PartAbility ability : GTNA$SINGLE_PER_CONTROLLER) {
            int count = 0;
            for (IMultiPart part : parts) {
                if (ability.isApplicable(part.self().getBlockState().getBlock()) && ++count > 1) {
                    return true;
                }
            }
        }
        return false;
    }
}
