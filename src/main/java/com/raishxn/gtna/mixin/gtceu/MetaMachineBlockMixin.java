package com.raishxn.gtna.mixin.gtceu;

import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;

import com.raishxn.gtna.api.machine.multiblock.GTNASubPatterns;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Appends the module (sub-pattern) tooltip lines registered in {@link GTNASubPatterns} to a machine
 * item's tooltip, so a machine like the Electric Blast Furnace advertises what its attachable module
 * unlocks (e.g. GTOCore's "Accelerate Hatch + extra Energy Hatch").
 */
@Mixin(MetaMachineBlock.class)
public abstract class MetaMachineBlockMixin {

    @Inject(method = "appendHoverText", at = @At("TAIL"))
    private void gtna$appendModuleTooltip(ItemStack stack, BlockGetter level, List<Component> tooltip,
                                          TooltipFlag flag, CallbackInfo ci) {
        MachineDefinition definition = ((MetaMachineBlock) (Object) this).definition;
        if (!(definition instanceof MultiblockMachineDefinition multiblock)) {
            return;
        }
        List<Component> moduleTooltips = GTNASubPatterns.getTooltips(multiblock);
        if (moduleTooltips.isEmpty()) {
            return;
        }
        tooltip.add(Component.empty());
        tooltip.addAll(moduleTooltips);
    }
}
