package com.raishxn.gtna;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Guards that the output boost has a single application point.
 *
 * <p>
 * Why this exists: output boost is applied globally by the {@code RecipeHelperMixin} through
 * {@code GTNASpecialPartUtil.applyOutputBoosts} (match simulation and execution). The
 * multiple-recipes logic used to apply it a second time in {@code tryStartRecipe} via
 * {@code ModifierFunction.outputModifier(...)}, which squared the boost (M -> M^2) and made the
 * simulated match demand M^2 free space. The manual application was removed; this test keeps it
 * from coming back.
 *
 * <p>
 * Source scan (the unit test source set does not inherit Minecraft's libraries), GTLCore style:
 * {@code main()} + asserts.
 */
public final class OutputBoostContractTest {

    private static final Path LOGIC = Path
            .of("src/main/java/com/raishxn/gtna/common/machine/trait/GTNAMultipleRecipesLogic.java");
    private static final Path SPECIAL_UTIL = Path
            .of("src/main/java/com/raishxn/gtna/utils/GTNASpecialPartUtil.java");

    private OutputBoostContractTest() {}

    public static void main(String[] args) throws IOException {
        String logic = Files.readString(LOGIC, StandardCharsets.UTF_8);
        String util = Files.readString(SPECIAL_UTIL, StandardCharsets.UTF_8);

        // The boost must not be applied by the logic itself; the mixin/GTNASpecialPartUtil owns it.
        if (logic.contains(".outputModifier(")) {
            throw new AssertionError(LOGIC + " applies an output modifier itself. The output boost is " +
                    "global (RecipeHelperMixin -> GTNASpecialPartUtil.applyOutputBoosts); doing it here too " +
                    "squares the boost (M -> M^2). Remove the local application.");
        }

        // The single source of truth for the execution path must still exist.
        if (!util.contains("applyOutputBoosts")) {
            throw new AssertionError(SPECIAL_UTIL + " no longer exposes applyOutputBoosts; the global output " +
                    "boost path was renamed or removed - keep this contract in sync");
        }

        System.out.println("[OutputBoostContractTest] all cases passed");
    }
}
