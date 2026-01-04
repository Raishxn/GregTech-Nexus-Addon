package com.raishxn.gtna.data.recipe;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.raishxn.gtna.common.data.GTNAMaterials;
import net.minecraft.data.recipes.FinishedRecipe;

import java.util.function.Consumer;

public class GTNAMaterialRecipes {

    public static void register(Consumer<FinishedRecipe> provider) {

        // --- Stronze (Alloy Smelter) ---
        GTRecipeTypes.ALLOY_SMELTER_RECIPES.recipeBuilder("stronze_alloy")
                .inputItems(TagPrefix.ingot, GTMaterials.Bronze, 1)
                .inputItems(TagPrefix.ingot, GTMaterials.Steel, 2)
                .outputItems(TagPrefix.ingot, GTNAMaterials.Stronze, 3)
                .duration(200)
                .EUt(GTValues.LV)
                .save(provider);

        // --- Breel (Mixer) ---
        GTRecipeTypes.MIXER_RECIPES.recipeBuilder("breel_dust_mixing")
                .inputItems(TagPrefix.dust, GTMaterials.Bronze, 2)
                .inputItems(TagPrefix.dust, GTMaterials.Steel, 1)
                .outputItems(TagPrefix.dust, GTNAMaterials.Breel, 3)
                .duration(100)
                .EUt(GTValues.LV)
                .save(provider);

        // --- Clay Compound (Mixer) ---
        GTRecipeTypes.MIXER_RECIPES.recipeBuilder("clay_compound_mixing")
                .inputItems(TagPrefix.dust, GTMaterials.Flint, 1)
                .inputItems(TagPrefix.dust, GTMaterials.Clay, 1)
                .inputItems(TagPrefix.dust, GTMaterials.Stone, 1)
                .outputItems(TagPrefix.dust, GTNAMaterials.ClayCompound, 2)
                .duration(80)
                .EUt(GTValues.LV)
                .save(provider);

        // --- Compressed Steam Ingot (Compressor) ---
        GTRecipeTypes.FLUID_SOLIDFICATION_RECIPES.recipeBuilder("compressed_steam_ingot")
                .notConsumable(GTItems.SHAPE_MOLD_INGOT)
                .inputFluids(GTNAMaterials.DenseSupercriticalSteam.getFluid(1000))
                .outputItems(TagPrefix.ingot, GTNAMaterials.CompressedSteam, 1)
                .duration(300)
                .EUt(GTValues.HV)
                .save(provider);
        GTRecipeTypes.FLUID_SOLIDFICATION_RECIPES.recipeBuilder("compressed_steam_ingot_2")
                .notConsumable(GTItems.SHAPE_MOLD_INGOT)
                .inputFluids(GTMaterials.Steam.getFluid(15000))
                .outputItems(TagPrefix.ingot, GTNAMaterials.CompressedSteam, 1)
                .duration(900)
                .EUt(GTValues.ULV)
                .save(provider);

        GTRecipeTypes.BLAST_RECIPES.recipeBuilder("echoite_alloy_smelting")
                .inputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Steel), 4)
                .inputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.EnderPearl), 2)
                .inputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Obsidian), 2)
                .inputFluids(GTNAMaterials.SuperHeatedSteam.getFluid(1000))
                .outputItems(ChemicalHelper.get(TagPrefix.ingot, GTNAMaterials.Echoite), 4)
                .blastFurnaceTemp(1730)
                .duration(1200)
                .EUt(120)
                .save(provider);
    }
}