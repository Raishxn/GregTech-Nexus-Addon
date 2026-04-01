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
}
