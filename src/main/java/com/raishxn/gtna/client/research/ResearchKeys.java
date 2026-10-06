package com.raishxn.gtna.client.research;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

/** The key that opens the research tree (K by default; rebindable in Controls). */
@OnlyIn(Dist.CLIENT)
public final class ResearchKeys {

    public static final KeyMapping OPEN_TREE = new KeyMapping("key.gtna.research_tree", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, "key.categories.gtna");

    private ResearchKeys() {}

    public static void register(RegisterKeyMappingsEvent event) {
        event.register(OPEN_TREE);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        while (OPEN_TREE.consumeClick()) KnowledgeTreeScreen.open();
    }
}
