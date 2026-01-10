package com.raishxn.gtna.utils;

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;

import java.util.HashMap;
import java.util.Map;

public class ThreadMultiplierStrategy {

    private static final Map<MultiblockMachineDefinition, Integer> BLOCK_MULTIPLIER_MAP = new HashMap<>();

    private static final int DEFAULT_MULTIPLIER = 1;

    public static void register(MultiblockMachineDefinition definition, int multiplier) {
        if (definition != null) {
            BLOCK_MULTIPLIER_MAP.put(definition, multiplier);
        }
    }
    public static int getAdditionalMultiplier(MultiblockMachineDefinition definition) {
        return BLOCK_MULTIPLIER_MAP.getOrDefault(definition, DEFAULT_MULTIPLIER);
    }
}