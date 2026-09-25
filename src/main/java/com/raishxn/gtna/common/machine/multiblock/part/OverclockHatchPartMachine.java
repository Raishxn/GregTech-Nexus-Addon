package com.raishxn.gtna.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;

import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.MethodsReturnNonnullByDefault;

import com.raishxn.gtna.api.machine.feature.OverclockHatchMath;
import com.raishxn.gtna.config.GTNABalance;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Improves the machine's overclock so the duration falls to {@code 1/divisor} per overclock step
 * instead of the standard {@code 0.5}. The divisor is player-adjustable (GTOCore
 * {@code OverclockPartMachine} parity, UI label "Divisor of duration"): it defaults to the tier's
 * best value ({@code tier - 6}) and can be dialed back to {@link OverclockHatchMath#MIN_DIVISOR}
 * (the standard overclock, i.e. no gain).
 *
 * <p>
 * The value is an integer divisor, never a rounded percentage, so the factor used by the recipe logic
 * and the percentage shown by the tooltip always agree exactly.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class OverclockHatchPartMachine extends ConfigurableAmountPartMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            OverclockHatchPartMachine.class, ConfigurableAmountPartMachine.MANAGED_FIELD_HOLDER);

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    public OverclockHatchPartMachine(IMachineBlockEntity holder, int tier, Object... args) {
        super(holder, tier, OverclockHatchMath.MIN_DIVISOR, maxDivisor(tier), maxDivisor(tier));
    }

    private static int maxDivisor(int tier) {
        return OverclockHatchMath.clampDivisor(GTNABalance.getOverclockDivisor(tier), tier);
    }

    /** The active duration divisor ({@code 2} = standard overclock, {@code tier - 6} = best). */
    public int getOverclockDivisor() {
        return getCurrentAmount();
    }

    /** Per-overclock-step duration factor, exactly {@code 1/divisor}. */
    public double getOverclockMultiplier() {
        return OverclockHatchMath.stepFactor(getOverclockDivisor());
    }

    @Override
    protected String getAmountLabel() {
        return "gtna.machine.overclock_hatch.divisor";
    }
}
