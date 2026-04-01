package com.raishxn.gtna.common.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class NexusCapacitorBlock extends Block {

    private final int tier;
    private final long unitCapacity;

    public NexusCapacitorBlock(BlockBehaviour.Properties properties, int tier, long unitCapacity) {
        super(properties
                .mapColor(MapColor.METAL)
                .strength(5.0f, 6.0f)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops());
        this.tier = tier;
        this.unitCapacity = unitCapacity;
    }

    public int getTier() {
        return tier;
    }

    public long getUnitCapacity() {
        return unitCapacity;
    }
}
