package com.raishxn.gtna.mixin.kubejs;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.fml.ModList;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** KubeJS leaves a non-daemon script executor alive after Forge's data generators finish. */
@Mixin(DataGenerator.class)
public abstract class DataGeneratorMixin {

    private static final Logger GTNA_LOGGER = LogUtils.getLogger();

    // No refmap is packaged for this mixin. Dev uses the mapped name; production uses SRG.
    // Data generation cleanup must never become a mandatory client startup injection.
    @Inject(method = { "run", "m_123917_" }, at = @At("TAIL"), remap = false, require = 0)
    private void gtna$stopKubeJSBackgroundThread(CallbackInfo ci) {
        if (ModList.get().isLoaded("kubejs")) {
            try {
                Class<?> thread = Class.forName("dev.latvian.mods.kubejs.util.KubeJSBackgroundThread");
                thread.getField("running").setBoolean(null, false);
            } catch (ReflectiveOperationException | LinkageError e) {
                GTNA_LOGGER.warn("Could not shut down KubeJS after data generation", e);
            }
        }
    }
}
