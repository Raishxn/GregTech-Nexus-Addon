package com.raishxn.gtna.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import com.raishxn.gtna.api.machine.multiblock.GTNASubPatterns;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/** Sends server-script module tooltip keys to clients, including dedicated-server clients. */
public record SKubeModuleDescriptions(Map<ResourceLocation, List<String>> descriptions) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(descriptions.size());
        descriptions.forEach((id, keys) -> {
            buf.writeResourceLocation(id);
            buf.writeVarInt(keys.size());
            keys.forEach(buf::writeUtf);
        });
    }

    public static SKubeModuleDescriptions decode(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        Map<ResourceLocation, List<String>> descriptions = new HashMap<>();
        for (int i = 0; i < size; i++) {
            ResourceLocation id = buf.readResourceLocation();
            int count = buf.readVarInt();
            List<String> keys = new ArrayList<>(count);
            for (int j = 0; j < count; j++) keys.add(buf.readUtf());
            descriptions.put(id, keys);
        }
        return new SKubeModuleDescriptions(descriptions);
    }

    public static void handle(SKubeModuleDescriptions msg, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> GTNASubPatterns.setClientKubeDescriptions(msg.descriptions()));
        context.get().setPacketHandled(true);
    }
}
