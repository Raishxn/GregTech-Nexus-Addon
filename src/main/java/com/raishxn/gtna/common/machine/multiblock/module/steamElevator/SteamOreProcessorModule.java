package com.raishxn.gtna.common.machine.multiblock.module.steamElevator;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntCircuitIngredient;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;

import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;

import com.raishxn.gtna.common.data.GTNARecipeType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * GTNL {@code SteamOreProcessorModule} port (LGPLv3, original by ScienceNotLeisure).
 *
 * <p>
 * GTNL runs a configurable multi-stage ore-processing chain (macerate → wash → thermal → centrifuge,
 * chem-bath, sifter, forge-hammer …) with up to 8 parallels. GTCEu's recipe processing is
 * controller-centric, so reproducing every stage inside a part machine is out of scope; this port
 * keeps the processor's contract and the tooltip's concrete numbers:
 * <ul>
 * <li>the circuit in the module structure's <b>input bus</b> selects the mode (0–6) and its processing
 * time (600/300/200/400/340/640/20 ticks);</li>
 * <li>up to {@code 8 * 2^mode} ores per batch ("Can process up to 16 ores at a time" at circuit 1);</li>
 * <li>every ore costs 128L steam/t scaled by the circuit ("Set circuit to double both parallel and
 * EU consumption") and the <b>washing fluid of the matching integrated recipe</b> (circuit 1 needs
 * none; circuits 2/3/4 distilled water; circuits 5/6/7 the ore's own fluid, e.g. mercury), drawn from
 * the <b>input hatch</b>;</li>
 * <li>the products are the ore's macerated drops inserted into the <b>output bus</b> (the chain depth
 * is the documented simplification).</li>
 * </ul>
 */
public class SteamOreProcessorModule extends SteamElevatorModuleMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            SteamOreProcessorModule.class, SteamElevatorModuleMachine.MANAGED_FIELD_HOLDER);

    /** GTNL {@code RECIPE_EUT}. */
    public static final long STEAM_UPKEEP = 128;
    private static final int MAX_MODE = 6;
    /** GTNL {@code getRecipeTickTime(mode)}: modes 0..6. */
    private static final int[] MODE_TICKS = { 600, 300, 200, 400, 340, 640, 20 };

    private int progress;

    public SteamOreProcessorModule(IMachineBlockEntity holder, int tier) {
        super(holder, tier);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    /** The mode selected by the circuit in the input bus (0–6). */
    private int mode() {
        int circuit = findCircuit();
        return Math.max(0, Math.min(MAX_MODE, circuit));
    }

    private int cycleTicks() {
        return cycleTicksFor(mode());
    }

    private int maxParallel() {
        return maxParallelFor(mode());
    }

    @Override
    public long getSteamUpkeep() {
        // GTNL: requiredEUT = 128 * 2^circuit.
        return upkeepFor(mode());
    }

    @Override
    public int getModuleProgress() {
        return progress;
    }

    @Override
    public int getModuleMaxProgress() {
        return cycleTicks();
    }

    @Override
    protected boolean isModuleWorking() {
        if (!hasUpkeepSteam()) return false;
        for (NotifiableItemStackHandler handler : inputItemHandlers()) {
            for (int slot = 0; slot < handler.getSlots(); slot++) {
                ItemStack input = handler.getStackInSlot(slot);
                if (!isOre(input)) continue;
                FluidStack required = requiredFluidFor(input);
                if (required.isEmpty() || countFluid(required) >= required.getAmount()) {
                    return true;
                }
            }
        }
        return false;
    }

    private static int clampMode(int mode) {
        return Math.max(0, Math.min(MAX_MODE, mode));
    }

    /** GTNL {@code getRecipeTickTime(mode)}. Static so the contract can be gametested. */
    public static int cycleTicksFor(int mode) {
        return MODE_TICKS[clampMode(mode)];
    }

    /** GTNL parallel: 8 per unit of the circuit multiplier (8, 16, 32 ...). */
    public static int maxParallelFor(int mode) {
        return 8 << clampMode(mode);
    }

    /** GTNL steam: 128 mB/t scaled by the circuit multiplier. */
    public static long upkeepFor(int mode) {
        return STEAM_UPKEEP << clampMode(mode);
    }

    @Override
    public void onElevatorTick(SteamElevator elevator) {
        if (!consumeSteam(getSteamUpkeep())) return;
        if (++progress < cycleTicks()) return;
        progress = 0;
        processBatch();
    }

    /** Processes up to {@link #maxParallel()} ores, each paying the washing fluid of its recipe. */
    private void processBatch() {
        int parallel = 0;
        outer:
        for (NotifiableItemStackHandler handler : inputItemHandlers()) {
            for (int slot = 0; slot < handler.getSlots(); slot++) {
                if (parallel >= maxParallel()) break outer;
                ItemStack input = handler.getStackInSlot(slot);
                if (!isOre(input)) continue;
                List<ItemStack> outputs = refine(input, chainCircuit());
                if (outputs.isEmpty()) continue;
                if (!drainRequiredFluid(input)) break outer;
                if (!canInsertItems(outputs)) break outer;
                handler.extractItemInternal(slot, 1, false);
                insertItems(outputs);
                parallel++;
            }
        }
        if (parallel > 0) {
            markDirty();
        }
    }

    /** Drains the washing fluid of the integrated recipe for {@code ore} + the current circuit. */
    private boolean drainRequiredFluid(ItemStack ore) {
        FluidStack required = requiredFluidFor(ore);
        return required.isEmpty() || drainFluid(required, required.getAmount());
    }

    /** The fluid input of the integrated recipe matching {@code ore} on the current circuit, or empty. */
    private FluidStack requiredFluidFor(ItemStack ore) {
        return requiredFluid(recipeFor(ore, chainCircuit()));
    }

    /** The required fluid for the first processable ore, for the module UI (may be empty). */
    private FluidStack requiredFluidForFirstOre() {
        for (NotifiableItemStackHandler handler : inputItemHandlers()) {
            for (int slot = 0; slot < handler.getSlots(); slot++) {
                ItemStack input = handler.getStackInSlot(slot);
                if (isOre(input)) return requiredFluidFor(input);
            }
        }
        return FluidStack.EMPTY;
    }

    /** The integrated recipe for {@code ore} on {@code circuit} (1..7), or {@code null}. */
    private static GTRecipe recipeFor(ItemStack ore, int circuit) {
        if (ore.isEmpty()) return null;
        return GTNARecipeType.ORE_PROCESSING_RECIPES.db().find(
                Map.of(ItemRecipeCapability.CAP, List.of(Ingredient.of(ore))),
                recipe -> circuitOf(recipe) == circuit);
    }

    private static int circuitOf(GTRecipe recipe) {
        for (Content content : recipe.getInputContents(ItemRecipeCapability.CAP)) {
            if (content.content instanceof IntCircuitIngredient circuit) {
                ItemStack[] items = circuit.getItems();
                if (items.length > 0) {
                    return IntCircuitBehaviour.getCircuitConfiguration(items[0]);
                }
            }
        }
        return -1;
    }

    /** The first fluid input of {@code recipe} (with its amount), or empty. */
    private static FluidStack requiredFluid(GTRecipe recipe) {
        if (recipe == null) return FluidStack.EMPTY;
        for (Content content : recipe.getInputContents(FluidRecipeCapability.CAP)) {
            if (content.content instanceof FluidIngredient ingredient) {
                FluidStack[] stacks = ingredient.getStacks();
                if (stacks.length > 0) return stacks[0];
            }
        }
        return FluidStack.EMPTY;
    }

    /**
     * GTLCore/GTNL's seven integrated chains (circuit 1..7), indexed from 0:
     * <ol>
     * <li>Grinding-Grinding-Centrifuge</li>
     * <li>Grinding-Ore Wash-Thermal Centrifuge-Grinding</li>
     * <li>Grinding-Ore Wash-Grinding-Centrifuge</li>
     * <li>Grinding-Ore Wash-Sifting-Centrifuge</li>
     * <li>Grinding-Chemical Bath-Thermal Centrifuge-Grinding</li>
     * <li>Grinding-Chemical Bath-Grinding-Centrifuge</li>
     * <li>Grinding-Chemical Bath-Sifting-Centrifuge</li>
     * </ol>
     */
    private static final GTRecipeType[][] CHAINS = {
            { GTRecipeTypes.MACERATOR_RECIPES, GTRecipeTypes.MACERATOR_RECIPES, GTRecipeTypes.CENTRIFUGE_RECIPES },
            { GTRecipeTypes.MACERATOR_RECIPES, GTRecipeTypes.ORE_WASHER_RECIPES,
                    GTRecipeTypes.THERMAL_CENTRIFUGE_RECIPES, GTRecipeTypes.MACERATOR_RECIPES },
            { GTRecipeTypes.MACERATOR_RECIPES, GTRecipeTypes.ORE_WASHER_RECIPES,
                    GTRecipeTypes.MACERATOR_RECIPES, GTRecipeTypes.CENTRIFUGE_RECIPES },
            { GTRecipeTypes.MACERATOR_RECIPES, GTRecipeTypes.ORE_WASHER_RECIPES,
                    GTRecipeTypes.SIFTER_RECIPES, GTRecipeTypes.CENTRIFUGE_RECIPES },
            { GTRecipeTypes.MACERATOR_RECIPES, GTRecipeTypes.CHEMICAL_BATH_RECIPES,
                    GTRecipeTypes.THERMAL_CENTRIFUGE_RECIPES, GTRecipeTypes.MACERATOR_RECIPES },
            { GTRecipeTypes.MACERATOR_RECIPES, GTRecipeTypes.CHEMICAL_BATH_RECIPES,
                    GTRecipeTypes.MACERATOR_RECIPES, GTRecipeTypes.CENTRIFUGE_RECIPES },
            { GTRecipeTypes.MACERATOR_RECIPES, GTRecipeTypes.CHEMICAL_BATH_RECIPES,
                    GTRecipeTypes.SIFTER_RECIPES, GTRecipeTypes.CENTRIFUGE_RECIPES },
    };

    /** The circuit 1..7 chain applied to {@code input}, each stage a GTCEu recipe map. */
    public static List<ItemStack> refine(ItemStack input, int circuit) {
        int index = Math.max(0, Math.min(CHAINS.length - 1, circuit - 1));
        List<ItemStack> current = List.of(input.copyWithCount(1));
        for (GTRecipeType stage : CHAINS[index]) {
            current = applyMap(stage, current);
        }
        return current;
    }

    /** The chain selected by the circuit in the input bus (circuit 0 falls back to chain 1). */
    private int chainCircuit() {
        return Math.max(1, Math.min(CHAINS.length, findCircuit()));
    }

    private static List<ItemStack> applyMap(GTRecipeType type, List<ItemStack> inputs) {
        List<ItemStack> result = new ArrayList<>();
        for (ItemStack stack : inputs) {
            List<ItemStack> products = recipeOutputs(type, stack);
            if (products.isEmpty()) {
                result.add(stack);
            } else {
                for (ItemStack product : products) {
                    result.add(product.copyWithCount(product.getCount() * stack.getCount()));
                }
            }
            if (result.size() > 64) break; // safety cap on the intermediate chain
        }
        return result;
    }

    /** All item outputs of a recipe of {@code type} matching {@code input}, or an empty list. */
    private static List<ItemStack> recipeOutputs(GTRecipeType type, ItemStack input) {
        if (input.isEmpty()) return List.of();
        GTRecipe recipe = type.db().find(
                Map.of(ItemRecipeCapability.CAP, List.of(Ingredient.of(input))),
                r -> true);
        if (recipe == null) return List.of();
        List<ItemStack> outputs = new ArrayList<>();
        for (Content content : recipe.getOutputContents(ItemRecipeCapability.CAP)) {
            if (content.content instanceof Ingredient ingredient && ingredient.getItems().length > 0) {
                outputs.add(ingredient.getItems()[0].copy());
            }
        }
        return outputs;
    }

    /** True for an ore/crushed/raw-ore input the processor can work (not an ingot or a pure dust). */
    private static boolean isOre(ItemStack stack) {
        if (stack.isEmpty()) return false;
        MaterialEntry entry = ChemicalHelper.getMaterialEntry(stack.getItem());
        if (entry.isEmpty()) return false;
        TagPrefix prefix = entry.tagPrefix();
        return TagPrefix.ORES.containsKey(prefix) ||
                prefix == TagPrefix.rawOre ||
                prefix == TagPrefix.crushed ||
                prefix == TagPrefix.crushedPurified ||
                prefix == TagPrefix.crushedRefined;
    }

    @Override
    protected Widget createModuleUIWidget() {
        WidgetGroup group = screenGroup(150, 76);
        group.addWidget(new LabelWidget(5, 4, () -> "Ore Processor tier §b" + getModuleTier()));
        group.addWidget(new LabelWidget(5, 16, () -> "Mode §b" + mode() + " §r| §b" + maxParallel() +
                "x §r| §b" + getSteamUpkeep() + " mB/t"));
        group.addWidget(new LabelWidget(5, 28, () -> {
            FluidStack required = requiredFluidForFirstOre();
            if (required.isEmpty()) return "Fluid: §bnot required";
            String name = required.getDisplayName().getString().replace("%", "%%");
            return "Fluid: §b" + name + " §r" + countFluid(required) + " / " + required.getAmount();
        }));
        group.addWidget(new LabelWidget(5, 40, () -> "§7Input hatch total: §b" + totalInputFluid() + " §7mB"));
        group.addWidget(new LabelWidget(5, 52, () -> "§7Circuit + ore: input bus"));
        return group;
    }
}
