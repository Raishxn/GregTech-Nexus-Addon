package com.raishxn.gtna.common.data;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.recipe.GTRecipeSerializer;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;
import com.raishxn.gtna.GTNACORE;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeType;

import static com.lowdragmc.lowdraglib.gui.texture.ProgressTexture.FillDirection.LEFT_TO_RIGHT;

public class GTNARecipeType {

    public static final String HYDRAULIC = "hydraulic";
    public static final GTRecipeType HYDRAULIC_MANUFACTURING = register("hydraulic_manufacturing", HYDRAULIC)
            .setMaxIOSize(9, 2, 3, 1)
            .setEUIO(IO.IN)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ASSEMBLER, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.BATH);

    public static final String SUPERHEATER_NAME = "superheater";
    public static final GTRecipeType SUPERHEATER_RECIPES = register("super_heater", SUPERHEATER_NAME)
            .setMaxIOSize(1,0,0,1)
            .setEUIO(IO.IN)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARC_FURNACE, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.ARC);

    public static final String WOODCUTTER = "woodcutter";
    public static final GTRecipeType WOODCUTTER_RECIPES = register("woodcutter", WOODCUTTER)
            .setMaxIOSize(1, 6,0,0)
            .setEUIO(IO.IN)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.SAW_TOOL);
    // No final do arquivo, antes de init()
    public static final String INFERNAL_COKE = "infernal_coke";
    public static final GTRecipeType INFERNAL_COKE_RECIPES = register("infernal_coke", INFERNAL_COKE)
            .setMaxIOSize(1, 1, 0, 1)
            .setEUIO(IO.IN)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.FURNACE);

    public static final String HIGH_PRESSURE_REACTOR = "high_pressure_reactor";
    public static final GTRecipeType HIGH_PRESSURE_REACTOR_RECIPES = register("high_pressure_reactor", HIGH_PRESSURE_REACTOR)
            .setMaxIOSize(0, 0, 2, 1)
            .setEUIO(IO.IN)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.ARC);

    public static final String SLAUGTHERHOUSE = "slaugterhouse";
    public static final GTRecipeType SLAUGHTERHOUSE_RECIPES = register("slaughterhouse", SLAUGTHERHOUSE)
            .setEUIO(IO.IN)
            .setMaxIOSize(1, 64, 0, 0)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.MACERATOR);



    public static GTRecipeType register(String name, String group, RecipeType<?>... proxyRecipes) {
        GTRecipeType recipeType = new GTRecipeType(GTNACORE.id(name), group, proxyRecipes);
        GTRegistries.register(BuiltInRegistries.RECIPE_TYPE, recipeType.registryName, recipeType);
        GTRegistries.register(BuiltInRegistries.RECIPE_SERIALIZER, recipeType.registryName, new GTRecipeSerializer());
        GTRegistries.RECIPE_TYPES.register(recipeType.registryName, recipeType);
        return recipeType;
    }

    public static void init() {
    }
}