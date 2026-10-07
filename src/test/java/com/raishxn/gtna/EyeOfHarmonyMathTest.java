package com.raishxn.gtna;

import com.raishxn.gtna.api.machine.feature.eyeofharmony.EyeOfHarmonyMath;
import com.raishxn.gtna.api.machine.feature.eyeofharmony.EyeOfHarmonyMath.Fields;
import com.raishxn.gtna.api.machine.feature.eyeofharmony.EyeOfHarmonyMath.History;
import com.raishxn.gtna.api.machine.feature.eyeofharmony.EyeOfHarmonyMath.Plan;
import com.raishxn.gtna.api.machine.feature.eyeofharmony.EyeOfHarmonyMath.Program;

import java.math.BigInteger;

public final class EyeOfHarmonyMathTest {

    private static final long GAS = 1_000_000_000L;

    private EyeOfHarmonyMathTest() {}

    public static void main(String[] args) {
        Program program = Program.overworld(0);
        check(program.startupEU() == 36_000_188_743_680_000L, "VM3 and baseline energy");
        check(program.returnEU() == 21_600_113_246_208_000L, "Overworld efficiency");
        Plan baseline = plan(program, new Fields(0, 0, 0), 0, GAS, GAS, History.fresh());
        check(baseline.durationTicks() == 360_000, "five-hour base duration");
        close(1, baseline.chance(), "base chance");
        close(1, baseline.yield(), "base yield");
        check(baseline.creditEU().equals(BigInteger.valueOf(12_960_067_947_724_800L)), "tier-zero EU penalty");
        check(baseline.netEU().signum() < 0, "Overworld net energy is negative");
        check(EyeOfHarmonyMath.resolve(baseline, 9999).success(), "100 percent succeeds at largest roll");

        for (int circuit = 0; circuit <= 24; circuit++) {
            int expected = Math.max(360_000 >> circuit, 1);
            check(plan(program, new Fields(0, 0, 0), circuit, GAS, GAS, History.fresh()).debitEU()
                    .equals(baseline.debitEU().shiftLeft(2 * circuit)),
                    "GTNH 4^k startup debit for every circuit: " + circuit);
            check(plan(program, new Fields(0, 0, 0), circuit, GAS, GAS, History.fresh()).durationTicks() == expected,
                    "every circuit halves time down to the one-tick floor: " + circuit);
        }
        int previousCompression = 360_001;
        for (int tier = 0; tier <= 8; tier++) {
            int compression = plan(program, new Fields(tier, 0, 0), 0, GAS, GAS, History.fresh()).durationTicks();
            check(compression < previousCompression, "each compression tier shortens time: " + tier);
            previousCompression = compression;
            check(plan(program, new Fields(0, tier, 0), 0, GAS, GAS, History.fresh()).durationTicks() ==
                    360_000 / (1 << tier), "each acceleration tier halves time: " + tier);
            check(plan(program, new Fields(0, 0, tier), 0, GAS, GAS, History.fresh()).durationTicks() == 360_000,
                    "stabilisation affects chance/yield, not duration: " + tier);
        }
        Plan accelerated = plan(program, new Fields(1, 2, 3), 4, GAS, GAS, History.fresh());
        check(accelerated.durationTicks() == 5456, "compression, acceleration and circuit duration");
        check(accelerated.debitEU().equals(baseline.debitEU().shiftLeft(8)),
                "GTNH 4^k circuit debit");
        close(0.965, accelerated.chance(), "fields act independently");
        close(0.85, accelerated.yield(), "stabilisation trades yield for chance");
        check(accelerated.outputAmount(1152) == 979, "fractional output truncates");
        check(accelerated.outputAmount(100_000) == 85_000, "raw star matter yield");

        double onePercent = 1 - Math.exp(-0.09);
        Plan excess = plan(program, new Fields(0, 0, 0), 0, GAS + GAS / 100, GAS + GAS / 100, History.fresh());
        close(onePercent, excess.hydrogenPenalty(), "one percent overflow");
        close(1 - 2 * onePercent, excess.chance(), "both gases penalize chance");
        close(excess.chance(), excess.yield(), "both gases penalize yield");
        check(excess.bufferedHydrogen() == 1_010_000_000L, "plan freezes entire gas buffer");
        var failed = EyeOfHarmonyMath.resolve(excess, 9999);
        check(!failed.success(), "deterministic failure");
        check(failed.failedSpaceTime() == 23842, "failure spacetime follows actual chance");
        check(failed.creditEU().equals(excess.creditEU()), "EU credited even after failure");
        close(excess.chance(), failed.history().pity(), "new chance resets pity");
        var repeated = plan(program, new Fields(0, 0, 0), 0, GAS + GAS / 100, GAS + GAS / 100, failed.history());
        var failedAgain = EyeOfHarmonyMath.resolve(repeated, 9999);
        close(excess.chance() + (1 - excess.chance()) * excess.chance(), failedAgain.history().pity(),
                "same chance builds pity");

        Program half = new Program(0, 0, 360_000, GAS, GAS, 1000, 600, 0.5);
        Plan pity = plan(half, new Fields(0, 0, 0), 0, GAS, GAS, new History(0.5, 1));
        close(1, pity.chance(), "pity compares base chance");
        Plan overflowingPity = plan(half, new Fields(0, 0, 0), 0, GAS + GAS / 100, GAS, new History(0.5, 1));
        close(1 - onePercent, overflowingPity.chance(), "overflow applies after pity guarantee");
        Plan noGuarantee = plan(half, new Fields(0, 0, 0), 0, GAS, GAS, new History(0.4, 2));
        close(0.5, noGuarantee.chance(), "different base chance does not guarantee success");
        var success = EyeOfHarmonyMath.resolve(noGuarantee, 4999);
        check(success.success() && success.history().pity() == Double.MIN_VALUE, "success sets reset sentinel");
        check(!EyeOfHarmonyMath.resolve(noGuarantee, 5000).success(), "roll threshold is exclusive");
        Plan reset = plan(half, new Fields(0, 0, 0), 0, GAS, GAS, success.history());
        close(0.5, reset.historyAtStart().pity(), "next start initializes sentinel");

        Plan saturated = plan(program, new Fields(8, 8, 8), 24, GAS, GAS, History.fresh());
        check(saturated.durationTicks() == 1, "duration floor");
        check(saturated.debitEU().compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0, "cost exceeds long safely");
        check(saturated.debitEU().bitLength() < 127, "Overworld max cost fits Nexus Int128");
        check(saturated.creditEU().equals(BigInteger.valueOf(program.returnEU())),
                "best stabilisation has no EU penalty");
        check(plan(program, new Fields(0, 0, 0), -10, GAS, GAS, History.fresh()).circuit() == 0, "lower circuit clamp");
        check(plan(program, new Fields(0, 0, 0), 30, GAS, GAS, History.fresh()).circuit() == 24, "upper circuit clamp");
        Plan tooMuch = plan(program, new Fields(0, 0, 0), 0, Long.MAX_VALUE, Long.MAX_VALUE, History.fresh());
        close(0, tooMuch.chance(), "excess chance floor");
        close(0, tooMuch.yield(), "excess yield floor");
        check(!EyeOfHarmonyMath.resolve(tooMuch, 0).success(), "zero chance always fails");
        check(EyeOfHarmonyMath.resolve(tooMuch, 0).failedSpaceTime() == 0, "zero chance yields zero failure fluid");
        Program high = new Program(9, 8, 360_000, GAS, GAS, 1000, 600, 0.55);
        Plan negativeBase = plan(high, new Fields(8, 8, 0), 0, GAS, GAS, new History(1, Double.MIN_VALUE));
        check(negativeBase.historyAtStart().pity() < 0, "upstream permits negative unclamped initial pity");
        close(0, negativeBase.chance(), "negative base chance is clamped only after pity");

        invalid(() -> new Fields(-1, 0, 0));
        invalid(() -> new Fields(0, 9, 0));
        invalid(() -> plan(program, new Fields(0, 0, 0), 0, GAS - 1, GAS, History.fresh()));
        invalid(() -> plan(high, new Fields(7, 0, 0), 0, GAS, GAS, History.fresh()));
        invalid(() -> EyeOfHarmonyMath.resolve(baseline, 10_000));
        invalid(() -> new History(Double.NaN, 0));
        invalid(() -> Program.overworld(-1));
        try {
            Program.overworld(Long.MAX_VALUE);
            throw new AssertionError("Overflow must be rejected");
        } catch (ArithmeticException expected) {}
        // Planning is referentially transparent: no history mutation or implicit random draw.
        check(excess.equals(plan(program, new Fields(0, 0, 0), 0, GAS + GAS / 100, GAS + GAS / 100, History.fresh())),
                "repeated preview produces identical plan");
        check(failed.equals(EyeOfHarmonyMath.resolve(excess, 9999)), "saved roll resolves identically");
        System.out.println("[EyeOfHarmonyMathTest] all cases passed");
    }

    private static Plan plan(Program program, Fields fields, int circuit, long hydrogen, long helium, History history) {
        return EyeOfHarmonyMath.plan(program, fields, circuit, hydrogen, helium, history);
    }

    private static void close(double expected, double actual, String message) {
        check(Math.abs(expected - actual) < 1e-12, message + " expected " + expected + " got " + actual);
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static void invalid(Runnable operation) {
        try {
            operation.run();
            throw new AssertionError("Invalid input must be rejected");
        } catch (IllegalArgumentException expected) {}
    }
}
