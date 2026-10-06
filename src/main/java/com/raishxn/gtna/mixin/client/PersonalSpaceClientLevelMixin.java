package com.raishxn.gtna.mixin.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;

import com.raishxn.gtna.client.personalspace.PersonalSpaceEffects;
import com.raishxn.gtna.common.world.personalspace.PersonalSpaceSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Fixed sun or moon in a personal dimension: the sky takes the configured color, stars the configured brightness and
 * the sun brightness is 1.0 by day or 0.2 by night, like the original {@code PersonalWorldProvider}.
 */
@Mixin(ClientLevel.class)
public abstract class PersonalSpaceClientLevelMixin {

    private PersonalSpaceSettings gtna$fixedSky() {
        PersonalSpaceSettings settings = PersonalSpaceEffects.current((ClientLevel) (Object) this);
        return settings != null && settings.getDaylightCycle() != PersonalSpaceSettings.DaylightCycle.CYCLE ?
                settings : null;
    }

    @Inject(method = "getSkyColor", at = @At("HEAD"), cancellable = true)
    private void gtna$personalSkyColor(Vec3 pos, float partialTick, CallbackInfoReturnable<Vec3> cir) {
        PersonalSpaceSettings settings = gtna$fixedSky();
        if (settings != null) cir.setReturnValue(PersonalSpaceEffects.skyColor(settings));
    }

    @Inject(method = "getStarBrightness", at = @At("HEAD"), cancellable = true)
    private void gtna$personalStarBrightness(float partialTick, CallbackInfoReturnable<Float> cir) {
        PersonalSpaceSettings settings = gtna$fixedSky();
        if (settings != null) cir.setReturnValue(settings.getStarBrightness());
    }

    @Inject(method = "getSkyDarken", at = @At("HEAD"), cancellable = true)
    private void gtna$personalSunBrightness(float partialTick, CallbackInfoReturnable<Float> cir) {
        PersonalSpaceSettings settings = gtna$fixedSky();
        if (settings != null) cir.setReturnValue(settings.isNightTime() ? 0.2F : 1.0F);
    }
}
