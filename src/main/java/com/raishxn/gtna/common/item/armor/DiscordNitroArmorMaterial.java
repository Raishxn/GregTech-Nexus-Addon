package com.raishxn.gtna.common.item.armor;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

import com.raishxn.gtna.common.data.GTNAMaterials;

public final class DiscordNitroArmorMaterial implements ArmorMaterial {

    public static final DiscordNitroArmorMaterial INSTANCE = new DiscordNitroArmorMaterial();

    private DiscordNitroArmorMaterial() {}

    @Override
    public int getDurabilityForType(ArmorItem.Type type) {
        return switch (type) {
            case BOOTS -> 481;
            case LEGGINGS -> 555;
            case CHESTPLATE -> 592;
            case HELMET -> 407;
        };
    }

    @Override
    public int getDefenseForType(ArmorItem.Type type) {
        return switch (type) {
            case BOOTS -> 4;
            case LEGGINGS -> 7;
            case CHESTPLATE -> 9;
            case HELMET -> 4;
        };
    }

    @Override
    public int getEnchantmentValue() {
        return 26;
    }

    @Override
    public SoundEvent getEquipSound() {
        return SoundEvents.ARMOR_EQUIP_NETHERITE;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.of(ChemicalHelper.get(TagPrefix.ingot, GTNAMaterials.Echoite).getItem());
    }

    @Override
    public String getName() {
        return "discord_nitro";
    }

    @Override
    public float getToughness() {
        return 4.0F;
    }

    @Override
    public float getKnockbackResistance() {
        return 0.15F;
    }
}
