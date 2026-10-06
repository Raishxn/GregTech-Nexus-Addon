package com.raishxn.gtna.research;

import net.minecraft.resources.ResourceLocation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

/** What unlocking a {@link KnowledgeNode} gives the owning team. */
public sealed interface KnowledgeGrant {

    /** Type key used in JSON ({@code "type": "..."}). */
    String type();

    /** Machine recipes that only run for teams that hold the node. */
    record Recipes(List<ResourceLocation> recipes) implements KnowledgeGrant {

        public static final String TYPE = "recipe_condition";
        static final Codec<Recipes> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.listOf().fieldOf("recipes").forGetter(Recipes::recipes))
                .apply(instance, Recipes::new));

        public Recipes {
            recipes = List.copyOf(recipes);
        }

        @Override
        public String type() {
            return TYPE;
        }
    }

    /** A named switch that other systems read with {@link KnowledgeService#hasFlag}. */
    record Flag(ResourceLocation flag) implements KnowledgeGrant {

        public static final String TYPE = "flag";
        static final Codec<Flag> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("id").forGetter(Flag::flag)).apply(instance, Flag::new));

        @Override
        public String type() {
            return TYPE;
        }
    }

    Codec<KnowledgeGrant> CODEC = Codec.STRING.partialDispatch("type",
            grant -> DataResult.success(grant.type()),
            type -> switch (type) {
                case Recipes.TYPE -> DataResult.success(Recipes.CODEC);
                case Flag.TYPE -> DataResult.success(Flag.CODEC);
                default -> DataResult.error(() -> "Unknown research grant type: " + type);
            });
}
