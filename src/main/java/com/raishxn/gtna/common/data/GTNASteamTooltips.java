package com.raishxn.gtna.common.data;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import com.raishxn.gtna.GTNACORE;

import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * Appends the shared "High pressure mode" description line to the tooltips of the steam multiblocks
 * whose structure actually accepts steel casings (GTNL parity).
 *
 * <p>
 * The line is descriptive, so it is only added to machines that implement the feature. When the
 * remaining {@code large_steam_*} structures are re-ported onto the tier-aware casing predicate
 * ({@code SteamMultiMachineBase.casing()}), their ids are added here.
 */
public final class GTNASteamTooltips {

    /** Steam machines whose pattern uses {@code SteamMultiMachineBase.casing()}. */
    private static final Set<String> HIGH_PRESSURE = Set.of(
            "large_steam_alloy_smelter",
            "large_steam_bending",
            "large_steam_extruder",
            "large_steam_wiremill",
            "large_steam_sifter",
            "steam_lava_maker",
            "steam_item_vault");

    private GTNASteamTooltips() {}

    public static void applyAll() {
        for (MachineDefinition definition : GTRegistries.MACHINES) {
            ResourceLocation id = definition.getId();
            if (!GTNACORE.MOD_ID.equals(id.getNamespace()) || !HIGH_PRESSURE.contains(id.getPath())) {
                continue;
            }
            BiConsumer<ItemStack, List<Component>> original = definition.getTooltipBuilder();
            definition.setTooltipBuilder((stack, components) -> {
                if (original != null) {
                    original.accept(stack, components);
                }
                components.add(Component.translatable("gtna.tooltip.steam.high_pressure"));
            });
        }
    }
}
