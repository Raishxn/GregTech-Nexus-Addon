package com.raishxn.gtna.data.recipe;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.OreProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;

import com.raishxn.gtna.common.data.GTNARecipeType;
import com.raishxn.gtna.config.GTNABalance;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.DistilledWater;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Stone;

/**
 * Faithful port of GTLCore's integrated ore processing recipes (LGPLv3, attribution in
 * {@code THIRD_PARTY_NOTICES.md} and {@code GTNASources}).
 *
 * <p>
 * GTLCore overwrites GTCEu's {@code OreRecipeHandler} with a mixin and emits, for every ore material,
 * one recipe per circuit (1..7) on its {@code integrated_ore_processor} recipe type: the whole
 * macerate → wash → thermal/sift/centrifuge chain collapsed into a single recipe, with the real
 * per-stage byproducts and the real washing fluid (mercury / water / distilled water, whatever the
 * material declares via {@code OreProperty#getWashedIn()}).
 *
 * <p>
 * This port reproduces that generation in GTNA's own datagen hook instead of a mixin, so it never
 * fights GTCEu's recipe loader. Circuits 2/4/5/7 only exist when the material can actually reach that
 * stage (refined ore for the centrifuge branch, gem property for sifting, a washing fluid for the
 * bath branch), exactly as in the source. The {@code crushedAmount} multiplier is GTNA's
 * {@code integratedOreMultiplier} (default 4, GTLCore parity).
 */
public final class IntegratedOreRecipes {

    private IntegratedOreRecipes() {}

    public static void register(Consumer<FinishedRecipe> provider) {
        int oreMultiplier = GTNABalance.getIntegratedOreMultiplier();
        if (oreMultiplier <= 0) {
            return;
        }
        for (Material material : GTCEuAPI.materialManager.getRegisteredMaterials()) {
            OreProperty property = material.getProperty(PropertyKey.ORE);
            if (property == null) {
                continue;
            }
            // GTLCore runs processRawOre for the raw ore prefix and processOre for the stone ore
            // prefix only; the other ore variants (granite, deepslate, ...) are deliberately skipped.
            if (material.shouldGenerateRecipesFor(rawOre)) {
                generate(provider, material, property, rawOre, true, oreMultiplier);
            }
            if (material.shouldGenerateRecipesFor(ore)) {
                generate(provider, material, property, ore, false, oreMultiplier);
            }
        }
    }

    private static void generate(Consumer<FinishedRecipe> provider, Material material, OreProperty property,
                                 TagPrefix inputPrefix, boolean rawOreInput, int oreMultiplier) {
        int crushedAmount = rawOreInput ?
                IntegratedOreMath.rawCrushedAmount(property.getOreMultiplier(), oreMultiplier) :
                IntegratedOreMath.stoneCrushedAmount(property.getOreMultiplier(), oreMultiplier);
        if (crushedAmount <= 0) {
            return;
        }

        ItemStack dustStack = ChemicalHelper.get(dust, material, crushedAmount);
        if (dustStack.isEmpty()) {
            return;
        }

        // Step-1 byproduct: the material's gem if it has one, otherwise its dust.
        Material byproductMaterial = property.getOreByProduct(0, material);
        ItemStack byproductStack = ChemicalHelper.get(gem, byproductMaterial);
        if (byproductStack.isEmpty()) {
            byproductStack = ChemicalHelper.get(dust, byproductMaterial);
        }
        ItemStack secondaryByproduct = ChemicalHelper.get(dust, byproductMaterial,
                property.getByProductMultiplier() * crushedAmount);
        Material byproductMaterial1 = property.getOreByProduct(1, material);
        ItemStack byproductStack1 = ChemicalHelper.get(dust, byproductMaterial1, crushedAmount);
        ItemStack byproductStack2 = ChemicalHelper.get(dust, property.getOreByProduct(2, material), crushedAmount);
        int step1Chance = rawOreInput ? 1000 : 1400;
        int step1Boost = rawOreInput ? 300 : 850;
        String idPrefix = rawOreInput ? "integrated_ore_processing_raw_" : "integrated_ore_processing_";
        long mass = material.getMass();

        // 1 macerate - macerate - centrifuge
        GTRecipeBuilder b1 = GTNARecipeType.ORE_PROCESSING_RECIPES
                .recipeBuilder(idPrefix + "1_" + material.getName())
                .circuitMeta(1)
                .inputItems(inputPrefix, material)
                .outputItems(dustStack)
                .chancedOutput(byproductStack, step1Chance, step1Boost)
                .chancedOutput(secondaryByproduct, 1400, 850)
                .duration(IntegratedOreMath.duration(1, mass, crushedAmount))
                .EUt(30);
        addSecondaryOutputs(b1, rawOreInput, 1, crushedAmount);
        if (byproductMaterial.hasProperty(PropertyKey.DUST)) {
            b1.chancedOutput(dust, byproductMaterial, crushedAmount, "1/9", 0);
        }
        b1.save(provider);

        boolean canCentrifuge = !ChemicalHelper.get(crushedRefined, material).isEmpty();

        // 2 macerate - wash - thermal centrifuge - macerate
        if (canCentrifuge) {
            GTRecipeBuilder b2 = GTNARecipeType.ORE_PROCESSING_RECIPES
                    .recipeBuilder(idPrefix + "2_" + material.getName())
                    .circuitMeta(2)
                    .inputItems(inputPrefix, material)
                    .inputFluids(DistilledWater, IntegratedOreMath.washFluidAmount(crushedAmount))
                    .outputItems(dustStack)
                    .chancedOutput(byproductStack, step1Chance, step1Boost)
                    .chancedOutput(dust, byproductMaterial, crushedAmount, "1/3", 0)
                    .outputItems(dust, Stone, crushedAmount)
                    .chancedOutput(dust, byproductMaterial1, crushedAmount, "1/3", 0)
                    .chancedOutput(byproductStack2, 1400, 850)
                    .duration(IntegratedOreMath.duration(2, mass, crushedAmount))
                    .EUt(30);
            addSecondaryOutputs(b2, rawOreInput, 2, crushedAmount);
            b2.save(provider);
        }

        // 3 macerate - wash - macerate - centrifuge
        GTRecipeBuilder b3 = GTNARecipeType.ORE_PROCESSING_RECIPES
                .recipeBuilder(idPrefix + "3_" + material.getName())
                .circuitMeta(3)
                .inputItems(inputPrefix, material)
                .inputFluids(DistilledWater, IntegratedOreMath.washFluidAmount(crushedAmount))
                .outputItems(dustStack)
                .chancedOutput(byproductStack, step1Chance, step1Boost)
                .chancedOutput(dust, byproductMaterial, crushedAmount, "1/3", 0)
                .outputItems(dust, Stone, crushedAmount)
                .chancedOutput(byproductStack1, 1400, 850)
                .chancedOutput(dust, byproductMaterial1, crushedAmount, "1/9", 0)
                .duration(IntegratedOreMath.duration(3, mass, crushedAmount))
                .EUt(30);
        addSecondaryOutputs(b3, rawOreInput, 3, crushedAmount);
        b3.save(provider);

        boolean highSifter = material.hasFlag(MaterialFlags.HIGH_SIFTER_OUTPUT);

        // 4 macerate - wash - sift - centrifuge
        if (material.hasProperty(PropertyKey.GEM)) {
            ItemStack exquisiteStack = ChemicalHelper.get(gemExquisite, material, crushedAmount);
            ItemStack flawlessStack = ChemicalHelper.get(gemFlawless, material, crushedAmount);
            ItemStack gemStack = ChemicalHelper.get(gem, material, crushedAmount);
            GTRecipeBuilder b4 = GTNARecipeType.ORE_PROCESSING_RECIPES
                    .recipeBuilder(idPrefix + "4_" + material.getName())
                    .circuitMeta(4)
                    .inputItems(inputPrefix, material)
                    .inputFluids(DistilledWater, IntegratedOreMath.washFluidAmount(crushedAmount))
                    .outputItems(dustStack)
                    .chancedOutput(byproductStack, step1Chance, step1Boost)
                    .chancedOutput(dust, byproductMaterial, crushedAmount, "1/3", 0)
                    .outputItems(dust, Stone, crushedAmount)
                    .chancedOutput(exquisiteStack, highSifter ? 500 : 300, highSifter ? 150 : 100)
                    .chancedOutput(flawlessStack, highSifter ? 1500 : 1000, highSifter ? 200 : 150)
                    .chancedOutput(gemStack, highSifter ? 5000 : 3500, highSifter ? 1000 : 500)
                    .chancedOutput(dust, byproductMaterial1, crushedAmount, "1/9", 0)
                    .duration(IntegratedOreMath.duration(4, mass, crushedAmount))
                    .EUt(30);
            addSecondaryOutputs(b4, rawOreInput, 4, crushedAmount);
            b4.save(provider);
        }

        // 5..7: the chemical bath branch, only for materials with a washing fluid.
        ObjectIntPair<Material> washedIn = property.getWashedIn();
        if (!washedIn.first().isNull()) {
            Material washingByproduct = property.getOreByProduct(3, material);
            int washAmount = clampInt((long) washedIn.secondInt() * crushedAmount);
            ItemStack washingStack = ChemicalHelper.get(dust, washingByproduct,
                    property.getByProductMultiplier() * crushedAmount);
            ItemStack stoneStack = ChemicalHelper.get(dust, Stone, crushedAmount);

            // 5 macerate - chemical bath - thermal centrifuge - macerate
            if (canCentrifuge) {
                GTRecipeBuilder b5 = GTNARecipeType.ORE_PROCESSING_RECIPES
                        .recipeBuilder(idPrefix + "5_" + material.getName())
                        .circuitMeta(5)
                        .inputItems(inputPrefix, material)
                        .inputFluids(washedIn.first(), washAmount)
                        .outputItems(dustStack)
                        .chancedOutput(byproductStack, step1Chance, step1Boost)
                        .chancedOutput(washingStack, 7000, 580)
                        .chancedOutput(stoneStack, 4000, 650)
                        .chancedOutput(dust, byproductMaterial1, crushedAmount, "1/3", 0)
                        .chancedOutput(byproductStack2, 1400, 850)
                        .duration(IntegratedOreMath.duration(5, mass, crushedAmount))
                        .EUt(30);
                addSecondaryOutputs(b5, rawOreInput, 5, crushedAmount);
                b5.save(provider);
            }

            // 6 macerate - chemical bath - macerate - centrifuge
            GTRecipeBuilder b6 = GTNARecipeType.ORE_PROCESSING_RECIPES
                    .recipeBuilder(idPrefix + "6_" + material.getName())
                    .circuitMeta(6)
                    .inputItems(inputPrefix, material)
                    .inputFluids(washedIn.first(), washAmount)
                    .outputItems(dustStack)
                    .chancedOutput(byproductStack, step1Chance, step1Boost)
                    .chancedOutput(washingStack, 7000, 580)
                    .chancedOutput(stoneStack, 4000, 650)
                    .chancedOutput(byproductStack1, 1400, 850)
                    .chancedOutput(dust, byproductMaterial1, crushedAmount, "1/9", 0)
                    .duration(IntegratedOreMath.duration(6, mass, crushedAmount))
                    .EUt(30);
            addSecondaryOutputs(b6, rawOreInput, 6, crushedAmount);
            b6.save(provider);

            // 7 macerate - chemical bath - sift - centrifuge
            if (material.hasProperty(PropertyKey.GEM)) {
                ItemStack exquisiteStack = ChemicalHelper.get(gemExquisite, material, crushedAmount);
                ItemStack flawlessStack = ChemicalHelper.get(gemFlawless, material, crushedAmount);
                ItemStack gemStack = ChemicalHelper.get(gem, material, crushedAmount);
                GTRecipeBuilder b7 = GTNARecipeType.ORE_PROCESSING_RECIPES
                        .recipeBuilder(idPrefix + "7_" + material.getName())
                        .circuitMeta(7)
                        .inputItems(inputPrefix, material)
                        .inputFluids(washedIn.first(), washAmount)
                        .outputItems(dustStack)
                        .chancedOutput(byproductStack, step1Chance, step1Boost)
                        .chancedOutput(washingStack, 7000, 580)
                        .chancedOutput(stoneStack, 4000, 650)
                        .chancedOutput(exquisiteStack, highSifter ? 500 : 300, highSifter ? 150 : 100)
                        .chancedOutput(flawlessStack, highSifter ? 1500 : 1000, highSifter ? 200 : 150)
                        .chancedOutput(gemStack, highSifter ? 5000 : 3500, highSifter ? 1000 : 500)
                        .chancedOutput(dust, byproductMaterial1, crushedAmount, "1/9", 0)
                        .duration(IntegratedOreMath.duration(7, mass, crushedAmount))
                        .EUt(30);
                addSecondaryOutputs(b7, rawOreInput, 7, crushedAmount);
                b7.save(provider);
            }
        }
    }

    /**
     * GTLCore's secondary-material byproducts: the stone-ore chain adds every declared secondary to
     * every circuit at 67%% (boosted 8%% per tier); the raw-ore chain only adds the first one, and
     * only to circuit 3, at 5%% (boosted 1%%).
     */
    private static void addSecondaryOutputs(GTRecipeBuilder builder, boolean rawOreInput, int circuit,
                                            int crushedAmount) {
        if (rawOreInput) {
            if (circuit == 3) {
                for (MaterialStack secondary : ore.secondaryMaterials()) {
                    if (secondary.material().hasProperty(PropertyKey.DUST)) {
                        builder.chancedOutput(ChemicalHelper.getGem(secondary), 500, 100);
                        break;
                    }
                }
            }
            return;
        }
        for (MaterialStack secondary : ore.secondaryMaterials()) {
            if (secondary.material().hasProperty(PropertyKey.DUST)) {
                builder.chancedOutput(ChemicalHelper.getGem(secondary).copyWithCount(crushedAmount), 6700, 800);
            }
        }
    }

    private static int clampInt(long value) {
        return (int) Math.min(Integer.MAX_VALUE, value);
    }
}
