package com.raishxn.gtna.integration.kubejs;

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;

import net.minecraft.resources.ResourceLocation;

import com.raishxn.gtna.api.machine.multiblock.GTNASubPatterns;
import dev.latvian.mods.kubejs.event.EventJS;

import java.util.function.Function;

/**
 * KubeJS event: register extension structures ("modules") for multiblocks.
 *
 * <p>
 * The factory receives the {@link MultiblockMachineDefinition} and returns a {@link BlockPattern}
 * built with GTCEu's {@code FactoryBlockPattern}/{@code Predicates} (both already exposed to KubeJS).
 * Every matching extension's parts are merged into the controller, so the module can unlock new
 * abilities (Parallel / Accelerate hatches, extra IO, ...).
 */
public class SubPatternEventJS extends EventJS {

    public void add(String machineId, Function<MultiblockMachineDefinition, BlockPattern> factory) {
        ResourceLocation id = ResourceLocation.tryParse(machineId);
        if (id == null) {
            throw new IllegalArgumentException("Invalid machine id: " + machineId);
        }
        GTNASubPatterns.register(id, factory);
    }
}
