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
 * QA contract (B3): every multiblock registered in {@code GTNAMachines3} must ship the automatic
 * {@code gtna.machine.<id>.tooltip} description plus at least three numbered
 * {@code gtna.machine.<id>.tooltip.N} lines in {@code GTNALangProvider}. A missing line renders as
 * the raw key on the client, and the dedicated GameTest server never renders tooltips, so this
 * source scan is the only automated guard.
 *
 * <p>
 * The same contract is asserted at runtime for a fixed subset by
 * {@code GTNAMachineGameTests.newGtoControllersDescribeTheirFunction}; this test makes it universal
 * for the GTOCore port class. GTLCore style: {@code main()} + asserts.
 */
public final class MachineTooltipContractTest {

    private static final Path MACHINES = Path.of("src/main/java/com/raishxn/gtna/common/data/GTNAMachines3.java");
    private static final Path LANG_PROVIDER = Path.of("src/main/java/com/raishxn/gtna/data/GTNALangProvider.java");
    private static final Pattern MULTIBLOCK = Pattern.compile("\\.multiblock\\(\"([a-z0-9_]+)\"");
    private static final int MIN_FUNCTIONAL_LINES = 3;

    private MachineTooltipContractTest() {}

    public static void main(String[] args) throws IOException {
        String machinesSource = Files.readString(MACHINES, StandardCharsets.UTF_8);
        String langSource = Files.readString(LANG_PROVIDER, StandardCharsets.UTF_8);

        List<String> machineIds = new ArrayList<>();
        Matcher matcher = MULTIBLOCK.matcher(machinesSource);
        while (matcher.find()) {
            machineIds.add(matcher.group(1));
        }

        List<String> problems = new ArrayList<>();
        for (String id : machineIds) {
            String prefix = "gtna.machine." + id + ".tooltip";
            if (!langSource.contains("\"" + prefix + "\"")) {
                problems.add(prefix);
            }
            int functional = 0;
            for (int i = 0; i < 32; i++) {
                if (langSource.contains("\"" + prefix + "." + i + "\"")) {
                    functional++;
                }
            }
            if (functional < MIN_FUNCTIONAL_LINES) {
                problems.add(prefix + ".N (found " + functional + ", need " + MIN_FUNCTIONAL_LINES + ")");
            }
        }
        if (machineIds.isEmpty()) {
            throw new AssertionError("no machines parsed from GTNAMachines3; the regex or the layout changed");
        }
        if (!problems.isEmpty()) {
            throw new AssertionError("GTNAMachines3 machines with incomplete tooltips: " + problems +
                    " - add the keys to GTNALangProvider");
        }
        System.out.println("[MachineTooltipContractTest] " + machineIds.size() +
                " GTNAMachines3 controllers have a description and >= " + MIN_FUNCTIONAL_LINES + " tooltip lines");
    }
}
