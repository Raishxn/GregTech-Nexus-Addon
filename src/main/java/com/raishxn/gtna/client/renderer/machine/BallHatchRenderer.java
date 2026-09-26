package com.raishxn.gtna.client.renderer.machine;

import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderManager;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.Codec;
import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.common.machine.multiblock.part.BallHatchPartMachine;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/** Renders the GTO hatch's three-block-wide idle/spinning rotor sprite. */
public class BallHatchRenderer extends DynamicRender<BallHatchPartMachine, BallHatchRenderer> {

    public static final Codec<BallHatchRenderer> CODEC = Codec.unit(BallHatchRenderer::new);
    public static final DynamicRenderType<BallHatchPartMachine, BallHatchRenderer> TYPE = DynamicRenderManager
            .register(GTNACORE.id("ball_hatch/ball"), new DynamicRenderType<>(CODEC));

    private static final ResourceLocation IDLE = GTNACORE.id("textures/block/overlay/machine/ball_hatch_idle.png");
    private static final ResourceLocation SPINNING = GTNACORE
            .id("textures/block/overlay/machine/ball_hatch_spinning.png");

    @Override
    public DynamicRenderType<BallHatchPartMachine, BallHatchRenderer> getType() {
        return TYPE;
    }

    @Override
    public boolean shouldRender(BallHatchPartMachine machine, Vec3 cameraPos) {
        return machine.isFormed() &&
                Vec3.atCenterOf(machine.self().getPos()).closerThan(cameraPos, getViewDistance());
    }

    @Override
    public void render(BallHatchPartMachine machine, float partialTicks, PoseStack poseStack, MultiBufferSource buffer,
                       int combinedLight, int combinedOverlay) {
        if (!machine.isFormed()) return;
        Direction front = machine.getFrontFacing();
        Vector3f normal = new Vector3f(front.getStepX(), front.getStepY(), front.getStepZ());
        Vector3f right = switch (front) {
            case NORTH -> new Vector3f(1, 0, 0);
            case SOUTH -> new Vector3f(-1, 0, 0);
            case EAST -> new Vector3f(0, 0, 1);
            case WEST -> new Vector3f(0, 0, -1);
            case UP, DOWN -> new Vector3f(1, 0, 0);
        };
        Vector3f up = switch (front) {
            case UP -> new Vector3f(0, 0, -1);
            case DOWN -> new Vector3f(0, 0, 1);
            default -> new Vector3f(0, 1, 0);
        };
        Vector3f center = new Vector3f(0.5f, 0.5f, 0.5f).fma(0.512f, normal);
        float frameStart = machine.isWorking() ? (machine.getOffsetTimer() % 4) / 4.0f : 0;
        float frameEnd = machine.isWorking() ? frameStart + 0.25f : 1;
        ResourceLocation texture = machine.isWorking() ? SPINNING : IDLE;
        VertexConsumer vertices = buffer.getBuffer(RenderType.entityTranslucent(texture));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normals = pose.normal();
        vertex(vertices, matrix, normals, corner(center, right, up, -1.5f, -1.5f), normal, 0, frameEnd,
                LightTexture.FULL_BRIGHT);
        vertex(vertices, matrix, normals, corner(center, right, up, 1.5f, -1.5f), normal, 1, frameEnd,
                LightTexture.FULL_BRIGHT);
        vertex(vertices, matrix, normals, corner(center, right, up, 1.5f, 1.5f), normal, 1, frameStart,
                LightTexture.FULL_BRIGHT);
        vertex(vertices, matrix, normals, corner(center, right, up, -1.5f, 1.5f), normal, 0, frameStart,
                LightTexture.FULL_BRIGHT);
    }

    private static Vector3f corner(Vector3f center, Vector3f right, Vector3f up, float x, float y) {
        return new Vector3f(center).fma(x, right).fma(y, up);
    }

    private static void vertex(VertexConsumer vertices, Matrix4f matrix, Matrix3f normals, Vector3f pos,
                               Vector3f normal, float u, float v, int light) {
        vertices.vertex(matrix, pos.x(), pos.y(), pos.z()).color(255, 255, 255, 255)
                .uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light)
                .normal(normals, normal.x(), normal.y(), normal.z()).endVertex();
    }

    @Override
    public boolean shouldRenderOffScreen(BallHatchPartMachine machine) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 64;
    }

    @Override
    public AABB getRenderBoundingBox(BallHatchPartMachine machine) {
        return new AABB(machine.getPos()).inflate(2.0D);
    }
}
