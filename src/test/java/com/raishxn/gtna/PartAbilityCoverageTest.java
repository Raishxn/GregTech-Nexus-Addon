package com.raishxn.gtna;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Guards the other half of the wiring contract: a GTNA part that declares a custom ability must be
 * accepted by at least one machine pattern. Otherwise the part is craftable but can never be placed
 * (the orphan Thread Hatch class of bug, see G-0012).
 *
 * <p>
 * Source scan on purpose: the unit test source set does not inherit Minecraft's libraries, so this
 * mirrors {@code SteamWiringContractTest} (GTLCore style: {@code main()} + asserts).
 */
public final class PartAbilityCoverageTest {

    private static final Path DATA_DIR = Path.of("src/main/java/com/raishxn/gtna/common/data");
    private static final Path MACHINES_SOURCE = DATA_DIR.resolve("GTNAMachines.java");
    private static final Path PARTS_SOURCE = DATA_DIR.resolve("GTNAMachines2.java");

    private static final Pattern GTNA_ABILITY = Pattern.compile("GTNAPartAbility\\.([A-Z_]+)");

    private PartAbilityCoverageTest() {}

    public static void main(String[] args) throws IOException {
        Set<String> declared = declaredByParts();
        Set<String> accepted = acceptedByMachines();
        Set<String> orphans = new LinkedHashSet<>(declared);
        orphans.removeAll(accepted);
        if (!orphans.isEmpty()) {
            throw new AssertionError("GTNA part abilities not accepted by any machine pattern (craftable but " +
                    "unplaceable): " + orphans + " - add abilities(GTNAPartAbility.X) to a machine pattern or drop " +
                    "the part");
        }
        System.out.println("[PartAbilityCoverageTest] " + declared.size() + " part abilities all accepted: " +
                declared);
    }

    /** Custom abilities declared by a GTNA part (registrate {@code .abilities(...)} call). */
    private static Set<String> declaredByParts() throws IOException {
        Set<String> declared = new LinkedHashSet<>();
        for (String line : Files.readAllLines(PARTS_SOURCE, StandardCharsets.UTF_8)) {
            if (line.contains(".abilities(") && !line.contains("Predicates.abilities(")) {
                collect(line, declared);
            }
        }
        return declared;
    }

    /** Custom abilities accepted by a machine pattern. */
    private static Set<String> acceptedByMachines() throws IOException {
        Set<String> accepted = new LinkedHashSet<>();
        for (String line : Files.readAllLines(MACHINES_SOURCE, StandardCharsets.UTF_8)) {
            if (line.contains("abilities(GTNAPartAbility.")) {
                collect(line, accepted);
            }
        }
        for (String line : Files.readAllLines(PARTS_SOURCE, StandardCharsets.UTF_8)) {
            if (line.contains("Predicates.abilities(GTNAPartAbility.")) {
                collect(line, accepted);
            }
        }
        return accepted;
    }

    private static void collect(String line, Set<String> out) {
        Matcher matcher = GTNA_ABILITY.matcher(line);
        while (matcher.find()) {
            out.add(matcher.group(1));
        }
    }
}
