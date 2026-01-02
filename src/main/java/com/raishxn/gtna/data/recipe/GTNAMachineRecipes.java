package com.raishxn.gtna.data.recipe;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.raishxn.gtna.common.data.*;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;

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
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNAMachines.LARGE_STEAM_FURNACE.asStack().getItem())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("DBD")
                .define('A', GTNAItems.PRECISION_STEAM_COMPONENT.get())
                .define('B', ChemicalHelper.get(TagPrefix.pipeNormalFluid, GTNAMaterials.Stronze).getItem())
                .define('C', GTMultiMachines.STEAM_OVEN.asStack().getItem())
                .define('D', ChemicalHelper.get(TagPrefix.pipeTinyFluid, GTNAMaterials.Breel).getItem())
                .unlockedBy("has_precision_steam_component", InventoryChangeTrigger.TriggerInstance.hasItems(GTNAItems.PRECISION_STEAM_COMPONENT.get()))
                .save(provider);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNAMachines.LARGE_STEAM_ALLOY_SMELTER.asStack().getItem())
                .pattern("ABA")
                .pattern("CDE")
                .pattern("AFA")
                .define('A', ChemicalHelper.get(TagPrefix.plate, GTMaterials.Bronze).getItem())
                .define('B', ChemicalHelper.get(TagPrefix.rotor, GTMaterials.Bronze).getItem())
                .define('C', GTNAItems.HYDRAULIC_CONVEYOR.get())
                .define('D', Items.CAULDRON)
                .define('E', ChemicalHelper.get(TagPrefix.pipeNormalFluid, GTMaterials.Bronze).getItem())
                .define('F', GTMachines.STEAM_ALLOY_SMELTER.right().asStack().getItem())
                .unlockedBy("has_hydraulic_conveyor", InventoryChangeTrigger.TriggerInstance.hasItems(GTNAItems.HYDRAULIC_CONVEYOR.get()))
                .save(provider);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNAMachines.STEAM_COBBLER.asStack().getItem())
                .pattern("ABA")
                .pattern("CDE")
                .pattern("ABA")
                .define('A', GTBlocks.CASING_BRONZE_BRICKS.get())
                .define('B', GTBlocks.CASING_BRONZE_PIPE.get()) // Assumindo que você tem acesso a isso via GTBlocks
                .define('C', Items.WATER_BUCKET)
                .define('D', ChemicalHelper.get(TagPrefix.frameGt, GTNAMaterials.ClayCompound).getItem())// Assumindo nome do item no seu mod
                .define('E', Items.LAVA_BUCKET)
                .unlockedBy("has_clay_compound_frame", InventoryChangeTrigger.TriggerInstance.hasItems(ChemicalHelper.get(TagPrefix.frameGt, GTNAMaterials.ClayCompound).getItem()))
                .save(provider);

        GTNARecipeType.HYDRAULIC_MANUFACTURING.recipeBuilder("stone_superheater_controller")
                .inputItems(GTNABlocks.STRONZE_WRAPPED_CASING.get(), 1)
                .inputItems(GTNAItems.HYDRAULIC_MOTOR.get(), 2)
                .inputItems(ChemicalHelper.get(TagPrefix.pipeNormalFluid, GTNAMaterials.Stronze).getItem(), 2)
                .inputItems(ChemicalHelper.get(TagPrefix.pipeNormalFluid, GTNAMaterials.Breel).getItem(), 2)
                .outputItems(GTNAMachines.STONE_SUPERHEATER.asStack())
                .duration(400)
                .EUt(250)
                .save(provider);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNAMachines.STEAM_MANUFACTURER.asStack().getItem())
                .pattern("AAA")
                .pattern("BCB")
                .pattern("DED")
                .define('A', GTNAItems.HYDRAULIC_ARM.get())
                .define('B', GTNABlocks.HYDRAULIC_ASSEMBLER_CASING.get())
                .define('C', ChemicalHelper.get(TagPrefix.plateDouble, GTNAMaterials.Stronze).getItem()) // Stronze Double Plate
                .define('D', GTBlocks.CASING_STEEL_GEARBOX.get())
                .define('E', GTNAItems.HYDRAULIC_CONVEYOR.get())
                .unlockedBy("has_hydraulic_casing", InventoryChangeTrigger.TriggerInstance.hasItems(GTNABlocks.HYDRAULIC_ASSEMBLER_CASING.get()))
                .save(provider);

    }
}