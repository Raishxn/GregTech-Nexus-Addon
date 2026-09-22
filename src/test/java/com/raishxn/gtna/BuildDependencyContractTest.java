package com.raishxn.gtna;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Guards against the same mod being declared twice through different coordinates.
 *
 * <p>
 * Why this exists: {@code build.gradle} declared {@code dev.toma.configuration:configuration-forge-1.20.1:2.2.0}
 * <b>and</b> {@code curse.maven:configuration-444699:5840405}. Both are the "configuration" mod, FML
 * loaded the newer one at runtime, and code compiled against the older API blew up with a
 * {@code NoSuchMethodError} only when a player used the feature (the HUD drag crash). A plain
 * duplicate check cannot rely on the raw string because the intentional {@code modCompileOnly} +
 * {@code modRuntimeOnly} pattern declares the <b>same</b> coordinate twice (e.g. Jade), so this test
 * groups the declarations by a normalized mod slug and only fails when one slug has two distinct
 * coordinates.
 *
 * <p>
 * GTLCore style: {@code main()} + asserts.
 */
public final class BuildDependencyContractTest {

    private static final Path BUILD_GRADLE = Path.of("build.gradle");

    /** {@code modImplementation("coord")} / modCompileOnly / modRuntimeOnly, single- or multi-line. */
    private static final Pattern MOD_DEPENDENCY = Pattern.compile(
            "mod(?:Implementation|CompileOnly|RuntimeOnly)\\s*\\(\\s*\"([^\"]+)\"");

    private static final Pattern TRAILING_NUMBER = Pattern.compile("-\\d+$");
    private static final Pattern MC_VERSION = Pattern.compile("-\\d+\\.\\d+(\\.\\d+)?$");

    private BuildDependencyContractTest() {}

    public static void main(String[] args) throws IOException {
        String source = Files.readString(BUILD_GRADLE, StandardCharsets.UTF_8);

        Map<String, Set<String>> bySlug = new LinkedHashMap<>();
        Matcher matcher = MOD_DEPENDENCY.matcher(source);
        int declarations = 0;
        while (matcher.find()) {
            declarations++;
            String coordinate = matcher.group(1);
            bySlug.computeIfAbsent(slug(coordinate), ignored -> new LinkedHashSet<>()).add(coordinate);
        }

        List<String> conflicts = new ArrayList<>();
        bySlug.forEach((slug, coordinates) -> {
            if (coordinates.size() > 1) {
                conflicts.add(slug + " -> " + coordinates);
            }
        });

        if (declarations == 0) {
            throw new AssertionError("no mod dependency declarations parsed from " + BUILD_GRADLE +
                    "; the regex or the file layout changed");
        }
        if (!conflicts.isEmpty()) {
            throw new AssertionError("the same mod is declared through different coordinates (a version " +
                    "mismatch like configuration 2.2.0/3.1.0 compiles against one API and loads another): " +
                    conflicts);
        }
        System.out.println("[BuildDependencyContractTest] " + declarations + " mod dependencies, " +
                bySlug.size() + " distinct mods, no conflicting coordinates");
    }

    /**
     * Normalizes a dependency coordinate to its mod slug: {@code curse.maven:configuration-444699}
     * and {@code dev.toma.configuration:configuration-forge-1.20.1} both become {@code configuration}.
     */
    private static String slug(String coordinate) {
        String base;
        if (coordinate.startsWith("curse.maven:")) {
            base = coordinate.substring("curse.maven:".length());
            // Drop the trailing CurseForge project id.
            base = TRAILING_NUMBER.matcher(base).replaceFirst("");
        } else {
            String[] parts = coordinate.split(":");
            base = parts.length >= 2 ? parts[1] : parts[0];
            // Strip the Minecraft version and the loader suffix (configuration-forge-1.20.1 -> configuration).
            base = MC_VERSION.matcher(base).replaceFirst("");
            base = base.replaceAll("-(forge|fabric|neoforge)$", "");
        }
        base = base.toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
        return base;
    }
}
