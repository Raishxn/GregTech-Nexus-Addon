package com.raishxn.gtna.data.recipe;

import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.raishxn.gtna.common.data.GTNAMaterials; // Assumindo onde estão seus custom fluids
import com.raishxn.gtna.common.data.GTNARecipeType;
import com.raishxn.gtna.common.data.condition.CompactCondition;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.level.material.Fluids;

import java.util.function.Consumer;

public class GTNAHighPressureRecipes {

    public static void register(Consumer<FinishedRecipe> provider) {

        // 1. Steam + Creosote -> Superheated Steam
        GTNARecipeType.HIGH_PRESSURE_REACTOR_RECIPES.recipeBuilder("superheated_steam_conversion")
                .inputFluids(GTMaterials.Steam.getFluid(160000))
                .inputFluids(GTMaterials.Creosote.getFluid(40000))
                .outputFluids(GTNAMaterials.SuperHeatedSteam.getFluid(16000))
                .duration(20)
                .save(provider);

        // 2. Superheated Steam + Creosote -> Dense Supercritical Steam
        GTNARecipeType.HIGH_PRESSURE_REACTOR_RECIPES.recipeBuilder("dense_supercritical_steam_conversion")
                .inputFluids(GTNAMaterials.SuperHeatedSteam.getFluid(160000))
                .inputFluids(GTMaterials.Creosote.getFluid(40000))
                .outputFluids(GTNAMaterials.DenseSupercriticalSteam.getFluid(16000))
                .duration(20)
                .save(provider);

        // 3. Water + Lava -> Dense Supercritical Steam (COMPACT ONLY)
        GTNARecipeType.HIGH_PRESSURE_REACTOR_RECIPES.recipeBuilder("magmatic_supercritical_generation")
                .inputFluids(GTMaterials.Water, 100000)
                .inputFluids(GTMaterials.Lava.getFluid(125000))
                .outputFluids(GTNAMaterials.DenseSupercriticalSteam.getFluid(1000))
                .duration(1)
                .addCondition(CompactCondition.INSTANCE)
                .save(provider);

        // 4. Dense Supercritical Steam + Helium -> Insanely Supercritical Steam (COMPACT ONLY)
        GTNARecipeType.HIGH_PRESSURE_REACTOR_RECIPES.recipeBuilder("insanely_steam_generation")
                .inputFluids(GTNAMaterials.DenseSupercriticalSteam.getFluid(100000))
                .inputFluids(GTMaterials.Helium.getFluid(200000))
                .outputFluids(GTNAMaterials.InsanelySupercriticalSteam.getFluid(10000))
                .duration(100)
                .EUt(480)
                .addCondition(CompactCondition.INSTANCE)
                .save(provider);
    }
}