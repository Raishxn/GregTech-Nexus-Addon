package com.raishxn.gtna.data.recipe;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import com.raishxn.gtna.common.data.GTNAEyeOfHarmonyContent;
import com.raishxn.gtna.common.data.GTNAItems;
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
        planets(provider, eut, spatial.asStack(), temporal.asStack());
    }

    /**
     * Every other Planet Block is researched from the previous body and needs that dimension's own material:
     * 64 of its surface block plus, for the Ad Astra planets, its Planet Data Chip (not consumed). Higher rocket
     * tiers add field generators and take longer.
     */
    private static void planets(Consumer<FinishedRecipe> provider, long eut, ItemStack spatial, ItemStack temporal) {
        var overworld = GTNAEyeOfHarmonyContent.OVERWORLD_PLANET.asStack();
        planet(provider, eut, spatial, temporal, "nether", GTNAEyeOfHarmonyContent.NETHER_PLANET.asStack(), overworld,
                0, "minecraft:netherrack", null, new ItemStack(Items.NETHER_STAR, 4));
        planet(provider, eut, spatial, temporal, "end", GTNAEyeOfHarmonyContent.END_PLANET.asStack(),
                GTNAEyeOfHarmonyContent.NETHER_PLANET.asStack(), 0, "minecraft:end_stone", null,
                new ItemStack(Items.DRAGON_BREATH, 16));
        if (!net.minecraftforge.fml.ModList.get().isLoaded("ad_astra")) return;
        var moon = GTNAEyeOfHarmonyContent.MOON_PLANET.asStack();
        var mars = GTNAEyeOfHarmonyContent.MARS_PLANET.asStack();
        var venus = GTNAEyeOfHarmonyContent.VENUS_PLANET.asStack();
        planet(provider, eut, spatial, temporal, "moon", moon, overworld, 1, "ad_astra:moon_stone", 0, null);
        planet(provider, eut, spatial, temporal, "mars", mars, moon, 2, "ad_astra:mars_stone", 1, null);
        planet(provider, eut, spatial, temporal, "venus", venus, mars, 4, "ad_astra:venus_stone", 2, null);
        planet(provider, eut, spatial, temporal, "mercury", GTNAEyeOfHarmonyContent.MERCURY_PLANET.asStack(), mars,
                4, "ad_astra:mercury_stone", 3, null);
        planet(provider, eut, spatial, temporal, "glacio", GTNAEyeOfHarmonyContent.GLACIO_PLANET.asStack(), venus,
                5, "ad_astra:glacio_stone", 4, null);
    }

    private static void planet(Consumer<FinishedRecipe> provider, long eut, ItemStack spatial, ItemStack temporal,
                               String name, ItemStack output, ItemStack research, int rocketTier, String surface,
                               Integer dataChip, ItemStack extra) {
        var surfaceItem = BuiltInRegistries.ITEM.get(new ResourceLocation(surface));
        if (surfaceItem == Items.AIR) return;
        var builder = GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder("eye_of_harmony/" + name + "_planet")
                .inputItems(spatial.copyWithCount(4 + 2 * rocketTier))
                .inputItems(temporal.copyWithCount(4 + 2 * rocketTier))
                .inputItems(TagPrefix.block, GTMaterials.Neutronium, 1 + rocketTier)
                .inputItems(GTItems.EMITTER_UIV, 4)
                .inputItems(new ItemStack(surfaceItem, 64));
        if (rocketTier > 0) builder.inputItems(GTItems.FIELD_GENERATOR_UIV, 2 * rocketTier);
        if (extra != null) builder.inputItems(extra);
        if (dataChip != null) builder.notConsumable(GTNAItems.PLANET_DATA_CHIPS[dataChip].asStack());
        builder.outputItems(output).duration(2400 * (rocketTier + 1)).EUt(eut)
                .stationResearch(b -> b.researchStack(research).CWUt(256).EUt(eut))
                .save(provider);
    }
}
