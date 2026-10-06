package com.raishxn.gtna.gametest;

import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.lookup.RecipeAdditionHandler;
import com.gregtechceu.gtceu.api.recipe.lookup.RecipeDB;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.raishxn.gtna.common.data.GTNARecipeType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@GameTestHolder("gtna")
@PrefixGameTestTemplate(false)
public final class GTNAVoidMinerLookupGameTests {

    @GameTest(template = "empty_12", timeoutTicks = 40)
    public static void randomRecipeViewerKeepsAllOutputsInsideScrollableViewport(GameTestHelper h) {
        var ui = GTNARecipeType.RANDOM_VOID_MINING_RECIPES.getRecipeUI();
        h.assertTrue(ui.getJEISize().height < 200, "random recipe viewer must fit a normal screen");
        var template = ui.createEditableUITemplate(false, false).createDefault();
        var scroll = template.widgets.stream()
                .filter(com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup.class::isInstance)
                .map(com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup.class::cast)
                .findFirst().orElseThrow();
        h.assertTrue(scroll.isUseScissor(), "outputs must be clipped to their viewport");
        h.assertTrue(scroll.widgets.size() == 216, "all output candidates must remain accessible");
        var last = scroll.widgets.get(215);
        int originalY = last.getSelfPosition().y;
        scroll.setScrollYOffset(originalY - 4);
        h.assertTrue(last.isVisible() && last.getSelfPosition().y == 4,
                "scrolling must bring the final output into view");
        h.succeed();
    }

    @GameTest(template = "empty_12", timeoutTicks = 40)
    public static void everyProductionVoidRecipeSurvivesLookupBaking(GameTestHelper h) {
        for (var type : List.of(GTNARecipeType.ELECTRIC_VOID_MINING_RECIPES,
                GTNARecipeType.RANDOM_VOID_MINING_RECIPES)) {
            // Bake the real recipe-manager catalogue in isolation: other machine fixtures
            // intentionally replace the global lookup with short test recipes.
            var recipes = h.getLevel().getRecipeManager().getAllRecipesFor(type);
            h.assertTrue(recipes.size() >= (type == GTNARecipeType.RANDOM_VOID_MINING_RECIPES ? 2 : 43),
                    "production void catalogue must be available: " + type);
            var db = new RecipeDB();
            var addition = new RecipeAdditionHandler(db);
            addition.beginStaging();
            recipes.forEach(addition::addStaging);
            addition.completeStaging();
            for (GTRecipe recipe : recipes) {
                Map<RecipeCapability<?>, List<Object>> inputs = new HashMap<>();
                recipe.inputs.forEach((cap, contents) -> inputs.put(cap,
                        contents.stream().map(content -> content.content).toList()));
                h.assertTrue(db.find(inputs, found -> found.id.equals(recipe.id)) != null,
                        "recipe must remain selectable after GTCEu lookup baking: " + recipe.id);
            }
        }
        h.succeed();
    }
}
