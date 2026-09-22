package com.raishxn.gtna.client.hud;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.raishxn.gtna.GTNACORE;
import dev.toma.configuration.config.value.ConfigValue;
import dev.toma.configuration.config.value.ObjectValue;

/**
 * Writes a client config value and persists it, so the HUD editor can save the position/enabled
 * state without the player opening the config screen.
 *
 * <p>
 * The configuration library keeps one {@code ConfigValue} per field, nested inside an
 * {@link ObjectValue} per config section ({@code client}, {@code machines}, ...). The lookup walks
 * that tree by field name; a flat {@code getValueMap().get(id)} misses the nested fields, which is
 * why a dragged HUD used to snap back (the write silently did nothing).
 *
 * <p>
 * Targets the configuration API the pack actually loads (3.1.0): {@code setValue} stages the change
 * and {@code ConfigIO.saveClientValues} commits it to disk. Building against the old 2.2.0 jar
 * compiled calls to {@code ConfigValue.set}, which does not exist at runtime.
 */
@OnlyIn(Dist.CLIENT)
public final class HudConfigValues {

    private HudConfigValues() {}

    public static void set(String fieldId, Object value) {
        var holder = dev.toma.configuration.config.ConfigHolder.getConfig(GTNACORE.MOD_ID).orElse(null);
        if (holder == null) return;
        for (ConfigValue<?> root : holder.values()) {
            ConfigValue<?> target = find(root, fieldId);
            if (target != null) {
                setRaw(target, value);
                dev.toma.configuration.config.io.ConfigIO.saveClientValues(holder);
                return;
            }
        }
    }

    /** Depth-first search of the config value tree for a field id. */
    private static ConfigValue<?> find(ConfigValue<?> value, String fieldId) {
        if (fieldId.equals(value.getId())) {
            return value;
        }
        if (value instanceof ObjectValue objectValue) {
            for (ConfigValue<?> child : objectValue.get().values()) {
                ConfigValue<?> found = find(child, fieldId);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static void setRaw(ConfigValue value, Object newValue) {
        value.setValue(newValue);
    }
}
