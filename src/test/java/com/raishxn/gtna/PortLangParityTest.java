package com.raishxn.gtna;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * QA contract (B1): every {@code block.gtna.<id>} and {@code gtna.machine.<id>.tooltip*} key that the
 * English language provider exposes must also exist in the manual {@code pt_br.json}. A missing key
 * renders as the raw translation key in the Portuguese client, and the dedicated GameTest server
 * never renders a tooltip, so this source+resource scan is the only automated guard.
 *
 * <p>
 * Sources scanned: the literal {@code add("...")} calls in {@code GTNALangProvider} (machine names,
 * tooltip lines, block names) and the manual {@code src/main/resources/assets/gtna/lang/en_us.json}
 * that the provider merges through {@code addManualTranslations}. Deliberately dependency-free
 * (regex + substring checks): the unit test source set does not inherit Minecraft's libraries.
 * GTLCore style: {@code main()} + asserts. Run {@code ./gradlew runData} after touching lang.
 */
public final class PortLangParityTest {

    private static final Path LANG_PROVIDER = Path.of("src/main/java/com/raishxn/gtna/data/GTNALangProvider.java");
    private static final Path MANUAL_EN_US = Path
            .of("src/main/resources/assets/gtna/lang/en_us.json");
    private static final Path MANUAL_PT_BR = Path
            .of("src/main/resources/assets/gtna/lang/pt_br.json");

    /** Literal provider keys only: a string concatenated into a key is resolved at runtime. */
    private static final Pattern PROVIDER_KEY = Pattern
            .compile("add\\(\\s*\"((?:block\\.gtna\\.|gtna\\.machine\\.)[^\"]*)\"\\s*,");
    /** JSON object keys in the manual en_us file. */
    private static final Pattern JSON_KEY = Pattern
            .compile("\"((?:block\\.gtna\\.|gtna\\.machine\\.)[^\"]*)\"\\s*:");
    private static final Pattern TOOLTIP_KEY = Pattern.compile("^gtna\\.machine\\.[a-z0-9_]+\\.tooltip");

    private PortLangParityTest() {}

    public static void main(String[] args) throws IOException {
        Set<String> english = new LinkedHashSet<>();
        collect(PROVIDER_KEY, Files.readString(LANG_PROVIDER, StandardCharsets.UTF_8), english);
        collect(JSON_KEY, Files.readString(MANUAL_EN_US, StandardCharsets.UTF_8), english);

        String portuguese = Files.readString(MANUAL_PT_BR, StandardCharsets.UTF_8);
        List<String> missing = english.stream()
                .filter(key -> key.startsWith("block.gtna.") || TOOLTIP_KEY.matcher(key).find())
                .filter(key -> !portuguese.contains('"' + key + '"'))
                .sorted()
                .toList();

        if (!missing.isEmpty()) {
            throw new AssertionError("pt_br.json is missing " + missing.size() + " block/tooltip key(s) exposed by " +
                    "GTNALangProvider: " + missing + " - add them to src/main/resources/assets/gtna/lang/pt_br.json");
        }
        if (english.size() < 100) {
            throw new AssertionError("expected at least 100 block/tooltip keys, found " + english.size() +
                    "; the scan patterns or the provider layout changed");
        }
        System.out.println("[PortLangParityTest] all block/tooltip keys have pt_br translations");
    }

    private static void collect(Pattern pattern, String source, Set<String> out) {
        Matcher matcher = pattern.matcher(source);
        while (matcher.find()) {
            out.add(matcher.group(1));
        }
    }
}
