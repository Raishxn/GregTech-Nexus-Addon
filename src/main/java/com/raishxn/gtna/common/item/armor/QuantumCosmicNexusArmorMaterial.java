package com.raishxn.gtna.common.item.armor;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

import com.raishxn.gtna.common.data.GTNAMaterials;

public final class QuantumCosmicNexusArmorMaterial implements ArmorMaterial {

    public static final QuantumCosmicNexusArmorMaterial INSTANCE = new QuantumCosmicNexusArmorMaterial();

    private QuantumCosmicNexusArmorMaterial() {}

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
            case BOOTS -> 10;
            case LEGGINGS -> 20;
            case CHESTPLATE -> 24;
            case HELMET -> 10;
        };
    }

    @Override
    public int getEnchantmentValue() {
        return 200;
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
        return "quantum_cosmic_nexus";
    }

    @Override
    public float getToughness() {
        return 100.0F;
    }

    @Override
    public float getKnockbackResistance() {
        return 1.0F;
    }
}
