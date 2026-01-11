package com.raishxn.gtna.utils;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.ActionResult;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import it.unimi.dsi.fastutil.objects.Object2IntMap;

import java.util.Map;

public class GTNARecipeUtils {

    public static ActiveRecipe startRecipe(RecipeLogic logic, GTRecipe recipe, MetaMachine machine) {
        if (!(machine instanceof IRecipeLogicMachine recipeLogicMachine)) return null;

        // English: Trigger the machine's getRecipeModifier (calls accurateParallel -> Overclock)
        // Português: Aciona o getRecipeModifier da máquina (chama accurateParallel -> Overclock)
        // Español: Activa el getRecipeModifier de la máquina (llama accurateParallel -> Overclock)
        GTRecipe modifiedRecipe = recipeLogicMachine.fullModifyRecipe(recipe.copy());

        if (modifiedRecipe == null) return null;

        // English: Safety check for duration
        // Português: Verificação de segurança para duração
        // Español: Comprobación de seguridad para duración
        if (modifiedRecipe.duration < 1) {
            modifiedRecipe.duration = 1;
        }

        // English: Check if inputs are sufficient for the (possibly multiplied) recipe
        // Português: Verifica se os inputs são suficientes para a receita (possivelmente multiplicada)
        // Español: Verifica si los insumos son suficientes para la receta (posiblemente multiplicada)
        if (!RecipeHelper.matchContents((IRecipeCapabilityHolder) machine, modifiedRecipe).isSuccess()) {
            return null;
        }

        // English: Consume inputs
        // Português: Consome os inputs
        // Español: Consumir insumos
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
        public final Map<RecipeCapability<?>, Object2IntMap<?>> chanceCaches;

        public ActiveRecipe(GTRecipe recipe, int maxProgress, Map<RecipeCapability<?>, Object2IntMap<?>> chanceCaches) {
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