package com.raishxn.gtna;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * QA contract (B2): the Industrial Flotation Cell / Vacuum Drying Furnace chain must stay closed.
 * Every {@code *Front} fluid (and {@code RedMud}) that a GTNA recipe produces must be consumed by
 * another GTNA recipe, so the chain does not create dead-end fluids.
 *
 * <p>
 * GTCEu registers addon recipes through a dynamic data pack instead of writing JSON under
 * {@code src/generated/resources/data/gtna/recipes} (this project's datagen only emits assets and
 * lang), so this scans the recipe builders in {@code data/recipe}: any {@code outputFluids(...)}
 * or {@code outputItems(...)} line that names a {@code *Front}/{@code RedMud} identifier counts as
 * production, any {@code inputFluids(...)}/{@code inputItems(...)} line as consumption. Identifiers
 * are compared by name, so the test works before and after the port lands. GTLCore style:
 * {@code main()} + asserts.
 */
public final class PortChainClosureTest {

    private static final Path RECIPE_DIR = Path.of("src/main/java/com/raishxn/gtna/data/recipe");
    private static final Pattern IDENTIFIER = Pattern.compile("\\b([A-Za-z0-9_]*Front|RedMud)\\b");
    private static final Pattern PRODUCTION = Pattern.compile("\\boutput(Fluids|Items)\\s*\\(");
    private static final Pattern CONSUMPTION = Pattern.compile("\\binput(Fluids|Items)\\s*\\(");

    private PortChainClosureTest() {}

    public static void main(String[] args) throws IOException {
        Set<String> produced = new LinkedHashSet<>();
        Set<String> consumed = new LinkedHashSet<>();
        List<Path> files;
        try (Stream<Path> walk = Files.walk(RECIPE_DIR)) {
            files = walk.filter(path -> path.toString().endsWith(".java")).sorted().toList();
        }
        for (Path file : files) {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                boolean produces = PRODUCTION.matcher(line).find();
                boolean consumes = CONSUMPTION.matcher(line).find();
                if (!produces && !consumes) {
                    continue;
                }
                Matcher matcher = IDENTIFIER.matcher(line);
                while (matcher.find()) {
                    (produces ? produced : consumed).add(matcher.group(1));
                }
            }
        }
        List<String> deadEnds = new ArrayList<>(produced);
        deadEnds.removeAll(consumed);
        if (!deadEnds.isEmpty()) {
            throw new AssertionError("*Front/RedMud fluids produced without a consuming GTNA recipe: " + deadEnds +
                    " - add the drying/dehydration consumer or a processing recipe before shipping the chain");
        }
        if (produced.isEmpty()) {
            System.out.println("[PortChainClosureTest] no *Front/RedMud production exists yet (chain not ported)");
        } else {
            System.out.println("[PortChainClosureTest] every produced fluid is consumed: " + produced);
        }
    }
}
