package com.raishxn.gtna.data.recipe;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.level.material.Fluid;

import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.api.machine.feature.godforge.GodforgePlasmaTable;
import com.raishxn.gtna.common.data.GTNARecipeType;
import com.raishxn.gtna.common.data.material.GodforgeMaterials;

import java.util.function.Consumer;

/** Forge of Gods Plasma Module recipes from GTNH {@code Godforge.run()}, at the GTNH UXV recipe tier. */
public final class GTNAGodforgeRecipes {

    public static final String PLASMA_TIER = "fog_plasma_tier";
    public static final String PLASMA_MULTISTEP = "fog_plasma_multistep";

    private GTNAGodforgeRecipes() {}

    public static void register(Consumer<FinishedRecipe> provider) {
        long eut = GTValues.VA[GTValues.UXV];
        for (var entry : GodforgePlasmaTable.ENTRIES) {
            Material material = GodforgeMaterials.material(entry.material());
            if (material == null || !material.hasProperty(PropertyKey.FLUID)) continue;
            Fluid plasma = material.getFluid(FluidStorageKeys.PLASMA);
            if (plasma == null) continue;
            if (entry.solid()) {
                if (material.hasProperty(PropertyKey.DUST)) {
                    GTNARecipeType.GODFORGE_PLASMA_RECIPES.recipeBuilder(GTNACORE.id("godforge_plasma_" +
                            entry.material() + "_dust"))
                            .inputItems(TagPrefix.dust, material)
                            .outputFluids(material.getFluid(FluidStorageKeys.PLASMA, 144))
                            .duration(entry.durationTicks()).EUt(eut)
                            .addData(PLASMA_TIER, entry.tier()).addData(PLASMA_MULTISTEP, entry.multiStep())
                            .save(provider);
                }
                Fluid molten = material.getFluid(FluidStorageKeys.MOLTEN);
                if (molten == null) molten = material.getFluid(FluidStorageKeys.LIQUID);
                if (molten != null) {
                    GTNARecipeType.GODFORGE_PLASMA_RECIPES.recipeBuilder(GTNACORE.id("godforge_plasma_" +
                            entry.material() + "_molten"))
                            .inputFluids(new net.minecraftforge.fluids.FluidStack(molten, 144))
                            .outputFluids(material.getFluid(FluidStorageKeys.PLASMA, 144))
                            .duration(entry.durationTicks()).EUt(eut)
                            .addData(PLASMA_TIER, entry.tier()).addData(PLASMA_MULTISTEP, entry.multiStep())
                            .save(provider);
                }
            } else {
                Fluid source = material.getFluid();
                if (source == null || source == plasma) continue;
                GTNARecipeType.GODFORGE_PLASMA_RECIPES.recipeBuilder(GTNACORE.id("godforge_plasma_" +
                        entry.material()))
                        .inputFluids(new net.minecraftforge.fluids.FluidStack(source, 500))
                        .outputFluids(material.getFluid(FluidStorageKeys.PLASMA, 500))
                        .duration(entry.durationTicks()).EUt(eut)
                        .addData(PLASMA_TIER, entry.tier()).addData(PLASMA_MULTISTEP, entry.multiStep())
                        .save(provider);
            }
        }
    }
}
