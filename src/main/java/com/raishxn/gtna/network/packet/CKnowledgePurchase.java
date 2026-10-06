package com.raishxn.gtna.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import com.raishxn.gtna.research.KnowledgeCommands;
import com.raishxn.gtna.research.KnowledgeScope;
import com.raishxn.gtna.research.KnowledgeService;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * The research screen asks to buy a node for the sender's team. The server checks everything again, exactly
 * as {@code /gtna research buy} does, and answers in the action bar; the new state reaches the screen through
 * the usual {@link SKnowledgeSync}.
 */
public record CKnowledgePurchase(ResourceLocation node) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(node);
    }

    public static CKnowledgePurchase decode(FriendlyByteBuf buf) {
        return new CKnowledgePurchase(buf.readResourceLocation());
    }

    public static void handle(CKnowledgePurchase msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            UUID scope = KnowledgeScope.of(player);
            var result = KnowledgeService.purchase(player.server, scope, msg.node);
            player.displayClientMessage(KnowledgeCommands.purchaseMessage(player.server, scope, msg.node, result,
                    player.getDisplayName()), true);
        });
        ctx.get().setPacketHandled(true);
    }
}
