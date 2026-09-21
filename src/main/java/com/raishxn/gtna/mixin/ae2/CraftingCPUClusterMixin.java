package com.raishxn.gtna.mixin.ae2;

import net.minecraft.world.level.Level;

import appeng.api.networking.IGridNode;
import appeng.blockentity.crafting.CraftingBlockEntity;
import appeng.crafting.execution.CraftingCpuLogic;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.me.helpers.MachineSource;
import com.raishxn.gtna.common.machine.multiblock.part.ae.GTNACraftingCPUInterfacePartMachine;
import com.raishxn.gtna.integration.ae2.crafting.GTNAOptimizedCraftingCpuLogic;
import com.raishxn.gtna.integration.ae2.crafting.IGTNACraftingCPUCluster;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = CraftingCPUCluster.class, remap = false)
public abstract class CraftingCPUClusterMixin implements IGTNACraftingCPUCluster {

    @Mutable
    @Shadow
    @Final
    public CraftingCpuLogic craftingLogic;

    @Shadow
    private long storage;

    @Shadow
    private int accelerator;

    @Shadow
    private MachineSource machineSrc;

    @Shadow
    private net.minecraft.network.chat.Component myName;

    @Shadow
    protected abstract CraftingBlockEntity getCore();

    @Unique
    private GTNACraftingCPUInterfacePartMachine gtna$interfaceMachine;

    /**
     * @author GTNA
     * @reason Let virtual Nexus ME Hypercore CPU clusters persist through their interface part.
     */
    @Overwrite
    public void markDirty() {
        if (gtna$interfaceMachine == null) {
            getCore().saveChanges();
        } else {
            gtna$interfaceMachine.onChanged();
        }
    }

    /**
     * @author GTNA
     * @reason Let virtual Nexus ME Hypercore CPU clusters use the interface AE2 node.
     */
    @Overwrite
    public IGridNode getNode() {
        if (gtna$interfaceMachine == null) {
            CraftingBlockEntity core = getCore();
            return core == null ? null : core.getActionableNode();
        }
        return gtna$interfaceMachine.getActionableNode();
    }

    /**
     * @author GTNA
     * @reason Let virtual Nexus ME Hypercore CPU clusters resolve their world from the interface part.
     */
    @Overwrite
    public Level getLevel() {
        if (gtna$interfaceMachine == null) {
            return getCore().getLevel();
        }
        return gtna$interfaceMachine.getLevel();
    }

    @Override
    public void gtna$setMachine(GTNACraftingCPUInterfacePartMachine machine) {
        this.gtna$interfaceMachine = machine;
        // Keep native AE2 crafting CPUs on AE2's own executor. The optimized executor is
        // specific to the virtual Nexus CPU and replacing every CraftingCPUCluster here
        // made a normal AE2 network vulnerable to GTNA-only scheduling regressions.
        if (machine != null && !(craftingLogic instanceof GTNAOptimizedCraftingCpuLogic)) {
            craftingLogic = new GTNAOptimizedCraftingCpuLogic((CraftingCPUCluster) (Object) this);
        }
    }

    @Override
    public void gtna$setStorage(long storage) {
        this.storage = Math.max(0L, storage);
    }

    @Override
    public void gtna$setAccelerator(int accelerator) {
        this.accelerator = Math.max(0, accelerator);
    }

    @Override
    public void gtna$setMachineSource(MachineSource source) {
        this.machineSrc = source;
    }

    @Override
    public void gtna$setName(net.minecraft.network.chat.Component name) {
        this.myName = name;
    }
}
