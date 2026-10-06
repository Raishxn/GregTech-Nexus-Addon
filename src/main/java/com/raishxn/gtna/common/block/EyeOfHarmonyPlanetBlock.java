package com.raishxn.gtna.common.block;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

/** Persistent registry identity for a dimension selector; operation is connected in EOH-04. */
public class EyeOfHarmonyPlanetBlock extends Block {

    private final ResourceLocation dimension;

    public EyeOfHarmonyPlanetBlock(Properties properties, ResourceLocation dimension) {
        super(properties);
        this.dimension = dimension;
    }

    public ResourceLocation getDimension() {
        return dimension;
    }
}
