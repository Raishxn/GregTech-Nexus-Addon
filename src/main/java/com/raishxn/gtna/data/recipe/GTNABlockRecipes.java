package com.raishxn.gtna.data.recipe;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.*;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.data.recipe.GTCraftingComponents;

import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.api.data.tag.GTNATagPrefix;
import com.raishxn.gtna.common.data.GTNABlocks;
import com.raishxn.gtna.common.data.GTNAItems;
import com.raishxn.gtna.common.data.GTNAMachines;
import com.raishxn.gtna.common.data.GTNAMaterials;
import com.raishxn.gtna.common.data.GTNARecipeType;
import com.tterrag.registrate.util.entry.BlockEntry;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.common.data.GTMaterials.CHEMICAL_DYES;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Lava;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.ROCK_BREAKER_RECIPES;

public class GTNABlockRecipes {

    public static void register(Consumer<FinishedRecipe> provider) {
        // 1. Breel Pipe Casing
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNABlocks.BREEL_PIPE_CASING.get(), 2)
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', ChemicalHelper.get(TagPrefix.plate, GTNAMaterials.ClayCompound).getItem())
                .define('B', ChemicalHelper.get(TagPrefix.pipeNormalFluid, GTNAMaterials.Breel).getItem())
                .define('C', ChemicalHelper.get(TagPrefix.frameGt, GTNAMaterials.ClayCompound).getItem())
                .unlockedBy("has_clay_compound_plate",
                        InventoryChangeTrigger.TriggerInstance
                                .hasItems(ChemicalHelper.get(TagPrefix.plate, GTNAMaterials.ClayCompound).getItem()))
                .save(provider);

        // 2. Hyper Pressure Breel Casing
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNABlocks.HYPER_PRESSURE_BREEL_CASING.get())
                .pattern("AAA")
                .pattern("BCB")
                .pattern("AAA")
                .define('A', ChemicalHelper.get(TagPrefix.plate, GTNAMaterials.Breel).getItem())
                .define('B', ChemicalHelper.get(TagPrefix.rod, GTMaterials.Beryllium).getItem())
                .define('C', ChemicalHelper.get(TagPrefix.frameGt, GTMaterials.Beryllium).getItem())
                .unlockedBy("has_breel_plate",
                        InventoryChangeTrigger.TriggerInstance
                                .hasItems(ChemicalHelper.get(TagPrefix.plate, GTNAMaterials.Breel).getItem()))
                .save(provider);

        // 3. Vibration-Safe Casing
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNABlocks.VIBRATION_SAFE_CASING.get())
                .pattern("AAA")
                .pattern("BCB")
                .pattern("AAA")
                .define('A', ChemicalHelper.get(TagPrefix.plate, GTNAMaterials.ClayCompound).getItem())
                .define('B', ChemicalHelper.get(TagPrefix.plateDouble, GTNAMaterials.Breel).getItem())
                .define('C', GTBlocks.CASING_STEEL_SOLID.get())
                .unlockedBy("has_clay_compound_plate",
                        InventoryChangeTrigger.TriggerInstance
                                .hasItems(ChemicalHelper.get(TagPrefix.plate, GTNAMaterials.ClayCompound).getItem()))
                .save(provider);

        // 4. Bronze Reinforced Wood
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNABlocks.BRONZE_REINFORCED_WOOD.get())
                .pattern("AAA")
                .pattern("ABA")
                .pattern("AAA")
                .define('A', ChemicalHelper.get(TagPrefix.plate, GTMaterials.Bronze).getItem())
                .define('B', ChemicalHelper.get(TagPrefix.frameGt, GTMaterials.Wood).getItem())
                .unlockedBy("has_bronze_plate",
                        InventoryChangeTrigger.TriggerInstance
                                .hasItems(ChemicalHelper.get(TagPrefix.plate, GTMaterials.Bronze).getItem()))
                .save(provider);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNABlocks.STEEL_REINFORCED_WOOD.get())
                .pattern("AAA")
                .pattern("ABA")
                .pattern("AAA")
                .define('A', ChemicalHelper.get(TagPrefix.plate, GTMaterials.Steel).getItem())
                .define('B', ChemicalHelper.get(TagPrefix.frameGt, GTMaterials.Wood).getItem())
                .unlockedBy("has_steel_plate",
                        InventoryChangeTrigger.TriggerInstance
                                .hasItems(ChemicalHelper.get(TagPrefix.plate, GTMaterials.Steel).getItem()))
                .save(provider);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNABlocks.IRON_REINFORCED_WOOD.get())
                .pattern("AAA")
                .pattern("ABA")
                .pattern("AAA")
                .define('A', ChemicalHelper.get(TagPrefix.plate, GTMaterials.Iron).getItem())
                .define('B', ChemicalHelper.get(TagPrefix.frameGt, GTMaterials.Wood).getItem())
                .unlockedBy("has_iron_plate",
                        InventoryChangeTrigger.TriggerInstance
                                .hasItems(ChemicalHelper.get(TagPrefix.plate, GTMaterials.Iron).getItem()))
                .save(provider);

        // 5. Solar Boiling Cell
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNABlocks.SOLAR_BOILING_CELL.get())
                .pattern("AAA")
                .pattern("BCB")
                .define('A', Blocks.GLASS)
                .define('B', ChemicalHelper.get(TagPrefix.pipeTinyFluid, GTNAMaterials.Stronze).getItem())
                .define('C', GTMachines.STEAM_SOLAR_BOILER.right().asStack().getItem())
                .unlockedBy("has_glass", InventoryChangeTrigger.TriggerInstance.hasItems(Blocks.GLASS))
                .save(provider);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNABlocks.STEAM_ASSEMBLY_BLOCK.get())
                .pattern("ABA")
                .pattern("DCD")
                .pattern("ABA")
                .define('A', ChemicalHelper.get(TagPrefix.pipeNormalFluid, GTMaterials.Bronze).getItem())
                .define('B', GTNAItems.PRECISION_STEAM_COMPONENT.get())
                .define('C', ChemicalHelper.get(TagPrefix.frameGt, GTMaterials.Bronze).getItem())
                .define('D', ChemicalHelper.get(TagPrefix.plate, GTMaterials.Bronze).getItem())
                .unlockedBy("has_precision_steam_component",
                        InventoryChangeTrigger.TriggerInstance.hasItems(GTNAItems.PRECISION_STEAM_COMPONENT.get()))
                .save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("gtna_brass_reinforced_wooden_casing")
                .inputItems(TagPrefix.frameGt, GTMaterials.Wood)
                .inputItems(GTBlocks.TREATED_WOOD_PLANK.asItem(), 4)
                .inputItems(TagPrefix.screw, GTMaterials.Brass, 8)
                .inputItems(TagPrefix.plate, GTMaterials.Brass, 2)
                .circuitMeta(6)
                .outputItems(GTNABlocks.BRASS_REINFORCED_WOODEN_CASING.asItem())
                .EUt(16)
                .duration(50)
                .save(provider);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNABlocks.SOLAR_HEAT_COLLECTOR_PIPE_CASING.get())
                .pattern("AAA")
                .pattern("BBB")
                .pattern("CCC")
                .define('A', Items.TINTED_GLASS)
                .define('B', ChemicalHelper.get(TagPrefix.pipeTinyFluid, GTMaterials.Steel).getItem())
                .define('C', ChemicalHelper.get(TagPrefix.plateDouble, GTMaterials.Silver).getItem())
                .unlockedBy("has_tinted_glass",
                        InventoryChangeTrigger.TriggerInstance.hasItems(Items.TINTED_GLASS))
                .save(provider);

        GTRecipeTypes.ROCK_BREAKER_RECIPES.recipeBuilder("steam_cobble_gen")
                .circuitMeta(1)
                .outputItems(Items.COBBLESTONE)
                .duration(40)
                .EUt(30)
                .save(provider);

        // Circuito 2: Stone
        GTRecipeTypes.ROCK_BREAKER_RECIPES.recipeBuilder("steam_stone_gen")
                .circuitMeta(2)
                .outputItems(Items.STONE)
                .duration(40)
                .EUt(30)
                .save(provider);

        // Circuito 3: Obsidian (com Redstone)
        GTRecipeTypes.ROCK_BREAKER_RECIPES.recipeBuilder("steam_obsidian_gen")
                .circuitMeta(3)
                .inputItems(Items.REDSTONE)
                .outputItems(Items.OBSIDIAN)
                .duration(240)
                .EUt(30)
                .save(provider);

        // Circuito 4: Basalt (com Blue Ice - Não Consumido)
        GTRecipeTypes.ROCK_BREAKER_RECIPES.recipeBuilder("steam_basalt_gen")
                .circuitMeta(4)
                .chancedInput(Items.BLUE_ICE.getDefaultInstance(), 0, 0)
                .outputItems(Items.BASALT)
                .duration(40)
                .EUt(30)
                .save(provider);

        // Circuito 5: Cobbled Deepslate (com Magma Block - Não Consumido)
        GTRecipeTypes.ROCK_BREAKER_RECIPES.recipeBuilder("steam_deepslate_gen")
                .circuitMeta(5)
                .chancedInput(Items.MAGMA_BLOCK.getDefaultInstance(), 0, 0)
                .outputItems(Items.COBBLED_DEEPSLATE)
                .duration(40)
                .EUt(30)
                .save(provider);

        // Circuito 6: Netherrack (com Glowstone Dust)
        GTRecipeTypes.ROCK_BREAKER_RECIPES.recipeBuilder("steam_netherrack_gen")
                .circuitMeta(6)
                .inputItems(Items.GLOWSTONE_DUST)
                .outputItems(Items.NETHERRACK)
                .duration(40)
                .EUt(30)
                .save(provider);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNABlocks.STRONZE_WRAPPED_CASING.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', ChemicalHelper.get(TagPrefix.frameGt, GTNAMaterials.ClayCompound).getItem())
                .define('B', ChemicalHelper.get(TagPrefix.plate, GTNAMaterials.Stronze).getItem())
                .define('C', ChemicalHelper.get(TagPrefix.gear, GTNAMaterials.Stronze).getItem())
                .unlockedBy("has_stronze",
                        InventoryChangeTrigger.TriggerInstance
                                .hasItems(ChemicalHelper.get(TagPrefix.plate, GTNAMaterials.Stronze).getItem()))
                .save(provider);

        // 2. Hydraulic Assembler Casing
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNABlocks.HYDRAULIC_ASSEMBLER_CASING.get())
                .pattern("ABA")
                .pattern("CCC")
                .pattern("ABA")
                .define('A', ChemicalHelper.get(TagPrefix.pipeTinyFluid, GTNAMaterials.Stronze).getItem())
                .define('B', ChemicalHelper.get(TagPrefix.plate, GTNAMaterials.Breel).getItem())
                .define('C', GTNAItems.HYDRAULIC_ARM.get())
                .unlockedBy("has_breel",
                        InventoryChangeTrigger.TriggerInstance
                                .hasItems(ChemicalHelper.get(TagPrefix.plate, GTNAMaterials.Breel).getItem()))
                .save(provider);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNABlocks.BREEL_PLATED_CASING.get())
                .pattern("AAA")
                .pattern("BCB")
                .pattern("AAA")
                .define('A', ChemicalHelper.get(TagPrefix.pipeTinyFluid, GTNAMaterials.Breel).getItem())
                .define('B', ChemicalHelper.get(TagPrefix.plate, GTNAMaterials.Breel).getItem())
                .define('C', ChemicalHelper.get(TagPrefix.frameGt, GTNAMaterials.ClayCompound).getItem())
                .unlockedBy("has_breel",
                        InventoryChangeTrigger.TriggerInstance
                                .hasItems(ChemicalHelper.get(TagPrefix.plate, GTNAMaterials.Breel).getItem()))
                .save(provider);

        GTNARecipeType.SUPERHEATER_RECIPES.recipeBuilder("superheat_stone")
                .inputItems(Items.STONE)
                .outputFluids(Lava.getFluid(1000))
                .duration(40)
                .EUt(32)
                .save(provider);

        // Cobblestone
        GTNARecipeType.SUPERHEATER_RECIPES.recipeBuilder("superheat_cobble")
                .inputItems(Items.COBBLESTONE)
                .outputFluids(Lava.getFluid(1000))
                .duration(40)
                .EUt(32)
                .save(provider);

        // Granite
        GTNARecipeType.SUPERHEATER_RECIPES.recipeBuilder("superheat_granite")
                .inputItems(Items.GRANITE)
                .outputFluids(Lava.getFluid(1000))
                .duration(40)
                .EUt(32)
                .save(provider);

        // Diorite
        GTNARecipeType.SUPERHEATER_RECIPES.recipeBuilder("superheat_diorite")
                .inputItems(Items.DIORITE)
                .outputFluids(Lava.getFluid(1000))
                .duration(40)
                .EUt(32)
                .save(provider);

        GTRecipeTypes.ALLOY_SMELTER_RECIPES.recipeBuilder("borosilicate_gtna_glass_block_v2")
                .inputItems(ChemicalHelper.get(TagPrefix.block, GTMaterials.BorosilicateGlass))
                .inputItems(Items.GLASS)
                .outputItems(GTNABlocks.BOROSILICATE_GLASS_BLOCK.get().asItem())
                .duration(100)
                .EUt(120)
                .save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("oxidation_resistant_hastelloy_n_mechanical_casing")
                .inputItems(TagPrefix.frameGt, GTNAMaterials.HastelloyN)
                .inputItems(TagPrefix.plate, GTNAMaterials.HastelloyN, 6)
                .circuitMeta(6)
                .outputItems(GTNABlocks.OXIDATION_RESISTANT_HASTELLOY_N_MECHANICAL_CASING.asItem())
                .EUt(16)
                .duration(50)
                .save(provider);

        // GTOCore classified/Assembler.java "supercritical_turbine_casing": GTO's MarM200Steel is
        // GTNA's MarM200Steel, so the casing recipe is ported 1:1.
        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("supercritical_turbine_casing")
                .inputItems(GTBlocks.CASING_TUNGSTENSTEEL_TURBINE.asItem())
                .inputItems(TagPrefix.rod, GTNAMaterials.MarM200Steel, 2)
                .inputItems(TagPrefix.gear, GTNAMaterials.MarM200Steel)
                .inputItems(TagPrefix.plate, GTNAMaterials.MarM200Steel, 6)
                .circuitMeta(6)
                .outputItems(GTNABlocks.SUPERCRITICAL_TURBINE_CASING.asItem())
                .EUt(16)
                .duration(50)
                .save(provider);

        GTRecipeTypes.SIFTER_RECIPES.recipeBuilder("gtna_zirconia_ceramic_dust")
                .inputItems(TagPrefix.dust, GTNAMaterials.ZirconiumOxide, 2)
                .outputItems(TagPrefix.dust, GTNAMaterials.ZirconiaCeramic)
                .duration(120)
                .EUt(480)
                .save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES
                .recipeBuilder("gtna_zirconia_ceramic_high_strength_bending_resistance_mechanical_block")
                .inputItems(TagPrefix.frameGt, GTMaterials.TungstenSteel)
                .inputItems(TagPrefix.plate, GTMaterials.RedSteel, 2)
                .inputItems(GTNATagPrefix.flake, GTNAMaterials.ZirconiaCeramic, 16)
                .outputItems(GTNABlocks.ZIRCONIA_CERAMIC_HIGH_STRENGTH_BENDING_RESISTANCE_MECHANICAL_BLOCK.asItem())
                .duration(200)
                .EUt(30)
                .save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("gtna_high_strength_concrete")
                .inputItems(Blocks.REINFORCED_DEEPSLATE)
                .inputItems(TagPrefix.plate, GTMaterials.Steel, 2)
                .inputItems(TagPrefix.dust, GTMaterials.Concrete, 4)
                .inputFluids(GTMaterials.Concrete.getFluid(576))
                .outputItems(GTNABlocks.HIGH_STRENGTH_CONCRETE.asItem(), 4)
                .duration(160)
                .EUt(120)
                .save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("gtna_cobalt_oxide_ceramic_mechanical_block")
                .inputItems(TagPrefix.frameGt, GTMaterials.BlackSteel)
                .inputItems(TagPrefix.dust, GTNAMaterials.CobaltOxide, 8)
                .inputItems(TagPrefix.plate, GTMaterials.Cobalt, 4)
                .inputFluids(GTMaterials.Polytetrafluoroethylene.getFluid(288))
                .outputItems(GTNABlocks.COBALT_OXIDE_CERAMIC_STRONG_THERMALLY_CONDUCTIVE_MECHANICAL_BLOCK.asItem())
                .duration(200)
                .EUt(480)
                .save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("gtna_lithium_oxide_ceramic_mechanical_cube")
                .inputItems(TagPrefix.frameGt, GTNAMaterials.DarkSteel)
                .inputItems(TagPrefix.dust, GTNAMaterials.LithiumOxide, 8)
                .inputItems(TagPrefix.plate, GTMaterials.StainlessSteel, 4)
                .inputFluids(GTMaterials.Polybenzimidazole.getFluid(288))
                .outputItems(GTNABlocks.LITHIUM_OXIDE_CERAMIC_HEAT_RESISTANT_SHOCK_RESISTANT_MECHANICAL_CUBE.asItem())
                .duration(200)
                .EUt(1920)
                .save(provider);

        registerABSCasingRecipes(provider);
        registerComponentAssemblyCasingRecipes(provider);

        GTRecipeTypes.FLUID_SOLIDFICATION_RECIPES.recipeBuilder("gtna_naquadah_borosilicate_glass")
                .inputItems(GTNABlocks.BOROSILICATE_GLASS_BLOCK.asItem())
                .inputFluids(GTMaterials.Naquadah.getFluid(1152))
                .outputItems(GTNABlocks.NAQUADAH_BOROSILICATE_GLASS.asItem())
                .duration(200)
                .EUt(122880)
                .save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("gtna_process_machine_casing")
                .inputItems(GTBlocks.CASING_STEEL_SOLID.asItem())
                .inputItems(CustomTags.IV_CIRCUITS, 2)
                .inputItems(TagPrefix.wireGtQuadruple, GTNAMaterials.EndSteel)
                .inputItems(TagPrefix.plateDouble, GTMaterials.StainlessSteel, 2)
                .inputItems(TagPrefix.plateDouble, GTNAMaterials.MarM200Steel, 4)
                .inputFluids(GTNAMaterials.FallKing.getFluid(576))
                .outputItems(GTNABlocks.PROCESS_MACHINE_CASING.asItem())
                .EUt(7680)
                .duration(200)
                .save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("gtna_magtech_casing")
                .inputItems(TagPrefix.frameGt, GTMaterials.Tungsten)
                .inputItems(TagPrefix.plate, GTMaterials.Nichrome, 2)
                .inputItems(TagPrefix.plate, GTMaterials.IndiumTinBariumTitaniumCuprate, 4)
                .inputItems(TagPrefix.plate, GTMaterials.HSSS, 2)
                .circuitMeta(6)
                .outputItems(GTNABlocks.MAGTECH_CASING.asItem())
                .EUt(16)
                .duration(50)
                .save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("gtna_compressor_controller_casing")
                .inputItems(TagPrefix.frameGt, GTNAMaterials.AluminiumBronze)
                .inputItems(TagPrefix.plate, GTMaterials.Titanium, 4)
                .inputItems(TagPrefix.plateDouble, GTMaterials.Steel, 2)
                .circuitMeta(6)
                .outputItems(GTNABlocks.COMPRESSOR_CONTROLLER_CASING.asItem())
                .EUt(16)
                .duration(50)
                .save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("gtna_extreme_density_casing")
                .inputItems(TagPrefix.frameGt, GTNAMaterials.Trinaquadalloy)
                .inputItems(TagPrefix.plateDense, GTMaterials.NaquadahAlloy)
                .inputItems(TagPrefix.plate, GTNAMaterials.Trinaquadalloy, 6)
                .inputFluids(GTMaterials.Naquadria.getFluid(576))
                .outputItems(GTNABlocks.EXTREME_DENSITY_CASING.asItem())
                .EUt(120)
                .duration(200)
                .save(provider);

        GTNARecipeType.HYDRAULIC_MANUFACTURING.recipeBuilder("hyper_pressure_breel_casing")
                .inputItems(ChemicalHelper.get(TagPrefix.plate, GTNAMaterials.Breel).getItem(), 6)
                .inputItems(ChemicalHelper.get(TagPrefix.rod, GTMaterials.Beryllium).getItem(), 2)
                .inputItems(ChemicalHelper.get(TagPrefix.frameGt, GTMaterials.Beryllium).getItem(), 1)
                .outputItems(GTNABlocks.HYPER_PRESSURE_BREEL_CASING.asItem())
                .duration(40)
                .EUt(16)
                .save(provider);
        GTNARecipeType.HYDRAULIC_MANUFACTURING.recipeBuilder("steam_compact_pipe_casing_v2")
                .inputItems(GTNABlocks.BREEL_PIPE_CASING.asItem(), 1)
                .inputItems(ChemicalHelper.get(TagPrefix.pipeTinyFluid, GTNAMaterials.CompressedSteam).getItem(), 2)
                .inputItems(ChemicalHelper.get(TagPrefix.plate, GTNAMaterials.CompressedSteam).getItem(), 6)
                .outputItems(GTNABlocks.STEAM_COMPACT_PIPE_CASING.asItem())
                .duration(120)
                .EUt(24)
                .save(provider);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNABlocks.STEAM_COMPACT_PIPE_CASING.get())
                .pattern("PPP")
                .pattern("TCT")
                .pattern("PPP")
                .define('P', ChemicalHelper.get(TagPrefix.plate, GTNAMaterials.CompressedSteam).getItem())
                .define('T', ChemicalHelper.get(TagPrefix.pipeTinyFluid, GTNAMaterials.CompressedSteam).getItem())
                .define('C', GTNABlocks.BREEL_PIPE_CASING.get())
                .unlockedBy("has_breel_casing",
                        InventoryChangeTrigger.TriggerInstance.hasItems(GTNABlocks.BREEL_PIPE_CASING.get()))
                .save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("gtna_naquadah_alloy_casing")
                .inputItems(TagPrefix.frameGt, GTMaterials.NaquadahAlloy)
                .inputItems(TagPrefix.plate, GTMaterials.NaquadahAlloy, 6)
                .circuitMeta(6)
                .outputItems(GTNABlocks.NAQUADAH_ALLOY_CASING.asItem())
                .EUt(16)
                .duration(50)
                .save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("gtna_hyper_mechanical_casing")
                .inputItems(TagPrefix.frameGt, GTMaterials.NaquadahAlloy)
                .inputItems(TagPrefix.plate, GTMaterials.Naquadria, 6)
                .circuitMeta(6)
                .outputItems(GTNABlocks.HYPER_MECHANICAL_CASING.asItem())
                .EUt(16)
                .duration(50)
                .save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("gtna_rhenium_reinforced_energy_glass")
                .inputItems(GTBlocks.FUSION_GLASS.asItem(), 2)
                .inputItems(TagPrefix.plate, GTMaterials.Rhenium, 6)
                .outputItems(GTNABlocks.RHENIUM_REINFORCED_ENERGY_GLASS.asItem())
                .EUt(491520)
                .duration(1200)
                .save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("gtna_degenerate_rhenium_constrained_casing")
                .circuitMeta(6)
                .inputItems(TagPrefix.frameGt, GTMaterials.Rhenium)
                .inputItems(TagPrefix.plate, GTMaterials.Rhenium, 6)
                .outputItems(GTNABlocks.DEGENERATE_RHENIUM_CONSTRAINED_CASING.asItem())
                .EUt(491520)
                .duration(1200)
                .save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("gtna_graviton_field_constraint_casing")
                .inputItems(GTBlocks.HIGH_POWER_CASING.asItem())
                .inputItems(GTItems.FIELD_GENERATOR_UEV, 2)
                .inputItems(GTItems.SENSOR_UEV, 2)
                .inputItems(TagPrefix.plateDouble, GTMaterials.Naquadria, 4)
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(576))
                .outputItems(GTNABlocks.GRAVITON_FIELD_CONSTRAINT_CASING.asItem())
                .EUt(7864320)
                .duration(400)
                .save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("gtna_dyson_control_casing")
                .inputItems(GTNABlocks.NAQUADAH_ALLOY_CASING.asItem())
                .inputItems(GTItems.EMITTER_ZPM, 2)
                .inputItems(GTItems.SENSOR_ZPM, 2)
                .inputItems(CustomTags.UV_CIRCUITS, 2)
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(576))
                .outputItems(GTNABlocks.DYSON_CONTROL_CASING.asItem())
                .EUt(491520)
                .duration(300)
                .save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("gtna_dyson_receiver_casing")
                .inputItems(GTNABlocks.RHENIUM_REINFORCED_ENERGY_GLASS.asItem(), 2)
                .inputItems(GTNABlocks.NAQUADAH_ALLOY_CASING.asItem(), 2)
                .inputItems(GTItems.EMITTER_UV, 2)
                .inputItems(GTItems.SENSOR_UV, 2)
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(576))
                .outputItems(GTNABlocks.DYSON_RECEIVER_CASING.asItem())
                .EUt(1966080)
                .duration(400)
                .save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("gtna_antimatter_containment_casing")
                .inputItems(GTNABlocks.HYPER_MECHANICAL_CASING.asItem())
                .inputItems(GTItems.FIELD_GENERATOR_UHV, 2)
                .inputItems(GTNABlocks.RHENIUM_REINFORCED_ENERGY_GLASS.asItem(), 2)
                .inputItems(TagPrefix.plateDouble, GTMaterials.Neutronium, 4)
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(576))
                .outputItems(GTNABlocks.ANTIMATTER_CONTAINMENT_CASING.asItem())
                .EUt(1966080)
                .duration(400)
                .save(provider);

        // GTOCore classified/AssemblyLine.java:2291 "iridium_casing": the Component Assembly Line
        // shell. GTO's Tanmolyium plate is ported 1:1 (MaterialBuilder); the rest is GTCEu base.
        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder("gtna_iridium_casing")
                .inputItems(TagPrefix.frameGt, GTMaterials.Iridium, 2)
                .inputItems(GTBlocks.CASING_TITANIUM_TURBINE.asItem())
                .inputItems(GTBlocks.CASING_STAINLESS_TURBINE.asItem())
                .inputItems(TagPrefix.foil, GTMaterials.Osmiridium, 4)
                .inputItems(TagPrefix.foil, GTMaterials.Iridium, 4)
                .inputItems(TagPrefix.plate, GTNAMaterials.Tanmolyium)
                .inputItems(TagPrefix.plateDouble, GTMaterials.Iridium)
                .inputItems(TagPrefix.plateDouble, GTMaterials.Osmiridium)
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(1440))
                .inputFluids(GTMaterials.Iridium.getFluid(576))
                .outputItems(GTNABlocks.IRIDIUM_CASING.asItem(), 2)
                .EUt(30720)
                .duration(200)
                .stationResearch(b -> b
                        .researchStack(ChemicalHelper.get(TagPrefix.block, GTMaterials.Osmiridium))
                        .CWUt(32)
                        .EUt(30720))
                .save(provider);

        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder("gtna_hollow_casing")
                .inputItems(GTBlocks.HIGH_POWER_CASING.asItem(), 2)
                .inputItems(GTNABlocks.NAQUADAH_ALLOY_CASING.asItem(), 2)
                .inputItems(GTNABlocks.RHENIUM_REINFORCED_ENERGY_GLASS.asItem(), 2)
                .inputItems(GTItems.FIELD_GENERATOR_UV, 4)
                .inputItems(CustomTags.UV_CIRCUITS, 2)
                .inputItems(TagPrefix.plateDouble, GTMaterials.Neutronium, 4)
                .inputFluids(GTMaterials.Europium.getFluid(2304))
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(2304))
                .outputItems(GTNABlocks.HOLLOW_CASING.asItem(), 2)
                .EUt(491520)
                .duration(400)
                .stationResearch(b -> b
                        .researchStack(GTNABlocks.NAQUADAH_ALLOY_CASING.asStack())
                        .CWUt(128)
                        .EUt(491520))
                .save(provider);

        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder("gtna_dyson_control_toroid")
                .inputItems(TagPrefix.frameGt, GTMaterials.Neutronium)
                .inputItems(GTItems.EMITTER_UIV, 4)
                .inputItems(GTItems.ELECTRIC_PUMP_UIV, 2)
                .inputItems(CustomTags.UIV_CIRCUITS, 2)
                .inputItems(GTNABlocks.DYSON_CONTROL_CASING.asItem(), 2)
                .inputItems(TagPrefix.foil, GTMaterials.Neutronium, 24)
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(2000))
                .inputFluids(GTMaterials.Naquadria.getFluid(1296))
                .inputFluids(GTMaterials.Europium.getFluid(1296))
                .outputItems(GTNABlocks.DYSON_CONTROL_TOROID.asItem())
                .EUt(31457280)
                .duration(800)
                .stationResearch(b -> b
                        .researchStack(GTNABlocks.DYSON_CONTROL_CASING.asStack())
                        .CWUt(512)
                        .EUt(31457280))
                .save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("gtna_annihilate_core")
                .inputItems(TagPrefix.frameGt, GTMaterials.Neutronium)
                .inputItems(GTItems.GRAVI_STAR)
                .inputItems(GTItems.FIELD_GENERATOR_UXV)
                .inputItems(GTItems.EMITTER_UXV)
                .inputItems(GTItems.SENSOR_UXV)
                .inputItems(CustomTags.UXV_CIRCUITS, 2)
                .inputItems(TagPrefix.plateDouble, GTMaterials.Neutronium, 4)
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(1296))
                .outputItems(GTNABlocks.ANNIHILATE_CORE.asItem())
                .EUt(125829120)
                .duration(400)
                .save(provider);

        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder("gtna_dimensionally_transcendent_casing")
                .inputItems(GTBlocks.HIGH_POWER_CASING.asItem(), 4)
                .inputItems(GTNABlocks.HOLLOW_CASING.asItem(), 4)
                .inputItems(GTNABlocks.RHENIUM_REINFORCED_ENERGY_GLASS.asItem(), 4)
                .inputItems(GTItems.FIELD_GENERATOR_UHV, 4)
                .inputItems(GTItems.SENSOR_UHV, 4)
                .inputItems(CustomTags.UHV_CIRCUITS, 4)
                .inputItems(TagPrefix.plateDouble, GTMaterials.Neutronium, 8)
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(2304))
                .inputFluids(GTMaterials.Europium.getFluid(2304))
                .inputFluids(GTMaterials.Naquadria.getFluid(2304))
                .outputItems(GTNABlocks.DIMENSIONALLY_TRANSCENDENT_CASING.asItem(), 2)
                .EUt(31457280)
                .duration(800)
                .stationResearch(b -> b
                        .researchStack(GTNABlocks.HOLLOW_CASING.asStack())
                        .CWUt(512)
                        .EUt(31457280))
                .save(provider);

        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder("gtna_dimension_injection_casing")
                .inputItems(GTNABlocks.DIMENSIONALLY_TRANSCENDENT_CASING.asItem(), 2)
                .inputItems(GTNABlocks.DYSON_RECEIVER_CASING.asItem(), 2)
                .inputItems(GTItems.FIELD_GENERATOR_UEV, 2)
                .inputItems(GTItems.SENSOR_UEV, 2)
                .inputItems(CustomTags.UEV_CIRCUITS, 2)
                .inputItems(TagPrefix.foil, GTMaterials.Rhenium, 16)
                .inputItems(TagPrefix.plateDouble, GTMaterials.Naquadria, 8)
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(2304))
                .inputFluids(GTMaterials.Europium.getFluid(1152))
                .inputFluids(GTMaterials.Neutronium.getFluid(1152))
                .outputItems(GTNABlocks.DIMENSION_INJECTION_CASING.asItem(), 2)
                .EUt(125829120)
                .duration(600)
                .stationResearch(b -> b
                        .researchStack(GTNABlocks.DIMENSIONALLY_TRANSCENDENT_CASING.asStack())
                        .CWUt(1024)
                        .EUt(125829120))
                .save(provider);

        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder("gtna_dimensional_bridge_casing")
                .inputItems(GTNABlocks.DIMENSIONALLY_TRANSCENDENT_CASING.asItem())
                .inputItems(GTNABlocks.DIMENSION_INJECTION_CASING.asItem(), 2)
                .inputItems(GTItems.FIELD_GENERATOR_UIV, 2)
                .inputItems(GTItems.EMITTER_UIV, 2)
                .inputItems(CustomTags.UIV_CIRCUITS, 2)
                .inputItems(TagPrefix.plateDouble, GTMaterials.Neutronium, 8)
                .inputItems(TagPrefix.plateDouble, GTMaterials.NaquadahAlloy, 8)
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(2304))
                .inputFluids(GTMaterials.Europium.getFluid(2304))
                .inputFluids(GTMaterials.Naquadria.getFluid(2304))
                .outputItems(GTNABlocks.DIMENSIONAL_BRIDGE_CASING.asItem())
                .EUt(503316480)
                .duration(800)
                .stationResearch(b -> b
                        .researchStack(GTNABlocks.DIMENSION_INJECTION_CASING.asStack())
                        .CWUt(2048)
                        .EUt(503316480))
                .save(provider);

        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder("gtna_dimensional_stability_casing")
                .inputItems(GTNABlocks.DIMENSIONAL_BRIDGE_CASING.asItem())
                .inputItems(GTNABlocks.DYSON_CONTROL_CASING.asItem(), 2)
                .inputItems(GTNABlocks.DYSON_CONTROL_TOROID.asItem(), 2)
                .inputItems(GTItems.FIELD_GENERATOR_UXV, 2)
                .inputItems(GTItems.ELECTRIC_PUMP_UXV, 2)
                .inputItems(CustomTags.UXV_CIRCUITS, 2)
                .inputItems(TagPrefix.plateDouble, GTMaterials.Neutronium, 8)
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(2304))
                .inputFluids(GTMaterials.Europium.getFluid(2304))
                .inputFluids(GTMaterials.Neutronium.getFluid(2304))
                .outputItems(GTNABlocks.DIMENSIONAL_STABILITY_CASING.asItem())
                .EUt(2013265920)
                .duration(1000)
                .stationResearch(b -> b
                        .researchStack(GTNABlocks.DIMENSIONAL_BRIDGE_CASING.asStack())
                        .CWUt(4096)
                        .EUt(2013265920))
                .save(provider);

        if (GTNAMachines.ARTIFICIAL_STAR != null) {
            GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder("gtna_spacetime_compression_field_generator")
                    .inputItems(GTNABlocks.DIMENSIONALLY_TRANSCENDENT_CASING.asItem())
                    .inputItems(GTNABlocks.DIMENSIONAL_STABILITY_CASING.asItem())
                    .inputItems(GTNABlocks.DIMENSIONAL_BRIDGE_CASING.asItem())
                    .inputItems(GTNABlocks.ANNIHILATE_CORE.asItem())
                    .inputItems(GTNAMachines.ARTIFICIAL_STAR.asStack().getItem())
                    .inputItems(GTItems.FIELD_GENERATOR_OpV, 2)
                    .inputItems(CustomTags.OpV_CIRCUITS, 2)
                    .inputItems(TagPrefix.plateDouble, GTMaterials.Neutronium, 8)
                    .inputFluids(GTMaterials.SolderingAlloy.getFluid(4608))
                    .inputFluids(GTMaterials.Europium.getFluid(4608))
                    .inputFluids(GTMaterials.Naquadria.getFluid(4608))
                    .outputItems(GTNABlocks.SPACETIME_COMPRESSION_FIELD_GENERATOR.asItem())
                    .EUt(8053063680L)
                    .duration(1200)
                    .stationResearch(b -> b
                            .researchStack(GTNABlocks.DIMENSIONAL_STABILITY_CASING.asStack())
                            .CWUt(8192)
                            .EUt(8053063680L))
                    .save(provider);
        }
    }

    @SuppressWarnings("unchecked")
    private static void registerABSCasingRecipes(Consumer<FinishedRecipe> provider) {
        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("abs_white_casing")
                .inputItems(TagPrefix.frameGt, GTMaterials.Europium)
                .inputItems(TagPrefix.plate, GTNAMaterials.Abs, 6)
                .inputItems(TagPrefix.foil, GTNAMaterials.Polystyrene, 4)
                .inputFluids(GTMaterials.Polyethylene.getFluid(576))
                .outputItems(GTNABlocks.ABS_WHITE_CASING.asItem())
                .duration(100)
                .EUt(30)
                .save(provider);

        BlockEntry<Block>[] absBlocks = new BlockEntry[] {
                GTNABlocks.ABS_WHITE_CASING,
                GTNABlocks.ABS_ORANGE_CASING,
                GTNABlocks.ABS_MAGENTA_CASING,
                GTNABlocks.ABS_LIGHT_BULL_CASING,
                GTNABlocks.ABS_YELLOW_CASING,
                GTNABlocks.ABS_LIME_CASING,
                GTNABlocks.ABS_PINK_CASING,
                GTNABlocks.ABS_GREY_CASING,
                GTNABlocks.ABS_LIGHT_GREY_CASING,
                GTNABlocks.ABS_CYAN_CASING,
                GTNABlocks.ABS_PURPLE_CASING,
                GTNABlocks.ABS_BLUE_CASING,
                GTNABlocks.ABS_BROWN_CASING,
                GTNABlocks.ABS_GREEN_CASING,
                GTNABlocks.ABS_RED_CASING,
                GTNABlocks.ABS_BLACK_CASING
        };

        for (int i = 0; i < CHEMICAL_DYES.length; i++) {
            DyeColor color = DyeColor.values()[i];
            if (color == DyeColor.WHITE) {
                continue;
            }
            GTRecipeTypes.CHEMICAL_BATH_RECIPES.recipeBuilder("abs_" + color.getName())
                    .inputItems(GTNABlocks.ABS_WHITE_CASING.asItem())
                    .inputFluids(CHEMICAL_DYES[i], 144)
                    .outputItems(absBlocks[i].asItem())
                    .duration(200)
                    .EUt(7)
                    .category(GTRecipeCategories.CHEM_DYES)
                    .save(provider);
        }
    }

    /**
     * Component Assembler extension blocks and the {@code component_assembly_line} casing family.
     *
     * <p>
     * The casing production for LV–IV follows GTOCore's {@code Assembler} recipes 1:1 (the same
     * frame/plateDouble/component/solder pattern already used for the base assembler family). The
     * LuV–UV casings are GTOCore's {@code AssemblyLine} recipes with the research station on the
     * previous tier of the same family; their GTO-only solder fluids are substituted by obtainable
     * GTNA/GTCEu equivalents (Pikyonium → Trinaquadalloy; ArtheriumTin →
     * EnrichedNaquadahTriniumEuropiumDuranide; AbyssalAlloy → RutheniumTriniumAmericiumNeutronate),
     * documented in the ledger G-0114.
     *
     * <p>
     * The four extension control/transmission casings have no portable GTOCore recipe: the machining
     * and energy control casings are built in GTO's Precision Assembler (an excluded machine) from
     * GTO-only modules and composites, and the power transmission casing uses GTO-only composite
     * materials. Their GTNA routes keep GTOCore's shape (frame + double plate + optical/electrical
     * parts + circuit + solder) with GTNA/GTCEu materials and are recorded in the ledger.
     */
    private static void registerComponentAssemblyCasingRecipes(Consumer<FinishedRecipe> provider) {
        registerComponentAssemblyLineStructureCasings(provider);
        componentCasing(provider, "component_assembly_line_casing_lv", GTValues.LV, GTMaterials.Steel,
                GTNABlocks.COMPONENT_ASSEMBLY_LINE_CASING_LV, 288);
        componentCasing(provider, "component_assembly_line_casing_mv", GTValues.MV, GTMaterials.Aluminium,
                GTNABlocks.COMPONENT_ASSEMBLY_LINE_CASING_MV, 432);
        componentCasing(provider, "component_assembly_line_casing_hv", GTValues.HV, GTMaterials.StainlessSteel,
                GTNABlocks.COMPONENT_ASSEMBLY_LINE_CASING_HV, 576);
        componentCasing(provider, "component_assembly_line_casing_ev", GTValues.EV, GTMaterials.Titanium,
                GTNABlocks.COMPONENT_ASSEMBLY_LINE_CASING_EV, 720);
        componentCasing(provider, "component_assembly_line_casing_iv", GTValues.IV, GTMaterials.TungstenSteel,
                GTNABlocks.COMPONENT_ASSEMBLY_LINE_CASING_IV, 864);

        // GTOCore AssemblyLine.java "component_assembly_line_casing_luv": GTCEu materials plus GTNA's
        // ported Indalloy140, with the research station on the IV casing of the same family. The
        // Component Assembler's own LuV casing (the extension family) is built the same way but
        // researches the component-assembly IV casing.
        assemblyLineCasing(provider, GTNABlocks.COMPONENT_ASSEMBLY_CASING_LUV,
                GTNABlocks.COMPONENT_ASSEMBLY_CASING_IV, "component_assembly_casing_luv");
        assemblyLineCasing(provider, GTNABlocks.COMPONENT_ASSEMBLY_LINE_CASING_LUV,
                GTNABlocks.COMPONENT_ASSEMBLY_LINE_CASING_IV, "component_assembly_line_casing_luv");

        // GTOCore AssemblyLine.java "component_assembly_line_casing_zpm"/"_uv": the top two tiers of
        // the GTO component assembly line. Their GTO-only solder fluids are substituted by obtainable
        // GTNA/GTCEu equivalents of the same tier (documented in the ledger G-0114):
        // Pikyonium -> GTNA Trinaquadalloy (ZPM alloy), ArtheriumTin -> EnrichedNaquadahTriniumEuropiumDuranide
        // and AbyssalAlloy -> RutheniumTriniumAmericiumNeutronate (UV/UHV superconductors, nearly the
        // same GTO blast temperatures). Research stations follow GTO: LuV casing for ZPM, ZPM for UV.
        assemblyLineCasingZpm(provider, GTNABlocks.COMPONENT_ASSEMBLY_CASING_ZPM,
                GTNABlocks.COMPONENT_ASSEMBLY_CASING_LUV, "component_assembly_casing_zpm");
        assemblyLineCasingZpm(provider, GTNABlocks.COMPONENT_ASSEMBLY_LINE_CASING_ZPM,
                GTNABlocks.COMPONENT_ASSEMBLY_LINE_CASING_LUV, "component_assembly_line_casing_zpm");
        assemblyLineCasingUv(provider, GTNABlocks.COMPONENT_ASSEMBLY_CASING_UV,
                GTNABlocks.COMPONENT_ASSEMBLY_CASING_ZPM, "component_assembly_casing_uv");
        assemblyLineCasingUv(provider, GTNABlocks.COMPONENT_ASSEMBLY_LINE_CASING_UV,
                GTNABlocks.COMPONENT_ASSEMBLY_LINE_CASING_ZPM, "component_assembly_line_casing_uv");

        // GTOCore AssemblerA "THREE_PROOF_COMPUTER_CASING": GTNA substitutes GTO's
        // StainlessSteelJbk75 frame and TungstenAlloyYG10 plates with TungstenSteel/TungstenCarbide;
        // the optical pipe is GTCEu's normal optical pipe.
        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("three_proof_computer_casing")
                .inputItems(TagPrefix.frameGt, GTMaterials.TungstenSteel)
                .inputItems(TagPrefix.plateDouble, GTMaterials.TungstenCarbide, 6)
                .inputItems(TagPrefix.wireFine, GTMaterials.Platinum, 64)
                .inputItems(TagPrefix.wireFine, GTMaterials.Silver, 64)
                .inputItems(GTBlocks.OPTICAL_PIPES[0].asItem(), 2)
                .inputItems(CustomTags.LuV_CIRCUITS)
                .circuitMeta(6)
                .outputItems(GTNABlocks.THREE_PROOF_COMPUTER_CASING.asItem())
                .EUt(30000)
                .duration(200)
                .save(provider);

        // GTOCore PrecisionAssembler "machining_control_casing_mk2" (excluded machine, GTO-only
        // Machining Control Module MK II and composites): GTNA keeps the frame + composite double
        // plate + optical pipe shape and the solder/YttriumBariumCuprate fluids.
        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("machining_control_casing_mk2")
                .inputItems(TagPrefix.frameGt, GTMaterials.TungstenSteel)
                .inputItems(TagPrefix.plateDouble, GTNAMaterials.CarbonFiberPolyphenyleneSulfideComposite, 6)
                .inputItems(GTBlocks.OPTICAL_PIPES[0].asItem(), 16)
                .inputItems(CustomTags.LuV_CIRCUITS, 2)
                .inputFluids(GTMaterials.YttriumBariumCuprate.getFluid(576))
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(288))
                .circuitMeta(2)
                .outputItems(GTNABlocks.MACHINING_CONTROL_CASING_MK2.asItem())
                .EUt(30720)
                .duration(400)
                .save(provider);

        // GTOCore PrecisionAssembler "energy_control_casing_mk2" (excluded machine, GTO-only Energy
        // Control Module MK II): GTNA keeps the UV voltage coil, frame, composite double plate and
        // YttriumBariumCuprate/solder fluids.
        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("energy_control_casing_mk2")
                .inputItems(GTItems.VOLTAGE_COIL_UV, 2)
                .inputItems(TagPrefix.frameGt, GTMaterials.TungstenSteel)
                .inputItems(TagPrefix.plateDouble, GTNAMaterials.CarbonFiberPolyphenyleneSulfideComposite, 6)
                .inputFluids(GTMaterials.YttriumBariumCuprate.getFluid(576))
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(288))
                .circuitMeta(2)
                .outputItems(GTNABlocks.ENERGY_CONTROL_CASING_MK2.asItem())
                .EUt(30720)
                .duration(400)
                .save(provider);

        // GTOCore AssemblerA "electric_power_transmission_casing": GTNA substitutes GTO's composites
        // with the ported carbon-fiber composite and copper foil, keeping the two GTCEu wire inputs
        // (SamariumIronArsenicOxide and UraniumTriplatinum) and the IV sensor.
        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("electric_power_transmission_casing")
                .inputItems(TagPrefix.frameGt, GTNAMaterials.CarbonFiberPolyphenyleneSulfideComposite)
                .inputItems(TagPrefix.plateDouble, GTMaterials.Aluminium, 6)
                .inputItems(TagPrefix.foil, GTMaterials.Copper, 10)
                .inputItems(TagPrefix.wireGtSingle, GTMaterials.SamariumIronArsenicOxide, 6)
                .inputItems(TagPrefix.wireGtSingle, GTMaterials.UraniumTriplatinum, 6)
                .inputItems(GTItems.SENSOR_IV)
                .circuitMeta(6)
                .outputItems(GTNABlocks.ELECTRIC_POWER_TRANSMISSION_CASING.asItem())
                .EUt(16)
                .duration(50)
                .save(provider);

        // GTOCore Assembler "titanium_nitride_ceramic_impact_resistant_mechanical_block": 1:1 with the
        // locally created TitaniumNitrideCeramic and GTNA's flake prefix.
        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("titanium_nitride_ceramic_impact_resistant_mechanical_block")
                .inputItems(TagPrefix.frameGt, GTMaterials.Titanium)
                .inputItems(TagPrefix.plate, GTMaterials.BlueSteel, 2)
                .inputItems(GTNATagPrefix.flake, GTNAMaterials.TitaniumNitrideCeramic, 16)
                .outputItems(GTNABlocks.TITANIUM_NITRIDE_CERAMIC_IMPACT_RESISTANT_MECHANICAL_BLOCK.asItem())
                .EUt(30)
                .duration(200)
                .save(provider);

        // GTOCore's nitridation chain for TitaniumNitrideCeramic is not ported; GTNA reacts Titanium
        // dust with Nitrogen in the Mixer instead (recorded in the ledger).
        GTRecipeTypes.MIXER_RECIPES.recipeBuilder("gtna_titanium_nitride_ceramic_dust")
                .inputItems(TagPrefix.dust, GTMaterials.Titanium)
                .inputFluids(GTMaterials.Nitrogen.getFluid(1000))
                .outputItems(TagPrefix.dust, GTNAMaterials.TitaniumNitrideCeramic)
                .duration(200)
                .EUt(480)
                .save(provider);
    }

    /** Structure materials retained from GTO; unavailable GTO-only inputs use existing GTNA materials. */
    private static void registerComponentAssemblyLineStructureCasings(Consumer<FinishedRecipe> provider) {
        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("molecular_casing")
                .inputItems(GTBlocks.HIGH_POWER_CASING.asItem())
                .inputItems(TagPrefix.plateDouble, GTMaterials.BatteryAlloy, 4)
                .inputItems(GTItems.EMITTER_IV)
                .inputItems(TagPrefix.ring, GTMaterials.Darmstadtium, 24)
                .inputItems(TagPrefix.foil, GTMaterials.Tungsten, 12)
                .inputItems(TagPrefix.foil, GTMaterials.Ruridit, 12)
                .inputItems(TagPrefix.foil, GTMaterials.TungstenSteel, 24)
                .inputItems(TagPrefix.plate, GTMaterials.Rhodium, 6)
                .inputItems(TagPrefix.plateDouble, GTMaterials.Ruthenium, 4)
                .inputFluids(GTMaterials.NiobiumNitride, 864)
                .outputItems(GTNABlocks.MOLECULAR_CASING.asItem())
                .EUt(491520).duration(400).save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("boron_carbide_ceramic_radiation_resistant_mechanical_cube")
                .inputItems(TagPrefix.frameGt, GTMaterials.Ruridit)
                .inputItems(TagPrefix.plate, GTMaterials.TitaniumTungstenCarbide, 2)
                .inputItems(TagPrefix.dust, GTMaterials.Boron, 8)
                .inputItems(TagPrefix.dust, GTMaterials.Carbon, 8)
                .outputItems(GTNABlocks.BORON_CARBIDE_CERAMIC_RADIATION_RESISTANT_MECHANICAL_CUBE.asItem())
                .EUt(30).duration(200).save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("precision_processing_mechanical_casing")
                .inputItems(TagPrefix.frameGt, GTNAMaterials.HastelloyN)
                .inputItems(TagPrefix.ingot, GTNAMaterials.HastelloyN, 6)
                .inputItems(TagPrefix.gearSmall, GTMaterials.RhodiumPlatedPalladium, 4)
                .inputItems(TagPrefix.gearSmall, GTMaterials.HSSS, 4)
                .inputItems(TagPrefix.gearSmall, GTMaterials.Osmiridium, 4)
                .inputItems(TagPrefix.rod, GTNAMaterials.HastelloyN, 6)
                .inputFluids(GTMaterials.Rhodium, 1152)
                .outputItems(GTNABlocks.PRECISION_PROCESSING_MECHANICAL_CASING.asItem())
                .EUt(480).duration(200).save(provider);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNABlocks.ADVANCED_ASSEMBLY_LINE_UNIT.get())
                .pattern("ABA").pattern("CDC").pattern("ABA")
                .define('A', ChemicalHelper.get(TagPrefix.plate, GTMaterials.HSSG).getItem())
                .define('B', ChemicalHelper.get(TagPrefix.gear, GTMaterials.Rhodium).getItem())
                .define('C', CustomTags.UV_CIRCUITS)
                .define('D', GTBlocks.CASING_ASSEMBLY_LINE.asItem())
                .unlockedBy("has_assembly_line", InventoryChangeTrigger.TriggerInstance
                        .hasItems(GTBlocks.CASING_ASSEMBLY_LINE.asItem()))
                .save(provider, GTNACORE.id("advanced_assembly_line_unit"));

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("chemical_corrosion_resistant_pipe_casing")
                .inputItems(TagPrefix.frameGt, GTMaterials.StainlessSteel)
                .inputItems(TagPrefix.pipeNormalFluid, GTMaterials.StainlessSteel, 4)
                .inputItems(TagPrefix.plate, GTMaterials.Polytetrafluoroethylene, 4)
                .inputItems(TagPrefix.plate, GTMaterials.StainlessSteel, 4)
                .inputFluids(GTMaterials.Polytetrafluoroethylene, 576)
                .outputItems(GTNABlocks.CHEMICAL_CORROSION_RESISTANT_PIPE_CASING.asItem())
                .EUt(480).duration(200).save(provider);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNABlocks.MACHINE_CASING_CIRCUIT_ASSEMBLY_LINE.get())
                .pattern("ABA").pattern("CDC").pattern("ABA")
                .define('A', ChemicalHelper.get(TagPrefix.plate, GTMaterials.Ruridit).getItem())
                .define('B', ChemicalHelper.get(TagPrefix.gear, GTMaterials.HSSG).getItem())
                .define('C', GTItems.ROBOT_ARM_LuV.get())
                .define('D', ChemicalHelper.get(TagPrefix.frameGt, GTMaterials.Ruridit).getItem())
                .unlockedBy("has_luv_robot_arm", InventoryChangeTrigger.TriggerInstance
                        .hasItems(GTItems.ROBOT_ARM_LuV.get()))
                .save(provider, GTNACORE.id("machine_casing_circuit_assembly_line"));

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("spacetime_assembly_line_unit")
                .inputItems(GTNABlocks.ADVANCED_ASSEMBLY_LINE_UNIT.asItem(), 2)
                .inputItems(GTNABlocks.MACHINE_CASING_CIRCUIT_ASSEMBLY_LINE.asItem(), 2)
                .inputItems(TagPrefix.plate, GTMaterials.NaquadahAlloy, 8)
                .inputItems(CustomTags.UV_CIRCUITS, 4)
                .inputFluids(GTMaterials.SolderingAlloy, 1152)
                .outputItems(GTNABlocks.SPACETIME_ASSEMBLY_LINE_UNIT.asItem())
                .EUt(491520).duration(400).save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("pressure_containment_casing")
                .inputItems(TagPrefix.frameGt, GTMaterials.Polytetrafluoroethylene)
                .inputItems(TagPrefix.plate, GTMaterials.DamascusSteel, 2)
                .inputFluids(GTMaterials.StainlessSteel, 1152)
                .outputItems(GTNABlocks.PRESSURE_CONTAINMENT_CASING.asItem())
                .EUt(120).duration(100).save(provider);
    }

    /** GTOCore's {@code component_assembly_line_casing_*} Assembler recipe, per tier. */
    private static void componentCasing(Consumer<FinishedRecipe> provider, String name, int tier, Material material,
                                        BlockEntry<Block> output, int solder) {
        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(name)
                .inputItems(TagPrefix.frameGt, material)
                .inputItems(TagPrefix.plateDouble, material, 16)
                .inputItems(component("field_generator", tier), 2)
                .inputItems(component("electric_pump", tier), 4)
                .inputItems(component("robot_arm", tier), 4)
                .inputItems(component("sensor", tier), 4)
                .inputItems(component("conveyor_module", tier), 6)
                .inputItems(TagPrefix.gear, material, 4)
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(solder))
                .outputItems(output.asItem())
                .EUt(GTValues.VA[tier])
                .duration(320)
                .save(provider);
    }

    /** GTOCore's {@code component_assembly_line_casing_luv} Assembly Line recipe. */
    private static void assemblyLineCasing(Consumer<FinishedRecipe> provider,
                                           BlockEntry<Block> output, BlockEntry<Block> researchStack,
                                           String name) {
        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(name)
                .inputItems(TagPrefix.frameGt, GTMaterials.Europium)
                .inputItems(TagPrefix.plateDense, GTMaterials.RhodiumPlatedPalladium, 6)
                .inputItems(GTItems.FIELD_GENERATOR_LuV, 4)
                .inputItems(GTItems.ELECTRIC_PUMP_LuV, 6)
                .inputItems(GTItems.ROBOT_ARM_LuV, 8)
                .inputItems(GTItems.SENSOR_LuV, 10)
                .inputItems(GTItems.CONVEYOR_MODULE_LuV, 16)
                .inputItems(TagPrefix.gear, GTMaterials.Osmiridium, 4)
                .inputItems(TagPrefix.gearSmall, GTMaterials.RhodiumPlatedPalladium, 16)
                .inputItems(TagPrefix.wireGtOctal, GTMaterials.IndiumTinBariumTitaniumCuprate, 4)
                .inputItems(CustomTags.LuV_CIRCUITS, 8)
                .inputItems(CustomTags.IV_CIRCUITS, 16)
                .inputFluids(GTNAMaterials.Indalloy140.getFluid(3456))
                .inputFluids(GTMaterials.Zeron100.getFluid(1728))
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(1152))
                .inputFluids(GTMaterials.Lubricant.getFluid(4000))
                .outputItems(output.asItem())
                .EUt(30720)
                .duration(600)
                .stationResearch(b -> b
                        .researchStack(researchStack.asStack())
                        .CWUt(128)
                        .EUt(30720))
                .save(provider);
    }

    /**
     * GTOCore's {@code component_assembly_line_casing_zpm} Assembly Line recipe. GTO's Pikyonium
     * solder is substituted by GTNA's obtainable ZPM alloy {@code Trinaquadalloy} (same 2016 mB);
     * Indalloy140, Neutronium and Lubricant are ported 1:1. The station research targets the LuV
     * casing of the same family, like GTO.
     */
    private static void assemblyLineCasingZpm(Consumer<FinishedRecipe> provider,
                                              BlockEntry<Block> output, BlockEntry<Block> researchStack,
                                              String name) {
        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(name)
                .inputItems(TagPrefix.frameGt, GTMaterials.NaquadahAlloy)
                .inputItems(TagPrefix.plateDense, GTMaterials.NaquadahAlloy, 6)
                .inputItems(GTItems.FIELD_GENERATOR_ZPM, 4)
                .inputItems(GTItems.ELECTRIC_PUMP_ZPM, 6)
                .inputItems(GTItems.ROBOT_ARM_ZPM, 8)
                .inputItems(GTItems.SENSOR_ZPM, 10)
                .inputItems(GTItems.CONVEYOR_MODULE_ZPM, 16)
                .inputItems(TagPrefix.gear, GTMaterials.NaquadahAlloy, 4)
                .inputItems(TagPrefix.gearSmall, GTMaterials.NaquadahAlloy, 16)
                .inputItems(TagPrefix.wireGtOctal, GTMaterials.IndiumTinBariumTitaniumCuprate, 4)
                .inputItems(CustomTags.ZPM_CIRCUITS, 8)
                .inputItems(CustomTags.LuV_CIRCUITS, 16)
                .inputFluids(GTNAMaterials.Indalloy140.getFluid(4032))
                .inputFluids(GTNAMaterials.Trinaquadalloy.getFluid(2016))
                .inputFluids(GTMaterials.Neutronium.getFluid(1008))
                .inputFluids(GTMaterials.Lubricant.getFluid(5000))
                .outputItems(output.asItem())
                .EUt(122880)
                .duration(600)
                .stationResearch(b -> b
                        .researchStack(researchStack.asStack())
                        .CWUt(192)
                        .EUt(122880))
                .save(provider);
    }

    /**
     * GTOCore's {@code component_assembly_line_casing_uv} Assembly Line recipe. GTO's ArtheriumTin
     * and AbyssalAlloy solders are substituted by the closest obtainable GTCEu superconductors of
     * nearly the same blast temperature (EnrichedNaquadahTriniumEuropiumDuranide 9900 K for
     * ArtheriumTin's 9800 K, RutheniumTriniumAmericiumNeutronate 10800 K for AbyssalAlloy's
     * 10800 K), keeping the original 2304/1152 mB. GTO's {@code plateDouble Tritanium} is kept
     * (GTCEu generates double plates for any material with the plate flag). The station research
     * targets the ZPM casing of the same family, like GTO.
     */
    private static void assemblyLineCasingUv(Consumer<FinishedRecipe> provider,
                                             BlockEntry<Block> output, BlockEntry<Block> researchStack,
                                             String name) {
        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(name)
                .inputItems(TagPrefix.frameGt, GTMaterials.Tritanium)
                .inputItems(TagPrefix.plateDouble, GTMaterials.Tritanium, 24)
                .inputItems(GTItems.FIELD_GENERATOR_UV, 4)
                .inputItems(GTItems.ELECTRIC_PUMP_UV, 6)
                .inputItems(GTItems.ROBOT_ARM_UV, 8)
                .inputItems(GTItems.SENSOR_UV, 10)
                .inputItems(GTItems.CONVEYOR_MODULE_UV, 16)
                .inputItems(TagPrefix.gear, GTMaterials.Tritanium, 4)
                .inputItems(TagPrefix.gearSmall, GTMaterials.Tritanium, 16)
                .inputItems(TagPrefix.wireGtOctal, GTMaterials.IndiumTinBariumTitaniumCuprate, 4)
                .inputItems(CustomTags.UV_CIRCUITS, 8)
                .inputItems(CustomTags.ZPM_CIRCUITS, 16)
                .inputFluids(GTNAMaterials.Indalloy140.getFluid(4608))
                .inputFluids(GTMaterials.EnrichedNaquadahTriniumEuropiumDuranide.getFluid(2304))
                .inputFluids(GTMaterials.RutheniumTriniumAmericiumNeutronate.getFluid(1152))
                .inputFluids(GTMaterials.Lubricant.getFluid(6000))
                .outputItems(output.asItem())
                .EUt(491520)
                .duration(600)
                .stationResearch(b -> b
                        .researchStack(researchStack.asStack())
                        .CWUt(256)
                        .EUt(491520))
                .save(provider);
    }

    private static net.minecraft.world.item.Item component(String kind, int tier) {
        Object value = switch (kind) {
            case "field_generator" -> GTCraftingComponents.FIELD_GENERATOR.get(tier);
            case "electric_pump" -> GTCraftingComponents.PUMP.get(tier);
            case "robot_arm" -> GTCraftingComponents.ROBOT_ARM.get(tier);
            case "sensor" -> GTCraftingComponents.SENSOR.get(tier);
            default -> GTCraftingComponents.CONVEYOR.get(tier);
        };
        return ((net.minecraft.world.item.ItemStack) value).getItem();
    }
}
