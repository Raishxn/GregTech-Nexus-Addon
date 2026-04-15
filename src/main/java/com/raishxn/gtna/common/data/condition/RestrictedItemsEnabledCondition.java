package com.raishxn.gtna.common.data.condition;

import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;

import net.minecraft.network.chat.Component;

import com.mojang.serialization.Codec;
import com.raishxn.gtna.config.ConfigHolder;
import com.raishxn.gtna.data.recipe.GTNARecipeConditions;
import org.jetbrains.annotations.NotNull;

public class RestrictedItemsEnabledCondition extends RecipeCondition<RestrictedItemsEnabledCondition> {

    public static final RestrictedItemsEnabledCondition INSTANCE = new RestrictedItemsEnabledCondition();
    public static final Codec<RestrictedItemsEnabledCondition> CODEC = Codec.unit(INSTANCE);

    @Override
    public RecipeConditionType<RestrictedItemsEnabledCondition> getType() {
        return GTNARecipeConditions.RESTRICTED_ITEMS_ENABLED;
    }

    @Override
    public Component getTooltips() {
        return Component.translatable("gtna.recipe.condition.restricted_items_disabled");
    }

    @Override
    public boolean testCondition(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        return ConfigHolder.areRestrictedRecipesEnabled();
    }

    @Override
    public RestrictedItemsEnabledCondition createTemplate() {
        return INSTANCE;
    }
}
