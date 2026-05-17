package com.raishxn.gtna.utils;

import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.ModList;

import java.util.Optional;
import java.util.UUID;

/**
 * Centralised network-identity resolution for the Nexus wireless networks.
 *
 * Rule:
 *   - If FTB Teams is loaded AND the player belongs to a shared party team,
 *     return that party team's UUID so all team members share one network.
 *   - Otherwise return the player's own UUID.
 *
 * FTB Teams is a soft dependency: the {@link #resolveNetworkId} methods are
 * guarded by a {@link ModList} presence check, so the mod loads and runs
 * correctly whether FTB Teams is installed or not.
 *
 * The compile-time dependency is satisfied by placing
 * {@code ftb-teams-forge-*.jar} and {@code ftb-library-forge-*.jar}
 * in the project's {@code libs/} directory and referencing them via
 * {@code compileOnly fileTree(dir: 'libs', include: '*.jar')} in build.gradle.
 */
public final class GTNANetworkIdentityUtil {

    private GTNANetworkIdentityUtil() {}

    private static final String FTBTEAMS_MOD_ID = "ftbteams";

    /**
     * Resolve the Nexus network-owner UUID for a player entity.
     *
     * @param player the placing/activating player (server-side)
     * @return party team UUID if the player is in one, otherwise player UUID
     */
    public static UUID resolveNetworkId(Player player) {
        if (player == null) return null;
        return resolveNetworkId(player.getUUID());
    }

    /**
     * Resolve the Nexus network-owner UUID from a raw player UUID.
     *
     * Safe to call from any server-side code. Returns {@code playerUUID}
     * unchanged when FTB Teams is absent or the API isn't ready yet.
     *
     * @param playerUUID the player's UUID
     * @return party team UUID if applicable, otherwise {@code playerUUID}
     */
    public static UUID resolveNetworkId(UUID playerUUID) {
        if (playerUUID == null) return null;

        // Fast exit when FTB Teams isn't installed
        if (!ModList.get().isLoaded(FTBTEAMS_MOD_ID)) {
            return playerUUID;
        }

        try {
            FTBTeamsAPI.API api = FTBTeamsAPI.api();
            if (api == null || !api.isManagerLoaded()) return playerUUID;

            Optional<Team> team = api.getManager().getTeamForPlayerID(playerUUID);

            // Use the team UUID only for party teams (shared with multiple players).
            // Personal "player teams" are private to one player and must not be
            // used as a shared network identity.
            if (team.isPresent() && team.get().isPartyTeam()) {
                return team.get().getTeamId();
            }
        } catch (Throwable t) {
            // FTB Teams present but API not yet initialised (very early load order).
            // Fall through to player UUID.
        }

        return playerUUID;
    }
}
