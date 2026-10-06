package com.raishxn.gtna.gametest;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.JsonOps;
import com.raishxn.gtna.network.packet.SKnowledgeSync;
import com.raishxn.gtna.research.*;
import io.netty.buffer.Unpooled;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/** Research engine: JSON shape, graph validation, saved data, unlocking rules and team scope. */
@GameTestHolder("gtna")
@PrefixGameTestTemplate(false)
public final class GTNAKnowledgeGameTests {

    private static final String TEMPLATE = "empty_12";

    private static ResourceLocation id(String path) {
        return new ResourceLocation("gtna_test", path);
    }

    private static KnowledgeNode node(String path, int tier, String... prerequisites) {
        return new KnowledgeNode(id(path), tier, Optional.empty(),
                java.util.Arrays.stream(prerequisites).map(GTNAKnowledgeGameTests::id).toList(),
                new KnowledgeTrigger.Manual(), List.of(), Optional.empty());
    }

    private static KnowledgeNode.Definition parse(String json) {
        return KnowledgeNode.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json))
                .getOrThrow(false, message -> {
                    throw new AssertionError(message);
                });
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 20)
    public static void researchJsonParsesFullAndMinimalNodes(GameTestHelper h) {
        var full = parse("""
                {"tier":3,"icon":"minecraft:iron_ingot","prerequisites":["gtna_test:a"],
                 "trigger":{"type":"obtain_item","item":"gtceu:aluminium_ingot"},
                 "grants":[{"type":"recipe_condition","recipes":["gtceu:foo","gtceu:bar"]},
                           {"type":"flag","id":"gtna_test:wireless"}],"quest":"start/steam"}
                """);
        h.assertTrue(full.tier() == 3, "tier");
        h.assertTrue(full.prerequisites().equals(List.of(id("a"))), "prerequisites");
        h.assertTrue(full.trigger() instanceof KnowledgeTrigger.ObtainItem item &&
                item.item().equals(new ResourceLocation("gtceu", "aluminium_ingot")), "trigger");
        h.assertTrue(full.grants().size() == 2, "grants");
        h.assertTrue(full.quest().orElse("").equals("start/steam"), "quest");
        var minimal = parse("{}");
        h.assertTrue(minimal.tier() == 1 && minimal.prerequisites().isEmpty() && minimal.grants().isEmpty() &&
                minimal.trigger() instanceof KnowledgeTrigger.Manual, "an empty object must use defaults");
        h.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 20)
    public static void researchJsonRejectsUnknownTypesAndBadIds(GameTestHelper h) {
        for (String bad : List.of("{\"grants\":[{\"type\":\"nonsense\"}]}", "{\"trigger\":{\"type\":\"nonsense\"}}",
                "{\"prerequisites\":[\"Not A Valid Id\"]}", "{\"tier\":\"high\"}")) {
            h.assertTrue(KnowledgeNode.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(bad)).result().isEmpty(),
                    "must be rejected: " + bad);
        }
        h.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 20)
    public static void researchGraphDropsBrokenNodesAndOrdersTheRest(GameTestHelper h) {
        var build = KnowledgeGraph.build(List.of(node("root", 1), node("orphan", 1, "ghost"),
                node("child_of_orphan", 2, "orphan"), node("fine", 2, "root"), node("a", 1, "b"), node("b", 1, "a"),
                node("self", 1, "self"), node("after_cycle", 2, "a"), node("zero", 0)));
        var kept = build.graph().nodes().stream().map(n -> n.id().getPath()).toList();
        h.assertTrue(kept.equals(List.of("root", "fine")), "survivors were " + kept);
        h.assertTrue(build.problems().size() == 7, "one problem per dropped node: " + build.problems());

        var ordered = KnowledgeGraph.build(List.of(node("z_tier2", 2), node("b", 1, "a"), node("a", 1),
                node("c", 1, "b"))).graph().nodes().stream().map(n -> n.id().getPath()).toList();
        h.assertTrue(ordered.equals(List.of("a", "b", "c", "z_tier2")), "order was " + ordered);
        h.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 20)
    public static void researchGraphIndexesGatesTriggersAndClosures(GameTestHelper h) {
        var recipe = new ResourceLocation("gtceu", "ebf_recipe");
        var gated = new KnowledgeNode(id("ebf"), 2, Optional.empty(), List.of(),
                new KnowledgeTrigger.ObtainItem(new ResourceLocation("gtceu", "steel_ingot")),
                List.of(new KnowledgeGrant.Recipes(List.of(recipe)), new KnowledgeGrant.Flag(id("flag"))),
                Optional.empty());
        var second = new KnowledgeNode(id("alu"), 2, Optional.empty(), List.of(), new KnowledgeTrigger.Manual(),
                List.of(new KnowledgeGrant.Recipes(List.of(recipe))), Optional.empty());
        var graph = KnowledgeGraph.build(List.of(gated, second, node("a", 1), node("b", 2, "a"),
                node("c", 3, "a", "b"))).graph();
        h.assertTrue(graph.gatesFor(recipe).equals(Set.of(id("ebf"), id("alu"))), "a recipe may need several nodes");
        h.assertTrue(graph.gatesFor(new ResourceLocation("gtceu", "ungated")).isEmpty(), "ungated recipes");
        h.assertTrue(graph.triggeredByItem(new ResourceLocation("gtceu", "steel_ingot")).size() == 1, "item trigger");
        h.assertTrue(graph.triggeredByItem(new ResourceLocation("gtceu", "other")).isEmpty(), "other items");
        var closure = graph.closure(id("c")).stream().map(n -> n.id().getPath()).toList();
        h.assertTrue(closure.equals(List.of("a", "b", "c")), "closure was " + closure);
        h.assertTrue(graph.closure(id("missing")).isEmpty(), "unknown node has an empty closure");
        var c = graph.get(id("c")).orElseThrow();
        h.assertTrue(graph.missingPrerequisites(c, other -> other.equals(id("a"))).equals(List.of(id("b"))),
                "missing prerequisites");
        h.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 20)
    public static void researchSavedDataRoundTripsAndKeepsRemovedNodes(GameTestHelper h) {
        UUID first = UUID.randomUUID(), second = UUID.randomUUID();
        var data = new KnowledgeData();
        h.assertTrue(data.add(first, id("a")) && data.add(first, id("removed_from_pack")) &&
                data.add(second, id("a")), "new entries are added");
        h.assertTrue(!data.add(first, id("a")), "duplicates are ignored");
        CompoundTag saved = data.save(new CompoundTag());
        h.assertTrue(saved.getInt("data_version") == KnowledgeData.DATA_VERSION, "data version is written");
        var loaded = KnowledgeData.load(saved);
        h.assertTrue(loaded.isUnlocked(first, id("a")) && loaded.isUnlocked(first, id("removed_from_pack")),
                "ids missing from the current graph must survive a reload");
        h.assertTrue(loaded.isUnlocked(second, id("a")) && !loaded.isUnlocked(second, id("removed_from_pack")),
                "scopes stay separate");
        h.assertTrue(loaded.reset(first) == 2 && loaded.unlocked(first).isEmpty() && loaded.reset(first) == 0,
                "reset removes exactly one scope");
        h.assertTrue(loaded.isUnlocked(second, id("a")), "reset must not touch other scopes");
        h.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 20)
    public static void researchServiceEnforcesPrerequisitesFlagsAndRecipeGates(GameTestHelper h) {
        var server = h.getLevel().getServer();
        UUID scope = UUID.randomUUID(), other = UUID.randomUUID();
        var recipe = new ResourceLocation("gtceu", "research_test_recipe");
        var flag = id("research_test_flag");
        var previous = KnowledgeRegistry.graph();
        List<ResourceLocation> events = new ArrayList<>();
        java.util.function.Consumer<KnowledgeUnlockedEvent> listener = event -> {
            if (event.scope().equals(scope)) events.add(event.node().id());
        };
        MinecraftForge.EVENT_BUS.addListener(listener);
        try {
            var gate = new KnowledgeNode(id("gate"), 2, Optional.empty(), List.of(id("base")),
                    new KnowledgeTrigger.Manual(), List.of(new KnowledgeGrant.Recipes(List.of(recipe)),
                            new KnowledgeGrant.Flag(flag)),
                    Optional.empty());
            KnowledgeRegistry.set(KnowledgeGraph.build(List.of(node("base", 1), gate, node("deep", 3, "gate")))
                    .graph());

            h.assertTrue(KnowledgeService.unlock(server, scope, id("nope"), false) ==
                    KnowledgeService.UnlockResult.UNKNOWN_NODE, "unknown node");
            h.assertTrue(!KnowledgeService.recipeAllowed(server, scope, recipe), "gated recipe is blocked");
            h.assertTrue(KnowledgeService.recipeAllowed(server, scope, new ResourceLocation("gtceu", "free")),
                    "ungated recipes always pass");
            h.assertTrue(KnowledgeService.unlock(server, scope, id("gate"), false) ==
                    KnowledgeService.UnlockResult.MISSING_PREREQUISITES, "prerequisite is enforced");
            h.assertTrue(events.isEmpty(), "a refused unlock must not post an event");
            h.assertTrue(KnowledgeService.unlock(server, scope, id("base"), false) ==
                    KnowledgeService.UnlockResult.UNLOCKED, "base unlocks");
            h.assertTrue(KnowledgeService.unlock(server, scope, id("base"), false) ==
                    KnowledgeService.UnlockResult.ALREADY_UNLOCKED, "second unlock is a no-op");
            h.assertTrue(!KnowledgeService.hasFlag(server, scope, flag), "flag needs its node");
            h.assertTrue(KnowledgeService.unlock(server, scope, id("gate"), false) ==
                    KnowledgeService.UnlockResult.UNLOCKED, "gate unlocks once the base is held");
            h.assertTrue(KnowledgeService.hasFlag(server, scope, flag), "flag granted");
            h.assertTrue(KnowledgeService.recipeAllowed(server, scope, recipe), "recipe allowed after unlock");
            h.assertTrue(!KnowledgeService.recipeAllowed(server, other, recipe) &&
                    !KnowledgeService.hasFlag(server, other, flag), "another scope does not inherit");
            h.assertTrue(KnowledgeService.unlock(server, other, id("deep"), true) ==
                    KnowledgeService.UnlockResult.UNLOCKED, "force unlocks the whole chain");
            h.assertTrue(KnowledgeService.unlocked(server, other).equals(
                    Set.of(id("base"), id("gate"), id("deep"))),
                    "chain was " + KnowledgeService.unlocked(server, other));
            h.assertTrue(events.equals(List.of(id("base"), id("gate"))), "events were " + events);
            h.assertTrue(KnowledgeService.reset(server, scope) == 2 && KnowledgeService.reset(server, other) == 3,
                    "reset reports what it removed");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(listener);
            KnowledgeRegistry.set(previous);
            KnowledgeService.reset(server, scope);
            KnowledgeService.reset(server, other);
        }
        h.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 20)
    public static void researchScopeMatchesFluidDiscoveryAndSeparatesPlayers(GameTestHelper h) {
        UUID firstId = UUID.randomUUID(), secondId = UUID.randomUUID();
        var first = FakePlayerFactory.get(h.getLevel(), new GameProfile(firstId, "ResearchFirst"));
        var second = FakePlayerFactory.get(h.getLevel(), new GameProfile(secondId, "ResearchSecond"));
        h.assertTrue(KnowledgeScope.of(first).equals(com.raishxn.gtna.common.item.DepositRecorderBehavior.owner(first)),
                "research and fluid discovery must use the same owner rule");
        h.assertTrue(!KnowledgeScope.of(first).equals(KnowledgeScope.of(second)),
                "players outside any team must not share progress");
        h.assertTrue(KnowledgeScope.of(firstId).equals(KnowledgeScope.of(first)), "UUID and player overloads agree");
        h.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 20)
    public static void researchGateBlocksTheRecipeUntilTheOwnerUnlocksTheNode(GameTestHelper h) {
        var server = h.getLevel().getServer();
        var pos = new BlockPos(1, 2, 1);
        h.setBlock(pos, GTMachines.MACERATOR[GTValues.LV].getBlock());
        var machine = ((MetaMachineBlockEntity) h.getBlockEntity(pos)).getMetaMachine();
        var logic = ((IRecipeLogicMachine) machine).getRecipeLogic();
        var recipes = h.getLevel().getRecipeManager().getAllRecipesFor(GTRecipeTypes.MACERATOR_RECIPES);
        h.assertTrue(recipes.size() >= 2, "need two real macerator recipes");
        var gated = recipes.get(0);
        var free = recipes.get(1);
        UUID owner = UUID.randomUUID(), stranger = UUID.randomUUID();
        var previous = KnowledgeRegistry.graph();
        try {
            h.assertTrue(RecipeHelper.checkConditions(gated, logic).isSuccess(), "ungated before any graph");
            var gate = new KnowledgeNode(id("macerator_gate"), 1, Optional.empty(), List.of(),
                    new KnowledgeTrigger.Manual(), List.of(new KnowledgeGrant.Recipes(List.of(gated.id))),
                    Optional.empty());
            KnowledgeRegistry.set(KnowledgeGraph.build(List.of(gate)).graph());

            machine.setOwnerUUID(owner);
            var blocked = RecipeHelper.checkConditions(gated, logic);
            h.assertTrue(!blocked.isSuccess(), "gated recipe must be blocked for an owner without the node");
            h.assertTrue(blocked.reason() != null && blocked.reason().getString().contains("research"),
                    "the failure must say research is missing, was: " + blocked.reason());
            machine.setOwnerUUID(null);
            h.assertTrue(!RecipeHelper.checkConditions(gated, logic).isSuccess(),
                    "an ownerless machine must not run a gated recipe");
            machine.setOwnerUUID(owner);
            h.assertTrue(RecipeHelper.checkConditions(free, logic).isSuccess(),
                    "other recipes on the same machine must keep running");

            h.assertTrue(KnowledgeService.unlock(server, KnowledgeScope.of(owner), gate.id(), false) ==
                    KnowledgeService.UnlockResult.UNLOCKED, "unlock");
            h.assertTrue(RecipeHelper.checkConditions(gated, logic).isSuccess(),
                    "the recipe must run once the owner's scope holds the node");
            machine.setOwnerUUID(stranger);
            h.assertTrue(!RecipeHelper.checkConditions(gated, logic).isSuccess(),
                    "another owner must not inherit the unlock");
        } finally {
            KnowledgeRegistry.set(previous);
            KnowledgeService.reset(server, KnowledgeScope.of(owner));
        }
        h.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 20)
    public static void researchItemTriggerUnlocksChainsOnlyWhenPrerequisitesAreMet(GameTestHelper h) {
        var server = h.getLevel().getServer();
        UUID holderId = UUID.randomUUID(), bystanderId = UUID.randomUUID();
        var holder = FakePlayerFactory.get(h.getLevel(), new GameProfile(holderId, "TriggerHolder"));
        var bystander = FakePlayerFactory.get(h.getLevel(), new GameProfile(bystanderId, "TriggerBystander"));
        holder.getInventory().clearContent();
        bystander.getInventory().clearContent();
        var previous = KnowledgeRegistry.graph();
        UUID scope = KnowledgeScope.of(holder), bystanderScope = KnowledgeScope.of(bystander);
        try {
            KnowledgeRegistry.set(KnowledgeGraph.build(List.of(
                    triggered("first", Items.STICK, null), triggered("second", Items.COAL, "first"),
                    triggered("third", Items.IRON_INGOT, "second"))).graph());

            holder.getInventory().add(new ItemStack(Items.COAL));
            h.assertTrue(KnowledgeTriggers.scan(holder, ItemStack.EMPTY) == 0,
                    "an item whose node needs a missing prerequisite must not unlock it");
            h.assertTrue(KnowledgeService.unlocked(server, scope).isEmpty(), "nothing unlocked yet");

            holder.getInventory().add(new ItemStack(Items.STICK));
            h.assertTrue(KnowledgeTriggers.scan(holder, ItemStack.EMPTY) == 2,
                    "holding both items must unlock the chain in one pass");
            h.assertTrue(KnowledgeService.unlocked(server, scope).equals(Set.of(id("first"), id("second"))),
                    "unlocked was " + KnowledgeService.unlocked(server, scope));
            h.assertTrue(KnowledgeTriggers.scan(holder, ItemStack.EMPTY) == 0, "a second scan is a no-op");

            h.assertTrue(KnowledgeTriggers.scan(holder, new ItemStack(Items.IRON_INGOT)) == 1,
                    "an item just crafted or picked up counts before it reaches the inventory");
            h.assertTrue(KnowledgeService.isUnlocked(server, scope, id("third")), "third unlocked");
            h.assertTrue(KnowledgeTriggers.scan(bystander, ItemStack.EMPTY) == 0 &&
                    KnowledgeService.unlocked(server, bystanderScope).isEmpty(),
                    "a player without the items must not inherit anything");
        } finally {
            KnowledgeRegistry.set(previous);
            KnowledgeService.reset(server, scope);
            KnowledgeService.reset(server, bystanderScope);
            holder.getInventory().clearContent();
        }
        h.succeed();
    }

    private static KnowledgeNode triggered(String path, net.minecraft.world.item.Item item, String prerequisite) {
        return new KnowledgeNode(id(path), 1, Optional.empty(),
                prerequisite == null ? List.of() : List.of(id(prerequisite)),
                new KnowledgeTrigger.ObtainItem(net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(item)),
                List.of(), Optional.empty());
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 20)
    public static void researchQuestTaskCompletesFromTheNodeState(GameTestHelper h) {
        if (net.minecraftforge.fml.ModList.get().isLoaded("ftbquests")) FTBResearchTaskTests.run(h);
        h.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 20)
    public static void researchSyncSnapshotRoundTripsThroughThePacket(GameTestHelper h) {
        var server = h.getLevel().getServer();
        UUID scope = UUID.randomUUID();
        var recipe = new ResourceLocation("gtceu", "sync_test_recipe");
        var previous = KnowledgeRegistry.graph();
        try {
            var gated = new KnowledgeNode(id("sync_gate"), 2, Optional.empty(), List.of(id("sync_base")),
                    new KnowledgeTrigger.ObtainItem(new ResourceLocation("minecraft", "stick")),
                    List.of(new KnowledgeGrant.Recipes(List.of(recipe))), Optional.empty());
            KnowledgeRegistry.set(KnowledgeGraph.build(List.of(node("sync_base", 1), gated)).graph());
            KnowledgeService.unlock(server, scope, id("sync_base"), false);

            var sent = KnowledgeSync.snapshot(server, scope);
            var buf = new FriendlyByteBuf(Unpooled.buffer());
            sent.encode(buf);
            var received = SKnowledgeSync.decode(buf);
            h.assertTrue(received.nodes().equals(sent.nodes()) && received.unlocked().equals(sent.unlocked()),
                    "the packet must survive encode and decode unchanged");
            h.assertTrue(buf.readableBytes() == 0, "decode must consume the whole packet");
            var view = received.nodes().stream().filter(n -> n.id().equals(id("sync_gate"))).findFirst().orElseThrow();
            h.assertTrue(view.recipes().equals(List.of(recipe)) && view.tier() == 2 &&
                    view.prerequisites().equals(List.of(id("sync_base"))) &&
                    view.triggerItem().equals(Optional.of(new ResourceLocation("minecraft", "stick"))),
                    "node view fields were " + view);
            h.assertTrue(received.unlocked().equals(List.of(id("sync_base"))), "only the scope's own unlocks are sent");
        } finally {
            KnowledgeRegistry.set(previous);
            KnowledgeService.reset(server, scope);
        }
        h.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 20)
    public static void researchViewerListsResearchWithoutTouchingTheRecipe(GameTestHelper h) {
        var real = h.getLevel().getRecipeManager().getAllRecipesFor(GTRecipeTypes.MACERATOR_RECIPES).get(0);
        var own = real.conditions;
        var shownNode = id("shown");
        try {
            ClientKnowledge.set(List.of(new ClientKnowledge.NodeView(shownNode, 1, List.of(), Optional.empty(),
                    Optional.empty(), List.of(real.id))), List.of());
            h.assertTrue(ClientKnowledge.gatesFor(real.id).equals(List.of(shownNode)), "recipe index");
            var shown = ResearchGateCondition.withResearch(own, real.id);
            h.assertTrue(shown.size() == own.size() + 1 && shown != own, "one extra line for the gated recipe");
            h.assertTrue(
                    shown.get(shown.size() - 1) instanceof ResearchGateCondition gate && gate.node().equals(shownNode),
                    "the extra line names the node");
            h.assertTrue(real.conditions == own && real.conditions.size() == own.size(),
                    "the recipe itself must not change (it can be the server's object or hold an immutable list)");
            String locked = shown.get(shown.size() - 1).getTooltips().getString();
            h.assertTrue(locked.toLowerCase().contains("research"), "the line must mention research: " + locked);
            ClientKnowledge.set(List.of(new ClientKnowledge.NodeView(shownNode, 1, List.of(), Optional.empty(),
                    Optional.empty(), List.of(real.id))), List.of(shownNode));
            String unlocked = ResearchGateCondition.withResearch(own, real.id).get(own.size()).getTooltips()
                    .getString();
            h.assertTrue(!unlocked.equals(locked), "the line changes once the node is unlocked: " + unlocked);
            h.assertTrue(ResearchGateCondition.withResearch(own, new ResourceLocation("gtceu", "ungated")) == own,
                    "ungated recipes list their own conditions only");
            h.assertTrue(ResearchGateCondition.withResearch(own, null) == own, "a recipe without an id is left alone");
        } finally {
            ClientKnowledge.clear();
        }
        h.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 20)
    public static void researchGateConditionAgreesWithTheServerRule(GameTestHelper h) {
        var server = h.getLevel().getServer();
        var pos = new BlockPos(1, 2, 1);
        h.setBlock(pos, GTMachines.MACERATOR[GTValues.LV].getBlock());
        var machine = ((MetaMachineBlockEntity) h.getBlockEntity(pos)).getMetaMachine();
        var logic = ((IRecipeLogicMachine) machine).getRecipeLogic();
        var recipe = h.getLevel().getRecipeManager().getAllRecipesFor(GTRecipeTypes.MACERATOR_RECIPES).get(0);
        UUID owner = UUID.randomUUID();
        var node = id("condition_node");
        var previous = KnowledgeRegistry.graph();
        try {
            KnowledgeRegistry.set(KnowledgeGraph.build(List.of(node("condition_node", 1))).graph());
            var condition = new ResearchGateCondition(node);
            machine.setOwnerUUID(owner);
            h.assertTrue(!condition.check(recipe, logic), "locked without the node");
            machine.setOwnerUUID(null);
            h.assertTrue(!condition.check(recipe, logic), "no owner means locked");
            machine.setOwnerUUID(owner);
            KnowledgeService.unlock(server, KnowledgeScope.of(owner), node, false);
            h.assertTrue(condition.check(recipe, logic), "open once the owner's scope holds the node");
            h.assertTrue(ResearchGateCondition.CODEC.encodeStart(com.mojang.serialization.JsonOps.INSTANCE, condition)
                    .result().isPresent(), "the condition serializes");
        } finally {
            KnowledgeRegistry.set(previous);
            KnowledgeService.reset(server, KnowledgeScope.of(owner));
        }
        h.succeed();
    }

    private static ClientKnowledge.NodeView view(String path, int tier, String... prerequisites) {
        return new ClientKnowledge.NodeView(id(path), tier,
                java.util.Arrays.stream(prerequisites).map(GTNAKnowledgeGameTests::id).toList(), Optional.empty(),
                Optional.empty(), List.of());
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 20)
    public static void researchTreeLayoutOrdersTiersAndKeepsSlotsUnique(GameTestHelper h) {
        var layout = KnowledgeTreeLayout.of(List.of(view("a", 1), view("b", 1, "a"), view("c", 1, "a"),
                view("d", 2, "b", "c"), view("e", 2, "d"), view("f", 2)));
        var slots = layout.slots();
        h.assertTrue(slots.size() == 6, "every node gets a slot");
        h.assertTrue(slots.get(id("a")).column() < slots.get(id("b")).column() &&
                slots.get(id("b")).column() == slots.get(id("c")).column(),
                "siblings share a column after their parent");
        h.assertTrue(slots.get(id("d")).column() > slots.get(id("b")).column(), "a later tier sits to the right");
        h.assertTrue(slots.get(id("e")).column() > slots.get(id("d")).column(), "depth inside a tier moves right");
        var seen = new java.util.HashSet<KnowledgeTreeLayout.Slot>(slots.values());
        h.assertTrue(seen.size() == slots.size(), "no two nodes share a slot");
        h.assertTrue(layout.bands().size() == 2 && layout.bands().get(0).tier() == 1 &&
                layout.bands().get(0).columnCount() == 2 && layout.bands().get(1).firstColumn() == 2,
                "one header band per tier: " + layout.bands());
        h.assertTrue(layout.columns() == 4 && layout.rows() == 2, "size was " + layout.columns() + "x" + layout.rows());
        h.assertTrue(KnowledgeTreeLayout.of(List.of()).slots().isEmpty(), "an empty tree has no slots");
        h.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 20)
    public static void researchTreeLayoutSurvivesCyclesAndUnknownPrerequisites(GameTestHelper h) {
        var layout = KnowledgeTreeLayout.of(List.of(view("x", 1, "y"), view("y", 1, "x"), view("z", 1, "ghost")));
        h.assertTrue(layout.slots().size() == 3, "malformed data must still lay out every node");
        h.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 20)
    public static void researchTeamsShareProgressAndMachineGates(GameTestHelper h) throws ReflectiveOperationException {
        if (net.minecraftforge.fml.ModList.get().isLoaded("ftbteams")) FTBResearchTeamTests.run(h);
        h.succeed();
    }

    private static com.gregtechceu.gtceu.api.machine.trait.RecipeLogic ownedLogic(GameTestHelper h, UUID owner) {
        var pos = new BlockPos(1, 2, 1);
        h.setBlock(pos, GTMachines.MACERATOR[GTValues.LV].getBlock());
        var machine = ((MetaMachineBlockEntity) h.getBlockEntity(pos)).getMetaMachine();
        machine.setOwnerUUID(owner);
        return ((IRecipeLogicMachine) machine).getRecipeLogic();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 20)
    public static void researchPlaythroughPassesOnAWellFormedTree(GameTestHelper h) {
        var player = FakePlayerFactory.get(h.getLevel(), new GameProfile(UUID.randomUUID(), "Playthrough"));
        var recipes = h.getLevel().getRecipeManager().getAllRecipesFor(GTRecipeTypes.MACERATOR_RECIPES);
        var previous = KnowledgeRegistry.graph();
        try {
            var base = new KnowledgeNode(id("pt_base"), 1, Optional.empty(), List.of(),
                    new KnowledgeTrigger.ObtainItem(new ResourceLocation("minecraft", "stick")), List.of(),
                    Optional.empty());
            var gate = new KnowledgeNode(id("pt_gate"), 2, Optional.empty(), List.of(id("pt_base")),
                    new KnowledgeTrigger.ObtainItem(new ResourceLocation("minecraft", "coal")),
                    List.of(new KnowledgeGrant.Recipes(List.of(recipes.get(0).id))), Optional.empty());
            var manual = new KnowledgeNode(id("pt_manual"), 2, Optional.empty(), List.of(id("pt_gate")),
                    new KnowledgeTrigger.Manual(), List.of(), Optional.empty());
            KnowledgeRegistry.set(KnowledgeGraph.build(List.of(base, gate, manual)).graph());
            var result = KnowledgePlaythrough.run(player, ownedLogic(h, player.getUUID()));
            h.assertTrue(result.passed(), "a well-formed tree must pass: " + result.failures());
            h.assertTrue(result.nodes() == 3 && result.gatedRecipes() == 1, "counts were " + result);
        } finally {
            KnowledgeRegistry.set(previous);
        }
        h.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 20)
    public static void researchPlaythroughReportsBrokenContent(GameTestHelper h) {
        var player = FakePlayerFactory.get(h.getLevel(), new GameProfile(UUID.randomUUID(), "PlaythroughBad"));
        var previous = KnowledgeRegistry.graph();
        try {
            var badItem = new KnowledgeNode(id("bad_item"), 1, Optional.empty(), List.of(),
                    new KnowledgeTrigger.ObtainItem(new ResourceLocation("gtna_test", "no_such_item")), List.of(),
                    Optional.empty());
            var badRecipe = new KnowledgeNode(id("bad_recipe"), 1, Optional.empty(), List.of(),
                    new KnowledgeTrigger.Manual(),
                    List.of(new KnowledgeGrant.Recipes(List.of(new ResourceLocation("gtna_test", "no_such_recipe")))),
                    Optional.empty());
            KnowledgeRegistry.set(KnowledgeGraph.build(List.of(badItem, badRecipe)).graph());
            var result = KnowledgePlaythrough.run(player, ownedLogic(h, player.getUUID()));
            h.assertTrue(!result.passed() && result.failures().size() == 2,
                    "a missing item and a missing recipe must both be reported: " + result.failures());
        } finally {
            KnowledgeRegistry.set(previous);
        }
        h.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 20)
    public static void researchPlaythroughUnlocksManualPrerequisitesItself(GameTestHelper h) {
        var player = FakePlayerFactory.get(h.getLevel(), new GameProfile(UUID.randomUUID(), "PlaythroughOrder"));
        var recipes = h.getLevel().getRecipeManager().getAllRecipesFor(GTRecipeTypes.MACERATOR_RECIPES);
        var previous = KnowledgeRegistry.graph();
        try {
            // The prerequisite is manual, so no item unlocks it: the script must unlock it through the
            // service before the item-triggered node that depends on it, and still pass.
            var manual = new KnowledgeNode(id("order_manual"), 1, Optional.empty(), List.of(),
                    new KnowledgeTrigger.Manual(), List.of(), Optional.empty());
            var later = new KnowledgeNode(id("order_later"), 1, Optional.empty(), List.of(id("order_manual")),
                    new KnowledgeTrigger.ObtainItem(new ResourceLocation("minecraft", "stick")),
                    List.of(new KnowledgeGrant.Recipes(List.of(recipes.get(1).id))), Optional.empty());
            KnowledgeRegistry.set(KnowledgeGraph.build(List.of(manual, later)).graph());
            var result = KnowledgePlaythrough.run(player, ownedLogic(h, player.getUUID()));
            h.assertTrue(result.passed(), "a manual prerequisite is unlocked by the script: " + result.failures());
        } finally {
            KnowledgeRegistry.set(previous);
        }
        h.succeed();
    }

    private static int command(GameTestHelper h, net.minecraft.server.level.ServerPlayer player, int permission,
                               String command) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var source = player.createCommandSourceStack().withPermission(permission);
        return h.getLevel().getServer().getCommands().getDispatcher().execute(command, source);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 20)
    public static void researchCommandsListUnlockForceAndReset(GameTestHelper h)
                                                                                 throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var server = h.getLevel().getServer();
        var player = FakePlayerFactory.get(h.getLevel(), new GameProfile(UUID.randomUUID(), "CommandPlayer"));
        var scope = KnowledgeScope.of(player);
        var previous = KnowledgeRegistry.graph();
        try {
            KnowledgeRegistry.set(KnowledgeGraph.build(List.of(node("cmd_a", 1), node("cmd_b", 1, "cmd_a"),
                    node("cmd_c", 2, "cmd_b"))).graph());
            h.assertTrue(command(h, player, 0, "gtna research list") == 0, "nothing unlocked yet");
            h.assertTrue(command(h, player, 0, "gtna research info gtna_test:cmd_b") == 1, "info on a known node");
            h.assertTrue(command(h, player, 0, "gtna research info gtna_test:nope") == 0, "info on an unknown node");

            boolean refused = false;
            try {
                command(h, player, 0, "gtna research unlock @s gtna_test:cmd_a");
            } catch (com.mojang.brigadier.exceptions.CommandSyntaxException denied) {
                refused = true;
            }
            h.assertTrue(refused && !KnowledgeService.isUnlocked(server, scope, id("cmd_a")),
                    "changing progress must need permission level 2");

            h.assertTrue(command(h, player, 2, "gtna research unlock @s gtna_test:cmd_b") == 0 &&
                    !KnowledgeService.isUnlocked(server, scope, id("cmd_b")), "prerequisites are enforced");
            h.assertTrue(command(h, player, 2, "gtna research unlock @s gtna_test:cmd_a") == 1 &&
                    KnowledgeService.isUnlocked(server, scope, id("cmd_a")), "unlock");
            h.assertTrue(command(h, player, 2, "gtna research unlock @s gtna_test:cmd_a") == 0, "already unlocked");
            h.assertTrue(command(h, player, 2, "gtna research unlock @s gtna_test:nope") == 0, "unknown node");
            h.assertTrue(command(h, player, 0, "gtna research list") == 1, "one node unlocked");
            h.assertTrue(command(h, player, 2, "gtna research unlock @s gtna_test:cmd_c force") == 1 &&
                    KnowledgeService.unlocked(server, scope).containsAll(Set.of(id("cmd_a"), id("cmd_b"), id("cmd_c"))),
                    "force unlocks the whole chain");
            h.assertTrue(command(h, player, 2, "gtna research reset @s") == 3 &&
                    KnowledgeService.unlocked(server, scope).isEmpty(), "reset clears the scope");
        } finally {
            KnowledgeRegistry.set(previous);
            KnowledgeService.reset(server, scope);
        }
        h.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 20)
    public static void researchNodesForMissingModsAreSkippedNotBroken(GameTestHelper h) {
        var optional = parse("{\"requires_mods\":[\"minecraft\",\"gtna_test_no_such_mod\"]}");
        h.assertTrue(optional.requiresMods().equals(List.of("minecraft", "gtna_test_no_such_mod")), "field parsed");
        java.util.function.Predicate<String> loaded = net.minecraftforge.fml.ModList.get()::isLoaded;
        h.assertTrue(!optional.modsLoaded(loaded), "one missing mod is enough to skip the node");
        h.assertTrue(parse("{\"requires_mods\":[\"minecraft\"]}").modsLoaded(loaded), "all present mods load the node");
        h.assertTrue(parse("{}").modsLoaded(loaded) && parse("{}").requiresMods().isEmpty(),
                "no requirement by default");
        h.assertTrue(
                KnowledgeNode.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString("{\"requires_mods\":\"minecraft\"}"))
                        .result().isEmpty(),
                "requires_mods must be a list");
        h.succeed();
    }

    // ------------------------------------------------------------------ research points (plan v02, P1)

    private static ResourceLocation area(String path) {
        return new ResourceLocation("gtna_test", path);
    }

    private static KnowledgeNode bought(String path, String prerequisite, net.minecraft.world.item.Item item,
                                        Map<ResourceLocation, Integer> cost,
                                        Optional<KnowledgeNode.Eureka> eureka) {
        return new KnowledgeNode(id(path), 1, Optional.empty(),
                prerequisite == null ? List.of() : List.of(id(prerequisite)),
                item == null ? new KnowledgeTrigger.Manual() :
                        new KnowledgeTrigger.ObtainItem(
                                net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(item)),
                List.of(), Optional.empty(), cost, KnowledgeNode.Kind.BRANCH, eureka);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 20)
    public static void researchJsonParsesCostKindAndEureka(GameTestHelper h) {
        var node = parse("""
                {"cost":{"gtna_test:metallurgy":15,"gtna_test:energy":5},"kind":"branch",
                 "eureka":{"item":"minecraft:iron_ingot","reduction":0.5}}
                """);
        h.assertTrue(node.cost().equals(Map.of(area("metallurgy"), 15, area("energy"), 5)), "cost " + node.cost());
        h.assertTrue(node.kind() == KnowledgeNode.Kind.BRANCH, "kind");
        h.assertTrue(node.eureka().isPresent() && node.eureka().get().reduction() == 0.5 &&
                node.eureka().get().item().equals(new ResourceLocation("minecraft", "iron_ingot")), "eureka");
        var plain = parse("{}");
        h.assertTrue(plain.cost().isEmpty() && plain.kind() == KnowledgeNode.Kind.TRUNK && plain.eureka().isEmpty(),
                "an empty object stays a free trunk node");
        var badKind = KnowledgeNode.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString("{\"kind\":\"twig\"}"));
        h.assertTrue(badKind.error().isPresent(), "an unknown kind is an error");
        var zeroCost = KnowledgeNode.CODEC.parse(JsonOps.INSTANCE,
                JsonParser.parseString("{\"cost\":{\"gtna_test:energy\":0}}"));
        h.assertTrue(zeroCost.error().isPresent(), "a cost entry must be at least 1");
        var withId = node.withId(id("parsed"));
        h.assertTrue(withId.purchasable() && withId.cost(true).equals(Map.of(area("metallurgy"), 8, area("energy"), 3)),
                "half off, rounded up: " + withId.cost(true));
        h.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 20)
    public static void researchPurchaseNeedsPrerequisitesRequirementAndPoints(GameTestHelper h) {
        var server = h.getLevel().getServer();
        UUID scope = UUID.randomUUID(), other = UUID.randomUUID();
        var previous = KnowledgeRegistry.graph();
        var metallurgy = area("metallurgy");
        var energy = area("energy");
        try {
            KnowledgeRegistry.set(KnowledgeGraph.build(List.of(node("buy_base", 1),
                    bought("buy_node", "buy_base", Items.IRON_INGOT, Map.of(metallurgy, 10, energy, 4),
                            Optional.of(
                                    new KnowledgeNode.Eureka(new ResourceLocation("minecraft", "gold_ingot"), 0.5)))))
                    .graph());
            var buy = id("buy_node");
            h.assertTrue(KnowledgeService.purchase(server, scope, id("nope")) ==
                    KnowledgeService.PurchaseResult.UNKNOWN_NODE, "unknown node");
            h.assertTrue(KnowledgeService.purchase(server, scope, buy) ==
                    KnowledgeService.PurchaseResult.MISSING_PREREQUISITES, "prerequisite first");
            KnowledgeService.unlock(server, scope, id("buy_base"), false);
            h.assertTrue(KnowledgeService.purchase(server, scope, buy) ==
                    KnowledgeService.PurchaseResult.MISSING_REQUIREMENT, "the item must have been held");
            h.assertTrue(KnowledgeService.markRequirement(server, scope, buy) &&
                    !KnowledgeService.markRequirement(server, scope, buy), "requirement is recorded once");
            KnowledgeService.addPoints(server, scope, metallurgy, 10);
            h.assertTrue(KnowledgeService.purchase(server, scope, buy) ==
                    KnowledgeService.PurchaseResult.NOT_ENOUGH_POINTS, "energy is missing");
            h.assertTrue(KnowledgeService.points(server, scope).equals(Map.of(metallurgy, 10L)),
                    "a refused purchase spends nothing: " + KnowledgeService.points(server, scope));
            h.assertTrue(KnowledgeService.markEureka(server, scope, buy), "eureka found");
            h.assertTrue(KnowledgeService.cost(server, scope, KnowledgeRegistry.graph().get(buy).orElseThrow())
                    .equals(Map.of(metallurgy, 5, energy, 2)), "the eureka halves the cost");
            KnowledgeService.addPoints(server, scope, energy, 2);
            h.assertTrue(KnowledgeService.purchase(server, scope, buy) ==
                    KnowledgeService.PurchaseResult.PURCHASED, "bought");
            h.assertTrue(KnowledgeService.isUnlocked(server, scope, buy), "a bought node is unlocked");
            h.assertTrue(KnowledgeService.points(server, scope).equals(Map.of(metallurgy, 5L)),
                    "exactly the discounted cost was spent: " + KnowledgeService.points(server, scope));
            h.assertTrue(KnowledgeService.purchase(server, scope, buy) ==
                    KnowledgeService.PurchaseResult.ALREADY_UNLOCKED, "second purchase is a no-op");
            h.assertTrue(KnowledgeService.points(server, other).isEmpty() &&
                    !KnowledgeService.isUnlocked(server, other, buy), "another scope does not share points");
            h.assertTrue(KnowledgeService.addPoints(server, scope, metallurgy, -100) == 0 &&
                    KnowledgeService.points(server, scope).isEmpty(), "the balance never goes below zero");
        } finally {
            KnowledgeRegistry.set(previous);
            KnowledgeService.reset(server, scope);
            KnowledgeService.reset(server, other);
        }
        h.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 20)
    public static void researchBoughtNodeIsNotUnlockedByHoldingItsItem(GameTestHelper h) {
        var server = h.getLevel().getServer();
        var previous = KnowledgeRegistry.graph();
        var holder = FakePlayerFactory.get(h.getLevel(), new GameProfile(UUID.randomUUID(), "ResearchBuyer"));
        UUID scope = KnowledgeScope.of(holder);
        try {
            KnowledgeRegistry.set(KnowledgeGraph.build(List.of(
                    bought("held_bought", null, Items.IRON_INGOT, Map.of(area("metallurgy"), 3), Optional.empty()),
                    triggered("held_free", Items.IRON_INGOT, null))).graph());
            holder.getInventory().add(new ItemStack(Items.IRON_INGOT));
            h.assertTrue(KnowledgeTriggers.scan(holder, ItemStack.EMPTY) == 1,
                    "only the free node unlocks by holding the item");
            h.assertTrue(KnowledgeService.isUnlocked(server, scope, id("held_free")), "free node unlocked");
            h.assertTrue(!KnowledgeService.isUnlocked(server, scope, id("held_bought")),
                    "a node with a cost waits for the purchase");
            h.assertTrue(KnowledgeData.get(server).requirementMet(scope, id("held_bought")),
                    "holding the item meets the requirement");
            KnowledgeService.addPoints(server, scope, area("metallurgy"), 3);
            h.assertTrue(KnowledgeService.purchase(server, scope, id("held_bought")) ==
                    KnowledgeService.PurchaseResult.PURCHASED, "bought after the requirement");
        } finally {
            KnowledgeRegistry.set(previous);
            KnowledgeService.reset(server, scope);
            holder.getInventory().clearContent();
        }
        h.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 20)
    public static void researchDataLoadsVersionOneAndRoundTripsVersionTwo(GameTestHelper h) {
        UUID scope = UUID.randomUUID();
        var legacy = new CompoundTag();
        legacy.putInt("data_version", 1);
        var scopes = new net.minecraft.nbt.ListTag();
        var entry = new CompoundTag();
        entry.putUUID("scope", scope);
        var nodes = new net.minecraft.nbt.ListTag();
        nodes.add(net.minecraft.nbt.StringTag.valueOf(id("old").toString()));
        entry.put("nodes", nodes);
        scopes.add(entry);
        legacy.put("scopes", scopes);
        var fromV1 = KnowledgeData.load(legacy);
        h.assertTrue(fromV1.isUnlocked(scope, id("old")) && fromV1.points(scope).isEmpty() &&
                fromV1.requirements(scope).isEmpty() && fromV1.eurekas(scope).isEmpty(),
                "version 1 keeps its nodes and starts with nothing else");

        fromV1.addPoints(scope, area("phase"), 7);
        fromV1.markRequirement(scope, id("needs_item"));
        fromV1.markEureka(scope, id("cheaper"));
        var saved = fromV1.save(new CompoundTag());
        h.assertTrue(saved.getInt("data_version") == KnowledgeData.DATA_VERSION, "saves the current version");
        var reloaded = KnowledgeData.load(saved);
        h.assertTrue(reloaded.isUnlocked(scope, id("old")), "nodes survive");
        h.assertTrue(reloaded.points(scope).equals(Map.of(area("phase"), 7L)), "points survive");
        h.assertTrue(reloaded.requirementMet(scope, id("needs_item")) && reloaded.eurekaMet(scope, id("cheaper")),
                "requirements and eurekas survive");
        UUID pointsOnly = UUID.randomUUID();
        reloaded.addPoints(pointsOnly, area("energy"), 3);
        h.assertTrue(KnowledgeData.load(reloaded.save(new CompoundTag())).points(pointsOnly, area("energy")) == 3,
                "a scope with only points is kept");
        h.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 20)
    public static void researchSyncPacketCarriesCostsAndPoints(GameTestHelper h) {
        var view = new ClientKnowledge.NodeView(id("packet_node"), 2, List.of(id("packet_base")), Optional.empty(),
                Optional.empty(), List.of(), Map.of(area("chemistry"), 12), KnowledgeNode.Kind.LEAF,
                Optional.of(new KnowledgeNode.Eureka(new ResourceLocation("minecraft", "slime_ball"), 0.25)));
        var packet = new SKnowledgeSync(List.of(view), List.of(id("packet_base")), Map.of(area("chemistry"), 40L),
                List.of(id("packet_node")), List.of(id("packet_node")));
        var buf = new FriendlyByteBuf(Unpooled.buffer());
        packet.encode(buf);
        var read = SKnowledgeSync.decode(buf);
        h.assertTrue(read.equals(packet), "the packet must round-trip: " + read);
        h.succeed();
    }

    // ------------------------------------------------------------------ point sources (plan v02, P2)

    private static ResearchSource source(String path, ResearchSource.Event event, String item, String recipeType,
                                         String machine, int first, ResearchSource.Milestone... milestones) {
        return new ResearchSource(id(path), event, Optional.ofNullable(item).map(ResourceLocation::new),
                Optional.ofNullable(recipeType).map(ResourceLocation::new),
                Optional.ofNullable(machine).map(ResourceLocation::new), area("metallurgy"), first,
                List.of(milestones));
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 20)
    public static void researchSourceJsonValidatesWhatEachEventNeeds(GameTestHelper h) {
        java.util.function.Function<String, com.mojang.serialization.DataResult<ResearchSource>> parse = json -> ResearchSource.CODEC
                .parse(JsonOps.INSTANCE, JsonParser.parseString(json))
                .flatMap(definition -> definition.withId(id("parsed_source")));
        var good = parse.apply("""
                {"event":"craft","item":"minecraft:iron_ingot","area":"gtna_test:metallurgy","first":10,
                 "milestones":[{"count":1000,"points":7},{"count":100,"points":5}]}
                """);
        h.assertTrue(good.result().isPresent(), "valid source: " + good.error());
        var source = good.result().orElseThrow();
        h.assertTrue(source.milestones().get(0).count() == 100, "milestones are sorted by count");
        h.assertTrue(source.pointsFor(0, 1) == 10 && source.pointsFor(1, 99) == 0 && source.pointsFor(99, 100) == 5 &&
                source.pointsFor(0, 1000) == 22 && source.pointsFor(5, 5) == 0, "first time plus crossed milestones");
        h.assertTrue(parse.apply("{\"event\":\"craft\",\"area\":\"gtna_test:a\",\"first\":1}").error().isPresent(),
                "craft needs an item");
        h.assertTrue(parse.apply("{\"event\":\"machine_recipe\",\"area\":\"gtna_test:a\",\"first\":1}").error()
                .isPresent(), "machine_recipe needs a recipe type or an item");
        h.assertTrue(parse.apply("{\"event\":\"multiblock_formed\",\"area\":\"gtna_test:a\",\"first\":1}").error()
                .isPresent(), "multiblock_formed needs a machine");
        h.assertTrue(parse.apply("{\"event\":\"obtain\",\"item\":\"minecraft:dirt\",\"area\":\"gtna_test:a\"}")
                .error().isPresent(), "a source must pay something");
        h.assertTrue(parse.apply("{\"event\":\"dance\",\"area\":\"gtna_test:a\",\"first\":1}").error().isPresent(),
                "unknown event");
        h.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 20)
    public static void researchSourcesPayTheFirstTimeAndMilestonesOnly(GameTestHelper h) {
        var server = h.getLevel().getServer();
        var previous = ResearchSources.index();
        var holder = FakePlayerFactory.get(h.getLevel(), new GameProfile(UUID.randomUUID(), "PointEarner"));
        UUID scope = KnowledgeScope.of(holder);
        var craft = source("src_craft", ResearchSource.Event.CRAFT, "minecraft:iron_ingot", null, null, 10,
                new ResearchSource.Milestone(100, 5));
        var obtain = source("src_obtain", ResearchSource.Event.OBTAIN, "minecraft:gold_ingot", null, null, 3);
        try {
            ResearchSources.set(ResearchSources.Index.of(List.of(craft, obtain)));
            KnowledgeTriggers.count(holder, ResearchSources.index().craft(), new ItemStack(Items.IRON_INGOT));
            h.assertTrue(KnowledgeService.points(server, scope).equals(Map.of(area("metallurgy"), 10L)),
                    "first craft pays: " + KnowledgeService.points(server, scope));
            KnowledgeTriggers.count(holder, ResearchSources.index().craft(), new ItemStack(Items.IRON_INGOT, 64));
            h.assertTrue(KnowledgeService.points(server, scope).get(area("metallurgy")) == 10L,
                    "repeating pays nothing until a milestone");
            KnowledgeTriggers.count(holder, ResearchSources.index().craft(), new ItemStack(Items.IRON_INGOT, 35));
            h.assertTrue(KnowledgeService.points(server, scope).get(area("metallurgy")) == 15L,
                    "crossing 100 pays the milestone once");
            h.assertTrue(KnowledgeData.get(server).counter(scope, craft.id()) == 100, "the counter tracks the total");
            KnowledgeTriggers.count(holder, ResearchSources.index().craft(), new ItemStack(Items.COPPER_INGOT, 64));
            h.assertTrue(KnowledgeService.points(server, scope).get(area("metallurgy")) == 15L,
                    "other items do not count");

            holder.getInventory().add(new ItemStack(Items.GOLD_INGOT));
            KnowledgeTriggers.scan(holder, ItemStack.EMPTY);
            KnowledgeTriggers.scan(holder, ItemStack.EMPTY);
            h.assertTrue(KnowledgeService.points(server, scope).get(area("metallurgy")) == 18L,
                    "holding an item pays once, however often it is scanned");
        } finally {
            ResearchSources.set(previous);
            KnowledgeService.reset(server, scope);
            holder.getInventory().clearContent();
        }
        h.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 20)
    public static void researchSourcesPayMachineOwnersForRecipesAndFormedMultiblocks(GameTestHelper h) {
        var server = h.getLevel().getServer();
        var previous = ResearchSources.index();
        var maceratorPos = new BlockPos(1, 2, 1);
        h.setBlock(maceratorPos, GTMachines.MACERATOR[GTValues.LV].getBlock());
        var macerator = ((MetaMachineBlockEntity) h.getBlockEntity(maceratorPos)).getMetaMachine();
        var ebfPos = new BlockPos(3, 2, 1);
        h.setBlock(ebfPos,
                com.gregtechceu.gtceu.common.data.machines.GTMultiMachines.ELECTRIC_BLAST_FURNACE.getBlock());
        var ebf = ((MetaMachineBlockEntity) h.getBlockEntity(ebfPos)).getMetaMachine();
        var recipe = h.getLevel().getRecipeManager().getAllRecipesFor(GTRecipeTypes.MACERATOR_RECIPES).get(0);
        UUID owner = UUID.randomUUID();
        UUID scope = KnowledgeScope.of(owner);
        var byType = source("src_macerator", ResearchSource.Event.MACHINE_RECIPE, null, "gtceu:macerator", null, 4);
        var wrongType = source("src_other_type", ResearchSource.Event.MACHINE_RECIPE, null, "gtceu:centrifuge", null,
                100);
        var formed = source("src_ebf", ResearchSource.Event.MULTIBLOCK_FORMED, null, null,
                com.gregtechceu.gtceu.common.data.machines.GTMultiMachines.ELECTRIC_BLAST_FURNACE.getId().toString(),
                6);
        try {
            ResearchSources.set(ResearchSources.Index.of(List.of(byType, wrongType, formed)));
            ResearchMachineHooks.onRecipeFinished(macerator, recipe);
            h.assertTrue(KnowledgeService.points(server, scope).isEmpty(), "an ownerless machine earns nothing");
            macerator.setOwnerUUID(owner);
            ResearchMachineHooks.onRecipeFinished(macerator, recipe);
            ResearchMachineHooks.onRecipeFinished(macerator, recipe);
            h.assertTrue(KnowledgeService.points(server, scope).equals(Map.of(area("metallurgy"), 4L)),
                    "the recipe type pays its owner once: " + KnowledgeService.points(server, scope));
            h.assertTrue(KnowledgeData.get(server).counter(scope, byType.id()) == 2 &&
                    KnowledgeData.get(server).counter(scope, wrongType.id()) == 0, "only the matching type counts");
            ebf.setOwnerUUID(owner);
            ResearchMachineHooks.onMultiblockFormed(ebf);
            ResearchMachineHooks.onMultiblockFormed(ebf);
            h.assertTrue(KnowledgeService.points(server, scope).get(area("metallurgy")) == 10L,
                    "forming pays once, however often the structure re-forms");
        } finally {
            ResearchSources.set(previous);
            KnowledgeService.reset(server, scope);
        }
        h.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 20)
    public static void analysingAnItemPaysOnceAndFindsTheEurekaOfNodesThatNameIt(GameTestHelper h) {
        var server = h.getLevel().getServer();
        var previousSources = ResearchSources.index();
        var previousGraph = KnowledgeRegistry.graph();
        var pos = new BlockPos(1, 2, 1);
        h.setBlock(pos, GTMachines.MACERATOR[GTValues.LV].getBlock());
        var machine = ((MetaMachineBlockEntity) h.getBlockEntity(pos)).getMetaMachine();
        var recipe = h.getLevel().getRecipeManager().getAllRecipesFor(GTRecipeTypes.MACERATOR_RECIPES).get(0);
        var input = ((net.minecraft.world.item.crafting.Ingredient) recipe.inputs
                .get(com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability.CAP).get(0).content)
                .getItems()[0].getItem();
        var inputId = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(input);
        UUID owner = UUID.randomUUID();
        UUID scope = KnowledgeScope.of(owner);
        var analyze = source("src_analyze", ResearchSource.Event.ANALYZE, inputId.toString(), "gtceu:macerator",
                null, 3);
        var elsewhere = source("src_analyze_elsewhere", ResearchSource.Event.ANALYZE, inputId.toString(),
                "gtceu:centrifuge", null, 100);
        var discounted = bought("analysed", null, null, Map.of(area("metallurgy"), 10),
                Optional.of(new KnowledgeNode.Eureka(inputId, 0.5)));
        var unrelated = bought("unrelated", null, null, Map.of(area("metallurgy"), 10),
                Optional.of(new KnowledgeNode.Eureka(new ResourceLocation("minecraft", "barrier"), 0.5)));
        try {
            var noType = ResearchSource.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(
                    "{\"event\":\"analyze\",\"item\":\"minecraft:dirt\",\"area\":\"gtna_test:a\",\"first\":1}"))
                    .result().orElseThrow().withId(id("no_type"));
            h.assertTrue(noType.error().isPresent(), "analyze needs a recipe type");
            KnowledgeRegistry.set(KnowledgeGraph.build(List.of(discounted, unrelated)).graph());
            ResearchSources.set(ResearchSources.Index.of(List.of(analyze, elsewhere)));
            machine.setOwnerUUID(owner);
            ResearchMachineHooks.onRecipeFinished(machine, recipe);
            ResearchMachineHooks.onRecipeFinished(machine, recipe);
            h.assertTrue(KnowledgeService.points(server, scope).equals(Map.of(area("metallurgy"), 3L)),
                    "the first analysis pays, in the matching machine only: " +
                            KnowledgeService.points(server, scope));
            var data = KnowledgeData.get(server);
            h.assertTrue(data.eurekaMet(scope, discounted.id()) && !data.eurekaMet(scope, unrelated.id()),
                    "the analysis finds the eureka of the node that names the item, and only that one");
            h.assertTrue(KnowledgeService.cost(server, scope, discounted).equals(Map.of(area("metallurgy"), 5)),
                    "the eureka halves the cost");
            h.assertTrue(ResearchSources.analyze(server, scope, inputId, new ResourceLocation("gtceu", "macerator"))
                    .isEmpty(), "a found eureka is found once");

            var packet = new com.raishxn.gtna.network.packet.CKnowledgePurchase(discounted.id());
            var buf = new FriendlyByteBuf(Unpooled.buffer());
            packet.encode(buf);
            h.assertTrue(com.raishxn.gtna.network.packet.CKnowledgePurchase.decode(buf).equals(packet),
                    "the purchase packet round-trips");
            var result = KnowledgeService.purchase(server, scope, discounted.id());
            h.assertTrue(result == KnowledgeService.PurchaseResult.NOT_ENOUGH_POINTS, "3 points do not buy 5");
            h.assertTrue(KnowledgeCommands.purchaseMessage(server, scope, discounted.id(), result,
                    net.minecraft.network.chat.Component.literal("team")).getString().contains("5"),
                    "the message names the discounted cost");
            KnowledgeService.addPoints(server, scope, area("metallurgy"), 2);
            h.assertTrue(KnowledgeService.purchase(server, scope, discounted.id()) ==
                    KnowledgeService.PurchaseResult.PURCHASED, "5 points buy it");
        } finally {
            ResearchSources.set(previousSources);
            KnowledgeRegistry.set(previousGraph);
            KnowledgeService.reset(server, scope);
        }
        h.succeed();
    }
}
