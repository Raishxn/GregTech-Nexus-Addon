package com.raishxn.gtna.data.recipe;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

/** Checks optional integration ingredients before generating their recipes. */
public final class GTNARecipeItems {

    private GTNARecipeItems() {}

    public static boolean present(String... ids) {
        for (String id : ids) {
            if (!BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(id))) return false;
        }
        return true;
    }
}
