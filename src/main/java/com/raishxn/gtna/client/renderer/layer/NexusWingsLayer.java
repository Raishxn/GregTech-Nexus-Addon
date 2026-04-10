package com.raishxn.gtna.client.renderer.layer;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.client.model.NexusWingsModel;
import com.raishxn.gtna.common.item.armor.NexusArmorHandler;

public class NexusWingsLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    private static final ResourceLocation TEXTURE = GTNACORE.id("textures/entity/nexus_wings.png");
    private final NexusWingsModel wingsModel;

    public NexusWingsLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent,
                           NexusWingsModel wingsModel) {
        super(parent);
        this.wingsModel = wingsModel;
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
        poseStack.translate(0.0D, player.isCrouching() ? 0.18D : 0.0D, 0.125D);

        wingsModel.setupAnim(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        wingsModel.renderToBuffer(poseStack, buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)), packedLight,
                OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();
    }
}
