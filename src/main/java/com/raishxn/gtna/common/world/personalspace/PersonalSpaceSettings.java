package com.raishxn.gtna.common.world.personalspace;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Generation and visual settings of one personal dimension: a port of PersonalSpace's {@code DimensionConfig}. Setters
 * clamp exactly like the original and track whether anything changed. Blocks are modern registry IDs; legacy
 * {@code modid:name:meta} names are converted when presets are parsed.
 */
public final class PersonalSpaceSettings {

    public enum SkyType {

        VANILLA,
        BARNADA_C,
        GARDEN_OF_GLASS;

        public static SkyType fromOrdinal(int ordinal) {
            return ordinal < 0 || ordinal >= values().length ? VANILLA : values()[ordinal];
        }

        /** The GalaxySpace and Botania 1.7.10 sky renderers have no Forge 1.20.1 equivalent here. */
        public boolean isLoaded() {
            return this == VANILLA;
        }

        public String buttonText() {
            return name().substring(0, 1);
        }
    }

    public enum DaylightCycle {

        SUN,
        MOON,
        CYCLE;

        public static DaylightCycle fromOrdinal(int ordinal) {
            return ordinal < 0 || ordinal >= values().length ? CYCLE : values()[ordinal];
        }
    }

    public enum GapPreset {

        ROAD,
        SOLID;

        public static GapPreset fromOrdinal(int ordinal) {
            return ordinal < 0 || ordinal >= values().length ? ROAD : values()[ordinal];
        }
    }

    public enum CenterDirection {

        SE,
        SW,
        NE,
        NW;

        public static CenterDirection fromOrdinal(int ordinal) {
            return ordinal < 0 || ordinal >= values().length ? SE : values()[ordinal];
        }
    }

    /** One flat layer; {@code block} is a modern registry ID, air included. */
    public record Layer(String block, int count) {

        public Layer {
            Objects.requireNonNull(block);
            count = Mth.clamp(count, 1, 255);
        }
    }

    public static final String DEFAULT_BIOME = "minecraft:plains";
    public static final String PRESET_UW_VOID = "";
    public static final String PRESET_UW_GARDEN = "minecraft:bedrock;minecraft:dirt*3;minecraft:grass_block";
    public static final String PRESET_UW_MINING = "minecraft:bedrock*4;minecraft:stone*58;minecraft:dirt;" +
            "minecraft:grass_block";
    public static final Pattern PRESET_VALIDATION_PATTERN = Pattern
            .compile("^([^:\\*;]+:[^:\\*;]+(:\\d+)?(\\*\\d+)?;)*([^:\\*;]+:[^:\\*;]+(:\\d+)?(\\*\\d+)?)?$");

    private static final Map<String, String> LEGACY_BIOMES = Map.ofEntries(
            Map.entry("plains", "minecraft:plains"), Map.entry("ocean", "minecraft:ocean"),
            Map.entry("desert", "minecraft:desert"), Map.entry("extreme hills", "minecraft:windswept_hills"),
            Map.entry("forest", "minecraft:forest"), Map.entry("taiga", "minecraft:taiga"),
            Map.entry("swampland", "minecraft:swamp"), Map.entry("river", "minecraft:river"),
            Map.entry("mushroomisland", "minecraft:mushroom_fields"), Map.entry("jungle", "minecraft:jungle"),
            Map.entry("savanna", "minecraft:savanna"), Map.entry("mesa", "minecraft:badlands"));

    private int skyColor = 0xc0d8ff;
    private float starBrightness = 1.0F;
    private boolean weatherEnabled = false;
    private DaylightCycle daylightCycle = DaylightCycle.CYCLE;
    private boolean cloudsEnabled = true;
    private SkyType skyType = SkyType.VANILLA;
    private boolean generatingVegetation = false;
    private boolean generatingTrees = false;
    private boolean allowGenerationChanges = false;
    private String biomeId = DEFAULT_BIOME;
    private List<Layer> layers = new ArrayList<>();

    private long worldTime = 0;
    private boolean raining = false;
    private int rainTime = 0;
    private boolean thundering = false;
    private int thunderTime = 0;
    private boolean timeDataPersisted = false;

    private String boundaryBlockA = "minecraft:yellow_wool";
    private String boundaryBlockB = "minecraft:black_wool";
    private int boundaryChunkIntervalX = 0;
    private int boundaryChunkIntervalZ = 0;

    private int gapWidth = 0;
    private GapPreset gapPreset = GapPreset.ROAD;
    private String gapBlockA = "minecraft:black_wool";
    private String gapBlockB = "minecraft:white_wool";
    private String gapBlockC = "minecraft:white_wool";
    private boolean applyToAllSurfaceLayers = false;

    private boolean centerEnabled = false;
    private CenterDirection centerDirection = CenterDirection.SE;
    private String centerBlock = "minecraft:red_wool";

    private boolean needsSaving = true;

    public PersonalSpaceSettings() {}

    public PersonalSpaceSettings copy() {
        PersonalSpaceSettings copy = new PersonalSpaceSettings();
        copy.copyFrom(this, true, true);
        copy.copyTimeFrom(this);
        return copy;
    }

    /** Equivalent of {@code DimensionConfig.copyFrom}; returns whether the target changed. */
    public boolean copyFrom(PersonalSpaceSettings source, boolean copyVisualInfo, boolean copyGenerationInfo) {
        needsSaving = false;
        if (copyVisualInfo) {
            setSkyColor(source.skyColor);
            setStarBrightness(source.starBrightness);
            setDaylightCycle(source.daylightCycle);
            setCloudsEnabled(source.cloudsEnabled);
            setSkyType(source.skyType);
            setWeatherEnabled(source.weatherEnabled);
        }
        if (copyGenerationInfo) {
            setAllowGenerationChanges(source.allowGenerationChanges);
            setBiomeId(source.biomeId);
            setGeneratingTrees(source.generatingTrees);
            setGeneratingVegetation(source.generatingVegetation);
            layers = new ArrayList<>(source.layers);
            setBoundaryBlockA(source.boundaryBlockA);
            setBoundaryBlockB(source.boundaryBlockB);
            setBoundaryChunkIntervalX(source.boundaryChunkIntervalX);
            setBoundaryChunkIntervalZ(source.boundaryChunkIntervalZ);
            setGapWidth(source.gapWidth);
            setGapPreset(source.gapPreset);
            setGapBlockA(source.gapBlockA);
            setGapBlockB(source.gapBlockB);
            setGapBlockC(source.gapBlockC);
            setApplyToAllSurfaceLayers(source.applyToAllSurfaceLayers);
            setCenterEnabled(source.centerEnabled);
            setCenterDirection(source.centerDirection);
            setCenterBlock(source.centerBlock);
            needsSaving = true;
        }
        boolean modified = needsSaving;
        needsSaving = true;
        return modified;
    }

    private void copyTimeFrom(PersonalSpaceSettings source) {
        worldTime = source.worldTime;
        raining = source.raining;
        rainTime = source.rainTime;
        thundering = source.thundering;
        thunderTime = source.thunderTime;
        timeDataPersisted = source.timeDataPersisted;
    }

    // ---- Presets used before the editor existed and by tests ----

    public static PersonalSpaceSettings voidWorld() {
        return fromPreset(PRESET_UW_VOID);
    }

    public static PersonalSpaceSettings flat() {
        return fromPreset(PRESET_UW_GARDEN);
    }

    public static PersonalSpaceSettings mining() {
        return fromPreset(PRESET_UW_MINING);
    }

    /**
     * Fourth preset of the GTNH pack config: two white concrete floors (y=53 and y=63) with 2x2-chunk lots and
     * one-chunk roads. GTNH uses etfuturum concrete, chisel factory block 6 and ExtraUtilities greenscreen 9; the
     * vanilla blocks here are the closest 1.20.1 equivalents.
     */
    public static final String PRESET_GTNA_ROADS = "minecraft:air*53;minecraft:white_concrete;minecraft:air*9;" +
            "minecraft:white_concrete|B,minecraft:light_gray_concrete,,2,2|G,1,0,minecraft:black_concrete," +
            "minecraft:cyan_concrete,minecraft:white_concrete|C,0,0,";

    public static PersonalSpaceSettings roads() {
        return fromPreset(PRESET_GTNA_ROADS);
    }

    public static PersonalSpaceSettings fromPreset(String fullPreset) {
        PersonalSpaceSettings settings = new PersonalSpaceSettings();
        settings.setLayers(extractLayersPart(fullPreset));
        if (hasExtendedSettings(fullPreset)) settings.applyExtendedSettings(fullPreset);
        return settings;
    }

    // ---- Visual settings ----

    public int getSkyColor() {
        return skyColor;
    }

    public void setSkyColor(int value) {
        if (value != skyColor) {
            needsSaving = true;
            skyColor = Mth.clamp(value, 0, 0xFFFFFF);
        }
    }

    public float getStarBrightness() {
        return starBrightness;
    }

    public void setStarBrightness(float value) {
        if (value != starBrightness) {
            needsSaving = true;
            starBrightness = Mth.clamp(value, 0.0F, 1.0F);
        }
    }

    public boolean isWeatherEnabled() {
        return weatherEnabled;
    }

    public void setWeatherEnabled(boolean value) {
        if (value != weatherEnabled) {
            needsSaving = true;
            weatherEnabled = value;
        }
    }

    public DaylightCycle getDaylightCycle() {
        return daylightCycle;
    }

    public void setDaylightCycle(DaylightCycle value) {
        if (value != daylightCycle) {
            needsSaving = true;
            daylightCycle = value;
        }
    }

    public boolean isNightTime() {
        return daylightCycle == DaylightCycle.MOON;
    }

    public boolean isCloudsEnabled() {
        return cloudsEnabled;
    }

    public void setCloudsEnabled(boolean value) {
        if (value != cloudsEnabled) {
            needsSaving = true;
            cloudsEnabled = value;
        }
    }

    public SkyType getSkyType() {
        return skyType;
    }

    public void setSkyType(SkyType value) {
        if (value != skyType) {
            needsSaving = true;
            skyType = value;
        }
    }

    // ---- Generation settings ----

    public boolean isGeneratingVegetation() {
        return generatingVegetation;
    }

    public void setGeneratingVegetation(boolean value) {
        if (value != generatingVegetation) {
            needsSaving = true;
            generatingVegetation = value;
        }
    }

    public boolean isGeneratingTrees() {
        return generatingTrees;
    }

    public void setGeneratingTrees(boolean value) {
        if (value != generatingTrees) {
            needsSaving = true;
            generatingTrees = value;
        }
    }

    public String getBiomeId() {
        return biomeId;
    }

    public void setBiomeId(String value) {
        value = normalizeBiome(value);
        if (!biomeId.equalsIgnoreCase(value)) {
            needsSaving = true;
            biomeId = value;
        }
    }

    /** Converts the 1.7.10 biome names of the original config to registry IDs. */
    public static String normalizeBiome(String value) {
        if (value == null || value.isBlank()) return DEFAULT_BIOME;
        String trimmed = value.trim();
        String legacy = LEGACY_BIOMES.get(trimmed.toLowerCase(Locale.ROOT));
        return legacy != null ? legacy : trimmed.toLowerCase(Locale.ROOT);
    }

    public boolean getAllowGenerationChanges() {
        return allowGenerationChanges;
    }

    public void setAllowGenerationChanges(boolean value) {
        if (value != allowGenerationChanges) {
            needsSaving = true;
            allowGenerationChanges = value;
        }
    }

    public boolean needsSaving() {
        return needsSaving;
    }

    public void markDirty() {
        needsSaving = true;
    }

    public List<Layer> getLayers() {
        return Collections.unmodifiableList(layers);
    }

    public List<Layer> getMutableLayers() {
        return layers;
    }

    /** Doesn't check that blocks are allowed; call {@link #canUseLayers} on user input. */
    public void setLayers(String preset) {
        layers = parseLayers(preset);
    }

    public String getLayersAsString() {
        return layersToString(layers);
    }

    /** Ground level of the original provider: 128 for a void world, otherwise the stack height capped at 255. */
    public int getGroundLevel() {
        if (layers.isEmpty()) return 128;
        int y = 0;
        for (Layer layer : layers) y += layer.count();
        return Mth.clamp(y, 0, 255);
    }

    public static List<Layer> parseLayers(String preset) {
        if (preset == null) return new ArrayList<>();
        preset = preset.replaceAll("\\s+", "");
        if (preset.isEmpty() || !PRESET_VALIDATION_PATTERN.matcher(preset).matches()) return new ArrayList<>();
        List<Layer> result = new ArrayList<>();
        int y = 0;
        for (String part : preset.split(";")) {
            if (part.isEmpty()) continue;
            String[] components = part.split("\\*", 2);
            int count = 1;
            if (components.length > 1) {
                try {
                    count = Integer.parseInt(components[1]);
                } catch (NumberFormatException exception) {
                    return new ArrayList<>();
                }
            }
            count = Mth.clamp(count, 1, 255);
            String block = PersonalSpaceBlocks.normalize(components[0]);
            if (block == null || PersonalSpaceBlocks.block(block) == null) return new ArrayList<>();
            result.add(new Layer(block, count));
            y += count;
            if (y > 255) break;
        }
        return result;
    }

    public static String layersToString(List<Layer> layers) {
        StringBuilder builder = new StringBuilder();
        for (Layer layer : layers) {
            if (builder.length() > 0) builder.append(';');
            builder.append(layer.block());
            if (layer.count() > 1) builder.append('*').append(layer.count());
        }
        return builder.toString();
    }

    /**
     * {@code layers|B,blockA,blockB,intervalX,intervalZ|G,width,preset,blockA,blockB,blockC|S,allSurfaces|C,1,dir,block}
     * with the center section omitted when the marker is off, as in the original.
     */
    public String getFullPresetString() {
        StringBuilder builder = new StringBuilder(getLayersAsString());
        builder.append("|B,").append(boundaryBlockA).append(',').append(boundaryBlockB).append(',')
                .append(boundaryChunkIntervalX).append(',').append(boundaryChunkIntervalZ);
        builder.append("|G,").append(gapWidth).append(',').append(gapPreset.ordinal()).append(',')
                .append(gapBlockA).append(',').append(gapBlockB).append(',').append(gapBlockC);
        builder.append("|S,").append(applyToAllSurfaceLayers ? 1 : 0);
        if (centerEnabled && !centerBlock.isEmpty()) {
            builder.append("|C,1,").append(centerDirection.ordinal()).append(',').append(centerBlock);
        }
        return builder.toString();
    }

    public static String extractLayersPart(String fullPreset) {
        if (fullPreset == null) return "";
        int pipe = fullPreset.indexOf('|');
        return pipe >= 0 ? fullPreset.substring(0, pipe) : fullPreset;
    }

    public static boolean hasExtendedSettings(String fullPreset) {
        return fullPreset != null && fullPreset.contains("|");
    }

    /** Applies all boundary, gap, surface and center sections of a preset, or none of them. */
    public void applyExtendedSettings(String fullPreset) {
        if (fullPreset == null || !fullPreset.contains("|")) return;
        PersonalSpaceSettings parsed = new PersonalSpaceSettings();
        parsed.copyFrom(this, false, true);
        parsed.setApplyToAllSurfaceLayers(false);
        if (!applyExtendedSettingsSections(fullPreset.split("\\|"), parsed)) return;
        copyFrom(parsed, false, true);
    }

    private static boolean applyExtendedSettingsSections(String[] sections, PersonalSpaceSettings target) {
        for (int index = 1; index < sections.length; index++) {
            String section = sections[index];
            if (section.isEmpty()) continue;
            String[] parts = section.split(",", -1);
            try {
                switch (parts[0]) {
                    case "B" -> {
                        if (parts.length < 5) return false;
                        target.setBoundaryBlockA(presetBlock(parts[1]));
                        target.setBoundaryBlockB(presetBlock(parts[2]));
                        target.setBoundaryChunkIntervalX(Integer.parseInt(parts[3]));
                        target.setBoundaryChunkIntervalZ(Integer.parseInt(parts[4]));
                    }
                    case "G" -> {
                        if (parts.length < 6) return false;
                        target.setGapWidth(Integer.parseInt(parts[1]));
                        target.setGapPreset(GapPreset.fromOrdinal(Integer.parseInt(parts[2])));
                        target.setGapBlockA(presetBlock(parts[3]));
                        target.setGapBlockB(presetBlock(parts[4]));
                        target.setGapBlockC(presetBlock(parts[5]));
                    }
                    case "S" -> {
                        if (parts.length < 2 || parts[1].isEmpty()) return false;
                        target.setApplyToAllSurfaceLayers(Integer.parseInt(parts[1]) != 0);
                    }
                    case "C" -> {
                        if (parts.length < 4 || parts[1].isEmpty() || parts[2].isEmpty() || parts[3].isEmpty()) {
                            break;
                        }
                        if (Integer.parseInt(parts[1]) != 0) {
                            target.setCenterEnabled(true);
                            target.setCenterDirection(CenterDirection.fromOrdinal(Integer.parseInt(parts[2])));
                            target.setCenterBlock(presetBlock(parts[3]));
                        }
                    }
                    default -> {
                        return false;
                    }
                }
            } catch (NumberFormatException exception) {
                return false;
            }
        }
        return true;
    }

    private static String presetBlock(String raw) {
        String normalized = PersonalSpaceBlocks.normalize(raw);
        return normalized == null ? raw.trim() : normalized;
    }

    /** Server or client check of a layer string against the allowed block IDs. */
    public static boolean canUseLayers(String preset, java.util.Set<String> allowedBlocks) {
        if (preset == null) preset = "";
        if (preset.equals(PRESET_UW_GARDEN) || preset.equals(PRESET_UW_VOID) || preset.equals(PRESET_UW_MINING)) {
            return true;
        }
        List<Layer> parsed = parseLayers(preset);
        if (parsed.isEmpty() && !preset.trim().isEmpty()) return false;
        for (Layer layer : parsed) {
            if (PersonalSpaceBlocks.isAir(layer.block())) continue;
            if (!allowedBlocks.contains(layer.block())) return false;
        }
        return true;
    }

    public static boolean canUseBiome(String biome, java.util.Collection<String> allowedBiomes) {
        String normalized = normalizeBiome(biome);
        return normalized.equals(DEFAULT_BIOME) || allowedBiomes.contains(normalized);
    }

    // ---- Boundary, gap and center ----

    public String getBoundaryBlockA() {
        return boundaryBlockA;
    }

    public void setBoundaryBlockA(String value) {
        value = value == null ? "" : value;
        if (!boundaryBlockA.equals(value)) {
            needsSaving = true;
            boundaryBlockA = value;
        }
    }

    public String getBoundaryBlockB() {
        return boundaryBlockB;
    }

    public void setBoundaryBlockB(String value) {
        value = value == null ? "" : value;
        if (!boundaryBlockB.equals(value)) {
            needsSaving = true;
            boundaryBlockB = value;
        }
    }

    public int getBoundaryChunkIntervalX() {
        return boundaryChunkIntervalX;
    }

    public void setBoundaryChunkIntervalX(int value) {
        value = Mth.clamp(value, 0, 20);
        if (boundaryChunkIntervalX != value) {
            needsSaving = true;
            boundaryChunkIntervalX = value;
        }
    }

    public int getBoundaryChunkIntervalZ() {
        return boundaryChunkIntervalZ;
    }

    public void setBoundaryChunkIntervalZ(int value) {
        value = Mth.clamp(value, 0, 20);
        if (boundaryChunkIntervalZ != value) {
            needsSaving = true;
            boundaryChunkIntervalZ = value;
        }
    }

    public int getGapWidth() {
        return gapWidth;
    }

    public void setGapWidth(int value) {
        value = Mth.clamp(value, 0, 5);
        if (gapWidth != value) {
            needsSaving = true;
            gapWidth = value;
        }
    }

    public GapPreset getGapPreset() {
        return gapPreset;
    }

    public void setGapPreset(GapPreset value) {
        if (gapPreset != value) {
            needsSaving = true;
            gapPreset = value;
        }
    }

    public String getGapBlockA() {
        return gapBlockA;
    }

    public void setGapBlockA(String value) {
        value = value == null ? "" : value;
        if (!gapBlockA.equals(value)) {
            needsSaving = true;
            gapBlockA = value;
        }
    }

    public String getGapBlockB() {
        return gapBlockB;
    }

    public void setGapBlockB(String value) {
        value = value == null ? "" : value;
        if (!gapBlockB.equals(value)) {
            needsSaving = true;
            gapBlockB = value;
        }
    }

    public String getGapBlockC() {
        return gapBlockC;
    }

    public void setGapBlockC(String value) {
        value = value == null ? "" : value;
        if (!gapBlockC.equals(value)) {
            needsSaving = true;
            gapBlockC = value;
        }
    }

    public boolean isApplyToAllSurfaceLayers() {
        return applyToAllSurfaceLayers;
    }

    public void setApplyToAllSurfaceLayers(boolean value) {
        if (applyToAllSurfaceLayers != value) {
            needsSaving = true;
            applyToAllSurfaceLayers = value;
        }
    }

    public boolean isCenterEnabled() {
        return centerEnabled;
    }

    public void setCenterEnabled(boolean value) {
        if (centerEnabled != value) {
            needsSaving = true;
            centerEnabled = value;
        }
    }

    public CenterDirection getCenterDirection() {
        return centerDirection;
    }

    public void setCenterDirection(CenterDirection value) {
        if (centerDirection != value) {
            needsSaving = true;
            centerDirection = value;
        }
    }

    public String getCenterBlock() {
        return centerBlock;
    }

    public void setCenterBlock(String value) {
        value = value == null ? "" : value;
        if (!centerBlock.equals(value)) {
            needsSaving = true;
            centerBlock = value;
        }
    }

    /** Boundary, gap and center blocks that are set, for server-side permission checks. */
    public List<String> decorationBlocks(Category category) {
        List<String> result = new ArrayList<>();
        switch (category) {
            case BOUNDARY -> {
                result.add(boundaryBlockA);
                result.add(boundaryBlockB);
            }
            case GAP -> {
                result.add(gapBlockA);
                result.add(gapBlockB);
                result.add(gapBlockC);
            }
            case CENTER -> result.add(centerBlock);
        }
        result.removeIf(String::isEmpty);
        return result;
    }

    public enum Category {
        BOUNDARY,
        GAP,
        CENTER
    }

    // ---- Independent time and weather of the dimension ----

    public long getWorldTime() {
        return worldTime;
    }

    public boolean isRaining() {
        return raining;
    }

    public int getRainTime() {
        return rainTime;
    }

    public boolean isThundering() {
        return thundering;
    }

    public int getThunderTime() {
        return thunderTime;
    }

    public boolean isTimeDataPersisted() {
        return timeDataPersisted;
    }

    public void setWorldTime(long worldTime) {
        this.worldTime = worldTime;
        timeDataPersisted = true;
    }

    public void setRaining(boolean raining) {
        this.raining = raining;
        timeDataPersisted = true;
    }

    public void setRainTime(int rainTime) {
        this.rainTime = rainTime;
        timeDataPersisted = true;
    }

    public void setThundering(boolean thundering) {
        this.thundering = thundering;
        timeDataPersisted = true;
    }

    public void setThunderTime(int thunderTime) {
        this.thunderTime = thunderTime;
        timeDataPersisted = true;
    }

    public void setTimeData(long worldTime, boolean raining, int rainTime, boolean thundering, int thunderTime) {
        this.worldTime = worldTime;
        this.raining = raining;
        this.rainTime = rainTime;
        this.thundering = thundering;
        this.thunderTime = thunderTime;
        this.timeDataPersisted = true;
    }

    // ---- Persistence and sync ----

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        CompoundTag visual = new CompoundTag();
        visual.putInt("skyColor", skyColor);
        visual.putFloat("starBrightness", starBrightness);
        visual.putBoolean("weatherEnabled", weatherEnabled);
        visual.putInt("daylightCycle", daylightCycle.ordinal());
        visual.putBoolean("cloudsEnabled", cloudsEnabled);
        visual.putInt("skyType", skyType.ordinal());
        tag.put("visual", visual);

        CompoundTag worldgen = new CompoundTag();
        worldgen.putString("biomeId", biomeId);
        worldgen.putBoolean("generatingTrees", generatingTrees);
        worldgen.putBoolean("generatingVegetation", generatingVegetation);
        worldgen.putBoolean("allowGenerationChanges", allowGenerationChanges);
        ListTag layerList = new ListTag();
        for (Layer layer : layers) {
            CompoundTag entry = new CompoundTag();
            entry.putString("block", layer.block());
            entry.putInt("count", layer.count());
            layerList.add(entry);
        }
        worldgen.put("layers", layerList);
        worldgen.putBoolean("applyToAllSurfaceLayers", applyToAllSurfaceLayers);
        tag.put("worldgen", worldgen);

        CompoundTag boundary = new CompoundTag();
        boundary.putString("blockA", boundaryBlockA);
        boundary.putString("blockB", boundaryBlockB);
        boundary.putInt("chunkIntervalX", boundaryChunkIntervalX);
        boundary.putInt("chunkIntervalZ", boundaryChunkIntervalZ);
        tag.put("boundary", boundary);

        CompoundTag gap = new CompoundTag();
        gap.putInt("width", gapWidth);
        gap.putInt("preset", gapPreset.ordinal());
        gap.putString("blockA", gapBlockA);
        gap.putString("blockB", gapBlockB);
        gap.putString("blockC", gapBlockC);
        tag.put("gap", gap);

        CompoundTag center = new CompoundTag();
        center.putBoolean("enabled", centerEnabled);
        center.putInt("direction", centerDirection.ordinal());
        center.putString("block", centerBlock);
        tag.put("center", center);

        if (timeDataPersisted) {
            CompoundTag time = new CompoundTag();
            time.putLong("worldTime", worldTime);
            time.putBoolean("raining", raining);
            time.putInt("rainTime", rainTime);
            time.putBoolean("thundering", thundering);
            time.putInt("thunderTime", thunderTime);
            tag.put("time", time);
        }
        return tag;
    }

    public static PersonalSpaceSettings load(CompoundTag tag) {
        PersonalSpaceSettings settings = new PersonalSpaceSettings();
        if (tag.contains("Layers", Tag.TAG_STRING)) {
            settings.loadG0173(tag);
            return settings;
        }
        CompoundTag visual = tag.getCompound("visual");
        settings.setSkyColor(visual.getInt("skyColor"));
        settings.setStarBrightness(visual.getFloat("starBrightness"));
        settings.setWeatherEnabled(visual.getBoolean("weatherEnabled"));
        settings.setDaylightCycle(DaylightCycle.fromOrdinal(visual.getInt("daylightCycle")));
        settings.setCloudsEnabled(visual.getBoolean("cloudsEnabled"));
        settings.setSkyType(SkyType.fromOrdinal(visual.getInt("skyType")));

        CompoundTag worldgen = tag.getCompound("worldgen");
        settings.setBiomeId(worldgen.getString("biomeId"));
        settings.setGeneratingTrees(worldgen.getBoolean("generatingTrees"));
        settings.setGeneratingVegetation(worldgen.getBoolean("generatingVegetation"));
        settings.setAllowGenerationChanges(worldgen.getBoolean("allowGenerationChanges"));
        ListTag layerList = worldgen.getList("layers", Tag.TAG_COMPOUND);
        List<Layer> layers = new ArrayList<>();
        for (int index = 0; index < layerList.size(); index++) {
            CompoundTag entry = layerList.getCompound(index);
            layers.add(new Layer(entry.getString("block"), entry.getInt("count")));
        }
        settings.layers = layers;
        settings.setApplyToAllSurfaceLayers(worldgen.getBoolean("applyToAllSurfaceLayers"));

        CompoundTag boundary = tag.getCompound("boundary");
        settings.setBoundaryBlockA(boundary.getString("blockA"));
        settings.setBoundaryBlockB(boundary.getString("blockB"));
        settings.setBoundaryChunkIntervalX(boundary.getInt("chunkIntervalX"));
        settings.setBoundaryChunkIntervalZ(boundary.getInt("chunkIntervalZ"));

        CompoundTag gap = tag.getCompound("gap");
        settings.setGapWidth(gap.getInt("width"));
        settings.setGapPreset(GapPreset.fromOrdinal(gap.getInt("preset")));
        settings.setGapBlockA(gap.getString("blockA"));
        settings.setGapBlockB(gap.getString("blockB"));
        settings.setGapBlockC(gap.getString("blockC"));

        CompoundTag center = tag.getCompound("center");
        settings.setCenterEnabled(center.getBoolean("enabled"));
        settings.setCenterDirection(CenterDirection.fromOrdinal(center.getInt("direction")));
        settings.setCenterBlock(center.getString("block"));

        if (tag.contains("time", Tag.TAG_COMPOUND)) {
            CompoundTag time = tag.getCompound("time");
            settings.setTimeData(time.getLong("worldTime"), time.getBoolean("raining"), time.getInt("rainTime"),
                    time.getBoolean("thundering"), time.getInt("thunderTime"));
        }
        return settings;
    }

    /** Reads the flat format written by the first local G-0173/G-0174 builds. */
    private void loadG0173(CompoundTag tag) {
        setLayers(tag.getString("Layers"));
        setBiomeId(tag.getString("Biome"));
        setBoundaryChunkIntervalX(tag.getInt("IntervalX"));
        setBoundaryChunkIntervalZ(tag.getInt("IntervalZ"));
        setGapWidth(tag.getInt("GapWidth"));
        setGapPreset("SOLID".equals(tag.getString("GapPreset")) ? GapPreset.SOLID : GapPreset.ROAD);
        setBoundaryBlockA(tag.getString("BoundaryA"));
        setBoundaryBlockB(tag.getString("BoundaryB"));
        setGapBlockA(tag.getString("GapA"));
        setGapBlockB(tag.getString("GapB"));
        setGapBlockC(tag.getString("GapC"));
        setCenterEnabled(tag.getBoolean("CenterEnabled"));
        try {
            setCenterDirection(CenterDirection.valueOf(tag.getString("CenterDirection")));
        } catch (IllegalArgumentException ignored) {
            setCenterDirection(CenterDirection.SE);
        }
        setCenterBlock(tag.getString("CenterBlock"));
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeInt(skyColor);
        buf.writeFloat(starBrightness);
        buf.writeUtf(biomeId);
        buf.writeVarInt(daylightCycle.ordinal());
        buf.writeBoolean(cloudsEnabled);
        buf.writeVarInt(skyType.ordinal());
        buf.writeBoolean(weatherEnabled);
        buf.writeBoolean(generatingVegetation);
        buf.writeBoolean(generatingTrees);
        buf.writeBoolean(allowGenerationChanges);
        buf.writeVarInt(layers.size());
        for (Layer layer : layers) {
            buf.writeUtf(layer.block());
            buf.writeVarInt(layer.count());
        }
        buf.writeUtf(boundaryBlockA);
        buf.writeUtf(boundaryBlockB);
        buf.writeVarInt(boundaryChunkIntervalX);
        buf.writeVarInt(boundaryChunkIntervalZ);
        buf.writeVarInt(gapWidth);
        buf.writeVarInt(gapPreset.ordinal());
        buf.writeUtf(gapBlockA);
        buf.writeUtf(gapBlockB);
        buf.writeUtf(gapBlockC);
        buf.writeBoolean(applyToAllSurfaceLayers);
        buf.writeBoolean(centerEnabled);
        buf.writeVarInt(centerDirection.ordinal());
        buf.writeUtf(centerBlock);
    }

    public static PersonalSpaceSettings read(FriendlyByteBuf buf) {
        PersonalSpaceSettings settings = new PersonalSpaceSettings();
        settings.setSkyColor(buf.readInt());
        settings.setStarBrightness(buf.readFloat());
        settings.setBiomeId(buf.readUtf(256));
        settings.setDaylightCycle(DaylightCycle.fromOrdinal(buf.readVarInt()));
        settings.setCloudsEnabled(buf.readBoolean());
        settings.setSkyType(SkyType.fromOrdinal(buf.readVarInt()));
        settings.setWeatherEnabled(buf.readBoolean());
        settings.setGeneratingVegetation(buf.readBoolean());
        settings.setGeneratingTrees(buf.readBoolean());
        settings.setAllowGenerationChanges(buf.readBoolean());
        int layerCount = Math.min(buf.readVarInt(), 256);
        List<Layer> layers = new ArrayList<>(layerCount);
        for (int index = 0; index < layerCount; index++) {
            layers.add(new Layer(buf.readUtf(256), buf.readVarInt()));
        }
        settings.layers = layers;
        settings.setBoundaryBlockA(buf.readUtf(256));
        settings.setBoundaryBlockB(buf.readUtf(256));
        settings.setBoundaryChunkIntervalX(buf.readVarInt());
        settings.setBoundaryChunkIntervalZ(buf.readVarInt());
        settings.setGapWidth(buf.readVarInt());
        settings.setGapPreset(GapPreset.fromOrdinal(buf.readVarInt()));
        settings.setGapBlockA(buf.readUtf(256));
        settings.setGapBlockB(buf.readUtf(256));
        settings.setGapBlockC(buf.readUtf(256));
        settings.setApplyToAllSurfaceLayers(buf.readBoolean());
        settings.setCenterEnabled(buf.readBoolean());
        settings.setCenterDirection(CenterDirection.fromOrdinal(buf.readVarInt()));
        settings.setCenterBlock(buf.readUtf(256));
        return settings;
    }

    /** Equality of the user-visible settings; time data and the dirty flag are ignored. */
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof PersonalSpaceSettings that)) return false;
        return skyColor == that.skyColor && Float.compare(starBrightness, that.starBrightness) == 0 &&
                weatherEnabled == that.weatherEnabled && daylightCycle == that.daylightCycle &&
                cloudsEnabled == that.cloudsEnabled && skyType == that.skyType &&
                generatingVegetation == that.generatingVegetation && generatingTrees == that.generatingTrees &&
                allowGenerationChanges == that.allowGenerationChanges && biomeId.equals(that.biomeId) &&
                layers.equals(that.layers) && boundaryBlockA.equals(that.boundaryBlockA) &&
                boundaryBlockB.equals(that.boundaryBlockB) &&
                boundaryChunkIntervalX == that.boundaryChunkIntervalX &&
                boundaryChunkIntervalZ == that.boundaryChunkIntervalZ && gapWidth == that.gapWidth &&
                gapPreset == that.gapPreset && gapBlockA.equals(that.gapBlockA) && gapBlockB.equals(that.gapBlockB) &&
                gapBlockC.equals(that.gapBlockC) && applyToAllSurfaceLayers == that.applyToAllSurfaceLayers &&
                centerEnabled == that.centerEnabled && centerDirection == that.centerDirection &&
                centerBlock.equals(that.centerBlock);
    }

    @Override
    public int hashCode() {
        return Objects.hash(skyColor, starBrightness, weatherEnabled, daylightCycle, cloudsEnabled, skyType,
                generatingVegetation, generatingTrees, allowGenerationChanges, biomeId, layers, boundaryBlockA,
                boundaryBlockB, boundaryChunkIntervalX, boundaryChunkIntervalZ, gapWidth, gapPreset, gapBlockA,
                gapBlockB, gapBlockC, applyToAllSurfaceLayers, centerEnabled, centerDirection, centerBlock);
    }

    /** Hash of the generation-related settings used by the editor's preview panel. */
    public int generationHash() {
        return Objects.hash(boundaryBlockA, boundaryBlockB, boundaryChunkIntervalX, boundaryChunkIntervalZ, gapWidth,
                gapPreset, gapBlockA, gapBlockB, gapBlockC, centerEnabled, centerDirection, centerBlock, layers);
    }
}
