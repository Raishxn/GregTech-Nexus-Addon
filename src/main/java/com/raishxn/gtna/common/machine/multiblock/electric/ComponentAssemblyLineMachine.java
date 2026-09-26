package com.raishxn.gtna.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import net.minecraft.network.chat.Component;

import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * GTOCore {@code component_assembly_line}: the 31×15×47 tier-cased line that runs the
 * {@code component_assembly} family with a Parallel Hatch and GTO's LuV-and-above batches.
 *
 * <p>
 * GTOCore builds it on the gtolib {@code TierCasingCrossRecipeMultiblockMachine}: a cross-recipe
 * machine with a uniform tier-casing rule and hatch-driven parallel. GTNA has neither gtolib nor the
 * cross-recipe execution, so this port keeps the observable behavior: all {@code [` tier casings
 * must agree, a recipe cannot start above that casing tier, and the Parallel Hatch drives
 * {@code GTRecipeModifiers#hatchParallel}. The machine runs one recipe at a time (GTOCore can keep
 * several cross-recipe threads); the non-perfect overclock matches the base Component Assembler and
 * GTO's separate {@code perfectOverclock()} marker. GTNA ships the LV–UV casing tiers, so the cap
 * is UV instead of MAX like GTOCore.
 */
public final class ComponentAssemblyLineMachine extends WorkableElectricMultiblockMachine {

    private int casingTier;

    public ComponentAssemblyLineMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        Object matched = getMultiblockState().getMatchContext().get("ComponentAssemblyLineCasingTier");
        casingTier = matched instanceof Integer tier ? Math.min(GTValues.UV, tier) : 0;
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        casingTier = 0;
    }

    @Override
    public boolean beforeWorking(@Nullable GTRecipe recipe) {
        return recipe != null && recipe.data.getInt("component_casing_tier") <= casingTier &&
                super.beforeWorking(recipe);
    }

    /** The casing tier read from the formed structure (0 when not formed). */
    public int getCasingTier() {
        return casingTier;
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (casingTier > 0) {
            textList.add(Component.translatable("gtna.machine.component_assembly_line.casing_tier",
                    GTValues.VN[casingTier]));
        }
    }
}
