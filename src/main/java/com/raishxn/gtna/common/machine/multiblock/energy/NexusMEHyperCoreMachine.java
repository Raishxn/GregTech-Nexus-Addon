package com.raishxn.gtna.common.machine.multiblock.energy;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.UITemplate;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;

import com.raishxn.gtna.common.data.GTNABlocks;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

public class NexusMEHyperCoreMachine extends WorkableMultiblockMachine implements IDisplayUIMachine {

    public static final int INTERIOR_SIZE = 5;
    public static final int TOTAL_MODULE_SLOTS = INTERIOR_SIZE * INTERIOR_SIZE * INTERIOR_SIZE;

    private static final long MODULE_I_STORAGE = 4L * 1024L * 1024L;
    private static final long MODULE_II_STORAGE = 64L * 1024L * 1024L;
    private static final long MODULE_III_STORAGE = 1024L * 1024L * 1024L;
    private static final long MODULE_IV_STORAGE = 16L * 1024L * 1024L * 1024L;

    private static final long MODULE_I_COPROCESSORS = 64L;
    private static final long MODULE_II_COPROCESSORS = 4096L;
    private static final long MODULE_III_COPROCESSORS = 262_144L;
    private static final long MODULE_IV_COPROCESSORS = 16_777_216L;

    private static final long MODULE_I_THREADS = 4L;
    private static final long MODULE_II_THREADS = 16L;
    private static final long MODULE_III_THREADS = 128L;
    private static final long MODULE_IV_THREADS = 1024L;

    private long installedModules;
    private long matrixI;
    private long matrixII;
    private long matrixIII;
    private long matrixIV;

    private long totalStorageBytes;
    private long totalCoProcessors;
    private long totalThreads;

    private int highestModuleTier;
    private boolean transcendentMode;

    public NexusMEHyperCoreMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        recalculateInternalModules();
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        resetStats();
    }

    private void resetStats() {
        installedModules = 0L;
        matrixI = 0L;
        matrixII = 0L;
        matrixIII = 0L;
        matrixIV = 0L;
        totalStorageBytes = 0L;
        totalCoProcessors = 0L;
        totalThreads = 0L;
        highestModuleTier = 0;
        transcendentMode = false;
    }

    private void recalculateInternalModules() {
        resetStats();
        if (getLevel() == null) {
            return;
        }

        Direction facing = getFrontFacing();
        Direction inward = facing.getOpposite();
        Direction right = facing.getClockWise();

        for (int depth = 1; depth <= INTERIOR_SIZE; depth++) {
            for (int y = -2; y <= 2; y++) {
                for (int x = -2; x <= 2; x++) {
                    BlockPos pos = getPos()
                            .relative(inward, depth)
                            .relative(right, x)
                            .relative(Direction.UP, y);
                    Block block = getLevel().getBlockState(pos).getBlock();
                    int moduleTier = getModuleTier(block);
                    if (moduleTier <= 0) {
                        continue;
                    }

                    installedModules++;
                    highestModuleTier = Math.max(highestModuleTier, moduleTier);
                    switch (moduleTier) {
                        case 1 -> {
                            matrixI++;
                            totalStorageBytes += MODULE_I_STORAGE;
                            totalCoProcessors += MODULE_I_COPROCESSORS;
                            totalThreads += MODULE_I_THREADS;
                        }
                        case 2 -> {
                            matrixII++;
                            totalStorageBytes += MODULE_II_STORAGE;
                            totalCoProcessors += MODULE_II_COPROCESSORS;
                            totalThreads += MODULE_II_THREADS;
                        }
                        case 3 -> {
                            matrixIII++;
                            totalStorageBytes += MODULE_III_STORAGE;
                            totalCoProcessors += MODULE_III_COPROCESSORS;
                            totalThreads += MODULE_III_THREADS;
                        }
                        case 4 -> {
                            matrixIV++;
                            totalStorageBytes += MODULE_IV_STORAGE;
                            totalCoProcessors += MODULE_IV_COPROCESSORS;
                            totalThreads += MODULE_IV_THREADS;
                        }
                        default -> {
                        }
                    }
                }
            }
        }

        transcendentMode = matrixIV == TOTAL_MODULE_SLOTS;
    }

    private int getModuleTier(Block block) {
        if (block == GTNABlocks.MATRIX_MODULE_I.get()) return 1;
        if (block == GTNABlocks.MATRIX_MODULE_II.get()) return 2;
        if (block == GTNABlocks.MATRIX_MODULE_III.get()) return 3;
        if (block == GTNABlocks.MATRIX_MODULE_IV.get()) return 4;
        return 0;
    }

    private int getComputationTier() {
        return switch (highestModuleTier) {
            case 1 -> GTValues.EV;
            case 2 -> GTValues.LuV;
            case 3 -> GTValues.UV;
            case 4 -> GTValues.UEV;
            default -> GTValues.ULV;
        };
    }

    private String getComputationTierName() {
        if (transcendentMode) {
            return "Transcendent";
        }
        if (highestModuleTier <= 0) {
            return "Offline";
        }
        return GTValues.VN[getComputationTier()];
    }

    private String formatBytes(long bytes) {
        if (transcendentMode) {
            return "∞";
        }
        if (bytes <= 0L) {
            return "0 B";
        }
        String[] units = { "B", "KiB", "MiB", "GiB", "TiB", "PiB" };
        double value = bytes;
        int unitIndex = 0;
        while (value >= 1024.0D && unitIndex < units.length - 1) {
            value /= 1024.0D;
            unitIndex++;
        }
        if (Math.abs(value - Math.rint(value)) < 0.0001D) {
            return String.format(Locale.US, "%.0f %s", value, units[unitIndex]);
        }
        return String.format(Locale.US, "%.2f %s", value, units[unitIndex]);
    }

    private String formatStat(long value) {
        if (transcendentMode) {
            return "∞";
        }
        if (value < 1_000L) {
            return Long.toString(value);
        }
        if (value < 1_000_000L) {
            return String.format(Locale.US, "%.2fK", value / 1_000.0D);
        }
        if (value < 1_000_000_000L) {
            return String.format(Locale.US, "%.2fM", value / 1_000_000.0D);
        }
        return String.format(Locale.US, "%.2fB", value / 1_000_000_000.0D);
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        var screen = new DraggableScrollableWidgetGroup(7, 4, 292, 175)
                .setBackground(GuiTextures.DISPLAY);

        screen.addWidget(new LabelWidget(4, 5, self().getBlockState().getBlock().getDescriptionId()));
        screen.addWidget(new ComponentPanelWidget(4, 17, this::addDisplayText).setMaxWidthLimit(282));

        return new ModularUI(310, 270, this, entityPlayer)
                .background(GuiTextures.BACKGROUND)
                .widget(screen)
                .widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(),
                        GuiTextures.SLOT, 74, 188, true));
    }

    @Override
    public void addDisplayText(@NotNull List<Component> textList) {
        IDisplayUIMachine.super.addDisplayText(textList);
        if (!isFormed()) {
            return;
        }

        recalculateInternalModules();

        textList.add(Component.literal("Nexus ME HyperCore").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        textList.add(Component.literal("--------------------------------").withStyle(ChatFormatting.DARK_GRAY));
        textList.add(Component.literal("Computation Tier: ")
                .append(Component.literal(getComputationTierName()).withStyle(ChatFormatting.GOLD)));
        textList.add(Component.literal("Installed Modules: ")
                .append(Component.literal(installedModules + " / " + TOTAL_MODULE_SLOTS).withStyle(ChatFormatting.GREEN)));
        textList.add(Component.literal("Storage: ")
                .append(Component.literal(formatBytes(totalStorageBytes)).withStyle(ChatFormatting.YELLOW)));
        textList.add(Component.literal("Co-Processors: ")
                .append(Component.literal(formatStat(totalCoProcessors)).withStyle(ChatFormatting.LIGHT_PURPLE)));
        textList.add(Component.literal("Threads: ")
                .append(Component.literal(formatStat(totalThreads)).withStyle(ChatFormatting.AQUA)));
        textList.add(Component.literal("Transcendent Mode: ")
                .append(Component.literal(transcendentMode ? "∞ ACTIVE" : "Inactive")
                        .withStyle(transcendentMode ? ChatFormatting.RED : ChatFormatting.GRAY)));
        textList.add(Component.literal("Matrix I: " + matrixI + "  Matrix II: " + matrixII));
        textList.add(Component.literal("Matrix III: " + matrixIII + "  Matrix IV: " + matrixIV));
    }
}
