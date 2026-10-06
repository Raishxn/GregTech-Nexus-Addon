package com.raishxn.gtna.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import com.raishxn.gtna.client.research.KnowledgeClientHandler;
import com.raishxn.gtna.research.ClientKnowledge;
import com.raishxn.gtna.research.KnowledgeNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Server → Client: the research graph plus the nodes the receiving player's scope has unlocked. Sent on
 * login, after a data reload and whenever that scope's progress changes. The client work lives in
 * {@link KnowledgeClientHandler} so this class never references client-only classes on a dedicated server.
 */
public record SKnowledgeSync(List<ClientKnowledge.NodeView> nodes, List<ResourceLocation> unlocked,
                             Map<ResourceLocation, Long> points, List<ResourceLocation> requirements,
                             List<ResourceLocation> eurekas) {

    public SKnowledgeSync(List<ClientKnowledge.NodeView> nodes, List<ResourceLocation> unlocked) {
        this(nodes, unlocked, Map.of(), List.of(), List.of());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(nodes.size());
        for (var node : nodes) {
            buf.writeResourceLocation(node.id());
            buf.writeVarInt(node.tier());
            buf.writeCollection(node.prerequisites(), FriendlyByteBuf::writeResourceLocation);
            buf.writeOptional(node.triggerItem(), FriendlyByteBuf::writeResourceLocation);
            buf.writeOptional(node.icon(), FriendlyByteBuf::writeResourceLocation);
            buf.writeCollection(node.recipes(), FriendlyByteBuf::writeResourceLocation);
            buf.writeMap(node.cost(), FriendlyByteBuf::writeResourceLocation, FriendlyByteBuf::writeVarInt);
            buf.writeEnum(node.kind());
            buf.writeOptional(node.eureka(), (b, eureka) -> {
                b.writeResourceLocation(eureka.item());
                b.writeDouble(eureka.reduction());
            });
        }
        buf.writeCollection(unlocked, FriendlyByteBuf::writeResourceLocation);
        buf.writeMap(points, FriendlyByteBuf::writeResourceLocation, FriendlyByteBuf::writeVarLong);
        buf.writeCollection(requirements, FriendlyByteBuf::writeResourceLocation);
        buf.writeCollection(eurekas, FriendlyByteBuf::writeResourceLocation);
    }

    public static SKnowledgeSync decode(FriendlyByteBuf buf) {
        int count = buf.readVarInt();
        List<ClientKnowledge.NodeView> nodes = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            ResourceLocation id = buf.readResourceLocation();
            int tier = buf.readVarInt();
            List<ResourceLocation> prerequisites = buf.readList(FriendlyByteBuf::readResourceLocation);
            Optional<ResourceLocation> trigger = buf.readOptional(FriendlyByteBuf::readResourceLocation);
            Optional<ResourceLocation> icon = buf.readOptional(FriendlyByteBuf::readResourceLocation);
            List<ResourceLocation> recipes = buf.readList(FriendlyByteBuf::readResourceLocation);
            Map<ResourceLocation, Integer> cost = buf.readMap(LinkedHashMap::new, FriendlyByteBuf::readResourceLocation,
                    FriendlyByteBuf::readVarInt);
            KnowledgeNode.Kind kind = buf.readEnum(KnowledgeNode.Kind.class);
            Optional<KnowledgeNode.Eureka> eureka = buf.readOptional(
                    b -> new KnowledgeNode.Eureka(b.readResourceLocation(), b.readDouble()));
            nodes.add(new ClientKnowledge.NodeView(id, tier, prerequisites, trigger, icon, recipes, cost, kind,
                    eureka));
        }
        List<ResourceLocation> unlocked = buf.readList(FriendlyByteBuf::readResourceLocation);
        Map<ResourceLocation, Long> points = buf.readMap(LinkedHashMap::new, FriendlyByteBuf::readResourceLocation,
                FriendlyByteBuf::readVarLong);
        return new SKnowledgeSync(nodes, unlocked, points, buf.readList(FriendlyByteBuf::readResourceLocation),
                buf.readList(FriendlyByteBuf::readResourceLocation));
    }

    public static void handle(SKnowledgeSync msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> KnowledgeClientHandler.apply(msg));
        ctx.get().setPacketHandled(true);
    }
}
