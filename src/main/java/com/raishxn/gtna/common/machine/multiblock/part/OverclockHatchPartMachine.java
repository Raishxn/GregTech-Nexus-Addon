package com.raishxn.gtna.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;

import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.MethodsReturnNonnullByDefault;

import com.raishxn.gtna.config.GTNABalance;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Improves the machine's overclock so the duration falls to {@code amount}% per overclock step
 * instead of the standard 50%. The amount is player-adjustable (GTOCore
 * {@code WorkableAmountConfigurationPartMachine} parity): it defaults to the tier's best value and
 * can be dialed up to 100% (no effect). Values match GTOCore's {@code 100 / (tier - 6) %}.
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
        super(holder, tier, tierPercent(tier), 100);
    }

    private static int tierPercent(int tier) {
        return (int) Math.round(GTNABalance.getOverclockDurationMultiplier(tier) * 100.0);
    }

    public double getOverclockMultiplier() {
        return getCurrentAmount() / 100.0;
    }
}
