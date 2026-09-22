package com.raishxn.gtna.common.machine.multiblock.steam;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;

import org.jetbrains.annotations.NotNull;

/**
 * GTNL {@code SteamCactusWonder} port (LGPLv3, original by ScienceNotLeisure).
 *
 * <p>
 * GTNL implements this machine with a bespoke {@code onPostTick} fuel accumulator backed by a fake
 * recipe map that only exists for JEI. GTNA has no GT++ cactus charcoal/coke ladder, so this port
 * keeps the same behaviour through a real recipe type ({@code CACTUS_WONDER_RECIPES}) whose
 * recipes turn GTNA's closest carbon fuels into the three steam grades (see
 * {@code GTNAMachineRecipes}). That keeps the machine JEI-visible and independently testable while
 * preserving "burn a carbon fuel, get steam" and the 20-tick cadence of the GTNL fake recipes.
 */
public class SteamCactusWonder extends WorkableMultiblockMachine implements IDisplayUIMachine {

    public SteamCactusWonder(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    protected @NotNull RecipeLogic createRecipeLogic(Object... args) {
        return new RecipeLogic(this);
    }
}
