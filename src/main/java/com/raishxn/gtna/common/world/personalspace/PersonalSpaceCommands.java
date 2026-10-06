package com.raishxn.gtna.common.world.personalspace;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.RegisterCommandsEvent;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.raishxn.gtna.network.packet.SPersonalSpaceWorldList;

/**
 * {@code /pspace} from the original: {@code give-portal}, {@code tpx}, {@code ls}, {@code where},
 * {@code allow-worldgen-change} and {@code reload-config}. A DIM is a PersonalSpace number (0 = overworld,
 * -1 = nether, 1 = end, as in 1.7.10) or a dimension ID.
 */
public final class PersonalSpaceCommands {

    private static final SimpleCommandExceptionType BAD_DIMENSION = new SimpleCommandExceptionType(
            Component.translatable("commands.pspace.badDimension"));

    private PersonalSpaceCommands() {}

    public static void registerEvent(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("pspace").requires(source -> source.hasPermission(2))
                .then(Commands.literal("ls").executes(PersonalSpaceCommands::list))
                .then(Commands.literal("where").then(Commands.argument("player", EntityArgument.player())
                        .executes(PersonalSpaceCommands::where)))
                .then(Commands.literal("tpx").then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("dim", ResourceLocationArgument.id())
                                .executes(context -> tpx(context, false))
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(context -> tpx(context, true))))))
                .then(Commands.literal("give-portal").then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("dim", ResourceLocationArgument.id())
                                .executes(context -> givePortal(context, false))
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(context -> givePortal(context, true))))))
                .then(Commands.literal("allow-worldgen-change")
                        .then(Commands.argument("dim", IntegerArgumentType.integer(1))
                                .executes(PersonalSpaceCommands::allowWorldgenChange)))
                .then(Commands.literal("reload-config").executes(PersonalSpaceCommands::reloadConfig)));
    }

    /** Converts the original numeric IDs and modern dimension IDs to a level key. */
    static ResourceKey<Level> dimension(ResourceLocation argument) {
        if (argument.getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE)) {
            switch (argument.getPath()) {
                case "0" -> {
                    return Level.OVERWORLD;
                }
                case "-1" -> {
                    return Level.NETHER;
                }
                case "1" -> {
                    return Level.END;
                }
                default -> {
                    if (argument.getPath().chars().allMatch(Character::isDigit)) {
                        return PersonalSpaceWorlds.key(Integer.parseInt(argument.getPath()));
                    }
                }
            }
        }
        return ResourceKey.create(Registries.DIMENSION, argument);
    }

    private static ServerLevel level(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ResourceKey<Level> key;
        try {
            key = dimension(ResourceLocationArgument.getId(context, "dim"));
        } catch (RuntimeException exception) {
            throw BAD_DIMENSION.create();
        }
        ServerLevel level = PersonalSpaceWorlds.level(context.getSource().getServer(), key);
        if (level == null) throw BAD_DIMENSION.create();
        return level;
    }

    private static String label(ResourceKey<Level> key) {
        int id = PersonalSpaceWorlds.id(key);
        return id > 0 ? Integer.toString(id) : key.location().toString();
    }

    private static int list(CommandContext<CommandSourceStack> context) {
        var dimensions = PersonalSpaceDirectory.get(context.getSource().getServer()).dimensions();
        dimensions.keySet().forEach(id -> context.getSource().sendSuccess(() -> Component.literal(
                String.format("%d: %s", id, PersonalSpaceWorlds.key(id).location())), false));
        return dimensions.size();
    }

    private static int where(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        context.getSource().sendSuccess(() -> Component.translatable("commands.pspace.where",
                player.getGameProfile().getName(), label(player.level().dimension())), false);
        return 1;
    }

    private static BlockPos target(CommandContext<CommandSourceStack> context, ServerLevel level, boolean explicit)
                                                                                                                    throws CommandSyntaxException {
        if (explicit) return BlockPosArgument.getBlockPos(context, "pos");
        return PersonalSpaceWorlds.topSpawn(level).above();
    }

    private static int tpx(CommandContext<CommandSourceStack> context, boolean explicit)
                                                                                         throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        ServerLevel level = level(context);
        BlockPos pos = target(context, level, explicit);
        PersonalSpaceWorlds.teleport(player, level, pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5,
                player.getYRot());
        context.getSource().sendSuccess(() -> Component.translatable("commands.pspace.tpx",
                player.getGameProfile().getName(), label(level.dimension()), pos.getX(), pos.getY(), pos.getZ()),
                true);
        return 1;
    }

    private static int givePortal(CommandContext<CommandSourceStack> context, boolean explicit)
                                                                                                throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        ServerLevel level = level(context);
        BlockPos pos = target(context, level, explicit);
        ItemStack stack = new ItemStack(PersonalSpacePortalRegistry.PORTAL_ITEM.get());
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("active", true);
        tag.putString("targetDimension", level.dimension().location().toString());
        tag.putIntArray("target", new int[] { pos.getX(), pos.getY(), pos.getZ() });
        stack.getOrCreateTag().put("BlockEntityTag", tag);
        var item = player.drop(stack, false);
        if (item != null) {
            item.setNoPickUpDelay();
            item.setTarget(player.getUUID());
        }
        return 1;
    }

    private static int allowWorldgenChange(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        int id = IntegerArgumentType.getInteger(context, "dim");
        var server = context.getSource().getServer();
        PersonalSpaceSettings settings = PersonalSpaceDirectory.get(server).settings(id);
        if (settings == null) throw BAD_DIMENSION.create();
        settings.setAllowGenerationChanges(true);
        PersonalSpaceDirectory.get(server).setDirty();
        SPersonalSpaceWorldList.syncAll(server);
        context.getSource().sendSuccess(() -> Component.translatable("commands.pspace.allow-worldgen-change",
                context.getSource().getTextName(), id), true);
        return 1;
    }

    private static int reloadConfig(CommandContext<CommandSourceStack> context) {
        if (!PersonalSpaceConfig.load()) {
            context.getSource().sendFailure(Component.translatable("commands.pspace.reload-config.fail"));
            return 0;
        }
        SPersonalSpaceWorldList.syncAll(context.getSource().getServer());
        context.getSource().sendSuccess(() -> Component.translatable("commands.pspace.reload-config.success"), true);
        return 1;
    }
}
