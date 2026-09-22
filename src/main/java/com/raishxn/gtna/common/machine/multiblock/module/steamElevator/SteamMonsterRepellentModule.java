package com.raishxn.gtna.common.machine.multiblock.module.steamElevator;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.AABB;

/**
 * GTNL {@code SteamMonsterRepellentModule} port (LGPLv3, original by ScienceNotLeisure).
 *
 * <p>
 * GTNL registers a spawn-event repellent with a radius of {@code 1 << (5 + tier)} so hostile mobs
 * never spawn in the area. GTNA reaches the same end state without a global spawn hook: while the
 * elevator runs, hostile monsters that appear inside the radius are quietly removed (no loot). The
 * radius and energy cost follow GTNL exactly.
 */
public class SteamMonsterRepellentModule extends SteamElevatorModuleMachine {

    private int counter;

    public SteamMonsterRepellentModule(IMachineBlockEntity holder, int tier) {
        super(holder, tier);
    }

    @Override
    public int getEffectRange() {
        // GTNL SteamMonsterRepellentModule#mRange.
        return 1 << (5 + getModuleTier());
    }

    @Override
    public long getSteamUpkeep() {
        return getModuleTier() * GTValues.V[3];
    }

    @Override
    public void onElevatorTick(SteamElevator elevator) {
        if (!consumeSteam(getSteamUpkeep())) return;
        if (!(getLevel() instanceof ServerLevel level)) return;
        // Spawn checks are expensive; run once per two seconds.
        if (++counter % 40 != 0) return;

        double range = getEffectRange();
        AABB box = new AABB(getPos()).inflate(range);
        for (Monster monster : level.getEntitiesOfClass(Monster.class, box)) {
            monster.remove(Entity.RemovalReason.DISCARDED);
        }
    }
}
