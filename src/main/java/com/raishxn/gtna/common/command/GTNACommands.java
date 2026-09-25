package com.raishxn.gtna.common.command;

import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.raishxn.gtna.api.capability.SteamWirelessNetworkManager;
import com.raishxn.gtna.config.ConfigHolder;
import com.raishxn.gtna.network.GTNANetworkHandler;
import com.raishxn.gtna.network.packet.SStructureDetectHighlight;
import com.raishxn.gtna.planner.neoforge.crafting.Ae2PlannerBridge;
import com.raishxn.gtna.planner.neoforge.crafting.PlannerDiagnosticsReport;

import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = com.raishxn.gtna.GTNACORE.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GTNACommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("gtna_locate")
                .requires(source -> true) // Anyone can use this command
                .then(Commands.argument("x", IntegerArgumentType.integer())
                        .then(Commands.argument("y", IntegerArgumentType.integer())
                                .then(Commands.argument("z", IntegerArgumentType.integer())
                                        .then(Commands.argument("dim", StringArgumentType.string())
                                                .executes(context -> {
                                                    CommandSourceStack source = context.getSource();
                                                    if (source.getEntity() instanceof ServerPlayer player) {
                                                        int x = IntegerArgumentType.getInteger(context, "x");
                                                        int y = IntegerArgumentType.getInteger(context, "y");
                                                        int z = IntegerArgumentType.getInteger(context, "z");
                                                        String dim = StringArgumentType.getString(context, "dim");

                                                        BlockPos pos = new BlockPos(x, y, z);
                                                        ResourceKey<net.minecraft.world.level.Level> dimKey = ResourceKey
                                                                .create(Registries.DIMENSION,
                                                                        new ResourceLocation(dim));

                                                        player.sendSystemMessage(Component.literal(
                                                                "§a[GTNA Terminal] §fLocated connection at §eX: " + x +
                                                                        " Y: " + y + " Z: " + z + " §7(" + dim + ")"));

                                                        long time = System.currentTimeMillis() + 15000L;
                                                        GTNANetworkHandler.sendToPlayer(
                                                                new SStructureDetectHighlight(pos, dimKey, time),
                                                                player);
                                                    }
                                                    return 1;
                                                }))))));

        // Wireless steam network inspection: /gtna steam [player]
        dispatcher.register(Commands.literal("gtna")
                .then(Commands.literal("steam")
                        .executes(context -> steamReport(context.getSource(), null))
                        .then(Commands.literal("add")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.argument("amount", LongArgumentType.longArg())
                                        .executes(context -> steamAdd(context.getSource(),
                                                LongArgumentType.getLong(context, "amount")))))
                        .then(Commands.literal("set")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.argument("amount", LongArgumentType.longArg(0L))
                                        .executes(context -> steamSet(context.getSource(),
                                                LongArgumentType.getLong(context, "amount")))))
                        .then(Commands.argument("player", EntityArgument.player())
                                .requires(source -> source.hasPermission(2))
                                .executes(context -> steamReport(context.getSource(),
                                        EntityArgument.getPlayer(context, "player"))))));

        dispatcher.register(Commands.literal("gtna")
                .then(Commands.literal("planner")
                        .executes(context -> plannerReport(context.getSource()))));
    }

    private static int plannerReport(CommandSourceStack source) {
        List<Ae2PlannerBridge.Diagnostics> diagnostics = Ae2PlannerBridge.activeDiagnostics();
        source.sendSuccess(() -> Component.literal("Nexus Planner: " + diagnostics.size() + " active grid(s)"), false);
        for (Ae2PlannerBridge.Diagnostics diagnostic : diagnostics.stream().limit(4).toList()) {
            source.sendSuccess(() -> Component.literal(PlannerDiagnosticsReport.toJson(diagnostic)), false);
        }
        return diagnostics.size();
    }

    /**
     * Prints the stored steam, the lifetime in/out flow and every connected wireless hatch with its
     * own tank level and last transfer, so a pool that reads 0 mB can be told apart from a pool
     * that is not moving at all.
     */
    private static int steamReport(CommandSourceStack source, ServerPlayer target) {
        ServerPlayer player = target != null ? target : source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.translatable("gtna.command.steam.not_player"));
            return 0;
        }
        ServerLevel level = player.serverLevel();
        UUID owner = player.getUUID();
        String name = player.getGameProfile().getName();
        long stored = SteamWirelessNetworkManager.getUserSteam(level, owner);
        source.sendSuccess(() -> Component.translatable("gtna.command.steam.balance", name, stored), false);

        var flow = SteamWirelessNetworkManager.getFlowStats(level, owner);
        int pulling = SteamWirelessNetworkManager.getActiveInputCount(level, owner);
        source.sendSuccess(() -> Component.translatable("gtna.command.steam.flow",
                FormattingUtil.formatNumbers(flow.added), FormattingUtil.formatNumbers(flow.consumed), pulling), false);

        List<com.raishxn.gtna.common.data.SteamNetworkData.ConnectionInfo> connections = SteamWirelessNetworkManager
                .getConnections(level, owner);
        if (connections.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("gtna.command.steam.no_hatches"), false);
        } else {
            long now = level.getGameTime();
            source.sendSuccess(() -> Component.translatable("gtna.command.steam.hatches", connections.size()), false);
            for (var connection : connections) {
                long rate = connection.isSteel ? ConfigHolder.INSTANCE.wirelessSteam.steelTransferRate :
                        ConfigHolder.INSTANCE.wirelessSteam.bronzeTransferRate;
                Component rateText = rate >= Integer.MAX_VALUE ?
                        Component.translatable("gtna.command.steam.rate.unlimited") :
                        Component.literal(FormattingUtil.formatNumbers(rate));
                Component lastOperation = lastOperationText(connection, now);
                source.sendSuccess(() -> Component.translatable("gtna.command.steam.hatch_entry",
                        Component.translatable(connection.isInput ? "gtna.command.steam.type.input" :
                                "gtna.command.steam.type.output"),
                        Component.translatable(connection.isSteel ? "gtna.command.steam.tier.steel" :
                                "gtna.command.steam.tier.bronze"),
                        connection.pos.dimension().location().toString(),
                        connection.pos.pos().toShortString(),
                        FormattingUtil.formatNumbers(connection.tankAmount),
                        FormattingUtil.formatNumbers(connection.tankCapacity),
                        rateText,
                        lastOperation), false);
            }
        }
        return connections.size() + 1;
    }

    /** "no transfer yet", "pushed N mB (T t ago)" or "pulled N mB (T t ago)" for one hatch. */
    private static Component lastOperationText(com.raishxn.gtna.common.data.SteamNetworkData.ConnectionInfo connection,
                                               long now) {
        if (connection.lastTransferTick < 0 || connection.lastTransferAmount == 0) {
            return Component.translatable("gtna.command.steam.last.none");
        }
        long age = Math.max(0L, now - connection.lastTransferTick);
        String amount = FormattingUtil.formatNumbers(Math.abs(connection.lastTransferAmount));
        return Component.translatable(connection.lastTransferAmount > 0 ? "gtna.command.steam.last.push" :
                "gtna.command.steam.last.pull", amount, age);
    }

    /** Op helper: adds steam to the sender's own network (negative values subtract atomically). */
    private static int steamAdd(CommandSourceStack source, long amount) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.translatable("gtna.command.steam.not_player"));
            return 0;
        }
        ServerLevel level = player.serverLevel();
        if (!SteamWirelessNetworkManager.addSteamToGlobalSteamMap(level, player.getUUID(), amount)) {
            source.sendFailure(Component.translatable("gtna.command.steam.add_failed"));
            return 0;
        }
        long stored = SteamWirelessNetworkManager.getUserSteam(level, player.getUUID());
        source.sendSuccess(() -> Component.translatable("gtna.command.steam.added", amount, stored), false);
        return 1;
    }

    /** Op helper: overwrites the sender's network balance (used to reproduce/verify states). */
    private static int steamSet(CommandSourceStack source, long amount) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.translatable("gtna.command.steam.not_player"));
            return 0;
        }
        ServerLevel level = player.serverLevel();
        SteamWirelessNetworkManager.setUserSteam(level, player.getUUID(), amount);
        source.sendSuccess(() -> Component.translatable("gtna.command.steam.set", amount), false);
        return 1;
    }
}
