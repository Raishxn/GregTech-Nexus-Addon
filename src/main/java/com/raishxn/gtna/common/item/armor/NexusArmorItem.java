package com.raishxn.gtna.common.item.armor;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class NexusArmorItem extends ArmorItem {

    public NexusArmorItem(Type type, Properties properties) {
        super(NexusArmorMaterial.INSTANCE, type, properties);
    }

    @Override
    public String getArmorTexture(ItemStack stack, net.minecraft.world.entity.Entity entity, EquipmentSlot slot,
                                  String type) {
        return "gtna:textures/models/armor/nexus_layer_" + (slot == EquipmentSlot.LEGS ? "2" : "1") + ".png";
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip,
                                TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("item.gtna.nexus_armor.tooltip.core").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("item.gtna.nexus_armor.tooltip.flight").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.gtna.nexus_armor.tooltip.shield").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.gtna.nexus_armor.tooltip.mobility").withStyle(ChatFormatting.GRAY));
    }
}
