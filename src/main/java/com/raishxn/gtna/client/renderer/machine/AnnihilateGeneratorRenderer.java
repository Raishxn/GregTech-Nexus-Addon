package com.raishxn.gtna.client.renderer.machine;

import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderManager;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;
import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.common.machine.multiblock.energy.ArtificialStarMachine;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
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
import org.joml.Quaternionf;

public class AnnihilateGeneratorRenderer extends DynamicRender<ArtificialStarMachine, AnnihilateGeneratorRenderer> {

    public static final Codec<AnnihilateGeneratorRenderer> CODEC = Codec.unit(AnnihilateGeneratorRenderer::new);
    public static final DynamicRenderType<ArtificialStarMachine, AnnihilateGeneratorRenderer> TYPE = DynamicRenderManager
            .register(GTNACORE.id("annihilate_generator/star"), new DynamicRenderType<>(CODEC));

    private static final ResourceLocation STAR_MODEL = GTNACORE.id("obj/star");

    public AnnihilateGeneratorRenderer() {}

    @Override
    public DynamicRenderType<ArtificialStarMachine, AnnihilateGeneratorRenderer> getType() {
        return TYPE;
    }

    @Override
    public boolean shouldRender(ArtificialStarMachine machine, Vec3 cameraPos) {
        return machine.isFormed() && machine.isActive() &&
                Vec3.atCenterOf(machine.self().getPos()).closerThan(cameraPos, getViewDistance());
    }

    @Override
    public void render(ArtificialStarMachine machine, float partialTicks, PoseStack poseStack, MultiBufferSource buffer,
                       int combinedLight, int combinedOverlay) {
        if (!machine.isFormed() || !machine.isActive()) {
            return;
        }
        float tick = machine.getOffsetTimer() + partialTicks;
        double x = 0.5, y = 36.5, z = 0.5;
        switch (machine.getFrontFacing()) {
            case NORTH -> z = 39.5;
            case SOUTH -> z = -38.5;
            case WEST -> x = 39.5;
            case EAST -> x = -38.5;
        }
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        renderStar(tick, poseStack, buffer);
        poseStack.popPose();
    }

    private static void renderStar(float tick, PoseStack poseStack, MultiBufferSource buffer) {
        poseStack.pushPose();
        poseStack.scale(0.45F, 0.45F, 0.45F);
        poseStack.mulPose(new Quaternionf().fromAxisAngleDeg(0F, 1F, 1F, tick % 360F));

        BakedModel model = Minecraft.getInstance().getModelManager().getModel(STAR_MODEL);
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
        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(ArtificialStarMachine machine) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 512;
    }

    @Override
    public AABB getRenderBoundingBox(ArtificialStarMachine machine) {
        return new AABB(machine.getPos()).inflate(getViewDistance() / 2.0D);
    }
}
