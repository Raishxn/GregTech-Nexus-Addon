package com.raishxn.gtna;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * QA contract (B5): every multiblock registered in {@code GTNAMachines3} must either have its
 * controller recipe in the GTNA recipe sources or be listed here as deliberately omitted with a
 * reason. The author's rule is to port the original controller recipe faithfully whenever it only
 * uses GTCEu/GTNA resources and to omit it (documented) when the original depends on GTO-exclusive
 * resources.
 *
 * <p>
 * GTCEu registers addon recipes through a dynamic data pack instead of writing recipe JSON under
 * {@code src/generated/resources}, so the recipes are scanned in {@code data/recipe}. A machine
 * counts as covered when its registry constant appears next to {@code asStack()} in a recipe
 * source. GTLCore style: {@code main()} + asserts.
 */
public final class ControllerRecipePolicyTest {

    private static final Path MACHINES = Path.of("src/main/java/com/raishxn/gtna/common/data/GTNAMachines3.java");
    private static final Path RECIPE_DIR = Path.of("src/main/java/com/raishxn/gtna/data/recipe");
    private static final Pattern MULTIBLOCK = Pattern
            .compile(
                    "public static final MultiblockMachineDefinition ([A-Z0-9_]+) = REGISTRATE\\s*\\.multiblock\\(\"([a-z0-9_]+)\"");

    /** Registry path → documented reason for omitting the controller recipe. */
    private static final Map<String, String> OMITTED = Map.of(
            "chemical_plant",
            "GTO's original is an Assembly Line recipe using GTO-only WatertightSteel and other resources",
            "component_assembly_line",
            "GTO's original Assembly Line recipe uses the GTO-only Advanced Assembly Line, Advanced Assembly Line " +
                    "Unit and Mithril");

    private ControllerRecipePolicyTest() {}

    public static void main(String[] args) throws IOException {
        String machinesSource = Files.readString(MACHINES, StandardCharsets.UTF_8);
        Map<String, String> machines = new LinkedHashMap<>();
        Matcher matcher = MULTIBLOCK.matcher(machinesSource);
        while (matcher.find()) {
            machines.put(matcher.group(2), matcher.group(1));
        }
        if (machines.isEmpty()) {
            throw new AssertionError("no machines parsed from GTNAMachines3; the regex or the layout changed");
        }

        StringBuilder recipes = new StringBuilder();
        List<Path> files;
        try (Stream<Path> walk = Files.walk(RECIPE_DIR)) {
            files = walk.filter(path -> path.toString().endsWith(".java")).sorted().toList();
        }
        for (Path file : files) {
            recipes.append(Files.readString(file, StandardCharsets.UTF_8));
        }
        String recipeSource = recipes.toString();

        List<String> problems = new ArrayList<>();
        for (Map.Entry<String, String> machine : machines.entrySet()) {
            String id = machine.getKey();
            String constant = machine.getValue();
            if (OMITTED.containsKey(id)) {
                continue;
            }
            if (!recipeSource.contains("GTNAMachines3." + constant + ".asStack()")) {
                problems.add(id + " (" + constant + ")");
            }
        }
        if (!problems.isEmpty()) {
            throw new AssertionError("GTNAMachines3 controllers without a controller recipe and without a " +
                    "documented omission: " + problems + " - add the recipe or list the machine in OMITTED");
        }
        System.out.println("[ControllerRecipePolicyTest] " + machines.size() + " controllers have a recipe or a " +
                "documented omission (" + OMITTED.size() + " omitted)");
    }
}
