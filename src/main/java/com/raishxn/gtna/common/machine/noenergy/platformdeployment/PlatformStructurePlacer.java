package com.raishxn.gtna.common.machine.noenergy.platformdeployment;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;

import it.unimi.dsi.fastutil.chars.Char2ReferenceOpenHashMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.IntConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class PlatformStructurePlacer {

    private final ServerLevel level;
    private final BlockPos protectedPos;
    private final BlockIterator iterator;
    private final int perTick;
    private final boolean breakBlocks;
    private final boolean skipAir;
    private final int blockFlags;
    private final IntConsumer onBatch;
    private final Runnable onFinished;

    private boolean finished;

    private PlatformStructurePlacer(ServerLevel level, BlockPos protectedPos, BlockIterator iterator, int perTick,
                                    boolean breakBlocks, boolean skipAir, boolean updateLight,
                                    IntConsumer onBatch, Runnable onFinished) {
        this.level = level;
        this.protectedPos = protectedPos.immutable();
        this.iterator = iterator;
        this.perTick = Math.max(1, perTick);
        this.breakBlocks = breakBlocks;
        this.skipAir = skipAir;
        this.blockFlags = updateLight ? 3 : 2;
        this.onBatch = onBatch;
        this.onFinished = onFinished;
    }

    static PlatformStructurePlacer create(ServerLevel level, BlockPos protectedPos, BlockPos startPos,
                                          PlatformBlockType.PlatformBlockStructure structure, int perTick,
                                          boolean breakBlocks, boolean skipAir, boolean updateLight,
                                          boolean xMirror, boolean zMirror, int rotation,
                                          IntConsumer onBatch, Runnable onFinished) throws IOException {
        Map<Character, BlockState> mapping = PlatformCreators.loadMappingFromJson(structure.blockMapping());
        String resourcePath = "assets/" + structure.resource().getNamespace() + "/" + structure.resource().getPath();
        InputStream input = PlatformStructurePlacer.class.getClassLoader().getResourceAsStream(resourcePath);
        if (input == null) {
            throw new IOException("Missing structure resource: " + resourcePath);
        }
        return new PlatformStructurePlacer(
                level,
                protectedPos,
                new BlockIterator(input, startPos, mapping, structure, xMirror, zMirror, rotation),
                perTick,
                breakBlocks,
                skipAir,
                updateLight,
                onBatch,
                onFinished);
    }

    static List<BlockPos> collectPreviewPositions(BlockPos startPos,
                                                  PlatformBlockType.PlatformBlockStructure structure,
                                                  boolean skipAir,
                                                  boolean xMirror,
                                                  boolean zMirror,
                                                  int rotation,
                                                  int maxBlocks) throws IOException {
        Map<Character, BlockState> mapping = PlatformCreators.loadMappingFromJson(structure.blockMapping());
        String resourcePath = "assets/" + structure.resource().getNamespace() + "/" + structure.resource().getPath();
        InputStream input = PlatformStructurePlacer.class.getClassLoader().getResourceAsStream(resourcePath);
        if (input == null) {
            throw new IOException("Missing structure resource: " + resourcePath);
        }
        try (input) {
            BlockIterator iterator = new BlockIterator(input, startPos, mapping, structure, xMirror, zMirror, rotation);
            int stride = Math.max(1, (structure.xSize() * structure.ySize() * structure.zSize()) / Math.max(1, maxBlocks));
            List<BlockPos> positions = new ArrayList<>(Math.min(maxBlocks, 1024));
            int index = 0;
            while (iterator.hasNext() && positions.size() < maxBlocks) {
                BlockIterator.Entry entry = iterator.next();
                if (skipAir && entry.state().isAir()) {
                    index++;
                    continue;
                }
                if (index % stride == 0) {
                    positions.add(entry.pos().immutable());
                }
                index++;
            }
            return positions.isEmpty() ? Collections.emptyList() : positions;
        }
    }

    void tick() {
        if (finished) {
            return;
        }

        int processed = 0;
        while (iterator.hasNext() && processed < perTick) {
            BlockIterator.Entry entry = iterator.next();
            BlockState targetState = entry.state();
            BlockPos pos = entry.pos();

            if (skipAir && targetState.isAir()) {
                continue;
            }
            if (pos.equals(protectedPos) || level.isOutsideBuildHeight(pos)) {
                continue;
            }

            BlockState oldState = level.getBlockState(pos);
            if (oldState.is(Blocks.BEDROCK)) {
                processed++;
                continue;
            }
            if (!breakBlocks && !oldState.isAir()) {
                processed++;
                continue;
            }
            if (oldState.equals(targetState)) {
                processed++;
                continue;
            }

            level.setBlock(pos, targetState, blockFlags);
            processed++;
        }

        if (onBatch != null) {
            onBatch.accept(iterator.getProgressPercentage());
        }

        if (!iterator.hasNext()) {
            finished = true;
            if (onFinished != null) {
                onFinished.run();
            }
        }
    }

    boolean isFinished() {
        return finished;
    }

    private static final class BlockIterator implements Iterator<BlockIterator.Entry> {

        private static final Pattern AISLE_PATTERN = Pattern.compile("\\.aisle\\(([^)]+)\\)");
        private static final Pattern STRING_PATTERN = Pattern.compile("\"([^\"]*)\"");

        private final BlockPos startPos;
        private final Char2ReferenceOpenHashMap<BlockState> blockMapping;
        private final List<String[]> aisles;
        private final int totalBlocks;
        private final int sizeX;
        private final int sizeY;
        private final int sizeZ;
        private final boolean xMirror;
        private final boolean zMirror;
        private final int rotation;

        private int x;
        private int y;
        private int z;
        private int emitted;

        private BlockIterator(InputStream input, BlockPos startPos, Map<Character, BlockState> blockMapping,
                              PlatformBlockType.PlatformBlockStructure structure,
                              boolean xMirror, boolean zMirror, int rotation) throws IOException {
            this.startPos = startPos.immutable();
            this.blockMapping = new Char2ReferenceOpenHashMap<>(blockMapping);
            this.aisles = readAisles(input);
            this.sizeX = structure.xSize();
            this.sizeY = structure.ySize();
            this.sizeZ = structure.zSize();
            this.totalBlocks = Math.max(1, sizeX * sizeY * sizeZ);
            this.xMirror = xMirror;
            this.zMirror = zMirror;
            this.rotation = rotation;
            if (aisles.size() != sizeZ) {
                throw new IOException("Structure aisle count mismatch for " + structure.name());
            }
        }

        private static List<String[]> readAisles(InputStream input) throws IOException {
            List<String[]> aisles = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!line.startsWith(".aisle(")) {
                        continue;
                    }
                    Matcher aisleMatcher = AISLE_PATTERN.matcher(line);
                    if (!aisleMatcher.find()) {
                        continue;
                    }
                    Matcher stringMatcher = STRING_PATTERN.matcher(aisleMatcher.group(1));
                    List<String> rows = new ArrayList<>();
                    while (stringMatcher.find()) {
                        rows.add(stringMatcher.group(1));
                    }
                    aisles.add(rows.toArray(String[]::new));
                }
            }
            return aisles;
        }

        @Override
        public boolean hasNext() {
            return z < sizeZ;
        }

        @Override
        public Entry next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            String[] aisle = aisles.get(z);
            String row = aisle[y];
            char symbol = x < row.length() ? row.charAt(x) : ' ';
            BlockState state = Objects.requireNonNullElse(blockMapping.get(symbol), Blocks.AIR.defaultBlockState());

            int[] transformed = transformCoords(x, y, z, sizeX, sizeZ, rotation, xMirror, zMirror);
            BlockPos pos = startPos.offset(transformed[0], transformed[1], transformed[2]);
            emitted++;

            x++;
            if (x >= sizeX) {
                x = 0;
                y++;
                if (y >= sizeY) {
                    y = 0;
                    z++;
                }
            }

            return new Entry(pos, transformState(state, rotation, xMirror, zMirror));
        }

        int getProgressPercentage() {
            return Math.min(100, (emitted * 100) / totalBlocks);
        }

        private static int[] transformCoords(int x, int y, int z, int sizeX, int sizeZ, int rotation,
                                             boolean xMirror, boolean zMirror) {
            int rx = x;
            int rz = z;
            int rotatedSizeX = sizeX;
            int rotatedSizeZ = sizeZ;

            switch (rotation) {
                case 90 -> {
                    int temp = rx;
                    rx = sizeZ - 1 - rz;
                    rz = temp;
                    rotatedSizeX = sizeZ;
                    rotatedSizeZ = sizeX;
                }
                case 180 -> {
                    rx = sizeX - 1 - rx;
                    rz = sizeZ - 1 - rz;
                }
                case 270 -> {
                    int temp = rx;
                    rx = rz;
                    rz = sizeX - 1 - temp;
                    rotatedSizeX = sizeZ;
                    rotatedSizeZ = sizeX;
                }
                default -> {}
            }

            if (xMirror) {
                rx = rotatedSizeX - 1 - rx;
            }
            if (zMirror) {
                rz = rotatedSizeZ - 1 - rz;
            }

            return new int[] { rx, y, rz };
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
            if (xMirror) {
                state = state.mirror(Mirror.FRONT_BACK);
            }
            if (zMirror) {
                state = state.mirror(Mirror.LEFT_RIGHT);
            }
            return state;
        }

        private record Entry(BlockPos pos, BlockState state) {}
    }
}
