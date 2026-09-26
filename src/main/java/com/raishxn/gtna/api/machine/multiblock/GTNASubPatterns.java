package com.raishxn.gtna.api.machine.multiblock;

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
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
    private static final Map<ResourceLocation, List<String>> KUBE_DESCRIPTIONS = new HashMap<>();
    private static final Map<ResourceLocation, List<String>> CLIENT_KUBE_DESCRIPTIONS = new HashMap<>();
    private static final Map<Function<MultiblockMachineDefinition, BlockPattern>, Performance> KUBE_PERFORMANCE = new IdentityHashMap<>();
    private static final Map<BlockPattern, Performance> BUILT_PERFORMANCE = new IdentityHashMap<>();

    public record Performance(double speedBonus, boolean perfectOverclock) {}

    private GTNASubPatterns() {}

    /** Registers one extension structure for the machine {@code machineId}. */
    public static void register(ResourceLocation machineId,
                                Function<MultiblockMachineDefinition, BlockPattern> factory) {
        register(machineId, factory, new Component[0]);
    }

    /** Registers a server-script extension so it can be replaced on the next server start. */
    public static void registerKubeJS(ResourceLocation machineId,
                                      Function<MultiblockMachineDefinition, BlockPattern> factory,
                                      String descriptionKey) {
        registerKubeJS(machineId, factory, descriptionKey, 1.0, false);
    }

    public static void registerKubeJS(ResourceLocation machineId,
                                      Function<MultiblockMachineDefinition, BlockPattern> factory,
                                      String descriptionKey, double speedBonus, boolean perfectOverclock) {
        if (!Double.isFinite(speedBonus) || speedBonus < 1.0) {
            throw new IllegalArgumentException("Module speed bonus must be finite and at least 1.0");
        }
        KUBE_FACTORIES.computeIfAbsent(machineId, id -> new ArrayList<>()).add(factory);
        KUBE_DESCRIPTIONS.computeIfAbsent(machineId, id -> new ArrayList<>()).add(descriptionKey);
        KUBE_PERFORMANCE.put(factory, new Performance(speedBonus, perfectOverclock));
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
        KUBE_DESCRIPTIONS.clear();
        KUBE_PERFORMANCE.clear();
        BUILT_PERFORMANCE.clear();
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
                    Performance performance = KUBE_PERFORMANCE.get(factory);
                    if (performance != null) BUILT_PERFORMANCE.put(pattern, performance);
                }
            }
            return patterns;
        });
    }

    public static Performance performance(BlockPattern pattern) {
        return BUILT_PERFORMANCE.get(pattern);
    }

    /** Tooltip lines describing the modules of {@code definition}, or an empty list. */
    public static List<Component> getTooltips(MultiblockMachineDefinition definition) {
        ResourceLocation id = definition.getId();
        List<Component> result = new ArrayList<>(TOOLTIPS.getOrDefault(id, List.of()));
        List<String> descriptions = CLIENT_KUBE_DESCRIPTIONS.getOrDefault(id,
                KUBE_DESCRIPTIONS.getOrDefault(id, List.of()));
        for (String description : descriptions) {
            result.add(Component.translatable("gtna.machine.auxiliary_module").withStyle(ChatFormatting.GOLD));
            result.add(Component.translatable(description));
        }
        return result;
    }

    public static Map<ResourceLocation, List<String>> kubeDescriptions() {
        Map<ResourceLocation, List<String>> copy = new HashMap<>();
        KUBE_DESCRIPTIONS.forEach((id, descriptions) -> copy.put(id, List.copyOf(descriptions)));
        return copy;
    }

    public static void setClientKubeDescriptions(Map<ResourceLocation, List<String>> descriptions) {
        CLIENT_KUBE_DESCRIPTIONS.clear();
        CLIENT_KUBE_DESCRIPTIONS.putAll(descriptions);
    }
}
