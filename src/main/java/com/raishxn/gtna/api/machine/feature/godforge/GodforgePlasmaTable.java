package com.raishxn.gtna.api.machine.feature.godforge;

import java.util.ArrayList;
import java.util.List;

/**
 * Plasma Module recipe table of GTNH {@code Godforge.run()} (GT5-Unofficial a3e1e112), with GTCEu material IDs.
 * GTNH materials with no GTCEu/GTNA counterpart (Ardite, Desh, Oriharukon, Meteoric Iron, Force, Runite, GT++
 * alloys, Bedrockium, Draconium, Cosmic Neutronium) are left out; the GT++ metals ported for the Forge chain
 * (G-0189+: Celestial Tungsten, Astral Titanium, Advanced Nitinol, Rhugnor, Dragon Metal, Chromatic Glass, Ichorium,
 * Hypogen, Six-Phased Copper) are in; the GT++ Thorium-232 duplicate maps to GTCEu Thorium once.
 */
public final class GodforgePlasmaTable {

    /** One conversion: {@code solid} uses a dust (or molten fluid) input, otherwise the material's own fluid. */
    public record Entry(String material, int tier, boolean multiStep, int durationTicks, boolean solid) {}

    public static final List<Entry> ENTRIES;

    static {
        List<Entry> list = new ArrayList<>();
        add(list, 0, false, 20, true, "aluminium", "iron", "calcium", "sulfur", "zinc", "niobium", "tin", "titanium",
                "nickel", "silver", "americium", "antimony", "arsenic", "barium", "beryllium", "caesium", "cadmium",
                "carbon", "cerium", "cobalt", "copper", "dysprosium", "erbium", "europium", "gadolinium", "gallium",
                "gold", "holmium", "indium", "lanthanum", "lithium", "lutetium", "magnesium", "manganese",
                "molybdenum", "neodymium", "palladium", "phosphorus", "potassium", "praseodymium", "promethium",
                "rubidium", "samarium", "silicon", "sodium", "strontium", "tantalum", "tellurium", "terbium",
                "thulium", "tungsten", "uranium_238", "uranium_235", "vanadium", "ytterbium", "yttrium", "chromium",
                "zirconium", "germanium", "thallium", "ruthenium", "rhenium", "rhodium", "iodine", "hafnium",
                "curium");
        add(list, 0, true, 80, true, "bismuth", "boron", "iridium", "naquadah", "osmium", "platinum",
                "plutonium_239", "californium", "advanced_nitinol", "astral_titanium", "celestial_tungsten");
        add(list, 1, false, 200, true, "lead", "plutonium_241", "thorium", "naquadria", "redstone");
        add(list, 1, true, 280, true, "neptunium", "fermium");
        add(list, 2, false, 600, true, "infinity", "rhugnor", "dragon_metal", "chronomatic_glass", "ichorium");
        add(list, 2, true, 1000, true, "tritanium", "flerovium", "neutronium", "hypogen", "six_phased_copper");
        add(list, 0, false, 40, false, "helium", "nitrogen", "argon", "chlorine", "deuterium", "fluorine", "hydrogen",
                "radon", "tritium", "mercury");
        add(list, 0, true, 120, false, "neon", "oxygen", "krypton", "xenon");
        ENTRIES = List.copyOf(list);
    }

    private GodforgePlasmaTable() {}

    private static void add(List<Entry> list, int tier, boolean multiStep, int duration, boolean solid,
                            String... materials) {
        for (String material : materials) list.add(new Entry(material, tier, multiStep, duration, solid));
    }
}
