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
import com.raishxn.gtna.api.machine.multiblock.IGTNAModulePerformanceHost;
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
                    .apply(getOverclockingLogic())
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
    /**
     * @param recipeTier the recipe's pre-overclock voltage tier (GTOCore semantics: the accelerate
     *                   penalty follows the recipe, not the machine tier)
     */
    public double getDurationMultiplier(int recipeTier) {
        double multiplier = 1.0;
        for (AccelerateHatchPartMachine hatch : accelerateHatches) {
            double percentage = hatch.calcDurationPercentage(recipeTier) / 100.0;
            multiplier *= percentage;
        }
        return Math.max(0.01, multiplier / ((IGTNAModulePerformanceHost) this).gtna$getModuleSpeedBonus());
    }

    /** Best-case multiplier for the UI (no recipe tier penalty). */
    public double getNominalDurationMultiplier() {
        double multiplier = 1.0;
        for (AccelerateHatchPartMachine hatch : accelerateHatches) {
            multiplier *= hatch.getMinDurationPercentage() / 100.0;
        }
        return Math.max(0.01, multiplier / ((IGTNAModulePerformanceHost) this).gtna$getModuleSpeedBonus());
    }

    public double getOverclockHatchMultiplier() {
        return getOverclockDurationFactor();
    }

    public double getOverclockDurationFactor() {
        double multiplier = OverclockingLogic.STD_DURATION_FACTOR;
        for (OverclockHatchPartMachine hatch : overclockHatches) {
            multiplier = Math.min(multiplier, hatch.getOverclockMultiplier());
        }
        return multiplier;
    }

    public boolean hasOverclockHatch() {
        return !overclockHatches.isEmpty();
    }

    public OverclockingLogic getOverclockingLogic() {
        if (!hasOverclockHatch()) {
            return ((IGTNAModulePerformanceHost) this).gtna$hasModulePerfectOverclock() ?
                    OverclockingLogic.PERFECT_OVERCLOCK : OverclockingLogic.NON_PERFECT_OVERCLOCK;
        }
        return OverclockingLogic.create(getOverclockDurationFactor(), OverclockingLogic.STD_VOLTAGE_FACTOR, false);
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
                    // Electric multiblocks derive their tier from the energy container voltage, which can exceed
                    // MAX when an over-tier energy hatch is installed; never index VN out of bounds.
                    String tierName = tier >= 0 && tier < GTValues.VN.length ? GTValues.VN[tier] : ("T" + tier);
                    text.add(Component.translatable("gtna.multiblock.max_eut",
                            Component.literal(String.format(Locale.US, "%,d", storedEnergy))
                                    .withStyle(ChatFormatting.WHITE),
                            Component.literal(tierName).withStyle(ChatFormatting.GOLD))
                            .withStyle(ChatFormatting.GRAY));

                    int parallel = getMaxParallel();
                    boolean gtoProcessMachine = this instanceof IndustrialFlotationCellMachine ||
                            this instanceof VacuumDryingFurnaceMachine;
                    if (parallel > 1 && !gtoProcessMachine) {
                        text.add(Component.translatable("gtna.multiblock.parallels",
                                Component.literal(String.valueOf(parallel)).withStyle(ChatFormatting.GREEN))
                                .withStyle(ChatFormatting.GRAY));
                    }

                    // Informações de UI dos Hatches
                    if (hasOverclockHatch()) {
                        double ocMultiplier = getOverclockDurationFactor();
                        text.add(Component.translatable("gtna.multiblock.overclock_hatch",
                                Component.translatable("gtna.multiblock.overclock_hatch.value", ocMultiplier)
                                        .withStyle(ChatFormatting.LIGHT_PURPLE))
                                .withStyle(ChatFormatting.GRAY));
                    }

                    double accMultiplier = getNominalDurationMultiplier();
                    if (accMultiplier < 1.0) {
                        text.add(Component.translatable("gtna.multiblock.accelerate_hatch",
                                Component.translatable("gtna.multiblock.accelerate_hatch.value", accMultiplier)
                                        .withStyle(ChatFormatting.LIGHT_PURPLE))
                                .withStyle(ChatFormatting.GRAY));
                    }

                    int outputMultiplier = getOutputBoostMultiplier();
                    if (outputMultiplier > 1) {
                        text.add(Component.translatable("gtna.multiblock.output_boost_hatch",
                                Component.translatable("gtna.multiblock.output_boost_hatch.value", outputMultiplier)
                                        .withStyle(ChatFormatting.AQUA))
                                .withStyle(ChatFormatting.GRAY));
                    }

                    // The thread panel only belongs to machines that actually run multiple threads
                    // (Thread Hatch installed). Without it the machine is a normal single-recipe
                    // multiblock and the standard MultiblockDisplayText progress above is enough.
                    if (logic.getMaxThreads() > 1) {
                        text.add(Component.translatable("gtna.multiblock.active_threads",
                                Component.literal(logic.getActiveRecipeCount() + " / " + logic.getMaxThreads())
                                        .withStyle(ChatFormatting.AQUA))
                                .withStyle(ChatFormatting.GRAY));

                        text.add(Component.empty());
                        List<Component> activeThreadsInfo = logic.getRecipeDisplayInfo();
                        if (!activeThreadsInfo.isEmpty()) text.addAll(activeThreadsInfo);
                        else text.add(Component.translatable("gtna.multiblock.idle")
                                .withStyle(ChatFormatting.DARK_GRAY));
                    }
                });
        if (isFormed() && (this instanceof IndustrialFlotationCellMachine ||
                this instanceof VacuumDryingFurnaceMachine)) {
            int index = Math.min(2, textList.size());
            int parallel = getMaxParallel();
            if (parallel > 1) {
                textList.add(index++, Component.translatable("gtna.ui.parallel_max",
                        Component.literal(Integer.toString(parallel)).withStyle(ChatFormatting.LIGHT_PURPLE))
                        .withStyle(ChatFormatting.GRAY));
            }
            textList.add(index++, Component.translatable("gtna.ui.voiding_mode",
                    Component.translatable(getVoidingMode().getSerializedName()).withStyle(ChatFormatting.GRAY))
                    .withStyle(ChatFormatting.WHITE));
            if (this instanceof VacuumDryingFurnaceMachine furnace) {
                textList.add(index++, Component.translatable("gtna.ui.heat_capacity",
                        Component.literal(com.gregtechceu.gtceu.utils.FormattingUtil
                                .formatNumbers(furnace.getHeatingCoilTemperature()) + "K")
                                .withStyle(ChatFormatting.RED))
                        .withStyle(ChatFormatting.WHITE));
            }
            if (getRecipeLogic().isIdle() && getRecipeLogic().getLastRecipe() == null) {
                textList.add(index, Component.translatable("gtna.ui.no_recipe_found")
                        .withStyle(ChatFormatting.GRAY));
            }
        }
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
        // Exact GTM formula (MachineModeFancyConfigurator.setActiveRecipeTypeAndUpdateTickSubs):
        // only re-subscribe tick handlers when the mode actually changed and the machine
        // does not keep its subscriptions alive permanently.
        for (int i = 0; i < getRecipeTypes().length; i++) {
            if (gtna$matchesModeId(modeId, getRecipeTypes()[i])) {
                boolean needUpdateTickSubs = !keepSubscribing() && getActiveRecipeType() != i;
                setActiveRecipeType(i); // @Persisted: NBT + network sync are automatic
                if (needUpdateTickSubs) {
                    getRecipeLogic().updateTickSubscription();
                }
                return true;
            }
        }
        return false;
    }
}
