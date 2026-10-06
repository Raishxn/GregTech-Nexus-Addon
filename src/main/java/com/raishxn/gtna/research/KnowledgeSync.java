package com.raishxn.gtna.research;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import com.raishxn.gtna.network.GTNANetworkHandler;
import com.raishxn.gtna.network.packet.SKnowledgeSync;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Server side of the research sync: builds the snapshot and sends it to the right players. */
public final class KnowledgeSync {

    private KnowledgeSync() {}

    public static SKnowledgeSync snapshot(MinecraftServer server, UUID scope) {
        List<ClientKnowledge.NodeView> nodes = new ArrayList<>();
        for (KnowledgeNode node : KnowledgeRegistry.graph().nodes()) {
            List<ResourceLocation> recipes = new ArrayList<>();
            for (KnowledgeGrant grant : node.grants()) {
                if (grant instanceof KnowledgeGrant.Recipes gated) recipes.addAll(gated.recipes());
            }
            Optional<ResourceLocation> trigger = node.trigger() instanceof KnowledgeTrigger.ObtainItem item ?
                    Optional.of(item.item()) : Optional.empty();
            nodes.add(new ClientKnowledge.NodeView(node.id(), node.tier(), node.prerequisites(), trigger, node.icon(),
                    recipes, node.cost(), node.kind(), node.eureka()));
        }
        KnowledgeData data = KnowledgeData.get(server);
        return new SKnowledgeSync(nodes, List.copyOf(data.unlocked(scope)), data.points(scope),
                List.copyOf(data.requirements(scope)), List.copyOf(data.eurekas(scope)));
    }

    public static void send(ServerPlayer player) {
        GTNANetworkHandler.sendToPlayer(snapshot(player.getServer(), KnowledgeScope.of(player)), player);
    }

    /** Re-sends to every online player whose scope is the given one (a whole team, or one player). */
    public static void syncScope(MinecraftServer server, UUID scope) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (KnowledgeScope.of(player).equals(scope)) send(player);
        }
    }
}
