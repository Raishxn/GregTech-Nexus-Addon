package com.raishxn.gtna.data.recipe;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.api.machine.multiblock.CleanroomType;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraftforge.fluids.FluidStack;

import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.common.data.GTNAMaterials;
import com.raishxn.gtna.common.data.GTNARecipeType;

import java.util.function.Consumer;

import static com.raishxn.gtna.common.data.material.GodforgeChainMaterials.*;

/**
 * Forge of Gods chain recipes, ported by stage from GT5-Unofficial (see
 * {@code docs/roadmap/forge-of-gods-material-chain.md}). Stage 2: the plasmas the catalysts need and the
 * Dimensionally Transcendent catalyst ladder.
 */
public final class GTNAGodforgeChainRecipes {

    /** GTNH 41 min 40 s for every catalyst step. */
    private static final int CATALYST_TICKS = 50_000;

    private GTNAGodforgeChainRecipes() {}

    public static void register(Consumer<FinishedRecipe> provider) {
        plasmas(provider);
        catalysts(provider);
        advancedFusion(provider);
        plasmaForge(provider);
        plasmaMixer(provider);
        phonon(provider);
        exoticRoutes(provider);
        nanoForge(provider);
        components(provider);
        batteries(provider);
        shiftedSuperfluid(provider);
        reachabilityFixes(provider);
    }

    /**
     * Gaps found by the reachability report (G-0199). GTNH 2.9 makes the Singularity Shielding Casing and the
     * Medial/Central Graviton Flow Modulators in the BEC Condensate Assembler (not ported): until it is, they use the
     * Assembly Line with the same items and the condensates as molten metals (Bedrockium / Cosmic Neutronium /
     * Netherite → Neutronium, Titansteel / Abyssal → Tritanium / Naquadria, Proto-Halkonite → Six-Phased Copper,
     * combined singularities → Gravi Stars, Field Restriction Coil T4 → T3, metamaterials and the Space-Time Continuum
     * Ripper left out, Boundless Cosmic Solder → Mutated Living Solder). Restraint Device follows GTO (Laser Cooling
     * Unit → UV emitters, Titan Steel → Tritanium, Quantanium → Quantum, High-Durability Compound Steel → Tritanium),
     * CF-PPS composite follows GTO's chemical bath (T600 fibre mesh → GTCEu carbon fibres) and the EOH matters are
     * solidified like any metal.
     */
    private static void reachabilityFixes(Consumer<FinishedRecipe> provider) {
        var shielding = com.raishxn.gtna.common.data.GTNAGodforgeContent.SINGULARITY_SHIELDING_CASING;
        var confinement = com.raishxn.gtna.common.data.GTNAGodforgeContent.MAGNETIC_CONFINEMENT_CASING;
        var coil = com.raishxn.gtna.common.data.GTNABlocks.FIELD_RESTRICTION_COIL_T3;
        var anomaly = com.raishxn.gtna.common.data.GTNAGodforgeComponents.GRAVITON_ANOMALY;
        var lens = com.raishxn.gtna.common.data.GTNAItems.LASER_LENS_SPECIAL;
        var superdense = com.raishxn.gtna.api.data.tag.GTNATagPrefix.superdensePlate;
        long uxv = GTValues.VA[GTValues.UXV];
        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(GTNACORE.id("godforge_chain/singularity_shielding_casing"))
                .inputItems(TagPrefix.frameGt, SixPhasedCopper, 4)
                .inputItems(TagPrefix.plateDense, GTMaterials.Neutronium, 64)
                .inputItems(TagPrefix.plate, Quantum, 16).inputItems(TagPrefix.frameGt, Infinity, 4)
                .inputItems(com.gregtechceu.gtceu.common.data.GTItems.GRAVI_STAR, 8)
                .inputItems(TagPrefix.rodLong, SuperconductorUIVBase, 16).inputItems(TagPrefix.plate, Creon, 16)
                .inputItems(TagPrefix.plate, Mellion, 16).inputItems(superdense, TranscendentMetal, 2)
                .inputItems(TagPrefix.frameGt, GTMaterials.Tritanium, 4)
                .inputItems(TagPrefix.plate, SixPhasedCopper, 16)
                .inputItems(TagPrefix.frameGt, GTMaterials.Naquadria, 4)
                .inputFluids(Hypogen.getFluid(4 * 144)).inputFluids(Infinity.getFluid(stacks(2)))
                .inputFluids(CelestialTungsten.getFluid(stacks(32)))
                .inputFluids(GTMaterials.Neutronium.getFluid(stacks(32)))
                .outputItems(shielding.asStack(6)).duration(6000).EUt(uxv)
                .stationResearch(b -> b.researchStack(confinement.asStack()).CWUt(512).EUt(uxv)).save(provider);
        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(GTNACORE.id("godforge_chain/medial_graviton_flow_modulator"))
                .inputItems(confinement.asStack()).inputItems(coil.asStack(2))
                .inputItems(TagPrefix.plateDense, Creon, 8)
                .inputItems(TagPrefix.gear, Mellion, 4).inputItems(TagPrefix.frameGt, SuperconductorUIVBase, 32)
                .inputItems(anomaly.asStack(4)).inputItems(lens.asStack(8))
                .inputItems(com.gregtechceu.gtceu.common.data.GTItems.EMITTER_UXV, 4)
                .inputItems(com.gregtechceu.gtceu.data.recipe.CustomTags.UXV_CIRCUITS, 8)
                .inputItems(com.raishxn.gtna.common.data.GTNANanoForge.SILVER_NANITES.asStack(2))
                .inputItems(com.raishxn.gtna.common.data.GTNANanoForge.GOLD_NANITES.asStack(2))
                .inputFluids(ChronomaticGlass.getFluid(64 * 144)).inputFluids(Infinity.getFluid(32 * 144))
                .inputFluids(TranscendentMetal.getFluid(32 * 144))
                .outputItems(com.raishxn.gtna.common.data.GTNAGodforgeContent.MEDIAL_GRAVITON_FLOW_MODULATOR.asStack())
                .duration(6000).EUt(uxv)
                .stationResearch(b -> b.researchStack(
                        com.raishxn.gtna.common.data.GTNAGodforgeContent.REMOTE_GRAVITON_FLOW_MODULATOR.asStack())
                        .CWUt(512).EUt(uxv))
                .save(provider);
        long opv = GTValues.VA[GTValues.OpV];
        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(GTNACORE.id("godforge_chain/central_graviton_flow_modulator"))
                .inputItems(confinement.asStack()).inputItems(coil.asStack(8)).inputItems(superdense, Creon, 8)
                .inputItems(TagPrefix.gear, Mellion, 64).inputItems(anomaly.asStack(8)).inputItems(lens.asStack(8))
                .inputItems(com.gregtechceu.gtceu.data.recipe.CustomTags.MAX_CIRCUITS, 4)
                .inputItems(com.gregtechceu.gtceu.common.data.GTItems.EMITTER_OpV, 4)
                .inputItems(com.raishxn.gtna.common.data.GTNANanoForge.SILVER_NANITES.asStack(8))
                .inputItems(com.raishxn.gtna.common.data.GTNANanoForge.GOLD_NANITES.asStack(8))
                .inputItems(com.raishxn.gtna.common.data.GTNANanoForge.SIX_PHASED_COPPER_NANITES.asStack(8))
                .inputItems(com.raishxn.gtna.common.data.GTNANanoForge.UNIVERSIUM_NANITES.asStack(8))
                .inputFluids(ChronomaticGlass.getFluid(256 * 144)).inputFluids(Infinity.getFluid(32 * 144))
                .inputFluids(MutatedLivingSolder.getFluid(20_000)).inputFluids(Eternity.getFluid(128 * 144))
                .outputItems(com.raishxn.gtna.common.data.GTNAGodforgeContent.CENTRAL_GRAVITON_FLOW_MODULATOR.asStack())
                .duration(6000).EUt(opv)
                .stationResearch(b -> b.researchStack(
                        com.raishxn.gtna.common.data.GTNAGodforgeContent.MEDIAL_GRAVITON_FLOW_MODULATOR.asStack())
                        .CWUt(1024).EUt(opv))
                .save(provider);

        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(GTNACORE.id("godforge_chain/restraint_device"))
                .inputItems(com.raishxn.gtna.common.data.GTNABlocks.HOLLOW_CASING.asStack(2))
                .inputItems(com.raishxn.gtna.common.data.GTNABlocks.FORCE_FIELD_GLASS.asStack(2))
                .inputItems(com.gregtechceu.gtceu.common.data.GTItems.EMITTER_UV, 6)
                .inputItems(TagPrefix.plateDense, GTMaterials.Tritanium, 4)
                .inputItems(TagPrefix.plate, GTMaterials.Plutonium241, 48).inputItems(TagPrefix.plateDense, Quantum, 4)
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(2304))
                .inputFluids(GTMaterials.Lubricant.getFluid(2000))
                .inputFluids(MutatedLivingSolder.getFluid(1000)).inputFluids(GTMaterials.Tritanium.getFluid(576))
                .outputItems(com.raishxn.gtna.common.data.GTNABlocks.RESTRAINT_DEVICE.asStack())
                .duration(1600).EUt(1_966_080)
                .stationResearch(b -> b.researchStack(
                        com.raishxn.gtna.common.data.GTNABlocks.CONTAINMENT_FIELD_GENERATOR.asStack()).CWUt(512)
                        .EUt(1_966_080))
                .save(provider);
        GTRecipeTypes.CHEMICAL_BATH_RECIPES.recipeBuilder(GTNACORE.id("godforge_chain/cf_pps_composite"))
                .inputItems(com.gregtechceu.gtceu.common.data.GTItems.CARBON_FIBERS, 10)
                .inputFluids(GTMaterials.PolyphenyleneSulfide.getFluid(576))
                .outputItems(TagPrefix.dust, GTNAMaterials.CarbonFiberPolyphenyleneSulfideComposite)
                .duration(200).EUt(8000).save(provider);
        // SpaceTime generates no shaping recipes (material recipes disabled): rod, bolt and screw for the Eternal coil.
        GTRecipeTypes.LATHE_RECIPES.recipeBuilder(GTNACORE.id("godforge_chain/space_time_rod"))
                .inputItems(TagPrefix.ingot, GTNAMaterials.SpaceTime)
                .outputItems(TagPrefix.rod, GTNAMaterials.SpaceTime, 2)
                .duration(200).EUt(GTValues.VA[GTValues.UHV]).save(provider);
        GTRecipeTypes.CUTTER_RECIPES.recipeBuilder(GTNACORE.id("godforge_chain/space_time_bolt"))
                .inputItems(TagPrefix.rod, GTNAMaterials.SpaceTime)
                .outputItems(TagPrefix.bolt, GTNAMaterials.SpaceTime, 4)
                .inputFluids(GTMaterials.Lubricant.getFluid(10)).duration(100).EUt(GTValues.VA[GTValues.UHV])
                .save(provider);
        GTRecipeTypes.LATHE_RECIPES.recipeBuilder(GTNACORE.id("godforge_chain/space_time_screw"))
                .inputItems(TagPrefix.bolt, GTNAMaterials.SpaceTime)
                .outputItems(TagPrefix.screw, GTNAMaterials.SpaceTime)
                .duration(100).EUt(GTValues.VA[GTValues.UHV]).save(provider);
        for (Material matter : new Material[] { GTNAMaterials.WhiteDwarfMatter, GTNAMaterials.BlackDwarfMatter,
                GTNAMaterials.Universium, GTNAMaterials.SpaceTime }) {
            GTRecipeTypes.FLUID_SOLIDFICATION_RECIPES
                    .recipeBuilder(GTNACORE.id("godforge_chain/solidify_" + matter.getName() + "_ingot"))
                    .notConsumable(com.gregtechceu.gtceu.common.data.GTItems.SHAPE_MOLD_INGOT.asStack())
                    .inputFluids(matter.getFluid(144)).outputItems(TagPrefix.ingot, matter)
                    .duration(200).EUt(GTValues.VA[GTValues.UHV]).save(provider);
            GTRecipeTypes.FLUID_SOLIDFICATION_RECIPES
                    .recipeBuilder(GTNACORE.id("godforge_chain/solidify_" + matter.getName() + "_block"))
                    .notConsumable(com.gregtechceu.gtceu.common.data.GTItems.SHAPE_MOLD_BLOCK.asStack())
                    .inputFluids(matter.getFluid(1296)).outputItems(TagPrefix.block, matter)
                    .duration(1800).EUt(GTValues.VA[GTValues.UHV]).save(provider);
        }
    }

    private static void battery(Consumer<FinishedRecipe> provider, String id,
                                net.minecraft.world.item.ItemStack research,
                                Material plate, net.minecraft.tags.TagKey<net.minecraft.world.item.Item> circuits,
                                net.minecraft.world.item.ItemStack previous,
                                com.tterrag.registrate.util.entry.ItemEntry<?> field, int soc, int crystal, int diodes,
                                int inductors, TagPrefix wire, Material conductor, FluidStack[] fluids,
                                com.tterrag.registrate.util.entry.ItemEntry<?> output, int duration, int tier) {
        var b = GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(GTNACORE.id("godforge_chain/" + id))
                .inputItems(TagPrefix.plateDense, plate, 16).inputItems(circuits, 4).inputItems(previous)
                .inputItems(field.asStack(4))
                .inputItems(com.gregtechceu.gtceu.common.data.GTItems.HIGHLY_ADVANCED_SOC, soc);
        if (crystal > 0) b.inputItems(com.gregtechceu.gtceu.common.data.GTItems.CRYSTAL_SYSTEM_ON_CHIP, crystal);
        b.inputItems(com.gregtechceu.gtceu.common.data.GTItems.ADVANCED_SMD_DIODE, diodes);
        if (inductors > 0) b.inputItems(com.gregtechceu.gtceu.common.data.GTItems.ADVANCED_SMD_INDUCTOR, inductors);
        b.inputItems(wire, conductor, 64);
        for (var fluid : fluids) b.inputFluids(fluid);
        long eut = GTValues.VA[tier];
        b.outputItems(output.asStack()).duration(duration).EUt(eut)
                .stationResearch(r -> r.researchStack(research).CWUt(256).EUt(eut)).save(provider);
    }

    /**
     * GTNH Really/Extremely/Insanely/Mega Ultimate Battery (ZPM3-ZPM6). Double plates (64) → dense plates (16),
     * Infinity Catalyst → Infinity, Quantium → Quantum (GT++), SuperCoolant / IC2 coolant → liquid helium, UHPIC/PPIC/
     * QPIC/FPIC wafers and SoC2 → Highly Advanced SoC, wetware/bioware chips → Crystal SoC, ASMD/XSMD → GTCEu
     * advanced SMDs, Superconductor UHV/UEV → Ruthenium Trinium Americium Neutronate / Superconductor UIV.
     */
    private static void batteries(Consumer<FinishedRecipe> provider) {
        var ultimate = com.gregtechceu.gtceu.common.data.GTItems.ULTIMATE_BATTERY.asStack(8);
        var zpm3 = com.raishxn.gtna.common.data.GTNAGodforgeComponents.REALLY_ULTIMATE_BATTERY;
        var zpm4 = com.raishxn.gtna.common.data.GTNAGodforgeComponents.EXTREMELY_ULTIMATE_BATTERY;
        var zpm5 = com.raishxn.gtna.common.data.GTNAGodforgeComponents.INSANELY_ULTIMATE_BATTERY;
        var zpm6 = com.raishxn.gtna.common.data.GTNAGodforgeComponents.MEGA_ULTIMATE_BATTERY;
        var helium = (java.util.function.IntFunction<FluidStack>) n -> GTMaterials.Helium
                .getFluid(FluidStorageKeys.LIQUID, n);
        battery(provider, "really_ultimate_battery",
                com.gregtechceu.gtceu.common.data.GTItems.ULTIMATE_BATTERY.asStack(),
                GTMaterials.Neutronium, com.gregtechceu.gtceu.data.recipe.CustomTags.UEV_CIRCUITS, ultimate,
                com.gregtechceu.gtceu.common.data.GTItems.FIELD_GENERATOR_UHV, 96, 0, 64, 0, TagPrefix.wireGtDouble,
                GTMaterials.RutheniumTriniumAmericiumNeutronate,
                new FluidStack[] { MutatedLivingSolder.getFluid(32 * 144), GTMaterials.Naquadria.getFluid(stacks(1)),
                        helium.apply(32_000) },
                zpm3, 4000, GTValues.UHV);
        battery(provider, "extremely_ultimate_battery", zpm3.asStack(), Infinity,
                com.gregtechceu.gtceu.data.recipe.CustomTags.UIV_CIRCUITS, zpm3.asStack(8),
                com.gregtechceu.gtceu.common.data.GTItems.FIELD_GENERATOR_UEV, 192, 0, 64, 0,
                TagPrefix.wireGtQuadruple, SuperconductorUIV,
                new FluidStack[] { MutatedLivingSolder.getFluid(stacks(1)), Quantum.getFluid(stacks(2)),
                        GTMaterials.Naquadria.getFluid(stacks(2)), helium.apply(64_000) },
                zpm4, 5000, GTValues.UEV);
        battery(provider, "insanely_ultimate_battery", zpm4.asStack(), Hypogen,
                com.gregtechceu.gtceu.data.recipe.CustomTags.UXV_CIRCUITS, zpm4.asStack(8),
                com.gregtechceu.gtceu.common.data.GTItems.FIELD_GENERATOR_UIV, 128, 64, 64, 32,
                TagPrefix.wireGtOctal, SuperconductorUIV,
                new FluidStack[] { MutatedLivingSolder.getFluid(stacks(2)), CelestialTungsten.getFluid(stacks(2)),
                        Quantum.getFluid(stacks(2)), helium.apply(128_000) },
                zpm5, 6000, GTValues.UIV);
        battery(provider, "mega_ultimate_battery", zpm5.asStack(), DragonMetal,
                com.gregtechceu.gtceu.data.recipe.CustomTags.OpV_CIRCUITS, zpm5.asStack(8),
                com.gregtechceu.gtceu.common.data.GTItems.FIELD_GENERATOR_UXV, 128, 64, 64, 64,
                TagPrefix.wireGtHex, SuperconductorUMV,
                new FluidStack[] { MutatedLivingSolder.getFluid(stacks(4)), AstralTitanium.getFluid(stacks(4)),
                        CelestialTungsten.getFluid(stacks(4)), helium.apply(256_000) },
                zpm6, 7200, GTValues.UXV);
    }

    /**
     * GTNH PlasmaForgeRecipes, Dimensionally Shifted Superfluid tiers 2-5. Stable Baryonic Matter → Raw Star Matter,
     * Super-Heavy / Heavy Radox → Naquadria, Grade 8 purified water → distilled water (×10).
     */
    private static void shiftedSuperfluid(Consumer<FinishedRecipe> provider) {
        int[] baryonic = { 250, 1_000, 2_000, 8_000 };
        int[] oganesson = { 144, 144, 288, 576 };
        int[] water = { 400, 1_600, 3_200, 12_800 };
        int[] celestial = { 24 * 144, 24 * 144, 48 * 144, stacks(1) + 32 * 144 };
        int[] radox = { 2_000, 2_000, 4_000, 32_000 };
        Material[] catalyst = { ExcitedDTPC, ExcitedDTRC, ExcitedDTEC, ExcitedDTSC };
        int[] dss = { 7_500, 30_000, 90_000, 360_000 };
        int[] dtr = { 250, 1_000, 2_000, 4_000 };
        int[] duration = { 600, 600, 600, 150 };
        int[] tier = { GTValues.UIV, GTValues.UXV, GTValues.UXV, GTValues.OpV };
        int[] heat = { 10_800, 12_600, 13_500, 13_500 };
        for (int t = 0; t < 4; t++) {
            GTNARecipeType.PLASMA_FORGE_RECIPES
                    .recipeBuilder(GTNACORE.id("godforge_chain/dimensionally_shifted_superfluid_t" + (t + 2)))
                    .inputFluids(GTNAMaterials.RawStarMatter.getFluid(baryonic[t]))
                    .inputFluids(MetastableOganesson.getFluid(oganesson[t]))
                    .inputFluids(GTMaterials.DistilledWater.getFluid(water[t] * 10))
                    .inputFluids(plasma(CelestialTungsten, celestial[t]))
                    .inputFluids(GTMaterials.Naquadria.getFluid(radox[t]))
                    .inputFluids(catalyst[t].getFluid(t == 0 ? 1_000 : 2_000))
                    .outputFluids(DimensionallyShiftedSuperfluid.getFluid(dss[t]))
                    .outputFluids(GTNAMaterials.DimensionallyTranscendentResidue.getFluid(dtr[t]))
                    .duration(duration[t]).EUt(GTValues.VA[tier[t]]).blastFurnaceTemp(heat[t]).save(provider);
        }
    }

    /**
     * Stage 8: GTNH Forge of Gods components (GT5U Assembler/Vacuum/Macerator/EBF, GT++ RecipesGregTech and
     * RecipeLoaderChemicalSkips, GoodGenerator RecipeLoader). Substitutes: Netherite → Neutronium, Naquarite foil →
     * Naquadria plates, Quantum Glass → Fusion Glass, Thulium → Europium, Neptunium/Fermium plasma → Americium/
     * Plutonium-241 plasma, Abyssal/Octiron/Titansteel/Trinium-Reinforced Steel → Naquadria/Osmiridium/Tritanium/
     * Trinium, Oganesson → Xenon, wafers → Highly Advanced SoC,
     * advanced radiation protection plates → dense Naquadah Alloy plates, Exotic Containment Unit → Quantum Star.
     * Raw Tesseract (NH Core), Stable Boson Containment Unit (Beam Crafter) and Superconductor Composite (QFT
     * byproduct) get GTNA recipes.
     */
    private static void components(Consumer<FinishedRecipe> provider) {
        long uhv = GTValues.VA[GTValues.UHV], uev = GTValues.VA[GTValues.UEV], uiv = GTValues.VA[GTValues.UIV];
        var tesseract = com.raishxn.gtna.common.data.GTNAGodforgeComponents.TESSERACT;
        var boson = com.raishxn.gtna.common.data.GTNAGodforgeComponents.STABLE_BOSON_CONTAINMENT_UNIT;
        var composite = com.raishxn.gtna.common.data.GTNAGodforgeComponents.SUPERCONDUCTOR_COMPOSITE;
        var thermal = com.raishxn.gtna.common.data.GTNAGodforgeComponents.THERMAL_SUPERCONDUCTOR;
        var glass = com.raishxn.gtna.common.data.GTNABlocks.FORCE_FIELD_GLASS;
        var special = com.raishxn.gtna.common.data.GTNAItems.LASER_LENS_SPECIAL;

        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(GTNACORE.id("godforge_chain/tesseract"))
                .inputItems(TagPrefix.frameGt, Infinity)
                .inputItems(com.gregtechceu.gtceu.common.data.GTItems.QUANTUM_STAR, 4)
                .inputItems(com.gregtechceu.gtceu.common.data.GTItems.FIELD_GENERATOR_UEV, 2)
                .inputItems(TagPrefix.plateDense, GTMaterials.Neutronium, 4)
                .inputFluids(CelestialTungsten.getFluid(576)).inputFluids(Mellion.getFluid(1152))
                .outputItems(tesseract.asStack()).duration(1200).EUt(uiv)
                .stationResearch(b -> b.researchStack(com.gregtechceu.gtceu.common.data.GTItems.QUANTUM_STAR.asStack())
                        .CWUt(256).EUt(uiv))
                .save(provider);
        // GT5U centrifuge: SpaceTime split into Space and Time with the Tesseract and the Special Lens.
        GTRecipeTypes.CENTRIFUGE_RECIPES.recipeBuilder(GTNACORE.id("godforge_chain/space_time_split"))
                .notConsumable(tesseract.asStack()).notConsumable(special.asStack())
                .inputFluids(GTNAMaterials.SpaceTime.getFluid(20 * 144))
                .outputFluids(GTNAMaterials.Space.getFluid(10 * 144))
                .outputFluids(GTNAMaterials.Time.getFluid(10 * 144))
                .duration(200).EUt(GTValues.VA[GTValues.UXV]).save(provider);
        GTRecipeTypes.MACERATOR_RECIPES.recipeBuilder(GTNACORE.id("godforge_chain/transcendent_metal_dust"))
                .inputItems(tesseract.asStack()).outputItems(TagPrefix.dust, TranscendentMetal, 8)
                .duration(100).EUt(uiv).save(provider);
        GTRecipeTypes.BLAST_RECIPES.recipeBuilder(GTNACORE.id("godforge_chain/transcendent_metal"))
                .inputItems(TagPrefix.dust, TranscendentMetal).circuitMeta(1)
                .inputFluids(GTMaterials.Tungsten.getFluid(144))
                .outputItems(TagPrefix.ingot, TranscendentMetal).outputFluids(CelestialTungsten.getFluid(72))
                .duration(3600).EUt(uiv).blastFurnaceTemp(11_701).save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GTNACORE.id("godforge_chain/stable_boson_containment_unit"))
                .inputItems(com.gregtechceu.gtceu.common.data.GTItems.GRAVI_STAR)
                .inputItems(com.gregtechceu.gtceu.common.data.GTItems.FIELD_GENERATOR_UHV)
                .inputItems(TagPrefix.plate, GTMaterials.Neutronium, 4).inputFluids(GTMaterials.Krypton.getFluid(100))
                .outputItems(boson.asStack()).duration(200).EUt(uhv).save(provider);
        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GTNACORE.id("godforge_chain/superconductor_composite"))
                .inputItems(TagPrefix.dust, GTMaterials.Samarium, 8)
                .inputItems(TagPrefix.dust, GTMaterials.Neodymium, 8)
                .inputItems(TagPrefix.wireFine, GTMaterials.RutheniumTriniumAmericiumNeutronate, 16)
                .inputFluids(GTMaterials.Cerium.getFluid(576))
                .outputItems(composite.asStack()).duration(400).EUt(uhv).save(provider);
        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GTNACORE.id("godforge_chain/electromagnet_tengam"))
                .inputItems(TagPrefix.rodLong, TengamAttuned, 4).inputItems(TagPrefix.frameGt, StellarAlloy)
                .inputItems(TagPrefix.wireFine, SuperconductorUIV, 32)
                .outputItems(com.raishxn.gtna.common.data.GTNAGodforgeComponents.ELECTROMAGNET_TENGAM.asStack())
                .duration(400).EUt(uev).save(provider);
        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GTNACORE.id("godforge_chain/neutronium_heat_capacitor"))
                .inputItems(TagPrefix.plateDense, GTMaterials.Neutronium, 8)
                .inputItems(TagPrefix.rodLong, GTMaterials.Neutronium, 4)
                .outputItems(com.raishxn.gtna.common.data.GTNAGodforgeComponents.NEUTRONIUM_HEAT_CAPACITOR.asStack())
                .duration(100).EUt(GTValues.VA[GTValues.ZPM]).save(provider);
        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GTNACORE.id("godforge_chain/space_coolant_cell"))
                .inputItems(TagPrefix.plate, GTMaterials.Tritanium, 6)
                .inputItems(TagPrefix.plate, GTMaterials.Neutronium, 2)
                .inputFluids(GTMaterials.Helium.getFluid(FluidStorageKeys.LIQUID, 1000))
                .outputItems(com.raishxn.gtna.common.data.GTNAGodforgeComponents.SPACE_COOLANT_CELL.asStack())
                .duration(400).EUt(uhv).save(provider);
        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GTNACORE.id("godforge_chain/thermal_superconductor"))
                .inputItems(TagPrefix.frameGt, TranscendentMetal).inputItems(TagPrefix.foil, SuperconductorUIVBase, 64)
                .inputItems(TagPrefix.plate, GTMaterials.Neutronium, 32)
                .inputItems(com.gregtechceu.gtceu.common.data.GTItems.ELECTRIC_PUMP_UIV)
                .inputFluids(PhononMedium.getFluid(100))
                .outputItems(thermal.asStack()).duration(40).EUt(uiv).save(provider);
        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GTNACORE.id("godforge_chain/relativistic_heat_capacitor"))
                .inputItems(thermal.asStack(2)).inputItems(TagPrefix.plate, GTMaterials.Naquadria, 16)
                .inputItems(TagPrefix.plate, SuperconductorUIVBase, 8).inputItems(TagPrefix.gear, SixPhasedCopper, 3)
                .inputItems(com.gregtechceu.gtceu.common.data.GTItems.FIELD_GENERATOR_UEV)
                .inputFluids(PhononMedium.getFluid(500))
                .outputItems(com.raishxn.gtna.common.data.GTNAGodforgeComponents.RELATIVISTIC_HEAT_CAPACITOR.asStack())
                .duration(100).EUt(uiv).save(provider);
        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GTNACORE.id("godforge_chain/force_field_glass"))
                .inputItems(com.gregtechceu.gtceu.common.data.GTBlocks.FUSION_GLASS.asStack())
                .inputItems(com.gregtechceu.gtceu.common.data.GTItems.FIELD_GENERATOR_ZPM)
                .inputItems(TagPrefix.rodLong, CelestialTungsten, 4)
                .inputItems(TagPrefix.rodLong, GTMaterials.Neutronium, 4)
                .inputItems(TagPrefix.plate, ChronomaticGlass, 6).inputFluids(Quantum.getFluid(864))
                .outputItems(glass.asStack()).duration(200).EUt(uev).save(provider);
        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(GTNACORE.id("godforge_chain/cosmic_fabric_manipulator"))
                .inputItems(glass.asStack(4))
                .inputItems(com.raishxn.gtna.common.data.GTNANanoForge.CARBON_NANITES.asStack(16))
                .inputItems(com.gregtechceu.gtceu.common.data.GTItems.EMITTER_UIV, 4)
                .inputItems(TagPrefix.wireGtHex, SuperconductorUIV, 8).inputItems(special.asStack())
                .inputItems(TagPrefix.plateDense, GTMaterials.NaquadahAlloy, 8).inputItems(composite.asStack(4))
                .inputFluids(GTMaterials.Europium.getFluid(15 * 144)).inputFluids(ExcitedDTRC.getFluid(5_000))
                .inputFluids(plasma(GTMaterials.Americium, 10_000))
                .inputFluids(plasma(GTMaterials.Plutonium241, 10_000))
                .outputItems(com.raishxn.gtna.common.data.GTNABlocks.COSMIC_FABRIC_MANIPULATOR.asStack())
                .duration(1500).EUt(uiv)
                .stationResearch(b -> b.researchStack(glass.asStack()).CWUt(256).EUt(GTValues.VA[GTValues.ZPM]))
                .save(provider);
        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(GTNACORE.id("godforge_chain/graviton_anomaly"))
                .inputItems(com.gregtechceu.gtceu.common.data.GTItems.QUANTUM_STAR).inputItems(boson.asStack(2))
                .inputItems(TagPrefix.plate, GTMaterials.Naquadria, 16)
                .inputItems(com.gregtechceu.gtceu.data.recipe.CustomTags.UHV_CIRCUITS, 8)
                .inputItems(TagPrefix.wireGtHex, GTMaterials.RutheniumTriniumAmericiumNeutronate, 32)
                .inputItems(TagPrefix.bolt, AstralTitanium, 8).inputItems(TagPrefix.screw, GTMaterials.Tritanium, 8)
                .inputFluids(GTMaterials.Naquadria.getFluid(16 * 144))
                .inputFluids(GTMaterials.Trinium.getFluid(32 * 144))
                .inputFluids(GTMaterials.Osmiridium.getFluid(16 * 144))
                .inputFluids(GTMaterials.Tritanium.getFluid(16 * 144))
                .outputItems(com.raishxn.gtna.common.data.GTNAGodforgeComponents.GRAVITON_ANOMALY.asStack())
                .duration(2400).EUt(uhv)
                .scannerResearch(b -> b.researchStack(boson.asStack()).duration(1200).EUt(uev))
                .save(provider);
        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(GTNACORE.id("godforge_chain/field_restriction_coil_t3"))
                .inputItems(TagPrefix.frameGt, Infinity)
                .inputItems(com.gregtechceu.gtceu.common.data.GTItems.FIELD_GENERATOR_UEV, 2)
                .inputItems(com.gregtechceu.gtceu.common.data.GTItems.ELECTRIC_PUMP_UEV, 8)
                .inputItems(TagPrefix.wireGtOctal, GTMaterials.RutheniumTriniumAmericiumNeutronate, 64)
                .inputItems(TagPrefix.plateDense, TranscendentMetal, 8).inputItems(TagPrefix.rodLong, Infinity, 16)
                .inputItems(com.gregtechceu.gtceu.common.data.GTItems.HIGHLY_ADVANCED_SOC, 32)
                .inputItems(com.gregtechceu.gtceu.data.recipe.CustomTags.UIV_CIRCUITS, 1)
                .inputFluids(GTMaterials.Xenon.getFluid(1000)).inputFluids(GTMaterials.Neutronium.getFluid(stacks(1)))
                .inputFluids(DimensionallyShiftedSuperfluid.getFluid(64_000))
                .outputItems(com.raishxn.gtna.common.data.GTNABlocks.FIELD_RESTRICTION_COIL_T3.asStack())
                .duration(1200).EUt(uhv)
                .scannerResearch(b -> b.researchStack(com.gregtechceu.gtceu.common.data.GTItems.FIELD_GENERATOR_UEV
                        .asStack()).duration(1200).EUt(uhv))
                .save(provider);
    }

    private static com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder nano(String id, int tier, long eut,
                                                                                  int duration) {
        return GTNARecipeType.NANO_FORGE_RECIPES.recipeBuilder(GTNACORE.id("godforge_chain/nano_" + id))
                .addData(com.raishxn.gtna.common.machine.multiblock.electric.NanoForgeMachine.TIER, tier)
                .duration(duration).EUt(eut);
    }

    /**
     * Stage 7: GTNH NaniteChain (tiers 1-3). Lenses: Mysterious Crystal (UV) → Nether Star, Chromatic (UHV) →
     * Orundum, Radox Polymer (UEV) → Magneto Resonatic, Energised Tesseract (UIV) → Special Laser Lens, Dilithium
     * (UMV) as in GTNH, Forcicium → Special Laser Lens. SoC/SoC2/APIC → GTCEu SoC/Advanced SoC/Highly Advanced SoC,
     * Wetware/Bioware chips → Crystal SoC, Timepiece → Gravi Star, Double Compressed Glowstone → glowstone blocks,
     * Stem Cells → GTCEu Stem Cells, BartWorks advanced block casings → Naquadah Alloy frames, superdense Naquadah
     * Alloy plates → dense plates.
     */
    private static void nanoForge(Consumer<FinishedRecipe> provider) {
        var soc = com.gregtechceu.gtceu.common.data.GTItems.SYSTEM_ON_CHIP;
        var soc2 = com.gregtechceu.gtceu.common.data.GTItems.ADVANCED_SYSTEM_ON_CHIP;
        var apic = com.gregtechceu.gtceu.common.data.GTItems.HIGHLY_ADVANCED_SOC;
        var crystal = com.gregtechceu.gtceu.common.data.GTItems.CRYSTAL_SYSTEM_ON_CHIP;
        var special = com.raishxn.gtna.common.data.GTNAItems.LASER_LENS_SPECIAL.asStack();
        var uu = GTMaterials.UUMatter;
        var lensUV = com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper.get(TagPrefix.lens, GTMaterials.NetherStar);
        var lensUHV = com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper.get(TagPrefix.lens, Orundum);
        var lensUEV = com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper.get(TagPrefix.lens, MagnetoResonatic);
        var lensUMV = com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper.get(TagPrefix.lens, Dilithium);
        nano("carbon", 1, 10_000_000, 10_000).notConsumable(lensUV)
                .inputItems(TagPrefix.frameGt, GTMaterials.NaquadahAlloy, 8).inputItems(soc, 64)
                .inputFluids(uu.getFluid(200_000))
                .outputItems(com.raishxn.gtna.common.data.GTNANanoForge.CARBON_NANITES.asStack(64)).save(provider);
        nano("silver", 2, 10_000_000, 15_000).notConsumable(lensUEV)
                .inputItems(TagPrefix.block, GTMaterials.Silver, 8).inputItems(soc, 16)
                .inputFluids(uu.getFluid(200_000))
                .outputItems(com.raishxn.gtna.common.data.GTNANanoForge.SILVER_NANITES.asStack()).save(provider);
        nano("neutronium", 1, 100_000_000, 2_000).notConsumable(lensUHV)
                .inputItems(TagPrefix.block, GTMaterials.Neutronium, 8).inputItems(soc2, 64).inputItems(soc2, 32)
                .inputFluids(uu.getFluid(200_000))
                .outputItems(com.raishxn.gtna.common.data.GTNANanoForge.NEUTRONIUM_NANITES.asStack()).save(provider);
        nano("glowstone", 2, 50_000_000, 4_000).notConsumable(lensUEV)
                .inputItems(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.GLOWSTONE, 64))
                .inputItems(soc2, 64).inputFluids(uu.getFluid(50_000))
                .outputItems(com.raishxn.gtna.common.data.GTNANanoForge.GLOWSTONE_NANITES.asStack(64)).save(provider);
        nano("gold", 3, 100_000_000, 20_000).notConsumable(lensUMV)
                .inputItems(TagPrefix.block, GTMaterials.Gold, 8).inputItems(soc, 16)
                .inputFluids(uu.getFluid(300_000))
                .outputItems(com.raishxn.gtna.common.data.GTNANanoForge.GOLD_NANITES.asStack()).save(provider);
        nano("transcendent_metal", 2, 1_000_000_000, 15_000).notConsumable(special)
                .inputItems(TagPrefix.block, TranscendentMetal, 8).inputItems(soc2, 64).inputItems(soc2, 64)
                .inputItems(soc2, 64).inputFluids(uu.getFluid(2_000_000))
                .outputItems(com.raishxn.gtna.common.data.GTNANanoForge.TRANSCENDENT_METAL_NANITES.asStack())
                .save(provider);
        nano("six_phased_copper", 3, 2_000_000_000, 2_000).notConsumable(special)
                .inputItems(TagPrefix.block, SixPhasedCopper, 16).inputItems(soc2, 64).inputItems(soc2, 64)
                .inputItems(soc2, 64).inputFluids(uu.getFluid(500_000))
                .inputFluids(GTNAMaterials.DimensionallyTranscendentResidue.getFluid(50_000))
                .inputFluids(Creon.getFluid(stacks(8)))
                .outputItems(com.raishxn.gtna.common.data.GTNANanoForge.SIX_PHASED_COPPER_NANITES.asStack(8))
                .save(provider);
        nano("white_dwarf_matter", 3, 2_000_000_000, 15_000).notConsumable(lensUEV).notConsumable(special)
                .inputItems(TagPrefix.block, GTNAMaterials.WhiteDwarfMatter, 8).inputItems(apic, 64)
                .inputItems(crystal, 64).inputFluids(uu.getFluid(500_000))
                .inputFluids(GTNAMaterials.RawStarMatter.getFluid(50_000))
                .inputFluids(GTNAMaterials.Space.getFluid(5 * 144))
                .outputItems(com.raishxn.gtna.common.data.GTNANanoForge.WHITE_DWARF_MATTER_NANITES.asStack(4))
                .save(provider);
        nano("black_dwarf_matter", 3, 2_000_000_000, 15_000).notConsumable(lensUEV).notConsumable(special)
                .inputItems(TagPrefix.block, GTNAMaterials.BlackDwarfMatter, 8).inputItems(apic, 64)
                .inputItems(crystal, 64).inputFluids(uu.getFluid(500_000))
                .inputFluids(GTNAMaterials.RawStarMatter.getFluid(50_000))
                .inputFluids(GTNAMaterials.Time.getFluid(5 * 144))
                .outputItems(com.raishxn.gtna.common.data.GTNANanoForge.BLACK_DWARF_MATTER_NANITES.asStack(4))
                .save(provider);
        nano("universium", 3, 2_000_000_000, 15_000).notConsumable(lensUEV).notConsumable(special)
                .inputItems(TagPrefix.block, GTNAMaterials.Universium, 8).inputItems(apic, 64)
                .inputItems(crystal, 64).inputFluids(GTNAMaterials.SpaceTime.getFluid(144))
                .inputFluids(Infinity.getFluid(4 * 144)).inputFluids(PrimordialMatter.getFluid(64_000))
                .outputItems(com.raishxn.gtna.common.data.GTNANanoForge.UNIVERSIUM_NANITES.asStack(2))
                .save(provider);
        nano("eternity", 3, GTValues.V[GTValues.MAX], 3_750).notConsumable(special)
                .inputItems(com.raishxn.gtna.common.data.GTNANanoForge.UNIVERSIUM_NANITES.asStack(2))
                .inputItems(TagPrefix.block, Eternity, 8).inputItems(apic, 64)
                .inputItems(com.gregtechceu.gtceu.common.data.GTItems.GRAVI_STAR)
                .inputFluids(GTNAMaterials.Space.getFluid(2 * 144)).inputFluids(ExcitedDTSC.getFluid(12_500))
                .inputFluids(PrimordialMatter.getFluid(64_000))
                .outputItems(com.raishxn.gtna.common.data.GTNANanoForge.ETERNITY_NANITES.asStack()).save(provider);

        // Stellar Alloy (Nano Forge frames): GTNA composition, GT5U has no alloy recipe to follow here.
        com.gregtechceu.gtceu.common.data.GCYMRecipeTypes.ALLOY_BLAST_RECIPES
                .recipeBuilder(GTNACORE.id("godforge_chain/stellar_alloy"))
                .inputItems(TagPrefix.dust, GTMaterials.Neutronium, 4)
                .inputItems(TagPrefix.dust, GTMaterials.Naquadria, 2)
                .inputItems(TagPrefix.dust, GTMaterials.Trinium, 2).circuitMeta(8)
                .outputFluids(StellarAlloy.getFluid(8 * 144))
                .duration(1200).EUt(GTValues.VA[GTValues.UV]).blastFurnaceTemp(10_800).save(provider);

        long zpm = GTValues.VA[GTValues.ZPM];
        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(GTNACORE.id("godforge_chain/nano_forge"))
                .inputItems(com.gregtechceu.gtceu.common.data.GTMachines.HULL[GTValues.UV].asStack(16))
                .inputItems(com.raishxn.gtna.common.data.GTNANanoForge.CARBON_NANITES.asStack(16))
                .inputItems(com.gregtechceu.gtceu.common.data.GTItems.FIELD_GENERATOR_ZPM, 16)
                .inputItems(com.gregtechceu.gtceu.common.data.GTItems.CONVEYOR_MODULE_UV, 16)
                .inputItems(com.gregtechceu.gtceu.common.data.GTItems.ELECTRIC_MOTOR_UV, 32)
                .inputItems(com.gregtechceu.gtceu.data.recipe.CustomTags.LuV_CIRCUITS, 16)
                .inputItems(TagPrefix.wireGtOctal, GTMaterials.Naquadah, 32)
                .inputItems(TagPrefix.plateDense, GTMaterials.NaquadahAlloy, 16)
                .inputFluids(GTNAMaterials.Indalloy140.getFluid(32 * 144))
                .inputFluids(GTMaterials.HSSS.getFluid(32 * 144))
                .inputFluids(GTMaterials.Osmiridium.getFluid(16 * 144))
                .outputItems(com.raishxn.gtna.common.data.GTNANanoForge.NANO_FORGE.asStack())
                .duration(6000).EUt(zpm)
                .scannerResearch(b -> b.researchStack(
                        com.raishxn.gtna.common.data.GTNANanoForge.CARBON_NANITES.asStack()).duration(3000).EUt(zpm))
                .save(provider);
        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(GTNACORE.id("godforge_chain/carbon_nanites"))
                .inputItems(com.gregtechceu.gtceu.data.recipe.CustomTags.UV_CIRCUITS, 16)
                .inputItems(com.gregtechceu.gtceu.common.data.GTItems.ROBOT_ARM_UV, 16)
                .inputItems(com.gregtechceu.gtceu.common.data.GTItems.STEM_CELLS, 32)
                .inputItems(TagPrefix.ring, GTMaterials.NaquadahAlloy, 32)
                .inputItems(TagPrefix.rod, GTMaterials.NaquadahAlloy, 16)
                .inputItems(TagPrefix.dust, GTMaterials.Carbon, 64)
                .inputFluids(uu.getFluid(10_000)).inputFluids(GTNAMaterials.Indalloy140.getFluid(32 * 144))
                .outputItems(com.raishxn.gtna.common.data.GTNANanoForge.CARBON_NANITES.asStack(2))
                .duration(1000).EUt(GTValues.VA[GTValues.UV])
                .scannerResearch(b -> b.researchStack(
                        com.gregtechceu.gtceu.common.data.GTItems.CRYSTAL_MAINFRAME_UV.asStack()).duration(2400)
                        .EUt(zpm))
                .save(provider);
    }

    /**
     * Stage 7 (routes only): GT:IA has no Avaritia, Thaumcraft, Draconic Evolution or GT++ Chemical Plant, so
     * Infinity, Quantum, Dragonblood, Ichorium, Mutated Living Solder and Eternity get GTNA routes through machines
     * that exist. Eternity runs in the DTPF until the Nano Forge (GTNH tier 3 nanites) is ported.
     */
    private static void exoticRoutes(Consumer<FinishedRecipe> provider) {
        long uev = GTValues.VA[GTValues.UEV], uiv = GTValues.VA[GTValues.UIV], uxv = GTValues.VA[GTValues.UXV];
        GTNARecipeType.PLASMA_FORGE_RECIPES.recipeBuilder(GTNACORE.id("godforge_chain/infinity"))
                .inputItems(com.gregtechceu.gtceu.common.data.GTItems.GRAVI_STAR, 4)
                .inputFluids(GTMaterials.Neutronium.getFluid(4608)).inputFluids(ExcitedDTRC.getFluid(1000))
                .outputFluids(Infinity.getFluid(1152))
                .outputFluids(GTNAMaterials.DimensionallyTranscendentResidue.getFluid(250))
                .duration(1200).EUt(uiv).blastFurnaceTemp(12_600).save(provider);
        // GTNH Quantum: a mixer alloy centrifuged out of Churitsu, which also leaves Shijima.
        GTRecipeTypes.MIXER_RECIPES.recipeBuilder(GTNACORE.id("godforge_chain/churitsu"))
                .inputItems(TagPrefix.dust, GTMaterials.Trinium, 2).inputItems(TagPrefix.dust, GTMaterials.Naquadria, 2)
                .inputItems(TagPrefix.dust, GTMaterials.Ruthenium, 2).inputItems(TagPrefix.dust, GTMaterials.Americium)
                .inputFluids(GTMaterials.Radon.getFluid(1000))
                .outputItems(TagPrefix.dust, Churitsu, 7).duration(600).EUt(uev).save(provider);
        GTRecipeTypes.CENTRIFUGE_RECIPES.recipeBuilder(GTNACORE.id("godforge_chain/quantum"))
                .inputItems(TagPrefix.dust, Churitsu, 7)
                .outputItems(TagPrefix.dust, Quantum, 4).outputItems(TagPrefix.dust, Shijima, 3)
                .duration(900).EUt(uev).save(provider);
        melt(provider, "quantum", Quantum, 11_000, GTValues.UEV);
        melt(provider, "shijima", Shijima, 10_800, GTValues.UEV);
        GTRecipeTypes.MIXER_RECIPES.recipeBuilder(GTNACORE.id("godforge_chain/dragonblood"))
                .inputItems(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DRAGON_BREATH, 16))
                .inputItems(TagPrefix.dust, GTMaterials.Naquadria, 4)
                .inputItems(TagPrefix.dust, GTMaterials.Neutronium)
                .inputFluids(GTMaterials.Blaze.getFluid(1152))
                .outputFluids(DragonMetal.getFluid(576)).duration(800).EUt(uev).save(provider);
        com.gregtechceu.gtceu.common.data.GCYMRecipeTypes.ALLOY_BLAST_RECIPES
                .recipeBuilder(GTNACORE.id("godforge_chain/ichorium"))
                .inputItems(TagPrefix.dust, Infinity).inputItems(TagPrefix.dust, GTMaterials.Gold, 4)
                .inputItems(TagPrefix.dust, GTMaterials.Glowstone, 8)
                .inputItems(TagPrefix.dust, GTMaterials.Naquadria, 2)
                .outputFluids(Ichorium.getFluid(1152)).duration(900).EUt(uev).blastFurnaceTemp(12_000)
                .save(provider);
        GTRecipeTypes.MIXER_RECIPES.recipeBuilder(GTNACORE.id("godforge_chain/mutated_living_solder"))
                .inputItems(TagPrefix.dust, GTMaterials.Bismuth, 2).inputItems(TagPrefix.dust, GTMaterials.Tin, 2)
                .inputItems(TagPrefix.dust, Infinity).inputItems(com.gregtechceu.gtceu.common.data.GTItems.GRAVI_STAR)
                .inputFluids(GTNAMaterials.Indalloy140.getFluid(1440))
                .outputFluids(MutatedLivingSolder.getFluid(1440)).duration(600).EUt(uiv).save(provider);
        GTNARecipeType.PLASMA_FORGE_RECIPES.recipeBuilder(GTNACORE.id("godforge_chain/eternity"))
                .inputFluids(Infinity.getFluid(1152)).inputFluids(Shirabon.getFluid(1152))
                .inputFluids(PrimordialMatter.getFluid(1000)).inputFluids(GTNAMaterials.Universium.getFluid(144))
                .outputFluids(Eternity.getFluid(1152))
                .duration(2400).EUt(uxv).blastFurnaceTemp(13_500).save(provider);
    }

    /**
     * Stage 6: Tengam, Dilithium, Magneto Resonatic, the UIV/UMV superconductors, Phonon (Seed Crystal, Crystal
     * Solution, Medium) and Shirabon. Adaptations: Raw Tengam has no ore yet and comes from a rare-earth mix; grade 8
     * purified water → DTR; GTNH's 17,000 K and 13,600 K blasts are capped at the Eternal coil's 13,500 K;
     * superconductor
     * bases are GTNA compositions guided by GTNH formulas; Shirabon's DTPF recipe is a GTNA adaptation.
     */
    private static void phonon(Consumer<FinishedRecipe> provider) {
        long uev = GTValues.VA[GTValues.UEV], uiv = GTValues.VA[GTValues.UIV], uxv = GTValues.VA[GTValues.UXV];
        GTRecipeTypes.MIXER_RECIPES.recipeBuilder(GTNACORE.id("godforge_chain/tengam_raw"))
                .inputItems(TagPrefix.dust, GTMaterials.Neodymium, 2)
                .inputItems(TagPrefix.dust, GTMaterials.Samarium, 2)
                .inputItems(TagPrefix.dust, GTMaterials.Europium).inputItems(TagPrefix.dust, GTMaterials.Lanthanum)
                .outputItems(TagPrefix.dust, TengamRaw, 4).duration(400).EUt(uev).save(provider);
        GTRecipeTypes.ELECTROMAGNETIC_SEPARATOR_RECIPES.recipeBuilder(GTNACORE.id("godforge_chain/tengam_purified"))
                .inputItems(TagPrefix.dust, TengamRaw).outputItems(TagPrefix.dust, TengamPurified)
                .chancedOutput(TagPrefix.dust, GTMaterials.Neodymium, 2500, 0)
                .duration(200).EUt(uev).save(provider);
        melt(provider, "tengam_purified", TengamPurified, 10_800, GTValues.UEV);
        GTRecipeTypes.POLARIZER_RECIPES.recipeBuilder(GTNACORE.id("godforge_chain/tengam_attuned"))
                .inputItems(TagPrefix.ingot, TengamPurified).outputItems(TagPrefix.ingot, TengamAttuned)
                .duration(300).EUt(uev).save(provider);

        GTRecipeTypes.AUTOCLAVE_RECIPES.recipeBuilder(GTNACORE.id("godforge_chain/dilithium"))
                .inputItems(TagPrefix.dust, GTMaterials.Lithium, 4).inputItems(TagPrefix.gem, GTMaterials.Diamond)
                .inputFluids(GTMaterials.Helium.getFluid(1000)).outputItems(TagPrefix.gem, Dilithium, 2)
                .duration(600).EUt(GTValues.VA[GTValues.UV]).save(provider);
        GTRecipeTypes.MIXER_RECIPES.recipeBuilder(GTNACORE.id("godforge_chain/magneto_resonatic_dust"))
                .inputItems(TagPrefix.dust, GTMaterials.Amethyst, 3).inputItems(TagPrefix.dust, GTMaterials.Bismuth, 2)
                .inputItems(TagPrefix.dust, GTMaterials.Zirconium).inputItems(TagPrefix.dust, GTMaterials.SteelMagnetic)
                .outputItems(TagPrefix.dust, MagnetoResonatic, 7).duration(300).EUt(GTValues.VA[GTValues.ZPM])
                .save(provider);
        GTRecipeTypes.AUTOCLAVE_RECIPES.recipeBuilder(GTNACORE.id("godforge_chain/magneto_resonatic_gem"))
                .inputItems(TagPrefix.dust, MagnetoResonatic).inputFluids(GTMaterials.Water.getFluid(250))
                .outputItems(TagPrefix.gem, MagnetoResonatic).duration(300).EUt(GTValues.VA[GTValues.LuV])
                .save(provider);

        GTRecipeTypes.MIXER_RECIPES.recipeBuilder(GTNACORE.id("godforge_chain/superconductor_uiv_base"))
                .inputItems(TagPrefix.dust, GTMaterials.Carbon, 14).inputItems(TagPrefix.dust, GTMaterials.Osmium, 11)
                .inputItems(TagPrefix.dust, GTMaterials.Silver, 3).inputItems(TagPrefix.dust, TranscendentMetal)
                .inputFluids(GTMaterials.Oxygen.getFluid(7000))
                .inputFluids(Infinity.getFluid(144))
                .outputItems(TagPrefix.dust, SuperconductorUIVBase, 4).duration(600).EUt(uiv).save(provider);
        melt(provider, "superconductor_uiv_base", SuperconductorUIVBase, 12_700, GTValues.UIV);
        GTRecipeTypes.MIXER_RECIPES.recipeBuilder(GTNACORE.id("godforge_chain/superconductor_umv_base"))
                .inputItems(TagPrefix.dust, SuperconductorUIVBase, 2).inputItems(TagPrefix.dust, Creon)
                .inputItems(TagPrefix.dust, Mellion).inputItems(TagPrefix.dust, SixPhasedCopper)
                .inputFluids(PhononMedium.getFluid(100))
                .outputItems(TagPrefix.dust, SuperconductorUMVBase, 4).duration(600).EUt(uxv).save(provider);
        melt(provider, "superconductor_umv_base", SuperconductorUMVBase, 13_500, GTValues.UXV);
        freeze(provider, "superconductor_uiv", SuperconductorUIVBase, SuperconductorUIV, uiv);
        freeze(provider, "superconductor_umv", SuperconductorUMVBase, SuperconductorUMV, uxv);

        var seed = com.raishxn.gtna.common.data.GTNAItems.PHONONIC_SEED_CRYSTAL;
        GTRecipeTypes.AUTOCLAVE_RECIPES.recipeBuilder(GTNACORE.id("godforge_chain/phononic_seed_crystal"))
                .inputItems(TagPrefix.dust, Mellion, 4).inputItems(TagPrefix.dust, TranscendentMetal, 2)
                .inputFluids(GTNAMaterials.DimensionallyTranscendentResidue.getFluid(1000))
                .outputItems(seed.asStack()).duration(1200).EUt(uxv).save(provider);
        GTNARecipeType.PLASMA_FORGE_RECIPES.recipeBuilder(GTNACORE.id("godforge_chain/phonon_crystal_solution"))
                .inputItems(seed.asStack()).inputItems(TagPrefix.dust, Dilithium, 8)
                .inputFluids(Mellion.getFluid(1152)).inputFluids(SixPhasedCopper.getFluid(288))
                .outputFluids(PhononCrystalSolution.getFluid(1000))
                .duration(1200).EUt(uxv).blastFurnaceTemp(13_500).save(provider);
        com.gregtechceu.gtceu.common.data.GCYMRecipeTypes.ALLOY_BLAST_RECIPES
                .recipeBuilder(GTNACORE.id("godforge_chain/phonon_medium"))
                .inputItems(TagPrefix.gem, MagnetoResonatic).inputItems(TagPrefix.dust, GTMaterials.Praseodymium, 4)
                .inputItems(TagPrefix.dust, SuperconductorUIVBase)
                .inputFluids(PhononCrystalSolution.getFluid(1000)).inputFluids(MetastableOganesson.getFluid(144))
                .outputFluids(PhononMedium.getFluid(1000))
                .duration(600).EUt(uxv).blastFurnaceTemp(13_500).save(provider);
        GTNARecipeType.PLASMA_FORGE_RECIPES.recipeBuilder(GTNACORE.id("godforge_chain/shirabon"))
                .inputFluids(Creon.getFluid(2304)).inputFluids(Mellion.getFluid(2304))
                .inputFluids(PhononMedium.getFluid(500)).inputFluids(ExcitedDTEC.getFluid(1000))
                .outputFluids(Shirabon.getFluid(4608))
                .outputFluids(GTNAMaterials.DimensionallyTranscendentResidue.getFluid(1000))
                .duration(2400).EUt(uxv).blastFurnaceTemp(13_500).save(provider);
    }

    private static void freeze(Consumer<FinishedRecipe> provider, String id, Material base, Material output,
                               long eut) {
        GTRecipeTypes.VACUUM_RECIPES.recipeBuilder(GTNACORE.id("godforge_chain/" + id))
                .inputItems(TagPrefix.ingot, base)
                .inputFluids(GTMaterials.Helium.getFluid(FluidStorageKeys.LIQUID, 1000))
                .outputItems(TagPrefix.ingot, output).duration(400).EUt(eut).save(provider);
    }

    private static void tpm(Consumer<FinishedRecipe> provider, String id, int circuit, long eut, long multiplier,
                            FluidStack output, FluidStack... inputs) {
        var builder = GTNARecipeType.TRANSCENDENT_PLASMA_MIXER_RECIPES
                .recipeBuilder(GTNACORE.id("godforge_chain/tpm_" + id)).circuitMeta(circuit).duration(100)
                .addData("tpm_eut", eut).addData("tpm_duration", 100).addData("eu_multiplier", multiplier);
        for (var input : inputs) builder.inputFluids(input);
        builder.outputFluids(output).save(provider);
    }

    /**
     * Stage 5 (GT5U TranscendentPlasmaMixerRecipes, Mixer/EBF/Vacuum Freezer, GoodGenerator): Excited catalysts,
     * Primordial Matter, Creon plasma, Harmonic Compound, Mellion, Orundum and the Atomic Separation Catalyst.
     * Substitutes: Fermium plasma → Americium, Fiery Steel → Blaze, Firestone → Redstone, Tiberium → Naquadria,
     * Raw Atomic Separation Catalyst → Naquadria dust.
     */
    private static void plasmaMixer(Consumer<FinishedRecipe> provider) {
        // GTNH controller: energy tunnels → UIV energy hatches, ProtoHalkonite → Six-Phased Copper.
        var mixer = com.raishxn.gtna.common.data.GTNATranscendentPlasmaMixer.TRANSCENDENT_PLASMA_MIXER;
        long uiv = GTValues.VA[GTValues.UIV];
        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(GTNACORE.id("godforge_chain/transcendent_plasma_mixer"))
                .inputItems(com.raishxn.gtna.common.data.GTNABlocks.PLASMA_FORGE_BRIDGE_CASING.asStack(8))
                .inputItems(com.gregtechceu.gtceu.common.data.GTMachines.ENERGY_INPUT_HATCH[GTValues.UIV].asStack(4))
                .inputItems(com.gregtechceu.gtceu.data.recipe.CustomTags.UIV_CIRCUITS, 16)
                .inputItems(com.gregtechceu.gtceu.common.data.GTItems.ELECTRIC_PUMP_UEV, 16)
                .inputItems(TagPrefix.plateDense, SixPhasedCopper, 8)
                .inputItems(TagPrefix.wireGtHex, SuperconductorUIV, 4)
                .inputFluids(ExcitedDTEC.getFluid(16_000))
                .inputFluids(GTNAMaterials.Indalloy140.getFluid(stacks(16)))
                .outputItems(mixer.asStack()).duration(6000).EUt(uiv)
                .stationResearch(b -> b.researchStack(
                        com.raishxn.gtna.common.data.GTNAPlasmaForge.PLASMA_FORGE.asStack()).CWUt(512).EUt(uiv))
                .save(provider);
        Material[] chain = { GTMaterials.Helium, GTMaterials.Iron, GTMaterials.Calcium, GTMaterials.Niobium,
                GTMaterials.Radon, GTMaterials.Nickel, GTMaterials.Boron, GTMaterials.Sulfur, GTMaterials.Nitrogen,
                GTMaterials.Zinc, GTMaterials.Silver, GTMaterials.Titanium, GTMaterials.Americium,
                GTMaterials.Bismuth, GTMaterials.Oxygen, GTMaterials.Tin, GTMaterials.Lead, GTMaterials.Thorium };
        int[] counts = { 4, 8, 12, 16 };
        Material[] outputs = { ExcitedDTCC, ExcitedDTPC, ExcitedDTRC, ExcitedDTEC };
        long[] eut = { 14_514_983L, 66_768_460L, 269_326_451L, 1_073_007_393L };
        for (int t = 0; t < 4; t++) {
            FluidStack[] in = new FluidStack[counts[t]];
            for (int i = 0; i < counts[t]; i++) in[i] = plasma(chain[i], 1000);
            tpm(provider, outputs[t].getName(), t + 1, eut[t], 10, outputs[t].getFluid(1000), in);
        }
        FluidStack[] stellar = new FluidStack[20];
        for (int i = 0; i < 18; i++) stellar[i] = plasma(chain[i], 1000);
        stellar[18] = plasma(GTMaterials.Naquadria, 100);
        stellar[19] = GTNAMaterials.RawStarMatter.getFluid(25);
        tpm(provider, "excited_dtsc", 5, 2_138_383_760L, 20, ExcitedDTSC.getFluid(1000), stellar);
        tpm(provider, "primordial_matter", 24, 2_000_000_000L, 10, PrimordialMatter.getFluid(1000),
                GTNAMaterials.RawStarMatter.getFluid(1000), GTNAMaterials.SpaceTime.getFluid(1000),
                GTNAMaterials.Space.getFluid(1000), GTNAMaterials.DimensionallyTranscendentResidue.getFluid(1000));
        tpm(provider, "creon_plasma", 23, GTValues.VA[GTValues.UXV], 10, plasma(Creon, 5000),
                plasma(GTMaterials.Americium, 1000), plasma(GTMaterials.Thorium, 1000),
                plasma(CelestialTungsten, 1000), plasma(GTMaterials.Calcium, 1000),
                GTNAMaterials.DimensionallyTranscendentResidue.getFluid(1000));

        var harmonic = com.raishxn.gtna.common.data.GTNAItems.HARMONIC_COMPOUND;
        GTRecipeTypes.BLAST_RECIPES.recipeBuilder(GTNACORE.id("godforge_chain/harmonic_compound"))
                .inputItems(TagPrefix.dust, Mellion).circuitMeta(11).inputFluids(plasma(Creon, 144))
                .outputItems(harmonic.asStack(2)).duration(600).EUt(GTValues.VA[GTValues.UXV])
                .blastFurnaceTemp(14_000).save(provider);
        GTRecipeTypes.VACUUM_RECIPES.recipeBuilder(GTNACORE.id("godforge_chain/harmonic_compound_split"))
                .inputItems(harmonic.asStack(2)).inputFluids(AtomicSeparationCatalyst.getFluid(144))
                .outputItems(TagPrefix.ingot, Mellion).outputFluids(Creon.getFluid(144))
                .duration(20).EUt(GTValues.VA[GTValues.UIV]).save(provider);
        GTRecipeTypes.MIXER_RECIPES.recipeBuilder(GTNACORE.id("godforge_chain/mellion"))
                .inputItems(TagPrefix.dust, GTMaterials.Tritanium, 11).inputItems(TagPrefix.dust, Orundum, 8)
                .inputItems(TagPrefix.dust, GTMaterials.Rubidium, 11).inputItems(TagPrefix.dust, GTMaterials.Blaze, 7)
                .inputItems(TagPrefix.dust, GTMaterials.Redstone, 13)
                .inputItems(TagPrefix.dust, AtomicSeparationCatalyst, 13)
                .inputFluids(GTNAMaterials.DimensionallyTranscendentResidue.getFluid(5000))
                .outputItems(TagPrefix.dust, Mellion, 63).duration(300).EUt(GTValues.VA[GTValues.UXV])
                .save(provider);
        GTRecipeTypes.FORMING_PRESS_RECIPES.recipeBuilder(GTNACORE.id("godforge_chain/orundum"))
                .inputItems(TagPrefix.plate, GTMaterials.Naquadria).inputItems(TagPrefix.plate, GTMaterials.Silicon, 8)
                .outputItems(TagPrefix.plate, Orundum).duration(400).EUt(GTValues.VA[GTValues.IV] / 2)
                .save(provider);
        GTRecipeTypes.BLAST_RECIPES.recipeBuilder(GTNACORE.id("godforge_chain/atomic_separation_catalyst"))
                .inputItems(TagPrefix.plate, Orundum, 2).inputItems(TagPrefix.dust, GTMaterials.Naquadria, 4)
                .inputFluids(GTMaterials.Plutonium239.getFluid(144))
                .outputItems(TagPrefix.ingot, AtomicSeparationCatalyst).duration(3600)
                .EUt(GTValues.VA[GTValues.HV]).blastFurnaceTemp(5_000).save(provider);
    }

    private static int stacks(double stacks) {
        return (int) Math.round(stacks * 64 * 144);
    }

    /**
     * Stage 4 (GT5U PlasmaForgeRecipes, ResearchStationAssemblyLine, GT++ RecipesGregTech): the DTPF, its parts and
     * the Special Laser Lens line. Mutated Living Solder has no source yet, so Indalloy 140 stands in; GTNH's
     * Oganesson becomes Radon, Avaritia/Eternal singularities become Quantum/Gravi Stars, the Exothermic Hearth
     * becomes the Mega Blast Furnace.
     */
    private static void plasmaForge(Consumer<FinishedRecipe> provider) {
        var forge = com.raishxn.gtna.common.data.GTNAPlasmaForge.PLASMA_FORGE;
        long uiv = GTValues.VA[GTValues.UIV];
        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(GTNACORE.id("godforge_chain/plasma_forge_controller"))
                .inputItems(com.raishxn.gtna.common.data.GTNABlocks.PLASMA_FORGE_BRIDGE_CASING.asStack(4))
                .inputItems(com.gregtechceu.gtceu.common.data.machines.GCYMMachines.MEGA_BLAST_FURNACE.asStack(16))
                .inputItems(com.gregtechceu.gtceu.common.data.GTMachines.ENERGY_INPUT_HATCH[GTValues.UEV].asStack(4))
                .inputItems(TagPrefix.wireGtHex, SuperconductorUIV, 2)
                .inputItems(com.gregtechceu.gtceu.data.recipe.CustomTags.UIV_CIRCUITS, 20)
                .inputItems(com.gregtechceu.gtceu.common.data.GTItems.FIELD_GENERATOR_UEV, 4)
                .inputItems(com.gregtechceu.gtceu.common.data.GTItems.GRAVI_STAR, 4)
                .inputItems(com.raishxn.gtna.common.data.GTNAItems.LASER_LENS_SPECIAL.asStack())
                .inputItems(TagPrefix.plateDense, GTMaterials.Osmiridium, 16)
                .inputItems(com.gregtechceu.gtceu.common.data.GTItems.ELECTRIC_PUMP_UEV, 4)
                .inputItems(com.gregtechceu.gtceu.common.data.GTItems.ENERGY_CLUSTER, 1)
                .inputFluids(GTMaterials.Radon.getFluid(128_000))
                .inputFluids(GTNAMaterials.Indalloy140.getFluid(stacks(8)))
                .inputFluids(GTMaterials.Americium.getFluid(stacks(4)))
                .inputFluids(GTMaterials.NaquadahEnriched.getFluid(stacks(4)))
                .outputItems(forge.asStack()).duration(6000).EUt(uiv)
                .stationResearch(b -> b.researchStack(
                        com.raishxn.gtna.common.data.GTNABlocks.PLASMA_FORGE_BRIDGE_CASING.asStack()).CWUt(512)
                        .EUt(uiv))
                .save(provider);
        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(GTNACORE.id("godforge_chain/plasma_forge_bridge_casing"))
                .inputItems(com.raishxn.gtna.common.data.GTNABlocks.DIMENSIONALLY_TRANSCENDENT_CASING.asStack())
                .inputItems(com.gregtechceu.gtceu.common.data.GTItems.EMITTER_UV, 1)
                .inputItems(com.gregtechceu.gtceu.data.recipe.CustomTags.UV_CIRCUITS, 2)
                .inputItems(com.gregtechceu.gtceu.common.data.GTItems.QUANTUM_STAR, 1)
                .inputItems(TagPrefix.wireGtSingle, GTMaterials.RutheniumTriniumAmericiumNeutronate, 6)
                .inputItems(com.gregtechceu.gtceu.common.data.GTItems.HIGHLY_ADVANCED_SOC_WAFER, 4)
                .inputItems(com.gregtechceu.gtceu.common.data.GTItems.FIELD_GENERATOR_UHV, 1)
                .inputFluids(GTMaterials.Radon.getFluid(8_000))
                .inputFluids(GTNAMaterials.Indalloy140.getFluid(stacks(1)))
                .inputFluids(GTMaterials.NaquadahEnriched.getFluid(1_296))
                .outputItems(com.raishxn.gtna.common.data.GTNABlocks.PLASMA_FORGE_BRIDGE_CASING.asStack())
                .duration(4800).EUt(uiv)
                .stationResearch(b -> b.researchStack(
                        com.raishxn.gtna.common.data.GTNABlocks.DIMENSION_INJECTION_CASING.asStack()).CWUt(256)
                        .EUt(uiv))
                .save(provider);
        var hypogenCoil = com.raishxn.gtna.common.data.GTNAGodforgeContent.HYPOGEN_COIL;
        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(GTNACORE.id("godforge_chain/hypogen_coil"))
                .inputItems(com.gregtechceu.gtceu.data.recipe.CustomTags.UEV_CIRCUITS, 1)
                .inputItems(TagPrefix.wireFine, Hypogen, 32).inputItems(TagPrefix.screw, Hypogen, 8)
                .inputItems(TagPrefix.foil, GTMaterials.Naquadria, 32)
                .inputFluids(Infinity.getFluid(576))
                .outputItems(hypogenCoil.asStack()).duration(1200).EUt(uiv)
                .stationResearch(b -> b.researchStack(
                        com.gregtechceu.gtceu.common.data.GTBlocks.COIL_TRITANIUM.asStack()).CWUt(256).EUt(uiv))
                .save(provider);
        long uxv = GTValues.VA[GTValues.UXV];
        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(GTNACORE.id("godforge_chain/eternal_coil"))
                .inputItems(com.gregtechceu.gtceu.data.recipe.CustomTags.UIV_CIRCUITS, 1)
                .inputItems(TagPrefix.rod, GTNAMaterials.SpaceTime, 8)
                .inputItems(TagPrefix.screw, GTNAMaterials.SpaceTime, 8)
                .inputItems(com.gregtechceu.gtceu.common.data.GTItems.GRAVI_STAR, 1)
                .inputItems(TagPrefix.foil, GTMaterials.Naquadria, 64)
                .inputFluids(Hypogen.getFluid(576))
                .outputItems(com.raishxn.gtna.common.data.GTNAGodforgeContent.ETERNAL_COIL.asStack())
                .duration(1200).EUt(uxv)
                .stationResearch(b -> b.researchStack(hypogenCoil.asStack()).CWUt(512).EUt(uxv))
                .save(provider);

        // Special Laser Lens, pre-DTPF route (GT++ Cyclotron of Duranium in GTNH; GTNA has no Cyclotron).
        var lens = com.raishxn.gtna.common.data.GTNAItems.LASER_LENS_SPECIAL;
        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(GTNACORE.id("godforge_chain/laser_lens_special_assembly"))
                .inputItems(TagPrefix.lens, GTMaterials.Diamond, 1)
                .inputItems(TagPrefix.plateDense, GTMaterials.Duranium, 4)
                .inputItems(com.gregtechceu.gtceu.common.data.GTItems.QUANTUM_STAR, 4)
                .inputItems(com.gregtechceu.gtceu.data.recipe.CustomTags.UV_CIRCUITS, 4)
                .inputFluids(GTMaterials.Naquadria.getFluid(1_152))
                .outputItems(lens.asStack()).duration(1200).EUt(GTValues.VA[GTValues.UV])
                .stationResearch(b -> b.researchStack(
                        com.gregtechceu.gtceu.common.data.GTItems.QUANTUM_STAR.asStack()).CWUt(64)
                        .EUt(GTValues.VA[GTValues.UV]))
                .save(provider);
        // GT5U "Quantum anomaly recipe bypass": Excited DTRC + Duranium in the DTPF, 10800 K.
        GTNARecipeType.PLASMA_FORGE_RECIPES.recipeBuilder(GTNACORE.id("godforge_chain/laser_lens_special"))
                .inputItems(com.gregtechceu.gtceu.common.data.GTItems.QUANTUM_STAR)
                .notConsumable(TagPrefix.lens, GTMaterials.Diamond)
                .notConsumable(net.minecraft.world.item.crafting.Ingredient.of(
                        com.gregtechceu.gtceu.data.recipe.CustomTags.UHV_CIRCUITS))
                .inputFluids(ExcitedDTRC.getFluid(92)).inputFluids(GTMaterials.Duranium.getFluid(144))
                .outputItems(lens.asStack()).outputFluids(GTNAMaterials.DimensionallyTranscendentResidue.getFluid(46))
                .duration(1200).EUt(GTValues.VA[GTValues.UEV]).blastFurnaceTemp(10_800)
                .save(provider);
        // GTNA bootstrap for Dimensionally Transcendent Residue (GTNH now makes it from the quark water chain).
        GTNARecipeType.PLASMA_FORGE_RECIPES.recipeBuilder(GTNACORE.id("godforge_chain/dtr"))
                .inputFluids(ExcitedDTCC.getFluid(1_000)).inputFluids(GTMaterials.Neutronium.getFluid(144))
                .outputFluids(GTNAMaterials.DimensionallyTranscendentResidue.getFluid(1_000))
                .duration(1200).EUt(GTValues.VA[GTValues.UV]).blastFurnaceTemp(10_800)
                .save(provider);
        // GT5U Six-Phased Copper; the Avaritia singularity (8×) becomes 8 Gravi Stars.
        GTNARecipeType.PLASMA_FORGE_RECIPES.recipeBuilder(GTNACORE.id("godforge_chain/six_phased_copper"))
                .inputItems(com.gregtechceu.gtceu.common.data.GTItems.GRAVI_STAR, 8)
                .inputFluids(CelestialTungsten.getFluid(stacks(1) + 8 * 144))
                .inputFluids(AstralTitanium.getFluid(stacks(4) + 32 * 144))
                .inputFluids(Hypogen.getFluid(36 * 144)).inputFluids(ChronomaticGlass.getFluid(stacks(9)))
                .inputFluids(Rhugnor.getFluid(18 * 144)).inputFluids(Mellion.getFluid(stacks(1) + 8 * 144))
                .outputFluids(SixPhasedCopper.getFluid(stacks(1) + 8 * 144))
                .duration(1200).EUt(uxv).blastFurnaceTemp(12_600)
                .save(provider);

        // GT++ Laser Engraver with the Special Lens (not consumed); Nitinol 60 from the Alloy Blast Smelter.
        engraveLens(provider, "celestial_tungsten", GTMaterials.Tungsten, CelestialTungsten, GTValues.UEV);
        engraveLens(provider, "astral_titanium", GTMaterials.Titanium, AstralTitanium, GTValues.UHV);
        engraveLens(provider, "chronomatic_glass", GTMaterials.Glass, ChronomaticGlass, GTValues.UHV);
        engraveLens(provider, "advanced_nitinol", Nitinol60, AdvancedNitinol, GTValues.UV);
        com.gregtechceu.gtceu.common.data.GCYMRecipeTypes.ALLOY_BLAST_RECIPES
                .recipeBuilder(GTNACORE.id("godforge_chain/nitinol_60"))
                .inputItems(TagPrefix.dust, GTMaterials.Nickel, 2).inputItems(TagPrefix.dust, GTMaterials.Titanium, 3)
                .circuitMeta(2).outputFluids(Nitinol60.getFluid(720))
                .duration(900).EUt(GTValues.VA[GTValues.IV]).blastFurnaceTemp(5_600)
                .save(provider);
        // GT++ standalone metals melt in the EBF (no autogenerated blast recipe on purpose, see the materials).
        melt(provider, "celestial_tungsten", CelestialTungsten, 10_800, GTValues.UEV);
        melt(provider, "astral_titanium", AstralTitanium, 10_800, GTValues.UHV);
        melt(provider, "chronomatic_glass", ChronomaticGlass, 9_200, GTValues.UHV);
        melt(provider, "advanced_nitinol", AdvancedNitinol, 8_400, GTValues.UV);
    }

    private static void engraveLens(Consumer<FinishedRecipe> provider, String id, Material input, Material output,
                                    int tier) {
        GTRecipeTypes.LASER_ENGRAVER_RECIPES.recipeBuilder(GTNACORE.id("godforge_chain/engrave_" + id))
                .inputItems(TagPrefix.dust, input)
                .notConsumable(com.raishxn.gtna.common.data.GTNAItems.LASER_LENS_SPECIAL.asStack())
                .outputItems(TagPrefix.dust, output).duration(200).EUt(GTValues.VA[tier])
                .save(provider);
    }

    private static void melt(Consumer<FinishedRecipe> provider, String id, Material material, int temp, int tier) {
        GTRecipeTypes.BLAST_RECIPES.recipeBuilder(GTNACORE.id("godforge_chain/blast_" + id))
                .inputItems(TagPrefix.dust, material).outputItems(TagPrefix.ingot, material)
                .duration(600).EUt(GTValues.VA[tier]).blastFurnaceTemp(temp)
                .save(provider);
    }

    /**
     * Stage 3 (GT++ RecipesGregTech / GoodGenerator RecipeLoader2): run in the Advanced Fusion Reactor, whose
     * buffer reaches 1.2 and 2 billion EU only with the UHV/UEV extensions. Metastable Oganesson: GTCEu's Oganesson
     * has no form, so Xenon replaces its gas (Radon would collide with GTCEu's Enriched Naquadah + Radon fusion).
     * Infinity, Quantum and Dragonblood get their routes in {@link #exoticRoutes}.
     */
    private static void advancedFusion(Consumer<FinishedRecipe> provider) {
        fusion(provider, "metastable_oganesson", GTMaterials.NaquadahEnriched.getFluid(144),
                GTMaterials.Xenon.getFluid(250), MetastableOganesson.getFluid(36), 600,
                GTValues.VA[GTValues.UV], 1_000_000_000L);
        fusion(provider, "rhugnor", Infinity.getFluid(144), Quantum.getFluid(288), Rhugnor.getFluid(144), 512,
                GTValues.VA[GTValues.UV], 2_000_000_000L);
        // GT++ "Rhugnor Mk5": Quark-Gluon Plasma from the Forge of Gods instead of Infinity.
        fusion(provider, "rhugnor_qgp", GTNAMaterials.QuarkGluonPlasma.getFluid(72), Quantum.getFluid(576),
                Rhugnor.getFluid(576), 50, GTValues.VA[GTValues.UEV], 2_000_000_000L);
        fusion(provider, "hypogen", DragonMetal.getFluid(144), Rhugnor.getFluid(288), Hypogen.getFluid(36), 8_172,
                GTValues.VA[GTValues.UHV], 1_200_000_000L);
    }

    private static FluidStack plasma(Material material, int amount) {
        return material.getFluid(FluidStorageKeys.PLASMA, amount);
    }

    /**
     * GTNH FusionReactorRecipes for Calcium, Sulfur, Zinc, Niobium, Silver, Bismuth, Radon and Thorium (Thorium's
     * 6 billion EU start lowered to 1 billion as GTO does, within the Advanced Fusion's buffer); GTO for
     * Titanium and Boron, which GTNH has no fusion for. Lead: GTNH fuses Tellurium, which has no form in GTCEu,
     * so Gold + Lithium (79 + 3 protons) replaces it.
     */
    private static void plasmas(Consumer<FinishedRecipe> provider) {
        fusion(provider, "calcium_plasma", GTMaterials.Magnesium.getFluid(128), GTMaterials.Oxygen.getFluid(128),
                plasma(GTMaterials.Calcium, 16), 128, GTValues.VA[GTValues.IV], 120_000_000L);
        fusion(provider, "sulfur_plasma", GTMaterials.Aluminium.getFluid(16), GTMaterials.Lithium.getFluid(16),
                plasma(GTMaterials.Sulfur, 144), 32, 10_240, 240_000_000L);
        fusion(provider, "zinc_plasma", GTMaterials.Copper.getFluid(72), GTMaterials.Tritium.getFluid(250),
                plasma(GTMaterials.Zinc, 72), 16, 49_152, 180_000_000L);
        fusion(provider, "niobium_plasma", GTMaterials.Cobalt.getFluid(144), GTMaterials.Silicon.getFluid(144),
                plasma(GTMaterials.Niobium, 144), 16, 49_152, 200_000_000L);
        fusion(provider, "silver_plasma", GTMaterials.Gold.getFluid(144), GTMaterials.Arsenic.getFluid(144),
                plasma(GTMaterials.Silver, 144), 16, 49_152, 350_000_000L);
        fusion(provider, "bismuth_plasma", GTMaterials.Tantalum.getFluid(144), plasma(GTMaterials.Zinc, 72),
                plasma(GTMaterials.Bismuth, 144), 16, 98_304, 350_000_000L);
        fusion(provider, "radon_plasma", GTMaterials.Iridium.getFluid(144), GTMaterials.Fluorine.getFluid(500),
                plasma(GTMaterials.Radon, 144), 32, 98_304, 450_000_000L);
        fusion(provider, "titanium_plasma", GTMaterials.Aluminium.getFluid(144), GTMaterials.Fluorine.getFluid(144),
                plasma(GTMaterials.Titanium, 144), 160, 49_152, 100_000_000L);
        fusion(provider, "boron_plasma", GTMaterials.Helium.getFluid(144), GTMaterials.Lithium.getFluid(144),
                plasma(GTMaterials.Boron, 144), 60, 10_240, 50_000_000L);
        fusion(provider, "lead_plasma", GTMaterials.Gold.getFluid(576), GTMaterials.Lithium.getFluid(576),
                plasma(GTMaterials.Lead, 576), 4, GTValues.VA[GTValues.UEV] / 2, 1_000_000_000L);
        fusion(provider, "thorium_plasma", GTMaterials.Osmium.getFluid(576), GTMaterials.Silicon.getFluid(576),
                plasma(GTMaterials.Thorium, 576), 4, GTValues.VA[GTValues.UEV] / 2, 1_000_000_000L);
    }

    private static void fusion(Consumer<FinishedRecipe> provider, String id, FluidStack a, FluidStack b,
                               FluidStack out, int ticks, long eut, long start) {
        GTRecipeTypes.FUSION_RECIPES.recipeBuilder(GTNACORE.id("godforge_chain/" + id))
                .inputFluids(a).inputFluids(b).outputFluids(out)
                .duration(ticks).EUt(eut).fusionStartEU(start)
                .save(provider);
    }

    /** GTNH MixerRecipes "Catalysts for Plasma Forge" and LaserEngraverRecipes (cleanroom). */
    private static void catalysts(Consumer<FinishedRecipe> provider) {
        mixer(provider, "dtcc", 9, GTValues.ZPM, DTCC, plasma(GTMaterials.Helium, 1000),
                plasma(GTMaterials.Iron, 1000), plasma(GTMaterials.Calcium, 1000), plasma(GTMaterials.Niobium, 1000));
        mixer(provider, "dtpc", 10, GTValues.UV, DTPC, DTCC.getFluid(1000), plasma(GTMaterials.Radon, 1000),
                plasma(GTMaterials.Nickel, 1000), plasma(GTMaterials.Boron, 1000), plasma(GTMaterials.Sulfur, 1000));
        mixer(provider, "dtrc", 11, GTValues.UHV, DTRC, DTPC.getFluid(1000), plasma(GTMaterials.Nitrogen, 1000),
                plasma(GTMaterials.Zinc, 1000), plasma(GTMaterials.Silver, 1000), plasma(GTMaterials.Titanium, 1000));
        mixer(provider, "dtec", 12, GTValues.UEV, DTEC, DTRC.getFluid(1000), plasma(GTMaterials.Americium, 1000),
                plasma(GTMaterials.Bismuth, 1000), plasma(GTMaterials.Oxygen, 1000), plasma(GTMaterials.Tin, 1000));
        // Naquadria plasma comes from the Forge's plasma module and Raw Star Matter from the Eye of Harmony,
        // so the Stellar catalyst stays post-Forge, as in GTNH.
        mixer(provider, "dtsc", 13, GTValues.UIV, DTSC, DTEC.getFluid(1000), plasma(GTMaterials.Lead, 1000),
                plasma(GTMaterials.Thorium, 1000), plasma(GTMaterials.Naquadria, 100),
                GTNAMaterials.RawStarMatter.getFluid(25));
        engrave(provider, "excited_dtcc", GTValues.ZPM, DTCC, ExcitedDTCC);
        engrave(provider, "excited_dtpc", GTValues.UV, DTPC, ExcitedDTPC);
        engrave(provider, "excited_dtrc", GTValues.UHV, DTRC, ExcitedDTRC);
        engrave(provider, "excited_dtec", GTValues.UEV, DTEC, ExcitedDTEC);
        engrave(provider, "excited_dtsc", GTValues.UIV, DTSC, ExcitedDTSC);
    }

    private static void mixer(Consumer<FinishedRecipe> provider, String id, int circuit, int tier, Material output,
                              FluidStack... inputs) {
        var builder = GTRecipeTypes.MIXER_RECIPES.recipeBuilder(GTNACORE.id("godforge_chain/" + id))
                .circuitMeta(circuit);
        for (var input : inputs) builder.inputFluids(input);
        builder.outputFluids(output.getFluid(1000)).duration(CATALYST_TICKS).EUt(GTValues.VA[tier]).save(provider);
    }

    private static void engrave(Consumer<FinishedRecipe> provider, String id, int tier, Material input,
                                Material output) {
        GTRecipeTypes.LASER_ENGRAVER_RECIPES.recipeBuilder(GTNACORE.id("godforge_chain/" + id))
                .inputFluids(input.getFluid(1000)).outputFluids(output.getFluid(1000))
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(CATALYST_TICKS).EUt(GTValues.VA[tier]).save(provider);
    }
}
