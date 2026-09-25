package com.raishxn.gtna.mixin.ae2;

import appeng.api.networking.crafting.ICraftingPlan;
import appeng.menu.guisync.GuiSync;
import appeng.menu.me.crafting.CraftConfirmMenu;
import com.raishxn.gtna.planner.neoforge.crafting.CraftConfirmPlannerOriginAccess;
import com.raishxn.gtna.planner.neoforge.crafting.PlanningOrigin;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.Future;

/** Syncs the actual engine of a completed request to AE2's confirmation screen. */
@Mixin(value = CraftConfirmMenu.class, remap = false)
public abstract class CoreCraftConfirmMenuMixin implements CraftConfirmPlannerOriginAccess {

    @Shadow
    @Nullable
    private Future<ICraftingPlan> job;

    // 127 is outside AE2's current CraftConfirmMenu ids (1-8). Enum sync keeps ordinary AE2
    // futures badge-free and lets a deferred capture truthfully report its AE2 fallback.
    @GuiSync(127)
    public PlanningOrigin raishxcore$planningOrigin = PlanningOrigin.NONE;

    @Inject(method = "planJob", at = @At("HEAD"))
    private void raishxcore$clearPlanningOrigin(CallbackInfoReturnable<Boolean> cir) {
        raishxcore$planningOrigin = PlanningOrigin.NONE;
    }

    // AbstractContainerMenu's override is SRG-named in the distributed AE2 jar.
    @Inject(method = { "broadcastChanges", "m_38946_" },
            remap = false,
            require = 0,
            at = @At(value = "INVOKE",
                     target = "Ljava/util/concurrent/Future;get()Ljava/lang/Object;",
                     shift = At.Shift.BEFORE))
    private void raishxcore$recordCompletedPlanningOrigin(CallbackInfo ci) {
        raishxcore$planningOrigin = PlanningOrigin.of(job);
    }

    @Override
    public PlanningOrigin raishxcore$getPlanningOrigin() {
        return raishxcore$planningOrigin;
    }
}
