package com.raishxn.gtna;

import com.raishxn.gtna.planner.api.amount.UfoAmount;
import com.raishxn.gtna.planner.api.crafting.planner.CraftingPattern;
import com.raishxn.gtna.planner.api.crafting.planner.ImmutableCraftingGraph;
import com.raishxn.gtna.planner.api.crafting.planner.IterativeCraftingPlanner;
import com.raishxn.gtna.planner.api.crafting.planner.PlanningRequest;
import com.raishxn.gtna.planner.api.crafting.planner.PlanningResult;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/** Exercises the ported planner on batching, shortages and route selection. */
public final class NexusPlannerTest {

    private NexusPlannerTest() {}

    public static void main(String[] args) {
        var plate = new CraftingPattern<>("plate", Map.of("ingot", UfoAmount.of(3)),
                Map.of("plate", UfoAmount.of(2), "dust", UfoAmount.of(1)));
        var graph = ImmutableCraftingGraph.create(7, Comparator.<String>naturalOrder(), List.of(plate));
        var result = new IterativeCraftingPlanner<String>().plan(graph,
                new PlanningRequest<>("plate", UfoAmount.of(5), Map.of("ingot", UfoAmount.of(9))));
        require(result.status() == PlanningResult.Status.COMPLETE, "batched plan must complete");
        require(UfoAmount.of(3).equals(result.plan().patternExecutions().get(plate)), "three batches");
        require(UfoAmount.of(3).equals(result.plan().remaining().get("dust")), "byproduct retained");

        var missing = new IterativeCraftingPlanner<String>().plan(graph,
                new PlanningRequest<>("plate", UfoAmount.of(5), Map.of()));
        require(missing.status() == PlanningResult.Status.MISSING_INGREDIENTS, "missing stock reported");
        require(UfoAmount.of(9).equals(missing.plan().missing().get("ingot")), "nine ingots missing");

        var expensive = new CraftingPattern<>("a-expensive", Map.of("absent", UfoAmount.ONE),
                Map.of("target", UfoAmount.ONE));
        var available = new CraftingPattern<>("z-available", Map.of("raw", UfoAmount.ONE),
                Map.of("target", UfoAmount.ONE));
        var alternate = ImmutableCraftingGraph.create(8, Comparator.<String>naturalOrder(),
                List.of(expensive, available));
        var selected = new IterativeCraftingPlanner<String>().plan(alternate,
                new PlanningRequest<>("target", UfoAmount.ONE, Map.of("raw", UfoAmount.ONE)));
        require(selected.status() == PlanningResult.Status.COMPLETE, "available route selected");
        require(selected.plan().patternExecutions().containsKey(available), "correct route selected");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
