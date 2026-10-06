package com.raishxn.gtna.gametest;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.registries.ForgeRegistries;

import com.mojang.authlib.GameProfile;
import com.raishxn.gtna.research.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Optional FTB Teams test body: research progress and machine gates must follow the effective party. */
final class FTBResearchTeamTests {

    @SuppressWarnings("unchecked")
    static void run(GameTestHelper h) throws ReflectiveOperationException {
        var manager = (dev.ftb.mods.ftbteams.data.TeamManagerImpl) dev.ftb.mods.ftbteams.api.FTBTeamsAPI.api()
                .getManager();
        var knownField = manager.getClass().getDeclaredField("knownPlayers");
        knownField.setAccessible(true);
        var known = (java.util.Map<UUID, dev.ftb.mods.ftbteams.api.Team>) knownField.get(manager);
        UUID firstId = UUID.randomUUID(), secondId = UUID.randomUUID(), outsiderId = UUID.randomUUID();
        var party = new dev.ftb.mods.ftbteams.data.PartyTeam(manager, UUID.randomUUID());
        var firstTeam = new dev.ftb.mods.ftbteams.data.PlayerTeam(manager, firstId);
        var secondTeam = new dev.ftb.mods.ftbteams.data.PlayerTeam(manager, secondId);
        var outsideTeam = new dev.ftb.mods.ftbteams.data.PlayerTeam(manager, outsiderId);
        firstTeam.setEffectiveTeam(party);
        secondTeam.setEffectiveTeam(party);
        known.put(firstId, firstTeam);
        known.put(secondId, secondTeam);
        known.put(outsiderId, outsideTeam);

        var server = h.getLevel().getServer();
        var first = FakePlayerFactory.get(h.getLevel(), new GameProfile(firstId, "TeamFirst"));
        var member = FakePlayerFactory.get(h.getLevel(), new GameProfile(secondId, "TeamMember"));
        var outsider = FakePlayerFactory.get(h.getLevel(), new GameProfile(outsiderId, "TeamOutsider"));
        var recipes = h.getLevel().getRecipeManager().getAllRecipesFor(GTRecipeTypes.MACERATOR_RECIPES);
        var gated = recipes.get(0);
        var node = new ResourceLocation("gtna_test", "team_node");
        var item = ForgeRegistries.ITEMS.getKey(Items.AMETHYST_SHARD);
        var previous = KnowledgeRegistry.graph();
        var pos = new BlockPos(1, 2, 1);
        h.setBlock(pos, GTMachines.MACERATOR[GTValues.LV].getBlock());
        var machine = ((MetaMachineBlockEntity) h.getBlockEntity(pos)).getMetaMachine();
        var logic = ((IRecipeLogicMachine) machine).getRecipeLogic();
        try {
            KnowledgeRegistry.set(KnowledgeGraph.build(List.of(new KnowledgeNode(node, 1, Optional.empty(), List.of(),
                    new KnowledgeTrigger.ObtainItem(item), List.of(new KnowledgeGrant.Recipes(List.of(gated.id))),
                    Optional.empty()))).graph());

            h.assertTrue(
                    KnowledgeScope.of(first).equals(party.getId()) && KnowledgeScope.of(member).equals(party.getId()),
                    "party members share one research scope");
            h.assertTrue(!KnowledgeScope.of(outsider).equals(party.getId()), "an outsider has a scope of its own");

            machine.setOwnerUUID(secondId);
            h.assertTrue(!RecipeHelper.checkConditions(gated, logic).isSuccess(), "blocked before anyone unlocks");

            // One member finds the item; the whole party gets the node.
            first.getInventory().clearContent();
            first.getInventory().add(new ItemStack(Items.AMETHYST_SHARD));
            h.assertTrue(KnowledgeTriggers.scan(first, ItemStack.EMPTY) == 1, "the first member unlocks the node");
            h.assertTrue(KnowledgeService.isUnlocked(server, KnowledgeScope.of(member), node),
                    "the second member sees the party's unlock");
            h.assertTrue(RecipeHelper.checkConditions(gated, logic).isSuccess(),
                    "a machine owned by a party member must run a recipe the party unlocked");

            machine.setOwnerUUID(outsiderId);
            h.assertTrue(!RecipeHelper.checkConditions(gated, logic).isSuccess(),
                    "an outsider's machine must stay blocked");

            machine.setOwnerUUID(secondId);
            secondTeam.setEffectiveTeam(secondTeam);
            h.assertTrue(!RecipeHelper.checkConditions(gated, logic).isSuccess(),
                    "leaving the party must revoke access to its research");
            secondTeam.setEffectiveTeam(party);
            h.assertTrue(RecipeHelper.checkConditions(gated, logic).isSuccess(),
                    "rejoining the party must restore it");
        } finally {
            KnowledgeRegistry.set(previous);
            KnowledgeService.reset(server, party.getId());
            first.getInventory().clearContent();
            known.remove(firstId);
            known.remove(secondId);
            known.remove(outsiderId);
        }
    }
}
