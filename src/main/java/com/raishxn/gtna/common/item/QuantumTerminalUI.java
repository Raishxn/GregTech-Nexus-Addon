package com.raishxn.gtna.common.item;

import com.gregtechceu.gtceu.api.gui.GuiTextures;

import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;

import net.minecraft.ChatFormatting;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

import com.raishxn.gtna.config.GTNABalance;
import com.raishxn.gtna.network.GTNANetworkHandler;
import com.raishxn.gtna.network.packet.CLocateConnectionPacket;
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

        // Only process on the client side — send a C2S packet so the server
        // echoes an S2C highlight packet back to this player.
        // Button data is encoded as "x, y, z|dimension" to support cross-dim connections.
        if (clickData.isRemote) {
            String[] dimSplit = componentData.split("\\|", 2);
            if (dimSplit.length == 2) {
                String[] parts = dimSplit[0].split(", ");
                if (parts.length == 3) {
                    try {
                        int x = Integer.parseInt(parts[0]);
                        int y = Integer.parseInt(parts[1]);
                        int z = Integer.parseInt(parts[2]);
                        GTNANetworkHandler.INSTANCE.sendToServer(
                                new CLocateConnectionPacket(x, y, z, dimSplit[1]));
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
    }

    private void addDisplayText(List<Component> textList) {
        if (player.level().isClientSide()) return;
        if (!(player.level() instanceof ServerLevel serverLevel)) return;

        NexusEnergyNetwork network = NexusEnergyNetwork.get(serverLevel);
        if (network == null) {
            textList.add(Component.literal("§cNetwork data unavailable. Try reopening."));
            return;
        }

        Int128 energy = network.getEnergy(networkOwner);
        Int128 maxCapacity = network.getMaxCapacity(networkOwner);
        boolean safeMode = network.getSafeMode(networkOwner);
        Int128 inPerTick = network.getLastInputPerTick(networkOwner);
        Int128 outPerTick = network.getLastOutputPerTick(networkOwner);

        // Matrix structural stats (aggregated across all controllers on this network)
        boolean matrixFormed = network.isMatrixFormed(networkOwner);
        int matrixCount      = network.getMatrixCount(networkOwner);
        long totalCapacitors = network.getTotalCapacitors(networkOwner);
        int averageTier = network.getAverageTier(networkOwner);
        double efficiency = network.getEfficiency(networkOwner);
        Int128 transferLimit = network.getTransferLimit(networkOwner);

        // Defensive: null Int128 values should not occur but catch data corruption
        if (energy == null || maxCapacity == null || inPerTick == null
                || outPerTick == null || transferLimit == null) {
            textList.add(Component.literal("§cNetwork state error. Relog or restart the server."));
            return;
        }

        // Show a helpful message when no network has ever been configured
        if (!matrixFormed && maxCapacity.isZero() && network.getConnections(networkOwner).isEmpty()) {
            textList.add(Component.literal("§b§l⚡ Quantum Network Terminal"));
            textList.add(Component.literal("§8═════════════════════════════════"));
            textList.add(Component.literal("§7Network: §f" + player.getGameProfile().getName()));
            textList.add(Component.literal("§8─────────────────────────────────"));
            textList.add(Component.literal("§eNo Nexus Flux Matrix linked."));
            textList.add(Component.literal("§7Form a Nexus Flux Matrix controller to"));
            textList.add(Component.literal("§7initialize your energy network."));
            return;
        }

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

        // Status: OFFLINE when no matrix is active; SAFE MODE when low; ONLINE otherwise
        if (!matrixFormed) {
            textList.add(Component.literal("§7Status: §8⚫ OFFLINE"));
        } else if (safeMode) {
            textList.add(Component.literal("§7Status: §c⛔ SAFE MODE"));
        } else {
            textList.add(Component.literal("§7Status: §a✅ ONLINE"));
        }

        // Matrix formed
        textList.add(Component.literal("§7Matrix: " + (matrixFormed ? "§a✅ FORMED" : "§c✖ MISSING")));

        textList.add(Component.literal("§8───────────────────────────────"));

        // ═══════════════════════════
        // STRUCTURE STATS (from controller)
        // ═══════════════════════════
        // Show the number of contributing matrices when more than one is registered.
        String matrixHeader = (matrixCount > 1)
                ? "§6§l⚙ Matrix Stats (" + matrixCount + " matrices)"
                : "§6§l⚙ Matrix Stats";
        textList.add(Component.literal(matrixHeader));
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

        // Use the same config-driven threshold as NexusFluxMatrixMachine
        // so both GUIs agree on when cross-dimensional linking is unlocked.
        boolean crossDim = GTNABalance.isNexusCrossDimensionEnabled(averageTier);
        textList.add(Component.literal("§7Cross-Dim: " + (crossDim ? "§a✅ Enabled" : "§c✖ Requires ZPM+")));

        // Per-matrix breakdown — only shown when multiple controllers share this Network ID
        if (matrixCount > 1) {
            java.util.Map<GlobalPos, NexusEnergyNetwork.MatrixRecord> matrixMap =
                    network.getMatrices(networkOwner);
            textList.add(Component.literal("§8  ── Per-matrix breakdown ──"));
            int mIdx = 1;
            for (java.util.Map.Entry<GlobalPos, NexusEnergyNetwork.MatrixRecord> e
                    : matrixMap.entrySet()) {
                GlobalPos gp = e.getKey();
                NexusEnergyNetwork.MatrixRecord mr = e.getValue();
                String mDim = gp.dimension().location().getPath();
                String mPos = gp.pos().getX() + "," + gp.pos().getY() + "," + gp.pos().getZ();
                String mTier = (mr.averageTier > 0
                        && mr.averageTier < com.gregtechceu.gtceu.api.GTValues.VN.length)
                        ? com.gregtechceu.gtceu.api.GTValues.VN[mr.averageTier] : "?";
                textList.add(Component.literal(
                        "§7 §a#" + mIdx + " §8[§f" + mPos + "§8] §7" + mDim));
                textList.add(Component.literal(
                        "§7   Caps: §a" + mr.totalCapacitors
                                + " §7Tier: §e" + mTier
                                + " §7Cap: §6" + mr.maxCapacity.toHumanReadableString() + " EU"));
                // Capacity-weighted attributed energy for this matrix
                Int128 mAttrEnergy = Int128.ZERO();
                if (!maxCapacity.isZero() && !mr.maxCapacity.isZero()) {
                    try {
                        java.math.BigDecimal mRatio = new java.math.BigDecimal(mr.maxCapacity.toBigInteger())
                                .divide(new java.math.BigDecimal(maxCapacity.toBigInteger()),
                                        10, java.math.RoundingMode.HALF_UP);
                        java.math.BigInteger mAttrBig = new java.math.BigDecimal(energy.toBigInteger())
                                .multiply(mRatio)
                                .setScale(0, java.math.RoundingMode.HALF_UP)
                                .toBigInteger();
                        mAttrEnergy = Int128.fromBigInteger(mAttrBig);
                        if (mAttrEnergy.compareTo(mr.maxCapacity) > 0)
                            mAttrEnergy = mr.maxCapacity.copy();
                    } catch (Exception ignored) {}
                }
                textList.add(Component.literal(
                        "§7   Energy: §f" + mAttrEnergy.toHumanReadableString()
                                + " §7/ §6" + mr.maxCapacity.toHumanReadableString() + " EU"));
                mIdx++;
            }
        }

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
        textList.add(Component.literal("§7⏱ Time to Empty: " + timeToEmpty));

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
                                            Component.literal("§ePos: " + posStr + "\n\n§7Dim: " + dim +
                                                    "\n\n§bClick [🔍] to locate")))
                                    .withColor(ChatFormatting.WHITE))
                            .append(ComponentPanelWidget.withButton(Component.literal(" §b[🔍]"), posStr + "|" + dim)));
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

        if (hours > 99999) return "§c> 99999h";
        if (hours > 0) return String.format("§c%dh %02dm %02ds", hours, minutes, seconds);
        if (minutes > 0) return String.format("§c%dm %02ds", minutes, seconds);
        return String.format("§c%ds", seconds);
    }
}
