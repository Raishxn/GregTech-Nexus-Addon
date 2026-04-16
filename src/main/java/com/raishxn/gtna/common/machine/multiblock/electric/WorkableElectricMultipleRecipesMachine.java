package com.raishxn.gtna.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IParallelHatch;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockDisplayText;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import com.raishxn.gtna.api.machine.IThreadModifierMachine;
import com.raishxn.gtna.api.machine.feature.IPatternBufferModeHost;
import com.raishxn.gtna.api.machine.multiblock.ParallelMachine;
import com.raishxn.gtna.common.machine.multiblock.part.AccelerateHatchPartMachine;
import com.raishxn.gtna.common.machine.multiblock.part.OutputBoostHatchPartMachine;
import com.raishxn.gtna.common.machine.multiblock.part.OverclockHatchPartMachine;
import com.raishxn.gtna.common.machine.multiblock.part.ThreadPartMachine;
import com.raishxn.gtna.common.machine.trait.GTNAMultipleRecipesLogic;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WorkableElectricMultipleRecipesMachine extends WorkableElectricMultiblockMachine
                                                    implements IThreadModifierMachine, ParallelMachine,
                                                    IPatternBufferModeHost {

    @Nullable
    private ThreadPartMachine threadModifierPart;
    // Listas essenciais para o Logic calcular o tempo
    private final List<AccelerateHatchPartMachine> accelerateHatches = new ArrayList<>();
    private final List<OverclockHatchPartMachine> overclockHatches = new ArrayList<>();
    private final List<OutputBoostHatchPartMachine> outputBoostHatches = new ArrayList<>();

    public WorkableElectricMultipleRecipesMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    // Mantemos o getRecipeModifier simples e funcional para compatibilidade
    @Override
    public RecipeModifier getRecipeModifier() {
        return (machine, recipe) -> {
            // 1. Calcula Paralelo
            int parallel = ParallelLogic.getParallelAmount(machine, recipe, getMaxParallel());

            // 2. Constrói o modificador manualmente (já que não existe getModifier no ParallelLogic)
            var modifier = parallel > 1 ?
                    ModifierFunction.builder()
                            .modifyAllContents(ContentModifier.multiplier(parallel))
                            .eutMultiplier(parallel)
                            .parallels(parallel)
                            .build() :
                    ModifierFunction.IDENTITY;

            // 3. Aplica o modificador e depois o Overclock Padrão
            return (ModifierFunction) GTRecipeModifiers.ELECTRIC_OVERCLOCK
                    .apply(OverclockingLogic.NON_PERFECT_OVERCLOCK)
                    .applyModifier(machine, modifier.apply(recipe));
        };
    }

    @Override
    public int getMaxParallel() {
        return getParallelLimit();
    }

    protected int getParallelLimit() {
        int superParallel = getParallelHatch().map(IParallelHatch::getCurrentParallel).orElse(1);
        if (superParallel > 1) return superParallel;
        int maxParallel = 1;
        for (IMultiPart part : getParts()) {
            if (part instanceof IParallelHatch hatch) {
                int current = hatch.getCurrentParallel();
                if (current > maxParallel) {
                    maxParallel = current;
                }
            }
        }
        return maxParallel;
    }

    // ESSENCIAL: Preenche as listas quando a estrutura forma
    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        this.accelerateHatches.clear();
        this.overclockHatches.clear();
        this.outputBoostHatches.clear();

        for (IMultiPart part : getParts()) {
            if (part instanceof AccelerateHatchPartMachine accelerateHatch) {
                accelerateHatches.add(accelerateHatch);
            }
            if (part instanceof OverclockHatchPartMachine overclockHatch) {
                overclockHatches.add(overclockHatch);
            }
            if (part instanceof OutputBoostHatchPartMachine outputBoostHatch) {
                outputBoostHatches.add(outputBoostHatch);
            }
        }
        if (this.energyContainer == null) {
            this.energyContainer = getEnergyContainer();
        }
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        this.accelerateHatches.clear();
        this.overclockHatches.clear();
        this.outputBoostHatches.clear();
    }

    @Override
    protected @NotNull GTNAMultipleRecipesLogic createRecipeLogic(Object... args) {
        return new GTNAMultipleRecipesLogic(this);
    }

    @Override
    public @NotNull GTNAMultipleRecipesLogic getRecipeLogic() {
        return (GTNAMultipleRecipesLogic) super.getRecipeLogic();
    }

    // Métodos usados pelo GTNAMultipleRecipesLogic para calcular a velocidade final
    public double getDurationMultiplier() {
        double multiplier = 1.0;
        for (AccelerateHatchPartMachine hatch : accelerateHatches) {
            double percentage = hatch.calcDurationPercentage(this.getTier()) / 100.0;
            multiplier *= percentage;
        }
        return Math.max(0.01, multiplier);
    }

    public double getOverclockHatchMultiplier() {
        double multiplier = 1.0;
        for (OverclockHatchPartMachine hatch : overclockHatches) {
            multiplier *= hatch.getOverclockMultiplier();
        }
        return multiplier;
    }

    public int getOutputBoostMultiplier() {
        int multiplier = 1;
        for (OutputBoostHatchPartMachine hatch : outputBoostHatches) {
            multiplier *= hatch.getOutputMultiplier();
        }
        return Math.max(1, multiplier);
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
                            .append(Component.literal(String.format(Locale.US, "%,d", storedEnergy))
                                    .withStyle(ChatFormatting.WHITE))
                            .append(Component.literal(" (" + tierName + ")").withStyle(ChatFormatting.GOLD)));

                    int parallel = getMaxParallel();
                    if (parallel > 1) {
                        text.add(Component.literal("Parallels: ").withStyle(ChatFormatting.GRAY)
                                .append(Component.literal(String.valueOf(parallel)).withStyle(ChatFormatting.GREEN)));
                    }

                    // Informações de UI dos Hatches
                    double ocMultiplier = getOverclockHatchMultiplier();
                    if (ocMultiplier < 1.0) {
                        text.add(Component.literal("Overclock Hatch: ").withStyle(ChatFormatting.GRAY)
                                .append(Component.literal(String.format("%.2fx Duration", ocMultiplier))
                                        .withStyle(ChatFormatting.LIGHT_PURPLE)));
                    }

                    double accMultiplier = getDurationMultiplier();
                    if (accMultiplier < 1.0) {
                        text.add(Component.literal("Accelerate Hatch: ").withStyle(ChatFormatting.GRAY)
                                .append(Component.literal(String.format("%.2fx Duration", accMultiplier))
                                        .withStyle(ChatFormatting.LIGHT_PURPLE)));
                    }

                    int outputMultiplier = getOutputBoostMultiplier();
                    if (outputMultiplier > 1) {
                        text.add(Component.literal("Output Boost Hatch: ").withStyle(ChatFormatting.GRAY)
                                .append(Component.literal(String.format("%dx Outputs", outputMultiplier))
                                        .withStyle(ChatFormatting.AQUA)));
                    }

                    text.add(Component.literal("Active Threads: ").withStyle(ChatFormatting.GRAY)
                            .append(Component.literal(logic.getActiveRecipeCount() + " / " + logic.getMaxThreads())
                                    .withStyle(ChatFormatting.AQUA)));

                    text.add(Component.empty());
                    List<Component> activeThreadsInfo = logic.getRecipeDisplayInfo();
                    if (!activeThreadsInfo.isEmpty()) text.addAll(activeThreadsInfo);
                    else text
                            .add(Component.literal("Idle - Waiting for inputs...").withStyle(ChatFormatting.DARK_GRAY));
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

    @Override
    public @Nullable String gtna$resolvePatternBufferMode(com.gregtechceu.gtceu.api.recipe.GTRecipe recipe) {
        if (getRecipeTypes().length <= 1) {
            return null;
        }
        return recipe.getType().registryName.toString();
    }

    @Override
    public boolean gtna$applyPatternBufferMode(String modeId, com.gregtechceu.gtceu.api.recipe.GTRecipe recipe) {
        if (modeId == null || modeId.isBlank()) {
            return false;
        }
        for (int i = 0; i < getRecipeTypes().length; i++) {
            if (gtna$matchesModeId(modeId, getRecipeTypes()[i])) {
                if (getActiveRecipeType() != i) {
                    setActiveRecipeType(i);
                }
                return true;
            }
        }
        return false;
    }
}
