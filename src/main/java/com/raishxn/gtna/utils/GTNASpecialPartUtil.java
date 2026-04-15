package com.raishxn.gtna.utils;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;

import com.raishxn.gtna.api.machine.feature.GTNANoConsumeFluidPart;
import com.raishxn.gtna.api.machine.feature.GTNANoConsumeItemPart;
import com.raishxn.gtna.api.machine.feature.GTNAOutputBoostFluidPart;
import com.raishxn.gtna.api.machine.feature.GTNAOutputBoostItemPart;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class GTNASpecialPartUtil {

    private GTNASpecialPartUtil() {}

    public static boolean hasNoConsumeItems(IRecipeCapabilityHolder holder) {
        if (!(holder instanceof IMultiController controller)) return false;
        for (IMultiPart part : controller.getParts()) {
            if (part instanceof GTNANoConsumeItemPart) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasNoConsumeFluids(IRecipeCapabilityHolder holder) {
        if (!(holder instanceof IMultiController controller)) return false;
        for (IMultiPart part : controller.getParts()) {
            if (part instanceof GTNANoConsumeFluidPart) {
                return true;
            }
        }
        return false;
    }

    public static int getItemOutputMultiplier(IRecipeCapabilityHolder holder) {
        if (!(holder instanceof IMultiController controller)) return 1;
        int multiplier = 1;
        for (IMultiPart part : controller.getParts()) {
            if (part instanceof GTNAOutputBoostItemPart boostPart) {
                multiplier = Math.max(multiplier, boostPart.gtna$getOutputMultiplier());
            }
        }
        return multiplier;
    }

    public static int getFluidOutputMultiplier(IRecipeCapabilityHolder holder) {
        if (!(holder instanceof IMultiController controller)) return 1;
        int multiplier = 1;
        for (IMultiPart part : controller.getParts()) {
            if (part instanceof GTNAOutputBoostFluidPart boostPart) {
                multiplier = Math.max(multiplier, boostPart.gtna$getOutputMultiplier());
            }
        }
        return multiplier;
    }

    public static GTRecipe stripNoConsumeInputs(IRecipeCapabilityHolder holder, GTRecipe recipe, boolean tick) {
        boolean stripItems = hasNoConsumeItems(holder);
        boolean stripFluids = hasNoConsumeFluids(holder);
        if (!stripItems && !stripFluids) {
            return null;
        }

        GTRecipe adjusted = recipe.copy();
        Map<RecipeCapability<?>, List<Content>> target = tick ? adjusted.tickInputs : adjusted.inputs;
        Map<RecipeCapability<?>, ?> chanceMap = tick ? adjusted.tickInputChanceLogics : adjusted.inputChanceLogics;

        if (stripItems) {
            target.remove(ItemRecipeCapability.CAP);
            chanceMap.remove(ItemRecipeCapability.CAP);
        }
        if (stripFluids) {
            target.remove(FluidRecipeCapability.CAP);
            chanceMap.remove(FluidRecipeCapability.CAP);
        }
        return adjusted;
    }

    public static GTRecipe applyOutputBoosts(IRecipeCapabilityHolder holder, GTRecipe recipe, boolean tick) {
        int itemMultiplier = getItemOutputMultiplier(holder);
        int fluidMultiplier = getFluidOutputMultiplier(holder);
        if (itemMultiplier <= 1 && fluidMultiplier <= 1) {
            return null;
        }

        GTRecipe adjusted = recipe.copy();
        Map<RecipeCapability<?>, List<Content>> target = tick ? adjusted.tickOutputs : adjusted.outputs;

        if (itemMultiplier > 1) {
            multiplyContents(target, ItemRecipeCapability.CAP, itemMultiplier);
        }
        if (fluidMultiplier > 1) {
            multiplyContents(target, FluidRecipeCapability.CAP, fluidMultiplier);
        }
        return adjusted;
    }

    private static void multiplyContents(Map<RecipeCapability<?>, List<Content>> target, RecipeCapability<?> capability,
                                         int multiplier) {
        List<Content> contents = target.get(capability);
        if (contents == null || contents.isEmpty()) {
            return;
        }

        List<Content> boosted = new ArrayList<>(contents.size());
        ContentModifier modifier = ContentModifier.multiplier(multiplier);
        for (Content content : contents) {
            boosted.add(content.copyChanced(capability, modifier));
        }
        target.put(capability, boosted);
    }
}
