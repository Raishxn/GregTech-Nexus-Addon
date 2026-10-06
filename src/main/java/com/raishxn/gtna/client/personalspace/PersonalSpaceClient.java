package com.raishxn.gtna.client.personalspace;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;

import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.client.personalspace.gui.PersonalSpaceEditScreen;
import com.raishxn.gtna.common.world.personalspace.PersonalSpaceClientState;
import com.raishxn.gtna.common.world.personalspace.PersonalSpacePortalEntity;
import com.raishxn.gtna.common.world.personalspace.PersonalSpacePortalRegistry;

/** Client registration of the PersonalSpace editor, portal book and sky effects. */
public final class PersonalSpaceClient {

    private PersonalSpaceClient() {}

    public static void init(IEventBus modBus) {
        modBus.addListener(PersonalSpaceClient::registerRenderers);
        modBus.addListener(PersonalSpaceClient::registerEffects);
        MinecraftForge.EVENT_BUS.addListener(PersonalSpaceClient::loggingOut);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(PersonalSpacePortalRegistry.PORTAL_ENTITY.get(),
                PersonalSpacePortalRenderer::new);
    }

    private static void registerEffects(RegisterDimensionSpecialEffectsEvent event) {
        event.register(GTNACORE.id("personal_space"), new PersonalSpaceEffects());
    }

    private static void loggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        PersonalSpaceClientState.clear();
    }

    public static void openPortalGui(BlockPos pos) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && mc.level.getBlockEntity(pos) instanceof PersonalSpacePortalEntity portal) {
            mc.setScreen(new PersonalSpaceEditScreen(portal));
        }
    }

    public static void closePortalGui(PersonalSpacePortalEntity portal) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof PersonalSpaceEditScreen screen && screen.tile == portal) mc.setScreen(null);
    }
}
