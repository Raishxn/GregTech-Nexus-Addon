package com.raishxn.gtna.common.data.multiblock;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;

import com.raishxn.gtna.api.machine.feature.eyeofharmony.EyeOfHarmonyMath;
import com.raishxn.gtna.common.data.GTNAMaterials;

import java.util.*;

/** Modern ore catalog adaptation of the pinned GTNH serial Eye; see the operation audit. */
public final class EyeOfHarmonyOverworld {

    private EyeOfHarmonyOverworld() {}

    public record Catalog(EyeOfHarmonyMath.Program program, CompoundTag products) {}

    public static Catalog build(ServerLevel level) {
        return build(level.getRecipeManager());
    }

    public static Catalog build(RecipeManager recipes) {
        Map<Material, Double> weights = new TreeMap<>(Comparator.comparing(Material::getName));
        for (var vein : GTRegistries.ORE_VEINS.values()) {
            if (!vein.dimensionFilter().contains(Level.OVERWORLD) || vein.weight() <= 0 || vein.density() <= 0)
                continue;
            var entries = vein.veinGenerator().getValidMaterialsChances();
            double total = entries.stream().mapToDouble(entry -> Math.max(0, entry.secondInt())).sum();
            if (total <= 0) continue;
            for (var entry : entries) {
                if (entry.secondInt() > 0 && entry.first().hasProperty(PropertyKey.ORE)) {
                    weights.merge(entry.first(), vein.weight() * entry.secondInt() / total, Double::sum);
                }
            }
        }
        double totalWeight = weights.values().stream().mapToDouble(Double::doubleValue).sum();
        if (totalWeight <= 0) throw new IllegalStateException("No supported Overworld ores");
        Map<Material, Double> processed = new TreeMap<>(Comparator.comparing(Material::getName));
        // GTNH normal veins yield two full entries plus two eighth entries = 2.25 VM3 streams.
        // Modern veins have arbitrary entries: retain their normalized weights and this total rate.
        weights.forEach(
                (material, weight) -> process(processed, material, 18_000 * 384.0 * 2.25 * weight / totalWeight));
        ListTag items = new ListTag();
        long sum = 0;
        for (var entry : processed.entrySet()) {
            var dust = ChemicalHelper.get(TagPrefix.dust, entry.getKey());
            long amount = (long) Math.floor(entry.getValue());
            if (dust.isEmpty() || amount <= 0) continue;
            var tag = dust.save(new CompoundTag());
            tag.putLong("remaining", amount);
            items.add(tag);
            sum = Math.addExact(sum, amount);
        }
        if (sum == 0) throw new IllegalStateException("No supported Overworld dust outputs");
        var stone = ChemicalHelper.get(TagPrefix.dust, GTMaterials.Stone).save(new CompoundTag());
        stone.putLong("remaining", Math.multiplyExact(sum, 3));
        items.add(stone);
        ListTag fluids = new ListTag();
        long plasmaSum = 0;
        // Only the intersection with the GTNH valid-plasma list: Argon is deliberately excluded.
        for (Material material : List.of(GTMaterials.Helium, GTMaterials.Iron, GTMaterials.Nitrogen,
                GTMaterials.Nickel, GTMaterials.Americium, GTMaterials.Oxygen, GTMaterials.Tin)) {
            if (!processed.containsKey(material)) continue;
            var plasma = material.getFluid(FluidStorageKeys.PLASMA);
            if (plasma == null) continue;
            long fuel = fuelEU(recipes, plasma);
            if (fuel <= 0) throw new IllegalStateException("Missing plasma fuel for " + material.getName());
            plasmaSum = Math.addExact(plasmaSum, Math.multiplyExact((long) (fuel * 3.85), 8_000_000));
            // Author's output scale is eight million buckets; Forge/AE quantities are mB.
            // Keep the existing fuel-derived program energy balance independent of this output correction.
            fluids.add(fluid(plasma, 8_000_000_000L));
        }
        fluids.add(fluid(GTNAMaterials.RawStarMatter.getFluid(), 100_000));
        fluids.add(fluid(GTNAMaterials.WhiteDwarfMatter.getFluid(), 1152));
        CompoundTag products = new CompoundTag();
        products.put("items", items);
        products.put("fluids", fluids);
        return new Catalog(EyeOfHarmonyMath.Program.overworld((long) (plasmaSum * 3.85)), products);
    }

    public static CompoundTag fluid(net.minecraft.world.level.material.Fluid fluid, long amount) {
        var tag = new FluidStack(fluid, 1).writeToNBT(new CompoundTag());
        tag.putLong("remaining", amount);
        return tag;
    }

    private static long fuelEU(RecipeManager recipes, net.minecraft.world.level.material.Fluid fluid) {
        for (var recipe : recipes.getAllRecipesFor(GTRecipeTypes.PLASMA_GENERATOR_FUELS)) {
            var inputs = recipe.getInputContents(FluidRecipeCapability.CAP);
            if (inputs.size() != 1) continue;
            var ingredient = FluidRecipeCapability.CAP.of(inputs.get(0).content);
            if (ingredient.getAmount() > 0 && ingredient.test(new FluidStack(fluid, ingredient.getAmount()))) {
                return Math.multiplyExact(recipe.getOutputEUt().getTotalEU(), recipe.duration) / ingredient.getAmount();
            }
        }
        return 0;
    }

    private static Material smelt(Material material) {
        var ore = material.getProperty(PropertyKey.ORE);
        return ore == null || ore.getDirectSmeltResult().isNull() ? material : ore.getDirectSmeltResult();
    }

    private static void add(Map<Material, Double> outputs, Material material, double amount) {
        outputs.merge(smelt(material), amount, Double::sum);
    }

    private static void process(Map<Material, Double> outputs, Material material, double rate) {
        var ore = material.getProperty(PropertyKey.ORE);
        add(outputs, material, 2 * ore.getOreMultiplier() * rate);
        for (var separated : ore.getSeparatedInto()) add(outputs, separated, 2 * (0.4 / 4 + 0.2 / 9) * rate);
        double bath = ore.getWashedIn().first().isNull() ? 0 : 0.7;
        if (ore.getOreByProducts().isEmpty()) add(outputs, material, 2 * bath * rate);
        add(outputs, material, 2 * bath * rate);
        double[] multiplier = { 0.1 + 1.0 / 9, 1.0 / 9, 0.1 };
        int index = 0;
        for (var byproduct : ore.getOreByProducts()) {
            if (index < 3) add(outputs, byproduct, 2 * multiplier[index] * rate);
            if (byproduct == material) continue;
            {
                var byOre = byproduct.getProperty(PropertyKey.ORE);
                if (byOre != null && !byOre.getWashedIn().first().isNull()) add(outputs, byproduct, 2 * 0.7 * rate);
                else if (index >= 3) add(outputs, byproduct, 2 * 0.7 * rate);
            }
            index++;
        }
        for (; index < 3; index++) add(outputs, ore.getOreByProduct(index, material), 2 * multiplier[index] * rate);
    }
}
