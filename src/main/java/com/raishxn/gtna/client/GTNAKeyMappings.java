package com.raishxn.gtna.client;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.mojang.blaze3d.platform.InputConstants;
import com.raishxn.gtna.GTNACORE;
import org.lwjgl.glfw.GLFW;

/**
 * GTNA client keybinds. The only one today opens the HUD editor ({@link
 * com.raishxn.gtna.client.hud.HudEditorScreen}), the GTOCore {@code movableHudToggle} parity.
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = GTNACORE.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class GTNAKeyMappings {

    public static final String CATEGORY = "key.gtna.category";

    public static final KeyMapping OPEN_HUD_EDITOR = new KeyMapping(
            "key.gtna.open_hud_editor", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Z, CATEGORY);

    private GTNAKeyMappings() {}

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_HUD_EDITOR);
    }
}
