package com.raishxn.gtna.data.recipe;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.machines.GCYMMachines;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

import com.raishxn.gtna.api.data.tag.GTNATagPrefix;
import com.raishxn.gtna.api.machine.feature.godforge.GodforgeUpgrade;
import com.raishxn.gtna.common.data.GTNABlocks;
import com.raishxn.gtna.common.data.GTNAGodforgeComponents;
import com.raishxn.gtna.common.data.GTNAGodforgeContent;
import com.raishxn.gtna.common.data.GTNAMachines3;
import com.raishxn.gtna.common.data.GTNAMaterials;
import com.raishxn.gtna.common.data.material.GodforgeChainMaterials;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Forge of Gods progression: the Research Station assembly line recipes of GTNH
 * {@code ResearchStationAssemblyLine.addGodforgeRecipes} and the upgrade extra costs of {@code Godforge.java}
 * (GT5-Unofficial a3e1e112). Recipe shapes, amounts, durations and tiers follow GTNH (GTNH UMV/UXV map to GTCEu
 * UXV); GTNH materials and items GTNA lacks use the substitutes listed in
 * {@code docs/roadmap/forge-of-gods-recipes-proposal.md}.
 */
public final class GTNAGodforgeProgression {

    private static final int INGOTS = 144;
    private static final int STACKS = 64 * INGOTS;
    private static final int SECONDS = 20;
    /** GTNH researches 48M CWU at 8192 CWU/t; GTCEu stations reach far less, so the time is kept instead. */
    private static final int CWUT = 256;
    private static final int TOTAL_CWU = 48_000_000 / 8_192 * CWUT;

    // GTNH materials, ported in G-0189..G-0195 (formerly GTNA substitutes).
    private static Material transcendentMetal() {
        return com.raishxn.gtna.common.data.material.GodforgeChainMaterials.TranscendentMetal;
    }

    private static Material creon() {
        return com.raishxn.gtna.common.data.material.GodforgeChainMaterials.Creon;
    }

    private static Material mellion() {
        return com.raishxn.gtna.common.data.material.GodforgeChainMaterials.Mellion;
    }

    private static Material sixPhasedCopper() {
        return com.raishxn.gtna.common.data.material.GodforgeChainMaterials.SixPhasedCopper;
    }

    private static Material hypogen() {
        return com.raishxn.gtna.common.data.material.GodforgeChainMaterials.Hypogen;
    }

    private static Material metastableOganesson() {
        return com.raishxn.gtna.common.data.material.GodforgeChainMaterials.MetastableOganesson;
    }

    private static Material eternity() {
        return com.raishxn.gtna.common.data.material.GodforgeChainMaterials.Eternity;
    }

    private static Material superconductorUIV() {
        return com.raishxn.gtna.common.data.material.GodforgeChainMaterials.SuperconductorUIV;
    }

    private GTNAGodforgeProgression() {}

    // ------------------------------------------------------------------ helpers

    /** A material form, falling back to coarser forms the material does generate. */
    private static ItemStack form(TagPrefix prefix, Material material, int amount) {
        TagPrefix[] chain = prefix == TagPrefix.plateDense ? new TagPrefix[] { TagPrefix.plateDense, TagPrefix.plate,
                TagPrefix.ingot } : prefix == TagPrefix.gearSmall ?
                        new TagPrefix[] { TagPrefix.gearSmall,
                                TagPrefix.gear, TagPrefix.plate } :
                        prefix == TagPrefix.rodLong ? new TagPrefix[] { TagPrefix.rodLong, TagPrefix.rod,
                                TagPrefix.ingot } :
                                new TagPrefix[] { prefix, TagPrefix.plate, TagPrefix.ingot, TagPrefix.dust };
        for (TagPrefix candidate : chain) {
            ItemStack stack = ChemicalHelper.get(candidate, material, Math.min(64, amount));
            if (!stack.isEmpty()) return stack;
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack item(ItemLike item, int amount) {
        return new ItemStack(item, amount);
    }

    private static FluidStack fluid(Material material, int amount) {
        Fluid fluid = material.getFluid();
        return fluid == null ? FluidStack.EMPTY : new FluidStack(fluid, amount);
    }

    private static FluidStack molten(Material material, int amount) {
        Fluid fluid = material.getFluid(FluidStorageKeys.MOLTEN);
        if (fluid == null) fluid = material.getFluid();
        return fluid == null ? FluidStack.EMPTY : new FluidStack(fluid, amount);
    }

    private static FluidStack plasma(Material material, int amount) {
        Fluid fluid = material.getFluid(FluidStorageKeys.PLASMA);
        return fluid == null ? molten(material, amount) : new FluidStack(fluid, amount);
    }

    /** GTNH Mutated Living Solder (GTNA route since G-0195). */
    private static FluidStack solder(int amount) {
        return fluid(com.raishxn.gtna.common.data.material.GodforgeChainMaterials.MutatedLivingSolder, amount);
    }

    private static void assemblyLine(Consumer<FinishedRecipe> provider, String id, ItemStack research,
                                     List<ItemStack> items, Object circuits, int circuitCount,
                                     List<FluidStack> fluids, ItemStack output, int duration, long eut) {
        var builder = GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder("godforge/" + id);
        for (ItemStack stack : items) if (!stack.isEmpty()) builder.inputItems(stack);
        if (circuits != null) builder.inputItems((net.minecraft.tags.TagKey<net.minecraft.world.item.Item>) circuits,
                circuitCount);
        for (FluidStack stack : fluids) if (!stack.isEmpty()) builder.inputFluids(stack);
        builder.outputItems(output).duration(duration).EUt(eut)
                .stationResearch(b -> b.researchStack(research).CWUt(CWUT, TOTAL_CWU)
                        .EUt(GTValues.VA[GTValues.UXV]))
                .save(provider);
    }

    // ------------------------------------------------------------------ recipes

    // GTNH items without a GT:IA counterpart.
    /** GTNH ZPM4 (Extremely Ultimate Battery). */
    private static ItemStack battery(int amount) {
        return component(GTNAGodforgeComponents.EXTREMELY_ULTIMATE_BATTERY, amount);
    }

    /** GTNH ZPM6 (Mega Ultimate Battery). */
    private static ItemStack megaBattery(int amount) {
        return component(GTNAGodforgeComponents.MEGA_ULTIMATE_BATTERY, amount);
    }

    /** Eternal Singularity (mod absent from GT:IA) → Gravi Stars. */
    private static ItemStack singularity(int amount) {
        return item(GTItems.GRAVI_STAR, amount);
    }

    /** TecTech UIV energy tunnel → GTCEu UIV 4096A laser target hatch. */
    private static ItemStack tunnel() {
        return GTMachines.LASER_INPUT_HATCH_4096[GTValues.UIV].asStack();
    }

    /** BartWorks UMV energy distributor → GTCEu UXV power transformer. */
    private static ItemStack distributor() {
        return GTMachines.POWER_TRANSFORMER[GTValues.UXV].asStack();
    }

    /** GT++ UV Fusion MK4 / GoodGenerator Compact Fusion MK5 → GTNA Advanced Fusion Reactor (GTO). */
    private static ItemStack advancedFusion(int amount) {
        return com.raishxn.gtna.common.data.GTNAAdvancedFusion.ADVANCED_FUSION_REACTOR.asStack(amount);
    }

    private static ItemStack component(com.tterrag.registrate.util.entry.ItemEntry<?> entry, int amount) {
        return new ItemStack(entry.get(), amount);
    }

    // ------------------------------------------------------------------ recipes

    /**
     * GTNH {@code addGodforgeRecipes}. Besides the helpers above: Exothermic Hearth → GCYM Mega Blast Furnace,
     * Multi Furnace → Multi Smelter, UV Plasma Generator (64) → Large Plasma Turbine (4), UIV Fluid Heater → GTCEu's,
     * Quantum Glass → Fusion Glass, BartWorks glass → Laminated Glass, NH Core lenses → Orundum / Nether Star /
     * Magneto Resonatic lenses, TFFT storage field → GTCEu Quantum Tank, Artificial Gravity Generator → Gravi Stars.
     */
    public static void register(Consumer<FinishedRecipe> provider) {
        long umv = GTValues.VA[GTValues.UXV];
        long uiv = GTValues.VA[GTValues.UIV];
        var siphon = GTNAGodforgeContent.STELLAR_ENERGY_SIPHON_CASING.get();
        var confinement = GTNAGodforgeContent.MAGNETIC_CONFINEMENT_CASING.get();
        var boundless = GTNAGodforgeContent.BOUNDLESS_STRUCTURE_CASING.get();
        var guidance = GTNAGodforgeContent.GUIDANCE_CASING.get();
        var shielding = GTNAGodforgeContent.SINGULARITY_SHIELDING_CASING.get();
        var conduit = GTNAGodforgeContent.HARMONIC_PHONON_TRANSMISSION_CONDUIT.get();
        var hypogenCoil = GTNAGodforgeContent.HYPOGEN_COIL.get();
        var boson = GTNAGodforgeComponents.STABLE_BOSON_CONTAINMENT_UNIT;
        var relativistic = GTNAGodforgeComponents.RELATIVISTIC_HEAT_CAPACITOR;
        var dtec = GodforgeChainMaterials.ExcitedDTEC;
        var phonon = GodforgeChainMaterials.PhononMedium;

        // Controller
        assemblyLine(provider, "forge_of_gods", item(siphon, 1),
                List.of(item(siphon, 4), battery(2), GTNABlocks.PLASMA_FORGE_BRIDGE_CASING.asStack(64),
                        singularity(32), form(TagPrefix.plateDense, mellion(), 16),
                        form(TagPrefix.plateDense, sixPhasedCopper(), 16), form(TagPrefix.plateDense, creon(), 16),
                        form(TagPrefix.plateDense, metastableOganesson(), 16), component(boson, 8),
                        form(TagPrefix.wireGtHex, superconductorUIV(), 16), item(GTItems.SENSOR_UIV, 32), tunnel(),
                        distributor()),
                CustomTags.UIV_CIRCUITS, 64,
                List.of(solder(32 * STACKS), fluid(dtec, 8_192_000), plasma(GTMaterials.Thorium, 4 * STACKS),
                        molten(transcendentMetal(), 32 * STACKS)),
                GTNAMachines3.FORGE_OF_GODS.asStack(), 300 * SECONDS, umv);

        // Magnetic Confinement Casing
        assemblyLine(provider, "magnetic_confinement_casing", form(TagPrefix.frameGt, transcendentMetal(), 1),
                List.of(form(TagPrefix.frameGt, transcendentMetal(), 8),
                        form(TagPrefix.block, GodforgeChainMaterials.MagnetoResonatic, 16),
                        form(TagPrefix.plateDense, GodforgeChainMaterials.TengamAttuned, 32),
                        form(TagPrefix.plate, creon(), 16), form(TagPrefix.screw, hypogen(), 8),
                        form(TagPrefix.screw, sixPhasedCopper(), 8),
                        component(GTNAGodforgeComponents.SUPERCONDUCTOR_COMPOSITE, 1), item(GTItems.EMITTER_UIV, 2),
                        component(GTNAGodforgeComponents.ELECTROMAGNET_TENGAM, 1)),
                null, 0, List.of(solder(16 * INGOTS), plasma(GTMaterials.Plutonium241, 16 * INGOTS)),
                item(confinement, 8), 50 * SECONDS, uiv);

        // Boundless Gravitationally Severed Structure Casing
        assemblyLine(provider, "boundless_structure_casing", item(confinement, 1),
                List.of(form(TagPrefix.frameGt, mellion(), 16), form(TagPrefix.frameGt, sixPhasedCopper(), 16),
                        form(TagPrefix.frameGt, transcendentMetal(), 8),
                        form(TagPrefix.frameGt, GodforgeChainMaterials.AstralTitanium, 8),
                        form(TagPrefix.plate, creon(), 6), component(boson, 1), item(GTItems.FIELD_GENERATOR_UEV, 2),
                        item(GTItems.GRAVI_STAR, 4)),
                null, 0, List.of(solder(16 * INGOTS), plasma(GTMaterials.Lead, 2 * INGOTS)),
                item(boundless, 1), 10 * SECONDS, uiv);

        // Celestial Matter Guidance Casing
        assemblyLine(provider, "guidance_casing", item(boundless, 1),
                List.of(item(boundless, 1), item(GTItems.ULTIMATE_BATTERY, 1),
                        GTNABlocks.COSMIC_FABRIC_MANIPULATOR.asStack(), item(GTItems.FIELD_GENERATOR_UEV, 2),
                        item(GTItems.EMITTER_UIV, 3), form(TagPrefix.plate, creon(), 6),
                        form(TagPrefix.gear, creon(), 8), form(TagPrefix.gearSmall, mellion(), 8)),
                null, 0, List.of(solder(16 * INGOTS), plasma(GTMaterials.Thorium, 2 * INGOTS)),
                item(guidance, 1), 10 * SECONDS, uiv);

        // Stellar Energy Siphon Casing
        assemblyLine(provider, "stellar_energy_siphon_casing", item(guidance, 1),
                List.of(item(boundless, 1), item(hypogenCoil, 64), item(hypogenCoil, 64),
                        form(TagPrefix.wireGtOctal, superconductorUIV(), 32),
                        component(GTNAGodforgeComponents.NEUTRONIUM_HEAT_CAPACITOR, 2),
                        component(GTNAGodforgeComponents.SPACE_COOLANT_CELL, 2), tunnel(),
                        GTMultiMachines.LARGE_PLASMA_TURBINE.asStack(4), form(TagPrefix.plateDense, creon(), 6),
                        form(TagPrefix.plate, hypogen(), 6)),
                null, 0, List.of(solder(16 * INGOTS),
                        molten(GodforgeChainMaterials.SuperconductorUIVBase, 32 * INGOTS), fluid(dtec, 128_000)),
                item(siphon, 1), 10 * SECONDS, uiv);

        // Gravitational Lens
        var lens = item(com.gregtechceu.gtceu.common.data.GTBlocks.FUSION_GLASS.get(), 1);
        assemblyLine(provider, "gravitational_lens", lens,
                List.of(item(com.gregtechceu.gtceu.common.data.GTBlocks.FUSION_GLASS.get(), 8),
                        item(com.gregtechceu.gtceu.common.data.GTBlocks.CASING_LAMINATED_GLASS.get(), 8),
                        GTNABlocks.FORCE_FIELD_GLASS.asStack(8), component(boson, 4),
                        form(TagPrefix.lens, GodforgeChainMaterials.MagnetoResonatic, 12),
                        form(TagPrefix.lens, GodforgeChainMaterials.Orundum, 6),
                        form(TagPrefix.lens, GTMaterials.NetherStar, 6),
                        form(TagPrefix.plateDense, GodforgeChainMaterials.ChronomaticGlass, 36),
                        form(TagPrefix.rodLong, creon(), 6), form(TagPrefix.rodLong, mellion(), 6),
                        form(TagPrefix.rodLong, sixPhasedCopper(), 6)),
                null, 0, List.of(molten(GodforgeChainMaterials.Rhugnor, 16 * INGOTS), molten(creon(), 16 * INGOTS),
                        molten(GodforgeChainMaterials.AdvancedNitinol, 16 * STACKS)),
                item(GTNAGodforgeContent.GRAVITATIONAL_LENS.get(), 1), 10 * SECONDS, uiv);

        // Remote Graviton Flow Modulator
        var anomaly = component(GTNAGodforgeComponents.GRAVITON_ANOMALY, 1);
        assemblyLine(provider, "remote_graviton_flow_modulator", anomaly,
                List.of(item(confinement, 2), GTNABlocks.FIELD_RESTRICTION_COIL_T3.asStack(),
                        form(TagPrefix.plate, creon(), 16), form(TagPrefix.gearSmall, mellion(), 8),
                        component(GTNAGodforgeComponents.GRAVITON_ANOMALY, 2),
                        com.raishxn.gtna.common.data.GTNAItems.LASER_LENS_SPECIAL.asStack(4),
                        item(GTItems.EMITTER_UIV, 4),
                        com.raishxn.gtna.common.data.GTNANanoForge.SILVER_NANITES.asStack(2)),
                CustomTags.UEV_CIRCUITS, 16,
                List.of(solder(32 * INGOTS), molten(GodforgeChainMaterials.SuperconductorUIVBase, 32 * INGOTS),
                        molten(GodforgeChainMaterials.Infinity, 32 * INGOTS)),
                item(GTNAGodforgeContent.REMOTE_GRAVITON_FLOW_MODULATOR.get(), 2), 10 * SECONDS, uiv);

        // Harmonic Phonon Transmission Conduit
        assemblyLine(provider, "harmonic_phonon_transmission_conduit", component(relativistic, 1),
                List.of(form(TagPrefix.frameGt, transcendentMetal(), 1), form(TagPrefix.rodLong, creon(), 12),
                        GTMachines.QUANTUM_TANK[GTValues.UHV].asStack(), component(GTNAGodforgeComponents.TESSERACT, 8),
                        component(relativistic, 4), component(GTNAGodforgeComponents.THERMAL_SUPERCONDUCTOR, 6),
                        item(GTItems.FIELD_GENERATOR_UEV, 4), form(TagPrefix.bolt, sixPhasedCopper(), 24)),
                null, 0, List.of(solder(STACKS), fluid(phonon, 1_000),
                        plasma(GTMaterials.Plutonium241, 16 * INGOTS)),
                item(conduit, 1), 10 * SECONDS, uiv);

        // Module controllers
        var hearth = GCYMMachines.MEGA_BLAST_FURNACE.asStack();
        module(provider, "godforge_smelting_module", hearth,
                List.of(item(shielding, 4), GCYMMachines.MEGA_BLAST_FURNACE.asStack(64),
                        GTMultiMachines.MULTI_SMELTER.asStack(64), battery(1),
                        form(TagPrefix.wireGtHex, superconductorUIV(), 16), item(GTItems.ROBOT_ARM_UIV, 16),
                        item(GTItems.CONVEYOR_MODULE_UIV, 32), form(TagPrefix.plateDense, sixPhasedCopper(), 16),
                        form(TagPrefix.plateDense, creon(), 8), form(TagPrefix.plateDense, mellion(), 8)),
                32, plasma(GTMaterials.Lead, 4 * STACKS), GTNAMachines3.GODFORGE_SMELTING_MODULE.asStack());
        module(provider, "godforge_molten_module", GTNAMachines3.MEGA_ALLOY_BLAST_SMELTER.asStack(),
                List.of(item(shielding, 4), GCYMMachines.MEGA_BLAST_FURNACE.asStack(64),
                        GTNAMachines3.MEGA_ALLOY_BLAST_SMELTER.asStack(64), battery(1),
                        form(TagPrefix.wireGtHex, superconductorUIV(), 32), item(GTItems.ROBOT_ARM_UIV, 16),
                        item(GTItems.CONVEYOR_MODULE_UIV, 32), item(GTItems.ELECTRIC_PUMP_UIV, 64),
                        component(relativistic, 8), form(TagPrefix.plateDense, sixPhasedCopper(), 16),
                        form(TagPrefix.plateDense, creon(), 8), form(TagPrefix.plateDense, mellion(), 8)),
                32, fluid(phonon, 32_000), GTNAMachines3.GODFORGE_MOLTEN_MODULE.asStack());
        var heater = GTMachines.FLUID_HEATER[GTValues.UIV].asStack();
        module(provider, "godforge_plasma_module", heater,
                List.of(item(shielding, 4), GTMachines.FLUID_HEATER[GTValues.UIV].asStack(64), advancedFusion(8),
                        battery(1), form(TagPrefix.wireGtHex, superconductorUIV(), 32),
                        item(GTItems.ROBOT_ARM_UIV, 16), item(GTItems.CONVEYOR_MODULE_UIV, 32),
                        item(GTItems.ELECTRIC_PUMP_UIV, 64), component(relativistic, 8),
                        form(GTNATagPrefix.superdensePlate, sixPhasedCopper(), 2),
                        form(GTNATagPrefix.superdensePlate, creon(), 1),
                        form(GTNATagPrefix.superdensePlate, mellion(), 1)),
                32, fluid(phonon, 32_000), GTNAMachines3.GODFORGE_PLASMA_MODULE.asStack());
        var mixer = com.raishxn.gtna.common.data.GTNATranscendentPlasmaMixer.TRANSCENDENT_PLASMA_MIXER;
        module(provider, "godforge_exotic_module", mixer.asStack(),
                List.of(item(shielding, 4), mixer.asStack(4), advancedFusion(1), battery(4),
                        form(TagPrefix.wireGtHex, superconductorUIV(), 64), item(GTItems.ROBOT_ARM_UIV, 16),
                        item(GTItems.CONVEYOR_MODULE_UIV, 32), item(GTItems.ELECTRIC_PUMP_UIV, 64), item(conduit, 8),
                        form(GTNATagPrefix.superdensePlate, sixPhasedCopper(), 4),
                        form(GTNATagPrefix.superdensePlate, creon(), 2),
                        form(GTNATagPrefix.superdensePlate, mellion(), 2)),
                64, fluid(phonon, 64_000), GTNAMachines3.GODFORGE_EXOTIC_MODULE.asStack());
    }

    private static void module(Consumer<FinishedRecipe> provider, String id, ItemStack research, List<ItemStack> items,
                               int circuits, FluidStack third, ItemStack output) {
        assemblyLine(provider, id, research, items, CustomTags.UIV_CIRCUITS, circuits,
                List.of(solder(16 * STACKS), fluid(GodforgeChainMaterials.ExcitedDTEC, 2_048_000), third,
                        molten(transcendentMetal(), 16 * STACKS)),
                output, 300 * SECONDS, GTValues.VA[GTValues.UXV]);
    }

    // ------------------------------------------------------------------ upgrade extra costs

    private static boolean costsRegistered;

    /**
     * GTNH {@code Godforge.java} extra costs (the branch with Eternal Singularity loaded). Idempotent. UHT Resistant
     * Mesh → Neutronium foil, GT++ Fusion Internal Casing 2 → Fusion Casing MK4, Compact Fusion Coil T4 →
     * Compressed Fusion Coil MK2, UMV parts → UXV, Transdimensional Alignment Matrix and MagMatter nanites (not
     * ported) are left out.
     */
    public static synchronized void registerExtraCosts() {
        if (costsRegistered) return;
        costsRegistered = true;
        var siphon = GTNAGodforgeContent.STELLAR_ENERGY_SIPHON_CASING.get();
        var conduit = GTNAGodforgeContent.HARMONIC_PHONON_TRANSMISSION_CONDUIT.get();
        var boundary = com.raishxn.gtna.common.data.GTNAEyeOfHarmonyContent.BOUNDARY_CASING.get();
        var fields = com.raishxn.gtna.common.data.GTNAEyeOfHarmonyContent.STABILISATION_FIELDS;
        cost(GodforgeUpgrade.START, form(TagPrefix.frameGt, GodforgeChainMaterials.SuperconductorUIVBase, 64),
                component(GTNAGodforgeComponents.SUPERCONDUCTOR_COMPOSITE, 32),
                form(TagPrefix.gear, metastableOganesson(), 16), singularity(8), item(GTItems.ROBOT_ARM_UIV, 64),
                item(GTItems.FIELD_GENERATOR_UEV, 64));
        cost(GodforgeUpgrade.FDIM, GTNAMachines3.MEGA_ALLOY_BLAST_SMELTER.asStack(16),
                item(GTNAGodforgeContent.HYPOGEN_COIL.get(), 64), item(conduit, 32), singularity(16),
                GTNABlocks.FIELD_RESTRICTION_COIL_T3.asStack(48), item(GTItems.ROBOT_ARM_UIV, 64),
                item(GTItems.FIELD_GENERATOR_UEV, 64));
        cost(GodforgeUpgrade.GPCI, item(siphon, 8), advancedFusion(8), GTNABlocks.FUSION_CASING_MK4.asStack(64),
                form(TagPrefix.foil, GTMaterials.Neutronium, 64),
                form(TagPrefix.plateDense, GodforgeChainMaterials.Quantum, 48),
                form(TagPrefix.gear, GodforgeChainMaterials.Rhugnor, 32), singularity(16),
                item(GTItems.ROBOT_ARM_UIV, 64), item(GTItems.FIELD_GENERATOR_UEV, 64));
        cost(GodforgeUpgrade.QGPIU, item(siphon, 16), advancedFusion(2),
                GTNABlocks.COMPRESSED_FUSION_COIL_MK2.asStack(64), item(conduit, 16),
                com.raishxn.gtna.common.data.GTNATranscendentPlasmaMixer.TRANSCENDENT_PLASMA_MIXER.asStack(4),
                form(TagPrefix.gear, GodforgeChainMaterials.Rhugnor, 64),
                form(TagPrefix.gear, GodforgeChainMaterials.Ichorium, 64), singularity(32),
                item(GTItems.ROBOT_ARM_UIV, 64), item(GTItems.FIELD_GENERATOR_UEV, 64));
        cost(GodforgeUpgrade.CD, form(TagPrefix.frameGt, GTNAMaterials.SpaceTime, 64),
                form(TagPrefix.frameGt, GodforgeChainMaterials.SuperconductorUMVBase, 64),
                form(TagPrefix.frameGt, hypogen(), 64), form(TagPrefix.frameGt, GodforgeChainMaterials.DragonMetal, 64),
                com.raishxn.gtna.common.data.GTNAEyeOfHarmonyContent.SPATIAL_CASING.asStack(64),
                item(boundary, 8), megaBattery(2), item(GTItems.FIELD_GENERATOR_UXV, 32));
        cost(GodforgeUpgrade.EE, form(TagPrefix.frameGt, GTNAMaterials.WhiteDwarfMatter, 64),
                form(TagPrefix.frameGt, GTNAMaterials.BlackDwarfMatter, 64), form(TagPrefix.frameGt, eternity(), 16),
                form(TagPrefix.frameGt, GTNAMaterials.Universium, 2), item(boundary, 64),
                item(fields[5].get(), 48), megaBattery(16), GTNAMachines3.GODFORGE_EXOTIC_MODULE.asStack(32),
                item(siphon, 64), item(GTItems.FIELD_GENERATOR_UXV, 64), item(GTItems.ROBOT_ARM_UXV, 64));
        cost(GodforgeUpgrade.END,
                form(TagPrefix.frameGt, GTNAMaterials.MagnetohydrodynamicallyConstrainedStarMatter, 64),
                form(TagPrefix.frameGt, eternity(), 64), form(TagPrefix.frameGt, GTNAMaterials.MagMatter, 64),
                item(fields[7].get(), 64), GTNAMachines3.GODFORGE_EXOTIC_MODULE.asStack(64),
                item(com.raishxn.gtna.common.data.GTNAEyeOfHarmonyContent.ASTRAL_ARRAY_FABRICATOR.get(), 4),
                megaBattery(32), item(GTItems.FIELD_GENERATOR_UXV, 64), item(GTItems.ROBOT_ARM_UXV, 64));
    }

    private static void cost(GodforgeUpgrade upgrade, ItemStack... stacks) {
        upgrade.clearExtraCost();
        List<GodforgeUpgrade.ExtraCost> costs = new ArrayList<>();
        for (ItemStack stack : stacks) {
            if (stack.isEmpty() || costs.size() >= 12) continue;
            costs.add(new GodforgeUpgrade.ExtraCost(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString(),
                    stack.getCount()));
        }
        upgrade.addExtraCost(costs.toArray(GodforgeUpgrade.ExtraCost[]::new));
    }
}
