package com.raishxn.gtna.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IParallelHatch;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockDisplayText;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntCircuitIngredient;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.network.chat.Component;

import com.raishxn.gtna.common.data.GTNAItems;
import com.raishxn.gtna.common.data.GTNARecipeType;
import com.raishxn.gtna.config.GTNABalance;
import com.raishxn.gtna.config.GTNABalance.ElectricVoidMinerBalance;

import java.util.List;

/**
 * EV-entry Void Miner with separate precise (consumed essence) and random recipe maps.
 * Scanned world/planet data belongs to the Incubator chain. Legacy non-consumable selectors
 * remain supported for pack recipes. Both modes validate fluid/output caps and use capped
 * parallelism from the configured IV gate; registration composes normal electric overclock.
 */
public final class ElectricVoidMinerMachine extends WorkableElectricMultiblockMachine implements IDisplayUIMachine {

    public ElectricVoidMinerMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    /** Validates the program against the balance config, then applies the capped parallel amount. */
    public static ModifierFunction recipeModifier(MetaMachine machine, GTRecipe recipe) {
        if (!(machine instanceof ElectricVoidMinerMachine miner)) {
            return ModifierFunction.NULL;
        }
        if (!miner.isProgramAllowed(recipe)) {
            return ModifierFunction.NULL;
        }
        int cap = miner.parallelCap();
        if (cap <= 1) {
            return ModifierFunction.IDENTITY;
        }
        int hatchParallel = miner.getParallelHatch().map(IParallelHatch::getCurrentParallel).orElse(1);
        int parallels = ParallelLogic.getParallelAmount(machine, recipe, Math.min(cap, hatchParallel));
        if (parallels <= 1) {
            return ModifierFunction.IDENTITY;
        }
        return ModifierFunction.builder()
                .modifyAllContents(ContentModifier.multiplier(parallels))
                .eutMultiplier(parallels)
                .parallels(parallels)
                .build();
    }

    /** Config gate for one operation: tier, program selector, Drilling Fluid and output caps. */
    public boolean isProgramAllowed(GTRecipe recipe) {
        ElectricVoidMinerBalance config = GTNABalance.getElectricVoidMiner();
        if (operatingTier() < GTNABalance.getElectricVoidMinerMinimumTier()) {
            return false;
        }
        boolean random = recipe.recipeType == GTNARecipeType.RANDOM_VOID_MINING_RECIPES;
        if (config.programRequired && !random && !hasNonConsumableProgram(recipe) &&
                !hasConsumableEssence(recipe) && !isFallbackRecipe(recipe)) {
            return false;
        }
        int drillingFluid = inputFluidAmount(recipe);
        return drillingFluid > 0 && drillingFluid <= config.maxDrillingFluidPerOperation &&
                outputStacks(recipe) <=
                        (random ? config.maxRandomOutputStacksPerOperation : config.maxOutputStacksPerOperation);
    }

    /** Tier actually available to the machine, taken from the formed energy hatches. */
    public int operatingTier() {
        long voltage = getOverclockVoltage();
        if (voltage <= 0) {
            return GTValues.LV;
        }
        return Math.min(GTUtil.getTierByVoltage(voltage), GTValues.MAX);
    }

    /** Effective configured/hatch limit; energy, inputs and output room may lower a running batch. */
    public int parallelCap() {
        int tier = operatingTier();
        if (tier < GTNABalance.getElectricVoidMinerParallelTier()) {
            return 1;
        }
        return Math.min(GTNABalance.getElectricVoidMinerMaxParallel(tier),
                getParallelHatch().map(IParallelHatch::getCurrentParallel).orElse(1));
    }

    private static boolean hasNonConsumableProgram(GTRecipe recipe) {
        for (Content content : recipe.getInputContents(ItemRecipeCapability.CAP)) {
            if (content.chance == 0 ||
                    ItemRecipeCapability.CAP.of(content.content) instanceof IntCircuitIngredient) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasConsumableEssence(GTRecipe recipe) {
        for (Content content : recipe.getInputContents(ItemRecipeCapability.CAP)) {
            if (content.chance == 0) continue;
            var ingredient = ItemRecipeCapability.CAP.of(content.content);
            // Compare registered items directly: recipe reload does not rely on tag binding order.
            for (var essence : GTNAItems.VEIN_ESSENCES.values()) {
                if (ingredient.test(essence.asStack())) return true;
            }
        }
        return false;
    }

    private static boolean isFallbackRecipe(GTRecipe recipe) {
        return recipe.id != null && recipe.id.getPath().equals("electric_void_mining_fallback");
    }

    private static int inputFluidAmount(GTRecipe recipe) {
        List<Content> inputs = recipe.getInputContents(FluidRecipeCapability.CAP);
        if (inputs.size() != 1) {
            return 0;
        }
        FluidIngredient ingredient = FluidRecipeCapability.CAP.of(inputs.get(0).content);
        if (ingredient == null || ingredient.getAmount() <= 0 || ingredient.getStacks().length == 0) {
            return 0;
        }
        for (var stack : ingredient.getStacks()) {
            if (stack.getFluid() != GTMaterials.DrillingFluid.getFluid()) {
                return 0;
            }
        }
        return ingredient.getAmount();
    }

    private static int outputStacks(GTRecipe recipe) {
        return recipe.getOutputContents(ItemRecipeCapability.CAP).size();
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        MultiblockDisplayText.builder(textList, isFormed())
                .setWorkingStatus(recipeLogic.isWorkingEnabled(), recipeLogic.isActive())
                .addWorkingStatusLine()
                .addProgressLine(recipeLogic)
                .addCustom(tl -> {
                    tl.add(Component.translatable("gtna.machine.electric_void_miner.mode",
                            Component.translatable(getRecipeType() == GTNARecipeType.RANDOM_VOID_MINING_RECIPES ?
                                    "gtna.random_void_mining" : "gtna.electric_void_mining")));
                    tl.add(Component.translatable("gtna.machine.electric_void_miner.parallel_cap", parallelCap()));
                    GTRecipe recipe = recipeLogic.getLastRecipe();
                    if (recipe == null) {
                        tl.add(Component.translatable("gtna.machine.electric_void_miner.no_program"));
                    } else {
                        tl.add(Component.translatable("gtna.machine.electric_void_miner.active_program",
                                recipe.id == null ? "?" : recipe.id.toString()));
                        tl.add(Component.translatable("gtna.machine.electric_void_miner.active_cost",
                                inputFluidAmount(recipe), recipe.getInputEUt().getTotalEU(), recipe.parallels));
                    }
                })
                .addOutputLines(recipeLogic.getLastRecipe());
    }
}
