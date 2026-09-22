package com.raishxn.gtna.common.machine.multiblock.module.steamElevator;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.widget.TankWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;

import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

/**
 * GTNL {@code SteamGreenhouseModule} port (LGPLv3, original by ScienceNotLeisure).
 *
 * <p>
 * GTNL is a CropsNH industrial farm: it stores seeds, tracks drop tables and grows crops across mode
 * phases. CropsNH is not available in GTNA, so this port keeps the greenhouse identity at the world
 * level instead: it consumes water and accelerates the growth of bone-mealable crops in its radius,
 * which is the same "irrigated greenhouse" outcome without the seed/produce bookkeeping.
 */
public class SteamGreenhouseModule extends SteamElevatorModuleMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            SteamGreenhouseModule.class, SteamElevatorModuleMachine.MANAGED_FIELD_HOLDER);

    /** GTNL {@code getWaterUsage()}. */
    public static final int WATER_PER_OPERATION = 16_000;
    public static final int RANGE = 16;
    private static final int CYCLE_TICKS = 100;

    public final NotifiableFluidTank waterTank;

    @Persisted
    @DescSynced
    private int progress;

    public SteamGreenhouseModule(IMachineBlockEntity holder, int tier) {
        super(holder, tier);
        this.waterTank = new NotifiableFluidTank(this, 1, 64_000, IO.IN);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public int getEffectRange() {
        return RANGE;
    }

    @Override
    public long getSteamUpkeep() {
        // GTNL getIndustrialFarmEUt().
        return 8192L;
    }

    @Override
    public void onElevatorTick(SteamElevator elevator) {
        if (!consumeSteam(getSteamUpkeep())) return;
        if (!(getLevel() instanceof ServerLevel level)) return;
        if (++progress < CYCLE_TICKS) return;
        progress = 0;

        FluidStack drained = waterTank.drainInternal(WATER_PER_OPERATION, IFluidHandler.FluidAction.SIMULATE);
        if (drained.getAmount() < WATER_PER_OPERATION) return;
        waterTank.drainInternal(WATER_PER_OPERATION, IFluidHandler.FluidAction.EXECUTE);

        BlockPos origin = getPos();
        int grown = 0;
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-RANGE, -4, -RANGE), origin.offset(RANGE, 4, RANGE))) {
            BlockState state = level.getBlockState(pos);
            if (!(state.getBlock() instanceof BonemealableBlock bonemealable)) continue;
            if (!bonemealable.isValidBonemealTarget(level, pos, state, false)) continue;
            bonemealable.performBonemeal(level, level.random, pos, state);
            if (++grown >= 16) break;
        }
    }

    @Override
    protected Widget createModuleUIWidget() {
        WidgetGroup group = screenGroup(150, 60);
        group.addWidget(new LabelWidget(5, 4, () -> "Greenhouse tier §b" + getModuleTier()));
        group.addWidget(new LabelWidget(5, 18, () -> "Water: §b" + waterTank.getFluidInTank(0).getAmount() + " mB"));
        group.addWidget(new TankWidget(waterTank.getStorages()[0], 5, 32, 18, 18, true, true));
        return group;
    }
}
