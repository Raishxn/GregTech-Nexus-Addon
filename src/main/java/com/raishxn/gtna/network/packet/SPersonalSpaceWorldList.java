package com.raishxn.gtna.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.network.NetworkEvent;

import com.raishxn.gtna.common.world.personalspace.PersonalSpaceClientState;
import com.raishxn.gtna.common.world.personalspace.PersonalSpaceConfig;
import com.raishxn.gtna.common.world.personalspace.PersonalSpaceDirectory;
import com.raishxn.gtna.common.world.personalspace.PersonalSpaceSettings;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/** Server allow-lists, presets and every dimension's settings, like the original UPDATE_WORLDLIST packet. */
public record SPersonalSpaceWorldList(List<String> biomes, List<String> blocks, List<String> boundaryBlocks,
                                      List<String> gapBlocks, List<String> centerBlocks, List<String> presets,
                                      int dropdownRows, int dropdownColumns,
                                      Map<Integer, PersonalSpaceSettings> dimensions) {

    public static SPersonalSpaceWorldList create(MinecraftServer server) {
        var values = PersonalSpaceConfig.values();
        Map<Integer, PersonalSpaceSettings> dimensions = new LinkedHashMap<>();
        PersonalSpaceDirectory.get(server).dimensions().forEach((id, settings) -> dimensions.put(id, settings.copy()));
        return new SPersonalSpaceWorldList(List.copyOf(PersonalSpaceConfig.allowedBiomes()),
                List.copyOf(PersonalSpaceConfig.allowedBlocks()),
                List.copyOf(PersonalSpaceConfig.allowedBoundaryBlocks()),
                List.copyOf(PersonalSpaceConfig.allowedGapBlocks()),
                List.copyOf(PersonalSpaceConfig.allowedCenterBlocks()), List.copyOf(values.defaultPresets),
                values.dropdownMaxVisibleRows, values.dropdownMaxVisibleColumns, dimensions);
    }

    /** Sends the current list to every player; a player without a network channel (GameTest mock) is skipped. */
    public static void syncAll(MinecraftServer server) {
        var packet = create(server);
        for (var player : server.getPlayerList().getPlayers()) sync(packet, player);
    }

    public static void sync(SPersonalSpaceWorldList packet, net.minecraft.server.level.ServerPlayer player) {
        try {
            com.raishxn.gtna.network.GTNANetworkHandler.sendToPlayer(packet, player);
        } catch (RuntimeException exception) {
            com.raishxn.gtna.GTNACORE.LOGGER.debug("Could not sync PersonalSpace data to {}", player, exception);
        }
    }

    private static void writeList(FriendlyByteBuf buf, List<String> list) {
        buf.writeVarInt(list.size());
        for (String value : list) buf.writeUtf(value);
    }

    private static List<String> readList(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<String> result = new ArrayList<>(Math.min(size, 4096));
        for (int index = 0; index < size; index++) result.add(buf.readUtf());
        return List.copyOf(result);
    }

    public void encode(FriendlyByteBuf buf) {
        writeList(buf, biomes);
        writeList(buf, blocks);
        writeList(buf, boundaryBlocks);
        writeList(buf, gapBlocks);
        writeList(buf, centerBlocks);
        writeList(buf, presets);
        buf.writeVarInt(dropdownRows);
        buf.writeVarInt(dropdownColumns);
        buf.writeVarInt(dimensions.size());
        dimensions.forEach((id, settings) -> {
            buf.writeVarInt(id);
            settings.write(buf);
        });
    }

    public static SPersonalSpaceWorldList decode(FriendlyByteBuf buf) {
        List<String> biomes = readList(buf);
        List<String> blocks = readList(buf);
        List<String> boundary = readList(buf);
        List<String> gap = readList(buf);
        List<String> center = readList(buf);
        List<String> presets = readList(buf);
        int rows = buf.readVarInt();
        int columns = buf.readVarInt();
        int count = buf.readVarInt();
        Map<Integer, PersonalSpaceSettings> dimensions = new LinkedHashMap<>();
        for (int index = 0; index < count; index++) dimensions.put(buf.readVarInt(), PersonalSpaceSettings.read(buf));
        return new SPersonalSpaceWorldList(biomes, blocks, boundary, gap, center, presets, rows, columns, dimensions);
    }

    public static void handle(SPersonalSpaceWorldList msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> PersonalSpaceClientState.update(msg));
        ctx.get().setPacketHandled(true);
    }
}
