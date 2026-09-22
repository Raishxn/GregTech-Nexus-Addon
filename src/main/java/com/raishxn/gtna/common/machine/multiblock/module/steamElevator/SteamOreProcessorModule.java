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
 * chem-bath, sifter, forge-hammer …) with up to 8 parallels. Reproducing every stage in a part
 * machine is not possible with GTCEu's controller-centric recipe logic, so this port keeps the ore
 * processor's core contract — water + ore in, crushed/dust products out at a fixed steam upkeep — with
 * a single maceration stage using GTCEu's material registry (no external recipe lookup). The
 * documented simplification is the chain depth, not the input/output identity.
 */
public class SteamOreProcessorModule extends SteamElevatorModuleMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            SteamOreProcessorModule.class, SteamElevatorModuleMachine.MANAGED_FIELD_HOLDER);

    public static final long STEAM_UPKEEP = 128;
    private static final int WATER_PER_ITEM = 1000;
    private static final int CYCLE_TICKS = 20;

    public final NotifiableItemStackHandler inputInventory;
    public final NotifiableItemStackHandler outputInventory;
    public final NotifiableFluidTank waterTank;

    private int progress;

    public SteamOreProcessorModule(IMachineBlockEntity holder, int tier) {
        super(holder, tier);
        this.inputInventory = new NotifiableItemStackHandler(this, 9, IO.IN);
        this.outputInventory = new NotifiableItemStackHandler(this, 9, IO.OUT);
        this.waterTank = new NotifiableFluidTank(this, 1, 16_000, IO.IN);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public long getSteamUpkeep() {
        return STEAM_UPKEEP;
    }

    @Override
    public void onElevatorTick(SteamElevator elevator) {
        if (!consumeSteam(getSteamUpkeep())) return;
        if (++progress < CYCLE_TICKS) return;
        progress = 0;
        processOne();
    }

    private void processOne() {
        for (int slot = 0; slot < inputInventory.getSlots(); slot++) {
            ItemStack input = inputInventory.getStackInSlot(slot);
            if (input.isEmpty()) continue;
            ItemStack output = macerate(input);
            if (output.isEmpty()) continue;
            if (!drainWater(WATER_PER_ITEM)) continue;
            if (!insertOutput(output)) continue;
            inputInventory.extractItem(slot, 1, false);
            markDirty();
            return;
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

    private boolean drainWater(int amount) {
        FluidStack drained = waterTank.drainInternal(amount, IFluidHandler.FluidAction.SIMULATE);
        if (drained.getAmount() < amount) return false;
        waterTank.drainInternal(amount, IFluidHandler.FluidAction.EXECUTE);
        return true;
    }

    private boolean insertOutput(ItemStack stack) {
        int remaining = stack.getCount();
        for (int slot = 0; slot < outputInventory.getSlots() && remaining > 0; slot++) {
            ItemStack inserted = outputInventory.insertItem(slot, new ItemStack(stack.getItem(), remaining),
                    false);
            remaining = inserted.getCount();
        }
        return remaining < stack.getCount();
    }

    @Override
    protected Widget createModuleUIWidget() {
        WidgetGroup group = screenGroup(150, 80);
        group.addWidget(new LabelWidget(5, 4, () -> "Ore Processor tier §b" + getModuleTier()));
        for (int i = 0; i < 3; i++) {
            group.addWidget(new SlotWidget(inputInventory, i, 5 + i * 18, 18)
                    .setBackgroundTexture(GuiTextures.SLOT));
        }
        group.addWidget(new TankWidget(waterTank.getStorages()[0], 63, 18, 18, 18, true, true));
        for (int i = 0; i < 3; i++) {
            group.addWidget(new SlotWidget(outputInventory, i, 89 + i * 18, 18)
                    .setBackgroundTexture(GuiTextures.SLOT));
        }
        group.addWidget(new LabelWidget(5, 42, () -> "Water: §b" + waterTank.getFluidInTank(0).getAmount() + " mB"));
        return group;
    }
}
