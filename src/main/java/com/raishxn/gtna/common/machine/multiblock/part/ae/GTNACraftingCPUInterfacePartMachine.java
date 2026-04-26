package com.raishxn.gtna.common.machine.multiblock.part.ae;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.integration.ae2.machine.MEBusPartMachine;

import net.minecraft.nbt.CompoundTag;

import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.events.GridCraftingCpuChange;
import appeng.api.networking.security.IActionHost;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.me.helpers.MachineSource;
import com.raishxn.gtna.common.machine.multiblock.energy.NexusMEHyperCoreMachine;
import com.raishxn.gtna.integration.ae2.crafting.IGTNACraftingCPUCluster;

import java.util.List;

public class GTNACraftingCPUInterfacePartMachine extends MEBusPartMachine implements IActionHost {

    private static final String CPU_TAG = "NexusCraftingCpu";
    private static final String STORAGE_TAG = "NexusCpuStorage";
    private static final String COPROCESSORS_TAG = "NexusCpuCoProcessors";

    private final MachineSource machineSource = new MachineSource(this);
    private CraftingCPUCluster cluster;
    private CompoundTag pendingClusterTag;
    private long storageBytes;
    private int coProcessors;
    private TickableSubscription reconnectSubscription;
    private int reconnectTicks;

    public GTNACraftingCPUInterfacePartMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, IO.IN, args);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        rebuildCluster();
        scheduleCpuReconnect();
    }

    @Override
    public void onUnload() {
        if (reconnectSubscription != null) {
            reconnectSubscription.unsubscribe();
            reconnectSubscription = null;
        }
        super.onUnload();
    }

    @Override
    public void addedToController(IMultiController controller) {
        super.addedToController(controller);
        configureFromController(controller);
    }

    @Override
    public void removedFromController(IMultiController controller) {
        super.removedFromController(controller);
        configureCpu(0L, 0);
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        super.onMainNodeStateChanged(reason);
        rebuildCluster();
        scheduleCpuReconnect();
    }

    @Override
    public void saveCustomPersistedData(CompoundTag tag, boolean forDrop) {
        super.saveCustomPersistedData(tag, forDrop);
        tag.putLong(STORAGE_TAG, storageBytes);
        tag.putInt(COPROCESSORS_TAG, coProcessors);
        if (cluster != null) {
            CompoundTag cpuTag = new CompoundTag();
            cluster.writeToNBT(cpuTag);
            tag.put(CPU_TAG, cpuTag);
        }
    }

    @Override
    public void loadCustomPersistedData(CompoundTag tag) {
        super.loadCustomPersistedData(tag);
        storageBytes = tag.getLong(STORAGE_TAG);
        coProcessors = tag.getInt(COPROCESSORS_TAG);
        pendingClusterTag = tag.contains(CPU_TAG) ? tag.getCompound(CPU_TAG) : null;
    }

    public void configureCpu(long storageBytes, long coProcessors) {
        this.storageBytes = Math.max(0L, storageBytes);
        this.coProcessors = (int) Math.min(Integer.MAX_VALUE, Math.max(0L, coProcessors));
        rebuildCluster();
        notifyCpuChanged();
        scheduleCpuReconnect();
    }

    public List<CraftingCPUCluster> getClusters() {
        if (!isFormed() || !getMainNode().isActive() || cluster == null || storageBytes <= 0L || coProcessors <= 0) {
            return List.of();
        }
        return List.of(cluster);
    }

    public void onChanged() {
        markDirty();
    }

    @Override
    public IGridNode getActionableNode() {
        return getMainNode().getNode();
    }

    private void configureFromController(IMultiController controller) {
        if (controller instanceof NexusMEHyperCoreMachine hyperCore) {
            configureCpu(hyperCore.getAeStorageBytes(), hyperCore.getAeCoProcessors());
        }
    }

    private void scheduleCpuReconnect() {
        if (isRemote() || reconnectSubscription != null && reconnectSubscription.isStillSubscribed()) {
            return;
        }
        reconnectTicks = 0;
        reconnectSubscription = subscribeServerTick(this::tickCpuReconnect);
    }

    private void tickCpuReconnect() {
        reconnectTicks++;
        if (isFormed()) {
            for (IMultiController controller : getControllers()) {
                configureFromController(controller);
                break;
            }
        }
        rebuildCluster();
        notifyCpuChanged();

        IGridNode node = getMainNode().getNode();
        if (node != null && node.getGrid() != null || reconnectTicks >= 100) {
            if (reconnectSubscription != null) {
                reconnectSubscription.unsubscribe();
                reconnectSubscription = null;
            }
        }
    }

    private void rebuildCluster() {
        if (isRemote() || storageBytes <= 0L || coProcessors <= 0) {
            cluster = null;
            return;
        }
        if (cluster == null) {
            cluster = IGTNACraftingCPUCluster.create(this, machineSource, storageBytes, coProcessors);
            if (pendingClusterTag != null) {
                cluster.readFromNBT(pendingClusterTag);
                pendingClusterTag = null;
            }
        } else {
            IGTNACraftingCPUCluster bridge = IGTNACraftingCPUCluster.of(cluster);
            bridge.gtna$setMachine(this);
            bridge.gtna$setMachineSource(machineSource);
            bridge.gtna$setStorage(storageBytes);
            bridge.gtna$setAccelerator(coProcessors);
        }
    }

    private void notifyCpuChanged() {
        IGridNode node = getMainNode().getNode();
        if (node != null && node.getGrid() != null) {
            node.getGrid().postEvent(new GridCraftingCpuChange(node));
        }
    }
}
