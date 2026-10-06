package com.raishxn.gtna.data.recipe;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.*;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.common.data.*;
import com.raishxn.gtna.config.GTNABalance;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

/** GTL 1.4.5.1 essence chain adapted to the GTNA EV miner and stock GTCEu materials. */
public final class VoidMinerEssenceRecipes {

    private VoidMinerEssenceRecipes() {}

    public static void register(Consumer<FinishedRecipe> provider) {
        // GTL converts bone blocks in its Block Conversion Room. GTNA uses a biomass bath
        // to bootstrap the same essence without introducing that unrelated machine.
        GTRecipeTypes.CHEMICAL_BATH_RECIPES.recipeBuilder("void_miner_essence")
                .inputItems(Items.BONE_BLOCK).inputFluids(GTMaterials.Biomass.getFluid(1000))
                .outputItems(GTNAItems.ESSENCE).duration(400).EUt(GTValues.VA[GTValues.HV]).save(provider);
        GTRecipeTypes.MIXER_RECIPES.recipeBuilder("essence_seed")
                .inputItems(ItemTags.create(ResourceLocation.parse("forge:seeds")), 16)
                .inputItems(GTNAItems.ESSENCE)
                .inputFluids(GTMaterials.DistilledWater.getFluid(1000))
                .inputFluids(GTMaterials.CarbonDioxide.getFluid(1000))
                .outputItems(GTNAItems.ESSENCE_SEED, 16).duration(400).EUt(120).save(provider);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNAMachines3.INCUBATOR.asStack().getItem())
                .pattern("ABA").pattern("CDC").pattern("ABA")
                .define('A', GTBlocks.PLASTCRETE.get()).define('B', GTItems.FIELD_GENERATOR_HV.get())
                .define('C', GTBlocks.FILTER_CASING.get()).define('D', GTNAMachines3.GREENHOUSE.asStack().getItem())
                .unlockedBy("has_greenhouse",
                        net.minecraft.advancements.critereon.InventoryChangeTrigger.TriggerInstance
                                .hasItems(GTNAMachines3.GREENHOUSE.asStack().getItem()))
                .save(provider, GTNACORE.id("incubator"));
        Material[] cables = { GTMaterials.Tin, GTMaterials.Copper, GTMaterials.Gold,
                GTMaterials.Aluminium, GTMaterials.Platinum };
        net.minecraft.world.item.Item[] sensors = { GTItems.SENSOR_LV.get(), GTItems.SENSOR_MV.get(),
                GTItems.SENSOR_HV.get(), GTItems.SENSOR_EV.get(), GTItems.SENSOR_IV.get() };
        for (int tier = GTValues.LV; tier <= GTValues.IV; tier++) {
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC,
                    GTNAMachines3.WORLD_DATA_SCANNER[tier].asStack().getItem())
                    .pattern("CDC").pattern("BAB").pattern("CDC")
                    .define('A', GTMachines.HULL[tier].asStack().getItem())
                    .define('B', ChemicalHelper.get(TagPrefix.cableGtSingle, cables[tier - 1]).getItem())
                    .define('C', sensors[tier - 1])
                    .define('D', ItemTags.create(ResourceLocation.parse("gtceu:circuits/" +
                            GTValues.VN[tier].toLowerCase(java.util.Locale.ROOT))))
                    .unlockedBy("has_hull", net.minecraft.advancements.critereon.InventoryChangeTrigger.TriggerInstance
                            .hasItems(GTMachines.HULL[tier].asStack().getItem()))
                    .save(provider, GTNACORE.id(GTValues.VN[tier].toLowerCase(java.util.Locale.ROOT) +
                            "_world_data_scanner"));
        }
        String[] worlds = { "overworld", "nether", "end" };
        Material[] stones = { GTMaterials.Stone, GTMaterials.Netherrack, GTMaterials.Endstone };
        Material[] airs = { GTMaterials.Air, GTMaterials.NetherAir, GTMaterials.EnderAir };
        String[] dimensions = { "minecraft:overworld", "minecraft:the_nether", "minecraft:the_end" };
        for (int world = 0; world < worlds.length; world++) {
            int amount = 1 << world;
            GTNARecipeType.WORLD_DATA_SCANNER_RECIPES.recipeBuilder(worlds[world] + "_data")
                    .inputItems(GTItems.TOOL_DATA_STICK, amount)
                    .inputItems(TagPrefix.dust, stones[world], 64).circuitMeta(1)
                    .inputFluids(GTMaterials.PCBCoolant.getFluid(100 * amount))
                    .inputFluids(airs[world].getFluid(64000))
                    .outputItems(GTNAItems.WORLD_DATA.get(worlds[world]), amount)
                    .dimension(ResourceLocation.parse(dimensions[world]))
                    .duration(4000).EUt(GTValues.VA[world + GTValues.LV]).save(provider);
        }
        Map<String, ItemStack> randomOutputs = new LinkedHashMap<>();
        for (var vein : GTNAVoidVeins.ALL) {
            var essence = GTNAItems.VEIN_ESSENCES.get(vein.essence());
            var incubator = GTNARecipeType.INCUBATOR_RECIPES.recipeBuilder(vein.essence())
                    .notConsumable(GTNAItems.WORLD_DATA.get(worlds[vein.world()]).asStack(16 << vein.world()))
                    .inputItems(GTNAItems.ESSENCE_SEED)
                    .inputFluids(GTMaterials.Biomass.getFluid(10000))
                    .inputFluids(GTMaterials.Milk.getFluid(10000))
                    .outputItems(essence, 64).duration(GTNABalance.getElectricVoidMiner().incubationDuration)
                    .EUt(GTValues.VA[GTValues.HV]);
            for (String sample : vein.samples()) {
                boolean oreTag = sample.contains("ores/");
                String id = oreTag ? sample.substring(sample.indexOf("ores/") + 5) :
                        sample.substring(sample.indexOf(':') + 1);
                ItemStack fallback = resource(id, 1);
                Ingredient ingredient = oreTag ? Ingredient.fromValues(Stream.of(
                        new Ingredient.TagValue(ItemTags.create(ResourceLocation.parse("forge:ores/" + id))),
                        new Ingredient.ItemValue(fallback))) : Ingredient.of(fallback);
                incubator.inputItems(ingredient, amount(sample));
            }
            incubator.save(provider);
            if (GTNAMachines.ELECTRIC_VOID_MINER != null) {
                var config = GTNABalance.getElectricVoidMiner();
                var mining = GTNARecipeType.ELECTRIC_VOID_MINING_RECIPES.recipeBuilder(vein.essence())
                        .circuitMeta(2).inputItems(essence)
                        .inputFluids(GTMaterials.DrillingFluid.getFluid(config.defaultDrillingFluidPerOperation))
                        .duration(config.baseDuration)
                        .EUt(GTValues.VA[vein.world() == 2 ? GTValues.ZPM : GTValues.LuV]);
                for (String output : vein.outputs()) {
                    String id = output.substring(output.indexOf(':') + 1)
                            .replaceFirst("^(netherrack_|endstone_)", "").replaceFirst("_ore$", "");
                    ItemStack stack = resource(id, Math.max(1, amount(output) / 10));
                    mining.outputItems(stack);
                    randomOutputs.putIfAbsent(id, stack);
                }
                mining.save(provider);
            }
        }
        if (GTNAMachines.ELECTRIC_VOID_MINER != null) {
            var config = GTNABalance.getElectricVoidMiner();
            GTRecipeBuilder random = GTNARecipeType.RANDOM_VOID_MINING_RECIPES.recipeBuilder("random_void_mining")
                    .circuitMeta(2)
                    .inputFluids(GTMaterials.DrillingFluid.getFluid(config.randomDrillingFluidPerOperation))
                    .duration(config.randomDuration).EUt(GTValues.VA[GTValues.ZPM]);
            for (ItemStack output : randomOutputs.values()) {
                random.chancedOutput(output.copyWithCount(1), 200, 20);
            }
            random.save(provider);
            // Keep EV useful without exposing the GTL End/high-tier pool before its power gate.
            starter(provider, "terrestrial_iron_copper", "iron_vein_essence", 1000, 1200,
                    ChemicalHelper.get(TagPrefix.rawOre, GTMaterials.Iron, 2),
                    ChemicalHelper.get(TagPrefix.rawOre, GTMaterials.Copper, 2));
            starter(provider, "terrestrial_tin_lead", "cassiterite_vein_essence", 1000, 1200,
                    ChemicalHelper.get(TagPrefix.rawOre, GTMaterials.Tin, 2),
                    ChemicalHelper.get(TagPrefix.rawOre, GTMaterials.Lead, 1));
            starter(provider, "terrestrial_gold_silver", "magnetite_vein_ow_essence", 1500, 1800,
                    ChemicalHelper.get(TagPrefix.rawOre, GTMaterials.Gold, 1),
                    ChemicalHelper.get(TagPrefix.rawOre, GTMaterials.Silver, 2));
            GTRecipeBuilder terrestrialRandom = GTNARecipeType.RANDOM_VOID_MINING_RECIPES
                    .recipeBuilder("terrestrial_random_void_mining")
                    .circuitMeta(1)
                    .inputFluids(GTMaterials.DrillingFluid.getFluid(config.randomDrillingFluidPerOperation))
                    .duration(config.randomDuration).EUt(config.baseEUt);
            for (Material material : new Material[] { GTMaterials.Iron, GTMaterials.Copper, GTMaterials.Tin,
                    GTMaterials.Lead, GTMaterials.Gold, GTMaterials.Silver }) {
                terrestrialRandom.chancedOutput(ChemicalHelper.get(TagPrefix.rawOre, material, 1), 2500, 20);
            }
            terrestrialRandom.save(provider);
        }
    }

    private static void starter(Consumer<FinishedRecipe> provider, String id, String essence,
                                int fluid, int duration, ItemStack... outputs) {
        var builder = GTNARecipeType.ELECTRIC_VOID_MINING_RECIPES.recipeBuilder(id)
                .circuitMeta(1).inputItems(GTNAItems.VEIN_ESSENCES.get(essence))
                .inputFluids(GTMaterials.DrillingFluid.getFluid(fluid))
                .duration(GTNABalance.getElectricVoidMiner().baseDuration).EUt(GTValues.VA[GTValues.EV]);
        for (ItemStack output : outputs) builder.outputItems(output);
        builder.save(provider);
    }

    public static void planet(Consumer<FinishedRecipe> provider, int index, net.minecraft.world.item.Item stone,
                              int fluid, int duration, ItemStack... outputs) {
        String planet = GTNAItems.PLANET_DATA_CHIP_PLANETS[index];
        var chip = GTNAItems.PLANET_DATA_CHIPS[index];
        var essence = GTNAItems.VEIN_ESSENCES.get(planet + "_vein_essence");
        GTNARecipeType.WORLD_DATA_SCANNER_RECIPES.recipeBuilder("planet_data_chip_" + planet)
                .inputItems(GTItems.TOOL_DATA_STICK).notConsumable(stone).circuitMeta(1)
                .inputFluids(GTMaterials.PCBCoolant.getFluid(1000))
                .inputFluids(GTMaterials.Air.getFluid(64000))
                .outputItems(chip).dimension(ResourceLocation.parse("ad_astra:" + planet))
                .duration(4000).EUt(GTValues.VA[GTValues.EV]).save(provider);
        var incubation = GTNARecipeType.INCUBATOR_RECIPES.recipeBuilder(planet + "_vein_essence")
                .notConsumable(chip).inputItems(GTNAItems.ESSENCE_SEED)
                .inputFluids(GTMaterials.Biomass.getFluid(10000))
                .inputFluids(GTMaterials.Milk.getFluid(10000))
                .outputItems(essence, 64).duration(GTNABalance.getElectricVoidMiner().incubationDuration)
                .EUt(GTValues.VA[GTValues.HV]);
        for (ItemStack output : outputs) incubation.inputItems(output);
        incubation.save(provider);
        var mining = GTNARecipeType.ELECTRIC_VOID_MINING_RECIPES.recipeBuilder("ad_astra_" + planet + "_program")
                .inputItems(essence).inputFluids(GTMaterials.DrillingFluid.getFluid(fluid))
                .duration(GTNABalance.getElectricVoidMiner().baseDuration).EUt(GTValues.VA[GTValues.EV]);
        for (ItemStack output : outputs) mining.outputItems(output);
        mining.save(provider);
    }

    private static int amount(String stack) {
        return stack.contains("x ") ? Integer.parseInt(stack.substring(0, stack.indexOf("x "))) : 1;
    }

    /** Fork-only ore materials map to their stock GTCEu elements; non-ore elements use dust. */
    private static ItemStack resource(String name, int count) {
        if (name.equals("ancient_debris")) return new ItemStack(Items.ANCIENT_DEBRIS, count);
        name = switch (name) {
            case "zircon" -> "zirconium";
            case "celestine" -> "strontium";
            case "trinium_compound" -> "trinium";
            default -> name;
        };
        Material material = GTCEuAPI.materialManager.getMaterial(name);
        if (material == null) throw new IllegalArgumentException("Missing void vein material: " + name);
        ItemStack stack = ChemicalHelper.get(TagPrefix.rawOre, material, count);
        if (stack.isEmpty()) stack = ChemicalHelper.get(TagPrefix.dust, material, count);
        if (stack.isEmpty()) throw new IllegalArgumentException("Missing void vein resource: " + name);
        return stack;
    }
}
