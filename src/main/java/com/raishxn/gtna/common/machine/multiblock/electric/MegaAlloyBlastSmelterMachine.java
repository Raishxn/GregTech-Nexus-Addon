package com.raishxn.gtna.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.CoilWorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;

/**
 * GTOCore Mega Alloy Blast Smelter: the coiled, parallel Alloy Blast Smelter with GTO's 0.8× EU
 * and 0.6× duration bonus.
 * <p>
 * GTO applies its bonus through the native (closed-source) gtolib
 * {@code GTORecipeModifiers.UPGRADE_GCYM_OVERCLOCKING}, which cannot be read. The GTNA port keeps
 * the documented 0.8×/0.6× multipliers, the Parallel Hatch and the standard Alloy Blast coil
 * overclock/temperature gate, which is how GTCEu's own Alloy Blast Smelter behaves.
 */
public final class MegaAlloyBlastSmelterMachine extends CoilWorkableElectricMultiblockMachine {

    public MegaAlloyBlastSmelterMachine(IMachineBlockEntity holder, Object... args) {
        super(holder);
    }

    public static ModifierFunction recipeModifier(MetaMachine machine, GTRecipe recipe) {
        if (!(machine instanceof MegaAlloyBlastSmelterMachine)) {
            return ModifierFunction.NULL;
        }
        ModifierFunction parallel = GTRecipeModifiers.hatchParallel(machine, recipe);
        ModifierFunction overclock = GTRecipeModifiers.ebfOverclock(machine, recipe);
        ModifierFunction bonus = ModifierFunction.builder()
                .eutMultiplier(0.8)
                .durationMultiplier(0.6)
                .build();
        return parallel.compose(bonus).compose(overclock);
    }
}
