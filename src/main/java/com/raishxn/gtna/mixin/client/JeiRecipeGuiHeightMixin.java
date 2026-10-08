package com.raishxn.gtna.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Optional JEI: JEI caps its recipe window with the {@code RecipeGuiHeight} setting (350 by default), which is
 * shorter than the Eye of Harmony page (full 9-row item grid, fluid grid and program info). Raise only the lower
 * bound so the outer frame grows to fit; a larger user setting is kept, and JEI still clamps to the screen.
 */
@Pseudo
@Mixin(targets = "mezz.jei.common.config.ClientConfig", remap = false)
public abstract class JeiRecipeGuiHeightMixin {

    @Inject(method = "getMaxRecipeGuiHeight", at = @At("RETURN"), cancellable = true, require = 0)
    private void gtna$fitEyeOfHarmonyPage(CallbackInfoReturnable<Integer> cir) {
        int needed = com.raishxn.gtna.integration.jei.EyeOfHarmonyCategory.FULL_HEIGHT + 64;
        if (cir.getReturnValue() < needed) cir.setReturnValue(needed);
    }
}
