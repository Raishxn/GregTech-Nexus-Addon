package com.raishxn.gtna.common.machine.multiblock.module.steamElevator;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * GTNA-native replacement for GTNL's Steam Elevator teleport.
 *
 * <p>
 * GTNL opened the Galacticraft celestial-selection screen and let the player travel between
 * planets. GTNA has no Galacticraft; the equivalent is Ad Astra. To avoid a hard dependency this
 * helper works purely off the dimension registry: it discovers dimensions whose namespace is
 * {@code ad_astra} at runtime and never imports an Ad Astra class.
 *
 * <p>
 * Two behaviours, mirroring GTNL's "elevator" and "between planets" buttons:
 * <ul>
 * <li>plain activation moves the player to the top of the elevator structure (vertical travel);</li>
 * <li>sneaking activation cycles to the next Ad Astra dimension, if any is registered (planet
 * travel). When no Ad Astra dimension exists the player is told instead of being moved.</li>
 * </ul>
 */
public final class SteamElevatorTeleport {

    /** Height (in blocks) the elevator lifts a player; matches the 43-tall structure plus clearance. */
    private static final int LIFT_HEIGHT = 46;

    private SteamElevatorTeleport() {}

    public static void execute(ServerPlayer player, SteamElevator elevator) {
        if (player.isShiftKeyDown()) {
            travelToNextPlanet(player);
        } else {
            liftUp(player, elevator);
        }
    }

    private static void liftUp(ServerPlayer player, SteamElevator elevator) {
        BlockPos pos = elevator.getPos();
        ServerLevel level = (ServerLevel) elevator.getLevel();
        int targetX = pos.getX();
        int targetZ = pos.getZ();
        int targetY = Math.min(level.getMaxBuildHeight() - 2, pos.getY() + LIFT_HEIGHT);
        player.teleportTo(level, targetX + 0.5, targetY, targetZ + 0.5, player.getYRot(), player.getXRot());
        player.displayClientMessage(Component.translatable("gtna.machine.steam_elevator.teleport.lift"), true);
    }

    private static void travelToNextPlanet(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) return;

        List<ResourceKey<Level>> destinations = new ArrayList<>();
        for (ServerLevel level : server.getAllLevels()) {
            if ("ad_astra".equals(level.dimension().location().getNamespace())) {
                destinations.add(level.dimension());
            }
        }
        if (destinations.isEmpty()) {
            player.displayClientMessage(Component.translatable("gtna.machine.steam_elevator.teleport.no_planets"),
                    true);
            return;
        }
        destinations.sort(Comparator.comparing(key -> key.location().toString()));

        ResourceKey<Level> current = player.level().dimension();
        int index = destinations.indexOf(current);
        ResourceKey<Level> next = destinations.get((index + 1) % destinations.size());

        ServerLevel target = server.getLevel(next);
        if (target == null) {
            player.displayClientMessage(Component.translatable("gtna.machine.steam_elevator.teleport.no_planets"),
                    true);
            return;
        }
        BlockPos spawn = target.getSharedSpawnPos();
        player.teleportTo(target, spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5, player.getYRot(),
                player.getXRot());
        player.displayClientMessage(
                Component.translatable("gtna.machine.steam_elevator.teleport.planet", next.location().toString()),
                true);
    }
}
