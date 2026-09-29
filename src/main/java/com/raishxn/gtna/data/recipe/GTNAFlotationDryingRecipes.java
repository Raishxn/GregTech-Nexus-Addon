package com.raishxn.gtna.data.recipe;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.data.recipes.FinishedRecipe;

import com.raishxn.gtna.api.data.tag.GTNATagPrefix;
import com.raishxn.gtna.common.data.GTNABlocks;
import com.raishxn.gtna.common.data.GTNAMachines3;
import com.raishxn.gtna.common.data.GTNAMaterials;
import com.raishxn.gtna.common.data.GTNARecipeType;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.data.recipe.GTCraftingComponents.CABLE_QUAD;
import static com.gregtechceu.gtceu.data.recipe.GTCraftingComponents.CIRCUIT;
import static com.gregtechceu.gtceu.data.recipe.GTCraftingComponents.HULL;
import static com.gregtechceu.gtceu.data.recipe.GTCraftingComponents.PLATE;
import static com.gregtechceu.gtceu.data.recipe.GTCraftingComponents.ROBOT_ARM;
import static com.gregtechceu.gtceu.data.recipe.GTCraftingComponents.WIRE_QUAD;
import static com.gregtechceu.gtceu.data.recipe.misc.MetaTileEntityLoader.registerMachineRecipe;

/**
 * GTOCore Industrial Flotation Cell / Vacuum Drying Furnace data, ported from
 * {@code data/recipe/classified/FlotatingBeneficiation.java}, {@code VacuumDrying.java},
 * {@code Dehydrator.java}, {@code Assembler.java}, {@code AssemblyLine.java} and the red-mud
 * neutralisation of {@code data/recipe/processing/StoneDustProcess.java}:
 *
 * <ul>
 * <li>the 12 froth-flotation recipes (reagent dust + 64 MILLED ore + turpentine → 1000 mB ore foam)
 * with GTO's exact EUt and duration;</li>
 * <li>the 12 drying recipes (4000 mB ore foam → six GTCEu dusts + 200 mB Red Mud + 2000 mB Water)
 * with GTO's exact counts, EUt, duration and blast-furnace temperature;</li>
 * <li>the single Dehydrator recipe that only uses GTCEu resources ({@code salt_dust});</li>
 * <li>GTO's red-mud neutralisation (Red Mud + hydrochloric acid → neutralised red mud), the
 * consumer that keeps the {@code RedMud} output of the drying furnace out of a dead end;</li>
 * <li>the Hastelloy-N75 casing/gearbox/pipe, Flotation Cell and Red Steel Casing Assembler
 * recipes and the ported Assembly Line controller recipe.</li>
 * </ul>
 *
 * <p>
 * Documented omissions (GTO-exclusive resources, see the ledger checkpoint G-0107):
 * <ul>
 * <li>{@code metal_compound_particle_front} / {@code rarest_metal_mixture_dust} depend on GTO's
 * space-era {@code MetalCompoundParticles} and its five residue dusts plus {@code NanoScaleTungsten},
 * which GTNA does not port, so neither the foam nor its consumer exists; no other recipe produces
 * {@code MetalCompoundParticleFront}, so the chain stays closed;</li>
 * <li>{@code trinium_compound} is restricted to GTO's Mega Vacuum Drying Furnace and returns
 * {@code ResidualTriniiteSolution}, neither of which is ported;</li>
 * <li>the remaining Dehydrator recipes depend on GTO fluids ({@code SodiumHydroxideSolution},
 * {@code ErLuOxidesSolution}, {@code Hydrazine}, …) GTCEu does not have;</li>
 * <li>GTO's neutralised red mud is further processed by its Stone Dust chain (red slurry → titanyl
 * sulfate → titanium tetrachloride / rare-earth chlorides); that chain is out of scope for this
 * pair, so {@code NeutralisedRedMud} is a terminal output for now (documented rigid gap).</li>
 * </ul>
 */
public final class GTNAFlotationDryingRecipes {

    private GTNAFlotationDryingRecipes() {}

    public static void register(Consumer<FinishedRecipe> provider) {
        flotation(provider);
        drying(provider);
        dehydrator(provider);
        redMud(provider);
        casings(provider);
        controllers(provider);
    }

    /**
     * {@code classified/FlotatingBeneficiation.java}: exactly the 12 ore-based foams. Each recipe is
     * written out so the produced {@code *Front} identifier stays visible in the source (the
     * PortChainClosureTest scan and readability both rely on that).
     */
    private static void flotation(Consumer<FinishedRecipe> provider) {
        // Sodium ethylxanthate collector.
        GTNARecipeType.FLOTATING_BENEFICIATION_RECIPES.recipeBuilder("pyrope_front")
                .inputItems(TagPrefix.dust, GTNAMaterials.SodiumEthylxanthate, 32)
                .inputItems(GTNATagPrefix.MILLED, GTMaterials.Pyrope, 64)
                .inputFluids(GTNAMaterials.Turpentine, 8000)
                .outputFluids(GTNAMaterials.PyropeFront.getFluid(1000))
                .EUt(7680)
                .duration(4800)
                .save(provider);

        GTNARecipeType.FLOTATING_BENEFICIATION_RECIPES.recipeBuilder("redstone_front")
                .inputItems(TagPrefix.dust, GTNAMaterials.SodiumEthylxanthate, 32)
                .inputItems(GTNATagPrefix.MILLED, GTMaterials.Redstone, 64)
                .inputFluids(GTNAMaterials.Turpentine, 13000)
                .outputFluids(GTNAMaterials.RedstoneFront.getFluid(1000))
                .EUt(7680)
                .duration(4800)
                .save(provider);

        GTNARecipeType.FLOTATING_BENEFICIATION_RECIPES.recipeBuilder("almandine_front")
                .inputItems(TagPrefix.dust, GTNAMaterials.SodiumEthylxanthate, 32)
                .inputItems(GTNATagPrefix.MILLED, GTMaterials.Almandine, 64)
                .inputFluids(GTNAMaterials.Turpentine, 18000)
                .outputFluids(GTNAMaterials.AlmandineFront.getFluid(1000))
                .EUt(7680)
                .duration(4800)
                .save(provider);

        GTNARecipeType.FLOTATING_BENEFICIATION_RECIPES.recipeBuilder("platinum_front")
                .inputItems(TagPrefix.dust, GTNAMaterials.SodiumEthylxanthate, 32)
                .inputItems(GTNATagPrefix.MILLED, GTMaterials.Platinum, 64)
                .inputFluids(GTNAMaterials.Turpentine, 35000)
                .outputFluids(GTNAMaterials.PlatinumFront.getFluid(1000))
                .EUt(30720)
                .duration(4800)
                .save(provider);

        GTNARecipeType.FLOTATING_BENEFICIATION_RECIPES.recipeBuilder("sphalerite_front")
                .inputItems(TagPrefix.dust, GTNAMaterials.SodiumEthylxanthate, 32)
                .inputItems(GTNATagPrefix.MILLED, GTMaterials.Sphalerite, 64)
                .inputFluids(GTNAMaterials.Turpentine, 14000)
                .outputFluids(GTNAMaterials.SphaleriteFront.getFluid(1000))
                .EUt(30720)
                .duration(4800)
                .save(provider);

        GTNARecipeType.FLOTATING_BENEFICIATION_RECIPES.recipeBuilder("monazite_front")
                .inputItems(TagPrefix.dust, GTNAMaterials.SodiumEthylxanthate, 32)
                .inputItems(GTNATagPrefix.MILLED, GTMaterials.Monazite, 64)
                .inputFluids(GTNAMaterials.Turpentine, 30000)
                .outputFluids(GTNAMaterials.MonaziteFront.getFluid(1000))
                .EUt(30720)
                .duration(4800)
                .save(provider);

        // Potassium ethylxanthate collector; the naquadah recipe uses 64 dust and half the duration.
        GTNARecipeType.FLOTATING_BENEFICIATION_RECIPES.recipeBuilder("chalcopyrite_front")
                .inputItems(TagPrefix.dust, GTNAMaterials.PotassiumEthylxanthate, 32)
                .inputItems(GTNATagPrefix.MILLED, GTMaterials.Chalcopyrite, 64)
                .inputFluids(GTNAMaterials.Turpentine, 12000)
                .outputFluids(GTNAMaterials.ChalcopyriteFront.getFluid(1000))
                .EUt(7680)
                .duration(4800)
                .save(provider);

        GTNARecipeType.FLOTATING_BENEFICIATION_RECIPES.recipeBuilder("grossular_front")
                .inputItems(TagPrefix.dust, GTNAMaterials.PotassiumEthylxanthate, 32)
                .inputItems(GTNATagPrefix.MILLED, GTMaterials.Grossular, 64)
                .inputFluids(GTNAMaterials.Turpentine, 28000)
                .outputFluids(GTNAMaterials.GrossularFront.getFluid(1000))
                .EUt(30720)
                .duration(4800)
                .save(provider);

        GTNARecipeType.FLOTATING_BENEFICIATION_RECIPES.recipeBuilder("nickel_front")
                .inputItems(TagPrefix.dust, GTNAMaterials.PotassiumEthylxanthate, 32)
                .inputItems(GTNATagPrefix.MILLED, GTMaterials.Nickel, 64)
                .inputFluids(GTNAMaterials.Turpentine, 25000)
                .outputFluids(GTNAMaterials.NickelFront.getFluid(1000))
                .EUt(7680)
                .duration(4800)
                .save(provider);

        GTNARecipeType.FLOTATING_BENEFICIATION_RECIPES.recipeBuilder("pentlandite_front")
                .inputItems(TagPrefix.dust, GTNAMaterials.PotassiumEthylxanthate, 32)
                .inputItems(GTNATagPrefix.MILLED, GTMaterials.Pentlandite, 64)
                .inputFluids(GTNAMaterials.Turpentine, 14000)
                .outputFluids(GTNAMaterials.PentlanditeFront.getFluid(1000))
                .EUt(30720)
                .duration(4800)
                .save(provider);

        GTNARecipeType.FLOTATING_BENEFICIATION_RECIPES.recipeBuilder("spessartine_front")
                .inputItems(TagPrefix.dust, GTNAMaterials.PotassiumEthylxanthate, 32)
                .inputItems(GTNATagPrefix.MILLED, GTMaterials.Spessartine, 64)
                .inputFluids(GTNAMaterials.Turpentine, 35000)
                .outputFluids(GTNAMaterials.SpessartineFront.getFluid(1000))
                .EUt(30720)
                .duration(4800)
                .save(provider);

        GTNARecipeType.FLOTATING_BENEFICIATION_RECIPES.recipeBuilder("enriched_naquadah_front")
                .inputItems(TagPrefix.dust, GTNAMaterials.PotassiumEthylxanthate, 64)
                .inputItems(GTNATagPrefix.MILLED, GTMaterials.NaquadahEnriched, 64)
                .inputFluids(GTNAMaterials.Turpentine, 280000)
                .outputFluids(GTNAMaterials.EnrichedNaquadahFront.getFluid(1000))
                .EUt(491520)
                .duration(2400)
                .save(provider);
    }

    /** {@code classified/VacuumDrying.java}: the 12 non-mega recipes that consume the ported foams. */
    private static void drying(Consumer<FinishedRecipe> provider) {
        GTNARecipeType.VACUUM_DRYING_RECIPES.recipeBuilder("grossular_front_pro")
                .inputFluids(GTNAMaterials.GrossularFront, 4000)
                .outputItems(TagPrefix.dust, GTMaterials.Calcium, 64)
                .outputItems(TagPrefix.dust, GTMaterials.Calcium, 64)
                .outputItems(TagPrefix.dust, GTMaterials.Aluminium, 64)
                .outputItems(TagPrefix.dust, GTMaterials.Aluminium, 64)
                .outputItems(TagPrefix.dust, GTMaterials.Tungsten, 64)
                .outputItems(TagPrefix.dust, GTMaterials.Thallium, 16)
                .outputFluids(GTNAMaterials.RedMud.getFluid(200))
                .outputFluids(GTMaterials.Water.getFluid(2000))
                .EUt(30720)
                .duration(2400)
                .blastFurnaceTemp(5500)
                .save(provider);

        GTNARecipeType.VACUUM_DRYING_RECIPES.recipeBuilder("sphalerite_front_pro")
                .inputFluids(GTNAMaterials.SphaleriteFront, 4000)
                .outputItems(TagPrefix.dust, GTMaterials.Zinc, 64)
                .outputItems(TagPrefix.dust, GTMaterials.Zinc, 64)
                .outputItems(TagPrefix.dust, GTMaterials.Iron, 64)
                .outputItems(TagPrefix.dust, GTMaterials.Iron, 32)
                .outputItems(TagPrefix.dust, GTMaterials.Indium, 64)
                .outputItems(TagPrefix.dust, GTMaterials.Gallium, 64)
                .outputFluids(GTNAMaterials.RedMud.getFluid(200))
                .outputFluids(GTMaterials.Water.getFluid(2000))
                .EUt(30720)
                .duration(2400)
                .blastFurnaceTemp(5500)
                .save(provider);

        GTNARecipeType.VACUUM_DRYING_RECIPES.recipeBuilder("chalcopyrite_front_pro")
                .inputFluids(GTNAMaterials.ChalcopyriteFront, 4000)
                .outputItems(TagPrefix.dust, GTMaterials.Copper, 64)
                .outputItems(TagPrefix.dust, GTMaterials.Copper, 64)
                .outputItems(TagPrefix.dust, GTMaterials.Iron, 64)
                .outputItems(TagPrefix.dust, GTMaterials.Iron, 48)
                .outputItems(TagPrefix.dust, GTMaterials.Cadmium, 48)
                .outputItems(TagPrefix.dust, GTMaterials.Indium, 32)
                .outputFluids(GTNAMaterials.RedMud.getFluid(200))
                .outputFluids(GTMaterials.Water.getFluid(2000))
                .EUt(7680)
                .duration(2400)
                .blastFurnaceTemp(4500)
                .save(provider);

        GTNARecipeType.VACUUM_DRYING_RECIPES.recipeBuilder("nickel_front_pro")
                .inputFluids(GTNAMaterials.NickelFront, 4000)
                .outputItems(TagPrefix.dust, GTMaterials.Nickel, 64)
                .outputItems(TagPrefix.dust, GTMaterials.Nickel, 64)
                .outputItems(TagPrefix.dust, GTMaterials.Cobalt, 64)
                .outputItems(TagPrefix.dust, GTMaterials.Cobalt, 64)
                .outputItems(TagPrefix.dust, GTMaterials.Iron, 32)
                .outputItems(TagPrefix.dust, GTMaterials.Rhodium, 32)
                .outputFluids(GTNAMaterials.RedMud.getFluid(200))
                .outputFluids(GTMaterials.Water.getFluid(2000))
                .EUt(7680)
                .duration(2400)
                .blastFurnaceTemp(4500)
                .save(provider);

        GTNARecipeType.VACUUM_DRYING_RECIPES.recipeBuilder("pyrope_front_pro")
                .inputFluids(GTNAMaterials.PyropeFront, 4000)
                .outputItems(TagPrefix.dust, GTMaterials.Magnesium, 64)
                .outputItems(TagPrefix.dust, GTMaterials.Magnesium, 64)
                .outputItems(TagPrefix.dust, GTMaterials.Aluminium, 64)
                .outputItems(TagPrefix.dust, GTMaterials.Manganese, 64)
                .outputItems(TagPrefix.dust, GTMaterials.Boron, 64)
                .outputItems(TagPrefix.dust, GTMaterials.Silicon, 48)
                .outputFluids(GTNAMaterials.RedMud.getFluid(200))
                .outputFluids(GTMaterials.Water.getFluid(2000))
                .EUt(1920)
                .duration(2400)
                .blastFurnaceTemp(3500)
                .save(provider);

        GTNARecipeType.VACUUM_DRYING_RECIPES.recipeBuilder("platinum_front_pro")
                .inputFluids(GTNAMaterials.PlatinumFront, 4000)
                .outputItems(TagPrefix.dust, GTMaterials.Platinum, 64)
                .outputItems(TagPrefix.dust, GTMaterials.Nickel, 48)
                .outputItems(TagPrefix.dust, GTMaterials.Iridium, 32)
                .outputItems(TagPrefix.dust, GTMaterials.Osmium, 32)
                .outputItems(TagPrefix.dust, GTMaterials.Palladium, 32)
                .outputItems(TagPrefix.dust, GTMaterials.Cobalt, 32)
                .outputFluids(GTNAMaterials.RedMud.getFluid(200))
                .outputFluids(GTMaterials.Water.getFluid(2000))
                .EUt(30720)
                .duration(2400)
                .blastFurnaceTemp(5500)
                .save(provider);

        GTNARecipeType.VACUUM_DRYING_RECIPES.recipeBuilder("redstone_front_pro")
                .inputFluids(GTNAMaterials.RedstoneFront, 4000)
                .outputItems(TagPrefix.dust, GTMaterials.Redstone, 64)
                .outputItems(TagPrefix.dust, GTMaterials.Redstone, 64)
                .outputItems(TagPrefix.dust, GTMaterials.Manganese, 64)
                .outputItems(TagPrefix.dust, GTMaterials.Manganese, 64)
                .outputItems(TagPrefix.dust, GTMaterials.Yttrium, 32)
                .outputItems(TagPrefix.dust, GTMaterials.Ytterbium, 16)
                .outputFluids(GTNAMaterials.RedMud.getFluid(200))
                .outputFluids(GTMaterials.Water.getFluid(2000))
                .EUt(7680)
                .duration(2400)
                .blastFurnaceTemp(4500)
                .save(provider);

        GTNARecipeType.VACUUM_DRYING_RECIPES.recipeBuilder("almandine_front_pro")
                .inputFluids(GTNAMaterials.AlmandineFront, 4000)
                .outputItems(TagPrefix.dust, GTMaterials.Aluminium, 64)
                .outputItems(TagPrefix.dust, GTMaterials.Aluminium, 64)
                .outputItems(TagPrefix.dust, GTMaterials.Manganese, 64)
                .outputItems(TagPrefix.dust, GTMaterials.Manganese, 24)
                .outputItems(TagPrefix.dust, GTMaterials.Yttrium, 24)
                .outputItems(TagPrefix.dust, GTMaterials.Ytterbium, 16)
                .outputFluids(GTNAMaterials.RedMud.getFluid(200))
                .outputFluids(GTMaterials.Water.getFluid(2000))
                .EUt(7680)
                .duration(2400)
                .blastFurnaceTemp(5500)
                .save(provider);

        GTNARecipeType.VACUUM_DRYING_RECIPES.recipeBuilder("monazite_front_pro")
                .inputFluids(GTNAMaterials.MonaziteFront, 4000)
                .outputItems(TagPrefix.dust, GTMaterials.Erbium, 64)
                .outputItems(TagPrefix.dust, GTMaterials.Neodymium, 64)
                .outputItems(TagPrefix.dust, GTMaterials.Thorium, 48)
                .outputItems(TagPrefix.dust, GTMaterials.Lanthanum, 32)
                .outputItems(TagPrefix.dust, GTMaterials.Lutetium, 16)
                .outputItems(TagPrefix.dust, GTMaterials.Europium, 8)
                .outputFluids(GTNAMaterials.RedMud.getFluid(200))
                .outputFluids(GTMaterials.Water.getFluid(2000))
                .EUt(122880)
                .duration(2400)
                .blastFurnaceTemp(5500)
                .save(provider);

        GTNARecipeType.VACUUM_DRYING_RECIPES.recipeBuilder("spessartine_front_pro")
                .inputFluids(GTNAMaterials.SpessartineFront, 4000)
                .outputItems(TagPrefix.dust, GTMaterials.Manganese, 64)
                .outputItems(TagPrefix.dust, GTMaterials.Manganese, 64)
                .outputItems(TagPrefix.dust, GTMaterials.Aluminium, 64)
                .outputItems(TagPrefix.dust, GTMaterials.Aluminium, 32)
                .outputItems(TagPrefix.dust, GTMaterials.Palladium, 32)
                .outputItems(TagPrefix.dust, GTMaterials.Strontium, 16)
                .outputFluids(GTNAMaterials.RedMud.getFluid(200))
                .outputFluids(GTMaterials.Water.getFluid(2000))
                .EUt(30720)
                .duration(2400)
                .blastFurnaceTemp(5500)
                .save(provider);

        GTNARecipeType.VACUUM_DRYING_RECIPES.recipeBuilder("pentlandite_front_pro")
                .inputFluids(GTNAMaterials.PentlanditeFront, 4000)
                .outputItems(TagPrefix.dust, GTMaterials.Iron, 64)
                .outputItems(TagPrefix.dust, GTMaterials.Iron, 64)
                .outputItems(TagPrefix.dust, GTMaterials.Nickel, 64)
                .outputItems(TagPrefix.dust, GTMaterials.Nickel, 64)
                .outputItems(TagPrefix.dust, GTMaterials.Bismuth, 64)
                .outputItems(TagPrefix.dust, GTMaterials.Ruthenium, 48)
                .outputFluids(GTNAMaterials.RedMud.getFluid(200))
                .outputFluids(GTMaterials.Water.getFluid(2000))
                .EUt(30720)
                .duration(2400)
                .blastFurnaceTemp(5500)
                .save(provider);

        GTNARecipeType.VACUUM_DRYING_RECIPES.recipeBuilder("enriched_naquadah_front_pro")
                .inputFluids(GTNAMaterials.EnrichedNaquadahFront, 4000)
                .outputItems(TagPrefix.dust, GTMaterials.NaquadahEnriched, 64)
                .outputItems(TagPrefix.dust, GTMaterials.NaquadahEnriched, 64)
                .outputItems(TagPrefix.dust, GTMaterials.Naquadah, 64)
                .outputItems(TagPrefix.dust, GTMaterials.Naquadah, 32)
                .outputItems(TagPrefix.dust, GTMaterials.Naquadria, 64)
                .outputItems(TagPrefix.dust, GTMaterials.Trinium, 32)
                .outputFluids(GTNAMaterials.RedMud.getFluid(200))
                .outputFluids(GTMaterials.Water.getFluid(2000))
                .EUt(491520)
                .duration(2400)
                .blastFurnaceTemp(9500)
                .save(provider);
    }

    /** {@code classified/Dehydrator.java}: the only recipe that uses just GTCEu resources. */
    private static void dehydrator(Consumer<FinishedRecipe> provider) {
        GTNARecipeType.DEHYDRATOR_RECIPES.recipeBuilder("salt_dust")
                .inputFluids(GTMaterials.SaltWater, 1000)
                .outputItems(TagPrefix.dust, GTMaterials.Salt, 2)
                .EUt(30)
                .duration(160)
                .save(provider);
    }

    /**
     * {@code processing/StoneDustProcess.java}'s neutralisation, kept 1:1: the drying furnace's
     * Red Mud output has a real consumer instead of piling up as a dead end.
     */
    private static void redMud(Consumer<FinishedRecipe> provider) {
        GTRecipeTypes.MIXER_RECIPES.recipeBuilder("neutralised_red_mud")
                .inputFluids(GTNAMaterials.RedMud, 1000)
                .inputFluids(GTMaterials.HydrochloricAcid, 4000)
                .outputFluids(GTNAMaterials.NeutralisedRedMud.getFluid(2000))
                .duration(100)
                .EUt(GTValues.VA[GTValues.MV])
                .save(provider);
    }

    /** {@code classified/Assembler.java}: the five new casings/cells, 1:1. */
    private static void casings(Consumer<FinishedRecipe> provider) {
        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("hastelloy_n_75_casing")
                .inputItems(GTMachines.HULL[GTValues.EV].asStack().getItem())
                .inputItems(TagPrefix.plateDouble, GTMaterials.Nichrome, 4)
                .inputItems(TagPrefix.plateDouble, GTMaterials.WatertightSteel, 4)
                .inputItems(TagPrefix.rodLong, GTMaterials.HSSG, 2)
                .inputItems(TagPrefix.bolt, GTNAMaterials.HastelloyN75, 16)
                .inputFluids(GTMaterials.StainlessSteel, 1152)
                .outputItems(GTNABlocks.HASTELLOY_N_75_CASING.asItem())
                .EUt(7680)
                .duration(500)
                .save(provider);

        // GTOCore lists the gear twice (3 + 6) in this recipe; kept as-is.
        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("hastelloy_n_75_gearbox")
                .inputItems(TagPrefix.frameGt, GTMaterials.HSSG)
                .inputItems(TagPrefix.plateDouble, GTMaterials.HSSG, 4)
                .inputItems(TagPrefix.gear, GTNAMaterials.HastelloyN75, 3)
                .inputItems(TagPrefix.gear, GTNAMaterials.HastelloyN75, 6)
                .inputItems(TagPrefix.bolt, GTMaterials.TungstenCarbide, 16)
                .inputFluids(GTMaterials.HastelloyX, 576)
                .outputItems(GTNABlocks.HASTELLOY_N_75_GEARBOX.asItem())
                .EUt(30720)
                .duration(280)
                .save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("hastelloy_n_75_pipe")
                .inputItems(TagPrefix.frameGt, GTMaterials.WatertightSteel)
                .inputItems(TagPrefix.plateDouble, GTNAMaterials.HastelloyN75, 6)
                .inputItems(TagPrefix.pipeSmallFluid, GTMaterials.TungstenSteel, 4)
                .inputFluids(GTMaterials.SolderingAlloy, 1152)
                .outputItems(GTNABlocks.HASTELLOY_N_75_PIPE.asItem())
                .EUt(1920)
                .duration(480)
                .save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("flotation_cell")
                .inputItems(TagPrefix.plate, GTNAMaterials.HastelloyN75, 7)
                .inputItems(GTBlocks.CASING_GRATE.asItem())
                .inputItems(GTItems.ELECTRIC_PUMP_IV)
                .outputItems(GTNABlocks.FLOTATION_CELL.asItem())
                .EUt(7680)
                .duration(400)
                .save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("red_steel_casing")
                .circuitMeta(6)
                .inputItems(TagPrefix.frameGt, GTMaterials.HSLASteel)
                .inputItems(TagPrefix.plate, GTMaterials.TitaniumCarbide, 2)
                .inputItems(TagPrefix.plateDouble, GTMaterials.RedSteel, 4)
                .outputItems(GTNABlocks.RED_STEEL_CASING.asItem())
                .EUt(480)
                .duration(600)
                .save(provider);
    }

    /**
     * {@code classified/AssemblyLine.java:1794}: the flotation controller only uses GTCEu/GTNA
     * resources (Hastelloy-N75 and Stellite are ported 1:1 from GTO), so it is ported faithfully.
     * The Vacuum Drying Furnace uses the original GTO Assembler recipe after its IV Dehydrator is
     * registered above.
     */
    private static void controllers(Consumer<FinishedRecipe> provider) {
        // GTO MachineRecipe.java:157: port the IV single-block prerequisite first.
        registerMachineRecipe(provider, GTNAMachines3.DEHYDRATOR,
                "WCW", "AMA", "PRP", 'M', HULL, 'P', PLATE, 'C', CIRCUIT,
                'W', WIRE_QUAD, 'R', ROBOT_ARM, 'A', CABLE_QUAD);

        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder("industrial_flotation_cell")
                .inputItems(GTMachines.HULL[GTValues.LuV].asStack().getItem())
                .inputItems(GTNABlocks.FLOTATION_CELL.asItem(), 2)
                .inputItems(TagPrefix.plateDouble, GTNAMaterials.HastelloyN75, 8)
                .inputItems(TagPrefix.plateDouble, GTMaterials.IncoloyMA956, 8)
                .inputItems(CustomTags.LuV_CIRCUITS, 8)
                .inputItems(GTItems.ELECTRIC_PUMP_LuV, 2)
                .inputItems(TagPrefix.gear, GTNAMaterials.Stellite, 4)
                .inputItems(TagPrefix.gearSmall, GTNAMaterials.HastelloyN75, 16)
                .inputItems(TagPrefix.foil, GTMaterials.TungstenSteel, 32)
                .inputItems(TagPrefix.cableGtQuadruple, GTMaterials.VanadiumGallium, 4)
                .inputFluids(GTMaterials.SolderingAlloy, 5760)
                .inputFluids(GTMaterials.Lubricant, 6000)
                .inputFluids(GTMaterials.WatertightSteel, 1152)
                .outputItems(GTNAMachines3.INDUSTRIAL_FLOTATION_CELL.asStack())
                .EUt(30720)
                .duration(2400)
                .stationResearch(b -> b
                        .researchStack(GTMachines.ORE_WASHER[GTValues.IV].asStack())
                        .CWUt(32)
                        .EUt(7680))
                .save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("vacuum_drying_furnace")
                .inputItems(TagPrefix.frameGt, GTMaterials.HSSE)
                .inputItems(GTNAMachines3.DEHYDRATOR[GTValues.IV].asStack().getItem(), 4)
                .inputItems(TagPrefix.plateDouble, GTMaterials.TitaniumTungstenCarbide, 2)
                .inputItems(CustomTags.IV_CIRCUITS, 4)
                .inputItems(GTItems.ELECTRIC_PISTON_IV, 2)
                .inputItems(TagPrefix.gear, GTMaterials.Iridium, 3)
                .inputItems(TagPrefix.gearSmall, GTMaterials.Titanium, 6)
                .inputItems(TagPrefix.screw, GTNAMaterials.Tantalloy61, 24)
                .inputFluids(GTMaterials.Platinum, 1152)
                .outputItems(GTNAMachines3.VACUUM_DRYING_FURNACE.asStack())
                .EUt(480)
                .duration(600)
                .save(provider);
    }
}
