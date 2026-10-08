package com.raishxn.gtna.common.data;

import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.common.item.CoverPlaceBehavior;
import com.gregtechceu.gtceu.common.item.TooltipBehavior;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.common.item.CoordinateCardBehavior;
import com.raishxn.gtna.common.item.PatternBufferCopyBehavior;
import com.raishxn.gtna.common.item.PatternBufferUpgraderBehavior;
import com.raishxn.gtna.common.item.RealityRipperSwordItem;
import com.raishxn.gtna.common.item.StructureDetectBehavior;
import com.raishxn.gtna.common.item.TesseractTargetMarkerBehavior;
import com.raishxn.gtna.common.item.armor.QuantumCosmicNexusArmorItem;
import com.tterrag.registrate.util.entry.ItemEntry;

import static com.gregtechceu.gtceu.common.data.GTItems.attach;
import static com.raishxn.gtna.api.registry.GTNARegistry.REGISTRATE;

public class GTNAItems {

    private static final String[] INDUSTRIAL_COMPONENT_GROUPS = { "standard", "extended", "special" };
    private static final String[] INDUSTRIAL_COMPONENT_GROUP_NAMES = { "Standard", "Extended", "Special" };
    private static final String[] INDUSTRIAL_COMPONENT_SIZES = { "small", "medium", "large" };
    private static final String[] INDUSTRIAL_COMPONENT_SIZE_NAMES = { "Small", "Medium", "Large" };

    static {
        REGISTRATE.creativeModeTab(() -> GTNACreativeModeTabs.ITEMS);
    }

    // Ferramentas e Itens de Debug Existentes
    public static ItemEntry<ComponentItem> DEBUG_STRUCTURE_WRITER;
    public static ItemEntry<ComponentItem> STRUCTURE_DETECT;
    public static ItemEntry<ComponentItem> COORDINATE_CARD;
    public static ItemEntry<ComponentItem> TESSERACT_TARGET_MARKER;

    // --- NOVOS COMPONENTES HIDRÁULICOS ---
    public static ItemEntry<ComponentItem> HYDRAULIC_MOTOR;
    public static ItemEntry<ComponentItem> HYDRAULIC_PISTON;
    public static ItemEntry<ComponentItem> HYDRAULIC_PUMP;
    public static ItemEntry<ComponentItem> HYDRAULIC_ARM;
    public static ItemEntry<ComponentItem> HYDRAULIC_CONVEYOR;
    public static ItemEntry<ComponentItem> HYDRAULIC_REGULATOR;
    public static ItemEntry<ComponentItem> HYDRAULIC_VAPOR_GENERATOR;
    public static ItemEntry<ComponentItem> HYDRAULIC_STEAM_JET_SPEWER;
    public static ItemEntry<ComponentItem> HYDRAULIC_STEAM_RECEIVER;
    public static ItemEntry<ComponentItem> PRECISION_STEAM_COMPONENT;
    public static ItemEntry<ComponentItem> PRIMITIVE_MANS_SPACETIME_DISTORTION_DEVICE;
    @SuppressWarnings("unchecked")
    public static ItemEntry<ComponentItem>[][] INDUSTRIAL_COMPONENTS = new ItemEntry[INDUSTRIAL_COMPONENT_GROUPS.length][INDUSTRIAL_COMPONENT_SIZES.length];

    public static ItemEntry<com.raishxn.gtna.common.item.NexusLinkerItem> NEXUS_LINKER;
    public static ItemEntry<QuantumCosmicNexusArmorItem> QUANTUM_COSMIC_NEXUS_HELMET;
    public static ItemEntry<QuantumCosmicNexusArmorItem> QUANTUM_COSMIC_NEXUS_CHESTPLATE;
    public static ItemEntry<QuantumCosmicNexusArmorItem> QUANTUM_COSMIC_NEXUS_LEGGINGS;
    public static ItemEntry<QuantumCosmicNexusArmorItem> QUANTUM_COSMIC_NEXUS_BOOTS;
    public static ItemEntry<ComponentItem> QUANTUM_NETWORK_TERMINAL;
    public static ItemEntry<ComponentItem> NEXUS_STRUCTURE_TERMINAL;
    public static ItemEntry<ComponentItem> PATTERN_BUFFER_UPGRADE_21;
    public static ItemEntry<ComponentItem> PATTERN_BUFFER_UPGRADE_32;
    public static ItemEntry<ComponentItem> PATTERN_BUFFER_UPGRADE_72;
    public static ItemEntry<ComponentItem> PATTERN_BUFFER_COPY_CARD;
    public static ItemEntry<ComponentItem> PATTERN_BUFFER_CUT_CARD;
    public static ItemEntry<ComponentItem> INFINITE_CELL_COMPONENT;
    public static ItemEntry<ComponentItem> CELL_COMPONENT_1M;
    public static ItemEntry<ComponentItem> CELL_COMPONENT_4M;
    public static ItemEntry<ComponentItem> CELL_COMPONENT_16M;
    public static ItemEntry<ComponentItem> CELL_COMPONENT_64M;
    public static ItemEntry<ComponentItem> CELL_COMPONENT_256M;
    public static ItemEntry<ComponentItem> ANNIHILATION_CONSTRAINER;
    /** GT++ Laser Lens Special (MetaGeneratedItem 105), Modernity-GTNH texture. */
    public static ItemEntry<ComponentItem> LASER_LENS_SPECIAL;
    /** GTNH Harmonic Compound (metaitem 03:762, GT5U texture). */
    public static ItemEntry<ComponentItem> HARMONIC_COMPOUND;
    /** GTNH Phononic Seed Crystal (metaitem 03:761, Modernity texture). */
    public static ItemEntry<ComponentItem> PHONONIC_SEED_CRYSTAL;
    public static ItemEntry<ComponentItem> NEUTRONIUM_ANTIMATTER_FUEL_ROD;
    public static ItemEntry<ComponentItem> DRACONIUM_ANTIMATTER_FUEL_ROD;
    public static ItemEntry<ComponentItem> COSMIC_NEUTRONIUM_ANTIMATTER_FUEL_ROD;
    public static ItemEntry<ComponentItem> INFINITY_ANTIMATTER_FUEL_ROD;
    public static ItemEntry<RealityRipperSwordItem> REALITY_RIPPER_SWORD;
    public static ItemEntry<ComponentItem> INFINITE_STEAM_SINGLEBLOCK_COVER;
    public static ItemEntry<ComponentItem> INFINITE_ELECTRIC_SINGLEBLOCK_COVER;
    /** GTOCore grind balls: durability 50 / tier 1 and durability 100 / tier 2. */
    public static ItemEntry<ComponentItem> GRINDBALL_SOAPSTONE;
    public static ItemEntry<ComponentItem> GRINDBALL_ALUMINIUM;
    @SuppressWarnings("unchecked")
    public static final ItemEntry<ComponentItem>[] PLANET_DATA_CHIPS = new ItemEntry[5];
    public static final String[] PLANET_DATA_CHIP_PLANETS = { "moon", "mars", "venus", "mercury", "glacio" };

    public static final java.util.Map<String, ItemEntry<ComponentItem>> VEIN_ESSENCES = new java.util.LinkedHashMap<>();
    public static final java.util.Map<String, ItemEntry<ComponentItem>> WORLD_DATA = new java.util.LinkedHashMap<>();
    public static ItemEntry<ComponentItem> DEPOSIT_RECORDER;
    public static ItemEntry<ComponentItem> DEPOSIT_DATA;
    public static ItemEntry<ComponentItem> FLUID_SEPARATION_FILTER;
    public static final java.util.Map<String, ItemEntry<ComponentItem>> FLUID_REMOTE_UPGRADES = new java.util.LinkedHashMap<>();
    public static ItemEntry<ComponentItem> ESSENCE;
    public static ItemEntry<ComponentItem> ESSENCE_SEED;
    public static final net.minecraft.tags.TagKey<net.minecraft.world.item.Item> VEIN_ESSENCE_TAG = net.minecraft.tags.ItemTags
            .create(GTNACORE.id("vein_essences"));

    private static ItemEntry<ComponentItem> essenceItem(String id, String texture, boolean vein) {
        var builder = REGISTRATE.item(id, ComponentItem::create)
                .lang(com.gregtechceu.gtceu.utils.FormattingUtil.toEnglishName(id))
                .onRegister(attach(new TooltipBehavior(lines -> lines.add(GTNASources.line(GTNASources.GTL)))))
                .model((ctx, provider) -> provider.generated(ctx, GTNACORE.id("item/" + texture)));
        if (vein) builder.tag(VEIN_ESSENCE_TAG);
        return builder.register();
    }

    public static ItemEntry<ComponentItem> ASTRAL_ARRAY_FABRICATOR;

    public static void init() {
        GTNAEyeOfHarmonyContent.init();
        GTNAGodforgeContent.init();
        ASTRAL_ARRAY_FABRICATOR = GTNAEyeOfHarmonyContent.ASTRAL_ARRAY_FABRICATOR;
        REGISTRATE.creativeModeTab(() -> GTNACreativeModeTabs.ITEMS);
        DEPOSIT_RECORDER = REGISTRATE.item("deposit_recorder", ComponentItem::create)
                .lang("Deposit Recorder").properties(p -> p.stacksTo(1))
                .onRegister(attach(new com.raishxn.gtna.common.item.DepositRecorderBehavior()))
                .model((ctx, provider) -> provider.generated(ctx, GTNACORE.id("item/" + ctx.getName())))
                .register();
        DEPOSIT_DATA = REGISTRATE.item("deposit_data", ComponentItem::create)
                .lang("Deposit Data").properties(p -> p.stacksTo(1))
                .onRegister(attach(new com.raishxn.gtna.common.item.DepositDataBehavior()))
                .model((ctx, provider) -> provider.generated(ctx, GTNACORE.id("item/" + ctx.getName())))
                .register();
        FLUID_SEPARATION_FILTER = REGISTRATE.item("fluid_separation_filter", ComponentItem::create)
                .lang("Fluid Separation Filter")
                .model((ctx, provider) -> provider.generated(ctx, GTNACORE.id("item/" + ctx.getName())))
                .register();
        for (String stage : java.util.List.of("terrestrial", "t1", "t2", "t3", "t4", "t5")) {
            FLUID_REMOTE_UPGRADES.put(stage, REGISTRATE.item("fluid_remote_upgrade_" + stage, ComponentItem::create)
                    .lang("Remote Fluid Upgrade " + stage.toUpperCase(java.util.Locale.ROOT))
                    .properties(p -> p.stacksTo(1))
                    .model((ctx, provider) -> provider.generated(ctx, GTNACORE.id("item/" + ctx.getName())))
                    .register());
        }
        ESSENCE = essenceItem("essence", "essence", false);
        ESSENCE_SEED = essenceItem("essence_seed", "essence_seed", false);
        for (var vein : GTNAVoidVeins.ALL) {
            VEIN_ESSENCES.put(vein.essence(), essenceItem(vein.essence(),
                    "essence/" + vein.essence().replace("_essence", ""), true));
        }
        for (String world : new String[] { "overworld", "nether", "end" }) {
            WORLD_DATA.put(world, REGISTRATE.item(world + "_data", ComponentItem::create)
                    .lang(com.gregtechceu.gtceu.utils.FormattingUtil.toEnglishName(world + "_data"))
                    .model((ctx, provider) -> provider.generated(ctx, GTNACORE.id("item/" + ctx.getName())))
                    .register());
        }
        for (String planet : PLANET_DATA_CHIP_PLANETS) {
            VEIN_ESSENCES.put(planet + "_vein_essence",
                    essenceItem(planet + "_vein_essence", "essence/" + planet + "_vein", true));
        }

        for (int index = 0; index < PLANET_DATA_CHIPS.length; index++) {
            String planet = PLANET_DATA_CHIP_PLANETS[index];
            PLANET_DATA_CHIPS[index] = REGISTRATE.item("planet_data_chip_" + planet, ComponentItem::create)
                    .lang(switch (planet) {
                        case "moon" -> "Moon Planet Data Chip";
                        case "mars" -> "Mars Planet Data Chip";
                        case "venus" -> "Venus Planet Data Chip";
                        case "mercury" -> "Mercury Planet Data Chip";
                        default -> "Glacio Planet Data Chip";
                    })
                    .properties(properties -> properties.stacksTo(1))
                    .model((ctx, provider) -> provider.generated(ctx, GTNACORE.id("item/" + ctx.getName())))
                    .register();
        }
        CELL_COMPONENT_1M = registerCellComponent(1);
        CELL_COMPONENT_4M = registerCellComponent(4);
        CELL_COMPONENT_16M = registerCellComponent(16);
        CELL_COMPONENT_64M = registerCellComponent(64);
        CELL_COMPONENT_256M = registerCellComponent(256);
        STRUCTURE_DETECT = REGISTRATE
                .item("structure_detect", ComponentItem::create)
                .lang("Structure Detector")
                .properties(stack -> stack.stacksTo(1))
                .onRegister(attach(StructureDetectBehavior.INSTANCE))
                .model((ctx, provider) -> {
                    provider.generated(ctx, new ResourceLocation("gtceu", "item/portable_scanner"));
                })
                .register();

        COORDINATE_CARD = REGISTRATE
                .item("coordinate_card", ComponentItem::create)
                .lang("Coordinate Card")
                .properties(stack -> stack.stacksTo(1))
                .onRegister(attach(CoordinateCardBehavior.INSTANCE))
                .model((ctx, provider) -> provider.generated(ctx, GTNACORE.id("item/coordinate_card")))
                .register();

        TESSERACT_TARGET_MARKER = REGISTRATE
                .item("tesseract_target_marker", ComponentItem::create)
                .lang("Tesseract Target Marker")
                .properties(stack -> stack.stacksTo(1))
                .onRegister(attach(TesseractTargetMarkerBehavior.INSTANCE))
                .model((ctx, provider) -> provider.generated(ctx, GTNACORE.id("item/tesseract_target_marker")))
                .register();

        HYDRAULIC_MOTOR = REGISTRATE.item("hydraulic_motor", ComponentItem::create)
                .properties(stack -> stack.stacksTo(64))
                .lang("Hydraulic Motor")
                .register();
        HYDRAULIC_PISTON = REGISTRATE.item("hydraulic_piston", ComponentItem::create)
                .lang("Hydraulic Piston")
                .properties(stack -> stack.stacksTo(64))
                .register();
        HYDRAULIC_PUMP = REGISTRATE.item("hydraulic_pump", ComponentItem::create)
                .lang("Hydraulic Pump")
                .properties(stack -> stack.stacksTo(64))
                .register();
        HYDRAULIC_ARM = REGISTRATE.item("hydraulic_arm", ComponentItem::create)
                .lang("Hydraulic Arm")
                .properties(stack -> stack.stacksTo(64))
                .register();
        HYDRAULIC_CONVEYOR = REGISTRATE.item("hydraulic_conveyor", ComponentItem::create)
                .lang("Hydraulic Conveyor")
                .properties(stack -> stack.stacksTo(64))
                .register();
        HYDRAULIC_REGULATOR = REGISTRATE.item("hydraulic_regulator", ComponentItem::create)
                .lang("Hydraulic Regulator")
                .properties(stack -> stack.stacksTo(64))
                .register();
        HYDRAULIC_VAPOR_GENERATOR = REGISTRATE.item("hydraulic_vapor_generator", ComponentItem::create)
                .lang("Hydraulic Vapor Generator")
                .properties(stack -> stack.stacksTo(64))
                .register();
        HYDRAULIC_STEAM_JET_SPEWER = REGISTRATE.item("hydraulic_steam_jet_spewer", ComponentItem::create)
                .lang("Hydraulic Steam Jet Spewer")
                .properties(stack -> stack.stacksTo(64))
                .register();
        HYDRAULIC_STEAM_RECEIVER = REGISTRATE.item("hydraulic_steam_receiver", ComponentItem::create)
                .lang("Hydraulic Steam Receiver")
                .properties(stack -> stack.stacksTo(64))
                .register();
        PRECISION_STEAM_COMPONENT = REGISTRATE.item("precision_steam_component", ComponentItem::create)
                .lang("Precision Steam Component")
                .properties(stack -> stack.stacksTo(64))
                .register();
        PRIMITIVE_MANS_SPACETIME_DISTORTION_DEVICE = REGISTRATE
                .item("primitive_mans_spacetime_distortion_device", ComponentItem::create)
                .lang("Primitive Man's SpaceTime Distortion Device")
                .properties(stack -> stack.stacksTo(64))
                .onRegister(attach(new TooltipBehavior(lines -> lines.add(
                        Component.translatable("item.gtna.primitive_mans_spacetime_distortion_device.tooltip")
                                .withStyle(ChatFormatting.GRAY)))))
                .model((ctx, provider) -> provider.generated(ctx,
                        GTNACORE.id("item/primitive_mans_spacetime_distortion_device")))
                .register();
        registerIndustrialComponents();

        // GTOCore grinding balls for the ISA Mill. Soapstone is tier 1 (50 durability) and
        // Aluminium is tier 2 (100 durability); the tier is read by BallHatchPartMachine.
        GRINDBALL_SOAPSTONE = REGISTRATE.item("grindball_soapstone", ComponentItem::create)
                .lang("Soapstone Grinding Ball")
                .properties(stack -> stack.stacksTo(1).durability(50))
                .model((ctx, provider) -> provider.generated(ctx, GTNACORE.id("item/grindball_soapstone")))
                .register();

        GRINDBALL_ALUMINIUM = REGISTRATE.item("grindball_aluminium", ComponentItem::create)
                .lang("Aluminium Grinding Ball")
                .properties(stack -> stack.stacksTo(1).durability(100))
                .model((ctx, provider) -> provider.generated(ctx, GTNACORE.id("item/grindball_aluminium")))
                .register();

        NEXUS_LINKER = REGISTRATE.item("nexus_linker", com.raishxn.gtna.common.item.NexusLinkerItem::new)
                .lang("Nexus Linker")
                .properties(stack -> stack.stacksTo(1))
                .register();

        QUANTUM_COSMIC_NEXUS_HELMET = REGISTRATE.item("quantum_cosmic_nexus_helmet",
                props -> new QuantumCosmicNexusArmorItem(net.minecraft.world.item.ArmorItem.Type.HELMET, props))
                .lang("Quantum Cosmic Nexus Helmet")
                .properties(stack -> stack.stacksTo(1))
                .model((ctx, provider) -> provider.generated(ctx, GTNACORE.id("item/" + ctx.getName())))
                .register();

        QUANTUM_COSMIC_NEXUS_CHESTPLATE = REGISTRATE.item("quantum_cosmic_nexus_chestplate",
                props -> new QuantumCosmicNexusArmorItem(net.minecraft.world.item.ArmorItem.Type.CHESTPLATE, props))
                .lang("Quantum Cosmic Nexus Chestplate")
                .properties(stack -> stack.stacksTo(1))
                .model((ctx, provider) -> provider.generated(ctx, GTNACORE.id("item/" + ctx.getName())))
                .register();

        QUANTUM_COSMIC_NEXUS_LEGGINGS = REGISTRATE.item("quantum_cosmic_nexus_leggings",
                props -> new QuantumCosmicNexusArmorItem(net.minecraft.world.item.ArmorItem.Type.LEGGINGS, props))
                .lang("Quantum Cosmic Nexus Leggings")
                .properties(stack -> stack.stacksTo(1))
                .model((ctx, provider) -> provider.generated(ctx, GTNACORE.id("item/" + ctx.getName())))
                .register();

        QUANTUM_COSMIC_NEXUS_BOOTS = REGISTRATE.item("quantum_cosmic_nexus_boots",
                props -> new QuantumCosmicNexusArmorItem(net.minecraft.world.item.ArmorItem.Type.BOOTS, props))
                .lang("Quantum Cosmic Nexus Boots")
                .properties(stack -> stack.stacksTo(1))
                .model((ctx, provider) -> provider.generated(ctx, GTNACORE.id("item/" + ctx.getName())))
                .register();

        QUANTUM_NETWORK_TERMINAL = REGISTRATE.item("quantum_network_terminal", ComponentItem::create)
                .lang("Quantum Network Terminal")
                .properties(stack -> stack.stacksTo(1))
                .onRegister(attach(com.raishxn.gtna.common.item.QuantumNetworkTerminalBehavior.INSTANCE))
                .register();

        NEXUS_STRUCTURE_TERMINAL = REGISTRATE.item("nexus_structure_terminal", ComponentItem::create)
                .lang("Nexus Structure Terminal")
                .properties(stack -> stack.stacksTo(1))
                .onRegister(attach(com.raishxn.gtna.common.item.terminal.NexusTerminalBehavior.INSTANCE))
                .register();

        PATTERN_BUFFER_UPGRADE_21 = REGISTRATE.item("pattern_buffer_upgrade_21", ComponentItem::create)
                .lang("Pattern Buffer Expansion Card")
                .properties(stack -> stack.stacksTo(16))
                .onRegister(attach(new PatternBufferUpgraderBehavior(() -> GTNAMachines2.ME_PATTERN_BUFFER)))
                .model((ctx, provider) -> provider.generated(ctx, GTNACORE.id("item/pattern_buffer_upgrader")))
                .register();

        PATTERN_BUFFER_UPGRADE_32 = REGISTRATE.item("pattern_buffer_upgrade_32", ComponentItem::create)
                .lang("Pattern Buffer Precision Card")
                .properties(stack -> stack.stacksTo(16))
                .onRegister(attach(new PatternBufferUpgraderBehavior(() -> GTNAMachines2.ME_ADVANCED_PATTERN_BUFFER)))
                .model((ctx, provider) -> provider.generated(ctx, GTNACORE.id("item/ex_pattern_buffer_upgrader")))
                .register();

        PATTERN_BUFFER_UPGRADE_72 = REGISTRATE.item("pattern_buffer_upgrade_72", ComponentItem::create)
                .lang("Pattern Buffer Ascension Card")
                .properties(stack -> stack.stacksTo(16))
                .onRegister(attach(new PatternBufferUpgraderBehavior(() -> GTNAMachines2.ME_ULTIMATE_PATTERN_BUFFER)))
                .model((ctx, provider) -> provider.generated(ctx, GTNACORE.id("item/ex_pattern_buffer_ultra_upgrader")))
                .register();

        PATTERN_BUFFER_COPY_CARD = REGISTRATE.item("pattern_buffer_copy_card", ComponentItem::create)
                .lang("Pattern Buffer Copy Card")
                .properties(stack -> stack.stacksTo(1))
                .onRegister(attach(new PatternBufferCopyBehavior(false)))
                .model((ctx, provider) -> provider.generated(ctx, GTNACORE.id("item/pattern_buffer_copy_card")))
                .register();

        PATTERN_BUFFER_CUT_CARD = REGISTRATE.item("pattern_buffer_cut_card", ComponentItem::create)
                .lang("Pattern Buffer Cut Card")
                .properties(stack -> stack.stacksTo(1))
                .onRegister(attach(new PatternBufferCopyBehavior(true)))
                .model((ctx, provider) -> provider.generated(ctx, GTNACORE.id("item/pattern_buffer_cut_card")))
                .register();

        INFINITE_CELL_COMPONENT = REGISTRATE.item("infinite_cell_component", ComponentItem::create)
                .lang("Infinite Cell Component")
                .properties(stack -> stack.stacksTo(64))
                .model((ctx, provider) -> provider.generated(ctx, GTNACORE.id("item/infinite_cell_component")))
                .register();

        PHONONIC_SEED_CRYSTAL = REGISTRATE.item("phononic_seed_crystal", ComponentItem::create)
                .lang("Phononic Seed Crystal")
                .register();

        HARMONIC_COMPOUND = REGISTRATE.item("harmonic_compound", ComponentItem::create)
                .lang("Harmonic Compound")
                .register();

        LASER_LENS_SPECIAL = REGISTRATE.item("laser_lens_special", ComponentItem::create)
                .lang("Special Laser Lens")
                .properties(stack -> stack.stacksTo(64))
                .model((ctx, provider) -> provider.generated(ctx, GTNACORE.id("item/laser_lens_special")))
                .register();

        ANNIHILATION_CONSTRAINER = REGISTRATE.item("annihilation_constrainer", ComponentItem::create)
                .lang("Annihilation Constrainer")
                .properties(stack -> stack.stacksTo(64))
                .register();

        NEUTRONIUM_ANTIMATTER_FUEL_ROD = REGISTRATE.item("neutronium_antimatter_fuel_rod", ComponentItem::create)
                .lang("Neutronium Antimatter Fuel Rod")
                .properties(stack -> stack.stacksTo(64))
                .register();

        DRACONIUM_ANTIMATTER_FUEL_ROD = REGISTRATE.item("draconium_antimatter_fuel_rod", ComponentItem::create)
                .lang("Draconium Antimatter Fuel Rod")
                .properties(stack -> stack.stacksTo(64))
                .register();

        COSMIC_NEUTRONIUM_ANTIMATTER_FUEL_ROD = REGISTRATE
                .item("cosmic_neutronium_antimatter_fuel_rod", ComponentItem::create)
                .lang("Cosmic Neutronium Antimatter Fuel Rod")
                .properties(stack -> stack.stacksTo(64))
                .register();

        INFINITY_ANTIMATTER_FUEL_ROD = REGISTRATE.item("infinity_antimatter_fuel_rod", ComponentItem::create)
                .lang("Infinity Antimatter Fuel Rod")
                .properties(stack -> stack.stacksTo(64))
                .register();

        REALITY_RIPPER_SWORD = REGISTRATE.item("reality_ripper_sword", RealityRipperSwordItem::new)
                .lang("Reality Ripper")
                .properties(stack -> stack.stacksTo(1))
                .model((ctx, provider) -> provider.generated(ctx, GTNACORE.id("item/" + ctx.getName())))
                .register();

        INFINITE_STEAM_SINGLEBLOCK_COVER = REGISTRATE.item("infinite_steam_singleblock_cover", ComponentItem::create)
                .lang("Infinite Steam Singleblock Cover")
                .properties(stack -> stack.stacksTo(64))
                .onRegister(attach(new CoverPlaceBehavior(GTNACovers.INFINITE_STEAM_SINGLEBLOCK_COVER)))
                .model((ctx, provider) -> provider.generated(ctx,
                        GTNACORE.id("item/734")))
                .register();

        INFINITE_ELECTRIC_SINGLEBLOCK_COVER = REGISTRATE.item("infinite_electric_singleblock_cover",
                ComponentItem::create)
                .lang("Infinite Electric Singleblock Cover")
                .properties(stack -> stack.stacksTo(64))
                .onRegister(attach(new CoverPlaceBehavior(GTNACovers.INFINITE_ELECTRIC_SINGLEBLOCK_COVER)))
                .model((ctx, provider) -> provider.generated(ctx,
                        GTNACORE.id("item/733")))
                .register();
    }

    private static ItemEntry<ComponentItem> registerCellComponent(int capacityM) {
        String name = "cell_component_" + capacityM + "m";
        return REGISTRATE.item(name, ComponentItem::create)
                .lang(capacityM + "M Cell Component")
                .onRegister(attach(new TooltipBehavior(lines -> lines.add(GTNASources.line(GTNASources.GTO)))))
                .model((ctx, provider) -> provider.generated(ctx, GTNACORE.id("item/" + name)))
                .register();
    }

    private static void registerIndustrialComponents() {
        for (int group = 0; group < INDUSTRIAL_COMPONENT_GROUPS.length; group++) {
            for (int size = 0; size < INDUSTRIAL_COMPONENT_SIZES.length; size++) {
                String id = INDUSTRIAL_COMPONENT_GROUPS[group] + "_industrial_components_" +
                        INDUSTRIAL_COMPONENT_SIZES[size];
                String lang = INDUSTRIAL_COMPONENT_GROUP_NAMES[group] + " Industrial Components (" +
                        INDUSTRIAL_COMPONENT_SIZE_NAMES[size] + ")";
                String sizeKey = INDUSTRIAL_COMPONENT_SIZES[size];
                INDUSTRIAL_COMPONENTS[group][size] = REGISTRATE.item(id, ComponentItem::create)
                        .lang(lang)
                        .properties(stack -> stack.stacksTo(64))
                        .model((ctx, provider) -> provider.generated(ctx,
                                GTNACORE.id("item/industrial_components_" + sizeKey + "_0"),
                                GTNACORE.id("item/industrial_components_" + sizeKey + "_1")))
                        .register();
            }
        }
    }
}
