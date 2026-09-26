package com.raishxn.gtna.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import com.raishxn.gtna.client.hud.WirelessEnergyHudState;

import java.util.function.Supplier;

/** Once-per-second snapshot of the receiving player's Nexus energy network. */
public record SWirelessEnergyStats(String balance, String capacity, String input, String output, int connections) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(balance);
        buf.writeUtf(capacity);
        buf.writeUtf(input);
        buf.writeUtf(output);
        buf.writeVarInt(connections);
    }

    public static SWirelessEnergyStats decode(FriendlyByteBuf buf) {
        return new SWirelessEnergyStats(buf.readUtf(), buf.readUtf(), buf.readUtf(), buf.readUtf(), buf.readVarInt());
    }

    public static void handle(SWirelessEnergyStats packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> WirelessEnergyHudState.update(packet.balance, packet.capacity,
                packet.input, packet.output, packet.connections));
        context.get().setPacketHandled(true);
    }
}
