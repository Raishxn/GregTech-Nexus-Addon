package com.raishxn.gtna.client.renderer.machine;

import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderManager;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;

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
import com.raishxn.gtna.common.machine.multiblock.noenergy.EyeOfHarmonyMachine;
import org.joml.Quaternionf;

public class EyeOfHarmonyRenderer extends DynamicRender<EyeOfHarmonyMachine, EyeOfHarmonyRenderer> {

    public static final Codec<EyeOfHarmonyRenderer> CODEC = Codec.unit(EyeOfHarmonyRenderer::new);
    public static final DynamicRenderType<EyeOfHarmonyMachine, EyeOfHarmonyRenderer> TYPE = DynamicRenderManager
            .register(GTNACORE.id("eye_of_harmony/render"), new DynamicRenderType<>(CODEC));

    private static final ResourceLocation SPACE_MODEL = GTNACORE.id("obj/space");
    private static final ResourceLocation STAR_MODEL = GTNACORE.id("obj/star");

    @Override
    public DynamicRenderType<EyeOfHarmonyMachine, EyeOfHarmonyRenderer> getType() {
        return TYPE;
    }

    @Override
    public boolean shouldRender(EyeOfHarmonyMachine machine, Vec3 cameraPos) {
        return machine.isFormed() && (machine.getCycleState() != 0 || machine.isActive()) &&
                Vec3.atCenterOf(machine.self().getPos()).closerThan(cameraPos, getViewDistance());
    }

    @Override
    public void render(EyeOfHarmonyMachine machine, float partialTicks, PoseStack poseStack, MultiBufferSource buffer,
                       int combinedLight, int combinedOverlay) {
        if (!machine.isFormed() || (machine.getCycleState() == 0 && !machine.isActive())) {
            return;
        }

        float tick = machine.getCycleState() != 0 ? machine.getCycleProgress() +
                (machine.getRecipeLogic().isWorkingEnabled() && machine.getCycleState() == 1 ? partialTicks : 0) :
                machine.getOffsetTimer() + partialTicks;
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
        renderPlanet(tick, poseStack, buffer);
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

    private static void renderPlanet(float tick, PoseStack poseStack, MultiBufferSource buffer) {
        // A paid planetary cycle currently always selects Overworld. One body, not all dimensions.
        poseStack.pushPose();
        double angle = tick / 80D;
        poseStack.translate(7 * Math.sin(angle), 0, 7 * Math.cos(angle));
        poseStack.mulPose(new Quaternionf().fromAxisAngleDeg(0F, 1F, 0F, (tick / 3F) % 360F));
        poseStack.scale(.014F, .014F, .014F);
        renderModel(poseStack, buffer, GTNACORE.id("obj/overworld"), RenderType.solid());
        poseStack.popPose();
    }

    private static void renderOuterSpaceShell(PoseStack poseStack, MultiBufferSource buffer) {
        poseStack.pushPose();
        poseStack.scale(0.175F, 0.175F, 0.175F);
        renderModel(poseStack, buffer, SPACE_MODEL, RenderType.translucent());
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
        return new AABB(machine.getPos()).move(machine.getFrontFacing().getOpposite().getStepX() * 16, 0,
                machine.getFrontFacing().getOpposite().getStepZ() * 16).inflate(15);
    }
}
