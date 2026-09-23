package com.raishxn.gtna.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.CoilWorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import org.jetbrains.annotations.Nullable;

/**
 * GTOCore {@code liquefaction_furnace} port (LGPLv3, attribution via {@code GTNASources}): a plain
 * coil multiblock that melts an item into a fluid; the recipe's {@code ebf_temp} sets the required
 * coil temperature.
 *
 * <p>
 * It is a <b>normal</b> machine: the base structure accepts no Parallel / Accelerate / Thread hatch.
 * GTOCore attaches a <b>sub-pattern</b> (module) — a stainless-steel tower beside the controller that
 * unlocks one Parallel Hatch and one Accelerate Hatch. GTNA registers that module in
 * {@code GTNAModules} through the native sub-pattern mechanic.
 */
public class LiquefactionFurnaceMachine extends CoilWorkableElectricMultiblockMachine {

    public LiquefactionFurnaceMachine(IMachineBlockEntity holder, Object... args) {
        super(holder);
    }

    @Override
    public boolean beforeWorking(@Nullable GTRecipe recipe) {
        if (recipe != null) {
            int requiredTemperature = recipe.data.getInt("ebf_temp");
            if (requiredTemperature > 0 && getCoilType().getCoilTemperature() < requiredTemperature) {
                return false;
            }
        }
        return super.beforeWorking(recipe);
    }
}
