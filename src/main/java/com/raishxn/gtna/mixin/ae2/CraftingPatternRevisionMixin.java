package com.raishxn.gtna.mixin.ae2;

import appeng.me.service.helpers.NetworkCraftingProviders;
import com.raishxn.gtna.planner.neoforge.crafting.PlannerRevisionSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = NetworkCraftingProviders.class, remap = false)
public abstract class CraftingPatternRevisionMixin implements PlannerRevisionSource {

    @Unique
    private long raishxcore$patternRevision;

    @Inject(method = "setLastModifiedOnTick", at = @At("HEAD"))
    private void raishxcore$advanceRevision(CallbackInfo ci) {
        raishxcore$patternRevision = Math.incrementExact(raishxcore$patternRevision);
    }

    @Override
    public long raishxcore$getPatternRevision() {
        return raishxcore$patternRevision;
    }
}
