package com.raishxn.gtna.client.personalspace;

import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.EnchantTableRenderer;
import net.minecraft.util.Mth;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.raishxn.gtna.common.world.personalspace.PersonalSpacePortalEntity;

/** The open book floating over the portal, turned toward its facing, as in the original {@code RenderPortal}. */
public final class PersonalSpacePortalRenderer implements BlockEntityRenderer<PersonalSpacePortalEntity> {

    private final BookModel bookModel;

    public PersonalSpacePortalRenderer(BlockEntityRendererProvider.Context context) {
        this.bookModel = new BookModel(context.bakeLayer(ModelLayers.BOOK));
    }

    @Override
    public void render(PersonalSpacePortalEntity portal, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.75F, 0.5F);
        float time = portal.tickCount + partialTick;
        poseStack.translate(0.0F, 0.1F + Mth.sin(time * 0.1F) * 0.01F, 0.0F);
        float facingAngle = (float) Math.atan2(portal.facing().getStepZ(), portal.facing().getStepX());
        poseStack.mulPose(Axis.YP.rotation(-facingAngle));
        poseStack.mulPose(Axis.ZP.rotationDegrees(80.0F));
        float page = Mth.lerp(partialTick, portal.prevPagePosition, portal.pagePosition);
        float right = Mth.clamp(Mth.frac(page + 0.25F) * 1.6F - 0.3F, 0.0F, 1.0F);
        float left = Mth.clamp(Mth.frac(page + 0.75F) * 1.6F - 0.3F, 0.0F, 1.0F);
        bookModel.setupAnim(time, right, left, 1.0F);
        var consumer = EnchantTableRenderer.BOOK_LOCATION.buffer(buffer, RenderType::entitySolid);
        bookModel.render(poseStack, consumer, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();
    }
}
