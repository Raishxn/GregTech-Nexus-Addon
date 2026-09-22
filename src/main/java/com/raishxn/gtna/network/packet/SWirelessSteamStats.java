package com.raishxn.gtna.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import com.raishxn.gtna.client.hud.WirelessSteamHudState;

import java.util.function.Supplier;

/**
 * Server → Client packet: one snapshot of the receiver's wireless steam network for the HUD.
 *
 * <p>
 * Sent once per second per online player (see {@code WirelessSteamHudSync}). It carries only the
 * latest numbers: the client keeps its own history ring buffer for the graph, so no history has to
 * travel over the network.
 */
public class SWirelessSteamStats {

    private final long balance;
    private final long addedPerSecond;
    private final long consumedPerSecond;
    private final int inputHatches;
    private final int outputHatches;

    public SWirelessSteamStats(long balance, long addedPerSecond, long consumedPerSecond,
                               int inputHatches, int outputHatches) {
        this.balance = balance;
        this.addedPerSecond = addedPerSecond;
        this.consumedPerSecond = consumedPerSecond;
        this.inputHatches = inputHatches;
        this.outputHatches = outputHatches;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeLong(this.balance);
        buf.writeVarLong(this.addedPerSecond);
        buf.writeVarLong(this.consumedPerSecond);
        buf.writeVarInt(this.inputHatches);
        buf.writeVarInt(this.outputHatches);
    }

    public static SWirelessSteamStats decode(FriendlyByteBuf buf) {
        return new SWirelessSteamStats(
                buf.readLong(),
                buf.readVarLong(),
                buf.readVarLong(),
                buf.readVarInt(),
                buf.readVarInt());
    }

    public static void handle(SWirelessSteamStats msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> WirelessSteamHudState.update(
                msg.balance, msg.addedPerSecond, msg.consumedPerSecond, msg.inputHatches, msg.outputHatches));
        ctx.get().setPacketHandled(true);
    }

    public long getBalance() {
        return balance;
    }

    public long getAddedPerSecond() {
        return addedPerSecond;
    }

    public long getConsumedPerSecond() {
        return consumedPerSecond;
    }

    public int getInputHatches() {
        return inputHatches;
    }

    public int getOutputHatches() {
        return outputHatches;
    }
}
