package com.raishxn.gtna.mixin.gtceu;

import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;

import com.raishxn.gtna.client.GTNAStructureCheckButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Adds the structure action to every controller that uses GTCEu's fancy screen. */
@Mixin(FancyMachineUIWidget.class)
public abstract class FancyMachineUIWidgetStructureMixin {

    @Redirect(method = "setupFancyUI(Lcom/gregtechceu/gtceu/api/gui/fancy/IFancyUIProvider;Z)V",
              at = @At(value = "INVOKE",
                       target = "Lcom/gregtechceu/gtceu/api/gui/fancy/IFancyUIProvider;attachConfigurators(Lcom/gregtechceu/gtceu/api/gui/fancy/ConfiguratorPanel;)V"),
              remap = false)
    private void gtna$attachStructureCheck(IFancyUIProvider page, ConfiguratorPanel panel) {
        page.attachConfigurators(panel);
        if (page instanceof MultiblockControllerMachine controller) {
            panel.attachConfigurators(GTNAStructureCheckButton.configurator(controller));
        }
    }
}
