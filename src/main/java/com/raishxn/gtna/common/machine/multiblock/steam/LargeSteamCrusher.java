package com.raishxn.gtna.common.machine.multiblock.steam;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.client.util.TooltipHelper;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.raishxn.gtna.common.machine.multiMachineBase.SteamMultiMachineBase;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiConsumer;

public class LargeSteamCrusher extends SteamMultiMachineBase {

    public static final BiConsumer<ItemStack, List<Component>> GTNA_ADD = (stack, components) -> components
            .add(Component.translatable("gtna.registry.add")
                    .withStyle(style -> style.withColor(TooltipHelper.RAINBOW_SLOW.getCurrent())));

    public LargeSteamCrusher(IMachineBlockEntity holder, Object... args) {
        super(holder, args.length > 0 && args[0] instanceof Boolean ? (boolean) args[0] : true);
    }

    @Override
    public com.gregtechceu.gtceu.api.recipe.GTRecipeType getRecipeType() {
        return GTRecipeTypes.MACERATOR_RECIPES;
    }

    // --- CORREÇÃO AQUI ---
    // O retorno mudou de GTRecipe para ModifierFunction
    @Nullable
    public static ModifierFunction recipeModifier(MetaMachine machine, @Nonnull GTRecipe recipe) {
        if (!(machine instanceof LargeSteamCrusher steamMachine)) {
            return ModifierFunction.NULL;
        }
        if (recipe.getType() != GTRecipeTypes.MACERATOR_RECIPES) {
            return ModifierFunction.NULL;
        }

        // Configuração: 8 paralelos e 2x velocidade (0.5x duração)
        int parallels = 8;
        double durationMultiplier = 0.5;

        return ModifierFunction.builder()
                .inputModifier(ContentModifier.multiplier(parallels))
                .outputModifier(ContentModifier.multiplier(parallels))
                .durationMultiplier(durationMultiplier)
                .parallels(parallels)
                .build();
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (this.isFormed()) {
            textList.add(Component.translatable("gtna.tooltip.large_steam_crusher.speed").withStyle(ChatFormatting.GOLD));
            textList.add(Component.translatable("gtna.tooltip.large_steam_crusher.steam").withStyle(ChatFormatting.AQUA));
            textList.add(Component.translatable("gtna.tooltip.large_steam_crusher.parallel").withStyle(ChatFormatting.BLUE));
            textList.add(Component.translatable(GTNA_ADD.toString()).withStyle(style -> style.withColor(TooltipHelper.RAINBOW_SLOW.getCurrent())));
        }
    }
}