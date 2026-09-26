package com.raishxn.gtna.common.data;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.block.ICoilType;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.recipe.GTRecipeSerializer;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.utils.CycleItemStackHandler;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;

import com.raishxn.gtna.GTNACORE;

import static com.lowdragmc.lowdraglib.gui.texture.ProgressTexture.FillDirection.LEFT_TO_RIGHT;

public class GTNARecipeType {

    public static final String HYDRAULIC = "hydraulic";
    public static final GTRecipeType HYDRAULIC_MANUFACTURING = register("hydraulic_manufacturing", HYDRAULIC)
            .setMaxIOSize(9, 2, 3, 1)
            .setEUIO(IO.IN)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.BATH);

    public static final String SUPERHEATER_NAME = "superheater";
    public static final GTRecipeType SUPERHEATER_RECIPES = register("super_heater", SUPERHEATER_NAME)
            .setMaxIOSize(1, 0, 0, 1)
            .setEUIO(IO.IN)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARC_FURNACE, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.ARC);

    public static final String WOODCUTTER = "woodcutter";
    public static final GTRecipeType WOODCUTTER_RECIPES = register("woodcutter", WOODCUTTER)
            .setMaxIOSize(1, 6, 0, 0)
            .setEUIO(IO.IN)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.SAW_TOOL);
    // No final do arquivo, antes de init()
    public static final String INFERNAL_COKE = "infernal_coke";
    public static final GTRecipeType INFERNAL_COKE_RECIPES = register("infernal_coke", INFERNAL_COKE)
            .setMaxIOSize(1, 1, 0, 1)
            .setEUIO(IO.IN)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.FURNACE);

    public static final String LAVA_MAKER = "lava_maker";
    public static final GTRecipeType LAVA_MAKER_RECIPES = register("lava_maker", LAVA_MAKER)
            .setMaxIOSize(1, 0, 0, 1)
            .setEUIO(IO.IN)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.FURNACE);

    /**
     * GTNL {@code CactusWonderFakeRecipes} port: one carbon fuel -> one steam grade. GTNL only used
     * this map for JEI, so GTNA promotes it to a real recipe type the Cactus Wonder can run.
     */
    public static final String CACTUS_WONDER = "cactus_wonder";
    public static final GTRecipeType CACTUS_WONDER_RECIPES = register("cactus_wonder", CACTUS_WONDER)
            .setMaxIOSize(1, 0, 0, 1)
            .setEUIO(IO.IN)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.FURNACE);

    /**
     * GTNA ore-processing chain (macerate → wash → thermal → centrifuge). Registered as a real
     * {@link GTRecipeType} so the Steam Ore Processor module and the Integrated / Advanced Integrated
     * Ore Processors can run datapack recipes against it, in the spirit of GTLAdditions/GTLCore's ore
     * processors. IO sizes match GTLCore's {@code integrated_ore_processor}: 2 item in (ore + circuit),
     * 9 item out (product + byproducts), 1 fluid in (water / mercury / distilled water).
     */
    public static final String ORE_PROCESSING = "ore_processing";
    public static final GTRecipeType ORE_PROCESSING_RECIPES = register("ore_processing", ORE_PROCESSING)
            .setMaxIOSize(2, 9, 1, 0)
            .setEUIO(IO.IN)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.MACERATOR);

    /**
     * GTOCore {@code brick_kiln} recipe type (LGPLv3): fires bricks/ceramics from compressed clay +
     * coal. No EU (the kiln is a primitive no-energy multiblock). IO 3 item in / 1 item out /
     * 1 fluid in, matching GTOCore's {@code BRICK_FURNACE_RECIPES}.
     */
    public static final String BRICK_FURNACE = "brick_furnace";
    public static final GTRecipeType BRICK_FURNACE_RECIPES = register("brick_furnace", BRICK_FURNACE)
            .setMaxIOSize(3, 1, 1, 0)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.FURNACE);

    /**
     * GTOCore {@code liquefaction_furnace} recipe type (LGPLv3): melts an item into a fluid using a
     * coil machine's heat (1 item in / 1 fluid out, EU in). The recipe's {@code ebf_temp} is the coil
     * temperature requirement, shown like the EBF's.
     */
    public static final String LIQUEFACTION_FURNACE = "liquefaction_furnace";
    public static final GTRecipeType LIQUEFACTION_FURNACE_RECIPES = register("liquefaction_furnace",
            LIQUEFACTION_FURNACE)
            .setMaxIOSize(1, 0, 0, 1)
            .setEUIO(IO.IN)
            .setProgressBar(GuiTextures.PROGRESS_BAR_EXTRACT, LEFT_TO_RIGHT)
            .addDataInfo(data -> LocalizationUtils.format("gtceu.recipe.temperature",
                    FormattingUtil.formatTemperature(data.getInt("ebf_temp"))))
            .setSound(GTSoundEntries.ARC);

    /** GTOCore Fishing Ground: fish seed + bait to fish, with circuit controlled loot modes. */
    public static final GTRecipeType FISHING_GROUND_RECIPES = register("fishing_ground", "fishing_ground")
            .setMaxIOSize(2, 2, 0, 0)
            .setEUIO(IO.IN)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.MINER);

    public static final GTRecipeType EVAPORATION_RECIPES = register("evaporation", "evaporation")
            .setMaxIOSize(0, 0, 1, 1)
            .setEUIO(IO.IN)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW_MULTIPLE, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.MOTOR);

    /** GTOCore Greenhouse: seed/crop catalyst, optional fertilizer and water. */
    public static final GTRecipeType GREENHOUSE_RECIPES = register("greenhouse", "greenhouse")
            .setMaxIOSize(3, 1, 1, 0)
            .setEUIO(IO.IN)
            .setProgressBar(GuiTextures.PROGRESS_BAR_BATH, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.COOLING);

    /** GTOCore component batches. Recipe data records the minimum matching casing tier. */
    public static final GTRecipeType COMPONENT_ASSEMBLY_RECIPES = register("component_assembly", "component_assembly")
            .setMaxIOSize(9, 1, 9, 0)
            .setEUIO(IO.IN)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, LEFT_TO_RIGHT)
            .addDataInfo(data -> LocalizationUtils.format("gtna.recipe.component_assembly.tier",
                    com.gregtechceu.gtceu.api.GTValues.VN[data.getInt("component_casing_tier")]))
            .setSound(GTSoundEntries.ASSEMBLER);

    /**
     * GTOCore {@code atomization_condensation} (LGPLv3): the Cold Ice Freezer's auxiliary-module
     * recipe family. A fluid (or molten metal) plus an inert gas condenses into a dust, or a molten
     * metal condenses back into its liquid form. GTO's dedicated high-pressure gas fluids are not
     * ported, so the recipes use the regular gas at the original amount (documented in
     * {@code GTNAAtomizationRecipes}).
     */
    public static final GTRecipeType ATOMIZATION_CONDENSATION_RECIPES = register("atomization_condensation",
            "atomization_condensation")
            .setEUIO(IO.IN)
            .setMaxIOSize(2, 2, 3, 3)
            .setProgressBar(GuiTextures.PROGRESS_BAR_MACERATE, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.COOLING);

    /** The second GTOCore Large Greenhouse mode: saplings plus water, optionally fertilizer. */
    public static final GTRecipeType TREE_GROWTH_RECIPES = register("tree_growth_simulator", "tree_growth_simulator")
            .setMaxIOSize(3, 2, 1, 0)
            .setEUIO(IO.IN)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.COOLING);

    /**
     * GTOCore {@code isa_mill} (LGPLv3): wet grinding of ores/raw ores into {@code MILLED} products.
     * IO matches GTO (2 item in / 1 item out / 1 fluid in); the {@code grindball} data key selects
     * the required grinding-ball tier and is displayed in JEI like the original.
     */
    public static final String ISA_MILL = "isa_mill";
    public static final GTRecipeType ISA_MILL_RECIPES = register("isa_mill", ISA_MILL)
            .setMaxIOSize(2, 1, 1, 0)
            .setEUIO(IO.IN)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.MACERATOR)
            .addDataInfo(data -> LocalizationUtils.format("gtna.recipe.grindball",
                    LocalizationUtils.format(data.getInt(GTNARecipeDataKeys.GRINDBALL) == 2 ?
                            "material.gtceu.aluminium" : "material.gtceu.soapstone")));

    public static final String HIGH_PRESSURE_REACTOR = "high_pressure_reactor";
    public static final GTRecipeType HIGH_PRESSURE_REACTOR_RECIPES = register("high_pressure_reactor",
            HIGH_PRESSURE_REACTOR)
            .setMaxIOSize(0, 0, 2, 1)
            .setEUIO(IO.IN)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.ARC);

    public static final String SLAUGTHERHOUSE = "slaugterhouse";
    public static final GTRecipeType SLAUGHTERHOUSE_RECIPES = register("slaughterhouse", SLAUGTHERHOUSE)
            .setEUIO(IO.IN)
            .setMaxIOSize(1, 64, 0, 0)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.MACERATOR);

    public static final String ARTIFICIAL_STAR = "annihilate_generator";
    public static final GTRecipeType ARTIFICIAL_STAR_RECIPES = register("annihilate_generator", ARTIFICIAL_STAR)
            .setMaxIOSize(1, 1, 0, 0)
            .setEUIO(IO.OUT)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.ARC);

    public static final String COSMOS_SIMULATION = "cosmos_simulation";
    public static final GTRecipeType COSMOS_SIMULATION_RECIPES = register("cosmos_simulation", COSMOS_SIMULATION)
            .setMaxIOSize(1, 120, 1, 18)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.SCIENCE);

    /**
     * GTOCore {@code rocket_engine} (LGPLv3): the Rocket Large Turbine's generator family. Fluid in,
     * EU out only, exactly like GTO's {@code ROCKET_ENGINE_FUELS} (one 10 mB fluid input, JET_ENGINE
     * sound). GTO's {@code addFuelProperties} hook feeds a GTO-only jetpack item and is not ported.
     */
    public static final String ROCKET_ENGINE = "rocket_engine";
    public static final GTRecipeType ROCKET_ENGINE_FUELS = register("rocket_engine", ROCKET_ENGINE)
            .setEUIO(IO.OUT)
            .setMaxIOSize(0, 0, 1, 0)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.JET_ENGINE);

    /**
     * GTOCore {@code supercritical_steam_turbine} (LGPLv3): the non-mega Supercritical Steam
     * Turbine's generator family. IO matches GTO's {@code SUPERCRITICAL_STEAM_TURBINE_FUELS} one
     * fluid input / one fluid output with EU out only, the centrifuge slot overlay, the gas
     * collector progress bar and the turbine sound.
     *
     * <p>
     * GTO feeds this type with its own {@code SupercriticalSteam}; GTNA reuses
     * {@code DenseSupercriticalSteam} instead (see {@code GTNASupercriticalSteamTurbineRecipes}).
     */
    public static final String SUPERCRITICAL_STEAM_TURBINE = "supercritical_steam_turbine";
    public static final GTRecipeType SUPERCRITICAL_STEAM_TURBINE_FUELS = register("supercritical_steam_turbine",
            SUPERCRITICAL_STEAM_TURBINE)
            .setMaxIOSize(0, 0, 1, 1)
            .setEUIO(IO.OUT)
            .setSlotOverlay(false, true, true, GuiTextures.CENTRIFUGE_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_GAS_COLLECTOR, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.TURBINE);

    /**
     * GTOCore {@code flotating_beneficiation} (LGPLv3): the Industrial Flotation Cell's froth
     * flotation family. Two item inputs (reagent dust + MILLED ore) and one fluid input produce one
     * fluid output (the ore foam); IO matches GTO exactly.
     */
    public static final String FLOTATING_BENEFICIATION = "flotating_beneficiation";
    public static final GTRecipeType FLOTATING_BENEFICIATION_RECIPES = register("flotating_beneficiation",
            FLOTATING_BENEFICIATION)
            .setMaxIOSize(2, 0, 1, 1)
            .setEUIO(IO.IN)
            .setProgressBar(GuiTextures.PROGRESS_BAR_BATH, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.CHEMICAL);

    /**
     * GTOCore {@code vacuum_drying} (LGPLv3): the Vacuum Drying Furnace's main family. One fluid
     * input (an ore foam) dries into up to nine item outputs plus two fluid outputs. GTO feeds the
     * JEI temperature/coil info from its own {@code temperature} data key; GTNA reads the
     * {@code ebf_temp} key that {@code blastFurnaceTemp} writes, like GTCEu's blast recipes, so the
     * required coil is shown.
     */
    public static final String VACUUM_DRYING = "vacuum_drying";
    public static final GTRecipeType VACUUM_DRYING_RECIPES = register("vacuum_drying", VACUUM_DRYING)
            .setMaxIOSize(0, 9, 1, 2)
            .setEUIO(IO.IN)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.COOLING)
            .addDataInfo(data -> LocalizationUtils.format("gtceu.recipe.temperature",
                    FormattingUtil.formatTemperature(data.getInt("ebf_temp"))))
            .addDataInfo(data -> {
                int temp = data.getInt("ebf_temp");
                ICoilType requiredCoil = ICoilType.getMinRequiredType(temp);
                if (requiredCoil != null && !requiredCoil.getMaterial().isNull()) {
                    return LocalizationUtils.format("gtceu.recipe.coil.tier",
                            I18n.get(requiredCoil.getMaterial().getUnlocalizedName()));
                }
                return "";
            })
            .setUiBuilder((recipe, widgetGroup) -> {
                int temp = recipe.data.getInt("ebf_temp");
                java.util.List<java.util.List<ItemStack>> items = new java.util.ArrayList<>();
                items.add(GTCEuAPI.HEATING_COILS.entrySet().stream()
                        .filter(coil -> coil.getKey().getCoilTemperature() >= temp)
                        .map(coil -> new ItemStack(coil.getValue().get())).toList());
                widgetGroup.addWidget(new SlotWidget(new CycleItemStackHandler(items), 0,
                        widgetGroup.getSize().width - 25, widgetGroup.getSize().height - 32, false, false));
            });

    /**
     * GTOCore {@code dehydrator} (LGPLv3): the single-block Dehydrator's family, also run by the
     * Vacuum Drying Furnace's second mode. IO matches GTO (2 item in / 6 item out / 2 fluid in /
     * 2 fluid out).
     */
    public static final String DEHYDRATOR = "dehydrator";
    public static final GTRecipeType DEHYDRATOR_RECIPES = register("dehydrator", DEHYDRATOR)
            .setMaxIOSize(2, 6, 2, 2)
            .setEUIO(IO.IN)
            .setProgressBar(GuiTextures.PROGRESS_BAR_EXTRACT, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.ARC);

    public static GTRecipeType register(String name, String group, RecipeType<?>... proxyRecipes) {
        GTRecipeType recipeType = new GTRecipeType(GTNACORE.id(name), group, proxyRecipes);
        GTRegistries.register(BuiltInRegistries.RECIPE_TYPE, recipeType.registryName, recipeType);
        GTRegistries.register(BuiltInRegistries.RECIPE_SERIALIZER, recipeType.registryName, new GTRecipeSerializer());
        GTRegistries.RECIPE_TYPES.register(recipeType.registryName, recipeType);
        return recipeType;
    }

    public static void init() {}
}
