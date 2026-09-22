package com.raishxn.gtna.common.machine.multiblock.module.steamElevator;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.Monster;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.raishxn.gtna.GTNACORE;

/**
 * Cancels hostile mob spawns inside a running {@link SteamMonsterRepellentModule}'s field, which is
 * how GTNL's spawn-event repellent behaves ("Only prevents spawns while the machine is running").
 */
@Mod.EventBusSubscriber(modid = GTNACORE.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class SteamRepellentHandler {

    private SteamRepellentHandler() {}

    @SubscribeEvent
    public static void onFinalizeSpawn(MobSpawnEvent.FinalizeSpawn event) {
        if (!(event.getEntity() instanceof Monster)) return;
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (SteamMonsterRepellentModule.blocksSpawn(level, event.getX(), event.getY(), event.getZ())) {
            event.setCanceled(true);
        }
    }
}
