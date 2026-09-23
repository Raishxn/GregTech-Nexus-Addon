package com.raishxn.gtna.common.data;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.resources.ResourceLocation;

import com.raishxn.gtna.api.machine.multiblock.GTNAPartAbility;
import com.raishxn.gtna.api.machine.multiblock.GTNASubPatterns;

import static com.gregtechceu.gtceu.api.machine.multiblock.PartAbility.PARALLEL_HATCH;
import static com.gregtechceu.gtceu.api.pattern.Predicates.*;

/**
 * GTNA module (sub-pattern) registrations. Modules attach to a multiblock's controller and merge
 * their parts into it, unlocking new abilities (see {@link GTNASubPatterns}).
 *
 * <p>
 * These are registered in Java for the machines GTNA ships; modpack creators can add more through
 * the KubeJS {@code GTNAServerEvents.subPatterns} event.
 */
public final class GTNAModules {

    private GTNAModules() {}

    public static void init() {
        registerElectricBlastFurnaceModule();
    }

    /**
     * Module for GTCEu's Electric Blast Furnace: a heatproof-casing block on the back of the furnace
     * that carries one Parallel Hatch plus GTNA's Overclock / Accelerate / Thread hatches (the latter
     * three already act on GTCEu multiblocks through GTNA's recipe-logic mixin). The cells overlapping
     * the furnace itself are {@code any()}.
     */
    private static void registerElectricBlastFurnaceModule() {
        GTNASubPatterns.register(new ResourceLocation("gtceu", "electric_blast_furnace"),
                definition -> FactoryBlockPattern.start()
                        .aisle("   ", "   ", "   ", "   ")
                        .aisle("   ", "   ", "   ", "   ")
                        .aisle(" ~ ", "   ", "   ", "   ")
                        .aisle("XXX", "XXX", "XXX", "XXX")
                        .aisle("XXX", "XXX", "XXX", "XXX")
                        .aisle("XXX", "XXX", "XXX", "XXX")
                        .where('~', controller(blocks(definition.getBlock())))
                        .where('X', blocks(GTBlocks.CASING_INVAR_HEATPROOF.get())
                                .or(abilities(PARALLEL_HATCH).setMaxGlobalLimited(1))
                                .or(abilities(GTNAPartAbility.OVERCLOCK_HATCH).setMaxGlobalLimited(1))
                                .or(abilities(GTNAPartAbility.ACCELERATE_HATCH).setMaxGlobalLimited(1))
                                .or(abilities(GTNAPartAbility.THREAD_HATCH).setMaxGlobalLimited(1))
                                .or(blocks(ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.Invar))))
                        .where(' ', any())
                        .build());
    }
}
