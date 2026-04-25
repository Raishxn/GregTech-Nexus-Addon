package com.raishxn.gtna.common.data.multiblock;

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public final class GTNAMultiBlockFileReader {

    private GTNAMultiBlockFileReader() {}

    public static FactoryBlockPattern start(MultiblockMachineDefinition definition, String name) {
        String[][] aisles = loadAisles(name);
        FactoryBlockPattern pattern = FactoryBlockPattern.start();
        for (String[] aisle : aisles) {
            pattern.aisle(aisle);
        }
        return pattern;
    }

    public static FactoryBlockPattern start(MultiblockMachineDefinition definition, String name,
                                            RelativeDirection... directions) {
        String[][] aisles = loadAisles(name);
        FactoryBlockPattern pattern = directions.length == 3
                ? FactoryBlockPattern.start(directions[0], directions[1], directions[2])
                : FactoryBlockPattern.start();
        for (String[] aisle : aisles) {
            pattern.aisle(aisle);
        }
        return pattern;
    }

    private static String[][] loadAisles(String name) {
        String resource = "pattern/" + name + ".mbs";
        try (InputStream stream = GTNAMultiBlockFileReader.class.getClassLoader().getResourceAsStream(resource)) {
            if (stream == null) {
                throw new IllegalStateException("Missing multiblock pattern resource: " + resource);
            }
            try (DataInputStream input = new DataInputStream(stream)) {
                int outer = input.readInt();
                String[][] aisles = new String[outer][];
                for (int i = 0; i < outer; i++) {
                    int inner = input.readInt();
                    aisles[i] = new String[inner];
                    for (int j = 0; j < inner; j++) {
                        int length = input.readInt();
                        StringBuilder row = new StringBuilder(length);
                        for (int index = 0; index < length; index++) {
                            row.append(decodeSymbol(input.readUnsignedByte()));
                        }
                        aisles[i][j] = row.toString();
                    }
                }
                return aisles;
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load multiblock pattern: " + resource, e);
        }
    }

    private static char decodeSymbol(int symbol) {
        return switch (symbol) {
            case 0 -> 'C';
            case 1 -> 'B';
            case 2 -> 'A';
            case 3 -> 'J';
            case 4 -> 'G';
            case 5 -> 'F';
            case 6 -> 'E';
            case 7 -> 'D';
            case 8 -> 'K';
            case 9 -> 'L';
            case 10 -> 'I';
            case 11 -> 'H';
            case 12 -> 'O';
            case 13 -> 'N';
            case 14 -> 'M';
            case 15 -> 'P';
            case 16 -> 'S';
            case 61 -> '~';
            case 99 -> ' ';
            default -> throw new IllegalArgumentException("Unknown multiblock pattern symbol: " + symbol);
        };
    }
}
