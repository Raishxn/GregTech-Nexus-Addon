package com.raishxn.gtna.client.personalspace;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.phys.Vec3;

import com.mojang.blaze3d.vertex.PoseStack;
import com.raishxn.gtna.common.world.personalspace.PersonalSpaceClientState;
import com.raishxn.gtna.common.world.personalspace.PersonalSpaceSettings;
import org.joml.Matrix4f;

/**
 * Sky of a personal dimension: clouds at height 256 or none, and with a fixed sun or moon the configured sky color
 * also tints the fog, as the original world provider does.
 */
public final class PersonalSpaceEffects extends DimensionSpecialEffects {

    public PersonalSpaceEffects() {
        super(256.0F, true, SkyType.NORMAL, false, false);
    }

    /** @return the settings of the dimension the client is in, or {@code null}. */
    public static PersonalSpaceSettings current(ClientLevel level) {
        return level == null ? null : PersonalSpaceClientState.settings(level.dimension());
    }

    public static Vec3 skyColor(PersonalSpaceSettings settings) {
        int color = settings.getSkyColor();
        return new Vec3((color >> 16 & 255) / 255.0, (color >> 8 & 255) / 255.0, (color & 255) / 255.0);
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 fogColor, float brightness) {
        PersonalSpaceSettings settings = current(Minecraft.getInstance().level);
        if (settings != null && settings.getDaylightCycle() != PersonalSpaceSettings.DaylightCycle.CYCLE) {
            return skyColor(settings);
        }
        return fogColor.multiply(brightness * 0.94F + 0.06F, brightness * 0.94F + 0.06F, brightness * 0.91F + 0.09F);
    }

    @Override
    public boolean isFoggyAt(int x, int y) {
        return false;
    }

    @Override
    public boolean renderClouds(ClientLevel level, int ticks, float partialTick, PoseStack poseStack, double camX,
                                double camY, double camZ, Matrix4f projectionMatrix) {
        PersonalSpaceSettings settings = current(level);
        return settings != null && !settings.isCloudsEnabled();
    }
}
