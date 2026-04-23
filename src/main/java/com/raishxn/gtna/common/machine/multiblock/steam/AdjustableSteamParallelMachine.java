package com.raishxn.gtna.common.machine.multiblock.steam;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;

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

public class AdjustableSteamParallelMachine extends SteamMultiMachineBase {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            AdjustableSteamParallelMachine.class, SteamMultiMachineBase.MANAGED_FIELD_HOLDER);

    private final GTRecipeType recipeType;
    private final int maxParallel;
    private final double durationMultiplier;
    private final boolean adjustable;

    @Persisted
    private int targetParallel;

    public AdjustableSteamParallelMachine(IMachineBlockEntity holder, GTRecipeType recipeType, int defaultParallel,
                                          int maxParallel, double durationMultiplier, boolean adjustable,
                                          Object... args) {
        super(holder, false, args);
        this.recipeType = recipeType;
        this.targetParallel = defaultParallel;
        this.maxParallel = maxParallel;
        this.durationMultiplier = durationMultiplier;
        this.adjustable = adjustable;
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Nullable
    @Override
    protected GTRecipe getRealRecipe(@Nonnull GTRecipe recipe) {
        if (recipe.getType() != recipeType) {
            return null;
        }
        int parallels = ParallelLogic.getParallelAmount(this, recipe, targetParallel);
        if (parallels == 0) {
            return null;
        }
        return ModifierFunction.builder()
                .modifyAllContents(ContentModifier.multiplier(parallels))
                .durationMultiplier(durationMultiplier)
                .parallels(parallels)
                .build()
                .apply(recipe.copy());
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (adjustable && isFormed()) {
            textList.add(Component.translatable("gtna.multiblock.parallel_amount", this.targetParallel)
                    .withStyle(ChatFormatting.GOLD));
            textList.add(Component.literal("Parallels: ")
                    .append(ComponentPanelWidget.withButton(Component.literal("[-] "), "parallelSub"))
                    .append(ComponentPanelWidget.withButton(Component.literal("[+]"), "parallelAdd")));
        }
    }

    @Override
    public void handleDisplayClick(String componentData, ClickData clickData) {
        if (!adjustable || clickData.isRemote) {
            return;
        }
        if ("parallelSub".equals(componentData)) {
            this.targetParallel = Math.max(1, this.targetParallel / 2);
        } else if ("parallelAdd".equals(componentData)) {
            this.targetParallel = Math.min(maxParallel, Math.max(1, this.targetParallel * 2));
        }
    }
}
