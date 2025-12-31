package com.raishxn.gtna.data.recipe;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.raishxn.gtna.common.data.GTNAItems;
import com.raishxn.gtna.common.data.GTNAMachines;
import com.raishxn.gtna.common.data.GTNAMaterials;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;

import java.util.Objects;
import java.util.function.Consumer;

public class GTNAMachineRecipes {

    public static void register(Consumer<FinishedRecipe> provider) {

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
                // Critério de desbloqueio corrigido
                .unlockedBy("has_stronze_plate", InventoryChangeTrigger.TriggerInstance.hasItems(ChemicalHelper.get(TagPrefix.plate, GTNAMaterials.Stronze).getItem()))
                .save(provider);
    }
}