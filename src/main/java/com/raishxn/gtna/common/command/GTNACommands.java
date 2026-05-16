package com.raishxn.gtna.common.command;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.raishxn.gtna.network.GTNANetworkHandler;
import com.raishxn.gtna.network.packet.SStructureDetectHighlight;

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
                                                        ResourceLocation dimLoc = ResourceLocation.tryParse(dim);
                                                        if (dimLoc == null) {
                                                            source.sendFailure(Component.literal("Invalid dimension: " + dim));
                                                            return 0;
                                                        }
                                                        ResourceKey<net.minecraft.world.level.Level> dimKey = ResourceKey
                                                                .create(Registries.DIMENSION, dimLoc);

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
    }
}
