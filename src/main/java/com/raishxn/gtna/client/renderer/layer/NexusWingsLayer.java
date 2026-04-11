package com.raishxn.gtna.client.renderer.layer;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import com.raishxn.gtna.common.data.GTNAItems;
import com.raishxn.gtna.common.item.armor.NexusArmorHandler;

public class NexusWingsLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    public NexusWingsLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer player,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (!NexusArmorHandler.shouldRenderWings(player)) {
            return;
        }

        poseStack.pushPose();
        getParentModel().body.translateAndRotate(poseStack);
        poseStack.translate(0.0D, player.isCrouching() ? -0.12D : -0.3D, 0.2D);
        poseStack.scale(1.0F, -1.0F, -1.0F);

        ItemStack wingsStack = new ItemStack(GTNAItems.NEXUS_WINGS.get());
        Minecraft.getInstance().getItemRenderer().renderStatic(player, wingsStack, ItemDisplayContext.FIXED, false,
                poseStack, buffer, player.level(), packedLight,
                net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY, player.getId());
        poseStack.popPose();
    }
}
