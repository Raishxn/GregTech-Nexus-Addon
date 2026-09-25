package com.raishxn.gtna.mixin.ae2;

import net.minecraft.nbt.CompoundTag;

import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.crafting.ICraftingSubmitResult;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.blockentity.crafting.CraftingBlockEntity;
import appeng.crafting.CraftingLink;
import appeng.crafting.execution.CraftingSubmitResult;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.me.service.CraftingService;
import com.google.common.collect.ImmutableSet;
import com.raishxn.gtna.common.machine.multiblock.part.ae.GTNACraftingCPUInterfacePartMachine;
import com.raishxn.gtna.integration.ae2.crafting.NexusSharedCraftingCpuPool;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

/** Integrates the Hypercore's shared CPU pool with AE2's crafting service. */
@Mixin(value = CraftingService.class, remap = false)
public abstract class CraftingServiceMixin {

    @Shadow
    @Final
    private IGrid grid;
    @Shadow
    @Final
    private IEnergyService energyGrid;
    @Shadow
    @Final
    private Set<CraftingCPUCluster> craftingCPUClusters;
    @Shadow
    @Final
    private Set<AEKey> currentlyCrafting;
    @Shadow
    private boolean updateList;
    @Shadow
    private long lastProcessedCraftingLogicChangeTick;

    @Shadow
    public abstract void addLink(CraftingLink link);

    @Inject(method = "addNode", at = @At("TAIL"))
    private void gtna$connectPool(IGridNode node, CompoundTag savedData, CallbackInfo ci) {
        if (node.getOwner() instanceof GTNACraftingCPUInterfacePartMachine machine) {
            updateList = true;
            machine.getCpuPool().restoreCraftingLinks(link -> {
                if (link instanceof CraftingLink concrete) addLink(concrete);
            });
        }
    }

    /** @author GTNA @reason Preserve native AE2 CPUs; Hypercore pools are exposed separately. */
    @Overwrite
    private void updateCPUClusters() {
        craftingCPUClusters.clear();
        for (CraftingBlockEntity blockEntity : grid.getMachines(CraftingBlockEntity.class)) {
            CraftingCPUCluster cluster = blockEntity.getCluster();
            if (cluster != null) {
                craftingCPUClusters.add(cluster);
                ICraftingLink link = cluster.craftingLogic.getLastLink();
                if (link instanceof CraftingLink concrete) addLink(concrete);
            }
        }
    }

    @Inject(method = "getCpus", at = @At("RETURN"), cancellable = true)
    private void gtna$showSharedPool(CallbackInfoReturnable<ImmutableSet<ICraftingCPU>> cir) {
        ImmutableSet.Builder<ICraftingCPU> cpus = ImmutableSet.builder();
        cpus.addAll(cir.getReturnValue());
        for (GTNACraftingCPUInterfacePartMachine machine : grid
                .getMachines(GTNACraftingCPUInterfacePartMachine.class)) {
            NexusSharedCraftingCpuPool pool = machine.getCpuPool();
            if (!pool.isActive()) continue;
            cpus.addAll(pool.getActiveCpus());
            if (pool.getAvailableStorage() > 0L) cpus.add(pool);
        }
        cir.setReturnValue(cpus.build());
    }

    @Inject(method = "submitJob", at = @At("HEAD"), cancellable = true)
    private void gtna$submitToSharedPool(ICraftingPlan plan, @Nullable ICraftingRequester requester,
                                         @Nullable ICraftingCPU target, boolean prioritizePower,
                                         IActionSource source, CallbackInfoReturnable<ICraftingSubmitResult> cir) {
        if (plan.simulation()) return;
        for (GTNACraftingCPUInterfacePartMachine machine : grid
                .getMachines(GTNACraftingCPUInterfacePartMachine.class)) {
            NexusSharedCraftingCpuPool pool = machine.getCpuPool();
            if (target == pool) {
                cir.setReturnValue(pool.submitJob(grid, plan, source, requester));
                return;
            }
            if (target != null && pool.containsCpu(target)) {
                cir.setReturnValue(CraftingSubmitResult.CPU_BUSY);
                return;
            }
            if (target == null && pool.isActive() && pool.getAvailableStorage() >= plan.bytes()) {
                cir.setReturnValue(pool.submitJob(grid, plan, source, requester));
                return;
            }
        }
    }

    @Inject(method = "onServerEndTick",
            at = @At(value = "FIELD",
                     target = "Lappeng/me/service/CraftingService;lastProcessedCraftingLogicChangeTick:J",
                     opcode = Opcodes.GETFIELD,
                     ordinal = 0))
    private void gtna$tickSharedJobs(CallbackInfo ci) {
        long latest = Long.MIN_VALUE;
        for (GTNACraftingCPUInterfacePartMachine machine : grid
                .getMachines(GTNACraftingCPUInterfacePartMachine.class)) {
            NexusSharedCraftingCpuPool pool = machine.getCpuPool();
            if (!pool.isActive()) continue;
            latest = Math.max(latest, pool.tick(energyGrid, (CraftingService) (Object) this));
        }
        if (latest != Long.MIN_VALUE) lastProcessedCraftingLogicChangeTick = -1L;
    }

    @Inject(method = "onServerEndTick",
            at = @At(value = "FIELD",
                     target = "Lappeng/me/service/CraftingService;interests:Lcom/google/common/collect/Multimap;",
                     opcode = Opcodes.GETFIELD,
                     ordinal = 0))
    private void gtna$includeSharedWaitingKeys(CallbackInfo ci) {
        for (GTNACraftingCPUInterfacePartMachine machine : grid
                .getMachines(GTNACraftingCPUInterfacePartMachine.class)) {
            machine.getCpuPool().addWaitingKeys(currentlyCrafting);
        }
    }

    @Inject(method = "insertIntoCpus", at = @At("RETURN"), cancellable = true)
    private void gtna$insertIntoSharedJobs(AEKey what, long amount, Actionable mode,
                                           CallbackInfoReturnable<Long> cir) {
        long inserted = cir.getReturnValue();
        for (GTNACraftingCPUInterfacePartMachine machine : grid
                .getMachines(GTNACraftingCPUInterfacePartMachine.class)) {
            if (inserted >= amount) break;
            inserted += machine.getCpuPool().insert(what, amount - inserted, mode);
        }
        cir.setReturnValue(inserted);
    }

    @Inject(method = "getRequestedAmount", at = @At("RETURN"), cancellable = true)
    private void gtna$requestedFromSharedJobs(AEKey what, CallbackInfoReturnable<Long> cir) {
        long requested = cir.getReturnValue();
        for (GTNACraftingCPUInterfacePartMachine machine : grid
                .getMachines(GTNACraftingCPUInterfacePartMachine.class)) {
            long addition = machine.getCpuPool().getRequestedAmount(what);
            requested = requested >= Long.MAX_VALUE - addition ? Long.MAX_VALUE : requested + addition;
        }
        cir.setReturnValue(requested);
    }

    @Inject(method = "hasCpu", at = @At("HEAD"), cancellable = true)
    private void gtna$hasSharedCpu(ICraftingCPU cpu, CallbackInfoReturnable<Boolean> cir) {
        for (GTNACraftingCPUInterfacePartMachine machine : grid
                .getMachines(GTNACraftingCPUInterfacePartMachine.class)) {
            if (machine.getCpuPool().containsCpu(cpu)) {
                cir.setReturnValue(true);
                return;
            }
        }
    }
}
