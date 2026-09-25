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
import appeng.me.helpers.MachineSource;
import com.raishxn.gtna.common.machine.multiblock.energy.NexusMEHyperCoreMachine;
import com.raishxn.gtna.integration.ae2.crafting.NexusSharedCraftingCpuPool;

/** AE2 connection for one shared Hypercore CPU with a per-request pool of jobs. */
public class GTNACraftingCPUInterfacePartMachine extends MEBusPartMachine implements IActionHost {

    private static final String POOL_TAG = "NexusSharedCraftingPool";
    private static final String LEGACY_CPUS_TAG = "NexusCraftingCpus";
    private static final String LEGACY_CPU_TAG = "NexusCraftingCpu";

    private final NexusSharedCraftingCpuPool cpuPool = new NexusSharedCraftingCpuPool(this, new MachineSource(this));
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
        configurePool(0L, 0L, false);
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        super.onMainNodeStateChanged(reason);
        scheduleCpuReconnect();
        notifyCpuChanged();
    }

    @Override
    public void saveCustomPersistedData(CompoundTag tag, boolean forDrop) {
        super.saveCustomPersistedData(tag, forDrop);
        CompoundTag poolTag = new CompoundTag();
        cpuPool.writeToNBT(poolTag);
        tag.put(POOL_TAG, poolTag);
    }

    @Override
    public void loadCustomPersistedData(CompoundTag tag) {
        super.loadCustomPersistedData(tag);
        if (tag.contains(POOL_TAG, Tag.TAG_COMPOUND)) {
            cpuPool.readFromNBT(tag.getCompound(POOL_TAG));
        } else if (tag.contains(LEGACY_CPUS_TAG, Tag.TAG_LIST)) {
            cpuPool.readLegacyCpus(tag.getList(LEGACY_CPUS_TAG, Tag.TAG_COMPOUND));
        } else if (tag.contains(LEGACY_CPU_TAG, Tag.TAG_COMPOUND)) {
            ListTag legacy = new ListTag();
            legacy.add(tag.getCompound(LEGACY_CPU_TAG));
            cpuPool.readLegacyCpus(legacy);
        }
    }

    public void configurePool(long storage, long coProcessors, boolean infinite) {
        cpuPool.reconfigure(storage, coProcessors, infinite);
        scheduleCpuReconnect();
    }

    public NexusSharedCraftingCpuPool getCpuPool() {
        return cpuPool;
    }

    public boolean isPoolOnline() {
        return isFormed() && getMainNode().isActive();
    }

    public int getBusyCpuCount() {
        return cpuPool.getActiveJobCount();
    }

    public void onChanged() {
        markDirty();
    }

    @Override
    public IGridNode getActionableNode() {
        return getMainNode().getNode();
    }

    public void notifyCpuChanged() {
        IGridNode node = getMainNode().getNode();
        if (node != null && node.getGrid() != null) {
            node.getGrid().postEvent(new GridCraftingCpuChange(node));
        }
    }

    private void configureFromController(IMultiController controller) {
        if (controller instanceof NexusMEHyperCoreMachine hyperCore) {
            configurePool(hyperCore.getAeStorageBytes(), hyperCore.getAeCoProcessors(),
                    hyperCore.isTranscendentMode());
        }
    }

    private void scheduleCpuReconnect() {
        if (isRemote() || reconnectSubscription != null && reconnectSubscription.isStillSubscribed()) return;
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
        notifyCpuChanged();
        IGridNode node = getMainNode().getNode();
        if (node != null && node.getGrid() != null || reconnectTicks >= 100) {
            reconnectSubscription.unsubscribe();
            reconnectSubscription = null;
        }
    }
}
