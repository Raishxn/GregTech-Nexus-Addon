package com.raishxn.gtna.research;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import com.raishxn.gtna.GTNACORE;

import java.util.*;

/**
 * The loaded {@link ResearchSource}s, indexed by what triggers them, and the one place that turns an event
 * into points: it advances the scope's counter for the source and pays what {@link ResearchSource#pointsFor}
 * says. Empty until a pack ships sources, in which case every hook returns at once.
 */
public final class ResearchSources {

    public static final String DIRECTORY = "gtna_research_sources";

    /** Immutable lookup tables for one data reload. */
    public record Index(List<ResearchSource> all, Map<ResourceLocation, List<ResearchSource>> obtain,
                        Map<ResourceLocation, List<ResearchSource>> craft,
                        Map<ResourceLocation, List<ResearchSource>> smelt, List<ResearchSource> machineRecipe,
                        Map<ResourceLocation, List<ResearchSource>> multiblock,
                        Map<ResourceLocation, List<ResearchSource>> analyze, Set<ResourceLocation> analysisTypes) {

        public static final Index EMPTY = of(List.of());

        public static Index of(Collection<ResearchSource> sources) {
            Map<ResourceLocation, List<ResearchSource>> obtain = new HashMap<>(), craft = new HashMap<>(),
                    smelt = new HashMap<>(), multiblock = new HashMap<>(), analyze = new HashMap<>();
            Set<ResourceLocation> analysisTypes = new HashSet<>();
            List<ResearchSource> machineRecipe = new ArrayList<>();
            for (ResearchSource source : sources) {
                switch (source.event()) {
                    case OBTAIN -> obtain.computeIfAbsent(source.item().orElseThrow(), k -> new ArrayList<>())
                            .add(source);
                    case CRAFT -> craft.computeIfAbsent(source.item().orElseThrow(), k -> new ArrayList<>())
                            .add(source);
                    case SMELT -> smelt.computeIfAbsent(source.item().orElseThrow(), k -> new ArrayList<>())
                            .add(source);
                    case MACHINE_RECIPE -> machineRecipe.add(source);
                    case MULTIBLOCK_FORMED -> multiblock
                            .computeIfAbsent(source.machine().orElseThrow(), k -> new ArrayList<>()).add(source);
                    case ANALYZE -> {
                        analyze.computeIfAbsent(source.item().orElseThrow(), k -> new ArrayList<>()).add(source);
                        analysisTypes.add(source.recipeType().orElseThrow());
                    }
                }
            }
            return new Index(List.copyOf(sources), obtain, craft, smelt, List.copyOf(machineRecipe), multiblock,
                    analyze, Set.copyOf(analysisTypes));
        }

        public boolean isEmpty() {
            return all.isEmpty();
        }
    }

    private static volatile Index index = Index.EMPTY;

    private ResearchSources() {}

    public static Index index() {
        return index;
    }

    public static void set(Index next) {
        index = next == null ? Index.EMPTY : next;
    }

    /**
     * Counts {@code amount} occurrences of the source for the scope and pays what that earns.
     *
     * @return the points paid (0 when the count only moved between milestones).
     */
    public static int record(MinecraftServer server, UUID scope, ResearchSource source, long amount) {
        if (amount <= 0) return 0;
        KnowledgeData data = KnowledgeData.get(server);
        long before = data.counter(scope, source.id());
        long after = source.event() == ResearchSource.Event.OBTAIN ? Math.max(before, 1) : before + amount;
        if (after == before) return 0;
        data.setCounter(scope, source.id(), after);
        int points = source.pointsFor(before, after);
        if (points > 0) {
            KnowledgeService.addPoints(server, scope, source.area(), points);
            announce(server, scope, source, points);
        }
        return points;
    }

    /**
     * The scope analysed {@code item} in a machine of {@code recipeType}: pays the matching analyze sources and,
     * the first time, finds the eureka of every node that names the item and tells the scope's players.
     *
     * @return the nodes whose eureka was newly found.
     */
    public static List<KnowledgeNode> analyze(MinecraftServer server, UUID scope, ResourceLocation item,
                                              ResourceLocation recipeType) {
        List<ResearchSource> sources = index.analyze().get(item);
        if (sources == null) return List.of();
        boolean matched = false;
        for (ResearchSource source : sources) {
            if (!source.recipeType().orElseThrow().equals(recipeType)) continue;
            matched = true;
            record(server, scope, source, 1);
        }
        if (!matched) return List.of();
        List<KnowledgeNode> found = new ArrayList<>();
        for (KnowledgeNode node : KnowledgeRegistry.graph().nodes()) {
            if (node.eureka().isPresent() && node.eureka().get().item().equals(item) &&
                    KnowledgeService.markEureka(server, scope, node.id())) {
                found.add(node);
            }
        }
        for (KnowledgeNode node : found) {
            Component message = Component.translatable("gtna.research.eureka", node.displayName());
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (KnowledgeScope.of(player).equals(scope)) player.sendSystemMessage(message);
            }
        }
        return found;
    }

    /** Records every source in the list, for convenience. */
    public static int recordAll(MinecraftServer server, UUID scope, List<ResearchSource> sources, long amount) {
        int total = 0;
        for (ResearchSource source : sources) total += record(server, scope, source, amount);
        return total;
    }

    private static void announce(MinecraftServer server, UUID scope, ResearchSource source, int points) {
        Component message = Component.translatable("gtna.research.points.gained", points,
                KnowledgeNode.areaName(source.area()));
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (KnowledgeScope.of(player).equals(scope)) player.displayClientMessage(message, true);
        }
    }

    /** Reads {@code data/<namespace>/gtna_research_sources/**.json} on every data reload. */
    public static final class Loader extends SimpleJsonResourceReloadListener {

        private static final Gson GSON = new GsonBuilder().create();

        public Loader() {
            super(GSON, DIRECTORY);
        }

        @Override
        protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager resources,
                             ProfilerFiller profiler) {
            List<ResearchSource> sources = new ArrayList<>();
            int skipped = 0;
            for (var entry : files.entrySet()) {
                var definition = ResearchSource.CODEC.parse(JsonOps.INSTANCE, entry.getValue()).resultOrPartial(
                        error -> GTNACORE.LOGGER.error("Invalid research source {}: {}", entry.getKey(), error));
                if (definition.isEmpty()) continue;
                if (!definition.get().modsLoaded(net.minecraftforge.fml.ModList.get()::isLoaded)) {
                    skipped++;
                    continue;
                }
                definition.get().withId(entry.getKey()).resultOrPartial(
                        error -> GTNACORE.LOGGER.error("Invalid research source {}: {}", entry.getKey(), error))
                        .ifPresent(sources::add);
            }
            set(Index.of(sources));
            GTNACORE.LOGGER.info("Loaded {} research point sources ({} files, {} skipped for missing mods)",
                    sources.size(), files.size(), skipped);
        }
    }
}
