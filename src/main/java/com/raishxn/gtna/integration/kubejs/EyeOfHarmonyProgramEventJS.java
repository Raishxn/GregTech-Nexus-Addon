package com.raishxn.gtna.integration.kubejs;

import net.minecraft.resources.ResourceLocation;

import com.raishxn.gtna.common.data.multiblock.EyeOfHarmonyPrograms;
import dev.latvian.mods.kubejs.event.EventJS;

/** KubeJS event: Eye of Harmony planetary programs. See {@link GTNAStartupEvents}. */
public class EyeOfHarmonyProgramEventJS extends EventJS {

    public void add(String planetItem, String dimension, int rocketTier) {
        add(planetItem, dimension, rocketTier, null);
    }

    public void add(String planetItem, String dimension, int rocketTier, String stoneDust) {
        EyeOfHarmonyPrograms.register(parse(planetItem), parse(dimension), rocketTier,
                stoneDust == null ? null : parse(stoneDust));
    }

    public void remove(String planetItem) {
        EyeOfHarmonyPrograms.remove(parse(planetItem));
    }

    private static ResourceLocation parse(String id) {
        var location = ResourceLocation.tryParse(id);
        if (location == null) throw new IllegalArgumentException("Invalid id: " + id);
        return location;
    }
}
