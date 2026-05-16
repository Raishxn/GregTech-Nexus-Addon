package com.raishxn.gtna.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.client.renderer.machine.AnnihilateGeneratorRenderer;
import com.raishxn.gtna.client.renderer.machine.EyeOfHarmonyRenderer;
import com.raishxn.gtna.client.renderer.machine.EyeOfWoodRenderer;
import com.raishxn.gtna.common.CommonProxy;
import dev.toma.configuration.Configuration;

@OnlyIn(Dist.CLIENT)
@SuppressWarnings("removal")
public class ClientProxy extends CommonProxy {

    public ClientProxy() {
        ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (mc, screen) -> Configuration.getConfigScreen(GTNACORE.MOD_ID, screen)));

        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        eventBus.addListener(this::clientSetup);
        eventBus.addListener(this::registerAdditionalModels);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(ClientProxy::registerDynamicRenderers);
    }

    private void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        registerDynamicRenderers();
        event.register(GTNACORE.id("obj/star"));
        event.register(GTNACORE.id("obj/space"));
        event.register(GTNACORE.id("obj/overworld"));
        event.register(GTNACORE.id("obj/the_nether"));
        event.register(GTNACORE.id("obj/the_end"));
        event.register(GTNACORE.id("obj/eye_of_wood_sweat"));
        event.register(GTNACORE.id("obj/eye_of_wood_thinking"));
        event.register(GTNACORE.id("block/machine/nexus_flux_matrix"));
    }

    private static void registerDynamicRenderers() {
        var ignoredAnnihilate = AnnihilateGeneratorRenderer.TYPE;
        var ignoredEyeOfHarmony = EyeOfHarmonyRenderer.TYPE;
        var ignoredEyeOfWood = EyeOfWoodRenderer.TYPE;
    }
}
