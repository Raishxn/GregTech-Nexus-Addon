package com.raishxn.gtna.data.recipe;

import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.raishxn.gtna.common.data.GTNARecipeType;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.block;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.gem;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Coal;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Coke;

public class GTNAInfernalCokeRecipes {

    public static void register(Consumer<FinishedRecipe> provider) {

        GTNARecipeType.INFERNAL_COKE_RECIPES.recipeBuilder("infernal_coke_from_coal")
                .inputItems(Items.COAL)
                .outputItems(gem, Coke)
                .outputFluids(GTMaterials.Creosote.getFluid(500))
                .duration(900)
                .EUt(0)
                .save(provider);
        GTNARecipeType.INFERNAL_COKE_RECIPES.recipeBuilder("infernal_charcoal_from_logs")
                .inputItems(ItemTags.LOGS_THAT_BURN)
                .outputItems(Items.CHARCOAL)
                .outputFluids(GTMaterials.Creosote.getFluid(250))
                .duration(900)
                .EUt(0)
                .save(provider);
        GTNARecipeType.INFERNAL_COKE_RECIPES.recipeBuilder("infernal_coke_block_from_coal_block")
                .inputItems(Items.COAL_BLOCK)
                .outputItems(block,Coke)
                .outputFluids(GTMaterials.Creosote.getFluid(4500))
                .duration(8100)
                .EUt(0)
                .save(provider);
    }
}