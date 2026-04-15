package com.raishxn.gtna.common.cover;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.steam.SteamWorkableMachine;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.core.Direction;

public class InfiniteSteamSingleblockCover extends CoverBehavior {

    private TickableSubscription subscription;

    public InfiniteSteamSingleblockCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
    }

    @Override
    public boolean canAttach() {
        if (!super.canAttach()) {
            return false;
        }
        MetaMachine machine = MetaMachine.getMachine(coverHolder.getLevel(), coverHolder.getPos());
        return machine instanceof SteamWorkableMachine && !(machine instanceof IMultiController);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        subscription = coverHolder.subscribeServerTick(subscription, this::fillSteamTank);
    }

    @Override
    public void onRemoved() {
        super.onRemoved();
        if (subscription != null) {
            subscription.unsubscribe();
            subscription = null;
        }
    }

    private void fillSteamTank() {
        MetaMachine machine = MetaMachine.getMachine(coverHolder.getLevel(), coverHolder.getPos());
        if (!(machine instanceof SteamWorkableMachine steamMachine) || coverHolder.isRemote()) {
            return;
        }
        int capacity = steamMachine.steamTank.getTankCapacity(0);
        steamMachine.steamTank.fill(GTMaterials.Steam.getFluid(capacity),
                net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
    }
}
