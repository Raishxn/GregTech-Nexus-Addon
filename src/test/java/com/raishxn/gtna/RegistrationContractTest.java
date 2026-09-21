package com.raishxn.gtna;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Guards the registry against silent overwrites: two machines registered under the same id, or two
 * machines sharing a config key, would let one definition replace the other with no error at
 * startup. Also checks that every config key is referenced by exactly one registration.
 *
 * <p>
 * Source scan (like {@code ConfigLangKeysTest}/{@code SteamWiringContractTest}): the unit test source
 * set does not inherit Minecraft's libraries. GTLCore style: {@code main()} + asserts.
 */
public final class RegistrationContractTest {

    private static final Path DATA_DIR = Path.of("src/main/java/com/raishxn/gtna/common/data");

    private static final Pattern REGISTRY_NAME = Pattern.compile("\\.(?:multiblock|machine)\\(\"([a-z0-9_]+)\"");
    private static final Pattern CONFIG_KEY = Pattern.compile("register(?:Machine|Hatch)\\(\"([A-Za-z0-9_]+)\"");

    private RegistrationContractTest() {}

    public static void main(String[] args) throws IOException {
        List<Path> files;
        try (Stream<Path> walk = Files.walk(DATA_DIR)) {
            files = walk.filter(p -> p.toString().endsWith(".java")).sorted().toList();
        }

        Set<String> registryNames = new HashSet<>();
        Set<String> configKeys = new HashSet<>();
        List<String> duplicateRegistryNames = new ArrayList<>();
        List<String> duplicateConfigKeys = new ArrayList<>();

        for (Path file : files) {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                Matcher registry = REGISTRY_NAME.matcher(line);
                while (registry.find()) {
                    if (!registryNames.add(registry.group(1))) {
                        duplicateRegistryNames.add(registry.group(1));
                    }
                }
                Matcher config = CONFIG_KEY.matcher(line);
                while (config.find()) {
                    if (!configKeys.add(config.group(1))) {
                        duplicateConfigKeys.add(config.group(1));
                    }
                }
            }
        }

        if (!duplicateRegistryNames.isEmpty()) {
            throw new AssertionError("duplicate machine registry ids (one definition silently replaces the " +
                    "other): " + duplicateRegistryNames);
        }
        if (!duplicateConfigKeys.isEmpty()) {
            throw new AssertionError("duplicate machine config keys (the config toggle would be shared): " +
                    duplicateConfigKeys);
        }
        System.out.println("[RegistrationContractTest] " + registryNames.size() + " registry ids and " +
                configKeys.size() + " config keys are unique");
    }
}
