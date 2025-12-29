//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.raishxn.gtna.utils;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeSerializer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class GTNAUtil {
    public static String getItemId(Item item) {
        return ((ResourceLocation)Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(item))).toString();
    }

    public static String getFluidId(Fluid fluid) {
        return ((ResourceLocation)Objects.requireNonNull(ForgeRegistries.FLUIDS.getKey(fluid))).toString();
    }

    public static ItemStack loadItemStack(CompoundTag compoundTag) {
        try {
            Item item = (Item)ForgeRegistries.ITEMS.getValue(new ResourceLocation(compoundTag.getString("id")));
            ItemStack stack = new ItemStack((ItemLike)Objects.requireNonNull(item), 1);
            if (compoundTag.contains("tag", 10)) {
                stack.setTag(compoundTag.getCompound("tag"));
                if (stack.getTag() != null) {
                    stack.getItem().verifyTagAfterLoad(stack.getTag());
                }
            }

            if (stack.getItem().canBeDepleted()) {
                stack.setDamageValue(stack.getDamageValue());
            }

            return stack;
        } catch (RuntimeException var2) {
            GTCEu.LOGGER.debug("Tried to load invalid item: {}", compoundTag, var2);
            return ItemStack.EMPTY;
        }
    }

    public static Tag serializeNBT(GTRecipe recipe) {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", recipe.id.toString());
        tag.put("recipe", (Tag)GTRecipeSerializer.CODEC.encodeStart(NbtOps.INSTANCE, recipe).result().orElse(new CompoundTag()));
        return tag;
    }

    public static @Nullable GTRecipe deserializeNBT(Tag tag) {
        if (tag instanceof CompoundTag ctag) {
            ResourceLocation id = ResourceLocation.tryParse(ctag.getString("id"));
            GTRecipe recipe = (GTRecipe)GTRecipeSerializer.CODEC.parse(NbtOps.INSTANCE, ctag.get("recipe")).result().orElse((GTRecipe) null);
            if (recipe != null && id != null) {
                recipe.setId(id);
                return recipe;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
}
