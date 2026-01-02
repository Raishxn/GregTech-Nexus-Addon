package com.raishxn.gtna.common.machine.multiblock.steam;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.raishxn.gtna.common.data.GTNARecipeType;
import com.raishxn.gtna.common.machine.multiMachineBase.SteamMultiMachineBase;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;

public class SteamManufacturer extends SteamMultiMachineBase {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            SteamManufacturer.class, SteamMultiMachineBase.MANAGED_FIELD_HOLDER);

    @Persisted
    private int targetParallel = 16; // Padrão e máximo inicial

    public SteamManufacturer(IMachineBlockEntity holder, Object... args) {
        // 'false' indica que usa a textura/UI estilo Bronze/Steam padrão, não Steel (a menos que queira mudar)
        super(holder, false, args);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Nullable
    public static ModifierFunction recipeModifier(MetaMachine machine, @Nonnull GTRecipe recipe) {
        if (!(machine instanceof SteamManufacturer manufacturer)) {
            return ModifierFunction.NULL;
        }

        // Verifica se a receita é do tipo correto (Hydraulic Manufacturing)
        if (recipe.getType() != GTNARecipeType.HYDRAULIC_MANUFACTURING) {
            return ModifierFunction.NULL;
        }

        int maxParallel = manufacturer.targetParallel;

        // Calcula o paralelo possível baseado nos inputs e fluidos (incluindo Steam)
        int parallels = ParallelLogic.getParallelAmount(machine, recipe, maxParallel);

        if (parallels == 0) return ModifierFunction.NULL;

        return ModifierFunction.builder()
                .modifyAllContents(ContentModifier.multiplier(parallels))
                .parallels(parallels)
                .build();
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (this.isFormed()) {
            textList.add(Component.translatable("gtna.multiblock.parallel_amount", this.targetParallel)
                    .withStyle(ChatFormatting.BLUE));

            // Botões para ajustar o parallel na interface
            textList.add(Component.literal("Parallels: ")
                    .append(ComponentPanelWidget.withButton(Component.literal("[-] "), "parallelSub"))
                    .append(ComponentPanelWidget.withButton(Component.literal("[+]"), "parallelAdd")));
        }
    }

    @Override
    public void handleDisplayClick(String componentData, ClickData clickData) {
        if (!clickData.isRemote) {
            if (componentData.equals("parallelSub")) {
                // Reduz pela metade ou mínimo 1
                this.targetParallel = Math.max(1, this.targetParallel / 2);
            } else if (componentData.equals("parallelAdd")) {
                // Dobra ou máximo 16 (conforme solicitado)
                this.targetParallel = Math.min(16, this.targetParallel * 2);
            }
        }
    }
}