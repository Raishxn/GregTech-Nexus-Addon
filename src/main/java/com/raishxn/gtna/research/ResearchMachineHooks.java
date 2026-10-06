package com.raishxn.gtna.research;

import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Machine events that pay research points, called from GTNA's GTCEu mixins. Both return at once when no
 * pack ships point sources, and both credit the machine's owner (mapped to their team), never whoever is
 * standing nearby. A machine without an owner earns nothing.
 */
public final class ResearchMachineHooks {

    private ResearchMachineHooks() {}

    /** A machine finished one run of {@code recipe}. */
    public static void onRecipeFinished(MetaMachine machine, GTRecipe recipe) {
        var index = ResearchSources.index();
        if (index.isEmpty() || recipe == null || !(machine.getLevel() instanceof ServerLevel level)) return;
        UUID owner = machine.getOwnerUUID();
        if (owner == null) return;
        ResourceLocation type = recipe.recipeType == null ? null : recipe.recipeType.registryName;
        UUID scope = KnowledgeScope.of(owner);
        if (type != null && index.analysisTypes().contains(type)) {
            for (ResourceLocation item : items(recipe.inputs.get(ItemRecipeCapability.CAP))) {
                ResearchSources.analyze(level.getServer(), scope, item, type);
            }
        }
        List<ResearchSource> sources = index.machineRecipe();
        if (sources.isEmpty()) return;
        Set<ResourceLocation> outputs = null;
        for (ResearchSource source : sources) {
            if (source.recipeType().isPresent() && !source.recipeType().get().equals(type)) continue;
            if (source.item().isPresent()) {
                if (outputs == null) outputs = items(recipe.outputs.get(ItemRecipeCapability.CAP));
                if (!outputs.contains(source.item().get())) continue;
            }
            ResearchSources.record(level.getServer(), scope, source, 1);
        }
    }

    /** A multiblock controller formed its structure. */
    public static void onMultiblockFormed(MetaMachine controller) {
        var multiblock = ResearchSources.index().multiblock();
        if (multiblock.isEmpty() || !(controller.getLevel() instanceof ServerLevel level)) return;
        List<ResearchSource> sources = multiblock.get(controller.getDefinition().getId());
        UUID owner = controller.getOwnerUUID();
        if (sources == null || owner == null) return;
        ResearchSources.recordAll(level.getServer(), KnowledgeScope.of(owner), sources, 1);
    }

    private static Set<ResourceLocation> items(List<Content> contents) {
        Set<ResourceLocation> ids = new HashSet<>();
        if (contents == null) return ids;
        for (Content content : contents) {
            if (content.content instanceof Ingredient ingredient) {
                for (ItemStack stack : ingredient.getItems()) {
                    ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
                    if (id != null) ids.add(id);
                }
            }
        }
        return ids;
    }
}
