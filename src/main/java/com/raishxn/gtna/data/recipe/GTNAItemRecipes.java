package com.raishxn.gtna.data.recipe;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.raishxn.gtna.api.data.tag.GTNATagPrefix;
import com.raishxn.gtna.api.item.tool.GTNAToolType;
import com.raishxn.gtna.common.data.GTNAItems;
import com.raishxn.gtna.common.data.GTNAMaterials;
import com.raishxn.gtna.common.data.GTNARecipeType;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

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
                .unlockedBy("has_bronze_ingot", InventoryChangeTrigger.TriggerInstance.hasItems(ChemicalHelper.get(TagPrefix.ingot, GTMaterials.Bronze).getItem()))
                .save(provider);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, GTNAItems.STRUCTURE_DETECT.get())
                .requires(Items.BOOK, 8)
                .requires(GTItems.TERMINAL.get())
                .unlockedBy("has_books", InventoryChangeTrigger.TriggerInstance.hasItems(Items.BOOK))
                .save(provider);
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
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNAItems.HYDRAULIC_CONVEYOR.get())
                .pattern("AAA")
                .pattern("BCB")
                .pattern("AAA")
                .define('A', ChemicalHelper.get(TagPrefix.plate, GTMaterials.Rubber).getItem())
                .define('B', GTNAItems.HYDRAULIC_MOTOR.get())
                .define('C', ChemicalHelper.get(TagPrefix.gear, GTMaterials.Clay).getItem())
                .unlockedBy("has_hydraulic_motor", InventoryChangeTrigger.TriggerInstance.hasItems(GTNAItems.HYDRAULIC_MOTOR.get()))
                .save(provider);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNAItems.HYDRAULIC_CONVEYOR.get())
                .pattern("AAA")
                .pattern("BCB")
                .pattern("AAA")
                .define('A', ChemicalHelper.get(TagPrefix.plate, GTMaterials.Rubber).getItem())
                .define('B', GTNAItems.HYDRAULIC_MOTOR.get())
                .define('C', ChemicalHelper.get(TagPrefix.gear, GTNAMaterials.ClayCompound).getItem())
                .unlockedBy("has_exquisite_salt", InventoryChangeTrigger.TriggerInstance.hasItems(ChemicalHelper.get(TagPrefix.gemExquisite, GTMaterials.Salt).getItem()))
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
                .unlockedBy("has_hydraulic_motor", InventoryChangeTrigger.TriggerInstance.hasItems(GTNAItems.HYDRAULIC_MOTOR.get()))
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
                .unlockedBy("has_hydraulic_piston", InventoryChangeTrigger.TriggerInstance.hasItems(GTNAItems.HYDRAULIC_PISTON.get()))
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
                .unlockedBy("has_hydraulic_pump", InventoryChangeTrigger.TriggerInstance.hasItems(GTNAItems.HYDRAULIC_PUMP.get()))
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
                    .unlockedBy("has_exquisite_salt", InventoryChangeTrigger.TriggerInstance.hasItems(ChemicalHelper.get(TagPrefix.gemExquisite, GTMaterials.Salt).getItem()))
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
                .unlockedBy("has_steam_jet_spewer", InventoryChangeTrigger.TriggerInstance.hasItems(GTNAItems.HYDRAULIC_STEAM_JET_SPEWER.get()))
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
                .unlockedBy("has_rotor", InventoryChangeTrigger.TriggerInstance.hasItems(ChemicalHelper.get(TagPrefix.rotor, GTMaterials.Steel).getItem()))
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
                .unlockedBy("has_spring", InventoryChangeTrigger.TriggerInstance.hasItems(ChemicalHelper.get(TagPrefix.springSmall, GTMaterials.Steel).getItem()))
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
        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("vajra_assembly")
                .inputItems(ChemicalHelper.get(TagPrefix.plateDouble, GTNAMaterials.Echoite), 2)
                .inputItems(GTNAItems.HYDRAULIC_STEAM_JET_SPEWER.get(), 1)
                .inputItems(GTNAItems.HYDRAULIC_MOTOR.get(), 1)
                .inputItems(GTItems.INTEGRATED_CIRCUIT_MV.get(),2)
                .inputItems(GTItems.BATTERY_MV_LITHIUM.get(),2)
                .inputItems(GTItems.EMITTER_MV.get(),2)
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(144))
                .outputItems(BuiltInRegistries.ITEM.get(new ResourceLocation("gtna", "echoite_vajra")))
                .duration(600)
                .EUt(120)
                .save(provider);


    }
}