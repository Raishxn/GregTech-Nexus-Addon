package com.raishxn.gtna.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.utils.GTMath;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.network.chat.Component;

import com.google.common.math.IntMath;

import java.math.RoundingMode;
import java.util.function.IntSupplier;

/**
 * GTCEu's heating-coil overclock, ported because GTCEu 7.5.3 keeps the pieces package-private
 * ({@code OverclockingLogic.heatingCoilOC}, {@code getCoilEUtDiscount} and the OC-parameter
 * computation of {@code OverclockingLogic.getModifier}). GTOCore's
 * {@code GTORecipeModifiers.UPGRADE_EBF_OVERCLOCK}, used by the Vacuum Drying Furnace's main mode,
 * is exactly this behavior:
 *
 * <ul>
 * <li>the machine must provide at least the recipe's {@code ebf_temp};</li>
 * <li>every 900K the coil sits above the recipe temperature grants one EU/t discount step
 * ({@code 0.95^n}, only for recipes at or above 900K);</li>
 * <li>half of those steps are "perfect" overclocks: a step multiplies EUt by 4 and divides the
 * duration by 4 instead of 2.</li>
 * </ul>
 *
 * <p>
 * Once every overclock step can be perfect, the result is identical to
 * {@link OverclockingLogic#PERFECT_OVERCLOCK} plus the EU discount, which is why a hot coil makes
 * the drying furnace much faster. {@code OCParams}/{@code OCResult} are public nested records of the
 * {@link OverclockingLogic} interface, so the port only re-implements the two math helpers.
 */
public final class GTNAHeatingCoilOverclock implements OverclockingLogic {

    private static final int COIL_EUT_DISCOUNT_TEMPERATURE = 900;

    /** The formed machine's heating-coil temperature, read every time the logic runs. */
    private final IntSupplier machineTemperature;
    /** Captured from the candidate recipe before the overclock math runs. */
    private int recipeTemperature;

    public GTNAHeatingCoilOverclock(IntSupplier machineTemperature) {
        this.machineTemperature = machineTemperature;
    }

    /**
     * Copy of GTCEu's {@code OverclockingLogic.getModifier(machine, recipe, maxVoltage, true)}: it
     * derives the OC amount from the recipe's EU tier against the machine's overclock voltage and
     * sizes the optional sub-tick parallels like the original.
     */
    @Override
    public ModifierFunction getModifier(MetaMachine machine, GTRecipe recipe, long maxVoltage) {
        this.recipeTemperature = recipe.data.getInt("ebf_temp");
        int machineTemp = machineTemperature.getAsInt();
        if (recipeTemperature > machineTemp) {
            return ModifierFunction.cancel(
                    Component.translatable("gtceu.recipe_modifier.coil_temperature_too_low"));
        }

        long EUt = RecipeHelper.getRealEUt(recipe).getTotalEU();
        if (EUt == 0) return ModifierFunction.IDENTITY;

        int recipeTier = GTUtil.getTierByVoltage(EUt);
        int maximumTier = GTUtil.getOCTierByVoltage(maxVoltage);
        int OCs = maximumTier - recipeTier;
        if (recipeTier == GTValues.ULV) OCs--;
        if (OCs <= 0) return ModifierFunction.IDENTITY;

        int maxParallels;
        int lg = IntMath.log2(recipe.duration, RoundingMode.FLOOR) / 2;
        if (lg > OCs) {
            maxParallels = 16;
        } else {
            maxParallels = ParallelLogic.getParallelAmount(machine, recipe,
                    GTMath.saturatedCast((1L << (2 * (OCs - lg))) + 1));
        }

        OCResult result = runOverclockingLogic(new OCParams(EUt, recipe.duration, OCs, maxParallels), maxVoltage);
        return result.toModifier().compose(eutDiscount());
    }

    /** Verbatim port of GTCEu's {@code OverclockingLogic.heatingCoilOC}. */
    @Override
    public OCResult runOverclockingLogic(OCParams params, long maxVoltage) {
        int machineTemp = machineTemperature.getAsInt();
        int perfectOCAmount = coilDiscountAmount(recipeTemperature, machineTemp) / 2;
        double duration = params.duration();
        double eut = params.eut();
        int ocAmount = params.ocAmount();
        int maxParallels = params.maxParallels();

        double parallel = 1;
        boolean shouldParallel = false;
        int ocLevel = 0;
        double durationMultiplier = 1;

        while (ocAmount-- > 0) {
            // Do perfects first if possible
            boolean perfect = perfectOCAmount-- > 0;

            // Check if EUt can be multiplied again without going over the max
            double potentialEUt = eut * STD_VOLTAGE_FACTOR;
            if (potentialEUt > maxVoltage) break;

            // If we're already doing parallels or our duration would go below 1, try parallels
            double dFactor = (perfect ? PERFECT_DURATION_FACTOR : STD_DURATION_FACTOR);
            if (shouldParallel || duration * dFactor < 1) {
                // Check if parallels can be multiplied without going over the maximum
                double pFactor = perfect ? PERFECT_DURATION_FACTOR_INV : STD_DURATION_FACTOR_INV;
                double potentialParallel = parallel * pFactor;
                if (potentialParallel > maxParallels) break;
                parallel = potentialParallel;
                shouldParallel = true;
            } else {
                duration *= dFactor;
                durationMultiplier *= dFactor;
            }

            // Only set EUt after checking parallels - no need to OC if parallels would be too high
            eut = potentialEUt;
            ocLevel++;
        }

        return new OCResult(Math.pow(STD_VOLTAGE_FACTOR, ocLevel), durationMultiplier, ocLevel, (int) parallel);
    }

    /** GTCEu's {@code getCoilEUtDiscount}: {@code 0.95^((machineTemp - recipeTemp) / 900)}. */
    private ModifierFunction eutDiscount() {
        int amount = coilDiscountAmount(recipeTemperature, machineTemperature.getAsInt());
        if (recipeTemperature < COIL_EUT_DISCOUNT_TEMPERATURE || amount < 1) {
            return ModifierFunction.IDENTITY;
        }
        return ModifierFunction.builder()
                .eutMultiplier(Math.min(1, Math.pow(0.95, amount)))
                .build();
    }

    /** GTCEu's {@code getCoilDiscountAmount}, the floor of the 900K steps above the recipe. */
    private static int coilDiscountAmount(int recipeTemp, int machineTemp) {
        return Math.max(0, (machineTemp - recipeTemp) / COIL_EUT_DISCOUNT_TEMPERATURE);
    }
}
