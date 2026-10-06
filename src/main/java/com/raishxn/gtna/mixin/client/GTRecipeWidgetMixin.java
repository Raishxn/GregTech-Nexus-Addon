package com.raishxn.gtna.mixin.client;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.ingredient.EnergyStack;
import com.gregtechceu.gtceu.integration.xei.widgets.GTRecipeWidget;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import com.raishxn.gtna.common.data.GTNARecipeType;
import com.raishxn.gtna.research.ResearchGateCondition;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

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

    /**
     * Lists the research a recipe needs among its other conditions in the recipe viewer. Ordinal 1 is the
     * loop that prints each condition (ordinal 0 feeds the UI template); the recipe itself is untouched.
     */
    @Redirect(method = "setRecipeWidget",
              at = @At(value = "FIELD",
                       target = "Lcom/gregtechceu/gtceu/api/recipe/GTRecipe;conditions:Ljava/util/List;",
                       ordinal = 1))
    private List<RecipeCondition<?>> gtna$listResearchConditions(GTRecipe shown) {
        return ResearchGateCondition.withResearch(shown.conditions, shown.id);
    }

    private void gtna$setEUt(long eut) {
        if (recipeVoltageText == null) return;
        recipeVoltageText.setComponent(Component.translatable("gtna.recipe.eu_usage",
                FormattingUtil.formatNumbers(eut)).withStyle(ChatFormatting.UNDERLINE));
    }
}
