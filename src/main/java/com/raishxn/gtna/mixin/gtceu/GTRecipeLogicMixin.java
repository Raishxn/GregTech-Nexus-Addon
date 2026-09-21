package com.raishxn.gtna.mixin.gtceu;

import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMufflerMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;

import com.raishxn.gtna.api.machine.feature.BufferModeSwitchPolicy;
import com.raishxn.gtna.api.machine.feature.IPatternBufferModeProvider;
import com.raishxn.gtna.api.machine.feature.ModeIdMatcher;
import com.raishxn.gtna.common.machine.multiblock.electric.WorkableElectricMultipleRecipesMachine;
import com.raishxn.gtna.common.machine.multiblock.energy.IndustrialSlaughterhouse;
import com.raishxn.gtna.common.machine.multiblock.part.AccelerateHatchPartMachine;
import com.raishxn.gtna.common.machine.multiblock.part.OverclockHatchPartMachine;
import com.raishxn.gtna.common.machine.trait.GTNAMultipleRecipesLogic;
import com.raishxn.gtna.config.ConfigHolder;
import com.raishxn.gtna.utils.GTNASpecialPartUtil;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Iterator;
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

        recipe = gtna$applyOverclockHatch(recipe, multiMachine);

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

    private GTRecipe gtna$applyOverclockHatch(GTRecipe recipe, WorkableMultiblockMachine multiMachine) {
        if (multiMachine instanceof WorkableElectricMultipleRecipesMachine ||
                multiMachine instanceof IndustrialSlaughterhouse) {
            return recipe;
        }

        if (recipe.ocLevel <= 0) {
            return recipe;
        }

        double durationFactor = OverclockingLogic.STD_DURATION_FACTOR;
        for (var part : multiMachine.getParts()) {
            if (part instanceof OverclockHatchPartMachine hatch) {
                durationFactor = Math.min(durationFactor, hatch.getOverclockMultiplier());
            }
        }
        if (durationFactor >= OverclockingLogic.STD_DURATION_FACTOR) {
            return recipe;
        }

        GTRecipe adjusted = recipe.copy();
        double additionalMultiplier = Math.pow(durationFactor / OverclockingLogic.STD_DURATION_FACTOR, recipe.ocLevel);
        adjusted.duration = Math.max(1, (int) Math.floor(adjusted.duration * additionalMultiplier));
        return adjusted;
    }

    @Inject(method = "setupRecipe", at = @At("RETURN"), remap = false)
    private void gtna$applyAccelerateHatch(GTRecipe recipe, CallbackInfo ci) {
        if (this.machine instanceof MetaMachine metaMachine &&
                metaMachine instanceof WorkableMultiblockMachine multiMachine) {

            // GTOCore semantics: the penalty follows the recipe, not the machine. getPreOCRecipeEuTier
            // undoes the overclock and parallel scaling, so a high-tier machine running a low-tier
            // recipe is not punished.
            int recipeTier = RecipeHelper.getPreOCRecipeEuTier(recipe);

            int bestPercentage = 100;
            boolean found = false;
            for (var part : multiMachine.getParts()) {
                if (part instanceof AccelerateHatchPartMachine accHatch) {
                    int percentage = accHatch.calcDurationPercentage(recipeTier);
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

    /**
     * Lets a GTNA pattern buffer drive the machine mode of GTCEu multiblocks that use the stock
     * logic (Large Cutter, Multi Smelter, ...). Those only ever search their <b>active</b> recipe
     * type, so the buffer cannot influence the search unless the mode is corrected first — hence
     * HEAD of {@code searchRecipe}, before the search body runs.
     *
     * <p>
     * Idle-only by policy ({@link BufferModeSwitchPolicy}): a busy machine is never touched. GTNA's
     * own multi-recipe machines are skipped because their logic already mirrors the mode.
     */
    @Inject(method = "searchRecipe", at = @At("HEAD"), remap = false)
    private void gtna$switchModeForPendingBufferContent(CallbackInfoReturnable<Iterator<GTRecipe>> cir) {
        if (ConfigHolder.INSTANCE == null || !ConfigHolder.INSTANCE.machines.bufferDrivenMachineMode) {
            return;
        }
        if ((Object) this instanceof GTNAMultipleRecipesLogic) {
            return;
        }
        if (!(this.machine instanceof MetaMachine metaMachine) ||
                !(metaMachine instanceof IMultiController controller)) {
            return;
        }
        GTRecipeType[] recipeTypes = this.machine.getRecipeTypes();
        if (recipeTypes == null || recipeTypes.length <= 1) {
            return;
        }

        int target = BufferModeSwitchPolicy.selectTargetIndex(
                this.machine.getRecipeLogic().isIdle(),
                gtna$resolveModeIndex(gtna$findPendingBufferMode(controller), recipeTypes),
                this.machine.getActiveRecipeType());
        if (target == BufferModeSwitchPolicy.KEEP_CURRENT) {
            return;
        }
        // Same formula as MachineModeFancyConfigurator.setActiveRecipeTypeAndUpdateTickSubs.
        boolean needUpdateTickSubs = !this.machine.keepSubscribing() &&
                target != this.machine.getActiveRecipeType();
        this.machine.setActiveRecipeType(target);
        if (needUpdateTickSubs) {
            this.machine.getRecipeLogic().updateTickSubscription();
        }
    }

    /** First non-blank mode request among the controller's pattern buffers, or {@code null}. */
    private static @Nullable String gtna$findPendingBufferMode(IMultiController controller) {
        for (IMultiPart part : controller.getParts()) {
            if (part instanceof IPatternBufferModeProvider provider) {
                String candidate = provider.gtna$getPendingModeId();
                if (candidate != null && !candidate.isBlank()) {
                    return candidate;
                }
            }
        }
        return null;
    }

    /** Index of the recipe type a mode id refers to, or {@link BufferModeSwitchPolicy#KEEP_CURRENT}. */
    private static int gtna$resolveModeIndex(@Nullable String modeId, GTRecipeType[] recipeTypes) {
        if (modeId == null || modeId.isBlank()) {
            return BufferModeSwitchPolicy.KEEP_CURRENT;
        }
        for (int i = 0; i < recipeTypes.length; i++) {
            if (ModeIdMatcher.matches(modeId, recipeTypes[i])) {
                return i;
            }
        }
        return BufferModeSwitchPolicy.KEEP_CURRENT;
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
