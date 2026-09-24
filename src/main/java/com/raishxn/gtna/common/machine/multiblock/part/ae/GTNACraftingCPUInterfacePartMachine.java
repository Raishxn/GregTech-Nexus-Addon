package com.raishxn.gtna.common.machine.multiblock.part.ae;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.integration.ae2.machine.MEBusPartMachine;

import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.events.GridCraftingCpuChange;
import appeng.api.networking.security.IActionHost;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.me.helpers.MachineSource;
import com.raishxn.gtna.common.machine.multiblock.energy.NexusMEHyperCoreMachine;
import com.raishxn.gtna.common.machine.multiblock.energy.NexusMEHyperCoreMachine.CpuSpec;
import com.raishxn.gtna.integration.ae2.crafting.IGTNACraftingCPUCluster;

import java.util.ArrayList;
import java.util.List;

public class GTNACraftingCPUInterfacePartMachine extends MEBusPartMachine implements IActionHost {

    private static final String CPUS_TAG = "NexusCraftingCpus";
    private static final String LEGACY_CPU_TAG = "NexusCraftingCpu";

    private final MachineSource machineSource = new MachineSource(this);
    private final List<CraftingCPUCluster> clusters = new ArrayList<>();
    private List<CompoundTag> pendingClusterTags = List.of();
    private List<CpuSpec> cpuSpecs = List.of();
    private TickableSubscription reconnectSubscription;
    private int reconnectTicks;

    public GTNACraftingCPUInterfacePartMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, IO.NONE, args);
    }

    @Override
    protected int getInventorySize() {
        return 0;
    }

    @Override
    protected boolean shouldSubscribe() {
        return false;
    }

    @Override
    public Widget createUIWidget() {
        var group = new WidgetGroup(0, 0, 154, 32);
        group.addWidget(new LabelWidget(5, 10, "gtna.machine.crafting_cpu_interface.connection_only"));
        return group;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        rebuildClusters();
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
        configureCpus(List.of());
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        super.onMainNodeStateChanged(reason);
        rebuildClusters();
        scheduleCpuReconnect();
    }

    @Override
    public void saveCustomPersistedData(CompoundTag tag, boolean forDrop) {
        super.saveCustomPersistedData(tag, forDrop);
        ListTag cpuTags = new ListTag();
        for (CraftingCPUCluster cluster : clusters) {
            CompoundTag cpuTag = new CompoundTag();
            cluster.writeToNBT(cpuTag);
            cpuTags.add(cpuTag);
        }
        for (int i = clusters.size(); i < pendingClusterTags.size(); i++) {
            cpuTags.add(pendingClusterTags.get(i).copy());
        }
        tag.put(CPUS_TAG, cpuTags);
    }

    @Override
    public void loadCustomPersistedData(CompoundTag tag) {
        super.loadCustomPersistedData(tag);
        List<CompoundTag> saved = new ArrayList<>();
        if (tag.contains(CPUS_TAG, Tag.TAG_LIST)) {
            ListTag cpuTags = tag.getList(CPUS_TAG, Tag.TAG_COMPOUND);
            for (int i = 0; i < cpuTags.size(); i++) {
                saved.add(cpuTags.getCompound(i));
            }
        } else if (tag.contains(LEGACY_CPU_TAG, Tag.TAG_COMPOUND)) {
            saved.add(tag.getCompound(LEGACY_CPU_TAG));
        }
        pendingClusterTags = saved;
    }

    public void configureCpus(List<CpuSpec> specs) {
        if (cpuSpecs.equals(specs)) {
            return;
        }
        cpuSpecs = List.copyOf(specs);
        rebuildClusters();
        notifyCpuChanged();
        scheduleCpuReconnect();
    }

    public List<CraftingCPUCluster> getClusters() {
        if (!isFormed() || !getMainNode().isActive() || clusters.size() < cpuSpecs.size()) {
            return List.of();
        }
        return List.copyOf(clusters.subList(0, cpuSpecs.size()));
    }

    public int getConfiguredCpuCount() {
        return cpuSpecs.size();
    }

    public int getBusyCpuCount() {
        int busy = 0;
        for (CraftingCPUCluster cluster : getClusters()) {
            if (cluster.craftingLogic.hasJob()) busy++;
        }
        return busy;
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
            configureCpus(hyperCore.getCpuSpecs());
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
        rebuildClusters();
        notifyCpuChanged();

        IGridNode node = getMainNode().getNode();
        if (node != null && node.getGrid() != null || reconnectTicks >= 100) {
            if (reconnectSubscription != null) {
                reconnectSubscription.unsubscribe();
                reconnectSubscription = null;
            }
        }
    }

    private void rebuildClusters() {
        if (isRemote()) {
            return;
        }
        for (int i = 0; i < cpuSpecs.size(); i++) {
            CpuSpec spec = cpuSpecs.get(i);
            CraftingCPUCluster cluster;
            if (i >= clusters.size()) {
                cluster = IGTNACraftingCPUCluster.create(this, machineSource, spec.storageBytes(),
                        spec.coProcessors(), i);
                clusters.add(cluster);
                if (i < pendingClusterTags.size()) {
                    cluster.readFromNBT(pendingClusterTags.get(i));
                }
            } else {
                cluster = clusters.get(i);
                IGTNACraftingCPUCluster bridge = IGTNACraftingCPUCluster.of(cluster);
                bridge.gtna$setMachine(this);
                bridge.gtna$setMachineSource(machineSource);
                bridge.gtna$setStorage(spec.storageBytes());
                bridge.gtna$setAccelerator(spec.coProcessors());
            }
        }
        if (clusters.size() >= pendingClusterTags.size()) pendingClusterTags = List.of();
    }

    private void notifyCpuChanged() {
        IGridNode node = getMainNode().getNode();
        if (node != null && node.getGrid() != null) {
            node.getGrid().postEvent(new GridCraftingCpuChange(node));
        }
    }
}
