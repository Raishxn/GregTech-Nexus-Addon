package com.raishxn.gtna.data.recipe;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

public class VoidminerRecipes {

    public static void register(Consumer<FinishedRecipe> provider) {


        GTRecipeBuilder.ofRaw()
                .inputFluids(GTMaterials.DrillingFluid.getFluid(10000))
                .outputItems(ChemicalHelper.get(TagPrefix.rawOre, GTMaterials.Gold).getItem(), 64)
                .outputItems(ChemicalHelper.get(TagPrefix.rawOre, GTMaterials.Copper).getItem(), 64)
                .outputItems(ChemicalHelper.get(TagPrefix.rawOre, GTMaterials.Iron).getItem(), 64)
                .outputItems(ChemicalHelper.get(TagPrefix.rawOre, GTMaterials.Cobalt).getItem(), 64)
                .outputItems(ChemicalHelper.get(TagPrefix.rawOre, GTMaterials.Coal).getItem(), 64)
                .duration(2400)
                .EUt(750)
                .save(provider);

    }
}
