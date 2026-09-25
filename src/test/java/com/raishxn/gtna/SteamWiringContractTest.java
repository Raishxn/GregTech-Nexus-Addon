package com.raishxn.gtna;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Guards the wiring contract between GTNA's steam parts and the multiblock patterns that are
 * supposed to accept them.
 *
 * <p>
 * Why this exists: the wireless steam input hatch was rejected by every GTNA steam multiblock
 * because the patterns pinned the steam slot to the <b>exact</b> stock block
 * ({@code blocks(GTMachines.STEAM_HATCH.getBlock())}) instead of the ability
 * ({@code abilities(PartAbility.STEAM)}). The part declared the STEAM ability correctly, so the
 * bug was invisible to logic-only unit tests and to the curated gametests. GTCEu's own steam
 * multiblocks (steam grinder/oven) use the ability form, and that is what a part's ability is
 * for. The gametest {@code wirelessSteamHatchIsAcceptedAsSteamSource} proves the runtime side;
 * this test keeps the whole class of regression from coming back in code.
 *
 * <p>
 * The unit test source set does not inherit Minecraft's libraries, so this is deliberately a
 * source scan (like {@code ConfigLangKeysTest}) rather than a reflection/registry test. GTLCore
 * style: {@code main()} + asserts.
 */
public final class SteamWiringContractTest {

    private static final Path DATA_DIR = Path.of("src/main/java/com/raishxn/gtna/common/data");
    private static final Path MACHINES_SOURCE = DATA_DIR.resolve("GTNAMachines.java");
    private static final Path STEAM_PART_DIR = Path
            .of("src/main/java/com/raishxn/gtna/common/machine/multiblock/part/steam");
    private static final Path ELEVATOR_DIR = Path
            .of("src/main/java/com/raishxn/gtna/common/machine/multiblock/module/steamElevator");

    /** Pinning a steam slot to this exact block excludes any part that only has the STEAM ability. */
    private static final String EXACT_HATCH_PIN = "blocks(GTMachines.STEAM_HATCH";

    /** Steam hatches must not advertise the universal fluid abilities. */
    private static final String INPUT_HATCH_ABILITIES = ".abilities(PartAbility.STEAM)";

    /** The output hatch only exports steam; declaring STEAM would let it occupy the energy slot. */
    private static final String OUTPUT_HATCH_ABILITIES = ".abilities(GTNAPartAbility.STEAM_EXPORT_FLUIDS)";

    /** The ability-based predicate GTCEu itself uses for the steam-source slot. */
    private static final String ABILITY_PIN = "abilities(PartAbility.STEAM)";

    private SteamWiringContractTest() {}

    public static void main(String[] args) throws IOException {
        checkNoExactSteamHatchPin();
        checkHatchAbilityRoles();
        checkSteamSlotsUseAbility();
        checkWirelessSteamAccounting();
        checkWirelessSteamMovesWholeBuffer();
        checkWirelessSteamFairShare();
        checkWirelessSteamHudWiring();
        checkSteamElevatorHostDoesNotLimitModuleHatches();
        checkFlightModuleSurvivesArmorCleanup();
        checkSolarBoilerProductionIsPerSecond();
        checkElevatorHasNoEuBuffer();
        System.out.println("[SteamWiringContractTest] all cases passed");
    }

    /** No multiblock pattern may pin its steam slot to the exact stock hatch block. */
    private static void checkNoExactSteamHatchPin() throws IOException {
        List<String> offenders = new ArrayList<>();
        try (Stream<Path> files = Files.walk(DATA_DIR)) {
            for (Path file : files.filter(p -> p.toString().endsWith(".java")).toList()) {
                List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
                for (int i = 0; i < lines.size(); i++) {
                    if (lines.get(i).contains(EXACT_HATCH_PIN)) {
                        offenders.add(file + ":" + (i + 1));
                    }
                }
            }
        }
        if (!offenders.isEmpty()) {
            throw new AssertionError("steam slot pinned to the exact stock hatch block (breaks the wireless " +
                    "steam input hatch and any other STEAM-ability part): " + offenders +
                    " - use abilities(PartAbility.STEAM) instead");
        }
    }

    /** The input hatch is a steam source; the output hatch must not be one. */
    private static void checkHatchAbilityRoles() throws IOException {
        String allMachines = Files.readString(MACHINES_SOURCE, StandardCharsets.UTF_8);
        String source = allMachines.substring(allMachines.indexOf("// --- INPUT HATCHES (Recebe Vapor) ---"),
                allMachines.indexOf("public static final MachineDefinition HUGE_STEAM_INPUT_BUS"));
        if (source.split(java.util.regex.Pattern.quote(INPUT_HATCH_ABILITIES), -1).length - 1 < 2) {
            throw new AssertionError("the wireless steam input hatch no longer declares " +
                    INPUT_HATCH_ABILITIES + " in " + MACHINES_SOURCE +
                    "; both tiers must stay steam-only sources");
        }
        if (source.split(java.util.regex.Pattern.quote(OUTPUT_HATCH_ABILITIES), -1).length - 1 != 2) {
            throw new AssertionError("both wireless steam output tiers must declare only " +
                    OUTPUT_HATCH_ABILITIES);
        }
        if (source.contains(".abilities(PartAbility.STEAM, IMPORT_FLUIDS)") ||
                source.contains(".abilities(EXPORT_FLUIDS)")) {
            throw new AssertionError("wireless steam hatches must not advertise universal fluid abilities");
        }
    }

    /** At least one machine must actually use the ability-based steam slot (the fix is in place). */
    private static void checkSteamSlotsUseAbility() throws IOException {
        String source = Files.readString(MACHINES_SOURCE, StandardCharsets.UTF_8);
        int count = source.split(java.util.regex.Pattern.quote(ABILITY_PIN), -1).length - 1;
        if (count == 0) {
            throw new AssertionError("no machine in " + MACHINES_SOURCE + " accepts a steam source by ability (" +
                    ABILITY_PIN + "); the steam slot is pinned to exact blocks again");
        }
    }

    /**
     * The wireless steam hatches must simulate the tank transfer before touching the network, so a
     * full/odd tank can never void steam (GTNL {@code tryFetchingSteam} order: simulate fill →
     * charge exactly the accepted amount → execute fill; simulate drain → add exactly the drained
     * amount → execute drain).
     */
    private static void checkWirelessSteamAccounting() throws IOException {
        String input = Files.readString(STEAM_PART_DIR.resolve("WirelessSteamInputHatch.java"),
                StandardCharsets.UTF_8);
        int inputSim = input.indexOf("FluidAction.SIMULATE");
        int inputConsume = input.indexOf("consumeSteamFromGlobalMap");
        int inputExec = input.indexOf("FluidAction.EXECUTE");
        if (!(inputSim >= 0 && inputConsume > inputSim && inputExec > inputConsume)) {
            throw new AssertionError("WirelessSteamInputHatch must simulate the fill, charge the network for " +
                    "exactly the accepted amount, then execute the fill (so it cannot void steam)");
        }
        // consumeSteamFromGlobalMap is all-or-nothing; the request must be clamped to the network's
        // actual balance first, otherwise a network holding less than one transfer tick is never
        // drained (the reported "input hatch shows no steam" bug).
        int inputAvailable = input.indexOf("getUserSteam");
        if (!(inputAvailable >= 0 && inputAvailable < inputSim)) {
            throw new AssertionError("WirelessSteamInputHatch must clamp its pull to the network balance " +
                    "(getUserSteam) before requesting the transfer");
        }

        String output = Files.readString(STEAM_PART_DIR.resolve("WirelessSteamOutputHatch.java"),
                StandardCharsets.UTF_8);
        int outputSim = output.indexOf("FluidAction.SIMULATE");
        int outputAdd = output.indexOf("addSteamToGlobalSteamMap");
        int outputExec = output.indexOf("FluidAction.EXECUTE");
        if (!(outputSim >= 0 && outputAdd > outputSim && outputExec > outputAdd)) {
            throw new AssertionError("WirelessSteamOutputHatch must simulate the drain, add exactly the drained " +
                    "amount to the network, then execute the drain (so it cannot duplicate steam)");
        }
    }

    /**
     * The wireless steam hatches must move the whole buffer by default (GTNL
     * {@code WirelessSteamDynamoHatch} parity): a boiler dumps a whole 20-tick recipe cycle at once,
     * so a hardcoded bronze cap of 10,000 mB/t plus a 20,000 mB buffer stranded &gt;90% of it and read
     * as "steam does not enter the network". The buffer must hold a large cycle and the transfer cap
     * must default to unlimited (configurable downward only).
     */
    private static void checkWirelessSteamMovesWholeBuffer() throws IOException {
        Path config = Path.of("src/main/java/com/raishxn/gtna/config/ConfigHolder.java");
        String configSource = Files.readString(config, StandardCharsets.UTF_8);
        if (!configSource.contains("public int bronzeOutputBuffer = 128000000")) {
            throw new AssertionError("the bronze wireless steam OUTPUT buffer must be large enough to hold a " +
                    "big boiler's whole recipe cycle (GTNL parity: 128,000,000 mB); a small buffer voids the " +
                    "rest of the cycle in ConfigHolder.WirelessSteam");
        }
        if (!configSource.contains("public int bronzeInputBuffer = 100000")) {
            throw new AssertionError("the bronze wireless steam INPUT buffer must stay small (100,000 mB = " +
                    "100 buckets): a huge input buffer let one hatch hoard the pool and starve the rest of " +
                    "the network (the reported 'network always 0' bug)");
        }
        for (String field : List.of("steelInputBuffer", "steelOutputBuffer")) {
            if (!configSource.contains("public int " + field + " = Integer.MAX_VALUE")) {
                throw new AssertionError(field + " must default to Integer.MAX_VALUE (GTNL steel parity)");
            }
        }
        for (String field : List.of("bronzeTransferRate", "steelTransferRate")) {
            if (!configSource.contains("public int " + field + " = Integer.MAX_VALUE")) {
                throw new AssertionError(field + " must default to Integer.MAX_VALUE (move the whole buffer " +
                        "each tick, GTNL parity); a finite default strands production behind the cap");
            }
        }

        String output = Files.readString(STEAM_PART_DIR.resolve("WirelessSteamOutputHatch.java"),
                StandardCharsets.UTF_8);
        if (!(output.contains("getTransferRate()") && output.contains("Math.min(currentSteam"))) {
            throw new AssertionError("WirelessSteamOutputHatch must cap its push by getTransferRate() clamped to " +
                    "the current tank contents (whole buffer), not by a fixed literal");
        }
        if (output.contains("Math.min(currentSteam, transferRate)")) {
            throw new AssertionError("WirelessSteamOutputHatch still caps the push at the raw transferRate field; " +
                    "it must go through getTransferRate() so a zero/legacy value is treated as unlimited");
        }
        if (!output.contains("bronzeOutputBuffer")) {
            throw new AssertionError("WirelessSteamOutputHatch must size its tank from the OUTPUT buffer config");
        }
        String input = Files.readString(STEAM_PART_DIR.resolve("WirelessSteamInputHatch.java"),
                StandardCharsets.UTF_8);
        if (!input.contains("getTransferRate()")) {
            throw new AssertionError("WirelessSteamInputHatch must clamp its pull by getTransferRate() so the " +
                    "whole free space/balance can be pulled by default");
        }
        if (!input.contains("bronzeInputBuffer")) {
            throw new AssertionError("WirelessSteamInputHatch must size its tank from the INPUT buffer config");
        }
    }

    /**
     * The input hatch must share the pool: with several inputs connected, a hatch that requests
     * the whole balance lets the first one in tick order drain everything, so the network always
     * reads 0 and the other machines starve. The pull has to be divided over the live inputs that
     * still have space. The runtime side is covered by
     * {@code wirelessSteamDistributesAcrossManyInputs}.
     */
    private static void checkWirelessSteamFairShare() throws IOException {
        String input = Files.readString(STEAM_PART_DIR.resolve("WirelessSteamInputHatch.java"),
                StandardCharsets.UTF_8);
        if (!input.contains("getActiveInputCount")) {
            throw new AssertionError("WirelessSteamInputHatch must divide the network balance over the live " +
                    "inputs that have space (getActiveInputCount); otherwise one hatch drains the whole pool " +
                    "every tick and every other machine starves");
        }
        int fairShare = input.indexOf("fairShare");
        int consume = input.indexOf("consumeSteamFromGlobalMap");
        if (!(fairShare >= 0 && fairShare < consume)) {
            throw new AssertionError("WirelessSteamInputHatch must compute its fair share before consuming from " +
                    "the network, not after");
        }
        for (String file : List.of("WirelessSteamInputHatch.java", "WirelessSteamOutputHatch.java")) {
            String source = Files.readString(STEAM_PART_DIR.resolve(file), StandardCharsets.UTF_8);
            if (source.contains("setWorkingEnabled(false)")) {
                throw new AssertionError(file + " still disables the GTCEu AUTO IO toggle, which makes Jade " +
                        "report 'Working Disabled' for a working wireless hatch; keep AUTO IO off by overriding " +
                        "updateTankSubscription() instead");
            }
        }
    }

    /**
     * The wireless steam HUD is client-only, so neither the unit gate nor the dedicated-server
     * gametest can exercise it: this source scan keeps its four moving parts wired together — the
     * config toggle (off by default, GTOCore {@code wirelessEnergyHUDEnabled} parity), the packet
     * registration, the client overlay registration and the server-side sampler. The runtime half
     * of the sampler is covered by {@code wirelessSteamHudSnapshotReportsNetworkState}.
     */
    private static void checkWirelessSteamHudWiring() throws IOException {
        Path config = Path.of("src/main/java/com/raishxn/gtna/config/ConfigHolder.java");
        String configSource = Files.readString(config, StandardCharsets.UTF_8);
        if (!configSource.contains("public boolean wirelessSteamHud = false")) {
            throw new AssertionError("the wireless steam HUD must default to off in ConfigHolder.Client " +
                    "(GTOCore wirelessEnergyHUDEnabled parity), so it never surprises a player who did not ask " +
                    "for it");
        }

        Path network = Path.of("src/main/java/com/raishxn/gtna/network/GTNANetworkHandler.java");
        String networkSource = Files.readString(network, StandardCharsets.UTF_8);
        if (!networkSource.contains("SWirelessSteamStats.class")) {
            throw new AssertionError("SWirelessSteamStats is not registered in GTNANetworkHandler; the client " +
                    "would never receive a HUD snapshot");
        }

        Path overlay = Path.of("src/main/java/com/raishxn/gtna/client/hud/WirelessSteamHudOverlay.java");
        String overlaySource = Files.readString(overlay, StandardCharsets.UTF_8);
        if (!overlaySource.contains("registerAboveAll")) {
            throw new AssertionError("WirelessSteamHudOverlay does not register itself with " +
                    "RegisterGuiOverlaysEvent; the HUD would never render");
        }
        if (!overlaySource.contains("wirelessSteamHud")) {
            throw new AssertionError("WirelessSteamHudOverlay does not read the wirelessSteamHud config toggle");
        }
        if (!overlaySource.contains("implements IGuiOverlay, IMoveableHud") ||
                !overlaySource.contains("HudEditorScreen.register")) {
            throw new AssertionError("WirelessSteamHudOverlay must be an IMoveableHud registered with the " +
                    "HUD editor, so the drag system can move it");
        }

        Path editor = Path.of("src/main/java/com/raishxn/gtna/client/hud/HudEditorScreen.java");
        String editorSource = Files.readString(editor, StandardCharsets.UTF_8);
        if (!editorSource.contains("mouseDragged") || !editorSource.contains("mouseReleased") ||
                !editorSource.contains("getAnchorBounds")) {
            throw new AssertionError("HudEditorScreen must forward drag events to the moveable HUDs and outline " +
                    "them while dragging");
        }

        // GTOCore parity: the toggle lives in the machine UI, not on a keybind. The common hatch
        // builds the button and calls the client-only bridge.
        Path bridge = Path.of("src/main/java/com/raishxn/gtna/client/hud/WirelessSteamHudBridge.java");
        String bridgeSource = Files.readString(bridge, StandardCharsets.UTF_8);
        if (!bridgeSource.contains("toggleHud") || !bridgeSource.contains("openEditor")) {
            throw new AssertionError("WirelessSteamHudBridge must expose the toggle and editor hooks the hatch UI " +
                    "calls");
        }
        for (String file : List.of("WirelessSteamInputHatch.java", "WirelessSteamOutputHatch.java")) {
            String hatch = Files.readString(STEAM_PART_DIR.resolve(file), StandardCharsets.UTF_8);
            if (!hatch.contains("WirelessSteamHudBridge") || !hatch.contains("onHudButton") ||
                    !hatch.contains("new ButtonWidget")) {
                throw new AssertionError(file + " must carry the HUD button that calls WirelessSteamHudBridge " +
                        "(GTOCore substation parity)");
            }
        }

        Path sync = Path.of("src/main/java/com/raishxn/gtna/common/WirelessSteamHudSync.java");
        String syncSource = Files.readString(sync, StandardCharsets.UTF_8);
        if (!syncSource.contains("SYNC_INTERVAL_TICKS") || !syncSource.contains("sendToPlayer") ||
                !syncSource.contains("snapshot(")) {
            throw new AssertionError("WirelessSteamHudSync must sample on its interval, build a snapshot and send " +
                    "it to the player");
        }
    }

    /**
     * The Steam Elevator host must not limit the abilities that its own modules can carry. The
     * module slots sit inside the host volume, so a module's steam/item/fluid hatch lands on a host
     * shell cell; a {@code setMaxGlobalLimited(1)} there counted every module's hatch as the host's
     * and failed the whole tower with "Maximum: 1" (the reported "only one module can have a steam
     * hatch" bug). The module pattern is what limits each module to one hatch.
     */
    private static void checkSteamElevatorHostDoesNotLimitModuleHatches() throws IOException {
        String machines = Files.readString(DATA_DIR.resolve("GTNAMachines.java"), StandardCharsets.UTF_8);
        int start = machines.indexOf("private static BlockPattern createSteamElevatorPattern");
        if (start < 0) {
            throw new AssertionError("createSteamElevatorPattern not found in GTNAMachines.java");
        }
        int end = machines.indexOf("/**", start + 1);
        String pattern = end < 0 ? machines.substring(start) : machines.substring(start, end);
        if (pattern.contains("PartAbility.STEAM).setMaxGlobalLimited") ||
                pattern.contains("PartAbility.IMPORT_FLUIDS).setMaxGlobalLimited") ||
                pattern.contains("PartAbility.EXPORT_FLUIDS).setMaxGlobalLimited")) {
            throw new AssertionError("the Steam Elevator host pattern still globally limits a module-facing " +
                    "ability; module hatches inside the host volume would trip it with 'Maximum: 1'");
        }
        if (!pattern.contains("abilities(PartAbility.STEAM)")) {
            throw new AssertionError("the Steam Elevator host pattern must still accept STEAM parts (module " +
                    "hatches land on the host shell)");
        }
    }

    /**
     * The Steam Elevator flight module grants creative flight by setting {@code mayfly}; the
     * Quantum Cosmic Nexus armor handler runs every player tick and used to clear {@code mayfly}
     * for anyone not wearing the armor, silently cancelling the module. Its legacy cleanup must only
     * act when the armor's quantum flying speed remnant is present.
     */
    private static void checkFlightModuleSurvivesArmorCleanup() throws IOException {
        Path armor = Path.of("src/main/java/com/raishxn/gtna/common/item/armor/QuantumCosmicNexusArmorHandler.java");
        String source = Files.readString(armor, StandardCharsets.UTF_8);
        if (!source.contains("if (legacyQuantumSpeed) {")) {
            throw new AssertionError("QuantumCosmicNexusArmorHandler#disableManagedFlight must gate the legacy " +
                    "mayfly cleanup on legacyQuantumSpeed; clearing mayfly unconditionally every tick cancels the " +
                    "Steam Elevator flight module's grant");
        }
    }

    /**
     * The solar boiler's reported production must be the real per-second rate. The old
     * {@code lastSteamOutput = steamOut * 20} was a 20x over-report (steamOut is already the amount
     * for the whole 20-tick cycle, i.e. one second), so the machine display read 6,240,000 L/s for a
     * field that actually makes 312,000 mB/s and disagreed with Jade's per-craft recipe output.
     */
    private static void checkSolarBoilerProductionIsPerSecond() throws IOException {
        Path boiler = Path.of(
                "src/main/java/com/raishxn/gtna/common/machine/multiblock/steam/LargeSteamSolarBoilerMachine.java");
        String source = Files.readString(boiler, StandardCharsets.UTF_8);
        if (source.contains("(long) steamOut * 20L;") || source.contains("lastSteamOutput = (long) steamOut")) {
            throw new AssertionError("LargeSteamSolarBoilerMachine still multiplies the per-cycle batch by 20 to get " +
                    "a 'per second' value; steamOut is already per cycle (= per second at the 20-tick cycle)");
        }
        if (!source.contains("steamPerSecond = (long) steamOut * 20L / TICK_INTERVAL")) {
            throw new AssertionError("LargeSteamSolarBoilerMachine must scale its per-cycle batch to a per-second " +
                    "rate (steamOut * 20 / TICK_INTERVAL) so the display and Jade agree with the actual output");
        }
        if (!source.contains("solarBoilerSteamPerCell")) {
            throw new AssertionError("LargeSteamSolarBoilerMachine must read its per-cell production from the " +
                    "solarBoilerSteamPerCell config (the hardcoded 200 was buffed 20x)");
        }
    }

    /**
     * The elevator and its modules must have no EU buffer: the author's model is "the elevator always
     * runs; the modules pay a steam upkeep drawn from the structure's steam input hatches".
     */
    private static void checkElevatorHasNoEuBuffer() throws IOException {
        List<String> banned = List.of("energyBuffer", "storedEnergy", "receiveEnergy", "consumeEnergy",
                "MAX_ENERGY");
        for (String file : List.of("SteamElevator.java", "SteamElevatorModuleMachine.java")) {
            String source = Files.readString(ELEVATOR_DIR.resolve(file), StandardCharsets.UTF_8);
            for (String token : banned) {
                if (source.contains(token)) {
                    throw new AssertionError(file + " still contains \"" + token +
                            "\"; the elevator/modules must have no EU buffer or EU upkeep");
                }
            }
        }
    }
}
