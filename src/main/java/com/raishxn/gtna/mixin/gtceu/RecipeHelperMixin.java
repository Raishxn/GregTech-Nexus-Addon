package com.raishxn.gtna.mixin.gtceu;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.ActionResult;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

import com.raishxn.gtna.research.KnowledgeNode;
import com.raishxn.gtna.research.KnowledgeRegistry;
import com.raishxn.gtna.research.KnowledgeScope;
import com.raishxn.gtna.research.KnowledgeService;
import com.raishxn.gtna.utils.GTNASpecialPartUtil;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.Map;

@Mixin(RecipeHelper.class)
public class RecipeHelperMixin {

    /**
     * Research gates: a recipe named by a node's {@code recipe_condition} grant only runs for an owner
     * whose team holds the node. Ungated recipes and empty graphs take the first two returns.
     */
    @Inject(method = "checkConditions", at = @At("HEAD"), cancellable = true, remap = false)
    private static void gtna$checkResearchGates(GTRecipe recipe, RecipeLogic recipeLogic,
                                                CallbackInfoReturnable<ActionResult> cir) {
        var graph = KnowledgeRegistry.graph();
        if (graph.isEmpty() || recipe.id == null || graph.gatesFor(recipe.id).isEmpty()) {
            return;
        }
        var machine = recipeLogic.getMachine();
        if (!(machine.getLevel() instanceof ServerLevel level)) {
            return;
        }
        // The owner is a player id; progress belongs to that player's effective team, so map it first.
        var owner = machine.getOwnerUUID();
        KnowledgeService.firstMissingGate(level.getServer(), owner == null ? null : KnowledgeScope.of(owner), recipe.id)
                .ifPresent(node -> cir.setReturnValue(ActionResult.fail(
                        Component.translatable("gtna.research.requires", KnowledgeNode.displayName(node)), null,
                        null)));
    }

    @Inject(method = "matchContents", at = @At("HEAD"), cancellable = true, remap = false)
    private static void gtna$matchSpecialContents(IRecipeCapabilityHolder holder, GTRecipe recipe,
                                                  CallbackInfoReturnable<ActionResult> cir) {
        GTRecipe adjusted = GTNASpecialPartUtil.adjustRecipeForMatching(holder, recipe);
        if (adjusted == null) {
            return;
        }
        cir.setReturnValue(gtna$matchContents(holder, adjusted));
    }

    @Inject(method = "matchTickRecipe", at = @At("HEAD"), cancellable = true, remap = false)
    private static void gtna$matchSpecialTickContents(IRecipeCapabilityHolder holder, GTRecipe recipe,
                                                      CallbackInfoReturnable<ActionResult> cir) {
        GTRecipe adjusted = GTNASpecialPartUtil.adjustRecipeForMatching(holder, recipe);
        if (adjusted == null) {
            return;
        }
        cir.setReturnValue(gtna$matchTick(holder, adjusted));
    }

    @Inject(method = "handleRecipeIO", at = @At("HEAD"), cancellable = true, remap = false)
    private static void gtna$handleSpecialRecipeIO(IRecipeCapabilityHolder holder, GTRecipe recipe, IO io,
                                                   Map<RecipeCapability<?>, Object2IntMap<?>> chanceCaches,
                                                   CallbackInfoReturnable<ActionResult> cir) {
        if (io == IO.IN) {
            GTRecipe adjusted = GTNASpecialPartUtil.stripNoConsumeInputs(holder, recipe, false);
            if (adjusted != null) {
                cir.setReturnValue(RecipeHelper.handleRecipe(holder, adjusted, io, adjusted.inputs, chanceCaches,
                        false, false));
            }
            return;
        }

        if (io == IO.OUT) {
            GTRecipe adjusted = GTNASpecialPartUtil.applyOutputBoosts(holder, recipe, false);
            if (adjusted != null) {
                cir.setReturnValue(RecipeHelper.handleRecipe(holder, adjusted, io, adjusted.outputs, chanceCaches,
                        false, false));
            }
        }
    }

    @Inject(method = "handleTickRecipeIO", at = @At("HEAD"), cancellable = true, remap = false)
    private static void gtna$handleSpecialTickRecipeIO(IRecipeCapabilityHolder holder, GTRecipe recipe, IO io,
                                                       Map<RecipeCapability<?>, Object2IntMap<?>> chanceCaches,
                                                       CallbackInfoReturnable<ActionResult> cir) {
        if (io == IO.IN) {
            GTRecipe adjusted = GTNASpecialPartUtil.stripNoConsumeInputs(holder, recipe, true);
            if (adjusted != null) {
                cir.setReturnValue(RecipeHelper.handleRecipe(holder, adjusted, io, adjusted.tickInputs, chanceCaches,
                        true, false));
            }
            return;
        }

        if (io == IO.OUT) {
            GTRecipe adjusted = GTNASpecialPartUtil.applyOutputBoosts(holder, recipe, true);
            if (adjusted != null) {
                cir.setReturnValue(RecipeHelper.handleRecipe(holder, adjusted, io, adjusted.tickOutputs, chanceCaches,
                        true, false));
            }
        }
    }

    private static ActionResult gtna$matchContents(IRecipeCapabilityHolder holder, GTRecipe recipe) {
        ActionResult result = RecipeHelper.handleRecipe(holder, recipe, IO.IN, recipe.inputs, Collections.emptyMap(),
                false, true);
        if (!result.isSuccess()) {
            return result;
        }
        result = RecipeHelper.handleRecipe(holder, recipe, IO.OUT, recipe.outputs, Collections.emptyMap(), false,
                true);
        if (!result.isSuccess()) {
            return result;
        }
        return gtna$matchTick(holder, recipe);
    }

    private static ActionResult gtna$matchTick(IRecipeCapabilityHolder holder, GTRecipe recipe) {
        if (!recipe.hasTick()) {
            return ActionResult.SUCCESS;
        }
        ActionResult result = RecipeHelper.handleRecipe(holder, recipe, IO.IN, recipe.tickInputs,
                Collections.emptyMap(), true, true);
        if (!result.isSuccess()) {
            return result;
        }
        return RecipeHelper.handleRecipe(holder, recipe, IO.OUT, recipe.tickOutputs, Collections.emptyMap(), true,
                true);
    }
}
