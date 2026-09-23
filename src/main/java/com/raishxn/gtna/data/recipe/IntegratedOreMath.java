package com.raishxn.gtna.data.recipe;

/**
 * Pure math for the GTLCore-parity Integrated Ore Processing recipes, split out so the numbers can
 * be locked by a unit test without booting Minecraft (the test source set has no GTCEu classes).
 *
 * <p>
 * Source: GTLCore {@code OreRecipeHandlerMixin} ({@code gtlcore$processOre} / {@code
 * gtlcore$processRawOre}), LGPLv3, with attribution. The constants below are copied verbatim:
 * each circuit's duration is {@code 26 + <stage cost> * crushedAmount} ticks and every recipe runs at
 * 30 EU/t.
 */
public final class IntegratedOreMath {

    private IntegratedOreMath() {}

    /** Crushed-ore units fed into the raw-ore chain: {@code (oreMultiplier * mult / 2) * 2}. */
    public static int rawCrushedAmount(int oreMultiplier, int integratedOreMultiplier) {
        return (oreMultiplier * integratedOreMultiplier / 2) * 2;
    }

    /** Crushed-ore units fed into the stone-ore chain: {@code oreMultiplier * 2 * mult}. */
    public static int stoneCrushedAmount(int oreMultiplier, int integratedOreMultiplier) {
        return oreMultiplier * 2 * integratedOreMultiplier;
    }

    /** Distilled-water / washing-fluid amount for the wash/bath circuits: {@code 100 * crushedAmount}. */
    public static int washFluidAmount(int crushedAmount) {
        long amount = 100L * crushedAmount;
        return (int) Math.min(Integer.MAX_VALUE, amount);
    }

    /**
     * Recipe duration in ticks for a circuit, from GTLCore's per-stage cost table. Circuit 1 uses the
     * material mass (macerate + centrifuge), circuits 2/5 are the "thermal centrifuge" branch,
     * circuits 3/6 the "grinding + centrifuge" branch and circuits 4/7 the "sifting" branch.
     */
    public static int duration(int circuit, long mass, int crushedAmount) {
        long base = switch (circuit) {
            case 1 -> 26L + (26L + mass * 4L) * crushedAmount;
            case 2, 5 -> 26L + (200L + 200L + 26L) * crushedAmount;
            case 3, 6 -> 26L + (200L + 26L + 16L) * crushedAmount;
            case 4, 7 -> 26L + (200L + 210L + 16L) * crushedAmount;
            default -> 26L;
        };
        return (int) Math.min(Integer.MAX_VALUE, base);
    }
}
