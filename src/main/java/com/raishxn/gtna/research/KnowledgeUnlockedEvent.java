package com.raishxn.gtna.research;

import net.minecraftforge.eventbus.api.Event;

import java.util.UUID;

/** Posted on the Forge bus (server side) after a scope newly unlocks a node. */
public final class KnowledgeUnlockedEvent extends Event {

    private final UUID scope;
    private final KnowledgeNode node;

    public KnowledgeUnlockedEvent(UUID scope, KnowledgeNode node) {
        this.scope = scope;
        this.node = node;
    }

    public UUID scope() {
        return scope;
    }

    public KnowledgeNode node() {
        return node;
    }
}
