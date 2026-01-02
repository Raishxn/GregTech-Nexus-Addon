package com.raishxn.gtna.data;

import com.gregtechceu.gtceu.api.GTCEuAPI;
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

    }
}