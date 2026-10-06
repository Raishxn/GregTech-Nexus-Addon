package com.raishxn.gtna.common.world.personalspace;

import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStartedEvent;

import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.network.packet.SPersonalSpaceWorldList;

/** Server-side lifecycle of personal dimensions: sync, own time and disabled weather. */
public final class PersonalSpaceEvents {

    private PersonalSpaceEvents() {}

    public static void register() {
        MinecraftForge.EVENT_BUS.addListener(PersonalSpaceCommands::registerEvent);
        MinecraftForge.EVENT_BUS.addListener(PersonalSpaceEvents::playerLoggedIn);
        MinecraftForge.EVENT_BUS.addListener(PersonalSpaceEvents::levelTick);
        MinecraftForge.EVENT_BUS.addListener(PersonalSpaceEvents::levelLoad);
        MinecraftForge.EVENT_BUS.addListener(PersonalSpaceEvents::levelUnload);
        MinecraftForge.EVENT_BUS.addListener(PersonalSpaceEvents::serverStarted);
    }

    /**
     * Makes sure every known personal dimension exists before anyone connects, so a player who logged out inside one
     * is not moved to the overworld (the original registers them all at startup too). Levels already restored from
     * level.dat are only set up.
     */
    private static void serverStarted(ServerStartedEvent event) {
        var server = event.getServer();
        for (int id : PersonalSpaceDirectory.get(server).dimensions().keySet()) {
            try {
                PersonalSpaceWorlds.load(server, id);
            } catch (RuntimeException exception) {
                GTNACORE.LOGGER.error("Could not load PersonalSpace dimension {}", id, exception);
            }
        }
    }

    private static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        // GameTest mock players have no network channel.
        if (event.getEntity() instanceof ServerPlayer player && player.connection.connection.channel() != null) {
            SPersonalSpaceWorldList.sync(SPersonalSpaceWorldList.create(player.getServer()), player);
        }
    }

    private static void levelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level)) return;
        if (!(level.getLevelData() instanceof PersonalSpaceLevelData data)) return;
        if (level.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) data.tickDayTime();
        if (!data.settings().isWeatherEnabled() && (level.getRainLevel(1.0F) > 0 || level.getThunderLevel(1.0F) > 0)) {
            level.setRainLevel(0.0F);
            level.setThunderLevel(0.0F);
            var players = level.getServer().getPlayerList();
            players.broadcastAll(new ClientboundGameEventPacket(ClientboundGameEventPacket.STOP_RAINING, 0.0F),
                    level.dimension());
            players.broadcastAll(new ClientboundGameEventPacket(ClientboundGameEventPacket.RAIN_LEVEL_CHANGE, 0.0F),
                    level.dimension());
            players.broadcastAll(new ClientboundGameEventPacket(ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE,
                    0.0F), level.dimension());
        }
    }

    private static void levelLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel level) PersonalSpaceWorlds.setUp(level);
    }

    private static void levelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level && level.getLevelData() instanceof PersonalSpaceLevelData) {
            PersonalSpaceDirectory.get(level.getServer()).setDirty();
        }
    }
}
