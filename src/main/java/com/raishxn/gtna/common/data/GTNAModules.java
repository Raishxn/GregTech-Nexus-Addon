package com.raishxn.gtna.common.data;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import com.raishxn.gtna.api.machine.multiblock.GTNAPartAbility;
import com.raishxn.gtna.api.machine.multiblock.GTNASubPatterns;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

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
        registerEvaporationPlantModule();
    }

    /**
     * Module for GTCEu's Electric Blast Furnace, ported from GTOCore
     * ({@code GTMachineModify#ELECTRIC_BLAST_FURNACE.setSubPatternFactory}): an invar heatproof shell
     * that wraps the front and sides of the furnace, made of heatproof casing, stainless-steel frames
     * and steel pipe casing. Its heatproof-casing cells accept the furnace's IO plus one
     * additional Energy Hatch and one Accelerate Hatch — what GTOCore's
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
                                .or(autoAbilities(definition.getRecipeTypes(), false, false, true, true, true, true))
                                .or(wiredEnergyHatches().setMaxGlobalLimited(1))
                                .or(abilities(GTNAPartAbility.ACCELERATE_HATCH).setMaxGlobalLimited(1)))
                        .where('B', blocks(ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.StainlessSteel)))
                        .where('C', blocks(GTBlocks.CASING_INVAR_HEATPROOF.get()))
                        .where('D', blocks(GTBlocks.CASING_STEEL_PIPE.get()))
                        .where('E', controller(blocks(definition.getBlock())))
                        .where(' ', any())
                        .build(),
                moduleTooltips("accelerate", "extra_energy"));
    }

    /** GTCEu's wired 2A/4A/16A hatches, excluding GTNA wireless hatches with the same ability. */
    private static com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate wiredEnergyHatches() {
        Block[] blocks = Stream.of(GTMachines.ENERGY_INPUT_HATCH, GTMachines.ENERGY_INPUT_HATCH_4A,
                GTMachines.ENERGY_INPUT_HATCH_16A)
                .flatMap(Arrays::stream)
                .filter(Objects::nonNull)
                .map(MachineDefinition::getBlock)
                .toArray(Block[]::new);
        return blocks(blocks);
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
                moduleTooltips("parallel", "accelerate"));
    }

    /** The auxiliary-module text stays the same; each machine supplies only its unlocked hatches. */
    private static Component[] moduleTooltips(String... hatchIds) {
        MutableComponent hatchList = Component.empty();
        for (int i = 0; i < hatchIds.length; i++) {
            if (i > 0) hatchList.append(", ");
            hatchList.append(Component.translatable("gtna.machine.auxiliary_module.hatch." + hatchIds[i])
                    .withStyle(ChatFormatting.AQUA));
        }
        return new Component[] {
                Component.translatable("gtna.machine.auxiliary_module").withStyle(ChatFormatting.GOLD),
                Component.translatable("gtna.machine.auxiliary_module.description"),
                Component.translatable("gtna.machine.auxiliary_module.hatches"),
                Component.translatable("gtna.machine.auxiliary_module.unlocked", hatchList)
        };
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

    /** GTOCore's titanium auxiliary tower for the Evaporation Plant. */
    private static void registerEvaporationPlantModule() {
        GTNASubPatterns.register(new ResourceLocation("gtna", "evaporation_plant"),
                GTNAModules::buildEvaporationPlantExtension,
                moduleTooltips("parallel", "accelerate"));
    }

    private static BlockPattern buildEvaporationPlantExtension(MultiblockMachineDefinition definition) {
        return FactoryBlockPattern.start()
                .aisle("HFH AAA", "FFF ACA", "FFF ACA", "FFF ACA", " G  AAA")
                .aisle("FGGAAAA", "FDGDA A", "FDGDA A", "FDGAA A", " G  AEA")
                .aisle("    AAA", "    A A", "    A A", "    A A", "    AAA")
                .aisle("   AAAA", "   DA A", "   DA A", "   DA A", "   AAEA")
                .aisle("    AAA", " B  ACA", "    ACA", "    ACA", "    AAA")
                .where('A', blocks(GTBlocks.CASING_TITANIUM_STABLE.get()))
                .where('B', controller(blocks(definition.getBlock())))
                .where('C', blocks(GTBlocks.FIREBOX_TITANIUM.get()))
                .where('D', blocks(GTBlocks.CASING_TITANIUM_PIPE.get()))
                .where('E', abilities(PartAbility.MUFFLER))
                .where('F', blocks(GTNABlocks.STAINLESS_EVAPORATION_CASING.get())
                        .or(GTNAMachines3.nonSteamFluidInputHatches())
                        .or(abilities(PartAbility.EXPORT_FLUIDS))
                        .or(abilities(PartAbility.PARALLEL_HATCH).setMaxGlobalLimited(1))
                        .or(abilities(GTNAPartAbility.ACCELERATE_HATCH).setMaxGlobalLimited(1)))
                .where('G', blocks(GTNABlocks.STAINLESS_EVAPORATION_CASING.get()))
                .where('H', frames(GTMaterials.Aluminium))
                .where(' ', any())
                .build();
    }
}
