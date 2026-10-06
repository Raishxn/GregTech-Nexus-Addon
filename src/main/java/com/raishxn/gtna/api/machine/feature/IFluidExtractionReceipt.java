package com.raishxn.gtna.api.machine.feature;

import net.minecraft.world.level.material.Fluid;

/** Receipt of an actual successful output, held by a native fluid drill's logic. */
public interface IFluidExtractionReceipt {

    Fluid gtna$getExtractedFluid();
}
