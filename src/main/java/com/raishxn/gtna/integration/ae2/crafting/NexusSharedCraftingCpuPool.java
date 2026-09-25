package com.raishxn.gtna.integration.ae2.crafting;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;

import appeng.api.config.Actionable;
import appeng.api.config.CpuSelectionMode;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.CraftingJobStatus;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.crafting.ICraftingSubmitResult;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.crafting.execution.CraftingSubmitResult;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.me.helpers.MachineSource;
import appeng.me.service.CraftingService;
import com.raishxn.gtna.common.machine.multiblock.part.ae.GTNACraftingCPUInterfacePartMachine;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * UFO Future's shared-capacity computation model adapted to AE2 15.4.10. AE2 sees one idle CPU;
 * accepted jobs reserve storage and run in temporary clusters sharing the same co-processor budget.
 */
public final class NexusSharedCraftingCpuPool implements ICraftingCPU {

    private static final int MAX_DISPATCH_SLOTS = 2048;
    private static final String TAG_JOBS = "Jobs";
    private static final String TAG_ID = "Id";
    private static final String TAG_RESERVED = "Reserved";
    private static final String TAG_STATE = "State";

    private final GTNACraftingCPUInterfacePartMachine host;
    private final MachineSource source;
    private final Map<UUID, Job> jobs = new LinkedHashMap<>();
    private long totalStorage;
    private long remainingStorage;
    private int coProcessors;
    private boolean infinite;

    public NexusSharedCraftingCpuPool(GTNACraftingCPUInterfacePartMachine host, MachineSource source) {
        this.host = host;
        this.source = source;
    }

    public void reconfigure(long storage, long coProcessors, boolean infinite) {
        long nextStorage = infinite ? Long.MAX_VALUE : Math.max(0L, storage);
        int nextCoProcessors = infinite ? Integer.MAX_VALUE - 1 :
                (int) Math.min(Integer.MAX_VALUE - 1L, Math.max(0L, coProcessors));
        if (totalStorage == nextStorage && this.coProcessors == nextCoProcessors && this.infinite == infinite) {
            return;
        }
        totalStorage = nextStorage;
        this.coProcessors = nextCoProcessors;
        this.infinite = infinite;
        recalculateRemaining();
        host.onChanged();
        host.notifyCpuChanged();
    }

    public int getActiveJobCount() {
        return jobs.size();
    }

    public boolean containsCpu(ICraftingCPU cpu) {
        if (cpu == this) return true;
        for (Job job : jobs.values()) if (job.cpu() == cpu) return true;
        return false;
    }

    public Collection<CraftingCPUCluster> getActiveCpus() {
        return jobs.values().stream().map(Job::cpu).toList();
    }

    public void restoreCraftingLinks(Consumer<ICraftingLink> consumer) {
        for (Job job : jobs.values()) {
            ICraftingLink link = job.cpu().craftingLogic.getLastLink();
            if (link != null) consumer.accept(link);
        }
    }

    public ICraftingSubmitResult submitJob(IGrid grid, ICraftingPlan plan, IActionSource actionSource,
                                           @Nullable ICraftingRequester requester) {
        if (!isActive()) return CraftingSubmitResult.CPU_OFFLINE;
        long reserved = Math.max(0L, plan.bytes());
        if (!infinite && reserved > remainingStorage) return CraftingSubmitResult.CPU_TOO_SMALL;

        UUID id = UUID.randomUUID();
        CraftingCPUCluster cpu = newCluster(reserved, jobs.size());
        jobs.put(id, new Job(id, reserved, cpu));
        recalculateRemaining();
        ICraftingSubmitResult result = cpu.submitJob(grid, plan, actionSource, requester);
        if (!result.successful()) {
            jobs.remove(id);
            recalculateRemaining();
        } else {
            host.onChanged();
            host.notifyCpuChanged();
        }
        return result;
    }

    public long tick(IEnergyService energy, CraftingService craftingService) {
        List<Job> scheduled = new ArrayList<>(jobs.values());
        if (scheduled.isEmpty()) return Long.MIN_VALUE;
        int dispatchSlots = Math.min(MAX_DISPATCH_SLOTS, coProcessors + 1);
        dispatchSlots = throttleToNetworkPower(energy, dispatchSlots);
        int count = Math.min(scheduled.size(), dispatchSlots);
        long latest = Long.MIN_VALUE;
        for (int index = 0; index < count; index++) {
            Job job = scheduled.get(index);
            int lanes = dispatchSlots / count + (index < dispatchSlots % count ? 1 : 0);
            IGTNACraftingCPUCluster.of(job.cpu()).gtna$setAccelerator(lanes - 1);
            job.cpu().craftingLogic.tickCraftingLogic(energy, craftingService);
            latest = Math.max(latest, job.cpu().craftingLogic.getLastModifiedOnTick());
        }
        rotateOrder();
        removeDrained();
        return latest;
    }

    public void addWaitingKeys(Set<AEKey> waitingKeys) {
        for (Job job : jobs.values()) job.cpu().craftingLogic.getAllWaitingFor(waitingKeys);
    }

    public long insert(AEKey what, long amount, Actionable mode) {
        long inserted = 0L;
        for (Job job : jobs.values()) {
            if (inserted >= amount) break;
            inserted += job.cpu().craftingLogic.insert(what, amount - inserted, mode);
        }
        return inserted;
    }

    public long getRequestedAmount(AEKey what) {
        long result = 0L;
        for (Job job : jobs.values()) {
            long addition = job.cpu().craftingLogic.getWaitingFor(what);
            result = result >= Long.MAX_VALUE - addition ? Long.MAX_VALUE : result + addition;
        }
        return result;
    }

    public void writeToNBT(CompoundTag tag) {
        ListTag list = new ListTag();
        for (Job job : jobs.values()) {
            CompoundTag encoded = new CompoundTag();
            encoded.putUUID(TAG_ID, job.id());
            encoded.putLong(TAG_RESERVED, job.reserved());
            CompoundTag state = new CompoundTag();
            job.cpu().writeToNBT(state);
            encoded.put(TAG_STATE, state);
            list.add(encoded);
        }
        tag.put(TAG_JOBS, list);
    }

    public void readFromNBT(CompoundTag tag) {
        jobs.clear();
        for (Tag raw : tag.getList(TAG_JOBS, Tag.TAG_COMPOUND)) {
            CompoundTag encoded = (CompoundTag) raw;
            if (!encoded.hasUUID(TAG_ID) || !encoded.contains(TAG_STATE, Tag.TAG_COMPOUND)) continue;
            UUID id = encoded.getUUID(TAG_ID);
            long reserved = Math.max(0L, encoded.getLong(TAG_RESERVED));
            CraftingCPUCluster cpu = newCluster(reserved, jobs.size());
            cpu.readFromNBT(encoded.getCompound(TAG_STATE));
            jobs.put(id, new Job(id, reserved, cpu));
        }
        recalculateRemaining();
    }

    /** Migrates persisted per-core clusters from the previous GTNA implementation. */
    public void readLegacyCpus(ListTag legacy) {
        jobs.clear();
        for (Tag raw : legacy) {
            if (!(raw instanceof CompoundTag state)) continue;
            // Legacy saves did not persist each cluster's capacity. Reserve the whole pool
            // until these jobs drain so a reload cannot overcommit shared storage.
            long reserved = Long.MAX_VALUE;
            CraftingCPUCluster cpu = newCluster(reserved, jobs.size());
            cpu.readFromNBT(state);
            if (!cpu.craftingLogic.hasJob() && cpu.craftingLogic.getInventory().list.isEmpty()) continue;
            UUID id = UUID.randomUUID();
            jobs.put(id, new Job(id, reserved, cpu));
        }
        recalculateRemaining();
    }

    @Override
    public boolean isBusy() {
        return false;
    }

    @Override
    public @Nullable CraftingJobStatus getJobStatus() {
        return null;
    }

    @Override
    public void cancelJob() {}

    @Override
    public long getAvailableStorage() {
        return remainingStorage;
    }

    @Override
    public int getCoProcessors() {
        return coProcessors;
    }

    @Override
    public Component getName() {
        return Component.translatable("gtna.ae2.cpu.nexus_hypercore");
    }

    @Override
    public CpuSelectionMode getSelectionMode() {
        return CpuSelectionMode.ANY;
    }

    public boolean isActive() {
        return host.isPoolOnline() && totalStorage > 0L;
    }

    private CraftingCPUCluster newCluster(long storage, int index) {
        return IGTNACraftingCPUCluster.create(host, source, storage, Math.min(coProcessors, MAX_DISPATCH_SLOTS - 1),
                index);
    }

    private void removeDrained() {
        boolean removed = jobs.values().removeIf(
                job -> !job.cpu().craftingLogic.hasJob() && job.cpu().craftingLogic.getInventory().list.isEmpty());
        if (removed) {
            recalculateRemaining();
            host.onChanged();
            host.notifyCpuChanged();
        }
    }

    private void rotateOrder() {
        if (jobs.size() < 2) return;
        var iterator = jobs.entrySet().iterator();
        var first = iterator.next();
        iterator.remove();
        jobs.put(first.getKey(), first.getValue());
    }

    private void recalculateRemaining() {
        if (infinite) {
            remainingStorage = Long.MAX_VALUE;
            return;
        }
        long used = 0L;
        for (Job job : jobs.values()) {
            long reservation = job.reserved();
            used = used >= Long.MAX_VALUE - reservation ? Long.MAX_VALUE : used + reservation;
        }
        remainingStorage = Math.max(0L, totalStorage - used);
    }

    private static int throttleToNetworkPower(IEnergyService energy, int slots) {
        double max = energy.getMaxStoredPower();
        if (max <= 0.0D) return slots;
        double ratio = energy.getStoredPower() / max;
        if (ratio < 0.10D) return 1;
        if (ratio < 0.25D) return Math.max(1, slots / 8);
        if (ratio < 0.50D) return Math.max(1, slots / 2);
        return slots;
    }

    private record Job(UUID id, long reserved, CraftingCPUCluster cpu) {}
}
