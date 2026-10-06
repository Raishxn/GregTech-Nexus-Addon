package com.raishxn.gtna.research;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * One entry of a research tree. Loaded from {@code data/<namespace>/gtna_research/<path>.json};
 * the node id is the file's resource location, so it is not part of the JSON.
 * <p>
 * A node with a {@link #cost} is <em>bought</em>: the team spends research points of each listed area,
 * and an {@code obtain_item} trigger becomes a requirement (the item must have been held once) instead
 * of unlocking the node by itself. A node without a cost keeps the original behaviour and unlocks as soon
 * as its trigger fires, so packs that never set a cost see no change.
 */
public record KnowledgeNode(ResourceLocation id, int tier, Optional<ResourceLocation> icon,
                            List<ResourceLocation> prerequisites, KnowledgeTrigger trigger,
                            List<KnowledgeGrant> grants, Optional<String> quest,
                            Map<ResourceLocation, Integer> cost, Kind kind, Optional<Eureka> eureka) {

    /** Where a node sits in a tier: the required trunk, an optional branch, or a cheap leaf. */
    public enum Kind {

        TRUNK,
        BRANCH,
        LEAF;

        public static final Codec<Kind> CODEC = Codec.STRING.comapFlatMap(name -> {
            try {
                return DataResult.success(Kind.valueOf(name.toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException e) {
                return DataResult.error(() -> "Unknown research node kind: " + name);
            }
        }, kind -> kind.name().toLowerCase(Locale.ROOT));
    }

    /**
     * Analysing (or, until the analysis machine exists, being granted) this item lowers the node's cost.
     * {@code reduction} is the fraction removed: 1.0 makes the node free.
     */
    public record Eureka(ResourceLocation item, double reduction) {

        public static final Codec<Eureka> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("item").forGetter(Eureka::item),
                Codec.doubleRange(0.0, 1.0).optionalFieldOf("reduction", 0.5).forGetter(Eureka::reduction))
                .apply(instance, Eureka::new));
    }

    /** The JSON shape, before the loader attaches the id. */
    public record Definition(int tier, Optional<ResourceLocation> icon, List<ResourceLocation> prerequisites,
                             KnowledgeTrigger trigger, List<KnowledgeGrant> grants, Optional<String> quest,
                             List<String> requiresMods, Map<ResourceLocation, Integer> cost, Kind kind,
                             Optional<Eureka> eureka) {

        /** True when every mod this node needs is present. A node for a missing mod is skipped, not an error. */
        public boolean modsLoaded(java.util.function.Predicate<String> isLoaded) {
            return requiresMods.stream().allMatch(isLoaded);
        }

        public KnowledgeNode withId(ResourceLocation id) {
            return new KnowledgeNode(id, tier, icon, prerequisites, trigger, grants, quest, cost, kind, eureka);
        }
    }

    // spotless:off
    public static final Codec<Definition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            StrictFields.optional("tier", Codec.INT, 1).forGetter(Definition::tier),
            StrictFields.optional("icon", ResourceLocation.CODEC).forGetter(Definition::icon),
            StrictFields.optional("prerequisites", ResourceLocation.CODEC.listOf(), List.<ResourceLocation>of()).forGetter(Definition::prerequisites),
            StrictFields.optional("trigger", KnowledgeTrigger.CODEC, (KnowledgeTrigger) new KnowledgeTrigger.Manual()).forGetter(Definition::trigger),
            StrictFields.optional("grants", KnowledgeGrant.CODEC.listOf(), List.<KnowledgeGrant>of()).forGetter(Definition::grants),
            StrictFields.optional("quest", Codec.STRING).forGetter(Definition::quest),
            StrictFields.optional("requires_mods", Codec.STRING.listOf(), List.<String>of()).forGetter(Definition::requiresMods),
            StrictFields.optional("cost", Codec.unboundedMap(ResourceLocation.CODEC, Codec.intRange(1, Integer.MAX_VALUE)), Map.<ResourceLocation, Integer>of()).forGetter(Definition::cost),
            StrictFields.optional("kind", Kind.CODEC, Kind.TRUNK).forGetter(Definition::kind),
            StrictFields.optional("eureka", Eureka.CODEC).forGetter(Definition::eureka)
    ).apply(instance, Definition::new));
    // spotless:on

    public KnowledgeNode {
        prerequisites = List.copyOf(prerequisites);
        grants = List.copyOf(grants);
        cost = Collections.unmodifiableMap(new LinkedHashMap<>(cost));
    }

    /** A free trunk node, as every node was before research points existed. */
    public KnowledgeNode(ResourceLocation id, int tier, Optional<ResourceLocation> icon,
                         List<ResourceLocation> prerequisites, KnowledgeTrigger trigger, List<KnowledgeGrant> grants,
                         Optional<String> quest) {
        this(id, tier, icon, prerequisites, trigger, grants, quest, Map.of(), Kind.TRUNK, Optional.empty());
    }

    /** True when the team buys the node with points instead of unlocking it by its trigger alone. */
    public boolean purchasable() {
        return !cost.isEmpty();
    }

    /** The item the team must have held once before buying, if the trigger names one. */
    public Optional<ResourceLocation> requiredItem() {
        return trigger instanceof KnowledgeTrigger.ObtainItem obtain ? Optional.of(obtain.item()) : Optional.empty();
    }

    /** The cost after the eureka discount, rounded up so a partial discount never makes a node free. */
    public Map<ResourceLocation, Integer> cost(boolean eurekaMet) {
        if (!eurekaMet || eureka.isEmpty()) return cost;
        double keep = 1.0 - eureka.get().reduction();
        Map<ResourceLocation, Integer> reduced = new LinkedHashMap<>();
        cost.forEach((area, amount) -> {
            int value = (int) Math.ceil(amount * keep - 1e-9);
            if (value > 0) reduced.put(area, value);
        });
        return Collections.unmodifiableMap(reduced);
    }

    /** Lang key for the node name: {@code gtna.research.node.<namespace>.<path with dots>}. */
    public String nameKey() {
        return nameKey(id);
    }

    public static String nameKey(ResourceLocation id) {
        return "gtna.research.node." + id.getNamespace() + "." + id.getPath().replace('/', '.');
    }

    /** Lang key for an area name: {@code gtna.research.area.<namespace>.<path with dots>}. */
    public static String areaKey(ResourceLocation area) {
        return "gtna.research.area." + area.getNamespace() + "." + area.getPath().replace('/', '.');
    }

    /** Name from the lang key, falling back to the plain id so content without lang still reads. */
    public Component displayName() {
        return displayName(id);
    }

    public static Component displayName(ResourceLocation id) {
        return Component.translatableWithFallback(nameKey(id), id.toString());
    }

    public static Component areaName(ResourceLocation area) {
        return Component.translatableWithFallback(areaKey(area), area.getPath());
    }
}
