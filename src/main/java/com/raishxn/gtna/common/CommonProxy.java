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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.common.data.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import com.raishxn.gtna.data.GTNALangProvider;
import com.raishxn.gtna.data.recipe.GTNARecipeConditions;
import com.raishxn.gtna.network.GTNANetworkHandler;

import java.util.concurrent.CompletableFuture;

import static com.raishxn.gtna.api.registry.GTNARegistry.REGISTRATE;

public class CommonProxy {

    @SuppressWarnings("removal")
    public CommonProxy() {
        CommonProxy.init();
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        REGISTRATE.registerEventListeners(eventBus);
        eventBus.addListener(this::commonSetup);
        eventBus.addListener(this::addMaterialRegistries);
        eventBus.addListener(this::addMaterials);
        eventBus.addListener(this::modifyMaterials);
        eventBus.addGenericListener(RecipeConditionType.class, this::registerRecipeConditions);
        eventBus.addGenericListener(GTRecipeType.class, this::registerRecipeTypes);
        eventBus.addGenericListener(MachineDefinition.class, this::registerMachines);
        eventBus.addListener(this::gatherData);

        // Server lifecycle events must be registered on the Forge event bus (not the mod bus).
        MinecraftForge.EVENT_BUS.addListener(this::onServerStarted);
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

    /**
     * Fires on the Forge event bus after the server has fully started.
     * Triggers a one-time sweep of all registered Nexus Flux Matrix entries,
     * pruning any that are missing or no longer formed so the QNT shows
     * accurate data immediately on world load.
     */
    private void onServerStarted(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld == null) {
            GTNACORE.LOGGER.warn("[GTNA] ServerStartedEvent: overworld is null, skipping startup matrix sweep.");
            return;
        }
        NexusEnergyNetwork network = NexusEnergyNetwork.get(overworld);
        network.sweepStaleMatrices(server);
    }

    public void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        boolean server = event.includeServer();
        boolean client = event.includeClient();
        generator.addProvider(client, new GTNALangProvider(packOutput));
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
