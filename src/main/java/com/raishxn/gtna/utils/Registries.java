package com.raishxn.gtna.utils;

import com.raishxn.gtna.GTNACORE;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.registries.ForgeRegistries;

public class Registries {

    public static Item getItem(String s) {
        Item i = ForgeRegistries.ITEMS.getValue(new ResourceLocation(s));
        if (i == Items.AIR) {
            GTNACORE.LOGGER.atError().log("Item with ID {}not found", s);
            return Items.BARRIER;
        }
        return i;
    }

    public static ItemStack getItemStack(String s) {
        return getItemStack(s, 1);
    }

    public static ItemStack getItemStack(String s, int a) {
        return new ItemStack(getItem(s), a);
    }

    public static Block getBlock(String s) {
        Block b = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(s));
        if (b == Blocks.AIR) {
            GTNACORE.LOGGER.atError().log("No block with ID {}found", s);
            return Blocks.BARRIER;
        }
        return b;
    }

    public static Fluid getFluid(String s) {
        Fluid f = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(s));
        if (f == Fluids.EMPTY) {
            GTNACORE.LOGGER.atError().log("No fluid with ID {}found", s);
            return Fluids.WATER;
        }
        return f;
    }

    public static ResourceKey<Level> getDimension(String s) {
        return ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION,
                new ResourceLocation(s));
    }
}
