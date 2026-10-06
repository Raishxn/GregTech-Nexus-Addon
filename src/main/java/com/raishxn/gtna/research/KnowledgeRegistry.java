package com.raishxn.gtna.research;

/** Holds the graph produced by the last data reload. */
public final class KnowledgeRegistry {

    private static volatile KnowledgeGraph graph = KnowledgeGraph.EMPTY;

    private KnowledgeRegistry() {}

    public static KnowledgeGraph graph() {
        return graph;
    }

    public static void set(KnowledgeGraph next) {
        graph = next == null ? KnowledgeGraph.EMPTY : next;
    }
}
