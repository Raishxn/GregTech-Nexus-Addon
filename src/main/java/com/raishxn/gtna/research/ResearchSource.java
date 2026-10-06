package com.raishxn.gtna.research;

import net.minecraft.resources.ResourceLocation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Where research points come from: something the team does, counted per scope. The first time pays
 * {@link #first} points; after that only the {@link #milestones} pay, when the running count crosses them.
 * Doing the same thing a thousand times therefore pays a handful of times, never a thousand.
 * <p>
 * Loaded from {@code data/<namespace>/gtna_research_sources/<path>.json}; the id is the file's location.
 *
 * <pre>
 * {"event": "craft", "item": "gtceu:steel_ingot", "area": "gtia:metallurgy", "first": 10,
 *  "milestones": [{"count": 100, "points": 5}, {"count": 10000, "points": 10}]}
 * </pre>
 */
public record ResearchSource(ResourceLocation id, Event event, Optional<ResourceLocation> item,
                             Optional<ResourceLocation> recipeType, Optional<ResourceLocation> machine,
                             ResourceLocation area, int first, List<Milestone> milestones) {

    /** What has to happen. */
    public enum Event {

        /** A team member holds the item (checked with the research trigger scan). Counts once. */
        OBTAIN,
        /** A team member crafts the item on a crafting table or in a crafting grid; counts the stack size. */
        CRAFT,
        /** A team member takes the item out of a furnace; counts the stack size. */
        SMELT,
        /**
         * A machine owned by the team finishes a recipe of {@code recipe_type}, or one whose outputs contain
         * {@code item} (both, when both are given). Counts one per finished recipe.
         */
        MACHINE_RECIPE,
        /** A multiblock owned by the team forms; {@code machine} is its definition id. Counts one per forming. */
        MULTIBLOCK_FORMED,
        /**
         * A machine owned by the team finishes a recipe of {@code recipe_type} that consumes {@code item}: the
         * item was analysed. Counts one per finished recipe. The first analysis is also a discovery: every node
         * whose eureka names the item gets its discount.
         */
        ANALYZE;

        public static final Codec<Event> CODEC = Codec.STRING.comapFlatMap(name -> {
            try {
                return DataResult.success(Event.valueOf(name.toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException e) {
                return DataResult.error(() -> "Unknown research source event: " + name);
            }
        }, event -> event.name().toLowerCase(Locale.ROOT));
    }

    /** Points paid once the running count reaches {@code count}. */
    public record Milestone(long count, int points) {

        public static final Codec<Milestone> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.LONG.fieldOf("count").forGetter(Milestone::count),
                Codec.intRange(1, Integer.MAX_VALUE).fieldOf("points").forGetter(Milestone::points))
                .apply(instance, Milestone::new));
    }

    /** The JSON shape, before the loader attaches the id. */
    public record Definition(Event event, Optional<ResourceLocation> item, Optional<ResourceLocation> recipeType,
                             Optional<ResourceLocation> machine, ResourceLocation area, int first,
                             List<Milestone> milestones, List<String> requiresMods) {

        public boolean modsLoaded(java.util.function.Predicate<String> isLoaded) {
            return requiresMods.stream().allMatch(isLoaded);
        }

        /** The source, or the reason it cannot work. */
        public DataResult<ResearchSource> withId(ResourceLocation id) {
            boolean needsItem = event == Event.OBTAIN || event == Event.CRAFT || event == Event.SMELT;
            if (needsItem && item.isEmpty()) return DataResult.error(() -> event + " needs an item");
            if (event == Event.MACHINE_RECIPE && item.isEmpty() && recipeType.isEmpty()) {
                return DataResult.error(() -> "machine_recipe needs a recipe_type, an item or both");
            }
            if (event == Event.ANALYZE && (item.isEmpty() || recipeType.isEmpty())) {
                return DataResult.error(() -> "analyze needs an item and a recipe_type");
            }
            if (event == Event.MULTIBLOCK_FORMED && machine.isEmpty()) {
                return DataResult.error(() -> "multiblock_formed needs a machine");
            }
            if (first == 0 && milestones.isEmpty()) return DataResult.error(() -> "the source pays nothing");
            List<Milestone> sorted = milestones.stream().sorted(Comparator.comparingLong(Milestone::count)).toList();
            return DataResult.success(new ResearchSource(id, event, item, recipeType, machine, area, first, sorted));
        }
    }

    // spotless:off
    public static final Codec<Definition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Event.CODEC.fieldOf("event").forGetter(Definition::event),
            StrictFields.optional("item", ResourceLocation.CODEC).forGetter(Definition::item),
            StrictFields.optional("recipe_type", ResourceLocation.CODEC).forGetter(Definition::recipeType),
            StrictFields.optional("machine", ResourceLocation.CODEC).forGetter(Definition::machine),
            ResourceLocation.CODEC.fieldOf("area").forGetter(Definition::area),
            StrictFields.optional("first", Codec.intRange(0, Integer.MAX_VALUE), 0).forGetter(Definition::first),
            StrictFields.optional("milestones", Milestone.CODEC.listOf(), List.<Milestone>of()).forGetter(Definition::milestones),
            StrictFields.optional("requires_mods", Codec.STRING.listOf(), List.<String>of()).forGetter(Definition::requiresMods)
    ).apply(instance, Definition::new));
    // spotless:on

    public ResearchSource {
        milestones = List.copyOf(milestones);
    }

    /** Points earned when the count goes from {@code before} to {@code after}. */
    public int pointsFor(long before, long after) {
        if (after <= before) return 0;
        int points = before == 0 ? first : 0;
        for (Milestone milestone : milestones) {
            if (milestone.count() > before && milestone.count() <= after) points += milestone.points();
        }
        return points;
    }
}
