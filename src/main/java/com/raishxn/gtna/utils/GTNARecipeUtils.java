package com.raishxn.gtna.utils;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.ActionResult;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.content.Content;

import java.util.Map;

public class GTNARecipeUtils {

    public static ActiveRecipe startRecipe(RecipeLogic logic, GTRecipe recipe, MetaMachine machine) {
        if (!(machine instanceof IRecipeLogicMachine recipeLogicMachine)) return null;
        GTRecipe modifiedRecipe = recipeLogicMachine.fullModifyRecipe(recipe.copy());
        if (modifiedRecipe == null) return null;
        if (!RecipeHelper.matchContents((IRecipeCapabilityHolder) machine, modifiedRecipe).isSuccess()) {
            return null;
        }
        ActionResult result = RecipeHelper.handleRecipeIO((IRecipeCapabilityHolder) machine, modifiedRecipe, IO.IN, logic.getChanceCaches());
        if (result.isSuccess()) {
            return new ActiveRecipe(modifiedRecipe, modifiedRecipe.duration, logic.getChanceCaches());
        }
        return null;
    }
    public static class ActiveRecipe {
        public final GTRecipe recipe;
        public int progress;
        public final int maxProgress;
        public final Map<com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability<?>, it.unimi.dsi.fastutil.objects.Object2IntMap<?>> chanceCaches;
        public ActiveRecipe(GTRecipe recipe, int maxProgress, Map<com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability<?>, it.unimi.dsi.fastutil.objects.Object2IntMap<?>> chanceCaches) {
            this.recipe = recipe;
            this.progress = 0;
            this.maxProgress = maxProgress;
            this.chanceCaches = chanceCaches;
        }
        public boolean update() {
            this.progress++;
            return this.progress >= this.maxProgress;
        }
    }
}