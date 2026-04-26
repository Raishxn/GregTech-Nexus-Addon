package com.raishxn.gtna.mixin.ae2;

import net.minecraft.nbt.CompoundTag;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.blockentity.crafting.CraftingBlockEntity;
import appeng.crafting.CraftingLink;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.me.service.CraftingService;
import com.raishxn.gtna.common.machine.multiblock.part.ae.GTNACraftingCPUInterfacePartMachine;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(value = CraftingService.class, remap = false)
public abstract class CraftingServiceMixin {

    @Shadow
    @Final
    private IGrid grid;

    @Shadow
    @Final
    private Set<CraftingCPUCluster> craftingCPUClusters;

    @Shadow
    private boolean updateList;

    @Shadow
    public abstract void addLink(CraftingLink link);

    @Inject(method = "addNode", at = @At("TAIL"))
    private void gtna$markCpuListDirtyForNexusInterface(IGridNode gridNode, CompoundTag savedData, CallbackInfo ci) {
        if (gridNode.getOwner() instanceof GTNACraftingCPUInterfacePartMachine) {
            updateList = true;
        }
    }

    /**
     * @author GTNA
     * @reason Include Nexus ME Hypercore Crafting CPU Interface clusters in AE2's CPU list.
     */
    @Overwrite
    private void updateCPUClusters() {
        craftingCPUClusters.clear();
        for (GTNACraftingCPUInterfacePartMachine machine : grid.getMachines(GTNACraftingCPUInterfacePartMachine.class)) {
            for (CraftingCPUCluster cluster : machine.getClusters()) {
                craftingCPUClusters.add(cluster);
                ICraftingLink maybeLink = cluster.craftingLogic.getLastLink();
                if (maybeLink != null) {
                    addLink((CraftingLink) maybeLink);
                }
            }
        }
        for (CraftingBlockEntity blockEntity : grid.getMachines(CraftingBlockEntity.class)) {
            CraftingCPUCluster cluster = blockEntity.getCluster();
            if (cluster != null) {
                craftingCPUClusters.add(cluster);
                ICraftingLink maybeLink = cluster.craftingLogic.getLastLink();
                if (maybeLink != null) {
                    addLink((CraftingLink) maybeLink);
                }
            }
        }
    }
}
