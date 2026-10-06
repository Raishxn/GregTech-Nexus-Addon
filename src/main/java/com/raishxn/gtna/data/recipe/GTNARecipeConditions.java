package com.raishxn.gtna.data.recipe;

import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import com.raishxn.gtna.common.data.condition.CompactCondition;
import com.raishxn.gtna.common.data.condition.RestrictedItemsEnabledCondition;
import com.raishxn.gtna.research.ResearchGateCondition;

public class GTNARecipeConditions {

    public static final RecipeConditionType<CompactCondition> COMPACT = GTRegistries.RECIPE_CONDITIONS.register(
            "compact",
            new RecipeConditionType<>(() -> CompactCondition.INSTANCE, CompactCondition.CODEC));

    public static final RecipeConditionType<RestrictedItemsEnabledCondition> RESTRICTED_ITEMS_ENABLED = GTRegistries.RECIPE_CONDITIONS
            .register("restricted_items_enabled",
                    new RecipeConditionType<>(() -> RestrictedItemsEnabledCondition.INSTANCE,
                            RestrictedItemsEnabledCondition.CODEC));

    public static final RecipeConditionType<ResearchGateCondition> RESEARCH_GATE = GTRegistries.RECIPE_CONDITIONS
            .register("research_gate",
                    new RecipeConditionType<>(ResearchGateCondition::new, ResearchGateCondition.CODEC));

    public static void init() {}
}
