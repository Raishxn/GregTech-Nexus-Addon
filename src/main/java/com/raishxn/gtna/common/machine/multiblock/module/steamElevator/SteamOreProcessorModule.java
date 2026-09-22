package com.raishxn.gtna.common.machine.multiblock.module.steamElevator;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.gui.widget.TankWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;

import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

/**
 * GTNL {@code SteamOreProcessorModule} port (LGPLv3, original by ScienceNotLeisure).
 *
 * <p>
 * GTNL runs a configurable multi-stage ore-processing chain (macerate → wash → thermal → centrifuge,
 * chem-bath, sifter, forge-hammer …) with up to 8 parallels. GTCEu's recipe processing is
 * controller-centric, so reproducing every stage inside a part machine is out of scope; this port
 * keeps the processor's contract and the tooltip's concrete numbers:
 * <ul>
 * <li>the circuit in the controller slot selects the mode (0–6) and its processing time
 * (600/300/200/400/340/640/20 ticks);</li>
 * <li>up to {@code 8 * 2^mode} ores per batch ("Can process up to 16 ores at a time" at circuit 1);</li>
 * <li>every ore costs 128L steam/t scaled by the circuit ("Set circuit to double both parallel and
 * EU consumption"), 10L distilled water and 1L lubricant;</li>
 * <li>the products are the ore's macerated drops (the chain depth is the documented simplification).</li>
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

    public final NotifiableItemStackHandler inputInventory;
    public final NotifiableItemStackHandler outputInventory;
    public final NotifiableItemStackHandler circuitInventory;
    public final NotifiableFluidTank waterTank;
    public final NotifiableFluidTank lubricantTank;

    private int progress;

    public SteamOreProcessorModule(IMachineBlockEntity holder, int tier) {
        super(holder, tier);
        this.inputInventory = new NotifiableItemStackHandler(this, 9, IO.IN);
        this.outputInventory = new NotifiableItemStackHandler(this, 9, IO.OUT);
        this.circuitInventory = new NotifiableItemStackHandler(this, 1, IO.IN)
                .setFilter(IntCircuitBehaviour::isIntegratedCircuit);
        this.waterTank = new NotifiableFluidTank(this, 1, 16_000, IO.IN)
                .setFilter(stack -> stack.getFluid().is(GTMaterials.DistilledWater.getFluidTag()));
        this.lubricantTank = new NotifiableFluidTank(this, 1, 16_000, IO.IN)
                .setFilter(stack -> stack.getFluid().is(GTMaterials.Lubricant.getFluidTag()));
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    /** The mode selected by the circuit (0–6). */
    private int mode() {
        int circuit = IntCircuitBehaviour.getCircuitConfiguration(circuitInventory.getStackInSlot(0));
        return Math.max(0, Math.min(MAX_MODE, circuit));
    }

    private int cycleTicks() {
        return MODE_TICKS[mode()];
    }

    private int maxParallel() {
        return 8 << mode();
    }

    @Override
    public long getSteamUpkeep() {
        // GTNL: requiredEUt = 128 * 2^circuit.
        return STEAM_UPKEEP << mode();
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
        for (int slot = 0; slot < inputInventory.getSlots() && parallel < maxParallel(); slot++) {
            ItemStack input = inputInventory.getStackInSlot(slot);
            if (input.isEmpty()) continue;
            ItemStack output = macerate(input);
            if (output.isEmpty()) continue;
            if (!drain(waterTank, WATER_PER_ORE)) break;
            if (!drain(lubricantTank, LUBRICANT_PER_ORE)) break;
            if (!insertOutput(output)) break;
            inputInventory.extractItem(slot, 1, false);
            parallel++;
        }
        if (parallel > 0) {
            markDirty();
        }
    }

    private ItemStack macerate(ItemStack input) {
        MaterialStack stack = ChemicalHelper.getMaterialStack(input);
        if (stack == null || stack.isEmpty()) return ItemStack.EMPTY;
        var material = stack.material();
        // Already-dust inputs are not ore; skip them so the module is an ore processor, not a duper.
        if (ItemStack.isSameItem(input, ChemicalHelper.get(TagPrefix.dust, material))) return ItemStack.EMPTY;
        ItemStack crushed = ChemicalHelper.get(TagPrefix.crushed, material, 2);
        if (!crushed.isEmpty()) return crushed;
        return ChemicalHelper.get(TagPrefix.dust, material, 1);
    }

    private boolean drain(NotifiableFluidTank tank, int amount) {
        FluidStack drained = tank.drainInternal(amount, IFluidHandler.FluidAction.SIMULATE);
        if (drained.getAmount() < amount) return false;
        tank.drainInternal(amount, IFluidHandler.FluidAction.EXECUTE);
        return true;
    }

    private boolean insertOutput(ItemStack stack) {
        int remaining = stack.getCount();
        for (int slot = 0; slot < outputInventory.getSlots() && remaining > 0; slot++) {
            ItemStack inserted = outputInventory.insertItem(slot, new ItemStack(stack.getItem(), remaining), false);
            remaining = inserted.getCount();
        }
        return remaining < stack.getCount();
    }

    @Override
    protected Widget createModuleUIWidget() {
        WidgetGroup group = screenGroup(150, 80);
        group.addWidget(new LabelWidget(5, 4, () -> "Ore Processor tier §b" + getModuleTier()));
        group.addWidget(new LabelWidget(5, 15, () -> "Mode §b" + mode() + " §r| parallel §b" + maxParallel() +
                " §r| §b" + getSteamUpkeep() + " mB/t"));
        group.addWidget(new SlotWidget(circuitInventory, 0, 5, 28).setBackgroundTexture(GuiTextures.SLOT));
        for (int i = 0; i < 3; i++) {
            group.addWidget(new SlotWidget(inputInventory, i, 27 + i * 18, 28)
                    .setBackgroundTexture(GuiTextures.SLOT));
        }
        group.addWidget(new TankWidget(waterTank.getStorages()[0], 90, 28, 18, 18, true, true));
        group.addWidget(new TankWidget(lubricantTank.getStorages()[0], 112, 28, 18, 18, true, true));
        for (int i = 0; i < 3; i++) {
            group.addWidget(new SlotWidget(outputInventory, i, 27 + i * 18, 52)
                    .setBackgroundTexture(GuiTextures.SLOT));
        }
        return group;
    }
}
