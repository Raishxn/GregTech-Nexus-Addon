package com.raishxn.gtna.common.machine.trait;

import com.gregtechceu.gtceu.api.capability.IParallelHatch;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.ActionResult;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.raishxn.gtna.api.machine.IThreadModifierMachine;
import com.raishxn.gtna.api.machine.multiblock.ParallelMachine;
import com.raishxn.gtna.common.machine.multiblock.electric.WorkableElectricMultipleRecipesMachine;
import com.raishxn.gtna.utils.GTNARecipeUtils;
import com.raishxn.gtna.utils.ThreadMultiplierStrategy;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class GTNAMultipleRecipesLogic extends RecipeLogic {

    private final List<GTNARecipeUtils.ActiveRecipe> activeRecipes = new ArrayList<>();

    public GTNAMultipleRecipesLogic(MetaMachine machine) {
        super((IRecipeLogicMachine) machine);
    }

    public int getActiveRecipeCount() {
        return activeRecipes.size();
    }

    public int getMaxThreads() {
        int threads = 1;
        if (machine instanceof IThreadModifierMachine modifierMachine) {
            threads += modifierMachine.getAdditionalThread();
        }
        if (machine instanceof MetaMachine metaMachine &&
                metaMachine.getDefinition() instanceof MultiblockMachineDefinition mbDefinition) {
            threads *= ThreadMultiplierStrategy.getAdditionalMultiplier(mbDefinition);
        }
        return Math.max(1, threads);
    }

    public int getMaxParallel() {
        if (machine instanceof ParallelMachine parallelMachine) {
            return parallelMachine.getMaxParallel();
        }
        if (machine instanceof WorkableElectricMultiblockMachine workable) {
            return workable.getParallelHatch()
                    .map(IParallelHatch::getCurrentParallel)
                    .orElse(1);
        }
        return 1;
    }

    @Override
    public void serverTick() {
        MetaMachine metaMachine = (MetaMachine) this.machine;
        if (metaMachine.getLevel() == null || metaMachine.getLevel().isClientSide) return;

        boolean visualChanged = false;

        Iterator<GTNARecipeUtils.ActiveRecipe> iterator = activeRecipes.iterator();
        while (iterator.hasNext()) {
            GTNARecipeUtils.ActiveRecipe active = iterator.next();

            if (active.update()) {
                completeRecipe(active);
                iterator.remove();
                visualChanged = true;
            }
        }

        boolean isMachineEnabled = true;
        if (machine instanceof WorkableElectricMultiblockMachine workable) {
            isMachineEnabled = workable.isWorkingEnabled();
        }

        if (isMachineEnabled) {
            int maxThreads = getMaxThreads();
            boolean limitThreadsByRecipe = getMaxParallel() > 1;

            if (activeRecipes.size() < maxThreads) {
                List<GTRecipe> possibleRecipes = new ArrayList<>();
                var recipeIterator = machine.getRecipeType().searchRecipe((IRecipeCapabilityHolder) machine, recipe -> true);

                int searchLimit = 30;
                while (recipeIterator.hasNext() && possibleRecipes.size() < searchLimit) {
                    GTRecipe r = recipeIterator.next();
                    if (r != null) possibleRecipes.add(r);
                }

                for (GTRecipe validRecipe : possibleRecipes) {
                    while (activeRecipes.size() < maxThreads) {
                        if (limitThreadsByRecipe && isRecipeAlreadyActive(validRecipe)) break;
                        if (!tryStartRecipe(validRecipe)) break;

                        visualChanged = true;
                        if (limitThreadsByRecipe) break;
                    }
                    if (activeRecipes.size() >= maxThreads) break;
                }
            }
        }

        if (!activeRecipes.isEmpty()) {
            this.setStatus(Status.WORKING);
        } else {
            this.setStatus(Status.IDLE);
        }

        if (visualChanged) {
            metaMachine.getHolder().scheduleRenderUpdate();
        }
    }

    private boolean isRecipeAlreadyActive(GTRecipe candidate) {
        for (GTNARecipeUtils.ActiveRecipe active : activeRecipes) {
            if (active.recipe.getId() != null && candidate.getId() != null) {
                if (active.recipe.getId().equals(candidate.getId())) return true;
            } else {
                if (!active.recipe.outputs.isEmpty() && !candidate.outputs.isEmpty()) {
                    if (active.recipe.outputs.equals(candidate.outputs)) return true;
                }
            }
        }
        return false;
    }

    private boolean tryStartRecipe(GTRecipe recipe) {
        if (recipe == null) return false;
        IRecipeLogicMachine recipeLogicMachine = (IRecipeLogicMachine) machine;
        GTRecipe modifiedRecipe = recipeLogicMachine.fullModifyRecipe(recipe.copy());
        if (modifiedRecipe == null) return false;

        if (modifiedRecipe.duration < 1) modifiedRecipe.duration = 1;

        if (machine instanceof WorkableElectricMultipleRecipesMachine multiMachine) {
            double durationMult = multiMachine.getDurationMultiplier();
            if (durationMult < 1.0) {
                int newDuration = (int) (modifiedRecipe.duration * durationMult);
                modifiedRecipe.duration = Math.max(1, newDuration);
            }
        }

        if (!RecipeHelper.matchContents((IRecipeCapabilityHolder) machine, modifiedRecipe).isSuccess()) {
            return false;
        }

        ActionResult result = RecipeHelper.handleRecipeIO((IRecipeCapabilityHolder) machine, modifiedRecipe, IO.IN, this.getChanceCaches());

        if (result.isSuccess()) {
            GTNARecipeUtils.ActiveRecipe newActive = new GTNARecipeUtils.ActiveRecipe(
                    modifiedRecipe,
                    modifiedRecipe.duration,
                    this.getChanceCaches()
            );
            activeRecipes.add(newActive);
            return true;
        }
        return false;
    }

    public List<Component> getRecipeDisplayInfo() {
        List<Component> infoList = new ArrayList<>();

        for (int i = 0; i < activeRecipes.size(); i++) {
            GTNARecipeUtils.ActiveRecipe active = activeRecipes.get(i);

            int prog = active.progress;
            int max = active.maxProgress;
            float currentSec = prog / 20.0f;
            float maxSec = max / 20.0f;
            int percentage = max > 0 ? (int)((prog / (float)max) * 100) : 0;

            ChatFormatting percentColor = percentage < 33 ? ChatFormatting.RED : (percentage < 66 ? ChatFormatting.YELLOW : ChatFormatting.GREEN);

            MutableComponent line1 = Component.literal("T" + (i + 1) + ": ")
                    .withStyle(ChatFormatting.GOLD)
                    .append(Component.literal(String.format(Locale.US, "%.1fs/%.1fs ", currentSec, maxSec))
                            .withStyle(ChatFormatting.WHITE))
                    .append(Component.literal(String.format("(%d%%)", percentage))
                            .withStyle(percentColor));

            infoList.add(line1);

            String outputName = "Unknown";
            int totalCount = 1;

            if (active.recipe.outputs.containsKey(ItemRecipeCapability.CAP)) {
                List<Content> itemOutputs = active.recipe.outputs.get(ItemRecipeCapability.CAP);
                if (itemOutputs != null && !itemOutputs.isEmpty()) {
                    Content content = itemOutputs.get(0);
                    Object inner = content.getContent();
                    if (inner instanceof ItemStack stack) {
                        outputName = stack.getHoverName().getString();
                        totalCount = stack.getCount();
                    } else {
                        outputName = inner.toString();
                        totalCount = 1;
                    }
                }
            }

            double timePerItem = (maxSec > 0 && totalCount > 0) ? (maxSec / totalCount) : maxSec;

            MutableComponent line2 = Component.literal(outputName + " x " + totalCount + " ")
                    .withStyle(ChatFormatting.LIGHT_PURPLE)
                    .append(Component.literal(String.format(Locale.US, "(%.2fs/ea)", timePerItem))
                            .withStyle(ChatFormatting.GRAY));

            infoList.add(line2);
        }
        return infoList;
    }

    @Override
    public void saveCustomPersistedData(@NotNull CompoundTag tag, boolean forDrop) {
        super.saveCustomPersistedData(tag, forDrop);
        tag.putInt("ActiveRecipeCount", activeRecipes.size());
        for (int i = 0; i < activeRecipes.size(); i++) {
            GTNARecipeUtils.ActiveRecipe recipe = activeRecipes.get(i);
            tag.putInt("RProg" + i, recipe.progress);
            tag.putInt("RMax" + i, recipe.maxProgress);
        }
    }

    @Override
    public void loadCustomPersistedData(@NotNull CompoundTag tag) {
        super.loadCustomPersistedData(tag);
        activeRecipes.clear();
    }

    private void completeRecipe(GTNARecipeUtils.ActiveRecipe active) {
        if (active != null && active.recipe != null) {
            RecipeHelper.handleRecipeIO((IRecipeCapabilityHolder) machine, active.recipe, IO.OUT, active.chanceCaches);
        }
    }
}