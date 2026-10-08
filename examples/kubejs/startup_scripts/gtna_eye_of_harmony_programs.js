// GTNA: Eye of Harmony planetary programs (startup script; runs on client and server).
// add(planetItem, dimension, gtnhRocketTier[, stoneDust]): the products are the GTCEu ore veins
// registered for that dimension, scaled with the GTNH rules for the rocket tier (0-9).
GTNAStartupEvents.eyeOfHarmony(event => {
    // Example: a KubeJS block as the selector of a modded dimension.
    // event.add('kubejs:titan_planet_block', 'mymod:titan', 5)
    // Example: hide a built-in program.
    // event.remove('gtna:eye_of_harmony_planet_glacio')
})
