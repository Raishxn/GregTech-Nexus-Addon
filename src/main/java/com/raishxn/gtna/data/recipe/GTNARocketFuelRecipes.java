package com.raishxn.gtna.data.recipe;

import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.data.recipes.FinishedRecipe;

import com.raishxn.gtna.common.data.GTNARecipeType;

import java.util.function.Consumer;

/**
 * Rocket engine fuel recipes for the Rocket Large Turbine (GTOCore {@code FuelRecipe.java}).
 *
 * <p>
 * Only GTO's {@code rocket_engine_fuel_1} uses a GTCEu material: 10 mB of {@code RocketFuel} for
 * 20 ticks at 512 EU/t (HV voltage), exactly as GTO defines it. The other five
 * {@code ROCKET_ENGINE_FUELS} entries consume GTO-only rocket fuels
 * ({@code RocketFuelRp1}, {@code DenseHydrazineFuelMixture}, {@code RocketFuelCn3h7o3},
 * {@code RocketFuelH8n4c2o4}, {@code ExplosiveHydrazine}) and a seventh uses Ad Astra's cryo fuel,
 * so they are not ported. GTO's {@code addFuelProperties} hook feeds the GTO-only Powerless Jetpack
 * and is not ported either.
 */
public final class GTNARocketFuelRecipes {

    private GTNARocketFuelRecipes() {}

    public static void register(Consumer<FinishedRecipe> provider) {
        // GTOCore FuelRecipe.java "rocket_engine_fuel_1": 10 mB RocketFuel -> 512 EU/t for 20 ticks.
        GTNARecipeType.ROCKET_ENGINE_FUELS.recipeBuilder("rocket_engine_fuel_1")
                .inputFluids(GTMaterials.RocketFuel, 10)
                .duration(20)
                .EUt(-512)
                .save(provider);
    }
}
