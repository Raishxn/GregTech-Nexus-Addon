package com.raishxn.gtna.data.recipe;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.raishxn.gtna.common.data.GTNAItems;
import com.raishxn.gtna.common.data.GTNAMachines; // Certifique-se que está importado
import com.raishxn.gtna.common.data.GTNAMaterials;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.function.Consumer;

public class GTNAMachineRecipes {

    public static void init(Consumer<FinishedRecipe> provider) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNAMachines.LARGE_STEAM_CRUSHER.asStack().getItem())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                // A: Stronze Plate
                .define('A', Objects.requireNonNull(ChemicalHelper.getTag(TagPrefix.plate, GTNAMaterials.Stronze)))
                // B: Steam Grinder (Bronze)
                .define('B', GTMultiMachines.STEAM_GRINDER.asStack().getItem())
                // C: Precision Steam Component
                .define('C', GTNAItems.PRECISION_STEAM_COMPONENT.get())
                .save(provider, new ResourceLocation("gtna", "large_steam_crusher"));}
}