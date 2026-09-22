package com.raishxn.gtna.client.renderer;

import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;

import net.minecraft.resources.ResourceLocation;

/**
 * GTNA GUI textures. {@link #LOGO} is the addon logo shown in the bottom-right corner of our
 * multiblock UIs, the same convention GTNL uses for its machines.
 */
public class GTNATextures {

    public static final ResourceTexture OVERLAY_STEAM_WIRELESS_IN = new ResourceTexture(
            new ResourceLocation("gtna", "block/overlay_steam_wireless_in"));
    public static final ResourceTexture OVERLAY_STEAM_WIRELESS_OUT = new ResourceTexture(
            new ResourceLocation("gtna", "block/overlay_steam_wireless_out"));

    /** The addon logo drawn in the corner of the multiblock screens. */
    public static final ResourceTexture LOGO = new ResourceTexture(new ResourceLocation("gtna", "logo"));
    /** GTNL draws its logo 18x18 in the bottom-right corner of a 176x166 machine screen. */
    public static final int LOGO_SIZE = 18;

    public static void init() {}

    /** The 18x18 addon logo widget at {@code (x, y)}. */
    public static ImageWidget logo(int x, int y) {
        return new ImageWidget(x, y, LOGO_SIZE, LOGO_SIZE, LOGO);
    }
}
