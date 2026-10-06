package com.raishxn.gtna.common.world.personalspace;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.Heightmap;

import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.mixin.personalspace.LevelDataAccessor;
import com.raishxn.gtna.mixin.personalspace.ServerLevelDataAccessor;
import commoble.infiniverse.api.InfiniverseAPI;

/** Creates, reloads and reconfigures one dimension per PersonalSpace ID. */
public final class PersonalSpaceWorlds {

    public static final ResourceKey<DimensionType> DIMENSION_TYPE = ResourceKey.create(Registries.DIMENSION_TYPE,
            GTNACORE.id("personal_space"));
    private static final String PATH_PREFIX = "personal_space/pdim_";

    private PersonalSpaceWorlds() {}

    public static ResourceKey<Level> key(int id) {
        if (id < 1) throw new IllegalArgumentException("Invalid PersonalSpace ID " + id);
        return ResourceKey.create(Registries.DIMENSION, GTNACORE.id(PATH_PREFIX + id));
    }

    /** @return the PersonalSpace ID of a dimension, or 0 when it is not a personal dimension. */
    public static int id(ResourceKey<Level> key) {
        return key == null ? 0 : id(key.location());
    }

    public static int id(ResourceLocation location) {
        if (location == null || !location.getNamespace().equals(GTNACORE.MOD_ID) ||
                !location.getPath().startsWith(PATH_PREFIX)) {
            return 0;
        }
        try {
            return Math.max(0, Integer.parseInt(location.getPath().substring(PATH_PREFIX.length())));
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    public static int create(MinecraftServer server, PersonalSpaceSettings settings) {
        if (!server.isSameThread()) throw new IllegalStateException("PersonalSpace creation must run on server thread");
        PersonalSpaceDirectory directory = PersonalSpaceDirectory.get(server);
        int id = directory.nextId();
        if (server.getLevel(key(id)) != null) throw new IllegalStateException("PersonalSpace key in use: " + id);
        directory.add(id, settings);
        if (load(server, id) == null) throw new IllegalStateException("Failed to create PersonalSpace " + id);
        return id;
    }

    public static ServerLevel load(MinecraftServer server, int id) {
        if (!server.isSameThread()) throw new IllegalStateException("PersonalSpace load must run on server thread");
        PersonalSpaceSettings settings = PersonalSpaceDirectory.get(server).settings(id);
        if (settings == null) return null;
        ResourceKey<Level> key = key(id);
        ServerLevel level = server.getLevel(key);
        if (level == null) level = InfiniverseAPI.get().getOrCreateLevel(server, key, () -> stem(server, id, settings));
        if (level != null) setUp(level);
        return level;
    }

    /**
     * Gives a loaded personal level its current settings and its own time/weather data. Runs from
     * {@code LevelEvent.Load}, both for levels vanilla recreates from level.dat and for new Infiniverse levels.
     */
    public static void setUp(ServerLevel level) {
        int id = id(level.dimension());
        if (id <= 0 || level.getLevelData() instanceof PersonalSpaceLevelData) return;
        MinecraftServer server = level.getServer();
        PersonalSpaceDirectory directory = PersonalSpaceDirectory.get(server);
        PersonalSpaceSettings settings = directory.settings(id);
        if (settings == null) {
            GTNACORE.LOGGER.warn("PersonalSpace dimension {} has no saved settings", id);
            return;
        }
        if (level.getChunkSource().getGenerator() instanceof PersonalSpaceChunkGenerator generator) {
            generator.updateSettings(settings, server.registryAccess());
        }
        var data = new PersonalSpaceLevelData(server.getWorldData(), server.getWorldData().overworldData(), settings,
                directory::setDirty);
        ((ServerLevelDataAccessor) level).gtna$setServerLevelData(data);
        ((LevelDataAccessor) level).gtna$setLevelData(data);
        if (!settings.isWeatherEnabled()) {
            level.setRainLevel(0.0F);
            level.setThunderLevel(0.0F);
        }
        if (PersonalSpaceConfig.debugLogging()) GTNACORE.LOGGER.info("Set up PersonalSpace dimension {}", id);
    }

    /** Called after a settings change: new chunks use it, the client receives it. */
    public static void settingsChanged(MinecraftServer server, int id) {
        PersonalSpaceDirectory directory = PersonalSpaceDirectory.get(server);
        directory.setDirty();
        PersonalSpaceSettings settings = directory.settings(id);
        ServerLevel level = server.getLevel(key(id));
        if (settings != null && level != null &&
                level.getChunkSource().getGenerator() instanceof PersonalSpaceChunkGenerator generator) {
            generator.updateSettings(settings, server.registryAccess());
        }
    }

    private static LevelStem stem(MinecraftServer server, int id, PersonalSpaceSettings settings) {
        var biomes = server.registryAccess().registryOrThrow(Registries.BIOME);
        ResourceLocation biomeId = ResourceLocation.tryParse(settings.getBiomeId());
        Holder<Biome> biome = biomeId == null ? null :
                biomes.getHolder(ResourceKey.create(Registries.BIOME, biomeId)).orElse(null);
        if (biome == null) {
            biome = biomes.getHolderOrThrow(ResourceKey.create(Registries.BIOME,
                    new ResourceLocation(PersonalSpaceSettings.DEFAULT_BIOME)));
        }
        var generator = new PersonalSpaceChunkGenerator(new FixedBiomeSource(biome), id, settings);
        Holder<DimensionType> type = server.registryAccess().registryOrThrow(Registries.DIMENSION_TYPE)
                .getHolder(DIMENSION_TYPE).map(holder -> (Holder<DimensionType>) holder)
                .orElseGet(() -> server.overworld().dimensionTypeRegistration());
        return new LevelStem(type, generator);
    }

    /** Spawn point of the original provider: (8, ground + 2, 8). */
    public static BlockPos spawnPoint(MinecraftServer server, int id) {
        PersonalSpaceSettings settings = PersonalSpaceDirectory.get(server).settings(id);
        int ground = settings == null ? 128 : settings.getGroundLevel();
        return new BlockPos(8, ground + 2, 8);
    }

    /** Spawn of a level with the top block recomputed, like {@code /pspace tpx} and {@code give-portal}. */
    public static BlockPos topSpawn(ServerLevel level) {
        int id = id(level.dimension());
        BlockPos spawn = id > 0 ? spawnPoint(level.getServer(), id) : level.getSharedSpawnPos();
        int top = level.getHeight(Heightmap.Types.MOTION_BLOCKING, spawn.getX(), spawn.getZ());
        return new BlockPos(spawn.getX(), Math.max(level.getMinBuildHeight(), top), spawn.getZ());
    }

    public static ServerLevel level(MinecraftServer server, ResourceKey<Level> key) {
        int id = id(key);
        if (id > 0) return load(server, id);
        return server.getLevel(key);
    }

    public static void teleport(ServerPlayer player, ServerLevel level, double x, double y, double z, float yaw) {
        player.teleportTo(level, x, y, z, yaw, 0.0F);
        player.setDeltaMovement(0, 0, 0);
    }
}
