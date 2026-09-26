package com.raishxn.gtna.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;

import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * GTOCore Industrial Flotation Cell: froth-floats a MILLED ore with an ethylxanthate reagent and
 * turpentine into the matching {@code *Front} foam, with hatch parallel and GTO's
 * {@code parallelizablePerfectOverclock()}.
 *
 * <p>
 * The GTNA base supplies the standard thread/accelerate machinery; GTO's perfect overclock maps to
 * {@link OverclockingLogic#PERFECT_OVERCLOCK}. That is also what the multiple-recipes logic applies
 * ({@code ELECTRIC_OVERCLOCK.apply(getOverclockingLogic())}), so the two are the same numbers: every
 * voltage tier above the recipe divides the duration by 4. A GTNA Overclock Hatch (when a pattern
 * ever accepts one) still wins over the default.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class IndustrialFlotationCellMachine extends WorkableElectricMultipleRecipesMachine {

    public IndustrialFlotationCellMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    public OverclockingLogic getOverclockingLogic() {
        return hasOverclockHatch() ? super.getOverclockingLogic() : OverclockingLogic.PERFECT_OVERCLOCK;
    }
}
