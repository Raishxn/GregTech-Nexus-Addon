package com.raishxn.gtna.common.machine.trait;

import com.gregtechceu.gtceu.api.capability.IParallelHatch;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
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
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.fluids.FluidStack;

import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.api.machine.IThreadModifierMachine;
import com.raishxn.gtna.api.machine.IZeroEnergyMachine;
import com.raishxn.gtna.api.machine.feature.IPatternBufferModeHost;
import com.raishxn.gtna.api.machine.feature.IPatternBufferModeProvider;
import com.raishxn.gtna.api.machine.feature.PatternBufferModeSelection;
import com.raishxn.gtna.api.machine.multiblock.ParallelMachine;
import com.raishxn.gtna.common.machine.multiblock.electric.WorkableElectricMultipleRecipesMachine;
import com.raishxn.gtna.common.machine.multiblock.part.ae.GTNAMEPatternBufferPartMachine;
import com.raishxn.gtna.common.machine.multiblock.steam.AdjustableSteamParallelMachine;
import com.raishxn.gtna.utils.GTNARecipeUtils;
import com.raishxn.gtna.utils.GTNAUtil;
import com.raishxn.gtna.utils.ThreadMultiplierStrategy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        MetaMachine metaMachine = (MetaMachine) this.machine;
        if (metaMachine.getLevel() == null || metaMachine.getLevel().isClientSide) return;
        boolean changed = false;
        boolean progressed = false;
        boolean waiting = false;
        boolean isMachineEnabled = !(machine instanceof WorkableMultiblockMachine workable) ||
                workable.isWorkingEnabled();

        if (isMachineEnabled) {
            Iterator<GTNARecipeUtils.ActiveRecipe> iterator = activeRecipes.iterator();
            while (iterator.hasNext()) {
                GTNARecipeUtils.ActiveRecipe active = iterator.next();
                TickResult tickResult = tickRecipe(active);
                if (tickResult == TickResult.COMPLETE) {
                    completeRecipe(active);
                    iterator.remove();
                    changed = true;
                    progressed = true;
                } else if (tickResult == TickResult.ABORTED) {
                    iterator.remove();
                    changed = true;
                } else if (tickResult == TickResult.PROGRESSED) {
                    progressed = true;
                } else {
                    waiting = true;
                }
            }
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
                        changed = true;
                        progressed = true;
                    }
                }
            }
        }

        updateAggregateState(progressed, waiting);
        if (changed || (!activeRecipes.isEmpty() && metaMachine.getOffsetTimer() % 20 == 0)) {
            metaMachine.markDirty();
        }
    }

    private TickResult tickRecipe(GTNARecipeUtils.ActiveRecipe active) {
        if (active == null || active.recipe == null) return TickResult.ABORTED;

        ActionResult conditions = RecipeHelper.checkConditions(active.recipe, this);
        if (!conditions.isSuccess()) return TickResult.WAITING;

        if (active.recipe.hasTick()) {
            ActionResult match = RecipeHelper.matchTickRecipe((IRecipeCapabilityHolder) machine, active.recipe);
            if (!match.isSuccess()) return TickResult.WAITING;

            ActionResult input = RecipeHelper.handleTickRecipeIO((IRecipeCapabilityHolder) machine,
                    active.recipe, IO.IN, active.chanceCaches);
            if (!input.isSuccess()) return TickResult.WAITING;

            ActionResult output = RecipeHelper.handleTickRecipeIO((IRecipeCapabilityHolder) machine,
                    active.recipe, IO.OUT, active.chanceCaches);
            if (!output.isSuccess()) return TickResult.WAITING;
        }

        if (!machine.onWorking()) return TickResult.ABORTED;
        return active.update() ? TickResult.COMPLETE : TickResult.PROGRESSED;
    }

    private void updateAggregateState(boolean progressed, boolean waiting) {
        if (activeRecipes.isEmpty()) {
            progress = 0;
            duration = 0;
            isActive = false;
            setStatus(Status.IDLE);
            lastRecipe = null;
            return;
        }

        GTNARecipeUtils.ActiveRecipe representative = activeRecipes.get(0);
        progress = representative.progress;
        duration = representative.maxProgress;
        lastRecipe = representative.recipe;
        isActive = progressed;
        if (progressed) {
            setStatus(Status.WORKING);
        } else if (waiting) {
            setWaiting(Component.translatable("gtceu.recipe_logic.insufficient_input"));
        } else {
            setStatus(Status.SUSPEND);
        }
    }

    private enum TickResult {
        PROGRESSED,
        WAITING,
        COMPLETE,
        ABORTED
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
                    if (isAllowedRecipeType(gtRecipe, recipeTypes) && buffer.gtna$slotAcceptsRecipe(i, gtRecipe) &&
                            !containsRecipe(target, gtRecipe)) {
                        target.add(gtRecipe);
                    }
                }
            }
        }
    }

    private static boolean isAllowedRecipeType(GTRecipe recipe, GTRecipeType[] recipeTypes) {
        for (GTRecipeType recipeType : recipeTypes) {
            if (recipeType == recipe.getType()) return true;
        }
        return false;
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
        if (machine instanceof IZeroEnergyMachine zeroEnergy) {
            // No energy: strip EU, parallelise from the (stripped) inputs and force a fixed duration.
            GTRecipe base = recipe.copy();
            stripEnergyContents(base);
            int cap = getMaxParallel();
            // GTCEu's ParallelLogic.getMaxByInput returns 0 for these (vanilla-converted) furnace recipes
            // even though the machine sees the items, so compute the input budget directly instead.
            int byInput = cap > 1 ? computeZeroEnergyParallel(base, cap) : 1;
            int byOutput = byInput > 0 ?
                    ParallelLogic.limitByOutputMerging((IRecipeCapabilityHolder) machine, base, byInput,
                            ((IRecipeLogicMachine) machine)::canVoidRecipeOutputs, java.util.Collections.emptyList()) :
                    0;
            int feasibleParallel = byOutput > 0 ? byOutput : Math.max(1, byInput);
            int inLists = ((IRecipeCapabilityHolder) machine).getCapabilitiesForIO(IO.IN).size();
            long visibleItems = 0;
            for (var hl : ((IRecipeCapabilityHolder) machine).getCapabilitiesForIO(IO.IN)) {
                for (var h : hl.getCapability(ItemRecipeCapability.CAP)) {
                    for (Object c : h.getContents()) {
                        if (c instanceof net.minecraft.world.item.ItemStack stack) visibleItems += stack.getCount();
                    }
                }
            }
            GTNACORE.LOGGER.debug(
                    "[GTNA][ZeroEnergy] recipe={} cap={} byInput={} byOutput={} feasibleParallel={} inLists={} visibleItems={}",
                    recipe.getId(), cap, byInput, byOutput, feasibleParallel, inLists, visibleItems);
            recipeToRun = base;
            if (feasibleParallel > 1) {
                recipeToRun = ModifierFunction.builder()
                        .modifyAllContents(ContentModifier.multiplier(feasibleParallel))
                        .parallels(feasibleParallel)
                        .build()
                        .apply(recipeToRun);
            }
            int forcedDuration = zeroEnergy.gtna$recipeDuration();
            if (forcedDuration > 0) {
                recipeToRun.duration = forcedDuration;
            }
        } else if (machine instanceof AdjustableSteamParallelMachine steamMachine) {
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
            double durationMultiplier = customMachine.getDurationMultiplier(RecipeHelper.getRecipeEUtTier(recipe));

            if (durationMultiplier < 0.999) {
                var hatchModifier = ModifierFunction.builder()
                        .durationMultiplier(durationMultiplier)
                        .build();
                recipeToRun = hatchModifier.apply(recipeToRun);
            }

            // Output boost is intentionally NOT applied here: the RecipeHelper mixin already
            // multiplies outputs (match and execution) from the parts implementing
            // GTNAOutputBoostItemPart/FluidPart, and the dedicated OutputBoostHatchPartMachine
            // implements both. Applying it here too squared the boost (M -> M^2) and made the
            // simulated match demand M^2 free space. Single source of truth: GTNASpecialPartUtil.
        }
        // ------------------------------------------------

        // --- FIM DA LÓGICA MANUAL ---

        if (!RecipeHelper.matchContents((IRecipeCapabilityHolder) machine, recipeToRun).isSuccess()) {
            return false;
        }

        // Mirror the pattern's mode onto the controller's activeRecipeType so the UI tab,
        // machine mode display and tick subscriptions reflect what is actually running.
        // Routing itself is per-slot (see gtna$slotAcceptsRecipe); this is display-only.
        //
        // Policy (chosen deliberately): the mode pinned on the pattern-buffer slot that serves
        // this recipe wins, because that is what the player asked for; AUTO slots fall back to the
        // recipe's own type. With several threads of different types the global activeRecipeType
        // will alternate between them — that is inherent to the field being global.
        String mirrorModeId = resolvePatternBufferModeId(recipeToRun);
        if (mirrorModeId != null && machine instanceof IPatternBufferModeHost host) {
            if (!host.gtna$applyPatternBufferMode(mirrorModeId, recipeToRun)) {
                // A pin is guaranteed by gtna$slotAcceptsRecipe to match the recipe's type, but it
                // may be a stale id that no machine mode resolves to anymore. Falling back to the
                // exact type keeps the mirror alive instead of silently disabling it.
                applyRecipeTypeMode(host, recipeToRun);
            }
        }

        if (!machine.beforeWorking(recipeToRun)) return false;

        var threadChanceCaches = makeChanceCaches();
        ActionResult result = RecipeHelper.handleRecipeIO((IRecipeCapabilityHolder) machine, recipeToRun, IO.IN,
                threadChanceCaches);
        if (result.isSuccess()) {
            GTNARecipeUtils.ActiveRecipe active = new GTNARecipeUtils.ActiveRecipe(
                    recipeToRun,
                    recipeToRun.duration,
                    threadChanceCaches);
            this.activeRecipes.add(active);
            notifyPatternBufferProviders(recipeToRun);
            return true;
        }
        return false;
    }

    /**
     * Zero-energy parallel budget computed directly from what the machine can see, because GTCEu's
     * {@code ParallelLogic.getMaxByInput} returns 0 for vanilla-converted furnace recipes even when
     * the inputs are present.
     */
    private int computeZeroEnergyParallel(GTRecipe recipe, int cap) {
        IRecipeCapabilityHolder holder = (IRecipeCapabilityHolder) machine;
        long parallel = cap;
        for (Content content : recipe.getInputContents(ItemRecipeCapability.CAP)) {
            Ingredient ing = ItemRecipeCapability.CAP.of(content.content);
            if (ing == null || ing.isEmpty()) continue;
            long perCraft = ing instanceof SizedIngredient sized ? Math.max(1, sized.getAmount()) : 1;
            long available = 0;
            for (var hl : holder.getCapabilitiesForIO(IO.IN)) {
                for (var h : hl.getCapability(ItemRecipeCapability.CAP)) {
                    for (Object c : h.getContents()) {
                        if (c instanceof ItemStack stack && !stack.isEmpty() && ing.test(stack)) {
                            available += stack.getCount();
                        }
                    }
                }
            }
            parallel = Math.min(parallel, available / perCraft);
        }
        for (Content content : recipe.getInputContents(FluidRecipeCapability.CAP)) {
            FluidIngredient ing = FluidRecipeCapability.CAP.of(content.content);
            if (ing == null || ing.isEmpty()) continue;
            long perCraft = Math.max(1, ing.getAmount());
            long available = 0;
            for (var hl : holder.getCapabilitiesForIO(IO.IN)) {
                for (var h : hl.getCapability(FluidRecipeCapability.CAP)) {
                    for (Object c : h.getContents()) {
                        if (c instanceof FluidStack stack && !stack.isEmpty() && ing.test(stack)) {
                            available += stack.getAmount();
                        }
                    }
                }
            }
            parallel = Math.min(parallel, available / perCraft);
        }
        return (int) Math.max(0, Math.min(parallel, cap));
    }

    /** Removes every EU content so a zero-energy machine can run an electric recipe for free. */
    private static void stripEnergyContents(GTRecipe recipe) {
        recipe.inputs.remove(EURecipeCapability.CAP);
        recipe.tickInputs.remove(EURecipeCapability.CAP);
        recipe.outputs.remove(EURecipeCapability.CAP);
        recipe.tickOutputs.remove(EURecipeCapability.CAP);
    }

    // ... (Métodos isRecipeAlreadyActive, completeRecipe, getRecipeDisplayInfo, save/load mantidos iguais) ...
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

    /**
     * Mode to mirror onto the controller when this recipe starts: the mode pinned on the
     * pattern-buffer slot that serves it, falling back to the recipe's own type for AUTO slots.
     *
     * @return the mode id to apply, or {@code null} when no buffer and no recipe type provide one
     */
    @Nullable
    private String resolvePatternBufferModeId(GTRecipe recipe) {
        String preferredModeId = null;
        for (IPatternBufferModeProvider provider : getPatternBufferProviders()) {
            String candidate = provider.gtna$getPreferredModeForRecipe(recipe);
            if (candidate != null && !candidate.isBlank()) {
                preferredModeId = candidate;
                break;
            }
        }
        GTRecipeType recipeType = recipe.getType();
        String recipeTypeId = recipeType == null || recipeType.registryName == null ? null :
                recipeType.registryName.toString();
        return PatternBufferModeSelection.select(preferredModeId, recipeTypeId);
    }

    /** Applies the recipe's exact type id as the mirrored mode, when the recipe has one. */
    private void applyRecipeTypeMode(IPatternBufferModeHost host, GTRecipe recipe) {
        GTRecipeType recipeType = recipe.getType();
        if (recipeType != null && recipeType.registryName != null) {
            host.gtna$applyPatternBufferMode(recipeType.registryName.toString(), recipe);
        }
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
            machine.afterWorking();
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
            MutableComponent line1 = Component.translatable("gtna.multiblock.thread_line",
                    i + 1,
                    Component.literal(String.format(Locale.US, "%.1fs / %.1fs ", currentSec, maxSec))
                            .withStyle(ChatFormatting.WHITE),
                    Component.literal(String.format("(%d%%)", percentage)).withStyle(percentColor))
                    .withStyle(ChatFormatting.GOLD);
            infoList.add(line1);
            Component outputComponent = Component.translatable("gtna.multiblock.unknown");
            int totalCount = 1;
            if (active.recipe.outputs.containsKey(ItemRecipeCapability.CAP)) {
                List<Content> itemOutputs = active.recipe.outputs.get(ItemRecipeCapability.CAP);
                if (itemOutputs != null && !itemOutputs.isEmpty()) {
                    Content content = itemOutputs.get(0);
                    Object inner = content.getContent();
                    if (inner instanceof ItemStack stack) {
                        outputComponent = stack.getHoverName();
                        totalCount = stack.getCount();
                    } else if (inner instanceof SizedIngredient sized) {
                        ItemStack[] stacks = sized.getItems();
                        if (stacks.length > 0) outputComponent = stacks[0].getHoverName();
                        totalCount = sized.getAmount();
                    } else if (inner instanceof Ingredient ing) {
                        ItemStack[] stacks = ing.getItems();
                        if (stacks.length > 0) outputComponent = stacks[0].getHoverName();
                    }
                }
            }
            double timePerItem = (maxSec > 0 && totalCount > 0) ? (maxSec / totalCount) : maxSec;
            String outputName = outputComponent.getString();
            String displayName = outputName;
            int maxLength = 20;
            if (displayName.length() > maxLength) {
                displayName = displayName.substring(0, maxLength) + "...";
            }
            MutableComponent line2 = Component.translatable("gtna.multiblock.output_line",
                    Component.literal(displayName)
                            .withStyle(Style.EMPTY.withColor(ChatFormatting.LIGHT_PURPLE).withHoverEvent(
                                    new HoverEvent(HoverEvent.Action.SHOW_TEXT, outputComponent))),
                    totalCount,
                    Component.literal(String.format(Locale.US, " (%.2fs/item)", timePerItem))
                            .withStyle(ChatFormatting.GRAY))
                    .withStyle(ChatFormatting.DARK_GRAY);

            infoList.add(line2);
        }
        return infoList;
    }

    @Override
    public void saveCustomPersistedData(@NotNull CompoundTag tag, boolean forDrop) {
        super.saveCustomPersistedData(tag, forDrop);
        ListTag recipesTag = new ListTag();
        for (GTNARecipeUtils.ActiveRecipe active : activeRecipes) {
            if (active == null || active.recipe == null || active.recipe.id == null) continue;
            CompoundTag activeTag = new CompoundTag();
            activeTag.put("Recipe", GTNAUtil.serializeNBT(active.recipe));
            activeTag.putInt("Progress", active.progress);
            activeTag.putInt("MaxProgress", active.maxProgress);
            recipesTag.add(activeTag);
        }
        tag.put("ActiveRecipes", recipesTag);
    }

    public List<GTNARecipeUtils.ActiveRecipe> getActiveRecipes() {
        return this.activeRecipes;
    }

    @Override
    public void loadCustomPersistedData(@NotNull CompoundTag tag) {
        super.loadCustomPersistedData(tag);
        activeRecipes.clear();
        ListTag recipesTag = tag.getList("ActiveRecipes", Tag.TAG_COMPOUND);
        for (int i = 0; i < recipesTag.size(); i++) {
            CompoundTag activeTag = recipesTag.getCompound(i);
            GTRecipe recipe = GTNAUtil.deserializeNBT(activeTag.get("Recipe"));
            if (recipe == null || !isAllowedRecipeType(recipe, machine.getRecipeTypes())) continue;
            int maxProgress = Math.max(1, activeTag.getInt("MaxProgress"));
            int savedProgress = Math.min(activeTag.getInt("Progress"), maxProgress - 1);
            activeRecipes.add(new GTNARecipeUtils.ActiveRecipe(recipe, savedProgress, maxProgress,
                    makeChanceCaches()));
        }
        updateAggregateState(false, !activeRecipes.isEmpty());
    }
}
