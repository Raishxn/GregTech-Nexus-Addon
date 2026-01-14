package com.raishxn.gtna.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IParallelHatch;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockDisplayText;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.raishxn.gtna.api.machine.IThreadModifierMachine;
import com.raishxn.gtna.common.machine.multiblock.part.AccelerateHatchPartMachine;
import com.raishxn.gtna.common.machine.multiblock.part.ThreadPartMachine;
import com.raishxn.gtna.common.machine.trait.GTNAMultipleRecipesLogic;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class WorkableElectricMultipleRecipesMachine extends WorkableElectricMultiblockMachine
        implements IThreadModifierMachine {

    @Nullable
    private ThreadPartMachine threadModifierPart;
    private final List<AccelerateHatchPartMachine> accelerateHatches = new ArrayList<>();

    public WorkableElectricMultipleRecipesMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
        // DEBUG: Se isso não aparecer no console ao carregar o jogo/colocar a máquina,
        // você registrou a classe errada no MachineDefinition!
        System.out.println(">>> DEBUG: CONSTRUTOR DE WorkableElectricMultipleRecipesMachine CHAMADO <<<");
    }

    @Override
    public RecipeModifier getRecipeModifier() {
        return (machine, recipe) -> {
            // DEBUG: Verificando inputs iniciais
            int hatchParallel = getParallelLimit();

            // Print para saber se estamos chegando aqui
            System.out.println("[DEBUG MODIFIER] Receita ID: " + recipe.getId());
            System.out.println("[DEBUG MODIFIER] Limite do Hatch: " + hatchParallel);

            // Se o hatch for 1, retornamos identidade, mas avisamos no log
            if (hatchParallel <= 1) {
                // System.out.println("[DEBUG MODIFIER] Hatch <= 1, abortando paralelo.");
                return ModifierFunction.IDENTITY;
            }

            // Tenta calcular o paralelo real usando a lógica do GregTech
            int feasibleParallel = ParallelLogic.getParallelAmount(machine, recipe, hatchParallel);
            System.out.println("[DEBUG MODIFIER] ParallelLogic calculou: " + feasibleParallel);

            if (feasibleParallel <= 1) {
                // System.out.println("[DEBUG MODIFIER] Itens insuficientes para paralelo > 1.");
                return ModifierFunction.IDENTITY;
            }

            // Aplica o modificador
            System.out.println("[DEBUG MODIFIER] APLICANDO PARALELO x" + feasibleParallel);

            var parallelModifier = ModifierFunction.builder()
                    .modifyAllContents(com.gregtechceu.gtceu.api.recipe.content.ContentModifier.multiplier(feasibleParallel))
                    .eutMultiplier(feasibleParallel)
                    .parallels(feasibleParallel)
                    .build();

            GTRecipe parallelRecipe = parallelModifier.apply(recipe);

            var overclockModifier = GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK)
                    .getModifier(machine, parallelRecipe);

            return parallelModifier.andThen(overclockModifier);
        };
    }

    protected int getParallelLimit() {
        // Logica robusta para achar o hatch
        int maxParallel = 1;
        for (IMultiPart part : getParts()) {
            if (part instanceof IParallelHatch hatch) {
                int current = hatch.getCurrentParallel();
                // Pega o maior valor encontrado (caso tenha múltiplos hatches, o que é raro)
                if (current > maxParallel) {
                    maxParallel = current;
                }
            }
        }
        return maxParallel;
    }

    // --- MÉTODOS OBRIGATÓRIOS (BOILERPLATE) ---

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        this.accelerateHatches.clear();
        for (IMultiPart part : getParts()) {
            if (part instanceof AccelerateHatchPartMachine accelerateHatch) {
                accelerateHatches.add(accelerateHatch);
            }
        }
        if (this.energyContainer == null) {
            this.energyContainer = getEnergyContainer();
        }
        System.out.println(">>> DEBUG: Estrutura Formada. Hatches encontrados: " + accelerateHatches.size());
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        this.accelerateHatches.clear();
    }

    @Override
    protected @NotNull GTNAMultipleRecipesLogic createRecipeLogic(Object... args) {
        return new GTNAMultipleRecipesLogic(this);
    }

    @Override
    public @NotNull GTNAMultipleRecipesLogic getRecipeLogic() {
        return (GTNAMultipleRecipesLogic) super.getRecipeLogic();
    }

    public double getDurationMultiplier() {
        double multiplier = 1.0;
        for (AccelerateHatchPartMachine hatch : accelerateHatches) {
            double percentage = hatch.calcDurationPercentage(this.getTier()) / 100.0;
            multiplier *= percentage;
        }
        return Math.max(0.1, multiplier);
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        MultiblockDisplayText.builder(textList, isFormed())
                .setWorkingStatus(recipeLogic.isWorkingEnabled(), recipeLogic.isActive())
                .addCustom(text -> {
                    GTNAMultipleRecipesLogic logic = getRecipeLogic();
                    long storedEnergy = 0;
                    if (this.energyContainer != null) {
                        storedEnergy = this.energyContainer.getEnergyStored();
                    } else if (getEnergyContainer() != null) {
                        storedEnergy = getEnergyContainer().getEnergyStored();
                    }
                    int tier = getTier();
                    String tierName = GTValues.VN[tier];
                    text.add(Component.literal("Max EU/t: ").withStyle(ChatFormatting.GRAY)
                            .append(Component.literal(String.format(Locale.US, "%,d", storedEnergy)).withStyle(ChatFormatting.WHITE))
                            .append(Component.literal(" (" + tierName + ")").withStyle(ChatFormatting.GOLD)));

                    int parallel = getParallelLimit();
                    if (parallel > 1) {
                        text.add(Component.literal("Parallels: ").withStyle(ChatFormatting.GRAY)
                                .append(Component.literal(String.valueOf(parallel)).withStyle(ChatFormatting.GREEN)));
                    }

                    text.add(Component.literal("Active Threads: ").withStyle(ChatFormatting.GRAY)
                            .append(Component.literal(logic.getActiveRecipeCount() + " / " + logic.getMaxThreads()).withStyle(ChatFormatting.AQUA)));

                    text.add(Component.empty());
                    List<Component> activeThreadsInfo = logic.getRecipeDisplayInfo();
                    if (!activeThreadsInfo.isEmpty()) text.addAll(activeThreadsInfo);
                    else text.add(Component.literal("Idle - Waiting for inputs...").withStyle(ChatFormatting.DARK_GRAY));
                });
    }

    @Override
    public @Nullable ThreadPartMachine getThreadPartMachine() {
        return this.threadModifierPart;
    }

    @Override
    public void setThreadPartMachine(@Nullable ThreadPartMachine threadModifierPart) {
        this.threadModifierPart = threadModifierPart;
    }
}