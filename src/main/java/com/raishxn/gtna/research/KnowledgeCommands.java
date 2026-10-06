package com.raishxn.gtna.research;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * {@code /gtna research list|info|buy|points|unlock|reset}. Reading is open to everyone for themselves;
 * changing anyone's progress needs permission level 2.
 */
public final class KnowledgeCommands {

    private static final SuggestionProvider<CommandSourceStack> NODES = (context, builder) -> SharedSuggestionProvider
            .suggestResource(KnowledgeRegistry.graph().nodes().stream().map(KnowledgeNode::id), builder);

    private KnowledgeCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("gtna").then(Commands.literal("research")
                .then(Commands.literal("list")
                        .executes(context -> list(context, context.getSource().getPlayerOrException()))
                        .then(Commands.argument("player", EntityArgument.player())
                                .requires(source -> source.hasPermission(2))
                                .executes(context -> list(context, EntityArgument.getPlayer(context, "player")))))
                .then(Commands.literal("info")
                        .then(Commands.argument("node", ResourceLocationArgument.id()).suggests(NODES)
                                .executes(KnowledgeCommands::info)))
                .then(Commands.literal("unlock").requires(source -> source.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("node", ResourceLocationArgument.id()).suggests(NODES)
                                        .executes(context -> unlock(context, false))
                                        .then(Commands.literal("force").executes(context -> unlock(context, true))))))
                .then(Commands.literal("buy")
                        .then(Commands.argument("node", ResourceLocationArgument.id()).suggests(NODES)
                                .executes(KnowledgeCommands::buy)))
                .then(Commands.literal("points")
                        .executes(context -> points(context, context.getSource().getPlayerOrException()))
                        .then(Commands.argument("player", EntityArgument.player())
                                .requires(source -> source.hasPermission(2))
                                .executes(context -> points(context, EntityArgument.getPlayer(context, "player")))
                                .then(Commands.literal("add")
                                        .then(Commands.argument("area", ResourceLocationArgument.id())
                                                .then(Commands.argument("amount", LongArgumentType.longArg())
                                                        .executes(KnowledgeCommands::addPoints))))))
                .then(Commands.literal("reset").requires(source -> source.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(KnowledgeCommands::reset)))));
    }

    private static int list(CommandContext<CommandSourceStack> context, ServerPlayer player) {
        var server = context.getSource().getServer();
        UUID scope = KnowledgeScope.of(player);
        var graph = KnowledgeRegistry.graph();
        var data = KnowledgeData.get(server);
        long done = graph.nodes().stream().filter(node -> data.isUnlocked(scope, node.id())).count();
        context.getSource().sendSuccess(() -> Component.translatable("gtna.research.command.list",
                player.getDisplayName(), done, graph.size()), false);
        for (KnowledgeNode node : graph.nodes()) {
            String key = data.isUnlocked(scope, node.id()) ? "gtna.research.command.entry.unlocked" :
                    "gtna.research.command.entry.locked";
            context.getSource().sendSuccess(() -> Component.translatable(key, node.displayName(), node.tier()), false);
        }
        return (int) done;
    }

    private static int info(CommandContext<CommandSourceStack> context) {
        ResourceLocation id = ResourceLocationArgument.getId(context, "node");
        var node = KnowledgeRegistry.graph().get(id);
        if (node.isEmpty()) {
            context.getSource().sendFailure(Component.translatable("gtna.research.command.unknown", id.toString()));
            return 0;
        }
        context.getSource().sendSuccess(() -> Component.translatable("gtna.research.command.info",
                node.get().displayName(), node.get().tier(), names(node.get().prerequisites())), false);
        return 1;
    }

    private static int unlock(CommandContext<CommandSourceStack> context, boolean force)
                                                                                         throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        ResourceLocation id = ResourceLocationArgument.getId(context, "node");
        var server = context.getSource().getServer();
        UUID scope = KnowledgeScope.of(player);
        var result = KnowledgeService.unlock(server, scope, id, force);
        switch (result) {
            case UNLOCKED -> context.getSource().sendSuccess(() -> Component.translatable(
                    "gtna.research.command.unlock.done", label(id), player.getDisplayName()), true);
            case ALREADY_UNLOCKED -> context.getSource().sendFailure(Component.translatable(
                    "gtna.research.command.unlock.already", label(id), player.getDisplayName()));
            case UNKNOWN_NODE -> context.getSource().sendFailure(
                    Component.translatable("gtna.research.command.unknown", id.toString()));
            case MISSING_PREREQUISITES -> {
                var node = KnowledgeRegistry.graph().get(id).orElseThrow();
                var data = KnowledgeData.get(server);
                var missing = KnowledgeRegistry.graph().missingPrerequisites(node,
                        prerequisite -> data.isUnlocked(scope, prerequisite));
                context.getSource().sendFailure(Component.translatable("gtna.research.command.unlock.missing",
                        node.displayName(), names(missing)));
            }
        }
        return result == KnowledgeService.UnlockResult.UNLOCKED ? 1 : 0;
    }

    private static int buy(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ResourceLocation id = ResourceLocationArgument.getId(context, "node");
        var server = context.getSource().getServer();
        UUID scope = KnowledgeScope.of(player);
        var result = KnowledgeService.purchase(server, scope, id);
        Component message = purchaseMessage(server, scope, id, result, player.getDisplayName());
        if (result == KnowledgeService.PurchaseResult.PURCHASED) {
            context.getSource().sendSuccess(() -> message, false);
        } else {
            context.getSource().sendFailure(message);
        }
        return result == KnowledgeService.PurchaseResult.PURCHASED ? 1 : 0;
    }

    /** What to tell a player about a purchase; shared by {@code /gtna research buy} and the research screen. */
    public static Component purchaseMessage(net.minecraft.server.MinecraftServer server, UUID scope,
                                            ResourceLocation id, KnowledgeService.PurchaseResult result,
                                            Component who) {
        return switch (result) {
            case PURCHASED -> Component.translatable("gtna.research.command.buy.done", label(id));
            case ALREADY_UNLOCKED -> Component.translatable("gtna.research.command.unlock.already", label(id), who);
            case UNKNOWN_NODE -> Component.translatable("gtna.research.command.unknown", id.toString());
            case MISSING_PREREQUISITES -> {
                var node = KnowledgeRegistry.graph().get(id).orElseThrow();
                var data = KnowledgeData.get(server);
                var missing = KnowledgeRegistry.graph().missingPrerequisites(node,
                        prerequisite -> data.isUnlocked(scope, prerequisite));
                yield Component.translatable("gtna.research.command.unlock.missing", node.displayName(),
                        names(missing));
            }
            case MISSING_REQUIREMENT -> {
                var node = KnowledgeRegistry.graph().get(id).orElseThrow();
                yield Component.translatable("gtna.research.command.buy.requirement", node.displayName(),
                        node.requiredItem().map(ResourceLocation::toString).orElse("?"));
            }
            case NOT_ENOUGH_POINTS -> {
                var node = KnowledgeRegistry.graph().get(id).orElseThrow();
                yield Component.translatable("gtna.research.command.buy.points", node.displayName(),
                        describe(KnowledgeService.cost(server, scope, node)));
            }
        };
    }

    private static int points(CommandContext<CommandSourceStack> context, ServerPlayer player) {
        var points = KnowledgeService.points(context.getSource().getServer(), KnowledgeScope.of(player));
        context.getSource().sendSuccess(() -> Component.translatable("gtna.research.command.points",
                player.getDisplayName(), points.isEmpty() ? Component.translatable("gtna.research.command.none") :
                        describe(points)),
                false);
        return points.size();
    }

    private static int addPoints(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        ResourceLocation area = ResourceLocationArgument.getId(context, "area");
        long amount = LongArgumentType.getLong(context, "amount");
        long value = KnowledgeService.addPoints(context.getSource().getServer(), KnowledgeScope.of(player), area,
                amount);
        context.getSource().sendSuccess(() -> Component.translatable("gtna.research.command.points.add",
                KnowledgeNode.areaName(area), value, player.getDisplayName()), true);
        return (int) Math.min(Integer.MAX_VALUE, value);
    }

    /** "Metallurgy 15, Mechanics 10" for a cost or a balance. */
    private static Component describe(java.util.Map<ResourceLocation, ? extends Number> amounts) {
        var text = Component.empty();
        boolean first = true;
        for (var entry : amounts.entrySet()) {
            if (!first) text.append(", ");
            text.append(KnowledgeNode.areaName(entry.getKey())).append(" " + entry.getValue());
            first = false;
        }
        return text;
    }

    private static int reset(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        int removed = KnowledgeService.reset(context.getSource().getServer(), KnowledgeScope.of(player));
        context.getSource().sendSuccess(() -> Component.translatable("gtna.research.command.reset", removed,
                player.getDisplayName()), true);
        return removed;
    }

    private static Component label(ResourceLocation id) {
        return KnowledgeRegistry.graph().get(id).map(KnowledgeNode::displayName)
                .orElseGet(() -> Component.literal(id.toString()));
    }

    private static Component names(List<ResourceLocation> ids) {
        if (ids.isEmpty()) return Component.translatable("gtna.research.command.none");
        return Component.literal(ids.stream().map(id -> label(id).getString()).collect(Collectors.joining(", ")));
    }
}
