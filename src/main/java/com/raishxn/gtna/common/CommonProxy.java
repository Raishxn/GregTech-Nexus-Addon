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
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.client.renderer.machine.AnnihilateGeneratorRenderer;
import com.raishxn.gtna.client.renderer.machine.EyeOfHarmonyRenderer;
import com.raishxn.gtna.client.renderer.machine.EyeOfWoodRenderer;
import com.raishxn.gtna.common.data.*;
import com.raishxn.gtna.data.GTNALangProvider;
import com.raishxn.gtna.data.recipe.GTNARecipeConditions;
import com.raishxn.gtna.network.GTNANetworkHandler;

import java.util.concurrent.CompletableFuture;

import static com.raishxn.gtna.api.registry.GTNARegistry.REGISTRATE;

public class CommonProxy {

    public CommonProxy() {
        CommonProxy.init();
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
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
        eventBus.addListener(this::gatherData);
    }

    public static void init() {
        GTNACreativeModeTabs.init();
    }

    /**
     * FMLCommonSetupEvent — registers network packets.
     * This was previously in postRegistrationInitialization() but never called.
     */
    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(GTNANetworkHandler::init);
    }

    public void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        boolean server = event.includeServer();
        boolean client = event.includeClient();
        generator.addProvider(client, new GTNALangProvider(packOutput));
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
        event.register(GTNACORE.id("obj/eye_of_wood_sweat"));
        event.register(GTNACORE.id("obj/eye_of_wood_thinking"));
    }

    private static void registerDynamicRenderers() {
        var ignoredAnnihilate = AnnihilateGeneratorRenderer.TYPE;
        var ignoredEyeOfHarmony = EyeOfHarmonyRenderer.TYPE;
        var ignoredEyeOfWood = EyeOfWoodRenderer.TYPE;
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
    private void modifyMaterials(PostMaterialEvent event) {}

    private void registerRecipeTypes(GTCEuAPI.RegisterEvent<ResourceLocation, GTRecipeType> event) {
        GTNARecipeType.init();
    }

    private void registerRecipeConditions(GTCEuAPI.RegisterEvent<ResourceLocation, RecipeConditionType<?>> event) {
        GTNARecipeConditions.init();
    }

    private void registerMachines(GTCEuAPI.RegisterEvent<ResourceLocation, MachineDefinition> event) {
        GTNAMachines.init();
        GTNAMachines2.init();
        GTNAEnergyHatches.init();
    }
}
