package com.raishxn.gtna.data.recipe;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.raishxn.gtna.common.data.GTNABlocks;
import com.raishxn.gtna.common.data.GTNAMaterials;
import com.raishxn.gtna.common.data.GTNARecipeType;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

import java.util.function.Consumer;

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
                // ADICIONADO: unlockedBy
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
                // ADICIONADO: unlockedBy
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
                // ADICIONADO: unlockedBy
                .unlockedBy("has_clay_compound_plate", InventoryChangeTrigger.TriggerInstance.hasItems(ChemicalHelper.get(TagPrefix.plate, GTNAMaterials.ClayCompound).getItem()))
                .save(provider);

        // 4. Bronze Reinforced Wood
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNABlocks.BRONZE_REINFORCED_WOOD.get())
                .pattern("AAA")
                .pattern("ABA")
                .pattern("AAA")
                .define('A', ChemicalHelper.get(TagPrefix.plate, GTMaterials.Bronze).getItem())
                .define('B', ChemicalHelper.get(TagPrefix.frameGt, GTMaterials.Wood).getItem())
                // ADICIONADO: unlockedBy
                .unlockedBy("has_bronze_plate", InventoryChangeTrigger.TriggerInstance.hasItems(ChemicalHelper.get(TagPrefix.plate, GTMaterials.Bronze).getItem()))
                .save(provider);

        // Receita de Máquina (GTCEu) - Esta não precisa de unlockedBy pois usa builder próprio
        GTNARecipeType.HYDRAULIC_MANUFACTURING.recipeBuilder("steam_compact_pipe_casing")
                .inputItems(ChemicalHelper.getTag(TagPrefix.pipeNormalFluid, GTNAMaterials.Breel), 1)
                .inputItems(ChemicalHelper.getTag(TagPrefix.pipeTinyFluid, GTNAMaterials.CompressedSteam), 2)
                .inputItems(ChemicalHelper.getTag(TagPrefix.plate, GTNAMaterials.CompressedSteam), 6)
                .outputItems(GTNABlocks.STEAM_COMPACT_PIPE_CASING.get(), 1)
                .duration(120)
                .EUt(24)
                .save(provider);

        // 5. Solar Boiling Cell
        // Atenção: Use .getItem() para o Stronze Pipe para evitar crash se o pipe não existir
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNABlocks.SOLAR_BOILING_CELL.get())
                .pattern("AAA")
                .pattern("BCB")
                .define('A', Blocks.GLASS)
                .define('B', ChemicalHelper.get(TagPrefix.pipeTinyFluid, GTNAMaterials.Stronze).getItem())
                .define('C', GTMachines.STEAM_SOLAR_BOILER.right().asStack().getItem())                // ADICIONADO: unlockedBy
                .unlockedBy("has_glass", InventoryChangeTrigger.TriggerInstance.hasItems(Blocks.GLASS))
                .save(provider);
    }
}