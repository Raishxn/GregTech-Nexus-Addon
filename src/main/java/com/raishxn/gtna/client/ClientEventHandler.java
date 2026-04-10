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
import com.raishxn.gtna.client.model.NexusWingsModel;
import com.raishxn.gtna.client.renderer.layer.NexusWingsLayer;
import com.raishxn.gtna.config.ConfigHolder;

@Mod.EventBusSubscriber(modid = GTNACORE.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEventHandler {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.side.isClient()) {
            Player player = event.player;
            if (player == Minecraft.getInstance().player) {
                if (ConfigHolder.INSTANCE.disableDrift && player.getAbilities().flying) {
                    if (player.zza == 0 && player.xxa == 0) {
                        player.setDeltaMovement(player.getDeltaMovement().multiply(0.0, 1.0, 0.0));
                    }
                }
            }
        }
    }

    @Mod.EventBusSubscriber(modid = GTNACORE.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModClientEvents {

        @SubscribeEvent
        public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
            event.registerLayerDefinition(NexusWingsModel.LAYER_LOCATION, NexusWingsModel::createBodyLayer);
        }

        @SubscribeEvent
        public static void addLayers(EntityRenderersEvent.AddLayers event) {
            for (String skin : event.getSkins()) {
                PlayerRenderer renderer = event.getPlayerSkin(skin);
                if (renderer != null) {
                    renderer.addLayer(new NexusWingsLayer(renderer,
                            new NexusWingsModel(event.getEntityModels().bakeLayer(NexusWingsModel.LAYER_LOCATION))));
                }
            }
        }
    }
}
