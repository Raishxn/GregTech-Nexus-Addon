package com.raishxn.gtna.common.item;

import com.gregtechceu.gtceu.api.gui.GuiTextures;

import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

import com.raishxn.gtna.client.renderer.BlockHighlightHandler;
import com.raishxn.gtna.common.data.NexusEnergyNetwork;
import com.raishxn.gtna.utils.datastructure.Int128;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class QuantumTerminalUI {

    private final HeldItemUIFactory.HeldItemHolder holder;
    private final Player player;
    private UUID networkOwner;

    private final List<NexusEnergyNetwork.ConnectionInfo> cachedConnections = new ArrayList<>();

    public QuantumTerminalUI(HeldItemUIFactory.HeldItemHolder holder, Player player) {
        this.holder = holder;
        this.player = player;

        var stack = holder.getHeld();
        var tag = stack.getTag();
        if (tag != null && tag.hasUUID("NetworkID")) {
            this.networkOwner = tag.getUUID("NetworkID");
        } else {
            this.networkOwner = player.getUUID();
        }
    }

    public ModularUI createModularUI() {
        var screen = new DraggableScrollableWidgetGroup(7, 4, 292, 268)
                .setBackground(GuiTextures.DISPLAY);

        screen.addWidget(new ComponentPanelWidget(4, 5, this::addDisplayText)
                .setMaxWidthLimit(282)
                .clickHandler(this::handleLocateClick));

        return new ModularUI(310, 280, holder, player)
                .background(GuiTextures.BACKGROUND)
                .widget(screen);
    }

    /**
     * Handle clicks on connection locate buttons.
     * Uses GTMThings pattern: only the client-side execution sets highlight.
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

    private void addDisplayText(List<Component> textList) {
        if (player.level().isClientSide()) return;

        NexusEnergyNetwork network = NexusEnergyNetwork.get((ServerLevel) player.level());
        Int128 energy = network.getEnergy(networkOwner);
        Int128 maxCapacity = network.getMaxCapacity(networkOwner);
        boolean safeMode = network.getSafeMode(networkOwner);
        Int128 inPerTick = network.getLastInputPerTick(networkOwner);
        Int128 outPerTick = network.getLastOutputPerTick(networkOwner);

        // Matrix structural stats (from controller)
        boolean matrixFormed = network.isMatrixFormed(networkOwner);
        long totalCapacitors = network.getTotalCapacitors(networkOwner);
        int averageTier = network.getAverageTier(networkOwner);
        double efficiency = network.getEfficiency(networkOwner);
        Int128 transferLimit = network.getTransferLimit(networkOwner);

        // Calculate fill %
        double fillPercentage = 0;
        if (!maxCapacity.isZero()) {
            if (maxCapacity.compareTo(Int128.fromBigInteger(java.math.BigInteger.valueOf(100000L))) < 0) {
                fillPercentage = (double) energy.toLong() / maxCapacity.toLong();
            } else {
                fillPercentage = energy.toBigInteger().doubleValue() / maxCapacity.toBigInteger().doubleValue();
            }
        }

        // Build text bar
        int barLength = 24;
        int filled = (int) Math.round(fillPercentage * barLength);
        StringBuilder bar = new StringBuilder("§b[");
        for (int i = 0; i < barLength; i++) {
            bar.append(i < filled ? "§a█" : "§8▒");
        }
        bar.append("§b]");

        // ═══════════════════════════
        // HEADER
        // ═══════════════════════════
        textList.add(Component.literal("§b§l⚡ Quantum Network Terminal"));
        textList.add(Component.literal("§8═══════════════════════════════"));

        // --- Network Info ---
        String ownerName = player.getGameProfile().getName();
        textList.add(Component.literal("§7Network: §f" + ownerName));

        // Status
        if (safeMode) {
            textList.add(Component.literal("§7Status: §c⛔ SAFE MODE"));
        } else {
            textList.add(Component.literal("§7Status: §a✅ ONLINE"));
        }

        // Matrix formed
        textList.add(Component.literal("§7Matrix: " + (matrixFormed ? "§a✅ FORMED" : "§c✖ NOT FORMED")));

        textList.add(Component.literal("§8───────────────────────────────"));

        // ═══════════════════════════
        // STRUCTURE STATS (from controller)
        // ═══════════════════════════
        textList.add(Component.literal("§6§l⚙ Matrix Stats"));
        textList.add(Component.literal("§7Capacitors: §a" + totalCapacitors));
        textList.add(Component.literal("§7Max Capacity: §e" + maxCapacity.toHumanReadableString() + " EU"));

        String tierName = "N/A";
        if (averageTier > 0 && averageTier < com.gregtechceu.gtceu.api.GTValues.VN.length) {
            tierName = com.gregtechceu.gtceu.api.GTValues.VN[averageTier];
        }
        textList.add(Component.literal("§7Average Tier: §e" + tierName + " §7(Tier " + averageTier + ")"));
        textList.add(Component
                .literal("§7Efficiency: §d" + String.format(java.util.Locale.US, "%.1f", efficiency * 100) + "%"));
        textList.add(Component.literal("§7Transfer Limit: §6" + transferLimit.toHumanReadableString() + " EU/t"));

        boolean crossDim = averageTier >= 7;
        textList.add(Component.literal("§7Cross-Dim: " + (crossDim ? "§a✅ Enabled (ZPM+)" : "§c✖ Requires ZPM+")));

        textList.add(Component.literal("§8───────────────────────────────"));

        // ═══════════════════════════
        // ENERGY STATUS
        // ═══════════════════════════
        textList.add(Component.literal("§b§l🔋 Energy"));
        textList.add(Component.literal(bar.toString() + " §f" + String.format("%.1f%%", fillPercentage * 100.0)));
        textList.add(Component.literal(
                "§7Energy: §f" + energy.toHumanReadableString() + " / " + maxCapacity.toHumanReadableString() + " EU"));
        textList.add(Component.literal(""));

        // --- IO Stats ---
        textList.add(Component.literal("§a⬆ Avg Input:  +" + inPerTick.toHumanReadableString() + " EU/t"));
        textList.add(Component.literal("§c⬇ Avg Output: -" + outPerTick.toHumanReadableString() + " EU/t"));

        // --- Time to Empty ---
        String timeToEmpty = calculateTimeToEmpty(energy, inPerTick, outPerTick);
        textList.add(Component.literal("§7⏱ Time to Empty: §f" + timeToEmpty));

        textList.add(Component.literal("§8───────────────────────────────"));

        // ═══════════════════════════
        // CONNECTIONS
        // ═══════════════════════════
        Map<GlobalPos, NexusEnergyNetwork.ConnectionInfo> connections = network.getConnections(networkOwner);
        cachedConnections.clear();
        cachedConnections.addAll(connections.values());

        textList.add(Component.literal("§e§l📋 Connections (" + connections.size() + "):"));

        for (NexusEnergyNetwork.ConnectionInfo info : cachedConnections) {
            String dirColor = info.isInput ? "§a" : "§c";
            String dirLabel = info.isInput ? "[IN]" : "[OUT]";
            String connTierName = com.gregtechceu.gtceu.api.GTValues.VN[Math.min(info.tier,
                    com.gregtechceu.gtceu.api.GTValues.VN.length - 1)];
            String amount = (info.isInput ? "+" : "-") + info.lastTickEuTransferred.toHumanReadableString();
            String posStr = info.pos.pos().toShortString(); // "x, y, z"
            String dim = info.pos.dimension().location().toString();

            textList.add(
                    Component
                            .literal(dirColor + dirLabel + " §f" + info.amperage + "A " + connTierName + " " +
                                    info.machineType + " §7" + amount + " EU/t")
                            .withStyle(style -> style
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                            Component.literal("§ePos: " + posStr + "\n§7Dim: " + dim +
                                                    "\n§bClick [🔍] to locate")))
                                    .withColor(ChatFormatting.WHITE))
                            .append(ComponentPanelWidget.withButton(Component.literal(" §b[🔍]"), posStr)));
        }

        if (cachedConnections.isEmpty()) {
            textList.add(Component.literal("§8  No active connections."));
        }
    }

    private String calculateTimeToEmpty(Int128 energy, Int128 inPerTick, Int128 outPerTick) {
        if (energy.isZero()) return "§c0s (EMPTY)";

        // Net drain = output - input per tick
        Int128 netDrain = outPerTick.copy();
        if (netDrain.compareTo(inPerTick) <= 0) {
            return "§a∞ (Charging)";
        }
        netDrain.subtract(inPerTick);

        if (netDrain.isZero() || netDrain.isNegative()) {
            return "§a∞ (Charging)";
        }

        try {
            long drainLong = netDrain.toLong();
            if (drainLong <= 0) return "§a∞";
            long energyLong = energy.toLong();
            long ticks = energyLong / drainLong;
            return formatTickDuration(ticks);
        } catch (Exception e) {
            java.math.BigInteger energyBig = energy.toBigInteger();
            java.math.BigInteger drainBig = netDrain.toBigInteger();
            if (drainBig.signum() <= 0) return "§a∞";
            java.math.BigInteger ticks = energyBig.divide(drainBig);
            long ticksLong = ticks.min(java.math.BigInteger.valueOf(Long.MAX_VALUE)).longValue();
            return formatTickDuration(ticksLong);
        }
    }

    private String formatTickDuration(long ticks) {
        if (ticks <= 0) return "§c0s";
        long totalSeconds = ticks / 20;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        if (hours > 99999) return "§a> 99999h";
        if (hours > 0) return String.format("§f%dh %02dm %02ds", hours, minutes, seconds);
        if (minutes > 0) return String.format("§f%dm %02ds", minutes, seconds);
        return String.format("§f%ds", seconds);
    }
}
