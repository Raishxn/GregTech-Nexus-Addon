package com.raishxn.gtna.integration.kubejs;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.registry.registrate.BuilderBase;
import com.gregtechceu.gtceu.api.registry.registrate.MultiblockMachineBuilder;
import com.gregtechceu.gtceu.integration.kjs.GTRegistryInfo;
import com.gregtechceu.gtceu.integration.kjs.builders.machine.KJSWrappingMultiblockBuilder;

import com.raishxn.gtna.common.machine.multiblock.electric.WorkableElectricMultipleRecipesMachine;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingsEvent;

public class GTNAKubeJSPlugin extends KubeJSPlugin {

    @Override
    @SuppressWarnings("unchecked")
    public void init() {
        super.init();

        // Register standard GTNA machines
        GTRegistryInfo.MACHINE.addType("gtna:multiple_recipes",
                (Class<? extends BuilderBase<? extends MachineDefinition>>) (Class<?>) MultiblockMachineBuilder.class,
                (id) -> KJSWrappingMultiblockBuilder.createKJSMulti(id, WorkableElectricMultipleRecipesMachine::new),
                false);
    }

    @Override
    public void registerBindings(BindingsEvent event) {
        super.registerBindings(event);

        // Expose GTNA constants
        event.add("GTNAPartAbility", GTNAPartAbilityWrapper.class);
    }
}
