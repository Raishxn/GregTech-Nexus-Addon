package com.raishxn.gtna.gametest;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.raishxn.gtna.GTNACORE;

import java.util.ArrayList;
import java.util.List;

@PrefixGameTestTemplate(false)
@GameTestHolder("gtna")
public final class GTNAGodforgeChainGameTests {

    private static final String[] STAGE_2 = { "calcium_plasma", "sulfur_plasma", "zinc_plasma", "niobium_plasma",
            "silver_plasma", "bismuth_plasma", "radon_plasma", "titanium_plasma", "boron_plasma", "lead_plasma",
            "thorium_plasma", "dtcc", "dtpc", "dtrc", "dtec", "dtsc", "excited_dtcc", "excited_dtpc", "excited_dtrc",
            "excited_dtec", "excited_dtsc" };

    private GTNAGodforgeChainGameTests() {}

    /** Every stage-2 recipe loads and no fluid on either side resolved to empty (missing form or plasma). */
    @GameTest(template = "empty_12", timeoutTicks = 40)
    public static void stageTwoCatalystChainLoads(GameTestHelper helper) {
        var manager = helper.getLevel().getRecipeManager();
        List<String> problems = new ArrayList<>();
        for (String id : STAGE_2) {
            var found = manager.getRecipes().stream()
                    .filter(r -> r instanceof GTRecipe && r.getId().getNamespace().equals(GTNACORE.MOD_ID) &&
                            r.getId().getPath().endsWith("godforge_chain/" + id))
                    .map(r -> (GTRecipe) r).findFirst();
            if (found.isEmpty()) {
                problems.add(id + " missing");
                continue;
            }
            var recipe = found.get();
            var contents = new ArrayList<>(recipe.getInputContents(FluidRecipeCapability.CAP));
            contents.addAll(recipe.getOutputContents(FluidRecipeCapability.CAP));
            for (var content : contents) {
                var stacks = FluidRecipeCapability.CAP.of(content.content).getStacks();
                if (stacks.length == 0 || stacks[0].isEmpty()) problems.add(id + " has an empty fluid");
            }
        }
        helper.assertTrue(problems.isEmpty(), String.join("; ", problems));
        helper.succeed();
    }
}
