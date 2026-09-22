package com.raishxn.gtna.client.hud;

/**
 * Client-only actions exposed to common code without referencing any client class.
 *
 * <p>
 * The wireless steam hatch UI is built in common code, so it cannot call {@code Minecraft} or the
 * HUD screen directly (the runtime dist cleaner rejects that on a dedicated server). It calls these
 * hooks instead; {@code ClientProxy} fills them in on the client. The defaults are no-ops, so the
 * fields are safe on both sides.
 */
public final class WirelessSteamHudBridge {

    /** Toggles the wireless steam HUD on/off (persisted by the config). */
    public static Runnable toggleHud = () -> {};

    /** Opens the HUD editor, where the position can be dragged (persisted by the config). */
    public static Runnable openEditor = () -> {};

    private WirelessSteamHudBridge() {}
}
