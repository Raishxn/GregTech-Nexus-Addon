package com.raishxn.gtna.data.recipe;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.api.machine.multiblock.CleanroomType;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.level.ItemLike;

import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.common.data.GTNAAdvancedFusion;
import com.raishxn.gtna.common.data.GTNABlocks;
import com.raishxn.gtna.common.data.GTNAMaterials;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;

/**
 * Recipes of the Advanced Fusion Reactor and its GTOCore blocks (G-0191). GTO recipes are kept when every input
 * exists in GTCEu/GTNA; GTO-only items are swapped for same-tier equivalents: Precision Assembler → Assembler in
 * a cleanroom, HUI circuits 1–5 → LuV–UEV circuits, GTO alloys → GTCEu/GTNA alloys of the same tier. Blocks GTO
 * builds through machines GTNA lacks get a tier-matched Assembler recipe. All are open to tuning.
 */
public final class GTNAAdvancedFusionRecipes {

    private GTNAAdvancedFusionRecipes() {}

    private static GTRecipeBuilder assembler(String id, long eut, int ticks) {
        return GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GTNACORE.id("advanced_fusion/" + id)).EUt(eut)
                .duration(ticks);
    }

    private static GTRecipeBuilder casing(String id, ItemLike output, Material frame, Material plate, int plates) {
        return assembler(id, 16, 50).inputItems(TagPrefix.frameGt, frame).inputItems(TagPrefix.plate, plate, plates)
                .circuitMeta(6).outputItems(output);
    }

    public static void register(Consumer<FinishedRecipe> provider) {
        // Controller: GTO PrecisionAssembler "luv_kuangbiao_one_giant_nuclear_fusion_reactor"; HUI circuit I → LuV.
        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(GTNACORE.id("advanced_fusion/controller"))
                .inputItems(GTMultiMachines.FUSION_REACTOR[LuV].asStack(16))
                .inputItems(GTNABlocks.IMPROVED_SUPERCONDUCTOR_COIL.asItem())
                .inputItems(CustomTags.LuV_CIRCUITS, 16)
                .inputItems(TagPrefix.plateDouble, GTMaterials.Osmiridium, 16)
                .inputFluids(GTMaterials.NiobiumTitanium.getFluid(864))
                .inputFluids(GTNAMaterials.Indalloy140.getFluid(1152))
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(2304))
                .inputFluids(GTMaterials.Polybenzimidazole.getFluid(2304))
                .outputItems(GTNAAdvancedFusion.ADVANCED_FUSION_REACTOR.asStack())
                .EUt(30720).duration(400)
                .stationResearch(b -> b.researchStack(GTMultiMachines.FUSION_REACTOR[LuV].asStack()).CWUt(32)
                        .EUt(VA[LuV]))
                .save(provider);

        // ---- Base structure (LuV) ----
        casing("strengthen_the_base_block", GTNABlocks.STRENGTHEN_THE_BASE_BLOCK, GTMaterials.Titanium,
                GTMaterials.Tungsten, 6).save(provider);
        casing("pbi_radiation_resistant_mechanical_enclosure", GTNABlocks.PBI_RADIATION_RESISTANT_MECHANICAL_ENCLOSURE,
                GTMaterials.Tungsten, GTMaterials.Polybenzimidazole, 6).save(provider);
        // GTO ReactorSteel → Stainless Steel.
        assembler("fission_reactor_casing", 16, 50).inputItems(TagPrefix.frameGt, GTMaterials.VanadiumSteel)
                .inputItems(TagPrefix.plate, GTMaterials.Lead, 6)
                .inputItems(TagPrefix.plate, GTMaterials.StainlessSteel, 6)
                .circuitMeta(6).outputItems(GTNABlocks.FISSION_REACTOR_CASING).save(provider);
        // GTO Special Ceramics + HUI I → LuV circuit and double Duranium plates.
        assembler("improved_superconductor_coil", 30720, 200).inputItems(GTBlocks.SUPERCONDUCTING_COIL.asItem())
                .inputItems(TagPrefix.plateDouble, GTMaterials.Duranium, 2).inputItems(CustomTags.LuV_CIRCUITS, 1)
                .inputFluids(GTNAMaterials.MarM200Steel.getFluid(1152)).inputFluids(GTMaterials.Europium.getFluid(144))
                .cleanroom(CleanroomType.CLEANROOM).outputItems(GTNABlocks.IMPROVED_SUPERCONDUCTOR_COIL)
                .save(provider);

        // ---- Extension 1 (ZPM) ----
        assembler("laser_casing", 16, 50).inputItems(TagPrefix.frameGt, GTMaterials.Iridium)
                .inputItems(TagPrefix.plateDouble, GTMaterials.Osmiridium, 2)
                .inputItems(TagPrefix.plateDouble, GTMaterials.Osmium, 4).circuitMeta(6)
                .outputItems(GTNABlocks.LASER_CASING).save(provider);
        // GTO Inconel 792 frame → Tungstensteel.
        assembler("antifreeze_heatproof_machine_casing", 16, 50)
                .inputItems(TagPrefix.frameGt, GTMaterials.TungstenSteel)
                .inputItems(TagPrefix.plateDouble, GTMaterials.HSSE, 2)
                .inputItems(TagPrefix.plateDouble, GTMaterials.RhodiumPlatedPalladium, 4).circuitMeta(6)
                .outputItems(GTNABlocks.ANTIFREEZE_HEATPROOF_MACHINE_CASING).save(provider);
        // GTO Depleted Uranium Alloy frame / Babbitt plates → Tungsten frame / Lead plates.
        assembler("radiation_absorbent_casing", 120, 200).inputItems(TagPrefix.frameGt, GTMaterials.Tungsten)
                .inputItems(TagPrefix.plateDouble, GTMaterials.Lead, 2)
                .inputItems(TagPrefix.plateDouble, GTMaterials.RhodiumPlatedPalladium, 4)
                .inputFluids(GTMaterials.Ultimet.getFluid(288)).outputItems(GTNABlocks.RADIATION_ABSORBENT_CASING)
                .save(provider);
        assembler("high_pressure_pipe_casing", VA[ZPM], 200).inputItems(TagPrefix.frameGt, GTMaterials.TungstenSteel)
                .inputItems(TagPrefix.pipeLargeFluid, GTMaterials.TungstenSteel, 2)
                .inputItems(TagPrefix.plateDouble, GTMaterials.Duranium, 4).circuitMeta(7)
                .outputItems(GTNABlocks.HIGH_PRESSURE_PIPE_CASING).save(provider);
        // GTO Inverter + HUI II → ZPM circuit and emitter.
        assembler("compressed_fusion_coil", 122880, 200).inputItems(GTBlocks.FUSION_COIL.asItem())
                .inputItems(GTItems.EMITTER_ZPM).inputItems(CustomTags.ZPM_CIRCUITS, 1)
                .inputFluids(GTNAMaterials.Tanmolyium.getFluid(1152)).inputFluids(GTMaterials.Americium.getFluid(144))
                .cleanroom(CleanroomType.CLEANROOM).outputItems(GTNABlocks.COMPRESSED_FUSION_COIL).save(provider);

        // ---- Extension 2 (UV) ----
        assembler("advanced_compressed_fusion_coil", 491520, 200).inputItems(GTNABlocks.COMPRESSED_FUSION_COIL)
                .inputItems(GTItems.QUANTUM_STAR).inputItems(CustomTags.UV_CIRCUITS, 1)
                .inputFluids(GTMaterials.Naquadria.getFluid(1152)).inputFluids(GTMaterials.Tritanium.getFluid(144))
                .cleanroom(CleanroomType.CLEANROOM).outputItems(GTNABlocks.ADVANCED_COMPRESSED_FUSION_COIL)
                .save(provider);
        assembler("optical_resonance_chamber", VA[UV], 300).inputItems(TagPrefix.frameGt, GTMaterials.Tritanium)
                .inputItems(GTItems.EMITTER_UV, 2).inputItems(GTBlocks.LASER_PIPES[0].asItem(), 8)
                .inputItems(TagPrefix.plateDouble, GTMaterials.Naquadria, 4).inputItems(CustomTags.UV_CIRCUITS, 1)
                .cleanroom(CleanroomType.CLEANROOM).outputItems(GTNABlocks.OPTICAL_RESONANCE_CHAMBER).save(provider);

        // ---- Extension 3 (UHV) ----
        // GTO Stabilized Naquadah Water Plant Casing → Naquadah Alloy frame.
        assembler("naquadah_reinforced_plant_casing", 480, 400).inputItems(TagPrefix.frameGt, GTMaterials.NaquadahAlloy)
                .inputItems(TagPrefix.bolt, GTNAMaterials.Tantalloy61, 16)
                .inputItems(TagPrefix.foil, GTMaterials.Naquadria, 8).inputFluids(GTMaterials.PCBCoolant.getFluid(1000))
                .outputItems(GTNABlocks.NAQUADAH_REINFORCED_PLANT_CASING).save(provider);
        // GTO Trinaquadalloy frame / Depleted Uranium Alloy → Trinium frame / Uranium-238.
        assembler("neutronium_stable_casing", 480, 200).inputItems(TagPrefix.frameGt, GTMaterials.Trinium)
                .inputItems(TagPrefix.rodLong, GTMaterials.Neutronium, 4)
                .inputItems(TagPrefix.plate, GTMaterials.EnrichedNaquadahTriniumEuropiumDuranide, 4)
                .inputItems(TagPrefix.plateDouble, GTMaterials.Naquadria, 4)
                .inputFluids(GTMaterials.Uranium238.getFluid(576)).outputItems(GTNABlocks.NEUTRONIUM_STABLE_CASING)
                .save(provider);
        // GTO Magnesium Oxide / Strontium Carbonate ceramic flakes → GTNA ceramics.
        assembler("magnesium_oxide_ceramic_block", 30, 200).inputItems(TagPrefix.frameGt, GTMaterials.HSLASteel)
                .inputItems(TagPrefix.plate, GTMaterials.TungstenCarbide, 2)
                .inputItems(TagPrefix.dust, GTNAMaterials.ZirconiaCeramic, 16)
                .outputItems(GTNABlocks.MAGNESIUM_OXIDE_CERAMIC_HIGH_TEMPERATURE_INSULATION_MECHANICAL_BLOCK)
                .save(provider);
        assembler("strontium_carbonate_ceramic_cube", 30, 200).inputItems(TagPrefix.frameGt, GTMaterials.Aluminium)
                .inputItems(TagPrefix.plate, GTMaterials.Osmium, 2)
                .inputItems(TagPrefix.dust, GTNAMaterials.BoronCarbideCeramics, 16)
                .outputItems(GTNABlocks.STRONTIUM_CARBONATE_CERAMIC_RAY_ABSORBING_MECHANICAL_CUBE).save(provider);
        // GTO Copper-76 dust → double Neodymium plate already in the recipe plus Europium pipe → Naquadah pipe.
        assembler("accelerated_pipeline", 7680, 400).inputItems(TagPrefix.pipeLargeFluid, GTMaterials.NiobiumTitanium)
                .inputItems(GTItems.VOLTAGE_COIL_LuV, 2).inputItems(CustomTags.LuV_CIRCUITS, 1)
                .inputItems(TagPrefix.cableGtSingle, GTMaterials.NiobiumNitride, 1)
                .inputItems(TagPrefix.plateDouble, GTMaterials.Neodymium, 1)
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(288)).cleanroom(CleanroomType.CLEANROOM)
                .outputItems(GTNABlocks.ACCELERATED_PIPELINE).save(provider);
        assembler("high_strength_support_mechanical_casing", VA[UHV], 200)
                .inputItems(TagPrefix.frameGt, GTMaterials.Neutronium)
                .inputItems(TagPrefix.plateDouble, GTMaterials.Tritanium, 4)
                .inputItems(TagPrefix.rodLong, GTMaterials.Neutronium, 4).circuitMeta(6)
                .outputItems(GTNABlocks.HIGH_STRENGTH_SUPPORT_MECHANICAL_CASING).save(provider);
        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(GTNACORE.id("advanced_fusion/containment_field_generator"))
                .inputItems(GTNABlocks.HOLLOW_CASING.asItem()).inputItems(GTItems.FIELD_GENERATOR_LuV, 4)
                .inputItems(TagPrefix.wireGtQuadruple, GTMaterials.IndiumTinBariumTitaniumCuprate, 6)
                .inputItems(CustomTags.UV_CIRCUITS, 4).inputItems(GTMachines.POWER_TRANSFORMER[UV].asStack())
                .inputItems(TagPrefix.plateDouble, GTMaterials.VanadiumGallium, 6)
                .inputItems(TagPrefix.plateDouble, GTMaterials.EnrichedNaquadahTriniumEuropiumDuranide, 4)
                .inputFluids(GTMaterials.Lanthanum.getFluid(2304)).inputFluids(GTMaterials.CobaltBrass.getFluid(5760))
                .inputFluids(GTMaterials.BatteryAlloy.getFluid(5760))
                .inputFluids(GTMaterials.MolybdenumDisilicide.getFluid(1296))
                .outputItems(GTNABlocks.CONTAINMENT_FIELD_GENERATOR).EUt(491520).duration(500)
                .stationResearch(b -> b.researchStack(GTNABlocks.HOLLOW_CASING.asStack()).CWUt(128).EUt(491520))
                .save(provider);
        // GTO Amprosium (= Neutronium) borosilicate glass + PVD → Fusion Glass + Neutronium in the Assembler.
        assembler("electron_permeable_amprosium_coated_glass", 122880, 100).inputItems(GTBlocks.FUSION_GLASS.asItem())
                .inputItems(TagPrefix.plate, GTMaterials.Neutronium, 2)
                .inputFluids(GTMaterials.Sulfur.getFluid(FluidStorageKeys.PLASMA, 288))
                .cleanroom(CleanroomType.CLEANROOM).outputItems(GTNABlocks.ELECTRON_PERMEABLE_AMPROSIUM_COATED_GLASS)
                .save(provider);
        assembler("amprosium_pipe_casing", VA[UHV], 100).inputItems(TagPrefix.frameGt, GTMaterials.Neutronium)
                .inputItems(TagPrefix.plate, GTMaterials.Neutronium, 4)
                .inputItems(TagPrefix.pipeNormalFluid, GTMaterials.Neutronium, 4).circuitMeta(8)
                .outputItems(GTNABlocks.AMPROSIUM_PIPE_CASING).save(provider);
        // GTO Advanced Fusion Coil / UHV voltage coil / HUI IV / Orichalcum → Fusion Coil, UHV circuits, Tritanium.
        assembler("fusion_casing_mk4", 1966080, 100).inputItems(GTBlocks.MACHINE_CASING_UHV.asItem())
                .inputItems(GTBlocks.FUSION_COIL.asItem(), 2).inputItems(CustomTags.UHV_CIRCUITS, 2)
                .inputItems(GTItems.FIELD_GENERATOR_UV).inputItems(TagPrefix.plate, GTMaterials.Tritanium, 6)
                .inputFluids(GTMaterials.Polybenzimidazole.getFluid(1152)).cleanroom(CleanroomType.CLEANROOM)
                .outputItems(GTNABlocks.FUSION_CASING_MK4).save(provider);
        assembler("compressed_fusion_coil_mk2_prototype", 1966080, 200)
                .inputItems(GTNABlocks.ADVANCED_COMPRESSED_FUSION_COIL).inputItems(GTItems.GRAVI_STAR)
                .inputItems(CustomTags.UHV_CIRCUITS, 1).inputFluids(GTMaterials.Neutronium.getFluid(1152))
                .inputFluids(GTMaterials.Duranium.getFluid(144)).cleanroom(CleanroomType.CLEANROOM)
                .outputItems(GTNABlocks.COMPRESSED_FUSION_COIL_MK2_PROTOTYPE).save(provider);

        // ---- Extension 4 (UEV) ----
        assembler("fusion_casing_mk5", 7864320, 100).inputItems(GTBlocks.MACHINE_CASING_UEV.asItem())
                .inputItems(GTNABlocks.ADVANCED_COMPRESSED_FUSION_COIL).inputItems(CustomTags.UEV_CIRCUITS, 2)
                .inputItems(GTItems.FIELD_GENERATOR_UHV).inputItems(TagPrefix.plate, GTMaterials.Neutronium, 6)
                .inputFluids(GTMaterials.Polybenzimidazole.getFluid(2304)).cleanroom(CleanroomType.CLEANROOM)
                .outputItems(GTNABlocks.FUSION_CASING_MK5).save(provider);
        assembler("compressed_fusion_coil_mk2", 7864320, 200)
                .inputItems(GTNABlocks.COMPRESSED_FUSION_COIL_MK2_PROTOTYPE).inputItems(GTItems.GRAVI_STAR, 4)
                .inputItems(CustomTags.UEV_CIRCUITS, 1).inputFluids(GTMaterials.Neutronium.getFluid(2304))
                .inputFluids(GTMaterials.Tritanium.getFluid(288)).cleanroom(CleanroomType.CLEANROOM)
                .outputItems(GTNABlocks.COMPRESSED_FUSION_COIL_MK2).save(provider);
        assembler("uev_hermetic_casing", VA[UEV], 200).inputItems(GTBlocks.MACHINE_CASING_UEV.asItem())
                .inputItems(TagPrefix.plate, GTMaterials.Neutronium, 8)
                .inputItems(TagPrefix.pipeLargeFluid, GTMaterials.Neutronium, 1).circuitMeta(6)
                .outputItems(GTNABlocks.HERMETIC_CASING_UEV).save(provider);
        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(GTNACORE.id("advanced_fusion/molecular_coil"))
                .inputItems(GTNABlocks.HOLLOW_CASING.asItem()).inputItems(GTBlocks.COIL_NAQUADAH.asItem(), 2)
                .inputItems(GTBlocks.FUSION_COIL.asItem(), 2).inputItems(TagPrefix.wireFine, GTMaterials.Europium, 64)
                .inputItems(TagPrefix.foil, GTMaterials.EnrichedNaquadahTriniumEuropiumDuranide, 32)
                .inputFluids(GTMaterials.BorosilicateGlass.getFluid(2304))
                .inputFluids(GTMaterials.SiliconeRubber.getFluid(5760))
                .inputFluids(GTMaterials.UraniumTriplatinum.getFluid(1296))
                .inputFluids(GTMaterials.Stellite100.getFluid(1296))
                .outputItems(GTNABlocks.MOLECULAR_COIL.asStack(2)).EUt(491520).duration(400)
                .stationResearch(b -> b.researchStack(
                        com.gregtechceu.gtceu.common.data.GCYMBlocks.MOLYBDENUM_DISILICIDE_COIL_BLOCK.asStack())
                        .CWUt(128).EUt(491520))
                .save(provider);
        machine("vacuum_chamber_beam_block", GTNABlocks.VACUUM_CHAMBER_BEAM_BLOCK, UEV, GTMaterials.Neutronium,
                GTItems.EMITTER_UHV.asItem()).save(provider);
        machine("high_pressure_gas_storage_tanks_casing", GTNABlocks.HIGH_PRESSURE_GAS_STORAGE_TANKS_CASING, UEV,
                GTMaterials.Tritanium, GTItems.ELECTRIC_PUMP_UHV.asItem()).save(provider);
        machine("accelerator_protection_casing", GTNABlocks.ACCELERATOR_PROTECTION_CASING, UEV, GTMaterials.Neutronium,
                GTItems.FIELD_GENERATOR_UHV.asItem()).save(provider);
        machine("accelerator_magnetic_constrained_rail_casing",
                GTNABlocks.ACCELERATOR_MAGNETIC_CONSTRAINED_RAIL_CASING, UEV, GTMaterials.Neutronium,
                GTItems.ELECTRIC_MOTOR_UHV.asItem()).save(provider);
        machine("coolant_pipe_casing", GTNABlocks.COOLANT_PIPE_CASING, UEV, GTMaterials.Tritanium,
                GTItems.ELECTRIC_PUMP_UV.asItem()).save(provider);
        assembler("plasma_field_glass", VA[UEV], 200).inputItems(GTBlocks.FUSION_GLASS.asItem(), 4)
                .inputItems(GTItems.FIELD_GENERATOR_UHV).inputFluids(GTMaterials.Neutronium.getFluid(576))
                .cleanroom(CleanroomType.CLEANROOM).outputItems(GTNABlocks.PLASMA_FIELD_GLASS.asStack(4))
                .save(provider);
        assembler("accelerator_observation_glass", VA[UEV], 200).inputItems(GTBlocks.FUSION_GLASS.asItem(), 4)
                .inputItems(GTItems.SENSOR_UHV).inputFluids(GTMaterials.Tritanium.getFluid(576))
                .cleanroom(CleanroomType.CLEANROOM).outputItems(GTNABlocks.ACCELERATOR_OBSERVATION_GLASS.asStack(4))
                .save(provider);
        // The MK2 recipe shape (frame, composite double plates, optical pipes) one tier up.
        assembler("machining_control_casing_mk3", VA[UEV], 400).inputItems(TagPrefix.frameGt, GTMaterials.Neutronium)
                .inputItems(TagPrefix.plateDouble, GTNAMaterials.CarbonFiberPolyphenyleneSulfideComposite, 8)
                .inputItems(GTBlocks.OPTICAL_PIPES[0].asItem(), 32).inputItems(CustomTags.UEV_CIRCUITS, 2)
                .inputFluids(GTMaterials.YttriumBariumCuprate.getFluid(1152))
                .inputFluids(GTNAMaterials.Indalloy140.getFluid(576)).circuitMeta(3)
                .outputItems(GTNABlocks.MACHINING_CONTROL_CASING_MK3).save(provider);
    }

    /** Tier casing for blocks GTO makes in machines GTNA lacks: frame + double plates + tier component. */
    private static GTRecipeBuilder machine(String id, ItemLike output, int tier, Material material,
                                           ItemLike component) {
        return assembler(id, VA[tier], 200).inputItems(TagPrefix.frameGt, material)
                .inputItems(TagPrefix.plateDouble, material, 4).inputItems(component, 2)
                .inputItems(CustomTags.CIRCUITS_ARRAY[tier], 1).cleanroom(CleanroomType.CLEANROOM)
                .outputItems(output);
    }
}
