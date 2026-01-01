package com.raishxn.gtna.common.machine.multiblock.steam;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.raishxn.gtna.common.machine.multiMachineBase.SteamMultiMachineBase;
import com.lowdragmc.lowdraglib.gui.util.ClickData; // Importante
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget; // Importante
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted; // Importante
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder; // Importante
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;

public class LargeSteamCrusher extends SteamMultiMachineBase {
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            LargeSteamCrusher.class, SteamMultiMachineBase.MANAGED_FIELD_HOLDER);
    @Persisted
    private int targetParallel = 8;

    public LargeSteamCrusher(IMachineBlockEntity holder, Object... args) {
        super(holder, false, args);
    }
    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }
    @Nullable
    public static ModifierFunction recipeModifier(MetaMachine machine, @Nonnull GTRecipe recipe) {
        if (!(machine instanceof LargeSteamCrusher steamMachine)) {
            return ModifierFunction.NULL;
        }
        if (recipe.getType() != GTRecipeTypes.MACERATOR_RECIPES) {
            return ModifierFunction.NULL;
        }
        int parallels = steamMachine.targetParallel;
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
        super.addDisplayText(textList); // Chama o base para mostrar Steam e Progresso

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
                this.targetParallel = Math.min(128, this.targetParallel * 2);
            }
        }
    }
}