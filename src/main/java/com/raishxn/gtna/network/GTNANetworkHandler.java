package com.raishxn.gtna.network;

import com.glodblock.github.glodium.network.NetworkHandler;
import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.network.packet.SStructureDetectHighlight;

public class GTNANetworkHandler extends NetworkHandler {

    public static final GTNANetworkHandler INSTANCE = new GTNANetworkHandler();

    public GTNANetworkHandler() {
        super(GTNACORE.MOD_ID);
    }

    public void init() {
        registerPacket(SStructureDetectHighlight.class, SStructureDetectHighlight::new);
    }
}
