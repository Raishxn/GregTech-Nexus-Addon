package com.raishxn.gtna.common.data;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import com.raishxn.gtna.GTNACORE;

public final class GTNADamageTypes {

    public static final ResourceKey<DamageType> REALITY_RIP = ResourceKey.create(Registries.DAMAGE_TYPE,
            new ResourceLocation(GTNACORE.MOD_ID, "reality_rip"));

    private GTNADamageTypes() {}

    public static DamageSource realityRip(Level level, Entity attacker) {
        return new DamageSource(
                level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(REALITY_RIP),
                attacker);
    }

    public static boolean isRealityRip(DamageSource source) {
        return source.is(REALITY_RIP);
    }
}
