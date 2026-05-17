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
import com.raishxn.gtna.data.recipe.*;
import com.raishxn.gtna.data.recipe.GTNAWirelessCoverRecipes;

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
        GTNAHatchesRecipes.register(provider);
        GTNABlockRecipes.register(provider);
        GTNAGeneratesRecipes.register(provider);
        GTNAWoodCutterRecipes.register(provider);
        GTNAInfernalCokeRecipes.register(provider);
        GTNAHighPressureRecipes.register(provider);
        VoidminerRecipes.register(provider);
        GTNAWirelessCoverRecipes.register(provider);
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
    }
}
