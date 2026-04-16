package com.raishxn.gtna.mixin.ae2;

import appeng.api.stacks.AEKeyType;
import appeng.crafting.execution.ElapsedTimeTracker;
import com.raishxn.gtna.integration.ae2.crafting.IGTNAElapsedTimeTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ElapsedTimeTracker.class)
public abstract class ElapsedTimeTrackerMixin implements IGTNAElapsedTimeTracker {

    @Shadow(remap = false)
    abstract void decrementItems(long itemDiff, AEKeyType keyType);

    @Shadow(remap = false)
    abstract void addMaxItems(long itemDiff, AEKeyType keyType);

    @Override
    public void gtna$decrementItems(long itemDiff, AEKeyType keyType) {
        decrementItems(itemDiff, keyType);
    }

    @Override
    public void gtna$addMaxItems(long itemDiff, AEKeyType keyType) {
        addMaxItems(itemDiff, keyType);
    }
}
