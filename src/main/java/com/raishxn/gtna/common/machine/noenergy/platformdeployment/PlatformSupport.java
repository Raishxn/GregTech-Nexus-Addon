package com.raishxn.gtna.common.machine.noenergy.platformdeployment;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import com.raishxn.gtna.GTNACORE;

final class PlatformSupport {

    private PlatformSupport() {}

    static ResourceLocation id(String path) {
        return GTNACORE.id(path);
    }

    static ResourceLocation parseId(String id) {
        ResourceLocation parsed = ResourceLocation.tryParse(id);
        if (parsed == null) {
            throw new IllegalArgumentException("Invalid resource location: " + id);
        }
        return parsed;
    }

    static ItemStack itemStack(String itemId) {
        Item item = BuiltInRegistries.ITEM.get(parseId(itemId));
        if (item == null) {
            throw new IllegalArgumentException("Unknown item: " + itemId);
        }
        return new ItemStack(item);
    }

    record Counted<T>(int amount, T value) {}
}
