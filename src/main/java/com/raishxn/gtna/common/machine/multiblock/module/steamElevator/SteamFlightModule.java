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
 * GTNL grants a Blood Magic "flight" potion for 1000 ticks to players inside
 * {@code getMachineEffectRange()} (= 64 blocks at overclock count 1). Blood Magic does not exist in
 * 1.20.1, so the GTNA port grants vanilla creative flight ({@code mayfly}) to players inside the
 * range and — like the potion expiring — <b>revokes it as soon as they leave the range</b> (or the
 * module stops). Only players this module actually granted flight to are tracked, so a creative
 * player or another flight source is never touched.
 */
public class SteamFlightModule extends SteamElevatorModuleMachine {

    /** GTNL {@code getMachineEffectRange()} = {@code 64 * max(overclockCount, 1)}. */
    public static final int RANGE = 64;

    /** Players whose {@code mayfly} this module granted and is therefore responsible for. */
    private final Set<UUID> grantedFlight = new HashSet<>();

    public SteamFlightModule(IMachineBlockEntity holder, int tier) {
        super(holder, tier);
    }

    @Override
    public int getEffectRange() {
        return RANGE;
    }

    @Override
    public long getSteamUpkeep() {
        // GTNL: mTier * V[5] * max(overclockCount, 1).
        return (long) getModuleTier() * GTValues.V[5];
    }

    @Override
    public void onElevatorTick(SteamElevator elevator) {
        if (!consumeSteam(getSteamUpkeep())) return;
        if (!(getLevel() instanceof ServerLevel level)) return;

        Vec3 center = Vec3.atCenterOf(getPos());
        double range = getEffectRange();
        AABB box = new AABB(getPos()).inflate(range);
        Set<UUID> inRange = new HashSet<>();
        for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, box)) {
            if (player.distanceToSqr(center) > range * range) continue;
            inRange.add(player.getUUID());
            if (!player.getAbilities().mayfly) {
                player.getAbilities().mayfly = true;
                player.onUpdateAbilities();
                // Track only the grant this module made, so leaving the range revokes exactly it.
                grantedFlight.add(player.getUUID());
            }
        }
        // GTNL's potion expires: drop the flight of anyone who left the range.
        revoke(grantedFlight, inRange);
    }

    /** Removes from {@code tracked} (and revokes flight for) every player not in {@code keep}. */
    private void revoke(Set<UUID> tracked, Set<UUID> keep) {
        if (!(getLevel() instanceof ServerLevel level)) return;
        var iterator = tracked.iterator();
        while (iterator.hasNext()) {
            UUID id = iterator.next();
            if (keep.contains(id)) continue;
            iterator.remove();
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(id);
            if (player != null && !player.isCreative() && !player.isSpectator()) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                player.onUpdateAbilities();
            }
        }
    }

    @Override
    public void onElevatorStop() {
        revoke(grantedFlight, Set.of());
        grantedFlight.clear();
    }
}
