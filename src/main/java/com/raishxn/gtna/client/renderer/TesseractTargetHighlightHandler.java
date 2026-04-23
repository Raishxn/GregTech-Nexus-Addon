package com.raishxn.gtna.client.renderer;

import com.lowdragmc.lowdraglib.client.utils.RenderBufferUtils;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.google.common.collect.ImmutableList;
import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.common.item.TesseractTargetMarkerBehavior;
import com.raishxn.gtna.common.machine.tesseract.DirectedTesseractMachine;
import com.raishxn.gtna.common.machine.tesseract.TesseractDirectedTarget;
import org.joml.Matrix4f;

import java.awt.Color;
import java.util.List;

@Mod.EventBusSubscriber(modid = GTNACORE.MOD_ID, value = Dist.CLIENT)
public class TesseractTargetHighlightHandler {

    private static final float HUE_RED = 0.0f;
    private static final float HUE_PURPLE = 0.8333f;

    @SubscribeEvent
    public static void renderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS) {
            return;
        }
        if (Minecraft.getInstance().player == null) {
            return;
        }

        var item = Minecraft.getInstance().player.getMainHandItem();
        if (TesseractTargetMarkerBehavior.isTesseractTargetMarker(item)) {
            renderDirectedTargets(event, TesseractTargetMarkerBehavior.getAllTargets(item));
        }

        if (!DirectedTesseractMachine.HIGHLIGHTS.isEmpty()) {
            for (ImmutableList<TesseractDirectedTarget> targets : List.copyOf(DirectedTesseractMachine.HIGHLIGHTS.elementSet())) {
                renderDirectedTargets(event, targets);
            }
        }
    }

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START || DirectedTesseractMachine.HIGHLIGHTS.isEmpty()) {
            return;
        }
        List.copyOf(DirectedTesseractMachine.HIGHLIGHTS.elementSet())
                .forEach(DirectedTesseractMachine.HIGHLIGHTS::remove);
    }

    private static void renderDirectedTargets(RenderLevelStageEvent event, List<TesseractDirectedTarget> targets) {
        if (targets.isEmpty()) {
            return;
        }

        var level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        Vec3 cameraPos = event.getCamera().getPosition();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        int totalLength = Math.max(1, targets.size() * 2);
        for (TesseractDirectedTarget target : targets) {
            if (target.pos().dimension() != level.dimension()) {
                continue;
            }

            float percent = Math.max(0.0f, Math.min(1.0f, target.order() / (float) totalLength));
            Color color = Color.getHSBColor(lerpHue(percent), 1.0f, 1.0f);

            double faceMinX = target.pos().pos().getX() + (target.face() == Direction.EAST ? 1.0 : 0.0);
            double faceMinY = target.pos().pos().getY() + (target.face() == Direction.UP ? 1.0 : 0.0);
            double faceMinZ = target.pos().pos().getZ() + (target.face() == Direction.SOUTH ? 1.0 : 0.0);
            double faceMaxX = faceMinX + (target.face().getAxis() == Direction.Axis.X ? 0.0 : 1.0);
            double faceMaxY = faceMinY + (target.face().getAxis() == Direction.Axis.Y ? 0.0 : 1.0);
            double faceMaxZ = faceMinZ + (target.face().getAxis() == Direction.Axis.Z ? 0.0 : 1.0);

            RenderSystem.disableDepthTest();
            RenderSystem.enableBlend();
            RenderSystem.disableCull();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder buffer = tesselator.getBuilder();

            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            RenderBufferUtils.renderCubeFace(
                    poseStack,
                    buffer,
                    (float) faceMinX, (float) faceMinY, (float) faceMinZ,
                    (float) faceMaxX, (float) faceMaxY, (float) faceMaxZ,
                    color.getRed() / 255f,
                    color.getGreen() / 255f,
                    color.getBlue() / 255f,
                    0.16f,
                    false);
            tesselator.end();

            buffer.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
            RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
            RenderSystem.lineWidth(4.0f);
            RenderBufferUtils.drawCubeFrame(
                    poseStack,
                    buffer,
                    (float) faceMinX, (float) faceMinY, (float) faceMinZ,
                    (float) faceMaxX, (float) faceMaxY, (float) faceMaxZ,
                    color.getRed() / 255f,
                    color.getGreen() / 255f,
                    color.getBlue() / 255f,
                    0.42f);
            tesselator.end();

            poseStack.pushPose();
            poseStack.translate((faceMinX + faceMaxX) / 2.0, (faceMinY + faceMaxY) / 2.0, (faceMinZ + faceMaxZ) / 2.0);
            poseStack.scale(-0.03f, -0.03f, -0.03f);
            poseStack.mulPose(event.getCamera().rotation());
            Matrix4f matrix = poseStack.last().pose();
            Font font = Minecraft.getInstance().font;
            String label = String.valueOf(target.order());
            font.drawInBatch(
                    label,
                    -font.width(label) / 2f,
                    -font.lineHeight / 2f,
                    color.getRGB(),
                    false,
                    matrix,
                    Minecraft.getInstance().renderBuffers().bufferSource(),
                    Font.DisplayMode.SEE_THROUGH,
                    0,
                    15728880);
            font.drawInBatch(
                    label,
                    -font.width(label) / 2f,
                    -font.lineHeight / 2f,
                    color.getRGB(),
                    false,
                    matrix,
                    Minecraft.getInstance().renderBuffers().bufferSource(),
                    Font.DisplayMode.NORMAL,
                    0,
                    15728880);
            poseStack.popPose();

            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            RenderSystem.enableDepthTest();
        }

        poseStack.popPose();
    }

    private static float lerpHue(float percent) {
        return HUE_RED + percent * (HUE_PURPLE - HUE_RED);
    }
}
