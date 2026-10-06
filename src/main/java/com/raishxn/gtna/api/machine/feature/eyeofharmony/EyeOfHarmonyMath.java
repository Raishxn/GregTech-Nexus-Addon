package com.raishxn.gtna.api.machine.feature.eyeofharmony;

import java.math.BigInteger;
import java.util.Objects;

/**
 * Serial operation rules adapted from GTNH GT5-Unofficial a3e1e11241a814c9fa0dd0973d5699548428f689,
 * MTEEyeOfHarmony and EyeOfHarmonyRecipe/Storage (LGPL-3.0; see THIRD_PARTY_NOTICES.md).
 * No world, inventories, energy account or random generator is touched while making a plan.
 * Tiers are zero based. Parallel Astral Array rules are intentionally separate.
 */
public final class EyeOfHarmonyMath {

    private EyeOfHarmonyMath() {}

    /** Plasma cost must come from the audited output catalog, not a guessed constant. */
    public record Program(int rocketTier, int requiredCompression, long baseTicks, long hydrogen,
                          long helium, long startupEU, long returnEU, double baseChance) {

        public Program {
            if (rocketTier < 0 || rocketTier > 9 || requiredCompression < 0 || requiredCompression > 8 ||
                    baseTicks <= 0 || hydrogen <= 0 || helium <= 0 || startupEU <= 0 || returnEU < 0 ||
                    !Double.isFinite(baseChance) || baseChance < 0 || baseChance > 1) {
                throw new IllegalArgumentException("Invalid planetary program");
            }
        }

        public static Program overworld(long plasmaEU) {
            if (plasmaEU < 0) throw new IllegalArgumentException("Negative plasma cost");
            long seconds = 18_000;
            long ticks = seconds * 20;
            long cost = Math.addExact(plasmaEU, Math.multiplyExact(ticks, (1L << 19) + 100_000_000_000L));
            // Preserve the upstream floating point multiplication followed by long truncation.
            return new Program(0, 0, ticks, 1_000_000_000L, 1_000_000_000L, cost, (long) (cost * 0.6), 1);
        }
    }

    public record Fields(int compression, int acceleration, int stabilisation) {

        public Fields {
            if (compression < 0 || compression > 8 || acceleration < 0 || acceleration > 8 ||
                    stabilisation < 0 || stabilisation > 8) {
                throw new IllegalArgumentException("Unformed or invalid fields");
            }
        }
    }

    /** The GTNH success sentinel is Double.MIN_VALUE; fresh machines start with both values zero. */
    public record History(double lastChance, double pity) {

        public History {
            if (!Double.isFinite(lastChance) || lastChance < 0 || lastChance > 1 ||
                    !Double.isFinite(pity)) {
                throw new IllegalArgumentException("Invalid chance history");
            }
        }

        public static History fresh() {
            return new History(0, 0);
        }
    }

    /** Values are frozen before debit; consume ALL buffered gas once when committing this plan. */
    public record Plan(Program program, Fields fields, int circuit, long bufferedHydrogen,
                       long bufferedHelium, int durationTicks, BigInteger debitEU, BigInteger creditEU,
                       double hydrogenPenalty, double heliumPenalty, double chance, double yield,
                       History historyAtStart) {

        public BigInteger netEU() {
            return creditEU.subtract(debitEU);
        }

        public long outputAmount(long baseAmount) {
            if (baseAmount < 0) throw new IllegalArgumentException("Negative output");
            // Mirrors GTNH's compound long *= double. Apply only to successful output.
            return (long) (baseAmount * yield);
        }
    }

    /** Credit EU is owed on failure too. Store the resolved result so reload never rolls again. */
    public record Result(boolean success, long failedSpaceTime, BigInteger creditEU, History history) {}

    public static Plan plan(Program program, Fields fields, int circuitSetting, long hydrogen, long helium,
                            History history) {
        Objects.requireNonNull(program);
        Objects.requireNonNull(fields);
        Objects.requireNonNull(history);
        if (fields.compression < program.requiredCompression) {
            throw new IllegalArgumentException("Insufficient compression");
        }
        if (hydrogen < program.hydrogen || helium < program.helium) {
            throw new IllegalArgumentException("Insufficient gas");
        }
        int circuit = Math.max(0, Math.min(24, circuitSetting));
        double hydrogenPenalty = overflowPenalty(hydrogen, program.hydrogen);
        double heliumPenalty = overflowPenalty(helium, program.helium);
        double baseChance = program.baseChance - fields.acceleration * 0.0925 + fields.stabilisation * 0.05;
        double pity = history.pity == Double.MIN_VALUE ? baseChance : history.pity;
        // Upstream compares BEFORE applying overflow and uses exact double equality.
        double chance = baseChance == history.lastChance && pity >= 1 ? 1 : baseChance;
        chance = clamp(chance - hydrogenPenalty - heliumPenalty);
        double yield = clamp(1 - fields.stabilisation * 0.05 - hydrogenPenalty - heliumPenalty);
        int duration = (int) Math.max(program.baseTicks * Math.pow(2, -fields.acceleration) *
                compressionDiscount(fields.compression - program.requiredCompression) * Math.pow(2, -circuit), 1);
        BigInteger debit = startupDebit(program, circuit);
        BigInteger credit = BigInteger.valueOf((long) (program.returnEU * (1 - (8 - fields.stabilisation) * 0.05)));
        return new Plan(program, fields, circuit, hydrogen, helium, duration, debit, credit,
                hydrogenPenalty, heliumPenalty, chance, yield, new History(history.lastChance, pity));
    }

    /** Shared by controller quotes and execution so balance changes cannot desynchronize the UI. */
    public static BigInteger startupDebit(Program program, int circuitSetting) {
        int circuit = Math.max(0, Math.min(24, circuitSetting));
        // GTNA balance: quadratic startup cost keeps circuit 24 reachable with endgame generation.
        long multiplier = (long) (circuit + 1) * (circuit + 1);
        return BigInteger.valueOf(program.startupEU).multiply(BigInteger.valueOf(multiplier));
    }

    public static Result resolve(Plan plan, int roll) {
        Objects.requireNonNull(plan);
        if (roll < 0 || roll >= 10_000) throw new IllegalArgumentException("Roll must be in [0, 10000)");
        boolean success = roll < (int) (10_000 * plan.chance);
        double pity = success ? Double.MIN_VALUE :
                plan.historyAtStart.lastChance == plan.chance ?
                        plan.historyAtStart.pity + (1 - plan.chance) * plan.chance : plan.chance;
        long failedSpaceTime = success ? 0 :
                (long) (plan.chance * 14_400L * Math.pow(2, plan.program.rocketTier + 1));
        return new Result(success, failedSpaceTime, plan.creditEU, new History(plan.chance, pity));
    }

    public static double overflowPenalty(long stored, long required) {
        if (required <= 0 || stored < required) throw new IllegalArgumentException("Insufficient gas");
        double excess = (double) stored / required - 1;
        double scaled = 30 * excess;
        return 1 - Math.exp(-(scaled * scaled));
    }

    private static double compressionDiscount(int exponent) {
        // Same multiplication order as GTUtility.powInt, preserving duration truncation boundaries.
        double base = 0.97;
        double result = 1;
        while (exponent > 0) {
            if ((exponent & 1) == 1) result *= base;
            base *= base;
            exponent >>= 1;
        }
        return result;
    }

    private static double clamp(double value) {
        return Math.max(0, Math.min(1, value));
    }
}
