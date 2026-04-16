package com.raishxn.gtna.integration.ae2.crafting;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.crafting.IPatternDetails;
import appeng.api.features.IPlayerRegistry;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.crafting.ICraftingSubmitResult;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.core.AELog;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.CraftingJobStatusPacket;
import appeng.crafting.CraftingLink;
import appeng.crafting.execution.CraftingCpuHelper;
import appeng.crafting.execution.CraftingCpuLogic;
import appeng.crafting.execution.CraftingSubmitResult;
import appeng.crafting.execution.ElapsedTimeTracker;
import appeng.crafting.inv.ListCraftingInventory;
import appeng.hooks.ticking.TickHandler;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.me.service.CraftingService;
import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.integration.ae2.pattern.IParallelPatternDetails;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public class GTNAOptimizedCraftingCpuLogic extends CraftingCpuLogic {

    final CraftingCPUCluster cluster;
    private GTNAExecutingCraftingJob job = null;
    private final ListCraftingInventory inventory = new ListCraftingInventory(this::postChange);
    private final Set<Consumer<AEKey>> listeners = new HashSet<>();
    private boolean cantStoreItems = false;
    private long lastModifiedOnTick = TickHandler.instance().getCurrentTick();

    public GTNAOptimizedCraftingCpuLogic(CraftingCPUCluster cluster) {
        super(cluster);
        this.cluster = cluster;
    }

    @Override
    public ICraftingSubmitResult trySubmitJob(IGrid grid, ICraftingPlan plan, IActionSource src,
                                              @Nullable ICraftingRequester requester) {
        if (this.job != null) return CraftingSubmitResult.CPU_BUSY;
        if (!cluster.isActive()) return CraftingSubmitResult.CPU_OFFLINE;
        if (cluster.getAvailableStorage() < plan.bytes()) return CraftingSubmitResult.CPU_TOO_SMALL;

        if (!inventory.list.isEmpty()) {
            AELog.warn("Crafting CPU inventory is not empty yet a job was submitted.");
        }

        var missingIngredient = CraftingCpuHelper.tryExtractInitialItems(plan, grid, inventory, src);
        if (missingIngredient != null) return CraftingSubmitResult.missingIngredient(missingIngredient);

        var playerId = src.player()
                .map(p -> p instanceof ServerPlayer serverPlayer ? IPlayerRegistry.getPlayerId(serverPlayer) : null)
                .orElse(null);
        var craftId = UUID.randomUUID();
        var linkCpu = new CraftingLink(CraftingCpuHelper.generateLinkData(craftId, requester == null, false), cluster);
        this.job = new GTNAExecutingCraftingJob(plan, this::postChange, linkCpu, playerId);
        GTNACORE.LOGGER.debug(
                "[GTNA] Submitted crafting job to CPU {} with final output {} x{}, {} task(s) and {} bytes",
                cluster,
                plan.finalOutput().what(),
                plan.finalOutput().amount(),
                this.job.tasks.size(),
                plan.bytes());
        cluster.updateOutput(plan.finalOutput());
        cluster.markDirty();
        notifyJobOwner(job, CraftingJobStatusPacket.Status.STARTED);

        if (requester != null) {
            var linkReq = new CraftingLink(CraftingCpuHelper.generateLinkData(craftId, false, true), requester);
            var craftingService = (CraftingService) grid.getCraftingService();
            craftingService.addLink(linkCpu);
            craftingService.addLink(linkReq);
            return CraftingSubmitResult.successful(linkReq);
        }
        return CraftingSubmitResult.successful(null);
    }

    @Override
    public void tickCraftingLogic(IEnergyService eg, CraftingService cc) {
        if (!cluster.isActive()) return;
        cantStoreItems = false;
        if (this.job == null) {
            this.storeItems();
            if (!this.inventory.list.isEmpty()) {
                cantStoreItems = true;
            }
            return;
        }
        if (job.link.isCanceled()) {
            cancel();
            return;
        }
        GTNACORE.LOGGER.debug(
                "[GTNA] Ticking optimized CPU {} with {} task(s), waitingFor={}, storedItems={}",
                cluster,
                job.tasks.size(),
                job.waitingFor.list.size(),
                inventory.list.size());
        executeCrafting(cluster.getCoProcessors(), cc, eg, cluster.getLevel());
    }

    @Override
    public int executeCrafting(int maxPatterns, CraftingService craftingService, IEnergyService energyService,
                               Level level) {
        if (job == null) return 0;

        int pushedPatterns = 0;
        var it = job.tasks.entrySet().iterator();
        taskLoop:
        while (it.hasNext()) {
            var task = it.next();
            if (task.getValue().value <= 0) {
                it.remove();
                continue;
            }

            IPatternDetails providerDetails = task.getKey();
            IPatternDetails details = providerDetails;
            long parallel = 1L;
            if (task.getValue().value > 1) {
                long maxParallel = getMaxParallel(task.getValue().value, providerDetails, inventory);
                if (maxParallel <= 0L) {
                    continue;
                }
                if (maxParallel > 1L) {
                    details = IParallelPatternDetails.of(providerDetails, level, maxParallel);
                    parallel = maxParallel;
                }
            }

            var providers = craftingService.getProviders(providerDetails);
            int providerCount = 0;
            for (var ignored : providers) {
                providerCount++;
            }
            GTNACORE.LOGGER.debug(
                    "[GTNA] CPU {} evaluating pattern {} with remainingOps={}, chosenParallel={}, providers={}",
                    cluster,
                    providerDetails.getDefinition(),
                    task.getValue().value,
                    parallel,
                    providerCount);

            KeyCounter expectedOutputs = new KeyCounter();
            KeyCounter expectedContainerItems = new KeyCounter();
            @Nullable
            KeyCounter[] craftingContainer = CraftingCpuHelper.extractPatternInputs(details, inventory, level,
                    expectedOutputs, expectedContainerItems);
            if (craftingContainer == null) {
                GTNACORE.LOGGER.debug(
                        "[GTNA] CPU {} could not extract inputs for pattern {} at parallel {}",
                        cluster,
                        providerDetails.getDefinition(),
                        parallel);
            }

            for (ICraftingProvider provider : providers) {
                if (craftingContainer == null) {
                    break;
                }
                if (provider.isBusy()) {
                    GTNACORE.LOGGER.debug(
                            "[GTNA] CPU {} skipped busy provider {} for pattern {}",
                            cluster,
                            provider,
                            providerDetails.getDefinition());
                    continue;
                }

                double patternPower = CraftingCpuHelper.calculatePatternPower(craftingContainer) * parallel;
                if (energyService.extractAEPower(patternPower, Actionable.SIMULATE, PowerMultiplier.CONFIG) <
                        patternPower - 0.01) {
                    break;
                }

                if (provider.pushPattern(details, craftingContainer)) {
                    GTNACORE.LOGGER.debug(
                            "[GTNA] CPU {} pushed pattern {} to provider {} with parallel {}",
                            cluster,
                            providerDetails.getDefinition(),
                            provider,
                            parallel);
                    energyService.extractAEPower(patternPower, Actionable.MODULATE, PowerMultiplier.CONFIG);
                    pushedPatterns++;

                    for (var expectedOutput : expectedOutputs) {
                        job.waitingFor.insert(expectedOutput.getKey(), expectedOutput.getLongValue(),
                                Actionable.MODULATE);
                    }
                    for (var expectedContainerItem : expectedContainerItems) {
                        job.waitingFor.insert(expectedContainerItem.getKey(), expectedContainerItem.getLongValue(),
                                Actionable.MODULATE);
                        job.tracker.gtna$addMaxItems(expectedContainerItem.getLongValue(),
                                expectedContainerItem.getKey().getType());
                    }

                    cluster.markDirty();
                    task.getValue().value -= parallel;
                    if (task.getValue().value <= 0) {
                        it.remove();
                    }
                    if (pushedPatterns > maxPatterns || parallel > 1L) {
                        break taskLoop;
                    }

                    expectedOutputs.reset();
                    expectedContainerItems.reset();
                    craftingContainer = CraftingCpuHelper.extractPatternInputs(details, inventory, level,
                            expectedOutputs, expectedContainerItems);
                }
            }

            if (providerCount == 0) {
                GTNACORE.LOGGER.debug(
                        "[GTNA] CPU {} found no providers for pattern {}",
                        cluster,
                        providerDetails.getDefinition());
            }

            if (craftingContainer != null) {
                CraftingCpuHelper.reinjectPatternInputs(inventory, craftingContainer);
            }
        }
        return pushedPatterns;
    }

    private static long getMaxParallel(long maxParallel, IPatternDetails details, ListCraftingInventory inventory) {
        for (IPatternDetails.IInput input : details.getInputs()) {
            long extracted = 0L;
            for (var stack : input.getPossibleInputs()) {
                extracted += inventory.extract(stack.what(), Long.MAX_VALUE, Actionable.SIMULATE) /
                        Math.max(1L, stack.amount());
            }
            maxParallel = Math.min(maxParallel, extracted / Math.max(1L, input.getMultiplier()));
            if (maxParallel < 1L) {
                return 0L;
            }
        }
        return maxParallel;
    }

    @Override
    public long insert(AEKey what, long amount, Actionable type) {
        if (what == null || job == null) return 0L;

        var waitingFor = job.waitingFor.extract(what, amount, Actionable.SIMULATE);
        if (waitingFor <= 0) {
            return 0L;
        }

        if (amount > waitingFor) {
            amount = waitingFor;
        }

        if (type == Actionable.MODULATE) {
            job.tracker.gtna$decrementItems(amount, what.getType());
            job.waitingFor.extract(what, amount, Actionable.MODULATE);
            cluster.markDirty();
        }

        long inserted = amount;
        if (what.matches(job.finalOutput)) {
            inserted = job.link.insert(what, amount, type);
            if (type == Actionable.MODULATE) {
                postChange(what);
                job.remainingAmount = Math.max(0, job.remainingAmount - amount);
                if (job.remainingAmount <= 0) {
                    finishJob(true);
                    cluster.updateOutput(null);
                } else {
                    cluster.updateOutput(new GenericStack(job.finalOutput.what(), job.remainingAmount));
                }
            }
        } else if (type == Actionable.MODULATE) {
            inventory.insert(what, amount, Actionable.MODULATE);
        }

        return inserted;
    }

    @Override
    public void cancel() {
        if (job == null) return;
        cluster.updateOutput(null);
        finishJob(false);
    }

    @Override
    public void storeItems() {
        if (this.inventory.list.isEmpty()) return;
        var g = cluster.getGrid();
        if (g == null) return;
        var storage = g.getStorageService().getInventory();
        for (var entry : this.inventory.list) {
            this.postChange(entry.getKey());
            var inserted = storage.insert(entry.getKey(), entry.getLongValue(), Actionable.MODULATE, cluster.getSrc());
            entry.setValue(entry.getLongValue() - inserted);
        }
        this.inventory.list.removeZeros();
        cluster.markDirty();
    }

    private void postChange(AEKey what) {
        lastModifiedOnTick = TickHandler.instance().getCurrentTick();
        for (var listener : listeners) {
            listener.accept(what);
        }
    }

    @Override
    public long getLastModifiedOnTick() {
        return lastModifiedOnTick;
    }

    @Override
    public boolean hasJob() {
        return this.job != null;
    }

    @Override
    public GenericStack getFinalJobOutput() {
        return this.job != null ? this.job.finalOutput : null;
    }

    @Override
    public ElapsedTimeTracker getElapsedTimeTracker() {
        return this.job != null ? this.job.timeTracker : new ElapsedTimeTracker();
    }

    @Override
    public void readFromNBT(CompoundTag data) {
        this.inventory.readFromNBT(data.getList("inventory", 10));
        if (data.contains("job")) {
            this.job = new GTNAExecutingCraftingJob(data.getCompound("job"), this::postChange, this);
            cluster.updateOutput(new GenericStack(job.finalOutput.what(), job.remainingAmount));
        } else {
            cluster.updateOutput(null);
        }
    }

    @Override
    public void writeToNBT(CompoundTag data) {
        data.put("inventory", this.inventory.writeToNBT());
        if (this.job != null) {
            data.put("job", this.job.writeToNBT());
        }
    }

    @Override
    public ICraftingLink getLastLink() {
        return this.job != null ? this.job.link : null;
    }

    @Override
    public ListCraftingInventory getInventory() {
        return this.inventory;
    }

    @Override
    public void addListener(Consumer<AEKey> listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(Consumer<AEKey> listener) {
        listeners.remove(listener);
    }

    @Override
    public long getStored(AEKey template) {
        return this.inventory.extract(template, Long.MAX_VALUE, Actionable.SIMULATE);
    }

    @Override
    public long getWaitingFor(AEKey template) {
        return this.job != null ? this.job.waitingFor.extract(template, Long.MAX_VALUE, Actionable.SIMULATE) : 0L;
    }

    @Override
    public void getAllWaitingFor(Set<AEKey> waitingFor) {
        if (this.job != null) {
            for (var entry : this.job.waitingFor.list) {
                waitingFor.add(entry.getKey());
            }
        }
    }

    @Override
    public long getPendingOutputs(AEKey template) {
        long count = 0L;
        if (this.job != null) {
            for (var task : job.tasks.entrySet()) {
                for (var output : task.getKey().getOutputs()) {
                    if (template.matches(output)) {
                        count += output.amount() * task.getValue().value;
                    }
                }
            }
        }
        return count;
    }

    @Override
    public void getAllItems(KeyCounter out) {
        out.addAll(this.inventory.list);
        if (this.job != null) {
            out.addAll(job.waitingFor.list);
            for (var task : job.tasks.entrySet()) {
                for (var output : task.getKey().getOutputs()) {
                    out.add(output.what(), output.amount() * task.getValue().value);
                }
            }
        }
    }

    @Override
    public boolean isCantStoreItems() {
        return cantStoreItems;
    }

    private void finishJob(boolean success) {
        if (success) {
            job.link.markDone();
        } else {
            job.link.cancel();
        }

        job.waitingFor.clear();
        for (var entry : job.tasks.entrySet()) {
            for (var output : entry.getKey().getOutputs()) {
                postChange(output.what());
            }
        }

        notifyJobOwner(job,
                success ? CraftingJobStatusPacket.Status.FINISHED : CraftingJobStatusPacket.Status.CANCELLED);
        this.job = null;
        this.storeItems();
    }

    private void notifyJobOwner(GTNAExecutingCraftingJob job, CraftingJobStatusPacket.Status status) {
        this.lastModifiedOnTick = TickHandler.instance().getCurrentTick();
        var playerId = job.playerId;
        if (playerId == null) {
            return;
        }
        var server = cluster.getLevel().getServer();
        var connectedPlayer = IPlayerRegistry.getConnected(server, playerId);
        if (connectedPlayer != null) {
            var jobId = job.link.getCraftingID();
            NetworkHandler.instance().sendTo(new CraftingJobStatusPacket(
                    jobId,
                    job.finalOutput.what(),
                    job.finalOutput.amount(),
                    job.remainingAmount,
                    status), connectedPlayer);
        }
    }
}
