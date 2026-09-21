package com.raishxn.gtna.data.recipe;

import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

import com.raishxn.gtna.common.data.GTNARecipeType;

import java.util.function.Consumer;

/**
 * Recipes for the Steam Lava Maker: melt stone into lava (GTNL parity, reimplemented).
 */
public class GTNALavaMakerRecipes {

    private GTNALavaMakerRecipes() {}

    public static void register(Consumer<FinishedRecipe> provider) {
        GTNARecipeType.LAVA_MAKER_RECIPES.recipeBuilder("lava_from_stone")
                .inputItems(new ItemStack(Blocks.STONE))
                .outputFluids(GTMaterials.Lava.getFluid(1000))
                .EUt(16)
                .duration(20)
                .save(provider);
    }
}
