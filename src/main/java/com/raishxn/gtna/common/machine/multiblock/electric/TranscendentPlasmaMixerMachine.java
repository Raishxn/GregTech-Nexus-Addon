package com.raishxn.gtna.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.capability.IParallelHatch;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;

import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

import com.raishxn.gtna.api.capability.WirelessEnergyManager;
import com.raishxn.gtna.utils.datastructure.Int128;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.UUID;

/**
 * GTNH {@code MTETranscendentPlasmaMixer}: power comes only from the owner's Nexus wireless network, paid in full when
 * a recipe starts — {@code EU multiplier (10, or the recipe's) × EU/t × duration} per parallel. Parallels come from a
 * Parallel Hatch (GTNH sets them in the controller) and are capped by the inputs and the network balance. No
 * overclocking.
 */
public class TranscendentPlasmaMixerMachine extends WorkableElectricMultiblockMachine {

    public static final String EU_MULTIPLIER = "eu_multiplier";
    /** GTNH recipe EU/t, kept as data: the mixer never draws per-tick EU, so recipes carry no EU content. */
    public static final String EUT = "tpm_eut";
    private static final String COST = "gtna_wireless_cost";

    public TranscendentPlasmaMixerMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    private int parallelLimit() {
        int limit = 1;
        for (IMultiPart part : getParts()) {
            if (part instanceof IParallelHatch hatch) limit = Math.max(limit, hatch.getCurrentParallel());
        }
        return limit;
    }

    public static BigInteger costPerRecipe(GTRecipe recipe) {
        long multiplier = recipe.data.contains(EU_MULTIPLIER) ? recipe.data.getLong(EU_MULTIPLIER) : 10;
        long eut = recipe.data.getLong(EUT);
        return BigInteger.valueOf(multiplier).multiply(BigInteger.valueOf(eut))
                .multiply(BigInteger.valueOf(recipe.duration));
    }

    public static @NotNull ModifierFunction wirelessParallel(@NotNull MetaMachine machine, @NotNull GTRecipe recipe) {
        if (!(machine instanceof TranscendentPlasmaMixerMachine mixer) ||
                !(mixer.getLevel() instanceof ServerLevel level))
            return ModifierFunction.NULL;
        BigInteger perRecipe = costPerRecipe(recipe);
        BigInteger balance = WirelessEnergyManager.getEnergy(level, mixer.getOwnerUUID()).toBigInteger();
        if (perRecipe.signum() <= 0 || balance.compareTo(perRecipe) < 0) {
            return ModifierFunction.cancel(Component.translatable("gtna.machine.transcendent_plasma_mixer.no_energy",
                    perRecipe.toString()));
        }
        int affordable = balance.divide(perRecipe).min(BigInteger.valueOf(Integer.MAX_VALUE)).intValue();
        int parallel = ParallelLogic.getParallelAmountWithoutEU(machine, recipe,
                Math.min(mixer.parallelLimit(), affordable));
        if (parallel < 1) return ModifierFunction.NULL;
        BigInteger cost = perRecipe.multiply(BigInteger.valueOf(parallel));
        return input -> {
            var scaled = parallel > 1 ? ModifierFunction.builder()
                    .modifyAllContents(ContentModifier.multiplier(parallel)).parallels(parallel).build()
                    .apply(input) : input.copy();
            if (scaled == null) return null;
            scaled.inputs.remove(EURecipeCapability.CAP);
            scaled.tickInputs.remove(EURecipeCapability.CAP);
            scaled.data.putString(COST, cost.toString());
            return scaled;
        };
    }

    /** The whole cost is debited once, as GTNH's onRecipeStart does. */
    @Override
    public boolean beforeWorking(GTRecipe recipe) {
        if (!super.beforeWorking(recipe)) return false;
        if (recipe == null || !recipe.data.contains(COST) || !(getLevel() instanceof ServerLevel level)) return true;
        UUID owner = getOwnerUUID();
        var cost = new BigInteger(recipe.data.getString(COST));
        return WirelessEnergyManager.consumeDirectEnergy(level, owner, Int128.fromBigInteger(cost),
                GlobalPos.of(level.dimension(), getPos()), getBlockState().getBlock().getDescriptionId());
    }
}
