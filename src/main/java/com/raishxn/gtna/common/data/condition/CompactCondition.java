package com.raishxn.gtna.common.data.condition;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.raishxn.gtna.common.data.GTNAMachines;
import com.mojang.serialization.Codec;
import com.raishxn.gtna.data.recipe.GTNARecipeConditions;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class CompactCondition extends RecipeCondition {

    public static final CompactCondition INSTANCE = new CompactCondition();

    public static final Codec<CompactCondition> CODEC = Codec.unit(INSTANCE);

    public CompactCondition() {
        super();
    }

    @Override
    public RecipeConditionType<?> getType() {
        return GTNARecipeConditions.COMPACT;
    }

    @Override
    public Component getTooltips() {
        return Component.translatable("gtna.recipe.condition.compact_only");
    }

    @Override
    public boolean testCondition(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        MetaMachine machine = recipeLogic.getMachine();
        return machine.getDefinition() == GTNAMachines.COMPACT_HYPER_PRESSURE_REACTOR;
    }

    @Override
    public RecipeCondition createTemplate() {
        return INSTANCE;
    }
}