package com.raishxn.gtna.research;

import net.minecraft.resources.ResourceLocation;

import java.util.*;

/**
 * Places research nodes on a grid for the tree screen: columns run left to right by tier and, inside a
 * tier, by prerequisite depth; rows inside a column follow the average row of the node's prerequisites
 * so connecting lines stay short. Pure data, no rendering.
 */
public final class KnowledgeTreeLayout {

    public record Slot(int column, int row) {}

    /** The columns that belong to one tier, for the header drawn above them. */
    public record Band(int tier, int firstColumn, int columnCount) {}

    public record Layout(Map<ResourceLocation, Slot> slots, List<Band> bands, int columns, int rows) {

        public static final Layout EMPTY = new Layout(Map.of(), List.of(), 0, 0);
    }

    private KnowledgeTreeLayout() {}

    public static Layout of(Collection<ClientKnowledge.NodeView> views) {
        if (views.isEmpty()) return Layout.EMPTY;
        Map<ResourceLocation, ClientKnowledge.NodeView> byId = new LinkedHashMap<>();
        views.stream().sorted(Comparator.comparing(ClientKnowledge.NodeView::id))
                .forEach(view -> byId.put(view.id(), view));

        Map<ResourceLocation, Integer> depth = new HashMap<>();
        for (var id : byId.keySet()) depthInTier(id, byId, depth, new HashSet<>());

        // Column key = (tier, depth in tier); distinct keys become consecutive columns.
        TreeMap<Long, List<ClientKnowledge.NodeView>> columns = new TreeMap<>();
        for (var view : byId.values()) {
            long key = ((long) view.tier() << 32) | depth.get(view.id());
            columns.computeIfAbsent(key, k -> new ArrayList<>()).add(view);
        }

        Map<ResourceLocation, Slot> slots = new LinkedHashMap<>();
        Map<Integer, int[]> bandRanges = new TreeMap<>();
        int column = 0, rows = 0;
        for (var entry : columns.entrySet()) {
            List<ClientKnowledge.NodeView> members = entry.getValue();
            final Map<ResourceLocation, Slot> placed = slots;
            members.sort(Comparator.comparingDouble((ClientKnowledge.NodeView v) -> barycenter(v, placed))
                    .thenComparing(ClientKnowledge.NodeView::id));
            for (int row = 0; row < members.size(); row++) {
                slots.put(members.get(row).id(), new Slot(column, row));
            }
            rows = Math.max(rows, members.size());
            int tier = (int) (entry.getKey() >> 32);
            final int firstColumn = column;
            int[] range = bandRanges.computeIfAbsent(tier, k -> new int[] { firstColumn, 0 });
            range[1]++;
            column++;
        }
        List<Band> bands = new ArrayList<>();
        bandRanges.forEach((tier, range) -> bands.add(new Band(tier, range[0], range[1])));
        return new Layout(Collections.unmodifiableMap(slots), List.copyOf(bands), column, rows);
    }

    private static double barycenter(ClientKnowledge.NodeView view, Map<ResourceLocation, Slot> placed) {
        double sum = 0;
        int count = 0;
        for (var prerequisite : view.prerequisites()) {
            Slot slot = placed.get(prerequisite);
            if (slot != null) {
                sum += slot.row();
                count++;
            }
        }
        return count == 0 ? Double.MAX_VALUE : sum / count;
    }

    private static int depthInTier(ResourceLocation id, Map<ResourceLocation, ClientKnowledge.NodeView> byId,
                                   Map<ResourceLocation, Integer> memo, Set<ResourceLocation> visiting) {
        Integer known = memo.get(id);
        if (known != null) return known;
        if (!visiting.add(id)) return 0; // malformed data (a cycle): break it instead of recursing forever
        var view = byId.get(id);
        int depth = 0;
        for (var prerequisite : view.prerequisites()) {
            var other = byId.get(prerequisite);
            if (other != null && other.tier() == view.tier()) {
                depth = Math.max(depth, 1 + depthInTier(prerequisite, byId, memo, visiting));
            }
        }
        visiting.remove(id);
        memo.put(id, depth);
        return depth;
    }
}
