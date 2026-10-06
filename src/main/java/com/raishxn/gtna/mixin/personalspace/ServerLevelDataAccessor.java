package com.raishxn.gtna.mixin.personalspace;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.ServerLevelData;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Lets PersonalSpace give a runtime dimension its own time and weather data. */
@Mixin(ServerLevel.class)
public interface ServerLevelDataAccessor {

    @Mutable
    @Accessor("serverLevelData")
    void gtna$setServerLevelData(ServerLevelData data);
}
