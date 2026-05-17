package com.raishxn.gtna.common.cover;

import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;

import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import com.raishxn.gtna.api.capability.WirelessEnergyManager;
import com.raishxn.gtna.utils.datastructure.Int128;

/**
 * Wireless EU Transmitter Cover — drains EU from the attached singleblock
 * machine and pushes it into the Nexus network every server tick.
 *
 * Direction detection: a transmitter container has getOutputVoltage() > 0
 * (and getInputVoltage() == 0), because IEnergyContainer does not expose
 * getHandlerIO() — that field is internal to NotifiableEnergyContainer.
 */
public class WirelessEUTransmitterCover extends WirelessEUCoverBase {

    public WirelessEUTransmitterCover(CoverDefinition definition, ICoverable coverHolder,
                                       Direction attachedSide, int tier, int amperage) {
        super(definition, coverHolder, attachedSide, tier, amperage);
    }

    @Override
    protected boolean canAttachForDirection(IEnergyContainer container) {
        // Output containers emit energy: outputVoltage > 0
        if (container.getOutputVoltage() <= 0) return false;
        return !alreadyHasCoverOfType(WirelessEUTransmitterCover.class);
    }

    @Override
    protected void onServerTick() {
        if (!(coverHolder.getLevel() instanceof ServerLevel serverLevel)) return;
        if (!isNetworkReady(serverLevel)) return;

        IEnergyContainer container = getEnergyContainer();
        if (container == null) return;

        long stored = container.getEnergyStored();
        if (stored <= 0) return;

        long offer = Math.min(stored, maxEnergyPerTick());
        if (offer <= 0) return;

        Int128 accepted = WirelessEnergyManager.addEnergy(
                serverLevel, getNetworkOwner(), new Int128(offer));

        long transferred = 0L;
        if (!accepted.isZero()) {
            long actual = accepted.toLong();
            container.removeEnergy(actual);
            transferred = actual;
        }

        WirelessEnergyManager.reportConnection(
                serverLevel, getNetworkOwner(), getGlobalPos(serverLevel),
                true, tier, amperage, "Wireless EU Transmitter", new Int128(transferred));
    }
}
