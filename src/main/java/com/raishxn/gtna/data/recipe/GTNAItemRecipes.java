package com.raishxn.gtna.data.recipe;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.raishxn.gtna.common.data.GTNAItems;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;

import java.util.function.Consumer;

public class GTNAItemRecipes {

    public static void register(Consumer<FinishedRecipe> provider) {

        // Receita do Precision Steam Component
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNAItems.PRECISION_STEAM_COMPONENT.get())
                .pattern("ABA")
                .pattern("CDC")
                .pattern("EBE")
                // A: Bronze Rod
                .define('A', ChemicalHelper.getTag(TagPrefix.rod, GTMaterials.Bronze))
                // B: Small Bronze Gear
                .define('B', ChemicalHelper.getTag(TagPrefix.gearSmall, GTMaterials.Bronze))
                // C: Small Bronze Spring
                .define('C', ChemicalHelper.getTag(TagPrefix.springSmall, GTMaterials.Bronze))
                // D: Small Steel Spring
                .define('D', ChemicalHelper.getTag(TagPrefix.springSmall, GTMaterials.Steel))
                // E: Bronze Gear
                .define('E', ChemicalHelper.getTag(TagPrefix.gear, GTMaterials.Bronze))
                // "has" substituído pela verificação direta de inventário
                .unlockedBy("has_bronze_ingot", InventoryChangeTrigger.TriggerInstance.hasItems(ChemicalHelper.get(TagPrefix.ingot, GTMaterials.Bronze).getItem()))
                .save(provider);
    }
}