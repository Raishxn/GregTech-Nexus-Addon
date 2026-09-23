package com.raishxn.gtna.common.machine.multiblock.module.steamElevator;

import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;

import com.raishxn.gtna.common.data.GTNARecipeType;

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
 * EU consumption"), 10L distilled water and 1L lubricant, both drawn from the <b>input hatch</b>;</li>
 * <li>the products are the ore's macerated drops inserted into the <b>output bus</b> (the chain depth
 * is the documented simplification).</li>
 * </ul>
 */
public class SteamOreProcessorModule extends SteamElevatorModuleMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            SteamOreProcessorModule.class, SteamElevatorModuleMachine.MANAGED_FIELD_HOLDER);

    /** GTNL {@code RECIPE_EUT}. */
    public static final long STEAM_UPKEEP = 128;
    private static final int WATER_PER_ORE = 10;
    private static final int LUBRICANT_PER_ORE = 1;
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
        // GTNL: requiredEUt = 128 * 2^circuit.
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
        return hasUpkeepSteam() &&
                waterAmount() >= WATER_PER_ORE &&
                countFluid(lubricant()) >= LUBRICANT_PER_ORE &&
                countItem(SteamOreProcessorModule::isOre) > 0;
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

    private static FluidStack distilledWater() {
        return GTMaterials.DistilledWater.getFluid(WATER_PER_ORE);
    }

    /** GTNL uses distilled water; vanilla water is accepted too so the module is usable early. */
    private static FluidStack vanillaWater() {
        return new FluidStack(Fluids.WATER, WATER_PER_ORE);
    }

    private int waterAmount() {
        return countFluid(distilledWater()) + countFluid(vanillaWater());
    }

    /** Drains one ore's worth of water, preferring distilled water. */
    private boolean drainWater() {
        if (waterAmount() < WATER_PER_ORE) return false;
        int distilled = Math.min(countFluid(distilledWater()), WATER_PER_ORE);
        if (distilled > 0) drainFluid(distilledWater(), distilled);
        if (distilled < WATER_PER_ORE) drainFluid(vanillaWater(), WATER_PER_ORE - distilled);
        return true;
    }

    private static FluidStack lubricant() {
        return GTMaterials.Lubricant.getFluid(LUBRICANT_PER_ORE);
    }

    @Override
    public void onElevatorTick(SteamElevator elevator) {
        if (!consumeSteam(getSteamUpkeep())) return;
        if (++progress < cycleTicks()) return;
        progress = 0;
        processBatch();
    }

    /** Processes up to {@link #maxParallel()} ores, each paying distilled water and lubricant. */
    private void processBatch() {
        int parallel = 0;
        outer:
        for (NotifiableItemStackHandler handler : inputItemHandlers()) {
            for (int slot = 0; slot < handler.getSlots(); slot++) {
                if (parallel >= maxParallel()) break outer;
                ItemStack input = handler.getStackInSlot(slot);
                if (input.isEmpty()) continue;
                ItemStack output = macerate(input);
                if (output.isEmpty()) continue;
                if (!drainWater()) break outer;
                if (!drainFluid(lubricant(), LUBRICANT_PER_ORE)) break outer;
                if (!canInsertItems(output)) break outer;
                handler.extractItemInternal(slot, 1, false);
                insertItems(output);
                parallel++;
            }
        }
        if (parallel > 0) {
            markDirty();
        }
    }

    /**
     * The first refined product for {@code input}: the GTNA {@code ore_processing} type first, then the
     * GTCEu macerator's real recipes, and finally the material-derived crushed/dust fallback. The full
     * multi-stage chain is still the documented simplification.
     */
    private ItemStack macerate(ItemStack input) {
        ItemStack processed = recipeOutput(GTNARecipeType.ORE_PROCESSING_RECIPES, input);
        if (!processed.isEmpty()) return processed;
        processed = recipeOutput(GTRecipeTypes.MACERATOR_RECIPES, input);
        if (!processed.isEmpty()) return processed;

        MaterialStack stack = ChemicalHelper.getMaterialStack(input);
        if (stack == null || stack.isEmpty()) return ItemStack.EMPTY;
        var material = stack.material();
        // Already-dust inputs are not ore; skip them so the module is an ore processor, not a duper.
        if (ItemStack.isSameItem(input, ChemicalHelper.get(TagPrefix.dust, material))) return ItemStack.EMPTY;
        ItemStack crushed = ChemicalHelper.get(TagPrefix.crushed, material, 2);
        if (!crushed.isEmpty()) return crushed;
        return ChemicalHelper.get(TagPrefix.dust, material, 1);
    }

    /** The first item output of a recipe of {@code type} matching {@code input}, or empty. */
    private static ItemStack recipeOutput(GTRecipeType type, ItemStack input) {
        if (input.isEmpty()) return ItemStack.EMPTY;
        GTRecipe recipe = type.db().find(
                Map.of(ItemRecipeCapability.CAP, List.of(Ingredient.of(input))),
                r -> true);
        if (recipe == null) return ItemStack.EMPTY;
        for (Content content : recipe.getOutputContents(ItemRecipeCapability.CAP)) {
            if (content.content instanceof Ingredient ingredient && ingredient.getItems().length > 0) {
                return ingredient.getItems()[0].copy();
            }
        }
        return ItemStack.EMPTY;
    }

    /** True for an ore/crushed input the processor can work (not an already-pure dust). */
    private static boolean isOre(ItemStack stack) {
        MaterialStack material = ChemicalHelper.getMaterialStack(stack);
        if (material == null || material.isEmpty()) return false;
        return !ItemStack.isSameItem(stack, ChemicalHelper.get(TagPrefix.dust, material.material()));
    }

    @Override
    protected Widget createModuleUIWidget() {
        WidgetGroup group = screenGroup(150, 76);
        group.addWidget(new LabelWidget(5, 4, () -> "Ore Processor tier §b" + getModuleTier()));
        group.addWidget(new LabelWidget(5, 16, () -> "Mode §b" + mode() + " §r| §b" + maxParallel() +
                "x §r| §b" + getSteamUpkeep() + " mB/t"));
        group.addWidget(new LabelWidget(5, 28, () -> "Water: §b" + waterAmount() + " §r| Lubricant: §b" +
                countFluid(lubricant())));
        group.addWidget(new LabelWidget(5, 40, () -> "§7Input hatch total: §b" + totalInputFluid() + " §7mB"));
        group.addWidget(new LabelWidget(5, 52, () -> "§7Circuit + ore: input bus"));
        return group;
    }
}
