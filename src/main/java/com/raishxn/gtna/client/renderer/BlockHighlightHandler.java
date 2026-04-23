package com.raishxn.gtna.client.renderer;

import com.gregtechceu.gtceu.api.GTValues;

import com.lowdragmc.lowdraglib.client.utils.RenderBufferUtils;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.raishxn.gtna.GTNACORE;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client-side block highlight renderer.
 * <p>
 * Two highlight modes:
 * <ul>
 * <li><b>Static fields</b> ({@link #highlightTicks}, {@link #highlightPos}):
 * Used by the GUI locate buttons (GTMThings pattern). Set directly from
 * the client-side click handler — no network packets needed.</li>
 * <li><b>Map-based</b> ({@link #HIGHLIGHTS}): Used by server-sent highlight
 * packets (commands, etc.).</li>
 * </ul>
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = GTNACORE.MOD_ID, value = Dist.CLIENT)
public class BlockHighlightHandler {

    // ── GTMThings-style static fields (for GUI locate buttons) ──

    /** Remaining ticks to display the highlight. Decremented every second (every 20 ticks). */
    public static int highlightTicks = 0;

    /** Position to highlight (set from client-side click handler). */
    public static BlockPos highlightPos = null;

    // ── Map-based highlights (for server-sent packets / commands) ──

    private static final Map<HighlightEntry, Long> HIGHLIGHTS = new ConcurrentHashMap<>();
    private static final Map<RegionHighlightEntry, RegionHighlightData> REGION_HIGHLIGHTS = new ConcurrentHashMap<>();
    private static final Map<ResourceKey<Level>, GhostHighlightData> GHOST_HIGHLIGHTS = new ConcurrentHashMap<>();

    private record HighlightEntry(BlockPos pos, ResourceKey<Level> dim) {}
    private record RegionHighlightEntry(BlockPos start, BlockPos end, ResourceKey<Level> dim) {}
    private record RegionHighlightData(int color, long expiryTime) {}
    private record GhostHighlightData(int color, long expiryTime, BlockPos[] positions) {}

    /**
     * Schedule a block highlight at the given position/dimension.
     * Used by server-sent packets (commands, etc.).
     */
    public static void highlight(BlockPos pos, ResourceKey<Level> dim, long expiryTime) {
        HIGHLIGHTS.put(new HighlightEntry(pos, dim), expiryTime);
    }

    public static void highlightRegion(BlockPos start, BlockPos end, ResourceKey<Level> dim, int color, long expiryTime) {
        BlockPos min = new BlockPos(
                Math.min(start.getX(), end.getX()),
                Math.min(start.getY(), end.getY()),
                Math.min(start.getZ(), end.getZ()));
        BlockPos max = new BlockPos(
                Math.max(start.getX(), end.getX()),
                Math.max(start.getY(), end.getY()),
                Math.max(start.getZ(), end.getZ()));
        REGION_HIGHLIGHTS.put(new RegionHighlightEntry(min, max, dim), new RegionHighlightData(color, expiryTime));
    }

    public static void stopRegionHighlight(BlockPos start, BlockPos end, ResourceKey<Level> dim) {
        BlockPos min = new BlockPos(
                Math.min(start.getX(), end.getX()),
                Math.min(start.getY(), end.getY()),
                Math.min(start.getZ(), end.getZ()));
        BlockPos max = new BlockPos(
                Math.max(start.getX(), end.getX()),
                Math.max(start.getY(), end.getY()),
                Math.max(start.getZ(), end.getZ()));
        REGION_HIGHLIGHTS.remove(new RegionHighlightEntry(min, max, dim));
    }

    public static void highlightStructureGhost(ResourceKey<Level> dim, java.util.List<BlockPos> positions, int color, long expiryTime) {
        GHOST_HIGHLIGHTS.put(dim, new GhostHighlightData(color, expiryTime, positions.toArray(BlockPos[]::new)));
    }

    public static void clearStructureGhost(ResourceKey<Level> dim) {
        GHOST_HIGHLIGHTS.remove(dim);
    }

    @SubscribeEvent
    public static void onRenderWorld(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        // Tick-down the static highlight counter (every second = every 20 ticks)
        if (highlightTicks > 0 && GTValues.CLIENT_TIME % 20 == 0) {
            highlightTicks--;
        }

        // Clean up expired map-based highlights
        long currentTime = System.currentTimeMillis();
        HIGHLIGHTS.values().removeIf(time -> time < currentTime);
        REGION_HIGHLIGHTS.values().removeIf(data -> data.expiryTime() < currentTime);
        GHOST_HIGHLIGHTS.entrySet().removeIf(entry -> entry.getValue().expiryTime() < currentTime);

        ResourceKey<Level> currentDim = mc.level.dimension();
        boolean hasStaticHighlight = highlightTicks > 0 && highlightPos != null;
        boolean hasMapHighlights = !HIGHLIGHTS.isEmpty();
        boolean hasRegionHighlights = !REGION_HIGHLIGHTS.isEmpty();
        GhostHighlightData ghostHighlight = GHOST_HIGHLIGHTS.get(currentDim);
        boolean hasGhostHighlights = ghostHighlight != null;

        if (!hasStaticHighlight && !hasMapHighlights && !hasRegionHighlights && !hasGhostHighlights) return;

        Camera camera = event.getCamera();
        PoseStack poseStack = event.getPoseStack();
        Vec3 cameraPos = camera.getPosition();
        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        // Set up rendering state — see-through, blended
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        RenderSystem.blendFunc(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer;

        // ── Pass 1: Solid faces (semi-transparent quads) ──
        buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        // Static highlight (red)
        if (hasStaticHighlight) {
            RenderBufferUtils.renderCubeFace(
                    poseStack, buffer,
                    highlightPos.getX(), highlightPos.getY(), highlightPos.getZ(),
                    highlightPos.getX() + 1, highlightPos.getY() + 1, highlightPos.getZ() + 1,
                    1.0f, 0.2f, 0.2f, 0.25f, true);
        }

        // Map-based highlights (red)
        for (Map.Entry<HighlightEntry, Long> entry : HIGHLIGHTS.entrySet()) {
            HighlightEntry loc = entry.getKey();
            if (!loc.dim().equals(currentDim)) continue;
            BlockPos pos = loc.pos();
            RenderBufferUtils.renderCubeFace(
                    poseStack, buffer,
                    pos.getX(), pos.getY(), pos.getZ(),
                    pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1,
                    1.0f, 0.2f, 0.2f, 0.25f, true);
        }

        for (Map.Entry<RegionHighlightEntry, RegionHighlightData> entry : REGION_HIGHLIGHTS.entrySet()) {
            RegionHighlightEntry loc = entry.getKey();
            if (!loc.dim().equals(currentDim)) continue;
            float[] color = unpackColor(entry.getValue().color(), 0.22f);
            BlockPos start = loc.start();
            BlockPos end = loc.end();
            RenderBufferUtils.renderCubeFace(
                    poseStack, buffer,
                    start.getX(), start.getY(), start.getZ(),
                    end.getX() + 1, end.getY() + 1, end.getZ() + 1,
                    color[0], color[1], color[2], color[3], true);
        }

        if (ghostHighlight != null) {
            float[] color = unpackColor(ghostHighlight.color(), 0.16f);
            for (BlockPos pos : ghostHighlight.positions()) {
                RenderBufferUtils.renderCubeFace(
                        poseStack, buffer,
                        pos.getX(), pos.getY(), pos.getZ(),
                        pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1,
                        color[0], color[1], color[2], color[3], true);
            }
        }

        tesselator.end();

        // ── Pass 2: Wireframe edges (lines) ──
        buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
        RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
        RenderSystem.lineWidth(3.0f);

        // Static highlight wireframe
        if (hasStaticHighlight) {
            RenderBufferUtils.drawCubeFrame(
                    poseStack, buffer,
                    highlightPos.getX(), highlightPos.getY(), highlightPos.getZ(),
                    highlightPos.getX() + 1, highlightPos.getY() + 1, highlightPos.getZ() + 1,
                    1.0f, 0.0f, 0.0f, 0.5f);
        }

        // Map-based wireframe
        for (Map.Entry<HighlightEntry, Long> entry : HIGHLIGHTS.entrySet()) {
            HighlightEntry loc = entry.getKey();
            if (!loc.dim().equals(currentDim)) continue;
            BlockPos pos = loc.pos();
            RenderBufferUtils.drawCubeFrame(
                    poseStack, buffer,
                    pos.getX(), pos.getY(), pos.getZ(),
                    pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1,
                    1.0f, 0.0f, 0.0f, 0.5f);
        }

        for (Map.Entry<RegionHighlightEntry, RegionHighlightData> entry : REGION_HIGHLIGHTS.entrySet()) {
            RegionHighlightEntry loc = entry.getKey();
            if (!loc.dim().equals(currentDim)) continue;
            float[] color = unpackColor(entry.getValue().color(), 0.55f);
            BlockPos start = loc.start();
            BlockPos end = loc.end();
            RenderBufferUtils.drawCubeFrame(
                    poseStack, buffer,
                    start.getX(), start.getY(), start.getZ(),
                    end.getX() + 1, end.getY() + 1, end.getZ() + 1,
                    color[0], color[1], color[2], color[3]);
        }

        if (ghostHighlight != null) {
            float[] color = unpackColor(ghostHighlight.color(), 0.28f);
            for (BlockPos pos : ghostHighlight.positions()) {
                RenderBufferUtils.drawCubeFrame(
                        poseStack, buffer,
                        pos.getX(), pos.getY(), pos.getZ(),
                        pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1,
                        color[0], color[1], color[2], color[3]);
            }
        }

        tesselator.end();

        // Restore rendering state
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();

        poseStack.popPose();
    }

    private static float[] unpackColor(int argb, float defaultAlpha) {
        float alpha = ((argb >> 24) & 0xFF) / 255.0f;
        if (alpha <= 0.0f) {
            alpha = defaultAlpha;
        }
        float red = ((argb >> 16) & 0xFF) / 255.0f;
        float green = ((argb >> 8) & 0xFF) / 255.0f;
        float blue = (argb & 0xFF) / 255.0f;
        return new float[] { red, green, blue, alpha };
    }
}
