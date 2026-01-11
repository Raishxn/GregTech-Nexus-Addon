package com.raishxn.gtna.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IParallelHatch;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockDisplayText; // Importação do Builder
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
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
    }

    @Override
    public RecipeModifier getRecipeModifier() {
        return (machine, recipe) -> {
            int parallelLimit = getParallelLimit();
            ModifierFunction parallelModifier = ModifierFunction.IDENTITY;

            if (parallelLimit > 1) {
                int actualParallel = ParallelLogic.getParallelAmount(machine, recipe, parallelLimit);
                if (actualParallel == 0) return ModifierFunction.NULL;

                if (actualParallel > 1) {
                    parallelModifier = ModifierFunction.builder()
                            .modifyAllContents(ContentModifier.multiplier(actualParallel))
                            .eutMultiplier(actualParallel)
                            .parallels(actualParallel)
                            .build();
                }
            }
            ModifierFunction overclockModifier = GTRecipeModifiers.OC_NON_PERFECT.getModifier(machine, recipe);
            return parallelModifier.andThen(overclockModifier);
        };
    }

    protected int getParallelLimit() {
        Optional<IParallelHatch> hatch = getParts().stream()
                .filter(IParallelHatch.class::isInstance)
                .map(IParallelHatch.class::cast)
                .findFirst();

        return hatch.map(IParallelHatch::getCurrentParallel).orElse(1);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        this.accelerateHatches.clear();
        for (IMultiPart part : getParts()) {
            if (part instanceof AccelerateHatchPartMachine accelerateHatch) {
                accelerateHatches.add(accelerateHatch);
            }
        }
        // Garante a inicialização do container de energia para exibição correta
        if (this.energyContainer == null) {
            this.energyContainer = getEnergyContainer();
        }
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
        // Usando o Builder MultiblockDisplayText, igual ao IndustrialSlaughterhouse
        MultiblockDisplayText.builder(textList, isFormed())
                .setWorkingStatus(recipeLogic.isWorkingEnabled(), recipeLogic.isActive())
                .addCustom(text -> {
                    GTNAMultipleRecipesLogic logic = getRecipeLogic();

                    // 1. Dados de Energia e Tier
                    long storedEnergy = 0;
                    if (this.energyContainer != null) {
                        storedEnergy = this.energyContainer.getEnergyStored();
                    } else if (getEnergyContainer() != null) {
                        storedEnergy = getEnergyContainer().getEnergyStored();
                    }

                    int tier = getTier();
                    String tierName = GTValues.VN[tier];

                    // --- FORMATO SOLICITADO ---
                    // "Max EU/t: energia armazenada ( tier)"
                    // Exemplo visual: "Max EU/t: 50,000 (HV)"
                    text.add(Component.literal("Max EU/t: ")
                            .withStyle(ChatFormatting.GRAY)
                            .append(Component.literal(String.format(Locale.US, "%,d", storedEnergy))
                                    .withStyle(ChatFormatting.WHITE))
                            .append(Component.literal(" (" + tierName + ")")
                                    .withStyle(ChatFormatting.GOLD)));
                    // --------------------------

                    // 2. Max Recipe Tier
                    text.add(Component.literal("Max Recipe Tier: ")
                            .withStyle(ChatFormatting.GRAY)
                            .append(Component.literal(tierName)
                                    .withStyle(ChatFormatting.YELLOW)));

                    // 3. Parallels
                    int parallel = getParallelLimit();
                    if (parallel > 1) {
                        text.add(Component.literal("Parallels: ")
                                .withStyle(ChatFormatting.GRAY)
                                .append(Component.literal(String.valueOf(parallel))
                                        .withStyle(ChatFormatting.GREEN)));
                    }

                    // 4. Speed Boost
                    double speedMult = getDurationMultiplier();
                    if (speedMult < 1.0) {
                        int speedBonus = (int)((1.0 - speedMult) * 100);
                        text.add(Component.literal("Speed Boost: ")
                                .withStyle(ChatFormatting.GRAY)
                                .append(Component.literal("+" + speedBonus + "%")
                                        .withStyle(ChatFormatting.LIGHT_PURPLE)));
                    }

                    // 5. Active Threads
                    text.add(Component.literal("Active Threads: ")
                            .withStyle(ChatFormatting.GRAY)
                            .append(Component.literal(logic.getActiveRecipeCount() + " / " + logic.getMaxThreads())
                                    .withStyle(ChatFormatting.AQUA)));

                    text.add(Component.empty());

                    // 6. Recipe Details
                    List<Component> activeThreadsInfo = logic.getRecipeDisplayInfo();
                    if (!activeThreadsInfo.isEmpty()) {
                        text.addAll(activeThreadsInfo);
                    } else {
                        text.add(Component.literal("Idle - Waiting for inputs...")
                                .withStyle(ChatFormatting.DARK_GRAY));
                    }
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