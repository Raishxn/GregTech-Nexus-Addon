package com.raishxn.gtna.common;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.material.event.MaterialEvent;
import com.gregtechceu.gtceu.api.data.chemical.material.event.MaterialRegistryEvent;
import com.gregtechceu.gtceu.api.data.chemical.material.event.PostMaterialEvent;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.gametest.framework.GameTestServer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.api.machine.multiblock.GTNASubPatterns;
import com.raishxn.gtna.client.renderer.machine.AnnihilateGeneratorRenderer;
import com.raishxn.gtna.client.renderer.machine.BallHatchRenderer;
import com.raishxn.gtna.client.renderer.machine.EyeOfHarmonyRenderer;
import com.raishxn.gtna.client.renderer.machine.EyeOfWoodRenderer;
import com.raishxn.gtna.common.data.*;
import com.raishxn.gtna.common.world.personalspace.PersonalSpaceChunkGenerators;
import com.raishxn.gtna.common.world.personalspace.PersonalSpaceConfig;
import com.raishxn.gtna.common.world.personalspace.PersonalSpaceEvents;
import com.raishxn.gtna.common.world.personalspace.PersonalSpacePortalRegistry;
import com.raishxn.gtna.data.GTNALangProvider;
import com.raishxn.gtna.data.recipe.GTNARecipeConditions;
import com.raishxn.gtna.gametest.GTNAGameTestReport;
import com.raishxn.gtna.integration.kubejs.GTNAKubeJSSubPatternLoader;
import com.raishxn.gtna.network.GTNANetworkHandler;
import com.raishxn.gtna.network.packet.SKubeModuleDescriptions;
import com.raishxn.gtna.research.KnowledgeEvents;

import java.util.concurrent.CompletableFuture;

import static com.raishxn.gtna.api.registry.GTNARegistry.REGISTRATE;

public class CommonProxy {

    public CommonProxy() {
        CommonProxy.init();
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        PersonalSpaceChunkGenerators.register(eventBus);
        PersonalSpacePortalRegistry.register(eventBus);
        REGISTRATE.registerEventListeners(eventBus);
        eventBus.addListener(this::clientSetup);
        eventBus.addListener(this::registerAdditionalModels);
        eventBus.addListener(this::commonSetup);
        eventBus.addListener(this::addMaterialRegistries);
        eventBus.addListener(this::addMaterials);
        eventBus.addListener(this::modifyMaterials);
        eventBus.addGenericListener(RecipeConditionType.class, this::registerRecipeConditions);
        eventBus.addGenericListener(GTRecipeType.class, this::registerRecipeTypes);
        eventBus.addGenericListener(MachineDefinition.class, this::registerMachines);
        eventBus.addGenericListener(com.gregtechceu.gtceu.api.data.DimensionMarker.class,
                (GTCEuAPI.RegisterEvent<ResourceLocation, com.gregtechceu.gtceu.api.data.DimensionMarker> event) -> com.raishxn.gtna.GTNAGTAddon
                        .registerPlanetDimensionMarkers());
        eventBus.addListener(this::gatherData);
        MinecraftForge.EVENT_BUS.addListener(EyeOfHarmonyLegacyMappings::remap);
        MinecraftForge.EVENT_BUS.addListener(this::serverStarting);
        PersonalSpaceConfig.load();
        PersonalSpaceEvents.register();
        MinecraftForge.EVENT_BUS.addListener(this::playerLoggedIn);
        KnowledgeEvents.register(MinecraftForge.EVENT_BUS);
    }

    public static void init() {
        GTNACreativeModeTabs.init();
    }

    private void serverStarting(ServerStartingEvent event) {
        if (event.getServer() instanceof GameTestServer) {
            GTNAGameTestReport.install();
        }
        if (ModList.get().isLoaded("kubejs")) {
            GTNAKubeJSSubPatternLoader.load();
        }
    }

    private void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            GTNANetworkHandler.sendToPlayer(new SKubeModuleDescriptions(GTNASubPatterns.kubeDescriptions()), player);
        }
    }

    /**
     * FMLCommonSetupEvent — registers network packets.
     * This was previously in postRegistrationInitialization() but never called.
     */
    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(GTNANetworkHandler::init);
        event.enqueueWork(com.raishxn.gtna.data.recipe.GTNAGodforgeProgression::registerExtraCosts);
        if (ModList.get().isLoaded("kubejs")) {
            event.enqueueWork(com.raishxn.gtna.integration.kubejs.GTNAStartupEvents::postEyeOfHarmony);
        }
        if (ModList.get().isLoaded("ftbquests")) {
            event.enqueueWork(com.raishxn.gtna.integration.ftb.GTNAQuestTypes::init);
        }
    }

    public void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        boolean server = event.includeServer();
        boolean client = event.includeClient();
        // Single en_us writer: GTNA translations are merged into Registrate's lang provider.
        com.raishxn.gtna.api.registry.GTNARegistry.REGISTRATE.addDataGenerator(
                com.tterrag.registrate.providers.ProviderType.LANG, lang -> GTNALangProvider.feed(lang, packOutput));
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            registerDynamicRenderers();
        });
    }

    private void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        registerDynamicRenderers();
        event.register(GTNACORE.id("obj/star"));
        event.register(GTNACORE.id("obj/space"));
        event.register(GTNACORE.id("obj/overworld"));
        event.register(GTNACORE.id("obj/the_nether"));
        event.register(GTNACORE.id("obj/the_end"));
        for (String planet : new String[] { "moon", "mars", "venus", "mercury", "glacio" })
            event.register(GTNACORE.id("obj/planet_" + planet));
        event.register(GTNACORE.id("obj/eye_of_wood_sweat"));
        event.register(GTNACORE.id("obj/eye_of_wood_thinking"));
    }

    private static void registerDynamicRenderers() {
        var ignoredAnnihilate = AnnihilateGeneratorRenderer.TYPE;
        var ignoredEyeOfHarmony = EyeOfHarmonyRenderer.TYPE;
        var ignoredEyeOfWood = EyeOfWoodRenderer.TYPE;
        var ignoredBallHatch = BallHatchRenderer.TYPE;
        var ignoredForgeOfGods = com.raishxn.gtna.client.renderer.machine.ForgeOfGodsRenderer.TYPE;
    }

    // You MUST have this for custom materials.
    // Remember to register them not to GT's namespace, but your own.
    private void addMaterialRegistries(MaterialRegistryEvent event) {
        GTCEuAPI.materialManager.createRegistry(GTNACORE.MOD_ID);
    }

    // As well as this.
    private void addMaterials(MaterialEvent event) {
        GTNAMaterials.init();
    }

    // This is optional, though.
    private void modifyMaterials(PostMaterialEvent event) {
        com.raishxn.gtna.common.data.worldgen.GTNAPlanetOres.modifyMaterials();
    }

    private void registerRecipeTypes(GTCEuAPI.RegisterEvent<ResourceLocation, GTRecipeType> event) {
        GTNARecipeType.init();
    }

    private void registerRecipeConditions(GTCEuAPI.RegisterEvent<ResourceLocation, RecipeConditionType<?>> event) {
        GTNARecipeConditions.init();
    }

    private void registerMachines(GTCEuAPI.RegisterEvent<ResourceLocation, MachineDefinition> event) {
        GTNAMachines.init();
        GTNAMachines2.init();
        GTNAMachines3.init();
        GTNAAdvancedFusion.init();
        com.raishxn.gtna.common.data.GTNAPlasmaForge.init();
        com.raishxn.gtna.common.data.GTNATranscendentPlasmaMixer.init();
        com.raishxn.gtna.common.data.GTNANanoForge.init();
        com.raishxn.gtna.common.data.GTNAGodforgeComponents.init();
        com.raishxn.gtna.common.data.GTNACircuits.init();
        GTNAEnergyHatches.init();
        // Append the shared high-pressure line before the source attribution, so the tooltip reads
        // stats -> high pressure -> Source.
        GTNASteamTooltips.applyAll();
        // Append the ported-content attribution line once every GTNA machine is registered.
        GTNASources.applyAll();
    }
}
