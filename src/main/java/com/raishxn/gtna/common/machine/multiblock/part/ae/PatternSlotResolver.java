package com.raishxn.gtna.common.machine.multiblock.part.ae;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.utils.GTMath;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.api.machine.feature.IPatternBufferModeHost;
import com.raishxn.gtna.api.machine.feature.ModeIdMatcher;
import com.raishxn.gtna.common.machine.trait.GTNAMultipleRecipesLogic;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Recipe-resolution and matching core of the ME Pattern Buffer (Fase 3 extraction): per-slot
 * recipe search (controller types first, then the global registry), the cached-recipe
 * preference, the legacy-tag migration, and the slot/recipe matching rules (circuit +
 * specialization consumption, catalyst rejection, keepByProduct output identity).
 *
 * <p>
 * Stateless by design — all persistent state ({@code slotConfigs}, pattern inventory, internal
 * slots) stays in {@link GTNAMEPatternBufferPartMachine} and is read through its accessors.
 * The pure helpers below are static so the matching rules have exactly one definition for
 * every caller (cache resolution, slot routing and the network-output path).
 */
@ParametersAreNonnullByDefault
final class PatternSlotResolver {

    private static final String PATTERN_RECIPE_ID_TAG = "gtnaPatternRecipeId";
    private static final String PATTERN_MODE_ID_TAG = "gtnaPatternModeId";

    private final GTNAMEPatternBufferPartMachine machine;

    PatternSlotResolver(GTNAMEPatternBufferPartMachine machine) {
        this.machine = machine;
    }

    private GTNAPatternBufferSlotConfig config(int slot) {
        return machine.getSlotConfigs()[slot];
    }

    /**
     * Legacy migration: strips the old per-item recipe/mode tags instead of loading them.
     * Slot state now lives exclusively in {@code slotConfigs}; tags on the pattern item were
     * a redundant second source of truth that carried ghost state across buffers.
     */
    void loadPatternRecipeMetadata(int slot, ItemStack pattern) {
        if (slot < 0 || slot >= machine.getSlotConfigs().length || pattern.isEmpty() || !pattern.hasTag()) {
            return;
        }
        CompoundTag tag = pattern.getTag();
        if (tag != null && (tag.contains(PATTERN_RECIPE_ID_TAG, Tag.TAG_STRING) ||
                tag.contains(PATTERN_MODE_ID_TAG, Tag.TAG_STRING))) {
            tag.remove(PATTERN_RECIPE_ID_TAG);
            tag.remove(PATTERN_MODE_ID_TAG);
        }
    }

    void clearPatternRecipeMetadata(int slot) {
        if (slot < 0 || slot >= machine.getPatternInventory().getSlots()) {
            return;
        }
        ItemStack pattern = machine.getPatternInventory().getStackInSlot(slot);
        if (pattern.isEmpty() || !pattern.hasTag()) {
            return;
        }
        CompoundTag tag = pattern.getOrCreateTag();
        tag.remove(PATTERN_RECIPE_ID_TAG);
        tag.remove(PATTERN_MODE_ID_TAG);
    }

    void cacheResolvedRecipe(int slot, GTRecipe recipe) {
        if (slot < 0 || slot >= machine.getSlotConfigs().length || recipe.id == null) {
            return;
        }
        GTNAPatternBufferSlotConfig config = config(slot);
        // cacheRecipe toggle (GTLCore parity): a slot may opt out of recipe caching entirely.
        if (!config.isCacheRecipe()) {
            return;
        }
        config.setCachedRecipeId(recipe.id.toString());
        String resolvedMode = config.getPreferredModeId().isBlank() ? resolveDerivedMode(recipe) :
                config.getPreferredModeId();
        if (config.getPreferredModeId().isBlank()) {
            config.setDerivedModeId(resolvedMode == null ? "" : resolvedMode);
        }
        // Single source of truth: slotConfigs. The pattern ItemStack is no longer mutated
        // with recipe/mode tags (that produced ghost state when a pattern moved buffers).
    }

    void resolveAndCacheSlotRecipe(int slot) {
        if (slot < 0 || slot >= machine.getMaxPatternCount() || machine.isRemote()) {
            return;
        }
        ItemStack pattern = machine.getPatternInventory().getStackInSlot(slot);
        if (pattern.isEmpty()) {
            return;
        }

        IRecipeCapabilityHolder holder = null;
        GTRecipeType[] recipeTypes = new GTRecipeType[0];
        if (machine.isFormed() && !machine.getControllers().isEmpty()) {
            IMultiController controller = machine.getControllers().first();
            if (controller instanceof IRecipeLogicMachine recipeMachine &&
                    controller instanceof IRecipeCapabilityHolder controllerHolder) {
                holder = controllerHolder;
                recipeTypes = recipeMachine.getRecipeTypes();
                if (recipeTypes == null || recipeTypes.length == 0) {
                    recipeTypes = new GTRecipeType[] { recipeMachine.getRecipeType() };
                }
            }
        }

        if (holder == null &&
                machine instanceof IRecipeCapabilityHolder selfHolder) {
            holder = selfHolder;
        }
        if (holder == null) {
            return;
        }

        GTRecipeType[] searchTypes = getRecipeTypesForSlotSearch(slot, recipeTypes);
        GTRecipe resolved = findPatternResolvedRecipeForSlot(slot, holder, searchTypes);
        if (resolved == null) {
            resolved = findResolvedRecipeForSlot(slot, holder, searchTypes);
        }
        if (resolved == null) {
            GTRecipeType[] fallbackTypes = getGlobalRecipeTypesForSlotSearch(slot, searchTypes);
            resolved = findPatternResolvedRecipeForSlot(slot, holder, fallbackTypes);
            if (resolved == null) {
                resolved = findResolvedRecipeForSlot(slot, holder, fallbackTypes);
            }
        }
        if (resolved != null) {
            GTNAPatternBufferSlotConfig config = config(slot);
            if (!config.getPreferredModeId().isBlank() && !matchesModeId(config.getPreferredModeId(), resolved)) {
                GTNACORE.LOGGER.warn(
                        "[GTNA][PatternBuffer] slot={} clearing stale preferred mode {} because detected recipe {} belongs to {}",
                        slot,
                        config.getPreferredModeId(),
                        resolved.id,
                        resolved.getType() == null || resolved.getType().registryName == null ? "unknown" :
                                resolved.getType().registryName);
                config.setPreferredModeId("");
            }
            cacheResolvedRecipe(slot, resolved);
            GTNACORE.LOGGER.info("[GTNA][PatternBuffer] slot={} detected recipe={} mode={} preferred={}",
                    slot,
                    resolved.id,
                    config(slot).getDerivedModeId(),
                    config(slot).getPreferredModeId());
            syncSingleRecipeMachineMode(slot, resolved);
        } else {
            logPatternDetectionFailure(slot, pattern, searchTypes);
        }
    }

    /**
     * Vanilla GTCEu recipe logic has one active recipe type and therefore needs
     * the controller switched before a pattern can run. GTNA's threaded logic
     * intentionally does not: each active recipe keeps its own recipe type.
     */
    void syncSingleRecipeMachineMode(int slot, GTRecipe recipe) {
        if (!machine.isFormed() || machine.getControllers().isEmpty()) {
            return;
        }
        IMultiController controller = machine.getControllers().first();
        if (!(controller instanceof IRecipeLogicMachine recipeMachine) ||
                recipeMachine.getRecipeLogic() instanceof GTNAMultipleRecipesLogic) {
            return;
        }

        GTNAPatternBufferSlotConfig config = config(slot);
        String modeId = config.getPreferredModeId().isBlank() ? config.getDerivedModeId() :
                config.getPreferredModeId();
        if (modeId == null || modeId.isBlank()) {
            return;
        }
        GTRecipeType[] recipeTypes = recipeMachine.getRecipeTypes();
        if (recipeTypes == null || recipeTypes.length <= 1) {
            return;
        }
        for (int i = 0; i < recipeTypes.length; i++) {
            if (modeMatches(modeId, recipeTypes[i])) {
                if (recipeMachine.getActiveRecipeType() != i) {
                    recipeMachine.setActiveRecipeType(i);
                }
                return;
            }
        }
    }

    GTRecipeType[] getRecipeTypesForSlotSearch(int slot, GTRecipeType[] recipeTypes) {
        if (recipeTypes == null || recipeTypes.length == 0) {
            return recipeTypes;
        }
        GTNAPatternBufferSlotConfig config = config(slot);
        if (config.getPreferredModeId().isBlank() || recipeTypes.length <= 1) {
            return recipeTypes;
        }
        List<GTRecipeType> ordered = new ArrayList<>(recipeTypes.length);
        for (GTRecipeType recipeType : recipeTypes) {
            if (modeMatches(config.getPreferredModeId(), recipeType)) {
                ordered.add(recipeType);
            }
        }
        return ordered.isEmpty() ? recipeTypes : ordered.toArray(GTRecipeType[]::new);
    }

    GTRecipeType[] getGlobalRecipeTypesForSlotSearch(int slot, GTRecipeType[] alreadySearched) {
        GTNAPatternBufferSlotConfig config = config(slot);
        List<GTRecipeType> ordered = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();

        if (alreadySearched != null) {
            for (GTRecipeType recipeType : alreadySearched) {
                if (recipeType != null && recipeType.registryName != null) {
                    seen.add(recipeType.registryName.toString());
                }
            }
        }

        for (GTRecipeType recipeType : GTRegistries.RECIPE_TYPES) {
            if (recipeType == null || recipeType.registryName == null) {
                continue;
            }
            String id = recipeType.registryName.toString();
            if (seen.contains(id)) {
                continue;
            }
            if (!config.getPreferredModeId().isBlank() && !modeMatches(config.getPreferredModeId(), recipeType)) {
                continue;
            }
            ordered.add(recipeType);
        }
        return ordered.toArray(GTRecipeType[]::new);
    }

    @Nullable
    GTRecipe findResolvedRecipeForSlot(int slot, IRecipeCapabilityHolder holder, GTRecipeType[] recipeTypes) {
        String cachedRecipeId = config(slot).getCachedRecipeId();
        IPatternDetails details = getPatternDetailsForSlot(slot);
        GTRecipe fallback = null;
        for (GTRecipeType recipeType : recipeTypes) {
            if (recipeType == null) {
                continue;
            }
            var iterator = recipeType.searchRecipe(holder, recipe -> true);
            int searchLimit = 256;
            while (iterator.hasNext() && searchLimit-- > 0) {
                GTRecipe recipe = iterator.next();
                // An input-only match may resolve a different recipe type that consumes the same
                // ingredients but produces another output. Never cache that as the AE2 pattern's
                // recipe: the machine would keep the wrong mode and could consume the request.
                if (recipe == null || details == null || !matchesPatternDetails(slot, recipe, details) ||
                        !matchesSlot(slot, recipe)) {
                    continue;
                }
                if (recipe.id != null && recipe.id.toString().equals(cachedRecipeId)) {
                    return recipe;
                }
                if (fallback == null) {
                    fallback = recipe;
                }
            }
        }
        return fallback;
    }

    @Nullable
    GTRecipe findPatternResolvedRecipeForSlot(int slot, IRecipeCapabilityHolder holder,
                                              GTRecipeType[] recipeTypes) {
        ItemStack pattern = machine.getPatternInventory().getStackInSlot(slot);
        IPatternDetails details = PatternDetailsHelper.decodePattern(pattern, machine.getLevel());
        if (details == null || machine.getLevel() == null) {
            GTNACORE.LOGGER.info("[GTNA][PatternBuffer] slot={} failed to decode pattern item={}",
                    slot, pattern.getItem().getDescriptionId());
            return null;
        }
        String cachedRecipeId = config(slot).getCachedRecipeId();
        GTRecipe fallback = null;
        int scanned = 0;
        for (GTRecipeType recipeType : recipeTypes) {
            if (recipeType == null) {
                continue;
            }
            int searchLimit = 512;
            for (GTRecipe recipe : machine.getLevel().getRecipeManager().getAllRecipesFor(recipeType)) {
                if (searchLimit-- <= 0) {
                    break;
                }
                scanned++;
                if (recipe == null || !matchesPatternDetails(slot, recipe, details)) {
                    continue;
                }
                if (recipe.id != null && recipe.id.toString().equals(cachedRecipeId)) {
                    GTNACORE.LOGGER.info(
                            "[GTNA][PatternBuffer] slot={} matched cached recipe={} after scanning {} recipes",
                            slot, recipe.id, scanned);
                    return recipe;
                }
                if (fallback == null) {
                    fallback = recipe;
                }
            }
        }
        if (fallback != null) {
            GTNACORE.LOGGER.info("[GTNA][PatternBuffer] slot={} matched recipe={} after scanning {} recipes",
                    slot, fallback.id, scanned);
        } else {
            GTNACORE.LOGGER.info(
                    "[GTNA][PatternBuffer] slot={} scanned {} recipes but found no match for pattern inputs={} fluids={} outputs={} fluidOutputs={}",
                    slot,
                    scanned,
                    summarizeItemStacks(collectPatternItemInputs(details)),
                    summarizeFluidStacks(collectPatternFluidInputs(details)),
                    summarizeItemStacks(collectPatternItemOutputs(details)),
                    summarizeFluidStacks(collectPatternFluidOutputs(details)));
        }
        return fallback;
    }

    void logPatternDetectionFailure(int slot, ItemStack pattern, GTRecipeType[] recipeTypes) {
        List<String> typeIds = new ArrayList<>();
        if (recipeTypes != null) {
            for (GTRecipeType recipeType : recipeTypes) {
                if (recipeType != null && recipeType.registryName != null) {
                    typeIds.add(recipeType.registryName.toString());
                }
            }
        }
        GTNACORE.LOGGER.warn(
                "[GTNA][PatternBuffer] slot={} no mode detected for pattern={} preferred={} cachedRecipe={} triedTypes={}",
                slot,
                pattern.getItem().getDescriptionId(),
                config(slot).getPreferredModeId(),
                config(slot).getCachedRecipeId(),
                typeIds);
    }

    @Nullable
    String resolveDerivedMode(GTRecipe recipe) {
        if (!machine.isFormed() || machine.getControllers().isEmpty()) {
            return null;
        }
        IMultiController controller = machine.getControllers().first();
        if (controller instanceof IPatternBufferModeHost host) {
            return host.gtna$resolvePatternBufferMode(recipe);
        }
        if (controller instanceof IRecipeLogicMachine recipeMachine &&
                recipeMachine.getRecipeTypes() != null &&
                recipeMachine.getRecipeTypes().length > 1 &&
                recipe.getType() != null &&
                recipe.getType().registryName != null) {
            return recipe.getType().registryName.toString();
        }
        return null;
    }

    @Nullable
    SlotMatch findMatchingSlot(GTRecipe recipe) {
        String recipeId = recipe.id == null ? "" : recipe.id.toString();
        for (int i = 0; i < machine.getMaxPatternCount(); i++) {
            GTNAPatternBufferSlotConfig config = config(i);
            if (!recipeId.isBlank() && recipeId.equals(config.getCachedRecipeId())) {
                if (!machine.gtna$slotAcceptsRecipe(i, recipe)) {
                    continue;
                }
                IPatternDetails details = getPatternDetailsForSlot(i);
                if ((details != null && matchesPatternDetails(i, recipe, details)) || matchesSlot(i, recipe)) {
                    return new SlotMatch(i);
                }
            }
        }
        for (int i = 0; i < machine.getMaxPatternCount(); i++) {
            GTNAPatternBufferSlotConfig config = config(i);
            if (!config.getPreferredModeId().isBlank() && !matchesPreferredMode(config, recipe)) {
                continue;
            }
            IPatternDetails details = getPatternDetailsForSlot(i);
            if (details != null && matchesPatternDetails(i, recipe, details)) {
                return new SlotMatch(i);
            }
            if (matchesSlot(i, recipe)) {
                return new SlotMatch(i);
            }
        }
        return null;
    }

    @Nullable
    IPatternDetails getPatternDetailsForSlot(int slot) {
        if (slot < 0 || slot >= machine.getPatternInventory().getSlots()) {
            return null;
        }
        ItemStack pattern = machine.getPatternInventory().getStackInSlot(slot);
        if (pattern.isEmpty()) {
            return null;
        }
        return PatternDetailsHelper.decodePattern(pattern, machine.getLevel());
    }

    boolean matchesSlot(int slotIndex, GTRecipe recipe) {
        List<Ingredient> itemInputs = copyItemInputs(recipe);
        List<FluidIngredient> fluidInputs = copyFluidInputs(recipe);
        itemInputs = machine.consumeCircuitInventory(itemInputs);
        itemInputs = consumeVirtualItems(config(slotIndex), itemInputs);
        fluidInputs = consumeVirtualFluids(config(slotIndex), fluidInputs);
        itemInputs = machine.getInternalInventory()[slotIndex].handleItemInternal(itemInputs, true);
        fluidInputs = machine.getInternalInventory()[slotIndex].handleFluidInternal(fluidInputs, true);
        boolean itemsMatched = itemInputs == null || itemInputs.isEmpty();
        boolean fluidsMatched = fluidInputs == null || fluidInputs.isEmpty();
        return itemsMatched && fluidsMatched;
    }

    boolean matchesPatternDetails(int slotIndex, GTRecipe recipe, IPatternDetails details) {
        // The machine may pass a copy multiplied for parallel execution (and modified by other
        // hatches) to the buffer's input handlers. The encoded AE2 pattern describes ONE craft.
        // Compare its identity with the registered recipe, while RecipeHelper still consumes the
        // scaled recipe against the actual staged quantities.
        if (recipe.id != null && machine.getLevel() != null) {
            var registered = machine.getLevel().getRecipeManager().byKey(recipe.id);
            if (registered.isPresent() && registered.get() instanceof GTRecipe base &&
                    base.getType() == recipe.getType()) {
                recipe = base;
            }
        }
        List<Ingredient> itemInputs = copyItemInputs(recipe);
        List<FluidIngredient> fluidInputs = copyFluidInputs(recipe);
        List<ItemStack> itemOutputs = copyItemOutputs(recipe);
        List<FluidStack> fluidOutputs = copyFluidOutputs(recipe);

        GTNAPatternBufferSlotConfig config = config(slotIndex);
        itemInputs = consumeConfiguredCircuit(config, itemInputs);
        itemInputs = machine.consumeCircuitInventory(itemInputs);
        itemInputs = consumeVirtualItems(config, itemInputs);
        fluidInputs = consumeVirtualFluids(config, fluidInputs);
        itemInputs = consumePatternItems(collectPatternItemInputs(details), itemInputs);
        fluidInputs = consumePatternFluids(collectPatternFluidInputs(details), fluidInputs);
        // Catalyst semantics (GTLCore parity): if the slot defines catalysts and the recipe
        // consumes one of them without the slot's actual contents covering it, the recipe is
        // rejected for this slot — the buffer never picks a recipe that "eats" a catalyst.
        if (!testCatalystItems(config, recipe)) return false;
        if (!testCatalystFluids(config, recipe)) return false;

        boolean inputsMatched = (itemInputs == null || itemInputs.isEmpty()) &&
                (fluidInputs == null || fluidInputs.isEmpty());
        if (!inputsMatched) {
            return false;
        }

        List<ItemStack> patternItemOutputs = collectPatternItemOutputs(details);
        List<FluidStack> patternFluidOutputs = collectPatternFluidOutputs(details);
        // keepByProduct == false (GTLCore default): secondary byproducts are not part of the
        // identity check, so only the primary output of each kind is compared.
        if (!machine.isKeepByProduct()) {
            itemOutputs = primaryOnly(itemOutputs);
            patternItemOutputs = primaryOnly(patternItemOutputs);
            fluidOutputs = primaryOnly(fluidOutputs);
            patternFluidOutputs = primaryOnly(patternFluidOutputs);
        }
        return compareItemStacks(itemOutputs, patternItemOutputs) &&
                compareFluidStacks(fluidOutputs, patternFluidOutputs);
    }

    static boolean matchesPreferredMode(GTNAPatternBufferSlotConfig config, GTRecipe recipe) {
        return matchesModeId(config.getPreferredModeId(), recipe);
    }

    static boolean matchesDerivedMode(GTNAPatternBufferSlotConfig config, GTRecipe recipe) {
        return matchesModeId(config.getDerivedModeId(), recipe);
    }

    static boolean matchesModeId(String modeId, GTRecipe recipe) {
        if (modeId == null || modeId.isBlank() || recipe.getType() == null || recipe.getType().registryName == null) {
            return false;
        }
        return modeMatches(modeId, recipe.getType());
    }

    static boolean modeMatches(String modeId, @Nullable GTRecipeType recipeType) {
        return ModeIdMatcher.matches(modeId, recipeType);
    }

    static <T> List<T> primaryOnly(List<T> stacks) {
        return stacks.size() <= 1 ? stacks : List.of(stacks.get(0));
    }

    static List<Ingredient> copyItemInputs(GTRecipe recipe) {
        List<Ingredient> copied = new ArrayList<>();
        for (Content content : recipe.getInputContents(ItemRecipeCapability.CAP)) {
            Object inner = content.getContent();
            if (inner instanceof Ingredient ingredient) {
                copied.add(SizedIngredient.copy(ingredient));
            } else if (inner instanceof ItemStack stack && !stack.isEmpty()) {
                copied.add(SizedIngredient.create(stack.copy()));
            }
        }
        return copied;
    }

    static List<ItemStack> copyItemOutputs(GTRecipe recipe) {
        List<ItemStack> copied = new ArrayList<>();
        for (Content content : recipe.getOutputContents(ItemRecipeCapability.CAP)) {
            Object inner = content.getContent();
            if (inner instanceof ItemStack stack && !stack.isEmpty()) {
                copied.add(stack.copy());
            } else if (inner instanceof Ingredient ingredient) {
                ItemStack[] items = ingredient.getItems();
                if (items.length > 0 && !items[0].isEmpty()) {
                    copied.add(items[0].copy());
                }
            }
        }
        return copied;
    }

    static List<FluidIngredient> copyFluidInputs(GTRecipe recipe) {
        List<FluidIngredient> copied = new ArrayList<>();
        for (Content content : recipe.getInputContents(FluidRecipeCapability.CAP)) {
            Object inner = content.getContent();
            if (inner instanceof FluidIngredient ingredient) {
                copied.add(ingredient.copy());
            } else if (inner instanceof FluidStack stack && !stack.isEmpty()) {
                copied.add(FluidIngredient.of(stack.copy()));
            }
        }
        return copied;
    }

    static List<FluidStack> copyFluidOutputs(GTRecipe recipe) {
        List<FluidStack> copied = new ArrayList<>();
        for (Content content : recipe.getOutputContents(FluidRecipeCapability.CAP)) {
            Object inner = content.getContent();
            if (inner instanceof FluidIngredient ingredient) {
                FluidStack[] stacks = ingredient.getStacks();
                if (stacks.length > 0 && !stacks[0].isEmpty()) {
                    copied.add(stacks[0].copy());
                }
            } else if (inner instanceof FluidStack stack && !stack.isEmpty()) {
                copied.add(stack.copy());
            }
        }
        return copied;
    }

    static List<Ingredient> consumeConfiguredCircuit(GTNAPatternBufferSlotConfig config, List<Ingredient> left) {
        if (left == null || left.isEmpty()) {
            return left;
        }
        ItemStack circuitStack = config.getCircuitStack();
        if (circuitStack == null || circuitStack.isEmpty()) {
            return left;
        }
        return consumeVirtualItemList(List.of(circuitStack), left);
    }

    static List<Ingredient> consumeVirtualItems(GTNAPatternBufferSlotConfig config, List<Ingredient> left) {
        if (left == null || left.isEmpty()) {
            return left;
        }
        return consumeVirtualItemList(config.getVirtualItemStacks(), left);
    }

    static List<Ingredient> consumeVirtualItemList(List<ItemStack> virtualStacks, List<Ingredient> left) {
        if (left == null || left.isEmpty()) {
            return left;
        }
        if (virtualStacks.isEmpty()) {
            return left;
        }
        for (var it = left.listIterator(); it.hasNext();) {
            Ingredient ingredient = it.next();
            if (ingredient == null || ingredient.isEmpty()) {
                it.remove();
                continue;
            }
            int amountLeft = extractAmount(ingredient);
            if (amountLeft <= 0) {
                it.remove();
                continue;
            }
            for (ItemStack stack : virtualStacks) {
                if (stack.isEmpty() || !ingredient.test(stack)) {
                    continue;
                }
                amountLeft -= stack.getCount();
                if (amountLeft <= 0) {
                    it.remove();
                    break;
                }
            }
            if (amountLeft > 0) {
                applyAmount(ingredient, amountLeft);
            }
        }
        return left.isEmpty() ? null : left;
    }

    static List<Ingredient> consumePatternItems(List<ItemStack> patternItems, List<Ingredient> left) {
        return consumeVirtualItemList(patternItems, left);
    }

    /**
     * GTLCore {@code testCatalystItemInternal} parity: returns {@code false} when any recipe input
     * could consume a catalyst key configured for the slot. The per-ingredient entries guard
     * {@code content.chance <= 0} (crafting-only outputs) the same way the reference does.
     */
    static boolean testCatalystItems(GTNAPatternBufferSlotConfig config, GTRecipe recipe) {
        List<ItemStack> catalysts = config.getCatalystItemStacks();
        if (catalysts.isEmpty()) return true;
        for (Content content : recipe.getInputContents(ItemRecipeCapability.CAP)) {
            if (content.chance <= 0) continue;
            if (!(content.getContent() instanceof Ingredient ingredient)) continue;
            for (ItemStack catalyst : catalysts) {
                if (!catalyst.isEmpty() && ingredient.test(catalyst)) return false;
            }
        }
        return true;
    }

    /**
     * GTLCore {@code testCatalystFluidInternal} parity.
     */
    static boolean testCatalystFluids(GTNAPatternBufferSlotConfig config, GTRecipe recipe) {
        List<FluidStack> catalysts = config.getCatalystFluidStacks();
        if (catalysts.isEmpty()) return true;
        for (Content content : recipe.getInputContents(FluidRecipeCapability.CAP)) {
            if (content.chance <= 0) continue;
            if (!(content.getContent() instanceof FluidIngredient fluidIngredient)) continue;
            for (FluidStack catalyst : catalysts) {
                if (!catalyst.isEmpty() && fluidIngredient.test(catalyst)) return false;
            }
        }
        return true;
    }

    static List<FluidIngredient> consumeVirtualFluids(GTNAPatternBufferSlotConfig config,
                                                      List<FluidIngredient> left) {
        if (left == null || left.isEmpty()) {
            return left;
        }
        List<FluidStack> configuredFluids = config.getVirtualFluidStacks();
        if (configuredFluids.isEmpty()) {
            return left;
        }
        for (var it = left.listIterator(); it.hasNext();) {
            FluidIngredient ingredient = it.next();
            if (ingredient == null || ingredient.isEmpty()) {
                it.remove();
                continue;
            }
            int amountLeft = ingredient.getAmount();
            for (FluidStack configuredFluid : configuredFluids) {
                if (configuredFluid.isEmpty() || !ingredient.test(configuredFluid)) {
                    continue;
                }
                amountLeft -= configuredFluid.getAmount();
                if (amountLeft <= 0) {
                    break;
                }
            }
            if (amountLeft <= 0) {
                it.remove();
            } else {
                ingredient.setAmount(amountLeft);
            }
        }
        return left.isEmpty() ? null : left;
    }

    static List<FluidIngredient> consumePatternFluids(List<FluidStack> patternFluids, List<FluidIngredient> left) {
        if (left == null || left.isEmpty() || patternFluids.isEmpty()) {
            return left;
        }
        for (var it = left.listIterator(); it.hasNext();) {
            FluidIngredient ingredient = it.next();
            if (ingredient == null || ingredient.isEmpty()) {
                it.remove();
                continue;
            }
            int amountLeft = ingredient.getAmount();
            for (FluidStack patternFluid : patternFluids) {
                if (patternFluid.isEmpty() || !ingredient.test(patternFluid)) {
                    continue;
                }
                amountLeft -= patternFluid.getAmount();
                if (amountLeft <= 0) {
                    break;
                }
            }
            if (amountLeft <= 0) {
                it.remove();
            } else {
                ingredient.setAmount(amountLeft);
            }
        }
        return left.isEmpty() ? null : left;
    }

    static List<ItemStack> collectPatternItemInputs(IPatternDetails details) {
        List<ItemStack> items = new ArrayList<>();
        for (IPatternDetails.IInput input : details.getInputs()) {
            if (input == null) {
                continue;
            }
            GenericStack selected = null;
            for (GenericStack candidate : input.getPossibleInputs()) {
                if (candidate != null && candidate.what() instanceof AEItemKey) {
                    selected = candidate;
                    break;
                }
            }
            if (selected == null || !(selected.what() instanceof AEItemKey itemKey)) {
                continue;
            }
            long amount = selected.amount() * Math.max(1L, input.getMultiplier());
            ItemStack stack = itemKey.toStack(GTMath.saturatedCast(amount));
            if (!stack.isEmpty()) {
                items.add(stack);
            }
        }
        return items;
    }

    static List<FluidStack> collectPatternFluidInputs(IPatternDetails details) {
        List<FluidStack> fluids = new ArrayList<>();
        for (IPatternDetails.IInput input : details.getInputs()) {
            if (input == null) {
                continue;
            }
            GenericStack selected = null;
            for (GenericStack candidate : input.getPossibleInputs()) {
                if (candidate != null && candidate.what() instanceof AEFluidKey) {
                    selected = candidate;
                    break;
                }
            }
            if (selected == null || !(selected.what() instanceof AEFluidKey fluidKey)) {
                continue;
            }
            long amount = selected.amount() * Math.max(1L, input.getMultiplier());
            FluidStack stack = fluidKey.toStack(GTMath.saturatedCast(amount));
            if (!stack.isEmpty()) {
                fluids.add(stack);
            }
        }
        return fluids;
    }

    static List<ItemStack> collectPatternItemOutputs(IPatternDetails details) {
        List<ItemStack> items = new ArrayList<>();
        for (GenericStack output : details.getOutputs()) {
            if (output == null || !(output.what() instanceof AEItemKey itemKey)) {
                continue;
            }
            ItemStack stack = itemKey.toStack(GTMath.saturatedCast(output.amount()));
            if (!stack.isEmpty()) {
                items.add(stack);
            }
        }
        return items;
    }

    static List<FluidStack> collectPatternFluidOutputs(IPatternDetails details) {
        List<FluidStack> fluids = new ArrayList<>();
        for (GenericStack output : details.getOutputs()) {
            if (output == null || !(output.what() instanceof AEFluidKey fluidKey)) {
                continue;
            }
            FluidStack stack = fluidKey.toStack(GTMath.saturatedCast(output.amount()));
            if (!stack.isEmpty()) {
                fluids.add(stack);
            }
        }
        return fluids;
    }

    static boolean compareItemStacks(List<ItemStack> expected, List<ItemStack> actual) {
        if (expected.size() != actual.size()) {
            return false;
        }
        List<ItemStack> remaining = new ArrayList<>();
        for (ItemStack stack : actual) {
            if (!stack.isEmpty()) {
                remaining.add(stack.copy());
            }
        }
        for (ItemStack expectedStack : expected) {
            if (expectedStack.isEmpty()) {
                continue;
            }
            boolean matched = false;
            for (var it = remaining.listIterator(); it.hasNext();) {
                ItemStack actualStack = it.next();
                if (!ItemStack.isSameItemSameTags(expectedStack, actualStack)) {
                    continue;
                }
                if (actualStack.getCount() != expectedStack.getCount()) {
                    continue;
                }
                it.remove();
                matched = true;
                break;
            }
            if (!matched) {
                return false;
            }
        }
        return remaining.isEmpty();
    }

    static boolean compareFluidStacks(List<FluidStack> expected, List<FluidStack> actual) {
        if (expected.size() != actual.size()) {
            return false;
        }
        List<FluidStack> remaining = new ArrayList<>();
        for (FluidStack stack : actual) {
            if (!stack.isEmpty()) {
                remaining.add(stack.copy());
            }
        }
        for (FluidStack expectedStack : expected) {
            if (expectedStack.isEmpty()) {
                continue;
            }
            boolean matched = false;
            for (var it = remaining.listIterator(); it.hasNext();) {
                FluidStack actualStack = it.next();
                if (!actualStack.isFluidEqual(expectedStack)) {
                    continue;
                }
                if (actualStack.getAmount() != expectedStack.getAmount()) {
                    continue;
                }
                it.remove();
                matched = true;
                break;
            }
            if (!matched) {
                return false;
            }
        }
        return remaining.isEmpty();
    }

    static String summarizeItemStacks(List<ItemStack> stacks) {
        if (stacks == null || stacks.isEmpty()) {
            return "[]";
        }
        List<String> summary = new ArrayList<>();
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty()) {
                summary.add(stack.getCount() + "x" + stack.getItem().getDescriptionId());
            }
        }
        return summary.toString();
    }

    static String summarizeFluidStacks(List<FluidStack> stacks) {
        if (stacks == null || stacks.isEmpty()) {
            return "[]";
        }
        List<String> summary = new ArrayList<>();
        for (FluidStack stack : stacks) {
            if (!stack.isEmpty()) {
                String fluidId = stack.getFluid().getFluidType().toString();
                summary.add(stack.getAmount() + "mb:" + fluidId);
            }
        }
        return summary.toString();
    }

    static int extractAmount(Ingredient ingredient) {
        if (ingredient instanceof SizedIngredient sizedIngredient) {
            return sizedIngredient.getAmount();
        }
        ItemStack[] items = ingredient.getItems();
        return items.length > 0 ? items[0].getCount() : 0;
    }

    static void applyAmount(Ingredient ingredient, int amount) {
        if (ingredient instanceof SizedIngredient sizedIngredient) {
            sizedIngredient.setAmount(amount);
            return;
        }
        ItemStack[] items = ingredient.getItems();
        if (items.length > 0) {
            items[0].setCount(amount);
        }
    }

    record SlotMatch(int slot) {}
}
