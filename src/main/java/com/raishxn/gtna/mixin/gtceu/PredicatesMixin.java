package com.raishxn.gtna.mixin.gtceu;

import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;

import com.raishxn.gtna.api.machine.multiblock.GTNAPartAbility;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Predicates.class)
public abstract class PredicatesMixin {

    @Inject(method = "autoAbilities([Lcom/gregtechceu/gtceu/api/recipe/GTRecipeType;ZZZZZZ)" +
            "Lcom/gregtechceu/gtceu/api/pattern/TraceabilityPredicate;",
            at = @At("RETURN"), cancellable = true, remap = false)
    private static void gtna$addPerformanceHatchesToElectricMultiblocks(GTRecipeType[] recipeType,
                                                                        boolean checkEnergyIn,
                                                                        boolean checkEnergyOut,
                                                                        boolean checkItemIn,
                                                                        boolean checkItemOut,
                                                                        boolean checkFluidIn,
                                                                        boolean checkFluidOut,
                                                                        CallbackInfoReturnable<TraceabilityPredicate> cir) {
        if (!checkEnergyIn) {
            return;
        }

        for (GTRecipeType type : recipeType) {
            if (type.getMaxInputs(EURecipeCapability.CAP) > 0) {
                cir.setReturnValue(cir.getReturnValue()
                        .or(Predicates.abilities(GTNAPartAbility.OVERCLOCK_HATCH).setMaxGlobalLimited(1)
                                .setPreviewCount(1))
                        .or(Predicates.abilities(GTNAPartAbility.ACCELERATE_HATCH).setMaxGlobalLimited(1)
                                .setPreviewCount(1)));
                return;
            }
        }
    }
}
