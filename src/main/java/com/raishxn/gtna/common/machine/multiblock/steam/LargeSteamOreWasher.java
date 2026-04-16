package com.raishxn.gtna.common.machine.multiblock.steam;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import com.raishxn.gtna.common.machine.multiMachineBase.SteamMultiMachineBase;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import javax.annotation.Nonnull;

public class LargeSteamOreWasher extends SteamMultiMachineBase {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            LargeSteamOreWasher.class, SteamMultiMachineBase.MANAGED_FIELD_HOLDER);

    @Persisted
    private int targetParallel = 48;

    public LargeSteamOreWasher(IMachineBlockEntity holder, Object... args) {
        super(holder, false, args);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Nullable
    public static ModifierFunction recipeModifier(MetaMachine machine, @Nonnull GTRecipe recipe) {
        if (!(machine instanceof LargeSteamOreWasher steamMachine)) {
            return ModifierFunction.NULL;
        }
        if (recipe.getType() != GTRecipeTypes.ORE_WASHER_RECIPES) {
            return ModifierFunction.NULL;
        }

        int parallels = ParallelLogic.getParallelAmount(machine, recipe, steamMachine.targetParallel);
        if (parallels == 0) return ModifierFunction.NULL;

        return ModifierFunction.builder()
                .modifyAllContents(ContentModifier.multiplier(parallels))
                .durationMultiplier(0.2)
                .parallels(parallels)
                .build();
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (this.isFormed()) {
            textList.add(Component.translatable("gtna.multiblock.parallel_amount", this.targetParallel)
                    .withStyle(ChatFormatting.GOLD));
            textList.add(Component.literal("Parallels: ")
                    .append(ComponentPanelWidget.withButton(Component.literal("[-] "), "parallelSub"))
                    .append(ComponentPanelWidget.withButton(Component.literal("[+]"), "parallelAdd")));
        }
    }

    @Override
    public void handleDisplayClick(String componentData, ClickData clickData) {
        if (!clickData.isRemote) {
            if (componentData.equals("parallelSub")) {
                this.targetParallel = Math.max(1, this.targetParallel / 2);
            } else if (componentData.equals("parallelAdd")) {
                this.targetParallel = Math.min(96, this.targetParallel * 2);
            }
        }
    }
}
