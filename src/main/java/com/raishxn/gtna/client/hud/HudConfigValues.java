package com.raishxn.gtna.client.hud;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.raishxn.gtna.GTNACORE;

/**
 * Writes a client config value and persists it, so the HUD editor can save the position/enabled
 * state without the player opening the config screen.
 *
 * <p>
 * The configuration library keeps a {@code ConfigValue} per field; its {@code set} writes the
 * annotated field back (in memory) and {@code ConfigIO.saveClientValues} flushes the file.
 */
@OnlyIn(Dist.CLIENT)
public final class HudConfigValues {

    private HudConfigValues() {}

    public static void set(String fieldId, Object value) {
        var holder = dev.toma.configuration.config.ConfigHolder.getConfig(GTNACORE.MOD_ID).orElse(null);
        if (holder == null) return;
        var configValue = holder.getValueMap().get(fieldId);
        if (configValue == null) return;
        setRaw(configValue, value);
        dev.toma.configuration.config.io.ConfigIO.saveClientValues(holder);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static void setRaw(dev.toma.configuration.config.value.ConfigValue value, Object newValue) {
        value.set(newValue);
    }
}
