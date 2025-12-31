package com.raishxn.gtna.common;

import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.common.data.*;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.material.event.MaterialEvent;
import com.gregtechceu.gtceu.api.data.chemical.material.event.MaterialRegistryEvent;
import com.gregtechceu.gtceu.api.data.chemical.material.event.PostMaterialEvent;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.data.event.GatherDataEvent;
import com.raishxn.gtna.data.GTNALangProvider;
import com.raishxn.gtna.network.GTNANetworkHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;


import static com.raishxn.gtna.api.registry.GTNARegistry.REGISTRATE;

public class CommonProxy {

    public CommonProxy() {
        CommonProxy.init();
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        REGISTRATE.registerEventListeners(eventBus);
        eventBus.addListener(this::clientSetup);
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

    public void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        generator.addProvider(event.includeClient(), new GTNALangProvider(packOutput));
    }

    public void postRegistrationInitialization() {
        GTNANetworkHandler.INSTANCE.init();
    }

    private void clientSetup(final FMLClientSetupEvent event) {}

    // You MUST have this for custom materials.
    // Remember to register them not to GT's namespace, but your own.
    private void addMaterialRegistries(MaterialRegistryEvent event) {
        GTCEuAPI.materialManager.createRegistry(GTNACORE.MOD_ID);
    }

    // As well as this.
    private void addMaterials(MaterialEvent event) {GTNAMaterials.init();}

    // This is optional, though.
    private void modifyMaterials(PostMaterialEvent event) {}

    private void registerRecipeTypes(GTCEuAPI.RegisterEvent<ResourceLocation, GTRecipeType> event) {}

    private void registerRecipeConditions(GTCEuAPI.RegisterEvent<ResourceLocation, RecipeConditionType<?>> event) {}

    private void registerMachines(GTCEuAPI.RegisterEvent<ResourceLocation, MachineDefinition> event) {
    GTNAMachines.init();
    }
}