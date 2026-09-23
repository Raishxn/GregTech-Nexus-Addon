package com.raishxn.gtna.mixin.gtceu;

import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;

import net.minecraft.network.chat.Component;

import com.raishxn.gtna.api.machine.multiblock.GTNAModuleDisplay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Shows "Formed modules: n / total" on every electric multiblock that has modules (sub-patterns),
 * including GTCEu machines such as the Electric Blast Furnace.
 */
@Mixin(WorkableElectricMultiblockMachine.class)
public abstract class WorkableElectricMultiblockMachineMixin {

    @Inject(method = "addDisplayText", at = @At("TAIL"), remap = false)
    private void gtna$addModuleCount(List<Component> textList, CallbackInfo ci) {
        GTNAModuleDisplay.append(textList, (WorkableElectricMultiblockMachine) (Object) this);
    }
}
