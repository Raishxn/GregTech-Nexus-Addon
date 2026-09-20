//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//
package com.raishxn.gtna.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;

import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.client.renderer.machine.AnnihilateGeneratorRenderer;
import com.raishxn.gtna.client.renderer.machine.EyeOfHarmonyRenderer;
import com.raishxn.gtna.client.renderer.machine.EyeOfWoodRenderer;
import com.raishxn.gtna.common.CommonProxy;
import dev.toma.configuration.Configuration;

@OnlyIn(Dist.CLIENT)
public class ClientProxy extends CommonProxy {

    @SuppressWarnings("removal")
    public ClientProxy() {
        // Client-only by construction (this class is loaded through DistExecutor on the client),
        // which is what keeps ConfigScreenHandler out of the dedicated-server class path.
        ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (mc, screen) -> Configuration.getConfigScreen(GTNACORE.MOD_ID, screen)));

        // Dynamic render codecs must exist before GTCEu starts baking machine models.
        // FMLClientSetupEvent runs too late for models that reference these IDs.
        var ignoredAnnihilate = AnnihilateGeneratorRenderer.TYPE;
        var ignoredHarmony = EyeOfHarmonyRenderer.TYPE;
        var ignoredWood = EyeOfWoodRenderer.TYPE;
        init();
    }
}
