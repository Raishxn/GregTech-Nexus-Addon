package com.raishxn.gtna.data.recipe;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntCircuitIngredient;
import com.gregtechceu.gtceu.common.data.*;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.raishxn.gtna.common.data.*;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

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
                .define('B', GTBlocks.CASING_BRONZE_PIPE.get())
                .define('C', Items.WATER_BUCKET)
                .define('D', ChemicalHelper.get(TagPrefix.frameGt, GTNAMaterials.ClayCompound).getItem())
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
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNAMachines.STEAM_WOODCUTTER.asStack().getItem())
                .pattern("AAA")
                .pattern("BCB")
                .pattern("DED")
                .define('A', GTNABlocks.BRONZE_REINFORCED_WOOD.get())
                .define('B', Items.GLASS)
                .define('C', Items.DIRT)
                .define('D', ChemicalHelper.get(TagPrefix.frameGt, GTMaterials.Wood).getItem())
                .define('E', GTNAItems.HYDRAULIC_PUMP.get())
                .unlockedBy("has_hydraulic_pump", InventoryChangeTrigger.TriggerInstance.hasItems(GTNAItems.HYDRAULIC_PUMP.get()))
                .save(provider);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNAMachines.LEAP_FORWARD_ONE_BLAST_FURNACE.asStack().getItem())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', ChemicalHelper.get(TagPrefix.plateDouble, GTMaterials.Bronze).getItem())
                .define('B', GTNAItems.PRECISION_STEAM_COMPONENT.get())
                .define('C', GTMultiMachines.PRIMITIVE_BLAST_FURNACE.asStack().getItem())
                .unlockedBy("has_precision_steam_component", InventoryChangeTrigger.TriggerInstance.hasItems(GTNAItems.PRECISION_STEAM_COMPONENT.get()))
                .save(provider);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNAMachines.HUGE_STEAM_INPUT_BUS.asStack().getItem())
                .pattern("AAA")
                .pattern("ABA")
                .pattern("AAA")
                .define('A', GTMachines.BRONZE_CRATE.asStack().getItem())
                .define('B', GTMachines.STEAM_IMPORT_BUS.asStack().getItem())
                .unlockedBy("has_steam_import", InventoryChangeTrigger.TriggerInstance.hasItems(GTMachines.STEAM_IMPORT_BUS.asStack().getItem()))
                .save(provider);

        // --- Huge Steam Output Bus ---
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNAMachines.HUGE_STEAM_OUTPUT_BUS.asStack().getItem())
                .pattern("AAA")
                .pattern("ABA")
                .pattern("AAA")
                .define('A', GTMachines.BRONZE_CRATE.asStack().getItem())
                .define('B', GTMachines.STEAM_EXPORT_BUS.asStack().getItem())
                .unlockedBy("has_steam_export", InventoryChangeTrigger.TriggerInstance.hasItems(GTMachines.STEAM_EXPORT_BUS.asStack().getItem()))
                .save(provider);

        // --- Wireless Steam Input Hatch (STEEL) ---
        GTNARecipeType.HYDRAULIC_MANUFACTURING.recipeBuilder("wireless_steam_input_hatch_steel")
                .inputItems(GTMachines.STEEL_DRUM.asStack().getItem(), 8) //Mudado de .get() para .asStack().getItem()
                .inputItems(GTNAMachines.WIRELESS_STEAM_INPUT_HATCH.asStack().getItem(), 1)
                .outputItems(GTNAMachines.WIRELESS_STEAM_INPUT_HATCH_STEEL.asStack())
                .duration(400)
                .EUt(120)
                .save(provider);

        // --- Wireless Steam Output Hatch (STEEL) ---
        GTNARecipeType.HYDRAULIC_MANUFACTURING.recipeBuilder("wireless_steam_output_hatch_steel")
                .inputItems(GTMachines.STEEL_DRUM.asStack().getItem(), 8)
                .inputItems(GTNAMachines.WIRELESS_STEAM_OUTPUT_HATCH.asStack().getItem(), 1)
                .outputItems(GTNAMachines.WIRELESS_STEAM_OUTPUT_HATCH_STEEL.asStack())
                .duration(400)
                .EUt(120)
                .save(provider);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNAMachines.INFERNAL_COKE_OVEN.asStack().getItem())
                .pattern("ABA")
                .pattern("CDC")
                .pattern("ABA")
                .define('A', Blocks.NETHER_BRICKS)
                .define('B', ChemicalHelper.get(TagPrefix.plate, GTNAMaterials.Breel).getItem())
                .define('C', ChemicalHelper.get(TagPrefix.plate, GTNAMaterials.Stronze).getItem())
                .define('D', GTMultiMachines.COKE_OVEN.asStack().getItem()) // Coke Oven do GTCEu
                .unlockedBy("has_coke_oven", InventoryChangeTrigger.TriggerInstance.hasItems(GTMultiMachines.COKE_OVEN.asStack().getItem()))
                .save(provider);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNAMachines.HYPER_PRESSURE_REACTOR.asStack().getItem())
                .pattern("ABA")
                .pattern("CDC")
                .pattern("ABA")
                .define('A', ChemicalHelper.get(TagPrefix.pipeHugeFluid, GTNAMaterials.Breel).getItem())
                .define('B', Items.EMERALD)
                .define('C', ChemicalHelper.get(TagPrefix.plate, GTMaterials.Beryllium).getItem())
                .define('D', GTNAMachines.MEGA_PRESSURE_SOLAR_BOILER.asStack().getItem())
                .unlockedBy("has_mega_solar", InventoryChangeTrigger.TriggerInstance.hasItems(GTNAMachines.MEGA_PRESSURE_SOLAR_BOILER.asStack().getItem()))
                .save(provider);

        // --- Compact Hyper Pressure Reactor ---
        GTNARecipeType.HYDRAULIC_MANUFACTURING.recipeBuilder("compact_hyper_pressure_reactor")
                .inputItems(GTNAMachines.HYPER_PRESSURE_REACTOR.asStack().getItem(), 64)
                .inputItems(GTNAItems.HYDRAULIC_VAPOR_GENERATOR.get(), 8)
                .outputItems(GTNAMachines.COMPACT_HYPER_PRESSURE_REACTOR.asStack())
                .duration(2400) // 120s * 20 ticks = 2400 ticks
                .EUt(1600)
                .save(provider);

        GTNARecipeType.HYDRAULIC_MANUFACTURING.recipeBuilder("void_miner_steam_gate_aged")
                .inputItems(GTNAMachines.LARGE_STEAM_FURNACE.asStack().getItem(), 1)
                .inputItems(GTNAMachines.LARGE_STEAM_CRUSHER.asStack().getItem(), 1)
                .inputItems(ChemicalHelper.get(TagPrefix.frameGt, GTNAMaterials.Breel).getItem(), 9)
                .inputItems(ChemicalHelper.get(TagPrefix.plate, GTNAMaterials.Stronze).getItem(), 9)
                .inputItems(GTNAItems.HYDRAULIC_MOTOR.get(), 9)
                .inputItems(GTNAItems.HYDRAULIC_STEAM_RECEIVER.get(), 9)
                .inputItems(GTNAItems.HYDRAULIC_VAPOR_GENERATOR.get(), 9)
                .inputItems(ChemicalHelper.get(TagPrefix.screw, GTNAMaterials.Breel).getItem(), 64)
                .inputFluids(GTNAMaterials.DenseSupercriticalSteam.getFluid(10000))
                .inputFluids(GTMaterials.Lava.getFluid(10000))
                .inputFluids(GTMaterials.Water.getFluid(10000))
                .outputItems(GTNAMachines.VOID_MINER_STEAM_GATE_AGED.asStack())
                .duration(120 * 20)
                .EUt(15000)
                .save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("industrial_slaughterhouse")
                .inputItems(ChemicalHelper.get(TagPrefix.frameGt, GTMaterials.Steel), 1)
                .inputItems(GTMachines.WORLD_ACCELERATOR[GTValues.LV].asStack().getItem(), 1)
                .inputItems(CustomTags.LV_CIRCUITS, 4)
                .inputItems(GTItems.ELECTRIC_MOTOR_LV, 8)
                .inputItems(GTItems.ROBOT_ARM_LV, 4)
                .inputItems(ChemicalHelper.get(TagPrefix.gear, GTMaterials.Invar), 4)
                .inputItems(ChemicalHelper.get(TagPrefix.plateDouble, GTMaterials.Steel), 4)
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(288))
                .outputItems(GTNAMachines.INDUSTRIAL_SLAUGHTERHOUSE.asStack())
                .duration(400)
                .EUt(30)
                .save(provider);

        GTNARecipeType.SLAUGHTERHOUSE_RECIPES.recipeBuilder("slaughterhouse_passive")
                .notConsumable(IntCircuitIngredient.of(1))
                .duration(40)
                .EUt(1000)
                .save(provider);

        GTNARecipeType.SLAUGHTERHOUSE_RECIPES.recipeBuilder("slaughterhouse_hostile")
                .notConsumable(IntCircuitIngredient.of(2))
                .duration(40)
                .EUt(2560)
                .save(provider);
        GTNARecipeType.SLAUGHTERHOUSE_RECIPES.recipeBuilder("slaughterhouse_boss")
                .notConsumable(IntCircuitIngredient.of(3))
                .duration(40)
                .EUt(32000)
                .save(provider);
        GTNARecipeType.SLAUGHTERHOUSE_RECIPES.recipeBuilder("slaughterhouse_ender_dragon")
                .notConsumable(IntCircuitIngredient.of(4))
                .duration(40)
                .EUt(120000)
                .save(provider);
        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("test")
                .inputItems(Items.NETHER_STAR,1)
                .outputItems(ChemicalHelper.get(TagPrefix.block, GTMaterials.NetherStar).getItem(), 9)
                .duration(6000)
                .EUt(10)
                .save(provider);


    }
}