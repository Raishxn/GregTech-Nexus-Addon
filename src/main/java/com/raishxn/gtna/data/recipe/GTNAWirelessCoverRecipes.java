package com.raishxn.gtna.data.recipe;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.ForgeRegistries;

import com.raishxn.gtna.common.data.GTNAItems;

import java.util.Locale;
import java.util.function.Consumer;

/**
 * Assembler recipes for all 112 Wireless EU Receiver / Transmitter covers.
 *
 * Items are retrieved from {@link GTNAItems#WIRELESS_EU_RECEIVER_COVER_ITEMS} and
 * {@link GTNAItems#WIRELESS_EU_TRANSMITTER_COVER_ITEMS} — the ItemEntry arrays
 * populated by {@code GTNAItems.registerWirelessEUCoverItems()}.
 *
 * Hatch fallback (when native-amp hatch doesn't exist at this tier):
 *   4A  LV/MV/HV  → 4×  1A hatch
 *   16A LV/MV/HV  → 16× 1A hatch
 *   64A LV/MV/HV  → 64× 1A hatch
 *   64A EV+       → 4×  16A hatch
 */
public class GTNAWirelessCoverRecipes {

    private static final int[] AMP_VALUES = { 1, 4, 16, 64 };
    private static final String[] AMP_TAGS = { "1a", "4a", "16a", "64a" };

    public static void register(Consumer<FinishedRecipe> provider) {
        int[] tiers = GTValues.tiersBetween(GTValues.LV,
                GTCEuAPI.isHighTier() ? GTValues.MAX : GTValues.UV);

        for (int tier : tiers) {
            String tierName = GTValues.VN[tier].toLowerCase(Locale.ROOT);
            int euCost = (int) Math.min(GTValues.VA[tier], Integer.MAX_VALUE);

            for (int ai = 0; ai < AMP_VALUES.length; ai++) {
                String ampTag = AMP_TAGS[ai];

                // Use ItemEntry.asStack() — the correct way to get an ItemStack from
                // a registered item in GTCEu's Registrate system.
                var rcvEntry = GTNAItems.WIRELESS_EU_RECEIVER_COVER_ITEMS[tier][ai];
                var txEntry  = GTNAItems.WIRELESS_EU_TRANSMITTER_COVER_ITEMS[tier][ai];

                HatchIngredient inputIng  = resolveInputIngredient(tier, ai);
                HatchIngredient outputIng = resolveOutputIngredient(tier, ai);

                // ── Receiver ──────────────────────────────────────────────────
                if (rcvEntry != null && inputIng != null) {
                    GTRecipeTypes.ASSEMBLER_RECIPES
                            .recipeBuilder("wireless_eu_receiver_" + ampTag + "_" + tierName)
                            .inputItems(inputIng.stack.getItem(), inputIng.count)
                            .inputItems(getControllerCoverIngredient())
                            .inputItems(getFluxPlugIngredient())
                            .inputItems(Items.ENDER_PEARL)
                            .outputItems(rcvEntry.asStack())
                            .duration(200)
                            .EUt(euCost)
                            .save(provider);
                } else if (rcvEntry == null) {
                    com.raishxn.gtna.GTNACORE.LOGGER.warn(
                            "[GTNA] Wireless EU Receiver item not registered for tier={} amp={} — skipping recipe",
                            tierName, ampTag);
                } else {
                    com.raishxn.gtna.GTNACORE.LOGGER.warn(
                            "[GTNA] Wireless EU Receiver hatch ingredient missing for tier={} amp={} — skipping recipe",
                            tierName, ampTag);
                }

                // ── Transmitter ───────────────────────────────────────────────
                if (txEntry != null && outputIng != null) {
                    GTRecipeTypes.ASSEMBLER_RECIPES
                            .recipeBuilder("wireless_eu_transmitter_" + ampTag + "_" + tierName)
                            .inputItems(outputIng.stack.getItem(), outputIng.count)
                            .inputItems(getControllerCoverIngredient())
                            .inputItems(getFluxPointIngredient())
                            .inputItems(Items.ENDER_PEARL)
                            .outputItems(txEntry.asStack())
                            .duration(200)
                            .EUt(euCost)
                            .save(provider);
                } else if (txEntry == null) {
                    com.raishxn.gtna.GTNACORE.LOGGER.warn(
                            "[GTNA] Wireless EU Transmitter item not registered for tier={} amp={} — skipping recipe",
                            tierName, ampTag);
                } else {
                    com.raishxn.gtna.GTNACORE.LOGGER.warn(
                            "[GTNA] Wireless EU Transmitter hatch ingredient missing for tier={} amp={} — skipping recipe",
                            tierName, ampTag);
                }
            }
        }
    }

    // ── GTCEu machine controller cover resolution ─────────────────────────
    // Use ForgeRegistries so the ingredient serializes with the correct
    // registry key ("gtceu:machine_controller_cover") that resolves at
    // runtime and during data generation.

    private static Ingredient getControllerCoverIngredient() {
        var item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("gtceu", "machine_controller_cover"));
        if (item == null || item == Items.AIR) {
            com.raishxn.gtna.GTNACORE.LOGGER.warn(
                    "[GTNA] gtceu:machine_controller_cover not found in registry — recipe may be incomplete");
            return Ingredient.EMPTY;
        }
        return Ingredient.of(item);
    }

    // ── FluxNetworks item resolution ─────────────────────────────────────────
    // Use ForgeRegistries instead of raw item-ID strings so the ingredient
    // serializes with the correct registry key ("fluxnetworks:flux_plug")
    // that resolves at runtime even when FluxNetworks is not on the data-gen
    // classpath.

    private static Ingredient getFluxPlugIngredient() {
        var item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("fluxnetworks", "flux_plug"));
        if (item == null || item == Items.AIR) {
            com.raishxn.gtna.GTNACORE.LOGGER.warn(
                    "[GTNA] fluxnetworks:flux_plug not found in registry — recipe may be incomplete");
            return Ingredient.EMPTY;
        }
        return Ingredient.of(item);
    }

    private static Ingredient getFluxPointIngredient() {
        var item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("fluxnetworks", "flux_point"));
        if (item == null || item == Items.AIR) {
            com.raishxn.gtna.GTNACORE.LOGGER.warn(
                    "[GTNA] fluxnetworks:flux_point not found in registry — recipe may be incomplete");
            return Ingredient.EMPTY;
        }
        return Ingredient.of(item);
    }

    // ── Hatch ingredient resolution ───────────────────────────────────────────

    private record HatchIngredient(ItemStack stack, int count) {}

    private static HatchIngredient resolveInputIngredient(int tier, int ampIdx) {
        return resolveIngredient(tier, ampIdx,
                GTMachines.ENERGY_INPUT_HATCH,
                GTMachines.ENERGY_INPUT_HATCH_4A,
                GTMachines.ENERGY_INPUT_HATCH_16A);
    }

    private static HatchIngredient resolveOutputIngredient(int tier, int ampIdx) {
        return resolveIngredient(tier, ampIdx,
                GTMachines.ENERGY_OUTPUT_HATCH,
                GTMachines.ENERGY_OUTPUT_HATCH_4A,
                GTMachines.ENERGY_OUTPUT_HATCH_16A);
    }

    private static HatchIngredient resolveIngredient(int tier, int ampIdx,
                                                      MachineDefinition[] arr1a,
                                                      MachineDefinition[] arr4a,
                                                      MachineDefinition[] arr16a) {
        ItemStack hatch1a = getStack(arr1a, tier);
        if (hatch1a == null) return null;

        return switch (ampIdx) {
            case 0 -> new HatchIngredient(hatch1a, 1);
            case 1 -> {
                ItemStack h4a = getStack(arr4a, tier);
                yield h4a != null ? new HatchIngredient(h4a, 1)
                                  : new HatchIngredient(hatch1a, 4);
            }
            case 2 -> {
                ItemStack h16a = getStack(arr16a, tier);
                yield h16a != null ? new HatchIngredient(h16a, 1)
                                   : new HatchIngredient(hatch1a, 16);
            }
            case 3 -> {
                ItemStack h16a = getStack(arr16a, tier);
                yield h16a != null ? new HatchIngredient(h16a, 4)
                                   : new HatchIngredient(hatch1a, 64);
            }
            default -> null;
        };
    }

    private static ItemStack getStack(MachineDefinition[] arr, int tier) {
        if (arr == null || tier >= arr.length || arr[tier] == null) return null;
        return arr[tier].asStack();
    }
}
