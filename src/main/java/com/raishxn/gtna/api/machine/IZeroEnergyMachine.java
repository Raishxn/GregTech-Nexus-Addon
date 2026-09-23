package com.raishxn.gtna.api.machine;

/**
 * Marks a multiple-recipes machine that runs its recipes without consuming EU (GTLsupb
 * {@code consumeEnergy = false}). {@code GTNAMultipleRecipesLogic} strips the energy contents from
 * the recipe and forces {@link #gtna$recipeDuration()} instead of applying the electric overclock.
 */
public interface IZeroEnergyMachine {

    /**
     * Fixed processing time in ticks (GTLsupb default is 1). Return a value {@code <= 0} to keep the
     * recipe's own duration instead (used by the brick kiln, whose recipes carry their own times).
     */
    default int gtna$recipeDuration() {
        return 1;
    }
}
