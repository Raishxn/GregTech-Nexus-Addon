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
 * GTNL {@code MegaSteamCompressor} port (LGPLv3, original by ScienceNotLeisure).
 *
 * <p>
 * GTNL's "Steam Supercompressor": a 256-parallel compressor, itself crafted inside the
 * Steam Manufacturer from 64 large steam compressors. GTNL applies a per-overclock EUt discount
 * and duration modifier; GTNA has no per-machine overclock-count hook, so this port keeps the 256
 * parallels and the 0.5x duration convention used by GTNA's other large steam machines, and lets
 * the base apply the usual 2x high-pressure bonus.
 */
public class MegaSteamCompressor extends SteamMultiMachineBase {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            MegaSteamCompressor.class, SteamMultiMachineBase.MANAGED_FIELD_HOLDER);

    public MegaSteamCompressor(IMachineBlockEntity holder, Object... args) {
        super(holder, false, args);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Nullable
    public static ModifierFunction recipeModifier(MetaMachine machine, @Nonnull GTRecipe recipe) {
        if (!(machine instanceof MegaSteamCompressor)) {
            return ModifierFunction.NULL;
        }
        if (recipe.getType() != GTRecipeTypes.COMPRESSOR_RECIPES) {
            return ModifierFunction.NULL;
        }
        int parallels = ParallelLogic.getParallelAmount(machine, recipe, 256);
        if (parallels == 0) {
            return ModifierFunction.NULL;
        }
        return ModifierFunction.builder()
                .modifyAllContents(ContentModifier.multiplier(parallels))
                .durationMultiplier(0.5)
                .parallels(parallels)
                .build();
    }
}
