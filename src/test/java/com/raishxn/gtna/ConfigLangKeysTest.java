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
 * Guards the lang keys that are <b>asserted or read at runtime</b> but that neither of the other
 * gates can reach.
 *
 * <p>
 * Why this exists: running the dev client crashed with
 * {@code Missing config translation: config.jade.plugin_gtna.me_pattern_buffer} because Jade builds
 * one config entry per registered data provider and asserts its translation exists. That is
 * client-only, so {@code runUnitTests} cannot see it and the dedicated-server gametest cannot
 * either. The same crash class also hides config options that simply render as raw keys.
 *
 * <p>
 * Deliberately dependency-free (regex over the source plus substring checks on the generated lang):
 * the test source set does not inherit Minecraft's libraries. GTLCore-style: {@code main()} +
 * asserts. Run {@code ./gradlew runData} after touching config or lang.
 */
public final class ConfigLangKeysTest {

    private static final Path CONFIG_SOURCE = Path.of("src/main/java/com/raishxn/gtna/config/ConfigHolder.java");
    private static final Path GENERATED_LANG = Path.of("src/generated/resources/assets/gtna/lang/en_us.json");

    /** Jade derives the key from the provider UID as {@code config.jade.plugin_<ns>.<path>}. */
    private static final List<String> JADE_PROVIDER_KEYS = List.of(
            "config.jade.plugin_gtna.multiple_recipes_provider",
            "config.jade.plugin_gtna.me_pattern_buffer",
            "config.jade.plugin_gtna.solar_boiler_provider",
            "config.jade.plugin_gtna.wireless_steam_network");

    private static final Pattern CONFIG_FIELD = Pattern
            .compile("^\\s*public\\s+[\\w<>\\[\\]., ]+?\\s+(\\w+)\\s*(?:=|;).*$");

    private ConfigLangKeysTest() {}

    public static void main(String[] args) throws IOException {
        String lang = Files.readString(GENERATED_LANG, StandardCharsets.UTF_8);
        checkConfigOptions(lang);
        checkJadeProviderKeys(lang);
        System.out.println("[ConfigLangKeysTest] all cases passed");
    }

    /** Every {@code @Configurable} field needs {@code config.gtna.option.<field>} in en_us. */
    private static void checkConfigOptions(String lang) throws IOException {
        List<String> source = Files.readAllLines(CONFIG_SOURCE, StandardCharsets.UTF_8);
        List<String> missing = new ArrayList<>();
        int checked = 0;
        for (int i = 0; i < source.size(); i++) {
            Matcher matcher = CONFIG_FIELD.matcher(source.get(i));
            if (!matcher.matches() || !isConfigurable(source, i)) {
                continue;
            }
            checked++;
            String key = "config.gtna.option." + matcher.group(1);
            if (!lang.contains('"' + key + '"')) {
                missing.add(key);
            }
        }
        if (checked == 0) {
            throw new AssertionError("no @Configurable fields parsed from " + CONFIG_SOURCE +
                    "; the regex or the file layout changed");
        }
        if (!missing.isEmpty()) {
            throw new AssertionError("config options with no en_us translation (" + missing.size() + "): " +
                    missing + " - add them to the manual en_us.json and run ./gradlew runData");
        }
    }

    /** A field counts as configurable when the annotation sits on one of the four lines above it. */
    private static boolean isConfigurable(List<String> source, int fieldLine) {
        for (int j = Math.max(0, fieldLine - 4); j < fieldLine; j++) {
            if (source.get(j).contains("@Configurable")) {
                return true;
            }
        }
        return false;
    }

    /** Jade asserts these on the client; a missing one crashes the dev client at startup. */
    private static void checkJadeProviderKeys(String lang) {
        List<String> missing = JADE_PROVIDER_KEYS.stream()
                .filter(key -> !lang.contains('"' + key + '"'))
                .toList();
        if (!missing.isEmpty()) {
            throw new AssertionError("Jade provider keys missing from en_us (dev client crash): " + missing +
                    " - keep them in sync with GTNAJadePlugin and run ./gradlew runData");
        }
    }
}
