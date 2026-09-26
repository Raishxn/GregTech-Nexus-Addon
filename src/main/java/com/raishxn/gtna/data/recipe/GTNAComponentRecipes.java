package com.raishxn.gtna.data.recipe;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.data.recipe.GTCraftingComponents;

import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import com.raishxn.gtna.common.data.GTNABlocks;
import com.raishxn.gtna.common.data.GTNAMachines3;
import com.raishxn.gtna.common.data.GTNAMaterials;
import com.raishxn.gtna.common.data.GTNARecipeType;

import java.util.function.Consumer;

/** GTOCore's LV–UV component batch recipes and matching casing production. */
public final class GTNAComponentRecipes {

    private GTNAComponentRecipes() {}

    public static void register(Consumer<FinishedRecipe> provider) {
        casing(provider, "lv", GTValues.LV, GTMaterials.Steel, GTNABlocks.COMPONENT_ASSEMBLY_CASING_LV.asItem(), 288);
        casing(provider, "mv", GTValues.MV, GTMaterials.Aluminium, GTNABlocks.COMPONENT_ASSEMBLY_CASING_MV.asItem(),
                432);
        casing(provider, "hv", GTValues.HV, GTMaterials.StainlessSteel,
                GTNABlocks.COMPONENT_ASSEMBLY_CASING_HV.asItem(), 576);
        casing(provider, "ev", GTValues.EV, GTMaterials.Titanium, GTNABlocks.COMPONENT_ASSEMBLY_CASING_EV.asItem(),
                720);
        casing(provider, "iv", GTValues.IV, GTMaterials.TungstenSteel, GTNABlocks.COMPONENT_ASSEMBLY_CASING_IV.asItem(),
                864);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNABlocks.MULTI_FUNCTIONAL_CASING.asItem())
                .pattern("ABA").pattern("CDC").pattern("ABA")
                .define('A', ChemicalHelper.get(TagPrefix.plateDouble, GTMaterials.Aluminium).getItem())
                .define('B', GTItems.ROBOT_ARM_MV.asItem())
                .define('C', GTItems.ELECTRIC_PISTON_MV.asItem())
                .define('D', GTBlocks.CASING_STEEL_SOLID.asItem())
                .unlockedBy("has_steel_casing", InventoryChangeTrigger.TriggerInstance
                        .hasItems(GTBlocks.CASING_STEEL_SOLID.asItem()))
                .save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("component_assembler_controller")
                .inputItems(GTNABlocks.COMPONENT_ASSEMBLY_CASING_LV.asItem(), 4)
                .inputItems(GTItems.CONVEYOR_MODULE_MV, 4)
                .inputItems(GTItems.ROBOT_ARM_MV, 8)
                .inputItems(GTItems.EMITTER_MV, 2)
                .inputItems(GTItems.SENSOR_MV, 2)
                .inputItems(CustomTags.MV_CIRCUITS, 4)
                .inputItems(TagPrefix.rod, GTMaterials.SterlingSilver, 8)
                .inputItems(TagPrefix.bolt, GTMaterials.TinAlloy, 32)
                .inputItems(TagPrefix.plate, GTMaterials.Steel, 16)
                .outputItems(GTNAMachines3.COMPONENT_ASSEMBLER.asStack())
                .EUt(120).duration(200).save(provider);

        batch(provider, "lv", GTValues.LV, CustomTags.LV_CIRCUITS,
                ChemicalHelper.get(TagPrefix.gem, GTMaterials.Quartzite),
                ChemicalHelper.get(TagPrefix.gem, GTMaterials.EnderPearl),
                GTMaterials.Steel, GTMaterials.Tin, GTMaterials.SteelMagnetic, GTMaterials.Copper,
                GTMaterials.Rubber, GTMaterials.Bronze, GTMaterials.Tin, GTMaterials.Brass,
                GTMaterials.ManganesePhosphide,
                GTItems.ELECTRIC_MOTOR_LV.asItem(), GTItems.CONVEYOR_MODULE_LV.asItem(),
                GTItems.ELECTRIC_PUMP_LV.asItem(), GTItems.ELECTRIC_PISTON_LV.asItem(),
                GTItems.ROBOT_ARM_LV.asItem(), GTItems.EMITTER_LV.asItem(),
                GTItems.SENSOR_LV.asItem(), GTItems.FIELD_GENERATOR_LV.asItem());
        batch(provider, "mv", GTValues.MV, CustomTags.MV_CIRCUITS,
                ChemicalHelper.get(TagPrefix.gemFlawless, GTMaterials.Emerald),
                ChemicalHelper.get(TagPrefix.gem, GTMaterials.EnderEye),
                GTMaterials.Aluminium, GTMaterials.Copper, GTMaterials.SteelMagnetic, GTMaterials.Cupronickel,
                GTMaterials.Rubber, GTMaterials.Steel, GTMaterials.Bronze, GTMaterials.Electrum,
                GTMaterials.MagnesiumDiboride,
                GTItems.ELECTRIC_MOTOR_MV.asItem(), GTItems.CONVEYOR_MODULE_MV.asItem(),
                GTItems.ELECTRIC_PUMP_MV.asItem(), GTItems.ELECTRIC_PISTON_MV.asItem(),
                GTItems.ROBOT_ARM_MV.asItem(), GTItems.EMITTER_MV.asItem(),
                GTItems.SENSOR_MV.asItem(), GTItems.FIELD_GENERATOR_MV.asItem());
        batch(provider, "hv", GTValues.HV, CustomTags.HV_CIRCUITS,
                ChemicalHelper.get(TagPrefix.gem, GTMaterials.EnderEye), new ItemStack(GTItems.QUANTUM_EYE.asItem()),
                GTMaterials.StainlessSteel, GTMaterials.Silver, GTMaterials.SteelMagnetic, GTMaterials.Electrum,
                GTMaterials.Rubber, GTMaterials.VanadiumSteel, GTMaterials.Steel, GTMaterials.Chromium,
                GTMaterials.MercuryBariumCalciumCuprate,
                GTItems.ELECTRIC_MOTOR_HV.asItem(), GTItems.CONVEYOR_MODULE_HV.asItem(),
                GTItems.ELECTRIC_PUMP_HV.asItem(), GTItems.ELECTRIC_PISTON_HV.asItem(),
                GTItems.ROBOT_ARM_HV.asItem(), GTItems.EMITTER_HV.asItem(),
                GTItems.SENSOR_HV.asItem(), GTItems.FIELD_GENERATOR_HV.asItem());
        batch(provider, "ev", GTValues.EV, CustomTags.EV_CIRCUITS,
                new ItemStack(GTItems.QUANTUM_EYE.asItem()), ChemicalHelper.get(TagPrefix.gem, GTMaterials.NetherStar),
                GTMaterials.Titanium, GTMaterials.Aluminium, GTMaterials.NeodymiumMagnetic, GTMaterials.Kanthal,
                GTMaterials.SiliconeRubber, GTMaterials.StainlessSteel, GTMaterials.Aluminium, GTMaterials.Platinum,
                GTMaterials.UraniumTriplatinum,
                GTItems.ELECTRIC_MOTOR_EV.asItem(), GTItems.CONVEYOR_MODULE_EV.asItem(),
                GTItems.ELECTRIC_PUMP_EV.asItem(), GTItems.ELECTRIC_PISTON_EV.asItem(),
                GTItems.ROBOT_ARM_EV.asItem(), GTItems.EMITTER_EV.asItem(),
                GTItems.SENSOR_EV.asItem(), GTItems.FIELD_GENERATOR_EV.asItem());
        batch(provider, "iv", GTValues.IV, CustomTags.IV_CIRCUITS,
                new ItemStack(GTItems.QUANTUM_STAR.asItem()), new ItemStack(GTItems.QUANTUM_STAR.asItem()),
                GTMaterials.TungstenSteel, GTMaterials.Tungsten, GTMaterials.NeodymiumMagnetic, GTMaterials.Graphene,
                GTMaterials.SiliconeRubber, GTMaterials.TungstenCarbide, GTMaterials.Titanium, GTMaterials.Iridium,
                GTMaterials.SamariumIronArsenicOxide,
                GTItems.ELECTRIC_MOTOR_IV.asItem(), GTItems.CONVEYOR_MODULE_IV.asItem(),
                GTItems.ELECTRIC_PUMP_IV.asItem(), GTItems.ELECTRIC_PISTON_IV.asItem(),
                GTItems.ROBOT_ARM_IV.asItem(), GTItems.EMITTER_IV.asItem(),
                GTItems.SENSOR_IV.asItem(), GTItems.FIELD_GENERATOR_IV.asItem());

        assemblyLineBatches(provider);
        assemblyLineZpmBatches(provider);
        assemblyLineUvBatches(provider);
    }

    /**
     * GTOCore {@code ComponentRecipes#assembly_line(LuV, ...)} component-assembly branch: the LuV batch
     * family unlocked by the Component Assembler's large extension (or run directly on the
     * {@code component_assembly_line}). Copied 1:1 with GTO's {@code fluidMultiplier = 2} for LuV;
     * the parallel {@code ASSEMBLY_LINE_RECIPES} branch of the same method is omitted because it
     * needs GTO-only item prefixes (motor enclosure, piston housing, ...) that GTNA does not port.
     */
    private static void assemblyLineBatches(Consumer<FinishedRecipe> provider) {
        var map = GTNARecipeType.COMPONENT_ASSEMBLY_RECIPES;
        var magnetic = ChemicalHelper.get(TagPrefix.rodLong, GTMaterials.SamariumMagnetic);
        var emitterGem = GTItems.QUANTUM_STAR.asItem();

        map.recipeBuilder("motor_luv").circuitMeta(1)
                .inputItems(magnetic, 12)
                .inputItems(TagPrefix.cableGtSingle, GTMaterials.NiobiumTitanium, 24)
                .inputFluids(GTMaterials.HSSS, 13680)
                .inputFluids(GTMaterials.Ruridit, 6912)
                .inputFluids(GTMaterials.SolderingAlloy, 3456)
                .inputFluids(GTMaterials.Lubricant, 6000)
                .inputFluids(GTMaterials.HSSE, 3456)
                .outputItems(GTItems.ELECTRIC_MOTOR_LuV, 16).addData("component_casing_tier", GTValues.LuV)
                .duration(2400).EUt(GTValues.VA[GTValues.LuV]).save(provider);

        map.recipeBuilder("conveyor_luv").circuitMeta(2)
                .inputItems(GTItems.ELECTRIC_MOTOR_LuV, 24)
                .inputItems(TagPrefix.cableGtSingle, GTMaterials.NiobiumTitanium, 24)
                .inputFluids(GTMaterials.HSSS, 9072)
                .inputFluids(GTMaterials.Ruridit, 3456)
                .inputFluids(GTMaterials.SolderingAlloy, 6000)
                .inputFluids(GTMaterials.HSSE, 3456)
                .inputFluids(GTMaterials.SiliconeRubber, 27648)
                .outputItems(GTItems.CONVEYOR_MODULE_LuV, 16).addData("component_casing_tier", GTValues.LuV)
                .duration(2400).EUt(GTValues.VA[GTValues.LuV]).save(provider);

        map.recipeBuilder("pump_luv").circuitMeta(3)
                .inputItems(GTItems.ELECTRIC_MOTOR_LuV, 12)
                .inputItems(TagPrefix.pipeSmallFluid, GTMaterials.NiobiumTitanium, 12)
                .inputItems(TagPrefix.cableGtSingle, GTMaterials.NiobiumTitanium, 24)
                .inputFluids(GTMaterials.HSSS, 12960)
                .inputFluids(GTMaterials.Ruridit, 3456)
                .inputFluids(GTMaterials.SolderingAlloy, 6000)
                .inputFluids(GTMaterials.HSSE, 3456)
                .inputFluids(GTMaterials.SiliconeRubber, 3456)
                .outputItems(GTItems.ELECTRIC_PUMP_LuV, 16).addData("component_casing_tier", GTValues.LuV)
                .duration(2400).EUt(GTValues.VA[GTValues.LuV]).save(provider);

        map.recipeBuilder("piston_luv").circuitMeta(4)
                .inputItems(GTItems.ELECTRIC_MOTOR_LuV, 12)
                .inputItems(TagPrefix.cableGtSingle, GTMaterials.NiobiumTitanium, 24)
                .inputFluids(GTMaterials.HSSS, 27648)
                .inputFluids(GTMaterials.Ruridit, 3456)
                .inputFluids(GTMaterials.SolderingAlloy, 6000)
                .inputFluids(GTMaterials.HSSE, 3456)
                .outputItems(GTItems.ELECTRIC_PISTON_LuV, 16).addData("component_casing_tier", GTValues.LuV)
                .duration(2400).EUt(GTValues.VA[GTValues.LuV]).save(provider);

        map.recipeBuilder("arm_luv").circuitMeta(5)
                .inputItems(GTItems.ELECTRIC_MOTOR_LuV, 24)
                .inputItems(GTItems.ELECTRIC_PISTON_LuV, 12)
                .inputItems(CustomTags.LuV_CIRCUITS, 12)
                .inputItems(CustomTags.IV_CIRCUITS, 24)
                .inputItems(CustomTags.HV_CIRCUITS, 36)
                .inputItems(TagPrefix.cableGtSingle, GTMaterials.NiobiumTitanium, 48)
                .inputFluids(GTMaterials.HSSS, 19008)
                .inputFluids(GTMaterials.Ruridit, 6912)
                .inputFluids(GTMaterials.SolderingAlloy, 6000)
                .inputFluids(GTMaterials.HSSE, 3456)
                .outputItems(GTItems.ROBOT_ARM_LuV, 16).addData("component_casing_tier", GTValues.LuV)
                .duration(2400).EUt(GTValues.VA[GTValues.LuV]).save(provider);

        map.recipeBuilder("emitter_luv").circuitMeta(6)
                .inputItems(GTItems.ELECTRIC_MOTOR_LuV, 12)
                .inputItems(emitterGem, 24)
                .inputItems(CustomTags.LuV_CIRCUITS, 24)
                .inputItems(TagPrefix.cableGtSingle, GTMaterials.NiobiumTitanium, 48)
                .inputFluids(GTMaterials.HSSS, 10368)
                .inputFluids(GTMaterials.Ruridit, 6912)
                .inputFluids(GTMaterials.HSSE, 3456)
                .inputFluids(GTMaterials.HSSS, 3456)
                .inputFluids(GTMaterials.Palladium, 27648)
                .inputFluids(GTMaterials.Ruthenium, 27648)
                .outputItems(GTItems.EMITTER_LuV, 16).addData("component_casing_tier", GTValues.LuV)
                .duration(2400).EUt(GTValues.VA[GTValues.LuV]).save(provider);

        map.recipeBuilder("sensor_luv").circuitMeta(7)
                .inputItems(GTItems.ELECTRIC_MOTOR_LuV, 12)
                .inputItems(emitterGem, 24)
                .inputItems(CustomTags.LuV_CIRCUITS, 24)
                .inputItems(TagPrefix.cableGtSingle, GTMaterials.NiobiumTitanium, 48)
                .inputFluids(GTMaterials.HSSS, 11232)
                .inputFluids(GTMaterials.Ruridit, 6912)
                .inputFluids(GTMaterials.HSSE, 3456)
                .inputFluids(GTMaterials.HSSS, 3456)
                .inputFluids(GTMaterials.Palladium, 27648)
                .inputFluids(GTMaterials.Ruthenium, 27648)
                .outputItems(GTItems.SENSOR_LuV, 16).addData("component_casing_tier", GTValues.LuV)
                .duration(2400).EUt(GTValues.VA[GTValues.LuV]).save(provider);

        map.recipeBuilder("field_generator_luv").circuitMeta(8)
                .inputItems(GTItems.EMITTER_LuV, 24)
                .inputItems(emitterGem, 12)
                .inputItems(CustomTags.LuV_CIRCUITS, 24)
                .inputItems(TagPrefix.cableGtSingle, GTMaterials.NiobiumTitanium, 48)
                .inputFluids(GTMaterials.HSSS, 13824)
                .inputFluids(GTMaterials.Ruridit, 6912)
                .inputFluids(GTMaterials.HSSE, 3456)
                .inputFluids(GTMaterials.HSSS, 3456)
                .inputFluids(GTMaterials.IndiumTinBariumTitaniumCuprate, 55296)
                .outputItems(GTItems.FIELD_GENERATOR_LuV, 16).addData("component_casing_tier", GTValues.LuV)
                .duration(2400).EUt(GTValues.VA[GTValues.LuV]).save(provider);
    }

    /**
     * GTOCore {@code ComponentRecipes#assembly_line(ZPM, ...)} component-assembly branch: the ZPM
     * batch family unlocked by the Component Assembler's large extension (or run directly on the
     * {@code component_assembly_line}). Copied 1:1 with GTO's {@code fluidMultiplier = 4} for ZPM
     * (L = 144); the parallel {@code ASSEMBLY_LINE_RECIPES} branch of the same method is omitted
     * because it needs GTO-only item prefixes (motor enclosure, piston housing, ...).
     */
    private static void assemblyLineZpmBatches(Consumer<FinishedRecipe> provider) {
        var map = GTNARecipeType.COMPONENT_ASSEMBLY_RECIPES;
        var magnetic = ChemicalHelper.get(TagPrefix.rodLong, GTMaterials.SamariumMagnetic);
        var emitterGem = GTItems.QUANTUM_STAR.asItem();

        map.recipeBuilder("motor_zpm").circuitMeta(1)
                .inputItems(magnetic, 12)
                .inputItems(TagPrefix.cableGtSingle, GTMaterials.VanadiumGallium, 24)
                .inputFluids(GTMaterials.Osmiridium, 13680)
                .inputFluids(GTMaterials.Europium, 13824)
                .inputFluids(GTMaterials.SolderingAlloy, 6912)
                .inputFluids(GTMaterials.Lubricant, 12000)
                .inputFluids(GTNAMaterials.MarM200Steel, 6912)
                .outputItems(GTItems.ELECTRIC_MOTOR_ZPM, 16).addData("component_casing_tier", GTValues.ZPM)
                .duration(2400).EUt(GTValues.VA[GTValues.ZPM]).save(provider);

        map.recipeBuilder("conveyor_zpm").circuitMeta(2)
                .inputItems(GTItems.ELECTRIC_MOTOR_ZPM, 24)
                .inputItems(TagPrefix.cableGtSingle, GTMaterials.VanadiumGallium, 24)
                .inputFluids(GTMaterials.Osmiridium, 9072)
                .inputFluids(GTMaterials.SolderingAlloy, 6912)
                .inputFluids(GTMaterials.Lubricant, 12000)
                .inputFluids(GTNAMaterials.MarM200Steel, 6912)
                .inputFluids(GTMaterials.StyreneButadieneRubber, 55296)
                .outputItems(GTItems.CONVEYOR_MODULE_ZPM, 16).addData("component_casing_tier", GTValues.ZPM)
                .duration(2400).EUt(GTValues.VA[GTValues.ZPM]).save(provider);

        map.recipeBuilder("pump_zpm").circuitMeta(3)
                .inputItems(GTItems.ELECTRIC_MOTOR_ZPM, 12)
                .inputItems(TagPrefix.pipeNormalFluid, GTMaterials.Polybenzimidazole, 12)
                .inputItems(TagPrefix.cableGtSingle, GTMaterials.VanadiumGallium, 24)
                .inputFluids(GTMaterials.Osmiridium, 12960)
                .inputFluids(GTMaterials.SolderingAlloy, 6912)
                .inputFluids(GTMaterials.Lubricant, 12000)
                .inputFluids(GTNAMaterials.MarM200Steel, 6912)
                .inputFluids(GTMaterials.StyreneButadieneRubber, 6912)
                .outputItems(GTItems.ELECTRIC_PUMP_ZPM, 16).addData("component_casing_tier", GTValues.ZPM)
                .duration(2400).EUt(GTValues.VA[GTValues.ZPM]).save(provider);

        map.recipeBuilder("piston_zpm").circuitMeta(4)
                .inputItems(GTItems.ELECTRIC_MOTOR_ZPM, 12)
                .inputItems(TagPrefix.cableGtSingle, GTMaterials.VanadiumGallium, 24)
                .inputFluids(GTMaterials.Osmiridium, 27648)
                .inputFluids(GTMaterials.SolderingAlloy, 6912)
                .inputFluids(GTMaterials.Lubricant, 12000)
                .inputFluids(GTNAMaterials.MarM200Steel, 6912)
                .outputItems(GTItems.ELECTRIC_PISTON_ZPM, 16).addData("component_casing_tier", GTValues.ZPM)
                .duration(2400).EUt(GTValues.VA[GTValues.ZPM]).save(provider);

        map.recipeBuilder("arm_zpm").circuitMeta(5)
                .inputItems(GTItems.ELECTRIC_MOTOR_ZPM, 24)
                .inputItems(GTItems.ELECTRIC_PISTON_ZPM, 12)
                .inputItems(CustomTags.ZPM_CIRCUITS, 12)
                .inputItems(CustomTags.LuV_CIRCUITS, 24)
                .inputItems(CustomTags.EV_CIRCUITS, 36)
                .inputItems(TagPrefix.cableGtSingle, GTMaterials.VanadiumGallium, 48)
                .inputFluids(GTMaterials.Osmiridium, 19008)
                .inputFluids(GTMaterials.SolderingAlloy, 13824)
                .inputFluids(GTMaterials.Lubricant, 12000)
                .inputFluids(GTNAMaterials.MarM200Steel, 6912)
                .outputItems(GTItems.ROBOT_ARM_ZPM, 16).addData("component_casing_tier", GTValues.ZPM)
                .duration(2400).EUt(GTValues.VA[GTValues.ZPM]).save(provider);

        map.recipeBuilder("emitter_zpm").circuitMeta(6)
                .inputItems(GTItems.ELECTRIC_MOTOR_ZPM, 12)
                .inputItems(emitterGem, 24)
                .inputItems(CustomTags.ZPM_CIRCUITS, 24)
                .inputItems(TagPrefix.cableGtSingle, GTMaterials.VanadiumGallium, 48)
                .inputFluids(GTMaterials.Osmiridium, 10368)
                .inputFluids(GTMaterials.SolderingAlloy, 13824)
                .inputFluids(GTNAMaterials.MarM200Steel, 6912)
                .inputFluids(GTMaterials.NaquadahAlloy, 3456)
                .inputFluids(GTMaterials.Trinium, 27648)
                .inputFluids(GTMaterials.Duranium, 27648)
                .outputItems(GTItems.EMITTER_ZPM, 16).addData("component_casing_tier", GTValues.ZPM)
                .duration(2400).EUt(GTValues.VA[GTValues.ZPM]).save(provider);

        map.recipeBuilder("sensor_zpm").circuitMeta(7)
                .inputItems(GTItems.ELECTRIC_MOTOR_ZPM, 12)
                .inputItems(emitterGem, 24)
                .inputItems(CustomTags.ZPM_CIRCUITS, 24)
                .inputItems(TagPrefix.cableGtSingle, GTMaterials.VanadiumGallium, 48)
                .inputFluids(GTMaterials.Osmiridium, 11232)
                .inputFluids(GTMaterials.SolderingAlloy, 13824)
                .inputFluids(GTNAMaterials.MarM200Steel, 6912)
                .inputFluids(GTMaterials.NaquadahAlloy, 3456)
                .inputFluids(GTMaterials.Trinium, 27648)
                .inputFluids(GTMaterials.Duranium, 27648)
                .outputItems(GTItems.SENSOR_ZPM, 16).addData("component_casing_tier", GTValues.ZPM)
                .duration(2400).EUt(GTValues.VA[GTValues.ZPM]).save(provider);

        map.recipeBuilder("field_generator_zpm").circuitMeta(8)
                .inputItems(GTItems.EMITTER_ZPM, 24)
                .inputItems(emitterGem, 12)
                .inputItems(CustomTags.ZPM_CIRCUITS, 24)
                .inputItems(TagPrefix.cableGtSingle, GTMaterials.VanadiumGallium, 48)
                .inputFluids(GTMaterials.Osmiridium, 13824)
                .inputFluids(GTMaterials.SolderingAlloy, 13824)
                .inputFluids(GTNAMaterials.MarM200Steel, 6912)
                .inputFluids(GTMaterials.NaquadahAlloy, 3456)
                .inputFluids(GTMaterials.UraniumRhodiumDinaquadide, 55296)
                .outputItems(GTItems.FIELD_GENERATOR_ZPM, 16).addData("component_casing_tier", GTValues.ZPM)
                .duration(2400).EUt(GTValues.VA[GTValues.ZPM]).save(provider);
    }

    /**
     * GTOCore {@code ComponentRecipes#assembly_line(UV, ...)} component-assembly branch: the UV
     * batch family and the top of the GTNA extension port. Copied 1:1 with GTO's
     * {@code fluidMultiplier = 4} for UV (L = 144), including the documented UV special case where
     * the motor's Americium input doubles to {@code L * 192} instead of the regular
     * {@code L * 24 * fluidMultiplier}; the parallel {@code ASSEMBLY_LINE_RECIPES} branch of the
     * same method is omitted because it needs GTO-only item prefixes.
     */
    private static void assemblyLineUvBatches(Consumer<FinishedRecipe> provider) {
        var map = GTNARecipeType.COMPONENT_ASSEMBLY_RECIPES;
        var magnetic = ChemicalHelper.get(TagPrefix.rodLong, GTMaterials.SamariumMagnetic);
        var emitterGem = GTItems.GRAVI_STAR.asItem();

        map.recipeBuilder("motor_uv").circuitMeta(1)
                .inputItems(magnetic, 12)
                .inputItems(TagPrefix.cableGtSingle, GTMaterials.YttriumBariumCuprate, 24)
                .inputFluids(GTMaterials.Tritanium, 13680)
                .inputFluids(GTMaterials.Americium, 27648)
                .inputFluids(GTMaterials.SolderingAlloy, 6912)
                .inputFluids(GTMaterials.Lubricant, 12000)
                .inputFluids(GTMaterials.Naquadria, 6912)
                .outputItems(GTItems.ELECTRIC_MOTOR_UV, 16).addData("component_casing_tier", GTValues.UV)
                .duration(2400).EUt(GTValues.VA[GTValues.UV]).save(provider);

        map.recipeBuilder("conveyor_uv").circuitMeta(2)
                .inputItems(GTItems.ELECTRIC_MOTOR_UV, 24)
                .inputItems(TagPrefix.cableGtSingle, GTMaterials.YttriumBariumCuprate, 24)
                .inputFluids(GTMaterials.Tritanium, 9072)
                .inputFluids(GTMaterials.SolderingAlloy, 6912)
                .inputFluids(GTMaterials.Lubricant, 12000)
                .inputFluids(GTMaterials.Naquadria, 6912)
                .inputFluids(GTMaterials.StyreneButadieneRubber, 55296)
                .outputItems(GTItems.CONVEYOR_MODULE_UV, 16).addData("component_casing_tier", GTValues.UV)
                .duration(2400).EUt(GTValues.VA[GTValues.UV]).save(provider);

        map.recipeBuilder("pump_uv").circuitMeta(3)
                .inputItems(GTItems.ELECTRIC_MOTOR_UV, 12)
                .inputItems(TagPrefix.pipeLargeFluid, GTMaterials.Naquadah, 12)
                .inputItems(TagPrefix.cableGtSingle, GTMaterials.YttriumBariumCuprate, 24)
                .inputFluids(GTMaterials.Tritanium, 12960)
                .inputFluids(GTMaterials.SolderingAlloy, 6912)
                .inputFluids(GTMaterials.Lubricant, 12000)
                .inputFluids(GTMaterials.Naquadria, 6912)
                .inputFluids(GTMaterials.StyreneButadieneRubber, 6912)
                .outputItems(GTItems.ELECTRIC_PUMP_UV, 16).addData("component_casing_tier", GTValues.UV)
                .duration(2400).EUt(GTValues.VA[GTValues.UV]).save(provider);

        map.recipeBuilder("piston_uv").circuitMeta(4)
                .inputItems(GTItems.ELECTRIC_MOTOR_UV, 12)
                .inputItems(TagPrefix.cableGtSingle, GTMaterials.YttriumBariumCuprate, 24)
                .inputFluids(GTMaterials.Tritanium, 27648)
                .inputFluids(GTMaterials.SolderingAlloy, 6912)
                .inputFluids(GTMaterials.Lubricant, 12000)
                .inputFluids(GTMaterials.Naquadria, 6912)
                .outputItems(GTItems.ELECTRIC_PISTON_UV, 16).addData("component_casing_tier", GTValues.UV)
                .duration(2400).EUt(GTValues.VA[GTValues.UV]).save(provider);

        map.recipeBuilder("arm_uv").circuitMeta(5)
                .inputItems(GTItems.ELECTRIC_MOTOR_UV, 24)
                .inputItems(GTItems.ELECTRIC_PISTON_UV, 12)
                .inputItems(CustomTags.UV_CIRCUITS, 12)
                .inputItems(CustomTags.ZPM_CIRCUITS, 24)
                .inputItems(CustomTags.IV_CIRCUITS, 36)
                .inputItems(TagPrefix.cableGtSingle, GTMaterials.YttriumBariumCuprate, 48)
                .inputFluids(GTMaterials.Tritanium, 19008)
                .inputFluids(GTMaterials.SolderingAlloy, 13824)
                .inputFluids(GTMaterials.Lubricant, 12000)
                .inputFluids(GTMaterials.Naquadria, 6912)
                .outputItems(GTItems.ROBOT_ARM_UV, 16).addData("component_casing_tier", GTValues.UV)
                .duration(2400).EUt(GTValues.VA[GTValues.UV]).save(provider);

        map.recipeBuilder("emitter_uv").circuitMeta(6)
                .inputItems(GTItems.ELECTRIC_MOTOR_UV, 12)
                .inputItems(emitterGem, 24)
                .inputItems(CustomTags.UV_CIRCUITS, 24)
                .inputItems(TagPrefix.cableGtSingle, GTMaterials.YttriumBariumCuprate, 48)
                .inputFluids(GTMaterials.Tritanium, 10368)
                .inputFluids(GTMaterials.SolderingAlloy, 13824)
                .inputFluids(GTMaterials.Naquadria, 6912)
                .inputFluids(GTMaterials.Tritanium, 3456)
                .inputFluids(GTMaterials.Naquadah, 27648)
                .inputFluids(GTMaterials.Tritanium, 27648)
                .outputItems(GTItems.EMITTER_UV, 16).addData("component_casing_tier", GTValues.UV)
                .duration(2400).EUt(GTValues.VA[GTValues.UV]).save(provider);

        map.recipeBuilder("sensor_uv").circuitMeta(7)
                .inputItems(GTItems.ELECTRIC_MOTOR_UV, 12)
                .inputItems(emitterGem, 24)
                .inputItems(CustomTags.UV_CIRCUITS, 24)
                .inputItems(TagPrefix.cableGtSingle, GTMaterials.YttriumBariumCuprate, 48)
                .inputFluids(GTMaterials.Tritanium, 11232)
                .inputFluids(GTMaterials.SolderingAlloy, 13824)
                .inputFluids(GTMaterials.Naquadria, 6912)
                .inputFluids(GTMaterials.Tritanium, 3456)
                .inputFluids(GTMaterials.Naquadah, 27648)
                .inputFluids(GTMaterials.Tritanium, 27648)
                .outputItems(GTItems.SENSOR_UV, 16).addData("component_casing_tier", GTValues.UV)
                .duration(2400).EUt(GTValues.VA[GTValues.UV]).save(provider);

        map.recipeBuilder("field_generator_uv").circuitMeta(8)
                .inputItems(GTItems.EMITTER_UV, 24)
                .inputItems(emitterGem, 12)
                .inputItems(CustomTags.UV_CIRCUITS, 24)
                .inputItems(TagPrefix.cableGtSingle, GTMaterials.YttriumBariumCuprate, 48)
                .inputFluids(GTMaterials.Tritanium, 13824)
                .inputFluids(GTMaterials.SolderingAlloy, 13824)
                .inputFluids(GTMaterials.Naquadria, 6912)
                .inputFluids(GTMaterials.Tritanium, 3456)
                .inputFluids(GTMaterials.EnrichedNaquadahTriniumEuropiumDuranide, 55296)
                .outputItems(GTItems.FIELD_GENERATOR_UV, 16).addData("component_casing_tier", GTValues.UV)
                .duration(2400).EUt(GTValues.VA[GTValues.UV]).save(provider);
    }

    private static void casing(Consumer<FinishedRecipe> provider, String name, int tier, Material material,
                               Item output, int solder) {
        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("component_assembly_casing_" + name)
                .inputItems(TagPrefix.frameGt, material)
                .inputItems(TagPrefix.plateDouble, material, 16)
                .inputItems(component("field_generator", tier), 2)
                .inputItems(component("electric_pump", tier), 4)
                .inputItems(component("robot_arm", tier), 4)
                .inputItems(component("sensor", tier), 4)
                .inputItems(component("conveyor_module", tier), 6)
                .inputItems(TagPrefix.gear, material, 4)
                .inputFluids(GTMaterials.SolderingAlloy, solder)
                .outputItems(output)
                .EUt(GTValues.VA[tier]).duration(320).save(provider);
    }

    private static Item component(String kind, int tier) {
        Object value = switch (kind) {
            case "field_generator" -> GTCraftingComponents.FIELD_GENERATOR.get(tier);
            case "electric_pump" -> GTCraftingComponents.PUMP.get(tier);
            case "robot_arm" -> GTCraftingComponents.ROBOT_ARM.get(tier);
            case "sensor" -> GTCraftingComponents.SENSOR.get(tier);
            default -> GTCraftingComponents.CONVEYOR.get(tier);
        };
        return ((ItemStack) value).getItem();
    }

    private static void batch(Consumer<FinishedRecipe> provider, String name, int tier, TagKey<Item> circuit,
                              ItemStack emitterGem, ItemStack fieldGem, Material body, Material cable,
                              Material magnetic, Material wire, Material rubber, Material pipe, Material rotor,
                              Material sensorRod, Material fieldWire, Item motor, Item conveyor, Item pump,
                              Item piston, Item arm, Item emitter, Item sensor, Item field) {
        var map = GTNARecipeType.COMPONENT_ASSEMBLY_RECIPES;
        map.recipeBuilder("motor_" + name).circuitMeta(1)
                .inputItems(TagPrefix.rod, magnetic, 12).inputItems(TagPrefix.wireGtDouble, wire, 48)
                .inputItems(TagPrefix.cableGtSingle, cable, 24).inputFluids(body, 5760)
                .outputItems(motor, 16).addData("component_casing_tier", tier)
                .duration(400).EUt(GTValues.VA[tier]).save(provider);
        map.recipeBuilder("conveyor_" + name).circuitMeta(2)
                .inputItems(motor, 24).inputItems(TagPrefix.cableGtSingle, cable, 24)
                .inputFluids(body, 3456).inputFluids(rubber, 10368)
                .outputItems(conveyor, 16).addData("component_casing_tier", tier)
                .duration(400).EUt(GTValues.VA[tier]).save(provider);
        map.recipeBuilder("pump_" + name).circuitMeta(3)
                .inputItems(motor, 12).inputItems(TagPrefix.cableGtSingle, cable, 24)
                .inputFluids(body, 4320).inputFluids(pipe, 5184)
                .inputFluids(rotor, 7344).inputFluids(rubber, 864)
                .outputItems(pump, 16).addData("component_casing_tier", tier)
                .duration(400).EUt(GTValues.VA[tier]).save(provider);
        map.recipeBuilder("piston_" + name).circuitMeta(4)
                .inputItems(motor, 12).inputItems(TagPrefix.cableGtSingle, cable, 24)
                .inputFluids(body, 17280)
                .outputItems(piston, 16).addData("component_casing_tier", tier)
                .duration(400).EUt(GTValues.VA[tier]).save(provider);
        map.recipeBuilder("arm_" + name).circuitMeta(5)
                .inputItems(motor, 12).inputItems(piston, 12)
                .inputItems(TagPrefix.cableGtSingle, cable, 36).inputItems(circuit, 12)
                .inputFluids(body, 3456)
                .outputItems(arm, 16).addData("component_casing_tier", tier)
                .duration(400).EUt(GTValues.VA[tier]).save(provider);
        map.recipeBuilder("emitter_" + name).circuitMeta(6)
                .inputItems(emitterGem.getItem(), 12).inputItems(TagPrefix.cableGtSingle, cable, 24)
                .inputItems(circuit, 24).inputFluids(body, 6912).inputFluids(sensorRod, 1728)
                .outputItems(emitter, 16).addData("component_casing_tier", tier)
                .duration(400).EUt(GTValues.VA[tier]).save(provider);
        map.recipeBuilder("sensor_" + name).circuitMeta(7)
                .inputItems(emitterGem.getItem(), 12).inputItems(TagPrefix.cableGtSingle, cable, 12)
                .inputItems(circuit, 24).inputFluids(body, 7776).inputFluids(sensorRod, 864)
                .outputItems(sensor, 16).addData("component_casing_tier", tier)
                .duration(400).EUt(GTValues.VA[tier]).save(provider);
        map.recipeBuilder("field_generator_" + name).circuitMeta(8)
                .inputItems(emitter, 12).inputItems(fieldGem.getItem(), 12)
                .inputItems(TagPrefix.wireGtQuadruple, fieldWire, 48).inputItems(circuit, 24)
                .inputFluids(body, 13824)
                .outputItems(field, 16).addData("component_casing_tier", tier)
                .duration(400).EUt(GTValues.VA[tier]).save(provider);
    }
}
