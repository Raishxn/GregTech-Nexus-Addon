package com.raishxn.gtna.data.recipe;

import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

import com.raishxn.gtna.common.data.GTNARecipeType;

import java.util.function.Consumer;

/** GTOCore WoodRecipes tree growth, leaf and sapling recovery family. */
public final class GTNATreeGrowthRecipes {

    private GTNATreeGrowthRecipes() {}

    public static void register(Consumer<FinishedRecipe> provider) {
        GTNARecipeType.TREE_GROWTH_RECIPES.recipeBuilder("minecraft_mangrove")
                .notConsumable(Blocks.MANGROVE_PROPAGULE.asItem()).circuitMeta(1)
                .inputFluids(GTMaterials.Water, 4000)
                .outputItems(Blocks.MANGROVE_LOG.asItem(), 16)
                .EUt(30).duration(1200).save(provider);
        GTNARecipeType.TREE_GROWTH_RECIPES.recipeBuilder("minecraft_mangrove_fertilizer")
                .notConsumable(Blocks.MANGROVE_PROPAGULE.asItem())
                .inputItems(GTItems.FERTILIZER, 8).circuitMeta(2)
                .inputFluids(GTMaterials.Water, 8000)
                .outputItems(Blocks.MANGROVE_LOG.asItem(), 64)
                .EUt(120).duration(300).save(provider);

        for (String name : new String[] { "oak", "spruce", "birch", "acacia", "dark_oak", "cherry" }) {
            tree(provider, "minecraft", name, null);
        }
        tree(provider, "minecraft", "jungle", "cocoa_beans");
        tree(provider, "deeperdarker", "echo", "sculk_gleam");
        tree(provider, "gtceu", "rubber", "sticky_resin");
        tree(provider, "ars_nouveau", "blue_archwood", "frostaya_pod");
        tree(provider, "ars_nouveau", "red_archwood", "bombegranate_pod");
        tree(provider, "ars_nouveau", "purple_archwood", "bastion_pod");
        tree(provider, "ars_nouveau", "green_archwood", "mendosteen_pod");
        for (String name : new String[] { "fir", "redwood", "mahogany", "jacaranda", "palm", "willow",
                "hellbark", "dead", "umbran", "magic" }) {
            tree(provider, "biomesoplenty", name, null);
        }
        for (String name : new String[] { "skyris", "white_mangrove", "willow", "witch_hazel", "zelkova",
                "holly", "ironwood", "jacaranda", "mahogany", "maple", "palm", "pine",
                "rainbow_eucalyptus", "redwood", "aspen", "baobab", "blue_enchanted", "cika",
                "cypress", "ebony", "fir", "green_enchanted" }) {
            tree(provider, "biomeswevegone", name, null);
        }
    }

    private static void tree(Consumer<FinishedRecipe> provider, String mod, String name, String extraName) {
        Item sapling = item(mod, name + "_sapling");
        Item log = item(mod, name + "_log");
        if (sapling == null || log == null) return;
        Item extra = extraName == null ? null : item(mod, extraName);
        String id = mod + "_" + name;
        var normal = GTNARecipeType.TREE_GROWTH_RECIPES.recipeBuilder(id)
                .notConsumable(sapling).circuitMeta(1).inputFluids(GTMaterials.Water, 4000)
                .outputItems(log, 16).EUt(30).duration(1200);
        if (extra != null) normal.outputItems(extra, 4);
        normal.save(provider);
        var fertilized = GTNARecipeType.TREE_GROWTH_RECIPES.recipeBuilder(id + "_fertilizer")
                .notConsumable(sapling).inputItems(GTItems.FERTILIZER, 8).circuitMeta(2)
                .inputFluids(GTMaterials.Water, 8000)
                .outputItems(log, 64).EUt(120).duration(300);
        if (extra != null) fertilized.outputItems(extra, 16);
        fertilized.save(provider);

        Item leaves = item(mod, name + "_leaves");
        if (leaves == null) return;
        GTNARecipeType.GREENHOUSE_RECIPES.recipeBuilder(id + "_leaves")
                .notConsumable(sapling).circuitMeta(1).inputFluids(GTMaterials.Water, 1000)
                .outputItems(leaves, 32).duration(1200).EUt(30).save(provider);
        GTNARecipeType.GREENHOUSE_RECIPES.recipeBuilder(id + "_leaves_fertilizer")
                .notConsumable(sapling).inputItems(GTItems.FERTILIZER, 4).circuitMeta(2)
                .inputFluids(GTMaterials.Water, 1000)
                .outputItems(leaves, 64).duration(400).EUt(60).save(provider);
        // GTCEu already consumes rubber leaves in an extractor recipe. Registering GTO's
        // sapling recovery at the same input would make a conflicting recipe that cannot load.
        if (mod.equals("gtceu") && name.equals("rubber")) return;
        GTRecipeTypes.EXTRACTOR_RECIPES.recipeBuilder("gtna_" + id + "_sapling")
                .inputItems(leaves).chancedOutput(new ItemStack(sapling), 1000, 100)
                .EUt(30).duration(100).save(provider);
    }

    private static Item item(String mod, String name) {
        return BuiltInRegistries.ITEM.getOptional(new ResourceLocation(mod, name)).orElse(null);
    }
}
