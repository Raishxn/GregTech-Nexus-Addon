package com.raishxn.gtna.research;

import net.minecraft.resources.ResourceLocation;

import java.util.*;
import java.util.function.Predicate;

/**
 * An immutable, validated set of research nodes. {@link #build} drops nodes whose prerequisites are
 * missing or that sit on a cycle, so every node a caller sees can actually be reached.
 */
public final class KnowledgeGraph {

    public static final KnowledgeGraph EMPTY = new KnowledgeGraph(new LinkedHashMap<>());

    /** The validated graph plus a human-readable line for every node that was dropped. */
    public record Build(KnowledgeGraph graph, List<String> problems) {}

    private final Map<ResourceLocation, KnowledgeNode> nodes;
    private final Map<ResourceLocation, Set<ResourceLocation>> recipeGates = new HashMap<>();
    private final Map<ResourceLocation, List<KnowledgeNode>> byItemTrigger = new HashMap<>();

    private KnowledgeGraph(Map<ResourceLocation, KnowledgeNode> ordered) {
        this.nodes = Collections.unmodifiableMap(ordered);
        for (KnowledgeNode node : ordered.values()) {
            for (KnowledgeGrant grant : node.grants()) {
                if (grant instanceof KnowledgeGrant.Recipes recipes) {
                    for (ResourceLocation recipe : recipes.recipes()) {
                        recipeGates.computeIfAbsent(recipe, key -> new LinkedHashSet<>()).add(node.id());
                    }
                }
            }
            if (node.trigger() instanceof KnowledgeTrigger.ObtainItem obtain) {
                byItemTrigger.computeIfAbsent(obtain.item(), key -> new ArrayList<>()).add(node);
            }
        }
    }

    public static Build build(Collection<KnowledgeNode> candidates) {
        List<String> problems = new ArrayList<>();
        Map<ResourceLocation, KnowledgeNode> remaining = new LinkedHashMap<>();
        candidates.stream().sorted(Comparator.comparing(KnowledgeNode::id)).forEach(node -> {
            if (node.tier() < 1) {
                problems.add(node.id() + ": tier must be at least 1, got " + node.tier());
            } else {
                remaining.put(node.id(), node);
            }
        });

        // Drop nodes with unknown prerequisites, repeatedly: removing one can orphan its dependants.
        boolean changed = true;
        while (changed) {
            changed = false;
            for (Iterator<KnowledgeNode> it = remaining.values().iterator(); it.hasNext();) {
                KnowledgeNode node = it.next();
                for (ResourceLocation prerequisite : node.prerequisites()) {
                    if (!remaining.containsKey(prerequisite)) {
                        problems.add(node.id() + ": missing prerequisite " + prerequisite);
                        it.remove();
                        changed = true;
                        break;
                    }
                }
            }
        }

        // Kahn's algorithm: whatever cannot be ordered sits on, or behind, a cycle.
        Map<ResourceLocation, Integer> missing = new HashMap<>();
        Map<ResourceLocation, List<ResourceLocation>> dependants = new HashMap<>();
        for (KnowledgeNode node : remaining.values()) {
            missing.put(node.id(), new HashSet<>(node.prerequisites()).size());
            for (ResourceLocation prerequisite : new HashSet<>(node.prerequisites())) {
                dependants.computeIfAbsent(prerequisite, key -> new ArrayList<>()).add(node.id());
            }
        }
        Deque<ResourceLocation> ready = new ArrayDeque<>();
        remaining.keySet().stream().filter(id -> missing.get(id) == 0).forEach(ready::add);
        List<ResourceLocation> order = new ArrayList<>();
        while (!ready.isEmpty()) {
            ResourceLocation id = ready.poll();
            order.add(id);
            for (ResourceLocation dependant : dependants.getOrDefault(id, List.of())) {
                if (missing.merge(dependant, -1, Integer::sum) == 0) ready.add(dependant);
            }
        }
        for (ResourceLocation id : remaining.keySet()) {
            if (!order.contains(id)) problems.add(id + ": prerequisite cycle");
        }

        // Order by tier, then by dependency order, then id, so lists and the UI are stable.
        Map<ResourceLocation, Integer> position = new HashMap<>();
        for (int i = 0; i < order.size(); i++) position.put(order.get(i), i);
        Map<ResourceLocation, KnowledgeNode> ordered = new LinkedHashMap<>();
        order.stream().map(remaining::get)
                .sorted(Comparator.comparingInt(KnowledgeNode::tier)
                        .thenComparingInt(node -> position.get(node.id()))
                        .thenComparing(KnowledgeNode::id))
                .forEach(node -> ordered.put(node.id(), node));
        return new Build(new KnowledgeGraph(ordered), List.copyOf(problems));
    }

    public Optional<KnowledgeNode> get(ResourceLocation id) {
        return Optional.ofNullable(nodes.get(id));
    }

    public Collection<KnowledgeNode> nodes() {
        return nodes.values();
    }

    public int size() {
        return nodes.size();
    }

    public boolean isEmpty() {
        return nodes.isEmpty();
    }

    public boolean hasItemTriggers() {
        return !byItemTrigger.isEmpty();
    }

    public boolean prerequisitesMet(KnowledgeNode node, Predicate<ResourceLocation> unlocked) {
        return missingPrerequisites(node, unlocked).isEmpty();
    }

    public List<ResourceLocation> missingPrerequisites(KnowledgeNode node, Predicate<ResourceLocation> unlocked) {
        return node.prerequisites().stream().filter(unlocked.negate()).toList();
    }

    /** Nodes that gate the recipe. A recipe with several gates needs all of them. */
    public Set<ResourceLocation> gatesFor(ResourceLocation recipeId) {
        return Collections.unmodifiableSet(recipeGates.getOrDefault(recipeId, Set.of()));
    }

    /** Nodes whose trigger is obtaining this item. */
    public List<KnowledgeNode> triggeredByItem(ResourceLocation item) {
        return Collections.unmodifiableList(byItemTrigger.getOrDefault(item, List.of()));
    }

    /** The node and every transitive prerequisite, prerequisites first. */
    public List<KnowledgeNode> closure(ResourceLocation id) {
        LinkedHashMap<ResourceLocation, KnowledgeNode> out = new LinkedHashMap<>();
        collect(id, out);
        return List.copyOf(out.values());
    }

    private void collect(ResourceLocation id, LinkedHashMap<ResourceLocation, KnowledgeNode> out) {
        KnowledgeNode node = nodes.get(id);
        if (node == null || out.containsKey(id)) return;
        for (ResourceLocation prerequisite : node.prerequisites()) collect(prerequisite, out);
        out.put(id, node);
    }
}
