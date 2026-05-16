package com.raishxn.gtna.common.machine.tesseract;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.Comparator;

public record TesseractDirectedTarget(GlobalPos pos, Direction face, int order) implements Comparable<TesseractDirectedTarget> {

    public static final Comparator<TesseractDirectedTarget> SORTER = Comparator.comparingInt(
            TesseractDirectedTarget::order);

    @Override
    public int compareTo(TesseractDirectedTarget other) {
        return SORTER.compare(this, other);
    }

    public String serialize() {
        return pos.dimension().location() + "|" + pos.pos().asLong() + "|" + face.get3DDataValue() + "|" + order;
    }

    public static TesseractDirectedTarget deserialize(String serialized) {
        String[] parts = serialized.split("\\|", 4);
        if (parts.length != 4) {
            throw new IllegalArgumentException("Invalid tesseract target: " + serialized);
        }
        ResourceLocation dimLoc = ResourceLocation.tryParse(parts[0]);
        if (dimLoc == null) throw new IllegalArgumentException("Invalid dimension: " + parts[0]);
        ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, dimLoc);
        BlockPos blockPos = BlockPos.of(Long.parseLong(parts[1]));
        Direction face = Direction.from3DDataValue(Integer.parseInt(parts[2]));
        int order = Integer.parseInt(parts[3]);
        return new TesseractDirectedTarget(GlobalPos.of(dimension, blockPos), face, order);
    }
}
