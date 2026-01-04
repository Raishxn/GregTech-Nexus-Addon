package com.raishxn.gtna.data.recipe;

import com.gregtechceu.gtceu.api.GTCEuAPI; // <--- Importante: Acesso à API do GTCEu
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import net.minecraft.data.recipes.FinishedRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.BENDER_RECIPES;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.COMPRESSOR_RECIPES;
import static com.raishxn.gtna.api.data.info.GTNAMaterialFlags.*;
import static com.raishxn.gtna.api.data.tag.GTNATagPrefix.*;

public class GTNAGeneratesRecipes {
    public static void register(Consumer<FinishedRecipe> provider) {
        GTCEuAPI.materialManager.getRegisteredMaterials().forEach(material -> run(provider, material));
    }

    // Método RUN: Executa a lógica para UM material específico
    public static void run(@NotNull Consumer<FinishedRecipe> provider, @NotNull Material material) {
        // Plates
        processTriplePlate(provider, material);
        processQuadruplePlate(provider, material);
        processQuintuplePlate(provider, material);
        processSuperdensePlate(provider, material);

        // Ingots (Com lógica do Compressor)
        processDoubleIngot(provider, material);
        processTripleIngot(provider, material);
        processQuadrupleIngot(provider, material);
        processQuintupleIngot(provider, material);

        // Singularity
        processSingularity(provider, material);
    }

    // --- PLATES ---

    private static void processTriplePlate(@NotNull Consumer<FinishedRecipe> provider, @NotNull Material material) {
        if (!material.shouldGenerateRecipesFor(triplePlate) || !material.hasProperty(PropertyKey.DUST)) {
            return;
        }
        if (material.hasProperty(PropertyKey.INGOT)) {
            BENDER_RECIPES.recipeBuilder("bend_" + material.getName() + "_ingot_to_triple_plate")
                    .inputItems(ingot, material, 3)
                    .circuitMeta(3)
                    .outputItems(triplePlate, material)
                    .duration(160)
                    .EUt(GTValues.VA[GTValues.MV])
                    .save(provider);
        }
        if (material.hasFlag(GENERATE_PLATE)) {
            BENDER_RECIPES.recipeBuilder("bend_" + material.getName() + "_plate_to_triple_plate")
                    .inputItems(plate, material, 3)
                    .circuitMeta(3)
                    .outputItems(triplePlate, material)
                    .duration(160)
                    .EUt(GTValues.VA[GTValues.MV])
                    .save(provider);

            if (!material.hasFlag(NO_SMASHING) && material.shouldGenerateRecipesFor(doubleIngot)) {
                VanillaRecipeHelper.addShapelessRecipe(provider, String.format("triple_plate_%s_manual", material.getName()),
                        ChemicalHelper.get(triplePlate, material),
                        new MaterialEntry(plate, material),
                        new MaterialEntry(plateDouble, material),
                        "gtceu:hammer");
            }
        }
    }

    private static void processQuadruplePlate(@NotNull Consumer<FinishedRecipe> provider, @NotNull Material material) {
        if (!material.shouldGenerateRecipesFor(quadruplePlate) || !material.hasProperty(PropertyKey.DUST)) {
            return;
        }
        if (material.hasFlag(GENERATE_PLATE)) {
            BENDER_RECIPES.recipeBuilder("bend_" + material.getName() + "_double_plate_to_quadruple_plate")
                    .inputItems(plateDouble, material, 2)
                    .circuitMeta(2)
                    .outputItems(quadruplePlate, material)
                    .duration(320)
                    .EUt(GTValues.VA[GTValues.MV])
                    .save(provider);
        }
        if (material.hasProperty(PropertyKey.INGOT)) {
            BENDER_RECIPES.recipeBuilder("bend_" + material.getName() + "_ingot_to_quadruple_plate")
                    .inputItems(ingot, material, 4)
                    .circuitMeta(4)
                    .outputItems(quadruplePlate, material)
                    .duration(320)
                    .EUt(GTValues.VA[GTValues.MV])
                    .save(provider);
        }
        if (material.hasFlag(GENERATE_PLATE)) {
            BENDER_RECIPES.recipeBuilder("bend_" + material.getName() + "_plate_to_quadruple_plate")
                    .inputItems(plate, material, 4)
                    .circuitMeta(4)
                    .outputItems(quadruplePlate, material)
                    .duration(320)
                    .EUt(GTValues.VA[GTValues.MV])
                    .save(provider);

            if (!material.hasFlag(NO_SMASHING)) {
                VanillaRecipeHelper.addShapelessRecipe(provider, String.format("quadruple_plate_%s_manual", material.getName()),
                        ChemicalHelper.get(quadruplePlate, material),
                        new MaterialEntry(plate, material),
                        new MaterialEntry(triplePlate, material),
                        "gtceu:hammer");
            }
        }
    }

    private static void processQuintuplePlate(@NotNull Consumer<FinishedRecipe> provider, @NotNull Material material) {
        if (!material.shouldGenerateRecipesFor(quintuplePlate) || !material.hasProperty(PropertyKey.DUST)) {
            return;
        }
        if (material.hasProperty(PropertyKey.INGOT)) {
            BENDER_RECIPES.recipeBuilder("bend_" + material.getName() + "_ingot_to_quintuple_plate")
                    .inputItems(ingot, material, 5)
                    .circuitMeta(5)
                    .outputItems(quintuplePlate, material)
                    .duration(800)
                    .EUt(GTValues.VA[GTValues.MV])
                    .save(provider);
        }
        if (material.hasFlag(GENERATE_PLATE)) {
            BENDER_RECIPES.recipeBuilder("bend_" + material.getName() + "_plate_to_quintuple_plate")
                    .inputItems(plate, material, 5)
                    .circuitMeta(5)
                    .outputItems(quintuplePlate, material)
                    .duration(800)
                    .EUt(GTValues.VA[GTValues.MV])
                    .save(provider);

            if (!material.hasFlag(NO_SMASHING)) {
                VanillaRecipeHelper.addShapelessRecipe(provider, String.format("quintuple_plate_%s_manual", material.getName()),
                        ChemicalHelper.get(quintuplePlate, material),
                        new MaterialEntry(plate, material),
                        new MaterialEntry(quadruplePlate, material),
                        "gtceu:hammer");
            }
        }
    }

    private static void processSuperdensePlate(@NotNull Consumer<FinishedRecipe> provider, @NotNull Material material) {
        if (!material.shouldGenerateRecipesFor(superdensePlate) || !material.hasProperty(PropertyKey.DUST)) {
            return;
        }
        if (material.hasFlag(GENERATE_PLATE)) {
            BENDER_RECIPES.recipeBuilder("bend_" + material.getName() + "_plate_to_superdense_plate")
                    .inputItems(plate, material, 64)
                    .outputItems(superdensePlate, material)
                    .duration(2400)
                    .EUt(GTValues.VA[GTValues.MV])
                    .save(provider);
        }
    }

    // --- INGOTS ---

    private static void processDoubleIngot(@NotNull Consumer<FinishedRecipe> provider, @NotNull Material material) {
        if (!material.shouldGenerateRecipesFor(doubleIngot) || !material.hasProperty(PropertyKey.INGOT)) {
            return;
        }
        // Manual
        if (!material.hasFlag(NO_SMASHING)) {
            VanillaRecipeHelper.addShapedRecipe(provider, String.format("double_ingot_%s_manual", material.getName()),
                    ChemicalHelper.get(doubleIngot, material),
                    "P", "P",
                    'P', new MaterialEntry(ingot, material));
        }
        // Compressor: 2 Ingots -> 1 Double
        COMPRESSOR_RECIPES.recipeBuilder("compress_" + material.getName() + "_ingot_to_double_ingot")
                .inputItems(ingot, material, 2)
                .outputItems(doubleIngot, material)
                .duration((int) material.getMass() * 2)
                .EUt(2)
                .save(provider);
    }

    private static void processTripleIngot(@NotNull Consumer<FinishedRecipe> provider, @NotNull Material material) {
        if (!material.shouldGenerateRecipesFor(tripleIngot) || !material.hasProperty(PropertyKey.INGOT)) {
            return;
        }
        // Manual
        if (!material.hasFlag(NO_SMASHING) && material.shouldGenerateRecipesFor(doubleIngot)) {
            VanillaRecipeHelper.addShapelessRecipe(provider, String.format("triple_ingot_%s_manual", material.getName()),
                    ChemicalHelper.get(tripleIngot, material),
                    new MaterialEntry(doubleIngot, material),
                    new MaterialEntry(ingot, material));
        }
        // Compressor: 3 Ingots -> 1 Triple
        COMPRESSOR_RECIPES.recipeBuilder("compress_" + material.getName() + "_ingot_to_triple_ingot")
                .inputItems(ingot, material, 3)
                .outputItems(tripleIngot, material)
                .duration((int) material.getMass() * 3)
                .EUt(2)
                .save(provider);
    }

    private static void processQuadrupleIngot(@NotNull Consumer<FinishedRecipe> provider, @NotNull Material material) {
        if (!material.shouldGenerateRecipesFor(quadrupleIngot) || !material.hasProperty(PropertyKey.INGOT)) {
            return;
        }
        // Manual
        if (!material.hasFlag(NO_SMASHING) && material.shouldGenerateRecipesFor(doubleIngot)) {
            VanillaRecipeHelper.addShapedRecipe(provider, String.format("quadruple_ingot_%s_from_double", material.getName()),
                    ChemicalHelper.get(quadrupleIngot, material),
                    "D", "D",
                    'D', new MaterialEntry(doubleIngot, material));
        }
        // Compressor: 4 Ingots -> 1 Quadruple
        COMPRESSOR_RECIPES.recipeBuilder("compress_" + material.getName() + "_ingot_to_quadruple_ingot")
                .inputItems(ingot, material, 4)
                .outputItems(quadrupleIngot, material)
                .duration((int) material.getMass() * 4)
                .EUt(2)
                .save(provider);
        // Compressor: 2 Double Ingots -> 1 Quadruple
        if (material.shouldGenerateRecipesFor(doubleIngot)) {
            COMPRESSOR_RECIPES.recipeBuilder("compress_" + material.getName() + "_double_ingot_to_quadruple_ingot")
                    .inputItems(doubleIngot, material, 2)
                    .outputItems(quadrupleIngot, material)
                    .duration((int) material.getMass() * 4)
                    .EUt(2)
                    .save(provider);
        }
    }

    private static void processQuintupleIngot(@NotNull Consumer<FinishedRecipe> provider, @NotNull Material material) {
        if (!material.shouldGenerateRecipesFor(quintupleIngot) || !material.hasProperty(PropertyKey.INGOT)) {
            return;
        }
        // Manual
        if (!material.hasFlag(NO_SMASHING) && material.shouldGenerateRecipesFor(quadrupleIngot)) {
            VanillaRecipeHelper.addShapelessRecipe(provider, String.format("quintuple_ingot_%s_manual", material.getName()),
                    ChemicalHelper.get(quintupleIngot, material),
                    new MaterialEntry(quintupleIngot, material),
                    new MaterialEntry(ingot, material));
        }
        // Compressor: 5 Ingots -> 1 Quintuple
        COMPRESSOR_RECIPES.recipeBuilder("compress_" + material.getName() + "_ingot_to_quintuple_ingot")
                .inputItems(ingot, material, 5)
                .outputItems(quintupleIngot, material)
                .duration((int) material.getMass() * 5)
                .EUt(2)
                .save(provider);
    }

    // --- SINGULARITY ---

    private static void processSingularity(@NotNull Consumer<FinishedRecipe> provider, @NotNull Material material) {
        if (!material.shouldGenerateRecipesFor(singularity)) {
            return;
        }
        if (material.shouldGenerateRecipesFor(quintupleIngot)) {
            COMPRESSOR_RECIPES.recipeBuilder("compress_" + material.getName() + "_to_singularity")
                    .inputItems(quintupleIngot, material, 6400)
                    .outputItems(singularity, material)
                    .duration(400)
                    .EUt(GTValues.VA[GTValues.MAX])
                    .save(provider);
        }
    }
}