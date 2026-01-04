package com.raishxn.gtna.common.machine.trait;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine; // Importante
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class DynamicRecipeLogic extends RecipeLogic {

    private final Supplier<GTRecipe> recipeSupplier;

    // O construtor deve receber IRecipeLogicMachine
    public DynamicRecipeLogic(IRecipeLogicMachine machine, GTRecipeType recipeType, Supplier<GTRecipe> recipeSupplier) {
        // CORREÇÃO 1: RecipeLogic aceita apenas 'machine' no construtor
        super(machine);
        this.recipeSupplier = recipeSupplier;
    }

    // CORREÇÃO 2: getRecipe() não existe em RecipeLogic.
    // Devemos sobrescrever findAndHandleRecipe para pular a busca no JEI e injetar nossa receita.
    @Override
    public void findAndHandleRecipe() {
        lastFailedMatches = null;

        // Se já tivermos uma receita rodando e ela for válida, deixa ela continuar (lógica padrão)
        if (!recipeDirty && lastRecipe != null && checkRecipe(lastRecipe).isSuccess()) {
            GTRecipe recipe = lastRecipe;
            lastRecipe = null;
            lastOriginRecipe = null;
            setupRecipe(recipe);
            return;
        }

        // Aqui está a mágica: Em vez de procurar no RecipeMap (searchRecipe),
        // nós pegamos direto do seu Supplier
        GTRecipe dynamicRecipe = recipeSupplier.get();

        if (dynamicRecipe != null) {
            // Verifica se a receita é válida (se cabe no output bus, tem energia, etc)
            if (checkMatchedRecipeAvailable(dynamicRecipe)) {
                return;
            }
        }

        // Se falhar, limpa tudo
        lastRecipe = null;
        lastOriginRecipe = null;
    }
}