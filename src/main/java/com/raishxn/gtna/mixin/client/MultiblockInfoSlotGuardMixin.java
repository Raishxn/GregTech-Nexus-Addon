package com.raishxn.gtna.mixin.client;

import com.lowdragmc.lowdraglib.gui.widget.Widget;

import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * GTCEu 7.5.3 keeps JEI's initial slot list when a multiblock preview changes pages, but rebuilds
 * its widget list. A stale slot index then crashes JEI while hovering a Component Assembler module.
 */
@Mixin(targets = "com.gregtechceu.gtceu.integration.jei.multipage.MultiblockInfoCategory$1ProxyRecipeWidget",
       remap = false)
public abstract class MultiblockInfoSlotGuardMixin {

    @Inject(method = "lambda$getSlotUnderMouse$0", at = @At("HEAD"), cancellable = true)
    private static void gtna$skipStalePreviewSlot(List<Widget> widgets, double mouseX, double mouseY,
                                                  IRecipeSlotDrawable slot, CallbackInfoReturnable<Boolean> cir) {
        var name = slot.getSlotName();
        if (name.isEmpty()) return;
        try {
            int index = Integer.parseInt(name.get().substring(5));
            if (index < 0 || index >= widgets.size()) cir.setReturnValue(false);
        } catch (IndexOutOfBoundsException | NumberFormatException exception) {
            cir.setReturnValue(false);
        }
    }
}
