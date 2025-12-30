package com.raishxn.gtna.client.renderer;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import net.minecraft.resources.ResourceLocation;

public class GTNATextures {

    // Supondo que seu Mod ID seja "gtna"
    public static final ResourceTexture OVERLAY_STEAM_WIRELESS_IN = new ResourceTexture(new ResourceLocation("gtna", "block/overlay_steam_wireless_in"));
    public static final ResourceTexture OVERLAY_STEAM_WIRELESS_OUT = new ResourceTexture(new ResourceLocation("gtna", "block/overlay_steam_wireless_out"));

    public static void init() {
        // Método vazio só para garantir que a classe carregue se necessário
    }
}