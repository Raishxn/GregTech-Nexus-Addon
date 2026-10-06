package com.raishxn.gtna.client.personalspace.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import com.mojang.blaze3d.platform.NativeImage;
import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.common.world.personalspace.PersonalSpaceBlocks;
import com.raishxn.gtna.common.world.personalspace.PersonalSpaceSettings;
import com.raishxn.gtna.common.world.personalspace.PersonalSpaceSettings.CenterDirection;
import com.raishxn.gtna.common.world.personalspace.PersonalSpaceSettings.GapPreset;

import java.awt.Rectangle;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Top-down preview of lots, boundaries, roads and center marker, ported from the original editor. */
public class WPreviewPanel extends Widget {

    private static final int TEX_SIZE = 128;
    private static final Map<Block, Integer> COLOR_CACHE = new HashMap<>();
    private static int instances = 0;

    private final PersonalSpaceSettings config;
    private final DynamicTexture texture;
    private final ResourceLocation textureLocation;
    private int lastHash = 0;

    public WPreviewPanel(Rectangle position, PersonalSpaceSettings config) {
        this.position = position;
        this.config = config;
        this.texture = new DynamicTexture(TEX_SIZE, TEX_SIZE, true);
        this.textureLocation = GTNACORE.id("personalspace_preview_" + instances++);
        Minecraft.getInstance().getTextureManager().register(textureLocation, texture);
    }

    public void close() {
        Minecraft.getInstance().getTextureManager().release(textureLocation);
    }

    @Override
    protected void drawImpl(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int hash = config.generationHash();
        if (hash != lastHash) {
            lastHash = hash;
            updatePreview();
        }
        graphics.fill(-1, -1, TEX_SIZE + 1, TEX_SIZE + 1, 0xFF000000);
        graphics.blit(textureLocation, 0, 0, 0, 0, TEX_SIZE, TEX_SIZE, TEX_SIZE, TEX_SIZE);
    }

    private void updatePreview() {
        NativeImage pixels = texture.getPixels();
        if (pixels == null) return;
        int intervalX = config.getBoundaryChunkIntervalX();
        int intervalZ = config.getBoundaryChunkIntervalZ();
        int gapWidth = config.getGapWidth();
        int mainColor = topLayerColor();
        if (intervalX <= 0 && intervalZ <= 0) {
            for (int z = 0; z < TEX_SIZE; z++) for (int x = 0; x < TEX_SIZE; x++) set(pixels, x, z, mainColor);
            texture.upload();
            return;
        }
        int periodX = intervalX + gapWidth;
        int periodZ = intervalZ + gapWidth;
        int boundaryA = blockColor(config.getBoundaryBlockA());
        int boundaryB = blockColor(config.getBoundaryBlockB());
        int gapA = blockColor(config.getGapBlockA());
        int gapB = blockColor(config.getGapBlockB());
        int gapC = blockColor(config.getGapBlockC());
        int centerRaw = config.isCenterEnabled() ? blockColor(config.getCenterBlock()) : 0;
        int center = centerRaw != 0 ? centerRaw : mainColor;
        int maxPeriodBlocks = Math.max(periodX > 0 ? periodX * 16 : 16, periodZ > 0 ? periodZ * 16 : 16);
        float scale = Math.max(1.0F, maxPeriodBlocks * 2.0F / TEX_SIZE);
        for (int pz = 0; pz < TEX_SIZE; pz++) {
            for (int px = 0; px < TEX_SIZE; px++) {
                set(pixels, px, pz, color((int) (px * scale), (int) (pz * scale), intervalX, intervalZ, gapWidth,
                        periodX, periodZ, mainColor, boundaryA, boundaryB, gapA, gapB, gapC, center));
            }
        }
        texture.upload();
    }

    private static void set(NativeImage image, int x, int z, int argb) {
        int a = argb >>> 24, r = argb >> 16 & 255, g = argb >> 8 & 255, b = argb & 255;
        image.setPixelRGBA(x, z, a << 24 | b << 16 | g << 8 | r);
    }

    private int color(int worldX, int worldZ, int intervalX, int intervalZ, int gapWidth, int periodX, int periodZ,
                      int mainColor, int bA, int bB, int gapA, int gapB, int gapC, int centerColor) {
        int chunkX = worldX >> 4;
        int chunkZ = worldZ >> 4;
        int localX = worldX & 15;
        int localZ = worldZ & 15;
        boolean gapX = gapWidth > 0 && intervalX > 0 && periodX > 0 && mod(chunkX, periodX) >= intervalX;
        boolean gapZ = gapWidth > 0 && intervalZ > 0 && periodZ > 0 && mod(chunkZ, periodZ) >= intervalZ;
        if (gapX || gapZ) {
            return gapColor(chunkX, chunkZ, localX, localZ, worldX, worldZ, gapX, gapZ, periodX, periodZ, gapWidth,
                    intervalX, intervalZ, gapA, gapB, gapC);
        }
        boolean isBoundaryX, prevBoundaryX, isBoundaryZ, prevBoundaryZ;
        if (gapWidth > 0) {
            isBoundaryX = intervalX > 0 && periodX > 0 && mod(chunkX, periodX) == 0;
            prevBoundaryX = intervalX > 0 && periodX > 0 && mod(chunkX, periodX) == intervalX - 1;
            isBoundaryZ = intervalZ > 0 && periodZ > 0 && mod(chunkZ, periodZ) == 0;
            prevBoundaryZ = intervalZ > 0 && periodZ > 0 && mod(chunkZ, periodZ) == intervalZ - 1;
        } else {
            isBoundaryX = intervalX > 0 && mod(chunkX, intervalX) == 0;
            isBoundaryZ = intervalZ > 0 && mod(chunkZ, intervalZ) == 0;
            prevBoundaryX = intervalX > 0 && mod(chunkX + 1, intervalX) == 0;
            prevBoundaryZ = intervalZ > 0 && mod(chunkZ + 1, intervalZ) == 0;
        }
        if (isBoundaryX && localX == 0 || prevBoundaryX && localX == 15 || isBoundaryZ && localZ == 0 ||
                prevBoundaryZ && localZ == 15) {
            boolean useA = ((worldX + worldZ) & 1) == 0;
            if (useA) return bA != 0 ? bA : bB != 0 ? bB : mainColor;
            return bB != 0 ? bB : bA != 0 ? bA : mainColor;
        }
        if (config.isCenterEnabled() && intervalX > 0 && intervalZ > 0 && periodX > 0 && periodZ > 0) {
            CenterDirection dir = config.getCenterDirection();
            int centerX = intervalX * 8 + (dir == CenterDirection.SW || dir == CenterDirection.NW ? -1 : 0);
            int centerZ = intervalZ * 8 + (dir == CenterDirection.NE || dir == CenterDirection.NW ? -1 : 0);
            int modX = mod(chunkX, periodX);
            int modZ = mod(chunkZ, periodZ);
            if (modX < intervalX && modZ < intervalZ) {
                int dx = modX * 16 + localX - centerX;
                int dz = modZ * 16 + localZ - centerZ;
                if (dx >= -1 && dx <= 1 && dz >= -1 && dz <= 1) return centerColor;
            }
        }
        return mainColor;
    }

    private int gapColor(int chunkX, int chunkZ, int localX, int localZ, int worldX, int worldZ, boolean isGapX,
                         boolean isGapZ, int periodX, int periodZ, int gapWidth, int intervalX, int intervalZ,
                         int gapA, int gapB, int gapC) {
        int gapWidthBlocks = gapWidth * 16;
        if (config.getGapPreset() == GapPreset.ROAD) {
            if (isGapX && isGapZ) {
                if (gapB != 0) {
                    int offsetX = (mod(chunkX, periodX) - intervalX) * 16 + localX;
                    int offsetZ = (mod(chunkZ, periodZ) - intervalZ) * 16 + localZ;
                    boolean edgeX = offsetX == 0 || offsetX == gapWidthBlocks - 1;
                    boolean edgeZ = offsetZ == 0 || offsetZ == gapWidthBlocks - 1;
                    if (edgeX && edgeZ) return gapB;
                }
            } else if (isGapX) {
                return roadColor((mod(chunkX, periodX) - intervalX) * 16 + localX, worldZ, gapWidthBlocks, gapA, gapB,
                        gapC);
            } else {
                return roadColor((mod(chunkZ, periodZ) - intervalZ) * 16 + localZ, worldX, gapWidthBlocks, gapA, gapB,
                        gapC);
            }
        }
        return gapA;
    }

    private static int roadColor(int offset, int along, int width, int gapA, int gapB, int gapC) {
        if (gapB != 0 && (offset == 0 || offset == width - 1)) return gapB;
        if (gapC != 0 && width >= 4) {
            int center = width / 2;
            if ((offset == center || offset == center - 1) && mod(along + 2, 8) < 4) return gapC;
        }
        return gapA;
    }

    private int topLayerColor() {
        List<PersonalSpaceSettings.Layer> layers = config.getLayers();
        if (layers.isEmpty()) return 0xFF202020;
        int color = blockColor(layers.get(layers.size() - 1).block());
        return color != 0 ? color : 0xFF202020;
    }

    /** Average color of the block's top texture, tinted like grass; 0 for none or air. */
    private static int blockColor(String id) {
        Block block = PersonalSpaceBlocks.block(id);
        if (block == null || block == Blocks.AIR) return 0;
        return COLOR_CACHE.computeIfAbsent(block, WPreviewPanel::computeColor);
    }

    private static int computeColor(Block block) {
        Minecraft mc = Minecraft.getInstance();
        BlockState state = block.defaultBlockState();
        try {
            var model = mc.getBlockRenderer().getBlockModel(state);
            List<BakedQuad> quads = model.getQuads(state, Direction.UP, RandomSource.create(42L));
            TextureAtlasSprite sprite = quads.isEmpty() ? model.getParticleIcon() : quads.get(0).getSprite();
            int tint = quads.isEmpty() || !quads.get(0).isTinted() ? -1 :
                    mc.getBlockColors().getColor(state, null, null, quads.get(0).getTintIndex());
            NativeImage image = sprite.contents().getOriginalImage();
            double r = 0, g = 0, b = 0, weight = 0;
            int size = Math.min(sprite.contents().width(), sprite.contents().height());
            for (int y = 0; y < size; y++) {
                for (int x = 0; x < sprite.contents().width(); x++) {
                    int abgr = image.getPixelRGBA(x, y);
                    if ((abgr >>> 24) < 128) continue;
                    int pr = abgr & 255, pg = abgr >> 8 & 255, pb = abgr >> 16 & 255;
                    double w = 0.1 + 0.9 * (pr + pg + pb) / (3.0 * 255.0);
                    r += pr * w;
                    g += pg * w;
                    b += pb * w;
                    weight += w;
                }
            }
            if (weight > 0) {
                int ar = Math.min(255, (int) (r / weight));
                int ag = Math.min(255, (int) (g / weight));
                int ab = Math.min(255, (int) (b / weight));
                if (tint != -1) {
                    ar = ar * (tint >> 16 & 255) / 255;
                    ag = ag * (tint >> 8 & 255) / 255;
                    ab = ab * (tint & 255) / 255;
                }
                return 0xFF000000 | ar << 16 | ag << 8 | ab;
            }
        } catch (RuntimeException ignored) {
            // fall back to the map color below
        }
        return 0xFF000000 | state.getMapColor(null, null).col;
    }

    private static int mod(int a, int b) {
        if (b <= 0) return 0;
        int m = a % b;
        return m < 0 ? m + b : m;
    }
}
