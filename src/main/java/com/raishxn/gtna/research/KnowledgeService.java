package com.raishxn.gtna.research;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/** The public entry point for other systems: ask, unlock, reset. Server side only. */
public final class KnowledgeService {

    public enum UnlockResult {
        UNLOCKED,
        ALREADY_UNLOCKED,
        UNKNOWN_NODE,
        MISSING_PREREQUISITES
    }

    /** Outcome of {@link #purchase}, checked in this order. */
    public enum PurchaseResult {
        PURCHASED,
        ALREADY_UNLOCKED,
        UNKNOWN_NODE,
        MISSING_PREREQUISITES,
        /** The node names an item the team has not held yet. */
        MISSING_REQUIREMENT,
        NOT_ENOUGH_POINTS
    }

    private KnowledgeService() {}

    public static boolean isUnlocked(MinecraftServer server, UUID scope, ResourceLocation node) {
        return KnowledgeData.get(server).isUnlocked(scope, node);
    }

    public static Set<ResourceLocation> unlocked(MinecraftServer server, UUID scope) {
        return KnowledgeData.get(server).unlocked(scope);
    }

    /** True when the scope holds any node that grants this flag. */
    public static boolean hasFlag(MinecraftServer server, UUID scope, ResourceLocation flag) {
        KnowledgeGraph graph = KnowledgeRegistry.graph();
        KnowledgeData data = KnowledgeData.get(server);
        for (ResourceLocation id : data.unlocked(scope)) {
            var node = graph.get(id);
            if (node.isEmpty()) continue;
            for (KnowledgeGrant grant : node.get().grants()) {
                if (grant instanceof KnowledgeGrant.Flag f && f.flag().equals(flag)) return true;
            }
        }
        return false;
    }

    /** True when every node that gates the recipe is unlocked. Ungated recipes always pass. */
    public static boolean recipeAllowed(MinecraftServer server, UUID scope, ResourceLocation recipeId) {
        return firstMissingGate(server, scope, recipeId).isEmpty();
    }

    /**
     * The first node, in graph order, that gates the recipe and is not unlocked for the scope (a team or
     * player id from {@link KnowledgeScope}, never a raw machine owner). A machine without an owner has no
     * scope, so any gate blocks it.
     */
    public static Optional<ResourceLocation> firstMissingGate(MinecraftServer server, @Nullable UUID scope,
                                                              ResourceLocation recipeId) {
        Set<ResourceLocation> gates = KnowledgeRegistry.graph().gatesFor(recipeId);
        if (gates.isEmpty()) return Optional.empty();
        if (scope == null) return Optional.of(gates.iterator().next());
        KnowledgeData data = KnowledgeData.get(server);
        return gates.stream().filter(gate -> !data.isUnlocked(scope, gate)).findFirst();
    }

    /**
     * Unlocks a node. Without {@code force} every prerequisite must already be held. With it, the
     * node's whole prerequisite chain is unlocked first (admin and quest use).
     */
    public static UnlockResult unlock(MinecraftServer server, UUID scope, ResourceLocation id, boolean force) {
        KnowledgeGraph graph = KnowledgeRegistry.graph();
        var node = graph.get(id);
        if (node.isEmpty()) return UnlockResult.UNKNOWN_NODE;
        KnowledgeData data = KnowledgeData.get(server);
        if (data.isUnlocked(scope, id)) return UnlockResult.ALREADY_UNLOCKED;
        if (!force) {
            if (!graph.prerequisitesMet(node.get(), prerequisite -> data.isUnlocked(scope, prerequisite))) {
                return UnlockResult.MISSING_PREREQUISITES;
            }
            grant(server, data, scope, node.get());
            return UnlockResult.UNLOCKED;
        }
        for (KnowledgeNode step : graph.closure(id)) {
            if (!data.isUnlocked(scope, step.id())) grant(server, data, scope, step);
        }
        return UnlockResult.UNLOCKED;
    }

    /**
     * Buys a node: prerequisites held, item requirement met (if the trigger names an item), and enough
     * points in every area of the cost after the eureka discount. On success the points are spent and the
     * node is granted exactly as an unlock would grant it. Nodes without a cost can be bought too, for free.
     */
    public static PurchaseResult purchase(MinecraftServer server, UUID scope, ResourceLocation id) {
        KnowledgeGraph graph = KnowledgeRegistry.graph();
        var found = graph.get(id);
        if (found.isEmpty()) return PurchaseResult.UNKNOWN_NODE;
        KnowledgeNode node = found.get();
        KnowledgeData data = KnowledgeData.get(server);
        if (data.isUnlocked(scope, id)) return PurchaseResult.ALREADY_UNLOCKED;
        if (!graph.prerequisitesMet(node, prerequisite -> data.isUnlocked(scope, prerequisite))) {
            return PurchaseResult.MISSING_PREREQUISITES;
        }
        if (node.requiredItem().isPresent() && !data.requirementMet(scope, id)) {
            return PurchaseResult.MISSING_REQUIREMENT;
        }
        if (!data.spend(scope, cost(server, scope, node))) return PurchaseResult.NOT_ENOUGH_POINTS;
        grant(server, data, scope, node);
        return PurchaseResult.PURCHASED;
    }

    /** What the node costs this scope right now, after the eureka discount. */
    public static Map<ResourceLocation, Integer> cost(MinecraftServer server, UUID scope, KnowledgeNode node) {
        return node.cost(KnowledgeData.get(server).eurekaMet(scope, node.id()));
    }

    public static Map<ResourceLocation, Long> points(MinecraftServer server, UUID scope) {
        return KnowledgeData.get(server).points(scope);
    }

    /**
     * Gives (or takes, with a negative amount) research points and tells the scope's players.
     *
     * @return the new balance of the area.
     */
    public static long addPoints(MinecraftServer server, UUID scope, ResourceLocation area, long amount) {
        long value = KnowledgeData.get(server).addPoints(scope, area, amount);
        KnowledgeSync.syncScope(server, scope);
        return value;
    }

    /**
     * Records that the scope has held the node's required item.
     *
     * @return true if this was the first time.
     */
    public static boolean markRequirement(MinecraftServer server, UUID scope, ResourceLocation node) {
        boolean added = KnowledgeData.get(server).markRequirement(scope, node);
        if (added) KnowledgeSync.syncScope(server, scope);
        return added;
    }

    /**
     * Records that the scope found the node's eureka, which lowers its cost.
     *
     * @return true if this was the first time.
     */
    public static boolean markEureka(MinecraftServer server, UUID scope, ResourceLocation node) {
        boolean added = KnowledgeData.get(server).markEureka(scope, node);
        if (added) KnowledgeSync.syncScope(server, scope);
        return added;
    }

    public static int reset(MinecraftServer server, UUID scope) {
        int removed = KnowledgeData.get(server).reset(scope);
        if (removed > 0) KnowledgeSync.syncScope(server, scope);
        return removed;
    }

    private static void grant(MinecraftServer server, KnowledgeData data, UUID scope, KnowledgeNode node) {
        data.add(scope, node.id());
        MinecraftForge.EVENT_BUS.post(new KnowledgeUnlockedEvent(scope, node));
        KnowledgeSync.syncScope(server, scope);
    }

    /** Ids the scope has not unlocked yet, in graph order. */
    public static List<KnowledgeNode> locked(MinecraftServer server, UUID scope) {
        KnowledgeData data = KnowledgeData.get(server);
        return KnowledgeRegistry.graph().nodes().stream().filter(node -> !data.isUnlocked(scope, node.id())).toList();
    }
}
