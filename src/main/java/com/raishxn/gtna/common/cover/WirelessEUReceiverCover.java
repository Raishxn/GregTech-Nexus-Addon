package com.raishxn.gtna.common.cover;

import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;

import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import com.raishxn.gtna.api.capability.WirelessEnergyManager;
import com.raishxn.gtna.utils.datastructure.Int128;

/**
 * Wireless EU Receiver Cover — pulls EU from the Nexus network into the
 * attached singleblock machine every server tick.
 *
 * Direction detection: a receiver container has getInputVoltage() > 0
 * (and getOutputVoltage() == 0), because IEnergyContainer does not expose
 * getHandlerIO() — that field is internal to NotifiableEnergyContainer.
 */
public class WirelessEUReceiverCover extends WirelessEUCoverBase {

    public WirelessEUReceiverCover(CoverDefinition definition, ICoverable coverHolder,
                                    Direction attachedSide, int tier, int amperage) {
        super(definition, coverHolder, attachedSide, tier, amperage);
    }

    @Override
    protected boolean canAttachForDirection(IEnergyContainer container) {
        // Input containers accept energy: inputVoltage > 0
        if (container.getInputVoltage() <= 0) return false;
        return !alreadyHasCoverOfType(WirelessEUReceiverCover.class);
    }

    @Override
    protected void onServerTick() {
        if (!(coverHolder.getLevel() instanceof ServerLevel serverLevel)) return;
        if (!isNetworkReady(serverLevel)) return;

        IEnergyContainer container = getEnergyContainer();
        if (container == null) return;

        long deficit = container.getEnergyCapacity() - container.getEnergyStored();
        if (deficit <= 0) return;

        long request = Math.min(deficit, maxEnergyPerTick());
        if (request <= 0) return;

        Int128 consumed = WirelessEnergyManager.consumeEnergy(
                serverLevel, getNetworkOwner(), new Int128(request));

        long transferred = 0L;
        if (!consumed.isZero()) {
            long actual = consumed.toLong();
            container.addEnergy(actual);
            transferred = actual;
        }

        WirelessEnergyManager.reportConnection(
                serverLevel, getNetworkOwner(), getGlobalPos(serverLevel),
                false, tier, amperage, "Wireless EU Receiver", new Int128(transferred));
    }
}
