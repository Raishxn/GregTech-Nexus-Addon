package com.raishxn.gtna.research;

import net.minecraft.resources.ResourceLocation;

import java.util.*;

/**
 * What the client knows about research: the nodes the server sent and which ones the player's scope
 * has unlocked. Plain data, safe in common code; only ever filled on the client.
 */
public final class ClientKnowledge {

    /** One node as the server describes it to the client. */
    public record NodeView(ResourceLocation id, int tier, List<ResourceLocation> prerequisites,
                           Optional<ResourceLocation> triggerItem, Optional<ResourceLocation> icon,
                           List<ResourceLocation> recipes, Map<ResourceLocation, Integer> cost,
                           KnowledgeNode.Kind kind, Optional<KnowledgeNode.Eureka> eureka) {

        public NodeView {
            prerequisites = List.copyOf(prerequisites);
            recipes = List.copyOf(recipes);
            cost = Collections.unmodifiableMap(new LinkedHashMap<>(cost));
        }

        /** A free trunk node, as every node was before research points existed. */
        public NodeView(ResourceLocation id, int tier, List<ResourceLocation> prerequisites,
                        Optional<ResourceLocation> triggerItem, Optional<ResourceLocation> icon,
                        List<ResourceLocation> recipes) {
            this(id, tier, prerequisites, triggerItem, icon, recipes, Map.of(), KnowledgeNode.Kind.TRUNK,
                    Optional.empty());
        }

        public boolean purchasable() {
            return !cost.isEmpty();
        }
    }

    private static volatile Map<ResourceLocation, NodeView> nodes = Map.of();
    private static volatile Set<ResourceLocation> unlocked = Set.of();
    private static volatile Map<ResourceLocation, List<ResourceLocation>> gatesByRecipe = Map.of();
    private static volatile Map<ResourceLocation, Long> points = Map.of();
    private static volatile Set<ResourceLocation> requirements = Set.of();
    private static volatile Set<ResourceLocation> eurekas = Set.of();

    private ClientKnowledge() {}

    public static void set(Collection<NodeView> views, Collection<ResourceLocation> unlockedIds) {
        set(views, unlockedIds, Map.of(), List.of(), List.of());
    }

    public static void set(Collection<NodeView> views, Collection<ResourceLocation> unlockedIds,
                           Map<ResourceLocation, Long> pointsByArea, Collection<ResourceLocation> requirementsMet,
                           Collection<ResourceLocation> eurekasFound) {
        points = Collections.unmodifiableMap(new LinkedHashMap<>(pointsByArea));
        requirements = Set.copyOf(requirementsMet);
        eurekas = Set.copyOf(eurekasFound);
        Map<ResourceLocation, NodeView> map = new LinkedHashMap<>();
        views.forEach(view -> map.put(view.id(), view));
        nodes = Collections.unmodifiableMap(map);
        unlocked = Set.copyOf(unlockedIds);
        Map<ResourceLocation, List<ResourceLocation>> index = new HashMap<>();
        for (NodeView view : map.values()) {
            for (ResourceLocation recipe : view.recipes()) {
                index.computeIfAbsent(recipe, key -> new ArrayList<>()).add(view.id());
            }
        }
        gatesByRecipe = index;
    }

    public static void clear() {
        points = Map.of();
        requirements = Set.of();
        eurekas = Set.of();
        nodes = Map.of();
        unlocked = Set.of();
        gatesByRecipe = Map.of();
    }

    /** Nodes the server says gate this recipe, in graph order. */
    public static List<ResourceLocation> gatesFor(ResourceLocation recipeId) {
        return gatesByRecipe.getOrDefault(recipeId, List.of());
    }

    public static Collection<NodeView> nodes() {
        return nodes.values();
    }

    public static Optional<NodeView> view(ResourceLocation id) {
        return Optional.ofNullable(nodes.get(id));
    }

    public static boolean isUnlocked(ResourceLocation id) {
        return unlocked.contains(id);
    }

    /** Research points per area the player's scope holds. */
    public static Map<ResourceLocation, Long> points() {
        return points;
    }

    public static boolean requirementMet(ResourceLocation id) {
        return requirements.contains(id);
    }

    public static boolean eurekaMet(ResourceLocation id) {
        return eurekas.contains(id);
    }
}
