package com.raishxn.gtna.client.renderer.machine;

import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderManager;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.ModelData;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.serialization.Codec;
import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.api.machine.feature.godforge.GodforgeStarColor;
import com.raishxn.gtna.common.machine.multiblock.godforge.ForgeOfGodsMachine;
import com.raishxn.gtna.common.machine.multiblock.godforge.GodforgeRings;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

/**
 * Forge of Gods star, rings and beam, ported from GTNH {@code RenderForgeOfGods} and {@code TileEntityForgeOfGods}
 * (GT5-Unofficial a3e1e112). The star sits 122 blocks behind the controller, as the GTNH render block does; the rings
 * are the real ring blocks taken into the controller, spinning around the beam axis.
 */
public class ForgeOfGodsRenderer extends DynamicRender<ForgeOfGodsMachine, ForgeOfGodsRenderer> {

    public static final Codec<ForgeOfGodsRenderer> CODEC = Codec.unit(ForgeOfGodsRenderer::new);
    public static final DynamicRenderType<ForgeOfGodsMachine, ForgeOfGodsRenderer> TYPE = DynamicRenderManager
            .register(GTNACORE.id("forge_of_gods/render"), new DynamicRenderType<>(CODEC));

    private static final ResourceLocation STAR_LAYER_0 = GTNACORE.id("textures/render/godforge/star_layer_0.png");
    private static final ResourceLocation STAR_LAYER_1 = GTNACORE.id("textures/render/godforge/star_layer_1.png");
    private static final ResourceLocation STAR_LAYER_2 = GTNACORE.id("textures/render/godforge/star_layer_2.png");
    private static final ResourceLocation BEAM_TEXTURE = GTNACORE.id("textures/render/godforge/space_layer.png");

    public static final int STAR_DISTANCE = 122;
    private static final int STAR_TESSELLATION = 128;
    private static final int BEAM_SEGMENT_QUADS = 16;
    private static final float BACK_PLATE_DISTANCE = -121.5f, BACK_PLATE_RADIUS = 13f;
    private static final float COLOR_CYCLE_SPEED = 16f;
    private static final double RING_RADIUS = 63;
    private static final double BEAM_LENGTH = 59;

    @Nullable
    private static VertexBuffer sphere;
    @Nullable
    private static VertexBuffer beamBuffer;
    private static final Map<BlockPos, ClientState> STATES = new HashMap<>();
    @Nullable
    private static Level statesLevel;

    @Override
    public DynamicRenderType<ForgeOfGodsMachine, ForgeOfGodsRenderer> getType() {
        return TYPE;
    }

    @Override
    public boolean shouldRender(ForgeOfGodsMachine machine, Vec3 cameraPos) {
        return machine.isRenderActive();
    }

    @Override
    public boolean shouldRenderOffScreen(ForgeOfGodsMachine machine) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 512;
    }

    @Override
    public AABB getRenderBoundingBox(ForgeOfGodsMachine machine) {
        Direction back = machine.getFrontFacing().getOpposite();
        BlockPos star = machine.getPos().relative(back, STAR_DISTANCE);
        return new AABB(star).inflate(RING_RADIUS + BEAM_LENGTH + 1);
    }

    /** Frames' queued forges; drawn after translucent blocks, outside GTCEu's batched block entity pass. */
    private static final Map<BlockPos, Queued> QUEUE = new java.util.LinkedHashMap<>();

    private record Queued(ForgeOfGodsMachine machine, float partialTicks) {}

    /**
     * The block entity pass only queues the forge (as GTLAdditions' Forge of the Antichrist does); drawing happens in
     * {@link #onRenderLevelStage}, where the depth buffer and GL state belong to the world render.
     */
    @Override
    public void render(ForgeOfGodsMachine machine, float partialTicks, PoseStack poseStack, MultiBufferSource buffer,
                       int combinedLight, int combinedOverlay) {
        if (machine.getLevel() == null || !machine.isRenderActive() || !machine.isFormed()) return;
        QUEUE.put(machine.getPos().immutable(), new Queued(machine, partialTicks));
    }

    public static void onRenderLevelStage(net.minecraftforge.client.event.RenderLevelStageEvent event) {
        if (event.getStage() != net.minecraftforge.client.event.RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS ||
                QUEUE.isEmpty()) {
            return;
        }
        Vec3 camera = event.getCamera().getPosition();
        PoseStack poseStack = event.getPoseStack();
        Matrix4f projection = new Matrix4f(event.getProjectionMatrix());
        try {
            for (Queued queued : QUEUE.values()) {
                ForgeOfGodsMachine machine = queued.machine();
                if (machine.isInValid() || !machine.isRenderActive() || !machine.isFormed()) continue;
                BlockPos pos = machine.getPos();
                poseStack.pushPose();
                poseStack.translate(pos.getX() - camera.x, pos.getY() - camera.y, pos.getZ() - camera.z);
                drawForge(machine, queued.partialTicks(), poseStack, projection);
                poseStack.popPose();
            }
        } finally {
            QUEUE.clear();
            RenderSystem.depthMask(true);
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableCull();
            RenderSystem.enableDepthTest();
        }
    }

    /** GTNH {@code RenderForgeOfGods.renderTileEntityAt}: opaque star, rings, transparent star shells, beam. */
    private static void drawForge(ForgeOfGodsMachine machine, float partialTicks, PoseStack poseStack,
                                  Matrix4f projection) {
        ShaderInstance starShader = GodforgeShaders.star();
        ShaderInstance beamShader = GodforgeShaders.beam();
        Level level = machine.getLevel();
        if (starShader == null || beamShader == null || level == null) return;

        ForgeOfGodsSound.ensurePlaying(machine);
        ClientState state = state(machine, level);
        state.updateColor(machine.starColor());
        float timer = (level.getGameTime() % 1_000_000L) + partialTicks;
        Direction back = machine.getFrontFacing().getOpposite();
        Vector3f axis = new Vector3f(back.getStepX(), back.getStepY(), back.getStepZ());
        Quaternionf beamRotation = new Quaternionf().rotationTo(new Vector3f(0, 0, 1), axis);
        float radius = machine.starSize();
        int rings = Math.max(1, Math.min(3, machine.ringAmount()));

        poseStack.pushPose();
        poseStack.translate(0.5 + back.getStepX() * STAR_DISTANCE, 0.5 + back.getStepY() * STAR_DISTANCE,
                0.5 + back.getStepZ() * STAR_DISTANCE);
        Matrix4f center = new Matrix4f(poseStack.last().pose());
        poseStack.popPose();
        float starTimer = timer * machine.rotationSpeed();

        // 1) Opaque star writes depth.
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.disableCull();
        starShader.safeGetUniform("Gamma").set(state.gamma);
        renderStarLayer(starShader, center, projection, state, 1f, STAR_LAYER_0, radius,
                new Vector3f(0, 1, 1).normalize(), 130 + starTimer % 360000);

        // 2) Rings: solid and depth writing.
        RenderSystem.enableCull();
        renderRings(machine, level, state, center, projection, axis, timer, rings);

        // 3) Transparent star shells: depth tested, no depth write.
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);
        renderStarLayer(starShader, center, projection, state, 0.4f, STAR_LAYER_1, radius * 1.02f,
                new Vector3f(1, 1, 0).normalize(), -49 + starTimer % 360000);
        renderStarLayer(starShader, center, projection, state, 0.2f, STAR_LAYER_2, radius * 1.04f,
                new Vector3f(1, 0, 1).normalize(), 67 + starTimer % 360000);

        // 4) Beam.
        renderBeam(machine, beamShader, center, projection, beamRotation, state, timer, radius, rings);
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
    }

    // ---- Star ----

    private static void renderStarLayer(ShaderInstance shader, Matrix4f center, Matrix4f projection,
                                        ClientState state, float alpha, ResourceLocation texture, float scale,
                                        Vector3f rotationAxis, float degrees) {
        VertexBuffer mesh = sphere();
        Matrix4f model = new Matrix4f(center)
                .rotate((float) Math.toRadians(degrees), rotationAxis)
                .scale(scale);
        RenderSystem.setShaderTexture(0, texture);
        shader.safeGetUniform("StarColor").set(state.r, state.g, state.b, alpha);
        mesh.bind();
        mesh.drawWithShader(model, projection, shader);
        VertexBuffer.unbind();
    }

    /** {@code EOHRenderingUtils.buildSphere}: a unit UV sphere. */
    private static VertexBuffer sphere() {
        if (sphere != null) return sphere;
        BufferBuilder builder = new BufferBuilder(STAR_TESSELLATION * STAR_TESSELLATION * 6 * 20);
        builder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_TEX);
        int slices = STAR_TESSELLATION, stacks = STAR_TESSELLATION;
        for (int i = 0; i < stacks; i++) {
            float v0 = (float) i / stacks, v1 = (float) (i + 1) / stacks;
            double phi0 = Math.PI / 2.0 - i * Math.PI / stacks;
            double phi1 = Math.PI / 2.0 - (i + 1) * Math.PI / stacks;
            float y0 = (float) Math.sin(phi0), y1 = (float) Math.sin(phi1);
            double r0 = Math.cos(phi0), r1 = Math.cos(phi1);
            for (int j = 0; j < slices; j++) {
                float u0 = 1f - (float) j / slices, u1 = 1f - (float) (j + 1) / slices;
                double th0 = j * 2.0 * Math.PI / slices, th1 = (j + 1) * 2.0 * Math.PI / slices;
                float x00 = (float) (r0 * Math.cos(th0)), z00 = (float) (r0 * Math.sin(th0));
                float x10 = (float) (r1 * Math.cos(th0)), z10 = (float) (r1 * Math.sin(th0));
                float x11 = (float) (r1 * Math.cos(th1)), z11 = (float) (r1 * Math.sin(th1));
                float x01 = (float) (r0 * Math.cos(th1)), z01 = (float) (r0 * Math.sin(th1));
                builder.vertex(x00, y0, z00).uv(u0, v0).endVertex();
                builder.vertex(x10, y1, z10).uv(u0, v1).endVertex();
                builder.vertex(x11, y1, z11).uv(u1, v1).endVertex();
                builder.vertex(x00, y0, z00).uv(u0, v0).endVertex();
                builder.vertex(x11, y1, z11).uv(u1, v1).endVertex();
                builder.vertex(x01, y0, z01).uv(u1, v0).endVertex();
            }
        }
        sphere = new VertexBuffer(VertexBuffer.Usage.STATIC);
        sphere.bind();
        sphere.upload(builder.end());
        VertexBuffer.unbind();
        return sphere;
    }

    // ---- Rings ----

    private static void renderRings(ForgeOfGodsMachine machine, Level level, ClientState state, Matrix4f center,
                                    Matrix4f projection, Vector3f axis, float timer, int rings) {
        ShaderInstance shader = GameRenderer.getRendertypeCutoutShader();
        if (shader == null) return;
        RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
        Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();
        float[] spins = { timer / 6 * 7, -timer / 4 * 5, timer * 3 };
        for (int ring = 1; ring <= rings; ring++) {
            VertexBuffer mesh = state.ring(machine, level, ring);
            if (mesh == null) continue;
            Matrix4f model = new Matrix4f(center)
                    .rotate((float) Math.toRadians(spins[ring - 1]), axis)
                    .translate(-0.5f, -0.5f, -0.5f);
            mesh.bind();
            mesh.drawWithShader(model, projection, shader);
        }
        VertexBuffer.unbind();
    }

    /** {@code StructureVBO.build}: the ring blocks around the star, hidden faces between ring blocks culled. */
    @Nullable
    private static VertexBuffer buildRing(ForgeOfGodsMachine machine, Level level, int ring) {
        Map<BlockPos, Character> blocks = GodforgeRings.positions(level, machine.getPos(), machine.getFrontFacing(),
                machine.getUpwardsFacing(), machine.isFlipped(), ring);
        if (blocks.isEmpty()) return null;
        BlockPos star = machine.getPos().relative(machine.getFrontFacing().getOpposite(), STAR_DISTANCE);
        var dispatcher = Minecraft.getInstance().getBlockRenderer();
        RandomSource random = RandomSource.create(42);
        BufferBuilder builder = new BufferBuilder(blocks.size() * 4 * DefaultVertexFormat.BLOCK.getVertexSize());
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
        PoseStack poseStack = new PoseStack();
        for (var entry : blocks.entrySet()) {
            BlockPos pos = entry.getKey();
            BlockState blockState = GodforgeRings.blockFor(entry.getValue()).defaultBlockState();
            BakedModel model = dispatcher.getBlockModel(blockState);
            poseStack.pushPose();
            poseStack.translate(pos.getX() - star.getX(), pos.getY() - star.getY(), pos.getZ() - star.getZ());
            for (Direction side : Direction.values()) {
                if (blocks.containsKey(pos.relative(side))) continue;
                random.setSeed(42);
                for (BakedQuad quad : model.getQuads(blockState, side, random, ModelData.EMPTY, null)) {
                    builder.putBulkData(poseStack.last(), quad, 1f, 1f, 1f, LightTexture.FULL_BRIGHT,
                            OverlayTexture.NO_OVERLAY);
                }
            }
            random.setSeed(42);
            for (BakedQuad quad : model.getQuads(blockState, null, random, ModelData.EMPTY, null)) {
                builder.putBulkData(poseStack.last(), quad, 1f, 1f, 1f, LightTexture.FULL_BRIGHT,
                        OverlayTexture.NO_OVERLAY);
            }
            poseStack.popPose();
        }
        VertexBuffer mesh = new VertexBuffer(VertexBuffer.Usage.STATIC);
        mesh.bind();
        mesh.upload(builder.end());
        VertexBuffer.unbind();
        return mesh;
    }

    // ---- Beam ----

    private static void renderBeam(ForgeOfGodsMachine machine, ShaderInstance shader, Matrix4f center,
                                   Matrix4f projection, Quaternionf beamRotation, ClientState state, float timer,
                                   float radius, int rings) {
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Direction back = machine.getFrontFacing().getOpposite();
        Vec3 starCenter = Vec3.atCenterOf(machine.getPos().relative(back, STAR_DISTANCE));
        Vec3 relative = camera.getPosition().subtract(starCenter);
        Vector3f cameraLocal = new Vector3f((float) relative.x, (float) relative.y, (float) relative.z)
                .rotate(new Quaternionf(beamRotation).conjugate());
        float cameraAngle = (float) Math.atan2(cameraLocal.y, cameraLocal.x);
        Matrix4f model = new Matrix4f(center).rotate(beamRotation);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShaderTexture(0, BEAM_TEXTURE);
        shader.safeGetUniform("CameraLocal").set(cameraLocal.x, cameraLocal.y, cameraLocal.z);

        // Soft beam in the star color, then the intense white core.
        shader.safeGetUniform("BeamColor").set(state.r, state.g, state.b);
        shader.safeGetUniform("Intensity").set(2f);
        drawBeam(shader, model, projection, softBeam(radius, rings), cameraAngle, timer);
        shader.safeGetUniform("BeamColor").set(1f, 1f, 1f);
        shader.safeGetUniform("Intensity").set(4f);
        drawBeam(shader, model, projection, intenseBeam(radius, rings), cameraAngle, timer);
    }

    /** One segment per row: radius, z offset along the beam, transparency ({@code u_SegmentArray}). */
    private static float[][] softBeam(float starRadius, int rings) {
        float angle = startAngle(starRadius, rings);
        float radius = starRadius * 1.1f;
        float startX = -radius * (float) Math.cos(angle);
        float startY = radius * (float) Math.sin(angle);
        float[][] segments = new float[rings + 2][];
        int i = 0;
        segments[i++] = new float[] { startY, startX, 0 };
        for (int lens = rings - 1; lens >= 0; lens--) {
            segments[i++] = new float[] { lensRadius(lens), lensDistance(lens), 1f };
        }
        segments[i] = new float[] { BACK_PLATE_RADIUS, BACK_PLATE_DISTANCE, -.05f };
        return segments;
    }

    private static float[][] intenseBeam(float starRadius, int rings) {
        float angle = startAngle(starRadius, rings);
        float radius = starRadius * 1.05f;
        float startX = -radius * (float) Math.cos(angle);
        float startY = radius * (float) Math.sin(angle);
        int firstLens = rings - 1;
        float nextX = lensDistance(firstLens);
        float nextY = lensRadius(firstLens) * .75f;
        float backX = Math.max(-radius, (nextX + radius) / 2);
        float backY = interpolate(startX, nextX, startY, nextY, backX);
        float[][] segments = new float[rings + 3][];
        int i = 0;
        segments[i++] = new float[] { backY, backX, 0 };
        float transparency = .2f;
        for (int lens = rings - 1; lens >= 0; lens--) {
            segments[i++] = new float[] { lensRadius(lens) / 2, lensDistance(lens), transparency };
            transparency += .3f;
        }
        float currX = lensDistance(0);
        float currY = lensRadius(0) / 2;
        float lastX = BACK_PLATE_DISTANCE;
        float lastY = Math.min(lensRadius(firstLens), BACK_PLATE_RADIUS);
        float midX = lastX + 8f;
        float midY = interpolate(currX, lastX, currY, lastY, midX);
        segments[i++] = new float[] { midY, midX, transparency };
        segments[i] = new float[] { lastY, lastX, 0f };
        return segments;
    }

    /** {@code gorgeBeam.vert}: a half cylinder per segment, turned towards the camera. */
    private static void drawBeam(ShaderInstance shader, Matrix4f model, Matrix4f projection, float[][] segments,
                                 float cameraAngle, float time) {
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);
        float shift = cameraAngle - (float) Math.PI / 2;
        float timer = time / 240f;
        for (int s = 0; s + 1 < segments.length; s++) {
            float[] seg0 = segments[s], seg1 = segments[s + 1];
            for (int q = 0; q < BEAM_SEGMENT_QUADS; q++) {
                float a0 = (float) Math.PI * q / BEAM_SEGMENT_QUADS + shift;
                float a1 = (float) Math.PI * (q + 1) / BEAM_SEGMENT_QUADS + shift;
                beamVertex(builder, a1, seg1, timer);
                beamVertex(builder, a1, seg0, timer);
                beamVertex(builder, a0, seg0, timer);
                beamVertex(builder, a0, seg1, timer);
            }
        }
        if (beamBuffer == null) beamBuffer = new VertexBuffer(VertexBuffer.Usage.DYNAMIC);
        beamBuffer.bind();
        beamBuffer.upload(builder.end());
        beamBuffer.drawWithShader(model, projection, shader);
        VertexBuffer.unbind();
    }

    private static void beamVertex(BufferBuilder builder, float angle, float[] segment, float timer) {
        float radius = segment[0], offset = segment[1], transparency = segment[2];
        float x = (float) Math.cos(angle) * radius, y = (float) Math.sin(angle) * radius;
        float heightOffset = offset / 256 + timer;
        float v = angle / (2 * (float) Math.PI) + heightOffset / 3 + timer;
        builder.vertex(x, y, offset).uv(heightOffset, v)
                .color(1f, 1f, 1f, Math.max(0f, Math.min(1f, transparency)))
                .normal((float) Math.cos(angle), (float) Math.sin(angle), 0).endVertex();
    }

    private static float lensDistance(int lens) {
        return switch (lens) {
            case 0 -> -61.5f;
            case 1 -> -54.5f;
            default -> -44.5f;
        };
    }

    private static float lensRadius(int lens) {
        return switch (lens) {
            case 0 -> 1.1f;
            case 1 -> 3.5f;
            default -> 5f;
        };
    }

    private static float startAngle(float radius, int rings) {
        float x = -lensDistance(rings - 1);
        float y = lensRadius(rings - 1);
        float alpha = (float) Math.atan2(y, x);
        float beta = (float) Math.asin(Math.min(1, radius / Math.sqrt(x * x + y * y)));
        return alpha + ((float) Math.PI / 2 - beta);
    }

    private static float interpolate(float x0, float x1, float y0, float y1, float x) {
        return y0 + ((x - x0) * (y1 - y0)) / (x1 - x0);
    }

    // ---- Client state ----

    private static ClientState state(ForgeOfGodsMachine machine, Level level) {
        if (statesLevel != level) {
            STATES.values().forEach(ClientState::close);
            STATES.clear();
            statesLevel = level;
        }
        return STATES.computeIfAbsent(machine.getPos().immutable(), pos -> new ClientState());
    }

    /** Ring meshes and the color cycle of {@code TileEntityForgeOfGods}. */
    private static final class ClientState {

        private final VertexBuffer[] rings = new VertexBuffer[3];
        private Direction ringFacing;
        private Direction ringUp;
        private String colorKey = "";
        private GodforgeStarColor color = GodforgeStarColor.DEFAULT;
        private float r, g, b, gamma = GodforgeStarColor.DEFAULT_GAMMA;
        private long lastColorUpdate;
        private float cycleStep;
        private int interpIndex;
        private int interpA, interpB;
        private float gammaA, gammaB;

        @Nullable
        VertexBuffer ring(ForgeOfGodsMachine machine, Level level, int ring) {
            if (ringFacing != machine.getFrontFacing() || ringUp != machine.getUpwardsFacing()) {
                close();
                ringFacing = machine.getFrontFacing();
                ringUp = machine.getUpwardsFacing();
            }
            if (rings[ring - 1] == null) rings[ring - 1] = buildRing(machine, level, ring);
            return rings[ring - 1];
        }

        void close() {
            for (int i = 0; i < rings.length; i++) {
                if (rings[i] != null) rings[i].close();
                rings[i] = null;
            }
        }

        void updateColor(String serialized) {
            if (!serialized.equals(colorKey)) {
                colorKey = serialized;
                GodforgeStarColor parsed = GodforgeStarColor.deserialize(serialized);
                setColor(parsed == null || parsed.numColors() == 0 ? GodforgeStarColor.DEFAULT : parsed);
            }
            incrementColors();
        }

        private void setColor(GodforgeStarColor color) {
            this.color = color;
            var first = color.color(0);
            apply(rgb(first), first.gamma());
            lastColorUpdate = 0;
            interpIndex = 0;
            if (color.numColors() > 1) {
                cycleStep = 0;
                interpA = rgb(first);
                gammaA = first.gamma();
                var next = color.color(1);
                interpB = rgb(next);
                gammaB = next.gamma();
                interpIndex = 1;
            }
        }

        private void incrementColors() {
            if (color.numColors() <= 1) return;
            long now = System.currentTimeMillis();
            if (lastColorUpdate == 0) {
                lastColorUpdate = now;
                return;
            }
            long delta = now - lastColorUpdate;
            lastColorUpdate = now;
            cycleStep += color.cycleSpeed() * (delta / COLOR_CYCLE_SPEED);
            while (cycleStep >= 255f) {
                cycleStep -= 255f;
                interpA = interpB;
                gammaA = gammaB;
                interpIndex = (interpIndex + 1) % color.numColors();
                var next = color.color(interpIndex);
                interpB = rgb(next);
                gammaB = next.gamma();
            }
            float position = cycleStep / 255f;
            int red = lerp(interpA >> 16 & 255, interpB >> 16 & 255, position);
            int green = lerp(interpA >> 8 & 255, interpB >> 8 & 255, position);
            int blue = lerp(interpA & 255, interpB & 255, position);
            apply(red << 16 | green << 8 | blue, gammaA + (gammaB - gammaA) * position);
        }

        private void apply(int rgb, float gamma) {
            r = (rgb >> 16 & 255) / 255f;
            g = (rgb >> 8 & 255) / 255f;
            b = (rgb & 255) / 255f;
            this.gamma = gamma;
        }

        private static int rgb(GodforgeStarColor.Setting setting) {
            return setting.r() << 16 | setting.g() << 8 | setting.b();
        }

        private static int lerp(int a, int b, float t) {
            return Math.round(a + (b - a) * t);
        }
    }
}
