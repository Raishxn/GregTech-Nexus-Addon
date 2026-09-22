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
    }

    /** Prints the stored steam and the connected wireless hatches for a player's network. */
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

        List<com.raishxn.gtna.common.data.SteamNetworkData.ConnectionInfo> connections = SteamWirelessNetworkManager
                .getConnections(level, owner);
        if (connections.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("gtna.command.steam.no_hatches"), false);
        } else {
            source.sendSuccess(() -> Component.translatable("gtna.command.steam.hatches", connections.size()), false);
            for (var connection : connections) {
                long rate = connection.isSteel ? ConfigHolder.INSTANCE.wirelessSteam.steelTransferRate :
                        ConfigHolder.INSTANCE.wirelessSteam.bronzeTransferRate;
                Component rateText = rate >= Integer.MAX_VALUE ?
                        Component.translatable("gtna.command.steam.rate.unlimited") :
                        Component.literal(FormattingUtil.formatNumbers(rate));
                source.sendSuccess(() -> Component.translatable("gtna.command.steam.hatch_entry",
                        Component.translatable(connection.isInput ? "gtna.command.steam.type.input" :
                                "gtna.command.steam.type.output"),
                        Component.translatable(connection.isSteel ? "gtna.command.steam.tier.steel" :
                                "gtna.command.steam.tier.bronze"),
                        connection.pos.dimension().location().toString(),
                        connection.pos.pos().toShortString(),
                        rateText), false);
            }
        }
        return connections.size() + 1;
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
