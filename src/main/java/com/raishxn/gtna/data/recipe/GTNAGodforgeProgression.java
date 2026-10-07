package com.raishxn.gtna.data.recipe;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

import com.raishxn.gtna.api.machine.feature.godforge.GodforgeUpgrade;
import com.raishxn.gtna.common.data.GTNAGodforgeContent;
import com.raishxn.gtna.common.data.GTNAMachines3;
import com.raishxn.gtna.common.data.GTNAMaterials;

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

    // Substitutes for GTNH materials absent from GTNA.
    private static Material transcendentMetal() {
        return GTNAMaterials.SpaceTime;
    }

    private static Material creon() {
        return GTNAMaterials.WhiteDwarfMatter;
    }

    private static Material mellion() {
        return GTNAMaterials.BlackDwarfMatter;
    }

    private static Material sixPhasedCopper() {
        return GTMaterials.Neutronium;
    }

    private static Material hypogen() {
        return GTMaterials.Tritanium;
    }

    private static Material metastableOganesson() {
        return GTMaterials.Darmstadtium;
    }

    private static Material eternity() {
        return GTNAMaterials.Universium;
    }

    private static Material superconductorUIV() {
        return GTMaterials.RutheniumTriniumAmericiumNeutronate;
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

    /** GTNH Mutated Living Solder; GTNA uses its best solder. */
    private static FluidStack solder(int amount) {
        return molten(GTNAMaterials.Indalloy140, amount);
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

        // Controller
        assemblyLine(provider, "forge_of_gods", item(siphon, 1),
                List.of(item(siphon, 4), item(GTItems.ENERGY_CLUSTER, 2),
                        form(TagPrefix.frameGt, transcendentMetal(), 64), item(GTItems.GRAVI_STAR, 32),
                        form(TagPrefix.plateDense, mellion(), 16), form(TagPrefix.plateDense, sixPhasedCopper(), 16),
                        form(TagPrefix.plateDense, creon(), 16), form(TagPrefix.plateDense, metastableOganesson(), 16),
                        item(GTItems.FIELD_GENERATOR_UIV, 8), form(TagPrefix.wireGtHex, superconductorUIV(), 16),
                        item(GTItems.SENSOR_UIV, 32)),
                CustomTags.UIV_CIRCUITS, 64,
                List.of(solder(32 * STACKS), fluid(GTNAMaterials.DimensionallyTranscendentResidue, 8_192_000),
                        plasma(GTMaterials.Thorium, 4 * STACKS), molten(transcendentMetal(), 32 * STACKS)),
                GTNAMachines3.FORGE_OF_GODS.asStack(), 300 * SECONDS, umv);

        // Magnetic Confinement Casing
        assemblyLine(provider, "magnetic_confinement_casing", form(TagPrefix.frameGt, transcendentMetal(), 1),
                List.of(form(TagPrefix.frameGt, transcendentMetal(), 8),
                        form(TagPrefix.block, GTMaterials.Neutronium, 16),
                        form(TagPrefix.plateDense, creon(), 32), form(TagPrefix.plate, creon(), 16),
                        form(TagPrefix.screw, hypogen(), 8), form(TagPrefix.screw, sixPhasedCopper(), 8),
                        item(GTItems.EMITTER_UIV, 2), item(GTItems.FIELD_GENERATOR_UEV, 1)),
                null, 0, List.of(solder(16 * INGOTS), plasma(GTMaterials.Plutonium241, 16 * INGOTS)),
                item(confinement, 8), 50 * SECONDS, uiv);

        // Boundless Gravitationally Severed Structure Casing
        assemblyLine(provider, "boundless_structure_casing", item(confinement, 1),
                List.of(form(TagPrefix.frameGt, mellion(), 16), form(TagPrefix.frameGt, sixPhasedCopper(), 16),
                        form(TagPrefix.frameGt, transcendentMetal(), 8), form(TagPrefix.frameGt, hypogen(), 8),
                        form(TagPrefix.plate, creon(), 6), item(GTItems.FIELD_GENERATOR_UIV, 1),
                        item(GTItems.FIELD_GENERATOR_UEV, 2), item(GTItems.GRAVI_STAR, 4)),
                null, 0, List.of(solder(16 * INGOTS), plasma(GTMaterials.Lead, 2 * INGOTS)),
                item(boundless, 1), 10 * SECONDS, uiv);

        // Celestial Matter Guidance Casing
        assemblyLine(provider, "guidance_casing", item(boundless, 1),
                List.of(item(boundless, 1), item(GTItems.ENERGY_MODULE, 1), item(GTItems.QUANTUM_STAR, 1),
                        item(GTItems.FIELD_GENERATOR_UEV, 2), item(GTItems.EMITTER_UIV, 3),
                        form(TagPrefix.plate, creon(), 6), form(TagPrefix.gear, creon(), 8),
                        form(TagPrefix.gearSmall, mellion(), 8)),
                null, 0, List.of(solder(16 * INGOTS), plasma(GTMaterials.Thorium, 2 * INGOTS)),
                item(guidance, 1), 10 * SECONDS, uiv);

        // Stellar Energy Siphon Casing
        assemblyLine(provider, "stellar_energy_siphon_casing", item(guidance, 1),
                List.of(item(boundless, 1), item(hypogenCoil, 64), item(hypogenCoil, 64),
                        form(TagPrefix.wireGtOctal, superconductorUIV(), 32), item(GTItems.ULTIMATE_BATTERY, 2),
                        item(GTItems.FIELD_GENERATOR_UIV, 2), form(TagPrefix.plateDense, creon(), 6),
                        form(TagPrefix.plate, hypogen(), 6)),
                null, 0, List.of(solder(16 * INGOTS), molten(superconductorUIV(), 32 * INGOTS),
                        fluid(GTNAMaterials.DimensionallyTranscendentResidue, 128_000)),
                item(siphon, 1), 10 * SECONDS, uiv);

        // Gravitational Lens
        assemblyLine(provider, "gravitational_lens", item(com.gregtechceu.gtceu.common.data.GTBlocks.FUSION_GLASS
                .get(), 1),
                List.of(item(com.gregtechceu.gtceu.common.data.GTBlocks.FUSION_GLASS.get(), 8),
                        item(com.gregtechceu.gtceu.common.data.GTBlocks.CASING_LAMINATED_GLASS.get(), 8),
                        item(GTItems.FIELD_GENERATOR_UIV, 4), form(TagPrefix.lens, GTMaterials.NetherStar, 6),
                        form(TagPrefix.lens, GTMaterials.Diamond, 6), form(TagPrefix.lens, GTMaterials.Ruby, 6),
                        form(TagPrefix.plateDense, GTMaterials.Neutronium, 36),
                        form(TagPrefix.rodLong, creon(), 6), form(TagPrefix.rodLong, mellion(), 6),
                        form(TagPrefix.rodLong, sixPhasedCopper(), 6)),
                null, 0, List.of(molten(hypogen(), 16 * INGOTS), molten(creon(), 16 * INGOTS),
                        molten(GTMaterials.Naquadria, 16 * STACKS)),
                item(GTNAGodforgeContent.GRAVITATIONAL_LENS.get(), 1), 10 * SECONDS, uiv);

        // Remote Graviton Flow Modulator
        assemblyLine(provider, "remote_graviton_flow_modulator", item(GTItems.ULTIMATE_BATTERY, 1),
                List.of(item(confinement, 2), item(GTItems.FIELD_GENERATOR_UIV, 1), form(TagPrefix.plate, creon(), 16),
                        form(TagPrefix.gearSmall, mellion(), 8), item(GTItems.ULTIMATE_BATTERY, 2),
                        item(GTItems.QUANTUM_STAR, 4), item(GTItems.EMITTER_UIV, 4),
                        form(TagPrefix.dust, GTMaterials.Silver, 2)),
                CustomTags.UEV_CIRCUITS, 16,
                List.of(solder(32 * INGOTS), molten(superconductorUIV(), 32 * INGOTS),
                        molten(GTMaterials.Neutronium, 32 * INGOTS)),
                item(GTNAGodforgeContent.REMOTE_GRAVITON_FLOW_MODULATOR.get(), 2), 10 * SECONDS, uiv);

        // Harmonic Phonon Transmission Conduit
        assemblyLine(provider, "harmonic_phonon_transmission_conduit", item(GTItems.FIELD_GENERATOR_UEV, 1),
                List.of(form(TagPrefix.frameGt, transcendentMetal(), 1), form(TagPrefix.rodLong, creon(), 12),
                        item(GTItems.QUANTUM_STAR, 8), item(GTItems.FIELD_GENERATOR_UEV, 4),
                        form(TagPrefix.bolt, sixPhasedCopper(), 24)),
                null, 0, List.of(solder(STACKS), fluid(GTNAMaterials.RawStarMatter, 1_000),
                        plasma(GTMaterials.Plutonium241, 16 * INGOTS)),
                item(conduit, 1), 10 * SECONDS, uiv);

        // Module controllers
        module(provider, "godforge_smelting_module", GTMultiMachines.ELECTRIC_BLAST_FURNACE.asStack(),
                List.of(item(shielding, 4), GTMultiMachines.ELECTRIC_BLAST_FURNACE.asStack(64),
                        GTMultiMachines.MULTI_SMELTER.asStack(64), item(GTItems.ENERGY_CLUSTER, 1),
                        form(TagPrefix.wireGtHex, superconductorUIV(), 16), item(GTItems.ROBOT_ARM_UIV, 16),
                        item(GTItems.CONVEYOR_MODULE_UIV, 32), form(TagPrefix.plateDense, sixPhasedCopper(), 16),
                        form(TagPrefix.plateDense, creon(), 8), form(TagPrefix.plateDense, mellion(), 8)),
                32, plasma(GTMaterials.Lead, 4 * STACKS), GTNAMachines3.GODFORGE_SMELTING_MODULE.asStack());
        module(provider, "godforge_molten_module", GTNAMachines3.MEGA_ALLOY_BLAST_SMELTER.asStack(),
                List.of(item(shielding, 4), GTMultiMachines.ELECTRIC_BLAST_FURNACE.asStack(64),
                        GTNAMachines3.MEGA_ALLOY_BLAST_SMELTER.asStack(64), item(GTItems.ENERGY_CLUSTER, 1),
                        form(TagPrefix.wireGtHex, superconductorUIV(), 32), item(GTItems.ROBOT_ARM_UIV, 16),
                        item(GTItems.CONVEYOR_MODULE_UIV, 32), item(GTItems.ELECTRIC_PUMP_UIV, 64),
                        form(TagPrefix.plateDense, sixPhasedCopper(), 16), form(TagPrefix.plateDense, creon(), 8),
                        form(TagPrefix.plateDense, mellion(), 8)),
                32, fluid(GTNAMaterials.RawStarMatter, 32_000), GTNAMachines3.GODFORGE_MOLTEN_MODULE.asStack());
        module(provider, "godforge_plasma_module", GTMultiMachines.FUSION_REACTOR[GTValues.UV].asStack(),
                List.of(item(shielding, 4), GTMultiMachines.FUSION_REACTOR[GTValues.UV].asStack(8),
                        item(GTItems.ENERGY_CLUSTER, 1), form(TagPrefix.wireGtHex, superconductorUIV(), 32),
                        item(GTItems.ROBOT_ARM_UIV, 16), item(GTItems.CONVEYOR_MODULE_UIV, 32),
                        item(GTItems.ELECTRIC_PUMP_UIV, 64), form(TagPrefix.plateDense, sixPhasedCopper(), 18),
                        form(TagPrefix.plateDense, creon(), 9), form(TagPrefix.plateDense, mellion(), 9)),
                32, fluid(GTNAMaterials.RawStarMatter, 32_000), GTNAMachines3.GODFORGE_PLASMA_MODULE.asStack());
        module(provider, "godforge_exotic_module", GTNAMachines3.GODFORGE_PLASMA_MODULE.asStack(),
                List.of(item(shielding, 4), GTMultiMachines.FUSION_REACTOR[GTValues.UV].asStack(16),
                        item(GTItems.ENERGY_CLUSTER, 4), form(TagPrefix.wireGtHex, superconductorUIV(), 64),
                        item(GTItems.ROBOT_ARM_UIV, 16), item(GTItems.CONVEYOR_MODULE_UIV, 32),
                        item(GTItems.ELECTRIC_PUMP_UIV, 64), item(conduit, 8),
                        form(TagPrefix.plateDense, sixPhasedCopper(), 36), form(TagPrefix.plateDense, creon(), 18),
                        form(TagPrefix.plateDense, mellion(), 18)),
                64, fluid(GTNAMaterials.RawStarMatter, 64_000), GTNAMachines3.GODFORGE_EXOTIC_MODULE.asStack());
    }

    private static void module(Consumer<FinishedRecipe> provider, String id, ItemStack research, List<ItemStack> items,
                               int circuits, FluidStack third, ItemStack output) {
        assemblyLine(provider, id, research, items, CustomTags.UIV_CIRCUITS, circuits,
                List.of(solder(16 * STACKS), fluid(GTNAMaterials.DimensionallyTranscendentResidue, 2_048_000), third,
                        molten(transcendentMetal(), 16 * STACKS)),
                output, 300 * SECONDS, GTValues.VA[GTValues.UXV]);
    }

    // ------------------------------------------------------------------ upgrade extra costs

    private static boolean costsRegistered;

    /** GTNH {@code Godforge.java} extra costs (the branch with Eternal Singularity loaded). Idempotent. */
    public static synchronized void registerExtraCosts() {
        if (costsRegistered) return;
        costsRegistered = true;
        var siphon = GTNAGodforgeContent.STELLAR_ENERGY_SIPHON_CASING.get();
        var conduit = GTNAGodforgeContent.HARMONIC_PHONON_TRANSMISSION_CONDUIT.get();
        cost(GodforgeUpgrade.START, form(TagPrefix.frameGt, superconductorUIV(), 64),
                form(TagPrefix.wireGtHex, superconductorUIV(), 32), form(TagPrefix.gear, metastableOganesson(), 16),
                item(GTItems.GRAVI_STAR, 8), item(GTItems.ROBOT_ARM_UIV, 64), item(GTItems.FIELD_GENERATOR_UEV, 64));
        cost(GodforgeUpgrade.FDIM, GTNAMachines3.MEGA_ALLOY_BLAST_SMELTER.asStack(16),
                item(GTNAGodforgeContent.HYPOGEN_COIL.get(), 64), item(conduit, 32), item(GTItems.GRAVI_STAR, 16),
                item(GTItems.FIELD_GENERATOR_UIV, 48), item(GTItems.ROBOT_ARM_UIV, 64),
                item(GTItems.FIELD_GENERATOR_UEV, 64));
        cost(GodforgeUpgrade.GPCI, item(siphon, 8), GTMultiMachines.FUSION_REACTOR[GTValues.UV].asStack(8),
                item(com.gregtechceu.gtceu.common.data.GTBlocks.FUSION_CASING_MK3.get(), 64),
                item(com.gregtechceu.gtceu.common.data.GTBlocks.FUSION_GLASS.get(), 64),
                form(TagPrefix.plateDense, GTMaterials.Naquadria, 48), form(TagPrefix.gear, hypogen(), 32),
                item(GTItems.GRAVI_STAR, 16), item(GTItems.ROBOT_ARM_UIV, 64), item(GTItems.FIELD_GENERATOR_UEV, 64));
        cost(GodforgeUpgrade.QGPIU, item(siphon, 16), GTMultiMachines.FUSION_REACTOR[GTValues.UV].asStack(2),
                item(com.gregtechceu.gtceu.common.data.GTBlocks.FUSION_COIL.get(), 64), item(conduit, 16),
                GTNAMachines3.GODFORGE_PLASMA_MODULE.asStack(4), form(TagPrefix.gear, hypogen(), 64),
                form(TagPrefix.gear, GTMaterials.Neutronium, 64), item(GTItems.GRAVI_STAR, 32),
                item(GTItems.ROBOT_ARM_UIV, 64), item(GTItems.FIELD_GENERATOR_UEV, 64));
        cost(GodforgeUpgrade.CD, form(TagPrefix.frameGt, GTNAMaterials.SpaceTime, 64),
                form(TagPrefix.frameGt, superconductorUIV(), 64), form(TagPrefix.frameGt, hypogen(), 64),
                form(TagPrefix.frameGt, GTMaterials.Neutronium, 64), item(GTItems.ULTIMATE_BATTERY, 2),
                item(GTItems.FIELD_GENERATOR_UXV, 32));
        cost(GodforgeUpgrade.EE, form(TagPrefix.frameGt, GTNAMaterials.WhiteDwarfMatter, 64),
                form(TagPrefix.frameGt, GTNAMaterials.BlackDwarfMatter, 64), form(TagPrefix.frameGt, eternity(), 16),
                form(TagPrefix.frameGt, GTNAMaterials.Universium, 2), item(GTItems.ULTIMATE_BATTERY, 16),
                GTNAMachines3.GODFORGE_EXOTIC_MODULE.asStack(32), item(siphon, 64),
                item(GTItems.FIELD_GENERATOR_UXV, 64), item(GTItems.ROBOT_ARM_UXV, 64));
        cost(GodforgeUpgrade.END, form(TagPrefix.ingot, GTNAMaterials.MagnetohydrodynamicallyConstrainedStarMatter, 64),
                form(TagPrefix.frameGt, eternity(), 64), form(TagPrefix.ingot, GTNAMaterials.MagMatter, 64),
                GTNAMachines3.GODFORGE_EXOTIC_MODULE.asStack(64), item(GTItems.ULTIMATE_BATTERY, 32),
                item(GTItems.FIELD_GENERATOR_UXV, 64), item(GTItems.ROBOT_ARM_UXV, 64));
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
