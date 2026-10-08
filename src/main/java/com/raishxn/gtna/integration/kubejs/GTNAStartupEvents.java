package com.raishxn.gtna.integration.kubejs;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;

/**
 * KubeJS startup events exposed by GTNA. Startup scripts run on both client and server, so EMI/JEI and the
 * controller see the same Eye of Harmony programs.
 *
 * <pre>{@code
 * GTNAStartupEvents.eyeOfHarmony(event => {
 *     // planet item, dimension whose GTCEu ore veins become the products, GTNH rocket tier 0-9
 *     event.add('kubejs:titan_planet_block', 'mymod:titan', 5)
 *     // optional stone dust that pads the catalog (default gtceu:stone_dust)
 *     event.add('kubejs:io_planet_block', 'mymod:io', 5, 'gtceu:netherrack_dust')
 *     event.remove('gtna:eye_of_harmony_planet_glacio')
 * })
 * }</pre>
 */
public interface GTNAStartupEvents {

    EventGroup GROUP = EventGroup.of("GTNAStartupEvents");

    /** Add, replace or remove Eye of Harmony planetary programs. */
    EventHandler EYE_OF_HARMONY = GROUP.startup("eyeOfHarmony", () -> EyeOfHarmonyProgramEventJS.class);

    /** Called from common setup, only when KubeJS is loaded. */
    static void postEyeOfHarmony() {
        com.raishxn.gtna.common.data.multiblock.EyeOfHarmonyPrograms.reset();
        EYE_OF_HARMONY.post(new EyeOfHarmonyProgramEventJS());
    }
}
