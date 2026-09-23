package com.raishxn.gtna.api.machine.multiblock;

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;

import net.minecraft.network.chat.Component;
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
 * A registration can carry tooltip lines describing what the module unlocks; they are appended to the
 * machine item's tooltip by {@code MetaMachineBlockMixin}.
 *
 * <p>
 * This class is safe to load without KubeJS on the classpath: it only stores pattern factories.
 */
public final class GTNASubPatterns {

    private static final Map<ResourceLocation, List<Function<MultiblockMachineDefinition, BlockPattern>>> FACTORIES = new HashMap<>();
    private static final Map<ResourceLocation, List<Function<MultiblockMachineDefinition, BlockPattern>>> KUBE_FACTORIES = new HashMap<>();
    private static final Map<ResourceLocation, List<BlockPattern>> CACHE = new HashMap<>();
    private static final Map<ResourceLocation, List<Component>> TOOLTIPS = new HashMap<>();

    private GTNASubPatterns() {}

    /** Registers one extension structure for the machine {@code machineId}. */
    public static void register(ResourceLocation machineId,
                                Function<MultiblockMachineDefinition, BlockPattern> factory) {
        register(machineId, factory, new Component[0]);
    }

    /** Registers a server-script extension so it can be replaced on the next server start. */
    public static void registerKubeJS(ResourceLocation machineId,
                                      Function<MultiblockMachineDefinition, BlockPattern> factory) {
        KUBE_FACTORIES.computeIfAbsent(machineId, id -> new ArrayList<>()).add(factory);
        register(machineId, factory);
    }

    public static void clearKubeJS() {
        KUBE_FACTORIES.forEach((id, factories) -> {
            List<Function<MultiblockMachineDefinition, BlockPattern>> registered = FACTORIES.get(id);
            if (registered != null) {
                registered.removeAll(factories);
                if (registered.isEmpty()) FACTORIES.remove(id);
            }
            CACHE.remove(id);
        });
        KUBE_FACTORIES.clear();
    }

    /**
     * Registers one extension structure for the machine {@code machineId}, plus tooltip lines
     * describing what the module unlocks (shown on the machine item).
     */
    public static void register(ResourceLocation machineId,
                                Function<MultiblockMachineDefinition, BlockPattern> factory,
                                Component... tooltips) {
        FACTORIES.computeIfAbsent(machineId, id -> new ArrayList<>()).add(factory);
        CACHE.remove(machineId);
        if (tooltips.length > 0) {
            TOOLTIPS.computeIfAbsent(machineId, id -> new ArrayList<>()).addAll(List.of(tooltips));
        }
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

    /** Tooltip lines describing the modules of {@code definition}, or an empty list. */
    public static List<Component> getTooltips(MultiblockMachineDefinition definition) {
        return TOOLTIPS.getOrDefault(definition.getId(), List.of());
    }
}
