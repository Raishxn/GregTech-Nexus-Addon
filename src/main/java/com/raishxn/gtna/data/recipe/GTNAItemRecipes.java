package com.raishxn.gtna.data.recipe;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.machines.GTAEMachines;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.api.data.tag.GTNATagPrefix;
import com.raishxn.gtna.common.data.GTNABlocks;
import com.raishxn.gtna.common.data.GTNAItems;
import com.raishxn.gtna.common.data.GTNAMachines2;
import com.raishxn.gtna.common.data.GTNAMaterials;
import com.raishxn.gtna.common.data.GTNARecipeType;
import com.raishxn.gtna.common.data.condition.RestrictedItemsEnabledCondition;
import appeng.core.definitions.AEItems;

import java.util.function.Consumer;

import static com.ibm.icu.impl.CurrencyData.provider;

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
                .unlockedBy("has_bronze_ingot",
                        InventoryChangeTrigger.TriggerInstance
                                .hasItems(ChemicalHelper.get(TagPrefix.ingot, GTMaterials.Bronze).getItem()))
                .save(provider);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, GTNAItems.STRUCTURE_DETECT.get())
                .requires(Items.BOOK, 8)
                .requires(GTItems.TERMINAL.get())
                .unlockedBy("has_books", InventoryChangeTrigger.TriggerInstance.hasItems(Items.BOOK))
                .save(provider);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNAItems.TESSERACT_TARGET_MARKER.get())
                .pattern("ABC")
                .pattern(" DE")
                .pattern(" FE")
                .define('A', GTItems.SENSOR_LV.get())
                .define('B', GTItems.COVER_SCREEN.get())
                .define('C', ChemicalHelper.get(TagPrefix.frameGt, GTNAMaterials.Stronze).getItem())
                .define('D', GTNAItems.COORDINATE_CARD.get())
                .define('E', ChemicalHelper.get(TagPrefix.rod, GTNAMaterials.Stronze).getItem())
                .define('F', CustomTags.MV_CIRCUITS)
                .unlockedBy("has_coordinate_card",
                        InventoryChangeTrigger.TriggerInstance.hasItems(GTNAItems.COORDINATE_CARD.get()))
                .save(provider);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNAItems.HYDRAULIC_MOTOR.get())
                .pattern("ABC")
                .pattern("BDB")
                .pattern("CBA")
                .define('A', ChemicalHelper.get(TagPrefix.gear, GTMaterials.Bronze).getItem())
                .define('B', ChemicalHelper.get(TagPrefix.pipeTinyFluid, GTMaterials.Bronze).getItem())
                .define('C', ChemicalHelper.get(TagPrefix.rod, GTMaterials.Iron).getItem())
                .define('D', ChemicalHelper.get(TagPrefix.rotor, GTMaterials.Iron).getItem())
                .unlockedBy("has_bronze_gear",
                        InventoryChangeTrigger.TriggerInstance
                                .hasItems(ChemicalHelper.get(TagPrefix.gear, GTMaterials.Bronze).getItem()))
                .save(provider);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNAItems.HYDRAULIC_CONVEYOR.get())
                .pattern("AAA")
                .pattern("BCB")
                .pattern("AAA")
                .define('A', ChemicalHelper.get(TagPrefix.plate, GTMaterials.Rubber).getItem())
                .define('B', GTNAItems.HYDRAULIC_MOTOR.get())
                .define('C', ChemicalHelper.get(TagPrefix.gear, GTMaterials.Clay).getItem())
                .unlockedBy("has_hydraulic_motor",
                        InventoryChangeTrigger.TriggerInstance.hasItems(GTNAItems.HYDRAULIC_MOTOR.get()))
                .save(provider);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNAItems.HYDRAULIC_CONVEYOR.get())
                .pattern("AAA")
                .pattern("BCB")
                .pattern("AAA")
                .define('A', ChemicalHelper.get(TagPrefix.plate, GTMaterials.Rubber).getItem())
                .define('B', GTNAItems.HYDRAULIC_MOTOR.get())
                .define('C', ChemicalHelper.get(TagPrefix.gear, GTNAMaterials.ClayCompound).getItem())
                .unlockedBy("has_exquisite_salt",
                        InventoryChangeTrigger.TriggerInstance
                                .hasItems(ChemicalHelper.get(TagPrefix.gemExquisite, GTMaterials.Salt).getItem()))
                .save(provider);
        GTNARecipeType.HYDRAULIC_MANUFACTURING.recipeBuilder("hydraulic_pump")
                .inputItems(GTNAItems.HYDRAULIC_MOTOR.get(), 2)
                .inputItems(ChemicalHelper.get(TagPrefix.ring, GTMaterials.Rubber), 2)
                .inputItems(ChemicalHelper.get(TagPrefix.bolt, GTNAMaterials.ClayCompound), 1)
                .inputItems(ChemicalHelper.get(TagPrefix.pipeLargeFluid, GTMaterials.Bronze), 1)
                .inputItems(ChemicalHelper.get(TagPrefix.rotor, GTMaterials.Bronze), 1)
                .outputItems(GTNAItems.HYDRAULIC_PUMP.get())
                .duration(120)
                .EUt(32)
                .save(provider);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNAItems.HYDRAULIC_PISTON.get())
                .pattern("AAA")
                .pattern("BCC")
                .pattern("DEF")
                .define('A', ChemicalHelper.get(TagPrefix.plate, GTNAMaterials.ClayCompound).getItem())
                .define('B', ChemicalHelper.get(TagPrefix.rotor, GTMaterials.Iron).getItem())
                .define('C', ChemicalHelper.get(TagPrefix.rod, GTMaterials.Iron).getItem())
                .define('D', ChemicalHelper.get(TagPrefix.pipeTinyFluid, GTMaterials.Bronze).getItem())
                .define('E', GTNAItems.HYDRAULIC_MOTOR.get())
                .define('F', ChemicalHelper.get(TagPrefix.gear, GTMaterials.Bronze).getItem())
                .unlockedBy("has_hydraulic_motor",
                        InventoryChangeTrigger.TriggerInstance.hasItems(GTNAItems.HYDRAULIC_MOTOR.get()))
                .save(provider);
        GTNARecipeType.HYDRAULIC_MANUFACTURING.recipeBuilder("hydraulic_piston")
                .inputItems(ChemicalHelper.get(TagPrefix.plate, GTNAMaterials.ClayCompound), 3)
                .inputItems(ChemicalHelper.get(TagPrefix.rotor, GTMaterials.Iron), 1)
                .inputItems(ChemicalHelper.get(TagPrefix.rod, GTMaterials.Iron), 2)
                .inputItems(ChemicalHelper.get(TagPrefix.pipeTinyFluid, GTMaterials.Bronze), 1)
                .inputItems(GTNAItems.HYDRAULIC_MOTOR.get(), 1)
                .inputItems(ChemicalHelper.get(TagPrefix.gear, GTMaterials.Bronze), 1)
                .outputItems(GTNAItems.HYDRAULIC_PISTON.get())
                .duration(20)
                .EUt(16)
                .save(provider);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNAItems.HYDRAULIC_ARM.get())
                .pattern("AAA")
                .pattern("BCB")
                .pattern("DEC")
                .define('A', ChemicalHelper.get(TagPrefix.plate, GTNAMaterials.ClayCompound).getItem())
                .define('B', GTNAItems.HYDRAULIC_MOTOR.get()) // Assumindo "hydraulic" como Motor
                .define('C', ChemicalHelper.get(TagPrefix.rod, GTMaterials.Iron).getItem())
                .define('D', GTNAItems.HYDRAULIC_PISTON.get())
                .define('E', ChemicalHelper.get(TagPrefix.gear, GTNAMaterials.ClayCompound).getItem())
                .unlockedBy("has_hydraulic_piston",
                        InventoryChangeTrigger.TriggerInstance.hasItems(GTNAItems.HYDRAULIC_PISTON.get()))
                .save(provider);
        GTNARecipeType.HYDRAULIC_MANUFACTURING.recipeBuilder("hydraulic_arm")
                .inputItems(ChemicalHelper.get(TagPrefix.plate, GTNAMaterials.ClayCompound), 3)
                .inputItems(GTNAItems.HYDRAULIC_MOTOR.get(), 2)
                .inputItems(ChemicalHelper.get(TagPrefix.rod, GTMaterials.Iron), 2)
                .inputItems(GTNAItems.HYDRAULIC_PISTON.get(), 1)
                .inputItems(ChemicalHelper.get(TagPrefix.gear, GTNAMaterials.ClayCompound), 1)
                .outputItems(GTNAItems.HYDRAULIC_ARM.get())
                .duration(20)
                .EUt(16)
                .save(provider);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNAItems.HYDRAULIC_STEAM_RECEIVER.get())
                .pattern("ABC")
                .pattern("ACB")
                .pattern("DAA")
                .define('A', ChemicalHelper.get(TagPrefix.plate, GTNAMaterials.CompressedSteam).getItem())
                .define('B', ChemicalHelper.get(TagPrefix.plate, GTMaterials.Rubber).getItem())
                .define('C', ChemicalHelper.get(TagPrefix.pipeHugeFluid, GTNAMaterials.Stronze).getItem())
                .define('D', GTNAItems.HYDRAULIC_PUMP.get())
                .unlockedBy("has_hydraulic_pump",
                        InventoryChangeTrigger.TriggerInstance.hasItems(GTNAItems.HYDRAULIC_PUMP.get()))
                .save(provider);
        GTNARecipeType.HYDRAULIC_MANUFACTURING.recipeBuilder("hydraulic_steam_receiver")
                .inputItems(ChemicalHelper.get(TagPrefix.plate, GTNAMaterials.CompressedSteam), 4)
                .inputItems(ChemicalHelper.get(TagPrefix.plate, GTMaterials.Rubber), 2)
                .inputItems(ChemicalHelper.get(TagPrefix.pipeHugeFluid, GTNAMaterials.Stronze), 2)
                .inputItems(GTNAItems.HYDRAULIC_PUMP.get(), 1)
                .outputItems(GTNAItems.HYDRAULIC_STEAM_RECEIVER.get())
                .duration(20)
                .EUt(16)
                .save(provider);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNAItems.HYDRAULIC_STEAM_JET_SPEWER.get())
                .pattern("ABC")
                .pattern("BDB")
                .pattern("CBA")
                .define('A', ChemicalHelper.get(TagPrefix.plate, GTNAMaterials.CompressedSteam).getItem())
                .define('B', ChemicalHelper.get(TagPrefix.rod, GTNAMaterials.CompressedSteam).getItem())
                .define('C', ChemicalHelper.get(TagPrefix.pipeHugeFluid, GTNAMaterials.Breel).getItem())
                .define('D', ChemicalHelper.get(TagPrefix.gemExquisite, GTMaterials.Salt).getItem())
                .unlockedBy("has_exquisite_salt",
                        InventoryChangeTrigger.TriggerInstance
                                .hasItems(ChemicalHelper.get(TagPrefix.gemExquisite, GTMaterials.Salt).getItem()))
                .save(provider);
        GTNARecipeType.HYDRAULIC_MANUFACTURING.recipeBuilder("hydraulic_steam_jet_spewer")
                .inputItems(ChemicalHelper.get(TagPrefix.plate, GTNAMaterials.CompressedSteam), 2)
                .inputItems(ChemicalHelper.get(TagPrefix.rod, GTNAMaterials.CompressedSteam), 2)
                .inputItems(ChemicalHelper.get(TagPrefix.pipeHugeFluid, GTNAMaterials.Breel), 2)
                .inputItems(ChemicalHelper.get(TagPrefix.gemExquisite, GTMaterials.Salt), 1)
                .outputItems(GTNAItems.HYDRAULIC_STEAM_JET_SPEWER.get())
                .duration(20)
                .EUt(16)
                .save(provider);
        GTNARecipeType.HYDRAULIC_MANUFACTURING.recipeBuilder("hydraulic_conveyor")
                .inputItems(ChemicalHelper.get(TagPrefix.plate, GTMaterials.Rubber), 6)
                .inputItems(GTNAItems.HYDRAULIC_MOTOR.get(), 2)
                .inputItems(ChemicalHelper.get(TagPrefix.gear, GTNAMaterials.ClayCompound), 1)
                .outputItems(GTNAItems.HYDRAULIC_CONVEYOR.get())
                .duration(20)
                .EUt(16)
                .save(provider);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNAItems.HYDRAULIC_VAPOR_GENERATOR.get())
                .pattern("ABC")
                .pattern("BDB")
                .pattern("CBA")
                .define('A', ChemicalHelper.get(GTNATagPrefix.superdensePlate, GTNAMaterials.CompressedSteam).getItem())
                .define('B', ChemicalHelper.get(TagPrefix.plateDouble, GTNAMaterials.CompressedSteam).getItem())
                .define('C', GTNAItems.HYDRAULIC_STEAM_JET_SPEWER.get())
                .define('D', ChemicalHelper.get(TagPrefix.rotor, GTNAMaterials.CompressedSteam).getItem())
                .unlockedBy("has_steam_jet_spewer",
                        InventoryChangeTrigger.TriggerInstance.hasItems(GTNAItems.HYDRAULIC_STEAM_JET_SPEWER.get()))
                .save(provider);
        GTNARecipeType.HYDRAULIC_MANUFACTURING.recipeBuilder("hydraulic_vapor_generator")
                .inputItems(ChemicalHelper.get(GTNATagPrefix.superdensePlate, GTNAMaterials.CompressedSteam), 2)
                .inputItems(ChemicalHelper.get(TagPrefix.plateDouble, GTNAMaterials.CompressedSteam), 4)
                .inputItems(GTNAItems.HYDRAULIC_STEAM_JET_SPEWER.get(), 2)
                .inputItems(ChemicalHelper.get(TagPrefix.rotor, GTNAMaterials.CompressedSteam), 1)
                .outputItems(GTNAItems.HYDRAULIC_VAPOR_GENERATOR.get())
                .duration(20)
                .EUt(16)
                .save(provider);
        GTNARecipeType.HYDRAULIC_MANUFACTURING.recipeBuilder("hydraulic_motor_manufacturing")
                .inputItems(ChemicalHelper.get(TagPrefix.rotor, GTMaterials.Steel), 1)
                .inputItems(ChemicalHelper.get(TagPrefix.pipeSmallFluid, GTMaterials.Bronze), 2)
                .inputItems(ChemicalHelper.get(TagPrefix.plate, GTMaterials.Iron), 2)
                .inputItems(ChemicalHelper.get(TagPrefix.screw, GTMaterials.Steel), 4)
                .outputItems(GTNAItems.HYDRAULIC_MOTOR.get())
                .duration(200)
                .EUt(16)
                .save(provider);

        // --- Hydraulic Pump (Shaped Recipe) ---
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNAItems.HYDRAULIC_PUMP.get())
                .pattern("SPS")
                .pattern("PRP")
                .pattern("SPS")
                .define('S', ChemicalHelper.get(TagPrefix.screw, GTMaterials.Steel).getItem())
                .define('P', ChemicalHelper.get(TagPrefix.pipeSmallFluid, GTMaterials.Bronze).getItem())
                .define('R', ChemicalHelper.get(TagPrefix.rotor, GTMaterials.Steel).getItem())
                .unlockedBy("has_rotor",
                        InventoryChangeTrigger.TriggerInstance
                                .hasItems(ChemicalHelper.get(TagPrefix.rotor, GTMaterials.Steel).getItem()))
                .save(provider);

        // --- Hydraulic Regulator (Shaped & Manufacturer) ---
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNAItems.HYDRAULIC_REGULATOR.get())
                .pattern("FPF")
                .pattern("SGS")
                .pattern("FPF")
                .define('F', ChemicalHelper.get(TagPrefix.plate, GTMaterials.Iron).getItem())
                .define('P', ChemicalHelper.get(TagPrefix.pipeNormalFluid, GTMaterials.Copper).getItem())
                .define('S', ChemicalHelper.get(TagPrefix.springSmall, GTMaterials.Steel).getItem())
                .define('G', ChemicalHelper.get(TagPrefix.gearSmall, GTMaterials.Bronze).getItem())
                .unlockedBy("has_spring",
                        InventoryChangeTrigger.TriggerInstance
                                .hasItems(ChemicalHelper.get(TagPrefix.springSmall, GTMaterials.Steel).getItem()))
                .save(provider);

        GTNARecipeType.HYDRAULIC_MANUFACTURING.recipeBuilder("hydraulic_regulator")
                .inputItems(ChemicalHelper.get(TagPrefix.plate, GTMaterials.Iron), 4)
                .inputItems(ChemicalHelper.get(TagPrefix.springSmall, GTMaterials.Steel), 2)
                .inputItems(ChemicalHelper.get(TagPrefix.gearSmall, GTMaterials.Bronze), 1)
                .inputItems(ChemicalHelper.get(TagPrefix.pipeNormalFluid, GTMaterials.Copper), 2)
                .outputItems(GTNAItems.HYDRAULIC_REGULATOR.get())
                .duration(160)
                .EUt(16)
                .save(provider);
        GTRecipeTypes.COMPRESSOR_RECIPES.recipeBuilder("gtna_standard_industrial_components_small")
                .inputItems(AEItems.MATTER_BALL.asItem(), 64)
                .outputItems(GTNAItems.INDUSTRIAL_COMPONENTS[0][0].get())
                .duration(20)
                .EUt(30)
                .save(provider);
        GTRecipeTypes.COMPRESSOR_RECIPES.recipeBuilder("gtna_standard_industrial_components_medium")
                .inputItems(GTNAItems.INDUSTRIAL_COMPONENTS[0][0].get(), 5)
                .outputItems(GTNAItems.INDUSTRIAL_COMPONENTS[0][1].get())
                .duration(20)
                .EUt(30)
                .save(provider);
        GTRecipeTypes.COMPRESSOR_RECIPES.recipeBuilder("gtna_standard_industrial_components_large")
                .inputItems(GTNAItems.INDUSTRIAL_COMPONENTS[0][1].get(), 5)
                .outputItems(GTNAItems.INDUSTRIAL_COMPONENTS[0][2].get())
                .duration(20)
                .EUt(30)
                .save(provider);
        GTRecipeTypes.COMPRESSOR_RECIPES.recipeBuilder("gtna_extended_industrial_components_small")
                .inputItems(AEItems.SINGULARITY.asItem(), 64)
                .outputItems(GTNAItems.INDUSTRIAL_COMPONENTS[1][0].get())
                .duration(20)
                .EUt(480)
                .save(provider);
        GTRecipeTypes.COMPRESSOR_RECIPES.recipeBuilder("gtna_extended_industrial_components_medium")
                .inputItems(GTNAItems.INDUSTRIAL_COMPONENTS[1][0].get(), 5)
                .outputItems(GTNAItems.INDUSTRIAL_COMPONENTS[1][1].get())
                .duration(20)
                .EUt(480)
                .save(provider);
        GTRecipeTypes.COMPRESSOR_RECIPES.recipeBuilder("gtna_extended_industrial_components_large")
                .inputItems(GTNAItems.INDUSTRIAL_COMPONENTS[1][1].get(), 5)
                .outputItems(GTNAItems.INDUSTRIAL_COMPONENTS[1][2].get())
                .duration(20)
                .EUt(480)
                .save(provider);
        GTRecipeTypes.COMPRESSOR_RECIPES.recipeBuilder("gtna_special_industrial_components_small")
                .inputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Neutronium).getItem(), 64)
                .outputItems(GTNAItems.INDUSTRIAL_COMPONENTS[2][0].get())
                .duration(20)
                .EUt(7680)
                .save(provider);
        GTRecipeTypes.COMPRESSOR_RECIPES.recipeBuilder("gtna_special_industrial_components_medium")
                .inputItems(GTNAItems.INDUSTRIAL_COMPONENTS[2][0].get(), 5)
                .outputItems(GTNAItems.INDUSTRIAL_COMPONENTS[2][1].get())
                .duration(20)
                .EUt(7680)
                .save(provider);
        GTRecipeTypes.COMPRESSOR_RECIPES.recipeBuilder("gtna_special_industrial_components_large")
                .inputItems(GTNAItems.INDUSTRIAL_COMPONENTS[2][1].get(), 5)
                .outputItems(GTNAItems.INDUSTRIAL_COMPONENTS[2][2].get())
                .duration(20)
                .EUt(7680)
                .save(provider);
        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("vajra_assembly")
                .inputItems(ChemicalHelper.get(TagPrefix.plateDouble, GTNAMaterials.Echoite), 2)
                .inputItems(GTNAItems.HYDRAULIC_STEAM_JET_SPEWER.get(), 1)
                .inputItems(GTNAItems.HYDRAULIC_MOTOR.get(), 1)
                .inputItems(GTItems.INTEGRATED_CIRCUIT_MV.get(), 2)
                .inputItems(GTItems.BATTERY_MV_LITHIUM.get(), 2)
                .inputItems(GTItems.EMITTER_MV.get(), 2)
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(144))
                .outputItems(BuiltInRegistries.ITEM.get(new ResourceLocation("gtna", "echoite_vajra")))
                .duration(600)
                .EUt(120)
                .save(provider);

        if (GTNAMachines2.ME_MINI_PATTERN_BUFFER != null) {
            GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("gtna_me_mini_pattern_buffer")
                    .inputItems(GTAEMachines.ME_PATTERN_BUFFER.asStack())
                    .inputItems(GTItems.TERMINAL.get())
                    .inputItems(GTItems.TOOL_DATA_STICK.get())
                    .inputItems(GTItems.INTEGRATED_CIRCUIT_HV.get(), 2)
                    .inputFluids(GTMaterials.SolderingAlloy.getFluid(288))
                    .outputItems(GTNAMachines2.ME_MINI_PATTERN_BUFFER.asStack())
                    .duration(400)
                    .EUt(480)
                    .save(provider);
        }

        if (GTNAMachines2.ME_PATTERN_BUFFER != null && GTNAMachines2.ME_CRAFT_PATTERN_HATCH != null) {
            GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("gtna_me_craft_pattern_hatch")
                    .inputItems(GTNAMachines2.ME_PATTERN_BUFFER.asStack())
                    .inputItems(GTItems.ROBOT_ARM_ZPM.get())
                    .inputItems(GTItems.SENSOR_ZPM.get())
                    .inputItems(GTItems.TOOL_DATA_ORB.get())
                    .inputItems("expatternprovider:assembler_matrix_crafter", 2)
                    .inputItems("expatternprovider:assembler_matrix_speed", 2)
                    .inputFluids(GTMaterials.SolderingAlloy.getFluid(576))
                    .outputItems(GTNAMachines2.ME_CRAFT_PATTERN_HATCH.asStack())
                    .duration(400)
                    .EUt(1920)
                    .save(provider);
        }

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("pattern_buffer_upgrade_21")
                .inputItems(GTItems.SENSOR_LuV.get())
                .inputItems(GTItems.ROBOT_ARM_LuV.get())
                .inputItems(GTItems.CONVEYOR_MODULE_LuV.get())
                .inputItems(GTItems.ELECTRIC_PUMP_LuV.get())
                .inputItems(GTItems.TOOL_DATA_STICK.get())
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(144))
                .outputItems(GTNAItems.PATTERN_BUFFER_UPGRADE_21.get())
                .duration(200)
                .EUt(480)
                .save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("pattern_buffer_upgrade_32")
                .inputItems(GTItems.SENSOR_ZPM.get())
                .inputItems(GTItems.ROBOT_ARM_ZPM.get())
                .inputItems(GTItems.CONVEYOR_MODULE_ZPM.get())
                .inputItems(GTItems.ELECTRIC_PUMP_ZPM.get())
                .inputItems(GTItems.TOOL_DATA_ORB.get())
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(288))
                .outputItems(GTNAItems.PATTERN_BUFFER_UPGRADE_32.get())
                .duration(300)
                .EUt(1920)
                .save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("pattern_buffer_upgrade_72")
                .inputItems(GTItems.SENSOR_UV.get())
                .inputItems(GTItems.ROBOT_ARM_UV.get())
                .inputItems(GTItems.CONVEYOR_MODULE_UV.get())
                .inputItems(GTItems.ELECTRIC_PUMP_UV.get())
                .inputItems(GTItems.COVER_WIRELESS_TRANSMITTER.get(), 2)
                .inputItems(GTItems.TOOL_DATA_ORB.get())
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(576))
                .outputItems(GTNAItems.PATTERN_BUFFER_UPGRADE_72.get())
                .duration(400)
                .EUt(7680)
                .save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("gtna_annihilation_constrainer")
                .inputItems(GTItems.GRAVI_STAR.get())
                .inputItems(GTItems.FIELD_GENERATOR_UHV.get(), 2)
                .inputItems(GTItems.EMITTER_UHV.get(), 2)
                .inputItems(GTItems.SENSOR_UHV.get(), 2)
                .inputItems(TagPrefix.plateDouble, GTMaterials.Neutronium, 4)
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(576))
                .outputItems(GTNAItems.ANNIHILATION_CONSTRAINER.get())
                .duration(400)
                .EUt(1966080)
                .save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("gtna_neutronium_antimatter_fuel_rod")
                .inputItems(GTNAItems.ANNIHILATION_CONSTRAINER.get())
                .inputItems(TagPrefix.rodLong, GTMaterials.Neutronium, 2)
                .inputItems(GTNABlocks.ANTIMATTER_CONTAINMENT_CASING.get())
                .inputItems(GTItems.QUANTUM_STAR.get())
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(288))
                .outputItems(GTNAItems.NEUTRONIUM_ANTIMATTER_FUEL_ROD.get())
                .duration(300)
                .EUt(491520)
                .save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("gtna_draconium_antimatter_fuel_rod")
                .inputItems(GTNAItems.NEUTRONIUM_ANTIMATTER_FUEL_ROD.get())
                .inputItems(GTItems.FIELD_GENERATOR_UHV.get(), 2)
                .inputItems(TagPrefix.plateDouble, GTMaterials.Naquadria, 4)
                .inputItems(CustomTags.UEV_CIRCUITS, 2)
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(288))
                .outputItems(GTNAItems.DRACONIUM_ANTIMATTER_FUEL_ROD.get())
                .duration(400)
                .EUt(1966080)
                .save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("gtna_cosmic_neutronium_antimatter_fuel_rod")
                .inputItems(GTNAItems.DRACONIUM_ANTIMATTER_FUEL_ROD.get())
                .inputItems(GTItems.FIELD_GENERATOR_UEV.get(), 2)
                .inputItems(GTItems.GRAVI_STAR.get(), 2)
                .inputItems(TagPrefix.plateDouble, GTMaterials.Neutronium, 8)
                .inputItems(CustomTags.UIV_CIRCUITS, 2)
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(576))
                .outputItems(GTNAItems.COSMIC_NEUTRONIUM_ANTIMATTER_FUEL_ROD.get())
                .duration(500)
                .EUt(7864320)
                .save(provider);

        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder("gtna_infinity_antimatter_fuel_rod")
                .inputItems(GTNAItems.COSMIC_NEUTRONIUM_ANTIMATTER_FUEL_ROD.get())
                .inputItems(GTNAItems.ANNIHILATION_CONSTRAINER.get(), 2)
                .inputItems(GTItems.FIELD_GENERATOR_UIV.get(), 2)
                .inputItems(GTItems.EMITTER_UIV.get(), 2)
                .inputItems(GTItems.SENSOR_UIV.get(), 2)
                .inputItems(TagPrefix.plateDouble, GTMaterials.Neutronium, 8)
                .inputItems(CustomTags.UXV_CIRCUITS, 2)
                .inputFluids(GTMaterials.Europium.getFluid(1152))
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(1152))
                .outputItems(GTNAItems.INFINITY_ANTIMATTER_FUEL_ROD.get())
                .duration(600)
                .EUt(31457280)
                .stationResearch(b -> b
                        .researchStack(GTNAItems.COSMIC_NEUTRONIUM_ANTIMATTER_FUEL_ROD.asStack())
                        .CWUt(256)
                        .EUt(7864320))
                .save(provider);

        GTNARecipeVisibility.saveRestricted(provider, GTNACORE.id("infinite_steam_singleblock_cover"),
                restrictedProvider -> ShapedRecipeBuilder.shaped(RecipeCategory.MISC,
                        GTNAItems.INFINITE_STEAM_SINGLEBLOCK_COVER.get())
                        .pattern("ABA")
                        .pattern("CDC")
                        .pattern("AEA")
                        .define('A', ChemicalHelper.get(TagPrefix.plate, GTNAMaterials.CompressedSteam).getItem())
                        .define('B', GTNAItems.HYDRAULIC_STEAM_RECEIVER.get())
                        .define('C', GTNAItems.PRECISION_STEAM_COMPONENT.get())
                        .define('D', GTNAItems.HYDRAULIC_PUMP.get())
                        .define('E', GTNAItems.HYDRAULIC_VAPOR_GENERATOR.get())
                        .unlockedBy("has_hydraulic_vapor_generator", InventoryChangeTrigger.TriggerInstance
                                .hasItems(GTNAItems.HYDRAULIC_VAPOR_GENERATOR.get()))
                        .save(restrictedProvider));

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("gtna_infinite_steam_singleblock_cover")
                .addCondition(RestrictedItemsEnabledCondition.INSTANCE)
                .inputItems(GTNAItems.HYDRAULIC_STEAM_RECEIVER.get(), 2)
                .inputItems(GTNAItems.HYDRAULIC_VAPOR_GENERATOR.get(), 2)
                .inputItems(GTNAItems.PRECISION_STEAM_COMPONENT.get(), 4)
                .inputItems(GTNAItems.HYDRAULIC_PUMP.get(), 2)
                .inputItems(TagPrefix.plateDouble, GTNAMaterials.CompressedSteam, 4)
                .inputFluids(GTNAMaterials.CompressedSteam.getFluid(576))
                .outputItems(GTNAItems.INFINITE_STEAM_SINGLEBLOCK_COVER.get())
                .duration(200)
                .EUt(120)
                .save(provider);

        GTNARecipeVisibility.saveRestricted(provider, GTNACORE.id("infinite_electric_singleblock_cover"),
                restrictedProvider -> ShapedRecipeBuilder.shaped(RecipeCategory.MISC,
                        GTNAItems.INFINITE_ELECTRIC_SINGLEBLOCK_COVER.get())
                        .pattern("ABA")
                        .pattern("CDC")
                        .pattern("AEA")
                        .define('A', ChemicalHelper.get(TagPrefix.cableGtSingle, GTMaterials.Tin).getItem())
                        .define('B', GTItems.ELECTRIC_PUMP_LV.get())
                        .define('C', GTItems.CONVEYOR_MODULE_LV.get())
                        .define('D', GTItems.ROBOT_ARM_LV.get())
                        .define('E', GTItems.SENSOR_LV.get())
                        .unlockedBy("has_electric_pump_lv",
                                InventoryChangeTrigger.TriggerInstance.hasItems(GTItems.ELECTRIC_PUMP_LV.get()))
                        .save(restrictedProvider));

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("gtna_infinite_electric_singleblock_cover")
                .addCondition(RestrictedItemsEnabledCondition.INSTANCE)
                .inputItems(GTItems.ELECTRIC_PUMP_LV.get(), 1)
                .inputItems(GTItems.CONVEYOR_MODULE_LV.get(), 1)
                .inputItems(GTItems.ROBOT_ARM_LV.get(), 1)
                .inputItems(GTItems.SENSOR_LV.get(), 1)
                .inputItems(TagPrefix.cableGtSingle, GTMaterials.Tin, 4)
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(144))
                .outputItems(GTNAItems.INFINITE_ELECTRIC_SINGLEBLOCK_COVER.get())
                .duration(100)
                .EUt(30)
                .save(provider);
    }
}
