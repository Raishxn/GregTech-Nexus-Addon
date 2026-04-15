package com.raishxn.gtna.mixin.ae2;

import com.raishxn.gtna.integration.ae2.crafting.GTNAOptimizedCraftingCpuLogic;

import net.minecraft.core.BlockPos;

import appeng.crafting.execution.CraftingCpuLogic;
import appeng.me.cluster.implementations.CraftingCPUCluster;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CraftingCPUCluster.class, remap = false)
public abstract class CraftingCPUClusterMixin {

    @Mutable
    @Shadow
    @Final
    public CraftingCpuLogic craftingLogic;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void gtna$useOptimizedCraftingCpuLogic(BlockPos boundsMin, BlockPos boundsMax, CallbackInfo ci) {
        craftingLogic = new GTNAOptimizedCraftingCpuLogic((CraftingCPUCluster) (Object) this);
    }
}
