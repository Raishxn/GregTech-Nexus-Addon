package com.raishxn.gtna.common.machine.multiblock.energy;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;

import org.jetbrains.annotations.NotNull;

/**
 * Generator multiblock based on the GTO/GTL Artificial Star.
 */
public class ArtificialStarMachine extends WorkableElectricMultiblockMachine {

    public ArtificialStarMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    public static ModifierFunction recipeModifier(MetaMachine machine, @NotNull GTRecipe recipe) {
        if (!(machine instanceof ArtificialStarMachine artificialStarMachine)) {
            return ModifierFunction.NULL;
        }
        var outputEUt = recipe.getOutputEUt();
        if (outputEUt.isEmpty()) {
            return ModifierFunction.NULL;
        }

        double resultDuration = recipe.duration;
        double resultVoltage = outputEUt.voltage();
        long maxVoltage = artificialStarMachine.getOverclockVoltage();

        for (int numberOfOCs = 16; numberOfOCs > 0; numberOfOCs--) {
            double potentialVoltage = resultVoltage * 4;
            if (potentialVoltage > maxVoltage) break;

            double potentialDuration = resultDuration / 4;
            if (potentialDuration < 1) break;

            resultDuration = potentialDuration;
            resultVoltage = potentialVoltage;
        }

        return ModifierFunction.builder()
                .durationMultiplier(resultDuration / recipe.duration)
                .eutMultiplier(resultVoltage / outputEUt.voltage())
                .build();
    }
}
