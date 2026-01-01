package com.raishxn.gtna.data.recipe;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.raishxn.gtna.common.data.GTNAItems;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;
public class GTNAItemRecipes {
    public static void register(Consumer<FinishedRecipe> provider) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNAItems.PRECISION_STEAM_COMPONENT.get())
                .pattern("ABA")
                .pattern("CDC")
                .pattern("EBE")
                .define('A', ChemicalHelper.getTag(TagPrefix.rod, GTMaterials.Bronze))
                .define('B', ChemicalHelper.getTag(TagPrefix.gearSmall, GTMaterials.Bronze))
                .define('C', ChemicalHelper.getTag(TagPrefix.springSmall, GTMaterials.Bronze))
                .define('D', ChemicalHelper.getTag(TagPrefix.springSmall, GTMaterials.Steel))
                .define('E', ChemicalHelper.getTag(TagPrefix.gear, GTMaterials.Bronze))
                .unlockedBy("has_bronze_ingot", InventoryChangeTrigger.TriggerInstance.hasItems(ChemicalHelper.get(TagPrefix.ingot, GTMaterials.Bronze).getItem()))
                .save(provider);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, GTNAItems.STRUCTURE_DETECT.get())
                .requires(Items.BOOK, 8)
                .requires(GTItems.TERMINAL.get())
                .unlockedBy("has_books", InventoryChangeTrigger.TriggerInstance.hasItems(Items.BOOK))
                .save(provider);
    }
}