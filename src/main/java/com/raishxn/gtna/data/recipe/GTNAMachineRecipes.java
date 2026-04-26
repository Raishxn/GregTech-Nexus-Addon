package com.raishxn.gtna.data.recipe;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntCircuitIngredient;
import com.gregtechceu.gtceu.common.data.*;
import com.gregtechceu.gtceu.common.data.machines.GCYMMachines;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.common.data.*;

import java.util.Objects;
import java.util.function.Consumer;

public class GTNAMachineRecipes {

    public static void register(Consumer<FinishedRecipe> provider) {
        if (enabled(GTNAMachines.LARGE_STEAM_CRUSHER)) {
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
        }
        if (enabled(GTNAMachines.WIRELESS_STEAM_INPUT_HATCH)) {
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
        }
        if (enabled(GTNAMachines.WIRELESS_STEAM_OUTPUT_HATCH)) {
            ShapedRecipeBuilder
                    .shaped(RecipeCategory.MISC, GTNAMachines.WIRELESS_STEAM_OUTPUT_HATCH.asStack().getItem())
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
        }
        if (enabled(GTNAMachines.MEGA_PRESSURE_SOLAR_BOILER)) {
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
                            InventoryChangeTrigger.TriggerInstance
                                    .hasItems(GTNABlocks.SOLAR_BOILING_CELL.get().asItem()))
                    .save(provider);
        }
        if (enabled(GTNAMachines.LARGE_STEAM_FURNACE)) {
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
        }
        if (enabled(GTNAMachines.LARGE_STEAM_ALLOY_SMELTER)) {
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
        }
        if (enabled(GTNAMachines.LARGE_STEAM_HAMMER)) {
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNAMachines.LARGE_STEAM_HAMMER.asStack().getItem())
                    .pattern("ABA")
                    .pattern("CDC")
                    .pattern("EAF")
                    .define('A', ChemicalHelper.get(TagPrefix.pipeTinyFluid, GTNAMaterials.Breel).getItem())
                    .define('B', GTNAItems.HYDRAULIC_PISTON.get())
                    .define('C', GTNAItems.PRECISION_STEAM_COMPONENT.get())
                    .define('D', Blocks.ANVIL)
                    .define('E', ChemicalHelper.get(TagPrefix.block, GTMaterials.Iron).getItem())
                    .define('F', GTNAItems.HYDRAULIC_MOTOR.get())
                    .unlockedBy("has_hydraulic_piston",
                            InventoryChangeTrigger.TriggerInstance.hasItems(GTNAItems.HYDRAULIC_PISTON.get()))
                    .save(provider);
        }
        if (enabled(GTNAMachines.LARGE_STEAM_COMPRESSOR)) {
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNAMachines.LARGE_STEAM_COMPRESSOR.asStack().getItem())
                    .pattern("ABA")
                    .pattern("CDC")
                    .pattern("EFE")
                    .define('A', GTNAItems.HYDRAULIC_PISTON.get())
                    .define('B', ChemicalHelper.get(TagPrefix.plate, GTNAMaterials.Stronze).getItem())
                    .define('C', GTNAItems.PRECISION_STEAM_COMPONENT.get())
                    .define('D', ChemicalHelper.get(TagPrefix.block, GTMaterials.Iron).getItem())
                    .define('E', ChemicalHelper.get(TagPrefix.plate, GTMaterials.Bronze).getItem())
                    .define('F', GTNAItems.HYDRAULIC_MOTOR.get())
                    .unlockedBy("has_precision_steam_component",
                            InventoryChangeTrigger.TriggerInstance.hasItems(GTNAItems.PRECISION_STEAM_COMPONENT.get()))
                    .save(provider);
        }
        if (enabled(GTNAMachines.LARGE_STEAM_EXTRACTOR)) {
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNAMachines.LARGE_STEAM_EXTRACTOR.asStack().getItem())
                    .pattern("ABA")
                    .pattern("CDC")
                    .pattern("EFE")
                    .define('A', Items.GLASS_BOTTLE)
                    .define('B', GTNAItems.HYDRAULIC_PUMP.get())
                    .define('C', ChemicalHelper.get(TagPrefix.pipeTinyFluid, GTNAMaterials.Breel).getItem())
                    .define('D', GTNAItems.PRECISION_STEAM_COMPONENT.get())
                    .define('E', ChemicalHelper.get(TagPrefix.plate, GTMaterials.Bronze).getItem())
                    .define('F', GTNAItems.HYDRAULIC_PISTON.get())
                    .unlockedBy("has_hydraulic_pump",
                            InventoryChangeTrigger.TriggerInstance.hasItems(GTNAItems.HYDRAULIC_PUMP.get()))
                    .save(provider);
        }
        if (enabled(GTNAMachines.LARGE_STEAM_ORE_WASHER)) {
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNAMachines.LARGE_STEAM_ORE_WASHER.asStack().getItem())
                    .pattern("ABA")
                    .pattern("CDC")
                    .pattern("EFE")
                    .define('A', ChemicalHelper.get(TagPrefix.rotor, GTMaterials.Steel).getItem())
                    .define('B', GTNAItems.HYDRAULIC_PUMP.get())
                    .define('C', GTNAItems.PRECISION_STEAM_COMPONENT.get())
                    .define('D', ChemicalHelper.get(TagPrefix.pipeTinyFluid, GTNAMaterials.Breel).getItem())
                    .define('E', GTNAItems.HYDRAULIC_MOTOR.get())
                    .define('F', Items.WATER_BUCKET)
                    .unlockedBy("has_hydraulic_motor",
                            InventoryChangeTrigger.TriggerInstance.hasItems(GTNAItems.HYDRAULIC_MOTOR.get()))
                    .save(provider);
        }
        if (enabled(GTNAMachines.LARGE_STEAM_CIRCUIT_ASSEMBLER)) {
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNAMachines.LARGE_STEAM_CIRCUIT_ASSEMBLER.asStack().getItem())
                    .pattern("ABA")
                    .pattern("CDC")
                    .pattern("ABA")
                    .define('A', ChemicalHelper.get(TagPrefix.plate, GTMaterials.Bronze).getItem())
                    .define('B', Blocks.COMPARATOR)
                    .define('C', GTNAItems.PRECISION_STEAM_COMPONENT.get())
                    .define('D', AEBlocks.MOLECULAR_ASSEMBLER.block().asItem())
                    .unlockedBy("has_precision_steam_component",
                            InventoryChangeTrigger.TriggerInstance.hasItems(GTNAItems.PRECISION_STEAM_COMPONENT.get()))
                    .save(provider);
        }
        if (enabled(GTNAMachines.LARGE_STEAM_MIXER)) {
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNAMachines.LARGE_STEAM_MIXER.asStack().getItem())
                    .pattern("ABA")
                    .pattern("CDC")
                    .pattern("EBE")
                    .define('A', ChemicalHelper.get(TagPrefix.rodLong, GTMaterials.Steel).getItem())
                    .define('B', GTNAItems.PRECISION_STEAM_COMPONENT.get())
                    .define('C', ChemicalHelper.get(TagPrefix.pipeHugeFluid, GTMaterials.Copper).getItem())
                    .define('D', GTMachines.MIXER[GTValues.LV].asStack().getItem())
                    .define('E', ChemicalHelper.get(TagPrefix.plate, GTMaterials.Bronze).getItem())
                    .unlockedBy("has_precision_steam_component",
                            InventoryChangeTrigger.TriggerInstance.hasItems(GTNAItems.PRECISION_STEAM_COMPONENT.get()))
                    .save(provider);
        }
        if (enabled(GTNAMachines.LARGE_STEAM_CENTRIFUGE)) {
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNAMachines.LARGE_STEAM_CENTRIFUGE.asStack().getItem())
                    .pattern("ABA")
                    .pattern("CDC")
                    .pattern("ABA")
                    .define('A', ChemicalHelper.get(TagPrefix.block, GTMaterials.Bronze).getItem())
                    .define('B', GTNAItems.PRECISION_STEAM_COMPONENT.get())
                    .define('C', ChemicalHelper.get(TagPrefix.gearSmall, GTMaterials.Iron).getItem())
                    .define('D', GTMachines.CENTRIFUGE[GTValues.LV].asStack().getItem())
                    .unlockedBy("has_precision_steam_component",
                            InventoryChangeTrigger.TriggerInstance.hasItems(GTNAItems.PRECISION_STEAM_COMPONENT.get()))
                    .save(provider);
        }
        if (enabled(GTNAMachines.LARGE_STEAM_THERMAL_CENTRIFUGE)) {
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNAMachines.LARGE_STEAM_THERMAL_CENTRIFUGE.asStack().getItem())
                    .pattern("ABA")
                    .pattern("CDC")
                    .pattern("ABA")
                    .define('A', ChemicalHelper.get(TagPrefix.plate, GTMaterials.Bronze).getItem())
                    .define('B', GTNAItems.PRECISION_STEAM_COMPONENT.get())
                    .define('C', ChemicalHelper.get(TagPrefix.rodLong, GTMaterials.Copper).getItem())
                    .define('D', GTNAMachines.LARGE_STEAM_CENTRIFUGE.asStack().getItem())
                    .unlockedBy("has_large_steam_centrifuge",
                            InventoryChangeTrigger.TriggerInstance.hasItems(
                                    GTNAMachines.LARGE_STEAM_CENTRIFUGE.asStack().getItem()))
                    .save(provider);
        }
        if (enabled(GTNAMachines.LARGE_STEAM_BATH)) {
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNAMachines.LARGE_STEAM_BATH.asStack().getItem())
                    .pattern("ABA")
                    .pattern("CDC")
                    .pattern("EBE")
                    .define('A', ChemicalHelper.get(TagPrefix.foil, GTMaterials.Steel).getItem())
                    .define('B', GTNAItems.PRECISION_STEAM_COMPONENT.get())
                    .define('C', ChemicalHelper.get(TagPrefix.rotor, GTMaterials.Aluminium).getItem())
                    .define('D', GTMachines.CHEMICAL_BATH[GTValues.LV].asStack().getItem())
                    .define('E', ChemicalHelper.get(TagPrefix.block, GTMaterials.Bronze).getItem())
                    .unlockedBy("has_precision_steam_component",
                            InventoryChangeTrigger.TriggerInstance.hasItems(GTNAItems.PRECISION_STEAM_COMPONENT.get()))
                    .save(provider);
        }
        if (enabled(GTNAMachines.PRIMITIVE_DISTILLATION_TOWER)) {
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC,
                    GTNAMachines.PRIMITIVE_DISTILLATION_TOWER.asStack().getItem())
                    .pattern("ABA")
                    .pattern("CDC")
                    .pattern("EFE")
                    .define('A', GTBlocks.CASING_BRONZE_BRICKS.get())
                    .define('B', GTBlocks.CASING_BRONZE_PIPE.get())
                    .define('C', GTNAItems.PRECISION_STEAM_COMPONENT.get())
                    .define('D', GTMachines.DISTILLERY[GTValues.LV].asStack().getItem())
                    .define('E', GTNAItems.HYDRAULIC_PUMP.get())
                    .define('F', Items.CAULDRON)
                    .unlockedBy("has_precision_steam_component",
                            InventoryChangeTrigger.TriggerInstance.hasItems(GTNAItems.PRECISION_STEAM_COMPONENT.get()))
                    .save(provider);
        }
        if (enabled(GTNAMachines.LARGE_STEAM_LATHE)) {
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNAMachines.LARGE_STEAM_LATHE.asStack().getItem())
                    .pattern("ABA")
                    .pattern("CDC")
                    .pattern("EFE")
                    .define('A', ChemicalHelper.get(TagPrefix.plate, GTMaterials.Bronze).getItem())
                    .define('B', GTNAItems.HYDRAULIC_MOTOR.get())
                    .define('C', GTNAItems.PRECISION_STEAM_COMPONENT.get())
                    .define('D', GTMachines.LATHE[GTValues.LV].asStack().getItem())
                    .define('E', ChemicalHelper.get(TagPrefix.rodLong, GTMaterials.Bronze).getItem())
                    .define('F', GTBlocks.CASING_BRONZE_PIPE.get())
                    .unlockedBy("has_precision_steam_component",
                            InventoryChangeTrigger.TriggerInstance.hasItems(GTNAItems.PRECISION_STEAM_COMPONENT.get()))
                    .save(provider);
        }
        if (enabled(GTNAMachines.LARGE_STEAM_CUTTING)) {
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNAMachines.LARGE_STEAM_CUTTING.asStack().getItem())
                    .pattern("ABA")
                    .pattern("CDC")
                    .pattern("EFE")
                    .define('A', ChemicalHelper.get(TagPrefix.plate, GTMaterials.Bronze).getItem())
                    .define('B', GTNAItems.HYDRAULIC_PUMP.get())
                    .define('C', GTNAItems.PRECISION_STEAM_COMPONENT.get())
                    .define('D', GTMachines.CUTTER[GTValues.LV].asStack().getItem())
                    .define('E', ChemicalHelper.get(TagPrefix.rodLong, GTMaterials.Bronze).getItem())
                    .define('F', Blocks.DIAMOND_BLOCK)
                    .unlockedBy("has_precision_steam_component",
                            InventoryChangeTrigger.TriggerInstance.hasItems(GTNAItems.PRECISION_STEAM_COMPONENT.get()))
                    .save(provider);
        }
        if (enabled(GTNAMachines.LARGE_STEAM_FORMING_PRESS)) {
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC,
                    GTNAMachines.LARGE_STEAM_FORMING_PRESS.asStack().getItem())
                    .pattern("ABA")
                    .pattern("CDC")
                    .pattern("EFE")
                    .define('A', ChemicalHelper.get(TagPrefix.plate, GTMaterials.Bronze).getItem())
                    .define('B', GTNAItems.HYDRAULIC_PISTON.get())
                    .define('C', GTNAItems.PRECISION_STEAM_COMPONENT.get())
                    .define('D', GTMachines.FORMING_PRESS[GTValues.LV].asStack().getItem())
                    .define('E', GTBlocks.CASING_BRONZE_GEARBOX.get())
                    .define('F', GTBlocks.CASING_BRONZE_PIPE.get())
                    .unlockedBy("has_precision_steam_component",
                            InventoryChangeTrigger.TriggerInstance.hasItems(GTNAItems.PRECISION_STEAM_COMPONENT.get()))
                    .save(provider);
        }
        if (enabled(GTNAMachines.LARGE_STEAM_STORAGE_TANK)) {
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNAMachines.LARGE_STEAM_STORAGE_TANK.asStack().getItem())
                    .pattern("ABA")
                    .pattern("CDC")
                    .pattern("ABA")
                    .define('A', GTNABlocks.BRASS_REINFORCED_WOODEN_CASING.get())
                    .define('B', GTBlocks.CASING_STEEL_SOLID.get())
                    .define('C', ChemicalHelper.get(TagPrefix.pipeLargeFluid, GTMaterials.Bronze).getItem())
                    .define('D', GTMultiMachines.STEEL_MULTIBLOCK_TANK.asStack().getItem())
                    .unlockedBy("has_brass_reinforced_wooden_casing",
                            InventoryChangeTrigger.TriggerInstance.hasItems(GTNABlocks.BRASS_REINFORCED_WOODEN_CASING.get()))
                    .save(provider);
        }
        if (enabled(GTNAMachines.LARGE_STEAM_SOLAR_BOILER)) {
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNAMachines.LARGE_STEAM_SOLAR_BOILER.asStack().getItem())
                    .pattern("ABA")
                    .pattern("CDC")
                    .pattern("EFE")
                    .define('A', GTNABlocks.SOLAR_HEAT_COLLECTOR_PIPE_CASING.get())
                    .define('B', GTNAItems.HYDRAULIC_PUMP.get())
                    .define('C', GTBlocks.STEEL_HULL.get())
                    .define('D', GTMachines.STEAM_SOLAR_BOILER.right().asStack().getItem())
                    .define('E', GTBlocks.CASING_BRONZE_PIPE.get())
                    .define('F', GTNAItems.PRECISION_STEAM_COMPONENT.get())
                    .unlockedBy("has_solar_heat_collector_pipe_casing",
                            InventoryChangeTrigger.TriggerInstance.hasItems(GTNABlocks.SOLAR_HEAT_COLLECTOR_PIPE_CASING.get()))
                    .save(provider);
        }
        if (enabled(GTNAMachines.DIMENSIONALLY_TRANSCENDENT_STEAM_BOILER)) {
            GTRecipeTypes.COMPRESSOR_RECIPES.recipeBuilder("dimensionally_transcendent_steam_boiler")
                    .inputItems(GTMultiMachines.LARGE_BOILER_TUNGSTENSTEEL.asStack().getItem(), 16)
                    .outputItems(GTNAMachines.DIMENSIONALLY_TRANSCENDENT_STEAM_BOILER.asStack())
                    .duration(2400)
                    .EUt(20)
                    .save(provider);
        }
        if (enabled(GTNAMachines.DIMENSIONALLY_TRANSCENDENT_DIRT_FORGE)) {
            GTRecipeTypes.COMPRESSOR_RECIPES.recipeBuilder("dimensionally_transcendent_dirt_forge")
                    .inputItems(GTNAMachines.LEAP_FORWARD_ONE_BLAST_FURNACE.asStack().getItem(), 16)
                    .outputItems(GTNAMachines.DIMENSIONALLY_TRANSCENDENT_DIRT_FORGE.asStack())
                    .duration(2400)
                    .EUt(20)
                    .save(provider);
        }
        if (enabled(GTNAMachines.DIMENSIONALLY_TRANSCENDENT_STEAM_OVEN)) {
            GTRecipeTypes.COMPRESSOR_RECIPES.recipeBuilder("dimensionally_transcendent_steam_oven")
                    .inputItems(GTNAMachines.LARGE_STEAM_FURNACE.asStack().getItem(), 16)
                    .outputItems(GTNAMachines.DIMENSIONALLY_TRANSCENDENT_STEAM_OVEN.asStack())
                    .duration(2400)
                    .EUt(20)
                    .save(provider);
        }
        if (enabled(GTNAMachines.EYE_OF_WOOD)) {
            GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("primitive_mans_spacetime_distortion_device")
                    .notConsumable(IntCircuitIngredient.of(17))
                    .inputItems(Items.ENCHANTED_GOLDEN_APPLE, 1)
                    .inputItems(GTItems.EMITTER_LV.asStack().getItem(), 64)
                    .inputItems(GTItems.FIELD_GENERATOR_LV.asStack().getItem(), 64)
                    .inputItems(CustomTags.LV_CIRCUITS, 64)
                    .outputItems(GTNAItems.PRIMITIVE_MANS_SPACETIME_DISTORTION_DEVICE.get())
                    .duration(2280)
                    .EUt(GTValues.VA[GTValues.LV])
                    .save(provider);

            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNAMachines.EYE_OF_WOOD.asStack().getItem())
                    .pattern("ABA")
                    .pattern("BCB")
                    .pattern("ABA")
                    .define('A', Blocks.BRICKS)
                    .define('B', ItemTags.PLANKS)
                    .define('C', GTNAItems.PRIMITIVE_MANS_SPACETIME_DISTORTION_DEVICE.get())
                    .unlockedBy("has_primitive_mans_spacetime_distortion_device",
                            InventoryChangeTrigger.TriggerInstance.hasItems(
                                    GTNAItems.PRIMITIVE_MANS_SPACETIME_DISTORTION_DEVICE.get()))
                    .save(provider);
        }

        if (enabled(GTNAMachines.STEAM_COBBLER)) {
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
                                    .hasItems(ChemicalHelper.get(TagPrefix.frameGt, GTNAMaterials.ClayCompound)
                                            .getItem()))
                    .save(provider);
        }

        if (enabled(GTNAMachines.STONE_SUPERHEATER)) {
            GTNARecipeType.HYDRAULIC_MANUFACTURING.recipeBuilder("stone_superheater_controller")
                    .inputItems(GTNABlocks.STRONZE_WRAPPED_CASING.get(), 1)
                    .inputItems(GTNAItems.HYDRAULIC_MOTOR.get(), 2)
                    .inputItems(ChemicalHelper.get(TagPrefix.pipeNormalFluid, GTNAMaterials.Stronze).getItem(), 2)
                    .inputItems(ChemicalHelper.get(TagPrefix.pipeNormalFluid, GTNAMaterials.Breel).getItem(), 2)
                    .outputItems(GTNAMachines.STONE_SUPERHEATER.asStack())
                    .duration(400)
                    .EUt(250)
                    .save(provider);
        }
        if (GTNAMachines2.DIRECTED_TESSERACT_GENERATOR != null) {
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNAMachines2.DIRECTED_TESSERACT_GENERATOR.asStack().getItem())
                    .pattern("ABA")
                    .pattern("CDC")
                    .pattern("EFE")
                    .define('A', AEItems.WIRELESS_RECEIVER.asItem())
                    .define('B', GTItems.EMITTER_HV.get())
                    .define('C', GTItems.FIELD_GENERATOR_HV.get())
                    .define('D', GTMachines.HULL[GTValues.IV].asStack().getItem())
                    .define('E', GTNAItems.TESSERACT_TARGET_MARKER.get())
                    .define('F', CustomTags.IV_CIRCUITS)
                    .unlockedBy("has_tesseract_marker",
                            InventoryChangeTrigger.TriggerInstance.hasItems(GTNAItems.TESSERACT_TARGET_MARKER.get()))
                    .save(provider);
        }
        if (enabled(GTNAMachines.STEAM_MANUFACTURER)) {
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNAMachines.STEAM_MANUFACTURER.asStack().getItem())
                    .pattern("AAA")
                    .pattern("BCB")
                    .pattern("DED")
                    .define('A', GTNAItems.HYDRAULIC_ARM.get())
                    .define('B', GTNABlocks.HYDRAULIC_ASSEMBLER_CASING.get())
                    .define('C', ChemicalHelper.get(TagPrefix.plateDouble, GTNAMaterials.Stronze).getItem())
                    .define('D', GTBlocks.CASING_STEEL_GEARBOX.get())
                    .define('E', GTNAItems.HYDRAULIC_CONVEYOR.get())
                    .unlockedBy("has_hydraulic_casing",
                            InventoryChangeTrigger.TriggerInstance
                                    .hasItems(GTNABlocks.HYDRAULIC_ASSEMBLER_CASING.get()))
                    .save(provider);
        }
        if (enabled(GTNAMachines.STEAM_WOODCUTTER)) {
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
        }
        if (enabled(GTNAMachines.LEAP_FORWARD_ONE_BLAST_FURNACE)) {
            ShapedRecipeBuilder
                    .shaped(RecipeCategory.MISC, GTNAMachines.LEAP_FORWARD_ONE_BLAST_FURNACE.asStack().getItem())
                    .pattern("ABA")
                    .pattern("BCB")
                    .pattern("ABA")
                    .define('A', ChemicalHelper.get(TagPrefix.plateDouble, GTMaterials.Bronze).getItem())
                    .define('B', GTNAItems.PRECISION_STEAM_COMPONENT.get())
                    .define('C', GTMultiMachines.PRIMITIVE_BLAST_FURNACE.asStack().getItem())
                    .unlockedBy("has_precision_steam_component",
                            InventoryChangeTrigger.TriggerInstance.hasItems(GTNAItems.PRECISION_STEAM_COMPONENT.get()))
                    .save(provider);
        }

        if (enabled(GTNAMachines.HUGE_STEAM_INPUT_BUS)) {
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
        }

        // --- Huge Steam Output Bus ---
        if (enabled(GTNAMachines.HUGE_STEAM_OUTPUT_BUS)) {
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
        }

        if (enabled(GTNAMachines.INFINITE_STEAM_INPUT_BUS))
            GTNARecipeVisibility.saveRestricted(provider, GTNACORE.id("infinite_steam_input_bus"),
                    restrictedProvider -> ShapedRecipeBuilder.shaped(RecipeCategory.MISC,
                            GTNAMachines.INFINITE_STEAM_INPUT_BUS.asStack().getItem())
                            .pattern("ABA")
                            .pattern("CDC")
                            .pattern("ABA")
                            .define('A', GTMachines.BRONZE_CRATE.asStack().getItem())
                            .define('B', GTNAItems.HYDRAULIC_CONVEYOR.get())
                            .define('C', GTNAItems.PRECISION_STEAM_COMPONENT.get())
                            .define('D', GTMachines.STEAM_IMPORT_BUS.asStack().getItem())
                            .unlockedBy("has_steam_import", InventoryChangeTrigger.TriggerInstance
                                    .hasItems(GTMachines.STEAM_IMPORT_BUS.asStack().getItem()))
                            .save(restrictedProvider));

        if (enabled(GTNAMachines.OUTPUT_BOOST_STEAM_OUTPUT_BUS))
            GTNARecipeVisibility.saveRestricted(provider, GTNACORE.id("output_boost_steam_output_bus"),
                    restrictedProvider -> ShapedRecipeBuilder.shaped(RecipeCategory.MISC,
                            GTNAMachines.OUTPUT_BOOST_STEAM_OUTPUT_BUS.asStack().getItem())
                            .pattern("ABA")
                            .pattern("CDC")
                            .pattern("ABA")
                            .define('A', GTMachines.BRONZE_CRATE.asStack().getItem())
                            .define('B', GTNAItems.HYDRAULIC_ARM.get())
                            .define('C', GTNAItems.PRECISION_STEAM_COMPONENT.get())
                            .define('D', GTMachines.STEAM_EXPORT_BUS.asStack().getItem())
                            .unlockedBy("has_steam_export", InventoryChangeTrigger.TriggerInstance
                                    .hasItems(GTMachines.STEAM_EXPORT_BUS.asStack().getItem()))
                            .save(restrictedProvider));

        // --- Wireless Steam Input Hatch (STEEL) ---
        if (enabled(GTNAMachines.WIRELESS_STEAM_INPUT_HATCH, GTNAMachines.WIRELESS_STEAM_INPUT_HATCH_STEEL)) {
            GTNARecipeType.HYDRAULIC_MANUFACTURING.recipeBuilder("wireless_steam_input_hatch_steel")
                    .inputItems(GTMachines.STEEL_DRUM.asStack().getItem(), 8)
                    .inputItems(GTNAMachines.WIRELESS_STEAM_INPUT_HATCH.asStack().getItem(), 1)
                    .outputItems(GTNAMachines.WIRELESS_STEAM_INPUT_HATCH_STEEL.asStack())
                    .duration(400)
                    .EUt(120)
                    .save(provider);
        }

        // --- Wireless Steam Output Hatch (STEEL) ---
        if (enabled(GTNAMachines.WIRELESS_STEAM_OUTPUT_HATCH, GTNAMachines.WIRELESS_STEAM_OUTPUT_HATCH_STEEL)) {
            GTNARecipeType.HYDRAULIC_MANUFACTURING.recipeBuilder("wireless_steam_output_hatch_steel")
                    .inputItems(GTMachines.STEEL_DRUM.asStack().getItem(), 8)
                    .inputItems(GTNAMachines.WIRELESS_STEAM_OUTPUT_HATCH.asStack().getItem(), 1)
                    .outputItems(GTNAMachines.WIRELESS_STEAM_OUTPUT_HATCH_STEEL.asStack())
                    .duration(400)
                    .EUt(120)
                    .save(provider);
        }

        if (enabled(GTNAMachines.INFERNAL_COKE_OVEN)) {
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNAMachines.INFERNAL_COKE_OVEN.asStack().getItem())
                    .pattern("ABA")
                    .pattern("CDC")
                    .pattern("ABA")
                    .define('A', Blocks.NETHER_BRICKS)
                    .define('B', ChemicalHelper.get(TagPrefix.plate, GTNAMaterials.Breel).getItem())
                    .define('C', ChemicalHelper.get(TagPrefix.plate, GTNAMaterials.Stronze).getItem())
                    .define('D', GTMultiMachines.COKE_OVEN.asStack().getItem())
                    .unlockedBy("has_coke_oven",
                            InventoryChangeTrigger.TriggerInstance
                                    .hasItems(GTMultiMachines.COKE_OVEN.asStack().getItem()))
                    .save(provider);
        }

        if (enabled(GTNAMachines.HYPER_PRESSURE_REACTOR, GTNAMachines.MEGA_PRESSURE_SOLAR_BOILER)) {
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
        }

        if (enabled(GTNAMachines.COMPACT_HYPER_PRESSURE_REACTOR, GTNAMachines.HYPER_PRESSURE_REACTOR)) {
            GTNARecipeType.HYDRAULIC_MANUFACTURING.recipeBuilder("compact_hyper_pressure_reactor")
                    .inputItems(GTNAMachines.HYPER_PRESSURE_REACTOR.asStack().getItem(), 64)
                    .inputItems(GTNAItems.HYDRAULIC_VAPOR_GENERATOR.get(), 8)
                    .outputItems(GTNAMachines.COMPACT_HYPER_PRESSURE_REACTOR.asStack())
                    .duration(2400)
                    .EUt(1600)
                    .save(provider);
        }

        if (enabled(GTNAMachines.VOID_MINER_STEAM_GATE_AGED, GTNAMachines.LARGE_STEAM_FURNACE,
                GTNAMachines.LARGE_STEAM_CRUSHER)) {
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
        }

        if (enabled(GTNAMachines.INDUSTRIAL_SLAUGHTERHOUSE)) {
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
        }

        if (enabled(GTNAMachines.INDUSTRIAL_PLATFORM_DEPLOYMENT_TOOLS)) {
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC,
                    GTNAMachines.INDUSTRIAL_PLATFORM_DEPLOYMENT_TOOLS.asStack().getItem())
                    .pattern("AAA")
                    .pattern("ABA")
                    .pattern("AAA")
                    .define('A', AEItems.MATTER_BALL.asItem())
                    .define('B', GTNAItems.INDUSTRIAL_COMPONENTS[0][0].get())
                    .unlockedBy("has_standard_industrial_component_small",
                            InventoryChangeTrigger.TriggerInstance
                                    .hasItems(GTNAItems.INDUSTRIAL_COMPONENTS[0][0].get()))
                    .save(provider);
        }

        if (enabled(GTNAMachines.ARTIFICIAL_STAR)) {
            GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder("artificial_star")
                    .inputItems(GTNABlocks.GRAVITON_FIELD_CONSTRAINT_CASING.asItem(), 4)
                    .inputItems(GTNABlocks.ANNIHILATE_CORE.asItem())
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
        }

        if (enabled(GTNAMachines.EYE_OF_HARMONY, GTNAMachines.ARTIFICIAL_STAR)) {
            GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder("eye_of_harmony")
                    .inputItems(GTNABlocks.DIMENSION_INJECTION_CASING.asItem(), 16)
                    .inputItems(GTNABlocks.SPACETIME_COMPRESSION_FIELD_GENERATOR.asItem(), 16)
                    .inputItems(GTNABlocks.DIMENSIONAL_STABILITY_CASING.asItem(), 16)
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
        }

        if (enabled(GTNAMachines.NEXUS_MOLECULAR_FORGE, GTNAMachines2.ME_CRAFT_PATTERN_HATCH)) {
            GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder("nexus_molecular_forge")
                    .inputItems(GTMachines.ASSEMBLER[GTValues.ZPM].asStack().getItem())
                    .inputItems(GTNAMachines2.ME_CRAFT_PATTERN_HATCH.asStack().getItem(), 4)
                    .inputItems("expatternprovider:assembler_matrix_crafter", 32)
                    .inputItems("expatternprovider:assembler_matrix_pattern", 32)
                    .inputItems("expatternprovider:assembler_matrix_speed", 32)
                    .inputItems(GTItems.ROBOT_ARM_ZPM.asStack().getItem(), 4)
                    .inputItems(GTItems.EMITTER_ZPM.asStack().getItem(), 8)
                    .inputItems(CustomTags.ZPM_CIRCUITS, 8)
                    .inputItems(GTNABlocks.OXIDATION_RESISTANT_HASTELLOY_N_MECHANICAL_CASING.asItem(), 16)
                    .inputItems(GTNABlocks.ZIRCONIA_CERAMIC_HIGH_STRENGTH_BENDING_RESISTANCE_MECHANICAL_BLOCK.asItem(), 20)
                    .inputItems(GTNABlocks.NAQUADAH_BOROSILICATE_GLASS.asItem(), 8)
                    .inputItems(GTNABlocks.MAGTECH_CASING.asItem(), 8)
                    .inputItems(GTNABlocks.PROCESS_MACHINE_CASING.asItem(), 8)
                    .inputItems(GTNABlocks.COMPRESSOR_CONTROLLER_CASING.asItem(), 4)
                    .inputItems(GTNABlocks.EXTREME_DENSITY_CASING.asItem(), 4)
                    .inputItems(GTBlocks.CASING_ASSEMBLY_LINE.asItem(), 16)
                    .inputItems(GTBlocks.HIGH_POWER_CASING.asItem(), 16)
                    .inputItems(TagPrefix.wireFine, GTMaterials.Tritanium, 64)
                    .inputItems(TagPrefix.plateDouble, GTMaterials.NaquadahAlloy, 8)
                    .inputItems(TagPrefix.frameGt, GTMaterials.Europium, 4)
                    .inputItems(TagPrefix.frameGt, GTNAMaterials.HastelloyN, 4)
                    .inputFluids(GTMaterials.SolderingAlloy.getFluid(2304))
                    .inputFluids(GTMaterials.Polybenzimidazole.getFluid(2304))
                    .outputItems(GTNAMachines.NEXUS_MOLECULAR_FORGE.asStack())
                    .duration(600)
                    .EUt(GTValues.VA[GTValues.ZPM])
                    .stationResearch(b -> b.researchStack(AEBlocks.MOLECULAR_ASSEMBLER.stack(1))
                            .CWUt(64)
                            .EUt(GTValues.VA[GTValues.ZPM]))
                    .save(provider);
        }

        if (enabled(GTNAMachines.NEXUS_ME_HYPERCORE)) {
            GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder("me_super_computer_core")
                    .inputItems(GTBlocks.HIGH_POWER_CASING.asItem(), 16)
                    .inputItems(GTNABlocks.HIGH_STRENGTH_CONCRETE.asItem(), 32)
                    .inputItems(GTNABlocks.COBALT_OXIDE_CERAMIC_STRONG_THERMALLY_CONDUCTIVE_MECHANICAL_BLOCK.asItem(), 16)
                    .inputItems(GTNABlocks.OXIDATION_RESISTANT_HASTELLOY_N_MECHANICAL_CASING.asItem(), 8)
                    .inputItems(GCYMBlocks.ELECTROLYTIC_CELL.asItem(), 8)
                    .inputItems(GCYMBlocks.MOLYBDENUM_DISILICIDE_COIL_BLOCK.asItem(), 8)
                    .inputItems(AEBlocks.CRAFTING_UNIT.stack(16).getItem(), 16)
                    .inputItems(CustomTags.UV_CIRCUITS, 8)
                    .inputItems(GTItems.EMITTER_UV.asStack().getItem(), 8)
                    .inputItems(GTItems.SENSOR_UV.asStack().getItem(), 8)
                    .inputFluids(GTMaterials.SolderingAlloy.getFluid(4608))
                    .inputFluids(GTMaterials.Polybenzimidazole.getFluid(2304))
                    .outputItems(GTNAMachines.NEXUS_ME_HYPERCORE.asStack())
                    .duration(900)
                    .EUt(GTValues.VA[GTValues.UV])
                    .stationResearch(b -> b.researchStack(AEBlocks.CRAFTING_UNIT.stack(1))
                            .CWUt(256)
                            .EUt(GTValues.VA[GTValues.UV]))
                    .save(provider);
        }

        if (enabled(GTNAMachines.ME_STORAGE)) {
            GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder("me_storage")
                    .inputItems(GTBlocks.COMPUTER_CASING.asItem(), 32)
                    .inputItems(GTBlocks.COMPUTER_HEAT_VENT.asItem(), 16)
                    .inputItems(GTBlocks.HIGH_POWER_CASING.asItem(), 8)
                    .inputItems(GTNABlocks.LITHIUM_OXIDE_CERAMIC_HEAT_RESISTANT_SHOCK_RESISTANT_MECHANICAL_CUBE.asItem(), 16)
                    .inputItems(GTNABlocks.ABS_BLACK_CASING.asItem(), 16)
                    .inputItems(AEBlocks.CRAFTING_STORAGE_256K.stack(4).getItem(), 4)
                    .inputItems(CustomTags.ZPM_CIRCUITS, 8)
                    .inputItems(GTItems.FIELD_GENERATOR_ZPM.asStack().getItem(), 4)
                    .inputFluids(GTMaterials.SolderingAlloy.getFluid(2304))
                    .inputFluids(GTMaterials.Polybenzimidazole.getFluid(2304))
                    .outputItems(GTNAMachines.ME_STORAGE.asStack())
                    .duration(600)
                    .EUt(GTValues.VA[GTValues.ZPM])
                    .stationResearch(b -> b.researchStack(AEBlocks.CRAFTING_STORAGE_256K.stack(1))
                            .CWUt(128)
                            .EUt(GTValues.VA[GTValues.ZPM]))
                    .save(provider);
        }

        registerMEStorageCoreRecipes(provider);

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

    private static void registerMEStorageCoreRecipes(Consumer<FinishedRecipe> provider) {
        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("t1_me_storage_core")
                .inputItems(AEBlocks.CRAFTING_STORAGE_64K.stack(1).getItem())
                .inputItems(TagPrefix.plate, GTNAMaterials.DarkSteel, 4)
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(576))
                .outputItems(GTNABlocks.T1_ME_STORAGE_CORE.asItem())
                .duration(200)
                .EUt(GTValues.VA[GTValues.EV])
                .save(provider);
        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("t2_me_storage_core")
                .inputItems(AEBlocks.CRAFTING_STORAGE_256K.stack(1).getItem())
                .inputItems(GTNABlocks.T1_ME_STORAGE_CORE.asItem())
                .inputItems(TagPrefix.plate, GTNAMaterials.DarkSteel, 8)
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(1152))
                .outputItems(GTNABlocks.T2_ME_STORAGE_CORE.asItem())
                .duration(240)
                .EUt(GTValues.VA[GTValues.IV])
                .save(provider);
        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("t3_me_storage_core")
                .inputItems(AEBlocks.CRAFTING_STORAGE_256K.stack(2).getItem(), 2)
                .inputItems(GTNABlocks.T2_ME_STORAGE_CORE.asItem())
                .inputItems(TagPrefix.plate, GTMaterials.TungstenSteel, 8)
                .inputFluids(GTMaterials.Polybenzimidazole.getFluid(576))
                .outputItems(GTNABlocks.T3_ME_STORAGE_CORE.asItem())
                .duration(280)
                .EUt(GTValues.VA[GTValues.LuV])
                .save(provider);
        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("t4_me_storage_core")
                .inputItems(AEBlocks.CRAFTING_STORAGE_256K.stack(4).getItem(), 4)
                .inputItems(GTNABlocks.T3_ME_STORAGE_CORE.asItem())
                .inputItems(TagPrefix.plate, GTMaterials.RhodiumPlatedPalladium, 8)
                .inputFluids(GTMaterials.Polybenzimidazole.getFluid(1152))
                .outputItems(GTNABlocks.T4_ME_STORAGE_CORE.asItem())
                .duration(320)
                .EUt(GTValues.VA[GTValues.ZPM])
                .save(provider);
        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("t5_me_storage_core")
                .inputItems(AEBlocks.CRAFTING_STORAGE_256K.stack(8).getItem(), 8)
                .inputItems(GTNABlocks.T4_ME_STORAGE_CORE.asItem())
                .inputItems(TagPrefix.plate, GTMaterials.NaquadahAlloy, 8)
                .inputFluids(GTMaterials.Polybenzimidazole.getFluid(2304))
                .outputItems(GTNABlocks.T5_ME_STORAGE_CORE.asItem())
                .duration(360)
                .EUt(GTValues.VA[GTValues.UV])
                .save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("t1_crafting_storage_core")
                .inputItems(GTNABlocks.T1_ME_STORAGE_CORE.asItem())
                .inputItems(AEBlocks.CRAFTING_UNIT.stack(4).getItem(), 4)
                .outputItems(GTNABlocks.T1_CRAFTING_STORAGE_CORE.asItem())
                .duration(120)
                .EUt(GTValues.VA[GTValues.EV])
                .save(provider);
        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("t2_crafting_storage_core")
                .inputItems(GTNABlocks.T2_ME_STORAGE_CORE.asItem())
                .inputItems(AEBlocks.CRAFTING_UNIT.stack(8).getItem(), 8)
                .outputItems(GTNABlocks.T2_CRAFTING_STORAGE_CORE.asItem())
                .duration(140)
                .EUt(GTValues.VA[GTValues.IV])
                .save(provider);
        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("t3_crafting_storage_core")
                .inputItems(GTNABlocks.T3_ME_STORAGE_CORE.asItem())
                .inputItems(AEBlocks.CRAFTING_UNIT.stack(16).getItem(), 16)
                .outputItems(GTNABlocks.T3_CRAFTING_STORAGE_CORE.asItem())
                .duration(160)
                .EUt(GTValues.VA[GTValues.LuV])
                .save(provider);
        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("t4_crafting_storage_core")
                .inputItems(GTNABlocks.T4_ME_STORAGE_CORE.asItem())
                .inputItems(AEBlocks.CRAFTING_UNIT.stack(32).getItem(), 32)
                .outputItems(GTNABlocks.T4_CRAFTING_STORAGE_CORE.asItem())
                .duration(180)
                .EUt(GTValues.VA[GTValues.ZPM])
                .save(provider);
        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("t5_crafting_storage_core")
                .inputItems(GTNABlocks.T5_ME_STORAGE_CORE.asItem())
                .inputItems(AEBlocks.CRAFTING_UNIT.stack(64).getItem(), 64)
                .outputItems(GTNABlocks.T5_CRAFTING_STORAGE_CORE.asItem())
                .duration(200)
                .EUt(GTValues.VA[GTValues.UV])
                .save(provider);
    }

    private static boolean enabled(MachineDefinition... definitions) {
        for (MachineDefinition definition : definitions) {
            if (definition == null) {
                return false;
            }
        }
        return true;
    }

    private static Item machineItem(String id) {
        Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(id));
        if (item == Items.AIR) {
            throw new IllegalStateException("Missing machine item id: " + id);
        }
        return item;
    }
}
