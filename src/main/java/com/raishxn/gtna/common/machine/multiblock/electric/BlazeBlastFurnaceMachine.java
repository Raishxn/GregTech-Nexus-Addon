package com.raishxn.gtna.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.CoilWorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import org.jetbrains.annotations.Nullable;

import java.util.List;

/** GTOCore Blaze Blast Furnace: 64 parallel, half duration, molten Blaze upkeep. */
public final class BlazeBlastFurnaceMachine extends CoilWorkableElectricMultiblockMachine {

    public BlazeBlastFurnaceMachine(IMachineBlockEntity holder, Object... args) {
        super(holder);
    }

    public static ModifierFunction recipeModifier(MetaMachine machine, GTRecipe recipe) {
        if (!(machine instanceof BlazeBlastFurnaceMachine)) return ModifierFunction.NULL;
        int parallel = ParallelLogic.getParallelAmount(machine, recipe, 64);
        if (parallel <= 0) return ModifierFunction.NULL;
        return ModifierFunction.builder()
                .modifyAllContents(ContentModifier.multiplier(parallel))
                .eutMultiplier(parallel)
                .durationMultiplier(0.5)
                .parallels(parallel)
                .build();
    }

    @Override
    public boolean beforeWorking(@Nullable GTRecipe recipe) {
        if (recipe == null || recipe.data.getInt("ebf_temp") > heatingTemperature()) return false;
        return drainBlaze() && super.beforeWorking(recipe);
    }

    @Override
    public boolean onWorking() {
        if (getOffsetTimer() % 20 == 0 && !drainBlaze()) return false;
        return super.onWorking();
    }

    private int heatingTemperature() {
        return getCoilType().getCoilTemperature() + 100 * Math.max(0, getTier() - 2);
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (!isFormed()) return;
        int index = Math.min(2, textList.size());
        textList.add(index++, Component.translatable("gtna.ui.parallel_max",
                Component.literal("64").withStyle(ChatFormatting.LIGHT_PURPLE))
                .withStyle(ChatFormatting.GRAY));
        textList.add(index++, Component.translatable("gtna.ui.voiding_mode",
                Component.translatable(getVoidingMode().getSerializedName()).withStyle(ChatFormatting.GRAY))
                .withStyle(ChatFormatting.WHITE));
        textList.add(index++, Component.translatable("gtna.ui.heat_capacity",
                Component.literal(FormattingUtil.formatNumbers(heatingTemperature()) + "K")
                        .withStyle(ChatFormatting.RED))
                .withStyle(ChatFormatting.WHITE));
        if (getRecipeLogic().isIdle() && getRecipeLogic().getLastRecipe() == null) {
            textList.add(index, Component.translatable("gtna.ui.no_recipe_found").withStyle(ChatFormatting.GRAY));
        }
    }

    private boolean drainBlaze() {
        int required = (1 << Math.max(0, Math.min(20, getTier() - 2))) * 10;
        return FluidUpkeep.consume(this, GTMaterials.Blaze.getFluid(), required);
    }
}
