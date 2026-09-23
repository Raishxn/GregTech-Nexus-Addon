package com.raishxn.gtna.api.machine.multiblock;

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;

import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Registry of extension structures ("sub-patterns" / modules) attached to multiblocks.
 *
 * <p>
 * Java machines can also declare their extensions through {@link ISubPatternMachine}; this registry is
 * what lets <b>KubeJS</b> (and datapacks, via the KubeJS event) add extensions to any multiblock —
 * new or already registered — without touching its Java class. The
 * {@code MultiblockControllerMachineMixin} reads both sources when it checks a controller's pattern
 * and merges the parts found by every matching extension, so a module can unlock new abilities
 * (Parallel / Accelerate hatches, extra IO, ...).
 *
 * <p>
 * This class is safe to load without KubeJS on the classpath: it only stores pattern factories.
 */
public final class GTNASubPatterns {

    private static final Map<ResourceLocation, List<Function<MultiblockMachineDefinition, BlockPattern>>> FACTORIES = new HashMap<>();
    private static final Map<ResourceLocation, List<BlockPattern>> CACHE = new HashMap<>();

    private GTNASubPatterns() {}

    /** Registers one extension structure for the machine {@code machineId}. */
    public static void register(ResourceLocation machineId,
                                Function<MultiblockMachineDefinition, BlockPattern> factory) {
        FACTORIES.computeIfAbsent(machineId, id -> new ArrayList<>()).add(factory);
        CACHE.remove(machineId);
    }

    /** The built extension structures for {@code definition} (cached), or an empty list. */
    public static List<BlockPattern> get(MultiblockMachineDefinition definition) {
        ResourceLocation id = definition.getId();
        List<Function<MultiblockMachineDefinition, BlockPattern>> factories = FACTORIES.get(id);
        if (factories == null || factories.isEmpty()) {
            return List.of();
        }
        return CACHE.computeIfAbsent(id, key -> {
            List<BlockPattern> patterns = new ArrayList<>(factories.size());
            for (Function<MultiblockMachineDefinition, BlockPattern> factory : factories) {
                BlockPattern pattern = factory.apply(definition);
                if (pattern != null) {
                    patterns.add(pattern);
                }
            }
            return patterns;
        });
    }
}
