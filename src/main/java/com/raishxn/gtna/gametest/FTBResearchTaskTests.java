package com.raishxn.gtna.gametest;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.FakePlayerFactory;

import com.mojang.authlib.GameProfile;
import com.raishxn.gtna.integration.ftb.GTNAQuestTypes;
import com.raishxn.gtna.integration.ftb.ResearchNodeTask;
import com.raishxn.gtna.research.*;
import dev.ftb.mods.ftbquests.quest.task.TaskTypes;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Optional FTB Quests test body: the registered GameTest holder must load without FTB Quests. */
final class FTBResearchTaskTests {

    static void run(GameTestHelper h) {
        h.assertTrue(TaskTypes.TYPES.containsKey(GTNAQuestTypes.RESEARCH_NODE_ID),
                "the research task type must be registered when FTB Quests is installed");
        var server = h.getLevel().getServer();
        var player = FakePlayerFactory.get(h.getLevel(), new GameProfile(UUID.randomUUID(), "TaskPlayer"));
        var node = new ResourceLocation("gtna_test", "task_node");
        var previous = KnowledgeRegistry.graph();
        var scope = KnowledgeScope.of(player);
        try {
            KnowledgeRegistry.set(KnowledgeGraph.build(List.of(new KnowledgeNode(node, 1, Optional.empty(),
                    List.of(), new KnowledgeTrigger.Manual(), List.of(), Optional.empty()))).graph());
            h.assertTrue(!ResearchNodeTask.isUnlockedFor(player, node.toString()), "locked before unlock");
            h.assertTrue(!ResearchNodeTask.isUnlockedFor(player, "Not A Valid Id"), "malformed ids never complete");
            h.assertTrue(!ResearchNodeTask.isUnlockedFor(player, ""), "an empty node never completes");
            KnowledgeService.unlock(server, scope, node, false);
            h.assertTrue(ResearchNodeTask.isUnlockedFor(player, node.toString()), "complete once unlocked");
            h.assertTrue(!ResearchNodeTask.isUnlockedFor(player, "gtna_test:other"), "other nodes stay locked");

            var task = new ResearchNodeTask(1L, null);
            var data = new CompoundTag();
            var configured = new ResearchNodeTask(2L, null);
            configured.readData(withNode(node.toString()));
            configured.writeData(data);
            h.assertTrue(data.getString("node").equals(node.toString()), "the node survives an NBT round trip");
            h.assertTrue(task.autoSubmitOnPlayerTick() == 20 && task.checkOnLogin(),
                    "checked each second and at login");
        } finally {
            KnowledgeRegistry.set(previous);
            KnowledgeService.reset(server, scope);
        }
    }

    private static CompoundTag withNode(String node) {
        var tag = new CompoundTag();
        tag.putString("node", node);
        return tag;
    }
}
