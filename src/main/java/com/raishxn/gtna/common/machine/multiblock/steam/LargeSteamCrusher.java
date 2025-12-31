package com.raishxn.gtna.common.machine.multiblock.steam;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
// IMPORTANTE: NENHUM import de .client.util.TooltipHelper aqui!
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.raishxn.gtna.common.data.GTNAMachines; // Importe GTNAMachines
import com.raishxn.gtna.common.machine.multiMachineBase.SteamMultiMachineBase;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class LargeSteamCrusher extends SteamMultiMachineBase {

    // REMOVIDO: public static final BiConsumer... GTNA_ADD = ...

    public LargeSteamCrusher(IMachineBlockEntity holder, Object... args) {
        // Correção do construtor: false para isSteel (Bronze)
        super(holder, false, args);
    }

    @Nullable
    public static ModifierFunction recipeModifier(MetaMachine machine, @Nonnull GTRecipe recipe) {
        if (!(machine instanceof LargeSteamCrusher steamMachine)) {
            return ModifierFunction.NULL;
        }
        if (recipe.getType() != GTRecipeTypes.MACERATOR_RECIPES) {
            return ModifierFunction.NULL;
        }

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

            // CORREÇÃO: Usa GTNAMachines.GTNA_ADD
            GTNAMachines.GTNA_ADD.accept(this.getDefinition().asStack(), textList);
        }
    }
}