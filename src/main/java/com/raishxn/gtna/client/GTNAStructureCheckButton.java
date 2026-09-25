package com.raishxn.gtna.client;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfiguratorButton;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;

import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;

import net.minecraft.network.chat.Component;

import com.raishxn.gtna.client.renderer.GTNATextures;

import java.util.List;

/** The same structure action for GTCEu fancy configurators and custom multiblock screens. */
public final class GTNAStructureCheckButton {

    private GTNAStructureCheckButton() {}

    public static IFancyConfiguratorButton.Toggle configurator(MultiblockControllerMachine machine) {
        return new IFancyConfiguratorButton.Toggle(
                new GuiTextureGroup(GuiTextures.BUTTON, GTNATextures.STRUCTURE_CHECK.getSubTexture(0, 0, 1, 0.5)),
                new GuiTextureGroup(GuiTextures.BUTTON, GTNATextures.STRUCTURE_CHECK.getSubTexture(0, 0.5, 1, 0.5)),
                machine::isFormed,
                (click, pressed) -> {
                    if (click.isRemote && (!machine.isFormed() || click.isShiftClick)) {
                        GTNAStructureCheckClient.request(machine.self().getPos(), click.isShiftClick);
                    }
                }).setTooltipsSupplier(formed -> formed ?
                        List.of(Component.translatable("gtna.machine.structure_check.up_to_date"),
                                Component.translatable("gtna.machine.structure_check.shift")) :
                        List.of(Component.translatable("gtna.machine.structure_check"),
                                Component.translatable("gtna.machine.structure_check.shift")));
    }

    public static Widget widget(MultiblockControllerMachine machine, int x, int y) {
        return new ButtonWidget(x, y, 18, 18,
                new GuiTextureGroup(GuiTextures.BUTTON, GTNATextures.STRUCTURE_CHECK.getSubTexture(0, 0, 1, 0.5)),
                click -> {
                    if (click.isRemote && (!machine.isFormed() || click.isShiftClick)) {
                        GTNAStructureCheckClient.request(machine.self().getPos(), click.isShiftClick);
                    }
                }).setHoverTooltips(Component.translatable("gtna.machine.structure_check"),
                        Component.translatable("gtna.machine.structure_check.shift"));
    }
}
