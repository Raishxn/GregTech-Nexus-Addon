package com.raishxn.gtna.data;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.api.data.tag.GTNATagPrefix;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;
import org.apache.commons.lang3.text.WordUtils;

import java.util.HashSet;
import java.util.Set;

public class GTNALangProvider extends LanguageProvider {

    private final Set<String> addedKeys = new HashSet<>();

    private static final TagPrefix[] GTNA_PREFIXES = {
            GTNATagPrefix.doubleIngot,
            GTNATagPrefix.tripleIngot,
            GTNATagPrefix.quadrupleIngot,
            GTNATagPrefix.quintupleIngot,
            GTNATagPrefix.triplePlate,
            GTNATagPrefix.quadruplePlate,
            GTNATagPrefix.quintuplePlate,
            GTNATagPrefix.superdensePlate,
            GTNATagPrefix.singularity
    };

    public GTNALangProvider(PackOutput output) {
        super(output, GTNACORE.MOD_ID, "en_us");
    }
    @Override
    public void add(String key, String value) {
        if (addedKeys.contains(key)) {
            return;
        }
        addedKeys.add(key);
        super.add(key, value);
    }
    @Override
    protected void addTranslations() {
        addStaticTranslations();
        addTagPrefixCategories();
        for (Material material : GTCEuAPI.materialManager.getRegisteredMaterials()) {
            if (material.getModid().equals(GTNACORE.MOD_ID)) {
                String langKey = material.getUnlocalizedName();
                String matName = formatMaterialName(material.getName());

                add(langKey, matName);

                for (TagPrefix prefix : GTNA_PREFIXES) {
                    if (prefix.doGenerateItem(material)) {
                        String idNoMod = String.format(prefix.idPattern(), material.getName());
                        String itemKey = "item." + GTNACORE.MOD_ID + "." + idNoMod;
                        String itemValue = String.format(prefix.langValue(), matName);

                        add(itemKey, itemValue);
                    }
                }
            }
        }
    }

    private String formatMaterialName(String name) {
        return WordUtils.capitalize(name.replace('_', ' '));
    }

    private void addTagPrefixCategories() {
        add(GTNATagPrefix.doubleIngot.getUnlocalizedName(), "Double Ingot");
        add(GTNATagPrefix.tripleIngot.getUnlocalizedName(), "Triple Ingot");
        add(GTNATagPrefix.quadrupleIngot.getUnlocalizedName(), "Quadruple Ingot");
        add(GTNATagPrefix.quintupleIngot.getUnlocalizedName(), "Quintuple Ingot");
        add(GTNATagPrefix.triplePlate.getUnlocalizedName(), "Triple Plate");
        add(GTNATagPrefix.quadruplePlate.getUnlocalizedName(), "Quadruple Plate");
        add(GTNATagPrefix.quintuplePlate.getUnlocalizedName(), "Quintuple Plate");
        add(GTNATagPrefix.superdensePlate.getUnlocalizedName(), "Superdense Plate");
        add(GTNATagPrefix.singularity.getUnlocalizedName(), "Singularity");
    }

    private void addStaticTranslations() {
        add("block.gtna.wireless_steam_input_hatch", "Wireless Steam Input Hatch");
        add("block.gtna.wireless_steam_input_hatch_steel", "Wireless Steam Input Hatch Steel");
        add("block.gtna.wireless_steam_output_hatch", "Wireless Steam Output Hatch");
        add("block.gtna.wireless_steam_output_hatch_steel", "Wireless Steam Output Hatch Steel");
        add("item.gtna.structure_detect", "Structure Writer");
        add("item.gtna.debug_structure_writer", "Debug Structure Writer");
        add("item.gtna.vajra", "Vajra");
        add("item.gtna.hydraulic_motor", "Hydraulic Motor");
        add("item.gtna.hydraulic_piston", "Hydraulic Piston");
        add("item.gtna.hydraulic_pump", "Hydraulic Pump");
        add("item.gtna.hydraulic_arm", "Hydraulic Arm");
        add("item.gtna.hydraulic_conveyor", "Hydraulic Conveyor");
        add("item.gtna.hydraulic_regulator", "Hydraulic Regulator");
        add("item.gtna.hydraulic_vapor_generator", "Hydraulic Vapor Generator");
        add("item.gtna.hydraulic_steam_jet_spewer", "Hydraulic Steam Jet Spewer");
        add("item.gtna.hydraulic_steam_receiver", "Hydraulic Steam Receiver");
        add("item.gtna.structure_detect.tooltip.0", "§aRight click§7 block to select Multiblock Controller.");
        add("item.gtna.structure_detect.tooltip.1", "§aShift Right click§7 to change mode.");
        add("item.gtna.structure_detect.tooltip.2", "§aMode: §f%s");
        add("item.gtna.structure_detect.error.0", "Required at %s:\n");
        add("item.gtna.structure_detect.error.1", "Required at %s:");
        add("item.gtna.structure_detect.error.2", "At %s %s");
        add("item.gtna.structure_detect.error.3", "(Mirrored Mode)");
        add("item.gtna.structure_detect.error.4", "(Normal Mode)");
        add("structure_detect.tooltip.0", "Right-click multiblock main block");
        add("structure_detect.tooltip.1", "Shift right-click to switch detection mode");
        add("itemGroup.gtna.creative_tab", "GregTech: Nexus Addon");
        add("itemGroup.gtna.creative_tab.machines", "GregTech: Nexus Addon Machines");
        add("itemGroup.gtna.creative_tab.items", "GregTech: Nexus Addon Items");
        add("itemGroup.gtna.creative_tab.material_blocks", "GregTech: Nexus Addon Material Blocks");
        add("itemGroup.gtna.creative_tab.material_fluids", "GregTech: Nexus Addon Material Fluids");
        add("itemGroup.gtna.creative_tab.material_items", "GregTech: Nexus Addon Material Items");
        add("itemGroup.gtna.creative_tab.material_pipes", "GregTech: Nexus Addon Material Pipes & Wires");
        add("itemGroup.gtna.creative_tab.blocks", "GregTech: Nexus Addon Blocks");
        add("structure_writer.export_order", "Export Order: C:%s  S:%s  A:%s");
        add("structure_writer.structural_scale", "Structure Scale: X:%s  Y:%s  Z:%s");
        add("message.gtna.detection_mode_mirrored", "Current detection mode: (Mirrored mode)");
        add("message.gtna.detection_mode_normal", "Current detection mode: (Normal mode)");
        add("message.gtnacore.structure_formed", "Structure formed");
        add("block.gtna.large_steam_crusher", "Large Steam Crusher");
        add("item.gtna.precision_steam_component", "Precision Steam Component");
        add("gtna.tooltip.large_steam_crusher.speed", "Speed: 900% faster than singleblock");
        add("gtna.tooltip.large_steam_crusher.steam", "Steam Consumption: 80% of original");
        add("gtna.tooltip.large_steam_crusher.parallel","Process up to 128 items at once");
        add("gtna.registry.add", "Added by GregTech Nexus Addon");
        add("gtna.multiblock.parallel_amount", "Parallels: %s");
        add("block.gtna.huge_steam_input_bus", "Huge Steam Input Bus");
        add("block.gtna.huge_steam_output_bus", "Huge Steam Output Bus");
        add("gtna.tooltip.huge_steam_bus","Input Bus with a lot of items capacity. around 3654 itens.");
        add("gtna.tooltip.mega_solar.desc", "A massive solar thermal power plant.");
        add("gtna.tooltip.mega_solar.expansion", "Structure is expandable! Add Solar Pipes behind and to the sides.");
        add("gtna.tooltip.mega_solar.sunlight", "REQUIREMENT: Every Solar Pipe casing must have direct access to the sky.");
        add("gtna.tooltip.mega_solar.production", "Production: 10,000 L/s of Steam per active Pipe Block.");
        add("gtna.tooltip.mega_solar.max_size", "Max Size: 33 Wide x 32 Deep.");
        add("gtna.machine.mega_solar.size", "Structure Size: %s x %s");
        add("gtna.machine.mega_solar.sunlit", "Sunlit Cells: %s");
        add("gtna.machine.mega_solar.production", "Steam Production: %s L/t");
        add("gtna.machine.wireless_steam_hatch.tooltip", "Steam Production: %s L/t");
        add("block.gtna.mega_pressure_solar_boiler", "Mega Pressure Solar Boiler");
        add("block.gtna.breel_pipe_casing", "Breel Pipe Casing");
        add("block.gtna.hyper_pressure_breel_casing", "Hyper Pressure Breel Casing");
        add("block.gtna.steam_compact_pipe_casing", "Steam Compact Pipe Casing");
        add("block.gtna.vibration_safe_casing", "Vibration Safe Casing");
        add("block.gtna.bronze_reinforced_wood", "Bronze Reinforced Wood");
        add("block.gtna.steel_reinforced_wood", "Steel Reinforced Wood");
        add("block.gtna.iron_reinforced_wood", "Iron Reinforced Wood");
        add("block.gtna.solar_boiling_cell", "Solar Boiling Cell");
        add("gtna.machine.wireless_steam_output.tooltip_desc", "Sends Steam wirelessly to your Global Network.");
        add("gtna.machine.wireless_steam_output.tooltip_usage", "Usage: Place on Boilers to export Steam.");
        add("gtna.machine.wireless_steam_input.tooltip_desc", "Receives Steam wirelessly from your Global Network.");
        add("block.gtna.large_steam_furnace", "Large Steam Furnace");
        add("gtna.tooltip.large_steam_furnace.desc", "An industrial-grade steam smelting facility.");
        add("gtna.tooltip.large_steam_furnace.speed", "Speed: 900% faster than a standard Steam Furnace.");
        add("gtna.tooltip.large_steam_furnace.efficiency", "Efficiency: Consumes only 50% of the required Steam.");
        add("gtna.tooltip.large_steam_furnace.parallel", "Parallelism: Processes up to 128 items simultaneously.");
        add("gtna.tooltip.large_steam_furnace.structure", "Structure: 9x5x9 (Hollow Center). check JEI for details.");
        add("block.gtna.large_steam_alloy_smelter", "Large Steam Alloy Smelter");
        add("gtna.tooltip.large_steam_alloy.desc", "High-pressure steam alloying.");
        add("gtna.tooltip.large_steam_alloy.speed", "Speed: 43% faster than Singleblock.");
        add("gtna.tooltip.large_steam_alloy.parallel", "Parallel: Processes up to 64 items.");
        add("gtna.tooltip.large_steam_alloy.structure", "Structure: 3x3x3 Cube (Hollow).");
        add("block.gtna.steam_cobbler", "Steam Cobbler");
        add("gtna.tooltip.steam_cobbler.desc", "Advanced Steam Rock Generator.");
        add("gtna.tooltip.steam_cobbler.modes", "Generates various stones based on Programmed Circuits.");
        add("gtna.tooltip.steam_cobbler.consumption", "Steam Consumption: 1200 L/s (60 L/t)");
        add("gtna.tooltip.steam_cobbler.parallel", "Max Parallel: 16 operations.");
        add("gtna.tooltip.steam_cobbler.structure", "Structure: 3x3x3 Cube with Bronze Pipe center.");
        add("block.gtna.stone_superheater", "Stone SuperHeater");
        add("block.gtna.steam_manufacturer", "Steam Manufacturer");
        add("block.gtna.stronze_wrapped_casing", "Stronze-Wrapped Casing");
        add("block.gtna.hydraulic_assembler_casing", "Hydraulic Assembler Casing");
        add("block.gtna.borosilicate_glass", "Borosilicate Glass");
        add("block.gtna.breel_plated_casing", "Breel-Plated Casing");
        add("gtna.tooltip.stone_superheater.desc", "Extreme heat stone melting.");
        add("gtna.tooltip.stone_superheater.parallel", "Max Parallel: 32");
        add("gtna.tooltip.stone_superheater.steam", "Steam Cost: 640 L/s per active recipe.");
        add("gtna.super_heater", "Super Heating");
        add("gtna.hydraulic_manufacturing", "Hydraulic Manufacturing");
        add("item.gtceu.tool.vajra", "Vajra Omnitool");
        add("gtna.tooltip.steam_manufacturer.desc", "Advanced Hydraulic Assembly Line.");
        add("gtna.tooltip.steam_manufacturer.parallel", "Max Parallel: 16");
        add("gtna.tooltip.steam_manufacturer.type", "Recipe Type: Hydraulic Manufacturing");
        add("block.gtna.steam_woodcutter", "Steam Woodcutter");
        add("gtna.woodcutter", "Woodcutter");
        add("gtna.tooltip.steam_woodcutter.desc", "Industrial Tree Processor.");
        add("gtna.tooltip.steam_woodcutter.parallel", "Max Parallel: 64");
        add("gtna.tooltip.steam_woodcutter.steam", "Steam Consumption: 1200 L/s");
        add("gtna.tooltip.steam_woodcutter.info", "Processes saplings consuming Only Steam.");
        add("gtna.recipe.hydraulic_manufacturing", "Hydraulic Manufacturing");
        add("block.gtna.leap_forward_one_blast_furnace", "Leap Forward One Blast Furnace");
        add("gtna.tooltip.leap_pbf.desc", "A Leap Forward in Primitive Technology.");
        add("gtna.tooltip.leap_pbf.speed", "Duration: Starts at 20s (+20s per layer).");
        add("gtna.tooltip.leap_pbf.parallel", "Parallel: Doubles every layer (Starts at 1x).");
        add("gtna.tooltip.leap_pbf.max", "Max Parallel: 32,000.");
        add("gtna.tooltip.leap_pbf.note", "Trade-off: Taller structure = More items but slower cycle.");
        add("gtna.multiblock.leap_pbf.parallel_hud", "Current Parallel: %s");
        add("gtna.multiblock.leap_pbf.duration_hud", "Cycle Time: %ss");
        add("block.gtna.infernal_coke_oven", "Infernal Coke Oven");
        add("gtna.tooltip.infernal_coke.desc", "Hellish efficiency for coal processing.");
        add("gtna.tooltip.infernal_coke.speed_bonus", "Ramps up speed by 1% every 5s. (Max 1000%). Decays 5% every 5s when idle.");
        add("gtna.tooltip.infernal_coke.max_speed", "Stage Bonus: +16 Parallels & +1600L/s Steam Cost every 10 min.");
        add("gtna.tooltip.infernal_coke.parallel", "Dynamic Parallel: Starts at 8 (Max 256).");
        add("gtna.tooltip.infernal_coke.steam", "Steam Cost: Starts at 6400 L/s. Increases with stage.");
        add("gtna.tooltip.infernal_coke.structure", "Structure: 3x3x3 Hollow Nether Bricks.");
        add("gtna.multiblock.infernal_coke.speed", "Current Speed: %s");
        add("gtna.multiblock.infernal_coke.uptime", "Uptime: %ss");
        add("gtna.recipe.infernal_coke", "Infernal Coke Processing");
        add("gtna.multiblock.infernal_coke.speed", "Current Speed: %s");
        add("gtna.multiblock.infernal_coke.uptime", "Uptime: %ss");
        add("gtna.infernal_coke", "Infernal Coke Processing");
        add("block.gtna.hyper_pressure_reactor", "Hyper Pressure Reactor");
        add("gtna.high_pressure_reactor", "Hyper Pressure Reactor");
        add("block.gtna.compact_hyper_pressure_reactor", "Compact Hyper Pressure Reactor");
        add("gtna.tooltip.hyper_pressure.desc", "Pressure-based fluid reaction chamber.");
        add("gtna.tooltip.hyper_pressure.no_energy", "Requires NO Energy or Steam(Maybe) to operate (Logic only).");
        add("gtna.tooltip.hyper_pressure.parallel", "Max Parallel: %s");
        add("gtna.tooltip.compact_hyper_pressure.desc", "Extreme density fluid processor.");
        add("gtna.tooltip.compact_hyper_pressure.special", "Can process Dense Supercritical Steam from basic resources.");
        add("gtna.recipe.high_pressure_reactor", "High Pressure Reaction");
        add("gtna.tooltip.compact_hyper_pressure.parallel","Max Parallel: 512");
        add("gtna.recipe.condition.compact_only","Requires: CHPR\nCompact HyperPressure Reactor");
        add("block.gtna.void_miner_steam_gate_aged", "Void Miner SteamGate Aged");
        add("gtna.tooltip.void_miner.desc", "Harvesting raw resources from the Steam Dimensions.");
        add("gtna.tooltip.void_miner.fluid_req", "Requires: 10,000L of Drilling Fluid per operation.");
        add("gtna.tooltip.void_miner.catalyst_info", "Inject Advanced Steam into Input Hatches to boost efficiency:");
        add("gtna.tooltip.void_miner.tier_dense", "Dense Steam: 2x Output | 2x Speed | 1.5x EU Cost");
        add("gtna.tooltip.void_miner.tier_super", "SuperHeated: 3x Output | 3x Speed | 2x EU Cost");
        add("gtna.tooltip.void_miner.tier_insane", "Insanely: 5x Output | 5x Speed | 4x EU Cost");
        add("gtna.tooltip.void_miner.outputs", "Outputs: Raw Gold, Copper, Iron, Cobalt, Coal.");
        add("gtna.machine.void_miner.steam_tier", "Steam Injection Tier");
        add("block.gtna.industrial_slaughterhouse", "Industrial Slaughterhouse");
        add("gtna.machine.slaughterhouse.desc", "High-tier industrial mob processing system.");
        add("gtna.machine.slaughterhouse.mechanics", "Scale drops based on Voltage Tier! (Virtual Mode)");
        add("gtna.machine.slaughterhouse.circuit1", "Circuit 1: Passive Mobs (512 EU | Base LV | x2 drops/tier)");
        add("gtna.machine.slaughterhouse.circuit2", "Circuit 2: Hostile Mobs (2560 EU | Base MV | x2 drops/tier)");
        add("gtna.machine.slaughterhouse.circuit3", "Circuit 3: Bosses (32k EU | Base ZPM | x3 drops/tier)");
        add("gtna.machine.slaughterhouse.circuit4", "Circuit 4: Dragon (120k EU | Base UHV | x5 drops/tier)");
        add("gtna.machine.slaughterhouse.tier", "Current Tier: %s");
        add("gtna.machine.slaughterhouse.mode.passive", "Passive Farming");
        add("gtna.machine.slaughterhouse.mode.hostile", "Hostile Farming");
        add("gtna.machine.slaughterhouse.mode.boss", "Boss Farming");
        add("gtna.machine.slaughterhouse.mode.dragon", "Dragon Slayer");
        add("gtna.machine.slaughterhouse.mode.unknown", "Unknown/Idle");
        add("gtna.recipe.slaughterhouse", "Industrial Slaughter");
        add("gtna.slaughterhouse", "Industrial Slaughter");
        add("gtna.multiblock.leap_pbf.layers_hud", "Extra Layers: %s");
        add("block.gtna.parallel_hatch_uhv", "UHV Parallel Control Hatch");
        add("block.gtna.parallel_hatch_uev", "UEV Parallel Control Hatch");
        add("block.gtna.parallel_hatch_uiv", "UIV Parallel Control Hatch");
        add("block.gtna.parallel_hatch_uxv", "UXV Parallel Control Hatch");
        add("block.gtna.parallel_hatch_opv", "OpV Parallel Control Hatch");
        add("block.gtna.parallel_hatch_max", "MAX Parallel Control Hatch");
        add("block.gtna.parallel_hatch_max", "MAX Parallel Control Hatch");
        add("gtna.machine.parallel_hatch.tooltip", "Enables huge parallel processing for multiblocks");
        add("gtna.machine.parallel_hatch.tier", "Max Parallel: %s");
        add("gtna.machine.accelerate_hatch.tooltip", "Reduces recipe duration in Multiblocks");
        add("gtna.machine.accelerate_hatch.desc", "Passive duration modifier");
        add("gtna.machine.accelerate_hatch.amount", "Target Duration: %s");
        add("gtna.multiblock.max_threads", "Max Threads: %s");
        add("gtna.machine.thread_hatch.tooltip", "Multiblock Logic Expansion");
        add("gtna.machine.thread_hatch.desc", "Allows the multiblock to process multiple distinct recipes simultaneously.");
        add("gtna.machine.thread_hatch.count", "Threads: +%s");
}
}