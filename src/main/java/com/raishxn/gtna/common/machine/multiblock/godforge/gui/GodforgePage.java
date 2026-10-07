package com.raishxn.gtna.common.machine.multiblock.godforge.gui;

import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;

import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * One Forge of Gods module panel shown as a side tab of the module UI (the controller uses its own GTNH
 * layout in {@link ForgeOfGodsUI}).
 */
public class GodforgePage implements IFancyUIProvider {

    private final String titleKey;
    private final IGuiTexture icon;
    private final Function<FancyMachineUIWidget, Widget> builder;
    private final boolean inventory;

    public GodforgePage(String titleKey, IGuiTexture icon, Supplier<Widget> builder) {
        this(titleKey, icon, widget -> builder.get(), false);
    }

    public GodforgePage(String titleKey, IGuiTexture icon, Function<FancyMachineUIWidget, Widget> builder,
                        boolean inventory) {
        this.titleKey = titleKey;
        this.icon = icon;
        this.builder = builder;
        this.inventory = inventory;
    }

    @Override
    public Widget createMainPage(FancyMachineUIWidget widget) {
        return builder.apply(widget);
    }

    @Override
    public IGuiTexture getTabIcon() {
        return icon;
    }

    @Override
    public Component getTitle() {
        return Component.translatable(titleKey);
    }

    @Override
    public List<Component> getTabTooltips() {
        return List.of(getTitle());
    }

    @Override
    public boolean hasPlayerInventory() {
        return inventory;
    }
}
