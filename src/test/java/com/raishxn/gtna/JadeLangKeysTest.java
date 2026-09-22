package com.raishxn.gtna;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Guards the Jade trap that has already crashed the dev client once (G-0007, and again for the
 * solar boiler provider): Jade asserts that every registered plugin UID has the translation key
 * {@code config.jade.plugin_<namespace>.<uid>} and throws at client start-up if it is missing. The
 * dedicated-server gate never sees it (Jade is client-side), so this source scan is the only guard.
 *
 * <p>
 * Source scan on purpose: the unit test source set does not inherit Minecraft's libraries.
 * GTLCore style: {@code main()} + asserts.
 */
public final class JadeLangKeysTest {

    private static final Path PROVIDER_DIR = Path
            .of("src/main/java/com/raishxn/gtna/integration/jade/provider");
    private static final Path LANG_SOURCE = Path
            .of("src/main/java/com/raishxn/gtna/data/GTNALangProvider.java");

    /** Matches {@code UID = GTNACORE.id("x")} and {@code UID = new ResourceLocation("gtna", "x")}. */
    private static final Pattern UID = Pattern.compile(
            "(?:GTNACORE\\.id|new ResourceLocation)\\s*\\(\\s*(?:\"gtna\"\\s*,\\s*)?\"([a-z0-9_]+)\"");

    private JadeLangKeysTest() {}

    public static void main(String[] args) throws IOException {
        String lang = Files.readString(LANG_SOURCE);
        List<String> missing = new ArrayList<>();
        int checked = 0;
        try (Stream<Path> files = Files.list(PROVIDER_DIR)) {
            for (Path path : files.filter(p -> p.toString().endsWith(".java")).toList()) {
                Matcher matcher = UID.matcher(Files.readString(path));
                while (matcher.find()) {
                    String uid = matcher.group(1);
                    checked++;
                    String key = "config.jade.plugin_gtna." + uid;
                    if (!lang.contains("\"" + key + "\"")) {
                        missing.add(path.getFileName() + " -> " + key);
                    }
                }
            }
        }
        if (!missing.isEmpty()) {
            throw new AssertionError("Jade provider(s) without a config translation (client crash): " + missing);
        }
        if (checked < 3) {
            throw new AssertionError("expected at least 3 Jade providers, found " + checked);
        }
        System.out.println("[JadeLangKeysTest] " + checked + " Jade provider lang keys present");
    }
}
