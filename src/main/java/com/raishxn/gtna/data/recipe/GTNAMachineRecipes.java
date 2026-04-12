package com.raishxn.gtna.data.recipe;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntCircuitIngredient;
import com.gregtechceu.gtceu.common.data.*;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import com.raishxn.gtna.common.data.*;
import appeng.core.definitions.AEBlocks;

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
                .unlockedBy("has_stronze_plate",
                        InventoryChangeTrigger.TriggerInstance
                                .hasItems(ChemicalHelper.get(TagPrefix.plate, GTNAMaterials.Stronze).getItem()))
                .save(provider);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNAMachines.WIRELESS_STEAM_INPUT_HATCH.asStack().getItem())
                .pattern("ABA")
                .pattern("CDC")
                .pattern("ABA")
                .define('A', GTBlocks.CASING_BRONZE_BRICKS.get())
                .define('B', GTNAItems.HYDRAULIC_REGULATOR.get())
                .define('C', ChemicalHelper.get(TagPrefix.pipeHugeFluid, GTNAMaterials.Stronze).getItem())
                .define('D', GTMachines.ITEM_IMPORT_BUS[1].asStack().getItem())
                .unlockedBy("has_hydraulic_regulator",
                        InventoryChangeTrigger.TriggerInstance.hasItems(GTNAItems.HYDRAULIC_REGULATOR.get()))
                .save(provider);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNAMachines.WIRELESS_STEAM_OUTPUT_HATCH.asStack().getItem())
                .pattern("ABA")
                .pattern("CDC")
                .pattern("ABA")
                .define('A', GTBlocks.CASING_BRONZE_BRICKS.get())
                .define('B', GTNAItems.HYDRAULIC_REGULATOR.get())
                .define('C', ChemicalHelper.get(TagPrefix.pipeHugeFluid, GTNAMaterials.Stronze).getItem())
                .define('D', GTMachines.ITEM_EXPORT_BUS[1].asStack().getItem())
                .unlockedBy("has_hydraulic_regulator",
                        InventoryChangeTrigger.TriggerInstance.hasItems(GTNAItems.HYDRAULIC_REGULATOR.get()))
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
                .unlockedBy("has_solar_boiling_cell",
                        InventoryChangeTrigger.TriggerInstance.hasItems(GTNABlocks.SOLAR_BOILING_CELL.get().asItem()))
                .save(provider);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNAMachines.LARGE_STEAM_FURNACE.asStack().getItem())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("DBD")
                .define('A', GTNAItems.PRECISION_STEAM_COMPONENT.get())
                .define('B', ChemicalHelper.get(TagPrefix.pipeNormalFluid, GTNAMaterials.Stronze).getItem())
                .define('C', GTMultiMachines.STEAM_OVEN.asStack().getItem())
                .define('D', ChemicalHelper.get(TagPrefix.pipeTinyFluid, GTNAMaterials.Breel).getItem())
                .unlockedBy("has_precision_steam_component",
                        InventoryChangeTrigger.TriggerInstance.hasItems(GTNAItems.PRECISION_STEAM_COMPONENT.get()))
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
                .unlockedBy("has_hydraulic_conveyor",
                        InventoryChangeTrigger.TriggerInstance.hasItems(GTNAItems.HYDRAULIC_CONVEYOR.get()))
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
                .unlockedBy("has_clay_compound_frame",
                        InventoryChangeTrigger.TriggerInstance
                                .hasItems(ChemicalHelper.get(TagPrefix.frameGt, GTNAMaterials.ClayCompound).getItem()))
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
                .define('C', ChemicalHelper.get(TagPrefix.plateDouble, GTNAMaterials.Stronze).getItem()) // Stronze
                                                                                                         // Double Plate
                .define('D', GTBlocks.CASING_STEEL_GEARBOX.get())
                .define('E', GTNAItems.HYDRAULIC_CONVEYOR.get())
                .unlockedBy("has_hydraulic_casing",
                        InventoryChangeTrigger.TriggerInstance.hasItems(GTNABlocks.HYDRAULIC_ASSEMBLER_CASING.get()))
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
                .unlockedBy("has_hydraulic_pump",
                        InventoryChangeTrigger.TriggerInstance.hasItems(GTNAItems.HYDRAULIC_PUMP.get()))
                .save(provider);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNAMachines.LEAP_FORWARD_ONE_BLAST_FURNACE.asStack().getItem())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', ChemicalHelper.get(TagPrefix.plateDouble, GTMaterials.Bronze).getItem())
                .define('B', GTNAItems.PRECISION_STEAM_COMPONENT.get())
                .define('C', GTMultiMachines.PRIMITIVE_BLAST_FURNACE.asStack().getItem())
                .unlockedBy("has_precision_steam_component",
                        InventoryChangeTrigger.TriggerInstance.hasItems(GTNAItems.PRECISION_STEAM_COMPONENT.get()))
                .save(provider);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNAMachines.HUGE_STEAM_INPUT_BUS.asStack().getItem())
                .pattern("AAA")
                .pattern("ABA")
                .pattern("AAA")
                .define('A', GTMachines.BRONZE_CRATE.asStack().getItem())
                .define('B', GTMachines.STEAM_IMPORT_BUS.asStack().getItem())
                .unlockedBy("has_steam_import",
                        InventoryChangeTrigger.TriggerInstance
                                .hasItems(GTMachines.STEAM_IMPORT_BUS.asStack().getItem()))
                .save(provider);

        // --- Huge Steam Output Bus ---
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNAMachines.HUGE_STEAM_OUTPUT_BUS.asStack().getItem())
                .pattern("AAA")
                .pattern("ABA")
                .pattern("AAA")
                .define('A', GTMachines.BRONZE_CRATE.asStack().getItem())
                .define('B', GTMachines.STEAM_EXPORT_BUS.asStack().getItem())
                .unlockedBy("has_steam_export",
                        InventoryChangeTrigger.TriggerInstance
                                .hasItems(GTMachines.STEAM_EXPORT_BUS.asStack().getItem()))
                .save(provider);

        // --- Wireless Steam Input Hatch (STEEL) ---
        GTNARecipeType.HYDRAULIC_MANUFACTURING.recipeBuilder("wireless_steam_input_hatch_steel")
                .inputItems(GTMachines.STEEL_DRUM.asStack().getItem(), 8) // Mudado de .get() para .asStack().getItem()
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
                .unlockedBy("has_coke_oven",
                        InventoryChangeTrigger.TriggerInstance.hasItems(GTMultiMachines.COKE_OVEN.asStack().getItem()))
                .save(provider);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNAMachines.HYPER_PRESSURE_REACTOR.asStack().getItem())
                .pattern("ABA")
                .pattern("CDC")
                .pattern("ABA")
                .define('A', ChemicalHelper.get(TagPrefix.pipeHugeFluid, GTNAMaterials.Breel).getItem())
                .define('B', Items.EMERALD)
                .define('C', ChemicalHelper.get(TagPrefix.plate, GTMaterials.Beryllium).getItem())
                .define('D', GTNAMachines.MEGA_PRESSURE_SOLAR_BOILER.asStack().getItem())
                .unlockedBy("has_mega_solar",
                        InventoryChangeTrigger.TriggerInstance
                                .hasItems(GTNAMachines.MEGA_PRESSURE_SOLAR_BOILER.asStack().getItem()))
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
                .inputItems(ChemicalHelper.get(TagPrefix.frameGt, GTMaterials.Steel).getItem(), 1)
                .inputItems(GTMachines.WORLD_ACCELERATOR[GTValues.LV].asStack().getItem(), 1)
                .inputItems(CustomTags.LV_CIRCUITS, 4)
                .inputItems(GTItems.ELECTRIC_MOTOR_LV, 8)
                .inputItems(GTItems.ROBOT_ARM_LV, 4)
                .inputItems(ChemicalHelper.get(TagPrefix.gear, GTMaterials.Invar).getItem(), 4)
                .inputItems(ChemicalHelper.get(TagPrefix.plateDouble, GTMaterials.Steel).getItem(), 4)
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(288))
                .outputItems(GTNAMachines.INDUSTRIAL_SLAUGHTERHOUSE.asStack())
                .duration(400)
                .EUt(30)
                .save(provider);

        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder("artificial_star")
                .inputItems(GTNABlocks.GRAVITON_FIELD_CONSTRAINT_CASING.get(), 4)
                .inputItems(GTNABlocks.ANNIHILATE_CORE.get())
                .inputItems(GTItems.EMITTER_UXV, 4)
                .inputItems(GTItems.SENSOR_UXV, 4)
                .inputItems(CustomTags.OpV_CIRCUITS, 4)
                .inputItems(GTItems.FIELD_GENERATOR_UXV, 16)
                .inputItems(ChemicalHelper.get(TagPrefix.plateDouble, GTMaterials.Neutronium).getItem(), 8)
                .inputItems(ChemicalHelper.get(TagPrefix.plateDouble, GTMaterials.NaquadahAlloy).getItem(), 8)
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(4000))
                .inputFluids(GTMaterials.Europium.getFluid(8192))
                .inputFluids(GTMaterials.Naquadria.getFluid(4000))
                .outputItems(GTNAMachines.ARTIFICIAL_STAR.asStack())
                .duration(1800)
                .EUt(125829120)
                .stationResearch(b -> b.researchStack(GTNABlocks.ANNIHILATE_CORE.asStack())
                        .CWUt(4096)
                        .EUt(125829120))
                .save(provider);

        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder("eye_of_harmony")
                .inputItems(GTNABlocks.DIMENSION_INJECTION_CASING.get(), 16)
                .inputItems(GTNABlocks.SPACETIME_COMPRESSION_FIELD_GENERATOR.get(), 16)
                .inputItems(GTNABlocks.DIMENSIONAL_STABILITY_CASING.get(), 16)
                .inputItems(GTNAMachines.ARTIFICIAL_STAR.asStack().getItem(), 4)
                .inputItems(GTItems.FIELD_GENERATOR_OpV, 16)
                .inputItems(GTItems.EMITTER_OpV, 16)
                .inputItems(GTItems.SENSOR_OpV, 16)
                .inputItems(GTItems.ROBOT_ARM_OpV, 16)
                .inputItems(GTItems.ELECTRIC_PUMP_OpV, 8)
                .inputItems(GTItems.ELECTRIC_MOTOR_OpV, 8)
                .inputItems(GTItems.GRAVI_STAR, 8)
                .inputItems(CustomTags.OpV_CIRCUITS, 16)
                .inputItems(ChemicalHelper.get(TagPrefix.plateDouble, GTMaterials.Neutronium).getItem(), 32)
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(48000))
                .inputFluids(GTMaterials.Neutronium.getFluid(57600))
                .inputFluids(GTMaterials.Europium.getFluid(32000))
                .inputFluids(GTMaterials.Naquadria.getFluid(16000))
                .outputItems(GTNAMachines.EYE_OF_HARMONY.asStack())
                .duration(2400)
                .EUt(8053063680L)
                .stationResearch(b -> b.researchStack(GTNABlocks.SPACETIME_COMPRESSION_FIELD_GENERATOR.asStack())
                        .CWUt(16384)
                        .EUt(8053063680L))
                .save(provider);

        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder("nexus_molecular_forge")
                .inputItems(GTMachines.ASSEMBLER[GTValues.ZPM].asStack().getItem())
                .inputItems(GTNAMachines2.ME_CRAFT_PATTERN_HATCH.asStack().getItem(), 4)
                .inputItems("expatternprovider:assembler_matrix_crafter", 16)
                .inputItems("expatternprovider:assembler_matrix_speed", 16)
                .inputItems(GTItems.ROBOT_ARM_ZPM.asStack().getItem(), 4)
                .inputItems(GTItems.EMITTER_ZPM.asStack().getItem(), 8)
                .inputItems(CustomTags.ZPM_CIRCUITS, 8)
                .inputItems(GTBlocks.HIGH_POWER_CASING.get(), 16)
                .inputItems(GTBlocks.CASING_ASSEMBLY_LINE.get(), 16)
                .inputItems(GTBlocks.FUSION_GLASS.get(), 8)
                .inputItems(GTBlocks.ADVANCED_COMPUTER_CASING.get(), 8)
                .inputItems(GTBlocks.CASING_PALLADIUM_SUBSTATION.get(), 4)
                .inputItems(TagPrefix.wireFine, GTMaterials.Tritanium, 64)
                .inputItems(TagPrefix.plateDouble, GTMaterials.NaquadahAlloy, 8)
                .inputItems(TagPrefix.frameGt, GTMaterials.Europium, 4)
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(2304))
                .inputFluids(GTMaterials.Polybenzimidazole.getFluid(2304))
                .outputItems(GTNAMachines.NEXUS_MOLECULAR_FORGE.asStack())
                .duration(600)
                .EUt(GTValues.VA[GTValues.ZPM])
                .stationResearch(b -> b.researchStack(AEBlocks.MOLECULAR_ASSEMBLER.stack(1))
                        .CWUt(64)
                        .EUt(GTValues.VA[GTValues.ZPM]))
                .save(provider);

        GTNARecipeType.ARTIFICIAL_STAR_RECIPES.recipeBuilder("neutronium_antimatter_fuel_rod")
                .inputItems(GTNAItems.NEUTRONIUM_ANTIMATTER_FUEL_ROD.get())
                .chancedOutput(GTNAItems.ANNIHILATION_CONSTRAINER.asStack(), 9000, 0)
                .EUt(-549755813888L)
                .duration(200)
                .save(provider);

        GTNARecipeType.ARTIFICIAL_STAR_RECIPES.recipeBuilder("draconium_antimatter_fuel_rod")
                .inputItems(GTNAItems.DRACONIUM_ANTIMATTER_FUEL_ROD.get())
                .chancedOutput(GTNAItems.ANNIHILATION_CONSTRAINER.asStack(), 8000, 0)
                .EUt(-8796093022208L)
                .duration(200)
                .save(provider);

        GTNARecipeType.ARTIFICIAL_STAR_RECIPES.recipeBuilder("cosmic_neutronium_antimatter_fuel_rod")
                .inputItems(GTNAItems.COSMIC_NEUTRONIUM_ANTIMATTER_FUEL_ROD.get())
                .chancedOutput(GTNAItems.ANNIHILATION_CONSTRAINER.asStack(), 7000, 0)
                .EUt(-140737488355328L)
                .duration(200)
                .save(provider);

        GTNARecipeType.ARTIFICIAL_STAR_RECIPES.recipeBuilder("infinity_antimatter_fuel_rod")
                .inputItems(GTNAItems.INFINITY_ANTIMATTER_FUEL_ROD.get())
                .chancedOutput(GTNAItems.ANNIHILATION_CONSTRAINER.asStack(), 6000, 0)
                .EUt(-2251799813685248L)
                .duration(200)
                .save(provider);

        GTNARecipeType.COSMOS_SIMULATION_RECIPES.recipeBuilder("stellar_atmosphere")
                .inputItems(GTItems.GRAVI_STAR)
                .inputFluids(GTMaterials.UUMatter.getFluid(1000))
                .outputFluids(GTMaterials.Hydrogen.getFluid(64000000))
                .outputFluids(GTMaterials.Helium.getFluid(32000000))
                .outputFluids(GTMaterials.Oxygen.getFluid(16000000))
                .outputFluids(GTMaterials.Nitrogen.getFluid(16000000))
                .outputFluids(GTMaterials.Deuterium.getFluid(8000000))
                .outputFluids(GTMaterials.Tritium.getFluid(4000000))
                .outputFluids(GTMaterials.Helium3.getFluid(4000000))
                .outputFluids(GTMaterials.Neon.getFluid(1000000))
                .outputFluids(GTMaterials.Argon.getFluid(1000000))
                .outputFluids(GTMaterials.Krypton.getFluid(500000))
                .outputFluids(GTMaterials.Xenon.getFluid(250000))
                .duration(12000)
                .EUt(1)
                .addData("tier", 8)
                .save(provider);

        GTNARecipeType.COSMOS_SIMULATION_RECIPES.recipeBuilder("stellar_metallogenesis")
                .inputItems(GTNAItems.NEUTRONIUM_ANTIMATTER_FUEL_ROD.get())
                .inputFluids(GTMaterials.UUMatter.getFluid(4000))
                .outputItems(TagPrefix.dust, GTMaterials.Carbon, 8192)
                .outputItems(TagPrefix.dust, GTMaterials.Silicon, 4096)
                .outputItems(TagPrefix.dust, GTMaterials.Iron, 4096)
                .outputItems(TagPrefix.dust, GTMaterials.Copper, 4096)
                .outputItems(TagPrefix.dust, GTMaterials.Nickel, 2048)
                .outputItems(TagPrefix.dust, GTMaterials.Aluminium, 2048)
                .outputItems(TagPrefix.dust, GTMaterials.Titanium, 1024)
                .outputItems(TagPrefix.dust, GTMaterials.Tungsten, 1024)
                .outputItems(TagPrefix.dust, GTMaterials.Silver, 1024)
                .outputItems(TagPrefix.dust, GTMaterials.Gold, 1024)
                .outputItems(TagPrefix.dust, GTMaterials.Lead, 2048)
                .outputItems(TagPrefix.dust, GTMaterials.Platinum, 512)
                .outputItems(TagPrefix.dust, GTMaterials.Uranium238, 512)
                .outputFluids(GTMaterials.Mercury.getFluid(1000000))
                .duration(16000)
                .EUt(1)
                .addData("tier", 9)
                .save(provider);

        GTNARecipeType.COSMOS_SIMULATION_RECIPES.recipeBuilder("stellar_superheavy_synthesis")
                .inputItems(GTNAItems.INFINITY_ANTIMATTER_FUEL_ROD.get())
                .inputFluids(GTMaterials.UUMatter.getFluid(8000))
                .outputItems(TagPrefix.dust, GTMaterials.Naquadah, 2048)
                .outputItems(TagPrefix.dust, GTMaterials.NaquadahEnriched, 1024)
                .outputItems(TagPrefix.dust, GTMaterials.Naquadria, 512)
                .outputItems(TagPrefix.dust, GTMaterials.Neutronium, 256)
                .outputItems(TagPrefix.dust, GTMaterials.Duranium, 1024)
                .outputItems(TagPrefix.dust, GTMaterials.Tritanium, 512)
                .outputItems(TagPrefix.dust, GTMaterials.Rhenium, 2048)
                .outputItems(TagPrefix.dust, GTMaterials.Osmium, 1024)
                .outputItems(TagPrefix.dust, GTMaterials.Iridium, 1024)
                .outputItems(TagPrefix.dust, GTMaterials.Europium, 1024)
                .outputItems(TagPrefix.dust, GTMaterials.Beryllium, 2048)
                .outputItems(TagPrefix.dust, GTMaterials.Hafnium, 1024)
                .outputItems(TagPrefix.dust, GTMaterials.Tantalum, 1024)
                .duration(20000)
                .EUt(1)
                .addData("tier", 10)
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
        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("nexus_flux_matrix")
                .inputItems(ChemicalHelper.get(TagPrefix.plateDense, GTMaterials.Steel).getItem(), 4)
                .inputItems(CustomTags.LV_CIRCUITS, 2)
                .inputItems(ChemicalHelper.get(TagPrefix.plate, GTMaterials.EnderPearl).getItem(), 2)
                .inputItems(ChemicalHelper.get(TagPrefix.frameGt, GTMaterials.Invar).getItem(), 1)
                .outputItems(GTNAEnergyHatches.NEXUS_FLUX_MATRIX.asStack())
                .duration(200)
                .EUt(GTValues.VA[GTValues.LV])
                .save(provider);

        // --- Nexus Capacitors ---
        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("nexus_capacitor_lv")
                .inputItems(ChemicalHelper.get(TagPrefix.foil, GTMaterials.RedAlloy).getItem(), 64)
                .inputItems(CustomTags.LV_CIRCUITS, 1)
                .inputItems(ChemicalHelper.get(TagPrefix.plateDouble, GTMaterials.Steel).getItem(), 4)
                .inputItems(GTItems.FIELD_GENERATOR_LV.asStack().getItem(), 4)
                .inputItems(ChemicalHelper.get(TagPrefix.frameGt, GTMaterials.Steel).getItem(), 1)
                .outputItems(GTNABlocks.NEXUS_CAPACITOR_LV.asStack())
                .duration(400).EUt(30).save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("nexus_capacitor_mv")
                .inputItems(GTNABlocks.NEXUS_CAPACITOR_LV.asStack().getItem(), 1)
                .inputItems(CustomTags.MV_CIRCUITS, 2)
                .inputItems(ChemicalHelper.get(TagPrefix.plateDouble, GTMaterials.Aluminium).getItem(), 4)
                .inputItems(GTItems.FIELD_GENERATOR_MV.asStack().getItem(), 4)
                .inputItems(ChemicalHelper.get(TagPrefix.foil, GTMaterials.Electrum).getItem(), 32)
                .inputFluids(GTMaterials.Nitrogen.getFluid(1000))
                .outputItems(GTNABlocks.NEXUS_CAPACITOR_MV.asStack())
                .duration(400).EUt(120).save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("nexus_capacitor_hv")
                .inputItems(GTNABlocks.NEXUS_CAPACITOR_MV.asStack().getItem(), 1)
                .inputItems(CustomTags.HV_CIRCUITS, 2)
                .inputItems(ChemicalHelper.get(TagPrefix.plateDouble, GTMaterials.StainlessSteel).getItem(), 4)
                .inputItems(GTItems.FIELD_GENERATOR_HV.asStack().getItem(), 4)
                .inputItems(ChemicalHelper.get(TagPrefix.foil, GTMaterials.Platinum).getItem(), 32)
                .inputFluids(GTMaterials.Helium.getFluid(1000))
                .outputItems(GTNABlocks.NEXUS_CAPACITOR_HV.asStack())
                .duration(400).EUt(480).save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("nexus_capacitor_ev")
                .inputItems(GTNABlocks.NEXUS_CAPACITOR_HV.asStack().getItem(), 1)
                .inputItems(CustomTags.EV_CIRCUITS, 2)
                .inputItems(ChemicalHelper.get(TagPrefix.plateDouble, GTMaterials.Titanium).getItem(), 4)
                .inputItems(GTItems.FIELD_GENERATOR_EV.asStack().getItem(), 4)
                .inputItems(ChemicalHelper.get(TagPrefix.plate, GTMaterials.EnderPearl).getItem(), 8)
                .inputFluids(GTMaterials.Radon.getFluid(1000))
                .outputItems(GTNABlocks.NEXUS_CAPACITOR_EV.asStack())
                .duration(400).EUt(1920).save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("nexus_capacitor_iv")
                .inputItems(GTNABlocks.NEXUS_CAPACITOR_EV.asStack().getItem(), 1)
                .inputItems(CustomTags.IV_CIRCUITS, 2)
                .inputItems(ChemicalHelper.get(TagPrefix.plateDouble, GTMaterials.TungstenSteel).getItem(), 4)
                .inputItems(GTItems.FIELD_GENERATOR_IV.asStack().getItem(), 4)
                .inputItems(ChemicalHelper.get(TagPrefix.plate, GTMaterials.EnderPearl).getItem(), 16)
                .inputFluids(GTMaterials.Argon.getFluid(1000))
                .outputItems(GTNABlocks.NEXUS_CAPACITOR_IV.asStack())
                .duration(400).EUt(7680).save(provider);

        // LuV+ capacitors: Assembly Line with Research Station
        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder("nexus_capacitor_luv")
                .inputItems(GTNABlocks.NEXUS_CAPACITOR_IV.asStack().getItem(), 2)
                .inputItems(CustomTags.LuV_CIRCUITS, 4)
                .inputItems(GTItems.FIELD_GENERATOR_LuV.asStack().getItem(), 2)
                .inputItems(ChemicalHelper.get(TagPrefix.plateDouble, GTMaterials.RhodiumPlatedPalladium).getItem(), 8)
                .inputItems(ChemicalHelper.get(TagPrefix.plate, GTMaterials.EnderPearl).getItem(), 32)
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(1152))
                .outputItems(GTNABlocks.NEXUS_CAPACITOR_LUV.asStack())
                .duration(600).EUt(GTValues.VA[GTValues.LuV])
                .stationResearch(b -> b.researchStack(GTNABlocks.NEXUS_CAPACITOR_IV.asStack()).CWUt(64)
                        .EUt(GTValues.VA[GTValues.LuV]))
                .save(provider);

        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder("nexus_capacitor_zpm")
                .inputItems(GTNABlocks.NEXUS_CAPACITOR_LUV.asStack().getItem(), 2)
                .inputItems(CustomTags.ZPM_CIRCUITS, 4)
                .inputItems(GTItems.FIELD_GENERATOR_ZPM.asStack().getItem(), 2)
                .inputItems(ChemicalHelper.get(TagPrefix.plateDouble, GTMaterials.NaquadahAlloy).getItem(), 8)
                .inputItems(ChemicalHelper.get(TagPrefix.plate, GTMaterials.EnderPearl).getItem(), 64)
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(2304))
                .outputItems(GTNABlocks.NEXUS_CAPACITOR_ZPM.asStack())
                .duration(600).EUt(GTValues.VA[GTValues.ZPM])
                .stationResearch(b -> b.researchStack(GTNABlocks.NEXUS_CAPACITOR_LUV.asStack()).CWUt(128)
                        .EUt(GTValues.VA[GTValues.ZPM]))
                .save(provider);

        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder("nexus_capacitor_uv")
                .inputItems(GTNABlocks.NEXUS_CAPACITOR_ZPM.asStack().getItem(), 2)
                .inputItems(CustomTags.UV_CIRCUITS, 4)
                .inputItems(GTItems.FIELD_GENERATOR_UV.asStack().getItem(), 2)
                .inputItems(ChemicalHelper.get(TagPrefix.plateDouble, GTMaterials.Darmstadtium).getItem(), 8)
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(4608))
                .outputItems(GTNABlocks.NEXUS_CAPACITOR_UV.asStack())
                .duration(600).EUt(GTValues.VA[GTValues.UV])
                .stationResearch(b -> b.researchStack(GTNABlocks.NEXUS_CAPACITOR_ZPM.asStack()).CWUt(256)
                        .EUt(GTValues.VA[GTValues.UV]))
                .save(provider);

        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder("nexus_capacitor_uhv")
                .inputItems(GTNABlocks.NEXUS_CAPACITOR_UV.asStack().getItem(), 2)
                .inputItems(CustomTags.UHV_CIRCUITS, 4)
                .inputItems(ChemicalHelper.get(TagPrefix.plateDouble, GTMaterials.Neutronium).getItem(), 8)
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(9216))
                .outputItems(GTNABlocks.NEXUS_CAPACITOR_UHV.asStack())
                .duration(600).EUt(GTValues.VA[GTValues.UHV])
                .stationResearch(b -> b.researchStack(GTNABlocks.NEXUS_CAPACITOR_UV.asStack()).CWUt(512)
                        .EUt(GTValues.VA[GTValues.UHV]))
                .save(provider);

        // --- Wireless Hatches Recipes ---
        // Uses GTMachines arrays directly for reliable recipe generation

        // Helper: sensor items per tier (LV=1 ... UV=8)
        net.minecraft.world.level.ItemLike[] sensors = {
                null, // ULV placeholder
                GTItems.SENSOR_LV.get(),
                GTItems.SENSOR_MV.get(),
                GTItems.SENSOR_HV.get(),
                GTItems.SENSOR_EV.get(),
                GTItems.SENSOR_IV.get(),
                GTItems.SENSOR_LuV.get(),
                GTItems.SENSOR_ZPM.get(),
                GTItems.SENSOR_UV.get()
        };

        for (int tier = GTValues.LV; tier <= GTValues.UHV; tier++) {
            String tierName = GTValues.VN[tier].toLowerCase(java.util.Locale.ROOT);
            int euCost = (int) GTValues.VA[tier];
            net.minecraft.world.level.ItemLike sensor = (tier < sensors.length) ? sensors[tier] :
                    sensors[sensors.length - 1];

            // === 1A Hatches (LV-UHV) — ampExp = 0 ===
            if (tier < GTMachines.ENERGY_INPUT_HATCH.length && GTMachines.ENERGY_INPUT_HATCH[tier] != null &&
                    GTNAEnergyHatches.WIRELESS_ENERGY_HATCHES[tier][0] != null) {
                GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("wireless_energy_in_1a_" + tierName)
                        .inputItems(GTMachines.ENERGY_INPUT_HATCH[tier].asStack(), 2)
                        .inputItems(sensor, 2)
                        .inputItems(ChemicalHelper.get(TagPrefix.plate, GTMaterials.EnderPearl).getItem(), 2)
                        .inputFluids(GTMaterials.SolderingAlloy.getFluid(144))
                        .outputItems(GTNAEnergyHatches.WIRELESS_ENERGY_HATCHES[tier][0].asStack())
                        .duration(200).EUt(euCost)
                        .save(provider);
            }
            if (tier < GTMachines.ENERGY_OUTPUT_HATCH.length && GTMachines.ENERGY_OUTPUT_HATCH[tier] != null &&
                    GTNAEnergyHatches.WIRELESS_DYNAMO_HATCHES[tier][0] != null) {
                GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("wireless_energy_out_1a_" + tierName)
                        .inputItems(GTMachines.ENERGY_OUTPUT_HATCH[tier].asStack(), 2)
                        .inputItems(sensor, 2)
                        .inputItems(ChemicalHelper.get(TagPrefix.plate, GTMaterials.EnderPearl).getItem(), 2)
                        .inputFluids(GTMaterials.SolderingAlloy.getFluid(144))
                        .outputItems(GTNAEnergyHatches.WIRELESS_DYNAMO_HATCHES[tier][0].asStack())
                        .duration(200).EUt(euCost)
                        .save(provider);
            }

            // === 4A Hatches (EV+) — ampExp = 1 ===
            if (tier < GTMachines.ENERGY_INPUT_HATCH_4A.length &&
                    GTMachines.ENERGY_INPUT_HATCH_4A[tier] != null &&
                    GTNAEnergyHatches.WIRELESS_ENERGY_HATCHES[tier][1] != null) {
                GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("wireless_energy_in_4a_" + tierName)
                        .inputItems(GTMachines.ENERGY_INPUT_HATCH_4A[tier].asStack(), 2)
                        .inputItems(sensor, 4)
                        .inputItems(ChemicalHelper.get(TagPrefix.plate, GTMaterials.EnderPearl).getItem(), 2)
                        .inputFluids(GTMaterials.SolderingAlloy.getFluid(288))
                        .outputItems(GTNAEnergyHatches.WIRELESS_ENERGY_HATCHES[tier][1].asStack())
                        .duration(200).EUt(euCost)
                        .save(provider);
            }
            if (tier < GTMachines.ENERGY_OUTPUT_HATCH_4A.length &&
                    GTMachines.ENERGY_OUTPUT_HATCH_4A[tier] != null &&
                    GTNAEnergyHatches.WIRELESS_DYNAMO_HATCHES[tier][1] != null) {
                GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("wireless_energy_out_4a_" + tierName)
                        .inputItems(GTMachines.ENERGY_OUTPUT_HATCH_4A[tier].asStack(), 2)
                        .inputItems(sensor, 4)
                        .inputItems(ChemicalHelper.get(TagPrefix.plate, GTMaterials.EnderPearl).getItem(), 2)
                        .inputFluids(GTMaterials.SolderingAlloy.getFluid(288))
                        .outputItems(GTNAEnergyHatches.WIRELESS_DYNAMO_HATCHES[tier][1].asStack())
                        .duration(200).EUt(euCost)
                        .save(provider);
            }

            // === 16A Hatches (EV+) — ampExp = 2 ===
            if (tier < GTMachines.ENERGY_INPUT_HATCH_16A.length &&
                    GTMachines.ENERGY_INPUT_HATCH_16A[tier] != null &&
                    GTNAEnergyHatches.WIRELESS_ENERGY_HATCHES[tier][2] != null) {
                GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("wireless_energy_in_16a_" + tierName)
                        .inputItems(GTMachines.ENERGY_INPUT_HATCH_16A[tier].asStack(), 2)
                        .inputItems(sensor, 4)
                        .inputItems(ChemicalHelper.get(TagPrefix.plate, GTMaterials.EnderPearl).getItem(), 4)
                        .inputFluids(GTMaterials.SolderingAlloy.getFluid(576))
                        .outputItems(GTNAEnergyHatches.WIRELESS_ENERGY_HATCHES[tier][2].asStack())
                        .duration(200).EUt(euCost)
                        .save(provider);
            }
            if (tier < GTMachines.ENERGY_OUTPUT_HATCH_16A.length &&
                    GTMachines.ENERGY_OUTPUT_HATCH_16A[tier] != null &&
                    GTNAEnergyHatches.WIRELESS_DYNAMO_HATCHES[tier][2] != null) {
                GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("wireless_energy_out_16a_" + tierName)
                        .inputItems(GTMachines.ENERGY_OUTPUT_HATCH_16A[tier].asStack(), 2)
                        .inputItems(sensor, 4)
                        .inputItems(ChemicalHelper.get(TagPrefix.plate, GTMaterials.EnderPearl).getItem(), 4)
                        .inputFluids(GTMaterials.SolderingAlloy.getFluid(576))
                        .outputItems(GTNAEnergyHatches.WIRELESS_DYNAMO_HATCHES[tier][2].asStack())
                        .duration(200).EUt(euCost)
                        .save(provider);
            }

            // === Laser Hatches 256A/1024A/4096A (IV+) — ampExp = 3,4,5 ===
            if (true) {
                // 256A — ampExp = 3
                if (tier < GTMachines.LASER_INPUT_HATCH_256.length && GTMachines.LASER_INPUT_HATCH_256[tier] != null &&
                        GTNAEnergyHatches.WIRELESS_ENERGY_HATCHES[tier][3] != null) {
                    GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("wireless_energy_in_256a_" + tierName)
                            .inputItems(GTMachines.LASER_INPUT_HATCH_256[tier].asStack(), 1)
                            .inputItems(sensor, 4)
                            .inputItems(ChemicalHelper.get(TagPrefix.plate, GTMaterials.EnderPearl).getItem(), 4)
                            .inputFluids(GTMaterials.SolderingAlloy.getFluid(1152))
                            .outputItems(GTNAEnergyHatches.WIRELESS_ENERGY_HATCHES[tier][3].asStack())
                            .duration(400).EUt(euCost)
                            .save(provider);
                }
                if (tier < GTMachines.LASER_OUTPUT_HATCH_256.length &&
                        GTMachines.LASER_OUTPUT_HATCH_256[tier] != null &&
                        GTNAEnergyHatches.WIRELESS_DYNAMO_HATCHES[tier][3] != null) {
                    GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("wireless_energy_out_256a_" + tierName)
                            .inputItems(GTMachines.LASER_OUTPUT_HATCH_256[tier].asStack(), 1)
                            .inputItems(sensor, 4)
                            .inputItems(ChemicalHelper.get(TagPrefix.plate, GTMaterials.EnderPearl).getItem(), 4)
                            .inputFluids(GTMaterials.SolderingAlloy.getFluid(1152))
                            .outputItems(GTNAEnergyHatches.WIRELESS_DYNAMO_HATCHES[tier][3].asStack())
                            .duration(400).EUt(euCost)
                            .save(provider);
                }

                // 1024A — ampExp = 4
                if (tier < GTMachines.LASER_INPUT_HATCH_1024.length &&
                        GTMachines.LASER_INPUT_HATCH_1024[tier] != null &&
                        GTNAEnergyHatches.WIRELESS_ENERGY_HATCHES[tier][4] != null) {
                    GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("wireless_energy_in_1024a_" + tierName)
                            .inputItems(GTMachines.LASER_INPUT_HATCH_1024[tier].asStack(), 1)
                            .inputItems(sensor, 8)
                            .inputItems(ChemicalHelper.get(TagPrefix.plate, GTMaterials.EnderPearl).getItem(), 4)
                            .inputFluids(GTMaterials.SolderingAlloy.getFluid(1152))
                            .outputItems(GTNAEnergyHatches.WIRELESS_ENERGY_HATCHES[tier][4].asStack())
                            .duration(400).EUt(euCost)
                            .save(provider);
                }
                if (tier < GTMachines.LASER_OUTPUT_HATCH_1024.length &&
                        GTMachines.LASER_OUTPUT_HATCH_1024[tier] != null &&
                        GTNAEnergyHatches.WIRELESS_DYNAMO_HATCHES[tier][4] != null) {
                    GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("wireless_energy_out_1024a_" + tierName)
                            .inputItems(GTMachines.LASER_OUTPUT_HATCH_1024[tier].asStack(), 1)
                            .inputItems(sensor, 8)
                            .inputItems(ChemicalHelper.get(TagPrefix.plate, GTMaterials.EnderPearl).getItem(), 4)
                            .inputFluids(GTMaterials.SolderingAlloy.getFluid(1152))
                            .outputItems(GTNAEnergyHatches.WIRELESS_DYNAMO_HATCHES[tier][4].asStack())
                            .duration(400).EUt(euCost)
                            .save(provider);
                }

                // 4096A — ampExp = 5
                if (tier < GTMachines.LASER_INPUT_HATCH_4096.length &&
                        GTMachines.LASER_INPUT_HATCH_4096[tier] != null &&
                        GTNAEnergyHatches.WIRELESS_ENERGY_HATCHES[tier][5] != null) {
                    GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("wireless_energy_in_4096a_" + tierName)
                            .inputItems(GTMachines.LASER_INPUT_HATCH_4096[tier].asStack(), 1)
                            .inputItems(sensor, 16)
                            .inputItems(ChemicalHelper.get(TagPrefix.plate, GTMaterials.EnderPearl).getItem(), 4)
                            .inputFluids(GTMaterials.SolderingAlloy.getFluid(1152))
                            .outputItems(GTNAEnergyHatches.WIRELESS_ENERGY_HATCHES[tier][5].asStack())
                            .duration(400).EUt(euCost)
                            .save(provider);
                }
                if (tier < GTMachines.LASER_OUTPUT_HATCH_4096.length &&
                        GTMachines.LASER_OUTPUT_HATCH_4096[tier] != null &&
                        GTNAEnergyHatches.WIRELESS_DYNAMO_HATCHES[tier][5] != null) {
                    GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("wireless_energy_out_4096a_" + tierName)
                            .inputItems(GTMachines.LASER_OUTPUT_HATCH_4096[tier].asStack(), 1)
                            .inputItems(sensor, 16)
                            .inputItems(ChemicalHelper.get(TagPrefix.plate, GTMaterials.EnderPearl).getItem(), 4)
                            .inputFluids(GTMaterials.SolderingAlloy.getFluid(1152))
                            .outputItems(GTNAEnergyHatches.WIRELESS_DYNAMO_HATCHES[tier][5].asStack())
                            .duration(400).EUt(euCost)
                            .save(provider);
                }
            }
        }
    }
}
