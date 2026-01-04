package com.raishxn.gtna.common.machine.multiblock.noenergy;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;
import com.gregtechceu.gtceu.api.gui.fancy.TooltipsPanel;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockDisplayText;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.common.machine.multiblock.primitive.PrimitiveBlastFurnaceMachine;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

public class LeapForwardBlastFurnace extends PrimitiveBlastFurnaceMachine implements ITieredMachine, IDisplayUIMachine, IFancyUIMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            LeapForwardBlastFurnace.class, PrimitiveBlastFurnaceMachine.MANAGED_FIELD_HOLDER);

    @Persisted @DescSynced
    private int currentParallel = 8;

    @Persisted @DescSynced
    private int targetDuration = 400;

    private static final int MAX_PARALLEL_CAP = 32000;
    private static final int TICKS_PER_LAYER = 400;

    public LeapForwardBlastFurnace(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (IMultiPart part : this.getParts()) {
            int y = part.self().getPos().getY();
            if (y < minY) minY = y;
            if (y > maxY) maxY = y;
        }
        if (minY == Integer.MAX_VALUE) {
            minY = 0; maxY = 0;
        }

        int height = maxY - minY + 1;

        int extraLayers = Math.max(0, height - 3);

        this.targetDuration = 400 + (extraLayers * TICKS_PER_LAYER);

        long calcParallel = 8L * (long) Math.pow(2, extraLayers);

        this.currentParallel = (int) Math.min(calcParallel, MAX_PARALLEL_CAP);
    }

    @Nullable
    public static ModifierFunction recipeModifier(MetaMachine machine, @Nonnull GTRecipe recipe) {
        if (machine instanceof LeapForwardBlastFurnace pbf) {
            int parallels = ParallelLogic.getParallelAmount(machine, recipe, pbf.currentParallel);

            double originalDuration = Math.max(1, recipe.duration);
            double durationMultiplier = (double) pbf.targetDuration / originalDuration;

            return ModifierFunction.builder()
                    .parallels(parallels)
                    .modifyAllContents(ContentModifier.multiplier(parallels))
                    .durationMultiplier(durationMultiplier)
                    .build();
        }
        return ModifierFunction.NULL;
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        return new ModularUI(198, 208, this, entityPlayer)
                .widget(new FancyMachineUIWidget(this, 198, 208));
    }

    @Override
    public Widget createUIWidget() {
        var group = new WidgetGroup(0, 0, 182 + 8, 117 + 8);

        var screen = new DraggableScrollableWidgetGroup(4, 4, 182, 117)
                .setBackground(GuiTextures.DISPLAY);

        screen.addWidget(new LabelWidget(4, 5, self().getBlockState().getBlock().getDescriptionId()));
        screen.addWidget(new ComponentPanelWidget(4, 17, this::addDisplayText)
                .setMaxWidthLimit(170)
                .clickHandler(this::handleDisplayClick));

        group.addWidget(screen);
        group.setBackground(GuiTextures.BACKGROUND_INVERSE);
        return group;
    }

    @Override
    public List<IFancyUIProvider> getSubTabs() {
        return getParts().stream()
                .filter(Objects::nonNull)
                .filter(IFancyUIProvider.class::isInstance)
                .map(IFancyUIProvider.class::cast)
                .toList();
    }

    @Override
    public void attachTooltips(TooltipsPanel tooltipsPanel) {
        for (IMultiPart part : getParts()) {
            part.attachFancyTooltipsToController(this, tooltipsPanel);
        }
    }

    @Override
    public void addDisplayText(@NotNull List<Component> textList) {
        if (isFormed()) {
            textList.add(Component.translatable("gtna.multiblock.leap_pbf.parallel_hud",
                    Component.literal(String.valueOf(currentParallel)).withStyle(ChatFormatting.GOLD)));
            textList.add(Component.translatable("gtna.multiblock.leap_pbf.duration_hud",
                    Component.literal(String.valueOf(targetDuration / 20)).withStyle(ChatFormatting.RED)));
        }
        MultiblockDisplayText.builder(textList, isFormed())
                .setWorkingStatus(recipeLogic.isWorkingEnabled(), recipeLogic.isActive())
                .addWorkingStatusLine()
                .addProgressLine(recipeLogic)
                .addOutputLines(recipeLogic.getLastRecipe());
    }

    @Override
    public void handleDisplayClick(String componentData, ClickData clickData) {
    }

    @Override
    public int getTier() {
        return GTValues.LV;
    }
}