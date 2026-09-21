package com.raishxn.gtna.common.data;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import com.raishxn.gtna.GTNACORE;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Tooltip attribution for content ported from other GT addons/modpacks.
 *
 * <p>
 * GTNH and GTO credit the original source addon in the tooltips of ported structures, and GTO asked
 * GTNA to do the same. The source is declared centrally in {@link #SOURCES} (machine registry name →
 * source id) and appended to every GTNA machine tooltip by {@link #applyAll()}, so this map is the
 * single source of truth for attribution.
 */
public final class GTNASources {

    public static final String GTO = "gto";
    public static final String GTNL = "gtnl";
    public static final String GTNH = "gtnh";
    public static final String TST = "tst";
    public static final String GTL = "gtl";
    public static final String GTLCORE = "gtlcore";
    public static final String GTLSUPB = "gtlsupb";
    public static final String GTOEPP = "gtoepp";
    public static final String GTMTHINGS = "gtmthings";

    /** Machine registry path → source id. Only machines with a confirmed origin are listed. */
    private static final Map<String, String> SOURCES = Map.ofEntries(
            // --- GTO / GTOCore ---
            Map.entry("annihilate_generator", GTO),
            Map.entry("eye_of_harmony", GTO),
            Map.entry("me_storage", GTO),
            Map.entry("dimensionally_transcendent_dirt_forge", GTO),
            Map.entry("dimensionally_transcendent_steam_boiler", GTO),
            Map.entry("dimensionally_transcendent_steam_oven", GTO),
            Map.entry("primitive_distillation_tower", GTO),
            Map.entry("void_miner_steam_gate_aged", GTO),
            Map.entry("leap_forward_one_blast_furnace", GTO),
            Map.entry("large_steam_solar_boiler", GTO),
            // --- Twist Space Technology (GPL-3.0) ---
            Map.entry("eye_of_wood", TST),
            Map.entry("industrial_slaughterhouse", TST),
            // --- GT: Not Leisure ---
            Map.entry("large_steam_crusher", GTNL),
            Map.entry("large_steam_furnace", GTNL),
            Map.entry("large_steam_alloy_smelter", GTNL),
            Map.entry("large_steam_hammer", GTNL),
            Map.entry("large_steam_compressor", GTNL),
            Map.entry("large_steam_extractor", GTNL),
            Map.entry("large_steam_ore_washer", GTNL),
            Map.entry("large_steam_circuit_assembler", GTNL),
            Map.entry("large_steam_mixer", GTNL),
            Map.entry("large_steam_centrifuge", GTNL),
            Map.entry("large_steam_thermal_centrifuge", GTNL),
            Map.entry("large_steam_bath", GTNL),
            Map.entry("large_steam_lathe", GTNL),
            Map.entry("large_steam_cutting", GTNL),
            Map.entry("large_steam_forming_press", GTNL),
            Map.entry("large_steam_storage_tank", GTNL),
            Map.entry("steam_manufacturer", GTNL),
            Map.entry("infernal_coke_oven", GTNL),
            Map.entry("nexus_molecular_forge", GTNL),
            Map.entry("hyper_pressure_reactor", GTNL),
            Map.entry("compact_hyper_pressure_reactor", GTNL),
            Map.entry("steam_cobbler", GTNL),
            Map.entry("steam_woodcutter", GTNL),
            Map.entry("stone_superheater", GTNL),
            // --- GTLsupb (LGPLv3) ---
            Map.entry("universal_factory", GTLSUPB),
            Map.entry("primitive_stone_furnace", GTLSUPB),
            // --- GTO Extended Platform Presets (permission granted by the GTO team) ---
            Map.entry("industrial_platform_deployment_tools", GTOEPP));

    private GTNASources() {}

    /** The "Source: <addon>" tooltip line for a source id. */
    public static Component line(String sourceId) {
        return Component.translatable("gtna.tooltip.source", Component.translatable("gtna.source." + sourceId))
                .withStyle(ChatFormatting.DARK_GRAY);
    }

    /**
     * Appends the attribution line to every GTNA machine whose origin is declared in {@link #SOURCES}.
     * Must run after all machine definitions are registered.
     */
    public static void applyAll() {
        for (MachineDefinition definition : GTRegistries.MACHINES) {
            ResourceLocation id = definition.getId();
            if (!GTNACORE.MOD_ID.equals(id.getNamespace())) {
                continue;
            }
            String sourceId = SOURCES.get(id.getPath());
            if (sourceId == null) {
                continue;
            }
            BiConsumer<ItemStack, List<Component>> original = definition.getTooltipBuilder();
            definition.setTooltipBuilder((stack, components) -> {
                if (original != null) {
                    original.accept(stack, components);
                }
                components.add(line(sourceId));
            });
        }
    }
}
