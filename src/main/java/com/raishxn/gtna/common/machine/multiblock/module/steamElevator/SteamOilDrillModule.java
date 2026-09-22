package com.raishxn.gtna.common.machine.multiblock.module.steamElevator;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidVeinSavedData;
import com.gregtechceu.gtceu.api.gui.widget.TankWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;

import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

/**
 * GTNL {@code SteamOilDrillModule} port (LGPLv3, original by ScienceNotLeisure).
 *
 * <p>
 * GTNL reads GT's per-dimension underground-oil table and outputs a random vein fluid. 1.20.1's
 * equivalent is GTCEu's bedrock fluid veins, so the module pumps the fluid of the chunk it sits in
 * ({@link BedrockFluidVeinSavedData#getFluidInChunk}) into its own output tank. Tiers I/II/III
 * (GTNL tiers 2/3/4) increase the yield and shorten the cycle.
 */
public class SteamOilDrillModule extends SteamElevatorModuleMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            SteamOilDrillModule.class, SteamElevatorModuleMachine.MANAGED_FIELD_HOLDER);

    public final NotifiableFluidTank outputTank;

    @Persisted
    @DescSynced
    private int progress;

    public SteamOilDrillModule(IMachineBlockEntity holder, int tier) {
        super(holder, tier);
        this.outputTank = new NotifiableFluidTank(this, 1, 64_000, IO.OUT);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public long getSteamUpkeep() {
        // GTNL uses GTValues.VP = V * 30 / 32: 120 / 480 / 1920 for tiers I/II/III.
        int tier = Math.min(GTValues.V.length - 1, getModuleTier());
        return GTValues.V[tier] * 30L / 32L;
    }

    private int cycleTicks() {
        return Math.max(1, 1200 / (getModuleTier() - 1));
    }

    private int baseYield() {
        return 250 * (1 << Math.max(0, getModuleTier() - 2));
    }

    @Override
    public void onElevatorTick(SteamElevator elevator) {
        if (!consumeSteam(getSteamUpkeep())) return;
        if (!(getLevel() instanceof ServerLevel level)) return;
        if (outputTank.getFluidInTank(0).getAmount() >= outputTank.getTankCapacity(0)) return;

        if (++progress < cycleTicks()) return;
        progress = 0;

        int chunkX = SectionPos.blockToSectionCoord(getPos().getX());
        int chunkZ = SectionPos.blockToSectionCoord(getPos().getZ());
        BedrockFluidVeinSavedData data = BedrockFluidVeinSavedData.getOrCreate(level);
        Fluid fluid = data.getFluidInChunk(chunkX, chunkZ);
        if (fluid == null) return;

        int amount = 0;
        for (int i = 0; i < getModuleTier() - 1; i++) {
            amount += baseYield() * (1 + level.random.nextInt(4));
        }
        outputTank.fillInternal(new FluidStack(fluid, amount), IFluidHandler.FluidAction.EXECUTE);
        // GTNL's underground oil depletes on extraction; mirror that at a gentler rate.
        if (level.random.nextInt(25) == 0) {
            data.depleteVein(chunkX, chunkZ, 0, false);
        }
    }

    @Override
    protected Widget createModuleUIWidget() {
        WidgetGroup group = screenGroup(150, 52);
        group.addWidget(new LabelWidget(5, 5, () -> "Tier: §b" + getModuleTier() + " §r| Yield: §b" + baseYield()));
        group.addWidget(new TankWidget(outputTank.getStorages()[0], 5, 20, 18, 18, true, false));
        return group;
    }
}
