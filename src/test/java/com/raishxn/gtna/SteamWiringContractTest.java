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

    /** Pinning a steam slot to this exact block excludes any part that only has the STEAM ability. */
    private static final String EXACT_HATCH_PIN = "blocks(GTMachines.STEAM_HATCH";

    /** The input hatch must serve as a steam source: STEAM ability + fluid import. */
    private static final String INPUT_HATCH_ABILITIES = ".abilities(PartAbility.STEAM, IMPORT_FLUIDS)";

    /** The output hatch only exports steam; declaring STEAM would let it occupy the energy slot. */
    private static final String OUTPUT_HATCH_WITH_STEAM = ".abilities(PartAbility.STEAM, EXPORT_FLUIDS)";

    /** The ability-based predicate GTCEu itself uses for the steam-source slot. */
    private static final String ABILITY_PIN = "abilities(PartAbility.STEAM)";

    private SteamWiringContractTest() {}

    public static void main(String[] args) throws IOException {
        checkNoExactSteamHatchPin();
        checkHatchAbilityRoles();
        checkSteamSlotsUseAbility();
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
        String source = Files.readString(MACHINES_SOURCE, StandardCharsets.UTF_8);
        if (!source.contains(INPUT_HATCH_ABILITIES)) {
            throw new AssertionError("the wireless steam input hatch no longer declares " +
                    INPUT_HATCH_ABILITIES + " in " + MACHINES_SOURCE +
                    "; it must stay a STEAM + IMPORT_FLUIDS part");
        }
        if (source.contains(OUTPUT_HATCH_WITH_STEAM)) {
            throw new AssertionError("the wireless steam output hatch declares the STEAM ability " +
                    "(matches " + OUTPUT_HATCH_WITH_STEAM + "); with the ability-based steam slot it could " +
                    "occupy the machine's energy slot and invalidate the structure - it is a pure " +
                    "EXPORT_FLUIDS part");
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
}
