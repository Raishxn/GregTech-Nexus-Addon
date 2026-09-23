package com.raishxn.gtna.common.data;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
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
        registerLiquefactionFurnaceModule();
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

    /**
     * Module for GTNA's Liquefaction Furnace (GTOCore port): GTOCore's stainless-steel tower that
     * sits beside the furnace and adds one Parallel Hatch and one Accelerate Hatch. The furnace is a
     * normal coil machine that accepts none of those on its own, so the module is the only way to get
     * them.
     *
     * <p>
     * GTOCore anchors the tower at the controller with the <i>default</i> pattern directions, which
     * places it to the right of the main structure; the cells that overlap the furnace are
     * {@code any()}.
     */
    private static void registerLiquefactionFurnaceModule() {
        GTNASubPatterns.register(new ResourceLocation("gtna", "liquefaction_furnace"),
                GTNAModules::buildLiquefactionExtension);
    }

    private static BlockPattern buildLiquefactionExtension(MultiblockMachineDefinition definition) {
        return FactoryBlockPattern.start()
                .aisle("AAA    ", "AAA    ", "AAA    ")
                .aisle("BBB    ", "BDB    ", "BBB    ")
                .aisle("BEBF   ", "E EF   ", "BEBF   ")
                .aisle("BEBG   ", "E E    ", "BEBG   ")
                .aisle("BEBF   ", "E EF   ", "BEBF   ")
                .aisle("BBB   C", "BDB    ", "BBB    ")
                .aisle("AAA    ", "AAA    ", "AAA    ")
                .where('A', blocks(GTBlocks.CASING_INVAR_HEATPROOF.get())
                        .or(abilities(PARALLEL_HATCH).setMaxGlobalLimited(1))
                        .or(abilities(GTNAPartAbility.ACCELERATE_HATCH).setMaxGlobalLimited(1)))
                .where('B', blocks(GTBlocks.CASING_STAINLESS_TURBINE.get()))
                .where('C', controller(blocks(definition.get())))
                .where('D', blocks(GTBlocks.CASING_STAINLESS_STEEL_GEARBOX.get()))
                .where('E', blocks(GTBlocks.CASING_STAINLESS_CLEAN.get()))
                .where('F', blocks(ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.StainlessSteel)))
                .where('G', blocks(GTBlocks.CASING_TITANIUM_PIPE.get()))
                .where(' ', any())
                .build();
    }
}
