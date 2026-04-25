package com.raishxn.gtna.common.machine.multiblock.steam;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.raishxn.gtna.api.machine.IThreadModifierMachine;
import com.raishxn.gtna.common.machine.multiblock.part.ThreadPartMachine;
import com.raishxn.gtna.common.machine.trait.GTNAMultipleRecipesLogic;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FixedThreadSteamParallelMachine extends AdjustableSteamParallelMachine implements IThreadModifierMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            FixedThreadSteamParallelMachine.class, AdjustableSteamParallelMachine.MANAGED_FIELD_HOLDER);

    private final int fixedThreads;

    public FixedThreadSteamParallelMachine(IMachineBlockEntity holder, GTRecipeType recipeType, int defaultParallel,
                                           int maxParallel, double durationMultiplier, int fixedThreads,
                                           Object... args) {
        super(holder, recipeType, defaultParallel, maxParallel, durationMultiplier, false, args);
        this.fixedThreads = Math.max(1, fixedThreads);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    protected @NotNull GTNAMultipleRecipesLogic createRecipeLogic(Object... args) {
        return new GTNAMultipleRecipesLogic(this);
    }

    @Override
    public @NotNull GTNAMultipleRecipesLogic getRecipeLogic() {
        return (GTNAMultipleRecipesLogic) super.getRecipeLogic();
    }

    @Override
    public int getAdditionalThread() {
        return fixedThreads - 1;
    }

    @Override
    public RecipeModifier getRecipeModifier() {
        return (machine, recipe) -> ModifierFunction.IDENTITY;
    }

    @Override
    public @Nullable ThreadPartMachine getThreadPartMachine() {
        return null;
    }

    @Override
    public void setThreadPartMachine(@Nullable ThreadPartMachine threadModifierPart) {}

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (isFormed()) {
            GTNAMultipleRecipesLogic logic = getRecipeLogic();
            textList.add(Component.literal("Active Threads: ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(logic.getActiveRecipeCount() + " / " + logic.getMaxThreads())
                            .withStyle(ChatFormatting.AQUA)));
            textList.addAll(logic.getRecipeDisplayInfo());
        }
    }
}
