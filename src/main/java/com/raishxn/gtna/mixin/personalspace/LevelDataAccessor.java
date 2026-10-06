package com.raishxn.gtna.mixin.personalspace;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.WritableLevelData;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Lets PersonalSpace give a runtime dimension its own time and weather data. */
@Mixin(Level.class)
public interface LevelDataAccessor {

    @Mutable
    @Accessor("levelData")
    void gtna$setLevelData(WritableLevelData data);
}
