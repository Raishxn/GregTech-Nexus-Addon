package com.raishxn.gtna.data.recipe;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.ConditionalRecipe;

import com.raishxn.gtna.common.data.condition.RestrictedItemsEnabledForgeCondition;

import java.util.function.Consumer;

public final class GTNARecipeVisibility {

    private GTNARecipeVisibility() {}

    public static void saveRestricted(Consumer<FinishedRecipe> provider, ResourceLocation id,
                                      Consumer<Consumer<FinishedRecipe>> recipeFactory) {
        ConditionalRecipe.builder()
                .addCondition(RestrictedItemsEnabledForgeCondition.INSTANCE)
                .addRecipe(recipeFactory)
                .generateAdvancement()
                .build(provider, id);
    }
}
