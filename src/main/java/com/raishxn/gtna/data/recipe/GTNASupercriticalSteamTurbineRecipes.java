package com.raishxn.gtna.data.recipe;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.data.recipes.FinishedRecipe;

import com.raishxn.gtna.common.data.GTNAMachines3;
import com.raishxn.gtna.common.data.GTNAMaterials;
import com.raishxn.gtna.common.data.GTNARecipeType;

import java.util.function.Consumer;

/**
 * Supercritical Steam Turbine recipes (GTOCore {@code FuelRecipe.java} and
 * {@code classified/Assembler.java}).
 *
 * <p>
 * Fluid equivalence: GTO's only supercritical grade is {@code SupercriticalSteam}
 * ({@code MaterialB.java:3490}, 1000 K, produced by the Heat Exchanger from hot fluids). GTNA keeps
 * its own supercritical chain and does <b>not</b> register a {@code SupercriticalSteam} material, so
 * the fuel recipe consumes {@code DenseSupercriticalSteam} — GTNA's first supercritical grade,
 * produced from {@code SuperHeatedSteam} in the High Pressure Reactor and reused by the Void Miner
 * and the Cactus Wonder. The second GTNA grade, {@code InsanelySupercriticalSteam}, stays a
 * GTNA-only fluid; consuming it here would invent a second GTO fuel. The recipe keeps GTO's exact
 * numbers: 80 mB steam → 8 mB distilled water over 30 ticks at {@code EUt(-V[MV])}.
 */
public final class GTNASupercriticalSteamTurbineRecipes {

    private GTNASupercriticalSteamTurbineRecipes() {}

    public static void register(Consumer<FinishedRecipe> provider) {
        // GTOCore FuelRecipe.java "supercritical_steam": 80 mB -> 8 mB distilled water, 30 ticks,
        // EUt(-V[MV]). GTO's SupercriticalSteam is replaced by GTNA's DenseSupercriticalSteam.
        GTNARecipeType.SUPERCRITICAL_STEAM_TURBINE_FUELS.recipeBuilder("supercritical_steam")
                .inputFluids(GTNAMaterials.DenseSupercriticalSteam.getFluid(80))
                .outputFluids(GTMaterials.DistilledWater.getFluid(8))
                .duration(30)
                .EUt(-GTValues.V[GTValues.MV])
                .save(provider);

        // GTOCore classified/Assembler.java "supercritical_steam_turbine": GTO's MarM200Steel is
        // GTNA's MarM200Steel, so the controller recipe is ported 1:1.
        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("supercritical_steam_turbine")
                .inputItems(GTMachines.HULL[GTValues.LuV].asStack().getItem())
                .inputItems(CustomTags.LuV_CIRCUITS, 4)
                .inputItems(GTItems.ELECTRIC_MOTOR_LuV, 2)
                .inputItems(TagPrefix.gear, GTNAMaterials.MarM200Steel, 2)
                .inputItems(TagPrefix.pipeLargeFluid, GTMaterials.TungstenCarbide, 2)
                .inputItems(TagPrefix.plate, GTNAMaterials.MarM200Steel, 8)
                .outputItems(GTNAMachines3.SUPERCRITICAL_STEAM_TURBINE.asStack())
                .EUt(30720)
                .duration(200)
                .save(provider);
    }
}
