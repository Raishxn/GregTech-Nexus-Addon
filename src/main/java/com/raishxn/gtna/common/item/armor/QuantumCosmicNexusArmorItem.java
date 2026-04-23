package com.raishxn.gtna.common.item.armor;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import com.raishxn.gtna.utils.GTNATooltips;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class QuantumCosmicNexusArmorItem extends ArmorItem {

    public QuantumCosmicNexusArmorItem(Type type, Properties properties) {
        super(QuantumCosmicNexusArmorMaterial.INSTANCE, type, properties);
    }

    @Override
    public String getArmorTexture(ItemStack stack, net.minecraft.world.entity.Entity entity, EquipmentSlot slot,
                                  String type) {
        return "gtna:textures/models/armor/quantum_cosmic_nexus_armor_layer_" +
                (slot == EquipmentSlot.LEGS ? "2" : "1") + ".png";
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip,
                                TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(GTNATooltips.desc("item.gtna.quantum_cosmic_nexus_armor.tooltip"));
        tooltip.add(GTNATooltips.important("item.gtna.quantum_cosmic_nexus_armor.tooltip.power"));
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }
}
