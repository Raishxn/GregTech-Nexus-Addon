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

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

import com.raishxn.gtna.common.block.NexusCapacitorBlock;
import com.raishxn.gtna.common.data.GTNABlocks;
import com.raishxn.gtna.common.data.NexusEnergyNetwork;
import com.raishxn.gtna.config.ConfigHolder;
import com.raishxn.gtna.config.GTNABalance;
import com.raishxn.gtna.utils.datastructure.Int128;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;

public class NexusFluxMatrixMachine extends WorkableMultiblockMachine implements IDisplayUIMachine {

    @com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced
    private long totalCapacitors = 0;
    private Int128 sumCapacities = Int128.ZERO();
    private int sumTiers = 0;

    private Int128 maxCapacity = Int128.ZERO();
    @com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced
    private int averageTier = 1;
    private int maxTier = 1;
    private Int128 transferLimit = Int128.ZERO();
    @com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced
    private double efficiency = 0.85;

    @com.lowdragmc.lowdraglib.syncdata.annotation.Persisted
    private java.util.UUID ownerUUID = null;

    public NexusFluxMatrixMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (this.ownerUUID == null && super.getOwnerUUID() != null) {
            this.ownerUUID = super.getOwnerUUID();
        }
    }

    public java.util.UUID getOwnerUUID() {
        return ownerUUID != null ? ownerUUID : (super.getOwnerUUID() != null ? super.getOwnerUUID() : null);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        if (this.ownerUUID == null && super.getOwnerUUID() != null) {
            this.ownerUUID = super.getOwnerUUID();
        }
        recalculateCapacitors();
        if (getLevel() instanceof ServerLevel serverLevel && getOwnerUUID() != null) {
            NexusEnergyNetwork network = NexusEnergyNetwork.get(serverLevel);
            GlobalPos controllerPos = GlobalPos.of(serverLevel.dimension(), getPos());
            network.registerMatrix(getOwnerUUID(), controllerPos,
                    new NexusEnergyNetwork.MatrixRecord(totalCapacitors, averageTier, efficiency,
                            transferLimit, maxCapacity));
        }
    }

    @Override
    public void onStructureInvalid() {
        if (getLevel() instanceof ServerLevel serverLevel && getOwnerUUID() != null) {
            NexusEnergyNetwork network = NexusEnergyNetwork.get(serverLevel);
            GlobalPos controllerPos = GlobalPos.of(serverLevel.dimension(), getPos());
            network.unregisterMatrix(getOwnerUUID(), controllerPos);
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

    /**
     * Called by GTCEu when the machine block entity is unloaded -- both on chunk
     * unload and on permanent block removal.  We distinguish the two cases by
     * inspecting the block state at this position: Minecraft sets the block to
     * AIR *before* calling setRemoved() / onUnload(), so an air state here means
     * the controller was physically broken.  During a normal chunk unload the NFM
     * block state is still present, so we leave the matrix entry intact.
     */
    @Override
    public void onUnload() {
        // Unregister only when the controller block itself has been physically
        // removed. Minecraft replaces the block with whatever is there (typically
        // air) before calling setRemoved() / onUnload(), so if the block state
        // at this position is no longer the NFM controller block the player (or
        // some other mechanism) has broken it.  During a normal chunk-unload the
        // NFM block state is still present, so we leave the matrix entry intact.
        if (getLevel() instanceof ServerLevel serverLevel && getOwnerUUID() != null
                && !serverLevel.getBlockState(getPos()).is(getDefinition().getBlock())) {
            NexusEnergyNetwork network = NexusEnergyNetwork.get(serverLevel);
            GlobalPos controllerPos = GlobalPos.of(serverLevel.dimension(), getPos());
            network.unregisterMatrix(getOwnerUUID(), controllerPos);
        }
        super.onUnload();
    }

    private void recalculateCapacitors() {
        totalCapacitors = 0;
        sumCapacities = Int128.ZERO();
        sumTiers = 0;
        maxTier = 1;

        // Only scan on the server — client block-states may be stale/unloaded.
        if (!(getLevel() instanceof ServerLevel scanLevel)) return;

        // Derive local axes from the controller's facing direction.
        // Pattern start(charDir=UP, rowDir=RIGHT, aisleDir=BACK):
        //   charDir  = UP    : chars 0..6 go from bottom (0) to top (6)
        //   rowDir   = RIGHT : rows  0..6 span E/W; controller centred at rowIdx=3
        //                      row offsets: (rowIdx-3)*localRight
        //                      row 0 => localLeft*3 (east), row 6 => localRight*3 (west)
        //   aisleDir = BACK  : aisles stack northward (behind a south-facing controller)
        // Controller at (charIdx=0, rowIdx=3, aisleIdx=0):
        //   UP=0    => controller sits at the very bottom of the structure
        //   RIGHT=3 => centred E/W (3 blocks east, 3 blocks west)
        //   BACK=0  => south end-cap; body extends BACK (northward) from here
        // RotationState.NON_Y_AXIS guarantees getFrontFacing() is always horizontal.
        Direction localFront = getFrontFacing();
        Direction localUp    = Direction.UP;
        Direction localRight = localFront.getClockWise();
        Direction localLeft  = localFront.getCounterClockWise();
        Direction localBack  = localFront.getOpposite();
        net.minecraft.world.level.block.Block borosilicateGlass =
                GTNABlocks.BOROSILICATE_GLASS_BLOCK.get();

        // Probe the variable DEPTH axis (aisleDir = localBack direction).
        // For each candidate inner layer w (aisleIdx=w), two 'C' glass blocks
        // must exist at the bottom of the east-side and west-side glass walls:
        //   eastGlass: charIdx=0, rowIdx=1, aisleIdx=w
        //              worldPos = controllerPos + RIGHT*(1-3) + BACK*w
        //                       = controllerPos + localLeft*2 + localBack*w
        //   westGlass: charIdx=0, rowIdx=5, aisleIdx=w
        //              worldPos = controllerPos + RIGHT*(5-3) + BACK*w
        //                       = controllerPos + localRight*2 + localBack*w
        // The north end-cap is all steel ('A'), so the first missing glass
        // terminates the probe, preventing overlap with adjacent NFMs.
        int innerWidthCount = 0;
        for (int w = 1; w <= 29; w++) {
            BlockPos eastGlass = getPos().relative(localLeft,  2).relative(localBack, w);
            BlockPos westGlass = getPos().relative(localRight, 2).relative(localBack, w);
            boolean glass1 = scanLevel.getBlockState(eastGlass).getBlock() == borosilicateGlass;
            boolean glass2 = scanLevel.getBlockState(westGlass).getBlock() == borosilicateGlass;
            if (glass1 && glass2) {
                innerWidthCount = w;
            } else {
                break;
            }
        }
        if (innerWidthCount == 0) return;

        // Compute the explicit interior corners (isolation boundary).
        //
        // The probe above already guarantees this NFM's scan volume cannot
        // overlap an adjacent NFM's interior: the probe stops at the first
        // non-glass block, which is always a steel end-cap (pattern 'A').
        // Two adjacent NFMs therefore share that steel wall and the probe
        // for each stops AT the steel, not THROUGH it.  We also make the
        // bounds explicit here so that any future change to the probe loop
        // cannot silently extend the scan into a neighbour's volume.
        //
        // Interior 'D' slots: charIdx=1..5, rowIdx=1..5, aisleIdx=1..N
        //   rowIdx  1..5 => RIGHT*(1-3)..RIGHT*(5-3) = localLeft*2..localRight*2
        //   aisleIdx 1..N => localBack*1..localBack*N
        //   charIdx  1..5 => localUp*1..localUp*5
        // cornerA: controllerPos + localLeft*2  + localUp*1 + localBack*1
        // cornerB: controllerPos + localRight*2 + localUp*5 + localBack*N
        BlockPos cornerA = getPos()
                .relative(localLeft,  2)
                .relative(localUp,    1)
                .relative(localBack,  1);
        BlockPos cornerB = getPos()
                .relative(localRight, 2)
                .relative(localUp,    5)
                .relative(localBack,  innerWidthCount);
        // Canonicalise min/max so betweenClosed works for any facing direction.
        BlockPos scanMin = new BlockPos(
                Math.min(cornerA.getX(), cornerB.getX()),
                Math.min(cornerA.getY(), cornerB.getY()),
                Math.min(cornerA.getZ(), cornerB.getZ()));
        BlockPos scanMax = new BlockPos(
                Math.max(cornerA.getX(), cornerB.getX()),
                Math.max(cornerA.getY(), cornerB.getY()),
                Math.max(cornerA.getZ(), cornerB.getZ()));

        // Scan the interior using the explicitly bounded range.
        for (BlockPos pos : BlockPos.betweenClosed(scanMin, scanMax)) {
            BlockState blockState = scanLevel.getBlockState(pos);
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

        // Capacity formula (spec): Total Capacity = Σ(unitCapacity) × N / 2
        // where N = totalCapacitors.  This is quadratic in N because both
        // sumCapacities and the multiplier grow with N, rewarding denser builds.
        // Example: 750 MAX-tier blocks → (5E18 × 750) × 750 / 2 ≈ 1.406 Yotta EU.
        maxCapacity = sumCapacities.copy().multiply((long) totalCapacitors).divide(2L);

        // Loss formula (spec): loss% = baseLossPercentAtLV × (1 − (averageTier−1) / 13)
        // = 15% at LV (tier 1, index 0) down to 0% at MAX (tier 14, index 13),
        // clamped to [minimumEfficiency, maximumEfficiency] from config.
        double tierRatio = (averageTier - 1) / 13.0;
        double lossPercent = balanceCfg.efficiency.baseLossPercentAtLV * (1.0 - tierRatio);
        efficiency = 1.0 - (lossPercent / 100.0);
        efficiency = Math.max(balanceCfg.efficiency.minimumEfficiency,
                Math.min(balanceCfg.efficiency.maximumEfficiency, efficiency));

        // Int128.fromString correctly handles the MAX-tier transfer limit
        // "500000000000000000000000" (24 digits): strings longer than 18 digits
        // bypass Long.parseLong and use the digit-by-digit Int128 multiply loop,
        // so the full 500 Z EU/t value is parsed without silent truncation.
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

        screen.addWidget(new ComponentPanelWidget(4, 5, this::addDisplayText)
                .setMaxWidthLimit(282));

        return new ModularUI(310, 270, this, entityPlayer)
                .background(GuiTextures.BACKGROUND)
                .widget(screen)
                .widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(), GuiTextures.SLOT, 74, 188, true));
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        IDisplayUIMachine.super.addDisplayText(textList);
        if (!isFormed()) return;
        // LDLib's ComponentPanelWidget always calls addDisplayText server-side
        // via detectAndSendChanges() and sends the resulting Component list to
        // the client as a packet.  The guard below is a defensive fallback for
        // any code path that invokes this method client-side (e.g. fast-open
        // before the first server tick): in that case we suppress the custom
        // lines and let IDisplayUIMachine's default output stand.
        // The simple numeric fields are also annotated @DescSynced above so
        // the client always has the most recent server value available.
        if (!(getLevel() instanceof ServerLevel)) return;

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
                (crossDim ? "\u00a7aEnabled" : "\u00a7cRequires ZPM+")));

        if (getOwnerUUID() == null || !(getLevel() instanceof ServerLevel serverLevel)) return;

        textList.add(Component.literal("\u00a78--------------------------------"));

        NexusEnergyNetwork network = NexusEnergyNetwork.get(serverLevel);
        if (network == null) {
            textList.add(Component.literal("\u00a7cNetwork data unavailable."));
            return;
        }

        Int128 networkEnergy = network.getEnergy(getOwnerUUID());
        Int128 networkMaxCap = network.getMaxCapacity(getOwnerUUID());
        boolean safeMode    = network.getSafeMode(getOwnerUUID());
        Int128 networkInput  = network.getLastInputPerTick(getOwnerUUID());
        Int128 networkOutput = network.getLastOutputPerTick(getOwnerUUID());
        int matrixCount = Math.max(1, network.getMatrixCount(getOwnerUUID()));

        if (networkEnergy == null || networkInput == null || networkOutput == null) {
            textList.add(Component.literal("\u00a7cNetwork state error."));
            return;
        }

        textList.add(Component.literal("\u00a77Status: " + (safeMode ? "\u00a7cSAFE MODE" : "\u00a7aONLINE")));

        // --- Capacity-weighted energy attribution ---
        // ratio = thisMaxCap / totalNetworkMaxCap  (falls back to 1.0 when single-matrix)
        double ratio = 1.0;
        if (!networkMaxCap.isZero() && !maxCapacity.isZero()) {
            try {
                ratio = maxCapacity.toBigInteger().doubleValue()
                        / networkMaxCap.toBigInteger().doubleValue();
                ratio = Math.max(0.0, Math.min(1.0, ratio));
            } catch (Exception ignored) {}
        }

        Int128 attributedEnergy;
        try {
            BigInteger attrBig = new BigDecimal(networkEnergy.toBigInteger())
                    .multiply(BigDecimal.valueOf(ratio))
                    .setScale(0, RoundingMode.HALF_UP)
                    .toBigInteger();
            attributedEnergy = Int128.fromBigInteger(attrBig);
            if (attributedEnergy.compareTo(maxCapacity) > 0) attributedEnergy = maxCapacity.copy();
        } catch (Exception ignored) {
            attributedEnergy = networkEnergy.copy();
        }

        double fill = 0.0;
        if (!maxCapacity.isZero()) {
            try {
                fill = attributedEnergy.toBigInteger().doubleValue()
                        / maxCapacity.toBigInteger().doubleValue();
                fill = Math.min(1.0, Math.max(0.0, fill));
            } catch (Exception ignored) {}
        }

        int barLength = 20;
        int filledCount = (int) Math.round(fill * barLength);
        StringBuilder bar = new StringBuilder("\u00a7b[");
        for (int i = 0; i < barLength; i++) {
            bar.append(i < filledCount ? "\u00a7a|" : "\u00a78|");
        }
        bar.append("\u00a7b]");

        textList.add(Component.literal(bar + " \u00a7f" + String.format(Locale.US, "%.1f%%", fill * 100.0)));
        textList.add(Component.literal("\u00a77Energy: \u00a7f" + attributedEnergy.toHumanReadableString() + " / " +
                maxCapacity.toHumanReadableString() + " EU"));

        // --- Equal-split IO: each matrix shows 1/N of network-wide IO ---
        // Energy consumed by machines is divided equally among all linked matrices.
        Int128 attributedInput  = divideByCount(networkInput,  matrixCount);
        Int128 attributedOutput = divideByCount(networkOutput, matrixCount);
        if (!transferLimit.isZero()) {
            if (attributedInput.compareTo(transferLimit)  > 0) attributedInput  = transferLimit.copy();
            if (attributedOutput.compareTo(transferLimit) > 0) attributedOutput = transferLimit.copy();
        }

        textList.add(Component.literal("\u00a7aInput: +" + attributedInput.toHumanReadableString() + " EU/t"));
        textList.add(Component.literal("\u00a7cOutput: -" + attributedOutput.toHumanReadableString() + " EU/t"));

        // --- Time to Empty ---
        textList.add(Component.literal("\u00a77\u23f1 Time to Empty: "
                + calculateTimeToEmpty(attributedEnergy, attributedInput, attributedOutput)));
    }

    /**
     * Returns a coloured time-to-empty string: \u00a7c (red) when draining,
     * \u00a7a (green) when charging. Mirrors QuantumTerminalUI.calculateTimeToEmpty.
     */
    private String calculateTimeToEmpty(Int128 energy, Int128 inPerTick, Int128 outPerTick) {
        if (energy.isZero()) return "\u00a7c0s (EMPTY)";
        Int128 netDrain = outPerTick.copy();
        if (netDrain.compareTo(inPerTick) <= 0) return "\u00a7a\u221e (Charging)";
        netDrain.subtract(inPerTick);
        if (netDrain.isZero() || netDrain.isNegative()) return "\u00a7a\u221e (Charging)";
        try {
            long drainLong = netDrain.toLong();
            if (drainLong <= 0) return "\u00a7a\u221e";
            long energyLong = energy.toLong();
            long ticks = energyLong / drainLong;
            return formatTickDuration(ticks);
        } catch (Exception e) {
            BigInteger energyBig = energy.toBigInteger();
            BigInteger drainBig  = netDrain.toBigInteger();
            if (drainBig.signum() <= 0) return "\u00a7a\u221e";
            BigInteger ticksBig = energyBig.divide(drainBig);
            long ticksLong = ticksBig.min(BigInteger.valueOf(Long.MAX_VALUE)).longValue();
            return formatTickDuration(ticksLong);
        }
    }

    /** Formats a tick count as a duration string in \u00a7c (red) for an active drain. */
    private String formatTickDuration(long ticks) {
        if (ticks <= 0) return "\u00a7c0s";
        long totalSeconds = ticks / 20;
        long hours   = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        if (hours > 99999) return "\u00a7c> 99999h";
        if (hours   > 0) return String.format("\u00a7c%dh %02dm %02ds", hours, minutes, seconds);
        if (minutes > 0) return String.format("\u00a7c%dm %02ds", minutes, seconds);
        return String.format("\u00a7c%ds", seconds);
    }

    /** Integer division of an Int128 by {@code count}, rounding down. */
    private static Int128 divideByCount(Int128 value, int count) {
        if (count <= 1 || value == null || value.isZero()) {
            return value != null ? value.copy() : Int128.ZERO();
        }
        try {
            return Int128.fromBigInteger(value.toBigInteger().divide(BigInteger.valueOf(count)));
        } catch (Exception ignored) {
            return value.copy();
        }
    }
}
