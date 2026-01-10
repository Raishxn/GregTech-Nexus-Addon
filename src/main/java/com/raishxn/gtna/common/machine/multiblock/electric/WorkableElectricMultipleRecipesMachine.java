package com.raishxn.gtna.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.raishxn.gtna.api.machine.IThreadModifierMachine;
import com.raishxn.gtna.common.machine.multiblock.part.ThreadPartMachine;
import com.raishxn.gtna.common.machine.trait.GTNAMultipleRecipesLogic;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WorkableElectricMultipleRecipesMachine extends WorkableElectricMultiblockMachine
        implements IThreadModifierMachine {

    @Nullable
    private ThreadPartMachine threadModifierPart;

    public WorkableElectricMultipleRecipesMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    // Substitui a lógica padrão pela nossa lógica de Multi-Thread
    @Override
    protected @NotNull RecipeLogic createRecipeLogic(Object @NotNull... args) {
        return new GTNAMultipleRecipesLogic(this);
    }

    @Override
    public GTNAMultipleRecipesLogic getRecipeLogic() {
        return (GTNAMultipleRecipesLogic) super.getRecipeLogic();
    }

    // Implementação da Interface IThreadModifierMachine
    @Override
    public @Nullable ThreadPartMachine getThreadPartMachine() {
        return this.threadModifierPart;
    }

    @Override
    public void setThreadPartMachine(@Nullable ThreadPartMachine threadModifierPart) {
        this.threadModifierPart = threadModifierPart;
    }
}