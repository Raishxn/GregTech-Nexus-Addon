package com.raishxn.gtna.client.hud;

/** Client callbacks installed by ClientProxy; safe to reference from the common machine UI. */
public final class WirelessEnergyHudBridge {

    public static Runnable toggleHud = () -> {};
    public static Runnable openEditor = () -> {};

    private WirelessEnergyHudBridge() {}
}
