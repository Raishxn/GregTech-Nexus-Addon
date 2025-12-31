/*package com.raishxn.gtna.data.recipe;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.raishxn.gtna.common.data.GTNAMaterials;

import net.minecraft.data.recipes.FinishedRecipe;
import java.util.function.Consumer;

public class GTNAMaterialRecipes {
    public static void init(Consumer<FinishedRecipe> provider) {

        // --- Stronze (Alloy Smelter) ---
        // 1 Bronze + 2 Steel = 3 Stronze
        GTRecipeTypes.ALLOY_SMELTER_RECIPES.recipeBuilder("stronze_alloy")
                .inputItems(TagPrefix.ingot, GTMaterials.Bronze, 1)
                .inputItems(TagPrefix.ingot, GTMaterials.Steel, 2)
                .outputItems(TagPrefix.ingot, GTNAMaterials.Stronze, 3)
                .duration(200)
                .EUt(GTValues.LV)
                .save(provider);

        // --- Breel (Mixer) ---
        // 2 Bronze Dust + 1 Steel Dust = 3 Breel Dust
        GTRecipeTypes.MIXER_RECIPES.recipeBuilder("breel_dust_mixing")
                .inputItems(TagPrefix.dust, GTMaterials.Bronze, 2)
                .inputItems(TagPrefix.dust, GTMaterials.Steel, 1)
                .outputItems(TagPrefix.dust, GTNAMaterials.Breel, 3)
                .duration(100)
                .EUt(GTValues.LV)
                .save(provider);

        // --- Clay Compound (Mixer) ---
        // 1 Flint + 1 Clay + 1 Stone = 2 Clay Compound
        GTRecipeTypes.MIXER_RECIPES.recipeBuilder("clay_compound_mixing")
                .inputItems(TagPrefix.dust, GTMaterials.Flint, 1)
                .inputItems(TagPrefix.dust, GTMaterials.Clay, 1)
                .inputItems(TagPrefix.dust, GTMaterials.Stone, 1)
                .outputItems(TagPrefix.dust, GTNAMaterials.ClayCompound, 2)
                .duration(80)
                .EUt(GTValues.LV)
                .save(provider);

        // --- Compressed Steam Ingot (Compressor) ---
        // Dense Supercritical Steam + Steam -> Ingot
        GTRecipeTypes.COMPRESSOR_RECIPES.recipeBuilder("compressed_steam_ingot")
                .inputFluids(GTNAMaterials.DenseSupercriticalSteam.getFluid(1000))
                .inputFluids(GTMaterials.Steam.getFluid(1000))
                .outputItems(TagPrefix.ingot, GTNAMaterials.CompressedSteam, 1)
                .duration(300)
                .EUt(GTValues.HV)
                .save(provider);
    }
}*/