package com.raishxn.gtna.gametest;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.raishxn.gtna.GTNACORE;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Report, not a gate: lists every {@code gtna} item and fluid that no loaded recipe produces, so missing crafts
 * show up after each run in {@code gtna-recipe-coverage.txt} of the game test server directory.
 */
@PrefixGameTestTemplate(false)
@GameTestHolder("gtna")
public final class GTNARecipeCoverageGameTests {

    private GTNARecipeCoverageGameTests() {}

    @GameTest(template = "empty_12", timeoutTicks = 200)
    public static void writeRecipeCoverageReport(GameTestHelper helper) {
        var server = helper.getLevel().getServer();
        Set<Item> items = new HashSet<>();
        Set<Fluid> fluids = new HashSet<>();
        for (var recipe : server.getRecipeManager().getRecipes()) {
            if (recipe instanceof GTRecipe gt) {
                for (var content : gt.getOutputContents(ItemRecipeCapability.CAP)) {
                    for (var stack : ItemRecipeCapability.CAP.of(content.content).getItems())
                        items.add(stack.getItem());
                }
                for (var content : gt.getOutputContents(FluidRecipeCapability.CAP)) {
                    for (var stack : FluidRecipeCapability.CAP.of(content.content).getStacks())
                        fluids.add(stack.getFluid());
                }
            } else {
                try {
                    var result = recipe.getResultItem(server.registryAccess());
                    if (!result.isEmpty()) items.add(result.getItem());
                } catch (RuntimeException ignored) {
                    // Some special recipes compute their output only from a real container.
                }
            }
        }
        Set<String> machinesAndBlocks = new TreeSet<>();
        Set<String> plainItems = new TreeSet<>();
        Set<String> materialItems = new TreeSet<>();
        for (Item item : BuiltInRegistries.ITEM) {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
            if (!GTNACORE.MOD_ID.equals(id.getNamespace()) || items.contains(item)) continue;
            if (!ChemicalHelper.getMaterialEntry(item).isEmpty()) materialItems.add(id.getPath());
            else if (item instanceof net.minecraft.world.item.BlockItem) machinesAndBlocks.add(id.getPath());
            else plainItems.add(id.getPath());
        }
        Set<String> missingFluids = new TreeSet<>();
        for (Fluid fluid : BuiltInRegistries.FLUID) {
            ResourceLocation id = BuiltInRegistries.FLUID.getKey(fluid);
            if (GTNACORE.MOD_ID.equals(id.getNamespace()) && !fluids.contains(fluid) &&
                    fluid.isSource(fluid.defaultFluidState()))
                missingFluids.add(id.getPath());
        }
        List<String> lines = new ArrayList<>();
        section(lines, "Machines and blocks", machinesAndBlocks);
        section(lines, "Items", plainItems);
        section(lines, "Material items", materialItems);
        section(lines, "Fluids", missingFluids);
        reachability(server, lines);
        try {
            Files.write(server.getServerDirectory().toPath().resolve("gtna-recipe-coverage.txt"), lines);
        } catch (IOException e) {
            helper.fail("could not write the recipe coverage report: " + e);
            return;
        }
        GTNACORE.LOGGER.info("Recipe coverage: {} blocks, {} items, {} material items, {} fluids without a recipe",
                machinesAndBlocks.size(), plainItems.size(), materialItems.size(), missingFluids.size());
        helper.succeed();
    }

    private static boolean isGtna(ResourceLocation id) {
        return GTNACORE.MOD_ID.equals(id.getNamespace());
    }

    /**
     * Fixed point over every loaded recipe: anything outside the gtna namespace counts as obtainable, a gtna item or
     * fluid becomes obtainable once a recipe whose every input is obtainable produces it. Lists what never becomes
     * obtainable and, for each blocked recipe, the first missing input — gtna content made only by machine logic
     * (Forge of Gods, Eye of Harmony...) shows up here and is checked by hand.
     */
    private static void reachability(net.minecraft.server.MinecraftServer server, List<String> lines) {
        record Node(String id, List<List<String>> inputs, List<String> outputs) {}
        List<Node> nodes = new ArrayList<>();
        for (var recipe : server.getRecipeManager().getRecipes()) {
            List<List<String>> inputs = new ArrayList<>();
            List<String> outputs = new ArrayList<>();
            if (recipe instanceof GTRecipe gt) {
                for (var content : gt.getInputContents(ItemRecipeCapability.CAP)) {
                    List<String> options = new ArrayList<>();
                    for (var stack : ItemRecipeCapability.CAP.of(content.content).getItems())
                        if (!stack.isEmpty()) options.add("item:" + BuiltInRegistries.ITEM.getKey(stack.getItem()));
                    inputs.add(options);
                }
                for (var content : gt.getInputContents(FluidRecipeCapability.CAP)) {
                    List<String> options = new ArrayList<>();
                    for (var stack : FluidRecipeCapability.CAP.of(content.content).getStacks())
                        if (!stack.isEmpty())
                            options.add("fluid:" + BuiltInRegistries.FLUID.getKey(stack.getFluid()));
                    inputs.add(options);
                }
                for (var content : gt.getOutputContents(ItemRecipeCapability.CAP))
                    for (var stack : ItemRecipeCapability.CAP.of(content.content).getItems())
                        outputs.add("item:" + BuiltInRegistries.ITEM.getKey(stack.getItem()));
                for (var content : gt.getOutputContents(FluidRecipeCapability.CAP))
                    for (var stack : FluidRecipeCapability.CAP.of(content.content).getStacks())
                        outputs.add("fluid:" + BuiltInRegistries.FLUID.getKey(stack.getFluid()));
            } else {
                try {
                    var result = recipe.getResultItem(server.registryAccess());
                    if (result.isEmpty()) continue;
                    outputs.add("item:" + BuiltInRegistries.ITEM.getKey(result.getItem()));
                    for (var ingredient : recipe.getIngredients()) {
                        if (ingredient.isEmpty()) continue;
                        List<String> options = new ArrayList<>();
                        for (var stack : ingredient.getItems())
                            options.add("item:" + BuiltInRegistries.ITEM.getKey(stack.getItem()));
                        inputs.add(options);
                    }
                } catch (RuntimeException ignored) {
                    continue;
                }
            }
            nodes.add(new Node(recipe.getId().toString(), inputs, outputs));
        }
        Set<String> have = new HashSet<>();
        java.util.function.Predicate<String> free = key -> !key.contains(":" + GTNACORE.MOD_ID + ":");
        boolean changed = true;
        List<Node> pending = new ArrayList<>(nodes);
        while (changed) {
            changed = false;
            for (var it = pending.iterator(); it.hasNext();) {
                Node node = it.next();
                boolean ok = true;
                for (var options : node.inputs()) {
                    if (options.isEmpty()) continue;
                    if (options.stream().noneMatch(o -> free.test(o) || have.contains(o))) {
                        ok = false;
                        break;
                    }
                }
                if (!ok) continue;
                it.remove();
                for (String out : node.outputs()) changed |= have.add(out);
                changed = true;
            }
        }
        Set<String> unreachable = new TreeSet<>();
        for (Item item : BuiltInRegistries.ITEM) {
            var id = BuiltInRegistries.ITEM.getKey(item);
            if (isGtna(id) && !have.contains("item:" + id)) unreachable.add("item:" + id);
        }
        for (Fluid fluid : BuiltInRegistries.FLUID) {
            var id = BuiltInRegistries.FLUID.getKey(fluid);
            if (isGtna(id) && fluid.isSource(fluid.defaultFluidState()) && !have.contains("fluid:" + id))
                unreachable.add("fluid:" + id);
        }
        Set<String> blocked = new TreeSet<>();
        for (Node node : pending) {
            if (node.outputs().stream().noneMatch(o -> !free.test(o)) && !node.id().startsWith(GTNACORE.MOD_ID))
                continue;
            for (var options : node.inputs()) {
                if (!options.isEmpty() && options.stream().noneMatch(o -> free.test(o) || have.contains(o))) {
                    blocked.add(node.id() + "  <-  " + options.get(0));
                    break;
                }
            }
        }
        var graph = new com.google.gson.JsonArray();
        for (Node node : pending) {
            var json = new com.google.gson.JsonObject();
            json.addProperty("id", node.id());
            var ins = new com.google.gson.JsonArray();
            for (var options : node.inputs()) {
                var alt = new com.google.gson.JsonArray();
                options.stream().filter(o -> !free.test(o) && !have.contains(o)).forEach(alt::add);
                if (options.stream().noneMatch(o -> free.test(o) || have.contains(o)) && !options.isEmpty())
                    ins.add(alt);
            }
            json.add("missing", ins);
            var outs = new com.google.gson.JsonArray();
            node.outputs().stream().filter(o -> !free.test(o)).forEach(outs::add);
            json.add("outputs", outs);
            graph.add(json);
        }
        try {
            Files.writeString(server.getServerDirectory().toPath().resolve("gtna-blocked-graph.json"),
                    graph.toString());
        } catch (IOException ignored) {}
        Set<String> usedTypes = new TreeSet<>();
        for (var recipe : server.getRecipeManager().getRecipes())
            if (recipe instanceof GTRecipe gt) usedTypes.add(gt.recipeType.registryName.toString());
        Set<String> served = new HashSet<>();
        for (var machine : com.gregtechceu.gtceu.api.registry.GTRegistries.MACHINES)
            for (var type : machine.getRecipeTypes()) if (type != null) served.add(type.registryName.toString());
        Set<String> orphanTypes = new TreeSet<>();
        for (String type : usedTypes) if (!served.contains(type)) orphanTypes.add(type);
        section(lines, "Recipe types with recipes but no machine", orphanTypes);
        Set<String> dry = new TreeSet<>();
        long uhv = com.gregtechceu.gtceu.api.GTValues.VA[com.gregtechceu.gtceu.api.GTValues.UHV];
        for (var recipe : server.getRecipeManager().getRecipes()) {
            if (!(recipe instanceof GTRecipe gt) || !GTNACORE.MOD_ID.equals(gt.id.getNamespace())) continue;
            long eut = gt.getInputEUt().getTotalEU();
            if (eut >= uhv && gt.getInputContents(FluidRecipeCapability.CAP).isEmpty())
                dry.add(gt.recipeType.registryName.getPath() + "  " + gt.id.getPath());
        }
        section(lines, "Endgame (>= UHV) gtna recipes without any fluid input", dry);
        // Non-gtna items used by recipes but produced by no recipe at all (e.g. GTCEu UEV+ components).
        Set<String> producedAny = new HashSet<>();
        for (Node node : nodes) producedAny.addAll(node.outputs());
        Set<String> foreignDeadEnds = new TreeSet<>();
        for (Node node : nodes) {
            for (var options : node.inputs()) {
                if (options.isEmpty() || options.stream().anyMatch(producedAny::contains)) continue;
                String first = options.get(0);
                if (first.contains(":gtceu:") || first.contains(":gtmthings:")) foreignDeadEnds.add(first);
            }
        }
        section(lines, "GTCEu inputs no recipe produces", foreignDeadEnds);
        section(lines, "Unreachable gtna content (no runnable recipe chain)", unreachable);
        section(lines, "Blocked recipes (first missing input)", blocked);
    }

    private static void section(List<String> lines, String title, Set<String> ids) {
        lines.add("## " + title + " (" + ids.size() + ")");
        lines.addAll(ids);
        lines.add("");
    }
}
