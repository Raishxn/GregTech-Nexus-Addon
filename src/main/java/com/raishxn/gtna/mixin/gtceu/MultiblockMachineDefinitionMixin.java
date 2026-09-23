package com.raishxn.gtna.mixin.gtceu;

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.MultiblockShapeInfo;

import com.raishxn.gtna.api.machine.multiblock.GTNASubPatterns;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

/**
 * Adds each registered module (sub-pattern) as an extra page in the multiblock's JEI/XEI preview, so
 * the structure of an attachable module can be previewed next to the main multiblock.
 */
@Mixin(MultiblockMachineDefinition.class)
public abstract class MultiblockMachineDefinitionMixin {

    @Inject(method = "getMatchingShapes", at = @At("RETURN"), cancellable = true, remap = false)
    private void gtna$appendModuleShapes(CallbackInfoReturnable<List<MultiblockShapeInfo>> cir) {
        MultiblockMachineDefinition self = (MultiblockMachineDefinition) (Object) this;
        List<BlockPattern> subPatterns = GTNASubPatterns.get(self);
        if (subPatterns.isEmpty()) {
            return;
        }
        List<MultiblockShapeInfo> shapes = new ArrayList<>(cir.getReturnValue());
        for (BlockPattern sub : subPatterns) {
            if (sub == null) {
                continue;
            }
            int[] repetitions = new int[sub.aisleRepetitions.length];
            for (int i = 0; i < repetitions.length; i++) {
                repetitions[i] = sub.aisleRepetitions[i][0];
            }
            shapes.add(new MultiblockShapeInfo(sub.getPreview(repetitions)));
        }
        cir.setReturnValue(shapes);
    }
}
