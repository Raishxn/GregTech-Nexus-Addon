package com.raishxn.gtna.common.machine.noenergy.platformdeployment;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.chunk.LevelChunk;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.raishxn.gtna.GTNACORE;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

final class PlatformCreators {

    private static final Map<String, String> BLOCK_ID_ALIASES = Map.of(
            "gtocore:abs_black_casing", "gtna:abs_black_casing",
            "gtocore:abs_grey_casing", "gtna:abs_grey_casing",
            "gtocore:abs_light_grey_casing", "gtna:abs_light_grey_casing",
            "gtocore:restraint_device", "gtna:restraint_device");

    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type JSON_TYPE = new TypeToken<Map<String, JsonObject>>() {}.getType();
    private static volatile boolean exporting = false;

    private PlatformCreators() {}

    static void exportStructureAsync(ServerLevel level, BlockPos startPos, BlockPos endPos,
                                     boolean xMirror, boolean zMirror, int rotation) {
        if (exporting) return;
        exporting = true;
        try {
            exportStructure(level, startPos, endPos, xMirror, zMirror, rotation);
        } catch (Exception exception) {
            GTNACORE.LOGGER.error("Failed to export platform structure", exception);
        } finally {
            exporting = false;
        }
    }

    private static void exportStructure(ServerLevel level, BlockPos pos1, BlockPos pos2,
                                        boolean xMirror, boolean zMirror, int rotation) throws IOException {
        Path outputDir = Paths.get("logs", "platform");
        Files.createDirectories(outputDir);

        String timestamp = LocalDateTime.now().format(TIMESTAMP);
        Path structurePath = outputDir.resolve(timestamp);
        Path mappingPath = outputDir.resolve(timestamp + ".json");

        int minX = Math.min(pos1.getX(), pos2.getX());
        int minY = Math.min(pos1.getY(), pos2.getY());
        int minZ = Math.min(pos1.getZ(), pos2.getZ());
        int maxX = Math.max(pos1.getX(), pos2.getX());
        int maxY = Math.max(pos1.getY(), pos2.getY());
        int maxZ = Math.max(pos1.getZ(), pos2.getZ());

        int dx = maxX - minX + 1;
        int dy = maxY - minY + 1;
        int dz = maxZ - minZ + 1;

        if (rotation == 90 || rotation == 270) {
            int swap = dx;
            dx = dz;
            dz = swap;
        }

        Map<BlockState, Character> stateToChar = new LinkedHashMap<>();
        stateToChar.put(Blocks.AIR.defaultBlockState(), ' ');
        char next = 'A';
        MutableBlockPos mutablePos = new MutableBlockPos();
        Map<ChunkPos, LevelChunk> chunkCache = new HashMap<>();

        try (BufferedWriter writer = Files.newBufferedWriter(structurePath, StandardCharsets.UTF_8)) {
            writer.write(".size(" + dx + ", " + dy + ", " + dz + ")");
            writer.newLine();

            for (int outZ = 0; outZ < dz; outZ++) {
                String[] slices = new String[dy];
                for (int outY = 0; outY < dy; outY++) {
                    StringBuilder chars = new StringBuilder(dx);
                    for (int outX = 0; outX < dx; outX++) {
                        int[] transformed = transformCoords(outX, outZ, dx, dz, rotation, xMirror, zMirror);
                        int worldX = minX + transformed[0];
                        int worldY = minY + outY;
                        int worldZ = minZ + transformed[1];
                        mutablePos.set(worldX, worldY, worldZ);
                        BlockState state = transformState(getCachedBlockState(level, mutablePos, chunkCache), rotation, xMirror, zMirror);
                        Character mapped = stateToChar.get(state);
                        if (mapped == null) {
                            mapped = nextValidChar(next);
                            next = (char) (mapped + 1);
                            stateToChar.put(state, mapped);
                        }
                        chars.append(mapped);
                    }
                    slices[outY] = "\"" + chars + "\"";
                }
                writer.write(".aisle(" + String.join(", ", slices) + ")");
                writer.newLine();
            }
        }

        Map<Character, BlockState> charToState = new LinkedHashMap<>();
        stateToChar.forEach((state, ch) -> charToState.put(ch, state));
        saveMapping(charToState, mappingPath);
    }

    private static BlockState getCachedBlockState(ServerLevel level, BlockPos pos, Map<ChunkPos, LevelChunk> cache) {
        ChunkPos chunkPos = new ChunkPos(pos);
        LevelChunk chunk = cache.computeIfAbsent(chunkPos, ignored -> level.getChunk(chunkPos.x, chunkPos.z));
        return chunk.getBlockState(pos);
    }

    private static char nextValidChar(char start) {
        char current = start;
        while (current == '"' || current == '\\' || Character.isWhitespace(current) || Character.isISOControl(current)) {
            current++;
        }
        return current;
    }

    private static int[] transformCoords(int x, int z, int sizeX, int sizeZ, int rotation, boolean xMirror, boolean zMirror) {
        int rx = x;
        int rz = z;
        switch (rotation) {
            case 90 -> {
                int tmp = rx;
                rx = sizeZ - 1 - rz;
                rz = tmp;
            }
            case 180 -> {
                rx = sizeX - 1 - rx;
                rz = sizeZ - 1 - rz;
            }
            case 270 -> {
                int tmp = rx;
                rx = rz;
                rz = sizeX - 1 - tmp;
            }
            default -> {}
        }
        if (xMirror) rx = sizeX - 1 - rx;
        if (zMirror) rz = sizeZ - 1 - rz;
        return new int[] { rx, rz };
    }

    private static BlockState transformState(BlockState state, int rotation, boolean xMirror, boolean zMirror) {
        if (rotation != 0) {
            state = state.rotate(switch (rotation) {
                case 90 -> Rotation.CLOCKWISE_90;
                case 180 -> Rotation.CLOCKWISE_180;
                case 270 -> Rotation.COUNTERCLOCKWISE_90;
                default -> Rotation.NONE;
            });
        }
        if (xMirror) state = state.mirror(Mirror.FRONT_BACK);
        if (zMirror) state = state.mirror(Mirror.LEFT_RIGHT);
        return state;
    }

    static Map<Character, BlockState> loadMappingFromJson(ResourceLocation resource) throws IOException {
        String resourcePath = "assets/" + resource.getNamespace() + "/" + resource.getPath();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(PlatformCreators.class.getClassLoader().getResourceAsStream(resourcePath),
                        "Missing mapping: " + resourcePath),
                StandardCharsets.UTF_8))) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            Map<Character, BlockState> result = new LinkedHashMap<>();
            for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
                char key = entry.getKey().charAt(0);
                JsonObject blockJson = entry.getValue().getAsJsonObject();
                JsonElement blockIdElement = blockJson.has("block") ? blockJson.get("block") : blockJson.get("id");
                if (blockIdElement == null || blockIdElement.isJsonNull()) {
                    GTNACORE.LOGGER.warn("Invalid block mapping entry for symbol '{}' in '{}': missing block/id field",
                            key, resource);
                    result.put(key, Blocks.AIR.defaultBlockState());
                    continue;
                }
                ResourceLocation blockId = remapBlockId(PlatformSupport.parseId(blockIdElement.getAsString()));
                Block block = BuiltInRegistries.BLOCK.getOptional(blockId).orElse(null);
                if (block == null) {
                    GTNACORE.LOGGER.warn("Unknown block mapping '{}' in '{}'; substituting air for symbol '{}'",
                            blockId, resource, key);
                    result.put(key, Blocks.AIR.defaultBlockState());
                    continue;
                }
                BlockState state = block.defaultBlockState();
                if (blockJson.has("properties")) {
                    JsonObject properties = blockJson.getAsJsonObject("properties");
                    for (Map.Entry<String, JsonElement> propertyEntry : properties.entrySet()) {
                        Property<?> property = state.getBlock().getStateDefinition().getProperty(propertyEntry.getKey());
                        if (property != null) {
                            state = setProperty(state, property, propertyEntry.getValue().getAsString());
                        } else {
                            GTNACORE.LOGGER.warn("Unknown block property '{}' for '{}' in '{}'",
                                    propertyEntry.getKey(), blockId, resource);
                        }
                    }
                }
                result.put(key, state);
            }
            return result;
        }
    }

    private static ResourceLocation remapBlockId(ResourceLocation blockId) {
        String alias = BLOCK_ID_ALIASES.get(blockId.toString());
        if (alias != null) {
            return PlatformSupport.parseId(alias);
        }
        return blockId;
    }

    private static <T extends Comparable<T>> BlockState setProperty(BlockState state, Property<T> property, String value) {
        return property.getValue(value).map(v -> state.setValue(property, v)).orElse(state);
    }

    private static void saveMapping(Map<Character, BlockState> mapping, Path path) throws IOException {
        Map<String, JsonObject> data = new LinkedHashMap<>();
        for (Map.Entry<Character, BlockState> entry : mapping.entrySet()) {
            BlockState state = entry.getValue();
            JsonObject object = new JsonObject();
            object.addProperty("block", BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString());
            if (!state.getValues().isEmpty()) {
                JsonObject properties = new JsonObject();
                state.getValues().forEach((property, value) -> properties.addProperty(property.getName(), value.toString()));
                object.add("properties", properties);
            }
            data.put(String.valueOf(entry.getKey()), object);
        }
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            GSON.toJson(data, JSON_TYPE, writer);
        }
    }
}
