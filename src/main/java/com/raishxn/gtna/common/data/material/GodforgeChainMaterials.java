package com.raishxn.gtna.common.data.material;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.fluids.FluidBuilder;

import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.api.data.info.GTNAMaterialFlags;

import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.*;

/**
 * Materials of the GTNH Forge of Gods chain (GT5-Unofficial {@code MaterialsInit}, GT++ {@code MaterialsElements}/
 * {@code MaterialsAlloy}/{@code MaterialMisc}, GoodGenerator {@code GGMaterial}): names, colours and formulas as in
 * GTNH. They are registered first so recipes can be ported and tuned afterwards (see
 * {@code docs/roadmap/forge-of-gods-material-chain.md}).
 *
 * <p>
 * No blast temperature is set and dusts do not smelt, so GTCEu creates no EBF or furnace shortcut; each material's
 * acquisition comes from its own ported recipe. Forms derived from an ingot (plates, rods, gears...) are generated
 * normally. GTNH tiers are mapped to GTCEu as UMV → UXV.
 */
public final class GodforgeChainMaterials {

    public static Material Creon;
    public static Material Mellion;
    public static Material TranscendentMetal;
    public static Material SixPhasedCopper;
    public static Material TengamPurified;
    public static Material TengamAttuned;
    public static Material Infinity;
    public static Material Eternity;
    public static Material Ichorium;
    public static Material Shijima;
    public static Material Churitsu;
    public static Material SuperconductorUIVBase;
    public static Material SuperconductorUMVBase;
    public static Material Hypogen;
    public static Material Rhugnor;
    public static Material CelestialTungsten;
    public static Material AstralTitanium;
    public static Material ChronomaticGlass;
    public static Material Nitinol60;
    public static Material AdvancedNitinol;
    public static Material Quantum;
    public static Material DragonMetal;
    public static Material MetastableOganesson;
    public static Material Shirabon;
    public static Material AtomicSeparationCatalyst;
    public static Material Dilithium;
    public static Material Orundum;
    public static Material MagnetoResonatic;
    public static Material TengamRaw;
    public static Material DTCC;
    public static Material DTPC;
    public static Material DTRC;
    public static Material DTEC;
    public static Material DTSC;
    public static Material ExcitedDTCC;
    public static Material ExcitedDTPC;
    public static Material ExcitedDTRC;
    public static Material ExcitedDTEC;
    public static Material ExcitedDTSC;
    public static Material PhononMedium;
    public static Material PhononCrystalSolution;
    public static Material MutatedLivingSolder;
    public static Material PrimordialMatter;
    public static Material StableBaryonicMatter;
    public static Material HeavyRadox;
    /** Superconductor wires: UIV and UMV (GTCEu UXV), lossless. */
    public static Material SuperconductorUIV;
    public static Material SuperconductorUMV;
    /** GTOCore space-era alloys used as frames by the Advanced Fusion Reactor extensions (GTO MaterialSpaceEra). */
    public static Material PlatinumManganeseAntimonyHeuslerAlloy;
    public static Material OdysseyNanoSuperalloy;
    /** GTNH Stellar Alloy (Nano Forge frames). */
    public static Material StellarAlloy;
    public static Material DimensionallyShiftedSuperfluid;

    private GodforgeChainMaterials() {}

    /** Materials whose liquid uses its original GTNH/Modernity still texture (gtna:block/fluids/fluid.<id>). */
    private static final java.util.Set<String> CUSTOM_FLUIDS = java.util.Set.of("infinity", "eternity", "hypogen",
            "chronomatic_glass", "rhugnor", "dragon_metal", "primordial_matter", "phonon_crystal_solution",
            "phonon_medium", "stable_baryonic_matter");
    /** Liquids with no texture anywhere get their own colour instead of a white material tint. */
    private static final java.util.Map<String, Integer> FLUID_COLORS = java.util.Map.of(
            "shijima", 0xc4d6f2, "platinum_manganese_antimony_heusler_alloy", 0xb0bcc8);

    private static FluidBuilder liquid(String id, int temperature) {
        var builder = new FluidBuilder().temperature(temperature);
        if (CUSTOM_FLUIDS.contains(id)) builder.customStill();
        Integer color = FLUID_COLORS.get(id);
        if (color != null) builder.color(color);
        return builder;
    }

    public static void init() {
        Creon = metal("creon", "Creon", 0x460046, MaterialIconSet.SHINY, true, true).setFormula("⸎");
        Mellion = metal("mellion", "Mellion", 0x3c0505, MaterialIconSet.SHINY, false, true);
        TranscendentMetal = metal("transcendent_metal", "Transcendent Metal", 0x323232, MaterialIconSet.METALLIC, false,
                true).setFormula("TsЖ");
        SixPhasedCopper = metal("six_phased_copper", "Six-Phased Copper", 0xff7814, MaterialIconSet.SHINY, true, true)
                .setFormula("✢");
        TengamPurified = metal("tengam_purified", "Purified Tengam", 0xbadf70, MaterialIconSet.METALLIC, false, false)
                .setFormula("M");
        TengamAttuned = metal("tengam_attuned", "Attuned Tengam", 0xd5ff80, MaterialIconSet.MAGNETIC, false, false)
                .setFormula("M");
        Infinity = metal("infinity", "Infinity", 0xffffff, GodforgeIconSets.INFINITY, true, true).setFormula("If*");
        Eternity = metal("eternity", "Eternity", 0xffffff, GodforgeIconSets.ETERNITY, false, true).setFormula("En⦼");
        Ichorium = metal("ichorium", "Ichorium", 0xd37806, MaterialIconSet.SHINY, true, false).setFormula("IcMa");
        Shijima = metal("shijima", "Shijima", 0xf0f0f0, MaterialIconSet.SHINY, false, false);
        Churitsu = metal("churitsu", "Churitsu", 0x828282, MaterialIconSet.SHINY, false, false);
        SuperconductorUIVBase = metal("superconductor_uiv_base", "Superconductor Base UIV", 0xe558b1,
                MaterialIconSet.SHINY, false, false).setFormula("(C₁₄Os₁₁O₇Ag₃SpH₂O)₄?₁");
        SuperconductorUMVBase = metal("superconductor_umv_base", "Superconductor Base UMV", 0xb526cd,
                MaterialIconSet.SHINY, false, false);
        Hypogen = metal("hypogen", "Hypogen", 0xffffff, GodforgeIconSets.HYPOGEN, true, false).setFormula("Hy⚶");
        Rhugnor = metal("rhugnor", "Rhugnor", 0xbe00ff, MaterialIconSet.SHINY, true, false).setFormula("Fs⚶");
        CelestialTungsten = metal("celestial_tungsten", "Celestial Tungsten", 0x323232, MaterialIconSet.METALLIC, true,
                false).setFormula("✦◆✦");
        AstralTitanium = metal("astral_titanium", "Astral Titanium", 0xdca0f0, MaterialIconSet.SHINY, true, false)
                .setFormula("✧◇✧");
        ChronomaticGlass = metal("chronomatic_glass", "Chromatic Glass", 0xffffff, GodforgeIconSets.CHROMATIC_GLASS,
                false, false)
                .setFormula("⌘☯☯⌘");
        Nitinol60 = metal("nitinol_60", "Nitinol 60", 0xc8c8c8, MaterialIconSet.METALLIC, false, false)
                .setFormula("Ni4Ti6");
        AdvancedNitinol = metal("advanced_nitinol", "Advanced Nitinol", 0xd2b48c, MaterialIconSet.SHINY, false, false)
                .setFormula("⚷⚙⚷ Ni4Ti6");
        Quantum = metal("quantum", "Quantum", 0x6ec8c8, MaterialIconSet.SHINY, false, false);
        DragonMetal = metal("dragon_metal", "Dragonblood", 0xdc2814, GodforgeIconSets.HYPOGEN, false, false)
                .setFormula("۞");
        MetastableOganesson = metal("metastable_oganesson", "Metastable Oganesson", 0x14397f, MaterialIconSet.SHINY,
                false, false).setFormula("Og*");
        Shirabon = metal("shirabon", "Shirabon", 0xe0156d, MaterialIconSet.SHINY, false, false).setFormula("Sh⏧");
        AtomicSeparationCatalyst = metal("atomic_separation_catalyst", "Atomic Separation Catalyst", 0xe85e0c,
                MaterialIconSet.METALLIC, false, false);
        Dilithium = gem("dilithium", "Dilithium", 0xfffafa, MaterialIconSet.DIAMOND, false).setFormula("∳Li∳Li∳");
        Orundum = gem("orundum", "Orundum", 0xcd2626, MaterialIconSet.DIAMOND, true).setFormula("Or");
        MagnetoResonatic = gem("magneto_resonatic", "Magneto Resonatic", 0xdd77ff, MaterialIconSet.MAGNETIC, false);
        TengamRaw = dust("tengam_raw", "Raw Tengam", 0xa0bf60, MaterialIconSet.ROUGH).setFormula("M");
        DTCC = fluid("dimensionally_transcendent_crude_catalyst", "Dimensionally Transcendent Crude Catalyst",
                0x0a1414);
        DTPC = fluid("dimensionally_transcendent_prosaic_catalyst", "Dimensionally Transcendent Prosaic Catalyst",
                0x0a1414);
        DTRC = fluid("dimensionally_transcendent_resplendent_catalyst",
                "Dimensionally Transcendent Resplendent Catalyst", 0x0a1414);
        DTEC = fluid("dimensionally_transcendent_exotic_catalyst", "Dimensionally Transcendent Exotic Catalyst",
                0x0a1414);
        DTSC = fluid("dimensionally_transcendent_stellar_catalyst", "Dimensionally Transcendent Stellar Catalyst",
                0x0a1414).setFormula("Stellar");
        ExcitedDTCC = fluid("excited_dtcc", "Excited Dimensionally Transcendent Crude Catalyst", 0x0a1414);
        ExcitedDTPC = fluid("excited_dtpc", "Excited Dimensionally Transcendent Prosaic Catalyst", 0x233b29);
        ExcitedDTRC = fluid("excited_dtrc", "Excited Dimensionally Transcendent Resplendent Catalyst", 0x261438);
        ExcitedDTEC = fluid("excited_dtec", "Excited Dimensionally Transcendent Exotic Catalyst", 0xf0f029);
        ExcitedDTSC = fluid("excited_dtsc", "Excited Dimensionally Transcendent Stellar Catalyst", 0x7e4b0b)
                .setFormula("[-Stellar-Stellar-]");
        PhononMedium = fluid("phonon_medium", "Lossless Phonon Transfer Medium", 0xffffff);
        PhononCrystalSolution = fluid("phonon_crystal_solution", "Saturated Phononic Crystal Solution", 0xffffff)
                .setFormula("〄");
        MutatedLivingSolder = fluid("mutated_living_solder", "Mutated Living Solder", 0x936d9b)
                .setFormula("?Sn?Bi?If?");
        PrimordialMatter = fluid("primordial_matter", "Liquid Primordial Matter", 0xffffff);
        StableBaryonicMatter = fluid("stable_baryonic_matter", "Stabilised Baryonic Matter", 0xffffff);
        HeavyRadox = fluid("heavy_radox", "Heavy Radox", 0x730073);
        SuperconductorUIV = superconductor("superconductor_uiv", "Superconductor UIV", 0xe558b1, GTValues.UIV);
        // GTO composes Odyssey with Trinium, Naquadria, Molybdenum and nano-scale yttria/alumina; GTNA keeps the three
        // metals GTCEu has. Both keep GTO's blast temperatures, so the EBF recipes come from the composition.
        PlatinumManganeseAntimonyHeuslerAlloy = alloy("platinum_manganese_antimony_heusler_alloy",
                "Platinum-Manganese-Antimony Heusler Alloy", 0xe6e6e6, 13_000,
                com.gregtechceu.gtceu.common.data.GTMaterials.Platinum, 1,
                com.gregtechceu.gtceu.common.data.GTMaterials.Manganese, 1,
                com.gregtechceu.gtceu.common.data.GTMaterials.Antimony, 1);
        OdysseyNanoSuperalloy = alloy("odyssey_nano_superalloy", "Odyssey Nano Superalloy", 0x4c7a6a, 15_000,
                com.gregtechceu.gtceu.common.data.GTMaterials.Trinium, 14,
                com.gregtechceu.gtceu.common.data.GTMaterials.Naquadria, 8,
                com.gregtechceu.gtceu.common.data.GTMaterials.Molybdenum, 17);
        DimensionallyShiftedSuperfluid = fluid("dimensionally_shifted_superfluid", "Dimensionally Shifted Superfluid",
                0x2a1d4f);
        StellarAlloy = metal("stellar_alloy", "Stellar Alloy", 0xc5ced8, MaterialIconSet.SHINY, false, false);
        SuperconductorUMV = superconductor("superconductor_umv", "Superconductor UMV", 0xb526cd, GTValues.UXV);
    }

    private static Material.Builder builder(String id, String lang, int color, MaterialIconSet icon) {
        return new Material.Builder(GTNACORE.id(id)).langValue(lang).color(color).iconSet(icon);
    }

    private static Material metal(String id, String lang, int color, MaterialIconSet icon, boolean plasma,
                                  boolean superdense) {
        var builder = builder(id, lang, color, icon).ingot().liquid(liquid(id, 10_000))
                .flags(GENERATE_PLATE, GENERATE_DENSE, GENERATE_ROD, GENERATE_LONG_ROD, GENERATE_GEAR,
                        GENERATE_SMALL_GEAR, GENERATE_BOLT_SCREW, GENERATE_FRAME, GENERATE_FINE_WIRE, GENERATE_FOIL,
                        GENERATE_RING, GENERATE_SPRING, NO_SMELTING, DISABLE_DECOMPOSITION);
        if (plasma) builder.plasma();
        if (superdense) builder.flags(GTNAMaterialFlags.GENERATE_SUPERDENSE);
        return builder.buildAndRegister();
    }

    private static Material gem(String id, String lang, int color, MaterialIconSet icon, boolean liquid) {
        var builder = builder(id, lang, color, icon).gem()
                .flags(GENERATE_PLATE, GENERATE_LENS, NO_SMELTING, DISABLE_DECOMPOSITION);
        if (liquid) builder.liquid(new FluidBuilder().temperature(3_000));
        return builder.buildAndRegister();
    }

    private static Material dust(String id, String lang, int color, MaterialIconSet icon) {
        return builder(id, lang, color, icon).dust().flags(DISABLE_DECOMPOSITION).buildAndRegister();
    }

    private static Material fluid(String id, String lang, int color) {
        return builder(id, lang, color, MaterialIconSet.FLUID).liquid(liquid(id, 300))
                .flags(DISABLE_DECOMPOSITION).buildAndRegister();
    }

    private static Material alloy(String id, String lang, int color, int blast, Object... components) {
        return builder(id, lang, color, MaterialIconSet.METALLIC).ingot().liquid(liquid(id, blast))
                .components(components).blastTemp(blast)
                .flags(GENERATE_PLATE, GENERATE_FRAME, GENERATE_ROD, GENERATE_LONG_ROD, GENERATE_GEAR,
                        GENERATE_SMALL_GEAR, DISABLE_DECOMPOSITION)
                .buildAndRegister();
    }

    private static Material superconductor(String id, String lang, int color, int tier) {
        return builder(id, lang, color, MaterialIconSet.SHINY).ingot().liquid(new FluidBuilder().temperature(1))
                .cableProperties(GTValues.V[tier], 4, 0, true)
                .flags(GENERATE_FINE_WIRE, NO_SMELTING, DISABLE_DECOMPOSITION).buildAndRegister();
    }
}
