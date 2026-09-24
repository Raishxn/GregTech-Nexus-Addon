package com.raishxn.gtna.mixin.ae2;

import net.minecraft.world.level.Level;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.CalculationStrategy;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.crafting.ICraftingSimulationRequester;
import appeng.api.stacks.AEKey;
import appeng.me.service.CraftingService;
import appeng.me.service.helpers.NetworkCraftingProviders;
import com.raishxn.gtna.common.machine.multiblock.part.ae.GTNACraftingCPUInterfacePartMachine;
import com.raishxn.gtna.planner.neoforge.crafting.Ae2PlannerBridge;
import com.raishxn.gtna.planner.neoforge.crafting.PlannerGridService;
import com.raishxn.gtna.planner.neoforge.crafting.PlannerRevisionSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.Future;

@Mixin(value = CraftingService.class, remap = false)
public abstract class CoreCraftingPlannerServiceMixin implements PlannerGridService {

    /**
     * Guards the re-entrant call that hands one discarded attempt to AE2's own planner. The call goes
     * back through this same injection point, and the guard makes it skip the bridge entirely.
     */
    @Unique
    private static final ThreadLocal<Boolean> raishxcore$inVanillaFallback = ThreadLocal
            .withInitial(() -> Boolean.FALSE);
    @Shadow
    @Final
    private IGrid grid;
    @Shadow
    @Final
    private NetworkCraftingProviders craftingProviders;
    @Unique
    private Ae2PlannerBridge raishxcore$planner;

    @Inject(method = "beginCraftingCalculation", at = @At("HEAD"), cancellable = true)
    private void raishxcore$planFromSnapshot(Level level, ICraftingSimulationRequester requester,
                                             AEKey target, long amount, CalculationStrategy strategy,
                                             CallbackInfoReturnable<Future<ICraftingPlan>> cir) {
        if (raishxcore$inVanillaFallback.get() ||
                grid.getMachines(GTNACraftingCPUInterfacePartMachine.class).isEmpty())
            return;
        if (!com.raishxn.gtna.planner.CoreConfig.isPlannerEnabled()) {
            // Kill-switch: leave AE2's own planner in charge of this request entirely.
            if (raishxcore$planner == null) raishxcore$planner = new Ae2PlannerBridge();
            raishxcore$planner.recordDisabled();
            return;
        }
        if (raishxcore$planner == null) raishxcore$planner = new Ae2PlannerBridge();
        var revisions = (PlannerRevisionSource) craftingProviders;
        Future<ICraftingPlan> result = raishxcore$planner.begin(new Ae2PlannerBridge.BeginRequest(
                level, grid, requester, target, amount, strategy,
                revisions.raishxcore$getPatternRevision(),
                revisions::raishxcore$getPatternRevision,
                () -> raishxcore$planWithVanillaAe2(level, requester, target, amount, strategy)));
        if (result != null) cir.setReturnValue(result);
    }

    /**
     * AE2's planner, used only when a RaishxCore capture was discarded in full and its caller already
     * holds a deferred future. Skipping the injection keeps the bridge from capturing the graph again.
     */
    @Unique
    private Future<ICraftingPlan> raishxcore$planWithVanillaAe2(Level level, ICraftingSimulationRequester requester,
                                                                AEKey target, long amount,
                                                                CalculationStrategy strategy) {
        if (raishxcore$inVanillaFallback.get()) return null;
        raishxcore$inVanillaFallback.set(Boolean.TRUE);
        try {
            return ((CraftingService) (Object) this).beginCraftingCalculation(
                    level, requester, target, amount, strategy);
        } finally {
            raishxcore$inVanillaFallback.set(Boolean.FALSE);
        }
    }

    @Inject(method = "removeNode", at = @At("HEAD"))
    private void raishxcore$cancelPlanningForGridChange(IGridNode node, CallbackInfo ci) {
        if (raishxcore$planner == null) return;
        if (grid.size() <= 1) raishxcore$planner.close();
        else raishxcore$planner.invalidate("grid node removed");
    }

    @Override
    public Ae2PlannerBridge.Diagnostics raishxcore$getPlannerDiagnostics() {
        if (raishxcore$planner == null) raishxcore$planner = new Ae2PlannerBridge();
        return raishxcore$planner.diagnostics();
    }
}
