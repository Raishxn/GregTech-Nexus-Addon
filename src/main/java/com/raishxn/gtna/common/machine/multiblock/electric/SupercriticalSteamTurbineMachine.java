package com.raishxn.gtna.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;

import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * GTOCore Supercritical Steam Turbine: the non-mega path of GTO's {@code TurbineMachine} for the
 * {@code supercritical_steam_turbine} (IV, {@code special = false}, base output
 * {@code V[IV] * 2 = 16384 EU/t}).
 *
 * <p>
 * The shared non-mega turbine logic lives in {@link GTNALargeTurbineMachine}; this class only pins
 * the construction tier and the language id, exactly like GTO creates both non-mega turbines from
 * the same {@code TurbineMachine} constructor. GTO's
 * {@code MachineRegisterUtils.registerLargeTurbine} SUPERCRITICAL branch states the module bonus
 * (2x output, +20% efficiency and a 2x rotor damage multiplier) and its
 * {@code SUPERCRITICAL_STEAM_TURBINE_FUELS} recipe family is detailed in the registry.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class SupercriticalSteamTurbineMachine extends GTNALargeTurbineMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            SupercriticalSteamTurbineMachine.class, GTNALargeTurbineMachine.MANAGED_FIELD_HOLDER);

    public SupercriticalSteamTurbineMachine(IMachineBlockEntity holder, int tier, boolean special) {
        super(holder, tier, special, "supercritical_steam_turbine");
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }
}
