package com.raishxn.gtna.mixin.gtceu;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.raishxn.gtna.common.machine.multiblock.part.AccelerateHatchPartMachine;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RecipeLogic.class)
public abstract class GTRecipeLogicMixin {

    @Shadow(remap = false)
    @Final
    public IRecipeLogicMachine machine;

    @Shadow(remap = false)
    protected int duration;
    @Inject(method = "setupRecipe", at = @At("RETURN"), remap = false)
    private void gtna$applyAccelerateHatch(GTRecipe recipe, CallbackInfo ci) {
        if (this.machine instanceof MetaMachine metaMachine && metaMachine instanceof WorkableMultiblockMachine multiMachine) {

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
}