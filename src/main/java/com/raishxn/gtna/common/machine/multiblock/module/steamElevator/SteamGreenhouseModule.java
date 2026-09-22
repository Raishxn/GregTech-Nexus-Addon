package com.raishxn.gtna.common.machine.multiblock.module.steamElevator;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;

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
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;

/**
 * GTNL {@code SteamGreenhouseModule} port (LGPLv3, original by ScienceNotLeisure).
 *
 * <p>
 * GTNL is a CropsNH industrial farm: it stores seeds, tracks drop tables and grows crops across mode
 * phases. CropsNH is not available in GTNA, so this port keeps the greenhouse identity at the world
 * level instead: it consumes water from the module structure's <b>fluid input hatch</b> and
 * accelerates the growth of bone-mealable crops in its radius, which is the same "irrigated
 * greenhouse" outcome without the seed/produce bookkeeping.
 */
public class SteamGreenhouseModule extends SteamElevatorModuleMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            SteamGreenhouseModule.class, SteamElevatorModuleMachine.MANAGED_FIELD_HOLDER);

    /** GTNL {@code getWaterUsage()}. */
    public static final int WATER_PER_OPERATION = 16_000;
    public static final int RANGE = 16;
    private static final int CYCLE_TICKS = 100;

    @Persisted
    @DescSynced
    private int progress;

    public SteamGreenhouseModule(IMachineBlockEntity holder, int tier) {
        super(holder, tier);
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

    private static FluidStack water() {
        return new FluidStack(Fluids.WATER, WATER_PER_OPERATION);
    }

    @Override
    public void onElevatorTick(SteamElevator elevator) {
        if (!consumeSteam(getSteamUpkeep())) return;
        if (!(getLevel() instanceof ServerLevel level)) return;
        if (++progress < CYCLE_TICKS) return;
        progress = 0;

        if (!drainFluid(water(), WATER_PER_OPERATION)) return;

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
        WidgetGroup group = screenGroup(150, 44);
        group.addWidget(new LabelWidget(5, 4, () -> "Greenhouse tier §b" + getModuleTier()));
        group.addWidget(new LabelWidget(5, 16,
                () -> "Water in hatch: §b" + countFluid(water()) + " mB"));
        group.addWidget(new LabelWidget(5, 28, () -> "§7Water: input hatch"));
        return group;
    }
}
