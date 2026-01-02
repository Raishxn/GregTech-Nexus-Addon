package com.raishxn.gtna.data.recipe;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.raishxn.gtna.common.data.GTNARecipeType;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import java.util.function.Consumer;
public class GTNAWoodCutterRecipes {
    public static void register(Consumer<FinishedRecipe> provider) {
        createWoodcutterRecipe(provider, "oak", Items.OAK_SAPLING, Items.OAK_LOG, Items.OAK_LEAVES, false);
        createWoodcutterRecipe(provider, "spruce", Items.SPRUCE_SAPLING, Items.SPRUCE_LOG, Items.SPRUCE_LEAVES, false);
        createWoodcutterRecipe(provider, "birch", Items.BIRCH_SAPLING, Items.BIRCH_LOG, Items.BIRCH_LEAVES, false);
        createWoodcutterRecipe(provider, "jungle", Items.JUNGLE_SAPLING, Items.JUNGLE_LOG, Items.JUNGLE_LEAVES, false);
        createWoodcutterRecipe(provider, "acacia", Items.ACACIA_SAPLING, Items.ACACIA_LOG, Items.ACACIA_LEAVES, false);
        createWoodcutterRecipe(provider, "dark_oak", Items.DARK_OAK_SAPLING, Items.DARK_OAK_LOG, Items.DARK_OAK_LEAVES, false);
        createWoodcutterRecipe(provider, "mangrove", Items.MANGROVE_PROPAGULE, Items.MANGROVE_LOG, Items.MANGROVE_LEAVES, false);
        createWoodcutterRecipe(provider, "cherry", Items.CHERRY_SAPLING, Items.CHERRY_LOG, Items.CHERRY_LEAVES, false);
        createWoodcutterRecipe(provider, "rubber", GTBlocks.RUBBER_SAPLING.get(), GTBlocks.RUBBER_LOG.get(), GTBlocks.RUBBER_LEAVES.get(), true);
    }
    private static void createWoodcutterRecipe(Consumer<FinishedRecipe> provider, String name, ItemLike sapling, ItemLike log, ItemLike leaves, boolean isRubber) {
        var builder = GTNARecipeType.WOODCUTTER_RECIPES.recipeBuilder("woodcutter_" + name)
                .notConsumable(sapling.asItem())
                .outputItems(new ItemStack(log.asItem(), 48))
                .chancedOutput(new ItemStack(sapling.asItem(), 2), 1000, 0)
                .chancedOutput(new ItemStack(leaves.asItem(), 6), 1500, 0)
                .EUt(60)
                .duration(800);
        if (isRubber) {
            builder.chancedOutput(new ItemStack(GTItems.STICKY_RESIN.asItem(), 4), 3000, 0);
        }
        builder.save(provider);
    }
}