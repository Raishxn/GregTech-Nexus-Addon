package com.raishxn.gtna.mixin.gtceu;

import com.gregtechceu.gtceu.api.pattern.MultiblockState;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import com.raishxn.gtna.common.item.terminal.NexusBuildCheckGuard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** The terminal checks its target once after the build instead of once per placed block. */
@Mixin(value = MultiblockState.class, remap = false)
public abstract class MultiblockStateBuildMixin {

    @Inject(method = "onBlockStateChanged", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void gtna$deferTerminalBuildCheck(BlockPos pos, BlockState state, CallbackInfo ci) {
        if (NexusBuildCheckGuard.skips((MultiblockState) (Object) this, pos)) {
            ci.cancel();
        }
    }
}
