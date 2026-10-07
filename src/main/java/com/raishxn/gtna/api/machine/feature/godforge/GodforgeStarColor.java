package com.raishxn.gtna.api.machine.feature.godforge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Star color of the Forge of Gods, ported from GTNH {@code ForgeOfGodsStarColor} and {@code StarColorSetting}
 * (GT5-Unofficial a3e1e112). The string form ({@code StarColorV1:{...}}) matches the original so colors can be
 * shared between packs.
 */
public final class GodforgeStarColor {

    public record Setting(int r, int g, int b, float gamma) {}

    private static final Map<String, GodforgeStarColor> PRESETS = new LinkedHashMap<>(4);

    public static final int DEFAULT_RED = 179;
    public static final int DEFAULT_GREEN = 204;
    public static final int DEFAULT_BLUE = 255;
    public static final float DEFAULT_GAMMA = 3.0f;
    public static final int DEFAULT_CYCLE_SPEED = 1;
    public static final int MAX_COLORS = 9;

    public static final GodforgeStarColor DEFAULT = new GodforgeStarColor("Default")
            .nameKey("gtna.godforge.star_color.preset.default")
            .addColor(DEFAULT_RED, DEFAULT_GREEN, DEFAULT_BLUE, DEFAULT_GAMMA)
            .registerPreset();

    public static final GodforgeStarColor RAINBOW = new GodforgeStarColor("Rainbow")
            .nameKey("gtna.godforge.star_color.preset.rainbow")
            .addColor(255, 0, 0, 3.0f)
            .addColor(255, 255, 0, 3.0f)
            .addColor(0, 255, 0, 3.0f)
            .addColor(0, 255, 255, 3.0f)
            .addColor(255, 0, 255, 3.0f)
            .cycleSpeed(1)
            .registerPreset();

    public static final GodforgeStarColor CLOUDS_PICK = new GodforgeStarColor("Cloud's Pick")
            .nameKey("gtna.godforge.star_color.preset.clouds_pick")
            .addColor(255, 255, 0, 0.8f)
            .addColor(0, 0, 0, 0)
            .addColor(0, 255, 255, 0.4f)
            .addColor(0, 0, 0, 0)
            .cycleSpeed(1)
            .registerPreset();

    public static final GodforgeStarColor MAYAS_PICK = new GodforgeStarColor("Maya's Pick")
            .nameKey("gtna.godforge.star_color.preset.mayas_pick")
            .addColor(0, 0, 0, 0.0f)
            .addColor(109, 201, 225, 1.0f)
            .addColor(255, 255, 255, 3.0f)
            .addColor(255, 172, 210, 1.0f)
            .cycleSpeed(1)
            .registerPreset();

    private String name;
    private String nameKey = "";
    private boolean preset;
    private final List<Setting> settings = new ArrayList<>();
    private int cycleSpeed = DEFAULT_CYCLE_SPEED;

    public GodforgeStarColor(String name) {
        this.name = name;
    }

    public static List<GodforgeStarColor> presets() {
        return new ArrayList<>(PRESETS.values());
    }

    public static GodforgeStarColor preset(String name) {
        return PRESETS.get(name);
    }

    private GodforgeStarColor registerPreset() {
        preset = true;
        PRESETS.put(name, this);
        return this;
    }

    public boolean isPreset() {
        return preset;
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.nameKey = "";
    }

    public String nameKey() {
        return nameKey;
    }

    public GodforgeStarColor nameKey(String key) {
        this.nameKey = key;
        return this;
    }

    public int cycleSpeed() {
        return cycleSpeed;
    }

    public GodforgeStarColor cycleSpeed(int speed) {
        this.cycleSpeed = speed;
        return this;
    }

    public GodforgeStarColor addColor(int r, int g, int b, float gamma) {
        settings.add(new Setting(r, g, b, gamma));
        return this;
    }

    public GodforgeStarColor addColor(Setting setting) {
        settings.add(setting);
        return this;
    }

    public int numColors() {
        return settings.size();
    }

    public Setting color(int index) {
        return settings.get(index);
    }

    public void setColor(int index, Setting setting) {
        settings.set(index, setting);
    }

    public void removeColor(int index) {
        if (index < settings.size()) settings.remove(index);
    }

    public List<Setting> settings() {
        return Collections.unmodifiableList(settings);
    }

    public GodforgeStarColor copy(String newName) {
        GodforgeStarColor copy = new GodforgeStarColor(newName).cycleSpeed(cycleSpeed);
        copy.settings.addAll(settings);
        return copy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GodforgeStarColor that)) return false;
        return cycleSpeed == that.cycleSpeed && name.equals(that.name) && settings.equals(that.settings);
    }

    @Override
    public int hashCode() {
        return name.hashCode() * 31 + settings.hashCode();
    }

    /** Presets serialize by name only, as {@code serializeToNBT} does. */
    public String serialize() {
        return preset ? "Preset:" + name : serializeToString();
    }

    public static GodforgeStarColor deserialize(String raw) {
        if (raw == null) return null;
        if (raw.startsWith("Preset:")) return PRESETS.get(raw.substring(7));
        return deserializeString(raw);
    }

    public String serializeToString() {
        StringBuilder sb = new StringBuilder("StarColorV1:{Name:").append(name).append(",Cycle:").append(cycleSpeed);
        for (Setting s : settings) {
            sb.append("|r:").append(s.r()).append(",g:").append(s.g()).append(",b:").append(s.b()).append(",m:")
                    .append(s.gamma());
        }
        return sb.append('}').toString();
    }

    public static GodforgeStarColor deserializeString(String raw) {
        if (raw == null || !raw.startsWith("StarColorV1:{") || !raw.endsWith("}")) return null;
        try {
            String[] data = raw.substring(13, raw.length() - 1).split("\\|");
            String[] header = data[0].split(",");
            String name = header[0].startsWith("Name:") ? header[0].substring(5) : null;
            Integer cycle = header[1].startsWith("Cycle:") ? Integer.valueOf(header[1].substring(6)) : null;
            List<Setting> colors = new ArrayList<>();
            for (int i = 1; i < data.length; i++) {
                int r = -1, g = -1, b = -1;
                float m = -1;
                for (String part : data[i].split(",")) {
                    String[] kv = part.split(":");
                    switch (kv[0]) {
                        case "r" -> r = Integer.parseInt(kv[1]);
                        case "g" -> g = Integer.parseInt(kv[1]);
                        case "b" -> b = Integer.parseInt(kv[1]);
                        case "m" -> m = Float.parseFloat(kv[1]);
                        default -> {}
                    }
                }
                if (r != -1 && g != -1 && b != -1 && m != -1) colors.add(new Setting(r, g, b, m));
            }
            if (name == null || cycle == null || colors.isEmpty()) return null;
            GodforgeStarColor color = new GodforgeStarColor(name).cycleSpeed(cycle);
            color.settings.addAll(colors);
            return color;
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    /** {@code StarColorStorage}: presets first, then unique custom colors. */
    public static final class Storage {

        private final Map<String, GodforgeStarColor> byName = new LinkedHashMap<>();
        private final List<GodforgeStarColor> byIndex = new ArrayList<>();

        public Storage() {
            initPresets();
        }

        private void initPresets() {
            for (GodforgeStarColor preset : presets()) {
                byName.put(preset.name(), preset);
                byIndex.add(preset);
            }
        }

        public GodforgeStarColor newTemplateColor() {
            String name = "New Star Color";
            int i = 1;
            while (byName.containsKey(name)) name = "New Star Color " + i++;
            return new GodforgeStarColor(name);
        }

        public void store(GodforgeStarColor color) {
            byIndex.add(color);
            String name = color.name();
            int i = 1;
            while (byName.containsKey(name)) name = color.name() + i++;
            if (!color.name().equals(name)) color.setName(name);
            byName.put(name, color);
        }

        public void insert(GodforgeStarColor color, int pos) {
            GodforgeStarColor existing = byIndex.set(pos, color);
            if (existing != null) {
                byName.remove(existing.name());
                byName.put(color.name(), color);
            }
        }

        public void drop(GodforgeStarColor color) {
            GodforgeStarColor existing = byName.remove(color.name());
            if (existing != null) byIndex.remove(existing);
        }

        public GodforgeStarColor byName(String name) {
            return byName.get(name);
        }

        public GodforgeStarColor byIndex(int index) {
            return byIndex.get(index);
        }

        public int size() {
            return byIndex.size();
        }

        public List<String> serializeCustom() {
            List<String> out = new ArrayList<>();
            for (GodforgeStarColor color : byIndex) if (!color.isPreset()) out.add(color.serializeToString());
            return out;
        }

        public void rebuild(List<String> custom) {
            byName.clear();
            byIndex.clear();
            initPresets();
            for (String raw : custom) {
                GodforgeStarColor color = deserializeString(raw);
                if (color != null) store(color);
            }
        }
    }
}
