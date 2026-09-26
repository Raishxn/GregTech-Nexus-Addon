package com.raishxn.gtna.mixin.client;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.ingredient.EnergyStack;
import com.gregtechceu.gtceu.integration.xei.widgets.GTRecipeWidget;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import com.raishxn.gtna.common.data.GTNARecipeType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Show the Component Assembly Line's actual EU/t as GTO does in its recipe viewer. */
@Mixin(value = GTRecipeWidget.class, remap = false)
public abstract class GTRecipeWidgetMixin {

    @Shadow
    @Final
    private GTRecipe recipe;

    @Shadow
    private LabelWidget recipeVoltageText;

    @Shadow
    private int tier;

    @Shadow
    @Final
    private int minTier;

    @Inject(method = "initializeRecipeTextWidget", at = @At("TAIL"))
    private void gtna$showComponentAssemblyEUt(CallbackInfo ci) {
        if (recipe.recipeType == GTNARecipeType.COMPONENT_ASSEMBLY_RECIPES) {
            gtna$setEUt(RecipeHelper.getRealEUtWithIO(recipe).getTotalEU());
        }
    }

    @Inject(method = "setRecipeOverclockWidget", at = @At("TAIL"))
    private void gtna$showOverclockedComponentAssemblyEUt(OverclockingLogic logic, CallbackInfo ci) {
        if (recipe.recipeType != GTNARecipeType.COMPONENT_ASSEMBLY_RECIPES) return;
        EnergyStack inputEUt = recipe.getInputEUt();
        if (tier > minTier && !inputEUt.isEmpty()) {
            int overclocks = tier - minTier;
            if (minTier == GTValues.ULV) overclocks--;
            var params = new OverclockingLogic.OCParams(inputEUt.voltage(), recipe.duration, overclocks, 1);
            var result = logic.runOverclockingLogic(params, GTValues.V[tier]);
            inputEUt = inputEUt.multiplyVoltage(result.eutMultiplier());
        }
        gtna$setEUt(inputEUt.getTotalEU());
    }

    private void gtna$setEUt(long eut) {
        if (recipeVoltageText == null) return;
        recipeVoltageText.setComponent(Component.translatable("gtna.recipe.eu_usage",
                FormattingUtil.formatNumbers(eut)).withStyle(ChatFormatting.UNDERLINE));
    }
}
