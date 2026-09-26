package com.raishxn.gtna.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.CoilWorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * GTOCore Chemical Plant. Runs Large Chemical Reactor recipes with the Parallel Hatch, a perfect
 * overclock and a coil efficiency bonus: every coil tier reduces EU and duration by 5%.
 * <p>
 * GTO applies this through the native (closed-source) gtolib {@code coilReductionOverclock(0.25)}.
 * The GTNA port reproduces the behaviour GTO itself displays for the controller
 * ({@code 1 - coilTier * 0.05} on EU and duration) and uses the guaranteed perfect overclock that
 * the controller advertises. The gtolib duration argument is documented as unverified.
 */
public final class ChemicalPlantMachine extends CoilWorkableElectricMultiblockMachine {

    public ChemicalPlantMachine(IMachineBlockEntity holder, Object... args) {
        super(holder);
    }

    public static ModifierFunction recipeModifier(MetaMachine machine, GTRecipe recipe) {
        if (!(machine instanceof ChemicalPlantMachine plant) || !plant.isFormed()) {
            return ModifierFunction.NULL;
        }
        double coilEfficiency = coilEfficiencyOf(plant);
        ModifierFunction reduction = ModifierFunction.builder()
                .eutMultiplier(coilEfficiency)
                .durationMultiplier(coilEfficiency)
                .build();
        ModifierFunction parallel = GTRecipeModifiers.hatchParallel(machine, recipe);
        ModifierFunction overclock = GTRecipeModifiers.OC_PERFECT.getModifier(machine, recipe);
        return parallel.compose(reduction).compose(overclock);
    }

    /** GTO's own controller display: EU and duration are scaled by one minus 5% per coil tier. */
    public static void addCoilDisplay(IMultiController controller, List<Component> components) {
        if (!(controller instanceof ChemicalPlantMachine plant)) return;
        double value = coilEfficiencyOf(plant);
        components.add(Component.translatable("gtna.machine.chemical_plant.eut_multiplier",
                FormattingUtil.formatNumbers(value)));
        components.add(Component.translatable("gtna.machine.chemical_plant.duration_multiplier",
                FormattingUtil.formatNumbers(value)));
    }

    private static double coilEfficiencyOf(ChemicalPlantMachine plant) {
        return 1 - plant.getCoilTier() * 0.05;
    }
}
