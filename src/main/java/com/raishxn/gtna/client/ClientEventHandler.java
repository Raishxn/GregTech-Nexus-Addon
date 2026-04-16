package com.raishxn.gtna.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.config.ConfigHolder;

@Mod.EventBusSubscriber(modid = GTNACORE.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEventHandler {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.side.isClient()) {
            Player player = event.player;
            if (player == Minecraft.getInstance().player) {
                if (ConfigHolder.INSTANCE.client.disableFlyInertia && player.getAbilities().flying) {
                    if (player.zza == 0 && player.xxa == 0) {
                        player.setDeltaMovement(player.getDeltaMovement().multiply(0.0, 1.0, 0.0));
                    }
                }
            }
        }
    }
}
