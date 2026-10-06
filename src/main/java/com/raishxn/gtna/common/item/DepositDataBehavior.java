package com.raishxn.gtna.common.item;

import com.gregtechceu.gtceu.api.item.component.IAddInformation;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public final class DepositDataBehavior implements IAddInformation {

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable("gtna.fluid.data.tooltip"));
        if (stack.getTag() == null) return;
        var tag = stack.getTag();
        lines.add(Component.translatable("gtna.fluid.data.origin", tag.getString("dimension")));
        var fluidId = ResourceLocation.tryParse(tag.getString("fluid"));
        if (fluidId != null) {
            lines.add(Component.translatable("gtna.fluid.data.fluid",
                    BuiltInRegistries.FLUID.get(fluidId).getFluidType().getDescription()));
        }
    }
}
