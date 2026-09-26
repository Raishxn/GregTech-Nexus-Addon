package com.raishxn.gtna.common.data.material;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.BlastProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ToolProperty;

import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.api.item.tool.GTNAToolType;
import com.raishxn.gtna.common.data.GTNAMaterials;

import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.*;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet.*;
import static com.gregtechceu.gtceu.api.data.chemical.material.properties.BlastProperty.GasTier.LOW;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.raishxn.gtna.api.data.info.GTNAMaterialFlags.*;
import static com.raishxn.gtna.common.data.GTNAMaterials.*;

public class MaterialBuilder {

    public static void init() {
        Stronze = new Material.Builder(GTNACORE.id("stronze"))
                .ingot().fluid().dust()
                .color(0x968030).iconSet(METALLIC)
                .components(Bronze, 1, Steel, 2)
                .blastTemp(1123, BlastProperty.GasTier.LOW)
                .flags(GENERATE_PLATE, GENERATE_ROD, GENERATE_LONG_ROD, GENERATE_BOLT_SCREW,
                        GENERATE_FRAME, GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_RING,
                        GENERATE_ROUND, GENERATE_SPRING, GENERATE_SPRING_SMALL,
                        GENERATE_FOIL, GENERATE_FINE_WIRE, GENERATE_ROTOR, GENERATE_DENSE)
                .fluidPipeProperties(1123, 1000, true, true, true, true)
                .appendFlags(EXT2_METAL)
                .buildAndRegister().setFormula("(SnCu3)(Fe50C)2");

        Breel = new Material.Builder(GTNACORE.id("breel"))
                .dust().ingot().fluid()
                .color(0x506040).iconSet(MaterialIconSet.SHINY)
                .components(Bronze, 2, Steel, 1)
                .blastTemp(1123, BlastProperty.GasTier.LOW)
                .flags(GENERATE_PLATE, GENERATE_ROD, GENERATE_LONG_ROD, GENERATE_BOLT_SCREW,
                        GENERATE_FRAME, GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_RING,
                        GENERATE_ROUND, GENERATE_SPRING, GENERATE_SPRING_SMALL,
                        GENERATE_FOIL, GENERATE_FINE_WIRE, GENERATE_ROTOR, GENERATE_DENSE)
                .fluidPipeProperties(1123, 1000, true, true, true, true)
                .buildAndRegister().setFormula("(Fe50C)(SnCu3)2");

        HastelloyN = new Material.Builder(GTNACORE.id("hastelloy_n"))
                .ingot().fluid()
                .blastTemp(4350, BlastProperty.GasTier.HIGHER, 1920)
                .components(Iridium, 2, Molybdenum, 4, Chromium, 2, Titanium, 2, Nickel, 15)
                .color(0xAAAAAA)
                .iconSet(METALLIC)
                .flags(GENERATE_PLATE, GENERATE_FRAME, DISABLE_DECOMPOSITION)
                .buildAndRegister().setFormula("Ir2Mo4Cr2Ti2Ni15");

        AluminiumBronze = new Material.Builder(GTNACORE.id("aluminium_bronze"))
                .ingot().fluid()
                .color(0xFFDEAD)
                .iconSet(METALLIC)
                .components(Aluminium, 1, Bronze, 6)
                .blastTemp(1200, LOW)
                .flags(GENERATE_PLATE, GENERATE_FRAME, GENERATE_ROD, GENERATE_BOLT_SCREW,
                        GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_RING, GENERATE_FOIL,
                        GENERATE_DOUBLE_PLATE, GENERATE_TRIPLE_PLATE)
                .buildAndRegister().setFormula("Al(CuSn)3");

        EglinSteel = new Material.Builder(GTNACORE.id("eglin_steel"))
                .ingot().fluid()
                .blastTemp(1048, LOW)
                .components(Iron, 4, Kanthal, 1, Invar, 5, Sulfur, 1, Silicon, 1, Carbon, 1)
                .color(0x4e270b)
                .iconSet(METALLIC)
                .flags(GENERATE_PLATE, GENERATE_GEAR, DISABLE_DECOMPOSITION)
                .buildAndRegister();

        RawBrine = new Material.Builder(GTNACORE.id("raw_brine"))
                .fluid()
                .color(0x9f6b26)
                .buildAndRegister();

        HotBrine = new Material.Builder(GTNACORE.id("hot_brine"))
                .liquid(320).color(0xbe6026).buildAndRegister();
        HotDebrominatedBrine = new Material.Builder(GTNACORE.id("hot_debrominated_brine"))
                .liquid(320).color(0xab896d).buildAndRegister();
        HotChlorinatedBrominatedBrine = new Material.Builder(GTNACORE.id("hot_chlorinated_brominated_brine"))
                .liquid(320).color(0xab765d)
                .components(HotBrine, 1, Chlorine, 1)
                .flags(DISABLE_DECOMPOSITION).buildAndRegister();
        HotAlkalineDebrominatedBrine = new Material.Builder(GTNACORE.id("hot_alkaline_debrominated_brine"))
                .liquid(320).color(0xbe8938)
                .components(HotDebrominatedBrine, 2, Chlorine, 1)
                .flags(DISABLE_DECOMPOSITION).buildAndRegister();
        DebrominatedBrine = new Material.Builder(GTNACORE.id("debrominated_brine"))
                .liquid().color(0xab8c6d).buildAndRegister();
        BrominatedChlorineVapor = new Material.Builder(GTNACORE.id("brominated_chlorine_vapor"))
                .gas().color(0xbb9b72)
                .components(Chlorine, 1, Bromine, 1, Steam, 1)
                .flags(DISABLE_DECOMPOSITION).buildAndRegister();
        AcidicBromineSolution = new Material.Builder(GTNACORE.id("acidic_bromine_solution"))
                .liquid().color(0xc49b52)
                .components(Chlorine, 1, Bromine, 1)
                .flags(DISABLE_DECOMPOSITION).buildAndRegister();
        ConcentratedBromineSolution = new Material.Builder(GTNACORE.id("concentrated_bromine_solution"))
                .liquid().color(0x91481e)
                .components(Bromine, 2, Chlorine, 1)
                .flags(DISABLE_DECOMPOSITION).buildAndRegister();
        AcidicBromineExhaust = new Material.Builder(GTNACORE.id("acidic_bromine_exhaust"))
                .gas().color(0x8f681e)
                .components(Steam, 3, Chlorine, 1)
                .buildAndRegister();
        HydrogenIodide = new Material.Builder(GTNACORE.id("hydrogen_iodide"))
                .gas().color(0x8187a6)
                .components(Hydrogen, 1, Iodine, 1)
                .flags(DISABLE_DECOMPOSITION).buildAndRegister();

        DarkSteel = new Material.Builder(GTNACORE.id("dark_steel"))
                .ingot().fluid()
                .color(0x414751)
                .iconSet(METALLIC)
                .components(Iron, 1, Coal, 1, Obsidian, 1)
                .blastTemp(1450, LOW, GTValues.VA[GTValues.MV], 600)
                .flags(GENERATE_PLATE, GENERATE_GEAR, DISABLE_DECOMPOSITION)
                .buildAndRegister().setFormula("FeCOb");

        EndSteel = new Material.Builder(GTNACORE.id("end_steel"))
                .ingot()
                .color(0xD6D980)
                .iconSet(METALLIC)
                .components(Endstone, 1, DarkSteel, 1, Obsidian, 1)
                .blastTemp(3250, LOW, GTValues.VA[GTValues.HV], 900)
                .flags(GENERATE_PLATE, GENERATE_FRAME, GENERATE_ROD, GENERATE_LONG_ROD,
                        GENERATE_BOLT_SCREW, GENERATE_FOIL, GENERATE_FINE_WIRE,
                        GENERATE_DOUBLE_PLATE, GENERATE_TRIPLE_PLATE, GENERATE_QUADRUPLE_PLATE)
                .cableProperties(2048, 1, 0, true)
                .buildAndRegister().setFormula("St4E");

        Indalloy140 = new Material.Builder(GTNACORE.id("indalloy_140"))
                .ingot().fluid().dust()
                .color(0x6A5ACD)
                .iconSet(METALLIC)
                .components(Bismuth, 47, Lead, 25, Tin, 13, Cadmium, 10, Indium, 5)
                .blastTemp(2600, LOW, GTValues.VA[GTValues.EV])
                .flags(GENERATE_PLATE, GENERATE_FOIL, GENERATE_DOUBLE_PLATE, DISABLE_DECOMPOSITION)
                .buildAndRegister().setFormula("In2PbAg");

        Trinaquadalloy = new Material.Builder(GTNACORE.id("trinaquadalloy"))
                .ingot().fluid()
                .color(0x281832)
                .iconSet(METALLIC)
                .components(Trinium, 6, Naquadah, 2, Carbon, 1)
                .blastTemp(8747, BlastProperty.GasTier.HIGHER, GTValues.VA[GTValues.ZPM], 1200)
                .flags(GENERATE_PLATE, GENERATE_DENSE, GENERATE_FRAME, GENERATE_ROD,
                        GENERATE_BOLT_SCREW, GENERATE_FOIL,
                        GENERATE_DOUBLE_PLATE, GENERATE_TRIPLE_PLATE, GENERATE_QUADRUPLE_PLATE,
                        GENERATE_QUINTUPLE_PLATE, GENERATE_SUPERDENSE)
                .buildAndRegister().setFormula("Nq2WsHs");

        MarM200Steel = new Material.Builder(GTNACORE.id("mar_m_200_steel"))
                .ingot().fluid()
                .color(0x515151)
                .iconSet(METALLIC)
                .components(Niobium, 2, Chromium, 9, Aluminium, 5, Titanium, 2, Cobalt, 10,
                        Tungsten, 13, Nickel, 18)
                .blastTemp(4600, BlastProperty.GasTier.HIGH, GTValues.VA[GTValues.IV], 300)
                .flags(GENERATE_FRAME, GENERATE_GEAR, GENERATE_PLATE, GENERATE_DOUBLE_PLATE,
                        DISABLE_DECOMPOSITION)
                .buildAndRegister().setFormula("Nb2Cr9Al5Ti2Co10W13Ni18");

        // GTOCore Inconel-625 / Inconel-792 / Tantalloy-61, copied 1:1 (components, colour, icon set,
        // blast stats and flags) because the ISA Mill casings and the ISA Mill Assembly Line
        // controller recipe use them. Alloy-blast, EBF and mixer routes are the GTCEu automatic ones.
        Inconel625 = new Material.Builder(GTNACORE.id("inconel_625"))
                .ingot().fluid()
                .color(0x00CD66)
                .blastTemp(4850, BlastProperty.GasTier.HIGH, GTValues.VA[GTValues.IV])
                .components(Nickel, 8, Chromium, 6, Molybdenum, 4, Niobium, 4, Titanium, 3, Iron, 2,
                        Aluminium, 2)
                .iconSet(METALLIC)
                .flags(DISABLE_DECOMPOSITION, GENERATE_PLATE, GENERATE_GEAR, GENERATE_SMALL_GEAR,
                        GENERATE_BOLT_SCREW)
                .buildAndRegister();

        Inconel792 = new Material.Builder(GTNACORE.id("inconel_792"))
                .ingot().fluid()
                .blastTemp(5200, BlastProperty.GasTier.HIGH)
                .components(Nickel, 2, Niobium, 1, Aluminium, 2, Nichrome, 1)
                .color(0x44974A)
                .iconSet(METALLIC)
                .flags(GENERATE_BOLT_SCREW, GENERATE_FRAME, GENERATE_GEAR, DISABLE_DECOMPOSITION)
                .buildAndRegister();

        Tantalloy61 = new Material.Builder(GTNACORE.id("tantalloy_61"))
                .ingot().fluid()
                .blastTemp(6900, BlastProperty.GasTier.HIGHER, GTValues.VA[GTValues.IV], 500)
                .components(Tantalum, 13, Tungsten, 12, Titanium, 6, Yttrium, 4)
                .color(0x363636)
                .iconSet(METALLIC)
                .flags(GENERATE_BOLT_SCREW, DISABLE_DECOMPOSITION)
                .buildAndRegister();

        FallKing = new Material.Builder(GTNACORE.id("fall_king"))
                .ingot().fluid()
                .color(0xFFCF6B)
                .iconSet(BRIGHT)
                .components(Helium, 1, Lithium, 1, Cobalt, 1, Platinum, 1, Erbium, 1)
                .blastTemp(5400, BlastProperty.GasTier.HIGH)
                .flags(DISABLE_DECOMPOSITION)
                .buildAndRegister().setFormula("HeLiCoPtEr");

        Acrylonitrile = new Material.Builder(GTNACORE.id("acrylonitrile"))
                .fluid()
                .components(Carbon, 3, Hydrogen, 3, Nitrogen, 1)
                .flags(DISABLE_DECOMPOSITION)
                .color(0xA4A4E1)
                .iconSet(DULL)
                .buildAndRegister();

        Abs = new Material.Builder(GTNACORE.id("abs"))
                .polymer()
                .fluid()
                .components(Acrylonitrile, 1, Butadiene, 1, Styrene, 2)
                .color(0xE8E7E5)
                .iconSet(DULL)
                .flags(GENERATE_PLATE, DISABLE_DECOMPOSITION)
                .buildAndRegister().setFormula("ABS");

        Polystyrene = new Material.Builder(GTNACORE.id("polystyrene"))
                .polymer()
                .fluid()
                .components(Styrene, 1)
                .color(0xC6C6C6)
                .iconSet(DULL)
                .flags(GENERATE_FOIL, DISABLE_DECOMPOSITION)
                .buildAndRegister().setFormula("(C8H8)n");

        GTNAMaterials.CobaltOxide = new Material.Builder(GTNACORE.id("cobalt_oxide"))
                .dust()
                .color(0x355D87)
                .iconSet(DULL)
                .components(Cobalt, 1, Oxygen, 1)
                .buildAndRegister().setFormula("CoO");

        LithiumOxide = new Material.Builder(GTNACORE.id("lithium_oxide"))
                .dust()
                .color(0xE7EEF5)
                .iconSet(BRIGHT)
                .components(Lithium, 2, Oxygen, 1)
                .buildAndRegister().setFormula("Li2O");

        ZirconiumOxide = new Material.Builder(GTNACORE.id("zirconium_oxide"))
                .dust()
                .color(0x3C3C3C)
                .iconSet(DULL)
                .components(Zirconium, 1, Oxygen, 2)
                .buildAndRegister().setFormula("ZrO2");

        ZirconiaCeramic = new Material.Builder(GTNACORE.id("zirconia_ceramic"))
                .dust()
                .color(0xEAEAEA)
                .iconSet(BRIGHT)
                .flags(GENERATE_BRICK)
                .buildAndRegister().setFormula("ZrO2-C");

        ClayCompound = new Material.Builder(GTNACORE.id("clay_compound"))
                .dust().ingot().fluid()
                .color(0xAA8866).iconSet(MaterialIconSet.DULL)
                .components(Flint, 1, Clay, 1, Stone, 1)

                .flags(GENERATE_PLATE, GENERATE_ROD, GENERATE_LONG_ROD, GENERATE_BOLT_SCREW,
                        GENERATE_FRAME, GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_RING,
                        GENERATE_ROUND)
                .fluidPipeProperties(167, 1000, false, false, true, false)

                .buildAndRegister().setFormula("?(NA2LiAl2Si2O7(H2O)2)(SiO2)");

        Echoite = new Material.Builder(GTNACORE.id("echoite"))
                .ingot().fluid().plasma().dust()
                .blastTemp(1730, LOW)
                .color(0x26734d)
                .iconSet(METALLIC)
                .flags(GENERATE_PLATE, GENERATE_ROD, GENERATE_LONG_ROD, GENERATE_BOLT_SCREW,
                        GENERATE_FRAME, GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_RING,
                        GENERATE_ROUND, GENERATE_ROTOR, GENERATE_SINGULARITY, GENERATE_DOUBLE_INGOT,
                        GENERATE_TRIPLE_INGOT, GENERATE_QUADRUPLE_INGOT, GENERATE_QUINTUPLE_INGOT,
                        GENERATE_DOUBLE_PLATE, GENERATE_TRIPLE_PLATE, GENERATE_QUADRUPLE_PLATE,
                        GENERATE_QUINTUPLE_PLATE, GENERATE_DENSE, GENERATE_SUPERDENSE)

                .cableProperties(GTValues.V[GTValues.MV], 32, 0, true)
                .toolStats(ToolProperty.Builder.of(8.0F, 100.0F, 64, 6, GTNAToolType.VAJRA).magnetic()
                        .unbreakable().build())
                .fluidPipeProperties(2000000, 100000, true, true, true, true)
                .buildAndRegister().setFormula("Ec");

        // --- LINGOTE ESPECIAL (Compressed Steam) ---

        CompressedSteam = new Material.Builder(GTNACORE.id("compressed_steam"))
                .ingot().fluid().dust()
                .color(0xCCCCCC).iconSet(MaterialIconSet.SHINY)
                .flags(NO_SMELTING, GENERATE_PLATE, GENERATE_ROD, GENERATE_FRAME, GENERATE_GEAR,
                        GENERATE_DENSE, GENERATE_SUPERDENSE, GENERATE_DOUBLE_PLATE,
                        GENERATE_TRIPLE_PLATE, GENERATE_QUADRUPLE_PLATE, GENERATE_QUINTUPLE_PLATE, GENERATE_ROTOR)
                .fluidPipeProperties(500, 500, true, true, true, false)
                .buildAndRegister().setFormula("H2O");

        // --- FLUIDOS (Vapores) ---

        DenseSupercriticalSteam = new Material.Builder(GTNACORE.id("dense_supercritical_steam"))
                .gas(295000).fluid()
                .color(0xA0A0A0)
                .iconSet(SHINY)
                .buildAndRegister();

        SuperHeatedSteam = new Material.Builder(GTNACORE.id("super_heated_steam"))
                .gas(600000).fluid()
                .color(0xC0C0C0)
                .iconSet(BRIGHT)
                .flags(DISABLE_DECOMPOSITION)
                .buildAndRegister();

        InsanelySupercriticalSteam = new Material.Builder(GTNACORE.id("insanely_supercritical_steam"))
                .gas(1000000).fluid()
                .color(0xFFFFFF)
                .iconSet(RADIOACTIVE)
                .flags(DISABLE_DECOMPOSITION)
                .buildAndRegister();

        // ------------------------------------------------------------------------------------
        // GTOCore Industrial Flotation Cell / Vacuum Drying Furnace pair (G-0107/G-0108).
        // Material definitions copied 1:1 from GTO's MaterialA/MaterialB (components, colour, icon
        // set, flags and formula) so the ported recipes keep the original reagents and products.
        // ------------------------------------------------------------------------------------

        // GTOCore alloy additions used by the Hastelloy-N75 casing family and the flotation
        // controller recipe (GTO MaterialA:693 and MaterialA:1060).
        HastelloyN75 = new Material.Builder(GTNACORE.id("hastelloy_n_75"))
                .ingot().fluid()
                .color(0x8b6914)
                .blastTemp(4550, BlastProperty.GasTier.HIGH, GTValues.VA[GTValues.EV])
                .components(Nickel, 15, Molybdenum, 9, Chromium, 4, Titanium, 2, Erbium, 2)
                .iconSet(METALLIC)
                .flags(DISABLE_DECOMPOSITION, GENERATE_BOLT_SCREW, GENERATE_GEAR, GENERATE_SMALL_GEAR,
                        GENERATE_PLATE)
                .buildAndRegister();

        Stellite = new Material.Builder(GTNACORE.id("stellite"))
                .ingot().fluid()
                .blastTemp(4310, BlastProperty.GasTier.HIGH, 1920)
                .components(Cobalt, 9, Chromium, 9, Manganese, 5, Titanium, 2)
                .color(0x888192)
                .iconSet(METALLIC)
                .flags(GENERATE_GEAR, DISABLE_DECOMPOSITION, GENERATE_FOIL, GENERATE_PLATE)
                .buildAndRegister();

        // GTOCore flotation reagents (GTO MaterialB:2776, :2784, :2952).
        SodiumEthylxanthate = new Material.Builder(GTNACORE.id("sodium_ethylxanthate"))
                .dust()
                .color(0xcdad00)
                .components(Carbon, 3, Hydrogen, 5, Sodium, 1, Oxygen, 1, Sulfur, 2)
                .flags(DISABLE_DECOMPOSITION)
                .iconSet(DULL)
                .buildAndRegister();

        PotassiumEthylxanthate = new Material.Builder(GTNACORE.id("potassium_ethylxanthate"))
                .dust()
                .color(0xcdc8b1)
                .components(Carbon, 3, Hydrogen, 5, Potassium, 1, Oxygen, 1, Sulfur, 2)
                .flags(DISABLE_DECOMPOSITION)
                .iconSet(DULL)
                .buildAndRegister();

        Turpentine = new Material.Builder(GTNACORE.id("turpentine"))
                .fluid()
                .components(Carbon, 10, Hydrogen, 16)
                .color(0x9acd32)
                .flags(DISABLE_DECOMPOSITION)
                .iconSet(FLUID)
                .buildAndRegister();

        // GTOCore ore foams (GTO MaterialB:2974-3070); every one has a drying consumer.
        PyropeFront = oreFront("pyrope_front", Pyrope, 0x8b0000);
        RedstoneFront = oreFront("redstone_front", Redstone, 0xee0000);
        ChalcopyriteFront = oreFront("chalcopyrite_front", Chalcopyrite, 0xcdaa7d);
        MonaziteFront = oreFront("monazite_front", Monazite, 0x838b83);
        EnrichedNaquadahFront = oreFront("enriched_naquadah_front", NaquadahEnriched, 0x58d00f);
        GrossularFront = oreFront("grossular_front", Grossular, 0xd2691e);
        NickelFront = oreFront("nickel_front", Nickel, 0xc1cdcd);
        AlmandineFront = oreFront("almandine_front", Almandine, 0xb22222);
        PlatinumFront = oreFront("platinum_front", Platinum, 0xcdc9a5);
        PentlanditeFront = oreFront("pentlandite_front", Pentlandite, 0xcdaa7d);
        SpessartineFront = oreFront("spessartine_front", Spessartine, 0xee5c42);
        SphaleriteFront = oreFront("sphalerite_front", Sphalerite, 0xeee9e9);

        // GTOCore drying by-products (GTO MaterialB:4023, :4028). GTO uses its own LIMPID icon
        // set, which GTCEu does not have; GTNA substitutes the regular FLUID icon set.
        RedMud = new Material.Builder(GTNACORE.id("red_mud"))
                .fluid()
                .color(0x972903).iconSet(FLUID)
                .buildAndRegister().setFormula("HCl?", false);

        NeutralisedRedMud = new Material.Builder(GTNACORE.id("neutralised_red_mud"))
                .fluid()
                .color(0x972903).iconSet(FLUID)
                .buildAndRegister().setFormula("Fe??", false);

        // GTOCore CarbonFiberPolyphenyleneSulfideComposite (GTO MaterialComposite:179): the
        // Component Assembler extension frame. Copied 1:1 apart from GTO's CURVED_PLATE and
        // COMPOSITE_MATERIAL flags, which GTNA does not implement.
        CarbonFiberPolyphenyleneSulfideComposite = new Material.Builder(
                GTNACORE.id("carbon_fiber_polyphenylene_sulfide_composite"))
                .ingot().fluid()
                .color(0x24221d).secondaryColor(0x171614)
                .blastTemp(4480, BlastProperty.GasTier.HIGH, GTValues.VA[GTValues.IV], 600)
                .iconSet(BRIGHT)
                .flags(GENERATE_PLATE, GENERATE_LONG_ROD, GENERATE_FRAME)
                .buildAndRegister();

        // GTOCore TitaniumNitrideCeramic (GTO MaterialB:4980). GTO's GENERATE_CERAMIC/MXene flags do
        // not exist in GTNA; GENERATE_BRICK is the flag that makes the flake items this casing needs.
        TitaniumNitrideCeramic = new Material.Builder(GTNACORE.id("titanium_nitride_ceramic"))
                .dust()
                .color(0xd4ac4b).secondaryColor(0x9c7a34)
                .iconSet(BRIGHT)
                .flags(GENERATE_BRICK)
                .buildAndRegister().setFormula("TiN");

        // GTOCore Tanmolyium (GTO MaterialA): the iridium casing's plate. Copied 1:1; the production
        // route is GTCEu's automatic component mixer/EBF chain.
        Tanmolyium = new Material.Builder(GTNACORE.id("tanmolyium"))
                .ingot().fluid()
                .blastTemp(4300, BlastProperty.GasTier.HIGH, GTValues.VA[GTValues.EV], 600)
                .components(Titanium, 5, Molybdenum, 5, Vanadium, 2, Chromium, 3, Aluminium, 1)
                .color(0x97249a)
                .iconSet(METALLIC)
                .flags(DISABLE_DECOMPOSITION, GENERATE_PLATE)
                .buildAndRegister();
    }

    /** One GTOCore {@code *Front} ore foam: a limpid fluid carrying one unit of its ore. */
    private static Material oreFront(String id, Material ore, int color) {
        return new Material.Builder(GTNACORE.id(id))
                .fluid()
                .components(ore, 1)
                .color(color)
                .flags(DISABLE_DECOMPOSITION)
                .iconSet(FLUID)
                .buildAndRegister();
    }
}
