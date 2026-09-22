package com.raishxn.gtna.common.data;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.client.util.TooltipHelper;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.loading.FMLEnvironment;

import com.raishxn.gtna.GTNACORE;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * Standardises the tooltips of every GTNA steam machine ({@code large_steam_*} and {@code steam_*},
 * including the GTNL Steam Elevator modules) to the GTNL reference layout:
 *
 * <pre>
 * &lt;Machine Name&gt;                                     (animated rainbow line)
 * Machine Type: &lt;recipe type&gt;
 * &lt;speed / efficiency / parallel stats&gt;
 * High pressure mode doubles processing speed and steam consumption
 * ──────────────────────────────
 * Source: &lt;addon&gt;                                     (appended afterwards by {@link GTNASources})
 * </pre>
 *
 * <p>
 * <b>Rainbow name:</b> {@code MachineDefinition} and {@code MetaMachineItem} expose no hook to style
 * the item hover name itself (the name row is rendered by vanilla outside the tooltip builder), so
 * the animated name is added as the first tooltip line, following GTCEu's own
 * {@code TooltipHelper.RAINBOW_HSL_SLOW} usage (e.g. {@code GTMultiMachines}). The style is only
 * applied on the client; the line itself is dist-agnostic so the server-side
 * {@code everyGtnaMachineTooltipBuilds} gametest can still exercise the builder.
 *
 * <p>
 * The {@code Machine Type} line is only added when the machine has a real (non-dummy) recipe type,
 * and the separator only when {@link GTNASources} will actually append a {@code Source} line, so the
 * tooltip never ends with a dangling separator. The high-pressure line is only added to the machines
 * whose pattern uses {@code SteamMultiMachineBase.casing()} (tier-aware bronze/steel casings).
 */
public final class GTNASteamTooltips {

    /** Steam machines whose pattern uses {@code SteamMultiMachineBase.casing()}. */
    private static final Set<String> HIGH_PRESSURE = Set.of(
            "large_steam_alloy_smelter",
            "large_steam_bath",
            "large_steam_bending",
            "large_steam_centrifuge",
            "large_steam_circuit_assembler",
            "large_steam_compressor",
            "large_steam_crusher",
            "large_steam_cutting",
            "large_steam_extractor",
            "large_steam_extruder",
            "large_steam_forming_press",
            "large_steam_furnace",
            "large_steam_hammer",
            "large_steam_lathe",
            "large_steam_mixer",
            "large_steam_ore_washer",
            "large_steam_sifter",
            "large_steam_thermal_centrifuge",
            "large_steam_wiremill",
            "steam_lava_maker",
            "steam_item_vault");

    /** GTNL draws ~30 box-drawing characters between the stats and the attribution line. */
    private static final String SEPARATOR = "\u2500".repeat(30);

    private GTNASteamTooltips() {}

    public static void applyAll() {
        for (MachineDefinition definition : GTRegistries.MACHINES) {
            ResourceLocation id = definition.getId();
            if (!GTNACORE.MOD_ID.equals(id.getNamespace())) {
                continue;
            }
            String path = id.getPath();
            if (!path.startsWith("large_steam_") && !path.startsWith("steam_")) {
                continue;
            }

            GTRecipeType recipeType = firstRealRecipeType(definition);
            boolean highPressure = HIGH_PRESSURE.contains(path);
            boolean sourced = GTNASources.hasSource(path);

            BiConsumer<ItemStack, List<Component>> original = definition.getTooltipBuilder();
            definition.setTooltipBuilder((stack, components) -> {
                List<Component> base = new ArrayList<>();
                if (original != null) {
                    original.accept(stack, base);
                }
                components.add(rainbowName(definition));
                if (recipeType != null) {
                    components.add(Component.translatable("gtna.tooltip.machine_type",
                            Component.translatable(recipeType.registryName.toLanguageKey())));
                }
                components.addAll(base);
                if (highPressure) {
                    components.add(Component.translatable("gtna.tooltip.steam.high_pressure"));
                }
                if (sourced) {
                    components.add(Component.literal(SEPARATOR).withStyle(ChatFormatting.DARK_GRAY));
                }
            });
        }
    }

    /**
     * A copy of the machine's display name carrying GTCEu's animated rainbow style. Applied only on
     * the client (the style lambda lives in a client package); the plain name is still returned on
     * the server so tooltip builders stay callable there (tooltip gametests run dedicated-server).
     */
    private static Component rainbowName(MachineDefinition definition) {
        MutableComponent name = Component.translatable(definition.getDescriptionId());
        if (!FMLEnvironment.dist.isClient()) {
            return name;
        }
        return name.withStyle(TooltipHelper.RAINBOW_HSL_SLOW);
    }

    /** The first non-dummy recipe type of the machine, or {@code null} when it has none. */
    private static GTRecipeType firstRealRecipeType(MachineDefinition definition) {
        GTRecipeType[] recipeTypes = definition.getRecipeTypes();
        if (recipeTypes.length == 0) {
            return null;
        }
        GTRecipeType recipeType = recipeTypes[0];
        return recipeType == GTRecipeTypes.DUMMY_RECIPES ? null : recipeType;
    }
}
