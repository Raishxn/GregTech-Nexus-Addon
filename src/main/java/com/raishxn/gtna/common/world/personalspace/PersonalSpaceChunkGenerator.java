package com.raishxn.gtna.common.world.personalspace;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.raishxn.gtna.common.world.personalspace.PersonalSpaceSettings.CenterDirection;
import com.raishxn.gtna.common.world.personalspace.PersonalSpaceSettings.GapPreset;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Forge 1.20.1 port of PersonalSpace's {@code PersonalChunkProvider}. It reads the current settings of its dimension,
 * so a worldgen change unlocked by an operator affects only chunks generated afterwards, like the original.
 */
public final class PersonalSpaceChunkGenerator extends ChunkGenerator {

    /**
     * Infiniverse stores runtime dimensions in level.dat with this codec, so vanilla recreates them at startup. The
     * fields are optional to keep older local worlds loadable; the overworld directory provides the real settings
     * when the level loads.
     */
    public static final Codec<PersonalSpaceChunkGenerator> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(BiomeSource.CODEC.fieldOf("biome_source").forGetter(g -> g.biomeSource),
                    Codec.INT.optionalFieldOf("dimension_id", 0).forGetter(g -> g.dimensionId),
                    CompoundTag.CODEC.optionalFieldOf("settings", new CompoundTag())
                            .forGetter(g -> g.compiled.settings.save()))
            .apply(instance, (source, id, tag) -> new PersonalSpaceChunkGenerator(source, id,
                    tag.isEmpty() ? PersonalSpaceSettings.voidWorld() : PersonalSpaceSettings.load(tag))));

    public static final int WORLD_HEIGHT = 256;
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final BlockState PLATFORM = Blocks.SMOOTH_STONE_SLAB.defaultBlockState()
            .setValue(SlabBlock.TYPE, SlabType.DOUBLE);

    private final int dimensionId;
    private volatile Compiled compiled;

    public PersonalSpaceChunkGenerator(BiomeSource biomeSource, int dimensionId, PersonalSpaceSettings settings) {
        super(biomeSource);
        this.dimensionId = dimensionId;
        this.compiled = new Compiled(settings.copy(), null);
    }

    public int dimensionId() {
        return dimensionId;
    }

    public PersonalSpaceSettings settings() {
        return compiled.settings;
    }

    /** Applies new settings to chunks generated from now on; existing chunks are never rewritten. */
    public void updateSettings(PersonalSpaceSettings settings, RegistryAccess registries) {
        compiled = new Compiled(settings.copy(), biome(registries, settings.getBiomeId()));
    }

    private static Holder<Biome> biome(RegistryAccess registries, String id) {
        if (registries == null) return null;
        ResourceLocation location = ResourceLocation.tryParse(id);
        var registry = registries.registryOrThrow(Registries.BIOME);
        if (location != null) {
            var holder = registry.getHolder(ResourceKey.create(Registries.BIOME, location));
            if (holder.isPresent()) return holder.get();
        }
        return registry.getHolder(ResourceKey.create(Registries.BIOME,
                new ResourceLocation(PersonalSpaceSettings.DEFAULT_BIOME))).orElse(null);
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return PersonalSpaceChunkGenerators.CODEC.get();
    }

    /** Pre-resolved block states of one settings version. */
    private static final class Compiled {

        final PersonalSpaceSettings settings;
        final Holder<Biome> biome;
        final BlockState[] column = new BlockState[WORLD_HEIGHT];
        final int stackHeight;
        final int groundLevel;
        final List<Integer> surfaceLevels;
        final BlockState boundaryA, boundaryB, gapA, gapB, gapC, center;

        Compiled(PersonalSpaceSettings settings, Holder<Biome> biome) {
            this.settings = settings;
            this.biome = biome;
            int y = 0;
            boolean[] solid = new boolean[WORLD_HEIGHT];
            java.util.Arrays.fill(column, AIR);
            for (PersonalSpaceSettings.Layer layer : settings.getLayers()) {
                BlockState state = PersonalSpaceBlocks.solidState(layer.block());
                for (int end = y + layer.count(); y < end && y < WORLD_HEIGHT; y++) {
                    if (state != null) {
                        column[y] = state;
                        solid[y] = true;
                    }
                }
                if (y >= WORLD_HEIGHT) break;
            }
            this.stackHeight = y;
            this.groundLevel = settings.getGroundLevel();
            int top = groundLevel - 1;
            if (top < 0 || top >= WORLD_HEIGHT) {
                surfaceLevels = List.of();
            } else if (settings.isApplyToAllSurfaceLayers()) {
                List<Integer> levels = new ArrayList<>();
                for (int level = 0; level < WORLD_HEIGHT; level++) {
                    if (solid[level] && (level + 1 >= WORLD_HEIGHT || !solid[level + 1])) levels.add(level);
                }
                surfaceLevels = List.copyOf(levels);
            } else {
                surfaceLevels = List.of(top);
            }
            boundaryA = PersonalSpaceBlocks.solidState(settings.getBoundaryBlockA());
            boundaryB = PersonalSpaceBlocks.solidState(settings.getBoundaryBlockB());
            gapA = PersonalSpaceBlocks.solidState(settings.getGapBlockA());
            gapB = PersonalSpaceBlocks.solidState(settings.getGapBlockB());
            gapC = PersonalSpaceBlocks.solidState(settings.getGapBlockC());
            center = PersonalSpaceBlocks.solidState(settings.getCenterBlock());
        }
    }

    @Override
    public CompletableFuture<ChunkAccess> createBiomes(Executor executor, RandomState randomState, Blender blender,
                                                       StructureManager structures, ChunkAccess chunk) {
        Holder<Biome> fixed = compiled.biome;
        if (fixed == null) return super.createBiomes(executor, randomState, blender, structures, chunk);
        return CompletableFuture.supplyAsync(() -> {
            chunk.fillBiomesFromNoise((x, y, z, sampler) -> fixed, randomState.sampler());
            return chunk;
        }, executor);
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, Blender blender, RandomState randomState,
                                                        StructureManager structures, ChunkAccess chunk) {
        Compiled current = compiled;
        int startX = chunk.getPos().getMinBlockX();
        int startZ = chunk.getPos().getMinBlockZ();
        Heightmap ocean = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap surface = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        BlockState[] column = new BlockState[WORLD_HEIGHT];
        for (int z = 0; z < 16; z++) {
            for (int x = 0; x < 16; x++) {
                fillColumn(current, startX + x, startZ + z, column);
                for (int y = 0; y < WORLD_HEIGHT; y++) {
                    BlockState state = column[y];
                    if (state.isAir()) continue;
                    chunk.setBlockState(pos.set(startX + x, y, startZ + z), state, false);
                    ocean.update(x, y, z, state);
                    surface.update(x, y, z, state);
                }
            }
        }
        return CompletableFuture.completedFuture(chunk);
    }

    /** Block at a world position for the current settings, used by tests and the noise column. */
    public BlockState stateAt(int x, int y, int z) {
        if (y < 0 || y >= WORLD_HEIGHT) return AIR;
        BlockState[] column = new BlockState[WORLD_HEIGHT];
        fillColumn(compiled, x, z, column);
        return column[y];
    }

    /** One column: layers, the decoration on every surface level, and the spawn platform. */
    private static void fillColumn(Compiled c, int x, int z, BlockState[] column) {
        System.arraycopy(c.column, 0, column, 0, WORLD_HEIGHT);
        BlockState decoration = c.surfaceLevels.isEmpty() ? null : decorationAt(c, x, z);
        if (decoration != null) for (int level : c.surfaceLevels) column[level] = decoration;
        int localX = Math.floorMod(x, 16);
        int localZ = Math.floorMod(z, 16);
        if (Math.floorDiv(x, 16) == 0 && Math.floorDiv(z, 16) == 0 && localX >= 6 && localX < 11 && localZ >= 6 &&
                localZ < 11) {
            column[c.groundLevel] = PLATFORM;
        }
    }

    private static int mod(int a, int b) {
        int m = a % b;
        return m < 0 ? m + b : m;
    }

    /**
     * Boundary, gap or center block drawn at (x, z) on the surface levels, or {@code null} to keep the layer. Same
     * rules and precedence as the original chunk provider (gap, then boundary stripes, then center marker).
     */
    private static BlockState decorationAt(Compiled c, int x, int z) {
        PersonalSpaceSettings cfg = c.settings;
        int chunkX = Math.floorDiv(x, 16);
        int chunkZ = Math.floorDiv(z, 16);
        int localX = Math.floorMod(x, 16);
        int localZ = Math.floorMod(z, 16);
        int intervalX = cfg.getBoundaryChunkIntervalX();
        int intervalZ = cfg.getBoundaryChunkIntervalZ();
        int gapWidth = cfg.getGapWidth();
        int periodX = intervalX + gapWidth;
        int periodZ = intervalZ + gapWidth;
        boolean isGapX = gapWidth > 0 && intervalX > 0 && mod(chunkX, periodX) >= intervalX;
        boolean isGapZ = gapWidth > 0 && intervalZ > 0 && mod(chunkZ, periodZ) >= intervalZ;
        if (isGapX || isGapZ) {
            if (c.gapA == null) return null;
            if (c.settings.getGapPreset() != GapPreset.ROAD) return c.gapA;
            int gapWidthBlocks = gapWidth * 16;
            if (isGapX && isGapZ) {
                if (c.gapB != null) {
                    int offsetX = (mod(chunkX, periodX) - intervalX) * 16 + localX;
                    int offsetZ = (mod(chunkZ, periodZ) - intervalZ) * 16 + localZ;
                    boolean onEdgeX = offsetX == 0 || offsetX == gapWidthBlocks - 1;
                    boolean onEdgeZ = offsetZ == 0 || offsetZ == gapWidthBlocks - 1;
                    if (onEdgeX && onEdgeZ) return c.gapB;
                }
                return c.gapA;
            }
            if (isGapX) return roadBlock(c, (mod(chunkX, periodX) - intervalX) * 16 + localX, z, gapWidthBlocks);
            return roadBlock(c, (mod(chunkZ, periodZ) - intervalZ) * 16 + localZ, x, gapWidthBlocks);
        }

        BlockState result = null;
        boolean isBoundaryX, prevBoundaryX, isBoundaryZ, prevBoundaryZ;
        if (gapWidth > 0) {
            isBoundaryX = intervalX > 0 && mod(chunkX, periodX) == 0;
            prevBoundaryX = intervalX > 0 && mod(chunkX, periodX) == intervalX - 1;
            isBoundaryZ = intervalZ > 0 && mod(chunkZ, periodZ) == 0;
            prevBoundaryZ = intervalZ > 0 && mod(chunkZ, periodZ) == intervalZ - 1;
        } else {
            isBoundaryX = intervalX > 0 && mod(chunkX, intervalX) == 0;
            isBoundaryZ = intervalZ > 0 && mod(chunkZ, intervalZ) == 0;
            prevBoundaryX = intervalX > 0 && mod(chunkX + 1, intervalX) == 0;
            prevBoundaryZ = intervalZ > 0 && mod(chunkZ + 1, intervalZ) == 0;
        }
        if ((c.boundaryA != null || c.boundaryB != null) &&
                (isBoundaryX && localX == 0 || prevBoundaryX && localX == 15 || isBoundaryZ && localZ == 0 ||
                        prevBoundaryZ && localZ == 15)) {
            boolean useA = ((x + z) & 1) == 0;
            result = useA ? (c.boundaryA != null ? c.boundaryA : c.boundaryB) :
                    (c.boundaryB != null ? c.boundaryB : c.boundaryA);
        }

        if (cfg.isCenterEnabled() && intervalX > 0 && intervalZ > 0 && c.center != null) {
            CenterDirection dir = cfg.getCenterDirection();
            int centerX = intervalX * 8 + (dir == CenterDirection.SW || dir == CenterDirection.NW ? -1 : 0);
            int centerZ = intervalZ * 8 + (dir == CenterDirection.NE || dir == CenterDirection.NW ? -1 : 0);
            int modX = mod(chunkX, periodX);
            int modZ = mod(chunkZ, periodZ);
            if (modX < intervalX && modZ < intervalZ && modX * 16 + localX == centerX &&
                    modZ * 16 + localZ == centerZ) {
                result = c.center;
            }
        }
        return result;
    }

    private static BlockState roadBlock(Compiled c, int offsetInGap, int alongRoad, int gapWidthBlocks) {
        if (c.gapB != null && (offsetInGap == 0 || offsetInGap == gapWidthBlocks - 1)) return c.gapB;
        if (c.gapC != null && gapWidthBlocks >= 4) {
            int center = gapWidthBlocks / 2;
            if ((offsetInGap == center || offsetInGap == center - 1) && mod(alongRoad + 2, 8) < 4) return c.gapC;
        }
        return c.gapA;
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor level, RandomState randomState) {
        BlockState[] column = new BlockState[WORLD_HEIGHT];
        fillColumn(compiled, x, z, column);
        for (int y = WORLD_HEIGHT - 1; y >= 0; y--) {
            if (type.isOpaque().test(column[y])) return y + 1;
        }
        return 0;
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor level, RandomState randomState) {
        BlockState[] column = new BlockState[WORLD_HEIGHT];
        fillColumn(compiled, x, z, column);
        return new NoiseColumn(0, column);
    }

    @Override
    public void buildSurface(WorldGenRegion region, StructureManager structures, RandomState randomState,
                             ChunkAccess chunk) {}

    @Override
    public void applyCarvers(WorldGenRegion region, long seed, RandomState randomState, BiomeManager biomes,
                             StructureManager structures, ChunkAccess chunk, GenerationStep.Carving step) {}

    /** Foliage runs the biome decoration; trees add one plains tree per chunk, as in the original populate(). */
    @Override
    public void applyBiomeDecoration(WorldGenLevel level, ChunkAccess chunk, StructureManager structures) {
        PersonalSpaceSettings settings = compiled.settings;
        if (settings.isGeneratingVegetation()) super.applyBiomeDecoration(level, chunk, structures);
        if (!settings.isGeneratingTrees()) return;
        int chunkX = chunk.getPos().x;
        int chunkZ = chunk.getPos().z;
        long seed = level.getSeed();
        java.util.Random seeded = new java.util.Random(seed);
        long i1 = seeded.nextLong() / 2L * 2L + 1L;
        long j1 = seeded.nextLong() / 2L * 2L + 1L;
        RandomSource random = RandomSource.create((long) chunkX * i1 + (long) chunkZ * j1 ^ seed);
        int x = chunkX * 16 + random.nextInt(16) + 8;
        int z = chunkZ * 16 + random.nextInt(16) + 8;
        int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
        ResourceKey<ConfiguredFeature<?, ?>> tree = random.nextInt(10) == 0 ? TreeFeatures.FANCY_OAK :
                TreeFeatures.OAK;
        level.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE).getHolder(tree)
                .ifPresent(feature -> feature.value().place(level, this, random, new BlockPos(x, y, z)));
    }

    @Override
    public void createStructures(RegistryAccess registries, ChunkGeneratorStructureState state,
                                 StructureManager structures, ChunkAccess chunk,
                                 StructureTemplateManager templates) {}

    @Override
    public void createReferences(WorldGenLevel level, StructureManager structures, ChunkAccess chunk) {}

    @Override
    public void spawnOriginalMobs(WorldGenRegion region) {}

    /** The original provider returns no possible creatures. */
    @Override
    public WeightedRandomList<MobSpawnSettings.SpawnerData> getMobsAt(Holder<Biome> biome,
                                                                      StructureManager structures,
                                                                      MobCategory category, BlockPos pos) {
        return WeightedRandomList.create();
    }

    @Override
    public int getGenDepth() {
        return WORLD_HEIGHT;
    }

    @Override
    public int getSeaLevel() {
        return 63;
    }

    @Override
    public int getMinY() {
        return 0;
    }

    @Override
    public void addDebugScreenInfo(List<String> lines, RandomState randomState, BlockPos pos) {
        lines.add("PersonalSpace DIM" + dimensionId);
    }
}
