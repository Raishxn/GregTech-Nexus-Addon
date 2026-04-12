package com.raishxn.gtna.client.renderer.machine;

import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderManager;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.common.machine.multiblock.noenergy.EyeOfHarmonyMachine;
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

import java.util.List;

public class EyeOfHarmonyRenderer extends DynamicRender<EyeOfHarmonyMachine, EyeOfHarmonyRenderer> {

    public static final Codec<EyeOfHarmonyRenderer> CODEC = Codec.unit(EyeOfHarmonyRenderer::new);
    public static final DynamicRenderType<EyeOfHarmonyMachine, EyeOfHarmonyRenderer> TYPE = DynamicRenderManager
            .register(GTNACORE.id("eye_of_harmony/render"), new DynamicRenderType<>(CODEC));

    private static final ResourceLocation SPACE_MODEL = GTNACORE.id("obj/space");
    private static final ResourceLocation STAR_MODEL = GTNACORE.id("obj/star");
    private static final List<ResourceLocation> ORBIT_OBJECTS = List.of(
            GTNACORE.id("obj/the_nether"),
            GTNACORE.id("obj/overworld"),
            GTNACORE.id("obj/the_end"));

    @Override
    public DynamicRenderType<EyeOfHarmonyMachine, EyeOfHarmonyRenderer> getType() {
        return TYPE;
    }

    @Override
    public boolean shouldRender(EyeOfHarmonyMachine machine, Vec3 cameraPos) {
        return machine.isFormed() && machine.isActive() &&
                Vec3.atCenterOf(machine.self().getPos()).closerThan(cameraPos, getViewDistance());
    }

    @Override
    public void render(EyeOfHarmonyMachine machine, float partialTicks, PoseStack poseStack, MultiBufferSource buffer,
                       int combinedLight, int combinedOverlay) {
        if (!machine.isFormed() || !machine.isActive()) {
            return;
        }

        float tick = machine.getOffsetTimer() + partialTicks;
        double x = 0.5D;
        double y = 0.5D;
        double z = 0.5D;
        switch (machine.getFrontFacing()) {
            case NORTH -> z = 16.5D;
            case SOUTH -> z = -15.5D;
            case WEST -> x = 16.5D;
            case EAST -> x = -15.5D;
        }

        poseStack.pushPose();
        poseStack.translate(x, y, z);
        renderStar(tick, poseStack, buffer);
        renderOrbitObjects(tick, poseStack, buffer, x, y, z);
        renderOuterSpaceShell(poseStack, buffer);
        poseStack.popPose();
    }

    private static void renderStar(float tick, PoseStack poseStack, MultiBufferSource buffer) {
        poseStack.pushPose();
        poseStack.scale(0.02F, 0.02F, 0.02F);
        poseStack.mulPose(new Quaternionf().fromAxisAngleDeg(0F, 1F, 1F, (tick / 2F) % 360F));
        renderModel(poseStack, buffer, STAR_MODEL, RenderType.translucent());
        poseStack.popPose();
    }

    private static void renderOrbitObjects(float tick, PoseStack poseStack, MultiBufferSource buffer,
                                           double x, double y, double z) {
        for (int orbitIndex = 1; orbitIndex <= ORBIT_OBJECTS.size(); orbitIndex++) {
            float scale = 0.007F + 0.003F * orbitIndex;
            poseStack.pushPose();
            poseStack.scale(scale, scale, scale);
            poseStack.mulPose(new Quaternionf().fromAxisAngleDeg(1F, 0F, 1F, (tick * 1.5F / orbitIndex) % 360F));
            poseStack.translate(
                    x + (orbitIndex * 100D + 160D) * Math.sin(tick * orbitIndex / 80D + 0.4D),
                    y,
                    z + (orbitIndex * 100D + 160D) * Math.cos(tick * orbitIndex / 80D + 0.4D));
            renderModel(poseStack, buffer, ORBIT_OBJECTS.get(orbitIndex - 1), RenderType.solid());
            poseStack.popPose();
        }
    }

    private static void renderOuterSpaceShell(PoseStack poseStack, MultiBufferSource buffer) {
        poseStack.pushPose();
        poseStack.scale(0.175F, 0.175F, 0.175F);
        renderModel(poseStack, buffer, SPACE_MODEL, RenderType.solid());
        poseStack.popPose();
    }

    private static void renderModel(PoseStack poseStack, MultiBufferSource buffer, ResourceLocation modelLocation,
                                    RenderType renderType) {
        BakedModel model = Minecraft.getInstance().getModelManager().getModel(modelLocation);
        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(
                poseStack.last(),
                buffer.getBuffer(renderType),
                Blocks.AIR.defaultBlockState(),
                model,
                1.0F,
                1.0F,
                1.0F,
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                ModelData.EMPTY,
                renderType);
    }

    @Override
    public boolean shouldRenderOffScreen(EyeOfHarmonyMachine machine) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public AABB getRenderBoundingBox(EyeOfHarmonyMachine machine) {
        return new AABB(machine.getPos()).inflate(getViewDistance() / 2.0D);
    }
}
