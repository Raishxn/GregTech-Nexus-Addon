package com.raishxn.gtna.api.capability;

import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.raishxn.gtna.api.data.SteamNetworkData;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class WirelessSteamFluidHandler implements IFluidHandler {

    private final Level level;
    private final UUID ownerUUID;
    private final int capacityLimit; // Nova variável para controlar o limite (20k ou MAX)

    // O construtor agora recebe a capacidade máxima deste hatch específico
    public WirelessSteamFluidHandler(Level level, UUID ownerUUID, int capacityLimit) {
        this.level = level;
        this.ownerUUID = ownerUUID;
        this.capacityLimit = capacityLimit;
    }

    private SteamNetworkData getData() {
        return SteamNetworkData.get(level);
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        SteamNetworkData data = getData();
        if (data == null || ownerUUID == null) return FluidStack.EMPTY;

        long amount = data.getSteam(ownerUUID);
        int visualAmount = (int) Math.min(this.capacityLimit, amount);

        if (visualAmount <= 0) return FluidStack.EMPTY;
        return GTMaterials.Steam.getFluid(visualAmount);
    }

    @Override
    public int getTankCapacity(int tank) {
        return this.capacityLimit;
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return stack.getFluid().isSame(GTMaterials.Steam.getFluid());
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (!isFluidValid(0, resource) || ownerUUID == null) return 0;

        SteamNetworkData data = getData();
        if (data == null) return 0;

        int toFill = Math.min(resource.getAmount(), this.capacityLimit);

        if (action.execute()) {
            data.addSteam(ownerUUID, toFill);
        }
        return toFill;
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        if (!resource.getFluid().isSame(GTMaterials.Steam.getFluid()) || ownerUUID == null) return FluidStack.EMPTY;
        return drain(resource.getAmount(), action);
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        if (ownerUUID == null) return FluidStack.EMPTY;

        SteamNetworkData data = getData();
        if (data == null) return FluidStack.EMPTY;

        long current = data.getSteam(ownerUUID);

        // Limita a saída à capacidade do tanque (gargalo de transferência)
        long effectiveLimit = Math.min(maxDrain, this.capacityLimit);
        int toDrain = (int) Math.min(effectiveLimit, current);

        if (toDrain <= 0) return FluidStack.EMPTY;

        if (action.execute()) {
            data.consumeSteam(ownerUUID, toDrain);
        }

        return GTMaterials.Steam.getFluid(toDrain);
    }
}