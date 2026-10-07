package com.raishxn.gtna.data.recipe;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;

import com.raishxn.gtna.common.data.GTNAEyeOfHarmonyContent;
import com.raishxn.gtna.common.data.GTNAMachines;

import java.util.function.Consumer;

/** A GTNA-adapted, pre-EOH construction route. GTNH's BEC inputs are intentionally not claimed here. */
public final class GTNAEyeOfHarmonyProgression {

    private GTNAEyeOfHarmonyProgression() {}

    public static void register(Consumer<FinishedRecipe> provider) {
        long eut = GTValues.VA[GTValues.UXV];
        var boundary = GTNAEyeOfHarmonyContent.BOUNDARY_CASING;
        var spatial = GTNAEyeOfHarmonyContent.SPATIAL_CASING;
        var temporal = GTNAEyeOfHarmonyContent.TEMPORAL_CASING;

        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder("eye_of_harmony/boundary_casing")
                .inputItems(TagPrefix.plateDense, GTMaterials.Neutronium, 16)
                .inputItems(GTItems.FIELD_GENERATOR_UIV, 4)
                .inputItems(GTItems.ENERGY_CLUSTER, 2)
                .outputItems(boundary.asStack(16)).duration(1200).EUt(eut)
                .stationResearch(b -> b.researchStack(new ItemStack(GTItems.FIELD_GENERATOR_UIV.get()))
                        .CWUt(256).EUt(eut))
                .save(provider);
        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder("eye_of_harmony/spatial_casing")
                .inputItems(boundary.asStack(4))
                .inputItems(TagPrefix.plateDense, GTMaterials.Neutronium, 16)
                .inputItems(GTItems.EMITTER_UIV, 4)
                .outputItems(spatial.asStack(16)).duration(1200).EUt(eut)
                .stationResearch(b -> b.researchStack(boundary.asStack()).CWUt(256).EUt(eut))
                .save(provider);
        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder("eye_of_harmony/temporal_casing")
                .inputItems(boundary.asStack(4))
                .inputItems(TagPrefix.plateDense, GTMaterials.Neutronium, 16)
                .inputItems(GTItems.SENSOR_UIV, 4)
                .outputItems(temporal.asStack(16)).duration(1200).EUt(eut)
                .stationResearch(b -> b.researchStack(boundary.asStack()).CWUt(256).EUt(eut))
                .save(provider);

        for (int tier = 0; tier < GTNAEyeOfHarmonyContent.TIER_NAMES.length; tier++) {
            for (var family : com.raishxn.gtna.common.block.EyeOfHarmonyFieldBlock.Family.values()) {
                var fields = switch (family) {
                    case COMPRESSION -> GTNAEyeOfHarmonyContent.COMPRESSION_FIELDS;
                    case ACCELERATION -> GTNAEyeOfHarmonyContent.ACCELERATION_FIELDS;
                    case STABILISATION -> GTNAEyeOfHarmonyContent.STABILISATION_FIELDS;
                };
                var builder = GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(
                        "eye_of_harmony/" + family.id + "_tier_" + tier)
                        .inputItems(spatial.asStack(2)).inputItems(temporal.asStack(2))
                        .inputItems(TagPrefix.plateDense, GTMaterials.Neutronium, 8)
                        .inputItems(GTItems.FIELD_GENERATOR_UIV, 2 + tier)
                        .outputItems(fields[tier].asStack(4)).duration(1200 + tier * 400).EUt(eut);
                if (tier == 0) {
                    builder.stationResearch(b -> b.researchStack(spatial.asStack()).CWUt(256).EUt(eut));
                } else {
                    builder.inputItems(fields[tier - 1].asStack(4));
                    ItemStack previous = fields[tier - 1].asStack();
                    builder.stationResearch(b -> b.researchStack(previous).CWUt(256).EUt(eut));
                }
                builder.save(provider);
            }
        }

        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder("eye_of_harmony/controller")
                .inputItems(boundary.asStack(8)).inputItems(spatial.asStack(8))
                .inputItems(temporal.asStack(8)).inputItems(GTItems.FIELD_GENERATOR_UIV, 16)
                .inputItems(GTItems.ENERGY_CLUSTER, 4)
                .outputItems(GTNAMachines.EYE_OF_HARMONY.asStack()).duration(6000).EUt(eut)
                .stationResearch(b -> b.researchStack(spatial.asStack()).CWUt(256).EUt(eut))
                .save(provider);
        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder("eye_of_harmony/overworld_planet")
                .inputItems(spatial.asStack(4)).inputItems(temporal.asStack(4))
                .inputItems(TagPrefix.block, GTMaterials.Neutronium, 1)
                .inputItems(GTItems.EMITTER_UIV, 4)
                .outputItems(GTNAEyeOfHarmonyContent.OVERWORLD_PLANET.asStack()).duration(2400).EUt(eut)
                .stationResearch(b -> b.researchStack(temporal.asStack()).CWUt(256).EUt(eut))
                .save(provider);
    }
}
