package com.raishxn.gtna.api.machine;

import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier; // Import Required/Necessário
import com.raishxn.gtna.common.machine.multiblock.part.ThreadPartMachine;
import org.jetbrains.annotations.Nullable;

public interface IThreadModifierMachine {

    /**
     * English: define this method here to allow overriding in the machine class.
     * Português: define este método aqui para permitir o @Override na classe da máquina.
     * Español: define este método aquí para permitir el @Override en la clase de la máquina.
     */
    RecipeModifier getRecipeModifier();

    default int getAdditionalThread() {
        ThreadPartMachine part = getThreadPartMachine();
        return part != null ? part.getThreadCount() : 0;
    }

    @Nullable
    ThreadPartMachine getThreadPartMachine();
    void setThreadPartMachine(@Nullable ThreadPartMachine threadModifierPart);
}