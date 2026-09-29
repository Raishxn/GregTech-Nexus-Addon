package com.raishxn.gtna.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;

import net.minecraft.MethodsReturnNonnullByDefault;

import com.raishxn.gtna.common.data.GTNARecipeType;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * GTOCore Vacuum Drying Furnace: dries flotation {@code *Front} foams back into GTCEu dusts (main
 * mode) or runs the GTO Dehydrator family (second mode), heated by GTCEu coils.
 *
 * <p>
 * GTOCore drives this machine with {@code CoilCustomParallelMultiblockMachine} and a per-recipe-type
 * recipe modifier:
 *
 * <ul>
 * <li>{@code VACUUM_DRYING}: serial (parallel 1) with {@code UPGRADE_EBF_OVERCLOCK}, reproduced by
 * {@link GTNAHeatingCoilOverclock} (coil-temperature gate, 0.95 EU/t discount per 900K and perfect
 * overclock steps);</li>
 * <li>{@code DEHYDRATOR}: parallel {@code 2^floor(coilTemp / 900)} with the standard
 * {@code UPGRADE_PARALLELIZABLE_OVERCLOCK}.</li>
 * </ul>
 *
 * <p>
 * GTO reads the active recipe type with {@code m.getRecipeType()}, exactly like this port. GTNA's
 * multi-recipe search can start a recipe of the other type while the controller shows one mode, so
 * the parallel formula follows the selected mode rather than the candidate recipe.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class VacuumDryingFurnaceMachine extends CoilWorkableElectricMultipleRecipesMachine {

    public VacuumDryingFurnaceMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    public int getMaxParallel() {
        int hatchParallel = getParallelLimit();
        if (getRecipeType() != GTNARecipeType.DEHYDRATOR_RECIPES) {
            // GTOCore's parallel function returns 1 for every type except the Dehydrator family.
            return hatchParallel;
        }
        int exponent = Math.max(0, getCoilType().getCoilTemperature() / 900);
        int coilParallel = exponent >= 31 ? Integer.MAX_VALUE : 1 << exponent;
        return Math.max(hatchParallel, coilParallel);
    }

    @Override
    public OverclockingLogic getOverclockingLogic() {
        if (getRecipeType() == GTNARecipeType.VACUUM_DRYING_RECIPES && !hasOverclockHatch()) {
            return new GTNAHeatingCoilOverclock(this::getHeatingCoilTemperature);
        }
        return super.getOverclockingLogic();
    }
}
