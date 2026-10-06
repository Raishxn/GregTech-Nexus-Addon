package com.raishxn.gtna;

import com.raishxn.gtna.common.world.personalspace.PersonalSpaceBlocks;

import java.util.List;
import java.util.Objects;

/** 1.7.10 PersonalSpace names and meta ranges map to the intended modern blocks without a registry. */
public final class PersonalSpaceLegacyNamesTest {

    private PersonalSpaceLegacyNamesTest() {}

    public static void main(String[] args) {
        expect("minecraft:wool:14", "minecraft:red_wool");
        expect("minecraft:wool:4", "minecraft:yellow_wool");
        expect("minecraft:wool", "minecraft:white_wool");
        expect("minecraft:grass", "minecraft:grass_block");
        expect("minecraft:double_stone_slab", "minecraft:smooth_stone");
        expect("minecraft:stone", "minecraft:stone");
        expect("minecraft:stone:0", "minecraft:stone");
        expect("minecraft:stained_hardened_clay:15", "minecraft:black_terracotta");
        expect("minecraft:stone:3", null);
        expect("", "");
        expect("gtceu:steel_block", "gtceu:steel_block");

        check(PersonalSpaceBlocks.metaValues("0-15").size() == 16, "0-15 has 16 values");
        check(PersonalSpaceBlocks.metaValues("0-15,!3").equals(List.of(0, 1, 2, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13,
                14, 15)), "negated meta is removed");
        check(PersonalSpaceBlocks.metaValues("5").equals(List.of(5)), "single meta");
        check(PersonalSpaceBlocks.metaValues("").equals(List.of(0)), "empty spec defaults to meta 0");
        check(PersonalSpaceBlocks.metaValues("x").isEmpty(), "invalid spec yields nothing");
    }

    private static void expect(String legacy, String modern) {
        String actual = PersonalSpaceBlocks.normalize(legacy);
        if (!Objects.equals(actual, modern)) {
            throw new AssertionError(legacy + ": expected " + modern + ", got " + actual);
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
