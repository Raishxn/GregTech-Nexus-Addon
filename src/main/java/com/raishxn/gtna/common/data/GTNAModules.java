package com.raishxn.gtna.common.data;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.common.data.GCYMBlocks;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import com.raishxn.gtna.api.machine.multiblock.GTNAPartAbility;
import com.raishxn.gtna.api.machine.multiblock.GTNASubPatterns;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

import static com.gregtechceu.gtceu.api.pattern.Predicates.*;

/**
 * GTNA module (sub-pattern) registrations. Modules attach to a multiblock's controller and merge
 * their parts into it, unlocking new abilities (see {@link GTNASubPatterns}).
 *
 * <p>
 * These are registered in Java for the machines GTNA ships; modpack creators can add more through
 * the KubeJS {@code GTNAServerEvents.subPatterns} event.
 */
public final class GTNAModules {

    private GTNAModules() {}

    public static void init() {
        registerElectricBlastFurnaceModule();
        registerLiquefactionFurnaceModule();
        registerEvaporationPlantModule();
        registerColdIceFreezerModule();
        registerRocketLargeTurbineModule();
        registerSupercriticalSteamTurbineModule();
        registerComponentAssemblerExtension();
    }

    /**
     * GTOCore's two {@code COMPONENT_ASSEMBLER.addSubPattern} layers
     * ({@code MultiBlockC.java:328-397}). The first is the 29×6×13 shell that wraps the base on the
     * west/east sides and carries the LuV tier casings plus one Accelerate Hatch; the second is the
     * 29×6×20 outer computer wall with the Lasers/Parallel Hatch. GTOCore's tier cells record the
     * casing tier of the <i>base</i> structure, so the machine still reads its tier from the main
     * pattern.
     *
     * <p>
     * GTOCore requires the extensions to reach UV; GTNA only ships LV–LuV component casings, so the
     * machine cap stops at LuV. Both extensions are registered against the same controller and can
     * be matched independently (their only shared cell is the controller), like GTOCore's
     * {@code getSubFormedAmount() > 0} rule.
     */
    private static void registerComponentAssemblerExtension() {
        ResourceLocation machine = new ResourceLocation("gtna", "component_assembler");
        GTNASubPatterns.register(machine, GTNAModules::buildComponentAssemblerExtension,
                moduleTooltips("laser", "parallel", "accelerate"));
        GTNASubPatterns.register(machine, GTNAModules::buildComponentAssemblerExtensionWide);
    }

    private static BlockPattern buildComponentAssemblerExtension(MultiblockMachineDefinition definition) {
        return FactoryBlockPattern.start()
                .aisle(" CCCCCCCCCCCCCCCCCCCCCCCCCCC ", " C         C     C         C ",
                        " C         C     C         C ",
                        " C         C     C         C ", " C         C     C         C ",
                        " CCCCCCCCCCCCCCCCCCCCCCCCCCC ")
                .aisle("ADDDDDDDDDDDDDDDDDDDDDDDDDDDA", "AHMMMMMMMMHDHNNNHDHMMMMMMMMHA",
                        "AHMMMMMMMMHDHNNNHDHMMMMMMMMHA", "AHMMMMMMMMHDHNNNHDHMMMMMMMMHA",
                        "ADDDDDDDDDDDDDDDDDDDDDDDDDDDA", " CFFC  CFFC CFFFC CFFC  CFFC ")
                .aisle("AEEEEEEEEEEDEEEEEDEEEEEEEEEEA", "B                           B",
                        "A          J     J          A", "B                           B",
                        "ADHHDDDDHHDDDHDHDDDHHDDDDHHDA", " CHHC  CHHC CHFHC CHHC  CHHC ")
                .aisle("AFFFFFFFFFFDFFFFFDFFFFFFFFFFA", "BIIIIIIIIII  P P  IIIIIIIIIIB",
                        "A          J P P J          A", "BJJJJJJJJJJ       JJJJJJJJJJB",
                        "ADHHDDDDHHDDDHDHDDDHHDDDDHHDA", " CHHC  CHHC CHFHC CHHC  CHHC ")
                .aisle("AGGGGGGGGGGDGGGGGDGGGGGGGGGGA", "B                           B",
                        "AGGGGGGGGGGJGGGGGJGGGGGGGGGGA", "B                           B",
                        "ADHHDDDDHHDDDHDHDDDHHDDDDHHDA", " CHHC  CHHC CHFHC CHHC  CHHC ")
                .aisle("AFFFFFFFFFFDFFFFFDFFFFFFFFFFA", "BIIIIIIIIII  P P  IIIIIIIIIIB",
                        "A          J P P J          A", "BJJJJJJJJJJ       JJJJJJJJJJB",
                        "ADHHDDDDHHDDDHDHDDDHHDDDDHHDA", " CHHC  CHHC CHFHC CHHC  CHHC ")
                .aisle("AEEEEEEEEEEDEEEEEDEEEEEEEEEEA", "B                           B",
                        "A          J     J          A", "B                           B",
                        "ADHHDDDDHHDDDHDHDDDHHDDDDHHDA", " CHHC  CHHC CHFHC CHHC  CHHC ")
                .aisle("ADDDDDDDDDD       DDDDDDDDDDA", "AHDO OO ODH       HDO OO ODHA",
                        "AHDO OO ODH       HDO OO ODHA", "AHD      DH       HD      DHA",
                        "ADDDDDDDDDD       DDDDDDDDDDA", " CFFC  CFFC CFFFC CFFC  CFFC ")
                .aisle(" CKKKKKKKKC       CKKKKKKKKC ", " CKO OO OKC       CKO OO OKC ",
                        " CKO OO OKC       CKO OO OKC ", " CKKKKKKKKC       CKKKKKKKKC ",
                        " CC      CC       CC      CC ", " CCCCCCCCCCCCCCCCCCCCCCCCCCC ")
                .aisle(" CKFFFFFFKC       CKFFFFFFKC ", "  LO OO OL         LO OO OL  ",
                        "  LO OO OL         LO OO OL  ", "  KNNNNNNK         KNNNNNNK  ",
                        "  C      C         C      C  ", "                             ")
                .aisle(" CKFFFFFFKC       CKFFFFFFKC ", "  LO OO OL         LO OO OL  ",
                        "  LO OO OL         LO OO OL  ", "  KNNNNNNK         KNNNNNNK  ",
                        "  C      C         C      C  ", "                             ")
                .aisle(" CKFFFFFFKC   Q   CKFFFFFFKC ", "  L      L         L      L  ",
                        "  L      L         L      L  ", "  KNNNNNNK         KNNNNNNK  ",
                        "  C      C         C      C  ", "                             ")
                .aisle(" CKKKKKKKKC       CKKKKKKKKC ", "  KNNNNNNK         KNNNNNNK  ",
                        "  KNNNNNNK         KNNNNNNK  ", "  KKKKKKKK         KKKKKKKK  ",
                        "                             ", "                             ")
                .where('A', blocks(GTBlocks.CASING_STEEL_SOLID.get())
                        .or(autoAbilities(definition.getRecipeTypes(), false, false, true, true, true, true))
                        .or(abilities(GTNAPartAbility.ACCELERATE_HATCH).setMaxGlobalLimited(1)))
                .where('B', GTNAMachines3.componentAssemblyTierCasings())
                .where('C', blocks(GCYMBlocks.CASING_NONCONDUCTING.get()))
                .where('D', blocks(GTBlocks.CASING_STEEL_SOLID.get()))
                .where('E', blocks(GTBlocks.CASING_ASSEMBLY_CONTROL.get()))
                .where('F', blocks(GTBlocks.STEEL_HULL.get()))
                .where('G', blocks(GTBlocks.CASING_ASSEMBLY_LINE.get()))
                .where('H', blocks(GTBlocks.CASING_GRATE.get()))
                .where('I', frames(GTMaterials.Trinium))
                .where('J', blocks(GTNABlocks.PROCESS_MACHINE_CASING.get()))
                .where('K', blocks(GTNABlocks.OXIDATION_RESISTANT_HASTELLOY_N_MECHANICAL_CASING.get()))
                .where('L', blocks(GTNABlocks.TITANIUM_NITRIDE_CERAMIC_IMPACT_RESISTANT_MECHANICAL_BLOCK.get()))
                .where('M', blocks(GTBlocks.CASING_LAMINATED_GLASS.get()))
                .where('N', blocks(GTBlocks.CASING_TEMPERED_GLASS.get()))
                .where('O', blocks(GTBlocks.CASING_POLYTETRAFLUOROETHYLENE_PIPE.get()))
                .where('P', blocks(Blocks.IRON_BARS))
                .where('Q', controller(blocks(definition.getBlock())))
                .where(' ', any())
                .build();
    }

    private static BlockPattern buildComponentAssemblerExtensionWide(MultiblockMachineDefinition definition) {
        return FactoryBlockPattern.start()
                .aisle("AAADDDAAA AAAGGGAAA AAADDDAAA", "BBCBBBCBBAA AGFGA AABBCBBBCBB",
                        "BBCBIBCBB AAAGGGAAA BBCBEBCBB", "BBCBIBCBB    GGG    BBCBEBCBB",
                        "BBCBIBCBB    GFG    BBCBEBCBB", "  CBBBC      GGG      CBBBC  ")
                .aisle("AAAAAAAAAAA A   A AAAAAAAAAAA", "BB BIB BBFFFFFFFFFFFBB BEB BB",
                        "BB BIB BBAA AGFGA AABB BEB BB", "BB BIB BB    GFG    BB BEB BB",
                        "BB BIB BB    FFF    BB BEB BB", "  CBBBC      GFG      CBBBC  ")
                .aisle("AAAAAAAAA AAAGGGAAA AAAAAAAAA", "BB BBB BBAAFAGFGAFAABB BBB BB",
                        "BB BIB BB AAAGGGAAA BB BEB BB", "BB BIB BB    GGG    BB BEB BB",
                        "BB BIB BB    G G    BB BEB BB", "  CBBBC      GGG      CBBBC  ")
                .aisle("AAAAAAAAA    G G    AAAAAAAAA", "C  III  C  F  F  F  C  EEE  C",
                        "C  III  C    GFG    C  EEE  C", "C  III  C    GFG    C  EEE  C",
                        "C  III  C    FFF    C  EEE  C", "CCCCCCCCC    GFG    CCCCCCCCC")
                .aisle("AAAAAAAAA AAAGGGAAA AAAAAAAAA", "BB BBB BB AFAGFGAFA BB BBB BB",
                        "BB BIB BB AAAGGGAAA BB BEB BB", "BB BIB BB    GGG    BB BEB BB",
                        "BB BIB BB    G G    BB BEB BB", "  CBBBC      GGG      CBBBC  ")
                .aisle("AAAAAAAAA    G G    AAAAAAAAA", "BB BIB BB  F  F  F  BB BEB BB",
                        "BB BIB BB    GFG    BB BEB BB", "BB BIB BB    GFG    BB BEB BB",
                        "BB BIB BB    FFF    BB BEB BB", "  CBBBC      GFG      CBBBC  ")
                .aisle("AAAAAAAAA AAAGGGAAA AAAAAAAAA", "BBCBBBCBB AFAG GAFA BBCBBBCBB",
                        "BBCBIBCBB AAAGGGAAA BBCBEBCBB", "BBCBIBCBB    GGG    BBCBEBCBB",
                        "BBCBIBCBB    G G    BBCBEBCBB", "  CBBBC      GGG      CBBBC  ")
                .aisle("           A     A           ", "          AFA   AFA          ",
                        "           A     A           ", "                             ",
                        "                             ", "                             ")
                .aisle("                             ", "          A A   A A          ",
                        "                             ", "                             ",
                        "                             ", "                             ")
                .aisle("                             ", "                             ",
                        "                             ", "                             ",
                        "                             ", "                             ")
                .aisle("                             ", "                             ",
                        "                             ", "                             ",
                        "                             ", "                             ")
                .aisle("                             ", "                             ",
                        "                             ", "                             ",
                        "                             ", "                             ")
                .aisle("                             ", "                             ",
                        "                             ", "                             ",
                        "                             ", "                             ")
                .aisle("                             ", "                             ",
                        "                             ", "                             ",
                        "                             ", "                             ")
                .aisle("                             ", "                             ",
                        "                             ", "                             ",
                        "                             ", "                             ")
                .aisle("                             ", "                             ",
                        "                             ", "                             ",
                        "                             ", "                             ")
                .aisle("                             ", "                             ",
                        "                             ", "                             ",
                        "                             ", "                             ")
                .aisle("                             ", "                             ",
                        "                             ", "                             ",
                        "                             ", "                             ")
                .aisle("                             ", "                             ",
                        "                             ", "                             ",
                        "                             ", "                             ")
                .aisle("              H              ", "                             ",
                        "                             ", "                             ",
                        "                             ", "                             ")
                .where('A', blocks(GCYMBlocks.CASING_NONCONDUCTING.get()))
                .where('B', blocks(GTNABlocks.THREE_PROOF_COMPUTER_CASING.get()))
                .where('C', frames(GTNAMaterials.CarbonFiberPolyphenyleneSulfideComposite))
                .where('D', blocks(GTBlocks.CASING_STEEL_SOLID.get())
                        .or(abilities(PartAbility.INPUT_LASER).setMaxGlobalLimited(2))
                        .or(abilities(PartAbility.PARALLEL_HATCH).setMaxGlobalLimited(1)))
                .where('E', blocks(GTNABlocks.MACHINING_CONTROL_CASING_MK2.get()))
                .where('F', blocks(GTNABlocks.ELECTRIC_POWER_TRANSMISSION_CASING.get()))
                .where('G', blocks(GTBlocks.CASING_PALLADIUM_SUBSTATION.get()))
                .where('H', controller(blocks(definition.getBlock())))
                .where('I', blocks(GTNABlocks.ENERGY_CONTROL_CASING_MK2.get()))
                .where(' ', any())
                .build();
    }

    /**
     * GTOCore's rocket engine module for the Rocket Large Turbine
     * ({@code MachineRegisterUtils.registerLargeTurbine}, ROCKET_ENGINE_FUELS branch): the titanium
     * shell with the engine-intake nose, a turbine-casing back wall and a BlueSteel frame ring that
     * wraps the 3×3×3 turbine base. GTO grants the machine 2× output, +20% efficiency and a 2×
     * rotor damage multiplier while the module is formed ({@code formedAmount > 0}); its 'D' cells
     * additionally accept up to three Energy Output Hatches.
     */
    private static void registerRocketLargeTurbineModule() {
        GTNASubPatterns.register(new ResourceLocation("gtna", "rocket_large_turbine"),
                GTNAModules::buildRocketLargeTurbineExtension,
                Component.translatable("gtna.machine.auxiliary_module").withStyle(ChatFormatting.GOLD),
                Component.translatable("gtna.machine.rocket_large_turbine.module_bonus"));
    }

    private static BlockPattern buildRocketLargeTurbineExtension(MultiblockMachineDefinition definition) {
        return FactoryBlockPattern.start()
                .aisle("AAAAAAA", "A   ABA", "A   ABA", "AAAAAAA")
                .aisle("    CCD", "    CCD", "    CCD", "A   ABA")
                .aisle("    CCD", "    FFF", "    CCD", "A   ABA")
                .aisle("    CCD", " E  CCD", "    CCD", "A   ABA")
                .aisle("AAAAAAA", "A   ABA", "A   ABA", "AAAAAAA")
                .where('A', blocks(GTBlocks.CASING_TITANIUM_STABLE.get()))
                .where('B', blocks(GTBlocks.CASING_ENGINE_INTAKE.get()))
                .where('C', blocks(GTBlocks.CASING_TITANIUM_TURBINE.get()))
                .where('D', blocks(GTBlocks.CASING_TITANIUM_TURBINE.get())
                        .or(abilities(PartAbility.OUTPUT_ENERGY).setMaxGlobalLimited(3)))
                .where('E', controller(blocks(definition.getBlock())))
                .where('F', frames(GTMaterials.BlueSteel))
                .where(' ', any())
                .build();
    }

    /**
     * GTOCore's SUPERCRITICAL module for the Supercritical Steam Turbine
     * ({@code MachineRegisterUtils.registerLargeTurbine}, SUPERCRITICAL_STEAM_TURBINE_FUELS branch):
     * the heat-resistant shell with an electrolytic-cell nose, a supercritical turbine casing back
     * wall and a TungstenSteel frame ring that wraps the 3×3×3 turbine base. GTO grants the machine
     * 2× output, +20% efficiency and a 2× rotor damage multiplier while the module is formed
     * ({@code formedAmount > 0}); its 'D' cells additionally accept up to three Energy Output
     * Hatches.
     */
    private static void registerSupercriticalSteamTurbineModule() {
        GTNASubPatterns.register(new ResourceLocation("gtna", "supercritical_steam_turbine"),
                GTNAModules::buildSupercriticalSteamTurbineExtension,
                Component.translatable("gtna.machine.auxiliary_module").withStyle(ChatFormatting.GOLD),
                Component.translatable("gtna.machine.supercritical_steam_turbine.module_bonus"));
    }

    private static BlockPattern buildSupercriticalSteamTurbineExtension(MultiblockMachineDefinition definition) {
        return FactoryBlockPattern.start()
                .aisle("AAAAAAA", "A   ABA", "A   ABA", "AAAAAAA")
                .aisle("    CCD", "    CCD", "    CCD", "A   ABA")
                .aisle("    CCD", "    FFF", "    CCD", "A   ABA")
                .aisle("    CCD", " E  CCD", "    CCD", "A   ABA")
                .aisle("AAAAAAA", "A   ABA", "A   ABA", "AAAAAAA")
                .where('A', blocks(GCYMBlocks.CASING_HIGH_TEMPERATURE_SMELTING.get()))
                .where('B', blocks(GCYMBlocks.ELECTROLYTIC_CELL.get()))
                .where('C', blocks(GTNABlocks.SUPERCRITICAL_TURBINE_CASING.get()))
                .where('D', blocks(GTNABlocks.SUPERCRITICAL_TURBINE_CASING.get())
                        .or(abilities(PartAbility.OUTPUT_ENERGY).setMaxGlobalLimited(3)))
                .where('E', controller(blocks(definition.getBlock())))
                .where('F', frames(GTMaterials.TungstenSteel))
                .where(' ', any())
                .build();
    }

    /**
     * GTOCore Cold Ice Freezer auxiliary module ({@code MultiBlockD.COLD_ICE_FREEZER.addSubPattern}):
     * the Naquadah-framed tower with Naquadah Alloy casing and heat vents that unlocks the
     * {@code atomization_condensation} recipe type plus one Accelerate Hatch and up to six extra
     * Energy Hatches. GTOCore's {@code ACCELERATE_HATCH} is the GTO part ability; GTNA maps it to its
     * own {@link GTNAPartAbility#ACCELERATE_HATCH}, since GTCEu 7.5.3 has no such ability.
     *
     * <p>
     * The pattern is copied from GTOCore with the default GTCEu directions, exactly like the
     * original sub-pattern (the main structure uses {@code RIGHT/UP/BACK}).
     */
    private static void registerColdIceFreezerModule() {
        GTNASubPatterns.register(new ResourceLocation("gtna", "cold_ice_freezer"),
                GTNAModules::buildColdIceFreezerExtension,
                moduleTooltips(new String[] { "accelerate", "extra_energy" },
                        com.raishxn.gtna.common.data.GTNARecipeType.ATOMIZATION_CONDENSATION_RECIPES));
    }

    private static BlockPattern buildColdIceFreezerExtension(MultiblockMachineDefinition definition) {
        return FactoryBlockPattern.start()
                .aisle("    AAA", "    BBB", "    BBB", "    DDD", "    DDD", "    DDD", "    DDD")
                .aisle("   EAAA", "   EDFB", "   EDFB", "   EDFD", "    DFD", "    DFD", "    DDD")
                .aisle("   FAAA", "   FDDB", "  FFDDB", "   EDDD", "    DDD", "    DDD", "    DDD")
                .aisle("   EGGG", "   EG H", "   EG H", "   EGGG", "       ", "       ", "       ")
                .aisle("    GGG", "    G H", "    G H", "    GGG", "       ", "       ", "       ")
                .aisle("   EGGG", "   EG H", "   EG H", "   EGGG", "       ", "       ", "       ")
                .aisle("   FAAA", "   FDDB", "  FFDDB", "   EDDD", "    DDD", "    DDD", "    DDD")
                .aisle("   EAAA", "   EDFB", "   EDFB", "   EDFD", "    DFD", "    DFD", "    DDD")
                .aisle("    AAA", "    BBB", "C   BBB", "    DDD", "    DDD", "    DDD", "    DDD")
                .where('A', blocks(GTBlocks.CASING_ALUMINIUM_FROSTPROOF.get()))
                .where('B', blocks(GTNABlocks.COLD_ICE_CASING.get())
                        .or(abilities(PartAbility.INPUT_ENERGY).setMaxGlobalLimited(6))
                        .or(abilities(GTNAPartAbility.ACCELERATE_HATCH).setMaxGlobalLimited(1)))
                .where('C', controller(blocks(definition.getBlock())))
                .where('D', blocks(GTNABlocks.COLD_ICE_CASING.get()))
                .where('E', frames(GTMaterials.Naquadah))
                .where('F', blocks(GTBlocks.CASING_TUNGSTENSTEEL_PIPE.get()))
                .where('G', blocks(GTNABlocks.NAQUADAH_ALLOY_CASING.get()))
                .where('H', blocks(GCYMBlocks.HEAT_VENT.get()))
                .where(' ', any())
                .build();
    }

    /**
     * Module for GTCEu's Electric Blast Furnace, ported from GTOCore
     * ({@code GTMachineModify#ELECTRIC_BLAST_FURNACE.setSubPatternFactory}): an invar heatproof shell
     * that wraps the front and sides of the furnace, made of heatproof casing, stainless-steel frames
     * and steel pipe casing. Its heatproof-casing cells accept the furnace's IO plus one
     * additional Energy Hatch and one Accelerate Hatch — what GTOCore's
     * {@code moduleTooltips(ACCELERATE_HATCH, EXTRA_ENERGY_HATCH)} advertises.
     *
     * <p>
     * Cells that overlap the furnace itself are {@code any()}.
     */
    private static void registerElectricBlastFurnaceModule() {
        GTNASubPatterns.register(new ResourceLocation("gtceu", "electric_blast_furnace"),
                definition -> FactoryBlockPattern.start()
                        .aisle("AAAAA", " DBD ", " DBD ", " CCC ")
                        .aisle("ACCCA", "BD DB", "BD DB", "CCCCC")
                        .aisle("A   A", "     ", "     ", "C   C")
                        .aisle("A   A", "B   B", "B   B", "C   C")
                        .aisle("A E A", "     ", "     ", "     ")
                        .where('A', blocks(GTBlocks.CASING_INVAR_HEATPROOF.get())
                                .or(autoAbilities(definition.getRecipeTypes(), false, false, true, true, true, true))
                                .or(wiredEnergyHatches().setMaxGlobalLimited(1))
                                .or(abilities(GTNAPartAbility.ACCELERATE_HATCH).setMaxGlobalLimited(1)))
                        .where('B', blocks(ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.StainlessSteel)))
                        .where('C', blocks(GTBlocks.CASING_INVAR_HEATPROOF.get()))
                        .where('D', blocks(GTBlocks.CASING_STEEL_PIPE.get()))
                        .where('E', controller(blocks(definition.getBlock())))
                        .where(' ', any())
                        .build(),
                moduleTooltips("accelerate", "extra_energy"));
    }

    /** GTCEu's wired 2A/4A/16A hatches, excluding GTNA wireless hatches with the same ability. */
    private static com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate wiredEnergyHatches() {
        Block[] blocks = Stream.of(GTMachines.ENERGY_INPUT_HATCH, GTMachines.ENERGY_INPUT_HATCH_4A,
                GTMachines.ENERGY_INPUT_HATCH_16A)
                .flatMap(Arrays::stream)
                .filter(Objects::nonNull)
                .map(MachineDefinition::getBlock)
                .toArray(Block[]::new);
        return blocks(blocks);
    }

    /**
     * Module for GTNA's Liquefaction Furnace (GTOCore port): GTOCore's stainless-steel tower that
     * sits beside the furnace and adds one Parallel Hatch and one Accelerate Hatch. The furnace is a
     * normal coil machine that accepts none of those on its own, so the module is the only way to get
     * them.
     *
     * <p>
     * GTOCore anchors the tower at the controller with the <i>default</i> pattern directions, which
     * places it to the right of the main structure; the cells that overlap the furnace are
     * {@code any()}.
     */
    private static void registerLiquefactionFurnaceModule() {
        GTNASubPatterns.register(new ResourceLocation("gtna", "liquefaction_furnace"),
                GTNAModules::buildLiquefactionExtension,
                moduleTooltips("parallel", "accelerate"));
    }

    /** The auxiliary-module text stays the same; each machine supplies only its unlocked hatches. */
    private static Component[] moduleTooltips(String... hatchIds) {
        return moduleTooltips(hatchIds, new com.gregtechceu.gtceu.api.recipe.GTRecipeType[0]);
    }

    /**
     * GTOCore {@code moduleTooltips(abilities, recipeTypes)}: the auxiliary-module block advertises
     * both the unlocked hatch types and the unlocked recipe types. The recipe types are rendered
     * through the GTCEu category translation key ({@code <namespace>.<path>}).
     */
    private static Component[] moduleTooltips(String[] hatchIds,
                                              com.gregtechceu.gtceu.api.recipe.GTRecipeType... recipeTypes) {
        MutableComponent hatchList = Component.empty();
        for (int i = 0; i < hatchIds.length; i++) {
            if (i > 0) hatchList.append(", ");
            hatchList.append(Component.translatable("gtna.machine.auxiliary_module.hatch." + hatchIds[i])
                    .withStyle(ChatFormatting.AQUA));
        }
        java.util.List<Component> lines = new java.util.ArrayList<>();
        lines.add(Component.translatable("gtna.machine.auxiliary_module").withStyle(ChatFormatting.GOLD));
        lines.add(Component.translatable("gtna.machine.auxiliary_module.description"));
        lines.add(Component.translatable("gtna.machine.auxiliary_module.hatches"));
        lines.add(Component.translatable("gtna.machine.auxiliary_module.unlocked", hatchList));
        if (recipeTypes.length > 0) {
            MutableComponent recipeTypeList = Component.empty();
            for (int i = 0; i < recipeTypes.length; i++) {
                if (i > 0) recipeTypeList.append(", ");
                recipeTypeList.append(Component.translatable(recipeTypes[i].registryName.toLanguageKey())
                        .withStyle(ChatFormatting.AQUA));
            }
            lines.add(Component.translatable("gtna.machine.auxiliary_module.recipe_types", recipeTypeList));
        }
        return lines.toArray(Component[]::new);
    }

    private static BlockPattern buildLiquefactionExtension(MultiblockMachineDefinition definition) {
        return FactoryBlockPattern.start()
                .aisle("AAA    ", "AAA    ", "AAA    ")
                .aisle("BBB    ", "BDB    ", "BBB    ")
                .aisle("BEBF   ", "E EF   ", "BEBF   ")
                .aisle("BEBG   ", "E E    ", "BEBG   ")
                .aisle("BEBF   ", "E EF   ", "BEBF   ")
                .aisle("BBB   C", "BDB    ", "BBB    ")
                .aisle("AAA    ", "AAA    ", "AAA    ")
                .where('A', blocks(GTBlocks.CASING_INVAR_HEATPROOF.get())
                        .or(abilities(PartAbility.PARALLEL_HATCH).setMaxGlobalLimited(1))
                        .or(abilities(GTNAPartAbility.ACCELERATE_HATCH).setMaxGlobalLimited(1)))
                .where('B', blocks(GTBlocks.CASING_STAINLESS_TURBINE.get()))
                .where('C', controller(blocks(definition.getBlock())))
                .where('D', blocks(GTBlocks.CASING_STAINLESS_STEEL_GEARBOX.get()))
                .where('E', blocks(GTBlocks.CASING_STAINLESS_CLEAN.get()))
                .where('F', blocks(ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.StainlessSteel)))
                .where('G', blocks(GTBlocks.CASING_TITANIUM_PIPE.get()))
                .where(' ', any())
                .build();
    }

    /** GTOCore's titanium auxiliary tower for the Evaporation Plant. */
    private static void registerEvaporationPlantModule() {
        GTNASubPatterns.register(new ResourceLocation("gtna", "evaporation_plant"),
                GTNAModules::buildEvaporationPlantExtension,
                moduleTooltips("parallel", "accelerate"));
    }

    private static BlockPattern buildEvaporationPlantExtension(MultiblockMachineDefinition definition) {
        // The F and G cells have the same visible casing, and the outer titanium shell is where
        // players can actually reach the tower. Reuse each limited predicate across all three
        // surfaces so either hatch may be installed without creating a second copy of the bonus.
        var performanceHatches = abilities(PartAbility.PARALLEL_HATCH).setMaxGlobalLimited(1)
                .or(abilities(GTNAPartAbility.ACCELERATE_HATCH).setMaxGlobalLimited(1));
        return FactoryBlockPattern.start()
                .aisle("HFH AAA", "FFF ACA", "FFF ACA", "FFF ACA", " G  AAA")
                .aisle("FGGAAAA", "FDGDA A", "FDGDA A", "FDGAA A", " G  AEA")
                .aisle("    AAA", "    A A", "    A A", "    A A", "    AAA")
                .aisle("   AAAA", "   DA A", "   DA A", "   DA A", "   AAEA")
                .aisle("    AAA", " B  ACA", "    ACA", "    ACA", "    AAA")
                .where('A', blocks(GTBlocks.CASING_TITANIUM_STABLE.get()).or(performanceHatches))
                .where('B', controller(blocks(definition.getBlock())))
                .where('C', blocks(GTBlocks.FIREBOX_TITANIUM.get()))
                .where('D', blocks(GTBlocks.CASING_TITANIUM_PIPE.get()))
                .where('E', abilities(PartAbility.MUFFLER))
                .where('F', blocks(GTNABlocks.STAINLESS_EVAPORATION_CASING.get())
                        .or(GTNAMachines3.nonSteamFluidInputHatches())
                        .or(abilities(PartAbility.EXPORT_FLUIDS))
                        .or(performanceHatches))
                .where('G', blocks(GTNABlocks.STAINLESS_EVAPORATION_CASING.get()).or(performanceHatches))
                .where('H', frames(GTMaterials.Aluminium))
                .where(' ', any())
                .build();
    }
}
