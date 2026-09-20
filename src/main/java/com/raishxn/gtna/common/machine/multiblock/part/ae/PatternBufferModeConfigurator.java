package com.raishxn.gtna.common.machine.multiblock.part.ae;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;
import com.gregtechceu.gtceu.common.data.GTItems;

import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceBorderTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Fancy side tab that pins the buffer to one recipe type — GTOCore
 * {@code MultiMachineModeFancyConfigurator} parity, adapted to the official GTM base.
 *
 * <p>
 * GTOCore's buffer exposes a mode selector on its own UI listing the recipe types the controller
 * offers; picking one limits which patterns that buffer answers for. This is the same idea: the
 * option list comes from {@link PatternBufferModeRegistry#getBufferModeOptions(String)} and the
 * selection lives on the machine as a synced {@code selectedModeId}, which
 * {@code gtna$slotAcceptsRecipe} enforces. Selecting "all modes" clears the filter.
 *
 * <p>
 * No manual widget sync is needed (unlike GTOCore's configurator): the selection is a
 * {@code @DescSynced} machine field, so the highlight follows the server value on its own. Clicks
 * are only applied server-side, which is why the button checks {@code clickData.isRemote}.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
final class PatternBufferModeConfigurator implements IFancyUIProvider {

    private static final int WIDTH = 140;
    private static final int ROW_HEIGHT = 20;

    private final GTNAMEPatternBufferPartMachine machine;

    PatternBufferModeConfigurator(GTNAMEPatternBufferPartMachine machine) {
        this.machine = machine;
    }

    @Override
    public Widget createMainPage(FancyMachineUIWidget widget) {
        List<PatternBufferModeRegistry.ModeOption> options = machine.getModeRegistry()
                .getBufferModeOptions(machine.getSelectedModeId());

        WidgetGroup group = new WidgetGroup(0, 0, WIDTH, ROW_HEIGHT * options.size() + 4);
        group.setBackground(GuiTextures.BACKGROUND_INVERSE);
        for (int i = 0; i < options.size(); i++) {
            PatternBufferModeRegistry.ModeOption option = options.get(i);
            int y = 2 + i * ROW_HEIGHT;
            group.addWidget(new ButtonWidget(2, y, WIDTH - 4, ROW_HEIGHT, IGuiTexture.EMPTY, clickData -> {
                if (!clickData.isRemote) {
                    machine.setSelectedModeId(option.id());
                }
            }));
            group.addWidget(new ImageWidget(2, y, WIDTH - 4, ROW_HEIGHT,
                    () -> new GuiTextureGroup(
                            ResourceBorderTexture.BUTTON_COMMON.copy()
                                    .setColor(option.id().equals(machine.getSelectedModeId()) ?
                                            ColorPattern.CYAN.color : -1),
                            new TextTexture(option.label()).setWidth(WIDTH - 8)
                                    .setType(TextTexture.TextType.ROLL))));
        }
        return group;
    }

    @Override
    public IGuiTexture getTabIcon() {
        return new ItemStackTexture(GTItems.ROBOT_ARM_LV.get());
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gtceu.gui.machinemode.title");
    }

    @Override
    public List<Component> getTabTooltips() {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(Component.translatable("gtceu.gui.machinemode.tab_tooltip"));
        tooltip.add(Component.translatable("gtna.machine.pattern_buffer.buffer_mode.tooltip"));
        return tooltip;
    }
}
