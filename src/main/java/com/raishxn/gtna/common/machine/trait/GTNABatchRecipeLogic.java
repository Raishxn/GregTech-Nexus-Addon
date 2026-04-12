package com.raishxn.gtna.common.machine.trait;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class GTNABatchRecipeLogic extends RecipeLogic {

    private final Supplier<@Nullable GTRecipe> recipeSupplier;

    public GTNABatchRecipeLogic(IRecipeLogicMachine machine, Supplier<@Nullable GTRecipe> recipeSupplier) {
        super(machine);
        this.recipeSupplier = recipeSupplier;
    }

    @Override
    public void findAndHandleRecipe() {
        lastFailedMatches = null;
        failureReasonMap.clear();
        lastRecipe = null;
        lastOriginRecipe = null;

        GTRecipe recipe = recipeSupplier.get();
        if (recipe != null) {
            var result = checkRecipe(recipe);
            if (result.isSuccess()) {
                setupRecipe(recipe);
            } else {
                putFailureReason(this, recipe, result.reason());
                setWaiting(result.reason());
            }
        } else {
            setStatus(Status.IDLE);
            consecutiveRecipes = 0;
            progress = 0;
            duration = 0;
            isActive = false;
        }
        recipeDirty = false;
    }

    @Override
    public void onRecipeFinish() {
        machine.afterWorking();
        if (lastRecipe == null) {
            return;
        }
        runAttempt = 0;
        runDelay = 0;
        handleRecipeIO(lastRecipe, IO.OUT);
        setStatus(Status.IDLE);
        consecutiveRecipes = 0;
        progress = 0;
        duration = 0;
        isActive = false;
        lastRecipe = null;
        lastOriginRecipe = null;
    }

    @Override
    public boolean hasCustomProgressLine() {
        return true;
    }

    @Override
    public @Nullable Component getCustomProgressLine() {
        if (duration <= 0) {
            return null;
        }
        return Component.literal("Batch Progress: " + progress + " / " + duration + " ticks");
    }
}
