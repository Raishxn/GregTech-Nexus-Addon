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
                .define('A', ChemicalHelper.get(TagPrefix.rod, GTMaterials.Bronze).getItem())
                .define('B', ChemicalHelper.get(TagPrefix.gearSmall, GTMaterials.Bronze).getItem())
                .define('C', ChemicalHelper.get(TagPrefix.springSmall, GTMaterials.Bronze).getItem())
                .define('D', ChemicalHelper.get(TagPrefix.springSmall, GTMaterials.Steel).getItem())
                .define('E', ChemicalHelper.get(TagPrefix.gear, GTMaterials.Bronze).getItem())
                .unlockedBy("has_bronze_ingot", InventoryChangeTrigger.TriggerInstance.hasItems(ChemicalHelper.get(TagPrefix.ingot, GTMaterials.Bronze).getItem()))
                .save(provider);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, GTNAItems.STRUCTURE_DETECT.get())
                .requires(Items.BOOK, 8)
                .requires(GTItems.TERMINAL.get())
                .unlockedBy("has_books", InventoryChangeTrigger.TriggerInstance.hasItems(Items.BOOK))
                .save(provider);
        // Hydraulic Motor
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNAItems.HYDRAULIC_MOTOR.get())
                .pattern("ABC")
                .pattern("BDB")
                .pattern("CBA")
                .define('A', ChemicalHelper.get(TagPrefix.gear, GTMaterials.Bronze).getItem())
                .define('B', ChemicalHelper.get(TagPrefix.pipeTinyFluid, GTMaterials.Bronze).getItem())
                .define('C', ChemicalHelper.get(TagPrefix.rod, GTMaterials.Iron).getItem())
                .define('D', ChemicalHelper.get(TagPrefix.rotor, GTMaterials.Iron).getItem())
                .unlockedBy("has_bronze_gear", InventoryChangeTrigger.TriggerInstance.hasItems(ChemicalHelper.get(TagPrefix.gear, GTMaterials.Bronze).getItem()))
                .save(provider);

        // Hydraulic Conveyor
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNAItems.HYDRAULIC_CONVEYOR.get())
                .pattern("AAA")
                .pattern("BCB")
                .pattern("AAA")
                .define('A', ChemicalHelper.get(TagPrefix.plate, GTMaterials.Rubber).getItem())
                .define('B', GTNAItems.HYDRAULIC_MOTOR.get())
                .define('C', ChemicalHelper.get(TagPrefix.gear, GTMaterials.Clay).getItem())
                .unlockedBy("has_hydraulic_motor", InventoryChangeTrigger.TriggerInstance.hasItems(GTNAItems.HYDRAULIC_MOTOR.get()))
                .save(provider);
    }
}