package com.raishxn.gtna.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;

import net.minecraft.ChatFormatting;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;

import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.api.data.tag.GTNATagPrefix;
import com.raishxn.gtna.common.data.GTNAMachines2;
import com.raishxn.gtna.utils.TextUtil;
import org.apache.commons.lang3.text.WordUtils;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class GTNALangProvider extends LanguageProvider {

    private final Set<String> addedKeys = new HashSet<>();
    private final PackOutput output;

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
        this.output = output;
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
        addManualTranslations();
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

    private void addManualTranslations() {
        Path manualLangPath = output.getOutputFolder()
                .getParent()
                .getParent()
                .resolve("main")
                .resolve("resources")
                .resolve("assets")
                .resolve(GTNACORE.MOD_ID)
                .resolve("lang")
                .resolve("en_us.json");
        if (!Files.exists(manualLangPath)) {
            return;
        }

        try (Reader reader = Files.newBufferedReader(manualLangPath, StandardCharsets.UTF_8)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            for (var entry : json.entrySet()) {
                JsonElement value = entry.getValue();
                if (value != null && value.isJsonPrimitive()) {
                    add(entry.getKey(), value.getAsString());
                }
            }
        } catch (IOException exception) {
            throw new RuntimeException("Failed to load manual GTNA en_us translations", exception);
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
        // --- Hatches Translations (UPDATED) ---

        // Accelerate Hatch
        add("gtna.machine.accelerate_hatch.main_function",
                "Main Function: Unconditionally accelerates machine operation speed");
        add("gtna.machine.accelerate_hatch.range", "Operation Duration adjustment Range: %s - 100%%");
        add("gtna.machine.accelerate_hatch.weakness",
                "The Acceleration effect is weakened by 20% per level when the level of accelerate hatch is lower than the machine tier.");

        // Thread Hatch
        add("gtna.machine.thread_hatch.tooltip", "Can Provide +%s thread parallel processing for the machine.");

        // Overclock Hatch
        add("gtna.machine.overclock_hatch.main_function", "Main function: Overclock machine");
        add("gtna.machine.overclock_hatch.not_installed",
                "When this hatch is not installed, for every 4 times power used machine duration x 55%");
        add("gtna.machine.overclock_hatch.installed",
                "After installing this hatch, the overclock effect is increased to a maximum of every 4 times power used, machine duration x %s");
        add("gtna.machine.overclock_hatch.desc",
                "- More aggressive overclocking strategies, more powerful machine performance and more extreme processing speeds!");
        add("gtna.machine.output_boost_hatch.main_function",
                "Main function: multiplies only recipe outputs for compatible multiblocks");
        add("gtna.machine.output_boost_hatch.multiplier", "Output Multiplier: %sx items and fluids");
        add("gtna.machine.infinite_input_bus.tooltip",
                "Compatible multiblocks can read item inputs from this bus without consuming them");
        add("gtna.machine.infinite_input_hatch.tooltip",
                "Compatible multiblocks can read fluid inputs from this hatch without consuming them");
        add("gtna.machine.output_boost_bus.tooltip",
                "Compatible multiblocks multiply item outputs by %sx while this bus is installed");
        add("gtna.machine.infinite_steam_input_bus.tooltip",
                "Steam multiblocks can read item inputs from this bus without consuming them");
        add("gtna.machine.output_boost_steam_output_bus.tooltip",
                "Steam multiblocks multiply item outputs by %sx while this bus is installed");

        // Parallel Hatch
        add("block.gtna.parallel_hatch_uhv", "UHV Parallel Control Hatch");
        add("block.gtna.parallel_hatch_uev", "UEV Parallel Control Hatch");
        add("block.gtna.parallel_hatch_uiv", "UIV Parallel Control Hatch");
        add("block.gtna.parallel_hatch_uxv", "UXV Parallel Control Hatch");
        add("block.gtna.parallel_hatch_opv", "OpV Parallel Control Hatch");
        add("block.gtna.parallel_hatch_max", "MAX Parallel Control Hatch");
        add("gtna.machine.parallel_hatch.tooltip", "Enables huge parallel processing for multiblocks");
        add("gtna.machine.parallel_hatch.tier", "Max Parallel: %s");
        add("block.gtna.me_mini_pattern_buffer", "ME Mini Pattern Buffer");
        add("block.gtna.me_pattern_buffer", "ME Pattern Buffer");
        add("block.gtna.me_advanced_pattern_buffer", "ME Advanced Pattern Buffer");
        add("block.gtna.me_ultimate_pattern_buffer", "ME Ultimate Pattern Buffer");
        add("block.gtna.me_craft_pattern_hatch", "Nexus Craft Pattern Hatch");
        add("gtna.machine.pattern_buffer.tooltip", "AE2 pattern buffer with per-slot GTNA specialization");
        add("gtna.machine.pattern_buffer.slots", "Pattern Slots: %s");
        add("gtna.machine.pattern_buffer.break_persist", "Stored patterns and slot data are preserved on block drop");
        add("gtna.machine.pattern_buffer.specialization_pending",
                "Middle-click a pattern slot to edit item, fluid, circuit, and mode specialization");
        add("gtna.machine.pattern_buffer.middle_click_hint", "Middle-click to configure this pattern slot");
        add("gtna.machine.pattern_buffer.selected_slot", "Editing Slot %s");
        add("gtna.machine.pattern_buffer.no_slot_selected", "No slot selected");
        add("gtna.machine.pattern_buffer.cached_recipe_short", "Recipe Cache: %s");
        add("gtna.machine.pattern_buffer.derived_mode_short", "Derived Mode: %s");
        add("gtna.machine.pattern_buffer.item_field", "Ghost Items");
        add("gtna.machine.pattern_buffer.item_count_field", "Special Item Count");
        add("gtna.machine.pattern_buffer.fluid_field", "Ghost Fluids");
        add("gtna.machine.pattern_buffer.fluid_amount_field", "Special Fluid Amount (mB)");
        add("gtna.machine.pattern_buffer.fluid_amount_hint", "Drag a fluid here. Right-click clears the slot.");
        add("gtna.machine.pattern_buffer.circuit_field", "Circuit");
        add("gtna.machine.pattern_buffer.mode_field", "Preferred Mode");
        add("gtna.machine.pattern_buffer.mode.auto", "Auto");
        add("gtna.machine.pattern_buffer.mode.none", "No Mode");
        add("gtna.machine.pattern_buffer.mode.legacy", "Custom: %s");
        add("gtna.machine.pattern_buffer.mode_button.tooltip",
                "Click to cycle the preferred multiblock mode for this slot");
        add("gtna.machine.pattern_buffer.mode_button.current", "Selected Mode: %s");
        add("gtna.machine.pattern_buffer.mode_button.derived", "Detected Mode: %s");
        add("gtna.machine.pattern_buffer.no_circuit", "No configured circuit");
        add("gtna.machine.pattern_buffer.clear_specialization", "Clear Spec");
        add("gtna.machine.pattern_buffer.clear_cache", "Clear Cache");
        add("gtna.machine.pattern_buffer.refund_slot", "Refund");
        add("gtna.machine.craft_pattern_hatch.tooltip", "Dedicated AE2 crafting hatch for the Nexus Assembly Forge");
        add("gtna.machine.craft_pattern_hatch.slots", "Pattern Slots: %s");
        add("gtna.machine.craft_pattern_hatch.patterns",
                "Accepts encoded crafting patterns only and registers them to the AE2 network");
        add("gtna.machine.craft_pattern_hatch.cheat",
                "Queues crafted outputs directly for the controller to materialize");
        add("item.gtna.pattern_buffer_upgrade_21", "Pattern Buffer Expansion Card");
        add("item.gtna.pattern_buffer_upgrade_32", "Pattern Buffer Precision Card");
        add("item.gtna.pattern_buffer_upgrade_72", "Pattern Buffer Ascension Card");
        add("item.gtna.infinite_steam_singleblock_cover", "Infinite Steam Singleblock Cover");
        add("item.gtna.infinite_electric_singleblock_cover", "Infinite Electric Singleblock Cover");
        add("item.gtna.pattern_buffer_upgrader.tooltip.use",
                "Right-click a lower tier GTNA Pattern Buffer to upgrade it");
        add("item.gtna.pattern_buffer_upgrader.tooltip.keep_data",
                "Preserves encoded patterns, internal slot data, and slot specialization");

        // --- Wireless Energy/Dynamo Hatches ---
        add("gtna.machine.wireless_energy_hatch.tooltip", "Pulls energy wirelessly from the Nexus Network");
        add("gtna.machine.wireless_energy_hatch.tier_info", "Tier: %s | Amperage: %sA");
        add("gtna.machine.wireless_energy_hatch.bound", "§a[GTNA] §fWireless Energy Hatch bound to your network.");

        add("gtna.machine.wireless_dynamo_hatch.tooltip", "Pushes energy wirelessly to the Nexus Network");
        add("gtna.machine.wireless_dynamo_hatch.tier_info", "Tier: %s | Amperage: %sA");
        add("gtna.machine.wireless_dynamo_hatch.bound", "§a[GTNA] §fWireless Dynamo Hatch bound to your network.");

        add("gtna.machine.wireless_hatch.not_bound", "§cNot bound to any network! Place to auto-bind.");
        add("gtna.machine.wireless_hatch.capacity", "Buffer Capacity: %s EU");
        add("gtna.machine.wireless_hatch.auto_bind", "§8Place in world to auto-bind to your network");

        // Generate names for all Wireless Hatches (LV to MAX, 1A to MAX A) — colored tier names
        for (int tier = GTValues.LV; tier <= GTValues.MAX; tier++) {
            String tierName = GTValues.VN[tier].toLowerCase(Locale.ROOT);
            ChatFormatting color = (tier < TextUtil.GTI_CORE$VC.length) ? TextUtil.GTI_CORE$VC[tier] :
                    ChatFormatting.WHITE;
            String colorCode = getColorCode(color);
            String coloredTierName = colorCode + GTValues.VN[tier] + "§r";
            for (int ampExp = 0; ampExp <= 10; ampExp++) {
                int amps = (int) Math.pow(4, ampExp);
                String outName = "block.gtna.wireless_energy_hatch_" + amps + "a_" + tierName;
                String inName = "block.gtna.wireless_dynamo_hatch_" + amps + "a_" + tierName;
                add(outName, coloredTierName + " " + amps + "A Wireless Energy Hatch");
                add(inName, coloredTierName + " " + amps + "A Wireless Dynamo Hatch");
            }
        }

        // Linker
        add("gtna.message.linker.copied", "Network ID copied to linker!");
        add("gtna.message.linker.linked", "%s linked to network!");
        add("item.gtna.nexus_linker", "Nexus Linker");

        // Terminal
        add("item.gtna.quantum_network_terminal", "Quantum Network Terminal");
        add("gtna.terminal.locate.message", "§a[GTNA Terminal] §fLocated at §eX: %s Y: %s Z: %s §7(%s)");

        // Nexus Structure Terminal
        add("item.gtna.nexus_structure_terminal", "Nexus Structure Terminal");
        add("gtna.terminal.nexus.title", "§l§5Nexus Terminal");

        // Toggle settings
        add("gtna.terminal.nexus.no_hatch", "No Hatch Mode");
        add("gtna.terminal.nexus.no_hatch.tooltip",
                "When enabled, skips placing hatches\n(buses, energy I/O, etc.) during auto-build.");
        add("gtna.terminal.nexus.no_hatch.hint", "§7Bypass hatch placement");

        add("gtna.terminal.nexus.replace_mode", "Replace Mode");
        add("gtna.terminal.nexus.replace_mode.tooltip",
                "When enabled, replaces existing tier blocks\n(e.g. coils) with the configured selection.");
        add("gtna.terminal.nexus.replace_mode.hint", "§7Swap existing blocks with configured tier");

        add("gtna.terminal.nexus.demolition_mode", "Demolition Mode");
        add("gtna.terminal.nexus.demolition_mode.tooltip",
                "When enabled, removes blocks that don't\nbelong to the multiblock pattern.");
        add("gtna.terminal.nexus.demolition_mode.hint", "§7Remove blocks outside the pattern");

        add("gtna.terminal.nexus.use_ae", "Use AE Items");
        add("gtna.terminal.nexus.use_ae.tooltip",
                "When enabled, extracts required blocks from\nyour AE2 network via Wireless Terminal.");
        add("gtna.terminal.nexus.use_ae.hint", "§7Pull materials from ME network");

        add("gtna.terminal.nexus.mirror_build", "Mirror Build");
        add("gtna.terminal.nexus.mirror_build.tooltip",
                "When enabled, builds the structure in\nmirrored orientation.");
        add("gtna.terminal.nexus.mirror_build.hint", "§7Build mirrored structure");

        // Numeric settings
        add("gtna.terminal.nexus.repetitions", "Repetitions");
        add("gtna.terminal.nexus.repetitions.tooltip",
                "Number of structure layer repetitions (0-1000).\nUsed for expandable multiblocks like Distillation Tower.");
        add("gtna.terminal.nexus.repetitions.hint", "§7Scroll wheel or type (0-1000)");

        add("gtna.terminal.nexus.module_build", "Module Build");
        add("gtna.terminal.nexus.module_build.tooltip",
                "Module build count (0-100).\nSets how many module layers to construct.");
        add("gtna.terminal.nexus.module_build.hint", "§7Scroll wheel or type (0-100)");

        // Nexus Structure Terminal Tooltips
        add("item.gtna.nexus_structure_terminal.tooltip.use", "§dRight Click§7: Open Settings");
        add("item.gtna.nexus_structure_terminal.tooltip.shift_use",
                "§dShift + Right Click§7 on Controller: Build Structure");
        add("item.gtna.nexus_structure_terminal.tooltip.replace_mode_active", "§5⚠ Replace Mode Active");
        add("item.gtna.nexus_structure_terminal.tooltip.config", "Open block selection configuration");

        // Block Config tab — category labels with purple accent
        add("gtna.terminal.config.title", "Block Configuration");
        add("gtna.terminal.config.coils", "§dCoils");
        add("gtna.terminal.config.machine_casing", "§dMachine Casings");
        add("gtna.terminal.config.muffler", "§dMuffler Hatches");
        add("gtna.terminal.config.rotor_holder", "§dRotor Holders");
        add("gtna.terminal.config.wireless_capacitor", "§dWireless Capacitors");

        // AE2 Network linking
        add("gtna.terminal.nexus.ae2.linked", "§a[GTNA] §fLinked to ME Wireless Access Point!");
        add("gtna.terminal.nexus.ae2.tooltip.linked", "§a⚡ ME Network linked at [%s, %s, %s]");
        add("gtna.terminal.nexus.ae2.tooltip.not_linked", "§c✖ ME Network: Not connected");
        add("gtna.terminal.nexus.ae2.tooltip.how_to_link", "§8Right-click a Wireless Access Point to link");
        add("gtna.terminal.nexus.ae2.tooltip.in_range", "§a✔ Wireless: In Range");
        add("gtna.terminal.nexus.ae2.tooltip.out_of_range", "§c✖ Wireless: Out of Range");

        // Nexus Flux Matrix
        add("block.gtna.nexus_flux_matrix", "Nexus Flux Matrix");
        add("gtna.machine.nexus_flux_matrix.tooltip_1", "Central hub for the Nexus Energy Network");
        add("gtna.machine.nexus_flux_matrix.tooltip_2", "Stores and distributes energy wirelessly");

        // Capacitor blocks — colored tier names
        String[] capacitorTierNames = { "lv", "mv", "hv", "ev", "iv", "luv", "zpm", "uv", "uhv", "uev", "uiv", "uxv",
                "opv", "max" };
        for (int ci = 0; ci < capacitorTierNames.length; ci++) {
            int capTier = ci + GTValues.LV; // LV=1, MV=2, ...
            ChatFormatting capColor = (capTier < TextUtil.GTI_CORE$VC.length) ? TextUtil.GTI_CORE$VC[capTier] :
                    ChatFormatting.WHITE;
            String capColorCode = getColorCode(capColor);
            String coloredCapTierName = capColorCode + GTValues.VN[capTier] + "§r";
            add("block.gtna.nexus_capacitor_" + capacitorTierNames[ci], coloredCapTierName + " Nexus Capacitor");
        }

        // --- Other Machines & Items ---
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
        add("item.gtna.annihilation_constrainer", "Annihilation Constrainer");
        add("item.gtna.neutronium_antimatter_fuel_rod", "Neutronium Antimatter Fuel Rod");
        add("item.gtna.draconium_antimatter_fuel_rod", "Draconium Antimatter Fuel Rod");
        add("item.gtna.cosmic_neutronium_antimatter_fuel_rod", "Cosmic Neutronium Antimatter Fuel Rod");
        add("item.gtna.infinity_antimatter_fuel_rod", "Infinity Antimatter Fuel Rod");
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
        add("itemGroup.gtna.creative_tab.wireless", "GregTech: Nexus Addon Wireless");
        add("structure_writer.export_order", "Export Order: C:%s  S:%s  A:%s");
        add("structure_writer.structural_scale", "Structure Scale: X:%s  Y:%s  Z:%s");
        add("message.gtna.detection_mode_mirrored", "Current detection mode: (Mirrored mode)");
        add("message.gtna.detection_mode_normal", "Current detection mode: (Normal mode)");
        add("message.gtnacore.structure_formed", "Structure formed");
        add("block.gtna.large_steam_crusher", "Large Steam Crusher");
        add("item.gtna.precision_steam_component", "Precision Steam Component");
        add("gtna.tooltip.large_steam_crusher.speed", "Speed: 900% faster than singleblock");
        add("gtna.tooltip.large_steam_crusher.steam", "Steam Consumption: 80% of original");
        add("gtna.tooltip.large_steam_crusher.parallel", "Process up to 128 items at once");
        add("gtna.registry.add", "Added by GregTech Nexus Addon");
        add("gtna.multiblock.parallel_amount", "Parallels: %s");
        add("block.gtna.huge_steam_input_bus", "Huge Steam Input Bus");
        add("block.gtna.huge_steam_output_bus", "Huge Steam Output Bus");
        add("block.gtna.infinite_steam_input_bus", "Infinite Steam Input Bus");
        add("block.gtna.output_boost_steam_output_bus", "Output Boost Steam Output Bus");
        add("gtna.tooltip.huge_steam_bus", "Input Bus with a lot of items capacity. around 3654 itens.");
        add("gtna.tooltip.mega_solar.desc", "A massive solar thermal power plant.");
        add("gtna.tooltip.mega_solar.expansion", "Structure is expandable! Add Solar Pipes behind and to the sides.");
        add("gtna.tooltip.mega_solar.sunlight",
                "REQUIREMENT: Every Solar Pipe casing must have direct access to the sky.");
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
        add("block.gtna.graviton_field_constraint_casing", "Graviton Field Constraint Casing");
        add("block.gtna.annihilate_core", "Annihilate Core");
        add("block.gtna.hyper_mechanical_casing", "Hyper Mechanical Casing");
        add("block.gtna.hollow_casing", "Hollow Casing");
        add("block.gtna.naquadah_alloy_casing", "Naquadah Alloy Casing");
        add("block.gtna.dyson_control_toroid", "Dyson Control Toroid");
        add("block.gtna.dyson_control_casing", "Dyson Control Casing");
        add("block.gtna.degenerate_rhenium_constrained_casing", "Degenerate Rhenium Constrained Casing");
        add("block.gtna.dyson_receiver_casing", "Dyson Receiver Casing");
        add("block.gtna.rhenium_reinforced_energy_glass", "Rhenium Reinforced Energy Glass");
        add("block.gtna.antimatter_containment_casing", "Antimatter Containment Casing");
        add("block.gtna.dimensionally_transcendent_casing", "Dimensionally Transcendent Casing");
        add("block.gtna.dimension_injection_casing", "Dimension Injection Casing");
        add("block.gtna.dimensional_bridge_casing", "Dimensional Bridge Casing");
        add("block.gtna.dimensional_stability_casing", "Dimensional Stability Casing");
        add("block.gtna.spacetime_compression_field_generator", "Spacetime Compression Field Generator");
        add("block.gtna.bronze_reinforced_wood", "Bronze Reinforced Wood");
        add("block.gtna.steel_reinforced_wood", "Steel Reinforced Wood");
        add("block.gtna.iron_reinforced_wood", "Iron Reinforced Wood");
        add("block.gtna.solar_boiling_cell", "Solar Boiling Cell");
        add("block.gtna.oxidation_resistant_hastelloy_n_mechanical_casing",
                "Oxidation Resistant Hastelloy N Mechanical Casing");
        add("block.gtna.zirconia_ceramic_high_strength_bending_resistance_mechanical_block",
                "Zirconia Ceramic High Strength Bending Resistance Mechanical Block");
        add("block.gtna.naquadah_borosilicate_glass", "Naquadah Borosilicate Glass");
        add("block.gtna.magtech_casing", "Magtech Casing");
        add("block.gtna.process_machine_casing", "Process Machine Casing");
        add("block.gtna.compressor_controller_casing", "Compressor Controller Casing");
        add("block.gtna.extreme_density_casing", "Extreme Density Casing");
        add("block.gtna.annihilate_generator", "Artificial Star");
        add("block.gtna.eye_of_harmony", "Eye of Harmony");
        add("block.gtna.nexus_molecular_forge", "Nexus Assembly Forge");
        add("gtceu.annihilate_generator", "Annihilation Generator");
        add("gtna.cosmos_simulation", "Cosmos Simulation");
        add("gtna.machine.artificial_star.output", "Supports Laser or Wireless Dynamo output.");
        add("gtna.machine.nexus_molecular_forge.tooltip.0",
                "Ultra-fast AE2 mass crafting forge.");
        add("gtna.machine.nexus_molecular_forge.tooltip.1",
                "Queues jobs from Nexus Craft Pattern Hatches and materializes them in giant batches.");
        add("gtna.machine.nexus_molecular_forge.tooltip.2",
                "Parallel crafting follows the AE2 CPU and is optimized for extreme throughput.");
        add("gtna.machine.nexus_molecular_forge.tooltip.3", "Each operation materializes every queued craft output at once.");
        add("gtna.machine.nexus_molecular_forge.tooltip.4", "Queued crafts do not consume physical ingredients.");
        add("gtna.machine.nexus_molecular_forge.tooltip.5", "Power Cost: 1 EU per crafted item, compressed into batch EU/t.");
        add("gtna.machine.nexus_molecular_forge.tooltip.6",
                "HUD shows pattern hatches, loaded patterns, queued outputs, active batch, and forge ceiling.");
        add("gtna.machine.eye_of_harmony.tooltip.0", "Creates a miniature universe and extracts its resources.");
        add("gtna.machine.eye_of_harmony.tooltip.1", "Startup power comes directly from the GTNA wireless network.");
        add("gtna.machine.eye_of_harmony.tooltip.2", "Bind with a Data Stick to swap the network owner.");
        add("gtna.machine.eye_of_harmony.tooltip.3", "Uses circuits 1-4 to choose 0-3 special overclocks.");
        add("gtna.machine.eye_of_harmony.tooltip.4", "Requires 1024 buckets each of Hydrogen and Helium before startup.");
        add("gtna.machine.eye_of_harmony.tooltip.5", "Consumes those gases internally in 100-bucket batches.");
        add("gtna.machine.eye_of_harmony.tooltip.6", "No conventional energy hatches are used here.");
        add("gtna.machine.eye_of_harmony.tooltip.7", "Outputs are handled through the regular item and fluid ports.");
        add("gtna.machine.eye_of_harmony.owner", "Network Owner: %s");
        add("gtna.machine.eye_of_harmony.network_eu", "Stored Network EU: %s");
        add("gtna.machine.eye_of_harmony.startup_eu", "Startup Energy: %s EU");
        add("gtna.machine.eye_of_harmony.hydrogen", "Hydrogen Storage: %s mB");
        add("gtna.machine.eye_of_harmony.helium", "Helium Storage: %s mB");
        add("gtna.machine.eye_of_harmony.rebound", "[GTNA] Eye of Harmony rebound to your network.");
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
        add("block.gtna.large_steam_hammer", "Large Steam Hammer");
        add("gtna.tooltip.large_steam_hammer.desc", "Heavy steam forge hammer based on the GTNH addon layout.");
        add("gtna.tooltip.large_steam_hammer.speed", "Speed: 100% faster than singleblock.");
        add("gtna.tooltip.large_steam_hammer.parallel", "Parallel: Processes up to 64 items.");
        add("gtna.tooltip.large_steam_hammer.structure",
                "Structure: 7x13x7 with iron core, bronze frames, and glass columns.");
        add("block.gtna.large_steam_compressor", "Large Steam Compressor");
        add("gtna.tooltip.large_steam_compressor.desc",
                "High-throughput steam compressor using the GTNH reference shell.");
        add("gtna.tooltip.large_steam_compressor.speed", "Speed: 150% faster than singleblock.");
        add("gtna.tooltip.large_steam_compressor.parallel", "Parallel: Processes up to 48 items.");
        add("gtna.tooltip.large_steam_compressor.structure",
                "Structure: 7x7x7 with framed compression chamber and glass sides.");
        add("block.gtna.large_steam_extractor", "Large Steam Extractor");
        add("gtna.tooltip.large_steam_extractor.desc",
                "Steam extractor with the same compact frame from the GTNH reference addon.");
        add("gtna.tooltip.large_steam_extractor.speed", "Speed: 75% faster than singleblock.");
        add("gtna.tooltip.large_steam_extractor.parallel", "Parallel: Processes up to 48 items.");
        add("gtna.tooltip.large_steam_extractor.structure",
                "Structure: 5x5x5 pressure cage with bronze pipes and glass vents.");
        add("block.gtna.large_steam_ore_washer", "Large Steam Ore Washer");
        add("gtna.tooltip.large_steam_ore_washer.desc",
                "Large steam ore washer using the reference washing basin layout.");
        add("gtna.tooltip.large_steam_ore_washer.speed", "Speed: 400% faster than singleblock.");
        add("gtna.tooltip.large_steam_ore_washer.parallel", "Parallel: Processes up to 96 items.");
        add("gtna.tooltip.large_steam_ore_washer.structure",
                "Structure: 9x5x9 basin with glass walls and bronze pipe agitators.");
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
        add("gtna.tooltip.infernal_coke.speed_bonus",
                "Ramps up speed by 1% every 5s. (Max 1000%). Decays 5% every 5s when idle.");
        add("gtna.tooltip.infernal_coke.max_speed", "Stage Bonus: +16 Parallels & +1600L/s Steam Cost every 10 min.");
        add("gtna.tooltip.infernal_coke.parallel", "Dynamic Parallel: Starts at 8 (Max 256).");
        add("gtna.tooltip.infernal_coke.steam", "Steam Cost: Starts at 6400 L/s. Increases with stage.");
        add("gtna.tooltip.infernal_coke.structure", "Structure: 3x3x3 Hollow Nether Bricks.");
        add("gtna.multiblock.infernal_coke.speed", "Current Speed: %s");
        add("gtna.multiblock.infernal_coke.uptime", "Uptime: %ss");
        add("gtna.recipe.infernal_coke", "Infernal Coke Processing");
        add("gtna.infernal_coke", "Infernal Coke Processing");
        add("block.gtna.hyper_pressure_reactor", "Hyper Pressure Reactor");
        add("gtna.high_pressure_reactor", "Hyper Pressure Reactor");
        add("block.gtna.compact_hyper_pressure_reactor", "Compact Hyper Pressure Reactor");
        add("gtna.tooltip.hyper_pressure.desc", "Pressure-based fluid reaction chamber.");
        add("gtna.tooltip.hyper_pressure.no_energy", "Requires NO Energy or Steam(Maybe) to operate (Logic only).");
        add("gtna.tooltip.hyper_pressure.parallel", "Max Parallel: %s");
        add("gtna.tooltip.compact_hyper_pressure.desc", "Extreme density fluid processor.");
        add("gtna.tooltip.compact_hyper_pressure.special",
                "Can process Dense Supercritical Steam from basic resources.");
        add("gtna.recipe.high_pressure_reactor", "High Pressure Reaction");
        add("gtna.tooltip.compact_hyper_pressure.parallel", "Max Parallel: 512");
        add("gtna.recipe.condition.compact_only", "Requires: CHPR\nCompact HyperPressure Reactor");
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
        add("gtna.multiblock.max_threads", "Max Threads: %s");
        add("gtna.recipe.condition.restricted_items_disabled", "Disabled by Journey mode or Self Restraint");

        // --- Configurações (GUI) ---
        // Drift
        add("config.gtna.option.gameplay", "Gameplay");
        add("config.gtna.option.client", "Client");
        add("config.gtna.option.machines", "Machines");
        add("config.gtna.option.nexusFluxMatrix", "Nexus Flux Matrix");
        add("config.gtna.option.modDifficulty", "Mod Difficulty");
        add("config.gtna.option.selfRestraint", "Self Restraint");
        add("config.gtna.option.disableFlyInertia", "Disable Fly Inertia");

        // Accelerate Hatch
        add("config.gtna.option.accelerateHatchMultiplier", "Accelerate Hatch Speed");
        add("config.gtna.option.accelerateHatchEnergyCost", "Accelerate Hatch Energy Cost");

        // Wireless Steam
        add("config.gtna.option.wirelessSteamTransferRate", "Wireless Steam Transfer Rate");

        // Mega Solar Boiler
        add("config.gtna.option.megaSolarSteamPerBlock", "Solar Steam Per Block");

        // Void Miner - Tier 1 (Dense)
        add("config.gtna.option.voidMinerDenseOutputMult", "Void Miner (Dense) Output");
        add("config.gtna.option.voidMinerDenseSpeedMult", "Void Miner (Dense) Speed");
        add("config.gtna.option.voidMinerDenseEnergyMult", "Void Miner (Dense) Energy");

        // Void Miner - Tier 2 (SuperHeated)
        add("config.gtna.option.voidMinerSuperHeatedOutputMult", "Void Miner (SuperHeated) Output");
        add("config.gtna.option.voidMinerSuperHeatedSpeedMult", "Void Miner (SuperHeated) Speed");
        add("config.gtna.option.voidMinerSuperHeatedEnergyMult", "Void Miner (SuperHeated) Energy");

        // Void Miner - Tier 3 (Insanely)
        add("config.gtna.option.voidMinerInsanelyOutputMult", "Void Miner (Insanely) Output");
        add("config.gtna.option.voidMinerInsanelySpeedMult", "Void Miner (Insanely) Speed");
        add("config.gtna.option.voidMinerInsanelyEnergyMult", "Void Miner (Insanely) Energy");
        add("config.gtna.option.baseLossPercent", "Base Loss Percent");
        add("config.gtna.option.maxTransferTierMAX", "MAX Tier Transfer Limit");
        add("config.gtna.option.safeModeThreshold", "Safe Mode Threshold");
        add("config.gtna.option.safeModeRecovery", "Safe Mode Recovery");
        add("config.gtna.option.alertCooldownTicks", "Alert Cooldown");
        add("config.gtna.option.useHighestTierForEfficiency", "Use Highest Tier For Efficiency");
        add("config.jade.plugin_gtna.multiple_recipes_provider", "Multiple Recipes Machine Info");

        for (int i = 0; i < GTValues.V.length; i++) {
            String tierName = GTValues.VN[i];
            String tierLower = tierName.toLowerCase(Locale.ROOT);
            ChatFormatting color = (i < TextUtil.GTI_CORE$VC.length) ? TextUtil.GTI_CORE$VC[i] : ChatFormatting.WHITE;
            String colorCode = getColorCode(color);
            String coloredTierName = colorCode + tierName + "§r";
            if (i < GTNAMachines2.THREAD_HATCHES.length && GTNAMachines2.THREAD_HATCHES[i] != null) {
                add("block.gtna.thread_hatch_" + tierLower, coloredTierName + " Thread Hatch");
            }
            if (i < GTNAMachines2.ACCELERATE_HATCHES.length && GTNAMachines2.ACCELERATE_HATCHES[i] != null) {
                add("block.gtna.accelerate_hatch_" + tierLower, coloredTierName + " Accelerate Hatch");
            }
            if (i < GTNAMachines2.OVERCLOCK_HATCHES.length && GTNAMachines2.OVERCLOCK_HATCHES[i] != null) {
                add("block.gtna.overclock_hatch_" + tierLower, coloredTierName + " Overclock Hatch");
            }
            if (i < GTNAMachines2.OUTPUT_BOOST_HATCHES.length && GTNAMachines2.OUTPUT_BOOST_HATCHES[i] != null) {
                add("block.gtna.output_boost_hatch_" + tierLower, coloredTierName + " Output Boost Hatch");
            }
            if (i < GTNAMachines2.INFINITE_INPUT_BUSES.length && GTNAMachines2.INFINITE_INPUT_BUSES[i] != null) {
                add("block.gtna.infinite_input_bus_" + tierLower, coloredTierName + " Infinite Input Bus");
            }
            if (i < GTNAMachines2.INFINITE_INPUT_HATCHES.length && GTNAMachines2.INFINITE_INPUT_HATCHES[i] != null) {
                add("block.gtna.infinite_input_hatch_" + tierLower, coloredTierName + " Infinite Input Hatch");
            }
            if (i < GTNAMachines2.OUTPUT_BOOST_ITEM_BUSES.length && GTNAMachines2.OUTPUT_BOOST_ITEM_BUSES[i] != null) {
                add("block.gtna.output_boost_item_bus_" + tierLower, coloredTierName + " Output Boost Item Bus");
            }
            if (i < GTNAMachines2.OUTPUT_BOOST_FLUID_HATCHES.length &&
                    GTNAMachines2.OUTPUT_BOOST_FLUID_HATCHES[i] != null) {
                add("block.gtna.output_boost_fluid_hatch_" + tierLower, coloredTierName + " Output Boost Fluid Hatch");
            }
        }
    }

    private String getColorCode(ChatFormatting formatting) {
        if (formatting == null) return "§f";
        return switch (formatting) {
            case BLACK -> "§0";
            case DARK_BLUE -> "§1";
            case DARK_GREEN -> "§2";
            case DARK_AQUA -> "§3";
            case DARK_RED -> "§4";
            case DARK_PURPLE -> "§5";
            case GOLD -> "§6";
            case GRAY -> "§7";
            case DARK_GRAY -> "§8";
            case BLUE -> "§9";
            case GREEN -> "§a";
            case AQUA -> "§b";
            case RED -> "§c";
            case LIGHT_PURPLE -> "§d";
            case YELLOW -> "§e";
            case WHITE -> "§f";
            case OBFUSCATED -> "§k";
            case BOLD -> "§l";
            case STRIKETHROUGH -> "§m";
            case UNDERLINE -> "§n";
            case ITALIC -> "§o";
            case RESET -> "§r";
        };
    }
}
