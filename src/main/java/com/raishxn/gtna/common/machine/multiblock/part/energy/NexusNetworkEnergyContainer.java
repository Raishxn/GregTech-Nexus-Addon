// SPDX-License-Identifier: GPL-3.0-only
// Adapted behavior: GTLAdditions 8caff5e93a5e65914d10dd176d48d66e7ec8c329; see THIRD_PARTY_NOTICES.md.
package com.raishxn.gtna.common.machine.multiblock.part.energy;

import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.EnergyStack;

import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import com.raishxn.gtna.common.data.NexusEnergyNetwork;
import com.raishxn.gtna.utils.datastructure.Int128;

import java.math.BigInteger;
import java.util.List;

/** Direct recipe transactions: no local buffer, cable transfer or second wireless energy account. */
public final class NexusNetworkEnergyContainer extends NotifiableRecipeHandlerTrait<EnergyStack>
                                               implements IEnergyContainer {

    private final NexusNetworkTerminalPartMachine terminal;
    private static final BigInteger LONG_MAX = BigInteger.valueOf(Long.MAX_VALUE);

    public NexusNetworkEnergyContainer(NexusNetworkTerminalPartMachine terminal) {
        super(terminal);
        this.terminal = terminal;
        setCapabilityValidator(side -> side == null);
    }

    private ServerLevel level() {
        return terminal.getLevel() instanceof ServerLevel level ? level : null;
    }

    private boolean enabled() {
        return terminal.isWorkingEnabled() && terminal.getNetworkOwner() != null && level() != null;
    }

    public long quote(long amount) {
        if (!enabled() || amount == Long.MIN_VALUE) return 0;
        var network = NexusEnergyNetwork.get(level());
        if (amount > 0 && terminal.networkIO == IO.OUT)
            return network.quoteInsertion(terminal.getNetworkOwner(), new Int128(amount), level()).toLong();
        if (amount < 0 && terminal.networkIO == IO.IN) {
            var available = network.availableForTransfer(terminal.getNetworkOwner(), level()).toBigInteger();
            return -available.min(BigInteger.valueOf(-amount)).longValueExact();
        }
        return 0;
    }

    @Override
    public long changeEnergy(long amount) {
        long accepted = quote(amount);
        if (accepted == 0) return 0;
        var network = NexusEnergyNetwork.get(level());
        if (accepted > 0)
            accepted = network.addEnergy(terminal.getNetworkOwner(), new Int128(accepted), level()).toLong();
        else if (!network.consumeEnergy(terminal.getNetworkOwner(), new Int128(-accepted), level())) return 0;
        if (accepted != 0) terminal.report(new Int128(accepted > 0 ? accepted : -accepted));
        return accepted;
    }

    @Override
    public List<EnergyStack> handleRecipeInner(IO io, GTRecipe recipe, List<EnergyStack> left, boolean simulate) {
        if (io != terminal.networkIO) return left;
        BigInteger sum = BigInteger.ZERO;
        for (EnergyStack stack : left) {
            if (stack.voltage() < 0 || stack.amperage() < 1) return left;
            sum = sum.add(BigInteger.valueOf(stack.voltage()).multiply(BigInteger.valueOf(stack.amperage())));
        }
        if (sum.compareTo(LONG_MAX) > 0) return left;
        long amount = sum.longValueExact();
        if (amount == 0) return null;
        long signed = io == IO.IN ? -amount : amount;
        if (quote(signed) != signed) return left;
        if (!simulate && changeEnergy(signed) != signed) return left;
        return null;
    }

    @Override
    public long getEnergyStored() {
        return terminal.networkIO == IO.IN && enabled() ? NexusEnergyNetwork.get(level())
                .availableForTransfer(terminal.getNetworkOwner(), level()).toBigInteger()
                .min(BigInteger.valueOf(getEnergyCapacity()))
                .longValueExact() : 0;
    }

    @Override
    public long getEnergyCapacity() {
        return nominalBudget(terminal.networkIO, true);
    }

    @Override
    public long getEnergyCanBeInserted() {
        return terminal.networkIO == IO.OUT ? quote(Long.MAX_VALUE) : 0;
    }

    /** Share the nominal long budget across attached terminals; leave room for ordinary hatches. */
    private long nominalVoltage(IO io) {
        return nominalBudget(io, false);
    }

    private long nominalBudget(IO io, boolean capacity) {
        if (terminal.networkIO != io) return 0;
        if (terminal.getControllers().isEmpty()) return Long.MAX_VALUE;
        var controller = terminal.getControllers().first();
        int terminals = 0;
        BigInteger ordinary = BigInteger.ZERO;
        for (var part : controller.getParts()) {
            if (part instanceof NexusNetworkTerminalPartMachine direct) {
                if (direct.networkIO == io) terminals++;
            } else {
                for (var trait : part.self().getTraits()) if (trait instanceof IEnergyContainer container) {
                    long voltage = io == IO.IN ? container.getInputVoltage() : container.getOutputVoltage();
                    long amps = io == IO.IN ? container.getInputAmperage() : container.getOutputAmperage();
                    if (voltage > 0)
                        ordinary = ordinary.add(capacity ? BigInteger.valueOf(container.getEnergyCapacity()) :
                                BigInteger.valueOf(voltage).multiply(BigInteger.valueOf(amps)));
                }
            }
        }
        return LONG_MAX.subtract(ordinary).max(BigInteger.ZERO).divide(BigInteger.valueOf(Math.max(1, terminals)))
                .longValueExact();
    }

    @Override
    public long getInputVoltage() {
        return nominalVoltage(IO.IN);
    }

    @Override
    public long getOutputVoltage() {
        return nominalVoltage(IO.OUT);
    }

    @Override
    public long getInputAmperage() {
        return terminal.networkIO == IO.IN ? 1 : 0;
    }

    @Override
    public long getOutputAmperage() {
        return terminal.networkIO == IO.OUT ? 1 : 0;
    }

    @Override
    public long acceptEnergyFromNetwork(Direction side, long voltage, long amps) {
        return 0;
    }

    @Override
    public boolean inputsEnergy(Direction side) {
        return false;
    }

    @Override
    public boolean outputsEnergy(Direction side) {
        return false;
    }

    @Override
    public List<Object> getContents() {
        return List.of(new EnergyStack(getEnergyStored()));
    }

    @Override
    public double getTotalContentAmount() {
        return getEnergyStored();
    }

    @Override
    public RecipeCapability<EnergyStack> getCapability() {
        return EURecipeCapability.CAP;
    }

    @Override
    public IO getHandlerIO() {
        return terminal.networkIO;
    }
}
