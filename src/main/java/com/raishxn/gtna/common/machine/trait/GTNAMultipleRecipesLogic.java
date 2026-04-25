package com.raishxn.gtna.common.machine.trait;

import com.gregtechceu.gtceu.api.capability.IParallelHatch;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.ActionResult;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;

import com.raishxn.gtna.api.machine.IThreadModifierMachine;
import com.raishxn.gtna.api.machine.feature.IPatternBufferModeHost;
import com.raishxn.gtna.api.machine.feature.IPatternBufferModeProvider;
import com.raishxn.gtna.api.machine.multiblock.ParallelMachine;
import com.raishxn.gtna.common.machine.multiblock.electric.WorkableElectricMultipleRecipesMachine;
import com.raishxn.gtna.common.machine.multiblock.steam.AdjustableSteamParallelMachine;
import com.raishxn.gtna.common.machine.multiblock.part.ae.GTNAMEPatternBufferPartMachine;
import com.raishxn.gtna.utils.GTNARecipeUtils;
import com.raishxn.gtna.utils.ThreadMultiplierStrategy;
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

    // ... (getters ActiveRecipeCount, MaxThreads, MaxParallel mantidos iguais) ...
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
        // ... (lógica do serverTick mantida igual) ...
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
        if (machine instanceof WorkableMultiblockMachine workable) {
            isMachineEnabled = workable.isWorkingEnabled();
        }
        if (isMachineEnabled) {
            int maxThreads = getMaxThreads();
            int currentParallel = getMaxParallel();
            boolean limitUniqueRecipe = currentParallel > 1;

            if (activeRecipes.size() < maxThreads) {
                int searchLimit = 30;
                List<GTRecipe> possibleRecipes = collectPossibleRecipes(searchLimit);
                for (GTRecipe validRecipe : possibleRecipes) {
                    if (activeRecipes.size() >= maxThreads) break;
                    if (limitUniqueRecipe && isRecipeAlreadyActive(validRecipe)) continue;
                    if (tryStartRecipe(validRecipe)) {
                        visualChanged = true;
                    }
                }
            }
        }
    }

    private List<GTRecipe> collectPossibleRecipes(int searchLimit) {
        List<GTRecipe> possibleRecipes = new ArrayList<>(searchLimit);
        var recipeTypes = machine.getRecipeTypes();
        if (recipeTypes == null || recipeTypes.length == 0) {
            recipeTypes = new GTRecipeType[] { machine.getRecipeType() };
        }

        // === FAST-PATH: receitas cacheadas direto do pattern buffer ===
        collectCachedBufferRecipes(possibleRecipes, recipeTypes);

        // === FALLBACK: busca com distribuição justa entre tipos ===
        if (possibleRecipes.size() < searchLimit) {
            int remaining = searchLimit - possibleRecipes.size();
            int perTypeLimit = Math.max(4, remaining / Math.max(1, recipeTypes.length));

            for (var recipeType : recipeTypes) {
                if (recipeType == null) continue;
                int found = 0;
                var recipeIterator = recipeType.searchRecipe(
                        (IRecipeCapabilityHolder) machine, recipe -> true);
                while (recipeIterator.hasNext() && found < perTypeLimit && possibleRecipes.size() < searchLimit) {
                    GTRecipe recipe = recipeIterator.next();
                    if (recipe == null || containsRecipe(possibleRecipes, recipe)) continue;
                    possibleRecipes.add(recipe);
                    found++;
                }
            }
        }
        return possibleRecipes;
    }

    /**
     * Fast-path: percorre os slots do pattern buffer que têm itens pendentes.
     * Se o slot já tem um cachedRecipeId, faz lookup direto por ID → O(1).
     * Isso evita o searchRecipe() cego que é O(n) por recipe type.
     */
    private void collectCachedBufferRecipes(List<GTRecipe> target, GTRecipeType[] recipeTypes) {
        if (!(machine instanceof IMultiController multiController)) return;
        MetaMachine metaMachine = (MetaMachine) machine;
        if (metaMachine.getLevel() == null) return;
        RecipeManager recipeManager = metaMachine.getLevel().getRecipeManager();

        for (IMultiPart part : multiController.getParts()) {
            if (!(part instanceof GTNAMEPatternBufferPartMachine buffer)) continue;
            for (int i = 0; i < buffer.getMaxPatternCount(); i++) {
                var slot = buffer.getInternalInventory()[i];
                if (slot.isItemEmpty() && slot.isFluidEmpty()) continue;

                var config = buffer.getSlotConfigs()[i];
                String cachedId = config.getCachedRecipeId();
                if (cachedId.isBlank()) continue;

                ResourceLocation recipeRL = ResourceLocation.tryParse(cachedId);
                if (recipeRL == null) continue;

                // Lookup direto por ID via RecipeManager → O(1)
                var optional = recipeManager.byKey(recipeRL);
                if (optional.isPresent() && optional.get() instanceof GTRecipe gtRecipe) {
                    if (!containsRecipe(target, gtRecipe)) {
                        target.add(gtRecipe);
                    }
                }
            }
        }
    }

    private static boolean containsRecipe(List<GTRecipe> possibleRecipes, GTRecipe candidate) {
        for (GTRecipe existing : possibleRecipes) {
            if (existing == candidate) {
                return true;
            }
            if (existing != null && candidate != null && existing.id != null && existing.id.equals(candidate.id)) {
                return true;
            }
        }
        return false;
    }

    private boolean tryStartRecipe(GTRecipe recipe) {
        // --- INICIO DA LÓGICA MANUAL ---

        GTRecipe recipeToRun;
        if (machine instanceof AdjustableSteamParallelMachine steamMachine) {
            recipeToRun = steamMachine.createThreadedRecipe(recipe);
            if (recipeToRun == null) return false;
        } else {
            int hatchParallel = getMaxParallel();
            int feasibleParallel = 1;

            if (hatchParallel > 1) {
                feasibleParallel = ParallelLogic.getParallelAmount((MetaMachine) machine, recipe, hatchParallel);
            }

            // 1. Modificador de Paralelo
            recipeToRun = recipe.copy();
            if (feasibleParallel > 1) {
                var parallelModifier = ModifierFunction.builder()
                        .modifyAllContents(ContentModifier.multiplier(feasibleParallel))
                        .eutMultiplier(feasibleParallel)
                        .parallels(feasibleParallel)
                        .build();
                recipeToRun = parallelModifier.apply(recipeToRun);
            }

            OverclockingLogic overclockingLogic = machine instanceof WorkableElectricMultipleRecipesMachine customMachine ?
                    customMachine.getOverclockingLogic() :
                    OverclockingLogic.NON_PERFECT_OVERCLOCK;
            var overclockModifier = GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(overclockingLogic)
                    .getModifier((MetaMachine) machine, recipeToRun);
            recipeToRun = overclockModifier.apply(recipeToRun);

            if (recipeToRun == null) return false;
        }

        if (machine instanceof WorkableElectricMultipleRecipesMachine customMachine) {
            double durationMultiplier = customMachine.getDurationMultiplier();

            if (durationMultiplier < 0.999) {
                var hatchModifier = ModifierFunction.builder()
                        .durationMultiplier(durationMultiplier)
                        .build();
                recipeToRun = hatchModifier.apply(recipeToRun);
            }

            int outputMultiplier = customMachine.getOutputBoostMultiplier();
            if (outputMultiplier > 1) {
                var outputModifier = ModifierFunction.builder()
                        .outputModifier(ContentModifier.multiplier(outputMultiplier))
                        .build();
                recipeToRun = outputModifier.apply(recipeToRun);
            }
        }
        // ------------------------------------------------

        // --- FIM DA LÓGICA MANUAL ---

        applyPatternBufferMode(recipeToRun);

        if (!RecipeHelper.matchContents((IRecipeCapabilityHolder) machine, recipeToRun).isSuccess()) {
            return false;
        }

        ActionResult result = RecipeHelper.handleRecipeIO((IRecipeCapabilityHolder) machine, recipeToRun, IO.IN,
                this.getChanceCaches());
        if (result.isSuccess()) {
            GTNARecipeUtils.ActiveRecipe active = new GTNARecipeUtils.ActiveRecipe(
                    recipeToRun,
                    recipeToRun.duration,
                    this.getChanceCaches());
            this.activeRecipes.add(active);
            notifyPatternBufferProviders(recipeToRun);
            return true;
        }
        return false;
    }

    // ... (Métodos isRecipeAlreadyActive, completeRecipe, getRecipeDisplayInfo, save/load mantidos iguais) ...
    private void applyPatternBufferMode(GTRecipe recipe) {
        String requestedMode = null;
        for (IPatternBufferModeProvider provider : getPatternBufferProviders()) {
            requestedMode = provider.gtna$getPreferredModeForRecipe(recipe);
            if (requestedMode != null && !requestedMode.isBlank()) {
                break;
            }
        }
        if ((requestedMode == null || requestedMode.isBlank()) && machine instanceof IPatternBufferModeHost host) {
            requestedMode = host.gtna$resolvePatternBufferMode(recipe);
        }
        if (requestedMode != null && !requestedMode.isBlank()) {
            applyRequestedMode(requestedMode, recipe);
        }
    }

    private void applyRequestedMode(String modeId, GTRecipe recipe) {
        if (modeId == null || modeId.isBlank()) {
            return;
        }
        if (machine instanceof IPatternBufferModeHost host && host.gtna$applyPatternBufferMode(modeId, recipe)) {
            return;
        }
        var recipeTypes = machine.getRecipeTypes();
        if (recipeTypes == null || recipeTypes.length <= 1) {
            return;
        }
        for (int i = 0; i < recipeTypes.length; i++) {
            var recipeType = recipeTypes[i];
            if (recipeType == null || recipeType.registryName == null) {
                continue;
            }
            String requested = modeId.trim().toLowerCase(Locale.ROOT);
            String fullId = recipeType.registryName.toString().toLowerCase(Locale.ROOT);
            String path = recipeType.registryName.getPath().toLowerCase(Locale.ROOT);
            String requestedNormalized = requested.replace('_', '/');
            String pathNormalized = path.replace('_', '/');
            if (requested.equals(fullId) || requested.equals(path) ||
                    requestedNormalized.equals(fullId) || requestedNormalized.equals(pathNormalized) ||
                    path.endsWith("_" + requested) || path.endsWith("/" + requested) ||
                    pathNormalized.endsWith("/" + requestedNormalized) ||
                    (("saw".equals(requested) || "cutting_saw".equals(requested)) &&
                            (path.contains("cutter") || path.contains("saw")))) {
                if (machine.getActiveRecipeType() != i) {
                    machine.setActiveRecipeType(i);
                }
                return;
            }
        }
    }

    private void notifyPatternBufferProviders(GTRecipe recipe) {
        for (IPatternBufferModeProvider provider : getPatternBufferProviders()) {
            provider.gtna$onRecipeStarted(recipe);
        }
    }

    private List<IPatternBufferModeProvider> getPatternBufferProviders() {
        List<IPatternBufferModeProvider> providers = new ArrayList<>();
        if (machine instanceof IMultiController multiController) {
            for (IMultiPart part : multiController.getParts()) {
                if (part instanceof IPatternBufferModeProvider provider) {
                    providers.add(provider);
                }
            }
        }
        return providers;
    }

    private boolean isRecipeAlreadyActive(GTRecipe recipe) {
        if (recipe.id == null) return false;
        for (GTNARecipeUtils.ActiveRecipe active : activeRecipes) {
            if (active.recipe.id != null && active.recipe.id.equals(recipe.id)) {
                return true;
            }
        }
        return false;
    }

    private void completeRecipe(GTNARecipeUtils.ActiveRecipe active) {
        if (active != null && active.recipe != null) {
            RecipeHelper.handleRecipeIO((IRecipeCapabilityHolder) machine, active.recipe, IO.OUT, active.chanceCaches);
        }
    }

    public List<Component> getRecipeDisplayInfo() {
        // (Código original de display info...)
        List<Component> infoList = new ArrayList<>();
        for (int i = 0; i < activeRecipes.size(); i++) {
            GTNARecipeUtils.ActiveRecipe active = activeRecipes.get(i);
            int prog = active.progress;
            int max = active.maxProgress;
            float currentSec = prog / 20.0f;
            float maxSec = max / 20.0f;
            int percentage = max > 0 ? (int) ((prog / (float) max) * 100) : 0;
            ChatFormatting percentColor = percentage < 33 ? ChatFormatting.RED :
                    (percentage < 66 ? ChatFormatting.YELLOW : ChatFormatting.GREEN);
            MutableComponent line1 = Component.literal("Thread " + (i + 1) + ": ")
                    .withStyle(ChatFormatting.GOLD)
                    .append(Component.literal(String.format(Locale.US, "%.1fs / %.1fs ", currentSec, maxSec))
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
                    } else if (inner instanceof SizedIngredient sized) {
                        ItemStack[] stacks = sized.getItems();
                        if (stacks.length > 0) outputName = stacks[0].getHoverName().getString();
                        totalCount = sized.getAmount();
                    } else if (inner instanceof Ingredient ing) {
                        ItemStack[] stacks = ing.getItems();
                        if (stacks.length > 0) outputName = stacks[0].getHoverName().getString();
                    }
                }
            }
            double timePerItem = (maxSec > 0 && totalCount > 0) ? (maxSec / totalCount) : maxSec;
            String displayName = outputName;
            int maxLength = 20;
            if (displayName.length() > maxLength) {
                displayName = displayName.substring(0, maxLength) + "...";
            }
            MutableComponent line2 = Component.literal(" -> ")
                    .withStyle(ChatFormatting.DARK_GRAY)
                    .append(Component.literal(displayName)
                            .withStyle(Style.EMPTY.withColor(ChatFormatting.LIGHT_PURPLE).withHoverEvent(
                                    new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(outputName)))))
                    .append(Component.literal(" x" + totalCount)
                            .withStyle(ChatFormatting.AQUA))
                    .append(Component.literal(String.format(Locale.US, " (%.2fs/item)", timePerItem))
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

    public List<GTNARecipeUtils.ActiveRecipe> getActiveRecipes() {
        return this.activeRecipes;
    }

    @Override
    public void loadCustomPersistedData(@NotNull CompoundTag tag) {
        super.loadCustomPersistedData(tag);
        activeRecipes.clear();
    }
}
