//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//
package com.raishxn.gtna.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.raishxn.gtna.client.renderer.machine.AnnihilateGeneratorRenderer;
import com.raishxn.gtna.client.renderer.machine.EyeOfHarmonyRenderer;
import com.raishxn.gtna.client.renderer.machine.EyeOfWoodRenderer;
import com.raishxn.gtna.common.CommonProxy;

@OnlyIn(Dist.CLIENT)
public class ClientProxy extends CommonProxy {

    public ClientProxy() {
        // Dynamic render codecs must exist before GTCEu starts baking machine models.
        // FMLClientSetupEvent runs too late for models that reference these IDs.
        var ignoredAnnihilate = AnnihilateGeneratorRenderer.TYPE;
        var ignoredHarmony = EyeOfHarmonyRenderer.TYPE;
        var ignoredWood = EyeOfWoodRenderer.TYPE;
        init();
    }
}
