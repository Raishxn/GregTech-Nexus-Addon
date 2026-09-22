package com.raishxn.gtna.common.machine.multiblock.module.steamElevator;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.AABB;

/**
 * GTNL {@code SteamEntityCrusherModule} port (LGPLv3, original by ScienceNotLeisure).
 *
 * <p>
 * GTNL runs the "Extreme Extreme Entity Crusher" recipe map, which turns mob drops and similar
 * organic inputs into higher outputs. GTNA has no equivalent mob-drop item chain, so this port keeps
 * the "entity crusher" identity at the source: hostile monsters inside the range are crushed,
 * dropping their normal loot (damage is credited as a generic player-like kill so loot tables run).
 * The GTNL parallel/drop-multiplier overclocks are reduced to a flat range/energy upkeep.
 */
public class SteamEntityCrusherModule extends SteamElevatorModuleMachine {

    public static final int RANGE = 8;

    private int counter;

    public SteamEntityCrusherModule(IMachineBlockEntity holder, int tier) {
        super(holder, tier);
    }

    @Override
    public int getEffectRange() {
        return RANGE;
    }

    @Override
    public long getSteamUpkeep() {
        return getModuleTier() * GTValues.V[3];
    }

    @Override
    public void onElevatorTick(SteamElevator elevator) {
        if (!consumeSteam(getSteamUpkeep())) return;
        if (!(getLevel() instanceof ServerLevel level)) return;
        if (++counter % 20 != 0) return;

        double range = getEffectRange();
        AABB box = new AABB(getPos()).inflate(range);
        for (Monster monster : level.getEntitiesOfClass(Monster.class, box)) {
            if (monster.distanceToSqr(getPos().getX() + 0.5, getPos().getY() + 0.5, getPos().getZ() + 0.5) >
                    range * range) {
                continue;
            }
            monster.hurt(level.damageSources().generic(), 10_000.0F);
        }
    }
}
