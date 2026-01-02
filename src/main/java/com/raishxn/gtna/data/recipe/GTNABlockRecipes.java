package com.raishxn.gtna.data.recipe;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.*;
import com.raishxn.gtna.common.data.GTNABlocks;
import com.raishxn.gtna.common.data.GTNAItems;
import com.raishxn.gtna.common.data.GTNAMaterials;
import com.raishxn.gtna.common.data.GTNARecipeType;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;

import java.util.function.Consumer;

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
                .unlockedBy("has_clay_compound_plate", InventoryChangeTrigger.TriggerInstance.hasItems(ChemicalHelper.get(TagPrefix.plate, GTNAMaterials.ClayCompound).getItem()))
                .save(provider);

        // 2. Hyper Pressure Breel Casing
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNABlocks.HYPER_PRESSURE_BREEL_CASING.get())
                .pattern("AAA")
                .pattern("BCB")
                .pattern("AAA")
                .define('A', ChemicalHelper.get(TagPrefix.plate, GTNAMaterials.Breel).getItem())
                .define('B', ChemicalHelper.get(TagPrefix.rod, GTMaterials.Beryllium).getItem())
                .define('C', ChemicalHelper.get(TagPrefix.frameGt, GTMaterials.Beryllium).getItem())
                .unlockedBy("has_breel_plate", InventoryChangeTrigger.TriggerInstance.hasItems(ChemicalHelper.get(TagPrefix.plate, GTNAMaterials.Breel).getItem()))
                .save(provider);

        // 3. Vibration-Safe Casing
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNABlocks.VIBRATION_SAFE_CASING.get())
                .pattern("AAA")
                .pattern("BCB")
                .pattern("AAA")
                .define('A', ChemicalHelper.get(TagPrefix.plate, GTNAMaterials.ClayCompound).getItem())
                .define('B', ChemicalHelper.get(TagPrefix.plateDouble, GTNAMaterials.Breel).getItem())
                .define('C', GTBlocks.CASING_STEEL_SOLID.get())
                .unlockedBy("has_clay_compound_plate", InventoryChangeTrigger.TriggerInstance.hasItems(ChemicalHelper.get(TagPrefix.plate, GTNAMaterials.ClayCompound).getItem()))
                .save(provider);

        // 4. Bronze Reinforced Wood
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNABlocks.BRONZE_REINFORCED_WOOD.get())
                .pattern("AAA")
                .pattern("ABA")
                .pattern("AAA")
                .define('A', ChemicalHelper.get(TagPrefix.plate, GTMaterials.Bronze).getItem())
                .define('B', ChemicalHelper.get(TagPrefix.frameGt, GTMaterials.Wood).getItem())
                .unlockedBy("has_bronze_plate", InventoryChangeTrigger.TriggerInstance.hasItems(ChemicalHelper.get(TagPrefix.plate, GTMaterials.Bronze).getItem()))
                .save(provider);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNABlocks.STEEL_REINFORCED_WOOD.get())
                .pattern("AAA")
                .pattern("ABA")
                .pattern("AAA")
                .define('A', ChemicalHelper.get(TagPrefix.plate, GTMaterials.Steel).getItem())
                .define('B', ChemicalHelper.get(TagPrefix.frameGt, GTMaterials.Wood).getItem())
                .unlockedBy("has_steel_plate", InventoryChangeTrigger.TriggerInstance.hasItems(ChemicalHelper.get(TagPrefix.plate, GTMaterials.Steel).getItem()))
                .save(provider);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNABlocks.IRON_REINFORCED_WOOD.get())
                .pattern("AAA")
                .pattern("ABA")
                .pattern("AAA")
                .define('A', ChemicalHelper.get(TagPrefix.plate, GTMaterials.Iron).getItem())
                .define('B', ChemicalHelper.get(TagPrefix.frameGt, GTMaterials.Wood).getItem())
                .unlockedBy("has_iron_plate", InventoryChangeTrigger.TriggerInstance.hasItems(ChemicalHelper.get(TagPrefix.plate, GTMaterials.Iron).getItem()))
                .save(provider);

        // Receita de Máquina (GTCEu) - Esta não precisa de unlockedBy pois usa builder próprio
        GTNARecipeType.HYDRAULIC_MANUFACTURING.recipeBuilder("steam_compact_pipe_casing")
                .inputItems(ChemicalHelper.get(TagPrefix.pipeNormalFluid, GTNAMaterials.Breel).getItem(), 1)
                .inputItems(ChemicalHelper.get(TagPrefix.pipeTinyFluid, GTNAMaterials.CompressedSteam).getItem(), 2)
                .inputItems(ChemicalHelper.get(TagPrefix.plate, GTNAMaterials.CompressedSteam).getItem(), 6)
                .outputItems(GTNABlocks.STEAM_COMPACT_PIPE_CASING.get(), 1)
                .duration(120)
                .EUt(24)
                .save(provider);

        // 5. Solar Boiling Cell
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNABlocks.SOLAR_BOILING_CELL.get())
                .pattern("AAA")
                .pattern("BCB")
                .define('A', Blocks.GLASS)
                .define('B', ChemicalHelper.get(TagPrefix.pipeTinyFluid, GTNAMaterials.Stronze).getItem())
                .define('C', GTMachines.STEAM_SOLAR_BOILER.right().asStack().getItem())                // ADICIONADO: unlockedBy
                .unlockedBy("has_glass", InventoryChangeTrigger.TriggerInstance.hasItems(Blocks.GLASS))
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
                .unlockedBy("has_stronze", InventoryChangeTrigger.TriggerInstance.hasItems(ChemicalHelper.get(TagPrefix.plate, GTNAMaterials.Stronze).getItem()))
                .save(provider);

// 2. Hydraulic Assembler Casing
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNABlocks.HYDRAULIC_ASSEMBLER_CASING.get())
                .pattern("ABA")
                .pattern("CCC")
                .pattern("ABA")
                .define('A', ChemicalHelper.get(TagPrefix.pipeTinyFluid, GTNAMaterials.Stronze).getItem())
                .define('B', ChemicalHelper.get(TagPrefix.plate, GTNAMaterials.Breel).getItem())
                .define('C', GTNAItems.HYDRAULIC_ARM.get())
                .unlockedBy("has_breel", InventoryChangeTrigger.TriggerInstance.hasItems(ChemicalHelper.get(TagPrefix.plate, GTNAMaterials.Breel).getItem()))
                .save(provider);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNABlocks.BREEL_PLATED_CASING.get())
                .pattern("AAA")
                .pattern("BCB")
                .pattern("AAA")
                .define('A', ChemicalHelper.get(TagPrefix.pipeTinyFluid, GTNAMaterials.Breel).getItem())
                .define('B', ChemicalHelper.get(TagPrefix.plate, GTNAMaterials.Breel).getItem())
                .define('C', ChemicalHelper.get(TagPrefix.frameGt, GTNAMaterials.ClayCompound).getItem())
                .unlockedBy("has_breel", InventoryChangeTrigger.TriggerInstance.hasItems(ChemicalHelper.get(TagPrefix.plate, GTNAMaterials.Breel).getItem()))
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

        GTRecipeTypes.EXTRUDER_RECIPES.recipeBuilder("borosilicate_glass")
                .notConsumable(GTItems.SHAPE_EXTRUDER_BLOCK)
                .outputItems(GTNABlocks.BOROSILICATE_GLASS_BLOCK.get())
                .duration(100)
                .EUt(120)
                .save(provider);

    }
}