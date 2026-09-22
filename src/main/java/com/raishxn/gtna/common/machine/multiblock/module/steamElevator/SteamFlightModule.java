package com.raishxn.gtna.common.machine.multiblock.module.steamElevator;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * GTNL {@code SteamFlightModule} port (LGPLv3, original by ScienceNotLeisure).
 *
 * <p>
 * GTNL grants a Blood Magic "flight" potion for 1000 ticks to players within range. Blood Magic does
 * not exist in 1.20.1, so the GTNA port grants vanilla creative flight ({@code mayfly}) to players
 * inside the range and revokes it when the elevator stops (the tracked-player set is the analogue
 * of the potion expiring).
 */
public class SteamFlightModule extends SteamElevatorModuleMachine {

    /** GTNL {@code getMachineEffectRange()} at tier 1 (oc-expanded in GTNL). */
    public static final int RANGE = 64;

    private final Set<UUID> flying = new HashSet<>();

    public SteamFlightModule(IMachineBlockEntity holder, int tier) {
        super(holder, tier);
    }

    @Override
    public int getEffectRange() {
        return RANGE;
    }

    @Override
    public long getSteamUpkeep() {
        // GTNL: mTier * V[5].
        return (long) getModuleTier() * GTValues.V[5];
    }

    @Override
    public void onElevatorTick(SteamElevator elevator) {
        if (!consumeSteam(getSteamUpkeep())) return;
        if (!(getLevel() instanceof ServerLevel level)) return;

        Vec3 center = Vec3.atCenterOf(getPos());
        double range = getEffectRange();
        AABB box = new AABB(getPos()).inflate(range);
        for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, box)) {
            if (player.distanceToSqr(center) > range * range) continue;
            if (!player.getAbilities().mayfly) {
                player.getAbilities().mayfly = true;
                player.onUpdateAbilities();
            }
            flying.add(player.getUUID());
        }
    }

    @Override
    public void onElevatorStop() {
        if (!(getLevel() instanceof ServerLevel level)) {
            flying.clear();
            return;
        }
        for (UUID id : flying) {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(id);
            if (player != null && !player.isCreative() && !player.isSpectator()) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                player.onUpdateAbilities();
            }
        }
        flying.clear();
    }
}
