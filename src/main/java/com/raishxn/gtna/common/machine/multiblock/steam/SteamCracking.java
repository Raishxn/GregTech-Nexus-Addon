package com.raishxn.gtna.common.machine.multiblock.steam;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import com.raishxn.gtna.common.machine.multiMachineBase.SteamMultiMachineBase;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

/**
 * GTNL {@code SteamCracking} port (LGPLv3, original by ScienceNotLeisure).
 *
 * <p>
 * Uses GTCEu's native {@code CRACKING_RECIPES} instead of GTNL's private {@code SteamCrackerRecipes}
 * map (both are the same sulfuric -> desulfurized hydrocarbon cracking recipes). GTNL scales the
 * parallel count with the casing tier (8 bronze / 16 steel); GTNA reads that off
 * {@link SteamMultiMachineBase#isHighPressure()}, and the base's high-pressure bonus supplies the
 * matching 2x speed of GTNL's {@code getDurationModifier() / tierMachine}.
 */
public class SteamCracking extends SteamMultiMachineBase {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            SteamCracking.class, SteamMultiMachineBase.MANAGED_FIELD_HOLDER);

    public SteamCracking(IMachineBlockEntity holder, Object... args) {
        super(holder, false, args);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Nullable
    public static ModifierFunction recipeModifier(MetaMachine machine, @Nonnull GTRecipe recipe) {
        if (!(machine instanceof SteamCracking steamMachine)) {
            return ModifierFunction.NULL;
        }
        if (recipe.getType() != GTRecipeTypes.CRACKING_RECIPES) {
            return ModifierFunction.NULL;
        }
        int maxParallel = steamMachine.isHighPressure() ? 16 : 8;
        int parallels = ParallelLogic.getParallelAmount(machine, recipe, maxParallel);
        if (parallels == 0) {
            return ModifierFunction.NULL;
        }
        return ModifierFunction.builder()
                .modifyAllContents(ContentModifier.multiplier(parallels))
                .parallels(parallels)
                .build();
    }
}
