package com.raishxn.gtna.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import com.raishxn.gtna.api.machine.multiblock.IGTNAModuleHost;
import com.raishxn.gtna.common.data.GTNARecipeType;
import org.jetbrains.annotations.Nullable;

/**
 * GTOCore Cold Ice Freezer base: 64 parallel, half duration, liquid Ice upkeep.
 *
 * <p>
 * The auxiliary module ported from GTOCore's {@code addSubPattern} (Naquadah Alloy casing tower)
 * unlocks the {@code atomization_condensation} recipe type, exactly like GTO's
 * {@code ColdIceFreezerMachine#recipeTypeAvailable} returns {@code formedAmount > 0}. Without the
 * module the machine only exposes vacuum recipes to its recipe logic, the mode switcher and JEI.
 */
public final class ColdIceFreezerMachine extends WorkableElectricMultiblockMachine {

    public ColdIceFreezerMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    public static ModifierFunction recipeModifier(MetaMachine machine, GTRecipe recipe) {
        if (!(machine instanceof ColdIceFreezerMachine)) return ModifierFunction.NULL;
        int parallel = ParallelLogic.getParallelAmount(machine, recipe, 64);
        if (parallel <= 0) return ModifierFunction.NULL;
        return ModifierFunction.builder()
                .modifyAllContents(ContentModifier.multiplier(parallel))
                .eutMultiplier(parallel)
                .durationMultiplier(0.5)
                .parallels(parallel)
                .build();
    }

    /**
     * GTOCore {@code recipeTypeAvailable(ATOMIZATION_CONDENSATION_RECIPES)}: the atomization family
     * only exists once the auxiliary module matched. GTCEu registers recipe types on the definition,
     * so the filter happens here instead of in the pattern.
     */
    @Override
    public GTRecipeType[] getRecipeTypes() {
        GTRecipeType[] all = super.getRecipeTypes();
        if (gtna$formedModuleCount() > 0) {
            return all;
        }
        int hidden = 0;
        for (GTRecipeType type : all) {
            if (type == GTNARecipeType.ATOMIZATION_CONDENSATION_RECIPES) {
                hidden++;
            }
        }
        if (hidden == 0) {
            return all;
        }
        GTRecipeType[] base = new GTRecipeType[all.length - hidden];
        int index = 0;
        for (GTRecipeType type : all) {
            if (type != GTNARecipeType.ATOMIZATION_CONDENSATION_RECIPES) {
                base[index++] = type;
            }
        }
        return base;
    }

    @Override
    public GTRecipeType getRecipeType() {
        GTRecipeType[] types = getRecipeTypes();
        int index = getActiveRecipeType();
        return types[index >= 0 && index < types.length ? index : 0];
    }

    private boolean atomizationAvailable() {
        return gtna$formedModuleCount() > 0;
    }

    @Override
    public boolean beforeWorking(@Nullable GTRecipe recipe) {
        if (recipe != null && recipe.getType() == GTNARecipeType.ATOMIZATION_CONDENSATION_RECIPES &&
                !atomizationAvailable()) {
            return false;
        }
        return recipe != null && drainIce() && super.beforeWorking(recipe);
    }

    @Override
    public boolean onWorking() {
        if (getOffsetTimer() % 20 == 0 && !drainIce()) return false;
        return super.onWorking();
    }

    private boolean drainIce() {
        int required = (1 << Math.max(0, Math.min(20, getTier() - 2))) * 10;
        return FluidUpkeep.consume(this, GTMaterials.Ice.getFluid(), required);
    }

    private int gtna$formedModuleCount() {
        return ((IGTNAModuleHost) (Object) this).gtna$formedModuleCount();
    }
}
