package com.raishxn.gtna.utils;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;

import com.raishxn.gtna.api.machine.feature.GTNANoConsumeFluidPart;
import com.raishxn.gtna.api.machine.feature.GTNANoConsumeItemPart;
import com.raishxn.gtna.api.machine.feature.GTNAOutputBoostFluidPart;
import com.raishxn.gtna.api.machine.feature.GTNAOutputBoostItemPart;
import com.raishxn.gtna.common.cover.InfiniteElectricSingleblockCover;
import com.raishxn.gtna.common.cover.InfiniteSteamSingleblockCover;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class GTNASpecialPartUtil {

    private GTNASpecialPartUtil() {}

    private static final int SINGLEBLOCK_OUTPUT_MULTIPLIER = 10;
    private static final int SINGLEBLOCK_DURATION_DIVISOR = 5;

    private static MetaMachine getMetaMachine(IRecipeCapabilityHolder holder) {
        return holder instanceof MetaMachine metaMachine ? metaMachine : null;
    }

    private static boolean isSingleblockMachine(IRecipeCapabilityHolder holder) {
        return getMetaMachine(holder) != null && !(holder instanceof IMultiController);
    }

    private static boolean hasCover(IRecipeCapabilityHolder holder, Class<? extends CoverBehavior> coverClass) {
        MetaMachine machine = getMetaMachine(holder);
        if (machine == null) {
            return false;
        }
        for (CoverBehavior cover : machine.getCoverContainer().getCovers()) {
            if (coverClass.isInstance(cover)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasInfiniteElectricSingleblockCover(IRecipeCapabilityHolder holder) {
        return isSingleblockMachine(holder) && hasCover(holder, InfiniteElectricSingleblockCover.class);
    }

    public static boolean hasInfiniteSteamSingleblockCover(IRecipeCapabilityHolder holder) {
        return isSingleblockMachine(holder) && hasCover(holder, InfiniteSteamSingleblockCover.class);
    }

    public static boolean hasSingleblockInfinityCover(IRecipeCapabilityHolder holder) {
        return hasInfiniteElectricSingleblockCover(holder) || hasInfiniteSteamSingleblockCover(holder);
    }

    public static boolean hasNoConsumeItems(IRecipeCapabilityHolder holder) {
        if (hasSingleblockInfinityCover(holder)) {
            return true;
        }
        if (!(holder instanceof IMultiController controller)) return false;
        for (IMultiPart part : controller.getParts()) {
            if (part instanceof GTNANoConsumeItemPart) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasNoConsumeFluids(IRecipeCapabilityHolder holder) {
        if (hasSingleblockInfinityCover(holder)) {
            return true;
        }
        if (!(holder instanceof IMultiController controller)) return false;
        for (IMultiPart part : controller.getParts()) {
            if (part instanceof GTNANoConsumeFluidPart) {
                return true;
            }
        }
        return false;
    }

    public static int getItemOutputMultiplier(IRecipeCapabilityHolder holder) {
        int multiplier = 1;
        if (hasSingleblockInfinityCover(holder)) {
            multiplier = Math.max(multiplier, SINGLEBLOCK_OUTPUT_MULTIPLIER);
        }
        if (holder instanceof IMultiController controller) {
            for (IMultiPart part : controller.getParts()) {
                if (part instanceof GTNAOutputBoostItemPart boostPart) {
                    multiplier = Math.max(multiplier, boostPart.gtna$getOutputMultiplier());
                }
            }
        }
        return multiplier;
    }

    public static int getFluidOutputMultiplier(IRecipeCapabilityHolder holder) {
        int multiplier = 1;
        if (hasSingleblockInfinityCover(holder)) {
            multiplier = Math.max(multiplier, SINGLEBLOCK_OUTPUT_MULTIPLIER);
        }
        if (holder instanceof IMultiController controller) {
            for (IMultiPart part : controller.getParts()) {
                if (part instanceof GTNAOutputBoostFluidPart boostPart) {
                    multiplier = Math.max(multiplier, boostPart.gtna$getOutputMultiplier());
                }
            }
        }
        return multiplier;
    }

    public static GTRecipe adjustRecipeForSingleblockCover(IRecipeCapabilityHolder holder, GTRecipe recipe) {
        if (!hasSingleblockInfinityCover(holder)) {
            return null;
        }
        GTRecipe adjusted = recipe.copy();
        clearEnergyContents(adjusted.inputs, adjusted.inputChanceLogics);
        clearEnergyContents(adjusted.tickInputs, adjusted.tickInputChanceLogics);
        adjusted.duration = Math.max(1, adjusted.duration / SINGLEBLOCK_DURATION_DIVISOR);
        return adjusted;
    }

    public static GTRecipe adjustRecipeForMatching(IRecipeCapabilityHolder holder, GTRecipe recipe) {
        boolean hasSingleblockCover = hasSingleblockInfinityCover(holder);
        int itemMultiplier = getItemOutputMultiplier(holder);
        int fluidMultiplier = getFluidOutputMultiplier(holder);
        if (!hasSingleblockCover && itemMultiplier <= 1 && fluidMultiplier <= 1) {
            return null;
        }

        GTRecipe adjusted = recipe.copy();
        if (hasSingleblockCover) {
            clearEnergyContents(adjusted.inputs, adjusted.inputChanceLogics);
            clearEnergyContents(adjusted.tickInputs, adjusted.tickInputChanceLogics);
        }
        if (itemMultiplier > 1) {
            multiplyContents(adjusted.outputs, ItemRecipeCapability.CAP, itemMultiplier);
            multiplyContents(adjusted.tickOutputs, ItemRecipeCapability.CAP, itemMultiplier);
        }
        if (fluidMultiplier > 1) {
            multiplyContents(adjusted.outputs, FluidRecipeCapability.CAP, fluidMultiplier);
            multiplyContents(adjusted.tickOutputs, FluidRecipeCapability.CAP, fluidMultiplier);
        }
        return adjusted;
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

    private static void clearEnergyContents(Map<RecipeCapability<?>, List<Content>> target,
                                            Map<RecipeCapability<?>, ?> chanceMap) {
        target.remove(EURecipeCapability.CAP);
        chanceMap.remove(EURecipeCapability.CAP);
    }
}
