package com.raishxn.gtna.api.registry;

import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;

import com.raishxn.gtna.GTNACORE;

public class GTNARegistry {

    public static final GTRegistrate REGISTRATE = GTRegistrate.create(GTNACORE.MOD_ID);

    static {
        GTNARegistry.REGISTRATE.defaultCreativeTab((ResourceKey<CreativeModeTab>) null);
    }

    private GTNARegistry() {/**/}
}
