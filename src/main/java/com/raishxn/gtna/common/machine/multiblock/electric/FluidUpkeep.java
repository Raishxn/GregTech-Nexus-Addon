package com.raishxn.gtna.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;

import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

/** One atomic fluid upkeep payment across the formed machine's input hatches. */
final class FluidUpkeep {

    private FluidUpkeep() {}

    static boolean consume(WorkableElectricMultiblockMachine machine, Fluid fluid, int amount) {
        FluidStack request = new FluidStack(fluid, amount);
        int available = 0;
        for (var part : machine.getParts()) {
            for (var handlers : part.getRecipeHandlers()) {
                if (!handlers.isValid(IO.IN)) continue;
                for (var candidate : handlers.getCapability(FluidRecipeCapability.CAP)) {
                    available += drain(candidate, request, IFluidHandler.FluidAction.SIMULATE);
                    if (available >= amount) break;
                }
                if (available >= amount) break;
            }
            if (available >= amount) break;
        }
        if (available < amount) return false;
        int remaining = amount;
        for (var part : machine.getParts()) {
            for (var handlers : part.getRecipeHandlers()) {
                if (!handlers.isValid(IO.IN)) continue;
                for (var candidate : handlers.getCapability(FluidRecipeCapability.CAP)) {
                    request.setAmount(remaining);
                    remaining -= drain(candidate, request, IFluidHandler.FluidAction.EXECUTE);
                    if (remaining == 0) return true;
                }
            }
        }
        return false;
    }

    private static int drain(Object candidate, FluidStack request, IFluidHandler.FluidAction action) {
        if (candidate instanceof NotifiableFluidTank tank) {
            return tank.drainInternal(request, action).getAmount();
        }
        if (candidate instanceof IFluidHandler handler) {
            return handler.drain(request, action).getAmount();
        }
        return 0;
    }
}
