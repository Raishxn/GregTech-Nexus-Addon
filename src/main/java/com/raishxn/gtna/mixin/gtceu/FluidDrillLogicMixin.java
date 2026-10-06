package com.raishxn.gtna.mixin.gtceu;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.ActionResult;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.common.machine.trait.FluidDrillLogic;

import net.minecraft.world.level.material.Fluid;

import com.raishxn.gtna.api.machine.feature.IFluidExtractionReceipt;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;

/** Records successful extraction, rather than discovery of the vein or starting a cycle. */
@Mixin(FluidDrillLogic.class)
public abstract class FluidDrillLogicMixin implements IFluidExtractionReceipt {

    @Unique
    private Fluid gtna$extractedFluid;

    @Redirect(method = "onRecipeFinish",
              at = @At(value = "INVOKE",
                       target = "Lcom/gregtechceu/gtceu/api/recipe/RecipeHelper;handleRecipeIO(Lcom/gregtechceu/gtceu/api/capability/recipe/IRecipeCapabilityHolder;Lcom/gregtechceu/gtceu/api/recipe/GTRecipe;Lcom/gregtechceu/gtceu/api/capability/recipe/IO;Ljava/util/Map;)Lcom/gregtechceu/gtceu/api/recipe/ActionResult;"),
              remap = false)
    private ActionResult gtna$recordOutput(IRecipeCapabilityHolder holder, GTRecipe recipe, IO io,
                                           Map<RecipeCapability<?>, Object2IntMap<?>> caches) {
        ActionResult result = RecipeHelper.handleRecipeIO(holder, recipe, io, caches);
        boolean positiveOutput = recipe
                .getOutputContents(com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability.CAP)
                .stream().anyMatch(content -> com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability.CAP
                        .of(content.content).getAmount() > 0);
        if (result.isSuccess() && io == IO.OUT && positiveOutput) {
            gtna$extractedFluid = ((FluidDrillLogic) (Object) this).getVeinFluid();
        }
        return result;
    }

    @Override
    public Fluid gtna$getExtractedFluid() {
        return gtna$extractedFluid;
    }
}
