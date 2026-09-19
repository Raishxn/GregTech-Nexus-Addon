package com.raishxn.gtna.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.block.ICoilType;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.block.CoilBlock;

import org.jetbrains.annotations.Nullable;

/**
 * Thread-capable electric multiblock base that also exposes GTCEu heating-coil data.
 * Recipes carrying {@code ebf_temp} are rejected before their inputs are consumed when
 * the formed structure cannot provide the required temperature.
 */
public class CoilWorkableElectricMultipleRecipesMachine extends WorkableElectricMultipleRecipesMachine {

    private ICoilType coilType = CoilBlock.CoilType.CUPRONICKEL;
    private final boolean scaleParallelWithCoil;

    public CoilWorkableElectricMultipleRecipesMachine(IMachineBlockEntity holder, Object... args) {
        this(holder, false, args);
    }

    public CoilWorkableElectricMultipleRecipesMachine(IMachineBlockEntity holder, boolean scaleParallelWithCoil,
                                                       Object... args) {
        super(holder, args);
        this.scaleParallelWithCoil = scaleParallelWithCoil;
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        Object matchedCoil = getMultiblockState().getMatchContext().get("CoilType");
        if (matchedCoil instanceof ICoilType coil) {
            coilType = coil;
        }
    }

    @Override
    public boolean beforeWorking(@Nullable GTRecipe recipe) {
        if (recipe != null) {
            int requiredTemperature = recipe.data.getInt("ebf_temp");
            if (requiredTemperature > 0 && getHeatingCoilTemperature() < requiredTemperature) {
                return false;
            }
        }
        return super.beforeWorking(recipe);
    }

    @Override
    public int getMaxParallel() {
        int hatchParallel = super.getMaxParallel();
        if (!scaleParallelWithCoil) return hatchParallel;
        int exponent = Math.max(0, coilType.getCoilTemperature() / 900);
        int coilParallel = exponent >= 30 ? Integer.MAX_VALUE : 1 << exponent;
        return Math.max(hatchParallel, coilParallel);
    }

    public ICoilType getCoilType() {
        return coilType;
    }

    public int getCoilTier() {
        return coilType.getTier();
    }

    public int getHeatingCoilTemperature() {
        return coilType.getCoilTemperature() + 100 * Math.max(0, getTier() - 2);
    }
}
