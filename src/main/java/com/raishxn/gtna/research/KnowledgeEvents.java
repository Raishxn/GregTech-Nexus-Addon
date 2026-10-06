package com.raishxn.gtna.research;

import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;

/** Wires the research engine into the Forge bus. */
public final class KnowledgeEvents {

    private KnowledgeEvents() {}

    public static void register(IEventBus forgeBus) {
        forgeBus.addListener(KnowledgeEvents::addReloadListeners);
        forgeBus.addListener(KnowledgeEvents::registerCommands);
        forgeBus.addListener(KnowledgeEvents::syncToPlayers);
        forgeBus.addListener(KnowledgeTriggers::onPlayerTick);
        forgeBus.addListener(KnowledgeTriggers::onItemCrafted);
        forgeBus.addListener(KnowledgeTriggers::onItemPickup);
        forgeBus.addListener(KnowledgeTriggers::onItemSmelted);
    }

    private static void addReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new KnowledgeLoader());
        event.addListener(new ResearchSources.Loader());
    }

    /** Fires at login and after every data reload, for exactly the players that need the new graph. */
    private static void syncToPlayers(OnDatapackSyncEvent event) {
        var joining = event.getPlayer();
        if (joining != null) {
            KnowledgeSync.send(joining);
        } else {
            event.getPlayerList().getPlayers().forEach(KnowledgeSync::send);
        }
    }

    private static void registerCommands(RegisterCommandsEvent event) {
        KnowledgeCommands.register(event.getDispatcher());
    }
}
