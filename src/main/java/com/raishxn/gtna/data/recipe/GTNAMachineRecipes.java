package com.raishxn.gtna.data.recipe;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.raishxn.gtna.common.data.GTNABlocks;
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
                .define('A', Objects.requireNonNull(ChemicalHelper.getTag(TagPrefix.plate, GTNAMaterials.Stronze)))
                .define('B', GTMultiMachines.STEAM_GRINDER.asStack().getItem())
                .define('C', GTNAItems.PRECISION_STEAM_COMPONENT.get())
                .unlockedBy("has_stronze_plate", InventoryChangeTrigger.TriggerInstance.hasItems(ChemicalHelper.get(TagPrefix.plate, GTNAMaterials.Stronze).getItem()))
                .save(provider);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNAMachines.WIRELESS_STEAM_INPUT_HATCH.asStack().getItem())
                .pattern("ABA")
                .pattern("CDC")
                .pattern("ABA")
                .define('A', GTBlocks.CASING_BRONZE_BRICKS.get())
                .define('B', GTNAItems.HYDRAULIC_REGULATOR.get())
                .define('C', ChemicalHelper.get(TagPrefix.pipeHugeFluid, GTNAMaterials.Stronze).getItem())
                .define('D', GTMachines.ITEM_IMPORT_BUS[1].asStack().getItem())
                .unlockedBy("has_hydraulic_regulator", InventoryChangeTrigger.TriggerInstance.hasItems(GTNAItems.HYDRAULIC_REGULATOR.get()))
                .save(provider);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNAMachines.WIRELESS_STEAM_OUTPUT_HATCH.asStack().getItem())
                .pattern("ABA")
                .pattern("CDC")
                .pattern("ABA")
                .define('A', GTBlocks.CASING_BRONZE_BRICKS.get())
                .define('B', GTNAItems.HYDRAULIC_REGULATOR.get())
                .define('C', ChemicalHelper.get(TagPrefix.pipeHugeFluid, GTNAMaterials.Stronze).getItem())
                .define('D', GTMachines.ITEM_EXPORT_BUS[1].asStack().getItem())
                .unlockedBy("has_hydraulic_regulator", InventoryChangeTrigger.TriggerInstance.hasItems(GTNAItems.HYDRAULIC_REGULATOR.get()))
                .save(provider);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNAMachines.MEGA_PRESSURE_SOLAR_BOILER.asStack().getItem())
                .pattern("AAA")
                .pattern("BCB")
                .pattern("DED")
                .define('A', GTNABlocks.SOLAR_BOILING_CELL.get())
                .define('B', GTNAItems.HYDRAULIC_PUMP.get())
                .define('C', GTBlocks.CASING_STEEL_SOLID.get())
                .define('D', GTBlocks.CASING_BRONZE_BRICKS.get())
                .define('E', GTBlocks.CASING_STEEL_SOLID.get())
                .unlockedBy("has_solar_boiling_cell", InventoryChangeTrigger.TriggerInstance.hasItems(GTNABlocks.SOLAR_BOILING_CELL.get().asItem()))
                .save(provider);
    }
}