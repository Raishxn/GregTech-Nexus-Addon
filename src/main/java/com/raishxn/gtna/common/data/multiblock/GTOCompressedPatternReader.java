package com.raishxn.gtna.common.data.multiblock;

import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

/** Reads the compressed GTOCore MBS format, including its recorded orientation. */
public final class GTOCompressedPatternReader {

    private GTOCompressedPatternReader() {}

    public static FactoryBlockPattern start(String name) {
        PatternData data = read(name);
        FactoryBlockPattern pattern = FactoryBlockPattern.start(data.chars(), data.rows(), data.aisles());
        for (String[] aisle : data.slices()) pattern.aisle(aisle);
        return pattern;
    }

    public static PatternData read(String name) {
        String resource = "pattern/gto/" + name + ".mbs";
        try (InputStream stream = GTOCompressedPatternReader.class.getClassLoader().getResourceAsStream(resource)) {
            if (stream == null) throw new IllegalStateException("Missing GTOCore pattern: " + resource);
            try (DataInputStream input = new DataInputStream(new GZIPInputStream(stream))) {
                readVarInt(input); // serialized anchor offset, -1 in GTOCore patterns
                int version = readVarInt(input);
                if (version != 1) throw new IOException("Unsupported GTOCore MBS version " + version);
                RelativeDirection[] values = RelativeDirection.VALUES;
                RelativeDirection chars = values[readDirection(input, values.length)];
                RelativeDirection rows = values[readDirection(input, values.length)];
                RelativeDirection aisles = values[readDirection(input, values.length)];
                int depth = readSize(input);
                String[][] slices = new String[depth][];
                for (int aisle = 0; aisle < depth; aisle++) {
                    int height = readSize(input);
                    String[] slice = new String[height];
                    for (int row = 0; row < height; row++) {
                        slice[row] = input.readUTF();
                    }
                    slices[aisle] = slice;
                }
                return new PatternData(chars, rows, aisles, slices);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read GTOCore pattern: " + resource, e);
        }
    }

    public record PatternData(RelativeDirection chars, RelativeDirection rows, RelativeDirection aisles,
                              String[][] slices) {}

    private static int readDirection(DataInputStream input, int count) throws IOException {
        int direction = readVarInt(input);
        if (direction < 0 || direction >= count) throw new IOException("Invalid MBS direction " + direction);
        return direction;
    }

    private static int readSize(DataInputStream input) throws IOException {
        int size = readVarInt(input);
        if (size <= 0 || size > 64) throw new IOException("Invalid MBS dimension " + size);
        return size;
    }

    private static int readVarInt(DataInputStream input) throws IOException {
        int result = 0;
        for (int shift = 0; shift < 35; shift += 7) {
            int value = input.readUnsignedByte();
            result |= (value & 0x7f) << shift;
            if ((value & 0x80) == 0) return result;
        }
        throw new IOException("MBS VarInt exceeds five bytes");
    }
}
