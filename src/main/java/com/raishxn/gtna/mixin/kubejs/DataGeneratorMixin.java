package com.raishxn.gtna.mixin.kubejs;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.fml.ModList;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** KubeJS leaves a non-daemon script executor alive after Forge's data generators finish. */
@Mixin(DataGenerator.class)
public abstract class DataGeneratorMixin {

    @Inject(method = "run", at = @At("TAIL"))
    private void gtna$stopKubeJSBackgroundThread(CallbackInfo ci) {
        if (ModList.get().isLoaded("kubejs")) {
            try {
                Class<?> thread = Class.forName("dev.latvian.mods.kubejs.util.KubeJSBackgroundThread");
                thread.getField("running").setBoolean(null, false);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Could not shut down KubeJS after data generation", e);
            }
        }
    }
}
