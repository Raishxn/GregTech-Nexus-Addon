package com.raishxn.gtna;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Guards the Steam Elevator module tooltips: every {@code moduleLines(id, from, to)} call in
 * {@code GTNAMachines2} must have a generated {@code gtna.machine.<id>.tooltip.<n>} entry for
 * {@code n in [from, to]}, and every registered module must have its auto-inserted
 * {@code gtna.machine.<id>.tooltip} description.
 *
 * <p>
 * Why this exists: the module tooltips are assembled from many numbered lang keys (the exact GTNL
 * lines), and a missing or off-by-one key silently renders as the raw key in game. The dedicated
 * server gate never renders a tooltip, so this source+lang scan is the only automated guard.
 * GTLCore style: {@code main()} + asserts.
 */
public final class ModuleTooltipContractTest {

    private static final Path MACHINES_SOURCE = Path
            .of("src/main/java/com/raishxn/gtna/common/data/GTNAMachines2.java");
    private static final Path GENERATED_LANG = Path
            .of("src/generated/resources/assets/gtna/lang/en_us.json");

    private static final Pattern MODULE_LINES = Pattern
            .compile("moduleLines\\(\"([a-z0-9_]+)\",\\s*(\\d+),\\s*(\\d+)\\)");
    private static final Pattern REGISTER_MODULE = Pattern
            .compile("registerElevatorModule\\(\"([a-z0-9_]+)\"");

    private ModuleTooltipContractTest() {}

    public static void main(String[] args) throws IOException {
        String source = Files.readString(MACHINES_SOURCE, StandardCharsets.UTF_8);
        String lang = Files.readString(GENERATED_LANG, StandardCharsets.UTF_8);

        List<String> missing = new ArrayList<>();
        int checkedLines = countModuleLines(source, lang, missing);
        int checkedModules = countRegisteredModules(source, lang, missing);

        if (checkedModules == 0) {
            throw new AssertionError("no elevator modules parsed from " + MACHINES_SOURCE +
                    "; the regex or the file layout changed");
        }
        if (!missing.isEmpty()) {
            throw new AssertionError("elevator module tooltip lang keys missing from the generated en_us (" +
                    missing.size() + "): " + missing + " - add them to GTNALangProvider and run ./gradlew runData");
        }
        System.out.println("[ModuleTooltipContractTest] " + checkedModules + " modules and " + checkedLines +
                " moduleLines entries are fully translated");
    }

    private static int countModuleLines(String source, String lang, List<String> missing) {
        int checked = 0;
        Matcher matcher = MODULE_LINES.matcher(source);
        while (matcher.find()) {
            String id = matcher.group(1);
            int from = Integer.parseInt(matcher.group(2));
            int to = Integer.parseInt(matcher.group(3));
            checked++;
            for (int i = from; i <= to; i++) {
                String key = "gtna.machine." + id + ".tooltip." + i;
                if (!lang.contains('"' + key + '"')) {
                    missing.add(key);
                }
            }
        }
        return checked;
    }

    private static int countRegisteredModules(String source, String lang, List<String> missing) {
        int checked = 0;
        Matcher matcher = REGISTER_MODULE.matcher(source);
        while (matcher.find()) {
            checked++;
            String key = "gtna.machine." + matcher.group(1) + ".tooltip";
            if (!lang.contains('"' + key + '"')) {
                missing.add(key);
            }
        }
        return checked;
    }
}
