package com.raishxn.gtna.data.recipe;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.BlastProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.common.data.GTNARecipeType;

import java.util.Locale;
import java.util.function.Consumer;

/**
 * GTOCore {@code GTOMaterialRecipeHandler.processDust} atomization branch (LGPLv3, source lines
 * 425-455): the Cold Ice Freezer's auxiliary-module family.
 *
 * <p>
 * For every material that has a dust and a fluid, three recipes are generated:
 * <ul>
 * <li>{@code fluid + inert gas -> dust + inert gas} (no circuit);</li>
 * <li>{@code molten + inert gas -> dust + inert gas} (circuit 1);</li>
 * <li>{@code molten + inert gas -> fluid + inert gas} (circuit 2).</li>
 * </ul>
 * The inert gas and its amount follow GTOCore's formulas from the material's EBF gas tier (Nitrogen
 * 1000 / Helium 100 / Argon 50 / Neon 25 / Krypton 10 mB, scaled by mass), defaulting to Nitrogen for
 * materials without a blast property. Materials whose blast temperature reaches 5000 K also require
 * 500 mB of liquid Helium and return 250 mB of gaseous Helium, exactly like the original.
 *
 * <p>
 * <b>Documented substitution:</b> GTOCore feeds the recipes with dedicated high-pressure gas fluids
 * (its {@code GTOFluidStorageKey.HIGH_PRESSURE_GAS}, e.g. {@code high_pressure_nitrogen}) produced by
 * GTO's Gas Compressor. GTNA does not port that fluid key nor the Gas Compressor, and adding an
 * unobtainable fluid would open a dead-end chain, so the high-pressure slot uses the regular GTCEu
 * gas at GTO's high-pressure amount. The recipe semantics (gas carrier scaled by material mass) are
 * preserved. GTO's recipe categories ({@code CONDENSE_FLUID_TO_DUST}/{@code CONDENSE_MOLTEN_TO_DUST})
 * are cosmetic and are not ported.
 */
public final class GTNAAtomizationRecipes {

    private GTNAAtomizationRecipes() {}

    public static void register(Consumer<FinishedRecipe> provider) {
        for (Material material : GTCEuAPI.materialManager.getRegisteredMaterials()) {
            if (!GTNACORE.MOD_ID.equals(material.getModid()) && !"gtceu".equals(material.getModid())) {
                continue;
            }
            atomize(material, provider);
        }
    }

    private static void atomize(Material material, Consumer<FinishedRecipe> provider) {
        ItemStack dustStack = ChemicalHelper.get(TagPrefix.dust, material);
        if (dustStack.isEmpty() || !material.hasFluid()) {
            return;
        }
        int mass = (int) material.getMass();
        Fluid liquid = material.getFluid();
        if (liquid == null) {
            return;
        }
        FluidIngredient inert = inert(material, mass, false);
        FluidIngredient highPressure = inert(material, mass, true);
        String id = "atomize_condense_" + TagPrefix.dust.name.toLowerCase(Locale.ROOT) + "_" +
                material.getName().toLowerCase(Locale.ROOT) + "_";

        GTNARecipeType.ATOMIZATION_CONDENSATION_RECIPES.recipeBuilder(id + "to_dust")
                .inputFluids(new net.minecraftforge.fluids.FluidStack(liquid, GTValues.L))
                .inputFluids(highPressure)
                .outputItems(dustStack)
                .outputFluids(inert)
                .duration(mass / 2 + 1)
                .EUt(GTValues.VA[GTValues.LV] / 2)
                .save(provider);

        Fluid molten = material.getFluid(FluidStorageKeys.MOLTEN);
        if (molten != null) {
            boolean needLiquidHelium = material.hasProperty(PropertyKey.ALLOY_BLAST) &&
                    material.getProperty(PropertyKey.ALLOY_BLAST).getTemperature() >= 5000;
            int eut = voltageMultiplier(material);
            var toDust = GTNARecipeType.ATOMIZATION_CONDENSATION_RECIPES
                    .recipeBuilder(id + "to_dust_from_molten")
                    .inputFluids(new net.minecraftforge.fluids.FluidStack(molten, GTValues.L))
                    .inputFluids(highPressure)
                    .outputItems(dustStack)
                    .outputFluids(inert)
                    .duration((int) (mass * 1.5f))
                    .EUt(eut)
                    .circuitMeta(1);
            var toLiquid = GTNARecipeType.ATOMIZATION_CONDENSATION_RECIPES
                    .recipeBuilder(id + "to_liquid_from_molten")
                    .inputFluids(new net.minecraftforge.fluids.FluidStack(molten, GTValues.L))
                    .inputFluids(highPressure)
                    .outputFluids(new net.minecraftforge.fluids.FluidStack(liquid, GTValues.L))
                    .outputFluids(inert)
                    .duration((int) (mass * 2.5f))
                    .EUt(eut)
                    .circuitMeta(2);
            if (needLiquidHelium) {
                toDust.inputFluids(GTMaterials.Helium.getFluid(FluidStorageKeys.LIQUID, 500))
                        .outputFluids(GTMaterials.Helium.getFluid(250));
                toLiquid.inputFluids(GTMaterials.Helium.getFluid(FluidStorageKeys.LIQUID, 500))
                        .outputFluids(GTMaterials.Helium.getFluid(250));
            }
            toDust.save(provider);
            toLiquid.save(provider);
        }
    }

    /**
     * GTOCore's inert gas: the material's EBF gas tier fluid scaled by mass, or Nitrogen for materials
     * without a blast property. The {@code highPressure} flag applies GTO's second amount formula;
     * see the class Javadoc for the high-pressure-fluid substitution.
     */
    private static FluidIngredient inert(Material material, int mass, boolean highPressure) {
        BlastProperty blast = material.getProperty(PropertyKey.BLAST);
        if (blast != null && blast.getGasTier() != null) {
            FluidIngredient base = blast.getGasTier().getFluid();
            int amount = highPressure ? base.getAmount() * mass / 450 + 40 + mass / 5 * 6 :
                    base.getAmount() * mass / 500 + 30 + mass;
            FluidIngredient scaled = base.copy();
            scaled.setAmount(Math.max(1, amount));
            return scaled;
        }
        return FluidIngredient.of(GTMaterials.Nitrogen.getFluid(), Math.max(1, highPressure ? 5 * mass : 4 * mass));
    }

    /** GTOCore {@code GTOUtils.getVoltageMultiplier}: 30 EU/t for hot materials, 8 EU/t otherwise. */
    private static int voltageMultiplier(Material material) {
        return material.getBlastTemperature() >= 2800 ? GTValues.VA[GTValues.LV] : GTValues.VA[GTValues.ULV];
    }
}
