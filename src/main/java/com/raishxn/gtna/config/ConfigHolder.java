package com.raishxn.gtna.config;

import dev.toma.configuration.Configuration;
import dev.toma.configuration.config.Config;
import dev.toma.configuration.config.Configurable;
import dev.toma.configuration.config.Configurable.Comment;
import dev.toma.configuration.config.Configurable.Range;
import dev.toma.configuration.config.format.ConfigFormats;

@Config(id = "gtna")
public class ConfigHolder {

    public static ConfigHolder INSTANCE;
    private static final Object LOCK = new Object();

    @Configurable
    @Comment({ "En: Gameplay Settings", "Pt: Configuracoes de gameplay" })
    public Gameplay gameplay = new Gameplay();

    @Configurable
    @Comment({ "En: Client Settings", "Pt: Configuracoes do cliente" })
    public Client client = new Client();

    @Configurable
    @Comment({ "En: Machine Settings", "Pt: Configuracoes de maquinas" })
    public Machines machines = new Machines();

    @Configurable
    @Comment({ "En: Restricted item rules", "Pt: Regras de itens restritos" })
    public RestrictedItems restrictedItems = new RestrictedItems();

    @Configurable
    @Comment({ "En: Wireless steam network rules", "Pt: Regras da rede wireless de vapor" })
    public WirelessSteam wirelessSteam = new WirelessSteam();

    @Configurable
    @Comment({ "En: Per-machine server toggles", "Pt: Toggles de servidor por maquina" })
    public MachineToggles machineToggles = new MachineToggles();

    @Configurable
    @Comment({ "En: Per-hatch server toggles", "Pt: Toggles de servidor por hatch" })
    public HatchToggles hatchToggles = new HatchToggles();

    public enum ModDifficulty {
        JOURNEY,
        NORMAL
    }

    public static boolean isJourneyMode() {
        return INSTANCE != null && INSTANCE.gameplay.modDifficulty == ModDifficulty.JOURNEY;
    }

    public static boolean isSelfRestraintEnabled() {
        return INSTANCE != null && INSTANCE.gameplay.selfRestraint;
    }

    public static boolean areRestrictedItemsEnabled() {
        return INSTANCE != null && isJourneyMode() && !isSelfRestraintEnabled() &&
                !INSTANCE.restrictedItems.disableUsage;
    }

    public static boolean areRestrictedRecipesEnabled() {
        return areRestrictedItemsEnabled() && INSTANCE != null && !INSTANCE.restrictedItems.disableRecipes;
    }

    public static boolean shouldHideRestrictedItemsFromJei() {
        return INSTANCE == null || INSTANCE.restrictedItems.hideFromJei;
    }

    public static boolean isRestrictedGroupAllowed(String groupId) {
        if (!areRestrictedItemsEnabled() || INSTANCE == null) {
            return false;
        }
        return switch (groupId) {
            case "infinityCovers" -> INSTANCE.restrictedItems.allowInfinityCovers;
            case "outputBoostParts" -> INSTANCE.restrictedItems.allowOutputBoostParts;
            case "infiniteInputParts" -> INSTANCE.restrictedItems.allowInfiniteInputParts;
            case "quantumCosmicNexusArmor" -> INSTANCE.restrictedItems.allowQuantumCosmicNexusArmor;
            case "realityRipper" -> INSTANCE.restrictedItems.allowRealityRipper;
            default -> true;
        };
    }

    public static boolean isMachineEnabled(String machineId) {
        if (INSTANCE == null) return true;
        return switch (machineId) {
            case "largeSteamCrusher" -> INSTANCE.machineToggles.largeSteamCrusher;
            case "largeSteamFurnace" -> INSTANCE.machineToggles.largeSteamFurnace;
            case "largeSteamAlloySmelter" -> INSTANCE.machineToggles.largeSteamAlloySmelter;
            case "largeSteamHammer" -> INSTANCE.machineToggles.largeSteamHammer;
            case "largeSteamCompressor" -> INSTANCE.machineToggles.largeSteamCompressor;
            case "largeSteamExtractor" -> INSTANCE.machineToggles.largeSteamExtractor;
            case "largeSteamOreWasher" -> INSTANCE.machineToggles.largeSteamOreWasher;
            case "primitiveDistillationTower" -> INSTANCE.machineToggles.primitiveDistillationTower;
            case "largeSteamLathe" -> INSTANCE.machineToggles.largeSteamLathe;
            case "largeSteamCutting" -> INSTANCE.machineToggles.largeSteamCutting;
            case "largeSteamBending" -> INSTANCE.machineToggles.largeSteamBending;
            case "largeSteamExtruder" -> INSTANCE.machineToggles.largeSteamExtruder;
            case "largeSteamWiremill" -> INSTANCE.machineToggles.largeSteamWiremill;
            case "largeSteamSifter" -> INSTANCE.machineToggles.largeSteamSifter;
            case "steamLavaMaker" -> INSTANCE.machineToggles.steamLavaMaker;
            case "steamItemVault" -> INSTANCE.machineToggles.steamItemVault;
            case "steamCactusWonder" -> INSTANCE.machineToggles.steamCactusWonder;
            case "steamCracking" -> INSTANCE.machineToggles.steamCracking;
            case "megaSteamCompressor" -> INSTANCE.machineToggles.megaSteamCompressor;
            case "steamElevator" -> INSTANCE.machineToggles.steamElevator;
            case "steamElevatorModules" -> INSTANCE.machineToggles.steamElevatorModules;
            case "steamApiaryModule" -> INSTANCE.machineToggles.steamApiaryModule;
            case "steamBeeBreedingModule" -> INSTANCE.machineToggles.steamBeeBreedingModule;
            case "largeSteamFormingPress" -> INSTANCE.machineToggles.largeSteamFormingPress;
            case "steamCobbler" -> INSTANCE.machineToggles.steamCobbler;
            case "stoneSuperheater" -> INSTANCE.machineToggles.stoneSuperheater;
            case "steamManufacturer" -> INSTANCE.machineToggles.steamManufacturer;
            case "steamWoodcutter" -> INSTANCE.machineToggles.steamWoodcutter;
            case "leapForwardOneBlastFurnace" -> INSTANCE.machineToggles.leapForwardOneBlastFurnace;
            case "infernalCokeOven" -> INSTANCE.machineToggles.infernalCokeOven;
            case "hyperPressureReactor" -> INSTANCE.machineToggles.hyperPressureReactor;
            case "compactHyperPressureReactor" -> INSTANCE.machineToggles.compactHyperPressureReactor;
            case "voidMinerSteamGateAged" -> INSTANCE.machineToggles.voidMinerSteamGateAged;
            case "industrialSlaughterhouse" -> INSTANCE.machineToggles.industrialSlaughterhouse;
            case "artificialStar" -> INSTANCE.machineToggles.artificialStar;
            case "eyeOfHarmony" -> INSTANCE.machineToggles.eyeOfHarmony;
            case "eyeOfWood" -> INSTANCE.machineToggles.eyeOfWood;
            case "nexusMolecularForge" -> INSTANCE.machineToggles.nexusMolecularForge;
            case "nexusMeHypercore" -> INSTANCE.machineToggles.nexusMeHypercore;
            case "meStorage" -> INSTANCE.machineToggles.meStorage;
            case "integratedOreProcessor" -> INSTANCE.machineToggles.integratedOreProcessor;
            case "advancedIntegratedOreProcessor" -> INSTANCE.machineToggles.advancedIntegratedOreProcessor;
            case "brickKiln" -> INSTANCE.machineToggles.brickKiln;
            case "thermalPowerPump" -> INSTANCE.machineToggles.thermalPowerPump;
            case "liquefactionFurnace" -> INSTANCE.machineToggles.liquefactionFurnace;
            case "durationTester" -> INSTANCE.machineToggles.durationTester;
            default -> true;
        };
    }

    public static boolean isHatchEnabled(String hatchId) {
        if (INSTANCE == null) return true;
        return switch (hatchId) {
            case "wirelessSteamInputBronze" -> INSTANCE.hatchToggles.wirelessSteamInputBronze;
            case "wirelessSteamInputSteel" -> INSTANCE.hatchToggles.wirelessSteamInputSteel;
            case "wirelessSteamOutputBronze" -> INSTANCE.hatchToggles.wirelessSteamOutputBronze;
            case "wirelessSteamOutputSteel" -> INSTANCE.hatchToggles.wirelessSteamOutputSteel;
            case "hugeSteamInputBus" -> INSTANCE.hatchToggles.hugeSteamInputBus;
            case "hugeSteamOutputBus" -> INSTANCE.hatchToggles.hugeSteamOutputBus;
            case "infiniteSteamInputBus" -> INSTANCE.hatchToggles.infiniteSteamInputBus;
            case "outputBoostSteamOutputBus" -> INSTANCE.hatchToggles.outputBoostSteamOutputBus;
            case "advancedParallelHatches" -> INSTANCE.hatchToggles.advancedParallelHatches;
            case "accelerateHatches" -> INSTANCE.hatchToggles.accelerateHatches;
            case "threadHatches" -> INSTANCE.hatchToggles.threadHatches;
            case "overclockHatches" -> INSTANCE.hatchToggles.overclockHatches;
            case "outputBoostHatches" -> INSTANCE.hatchToggles.outputBoostHatches;
            case "infiniteInputBuses" -> INSTANCE.hatchToggles.infiniteInputBuses;
            case "infiniteInputHatches" -> INSTANCE.hatchToggles.infiniteInputHatches;
            case "outputBoostItemBuses" -> INSTANCE.hatchToggles.outputBoostItemBuses;
            case "outputBoostFluidHatches" -> INSTANCE.hatchToggles.outputBoostFluidHatches;
            case "meMiniPatternBuffer" -> INSTANCE.hatchToggles.meMiniPatternBuffer;
            case "mePatternBuffer" -> INSTANCE.hatchToggles.mePatternBuffer;
            case "meAdvancedPatternBuffer" -> INSTANCE.hatchToggles.meAdvancedPatternBuffer;
            case "meUltimatePatternBuffer" -> INSTANCE.hatchToggles.meUltimatePatternBuffer;
            case "meCraftPatternHatch" -> INSTANCE.hatchToggles.meCraftPatternHatch;
            case "mePatternBufferProxy" -> INSTANCE.hatchToggles.mePatternBufferProxy;
            case "meStorageAccessHatch" -> INSTANCE.hatchToggles.meStorageAccessHatch;
            case "meBigStorageAccessHatch" -> INSTANCE.hatchToggles.meBigStorageAccessHatch;
            case "meIOPortHatch" -> INSTANCE.hatchToggles.meIOPortHatch;
            default -> true;
        };
    }

    public static class Gameplay {

        @Configurable
        @Comment({ "En: Mod difficulty. Journey enables restricted utility items, while Normal keeps them disabled.",
                "Pt: Dificuldade do mod. Journey libera itens utilitarios restritos, enquanto Normal os mantem desabilitados." })
        public ModDifficulty modDifficulty = ModDifficulty.NORMAL;

        @Configurable
        @Comment({ "En: Self restraint mode disables restricted cheat-like GTNA items.",
                "Pt: Self restraint desabilita itens restritos e com cara de cheat do GTNA." })
        public boolean selfRestraint = false;
    }

    public static class Client {

        @Configurable
        @Comment({ "En: Disable fly inertia when the player stops moving in the air.",
                "Pt: Remove a inercia do voo quando o jogador para de se mover no ar." })
        public boolean disableFlyInertia = true;

        @Configurable
        @Comment({ "En: Show the wireless steam network HUD (balance, flow and connected hatches).",
                "Pt: Mostra o HUD da rede wireless de vapor (saldo, fluxo e hatches conectados)." })
        public boolean wirelessSteamHud = false;

        @Configurable
        @Range(min = 0, max = 100)
        @Comment({ "En: Wireless steam HUD horizontal position: 0 = left edge, 100 = right edge.",
                "Pt: Posicao horizontal do HUD de vapor: 0 = esquerda, 100 = direita." })
        public int wirelessSteamHudX = 5;

        @Configurable
        @Range(min = 0, max = 100)
        @Comment({ "En: Wireless steam HUD vertical position: 0 = top edge, 100 = bottom edge.",
                "Pt: Posicao vertical do HUD de vapor: 0 = topo, 100 = baixo." })
        public int wirelessSteamHudY = 75;

        @Configurable
        @Range(min = 0, max = 600)
        @Comment({ "En: Seconds of balance history drawn as a graph in the HUD (0 hides the graph).",
                "Pt: Segundos de historico de saldo no grafico do HUD (0 esconde o grafico)." })
        public int wirelessSteamHudHistorySeconds = 60;
    }

    public static class RestrictedItems {

        @Configurable
        @Comment("En: Hide restricted items from JEI and creative tabs when disabled.")
        public boolean hideFromJei = true;

        @Configurable
        @Comment("En: Disable recipes that use the broad restricted-items condition.")
        public boolean disableRecipes = false;

        @Configurable
        @Comment("En: Disable restricted item usage entirely unless explicitly allowed below.")
        public boolean disableUsage = false;

        @Configurable
        @Comment("En: Allow infinity covers.")
        public boolean allowInfinityCovers = true;

        @Configurable
        @Comment("En: Allow output boost parts.")
        public boolean allowOutputBoostParts = true;

        @Configurable
        @Comment("En: Allow infinite input parts.")
        public boolean allowInfiniteInputParts = true;

        @Configurable
        @Comment("En: Allow Quantum Cosmic Nexus armor.")
        public boolean allowQuantumCosmicNexusArmor = true;

        @Configurable
        @Comment("En: Allow Reality Ripper.")
        public boolean allowRealityRipper = true;
    }

    public static class WirelessSteam {

        @Configurable
        @Comment("En: Master switch for the wireless steam network.")
        public boolean enabled = true;

        @Configurable
        @Comment({ "En: Tank capacity for the bronze wireless steam INPUT hatch (network -> machine).",
                "En: Deliberately small: a huge input buffer let a single hatch hoard the whole pool,",
                "En: which starved every other machine and made the network read 0 mB (the reported",
                "En: 'network always 0' bug). The input hatch only bridges the gap until its machine",
                "En: consumes the steam. 100,000 mB = 100 buckets." })
        public int bronzeInputBuffer = 100000;

        @Configurable
        @Comment({ "En: Tank capacity for the steel wireless steam INPUT hatch (network -> machine).",
                "En: Integer.MAX_VALUE = effectively unbounded, like GTNL's steel wireless energy hatch." })
        public int steelInputBuffer = Integer.MAX_VALUE;

        @Configurable
        @Comment({ "En: Tank capacity for the bronze wireless steam OUTPUT hatch (boiler -> network).",
                "En: GTNL parity: the bronze wireless dynamo holds 128,000,000 mB. The buffer is the",
                "En: real per-tick throughput limit: a large boiler can only move what fits in the tank,",
                "En: so it must hold a whole recipe cycle (a 41x42 solar boiler makes 6,240,000 mB per",
                "En: 20-tick cycle at the current balance). A small buffer voids the rest of the cycle." })
        public int bronzeOutputBuffer = 128000000;

        @Configurable
        @Comment("En: Tank capacity for the steel wireless steam OUTPUT hatch (boiler -> network).")
        public int steelOutputBuffer = Integer.MAX_VALUE;

        @Configurable
        @Comment({ "En: Optional per-tick transfer limit for bronze wireless steam hatches.",
                "En: Integer.MAX_VALUE (the default) moves the whole buffer every tick, exactly like",
                "En: GTNL's WirelessSteamDynamoHatch, so a boiler never strands its production. Lower",
                "En: it only to deliberately throttle the wireless network." })
        public int bronzeTransferRate = Integer.MAX_VALUE;

        @Configurable
        @Comment("En: Optional per-tick transfer limit for steel wireless steam hatches (default: whole buffer).")
        public int steelTransferRate = Integer.MAX_VALUE;
    }

    public static class MachineToggles {

        @Configurable
        public boolean largeSteamCrusher = true;
        @Configurable
        public boolean largeSteamFurnace = true;
        @Configurable
        public boolean largeSteamAlloySmelter = true;
        @Configurable
        public boolean largeSteamHammer = true;
        @Configurable
        public boolean largeSteamCompressor = true;
        @Configurable
        public boolean largeSteamExtractor = true;
        @Configurable
        public boolean largeSteamOreWasher = true;
        @Configurable
        public boolean primitiveDistillationTower = true;
        @Configurable
        public boolean largeSteamLathe = true;
        @Configurable
        public boolean largeSteamCutting = true;
        public boolean largeSteamBending = true;
        public boolean largeSteamExtruder = true;
        public boolean largeSteamWiremill = true;
        public boolean largeSteamSifter = true;
        public boolean steamLavaMaker = true;
        public boolean steamItemVault = true;
        @Configurable
        public boolean steamCactusWonder = true;
        @Configurable
        public boolean steamCracking = true;
        @Configurable
        public boolean megaSteamCompressor = true;
        @Configurable
        public boolean steamElevator = true;
        @Configurable
        public boolean steamElevatorModules = true;
        @Configurable
        public boolean steamApiaryModule = true;
        @Configurable
        public boolean steamBeeBreedingModule = true;
        @Configurable
        public boolean largeSteamFormingPress = true;
        @Configurable
        public boolean steamCobbler = true;
        @Configurable
        public boolean stoneSuperheater = true;
        @Configurable
        public boolean steamManufacturer = true;
        @Configurable
        public boolean steamWoodcutter = true;
        @Configurable
        public boolean leapForwardOneBlastFurnace = true;
        @Configurable
        public boolean infernalCokeOven = true;
        @Configurable
        public boolean hyperPressureReactor = true;
        @Configurable
        public boolean compactHyperPressureReactor = true;
        @Configurable
        public boolean voidMinerSteamGateAged = true;
        @Configurable
        public boolean industrialSlaughterhouse = true;
        @Configurable
        public boolean artificialStar = true;
        @Configurable
        public boolean eyeOfHarmony = true;
        @Configurable
        public boolean eyeOfWood = true;
        @Configurable
        public boolean nexusMolecularForge = true;
        @Configurable
        public boolean nexusMeHypercore = true;
        @Configurable
        public boolean meStorage = true;
        @Configurable
        public boolean integratedOreProcessor = true;
        @Configurable
        public boolean advancedIntegratedOreProcessor = true;
        @Configurable
        public boolean brickKiln = true;
        @Configurable
        public boolean thermalPowerPump = true;
        @Configurable
        public boolean liquefactionFurnace = true;
        @Configurable
        public boolean durationTester = true;
    }

    public static class HatchToggles {

        @Configurable
        public boolean wirelessSteamInputBronze = true;
        @Configurable
        public boolean wirelessSteamInputSteel = true;
        @Configurable
        public boolean wirelessSteamOutputBronze = true;
        @Configurable
        public boolean wirelessSteamOutputSteel = true;
        @Configurable
        public boolean hugeSteamInputBus = true;
        @Configurable
        public boolean hugeSteamOutputBus = true;
        @Configurable
        public boolean infiniteSteamInputBus = true;
        @Configurable
        public boolean outputBoostSteamOutputBus = true;
        @Configurable
        public boolean advancedParallelHatches = true;
        @Configurable
        public boolean accelerateHatches = true;
        @Configurable
        public boolean threadHatches = true;
        @Configurable
        public boolean overclockHatches = true;
        @Configurable
        public boolean outputBoostHatches = true;
        @Configurable
        public boolean infiniteInputBuses = true;
        @Configurable
        public boolean infiniteInputHatches = true;
        @Configurable
        public boolean outputBoostItemBuses = true;
        @Configurable
        public boolean outputBoostFluidHatches = true;
        @Configurable
        public boolean meMiniPatternBuffer = true;
        @Configurable
        public boolean mePatternBuffer = true;
        @Configurable
        public boolean meAdvancedPatternBuffer = true;
        @Configurable
        public boolean meUltimatePatternBuffer = true;
        @Configurable
        public boolean meCraftPatternHatch = true;
        @Configurable
        public boolean mePatternBufferProxy = true;
        @Configurable
        public boolean meStorageAccessHatch = true;
        @Configurable
        public boolean meBigStorageAccessHatch = true;
        @Configurable
        public boolean meIOPortHatch = true;
    }

    public static class Machines {

        @Configurable
        @Range(min = 1, max = 100)
        @Comment("En: Energy Cost Multiplier for Accelerate Hatch.")
        public double accelerateHatchEnergyCost = 1.5;

        @Configurable
        @Comment({ "En: Let a GTNA ME Pattern Buffer switch the machine mode of GTCEu multiblocks",
                "En: (e.g. the Large Cutter between cutter and lathe). The buffer only drives the",
                "En: mode while the machine is idle, so it never interrupts work; the buffer's own",
                "En: per-slot mode pins and its buffer-level filter stay the manual control.",
                "Pt: Permite que um ME Pattern Buffer do GTNA troque o modo de maquinas do GTCEu",
                "Pt: (ex: Large Cutter entre cutter e lathe) enquanto a maquina estiver ociosa." })
        public boolean bufferDrivenMachineMode = true;

        @Configurable
        @Comment({ "En: Enable RaishxCore crafting planner for Nexus ME Hypercore grids",
                "Pt: Ativar planner RaishxCore nas redes do Nexus ME Hypercore" })
        public boolean nexusPlannerEnabled = true;

        @Configurable
        @Range(min = 1000, max = 1000000)
        public int wirelessSteamTransferRate = 8192;

        @Configurable
        @Range(min = 1, max = 100000)
        @Comment({ "En: Steam produced per sunlit solar boiling cell per 20-tick cycle in the",
                "En: Large Steam Solar Boiler (mB). 20x the original 200 mB: a 41x42 field makes",
                "En: ~6,240,000 mB per second instead of ~312,000, which matches the massive",
                "En: structure cost.",
                "Pt: Vapor por celula solar por ciclo de 20 ticks no Large Steam Solar Boiler (mB).",
                "Pt: 20x o valor original de 200 mB: um campo 41x42 passa a fazer ~6.240.000 mB/s." })
        public int solarBoilerSteamPerCell = 4000;

        @Configurable
        @Range(min = 1, max = 64)
        @Comment("En: Output Multiplier for Dense Steam.")
        public int voidMinerDenseOutputMult = 2;

        @Configurable
        @Range(min = 1, max = 128)
        @Comment("En: Speed Multiplier for Dense Steam.")
        public double voidMinerDenseSpeedMult = 2.0;

        @Configurable
        @Range(min = 1, max = 128)
        @Comment("En: Energy Cost Multiplier for Dense Steam.")
        public double voidMinerDenseEnergyMult = 1.5;

        @Configurable
        @Range(min = 1, max = 64)
        @Comment("En: Output Multiplier for SuperHeated Steam.")
        public int voidMinerSuperHeatedOutputMult = 3;

        @Configurable
        @Range(min = 1, max = 128)
        @Comment("En: Speed Multiplier for SuperHeated Steam.")
        public double voidMinerSuperHeatedSpeedMult = 3.0;

        @Configurable
        @Range(min = 1, max = 128)
        @Comment("En: Energy Cost Multiplier for SuperHeated Steam.")
        public double voidMinerSuperHeatedEnergyMult = 2.0;

        @Configurable
        @Range(min = 1, max = 64)
        @Comment("En: Output Multiplier for Insanely Steam.")
        public int voidMinerInsanelyOutputMult = 5;

        @Configurable
        @Range(min = 1, max = 128)
        @Comment("En: Speed Multiplier for Insanely Steam.")
        public double voidMinerInsanelySpeedMult = 5.0;

        @Configurable
        @Range(min = 1, max = 128)
        @Comment("En: Energy Cost Multiplier for Insanely Steam.")
        public double voidMinerInsanelyEnergyMult = 4.0;

        @Configurable
        @Comment("Nexus Flux Matrix Configuration")
        public NexusFluxMatrixConfig nexusFluxMatrix = new NexusFluxMatrixConfig();
    }

    public static class NexusFluxMatrixConfig {

        @Configurable
        @Range(min = 0, max = 100)
        @Comment("Base efficiency loss percentage at Tier 1 (LV).")
        public double baseLossPercent = 15.0;

        @Configurable
        @Comment("Max transfer capacity per tick for a MAX Tier array.")
        public String maxTransferTierMAX = "500000000000000000000000";

        @Configurable
        @Comment("If false, efficiency uses the average capacitor tier. If true, it uses the highest installed tier.")
        public boolean useHighestTierForEfficiency = false;
    }

    public static void init() {
        synchronized (LOCK) {
            if (INSTANCE == null) {
                INSTANCE = (ConfigHolder) Configuration.registerConfig(ConfigHolder.class, ConfigFormats.yaml())
                        .getConfigInstance();
            }
        }
    }
}
