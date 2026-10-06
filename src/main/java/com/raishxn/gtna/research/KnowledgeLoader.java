package com.raishxn.gtna.research;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import com.raishxn.gtna.GTNACORE;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Reads {@code data/<namespace>/gtna_research/**.json} on every data reload. */
public final class KnowledgeLoader extends SimpleJsonResourceReloadListener {

    public static final String DIRECTORY = "gtna_research";
    private static final Gson GSON = new GsonBuilder().create();

    public KnowledgeLoader() {
        super(GSON, DIRECTORY);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager resources,
                         ProfilerFiller profiler) {
        List<KnowledgeNode> parsed = new ArrayList<>();
        int skipped = 0;
        for (var entry : files.entrySet()) {
            var result = KnowledgeNode.CODEC.parse(JsonOps.INSTANCE, entry.getValue());
            var definition = result.resultOrPartial(
                    error -> GTNACORE.LOGGER.error("Invalid research node {}: {}", entry.getKey(), error));
            if (definition.isEmpty()) continue;
            if (!definition.get().modsLoaded(net.minecraftforge.fml.ModList.get()::isLoaded)) {
                skipped++;
                GTNACORE.LOGGER.debug("Skipping research node {}: needs mods {}", entry.getKey(),
                        definition.get().requiresMods());
                continue;
            }
            parsed.add(definition.get().withId(entry.getKey()));
        }
        KnowledgeGraph.Build build = KnowledgeGraph.build(parsed);
        build.problems().forEach(problem -> GTNACORE.LOGGER.error("Research node dropped: {}", problem));
        KnowledgeRegistry.set(build.graph());
        GTNACORE.LOGGER.info("Loaded {} research nodes ({} files, {} skipped for missing mods, {} problems)",
                build.graph().size(), files.size(), skipped,
                build.problems().size() + (files.size() - parsed.size() - skipped));
    }
}
