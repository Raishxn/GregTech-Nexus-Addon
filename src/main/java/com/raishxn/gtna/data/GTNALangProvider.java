package com.raishxn.gtna.data;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;

import net.minecraft.ChatFormatting;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
            GTNATagPrefix.singularity,
            GTNATagPrefix.brick,
            GTNATagPrefix.roughBlank,
            GTNATagPrefix.flake
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

    /**
     * Registers a Steam Elevator module's block name, auto-inserted tooltip and numbered GTNL lines
     * for its tier I/II/III items (ids {@code baseId_i/_ii/_iii}) from a single shared set of lines.
     */
    private void elevatorModuleTiers(String baseId, String name, String description, String... lines) {
        String[] suffixes = { "_i", "_ii", "_iii" };
        String[] romans = { " I", " II", " III" };
        for (int tier = 0; tier < suffixes.length; tier++) {
            String id = baseId + suffixes[tier];
            add("block.gtna." + id, name + romans[tier]);
            add("gtna.machine." + id + ".tooltip", description);
            for (int i = 0; i < lines.length; i++) {
                add("gtna.machine." + id + ".tooltip." + (i + 1), lines[i]);
            }
        }
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
        add(GTNATagPrefix.brick.getUnlocalizedName(), "Brick");
        add(GTNATagPrefix.roughBlank.getUnlocalizedName(), "Rough Blank");
        add(GTNATagPrefix.flake.getUnlocalizedName(), "Flake");
    }

    private void addStaticTranslations() {
        // --- Ported-content attribution (appended to machine tooltips by GTNASources) ---
        add("gtna.tooltip.source", "Source: %s");
        add("gtna.tooltip.machine_type", "Machine Type: %s");
        add("gtna.source.gto", "GregTech Odyssey (GTO)");
        add("gtna.source.gtnl", "GT: Not Leisure (GTNL)");
        add("gtna.source.gtnh", "GT: New Horizons (GTNH)");
        add("gtna.source.tst", "Twist Space Technology (TST)");
        add("gtna.source.gtl", "GregTech Leisure (GTL)");
        add("gtna.source.gtlcore", "GTLCore");
        add("gtna.source.gtlsupb", "GTLsupb");
        add("gtna.source.gtoepp", "GTO Extended Platform Presets");
        add("gtna.source.gtmthings", "GTMThings");

        // --- Multiple-recipes machine UI (previously hardcoded literals) ---
        add("gtna.multiblock.max_eut", "Max EU/t: %s (%s)");
        add("gtna.multiblock.parallels", "Parallels: %s");
        add("gtna.multiblock.overclock_hatch", "Overclock Hatch: %s");
        add("gtna.multiblock.overclock_hatch.value", "%.2fx duration per 4x EU");
        add("gtna.multiblock.accelerate_hatch", "Accelerate Hatch: %s");
        add("gtna.multiblock.accelerate_hatch.value", "%.2fx Duration");
        add("gtna.multiblock.output_boost_hatch", "Output Boost Hatch: %s");
        add("gtna.multiblock.output_boost_hatch.value", "%dx Outputs");
        add("gtna.multiblock.active_threads", "Active Threads: %s");
        add("gtna.multiblock.idle", "Idle - Waiting for inputs...");
        add("gtna.multiblock.thread_line", "Thread %s: %s%s");
        add("gtna.multiblock.output_line", " -> %s x%s%s");
        add("gtna.multiblock.unknown", "Unknown");
        add("gtna.multiblock.steam.high_pressure",
                "High pressure mode active: 2x processing speed, 2x steam consumption");
        add("gtna.tooltip.steam.high_pressure",
                "High pressure mode doubles processing speed and steam consumption");

        add("material.gtna.aluminium_bronze", "Aluminium Bronze");
        add("material.gtna.end_steel", "EndSteel");
        add("material.gtna.indalloy_140", "Indalloy 140");
        add("material.gtna.trinaquadalloy", "Trinaquadalloy");
        add("material.gtna.mar_m_200_steel", "Mar-M200 Steel");
        add("material.gtna.fall_king", "FallKing");
        add("material.gtna.cobalt_oxide", "Cobalt Oxide");
        add("material.gtna.lithium_oxide", "Lithium Oxide");
        add("material.gtna.zirconium_oxide", "Zirconium Oxide");
        add("material.gtna.zirconia_ceramic", "Zirconia Ceramic");
        // --- Hatches Translations (UPDATED) ---

        // Accelerate Hatch
        add("gtna.machine.accelerate_hatch.main_function",
                "Main Function: reduces recipe duration on top of the normal overclock.");
        add("gtna.machine.accelerate_hatch.range",
                "Default duration: %s of the original - adjust it in the hatch UI, up to 100%% (no effect).");
        add("gtna.machine.accelerate_hatch.weakness",
                "Penalty: +20%% duration per tier the RECIPE is above this hatch. A low-tier recipe in a high-tier machine is NOT penalized.");
        add("gtna.machine.accelerate_hatch.compat",
                "Works in any electric multiblock that accepts the hatch (GTCEu and GTNA).");

        // Thread Hatch
        add("gtna.machine.thread_hatch.tooltip",
                "Provides +%s threads so the machine can process different recipes at the same time.");
        add("gtna.machine.thread_hatch.range", "Adjustable in the hatch UI (0..%s).");
        add("gtna.machine.thread_hatch.requires",
                "Requires a machine on the multiple-recipes base (currently the Duration Tester and KubeJS machines).");

        // Overclock Hatch
        add("gtna.machine.overclock_hatch.main_function", "Main Function: improves the machine's overclock curve.");
        add("gtna.machine.overclock_hatch.not_installed",
                "Without this hatch, every 4x EU/t overclock uses the standard 50%% duration factor.");
        add("gtna.machine.overclock_hatch.installed",
                "With this hatch, every 4x EU/t overclock reduces duration to %s - adjust it in the hatch UI, up to 100%%.");
        add("gtna.machine.overclock_hatch.desc",
                "Changes the overclock scaling itself; it is not a final duration multiplier like the Accelerate Hatch.");
        add("gtna.machine.overclock_hatch.note",
                "Note: at UV the factor equals the standard overclock, so a UV hatch gives no gain - the gain starts at UHV.");
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
        // Universal Factory (GTLsupb port)
        add("block.gtna.universal_factory_casing", "Universal Factory Casing");
        add("block.gtna.universal_factory", "Universal Factory");
        // Large steam casings (GTNL port; textures from Modernity-GTNH)
        add("block.gtna.industrial_steam_casing", "Industrial Steam Casing");
        add("block.gtna.advanced_industrial_steam_casing", "Advanced Industrial Steam Casing");
        add("gtna.machine.universal_factory.tooltip.0",
                "Processes many different recipes at once with cross-recipe parallelism and threads.");
        add("gtna.machine.universal_factory.tooltip.1",
                "Threads and parallel scale with the operating voltage tier; idle time cools the machine down.");
        add("gtna.machine.universal_factory.tooltip.2",
                "Warmup raises parallel while it keeps running (up to a maximum); past the overload time it stays maxed.");
        add("gtna.machine.universal_factory.tooltip.3",
                "Batch multiplier and AUTO batch are set in the machine UI. Accepts Thread / Accelerate / Overclock / Output Boost hatches.");
        add("gtna.machine.universal_factory.thermal_status", "Thermal status: %s / %s (warmup %sx)");
        add("gtna.machine.universal_factory.overload_active", "OVERLOAD ACTIVE - maximum warmup");
        add("gtna.machine.universal_factory.dynamic_threads", "Dynamic threads: %s");
        add("gtna.machine.batch_multiplier", "Batch multiplier: %s");

        // Integrated Ore Processors (GTLCore port, LGPLv3)
        add("block.gtna.integrated_ore_processor", "Integrated Ore Processor");
        add("block.gtna.advanced_integrated_ore_processor", "Advanced Integrated Ore Processor");
        add("gtna.ore_processing", "Integrated Ore Processing");
        add("gtna.machine.integrated_ore_processor.tooltip.0", "Complete ore processing in one step");
        add("gtna.machine.integrated_ore_processor.tooltip.1", "Circuit 1: Grinding-Grinding-Centrifuge");
        add("gtna.machine.integrated_ore_processor.tooltip.2",
                "Circuit 2: Grinding-Ore Wash-Thermal Centrifuge-Grinding");
        add("gtna.machine.integrated_ore_processor.tooltip.3", "Circuit 3: Grinding-Ore Wash-Grinding-Centrifuge");
        add("gtna.machine.integrated_ore_processor.tooltip.4", "Circuit 4: Grinding-Ore Wash-Sifting-Centrifuge");
        add("gtna.machine.integrated_ore_processor.tooltip.5",
                "Circuit 5: Grinding-Chemical Bath-Thermal Centrifuge-Grinding");
        add("gtna.machine.integrated_ore_processor.tooltip.6", "Circuit 6: Grinding-Chemical Bath-Grinding-Centrifuge");
        add("gtna.machine.integrated_ore_processor.tooltip.7", "Circuit 7: Grinding-Chemical Bath-Sifting-Centrifuge");
        add("gtna.machine.advanced_integrated_ore_processor.tooltip.0", "Max Parallel: 2147483647");
        add("gtna.machine.advanced_integrated_ore_processor.laser", "Only accepts Laser Energy Hatches.");
        add("gtna.machine.advanced_integrated_ore_processor.multiple_recipes",
                "Processes multiple different recipes at the same time.");

        // Primitive Stone Furnace (GTLsupb port)
        add("block.gtna.primitive_stone_furnace", "Primitive Stone Furnace");
        add("gtna.machine.primitive_stone_furnace.tooltip.0",
                "A primitive furnace carved from stone: smelts like a furnace, with no energy required.");
        add("gtna.machine.primitive_stone_furnace.tooltip.1",
                "Feed items in and take the smelted results out; no power hatch is needed.");

        add("block.gtna.industrial_platform_deployment_tools", "Industrial Platform Deployment Tools");
        add("gtna.machine.industrial_platform_deployment_tools.tooltip.0",
                "Deploys prefabricated platform and factory presets directly into the world");
        add("gtna.machine.industrial_platform_deployment_tools.tooltip.1",
                "Consumes hydraulic deployment materials from its internal inventory");

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
        add("block.gtna.me_pattern_buffer_proxy", "ME Pattern Buffer Proxy");
        add("block.gtna.me_craft_pattern_hatch", "Nexus Craft Pattern Hatch");
        add("block.gtna.crafting_cpu_interface", "Crafting CPU Interface");
        add("block.gtna.nexus_me_hypercore", "Nexus ME Hypercore");
        add("item.gtna.infinite_cell_component", "Infinite Cell Component");
        add("gtna.ae2.cpu.nexus_hypercore", "N-ME CPU");
        add("gtna.machine.nexus_me_hypercore.ui.title", "Nexus ME Hypercore");
        add("gtna.machine.nexus_me_hypercore.ui.tier", "Tier: ");
        add("gtna.machine.nexus_me_hypercore.ui.modules", "Installed Modules: ");
        add("gtna.machine.nexus_me_hypercore.ui.storage", "Storage: ");
        add("gtna.machine.nexus_me_hypercore.ui.coprocessors", "Co-Processors: ");
        add("gtna.machine.nexus_me_hypercore.ui.threads", "Threads: ");
        add("gtna.machine.nexus_me_hypercore.ui.transcendent", "Transcendent Mode: ");
        add("gtna.machine.nexus_me_hypercore.ui.on", "Active");
        add("gtna.machine.nexus_me_hypercore.ui.off", "Inactive");
        add("gtna.machine.pattern_buffer.tooltip", "AE2 pattern buffer with per-slot GTNA specialization");
        add("gtna.machine.pattern_buffer.slots", "Pattern Slots: %s");
        add("gtna.machine.pattern_buffer.break_persist", "Stored patterns and slot data are preserved on block drop");
        add("gtna.machine.pattern_buffer.specialization_pending",
                "Middle-click a pattern slot to edit item, fluid, circuit, and mode specialization");
        add("gtna.machine.pattern_buffer.middle_click_hint", "Middle-click to configure this pattern slot");
        add("gtna.machine.pattern_buffer.selected_slot", "Editing Slot %s");
        add("gtna.machine.pattern_buffer.no_slot_selected", "No slot selected");
        add("gtna.machine.pattern_buffer.cached_recipe_short", "Recipe: %s");
        add("gtna.machine.pattern_buffer.derived_mode_short", "Mode: %s");
        add("gtna.machine.pattern_buffer.select_slot_hint", "Select a pattern slot");
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
        add("gtna.machine.pattern_buffer.mode.all", "All Modes");
        add("gtna.machine.pattern_buffer.buffer_mode.tooltip",
                "Limits this buffer to a single recipe type");
        add("gtna.machine.pattern_buffer.mode_button.tooltip",
                "Click to cycle the preferred multiblock mode for this slot");
        add("gtna.machine.pattern_buffer.mode_button.current", "Selected Mode: %s");
        add("gtna.machine.pattern_buffer.mode_button.derived", "Detected Mode: %s");
        add("gtna.machine.pattern_buffer.no_circuit", "No configured circuit");
        add("gtna.machine.pattern_buffer.previous_page", "Previous pattern page");
        add("gtna.machine.pattern_buffer.next_page", "Next pattern page");
        add("gtna.machine.pattern_buffer.back", "Back to patterns");
        add("gtna.machine.pattern_buffer.clear_machine_recipe_cache", "Clear Machine Recipe Cache");
        add("gtna.machine.pattern_buffer.clear_machine_recipe_cache.tooltip",
                "Clear all recipe lookup caches in this buffer without changing encoded patterns");
        add("gtna.machine.pattern_buffer.clear_pattern_recipe_cache", "Clear Recipe in Pattern");
        add("gtna.machine.pattern_buffer.clear_pattern_recipe_cache.tooltip",
                "Clear the recipe recorded in this pattern without changing its inputs or outputs");
        add("gtna.machine.pattern_buffer.embedded_circuit", "Embedded Circuit (all patterns)");
        add("gtna.machine.pattern_buffer.embed_circuit", "Embed Circuit");
        add("gtna.machine.pattern_buffer.remove_circuits", "Remove Circuits");
        add("gtna.machine.pattern_buffer.skip_existing.on", "Skip Existing: ON");
        add("gtna.machine.pattern_buffer.skip_existing.off", "Skip Existing: OFF");
        add("gtna.machine.pattern_buffer.skip_existing.tooltip",
                "ON keeps patterns that already carry a circuit untouched when embedding");
        add("gtna.machine.pattern_buffer.cache_section", "Recipe Cache");
        add("gtna.machine.pattern_buffer.tools", "Buffer Tools");
        add("gtna.machine.pattern_buffer.tools.tooltip",
                "Recipe cache maintenance and encoded-pattern circuit tooling for the whole buffer");
        add("item.gtna.pattern_buffer_copy.tooltip.snapshot",
                "Sneak-right-click a Pattern Buffer to snapshot its configuration");
        add("item.gtna.pattern_buffer_copy.tooltip.apply",
                "Right-click another Pattern Buffer to paste the configuration");
        add("item.gtna.pattern_buffer_cut.tooltip.snapshot",
                "Sneak-right-click a Pattern Buffer to cut its configuration out");
        add("item.gtna.pattern_buffer_cut.tooltip.apply",
                "Right-click another Pattern Buffer to paste the configuration");
        add("gtna.machine.pattern_buffer.copy.copied", "Pattern buffer configuration copied");
        add("gtna.machine.pattern_buffer.copy.cut", "Pattern buffer configuration cut");
        add("gtna.machine.pattern_buffer.copy.empty", "No buffer configuration stored on this card");
        add("gtna.machine.pattern_buffer.copy.pasted", "Pasted %s pattern(s) into the buffer");
        add("gtna.jade.pattern_buffer.proxies", "Bound Proxies: %s");
        add("gtna.machine.pattern_buffer.recipe_cached", "Recipe cached for this slot");
        add("gtna.machine.pattern_buffer.cache_toggle.on", "Recipe Cache: ON (click to disable)");
        add("gtna.machine.pattern_buffer.cache_toggle.off", "Recipe Cache: OFF (click to enable)");
        add("gtna.machine.pattern_buffer.cache_toggle.tooltip",
                "Caching remembers the resolved recipe per slot; disable to always resolve fresh");
        add("gtna.machine.pattern_buffer.keep_byproduct", "Keep Byproducts: ");
        add("gtna.machine.pattern_buffer.toggle_yes", "ON");
        add("gtna.machine.pattern_buffer.toggle_no", "OFF");
        add("gtna.machine.pattern_buffer.proxy", "ME Pattern Buffer Proxy");
        add("gtna.machine.pattern_buffer.proxy.tooltip",
                "Lets another multiblock use the patterns stored in a distant ME Pattern Buffer");
        add("gtna.machine.pattern_buffer.proxy.binding",
                "Bind: Shift-right-click the buffer with a Data Stick, then right-click this part");
        add("gtna.machine.pattern_buffer.proxy.range_note",
                "Same dimension only; the buffer chunk must be loaded");
        add("gtna.machine.pattern_buffer.proxy_bound", "Pattern Buffer bound to this proxy");
        add("gtna.machine.pattern_buffer.catalyst_item_field", "Catalyst Items (not consumed)");
        add("gtna.machine.pattern_buffer.catalyst_fluid_field", "Catalyst Fluids (not consumed)");
        add("gtna.machine.pattern_buffer.terminal_visibility", "Terminal: ");
        add("gtna.machine.pattern_buffer.terminal_hidden", "Hidden from Pattern Access Terminal");
        add("gtna.machine.pattern_buffer.terminal_visible", "Visible in Pattern Access Terminal");
        add("gtna.machine.pattern_buffer.clear_specialization", "Clear Spec");
        add("gtna.machine.pattern_buffer.clear_cache", "Clear Cache");
        add("gtna.machine.pattern_buffer.refund_slot", "Refund");
        add("gtna.machine.craft_pattern_hatch.tooltip", "Dedicated AE2 crafting hatch for the Nexus Assembly Forge");
        add("gtna.machine.craft_pattern_hatch.slots", "Pattern Slots: %s");
        add("gtna.machine.craft_pattern_hatch.patterns",
                "Accepts encoded crafting patterns only and registers them to the AE2 network");
        add("gtna.machine.craft_pattern_hatch.cheat",
                "Queues crafted outputs directly for the controller to materialize");
        add("gtna.machine.crafting_cpu_interface.tooltip",
                "Connects the Nexus ME Hypercore to the AE2 crafting CPU network");
        add("gtna.machine.crafting_cpu_interface.network",
                "The Nexus ME Hypercore structure requires exactly one interface");
        add("item.gtna.pattern_buffer_upgrade_21", "Pattern Buffer Expansion Card");
        add("item.gtna.pattern_buffer_upgrade_32", "Pattern Buffer Precision Card");
        add("item.gtna.pattern_buffer_upgrade_72", "Pattern Buffer Ascension Card");
        add("item.gtna.infinite_steam_singleblock_cover", "Infinite Steam Singleblock Cover");
        add("item.gtna.infinite_electric_singleblock_cover", "Infinite Electric Singleblock Cover");
        add("item.gtna.pattern_buffer_upgrader.tooltip.use",
                "Right-click a lower tier GTNA Pattern Buffer to upgrade it");
        add("item.gtna.pattern_buffer_upgrader.tooltip.keep_data",
                "Preserves encoded patterns, internal slot data, and slot specialization");
        add("item.gtna.primitive_mans_spacetime_distortion_device",
                "Primitive Man's SpaceTime Distortion Device");
        add("item.gtna.primitive_mans_spacetime_distortion_device.tooltip", "Anyway...");

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
        add("gtna.terminal.config.matrix_storage_module", "§dMatrix Storage Modules");
        add("gtna.terminal.config.matrix_crafting_module", "§dMatrix Crafting Modules");
        add("gtna.terminal.config.me_storage_access", "§dME Storage Access");

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
        add("gtna.tooltip.large_steam_crusher.speed", "Speed: 100% faster than a standard Macerator");
        add("gtna.tooltip.large_steam_crusher.steam", "Steam Consumption: 80% of original");
        add("gtna.tooltip.large_steam_crusher.parallel", "Process up to 128 items at once");
        add("gtna.registry.add", "Added by GregTech Nexus Addon");
        add("gtna.multiblock.parallel_amount", "Parallels: %s");
        add("block.gtna.huge_steam_input_bus", "Huge Steam Input Bus");
        add("block.gtna.huge_steam_output_bus", "Huge Steam Output Bus");
        add("block.gtna.infinite_steam_input_bus", "Infinite Steam Input Bus");
        add("block.gtna.output_boost_steam_output_bus", "Output Boost Steam Output Bus");
        add("gtna.tooltip.huge_steam_bus", "Input Bus with a lot of items capacity. around 3654 itens.");
        add("gtna.machine.wireless_steam_hatch.tooltip", "Steam Production: %s L/t");
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
        add("block.gtna.high_strength_concrete", "High Strength Concrete");
        add("block.gtna.cobalt_oxide_ceramic_strong_thermally_conductive_mechanical_block",
                "Cobalt Oxide Ceramic Strong Thermally Conductive Mechanical Block");
        add("block.gtna.lithium_oxide_ceramic_heat_resistant_shock_resistant_mechanical_cube",
                "Lithium Oxide Ceramic Heat Resistant Shock Resistant Mechanical Cube");
        add("block.gtna.abs_black_casing", "Black ABS Plastic Mechanical Casing");
        add("block.gtna.abs_blue_casing", "Blue ABS Plastic Mechanical Casing");
        add("block.gtna.abs_brown_casing", "Brown ABS Plastic Mechanical Casing");
        add("block.gtna.abs_green_casing", "Green ABS Plastic Mechanical Casing");
        add("block.gtna.abs_grey_casing", "Gray ABS Plastic Mechanical Casing");
        add("block.gtna.abs_lime_casing", "Lime ABS Plastic Mechanical Casing");
        add("block.gtna.abs_orange_casing", "Orange ABS Plastic Mechanical Casing");
        add("block.gtna.abs_red_casing", "Red ABS Plastic Mechanical Casing");
        add("block.gtna.abs_white_casing", "White ABS Plastic Mechanical Casing");
        add("block.gtna.abs_yellow_casing", "Yellow ABS Plastic Mechanical Casing");
        add("block.gtna.abs_cyan_casing", "Cyan ABS Plastic Mechanical Casing");
        add("block.gtna.abs_magenta_casing", "Magenta ABS Plastic Mechanical Casing");
        add("block.gtna.abs_pink_casing", "Pink ABS Plastic Mechanical Casing");
        add("block.gtna.abs_purple_casing", "Purple ABS Plastic Mechanical Casing");
        add("block.gtna.abs_light_bull_casing", "Light Blue ABS Plastic Mechanical Casing");
        add("block.gtna.abs_light_grey_casing", "Light Gray ABS Plastic Mechanical Casing");
        add("block.gtna.t1_me_storage_core", "T1 Matrix Storage Module");
        add("block.gtna.t2_me_storage_core", "T2 Matrix Storage Module");
        add("block.gtna.t3_me_storage_core", "T3 Matrix Storage Module");
        add("block.gtna.t4_me_storage_core", "T4 Matrix Storage Module");
        add("block.gtna.t5_me_storage_core", "T5 Matrix Storage Module");
        add("block.gtna.t1_crafting_storage_core", "T1 Matrix Crafting Module");
        add("block.gtna.t2_crafting_storage_core", "T2 Matrix Crafting Module");
        add("block.gtna.t3_crafting_storage_core", "T3 Matrix Crafting Module");
        add("block.gtna.t4_crafting_storage_core", "T4 Matrix Crafting Module");
        add("block.gtna.t5_crafting_storage_core", "T5 Matrix Crafting Module");
        add("gtna.machine.me_storage_access_hatch.tooltip", "Connects ME Storage to an AE2 network.");
        add("gtna.machine.me_big_storage_access_hatch.tooltip",
                "Connects ME Storage to an AE2 network with BigInteger storage mode.");
        add("gtna.machine.me_io_port_hatch.tooltip", "Connects ME Storage to an AE2 network through IO Port mode.");
        add("gtna.machine.me_storage_access_hatch.network",
                "The ME Storage multiblock accepts exactly one of these access hatches.");
        add("gtna.machine.me_storage_access_hatch.mode", "Storage Access");
        add("gtna.machine.me_big_storage_access_hatch.mode", "Big Storage Access");
        add("gtna.machine.me_io_port_hatch.mode", "IO Port");
        add("gtna.machine.me_storage.unformed", "Form the Matrix Storage structure to mount AE2 storage.");
        add("gtna.machine.me_storage.title", "ME Storage");
        add("gtna.machine.me_storage.no_access",
                "Missing ME Storage Access Hatch, ME Big Storage Access Hatch, or ME IO Port Hatch.");
        add("gtna.machine.me_storage.access", "%s: %s");
        add("gtna.machine.me_storage.capacity", "Capacity: %s");
        add("gtna.machine.me_storage.used", "Used: %s / Types: %s");
        add("gtna.machine.me_storage.infinite_status", "Infinite Cell Components: %s/64 - %s");
        add("gtna.machine.me_storage.infinite_cell_slot", "Infinite Cell Component Slot");
        add("block.gtna.naquadah_borosilicate_glass", "Naquadah Borosilicate Glass");
        add("block.gtna.magtech_casing", "Magtech Casing");
        add("block.gtna.process_machine_casing", "Process Machine Casing");
        add("block.gtna.compressor_controller_casing", "Compressor Controller Casing");
        add("block.gtna.extreme_density_casing", "Extreme Density Casing");
        add("block.gtna.steam_assembly_block", "Steam Assembly Block");
        add("block.gtna.brass_reinforced_wooden_casing", "Brass Reinforced Wooden Casing");
        add("block.gtna.solar_heat_collector_pipe_casing", "Solar Heat Collector Pipe Casing");
        add("block.gtna.annihilate_generator", "Artificial Star");
        add("block.gtna.eye_of_harmony", "Eye of Harmony");
        add("block.gtna.eye_of_wood", "Eye of Wood");
        add("block.gtna.nexus_molecular_forge", "Nexus Assembly Forge");
        add("block.gtna.me_storage", "ME Storage");
        add("gtceu.annihilate_generator", "Annihilation Generator");
        add("gtna.cosmos_simulation", "Cosmos Simulation");
        add("gtna.machine.artificial_star.output", "Supports Laser or Wireless Dynamo output.");
        add("gtna.machine.nexus_molecular_forge.tooltip.0",
                "Ultra-fast AE2 mass crafting forge.");
        add("gtna.machine.nexus_molecular_forge.tooltip.1",
                "Queues jobs from Nexus Craft Pattern Hatches and materializes them in giant batches.");
        add("gtna.machine.nexus_molecular_forge.tooltip.2",
                "Parallel crafting follows the AE2 CPU and is optimized for extreme throughput.");
        add("gtna.machine.nexus_molecular_forge.tooltip.3",
                "Each operation materializes every queued craft output at once.");
        add("gtna.machine.nexus_molecular_forge.tooltip.4", "Queued crafts do not consume physical ingredients.");
        add("gtna.machine.nexus_molecular_forge.tooltip.5",
                "Power Cost: 1 EU per crafted item, compressed into batch EU/t.");
        add("gtna.machine.nexus_molecular_forge.tooltip.6",
                "HUD shows pattern hatches, loaded patterns, queued outputs, active batch, and forge ceiling.");
        add("gtna.machine.eye_of_harmony.tooltip.0", "Creates a miniature universe and extracts its resources.");
        add("gtna.machine.eye_of_harmony.tooltip.1", "Startup power comes directly from the GTNA wireless network.");
        add("gtna.machine.eye_of_harmony.tooltip.2", "Bind with a Data Stick to swap the network owner.");
        add("gtna.machine.eye_of_harmony.tooltip.3", "Uses circuits 1-4 to choose 0-3 special overclocks.");
        add("gtna.machine.eye_of_harmony.tooltip.4",
                "Requires 1024 buckets each of Hydrogen and Helium before startup.");
        add("gtna.machine.eye_of_harmony.tooltip.5", "Consumes those gases internally in 100-bucket batches.");
        add("gtna.machine.eye_of_harmony.tooltip.6", "No conventional energy hatches are used here.");
        add("gtna.machine.eye_of_harmony.tooltip.7", "Outputs are handled through the regular item and fluid ports.");
        add("gtna.machine.eye_of_harmony.owner", "Network Owner: %s");
        add("gtna.machine.eye_of_harmony.network_eu", "Stored Network EU: %s");
        add("gtna.machine.eye_of_harmony.startup_eu", "Startup Energy: %s EU");
        add("gtna.machine.eye_of_harmony.hydrogen", "Hydrogen Storage: %s mB");
        add("gtna.machine.eye_of_harmony.helium", "Helium Storage: %s mB");
        add("gtna.machine.eye_of_harmony.rebound", "[GTNA] Eye of Harmony rebound to your network.");
        add("gtna.machine.eye_of_wood.tooltip.0",
                "Overworld-only ore condenser based on the original Twist Space Technology machine.");
        add("gtna.machine.eye_of_wood.tooltip.1",
                "Constantly consumes Water and Lava from input hatches and stores both fluids inside the machine.");
        add("gtna.machine.eye_of_wood.tooltip.2",
                "Peak success rate: 75% when stored Water and Lava are both exactly 256,000 mB.");
        add("gtna.machine.eye_of_wood.tooltip.3",
                "Success falls off as either stored fluid drifts away from the 256,000 mB target.");
        add("gtna.machine.eye_of_wood.tooltip.4",
                "Each run takes a fixed 60 seconds.");
        add("gtna.machine.eye_of_wood.tooltip.5",
                "Success outputs large Overworld ore bundles.");
        add("gtna.machine.eye_of_wood.tooltip.6",
                "Failure vents a huge amount of Steam, up to 270,000,000 mB.");
        add("gtna.machine.eye_of_wood.tooltip.7",
                "Structure: iconic 33x33x33 Eye of Wood using bricks, planks, bookshelves, lapis, cracked stone bricks, and steel casings.");
        add("gtna.machine.eye_of_wood.water", "Stored Water: %s / %s mB");
        add("gtna.machine.eye_of_wood.lava", "Stored Lava: %s / %s mB");
        add("gtna.machine.eye_of_wood.chance", "Success Chance: %s / 10000");
        add("gtna.machine.eye_of_wood.last_result", "Last Roll: %s");
        add("gtna.machine.eye_of_wood.result.success", "Success");
        add("gtna.machine.eye_of_wood.result.fail", "Steam Vent");
        add("gtna.machine.wireless_steam_output.tooltip_desc", "Sends Steam wirelessly to your Global Network.");
        add("gtna.machine.wireless_steam_output.tooltip_usage", "Usage: Place on Boilers to export Steam.");
        add("gtna.machine.wireless_steam_input.tooltip_desc", "Receives Steam wirelessly from your Global Network.");
        add("gtna.machine.wireless_steam.transfer_rate", "Transfer Rate: %s mB/t");
        add("gtna.machine.wireless_steam.transfer_rate.unlimited",
                "Transfer Rate: Unlimited (moves the whole buffer every tick)");
        add("block.gtna.large_steam_furnace", "Large Steam Furnace");
        add("gtna.tooltip.large_steam_furnace.desc", "An industrial-grade steam smelting facility.");
        add("gtna.tooltip.large_steam_furnace.speed", "Speed: 900% faster than a standard Steam Furnace.");
        add("gtna.tooltip.large_steam_furnace.efficiency", "Efficiency: Consumes only 50% of the required Steam.");
        add("gtna.tooltip.large_steam_furnace.parallel", "Parallelism: Processes up to 128 items simultaneously.");
        add("gtna.tooltip.large_steam_furnace.structure",
                "Structure: GTOCore large steam furnace shell. Check JEI for details.");
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
        add("block.gtna.large_steam_circuit_assembler", "Large Steam Circuit Assembler");
        add("gtna.tooltip.large_steam_circuit_assembler.desc",
                "Steam-era circuit assembly line with engraved-circuit targeting.");
        add("gtna.tooltip.large_steam_circuit_assembler.mode",
                "Supports a multiply mode after engraving the target circuit.");
        add("gtna.tooltip.large_steam_circuit_assembler.parallel", "Parallel: Processes up to 64 recipes.");
        add("gtna.tooltip.large_steam_circuit_assembler.structure", "Structure: 3x4x10 steam assembly tunnel.");
        add("gtna.machine.large_steam_circuit_assembler.engrave_circuit", "Engrave Circuit");
        add("gtna.machine.large_steam_circuit_assembler.circuit", "Engraved Circuit: %s");
        add("gtna.machine.large_steam_circuit_assembler.remaining", "Circuits Needed: %s");
        add("gtna.machine.large_steam_circuit_assembler.multiply_mode", "Multiply Mode: %s");
        add("block.gtna.large_steam_mixer", "Large Steam Mixer");
        add("gtna.tooltip.large_steam_mixer.desc", "Bulk steam mixing for dusts and fluids.");
        add("gtna.tooltip.large_steam_mixer.speed", "Speed: 100% faster than singleblock.");
        add("gtna.tooltip.large_steam_mixer.parallel", "Parallel: Processes up to 64 recipes.");
        add("gtna.tooltip.large_steam_mixer.structure", "Structure: 9x7x9 steam mixing chamber.");
        add("block.gtna.large_steam_centrifuge", "Large Steam Centrifuge");
        add("gtna.tooltip.large_steam_centrifuge.desc", "High-throughput steam centrifuge with fluid support.");
        add("gtna.tooltip.large_steam_centrifuge.speed", "Speed: 250% faster than singleblock.");
        add("gtna.tooltip.large_steam_centrifuge.parallel", "Parallel: Processes up to 64 recipes.");
        add("gtna.tooltip.large_steam_centrifuge.structure", "Structure: 11x5x11 reinforced steam centrifuge.");
        add("block.gtna.large_steam_thermal_centrifuge", "Large Steam Thermal Centrifuge");
        add("gtna.tooltip.large_steam_thermal_centrifuge.desc",
                "Firebox-heated thermal centrifuge for heavy steam processing.");
        add("gtna.tooltip.large_steam_thermal_centrifuge.speed", "Speed: 200% faster than singleblock.");
        add("gtna.tooltip.large_steam_thermal_centrifuge.parallel", "Parallel: Processes up to 64 recipes.");
        add("gtna.tooltip.large_steam_thermal_centrifuge.structure",
                "Structure: 7x5x7 with bronze fireboxes and a rear muffler.");
        add("block.gtna.large_steam_bath", "Large Steam Bath");
        add("gtna.tooltip.large_steam_bath.desc", "Large steam chemical bath for early bulk washing.");
        add("gtna.tooltip.large_steam_bath.speed", "Speed: 100% faster than singleblock.");
        add("gtna.tooltip.large_steam_bath.parallel", "Parallel: Processes up to 64 recipes.");
        add("gtna.tooltip.large_steam_bath.structure",
                "Structure: 9x5x9 basin with glass walls and bronze pipe agitators.");
        add("block.gtna.primitive_distillation_tower", "Primitive Distillation Tower");
        add("gtna.tooltip.primitive_distillation_tower.desc",
                "Machine Type: Distillation Tower. Can only output 6 types of fluids.");
        add("gtna.tooltip.primitive_distillation_tower.parallel",
                "Consumes only 75% of the normal steam requirement. Can only process MV tier recipes or lower.");
        add("gtna.tooltip.primitive_distillation_tower.structure",
                "Structure: GT-Not-Leisure primitive tower: 3x3 steel firebox base, five hollow steel hull layers, and a closed top layer.");
        add("block.gtna.large_steam_lathe", "Large Steam Lathe");
        add("gtna.tooltip.large_steam_lathe.desc", "GT-Not-Leisure style steam lathe for bulk turning.");
        add("gtna.tooltip.large_steam_lathe.speed", "Speed: 100% faster than singleblock.");
        add("gtna.tooltip.large_steam_lathe.efficiency", "Efficiency: consumes 50% total steam per recipe.");
        add("gtna.tooltip.large_steam_lathe.parallel", "Parallel: Processes up to 16 recipes.");
        add("block.gtna.large_steam_cutting", "Large Steam Cutting Machine");
        add("gtna.tooltip.large_steam_cutting.desc", "GT-Not-Leisure style steam cutting machine.");
        add("gtna.tooltip.large_steam_cutting.speed", "Speed: 100% faster than singleblock.");
        add("gtna.tooltip.large_steam_cutting.efficiency", "Efficiency: consumes 50% total steam per recipe.");
        add("gtna.tooltip.large_steam_cutting.parallel", "Parallel: Processes up to 16 recipes.");
        add("block.gtna.large_steam_bending", "Large Steam Bending Machine");
        add("gtna.tooltip.large_steam_bending.desc", "GT-Not-Leisure style steam bending machine.");
        add("gtna.tooltip.large_steam_bending.speed", "Speed: 100% faster than singleblock.");
        add("gtna.tooltip.large_steam_bending.efficiency", "Efficiency: consumes 50% total steam per recipe.");
        add("gtna.tooltip.large_steam_bending.parallel", "Parallel: Processes up to 16 recipes.");
        add("block.gtna.large_steam_extruder", "Large Steam Extruder");
        add("gtna.tooltip.large_steam_extruder.desc", "GT-Not-Leisure style steam extruder.");
        add("gtna.tooltip.large_steam_extruder.speed", "Speed: 100% faster than singleblock.");
        add("gtna.tooltip.large_steam_extruder.efficiency", "Efficiency: consumes 50% total steam per recipe.");
        add("gtna.tooltip.large_steam_extruder.parallel", "Parallel: Processes up to 16 recipes.");
        add("block.gtna.large_steam_wiremill", "Large Steam Wiremill");
        add("gtna.tooltip.large_steam_wiremill.desc", "GT-Not-Leisure style steam wiremill.");
        add("gtna.tooltip.large_steam_wiremill.speed", "Speed: 100% faster than singleblock.");
        add("gtna.tooltip.large_steam_wiremill.efficiency", "Efficiency: consumes 50% total steam per recipe.");
        add("gtna.tooltip.large_steam_wiremill.parallel", "Parallel: Processes up to 16 recipes.");
        add("block.gtna.large_steam_sifter", "Large Steam Sifter");
        add("gtna.tooltip.large_steam_sifter.desc", "GT-Not-Leisure style steam sifter.");
        add("gtna.tooltip.large_steam_sifter.speed", "Speed: 100% faster than singleblock.");
        add("gtna.tooltip.large_steam_sifter.efficiency", "Efficiency: consumes 50% total steam per recipe.");
        add("gtna.tooltip.large_steam_sifter.parallel", "Parallel: Processes up to 16 recipes.");
        add("block.gtna.steam_lava_maker", "Steam Lava Maker");
        add("gtna.tooltip.steam_lava_maker.desc",
                "GT-Not-Leisure style steam lava maker: melts stone into lava.");
        add("gtna.tooltip.steam_lava_maker.speed", "Speed: 1 second per stone block.");
        add("gtna.tooltip.steam_lava_maker.efficiency", "Efficiency: runs on steam instead of EU.");
        add("gtna.tooltip.steam_lava_maker.parallel", "Parallel: Processes up to 16 recipes.");
        add("block.gtna.steam_item_vault", "Steam Item Vault");
        add("gtna.tooltip.steam_item_vault.desc",
                "GT-Not-Leisure style steam item vault: a very large item storage for the steam era.");
        add("gtna.tooltip.steam_item_vault.capacity", "Capacity: 256 types x 64,000 items each.");
        add("gtna.tooltip.steam_item_vault.access",
                "Access the stored items through the structure's item buses.");
        add("gtna.multiblock.vault.types", "Stored types: %s / %s");
        add("gtna.multiblock.vault.items", "Stored items: %s");
        add("block.gtna.large_steam_forming_press", "Large Steam Forming Press");
        add("gtna.tooltip.large_steam_forming_press.desc", "GT-Not-Leisure style steam forming press.");
        add("gtna.tooltip.large_steam_forming_press.speed", "Speed: 150% faster than singleblock.");
        add("gtna.tooltip.large_steam_forming_press.efficiency", "Efficiency: consumes 40% total steam per recipe.");
        add("gtna.tooltip.large_steam_forming_press.parallel", "Parallel: Processes up to 32 recipes.");
        add("block.gtna.steam_cactus_wonder", "Cactus Wonder");
        add("gtna.tooltip.steam_cactus_wonder.desc", "GT-Not-Leisure style cactus-fueled steam generator.");
        add("gtna.tooltip.steam_cactus_wonder.offer",
                "Burns cactus-era carbon fuels and returns their value as steam blessings.");
        add("gtna.tooltip.steam_cactus_wonder.fuel",
                "Fuels: charcoal/coal -> steam, coke -> superheated, coke block -> dense supercritical.");
        add("gtna.tooltip.steam_cactus_wonder.structure",
                "Requires fully grown cacti on the sand floor to form.");
        add("block.gtna.steam_cracking", "Steam Cracker");
        add("gtna.tooltip.steam_cracking.desc",
                "GT-Not-Leisure style steam cracker: thermally cracks sulfuric hydrocarbons.");
        add("gtna.tooltip.steam_cracking.cracking",
                "Runs the native GTCEu cracking recipes using integrated circuits 1-3.");
        add("gtna.tooltip.steam_cracking.parallel", "Parallel: 8 (bronze) / 16 (steel high pressure).");
        add("gtna.tooltip.steam_cracking.structure",
                "Structure: GTNL large steam cracking shell. Check JEI for details.");
        add("block.gtna.steam_mega_compressor", "Steam Supercompressor");
        add("gtna.tooltip.mega_steam_compressor.desc",
                "GT-Not-Leisure style supercompressor: impossible jets of steam compress material.");
        add("gtna.tooltip.mega_steam_compressor.parallel", "Parallel: processes up to 256 recipes.");
        add("gtna.tooltip.mega_steam_compressor.speed", "Speed: 100% faster than singleblock.");
        add("gtna.tooltip.mega_steam_compressor.structure",
                "Crafted inside the Steam Manufacturer from 64 large steam compressors.");
        add("block.gtna.large_steam_storage_tank", "Large Steam Storage Tank");
        add("gtna.tooltip.large_steam_storage_tank.desc", "A GTOCore-style industrial steam reservoir.");
        add("gtna.tooltip.large_steam_storage_tank.capacity", "Capacity: 120,000,000 mB of Steam.");
        add("gtna.tooltip.large_steam_storage_tank.structure",
                "Structure: 5x7x5 steam tank with industrial steam casings.");
        add("block.gtna.large_steam_solar_boiler", "Large Steam Solar Boiler");
        add("gtna.tooltip.large_steam_solar_boiler.desc", "Expandable solar steam field using solar boiling cells.");
        add("gtna.tooltip.large_steam_solar_boiler.expandable",
                "Structure expands backward and sideways as long as cells remain sunlit.");
        add("gtna.tooltip.large_steam_solar_boiler.production", "Steam Output scales with the number of sunlit cells.");
        add("gtna.tooltip.large_steam_solar_boiler.structure", "Structure: starts at 5x1x5 and expands horizontally.");
        add("gtna.machine.large_steam_solar_boiler.size", "Structure Size: %s x %s");
        add("gtna.machine.large_steam_solar_boiler.sunlit", "Sunlit Cells: %s");
        add("gtna.machine.large_steam_solar_boiler.production", "Steam Production: %s mB/s");
        add("gtna.machine.large_steam_solar_boiler.idle",
                "Not producing: night, rain, or no sunlit solar boiling cells.");
        add("block.gtna.dimensionally_transcendent_dirt_forge", "Dimensionally Transcendent Dirt Forge");
        add("gtna.tooltip.dimensionally_transcendent_dirt_forge.desc",
                "GTLCore's absurd primitive forge shell, repurposed for massive primitive blast throughput.");
        add("gtna.tooltip.dimensionally_transcendent_dirt_forge.parallel",
                "Parallel: Processes up to 524288 primitive blast recipes.");
        add("gtna.tooltip.dimensionally_transcendent_dirt_forge.structure",
                "Structure: GTLCore DTPF shell using primitive bricks, bricks, dirt, and stone bricks.");
        add("block.gtna.dimensionally_transcendent_steam_boiler", "Dimensionally Transcendent Steam Boiler");
        add("gtna.tooltip.dimensionally_transcendent_steam_boiler.desc",
                "Absurd late-game boiler that bends dimensional space into steam throughput.");
        add("gtna.tooltip.dimensionally_transcendent_steam_boiler.output", "Output: 4,096,000 L of steam per cycle.");
        add("gtna.tooltip.dimensionally_transcendent_steam_boiler.structure",
                "Structure: GTLCore DTPF shell with robust tungstensteel, heatproof casings, coils, and boiler pipes.");
        add("block.gtna.dimensionally_transcendent_steam_oven", "Dimensionally Transcendent Steam Oven");
        add("gtna.tooltip.dimensionally_transcendent_steam_oven.desc",
                "A steam oven pushed far beyond sane thermal engineering limits.");
        add("gtna.tooltip.dimensionally_transcendent_steam_oven.speed",
                "Speed: 9900% faster than a standard furnace recipe.");
        add("gtna.tooltip.dimensionally_transcendent_steam_oven.threads",
                "Threads: 2 fixed recipe threads, allowing two different furnace recipes at the same time.");
        add("gtna.tooltip.dimensionally_transcendent_steam_oven.parallel", "Parallel: Processes up to 524288 recipes.");
        add("gtna.tooltip.dimensionally_transcendent_steam_oven.structure",
                "Structure: GTLCore DTPF shell using bronze bricks, bricks, deepslate, and stone bricks.");
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
        add("gtna.lava_maker", "Lava Maker");
        add("gtna.cactus_wonder", "Cactus Wonder");
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
        add("config.gtna.option.wirelessSteamHud", "Wireless Steam HUD");
        add("config.gtna.option.wirelessSteamHudX", "Wireless Steam HUD X Position");
        add("config.gtna.option.wirelessSteamHudY", "Wireless Steam HUD Y Position");
        add("config.gtna.option.wirelessSteamHudHistorySeconds", "Wireless Steam HUD History Seconds");

        // Accelerate Hatch
        add("config.gtna.option.accelerateHatchMultiplier", "Accelerate Hatch Speed");
        add("config.gtna.option.accelerateHatchEnergyCost", "Accelerate Hatch Energy Cost");

        // Pattern Buffer mode auto-switch on GTCEu multiblocks
        add("config.gtna.option.bufferDrivenMachineMode", "Buffer Driven Machine Mode");

        // Wireless Steam
        add("config.gtna.option.wirelessSteamTransferRate", "Wireless Steam Transfer Rate");
        add("config.gtna.option.solarBoilerSteamPerCell", "Solar Boiler Steam Per Cell");
        add("config.gtna.option.bronzeInputBuffer", "Bronze Input Hatch Buffer");
        add("config.gtna.option.steelInputBuffer", "Steel Input Hatch Buffer");
        add("config.gtna.option.bronzeOutputBuffer", "Bronze Output Hatch Buffer");
        add("config.gtna.option.steelOutputBuffer", "Steel Output Hatch Buffer");

        // GTNL steam machine toggles
        add("config.gtna.option.steamCactusWonder", "Cactus Wonder");
        add("config.gtna.option.steamCracking", "Steam Cracker");
        add("config.gtna.option.megaSteamCompressor", "Steam Supercompressor");
        add("config.gtna.option.steamElevator", "Steam Elevator");
        add("config.gtna.option.steamElevatorModules", "Steam Elevator Modules");
        add("config.gtna.option.steamApiaryModule", "Steam Apiary Module");
        add("config.gtna.option.steamBeeBreedingModule", "Steam Bee Breeding Module");
        add("config.gtna.option.integratedOreProcessor", "Integrated Ore Processor");
        add("config.gtna.option.advancedIntegratedOreProcessor", "Advanced Integrated Ore Processor");

        // Steam Elevator + its modules (GTNL port)
        add("block.gtna.steam_elevator", "Steam Elevator");
        add("gtna.tooltip.steam_elevator.desc", "A modular steam-powered elevator.");
        add("gtna.tooltip.steam_elevator.modules",
                "Install up to 12 modules in the module slots: flight, weather, greenhouse, apiary, bee breeding, oil drill, entity crusher, ore processor, monster repellent, beacon.");
        add("gtna.tooltip.steam_elevator.teleport",
                "Set out button: opens the Ad Astra planet selection, like a rocket launch.");
        add("gtna.tooltip.steam_elevator.structure",
                "35x43x35; steel reinforced wood shell, steam compact pipe casing, solid steel machine casing.");
        add("gtna.machine.steam_elevator.steam", "Steam: %s mB");
        add("gtna.machine.steam_elevator.modules", "Modules: %s");
        add("gtna.machine.steam_elevator.steam_hatches", "Steam hatches: %s");
        add("gtna.machine.steam_elevator.no_modules", "No modules installed");
        add("gtna.machine.steam_elevator.set_out", "Set out");
        add("gtna.machine.steam_elevator.set_out.not_running", "The elevator must be running to set out.");

        add("gtna.machine.steam_elevator_module.tier", "Module tier: %s");
        add("gtna.machine.steam_elevator_module.upkeep", "Upkeep: %s mB/t | Own steam: %s mB");
        add("gtna.machine.steam_elevator_module.connected", "Connected to the elevator");
        add("gtna.machine.steam_elevator_module.working", "Working");
        add("gtna.machine.steam_elevator_module.not_working", "Not Working");
        add("gtna.machine.steam_elevator_module.disconnected", "Not connected");

        // Shared stat line shown on the module's own screen (not the item tooltip): the item
        // tooltips are the exact GTNL module lines, added per module below.
        add("gtna.machine.steam_elevator_module.upkeep", "Upkeep: %s mB/t | Own steam: %s mB");

        // Wireless steam network inspection command (/gtna steam)
        add("gtna.command.steam.balance", "§b[GTNA] §fWireless steam network §7(%s)§f: §b%s§f mB");
        add("gtna.command.steam.flow",
                "§b[GTNA] §7Flow since server start: §a+%s§7 mB in, §c-%s§7 mB out; §e%s§7 input hatch(es) with space");
        add("gtna.command.steam.hatches", "§b[GTNA] §fConnected wireless steam hatches: §e%s");
        add("gtna.command.steam.hatch_entry",
                "  §7- §f%s §7%s §f@ §7%s %s §7| §7tank §f%s§7/§f%s §7mB | §e%s §7mB/t | %s");
        add("gtna.command.steam.rate.unlimited", "unlimited");
        add("gtna.command.steam.no_hatches", "§b[GTNA] §7No connected wireless steam hatches.");
        add("gtna.command.steam.type.input", "Input");
        add("gtna.command.steam.type.output", "Output");
        add("gtna.command.steam.tier.bronze", "bronze");
        add("gtna.command.steam.tier.steel", "steel");
        add("gtna.command.steam.last.none", "§7no transfer yet");
        add("gtna.command.steam.last.push", "§apushed %s§7 mB (%s§7 t ago)");
        add("gtna.command.steam.last.pull", "§bpulled %s§7 mB (%s§7 t ago)");
        add("gtna.command.steam.not_player", "§c[GTNA] This command must be run by a player.");
        add("gtna.command.steam.added", "§b[GTNA] §fAdded §b%s§f mB; balance is now §b%s§f mB.");
        add("gtna.command.steam.set", "§b[GTNA] §fSet the balance to §b%s§f mB.");
        add("gtna.command.steam.add_failed", "§c[GTNA] Could not add that amount to the network.");

        // Wireless steam network HUD (client overlay, off by default; see ConfigHolder.Client)
        add("gtna.hud.wireless_steam.name", "Wireless Steam HUD");
        add("gtna.hud.wireless_steam.balance", "§bWireless Steam§7: §f%s §7mB");
        add("gtna.hud.wireless_steam.flow", "§7Flow: §a+%s§7 / §c-%s§7 mB/s");
        add("gtna.hud.wireless_steam.hatches", "§7Hatches: §f%s §7drain / §f%s §7feed");
        add("gtna.hud.editor.title", "HUD Editor");
        add("gtna.hud.editor.hint", "Drag the HUD to move it; the position is saved automatically. ESC closes.");
        add("gtna.hud.editor.toggle", "Wireless Steam HUD: %s");
        add("gtna.hud.editor.on", "ON");
        add("gtna.hud.editor.off", "OFF");
        add("gtna.machine.wireless_steam.hud.toggle", "Toggle the wireless steam HUD");
        add("gtna.machine.wireless_steam.hud.editor", "Right click to move the HUD");

        elevatorModuleTiers("steam_elevator_flight_module", "Steam Flight Module",
                "§a§lSteam-powered flight assistance.",
                "Adds flight module to the Steam Stairway",
                "Consumes steam to grant you the power of free flight",
                "Adjust overclock count to consume more steam and expand the range",
                "Effective range: 64 * module tier");

        elevatorModuleTiers("steam_elevator_weather_module", "Steam Weather Module",
                "§a§lControls the weather using steam.",
                "Adds weather changes module to the Steam Stairway",
                "Enter a specific material to switch weather, lasting 1 hour per tier");

        add("block.gtna.steam_elevator_greenhouse_module", "Steam Greenhouse Planting Module");
        add("gtna.machine.steam_elevator_greenhouse_module.tooltip", "§a§lFarmer’s Secret to Fortune.");
        add("gtna.machine.steam_elevator_greenhouse_module.tooltip.1",
                "Adds greenhouse module to the Steam Stairway");
        add("gtna.machine.steam_elevator_greenhouse_module.tooltip.2",
                "No more fertilizer or herbicide needed!");
        add("gtna.machine.steam_elevator_greenhouse_module.tooltip.3",
                "Consumes 16,000L of water per crop");
        add("gtna.machine.steam_elevator_greenhouse_module.tooltip.4",
                "Fixed operation time of 60 seconds");
        add("gtna.machine.steam_elevator_greenhouse_module.tooltip.5", "Cannot be overclocked");

        add("block.gtna.steam_elevator_oil_drill_module_i", "Steam Oil Drill Module I");
        add("block.gtna.steam_elevator_oil_drill_module_ii", "Steam Oil Drill Module II");
        add("block.gtna.steam_elevator_oil_drill_module_iii", "Steam Oil Drill Module III");
        add("gtna.machine.steam_elevator_oil_drill_module_i.tooltip", "§a§lExtracts oil with steam power.");
        add("gtna.machine.steam_elevator_oil_drill_module_i.tooltip.0",
                "Adds fluid drill module to the Steam Stairway");
        add("gtna.machine.steam_elevator_oil_drill_module_i.tooltip.1",
                "Extracts all underground fluid types in the current dimension.");
        add("gtna.machine.steam_elevator_oil_drill_module_i.tooltip.2", "Randomly selects up to 1 types of fluids.");
        add("gtna.machine.steam_elevator_oil_drill_module_i.tooltip.3", "Each extraction may yield 250~1000L.");
        add("gtna.machine.steam_elevator_oil_drill_module_i.tooltip.4", "Base cycle time: 1200 ticks");
        add("gtna.machine.steam_elevator_oil_drill_module_ii.tooltip", "§a§lAdvanced steam oil extraction.");
        add("gtna.machine.steam_elevator_oil_drill_module_ii.tooltip.0",
                "Adds fluid drill module to the Steam Stairway");
        add("gtna.machine.steam_elevator_oil_drill_module_ii.tooltip.1",
                "Extracts all underground fluid types in the current dimension.");
        add("gtna.machine.steam_elevator_oil_drill_module_ii.tooltip.2", "Randomly selects up to 2 types of fluids.");
        add("gtna.machine.steam_elevator_oil_drill_module_ii.tooltip.3", "Each extraction may yield 500~2000L.");
        add("gtna.machine.steam_elevator_oil_drill_module_ii.tooltip.4", "Base cycle time: 600 ticks");
        add("gtna.machine.steam_elevator_oil_drill_module_iii.tooltip", "§a§lElite steam oil extraction.");
        add("gtna.machine.steam_elevator_oil_drill_module_iii.tooltip.0",
                "Adds fluid drill module to the Steam Stairway");
        add("gtna.machine.steam_elevator_oil_drill_module_iii.tooltip.1",
                "Extracts all underground fluid types in the current dimension.");
        add("gtna.machine.steam_elevator_oil_drill_module_iii.tooltip.2", "Randomly selects up to 3 types of fluids.");
        add("gtna.machine.steam_elevator_oil_drill_module_iii.tooltip.3", "Each extraction may yield 1000~4000L.");
        add("gtna.machine.steam_elevator_oil_drill_module_iii.tooltip.4", "Base cycle time: 400 ticks");

        elevatorModuleTiers("steam_elevator_entity_crusher_module", "Steam Entity Crusher Module",
                "§a§lSmelt mobs using high-temperature steam.",
                "Adds entity crusher module to the Steam Stairway",
                "Each operation has a 2% chance to double output.",
                "Each additional identical spawner increases the chance by 0.5%.",
                "Maximum chance increase is 34%.",
                "As a trade-off, default time is doubled, power is halved.",
                "Cannot be overclocked");

        add("block.gtna.steam_elevator_ore_processor_module", "Steam Ore Processing Module");
        add("gtna.machine.steam_elevator_ore_processor_module.tooltip", "§a§lHigh-efficiency steam ore processor.");
        add("gtna.machine.steam_elevator_ore_processor_module.tooltip.1",
                "Adds ore processing module to the Steam Stairway");
        add("gtna.machine.steam_elevator_ore_processor_module.tooltip.2", "Do all ore processing in one step");
        add("gtna.machine.steam_elevator_ore_processor_module.tooltip.3", "Can process up to 16 ores at a time");
        add("gtna.machine.steam_elevator_ore_processor_module.tooltip.4",
                "Every ore costs 128L Steam/t and the washing fluid of the selected circuit (circuit 1 needs none)");
        add("gtna.machine.steam_elevator_ore_processor_module.tooltip.5",
                "Processing time depends on the current mode");
        add("gtna.machine.steam_elevator_ore_processor_module.tooltip.6",
                "Set circuit to double both parallel and EU consumption");
        add("gtna.machine.steam_elevator_ore_processor_module.tooltip.7", "Use a screwdriver to switch mode");
        add("gtna.machine.steam_elevator_ore_processor_module.tooltip.8",
                "Sneak click with screwdriver to void the stone dust");

        add("block.gtna.steam_elevator_monster_repellent_module_i", "Steam Monster Repellator Module I");
        add("block.gtna.steam_elevator_monster_repellent_module_ii", "Steam Monster Repellator Module II");
        add("block.gtna.steam_elevator_monster_repellent_module_iii", "Steam Monster Repellator Module III");
        add("gtna.machine.steam_elevator_monster_repellent_module_i.tooltip",
                "§a§lIt drives away monsters with loud noise.");
        add("gtna.machine.steam_elevator_monster_repellent_module_i.tooltip.0",
                "Adds mob repellent module to the Steam Stairway");
        add("gtna.machine.steam_elevator_monster_repellent_module_i.tooltip.1",
                "Creates a force field to prevent monster spawns");
        add("gtna.machine.steam_elevator_monster_repellent_module_i.tooltip.2",
                "Only prevents spawns while the machine is running");
        add("gtna.machine.steam_elevator_monster_repellent_module_i.tooltip.3", "Effective range: 64");
        add("gtna.machine.steam_elevator_monster_repellent_module_ii.tooltip",
                "§a§lYou can’t hear it, but the monsters hear it loud and clear.");
        add("gtna.machine.steam_elevator_monster_repellent_module_ii.tooltip.0",
                "Adds mob repellent module to the Steam Stairway");
        add("gtna.machine.steam_elevator_monster_repellent_module_ii.tooltip.1",
                "Creates a force field to prevent monster spawns");
        add("gtna.machine.steam_elevator_monster_repellent_module_ii.tooltip.2",
                "Only prevents spawns while the machine is running");
        add("gtna.machine.steam_elevator_monster_repellent_module_ii.tooltip.3", "Effective range: 128");
        add("gtna.machine.steam_elevator_monster_repellent_module_iii.tooltip",
                "§a§lThis machine makes monsters alert and run away on their own.");
        add("gtna.machine.steam_elevator_monster_repellent_module_iii.tooltip.0",
                "Adds mob repellent module to the Steam Stairway");
        add("gtna.machine.steam_elevator_monster_repellent_module_iii.tooltip.1",
                "Creates a force field to prevent monster spawns");
        add("gtna.machine.steam_elevator_monster_repellent_module_iii.tooltip.2",
                "Only prevents spawns while the machine is running");
        add("gtna.machine.steam_elevator_monster_repellent_module_iii.tooltip.3", "Effective range: 256");

        add("block.gtna.steam_elevator_beacon_module_i", "Steam Elevator Beacon Module I");
        add("block.gtna.steam_elevator_beacon_module_ii", "Steam Elevator Beacon Module II");
        add("block.gtna.steam_elevator_beacon_module_iii", "Steam Elevator Beacon Module III");
        add("gtna.machine.steam_elevator_beacon_module_i.tooltip", "§a§lBasic Beacon.");
        add("gtna.machine.steam_elevator_beacon_module_i.tooltip.0",
                "Add beacon module effects into the Steam Stairway");
        add("gtna.machine.steam_elevator_beacon_module_i.tooltip.1",
                "Input steam to enable large-area potion effects");
        add("gtna.machine.steam_elevator_beacon_module_i.tooltip.2", "Effective range: 64");
        add("gtna.machine.steam_elevator_beacon_module_i.tooltip.3",
                "Configure the potion effects to emit in the GUI");
        add("gtna.machine.steam_elevator_beacon_module_i.tooltip.4",
                "Adjust overclock count to increase potion effect levels");
        add("gtna.machine.steam_elevator_beacon_module_ii.tooltip", "§a§lAdvanced Beacon.");
        add("gtna.machine.steam_elevator_beacon_module_ii.tooltip.0",
                "Add beacon module effects into the Steam Stairway");
        add("gtna.machine.steam_elevator_beacon_module_ii.tooltip.1",
                "Input steam to enable large-area potion effects");
        add("gtna.machine.steam_elevator_beacon_module_ii.tooltip.2", "Effective range: 128");
        add("gtna.machine.steam_elevator_beacon_module_ii.tooltip.3",
                "Configure the potion effects to emit in the GUI");
        add("gtna.machine.steam_elevator_beacon_module_ii.tooltip.4",
                "Adjust overclock count to increase potion effect levels");
        add("gtna.machine.steam_elevator_beacon_module_iii.tooltip", "§a§lElite Beacon.");
        add("gtna.machine.steam_elevator_beacon_module_iii.tooltip.0",
                "Add beacon module effects into the Steam Stairway");
        add("gtna.machine.steam_elevator_beacon_module_iii.tooltip.1",
                "Input steam to enable large-area potion effects");
        add("gtna.machine.steam_elevator_beacon_module_iii.tooltip.2", "Effective range: 256");
        add("gtna.machine.steam_elevator_beacon_module_iii.tooltip.3",
                "Configure the potion effects to emit in the GUI");
        add("gtna.machine.steam_elevator_beacon_module_iii.tooltip.4",
                "Adjust overclock count to increase potion effect levels");

        add("block.gtna.steam_elevator_apiary_module", "Steam-Powered Apiary Module");
        add("gtna.machine.steam_elevator_apiary_module.tooltip",
                "§a§lHigh pressure makes the bees work §0§mharder§a§l and more efficiently.");
        add("gtna.machine.steam_elevator_apiary_module.tooltip.1", "Adds apiary module to the Steam Stairway");
        add("gtna.machine.steam_elevator_apiary_module.tooltip.2", "The bees' (ideal?) home");
        add("gtna.machine.steam_elevator_apiary_module.tooltip.3", "Fixed operating time: 300 seconds");
        add("gtna.machine.steam_elevator_apiary_module.tooltip.4",
                "Each overclock doubles the capacity, power ×4");
        add("gtna.machine.steam_elevator_apiary_module.tooltip.5", "Base amount: 8");

        add("block.gtna.steam_elevator_bee_breeding_module", "Steam Bee Breeding Module");
        add("gtna.machine.steam_elevator_bee_breeding_module.tooltip", "§a§lLet the bees feel the scorching♂love.");
        add("gtna.machine.steam_elevator_bee_breeding_module.tooltip.1",
                "Adds bee breeding module to the Steam Stairway");
        add("gtna.machine.steam_elevator_bee_breeding_module.tooltip.2",
                "Place a queen bee inside the controller");
        add("gtna.machine.steam_elevator_bee_breeding_module.tooltip.3",
                "It will slowly produce ignoble princesses");
        add("gtna.machine.steam_elevator_bee_breeding_module.tooltip.4",
                "Consumes 128 royal jelly per operation");
        add("gtna.machine.steam_elevator_bee_breeding_module.tooltip.5", "Base processing time: 10 minutes");

        add("config.gtna.option.eyeOfWood", "Eye of Wood");

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
        // Jade builds one config entry per registered data provider and asserts the translation
        // exists (config.jade.plugin_<namespace>.<provider uid path>); a missing one crashes the dev
        // client with 'Missing config translation'. Keep these in sync with GTNAJadePlugin.
        add("config.jade.plugin_gtna.multiple_recipes_provider", "Multiple Recipes Machine Info");
        add("config.jade.plugin_gtna.me_pattern_buffer", "ME Pattern Buffer Info");
        add("config.jade.plugin_gtna.solar_boiler_provider", "Large Steam Solar Boiler Info");
        add("config.jade.plugin_gtna.wireless_steam_network", "Wireless Steam Network Info");
        add("gtna.jade.wireless_steam.balance", "Network: %s mB");
        add("gtna.jade.wireless_steam.tank", "Hatch Tank: %s / %s mB");
        add("gtna.jade.wireless_steam.last_push", "Last push: %s mB");
        add("gtna.jade.wireless_steam.last_pull", "Last pull: %s mB");
        add("gtna.jade.wireless_steam.idle", "No transfer yet");

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
