package com.raishxn.gtna.client;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.common.item.armor.DiscordNitroArmorHandler;
import com.raishxn.gtna.config.ConfigHolder;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEventHandler {

    private static final ResourceLocation DISCORD_NITRO_WINGS = GTNACORE.id("textures/entity/discord_nitro_wings.png");
    private static ElytraModel<AbstractClientPlayer> nitroWingsModel;

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

    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Post event) {
        if (!(event.getEntity() instanceof AbstractClientPlayer player) ||
                !DiscordNitroArmorHandler.shouldRenderWings(player)) {
            return;
        }

        if (nitroWingsModel == null) {
            nitroWingsModel = new ElytraModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.ELYTRA));
        }

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();

        PlayerModel<AbstractClientPlayer> playerModel = event.getRenderer().getModel();
        playerModel.body.translateAndRotate(poseStack);
        poseStack.translate(0.0D, 0.0D, 0.125D);

        nitroWingsModel.young = false;
        nitroWingsModel.setupAnim(player, 0.0F, 0.0F, event.getPartialTick(), 0.0F, 0.0F);
        nitroWingsModel.renderToBuffer(
                poseStack,
                ItemRenderer.getArmorFoilBuffer(
                        event.getMultiBufferSource(),
                        RenderType.armorCutoutNoCull(DISCORD_NITRO_WINGS),
                        false,
                        false),
                event.getPackedLight(),
                OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.popPose();
    }
}
