package com.raishxn.gtna.mixin.ae2;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;

import net.minecraft.core.Direction;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import com.raishxn.gtna.common.machine.tesseract.DirectedTesseractMachine;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(value = PatternProviderLogic.class, remap = false)
public abstract class PatternProviderLogicMixin {

    @Shadow
    @Final
    private PatternProviderLogicHost host;

    @Shadow
    @Final
    private IActionSource actionSource;

    @Shadow
    @Final
    private Set<AEKey> patternInputs;

    @Shadow
    protected abstract Set<Direction> getActiveSides();

    @Inject(method = "pushPattern", at = @At("HEAD"), cancellable = true, remap = false)
    private void gtna$pushToDirectedTesseract(IPatternDetails patternDetails, KeyCounter[] inputHolder,
                                              CallbackInfoReturnable<Boolean> cir) {
        var blockEntity = host.getBlockEntity();
        if (blockEntity == null || blockEntity.getLevel() == null) {
            return;
        }

        var level = blockEntity.getLevel();
        var pos = blockEntity.getBlockPos();
        for (Direction direction : getActiveSides()) {
            var adjacent = level.getBlockEntity(pos.relative(direction));
            if (!(adjacent instanceof MetaMachineBlockEntity machineBlockEntity)) {
                continue;
            }
            if (!(machineBlockEntity.getMetaMachine() instanceof DirectedTesseractMachine directedTesseract)) {
                continue;
            }
            cir.setReturnValue(directedTesseract.pushPatternFromProvider(
                    patternDetails, inputHolder, patternInputs, actionSource));
            return;
        }
    }
}
