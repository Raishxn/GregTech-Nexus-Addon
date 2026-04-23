package com.raishxn.gtna.data.recipe;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import net.minecraft.data.recipes.FinishedRecipe;

import com.raishxn.gtna.common.data.GTNAMaterials;

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

        GTRecipeTypes.MIXER_RECIPES.recipeBuilder("hastelloy_n_dust")
                .inputItems(TagPrefix.dust, GTMaterials.Iridium, 2)
                .inputItems(TagPrefix.dust, GTMaterials.Molybdenum, 4)
                .inputItems(TagPrefix.dust, GTMaterials.Chromium, 2)
                .inputItems(TagPrefix.dust, GTMaterials.Titanium, 2)
                .inputItems(TagPrefix.dust, GTMaterials.Nickel, 15)
                .circuitMeta(5)
                .outputItems(TagPrefix.dust, GTNAMaterials.HastelloyN, 25)
                .EUt(1920)
                .duration(1000)
                .save(provider);

        GTRecipeTypes.MIXER_RECIPES.recipeBuilder("aluminium_bronze_dust")
                .inputItems(TagPrefix.dust, GTMaterials.Aluminium)
                .inputItems(TagPrefix.dust, GTMaterials.Bronze, 6)
                .circuitMeta(1)
                .outputItems(TagPrefix.dust, GTNAMaterials.AluminiumBronze, 7)
                .EUt(30)
                .duration(400)
                .save(provider);

        GTRecipeTypes.MIXER_RECIPES.recipeBuilder("dark_steel_dust")
                .inputItems(TagPrefix.dust, GTMaterials.Iron)
                .inputItems(TagPrefix.dust, GTMaterials.Coal)
                .inputItems(TagPrefix.dust, GTMaterials.Obsidian)
                .circuitMeta(3)
                .outputItems(TagPrefix.dust, GTNAMaterials.DarkSteel, 3)
                .EUt(30)
                .duration(200)
                .save(provider);

        GTRecipeTypes.MIXER_RECIPES.recipeBuilder("end_steel_dust")
                .inputItems(TagPrefix.dust, GTMaterials.Endstone)
                .inputItems(TagPrefix.dust, GTNAMaterials.DarkSteel)
                .inputItems(TagPrefix.dust, GTMaterials.Obsidian)
                .circuitMeta(3)
                .outputItems(TagPrefix.dust, GTNAMaterials.EndSteel, 3)
                .EUt(480)
                .duration(360)
                .save(provider);

        GTRecipeTypes.MIXER_RECIPES.recipeBuilder("indalloy_140_dust")
                .inputItems(TagPrefix.dust, GTMaterials.Bismuth, 47)
                .inputItems(TagPrefix.dust, GTMaterials.Lead, 25)
                .inputItems(TagPrefix.dust, GTMaterials.Tin, 13)
                .inputItems(TagPrefix.dust, GTMaterials.Cadmium, 10)
                .inputItems(TagPrefix.dust, GTMaterials.Indium, 5)
                .circuitMeta(1)
                .outputItems(TagPrefix.dust, GTNAMaterials.Indalloy140, 100)
                .EUt(GTValues.EV)
                .duration(800)
                .save(provider);

        GTRecipeTypes.MIXER_RECIPES.recipeBuilder("trinaquadalloy_dust")
                .inputItems(TagPrefix.dust, GTMaterials.Trinium, 6)
                .inputItems(TagPrefix.dust, GTMaterials.Naquadah, 2)
                .inputItems(TagPrefix.dust, GTMaterials.Carbon)
                .circuitMeta(1)
                .outputItems(TagPrefix.dust, GTNAMaterials.Trinaquadalloy, 9)
                .EUt(GTValues.ZPM)
                .duration(600)
                .save(provider);

        GTRecipeTypes.MIXER_RECIPES.recipeBuilder("mar_m_200_steel_dust")
                .inputItems(TagPrefix.dust, GTMaterials.Niobium, 2)
                .inputItems(TagPrefix.dust, GTMaterials.Chromium, 9)
                .inputItems(TagPrefix.dust, GTMaterials.Aluminium, 5)
                .inputItems(TagPrefix.dust, GTMaterials.Titanium, 2)
                .inputItems(TagPrefix.dust, GTMaterials.Cobalt, 10)
                .inputItems(TagPrefix.dust, GTMaterials.Tungsten, 13)
                .inputItems(TagPrefix.dust, GTMaterials.Nickel, 18)
                .circuitMeta(1)
                .outputItems(TagPrefix.dust, GTNAMaterials.MarM200Steel, 59)
                .EUt(GTValues.IV)
                .duration(900)
                .save(provider);

        GTRecipeTypes.MIXER_RECIPES.recipeBuilder("fall_king_dust")
                .inputItems(TagPrefix.dust, GTMaterials.Lithium)
                .inputItems(TagPrefix.dust, GTMaterials.Cobalt)
                .inputItems(TagPrefix.dust, GTMaterials.Platinum)
                .inputItems(TagPrefix.dust, GTMaterials.Erbium)
                .inputFluids(GTMaterials.Helium.getFluid(1000))
                .circuitMeta(1)
                .outputItems(TagPrefix.dust, GTNAMaterials.FallKing, 5)
                .EUt(GTValues.IV)
                .duration(500)
                .save(provider);

        GTRecipeTypes.CHEMICAL_RECIPES.recipeBuilder("zirconium_oxide_dust")
                .inputItems(TagPrefix.dust, GTMaterials.Zirconium)
                .inputFluids(GTMaterials.Oxygen.getFluid(2000))
                .outputItems(TagPrefix.dust, GTNAMaterials.ZirconiumOxide)
                .EUt(480)
                .duration(200)
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
