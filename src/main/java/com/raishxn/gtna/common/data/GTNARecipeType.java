package com.raishxn.gtna.common.data;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.recipe.GTRecipeSerializer;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeType;

import com.raishxn.gtna.GTNACORE;

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
            .setMaxIOSize(1, 0, 0, 1)
            .setEUIO(IO.IN)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARC_FURNACE, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.ARC);

    public static final String WOODCUTTER = "woodcutter";
    public static final GTRecipeType WOODCUTTER_RECIPES = register("woodcutter", WOODCUTTER)
            .setMaxIOSize(1, 6, 0, 0)
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

    public static final String LAVA_MAKER = "lava_maker";
    public static final GTRecipeType LAVA_MAKER_RECIPES = register("lava_maker", LAVA_MAKER)
            .setMaxIOSize(1, 0, 0, 1)
            .setEUIO(IO.IN)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.FURNACE);

    /**
     * GTNL {@code CactusWonderFakeRecipes} port: one carbon fuel -> one steam grade. GTNL only used
     * this map for JEI, so GTNA promotes it to a real recipe type the Cactus Wonder can run.
     */
    public static final String CACTUS_WONDER = "cactus_wonder";
    public static final GTRecipeType CACTUS_WONDER_RECIPES = register("cactus_wonder", CACTUS_WONDER)
            .setMaxIOSize(1, 0, 0, 1)
            .setEUIO(IO.IN)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.FURNACE);

    /**
     * GTNA ore-processing chain (macerate → wash → thermal → centrifuge). Registered as a real
     * {@link GTRecipeType} so the Steam Ore Processor module and future multiblocks can run datapack
     * recipes against it, in the spirit of GTLAdditions/GTLCore's ore processors.
     */
    public static final String ORE_PROCESSING = "ore_processing";
    public static final GTRecipeType ORE_PROCESSING_RECIPES = register("ore_processing", ORE_PROCESSING)
            .setMaxIOSize(3, 6, 2, 0)
            .setEUIO(IO.IN)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.MACERATOR);

    public static final String HIGH_PRESSURE_REACTOR = "high_pressure_reactor";
    public static final GTRecipeType HIGH_PRESSURE_REACTOR_RECIPES = register("high_pressure_reactor",
            HIGH_PRESSURE_REACTOR)
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

    public static final String ARTIFICIAL_STAR = "annihilate_generator";
    public static final GTRecipeType ARTIFICIAL_STAR_RECIPES = register("annihilate_generator", ARTIFICIAL_STAR)
            .setMaxIOSize(1, 1, 0, 0)
            .setEUIO(IO.OUT)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.ARC);

    public static final String COSMOS_SIMULATION = "cosmos_simulation";
    public static final GTRecipeType COSMOS_SIMULATION_RECIPES = register("cosmos_simulation", COSMOS_SIMULATION)
            .setMaxIOSize(1, 120, 1, 18)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.SCIENCE);

    public static GTRecipeType register(String name, String group, RecipeType<?>... proxyRecipes) {
        GTRecipeType recipeType = new GTRecipeType(GTNACORE.id(name), group, proxyRecipes);
        GTRegistries.register(BuiltInRegistries.RECIPE_TYPE, recipeType.registryName, recipeType);
        GTRegistries.register(BuiltInRegistries.RECIPE_SERIALIZER, recipeType.registryName, new GTRecipeSerializer());
        GTRegistries.RECIPE_TYPES.register(recipeType.registryName, recipeType);
        return recipeType;
    }

    public static void init() {}
}
