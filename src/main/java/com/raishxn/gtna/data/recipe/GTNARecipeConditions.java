package com.raishxn.gtna.data.recipe;

import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.raishxn.gtna.common.data.condition.CompactCondition;

public class GTNARecipeConditions {

    public static final RecipeConditionType<CompactCondition> COMPACT = GTRegistries.RECIPE_CONDITIONS.register(
            "compact",
            new RecipeConditionType<>(() -> CompactCondition.INSTANCE, CompactCondition.CODEC)
    );

    public static void init() {
    }
}