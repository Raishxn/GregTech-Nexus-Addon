package com.raishxn.gtna.mixin.gtceu;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.ButtonConfigurator;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;

import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;

import net.minecraft.network.chat.Component;

import com.raishxn.gtna.api.machine.multiblock.GTNAModuleDisplay;
import com.raishxn.gtna.api.machine.multiblock.GTNAStructureRefresh;
import com.raishxn.gtna.api.machine.multiblock.GTNASubPatterns;
import com.raishxn.gtna.client.renderer.GTNATextures;
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

    @Inject(method = "attachConfigurators", at = @At("TAIL"), remap = false)
    private void gtna$addStructureCheck(ConfiguratorPanel panel, CallbackInfo ci) {
        WorkableElectricMultiblockMachine machine = (WorkableElectricMultiblockMachine) (Object) this;
        if (GTNASubPatterns.get(machine.getDefinition()).isEmpty()) {
            return;
        }
        panel.attachConfigurators(new ButtonConfigurator(
                new GuiTextureGroup(GuiTextures.BUTTON, GTNATextures.STRUCTURE_CHECK.getSubTexture(0, 0, 1, 0.5)),
                click -> {
                    if (!click.isRemote) {
                        GTNAStructureRefresh.refresh(machine, click.isShiftClick);
                    }
                }).setTooltips(List.of(
                        Component.translatable("gtna.machine.structure_check"),
                        Component.translatable("gtna.machine.structure_check.shift"))));
    }
}
