package com.raishxn.gtna.common.machine.trait;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.raishxn.gtna.api.machine.IThreadModifierMachine;
import com.raishxn.gtna.utils.GTNARecipeUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GTNAMultipleRecipesLogic extends RecipeLogic {

    // Lista de receitas rodando ao mesmo tempo (Threads)
    private final List<GTNARecipeUtils.ActiveRecipe> activeRecipes = new ArrayList<>();

    public GTNAMultipleRecipesLogic(MetaMachine machine) {
        super((IRecipeLogicMachine) machine);
    }

    @Override
    public void serverTick() {
        // CORREÇÃO 1 e 2: Cast para MetaMachine para acessar getLevel() e removemos getSubscription() que é redundante aqui
        MetaMachine metaMachine = (MetaMachine) this.machine;

        // 1. Verificar se precisamos iniciar novas receitas
        if (!metaMachine.getLevel().isClientSide) {
            // Se ainda temos espaço para mais threads (receitas distintas)
            while (activeRecipes.size() < getMaxThreads()) {
                if (!tryStartNewRecipe()) break; // Se não achou receita nova, para o loop
            }
        }

        // 2. Atualizar progresso das receitas existentes
        if (!activeRecipes.isEmpty()) {
            boolean anyFinished = false;
            Iterator<GTNARecipeUtils.ActiveRecipe> iterator = activeRecipes.iterator();

            while (iterator.hasNext()) {
                GTNARecipeUtils.ActiveRecipe active = iterator.next();

                // Atualiza a receita. Você deve implementar a lógica de consumo de energia aqui ou no ActiveRecipe
                if (active.update()) {
                    // Receita terminou
                    completeRecipe(active);
                    iterator.remove();
                    anyFinished = true;
                }
            }

            if (anyFinished) {
                // CORREÇÃO 3: Usar markDirty() do MetaMachine em vez de self().setChanged()
                metaMachine.markDirty();
            }
        }

        // Define se está ativo visualmente
        boolean isNowActive = !activeRecipes.isEmpty();
        if (this.isActive != isNowActive) {
            this.isActive = isNowActive;
            // Agenda atualização visual
            metaMachine.scheduleRenderUpdate();
        }
    }

    public int getMaxThreads() {
        int threads = 1;
        // Verificação segura usando a interface criada anteriormente
        if (machine instanceof IThreadModifierMachine modifier) {
            threads += modifier.getAdditionalThread();
        }
        return threads;
    }

    // Mude de 'void' para 'boolean'
    private boolean tryStartNewRecipe() {
        GTRecipe recipe = findRecipe();

        if (recipe != null) {
            // Tenta consumir os inputs e criar a instância da receita ativa
            GTNARecipeUtils.ActiveRecipe started = GTNARecipeUtils.startRecipe(this, recipe, (MetaMachine) machine);

            if (started != null) {
                activeRecipes.add(started);
                return true; // SUCESSO: Receita iniciada, continue o loop!
            }
        }

        return false; // FALHA: Não encontrou receita ou não conseguiu iniciar (falta de input, etc), pare o loop!
    }

    @Nullable
    protected GTRecipe findRecipe() {
        // Modificamos o filtro (o lambda r -> true) para rejeitar receitas que já estão rodando
        var iterator = machine.getRecipeType().searchRecipe((IRecipeCapabilityHolder) machine, recipe -> {

            // Itera sobre todas as receitas que já estão ativas (rodando em outras threads)
            for (GTNARecipeUtils.ActiveRecipe active : activeRecipes) {
                // Compara os IDs das receitas.
                // Usamos getId() porque o 'active.recipe' é uma cópia da receita original.
                if (active.recipe.getId().equals(recipe.getId())) {
                    return false; // REJEITA a receita se ela já estiver rodando em outra thread
                }
            }

            // Se a receita não estiver rodando, aceita ela (TRUE)
            return true;
        });

        if (iterator.hasNext()) {
            return iterator.next();
        }
        if (activeRecipes.size() < getMaxThreads()) {
             var fallbackIterator = machine.getRecipeType().searchRecipe((IRecipeCapabilityHolder) machine, r -> true);
             if (fallbackIterator.hasNext()) return fallbackIterator.next();
        }

        return null;
    }

    private void completeRecipe(GTNARecipeUtils.ActiveRecipe active) {
        // Entrega os outputs quando terminar
        RecipeHelper.handleRecipeIO((IRecipeCapabilityHolder) machine, active.recipe, IO.OUT, active.chanceCaches);
    }
}