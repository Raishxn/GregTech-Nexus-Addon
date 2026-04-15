package com.raishxn.gtna.integration.kubejs;

import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;

import com.raishxn.gtna.api.machine.multiblock.GTNAPartAbility;

/**
 * Wrapper to expose GTNA PartAbilities to KubeJS scripts.
 * Accessible as GTNAPartAbility.THREAD_HATCH, etc.
 */
public class GTNAPartAbilityWrapper {

    public static final PartAbility THREAD_HATCH = GTNAPartAbility.THREAD_HATCH;
    public static final PartAbility ACCELERATE_HATCH = GTNAPartAbility.ACCELERATE_HATCH;
    public static final PartAbility OVERCLOCK_HATCH = GTNAPartAbility.OVERCLOCK_HATCH;
    public static final PartAbility OUTPUT_BOOST_HATCH = GTNAPartAbility.OUTPUT_BOOST_HATCH;
}
