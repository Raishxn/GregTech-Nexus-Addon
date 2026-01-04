package com.raishxn.gtna.utils;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;
import com.gregtechceu.gtceu.utils.GTTransferUtils;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.List;

public class MachineUtil {
    public static boolean outputItem(WorkableMultiblockMachine machine, ItemStack item) {
        if (item.isEmpty()) return true;

        var ioMap = machine.getCapabilitiesFlat().get(IO.OUT);
        if (ioMap == null) return false;

        var handlers = ioMap.get(ItemRecipeCapability.CAP);
        if (handlers == null) return false;

        ItemStack remaining = item.copy();

        for (Object handlerObj : handlers) {
            if (handlerObj instanceof IItemHandler handler) {
                // GTTransferUtils lida melhor com faces e restrições do que o Forge puro
                remaining = GTTransferUtils.insertItem(handler, remaining, false);
                if (remaining.isEmpty()) return true;
            }
        }

        return remaining.isEmpty();
    }
    public static boolean inputItem(WorkableMultiblockMachine machine, ItemStack item) {
        var ioMap = machine.getCapabilitiesFlat().get(IO.IN);
        if (ioMap == null) return false;

        var handlers = ioMap.get(ItemRecipeCapability.CAP);
        if (handlers == null) return false;

        int needed = item.getCount();

        for (Object handlerObj : handlers) {
            if (handlerObj instanceof IItemHandler handler) {
                for (int i = 0; i < handler.getSlots(); i++) {
                    ItemStack inSlot = handler.getStackInSlot(i);
                    if (ItemStack.isSameItemSameTags(inSlot, item)) {
                        int extracted = handler.extractItem(i, needed, false).getCount();
                        needed -= extracted;
                        if (needed <= 0) return true;
                    }
                }
            }
        }
        return needed <= 0;
    }
    public static boolean notConsumableItem(WorkableMultiblockMachine machine, ItemStack item) {
        var ioMap = machine.getCapabilitiesFlat().get(IO.IN);
        if (ioMap == null) return false;

        var handlers = ioMap.get(ItemRecipeCapability.CAP);
        if (handlers == null) return false;

        int foundCount = 0;

        for (Object handlerObj : handlers) {
            if (handlerObj instanceof IItemHandler handler) {
                for (int i = 0; i < handler.getSlots(); i++) {
                    ItemStack inSlot = handler.getStackInSlot(i);
                    if (ItemStack.isSameItemSameTags(inSlot, item)) {
                        foundCount += inSlot.getCount();
                        if (foundCount >= item.getCount()) return true;
                    }
                }
            }
        }
        return false;
    }
    public static boolean notConsumableCircuit(WorkableMultiblockMachine machine, int configuration) {
        var ioMap = machine.getCapabilitiesFlat().get(IO.IN);
        if (ioMap == null) return false;

        var handlers = ioMap.get(ItemRecipeCapability.CAP);
        if (handlers == null) return false;

        for (Object handlerObj : handlers) {
            if (handlerObj instanceof IItemHandler handler) {
                for (int i = 0; i < handler.getSlots(); i++) {
                    ItemStack stack = handler.getStackInSlot(i);
                    if (IntCircuitBehaviour.isIntegratedCircuit(stack)) {
                        if (IntCircuitBehaviour.getCircuitConfiguration(stack) == configuration) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
    public static boolean outputFluid(WorkableMultiblockMachine machine, FluidStack fluid) {
        if (fluid.isEmpty()) return true;

        var ioMap = machine.getCapabilitiesFlat().get(IO.OUT);
        if (ioMap == null) return false;

        var handlers = ioMap.get(FluidRecipeCapability.CAP);
        if (handlers == null) return false;

        FluidStack remaining = fluid.copy();

        for (Object handlerObj : handlers) {
            if (handlerObj instanceof com.lowdragmc.lowdraglib.side.fluid.IFluidStorage handler) {
                long filled = handler.fill(remaining, false);
                remaining.shrink(filled);
                if (remaining.isEmpty()) return true;
            }
        }
        return remaining.isEmpty();
    }
    public static boolean inputFluid(WorkableMultiblockMachine machine, FluidStack fluid) { return false; }
    public static boolean inputEU(WorkableMultiblockMachine machine, long eu) { return false; }
}