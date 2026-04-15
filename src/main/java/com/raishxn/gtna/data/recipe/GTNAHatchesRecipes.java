package com.raishxn.gtna.data.recipe;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.level.ItemLike;

import com.raishxn.gtna.common.data.GTNAMachines2;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;

public class GTNAHatchesRecipes {

    public static void register(Consumer<FinishedRecipe> provider) {
        // --- Thread Hatch ZPM ---
        // Segue o padrão explícito do GTNAMachineRecipes
        if (GTNAMachines2.THREAD_HATCHES[ZPM] != null) {
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNAMachines2.THREAD_HATCHES[ZPM].asStack().getItem())
                    .pattern("ABA")
                    .pattern("CDC")
                    .pattern("EFE")
                    .define('A', CustomTags.ZPM_CIRCUITS)
                    .define('B', GTItems.ROBOT_ARM_ZPM.asStack().getItem())
                    .define('C', GTItems.CONVEYOR_MODULE_ZPM.asStack().getItem())
                    .define('D', GTItems.FIELD_GENERATOR_ZPM.asStack().getItem())
                    .define('E',
                            ChemicalHelper.get(TagPrefix.cableGtQuadruple, GTMaterials.UraniumRhodiumDinaquadide)
                                    .getItem())
                    .define('F', GTMachines.HULL[ZPM].asStack().getItem())
                    .unlockedBy("has_hull_zpm",
                            InventoryChangeTrigger.TriggerInstance.hasItems(GTMachines.HULL[ZPM].asStack().getItem()))
                    .save(provider);
        }

        // --- Thread Hatch UV ---
        if (GTNAMachines2.THREAD_HATCHES[UV] != null) {
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNAMachines2.THREAD_HATCHES[UV].asStack().getItem())
                    .pattern("ABA")
                    .pattern("CDC")
                    .pattern("EFE")
                    .define('A', CustomTags.UV_CIRCUITS)
                    .define('B', GTItems.ROBOT_ARM_UV.asStack().getItem())
                    .define('C', GTItems.CONVEYOR_MODULE_UV.asStack().getItem())
                    .define('D', GTItems.FIELD_GENERATOR_UV.asStack().getItem())
                    .define('E',
                            ChemicalHelper.get(TagPrefix.cableGtQuadruple,
                                    GTMaterials.EnrichedNaquadahTriniumEuropiumDuranide).getItem())
                    .define('F', GTMachines.HULL[UV].asStack().getItem())
                    .unlockedBy("has_hull_uv",
                            InventoryChangeTrigger.TriggerInstance.hasItems(GTMachines.HULL[UV].asStack().getItem()))
                    .save(provider);
        }
        createAccelerateRecipe(provider, LV, GTItems.SENSOR_LV, GTItems.FIELD_GENERATOR_LV);
        createAccelerateRecipe(provider, MV, GTItems.SENSOR_MV, GTItems.FIELD_GENERATOR_MV);
        createAccelerateRecipe(provider, HV, GTItems.SENSOR_HV, GTItems.FIELD_GENERATOR_HV);
        createAccelerateRecipe(provider, EV, GTItems.SENSOR_EV, GTItems.FIELD_GENERATOR_EV);
        createAccelerateRecipe(provider, IV, GTItems.SENSOR_IV, GTItems.FIELD_GENERATOR_IV);
        createAccelerateRecipe(provider, LuV, GTItems.SENSOR_LuV, GTItems.FIELD_GENERATOR_LuV);
        createAccelerateRecipe(provider, ZPM, GTItems.SENSOR_ZPM, GTItems.FIELD_GENERATOR_ZPM);
        createAccelerateRecipe(provider, UV, GTItems.SENSOR_UV, GTItems.FIELD_GENERATOR_UV);
        createOutputBoostRecipe(provider, LV, GTItems.EMITTER_LV, GTItems.SENSOR_LV);
        createOutputBoostRecipe(provider, MV, GTItems.EMITTER_MV, GTItems.SENSOR_MV);
        createOutputBoostRecipe(provider, HV, GTItems.EMITTER_HV, GTItems.SENSOR_HV);
        createOutputBoostRecipe(provider, EV, GTItems.EMITTER_EV, GTItems.SENSOR_EV);
        createOutputBoostRecipe(provider, IV, GTItems.EMITTER_IV, GTItems.SENSOR_IV);
        createOutputBoostRecipe(provider, LuV, GTItems.EMITTER_LuV, GTItems.SENSOR_LuV);
        createOutputBoostRecipe(provider, ZPM, GTItems.EMITTER_ZPM, GTItems.SENSOR_ZPM);
        createOutputBoostRecipe(provider, UV, GTItems.EMITTER_UV, GTItems.SENSOR_UV);
        for (int tier = LV; tier <= OpV; tier++) {
            createInfiniteInputBusRecipe(provider, tier, getEmitter(tier), getSensor(tier));
            createInfiniteInputHatchRecipe(provider, tier, getEmitter(tier), getFieldGenerator(tier));
            createOutputBoostItemBusRecipe(provider, tier, getEmitter(tier), getSensor(tier));
            createOutputBoostFluidHatchRecipe(provider, tier, getEmitter(tier), getFieldGenerator(tier));
        }

        createOverclockRecipe(provider, UV, GTItems.FIELD_GENERATOR_UV, GTItems.VOLTAGE_COIL_UV);
    }

    private static void createAccelerateRecipe(Consumer<FinishedRecipe> provider, int tier, ItemLike sensor,
                                               ItemLike middleItem) {
        if (GTNAMachines2.ACCELERATE_HATCHES[tier] == null) return;
        ItemLike hull = GTMachines.HULL[tier].asStack().getItem();
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNAMachines2.ACCELERATE_HATCHES[tier].asStack().getItem())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', sensor)
                .define('B', middleItem)
                .define('C', hull)
                .unlockedBy("has_hull", InventoryChangeTrigger.TriggerInstance.hasItems(hull))
                .save(provider);
    }

    private static void createOverclockRecipe(Consumer<FinishedRecipe> provider, int tier, ItemLike fieldGen,
                                              ItemLike coil) {
        if (GTNAMachines2.OVERCLOCK_HATCHES[tier] == null) return;
        ItemLike hull = GTMachines.HULL[tier].asStack().getItem();
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNAMachines2.OVERCLOCK_HATCHES[tier].asStack().getItem())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', fieldGen)
                .define('B', coil)
                .define('C', hull)
                .unlockedBy("has_hull", InventoryChangeTrigger.TriggerInstance.hasItems(hull))
                .save(provider);
    }

    private static void createOutputBoostRecipe(Consumer<FinishedRecipe> provider, int tier, ItemLike emitter,
                                                ItemLike sensor) {
        if (GTNAMachines2.OUTPUT_BOOST_HATCHES[tier] == null) return;
        ItemLike hull = GTMachines.HULL[tier].asStack().getItem();
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNAMachines2.OUTPUT_BOOST_HATCHES[tier].asStack().getItem())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', emitter)
                .define('B', sensor)
                .define('C', hull)
                .unlockedBy("has_hull", InventoryChangeTrigger.TriggerInstance.hasItems(hull))
                .save(provider);
    }

    private static void createInfiniteInputBusRecipe(Consumer<FinishedRecipe> provider, int tier, ItemLike emitter,
                                                     ItemLike sensor) {
        if (GTNAMachines2.INFINITE_INPUT_BUSES[tier] == null) return;
        ItemLike baseBus = GTMachines.ITEM_IMPORT_BUS[tier].asStack().getItem();
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNAMachines2.INFINITE_INPUT_BUSES[tier].asStack().getItem())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', emitter)
                .define('B', sensor)
                .define('C', baseBus)
                .unlockedBy("has_base_bus", InventoryChangeTrigger.TriggerInstance.hasItems(baseBus))
                .save(provider);
    }

    private static void createInfiniteInputHatchRecipe(Consumer<FinishedRecipe> provider, int tier, ItemLike emitter,
                                                       ItemLike fieldGenerator) {
        if (GTNAMachines2.INFINITE_INPUT_HATCHES[tier] == null) return;
        ItemLike baseHatch = GTMachines.FLUID_IMPORT_HATCH[tier].asStack().getItem();
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNAMachines2.INFINITE_INPUT_HATCHES[tier].asStack().getItem())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', emitter)
                .define('B', fieldGenerator)
                .define('C', baseHatch)
                .unlockedBy("has_base_hatch", InventoryChangeTrigger.TriggerInstance.hasItems(baseHatch))
                .save(provider);
    }

    private static void createOutputBoostItemBusRecipe(Consumer<FinishedRecipe> provider, int tier, ItemLike emitter,
                                                       ItemLike sensor) {
        if (GTNAMachines2.OUTPUT_BOOST_ITEM_BUSES[tier] == null) return;
        ItemLike baseBus = GTMachines.ITEM_EXPORT_BUS[tier].asStack().getItem();
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNAMachines2.OUTPUT_BOOST_ITEM_BUSES[tier].asStack().getItem())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', emitter)
                .define('B', sensor)
                .define('C', baseBus)
                .unlockedBy("has_base_bus", InventoryChangeTrigger.TriggerInstance.hasItems(baseBus))
                .save(provider);
    }

    private static void createOutputBoostFluidHatchRecipe(Consumer<FinishedRecipe> provider, int tier, ItemLike emitter,
                                                          ItemLike fieldGenerator) {
        if (GTNAMachines2.OUTPUT_BOOST_FLUID_HATCHES[tier] == null) return;
        ItemLike baseHatch = GTMachines.FLUID_EXPORT_HATCH[tier].asStack().getItem();
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC,
                        GTNAMachines2.OUTPUT_BOOST_FLUID_HATCHES[tier].asStack().getItem())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', emitter)
                .define('B', fieldGenerator)
                .define('C', baseHatch)
                .unlockedBy("has_base_hatch", InventoryChangeTrigger.TriggerInstance.hasItems(baseHatch))
                .save(provider);
    }

    private static ItemLike getEmitter(int tier) {
        return (switch (tier) {
            case LV -> GTItems.EMITTER_LV;
            case MV -> GTItems.EMITTER_MV;
            case HV -> GTItems.EMITTER_HV;
            case EV -> GTItems.EMITTER_EV;
            case IV -> GTItems.EMITTER_IV;
            case LuV -> GTItems.EMITTER_LuV;
            case ZPM -> GTItems.EMITTER_ZPM;
            case UV -> GTItems.EMITTER_UV;
            case UHV -> GTItems.EMITTER_UHV;
            case UEV -> GTItems.EMITTER_UEV;
            case UIV -> GTItems.EMITTER_UIV;
            case UXV -> GTItems.EMITTER_UXV;
            case OpV -> GTItems.EMITTER_OpV;
            default -> GTItems.EMITTER_LV;
        }).asStack().getItem();
    }

    private static ItemLike getSensor(int tier) {
        return (switch (tier) {
            case LV -> GTItems.SENSOR_LV;
            case MV -> GTItems.SENSOR_MV;
            case HV -> GTItems.SENSOR_HV;
            case EV -> GTItems.SENSOR_EV;
            case IV -> GTItems.SENSOR_IV;
            case LuV -> GTItems.SENSOR_LuV;
            case ZPM -> GTItems.SENSOR_ZPM;
            case UV -> GTItems.SENSOR_UV;
            case UHV -> GTItems.SENSOR_UHV;
            case UEV -> GTItems.SENSOR_UEV;
            case UIV -> GTItems.SENSOR_UIV;
            case UXV -> GTItems.SENSOR_UXV;
            case OpV -> GTItems.SENSOR_OpV;
            default -> GTItems.SENSOR_LV;
        }).asStack().getItem();
    }

    private static ItemLike getFieldGenerator(int tier) {
        return (switch (tier) {
            case LV -> GTItems.FIELD_GENERATOR_LV;
            case MV -> GTItems.FIELD_GENERATOR_MV;
            case HV -> GTItems.FIELD_GENERATOR_HV;
            case EV -> GTItems.FIELD_GENERATOR_EV;
            case IV -> GTItems.FIELD_GENERATOR_IV;
            case LuV -> GTItems.FIELD_GENERATOR_LuV;
            case ZPM -> GTItems.FIELD_GENERATOR_ZPM;
            case UV -> GTItems.FIELD_GENERATOR_UV;
            case UHV -> GTItems.FIELD_GENERATOR_UHV;
            case UEV -> GTItems.FIELD_GENERATOR_UEV;
            case UIV -> GTItems.FIELD_GENERATOR_UIV;
            case UXV -> GTItems.FIELD_GENERATOR_UXV;
            case OpV -> GTItems.FIELD_GENERATOR_OpV;
            default -> GTItems.FIELD_GENERATOR_LV;
        }).asStack().getItem();
    }
}
