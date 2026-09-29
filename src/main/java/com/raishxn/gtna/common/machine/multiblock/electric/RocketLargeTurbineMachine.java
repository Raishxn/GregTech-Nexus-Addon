package com.raishxn.gtna.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;

import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * GTOCore Rocket Large Turbine: the non-mega path of GTO's {@code TurbineMachine} for the
 * {@code rocket_large_turbine} (EV, {@code special = true}, base output {@code V[EV] * 2.5}).
 *
 * <p>
 * The shared non-mega turbine logic lives in {@link GTNALargeTurbineMachine}; this class only pins
 * the construction tier and the language id, exactly like GTO creates both non-mega turbines from
 * the same {@code TurbineMachine} constructor. GTO's
 * {@code MachineRegisterUtils.registerLargeTurbine} states the rocket turbine's module bonus
 * (2x output, +20% efficiency and a 2x rotor damage multiplier) and its
 * {@code ROCKET_ENGINE_FUELS} recipe family are detailed in the registry.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class RocketLargeTurbineMachine extends GTNALargeTurbineMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            RocketLargeTurbineMachine.class, GTNALargeTurbineMachine.MANAGED_FIELD_HOLDER);

    public RocketLargeTurbineMachine(IMachineBlockEntity holder, int tier, boolean special) {
        super(holder, tier, special, "rocket_large_turbine");
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }
}
