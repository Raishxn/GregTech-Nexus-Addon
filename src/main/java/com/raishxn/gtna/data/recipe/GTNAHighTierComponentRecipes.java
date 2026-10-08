package com.raishxn.gtna.data.recipe;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.fluids.FluidStack;

import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.common.data.GTNAMaterials;

import java.util.function.Consumer;

import static com.raishxn.gtna.common.data.material.GodforgeChainMaterials.*;

/**
 * Default recipes for what GTCEu registers without a recipe: UHV-OpV components (motor, piston, pump, conveyor,
 * robot arm, emitter, sensor, field generator), UEV-MAX machine casings and UEV-MAX energy hatches. Shapes follow
 * GTCEu's UV recipes ({@code ComponentRecipes}); each tier uses materials obtainable one tier below (the recipe
 * runs at the previous tier's voltage, as GTCEu's UHV components run at UV), so there is no voltage deadlock.
 * Modpacks are expected to replace them through KubeJS (ids {@code gtna:assembly_line/high_tier/...}).
 */
public final class GTNAHighTierComponentRecipes {

    private record Tier(int tier, String suffix, Material main, Material gearMat, Material magnet, Material fine,
                        Material wire, Material foil, Material solder, Material extra, ItemLike gem,
                        TagKey<Item> circuit, TagKey<Item> lower, TagKey<Item> lowest) {}

    private GTNAHighTierComponentRecipes() {}

    private static Item item(String name) {
        try {
            var entry = (com.tterrag.registrate.util.entry.ItemEntry<?>) GTItems.class.getField(name).get(null);
            return entry.get();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("GTItems." + name, e);
        }
    }

    public static void register(Consumer<FinishedRecipe> provider) {
        Tier[] tiers = {
                new Tier(GTValues.UHV, "UHV", GTMaterials.Neutronium, GTMaterials.NaquadahAlloy,
                        GTMaterials.SamariumMagnetic, GTMaterials.Americium,
                        GTMaterials.RutheniumTriniumAmericiumNeutronate, GTMaterials.Naquadria,
                        GTMaterials.SolderingAlloy, GTMaterials.Naquadria, GTItems.GRAVI_STAR,
                        CustomTags.UHV_CIRCUITS, CustomTags.UV_CIRCUITS, CustomTags.ZPM_CIRCUITS),
                new Tier(GTValues.UEV, "UEV", GTMaterials.Neutronium, GTMaterials.Neutronium,
                        GTMaterials.SamariumMagnetic, GTMaterials.Americium,
                        GTMaterials.RutheniumTriniumAmericiumNeutronate, GTMaterials.Naquadria,
                        GTNAMaterials.Indalloy140, GTMaterials.Naquadria, GTItems.GRAVI_STAR,
                        CustomTags.UEV_CIRCUITS, CustomTags.UHV_CIRCUITS, CustomTags.UV_CIRCUITS),
                new Tier(GTValues.UIV, "UIV", CelestialTungsten, CelestialTungsten, TengamAttuned,
                        CelestialTungsten, GTMaterials.RutheniumTriniumAmericiumNeutronate, CelestialTungsten,
                        GTNAMaterials.Indalloy140, Hypogen, GTItems.GRAVI_STAR,
                        CustomTags.UIV_CIRCUITS, CustomTags.UEV_CIRCUITS, CustomTags.UHV_CIRCUITS),
                new Tier(GTValues.UXV, "UXV", TranscendentMetal, TranscendentMetal, TengamAttuned,
                        TranscendentMetal, SuperconductorUIV, TranscendentMetal, MutatedLivingSolder, Creon,
                        GTItems.GRAVI_STAR, CustomTags.UXV_CIRCUITS, CustomTags.UIV_CIRCUITS,
                        CustomTags.UEV_CIRCUITS),
                new Tier(GTValues.OpV, "OpV", SixPhasedCopper, SixPhasedCopper, TengamAttuned, SixPhasedCopper,
                        SuperconductorUMV, SixPhasedCopper, MutatedLivingSolder, Shirabon, GTItems.GRAVI_STAR,
                        CustomTags.OpV_CIRCUITS, CustomTags.UXV_CIRCUITS, CustomTags.UIV_CIRCUITS),
        };
        // GTCEu 1.20 has no Mass Fabricator: default UU Matter for the Nano Forge (replace in the pack).
        GTRecipeTypes.MIXER_RECIPES.recipeBuilder(GTNACORE.id("high_tier/uu_matter"))
                .inputItems(TagPrefix.dustTiny, GTMaterials.Neutronium).inputFluids(GTMaterials.Helium.getFluid(1000))
                .outputFluids(GTMaterials.UUMatter.getFluid(100)).duration(200).EUt(GTValues.VA[GTValues.UV])
                .save(provider);
        circuits(provider);
        String[] previous = { "UV", "UHV", "UEV", "UIV", "UXV" };
        for (int i = 0; i < tiers.length; i++) components(provider, tiers[i], previous[i]);

        // Machine casings (UHV is GTCEu's) and energy hatches; MAX uses Shirabon.
        Material[] casing = { Neutronium(), CelestialTungsten, TranscendentMetal, SixPhasedCopper, Shirabon };
        int[] casingTier = { GTValues.UEV, GTValues.UIV, GTValues.UXV, GTValues.OpV, GTValues.MAX };
        String[] casingName = { "UEV", "UIV", "UXV", "OpV", "MAX" };
        Material[] hatchWire = { GTMaterials.RutheniumTriniumAmericiumNeutronate,
                GTMaterials.RutheniumTriniumAmericiumNeutronate, SuperconductorUIV, SuperconductorUMV,
                SuperconductorUMV };
        for (int i = 0; i < casing.length; i++) {
            int t = casingTier[i];
            ItemLike block;
            try {
                block = ((com.tterrag.registrate.util.entry.BlockEntry<?>) GTBlocks.class
                        .getField("MACHINE_CASING_" + casingName[i]).get(null)).get();
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(e);
            }
            GTRecipeTypes.ASSEMBLER_RECIPES
                    .recipeBuilder(GTNACORE.id("high_tier/casing_" + casingName[i].toLowerCase()))
                    .inputItems(TagPrefix.plate, casing[i], 8).circuitMeta(8)
                    .outputItems(new ItemStack(block)).duration(50).EUt(GTValues.VA[t - 1]).save(provider);
            GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GTNACORE.id("high_tier/energy_input_hatch_" +
                    casingName[i].toLowerCase()))
                    .inputItems(GTMachines.HULL[t].asStack()).inputItems(TagPrefix.wireGtQuadruple, hatchWire[i], 2)
                    .inputItems(TagPrefix.spring, casing[i], 2).circuitMeta(1)
                    .inputFluids(GTNAMaterials.Indalloy140.getFluid(576))
                    .outputItems(GTMachines.ENERGY_INPUT_HATCH[t].asStack()).duration(400)
                    .EUt(GTValues.VA[t - 1]).save(provider);
            GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GTNACORE.id("high_tier/energy_output_hatch_" +
                    casingName[i].toLowerCase()))
                    .inputItems(GTMachines.HULL[t].asStack()).inputItems(TagPrefix.wireGtQuadruple, hatchWire[i], 2)
                    .inputItems(TagPrefix.spring, casing[i], 2).circuitMeta(2)
                    .inputFluids(GTNAMaterials.Indalloy140.getFluid(576))
                    .outputItems(GTMachines.ENERGY_OUTPUT_HATCH[t].asStack()).duration(400)
                    .EUt(GTValues.VA[t - 1]).save(provider);
        }
    }

    /** Default recipes for the GTNA Optical (UV–UIV) and Exotic (UHV–UXV) circuits. */
    private static void circuits(Consumer<FinishedRecipe> provider) {
        var opt = new com.tterrag.registrate.util.entry.ItemEntry<?>[] {
                com.raishxn.gtna.common.data.GTNACircuits.OPTICAL_PROCESSOR,
                com.raishxn.gtna.common.data.GTNACircuits.OPTICAL_ASSEMBLY,
                com.raishxn.gtna.common.data.GTNACircuits.OPTICAL_COMPUTER,
                com.raishxn.gtna.common.data.GTNACircuits.OPTICAL_MAINFRAME };
        var exo = new com.tterrag.registrate.util.entry.ItemEntry<?>[] {
                com.raishxn.gtna.common.data.GTNACircuits.EXOTIC_PROCESSOR,
                com.raishxn.gtna.common.data.GTNACircuits.EXOTIC_ASSEMBLY,
                com.raishxn.gtna.common.data.GTNACircuits.EXOTIC_COMPUTER,
                com.raishxn.gtna.common.data.GTNACircuits.EXOTIC_MAINFRAME };
        family(provider, "optical", opt, GTValues.UV, GTItems.WETWARE_CIRCUIT_BOARD.get(), GTMaterials.Neutronium,
                GTMaterials.RutheniumTriniumAmericiumNeutronate, GTNAMaterials.Indalloy140);
        family(provider, "exotic", exo, GTValues.UHV, GTItems.WETWARE_CIRCUIT_BOARD.get(), CelestialTungsten,
                GTMaterials.RutheniumTriniumAmericiumNeutronate, GTNAMaterials.Indalloy140);
    }

    private static void family(Consumer<FinishedRecipe> provider, String name,
                               com.tterrag.registrate.util.entry.ItemEntry<?>[] items, int baseTier, Item board,
                               Material metal, Material wire, Material solder) {
        Item processor = items[0].get(), assembly = items[1].get(), computer = items[2].get(),
                mainframe = items[3].get();
        GTRecipeTypes.CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder(GTNACORE.id("high_tier/" + name + "_processor"))
                .inputItems(new ItemStack(board)).inputItems(GTItems.HIGHLY_ADVANCED_SOC.get())
                .inputItems(new ItemStack(GTItems.ADVANCED_SMD_TRANSISTOR.get(), 16))
                .inputItems(new ItemStack(GTItems.ADVANCED_SMD_CAPACITOR.get(), 16))
                .inputItems(TagPrefix.bolt, metal, 8).inputItems(TagPrefix.wireFine, GTMaterials.Americium, 16)
                .inputFluids(solder.getFluid(144)).outputItems(new ItemStack(processor, 2))
                .duration(200).EUt(GTValues.VA[baseTier]).cleanroom(
                        com.gregtechceu.gtceu.api.machine.multiblock.CleanroomType.CLEANROOM)
                .save(provider);
        GTRecipeTypes.CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder(GTNACORE.id("high_tier/" + name + "_assembly"))
                .inputItems(new ItemStack(board)).inputItems(new ItemStack(processor, 2))
                .inputItems(new ItemStack(GTItems.ADVANCED_SMD_INDUCTOR.get(), 16))
                .inputItems(new ItemStack(GTItems.ADVANCED_SMD_RESISTOR.get(), 16))
                .inputItems(new ItemStack(GTItems.HIGHLY_ADVANCED_SOC.get(), 2))
                .inputItems(TagPrefix.wireGtSingle, wire, 4)
                .inputFluids(solder.getFluid(288)).outputItems(new ItemStack(assembly))
                .duration(400).EUt(GTValues.VA[baseTier]).cleanroom(
                        com.gregtechceu.gtceu.api.machine.multiblock.CleanroomType.CLEANROOM)
                .save(provider);
        GTRecipeTypes.CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder(GTNACORE.id("high_tier/" + name + "_computer"))
                .inputItems(new ItemStack(board)).inputItems(new ItemStack(assembly, 2))
                .inputItems(new ItemStack(GTItems.ADVANCED_SMD_DIODE.get(), 32))
                .inputItems(new ItemStack(GTItems.HIGHLY_ADVANCED_SOC.get(), 4))
                .inputItems(TagPrefix.plate, metal, 4).inputItems(TagPrefix.wireGtSingle, wire, 8)
                .inputFluids(solder.getFluid(576)).outputItems(new ItemStack(computer))
                .duration(600).EUt(GTValues.VA[baseTier + 1]).cleanroom(
                        com.gregtechceu.gtceu.api.machine.multiblock.CleanroomType.CLEANROOM)
                .save(provider);
        long eut = GTValues.VA[baseTier + 2];
        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(GTNACORE.id("high_tier/" + name + "_mainframe"))
                .inputItems(TagPrefix.frameGt, metal, 2).inputItems(new ItemStack(computer, 2))
                .inputItems(new ItemStack(GTItems.ADVANCED_SMD_DIODE.get(), 64))
                .inputItems(new ItemStack(GTItems.ADVANCED_SMD_CAPACITOR.get(), 64))
                .inputItems(new ItemStack(GTItems.ADVANCED_SMD_TRANSISTOR.get(), 64))
                .inputItems(new ItemStack(GTItems.ADVANCED_SMD_RESISTOR.get(), 64))
                .inputItems(new ItemStack(GTItems.ADVANCED_SMD_INDUCTOR.get(), 64))
                .inputItems(TagPrefix.wireGtDouble, wire, 16).inputItems(TagPrefix.plateDense, metal, 4)
                .inputFluids(solder.getFluid(144 * 16)).inputFluids(GTMaterials.Naquadria.getFluid(144 * 8))
                .outputItems(new ItemStack(mainframe)).duration(1200).EUt(eut)
                .stationResearch(b -> b.researchStack(new ItemStack(computer)).CWUt(64).EUt(eut))
                .save(provider);
    }

    private static Material Neutronium() {
        return GTMaterials.Neutronium;
    }

    private static void components(Consumer<FinishedRecipe> provider, Tier t, String prev) {
        int k = t.tier() - GTValues.UV; // 1..5
        long eut = GTValues.VA[t.tier() - 1];
        int cwu = 32 << k;
        Item motor = item("ELECTRIC_MOTOR_" + t.suffix()), piston = item("ELECTRIC_PISTON_" + t.suffix()),
                pump = item("ELECTRIC_PUMP_" + t.suffix()), conveyor = item("CONVEYOR_MODULE_" + t.suffix()),
                arm = item("ROBOT_ARM_" + t.suffix()), emitter = item("EMITTER_" + t.suffix()),
                sensor = item("SENSOR_" + t.suffix()), field = item("FIELD_GENERATOR_" + t.suffix());
        FluidStack solder = t.solder().getFluid(144 * 4 * k);
        FluidStack lube = GTMaterials.Lubricant.getFluid(1000 * k);
        FluidStack extra = t.extra().getFluid(144 * 4 * k);
        line(provider, "electric_motor", t, prev, motor, cwu, eut, b -> b
                .inputItems(TagPrefix.rodLong, t.magnet()).inputItems(TagPrefix.rodLong, t.main(), 4)
                .inputItems(TagPrefix.ring, t.main(), 4).inputItems(TagPrefix.bolt, t.main(), 8)
                .inputItems(TagPrefix.wireFine, t.fine(), 64).inputItems(TagPrefix.wireFine, t.fine(), 64)
                .inputItems(TagPrefix.wireGtSingle, t.wire(), 2)
                .inputFluids(solder).inputFluids(lube).inputFluids(extra));
        line(provider, "electric_piston", t, prev, piston, cwu, eut, b -> b
                .inputItems(motor).inputItems(TagPrefix.plate, t.main(), 4).inputItems(TagPrefix.ring, t.main(), 4)
                .inputItems(TagPrefix.bolt, t.main(), 16).inputItems(TagPrefix.rod, t.main(), 4)
                .inputItems(TagPrefix.gear, t.gearMat()).inputItems(TagPrefix.gearSmall, t.gearMat(), 2)
                .inputItems(TagPrefix.wireGtSingle, t.wire(), 2)
                .inputFluids(solder).inputFluids(lube).inputFluids(extra));
        line(provider, "electric_pump", t, prev, pump, cwu, eut, b -> b
                .inputItems(motor).inputItems(TagPrefix.pipeLargeFluid, GTMaterials.Neutronium)
                .inputItems(TagPrefix.plate, t.main(), 2).inputItems(TagPrefix.screw, t.main(), 8)
                .inputItems(TagPrefix.ring, GTMaterials.SiliconeRubber, 16).inputItems(TagPrefix.gear, t.gearMat())
                .inputItems(TagPrefix.wireGtSingle, t.wire(), 2)
                .inputFluids(solder).inputFluids(lube).inputFluids(extra));
        line(provider, "conveyor_module", t, prev, conveyor, cwu, eut, b -> b
                .inputItems(new ItemStack(motor, 2)).inputItems(TagPrefix.plate, t.main(), 2)
                .inputItems(TagPrefix.ring, t.main(), 4).inputItems(TagPrefix.bolt, t.main(), 16)
                .inputItems(TagPrefix.screw, t.main(), 4).inputItems(TagPrefix.wireGtSingle, t.wire(), 2)
                .inputFluids(solder).inputFluids(lube)
                .inputFluids(GTMaterials.StyreneButadieneRubber.getFluid(144 * 24 * k)).inputFluids(extra));
        line(provider, "robot_arm", t, prev, arm, cwu, eut, b -> b
                .inputItems(TagPrefix.rodLong, t.main(), 4).inputItems(TagPrefix.gear, t.main())
                .inputItems(TagPrefix.gearSmall, t.main(), 3).inputItems(new ItemStack(motor, 2))
                .inputItems(piston).inputItems(t.circuit()).inputItems(t.lower(), 2).inputItems(t.lowest(), 4)
                .inputItems(TagPrefix.wireGtSingle, t.wire(), 4)
                .inputFluids(t.solder().getFluid(144 * 12 * k)).inputFluids(lube).inputFluids(extra));
        line(provider, "emitter", t, prev, emitter, cwu, eut, b -> b
                .inputItems(TagPrefix.frameGt, t.main()).inputItems(motor).inputItems(TagPrefix.rodLong, t.main(), 4)
                .inputItems(new ItemStack(t.gem(), k)).inputItems(t.circuit(), 2)
                .inputItems(TagPrefix.foil, t.foil(), 64).inputItems(TagPrefix.foil, t.foil(), 32)
                .inputItems(TagPrefix.wireGtSingle, t.wire(), 4)
                .inputFluids(t.solder().getFluid(144 * 8 * k)).inputFluids(extra));
        line(provider, "sensor", t, prev, sensor, cwu, eut, b -> b
                .inputItems(TagPrefix.frameGt, t.main()).inputItems(motor).inputItems(TagPrefix.plate, t.main(), 4)
                .inputItems(new ItemStack(t.gem(), k)).inputItems(t.circuit(), 2)
                .inputItems(TagPrefix.foil, t.foil(), 64).inputItems(TagPrefix.foil, t.foil(), 32)
                .inputItems(TagPrefix.wireGtSingle, t.wire(), 4)
                .inputFluids(t.solder().getFluid(144 * 8 * k)).inputFluids(extra));
        line(provider, "field_generator", t, prev, field, cwu, eut, b -> b
                .inputItems(TagPrefix.frameGt, t.main()).inputItems(TagPrefix.plate, t.main(), 6)
                .inputItems(new ItemStack(GTItems.QUANTUM_STAR.get(), k)).inputItems(new ItemStack(emitter, 2))
                .inputItems(t.circuit(), 2).inputItems(TagPrefix.wireFine, t.fine(), 64)
                .inputItems(TagPrefix.wireFine, t.fine(), 64).inputItems(TagPrefix.wireGtSingle, t.wire(), 4)
                .inputFluids(t.solder().getFluid(144 * 12 * k)).inputFluids(extra));
    }

    private static void line(Consumer<FinishedRecipe> provider, String name, Tier t, String prev, Item output,
                             int cwu, long eut,
                             java.util.function.UnaryOperator<com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder> inputs) {
        String prefix = switch (name) {
            case "electric_motor" -> "ELECTRIC_MOTOR_";
            case "electric_piston" -> "ELECTRIC_PISTON_";
            case "electric_pump" -> "ELECTRIC_PUMP_";
            case "conveyor_module" -> "CONVEYOR_MODULE_";
            case "robot_arm" -> "ROBOT_ARM_";
            case "emitter" -> "EMITTER_";
            case "sensor" -> "SENSOR_";
            default -> "FIELD_GENERATOR_";
        };
        Item research = item(prefix + prev);
        inputs.apply(GTRecipeTypes.ASSEMBLY_LINE_RECIPES
                .recipeBuilder(GTNACORE.id("high_tier/" + name + "_" + t.suffix().toLowerCase())))
                .outputItems(new ItemStack(output)).duration(600 + 200 * (t.tier() - GTValues.UV)).EUt(eut)
                .stationResearch(b -> b.researchStack(new ItemStack(research)).CWUt(cwu).EUt(eut))
                .save(provider);
    }
}
