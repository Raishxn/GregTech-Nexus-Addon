package com.raishxn.gtna.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;

import com.raishxn.gtna.client.hud.HudEditorScreen;
import com.raishxn.gtna.client.hud.WirelessEnergyHudBridge;
import com.raishxn.gtna.client.hud.WirelessEnergyHudOverlay;
import com.raishxn.gtna.client.hud.WirelessSteamHudBridge;
import com.raishxn.gtna.client.hud.WirelessSteamHudOverlay;
import com.raishxn.gtna.client.renderer.machine.AnnihilateGeneratorRenderer;
import com.raishxn.gtna.client.renderer.machine.BallHatchRenderer;
import com.raishxn.gtna.client.renderer.machine.EyeOfHarmonyRenderer;
import com.raishxn.gtna.client.renderer.machine.EyeOfWoodRenderer;
import com.raishxn.gtna.common.CommonProxy;

@OnlyIn(Dist.CLIENT)
public class ClientProxy extends CommonProxy {

    public ClientProxy() {
        // The configuration library registers the config screen for every mod itself (see
        // ConfigurationForge#clientInit). GTNA used to register its own factory against the old
        // Configuration#getConfigScreen, which no longer exists in the 3.1.0 the pack runs - so that
        // registration is gone.

        // The wireless steam hatch UI is common code, so it reaches the client-only HUD through
        // this hook instead of referencing a client class directly (dedicated-server safety).
        WirelessSteamHudBridge.toggleHud = () -> {
            WirelessSteamHudOverlay hud = WirelessSteamHudOverlay.INSTANCE;
            hud.setEnabled(!hud.isEnabled());
        };
        WirelessSteamHudBridge.openEditor = () -> Minecraft.getInstance().setScreen(new HudEditorScreen());
        WirelessEnergyHudBridge.toggleHud = () -> {
            var hud = WirelessEnergyHudOverlay.INSTANCE;
            hud.setEnabled(!hud.isEnabled());
        };
        WirelessEnergyHudBridge.openEditor = () -> Minecraft.getInstance().setScreen(new HudEditorScreen());

        // Dynamic render codecs must exist before GTCEu starts baking machine models.
        // FMLClientSetupEvent runs too late for models that reference these IDs.
        var ignoredAnnihilate = AnnihilateGeneratorRenderer.TYPE;
        var ignoredHarmony = EyeOfHarmonyRenderer.TYPE;
        var ignoredWood = EyeOfWoodRenderer.TYPE;
        var ignoredBallHatch = BallHatchRenderer.TYPE;
        var ignoredForgeOfGods = com.raishxn.gtna.client.renderer.machine.ForgeOfGodsRenderer.TYPE;
        MinecraftForge.EVENT_BUS.addListener(
                com.raishxn.gtna.client.renderer.machine.ForgeOfGodsRenderer::onRenderLevelStage);
        net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus()
                .addListener((net.minecraftforge.client.event.RegisterShadersEvent event) -> {
                    try {
                        com.raishxn.gtna.client.renderer.machine.GodforgeShaders.register(event);
                    } catch (java.io.IOException e) {
                        throw new java.io.UncheckedIOException(e);
                    }
                });
        MinecraftForge.EVENT_BUS.register(ModuleCountClientHandler.class);
        MinecraftForge.EVENT_BUS.register(EyeOfHarmonyTooltips.class);
        MinecraftForge.EVENT_BUS.register(com.raishxn.gtna.client.research.KnowledgeClientHandler.class);
        MinecraftForge.EVENT_BUS.register(com.raishxn.gtna.client.research.ResearchKeys.class);
        net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus()
                .addListener(com.raishxn.gtna.client.research.ResearchKeys::register);
        com.raishxn.gtna.client.personalspace.PersonalSpaceClient
                .init(net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus());
        init();
    }
}
