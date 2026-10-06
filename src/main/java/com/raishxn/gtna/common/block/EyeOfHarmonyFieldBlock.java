package com.raishxn.gtna.common.block;

import net.minecraft.world.level.block.Block;

/** Physical GTNH field tier. Indices 0–8 are independent from displayed tiers 1–9. */
public class EyeOfHarmonyFieldBlock extends Block {

    public enum Family {

        COMPRESSION("compression", "Spacetime Compression"),
        ACCELERATION("acceleration", "Time Acceleration"),
        STABILISATION("stabilisation", "Stabilisation");

        public final String id;
        public final String englishName;

        Family(String id, String englishName) {
            this.id = id;
            this.englishName = englishName;
        }
    }

    private final Family family;
    private final int fieldTier;

    public EyeOfHarmonyFieldBlock(Properties properties, Family family, int fieldTier) {
        super(properties);
        if (fieldTier < 0 || fieldTier > 8) throw new IllegalArgumentException("Eye of Harmony field tier must be 0–8");
        this.family = family;
        this.fieldTier = fieldTier;
    }

    public Family getFamily() {
        return family;
    }

    public int getFieldTier() {
        return fieldTier;
    }
}
