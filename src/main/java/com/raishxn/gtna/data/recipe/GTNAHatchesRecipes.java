package com.raishxn.gtna.data.recipe;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.raishxn.gtna.common.data.GTNAMachines2;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Locale;
import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;

public class GTNAHatchesRecipes {

    public static void register(Consumer<FinishedRecipe> provider) {
        for (int tier = LV; tier <= MAX; tier++) {
            if (GTNAMachines2.ACCELERATE_HATCHES[tier] == null) continue;
            var hull = GTMachines.HULL[tier].asStack();
            var sensor = getComponent("sensor", tier);
            var fieldGen = getComponent("field_generator", tier);

            if (isValid(sensor) && isValid(fieldGen)) {
                ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNAMachines2.ACCELERATE_HATCHES[tier].asStack().getItem())
                        .pattern("ABA")
                        .pattern("BCB")
                        .pattern("ABA")
                        .define('A', sensor)
                        .define('B', fieldGen)
                        .define('C', hull.getItem())
                        .unlockedBy("has_hull", InventoryChangeTrigger.TriggerInstance.hasItems(hull.getItem()))
                        .save(provider);
            }
        }
        for (int tier = ZPM; tier <= MAX; tier++) {
            if (GTNAMachines2.THREAD_HATCHES[tier] == null) continue;
            ItemStack cableStack = ItemStack.EMPTY;
            if (tier == ZPM) {
                cableStack = ChemicalHelper.get(TagPrefix.cableGtQuadruple, GTMaterials.UraniumRhodiumDinaquadide);
            } else if (tier == UV) {
                cableStack = ChemicalHelper.get(TagPrefix.cableGtQuadruple, GTMaterials.EnrichedNaquadahTriniumEuropiumDuranide);
            }
            if (cableStack.isEmpty()) continue;
            var hull = GTMachines.HULL[tier].asStack();
            var circuitTag = getCircuitTag(tier);
            var robotArm = getComponent("robot_arm", tier);
            var conveyor = getComponent("conveyor_module", tier);
            var parallelHatch = GTNAMachines2.ADVANCED_PARALLEL_HATCH[tier];
            Item parallelControl = (parallelHatch != null) ? parallelHatch.asStack().getItem() : null;
            if (isValid(robotArm) && isValid(conveyor) && parallelControl != null) {
                ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNAMachines2.THREAD_HATCHES[tier].asStack().getItem())
                        .pattern("ABA")
                        .pattern("CDC")
                        .pattern("EFE")
                        .define('A', circuitTag)
                        .define('B', robotArm)
                        .define('C', conveyor)
                        .define('D', parallelControl)
                        .define('E', cableStack.getItem())
                        .define('F', hull.getItem())
                        .unlockedBy("has_hull", InventoryChangeTrigger.TriggerInstance.hasItems(hull.getItem()))
                        .save(provider);
            }
        }
        for (int tier = UV; tier <= MAX; tier++) {
            if (GTNAMachines2.OVERCLOCK_HATCHES[tier] == null) continue;
            var hull = GTMachines.HULL[tier].asStack();
            var fieldGen = getComponent("field_generator", tier);
            var voltageCoil = getVoltageCoil(tier);
            if (isValid(fieldGen) && isValid(voltageCoil)) {
                ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTNAMachines2.OVERCLOCK_HATCHES[tier].asStack().getItem())
                        .pattern("ABA")
                        .pattern("BCB")
                        .pattern("ABA")
                        .define('A', fieldGen)
                        .define('B', voltageCoil)
                        .define('C', hull.getItem())
                        .unlockedBy("has_hull", InventoryChangeTrigger.TriggerInstance.hasItems(hull.getItem()))
                        .save(provider);
            }
        }
    }
    private static Item getComponent(String name, int tier) {
        String tierName = GTValues.VN[tier].toLowerCase(Locale.ROOT);
        ResourceLocation id = com.gregtechceu.gtceu.GTCEu.id(name + "_" + tierName);
        return ForgeRegistries.ITEMS.getValue(id);
    }
    private static TagKey<Item> getCircuitTag(int tier) {
        String tierName = GTValues.VN[tier].toLowerCase(Locale.ROOT);
        return ItemTags.create(new ResourceLocation("forge", "circuits/" + tierName));
    }
    private static Item getVoltageCoil(int tier) {
        String tierName = GTValues.VN[tier].toLowerCase(Locale.ROOT);
        ResourceLocation id = com.gregtechceu.gtceu.GTCEu.id(tierName + "_voltage_coil");
        return ForgeRegistries.ITEMS.getValue(id);
    }
    private static boolean isValid(Item item) {
        return item != null && item != Items.AIR;
    }
}