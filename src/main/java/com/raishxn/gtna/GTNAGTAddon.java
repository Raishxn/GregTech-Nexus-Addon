package com.raishxn.gtna;

import com.gregtechceu.gtceu.api.addon.GTAddon;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.lowdragmc.lowdraglib.Platform;
import com.raishxn.gtna.api.registry.GTNARegistry;
import com.raishxn.gtna.common.data.GTNABlocks;
import com.raishxn.gtna.common.data.GTNAItems;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
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
        //GTNABlocks.init();
    }

    @Override
    public void registerSounds() {}

    @Override
    public void registerCovers() {}

    @Override
    public void registerElements() {}

    @Override
    public void registerTagPrefixes() {}

    @Override
    public void addRecipes(Consumer<FinishedRecipe> provider) {}

    @Override
    public void removeRecipes(Consumer<ResourceLocation> consumer) {}

    @Override
    public void registerFluidVeins() {if (!Platform.isDevEnv()) {}}
}
