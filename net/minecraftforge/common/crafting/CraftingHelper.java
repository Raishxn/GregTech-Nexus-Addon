/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.common.crafting;

import java.util.Objects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.TagParser;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.GsonHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public class CraftingHelper {
    private static Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static ItemStack getItemStack(JsonObject json, boolean readNBT) {
        return getItemStack(json, readNBT, false);
    }

    public static Item getItem(String itemName, boolean disallowsAirInRecipe) {
        ResourceLocation itemKey = new ResourceLocation(itemName);
        if (!ForgeRegistries.ITEMS.containsKey(itemKey))
            throw new JsonSyntaxException("Unknown item '" + itemName + "'");

        Item item = ForgeRegistries.ITEMS.getValue(itemKey);
        if (disallowsAirInRecipe && item == Items.f_41852_)
            throw new JsonSyntaxException("Invalid item: " + itemName);
        return Objects.requireNonNull(item);
    }

    public static CompoundTag getNBT(JsonElement element) {
        try {
            if (element.isJsonObject())
                return TagParser.m_129359_(GSON.toJson(element));
            else
                return TagParser.m_129359_(GsonHelper.m_13805_(element, "nbt"));
        } catch (CommandSyntaxException e) {
            throw new JsonSyntaxException("Invalid NBT Entry: " + e);
        }
    }

    public static ItemStack getItemStack(JsonObject json, boolean readNBT, boolean disallowsAirInRecipe) {
        String itemName = GsonHelper.m_13906_(json, "item");
        Item item = getItem(itemName, disallowsAirInRecipe);
        if (readNBT && json.has("nbt")) {
            CompoundTag nbt = getNBT(json.get("nbt"));
            CompoundTag tmp = new CompoundTag();
            if (nbt.m_128441_("ForgeCaps")) {
                tmp.m_128365_("ForgeCaps", nbt.m_128423_("ForgeCaps"));
                nbt.m_128473_("ForgeCaps");
            }

            tmp.m_128365_("tag", nbt);
            tmp.m_128359_("id", itemName);
            tmp.m_128405_("Count", GsonHelper.m_13824_(json, "count", 1));

            return ItemStack.m_41712_(tmp);
        }

        return new ItemStack(item, GsonHelper.m_13824_(json, "count", 1));
    }
}
