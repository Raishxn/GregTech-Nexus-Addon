package com.raishxn.gtna.common.machine.multiblock.energy;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.UITemplate;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

import com.raishxn.gtna.client.renderer.BlockHighlightHandler;
import com.raishxn.gtna.common.block.NexusCapacitorBlock;
import com.raishxn.gtna.common.data.NexusEnergyNetwork;
import com.raishxn.gtna.config.ConfigHolder;
import com.raishxn.gtna.config.GTNABalance;
import com.raishxn.gtna.utils.datastructure.Int128;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class NexusFluxMatrixMachine extends WorkableMultiblockMachine implements IDisplayUIMachine {

    private long totalCapacitors = 0;
    private Int128 sumCapacities = Int128.ZERO();
    private int sumTiers = 0;

    private Int128 maxCapacity = Int128.ZERO();
    private int averageTier = 1;
    private int maxTier = 1;
    private Int128 transferLimit = Int128.ZERO();
    private double efficiency = 0.85;

    private final List<NexusEnergyNetwork.ConnectionInfo> cachedConnections = new ArrayList<>();

    @com.lowdragmc.lowdraglib.syncdata.annotation.Persisted
    private java.util.UUID ownerUUID = null;

    public NexusFluxMatrixMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    public java.util.UUID getOwnerUUID() {
        return ownerUUID != null ? ownerUUID : (super.getOwnerUUID() != null ? super.getOwnerUUID() : null);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        recalculateCapacitors();
        if (getLevel() instanceof ServerLevel serverLevel && getOwnerUUID() != null) {
            NexusEnergyNetwork network = NexusEnergyNetwork.get(serverLevel);
            network.setMaxCapacity(getOwnerUUID(), maxCapacity);
            network.setMatrixStats(getOwnerUUID(), totalCapacitors, averageTier, efficiency, transferLimit, true);
        }
    }

    @Override
    public void onStructureInvalid() {
        if (getLevel() instanceof ServerLevel serverLevel && getOwnerUUID() != null) {
            NexusEnergyNetwork network = NexusEnergyNetwork.get(serverLevel);
            network.setMaxCapacity(getOwnerUUID(), Int128.ZERO());
            network.setMatrixStats(getOwnerUUID(), 0, 0, 0.0, Int128.ZERO(), false);
        }
        super.onStructureInvalid();
        totalCapacitors = 0;
        sumCapacities = Int128.ZERO();
        sumTiers = 0;
        maxCapacity = Int128.ZERO();
        averageTier = 1;
        maxTier = 1;
        transferLimit = Int128.ZERO();
        efficiency = 0.85;
    }

    private void recalculateCapacitors() {
        totalCapacitors = 0;
        sumCapacities = Int128.ZERO();
        sumTiers = 0;
        maxTier = 1;

        if (getLevel() != null) {
            BlockPos startPos = getPos();
            for (int x = -16; x <= 16; x++) {
                for (int y = -16; y <= 35; y++) {
                    for (int z = -16; z <= 16; z++) {
                        BlockPos pos = startPos.offset(x, y, z);
                        BlockState blockState = getLevel().getBlockState(pos);
                        if (blockState.getBlock() instanceof NexusCapacitorBlock capacitor) {
                            totalCapacitors++;
                            long configuredCapacity = GTNABalance
                                    .getNexusCapacitorCapacity(capacitor.getTier(), capacitor.getUnitCapacity());
                            sumCapacities.add(new Int128(configuredCapacity));
                            sumTiers += capacitor.getTier();
                            if (capacitor.getTier() > maxTier) {
                                maxTier = capacitor.getTier();
                            }
                        }
                    }
                }
            }
        }

        if (totalCapacitors <= 0) {
            maxCapacity = Int128.ZERO();
            averageTier = 1;
            transferLimit = Int128.ZERO();
            efficiency = 0.85;
            return;
        }

        var machineCfg = ConfigHolder.INSTANCE.machines.nexusFluxMatrix;
        var balanceCfg = GTNABalance.getNexusFluxMatrix();

        averageTier = machineCfg.useHighestTierForEfficiency ? maxTier : (int) (sumTiers / totalCapacitors);
        if (averageTier < 1) averageTier = 1;

        maxCapacity = sumCapacities.copy();

        double tierRatio = (averageTier - 1) / 13.0;
        double lossPercent = balanceCfg.efficiency.baseLossPercentAtLV * (1.0 - tierRatio);
        efficiency = 1.0 - (lossPercent / 100.0);
        efficiency = Math.max(balanceCfg.efficiency.minimumEfficiency,
                Math.min(balanceCfg.efficiency.maximumEfficiency, efficiency));

        long fallbackTransfer = 2000L * (long) Math.pow(4, Math.max(0, averageTier - 1));
        transferLimit = Int128.fromString(
                GTNABalance.getNexusTransferLimit(averageTier, Long.toString(fallbackTransfer)),
                new Int128(fallbackTransfer));
    }

    public Int128 getMaxCapacity() {
        return maxCapacity;
    }

    public int getAverageTier() {
        return averageTier;
    }

    public Int128 getTransferLimit() {
        return transferLimit;
    }

    public double getEfficiency() {
        return efficiency;
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        var screen = new DraggableScrollableWidgetGroup(7, 4, 292, 175)
                .setBackground(GuiTextures.DISPLAY);

        screen.addWidget(new LabelWidget(4, 5, self().getBlockState().getBlock().getDescriptionId()));
        screen.addWidget(new ComponentPanelWidget(4, 17, this::addDisplayText)
                .setMaxWidthLimit(282)
                .clickHandler(this::handleLocateClick));

        return new ModularUI(310, 270, this, entityPlayer)
                .background(GuiTextures.BACKGROUND)
                .widget(screen)
                .widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(), GuiTextures.SLOT, 74, 188, true));
    }

    private void handleLocateClick(String componentData, ClickData clickData) {
        if (componentData == null || !clickData.isRemote) return;

        BlockHighlightHandler.highlightTicks = 100;
        String[] parts = componentData.split(", ");
        if (parts.length != 3) return;

        try {
            BlockHighlightHandler.highlightPos = new BlockPos(
                    Integer.parseInt(parts[0]),
                    Integer.parseInt(parts[1]),
                    Integer.parseInt(parts[2]));
        } catch (NumberFormatException ignored) {}
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        IDisplayUIMachine.super.addDisplayText(textList);
        if (!isFormed()) return;

        textList.add(Component.literal("\u00a7b\u00a7lNexus Flux Matrix"));
        textList.add(Component.literal("\u00a78--------------------------------"));
        textList.add(Component.literal("\u00a77Capacitors: \u00a7a" + totalCapacitors));
        textList.add(Component.literal("\u00a77Max Capacity: \u00a7e" + maxCapacity.toHumanReadableString() + " EU"));

        String tierName = GTValues.VN[Math.min(averageTier, GTValues.VN.length - 1)];
        textList.add(
                Component.literal("\u00a77Average Tier: \u00a7e" + tierName + " \u00a77(Tier " + averageTier + ")"));
        textList.add(Component.literal("\u00a77Efficiency: \u00a7d" +
                String.format(Locale.US, "%.1f", efficiency * 100) + "%"));
        textList.add(Component.literal("\u00a77Transfer Limit: \u00a76" +
                transferLimit.toHumanReadableString() + " EU/t"));

        boolean crossDim = GTNABalance.isNexusCrossDimensionEnabled(averageTier);
        textList.add(Component.literal("\u00a77Cross-Dim: " +
                (crossDim ? "\u00a7aEnabled" : "\u00a7cRequires EV+")));

        if (getOwnerUUID() == null || !(getLevel() instanceof ServerLevel serverLevel)) return;

        textList.add(Component.literal("\u00a78--------------------------------"));

        NexusEnergyNetwork network = NexusEnergyNetwork.get(serverLevel);
        Int128 energy = network.getEnergy(getOwnerUUID());
        Int128 maxCap = maxCapacity;
        boolean safeMode = network.getSafeMode(getOwnerUUID());
        Int128 inPerTick = network.getLastInputPerTick(getOwnerUUID());
        Int128 outPerTick = network.getLastOutputPerTick(getOwnerUUID());

        textList.add(Component.literal("\u00a77Status: " + (safeMode ? "\u00a7cSAFE MODE" : "\u00a7aONLINE")));

        double fill = 0.0;
        if (!maxCap.isZero()) {
            try {
                fill = energy.toBigInteger().doubleValue() / maxCap.toBigInteger().doubleValue();
            } catch (Exception ignored) {
                fill = 0.0;
            }
        }

        int barLength = 20;
        int filledCount = (int) Math.round(fill * barLength);
        StringBuilder bar = new StringBuilder("\u00a7b[");
        for (int i = 0; i < barLength; i++) {
            bar.append(i < filledCount ? "\u00a7a|" : "\u00a78|");
        }
        bar.append("\u00a7b]");

        textList.add(Component.literal(bar + " \u00a7f" + String.format(Locale.US, "%.1f%%", fill * 100.0)));
        textList.add(Component.literal("\u00a77Energy: \u00a7f" + energy.toHumanReadableString() + " / " +
                maxCap.toHumanReadableString() + " EU"));
        textList.add(Component.literal("\u00a7aInput: +" + inPerTick.toHumanReadableString() + " EU/t"));
        textList.add(Component.literal("\u00a7cOutput: -" + outPerTick.toHumanReadableString() + " EU/t"));

        Map<GlobalPos, NexusEnergyNetwork.ConnectionInfo> connections = network.getConnections(getOwnerUUID());
        cachedConnections.clear();
        cachedConnections.addAll(connections.values());

        textList.add(Component.literal("\u00a78--------------------------------"));
        textList.add(Component.literal("\u00a7eConnections (" + connections.size() + "):"));
        textList.add(Component.literal("\u00a77Click a connection to locate it"));

        for (NexusEnergyNetwork.ConnectionInfo info : cachedConnections) {
            String directionColor = info.isInput ? "\u00a7a" : "\u00a7c";
            String directionLabel = info.isInput ? "[IN]" : "[OUT]";
            String connectionTier = GTValues.VN[Math.min(info.tier, GTValues.VN.length - 1)];
            String amount = (info.isInput ? "+" : "-") + info.lastTickEuTransferred.toHumanReadableString();
            String pos = info.pos.pos().toShortString();
            String dimension = info.pos.dimension().location().toString();

            textList.add(Component.literal(directionColor + directionLabel + " \u00a7f" + info.amperage + "A " +
                    connectionTier + " " + info.machineType + " \u00a77" + amount + " EU/t")
                    .withStyle(style -> style
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    Component.literal("\u00a7ePos: " + pos + "\n\u00a77Dim: " + dimension +
                                            "\n\u00a7bClick [Locate]")))
                            .withColor(ChatFormatting.WHITE))
                    .append(ComponentPanelWidget.withButton(Component.literal(" \u00a7b[Locate]"), pos)));
        }
    }
}
