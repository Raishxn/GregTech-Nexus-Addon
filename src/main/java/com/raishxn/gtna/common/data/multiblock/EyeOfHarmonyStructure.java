package com.raishxn.gtna.common.data.multiblock;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.gregtechceu.gtceu.api.pattern.error.PatternStringError;
import com.gregtechceu.gtceu.integration.ae2.machine.feature.multiblock.IMEStockingPart;

import com.lowdragmc.lowdraglib.utils.BlockInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.EntityBlock;

import com.google.common.base.Suppliers;
import com.raishxn.gtna.common.block.EyeOfHarmonyFieldBlock;
import com.raishxn.gtna.common.block.EyeOfHarmonyFieldBlock.Family;
import com.raishxn.gtna.common.data.GTNAEyeOfHarmonyContent;
import com.tterrag.registrate.util.entry.BlockEntry;

import java.util.Arrays;

import static com.gregtechceu.gtceu.api.pattern.Predicates.*;

/** GTNH geometry/fields with the author's ME-only port policy; bottom-row/front-aisle convention. */
public final class EyeOfHarmonyStructure {

    private EyeOfHarmonyStructure() {}

    public static String tierKey(Family family) {
        return "EOHFieldTier_" + family.id;
    }

    public static BlockPattern create(MultiblockMachineDefinition definition) {
        var pattern = FactoryBlockPattern.start();
        for (String[] aisle : EyeOfHarmonyAisles.AISLES) pattern.aisle(aisle);
        return pattern.where('~', controller(blocks(definition.get())))
                .where('A', blocks(GTNAEyeOfHarmonyContent.SPATIAL_CASING.get()))
                .where('D', blocks(GTNAEyeOfHarmonyContent.TEMPORAL_CASING.get()))
                .where('B', blocks(GTNAEyeOfHarmonyContent.BOUNDARY_CASING.get())
                        .or(boundaryPort(PartAbility.IMPORT_ITEMS, 1))
                        .or(boundaryPort(PartAbility.IMPORT_FLUIDS, 2))
                        .or(boundaryPort(PartAbility.EXPORT_ITEMS, 1))
                        .or(boundaryPort(PartAbility.EXPORT_FLUIDS, 1)))
                .where('E', uniformField(Family.ACCELERATION, GTNAEyeOfHarmonyContent.ACCELERATION_FIELDS))
                .where('F', uniformField(Family.COMPRESSION, GTNAEyeOfHarmonyContent.COMPRESSION_FIELDS))
                .where('G', uniformField(Family.STABILISATION, GTNAEyeOfHarmonyContent.STABILISATION_FIELDS))
                // StructureLib's unmapped spaces are ignored, not mandatory air.
                .where(' ', any()).build();
    }

    private static TraceabilityPredicate uniformField(Family family, BlockEntry<EyeOfHarmonyFieldBlock>[] entries) {
        return new TraceabilityPredicate(state -> {
            if (!(state.getBlockState().getBlock() instanceof EyeOfHarmonyFieldBlock field) ||
                    field.getFamily() != family)
                return false;
            String key = tierKey(family);
            Integer matched = state.getMatchContext().get(key);
            if (matched != null && matched != field.getFieldTier()) {
                state.setError(new PatternStringError("gtna.eoh.structure.uniform_" + family.id));
                return false;
            }
            state.getMatchContext().set(key, field.getFieldTier());
            return true;
        }, () -> Arrays.stream(entries).map(entry -> BlockInfo.fromBlock(entry.get())).toArray(BlockInfo[]::new))
                .addTooltips(Component.translatable("gtna.eoh.structure.uniform_" + family.id));
    }

    private static TraceabilityPredicate boundaryPort(PartAbility ability, int count) {
        var predicate = abilities(ability);
        // Only finite-buffer ME inputs and ME outputs; preview and auto-build use the same filter.
        for (var simple : predicate.common) {
            var match = simple.predicate;
            simple.predicate = state -> match.test(state) &&
                    state.getTileEntity() instanceof IMachineBlockEntity holder && allowedPort(holder.getMetaMachine());
            var candidates = simple.candidates;
            simple.candidates = Suppliers.memoize(() -> Arrays.stream(candidates.get()).filter(info -> {
                var blockState = info.getBlockState();
                return blockState.getBlock() instanceof EntityBlock block &&
                        block.newBlockEntity(BlockPos.ZERO, blockState) instanceof IMachineBlockEntity holder &&
                        allowedPort(holder.getMetaMachine());
            }).toArray(BlockInfo[]::new));
        }
        return predicate.setExactLimit(count).setPreviewCount(count)
                .addTooltips(Component.translatable("gtna.eoh.structure.ports"));
    }

    private static boolean allowedPort(MetaMachine machine) {
        if (!GTCEu.Mods.isAE2Loaded() || machine instanceof IMEStockingPart) return false;
        return machine instanceof com.gregtechceu.gtceu.integration.ae2.machine.MEInputBusPartMachine ||
                machine instanceof com.gregtechceu.gtceu.integration.ae2.machine.MEInputHatchPartMachine ||
                machine instanceof com.gregtechceu.gtceu.integration.ae2.machine.MEOutputBusPartMachine ||
                machine instanceof com.gregtechceu.gtceu.integration.ae2.machine.MEOutputHatchPartMachine;
    }
}
