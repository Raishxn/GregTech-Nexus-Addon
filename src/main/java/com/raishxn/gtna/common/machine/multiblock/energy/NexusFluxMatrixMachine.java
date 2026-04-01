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
import com.raishxn.gtna.utils.datastructure.Int128;

import java.util.ArrayList;
import java.util.List;
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

    public NexusFluxMatrixMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @com.lowdragmc.lowdraglib.syncdata.annotation.Persisted
    private java.util.UUID ownerUUID = null;

    public java.util.UUID getOwnerUUID() {
        return ownerUUID != null ? ownerUUID : (super.getOwnerUUID() != null ? super.getOwnerUUID() : null);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        recalculateCapacitors();
        if (getLevel() instanceof ServerLevel serverLevel && getOwnerUUID() != null) {
            // SET the capacity directly — never ADD to prevent accumulation on re-form
            NexusEnergyNetwork network = NexusEnergyNetwork.get(serverLevel);
            network.setMaxCapacity(getOwnerUUID(), maxCapacity);
            // Push structural stats to network for terminal access
            network.setMatrixStats(getOwnerUUID(), totalCapacitors, averageTier, efficiency, transferLimit, true);
        }
    }

    @Override
    public void onStructureInvalid() {
        if (getLevel() instanceof ServerLevel serverLevel && getOwnerUUID() != null) {
            // Clear capacity to zero on invalid
            NexusEnergyNetwork network = NexusEnergyNetwork.get(serverLevel);
            network.setMaxCapacity(getOwnerUUID(), Int128.ZERO());
            // Clear structural stats
            network.setMatrixStats(getOwnerUUID(), 0, 0, 0.0, Int128.ZERO(), false);
        }
        super.onStructureInvalid();
        totalCapacitors = 0;
        sumCapacities = Int128.ZERO();
        sumTiers = 0;
        maxCapacity = Int128.ZERO();
        transferLimit = Int128.ZERO();
        averageTier = 1;
        maxTier = 1;
        efficiency = 0.85;
    }

    private void recalculateCapacitors() {
        totalCapacitors = 0;
        sumCapacities = Int128.ZERO();
        sumTiers = 0;

        if (getLevel() != null) {
            BlockPos startPos = getPos();
            for (int x = -16; x <= 16; x++) {
                for (int y = -16; y <= 35; y++) {
                    for (int z = -16; z <= 16; z++) {
                        BlockPos p = startPos.offset(x, y, z);
                        BlockState bs = getLevel().getBlockState(p);
                        if (bs.getBlock() instanceof NexusCapacitorBlock capacitor) {
                            totalCapacitors++;
                            // Use Int128 to avoid long overflow on high-tier capacitors
                            sumCapacities.add(new Int128(capacitor.getUnitCapacity()));
                            sumTiers += capacitor.getTier();
                            if (capacitor.getTier() > maxTier) {
                                maxTier = capacitor.getTier();
                            }
                        }
                    }
                }
            }
        }

        if (totalCapacitors > 0) {
            var cfg = com.raishxn.gtna.config.ConfigHolder.INSTANCE.nexusFluxMatrix;

            if (cfg.useHighestTierForEfficiency) {
                averageTier = maxTier;
            } else {
                averageTier = (int) (sumTiers / totalCapacitors);
            }
            if (averageTier < 1) averageTier = 1;

            // Using simple linear summation instead of quadratic PRD scaling
            maxCapacity = sumCapacities.copy();

            double tierRatio = (averageTier - 1) / 13.0;
            double lossPercent = cfg.baseLossPercent * (1.0 - tierRatio);
            efficiency = 1.0 - (lossPercent / 100.0);

            long transferBase = 2000L * (long) Math.pow(4, averageTier - 1);
            transferLimit = new Int128(transferBase);
            if (averageTier >= 14) {
                try {
                    transferLimit = Int128.fromString(cfg.maxTransferTierMAX);
                } catch (Exception e) {
                    transferLimit = Int128.fromString("500000000000000000000000");
                }
            }
        }
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

    // ── UI — Expanded to 240x280 for better connections view ──

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
                .widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(),
                        GuiTextures.SLOT, 74, 188, true));
    }

    /**
     * Handle clicks on connection locate buttons.
     * Uses GTMThings pattern: the click handler runs on BOTH client and server,
     * but only the client-side execution sets the highlight static fields.
     */
    private void handleLocateClick(String componentData, ClickData clickData) {
        if (componentData == null) return;

        // Only process on the client side — set the highlight directly
        if (clickData.isRemote) {
            BlockHighlightHandler.highlightTicks = 100; // ~5 seconds
            String[] parts = componentData.split(", ");
            if (parts.length == 3) {
                try {
                    BlockHighlightHandler.highlightPos = new BlockPos(
                            Integer.parseInt(parts[0]),
                            Integer.parseInt(parts[1]),
                            Integer.parseInt(parts[2]));
                } catch (NumberFormatException ignored) {}
            }
        }
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        IDisplayUIMachine.super.addDisplayText(textList);
        if (isFormed()) {
            textList.add(Component.literal("§b§l⚡ Nexus Flux Matrix"));
            textList.add(Component.literal("§8────────────────────────────────"));
            textList.add(Component.literal("§7Capacitors: §a" + totalCapacitors));
            textList.add(Component.literal("§7Max Capacity: §e" + maxCapacity.toHumanReadableString() + " EU"));

            String tierName = GTValues.VN[Math.min(averageTier, GTValues.VN.length - 1)];
            textList.add(Component.literal("§7Average Tier: §e" + tierName + " §7(Tier " + averageTier + ")"));
            textList.add(Component
                    .literal("§7Efficiency: §d" + String.format(java.util.Locale.US, "%.1f", efficiency * 100) + "%"));
            textList.add(Component.literal("§7Transfer Limit: §6" + transferLimit.toHumanReadableString() + " EU/t"));

            boolean crossDim = averageTier >= 4; // Tier 4 = EV
            textList.add(Component.literal("§7Cross-Dim: " + (crossDim ? "§a✅ Enabled" : "§c✖ Requires EV+")));

            if (getOwnerUUID() != null && getLevel() instanceof ServerLevel serverLevel) {
                textList.add(Component.literal("§8────────────────────────────────"));

                NexusEnergyNetwork network = NexusEnergyNetwork.get(serverLevel);
                Int128 energy = network.getEnergy(getOwnerUUID());
                // Use local maxCapacity for consistency — this is the single source of truth
                Int128 maxCap = maxCapacity;
                boolean safeMode = network.getSafeMode(getOwnerUUID());
                Int128 inPerTick = network.getLastInputPerTick(getOwnerUUID());
                Int128 outPerTick = network.getLastOutputPerTick(getOwnerUUID());

                // Status
                textList.add(Component.literal("§7Status: " + (safeMode ? "§c⛔ SAFE MODE" : "§a✅ ONLINE")));

                // Energy bar
                double fill = 0;
                if (!maxCap.isZero()) {
                    try {
                        fill = energy.toBigInteger().doubleValue() / maxCap.toBigInteger().doubleValue();
                    } catch (Exception e) {
                        fill = 0;
                    }
                }
                int barLen = 20;
                int filledCount = (int) Math.round(fill * barLen);
                StringBuilder bar = new StringBuilder("§b[");
                for (int i = 0; i < barLen; i++) {
                    bar.append(i < filledCount ? "§a█" : "§8▒");
                }
                bar.append("§b]");
                textList.add(Component.literal(bar.toString() + " §f" + String.format("%.1f%%", fill * 100.0)));
                textList.add(Component.literal("§7Energy: §f" + energy.toHumanReadableString() + " / " +
                        maxCap.toHumanReadableString() + " EU"));

                // IO
                textList.add(Component.literal("§a⬆ Input: +" + inPerTick.toHumanReadableString() + " EU/t"));
                textList.add(Component.literal("§c⬇ Output: -" + outPerTick.toHumanReadableString() + " EU/t"));

                // Connections
                Map<GlobalPos, NexusEnergyNetwork.ConnectionInfo> connections = network.getConnections(getOwnerUUID());
                cachedConnections.clear();
                cachedConnections.addAll(connections.values());

                textList.add(Component.literal("§8────────────────────────────────"));
                textList.add(Component.literal("§e§l📋 Connections (" + connections.size() + "):"));
                textList.add(Component.literal("§7(Click any connection to locate it)"));

                for (NexusEnergyNetwork.ConnectionInfo info : cachedConnections) {
                    String dirColor = info.isInput ? "§a" : "§c";
                    String dirLabel = info.isInput ? "[IN]" : "[OUT]";
                    String connTierName = GTValues.VN[Math.min(info.tier, GTValues.VN.length - 1)];
                    String amount = (info.isInput ? "+" : "-") + info.lastTickEuTransferred.toHumanReadableString();
                    String posStr = info.pos.pos().toShortString(); // "x, y, z"
                    String dim = info.pos.dimension().location().toString();

                    textList.add(Component
                            .literal(dirColor + dirLabel + " §f" + info.amperage + "A " + connTierName + " " +
                                    info.machineType + " §7" + amount + " EU/t")
                            .withStyle(s -> s
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                            Component.literal("§ePos: " + posStr + "\n§7Dim: " + dim +
                                                    "\n§bClick [🔍] to locate")))
                                    .withColor(ChatFormatting.WHITE))
                            .append(ComponentPanelWidget.withButton(Component.literal(" §b[🔍]"), posStr)));
                }
            }
        }
    }
}
