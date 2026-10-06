package com.raishxn.gtna.data.recipe;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.crafting.PartialNBTIngredient;
import net.minecraftforge.fluids.FluidStack;

import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.common.data.GTNAItems;
import com.raishxn.gtna.common.data.GTNAMachines3;
import com.raishxn.gtna.common.data.GTNARecipeType;
import com.raishxn.gtna.common.item.DepositRecorderBehavior;
import com.raishxn.gtna.config.VoidFluidDrillConfig;

import java.util.Locale;
import java.util.function.Consumer;

public final class VoidFluidDrillingRecipes {

    private VoidFluidDrillingRecipes() {}

    public static void register(Consumer<FinishedRecipe> provider) {
        if (!VoidFluidDrillConfig.get().enabled) return;
        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("void_fluid_drilling_rig")
                .inputItems(
                        com.gregtechceu.gtceu.common.data.machines.GTMultiMachines.FLUID_DRILLING_RIG[VoidFluidDrillConfig
                                .get().controllerRigTier]
                                .asStack())
                .inputItems(GTItems.ELECTRIC_PUMP_EV, 4)
                .inputItems(VoidFluidDrillConfig.get().controllerSensorTier == GTValues.HV ? GTItems.SENSOR_HV.get() :
                        GTItems.SENSOR_EV.get(), 2)
                .inputItems(GTItems.FIELD_GENERATOR_EV).inputItems(CustomTags.EV_CIRCUITS, 4)
                .inputItems(TagPrefix.plate, GTMaterials.Titanium, 8)
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(576))
                .outputItems(GTNAMachines3.VOID_FLUID_DRILLING_RIG.asStack()).duration(600).EUt(1920).save(provider);
        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("deposit_recorder")
                .inputItems(GTItems.SENSOR_HV).inputItems(CustomTags.HV_CIRCUITS)
                .inputItems(GTItems.TOOL_DATA_STICK).inputItems(TagPrefix.plate, GTMaterials.Titanium, 2)
                .outputItems(GTNAItems.DEPOSIT_RECORDER).duration(200).EUt(GTValues.VA[GTValues.HV]).save(provider);
        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("fluid_separation_filter")
                .inputItems(TagPrefix.ring, GTMaterials.Steel, 4).inputItems(TagPrefix.dust, GTMaterials.Carbon, 4)
                .inputItems(TagPrefix.wireFine, GTMaterials.Gold, 8)
                .outputItems(GTNAItems.FLUID_SEPARATION_FILTER, 32).duration(200).EUt(GTValues.VA[GTValues.HV])
                .save(provider);
        for (var p : VoidFluidDrillConfig.get().programs) {
            var fluid = BuiltInRegistries.FLUID.get(ResourceLocation.parse(p.fluid));
            // Optional dimensions/mods may be absent. Missing fluids never turn into empty/free recipes.
            if (fluid == Fluids.EMPTY) {
                GTNACORE.LOGGER.warn("Skipping void fluid program {}: unavailable fluid {}", p.id, p.fluid);
                continue;
            }
            var builder = GTNARecipeType.VOID_FLUID_DRILLING_RECIPES.recipeBuilder(p.id)
                    .notConsumable(PartialNBTIngredient.of(GTNAItems.DEPOSIT_DATA.get(),
                            DepositRecorderBehavior.programTag(p.dimension, p.fluid)))
                    .inputFluids(GTMaterials.DrillingFluid.getFluid(p.drillingFluid))
                    .outputFluids(new FluidStack(fluid, p.output))
                    .addData("gtna_fluid_program", p.id)
                    .addData("gtna_fluid_origin", p.dimension)
                    .addData("gtna_fluid_remote_upgrade", p.remoteUpgrade)
                    .duration(p.duration).EUt(p.euPerTick);
            if (p.nitrogen > 0) builder.inputFluids(GTMaterials.Nitrogen.getFluid(p.nitrogen));
            if (p.filters > 0) builder.inputItems(GTNAItems.FLUID_SEPARATION_FILTER, p.filters);
            builder.save(provider);
        }
        VoidFluidDrillConfig.get().upgrades.forEach((stage, upgrade) -> {
            if (upgrade.researchItems.isEmpty()) return;
            var research = upgrade.researchItems.stream()
                    .map(id -> BuiltInRegistries.ITEM.get(ResourceLocation.parse(id))).toList();
            if (research.contains(Items.AIR)) {
                GTNACORE.LOGGER.warn("Skipping remote fluid upgrade {}: unavailable research item", stage);
                return;
            }
            String tier = GTValues.VN[upgrade.tier].toLowerCase(Locale.ROOT);
            var sensor = BuiltInRegistries.ITEM.get(ResourceLocation.parse("gtceu:" + tier + "_sensor"));
            var field = BuiltInRegistries.ITEM.get(ResourceLocation.parse("gtceu:" + tier + "_field_generator"));
            if (sensor == Items.AIR || field == Items.AIR)
                throw new IllegalStateException("Missing upgrade components for " + tier);
            var builder = GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("fluid_remote_upgrade_" + stage)
                    .inputItems(GTItems.TOOL_DATA_ORB).inputItems(sensor, 2).inputItems(field, 2)
                    .inputItems(CustomTags.CIRCUITS_ARRAY[upgrade.tier], 4)
                    .inputFluids(GTMaterials.SolderingAlloy.getFluid(576))
                    .outputItems(GTNAItems.FLUID_REMOTE_UPGRADES.get(stage)).duration(600)
                    .EUt(GTValues.VA[upgrade.tier]);
            research.forEach(builder::notConsumable);
            builder.save(provider);
        });
    }
}
