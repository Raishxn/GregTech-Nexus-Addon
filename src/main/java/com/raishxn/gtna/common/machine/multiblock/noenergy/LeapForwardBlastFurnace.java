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
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
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

    @Persisted @DescSynced
    private int extraLayers = 0;

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

        Level level = this.getLevel();
        BlockPos centerPos = this.getPos();

        // CORREÇÃO: Fixar a Base.
        // O Pattern define o Controller na segunda camada (índice 1).
        // Logo, a base física da máquina é SEMPRE (Y - 1).
        // Isso impede que o scanner leia a terra/chão abaixo da máquina.
        int structMinY = centerPos.getY() - 1;

        // Assumimos inicialmente que o topo é onde o controller está
        int structMaxY = centerPos.getY();

        if (level != null) {
            // Escaneamos APENAS para CIMA (de +1 até +50)
            for (int yRel = 1; yRel <= 50; yRel++) {

                boolean hasBlockInLayer = false;

                // Varredura Radial na camada
                for (int xRel = -4; xRel <= 4; xRel++) {
                    for (int zRel = -4; zRel <= 4; zRel++) {
                        BlockPos checkPos = centerPos.offset(xRel, yRel, zRel);
                        if (!level.getBlockState(checkPos).isAir()) {
                            hasBlockInLayer = true;
                            break;
                        }
                    }
                    if (hasBlockInLayer) break;
                }

                if (hasBlockInLayer) {
                    structMaxY = centerPos.getY() + yRel;
                } else {
                    // Se encontrou uma camada de ar acima do controller, a máquina acabou.
                    break;
                }
            }
        }

        int totalHeight = structMaxY - structMinY + 1;

        // Com a base fixada em (Y-1), a altura 13 agora será lida corretamente como 13.
        this.extraLayers = Math.max(0, totalHeight - 13);

        this.targetDuration = 400 + (this.extraLayers * TICKS_PER_LAYER);

        long calcParallel = 8L * (long) Math.pow(2, this.extraLayers);
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

            textList.add(Component.translatable("gtna.multiblock.leap_pbf.layers_hud",
                    Component.literal(String.valueOf(extraLayers)).withStyle(ChatFormatting.AQUA)));

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