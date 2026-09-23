package com.raishxn.gtna.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import com.raishxn.gtna.api.machine.multiblock.GTNAPartAbility;
import com.raishxn.gtna.api.machine.multiblock.ISubPatternMachine;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.gregtechceu.gtceu.api.machine.multiblock.PartAbility.PARALLEL_HATCH;
import static com.gregtechceu.gtceu.api.pattern.Predicates.*;

/**
 * GTOCore {@code liquefaction_furnace} port (LGPLv3, attribution via {@code GTNASources}): a coil
 * multiblock that melts an item into a fluid; the recipe's {@code ebf_temp} sets the required coil
 * temperature.
 *
 * <p>
 * GTOCore gives it a <b>sub-pattern</b>: a stainless-steel tower that can be attached to the
 * controller and adds Parallel / Accelerate hatches. GTNA reproduces it with the native
 * {@link ISubPatternMachine} mechanic.
 */
public class LiquefactionFurnaceMachine extends CoilWorkableElectricMultipleRecipesMachine
                                        implements ISubPatternMachine {

    @Nullable
    private List<BlockPattern> subPatterns;

    public LiquefactionFurnaceMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    public List<BlockPattern> gtna$getSubPatterns() {
        if (subPatterns == null) {
            subPatterns = List.of(buildExtension(getDefinition()));
        }
        return subPatterns;
    }

    /**
     * GTOCore's sub-pattern ({@code MultiBlockB.LIQUEFACTION_FURNACE.addSubPattern}): a 7x3x7
     * stainless tower whose heatproof-casing cells accept the machine's IO plus one Parallel Hatch and
     * one Accelerate Hatch.
     */
    private static BlockPattern buildExtension(MultiblockMachineDefinition definition) {
        return FactoryBlockPattern.start()
                .aisle("AAA    ", "AAA    ", "AAA    ")
                .aisle("BBB    ", "BDB    ", "BBB    ")
                .aisle("BEBF   ", "E EF   ", "BEBF   ")
                .aisle("BEBG   ", "E E    ", "BEBG   ")
                .aisle("BEBF   ", "E EF   ", "BEBF   ")
                .aisle("BBB   C", "BDB    ", "BBB    ")
                .aisle("AAA    ", "AAA    ", "AAA    ")
                .where('A', blocks(GTBlocks.CASING_INVAR_HEATPROOF.get())
                        .or(autoAbilities(definition.getRecipeTypes()))
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
