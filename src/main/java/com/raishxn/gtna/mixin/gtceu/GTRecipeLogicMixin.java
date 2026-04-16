package com.raishxn.gtna.mixin.gtceu;

import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMufflerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredPartMachine;

import com.raishxn.gtna.common.machine.multiblock.part.AccelerateHatchPartMachine;
import com.raishxn.gtna.utils.GTNASpecialPartUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mixin(RecipeLogic.class)
public abstract class GTRecipeLogicMixin {

    @Shadow(remap = false)
    @Final
    public IRecipeLogicMachine machine;

    @Shadow(remap = false)
    protected int duration;

    @ModifyVariable(method = "setupRecipe", at = @At("HEAD"), argsOnly = true, remap = false)
    private GTRecipe gtna$applyMufflerEfficiencyBonus(GTRecipe recipe) {
        IRecipeCapabilityHolder holder = (IRecipeCapabilityHolder) this.machine;
        GTRecipe adjustedForCover = GTNASpecialPartUtil.adjustRecipeForSingleblockCover(holder, recipe);
        if (adjustedForCover != null) {
            recipe = adjustedForCover;
        }
        if (!(this.machine instanceof MetaMachine metaMachine) ||
                !(metaMachine instanceof WorkableMultiblockMachine multiMachine)) {
            return recipe;
        }

        int bestMufflerTier = -1;
        for (var part : multiMachine.getParts()) {
            if (part instanceof IMufflerMachine && part instanceof TieredPartMachine tieredPart) {
                bestMufflerTier = Math.max(bestMufflerTier, tieredPart.getTier());
            }
        }

        if (bestMufflerTier < 0) {
            return recipe;
        }

        int bonusTiers = Math.max(0, bestMufflerTier - 1);
        if (bonusTiers <= 0) {
            return recipe;
        }

        double multiplier = Math.max(0.01D, 1.0D / Math.pow(1.05D, bonusTiers));
        GTRecipe adjusted = recipe.copy();
        if (!gtna$scaleEnergyContents(adjusted.inputs, multiplier) &&
                !gtna$scaleEnergyContents(adjusted.tickInputs, multiplier)) {
            return recipe;
        }
        return adjusted;
    }

    @Inject(method = "setupRecipe", at = @At("RETURN"), remap = false)
    private void gtna$applyAccelerateHatch(GTRecipe recipe, CallbackInfo ci) {
        if (this.machine instanceof MetaMachine metaMachine &&
                metaMachine instanceof WorkableMultiblockMachine multiMachine) {

            int machineTier;
            if (metaMachine instanceof WorkableElectricMultiblockMachine electricMachine) {
                machineTier = electricMachine.getTier();
            } else {
                machineTier = metaMachine.getDefinition().getTier();
            }

            int bestPercentage = 100;
            boolean found = false;
            for (var part : multiMachine.getParts()) {
                if (part instanceof AccelerateHatchPartMachine accHatch) {
                    int percentage = accHatch.calcDurationPercentage(machineTier);
                    if (percentage < bestPercentage) {
                        bestPercentage = percentage;
                        found = true;
                    }
                }
            }
            if (found && bestPercentage < 100) {
                long newDuration = (long) this.duration * bestPercentage / 100L;
                this.duration = Math.max(1, (int) newDuration);
            }
        }
    }

    private static boolean gtna$scaleEnergyContents(Map<?, List<Content>> contents, double multiplier) {
        @SuppressWarnings("unchecked")
        List<Content> euContents = (List<Content>) contents.get(EURecipeCapability.CAP);
        if (euContents == null || euContents.isEmpty()) {
            return false;
        }

        List<Content> adjusted = new ArrayList<>(euContents.size());
        ContentModifier modifier = ContentModifier.multiplier(multiplier);
        for (Content content : euContents) {
            adjusted.add(content.copyChanced(EURecipeCapability.CAP, modifier));
        }
        @SuppressWarnings("unchecked")
        Map<Object, List<Content>> rawContents = (Map<Object, List<Content>>) contents;
        rawContents.put(EURecipeCapability.CAP, adjusted);
        return true;
    }
}
