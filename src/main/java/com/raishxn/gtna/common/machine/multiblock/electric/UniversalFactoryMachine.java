package com.raishxn.gtna.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.CombinedDirectionalFancyConfigurator;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

import com.raishxn.gtna.api.machine.gui.ScrollableMachineModeFancyConfigurator;
import com.raishxn.gtna.config.GTNABalance;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

/**
 * GTNA-native port of GTLsupb's Universal Factory (LGPLv3). It processes a large set of recipe types
 * with cross-recipe parallelism and threads (both already provided by
 * {@link WorkableElectricMultipleRecipesMachine} / its logic) plus the GTLsupb-specific
 * warmup / overload / batch mechanics.
 *
 * <p>
 * The original extends GTLCore's own multiple-recipes base and GTLCore helpers; this port keeps only
 * the observable behaviour on the GTNA/GTCEu base.
 */
public class UniversalFactoryMachine extends WorkableElectricMultipleRecipesMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            UniversalFactoryMachine.class, WorkableElectricMultipleRecipesMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    private int batchMultiplier = 1;
    @Persisted
    @DescSynced
    private long runningSecs;
    @Persisted
    private boolean autoBatch;

    @Nullable
    private TickableSubscription runningSecSubs;
    private volatile int cachedVoltageTier = -1;
    private volatile long lastOverclockVoltage = -1L;

    public UniversalFactoryMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void attachSideTabs(TabsWidget sideTabs) {
        // 32 recipe types overflow the stock machine-mode tab; use a scrollable one instead.
        sideTabs.setMainTab(this);
        if (getRecipeTypes().length > 1) {
            sideTabs.attachSubTab(new ScrollableMachineModeFancyConfigurator(this));
        }
        var directionalConfigurator = CombinedDirectionalFancyConfigurator.of(self(), self());
        if (directionalConfigurator != null) {
            sideTabs.attachSubTab(directionalConfigurator);
        }
    }

    // ------------------------------------------------------------------
    // Warmup / overload / batch
    // ------------------------------------------------------------------

    public int getBatchMultiplier() {
        return batchMultiplier;
    }

    public boolean getAutoBatch() {
        return autoBatch;
    }

    public long getRunningSecs() {
        return runningSecs;
    }

    public boolean getOverloadUnlocked() {
        return runningSecs >= GTNABalance.getUniversalFactoryOverloadTime();
    }

    /** Exponential warmup: 1x when cold, up to the configured max while running. */
    public double getWarmupMultiplier() {
        if (runningSecs <= 0) {
            return 1.0;
        }
        double max = GTNABalance.getUniversalFactoryMaxWarmup();
        double tau = GTNABalance.getUniversalFactoryWarmupTau();
        return 1.0 + (max - 1.0) * (1.0 - Math.exp(-(double) runningSecs / tau));
    }

    private int computeVoltageTier() {
        long voltage = getOverclockVoltage();
        if (voltage == lastOverclockVoltage) {
            return cachedVoltageTier;
        }
        lastOverclockVoltage = voltage;
        cachedVoltageTier = voltage <= 0 ? 0 : Math.min(GTUtil.getTierByVoltage(voltage), 20);
        return cachedVoltageTier;
    }

    /** Threads scale with the operating voltage tier (GTLsupb: baseThreads * 2^tier). */
    public int getDynamicThreads() {
        return Math.max(1, GTNABalance.getUniversalFactoryBaseThreads() * (1 << computeVoltageTier()));
    }

    @Override
    public int getMaxParallel() {
        int tier = computeVoltageTier();
        double warmup = getOverloadUnlocked() ? GTNABalance.getUniversalFactoryMaxWarmup() : getWarmupMultiplier();
        long parallel = (long) GTNABalance.getUniversalFactoryBaseParallel() * (1L << tier) * batchMultiplier;
        long result = (long) (parallel * warmup);
        return (int) Math.max(1, Math.min(result, Integer.MAX_VALUE));
    }

    @Override
    public int getAdditionalThread() {
        return Math.max(super.getAdditionalThread(), getDynamicThreads() - 1);
    }

    // ------------------------------------------------------------------
    // Lifecycle
    // ------------------------------------------------------------------

    @Override
    public void onLoad() {
        super.onLoad();
        if (getLevel() instanceof ServerLevel) {
            this.runningSecSubs = subscribeServerTick(this.runningSecSubs, this::updateRunningSecs);
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (runningSecSubs != null) {
            runningSecSubs.unsubscribe();
            runningSecSubs = null;
        }
    }

    @Override
    public boolean onWorking() {
        if (runningSecs == 0) {
            runningSecs = 1;
        }
        return super.onWorking();
    }

    private void updateRunningSecs() {
        if (getOffsetTimer() % 20 != 0) {
            return;
        }
        if (getRecipeLogic().isWorking()) {
            runningSecs = Math.max(runningSecs + 1, 0);
        } else {
            runningSecs = Math.max(runningSecs - 16, 0);
        }
    }

    private void incBatchMultiplier(int increment) {
        batchMultiplier = Math.min(batchMultiplier + increment, GTNABalance.getUniversalFactoryMaxBatchMultiplier());
    }

    private void decBatchMultiplier(int decrement) {
        batchMultiplier = Math.max(batchMultiplier - decrement, 1);
    }

    // ------------------------------------------------------------------
    // Display
    // ------------------------------------------------------------------

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (!isFormed()) {
            return;
        }

        double warmup = getOverloadUnlocked() ? GTNABalance.getUniversalFactoryMaxWarmup() : getWarmupMultiplier();
        long overloadTime = GTNABalance.getUniversalFactoryOverloadTime();

        textList.add(Component.translatable("gtna.machine.universal_factory.thermal_status",
                Component.literal(runningSecs + "s").withStyle(ChatFormatting.GOLD),
                Component.literal(overloadTime + "s").withStyle(ChatFormatting.GRAY),
                Component.literal(String.format(Locale.US, "%.2f", warmup)).withStyle(ChatFormatting.AQUA))
                .withStyle(ChatFormatting.GRAY));

        if (getOverloadUnlocked()) {
            textList.add(Component.translatable("gtna.machine.universal_factory.overload_active")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
        }

        textList.add(Component.translatable("gtna.machine.universal_factory.dynamic_threads",
                Component.literal(String.valueOf(getDynamicThreads())).withStyle(ChatFormatting.AQUA))
                .withStyle(ChatFormatting.GRAY));

        textList.add(Component.translatable("gtna.machine.batch_multiplier",
                Component.literal(String.valueOf(batchMultiplier)).withStyle(ChatFormatting.AQUA))
                .withStyle(ChatFormatting.GRAY));

        textList.add(Component.empty()
                .append(ComponentPanelWidget.withButton(Component.literal("§c[-]"), "batch_sub"))
                .append(Component.literal(" ").withStyle(ChatFormatting.RESET))
                .append(ComponentPanelWidget.withButton(Component.literal("§a[+]"), "batch_add"))
                .append(Component.literal("  ").withStyle(ChatFormatting.RESET))
                .append(ComponentPanelWidget.withButton(
                        Component.literal(autoBatch ? "§d[AUTO]" : "§7[AUTO]"), "batch_auto")));
    }

    @Override
    public void handleDisplayClick(String componentData, ClickData clickData) {
        if (clickData.isRemote || !isFormed()) {
            return;
        }
        int multiplier = clickData.isCtrlClick ? 4 : (clickData.isShiftClick ? 2 : 1);
        switch (componentData) {
            case "batch_sub" -> decBatchMultiplier(multiplier);
            case "batch_add" -> incBatchMultiplier(multiplier);
            case "batch_auto" -> autoBatch = !autoBatch;
            default -> {}
        }
    }
}
