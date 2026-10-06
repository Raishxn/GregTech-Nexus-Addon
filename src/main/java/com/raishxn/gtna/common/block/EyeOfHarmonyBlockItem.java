package com.raishxn.gtna.common.block;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import com.raishxn.gtna.common.data.GTNASources;

import java.util.List;

import javax.annotation.Nullable;

/** Field metadata and source attribution for the new physical Eye of Harmony components. */
public class EyeOfHarmonyBlockItem extends GTNABlockItem {

    public EyeOfHarmonyBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (getBlock() instanceof EyeOfHarmonyFieldBlock field) {
            tooltip.add(Component.translatable("gtna.eoh.field_tier", field.getFieldTier() + 1)
                    .withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.translatable("gtna.eoh.field." + field.getFamily().id)
                    .withStyle(ChatFormatting.GRAY));
        } else if (getBlock() instanceof EyeOfHarmonyPlanetBlock planet) {
            tooltip.add(Component.translatable("gtna.eoh.planet_dimension", planet.getDimension().toString())
                    .withStyle(ChatFormatting.GRAY));
        }
        tooltip.add(Component.translatable(getBlock() instanceof EyeOfHarmonyPlanetBlock ?
                "gtna.eoh.component_stage" : "gtna.eoh.structure.component").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(GTNASources
                .line(getBlock() instanceof EyeOfHarmonyPlanetBlock ? GTNASources.GTNEIOREPLUGIN : GTNASources.GTNH));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
