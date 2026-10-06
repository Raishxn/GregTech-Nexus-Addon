package com.raishxn.gtna.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import com.raishxn.gtna.common.world.personalspace.PersonalSpacePortalEntity;
import com.raishxn.gtna.common.world.personalspace.PersonalSpaceSettings;

import java.util.function.Supplier;

/** Editor "Done" button: the server re-validates everything before applying it. */
public record CPersonalSpaceChangeSettings(ResourceLocation dimension, BlockPos pos, PersonalSpaceSettings settings) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(dimension);
        buf.writeBlockPos(pos);
        settings.write(buf);
    }

    public static CPersonalSpaceChangeSettings decode(FriendlyByteBuf buf) {
        return new CPersonalSpaceChangeSettings(buf.readResourceLocation(), buf.readBlockPos(),
                PersonalSpaceSettings.read(buf));
    }

    public static void handle(CPersonalSpaceChangeSettings msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sender = ctx.get().getSender();
            if (sender == null || !sender.level().dimension().location().equals(msg.dimension())) return;
            if (!sender.level().isLoaded(msg.pos())) return;
            if (sender.distanceToSqr(msg.pos().getCenter()) > 64.0 * 64.0) return;
            if (sender.level().getBlockEntity(msg.pos()) instanceof PersonalSpacePortalEntity portal) {
                portal.updateSettings(sender, msg.settings());
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
