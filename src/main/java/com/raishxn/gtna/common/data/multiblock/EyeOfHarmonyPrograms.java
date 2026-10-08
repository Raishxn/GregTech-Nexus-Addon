package com.raishxn.gtna.common.data.multiblock;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import com.raishxn.gtna.GTNACORE;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * Planetary programs of the Eye of Harmony: which item selects which dimension, at which GTNH rocket tier. The
 * products come from the GTCEu ore veins registered for that dimension (see {@link EyeOfHarmonyCatalog}).
 *
 * <p>
 * Rocket tiers follow GTNH's {@code gtneioreplugin} (Overworld/Nether/End 0, Moon 1, Mars 2, Venus and Mercury 4).
 * Glacio does not exist in GTNH; it is placed at tier 5, after Venus and Mercury as in Ad Astra's own order.
 * KubeJS startup scripts can add or remove programs through {@code GTNAStartupEvents.eyeOfHarmony}.
 */
public final class EyeOfHarmonyPrograms {

    /**
     * @param planet    item that selects the program in the controller's planet slot
     * @param stoneDust dust that pads the catalog with three times the ore output (GTNH stone dust per dimension)
     */
    public record Definition(ResourceLocation planet, ResourceKey<Level> dimension, int rocketTier,
                             ResourceLocation stoneDust) {

        public Definition {
            if (rocketTier < 0 || rocketTier > 9) throw new IllegalArgumentException("Rocket tier must be 0-9");
        }

        public ItemStack planetStack() {
            return new ItemStack(BuiltInRegistries.ITEM.get(planet));
        }
    }

    private static final ResourceLocation STONE = new ResourceLocation("gtceu", "stone_dust");
    private static final Map<ResourceLocation, Definition> BUILT_IN = new LinkedHashMap<>();
    private static final Map<ResourceLocation, Definition> PROGRAMS = new LinkedHashMap<>();

    static {
        builtIn("overworld", "minecraft:overworld", 0, STONE);
        builtIn("nether", "minecraft:the_nether", 0, new ResourceLocation("gtceu", "netherrack_dust"));
        builtIn("end", "minecraft:the_end", 0, new ResourceLocation("gtceu", "endstone_dust"));
        builtIn("moon", "ad_astra:moon", 1, STONE);
        builtIn("mars", "ad_astra:mars", 2, STONE);
        builtIn("venus", "ad_astra:venus", 4, STONE);
        builtIn("mercury", "ad_astra:mercury", 4, STONE);
        builtIn("glacio", "ad_astra:glacio", 5, STONE);
        PROGRAMS.putAll(BUILT_IN);
    }

    private EyeOfHarmonyPrograms() {}

    private static void builtIn(String planet, String dimension, int rocketTier, ResourceLocation stoneDust) {
        var id = GTNACORE.id("eye_of_harmony_planet_" + planet);
        BUILT_IN.put(id, new Definition(id, dimension(dimension), rocketTier, stoneDust));
    }

    private static ResourceKey<Level> dimension(String id) {
        return ResourceKey.create(Registries.DIMENSION, new ResourceLocation(id));
    }

    public static synchronized Collection<Definition> all() {
        return Collections.unmodifiableCollection(new java.util.ArrayList<>(PROGRAMS.values()));
    }

    @Nullable
    public static synchronized Definition get(ResourceLocation planet) {
        return PROGRAMS.get(planet);
    }

    @Nullable
    public static Definition forStack(ItemStack stack) {
        return stack.isEmpty() ? null : get(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }

    public static Definition overworld() {
        return BUILT_IN.get(GTNACORE.id("eye_of_harmony_planet_overworld"));
    }

    /** Adds or replaces a program; used by KubeJS. */
    public static synchronized void register(ResourceLocation planet, ResourceLocation dimension, int rocketTier,
                                             @Nullable ResourceLocation stoneDust) {
        PROGRAMS.put(planet, new Definition(planet, ResourceKey.create(Registries.DIMENSION, dimension), rocketTier,
                stoneDust == null ? STONE : stoneDust));
    }

    public static synchronized void remove(ResourceLocation planet) {
        PROGRAMS.remove(planet);
    }

    /** Restores the built-in list before the KubeJS startup event is posted again. */
    public static synchronized void reset() {
        PROGRAMS.clear();
        PROGRAMS.putAll(BUILT_IN);
    }
}
