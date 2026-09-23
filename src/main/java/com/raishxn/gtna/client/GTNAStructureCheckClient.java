package com.raishxn.gtna.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

import com.raishxn.gtna.network.GTNANetworkHandler;
import com.raishxn.gtna.network.packet.CStructureRefreshPacket;

/** Client half of the structure check button: send the request and reveal the world highlight. */
public final class GTNAStructureCheckClient {

    private GTNAStructureCheckClient() {}

    public static void request(BlockPos controllerPos, boolean force) {
        GTNANetworkHandler.INSTANCE.sendToServer(new CStructureRefreshPacket(controllerPos, force));
        Minecraft.getInstance().setScreen(null);
    }
}
