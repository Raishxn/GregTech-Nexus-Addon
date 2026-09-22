package com.raishxn.gtna.common.machine.multiblock.module.steamElevator;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import earth.terrarium.adastra.common.menus.base.PlanetsMenuProvider;
import earth.terrarium.botarium.common.menu.MenuHooks;

/**
 * GTNA-native Steam Elevator "set out" action.
 *
 * <p>
 * Faithful to GTLCore's {@code SpaceElevatorMachine} (the "space elevator" that GTLAdditions builds
 * on): instead of teleporting the player, the machine opens Ad Astra's planet-selection menu - the
 * very same screen a rocket launch opens - so the player picks a planet and travels there through
 * Ad Astra's own logic. The player is tagged with {@value #ELEVATOR_TAG} so the departure can be
 * tracked (GTLCore uses the exact same tag).
 */
public final class SteamElevatorTeleport {

    /** Player tag used by GTLCore's space elevator to mark an elevator departure. */
    public static final String ELEVATOR_TAG = "spaceelevatorst";

    private SteamElevatorTeleport() {}

    /** Opens the Ad Astra planet selection for the player, if the elevator is running. */
    public static void setOut(ServerPlayer player, SteamElevator elevator) {
        if (!elevator.isElevatorRunning()) {
            player.displayClientMessage(Component.translatable("gtna.machine.steam_elevator.set_out.not_running"),
                    true);
            return;
        }
        player.addTag(ELEVATOR_TAG);
        MenuHooks.openMenu(player, new PlanetsMenuProvider());
    }
}
