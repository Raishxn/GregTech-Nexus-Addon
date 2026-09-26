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
        add(machineId, factory, "gtna.machine.auxiliary_module.kubejs.generic");
    }

    /** The optional key is resolved by the client's language pack when the controller tooltip opens. */
    public void add(String machineId, Function<MultiblockMachineDefinition, BlockPattern> factory,
                    String descriptionKey) {
        add(machineId, factory, descriptionKey, 1.0, false);
    }

    /** Speed bonus applies only while this module is formed. Use 2.0 for twice the recipe speed. */
    public void add(String machineId, Function<MultiblockMachineDefinition, BlockPattern> factory,
                    String descriptionKey, double speedBonus, boolean perfectOverclock) {
        ResourceLocation id = ResourceLocation.tryParse(machineId);
        if (id == null) {
            throw new IllegalArgumentException("Invalid machine id: " + machineId);
        }
        GTNASubPatterns.registerKubeJS(id, factory, descriptionKey, speedBonus, perfectOverclock);
    }
}
