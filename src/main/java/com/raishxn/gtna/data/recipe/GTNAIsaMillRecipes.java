package com.raishxn.gtna.data.recipe;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.data.recipes.FinishedRecipe;

import com.raishxn.gtna.api.data.tag.GTNATagPrefix;
import com.raishxn.gtna.common.data.GTNABlocks;
import com.raishxn.gtna.common.data.GTNAItems;
import com.raishxn.gtna.common.data.GTNAMachines2;
import com.raishxn.gtna.common.data.GTNAMachines3;
import com.raishxn.gtna.common.data.GTNAMaterials;
import com.raishxn.gtna.common.data.GTNARecipeDataKeys;
import com.raishxn.gtna.common.data.GTNARecipeType;

import java.util.function.Consumer;

/**
 * GTOCore ISA Mill content, ported from {@code data/recipe/classified/IsaMill.java},
 * {@code Assembler.java} and {@code FormingPress.java} / {@code AssemblyLine.java}:
 *
 * <ul>
 * <li>the 48 {@code ISA_MILL_RECIPES} wet-grinding recipes (12 ores × block-ore/raw-ore × ball
 * tiers 1/2). Every recipe keeps GTO's exact EUt (1920), duration, water amount, output count,
 * circuit and {@code grindball} data; {@code TagUtils.createTGItemTag("ores/x")} maps to GTCEu's
 * {@code TagPrefix.ore} tag and {@code TagPrefix.rawOre} is used verbatim;</li>
 * <li>the Inconel-625 casing, gearbox and pipe Assembler recipes;</li>
 * <li>the two Forming Press grinding-ball recipes;</li>
 * <li>the Grinding Ball Hatch Assembler recipe and the ISA Mill Assembly Line controller recipe,
 * including the original station research.</li>
 * </ul>
 */
public final class GTNAIsaMillRecipes {

    private GTNAIsaMillRecipes() {}

    public static void register(Consumer<FinishedRecipe> provider) {
        // --- GTOCore classified/IsaMill.java: block ores (circuit 1 = tier 1, 10 = tier 2) ---
        milledOre(provider, "milled_grossular_bgs", GTMaterials.Grossular, 1, 96, 4800);
        milledOre(provider, "milled_grossular_bal", GTMaterials.Grossular, 2, 72, 2400);
        milledOre(provider, "milled_almandine_bgs", GTMaterials.Almandine, 1, 96, 4800);
        milledOre(provider, "milled_almandine_bal", GTMaterials.Almandine, 2, 72, 2400);
        milledOre(provider, "milled_enriched_naquadah_bgs", GTMaterials.NaquadahEnriched, 1, 96, 4800);
        milledOre(provider, "milled_enriched_naquadah_bal", GTMaterials.NaquadahEnriched, 2, 72, 2400);
        milledOre(provider, "milled_chalcopyrite_bgs", GTMaterials.Chalcopyrite, 1, 96, 4800);
        milledOre(provider, "milled_chalcopyrite_bal", GTMaterials.Chalcopyrite, 2, 72, 2400);
        milledOre(provider, "milled_platinum_bgs", GTMaterials.Platinum, 1, 96, 4800);
        milledOre(provider, "milled_platinum_bal", GTMaterials.Platinum, 2, 72, 2400);
        milledOre(provider, "milled_redstone_bgs", GTMaterials.Redstone, 1, 96, 4800);
        milledOre(provider, "milled_redstone_bal", GTMaterials.Redstone, 2, 72, 2400);
        milledOre(provider, "milled_monazite_bgs", GTMaterials.Monazite, 1, 96, 4800);
        milledOre(provider, "milled_monazite_bal", GTMaterials.Monazite, 2, 72, 2400);
        milledOre(provider, "milled_pentlandite_bgs", GTMaterials.Pentlandite, 1, 96, 4800);
        milledOre(provider, "milled_pentlandite_bal", GTMaterials.Pentlandite, 2, 72, 2400);
        milledOre(provider, "milled_nickel_bgs", GTMaterials.Nickel, 1, 96, 4800);
        milledOre(provider, "milled_nickel_bal", GTMaterials.Nickel, 2, 72, 2400);
        milledOre(provider, "milled_spessartine_bgs", GTMaterials.Spessartine, 1, 96, 4800);
        milledOre(provider, "milled_spessartine_bal", GTMaterials.Spessartine, 2, 72, 2400);
        milledOre(provider, "milled_pyrope_bgs", GTMaterials.Pyrope, 1, 96, 4800);
        milledOre(provider, "milled_pyrope_bal", GTMaterials.Pyrope, 2, 72, 2400);
        milledOre(provider, "milled_sphalerite_bgs", GTMaterials.Sphalerite, 1, 96, 4800);
        milledOre(provider, "milled_sphalerite_bal", GTMaterials.Sphalerite, 2, 72, 2400);

        // --- GTOCore classified/IsaMill.java: raw ores (16 raw ore, half the water) ---
        milledRawOre(provider, "milled_grossular_rgs", GTMaterials.Grossular, 1, 48, 2400);
        milledRawOre(provider, "milled_grossular_ral", GTMaterials.Grossular, 2, 36, 1200);
        milledRawOre(provider, "milled_almandine_rgs", GTMaterials.Almandine, 1, 48, 2400);
        milledRawOre(provider, "milled_almandine_ral", GTMaterials.Almandine, 2, 36, 1200);
        milledRawOre(provider, "milled_enriched_naquadah_rgs", GTMaterials.NaquadahEnriched, 1, 48, 2400);
        milledRawOre(provider, "milled_enriched_naquadah_ral", GTMaterials.NaquadahEnriched, 2, 36, 1200);
        milledRawOre(provider, "milled_chalcopyrite_rgs", GTMaterials.Chalcopyrite, 1, 48, 2400);
        milledRawOre(provider, "milled_chalcopyrite_ral", GTMaterials.Chalcopyrite, 2, 36, 1200);
        milledRawOre(provider, "milled_platinum_rgs", GTMaterials.Platinum, 1, 48, 2400);
        milledRawOre(provider, "milled_platinum_ral", GTMaterials.Platinum, 2, 36, 1200);
        milledRawOre(provider, "milled_redstone_rgs", GTMaterials.Redstone, 1, 48, 2400);
        milledRawOre(provider, "milled_redstone_ral", GTMaterials.Redstone, 2, 36, 1200);
        milledRawOre(provider, "milled_monazite_rgs", GTMaterials.Monazite, 1, 48, 2400);
        milledRawOre(provider, "milled_monazite_ral", GTMaterials.Monazite, 2, 36, 1200);
        milledRawOre(provider, "milled_pentlandite_rgs", GTMaterials.Pentlandite, 1, 48, 2400);
        milledRawOre(provider, "milled_pentlandite_ral", GTMaterials.Pentlandite, 2, 36, 1200);
        milledRawOre(provider, "milled_nickel_rgs", GTMaterials.Nickel, 1, 48, 2400);
        milledRawOre(provider, "milled_nickel_ral", GTMaterials.Nickel, 2, 36, 1200);
        milledRawOre(provider, "milled_spessartine_rgs", GTMaterials.Spessartine, 1, 48, 2400);
        milledRawOre(provider, "milled_spessartine_ral", GTMaterials.Spessartine, 2, 36, 1200);
        milledRawOre(provider, "milled_pyrope_rgs", GTMaterials.Pyrope, 1, 48, 2400);
        milledRawOre(provider, "milled_pyrope_ral", GTMaterials.Pyrope, 2, 36, 1200);
        milledRawOre(provider, "milled_sphalerite_rgs", GTMaterials.Sphalerite, 1, 48, 2400);
        milledRawOre(provider, "milled_sphalerite_ral", GTMaterials.Sphalerite, 2, 36, 1200);

        // --- GTOCore classified/Assembler.java: Inconel-625 casing, gearbox and pipe ---
        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("inconel_625_casing")
                .inputItems(GTMachines.HULL[GTValues.EV].asStack().getItem())
                .inputItems(TagPrefix.plateDouble, GTNAMaterials.Inconel625, 4)
                .inputItems(TagPrefix.plateDouble, GTMaterials.HSSE, 8)
                .inputItems(TagPrefix.bolt, GTNAMaterials.Inconel625, 16)
                .inputFluids(GTMaterials.Titanium, 1152)
                .outputItems(GTNABlocks.INCONEL_625_CASING.asItem())
                .EUt(1920)
                .duration(480)
                .save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("inconel_625_gearbox")
                .inputItems(TagPrefix.frameGt, GTMaterials.HSSE)
                .inputItems(TagPrefix.gear, GTNAMaterials.Inconel625, 3)
                .inputItems(TagPrefix.gearSmall, GTMaterials.HSSS, 6)
                .inputItems(TagPrefix.bolt, GTMaterials.HSSG, 16)
                .inputItems(GTItems.COMPONENT_GRINDER_TUNGSTEN, 2)
                .inputFluids(GTMaterials.Zeron100, 576)
                .outputItems(GTNABlocks.INCONEL_625_GEARBOX.asItem())
                .EUt(30720)
                .duration(600)
                .save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("inconel_625_pipe")
                .inputItems(TagPrefix.frameGt, GTMaterials.HSSS)
                .inputItems(TagPrefix.plateDouble, GTNAMaterials.Inconel625, 4)
                .inputItems(TagPrefix.plateDouble, GTMaterials.HSSE, 8)
                .inputItems(TagPrefix.bolt, GTNAMaterials.Inconel625, 16)
                .inputFluids(GTMaterials.SolderingAlloy, 1152)
                .outputItems(GTNABlocks.INCONEL_625_PIPE.asItem())
                .EUt(1920)
                .duration(480)
                .save(provider);

        // --- GTOCore classified/FormingPress.java: the two grinding balls ---
        GTRecipeTypes.FORMING_PRESS_RECIPES.recipeBuilder("grindball_soapstone")
                .notConsumable(GTItems.SHAPE_MOLD_BALL.asItem())
                .inputItems(TagPrefix.dust, GTMaterials.Soapstone, 16)
                .inputItems(TagPrefix.ingot, GTMaterials.SolderingAlloy, 2)
                .outputItems(GTNAItems.GRINDBALL_SOAPSTONE)
                .EUt(7680)
                .duration(800)
                .save(provider);

        GTRecipeTypes.FORMING_PRESS_RECIPES.recipeBuilder("grindball_aluminium")
                .notConsumable(GTItems.SHAPE_MOLD_BALL.asItem())
                .inputItems(TagPrefix.dust, GTMaterials.Aluminium, 16)
                .inputItems(TagPrefix.ingot, GTMaterials.SolderingAlloy, 2)
                .outputItems(GTNAItems.GRINDBALL_ALUMINIUM)
                .EUt(7680)
                .duration(800)
                .save(provider);

        // --- GTOCore classified/Assembler.java: the grinding ball hatch ---
        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("grind_ball_hatch")
                .inputItems(TagPrefix.frameGt, GTMaterials.VanadiumSteel)
                .inputItems(GTMachines.ITEM_IMPORT_BUS[GTValues.IV].asStack().getItem())
                .inputItems(TagPrefix.gear, GTMaterials.Titanium, 8)
                .inputItems(TagPrefix.plateDouble, GTMaterials.TungstenSteel)
                .inputItems(TagPrefix.foil, GTMaterials.TungstenSteel, 32)
                .inputItems(TagPrefix.wireFine, GTMaterials.Tin, 16)
                .inputFluids(GTMaterials.Tungsten, 1152)
                .outputItems(GTNAMachines2.GRIND_BALL_HATCH.asStack())
                .EUt(480)
                .duration(400)
                .save(provider);

        // --- GTOCore classified/AssemblyLine.java: the ISA Mill controller ---
        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder("isa_mill")
                .inputItems(GTMachines.HULL[GTValues.LuV].asStack().getItem())
                .inputItems(GTNABlocks.INCONEL_625_GEARBOX.asItem(), 2)
                .inputItems(TagPrefix.plateDouble, GTNAMaterials.Inconel625, 8)
                .inputItems(CustomTags.LuV_CIRCUITS, 8)
                .inputItems(GTItems.CONVEYOR_MODULE_LuV, 4)
                .inputItems(TagPrefix.gear, GTNAMaterials.Inconel792, 4)
                .inputItems(TagPrefix.gearSmall, GTNAMaterials.Inconel625, 16)
                .inputItems(TagPrefix.screw, GTNAMaterials.Tantalloy61, 32)
                .inputItems(TagPrefix.plate, GTMaterials.Titanium, 8)
                .inputItems(TagPrefix.cableGtQuadruple, GTMaterials.NiobiumTitanium, 4)
                .inputFluids(GTMaterials.SolderingAlloy, 5760)
                .inputFluids(GTMaterials.Lubricant, 6000)
                .inputFluids(GTMaterials.Zeron100, 1152)
                .outputItems(GTNAMachines3.ISA_MILL.asStack())
                .EUt(30720)
                .duration(2400)
                .stationResearch(b -> b
                        .researchStack(GTMachines.MACERATOR[GTValues.IV].asStack())
                        .CWUt(32)
                        .EUt(7680))
                .save(provider);
    }

    /**
     * One block-ore milling recipe: 1 ore tag block + 100 mB distilled water → 96/72 MILLED.
     * Tier 1 runs on circuit 1 at double duration, tier 2 on circuit 10.
     */
    private static void milledOre(Consumer<FinishedRecipe> provider, String id, Material material, int ballTier,
                                  int outputCount, int duration) {
        GTNARecipeType.ISA_MILL_RECIPES.recipeBuilder(id)
                .circuitMeta(ballTier == 2 ? 10 : 1)
                .inputItems(TagPrefix.ore, material)
                .inputFluids(GTMaterials.DistilledWater, 100)
                .outputItems(GTNATagPrefix.MILLED, material, outputCount)
                .EUt(1920)
                .duration(duration)
                .addData(GTNARecipeDataKeys.GRINDBALL, ballTier)
                .save(provider);
    }

    /**
     * One raw-ore milling recipe: 16 raw ore + 50 mB distilled water → 48/36 MILLED. Tier 1 runs on
     * circuit 1 at double duration, tier 2 on circuit 10.
     */
    private static void milledRawOre(Consumer<FinishedRecipe> provider, String id, Material material, int ballTier,
                                     int outputCount, int duration) {
        GTNARecipeType.ISA_MILL_RECIPES.recipeBuilder(id)
                .circuitMeta(ballTier == 2 ? 10 : 1)
                .inputItems(TagPrefix.rawOre, material, 16)
                .inputFluids(GTMaterials.DistilledWater, 50)
                .outputItems(GTNATagPrefix.MILLED, material, outputCount)
                .EUt(1920)
                .duration(duration)
                .addData(GTNARecipeDataKeys.GRINDBALL, ballTier)
                .save(provider);
    }
}
