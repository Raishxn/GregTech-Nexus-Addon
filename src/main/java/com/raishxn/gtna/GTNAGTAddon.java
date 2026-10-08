package com.raishxn.gtna;

import com.gregtechceu.gtceu.api.addon.GTAddon;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;

import com.lowdragmc.lowdraglib.Platform;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;

import com.raishxn.gtna.api.data.info.GTNAMaterialFlags;
import com.raishxn.gtna.api.data.tag.GTNATagPrefix;
import com.raishxn.gtna.api.registry.GTNARegistry;
import com.raishxn.gtna.common.data.*;
import com.raishxn.gtna.common.data.worldgen.GTNAPlanetOres;
import com.raishxn.gtna.data.recipe.*;

import java.util.function.Consumer;

@GTAddon
public class GTNAGTAddon implements IGTAddon {

    @Override
    public String addonModId() {
        return GTNACORE.MOD_ID;
    }

    @Override
    public GTRegistrate getRegistrate() {
        return GTNARegistry.REGISTRATE;
    }

    @Override
    public boolean requiresHighTier() {
        return true;
    }

    @Override
    public void initializeAddon() {
        GTNAItems.init();
        GTNAMachines.init();
        GTNAMachines3.init();
        GTNAModules.init();
    }

    /**
     * GTCEu shows dimension conditions (World Data Scanner, ore veins…) through dimension markers; without one an Ad
     * Astra planet appears as a raw ResourceKey. The GTNA Planet Blocks are the icons, tiers follow the rocket tiers.
     */
    public static void registerPlanetDimensionMarkers() {
        String[][] planets = { { "moon", "1" }, { "mars", "2" }, { "venus", "3" }, { "mercury", "3" },
                { "glacio", "4" } };
        for (String[] planet : planets) {
            new com.gregtechceu.gtceu.api.data.DimensionMarker(Integer.parseInt(planet[1]),
                    GTNACORE.id("eye_of_harmony_planet_" + planet[0]), null)
                    .register(new net.minecraft.resources.ResourceLocation("ad_astra", planet[0]));
        }
    }

    @Override
    public void registerSounds() {}

    @Override
    public void registerCovers() {
        GTNACovers.init();
    }

    @Override
    public void registerElements() {
        GTNAElements.init();
    }

    @Override
    public void addRecipes(Consumer<FinishedRecipe> provider) {
        GTNAMaterialRecipes.register(provider);
        GTNAItemRecipes.register(provider);
        GTNAMachineRecipes.register(provider);
        GTNAGreenhouseRecipes.register(provider);
        GTNAComponentRecipes.register(provider);
        GTNAAtomizationRecipes.register(provider);
        GTNAIsaMillRecipes.register(provider);
        GTNAFlotationDryingRecipes.register(provider);
        GTNARocketFuelRecipes.register(provider);
        GTNASupercriticalSteamTurbineRecipes.register(provider);
        GTNATreeGrowthRecipes.register(provider);
        GTNAHatchesRecipes.register(provider);
        GTNABlockRecipes.register(provider);
        GTNAGeneratesRecipes.register(provider);
        GTNAWoodCutterRecipes.register(provider);
        GTNAInfernalCokeRecipes.register(provider);
        GTNAHighPressureRecipes.register(provider);
        GTNALavaMakerRecipes.register(provider);
        VoidminerRecipes.register(provider);
        com.raishxn.gtna.data.recipe.GTNAGodforgeRecipes.register(provider);
        com.raishxn.gtna.data.recipe.GTNAGodforgeChainRecipes.register(provider);
        com.raishxn.gtna.data.recipe.GTNAHighTierComponentRecipes.register(provider);
        com.raishxn.gtna.data.recipe.GTNAAdvancedFusionRecipes.register(provider);
        com.raishxn.gtna.data.recipe.GTNAGodforgeProgression.register(provider);
        com.raishxn.gtna.data.recipe.GTNAGodforgeProgression.registerExtraCosts();
        com.raishxn.gtna.data.recipe.GTNAEyeOfHarmonyProgression.register(provider);
    }

    @Override
    public void removeRecipes(Consumer<ResourceLocation> consumer) {}

    @Override
    public void registerFluidVeins() {
        if (!Platform.isDevEnv()) {}
    }

    @Override
    public void registerTagPrefixes() {
        GTNAMaterialFlags.register();
        GTNATagPrefix.register();
        GTNAPlanetOres.registerTagPrefixes();
    }

    @Override
    public void registerWorldgenLayers() {
        GTNAPlanetOres.registerWorldgenLayers();
    }

    @Override
    public void registerOreVeins() {
        GTNAPlanetOres.registerOreVeins();
    }
}
