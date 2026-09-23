package com.raishxn.gtna.common.data;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import com.raishxn.gtna.api.machine.multiblock.GTNAPartAbility;
import com.raishxn.gtna.api.machine.multiblock.GTNASubPatterns;

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
     * Module for GTCEu's Electric Blast Furnace, ported from GTOCore
     * ({@code GTMachineModify#ELECTRIC_BLAST_FURNACE.setSubPatternFactory}): an invar heatproof shell
     * that wraps the front and sides of the furnace, made of heatproof casing, stainless-steel frames
     * and steel pipe casing. Its heatproof-casing cells accept the furnace's IO plus a <b>second
     * Energy Hatch</b> and one <b>Accelerate Hatch</b> — exactly what GTOCore's
     * {@code moduleTooltips(ACCELERATE_HATCH, EXTRA_ENERGY_HATCH)} advertises.
     *
     * <p>
     * Cells that overlap the furnace itself are {@code any()}.
     */
    private static void registerElectricBlastFurnaceModule() {
        GTNASubPatterns.register(new ResourceLocation("gtceu", "electric_blast_furnace"),
                definition -> FactoryBlockPattern.start()
                        .aisle("AAAAA", " DBD ", " DBD ", " CCC ")
                        .aisle("ACCCA", "BD DB", "BD DB", "CCCCC")
                        .aisle("A   A", "     ", "     ", "C   C")
                        .aisle("A   A", "B   B", "B   B", "C   C")
                        .aisle("A E A", "     ", "     ", "     ")
                        .where('A', blocks(GTBlocks.CASING_INVAR_HEATPROOF.get())
                                .or(autoAbilities(definition.getRecipeTypes()))
                                .or(abilities(PartAbility.INPUT_ENERGY).setMaxGlobalLimited(2))
                                .or(abilities(GTNAPartAbility.ACCELERATE_HATCH).setMaxGlobalLimited(1)))
                        .where('B', blocks(ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.StainlessSteel)))
                        .where('C', blocks(GTBlocks.CASING_INVAR_HEATPROOF.get()))
                        .where('D', blocks(GTBlocks.CASING_STEEL_PIPE.get()))
                        .where('E', controller(blocks(definition.getBlock())))
                        .where(' ', any())
                        .build(),
                Component.translatable("gtna.machine.electric_blast_furnace.module").withStyle(ChatFormatting.GOLD));
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
                GTNAModules::buildLiquefactionExtension,
                Component.translatable("gtna.machine.liquefaction_furnace.module").withStyle(ChatFormatting.GOLD));
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
                        .or(abilities(PartAbility.PARALLEL_HATCH).setMaxGlobalLimited(1))
                        .or(abilities(GTNAPartAbility.ACCELERATE_HATCH).setMaxGlobalLimited(1)))
                .where('B', blocks(GTBlocks.CASING_STAINLESS_TURBINE.get()))
                .where('C', controller(blocks(definition.getBlock())))
                .where('D', blocks(GTBlocks.CASING_STAINLESS_STEEL_GEARBOX.get()))
                .where('E', blocks(GTBlocks.CASING_STAINLESS_CLEAN.get()))
                .where('F', blocks(ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.StainlessSteel)))
                .where('G', blocks(GTBlocks.CASING_TITANIUM_PIPE.get()))
                .where(' ', any())
                .build();
    }
}
