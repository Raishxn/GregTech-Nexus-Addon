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
import com.raishxn.gtna.common.machine.multiMachineBase.SteamMultiMachineBase;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;

public class LargeSteamAlloySmelter extends SteamMultiMachineBase {
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            LargeSteamAlloySmelter.class, SteamMultiMachineBase.MANAGED_FIELD_HOLDER);

    @Persisted
    private int targetParallel = 64; // Padrão conforme pedido

    public LargeSteamAlloySmelter(IMachineBlockEntity holder, Object... args) {
        super(holder, false, args);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Nullable
    public static ModifierFunction recipeModifier(MetaMachine machine, @Nonnull GTRecipe recipe) {
        if (!(machine instanceof LargeSteamAlloySmelter steamMachine)) {
            return ModifierFunction.NULL;
        }
        if (recipe.getType() != GTRecipeTypes.ALLOY_SMELTER_RECIPES) {
            return ModifierFunction.NULL;
        }

        int maxParallel = steamMachine.targetParallel;
        // Lógica corrigida para calcular o paralelo baseado nos itens disponíveis
        int parallels = ParallelLogic.getParallelAmount(machine, recipe, maxParallel);

        if (parallels == 0) return ModifierFunction.NULL;

        // 43% mais rápido = 57% da duração original (0.57)
        double durationMultiplier = 0.57;

        return ModifierFunction.builder()
                .modifyAllContents(ContentModifier.multiplier(parallels))
                .durationMultiplier(durationMultiplier)
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
                this.targetParallel = Math.min(64, this.targetParallel * 2); // Limite de 64 conforme pedido
            }
        }
    }
}