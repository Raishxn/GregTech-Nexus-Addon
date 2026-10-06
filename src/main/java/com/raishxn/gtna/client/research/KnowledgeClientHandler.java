package com.raishxn.gtna.client.research;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import com.raishxn.gtna.network.packet.SKnowledgeSync;
import com.raishxn.gtna.research.ClientKnowledge;

/**
 * Client half of {@link SKnowledgeSync}: stores the graph and the player's unlocked nodes. The recipe
 * viewer reads them through {@code GTRecipeWidgetMixin}; recipes themselves are never modified, because
 * the viewer's recipes can be the integrated server's own objects.
 */
@OnlyIn(Dist.CLIENT)
public final class KnowledgeClientHandler {

    private KnowledgeClientHandler() {}

    public static void apply(SKnowledgeSync msg) {
        ClientKnowledge.set(msg.nodes(), msg.unlocked(), msg.points(), msg.requirements(), msg.eurekas());
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientKnowledge.clear();
    }
}
