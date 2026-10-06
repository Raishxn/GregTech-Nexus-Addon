package com.raishxn.gtna.mixin.client;

import net.minecraft.resources.ResourceLocation;

import com.raishxn.gtna.research.ClientKnowledge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Method;

/**
 * Makes room in the EMI recipe panel for the research lines {@code GTRecipeWidgetMixin} adds. EMI sizes the
 * panel from this value, so without it the extra line pushes the last data lines out of the frame. The
 * target is named by string because EMI is not a compile dependency; the recipe id comes from
 * {@code EmiRecipe#getId} by reflection, only for GTCEu's own recipe class.
 */
@Mixin(targets = "com.lowdragmc.lowdraglib.emi.ModularEmiRecipe", remap = false)
public abstract class ModularEmiRecipeMixin {

    /** Same value as {@code GTRecipeWidget.LINE_HEIGHT}. */
    @Unique
    private static final int GTNA$LINE_HEIGHT = 10;

    @Unique
    private static Method gtna$getId;

    @Unique
    private static boolean gtna$lookedUp;

    @Inject(method = "getDisplayHeight", at = @At("RETURN"), cancellable = true)
    private void gtna$makeRoomForResearch(CallbackInfoReturnable<Integer> cir) {
        if (!getClass().getName().equals("com.gregtechceu.gtceu.integration.emi.recipe.GTEmiRecipe")) return;
        if (ClientKnowledge.nodes().isEmpty()) return;
        try {
            if (!gtna$lookedUp) {
                gtna$getId = getClass().getMethod("getId");
                gtna$lookedUp = true;
            }
            if (gtna$getId == null) return;
            Object id = gtna$getId.invoke(this);
            if (id instanceof ResourceLocation recipeId) {
                int lines = ClientKnowledge.gatesFor(recipeId).size();
                if (lines > 0) cir.setReturnValue(cir.getReturnValue() + lines * GTNA$LINE_HEIGHT);
            }
        } catch (ReflectiveOperationException unavailable) {
            gtna$lookedUp = true;
            gtna$getId = null;
        }
    }
}
