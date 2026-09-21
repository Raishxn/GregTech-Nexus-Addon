package com.raishxn.gtna.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;

import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.MethodsReturnNonnullByDefault;

import com.raishxn.gtna.api.machine.feature.AccelerateHatchMath;
import com.raishxn.gtna.config.GTNABalance;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Reduces the duration of recipes. The effective percentage is player-adjustable (GTOCore
 * {@code WorkableAmountConfigurationPartMachine} parity): it defaults to the tier's best value and
 * can be dialed up to 100% (no effect). The tier penalty follows the recipe tier, never the machine.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AccelerateHatchPartMachine extends ConfigurableAmountPartMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            AccelerateHatchPartMachine.class, ConfigurableAmountPartMachine.MANAGED_FIELD_HOLDER);

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    public AccelerateHatchPartMachine(IMachineBlockEntity holder, int tier, Object... args) {
        super(holder, tier,
                GTNABalance.getAccelerateBaseMinPercent(tier),
                GTNABalance.getAccelerateMaximumFinalPercent());
    }

    /** Best-case percentage of this tier (no recipe-tier penalty), used for UI display. */
    public int getMinDurationPercentage() {
        return getMinAmount();
    }

    /**
     * @param recipeTier the recipe's <b>pre-overclock</b> voltage tier (GTOCore semantics: the penalty
     *                   is tied to the recipe, never the machine - a high-tier machine running a
     *                   low-tier recipe is not punished)
     */
    public int calcDurationPercentage(int recipeTier) {
        return AccelerateHatchMath.compute(getCurrentAmount(), getTier(), recipeTier,
                GTNABalance.getAcceleratePenaltyPerTierBelowRecipe(), GTNABalance.getAccelerateMinimumFinalPercent(),
                GTNABalance.getAccelerateMaximumFinalPercent());
    }
}
