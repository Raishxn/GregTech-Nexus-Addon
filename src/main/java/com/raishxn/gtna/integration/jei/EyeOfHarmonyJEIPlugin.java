package com.raishxn.gtna.integration.jei;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.integration.jei.recipe.GTRecipeJEICategory;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import com.raishxn.gtna.common.data.GTNAMachines;
import com.raishxn.gtna.common.data.GTNARecipeType;
import com.raishxn.gtna.common.data.multiblock.EyeOfHarmonyOverworld;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;

@JeiPlugin
public final class EyeOfHarmonyJEIPlugin implements IModPlugin {

    private EyeOfHarmonyCategory category;

    private static boolean enabled() {
        return !GTCEu.Mods.isREILoaded() && !GTCEu.Mods.isEMILoaded();
    }

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation("gtna", "eye_of_harmony");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        if (enabled()) {
            category = new EyeOfHarmonyCategory(registration.getJeiHelpers().getGuiHelper());
            registration.addRecipeCategories(category);
        }
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        if (enabled()) registration.addRecipeCatalyst(GTNAMachines.EYE_OF_HARMONY.asStack(), EyeOfHarmonyCategory.TYPE);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        if (!enabled()) return;
        var level = Minecraft.getInstance().level;
        if (level == null) return;
        try {
            registration.addRecipes(EyeOfHarmonyCategory.TYPE,
                    com.raishxn.gtna.common.data.multiblock.EyeOfHarmonyDisplay
                            .pages(EyeOfHarmonyOverworld.build(level.getRecipeManager()), category.pageSize()));
        } catch (IllegalStateException exception) {
            GTCEu.LOGGER.warn("Eye of Harmony JEI catalog unavailable: {}", exception.getMessage());
        }
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        if (enabled()) runtime.getRecipeManager().hideRecipeCategory(
                GTRecipeJEICategory.TYPES.apply(GTNARecipeType.COSMOS_SIMULATION_RECIPES.getCategory()));
    }
}
