package com.raishxn.gtna.integration.ae2.crafting;

import net.minecraft.network.chat.Component;

import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.me.helpers.MachineSource;
import com.raishxn.gtna.common.machine.multiblock.part.ae.GTNACraftingCPUInterfacePartMachine;

public interface IGTNACraftingCPUCluster {

    void gtna$setMachine(GTNACraftingCPUInterfacePartMachine machine);

    void gtna$setStorage(long storage);

    void gtna$setAccelerator(int accelerator);

    void gtna$setMachineSource(MachineSource source);

    void gtna$setName(Component name);

    static IGTNACraftingCPUCluster of(CraftingCPUCluster cluster) {
        return (IGTNACraftingCPUCluster) (Object) cluster;
    }

    static CraftingCPUCluster create(GTNACraftingCPUInterfacePartMachine machine, MachineSource source, long storage,
                                     int accelerator) {
        CraftingCPUCluster cluster = new CraftingCPUCluster(machine.getPos(), machine.getPos());
        IGTNACraftingCPUCluster bridge = of(cluster);
        bridge.gtna$setMachine(machine);
        bridge.gtna$setMachineSource(source);
        bridge.gtna$setStorage(storage);
        bridge.gtna$setAccelerator(accelerator);
        bridge.gtna$setName(Component.translatable("gtna.ae2.cpu.nexus_hypercore"));
        return cluster;
    }
}
