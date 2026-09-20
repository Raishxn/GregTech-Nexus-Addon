package com.raishxn.gtna.common.machine.multiblock.part.ae;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;
import com.gregtechceu.gtceu.api.gui.widget.IntInputWidget;
import com.gregtechceu.gtceu.common.data.GTItems;

import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Fancy side tab collecting the buffer-wide maintenance actions, so the per-slot configuration panel
 * can stay narrow enough to sit beside the pattern grid without overflowing the page.
 *
 * <p>
 * Everything here used to live under the per-slot panel: the recipe-cache cleaning (GTLCore
 * {@code clearMachineRecipeCache} / {@code clearPatternRecipeCache} parity) and the encoded-pattern
 * circuit tooling (GTLCore {@code PatternCircuitConfigurator} parity). They are not per-slot
 * decisions, they are rarely used, and they were the block that pushed the panel 106 px past the
 * bottom of the page.
 *
 * <p>
 * One of them still acts on the buffer's selected slot, which is why it is disabled until a slot is
 * picked on the main page; the tooltip says so.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
final class PatternBufferToolsConfigurator implements IFancyUIProvider {

    private final GTNAMEPatternBufferPartMachine machine;
    private ButtonWidget clearPatternCacheButton;

    PatternBufferToolsConfigurator(GTNAMEPatternBufferPartMachine machine) {
        this.machine = machine;
    }

    @Override
    public Widget createMainPage(FancyMachineUIWidget widget) {
        WidgetGroup group = new WidgetGroup(0, 0, PatternBufferLayout.TOOLS_PAGE_WIDTH,
                PatternBufferLayout.TOOLS_PAGE_HEIGHT);
        group.setBackground(GuiTextures.BACKGROUND);
        int x = PatternBufferLayout.TOOLS_INNER_X;
        int wide = PatternBufferLayout.TOOLS_WIDE_BUTTON_WIDTH;
        int buttonHeight = PatternBufferLayout.SMALL_BUTTON_HEIGHT;

        group.addWidget(new LabelWidget(x, PatternBufferLayout.TOOLS_CACHE_LABEL_Y,
                () -> Component.translatable("gtna.machine.pattern_buffer.cache_section").getString()));

        clearPatternCacheButton = makeTextButton(x, PatternBufferLayout.TOOLS_CLEAR_PATTERN_BUTTON_Y, wide,
                buttonHeight, "gtna.machine.pattern_buffer.clear_pattern_recipe_cache",
                clickData -> {
                    if (!clickData.isRemote) machine.clearSelectedRecipeCache();
                });
        group.addWidget(clearPatternCacheButton);

        group.addWidget(makeTextButton(x, PatternBufferLayout.TOOLS_CLEAR_MACHINE_BUTTON_Y, wide, buttonHeight,
                "gtna.machine.pattern_buffer.clear_machine_recipe_cache",
                clickData -> {
                    if (!clickData.isRemote) machine.clearMachineRecipeCaches();
                }));

        group.addWidget(new LabelWidget(x, PatternBufferLayout.TOOLS_CIRCUIT_LABEL_Y,
                () -> Component.translatable("gtna.machine.pattern_buffer.embedded_circuit").getString()));
        group.addWidget(new IntInputWidget(x, PatternBufferLayout.TOOLS_CIRCUIT_INPUT_Y, 50, 14,
                machine::getEmbeddedCircuitConfig,
                value -> {
                    machine.setEmbeddedCircuitConfig(value);
                    machine.markDirty();
                }).setMin(1).setMax(32));
        group.addWidget(new ButtonWidget(PatternBufferLayout.TOOLS_SKIP_TOGGLE_X,
                PatternBufferLayout.TOOLS_CIRCUIT_INPUT_Y, PatternBufferLayout.TOOLS_SKIP_TOGGLE_WIDTH, 14,
                new GuiTextureGroup(GuiTextures.BUTTON,
                        new TextTexture(this::getSkipExistingText)
                                .setWidth(PatternBufferLayout.TOOLS_SKIP_TOGGLE_WIDTH - 6)
                                .setType(TextTexture.TextType.ROLL).setDropShadow(false)),
                clickData -> {
                    if (!clickData.isRemote) {
                        machine.setSkipExistingCircuitPatterns(!machine.isSkipExistingCircuitPatterns());
                        machine.markDirty();
                    }
                }).setHoverTooltips(Component.translatable("gtna.machine.pattern_buffer.skip_existing.tooltip")));

        group.addWidget(makeTextButton(x, PatternBufferLayout.TOOLS_ACTION_BUTTON_Y,
                PatternBufferLayout.TOOLS_ACTION_BUTTON_WIDTH, buttonHeight,
                "gtna.machine.pattern_buffer.embed_circuit",
                clickData -> {
                    if (!clickData.isRemote) machine.embedCircuitInAllPatterns();
                }));
        group.addWidget(makeTextButton(PatternBufferLayout.TOOLS_REMOVE_BUTTON_X,
                PatternBufferLayout.TOOLS_ACTION_BUTTON_Y, PatternBufferLayout.TOOLS_ACTION_BUTTON_WIDTH,
                buttonHeight, "gtna.machine.pattern_buffer.remove_circuits",
                clickData -> {
                    if (!clickData.isRemote) machine.removeAllPatternCircuits();
                }));

        // The page is rebuilt every time the tab is opened, so this is the only place the
        // selection-dependent state has to be applied.
        clearPatternCacheButton.setActive(machine.getSelectedSlot() >= 0);
        return group;
    }

    private String getSkipExistingText() {
        return Component.translatable(machine.isSkipExistingCircuitPatterns() ?
                "gtna.machine.pattern_buffer.skip_existing.on" : "gtna.machine.pattern_buffer.skip_existing.off")
                .getString();
    }

    private ButtonWidget makeTextButton(int x, int y, int width, int height, String key, Consumer<ClickData> onPress) {
        ButtonWidget button = new ButtonWidget(x, y, width, height,
                new GuiTextureGroup(GuiTextures.BUTTON,
                        new TextTexture(() -> Component.translatable(key).getString())
                                .setWidth(width - 4)
                                .setType(TextTexture.TextType.ROLL)
                                .setDropShadow(false)),
                onPress);
        button.setHoverTooltips(Component.translatable(key + ".tooltip"));
        return button;
    }

    @Override
    public IGuiTexture getTabIcon() {
        return new ItemStackTexture(GTItems.TOOL_DATA_STICK.get());
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gtna.machine.pattern_buffer.tools");
    }

    @Override
    public List<Component> getTabTooltips() {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(Component.translatable("gtna.machine.pattern_buffer.tools"));
        tooltip.add(Component.translatable("gtna.machine.pattern_buffer.tools.tooltip"));
        return tooltip;
    }
}
