package com.raishxn.gtna.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import net.minecraft.network.chat.Component;

import com.raishxn.gtna.api.machine.multiblock.IGTNAModuleHost;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * GTOCore Component Assembler base. All tier casings in the structure must agree, and a batch
 * recipe cannot start above that tier.
 *
 * <p>
 * GTOCore's {@code ComponentAssemblerMachine#onStructureFormed} caps the base structure at IV and
 * raises the cap to UV once an {@code addSubPattern} extension matched ({@code getSubFormedAmount()
 * > 0}). GTNA ports the two extension layers as modules; the raised cap is UV, matching GTOCore,
 * and the tier itself still comes from the main pattern's casing cells, exactly like the base port.
 */
public final class ComponentAssemblerMachine extends WorkableElectricMultiblockMachine {

    private int casingTier;

    public ComponentAssemblerMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        Object matched = getMultiblockState().getMatchContext().get("ComponentAssemblyCasingTier");
        int baseCap = gtna$formedModuleCount() > 0 ? GTValues.UV : GTValues.IV;
        casingTier = matched instanceof Integer tier ? Math.min(baseCap, tier) : 0;
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

    /** The supported casing tier: IV for the base structure, UV once an extension module formed. */
    public int getCasingTier() {
        return casingTier;
    }

    private int gtna$formedModuleCount() {
        return ((IGTNAModuleHost) (Object) this).gtna$formedModuleCount();
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (casingTier > 0) {
            textList.add(
                    Component.translatable("gtna.machine.component_assembler.casing_tier", GTValues.VN[casingTier]));
        }
    }
}
