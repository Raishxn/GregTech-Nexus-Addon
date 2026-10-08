package com.raishxn.gtna.data.recipe;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.machines.GTAEMachines;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;

import appeng.core.definitions.AEBlocks;
import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.common.data.GTNAMachines2;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;

public class GTNAHatchesRecipes {

    public static void register(Consumer<FinishedRecipe> provider) {
        createHighTierModuleHatches(provider);
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
                            ChemicalHelper.get(TagPrefix.wireGtDouble, GTMaterials.UraniumRhodiumDinaquadide)
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
                            ChemicalHelper.get(TagPrefix.wireGtDouble,
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
        for (int tier = LV; tier <= MAX; tier++) {
            restrictedPart(provider, "output_boost_hatch_", GTNAMachines2.OUTPUT_BOOST_HATCHES, tier,
                    GTMachines.HULL[tier].asStack(), "emitter", "sensor");
            restrictedPart(provider, "infinite_input_bus_", GTNAMachines2.INFINITE_INPUT_BUSES, tier,
                    GTMachines.ITEM_IMPORT_BUS[tier].asStack(), "robot_arm", "conveyor_module");
            restrictedPart(provider, "infinite_input_hatch_", GTNAMachines2.INFINITE_INPUT_HATCHES, tier,
                    GTMachines.FLUID_IMPORT_HATCH[tier].asStack(), "electric_pump", "fluid_regulator");
            restrictedPart(provider, "output_boost_item_bus_", GTNAMachines2.OUTPUT_BOOST_ITEM_BUSES, tier,
                    GTMachines.ITEM_EXPORT_BUS[tier].asStack(), "emitter", "conveyor_module");
            restrictedPart(provider, "output_boost_fluid_hatch_", GTNAMachines2.OUTPUT_BOOST_FLUID_HATCHES, tier,
                    GTMachines.FLUID_EXPORT_HATCH[tier].asStack(), "emitter", "electric_pump");
        }

        createOverclockRecipe(provider, UV, GTItems.FIELD_GENERATOR_UV, GTItems.VOLTAGE_COIL_UV);
        createCraftingCPUInterfaceRecipe(provider);
        createMEStorageAccessRecipes(provider);
        createMEExportBufferRecipe(provider);
    }

    private static void createCraftingCPUInterfaceRecipe(Consumer<FinishedRecipe> provider) {
        if (GTNAMachines2.CRAFTING_CPU_INTERFACE == null) {
            return;
        }
        ItemLike hull = GTMachines.HULL[HV].asStack().getItem();
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNAMachines2.CRAFTING_CPU_INTERFACE.asStack().getItem())
                .pattern("ABA")
                .pattern("CDC")
                .pattern("AEA")
                .define('A', CustomTags.HV_CIRCUITS)
                .define('B', AEBlocks.CRAFTING_UNIT.stack().getItem())
                .define('C', GTItems.EMITTER_HV.asStack().getItem())
                .define('D', hull)
                .define('E', GTItems.FIELD_GENERATOR_HV.asStack().getItem())
                .unlockedBy("has_hull_hv", InventoryChangeTrigger.TriggerInstance.hasItems(hull))
                .save(provider);
    }

    /** Merges one ME output bus and one ME output hatch; the unbounded buffer costs LuV logistics parts. */
    private static void createMEExportBufferRecipe(Consumer<FinishedRecipe> provider) {
        if (GTNAMachines2.ME_EXPORT_BUFFER == null) return;
        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GTNACORE.id("me_export_buffer"))
                .inputItems(GTMachines.HULL[LuV].asStack())
                .inputItems(GTAEMachines.ITEM_EXPORT_BUS_ME.asStack())
                .inputItems(GTAEMachines.FLUID_EXPORT_HATCH_ME.asStack())
                .inputItems(AEBlocks.CRAFTING_STORAGE_256K.stack())
                .inputItems(GTItems.CONVEYOR_MODULE_LuV.asStack(2))
                .inputItems(GTItems.ELECTRIC_PUMP_LuV.asStack(2))
                .inputItems(CustomTags.LuV_CIRCUITS, 2)
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(576))
                .outputItems(GTNAMachines2.ME_EXPORT_BUFFER.asStack())
                .EUt(VA[LuV])
                .duration(400)
                .save(provider);
    }

    private static void createMEStorageAccessRecipes(Consumer<FinishedRecipe> provider) {
        if (GTNAMachines2.ME_STORAGE_ACCESS_HATCH != null) {
            ItemLike hull = GTMachines.HULL[EV].asStack().getItem();
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNAMachines2.ME_STORAGE_ACCESS_HATCH.asStack().getItem())
                    .pattern("ABA")
                    .pattern("CDC")
                    .pattern("AEA")
                    .define('A', CustomTags.EV_CIRCUITS)
                    .define('B', AEBlocks.CRAFTING_STORAGE_64K.stack().getItem())
                    .define('C', GTItems.EMITTER_EV.asStack().getItem())
                    .define('D', hull)
                    .define('E', GTItems.FIELD_GENERATOR_EV.asStack().getItem())
                    .unlockedBy("has_hull_ev", InventoryChangeTrigger.TriggerInstance.hasItems(hull))
                    .save(provider);
        }
        if (GTNAMachines2.ME_BIG_STORAGE_ACCESS_HATCH != null) {
            ItemLike hull = GTMachines.HULL[IV].asStack().getItem();
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC,
                    GTNAMachines2.ME_BIG_STORAGE_ACCESS_HATCH.asStack().getItem())
                    .pattern("ABA")
                    .pattern("CDC")
                    .pattern("AEA")
                    .define('A', CustomTags.IV_CIRCUITS)
                    .define('B', AEBlocks.CRAFTING_STORAGE_256K.stack().getItem())
                    .define('C', GTItems.EMITTER_IV.asStack().getItem())
                    .define('D', hull)
                    .define('E', GTItems.FIELD_GENERATOR_IV.asStack().getItem())
                    .unlockedBy("has_hull_iv", InventoryChangeTrigger.TriggerInstance.hasItems(hull))
                    .save(provider);
        }
        if (GTNAMachines2.ME_IO_PORT_HATCH != null) {
            ItemLike hull = GTMachines.HULL[EV].asStack().getItem();
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNAMachines2.ME_IO_PORT_HATCH.asStack().getItem())
                    .pattern("ABA")
                    .pattern("CDC")
                    .pattern("AEA")
                    .define('A', CustomTags.EV_CIRCUITS)
                    .define('B', AEBlocks.IO_PORT.stack().getItem())
                    .define('C', GTItems.CONVEYOR_MODULE_EV.asStack().getItem())
                    .define('D', hull)
                    .define('E', GTItems.FIELD_GENERATOR_EV.asStack().getItem())
                    .unlockedBy("has_hull_ev", InventoryChangeTrigger.TriggerInstance.hasItems(hull))
                    .save(provider);
        }
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

    private static ResourceLocation id(String prefix, int tier) {
        return GTNACORE.id(prefix + VN[tier].toLowerCase());
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

    /**
     * UHV-MAX module hatches that had no recipe. Each tier consumes the previous tier of the same hatch on the
     * Assembly Line and is researched from it; MAX reuses the OpV components, which GTCEu does not go beyond.
     */
    private static void createHighTierModuleHatches(Consumer<FinishedRecipe> provider) {
        moduleChain(provider, "accelerate_hatch", GTNAMachines2.ACCELERATE_HATCHES, UHV,
                GTNAMachines2.ACCELERATE_HATCHES[UV], "sensor", "emitter");
        moduleChain(provider, "overclock_hatch", GTNAMachines2.OVERCLOCK_HATCHES, UHV,
                GTNAMachines2.OVERCLOCK_HATCHES[UV], "field_generator",
                "electric_pump");
        moduleChain(provider, "thread_hatch", GTNAMachines2.THREAD_HATCHES, UEV, GTNAMachines2.THREAD_HATCHES[UHV],
                "robot_arm",
                "conveyor_module");
        var gtParallel = com.gregtechceu.gtceu.common.data.machines.GCYMMachines.PARALLEL_HATCH;
        moduleChain(provider, "parallel_hatch", GTNAMachines2.ADVANCED_PARALLEL_HATCH, UHV,
                gtParallel.length > UV ? gtParallel[UV] : null, "robot_arm", "field_generator");
    }

    private static void moduleChain(Consumer<FinishedRecipe> provider, String name,
                                    com.gregtechceu.gtceu.api.machine.MachineDefinition[] hatches, int from,
                                    com.gregtechceu.gtceu.api.machine.MachineDefinition belowFrom, String first,
                                    String second) {
        for (int tier = from; tier <= MAX; tier++) {
            if (hatches[tier] == null) continue;
            var previous = tier == from ? belowFrom : hatches[tier - 1];
            if (previous == null) continue;
            int part = Math.min(tier, OpV);
            var a = component(part, first);
            var b = component(part, second);
            if (a == null || b == null) continue;
            long eut = VA[tier];
            var research = previous.asStack();
            GTRecipeTypes.ASSEMBLY_LINE_RECIPES
                    .recipeBuilder(GTNACORE.id(name + "_" + VN[tier].toLowerCase(java.util.Locale.ROOT)))
                    .inputItems(GTMachines.HULL[tier].asStack())
                    .inputItems(previous.asStack())
                    .inputItems(a, tier == MAX ? 8 : 4)
                    .inputItems(b, tier == MAX ? 8 : 2)
                    .inputItems(CustomTags.CIRCUITS_ARRAY[tier], 4)
                    .inputItems(TagPrefix.plateDense, GTMaterials.Neutronium, 2 * (tier - UV))
                    .inputFluids(com.raishxn.gtna.common.data.GTNAMaterials.Indalloy140.getFluid(1152 * (tier - UV)))
                    .outputItems(hatches[tier].asStack())
                    .duration(400 + 200 * (tier - UV)).EUt(eut)
                    .stationResearch(r -> r.researchStack(research).CWUt(256).EUt(eut))
                    .save(provider);
        }
    }

    private static net.minecraft.world.item.Item component(int tier, String kind) {
        var item = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(
                new ResourceLocation("gtceu", VN[tier].toLowerCase(java.util.Locale.ROOT) + "_" + kind));
        return item == net.minecraft.world.item.Items.AIR ? null : item;
    }

    /**
     * Restricted parts (only with restricted items enabled): deliberately expensive for their tier, with circuits
     * of the next tier, four field generators and a long Assembler run. They only act on recipes of their own tier
     * ({@code GTNASpecialPartUtil.matchesTier}).
     */
    private static void restrictedPart(Consumer<FinishedRecipe> provider, String prefix,
                                       com.gregtechceu.gtceu.api.machine.MachineDefinition[] parts, int tier,
                                       net.minecraft.world.item.ItemStack base, String first, String second) {
        if (parts[tier] == null || base.isEmpty()) return;
        int part = Math.min(tier, OpV);
        var a = component(part, first);
        var b = component(part, second);
        var field = component(part, "field_generator");
        if (a == null || b == null || field == null) return;
        int next = Math.min(tier + 1, MAX);
        GTNARecipeVisibility.saveRestricted(provider, id(prefix, tier), restricted -> GTRecipeTypes.ASSEMBLER_RECIPES
                .recipeBuilder(id(prefix, tier))
                .inputItems(base)
                .inputItems(a, 4)
                .inputItems(b, 4)
                .inputItems(field, 4)
                .inputItems(CustomTags.CIRCUITS_ARRAY[tier], 8)
                .inputItems(CustomTags.CIRCUITS_ARRAY[next], 2)
                .inputFluids((tier <= LuV ? GTMaterials.SolderingAlloy :
                        com.raishxn.gtna.common.data.GTNAMaterials.Indalloy140).getFluid(576 * tier))
                .outputItems(parts[tier].asStack())
                .duration(1200 + 400 * tier).EUt(VA[tier])
                .save(restricted));
    }
}
