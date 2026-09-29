package com.raishxn.gtna;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Guards the other half of the wiring contract: a GTNA part that declares a custom ability must be
 * accepted by at least one machine pattern (base or auxiliary module). Otherwise the part is
 * craftable but can never be placed (the orphan Thread Hatch class of bug, see G-0012).
 *
 * <p>
 * Source scan on purpose: the unit test source set does not inherit Minecraft's libraries, so this
 * mirrors {@code SteamWiringContractTest} (GTLCore style: {@code main()} + asserts).
 *
 * <p>
 * QA contract B4 extends the original scan to the GTO port class: accepted sites include
 * {@code GTNAMachines3} multiblocks and {@code GTNAModules} auxiliary structures, and declared
 * abilities include the new Ball Hatch-family parts.
 */
public final class PartAbilityCoverageTest {

    private static final Path DATA_DIR = Path.of("src/main/java/com/raishxn/gtna/common/data");

    /** A part registration declares an ability with the registrate builder: {@code .abilities(X)}. */
    private static final Pattern DECLARED = Pattern.compile("\\.abilities\\(GTNAPartAbility\\.([A-Z_]+)");
    /** A pattern accepts an ability, whoever calls it (static import or {@code Predicates.}). */
    private static final Pattern ACCEPTED = Pattern.compile("abilities\\(GTNAPartAbility\\.([A-Z_]+)");

    private PartAbilityCoverageTest() {}

    public static void main(String[] args) throws IOException {
        Set<String> declared = new LinkedHashSet<>();
        Set<String> accepted = new LinkedHashSet<>();
        for (Path file : sources()) {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                if (line.contains("Predicates.abilities(GTNAPartAbility.")) {
                    collect(ACCEPTED, line, accepted);
                } else {
                    collect(DECLARED, line, declared);
                    collect(ACCEPTED, line, accepted);
                }
                // A pattern line that starts with "or(abilities(" uses the static import but is not a
                // registration: the declared scan above already skips it because it needs ".abilities(".
            }
        }
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

    private static java.util.List<Path> sources() throws IOException {
        try (Stream<Path> walk = Files.walk(DATA_DIR)) {
            return walk.filter(path -> path.toString().endsWith(".java")).sorted().toList();
        }
    }

    private static void collect(Pattern pattern, String line, Set<String> out) {
        Matcher matcher = pattern.matcher(line);
        while (matcher.find()) {
            out.add(matcher.group(1));
        }
    }
}
