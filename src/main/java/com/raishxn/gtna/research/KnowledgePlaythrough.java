package com.raishxn.gtna.research;

import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

/**
 * A scripted player that takes the loaded research tree from start to end, so a pack can test its own
 * progression without opening the game. For every node it checks the rules a human would check by hand:
 * the node stays locked when only its trigger item is held out of order, unlocks in order (a node with a cost
 * must be bought: holding its item alone must not unlock it), and every
 * recipe it gates is blocked before and allowed after. Meant for GameTests; it clears the player's
 * inventory and research and restores a clean state when it finishes.
 */
public final class KnowledgePlaythrough {

    /** What the run found. {@code failures} is empty when the whole tree behaves. */
    public record Result(int nodes, int gatedRecipes, List<String> failures) {

        public boolean passed() {
            return failures.isEmpty();
        }
    }

    private KnowledgePlaythrough() {}

    /**
     * @param player a server player (a fake one is fine); its scope is the one tested
     * @param logic  the recipe logic of any machine owned by {@code player}; gates are checked per recipe id,
     *               so the machine type does not matter
     */
    public static Result run(ServerPlayer player, RecipeLogic logic) {
        var server = player.getServer();
        var graph = KnowledgeRegistry.graph();
        var recipes = server.getRecipeManager();
        var scope = KnowledgeScope.of(player);
        List<String> failures = new ArrayList<>();
        int gated = 0;
        try {
            reset(player, scope);
            for (KnowledgeNode node : graph.nodes()) {
                if (node.trigger() instanceof KnowledgeTrigger.ObtainItem obtain && !hasItem(obtain.item())) {
                    failures.add(node.id() + ": trigger item does not exist: " + obtain.item());
                }
                for (KnowledgeGrant grant : node.grants()) {
                    if (grant instanceof KnowledgeGrant.Recipes gate) {
                        for (ResourceLocation id : gate.recipes()) {
                            if (recipes.byKey(id).filter(GTRecipe.class::isInstance).isEmpty()) {
                                failures.add(node.id() + ": gated recipe does not exist: " + id);
                            }
                        }
                    }
                }
            }
            if (!failures.isEmpty()) return new Result(graph.size(), 0, failures);

            // Out of order: holding only a later node's item must not unlock it.
            for (KnowledgeNode node : graph.nodes()) {
                if (node.prerequisites().isEmpty() || !(node.trigger() instanceof KnowledgeTrigger.ObtainItem obtain)) {
                    continue;
                }
                reset(player, scope);
                give(player, obtain.item());
                KnowledgeTriggers.scan(player, ItemStack.EMPTY);
                if (node.purchasable()) {
                    fund(player, scope, node);
                    KnowledgeService.purchase(server, scope, node.id());
                }
                if (KnowledgeService.isUnlocked(server, scope, node.id())) {
                    failures.add(node.id() + ": unlocked out of order, without " + node.prerequisites());
                }
            }

            // In order: one node at a time, checking each recipe gate on both sides of the unlock.
            reset(player, scope);
            for (KnowledgeNode node : graph.nodes()) {
                List<GTRecipe> gates = new ArrayList<>();
                for (KnowledgeGrant grant : node.grants()) {
                    if (grant instanceof KnowledgeGrant.Recipes gate) {
                        gate.recipes().forEach(id -> recipes.byKey(id).filter(GTRecipe.class::isInstance)
                                .ifPresent(recipe -> gates.add((GTRecipe) recipe)));
                    }
                }
                for (GTRecipe recipe : gates) {
                    gated++;
                    if (RecipeHelper.checkConditions(recipe, logic).isSuccess()) {
                        failures.add(node.id() + ": recipe " + recipe.id + " runs before the node is unlocked");
                    }
                }
                if (node.trigger() instanceof KnowledgeTrigger.ObtainItem obtain) {
                    give(player, obtain.item());
                    KnowledgeTriggers.scan(player, ItemStack.EMPTY);
                    if (node.purchasable() && KnowledgeService.isUnlocked(server, scope, node.id())) {
                        failures.add(node.id() + ": has a cost but unlocked by holding its item alone");
                    }
                }
                if (node.purchasable()) {
                    fund(player, scope, node);
                    var bought = KnowledgeService.purchase(server, scope, node.id());
                    if (bought != KnowledgeService.PurchaseResult.PURCHASED) {
                        failures.add(node.id() + ": could not be bought in order: " + bought);
                    }
                } else if (!(node.trigger() instanceof KnowledgeTrigger.ObtainItem)) {
                    KnowledgeService.unlock(server, scope, node.id(), false);
                }
                if (!KnowledgeService.isUnlocked(server, scope, node.id())) {
                    failures.add(node.id() + ": did not unlock in order (trigger " + node.trigger().type() + ")");
                    continue;
                }
                for (GTRecipe recipe : gates) {
                    if (!RecipeHelper.checkConditions(recipe, logic).isSuccess()) {
                        failures.add(node.id() + ": recipe " + recipe.id + " is still blocked after the node unlocked");
                    }
                }
            }
            long locked = graph.nodes().stream().filter(n -> !KnowledgeService.isUnlocked(server, scope, n.id()))
                    .count();
            if (locked > 0) failures.add(locked + " node(s) were still locked at the end of the playthrough");
        } finally {
            reset(player, scope);
        }
        return new Result(graph.size(), gated, failures);
    }

    /** Gives exactly the node's current cost, so a purchase that still fails points at a real rule. */
    private static void fund(ServerPlayer player, java.util.UUID scope, KnowledgeNode node) {
        KnowledgeService.cost(player.getServer(), scope, node)
                .forEach((area, amount) -> KnowledgeService.addPoints(player.getServer(), scope, area, amount));
    }

    private static boolean hasItem(ResourceLocation id) {
        return ForgeRegistries.ITEMS.containsKey(id);
    }

    private static void give(ServerPlayer player, ResourceLocation item) {
        player.getInventory().add(new ItemStack(ForgeRegistries.ITEMS.getValue(item)));
    }

    private static void reset(ServerPlayer player, java.util.UUID scope) {
        player.getInventory().clearContent();
        KnowledgeService.reset(player.getServer(), scope);
    }
}
