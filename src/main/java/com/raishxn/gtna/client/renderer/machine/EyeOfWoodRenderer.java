package com.raishxn.gtna.client.renderer.machine;

import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderManager;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.ModelData;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.common.machine.multiblock.noenergy.EyeOfWoodMachine;
import org.joml.Quaternionf;

public class EyeOfWoodRenderer extends DynamicRender<EyeOfWoodMachine, EyeOfWoodRenderer> {

    public static final Codec<EyeOfWoodRenderer> CODEC = Codec.unit(EyeOfWoodRenderer::new);
    public static final DynamicRenderType<EyeOfWoodMachine, EyeOfWoodRenderer> TYPE = DynamicRenderManager
            .register(GTNACORE.id("eye_of_wood/render"), new DynamicRenderType<>(CODEC));

    private static final ResourceLocation THINKING_MODEL = GTNACORE.id("obj/eye_of_wood_thinking");
    private static final ResourceLocation SWEAT_MODEL = GTNACORE.id("obj/eye_of_wood_sweat");

    @Override
    public DynamicRenderType<EyeOfWoodMachine, EyeOfWoodRenderer> getType() {
        return TYPE;
    }

    @Override
    public boolean shouldRender(EyeOfWoodMachine machine, Vec3 cameraPos) {
        return machine.shouldRenderEyeModel() &&
                Vec3.atCenterOf(machine.self().getPos()).closerThan(cameraPos, getViewDistance());
    }

    @Override
    public void render(EyeOfWoodMachine machine, float partialTicks, PoseStack poseStack, MultiBufferSource buffer,
                       int combinedLight, int combinedOverlay) {
        if (!machine.shouldRenderEyeModel()) {
            return;
        }

        Vec3 offset = Vec3.atLowerCornerOf(machine.getFrontFacing().getOpposite().getNormal()).scale(16.0D);
        float tick = machine.getOffsetTimer() + partialTicks;
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();

        poseStack.pushPose();
        poseStack.translate(0.5D + offset.x, 0.5D + offset.y, 0.5D + offset.z);
        poseStack.mulPose(new Quaternionf().rotateY((float) Math.toRadians(-camera.getYRot() + 180.0F)));
        poseStack.mulPose(new Quaternionf().rotateX((float) Math.toRadians(camera.getXRot())));
        poseStack.scale(4.5F, 4.5F, 4.5F);
        poseStack.mulPose(new Quaternionf().fromAxisAngleDeg(0F, 1F, 0F, (tick * 0.75F) % 360F));
        renderModel(poseStack, buffer, machine.didLastRollSucceed() ? THINKING_MODEL : SWEAT_MODEL);
        poseStack.popPose();
    }

    private static void renderModel(PoseStack poseStack, MultiBufferSource buffer, ResourceLocation modelLocation) {
        BakedModel model = Minecraft.getInstance().getModelManager().getModel(modelLocation);
        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(
                poseStack.last(),
                buffer.getBuffer(RenderType.translucent()),
                Blocks.AIR.defaultBlockState(),
                model,
                1.0F,
                1.0F,
                1.0F,
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                ModelData.EMPTY,
                RenderType.translucent());
    }

    @Override
    public boolean shouldRenderOffScreen(EyeOfWoodMachine machine) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public AABB getRenderBoundingBox(EyeOfWoodMachine machine) {
        return new AABB(machine.getPos()).inflate(96.0D);
    }
}
