package com.raishxn.gtna.integration.ae2.crafting;

import appeng.api.stacks.AEKeyType;

public interface IGTNAElapsedTimeTracker {

    void gtna$decrementItems(long itemDiff, AEKeyType keyType);

    void gtna$addMaxItems(long itemDiff, AEKeyType keyType);
}
